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

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.utils.Array;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.thread.FrequencyTimer;
import com.gmail.br45entei.util.CodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import org.libsdl.SDL;
import org.libsdl.SDL_Error;

import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager.InputPreference;

/** This class makes it easier to manage any controllers that are plugged in.
 *
 * @since 1.0
 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
public class ControllerManager {
	
	/** The default deadZone for controller axes.<br>
	 * Value is <tt>8000 / 65536</tt>. */
	public static final float DEFAULT_AXIS_DEADZONE = 8000.0f / 65536.0f;
	/** A common deadZone for controller axes.<br>
	 * Value is <tt>5120 / 65536</tt>. */
	public static final float COMMON_AXIS_DEADZONE = 5120.0f / 65536.0f;
	
	private static final int posZerof = Float.floatToIntBits(0.0f);
	private static final int negZerof = Float.floatToIntBits(-0.0f);
	
	/** Returns the specified controller's joystick name, if it has one, or it's
	 * generic name if it doesn't.
	 * 
	 * @param controller The controller whose joystick name will be returned
	 * @return The specified controller's joystick name, if it has one, or it's
	 *         generic name if it doesn't */
	public static final String getNameForController(Controller controller) {
		String name = controller.getName();
		return controller instanceof SDL2Controller ? ((SDL2Controller) controller).getLastKnownJoystickName() : name;
	}
	
	private volatile SDL2ControllerManager manager;
	private final ConcurrentLinkedDeque<Controller> controllers = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<InputCallback> listeners = new ConcurrentLinkedDeque<>();
	
	private final ConcurrentHashMap<String, boolean[][]> controllerButtonStates = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, long[][]> controllerButtonTimeStates = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, float[][]> controllerAxisStates = new ConcurrentHashMap<>();
	
	private volatile Controller controller1;
	private volatile Controller controller2;
	private volatile Controller controller3;
	private volatile Controller controller4;
	
	private volatile boolean allowBackgroundInput = true;
	private volatile boolean windowActive = false;
	
	private volatile long nanoTime = System.nanoTime();
	private volatile long lastFrameTime = this.nanoTime;
	private volatile double deltaTime = (((this.nanoTime = System.nanoTime()) - this.lastFrameTime) + 0.0D) / 1000000000.0D;
	
	private void removeFromMaps(Controller controller) {
		final String key = getNameForController(controller);
		this.controllerButtonStates.remove(key);
		this.controllerButtonTimeStates.remove(key);
		float[][] axisStates = this.controllerAxisStates.get(key);
		if(axisStates != null) {
			float[] lastAxisValues = axisStates[0];
			float[] axisValues = axisStates[1];
			for(int i = 0; i < axisValues.length; i++) {
				axisValues[i] = lastAxisValues[i] = 0.0f;
			}
		}
	}
	
	private void addToMaps(Controller controller) {
		final String key = getNameForController(controller);
		this.controllerButtonStates.put(key, new boolean[2][SDL.SDL_CONTROLLER_BUTTON_MAX]);
		this.controllerButtonTimeStates.put(key, new long[3][SDL.SDL_CONTROLLER_BUTTON_MAX]);
		this.getAxisStatesFor(controller);
		
		if(this.controller1 == null || (!this.isConnected(this.controller1) && key.equals(getNameForController(this.controller1)))) {
			this.controller1 = controller;
		} else if(this.controller2 == null || (!this.isConnected(this.controller2) && key.equals(getNameForController(this.controller2)))) {
			this.controller2 = controller;
		} else if(this.controller3 == null || (!this.isConnected(this.controller3) && key.equals(getNameForController(this.controller3)))) {
			this.controller3 = controller;
		} else if(this.controller4 == null || (!this.isConnected(this.controller4) && key.equals(getNameForController(this.controller4)))) {
			this.controller4 = controller;
		}
	}
	
	/** Constructs a new ControllerManager, ready for use. */
	public ControllerManager() {
		this.manager = new SDL2ControllerManager(InputPreference.XINPUT);
		for(Controller controller : this.manager.getControllers()) {
			System.out.println(getNameForController(controller));
		}
	}
	
	public final boolean isBackgroundInputAllowed() {
		return this.allowBackgroundInput;
	}
	
	public final ControllerManager setAllowBackgroundInput(boolean allowBackgroundInput) {
		this.allowBackgroundInput = allowBackgroundInput;
		return this;
	}
	
	public final boolean isWindowActive() {
		return this.windowActive;
	}
	
	public final ControllerManager setWindowActive(boolean windowActive) {
		this.windowActive = windowActive;
		return this;
	}
	
	private void updateDeltaTime() {
		this.lastFrameTime = this.nanoTime;
		this.nanoTime = System.nanoTime();
		this.deltaTime = ((this.nanoTime - this.lastFrameTime) + 0.0D) / 1000000000.0D;
	}
	
	/** Returns the delta time of the current tick from the last.<br>
	 * For a tickrate of <tt>120.0</tt>, values are typically around
	 * <tt>0.00833340</tt>.
	 * 
	 * @return The delta time of the current frame from the last */
	public final double getDeltaTime() {
		return this.deltaTime;
	}
	
	/** Checks if the given InputCallback is registered with this input source.
	 * 
	 * @param listener The InputCallback to check
	 * @return Whether or not the specified listener is currently registered */
	public final boolean isInputCallbackRegistered(InputCallback listener) {
		if(listener != null) {
			return this.listeners.contains(listener);
		}
		return false;
	}
	
	/** Registers the given InputCallback with this input source.<br>
	 * This method will return <tt><b>false</b></tt> if the listener was already
	 * registered.
	 * 
	 * @param listener The InputCallback to register
	 * @return Whether or not the specified listener was just registered */
	public final boolean registerInputCallback(InputCallback listener) {
		if(listener != null && !this.isInputCallbackRegistered(listener)) {
			this.listeners.add(listener);
			return this.isInputCallbackRegistered(listener);
		}
		return false;
	}
	
	/** Unregisters the given InputCallback from this input source.<br>
	 * This method will return <tt><b>false</b></tt> if the listener was never
	 * registered.
	 * 
	 * @param listener The InputCallback to unregister
	 * @return Whether or not the specified listener was just unregistered */
	public final boolean unregisterInputCallback(InputCallback listener) {
		if(listener != null && this.isInputCallbackRegistered(listener)) {
			while(this.listeners.remove(listener)) {
			}
			return !this.isInputCallbackRegistered(listener);
		}
		return false;
	}
	
	private void pollControllerButtons(Controller controller) {
		if(!this.allowBackgroundInput && !this.windowActive) {
			return;
		}
		
		final String key = getNameForController(controller);
		boolean[][] buttonStates = this.controllerButtonStates.get(key);
		if(buttonStates == null) {
			this.controllerButtonStates.put(key, buttonStates = new boolean[2][SDL.SDL_CONTROLLER_BUTTON_MAX]);
		}
		boolean[] lastButtonDownStates = buttonStates[0];
		boolean[] buttonDownStates = buttonStates[1];
		long[][] buttonTimeStates = this.controllerButtonTimeStates.get(key);
		if(buttonTimeStates == null) {
			this.controllerButtonTimeStates.put(key, buttonTimeStates = new long[3][SDL.SDL_CONTROLLER_BUTTON_MAX]);
		}
		long[] buttonDownTimes = buttonTimeStates[0];
		long[] buttonHoldTimes = buttonTimeStates[1];
		long[] buttonUpTimes = buttonTimeStates[2];
		
		System.arraycopy(buttonDownStates, 0, lastButtonDownStates, 0, lastButtonDownStates.length);
		long now = System.currentTimeMillis();
		Double dt = Double.valueOf(this.deltaTime);
		for(int button = 0; button < SDL.SDL_CONTROLLER_BUTTON_MAX; button++) {
			buttonDownStates[button] = controller.getButton(button);
			Integer b = Integer.valueOf(button);
			
			if(buttonDownStates[button] && !lastButtonDownStates[button]) {// XXX onControllerButtonDown
				buttonHoldTimes[button] = 0L;
				buttonDownTimes[button] = now;
				
				for(InputCallback listener : this.listeners) {
					try {
						listener.onControllerButtonDown(controller, button);
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerButtonDown", controller, b)) {
							this.listeners.remove(listener);
							continue;
						}
					}
				}
			}
			
			if(buttonDownStates[button] && lastButtonDownStates[button]) {// XXX onControllerButtonHeld/Repeat
				for(InputCallback listener : this.listeners) {
					try {
						listener.onControllerButtonHeld(controller, button, dt.doubleValue());
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerButtonHeld", controller, b, dt)) {
							this.listeners.remove(listener);
							continue;
						}
					}
				}
				
				if(now - buttonDownTimes[button] >= 460L) {
					if(buttonHoldTimes[button] == 0L || buttonDownTimes[button] > buttonHoldTimes[button]) {
						buttonHoldTimes[button] = now;
					}
					if(now - buttonHoldTimes[button] >= 40L) {
						buttonHoldTimes[button] = now;
						for(InputCallback listener : this.listeners) {
							try {
								listener.onControllerButtonRepeat(controller, button);
							} catch(Throwable ex) {
								if(!Window.handleListenerException(listener, ex, "onControllerButtonRepeat", controller, b)) {
									this.listeners.remove(listener);
									continue;
								}
							}
						}
					}
				}
			}
			
			if(!buttonDownStates[button] && lastButtonDownStates[button]) {// XXX onControllerButtonUp
				buttonHoldTimes[button] = 0L;
				boolean doubleTap = buttonUpTimes[button] > 0L && now - buttonUpTimes[button] <= 320L;
				buttonUpTimes[button] = now;
				
				for(InputCallback listener : this.listeners) {
					try {
						listener.onControllerButtonUp(controller, button);
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerButtonUp", controller, b)) {
							this.listeners.remove(listener);
							continue;
						}
					}
				}
				
				if(doubleTap) {
					buttonUpTimes[button] = 0L;
					
					for(InputCallback listener : this.listeners) {
						try {
							listener.onControllerButtonDoubleTapped(controller, button);
						} catch(Throwable ex) {
							if(!Window.handleListenerException(listener, ex, "onControllerButtonDoubleTap", controller, b)) {
								this.listeners.remove(listener);
								continue;
							}
						}
					}
				}
			}
		}
	}
	
	private void pollControllerAxes(Controller controller) {
		if(!this.allowBackgroundInput && !this.windowActive) {
			return;
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		float[] axisValues = axisStates[1];// <current controller axis value>
		float[] axisMinValues = axisStates[2];// default value: -1.0f
		float[] axisMaxValues = axisStates[3];// default value: 1.0f
		float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		System.arraycopy(axisValues, 0, lastAxisValues, 0, lastAxisValues.length);// copies the 'current' axis data from previous poll into the lastAxisValues array
		//long now = System.currentTimeMillis();
		double deltaTime = this.deltaTime;
		Double dt = Double.valueOf(deltaTime);
		for(int axis = 0; axis < SDL.SDL_CONTROLLER_AXIS_MAX; axis++) {
			float deadZone = axisDeadZones[axis];
			float value = controller.getAxis(axis);
			
			if(Math.abs(value) <= deadZone) {
				value = 0.0f;//The value falls within the specified dead-zone, so let's ignore it.
			} else {
				value = (value - deadZone) / (1.0f - (deadZone * 2.0f));//Adjust the value so that it doesn't suddenly jump from zero to the area just above/below the deadZone
			}
			
			//Limit the value based on the min/max values:
			if(value < 0) {
				value = Math.max(axisMinValues[axis], value);
			} else if(value > 0) {
				value = Math.min(axisMaxValues[axis], value);
			}
			axisValues[axis] = value;
			
			Float a = Float.valueOf(axis);
			Float newValue = Float.valueOf(axisValues[axis]);
			Float oldValue = Float.valueOf(lastAxisValues[axis]);
			
			if(axisValues[axis] != lastAxisValues[axis]) {// XXX onControllerAxisChanged
				for(InputCallback listener : this.listeners) {
					try {
						listener.onControllerAxisChanged(controller, axis, oldValue.floatValue(), newValue.floatValue());
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerAxisChanged", controller, a, oldValue, newValue)) {
							this.listeners.remove(listener);
							continue;
						}
					}
				}
			}
			
			int check = Float.floatToIntBits(axisValues[axis]);
			if(check != posZerof && check != negZerof) {// XXX onControllerAxisNonZero
				for(InputCallback listener : this.listeners) {
					try {
						listener.onControllerAxisNonZero(controller, axis, newValue.floatValue(), dt.doubleValue());
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerAxisNonZero", controller, a, newValue, dt)) {
							this.listeners.remove(listener);
							continue;
						}
					}
				}
			}
			
		}
	}
	
	/** Attempts to query the current controller configuration from {@link SDL}
	 * and update the states of each connected controller.<br>
	 * This method should not be called from multiple threads at once. Instead,
	 * you should use one thread for polling controllers, and if input data is
	 * needed in other threads, they may obtain it by calling the various
	 * methods available in the {@link Controller} class.
	 * 
	 * @return Whether or not this ControllerManager was successfully able to
	 *         query the current controller configuration from SDL. */
	public final boolean pollState() {
		SDL2ControllerManager manager = this.manager;
		if(manager != null) {
			this.updateDeltaTime();
			List<Controller> disconnectedControllers = new ArrayList<>();
			List<Controller> connectedControllers = new ArrayList<>();
			try {
				manager.pollState();
			} catch(SDL_Error ex) {
				System.err.println("Failed to poll the current controller configuration:");
				ex.printStackTrace(System.err);
				System.err.flush();
				return false;
			}
			Array<Controller> controllers = manager.getControllers();
			for(Controller existing : this.controllers) {
				if(!controllers.contains(existing, true)) {
					disconnectedControllers.add(existing);
					this.removeFromMaps(existing);
				}
			}
			for(Controller current : controllers) {
				getNameForController(current);// Just so the code has been called at least once in the controller object's lifetime (the controller caches the last known joystick name)
				boolean contains = false;
				for(Controller existing : this.controllers) {
					if(existing == current) {
						contains = true;
						break;
					}
				}
				if(!contains) {
					connectedControllers.add(current);
					this.addToMaps(current);
				}
			}
			this.controllers.removeAll(disconnectedControllers);
			for(InputCallback listener : this.listeners) {
				boolean unhandledException = false;
				for(Controller disconnected : disconnectedControllers) {
					try {
						listener.onControllerDisconnected(disconnected);
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerDisconnected", disconnected)) {
							unhandledException = true;
							break;
						}
					}
				}
				if(unhandledException) {
					this.listeners.remove(listener);
					continue;
				}
			}
			this.controllers.addAll(connectedControllers);
			for(InputCallback listener : this.listeners) {
				boolean unhandledException = false;
				for(Controller connected : connectedControllers) {
					try {
						listener.onControllerConnected(connected);
					} catch(Throwable ex) {
						if(!Window.handleListenerException(listener, ex, "onControllerConnected", connected)) {
							unhandledException = true;
							break;
						}
					}
				}
				if(unhandledException) {
					this.listeners.remove(listener);
					continue;
				}
			}
			return true;
		}
		return false;
	}
	
	/** Polls the specified controller by itself.<br>
	 * This method should not be called from multiple threads at once. Instead,
	 * you should use one thread for polling controllers, and if input data is
	 * needed in other threads, they may obtain it by calling the various
	 * methods available in the {@link Controller} class.
	 * 
	 * @param controller The controller to poll
	 * @param pollState Whether or not {@link #pollState()} should be called
	 *            prior to polling the controller
	 * @return Whether or not the controller was polled successfully */
	public final boolean pollController(Controller controller, boolean pollState) {
		if(this.isConnected(controller)) {
			boolean success = true;
			if(pollState) {
				success = this.pollState();
			}
			if(success) {
				this.pollControllerAxes(controller);
				this.pollControllerButtons(controller);
			}
			return success;
		}
		return false;
	}
	
	/** Inspects the current controller configuration and polls the connected
	 * controllers for input data.
	 * 
	 * @return Whether or not polling the controllers was successful */
	public final boolean pollControllers() {
		if(this.pollState()) {
			for(Controller controller : this.controllers) {
				this.pollControllerAxes(controller);
				this.pollControllerButtons(controller);
			}
			return true;
		}
		return false;
	}
	
	/** Creates a new daemon {@link Thread} which calls
	 * {@link #pollControllers()} at a rate specified by the given
	 * {@link FrequencyTimer timer} (if provided, otherwise one is created and
	 * the frequency is set to <tt>120</tt>).<br>
	 * The specified {@link Function returnFalseToStop} function may do as the
	 * name suggests to let the returned thread know it is time to stop running.
	 * If the <tt>returnFalseToStop</tt> function is <tt><b>null</b></tt>, then
	 * the thread will continue polling controllers until the application shuts
	 * down or is terminated.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;Each call to this method will create a new Thread. It
	 * is recommended that you only create one thread at a time, otherwise the
	 * results of using this class are undefined.
	 * 
	 * @param timer The {@link FrequencyTimer} to use for sleeping in between
	 *            calls to {@link #pollControllers()}
	 * @param returnFalseToStop The {@link Function} to use which may let the
	 *            returned thread know when it is time to stop running
	 * @param exceptionHandler The {@link Function} that will be used when the
	 *            <tt>returnFalseToStop</tt> function throws an exception.
	 *            Returning anything other than {@link Boolean#TRUE} (including
	 *            throwing another exception) will cause the returned thread to
	 *            stop running.
	 * @return The newly created Thread (which is already running by the time it
	 *         is returned) */
	public final Thread pollContinuously(FrequencyTimer timer, Function<Boolean, Boolean> returnFalseToStop, Function<Throwable, Boolean> exceptionHandler) {
		timer = timer == null ? new FrequencyTimer(120.0D, 1000.0D) : timer;
		final FrequencyTimer t = timer;
		Thread thread = new Thread(() -> {
			boolean success;
			Boolean result, exceptionHandled;
			while(true) {
				success = this.pollControllers();
				if(returnFalseToStop != null) {
					try {
						result = returnFalseToStop.apply(Boolean.valueOf(success));
						if(result != null && !result.booleanValue()) {
							break;
						}
					} catch(Throwable ex) {
						if(exceptionHandler != null) {
							try {
								exceptionHandled = exceptionHandler.apply(ex);
								if(exceptionHandled == null || !exceptionHandled.booleanValue()) {
									ex.printStackTrace(System.err);
									System.err.flush();
									break;
								}
								//The exception was handled, so let's just continue...
							} catch(Throwable ex1) {
								ex1.addSuppressed(ex);
								ex1.printStackTrace(System.err);
								System.err.flush();
								break;
							}
						} else {
							ex.printStackTrace(System.err);
							System.err.flush();
							break;
						}
					}
				}
				t.frequencySleep();
			}
		}, "ControllerPollThread");
		thread.setDaemon(true);
		thread.start();
		while(thread.getState() == Thread.State.NEW) {
			CodeUtil.sleep(10L);
		}
		return thread;
	}
	
	/** Creates a new daemon {@link Thread} which calls
	 * {@link #pollControllers()} at a rate specified by the given
	 * {@link FrequencyTimer timer} (if provided, otherwise one is created and
	 * the frequency is set to <tt>120</tt>).<br>
	 * The specified {@link Function returnFalseToStop} function may do as the
	 * name suggests to let the returned thread know it is time to stop running.
	 * If the <tt>returnFalseToStop</tt> function is <tt><b>null</b></tt>, then
	 * the thread will continue polling controllers until the application shuts
	 * down or is terminated.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;Each call to this method will create a new Thread. It
	 * is recommended that you only create one thread at a time, otherwise the
	 * results of using this class are undefined.
	 * 
	 * @param timer The {@link FrequencyTimer} to use for sleeping in between
	 *            calls to {@link #pollControllers()}
	 * @param returnFalseToStop The {@link Function} to use which may let the
	 *            returned thread know when it is time to stop running
	 * @return The newly created Thread (which is already running by the time it
	 *         is returned) */
	public final Thread pollContinuously(FrequencyTimer timer, Function<Boolean, Boolean> returnFalseToStop) {
		return this.pollContinuously(timer, returnFalseToStop, null);
	}
	
	/** Creates a new daemon {@link Thread} which calls
	 * {@link #pollControllers()} at a rate specified by the given
	 * {@link FrequencyTimer timer} (if provided, otherwise one is created and
	 * the frequency is set to <tt>120</tt>).<br>
	 * The returned thread will continue polling controllers until the
	 * application shuts down or is terminated.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;Each call to this method will create a new Thread. It
	 * is recommended that you only create one thread at a time, otherwise the
	 * results of using this class are undefined.
	 * 
	 * @param timer The {@link FrequencyTimer} to use for sleeping in between
	 *            calls to {@link #pollControllers()}
	 * @return The newly created Thread (which is already running by the time it
	 *         is returned) */
	public final Thread pollContinuously(FrequencyTimer timer) {
		return this.pollContinuously(timer, null, null);
	}
	
	/** Creates a new daemon {@link Thread} which calls
	 * {@link #pollControllers()} at a rate of <tt>120</tt> ticks per
	 * second.<br>
	 * The returned thread will continue polling controllers until the
	 * application shuts down or is terminated.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;Each call to this method will create a new Thread. It
	 * is recommended that you only create one thread at a time, otherwise the
	 * results of using this class are undefined.
	 * 
	 * @return The newly created Thread (which is already running by the time it
	 *         is returned) */
	public final Thread pollContinuously() {
		return this.pollContinuously(null, null, null);
	}
	
	/** Returns a copy of the list of all of the controllers currently connected
	 * to the system.<br>
	 * This method is thread-safe.<br>
	 * The internal list is updated whenever {@link #pollState()} is
	 * called.
	 * 
	 * @return A list of all of the controllers currently connected to the
	 *         system */
	public final List<Controller> getControllers() {
		return new ArrayList<>(this.controllers);
	}
	
	/** @param controller The controller to check
	 * @return Whether or not the specified controller is currently connected */
	public final boolean isConnected(Controller controller) {
		for(Controller existing : this.controllers) {
			if(existing == controller) {
				return true;
			}
		}
		return false;
	}
	
	/** Searches for and returns the controller whose name matches the specified
	 * name (case sensitive).<br>
	 * If no controller was found, <tt><b>null</b></tt> is returned.
	 * 
	 * @param controllerName The name of the controller to get
	 * @return The controller whose name matches the specified name, or
	 *         <tt><b>null</b></tt> if no controller was found */
	public final Controller getByName(String controllerName) {
		for(Controller existing : this.controllers) {
			if(getNameForController(existing).equals(controllerName)) {
				return existing;
			}
		}
		return null;
	}
	
	/** @return The controller currently set as the first controller */
	public final Controller getController1() {
		if(this.controller1 != null && !this.isConnected(this.controller1)) {
			return null;
		}
		return this.controller1;
	}
	
	/** @param controller The controller to set as the first controller
	 * @return This ControllerManager */
	public final ControllerManager setController1(Controller controller) {
		this.controller1 = controller;
		return this;
	}
	
	/** @return The controller currently set as the second controller */
	public final Controller getController2() {
		if(this.controller2 != null && !this.isConnected(this.controller2)) {
			return null;
		}
		return this.controller2;
	}
	
	/** @param controller The controller to set as the second controller
	 * @return This ControllerManager */
	public final ControllerManager setController2(Controller controller) {
		this.controller2 = controller;
		return this;
	}
	
	/** @return The controller currently set as the third controller */
	public final Controller getController3() {
		if(this.controller3 != null && !this.isConnected(this.controller3)) {
			return null;
		}
		return this.controller3;
	}
	
	/** @param controller The controller to set as the third controller
	 * @return This ControllerManager */
	public final ControllerManager setController3(Controller controller) {
		this.controller3 = controller;
		return this;
	}
	
	/** @return The controller currently set as the fourth controller */
	public final Controller getController4() {
		if(this.controller4 != null && !this.isConnected(this.controller4)) {
			return null;
		}
		return this.controller4;
	}
	
	/** @param controller The controller to set as the fourth controller
	 * @return This ControllerManager */
	public final ControllerManager setController4(Controller controller) {
		this.controller4 = controller;
		return this;
	}
	
	//==================================================================================================================
	
	private final float[][] getAxisStatesFor(Controller controller) {
		final String key = getNameForController(controller);
		float[][] axisStates = this.controllerAxisStates.get(key);
		if(axisStates == null) {
			this.controllerAxisStates.put(key, axisStates = new float[5][SDL.SDL_CONTROLLER_AXIS_MAX]);
			int axisMin = SDL.SDL_JOYSTICK_AXIS_MIN() / 32768;
			int axisMax = SDL.SDL_JOYSTICK_AXIS_MAX() / 32767;
			for(int axis = 0; axis < SDL.SDL_CONTROLLER_AXIS_MAX; axis++) {
				// axisStates[0] and [1] are the 'lastAxisValues' and 'axisValues' respectively
				axisStates[2][axis] = axis == SDL.SDL_CONTROLLER_AXIS_TRIGGERLEFT || axis == SDL.SDL_CONTROLLER_AXIS_TRIGGERRIGHT ? 0.0f : axisMin;// Axis minimum allowed value
				axisStates[3][axis] = axisMax;// Axis maximum allowed value
				axisStates[4][axis] = DEFAULT_AXIS_DEADZONE;// DeadZone
				System.out.println(String.format("Set controller \"%s\"'s %s's default settings to: minAxisValue=%s; maxAxisValue=%s; axisDeadZone=%s", key, getAxisName(axis), Float.toString(axisStates[2][axis]), Float.toString(axisStates[3][axis]), Float.toString(axisStates[4][axis])));
			}
		}
		return axisStates;
	}
	
	/** Returns the given controller's specified
	 * {@link SDL#SDL_CONTROLLER_AXIS_LEFTX
	 * SDL.SDL_CONTROLLER_AXIS}'s deadZone.<br>
	 * This method is thread-safe.
	 * 
	 * @param axis The axis whose deadZone will be returned
	 * @param controller The controller whose axis' deadZone will be returned
	 * @return The controller's axis' deadZone
	 * @throws IllegalArgumentException Thrown if <tt>axis</tt> is less than or
	 *             equal to {@link SDL#SDL_CONTROLLER_AXIS_INVALID} or is
	 *             greater than or equal to
	 *             {@link SDL#SDL_CONTROLLER_AXIS_MAX} */
	public final float getAxisDeadzone(int axis, Controller controller) throws IllegalArgumentException {
		if(axis <= SDL.SDL_CONTROLLER_AXIS_INVALID || axis >= SDL.SDL_CONTROLLER_AXIS_MAX) {
			throw new IllegalArgumentException(String.format("Invalid SDL_CONTROLLER_AXIS: %s", Integer.toString(axis)));
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		//float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		//float[] axisValues = axisStates[1];// <current controller axis value>
		//float[] axisMinValues = axisStates[2];// default value: -1.0f
		//float[] axisMaxValues = axisStates[3];// default value: 1.0f
		float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		return axisDeadZones[axis];
	}
	
	/** Sets the given controller's specified
	 * {@link SDL#SDL_CONTROLLER_AXIS_LEFTX SDL.SDL_CONTROLLER_AXIS}'s
	 * deadZone.<br>
	 * This method is thread-safe.
	 * 
	 * @param axis The axis whose deadZone will be set
	 * @param deadZone The new deadZone for the controller's axis
	 * @param controller The controller whose axis' deadZone will be set
	 * @return Whether or not the controller's axis' deadZone was changed as a
	 *         result
	 * @throws IllegalArgumentException Thrown if <tt>axis</tt> is less than or
	 *             equal to {@link SDL#SDL_CONTROLLER_AXIS_INVALID} or is
	 *             greater than or equal to
	 *             {@link SDL#SDL_CONTROLLER_AXIS_MAX} */
	public final boolean setAxisDeadzone(int axis, float deadZone, Controller controller) throws IllegalArgumentException {
		if(axis <= SDL.SDL_CONTROLLER_AXIS_INVALID || axis >= SDL.SDL_CONTROLLER_AXIS_MAX) {
			throw new IllegalArgumentException(String.format("Invalid SDL_CONTROLLER_AXIS: %s", Integer.toString(axis)));
		}
		if(deadZone != deadZone || Float.isInfinite(deadZone)) {
			return false;
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		//float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		//float[] axisValues = axisStates[1];// <current controller axis value>
		float[] axisMinValues = axisStates[2];// default value: -1.0f
		float[] axisMaxValues = axisStates[3];// default value: 1.0f
		float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		//Limit the deadZone based on the min/max values:
		if(deadZone < 0) {
			deadZone = Math.max(axisMinValues[axis], deadZone);
		} else if(deadZone > 0) {
			deadZone = Math.min(axisMaxValues[axis], deadZone);
		}
		
		float oldDeadZone = axisDeadZones[axis];
		axisDeadZones[axis] = deadZone;
		return axisDeadZones[axis] != oldDeadZone;
	}
	
	public final float getAxisMinimumValue(int axis, Controller controller) {
		if(axis <= SDL.SDL_CONTROLLER_AXIS_INVALID || axis >= SDL.SDL_CONTROLLER_AXIS_MAX) {
			throw new IllegalArgumentException(String.format("Invalid SDL_CONTROLLER_AXIS: %s", Integer.toString(axis)));
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		//float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		//float[] axisValues = axisStates[1];// <current controller axis value>
		float[] axisMinValues = axisStates[2];// default value: -1.0f
		//float[] axisMaxValues = axisStates[3];// default value: 1.0f
		//float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		return axisMinValues[axis];
	}
	
	public final boolean setAxisMinimumValue(int axis, float minValue, Controller controller) {
		if(axis <= SDL.SDL_CONTROLLER_AXIS_INVALID || axis >= SDL.SDL_CONTROLLER_AXIS_MAX) {
			throw new IllegalArgumentException(String.format("Invalid SDL_CONTROLLER_AXIS: %s", Integer.toString(axis)));
		}
		if(minValue != minValue || Float.isInfinite(minValue)) {
			return false;
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		//float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		//float[] axisValues = axisStates[1];// <current controller axis value>
		float[] axisMinValues = axisStates[2];// default value: -1.0f
		//float[] axisMaxValues = axisStates[3];// default value: 1.0f
		//float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		minValue = -1.0f * Math.abs(minValue);
		
		float oldMinValue = axisMinValues[axis];
		axisMinValues[axis] = minValue;
		return axisMinValues[axis] != oldMinValue;
	}
	
	public final float getAxisMaximumValue(int axis, Controller controller) {
		if(axis <= SDL.SDL_CONTROLLER_AXIS_INVALID || axis >= SDL.SDL_CONTROLLER_AXIS_MAX) {
			throw new IllegalArgumentException(String.format("Invalid SDL_CONTROLLER_AXIS: %s", Integer.toString(axis)));
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		//float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		//float[] axisValues = axisStates[1];// <current controller axis value>
		//float[] axisMinValues = axisStates[2];// default value: -1.0f
		float[] axisMaxValues = axisStates[3];// default value: 1.0f
		//float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		return axisMaxValues[axis];
	}
	
	public final boolean setAxisMaximumValue(int axis, float maxValue, Controller controller) {
		if(axis <= SDL.SDL_CONTROLLER_AXIS_INVALID || axis >= SDL.SDL_CONTROLLER_AXIS_MAX) {
			throw new IllegalArgumentException(String.format("Invalid SDL_CONTROLLER_AXIS: %s", Integer.toString(axis)));
		}
		if(maxValue != maxValue || Float.isInfinite(maxValue)) {
			return false;
		}
		
		float[][] axisStates = this.getAxisStatesFor(controller);
		//float[] lastAxisValues = axisStates[0];// <controller axis value as of previous poll>
		//float[] axisValues = axisStates[1];// <current controller axis value>
		//float[] axisMinValues = axisStates[2];// default value: -1.0f
		float[] axisMaxValues = axisStates[3];// default value: 1.0f
		//float[] axisDeadZones = axisStates[4];// default is DEFAULT_AXIS_DEADZONE
		
		maxValue = Math.abs(maxValue);
		
		float oldMaxValue = axisMaxValues[axis];
		axisMaxValues[axis] = maxValue;
		return axisMaxValues[axis] != oldMaxValue;
	}
	
	//==================================================================================================================
	
	/** Returns the specified {@link SDL#SDL_CONTROLLER_BUTTON_A
	 * SDL.SDL_CONTROLLER_BUTTON}'s display name.<br>
	 * This method is thread-safe.
	 * 
	 * @param button The button whose name will be returned
	 * @return The specified button's display name */
	public static String getButtonName(int button) {
		switch(button) {
		case SDL.SDL_CONTROLLER_BUTTON_A:
			return "BUTTON_A";
		case SDL.SDL_CONTROLLER_BUTTON_B:
			return "BUTTON_B";
		case SDL.SDL_CONTROLLER_BUTTON_X:
			return "BUTTON_X";
		case SDL.SDL_CONTROLLER_BUTTON_Y:
			return "BUTTON_Y";
		case SDL.SDL_CONTROLLER_BUTTON_DPAD_UP:
			return "BUTTON_DPAD_UP";
		case SDL.SDL_CONTROLLER_BUTTON_DPAD_DOWN:
			return "BUTTON_DPAD_DOWN";
		case SDL.SDL_CONTROLLER_BUTTON_DPAD_LEFT:
			return "BUTTON_DPAD_LEFT";
		case SDL.SDL_CONTROLLER_BUTTON_DPAD_RIGHT:
			return "BUTTON_DPAD_RIGHT";
		case SDL.SDL_CONTROLLER_BUTTON_BACK:
			return "BUTTON_BACK";
		case SDL.SDL_CONTROLLER_BUTTON_GUIDE:
			return "BUTTON_GUIDE";
		case SDL.SDL_CONTROLLER_BUTTON_START:
			return "BUTTON_START";
		case SDL.SDL_CONTROLLER_BUTTON_LEFTSTICK:
			return "BUTTON_LEFTSTICK";
		case SDL.SDL_CONTROLLER_BUTTON_RIGHTSTICK:
			return "BUTTON_RIGHTSTICK";
		case SDL.SDL_CONTROLLER_BUTTON_LEFTSHOULDER:
			return "BUTTON_LEFTSHOULDER";
		case SDL.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER:
			return "BUTTON_RIGHTSHOULDER";
		case SDL.SDL_CONTROLLER_BUTTON_INVALID:
		default:
			return "BUTTON_INVALID";
		}
	}
	
	/** Returns the specified {@link SDL#SDL_CONTROLLER_AXIS_LEFTX
	 * SDL.SDL_CONTROLLER_AXIS}'s display name.<br>
	 * This method is thread-safe.
	 * 
	 * @param axis The axis whose name will be returned
	 * @return The specified axis's display name */
	public static final String getAxisName(int axis) {
		switch(axis) {
		case SDL.SDL_CONTROLLER_AXIS_LEFTX:
			return "AXIS_LEFTX";
		case SDL.SDL_CONTROLLER_AXIS_LEFTY:
			return "AXIS_LEFTY";
		case SDL.SDL_CONTROLLER_AXIS_RIGHTX:
			return "AXIS_RIGHTX";
		case SDL.SDL_CONTROLLER_AXIS_RIGHTY:
			return "AXIS_RIGHTY";
		case SDL.SDL_CONTROLLER_AXIS_TRIGGERLEFT:
			return "AXIS_TRIGGERLEFT";
		case SDL.SDL_CONTROLLER_AXIS_TRIGGERRIGHT:
			return "AXIS_TRIGGERRIGHT";
		case SDL.SDL_CONTROLLER_AXIS_INVALID:
		default:
			return "AXIS_INVALID";
		}
	}
	
	/** Disposes of this ControllerManager's system resources, rendering it
	 * unusable. */
	public final void dispose() {
		this.manager.close();
		this.manager = null;
	}
	
	/** Returns whether or not this ControllerManager has been {@link #dispose()
	 * disposed}.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this ControllerManager has been disposed */
	public final boolean isDisposed() {
		return this.manager == null;
	}
	
}
