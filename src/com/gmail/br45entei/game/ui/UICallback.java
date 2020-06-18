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

import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.input.Mouse;

import java.util.Objects;

/** UICallback is an internal class used to provide input data from the end-user
 * to the {@link Window} so that it may listen to input events (such as key
 * combos etc.).
 * 
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
	public void input(double deltaTime) {
	}
	
	@Override
	public void update(double deltaTime) {
	}
	
	@Override
	public void onMouseScroll(boolean vertical, int count) {
	}
	
	@Override
	public void onMouseMoved(int deltaX, int deltaY, int oldX, int oldY, int newX, int newY) {
	}
	
	@Override
	public void onMouseButtonDown(int button) {
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
	}
	
	@Override
	public void onMouseButtonUp(int button) {
	}
	
	@Override
	public void onMouseDoubleClick(int button) {
	}
	
	@Override
	public void onKeyDown(int key) {
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
	}
	
	@Override
	public void onKeyHeld(int key) {
	}
	
	@Override
	public void onKeyUp(int key) {
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
	public boolean handleException(Throwable ex, String method, Object... params) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < params.length; i++) {
			Object param = params[i];
			sb.append(Objects.toString(param)).append(i + 1 == params.length ? "" : ", ");
		}
		String parameters = sb.toString();
		System.err.print(String.format("The Window's built-in InputCallback threw an exception while executing method %s(%s): ", method, parameters));
		ex.printStackTrace(System.err);
		System.err.flush();
		return true;
	}
	
}
