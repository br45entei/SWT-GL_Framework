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
import com.badlogic.gdx.utils.Array;
import com.gmail.br45entei.game.ui.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.libsdl.SDL_Error;

import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager.InputPreference;

/** This class makes it easier to manage any controllers that are plugged in.
 *
 * @since 1.0
 * @author Brian_Entei */
public class ControllerManager {
	
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
	
	/** Constructs a new ControllerManager, ready for use. */
	public ControllerManager() {
		this.manager = new SDL2ControllerManager(InputPreference.XINPUT);
		for(Controller controller : this.manager.getControllers()) {
			System.out.println(getNameForController(controller));
		}
	}
	
	/** Inspects the current controller configuration and polls the connected
	 * controllers for input data.
	 * 
	 * @return Whether or not polling the controllers was successful */
	public synchronized boolean pollControllers() {
		SDL2ControllerManager manager = this.manager;
		if(manager != null) {
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
				}
			}
			for(Controller current : controllers) {
				boolean contains = false;
				for(Controller existing : this.controllers) {
					if(existing == current) {
						contains = true;
						break;
					}
				}
				if(!contains) {
					connectedControllers.add(current);
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
			
			for(Controller controller : this.controllers) {
				// TODO Add and implement the following methods within InputCallback:
				// onControllerButtonDown(int button)
				// onControllerButtonHeld(int button)
				// onControllerButtonUp(int button)
				// onControllerAxisChanged(int axis, float value)
			}
			return true;
		}
		return false;
	}
	
	/** Returns a copy of the list of all of the controllers currently connected
	 * to the system.<br>
	 * This method is thread-safe.<br>
	 * The internal list is updated whenever {@link #pollControllers()} is
	 * called.
	 * 
	 * @return A list of all of the controllers currently connected to the
	 *         system */
	public List<Controller> getControllers() {
		return new ArrayList<>(this.controllers);
	}
	
	/** Disposes of this ControllerManager's system resources, rendering it
	 * unusable. */
	public void dispose() {
		this.manager.close();
		this.manager = null;
	}
	
	/** Returns whether or not this ControllerManager has been {@link #dispose()
	 * disposed}.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this ControllerManager has been disposed */
	public boolean isDisposed() {
		return this.manager == null;
	}
	
}
