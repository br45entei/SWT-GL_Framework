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
package com.gmail.br45entei.util;

import com.gmail.br45entei.game.graphics.GLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** @author Brian_Entei */
public class BitUtil {
	
	private BitUtil() {
	}
	
	public static final boolean getBitInByte(byte b, int position) {
		return ((b >> position) & 1) !=  0;
	}
	
	public static final byte setBitInByte(byte b, int position, boolean bit) {
		if(position < 0 || position >= 8) {
			throw new IndexOutOfBoundsException(position);
		}
		if(bit) {
			b |= (1 << position);// sets the bit at the position to 1
		} else {
			b &= ~(1 << position);// sets the bit at the position to 0
		}
		return b;
	}
	
	public static final void byteToBits(byte b, boolean[] bits, int offset) {
		for(int i = 0; i < Math.min(8, bits.length - offset); i++) {
			bits[i + offset] = ((b >> i) & 1) != 0;
		}
	}
	
	public static final boolean[] byteToBits(byte b) {
		boolean[] bits = new boolean[8];
		for(int i = 0; i < bits.length; i++) {
			bits[i] = ((b >> i) & 1) != 0;
		}
		return bits;
	}
	
	public static final byte bitsToByte(boolean[] bits, int offset) throws ArrayIndexOutOfBoundsException {
		byte b = 0;
		if(bits == null) {
			return b;
		}
		final int length = bits.length - offset;
		for(int position = 0; position + offset < Math.min(8, length); position++) {
			if(bits[position + offset]) {
				b |= (1 << position);// sets the bit at the position specified by bitCounter to 1
			} else {
				b &= ~(1 << position);// sets the bit at the position specified by bitCounter to 0
			}
		}
		return b;
	}
	
	public static final byte bitsToByte(boolean b0, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7) {
		return bitsToByte(new boolean[] {b0, b1, b2, b3, b4, b5, b6, b7}, 0);
	}
	
	public static final byte[] shortToBytes(short s) {
		return new byte[] {//@formatter:off
				(byte) (s >> 8),//(byte) ((s >> 8) & 0xff),
				(byte) s//(byte)(s & 0xff)
		};//@formatter:on
	}
	
	public static final short bytesToShort(byte b0, byte b1) {
		return (short) (((b0 & 0xff) << 8) + (b1 & 0xff));
	}
	
	public static final short bytesToShort(byte[] b0b1, int offset) throws ArrayIndexOutOfBoundsException {
		return bytesToShort(b0b1[0 + offset], b0b1[1 + offset]);
	}
	
	public static final short bytesToShort(byte[] b0b1) throws ArrayIndexOutOfBoundsException {
		return bytesToShort(b0b1, 0);
	}
	
	public static final byte[] intToBytes(int i) {
		return new byte[] {//@formatter:off
				(byte) ((i & 0xFF000000) >> 24),
				(byte) ((i & 0x00FF0000) >> 16),
				(byte) ((i & 0x0000FF00) >> 8),
				(byte) (i & 0x000000FF)
		};//@formatter:on
	}
	
	public static final byte[] intToBytes(int... array) {
		if(array.length >= 536870911) {
			throw new UnsupportedOperationException("(array.length * 4) is larger than or equal to (Integer.MAX_VALUE - 8)!");
		}
		byte[] result = new byte[Integer.BYTES * array.length];
		int index;
		for(int j = 0; j < array.length; j++) {
			int i = array[j];
			index = j * Integer.BYTES;
			
			System.arraycopy(intToBytes(i), 0, result, index, 4);
			
		}
		return result;
	}
	
	public static final int bytesToInt(byte b0, byte b1, byte b2, byte b3) {
		return(((b0 & 0xFF) << 24) + ((b1 & 0xFF) << 16) + ((b2 & 0xFF) << 8) + (b3 & 0xFF));
	}
	
	public static final int bytesToInt(byte[] b0b1b2b3, int offset) throws ArrayIndexOutOfBoundsException {
		return bytesToInt(b0b1b2b3[0 + offset], b0b1b2b3[1 + offset], b0b1b2b3[2 + offset], b0b1b2b3[3 + offset]);
	}
	
	public static final int bytesToInt(byte[] b0b1b2b3) throws ArrayIndexOutOfBoundsException {
		return bytesToInt(b0b1b2b3, 0);
	}
	
	public static final byte[] longToBytesOld(long data) {
		return new byte[] {//@formatter:off
				(byte) ((data & 0xFF00000000000000L) >> 56),
				(byte) ((data & 0x00FF000000000000L) >> 48),
				(byte) ((data & 0x0000FF0000000000L) >> 40),
				(byte) ((data & 0x000000FF00000000L) >> 32),
				(byte) ((data & 0x00000000FF000000L) >> 24),
				(byte) ((data & 0x0000000000FF0000L) >> 16),
				(byte) ((data & 0x000000000000FF00L) >> 8),
				(byte) (data &  0x00000000000000FFL)
		};//@formatter:on
	}
	
	public static final long bytesToLongOld(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
		return(((b0 & 0xFF) << 56) + ((b1 & 0xFF) << 48) + ((b2 & 0xFF) << 40) + ((b3 & 0xFF) << 32) + ((b4 & 0xFF) << 24) + ((b5 & 0xFF) << 16) + ((b6 & 0xFF) << 8) + (b7 & 0xFF));
	}
	
	/** Converts the given long value into a byte array.
	 *
	 * @param l The long to convert
	 * @return An array containing the eight bytes of data from the long */
	public static byte[] longToBytes(long l) {
		byte[] result = new byte[] {//@formatter:off
				(byte) (l >>> 56),
				(byte) (l >>> 48),
				(byte) (l >>> 40),
				(byte) (l >>> 32),
				(byte) (l >>> 24),
				(byte) (l >>> 16),
				(byte) (l >>>  8),
				(byte) (l >>>  0)
		};//@formatter:on
		return result;
	}
	
	public static byte[] longToBytes(long... array) {
		if(array.length >= 268435455) {
			throw new UnsupportedOperationException("(array.length * 8) is larger than or equal to (Integer.MAX_VALUE - 8)!");
		}
		byte[] result = new byte[Long.BYTES * array.length];
		int index;
		for(int j = 0; j < array.length; j++) {
			long l = array[j];
			index = j * Long.BYTES;
			
			//@formatter:off
			result[index + 0] = (byte) (l >>> 56);
			result[index + 1] = (byte) (l >>> 48);
			result[index + 2] = (byte) (l >>> 40);
			result[index + 3] = (byte) (l >>> 32);
			result[index + 4] = (byte) (l >>> 24);
			result[index + 5] = (byte) (l >>> 16);
			result[index + 6] = (byte) (l >>>  8);
			result[index + 7] = (byte) (l >>>  0);
			//@formatter:on
			
		}
		return result;
	}
	
	public static long[] bytesToLong(final byte[] bytes, final int offset, final int count) {
		int length = Long.BYTES * count;
		if(length > bytes.length - offset) {
			throw new ArrayIndexOutOfBoundsException(String.format("'bytes.length' (%s) is too small for the specified offset (%s) and count (%s)! Byte length required: %s; Byte length available(with offset): %s", Integer.toString(bytes.length), Integer.toString(offset), Integer.toString(count), Integer.toString(length), Integer.toString(bytes.length - offset)));
		}
		long[] results = new long[count];
		for(int i = 0; i < results.length; i++) {
			long result = results[i];
			
			int off = offset + (i * Long.BYTES);
			
			for(int j = 0; j < Long.BYTES; j++) {
				result <<= Long.BYTES;
				//result |= /*j == 0 ? */bytes[off + j]/* : (bytes[off + j] & 0xFF)*/;
				result |= (bytes[off + j] & 0xFF);
			}
			
			results[i] = result;
		}
		return results;
	}
	
	/** Converts the given byte array into a long value.
	 *
	 * @param bytes The bytes to convert
	 * @param offset The offset in the byte array where the data starts
	 * @return The resulting long value, constructed from the read bytes
	 * @author <a href="https://stackoverflow.com/a/29132118">Wytze</a> */
	public static long bytesToLong(final byte[] bytes, final int offset) {
		long result = 0;
		for(int i = offset; i < Math.min(offset + Long.BYTES, bytes.length); i++) {
			result <<= Long.BYTES;
			//result |= /*i == offset ? */bytes[i]/* : (bytes[i] & 0xFF)*/;
			result |= (bytes[i] & 0xFF);
		}
		return result;
	}
	
	public static final long bytesToLong(byte[] bytes) {
		return bytesToLong(bytes, 0);
	}
	
	//====================================================================================================================================================================
	
	public static final void main(String[] args) {
		/*byte[] test = new byte[6];
		test[0] = 1;
		test[1] = 2;
		test[2] = 3;
		test[3] = 4;
		test[4] = 5;
		test[5] = 6;
		long c = BitUtil.bytesToLong(test, 0);
		byte[] compare = BitUtil.longToBytes(c);
		System.out.println(GLUtil.vectorToString(test, true));
		System.out.println(GLUtil.vectorToString(compare, true));*/
		
		/*long failCount = 0, passCount = 0;
		//short s = 31765;
		for(int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
			short s = (short) i;
			
			byte[] buf = BitUtil.shortToBytes(s);
			short check = BitUtil.bytesToShort(buf);
			
			if(check != s) {
				failCount++;
				System.out.println("[Short] value: " + s + "; converted: " + check + ";");
			} else {
				passCount++;
			}
		}
		System.out.println("[Short] Fail count: " + failCount + "; Pass Count: " + passCount + ";\r\n\r\n");
		failCount = passCount = 0;*/
		
		/*for(long l = Integer.MIN_VALUE; l <= Integer.MAX_VALUE; l++) {
			int i = (int) l;
			
			byte[] buf = BitUtil.intToBytes(i);
			int check = BitUtil.bytesToInt(buf);
			
			if(check != i) {
				failCount++;
				System.out.println("[Integer] value: " + i + "; converted: " + check + ";");
			} else {
				passCount++;
			}
		}
		System.out.println("[Integer] Fail count: " + failCount + "; Pass Count: " + passCount + ";\r\n\r\n");
		failCount = passCount = 0;*/
		
		/*for(long l = Long.MIN_VALUE;; l++) {
			byte[] buf = BitUtil.longToBytes(l);
			long check = BitUtil.bytesToLong(buf);
			
			if(check != l) {
				failCount++;
				System.out.println("[Long] value: " + l + "; converted: " + check + ";");
			} else {
				passCount++;
			}
			
			if(l == Long.MAX_VALUE) {
				break;
			}
			if(l % Integer.MAX_VALUE == 0) {
				System.out.println("[" + l + "]");
			}
		}
		System.out.println("[Long] Fail count: " + failCount + "; Pass Count: " + passCount + ";\r\n\r\n");*/
		
		long[] pos = new long[] {772, -1, 256};//{772, 257, 256};
		byte[] data = BitUtil.longToBytes(pos);//Works great
		final int ogDataLength = data.length;
		
		/*long x = BitUtil.bytesToLong(data, 0);//Works great
		long y = BitUtil.bytesToLong(data, 8);//Works great
		long z = BitUtil.bytesToLong(data, 16);//Works great
		long[] testPos = new long[] {x, y, z};//( 772, -1,  256)*/
		long[] testPos = BitUtil.bytesToLong(data, 0, 3);
		
		if(!GLUtil.equals(pos, testPos)) {
			System.out.println(String.format("%s does not equal %s!\t\t%s", GLUtil.vectorToString(pos), GLUtil.vectorToString(testPos), GLUtil.vectorToString(data, true)));
		} else {
			System.out.println(String.format("%s matches %s!\t%s", GLUtil.vectorToString(pos), GLUtil.vectorToString(testPos), GLUtil.vectorToString(data, true)));
			System.out.println("Writing to disk & reading back...");
			
			File test = new File(CodeUtil.getProperty("user.dir"));
			test.mkdirs();
			test = new File(test, "BitUtil.test");
			//test.deleteOnExit();
			
			try(FileOutputStream out = new FileOutputStream(test)) {
				out.write(data, 0, data.length);
				out.flush();
			} catch(IOException ex) {
				ex.printStackTrace();
				return;
			}
			try {
				data = FileUtil.readFile(test);
			} catch(IOException ex) {
				ex.printStackTrace();
				return;
			}
			
			if(data.length != ogDataLength) {
				System.out.println(String.format("Original data.length (%s) does not equal data.length read from disk (%s)!", Integer.toString(ogDataLength), Integer.toString(data.length)));
			} else {
				/*x = BitUtil.bytesToLong(data, 0);
				y = BitUtil.bytesToLong(data, 8);
				z = BitUtil.bytesToLong(data, 16);
				testPos = new long[] {x, y, z};*/
				testPos = BitUtil.bytesToLong(data, 0, 3);
				
				if(!GLUtil.equals(pos, testPos)) {
					System.out.println(String.format("[Disk] %s does not equal %s!\t\t%s", GLUtil.vectorToString(pos), GLUtil.vectorToString(testPos), GLUtil.vectorToString(data, true)));
				} else {
					System.out.println(String.format("[Disk] %s matches %s!", GLUtil.vectorToString(pos), GLUtil.vectorToString(testPos)));
				}
			}
		}
		
	}
	
	//====================================================================================================================================================================
	
}
