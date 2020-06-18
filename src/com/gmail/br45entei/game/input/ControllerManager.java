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

import java.util.concurrent.ConcurrentLinkedDeque;

import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager.InputPreference;

/** This class makes it easier to manage any controllers that are plugged in.
 *
 * @author Brian_Entei
 * @since 1.0 */
public class ControllerManager {
	
	private volatile SDL2ControllerManager manager;
	private final ConcurrentLinkedDeque<Controller> controllers = new ConcurrentLinkedDeque<>();
	
	public ControllerManager() {
		this.manager = new SDL2ControllerManager(InputPreference.XINPUT);
		for(Controller controller : this.manager.getControllers()) {
			String name = controller.getName();
			name = controller instanceof SDL2Controller ? ((SDL2Controller) controller).getLastKnownJoystickName() : name;
			System.out.println(name);
		}
	}
	
	public boolean pollControllers() {
		SDL2ControllerManager manager = this.manager;
		if(manager != null) {
			// TODO
			return true;
		}
		return false;
	}
	
	public void dispose() {
		this.manager.close();
		this.manager = null;
	}
	
}
