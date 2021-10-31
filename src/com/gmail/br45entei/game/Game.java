/*******************************************************************************
 * 
 * Copyright © 2021 Brian_Entei (br45entei@gmail.com)
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
package com.gmail.br45entei.game;

import com.badlogic.gdx.controllers.Controller;
import com.gmail.br45entei.game.graphics.GLThread.InitializationProgress;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.ui.MenuProvider;
import com.gmail.br45entei.thread.ThreadType;
import com.gmail.br45entei.thread.UsedBy;

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
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
@UsedBy({ThreadType.UI, ThreadType.OpenGL, ThreadType.CONTROLLER})
public interface Game extends Renderer, InputCallback {
	
	/** GameAdapter is a helper interface which extends {@link Game} and
	 * implements all of its defined methods, except for {@link #getName()},
	 * {@link #isInitialized()}, {@link #initialize(InitializationProgress)},
	 * {@link #onCleanup()}, {@link #isInputInitialized()},
	 * {@link #inputInit()}, and {@link #inputCleanup()}.<br>
	 * <br>
	 * Classes implementing this interface may <em>optionally</em> override any
	 * of the methods defined by {@link Game} <em>except</em> for the above
	 * named methods, which <em>must</em> be implemented.
	 *
	 * @since 1.0
	 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
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
		
		@Override
		default public void onControllerConnected(Controller controller) {
		}
		
		@Override
		default public void onControllerDisconnected(Controller controller) {
		}
		
		@Override
		default void onControllerButtonDown(Controller controller, int button) {
		}
		
		@Override
		default void onControllerButtonRepeat(Controller controller, int button) {
		}
		
		@Override
		default void onControllerButtonUp(Controller controller, int button) {
		}
		
		@Override
		default void onControllerButtonDoubleTapped(Controller controller, int button) {
		}
		
		@Override
		default void onControllerAxisChanged(Controller controller, int axis, float oldValue, float newValue) {
		}
		
		//=============================================================================================
		
		/*@Override
		default public boolean handleException(Throwable ex, String method, Object... params) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < params.length; i++) {
				Object param = params[i];
				String toString;
				if(param == null || param.getClass().isPrimitive()) {
					toString = Objects.toString(param);
				} else {
					toString = param.toString();
					String className = param.getClass().getName();
					if(toString.startsWith(className.concat("@"))) {
						toString = className;
					}
				}
				
				sb.append(toString).append(i + 1 == params.length ? "" : ", ");
			}
			String parameters = sb.toString();
			System.err.println(String.format("The game \"%s\" threw an exception while executing method %s(%s)!", this.getName(), method, parameters));
			System.err.flush();
			return false;
		}*/
		
	}
	
}
