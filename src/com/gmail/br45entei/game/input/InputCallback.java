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

import com.gmail.br45entei.game.input.Keyboard.Keys;

/** Interface used to provide a way for listeners to receive and use data from
 * input events.<br>
 * <br>
 * <b>Note:</b>&nbsp;The methods that this interface defines are called by the
 * {@link com.gmail.br45entei.game.Window Window}'s display thread.
 *
 * @author Brian_Entei
 * @since 1.0 */
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
	public boolean isModal();
	
	/** Called once per frame to allow this callback to receive and use input
	 * data
	 * 
	 * @param deltaTime The delta time of the current frame from the last (will
	 *            be the same value in {@link #update(double)}) */
	public void input(double deltaTime);
	
	/** Called once per frame to allow this callback to update anything it needs
	 * to before the next frame
	 * 
	 * @param deltaTime The delta time of the current frame from the last (will
	 *            be the same value in {@link #input(double)}) */
	public void update(double deltaTime);
	
	/** Called whenever system cursor movement is detected by
	 * {@link Mouse#poll()}.
	 * 
	 * @param oldX The mouse's old x coordinate in canvas-relative coordinates
	 * @param oldY The mouse's old y coordinate in canvas-relative coordinates
	 * @param newX The mouse's new x coordinate in canvas-relative coordinates
	 * @param newY The mouse's new y coordinate in canvas-relative
	 *            coordinates */
	public void onMouseMoved(int oldX, int oldY, int newX, int newY);
	
	/** Called whenever a mouse button is pressed while the system cursor is
	 * over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that was just pressed (1 is left, 2 is
	 *            middle, 3 is right, etc.) */
	public void onMouseButtonDown(int button);
	
	/** Called whenever a mouse button is released while the system cursor is
	 * over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that was just released (1 is left, 2 is
	 *            middle, 3 is right, etc.) */
	public void onMouseButtonUp(int button);
	
	/** Called whenever a mouse button is double-clicked while the system cursor
	 * is over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 * 
	 * @param button The mouse button that was just double-clicked (1 is left, 2
	 *            is middle, 3 is right, etc.) */
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
	
	/** Gives this callback a chance to handle any exceptions that it might
	 * throw.<br>
	 * If the exception is not handled, this callback is removed from the
	 * listeners queue to prevent future unhandled exceptions.
	 * 
	 * @param ex The exception that this callback threw
	 * @return Whether or not this callback has handled the exception */
	public boolean handleException(Throwable ex);
	
}
