/*******************************************************************************
 * 
 * Copyright © 2021 Brian_Entei (br45entei@gmail.com)
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
package com.gmail.br45entei.thread;

import com.gmail.br45entei.audio.SoundManager;
import com.gmail.br45entei.game.graphics.GLThread;
import com.gmail.br45entei.game.graphics.Renderer;
import com.gmail.br45entei.game.input.ControllerManager;
import com.gmail.br45entei.game.ui.Window;

import org.eclipse.swt.widgets.Display;
import org.lwjgl.opengl.swt.GLData;

/** Enum class used to depict the various thread types that are {@link UsedBy
 * used by} or associated with a {@link Window}.
 *
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt;
 * @see #UI
 * @see #OpenGL
 * @see #CONTROLLER
 * @see #SOUND
 * @see #SCREENSHOT_CAPTURER
 * @see #VIDEO_RECORDER
 * @see #UNSPECIFIED
 * @see #get(Thread, Window)
 * @see #getCurrent(Window) */
public enum ThreadType {
	/** A {@link Window}'s main User-Interface thread. (Also referred to as
	 * the &quot;display thread&quot;)<br>
	 * This thread handles operating system calls (which keep up with the
	 * application's window) and {@link Window#pollKeyboardAndMouse() polls
	 * the system keyboard and mouse}.<br>
	 * This thread can execute tasks given to it asynchronously via
	 * {@link Display#asyncExec(Runnable)
	 * window.getShell().getDisplay().asyncExec(Runnable)}.<br>
	 * <br>
	 * In the context of methods and fields, this means the display thread
	 * is the thread that will primarily (call/access) the
	 * (method/field).<br>
	 * It is <em>probably <b>not</b></em> safe for other threads to access
	 * the aforementioned.
	 *
	 * @see #OpenGL
	 * @see #CONTROLLER
	 * @see #SOUND
	 * @see #SCREENSHOT_CAPTURER
	 * @see #VIDEO_RECORDER
	 * @see #UNSPECIFIED
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	UI,
	/** A {@link Window}'s main {@link GLThread}.<br>
	 * This thread calls the {@link Renderer#render(double)} method of the
	 * Window's {@link Window#getActiveRenderer() currently active renderer}
	 * and then swaps the front and back color buffers.<br>
	 * This thread can execute tasks given to it asynchronously via
	 * {@link GLThread#asyncExec(Runnable)
	 * window.getGLThread().asyncExec(Runnable)}.<br>
	 * <br>
	 * In the context of methods and fields, this means the {@link GLThread}
	 * thread is the thread that will primarily (call/access) the
	 * (method/field).<br>
	 * It is <em><b>not</b></em> safe for other threads to access the
	 * aforementioned.
	 *
	 * @see #UI
	 * @see #CONTROLLER
	 * @see #SOUND
	 * @see #SCREENSHOT_CAPTURER
	 * @see #VIDEO_RECORDER
	 * @see #UNSPECIFIED
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	OpenGL,
	/** A {@link Window}'s {@link ControllerManager}'s poll thread.<br>
	 * This thread detects and polls all controllers connected to the
	 * system.<br>
	 * <br>
	 * In the context of methods and fields, this means the controller poll
	 * thread is the thread that will primarily (call/access) the
	 * (method/field).<br>
	 * It is <em>probably</em> safe for other threads to access the
	 * aforementioned, however it is not recommended.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;This thread may not exist if
	 * {@link Window#Window(String, int, int, double, GLData, boolean, Renderer)
	 * new Window(String, int, int, double, GLData, boolean, Renderer)} is
	 * called with <tt>pollControllersAsynchronously</tt> set to
	 * <tt>false</tt>}
	 *
	 * @see #UI
	 * @see #OpenGL
	 * @see #SOUND
	 * @see #SCREENSHOT_CAPTURER
	 * @see #VIDEO_RECORDER
	 * @see #UNSPECIFIED
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	CONTROLLER,
	/** A {@link Window}'s main {@link SoundManager} thread.<br>
	 * This thread is the thread that creates, outputs, and manages sounds
	 * for the application.<br>
	 * <br>
	 * In the context of methods and fields, this means the
	 * {@link SoundManager} thread is the thread that will primarily
	 * (call/access) the (method/field).<br>
	 * It is <em>probably <b>not</b></em> safe for other threads to access
	 * the aforementioned.
	 *
	 * @see #UI
	 * @see #OpenGL
	 * @see #CONTROLLER
	 * @see #SCREENSHOT_CAPTURER
	 * @see #VIDEO_RECORDER
	 * @see #UNSPECIFIED
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	SOUND,
	/** A {@link Window}'s {@link GLThread}'s {@link ScreenshotHelper}
	 * thread.<br>
	 * This thread has the GLThread capture the current content of the front
	 * color buffer from OpenGL and store it, and then picks up the stored
	 * data and writes it to disk as a PNG file.<br>
	 * <br>
	 * In the context of methods and fields, this means the
	 * {@link ScreenshotHelper} thread is the thread that will primarily
	 * (call/access) the (method/field).<br>
	 * It is <em>probably</em> safe for other threads to access the
	 * aforementioned.
	 *
	 * @see #UI
	 * @see #OpenGL
	 * @see #CONTROLLER
	 * @see #SOUND
	 * @see #VIDEO_RECORDER
	 * @see #UNSPECIFIED
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	SCREENSHOT_CAPTURER,
	/** A {@link Window}'s {@link GLThread}'s {@link VideoHelper}
	 * thread.<br>
	 * This thread performs the same basic actions as the
	 * {@link #SCREENSHOT_CAPTURER} thread, only it captures once every
	 * frame, and stores the output to disk as a MP4 file.<br>
	 * <br>
	 * In the context of methods and fields, this means the
	 * {@link VideoHelper} thread is the thread that will primarily
	 * (call/access) the (method/field).<br>
	 * It is <em>probably</em> safe for other threads to access the
	 * aforementioned.
	 *
	 * @see #UI
	 * @see #OpenGL
	 * @see #CONTROLLER
	 * @see #SOUND
	 * @see #SCREENSHOT_CAPTURER
	 * @see #UNSPECIFIED
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	VIDEO_RECORDER,
	/** A thread whose association with a given {@link Window} is
	 * unknown.<br>
	 * <br>
	 * In the context of methods and fields, this means any thread may
	 * access the field/method, so implementors may wish to take extra
	 * precautions to ensure their code is thread-safe.
	 *
	 * @see #UI
	 * @see #OpenGL
	 * @see #CONTROLLER
	 * @see #SOUND
	 * @see #SCREENSHOT_CAPTURER
	 * @see #VIDEO_RECORDER
	 * @see #get(Thread, Window)
	 * @see #getCurrent(Window) */
	UNSPECIFIED;
	
	/** Checks and returns the given {@link Thread}'s relation to the given
	 * {@link Window}.<br>
	 * If the thread is unrelated to the Window, {@link #UNSPECIFIED} is
	 * returned.<br>
	 * If the given thread or window is <tt><b>null</b></tt>,
	 * <tt><b>null</b></tt> is returned.
	 *
	 * @param thread The {@link Thread} whose type will be returned
	 * @param window The {@link Window} associated with the <tt>thread</tt>
	 * @return The specified {@link Thread}'s {@link ThreadType type}, or
	 *         {@link #UNSPECIFIED} if the specified thread is not
	 *         associated
	 *         with the given {@link Window}
	 * @see #getCurrent(Window) */
	public static ThreadType get(Thread thread, Window window) {
		if(window == null || thread == null) {
			return null;
		}
		Display display = window.getShell() == null ? null : window.getShell().getDisplay();
		Thread uiThread = display == null || display.isDisposed() ? null : display.getThread();
		if(thread == uiThread) {
			return UI;
		}
		GLThread glThread = window.getGLThread();
		if(thread == glThread) {
			return OpenGL;
		}
		ControllerManager controllerManager = window.getControllerManager();
		Thread controllerThread = window.areControllersBeingPolledAsynchronously() && controllerManager != null && !controllerManager.isDisposed() ? window.getControllerPollThread() : null;
		if(thread == controllerThread) {
			return CONTROLLER;
		}
		Thread soundManager = window.getSoundManager();
		if(thread == soundManager) {
			return SOUND;
		}
		if(glThread != null) {
			Thread screenshotHelper = glThread.getScreenshotHelper();
			if(thread == screenshotHelper) {
				return SCREENSHOT_CAPTURER;
			}
			Thread videoRecorder = glThread.getVideoHelper();
			if(thread == videoRecorder) {
				return VIDEO_RECORDER;
			}
		}
		return UNSPECIFIED;
	}
	
	/** Checks and returns the {@link Thread#currentThread()}'s relation to
	 * the given {@link Window}.<br>
	 * If the thread is unrelated to the Window, {@link #UNSPECIFIED} is
	 * returned.<br>
	 * If the given window is <tt><b>null</b></tt>, <tt><b>null</b></tt> is
	 * returned.
	 *
	 * @param window The {@link Window} associated with the
	 *            {@link Thread#currentThread()}
	 * @return The current thread's {@link ThreadType type}, or
	 *         {@link #UNSPECIFIED} if the specified thread is not
	 *         associated
	 *         with the given {@link Window}
	 * @see #get(Thread, Window) */
	public static ThreadType getCurrent(Window window) {
		return get(Thread.currentThread(), window);
	}
	
}
