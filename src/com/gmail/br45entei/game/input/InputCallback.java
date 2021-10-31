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
package com.gmail.br45entei.game.input;

import com.badlogic.gdx.controllers.Controller;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.math.MathUtil;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.thread.ThreadType;
import com.gmail.br45entei.thread.UsedBy;
import com.gmail.br45entei.thread.ΔTimer;

import java.io.PrintStream;

import org.libsdl.SDL;

/** Interface used to provide a way for listeners to receive and use data from
 * input events.<br>
 * <br>
 * <b>Note:</b>&nbsp;The methods that this interface defines are called by the
 * {@link com.gmail.br45entei.game.ui.Window Window}'s display thread.
 *
 * @since 1.0
 * @author Brian_Entei */
@UsedBy(value = {ThreadType.UI, ThreadType.CONTROLLER})
public interface InputCallback {
	
	/** @return Whether or not {@link #inputInit()} has been called at least
	 *         once for this input callback
	 * 		
	 * @see ThreadType#UI */
	public boolean isInputInitialized();
	
	/** Called to allow this callback to initialize any resources that it will
	 * be using.<br>
	 * <br>
	 * After this method has been called, {@link #isInputInitialized()} should
	 * return <tt>true</tt> (unless something has failed to initialize of
	 * course).
	 *
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void inputInit();
	
	/** Called when the {@link Window} that this callback is registered with is
	 * about to close in order to allow this callback to clean up any resources
	 * that it has created.<br>
	 * <br>
	 * After this method has been called, {@link #isInputInitialized()} should
	 * return <tt>false</tt>.
	 *
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void inputCleanup();
	
	/** Returns whether or not this callback needs the mouse to be able to move around freely over the {@link Mouse#getCursorCanvas() cursor
	 * canvas}.<br>
	 * <br>
	 * (In other words, if this method returns <tt>true</tt>, the {@link Window} will stop the cursor from being captured when the user
	 * clicks on the cursor canvas.<br>
	 * However, it should be noted that the current back-end implementation for this method only works if this callback is also an instance
	 * of {@link Renderer}, and is currently the window's {@link Window#getActiveRenderer() active renderer}.<br>
	 * Otherwise, other background renderers/callbacks could unintentionally keep the user from being able to capture the cursor in order to
	 * play a game properly [e.g. move a first/third-person camera which requires the cursor to be captured].)<br>
	 * <br>
	 * It is also worth noting that the {@link Mouse#setModal(boolean) Mouse.setModal(true)} function will overwrite this method's effects
	 * (but only when it is set to <tt>true</tt>).
	 *
	 * @return Whether or not this callback needs the mouse to be able to move around freely over the {@link Mouse#getCursorCanvas() cursor
	 *         canvas}
	 *
	 * @see ThreadType#UI
	 * @see Mouse#setModal(boolean) */
	@UsedBy(ThreadType.UI)
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
	 * @param deltaTime The {@link ΔTimer#getΔTime() delta time} of the current
	 *            frame from the last (will be the same value in
	 *            {@link #update(double)})
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void input(double deltaTime);
	
	/** Called once per frame by the {@link Window}'s display thread to allow
	 * this callback to update anything it needs
	 * to before the next frame.<br>
	 * If this InputCallback also implements {@link Renderer}, and it is not the
	 * Window's {@link Window#getActiveRenderer() active renderer}, then this
	 * method is <b>not</b> called.
	 *
	 * @param deltaTime The {@link ΔTimer#getΔTime() delta time} of the current
	 *            frame from the last (will be the same value in
	 *            {@link #input(double)})
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
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
	 *            coordinates
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY);
	
	/** Called whenever a mouse button is pressed while the system cursor is
	 * over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 *
	 * @param button The mouse button that was just pressed (1 is left, 2 is
	 *            middle, 3 is right, etc.)
	 * @see ThreadType#UI
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	@UsedBy(ThreadType.UI)
	public void onMouseButtonDown(int button);
	
	/** Called constantly while a mouse button is being held down when the
	 * system cursor was over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 *
	 * @param button The mouse button that is being held down (1 is left, 2 is
	 *            middle, 3 is right, etc.)
	 * @see ThreadType#UI
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	@UsedBy(ThreadType.UI)
	public void onMouseButtonHeld(int button);
	
	/** Called whenever a mouse button is released while the system cursor is
	 * over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 *
	 * @param button The mouse button that was just released (1 is left, 2 is
	 *            middle, 3 is right, etc.)
	 * @see ThreadType#UI
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	@UsedBy(ThreadType.UI)
	public void onMouseButtonUp(int button);
	
	/** Called whenever a mouse button is double-clicked while the system cursor
	 * is over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 *
	 * @param button The mouse button that was just double-clicked (1 is left, 2
	 *            is middle, 3 is right, etc.)
	 * @see ThreadType#UI
	 * @see Mouse#BUTTON_LEFT
	 * @see Mouse#BUTTON_MIDDLE
	 * @see Mouse#BUTTON_RIGHT */
	@UsedBy(ThreadType.UI)
	public void onMouseDoubleClick(int button);
	
	/** Called whenever the mouse's scrollwheel is scrolled while the system
	 * cursor is over the {@link Mouse#getCursorCanvas() cursor canvas}.
	 *
	 * @param vertical Whether or not the scroll was a vertical (true) or
	 *            horizontal (false) scroll
	 * @param count The number of 'notches' that the scrollwheel was moved
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onMouseScroll(boolean vertical, int count);
	
	/** Called whenever the system cursor is 'captured' within the cursor-canvas
	 * via {@link Mouse#setCaptured(boolean) Mouse.setCaptured(true)} after
	 * previously not having been captured. */
	@UsedBy(ThreadType.UI)
	default public void onMouseCaptured() {
	}
	
	/** Called whenever the system cursor is 'released' from within the
	 * cursor-canvas via {@link Mouse#setCaptured(boolean)
	 * Mouse.setCaptured(false)} after previously having been captured. */
	@UsedBy(ThreadType.UI)
	default public void onMouseReleased() {
	}
	
	/** Called whenever a keyboard key is pressed.
	 *
	 * @param key The {@link Keys Key} that was just pressed down
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onKeyDown(int key);
	
	/** Called repeatedly while a keyboard key is being held down.
	 *
	 * @param key The {@link Keys Key} that is being held down
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onKeyHeld(int key);
	
	/** Called whenever a keyboard key is released.
	 *
	 * @param key The {@link Keys Key} that was just released
	 * @see ThreadType#UI */
	@UsedBy(ThreadType.UI)
	public void onKeyUp(int key);
	
	/** Called whenever a {@link Controller} is connected to the system.
	 *
	 * @param controller The controller that was just connected
	 * @see ThreadType#CONTROLLER */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerConnected(Controller controller);
	
	/** Called whenever a {@link Controller} is removed from the system.
	 *
	 * @param controller The controller that was just removed
	 * @see ThreadType#CONTROLLER */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerDisconnected(Controller controller);
	
	/** Called whenever one of the buttons on a {@link Controller} is
	 * pressed.<br>
	 * See below for a list of buttons.
	 *
	 * @param controller The controller whose button was just pressed
	 * @param button The button that was just pushed
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_BUTTON_A
	 * @see SDL#SDL_CONTROLLER_BUTTON_B
	 * @see SDL#SDL_CONTROLLER_BUTTON_X
	 * @see SDL#SDL_CONTROLLER_BUTTON_Y
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_UP
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_DOWN
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_LEFT
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_RIGHT
	 * @see SDL#SDL_CONTROLLER_BUTTON_BACK
	 * @see SDL#SDL_CONTROLLER_BUTTON_START
	 * @see SDL#SDL_CONTROLLER_BUTTON_GUIDE
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSHOULDER
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSHOULDER */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerButtonDown(Controller controller, int button);
	
	/** Called once each 'tick' while one of the buttons on a {@link Controller}
	 * is being held down.<br>
	 * See below for a list of buttons.
	 *
	 * @param controller The controller whose button is being held down
	 * @param button The button that is being held down
	 * @param deltaTime The {@link ControllerManager#getDeltaTime() deltaTime}
	 *            of the current 'tick' from the last
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_BUTTON_A
	 * @see SDL#SDL_CONTROLLER_BUTTON_B
	 * @see SDL#SDL_CONTROLLER_BUTTON_X
	 * @see SDL#SDL_CONTROLLER_BUTTON_Y
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_UP
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_DOWN
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_LEFT
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_RIGHT
	 * @see SDL#SDL_CONTROLLER_BUTTON_BACK
	 * @see SDL#SDL_CONTROLLER_BUTTON_START
	 * @see SDL#SDL_CONTROLLER_BUTTON_GUIDE
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSHOULDER
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSHOULDER */
	@UsedBy(ThreadType.CONTROLLER)
	default public void onControllerButtonHeld(Controller controller, int button, double deltaTime) {
	}
	
	/** Called once every 40ms while one of the buttons on a {@link Controller}
	 * is being held down.<br>
	 * See below for a list of buttons.
	 *
	 * @param controller The controller whose button is being held down
	 * @param button The button that is being held down
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_BUTTON_A
	 * @see SDL#SDL_CONTROLLER_BUTTON_B
	 * @see SDL#SDL_CONTROLLER_BUTTON_X
	 * @see SDL#SDL_CONTROLLER_BUTTON_Y
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_UP
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_DOWN
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_LEFT
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_RIGHT
	 * @see SDL#SDL_CONTROLLER_BUTTON_BACK
	 * @see SDL#SDL_CONTROLLER_BUTTON_START
	 * @see SDL#SDL_CONTROLLER_BUTTON_GUIDE
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSHOULDER
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSHOULDER */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerButtonRepeat(Controller controller, int button);
	
	/** Called whenever one of the buttons on a {@link Controller} is
	 * released.<br>
	 * See below for a list of buttons.
	 *
	 * @param controller The controller whose button was just released
	 * @param button The button that was just released
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_BUTTON_A
	 * @see SDL#SDL_CONTROLLER_BUTTON_B
	 * @see SDL#SDL_CONTROLLER_BUTTON_X
	 * @see SDL#SDL_CONTROLLER_BUTTON_Y
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_UP
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_DOWN
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_LEFT
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_RIGHT
	 * @see SDL#SDL_CONTROLLER_BUTTON_BACK
	 * @see SDL#SDL_CONTROLLER_BUTTON_START
	 * @see SDL#SDL_CONTROLLER_BUTTON_GUIDE
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSHOULDER
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSHOULDER */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerButtonUp(Controller controller, int button);
	
	/** Called whenever one of the buttons on a {@link Controller} is
	 * pressed twice in quick succession.<br>
	 * See below for a list of buttons.
	 *
	 * @param controller The controller whose button was just double tapped
	 * @param button The button that was just double tapped
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_BUTTON_A
	 * @see SDL#SDL_CONTROLLER_BUTTON_B
	 * @see SDL#SDL_CONTROLLER_BUTTON_X
	 * @see SDL#SDL_CONTROLLER_BUTTON_Y
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_UP
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_DOWN
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_LEFT
	 * @see SDL#SDL_CONTROLLER_BUTTON_DPAD_RIGHT
	 * @see SDL#SDL_CONTROLLER_BUTTON_BACK
	 * @see SDL#SDL_CONTROLLER_BUTTON_START
	 * @see SDL#SDL_CONTROLLER_BUTTON_GUIDE
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSTICK
	 * @see SDL#SDL_CONTROLLER_BUTTON_LEFTSHOULDER
	 * @see SDL#SDL_CONTROLLER_BUTTON_RIGHTSHOULDER */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerButtonDoubleTapped(Controller controller, int button);
	
	/** Called whenever one of the axes of a {@link Controller} is changed.<br>
	 * See below for a list of axes.
	 *
	 * @param controller The controller whose axis's value was just changed
	 * @param axis The axis whose value just changed
	 * @param oldValue The value that the axis held previously
	 * @param newValue The value that the axis was just changed to
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_AXIS_LEFTX
	 * @see SDL#SDL_CONTROLLER_AXIS_LEFTY
	 * @see SDL#SDL_CONTROLLER_AXIS_RIGHTX
	 * @see SDL#SDL_CONTROLLER_AXIS_RIGHTY
	 * @see SDL#SDL_CONTROLLER_AXIS_TRIGGERLEFT
	 * @see SDL#SDL_CONTROLLER_AXIS_TRIGGERRIGHT */
	@UsedBy(ThreadType.CONTROLLER)
	public void onControllerAxisChanged(Controller controller, int axis, float oldValue, float newValue);
	
	/** Called once each 'tick' while one of the axes of a {@link Controller} is
	 * a non-zero value.<br>
	 * See below for a list of axes.
	 *
	 * @param controller The controller whose axis's value is currently non-zero
	 * @param axis The axis whose value is currently non-zero
	 * @param value The axis's current value
	 * @param deltaTime The {@link ControllerManager#getDeltaTime() deltaTime}
	 *            of the current 'tick' from the last
	 * @see ThreadType#CONTROLLER
	 * @see SDL#SDL_CONTROLLER_AXIS_LEFTX
	 * @see SDL#SDL_CONTROLLER_AXIS_LEFTY
	 * @see SDL#SDL_CONTROLLER_AXIS_RIGHTX
	 * @see SDL#SDL_CONTROLLER_AXIS_RIGHTY
	 * @see SDL#SDL_CONTROLLER_AXIS_TRIGGERLEFT
	 * @see SDL#SDL_CONTROLLER_AXIS_TRIGGERRIGHT */
	@UsedBy(ThreadType.CONTROLLER)
	default public void onControllerAxisNonZero(Controller controller, int axis, float value, double deltaTime) {
	}
	
	/** Gives this callback a chance to handle any exceptions that it might
	 * throw.<br>
	 * If the exception is not handled, this callback is removed from the
	 * listeners queue to prevent future unhandled exceptions.
	 *
	 * @param ex The exception that this callback threw
	 * @param method This callback's method that threw the error
	 * @param params The method parameters (if any) that were passed in
	 * @return Whether or not this callback has handled the exception
	 * @see ThreadType#UI
	 * @see ThreadType#CONTROLLER */
	@UsedBy({ThreadType.UI, ThreadType.CONTROLLER})
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
	@UsedBy(value = {ThreadType.UI, ThreadType.CONTROLLER})
	public static class InputLogger implements InputCallback {
		
		private volatile boolean initialized = false, printDeltaTime = false;
		private volatile boolean printMouseScrolls = true;
		private volatile boolean printMouseMotion = false;
		private volatile boolean printMouseButtons = true;
		private volatile boolean printMouseButtonHelds = true;
		private volatile boolean printKeyboardButtons = true;
		private volatile boolean printKeyboardButtonHelds = false;
		private volatile boolean printControllerConnections = true;
		private volatile boolean printControllerButtons = true;
		private volatile boolean printControllerButtonHelds = false;
		private volatile boolean printControllerButtonRepeats = false;
		private volatile boolean printControllerAxisChanges = true;
		private volatile boolean printControllerNonZeroAxes = false;
		private final PrintStream pr;
		
		/** Creates a new InputLogger with the specified {@link PrintStream}.
		 * 
		 * @param pr The PrintStream that input logs will be written to */
		public InputLogger(PrintStream pr) {
			this.pr = pr;
		}
		
		/** @return Whether or not the deltaTime value passed to
		 *         {@link #input(double)} is being printed
		 * @see #input(double)
		 * @see #update(double) */
		public final boolean isDeltaTimeBeingPrinted() {
			return this.printDeltaTime;
		}
		
		/** @param printDeltaTime Whether or not the deltaTime value passed to
		 *            {@link #input(double)} should be printed
		 * @return This InputLogger
		 * @see #input(double)
		 * @see #update(double) */
		public final InputLogger setPrintDeltaTime(boolean printDeltaTime) {
			this.printDeltaTime = printDeltaTime;
			return this;
		}
		
		/** @return Whether or not mouse scroll events are being printed
		 * @see #onMouseScroll(boolean, int) */
		public final boolean areMouseScrollsBeingPrinted() {
			return this.printMouseScrolls;
		}
		
		/** @param printMouseScrolls Whether or not mouse scroll events should
		 *            be printed
		 * @return This InputLogger
		 * @see #onMouseScroll(boolean, int) */
		public final InputLogger setPrintMouseScrolls(boolean printMouseScrolls) {
			this.printMouseScrolls = printMouseScrolls;
			return this;
		}
		
		/** @return Whether or not mouse motion events are being printed
		 * @see #onMouseMoved(int, int, int, int, int, int) */
		public final boolean isMouseMotionBeingPrinted() {
			return this.printMouseMotion;
		}
		
		/** @param printMouseMotion Whether or not mouse motion events should be
		 *            printed
		 * @return This InputLogger
		 * @see #onMouseMoved(int, int, int, int, int, int) */
		public final InputLogger setPrintMouseMotion(boolean printMouseMotion) {
			this.printMouseMotion = printMouseMotion;
			return this;
		}
		
		/** @return the printMouseButtons Whether or not mouse button events are
		 *         being printed
		 * @see #onMouseButtonDown(int)
		 * @see #onMouseButtonUp(int)
		 * @see #onMouseDoubleClick(int) */
		public final boolean areMouseButtonsBeingPrinted() {
			return this.printMouseButtons;
		}
		
		/** @param printMouseButtons Whether or not mouse button events should
		 *            be printed
		 * @return This InputLogger
		 * @see #onMouseButtonDown(int)
		 * @see #onMouseButtonUp(int)
		 * @see #onMouseDoubleClick(int) */
		public final InputLogger setPrintMouseButtons(boolean printMouseButtons) {
			this.printMouseButtons = printMouseButtons;
			return this;
		}
		
		/** @return Whether or not mouse button hold events are being printed
		 * @see #onMouseButtonHeld(int) */
		public final boolean areMouseButtonHeldsBeingPrinted() {
			return this.printMouseButtonHelds;
		}
		
		/** @param printMouseButtonHelds Whether or not mouse button hold events
		 *            should be printed
		 * @return This InputLogger
		 * @see #onMouseButtonHeld(int) */
		public final InputLogger setPrintMouseButtonHelds(boolean printMouseButtonHelds) {
			this.printMouseButtonHelds = printMouseButtonHelds;
			return this;
		}
		
		/** @return Whether or not keyboard button events are being printed
		 * @see #onKeyDown(int)
		 * @see #onKeyUp(int) */
		public final boolean areKeyboardButtonsBeingPrinted() {
			return this.printKeyboardButtons;
		}
		
		/** @param printKeyboardButtons Whether or not keyboard button events
		 *            should be printed
		 * @return This InputLogger
		 * @see #onKeyDown(int)
		 * @see #onKeyUp(int) */
		public final InputLogger setPrintKeyboardButtons(boolean printKeyboardButtons) {
			this.printKeyboardButtons = printKeyboardButtons;
			return this;
		}
		
		/** @return Whether or not keyboard button hold events are being
		 *         printed
		 * @see #onKeyHeld(int) */
		public final boolean areKeyboardButtonHeldsBeingPrinted() {
			return this.printKeyboardButtonHelds;
		}
		
		/** @param printKeyboardButtonHelds Whether or not keyboard button hold
		 *            events should be printed
		 * @return This InputLogger
		 * @see #onKeyHeld(int) */
		public final InputLogger setPrintKeyboardButtonHelds(boolean printKeyboardButtonHelds) {
			this.printKeyboardButtonHelds = printKeyboardButtonHelds;
			return this;
		}
		
		/** @return Whether or not controller (dis)connection events are being
		 *         printed
		 * @see #onControllerConnected(Controller)
		 * @see #onControllerDisconnected(Controller) */
		public final boolean areControllerConnectionsBeingPrinted() {
			return this.printControllerConnections;
		}
		
		/** @param printControllerConnections Whether or not controller
		 *            (dis)connection events should be printed
		 * @return This InputLogger
		 * @see #onControllerConnected(Controller)
		 * @see #onControllerDisconnected(Controller) */
		public final InputLogger setPrintControllerConnections(boolean printControllerConnections) {
			this.printControllerConnections = printControllerConnections;
			return this;
		}
		
		/** @return Whether or not controller button events are being printed
		 * @see #onControllerButtonDown(Controller, int)
		 * @see #onControllerButtonUp(Controller, int)
		 * @see #onControllerButtonDoubleTapped(Controller, int) */
		public final boolean areControllerButtonsBeingPrinted() {
			return this.printControllerButtons;
		}
		
		/** @param printControllerButtons Whether or not controller button
		 *            events should be printed
		 * @return This InputLogger
		 * @see #onControllerButtonDown(Controller, int)
		 * @see #onControllerButtonUp(Controller, int)
		 * @see #onControllerButtonDoubleTapped(Controller, int) */
		public final InputLogger setPrintControllerButtons(boolean printControllerButtons) {
			this.printControllerButtons = printControllerButtons;
			return this;
		}
		
		/** @return Whether or not controller button hold events are being
		 *         printed
		 * @see #onControllerButtonHeld(Controller, int, double) */
		public final boolean areControllerButtonHeldsBeingPrinted() {
			return this.printControllerButtonHelds;
		}
		
		/** @param printControllerButtonHelds Whether or not controller button
		 *            hold events should be printed
		 * @return This InputLogger
		 * @see #onControllerButtonHeld(Controller, int, double) */
		public final InputLogger setPrintControllerButtonHelds(boolean printControllerButtonHelds) {
			this.printControllerButtonHelds = printControllerButtonHelds;
			return this;
		}
		
		/** @return Whether or not controller button repeat events are being
		 *         printed
		 * @see #onControllerButtonRepeat(Controller, int) */
		public final boolean areControllerButtonRepeatsBeingPrinted() {
			return this.printControllerButtonRepeats;
		}
		
		/** @param printControllerButtonRepeats Whether or not controller button
		 *            repeat events should be printed
		 * @return This InputLogger
		 * @see #onControllerButtonRepeat(Controller, int) */
		public final InputLogger setPrintControllerButtonRepeats(boolean printControllerButtonRepeats) {
			this.printControllerButtonRepeats = printControllerButtonRepeats;
			return this;
		}
		
		/** @return Whether or not controller axis change events are being
		 *         printed
		 * @see #onControllerAxisChanged(Controller, int, float, float) */
		public final boolean areControllerAxisChangesBeingPrinted() {
			return this.printControllerAxisChanges;
		}
		
		/** @param printControllerAxisChanges Whether or not controller axis
		 *            change events should be printed
		 * @return This InputLogger
		 * @see #onControllerAxisChanged(Controller, int, float, float) */
		public final InputLogger setPrintControllerAxisChanges(boolean printControllerAxisChanges) {
			this.printControllerAxisChanges = printControllerAxisChanges;
			return this;
		}
		
		/** @return Whether or not controller non-zero axis events are being
		 *         printed
		 * @see #onControllerAxisNonZero(Controller, int, float, double) */
		public final boolean areControllerNonZeroAxesBeingPrinted() {
			return this.printControllerNonZeroAxes;
		}
		
		/** @param printControllerNonZeroAxes Whether or not controller non-zero
		 *            axis events should be printed
		 * @return This InputLogger
		 * @see #onControllerAxisNonZero(Controller, int, float, double) */
		public final InputLogger setPrintControllerNonZeroAxes(boolean printControllerNonZeroAxes) {
			this.printControllerNonZeroAxes = printControllerNonZeroAxes;
			return this;
		}
		
		@Override
		public boolean isInputInitialized() {
			return this.initialized;
		}
		
		@Override
		public void inputInit() {
			// ...
			
			this.initialized = true;
		}
		
		@Override
		public void inputCleanup() {
			// ...
			
			this.initialized = false;
		}
		
		@Override
		public void input(double deltaTime) {
			if(this.printDeltaTime) {
				this.pr.println(String.format("DeltaTime: %s", MathUtil.limitDecimalNoRounding(deltaTime, 8, true)));
			}
		}
		
		@Override
		public void update(double deltaTime) {
		}
		
		@Override
		public void onMouseScroll(boolean vertical, int count) {
			if(this.printMouseScrolls) {
				this.pr.println(String.format("On mouse scrolled %s: %s", vertical ? "vertically" : "horizontally", Integer.toString(count)));
			}
		}
		
		@Override
		public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY) {
			if(this.printMouseMotion) {
				this.pr.println(String.format("On mouse moved: %s, %s (%s, %s --> %s, %s)", Integer.toString(deltaX), Integer.toString(deltaY), Integer.toString(oldX), Integer.toString(oldY), Integer.toString(newX), Integer.toString(newY)));
			}
		}
		
		@Override
		public void onMouseButtonDown(int button) {
			if(this.printMouseButtons) {
				this.pr.println(String.format("On mouse button down: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
		}
		
		@Override
		public void onMouseButtonHeld(int button) {
			if(this.printMouseButtonHelds) {
				this.pr.println(String.format("On mouse button held: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
		}
		
		@Override
		public void onMouseButtonUp(int button) {
			if(this.printMouseButtons) {
				this.pr.println(String.format("On mouse button up: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
		}
		
		@Override
		public void onMouseDoubleClick(int button) {
			if(this.printMouseButtons) {
				this.pr.println(String.format("On mouse button double click: %s", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
		}
		
		@Override
		public void onKeyDown(int key) {
			if(this.printKeyboardButtons) {
				this.pr.println(String.format("On key down: %s", Keys.getNameForKey(key)));
			}
		}
		
		@Override
		public void onKeyHeld(int key) {
			if(this.printKeyboardButtonHelds) {
				this.pr.println(String.format("On key held: %s", Keys.getNameForKey(key)));
			}
		}
		
		@Override
		public void onKeyUp(int key) {
			if(this.printKeyboardButtons) {
				this.pr.println(String.format("On key up:   %s", Keys.getNameForKey(key)));
			}
		}
		
		@Override
		public void onControllerConnected(Controller controller) {
			if(this.printControllerConnections) {
				this.pr.println(String.format("On controller connected: %s", ControllerManager.getNameForController(controller)));
			}
		}
		
		@Override
		public void onControllerDisconnected(Controller controller) {
			if(this.printControllerConnections) {
				this.pr.println(String.format("On controller disconnected: %s", ControllerManager.getNameForController(controller)));
			}
		}
		
		@Override
		public void onControllerButtonDown(Controller controller, int button) {
			if(this.printControllerButtons) {
				this.pr.println(String.format("On controller button down: \"%s\": %s", ControllerManager.getNameForController(controller), ControllerManager.getButtonName(button)));
			}
		}
		
		@Override
		public void onControllerButtonHeld(Controller controller, int button, double deltaTime) {
			if(this.printControllerButtonHelds) {
				this.pr.println(String.format("On controller button held: \"%s\": %s", ControllerManager.getNameForController(controller), ControllerManager.getButtonName(button)));
			}
		}
		
		@Override
		public void onControllerButtonRepeat(Controller controller, int button) {
			if(this.printControllerButtonRepeats) {
				this.pr.println(String.format("On controller button repeated: \"%s\": %s", ControllerManager.getNameForController(controller), ControllerManager.getButtonName(button)));
			}
		}
		
		@Override
		public void onControllerButtonUp(Controller controller, int button) {
			if(this.printControllerButtons) {
				this.pr.println(String.format("On controller button up: \"%s\": %s", ControllerManager.getNameForController(controller), ControllerManager.getButtonName(button)));
			}
		}
		
		@Override
		public void onControllerButtonDoubleTapped(Controller controller, int button) {
			if(this.printControllerButtons) {
				this.pr.println(String.format("On controller button double tapped: \"%s\": %s", ControllerManager.getNameForController(controller), ControllerManager.getButtonName(button)));
			}
		}
		
		@Override
		public void onControllerAxisChanged(Controller controller, int axis, float oldValue, float newValue) {
			if(this.printControllerAxisChanges) {
				this.pr.println(String.format("On controller axis changed: \"%s\": %s: (%s --> %s)", ControllerManager.getNameForController(controller), ControllerManager.getAxisName(axis), Float.toString(oldValue), Float.toString(newValue)));
			}
		}
		
		@Override
		public void onControllerAxisNonZero(Controller controller, int axis, float value, double deltaTime) {
			if(this.printControllerNonZeroAxes) {
				this.pr.println(String.format("On controller axis non-zero: \"%s\": %s: %s", ControllerManager.getNameForController(controller), ControllerManager.getAxisName(axis), Float.toString(value)));
			}
		}
		
		@Override
		public boolean handleException(Throwable ex, String method, Object... params) {
			ex.printStackTrace();
			return true;
		}
		
	}
	
}
