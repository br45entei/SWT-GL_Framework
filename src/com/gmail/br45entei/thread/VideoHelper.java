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
package com.gmail.br45entei.thread;

import com.gmail.br45entei.util.BufferUtil;
import com.gmail.br45entei.util.BufferUtil.DirectBuffer;
import com.gmail.br45entei.util.CodeUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.swt.graphics.Rectangle;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.lwjgl.opengl.GL11;

/** Helper thread used to capture a frame of graphics data from OpenGL and
 * encode it into a frame of video.
 * 
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt;
 * @see VideoRecordingCallback
 * @see #registerCallback(VideoRecordingCallback)
 * @see #unregisterCallback(VideoRecordingCallback) */
public class VideoHelper extends Thread {
	
	protected static final Picture blankPicture(int width, int height) {
		return Picture.create(width, height, ColorSpace.RGB);
	}
	
	protected static final BufferedImage blankImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	
	/** Interface used to provide a way to listen to video recording related
	 * events.
	 *
	 * @author Brian_Entei
	 * @see VideoHelper#registerCallback(VideoRecordingCallback)
	 * @see VideoHelper#unregisterCallback(VideoRecordingCallback)
	 * @see #onStarted(File)
	 * @see #onFinished(File)
	 * @see #onFailed(Throwable) */
	public static interface VideoRecordingCallback {
		
		/** @param file The video file that is now being recorded to */
		public void onStarted(File file);
		
		/** @param file The newly created video file */
		public void onFinished(File file);
		
		/** @param ex The throwable describing why the recording failed. May be
		 *            <tt><b>null</b></tt>. */
		public void onFailed(Throwable ex);
		
	}
	
	protected static final void slp(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	
	private static final File getNextVideoFile(String fileName) {
		File folder = VideoHelper.getSaveFolder();
		File file = new File(folder, fileName.concat(".mp4"));
		
		// check for duplicates
		int duplicate = 0;
		while(file.exists()) {
			file = new File(folder, fileName.concat("_").concat(Integer.toString(++duplicate)).concat(".mp4"));
		}
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException ex) {
				System.err.println("Failed to create new video file at:\n\"".concat(file.getAbsolutePath()).concat("\";\nThe following error occurred: ").concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage() == null ? "null" : ex.getMessage()));
				System.err.flush();
				//return null;
			}
		}
		return file;
	}
	
	/** @return The folder in which video capture files are saved */
	public static final File getSaveFolder() {
		File folder = new File(new File(CodeUtil.getProperty("user.dir")), "videos");
		folder.mkdirs();
		return folder;
	}
	
	/** Class used to store GL frame data and convert it into a BufferedImage
	 * ready for encoding when run
	 *
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	protected static final class VideoFrameTask {
		
		private static volatile long lastGC = System.currentTimeMillis();
		
		private final VideoHelper thread;
		protected final String fileName;
		
		private volatile float[] data;
		private final int width;
		private final int height;
		private volatile Picture picture;
		
		protected VideoFrameTask(VideoHelper thread, float[] imageData, Rectangle viewport) {
			this.thread = thread;
			this.fileName = "videoCapture_".concat(ScreenshotHelper.getSystemTime(false, true));// System.currentTimeMillis();
			this.width = viewport.width;
			this.height = viewport.height;
			this.data = new float[imageData.length];
			System.arraycopy(imageData, 0, this.data, 0, this.data.length);
			
			this.thread.framesToEncode.add(this);
		}
		
		protected VideoFrameTask(VideoHelper thread, int width, int height, Picture picture) {
			this.thread = thread;
			this.fileName = "videoCapture_".concat(ScreenshotHelper.getSystemTime(false, true));// System.currentTimeMillis();
			
			this.data = null;
			this.width = width;
			this.height = height;
			this.picture = picture;
			this.thread.framesToEncode.add(this);
		}
		
		/** @return The converted image, or <tt><b>null</b></tt> if the
		 *         conversion failed */
		protected final Picture convert() {
			if(this.picture != null) {
				return this.picture;
			}
			try {
				this.picture = Picture.create(this.width, this.height, ColorSpace.RGB);
				
				FloatBuffer imageData = FloatBuffer.wrap(this.data);
				
				// fill rgbArray for BufferedImage
				int[] rgbArray = new int[this.width * this.height];
				for(int y = 0; y < this.height; ++y) {
					for(int x = 0; x < this.width; ++x) {
						int r = (int) (imageData.get() * 255) << 16;
						int g = (int) (imageData.get() * 255) << 8;
						int b = (int) (imageData.get() * 255);
						int i = (((this.height - 1) - y) * this.width) + x;
						rgbArray[i] = r + g + b;
					}
				}
				
				// convert rgbArray to Picture
				byte[] dstData = this.picture.getPlaneData(0);
				
				int off = 0;
				for(int y = 0; y < this.height; y++) {
					for(int x = 0; x < this.width; x++) {
						//dstData[off++] = (byte) ((((int) (imageData.get() * 255)) & 0xff) - 128);
						//dstData[off++] = (byte) ((((int) (imageData.get() * 255)) & 0xff) - 128);
						//dstData[off++] = (byte) ((((int) (imageData.get() * 255)) & 0xff) - 128);
						int rgb1 = rgbArray[(y * this.width) + x];
						dstData[off++] = (byte) (((rgb1 >> 16) & 0xff) - 128);
						dstData[off++] = (byte) (((rgb1 >> 8) & 0xff) - 128);
						dstData[off++] = (byte) ((rgb1 & 0xff) - 128);
					}
				}
				
				return this.picture;
				
				/*// create and save image
				BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
				image.setRGB(0, 0, this.width, this.height, rgbArray, 0, this.width);
				return AWTUtil.fromBufferedImageRGB(image);*/
			} catch(BufferUnderflowException e) {
				return null;
			} finally {
				if(System.currentTimeMillis() - lastGC >= 5000L) {
					//System.gc();
					lastGC = System.currentTimeMillis();
				}
			}
		}
		
	}
	
	private final HashMap<Integer, DirectBuffer<FloatBuffer>> bufferMap = new HashMap<>();
	private final HashMap<Integer, float[]> arrayMap = new HashMap<>();
	
	@SuppressWarnings("resource")
	private final DirectBuffer<FloatBuffer> getFloatBuffer(int size) {
		Integer key = Integer.valueOf(size);
		DirectBuffer<FloatBuffer> buffer = this.bufferMap.get(key);
		if(buffer == null) {
			try {
				buffer = new DirectBuffer<>(size, FloatBuffer.class);
			} catch(OutOfMemoryError ex) {
				this.ex = ex;
				System.err.println("Failed to record video frame: ".concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage() == null ? "null" : ex.getMessage()));
				this.stopAcceptingNewFrames();
				return null;
			}
			this.bufferMap.put(key, buffer);
		} else {
			buffer.rewind();
		}
		return buffer;
	}
	
	private final float[] getFloatArray(int size) {
		Integer key = Integer.valueOf(size);
		float[] buffer = this.arrayMap.get(key);
		if(buffer == null) {
			try {
				buffer = new float[size];
			} catch(OutOfMemoryError ex) {
				this.ex = ex;
				System.err.println("Failed to record video frame: ".concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage() == null ? "null" : ex.getMessage()));
				this.stopAcceptingNewFrames();
				return null;
			}
			this.arrayMap.put(key, buffer);
		}
		return buffer;
	}
	
	/** Captures the specified viewport and encodes it into a frame of video
	 * data.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int)} was not
	 * called prior to calling this method, this method simply returns
	 * <tt>false</tt>.
	 * 
	 * @param x The x coordinate marking the leftmost edge of the desired
	 *            frame
	 * @param y The y coordinate marking the topmost edge of the desired
	 *            frame
	 * @param width The width of the desired frame
	 * @param height The height of the desired frame
	 * @return Whether or not the frame was captured and enqueued for
	 *         encoding */
	public final boolean recordFrame(int x, int y, int width, int height) {
		return this.recordFrame(new Rectangle(x, y, width, height));
	}
	
	/** Captures the specified viewport and enqueues it to be encoded into a
	 * frame of video data.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int)} was not
	 * called prior to calling this method, this method simply returns
	 * <tt>false</tt>.
	 * 
	 * @param viewport The area of graphics data to capture
	 * @return Whether or not the frame was captured and enqueued for
	 *         encoding */
	@SuppressWarnings({"unused", "resource"})
	public final boolean recordFrame(Rectangle viewport) {
		if(this.stopEncoding) {
			return false;
		}
		/*int size = viewport.width * viewport.height * 3;
		FloatBuffer imageData = this.getFloatBuffer(size);
		
		System.out.println("Thread.currentThread().getName(): ".concat(Thread.currentThread().getName()));
		System.out.println("glReadPixels(".concat(Integer.toString(viewport.x)).concat(", ").concat(Integer.toString(viewport.y)).concat(", ").concat(Integer.toString(viewport.width)).concat(", ").concat(Integer.toString(viewport.height)).concat(", GL11.GL_RGB, GL11.GL_FLOAT, createFloatBuffer(").concat(Integer.toString(size)).concat("))"));
		GL11.glReadPixels(viewport.x, viewport.y, viewport.width, viewport.height, GL11.GL_RGB, GL11.GL_FLOAT, imageData);
		System.out.println("Passing save task off...");
		new VideoFrameTask(this, imageData, viewport.width, viewport.height);
		return true;*/
		try {
			int size = viewport.width * viewport.height * 3;
			DirectBuffer<FloatBuffer> imageData = this.getFloatBuffer(size);
			if(imageData == null) {
				return false;
			}
			float[] data = this.getFloatArray(size);
			if(data == null) {
				this.bufferMap.remove(Integer.valueOf(size));
				imageData.close();
				imageData = null;
				return false;
			}
			GL11.glReadPixels(viewport.x, viewport.y, viewport.width, viewport.height, GL11.GL_RGB, GL11.GL_FLOAT, imageData.getBufferView());
			new VideoFrameTask(this, BufferUtil.getData(imageData.getBufferView(), data), viewport);
			return true;
		} catch(OutOfMemoryError ex) {
			ex.printStackTrace();
			this.stopAcceptingNewFrames();
			return false;
		}
		
		/*try {
			if(this.encoder == null) {
				this.openNextFile("videoCapture_".concat(ScreenshotHelper.getSystemTime(false, true)));
			}
			try {
				this.encoder.encodeNativeFrame(picture);
				this.lastFrame = picture;
				return true;
			} catch(IllegalArgumentException ex) {
				if(this.lastFrame != null) {
					this.encoder.encodeNativeFrame(this.lastFrame);
				} else {
					this.encoder.encodeNativeFrame(this.blankPicture);
				}
				return false;
			}
		} catch(IOException ex) {
			this.ex = ex;
			System.err.println("Failed to record video frame: ".concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage() == null ? "null" : ex.getMessage()));
			this.stopAcceptingNewFrames();
			return false;
		}*/
	}
	
	private final boolean[] state;
	
	private volatile boolean stopEncoding = true, stopAcceptingNewFrames = true;
	protected final ConcurrentLinkedDeque<VideoFrameTask> framesToEncode = new ConcurrentLinkedDeque<>();
	private volatile File videoFile = null;
	private volatile Throwable ex;
	private volatile FileOutputStream outputStream = null;
	private volatile SequenceEncoder encoder;
	private volatile int fps, width, height;
	
	private volatile Picture blankPicture = null;
	
	protected final ConcurrentLinkedDeque<VideoRecordingCallback> callbacks = new ConcurrentLinkedDeque<>();
	
	/** Creates a new Video recording helper thread */
	public VideoHelper() {
		this(new boolean[] {true});
	}
	
	/** Creates a new Video recording helper thread
	 * 
	 * @param state A boolean array containing at least one boolean which will
	 *            let this thread know that it should continue to run */
	public VideoHelper(boolean[] state) {
		super("VideoHelperThread");
		this.setPriority(Thread.MAX_PRIORITY - 1);
		this.setDaemon(false);
		this.state = state;
	}
	
	/** @param callback The callback to register
	 * @return Whether or not the given callback was registered successfully
	 *         with this {@link VideoHelper} */
	public final boolean registerCallback(VideoRecordingCallback callback) {
		if(this.callbacks.contains(callback)) {
			return false;
		}
		return this.callbacks.add(callback);
	}
	
	/** @param callback The callback to unregister
	 * @return True if the given callback was previously registered with this
	 *         {@link VideoHelper} */
	public final boolean unregisterCallback(VideoRecordingCallback callback) {
		boolean contained = false;
		while(this.callbacks.remove(callback)) {
			contained = true;
		}
		return contained;
	}
	
	/** Tells this VideoHelper to start recording.
	 * 
	 * @param fps The fps to record at.
	 * @param width The width of the screen to record at.
	 * @param height The height of the screen to record at.
	 * @throws IllegalArgumentException Thrown if any of the arguments are below
	 *             <code>1</code>.
	 * @throws IllegalStateException Thrown if an {@link OutOfMemoryError}
	 *             occurred while initializing internal video buffers */
	public final void startRecording(int fps, int width, int height) throws IllegalArgumentException, IllegalStateException {
		if(fps <= 0) {
			throw new IllegalArgumentException("FPS must be greater than zero! FPS given: ".concat(Integer.toString(fps)));
		}
		if(width <= 0) {
			throw new IllegalArgumentException("Width must be greater than zero! Width given: ".concat(Integer.toString(width)));
		}
		if(height <= 0) {
			throw new IllegalArgumentException("Height must be greater than zero! Height given: ".concat(Integer.toString(height)));
		}
		if(this.isRecording()) {
			return;
		}
		this.fps = fps;
		this.width = width;
		this.height = height;
		this.stopEncoding = this.stopAcceptingNewFrames = false;
		int size = width * height * 3;
		@SuppressWarnings("resource")
		DirectBuffer<FloatBuffer> db = this.getFloatBuffer(size);
		if(db == null) {
			throw new IllegalStateException("Out of memory!");
		}
		if(this.getFloatArray(size) == null) {
			@SuppressWarnings("resource")
			DirectBuffer<FloatBuffer> shutUpEclipseThisISThe_db = this.bufferMap.remove(Integer.valueOf(size));
			db.close();
			shutUpEclipseThisISThe_db.close();
			throw new IllegalStateException("Out of memory!");
		}
	}
	
	/** Tells this VideoHelper to stop recording if it is doing so. */
	public final void stopEncoding() {
		this.stopEncoding = this.stopAcceptingNewFrames = true;
	}
	
	/** @return True if this VideoHelper is currently recording cached frames
	 *         (irrespective of whether or not this video helper
	 *         {@link #shouldBeRecording()}) */
	public final boolean isRecording() {
		return this.encoder != null && this.outputStream != null;// && this.videoFile != null;
	}
	
	/** Tells this VideoHelper to stop accepting new frames from the various
	 * {@link #recordFrame(Rectangle) recordFrame(...)} methods. */
	public final void stopAcceptingNewFrames() {
		this.stopAcceptingNewFrames = true;
	}
	
	/** Returns Whether or not this VideoHelper is accepting new frames from the
	 * various {@link #recordFrame(Rectangle) recordFrame(...)} methods.
	 * 
	 * @return Whether or not this VideoHelper is accepting new frames */
	public final boolean isAcceptingNewFrames() {
		return !this.stopEncoding && !this.stopAcceptingNewFrames && this.isRecording();
	}
	
	/** Tells this VideoHelper to start accepting new frames from the various
	 * {@link #recordFrame(Rectangle) recordFrame(...)} methods. */
	public final void startAcceptingNewFrames() {
		if(this.stopEncoding) {
			return;
		}
		this.stopAcceptingNewFrames = false;
	}
	
	/** Returns the number of frames that this {@link VideoHelper} has left to
	 * encode.
	 * 
	 * @return The number of frames that this {@link VideoHelper} has left to
	 *         encode */
	public final int getNumFramesLeftToEncode() {
		return this.framesToEncode.size();
	}
	
	/** @return Whether or not this VideoHelper has been told to start recording
	 *         (and hasn't been told to stop yet) */
	public final boolean shouldBeRecording() {
		return !this.stopEncoding && !this.stopAcceptingNewFrames;
	}
	
	private final void closeCurrentFile() {
		this.stopEncoding = this.stopAcceptingNewFrames = true;
		try {
			if(this.encoder != null) {
				this.encoder.finish();
				this.encoder = null;
			}
			if(this.outputStream != null) {
				this.outputStream.flush();
				this.outputStream.close();
				this.outputStream = null;
			}
		} catch(IOException ex) {
			this.ex = ex;
			System.err.println("Failed to finalize the latest video file: ".concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage() == null ? "null" : ex.getMessage()));
			System.err.flush();
			this.encoder = null;
			if(this.outputStream != null) {
				try {
					this.outputStream.flush();
				} catch(IOException ignored) {
				}
				try {
					this.outputStream.close();
				} catch(IOException ignored) {
				}
				this.outputStream = null;
			}
			this.videoFile = null;
		} finally {
			if(this.videoFile != null) {
				System.out.println("Saved video capture as \"".concat(this.videoFile.getAbsolutePath()).concat("\"!"));
				System.out.flush();
				
				for(VideoRecordingCallback callback : this.callbacks) {
					try {
						callback.onFinished(this.videoFile);
					} catch(Throwable ex) {
						if(ex instanceof ThreadDeath) {
							throw ex;
						}
						ex.printStackTrace(System.err);
						System.err.flush();
						if(ex instanceof InterruptedException) {
							Thread.currentThread().interrupt();
							continue;
						}
					}
				}
				this.videoFile = null;
			} else {
				for(VideoRecordingCallback callback : this.callbacks) {
					try {
						callback.onFailed(this.ex);
					} catch(Throwable ex) {
						if(ex instanceof ThreadDeath) {
							throw ex;
						}
						ex.printStackTrace(System.err);
						System.err.flush();
						if(ex instanceof InterruptedException) {
							Thread.currentThread().interrupt();
							continue;
						}
					}
				}
			}
			
			for(Integer key : this.bufferMap.keySet()) {
				@SuppressWarnings("resource")
				DirectBuffer<?> db = this.bufferMap.get(key);
				if(db != null) {
					db.close();
					continue;
				}
			}
			this.bufferMap.clear();
		}
	}
	
	private final void openNextFile(String fileName) throws IOException {
		this.closeCurrentFile();
		this.stopEncoding = this.stopAcceptingNewFrames = false;
		this.videoFile = getNextVideoFile(fileName);
		this.outputStream = new FileOutputStream(this.videoFile);
		// for Android use: AndroidSequenceEncoder
		@SuppressWarnings("resource")
		SeekableByteChannel channel = NIOUtils.writableChannel(this.videoFile);
		this.encoder = new SequenceEncoder(channel, Rational.R(this.fps, 1), Format.MOV, Codec.H264, null);
		channel = null;// Shut up Eclipse, it gets closed later! >:(
		//this.encoder = AWTSequenceEncoder.createSequenceEncoder(this.videoFile, this.fps);
		this.blankPicture = blankPicture(this.width, this.height);
		
		for(VideoRecordingCallback callback : this.callbacks) {
			try {
				callback.onStarted(this.videoFile);
			} catch(Throwable ex) {
				if(ex instanceof ThreadDeath) {
					throw ex;
				}
				ex.printStackTrace(System.err);
				System.err.flush();
				if(ex instanceof InterruptedException) {
					Thread.currentThread().interrupt();
					continue;
				}
			}
		}
	}
	
	/** 'Captures' a solid black frame of video and enqueues it to be encoded
	 * into a frame of video data.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int)} was not
	 * called prior to calling this method, this method simply returns
	 * <tt>false</tt>.
	 * 
	 * @return Whether or not the blank frame was enqueued for encoding */
	@SuppressWarnings("unused")
	public final boolean recordBlankFrame() {
		if(this.stopEncoding || this.stopAcceptingNewFrames) {
			return false;
		}
		try {
			if(this.blankPicture == null) {
				this.blankPicture = blankPicture(this.width, this.height);
			}
			new VideoFrameTask(this, this.width, this.height, this.blankPicture);
			return true;
		} catch(OutOfMemoryError ex) {
			ex.printStackTrace();
			this.stopAcceptingNewFrames();
			return false;
		}
	}
	
	/** 'Captures' a solid black frame of video and enqueues it to be encoded
	 * into a frame of video data.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;If {@link #startRecording(int, int, int)} was not
	 * called prior to calling this method, this method simply returns
	 * <tt>false</tt>.
	 * 
	 * @param width The desired width of the blank frame
	 * @param height The desired height of the blank frame
	 * @return Whether or not the blank frame was enqueued for encoding */
	@SuppressWarnings("unused")
	public final boolean recordBlankFrame(int width, int height) {
		if(this.stopEncoding || this.stopAcceptingNewFrames) {
			return false;
		}
		try {
			if(this.blankPicture == null) {
				this.blankPicture = blankPicture(this.width, this.height);
			}
			new VideoFrameTask(this, Math.min(Math.max(1, width), this.width), Math.min(Math.max(1, height), this.height), this.blankPicture);
			return true;
		} catch(OutOfMemoryError ex) {
			ex.printStackTrace();
			this.stopAcceptingNewFrames();
			return false;
		}
	}
	
	@Override
	public void run() {
		if(!this.state[0]) {//wait for the game to start, otherwise the video thread will just end before the game even starts(derp)
			while(!this.state[0]) {
				slp(10L);
			}
		}
		Picture lastFrame = null;
		VideoFrameTask frame;
		while(true) {
			if(!this.state[0]) {
				this.stopEncoding();
				if(this.framesToEncode.isEmpty()) {
					break;
				}
			}
			if((frame = this.framesToEncode.poll()) != null) {
				if(this.encoder == null) {
					try {
						this.openNextFile(frame.fileName);
					} catch(IOException ex) {
						this.closeCurrentFile();
						this.framesToEncode.clear();
						System.err.println("Failed to open the next video file for encoding: ".concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage() == null ? "null" : ex.getMessage()));
						System.err.flush();
						continue;
					}
				}
				Picture image = frame.convert();
				if(image == null || image.getWidth() != this.width || image.getHeight() != this.height) {
					try {
						this.encoder.encodeNativeFrame(lastFrame = (lastFrame != null ? lastFrame : this.blankPicture));//this.blankPicture);//Frame wasn't null, but we were unable to convert it, so we'll encode a blank frame instead...
					} catch(IOException ex) {
					}
					continue;
				}
				try {
					this.encoder.encodeNativeFrame(lastFrame = image);//AWTUtil.fromBufferedImageRGB(image));
				} catch(IOException ex) {
				} catch(IllegalStateException ex) {
					this.closeCurrentFile();
					this.framesToEncode.clear();
				}
				continue;
			} else if(!this.stopEncoding && this.isRecording() && this.stopAcceptingNewFrames) {
				this.closeCurrentFile();
			}
			if(this.stopEncoding && (this.outputStream != null || this.encoder != null)) {
				this.closeCurrentFile();
				this.framesToEncode.clear();
			}
			slp(1L);
		}
		this.closeCurrentFile();
	}
	
}
