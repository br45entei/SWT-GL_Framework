/*******************************************************************************
 * 
 * Copyright © 2020 Brian_Entei (br45entei@gmail.com)
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

import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.Platform;
import com.gmail.br45entei.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

/** This class provides a simple way to retrieve input from the keyboard.
 *
 * @since 1.0
 * @author Brian_Entei */
public class Keyboard {
	
	protected static volatile boolean[] lastKeyboardButtonStates = new boolean[256];
	protected static volatile boolean[] keyboardButtonStates = new boolean[lastKeyboardButtonStates.length];
	protected static volatile long[] keyboardButtonDownTimes = new long[keyboardButtonStates.length];
	protected static volatile long[] keyboardButtonHoldTimes = new long[keyboardButtonStates.length];
	
	protected static volatile boolean pollKeyboardAsynchronously = false;
	
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
			ex.addSuppressed(ex1);
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
	
	/** Sets the keyboard poll mode.<br>
	 * <b>Note:</b>&nbsp;This feature may only be available on Windows
	 * platforms.
	 * 
	 * @param asynchronous Whether or not the keyboard can be polled even when
	 *            the {@link Mouse#getCursorCanvas() cursor canvas}' window is
	 *            not active */
	public static final void setKeyboardPollMode(boolean asynchronous) {
		pollKeyboardAsynchronously = asynchronous;
	}
	
	private static Class<?> win32OS;
	private static Method win32OSGetKeyState;
	
	/** Polls the keyboard using the {@link #setKeyboardPollMode(boolean) poll
	 * mode}.<br>
	 * <b>Note:</b>&nbsp;This function will block if it is called outside of the
	 * {@link Mouse#getCursorCanvas() cursor canvas}' display thread until the
	 * display thread calls this method back via
	 * {@link Display#asyncExec(Runnable) asyncExec}.
	 * 
	 * @return True if the keyboard was polled successfully */
	public static final boolean poll() {
		Canvas canvas = Mouse.cursorCanvas;
		if(canvas != null && !canvas.isDisposed() && canvas.getDisplay().getThread() == Thread.currentThread()) {
			switch(Platform.get()) {
			case WINDOWS:
				canvas.getDisplay().readAndDispatch();
				if(canvas.isDisposed()) {
					return false;
				}
				if(win32OS == null) {
					win32OS = ReflectionUtil.getClass("org.eclipse.swt.internal.win32.OS");
					if(win32OS == null) {
						System.err.println("Failed to poll Keyboard: Failed to locate required class \"org.eclipse.swt.internal.win32.OS\"!");
						System.err.flush();
						return false;
					}
				}
				if(win32OSGetKeyState == null) {
					win32OSGetKeyState = ReflectionUtil.getMethod(win32OS, "GetKeyState", Integer.TYPE);
					if(win32OSGetKeyState == null) {
						System.err.println("Failed to poll Keyboard: Failed to locate required method \"org.eclipse.swt.internal.win32.OS.GetKeyState(int nVirtKey)\"!");
						System.err.flush();
						return false;
					}
				}
				boolean active = pollKeyboardAsynchronously ? true : (canvas.getDisplay().getActiveShell() == canvas.getShell() && canvas.getShell().isVisible());
				for(int i = 0; i < keyboardButtonStates.length; i++) {
					lastKeyboardButtonStates[i] = keyboardButtonStates[i];
					short keyState;
					try {
						keyState = ((Short) win32OSGetKeyState.invoke(null, Integer.valueOf(i))).shortValue();
					} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						System.err.println("Failed to poll Keyboard: Invoking required method \"org.eclipse.swt.internal.win32.OS.GetKeyState(int nVirtKey)\" threw an exception:");
						e.printStackTrace(System.err);
						System.err.flush();
						return false;
					}
					keyboardButtonStates[i] = active ? keyState/*org.eclipse.swt.internal.win32.OS.GetKeyState(i)*/ < 0 : false;
				}
				break;
			case LINUX:
				// TODO Find a way to dynamically poll the keyboard as is done above for Windows platforms
				break;
			case MACOSX:
				// TODO Find a way to dynamically poll the keyboard as is done above for Windows platforms
				break;
			case UNKNOWN:
			default:
				return false;
			}
			
			long now = System.currentTimeMillis();
			for(int button = 0; button < keyboardButtonStates.length; button++) {
				if(!lastKeyboardButtonStates[button] && keyboardButtonStates[button]) {
					keyboardButtonHoldTimes[button] = 0;
					keyboardButtonDownTimes[button] = now;
					for(InputCallback listener : listeners) {
						try {
							listener.onKeyDown(button);
						} catch(Throwable ex) {
							if(!handleListenerException(listener, ex, "onKeyDown", Keys.getNameForKey(button))) {
								continue;
							}
						}
					}
				}
				if(lastKeyboardButtonStates[button] && keyboardButtonStates[button] && now - keyboardButtonDownTimes[button] >= 460) {
					if(keyboardButtonHoldTimes[button] == 0 || keyboardButtonDownTimes[button] > keyboardButtonHoldTimes[button]) {
						keyboardButtonHoldTimes[button] = now;
					}
					if(now - keyboardButtonHoldTimes[button] >= 40) {
						keyboardButtonHoldTimes[button] = now;
						for(InputCallback listener : listeners) {
							try {
								listener.onKeyHeld(button);
							} catch(Throwable ex) {
								if(!handleListenerException(listener, ex, "onKeyHeld", Keys.getNameForKey(button))) {
									continue;
								}
							}
						}
					}
				}
				if(lastKeyboardButtonStates[button] && !keyboardButtonStates[button]) {
					keyboardButtonHoldTimes[button] = 0;
					for(InputCallback listener : listeners) {
						try {
							listener.onKeyUp(button);
						} catch(Throwable ex) {
							if(!handleListenerException(listener, ex, "onKeyUp", Keys.getNameForKey(button))) {
								continue;
							}
						}
					}
				}
			}
			
			return true;
		}
		if(canvas != null && !canvas.isDisposed() && canvas.getDisplay().getThread() != Thread.currentThread()) {
			final Boolean[] rtrn = {null};
			canvas.getDisplay().asyncExec(() -> {
				rtrn[0] = Boolean.valueOf(Keyboard.poll());
			});
			while(rtrn[0] == null) {
				CodeUtil.sleep(10L);
			}
			return rtrn[0].booleanValue();
		}
		return false;
	}
	
	/** Returns true if the specified key is currently being held down.
	 * 
	 * @param key The {@link Keys key} to check
	 * @return Whether or not the specified key is being held down */
	public static final boolean isKeyDown(int key) {
		if(key < 0 || key >= keyboardButtonStates.length) {
			return false;
		}
		return keyboardButtonStates[key];
	}
	
	/** Returns true if the specified key was <em>just</em> pressed down.
	 * 
	 * @param key The {@link Keys key} to check
	 * @return Whether or not the specified key was just pressed */
	public static final boolean getKeyDown(int key) {
		if(key < 0 || key >= keyboardButtonStates.length) {
			return false;
		}
		return keyboardButtonStates[key] && !lastKeyboardButtonStates[key];
	}
	
	/** Returns true if the specified key was <em>just</em> released.
	 * 
	 * @param key The {@link Keys key} to check
	 * @return Whether or not the specified key was just released */
	public static final boolean getKeyUp(int key) {
		if(key < 0 || key >= keyboardButtonStates.length) {
			return false;
		}
		return !keyboardButtonStates[key] && lastKeyboardButtonStates[key];
	}
	
	/** Class used to store key constants.<br>
	 * A majority of the key names and values in this class were pulled from
	 * this <a href=
	 * "https://docs.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes">Microsoft
	 * website</a>.
	 * 
	 * @author Brian_Entei */
	@SuppressWarnings("javadoc")
	public static final class Keys {
		
		//public static final int VK_ = 0;//							Undefined
		/** The left click button on the mouse.
		 * 
		 * @deprecated May not be available for use on all platforms */
		@Deprecated
		public static final int VK_LEFT_CLICK = 1;
		/** The right click button on the mouse.
		 * 
		 * @deprecated May not be available for use on all platforms */
		@Deprecated
		public static final int VK_RIGHT_CLICK = 2;
		public static final int VK_CANCEL = 3;
		/** The middle click button on the mouse (usually the scrollwheel's
		 * 'click').
		 * 
		 * @deprecated May not be available for use on all platforms */
		@Deprecated
		public static final int VK_MIDDLE_CLICK = 4;
		public static final int VK_XBUTTON1 = 5;
		public static final int VK_XBUTTON2 = 6;
		//public static final int VK_ = 7;//							Undefined
		public static final int VK_BACKSPACE = 8;
		public static final int VK_TAB = 9;
		//public static final int VK_ = 10;//							Undefined, but the ASCII value is '\n'
		//public static final int VK_ = 11;//							Undefined
		//public static final int VK_ = 12;//							Undefined
		public static final int VK_RETURN = 13;//						ASCII value is '\r'
		public static final int VK_VERTICAL_TAB = 14;
		//public static final int VK_ = 15;								Undefined
		public static final int VK_SHIFT = 16;
		/** Either one of the CTRL(control) modifier keys */
		public static final int VK_CONTROL = 17;
		/** Either one of the ALT(alternate) modifier keys */
		public static final int VK_MENU = 18;
		public static final int VK_ALT = VK_MENU;
		public static final int VK_PAUSE_BREAK = 19;
		public static final int VK_CAPITALS_LOCK = 20;
		public static final int VK_KANA = 21;//						IME Kana mode
		public static final int VK_HANGUEL = 21;//					IME Hanguel mode (maintained for compatibility; use VK_HANGUL)
		public static final int VK_HANGUL = 21;//					IME Hangul mode
		//public static final int VK_ = 22;//							Undefined
		public static final int VK_JUNJA = 23;//					IME Junja mode
		public static final int VK_FINAL = 24;//					IME final mode
		public static final int VK_HANJA = 25;//					IME Hanja mode
		//public static final int VK_KANJI = 25;//					IME Kanji mode
		//public static final int VK_ = 26;//							Undefined
		public static final int VK_ESCAPE = 27;
		public static final int VK_CONVERT = 28;//					IME convert
		public static final int VK_NONCONVERT = 29;//				IME nonconvert
		public static final int VK_ACCEPT = 30;//					IME accept
		public static final int VK_MODECHANGE = 31;//				IME mode change request
		public static final int VK_SPACE = 32;
		public static final int VK_PAGE_UP = 33;
		public static final int VK_PAGE_DOWN = 34;
		public static final int VK_END = 35;
		public static final int VK_HOME = 36;
		public static final int VK_LEFT_ARROW = 37;
		public static final int VK_UP_ARROW = 38;
		public static final int VK_RIGHT_ARROW = 39;
		public static final int VK_DOWN_ARROW = 40;
		public static final int VK_SELECT = 41;//					SELECT key
		public static final int VK_PRINT = 42;//					PRINT key
		public static final int VK_EXECUTE = 43;//					EXECUTE key
		public static final int VK_PRINTSCREEN = 44;
		public static final int VK_INSERT = 45;
		public static final int VK_DELETE = 46;
		public static final int VK_HELP = 47;//						HELP key
		public static final int VK_0 = 48;
		public static final int VK_1 = 49;
		public static final int VK_2 = 50;
		public static final int VK_3 = 51;
		public static final int VK_4 = 52;
		public static final int VK_5 = 53;
		public static final int VK_6 = 54;
		public static final int VK_7 = 55;
		public static final int VK_8 = 56;
		public static final int VK_9 = 57;
		//public static final int VK_ = 58;//							Undefined, but the ALT code is: ':'
		//public static final int VK_ = 59;//							Undefined, but the ALT code is: ';'
		//public static final int VK_ = 60;//							Undefined, but the ALT code is: '<'
		//public static final int VK_ = 61;//							Undefined, but the ALT code is: '='
		//public static final int VK_ = 62;//							Undefined, but the ALT code is: '>'
		//public static final int VK_ = 63;//							Undefined, but the ALT code is: '?'
		//public static final int VK_ = 64;//							Undefined, but the ALT code is: '@'
		public static final int VK_A = 65;
		public static final int VK_B = 66;
		public static final int VK_C = 67;
		public static final int VK_D = 68;
		public static final int VK_E = 69;
		public static final int VK_F = 70;
		public static final int VK_G = 71;
		public static final int VK_H = 72;
		public static final int VK_I = 73;
		public static final int VK_J = 74;
		public static final int VK_K = 75;
		public static final int VK_L = 76;
		public static final int VK_M = 77;
		public static final int VK_N = 78;
		public static final int VK_O = 79;
		public static final int VK_P = 80;
		public static final int VK_Q = 81;
		public static final int VK_R = 82;
		public static final int VK_S = 83;
		public static final int VK_T = 84;
		public static final int VK_U = 85;
		public static final int VK_V = 86;
		public static final int VK_W = 87;
		public static final int VK_X = 88;
		public static final int VK_Y = 89;
		public static final int VK_Z = 90;
		public static final int VK_LEFT_LOGO = 91;
		public static final int VK_RIGHT_LOGO = 92;
		public static final int VK_APPS = 93;//						Applications key (Natural keyboard)
		public static final int VK_RESERVED_1 = 94;//				Reserved
		public static final int VK_SLEEP = 95;//					Computer Sleep key
		public static final int VK_NUMPAD0 = 96;
		public static final int VK_NUMPAD1 = 97;
		public static final int VK_NUMPAD2 = 98;
		public static final int VK_NUMPAD3 = 99;
		public static final int VK_NUMPAD4 = 100;
		public static final int VK_NUMPAD5 = 101;
		public static final int VK_NUMPAD6 = 102;
		public static final int VK_NUMPAD7 = 103;
		public static final int VK_NUMPAD8 = 104;
		public static final int VK_NUMPAD9 = 105;
		public static final int VK_MULTIPLY = 106;
		public static final int VK_ADD = 107;
		public static final int VK_SEPARATOR = 108;
		public static final int VK_SUBTRACT = 109;
		public static final int VK_DECIMAL = 110;
		public static final int VK_DIVIDE = 111;
		public static final int VK_F1 = 112;
		public static final int VK_F2 = 113;
		public static final int VK_F3 = 114;
		public static final int VK_F4 = 115;
		public static final int VK_F5 = 116;
		public static final int VK_F6 = 117;
		public static final int VK_F7 = 118;
		public static final int VK_F8 = 119;
		public static final int VK_F9 = 120;
		public static final int VK_F10 = 121;
		public static final int VK_F11 = 122;
		public static final int VK_F12 = 123;
		public static final int VK_F13 = 124;//						Function 13 key
		public static final int VK_F14 = 125;//						Function 14 key
		public static final int VK_F15 = 126;//						Function 15 key
		public static final int VK_F16 = 127;//						Function 16 key
		public static final int VK_F17 = 128;//						Function 17 key
		public static final int VK_F18 = 129;//						Function 18 key
		public static final int VK_F19 = 130;//						Function 19 key
		public static final int VK_F20 = 131;//						Function 20 key
		public static final int VK_F21 = 132;//						Function 21 key
		public static final int VK_F22 = 133;//						Function 22 key
		public static final int VK_F23 = 134;//						Function 23 key
		public static final int VK_F24 = 135;//						Function 24 key
		//public static final int VK_ = 136;//						Undefined
		//public static final int VK_ = 137;//						Undefined
		//public static final int VK_ = 138;//						Undefined
		//public static final int VK_ = 139;//						Undefined
		//public static final int VK_ = 140;//						Undefined
		//public static final int VK_ = 141;//						Undefined
		//public static final int VK_ = 142;//						Undefined
		//public static final int VK_ = 143;//						Undefined
		public static final int VK_NUM_LOCK = 144;
		public static final int VK_SCROLL_LOCK = 145;
		public static final int VK_OEM_SPECIFIC_1 = 146;//			OEM specific
		public static final int VK_OEM_SPECIFIC_2 = 147;//			OEM specific
		public static final int VK_OEM_SPECIFIC_3 = 148;//			OEM specific
		public static final int VK_OEM_SPECIFIC_4 = 149;//			OEM specific
		public static final int VK_OEM_SPECIFIC_5 = 150;//			OEM specific
		//public static final int VK_ = 151;//						Undefined
		//public static final int VK_ = 152;//						Undefined
		//public static final int VK_ = 153;//						Undefined
		//public static final int VK_ = 154;//						Undefined
		//public static final int VK_ = 155;//						Undefined
		//public static final int VK_ = 156;//						Undefined
		//public static final int VK_ = 157;//						Undefined
		//public static final int VK_ = 158;//						Undefined
		//public static final int VK_ = 159;//						Undefined
		public static final int VK_LSHIFT = 160;
		public static final int VK_RSHIFT = 161;
		public static final int VK_LCONTROL = 162;
		public static final int VK_RCONTROL = 163;
		public static final int VK_LMENU = 164;
		public static final int VK_LALT = VK_LMENU;
		public static final int VK_RMENU = 165;
		public static final int VK_RALT = VK_RMENU;
		public static final int VK_BROWSER_BACK = 166;//			Browser Back key
		public static final int VK_BROWSER_FORWARD = 167;//			Browser Forward key
		public static final int VK_BROWSER_REFRESH = 168;//			Browser Refresh key
		public static final int VK_BROWSER_STOP = 169;//			Browser Stop key
		public static final int VK_BROWSER_SEARCH = 170;//			Browser Search key 
		public static final int VK_BROWSER_FAVORITES = 171;//		Browser Favorites key
		public static final int VK_BROWSER_HOME = 172;//			Browser Start and Home key
		public static final int VK_VOLUME_MUTE = 173;//				Volume Mute key
		public static final int VK_VOLUME_DOWN = 174;//				Volume Down key
		public static final int VK_VOLUME_UP = 175;//				Volume Up key
		public static final int VK_MEDIA_NEXT_TRACK = 176;//		Next Track key
		public static final int VK_MEDIA_PREV_TRACK = 177;//		Previous Track key
		public static final int VK_MEDIA_STOP = 178;//				Stop Media key
		public static final int VK_MEDIA_PLAY_PAUSE = 179;//		Play/Pause Media key
		public static final int VK_LAUNCH_MAIL = 180;//				Start Mail key
		public static final int VK_LAUNCH_MEDIA_SELECT = 181;//		Select Media key
		public static final int VK_LAUNCH_APP1 = 182;//				Start Application 1 key
		public static final int VK_LAUNCH_APP2 = 183;//				Start Application 2 key
		public static final int VK_RESERVED_2 = 184;//				Reserved
		public static final int VK_RESERVED_3 = 185;//				Reserved
		public static final int VK_SEMI_COLON = 186;
		public static final int VK_OEM_1 = 186;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the ';:' key
		public static final int VK_EQUALS = 187;
		public static final int VK_OEM_PLUS = 187;//				For any country/region, the '+' key
		public static final int VK_COMMA = 188;
		public static final int VK_OEM_COMMA = 188;//				For any country/region, the ',' key
		public static final int VK_MINUS = 189;
		public static final int VK_OEM_MINUS = 189;//				For any country/region, the '-' key
		public static final int VK_PERIOD = 190;
		public static final int VK_OEM_PERIOD = 190;//				For any country/region, the '.' key
		public static final int VK_FORWARD_SLASH = 191;
		public static final int VK_OEM_2 = 191;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the '/?' key
		public static final int VK_BACK_TICK = 192;
		public static final int VK_OEM_3 = 192;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the '`~' key
		public static final int VK_APP1 = 193;
		public static final int VK_APP2 = 194;
		public static final int VK_APP3 = 195;
		public static final int VK_APP4 = 196;
		public static final int VK_APP5 = 197;
		public static final int VK_APP6 = 198;
		public static final int VK_RESERVED_4 = 199;//				Reserved
		public static final int VK_RESERVED_5 = 200;//				Reserved
		public static final int VK_RESERVED_6 = 201;//				Reserved
		public static final int VK_RESERVED_7 = 202;//				Reserved
		public static final int VK_RESERVED_8 = 203;//				Reserved
		public static final int VK_RESERVED_9 = 204;//				Reserved
		public static final int VK_RESERVED_10 = 205;//				Reserved
		public static final int VK_RESERVED_11 = 206;//				Reserved
		public static final int VK_RESERVED_12 = 207;//				Reserved
		public static final int VK_RESERVED_13 = 208;//				Reserved
		public static final int VK_RESERVED_14 = 209;//				Reserved
		public static final int VK_RESERVED_15 = 210;//				Reserved
		public static final int VK_RESERVED_16 = 211;//				Reserved
		public static final int VK_RESERVED_17 = 212;//				Reserved
		public static final int VK_RESERVED_18 = 213;//				Reserved
		public static final int VK_RESERVED_19 = 214;//				Reserved
		public static final int VK_RESERVED_20 = 215;//				Reserved
		//public static final int VK_ = 216;//						Undefined, but the ALT code is: '╪'
		//public static final int VK_ = 217;//						Undefined, but the ALT code is: '┘'
		//public static final int VK_ = 218;//						Undefined, but the ALT code is: '┌'
		public static final int VK_OPEN_BRACKET = 219;
		public static final int VK_OEM_4 = 219;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the '[{' key
		public static final int VK_BACKSLASH = 220;
		public static final int VK_OEM_5 = 220;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the '\|' key
		public static final int VK_CLOSE_BRACKET = 221;
		public static final int VK_OEM_6 = 221;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the ']}' key
		public static final int VK_APOSTROPHE = 222;
		public static final int VK_OEM_7 = 222;//					Used for miscellaneous characters; it can vary by keyboard. For the US standard keyboard, the 'single-quote/double-quote' key
		public static final int VK_OEM_8 = 223;//					Used for miscellaneous characters; it can vary by keyboard.
		public static final int VK_RESERVED_21 = 224;//				Reserved
		public static final int VK_OEM_SPECIFIC_6 = 225;//			OEM specific
		public static final int VK_OEM_102 = 226;//					Either the angle bracket key or the backslash key on the RT 102-key keyboard
		public static final int VK_OEM_SPECIFIC_7 = 227;//			OEM specific
		public static final int VK_OEM_SPECIFIC_8 = 228;//			OEM specific
		public static final int VK_PROCESSKEY = 229;//				IME PROCESS key
		public static final int VK_OEM_SPECIFIC_9 = 230;//			OEM specific
		public static final int VK_PACKET = 231;//					Used to pass Unicode characters as if they were keystrokes. The VK_PACKET key is the low word of a 32-bit Virtual Key value used for non-keyboard input methods. For more information, see Remark in KEYBDINPUT, SendInput, WM_KEYDOWN, and WM_KEYUP
		//public static final int VK_ = 232;//						Undefined, but the ALT code is: 'Φ'
		public static final int VK_OEM_SPECIFIC_10 = 233;//			OEM specific
		public static final int VK_OEM_SPECIFIC_11 = 234;//			OEM specific
		public static final int VK_OEM_SPECIFIC_12 = 235;//			OEM specific
		public static final int VK_OEM_SPECIFIC_13 = 236;//			OEM specific
		public static final int VK_OEM_SPECIFIC_14 = 237;//			OEM specific
		public static final int VK_OEM_SPECIFIC_15 = 238;//			OEM specific
		public static final int VK_OEM_SPECIFIC_16 = 239;//			OEM specific
		public static final int VK_OEM_SPECIFIC_17 = 240;//			OEM specific
		public static final int VK_OEM_SPECIFIC_18 = 241;//			OEM specific
		public static final int VK_OEM_SPECIFIC_19 = 242;//			OEM specific
		public static final int VK_OEM_SPECIFIC_20 = 243;//			OEM specific
		public static final int VK_OEM_SPECIFIC_21 = 244;//			OEM specific
		public static final int VK_OEM_SPECIFIC_22 = 245;//			OEM specific
		public static final int VK_ATTN = 0xF6;//					Attn key
		public static final int VK_CRSEL = 0xF7;//					CrSel key
		public static final int VK_EXSEL = 0xF8;//					ExSel key
		public static final int VK_EREOF = 0xF9;//					Erase EOF key
		public static final int VK_PLAY = 0xFA;//					Play key
		public static final int VK_ZOOM = 0xFB;//					Zoom key
		public static final int VK_NONAME = 0xFC;//					Reserved
		public static final int VK_RESERVED_22 = 0xFC;//			Reserved
		public static final int VK_PA1 = 0xFD;//					PA1 key
		public static final int VK_OEM_CLEAR = 0xFE;//				Clear key
		//public static final int VK_ = 0xFF;//						Undefined
		
		private static final ConcurrentHashMap<Integer, String> keyDefinitions = new ConcurrentHashMap<>();
		
		static {
			for(Field field : Keys.class.getDeclaredFields()) {
				if(!field.getName().startsWith("VK_") || field.getType() != int.class) {
					continue;
				}
				final boolean wasAccessible = field.canAccess(null);
				field.setAccessible(true);
				try {
					Object obj = field.get(null);
					if(obj instanceof Integer) {
						int value = ((Integer) obj).intValue();
						Integer key = Integer.valueOf(value);
						if(keyDefinitions.get(key) == null) {
							keyDefinitions.put(key, field.getName());
						}
					}
				} catch(IllegalArgumentException | IllegalAccessException e) {
				} finally {
					field.setAccessible(wasAccessible);
				}
			}
			//keyDefinitions.put(Integer.valueOf(0xD2/*Keys.VK_RESERVED_15*/), "VK_DWM_FOCUS");
			/*if(debug) {
				List<Integer> keys = new ArrayList<>(keyDefinitions.keySet());
				Collections.sort(keys);
				for(Integer key : keys) {
					String value = keyDefinitions.get(key);
					System.out.println("\t\tpublic static final int ".concat(value).concat(" = ").concat(key.toString()).concat(";"));
				}
				System.out.println(keys.size());
			}*/
		}
		
		/** Returns the field name whose value matches the specified key.
		 * 
		 * @param key The key whose field name will be returned
		 * @return The key's name */
		public static final String getNameForKey(int key) {
			Integer k = Integer.valueOf(key);
			String name = keyDefinitions.get(k);
			return name == null ? k.toString().concat("(Undefined)") : name;
		}
		
	}
	
}
