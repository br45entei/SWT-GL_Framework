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
package com.gmail.br45entei.audio;

import com.gmail.br45entei.lwjgl.natives.LWJGL_Natives;
import com.gmail.br45entei.util.BufferUtil;
import com.gmail.br45entei.util.CodeUtil;
import com.gmail.br45entei.util.ResourceUtil;
import com.gmail.br45entei.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.io.FilenameUtils;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.util.WaveData;

import static org.lwjgl.openal.AL.createCapabilities;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_DIRECTION;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO8;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO8;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_INITIAL;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_ORIENTATION;
import static org.lwjgl.openal.AL10.AL_PAUSED;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_STOPPED;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alListener3f;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.AL10.alListenerfv;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceRewind;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.ALC.createCapabilities;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

/** @since 1.0
 * @author Brian_Entei */
public class SoundManager extends Thread {
	
	/** @return An ArrayList containing all of the package names(or folder
	 *         names)
	 *         in the music package(which is "assets/music"). */
	public static final ArrayList<String> getAllPackagesInMusicPackage() {
		final ArrayList<String> packageContents;
		try {
			packageContents = ResourceUtil.getAllPackagesInPackage("assets/music");
		} catch(NullPointerException e) {
			//if(Main.isDebugModeOn()) {
			//	e.printStackTrace();
			//}
			return null;
		}
		return packageContents;
	}
	
	/** @return An ArrayList containing all of the package names(or folder
	 *         names)
	 *         in the default sound package(which is "assets/sounds"). */
	public static final ArrayList<String> getAllPackagesInSoundPackage() {
		final ArrayList<String> packageContents;
		try {
			packageContents = ResourceUtil.getAllPackagesInPackage("assets/sounds");
		} catch(NullPointerException e) {
			//if(Main.isDebugModeOn()) {
			//	e.printStackTrace();
			//}
			return null;
		}
		return packageContents;
	}
	
	/** @return An ArrayList containing all of the sound resource names(or file
	 *         names) in the music package(which is "assets/music"). */
	public static final ArrayList<String> getAllSoundResourcePathsFromMusicPackage() {
		return getAllSoundResourcePathsFromPackage("assets/music");
	}
	
	/** @return An ArrayList containing all of the sound resource names(or file
	 *         names) in the default sound package(which is "assets/sounds"). */
	public static final ArrayList<String> getAllSoundResourcePathsFromSoundPackage() {
		return getAllSoundResourcePathsFromPackage("assets/sounds");
	}
	
	/** @param pkgName The package to browse
	 * @return An ArrayList containing all of the sound resource names(or file
	 *         names) in the given package if it exists, or null otherwise. If
	 *         the package exists but contains no sounds, an empty list is
	 *         returned. */
	public static final ArrayList<String> getAllSoundResourcePathsFromPackage(String pkgName) {
		final ArrayList<String> rtrn = new ArrayList<>();
		final ArrayList<String> packageContents;
		try {
			packageContents = ResourceUtil.getAllFilesInPackage(pkgName);
		} catch(NullPointerException e) {
			//if(Main.isDebugModeOn()) {
			//	e.printStackTrace();
			//}
			return null;
		}
		for(String resPath : packageContents) {
			if(StringUtil.endsWithIgnoreCase(resPath, "ogg") || StringUtil.endsWithIgnoreCase(resPath, "wav") || StringUtil.endsWithIgnoreCase(resPath, "flac")) {
				rtrn.add("/" + pkgName + "/" + resPath);
			}
		}
		return rtrn;
	}
	
	/** Create a new SoundManager thread.<br>
	 * <br>
	 * <b>Note:</b>&nbsp;It is recommended that only one SoundManager thread be
	 * created and used at any given time, preferably for the life of the
	 * application. */
	public SoundManager() {
		soundThread = this;
		this.setName("SoundManagerThread");
		this.setDaemon(false);
	}
	
	protected static volatile SoundManager soundThread = null;
	protected volatile String deviceName;
	protected volatile long device;
	protected int[] contextAttributes = {SOFTHRTF.ALC_HRTF_SOFT, ALC10.ALC_TRUE, /* request HRTF */
			SOFTHRTF.ALC_HRTF_ID_SOFT, /* request HRTF ID */
			ALC10.ALC_FREQUENCY, 44100, /* request 44100 hertz frequency for HRTF */
			/* end of list */ 0};//{0};
	protected volatile long context;
	protected volatile org.lwjgl.openal.ALCCapabilities alcCaps;
	protected volatile org.lwjgl.openal.ALCapabilities alCaps;
	
	protected final ConcurrentLinkedDeque<Source> soundsToCreate = new ConcurrentLinkedDeque<>();
	protected final ConcurrentHashMap<Source, Sound> createdSounds = new ConcurrentHashMap<>();
	
	protected final ConcurrentLinkedDeque<Sound> soundsToDestroy = new ConcurrentLinkedDeque<>();
	
	private volatile float volume = 1f;
	private volatile float pitch = 1f;
	
	public volatile float x = 0;
	public volatile float y = 0;
	public volatile float z = 0;
	
	public volatile float ofx = 0;
	public volatile float ofy = 0;
	public volatile float ofz = -1;
	public volatile float oux = 0;
	public volatile float ouy = 1;
	public volatile float ouz = 0;
	
	public volatile float vx = 0;
	public volatile float vy = 0;
	public volatile float vz = 0;
	
	public final float getListenerVolume() {
		return this.volume;
	}
	
	public final SoundManager setListenerVolume(float volume) {
		this.volume = volume > 1f ? 1f : (volume < 0f ? 0f : volume);
		return this;
	}
	
	public final SoundManager setListenerPitch(float pitch) {
		this.pitch = pitch < 0f ? 0f : pitch;
		return this;
	}
	
	public final SoundManager setListenerOrientation(float[] forward, float[] up) {
		this.ofx = -forward[0];
		this.ofy = -forward[1];
		this.ofz = -forward[2];
		this.oux = -up[0];
		this.ouy = -up[1];
		this.ouz = -up[2];
		return this;
	}
	
	public final SoundManager setListenerPosition(float[] xyz) {
		this.x = xyz[0];
		this.y = xyz[1];
		this.z = xyz[2];
		return this;
	}
	
	public final SoundManager setListenerVelocity(float[] xyz) {
		this.vx = xyz[0];
		this.vy = xyz[1];
		this.vz = xyz[2];
		return this;
	}
	
	public final SoundManager loadSound(Source data) {
		if(this.soundsToCreate.contains(data)) {
			return this;
		}
		this.soundsToCreate.addLast(data);
		return this;
	}
	
	public final Sound getOrCreateSound(String resourcePath) {
		Sound check = Sound.getByPath(resourcePath);
		if(check != null) {
			return check;
		}
		return this.createSound(resourcePath);
	}
	
	public final Sound createSound(String resourcePath) {
		try {
			return this.createSound(new Source(resourcePath));
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public final Sound createSound(Source data) {
		this.loadSound(data);
		Sound sound;
		while((sound = this.createdSounds.get(data)) == null) {
			CodeUtil.sleep(10L);
		}
		this.createdSounds.remove(data);
		return sound;
	}
	
	public final Sound createSound(Source data, Runnable loopCode) {
		this.loadSound(data);
		Sound sound;
		while((sound = this.createdSounds.get(data)) == null) {
			try {
				loopCode.run();
			} catch(Throwable e) {
				e.printStackTrace();
				break;
			}
		}
		this.createdSounds.remove(data);
		return sound;
	}
	
	public static final ByteBuffer getData(InputStream in, boolean closeStream) throws IOException {
		if(in == null) {
			throw new IOException(new NullPointerException("Inputstream cannot be null!"));
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read;
			byte[] buf = new byte[4096];
			while((read = in.read(buf)) != -1) {
				baos.write(buf, 0, read);
			}
			byte[] data = baos.toByteArray();
			baos = null;
			return ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder()).put(data).rewind();
		} finally {
			if(closeStream) {
				try {
					in.close();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static final ShortBuffer stb_vorbis_decode_stream(InputStream in, IntBuffer channels, IntBuffer sampleRate) throws IOException {
		ShortBuffer decoded = stb_vorbis_decode_memory(getData(in, true), channels, sampleRate);
		System.gc();
		return decoded;
	}
	
	public static final ShortBuffer stb_vorbis_decode_resource(String resourcePath, IntBuffer channels, IntBuffer sampleRate) throws IOException {
		ShortBuffer decoded;
		try(InputStream in = SoundManager.class.getResourceAsStream(resourcePath)) {
			if(in == null) {
				throw new IOException("Resource \"" + resourcePath + "\" does not exist!");
			}
			decoded = stb_vorbis_decode_stream(in, channels, sampleRate);
		}
		return decoded;
	}
	
	protected volatile boolean running = false;
	protected volatile long callTime;
	protected volatile Thread caller;
	protected volatile Throwable trouble = null;
	
	/** Returns and clears any exception that has been thrown by this
	 * {@link SoundManager}.
	 * 
	 * @return Any exception that has been thrown by this SoundManager */
	public final Throwable getTrouble() {
		Throwable trouble = this.trouble;
		this.trouble = null;
		return trouble;
	}
	
	@Override
	@Deprecated
	public synchronized final void start() {
		this.startRunning();
	}
	
	/** Starts this new sound manager.
	 * 
	 * @return This sound manager */
	public final SoundManager startRunning() {
		this.callTime = System.currentTimeMillis();
		this.caller = Thread.currentThread();
		super.start();
		return this;
	}
	
	@Override
	public final void run() {
		this.initializeAL();
		this.running = true;
		for(String sound : getAllSoundResourcePathsFromSoundPackage()) {
			System.out.println(sound);
		}
		for(String music : getAllSoundResourcePathsFromMusicPackage()) {
			System.out.println(music);
		}
		while(this.running && this.caller.isAlive()) {
			this.updateAL();
			CodeUtil.sleep(10L);
		}
		this.running = false;
		this.destroyAL();
	}
	
	public final void stopRunning() {
		if(!this.isAlive()) {
			return;
		}
		this.running = false;
		while(this.isAlive()) {
			CodeUtil.sleep(10L);
		}
	}
	
	protected final void initializeAL() {
		//Initialization
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		this.deviceName = defaultDeviceName;
		this.device = alcOpenDevice(defaultDeviceName);
		this.context = alcCreateContext(this.device, this.contextAttributes);
		
		int response = ALC10.alcGetInteger(this.device, SOFTHRTF.ALC_HRTF_STATUS_SOFT);
		
		System.out.println("Using \"" + this.deviceName + "\" for sound device.");
		System.out.println("HRTF STATE: 0x" + Integer.toHexString(response) + "(" + (response == SOFTHRTF.ALC_HRTF_ENABLED_SOFT) + ")");
		System.out.println("Frequency: " + ALC10.alcGetInteger(this.device, ALC10.ALC_FREQUENCY));
		
		System.out.println(ALC10.alcGetString(this.device, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT));
		
		alcMakeContextCurrent(this.context);
		this.alcCaps = createCapabilities(this.device);
		this.alCaps = createCapabilities(this.alcCaps);
	}
	
	public static final void main(String[] args) {
		LWJGL_Natives.loadNatives();
		//System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir") + File.separator + "natives");
		/*initializeAL();
		Sound sound;
		try {
			//sound = new Sound("https://www.w3schools.com/html/horse.ogg?");
			sound = new Sound("explosion_loud.ogg", true);
		} catch(IOException e) {
			throw new RuntimeException("Unable to read resource sound!", e);
		}
		//Play the sound
		sound.setPitch(1f).setVolume(1f).play();
		while(true) {
			updateAL();
			if(sound.isActive()) {
				TickUtil.sleep(1);
				continue;
			}
			if(sound.isRewound()) {
				System.err.println("Huh?");
			}
			if(sound.isStopped()) {
				System.out.println("The sound has stopped.");
			}
			break;
		}
		//Terminate OpenAL
		destroyAL();*/
		SoundManager manager = new SoundManager().startRunning();
		Sound sound;
		try {
			//sound = manager.createSound(new Source("https://www.w3schools.com/html/horse.ogg?"));
			//sound = manager.createSound(new Source("explosion_loud.ogg"));
			//sound = manager.createSound(new Source("/assets/sounds/button_click.ogg"));
			sound = manager.createSound(new Source("/assets/sounds/stone/break2.ogg"));
		} catch(IOException e) {
			throw new RuntimeException("Unable to read resource sound!", e);
		}
		if(sound == Sound.NULL) {
			Throwable trouble = manager.getTrouble();
			System.err.println(trouble == null ? "Unable to load sound: null was returned" : StringUtil.throwableToStr(trouble, "\r\n"));
		}
		sound.play();
		CodeUtil.sleep(500L);
		if(sound.isStopped()) {
			System.err.println("Either the sound was shorter than half a second, or there was an error loading the sound. Is the sample rate too high?");
			manager.stopRunning();
			return;
		}
		if(!sound.isPlaying()) {
			System.out.println("The sound is not playing yet. Source state: " + sound.getState());
		}
		while(!sound.isPlaying()) {
			if(sound.isStopped()) {
				sound.rewind();
			}
			if(sound.isRewound()) {
				sound.play();
			}
			CodeUtil.sleep(10);
		}
		System.out.println("The sound is playing.");
		while(sound.isActive()) {
			CodeUtil.sleep(10);
		}
		System.out.println("The sound has stopped.");
		manager.stopRunning();
	}
	
	protected final void updateAL() {
		alListenerf(AL_GAIN, this.volume);
		alListener3f(AL_POSITION, -this.x, -this.y, -this.z);
		alListenerfv(AL_ORIENTATION, new float[] {this.ofx, this.ofy, this.ofz, this.oux, this.ouy, this.ouz});
		alListener3f(AL_VELOCITY, this.vx, this.vy, this.vz);
		for(Source data : this.soundsToCreate) {
			if(this.createdSounds.get(data) == null) {
				try {
					this.createdSounds.put(data, new Sound(data, true));
				} catch(IllegalArgumentException e) {
					this.trouble = e;
					this.createdSounds.put(data, Sound.NULL);
				}
			}
			this.soundsToCreate.remove(data);
		}
		while(this.soundsToDestroy.peek() != null) {
			this.soundsToDestroy.pop().destroy();
		}
		Sound.alUpdateAll(this.pitch);
	}
	
	protected final void destroyAL() {
		Sound.destroyAll();
		alcDestroyContext(this.context);
		alcCloseDevice(this.device);
		//System.err.println("AL destroyed.");
		soundThread = soundThread == this ? null : soundThread;
	}
	
	public static final class Sound {
		
		public static final Sound NULL = new Sound();
		
		protected static final ConcurrentLinkedDeque<Sound> instances = new ConcurrentLinkedDeque<>();
		
		public static final Sound getByPath(String resourcePath) {
			resourcePath = Source.isURL(resourcePath) ? resourcePath : Sound.fullPath(resourcePath);
			for(Sound sound : instances) {
				if(sound.data.path.equals(resourcePath)) {
					return sound;
				}
			}
			return null;
		}
		
		protected static final void alUpdateAll(float pitch) {
			for(Sound sound : instances) {
				sound.alUpdate(pitch);
			}
		}
		
		/** Destroys all loaded sounds */
		public static final void destroyAll() {
			for(Sound sound : instances) {
				sound.destroy();
			}
		}
		
		private static final InputStream fromFile(File file) {
			try {
				return new FileInputStream(file);
			} catch(IOException e) {
				System.err.print("Unable to load sound from file \"" + file.getAbsolutePath() + "\": ");
				e.printStackTrace();
				return null;
			}
		}
		
		/** The source of this sound */
		public final Source data;
		
		protected volatile int state = AL_INITIAL;
		
		/** Whether or not this sound is a stereo or mono sound */
		public final boolean stereo;
		/** This sound's source */
		protected final int source;
		/** This sound's buffer */
		protected final int buffer;
		
		protected final int sampleRate, bitsPerSample, channels, format;
		
		private volatile float pitch = 1;
		private volatile float volume = 1f;
		
		private volatile float x = 0;
		private volatile float y = 0;
		private volatile float z = 0;
		
		private volatile float dx = 0;
		private volatile float dy = 0;
		private volatile float dz = -1;
		
		private volatile float vx = 0;
		private volatile float vy = 0;
		private volatile float vz = 0;
		
		private volatile boolean loop;
		private volatile boolean play;
		private volatile boolean pause;
		private volatile boolean stop;
		private volatile boolean rewind;
		
		public String getPath() {
			return this == NULL ? "null" : this.data.toString();
		}
		
		public String toStringVerbose() {
			StringBuilder builder = new StringBuilder();
			builder.append("Sound [").append(this.data).append("]: stereo=").append(this.stereo).append(", pitch=").append(this.pitch).append(", volume=").append(this.volume).append(", playing=").append(this.isPlaying()).append(", paused=").append(this.isPaused()).append(", stopped=").append(this.isStopped()).append(", rewound=").append(this.isRewound()).append("]");
			return builder.toString();
		}
		
		public static final String fullPath(String resourcePath) {
			if(resourcePath.startsWith("/")) {
				return resourcePath;
			}
			resourcePath = resourcePath.startsWith("sounds/") || resourcePath.startsWith("music/") ? resourcePath : "sounds/" + resourcePath;
			resourcePath = resourcePath.startsWith("assets/") ? resourcePath : "assets/" + resourcePath;
			System.out.println("/" + resourcePath);
			return "/" + resourcePath;
		}
		
		private Sound() {
			this.data = null;
			this.source = -1;
			this.buffer = -1;
			this.stereo = false;
			this.sampleRate = -1;
			this.bitsPerSample = -1;
			this.channels = 0;
			this.format = -1;
		}
		
		@SuppressWarnings("resource")
		public Sound(File file) throws IOException {
			this(new Source(fromFile(file), FilenameUtils.normalize(file.getAbsolutePath())));
		}
		
		public Sound(String resourcePath) throws IOException {
			this(resourcePath, false);
		}
		
		public Sound(String resourcePath, boolean discardBuffer) throws IOException {
			this(new Source(resourcePath), discardBuffer);
		}
		
		public Sound(Source data) {
			this(data, false);
		}
		
		public Sound(Source data, boolean discardBuffer) {
			this.data = data;
			System.out.println("Loading sound \"" + this.data.toString() + "\"...");
			//Request space for the buffer
			this.buffer = alGenBuffers();
			switch(this.data.formatExt) {
			case "ogg":
				//Allocate space to store return information from the function
				stackPush();
				IntBuffer channelsBuffer = stackMallocInt(1);
				stackPush();
				IntBuffer sampleRateBuffer = stackMallocInt(1);
				ShortBuffer rawAudioBuffer;
				rawAudioBuffer = stb_vorbis_decode_memory(this.data.buffer, channelsBuffer, sampleRateBuffer);
				//Retrieve the extra information that was stored in the buffers by the function
				this.channels = channelsBuffer.get();
				this.sampleRate = sampleRateBuffer.get();
				this.bitsPerSample = -1;
				//Free the space we allocated earlier
				stackPop();
				stackPop();
				//Find the correct OpenAL format
				if(this.channels == 1) {
					this.format = AL_FORMAT_MONO16;
				} else if(this.channels >= 2) {
					this.format = AL_FORMAT_STEREO16;
				} else {
					this.format = -1;
				}
				this.stereo = this.format == AL_FORMAT_STEREO16;
				//Send the data to OpenAL
				alBufferData(this.buffer, this.format, rawAudioBuffer, this.sampleRate);
				//Free the memory allocated by STB
				free(rawAudioBuffer);
				break;
			case "wav":
				WaveData wav = WaveData.create(this.data.buffer);
				this.stereo = wav.format == AL_FORMAT_STEREO16 || wav.format == AL_FORMAT_STEREO8;
				this.sampleRate = wav.samplerate;
				this.bitsPerSample = wav.bitspersample;
				this.channels = wav.channels;
				this.format = wav.format;
				alBufferData(this.buffer, wav.format, wav.data, wav.samplerate);
				wav.dispose();
				break;
			case "flac":
				FLACDecoder decoder = new FLACDecoder(data.getInputStream());
				FlacData fData = new FlacData();
				decoder.addPCMProcessor(fData);
				try {
					decoder.decode();
				} catch(IOException e) {
					e.printStackTrace();
					this.stereo = false;
					alDeleteSources(this.buffer);
					this.source = -1;
					this.sampleRate = -1;
					this.bitsPerSample = -1;
					this.channels = 0;
					this.format = -1;
					fData.dispose();
					decoder.removePCMProcessor(fData);
					fData = null;
					System.gc();
					return;
				}
				StreamInfo info = decoder.getStreamInfo();
				this.sampleRate = info.getSampleRate();
				this.channels = info.getChannels();
				this.bitsPerSample = info.getBitsPerSample();
				if(this.bitsPerSample > 16) {
					alDeleteSources(this.buffer);
					fData.dispose();
					decoder.removePCMProcessor(fData);
					fData = null;
					System.gc();
					throw new IllegalArgumentException(String.format("Unsupported bits per sample: %s", Integer.toString(this.bitsPerSample)));
				}
				this.format = this.channels == 1 ? (this.bitsPerSample == 16 ? AL_FORMAT_MONO16 : AL_FORMAT_MONO8) : (this.bitsPerSample == 16 ? AL_FORMAT_STEREO16 : AL_FORMAT_STEREO8);
				this.stereo = this.format == AL_FORMAT_STEREO16 || this.format == AL_FORMAT_STEREO8;
				alBufferData(this.buffer, this.format, fData.getDirectBuffer(), this.sampleRate);
				fData.dispose();
				decoder.removePCMProcessor(fData);
				fData = null;
				break;
			default:
				alDeleteSources(this.buffer);
				throw new IllegalArgumentException(String.format("Unsupported format extension: \"%s\"!", this.data.formatExt));
			}
			//Request a source
			this.source = alGenSources();
			//Assign the sound we just loaded to the source
			alSourcei(this.source, AL_BUFFER, this.buffer);
			instances.add(this);
			if(discardBuffer) {
				this.data.buffer = null;
			}
			System.gc();
			System.out.println("Loaded sound \"" + this.data.toString() + "\".");
		}
		
		protected final void alUpdate(float pitch) {
			alSourcef(this.source, AL_GAIN, this.volume);
			alSourcef(this.source, AL_PITCH, this.pitch * pitch);
			alSource3f(this.source, AL_POSITION, -this.x, -this.y, -this.z);
			alSource3f(this.source, AL_DIRECTION, this.dx, this.dy, this.dz);
			alSource3f(this.source, AL_VELOCITY, -this.vx, -this.vy, -this.vz);
			alSourcei(this.source, AL_LOOPING, this.loop ? AL_TRUE : AL_FALSE);
			if(this.play) {
				alSourcePlay(this.source);
				this.play = false;
			}
			if(this.pause) {
				alSourcePause(this.source);
				this.pause = false;
			}
			if(this.stop) {
				alSourceStop(this.source);
				this.stop = false;
			}
			if(this.rewind) {
				alSourceRewind(this.source);
				this.rewind = false;
			}
			this.state = alGetSourcei(this.source, AL_SOURCE_STATE);
		}
		
		public final float[] getPosition() {
			return new float[] {this.x, this.y, this.z};
		}
		
		public final float getPositionX() {
			return this.x;
		}
		
		public final float getPositionY() {
			return this.y;
		}
		
		public final float getPositionZ() {
			return this.z;
		}
		
		public final Sound setPositionX(float x) {
			this.x = x;
			return this;
		}
		
		public final Sound setPositionY(float y) {
			this.y = y;
			return this;
		}
		
		public final Sound setPositionZ(float z) {
			this.z = z;
			return this;
		}
		
		public final Sound setPosition(float[] position) {
			this.x = position[0];
			this.y = position[1];
			this.z = position[2];
			return this;
		}
		
		public final float[] getDirection() {
			return new float[] {this.dx, this.dy, this.dz};
		}
		
		public final float getDirectionX() {
			return this.dx;
		}
		
		public final float getDirectionY() {
			return this.dy;
		}
		
		public final float getDirectionZ() {
			return this.dz;
		}
		
		public final Sound setDirectionX(float dx) {
			this.dx = dx;
			return this;
		}
		
		public final Sound setDirectionY(float dy) {
			this.dy = dy;
			return this;
		}
		
		public final Sound setDirectionZ(float dz) {
			this.dz = dz;
			return this;
		}
		
		public final Sound setDirection(float[] direction) {
			this.dx = direction[0];
			this.dy = direction[1];
			this.dz = direction[2];
			return this;
		}
		
		public final float[] getVelocity() {
			return new float[] {this.vx, this.vy, this.vz};
		}
		
		public final float getVelocityX() {
			return this.vx;
		}
		
		public final float getVelocityY() {
			return this.vy;
		}
		
		public final float getVelocityZ() {
			return this.vz;
		}
		
		public final Sound setVelocityX(float vx) {
			this.vx = vx;
			return this;
		}
		
		public final Sound setVelocityY(float vy) {
			this.vy = vy;
			return this;
		}
		
		public final Sound setVelocityZ(float vz) {
			this.vz = vz;
			return this;
		}
		
		public final Sound setVelocity(float[] velocity) {
			this.vx = velocity[0];
			this.vy = velocity[1];
			this.vz = velocity[2];
			return this;
		}
		
		/** Plays this sound
		 * 
		 * @return This sound */
		public final Sound play() {
			this.play = true;
			return this;
		}
		
		/** Pauses this sound
		 * 
		 * @return This sound */
		public final Sound pause() {
			this.pause = true;
			return this;
		}
		
		/** Stops this sound
		 * 
		 * @return This sound */
		public final Sound stop() {
			this.stop = true;
			return this;
		}
		
		/** Rewinds this sound
		 * 
		 * @return This sound */
		public final Sound rewind() {
			this.rewind = true;
			return this;
		}
		
		public final float getVolume() {
			return this.volume;
		}
		
		public final Sound setVolume(float volume) {
			this.volume = volume != volume ? this.volume : volume < 0 ? 0 : (volume > 1f ? 1f : volume);
			return this;
		}
		
		public final float getPitch() {
			return this.pitch;
		}
		
		public final Sound setPitch(float pitch) {
			this.pitch = pitch != pitch ? this.pitch : pitch < 0 ? 0 : pitch;
			return this;
		}
		
		public final Sound setLoop(boolean loop) {
			this.loop = loop;
			return this;
		}
		
		public final boolean isLooping() {
			return this.loop;
		}
		
		/** @return Whether or not this sound is playing or paused. */
		public final boolean isActive() {
			return this.state == AL_PLAYING || this.state == AL_PAUSED;
		}
		
		/** @return Whether or not this sound is at the beginning. */
		public final boolean isRewound() {
			return this.state == AL_INITIAL;
		}
		
		/** @return Whether or not this sound is playing */
		public final boolean isPlaying() {
			return this.state == AL_PLAYING;
		}
		
		/** @return Whether or not this sound is paused */
		public final boolean isPaused() {
			return this.state == AL_PAUSED;
		}
		
		/** @return Whether or not this sound is stopped */
		public final boolean isStopped() {
			return this.state == AL_STOPPED;
		}
		
		public final String getState() {
			switch(this.state) {
			case AL_INITIAL:
			default:
				return "AL_INITIAL";
			case AL_PLAYING:
				return "AL_PLAYING";
			case AL_PAUSED:
				return "AL_PAUSED";
			case AL_STOPPED:
				return "AL_STOPPED";
			}
		}
		
		/** Releases resources used by this sound */
		public final void destroy() {
			System.out.println("Unloading sound \"" + this.getPath() + "\"...");
			if(Thread.currentThread() == SoundManager.soundThread) {
				alDeleteSources(this.source);
				alDeleteBuffers(this.buffer);
				System.out.println("Unloaded sound \"" + this.getPath() + "\".");
				instances.remove(this);
			} else {
				SoundManager.soundThread.soundsToDestroy.addLast(this);
			}
		}
		
		@Override
		public String toString() {
			return this.getPath();
		}
		
		/** @return Whether or not this sound is a stereo sound(has both left
		 *         and right channels) */
		public final boolean isStereo() {
			return this.stereo;
		}
		
		/** @return The sample rate of this sound */
		public final int getSampleRate() {
			return this.sampleRate;
		}
		
		/** <b>Note:</b>&nbsp;This will be -1 if this sound was loaded from an
		 * ogg file, as {@link org.lwjgl.stb.STBVorbis the LWJGL-stb methods}
		 * for obtaining this information do not include a way to retrieve the
		 * bits per sample.
		 * 
		 * @return This sound's bits per sample */
		public final int getBitsPerSample() {
			return this.bitsPerSample;
		}
		
		/** @return the number of channels that this sound has */
		public final int getChannels() {
			return this.channels;
		}
		
		/** @return This sound's AL buffer format */
		public final int getFormat() {
			return this.format;
		}
		
	}
	
	public static final class Source {
		
		public final String path;
		public volatile ByteBuffer buffer;
		public final String formatExt;
		
		@SuppressWarnings("resource")
		public Source(URL url) throws IOException {
			this(url.openStream(), url.toString());
		}
		
		public static final boolean isURL(String resourcePath) {
			try {
				new URL(resourcePath).openStream().close();
				return true;
			} catch(Throwable ignored) {
				return false;
			}
		}
		
		@SuppressWarnings("resource")
		public Source(String resourcePath) throws IOException {
			this(isURL(resourcePath) ? new URL(resourcePath).openStream() : Sound.class.getResourceAsStream(Sound.fullPath(resourcePath)), isURL(resourcePath) ? resourcePath : Sound.fullPath(resourcePath));
		}
		
		public Source(InputStream in, String sourcePath) throws IOException {
			this(getData(in, true), sourcePath);
		}
		
		public Source(ByteBuffer buffer, String sourcePath) {
			this(sourcePath, buffer, FilenameUtils.getExtension(sourcePath));
		}
		
		public Source(String path, ByteBuffer buffer, String formatExt) {
			this.path = path;
			this.buffer = buffer.rewind();
			if(formatExt.contains("?")) {
				formatExt = formatExt.substring(0, formatExt.indexOf("?"));
			}
			this.formatExt = formatExt;
		}
		
		public final ByteArrayInputStream getInputStream() {
			return new ByteArrayInputStream(BufferUtil.getData(this.buffer));
		}
		
		@Override
		public final String toString() {
			return this.path;
		}
		
	}
	
}
