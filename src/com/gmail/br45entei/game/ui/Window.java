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
import com.gmail.br45entei.game.Game;
import com.gmail.br45entei.game.graphics.GLThread;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Keyboard.Keys;
import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.lwjgl.natives.LWJGL_Natives;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.SWTUtil;

import java.awt.GraphicsEnvironment;
import java.beans.Beans;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
		
		Window window = new Window(800, 600);
		window.registerInputCallback(new InputCallback() {
			
			volatile boolean initialized;
			
			@Override
			public boolean isInitialized() {
				return this.initialized;
			}
			
			@Override
			public void initialize() {
				this.initialized = true;
			}
			
			@Override
			public void input(double deltaTime) {
				if(Keyboard.getKeyDown(Keys.VK_W)) {
					System.out.println("'W' was just pressed!");
				}
				if(Keyboard.getKeyUp(Keys.VK_W)) {
					System.out.println("'W' was just released!");
				}
			}
			
			@Override
			public void update(double deltaTime) {
			}
			
			@Override
			public void onMouseScroll(boolean vertical, int count) {
				System.out.println(String.format("On mouse scrolled %s: %s", vertical ? "vertically" : "horizontally", Integer.toString(count)));
			}
			
			@Override
			public void onMouseMoved(int oldX, int oldY, int newX, int newY) {
				System.out.println(String.format("On mouse moved: %s, %s --> %s, %s", Integer.toString(oldX), Integer.toString(oldY), Integer.toString(newX), Integer.toString(newY)));
			}
			
			@Override
			public void onMouseDoubleClick(int button) {
				System.out.println(String.format("On mouse button double click: ", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
			
			@Override
			public void onMouseButtonUp(int button) {
				System.out.println(String.format("On mouse button up: ", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
			
			@Override
			public void onMouseButtonDown(int button) {
				System.out.println(String.format("On mouse button down: ", (button == 1 ? "Left" : button == 2 ? "Middle" : button == 3 ? "Right" : Integer.toString(button))));
			}
			
			@Override
			public void onKeyDown(int key) {
				System.out.println(String.format("'%s' was just pressed!", Keys.getNameForKey(key)));
			}
			
			@Override
			public void onKeyHeld(int key) {
				System.out.println(String.format("'%s' is being held down!", Keys.getNameForKey(key)));
			}
			
			@Override
			public void onKeyUp(int key) {
				System.out.println(String.format("'%s' was just released!", Keys.getNameForKey(key)));
			}
			
			@Override
			public boolean handleException(Throwable ex, String method, Object... params) {
				ex.printStackTrace();
				return true;
			}
		});
		window.open();
	}
	
	protected volatile String title;
	protected volatile int x = 0, y = 0, glx = 0, gly = 0, width = 800,
			height = 600;
	protected volatile double framerate = 60.0D;
	
	protected volatile boolean running = false, shellActive = false;
	protected volatile boolean isFullscreen = false;
	
	protected Display display;
	protected Shell shell;
	
	protected GLData data;
	protected GLCanvas glCanvas;
	protected GLThread glThread;
	
	protected MenuItem mntmVerticalSync, mntmRenderers, mntmRendererOptions;
	
	protected InputCallback uiCallback;
	
	public static final int getDefaultRefreshRate() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
	}
	
	public static final GLData createDefaultGLData() {
		GLData data = new GLData();
		data.doubleBuffer = true;
		data.swapInterval = Integer.valueOf(1);
		data.majorVersion = 3;
		data.minorVersion = 3;
		data.forwardCompatible = true;
		return data;
	}
	
	public Window(String title, int width, int height, double framerate, GLData data) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 2);
		this.title = title == null ? "SWT-LWJGL3 Framework" : title;
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
		//this.glCanvas.setBackground(this.display.getSystemColor(SWT.COLOR_BLACK));
		/*this.glCanvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				//F2
				if(e.keyCode == SWT.F2 && (e.stateMask & SWT.CTRL) == 0 && (e.stateMask & SWT.SHIFT) == 0 && (e.stateMask & SWT.ALT) == 0) {
					Window.this.glThread.takeScreenshot();
				}
			}
		});*/
		
		this.uiCallback = new InputCallback() {
			volatile boolean initialized = false;
			
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
			public void onMouseMoved(int oldX, int oldY, int newX, int newY) {
			}
			
			@Override
			public void onMouseDoubleClick(int button) {
			}
			
			@Override
			public void onMouseButtonUp(int button) {
			}
			
			@Override
			public void onMouseButtonDown(int button) {
			}
			
			@Override
			public void onKeyDown(int key) {
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
		};
		
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
		
		this.createMenus();
	}
	
	/** @return Whether or not this {@link Window} is in fullscreen mode */
	public boolean isFullscreen() {
		if(Thread.currentThread() == this.getWindowThread()) {
			this.isFullscreen = this.shell.getFullScreen();
		}
		return this.isFullscreen;
	}
	
	/** Sets whether or not this {@link Window} takes up the entire monitor.<br>
	 * This method is thread safe.
	 * 
	 * @param fullScreen Whether or not this {@link Window} should be in full
	 *            screen mode
	 * @return This Window */
	public Window setFullscreen(boolean fullScreen) {
		if(Thread.currentThread() != this.getWindowThread()) {
			if(!this.display.isDisposed()) {
				this.display.asyncExec(() -> {
					this.setFullscreen(fullScreen);
				});
			}
			return this;
		}
		if(fullScreen != this.isFullscreen()) {
			if(fullScreen) {
				this.destroyMenus();
			}
			this.shell.setFullScreen(fullScreen);
			if(!fullScreen) {
				this.createMenus();
			}
			this.shell.redraw();
			this.shell.update();
		}
		return this;
	}
	
	/** Toggles the fullscreen state of this Window.<br>
	 * This method is thread safe.
	 * 
	 * @param blockIfAsync Whether or not this method should block until the
	 *            main display thread toggles the fullscreen state
	 * @return The resulting fullscreen state of this {@link Window} */
	public boolean toggleFullscreen(boolean blockIfAsync) {
		if(Thread.currentThread() != this.getWindowThread()) {
			if(!blockIfAsync) {
				if(!this.display.isDisposed()) {
					this.display.asyncExec(() -> {
						this.toggleFullscreen(false);
					});
				}
				return this.isFullscreen;
			}
			final Boolean[] rtrn = {null};
			if(!this.display.isDisposed()) {
				this.display.asyncExec(() -> {
					rtrn[0] = Boolean.valueOf(this.toggleFullscreen(false));
				});
			}
			Display display;
			while(rtrn[0] == null) {
				display = Display.getCurrent();
				if(display != null) {
					if(!display.readAndDispatch()) {
						CodeUtil.sleep(10L);
					}
				} else {
					CodeUtil.sleep(10L);
				}
			}
			return rtrn[0].booleanValue();
		}
		this.setFullscreen(!this.isFullscreen());
		return this.isFullscreen();
	}
	
	/** Toggles the fullscreen state of this Window.<br>
	 * This method is thread safe.<br>
	 * <b>Note:</b>&nbsp;If this method is called outside of the main display
	 * thread, it will cause the calling thread to sleep until the main display
	 * thread toggles the fullscreen state and then return the resulting
	 * state.<br>
	 * If this behaviour is not desired, use {@link #toggleFullscreen(boolean)
	 * toggleFullscreen(false)} instead.
	 * 
	 * @return The resulting fullscreen state of this {@link Window} */
	public boolean toggleFullscreen() {
		return this.toggleFullscreen(true);
	}
	
	protected void createMenus() {
		Menu menu = new Menu(this.shell, SWT.BAR);
		this.shell.setMenuBar(menu);
		
		MenuItem mntmfile = new MenuItem(menu, SWT.CASCADE);
		mntmfile.setText("&File");
		
		Menu menu_1 = new Menu(mntmfile);
		mntmfile.setMenu(menu_1);
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setText("E&xit\tAlt+F4");
		
		MenuItem mntmwindow = new MenuItem(menu, SWT.CASCADE);
		mntmwindow.setText("&Window");
		
		Menu menu_2 = new Menu(mntmwindow);
		mntmwindow.setMenu(menu_2);
		
		MenuItem mntmTakeScreenshot = new MenuItem(menu_2, SWT.NONE);
		mntmTakeScreenshot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.glThread.takeScreenshot();
			}
		});
		mntmTakeScreenshot.setText("Take Screenshot\tF2");
		
		this.mntmVerticalSync = new MenuItem(menu_2, SWT.CHECK);
		this.mntmVerticalSync.setText("Vertical Sync\tV");
		this.mntmVerticalSync.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.getGLThread().setVsyncEnabled(Window.this.mntmVerticalSync.getSelection());
			}
		});
		
		MenuItem mntmFullscreen = new MenuItem(menu_2, SWT.CHECK);
		mntmFullscreen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		mntmFullscreen.setText("Fullscreen\tF11");
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		this.mntmRenderers = new MenuItem(menu_2, SWT.CASCADE);
		this.mntmRenderers.setText("Renderers");
		
		Menu menu_4 = new Menu(this.mntmRenderers);
		this.mntmRenderers.setMenu(menu_4);
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		this.mntmRendererOptions = new MenuItem(menu, SWT.CASCADE);
		this.mntmRendererOptions.setText("Renderer Options");
		
		final Menu menu_3 = new Menu(this.mntmRendererOptions);
		this.mntmRendererOptions.setMenu(menu_3);
		
		Renderer renderer = this.glThread.getRenderer();
		if(renderer instanceof MenuProvider) {
			MenuProvider provider = (MenuProvider) renderer;
			
			try {
				String name = provider.getMenuName();
				try {
					provider.onMenuBarCreation(menu_3);
					if(name != null && !name.trim().isEmpty()) {
						this.mntmRendererOptions.setText(name.replace("&", "&&"));
					}
					
					//TODO Implement provider.onPopupMenuCreation(...) here!
				} catch(Throwable ex) {
					if(!GLThread.handleRendererException(renderer, ex, "onMenuBarCreation", menu_3)) {
						this.glThread.setRenderer(null);
					}
				}
			} catch(Throwable ex) {
				if(!GLThread.handleRendererException(renderer, ex, "getMenuName")) {
					this.glThread.setRenderer(null);
				}
			}
		}
	}
	
	protected void destroyMenus() {
		Menu menu = this.shell.getMenuBar();
		if(menu != null) {
			this.shell.setMenuBar(null);
			menu.dispose();
		}
		this.mntmVerticalSync = this.mntmRenderers = this.mntmRendererOptions = null;
		
		menu = this.glCanvas.getMenu();
		if(menu != null) {
			this.glCanvas.setMenu(null);
			menu.dispose();
		}
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
	
	/** Has the main display thread execute the given runnable at the nearest
	 * opportunity.
	 * 
	 * @param runnable The runnable to execute on the main display thread
	 * @return This {@link Window}
	 * @throws RejectedExecutionException Thrown if the main display has been
	 *             disposed */
	public Window swtExec(Runnable runnable) throws RejectedExecutionException {
		if(this.display.isDisposed()) {
			throw new RejectedExecutionException("Display is disposed!");
		}
		this.display.asyncExec(runnable);
		return this;
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
	
	/** Convenience method alternative to {@link #getSize()}.<br>
	 * Returns the bounds of this {@link Window}'s {@link GLCanvas}.<br>
	 * This method is thread safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The x and y values are always zero.
	 * 
	 * @return This {@link Window}'s {@link GLCanvas}' bounds */
	public Rectangle getViewport() {
		return new Rectangle(0, 0, this.width, this.height);
	}
	
	/** Returns the size of this {@link Window}'s {@link GLCanvas}.<br>
	 * This method is thread safe.
	 * 
	 * @return The size of this {@link Window}'s {@link GLCanvas} */
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
	
	/** Returns this {@link Window}'s location.<br>
	 * This method is thread safe.
	 * 
	 * @return The location of this {@link Window}, in display-relative
	 *         coordinates */
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
	
	public final boolean registerGame(Game game) {
		if(game != null) {
			
		}
		return false;
	}
	
	public final boolean unregisterGame(Game game) {
		if(game != null) {
			
		}
		return false;
	}
	
	public final boolean isInputCallbackRegistered(InputCallback inputCallback) {
		return Mouse.isInputCallbackRegistered(inputCallback) && Keyboard.isInputCallbackRegistered(inputCallback);
	}
	
	public final boolean registerInputCallback(InputCallback inputCallback) {
		return Mouse.registerInputCallback(inputCallback) | Keyboard.registerInputCallback(inputCallback);
	}
	
	public final boolean unregisterInputCallback(InputCallback inputCallback) {
		return Mouse.unregisterInputCallback(inputCallback) | Keyboard.unregisterInputCallback(inputCallback);
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
	
	public final boolean isMenuProviderRegistered(MenuProvider provider) {
		if(provider != null) {
			
		}
		return false;
	}
	
	public final boolean registerMenuProvider(MenuProvider provider) {
		if(provider != null && !(provider instanceof Renderer)) {
			
		}
		return false;
	}
	
	public final boolean unregisterMenuProvider(MenuProvider provider) {
		if(provider != null) {
			
		}
		return false;
	}
	
	//======================================================================================================================================
	
}
