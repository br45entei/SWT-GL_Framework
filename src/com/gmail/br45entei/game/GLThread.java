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
package com.gmail.br45entei.game;

import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.FrequencyTimer;
import com.gmail.br45entei.util.FrequencyTimer.TimerCallback;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.swt.widgets.Display;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.swt.GLCanvas;

/** This class is the main OpenGL thread.
 *
 * @author Brian_Entei
 * @since 1.0 */
public final class GLThread extends Thread {
	
	protected final GLCanvas glCanvas;
	protected volatile GLCapabilities glCaps;
	protected volatile boolean running = false;
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
	
	/** Creates a new GLThread that will use the given GLCanvas.
	 * 
	 * @param glCanvas The GLCanvas that this thread will use */
	public GLThread(GLCanvas glCanvas) {
		super("GLThread");
		this.setDaemon(true);
		this.glCanvas = glCanvas;
		
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
		return this.running && this.isAlive();
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
			while(this.running) {
				CodeUtil.sleep(10L);
			}
		}
	}
	
	/** Has this {@link GLThread} execute the given runnable at the nearest
	 * opportunity.
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
	
	//===========================================================================================================================
	
	/** Attempts to have the renderer handle the exception it threw, or handles
	 * it and returns false if the renderer failed to even do that.
	 * 
	 * @param renderer The renderer that threw an exception
	 * @param problemMethod The renderer method that threw the exception
	 * @param ex The exception that was thrown
	 * @return Whether or not the renderer was able to handle the exception */
	protected static final boolean handleListenerException(Renderer renderer, String problemMethod, Throwable ex) {
		String name = renderer.getClass().getName();
		boolean handled = false;
		try {
			name = renderer.getName();
			handled = renderer.handleException(ex);
		} catch(Throwable ex1) {
			ex.addSuppressed(ex1);
			handled = false;
		}
		if(!handled) {
			System.err.print(String.format("Renderer \"%s\" threw an exception while executing %s: ", name, problemMethod));
			ex.printStackTrace(System.err);
			System.err.flush();
		}
		return handled;
	}
	
	public Renderer getRenderer() {
		return this.renderer;
	}
	
	public boolean setRenderer(final Renderer renderer) {
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
			handleListenerException(oldRenderer, "isInitialized()", ex);
			return false;
		}
		if(!initialized) {
			try {
				renderer.initialize();
			} catch(Throwable ex) {
				handleListenerException(oldRenderer, "initialize()", ex);
				return false;
			}
		}
		try {
			renderer.onSelected();
		} catch(Throwable ex) {
			handleListenerException(oldRenderer, "onSelected()", ex);
			return false;
		}
		
		this.renderer = renderer;
		if(oldRenderer != null) {
			try {
				oldRenderer.onDeselected();
			} catch(Throwable ex) {
				handleListenerException(oldRenderer, "onDeselected()", ex);
			}
		}
		return true;
	}
	
	//===========================================================================================================================
	
	/** Returns whether or not vertical sync is enabled.
	 * 
	 * @return Whether or not vertical sync is enabled */
	public final boolean isVsyncEnabled() {
		return this.vsync;
	}
	
	/** Sets whether or not vertical sync is enabled.
	 * 
	 * @param vsync Whether or not vertical sync will be enabled
	 * @return This GLThread */
	public final GLThread setVsyncEnabled(boolean vsync) {
		this.vsync = vsync;
		return this;
	}
	
	/** Returns the current target framerate per period.<br>
	 * The period defaults to a second, but may be different if
	 * {@link #setFPS(double, double)} has been used.
	 * 
	 * @return The current target framerate. */
	public double getTargetFPS() {
		return (this.timer.getTargetFrequency() * 1000.0D) / this.timer.getTargetPeriodInMilliseconds();
	}
	
	/** Sets the target frame rate that this GLThread will attempt to run at.
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
	 * at.
	 * 
	 * @param framerate The frames per second to set
	 * @return This GLThread */
	public final GLThread setFPS(double framerate) {
		return this.setFPS(framerate, this.timer.getTargetPeriodInMilliseconds());
	}
	
	/** @return The current frame number being rendered */
	public final long getFrameID() {
		return this.timer.getCurrentFrameCount();
	}
	
	/** @return The total FPS (Frames Per Second) that was rendered in the last
	 *         second */
	public final long getLastFPS() {
		return this.timer.getLastFrameCount();
	}
	
	/** @return The current average number of frames per second being
	 *         rendered */
	public final double getCurrentAverageFPS() {
		return 1000.0 / this.timer.getAverageMillisecondsPerFrame();
	}
	
	/** @return The average number of frames per second that was rendered in the
	 *         last second */
	public final double getLastAverageFPS() {
		return 1000.0 / this.timer.getLastAverageMillisecondsPerFrame();
	}
	
	/** Returns whether or not FPS logging is enabled.<br>
	 * While enabled, this thread generates an FPS log every second.
	 * 
	 * @return Whether or not FPS logging is enabled */
	public final boolean isFPSLoggingEnabled() {
		return this.logFPS;
	}
	
	/** Sets whether or not FPS logging will be enabled.<br>
	 * While enabled, this thread generates an FPS log every second.
	 * 
	 * @param logFPS Whether or not FPS logging should be enabled
	 * @return This GLThread */
	public final GLThread setLogFPS(boolean logFPS) {
		this.logFPS = logFPS;
		return this;
	}
	
	/** Prints the latest FPS log(s) to the given PrintStream.<br>
	 * One new log is created every second that this GLThread runs as long as
	 * {@link #setLogFPS(boolean) setLogFPS(false);} was never called.
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
	 * {@link #setLogFPS(boolean) setLogFPS(false);} was never called.
	 * 
	 * @return This GLThread */
	public final GLThread printFPSLog() {
		return this.printFPSLog(System.out);
	}
	
	//===========================================================================================================================
	
	/** {@inheritDoc} */
	@Override
	public void run() {
		this.running = true;
		try {
			this.glCanvas.setCurrent();
			this.glCaps = GL.createCapabilities(this.glCanvas.getGLData().forwardCompatible);
			
			this.lastSwap = this.glCanvas.glGetSwapInterval();
			this.lastVsync = this.lastSwap == 1;
			
			Runnable task;
			
			Renderer loop;
			this.nanoTime = System.nanoTime();
			this.lastFrameTime = this.nanoTime;
			this.nanoTime = System.nanoTime();
			while(this.shouldBeRunning()) {
				this.deltaTime = ((this.nanoTime - this.lastFrameTime) + 0.0D) / 1000000000.0D;
				
				loop = this.renderer;
				if(loop != null) {
					try {
						if(!loop.isInitialized()) {
							loop.initialize();
						}
						loop.render(this.deltaTime);
					} catch(Throwable ex) {
						boolean handled = false;
						try {
							handled = this.renderer.handleException(ex);
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
				
				//==========================================================================================================
				
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
				
				while((task = this.tasksToRun.pollFirst()) != null) {
					try {
						task.run();
					} catch(Throwable ex) {
						System.err.print("A GLThread async task threw an exception: ");
						ex.printStackTrace(System.err);
						System.err.flush();
					}
				}
				
				this.lastFrameTime = this.nanoTime;
				this.nanoTime = System.nanoTime();
			}
			
		} finally {
			try {
				GL.destroy();
				this.glCanvas.deleteContext();
			} finally {
				this.running = false;
			}
		}
	}
	
	/** Renderer is an interface which defines OpenGL related methods which are
	 * then called by the {@link GLThread}'s render loop.
	 *
	 * @author Brian_Entei
	 * @since 1.0 */
	public static interface Renderer {
		
		/** @return The name of this {@link Renderer} */
		public String getName();
		
		/** @return Whether or not this {@link Renderer}'s {@link #initialize()}
		 *         method has been called yet */
		public boolean isInitialized();
		
		/** Called by the {@link GLThread}'s render loop just before it begins
		 * to use this {@link Renderer} for the first time. */
		public void initialize();
		
		/** Called by the {@link GLThread} when this {@link Renderer} has been
		 * selected for rendering. */
		public void onSelected();
		
		/** Called by the {@link GLThread}'s render loop once per frame.
		 * 
		 * @param deltaTime The delta time of the current frame from the last
		 *            (with a framerate of <tt>60.0</tt>, this would typically
		 *            be around <tt>0.0166667</tt>) */
		public void render(double deltaTime);
		
		/** Called by the {@link GLThread} when this {@link Renderer} has been
		 * unselected for rendering. */
		public void onDeselected();
		
		public boolean handleException(Throwable ex);
		
		//=======================================================================================================================
		
		/** A simple OpenGL demo that changes the background color a little bit
		 * each frame randomly. */
		public static final Renderer colorDemo = new Renderer() {
			
			boolean initialized = false;
			
			final SecureRandom random = new SecureRandom();// A random source of data to use for our changing canvas color
			final float maxIncrement = 0.05f;// Each color channel will be changed by a random float value between 0 and this number
			volatile float r = 0.0f, g = this.random.nextFloat(), b = 1.0f;// The three color channels that we'll use to make our GLCanvas change color
			volatile boolean rUp = true, gUp = this.random.nextBoolean(),
					bUp = false;// The three booleans that will tell us what each color channel's direction of change is (up/down)
			volatile boolean ruWait = false, guWait = false, buWait = false;
			volatile boolean rdWait = false, gdWait = false, bdWait = false;
			
			@Override
			public String getName() {
				return "Color Demo";
			}
			
			@Override
			public boolean isInitialized() {
				return this.initialized;
			}
			
			@Override
			public void initialize() {
				//...
				
				this.initialized = true;
			}
			
			@Override
			public void onSelected() {
			}
			
			@Override
			public void render(double deltaTime) {
				if((System.currentTimeMillis() % 1000) <= 16) {
					Window.getWindow().getGLThread().fpsLog.addLast(String.format("deltaTime: %s", CodeUtil.limitDecimalNoRounding(deltaTime, 9, true)));
				}
				
				GL11.glViewport(0, 0, Window.getWindow().getWidth(), Window.getWindow().getHeight());// Set the viewport to match the glCanvas' size (and optional offset)
				GL11.glClearColor(this.r, this.g, this.b, 1);// Set the clear color to a random color that changes a bit every frame
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT/* | GL11.GL_DEPTH_BUFFER_BIT*/);// Clear the color buffer, setting it to the clear color above
				
				//Update our r/g/b variables for the next frame:
				if(!this.rdWait && !this.ruWait) {// If the color channel isn't staying on the same color for a while:
					this.r += (this.random.nextFloat() * this.maxIncrement) * (this.rUp ? 1.0f : -1.0f);
				} else {// The color channel is currently 'waiting', so let's have a slightly rarer random chance to let it continue
					if(this.rdWait && this.random.nextInt(256) == 42) {
						this.rdWait = false;
					}
					if(this.ruWait && this.random.nextInt(256) == 42) {
						this.ruWait = false;
					}
				}
				if(!this.gdWait && !this.guWait) {// ...above steps repeated for the green and blue color channels:
					this.g += (this.random.nextFloat() * this.maxIncrement) * (this.gUp ? 1.0f : -1.0f);
				} else {
					if(this.gdWait && this.random.nextInt(256) == 42) {
						this.gdWait = false;
					}
					if(this.guWait && this.random.nextInt(256) == 42) {
						this.guWait = false;
					}
				}
				if(!this.bdWait && !this.buWait) {
					this.b += (this.random.nextFloat() * this.maxIncrement) * (this.bUp ? 1.0f : -1.0f);
				} else {
					if(this.bdWait && this.random.nextInt(256) == 42) {
						this.bdWait = false;
					}
					if(this.buWait && this.random.nextInt(256) == 42) {
						this.buWait = false;
					}
				}
				
				if(this.r >= 1.0f && this.rUp) {// Check if the color channel has overshot the maximum value (which is 1.0f)
					this.rUp = false;// Set the direction to decreasing
					this.r = 1.0f;// Cap the color channel to the maximum (1.0f) just in case it overshot
					if(!this.rdWait && !this.ruWait && this.random.nextInt(100) == 42) {// Have a random chance to make the color channel stay on the same color for a while (while going up)
						this.rdWait = true;
					}
				}
				if(this.r <= 0.0f && !this.rUp) {// Check if the color channel has undershot the minimum value (which is 0.0f)
					this.rUp = true;// Set the direction to increasing
					this.r = 0.0f;// Cap the color channel to the minimum (0.0f) just in case it undershot
					if(!this.rdWait && !this.ruWait && this.random.nextInt(100) == 42) {// Have a random chance to make the color channel stay on the same color for a while (while going down)
						this.ruWait = true;
					}
				}
				if(this.g >= 1.0f && this.gUp) {// ...above steps repeated for the green and blue color channels:
					this.gUp = false;
					this.g = 1.0f;
					if(!this.gdWait && !this.guWait && this.random.nextInt(100) == 42) {
						this.gdWait = true;
					}
				}
				if(this.g <= 0.0f && !this.gUp) {
					this.gUp = true;
					this.g = 0.0f;
					if(!this.gdWait && !this.guWait && this.random.nextInt(100) == 42) {
						this.guWait = true;
					}
				}
				if(this.b >= 1.0f && this.bUp) {
					this.bUp = false;
					this.b = 1.0f;
					if(!this.bdWait && !this.buWait && this.random.nextInt(100) == 42) {
						this.bdWait = true;
					}
				}
				if(this.b <= 0.0f && !this.bUp) {
					this.bUp = true;
					this.b = 0.0f;
					if(!this.bdWait && !this.buWait && this.random.nextInt(100) == 42) {
						this.buWait = true;
					}
				}
				
			}
			
			@Override
			public void onDeselected() {
			}
			
			@Override
			public boolean handleException(Throwable ex) {
				ex.printStackTrace();
				return true;
			}
		};
		
	}
	
	//===========================================================================================================================
	
}
