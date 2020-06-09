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
package com.gmail.br45entei.game;

import com.badlogic.gdx.controllers.Controller;
import com.gmail.br45entei.game.GLThread.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.lwjgl.natives.LWJGL_Natives;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.SWTUtil;

import java.awt.GraphicsEnvironment;
import java.beans.Beans;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.opengl.swt.GLCanvas;
import org.lwjgl.opengl.swt.GLData;

import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager.InputPreference;

/** The main Window class which manages background tasks such as maintaining the
 * application window.
 *
 * @author Brian_Entei
 * @since 1.0 */
public class Window {
	
	static {
		String[] extraNativesToLoad;
		switch(LWJGL_Natives.Platform.get()) {
		case WINDOWS:
			final String pathRoot;
			switch(LWJGL_Natives.Architecture.get()) {
			case X86:
				pathRoot = "/natives/windows32/";
				extraNativesToLoad = new String[] {pathRoot.concat("libusb-1.0.dll"), pathRoot.concat("sdl2gdx.dll")};
				break;
			case X64:
				pathRoot = "/natives/windows64/";
				extraNativesToLoad = new String[] {pathRoot.concat("libusb-1.0.dll"), pathRoot.concat("sdl2gdx64.dll")};
				break;
			//$CASES-OMITTED$
			default:
				throw new AssertionError("No native libraries for SDL2GDX are available for this platform!");
			}
			break;
		case LINUX:
			extraNativesToLoad = new String[] {"/natives/linux64/libsdl2gdx64.so"};
			break;
		case MACOSX:
			extraNativesToLoad = new String[] {"/natives/macosx64/libsdl2gdx64.dylib"};
			break;
		case UNKNOWN:
		default:
			throw new AssertionError("No native libraries for SDL2GDX are available for this platform!");
		}
		LWJGL_Natives.loadNatives(extraNativesToLoad);
	}
	
	private static volatile Window instance = null;
	
	/** @return The main Window instance */
	public static final Window getWindow() {
		return instance;
	}
	
	/** Launches the application.
	 * 
	 * @param args Program command line arguments */
	public static void main(String[] args) {
		SDL2ControllerManager manager = new SDL2ControllerManager(InputPreference.XINPUT);
		for(Controller controller : manager.getControllers()) {
			String name = controller.getName();
			name = controller instanceof SDL2Controller ? ((SDL2Controller) controller).getLastKnownJoystickName() : name;
			System.out.println(name);
		}
		manager.close();
		
		new Window(800, 600).open();
	}
	
	protected volatile String title;
	protected volatile int x = 0, y = 0, glx = 0, gly = 0, width = 800,
			height = 600;
	protected volatile double framerate = 60.0D;
	
	protected volatile boolean running = false, shellActive = false;
	
	protected Display display;
	protected Shell shell;
	
	protected GLData data;
	protected GLCanvas glCanvas;
	protected GLThread glThread;
	
	public static final int getDefaultRefreshRate() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
	}
	
	private static GLData createDefaultGLData() {
		GLData data = new GLData();
		data.doubleBuffer = true;
		data.swapInterval = Integer.valueOf(1);
		data.majorVersion = 3;
		data.minorVersion = 3;
		return data;
	}
	
	public Window(String title, int width, int height, double framerate, GLData data) {
		this.title = title == null ? "SWT-GL_Framework" : title;
		this.width = width;
		this.height = height;
		this.framerate = framerate != framerate || Double.isInfinite(framerate) ? Window.getDefaultRefreshRate() : framerate;
		this.data = data == null ? Window.createDefaultGLData() : data;
		
		this.createContents();
		
		if(instance == null || instance.shell == null || instance.shell.isDisposed()) {
			instance = this;
		}
	}
	
	public Window(String title, int width, int height, double framerate) {
		this(title, width, height, framerate, Window.createDefaultGLData());
	}
	
	public Window(String title, double framerate) {
		this(title, 800, 600, framerate);
	}
	
	public Window(String title, int width, int height) {
		this(title, width, height, Window.getDefaultRefreshRate());
	}
	
	public Window(int width, int height, double framerate) {
		this(null, width, height, framerate);
	}
	
	public Window(int width, int height) {
		this(width, height, Window.getDefaultRefreshRate());
	}
	
	public Window() {
		this(800, 600);
	}
	
	private void createContents() {
		this.display = Display.getDefault();
		this.shell = new Shell(this.display, SWT.SHELL_TRIM);
		this.shell.setText(this.title);
		this.shell.setImages(SWTUtil.getTitleImages());
		this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				Window.this.running = false;
			}
		});
		this.shell.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle clientArea = Window.this.shell.getClientArea();
				Window.this.glCanvas.setBounds(clientArea);
				Point size = Window.this.glCanvas.getSize();
				Window.this.width = size.x;
				Window.this.height = size.y;
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				Point location = Window.this.shell.getLocation();
				Window.this.x = location.x;
				Window.this.y = location.y;
				
				location = Window.this.display.map(Window.this.glCanvas, null, 0, 0);
				Window.this.glx = location.x;
				Window.this.gly = location.y;
			}
		});
		
		this.glCanvas = new GLCanvas(this.shell, SWT.DOUBLE_BUFFERED, this.data);
		this.glCanvas.setBackground(this.display.getSystemColor(SWT.COLOR_BLACK));
		//this.glCanvas.setRedraw(false);
		this.setSize(this.width, this.height);
		if(Beans.isDesignTime()) {
			this.shell.setSize(this.width, this.height);
			Rectangle clientArea = this.shell.getClientArea();
			Point size = this.shell.getSize();
			int xDiff = size.x - clientArea.width,
					yDiff = size.y - clientArea.height;
			this.shell.setSize(this.width + xDiff, this.height + yDiff);
			this.glCanvas.setBounds(this.shell.getClientArea());
		}
		
		this.glThread = new GLThread(this.glCanvas);
		
	}
	
	/** Maintains the application window, polls the mouse and keyboard, and
	 * performs various other upkeep tasks.
	 * 
	 * @return Whether or not this window should continue running */
	public boolean swtLoop() {
		if(!this.display.readAndDispatch()) {
			CodeUtil.sleep(10L);
		}
		if(!this.shell.isDisposed()) {
			this.shellActive = this.display.getActiveShell() == this.shell && this.shell.isVisible();
			if(this.shell.isDisposed()) {
				return false;
			}
			Mouse.poll();
			if(this.shell.isDisposed()) {
				return false;
			}
			Keyboard.poll();
			if(this.shell.isDisposed()) {
				return false;
			}
			this.glThread.printFPSLog();
			if(this.shell.isDisposed()) {
				return false;
			}
			// TODO implement InputCallback.input(...) and update(...) here!
		}
		return this.running && !this.shell.isDisposed();
	}
	
	public final Thread getWindowThread() {
		return this.display.isDisposed() ? null : this.display.getThread();
	}
	
	public final GLThread getGLThread() {
		return this.glThread;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public Point getSize() {
		return new Point(this.width, this.height);
	}
	
	/** Sets the glCanvas's size to the point specified by the arguments.
	 * <p>
	 * Note: Attempting to set the width or height of the
	 * glCanvas to a negative number will cause that
	 * value to be set to zero instead.
	 * </p>
	 * <p>
	 * Note: On GTK, attempting to set the width or height of the
	 * receiver to a number higher or equal 2^14 will cause them to be
	 * set to (2^14)-1 instead.
	 * </p>
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 * 
	 * @param width the new width in points for the glCanvas
	 * @param height the new height in points for the glCanvas
	 * @return This Window
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Window setSize(int width, int height) throws SWTException {
		if(Thread.currentThread() == this.getWindowThread()) {
			this.shell.setSize(width, height);
			Rectangle clientArea = this.shell.getClientArea();
			Point size = this.shell.getSize();
			int xDiff = size.x - clientArea.width,
					yDiff = size.y - clientArea.height;
			this.shell.setSize(width + xDiff, height + yDiff);
		} else {
			this.display.asyncExec(() -> {
				this.setSize(width, height);
			});
		}
		return this;
	}
	
	/** Sets the glCanvas's size to the point specified by the arguments.
	 * <p>
	 * Note: Attempting to set the width or height of the
	 * glCanvas to a negative number will cause that
	 * value to be set to zero instead.
	 * </p>
	 * <p>
	 * Note: On GTK, attempting to set the width or height of the
	 * receiver to a number higher or equal 2^14 will cause them to be
	 * set to (2^14)-1 instead.
	 * </p>
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 * 
	 * @param size the new size in points for the glCanvas
	 * @return This Window
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Window setSize(Point size) throws SWTException {
		this.setSize(size.x, size.y);
		return this;
	}
	
	public Point getLocation() {
		return new Point(this.x, this.y);
	}
	
	public Point getGLCanvasLocation() {
		return new Point(this.glx, this.gly);
	}
	
	public Point getGLCanvasCenter() {
		Point location = this.getGLCanvasLocation();
		location.x += this.width / 2;
		location.y += this.height / 2;
		return location;
	}
	
	public Rectangle getBounds() {
		return new Rectangle(this.x, this.y, this.width, this.height);
	}
	
	public boolean isActive() {
		return this.shellActive;
	}
	
	/** Opens this window.<br>
	 * This method blocks until the window has been closed. */
	public void open() {
		this.running = true;
		Mouse.setCursorCanvas(this.glCanvas);
		
		this.shell.open();
		this.shell.layout();
		this.shell.forceActive();
		this.glCanvas.forceFocus();
		
		try {
			this.glThread.start();
			while(this.swtLoop() && this.glThread.getState() == Thread.State.NEW) {
			}
			
			while(this.swtLoop()) {
				/*if(Mouse.getButtonDown(1)) {
					System.out.println("Left click!");
				}
				if(Mouse.getButtonDown(2)) {
					System.out.println("Middle click!");
				}
				if(Mouse.getButtonDown(3)) {
					System.out.println("Right click!");
				}
				if(Keyboard.getKeyDown(Keys.VK_W)) {
					System.out.println("W was just pressed!");
				}
				if(Keyboard.isKeyDown(Keys.VK_W)) {
					System.out.println("W is being held down!");
				}
				if(Keyboard.getKeyUp(Keys.VK_W)) {
					System.out.println("W was just released!");
				}
				
				Point dxy = Mouse.getDXYSWT();
				if(dxy.x != 0 || dxy.y != 0) {
					Point loc = this.shell.getLocation();
					loc.x += dxy.x;
					loc.y += dxy.y;
					this.shell.setLocation(loc);
				}*/
			}
			
			this.glThread.stopRunning(true);
			
			return;
		} finally {
			this.shell.dispose();
			SWTResourceManager.dispose();
			this.display.dispose();
		}
	}
	
	//======================================================================================================================================
	
	public final boolean isGameRegistered(Game game) {
		if(game != null) {
			
		}
		return false;
	}
	
	public final boolean isInputCallbackRegistered(InputCallback inputCallback) {
		if(inputCallback != null) {
			
		}
		return false;
	}
	
	public final boolean registerInputCallback(InputCallback inputCallback) {
		if(inputCallback != null) {
			
		}
		return false;
	}
	
	public final boolean unregisterInputCallback(InputCallback inputCallback) {
		if(inputCallback != null) {
			
		}
		return false;
	}
	
	public final boolean isRendererRegistered(Renderer renderer) {
		if(renderer != null) {
			
		}
		return false;
	}
	
	public final boolean registerRenderer(Renderer renderer) {
		if(renderer != null) {
			
		}
		return false;
	}
	
	public final boolean unregisterRenderer(Renderer renderer) {
		if(renderer != null) {
			
		}
		return false;
	}
	
	//======================================================================================================================================
	
	/** MenuProvider is an interface which provides developers a way to add
	 * context menus to their game implementations.<br>
	 * 'MenuBar' menus appear in the top menu bar of the main application
	 * {@link Window}, and 'PopupMenu' menus appear in the right-click context
	 * menu of the main Window's {@link GLCanvas}.
	 * 
	 * @author Brian_Entei
	 * @since 1.0 */
	public static interface MenuProvider {
		
		/** Called when a new {@link Menu MenuBar} is being created for the main
		 * {@link Window}.<br>
		 * This allows you to use the provided menu to add your own
		 * {@link org.eclipse.swt.widgets.MenuItem menu items} which can perform
		 * various tasks when clicked.
		 * 
		 * @param menu The {@link Menu MenuBar} that you can populate with your
		 *            own {@link org.eclipse.swt.widgets.MenuItem menu items} */
		public void onMenuBarCreation(Menu menu);
		
		/** Called when the main {@link Window}'s existing {@link Menu MenuBar}
		 * is about to be disposed.<br>
		 * This gives you the opportunity to free up any system resources and
		 * perform any necessary tasks before the menu is destroyed.
		 * 
		 * @param menu The {@link Menu MenuBar} that is about to be disposed */
		public void onMenuBarDeletion(Menu menu);
		
		/** Called when a new {@link Menu PopupMenu} is being created for the
		 * main @link Window}'s {@link GLCanvas}.<br>
		 * This allows you to use the provided menu to add your own
		 * {@link org.eclipse.swt.widgets.MenuItem menu items} which can perform
		 * various tasks when clicked.
		 * 
		 * @param menu The {@link Menu PopupMenu} that you can populate with
		 *            your
		 *            own {@link org.eclipse.swt.widgets.MenuItem menu items} */
		public void onPopupMenuCreation(Menu menu);
		
		/** Called when the main {@link Window}'s {@link GLCanvas}' existing
		 * {@link Menu PopupMenu} (right-click menu) is about to be
		 * disposed.<br>
		 * This gives you the opportunity to free up any system resources and
		 * perform any necessary tasks before the menu is destroyed.
		 * 
		 * @param menu The {@link Menu PopupMenu} that is about to be
		 *            disposed */
		public void onPopupMenuDeletion(Menu menu);
		
	}
}
