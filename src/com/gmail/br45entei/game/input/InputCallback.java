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
package com.gmail.br45entei.game.input;

import com.badlogic.gdx.controllers.Controller;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.util.CodeUtil;

import java.io.PrintStream;

/** Interface used to provide a way for listeners to receive and use data from
 * input events.<br>
 * <br>
 * <b>Note:</b>&nbsp;The methods that this interface defines are called by the
 * {@link com.gmail.br45entei.game.ui.Window Window}'s display thread.
 *
 * @since 1.0
 * @author Brian_Entei */
public interface InputCallback {
	
	/** @return Whether or not {@link #initialize()} has been called at least
	 *         once for this input callback */
	public boolean isInitialized();
	
	/** Called to allow this callback to initialize any resources that it will
	 * be using. */
	public void initialize();
	
	/** @return Whether or not this callback needs the mouse to be able to move
	 *         around freely over the {@link Mouse#getCursorCanvas() cursor
	 *         canvas} */
	default public boolean isModal() {
		return false;
	}
	
	/** Called once per frame by the {@link Window}'s display thread to allow
	 * this callback to receive and use input
	 * data.<br>
	 * If this InputCallback also implements {@link Renderer}, and it is not the
	 * Window's {@link Window#getActiveRenderer() active renderer}, then this
	 * method is <b>not</b> called.
	 * 
	 * @param deltaTime The delta time of the current frame from the last (will
	 *            be the same value in {@link #update(double)}) */
	public void input(double deltaTime);
	
	/** Called once per frame by the {@link Window}'s display thread to allow
	 * this callback to update anything it needs
	 * to before the next frame.<br>
	 * If this InputCallback also implements {@link Renderer}, and it is not the
	 * Window's {@link Window#getActiveRenderer() active renderer}, then this
	 * method is <b>not</b> called.
	 * 
	 * @param deltaTime The delta time of the current frame from the last (will
	 *            be the same value in {@link #input(double)}) */
	public void update(double deltaTime);
	
	/** Called whenever system cursor movement is detected by
	 * {@link Mouse#poll()}.
	 * 
	 * @param deltaX The amount of pixels that the cursor has moved horizontally
	 * @param deltaY The amount of pixels that the cursor has moved vertically
	 * @param oldX The cursor's old x coordinate in canvas-relative coordinates
	 * @param oldY The cursor's old y coordinate in canvas-relative coordinates
	 * @param newX The cursor's new x coordinate in canvas-relative coordinates
	 * @param newY The cursor's new y coordinate in canvas-relative
	 *            coordinates */
	public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY);
	
	/** Called whenever a mouse button is pressed while the system cursor is
	 * over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that was just pressed (1 is left, 2 is
	 *            middle, 3 is right, etc.)
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	public void onMouseButtonDown(int button);
	
	/** Called constantly while a mouse button is being held down when the
	 * system cursor was over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that is being held down (1 is left, 2 is
	 *            middle, 3 is right, etc.)
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	public void onMouseButtonHeld(int button);
	
	/** Called whenever a mouse button is released while the system cursor is
	 * over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that was just released (1 is left, 2 is
	 *            middle, 3 is right, etc.)
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	public void onMouseButtonUp(int button);
	
	/** Called whenever a mouse button is double-clicked while the system cursor
	 * is over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that was just double-clicked (1 is left, 2
	 *            is middle, 3 is right, etc.)
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	public void onMouseDoubleClick(int button);
	
	/** Called whenever the mouse's scrollwheel is scrolled while the system
	 * cursor is over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param vertical Whether or not the scroll was a vertical (true) or
	 *            horizontal (false) scroll
	 * @param count The number of 'notches' that the scrollwheel was moved */
	public void onMouseScroll(boolean vertical, int count);
	
	/** Called whenever a keyboard key is pressed.
	 * 
	 * @param key The {@link Keys Key} that was just pressed down */
	public void onKeyDown(int key);
	
	/** Called repeatedly while a keyboard key is being held down.
	 * 
	 * @param key The {@link Keys Key} that is being held down */
	public void onKeyHeld(int key);
	
	/** Called whenever a keyboard key is released.
	 * 
	 * @param key The {@link Keys Key} that was just released */
	public void onKeyUp(int key);
	
	/** Called whenever a {@link Controller} is connected to the system.
	 * 
	 * @param controller The controller that was just connected */
	public void onControllerConnected(Controller controller);
	
	/** Called whenever a {@link Controller} is removed from the system.
	 * 
	 * @param controller The controller that was just removed */
	public void onControllerDisconnected(Controller controller);
	
	/** Gives this callback a chance to handle any exceptions that it might
	 * throw.<br>
	 * If the exception is not handled, this callback is removed from the
	 * listeners queue to prevent future unhandled exceptions.
	 * 
	 * @param ex The exception that this callback threw
	 * @param method This callback's method that threw the error
	 * @param params The method parameters (if any) that were passed in
	 * @return Whether or not this callback has handled the exception */
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
		System.err.println(String.format("An InputCallback (class \"%s\") threw an exception while executing method %s(%s)!", this.getClass().getName(), method, parameters));
		System.err.flush();
		return false;
	}*///@formatter:on
	
	/** InputLogger is a class which implements {@link InputCallback} and logs
	 * the input it receives to a user-provided {@link PrintStream}.
	 *
	 * @author Brian_Entei */
	public static class InputLogger implements InputCallback {
		
		private volatile boolean initialized, printDeltaTime;
		private final PrintStream pr;
		
		/** Creates a new InputLogger with the specified {@link PrintStream}.
		 * 
		 * @param pr The PrintStream that input logs will be written to */
		public InputLogger(PrintStream pr) {
			this.pr = pr;
		}
		
		/** Creates a new InputLogger with the specified {@link PrintStream}.
		 * 
		 * @param pr The PrintStream that input logs will be written to
		 * @param printDeltaTimes Whether or not the
		 *            {@link InputCallback#input(double) deltaTime} should be
		 *            logged each frame */
		public InputLogger(PrintStream pr, boolean printDeltaTimes) {
			this.pr = pr;
			this.printDeltaTime = printDeltaTimes;
		}
		
		@Override
		public boolean isInitialized() {
			return this.initialized;
		}
		
		@Override
		public void initialize() {
			this.initialized = true;
		}
		
		@Override
		public void input(double deltaTime) {
			if(this.printDeltaTime) {
				this.pr.println(String.format("DeltaTime: %s", CodeUtil.limitDecimalNoRounding(deltaTime, 8, true)));
			}
		}
		
		@Override
		public void update(double deltaTime) {
		}
		
		@Override
		public void onMouseScroll(boolean vertical, int count) {
			this.pr.println(String.format("On mouse scrolled %s: %s", vertical ? "vertically" : "horizontally", Integer.toString(count)));
		}
		
		@Override
		public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY) {
			this.pr.println(String.format("On mouse moved: %s, %s (%s, %s --> %s, %s)", Integer.toString(deltaX), Integer.toString(deltaY), Integer.toString(oldX), Integer.toString(oldY), Integer.toString(newX), Integer.toString(newY)));
		}
		
		@Override
		public void onMouseButtonDown(int button) {
			this.pr.println(String.format("On mouse button down: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
		}
		
		@Override
		public void onMouseButtonHeld(int button) {
			this.pr.println(String.format("On mouse button held: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
		}
		
		@Override
		public void onMouseButtonUp(int button) {
			this.pr.println(String.format("On mouse button up: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
		}
		
		@Override
		public void onMouseDoubleClick(int button) {
			this.pr.println(String.format("On mouse button double click: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
		}
		
		@Override
		public void onKeyDown(int key) {
			this.pr.println(String.format("On key down: %s", Keys.getNameForKey(key)));
		}
		
		@Override
		public void onKeyHeld(int key) {
			this.pr.println(String.format("On key held: %s", Keys.getNameForKey(key)));
		}
		
		@Override
		public void onKeyUp(int key) {
			this.pr.println(String.format("On key up:   %s", Keys.getNameForKey(key)));
		}
		
		@Override
		public void onControllerConnected(Controller controller) {
			
		}
		
		@Override
		public void onControllerDisconnected(Controller controller) {
			
		}
		
		@Override
		public boolean handleException(Throwable ex, String method, Object... params) {
			ex.printStackTrace();
			return true;
		}
		
	}
	
}
