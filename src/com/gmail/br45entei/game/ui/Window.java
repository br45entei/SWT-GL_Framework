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

import com.gmail.br45entei.game.Game;
import com.gmail.br45entei.game.graphics.GLThread;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.InputCallback.InputLogger;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.game.ui.swt.RendererMenuItem;
import com.gmail.br45entei.lwjgl.natives.LWJGL_Natives;
import com.gmail.br45entei.util.Architecture;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.Platform;
import com.gmail.br45entei.util.SWTUtil;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.Beans;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MenuDetectListener;
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
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.swt.GLCanvas;
import org.lwjgl.opengl.swt.GLData;

/** The main Window class which manages background tasks such as maintaining the
 * application window.
 *
 * @author Brian_Entei
 * @since 1.0 */
public class Window {
	
	static {
		String[] extraNativesToLoad;
		switch(Platform.get()) {
		case WINDOWS:
			final String pathRoot;
			switch(Architecture.get()) {
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
	
	/** Returns whether or not the specified {@link Shell} is currently the
	 * operating system's foreground window; meaning the end-user is currently
	 * using it.<br>
	 * This method <b><em>must</em></b> be called from the user-interface thread
	 * for the specified shell's display.
	 * 
	 * @param shell The shell to check
	 * @return Whether or not the specified shell is active */
	public static final boolean isShellActive(Shell shell) {
		boolean shellActive;
		long shellHandle = SWTUtil.getHandle(shell);
		switch(Platform.get()) {
		case WINDOWS:
			shellActive = org.eclipse.swt.internal.win32.OS.GetForegroundWindow() == shellHandle;
			break;
		case LINUX:
		case MACOSX:
		case UNKNOWN:
		default:
			shellActive = shell.getDisplay().getActiveShell() == shell && shell.isVisible();
			break;
		}
		return shellActive;
	}
	
	/** Launches the application.
	 * 
	 * @param args Program command line arguments */
	public static void main(String[] args) {
		Window window = new Window(800, 600);
		window.registerInputCallback(new InputLogger(System.out));
		window.setActiveRenderer(Renderer.colorDemo);
		window.open();
	}
	
	protected volatile String title;
	protected volatile int shellX = 0, shellY = 0;
	protected volatile int shellWidth = 0, shellHeight = 0;
	protected volatile int glx = 0, gly = 0;
	protected volatile int glTargetWidth = 800, glTargetHeight = 600;
	protected volatile int glWidth = this.glTargetWidth;
	protected volatile int glHeight = this.glTargetHeight;
	protected volatile double framerate = 60.0D;
	
	protected volatile boolean running = false, shellActive = false;
	protected volatile boolean isFullscreen = false;
	
	protected volatile long lastMenuInteraction = 0L;
	
	protected Display display;
	protected Shell shell;
	protected long shellHandle;
	
	protected GLData data;
	protected GLCanvas glCanvas;
	protected GLThread glThread;
	
	protected MenuItem mntmVerticalSync, mntmRenderers, mntmRendererOptions;
	
	protected InputCallback uiCallback;
	
	protected final ConcurrentLinkedDeque<Game> games = new ConcurrentLinkedDeque<>();
	protected final ConcurrentLinkedDeque<Renderer> renderers = new ConcurrentLinkedDeque<>();
	protected final ConcurrentLinkedDeque<InputCallback> inputListeners = new ConcurrentLinkedDeque<>();
	protected final ConcurrentLinkedDeque<MenuProvider> menuProviders = new ConcurrentLinkedDeque<>();
	
	private final void resetFieldsOnClose() {
		//this.title = null;
		this.shellX = this.shellY = 0;
		this.shellWidth = this.shellHeight = 0;
		this.glx = this.gly = 0;
		//this.glWidth = 800;
		//this.glHeight = 600;
		//this.framerate = 60.0D;
		
		this.running = false;
		this.shellActive = false;
		this.isFullscreen = false;
		
		this.display = null;
		this.shell = null;
		
		//this.data = null;
		this.glCanvas = null;
		this.glThread = null;
		
		this.mntmVerticalSync = this.mntmRenderers = this.mntmRendererOptions = null;
		
		//this.uiCallback = null;
		
		if(instance == this) {
			instance = null;
		}
	}
	
	/** Returns the default refresh rate of the current local
	 * {@link GraphicsEnvironment}'s {@link GraphicsDevice default screen
	 * device}'s display mode.<br>
	 * <br>
	 * This is generally either 50, 60, or 75 Hz.<br>
	 * Some modern gaming displays are capable of 240 Hz (or more!).
	 * 
	 * @return The current display mode's refresh rate. */
	public static final int getDefaultRefreshRate() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
	}
	
	/** Returns a new {@link GLData} with the default settings for creating a
	 * {@link Window}.<br>
	 * DoubleBuffer is enabled, the swap interval is set to 1, the OpenGL
	 * version is set to 3.3, and the context is set to be forward compatible.
	 * 
	 * @return A new {@link GLData} with the default settings for creating a
	 *         {@link Window}. */
	public static final GLData createDefaultGLData() {
		GLData data = new GLData();
		data.doubleBuffer = true;
		data.swapInterval = Integer.valueOf(1);
		data.majorVersion = 3;
		data.minorVersion = 3;
		data.forwardCompatible = true;
		return data;
	}
	
	/** Creates a new Window with the specified {@link GLData}, the specified
	 * viewport size, and the specified framerate.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param title The title that this Window will display
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height)
	 * @param framerate The framerate that the {@link GLThread} will run at
	 * @param data The {@link GLData} that the {@link GLThread} will use when
	 *            creating the OpenGL context
	 * @param renderer The renderer that this Window's {@link GLThread} will
	 *            attempt to use to display graphics */
	public Window(String title, int width, int height, double framerate, GLData data, Renderer renderer) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 2);
		this.title = title == null ? "SWT-LWJGL3 Framework" : title;
		this.glTargetWidth = width;
		this.glTargetHeight = height;
		this.framerate = framerate != framerate || Double.isInfinite(framerate) ? Window.getDefaultRefreshRate() : framerate;
		this.data = data == null ? Window.createDefaultGLData() : data;
		
		this.registerInputCallback(this.uiCallback = new UICallback(this));
		
		this.createContents();
		
		this.setActiveRenderer(renderer);
	}
	
	/** Creates a new Window with the specified {@link GLData}, the specified
	 * viewport size, and the specified framerate.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param title The title that this Window will display
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height)
	 * @param framerate The framerate that the {@link GLThread} will run at
	 * @param data The {@link GLData} that the {@link GLThread} will use when
	 *            creating the OpenGL context */
	public Window(String title, int width, int height, double framerate, GLData data) {
		this(title, width, height, framerate, data, (Renderer) null);
	}
	
	/** Creates a new Window with the specified viewport size and framerate.<br>
	 * A default {@link GLData} is created and used via
	 * {@link #createDefaultGLData()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param title The title that this Window will display
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height)
	 * @param framerate The framerate that the {@link GLThread} will run at */
	public Window(String title, int width, int height, double framerate) {
		this(title, width, height, framerate, Window.createDefaultGLData());
	}
	
	/** Creates a new Window with the specified viewport size and framerate.<br>
	 * A default {@link GLData} is created and used via
	 * {@link #createDefaultGLData()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param title The title that this Window will display
	 * @param framerate The framerate that the {@link GLThread} will run at */
	public Window(String title, double framerate) {
		this(title, 800, 600, framerate);
	}
	
	/** Creates a new Window with the specified viewport size and framerate.<br>
	 * A default {@link GLData} is created and used via
	 * {@link #createDefaultGLData()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param title The title that this Window will display
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height) */
	public Window(String title, int width, int height) {
		this(title, width, height, Window.getDefaultRefreshRate());
	}
	
	/** Creates a new Window with the specified viewport size and framerate.<br>
	 * A default {@link GLData} is created and used via
	 * {@link #createDefaultGLData()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height)
	 * @param framerate The framerate that the {@link GLThread} will run at */
	public Window(int width, int height, double framerate) {
		this(null, width, height, framerate);
	}
	
	/** Creates a new Window with the specified {@link GLData}, the specified
	 * viewport size, and the framerate is set to
	 * {@link #getDefaultRefreshRate()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height)
	 * @param data The {@link GLData} that the {@link GLThread} will use when
	 *            creating the OpenGL context */
	public Window(int width, int height, GLData data) {
		this(null, width, height, Window.getDefaultRefreshRate(), data);
	}
	
	/** Creates a new Window with the specified viewport size, and the framerate
	 * is set to {@link #getDefaultRefreshRate()}.<br>
	 * A default {@link GLData} is created and used via
	 * {@link #createDefaultGLData()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param width The width of the viewport (this will be the
	 *            {@link GLCanvas}' width)
	 * @param height The width of the viewport (this will be the
	 *            {@link GLCanvas}' height) */
	public Window(int width, int height) {
		this(width, height, Window.getDefaultRefreshRate());
	}
	
	/** Creates a new Window with the specified {@link GLData}, a default
	 * viewport size of <tt>800x600</tt>, and the framerate is set to
	 * {@link #getDefaultRefreshRate()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread.
	 * 
	 * @param data The {@link GLData} that the {@link GLThread} will use when
	 *            creating the OpenGL context */
	public Window(GLData data) {
		this(null, 800, 600, getDefaultRefreshRate(), data);
	}
	
	/** Creates a new Window with a default viewport size of <tt>800x600</tt>,
	 * and the framerate is set to {@link #getDefaultRefreshRate()}.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The thread that creates this Window needs to be the
	 * thread that {@link #open()}s it, as it will become the main display
	 * thread. */
	public Window() {
		this(800, 600);
	}
	
	private void createContents() {
		this.display = Display.getDefault();
		if(this.display.getThread() != Thread.currentThread()) {
			this.display = new Display();
		}
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
				if(Window.this.glThread == null || !Window.this.glThread.isRecording()) {
					synchronized(Window.this.glThread) {
						Window.this.glCanvas.setBounds(Window.this.shell.getClientArea());
					}
				}
				
				this.controlMoved(e);
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				Point location = Window.this.shell.getLocation();
				Window.this.shellX = location.x;
				Window.this.shellY = location.y;
				
				location = Window.this.display.map(Window.this.glCanvas, null, 0, 0);
				Window.this.glx = location.x;
				Window.this.gly = location.y;
				
				Point size = Window.this.glCanvas.getSize();
				Window.this.glWidth = size.x;
				Window.this.glHeight = size.y;
				
				size = Window.this.shell.getSize();
				Window.this.shellWidth = size.x;
				Window.this.shellHeight = size.y;
			}
		});
		
		if(GL.getFunctionProvider() == null) {
			GL.create();
		}
		try {
			this.glCanvas = new GLCanvas(this.shell, SWT.DOUBLE_BUFFERED, this.data);
		} catch(SWTException ex) {
			if(ex.getMessage() != null && ex.getMessage().startsWith("Swap interval requested but ") && ex.getMessage().endsWith(" is unavailable")) {
				this.data.swapInterval = null;
				this.glCanvas = new GLCanvas(this.shell, SWT.DOUBLE_BUFFERED, this.data);
			} else {
				throw ex;
			}
		}
		this.glCanvas.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point size = Window.this.glCanvas.getSize();
				Window.this.glWidth = size.x;
				Window.this.glHeight = size.y;
			}
		});
		
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
		
		if(Beans.isDesignTime()) {
			this.shell.setSize(this.glTargetWidth, this.glTargetHeight);
			Rectangle clientArea = this.shell.getClientArea();
			Point size = this.shell.getSize();
			int xDiff = size.x - clientArea.width,
					yDiff = size.y - clientArea.height;
			this.shell.setSize(this.glTargetWidth + xDiff, this.glTargetHeight + yDiff);
			this.glCanvas.setBounds(this.shell.getClientArea());
		}
		
		this.glThread = new GLThread(this.glCanvas);
		
		this.createMenus();
		
		if(instance == null || instance.shell == null || instance.shell.isDisposed()) {
			instance = this;
		}
	}
	
	/** Returns this {@link Window}'s current target FPS (frames per
	 * second).<br>
	 * This method is thread-safe.
	 * 
	 * @return The current target FPS */
	public int getRefreshRate() {
		return this.glThread.isVsyncEnabled() ? Window.getDefaultRefreshRate() : Long.valueOf(Math.round(Math.ceil(this.glThread.getTargetFPS()))).intValue();
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
			SWTUtil.setAlwaysOnTop(this.shell, fullScreen);
			this.shell.setFullScreen(fullScreen);
			if(!fullScreen) {
				this.createMenus();
			}
			this.shell.redraw();
			this.shell.update();
			this.shell.forceActive();
			this.shell.forceFocus();
		}
		this.isFullscreen();
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
	
	public boolean isVsyncEnabled() {
		return this.getGLThread().getRefreshRate() == Window.getDefaultRefreshRate();
	}
	
	private volatile double lastNonDefaultFramerate = 0;
	
	public Window setVSyncEnabled(boolean vsync) {
		if(this.glThread.isRecordingStartingUp() || this.glThread.isRecording()) {
			return this;
		}
		Runnable code = () -> {
			double fps = Window.this.getGLThread().getTargetFPS();
			int check = Window.this.getGLThread().getRefreshRate();
			int defaultRefreshRate = Window.getDefaultRefreshRate();
			
			this.getGLThread().setFPS(vsync ? defaultRefreshRate : this.lastNonDefaultFramerate);
			if(this.mntmVerticalSync != null && !this.mntmVerticalSync.isDisposed()) {
				this.mntmVerticalSync.setSelection(this.isVsyncEnabled());
			}
			
			if(check != Window.this.getGLThread().getRefreshRate() && check != defaultRefreshRate) {
				this.lastNonDefaultFramerate = fps;
			}
		};
		if(Thread.currentThread() == this.getWindowThread()) {
			code.run();
			return this;
		}
		if(this.display.isDisposed()) {
			throw new RejectedExecutionException("Display is disposed!");
		}
		this.display.asyncExec(code);
		return this;
	}
	
	public Window toggleVsyncEnabled() {
		return this.setVSyncEnabled(!this.isVsyncEnabled());
	}
	
	@SuppressWarnings("unused")
	protected void createMenus() {
		synchronized(this.glThread) {
			//==[Main MenuBar]=============================================
			
			Menu menu = new Menu(this.shell, SWT.BAR);
			this.shell.setMenuBar(menu);
			
			MenuItem mntmfile = new MenuItem(menu, SWT.CASCADE);
			mntmfile.setText("&File");
			
			Menu menu_1 = new Menu(mntmfile);
			mntmfile.setMenu(menu_1);
			
			new MenuItem(menu_1, SWT.SEPARATOR);
			
			MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
			mntmExit.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Window.this.close();
				}
			});
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
			
			final MenuItem mntmStartRecording = new MenuItem(menu_2, SWT.CHECK);
			mntmStartRecording.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						if(Window.this.glThread.isRecording() || Window.this.glThread.isRecordingFinishingUp()) {
							mntmStartRecording.setEnabled(false);
							mntmStartRecording.setText("Recording finishing up...");
							Window.this.glThread.stopRecording((v) -> Boolean.valueOf(Window.this.swtLoop()));
						} else {
							Window.this.glThread.startRecording(Window.this.getViewport());
							while(Window.this.glThread.isRecordingStartingUp() && Window.this.swtLoop() && !mntmStartRecording.isDisposed()) {
								SWTUtil.setEnabled(mntmStartRecording, !Window.this.glThread.isRecordingStartingUp());
								SWTUtil.setText(mntmStartRecording, mntmStartRecording.getEnabled() ? "Stop Recording\tShift+F2" : "Recording starting up...");
							}
						}
					} finally {
						if(!Window.this.shell.isDisposed() && !mntmStartRecording.isDisposed()) {
							mntmStartRecording.setEnabled(!Window.this.glThread.isRecordingStartingUp() && !Window.this.glThread.isRecordingFinishingUp());
							mntmStartRecording.setSelection(Window.this.glThread.isRecording());
							mntmStartRecording.setText(mntmStartRecording.getSelection() ? "Stop Recording\tShift+F2" : (Window.this.glThread.isRecordingStartingUp() ? "Recording starting up..." : "Start Recording\tShift+F2"));
						}
					}
				}
			});
			mntmStartRecording.setText("Start Recording\tShift+F2");
			
			new MenuItem(menu_2, SWT.SEPARATOR);
			
			this.mntmVerticalSync = new MenuItem(menu_2, SWT.CHECK);
			this.mntmVerticalSync.setText("Vertical Sync\tV");
			this.mntmVerticalSync.setSelection(this.getGLThread().getRefreshRate() == Window.getDefaultRefreshRate());
			this.mntmVerticalSync.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Window.this.toggleVsyncEnabled();
					//Window.this.getGLThread().setVsyncEnabled(Window.this.mntmVerticalSync.getSelection());
				}
			});
			
			final MenuItem mntmFullscreen = new MenuItem(menu_2, SWT.CHECK);
			mntmFullscreen.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Window.this.toggleFullscreen();
				}
			});
			mntmFullscreen.setText("Fullscreen\tF11");
			
			new MenuItem(menu_2, SWT.SEPARATOR);
			
			this.mntmRenderers = new MenuItem(menu_2, SWT.CASCADE);
			this.mntmRenderers.setText("Applications\t(Alt+Left | Alt+Right)");
			
			mntmwindow.addArmListener(new ArmListener() {
				@Override
				public void widgetArmed(ArmEvent e) {
					if(!Window.this.shell.isDisposed()) {
						if(!mntmStartRecording.isDisposed()) {
							if(Window.this.glThread.isRecordingFinishingUp()) {
								mntmStartRecording.setEnabled(false);
								mntmStartRecording.setText("Recording finishing up...");
								Window.this.glThread.stopRecording((v) -> Boolean.valueOf(Window.this.swtLoop()));
							}
							mntmStartRecording.setEnabled(!Window.this.glThread.isRecordingStartingUp() && !Window.this.glThread.isRecordingFinishingUp());
							mntmStartRecording.setSelection(Window.this.glThread.isRecording());
							mntmStartRecording.setText(mntmStartRecording.getSelection() ? "Stop Recording\tShift+F2" : (Window.this.glThread.isRecordingStartingUp() ? "Recording starting up..." : "Start Recording\tShift+F2"));
						}
						if(!Window.this.mntmVerticalSync.isDisposed()) {
							Window.this.mntmVerticalSync.setSelection(Window.this.getGLThread().getRefreshRate() == Window.getDefaultRefreshRate());
						}
						if(!mntmFullscreen.isDisposed()) {
							mntmFullscreen.setSelection(Window.this.isFullscreen());// This will probably always return false, but just in case ...
						}
						
					}
				}
			});
			
			Menu menu_3 = new Menu(this.mntmRenderers);
			this.mntmRenderers.setMenu(menu_3);
			
			for(Renderer renderer : this.getAvailableRenderers()) {
				String name;
				try {
					name = renderer.getName();
				} catch(Throwable ex) {
					GLThread.handleRendererException(renderer, ex, "getName");
					continue;
				}
				this.addRendererToCascadeMenu(renderer, name);
			}
			
			new MenuItem(menu, SWT.SEPARATOR);
			
			this.mntmRendererOptions = new MenuItem(menu, SWT.CASCADE);
			this.mntmRendererOptions.setText("Renderer Options");
			
			final MenuItem mntmSeparator = new MenuItem(menu, SWT.SEPARATOR);
			
			final Menu rendererMenu = new Menu(this.mntmRendererOptions);
			this.mntmRendererOptions.setMenu(rendererMenu);
			
			//==[Popup Menu]=============================================
			
			final Menu popupMenu;
			final boolean justCreatedPopupMenu;
			{
				Menu check = this.glCanvas.getMenu();
				if(check != null) {
					popupMenu = check;
					justCreatedPopupMenu = false;
				} else {
					popupMenu = new Menu(this.glCanvas);
					this.glCanvas.setMenu(popupMenu);
					justCreatedPopupMenu = true;
				}
			}
			
			//==[MenuProvider Implementation]=============================================
			
			Renderer renderer = this.glThread.getRenderer();
			if(renderer instanceof MenuProvider) {
				MenuProvider provider = (MenuProvider) renderer;
				
				try {
					String name = provider.getMenuName();
					try {
						provider.onMenuBarCreation(rendererMenu);
						if(name != null && !name.trim().isEmpty()) {
							this.mntmRendererOptions.setText(name.replace("&", "&&"));
						}
						
						if(justCreatedPopupMenu) {
							try {
								provider.onPopupMenuCreation(popupMenu);
							} catch(Throwable ex) {
								if(!Window.handleMenuProviderException(provider, ex, "onPopupMenuCreation", popupMenu)) {
									this.glThread.setRenderer(null);
									mntmSeparator.dispose();
									this.mntmRendererOptions.dispose();
									this.mntmRendererOptions = null;
								}
							}
						}
					} catch(Throwable ex) {
						if(!Window.handleMenuProviderException(provider, ex, "onMenuBarCreation", rendererMenu)) {
							this.glThread.setRenderer(null);
							mntmSeparator.dispose();
							this.mntmRendererOptions.dispose();
							this.mntmRendererOptions = null;
						}
					}
				} catch(Throwable ex) {
					if(!Window.handleMenuProviderException(provider, ex, "getMenuName")) {
						this.glThread.setRenderer(null);
						mntmSeparator.dispose();
						this.mntmRendererOptions.dispose();
						this.mntmRendererOptions = null;
					}
				}
			} else {
				mntmSeparator.dispose();
				this.mntmRendererOptions.dispose();
				this.mntmRendererOptions = null;
			}
			
			final MenuDetectListener menuDetectListener = (e) -> {
				this.lastMenuInteraction = System.currentTimeMillis();
			};
			this.shell.addMenuDetectListener(menuDetectListener);
			this.glCanvas.addMenuDetectListener(menuDetectListener);
			
			final ArmListener armListener = (e) -> {
				this.lastMenuInteraction = System.currentTimeMillis();
			};
			@SuppressWarnings("unchecked")
			final Function<Menu, Void>[] addArmListener = new Function[1];
			addArmListener[0] = (m) -> {
				if(m != null) {
					for(MenuItem item : m.getItems()) {
						item.addArmListener(armListener);
						addArmListener[0].apply(item.getMenu());
					}
				}
				return null;
			};
			
			addArmListener[0].apply(menu);
			addArmListener[0].apply(popupMenu);
			
		}
	}
	
	protected boolean addRendererToCascadeMenu(Renderer renderer, String name) {
		if(renderer != null) {
			name = name == null || name.trim().isEmpty() ? renderer.getClass().getSimpleName() : name;
			Menu menu = this.mntmRenderers == null || this.mntmRenderers.isDisposed() ? null : this.mntmRenderers.getMenu();
			if(menu != null) {
				for(MenuItem item : menu.getItems()) {
					if(item instanceof RendererMenuItem) {
						RendererMenuItem rItem = (RendererMenuItem) item;
						if(rItem.getRenderer() == renderer) {
							return SWTUtil.setText(rItem, name);
						}
					}
				}
				final RendererMenuItem item = new RendererMenuItem(menu, renderer, SWT.RADIO);
				item.setText(name);
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						boolean selection = Window.this.setActiveRenderer(item.getRenderer());
						if(!item.isDisposed()) {
							item.setSelection(selection);
						}
					}
				});
				item.setSelection(Window.this.getActiveRenderer() == renderer);
				return true;
			}
		}
		return false;
	}
	
	protected boolean removeRendererFromCascadeMenu(Renderer renderer) {
		if(renderer != null) {
			Menu menu = this.mntmRenderers == null || this.mntmRenderers.isDisposed() ? null : this.mntmRenderers.getMenu();
			if(menu != null) {
				boolean removed = false;
				for(MenuItem item : menu.getItems()) {
					if(item instanceof RendererMenuItem) {
						RendererMenuItem rItem = (RendererMenuItem) item;
						if(rItem.getRenderer() == renderer) {
							rItem.dispose();
							removed |= true;
						}
					}
				}
				return removed;
			}
		}
		return false;
	}
	
	protected void createGLCanvasPopupMenu() {
		synchronized(this.glThread) {
			final Menu popupMenu;
			final boolean justCreatedPopupMenu;
			{
				Menu check = this.glCanvas.getMenu();
				if(check != null) {
					popupMenu = check;
					justCreatedPopupMenu = false;
				} else {
					popupMenu = new Menu(this.glCanvas);
					this.glCanvas.setMenu(popupMenu);
					justCreatedPopupMenu = true;
				}
			}
			if(justCreatedPopupMenu) {
				Renderer renderer = this.glThread.getRenderer();
				if(renderer instanceof MenuProvider) {
					MenuProvider provider = (MenuProvider) renderer;
					try {
						provider.onPopupMenuCreation(popupMenu);
					} catch(Throwable ex) {
						if(!Window.handleMenuProviderException(provider, ex, "onPopupMenuCreation", popupMenu)) {
							this.glThread.setRenderer(null);
							this.mntmRendererOptions.dispose();
							this.mntmRendererOptions = null;
						}
					}
				}
			}
		}
	}
	
	protected void destroyGLCanvasPopupMenu() {
		synchronized(this.glThread) {
			Menu menu = this.glCanvas.getMenu();
			if(menu != null) {
				this.glCanvas.setMenu(null);
				
				final Renderer renderer = this.glThread.getRenderer();
				MenuProvider provider = renderer instanceof MenuProvider ? (MenuProvider) renderer : null;
				if(provider != null) {
					try {
						provider.onPopupMenuDeletion(menu);
					} catch(Throwable ex) {
						if(!Window.handleMenuProviderException(provider, ex, "onPopupMenuDeletion", menu)) {
							this.glThread.setRenderer(null);
							provider = null;
						}
					}
				}
				
				menu.dispose();
			}
		}
	}
	
	protected void destroyMenus() {
		synchronized(this.glThread) {
			final Renderer renderer = this.glThread.getRenderer();
			MenuProvider provider = renderer instanceof MenuProvider ? (MenuProvider) renderer : null;
			
			Menu menu = this.shell.getMenuBar();
			if(menu != null) {
				this.shell.setMenuBar(null);
				
				if(provider != null) {
					try {
						provider.onMenuBarDeletion(menu);
					} catch(Throwable ex) {
						if(!Window.handleMenuProviderException(provider, ex, "onMenuBarDeletion", menu)) {
							this.glThread.setRenderer(null);
							provider = null;
						}
					}
				}
				
				menu.dispose();
			}
			this.mntmVerticalSync = this.mntmRenderers = this.mntmRendererOptions = null;
		}
		
		this.destroyGLCanvasPopupMenu();
	}
	
	/** Maintains the application window, polls the mouse and keyboard, and
	 * performs various other upkeep tasks.<br>
	 * <b>Note:</b>&nbsp;This method should only be called by the main display
	 * thread.
	 * 
	 * @return Whether or not this window should continue running */
	public boolean swtLoop() {
		while(this.display.readAndDispatch()) {
		}
		CodeUtil.sleep(2L);
		if(!this.shell.isDisposed()) {
			this.shellActive = Window.isShellActive(this.shell);
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
			
			final double deltaTime = this.glThread.getDeltaTime();
			final Double dt = Double.valueOf(deltaTime);
			long startTime = System.currentTimeMillis();
			for(InputCallback listener : this.inputListeners) {
				try {
					listener.input(deltaTime);
				} catch(Throwable ex) {
					if(!handleListenerException(listener, ex, "input", dt)) {
						this.unregisterInputCallback(listener);
					}
				}
				
				if(System.currentTimeMillis() - startTime >= 4L) {
					while(this.display.readAndDispatch()) {
					}
					CodeUtil.sleep(1L);
					startTime = System.currentTimeMillis();
					if(this.shell.isDisposed()) {
						break;
					}
				}
			}
			startTime = System.currentTimeMillis();
			for(InputCallback listener : this.inputListeners) {
				try {
					listener.update(deltaTime);
				} catch(Throwable ex) {
					if(!handleListenerException(listener, ex, "update", dt)) {
						this.unregisterInputCallback(listener);
					}
				}
				
				if(System.currentTimeMillis() - startTime >= 4L) {
					while(this.display.readAndDispatch()) {
					}
					CodeUtil.sleep(1L);
					startTime = System.currentTimeMillis();
					if(this.shell.isDisposed()) {
						break;
					}
				}
			}
			
			// XXX Fix for when the cursor is captured and the user right clicks, causing the
			// cursor to suddenly become visible for a split second in an attempt to bring up the popup menu
			if(this.isFullscreen() || (Mouse.isCaptured() && !Mouse.isModal())) {
				this.destroyGLCanvasPopupMenu();
			} else {
				this.createGLCanvasPopupMenu();
			}
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
	
	/** @return The main display thread */
	public final Thread getWindowThread() {
		return this.display.isDisposed() ? null : this.display.getThread();
	}
	
	/** Returns the {@link GLThread} that this {@link Window} is using.<br>
	 * This method is thread safe.
	 * 
	 * @return The {@link GLThread} that this {@link Window} is using */
	public final GLThread getGLThread() {
		return this.glThread;
	}
	
	/** Returns the width of this {@link Window}'s {@link GLCanvas}.<br>
	 * This method is thread safe.
	 * 
	 * @return The width of this {@link Window}'s {@link GLCanvas} */
	public int getWidth() {
		return this.glWidth;
	}
	
	/** Returns the height of this {@link Window}'s {@link GLCanvas}.<br>
	 * This method is thread safe.
	 * 
	 * @return The height of this {@link Window}'s {@link GLCanvas} */
	public int getHeight() {
		return this.glHeight;
	}
	
	/** Convenience method alternative to {@link #getSize()}.<br>
	 * Returns the bounds of this {@link Window}'s {@link GLCanvas}.<br>
	 * This method is thread safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;The x and y values are always zero.
	 * 
	 * @return This {@link Window}'s {@link GLCanvas}' bounds */
	public Rectangle getViewport() {
		return new Rectangle(0, 0, this.glWidth, this.glHeight);
	}
	
	/** Returns the size of this {@link Window}.<br>
	 * This method is thread safe.
	 * 
	 * @return The size of this {@link Window} */
	public Point getSize() {
		return new Point(this.shellWidth, this.shellHeight);
	}
	
	/** Returns the size of this {@link Window}'s {@link GLCanvas}.<br>
	 * This method is thread safe.
	 * 
	 * @return The size of this {@link Window}'s {@link GLCanvas} */
	public Point getGLCanvasSize() {
		return new Point(this.glWidth, this.glHeight);
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
	public Window setGLCanvasSize(int width, int height) throws SWTException {
		if(Thread.currentThread() == this.getWindowThread()) {
			this.shell.setSize(width, height);
			Rectangle clientArea = this.shell.getClientArea();
			Point size = this.shell.getSize();
			int xDiff = size.x - clientArea.width,
					yDiff = size.y - clientArea.height;
			this.shell.setSize(width + xDiff, height + yDiff);
			synchronized(this.glThread) {
				this.glCanvas.setBounds(this.shell.getClientArea());
			}
		} else {
			this.display.asyncExec(() -> {
				this.setGLCanvasSize(width, height);
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
	public Window setGLCanvasSize(Point size) throws SWTException {
		this.setGLCanvasSize(size.x, size.y);
		return this;
	}
	
	/** Returns this {@link Window}'s location.<br>
	 * This method is thread safe.
	 * 
	 * @return The location of this {@link Window}, in display-relative
	 *         coordinates */
	public Point getLocation() {
		return new Point(this.shellX, this.shellY);
	}
	
	/** Sets the location of this {@link Window}, in display-relative
	 * coordinates.<br>
	 * This method is thread-safe.
	 * 
	 * @param x The x coordinate where the top-left of this window will be moved
	 *            to
	 * @param y The y coordinate where the top-left of this window will be moved
	 *            to
	 * @return This Window */
	public Window setLocation(int x, int y) {
		if(Thread.currentThread() == this.getWindowThread()) {
			this.shell.setLocation(x, y);
		} else {
			this.display.asyncExec(() -> {
				this.setLocation(x, y);
			});
		}
		return this;
	}
	
	/** Returns the absolute position of this {@link Window}'s {@link GLCanvas},
	 * in display-relative coordinates.<br>
	 * This method is thread safe.
	 * 
	 * @return The absolute position of this {@link Window}'s {@link GLCanvas},
	 *         in display-relative coordinates */
	public Point getGLCanvasLocation() {
		return new Point(this.glx, this.gly);
	}
	
	/** Returns the absolute position of this {@link Window}'s {@link GLCanvas}'
	 * center, in display-relative coordinates.<br>
	 * This method is thread safe.
	 * 
	 * @return The absolute position of this {@link Window}'s {@link GLCanvas}'
	 *         center, in display-relative coordinates */
	public Point getGLCanvasCenter() {
		Point location = this.getGLCanvasLocation();
		location.x += this.glWidth / 2;
		location.y += this.glHeight / 2;
		return location;
	}
	
	/** Returns the absolute position and size of this {@link Window}'s
	 * {@link GLCanvas}, in display-relative coordinates.<br>
	 * This method is thread safe.
	 * 
	 * @return The absolute position and size of this {@link Window}, in
	 *         display-relative coordinates */
	public Rectangle getGLCanvasBounds() {
		return new Rectangle(this.glx, this.gly, this.glWidth, this.glHeight);
	}
	
	/** Returns whether or not this {@link Window} is currently the foreground
	 * window; meaning the end-user is currently using this Window.<br>
	 * This method is thread safe.
	 * 
	 * @return Whether or not this {@link Window} is currently the foreground
	 *         window. */
	public boolean isActive() {
		return this.shellActive;
	}
	
	/** Opens this window.<br>
	 * This method blocks until the window has been closed. */
	public void open() {
		if(this.display == null || this.display.isDisposed() || this.shell == null || this.shell.isDisposed()) {
			if(this.glThread != null) {
				this.glThread.stopRunning(true);
				this.resetFieldsOnClose();
			}
			this.createContents();
		}
		this.running = true;
		
		try {
			if(instance == this) {
				Mouse.setCursorCanvas(this.glCanvas);
			}
			
			this.show();
			this.shellHandle = SWTUtil.getHandle(this.shell);
			this.setGLCanvasSize(this.glTargetWidth, this.glTargetHeight);
			
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
			this.running = false;
			this.shell.dispose();
			SWTResourceManager.dispose();
			this.display.dispose();
		}
	}
	
	/** Marks this {@link Window} as visible and brings it to the front of the
	 * drawing order, displaying it to the end-user.<br>
	 * This method is thread-safe.
	 * 
	 * @return This Window
	 * @throws RejectedExecutionException Thrown if this method was called
	 *             outside of the display thread after this {@link Window} has
	 *             been closed. */
	public Window show() throws RejectedExecutionException {
		if(Thread.currentThread() != this.getWindowThread()) {
			if(this.display.isDisposed()) {
				throw new RejectedExecutionException("Display is disposed!");
			}
			this.display.asyncExec(() -> {
				this.show();
			});
			return this;
		}
		this.shell.open();
		this.shell.layout();
		this.shell.forceActive();
		this.glCanvas.forceFocus();
		return this;
	}
	
	/** Sets this {@link Window}'s visibility state to false, hiding it from the
	 * end-user.<br>
	 * This method is thread-safe.
	 * 
	 * @return This Window
	 * @throws RejectedExecutionException Thrown if this method was called
	 *             outside of the display thread after this {@link Window} has
	 *             been closed. */
	public Window hide() throws RejectedExecutionException {
		if(Thread.currentThread() != this.getWindowThread()) {
			if(this.display.isDisposed()) {
				throw new RejectedExecutionException("Display is disposed!");
			}
			this.display.asyncExec(() -> {
				this.hide();
			});
			return this;
		}
		this.shell.setVisible(false);
		return this;
	}
	
	/** Closes this {@link Window}, disposing of it.<br>
	 * This method is thread-safe. */
	public void close() {
		this.running = false;
	}
	
	//======================================================================================================================================
	
	/** Checks if the specified game is registered with this {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @param game The game to check
	 * @return Whether or not the specified game is registered with this
	 *         Window */
	public final boolean isGameRegistered(Game game) {
		if(game != null) {
			return this.games.contains(game);
		}
		return false;
	}
	
	/** Registers the specified game with this {@link Window}, making it
	 * available for selection in the main {@link Menu MenuBar}.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;This method will return <tt>false</tt> if the specified
	 * game was already registered beforehand.
	 * 
	 * @param game The game that will be registered with this Window
	 * @return Whether or not the game was just registered */
	public final boolean registerGame(Game game) {
		if(game != null) {
			final String name;
			try {
				name = game.getName();
			} catch(Throwable ex) {
				GLThread.handleRendererException(game, ex, "getName");
				return false;
			}
			
			boolean registeredAnywhere = false;
			if(!this.isGameRegistered(game)) {
				registeredAnywhere |= this.games.add(game);
			}
			registeredAnywhere |= Mouse.registerInputCallback(game);
			registeredAnywhere |= Keyboard.registerInputCallback(game);
			if(game instanceof MenuProvider && !this.menuProviders.contains((MenuProvider) game)) {
				registeredAnywhere |= this.menuProviders.add((MenuProvider) game);
			}
			if(this.display != null && !this.display.isDisposed()) {
				this.display.asyncExec(() -> {
					this.addRendererToCascadeMenu(game, name);
				});
			}
			return registeredAnywhere;
		}
		return false;
	}
	
	/** Unregisters the specified game from this {@link Window}, removing it
	 * from the selection of games within the main {@link Menu MenuBar}.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;This method will return <tt>false</tt> if the specified
	 * game was not registered beforehand.
	 * 
	 * @param game The game that will be unregistered from this Window
	 * @return Whether or not the game was just unregistered */
	public final boolean unregisterGame(Game game) {
		if(game != null) {
			boolean unregisteredAnywhere = false;
			if(this.isGameRegistered(game)) {
				while(this.games.remove(game)) {
					unregisteredAnywhere |= true;
				}
			}
			unregisteredAnywhere |= Mouse.unregisterInputCallback(game);
			unregisteredAnywhere |= Keyboard.unregisterInputCallback(game);
			if(game instanceof MenuProvider && this.menuProviders.contains((MenuProvider) game)) {
				while(this.menuProviders.remove((MenuProvider) game)) {
					unregisteredAnywhere |= true;
				}
			}
			if(this.display != null && !this.display.isDisposed()) {
				this.display.asyncExec(() -> {
					this.removeRendererFromCascadeMenu(game);
				});
			}
			return unregisteredAnywhere;
		}
		return false;
	}
	
	public final boolean isInputCallbackRegistered(InputCallback inputCallback) {
		if(inputCallback instanceof Game) {
			while(this.inputListeners.remove(inputCallback)) {
			}
			return this.isGameRegistered((Game) inputCallback);
		}
		if(inputCallback != null) {
			return this.inputListeners.contains(inputCallback);
		}
		return false;
	}
	
	public final boolean registerInputCallback(InputCallback inputCallback) {
		if(inputCallback instanceof Game) {
			while(this.inputListeners.remove(inputCallback)) {
			}
			return this.registerGame((Game) inputCallback);
		}
		if(inputCallback != null) {
			boolean registeredAnywhere = false;
			if(!this.isInputCallbackRegistered(inputCallback)) {
				registeredAnywhere |= this.inputListeners.add(inputCallback);
			}
			registeredAnywhere |= Mouse.registerInputCallback(inputCallback);
			registeredAnywhere |= Keyboard.registerInputCallback(inputCallback);
			return registeredAnywhere;
		}
		return false;
	}
	
	public final boolean unregisterInputCallback(InputCallback inputCallback) {
		if(inputCallback instanceof Game) {
			while(this.inputListeners.remove(inputCallback)) {
			}
			return this.unregisterGame((Game) inputCallback);
		}
		if(inputCallback != null) {
			boolean unregisteredAnywhere = false;
			if(this.isInputCallbackRegistered(inputCallback)) {
				while(this.inputListeners.remove(inputCallback)) {
					unregisteredAnywhere |= true;
				}
			}
			unregisteredAnywhere |= Mouse.unregisterInputCallback(inputCallback);
			unregisteredAnywhere |= Keyboard.unregisterInputCallback(inputCallback);
			return unregisteredAnywhere;
		}
		return false;
	}
	
	/** Attempts to have the {@link InputCallback listener} handle the exception
	 * it threw, or handles it and returns false if the listener failed to even
	 * do that.
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
		}
		return handled;
	}
	
	public final boolean isRendererRegistered(Renderer renderer) {
		if(renderer instanceof Game) {
			while(this.renderers.remove(renderer)) {
			}
			return this.isGameRegistered((Game) renderer);
		}
		if(renderer != null) {
			return this.renderers.contains(renderer);
		}
		return false;
	}
	
	public final boolean registerRenderer(Renderer renderer) {
		if(renderer instanceof Game) {
			while(this.renderers.remove(renderer)) {
			}
			return this.registerGame((Game) renderer);
		}
		if(renderer != null && !this.isRendererRegistered(renderer)) {
			final String name;
			try {
				name = renderer.getName();
			} catch(Throwable ex) {
				GLThread.handleRendererException(renderer, ex, "getName");
				return false;
			}
			
			boolean success = this.renderers.add(renderer);
			if(success && this.display != null && !this.display.isDisposed()) {
				this.display.asyncExec(() -> {
					this.addRendererToCascadeMenu(renderer, name);
				});
			}
			return success;
		}
		return false;
	}
	
	public final boolean unregisterRenderer(Renderer renderer) {
		if(renderer instanceof Game) {
			while(this.renderers.remove(renderer)) {
			}
			return this.unregisterGame((Game) renderer);
		}
		if(renderer != null && this.isRendererRegistered(renderer)) {
			while(this.renderers.remove(renderer)) {
			}
			if(this.display != null && !this.display.isDisposed()) {
				this.display.asyncExec(() -> {
					this.removeRendererFromCascadeMenu(renderer);
				});
			}
			return true;
		}
		return false;
	}
	
	public final boolean isMenuProviderRegistered(MenuProvider provider) {
		if(provider instanceof Renderer) {
			while(this.menuProviders.remove(provider)) {
			}
			return this.isRendererRegistered((Renderer) provider);
		}
		return provider == null ? false : this.menuProviders.contains(provider);
	}
	
	public final boolean registerMenuProvider(MenuProvider provider) {
		if(provider instanceof Renderer) {
			while(this.menuProviders.remove(provider)) {
			}
			return this.registerRenderer((Renderer) provider);
		}
		if(provider != null && !this.isMenuProviderRegistered(provider)) {
			return this.menuProviders.add(provider);
		}
		return false;
	}
	
	public final boolean unregisterMenuProvider(MenuProvider provider) {
		if(provider instanceof Renderer) {
			while(this.menuProviders.remove(provider)) {
			}
			return this.unregisterRenderer((Renderer) provider);
		}
		if(provider != null && this.isMenuProviderRegistered(provider)) {
			while(this.menuProviders.remove(provider)) {
			}
			return true;
		}
		return false;
	}
	
	/** Attempts to have the {@link MenuProvider provider} handle the exception
	 * it threw, or handles it and returns false if the provider failed to do
	 * even that.
	 * 
	 * @param provider The provider that threw an exception
	 * @param method The provider's method that threw the exception
	 * @param params The parameters that were passed to the provider's method
	 * @param ex The exception that was thrown
	 * @return Whether or not the provider was able to handle the exception */
	public static final boolean handleMenuProviderException(MenuProvider provider, Throwable ex, String method, Object... params) {
		String name = provider.getClass().getName();
		boolean handled = false;
		try {
			name = provider.getMenuName();
			handled = provider.handleException(ex, method, params);
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
			System.err.print(String.format("MenuProvider \"%s\" threw an exception while executing method %s(%s): ", name, method, parameters));
			ex.printStackTrace(System.err);
			System.err.flush();
			return true;
		}
		return handled;
	}
	
	/** Sets (and {@link Window#registerRenderer(Renderer) registers}) the
	 * renderer that this {@link Window}'s {@link GLThread} will attempt to use
	 * to display graphics.<br>
	 * <br>
	 * This method is thread safe; however it may block if this Window is
	 * currently running as it must wait for the GLThread to change the active
	 * renderer.<br>
	 * {@link Display#readAndDispatch()} will be called while this method blocks
	 * if the calling thread has an active {@link Display}.
	 * 
	 * @param renderer The {@link Renderer renderer} that the GLThread will
	 *            attempt to use to display graphics
	 * @return Whether or not the {@link GLThread} was able to begin using the
	 *         specified renderer. If <tt>false</tt> is returned, then in
	 *         addition to not being set as the active renderer, the specified
	 *         renderer is not registered. */
	public final boolean setActiveRenderer(Renderer renderer) {
		if(this.display.isDisposed()) {
			throw new IllegalStateException("Display is disposed!");
		}
		if(this.glThread.setRenderer(renderer)) {
			//if(renderer instanceof MenuProvider) {
			this.display.asyncExec(() -> {
				if(this.shell.getMenuBar() != null) {
					this.destroyMenus();
					this.createMenus();
				}
			});
			//}
			if(renderer != null) {
				this.registerRenderer(renderer);
			}
			return true;
		}
		return false;
	}
	
	/** Returns the renderer that this {@link Window}'s {@link GLThread} is
	 * currently using to display graphics.<br>
	 * This method is thread safe.
	 * 
	 * @return The {@link Renderer renderer} that this Window's GLThread is
	 *         currently using to display graphics */
	public final Renderer getActiveRenderer() {
		return this.glThread.getRenderer();
	}
	
	/** Returns a list of all of this {@link Window}'s available renderers, some
	 * of which may or may not be instances of {@link Game}.<br>
	 * This method is thread-safe.
	 * 
	 * @return A list of all of this Window's available renderers */
	public final List<Renderer> getAvailableRenderers() {
		List<Renderer> list = new ArrayList<>();
		list.addAll(this.renderers);
		for(Game game : this.games) {
			if(!list.contains(game)) {
				list.add(game);
			}
		}
		Renderer activeRenderer = this.getActiveRenderer();
		if(activeRenderer != null && !list.contains(activeRenderer)) {
			list.add(activeRenderer);
		}
		return list;
	}
	
	/** Returns the index of this {@link Window}'s currently active
	 * {@link Renderer renderer} within the list returned by
	 * {@link Window#getAvailableRenderers()}.<br>
	 * This method is thread-safe.
	 * 
	 * @return The index of this Window's currently active renderer within the
	 *         list returned by {@link Window#getAvailableRenderers()} */
	public final int getActiveRendererIndex() {
		final Renderer activeRenderer = this.getActiveRenderer();
		if(activeRenderer != null) {
			Collection<Renderer> list = this.getAvailableRenderers();
			Iterator<Renderer> it = list.iterator();
			for(int i = 0; it.hasNext(); i++) {
				Renderer renderer = it.next();
				if(renderer == activeRenderer) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/** Returns the renderer after the currently active renderer in the list
	 * returned by {@link Window#getAvailableRenderers()}.<br>
	 * This method is thread-safe.
	 * 
	 * @param wrapAround Whether or not the first renderer in the list should be
	 *            returned if the currently active renderer happens to be the
	 *            last one in the list
	 * @return The renderer after the currently active renderer in the list
	 *         returned by {@link Window#getAvailableRenderers()} */
	public final Renderer getNextRenderer(boolean wrapAround) {
		final Renderer activeRenderer = this.getActiveRenderer();
		if(activeRenderer != null) {
			boolean thisOne = false;
			Renderer firstRenderer = null;
			for(Renderer renderer : this.getAvailableRenderers()) {
				if(firstRenderer == null) {
					firstRenderer = renderer;
				}
				if(thisOne) {
					return renderer;
				}
				if(renderer == activeRenderer) {
					thisOne = true;
					continue;
				}
			}
			if(thisOne && wrapAround) {
				return firstRenderer;
			}
		}
		return null;
	}
	
	/** Returns the renderer after the currently active renderer in the list
	 * returned by {@link Window#getAvailableRenderers()}.<br>
	 * This method is thread-safe.
	 * 
	 * @return The renderer after the currently active renderer in the list
	 *         returned by {@link Window#getAvailableRenderers()} */
	public final Renderer getNextRenderer() {
		return this.getNextRenderer(true);
	}
	
	/** Returns the renderer before the currently active renderer in the list
	 * returned by {@link Window#getAvailableRenderers()}.<br>
	 * This method is thread-safe.
	 * 
	 * @param wrapAround Whether or not the last renderer in the list should be
	 *            returned if the currently active renderer happens to be the
	 *            first one in the list
	 * @return The renderer before the currently active renderer in the list
	 *         returned by {@link Window#getAvailableRenderers()} */
	public final Renderer getPreviousRenderer(boolean wrapAround) {
		final Renderer activeRenderer = this.getActiveRenderer();
		if(activeRenderer != null) {
			List<Renderer> list = this.getAvailableRenderers();
			boolean thisOne = false;
			Renderer lastRenderer = null;
			for(int i = list.size() - 1; i >= 0; i--) {
				Renderer renderer = list.get(i);
				if(thisOne) {
					return renderer;
				}
				if(lastRenderer == null) {
					lastRenderer = renderer;
				}
				if(renderer == activeRenderer) {
					thisOne = true;
					continue;
				}
			}
			if(thisOne && wrapAround) {
				return lastRenderer;
			}
		}
		return null;
	}
	
	/** Returns the renderer before the currently active renderer in the list
	 * returned by {@link Window#getAvailableRenderers()}.<br>
	 * This method is thread-safe.
	 * 
	 * @return The renderer before the currently active renderer in the list
	 *         returned by {@link Window#getAvailableRenderers()} */
	public final Renderer getPreviousRenderer() {
		return this.getPreviousRenderer(true);
	}
	
}
