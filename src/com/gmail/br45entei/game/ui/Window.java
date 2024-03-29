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
import com.gmail.br45entei.audio.SoundManager;
import com.gmail.br45entei.game.Game;
import com.gmail.br45entei.game.graphics.GLThread;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.ControllerManager;
import com.gmail.br45entei.game.input.InputCallback;
import com.gmail.br45entei.game.input.InputCallback.InputLogger;
import com.gmail.br45entei.game.input.Keyboard;
import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.game.ui.swt.RendererMenuItem;
import com.gmail.br45entei.lwjgl.natives.LWJGL_Natives;
import com.gmail.br45entei.thread.FrequencyTimer;
import com.gmail.br45entei.thread.ScreenshotHelper;
import com.gmail.br45entei.thread.ThreadType;
import com.gmail.br45entei.thread.VideoHelper;
import com.gmail.br45entei.util.Architecture;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.FileUtil;
import com.gmail.br45entei.util.Platform;
import com.gmail.br45entei.util.SWTUtil;
import com.gmail.br45entei.util.StringUtil;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.Beans;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
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
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class Window {// TODO Implement InputCallback.isModal() within the Mouse class
	
	public static final boolean DEVELOPMENT_ENVIRONMENT;
	
	static {
		try {
			DEVELOPMENT_ENVIRONMENT = !new File(Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).isFile();
		} catch(Throwable ex) {
			throw new RuntimeException(ex);
		}
		
		//CodeUtil.setProperty("org.lwjgl.util.Debug", "true");
		CodeUtil.setProperty("org.lwjgl.util.NoChecks", "false");
		
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
	
	private static final ConcurrentLinkedDeque<Window> instances = new ConcurrentLinkedDeque<>();
	private static volatile Window instance = null;
	
	/** @return The {@link Window} that is running on the current thread, or
	 *         <tt><b>null</b></tt> if no Window was found */
	public static final Window getCurrent() {
		final Thread thread = Thread.currentThread();
		for(Window window : instances) {
			GLThread glThread = window.glThread;
			if(glThread == thread) {
				return window;
			}
			if(window.controllerPollThread == thread) {
				return window;
			}
			Display display = window.display;
			if(display != null && !display.isDisposed() && display.getThread() == thread) {
				return window.shouldContinueRunning() ? window : null;
			}
		}
		return null;
	}
	
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
	 * @return Whether or not the specified shell is active
	 * @throws SWTException
	 *             <ul>
	 *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *             thread that created the receiver</li>
	 *             <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *             disposed</li>
	 *             </ul>
	 */
	public static final boolean isShellActive(Shell shell) {
		boolean shellActive = shell.getDisplay().getActiveShell() == shell && shell.isVisible();
		/*long shellHandle = SWTUtil.getHandle(shell);
		switch(Platform.get()) {
		case WINDOWS: {
			shellActive = shell.isVisible() && org.eclipse.swt.internal.win32.OS.GetForegroundWindow() == shellHandle;
			break;
		}
		case LINUX:
		case MACOSX:
		case UNKNOWN:
		default: {
			break;
		}
		}*/
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
	
	protected volatile String title, effectiveTitle;
	protected final String originalTitle;
	protected volatile int shellX = 0, shellY = 0;
	protected volatile int shellWidth = 0, shellHeight = 0;
	protected volatile int glx = 0, gly = 0;
	protected volatile int glTargetWidth = 800, glTargetHeight = 600;
	protected volatile int glWidth = this.glTargetWidth;
	protected volatile int glHeight = this.glTargetHeight;
	protected volatile double framerate = 60.0D;
	protected static volatile int defaultRefreshRate = 60;
	
	protected volatile long nanoTime = System.nanoTime();
	protected volatile long lastFrameTime = this.nanoTime;
	protected volatile double ΔTime = (((this.nanoTime = System.nanoTime()) - this.lastFrameTime) + 0.0D) / 1000000000.0D;
	
	protected volatile boolean running = false, shellActive = false;
	protected volatile boolean shellVisible = false;
	protected volatile boolean isFullscreen = false;
	
	protected volatile long lastMenuInteraction = 0L;
	
	private final FrequencyTimer timer = new FrequencyTimer(60.0, 1000.0);
	
	protected volatile Display display;
	protected volatile Shell shell;
	protected volatile long shellHandle;
	protected volatile int shellStyle;
	protected volatile boolean appendTitleIfUIForced = true;
	protected volatile boolean alwaysOnTopEnabled = true;
	protected volatile Boolean alwaysOnTopFullscreenOrWindowed = Boolean.TRUE;//TRUE: Fullscreen; FALSE: Windowed; null: (Always)
	protected volatile boolean hasOpenedYet = false;
	protected volatile boolean closeWindowOnEscape = true;
	
	protected volatile String[] lastSetIconImages = null;
	
	protected volatile GLData data;
	protected volatile GLCanvas glCanvas;
	protected volatile GLThread glThread;
	protected volatile Thread fpsLogThread;
	protected volatile ControllerManager controllerManager;
	protected volatile boolean pollControllersAsynchronously = true;
	protected volatile Thread controllerPollThread = null;
	protected volatile SoundManager soundManager = null;
	
	protected volatile boolean updateMenuBar = false;
	protected volatile MenuItem mntmVerticalSync;
	protected volatile MenuItem mntmRenderers;
	protected volatile MenuItem mntmNoRenderer;
	protected volatile MenuItem mntmAllowControllerBackground;
	protected volatile MenuItem mntmRendererOptions;
	protected volatile MenuItem mntmUpdateTitleIf;
	
	protected volatile InputCallback uiCallback;
	protected volatile Renderer activeRendererToSetOnStartup = null;
	protected volatile boolean enableRendererSwitching = true;
	
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
		this.fpsLogThread = null;
		this.controllerManager = null;
		this.controllerPollThread = null;
		this.soundManager = null;
		
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
		return defaultRefreshRate;
	}
	
	/** Returns a new {@link GLData} with the default settings for creating a
	 * {@link Window}.<br>
	 * DoubleBuffer is enabled, the swap interval is set to <tt>1,</tt> the
	 * OpenGL version is set to <tt>3.3</tt>, and the context is set to be
	 * forward compatible.
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
	
	/** Returns a new {@link GLData} with the specified settings for creating a
	 * {@link Window}.<br>
	 * DoubleBuffer is enabled by default, and the swap interval is set to
	 * <tt>1</tt> (vsync enabled).
	 *
	 * @param majorVersion The major GL context version to use. Use <tt>0</tt>
	 *            for "not specified".
	 * @param minorVersion The minor GL context version to use. If
	 *            <tt>majorVersion</tt> is <tt>0</tt> this parameter is unused.
	 * @param forwardCompatible Whether a forward-compatible context should be
	 *            created. This has only an effect when
	 *            (<tt>majorVersion</tt>.<tt>minorVersion</tt>) is at least
	 *            <tt>3</tt>.<tt>2</tt>.
	 * @return A new {@link GLData} with the specified settings for creating a
	 *         {@link Window}. */
	public static final GLData createGLData(int majorVersion, int minorVersion, boolean forwardCompatible) {
		GLData data = new GLData();
		data.doubleBuffer = true;
		data.swapInterval = Integer.valueOf(1);
		data.majorVersion = majorVersion;
		data.minorVersion = minorVersion;
		data.forwardCompatible = forwardCompatible;
		return data;
	}
	
	/** Returns a new {@link GLData} with the specified settings for creating a
	 * {@link Window}.<br>
	 * DoubleBuffer is enabled by default.
	 *
	 * @param swapInterval The minimum number of video frames that are displayed
	 *            before a buffer swap will occur.<br>
	 *            A video frame period is the time required by the monitor to
	 *            display a full frame of video data. In the case of an
	 *            interlaced monitor, this is typically the time required to
	 *            display both the even and odd fields of a frame of video data.
	 *            An interval set to a value of <tt>2</tt> means that the color
	 *            buffers will be swapped at most every other video frame. If
	 *            <tt>swapInterval</tt> is set to a value of <tt>0</tt>, buffer
	 *            swaps are not synchronized to a video frame. The interval
	 *            value is silently clamped to the maximum
	 *            implementation-dependent value supported before being stored.
	 *            The swap interval is not part of the render context state. It
	 *            cannot be pushed or popped. The default swap interval is
	 *            <tt>1</tt>.
	 * @param majorVersion The major GL context version to use. Use <tt>0</tt>
	 *            for "not specified".
	 * @param minorVersion The minor GL context version to use. If
	 *            <tt>majorVersion</tt> is <tt>0</tt> this parameter is unused.
	 * @param forwardCompatible Whether a forward-compatible context should be
	 *            created. This has only an effect when
	 *            (<tt>majorVersion</tt>.<tt>minorVersion</tt>) is at least
	 *            <tt>3</tt>.<tt>2</tt>.
	 * @return A new {@link GLData} with the specified settings for creating a
	 *         {@link Window}. */
	public static final GLData createGLData(Integer swapInterval, int majorVersion, int minorVersion, boolean forwardCompatible) {
		GLData data = new GLData();
		data.doubleBuffer = true;
		data.swapInterval = swapInterval;
		data.majorVersion = majorVersion;
		data.minorVersion = minorVersion;
		data.forwardCompatible = forwardCompatible;
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
	 * @param pollControllersAsynchronously Whether or not the
	 *            {@link ControllerManager} should poll the controllers on a
	 *            dedicated thread rather than have this Window's display thread
	 *            do it
	 * @param renderer The renderer that this Window's {@link GLThread} will
	 *            attempt to use to display graphics */
	public Window(String title, int width, int height, double framerate, GLData data, boolean pollControllersAsynchronously, Renderer renderer) {
		this(title, width, height, framerate, data, renderer);
		this.pollControllersAsynchronously = pollControllersAsynchronously;
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
		DPIUtil.setUseCairoAutoScale(true);
		this.title = title == null ? "SWT-LWJGL3 Framework" : title;
		this.originalTitle = title;
		this.glTargetWidth = width;
		this.glTargetHeight = height;
		this.framerate = framerate != framerate || Double.isInfinite(framerate) ? Window.getDefaultRefreshRate() : framerate;
		this.data = data == null ? Window.createDefaultGLData() : data;
		
		this.uiCallback = new UICallback(this);
		
		this.createContents();
		
		this.setActiveRenderer(renderer);
		
		//this.controllerManager.pollState();
		
		instances.add(this);
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
	
	/** Creates a new Window with the specified {@link GLData}, the specified
	 * viewport size, and the framerate is set to
	 * {@link #getDefaultRefreshRate()}.<br>
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
	 * @param data The {@link GLData} that the {@link GLThread} will use when
	 *            creating the OpenGL context */
	public Window(String title, int width, int height, GLData data) {
		this(title, width, height, Window.getDefaultRefreshRate(), data);
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
		this(null, width, height, data);
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
	
	/** Sets this {@link Window}'s icon to the specified {@link Image}(s).<br>
	 * This method is thread-safe.
	 * 
	 * @param images The Image(s) to set
	 * @return This Window */
	public Window setIconImages(Image... images) {
		if(Thread.currentThread() != this.getWindowThread()) {
			if(this.display == null || this.display.isDisposed()) {
				throw new RejectedExecutionException("Display is disposed!");
			}
			this.display.asyncExec(() -> {
				this.setIconImages(images);
			});
			return this;
		}
		this.shell.setImages(images);
		return this;
	}
	
	/** Sets this {@link Window}'s icon to the specified {@link Image}(s).<br>
	 * This method is thread-safe.
	 * 
	 * @param resourcePaths The path(s) to the Image(s) to set
	 *            (<b>Note:</b>&nbsp;each path must lead with a
	 *            forward-slash['/']!)
	 * @return This Window */
	public Window setIconImages(String... resourcePaths) {
		if(Thread.currentThread() != this.getWindowThread()) {
			if(this.display == null || this.display.isDisposed()) {
				throw new RejectedExecutionException("Display is disposed!");
			}
			this.display.asyncExec(() -> {
				this.setIconImages(resourcePaths);
			});
			return this;
		}
		this.lastSetIconImages = resourcePaths;
		List<Image> list = new ArrayList<>();
		for(String resourcePath : resourcePaths) {
			boolean validPath = false;
			try(InputStream in = Window.class.getResourceAsStream(resourcePath)) {
				validPath = in != null;
			} catch(IOException ex) {
				validPath = false;
				ex.printStackTrace();
			} catch(NullPointerException ex) {
				validPath = false;
			}
			list.add(validPath ? SWTResourceManager.getImage(Window.class, resourcePath) : SWTResourceManager.getMissingImage());
		}
		return this.setIconImages(list.toArray(new Image[list.size()]));
	}
	
	private void createContents() {
		this.display = Display.getDefault();
		if(this.display.getThread() != Thread.currentThread()) {
			this.display = new Display();
		}
		this.shell = new Shell(this.display, SWT.SHELL_TRIM | SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);// (DOUBLE_BUFFERED is used here for when the shell is in fullscreen mode)
		this.shell.setText(this.title);
		String[] images = this.lastSetIconImages;
		if(images == null) {
			this.shell.setImages(SWTUtil.getTitleImages());
		} else {
			this.setIconImages(images);
		}
		this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				Window.this.close();//Window.this.running = false;
			}
		});
		this.shell.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				if(Window.this.glThread == null || !Window.this.glThread.isRecording()) {
					//synchronized(Window.this.glThread) {
					Window.this.glCanvas.setBounds(Window.this.shell.getClientArea());
					//}
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
		int style = SWT.DOUBLE_BUFFERED | /* SWT.NO_BACKGROUND | SWT.TRANSPARENT |*/ SWT.NO_REDRAW_RESIZE;
		try {
			this.glCanvas = new GLCanvas(this.shell, style, this.data);
		} catch(SWTException ex) {
			if(ex.getMessage() != null && ex.getMessage().startsWith("Swap interval requested but ") && ex.getMessage().endsWith(" is unavailable")) {
				this.data.swapInterval = null;
				this.glCanvas = new GLCanvas(this.shell, style, this.data);
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
		this.shell.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Window.this.glCanvas.forceFocus();
			}
		});
		
		if(Beans.isDesignTime()) {
			this.shell.setSize(this.glTargetWidth, this.glTargetHeight);
			Rectangle clientArea = this.shell.getClientArea();
			Point size = this.shell.getSize();
			int xDiff = size.x - clientArea.width, yDiff = size.y - clientArea.height;
			this.shell.setSize(this.glTargetWidth + xDiff, this.glTargetHeight + yDiff);
			this.glCanvas.setBounds(this.shell.getClientArea());
		}
		
		this.glThread = new GLThread(this.glCanvas);
		this.fpsLogThread = new Thread(() -> {
			final GLThread glThread = this.glThread;
			while(glThread != null && glThread.isAlive()) {
				glThread.printFPSLog();
				CodeUtil.sleep(40L);
			}
		}, "FPS Log Printer Thread");
		this.fpsLogThread.setDaemon(true);
		this.controllerManager = new ControllerManager();
		/*this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				Window.this.controllerManager.setWindowActive(true);
			}
			
			@Override
			public void shellDeactivated(ShellEvent e) {
				Window.this.controllerManager.setWindowActive(false);
			}
		});*/
		
		this.soundManager = new SoundManager();// SoundManager is not a daemon thread; however it will detect when the thread that started it has died, and then it will shut itself down.
		
		this.createMenus();
		
		if(instance == null || instance.shell == null || instance.shell.isDisposed()) {
			instance = this;
		}
		
		this.registerInputCallback(this.uiCallback);
	}
	
	/** Returns this {@link Window}'s current target FPS (frames per
	 * second).<br>
	 * This method is thread-safe.
	 * 
	 * @return The current target FPS */
	public int getRefreshRate() {
		return this.glThread.isVsyncEnabled() ? Window.getDefaultRefreshRate() : Long.valueOf(Math.round(Math.ceil(this.glThread.getTargetFPS()))).intValue();
	}
	
	/** Returns the {@link Shell} that this {@link Window} is maintaining.<br>
	 * While this method is thread-safe, the returned shell should only be used
	 * by this Window's display thread.
	 * 
	 * @return The Shell that this Window is maintaining */
	public Shell getShell() {
		return this.shell;
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
			if(this.display != null && !this.display.isDisposed()) {
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
			SWTUtil.setAlwaysOnTop(this.shell, this.alwaysOnTopEnabled && (this.alwaysOnTopFullscreenOrWindowed == null || this.alwaysOnTopFullscreenOrWindowed.booleanValue() == fullScreen));
			this.shell.setFullScreen(fullScreen);
			if(!fullScreen) {
				this.createMenus();
			}
			this.shell.redraw();
			this.shell.update();
			this.shell.forceActive();
			//this.shell.forceFocus();
			this.glCanvas.forceFocus();
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
				if(this.display != null && !this.display.isDisposed()) {
					this.display.asyncExec(() -> {
						this.toggleFullscreen(false);
					});
				}
				return this.isFullscreen;
			}
			final Boolean[] rtrn = {null};
			if(this.display != null && !this.display.isDisposed()) {
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
	
	/** Returns whether or not vsync (vertical sync) is enabled for this
	 * {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not vsync is enabled for this Window */
	public boolean isVsyncEnabled() {
		return this.getGLThread().getRefreshRate() == Window.getDefaultRefreshRate();
	}
	
	private volatile double lastNonDefaultFramerate = 0;
	
	/** Enables or disabled vsync (vertical sync) for this {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @param vsync Whether or not vsync will be enabled
	 * @return This Window */
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
		if(this.display == null || this.display.isDisposed()) {
			throw new RejectedExecutionException("Display is disposed!");
		}
		this.display.asyncExec(code);
		return this;
	}
	
	/** Toggles the vsync (vertical sync) state for this {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @return This Window */
	public Window toggleVsyncEnabled() {
		return this.setVSyncEnabled(!this.isVsyncEnabled());
	}
	
	/** Returns whether or not this {@link Window} is currently always on top of
	 * other windows.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this Window is currently always on top of other
	 *         windows
	 * @see #setAlwaysOnTop(boolean, Boolean) */
	public boolean isCurrentlyAlwaysOnTop() {
		return (this.shellStyle & SWT.ON_TOP) != 0;
	}
	
	/** Sets whether or not this {@link Window} is always on top, and when.<br>
	 * This method is thread-safe.
	 * 
	 * @param alwaysOnTop Whether or not this Window is always on top of other
	 *            windows
	 * @param fullscreenOrWindowedOnly If {@link Boolean#TRUE TRUE}, this Window
	 *            is always on top while in fullscreen mode; if
	 *            {@link Boolean#FALSE FALSE}, this Window is always on top
	 *            while in windowed mode; if <tt><b>null</b></tt>, this Window
	 *            is just always on top, period.
	 * @return This Window */
	public Window setAlwaysOnTop(boolean alwaysOnTop, Boolean fullscreenOrWindowedOnly) {
		this.alwaysOnTopEnabled = alwaysOnTop;
		this.alwaysOnTopFullscreenOrWindowed = this.alwaysOnTopEnabled && fullscreenOrWindowedOnly != null ? Boolean.valueOf(fullscreenOrWindowedOnly.booleanValue()) : null;
		return this;
	}
	
	/** Sets whether or not this {@link Window} is always on top.<br>
	 * This method is thread-safe.
	 * 
	 * @param alwaysOnTop Whether or not this Window is always on top of other
	 *            windows
	 * @return This Window */
	public Window setAlwaysOnTop(boolean alwaysOnTop) {
		return this.setAlwaysOnTop(alwaysOnTop, null);
	}
	
	public boolean shouldCloseWindowOnEscape() {
		return this.closeWindowOnEscape;
	}
	
	public Window setCloseWindowOnEscape(boolean closeWindowOnEscape) {
		this.closeWindowOnEscape = closeWindowOnEscape;
		return this;
	}
	
	@SuppressWarnings("unused")
	protected synchronized void createMenus() {
		this.updateMenuBar = false;
		
		//==[Main MenuBar]=============================================
		
		Menu menu = new Menu(this.shell, SWT.BAR);
		this.shell.setMenuBar(menu);
		
		MenuItem mntmfile = new MenuItem(menu, SWT.CASCADE);
		mntmfile.setText("&File");
		
		Menu menu_1 = new Menu(mntmfile);
		mntmfile.setMenu(menu_1);
		
		MenuItem mntmOpenScreenshotsFolder = new MenuItem(menu_1, SWT.NONE);
		mntmOpenScreenshotsFolder.setText("Open &Screenshots Folder");
		mntmOpenScreenshotsFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileUtil.showFileToUser(ScreenshotHelper.getSaveFolder());
			}
		});
		
		MenuItem mntmOpenVideosFolder = new MenuItem(menu_1, SWT.NONE);
		mntmOpenVideosFolder.setText("Open &Videos Folder");
		mntmOpenVideosFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileUtil.showFileToUser(VideoHelper.getSaveFolder());
			}
		});
		
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
		
		if(this.enableRendererSwitching) {
			this.mntmRenderers = new MenuItem(menu_2, SWT.CASCADE);
			this.mntmRenderers.setText("Applications\t(Alt+Left | Alt+Right)");
			
			Menu menu_3 = new Menu(this.mntmRenderers);
			this.mntmRenderers.setMenu(menu_3);
			
			this.mntmNoRenderer = new MenuItem(menu_3, SWT.RADIO);
			this.mntmNoRenderer.setText("<None>");
			this.mntmNoRenderer.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MenuItem item = Window.this.mntmNoRenderer;
					boolean selection = Window.this.setActiveRenderer(null);
					if(item != null && !item.isDisposed()) {
						item.setSelection(selection);
					}
				}
			});
			
			new MenuItem(menu_2, SWT.SEPARATOR);
		} else {
			this.mntmRenderers = null;
			this.mntmNoRenderer = null;
		}
		
		final MenuItem mntmFullscreen = new MenuItem(menu_2, SWT.CHECK);
		mntmFullscreen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.toggleFullscreen();
			}
		});
		mntmFullscreen.setText("Fullscreen\tF11");
		
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
						if(!mntmStartRecording.isDisposed()) {
							mntmStartRecording.setEnabled(!Window.this.glThread.isRecordingStartingUp() && !Window.this.glThread.isRecordingFinishingUp());
							mntmStartRecording.setSelection(Window.this.glThread.isRecording());
							mntmStartRecording.setText(mntmStartRecording.getSelection() ? "Stop Recording\tShift+F2" : (Window.this.glThread.isRecordingStartingUp() ? "Recording starting up..." : "Start Recording\tShift+F2"));
						}
					}
					if(!Window.this.mntmVerticalSync.isDisposed()) {
						Window.this.mntmVerticalSync.setSelection(Window.this.getGLThread().getRefreshRate() == Window.getDefaultRefreshRate());
					}
					if(!mntmFullscreen.isDisposed()) {
						mntmFullscreen.setSelection(Window.this.isFullscreen());// This will probably always return false (because there shouldn't be a menuBar in fullscreen mode), but just in case ...
					}
					
				}
			}
		});
		
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
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		MenuItem mntmAlwaysOnTop = new MenuItem(menu_2, SWT.CASCADE);
		mntmAlwaysOnTop.setText("Always On Top");
		
		Menu menu_4 = new Menu(mntmAlwaysOnTop);
		mntmAlwaysOnTop.setMenu(menu_4);
		
		MenuItem mntmDisabled = new MenuItem(menu_4, SWT.RADIO);
		mntmDisabled.setText("Disabled");
		mntmDisabled.setSelection(!this.alwaysOnTopEnabled);
		mntmDisabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.alwaysOnTopEnabled = false;
				Window.this.alwaysOnTopFullscreenOrWindowed = null;
			}
		});
		
		MenuItem mntmEnabledfullscreen = new MenuItem(menu_4, SWT.RADIO);
		mntmEnabledfullscreen.setSelection(this.alwaysOnTopEnabled && this.alwaysOnTopFullscreenOrWindowed == Boolean.TRUE);
		mntmEnabledfullscreen.setText("Enabled (Fullscreen Only)");
		mntmEnabledfullscreen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.alwaysOnTopEnabled = true;
				Window.this.alwaysOnTopFullscreenOrWindowed = Boolean.TRUE;
			}
		});
		menu_4.setDefaultItem(mntmEnabledfullscreen);
		
		MenuItem mntmEnabledwindowedOnly = new MenuItem(menu_4, SWT.RADIO);
		mntmEnabledwindowedOnly.setSelection(this.alwaysOnTopEnabled && this.alwaysOnTopFullscreenOrWindowed == Boolean.FALSE);
		mntmEnabledwindowedOnly.setText("Enabled (Windowed Only)");
		mntmEnabledwindowedOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.alwaysOnTopEnabled = true;
				Window.this.alwaysOnTopFullscreenOrWindowed = Boolean.FALSE;
			}
		});
		
		MenuItem mntmEnabledalways = new MenuItem(menu_4, SWT.RADIO);
		mntmEnabledalways.setSelection(this.alwaysOnTopEnabled && this.alwaysOnTopFullscreenOrWindowed == null);
		mntmEnabledalways.setText("Enabled (Always)");
		mntmEnabledalways.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.alwaysOnTopEnabled = true;
				Window.this.alwaysOnTopFullscreenOrWindowed = null;
			}
		});
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		this.mntmUpdateTitleIf = new MenuItem(menu_2, SWT.CHECK);
		this.mntmUpdateTitleIf.setToolTipText("When checked, this window's title will be suffixed with either \"(Mouse Captured)\", \"(Always On Top)\", or a combination of both, but only when such actions are currently being performed.");
		this.mntmUpdateTitleIf.setText("Append title when UI is forced");
		this.mntmUpdateTitleIf.setSelection(this.appendTitleIfUIForced);
		this.mntmUpdateTitleIf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.appendTitleIfUIForced = Window.this.mntmUpdateTitleIf.getSelection();
			}
		});
		
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
		
		MenuItem mntmInput = new MenuItem(menu, SWT.CASCADE);
		mntmInput.setText("Input");
		
		Menu menu_5 = new Menu(mntmInput);
		mntmInput.setMenu(menu_5);
		
		this.mntmAllowControllerBackground = new MenuItem(menu_5, SWT.CHECK);
		this.mntmAllowControllerBackground.setText("Allow Controller Background Input");
		this.mntmAllowControllerBackground.setToolTipText("When checked, controllers may still be used even when this window is not the foreground window.\r\nUseful for allowing multiple people to use one computer at the same time :)");
		this.mntmAllowControllerBackground.setSelection(this.controllerManager.isBackgroundInputAllowed());
		this.mntmAllowControllerBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Window.this.controllerManager.setAllowBackgroundInput(Window.this.mntmAllowControllerBackground.getSelection());
			}
		});
		
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
		if(justCreatedPopupMenu) {
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
				renderer = null;
				item.setText(name);
				item.setSelection(Window.this.getActiveRenderer() == item.getRenderer());
				final String _name = name;
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// SWT dispatches a widgetSelected event for widgets in a radio group differently than
						// it does for check-boxes and buttons; when one is enabled, its event is fired first,
						// then the previously enabled one's event is fired!
						// For us, this means that the new renderer is set active, and then the old renderer is
						// set active again! To prevent this, we just check to see if the item is currently
						// enabled before continuing.
						if(!item.getSelection()) {
							return;
						}
						boolean success = Window.this.setActiveRenderer(item.getRenderer());
						if(!success) {
							System.err.println(String.format("Failed to set the active renderer to \"%s\"!", _name));
						} else if(Window.this.getActiveRenderer() != item.getRenderer()) {
							new Throwable(String.format("The method setActiveRenderer reported success, but the active renderer is not \"%s\"!", _name)).printStackTrace();
						} else {
							System.out.println(String.format("Successfully set the active renderer to \"%s\".", _name));
						}
					}
				});
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
		// Begin of fix for main window thread constantly blocked by synchronization of GLThread
		Renderer renderer = this.glThread.getRenderer();
		if(renderer instanceof MenuProvider) {
			MenuProvider provider = (MenuProvider) renderer;
			boolean providesPopupMenu = true;
			try {
				providesPopupMenu = provider.providesPopupMenu();
			} catch(Throwable ex) {
				if(!Window.handleMenuProviderException(provider, ex, "providesPopupMenu")) {
					this.glThread.setRenderer(null);
					this.mntmRendererOptions.dispose();
					this.mntmRendererOptions = null;
					return;
				}
			}
			if(!providesPopupMenu) {
				return;
			}
		}
		// End of fix for main window thread constantly blocked by synchronization of GLThread
		
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
				//Renderer renderer = this.glThread.getRenderer();
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
						//popupMenu.dispose();
						//return;
					}
				}
				
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
				
				addArmListener[0].apply(popupMenu);
			}
		}
	}
	
	protected void destroyGLCanvasPopupMenu() {
		Menu menu = this.glCanvas.getMenu();
		if(menu != null) {
			synchronized(this.glThread) {
				this.glCanvas.setMenu(null);
				
				final Renderer renderer = this.glThread.getRenderer();
				MenuProvider provider = renderer instanceof MenuProvider ? (MenuProvider) renderer : null;
				if(provider != null) {
					boolean providesPopupMenu = true;
					
					try {
						providesPopupMenu = provider.providesPopupMenu();
					} catch(Throwable ex) {
						if(!Window.handleMenuProviderException(provider, ex, "providesPopupMenu")) {
							this.glThread.setRenderer(null);
							this.mntmRendererOptions.dispose();
							this.mntmRendererOptions = null;
							return;
						}
					}
					
					if(providesPopupMenu) {
						try {
							provider.onPopupMenuDeletion(menu);
						} catch(Throwable ex) {
							if(!Window.handleMenuProviderException(provider, ex, "onPopupMenuDeletion", menu)) {
								this.glThread.setRenderer(null);
								provider = null;
							}
						}
					}
				}
				
				menu.dispose();
			}
		}
	}
	
	private void _destroyMenuBar() {
		Menu menu = this.shell.getMenuBar();
		if(menu != null) {
			this.shell.setMenuBar(null);
			menu.dispose();
			this.mntmVerticalSync = this.mntmRenderers = this.mntmNoRenderer = this.mntmRendererOptions = this.mntmUpdateTitleIf = null;
		}
	}
	
	protected boolean onMenuBarDeletion() {
		if(this.shell.getMenuBar() == null) {
			return false;
		}
		synchronized(this.glThread) {
			final Renderer renderer = this.glThread.getRenderer();
			MenuProvider provider = renderer instanceof MenuProvider ? (MenuProvider) renderer : null;
			
			Menu menu = this.shell.getMenuBar();
			if(menu != null) {
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
			}
		}
		return true;
	}
	
	protected void destroyMenus() {
		if(this.onMenuBarDeletion()) {
			synchronized(this.glThread) {
				this._destroyMenuBar();
			}
		}
		this.destroyGLCanvasPopupMenu();
	}
	
	private final void updateFrameTime() {
		this.lastFrameTime = this.nanoTime;
		this.nanoTime = System.nanoTime();
		this.ΔTime = ((this.nanoTime - this.lastFrameTime) + 0.0D) / 1000000000.0D;
	}
	
	/** Returns the Δ (delta) time of the current frame from the last.<br>
	 * Returned values are in microseconds (milliseconds divided by 1000).<br>
	 * For a framerate of <tt>60.0</tt>, values are typically around
	 * <tt>0.01666670</tt>.
	 * 
	 * @return The Δ time of the current frame from the last
	 * @see InputCallback#input(double) */
	public final double getDeltaTime() {
		return this.ΔTime;
	}
	
	/** Polls the {@link Mouse#poll() Mouse} and {@link Keyboard#poll()
	 * Keyboard}, and then calls {@link InputCallback#input(double)
	 * input(deltaTime)} and {@link InputCallback#update(double)
	 * update(deltaTime)} for all of this {@link Window}'s registered
	 * {@link InputCallback}s.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;This method should only be called by this Window's
	 * display thread. It is marked <tt>public</tt> so that developers may use
	 * it to poll the mouse and keyboard again before the next frame as
	 * necessary.
	 * 
	 * @return Whether or not this Window {@link #shouldContinueRunning() should
	 *         continue running} */
	public boolean pollKeyboardAndMouse() {
		try {
			this.shellActive = Window.isShellActive(this.shell);
			if(!this.shouldContinueRunning()) {
				return false;
			}
			Mouse.poll();
			this.shellActive = Window.isShellActive(this.shell);
			if(!this.shouldContinueRunning()) {
				return false;
			}
			Keyboard.poll();
			if(!this.shouldContinueRunning()) {
				return false;
			}
			Renderer activeRenderer = this.getActiveRenderer();
			
			final double deltaTime = this.ΔTime;
			final Double dt = Double.valueOf(deltaTime);
			long startTime = System.currentTimeMillis();
			List<InputCallback> inputListeners = this.getAvailableInputCallbacks();
			
			for(InputCallback listener : inputListeners) {
				if(listener instanceof Renderer && listener != activeRenderer) {
					continue;
				}
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
			for(InputCallback listener : inputListeners) {
				if(listener instanceof Renderer && listener != activeRenderer) {
					continue;
				}
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
			if((Mouse.isCaptured() && !Mouse.isModal()) || this.getAvailableMenuProviders().isEmpty()) {
				this.destroyGLCanvasPopupMenu();
			} else {
				if(this.glCanvas.getMenu() == null) {
					this.createGLCanvasPopupMenu();
				}
			}
		} catch(Throwable ex) {
			System.out.flush();
			System.err.println("An exception occurred while polling the system mouse and keyboard:");
			ex.printStackTrace(System.err);
			System.err.flush();
		}
		return this.shouldContinueRunning();
	}
	
	/** Polls all available input devices and then returns whether or not
	 * polling was successful in addition to whether or not this {@link Window}
	 * {@link #shouldContinueRunning() should continue running}.
	 * 
	 * @return Whether or not polling the various input devices was successful
	 *         and this Window should continue running */
	public boolean pollInputDevices() {
		if(this.controllerManager == null || this.controllerManager.isDisposed()) {
			this.controllerManager = null;
			return this.pollKeyboardAndMouse();
		}
		if(this.pollControllersAsynchronously) {
			return this.pollKeyboardAndMouse();
		}
		return this.pollKeyboardAndMouse() && this.pollControllers();
	}
	
	/** Inspects the current controller configuration and polls the connected
	 * controllers for input data.
	 * 
	 * @return Whether or not polling the controllers was successful */
	public boolean pollControllers() {
		final ControllerManager manager = this.controllerManager;
		if(manager != null && !manager.isDisposed()) {
			if(this.pollControllersAsynchronously && this.controllerPollThread != null) {
				return false;
			}
			return manager.pollControllers();
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
		final ControllerManager manager = this.controllerManager;
		if(manager != null && !manager.isDisposed()) {
			return manager.getControllers();
		}
		return new ArrayList<>();
	}
	
	/** Returns the {@link ControllerManager} that this {@link Window} is
	 * currently using.<br>
	 * This method is thread-safe.
	 * 
	 * @return This Window's ControllerManager (may be <tt><b>null</b></tt>) */
	public ControllerManager getControllerManager() {
		if(this.controllerManager != null && this.controllerManager.isDisposed()) {
			this.controllerManager = null;
		}
		return this.controllerManager;
	}
	
	protected void updateTitle() {
		MenuItem mntmUpdateTitleIf = this.mntmUpdateTitleIf;
		if(mntmUpdateTitleIf != null && !mntmUpdateTitleIf.isDisposed()) {
			SWTUtil.setSelection(mntmUpdateTitleIf, this.appendTitleIfUIForced);
		}
		
		String extraTitle = "";
		if(this.appendTitleIfUIForced) {
			boolean captured = Mouse.isCaptured();
			boolean alwaysOnTop = this.isCurrentlyAlwaysOnTop();
			
			extraTitle = extraTitle.concat(alwaysOnTop ? (extraTitle.isEmpty() ? "" : ", ").concat("Always On Top") : "");
			extraTitle = extraTitle.concat(captured ? (extraTitle.isEmpty() ? "" : ", ").concat("Mouse Captured") : "");
		}
		if(!this.running) {
			boolean glThreadRunning = this.glThread == null ? false : this.glThread.isRunning();
			String shutdownTitle = (glThreadRunning ? "Waiting on GLThread" : "").concat(extraTitle.isEmpty() ? "" : "; ").concat(extraTitle);
			
			extraTitle = "Shutting down: ".concat(shutdownTitle);
		}
		
		this.effectiveTitle = this.title.concat(extraTitle.isEmpty() ? "" : String.format(" (%s)", extraTitle));
		SWTUtil.setTitle(this.shell, this.effectiveTitle);
	}
	
	protected void updateUI() {
		defaultRefreshRate = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
		
		if(this.isFullscreen()) {
			this.updateMenuBar = false;
			if(!Window.isShellActive(this.shell)) {//if(this.display.getActiveShell() != this.shell) {
				Shell check = this.display.getActiveShell();
				if(check != null) {
					this.setFullscreen(false);
					check.forceActive();
					check.forceFocus();
				} else {
					this.shell.forceActive();
					this.shell.forceFocus();
				}
			}
		} else {
			if(this.shell.getMenuBar() == null) {
				this.createMenus();
			}
			/*if(Mouse.isCaptured()) {
				if(!Window.isShellActive(this.shell)) {
					Shell check = this.display.getActiveShell();
					if(check != null && check != this.shell) {
						this.shell.setActive();
						this.shell.setFocus();
					} else {
						Mouse.setCaptured(false);
					}
				}
			}*/
		}
		if(this.updateMenuBar) {
			if(this.isFullscreen) {
				this.updateMenuBar = false;
			} else {
				if(this.shell.getMenuBar() == null) {
					this.createMenus();// this.updateMenuBar is set to false within this method
				} else {
					this.destroyMenus();
					this.createMenus();
				}
			}
		}
		this.controllerManager.setWindowActive(this.shellActive = Window.isShellActive(this.shell));
		if(this.shellActive) {
			this.glCanvas.forceFocus();
		} else {
			if(!this.isFullscreen) {
				if(Mouse.isCaptured()) {
					Mouse.setCaptured(false);
				}
			}
		}
		SWTUtil.setAlwaysOnTop(this.shell, this.alwaysOnTopEnabled && (this.alwaysOnTopFullscreenOrWindowed == null || this.alwaysOnTopFullscreenOrWindowed.booleanValue() == this.isFullscreen));//SWTUtil.setAlwaysOnTop(this.shell, this.alwaysOnTopEnabled ? (this.alwaysOnTopFullscreenOrWindowed == null ? true : this.alwaysOnTopFullscreenOrWindowed.booleanValue() == this.isFullscreen) : false);
		this.updateTitle();
		this.shellStyle = this.shell.getStyle();
		this.shellVisible = this.shell.isVisible() && !this.shell.getMinimized();
		
		Menu rendererMenu = this.mntmRenderers == null || this.mntmRenderers.isDisposed() ? null : this.mntmRenderers.getMenu();
		if(rendererMenu != null && !rendererMenu.isDisposed()) {
			final Renderer activeRenderer = this.getActiveRenderer();
			for(MenuItem item : rendererMenu.getItems()) {
				if(item instanceof RendererMenuItem) {
					RendererMenuItem rItem = (RendererMenuItem) item;
					Renderer renderer = rItem.getRenderer();
					if(renderer == null || !this.isRendererRegistered(renderer)) {
						rItem.dispose();
						continue;
					}
					SWTUtil.setSelection(item, renderer == activeRenderer);
				}
			}
			MenuItem noRenderer = this.mntmNoRenderer;
			if(noRenderer != null && !noRenderer.isDisposed()) {
				SWTUtil.setSelection(noRenderer, activeRenderer == null);
			}
		}
		
		for(MenuProvider provider : this.getAvailableMenuProviders()) {
			try {
				provider.updateMenuItems();
			} catch(Throwable ex) {
				if(!handleMenuProviderException(provider, ex, "updateMenuItems")) {
					this.unregisterMenuProvider(provider);
					continue;
				}
			}
		}
	}
	
	/** Returns whether or not this {@link Window} should continue to run.
	 * 
	 * @return Whether or not this Window should continue running */
	public final boolean shouldContinueRunning() {
		return this.running && !this.shell.isDisposed();
	}
	
	/** Returns whether or not this Window is currently running.<br>
	 * This has nothing to do with whether or not this window is
	 * {@link Window#isVisible() currently visible}.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this Window is currently running (open or not)
	 * @see #isShuttingDown()
	 * @see #shouldContinueRunning()
	 * @see #waitingForGLThreadToShutDown() */
	public final boolean isRunning() {
		return this.running && this.display != null && !this.display.isDisposed() && this.display.getThread().isAlive();
	}
	
	/** Returns whether or not this Window is currently in the process of shutting down.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this Window is currently running (open or not)
	 * @see #shouldContinueRunning()
	 * @see #waitingForGLThreadToShutDown() */
	public final boolean isShuttingDown() {
		try {
			return !this.running && this.display != null && !this.display.isDisposed() && this.display.getThread().isAlive();
		} catch(SWTException ex) {
			String msg = ex.getMessage();
			if(msg != null && msg.equals("Device is disposed")) {
				return true;
			}
			throw ex;
		}
	}
	
	/** @return True if this {@link Window} is currently shutting down (after
	 *         the end-user has clicked the X or selected &quot;File -&gt;
	 *         Exit&quot;) and is waiting for the {@link GLThread} to finish
	 *         executing */
	public final boolean waitingForGLThreadToShutDown() {
		return !this.shouldContinueRunning() && !this.shell.isDisposed() && this.glThread != null && this.glThread.isRunning();
	}
	
	/** Maintains the application window, polls the mouse and keyboard, and
	 * performs various other upkeep tasks.<br>
	 * <b>Note:</b>&nbsp;This method should only be called by the main display
	 * thread.
	 * 
	 * @return Whether or not this {@link Window} should continue running */
	public boolean swtLoop() {
		/*while(this.display.readAndDispatch()) {
		}
		this.timer.frequencySleep();*/
		if(this.shouldContinueRunning() || (!this.shell.isDisposed() && this.glThread != null && this.glThread.isRunning())) {
			this.updateUI();
		}
		this.updateFrameTime();
		/*while(this.display.readAndDispatch()) {
		}*/
		if(this.shouldContinueRunning() || (!this.shell.isDisposed() && this.glThread != null && this.glThread.isRunning())) {
			return this.pollInputDevices();
		}
		return false;
	}
	
	/** Has the main display thread execute the given runnable at the nearest
	 * opportunity.
	 * 
	 * @param runnable The runnable to execute on the main display thread
	 * @return This {@link Window}
	 * @throws RejectedExecutionException Thrown if the main display has been
	 *             disposed */
	public Window swtExec(Runnable runnable) throws RejectedExecutionException {
		if(this.display == null || this.display.isDisposed()) {
			throw new RejectedExecutionException("Display is disposed!");
		}
		this.display.asyncExec(runnable);
		return this;
	}
	
	/** @return The main display thread */
	public final Thread getWindowThread() {
		return this.display == null || this.display.isDisposed() ? null : this.display.getThread();
	}
	
	/** Returns the {@link GLThread} that this {@link Window} is using.<br>
	 * This method is thread safe.
	 * 
	 * @return The {@link GLThread} that this {@link Window} is using */
	public final GLThread getGLThread() {
		return this.glThread;
	}
	
	/** Returns the {@link SoundManager} that this {@link Window} is using.<br>
	 * This method is thread safe.
	 *
	 * @return The {@link SoundManager} that this {@link Window} is using */
	public final SoundManager getSoundManager() {
		return this.soundManager;
	}
	
	public final boolean areControllersBeingPolledAsynchronously() {
		return this.pollControllersAsynchronously && this.controllerPollThread != null && this.controllerPollThread.isAlive();
	}
	
	@SuppressWarnings("deprecation")
	public final Window setPollControllersAsynchronously(boolean pollControllersAsynchronously) {
		if(pollControllersAsynchronously != this.pollControllersAsynchronously) {
			this.pollControllersAsynchronously = pollControllersAsynchronously;
			Thread pollThread = this.controllerPollThread;
			if(!pollControllersAsynchronously) {
				if(pollThread != null) {
					final long startTime = System.currentTimeMillis(), timeout = 3000L;
					while(pollThread.isAlive()) {
						if(System.currentTimeMillis() - startTime >= timeout || pollThread.getState() == Thread.State.BLOCKED) {
							pollThread.stop();
						}
					}
					this.controllerPollThread = null;
				}
			} else {
				if(pollThread == null) {
					this.controllerPollThread = this.controllerManager.pollContinuously(null, b -> Boolean.valueOf(this.running && this.pollControllersAsynchronously));
				}
			}
		}
		return this;
	}
	
	public final Thread getControllerPollThread() {
		return this.controllerPollThread;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getEffectiveTitle() {
		String title = this.effectiveTitle;
		return title == null ? this.title : title;
	}
	
	public Window setTitle(String title) {
		this.title = title == null ? "" : title;
		return this;
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
			int xDiff = size.x - clientArea.width, yDiff = size.y - clientArea.height;
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
	
	/** Returns whether or not this {@link Window} is currently visible to the
	 * end-user.<br>
	 * This method is thread-safe.
	 * 
	 * @return Whether or not this Window is currently visible to the
	 *         end-user */
	public final boolean isVisible() {
		return !this.shell.isDisposed() && this.shellVisible;
	}
	
	public final boolean readAndDispatch() {
		if(this.swtLoop()) {
			while(this.display.readAndDispatch()) {
			}
			this.display.sleep();
			return true;
		}
		return false;
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
		if(!instances.contains(this)) {
			instances.add(this);
		}
		this.running = true;
		
		try {
			if(instance == this) {
				Mouse.setCursorCanvas(this.glCanvas);
			}
			
			this.show();
			this.shellHandle = SWTUtil.getHandle(this.shell);
			this.setGLCanvasSize(this.glTargetWidth, this.glTargetHeight);
			SWTUtil.centerShellOnPrimaryMonitor(this.shell);
			
			if(this.activeRendererToSetOnStartup != null) {
				this.setActiveRenderer(this.activeRendererToSetOnStartup);
				this.activeRendererToSetOnStartup = null;
			}
			
			this.glThread.start();
			while(this.swtLoop() && this.glThread.getState() == Thread.State.NEW) {
				while(this.display.readAndDispatch()) {
				}
				this.display.sleep();
			}
			this.fpsLogThread.start();
			if(this.pollControllersAsynchronously && this.controllerManager != null && !this.controllerManager.isDisposed()) {
				this.controllerPollThread = this.controllerManager.pollContinuously(null, b -> Boolean.valueOf(this.running && this.pollControllersAsynchronously));
			}
			this.soundManager.startRunning();
			for(Renderer failedRenderer : this.glThread.initializeRenderers(this.getAvailableRenderers(), true)) {
				this.unregisterRenderer(failedRenderer);
			}
			
			Thread periodicWakeThread = new Thread(() -> {
				while(this.running && !this.display.isDisposed()) {
					this.display.wake();
					this.timer.frequencySleep();
				}
			}, "PeriodicWakeSWTThread");
			periodicWakeThread.setDaemon(true);
			periodicWakeThread.start();
			
			this.hasOpenedYet = true;
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
				
				while(this.display.readAndDispatch()) {
				}
				this.display.sleep();
			}
			
			this.glThread.setRenderer(null);
			this.glThread.stopRunning(true);
			return;
		} catch(Throwable ex) {
			ex.printStackTrace();
		} finally {
			this.running = false;
			this.shell.dispose();
			SWTResourceManager.dispose();
			this.display.dispose();
			this.hasOpenedYet = false;
			
			this.soundManager.stopRunning();
			
			/*for(Game game : this.games) {
				this.unregisterGame(game);
			}
			for(Renderer renderer : this.renderers) {
				this.unregisterRenderer(renderer);
			}
			for(InputCallback listener : this.inputListeners) {
				this.unregisterInputCallback(listener);
			}
			for(MenuProvider provider : this.menuProviders) {
				this.unregisterMenuProvider(provider);
			}*/
			this.unregisterInputCallback(this.uiCallback);
			
			instances.remove(this);
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
			if(this.display == null || this.display.isDisposed()) {
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
		this.updateUI();
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
			if(this.display == null || this.display.isDisposed()) {
				throw new RejectedExecutionException("Display is disposed!");
			}
			this.display.asyncExec(() -> {
				this.hide();
			});
			return this;
		}
		this.shell.setVisible(false);
		this.updateUI();
		return this;
	}
	
	/** Closes this {@link Window}, disposing of any system resources it was
	 * using.<br>
	 * This method is thread-safe.<br>
	 * <br>
	 * This Window may be re-opened after closing by simply calling
	 * {@link #open()} again. */
	public void close() {
		if(this.waitingForGLThreadToShutDown()) {
			this.glThread.stop();
		}
		this.running = false;
	}
	
	/** Tells this {@link Window} that it needs to close, and waits either until
	 * the window has finished closing, or until the specified amount of time
	 * has elapsed.<br>
	 * This method is thread-safe; however it will block if <tt>waitFor</tt> is
	 * <tt>true</tt> and this method was called outside of this Window's display
	 * thread.
	 * 
	 * @param waitFor Whether or not this method should block until this Window
	 *            has finished closing
	 * @param timeoutMillis The amount of time, in milliseconds, that this
	 *            method should block for before throwing a
	 *            {@link TimeoutException}.<br>
	 *            You may specify a number less than one for no timeout.
	 * @throws TimeoutException Thrown if this Window took longer than the
	 *             specified <tt>timeoutMillis</tt> to finish closing */
	public void close(boolean waitFor, long timeoutMillis) throws TimeoutException {
		final Display display = this.display;
		final String title = this.title;
		this.close();
		if(waitFor && display != null && display.getThread() != Thread.currentThread()) {
			long startTime = System.currentTimeMillis();
			Display curDisplay;
			while(!display.isDisposed()) {
				curDisplay = Display.getCurrent();
				if(curDisplay != null && !curDisplay.isDisposed()) {
					if(!curDisplay.readAndDispatch()) {
						CodeUtil.sleep(10L);
					}
				} else {
					CodeUtil.sleep(10L);
				}
				if(timeoutMillis > 0 && System.currentTimeMillis() - startTime >= timeoutMillis) {
					throw new TimeoutException(String.format("Timeout of %s ms reached while waiting for window \"%s\" to close!", Long.toString(timeoutMillis), title));
				}
			}
		}
	}
	
	/** Tells this {@link Window} that it needs to close, and waits either until
	 * the window has finished closing, or until the specified amount of time
	 * has elapsed.<br>
	 * This method is thread-safe; however it will block if <tt>waitFor</tt> is
	 * <tt>true</tt>.
	 * 
	 * @param waitFor Whether or not this method should block until this Window
	 *            has finished closing */
	public void close(boolean waitFor) {
		try {
			this.close(waitFor, -1L);
		} catch(TimeoutException ex) {
			throw new RuntimeException("This should not have been able to happen!", ex);
		}
	}
	
	/** Returns whether or not this {@link Window} has been
	 * {@link Window#close() closed} (or <em>disposed</em>).
	 * 
	 * @return Whether or not this Window has been {@link Window#close()
	 *         closed} */
	public boolean isClosed() {
		return this.display == null || this.display.isDisposed() || this.shell == null || this.shell.isDisposed();
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
			if(this.controllerManager != null && !this.controllerManager.isDisposed()) {
				registeredAnywhere |= this.controllerManager.registerInputCallback(game);
			}
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
			if(this.controllerManager != null && !this.controllerManager.isDisposed()) {
				unregisteredAnywhere |= this.controllerManager.unregisterInputCallback(game);
			}
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
	
	/** Checks and returns whether or not the specified {@link InputCallback} is
	 * registered with this {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @param inputCallback The InputCallback to check
	 * @return Whether or not the InputCallback is registered with this
	 *         Window
	 * @see #registerInputCallback(InputCallback)
	 * @see #unregisterInputCallback(InputCallback) */
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
	
	/** Registers the specified {@link InputCallback} with this {@link Window}
	 * if it wasn't already.<br>
	 * This method is thread-safe.
	 * 
	 * @param inputCallback The InputCallback to register
	 * @return <tt>true</tt> if the InputCallback was just registered;
	 *         <tt>false</tt> otherwise
	 * @see #isInputCallbackRegistered(InputCallback)
	 * @see #unregisterInputCallback(InputCallback) */
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
			if(this.controllerManager != null && !this.controllerManager.isDisposed()) {
				registeredAnywhere |= this.controllerManager.registerInputCallback(inputCallback);
			}
			return registeredAnywhere;
		}
		return false;
	}
	
	/** Unregisters the specified {@link InputCallback} from this {@link Window}
	 * if it was previously registered.<br>
	 * This method is thread-safe.
	 * 
	 * @param inputCallback The InputCallback to unregister
	 * @return <tt>true</tt> if the InputCallback was just unregistered;
	 *         <tt>false</tt> otherwise
	 * @see #isInputCallbackRegistered(InputCallback)
	 * @see #registerInputCallback(InputCallback) */
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
			if(this.controllerManager != null && !this.controllerManager.isDisposed()) {
				unregisteredAnywhere |= this.controllerManager.unregisterInputCallback(inputCallback);
			}
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
			ex1.addSuppressed(ex);
			ex = ex1;
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
			System.err.println(StringUtil.throwableToStr(ex));
			System.err.flush();
		}
		return handled;
	}
	
	/** Checks and returns whether or not the specified {@link Renderer} is
	 * registered with this {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @param renderer The Renderer to check
	 * @return Whether or not the Renderer is registered with this Window
	 * @see #registerRenderer(Renderer)
	 * @see #unregisterRenderer(Renderer) */
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
	
	/** Registers the specified {@link Renderer} with this {@link Window} if it
	 * wasn't already.<br>
	 * This method is thread-safe.
	 * 
	 * @param renderer The Renderer to register
	 * @return <tt>true</tt> if the Renderer was just registered; <tt>false</tt>
	 *         otherwise
	 * @see #isRendererRegistered(Renderer)
	 * @see #unregisterRenderer(Renderer) */
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
	
	/** Unregisters the specified {@link Renderer} from this {@link Window} if
	 * it was previously registered.<br>
	 * This method is thread-safe.
	 * 
	 * @param renderer The Renderer to unregister
	 * @return <tt>true</tt> if the Renderer was just unregistered;
	 *         <tt>false</tt> otherwise
	 * @see #isRendererRegistered(Renderer)
	 * @see #registerRenderer(Renderer) */
	public final boolean unregisterRenderer(final Renderer renderer) {
		final boolean wasRendererRegistered = this.isRendererRegistered(renderer);
		final boolean wasActiveRenderer = this.getActiveRenderer() == renderer;
		try {
			if(renderer instanceof Game) {
				while(this.renderers.remove(renderer)) {
				}
				return this.unregisterGame((Game) renderer);
			}
			if(renderer != null && wasRendererRegistered) {
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
		} finally {// If the renderer is the active renderer when this method is called, and it was registered beforehand, but it isn't now, then we need to un-set it as the active renderer since it is now in an inconsistent state with this Window
			if(wasActiveRenderer && wasRendererRegistered && !this.isRendererRegistered(renderer)) {
				this.setActiveRenderer(null);
			}
		}
	}
	
	/** Checks and returns whether or not the specified {@link MenuProvider} is
	 * registered with this {@link Window}.<br>
	 * This method is thread-safe.
	 * 
	 * @param provider The MenuProvider to check
	 * @return Whether or not the MenuProvider is registered with this Window
	 * @see #registerMenuProvider(MenuProvider)
	 * @see #unregisterMenuProvider(MenuProvider) */
	public final boolean isMenuProviderRegistered(MenuProvider provider) {
		if(provider instanceof Renderer) {
			while(this.menuProviders.remove(provider)) {
			}
			return this.isRendererRegistered((Renderer) provider);
		}
		return provider == null ? false : this.menuProviders.contains(provider);
	}
	
	/** Registers the specified {@link MenuProvider} with this {@link Window} if
	 * it wasn't already.<br>
	 * This method is thread-safe.
	 * 
	 * @param provider The MenuProvider to register
	 * @return <tt>true</tt> if the MenuProvider was just registered;
	 *         <tt>false</tt> otherwise
	 * @see #isMenuProviderRegistered(MenuProvider)
	 * @see #unregisterMenuProvider(MenuProvider) */
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
	
	/** Unregisters the specified {@link MenuProvider} from this {@link Window}
	 * if it was previously registered.<br>
	 * This method is thread-safe.
	 * 
	 * @param provider The MenuProvider to unregister
	 * @return <tt>true</tt> if the MenuProvider was just unregistered;
	 *         <tt>false</tt> otherwise
	 * @see #isMenuProviderRegistered(MenuProvider)
	 * @see #registerMenuProvider(MenuProvider) */
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
		if(!this.enableRendererSwitching && this.hasOpenedYet) {
			return false;
		}
		if((this.display == null || this.display.isDisposed()) && renderer != null) {
			throw new IllegalStateException("Display is disposed!");
		}
		if(!this.isRunning()) {
			this.activeRendererToSetOnStartup = renderer;
			return true;
		}
		if(Thread.currentThread() != this.getWindowThread()) {
			final Boolean[] rtrn = {null};
			this.display.asyncExec(() -> {
				rtrn[0] = Boolean.valueOf(this.setActiveRenderer(renderer));
			});
			Display display;
			while(rtrn[0] == null) {
				display = Display.getCurrent();
				if(display != null && !display.isDisposed()) {
					if(!display.readAndDispatch()) {
						CodeUtil.sleep(10L);
					}
				} else {
					CodeUtil.sleep(10L);
				}
			}
			return rtrn[0].booleanValue();
		}
		Renderer originalRenderer = this.getActiveRenderer();
		if(originalRenderer == renderer) {
			return true;
		}
		boolean createMenuBar = this.shell.getMenuBar() != null;
		this.destroyMenus();
		
		String title = this.title;
		this.setTitle(this.originalTitle);
		
		if(this.glThread.setRenderer(renderer, true)) {
			if(createMenuBar && !this.isFullscreen()) {
				this.createMenus();
			}
			if(renderer != null) {
				this.registerRenderer(renderer);
				if(this.title.equals(this.originalTitle)) {
					this.setTitle(renderer.getName());
				}
			}
			return true;
		}
		if(title != null && !title.trim().isEmpty()) {
			this.setTitle(title);
		}
		return false;
	}
	
	/** Returns the renderer that this {@link Window}'s {@link GLThread} is
	 * currently using to display graphics.<br>
	 * This method is thread-safe.
	 * 
	 * @return The {@link Renderer renderer} that this Window's GLThread is
	 *         currently using to display graphics */
	public final Renderer getActiveRenderer() {
		return this.glThread.getRenderer();
	}
	
	public boolean getRendererSwitchingEnabled() {
		return this.enableRendererSwitching;
	}
	
	public Window setRendererSwitchingEnabled(boolean enableRendererSwitching) {
		final boolean oldEnable = this.enableRendererSwitching;
		this.enableRendererSwitching = enableRendererSwitching;
		
		if(oldEnable != this.enableRendererSwitching) {
			this.updateMenuBar = true;
		}
		return this;
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
	 * @return The renderer after the currently active renderer in the list
	 *         returned by {@link Window#getAvailableRenderers()} */
	public final Renderer getNextRenderer() {
		final Renderer activeRenderer = this.getActiveRenderer();
		List<Renderer> list = this.getAvailableRenderers();
		if(activeRenderer != null) {
			if(!list.isEmpty() && list.get(list.size() - 1) == activeRenderer) {
				return null;
			}
			boolean thisOne = false;
			Renderer firstRenderer = null;
			for(Renderer renderer : list) {
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
			if(thisOne) {
				return firstRenderer;
			}
		}
		return !list.isEmpty() ? list.get(0) : null;
	}
	
	/** Returns the renderer before the currently active renderer in the list
	 * returned by {@link Window#getAvailableRenderers()}.<br>
	 * This method is thread-safe.
	 * 
	 * @return The renderer before the currently active renderer in the list
	 *         returned by {@link Window#getAvailableRenderers()} */
	public final Renderer getPreviousRenderer() {
		final Renderer activeRenderer = this.getActiveRenderer();
		List<Renderer> list = this.getAvailableRenderers();
		if(activeRenderer != null) {
			if(!list.isEmpty() && list.get(0) == activeRenderer) {
				return null;
			}
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
			if(thisOne) {
				return lastRenderer;
			}
		}
		return !list.isEmpty() ? list.get(list.size() - 1) : null;
	}
	
	/** Returns a list of all of this {@link Window}'s available
	 * {@link InputCallback input callbacks}, some of which may or may not be
	 * instances of {@link Game} or {@link Renderer}.<br>
	 * This method is thread-safe.
	 * 
	 * @return A list of all of this Window's available input callbacks */
	public final List<InputCallback> getAvailableInputCallbacks() {
		List<InputCallback> list = new ArrayList<>();
		list.addAll(this.inputListeners);
		for(Game game : this.games) {
			if(!list.contains(game)) {
				list.add(game);
			}
		}
		for(Renderer renderer : this.renderers) {
			if(renderer instanceof InputCallback) {
				InputCallback listener = (InputCallback) renderer;
				if(!list.contains(listener)) {
					list.add(listener);
				}
			}
		}
		Renderer activeRenderer = this.getActiveRenderer();
		if(activeRenderer instanceof InputCallback) {
			InputCallback listener = (InputCallback) activeRenderer;
			if(!list.contains(listener)) {
				list.add(listener);
			}
		}
		return list;
	}
	
	/** Returns a list of all of this {@link Window}'s available
	 * {@link MenuProvider menu providers}, some of which may or may not be
	 * instances of {@link Game}, {@link Renderer}, or
	 * {@link InputCallback}.<br>
	 * This method is thread-safe.
	 * 
	 * @return A list of all of this Window's available menu providers */
	public final List<MenuProvider> getAvailableMenuProviders() {
		List<MenuProvider> list = new ArrayList<>();
		list.addAll(this.menuProviders);
		for(Game game : this.games) {
			if(game instanceof MenuProvider) {
				MenuProvider provider = (MenuProvider) game;
				if(!list.contains(provider)) {
					list.add(provider);
				}
			}
		}
		for(Renderer renderer : this.renderers) {
			if(renderer instanceof MenuProvider) {
				MenuProvider provider = (MenuProvider) renderer;
				if(!list.contains(provider)) {
					list.add(provider);
				}
			}
		}
		for(InputCallback listener : this.inputListeners) {
			if(listener instanceof MenuProvider) {
				MenuProvider provider = (MenuProvider) listener;
				if(!list.contains(provider)) {
					list.add(provider);
				}
			}
		}
		Renderer activeRenderer = this.getActiveRenderer();
		if(activeRenderer instanceof MenuProvider) {
			MenuProvider provider = (MenuProvider) activeRenderer;
			if(!list.contains(provider)) {
				list.add(provider);
			}
		}
		return list;
	}
	
	//==========================================================================================================
	
	/** Obtains the {@link Window#getCurrent() current window} and the
	 * {@link Thread#currentThread()}, then checks and returns the thread's
	 * relation to the window.
	 * 
	 * @return The current thread's relation to the current window, or
	 *         <tt><b>null</b></tt> if the current window could not be
	 *         determined
	 * @see ThreadType#get(Thread, Window)
	 * @see ThreadType#getCurrent(Window) */
	public static final ThreadType getCurrentThreadType() {
		return ThreadType.get(Thread.currentThread(), Window.getCurrent());
	}
	
	/** Obtains the {@link Window#getCurrent() current window}, then checks and
	 * returns the given thread's relation to the window.
	 *
	 * @param thread The thread whose relation to the current window will be
	 *            checked and returned
	 * @return The current thread's relation to the current window, or
	 *         <tt><b>null</b></tt> if either the given thread was
	 *         <tt><b>null</b></tt>, or the current window could not be
	 *         determined
	 * @see ThreadType#get(Thread, Window)
	 * @see ThreadType#getCurrent(Window) */
	public static final ThreadType getCurrentThreadType(Thread thread) {
		return ThreadType.get(thread, Window.getCurrent());
	}
	
	/** Obtains the {@link Thread#currentThread()}, then checks and returns its
	 * relation to the given window.
	 *
	 * @param window The window that will be used when checking the current
	 *            thread's relation to it
	 * @return The current thread's relation to the current window, or
	 *         <tt><b>null</b></tt> if the current window could not be
	 *         determined
	 * @see ThreadType#get(Thread, Window)
	 * @see ThreadType#getCurrent(Window) */
	public static final ThreadType getCurrentThreadType(Window window) {
		return ThreadType.get(Thread.currentThread(), window);
	}
	
	/** Obtains the {@link Thread#currentThread()}, then checks and returns its
	 * relation to this window.
	 *
	 * @return The current thread's relation to this window (should never be
	 *         <tt><b>null</b></tt>)
	 * @see ThreadType#get(Thread, Window)
	 * @see ThreadType#getCurrent(Window) */
	public final ThreadType getThreadType() {
		return ThreadType.get(Thread.currentThread(), this);
	}
	
	/** Checks and returns the given thread's relation to this window.
	 *
	 * @param thread The thread whose relation to this window will be checked
	 *            and returned
	 * @return The current thread's relation to this window, or
	 *         <tt><b>null</b></tt> if the given thread was <tt><b>null</b></tt>
	 * @see ThreadType#get(Thread, Window)
	 * @see ThreadType#getCurrent(Window) */
	public final ThreadType getThreadType(Thread thread) {
		return ThreadType.get(thread, this);
	}
	//
}
