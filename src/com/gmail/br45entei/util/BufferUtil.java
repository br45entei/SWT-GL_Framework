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
package com.gmail.br45entei.util;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.misc.Unsafe;

/** Utility class which provides helper functions for creating, using, and
 * destroying {@link Buffer}s.
 *
 * @author Brian_Entei */
public class BufferUtil {
	
	private static final sun.misc.Unsafe unsafe = AccessController.doPrivileged(new PrivilegedAction<sun.misc.Unsafe>() {
		@Override
		public Unsafe run() {
			try {
				Field f = Unsafe.class.getDeclaredField("theUnsafe");
				@SuppressWarnings("deprecation")
				boolean wasAccessible = f.isAccessible();
				try {
					f.setAccessible(true);
					return (Unsafe) f.get(null);
				} catch(SecurityException | IllegalArgumentException | IllegalAccessException | InaccessibleObjectException | ExceptionInInitializerError ex) {
					throw new RuntimeException(ex);
				} finally {
					try {
						f.setAccessible(wasAccessible);
					} catch(SecurityException | IllegalArgumentException | InaccessibleObjectException ignored) {
					}
				}
			} catch(NoSuchFieldException | NullPointerException | SecurityException | IllegalArgumentException | InaccessibleObjectException ex) {
				throw new RuntimeException(ex);
			}
		}
	});
	
	/** Runs a simple test which checks that the capacity of various direct
	 * buffers is what they should be.
	 * 
	 * @param args Program command line arguments */
	public static final void main(String[] args) {
		final int size = 26;
		int count1 = 0, count2 = 0, testsPassed = 0, testsFailed = 0;
		{
			ByteBuffer test = createDirectByteBuffer(size);
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((byte) i);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			if(count1 != size || count2 != size) {
				System.err.println(String.format("ByteBuffer's capacity calculation is off! Expected %s, got %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(test.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("ByteBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(test);
		}
		count1 = count2 = 0;
		{
			ByteBuffer buf = createDirectByteBufferWithShortCapacity(size);
			ShortBuffer test = buf.asShortBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((short) i);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			if(count1 != size || count2 != size) {
				System.err.println(String.format("ShortBuffer's capacity calculation is off! Expected %s, got %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("ShortBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		count1 = count2 = 0;
		{
			ByteBuffer buf = createDirectByteBufferWithCharCapacity(size);
			CharBuffer test = buf.asCharBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((char) i);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			if(count1 != size || count2 != size) {
				System.err.println(String.format("CharBuffer's capacity calculation is off! Expected %s, got %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("CharBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		count1 = count2 = 0;
		{
			ByteBuffer buf = createDirectByteBufferWithIntCapacity(size);
			IntBuffer test = buf.asIntBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((char) i);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			if(count1 != size || count2 != size) {
				System.err.println(String.format("IntBuffer's capacity calculation is off! Expected %s, got %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("IntBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		count1 = count2 = 0;
		{
			ByteBuffer buf = createDirectByteBufferWithFloatCapacity(size);
			FloatBuffer test = buf.asFloatBuffer();
			test.rewind();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put(i + 0.0f);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			test.rewind();
			if(count1 != size || count2 != size) {
				System.err.println(String.format("FloatBuffer's capacity calculation is off! Expected %s, got %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("FloatBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		count1 = count2 = 0;
		{
			ByteBuffer buf = createDirectByteBufferWithLongCapacity(size);
			LongBuffer test = buf.asLongBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put(i);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			if(count1 != size || count2 != size) {
				System.err.println(String.format("LongBuffer's capacity calculation is off! Expected %s, got %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("LongBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		count1 = count2 = 0;
		{
			ByteBuffer buf = createDirectByteBufferWithDoubleCapacity(size);
			DoubleBuffer test = buf.asDoubleBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put(i + 0.0D);
				count1++;
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count2++;
			}
			if(count1 != size || count2 != size) {
				System.err.println(String.format("DoubleBuffer's capacity calculation is off! Expected %s, got: %s, %s (actual size: %s)", Integer.toString(size), Integer.toString(count1), Integer.toString(count2), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println(String.format("DoubleBuffer capacity test passed. (Buffer limit: %s)", Integer.toString(test.limit())));
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		
		int totalTests = testsPassed + testsFailed;
		System.out.println(String.format("Tests passed: %s/%s; Tests failed: %s/%s", Integer.toString(testsPassed), Integer.toString(totalTests), Integer.toString(testsFailed), Integer.toString(totalTests)));
	}
	
	/** Frees the specified buffer's direct memory allocation.<br>
	 * The buffer should not be used after calling this method; you should
	 * instead allow it to be garbage-collected by removing all references of it
	 * from your program.
	 * 
	 * @deprecated {@link Unsafe}.
	 * @param directBuffer The direct buffer whose memory allocation will be
	 *            freed
	 * @return Whether or not the memory allocation was freed */
	@Deprecated
	public static final boolean freeDirectBufferMemory(ByteBuffer directBuffer) {
		if(!directBuffer.isDirect()) {
			return false;
		}
		try {
			unsafe.invokeCleaner(directBuffer);
			return true;
		} catch(IllegalArgumentException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBuffer(int capacity) {
		return ByteBuffer.allocate(capacity).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link ShortBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ShortBuffer createShortBuffer(int capacity) {
		return ByteBuffer.allocate((capacity * Short.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing short values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithShortCapacity(int capacity) {
		return ByteBuffer.allocate((capacity * Short.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link ShortBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ShortBuffer createDirectShortBuffer(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Short.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing short values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithShortCapacity(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Short.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link CharBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new character buffer */
	public static final CharBuffer createCharBuffer(int capacity) {
		return ByteBuffer.allocate((capacity * Character.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asCharBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing character values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithCharCapacity(int capacity) {
		return ByteBuffer.allocate((capacity * Character.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link CharBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new character buffer */
	public static final CharBuffer createDirectCharBuffer(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Character.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asCharBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing character values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithCharCapacity(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Character.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link IntBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new integer buffer */
	public static final IntBuffer createIntBuffer(int capacity) {
		return ByteBuffer.allocate((capacity * Integer.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asIntBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing integer values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithIntCapacity(int capacity) {
		return ByteBuffer.allocate((capacity * Integer.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link IntBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new integer buffer */
	public static final IntBuffer createDirectIntBuffer(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Integer.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asIntBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing integer values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithIntCapacity(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Integer.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link FloatBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new float buffer */
	public static final FloatBuffer createFloatBuffer(int capacity) {
		return ByteBuffer.allocate((capacity * Float.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing float values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithFloatCapacity(int capacity) {
		return ByteBuffer.allocate((capacity * Float.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link FloatBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new float buffer */
	public static final FloatBuffer createDirectFloatBuffer(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Float.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing float values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithFloatCapacity(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Float.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link LongBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new long buffer */
	public static final LongBuffer createLongBuffer(int capacity) {
		return ByteBuffer.allocate((capacity * Long.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asLongBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing long values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithLongCapacity(int capacity) {
		return ByteBuffer.allocate((capacity * Long.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link LongBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new long buffer */
	public static final LongBuffer createDirectLongBuffer(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Long.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asLongBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing long values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithLongCapacity(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Long.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link DoubleBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new double buffer */
	public static final DoubleBuffer createDoubleBuffer(int capacity) {
		return ByteBuffer.allocate((capacity * Double.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asDoubleBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing double values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithDoubleCapacity(int capacity) {
		return ByteBuffer.allocate((capacity * Double.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link DoubleBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new double buffer */
	public static final DoubleBuffer createDirectDoubleBuffer(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Double.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).asDoubleBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing double values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithDoubleCapacity(int capacity) {
		return ByteBuffer.allocateDirect((capacity * Double.SIZE) / Byte.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified byte array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static byte[] getData(ByteBuffer buf, byte[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new byte array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static byte[] getData(ByteBuffer buf) {
		byte[] array = new byte[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified short array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static short[] getData(ShortBuffer buf, short[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new short array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static short[] getData(ShortBuffer buf) {
		short[] array = new short[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified character array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static char[] getData(CharBuffer buf, char[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new character array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static char[] getData(CharBuffer buf) {
		char[] array = new char[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified integer array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static int[] getData(IntBuffer buf, int[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new integer array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static int[] getData(IntBuffer buf) {
		int[] array = new int[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified float array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static float[] getData(FloatBuffer buf, float[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new float array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static float[] getData(FloatBuffer buf) {
		float[] array = new float[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified long array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static long[] getData(LongBuffer buf, long[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new long array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static long[] getData(LongBuffer buf) {
		long[] array = new long[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * the specified double array.<br>
	 * The array needs to have a size of at least {@link Buffer#capacity()
	 * buffer.capacity()}.
	 * 
	 * @param buf The buffer to get the data from
	 * @param array The array to store the data in
	 * @return The buffer's data */
	public static double[] getData(DoubleBuffer buf, double[] array) {
		if(buf.hasArray()) {
			System.arraycopy(buf.array(), 0, array, 0, array.length);
			return buf.array();
		}
		buf.rewind();
		for(int i = 0; i < array.length; i++) {
			array[i] = buf.get();
		}
		return array;
	}
	
	/** Reads the entire contents of the specified buffer and puts the data into
	 * a new double array.
	 * 
	 * @param buf The buffer to get the data from
	 * @return The buffer's data */
	public static double[] getData(DoubleBuffer buf) {
		double[] array = new double[buf.capacity()];
		return getData(buf, array);
	}
	
	/** Class used to make it easier to use and then free a direct buffer.
	 * 
	 * @param <T> The buffer's subclass
	 * @since 1.0
	 * @author Brian_Entei */
	public static final class DirectBuffer<T extends Buffer> implements AutoCloseable {
		private volatile ByteBuffer directBuffer;
		private volatile T bufferView;
		
		/** Creates a new direct, native-ordered buffer of the specified type
		 * with the specified capacity.
		 * 
		 * @param capacity The buffer's capacity, in bytes
		 * @param bufferType The subclass type of the buffer to create (e.g.
		 *            <tt>FloatBuffer.class</tt>)
		 * @throws UnsupportedOperationException Thrown if the specified
		 *             <tt>bufferType</tt> is not one of the immediate
		 *             subclasses of {@link Buffer} */
		@SuppressWarnings("unchecked")
		public DirectBuffer(int capacity, Class<T> bufferType) throws UnsupportedOperationException {
			final String type = bufferType.getSimpleName();
			switch(type) {
			case "ByteBuffer":
				this.directBuffer = BufferUtil.createDirectByteBuffer(capacity);
				this.bufferView = (T) this.directBuffer;
				return;
			case "CharBuffer":
				this.directBuffer = BufferUtil.createDirectByteBufferWithCharCapacity(capacity);
				this.bufferView = (T) this.directBuffer.asCharBuffer();
				return;
			case "DoubleBuffer":
				this.directBuffer = BufferUtil.createDirectByteBufferWithDoubleCapacity(capacity);
				this.bufferView = (T) this.directBuffer.asDoubleBuffer();
				return;
			case "FloatBuffer":
				this.directBuffer = BufferUtil.createDirectByteBufferWithFloatCapacity(capacity);
				this.bufferView = (T) this.directBuffer.asFloatBuffer();
				return;
			case "IntBuffer":
				this.directBuffer = BufferUtil.createDirectByteBufferWithIntCapacity(capacity);
				this.bufferView = (T) this.directBuffer.asIntBuffer();
				return;
			case "LongBuffer":
				this.directBuffer = BufferUtil.createDirectByteBufferWithLongCapacity(capacity);
				this.bufferView = (T) this.directBuffer.asLongBuffer();
				return;
			case "ShortBuffer":
				this.directBuffer = BufferUtil.createDirectByteBufferWithShortCapacity(capacity);
				this.bufferView = (T) this.directBuffer.asShortBuffer();
				return;
			default:
				throw new UnsupportedOperationException(String.format("Buffer Type not supported: %s"));
			}
		}
		
		/** @return The typed-view of the direct buffer for general use */
		public T getBufferView() {
			return this.bufferView;
		}
		
		/** @deprecated Use {@link #getBufferView()} instead.
		 * @return The direct byte-buffer */
		@Deprecated
		public ByteBuffer getDirectBuffer() {
			return this.directBuffer;
		}
		
		/** Rewinds this buffer. The position is set to zero and the mark is
		 * discarded.
		 *
		 * <p>
		 * Invoke this method before a sequence of channel-write or <i>get</i>
		 * operations, assuming that the limit has already been set
		 * appropriately. For example:
		 *
		 * <blockquote>
		 * 
		 * <pre>
		 * out.write(buf);    // Write remaining data
		 * buf.rewind();      // Rewind buffer
		 * buf.get(array);    // Copy data into array
		 * </pre>
		 * 
		 * </blockquote>
		 *
		 * @return This buffer */
		public DirectBuffer<T> rewind() {
			this.bufferView.rewind();
			return this;
		}
		
		/** Frees the byte-buffer's direct memory allocation.<br>
		 * The buffer should not be used after calling this method; you should
		 * instead allow it to be garbage-collected by removing all references
		 * of it from your program. */
		public void free() {
			if(this.directBuffer != null) {
				BufferUtil.freeDirectBufferMemory(this.directBuffer);
				this.directBuffer = null;
				this.bufferView = null;
			}
		}
		
		@Override
		public void close() {
			this.free();
		}
		
	}
	
}
