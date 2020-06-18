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

import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.ui.MenuProvider;

import java.util.Objects;

import org.eclipse.swt.graphics.Rectangle;

/** Game is an interface which ties {@link Renderer} and {@link InputCallback}
 * together as one, making it easier for developers to create their own 3D
 * desktop applications.<br>
 * <br>
 * If you would like to add context menus to your game, have it implement
 * {@link MenuProvider}.<br>
 * <br>
 * <b>Note:</b>&nbsp;The methods that this interface provides are called by
 * different threads, depending on what super interface defines them.<br>
 * While this will have been taken into account behind the scenes, you should
 * still take care to ensure that code from one thread doesn't attempt to call
 * code intended for another thread (e.g. OpenGL code being called by the
 * Window's display thread or vice versa), or exceptions may be thrown.
 *
 * @author Brian_Entei
 * @since 1.0 */
public interface Game extends Renderer, InputCallback {
	
	/** GameAdapter is a helper interface which extends {@link Game} and
	 * implements all of its defined methods, except for {@link #getName()},
	 * {@link #isInitialized()}, and {@link #initialize()}.<br>
	 * <br>
	 * Classes implementing this interface may <em>optionally</em> override any
	 * of the methods defined by {@link Game} <em>except</em> for the above
	 * named methods, which <em>must</em> be implemented.
	 *
	 * @author Brian_Entei */
	public static interface GameAdapter extends Game {
		
		@Override
		default public void onSelected() {
		}
		
		@Override
		default void onViewportChanged(Rectangle oldViewport, Rectangle newViewport) {
		}
		
		@Override
		default public void render(double deltaTime) {
		}
		
		@Override
		default public void onDeselected() {
		}
		
		//=============================================================================================
		
		@Override
		default public boolean isModal() {
			return true;
		}
		
		@Override
		default public void input(double deltaTime) {
		}
		
		@Override
		default public void update(double deltaTime) {
		}
		
		@Override
		default public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY) {
		}
		
		@Override
		default public void onMouseButtonDown(int button) {
		}
		
		@Override
		default public void onMouseButtonHeld(int button) {
		}
		
		@Override
		default public void onMouseButtonUp(int button) {
		}
		
		@Override
		default public void onMouseDoubleClick(int button) {
		}
		
		@Override
		default public void onMouseScroll(boolean vertical, int count) {
		}
		
		@Override
		default public void onKeyDown(int key) {
		}
		
		@Override
		default public void onKeyHeld(int key) {
		}
		
		@Override
		default public void onKeyUp(int key) {
		}
		
		//=============================================================================================
		
		@Override
		default public boolean handleException(Throwable ex, String method, Object... params) {
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
			System.err.println(String.format("The game \"%s\" threw an exception while executing method %s(%s)!", this.getName(), method, parameters));
			System.err.flush();
			return false;
		}
		
	}
	
}
