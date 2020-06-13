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
	
	protected static final sun.misc.Unsafe unsafe = AccessController.doPrivileged(new PrivilegedAction<sun.misc.Unsafe>() {
		@Override
		public Unsafe run() {
			try {
				Field f = Unsafe.class.getDeclaredField("theUnsafe");
				f.setAccessible(true);
				return (Unsafe) f.get(null);
			} catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
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
		int count = 0, testsPassed = 0, testsFailed = 0;
		{
			ByteBuffer test = createDirectByteBuffer(size);
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((byte) i);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("ByteBuffer's capacity calculation is off! Expected %s, got %s", Integer.toString(size), Integer.toString(count)));
				testsFailed++;
			} else {
				System.out.println("ByteBuffer capacity test passed.");
				testsPassed++;
			}
			freeDirectBufferMemory(test);
		}
		{
			ByteBuffer buf = createDirectByteBufferWithShortCapacity(size);
			ShortBuffer test = buf.asShortBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((short) i);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("ShortBuffer's capacity calculation is off! Expected %s, got %s (actual size: %s)", Integer.toString(size), Integer.toString(count), Integer.toString(buf.capacity())));
				testsFailed++;
			} else {
				System.out.println("ShortBuffer capacity test passed.");
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		{
			ByteBuffer buf = createDirectByteBufferWithCharCapacity(size);
			CharBuffer test = buf.asCharBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((char) i);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("CharBuffer's capacity calculation is off! Expected %s, got %s", Integer.toString(size), Integer.toString(count)));
				testsFailed++;
			} else {
				System.out.println("CharBuffer capacity test passed.");
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		{
			ByteBuffer buf = createDirectByteBufferWithIntCapacity(size);
			IntBuffer test = buf.asIntBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put((char) i);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("IntBuffer's capacity calculation is off! Expected %s, got %s", Integer.toString(size), Integer.toString(count)));
				testsFailed++;
			} else {
				System.out.println("IntBuffer capacity test passed.");
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		{
			ByteBuffer buf = createDirectByteBufferWithFloatCapacity(size);
			FloatBuffer test = buf.asFloatBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put(i + 0.0f);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("FloatBuffer's capacity calculation is off! Expected %s, got %s", Integer.toString(size), Integer.toString(count)));
				testsFailed++;
			} else {
				System.out.println("FloatBuffer capacity test passed.");
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		{
			ByteBuffer buf = createDirectByteBufferWithLongCapacity(size);
			LongBuffer test = buf.asLongBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put(i);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("LongBuffer's capacity calculation is off! Expected %s, got %s", Integer.toString(size), Integer.toString(count)));
				testsFailed++;
			} else {
				System.out.println("LongBuffer capacity test passed.");
				testsPassed++;
			}
			freeDirectBufferMemory(buf);
		}
		{
			ByteBuffer buf = createDirectByteBufferWithDoubleCapacity(size);
			DoubleBuffer test = buf.asDoubleBuffer();
			for(int i = 0; test.hasRemaining(); i++) {
				test.put(i + 0.0D);
			}
			test.rewind();
			while(test.hasRemaining()) {
				test.get();
				count++;
			}
			if(count != size) {
				System.err.println(String.format("DoubleBuffer's capacity calculation is off! Expected %s, got %s", Integer.toString(size), Integer.toString(count)));
				testsFailed++;
			} else {
				System.out.println("DoubleBuffer capacity test passed.");
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
	 * @param directBuffer The direct buffer whose memory allocation will be
	 *            freed
	 * @return Whether or not the memory allocation was freed */
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
		return ByteBuffer.allocate(capacity / Short.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing short values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithShortCapacity(int capacity) {
		return ByteBuffer.allocate(capacity / Short.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link ShortBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ShortBuffer createDirectShortBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Short.SIZE).order(ByteOrder.nativeOrder()).asShortBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing short values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithShortCapacity(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Short.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link CharBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new character buffer */
	public static final CharBuffer createCharBuffer(int capacity) {
		return ByteBuffer.allocate(capacity / Character.SIZE).order(ByteOrder.nativeOrder()).asCharBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing character values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithCharCapacity(int capacity) {
		return ByteBuffer.allocate(capacity / Character.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link CharBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new character buffer */
	public static final CharBuffer createDirectCharBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Character.SIZE).order(ByteOrder.nativeOrder()).asCharBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing character values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithCharCapacity(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Character.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link IntBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new integer buffer */
	public static final IntBuffer createIntBuffer(int capacity) {
		return ByteBuffer.allocate(capacity / Integer.SIZE).order(ByteOrder.nativeOrder()).asIntBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing integer values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithIntCapacity(int capacity) {
		return ByteBuffer.allocate(capacity / Integer.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link IntBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new integer buffer */
	public static final IntBuffer createDirectIntBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Integer.SIZE).order(ByteOrder.nativeOrder()).asIntBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing integer values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithIntCapacity(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Integer.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link FloatBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new float buffer */
	public static final FloatBuffer createFloatBuffer(int capacity) {
		return ByteBuffer.allocate(capacity / Float.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing float values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithFloatCapacity(int capacity) {
		return ByteBuffer.allocate(capacity / Float.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link FloatBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new float buffer */
	public static final FloatBuffer createDirectFloatBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Float.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing float values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithFloatCapacity(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Float.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link LongBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new long buffer */
	public static final LongBuffer createLongBuffer(int capacity) {
		return ByteBuffer.allocate(capacity / Long.SIZE).order(ByteOrder.nativeOrder()).asLongBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing long values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithLongCapacity(int capacity) {
		return ByteBuffer.allocate(capacity / Long.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link LongBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new long buffer */
	public static final LongBuffer createDirectLongBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Long.SIZE).order(ByteOrder.nativeOrder()).asLongBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing long values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithLongCapacity(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Long.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new {@link DoubleBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new double buffer */
	public static final DoubleBuffer createDoubleBuffer(int capacity) {
		return ByteBuffer.allocate(capacity / Double.SIZE).order(ByteOrder.nativeOrder()).asDoubleBuffer().rewind();
	}
	
	/** Creates a new {@link ByteBuffer} with the given capacity, adjusted for
	 * storing double values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createByteBufferWithDoubleCapacity(int capacity) {
		return ByteBuffer.allocate(capacity / Double.SIZE).order(ByteOrder.nativeOrder()).rewind();
	}
	
	/** Creates a new direct {@link DoubleBuffer} with the given capacity.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new double buffer */
	public static final DoubleBuffer createDirectDoubleBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Double.SIZE).order(ByteOrder.nativeOrder()).asDoubleBuffer().rewind();
	}
	
	/** Creates a new direct {@link ByteBuffer} with the given capacity,
	 * adjusted for storing double values.
	 * 
	 * @param capacity The buffer's capacity, in bytes
	 * @return The new byte buffer */
	public static final ByteBuffer createDirectByteBufferWithDoubleCapacity(int capacity) {
		return ByteBuffer.allocateDirect(capacity / Double.SIZE).order(ByteOrder.nativeOrder()).rewind();
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
	
}
