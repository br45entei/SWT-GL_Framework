/*******************************************************************************
 * 
 * Copyright © 2022 Brian_Entei (br45entei@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 *******************************************************************************/
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.game.graphics.FontRender.GLFont;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.thread.FrequencyTimer;
import com.gmail.br45entei.thread.FrequencyTimer.TimerCallback;
import com.gmail.br45entei.thread.ScreenshotHelper;
import com.gmail.br45entei.thread.VideoHelper;
import com.gmail.br45entei.thread.ΔTimer;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.StringUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.swt.GLCanvas;
import org.lwjgl.opengl.swt.GLData;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

/** This class is the main OpenGL thread.
 *
 * @since 1.0
 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
public final class GLThread extends Thread {
	
	protected volatile Window window;
	protected final GLCanvas glCanvas;
	protected volatile GLCapabilities glCaps;
	protected final boolean[] state = {false};
	protected volatile boolean shouldBeRunning = false;
	protected volatile boolean vsync = false, lastVsync = false;
	protected volatile int lastSwap = 0;
	protected volatile boolean isVsyncAvailable = false;
	protected volatile boolean pauseRendering = false;
	protected volatile boolean logFPS = true;
	protected final FrequencyTimer timer = new FrequencyTimer(Window.getDefaultRefreshRate(), 1000.0D);
	protected volatile double lastSetFrequency = this.timer.getTargetFrequency();
	protected volatile double lastSetPeriod = this.timer.getTargetPeriodInMilliseconds();
	protected final ConcurrentLinkedDeque<String> fpsLog = new ConcurrentLinkedDeque<>();
	
	private volatile ΔTimer δTimer;
	
	protected volatile Renderer renderer = null;//Renderer.colorDemo;
	protected volatile int lastWidth, lastHeight;
	
	private final ConcurrentLinkedDeque<Runnable> tasksToRun = new ConcurrentLinkedDeque<>();
	
	private final ScreenshotHelper screenshotHelper = new ScreenshotHelper(this.state);
	private final VideoHelper videoHelper = new VideoHelper(this.state);
	private volatile boolean recordingWaitingOnRemainingFrames = false;
	private final Rectangle recordingViewport = new Rectangle(0, 0, 800, 600);
	
	/** Creates a new GLThread that will use the given {@link Window} and
	 * {@link GLCanvas}.
	 * 
	 * @param window The Window that this thread will use (may be
	 *            <tt><b>null</b></tt>)
	 * @param glCanvas The GLCanvas that this thread will use */
	public GLThread(Window window, GLCanvas glCanvas) {
		super("GLThread");
		this.setDaemon(true);
		this.setPriority(Thread.MAX_PRIORITY);
		
		Window current = Window.getCurrent();
		this.window = window == null ? (current == null ? Window.getWindow() : current) : window;
		this.glCanvas = glCanvas;
		
		//this.fpsLog.addFirst(new BigDecimal(this.ΔTime).toPlainString());
		this.timer.setCallback(new TimerCallback() {
			@Override
			public void onTick() {
			}
			
			@Override
			public void onSecond() {
				if(GLThread.this.logFPS) {
					GLThread.this.fpsLog.addLast(String.format("FPS: %s; Average FPS: %s; Last MPF: %s; Average MPF: %s; Last MPFPS: %s;", Double.toString(GLThread.this.timer.getLastFrameCount()), Double.toString(GLThread.this.timer.getTargetPeriodInMilliseconds() / GLThread.this.timer.getLastAverageMillisecondsPerFrame()), Double.toString(GLThread.this.timer.getLastMillisecondsPerFrame()), Double.toString(GLThread.this.timer.getLastAverageMillisecondsPerFrame()), Long.toString(GLThread.this.timer.getLastMillisecondsPerFramePerPeriod())));
				}
			}
		});
	}
	
	/** Creates a new GLThread that will use the given {@link GLCanvas}.
	 * 
	 * @param glCanvas The GLCanvas that this thread will use */
	public GLThread(GLCanvas glCanvas) {
		this(null, glCanvas);
	}
	
	/** {@inheritDoc} */
	@Override
	public final synchronized void start() {
		this.shouldBeRunning = true;
		super.start();
		this.screenshotHelper.start();
		this.videoHelper.start();
	}
	
	/** @return True if this {@link GLThread} is currently running */
	public final boolean isRunning() {
		return this.state[0] && this.isAlive();
	}
	
	/** @return True if this {@link GLThread} should be running */
	public final boolean shouldBeRunning() {
		return this.shouldBeRunning && !this.glCanvas.isDisposed();
	}
	
	/** @return Whether or not this {@link GLThread} is shutting down */
	public final boolean isShuttingDown() {
		return !this.shouldBeRunning();
	}
	
	/** Tells this GLThread that it should stop running.
	 * 
	 * @param waitFor Whether or not this method should cause the currently
	 *            running thread to sleep until this GLThread has stopped
	 *            running */
	public final void stopRunning(boolean waitFor) {
		this.shouldBeRunning = false;
		if(waitFor && Thread.currentThread() != this) {
			Display display;
			Window window;
			while(this.state[0]) {
				display = Display.getCurrent();
				if(display != null && !display.isDisposed()) {
					window = this.window == null ? Window.getCurrent() : this.window;
					if(window != null) {
						if(!window.swtLoop()) {
							CodeUtil.sleep(8L);//return;
						}
					} else {
						if(!display.readAndDispatch()) {
							CodeUtil.sleep(10L);
						}
					}
				} else {
					CodeUtil.sleep(10L);
				}
				if(this.getState() == Thread.State.BLOCKED) {
					System.err.println("The GLThread is blocked! Stack trace:");
					System.err.println(StringUtil.stackTraceElementsToStr(this.getStackTrace()));
					System.err.flush();
					System.err.println();
					System.err.println("Unable to wait for the GLThread to terminate...");
					//this.stop();// No need to do this, GLThread is a daemon thread.
					break;
				}
			}
		}
	}
	
	/** Has this {@link GLThread} execute the given runnable at the nearest
	 * opportunity. If this GLThread is the {@link Thread#currentThread()
	 * current thread}, the runnable is executed immediately.<br>
	 * Otherwise, the new task will be executed at the end of a rendered frame
	 * of graphics, after the color buffers are swapped (which is technically
	 * the beginning of the next frame).<br>
	 * <br>
	 * <b>Note:</b>&nbsp;Tasks should ideally take no more than a couple
	 * milliseconds to execute, as tasks that take longer will slow down the
	 * render loop, resulting in fewer frames per second.
	 * 
	 * @param runnable The runnable to run
	 * @return <tt>false</tt> if the code was enqueued; <tt>true</tt> if the
	 *         code was executed immediately
	 * @throws RejectedExecutionException Thrown if this GLThread has the same
	 *             number of tasks enqueued (or more) than it does milliseconds
	 *             in a frame */
	public final boolean asyncExec(Runnable runnable) throws RejectedExecutionException {
		if(runnable == null) {
			throw new NullPointerException();
		}
		if(Thread.currentThread() == this) {
			runnable.run();
			return true;
		}
		int size = this.tasksToRun.size();
		int max = Long.valueOf(Math.round(Math.floor(1000.0D / this.timer.getTargetFrequency()))).intValue();
		max = max <= 0 ? 16 : max;
		if(size >= max) {
			throw new RejectedExecutionException(String.format("This GLThread has %s/%s tasks already enqueued!", Integer.toString(size), Integer.toString(max)));
		}
		this.tasksToRun.add(runnable);
		return false;
	}
	
	/** Saves a screenshot of the last rendered frame to file.<br>
	 * This method is thread-safe.
	 * 
	 * @return This GLThread */
	public final GLThread takeScreenshot() {
		this.tasksToRun.add(() -> {
			ScreenshotHelper.saveScreenshot(Window.getWindow().getViewport());
		});
		return this;
	}
	
	/** Saves a screenshot of the last rendered frame to file.<br>
	 * This method is thread-safe.
	 * 
	 * @param x The x coordinate marking the leftmost edge of the desired
	 *            screenshot
	 * @param y The y coordinate marking the topmost edge of the desired
	 *            screenshot
	 * @param width The width of the desired screenshot
	 * @param height The height of the desired screenshot
	 * @return This GLThread
	 * @throws IndexOutOfBoundsException Thrown if the given viewport lays
	 *             outside of the {@link Window}'s current
	 *             {@link Window#getViewport() viewport}. */
	public final GLThread takeScreenshot(int x, int y, int width, int height) throws IndexOutOfBoundsException {
		Rectangle viewport = Window.getWindow().getViewport();
		if(x < 0 || y < 0 || width < 0 || height < 0 || (x + width) > (viewport.x + viewport.width) || (y + height) > (viewport.y + viewport.height)) {
			throw new IndexOutOfBoundsException(String.format("takeScreenshot: Viewport \"%s, %s, %s, %s\" is out of range! Current window viewport: %s, %s, %s, %s", Integer.toString(x), Integer.toString(y), Integer.toString(width), Integer.toString(height), Integer.toString(viewport.x), Integer.toString(viewport.y), Integer.toString(viewport.width), Integer.toString(viewport.height)));
		}
		this.tasksToRun.add(() -> {
			ScreenshotHelper.saveScreenshot(x, y, width, height);
		});
		return this;
	}
	
	/** Returns the helper thread that saves screenshots to disk.
	 * 
	 * @return The helper thread that saves screenshots to disk. */
	public final ScreenshotHelper getScreenshotHelper() {
		return this.screenshotHelper;
	}
	
	/** Returns whether or not this {@link GLThread} is capturing frames of
	 * video and enqueueing them to be encoded and written to file.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this {@link GLThread} is capturing video */
	public final boolean isRecording() {
		return this.videoHelper.isRecording() && this.videoHelper.shouldBeRecording();
	}
	
	/** Returns whether or not this {@link GLThread} is starting up a
	 * recording.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this GLThread is starting up a recording */
	public final boolean isRecordingStartingUp() {
		return !this.videoHelper.isRecording() && this.videoHelper.shouldBeRecording();
	}
	
	/** Returns whether or not this {@link GLThread} is finishing up a
	 * recording.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this GLThread is finishing up a recording */
	public final boolean isRecordingFinishingUp() {
		return this.recordingWaitingOnRemainingFrames || (this.videoHelper.isRecording() && !this.videoHelper.shouldBeRecording());
	}
	
	/** Tells this {@link GLThread} to open a new video file for writing.<br>
	 * The video's framerate will be what {@link #getRefreshRate()} returns.<br>
	 * This method is thread-safe.
	 * 
	 * @param x The x coordinate marking the leftmost edge of the desired
	 *            viewport to be captured
	 * @param y The y coordinate marking the topmost edge of the desired
	 *            viewport to be captured
	 * @param width The desired width of the video
	 * @param height The desired height of the video
	 * @return This GLThread */
	public final GLThread startRecording(int x, int y, int width, int height) {
		Rectangle viewport = Window.getWindow().getViewport();
		if(x < 0 || y < 0 || width < 0 || height < 0 || (x + width) > (viewport.x + viewport.width) || (y + height) > (viewport.y + viewport.height)) {
			throw new IndexOutOfBoundsException(String.format("startRecording: Viewport \"%s, %s, %s, %s\" is out of range! Current window viewport: %s, %s, %s, %s", Integer.toString(x), Integer.toString(y), Integer.toString(width), Integer.toString(height), Integer.toString(viewport.x), Integer.toString(viewport.y), Integer.toString(viewport.width), Integer.toString(viewport.height)));
		}
		this.recordingViewport.x = x;
		this.recordingViewport.y = y;
		this.recordingViewport.width = width;
		this.recordingViewport.height = height;
		this.timer.setFrequency(30.0D, 1000.0D);
		this.videoHelper.startRecording(this.getRefreshRate(), width, height);
		this.tasksToRun.add(() -> {
			this.recordedAFrame = this.videoHelper.recordBlankFrame();
			/*while(this.shouldBeRunning() && !this.videoHelper.isRecording() && this.videoHelper.shouldBeRecording()) {
				this._display();
				this._swapBuffers();
				this.updateFrameTime();
			}*/
		});
		return this;
	}
	
	/** Tells this {@link GLThread} to open a new video file for writing.<br>
	 * The video's framerate will be what {@link #getRefreshRate()} returns.<br>
	 * This method is thread-safe.
	 * 
	 * @param viewport The viewport to be captured
	 * @return This GLThread */
	public final GLThread startRecording(Rectangle viewport) {
		return this.startRecording(viewport.x, viewport.y, viewport.width, viewport.height);
	}
	
	/** Captures the specified viewport and enqueues it to be encoded into a
	 * frame of video data.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int, int)} was not
	 * called prior to calling this method, this method simply returns.
	 * 
	 * @return This GLThread */
	public final GLThread recordFrame() {
		Runnable code = () -> {
			this.recordedAFrame = this.videoHelper.recordFrame(Window.getWindow().getViewport());
		};
		if(Thread.currentThread() == this) {
			code.run();
			return this;
		}
		this.tasksToRun.add(code);
		return this;
	}
	
	/** 'Captures' a solid black frame of video and enqueues it to be encoded
	 * into a frame of video data.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int, int)} was not
	 * called prior to calling this method, this method simply returns.
	 * 
	 * @return This GLThread */
	public final GLThread recordBlankFrame() {
		Runnable code = () -> {
			this.recordedAFrame = this.videoHelper.recordBlankFrame();
		};
		if(Thread.currentThread() == this) {
			code.run();
			return this;
		}
		this.tasksToRun.add(code);
		return this;
	}
	
	/** Captures the specified viewport and enqueues it to be encoded into a
	 * frame of video data.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int, int)} was not
	 * called prior to calling this method, this method simply returns.
	 * 
	 * @param x The x coordinate marking the leftmost edge of the desired
	 *            frame
	 * @param y The y coordinate marking the topmost edge of the desired
	 *            frame
	 * @param width The width of the desired frame
	 * @param height The height of the desired frame
	 * @return This GLThread */
	public final GLThread recordFrame(int x, int y, int width, int height) {
		Rectangle viewport = Window.getWindow().getViewport();
		if(x < 0 || y < 0 || width < 0 || height < 0 || (x + width) > (viewport.x + viewport.width) || (y + height) > (viewport.y + viewport.height)) {
			throw new IndexOutOfBoundsException(String.format("recordFrame: Viewport \"%s, %s, %s, %s\" is out of range! Current window viewport: %s, %s, %s, %s", Integer.toString(x), Integer.toString(y), Integer.toString(width), Integer.toString(height), Integer.toString(viewport.x), Integer.toString(viewport.y), Integer.toString(viewport.width), Integer.toString(viewport.height)));
		}
		Runnable code = () -> {
			this.recordedAFrame = this.videoHelper.recordFrame(x, y, width, height);
		};
		if(Thread.currentThread() == this) {
			code.run();
			return this;
		}
		this.tasksToRun.add(code);
		return this;
	}
	
	/** Captures the specified viewport and enqueues it to be encoded into a
	 * frame of video data.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int, int)} was not
	 * called prior to calling this method, this method simply returns.
	 * 
	 * @param viewport The bounds of the frame to capture
	 * @return This GLThread */
	public final GLThread recordFrame(Rectangle viewport) {
		return this.recordFrame(viewport.x, viewport.y, viewport.width, viewport.height);
	}
	
	/** Tells this {@link GLThread} to stop recording video frames.<br>
	 * This method is thread-safe, but may <b>not</b> be called by the GLThread
	 * itself when <tt>waitForPendingFrames</tt> is set to <tt>true</tt>.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If <tt>waitForPendingFrames</tt> is set to
	 * <tt>false</tt>, any remaining frames that have yet to be encoded and
	 * written to file will be discarded, and the video file will end abruptly.
	 * 
	 * @param waitForPendingFrames Whether or not this method should block until
	 *            any remaining captured frames have finished being encoded and
	 *            written to file
	 * @return This GLThread */
	public final GLThread stopRecording(boolean waitForPendingFrames) {
		if(waitForPendingFrames) {
			if(Thread.currentThread() instanceof GLThread) {
				throw new IllegalArgumentException("Cannot make the GLThread wait for pending video frames to finish encoding!");
			}
			this.recordingWaitingOnRemainingFrames = true;
			try {
				this.recordedAFrame = this.videoHelper.recordBlankFrame();
				this.videoHelper.stopAcceptingNewFrames();
				Display display;
				while(this.videoHelper.isRecording() && this.videoHelper.getNumFramesLeftToEncode() > 0) {
					display = Display.getCurrent();
					if(display != null && !display.isDisposed()) {
						if(!display.readAndDispatch()) {
							CodeUtil.sleep(10L);
						}
					} else {
						CodeUtil.sleep(10L);
					}
				}
			} finally {
				this.recordingWaitingOnRemainingFrames = false;
			}
		}
		this.videoHelper.stopEncoding();
		return this;
	}
	
	/** Tells this {@link GLThread} to stop recording video frames, and causes
	 * the currently executing thread to sleep until it has done so.<br>
	 * This method is thread-safe, but may <b>not</b> be called by the GLThread.
	 * 
	 * @param waitCode A function that will be called while the method is
	 *            waiting for the video frames to finish being encoded.<br>
	 *            The function may return {@link Boolean#FALSE} to signify that
	 *            the method should stop waiting and stop the encoding process
	 *            prematurely.<br>
	 *            This may be <tt><b>null</b></tt>.
	 * @return This GLThread */
	public final GLThread stopRecording(Function<Void, Boolean> waitCode) {
		if(Thread.currentThread() instanceof GLThread) {
			throw new IllegalArgumentException("Cannot make the GLThread wait for pending video frames to finish encoding!");
		}
		this.recordingWaitingOnRemainingFrames = true;
		try {
			this.recordedAFrame = this.videoHelper.recordBlankFrame();
			this.videoHelper.stopAcceptingNewFrames();
			Display display;
			while(this.videoHelper.isRecording() && this.videoHelper.getNumFramesLeftToEncode() > 0) {
				if(waitCode != null) {
					try {
						Boolean result = waitCode.apply(null);
						if(result != null && !result.booleanValue()) {
							break;
						}
					} catch(Throwable ex) {
						ex.printStackTrace();
						waitCode = null;
					}
				} else {
					display = Display.getCurrent();
					if(display != null && !display.isDisposed()) {
						if(!display.readAndDispatch()) {
							CodeUtil.sleep(10L);
						}
					} else {
						CodeUtil.sleep(10L);
					}
				}
			}
		} finally {
			this.recordingWaitingOnRemainingFrames = false;
		}
		this.videoHelper.stopEncoding();
		return this;
	}
	
	/** Returns this {@link GLThread}'s {@link VideoHelper} which is used to
	 * capture videos and save them to file.<br>
	 * This method is thread-safe.
	 * 
	 * @return This {@link GLThread}'s {@link VideoHelper} */
	public final VideoHelper getVideoHelper() {
		return this.videoHelper;
	}
	
	/** Returns the GLCapabilities that were returned by
	 * {@link GL#createCapabilities(boolean)}<br>
	 * This method is thread-safe.
	 * 
	 * @return The GLCapabilities that were returned by
	 *         {@link GL#createCapabilities(boolean)} */
	public final GLCapabilities getGLCapabilities() {
		return this.glCaps;
	}
	
	/** Returns the GLData that describes the current OpenGL context.<br>
	 * This method is thread-safe.
	 * 
	 * @return The GLData that describes the current OpenGL context */
	public final GLData getGLData() {
		return this.glCanvas.getGLData();
	}
	
	//===========================================================================================================================
	
	/** Attempts to have the renderer handle the exception it threw, or handles
	 * it and returns false if the renderer failed to do even that.
	 * 
	 * @param renderer The renderer that threw an exception
	 * @param method The renderer's method that threw the exception
	 * @param params The parameters that were passed to the renderer's method
	 * @param ex The exception that was thrown
	 * @return Whether or not the renderer was able to handle the exception */
	public static final boolean handleRendererException(Renderer renderer, Throwable ex, String method, Object... params) {
		String name = renderer.getClass().getName();
		boolean handled = false;
		try {
			name = renderer.getName();
			handled = renderer.handleException(ex, method, params);
		} catch(Throwable ex1) {
			ex1.addSuppressed(ex);
			ex = ex1;
			handled = false;
		}
		if(!handled) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < params.length; i++) {
				Object param = params[i];
				String toString;
				if(param == null || param.getClass().isPrimitive()) {
					toString = Objects.toString(param);
				} else {
					toString = param.toString();
					if(toString.startsWith(param.getClass().getName().concat("@"))) {
						toString = param.getClass().getName();
					}
				}
				
				sb.append(toString).append(i + 1 == params.length ? "" : ", ");
			}
			String parameters = sb.toString();
			System.err.print(String.format("Renderer \"%s\" threw an exception while executing method %s(%s): ", name, method, parameters));
			System.err.println(StringUtil.throwableToStr(ex));
			System.err.flush();
			if(ex instanceof ThreadDeath) {
				ThreadDeath death = new ThreadDeath();
				death.addSuppressed(ex);
				throw death;
			}
		}
		return handled;
	}
	
	/** Returns the {@link Renderer renderer} that this GLThread is currently
	 * using to display graphics.<br>
	 * This method is thread-safe.
	 * 
	 * @return The {@link Renderer renderer} that this GLThread is currently
	 *         using to display graphics */
	public final Renderer getRenderer() {
		return this.renderer;
	}
	
	/** Sets the renderer that this {@link GLThread} will attempt to use to
	 * display graphics.<br>
	 * This method is thread-safe.
	 * 
	 * @param renderer The {@link Renderer renderer} that this GLThread will
	 *            attempt to use to display graphics
	 * @param readAndDispatch Whether or not {@link Display#readAndDispatch()}
	 *            should be called if the thread invoking this method is not
	 *            this GLThread, and a display is found for it
	 * @return Whether or not this {@link GLThread} was able to begin using the
	 *         specified renderer */
	public final boolean setRenderer(final Renderer renderer, final boolean readAndDispatch) {
		if(this.renderer == renderer) {
			return true;
		}
		
		if(!this.isAlive()) {
			if(this.getState() == Thread.State.TERMINATED) {
				return false;
			}
			this.renderer = renderer;
			return true;
		}
		if(Thread.currentThread() != this) {
			final Boolean[] rtrn = {null};
			Runnable task = () -> {
				rtrn[0] = Boolean.valueOf(this.setRenderer(renderer, false));
			};
			this.tasksToRun.add(task);
			Display display;
			Window window;
			long now, taskRemovalTime = 0L;
			while(this.isRunning() && rtrn[0] == null) {
				if(!this.tasksToRun.contains(task)) {
					now = System.currentTimeMillis();
					if(taskRemovalTime == 0L) {
						taskRemovalTime = now;
					} else {
						if(now - taskRemovalTime >= 500L) {
							return false;
						}
					}
				}
				display = readAndDispatch ? Display.getCurrent() : null;
				if(display != null) {
					window = this.window == null ? Window.getCurrent() : this.window;
					if(window != null) {
						if(!window.swtLoop()) {
							this.stopRunning(true);
							return false;
						}
					} else {
						if(!display.readAndDispatch()) {
							CodeUtil.sleep(10L);
						}
					}
				} else {
					CodeUtil.sleep(10L);
				}
			}
			return rtrn[0].booleanValue();
		}
		if(this.glCanvas.isDisposed()) {
			return false;
		}
		
		//System.out.println(String.format("GLThread.setRenderer(%s, %s);", (renderer == null ? "null" : renderer.getName()), Boolean.toString(readAndDispatch)));
		
		// (Run Tasks) We're technically in a running task right now (see above code logic),
		// but we'll go ahead and update the deltaTime and then start another 'new frame' here:
		
		// (Update) Update the deltaTime
		this.δTimer.getΔTime(true);
		
		try {
			// (Display) Clear any previous renderer's back buffer
			GL11.glClearColor(0, 0, 0, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			// (Swap) Swap the front and back buffers
			synchronized(this) {
				this.glCanvas.swapBuffers();
			}
			
			int lastWidth = this.lastWidth;
			int lastHeight = this.lastHeight;
			
			// (Run Tasks) Attempt to set the new renderer
			final Renderer oldRenderer = this.renderer;
			if(renderer != null) {
				boolean initialized;
				try {
					initialized = renderer.isInitialized();
				} catch(Throwable ex) {
					handleRendererException(renderer, ex, "isInitialized");
					return false;
				}
				if(!initialized) {
					RendererInitializationProgress rip = new RendererInitializationProgress(this, Arrays.asList(renderer));
					try {
						try {
							renderer.initialize(rip);
						} catch(Throwable ex) {
							handleRendererException(renderer, ex, "initialize");
							return false;
						}
						try {
							initialized = renderer.isInitialized();
						} catch(Throwable ex) {
							handleRendererException(renderer, ex, "isInitialized");
							return false;
						}
					} finally {
						rip.dispose();
					}
				}
				if(!initialized) {
					return false;
				}
				try {
					renderer.onSelected();
				} catch(Throwable ex) {
					handleRendererException(renderer, ex, "onSelected");
					return false;
				}
				
				Rectangle oldViewport = new Rectangle(0, 0, 0, 0);
				Rectangle newViewport = new Rectangle(0, 0, lastWidth, lastHeight);
				try {
					renderer.onViewportChanged(oldViewport, newViewport);
				} catch(Throwable ex) {
					handleRendererException(renderer, ex, "onViewportChanged", oldViewport, newViewport);
					return false;
				}
			}
			
			// (Update) Update the deltaTime
			final double ΔTime = this.δTimer.getΔTime(true);
			
			// Advance to the 'next frame'
			
			// (Display) Clear any previous renderer's front (now the back) buffer
			GL11.glClearColor(0, 0, 0, 1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			// (Display) ... and then render if applicable:
			if(renderer != null) {
				try {
					renderer.render(ΔTime, lastWidth, lastHeight);
				} catch(Throwable ex) {
					handleRendererException(renderer, ex, "render", Double.valueOf(ΔTime), Integer.valueOf(lastWidth), Integer.valueOf(lastHeight));
					return false;
				}
			}
			
			// (Swap) Swap the front and back buffers
			synchronized(this) {
				this.glCanvas.swapBuffers();
			}
			
			// (Run Tasks) Set the new renderer since it has worked thus far, and then unselect the old renderer:
			this.renderer = renderer;
			
			if(oldRenderer != null) {
				try {
					oldRenderer.onDeselected();
				} catch(Throwable ex) {
					handleRendererException(oldRenderer, ex, "onDeselected");
				}
			}
			return true;
		} finally {
			// (Update) The deltaTime will be updated soon since we're technically in a running task right now
		}
	}
	
	/** Sets the renderer that this {@link GLThread} will attempt to use to
	 * display graphics.<br>
	 * This method is thread-safe.
	 * 
	 * @param renderer The {@link Renderer renderer} that this GLThread will
	 *            attempt to use to display graphics
	 * @return Whether or not this {@link GLThread} was able to begin using the
	 *         specified renderer */
	public final boolean setRenderer(final Renderer renderer) {
		return this.setRenderer(renderer, true);
	}
	
	/** Returns the Δ (delta) time of the current frame from the last.<br>
	 * Returned values are in microseconds (milliseconds divided by 1000).<br>
	 * For a framerate of <tt>60.0</tt>, values are typically around
	 * <tt>0.01666670</tt>.
	 * 
	 * @return The Δ time of the current frame from the last
	 * @see Renderer#render(double) */
	public final double getDeltaTime() {
		return this.δTimer.getΔTime(false);
	}
	
	/** Returns the Δ (delta) time of the last frame from the one before it.<br>
	 * Returned values are in microseconds (milliseconds divided by 1000).<br>
	 * For a framerate of <tt>60.0</tt>, values are typically around
	 * <tt>0.01666670</tt>.
	 * 
	 * @return The Δ time of the last frame from the one before it
	 * @see Renderer#render(double) */
	public final double getLastDeltaTime() {
		return this.δTimer.getLastΔTime();
	}
	
	//===========================================================================================================================
	
	/** Returns whether or not vertical sync is enabled.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not vertical sync is enabled */
	public final boolean isVsyncEnabled() {
		return this.vsync;
	}
	
	/** Sets whether or not vertical sync is enabled.<br>
	 * This method is thread-safe.
	 * 
	 * @param vsync Whether or not vertical sync will be enabled
	 * @return This GLThread */
	public final GLThread setVsyncEnabled(boolean vsync) {
		this.vsync = vsync;
		return this;
	}
	
	/** Returns the current target framerate per period.<br>
	 * The period defaults to a second, but may be different if
	 * {@link #setFPS(double, double)} has been used.<br>
	 * This method is thread-safe.
	 * 
	 * @return The current target framerate. */
	public final double getTargetFPS() {
		return (this.timer.getTargetFrequency() * 1000.0D) / this.timer.getTargetPeriodInMilliseconds();
	}
	
	/** Returns this {@link GLThread}'s current target FPS (frames per
	 * second).<br>
	 * This method is thread-safe.
	 * 
	 * @return The current target FPS
	 * @see #getLastFPS()
	 * @see #getLastAverageFPS() */
	public int getRefreshRate() {
		return this.isVsyncEnabled() ? Window.getDefaultRefreshRate() : Long.valueOf(Math.round(Math.ceil(this.getTargetFPS()))).intValue();
	}
	
	/** Sets the target frame rate that this GLThread will attempt to run
	 * at.<br>
	 * This method is thread-safe.
	 * 
	 * @param framerate The frames per period to set
	 * @param period The length of time after which the frame count will reset
	 *            to zero and begin counting anew
	 * @return This GLThread */
	public final GLThread setFPS(double framerate, double period) {
		this.timer.setFrequency(framerate, period);
		this.lastSetFrequency = this.timer.getTargetFrequency();
		this.lastSetPeriod = this.timer.getTargetPeriodInMilliseconds();
		return this;
	}
	
	/** Sets the target frames per second that this GLThread will attempt to run
	 * at.<br>
	 * This method is thread-safe.
	 * 
	 * @param framerate The frames per second to set
	 * @return This GLThread */
	public final GLThread setFPS(double framerate) {
		return this.setFPS(framerate, this.timer.getTargetPeriodInMilliseconds());
	}
	
	/** @return The current frame number being rendered<br>
	 *         This method is thread-safe. */
	public final long getFrameID() {
		return this.timer.getCurrentFrameCount();
	}
	
	/** @return The total FPS (Frames Per Second) that was rendered in the last
	 *         second<br>
	 *         This method is thread-safe. */
	public final long getLastFPS() {
		return this.timer.getLastFrameCount();
	}
	
	/** @return The current average number of frames per second being
	 *         rendered<br>
	 *         This method is thread-safe. */
	public final double getCurrentAverageFPS() {
		return 1000.0 / this.timer.getAverageMillisecondsPerFrame();
	}
	
	/** @return The average number of frames per second that was rendered in the
	 *         last second<br>
	 *         This method is thread-safe. */
	public final double getLastAverageFPS() {
		return 1000.0 / this.timer.getLastAverageMillisecondsPerFrame();
	}
	
	/** Returns whether or not FPS logging is enabled.<br>
	 * While enabled, this thread generates an FPS log every second.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not FPS logging is enabled */
	public final boolean isFPSLoggingEnabled() {
		return this.logFPS;
	}
	
	/** Sets whether or not FPS logging will be enabled.<br>
	 * While enabled, this thread generates an FPS log every second.<br>
	 * This method is thread-safe.
	 * 
	 * @param logFPS Whether or not FPS logging should be enabled
	 * @return This GLThread */
	public final GLThread setLogFPS(boolean logFPS) {
		this.logFPS = logFPS;
		return this;
	}
	
	/** Prints the latest FPS log(s) to the given PrintStream.<br>
	 * One new log is created every second that this GLThread runs as long as
	 * {@link #setLogFPS(boolean) setLogFPS(false);} was never called.<br>
	 * This method is thread-safe.
	 * 
	 * @param pr The PrintStream to print the log to
	 * @return This GLThread */
	public final GLThread printFPSLog(PrintStream pr) {
		String log;
		while((log = this.fpsLog.pollFirst()) != null) {
			pr.println(log);
		}
		pr.flush();
		return this;
	}
	
	/** Prints the latest FPS log(s) to the standard output stream.<br>
	 * One new log is created every second that this GLThread runs as long as
	 * {@link #setLogFPS(boolean) setLogFPS(false);} was never called.<br>
	 * This method is thread-safe.
	 * 
	 * @return This GLThread */
	public final GLThread printFPSLog() {
		return this.printFPSLog(System.out);
	}
	
	//===========================================================================================================================
	
	protected final void _swapBuffers() {
		if(this.glCanvas.isDisposed()) {//if(!this.shouldBeRunning()) {
			return;
		}
		
		boolean isRecording = this.isRecordingStartingUp() || this.isRecording();
		if(this.timer.getTargetFrequency() != this.lastSetFrequency || this.timer.getTargetPeriodInMilliseconds() != this.lastSetPeriod) {
			if(!isRecording && (!this.isVsyncAvailable || !this.vsync)) {
				this.timer.setFrequency(this.lastSetFrequency, this.lastSetPeriod);
			}
		}
		
		if(!isRecording) {
			if(this.isVsyncAvailable) {
				if(this.vsync != this.lastVsync) {
					this.glCanvas.glSwapInterval(this.lastSwap = (this.vsync ? 1 : 0));
					this.lastVsync = this.vsync;
				}
				if(!this.lastVsync) {
					int currentRefreshRate = Long.valueOf(Math.round(this.timer.getTargetFrequency())).intValue();
					boolean tmpVsync = (currentRefreshRate == Window.getDefaultRefreshRate());
					if(tmpVsync != (this.lastSwap == 1)) {
						this.glCanvas.glSwapInterval(tmpVsync ? 1 : 0);
						this.lastSwap = this.glCanvas.glGetSwapInterval();
					}
				}
			} else {
				if(this.vsync != this.lastVsync) {
					this.timer.setFrequency(this.vsync ? Window.getDefaultRefreshRate() : this.lastSetFrequency, this.vsync ? 1000.0D : this.lastSetPeriod);
					this.lastVsync = this.vsync;
				}
			}
		} else {
			if(this.isVsyncAvailable) {
				if(this.lastSwap == 1) {
					this.glCanvas.glSwapInterval(0);
					this.lastSwap = this.glCanvas.glGetSwapInterval();
					this.lastVsync = this.lastSwap == 1;
				}
			}
		}
		
		synchronized(this) {
			try {
				this.glCanvas.swapBuffers();
			} catch(NullPointerException ex) {
				System.err.println("Failed to swap the front and back color buffers!");
				ex.printStackTrace(System.err);
				System.err.flush();
				this.shouldBeRunning = false;
				return;
			}
		}
		if(!this.isVsyncAvailable || !this.vsync) {
			this.timer.frequencySleep();
		}
	}
	
	/** Swaps the front and back color buffers, displaying the graphics that
	 * were just drawn.<br>
	 * This method is <b>not</b> thread safe.
	 * 
	 * @return This GLThread
	 * @throws IllegalStateException Thrown if this method is called by any
	 *             other thread other than this {@link GLThread} */
	public final GLThread swapBuffers() throws IllegalStateException {
		if(Thread.currentThread() != this) {
			throw new IllegalStateException("GLThread.swapBuffers() may only be called from within the GLThread itself!");
		}
		this._swapBuffers();
		return this;
	}
	
	private volatile boolean recordedAFrame = false;
	
	protected final void runTasks(boolean postSwapBuffers) {
		this.recordedAFrame = false;
		//Runnable task;
		//while((task = this.tasksToRun.peekFirst()) != null) {
		for(Runnable task : this.tasksToRun) {
			if(postSwapBuffers) {
				if(task instanceof PreSwapBuffersTask) {
					continue;
				}
			} else {
				if(task instanceof PostSwapBuffersTask) {
					continue;
				}
			}
			if(task instanceof PolledTask) {
				while(this.tasksToRun.remove(task)) {
				}
			}
			try {
				task.run();
			} catch(Throwable ex) {
				System.err.print("A GLThread async task threw an exception: ");
				ex.printStackTrace(System.err);
				System.err.flush();
				if(ex instanceof ThreadDeath) {
					ThreadDeath death = new ThreadDeath();
					death.addSuppressed(ex);
					throw death;
				}
			} finally {
				this.tasksToRun.remove(task);
			}
		}
		if(postSwapBuffers) {
			if(!this.recordedAFrame && this.isRecording() && !this.isRecordingFinishingUp()) {
				this.videoHelper.recordFrame(this.recordingViewport);
			}
		}
	}
	
	private final void _display() {
		if(this.pauseRendering) {
			return;
		}
		if(this.window == null) {
			this.window = Window.getCurrent();
			/*if(this.window == null) {
				this.window = Window.getWindow();
			}*/
		}
		
		Renderer renderer = this.renderer;
		if(renderer != null) {
			boolean initialized;
			try {
				initialized = renderer.isInitialized();
			} catch(Throwable ex) {
				if(!handleRendererException(renderer, ex, "isInitialized")) {
					this.renderer = null;
					return;
				}
				initialized = true;
			}
			if(!initialized) {
				/*RendererInitializationProgress rip = new RendererInitializationProgress(this, Arrays.asList(renderer));
				try {
					renderer.initialize(rip);
				} catch(Throwable ex) {
					boolean handled = false;
					try {
						handled = renderer.handleException(ex, "initialize");
					} catch(Throwable ex1) {
						ex.addSuppressed(ex1);
						handled = false;
					}
					if(!handled) {
						ex.printStackTrace();
						this.renderer = null;
						return;
					}
				} finally {
					rip.dispose();
				}*/
				return;
			}
			
			int width = this.window.getWidth();
			int height = this.window.getHeight();
			if(this.lastWidth != width || this.lastHeight != height) {
				Rectangle oldViewport = new Rectangle(0, 0, this.lastWidth, this.lastHeight);
				Rectangle newViewport = new Rectangle(0, 0, this.lastWidth = width, this.lastHeight = height);
				try {
					renderer.onViewportChanged(oldViewport, newViewport);
				} catch(Throwable ex) {
					boolean handled = false;
					try {
						handled = renderer.handleException(ex, "onViewportChanged", oldViewport, newViewport);
					} catch(Throwable ex1) {
						ex1.addSuppressed(ex);
						ex = ex1;
						handled = false;
					}
					if(!handled) {
						System.err.println(StringUtil.throwableToStr(ex));
						this.renderer = null;
						return;
					}
				}
			}
			
			double δTime = this.δTimer.getΔTime(true);
			try {
				renderer.render(δTime, width, height);
			} catch(Throwable ex) {
				boolean handled = false;
				try {
					handled = renderer.handleException(ex, "render", Double.valueOf(δTime), Integer.valueOf(width), Integer.valueOf(height));
				} catch(Throwable ex1) {
					ex1.addSuppressed(ex);
					ex = ex1;
					handled = false;
				}
				if(!handled) {
					System.err.println(StringUtil.throwableToStr(ex));
					this.renderer = null;
					return;
				}
			}
			
		}
	}
	
	/** Renders the next frame of graphics by calling
	 * {@link Renderer#render(double) renderer.render(deltaTime)}.<br>
	 * This method is <b>not</b> thread-safe.
	 * 
	 * @return This GLThread
	 * @throws IllegalStateException Thrown if this method is called by any
	 *             other thread other than this {@link GLThread} */
	public GLThread display() {
		if(Thread.currentThread() != this) {
			throw new IllegalStateException("GLThread.display() may only be called by the GLThread itself!");
		}
		this._display();
		return this;
	}
	
	/** Returns the {@link FrequencyTimer} that this {@link GLThread} uses when
	 * vsync is disabled.<br>
	 * This method is thread-safe.<br>
	 * Vsync is automatically enabled (if supported) when the target frequency
	 * matches the primary montior's {@link Window#getDefaultRefreshRate()
	 * default refresh rate}
	 * 
	 * @deprecated Use {@link #setFPS(double, double)} and/or
	 *             {@link #setVsyncEnabled(boolean)} instead.
	 * @return The FrequencyTimer that this GLThread uses when vsync is
	 *         disabled */
	@Deprecated
	public FrequencyTimer getFrequencyTimer() {
		return this.timer;
	}
	
	public Collection<Renderer> initializeRenderers(Collection<Renderer> renderers, boolean readAndDispatch) {
		if(!this.isAlive()) {
			return null;
		}
		//Don't initialize the active renderer, _display() will do that for us!
		//renderers.remove(this.renderer);//Nevermind, there can sometimes be strange side-effects when initializing the main renderer separately from the rest (namely the initialization progress bar thinking there is one more renderer than there actually are)
		
		if(Thread.currentThread() != this) {
			@SuppressWarnings("unchecked")
			final Collection<Renderer>[] rtrn = new Collection[] {null};
			PolledTask task = () -> {
				rtrn[0] = this.initializeRenderers(renderers, false);
			};
			this.tasksToRun.add(task);
			Window window;
			Display display;
			//long now, taskRemovalTime = 0L;
			while(this.isRunning() && rtrn[0] == null) {
				/*if(!this.tasksToRun.contains(task)) {
					now = System.currentTimeMillis();
					if(taskRemovalTime == 0L) {
						taskRemovalTime = now;
					} else {
						if(now - taskRemovalTime >= 500L) {
							return new ArrayList<>();
						}
					}
				}*/
				display = readAndDispatch ? Display.getCurrent() : null;
				if(display != null) {
					window = this.window == null ? Window.getCurrent() : this.window;
					if(window != null) {
						if(!window.swtLoop()) {
							return new ArrayList<>();
						}
					} else {
						if(!display.readAndDispatch()) {
							CodeUtil.sleep(10L);
						}
					}
				} else {
					CodeUtil.sleep(10L);
				}
			}
			return rtrn[0];
		}
		
		Collection<Renderer> failedRenderers = new ArrayList<>();
		
		final InitializationProgress rip = new RendererInitializationProgress(this, renderers);// Totally didn't make that the initials for this class on purpose! lmao
		try {
			/*for(Renderer renderer : renderers) {
				String name = null;
				try {
					name = renderer.getName();
				} catch(Throwable ex) {
					ex.printStackTrace();
					name = "<exception thrown>";
				}
				System.out.println(String.format("Renderer \"%s\" (%s)", name, renderer.getClass().getName()));
			}*/
			for(Renderer renderer : renderers) {
				if(!this.shouldBeRunning()) {
					failedRenderers.add(renderer);
					continue;
				}
				rip.setRendererBeingInitialized(renderer);
				boolean initialized;
				try {
					initialized = renderer.isInitialized();
				} catch(Throwable ex) {
					handleRendererException(renderer, ex, "isInitialized");
					failedRenderers.add(renderer);
					rip.removeRenderer(renderer);
					continue;
				}
				if(!initialized) {
					try {
						renderer.initialize(rip);
					} catch(Throwable ex) {
						handleRendererException(renderer, ex, "initialize");
						failedRenderers.add(renderer);
						rip.removeRenderer(renderer);
						continue;
					}
					try {
						initialized = renderer.isInitialized();
					} catch(Throwable ex) {
						handleRendererException(renderer, ex, "isInitialized");
						failedRenderers.add(renderer);
						rip.removeRenderer(renderer);
						continue;
					}
				}
				if(!initialized) {
					failedRenderers.add(renderer);
					rip.removeRenderer(renderer);
					continue;
				}
				rip.markRendererInitialized(renderer);
				rip.setRendererBeingInitialized(null);
			}
			return failedRenderers;
		} finally {
			rip.dispose();
		}
	}
	
	public Collection<Renderer> initializeRenderers(Collection<Renderer> renderers) {
		return this.initializeRenderers(renderers, true);
	}
	
	/** {@inheritDoc} */
	@Override
	public void run() {
		this.δTimer = ΔTimer.getΔTimer();
		this.state[0] = true;
		try {
			this.glCanvas.setCurrent();
			this.glCaps = GL.createCapabilities(this.glCanvas.getGLData().forwardCompatible);
			
			if(this.glCaps == null || !this.glCanvas.isCurrent()) {
				throw new IllegalStateException("Failed to set the canvas context as current and create the OpenGL Capabilities!");
			}
			
			try {
				this.isVsyncAvailable = this.glCanvas.glSwapInterval(this.vsync ? 1 : 0);
				this.lastSwap = this.glCanvas.glGetSwapInterval();
			} catch(UnsupportedOperationException ex) {
				if(ex.getMessage() != null && ex.getMessage().endsWith(" is unavailable")) {
					this.isVsyncAvailable = false;
					this.lastSwap = 0;
				} else {
					ex.printStackTrace(System.err);
					System.err.flush();
					System.exit(-1);
					return;
				}
			}
			this.lastSetFrequency = this.timer.getTargetFrequency();
			
			this.lastVsync = this.lastSwap == 1;
			
			System.out.println(GL11.glGetString(GL11.GL_VENDOR));
			System.out.println(GL11.glGetString(GL11.GL_RENDERER));
			System.out.println(GL11.glGetString(GL11.GL_VERSION));
			String lineSeparator = CodeUtil.getProperty("line.separator").concat("\t");
			
			// Thanks to Cornix over at the LWJGL forums for this if/else block: http://forum.lwjgl.org/index.php?topic=5400.msg28616#msg28616
			String extensions;
			if(this.getGLData().majorVersion >= 3) {
				extensions = "";
				int numExtensions = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
				for(int i = 0; i < numExtensions; i++) {
					extensions = extensions.concat(GL30.glGetStringi(GL11.GL_EXTENSIONS, i)).concat(lineSeparator);
				}
			} else {
				String list = GL11.glGetString(GL11.GL_EXTENSIONS);
				extensions = (list == null ? "" : list).trim().replace(" ", lineSeparator);
			}
			System.out.println("Extensions:".concat(lineSeparator).concat(extensions.trim()));
			System.out.println("GL 1.1.0: ".concat(Boolean.toString(GLUtil.isGL11Available())));
			System.out.println("\tGL11.GL_NV_vertex_buffer_unified_memory: ".concat(Boolean.toString(GLUtil.isGL11Available(this.glCaps, true))));//GL_NV_vertex_buffer_unified_memory
			System.out.println("GL 1.2.0: ".concat(Boolean.toString(GLUtil.isGL12Available())));
			System.out.println("GL 1.3.0: ".concat(Boolean.toString(GLUtil.isGL13Available())));
			System.out.println("GL 1.4.0: ".concat(Boolean.toString(GLUtil.isGL14Available())));
			System.out.println("GL 1.5.0: ".concat(Boolean.toString(GLUtil.isGL15Available())));
			System.out.println("GL 2.0.0: ".concat(Boolean.toString(GLUtil.isGL20Available())));
			System.out.println("GL 2.1.0: ".concat(Boolean.toString(GLUtil.isGL21Available())));
			System.out.println("GL 3.0.0: ".concat(Boolean.toString(GLUtil.isGL30Available())));
			System.out.println("GL 3.1.0: ".concat(Boolean.toString(GLUtil.isGL31Available())));
			System.out.println("GL 3.2.0: ".concat(Boolean.toString(GLUtil.isGL32Available())));
			System.out.println("GL 3.3.0: ".concat(Boolean.toString(GLUtil.isGL33Available())));
			System.out.println("GL 4.0.0: ".concat(Boolean.toString(GLUtil.isGL40Available())));
			System.out.println("GL 4.1.0: ".concat(Boolean.toString(GLUtil.isGL41Available())));
			System.out.println("GL 4.2.0: ".concat(Boolean.toString(GLUtil.isGL42Available())));
			System.out.println("GL 4.3.0: ".concat(Boolean.toString(GLUtil.isGL43Available())));
			System.out.println("GL 4.4.0: ".concat(Boolean.toString(GLUtil.isGL44Available())));
			System.out.println("GL 4.5.0: ".concat(Boolean.toString(GLUtil.isGL45Available())));
			System.out.println("GL 4.6.0: ".concat(Boolean.toString(GLUtil.isGL46Available())));
			
			//this.nanoTime = System.nanoTime();
			//this.updateFrameTime();
			
			while(this.glDisplayLoop()) {
			}
			
		} catch(Throwable ex) {
			if(ex instanceof ThreadDeath) {
				throw ex;
			}
			ex.printStackTrace(System.err);
			System.err.flush();
		} finally {
			this.state[0] = false;
			
			if(this.window != null) {
				for(Renderer renderer : this.window.getAvailableRenderers()) {
					try {
						renderer.onCleanup();
					} catch(Throwable ex) {
						if(!handleRendererException(renderer, ex, "onCleanup")) {
							this.window.unregisterRenderer(renderer);
							continue;
						}
					}
				}
			}
			
			GL.destroy();
			this.glCanvas.deleteContext();
		}
	}
	
	public boolean glDisplayLoop() {
		if(this.shouldBeRunning()) {
			this._display();
			this.runTasks(false);
			this._swapBuffers();
			this.runTasks(true);
		}
		return this.shouldBeRunning();
	}
	
	//===========================================================================================================================
	
	public boolean isRenderingPaused() {
		return this.pauseRendering;
	}
	
	public boolean setRenderingPaused(boolean pauseRendering) {
		boolean wasPaused = this.pauseRendering;
		this.pauseRendering = pauseRendering;
		return wasPaused;
	}
	
	public GLThread pauseRendering() {
		this.pauseRendering = true;
		return this;
	}
	
	public GLThread resumeRendering() {
		this.pauseRendering = false;
		return this;
	}
	
	public GLThread toggleRenderingPaused() {
		this.pauseRendering = !this.pauseRendering;
		return this;
	}
	
	//===========================================================================================================================
	
	/** @since 1.0
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static abstract class InitializationProgress {
		
		protected InitializationProgress() {
		}
		
		/** @return The renderer that is currently being initialized by the
		 *         {@link GLThread} */
		public abstract Renderer getRendererBeingInitialized();
		
		public abstract void markRendererInitialized(Renderer renderer);
		
		public abstract void setRendererBeingInitialized(Renderer renderer);
		
		public abstract void removeRenderer(Renderer renderer);
		
		/** @return The progress towards getting the current Renderer
		 *         initialized */
		public abstract float getProgress();
		
		/** @param percentage The new progress (between <tt>0.0f</tt> and
		 *            <tt>1.0f</tt> inclusive) to set towards getting the
		 *            current Renderer initialized */
		public abstract void setProgress(float percentage);
		
		public abstract String getBackgroundImage();
		
		public abstract boolean setBackgroundImage(String resourcePath);
		
		public abstract String getStatusMessage();
		
		public abstract void setStatusMessage(String msg);
		
		public abstract void set(float progressPercentage, String statusMessage);
		
		public abstract void set(float progressPercentage, String statusMessage, String backgroundImage);
		
		public abstract void setVsyncEnabled(boolean vsync);
		
		public abstract float dispose();
		
		public abstract boolean isDisposed();
		
	}
	
	private static final class RendererInitializationProgress extends InitializationProgress {
		
		private final GLThread glThread;
		private final boolean wasVsyncEnabled;
		
		private volatile boolean enableVsync = false;
		
		private final Map<Renderer, Float> rendererProgress = new HashMap<>();
		private final Map<Renderer, String> rendererBackgroundImages = new HashMap<>();
		private final Map<Renderer, String> rendererStatusMessages = new HashMap<>();
		private volatile Renderer currentRenderer = null;
		
		private volatile boolean finishedInitializing = true;// XXX This is true to start with so that the method calls in the constructor don't cause glDrawProgressScene to render prematurely
		
		private volatile GLFont font;
		private volatile String text = "Initializing Renderers, Please Wait...";
		private final double[] textBounds;
		private volatile double x, y;
		
		private volatile Texture background = null;
		private volatile Vector4f backgroundColorHue = null;
		private volatile boolean flipBackgroundHorizontally = false;
		private volatile boolean flipBackgroundVertically = false;
		
		private volatile float overallProgress = 0.0f;
		private volatile int numInitialized = 0;
		
		private volatile boolean isDisposed = false;
		
		public RendererInitializationProgress(GLThread glThread, Collection<Renderer> renderers) {
			super();
			this.glThread = glThread;
			this.wasVsyncEnabled = this.glThread.window == null ? this.glThread.isVsyncEnabled() : this.glThread.window.isVsyncEnabled();
			if(this.glThread.window != null) {
				this.glThread.window.setVSyncEnabled(this.enableVsync);
			} else {
				this.glThread.setVsyncEnabled(this.enableVsync);
			}
			
			for(Renderer renderer : renderers) {
				this.setRendererBeingInitialized(renderer);
				this.setProgress(0.0f);
			}
			this.setRendererBeingInitialized(null);
			
			if(!this.glThread.getGLData().forwardCompatible) {
				this.font = FontRender.createFont("Consolas", 12, false, false, true, true, false, false);
				GL11.glClearColor(0, 0, 0, 1);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadMatrixd(GLUtil.getOrthographicMatrixd(0, 0, this.glThread.lastWidth, this.glThread.lastHeight, 0.01, 1000.0));
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.textBounds = FontRender.sizeOf(this.font, this.text, 0, 0).getBounds();
				this.x = (this.glThread.lastWidth - this.textBounds[2]) / 2.0;
				this.y = (this.glThread.lastHeight - this.textBounds[3]) / 2.0;
				FontRender.drawString(this.font, this.text, this.x, this.y, 1, 1, 1, 1);
			} else {
				this.textBounds = new double[4];
			}
			
			this.finishedInitializing = false;
		}
		
		@Override
		public float dispose() {
			final float progress = this.getOverallProgress();
			if(this.isDisposed) {
				return progress;
			}
			this.isDisposed = true;
			
			this.finishedInitializing = true;
			this.rendererProgress.clear();
			this.rendererBackgroundImages.clear();
			this.rendererStatusMessages.clear();
			this.currentRenderer = null;
			this.text = null;
			for(int i = 0; i < this.textBounds.length; i++) {
				this.textBounds[i] = 0;
			}
			if(this.font != null) {
				this.font.destroy();
				this.font = null;
			}
			if(this.background != null) {
				this.background.dispose();
				this.background = null;
			}
			this.backgroundColorHue = null;
			this.flipBackgroundHorizontally = this.flipBackgroundVertically = false;
			
			if(this.glThread.window != null) {
				this.glThread.window.setVSyncEnabled(this.wasVsyncEnabled);
			} else {
				this.glThread.setVsyncEnabled(this.wasVsyncEnabled);
			}
			return progress;
		}
		
		@Override
		public boolean isDisposed() {
			return this.isDisposed;
		}
		
		@Override
		public Renderer getRendererBeingInitialized() {
			return this.currentRenderer;
		}
		
		@Override
		public void markRendererInitialized(Renderer renderer) {
			if(this.isDisposed) {
				return;
			}
			
			this.rendererProgress.put(renderer, Float.valueOf(1.0f));
			
			if(this.glThread.window != null) {
				this.glThread.window.setVSyncEnabled(this.wasVsyncEnabled);
			} else {
				this.glThread.setVsyncEnabled(this.wasVsyncEnabled);
			}
		}
		
		@Override
		public void setRendererBeingInitialized(Renderer renderer) {
			if(this.isDisposed) {
				return;
			}
			
			this.currentRenderer = renderer;
			if(this.background != null) {
				this.background.dispose();
				this.background = null;
			}
			this.backgroundColorHue = null;
			this.flipBackgroundHorizontally = this.flipBackgroundVertically = false;
			
			this.glDrawProgressScene();
		}
		
		@Override
		public void removeRenderer(Renderer failedRenderer) {
			if(this.isDisposed) {
				return;
			}
			
			if(this.currentRenderer == failedRenderer) {
				this.currentRenderer = null;
			}
			this.rendererProgress.remove(failedRenderer);
			this.rendererBackgroundImages.remove(failedRenderer);
			this.rendererStatusMessages.remove(failedRenderer);
			
			this.glDrawProgressScene();
		}
		
		public int getNumInitialized() {
			if(this.finishedInitializing) {
				return this.numInitialized;
			}
			int numInitialized = 0;
			for(Float progress : this.rendererProgress.values()) {
				if(progress != null && progress.floatValue() == 1.0f) {
					numInitialized++;
				}
			}
			return this.numInitialized = numInitialized;
		}
		
		public float getOverallProgress() {
			if(this.finishedInitializing) {
				return this.overallProgress;
			}
			float numRenderers = this.rendererProgress.size();
			float percentage = 0.0f;
			for(Float progress : this.rendererProgress.values()) {
				percentage += progress.floatValue();
			}
			return this.overallProgress = (percentage / numRenderers);
		}
		
		@Override
		public float getProgress() {
			Float progress = this.rendererProgress.get(this.currentRenderer);
			if(progress == null) {
				progress = Float.valueOf(0.0f);
				this.rendererProgress.put(this.currentRenderer, progress);
			}
			return progress.floatValue();
		}
		
		@Override
		public void setProgress(float percentage) {
			if(this.isDisposed) {
				return;
			}
			
			percentage = (percentage != percentage || Float.isInfinite(percentage) ? 0.0f : percentage);
			Float progress = Float.valueOf(percentage);
			Float oldProgress = this.rendererProgress.put(this.currentRenderer, progress);
			if(oldProgress == null || oldProgress.floatValue() != percentage) {
				this.glDrawProgressScene();
			}
		}
		
		@Override
		public String getBackgroundImage() {
			return this.rendererBackgroundImages.get(this.currentRenderer);
		}
		
		@Override
		public boolean setBackgroundImage(String resourcePath) {
			if(this.isDisposed) {
				return false;
			}
			boolean exists = TextureLoader.doesResourceExist(resourcePath, true);
			if(!exists) {
				resourcePath = null;
			}
			String oldBackgroundImage = this.rendererBackgroundImages.put(this.currentRenderer, resourcePath);
			if(oldBackgroundImage == null || !oldBackgroundImage.equals(resourcePath)) {
				this.glDrawProgressScene();
			}
			return exists;
		}
		
		@Override
		public String getStatusMessage() {
			return this.rendererStatusMessages.get(this.currentRenderer);
		}
		
		@Override
		public void setStatusMessage(String msg) {
			if(this.isDisposed) {
				return;
			}
			String oldMsg = this.rendererStatusMessages.put(this.currentRenderer, msg);
			if(oldMsg == null || !oldMsg.equals(msg)) {
				this.glDrawProgressScene();
			}
		}
		
		private void glDrawProgressScene() {
			if(this.isDisposed) {
				return;
			}
			if(!this.finishedInitializing) {
				try {
					final int width = this.glThread.window.getWidth();
					final int height = this.glThread.window.getHeight();
					
					this.x = (width / 2.0f) - (this.textBounds[2] / 2.0f);
					this.y = ((height / 2.0f) - (this.textBounds[3] / 2.0f)) - 50.0f;
					final double lineHeight = this.textBounds[3];
					
					this.text = String.format("Initializing Renderers (%s/%s), Please Wait...", Integer.toString(Math.min(this.rendererProgress.size(), this.getNumInitialized() + 1)), Integer.toString(this.rendererProgress.size()));
					final float progress = this.getProgress();
					final float overallProgress = this.getOverallProgress();
					final double[] progBounds = {//@formatter:off
							width * 0.25f,
							(height - this.y) - (lineHeight + 50.0f),
							width * 0.5f,
							50.0f
					};//@formatter:on
					final double[] progBar = {//@formatter:off
							progBounds[0] + 10,
							progBounds[1] + 10,
							(progBounds[2] - 20) * progress,
							(progBounds[3] - 20) / 2.0f
					};//@formatter:on
					final double[] overallProgBar = {//@formatter:off
							progBounds[0] + 10,
							progBounds[1] + 25,
							(progBounds[2] - 20) * overallProgress,
							(progBounds[3] - 20) / 2.0f
					};//@formatter:on
					
					final String msg = this.getStatusMessage();
					final double msgX, msgY;
					if(this.font != null && msg != null) {
						double[] textBounds = FontRender.sizeOf(this.font, msg, 0, 0).getBounds();
						msgX = (width / 2.0) - (textBounds[2] / 2.0);
						msgY = progBounds[1];//msgY = (height / 2.0) - (textBounds[3] / 2.0);
					} else {
						msgX = 0.0f;
						msgY = 0.0f;
					}
					
					String backgroundPath = this.getBackgroundImage();
					if(this.background == null && backgroundPath != null) {
						Runnable loadBackground = () -> {
							this.background = TextureLoader.createTexture(backgroundPath, //
									GL11.GL_TEXTURE_2D, // target
									GL11.GL_RGBA,       // dst pixel format
									GL11.GL_NEAREST,    // min filter
									GL11.GL_LINEAR);   // max filter);
						};
						this.glThread.asyncExec(loadBackground);
					}
					final Texture background = this.background;
					if(background != null) {
						this.backgroundColorHue = this.backgroundColorHue == null ? new Vector4f(1, 1, 1, 0) : this.backgroundColorHue;
					}
					
					final Runnable drawTask = () -> {
						if(this.glThread.lastWidth != width || this.glThread.lastHeight != height) {
							GL11.glViewport(0, 0, width, height);
						}
						GL11.glClearColor(0, 0, 0, 1);
						GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
						if(!this.glThread.glCaps.forwardCompatible) {
							GL11.glMatrixMode(GL11.GL_PROJECTION);
							GL11.glLoadMatrixd(GLUtil.getOrthographicMatrixd(0, 0, width, height, 0.01, 1000.0));
							GL11.glMatrixMode(GL11.GL_MODELVIEW);
							GL11.glLoadIdentity();
						}
						
						if(background != null && !this.glThread.glCaps.forwardCompatible) {
							GL11.glEnable(background.getTarget());
							GLUtil.glRenderQuad(background, new Vector2f(width, height), this.backgroundColorHue, this.flipBackgroundHorizontally, this.flipBackgroundVertically);
							GL11.glBindTexture(background.getTarget(), 0);
							GL11.glDisable(background.getTarget());
						}
						if(this.font != null && !this.glThread.glCaps.forwardCompatible) {
							FontRender.drawString(this.font, this.text, this.x, height - this.y, 1, 1, 1, 1);
							
							GLUtil.glPushColor();
							GLUtil.glColord(1.0f / 3.0f, 1.0f / 3.0f, 1.0f / 3.0f, 0.0f);
							GLUtil.glDrawRect2d(progBounds);
							GLUtil.glColord(0.0f, 198.0f / 255.0f, 0.0f, 0.0f);
							GLUtil.glDrawRect2d(progBar);
							GLUtil.glDrawRect2d(overallProgBar);
							GLUtil.glPopColor();
							
							if(msg != null) {
								FontRender.drawString(this.font, msg, msgX, msgY, 1, 1, 1, 1);
							}
						}
					};
					final Runnable swapTask = () -> {
						drawTask.run();
						if(this.glThread.window != null) {
							this.glThread.window.setVSyncEnabled(this.enableVsync);
						} else {
							this.glThread.setVsyncEnabled(this.enableVsync);
						}
						this.glThread._swapBuffers();
						if(this.glThread.getGLData().doubleBuffer) {
							drawTask.run();
							if(this.glThread.window != null) {
								this.glThread.window.setVSyncEnabled(this.enableVsync);
							} else {
								this.glThread.setVsyncEnabled(this.enableVsync);
							}
							this.glThread._swapBuffers();
						}
					};
					this.glThread.asyncExec(() -> {
						if(!this.finishedInitializing) {
							swapTask.run();
						}
					});
				} finally {
					if(Thread.currentThread() == this.glThread) {
						this.glThread.runTasks(false);
						this.glThread.runTasks(true);
					}
				}
			}
		}
		
		@Override
		public void set(float progressPercentage, String statusMessage) {
			if(this.isDisposed) {
				return;
			}
			
			boolean somethingChanged = false;
			
			Float oldProgress = this.rendererProgress.put(this.currentRenderer, Float.valueOf(progressPercentage));
			somethingChanged |= (oldProgress == null || oldProgress.floatValue() != progressPercentage);
			
			String oldMsg = this.rendererStatusMessages.put(this.currentRenderer, statusMessage);
			somethingChanged |= (oldMsg == null || !oldMsg.equals(statusMessage));
			
			if(somethingChanged) {
				this.glDrawProgressScene();
			}
		}
		
		@Override
		public void set(float progressPercentage, String statusMessage, String backgroundImage) {
			if(this.isDisposed) {
				return;
			}
			
			boolean somethingChanged = false;
			
			Float oldProgress = this.rendererProgress.put(this.currentRenderer, Float.valueOf(progressPercentage));
			somethingChanged |= (oldProgress == null || oldProgress.floatValue() != progressPercentage);
			
			String oldMsg = this.rendererStatusMessages.put(this.currentRenderer, statusMessage);
			somethingChanged |= (oldMsg == null || !oldMsg.equals(statusMessage));
			
			if(!TextureLoader.doesResourceExist(backgroundImage, true)) {
				backgroundImage = null;
			}
			String oldBackgroundImage = this.rendererBackgroundImages.put(this.currentRenderer, backgroundImage);
			somethingChanged |= (oldBackgroundImage == null || !oldBackgroundImage.equals(backgroundImage));
			
			if(somethingChanged) {
				this.glDrawProgressScene();
			}
		}
		
		@Override
		public void setVsyncEnabled(boolean vsync) {
			if(this.isDisposed) {
				return;
			}
			
			this.enableVsync = vsync;
		}
		
	}
	
	//===========================================================================================================================
	
	/** A GLThread asynchronous task that will be removed from the tasks queue
	 * before being run. */
	@FunctionalInterface
	public static interface PolledTask extends Runnable {
	}
	
	/** A GLThread asynchronous task that will be run after the scene has been
	 * drawn, but before the front and back color buffers are swapped.
	 *
	 * @since 1.0
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	@FunctionalInterface
	public static interface PreSwapBuffersTask extends Runnable {
		
		/** Identical to {@link PreSwapBuffersTask}, except that this class also
		 * extends {@link PolledTask}.
		 *
		 * @since 1.0
		 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
		@FunctionalInterface
		public static interface PolledPreSwapBuffersTask extends PreSwapBuffersTask, PolledTask {
		}
		
	}
	
	/** A GLThread asynchronous task that will be run after both the scene has
	 * been drawn and the front and back color buffers have been swapped.
	 *
	 * @since 1.0
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	@FunctionalInterface
	public static interface PostSwapBuffersTask extends Runnable {
		
		/** Identical to {@link PostSwapBuffersTask}, except that this class
		 * also extends {@link PolledTask}.
		 *
		 * @since 1.0
		 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
		@FunctionalInterface
		public static interface PolledPostSwapBuffersTask extends PostSwapBuffersTask, PolledTask {
		}
		
	}
	
}
