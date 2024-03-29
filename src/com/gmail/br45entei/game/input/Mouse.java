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
package com.gmail.br45entei.game.input;

import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.util.CodeUtil;
import com.stackoverflow.DeviceConfig;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TypedListener;

/** This class provides a simple way to retrieve input from and manage the
 * mouse and system cursor.
 *
 * @since 1.0
 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
public class Mouse {
	
	/** The left mouse button */
	public static final int BUTTON_LEFT = 1;
	/** The middle mouse button (usually the scrollwheel button, may not be
	 * present) */
	public static final int BUTTON_MIDDLE = 2;
	/** The right mouse button */
	public static final int BUTTON_RIGHT = 3;
	
	private static final Robot robot;
	
	static {
		DeviceConfig config = DeviceConfig.findDeviceConfig(new Rectangle(0, 0, 800, 600));
		GraphicsDevice screenDevice = config.getDevice();
		screenDevice = screenDevice == null ? GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice() : screenDevice;
		
		try {
			robot = new Robot(screenDevice);
		} catch(AWTException | IllegalArgumentException | SecurityException ex) {
			throw new RuntimeException("Failed to create java.awt.Robot for mouse control!", ex);
		}
	}
	
	protected static volatile boolean captured = false;
	protected static volatile boolean modal = false;
	protected static volatile boolean movingCursor = false;
	protected static volatile int captureX, captureY;
	protected static volatile int ΔX, ΔY;
	protected static volatile int lastFreeX, lastFreeY;
	protected static volatile int wheelΔX, wheelΔY;
	protected static volatile int lastWheelΔX, lastWheelΔY;
	protected static volatile Canvas cursorCanvas;
	protected static final org.eclipse.swt.graphics.Rectangle cursorCanvasBounds = new org.eclipse.swt.graphics.Rectangle(0, 0, 800, 600);
	protected static volatile boolean cursorCanvasFocus = false;
	protected static volatile long cursorCanvasFocusGainedTime = 0L;
	protected static volatile long cursorCanvasFocusLostTime = 0L;
	protected static volatile Cursor currentCursor, invisibleCursor;
	
	private static volatile long captureTime = -1L, releaseTime = 0L;
	
	/** @return True if the cursor-canvas has had user-focus for the past 150
	 *         milliseconds or more */
	public static final boolean shouldIListenToClickEvents() {
		return cursorCanvasFocus && System.currentTimeMillis() - cursorCanvasFocusGainedTime >= 150L &&//
				(Mouse.captured ? Mouse.getTimeSinceCaptured() : Mouse.getTimeSinceReleased()) >= 150L;
	}
	
	/** @return The time, in milliseconds, since the cursor-canvas gained
	 *         user-focus, or <tt>-1L</tt> if the canvas does not currently have
	 *         user-focus */
	public static final long getTimeSinceCanvasGainedFocus() {
		return cursorCanvasFocus ? System.currentTimeMillis() - cursorCanvasFocusGainedTime : -1L;
	}
	
	/** @return The time, in milliseconds, since the cursor-canvas lost
	 *         user-focus, or <tt>-1L</tt> if the canvas currently has
	 *         user-focus */
	public static final long getTimeSinceCanvasLostFocus() {
		return cursorCanvasFocus ? -1L : System.currentTimeMillis() - cursorCanvasFocusLostTime;
	}
	
	/** Returns the number of buttons on the mouse. On systems without a mouse,
	 * returns {@code -1}. The number of buttons is obtained from the AWT
	 * Toolkit by requesting the {@code "awt.mouse.numButtons"} desktop property
	 * which is set by the underlying native platform.
	 * 
	 * @return The number of buttons on the mouse */
	public static final int getNumberOfButtons() {
		return MouseInfo.getNumberOfButtons();
	}
	
	protected static final boolean[] mouseDownStates = new boolean[Math.max(3, Mouse.getNumberOfButtons())];
	protected static final boolean[] lastMouseDownStates = new boolean[mouseDownStates.length];
	protected static final boolean[] currentMouseDownStates = new boolean[mouseDownStates.length];
	
	protected static volatile long[] mouseButtonDownTimes = new long[mouseDownStates.length];
	protected static volatile long[] mouseButtonHoldTimes = new long[mouseDownStates.length];
	protected static volatile long[] mouseButtonUpTimes = new long[mouseDownStates.length];
	
	protected static final ConcurrentLinkedDeque<InputCallback> listeners = new ConcurrentLinkedDeque<>();
	
	/** Checks if the given InputCallback is registered with this input source.
	 * 
	 * @param listener The InputCallback to check
	 * @return Whether or not the specified listener is currently registered */
	public static final boolean isInputCallbackRegistered(InputCallback listener) {
		if(listener != null) {
			return listeners.contains(listener);
		}
		return false;
	}
	
	/** Registers the given InputCallback with this input source.<br>
	 * This method will return <tt><b>false</b></tt> if the listener was already
	 * registered.
	 * 
	 * @param listener The InputCallback to register
	 * @return Whether or not the specified listener was just registered */
	public static final boolean registerInputCallback(InputCallback listener) {
		if(listener != null && !isInputCallbackRegistered(listener)) {
			listeners.add(listener);
			return isInputCallbackRegistered(listener);
		}
		return false;
	}
	
	/** Unregisters the given InputCallback from this input source.<br>
	 * This method will return <tt><b>false</b></tt> if the listener was never
	 * registered.
	 * 
	 * @param listener The InputCallback to unregister
	 * @return Whether or not the specified listener was just unregistered */
	public static final boolean unregisterInputCallback(InputCallback listener) {
		if(listener != null && isInputCallbackRegistered(listener)) {
			while(listeners.remove(listener)) {
			}
			return !isInputCallbackRegistered(listener);
		}
		return false;
	}
	
	/** Attempts to have the listener handle the exception it threw, or handles
	 * it and unregisters the listener if it failed to even do that.
	 * 
	 * @param listener The listener that threw an exception
	 * @param ex The exception that was thrown
	 * @param method The listener method that threw the exception
	 * @param params The parameters that were passed to the listener's method
	 * @return Whether or not the listener was able to handle the exception */
	public static final boolean handleListenerException(InputCallback listener, Throwable ex, String method, Object... params) {
		String name = listener.getClass().getName();
		boolean handled = false;
		try {
			handled = listener.handleException(ex, method, params);
		} catch(Throwable ex1) {
			ex1.addSuppressed(ex);
			ex = ex1;
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
			System.err.print(String.format("Listener \"%s\" threw an exception while executing method %s(%s): ", name, method, parameters));
			ex.printStackTrace(System.err);
			System.err.flush();
			unregisterInputCallback(listener);
		}
		return handled;
	}
	
	/** Polls the mouse buttons, and then polls the system cursor for its
	 * location.<br>
	 * After polling, this function calls the functions listed below (when
	 * appropriate) for all of the {@link InputCallback}s registered via
	 * {@link #registerInputCallback(InputCallback)}.
	 * 
	 * @see InputCallback#onMouseButtonDown(int)
	 * @see InputCallback#onMouseButtonHeld(int)
	 * @see InputCallback#onMouseButtonUp(int)
	 * @see InputCallback#onMouseMoved(int, int, int, int, int, int) */
	public static final void poll() {
		//Copy from mouse down states to last down states
		System.arraycopy(mouseDownStates, 0, lastMouseDownStates, 0, lastMouseDownStates.length);
		//'Poll' new data from current down states into mouse down states
		System.arraycopy(currentMouseDownStates, 0, mouseDownStates, 0, mouseDownStates.length);
		
		Canvas canvas = cursorCanvas;
		Point mLoc = getLocation();
		Point topLeft = getCursorCanvasLocation();
		Point center = getCursorCanvasCenter();
		boolean shellActive = false;
		int dx = 0;
		int dy = 0;
		
		if(canvas != null && !canvas.isDisposed() && canvas.getDisplay().getThread() == Thread.currentThread()) {
			shellActive = Window.isShellActive(canvas.getShell());
			if(captured && !modal && !movingCursor) {
				if(shellActive) {
					setLocation(center);
					dx = mLoc.x - center.x;
					dy = mLoc.y - center.y;
					ΔX += dx;
					ΔY += dy;
				}
			} else if(!captured || modal) {
				dx += mLoc.x - lastFreeX;
				dy += mLoc.y - lastFreeY;
				ΔX += dx;
				ΔY += dy;
				lastFreeX = mLoc.x;
				lastFreeY = mLoc.y;
			}
		} else {
			ΔX = ΔY = 0;
		}
		
		if(shellActive) {
			long now = System.currentTimeMillis();
			for(int button = 1; button <= mouseDownStates.length; button++) {
				int i = button - 1;
				Integer b = Integer.valueOf(button);
				if(!lastMouseDownStates[i] && mouseDownStates[i]) {// XXX onMouseButtonDown
					mouseButtonHoldTimes[i] = 0;
					mouseButtonDownTimes[i] = now;
					
					for(InputCallback listener : listeners) {
						try {
							listener.onMouseButtonDown(button);
						} catch(Throwable ex) {
							if(!handleListenerException(listener, ex, "onMouseButtonDown", b)) {
								continue;
							}
						}
					}
				}
				
				if(lastMouseDownStates[i] && mouseDownStates[i] && now - mouseButtonDownTimes[i] >= 460) {// XXX onMouseButtonHeld
					if(mouseButtonHoldTimes[i] == 0 || mouseButtonDownTimes[i] > mouseButtonHoldTimes[i]) {
						mouseButtonHoldTimes[i] = now;
					}
					if(now - mouseButtonHoldTimes[i] >= 40) {
						mouseButtonHoldTimes[i] = now;
						for(InputCallback listener : listeners) {
							try {
								listener.onMouseButtonHeld(button);
							} catch(Throwable ex) {
								if(!handleListenerException(listener, ex, "onMouseButtonHeld", b)) {
									continue;
								}
							}
						}
					}
				}
				
				if(lastMouseDownStates[i] && !mouseDownStates[i]) {// XXX onMouseButtonUp
					mouseButtonHoldTimes[i] = 0;
					boolean doubleClick = false;
					if(mouseButtonUpTimes[i] > 0 && now - mouseButtonUpTimes[i] <= 320L) {
						doubleClick = true;
					}
					mouseButtonUpTimes[i] = now;
					
					for(InputCallback listener : listeners) {
						try {
							listener.onMouseButtonUp(button);
						} catch(Throwable ex) {
							if(!handleListenerException(listener, ex, "onMouseButtonUp", b)) {
								continue;
							}
						}
					}
					
					if(doubleClick) {// XXX onMouseDoubleClick
						mouseButtonUpTimes[i] = 0;
						
						for(InputCallback listener : listeners) {
							try {
								listener.onMouseDoubleClick(button);
							} catch(Throwable ex) {
								if(!handleListenerException(listener, ex, "onMouseDoubleClick", b)) {
									continue;
								}
							}
						}
					}
					
				}
				
			}
			
			int deltaX = dx;
			int deltaY = dy;
			Integer DeltaX = Integer.valueOf(deltaX);
			Integer DeltaY = Integer.valueOf(deltaY);
			
			if(deltaX != 0 || deltaY != 0) {// XXX onMouseMoved
				Point oldLoc = new Point(mLoc);
				oldLoc.x -= (topLeft.x + deltaX);
				oldLoc.y -= (topLeft.y + deltaY);
				Point newLoc = new Point(mLoc);
				newLoc.x -= topLeft.x;
				newLoc.y -= topLeft.y;
				Integer oldX = Integer.valueOf(oldLoc.x), oldY = Integer.valueOf(oldLoc.y);
				Integer newX = Integer.valueOf(newLoc.x), newY = Integer.valueOf(newLoc.y);
				
				for(InputCallback listener : listeners) {
					try {
						listener.onMouseMoved(deltaX, deltaY, oldLoc.x, oldLoc.y, newLoc.x, newLoc.y);
					} catch(Throwable ex) {
						if(!handleListenerException(listener, ex, "onMouseMoved", DeltaX, DeltaY, oldX, oldY, newX, newY)) {
							continue;
						}
					}
				}
			}
			
			int wheelDX = Mouse.wheelΔX - Mouse.lastWheelΔX;
			int wheelDY = Mouse.wheelΔY - Mouse.lastWheelΔY;
			Mouse.lastWheelΔX = Mouse.wheelΔX;
			Mouse.lastWheelΔY = Mouse.wheelΔY;
			Integer WDX = Integer.valueOf(wheelDX);
			Integer WDY = Integer.valueOf(wheelDY);
			
			if(wheelDX != 0 || wheelDY != 0) {// XXX onMouseScroll
				for(InputCallback listener : listeners) {
					if(wheelDX != 0) {
						try {
							listener.onMouseScroll(false, wheelDX);
						} catch(Throwable ex) {
							if(!handleListenerException(listener, ex, "onMouseScroll", Boolean.FALSE, WDX)) {
								continue;
							}
						}
					}
					if(wheelDY != 0) {
						try {
							listener.onMouseScroll(true, wheelDY);
						} catch(Throwable ex) {
							if(!handleListenerException(listener, ex, "onMouseScroll", Boolean.TRUE, WDY)) {
								continue;
							}
						}
					}
				}
			}
		}
	}
	
	private static final void checkIndex(int button) {
		if(button < 1 || button > mouseDownStates.length) {
			throw new IndexOutOfBoundsException(String.format("Button \"%s\" is not between 1 and %s inclusive!", Integer.toString(button), Integer.toString(mouseDownStates.length)));
		}
	}
	
	/** Checks if the specified mouse button is currently held down.
	 * 
	 * @param button The mouse button to check (1 is left, 2 is middle, 3 is
	 *            right, etc.)
	 * @return True if the button is being held down */
	public static final boolean isButtonDown(int button) {
		checkIndex(button);
		return mouseDownStates[button - 1];
	}
	
	/** Checks if the specified mouse button was <em>just</em> pressed down.
	 * 
	 * @param button The mouse button to check (1 is left, 2 is middle, 3 is
	 *            right, etc.)
	 * @return True if the button was just pressed */
	public static final boolean getButtonDown(int button) {
		checkIndex(button);
		return mouseDownStates[button - 1] && !lastMouseDownStates[button - 1];
	}
	
	/** Checks if the specified mouse button was <em>just</em> released.
	 * 
	 * @param button The mouse button to check (1 is left, 2 is middle, 3 is
	 *            right, etc.)
	 * @return True if the button was just released */
	public static final boolean getButtonUp(int button) {
		checkIndex(button);
		return !mouseDownStates[button - 1] && lastMouseDownStates[button - 1];
	}
	
	/** Returns a point containing the Δ (delta) x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.
	 * 
	 * @param reset Whether or not the Δ x and y should be reset to 0
	 *            afterwards
	 * @return The point containing the Δ x and y */
	public static final Point getΔXY(boolean reset) {
		Point dxy = new Point(ΔX, ΔY);
		if(reset) {
			ΔX = ΔY = 0;
		}
		return dxy;
	}
	
	/** Returns a point containing the Δ (delta) x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.<br>
	 * <b>Note:</b>&nbsp;The Δ x and y are reset to 0 after calling this
	 * method.
	 * 
	 * @return The point containing the Δ x and y */
	public static final Point getΔXY() {
		return getΔXY(true);
	}
	
	/** Returns a point containing the Δ (delta) x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.
	 * 
	 * @param reset Whether or not the Δ x and y should be reset to 0
	 *            afterwards
	 * @return The point containing the Δ x and y */
	public static final org.eclipse.swt.graphics.Point getΔXYSWT(boolean reset) {
		org.eclipse.swt.graphics.Point dxy = new org.eclipse.swt.graphics.Point(ΔX, ΔY);
		if(reset) {
			ΔX = ΔY = 0;
		}
		return dxy;
	}
	
	/** Returns a point containing the Δ (delta) x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.<br>
	 * <b>Note:</b>&nbsp;The Δ x and y are reset to 0 after calling this
	 * method.
	 * 
	 * @return The point containing the Δ x and y */
	public static final org.eclipse.swt.graphics.Point getΔXYSWT() {
		return getΔXYSWT(true);
	}
	
	/** Returns a point containing the Δ (delta) x and y of the mouse's
	 * scrollwheel from the last time {@link Mouse#poll()} was called.
	 * 
	 * @param reset Whether or not the Δ x and y should be reset to 0
	 *            afterwards
	 * @return The point containing the scrollwheel's Δ x and y */
	public static final Point getWheelΔXY(boolean reset) {
		Point dxy = new Point(wheelΔX, wheelΔY);
		if(reset) {
			wheelΔX = wheelΔY = 0;
		}
		return dxy;
	}
	
	/** Returns a point containing the Δ (delta) x and y of the mouse's
	 * scrollwheel from the last time {@link Mouse#poll()} was called.<br>
	 * <b>Note:</b>&nbsp;The Δ x and y are reset to 0 after calling this
	 * method.
	 * 
	 * @return The point containing the scrollwheel's Δ x and y */
	public static final Point getWheelΔXY() {
		return getWheelΔXY(true);
	}
	
	/** Returns a point containing the Δ (delta) x and y of the mouse's
	 * scrollwheel
	 * from the last time {@link Mouse#poll()} was called.
	 * 
	 * @param reset Whether or not the Δ x and y should be reset to 0
	 *            afterwards
	 * @return The point containing the scrollwheel's Δ x and y */
	public static final org.eclipse.swt.graphics.Point getWheelΔXYSWT(boolean reset) {
		org.eclipse.swt.graphics.Point Δxy = new org.eclipse.swt.graphics.Point(wheelΔX, wheelΔY);
		if(reset) {
			wheelΔX = wheelΔY = 0;
		}
		return Δxy;
	}
	
	/** Returns a point containing the Δ (delta) x and y of the mouse's
	 * scrollwheel
	 * from the last time {@link Mouse#poll()} was called.<br>
	 * <b>Note:</b>&nbsp;The Δ x and y are reset to 0 after calling this
	 * method.
	 * 
	 * @return The point containing the scrollwheel's Δ x and y */
	public static final org.eclipse.swt.graphics.Point getWheelΔXYSWT() {
		return getWheelΔXYSWT(true);
	}
	
	/** Returns whether or not the system cursor is 'captured' within the cursor
	 * canvas.
	 * 
	 * @return Whether or not the system cursor is 'captured' within the cursor
	 *         canvas */
	public static final boolean isCaptured() {
		return captured;
	}
	
	/** Sets the capture state of the cursor within the cursor canvas.<br>
	 * While the cursor is captured, it is kept in the center of the cursor
	 * canvas, and movement away from the center is recorded as
	 * {@link Mouse#getDXYSWT() deltaX and deltaY}.
	 * 
	 * @param captured Whether or not the cursor should be captured within the
	 *            cursor canvas
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the cursor canvas has not
	 *                been set with {@link #setCursorCanvas(Canvas)} yet</li>
	 *                </ul>
	 */
	public static final void setCaptured(final boolean captured) throws IllegalArgumentException {
		final Canvas canvas = cursorCanvas;
		if(canvas == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
			return;//this just makes eclipse happy
		}
		if(Thread.currentThread() != canvas.getDisplay().getThread()) {
			canvas.getDisplay().asyncExec(() -> {
				setCaptured(captured);
			});
			return;
		}
		canvas.setCursor(captured ? getInvisibleCursor() : canvas.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		currentCursor = canvas.getCursor();
		
		if(captured && modal) {
			modal = false;
		}
		if(captured == Mouse.captured) {
			return;
		}
		long now = System.currentTimeMillis();
		Mouse.captureTime = captured ? now : -1L;
		Mouse.releaseTime = captured ? -1L : now;
		Mouse.captured = captured;
		if(captured) {
			Point mLoc = getLocation();
			captureX = mLoc.x;
			captureY = mLoc.y;
			setLocation(getCursorCanvasCenter());
		} else {
			setLocation(captureX, captureY);
		}
		
		if(captured) {// XXX onMouseCaptured
			for(InputCallback listener : listeners) {
				try {
					listener.onMouseCaptured();
				} catch(Throwable ex) {
					if(!handleListenerException(listener, ex, "onMouseCaptured")) {
						continue;
					}
				}
			}
		} else {// XXX onMouseReleased
			for(InputCallback listener : listeners) {
				try {
					listener.onMouseReleased();
				} catch(Throwable ex) {
					if(!handleListenerException(listener, ex, "onMouseReleased")) {
						continue;
					}
				}
			}
		}
	}
	
	public static final long getCaptureTime() {
		return Mouse.captureTime;
	}
	
	public static final long getReleaseTime() {
		return Mouse.releaseTime;
	}
	
	public static final long getTimeSinceCaptured() {
		return Mouse.captureTime == -1L ? -1L : System.currentTimeMillis() - Mouse.captureTime;
	}
	
	public static final long getTimeSinceReleased() {
		return Mouse.releaseTime == -1L ? -1L : System.currentTimeMillis() - Mouse.releaseTime;
	}
	
	/** @return The location of the system cursor relative to the top left of
	 *         the cursor-canvas */
	public static final Point getLocationRelativeToCanvas() {
		Point mLoc = getLocation();
		return new Point((mLoc.x - cursorCanvasBounds.x) + 1, mLoc.y - cursorCanvasBounds.y);
	}
	
	/** @return The location of the system cursor relative to the bottom left of
	 *         the cursor-canvas */
	public static final Point getLocationRelativeToCanvasOpenGL() {
		Point mLoc = getLocation();
		return new Point((mLoc.x - cursorCanvasBounds.x) + 1, cursorCanvasBounds.height - (mLoc.y - cursorCanvasBounds.y));
	}
	
	/** Returns whether or not the system cursor is currently within the bounds
	 * of the cursor canvas.
	 *
	 * @return Whether or not the system cursor is currently within the bounds
	 *         of the cursor canvas */
	public static final boolean isWithinCanvasBounds() {
		if(captured) {
			return true;
		}
		Point mLoc = getLocationRelativeToCanvas();
		int x = mLoc.x;
		int y = mLoc.y;
		int width = cursorCanvasBounds.width;
		int height = cursorCanvasBounds.height;
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	/** Returns whether or not the system cursor is currently able to move
	 * freely over the cursor canvas.
	 * 
	 * @return Whether or not the system cursor is currently able to move freely
	 *         over the cursor canvas */
	public static final boolean isModal() {
		return modal;
	}
	
	/** Sets whether or not the system cursor will be able to move freely over
	 * the cursor canvas.
	 * 
	 * @param modal Whether or not the system cursor will be able to move freely
	 *            over the cursor canvas */
	public static final void setModal(boolean modal) {
		Mouse.modal = modal;
	}
	
	/** Returns the cursor canvas that is used for capturing the mouse and
	 * making it invisible, detecting button inputs, polling the keyboard, etc.
	 * 
	 * @return The currently set cursor canvas */
	public static final Canvas getCursorCanvas() {
		return cursorCanvas;
	}
	
	/** Sets the canvas that is used for capturing the mouse and making it
	 * invisible, detecting button inputs, polling the keyboard, etc.
	 * 
	 * @param canvas The canvas that the mouse will be able to be captured in.
	 * @return A list of SWT event listeners created to facilitate the capturing
	 *         operation.
	 * 
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_WIDGET_DISPOSED - if the canvas has been
	 *             disposed</li>
	 *             </ul>
	 */
	public static final List<EventListener> setCursorCanvas(Canvas canvas) throws SWTException {
		if(canvas != null && canvas.isDisposed()) {
			throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
		}
		Cursor check = invisibleCursor;
		if(check != null) {
			check.dispose();
			invisibleCursor = null;
		}
		cursorCanvas = canvas;
		if(canvas != null) {
			currentCursor = canvas.getCursor();
			getInvisibleCursor();
			List<EventListener> list = new ArrayList<>();
			MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas) {
						return;
					}
					currentMouseDownStates[e.button - 1] = true;
				}
				
				@Override
				public void mouseUp(MouseEvent e) {
					if(e.widget != cursorCanvas) {
						return;
					}
					currentMouseDownStates[e.button - 1] = false;
				}
			};
			canvas.addMouseListener(mouseListener);
			list.add(mouseListener);
			MouseMoveListener mouseMoveListener = new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas) {
						return;
					}
					// (Moved to Mouse.poll())
					/*if(captured && !modal && !movingCursor) {
						Point center = getCursorCanvasCenter();
						Point mLoc = getLocation();
						setLocation(center);
						deltaX = mLoc.x - center.x;
						deltaY = mLoc.y - center.y;
					}*/
				}
			};
			canvas.addMouseMoveListener(mouseMoveListener);
			list.add(mouseMoveListener);
			
			MouseWheelListener mouseVerticalWheelListener = new MouseWheelListener() {
				@Override
				public void mouseScrolled(MouseEvent e) {
					//lastWheelDY = 0;
					wheelΔY += e.count;//wheelDY = e.count;
				}
			};
			MouseWheelListener mouseHorizontalWheelListener = new MouseWheelListener() {
				@Override
				public void mouseScrolled(MouseEvent e) {
					//lastWheelDX = 0;
					wheelΔX += e.count;//wheelDX = e.count;
				}
			};
			canvas.addListener(SWT.MouseVerticalWheel, new TypedListener(mouseVerticalWheelListener));
			canvas.addListener(SWT.MouseHorizontalWheel, new TypedListener(mouseHorizontalWheelListener));
			list.add(mouseVerticalWheelListener);
			list.add(mouseHorizontalWheelListener);
			
			/*FocusListener focusListener = new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					cursorCanvasFocus = false;
					cursorCanvasFocusLostTime = System.currentTimeMillis();
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					cursorCanvasFocusGainedTime = System.currentTimeMillis();
					cursorCanvasFocus = true;
				}
			};
			canvas.addFocusListener(focusListener);
			list.add(focusListener);*/
			
			Shell shell = canvas.getShell();
			ShellListener shellListener = new ShellListener() {
				
				@Override
				public void shellIconified(ShellEvent e) {
				}
				
				@Override
				public void shellDeiconified(ShellEvent e) {
				}
				
				@Override
				public void shellDeactivated(ShellEvent e) {
					if(shell.getDisplay().getActiveShell() == shell || shell.isFocusControl()) {
						return;
					}
					cursorCanvasFocus = false;
					cursorCanvasFocusLostTime = System.currentTimeMillis();
				}
				
				@Override
				public void shellClosed(ShellEvent e) {
				}
				
				@Override
				public void shellActivated(ShellEvent e) {
					cursorCanvasFocusGainedTime = System.currentTimeMillis();
					cursorCanvasFocus = true;
				}
			};
			shell.addShellListener(shellListener);
			list.add(shellListener);
			
			ControlListener controlListener = new ControlListener() {
				@Override
				public void controlMoved(ControlEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas.getShell()) {
						return;
					}
					org.eclipse.swt.graphics.Point location = cursorCanvas.getDisplay().map(cursorCanvas, null, 0, 0);
					org.eclipse.swt.graphics.Point size = cursorCanvas.getSize();
					cursorCanvasBounds.x = location.x;
					cursorCanvasBounds.y = location.y;
					cursorCanvasBounds.width = size.x;
					cursorCanvasBounds.height = size.y;
					
					if(captured && !modal) {
						setLocation(getCursorCanvasCenter());
					}
				}
				
				@Override
				public void controlResized(ControlEvent e) {
					this.controlMoved(e);
				}
			};
			canvas.addControlListener(controlListener);
			canvas.getShell().addControlListener(controlListener);
			list.add(controlListener);
			
			return list;
		}
		return null;
	}
	
	/** @return The location of the cursor canvas in SWT display-relative
	 *         coordinates
	 * @see com.gmail.br45entei.game.ui.Window#getGLCanvasLocation()
	 *      Window.getGLCanvasLocation() */
	public static final Point getCursorCanvasLocation() {
		return new Point(cursorCanvasBounds.x, cursorCanvasBounds.y);
	}
	
	/** @return The size of the cursor canvas
	 * @see com.gmail.br45entei.game.ui.Window#getSize() Window.getSize() */
	public static final Point getCursorCanvasSize() {
		return new Point(cursorCanvasBounds.width, cursorCanvasBounds.height);
	}
	
	/** @return The bounds of the cursor canvas in SWT display-relative
	 *         coordinates
	 * @see com.gmail.br45entei.game.ui.Window#getViewport() */
	public static final Rectangle getCursorCanvasBounds() {
		return new Rectangle(cursorCanvasBounds.x, cursorCanvasBounds.y, cursorCanvasBounds.width, cursorCanvasBounds.height);
	}
	
	/** @return The center of the cursor canvas in SWT display-relative
	 *         coordinates
	 * @see com.gmail.br45entei.game.ui.Window#getGLCanvasCenter()
	 *      Window.getGLCanvasCenter() */
	public static final Point getCursorCanvasCenter() {
		return new Point(cursorCanvasBounds.x + (cursorCanvasBounds.width / 2), cursorCanvasBounds.y + (cursorCanvasBounds.height / 2));
	}
	
	/** @return An invisible SWT cursor. */
	public static final Cursor getInvisibleCursor() {
		if(invisibleCursor == null) {
			Display display = cursorCanvas.getDisplay();
			//builds a transparent cursor 
			//ImageData cursor = new ImageData(1, 1, 32, new PaletteData(0, 0, 0));
			//invisibleCursor = new Cursor(mouse.control.getDisplay(), cursor, 0, 0);
			Color white = display.getSystemColor(SWT.COLOR_WHITE);
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			PaletteData palette = new PaletteData(new RGB[] {white.getRGB(), black.getRGB()});
			ImageData sourceData = new ImageData(16, 16, 1, palette);
			sourceData.transparentPixel = 0;
			invisibleCursor = new Cursor(display, sourceData, 0, 0);
		}
		return invisibleCursor;
	}
	
	/** Returns the location of the system cursor on screen.
	 * 
	 * @return A point containing the location of the system cursor on screen
	 *         (in screen-relative coordinates) */
	public static final Point getLocation() {
		PointerInfo info = MouseInfo.getPointerInfo();
		if(info == null) {
			if(captured) {
				return getCursorCanvasCenter();
			}
			return new Point(lastFreeX, lastFreeY);
		}
		return info.getLocation();
	}
	
	/** Returns the location of the system cursor on screen.
	 * 
	 * @return A point containing the location of the system cursor on screen
	 *         (in screen-relative coordinates) */
	public static final org.eclipse.swt.graphics.Point getLocationSWT() {
		Point point = getLocation();
		return new org.eclipse.swt.graphics.Point(point.x, point.y);
	}
	
	/** Sets the location of the system cursor on screen (in screen-relative
	 * coordinates).
	 * 
	 * @param x The x coordinate to set
	 * @param y The y coordinate to set */
	public static final void setLocation(int x, int y) {
		synchronized(robot) {
			movingCursor = true;
			try {
				robot.mouseMove(x, y);
			} finally {
				movingCursor = false;
				ΔX = ΔY = 0;
			}
		}
	}
	
	/** Sets the location of the system cursor on screen (in screen-relative
	 * coordinates).
	 * 
	 * @param location The location to set the cursor to */
	public static final void setLocation(Point location) {
		setLocation(location.x, location.y);
	}
	
	/** Sets the location of the system cursor on screen (in screen-relative
	 * coordinates).
	 * 
	 * @param location The location to set the cursor to */
	public static final void setLocation(org.eclipse.swt.graphics.Point location) {
		setLocation(location.x, location.y);
	}
	
	/** Returns the cursor that the system cursor currently appears as when it
	 * is over the {@link #getCursorCanvas() cursor-canvas}.<br>
	 * This method is thread safe; however the returned {@link Cursor} object
	 * should only be used by the display thread which created it.
	 * 
	 * @return The cursor that the system cursor currently appears as when it is
	 *         over the {@link #getCursorCanvas() cursor-canvas} */
	public static final Cursor getCursor() {
		return currentCursor;
	}
	
	/** Sets the cursor that the system cursor will appear as when it is over
	 * the {@link #getCursorCanvas() cursor-canvas}.<br>
	 * This method is thread safe.
	 * 
	 * @param cursor The cursor that the system cursor will appear as when it is
	 *            over the {@link #getCursorCanvas() cursor-canvas} */
	public static final void setCursor(Cursor cursor) {
		final Canvas canvas = cursorCanvas;
		if(canvas == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
			return;//this just makes eclipse happy
		}
		if(Thread.currentThread() != canvas.getDisplay().getThread()) {
			canvas.getDisplay().asyncExec(() -> {
				setCursor(cursor);
			});
			return;
		}
		canvas.setCursor(cursor);
		currentCursor = canvas.getCursor();
	}
	
	/** Loads a cursor image from the specified {@link InputStream input stream}
	 * and sets the system cursor to it.<br>
	 * This method is thread safe; however it will block if it is not called
	 * from the cursor-canvas' display thread.
	 * 
	 * @param in The input stream to read the cursor image data from
	 * @param hotspotX The x coordinate of the cursor's hotspot
	 * @param hotspotY The y coordinate of the cursor's hotspot
	 * @return Whether or not the cursor was successfully loaded and set */
	public static final boolean setCursor(final InputStream in, final int hotspotX, final int hotspotY) {
		final ImageData data;
		try {
			data = new ImageData(in);
		} catch(IllegalArgumentException | SWTException ex) {
			ex.printStackTrace();
			return false;
		} finally {
			try {
				in.close();
			} catch(IOException ignored) {
			}
		}
		
		final Canvas canvas = cursorCanvas;
		if(canvas == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
			return false;//this just makes eclipse happy
		}
		
		final Function<Void, Boolean> setCursor = (v) -> {
			Display display = canvas.getDisplay();
			Cursor cursor = new Cursor(display, data, hotspotX, hotspotY);
			canvas.setCursor(cursor);
			return Boolean.valueOf(cursor.equals(canvas.getCursor()));
		};
		if(Thread.currentThread() != canvas.getDisplay().getThread()) {
			final Boolean[] rtrn = {null};
			canvas.getDisplay().asyncExec(() -> {
				rtrn[0] = setCursor.apply(null);
			});
			Display display;
			while(rtrn[0] == null) {
				display = Display.getCurrent();
				if(display != null && !display.isDisposed()) {
					if(!display.readAndDispatch()) {
						CodeUtil.sleep(10L);
					}
				} else {
					CodeUtil.sleep(10L);
				}
			}
			return rtrn[0].booleanValue();
		}
		
		return setCursor.apply(null).booleanValue();
	}
	
	/** Loads a cursor image from the specified resource path and sets the
	 * system cursor to it.<br>
	 * This method is thread safe; however it will block if it is not called
	 * from the cursor-canvas' display thread.
	 * 
	 * @param clazz The class whose resource loader will attempt to load the
	 *            cursor's image data
	 * @param resourcePath The path to the cursor's image data
	 * @param hotspotX The x coordinate of the cursor's hotspot
	 * @param hotspotY The y coordinate of the cursor's hotspot
	 * @return Whether or not the cursor was successfully loaded and set */
	public static final boolean setCursor(Class<?> clazz, String resourcePath, int hotspotX, int hotspotY) {
		try(InputStream in = clazz.getResourceAsStream(resourcePath)) {
			return setCursor(in, hotspotX, hotspotY);
		} catch(IOException ex) {
			return false;
		}
	}
	
	/** Loads a cursor image from the specified resource path and sets the
	 * system cursor to it.<br>
	 * This method is thread safe; however it will block if it is not called
	 * from the cursor-canvas' display thread.
	 * 
	 * @param resourcePath The path to the cursor's image data
	 * @param hotspotX The x coordinate of the cursor's hotspot
	 * @param hotspotY The y coordinate of the cursor's hotspot
	 * @return Whether or not the cursor was successfully loaded and set */
	public static final boolean setCursor(String resourcePath, int hotspotX, int hotspotY) {
		return setCursor(Mouse.class, resourcePath, hotspotX, hotspotY);
	}
	
}
