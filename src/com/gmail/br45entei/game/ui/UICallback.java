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
package com.gmail.br45entei.game.ui;

import com.badlogic.gdx.controllers.Controller;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.util.StringUtil;

import java.util.Objects;

/** UICallback is an internal class used to provide input data from the end-user
 * to the {@link Window} so that it may listen to input events (such as key
 * combos etc.).
 * 
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
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
				if(!Mouse.isCaptured() && !Mouse.isModal() && Mouse.shouldIListenToClickEvents()) {
					Renderer activeRenderer = this.window.getActiveRenderer();
					if(activeRenderer != null && activeRenderer instanceof InputCallback) {
						InputCallback listener = (InputCallback) activeRenderer;
						boolean modal;
						try {
							modal = listener.isModal();
						} catch(Throwable ex) {
							if(!Window.handleListenerException(listener, ex, "isModal")) {
								ex.printStackTrace(System.err);
								System.err.flush();
								this.window.unregisterRenderer(activeRenderer);
								this.window.unregisterInputCallback(listener);
							}
							modal = false;
						}
						
						if(modal) {
							return;
						}
					}
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
		if(this.window.isClosed() || !Mouse.shouldIListenToClickEvents()) {
			return;
		}
		if(key == Keys.VK_PAUSE_BREAK) {
			this.window.getGLThread().toggleRenderingPaused();
		}
		if(!Keyboard.isKeyDown(Keys.VK_ALT)) {
			if(key == Keys.VK_ESCAPE) {
				if(Mouse.isCaptured()) {
					Mouse.setCaptured(false);
				} else if(Mouse.getTimeSinceReleased() > 160) {
					if(this.window.isFullscreen()) {
						this.window.setFullscreen(false);
					} else {
						if(this.window.shouldCloseWindowOnEscape()) {
							this.window.close();
						}
					}
				}
				return;
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
	public void onControllerButtonDown(Controller controller, int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerButtonHeld(Controller controller, int button, double deltaTime) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerButtonRepeat(Controller controller, int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerButtonUp(Controller controller, int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerButtonDoubleTapped(Controller controller, int button) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerAxisChanged(Controller controller, int axis, float oldValue, float newValue) {
		if(this.window.isClosed()) {
			return;
		}
		
	}
	
	@Override
	public void onControllerAxisNonZero(Controller controller, int axis, float value, double deltaTime) {
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
		System.err.println(StringUtil.throwableToStr(ex));
		System.err.flush();
		return true;
	}
	
}
