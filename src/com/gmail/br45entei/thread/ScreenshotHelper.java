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
package com.gmail.br45entei.thread;

import com.gmail.br45entei.game.ui.Window;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Rectangle;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Screenshot helper class designed to make it quick and easy to save a
 * screenshot while also taking some of the load off of the OpenGL thread.
 * 
 * @author Brian_Entei */
public class ScreenshotHelper extends Thread {
	
	private static final File saveFolder;
	
	static {
		File rootDir = new File(System.getProperty("user.dir"));
		saveFolder = new File(rootDir, "screenshots");
		saveFolder.mkdirs();
	}
	
	private final boolean[] state;
	
	/** Creates a new ScreenshotHelper thread.
	 * 
	 * @param state A boolean array containing at least one boolean which will
	 *            let this thread know that it should continue to run */
	public ScreenshotHelper(boolean[] state) {
		this.setName("ScreenshotSaverThread");
		this.setDaemon(true);
		this.setPriority(Thread.MAX_PRIORITY - 4);
		this.state = state;
	}
	
	protected static final void slp(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public final void run() {
		if(!this.state[0]) {//wait for the game to start, otherwise the screenshot thread will just end before the game even starts(derp)
			while(!this.state[0]) {
				slp(10L);
			}
		}
		while(this.state[0]) {
			ArrayList<ScreenshotTask> screenshotsToTake = ScreenshotTask.getInstances();
			for(ScreenshotTask screenshot : screenshotsToTake) {
				screenshot.run();
				//slp(10L);
			}
			//if(screenshotsToTake.isEmpty()) {
			slp(8L);
			//}
		}
	}
	
	/** Reads the current front buffer and stores it into a file. */
	public static final void saveScreenshot(int x, int y, int width, int height) {
		//System.out.println("Saving screenshot...");// with viewport (".concat(Integer.toString(x)).concat(", ").concat(Integer.toString(viewport.y)).concat(", ").concat(Integer.toString(width)).concat(", ").concat(Integer.toString(height)).concat(")..."));
		// read current buffer
		int size = width * height * 3;
		FloatBuffer imageData = createFloatBuffer(size);
		//System.out.println("Thread.currentThread().getName(): ".concat(Thread.currentThread().getName()));
		//System.out.println("glReadPixels(".concat(Integer.toString(x)).concat(", ").concat(Integer.toString(y)).concat(", ").concat(Integer.toString(width)).concat(", ").concat(Integer.toString(height)).concat(", GL11.GL_RGB, GL11.GL_FLOAT, createFloatBuffer(").concat(Integer.toString(size)).concat("))"));
		GL11.glReadPixels(x, y, width, height, GL11.GL_RGB, GL11.GL_FLOAT, imageData);
		//System.out.println("Passing save task off...");
		new ScreenshotTask(imageData, width, height);
	}
	
	protected static final FloatBuffer createFloatBuffer(int size) {
		OutOfMemoryError exception = null;
		FloatBuffer buffer = null;
		int remainingRetries = 20;
		do {
			try {
				buffer = BufferUtils.createFloatBuffer(size);//ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();//Causes the application to hang and then terminate abnormally. Something to do with the fact that LWJGL expects the size of the buffer to be shifted(see BufferUtils.createFloatBuffer(...) source code)
			} catch(final OutOfMemoryError ex) {
				if(exception == null) {
					exception = ex;
				}
				Window.getWindow().swtExec(() -> {
					ex.printStackTrace(System.err);
					System.err.flush();
				});
				remainingRetries--;
			}
		} while(buffer == null && remainingRetries > 0);
		if(buffer == null) {
			throw exception == null ? new OutOfMemoryError("Direct buffer memory") : exception;
		}
		buffer.rewind();
		return buffer;
	}
	
	/** Reads the current front buffer and stores it into a file.
	 * 
	 * @param viewport The area to save a screenshot of */
	public static final void saveScreenshot(Rectangle viewport) {
		saveScreenshot(viewport.x, viewport.y, viewport.width, viewport.height);
	}
	
	/** @return The folder in which screenshot files are saved */
	public static final File getSaveFolder() {
		return saveFolder;
	}
	
	/** Generates a screenshot file.
	 * 
	 * @param fileName The filename to use
	 * 
	 * @return generated File */
	public static final File getNextScreenFile(final String fileName) {
		File imageToSave = new File(saveFolder, fileName.concat(".png"));
		
		// check for duplicates
		int duplicate = 0;
		while(imageToSave.exists()) {
			imageToSave = new File(saveFolder, fileName.concat("_").concat(Integer.toString(++duplicate)).concat(".png"));
		}
		if(!imageToSave.exists()) {
			try {
				imageToSave.createNewFile();
			} catch(IOException ex) {
				System.err.println("Failed to create screenshot file at:\n\"".concat(imageToSave.getAbsolutePath()).concat("\";\nThe following error occurred: ").concat(throwableToStr(ex)));
				System.err.flush();
			}
		}
		return imageToSave;
	}
	
	protected static final String throwableToStr(Throwable e) {
		if(e == null) {
			return "null";
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pr = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
		e.printStackTrace(pr);
		pr.flush();
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}
	
	/** @param getTimeOnly Whether or not time should be included but not date
	 *            as
	 *            well
	 * @param fileSystemSafe Whether or not the returned string will be used in
	 *            the making of a folder or file
	 * @return The resulting string */
	public static String getSystemTime(boolean getTimeOnly, boolean fileSystemSafe) {
		String timeAndDate = "";
		DateFormat dateFormat;
		if(getTimeOnly == false) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy_HH.mm.ss" : "MM/dd/yyyy_HH:mm:ss");
		} else {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss");
		}
		Date date = new Date(System.currentTimeMillis());
		timeAndDate = dateFormat.format(date);
		return timeAndDate;
	}
	
	/** Class used to store screenshot data and save it when run
	 *
	 * @author Brian_Entei */
	public static final class ScreenshotTask implements Runnable {
		
		private static volatile ArrayList<ScreenshotTask> instances = new ArrayList<>();
		
		private final String fileName;
		
		private volatile FloatBuffer imageData;
		private final int width;
		private final int height;
		
		protected static final ArrayList<ScreenshotTask> getInstances() {
			ArrayList<ScreenshotTask> list = new ArrayList<>();
			for(int i = 0; i < instances.size(); i++) {
				list.add(instances.get(i));
			}
			return list;
		}
		
		protected ScreenshotTask(FloatBuffer imageData, int screenWidth, int screenHeight) {
			this.fileName = "screenshot_" + getSystemTime(false, true);// System.currentTimeMillis();
			
			this.imageData = imageData;
			this.width = screenWidth;
			this.height = screenHeight;
			instances.add(this);
		}
		
		@Override
		public final void run() {
			try {
				this.imageData.rewind();
				// fill rgbArray for BufferedImage
				int[] rgbArray = new int[this.width * this.height];
				for(int y = 0; y < this.height; ++y) {
					for(int x = 0; x < this.width; ++x) {
						int r = (int) (this.imageData.get() * 255) << 16;
						int g = (int) (this.imageData.get() * 255) << 8;
						int b = (int) (this.imageData.get() * 255);
						int i = (((this.height - 1) - y) * this.width) + x;
						rgbArray[i] = r + g + b;
					}
				}
				// create and save image
				BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
				image.setRGB(0, 0, this.width, this.height, rgbArray, 0, this.width);
				File outputfile = ScreenshotHelper.getNextScreenFile(this.fileName);
				try {
					ImageIO.write(image, "png", outputfile);
					System.out.println("Saved screenshot as \"" + outputfile.getAbsolutePath() + "\"!");
				} catch(IOException e) {
					System.err.println("Could not save screenshot \"" + outputfile.getAbsolutePath() + "\": " + throwableToStr(e));
				}
			} catch(BufferUnderflowException e) {
				System.err.println("This is what happens when you spam the F2 key!\nLook at what you did: " + throwableToStr(e));//This probably can't even happen due the way I've set this up, but I'll leave it in just in case(I set the F2 button to listen to press state instead of down state and watched the screenshots folder fill up!) 
			}
			instances.remove(this);
			this.imageData = null;//Let the garbage collector eat it
		}
		
	}
	
}
