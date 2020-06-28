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
package com.gmail.br45entei.game.ui;

import com.badlogic.gdx.controllers.Controller;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.input.Mouse;

import java.util.Objects;

/** UICallback is an internal class used to provide input data from the end-user
 * to the {@link Window} so that it may listen to input events (such as key
 * combos etc.).
 * 
 * @since 1.0
 * @author Brian_Entei */
public class UICallback implements InputCallback {
	
	private volatile boolean initialized = false;
	private final Window window;
	
	/** Creates a new UICallback for the specified {@link Window}.
	 * 
	 * @param window The Window that will receive input data via this
	 *            UICallback */
	public UICallback(Window window) {
		this.window = window;
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
	public boolean isModal() {
		return !Mouse.isCaptured();
	}
	
	@Override
	public void input(double deltaTime) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void update(double deltaTime) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onMouseScroll(boolean vertical, int count) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onMouseButtonDown(int button) {
		if(this.window.isClosed()) {
			return;
		}
		
		if(button == Mouse.BUTTON_LEFT) {
			if(System.currentTimeMillis() - this.window.lastMenuInteraction > 480L) {
				if(!Mouse.isCaptured() && !Mouse.isModal()) {
					Mouse.setCaptured(true);
				}
			}
		}
	}
	
	@Override
	public void onMouseButtonHeld(int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onMouseButtonUp(int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onMouseDoubleClick(int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onKeyDown(int key) {
		if(this.window.isClosed()) {
			return;
		}
		if(key == Keys.VK_ESCAPE) {
			if(Mouse.isCaptured()) {
				Mouse.setCaptured(false);
			} else if(Mouse.getTimeSinceReleased() > 160) {
				if(this.window.isFullscreen()) {
					this.window.setFullscreen(false);
				} else {
					this.window.close();
					return;
				}
			}
		}
		if(key == Keys.VK_F2) {
			if(Keyboard.isKeyDown(Keys.VK_SHIFT)) {
				if(this.window.glThread.isRecording() || this.window.glThread.isRecordingStartingUp()) {
					this.window.glThread.stopRecording((v) -> Boolean.valueOf(this.window.swtLoop()));
				} else {
					this.window.glThread.startRecording(this.window.getViewport());
				}
			} else {
				this.window.glThread.takeScreenshot();
			}
		}
		if(key == Keys.VK_F11) {
			this.window.toggleFullscreen();
		}
		
		if(key == Keys.VK_V) {
			if(Keyboard.isKeyDown(Keys.VK_SHIFT)) {
				//TODO add dialog for adjusting frequency from the LWJGL_SWT_Demo
			} else {
				this.window.toggleVsyncEnabled();
			}
		}
		
		if(key == Keys.VK_BROWSER_BACK || (key == Keys.VK_LEFT_ARROW && Keyboard.isKeyDown(Keys.VK_ALT))) {
			Renderer activeRenderer = this.window.getActiveRenderer();
			Renderer previousRenderer = this.window.getPreviousRenderer();
			if(previousRenderer != activeRenderer) {
				this.window.setActiveRenderer(previousRenderer);
			}
		}
		if(key == Keys.VK_BROWSER_FORWARD || (key == Keys.VK_RIGHT_ARROW && Keyboard.isKeyDown(Keys.VK_ALT))) {
			Renderer activeRenderer = this.window.getActiveRenderer();
			Renderer nextRenderer = this.window.getNextRenderer();
			if(nextRenderer != activeRenderer) {
				this.window.setActiveRenderer(nextRenderer);
			}
		}
	}
	
	@Override
	public void onKeyHeld(int key) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onKeyUp(int key) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerConnected(Controller controller) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerDisconnected(Controller controller) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public boolean handleException(Throwable ex, String method, Object... params) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < params.length; i++) {
			Object param = params[i];
			sb.append(Objects.toString(param)).append(i + 1 == params.length ? "" : ", ");
		}
		String parameters = sb.toString();
		System.err.println(String.format("The Window's built-in InputCallback threw an exception while executing method %s(%s): ", method, parameters));
		ex.printStackTrace(System.err);
		System.err.flush();
		return true;
	}
	
}
