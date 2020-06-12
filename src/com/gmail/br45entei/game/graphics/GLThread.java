/*******************************************************************************
 * 
 * Copyright (C) 2020 Brian_Entei (br45entei@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 *******************************************************************************/
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.thread.ScreenshotHelper;
import com.gmail.br45entei.thread.VideoHelper;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.FrequencyTimer;
import com.gmail.br45entei.util.FrequencyTimer.TimerCallback;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.swt.GLCanvas;
import org.lwjgl.opengl.swt.GLData;

/** This class is the main OpenGL thread.
 *
 * @author Brian_Entei
 * @since 1.0 */
public final class GLThread extends Thread {
	
	protected final GLCanvas glCanvas;
	protected volatile GLCapabilities glCaps;
	protected volatile boolean[] state = {false};
	protected volatile boolean shouldBeRunning = false;
	protected volatile boolean vsync = false, lastVsync = false;
	protected volatile int lastSwap = 0;
	protected volatile boolean logFPS = true;
	protected final FrequencyTimer timer = new FrequencyTimer(60.0D, 1000.0D);
	protected final ConcurrentLinkedDeque<String> fpsLog = new ConcurrentLinkedDeque<>();
	
	protected volatile long nanoTime = System.nanoTime();
	protected volatile long lastFrameTime = this.nanoTime;
	protected volatile double deltaTime = (((this.nanoTime = System.nanoTime()) - this.lastFrameTime) + 0.0D) / 1000000000.0D;
	
	protected volatile Renderer renderer = Renderer.colorDemo;
	
	private final ConcurrentLinkedDeque<Runnable> tasksToRun = new ConcurrentLinkedDeque<>();
	
	private final ScreenshotHelper screenshotHelper = new ScreenshotHelper(this.state);
	private final VideoHelper videoHelper = new VideoHelper(this.state);
	private volatile boolean recordingWaitingOnRemainingFrames = false;
	private final Rectangle recordingViewport = new Rectangle(0, 0, 800, 600);
	
	/** Creates a new GLThread that will use the given GLCanvas.
	 * 
	 * @param glCanvas The GLCanvas that this thread will use */
	public GLThread(GLCanvas glCanvas) {
		super("GLThread");
		this.setDaemon(true);
		this.setPriority(Thread.MAX_PRIORITY);
		this.glCanvas = glCanvas;
		this.screenshotHelper.start();
		this.videoHelper.start();
		
		this.fpsLog.addFirst(new BigDecimal(this.deltaTime).toPlainString());
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
	
	/** {@inheritDoc} */
	@Override
	public final synchronized void start() {
		this.shouldBeRunning = true;
		super.start();
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
	public final synchronized void stopRunning(boolean waitFor) {
		this.shouldBeRunning = false;
		if(waitFor && Thread.currentThread() != this) {
			while(this.state[0]) {
				CodeUtil.sleep(10L);
			}
		}
	}
	
	/** Has this {@link GLThread} execute the given runnable at the nearest
	 * opportunity.<br>
	 * Tasks should ideally take no more than a couple milliseconds to execute,
	 * as tasks that take longer will slow down the render loop, resulting in
	 * fewer frames per second.
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
	 * This method is thread safe.
	 * 
	 * @return This GLThread */
	public final GLThread takeScreenshot() {
		this.tasksToRun.add(() -> {
			ScreenshotHelper.saveScreenshot(Window.getWindow().getViewport());
		});
		return this;
	}
	
	/** Saves a screenshot of the last rendered frame to file.<br>
	 * This method is thread safe.
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
		this.videoHelper.stopRecording();
		return this;
	}
	
	/** Tells this {@link GLThread} to stop recording video frames.<br>
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
		this.videoHelper.stopRecording();
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
	 * This method is thread safe.
	 * 
	 * @return The GLCapabilities that were returned by
	 *         {@link GL#createCapabilities(boolean)} */
	public final GLCapabilities getGLCapabilities() {
		return this.glCaps;
	}
	
	/** Returns the GLData that describes the current OpenGL context.<br>
	 * This method is thread safe.
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
			ex.addSuppressed(ex1);
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
			ex.printStackTrace(System.err);
			System.err.flush();
			return true;
		}
		return handled;
	}
	
	/** Returns the {@link Renderer renderer} that this GLThread is currently
	 * using to display graphics.<br>
	 * This method is thread safe.
	 * 
	 * @return The {@link Renderer renderer} that this GLThread is currently
	 *         using to display graphics */
	public final Renderer getRenderer() {
		return this.renderer;
	}
	
	/** Sets the renderer that this {@link GLThread} will attempt to use to
	 * display graphics.<br>
	 * This method is thread safe.
	 * 
	 * @param renderer The {@link Renderer renderer} that this GLThread will
	 *            attempt to use to display graphics
	 * @return True if the renderer */
	public final boolean setRenderer(final Renderer renderer) {
		if(Thread.currentThread() != this) {
			final Boolean[] rtrn = {null};
			this.tasksToRun.add(() -> {
				rtrn[0] = Boolean.valueOf(this.setRenderer(renderer));
			});
			Display display;
			while(rtrn[0] == null) {
				display = Display.getCurrent();
				if(display != null) {
					if(!display.readAndDispatch()) {
						CodeUtil.sleep(10L);
					}
				} else {
					CodeUtil.sleep(10L);
				}
			}
			return rtrn[0].booleanValue();
		}
		
		final Renderer oldRenderer = this.renderer;
		boolean initialized;
		try {
			initialized = renderer.isInitialized();
		} catch(Throwable ex) {
			handleRendererException(oldRenderer, ex, "isInitialized");
			return false;
		}
		if(!initialized) {
			try {
				renderer.initialize();
				try {
					initialized = renderer.isInitialized();
				} catch(Throwable ex) {
					handleRendererException(oldRenderer, ex, "isInitialized");
					return false;
				}
			} catch(Throwable ex) {
				handleRendererException(oldRenderer, ex, "initialize");
				return false;
			}
		}
		if(!initialized) {
			return false;
		}
		try {
			renderer.onSelected();
		} catch(Throwable ex) {
			handleRendererException(oldRenderer, ex, "onSelected");
			return false;
		}
		long nanoTime = System.nanoTime();
		long lastFrameTime = nanoTime;
		double deltaTime = (((nanoTime = System.nanoTime()) - lastFrameTime) + 0.0D) / 1000000000.0D;
		try {
			renderer.render(deltaTime);
		} catch(Throwable ex) {
			handleRendererException(oldRenderer, ex, "onSelected");
			return false;
		}
		this.glCanvas.swapBuffers();
		this.renderer = renderer;
		this.nanoTime = nanoTime;
		this.lastFrameTime = lastFrameTime;
		this.deltaTime = deltaTime;
		
		if(oldRenderer != null) {
			try {
				oldRenderer.onDeselected();
			} catch(Throwable ex) {
				handleRendererException(oldRenderer, ex, "onDeselected");
			}
		}
		return true;
	}
	
	//===========================================================================================================================
	
	/** Returns whether or not vertical sync is enabled.<br>
	 * This method is thread safe.
	 * 
	 * @return Whether or not vertical sync is enabled */
	public final boolean isVsyncEnabled() {
		return this.vsync;
	}
	
	/** Sets whether or not vertical sync is enabled.<br>
	 * This method is thread safe.
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
	 * This method is thread safe.
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
	 * This method is thread safe.
	 * 
	 * @param framerate The frames per period to set
	 * @param period The length of time after which the frame count will reset
	 *            to zero and begin counting anew
	 * @return This GLThread */
	public final GLThread setFPS(double framerate, double period) {
		this.timer.setFrequency(framerate, period);
		return this;
	}
	
	/** Sets the target frames per second that this GLThread will attempt to run
	 * at.<br>
	 * This method is thread safe.
	 * 
	 * @param framerate The frames per second to set
	 * @return This GLThread */
	public final GLThread setFPS(double framerate) {
		return this.setFPS(framerate, this.timer.getTargetPeriodInMilliseconds());
	}
	
	/** @return The current frame number being rendered<br>
	 *         This method is thread safe. */
	public final long getFrameID() {
		return this.timer.getCurrentFrameCount();
	}
	
	/** @return The total FPS (Frames Per Second) that was rendered in the last
	 *         second<br>
	 *         This method is thread safe. */
	public final long getLastFPS() {
		return this.timer.getLastFrameCount();
	}
	
	/** @return The current average number of frames per second being
	 *         rendered<br>
	 *         This method is thread safe. */
	public final double getCurrentAverageFPS() {
		return 1000.0 / this.timer.getAverageMillisecondsPerFrame();
	}
	
	/** @return The average number of frames per second that was rendered in the
	 *         last second<br>
	 *         This method is thread safe. */
	public final double getLastAverageFPS() {
		return 1000.0 / this.timer.getLastAverageMillisecondsPerFrame();
	}
	
	/** Returns whether or not FPS logging is enabled.<br>
	 * While enabled, this thread generates an FPS log every second.<br>
	 * This method is thread safe.
	 * 
	 * @return Whether or not FPS logging is enabled */
	public final boolean isFPSLoggingEnabled() {
		return this.logFPS;
	}
	
	/** Sets whether or not FPS logging will be enabled.<br>
	 * While enabled, this thread generates an FPS log every second.<br>
	 * This method is thread safe.
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
	 * This method is thread safe.
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
	 * This method is thread safe.
	 * 
	 * @return This GLThread */
	public final GLThread printFPSLog() {
		return this.printFPSLog(System.out);
	}
	
	//===========================================================================================================================
	
	protected final void _swapBuffers() {
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
		this.glCanvas.swapBuffers();
		if(!this.vsync) {
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
	
	protected final void runTasks() {
		this.recordedAFrame = false;
		Runnable task;
		while((task = this.tasksToRun.pollFirst()) != null) {
			try {
				task.run();
			} catch(Throwable ex) {
				System.err.print("A GLThread async task threw an exception: ");
				ex.printStackTrace(System.err);
				System.err.flush();
			}
		}
		if(!this.recordedAFrame && this.isRecording() && !this.isRecordingFinishingUp()) {
			this.videoHelper.recordFrame(this.recordingViewport);
		}
	}
	
	protected final void updateFrameTime() {
		this.lastFrameTime = this.nanoTime;
		this.nanoTime = System.nanoTime();
		this.deltaTime = ((this.nanoTime - this.lastFrameTime) + 0.0D) / 1000000000.0D;
	}
	
	protected final void _display() {
		Renderer renderer = this.renderer;
		if(renderer != null) {
			try {
				if(!renderer.isInitialized()) {
					renderer.initialize();
				}
				renderer.render(this.deltaTime);
			} catch(Throwable ex) {
				boolean handled = false;
				try {
					handled = renderer.handleException(ex, "render", Double.valueOf(this.deltaTime));
				} catch(Throwable ex1) {
					ex.addSuppressed(ex1);
					handled = false;
				}
				if(!handled) {
					ex.printStackTrace();
					this.renderer = null;
				}
			}
		}
	}
	
	/** Renders the next frame of graphics by calling
	 * {@link Renderer#render(double) renderer.render(deltaTime)}.<br>
	 * This method is <b>not</b> thread safe.
	 * 
	 * @return This GLThread
	 * @throws IllegalStateException Thrown if this method is called by any
	 *             other thread other than this {@link GLThread} */
	public GLThread display() {
		if(Thread.currentThread() != this) {
			throw new IllegalStateException("GLThread.display() may only be called from within the GLThread itself!");
		}
		this._display();
		return this;
	}
	
	/** {@inheritDoc} */
	@Override
	public void run() {
		this.state[0] = true;
		try {
			this.glCanvas.setCurrent();
			this.glCaps = GL.createCapabilities(this.glCanvas.getGLData().forwardCompatible);
			
			this.lastSwap = this.glCanvas.glGetSwapInterval();
			this.lastVsync = this.lastSwap == 1;
			
			this.nanoTime = System.nanoTime();
			this.updateFrameTime();
			
			while(this.shouldBeRunning()) {
				this._display();
				this._swapBuffers();
				this.runTasks();
				this.updateFrameTime();
			}
			
		} finally {
			this.state[0] = false;
			
			GL.destroy();
			this.glCanvas.deleteContext();
		}
	}
	
	//===========================================================================================================================
	
}
