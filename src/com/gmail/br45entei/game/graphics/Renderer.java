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

import com.gmail.br45entei.game.graphics.GLThread.InitializationProgress;
import com.gmail.br45entei.game.ui.MenuProvider;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.thread.ThreadType;
import com.gmail.br45entei.thread.UsedBy;
import com.gmail.br45entei.util.StringUtil;

import java.util.Objects;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.swt.GLCanvas;

/** Renderer is an interface which defines OpenGL related methods which are
 * then called by the {@link GLThread}'s render loop.
 *
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public interface Renderer {
	
	/** @return The name of this {@link Renderer}
	 *
	 * @see ThreadType#UNSPECIFIED */
	@UsedBy(ThreadType.UNSPECIFIED)
	public String getName();
	
	/** @return Whether or not this {@link Renderer}'s
	 *         {@link #initialize(InitializationProgress)}
	 *         method has been called yet
	 * 		
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public boolean isInitialized();
	
	/** Called by the {@link GLThread}'s render loop just before it begins
	 * to use this {@link Renderer} for the first time.<br>
	 * <br>
	 * After this method has been called, {@link #isInitialized()} should return
	 * <tt>true</tt> (unless something has failed to initialize of course).
	 *
	 * @param progress A {@link InitializationProgress progress tracker} used by
	 *            the GLThread which allows you to optionally inform the
	 *            end-user as to the total progress of your renderer's
	 *            initialization by having it display on-screen
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public void initialize(InitializationProgress progress);
	
	/** Called by the {@link GLThread} when this {@link Renderer} has been
	 * selected for rendering.
	 *
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public void onSelected();
	
	/** Called by the {@link GLThread} when the {@link Window}'s viewport has
	 * changed.
	 *
	 * @param oldViewport The old viewport
	 * @param newViewport The new viewport
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public void onViewportChanged(Rectangle oldViewport, Rectangle newViewport);
	
	/** Called by the {@link GLThread}'s render loop once per frame.
	 *
	 * @param deltaTime The delta time of the current frame from the last
	 *            (with a framerate of <tt>60.0</tt>, this would typically
	 *            be around <tt>0.0166667</tt>)
	 * @param width The {@link GLCanvas}'s current {@link Window#getWidth() width}
	 * @param height The {@link GLCanvas}'s current {@link Window#getHeight() height}
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public void render(double deltaTime, int width, int height);
	
	/** Called by the {@link GLThread} when this {@link Renderer} has been
	 * unselected for rendering.
	 *
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public void onDeselected();
	
	/** &#064;{@link UsedBy}({@link UsedBy#value() value}={{@link ThreadType#OpenGL OpenGL}})<br>
	 * <br>
	 * Called by the {@link GLThread} when it is about to stop running, and is
	 * getting ready to destroy the GL context.<br>
	 * <br>
	 * After this method has been called, {@link #isInitialized()} should return
	 * <tt>false</tt>.
	 *
	 * @see ThreadType#OpenGL */
	@UsedBy(ThreadType.OpenGL)
	public void onCleanup();
	
	/** Gives this renderer a chance to handle any exceptions that it might
	 * throw.<br>
	 * If the exception is not handled, the {@link GLThread} un-selects this
	 * renderer to prevent future unhandled exceptions.
	 *
	 * @param ex The exception that this renderer threw
	 * @param method This renderer's method that threw the error
	 * @param params The method parameters (if any) that were passed in
	 * @return Whether or not this renderer has handled the exception
	 * @see ThreadType#UI
	 * @see ThreadType#OpenGL
	 * @see ThreadType#CONTROLLER */
	@UsedBy({ThreadType.UI, ThreadType.OpenGL, ThreadType.CONTROLLER})
	/*default */public boolean handleException(Throwable ex, String method, Object... params);/* {//@formatter:off
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
		System.err.println(String.format("The renderer \"%s\" threw an exception while executing method %s(%s)!", this.getName(), method, parameters));
		System.err.println(StringUtil.throwableToStr(ex));
		System.err.flush();
		return false;
	}*///@formatter:on
	
	//=======================================================================================================================
	
	/** A simple OpenGL demo that changes the background color a little bit
	 * each frame randomly.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;This renderer will display rapidly flashing colors when
	 * vertical-sync is disabled, or on systems without Vsync support.<br>
	 * If you or someone around you is epileptic, do not use this renderer
	 * unless you are absolutely sure vertical sync is enabled (with a refresh
	 * rate of at most 60).
	 *
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static class ColorDemo implements Renderer, MenuProvider {
		
		boolean initialized = false;
		
		final RandomColorGenerator randomColorGenerator = new RandomColorGenerator();
		/*final SecureRandom random = new SecureRandom();// A random source of data to use for our changing canvas color
		final float maxIncrement = 0.05f;// Each color channel will be changed by a random float value between 0 and this number
		volatile float r = 0.0f, g = this.random.nextFloat(), b = 1.0f;// The three color channels that we'll use to make our GLCanvas change color
		volatile boolean rUp = true, gUp = this.random.nextBoolean(),
				bUp = false;// The three booleans that will tell us what each color channel's direction of change is (up/down)
		volatile boolean ruWait = false, guWait = false, buWait = false;
		volatile boolean rdWait = false, gdWait = false, bdWait = false;*/
		
		@Override
		public String getName() {
			return "Color Demo";
		}
		
		@Override
		public boolean isInitialized() {
			return this.initialized;
		}
		
		@Override
		public void initialize(InitializationProgress progress) {
			//...
			
			progress.setProgress(1.0f);
			this.initialized = true;
		}
		
		@Override
		public void onSelected() {
		}
		
		@Override
		public void onViewportChanged(Rectangle oldViewport, Rectangle newViewport) {
			GL11.glViewport(newViewport.x, newViewport.y, newViewport.width, newViewport.height);// Set the viewport to match the glCanvas' size (and optional offset)
		}
		
		@Override
		public void render(double deltaTime, int width, int height) {
			/*if((System.currentTimeMillis() % 1000) <= 16) {
				Window.getWindow().getGLThread().fpsLog.addLast(String.format("deltaTime: %s", CodeUtil.limitDecimalNoRounding(deltaTime, 9, true)));
			}*/
			
			//GL11.glViewport(0, 0, Window.getWindow().getWidth(), Window.getWindow().getHeight());// Set the viewport to match the glCanvas' size (and optional offset)
			float[] rgb = this.randomColorGenerator.getColor();
			GL11.glClearColor(rgb[0], rgb[1], rgb[2], 1);
			//GL11.glClearColor(this.r, this.g, this.b, 1);// Set the clear color to a random color that changes a bit every frame
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT/* | GL11.GL_DEPTH_BUFFER_BIT*/);// Clear the color buffer, setting it to the clear color above
			
			/*//Update our r/g/b variables for the next frame:
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
			}*/
			
		}
		
		@Override
		public void onDeselected() {
		}
		
		@Override
		public void onCleanup() {
			this.initialized = false;
		}
		
		@Override
		public boolean handleException(Throwable ex, String method, Object... params) {
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
			System.err.print(String.format("The ColorDemo renderer threw an exception while executing method %s(%s):", method, parameters));
			System.err.println(StringUtil.throwableToStr(ex));
			System.err.flush();
			return true;
		}
		
		@Override
		public String getMenuName() {
			return "Demo Options";
		}
		
		@Override
		public void onMenuBarCreation(Menu menu) {
		}
		
		@Override
		public void onMenuBarDeletion(Menu menu) {
		}
		
		@Override
		public boolean providesPopupMenu() {
			return false;
		}
		
		@Override
		public void onPopupMenuCreation(Menu menu) {
		}
		
		@Override
		public void onPopupMenuDeletion(Menu menu) {
		}
		
		@Override
		public void updateMenuItems() {
		}
		
	}
	
	/** A simple OpenGL demo that changes the background color a little bit
	 * each frame randomly.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;This renderer will display rapidly flashing colors when
	 * vertical-sync is disabled, or on systems without Vsync support.<br>
	 * If you or someone around you is epileptic, do not use this renderer
	 * unless you are absolutely sure vertical sync is enabled (with a refresh
	 * rate of at most 60). */
	public static final Renderer colorDemo = new ColorDemo();
	
}
