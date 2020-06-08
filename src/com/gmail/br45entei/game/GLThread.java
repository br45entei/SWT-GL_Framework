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
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.swt.GLCanvas;

/** This class is the main OpenGL thread.
 *
 * @author Brian_Entei */
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
	
	protected volatile GLLoop glLoop = GLLoop.colorDemo;
	
	/** Creates a new GLThread that will use the given GLCanvas.
	 * 
	 * @param glCanvas The GLCanvas that this thread will use */
	public GLThread(GLCanvas glCanvas) {
		super("GLThread");
		this.setDaemon(true);
		this.glCanvas = glCanvas;
		
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
	
	public final boolean isRunning() {
		return this.running && this.isAlive();
	}
	
	public final boolean shouldBeRunning() {
		return this.shouldBeRunning && !this.glCanvas.isDisposed();
	}
	
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
	
	//===========================================================================================================================
	
	public final boolean isVsyncEnabled() {
		return this.vsync;
	}
	
	public final GLThread setVsyncEnabled(boolean vsync) {
		this.vsync = vsync;
		return this;
	}
	
	public double getTargetFramerate() {
		return (this.timer.getTargetFrequency() * 1000.0D) / this.timer.getTargetPeriodInMilliseconds();
	}
	
	public final GLThread setFramerate(double framerate, double period) {
		this.timer.setFrequency(framerate, period);
		return this;
	}
	
	public final GLThread setFramerate(double framerate) {
		return this.setFramerate(framerate, this.timer.getTargetPeriodInMilliseconds());
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
			
			GLLoop loop;
			while(this.shouldBeRunning()) {
				loop = this.glLoop;
				if(loop != null) {
					try {
						if(!loop.isInitialized()) {
							loop.initialize();
						}
						loop.render();
					} catch(Throwable ex) {
						boolean handled = false;
						try {
							handled = this.glLoop.handleException(ex);
						} catch(Throwable ex1) {
							ex.addSuppressed(ex1);
							handled = false;
						}
						if(!handled) {
							ex.printStackTrace();
							this.glLoop = null;
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
	
	public static interface GLLoop {
		
		public boolean isInitialized();
		
		public void initialize();
		
		public void render();
		
		public boolean handleException(Throwable ex);
		
		//=======================================================================================================================
		
		public static final GLLoop colorDemo = new GLLoop() {
			
			boolean initialized = false;
			
			final SecureRandom random = new SecureRandom();// A random source of data to use for our changing canvas color
			final float maxIncrement = 0.05f;// Each color channel will be changed by a random float value between 0 and this number
			volatile float r = 0.0f, g = this.random.nextFloat(), b = 1.0f;// The three color channels that we'll use to make our GLCanvas change color
			volatile boolean rUp = true, gUp = this.random.nextBoolean(),
					bUp = false;// The three booleans that will tell us what each color channel's direction of change is (up/down)
			volatile boolean ruWait = false, guWait = false, buWait = false;
			volatile boolean rdWait = false, gdWait = false, bdWait = false;
			
			@Override
			public boolean isInitialized() {
				return this.initialized;
			}
			
			@Override
			public void initialize() {
				
				this.initialized = true;
			}
			
			@Override
			public void render() {
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
			public boolean handleException(Throwable ex) {
				
				return true;
			}
		};
		
	}
	
	//===========================================================================================================================
	
}
