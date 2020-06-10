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

import com.gmail.br45entei.game.ui.MenuProvider;
import com.gmail.br45entei.game.ui.Window;

import java.security.SecureRandom;
import java.util.Objects;

import org.eclipse.swt.widgets.Menu;
import org.lwjgl.opengl.GL11;

/** Renderer is an interface which defines OpenGL related methods which are
 * then called by the {@link GLThread}'s render loop.
 *
 * @author Brian_Entei
 * @since 1.0 */
public interface Renderer {
	
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
	
	/** Gives this renderer a chance to handle any exceptions that it might
	 * throw.<br>
	 * If the exception is not handled, this renderer is deselected from the
	 * {@link GLThread} to prevent future unhandled exceptions.
	 * 
	 * @param ex The exception that this renderer threw
	 * @param method This renderer's method that threw the error
	 * @param params The method parameters (if any) that were passed in
	 * @return Whether or not this renderer has handled the exception */
	/*default */public boolean handleException(Throwable ex, String method, Object... params);/* {
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
		System.err.flush();
		return false;
	}*/
	
	//=======================================================================================================================
	
	/** A simple OpenGL demo that changes the background color a little bit
	 * each frame randomly.
	 *
	 * @author Brian_Entei */
	public static class ColorDemo implements Renderer, MenuProvider {
		
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
			/*if((System.currentTimeMillis() % 1000) <= 16) {
				Window.getWindow().getGLThread().fpsLog.addLast(String.format("deltaTime: %s", CodeUtil.limitDecimalNoRounding(deltaTime, 9, true)));
			}*/
			
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
			System.err.print(String.format("The ColorDemo renderer threw an exception while executing method %s(%s): ", method, parameters));
			ex.printStackTrace(System.err);
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
		public void onPopupMenuCreation(Menu menu) {
		}
		
		@Override
		public void onPopupMenuDeletion(Menu menu) {
		}
		
	}
	
	/** A simple OpenGL demo that changes the background color a little bit
	 * each frame randomly. */
	public static final Renderer colorDemo = new ColorDemo();
	
}
