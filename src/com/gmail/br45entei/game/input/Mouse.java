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

import com.stackoverflow.DeviceConfig;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TypedListener;

/** This class provides a simple way to retrieve input from and manage the
 * mouse and system cursor.
 *
 * @author Brian_Entei
 * @since 1.0 */
public class Mouse {
	
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
	protected static volatile int captureX, captureY, deltaX, deltaY, lastFreeX,
			lastFreeY;
	protected static volatile Canvas cursorCanvas;
	protected static final org.eclipse.swt.graphics.Rectangle cursorCanvasBounds = new org.eclipse.swt.graphics.Rectangle(0, 0, 800, 600);
	protected static volatile Cursor invisibleCursor;
	
	protected static final boolean[] mouseDownStates = new boolean[Math.max(3, MouseInfo.getNumberOfButtons())];
	protected static final boolean[] lastMouseDownStates = new boolean[mouseDownStates.length];
	protected static final boolean[] currentMouseDownStates = new boolean[mouseDownStates.length];
	
	private static final ConcurrentLinkedDeque<InputCallback> listeners = new ConcurrentLinkedDeque<>();
	
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
	
	/** Polls the mouse buttons, and then polls the system cursor for its
	 * location. */
	public static final void poll() {
		//Copy from mouse down states to last down states
		System.arraycopy(mouseDownStates, 0, lastMouseDownStates, 0, lastMouseDownStates.length);
		//'Poll' new data from current down states into mouse down states
		System.arraycopy(currentMouseDownStates, 0, mouseDownStates, 0, mouseDownStates.length);
		
		Canvas canvas = cursorCanvas;
		if(canvas != null && !canvas.isDisposed() && canvas.getDisplay().getThread() == Thread.currentThread()) {
			Point mLoc = getLocation();
			
			if(captured && !modal && !movingCursor) {
				Point center = getCursorCanvasCenter();
				setLocation(center);
				deltaX = mLoc.x - center.x;
				deltaY = mLoc.y - center.y;
				
				//TODO implement InputCallback.onMouseMoved(...) here!
			} else if(!captured || modal) {
				deltaX = mLoc.x - lastFreeX;
				deltaY = mLoc.y - lastFreeY;
				lastFreeX = mLoc.x;
				lastFreeY = mLoc.y;
				
				//TODO implement InputCallback.onMouseMoved(...) here!
			}
		} else {
			deltaX = deltaY = 0;
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
	
	/** Returns a point containing the delta x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.
	 * 
	 * @param reset Whether or not the delta x and y should be reset to 0
	 *            afterwards
	 * @return The point containing the delta x and y */
	public static final Point getDXY(boolean reset) {
		Point dxy = new Point(deltaX, deltaY);
		if(reset) {
			deltaX = deltaY = 0;
		}
		return dxy;
	}
	
	/** Returns a point containing the delta x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.<br>
	 * <b>Note:</b>&nbsp;The delta x and y are reset to 0 after calling this
	 * method.
	 * 
	 * @return The point containing the delta x and y */
	public static final Point getDXY() {
		return getDXY(true);
	}
	
	/** Returns a point containing the delta x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.
	 * 
	 * @param reset Whether or not the delta x and y should be reset to 0
	 *            afterwards
	 * @return The point containing the delta x and y */
	public static final org.eclipse.swt.graphics.Point getDXYSWT(boolean reset) {
		org.eclipse.swt.graphics.Point dxy = new org.eclipse.swt.graphics.Point(deltaX, deltaY);
		if(reset) {
			deltaX = deltaY = 0;
		}
		return dxy;
	}
	
	/** Returns a point containing the delta x and y of the system cursor
	 * from the last time {@link Mouse#poll()} was called.<br>
	 * <b>Note:</b>&nbsp;The delta x and y are reset to 0 after calling this
	 * method.
	 * 
	 * @return The point containing the delta x and y */
	public static final org.eclipse.swt.graphics.Point getDXYSWT() {
		return getDXYSWT(true);
	}
	
	/** Returns whether or not the system cursor is 'captured' within the cursor
	 * canvas.
	 * 
	 * @return Whether or not the system cursor is 'captured' within the cursor
	 *         canvas */
	public static final boolean isCaptured() {
		return captured;
	}
	
	/** Sets the capture state of the cursor within the cursor canvas.
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
		Canvas canvas = cursorCanvas;
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
		
		if(captured && modal) {
			modal = false;
		}
		Mouse.captured = captured;
		if(captured) {
			Point mLoc = getLocation();
			captureX = mLoc.x;
			captureY = mLoc.y;
			setLocation(getCursorCanvasCenter());
		} else {
			setLocation(captureX, captureY);
		}
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
			getInvisibleCursor();
			List<EventListener> list = new ArrayList<>();
			MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas) {
						return;
					}
					if(e.button == 1) {
						if(!captured && !modal) {
							setCaptured(true);
						}
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
					//TODO implement InputCallback.onMouseScroll(...) here!
				}
			};
			MouseWheelListener mouseHorizontalWheelListener = new MouseWheelListener() {
				@Override
				public void mouseScrolled(MouseEvent e) {
					//TODO implement InputCallback.onMouseScroll(...) here!
				}
			};
			canvas.addListener(SWT.MouseVerticalWheel, new TypedListener(mouseVerticalWheelListener));
			canvas.addListener(SWT.MouseHorizontalWheel, new TypedListener(mouseHorizontalWheelListener));
			list.add(mouseVerticalWheelListener);
			
			ControlListener controlListener = new ControlListener() {
				@Override
				public void controlResized(ControlEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas) {
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
			};
			canvas.addControlListener(controlListener);
			canvas.getShell().addControlListener(controlListener);
			list.add(controlListener);
			
			TraverseListener traverseListener = new TraverseListener() {
				@Override
				public void keyTraversed(TraverseEvent e) {
					if(cursorCanvas == null || (e.widget != cursorCanvas && e.widget != cursorCanvas.getShell())) {
						return;
					}
					
					if(e.keyCode == SWT.ESC) {
						if(captured) {
							setCaptured(false);
						}
					}
				}
			};
			canvas.addTraverseListener(traverseListener);
			canvas.getShell().addTraverseListener(traverseListener);
			list.add(traverseListener);
			
			KeyListener keyListener = new KeyListener() {
				final boolean[] keyStates = new boolean[256];
				final boolean[] lastKeyStates = new boolean[this.keyStates.length];
				
				@Override
				public void keyPressed(KeyEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas) {
						return;
					}
					this.lastKeyStates[e.character] = this.keyStates[e.character];
					this.keyStates[e.character] = false;
					
					if(this.lastKeyStates[e.character] && this.keyStates[e.character]) {
						//TODO implement InputCallback.onKeyHeld(...) here!
					}
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if(cursorCanvas == null || e.widget != cursorCanvas) {
						return;
					}
					this.lastKeyStates[e.character] = this.keyStates[e.character];
					this.keyStates[e.character] = false;
				}
			};
			canvas.addKeyListener(keyListener);
			list.add(keyListener);
			
			return list;
		}
		return null;
	}
	
	/** @return The location of the cursor canvas in SWT display-relative
	 *         coordinates
	 * @see com.gmail.br45entei.game.Window#getGLCanvasLocation()
	 *      Window.getGLCanvasLocation() */
	public static final Point getCursorCanvasLocation() {
		return new Point(cursorCanvasBounds.x, cursorCanvasBounds.y);
	}
	
	/** @return The size of the cursor canvas
	 * @see com.gmail.br45entei.game.Window#getSize() Window.getSize() */
	public static final Point getCursorCanvasSize() {
		return new Point(cursorCanvasBounds.width, cursorCanvasBounds.height);
	}
	
	/** @return The bounds of the cursor canvas in SWT display-relative
	 *         coordinates
	 * @see com.gmail.br45entei.game.Window#getBounds() Window.getSize() */
	public static final Rectangle getCursorCanvasBounds() {
		return new Rectangle(cursorCanvasBounds.x, cursorCanvasBounds.y, cursorCanvasBounds.width, cursorCanvasBounds.height);
	}
	
	/** @return The center of the cursor canvas in SWT display-relative
	 *         coordinates
	 * @see com.gmail.br45entei.game.Window#getGLCanvasCenter()
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
		return MouseInfo.getPointerInfo().getLocation();
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
				deltaX = deltaY = 0;
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
	
}