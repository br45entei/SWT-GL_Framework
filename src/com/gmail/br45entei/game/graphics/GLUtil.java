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
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.game.input.Mouse;
import com.gmail.br45entei.game.math.MathUtil;
import com.gmail.br45entei.game.math.Vector2d;
import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.util.Platform;
import com.gmail.br45entei.util.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Color;
import org.lwjgl.opengl.ARBInternalformatQuery;
import org.lwjgl.opengl.ARBInternalformatQuery2;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.system.Checks.checkFunctions;

/** Utility class containing common graphics library functions.
 *
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
@SuppressWarnings("javadoc")
public class GLUtil {
	
	public static final void main(String[] args) {
		{
			String[] test = new String[] {"", null, "", null, "", null, "", "", "", "", null, "", GLUtil.class.getName(), "", null};
			System.out.println("test.length: " + test.length + "; clean.length: " + clean(test).length + ";");
			System.exit(0);
		}
		double[] matrix = GLUtil.getIdentityd();
		System.out.println(matrix4x4ToStringd(matrix));
		/*matrix = GLUtil.translateMatrix4x4d(matrix, 0, 0, 0);//27.913, 43.372, 92.046);
		System.out.println(matrix4x4ToStringd(matrix));
		System.out.println("===================================================");
		matrix = GLUtil.rotateMatrix4x4d(matrix, 90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		matrix = GLUtil.rotateMatrix4x4d(matrix, 90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		matrix = GLUtil.rotateMatrix4x4d(matrix, 90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		matrix = GLUtil.rotateMatrix4x4d(matrix, 90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		System.out.println("===================================================");
		matrix = GLUtil.rotateMatrix4x4d(matrix, -90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		matrix = GLUtil.rotateMatrix4x4d(matrix, -90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		matrix = GLUtil.rotateMatrix4x4d(matrix, -90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		matrix = GLUtil.rotateMatrix4x4d(matrix, -90, 0, 1, 0);
		System.out.println(matrix4x4ToStringd(matrix));
		System.out.println("===================================================");*/
		
		/*System.out.println(matrix4x4ToStringd(GLUtil.rotateMatrix4x4d(matrix, 90, 0, 1, 0)));
		System.out.println(matrix4x4ToStringd(matrix = GLUtil.rotateMatrix4x4d(matrix, 270, 1, 1, 1)));
		matrix = GLUtil.rotateMatrix4x4d(matrix, 270, 0, -1, 0);
		matrix = GLUtil.rotateMatrix4x4d(matrix, 270, -1, 0, 0);
		matrix = GLUtil.rotateMatrix4x4d(matrix, 270, 0, 0, -1);
		System.out.println(matrix4x4ToStringd(matrix));
		System.out.println(matrix4x4ToStringd(matrix = GLUtil.rotateMatrix4x4d(matrix, 270, 1, 1, 1)));
		System.out.println(matrix4x4ToStringd(matrix = GLUtil.rotateYXZMatrix4x4d(matrix, 270, -1, -1, -1)));*/
		
		matrix = GLUtil.rotateMatrix4x4d(matrix, 283, 0, 1, 0);//yaw
		matrix = GLUtil.rotateMatrix4x4d(matrix, 0, 1, 0, 0);//pitch
		matrix = GLUtil.rotateMatrix4x4d(matrix, 0, 0, 0, 1);//roll
		
		System.out.println(matrix4x4ToStringd(matrix, 16, true));
		
		for(double y = 0.0; y < 360.0; y += 1.0) {
			double[] viewMatrix = GLUtil.rotateMatrix4x4d(GLUtil.getIdentityd(), y, 0, 1);
			
			//double[] ypr = getYawPitchRollFrom4x4Matrixd(viewMatrix);
			//double yaw = ypr[0];
			//double pitch = ypr[1];
			//double roll = ypr[2];
			
			//System.out.println("[".concat(Double.toString(y)).concat("] ").concat("Yaw: ").concat(Long.toString(Math.round(yaw))).concat("; Pitch: ").concat(Double.toString(pitch)).concat(";"));
			
			double yawArcSinRad = Math.asin(viewMatrix[2]);
			double pitchArcSinRad = Math.asin(viewMatrix[9]);
			double rollArcSinRad = Math.asin(viewMatrix[4]);
			double yawArcSinDeg = (360 + (yawArcSinRad * toDegreesMultiplier)) % 360.0;
			double pitchArcSinDeg = (360 + (pitchArcSinRad * toDegreesMultiplier)) % 360.0;
			double rollArcSinDeg = (360 + (rollArcSinRad * toDegreesMultiplier)) % 360.0;
			
			double yawArcCosRollRad = Math.acos(viewMatrix[0]);
			double pitchArcCosYawRad = Math.acos(viewMatrix[10]);
			double rollArcCosPitchRad = Math.acos(viewMatrix[5]);
			double yawArcCosRollDeg = (360 + (yawArcCosRollRad * toDegreesMultiplier)) % 360.0;
			double pitchArcCosYawDeg = (360 + (pitchArcCosYawRad * toDegreesMultiplier)) % 360.0;
			double rollArcCosPitchDeg = (360 + (rollArcCosPitchRad * toDegreesMultiplier)) % 360.0;
			
			double yawArcCosPitchRad = pitchArcCosYawRad;//Math.acos(viewMatrix[10]);
			double pitchArcCosRollRad = rollArcCosPitchRad;//Math.acos(viewMatrix[5]);
			double rollArcCosYawRad = yawArcCosRollRad;//Math.acos(viewMatrix[0]);
			double yawArcCosPitchDeg = pitchArcCosYawDeg;//(360 + (yawArcCosPitchRad * toDegreesMultiplier)) % 360.0;
			double pitchArcCosRollDeg = rollArcCosPitchDeg;//(360 + (pitchArcCosRollRad * toDegreesMultiplier)) % 360.0;
			double rollArcCosYawDeg = yawArcCosRollDeg;//(360 + (rollArcCosYawRad * toDegreesMultiplier)) % 360.0;
			
			double yaw = yawArcSinDeg;
			double pitch = pitchArcSinDeg;
			double roll = rollArcSinDeg;
			
			if(yawArcCosPitchDeg == 90.0 ? (yawArcCosRollDeg > 90.0) : (yawArcCosPitchDeg > 90.0)) {
				yaw = yaw + ((270 - yaw) * 2.0);
				if(yaw > 360) {
					yaw = Math.abs(yaw + ((180 - yaw) * 2.0));
				}
			}
			
			System.out.println("[".concat(Double.toString(y)).concat("] Yaw: ").concat(Double.toString(yaw)).concat("; Pitch: ").concat(Double.toString(pitch)).concat("; Roll: ").concat(Double.toString(roll)).concat("; Yaw aSin aCos: (").concat(Double.toString(yawArcSinDeg)).concat(", ").concat(Double.toString(yawArcCosPitchDeg)).concat("); Pitch aSin aCos: (").concat(Double.toString(pitchArcSinDeg)).concat(", ").concat(Double.toString(pitchArcCosRollDeg)).concat("); Roll aSin aCos: (").concat(Double.toString(rollArcSinDeg)).concat(", ").concat(Double.toString(rollArcCosYawDeg)).concat(");"));
		}
		/*
		double[] viewMatrix = matrix;//GLUtil.scaleMatrix4x4d(matrix, -1, -1, -1);
		//Column-major order ( |/|/| )
		
		double[] right = new double[3];
		right[0] = viewMatrix[0];
		right[1] = viewMatrix[4];
		right[2] = viewMatrix[8];
		
		double[] up = new double[3];
		up[0] = viewMatrix[1];
		up[1] = viewMatrix[5];
		up[2] = viewMatrix[9];
		
		double[] forward = new double[3];
		forward[0] = viewMatrix[2];
		forward[1] = viewMatrix[6];
		forward[2] = viewMatrix[10];
		
		System.out.println();
		System.out.println("Forward: ".concat(vectorToString(forward, 8, true)));
		System.out.println("Up: ".concat(vectorToString(up, 8, true)));
		System.out.println("Right: ".concat(vectorToString(right, 8, true)));
		
		double[] ypr = getYawPitchRollFrom4x4Matrixd(viewMatrix);
		double yaw = ypr[0];
		double pitch = ypr[1];
		double roll = ypr[2];
		
		System.out.println("Yaw: ".concat(Double.toString(yaw)).concat("; Pitch: ").concat(Double.toString(pitch)).concat(";"));
		*/
		
		System.out.println("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n");
		
		float[] vertices = new float[] {//@formatter:off
				0.5f, -0.5f, 0.5f,    -0.5f, -0.5f, 0.5f,    0.5f, 0.5f, 0.5f,    -0.5f, -0.5f, 0.5f,   -0.5f, 0.5f, 0.5f,     0.5f, 0.5f, 0.5f
		};//@formatter:on
		float[] positionf = new float[] {1, 5, 7};
		
		float[] sum = GLUtil.add(vertices, positionf);
		float[] test = GLUtil.addDivisibly(vertices, positionf, false);
		
		System.out.println(String.format("sum.length: %s; test.length: %s;", Integer.toString(sum.length), Integer.toString(test.length)));
		
		System.out.println("sum:");
		System.out.println(GLUtil.vectorToString(sum, 2, true));
		System.out.println();
		System.out.println("test:");
		System.out.println(GLUtil.vectorToString(test, 2, true));
		System.out.println("original vertices:");
		System.out.println(GLUtil.vectorToString(vertices, 2, true));
		System.out.println();
		System.out.println("positionf:");
		System.out.println(GLUtil.vectorToString(positionf, 2, true));
	}
	
	public static final double[] getYawPitchRollFrom4x4Matrixd(double[] viewMatrix) {
		//Column-major order ( |/|/|/| )
		
		double[] right = new double[3];
		right[0] = viewMatrix[0];
		right[1] = viewMatrix[4];
		right[2] = viewMatrix[8];
		
		double[] up = new double[3];
		up[0] = viewMatrix[1];
		up[1] = viewMatrix[5];
		up[2] = viewMatrix[9];
		
		double[] forward = new double[3];
		forward[0] = viewMatrix[2];
		forward[1] = viewMatrix[6];
		forward[2] = viewMatrix[10];
		
		double yaw = (450.0 + (Math.atan2(-forward[2], forward[0]) * toDegreesMultiplier)) % 360.0;//450.0 = (360 + (90 + yaw)) % 360.0
		double pitch = (360.0 + (Math.asin(-forward[1]) * toDegreesMultiplier)) % 360.0;
		double roll = 0.0;
		return new double[] {yaw, pitch, roll};
	}
	
	public static final String vectorToString(boolean[] vector) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			boolean x = vector[i];
			sb.append(x).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToString(byte[] vector, boolean unsigned) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			byte x = vector[i];
			sb.append(unsigned || x < 0 ? "" : " ").append(unsigned ? Integer.toString(x & 0xFF) : Byte.toString(x)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToString(short[] vector) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			short x = vector[i];
			sb.append(x < 0 ? "" : " ").append(Short.toString(x)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToString(int[] vector) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			int x = vector[i];
			sb.append(x < 0 ? "" : " ").append(Integer.toString(x)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToStringOutOf(int[] vector, int dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			int x = vector[i];
			sb.append(x < 0 ? "" : " ").append(Integer.toString(x)).append("/").append(Integer.toString(dividend)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToStringOutOf(int[] vector, int[] dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			int x = vector[i], d = dividend[i];
			sb.append(x < 0 ? "" : " ").append(Integer.toString(x)).append("/").append(Integer.toString(d)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToString(float[] vector, int numOfPlaces, boolean pad) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			float x = vector[i];
			sb.append(x < 0.0 || Float.floatToIntBits(x) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(x, numOfPlaces, pad)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(" )").toString();
	}
	
	public static final String vectorToStringOutOf(float[] vector, int numOfPlaces, boolean pad, float dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			float x = vector[i];
			sb.append(x < 0.0 || Float.floatToIntBits(x) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(x, numOfPlaces, pad)).append("/").append(MathUtil.limitDecimalNoRounding(dividend, numOfPlaces, pad)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(" )").toString();
	}
	
	public static final String vectorToStringOutOf(float[] vector, int numOfPlaces, boolean pad, float[] dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			float x = vector[i], d = dividend[i];
			sb.append(x < 0.0 || Float.floatToIntBits(x) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(x, numOfPlaces, pad)).append("/").append(MathUtil.limitDecimalNoRounding(d, numOfPlaces, pad)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(" )").toString();
	}
	
	public static final String vectorToString(long[] vector) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			long x = vector[i];
			sb.append(x < 0 ? "" : " ").append(Long.toString(x)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToStringOutOf(long[] vector, long dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			long x = vector[i];
			sb.append(x < 0 ? "" : " ").append(Long.toString(x)).append("/").append(Long.toString(dividend)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToStringOutOf(long[] vector, long[] dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			long x = vector[i], d = dividend[i];
			sb.append(x < 0 ? "" : " ").append(Long.toString(x)).append("/").append(Long.toString(d)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToString(double[] vector, int numOfPlaces, boolean pad) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			double x = vector[i];
			sb.append(x < 0.0 || Double.doubleToLongBits(x) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(x, numOfPlaces, pad)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToStringOutOf(double[] vector, int numOfPlaces, boolean pad, double dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			double x = vector[i];
			sb.append(x < 0.0 || Double.doubleToLongBits(x) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(x, numOfPlaces, pad)).append("/").append(MathUtil.limitDecimalNoRounding(dividend, numOfPlaces, pad)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	public static final String vectorToStringOutOf(double[] vector, int numOfPlaces, boolean pad, double[] dividend) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			double x = vector[i], d = dividend[i];
			sb.append(x < 0.0 || Double.doubleToLongBits(x) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(x, numOfPlaces, pad)).append("/").append(MathUtil.limitDecimalNoRounding(d, numOfPlaces, pad)).append(i + 1 == vector.length ? "" : ", ");
		}
		return sb.append(")").toString();
	}
	
	//=====================================================================================================================
	
	public static final float[] concat(float[] array1, float... array2) {
		float[] array3 = new float[array1.length + array2.length];
		System.arraycopy(array1, 0, array3, 0, array1.length);
		System.arraycopy(array2, 0, array3, array1.length, array2.length);
		return array3;
	}
	
	public static final double[] concat(double[] array1, double... array2) {
		double[] array3 = new double[array1.length + array2.length];
		System.arraycopy(array1, 0, array3, 0, array1.length);
		System.arraycopy(array2, 0, array3, array1.length, array2.length);
		return array3;
	}
	
	public static final strictfp double[] asDoubleArray(float[] array) {
		double[] rtrn = new double[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = array[i];
		}
		return rtrn;
	}
	
	public static final strictfp float[] asFloatArray(double[] array) {
		float[] rtrn = new float[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = (float) array[i];
		}
		return rtrn;
	}
	
	public static final strictfp float[] floor(float[] array) {
		float[] rtrn = new float[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = (float) Math.floor(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp float[] ceil(float[] array) {
		float[] rtrn = new float[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = (float) Math.ceil(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp double[] floor(double[] array) {
		double[] rtrn = new double[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = Math.floor(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp double[] ceil(double[] array) {
		double[] rtrn = new double[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = Math.ceil(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp int[] roundToInt(float[] array) {
		int[] rtrn = new int[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = Math.round(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp int[] roundToInt(double[] array) {
		int[] rtrn = new int[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = (int) Math.round(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp long[] roundToLong(float[] array) {
		long[] rtrn = new long[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = Math.round(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp long[] roundToLong(double[] array) {
		long[] rtrn = new long[array.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = Math.round(array[i]);
		}
		return rtrn;
	}
	
	public static final strictfp double[] normalize(double x, double y) {
		double magnitude = Math.sqrt((x * x) + (y * y));
		return new double[] {x / magnitude, y / magnitude};
	}
	
	public static final strictfp double[] normalize(double x, double y, double z) {
		double magnitude = Math.sqrt((x * x) + (y * y) + (z * z));
		return new double[] {x / magnitude, y / magnitude, z / magnitude};
	}
	
	public static final strictfp double[] normalize(double x, double y, double z, double w) {
		double magnitude = Math.sqrt((x * x) + (y * y) + (z * z) + (w * w));
		return new double[] {x / magnitude, y / magnitude, z / magnitude, w / magnitude};
	}
	
	public static final strictfp float magnitude(float[] v) {
		float magnitude = 0x0.0p0f;
		for(int i = 0; i < v.length; i++) {
			magnitude += v[i] * v[i];
		}
		return (float) Math.sqrt(magnitude);
	}
	
	public static final strictfp double magnitude(double[] v) {
		double magnitude = 0x0.0p0;
		for(int i = 0; i < v.length; i++) {
			magnitude += v[i] * v[i];
		}
		return Math.sqrt(magnitude);
	}
	
	/** @param v The vector to normalize
	 * @return A new array containing the normalized coordinates */
	public static final strictfp float[] normalize(float[] v) {
		float[] result = new float[v.length];
		float magnitude = magnitude(v);
		for(int i = 0; i < v.length; i++) {
			result[i] = v[i] / magnitude;
		}
		return result;
	}
	
	/** @param v The vector to normalize
	 * @return A new array containing the normalized coordinates */
	public static final strictfp double[] normalize(double[] v) {
		double[] result = new double[v.length];
		double magnitude = magnitude(v);
		for(int i = 0; i < v.length; i++) {
			result[i] = v[i] / magnitude;
		}
		return result;
	}
	
	/** Adds every three elements in the given array with the given
	 * three-dimensional vector.<br>
	 * If the given array's length is not a multiple of three, the remaining
	 * elements are simply copied into the returned array (they are not added
	 * together with the vector).
	 *
	 * @param array The array in which every three elements will be added
	 *            together with the given vector
	 * @param x The x component of the vector
	 * @param y The y component of the vector
	 * @return The resulting array */
	public static final strictfp float[] addVector2(float[] array, float x, float y) {
		float[] sum = new float[array.length];
		int i = 0;
		for(; i < sum.length; i += 2) {
			sum[i + 0] = array[i + 0] + x;
			sum[i + 1] = array[i + 1] + y;
		}
		for(; i < sum.length; i++) {
			sum[i] = array[i];
		}
		return sum;
	}
	
	/** Adds every three elements in the given array with the given
	 * three-dimensional vector.<br>
	 * If the given array's length is not a multiple of three, the remaining
	 * elements are simply copied into the returned array (they are not added
	 * together with the vector).
	 *
	 * @param array The array in which every three elements will be added
	 *            together with the given vector
	 * @param x The x component of the vector
	 * @param y The y component of the vector
	 * @return The resulting array */
	public static final strictfp double[] addVector2(double[] array, double x, double y) {
		double[] sum = new double[array.length];
		int i = 0;
		for(; i < sum.length; i += 2) {
			sum[i + 0] = array[i + 0] + x;
			sum[i + 1] = array[i + 1] + y;
		}
		for(; i < sum.length; i++) {
			sum[i] = array[i];
		}
		return sum;
	}
	
	/** Adds every three elements in the given array with the given
	 * three-dimensional vector.<br>
	 * If the given array's length is not a multiple of three, the remaining
	 * elements are simply copied into the returned array (they are not added
	 * together with the vector).
	 *
	 * @param array The array in which every three elements will be added
	 *            together with the given vector
	 * @param x The x component of the vector
	 * @param y The y component of the vector
	 * @param z The z component of the vector
	 * @return The resulting array */
	public static final strictfp float[] addVector3(float[] array, float x, float y, float z) {
		float[] sum = new float[array.length];
		int i = 0;
		for(; i < sum.length; i += 3) {
			sum[i + 0] = array[i + 0] + x;
			sum[i + 1] = array[i + 1] + y;
			sum[i + 2] = array[i + 2] + z;
		}
		for(; i < sum.length; i++) {
			sum[i] = array[i];
		}
		return sum;
	}
	
	/** Adds every three elements in the given array with the given
	 * three-dimensional vector.<br>
	 * If the given array's length is not a multiple of three, the remaining
	 * elements are simply copied into the returned array (they are not added
	 * together with the vector).
	 *
	 * @param array The array in which every three elements will be added
	 *            together with the given vector
	 * @param x The x component of the vector
	 * @param y The y component of the vector
	 * @param z The z component of the vector
	 * @return The resulting array */
	public static final strictfp double[] addVector3(double[] array, double x, double y, double z) {
		double[] sum = new double[array.length];
		int i = 0;
		for(; i < sum.length; i += 3) {
			sum[i + 0] = array[i + 0] + x;
			sum[i + 1] = array[i + 1] + y;
			sum[i + 2] = array[i + 2] + z;
		}
		for(; i < sum.length; i++) {
			sum[i] = array[i];
		}
		return sum;
	}
	
	/** Adds every three elements in the given array with the given
	 * three-dimensional vector.<br>
	 * If the given array's length is not a multiple of three, the remaining
	 * elements are simply copied into the returned array (they are not added
	 * together with the vector).
	 *
	 * @param array The array in which every three elements will be added
	 *            together with the given vector
	 * @param x The x component of the vector
	 * @param y The y component of the vector
	 * @param z The z component of the vector
	 * @return The resulting array */
	public static final strictfp float[] addVector4(float[] array, float x, float y, float z, float w) {
		float[] sum = new float[array.length];
		int i = 0;
		for(; i < sum.length; i += 3) {
			sum[i + 0] = array[i + 0] + x;
			sum[i + 1] = array[i + 1] + y;
			sum[i + 2] = array[i + 2] + z;
			sum[i + 3] = array[i + 3] + w;
		}
		for(; i < sum.length; i++) {
			sum[i] = array[i];
		}
		return sum;
	}
	
	/** Adds every three elements in the given array with the given
	 * three-dimensional vector.<br>
	 * If the given array's length is not a multiple of three, the remaining
	 * elements are simply copied into the returned array (they are not added
	 * together with the vector).
	 *
	 * @param array The array in which every three elements will be added
	 *            together with the given vector
	 * @param x The x component of the vector
	 * @param y The y component of the vector
	 * @param z The z component of the vector
	 * @return The resulting array */
	public static final strictfp double[] addVector4(double[] array, double x, double y, double z, double w) {
		double[] sum = new double[array.length];
		int i = 0;
		for(; i < sum.length; i += 4) {
			sum[i + 0] = array[i + 0] + x;
			sum[i + 1] = array[i + 1] + y;
			sum[i + 2] = array[i + 2] + z;
			sum[i + 3] = array[i + 3] + w;
		}
		for(; i < sum.length; i++) {
			sum[i] = array[i];
		}
		return sum;
	}
	
	public static final <T> T[] clean(T[] array) {
		int newLength = 0;
		for(int i = 0; i < array.length; i++) {
			T elm = array[i];
			if(elm != null) {
				newLength++;
			}
		}
		T[] clean = Arrays.copyOf(array, newLength);
		for(int i = 0, j = 0; i < array.length; i++) {
			T elm = array[i];
			if(elm != null) {
				clean[j++] = elm;
			}
		}
		return clean;
	}
	
	public static final void boxAdd(Collection<Boolean> collection, boolean... booleans) {
		for(boolean b : booleans) {
			collection.add(Boolean.valueOf(b));
		}
	}
	
	public static final boolean[] unbox(Boolean... booleans) {
		boolean[] array = new boolean[booleans.length];
		int i = 0;
		for(Boolean b : booleans) {
			array[i++] = b.booleanValue();
		}
		return array;
	}
	
	public static final boolean[] unboxBooleans(Collection<Boolean> booleans) {
		boolean[] array = new boolean[booleans.size()];
		int i = 0;
		for(Boolean b : booleans) {
			array[i++] = b.booleanValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Byte> collection, byte... bytes) {
		for(byte b : bytes) {
			collection.add(Byte.valueOf(b));
		}
	}
	
	public static final byte[] unbox(Byte... bytes) {
		byte[] array = new byte[bytes.length];
		int i = 0;
		for(Byte b : bytes) {
			array[i++] = b.byteValue();
		}
		return array;
	}
	
	public static final byte[] unboxBytes(Collection<Byte> bytes) {
		byte[] array = new byte[bytes.size()];
		int i = 0;
		for(Byte b : bytes) {
			array[i++] = b.byteValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Character> collection, char... chars) {
		for(char c : chars) {
			collection.add(Character.valueOf(c));
		}
	}
	
	public static final char[] unbox(Character... chars) {
		char[] array = new char[chars.length];
		int i = 0;
		for(Character c : chars) {
			array[i++] = c.charValue();
		}
		return array;
	}
	
	public static final char[] unboxCharacters(Collection<Character> chars) {
		char[] array = new char[chars.size()];
		int i = 0;
		for(Character c : chars) {
			array[i++] = c.charValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Short> collection, short... shorts) {
		for(short s : shorts) {
			collection.add(Short.valueOf(s));
		}
	}
	
	public static final short[] unbox(Short... shorts) {
		short[] array = new short[shorts.length];
		int i = 0;
		for(Short s : shorts) {
			array[i++] = s.shortValue();
		}
		return array;
	}
	
	public static final short[] unboxShorts(Collection<Short> shorts) {
		short[] array = new short[shorts.size()];
		int i = 0;
		for(Short s : shorts) {
			array[i++] = s.shortValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Integer> collection, int... ints) {
		for(int i : ints) {
			collection.add(Integer.valueOf(i));
		}
	}
	
	public static final int[] unbox(Integer... ints) {
		int[] array = new int[ints.length];
		int j = 0;
		for(Integer i : ints) {
			array[j++] = i.intValue();
		}
		return array;
	}
	
	public static final int[] unboxIntegers(Collection<Integer> ints) {
		int[] array = new int[ints.size()];
		int j = 0;
		for(Integer i : ints) {
			array[j++] = i.intValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Float> collection, float... floats) {
		for(float f : floats) {
			collection.add(Float.valueOf(f));
		}
	}
	
	public static final float[] unbox(Float... floats) {
		float[] array = new float[floats.length];
		int i = 0;
		for(Float f : floats) {
			array[i++] = f.floatValue();
		}
		return array;
	}
	
	public static final float[] unboxFloats(Collection<Float> floats) {
		float[] array = new float[floats.size()];
		int i = 0;
		for(Float f : floats) {
			array[i++] = f.floatValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Long> collection, long... longs) {
		for(long l : longs) {
			collection.add(Long.valueOf(l));
		}
	}
	
	public static final long[] unbox(Long... longs) {
		long[] array = new long[longs.length];
		int i = 0;
		for(Long l : longs) {
			array[i++] = l.longValue();
		}
		return array;
	}
	
	public static final long[] unboxLongs(Collection<Long> longs) {
		long[] array = new long[longs.size()];
		int i = 0;
		for(Long l : longs) {
			array[i++] = l.longValue();
		}
		return array;
	}
	
	public static final void boxAdd(Collection<Double> collection, double... doubles) {
		for(double d : doubles) {
			collection.add(Double.valueOf(d));
		}
	}
	
	public static final double[] unbox(Double... doubles) {
		double[] array = new double[doubles.length];
		int i = 0;
		for(Double d : doubles) {
			array[i++] = d.doubleValue();
		}
		return array;
	}
	
	public static final double[] unboxDoubles(Collection<Double> doubles) {
		double[] array = new double[doubles.size()];
		int i = 0;
		for(Double d : doubles) {
			array[i++] = d.doubleValue();
		}
		return array;
	}
	
	public static final boolean doAnyElementsMatch(boolean[] v, boolean b) {
		if(v == null) {
			return false;
		}
		for(boolean vB : v) {
			if(vB == b) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean equals(boolean[] v, boolean b) {
		if(v == null) {
			return false;
		}
		for(boolean vB : v) {
			if(vB != b) {
				return false;
			}
		}
		return true;
	}
	
	public static final boolean equals(boolean[] v1, boolean[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(v1[i] != v2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static final boolean doAnyElementsMatch(int[] v, int i) {
		if(v == null) {
			return false;
		}
		for(int vI : v) {
			if(vI == i) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean equals(int[] v, int i) {
		if(v == null) {
			return false;
		}
		for(int vI : v) {
			if(vI != i) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equals(int[] v1, int[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(v1[i] != v2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static final boolean doAnyElementsMatch(float[] v, float f) {
		if(v == null) {
			return false;
		}
		for(float vF : v) {
			if(vF == f) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean doAnyElementsMatchExact(float[] v, float f) {
		if(v == null) {
			return false;
		}
		for(float vF : v) {
			if(Float.floatToRawIntBits(vF) == Float.floatToRawIntBits(f)) {
				return true;
			}
		}
		return false;
	}
	
	public static final strictfp boolean equals(float[] v, float f) {
		if(v == null) {
			return false;
		}
		for(float vF : v) {
			if(vF != f) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equalsExact(float[] v, float f) {
		if(v == null) {
			return false;
		}
		for(float vF : v) {
			if(Float.floatToRawIntBits(vF) != Float.floatToRawIntBits(f)) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equals(float[] v1, float[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(v1[i] != v2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equalsExact(float[] v1, float[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(Float.floatToRawIntBits(v1[i]) != Float.floatToRawIntBits(v2[i])) {
				return false;
			}
		}
		return true;
	}
	
	public static final boolean doAnyElementsMatch(long[] v, long l) {
		if(v == null) {
			return false;
		}
		for(long vL : v) {
			if(vL == l) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean equals(long[] v, long l) {
		if(v == null) {
			return false;
		}
		for(long vL : v) {
			if(vL != l) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equals(long[] v1, long[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(v1[i] != v2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static final boolean doAnyElementsMatch(double[] v, double d) {
		if(v == null) {
			return false;
		}
		for(double vD : v) {
			if(vD == d) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean doAnyElementsMatchExact(double[] v, double d) {
		if(v == null) {
			return false;
		}
		for(double vD : v) {
			if(Double.doubleToRawLongBits(vD) == Double.doubleToRawLongBits(d)) {
				return true;
			}
		}
		return false;
	}
	
	public static final strictfp boolean equals(double[] v, double d) {
		if(v == null) {
			return false;
		}
		for(double vD : v) {
			if(vD != d) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equalsExact(double[] v, double d) {
		if(v == null) {
			return false;
		}
		for(double vD : v) {
			if(Double.doubleToRawLongBits(vD) != Double.doubleToRawLongBits(d)) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equals(double[] v1, double[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(v1[i] != v2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp boolean equalsExact(double[] v1, double[] v2) {
		if(v1 == null) {
			return v2 == null;
		}
		if(v2 == null) {
			return false;
		}
		if(v1.length != v2.length) {
			return false;
		}
		for(int i = 0; i < v1.length; i++) {
			if(Double.doubleToRawLongBits(v1[i]) != Double.doubleToRawLongBits(v2[i])) {
				return false;
			}
		}
		return true;
	}
	
	public static final strictfp int[] abs(int[] v) {
		int[] abs = new int[v.length];
		for(int i = 0; i < abs.length; i++) {
			abs[i] = Math.abs(v[i]);
		}
		return abs;
	}
	
	public static final strictfp float[] abs(float[] v) {
		float[] abs = new float[v.length];
		for(int i = 0; i < abs.length; i++) {
			abs[i] = Math.abs(v[i]);
		}
		return abs;
	}
	
	public static final strictfp long[] abs(long[] v) {
		long[] abs = new long[v.length];
		for(int i = 0; i < abs.length; i++) {
			abs[i] = Math.abs(v[i]);
		}
		return abs;
	}
	
	public static final strictfp double[] abs(double[] v) {
		double[] abs = new double[v.length];
		for(int i = 0; i < abs.length; i++) {
			abs[i] = Math.abs(v[i]);
		}
		return abs;
	}
	
	public static final strictfp int[] add(int[] v1, int[] v2) {
		int length = Math.min(v1.length, v2.length);
		int[] sum = new int[length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v1[i] + v2[i];
		}
		return sum;
	}
	
	public static final strictfp int[] add(int[] v, int value) {
		int[] sum = new int[v.length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v[i] + value;
		}
		return sum;
	}
	
	public static final strictfp float[] add(float[] v1, float[] v2) {
		int length = Math.min(v1.length, v2.length);
		float[] sum = new float[length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v1[i] + v2[i];
		}
		return sum;
	}
	
	public static final strictfp float[] addDivisibly(float[] augend, float[] addend, boolean copySign) {
		if(augend.length < addend.length) {
			float[] tmp = augend;
			augend = addend;
			addend = tmp;
		}
		
		if(augend.length % addend.length != 0) {
			throw new IllegalArgumentException(String.format("augend.length (%s) is not divisible by addend.length! (%s)", Integer.toString(augend.length), Integer.toString(addend.length)));
		}
		float[] sum = new float[augend.length];
		/*for(int i = 0; i < sum.length;) {
			for(float f : addend) {
				sum[i] = augend[i] + f;
				i++;
			}
		}*/
		for(int i = 0; i < sum.length; i++) {
			sum[i] = augend[i] + (addend[i % addend.length] * (copySign && augend[i] < 0 ? -1.0f : 1.0f));
		}
		return sum;
	}
	
	public static final strictfp float[] add(float[] v, float value) {
		float[] sum = new float[v.length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v[i] + value;
		}
		return sum;
	}
	
	public static final strictfp long[] add(long[] v1, long[] v2) {
		int length = Math.min(v1.length, v2.length);
		long[] sum = new long[length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v1[i] + v2[i];
		}
		return sum;
	}
	
	public static final strictfp long[] add(long[] v, long value) {
		long[] sum = new long[v.length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v[i] + value;
		}
		return sum;
	}
	
	public static final strictfp double[] add(double[] v1, double[] v2) {
		int length = Math.min(v1.length, v2.length);
		double[] sum = new double[length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v1[i] + v2[i];
		}
		return sum;
	}
	
	public static final strictfp double[] add(double[] v, double value) {
		double[] sum = new double[v.length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v[i] + value;
		}
		return sum;
	}
	
	public static final strictfp int[] subtract(int[] v1, int[] v2) {
		int length = Math.min(v1.length, v2.length);
		int[] difference = new int[length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v1[i] - v2[i];
		}
		return difference;
	}
	
	public static final strictfp int[] subtract(int[] v, int value) {
		int[] difference = new int[v.length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v[i] - value;
		}
		return difference;
	}
	
	public static final strictfp float[] subtract(float[] v1, float[] v2) {
		int length = Math.min(v1.length, v2.length);
		float[] difference = new float[length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v1[i] - v2[i];
		}
		return difference;
	}
	
	public static final strictfp float[] subtract(float[] v, float value) {
		float[] difference = new float[v.length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v[i] - value;
		}
		return difference;
	}
	
	public static final strictfp long[] subtract(long[] v1, long[] v2) {
		int length = Math.min(v1.length, v2.length);
		long[] difference = new long[length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v1[i] - v2[i];
		}
		return difference;
	}
	
	public static final strictfp long[] subtract(long[] v, long value) {
		long[] difference = new long[v.length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v[i] - value;
		}
		return difference;
	}
	
	public static final strictfp double[] subtract(double[] v1, double[] v2) {
		int length = Math.min(v1.length, v2.length);
		double[] difference = new double[length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v1[i] - v2[i];
		}
		return difference;
	}
	
	public static final strictfp double[] subtract(double[] v, double value) {
		double[] difference = new double[v.length];
		for(int i = 0; i < difference.length; i++) {
			difference[i] = v[i] - value;
		}
		return difference;
	}
	
	public static final strictfp int[] multiply(int[] v1, int[] v2) {
		int length = Math.min(v1.length, v2.length);
		int[] product = new int[length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v1[i] * v2[i];
		}
		return product;
	}
	
	public static final strictfp int[] multiply(int[] v, int value) {
		int[] product = new int[v.length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v[i] * value;
		}
		return product;
	}
	
	public static final strictfp float[] multiply(float[] v1, float[] v2) {
		int length = Math.min(v1.length, v2.length);
		float[] product = new float[length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v1[i] * v2[i];
		}
		return product;
	}
	
	public static final strictfp float[] multiply(float[] v, float value) {
		float[] product = new float[v.length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v[i] * value;
		}
		return product;
	}
	
	public static final strictfp long[] multiply(long[] v1, long[] v2) {
		int length = Math.min(v1.length, v2.length);
		long[] product = new long[length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v1[i] * v2[i];
		}
		return product;
	}
	
	public static final strictfp long[] multiply(long[] v, long value) {
		long[] product = new long[v.length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v[i] * value;
		}
		return product;
	}
	
	public static final strictfp double[] multiply(double[] v1, double[] v2) {
		int length = Math.min(v1.length, v2.length);
		double[] product = new double[length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v1[i] * v2[i];
		}
		return product;
	}
	
	public static final strictfp double[] multiply(double[] v, double value) {
		double[] product = new double[v.length];
		for(int i = 0; i < product.length; i++) {
			product[i] = v[i] * value;
		}
		return product;
	}
	
	public static final strictfp int[] divide(int[] v1, int[] v2) {
		int length = Math.min(v1.length, v2.length);
		int[] quotient = new int[length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v1[i] / v2[i];
		}
		return quotient;
	}
	
	public static final strictfp int[] divide(int divisor, int[] v) {
		int[] quotient = new int[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = divisor / v[i];
		}
		return quotient;
	}
	
	public static final strictfp int[] divide(int[] v, int dividend) {
		int[] quotient = new int[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v[i] / dividend;
		}
		return quotient;
	}
	
	public static final strictfp float[] divide(float[] v1, float[] v2) {
		int length = Math.min(v1.length, v2.length);
		float[] quotient = new float[length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v1[i] / v2[i];
		}
		return quotient;
	}
	
	public static final strictfp float[] divide(float divisor, float[] v) {
		float[] quotient = new float[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = divisor / v[i];
		}
		return quotient;
	}
	
	public static final strictfp float[] divide(float[] v, float dividend) {
		float[] quotient = new float[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v[i] / dividend;
		}
		return quotient;
	}
	
	public static final strictfp long[] divide(long[] v1, long[] v2) {
		int length = Math.min(v1.length, v2.length);
		long[] quotient = new long[length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v1[i] / v2[i];
		}
		return quotient;
	}
	
	public static final strictfp long[] divide(long divisor, long[] v) {
		long[] quotient = new long[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = divisor / v[i];
		}
		return quotient;
	}
	
	public static final strictfp long[] divide(long[] v, long dividend) {
		long[] quotient = new long[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v[i] / dividend;
		}
		return quotient;
	}
	
	public static final strictfp double[] divide(double[] v1, double[] v2) {
		int length = Math.min(v1.length, v2.length);
		double[] quotient = new double[length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v1[i] / v2[i];
		}
		return quotient;
	}
	
	public static final strictfp double[] divide(double divisor, double[] v) {
		double[] quotient = new double[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = divisor / v[i];
		}
		return quotient;
	}
	
	public static final strictfp double[] divide(double[] v, double dividend) {
		double[] quotient = new double[v.length];
		for(int i = 0; i < quotient.length; i++) {
			quotient[i] = v[i] / dividend;
		}
		return quotient;
	}
	
	public static final strictfp int[] modulus(int[] v1, int[] v2, boolean copySign) {
		int length = Math.min(v1.length, v2.length);
		int[] sum = new int[length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v1[i] % v2[i];
			if(copySign) {
				if(v2[i] > 0 && sum[i] < 0) {
					sum[i] += v2[i];
				} else if(v2[i] < 0 && sum[i] > 0) {
					sum[i] -= v2[i];
				}
			}
		}
		return sum;
	}
	
	public static final strictfp int[] modulus(int[] v1, int[] v2) {
		return modulus(v1, v2, false);
	}
	
	public static final strictfp int[] modulus(int[] v, int value, boolean copySign) {
		int[] sum = new int[v.length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v[i] % value;
			if(copySign) {
				if(value > 0 && sum[i] < 0) {
					sum[i] += value;
				} else if(value < 0 && sum[i] > 0) {
					sum[i] -= value;
				}
			}
		}
		return sum;
	}
	
	public static final strictfp int[] modulus(int[] v1, int value) {
		return modulus(v1, value, false);
	}
	
	public static final strictfp int modulus(int i, int m, boolean copySign) {
		i = i % m;
		if(copySign) {
			if(m > 0 && i < 0) {
				i += m;
			} else if(m < 0 && i > 0) {
				i -= m;
			}
		}
		return i;
	}
	
	public static final strictfp float[] modulus(float[] v1, float[] v2, boolean copySign) {
		int length = Math.min(v1.length, v2.length);
		float[] sum = new float[length];
		for(int i = 0; i < sum.length; i++) {
			sum[i] = v1[i] % v2[i];
			if(copySign) {
				if(v2[i] > 0 && sum[i] < 0) {
					sum[i] += v2[i];
				} else if(v2[i] < 0 && sum[i] > 0) {
					sum[i] -= v2[i];
				}
			}
		}
		return sum;
	}
	
	public static final strictfp float[] modulus(float[] v1, float[] v2) {
		return modulus(v1, v2, false);
	}
	
	public static final strictfp float[] modulus(float[] v, float value, boolean copySign) {
		float[] mod = new float[v.length];
		for(int i = 0; i < mod.length; i++) {
			mod[i] = v[i] % value;
			if(copySign) {
				if(value > 0 && mod[i] < 0) {
					mod[i] += value;
				} else if(value < 0 && mod[i] > 0) {
					mod[i] -= value;
				}
			}
		}
		return mod;
	}
	
	public static final strictfp float[] modulus(float[] v, float value) {
		return modulus(v, value, false);
	}
	
	public static final strictfp long[] modulus(long[] v1, long[] v2, boolean copySign) {
		int length = Math.min(v1.length, v2.length);
		long[] mod = new long[length];
		for(int i = 0; i < mod.length; i++) {
			mod[i] = v1[i] % v2[i];
			if(copySign) {
				if(v2[i] > 0 && mod[i] < 0) {
					mod[i] += v2[i];
				} else if(v2[i] < 0 && mod[i] > 0) {
					mod[i] -= v2[i];
				}
			}
		}
		return mod;
	}
	
	public static final strictfp long[] modulus(long[] v1, long[] v2) {
		return modulus(v1, v2, false);
	}
	
	public static final strictfp long[] modulus(long[] v, long value, boolean copySign) {
		long[] mod = new long[v.length];
		for(int i = 0; i < mod.length; i++) {
			mod[i] = v[i] % value;
			if(copySign) {
				if(value > 0 && mod[i] < 0) {
					mod[i] += value;
				} else if(value < 0 && mod[i] > 0) {
					mod[i] -= value;
				}
			}
		}
		return mod;
	}
	
	public static final strictfp long[] modulus(long[] v, long value) {
		return modulus(v, value, false);
	}
	
	public static final strictfp double[] modulus(double[] v1, double[] v2, boolean copySign) {
		int length = Math.min(v1.length, v2.length);
		double[] mod = new double[length];
		for(int i = 0; i < mod.length; i++) {
			mod[i] = v1[i] % v2[i];
			if(copySign) {
				if(v2[i] > 0 && mod[i] < 0) {
					mod[i] += v2[i];
				} else if(v2[i] < 0 && mod[i] > 0) {
					mod[i] -= v2[i];
				}
			}
		}
		return mod;
	}
	
	public static final strictfp double[] modulus(double[] v1, double[] v2) {
		return modulus(v1, v2, false);
	}
	
	public static final strictfp double[] modulus(double[] v, double value, boolean copySign) {
		double[] mod = new double[v.length];
		for(int i = 0; i < mod.length; i++) {
			mod[i] = v[i] % value;
			if(copySign) {
				if(value > 0 && mod[i] < 0) {
					mod[i] += value;
				} else if(value < 0 && mod[i] > 0) {
					mod[i] -= value;
				}
			}
		}
		return mod;
	}
	
	public static final strictfp double[] modulus(double[] v, double value) {
		return modulus(v, value, false);
	}
	
	public static final strictfp float[] cross(float[] u, float[] v) {
		float uvi, uvj, uvk;
		uvi = (u[1] * v[2]) - (v[1] * u[2]);
		uvj = (v[0] * u[2]) - (u[0] * v[2]);
		uvk = (u[0] * v[1]) - (v[0] * u[1]);
		return new float[] {uvi, uvj, uvk};
	}
	
	public static final strictfp double[] cross(double[] u, double[] v) {
		double uvi, uvj, uvk;
		uvi = u[1] * v[2] - v[1] * u[2];
		uvj = v[0] * u[2] - u[0] * v[2];
		uvk = u[0] * v[1] - v[0] * u[1];
		return new double[] {uvi, uvj, uvk};
	}
	
	public static final strictfp float dot(float[] v1, float[] v2) {
		int length = Math.min(v1.length, v2.length);
		float sum = 0x0.0p0f;
		for(int i = 0; i < length; i++) {
			sum += v1[i] * v2[i];
		}
		return sum;
	}
	
	public static final strictfp double dot(double[] v1, double[] v2) {
		int length = Math.min(v1.length, v2.length);
		double sum = 0x0.0p0;
		for(int i = 0; i < length; i++) {
			sum += v1[i] * v2[i];
		}
		return sum;
	}
	
	public static final strictfp float[] pow(float[] v, float pow) {
		float[] r = new float[v.length];
		for(int i = 0; i < r.length; i++) {
			r[i] = (float) Math.pow(v[i], pow);
		}
		return r;
	}
	
	public static final strictfp double[] pow(double[] v, double pow) {
		double[] r = new double[v.length];
		for(int i = 0; i < r.length; i++) {
			r[i] = Math.pow(v[i], pow);
		}
		return r;
	}
	
	public static final strictfp float[] pow(float[] v1, float[] v2) {
		float[] r = new float[Math.min(v1.length, v2.length)];
		for(int i = 0; i < r.length; i++) {
			r[i] = (float) Math.pow(v1[i], v2[i]);
		}
		return r;
	}
	
	public static final strictfp double[] pow(double[] v1, double[] v2) {
		double[] r = new double[Math.min(v1.length, v2.length)];
		for(int i = 0; i < r.length; i++) {
			r[i] = Math.pow(v1[i], v2[i]);
		}
		return r;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final byte sum(byte[] v, boolean unsigned) {
		byte sum = 0;
		for(byte b : v) {
			sum += unsigned ? (b & 0xFF) : b;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final int sumToInt(byte[] v, boolean unsigned) {
		int sum = 0;
		for(byte b : v) {
			sum += unsigned ? (b & 0xFF) : b;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final long sumToLong(byte[] v, boolean unsigned) {
		long sum = 0;
		for(byte b : v) {
			sum += unsigned ? (b & 0xFF) : b;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final short sum(short[] v) {
		short sum = 0;
		for(short s : v) {
			sum += s;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final int sum(int[] v) {
		int sum = 0;
		for(int i : v) {
			sum += i;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final strictfp float sum(float[] v) {
		float sum = 0x0.0p0f;
		for(float f : v) {
			sum += f;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final long sum(long[] v) {
		long sum = 0;
		for(long l : v) {
			sum += l;
		}
		return sum;
	}
	
	/** @param v The vector whose entries will be added together
	 * @return The sum of all of the given vector's entries */
	public static final strictfp double sum(double[] v) {
		double sum = 0x0.0p0;
		for(double d : v) {
			sum += d;
		}
		return sum;
	}
	
	public static final strictfp float difference(float[] v) {
		float difference = 0x0.0p0f;
		for(float f : v) {
			difference = f - difference;
		}
		return difference;
	}
	
	public static final strictfp double difference(double[] v) {
		double difference = 0x0.0p0f;
		for(double f : v) {
			difference = f - difference;
		}
		return difference;
	}
	
	public static final strictfp float product(float[] v) {
		float product = 1.0f;
		for(float d : v) {
			product *= d;
		}
		return product;
	}
	
	public static final strictfp double product(double[] v) {
		double product = 1.0;
		for(double d : v) {
			product *= d;
		}
		return product;
	}
	
	public static final strictfp float quotient(float[] v) {
		float quotient = 1.0f;
		for(float d : v) {
			quotient = d / quotient;
		}
		return quotient;
	}
	
	public static final strictfp double quotient(double[] v) {
		double quotient = 1.0;
		for(double d : v) {
			quotient = d / quotient;
		}
		return quotient;
	}
	
	public static final strictfp float getDistanceBetween(float[] v1, float[] v2) {
		float[] diff = subtract(v2, v1);
		float[] squared = pow(diff, 2.0f);
		float magnitude = sum(squared);
		return (float) Math.sqrt(magnitude);
	}
	
	public static final strictfp double getDistanceBetween(double[] v1, double[] v2) {
		double[] diff = subtract(v2, v1);
		double[] squared = pow(diff, 2.0);
		double magnitude = sum(squared);
		return Math.sqrt(magnitude);
	}
	
	//@formatter:off
	/** @return <table border=1 cellPadding=6 cellSpacing=0>
	 * <tr><td>1.0f</td><td>0.0f</td><td>0.0f</td><td>0.0f</td></tr>
	 * <tr><td>0.0f</td><td>1.0f</td><td>0.0f</td><td>0.0f</td></tr>
	 * <tr><td>0.0f</td><td>0.0f</td><td>1.0f</td><td>0.0f</td></tr>
	 * <tr><td>0.0f</td><td>0.0f</td><td>0.0f</td><td>1.0f</td></tr>
	 * </table> *///@formatter:on
	public static final strictfp float[] getIdentityf() {
		return new float[] {//
				1, 0, 0, 0,//
				0, 1, 0, 0,//
				0, 0, 1, 0,//
				0, 0, 0, 1//
		};
	}
	
	//@formatter:off
	/** @return <table border=1 cellPadding=6 cellSpacing=0>
	 * <tr><td>1.0</td><td>0.0</td><td>0.0</td><td>0.0</td></tr>
	 * <tr><td>0.0</td><td>1.0</td><td>0.0</td><td>0.0</td></tr>
	 * <tr><td>0.0</td><td>0.0</td><td>1.0</td><td>0.0</td></tr>
	 * <tr><td>0.0</td><td>0.0</td><td>0.0</td><td>1.0</td></tr>
	 * </table> *///@formatter:on
	public static final strictfp double[] getIdentityd() {
		return new double[] {//
				1, 0, 0, 0,//
				0, 1, 0, 0,//
				0, 0, 1, 0,//
				0, 0, 0, 1//
		};
	}
	
	//@formatter:off
	/** @param m The matrix to be transposed
	 * @return Example matrix before transposing:<br>
	 * Row-Major Order (-↩-↩-↩-)<br>
	 * <table border=1 cellPadding=6 cellSpacing=0>
	 * <tr><td>&nbsp;0.0f</td><td>&nbsp;1.0f</td><td>&nbsp;2.0f</td><td>&nbsp;3.0f</td></tr>
	 * <tr><td>&nbsp;4.0f</td><td>&nbsp;5.0f</td><td>&nbsp;6.0f</td><td>&nbsp;7.0f</td></tr>
	 * <tr><td>&nbsp;8.0f</td><td>&nbsp;9.0f</td><td>10.0f</td><td>11.0f</td></tr>
	 * <tr><td>12.0f</td><td>13.0f</td><td>14.0f</td><td>15.0f</td></tr>
	 * </table><br>
	 * After transposing:<br>
	 * Column-Major Order (|↗|↗|↗|, used by OpenGL)<br>
	 * <table border=1 cellPadding=6 cellSpacing=0>
	 * <tr><td>&nbsp;0.0f&nbsp;</td><td>&nbsp;4.0f&nbsp;</td><td>&nbsp;8.0f</td><td>12.0f</td></tr>
	 * <tr><td>&nbsp;1.0f&nbsp;</td><td>&nbsp;5.0f&nbsp;</td><td>&nbsp;9.0f</td><td>13.0f</td></tr>
	 * <tr><td>&nbsp;2.0f&nbsp;</td><td>&nbsp;6.0f&nbsp;</td><td>10.0f</td><td>14.0f</td></tr>
	 * <tr><td>&nbsp;3.0f&nbsp;</td><td>&nbsp;7.0f&nbsp;</td><td>11.0f</td><td>15.0f</td></tr>
	 * </table>*///@formatter:on
	public static final strictfp float[] transpose(float[] m) {
		float[] mat = new float[16];
		mat[0] = m[0];//
		mat[1] = m[4];
		mat[2] = m[8];
		mat[3] = m[12];
		
		mat[4] = m[1];
		mat[5] = m[5];//
		mat[6] = m[9];
		mat[7] = m[13];
		
		mat[8] = m[2];
		mat[9] = m[6];
		mat[10] = m[10];//
		mat[11] = m[14];
		
		mat[12] = m[3];
		mat[13] = m[7];
		mat[14] = m[11];
		mat[15] = m[15];//
		return mat;
	}
	
	//@formatter:off
	/** @param m The matrix to be transposed
	 * @return Example matrix before transposing:<br>
	 * Row-Major Order (-↩-↩-↩-)<br>
	 * <table border=1 cellPadding=6 cellSpacing=0>
	 * <tr><td>&nbsp;0.0</td><td>&nbsp;1.0</td><td>&nbsp;2.0</td><td>&nbsp;3.0</td></tr>
	 * <tr><td>&nbsp;4.0</td><td>&nbsp;5.0</td><td>&nbsp;6.0</td><td>&nbsp;7.0</td></tr>
	 * <tr><td>&nbsp;8.0</td><td>&nbsp;9.0</td><td>10.0</td><td>11.0</td></tr>
	 * <tr><td>12.0</td><td>13.0</td><td>14.0</td><td>15.0</td></tr>
	 * </table><br>
	 * After transposing:<br>
	 * Column-Major Order (|↗|↗|↗|, used by OpenGL)<br>
	 * <table border=1 cellPadding=6 cellSpacing=0>
	 * <tr><td>&nbsp;0.0&nbsp;</td><td>&nbsp;4.0&nbsp;</td><td>&nbsp;8.0</td><td>12.0</td></tr>
	 * <tr><td>&nbsp;1.0&nbsp;</td><td>&nbsp;5.0&nbsp;</td><td>&nbsp;9.0</td><td>13.0</td></tr>
	 * <tr><td>&nbsp;2.0&nbsp;</td><td>&nbsp;6.0&nbsp;</td><td>10.0</td><td>14.0</td></tr>
	 * <tr><td>&nbsp;3.0&nbsp;</td><td>&nbsp;7.0&nbsp;</td><td>11.0</td><td>15.0</td></tr>
	 * </table>*///@formatter:on
	public static final strictfp double[] transpose(double[] m) {
		double[] mat = new double[16];
		mat[0] = m[0];//
		mat[1] = m[4];
		mat[2] = m[8];
		mat[3] = m[12];
		
		mat[4] = m[1];
		mat[5] = m[5];//
		mat[6] = m[9];
		mat[7] = m[13];
		
		mat[8] = m[2];
		mat[9] = m[6];
		mat[10] = m[10];//
		mat[11] = m[14];
		
		mat[12] = m[3];
		mat[13] = m[7];
		mat[14] = m[11];
		mat[15] = m[15];//
		return mat;
	}
	
	public static final strictfp float[] getOrthographicMatrixf(float x, float y, float width, float height, float zNear, float zFar) {
		float R = x + width;//x + (width - 1.0f);
		float L = x;
		
		float T = y + height;//y;
		float B = y;//y + (height - 1.0f);
		
		return transpose(new float[] {//
				2.0f / (R - L), 0, 0, -((R + L) / (R - L)),//
				0, 2.0f / (T - B), 0, -((T + B) / (T - B)),//
				0, 0, 1.0f / (zFar - zNear), -(zNear / (zFar - zNear)),//
				0, 0, 0, 1.0f//
		});
	}
	
	public static final strictfp double[] getOrthographicMatrixd(double x, double y, double width, double height, double zNear, double zFar) {
		double R = x + width;//x + (width - 1.0);
		double L = x;
		
		double T = y + height;//y;
		double B = y;//y + (height - 1.0);
		
		return transpose(new double[] {//
				2.0f / (R - L), 0, 0, -((R + L) / (R - L)),//
				0, 2.0f / (T - B), 0, -((T + B) / (T - B)),//
				0, 0, 1.0f / (zFar - zNear), -(zNear / (zFar - zNear)),//
				0, 0, 0, 1.0f//
		});
	}
	
	/** Multiply the angleDeg by this number to get radians */
	protected static final double toRadiansMultiplier = 0.01745329251994329576923690768489;	//3.141592653589793238462643383279502884197 / 180.0;
	/** Multiply the angleRad by this number to get degrees */
	protected static final double toDegreesMultiplier = 57.295779513082320876798154814105;	//180.0 / 3.141592653589793238462643383279502884197;
	/** Divide the angleDeg by this number to get radians */
	protected static final double toRadiansDenominator = toDegreesMultiplier;
	/** Divide the angleRad by this number to get degrees */
	protected static final double toDegreesDenominator = toRadiansMultiplier;
	protected static final long posZero = Double.doubleToLongBits(0.0);
	protected static final long negZero = Double.doubleToLongBits(-0.0);
	protected static final int posZerof = Float.floatToIntBits(0.0f);
	protected static final int negZerof = Float.floatToIntBits(-0.0f);
	
	/** The ratio of the circumference of a circle divided by its diameter. */
	public static final double π = 3.141592653589793;
	/** The ratio of the circumference of a circle divided by its radius. */
	public static final double τ = 6.283185307179586;
	/** The ratio of the circumference of a circle divided by its diameter. */
	public static final float πf = 3.14159265f;
	/** The ratio of the circumference of a circle divided by its radius. */
	public static final float τf = 6.2831853f;
	
	/** One quarter of the distance about a circle's circumference */
	public static final double quarterCircle = π / 2.0;
	/** One quarter of the distance about a circle's circumference */
	public static final float quarterCirclef = πf / 2.0f;
	
	public static final strictfp float[] getPerspectiveMatrixf(float fovy, float aspect, float zNear, float zFar, boolean rightHandedProjection) {
		float q = Double.valueOf(1.0 / Math.tan((fovy * toRadiansMultiplier) / 2.0)).floatValue();
		float A = q / aspect;
		float B = (zNear + zFar) / (zNear - zFar);
		float C = ((rightHandedProjection ? 2.0f : -2.0f) * (zNear * zFar)) / (zNear - zFar);
		float z = rightHandedProjection ? -1.0f : 1.0f;
		
		return new float[] {//
				A, 0, 0, 0,//
				0, q, 0, 0,//
				0, 0, B, z,//
				0, 0, C, 0//
		};
	}
	
	public static final strictfp double[] getPerspectiveMatrixd(double fovy, double aspect, double zNear, double zFar, boolean rightHandedProjection) {
		double q = 1.0 / Math.tan((fovy * toRadiansMultiplier) / 2.0);
		double A = q / aspect;
		double B = (zNear + zFar) / (zNear - zFar);
		double C = ((rightHandedProjection ? 2.0 : -2.0) * (zNear * zFar)) / (zNear - zFar);
		double z = rightHandedProjection ? -1.0 : 1.0;
		
		return new double[] {//
				A, 0, 0, 0,//
				0, q, 0, 0,//
				0, 0, B, z,//
				0, 0, C, 0//
		};
	}
	
	public static final strictfp double[] getPerspectiveMatrixd(double fovy, double aspect, double zNear, double zFar) {
		return getPerspectiveMatrixd(fovy, aspect, zNear, zFar, true);
	}
	
	public static final strictfp float[] multMatrix4x4f(float[] m1, float[] m2) {
		return new float[] {//@formatter:off
				(m1[0] * m2[0]) + (m1[1] * m2[4]) + (m1[2] * m2[8]) + (m1[3] * m2[12]),//0
				(m1[0] * m2[1]) + (m1[1] * m2[5]) + (m1[2] * m2[9]) + (m1[3] * m2[13]),//1
				(m1[0] * m2[2]) + (m1[1] * m2[6]) + (m1[2] * m2[10]) + (m1[3] * m2[14]),//2
				(m1[0] * m2[3]) + (m1[1] * m2[7]) + (m1[2] * m2[11]) + (m1[3] * m2[15]),//3
				
				(m1[4] * m2[0]) + (m1[5] * m2[4]) + (m1[6] * m2[8]) + (m1[7] * m2[12]),//4
				(m1[4] * m2[1]) + (m1[5] * m2[5]) + (m1[6] * m2[9]) + (m1[7] * m2[13]),//5
				(m1[4] * m2[2]) + (m1[5] * m2[6]) + (m1[6] * m2[10]) + (m1[7] * m2[14]),//6
				(m1[4] * m2[3]) + (m1[5] * m2[7]) + (m1[6] * m2[11]) + (m1[7] * m2[15]),//7
				
				(m1[8] * m2[0]) + (m1[9] * m2[4]) + (m1[10] * m2[8]) + (m1[11] * m2[12]),//8
				(m1[8] * m2[1]) + (m1[9] * m2[5]) + (m1[10] * m2[9]) + (m1[11] * m2[13]),//9
				(m1[8] * m2[2]) + (m1[9] * m2[6]) + (m1[10] * m2[10]) + (m1[11] * m2[14]),//10
				(m1[8] * m2[3]) + (m1[9] * m2[7]) + (m1[10] * m2[11]) + (m1[11] * m2[15]),//11
				
				(m1[12] * m2[0]) + (m1[13] * m2[4]) + (m1[14] * m2[8]) + (m1[15] * m2[12]),//12
				(m1[12] * m2[1]) + (m1[13] * m2[5]) + (m1[14] * m2[9]) + (m1[15] * m2[13]),//13
				(m1[12] * m2[2]) + (m1[13] * m2[6]) + (m1[14] * m2[10]) + (m1[15] * m2[14]),//14
				(m1[12] * m2[3]) + (m1[13] * m2[7]) + (m1[14] * m2[11]) + (m1[15] * m2[15])//15
		};//@formatter:on
	}
	
	public static final strictfp double[] multMatrix4x4d(double[] m1, double[] m2) {
		return new double[] {//@formatter:off
				(m1[0] * m2[0]) + (m1[1] * m2[4]) + (m1[2] * m2[8]) + (m1[3] * m2[12]),//0
				(m1[0] * m2[1]) + (m1[1] * m2[5]) + (m1[2] * m2[9]) + (m1[3] * m2[13]),//1
				(m1[0] * m2[2]) + (m1[1] * m2[6]) + (m1[2] * m2[10]) + (m1[3] * m2[14]),//2
				(m1[0] * m2[3]) + (m1[1] * m2[7]) + (m1[2] * m2[11]) + (m1[3] * m2[15]),//3
				
				(m1[4] * m2[0]) + (m1[5] * m2[4]) + (m1[6] * m2[8]) + (m1[7] * m2[12]),//4
				(m1[4] * m2[1]) + (m1[5] * m2[5]) + (m1[6] * m2[9]) + (m1[7] * m2[13]),//5
				(m1[4] * m2[2]) + (m1[5] * m2[6]) + (m1[6] * m2[10]) + (m1[7] * m2[14]),//6
				(m1[4] * m2[3]) + (m1[5] * m2[7]) + (m1[6] * m2[11]) + (m1[7] * m2[15]),//7
				
				(m1[8] * m2[0]) + (m1[9] * m2[4]) + (m1[10] * m2[8]) + (m1[11] * m2[12]),//8
				(m1[8] * m2[1]) + (m1[9] * m2[5]) + (m1[10] * m2[9]) + (m1[11] * m2[13]),//9
				(m1[8] * m2[2]) + (m1[9] * m2[6]) + (m1[10] * m2[10]) + (m1[11] * m2[14]),//10
				(m1[8] * m2[3]) + (m1[9] * m2[7]) + (m1[10] * m2[11]) + (m1[11] * m2[15]),//11
				
				(m1[12] * m2[0]) + (m1[13] * m2[4]) + (m1[14] * m2[8]) + (m1[15] * m2[12]),//12
				(m1[12] * m2[1]) + (m1[13] * m2[5]) + (m1[14] * m2[9]) + (m1[15] * m2[13]),//13
				(m1[12] * m2[2]) + (m1[13] * m2[6]) + (m1[14] * m2[10]) + (m1[15] * m2[14]),//14
				(m1[12] * m2[3]) + (m1[13] * m2[7]) + (m1[14] * m2[11]) + (m1[15] * m2[15])//15
		};//@formatter:on
	}
	
	public static final float[] buildTranslate4x4f(float x, float y, float z) {
		return new float[] {//@formatter:off
				1.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f, 0.0f,
				x, y, z, 1.0f
		};//@formatter:on
	}
	
	public static final double[] buildTranslate4x4d(double x, double y, double z) {
		return new double[] {//@formatter:off
				1.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0,
				x, y, z, 1.0
		};//@formatter:on
	}
	
	public static final strictfp float[] translateMatrix4x4f(float[] matrix, float x, float y, float z) {
		return multMatrix4x4f(buildTranslate4x4f(x, y, z), matrix);
	}
	
	public static final strictfp double[] translateMatrix4x4d(double[] matrix, double x, double y, double z) {
		return multMatrix4x4d(buildTranslate4x4d(x, y, z), matrix);
	}
	
	public static final float[] buildScale4x4f(float x, float y, float z) {
		return new float[] {//@formatter:off
				x, 0.0f, 0.0f, 0.0f,
				0.0f, y, 0.0f, 0.0f,
				0.0f, 0.0f, z, 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f,
		};//@formatter:on
	}
	
	public static final double[] buildScale4x4d(double x, double y, double z) {
		return new double[] {//@formatter:off
				x, 0.0, 0.0, 0.0,
				0.0, y, 0.0, 0.0,
				0.0, 0.0, z, 0.0,
				0.0, 0.0, 0.0, 1.0,
		};//@formatter:on
	}
	
	public static final strictfp float[] scaleMatrix4x4f(float[] matrix, float x, float y, float z) {
		return multMatrix4x4f(buildScale4x4f(x, y, z), matrix);
	}
	
	public static final strictfp double[] scaleMatrix4x4d(double[] matrix, double x, double y, double z) {
		return multMatrix4x4d(buildScale4x4d(x, y, z), matrix);
	}
	
	public static final strictfp float[] buildRotateX4x4Radf(float angleRad, float axis) {
		float cos = (float) (Math.cos(angleRad) * axis);
		float sin = (float) (Math.sin(angleRad) * axis);
		return new float[] {1.0f, 0.0f, 0.0f, 0.0f,//
				0.0f, cos, -sin, 0.0f,//
				0.0f, sin, cos, 0.0f,//
				0.0f, 0.0f, 0.0f, 1.0f//
		};
	}
	
	public static final strictfp double[] buildRotateX4x4Radd(double angleRad, double axis) {
		double cos = Math.cos(angleRad) * axis;
		double sin = Math.sin(angleRad) * axis;
		return new double[] {1.0, 0.0, 0.0, 0.0,//
				0.0, cos, -sin, 0.0,//
				0.0, sin, cos, 0.0,//
				0.0, 0.0, 0.0, 1.0//
		};
	}
	
	public static final strictfp float[] buildRotateX4x4Degf(float angleDeg, float axis) {
		return buildRotateX4x4Radf((float) (angleDeg * toRadiansMultiplier), axis);
	}
	
	public static final strictfp double[] buildRotateX4x4Degd(double angleDeg, double axis) {
		return buildRotateX4x4Radd(angleDeg * toRadiansMultiplier, axis);
	}
	
	public static final strictfp float[] buildRotateY4x4Radf(float angleRad, float axis) {
		float cos = (float) (Math.cos(angleRad) * axis);
		float sin = (float) (Math.sin(angleRad) * axis);
		return new float[] {//@formatter:off
				 cos,  0.0f, sin,  0.0f,
				 0.0f, 1.0f, 0.0f, 0.0f,
				-sin,  0.0f, cos,  0.0f,
				 0.0f, 0.0f, 0.0f, 1.0f
		};//@formatter:on
	}
	
	public static final strictfp double[] buildRotateY4x4Radd(double angleRad, double axis) {
		double cos = Math.cos(angleRad) * axis;
		double sin = Math.sin(angleRad) * axis;
		return new double[] {//@formatter:off
				 cos, 0.0, sin, 0.0,
				 0.0, 1.0, 0.0, 0.0,
				-sin, 0.0, cos, 0.0,
				 0.0, 0.0, 0.0, 1.0
		};//@formatter:on
	}
	
	public static final strictfp float[] buildRotateY4x4Degf(float angleDeg, float axis) {
		return buildRotateY4x4Radf((float) (angleDeg * toRadiansMultiplier), axis);
	}
	
	public static final strictfp double[] buildRotateY4x4Degd(double angleDeg, double axis) {
		return buildRotateY4x4Radd(angleDeg * toRadiansMultiplier, axis);
	}
	
	public static final strictfp float[] buildRotateZ4x4Radf(float angleRad, float axis) {
		float cos = (float) (Math.cos(angleRad) * axis);
		float sin = (float) (Math.sin(angleRad) * axis);
		return new float[] {cos, -sin, 0.0f, 0.0f,//
				sin, cos, 0.0f, 0.0f,//
				0.0f, 0.0f, 1.0f, 0.0f,//
				0.0f, 0.0f, 0.0f, 1.0f//
		};
	}
	
	public static final strictfp double[] buildRotateZ4x4Radd(double angleRad, double axis) {
		double cos = Math.cos(angleRad) * axis;
		double sin = Math.sin(angleRad) * axis;
		return new double[] {cos, -sin, 0.0, 0.0,//
				sin, cos, 0.0, 0.0,//
				0.0, 0.0, 1.0, 0.0,//
				0.0, 0.0, 0.0, 1.0//
		};
	}
	
	public static final strictfp float[] buildRotateZ4x4Degf(float angleDeg, float axis) {
		return buildRotateZ4x4Radf((float) (angleDeg * toRadiansMultiplier), axis);
	}
	
	public static final strictfp double[] buildRotateZ4x4Degd(double angleDeg, double axis) {
		return buildRotateZ4x4Radd(angleDeg * toRadiansMultiplier, axis);
	}
	
	public static final strictfp float[] rotateZXYMatrix4x4f(float[] matrix, float angle, float xAxisPitch, float yAxisYaw, float zAxisRoll) {
		int x = Float.floatToIntBits(xAxisPitch), y = Float.floatToIntBits(yAxisYaw), z = Float.floatToIntBits(zAxisRoll);
		float a = (float) (angle * toRadiansMultiplier);
		float c = (float) Math.cos(a);
		float s = (float) Math.sin(a);
		if(z != posZerof && z != negZerof) {
			float cos = c * zAxisRoll;
			float sin = s * zAxisRoll;
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, -sin, 0.0f, 0.0f,
					sin, cos, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(x != posZerof && x != negZerof) {
			float cos = c * xAxisPitch;
			float sin = s * xAxisPitch;
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, cos, -sin, 0.0f,
					0.0f, sin, cos, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(y != posZerof && y != negZerof) {
			float cos = c * yAxisYaw;
			float sin = s * yAxisYaw;
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, 0.0f, sin, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					-sin, 0.0f, cos, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp double[] rotateZXYMatrix4x4d(double[] matrix, double angle, double xAxisPitch, double yAxisYaw, double zAxisRoll) {
		long x = Double.doubleToLongBits(xAxisPitch), y = Double.doubleToLongBits(yAxisYaw), z = Double.doubleToLongBits(zAxisRoll);
		double a = angle * toRadiansMultiplier;
		double c = Math.cos(a);
		double s = Math.sin(a);
		if(z != posZero && z != negZero) {
			double cos = c * zAxisRoll;
			double sin = s * zAxisRoll;
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, -sin, 0.0, 0.0,
					sin, cos, 0.0, 0.0,
					0.0, 0.0, 1.0, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(x != posZero && x != negZero) {
			double cos = c * xAxisPitch;
			double sin = s * xAxisPitch;
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					1.0, 0.0, 0.0, 0.0,//
					0.0, cos, -sin, 0.0,//
					0.0, sin, cos, 0.0,//
					0.0, 0.0, 0.0, 1.0//
			}, matrix);//@formatter:on
		}
		if(y != posZero && y != negZero) {
			double cos = c * yAxisYaw;
			double sin = s * yAxisYaw;
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, 0.0, sin, 0.0,
					0.0, 1.0, 0.0, 0.0,
					-sin, 0.0, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp float[] rotateMatrix4x4f(float[] matrix, float angle, float xAxisPitch, float yAxisYaw, float zAxisRoll) {
		return rotateZXYMatrix4x4f(matrix, angle, xAxisPitch, yAxisYaw, zAxisRoll);
	}
	
	public static final strictfp double[] rotateMatrix4x4d(double[] matrix, double angle, double xAxisPitch, double yAxisYaw, double zAxisRoll) {
		return rotateZXYMatrix4x4d(matrix, angle, xAxisPitch, yAxisYaw, zAxisRoll);
	}
	
	public static final strictfp float[] rotateZXYMatrix4x4f(float[] matrix, float yaw, float pitch, float roll) {
		int p = Float.floatToIntBits(pitch), y = Float.floatToIntBits(yaw), r = Float.floatToIntBits(roll);
		if(r != posZerof && r != negZerof) {
			float a = (float) (roll * toRadiansMultiplier);
			float cos = (float) Math.cos(a);
			float sin = (float) Math.sin(a);
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, -sin, 0.0f, 0.0f,
					sin, cos, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(p != posZerof && p != negZerof) {
			float a = (float) (pitch * toRadiansMultiplier);
			float cos = (float) Math.cos(a);
			float sin = (float) Math.sin(a);
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					1.0f, 0.0f, 0.0f, 0.0f,//
					0.0f, cos, -sin, 0.0f,//
					0.0f, sin, cos, 0.0f,//
					0.0f, 0.0f, 0.0f, 1.0f//
			}, matrix);//@formatter:on
		}
		if(y != posZerof && y != negZerof) {
			float a = (float) (yaw * toRadiansMultiplier);
			float cos = (float) Math.cos(a);
			float sin = (float) Math.sin(a);
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, 0.0f, sin, 0.0f,//
					0.0f, 1.0f, 0.0f, 0.0f,//
					-sin, 0.0f, cos, 0.0f,//
					0.0f, 0.0f, 0.0f, 1.0f//
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp double[] rotateZXYMatrix4x4d(double[] matrix, double yaw, double pitch, double roll) {
		long p = Double.doubleToLongBits(pitch), y = Double.doubleToLongBits(yaw), r = Double.doubleToLongBits(roll);
		if(r != posZero && r != negZero) {
			double a = roll * toRadiansMultiplier;
			double cos = Math.cos(a);
			double sin = Math.sin(a);
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, -sin, 0.0, 0.0,
					sin, cos, 0.0, 0.0,
					0.0, 0.0, 1.0, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(p != posZero && p != negZero) {
			double a = pitch * toRadiansMultiplier;
			double cos = Math.cos(a);
			double sin = Math.sin(a);
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					1.0, 0.0, 0.0, 0.0,
					0.0, cos, -sin, 0.0,
					0.0, sin, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(y != posZero && y != negZero) {
			double a = yaw * toRadiansMultiplier;
			double cos = Math.cos(a);
			double sin = Math.sin(a);
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, 0.0, sin, 0.0,
					0.0, 1.0, 0.0, 0.0,
					-sin, 0.0, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp float[] rotateMatrix4x4f(float[] matrix, float yaw, float pitch, float roll) {
		return rotateZXYMatrix4x4f(matrix, yaw, pitch, roll);
	}
	
	public static final strictfp double[] rotateMatrix4x4d(double[] matrix, double yaw, double pitch, double roll) {
		return rotateZXYMatrix4x4d(matrix, yaw, pitch, roll);
	}
	
	public static final strictfp float[] rotateYXZMatrix4x4f(float[] matrix, float angle, float xAxis, float yAxis, float zAxis) {
		int x = Float.floatToIntBits(xAxis), y = Float.floatToIntBits(yAxis), z = Float.floatToIntBits(zAxis);
		float a = (float) (angle * toRadiansMultiplier);
		float c = (float) Math.cos(a);
		float s = (float) Math.sin(a);
		if(y != posZerof && y != negZerof) {
			float cos = c * yAxis;
			float sin = s * yAxis;
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, 0.0f, sin, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					-sin, 0.0f, cos, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(x != posZerof && x != negZerof) {
			float cos = c * xAxis;
			float sin = s * xAxis;
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, cos, -sin, 0.0f,
					0.0f, sin, cos, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(z != posZerof && z != negZerof) {
			float cos = c * zAxis;
			float sin = s * zAxis;
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, -sin, 0.0f, 0.0f,
					sin, cos, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp double[] rotateYXZMatrix4x4d(double[] matrix, double angle, double xAxis, double yAxis, double zAxis) {
		long x = Double.doubleToLongBits(xAxis), y = Double.doubleToLongBits(yAxis), z = Double.doubleToLongBits(zAxis);
		double a = angle * toRadiansMultiplier;
		double c = Math.cos(a);
		double s = Math.sin(a);
		if(y != posZero && y != negZero) {
			double cos = c * yAxis;
			double sin = s * yAxis;
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, 0.0, sin, 0.0,
					0.0, 1.0, 0.0, 0.0,
					-sin, 0.0, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(x != posZero && x != negZero) {
			double cos = c * xAxis;
			double sin = s * xAxis;
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					1.0, 0.0, 0.0, 0.0,
					0.0, cos, -sin, 0.0,
					0.0, sin, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(z != posZero && z != negZero) {
			double cos = c * zAxis;
			double sin = s * zAxis;
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, -sin, 0.0, 0.0,
					sin, cos, 0.0, 0.0,
					0.0, 0.0, 1.0, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp float[] rotateYXZMatrix4x4f(float[] matrix, float yaw, float pitch, float roll) {
		int p = Float.floatToIntBits(pitch), y = Float.floatToIntBits(yaw), r = Float.floatToIntBits(roll);
		if(y != posZerof && y != negZerof) {
			float a = (float) (yaw * toRadiansMultiplier);
			float cos = (float) Math.cos(a);
			float sin = (float) Math.sin(a);
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, 0.0f, sin, 0.0f,
					0.0f, 1.0f, 0.0f, 0.0f,
					-sin, 0.0f, cos, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(p != posZerof && p != negZerof) {
			float a = (float) (pitch * toRadiansMultiplier);
			float cos = (float) Math.cos(a);
			float sin = (float) Math.sin(a);
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					1.0f, 0.0f, 0.0f, 0.0f,
					0.0f, cos, -sin, 0.0f,
					0.0f, sin, cos, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		if(r != posZerof && r != negZerof) {
			float a = (float) (roll * toRadiansMultiplier);
			float cos = (float) Math.cos(a);
			float sin = (float) Math.sin(a);
			matrix = multMatrix4x4f(new float[] {//@formatter:off
					cos, -sin, 0.0f, 0.0f,
					sin, cos, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, 0.0f,
					0.0f, 0.0f, 0.0f, 1.0f
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp double[] rotateYXZMatrix4x4d(double[] matrix, double yaw, double pitch, double roll) {
		long p = Double.doubleToLongBits(pitch), y = Double.doubleToLongBits(yaw), r = Double.doubleToLongBits(roll);
		if(y != posZero && y != negZero) {
			double a = yaw * toRadiansMultiplier;
			double cos = Math.cos(a);
			double sin = Math.sin(a);
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, 0.0, sin, 0.0,
					0.0, 1.0, 0.0, 0.0,
					-sin, 0.0, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(p != posZero && p != negZero) {
			double a = pitch * toRadiansMultiplier;
			double cos = Math.cos(a);
			double sin = Math.sin(a);
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					1.0, 0.0, 0.0, 0.0,
					0.0, cos, -sin, 0.0,
					0.0, sin, cos, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		if(r != posZero && r != negZero) {
			double a = roll * toRadiansMultiplier;
			double cos = Math.cos(a);
			double sin = Math.sin(a);
			matrix = multMatrix4x4d(new double[] {//@formatter:off
					cos, -sin, 0.0, 0.0,
					sin, cos, 0.0, 0.0,
					0.0, 0.0, 1.0, 0.0,
					0.0, 0.0, 0.0, 1.0
			}, matrix);//@formatter:on
		}
		return matrix;
	}
	
	public static final strictfp float[] lookAtMatrix4x4f(float[] eye, float[] target, float[] y) {
		float[] fwd = normalize(subtract(target, eye));
		float[] side = normalize(cross(fwd, y));
		float[] up = normalize(cross(side, fwd));
		
		eye = multiply(eye, -1.0f);
		fwd = multiply(fwd, -1.0f);
		
		return new float[] {//@formatter:off
				side[0],			up[0],			fwd[0],			0.0f,
				side[1],			up[1],			fwd[1],			0.0f,
				side[2],			up[2],			fwd[2],			0.0f,
				dot(side, eye),		dot(up, eye),	dot(fwd, eye),	1.0f
		};//@formatter:on
	}
	
	public static final strictfp double[] lookAtMatrix4x4d(double[] eye, double[] target, double[] y) {
		double[] fwd = normalize(subtract(target, eye));
		double[] side = normalize(cross(fwd, y));
		double[] up = normalize(cross(side, fwd));
		
		fwd = multiply(fwd, -1.0);
		eye = multiply(eye, -1.0);
		
		return new double[] {//@formatter:off
				side[0],			up[0],			fwd[0],			0.0,
				side[1],			up[1],			fwd[1],			0.0,
				side[2],			up[2],			fwd[2],			0.0,
				dot(side, eye),		dot(up, eye),	dot(fwd, eye),	1.0
		};//@formatter:on
	}
	
	public static final strictfp float[] adjugateMatrix4x4f(float[] matrix) {
		final float[] adjugate = new float[16];
		adjugate[0] = matrix[5] * matrix[10] * matrix[15] - matrix[5] * matrix[11] * matrix[14] - matrix[9] * matrix[6] * matrix[15] + matrix[9] * matrix[7] * matrix[14] + matrix[13] * matrix[6] * matrix[11] - matrix[13] * matrix[7] * matrix[10];
		adjugate[1] = -matrix[1] * matrix[10] * matrix[15] + matrix[1] * matrix[11] * matrix[14] + matrix[9] * matrix[2] * matrix[15] - matrix[9] * matrix[3] * matrix[14] - matrix[13] * matrix[2] * matrix[11] + matrix[13] * matrix[3] * matrix[10];
		adjugate[2] = matrix[1] * matrix[6] * matrix[15] - matrix[1] * matrix[7] * matrix[14] - matrix[5] * matrix[2] * matrix[15] + matrix[5] * matrix[3] * matrix[14] + matrix[13] * matrix[2] * matrix[7] - matrix[13] * matrix[3] * matrix[6];
		adjugate[3] = -matrix[1] * matrix[6] * matrix[11] + matrix[1] * matrix[7] * matrix[10] + matrix[5] * matrix[2] * matrix[11] - matrix[5] * matrix[3] * matrix[10] - matrix[9] * matrix[2] * matrix[7] + matrix[9] * matrix[3] * matrix[6];
		adjugate[4] = -matrix[4] * matrix[10] * matrix[15] + matrix[4] * matrix[11] * matrix[14] + matrix[8] * matrix[6] * matrix[15] - matrix[8] * matrix[7] * matrix[14] - matrix[12] * matrix[6] * matrix[11] + matrix[12] * matrix[7] * matrix[10];
		adjugate[5] = matrix[0] * matrix[10] * matrix[15] - matrix[0] * matrix[11] * matrix[14] - matrix[8] * matrix[2] * matrix[15] + matrix[8] * matrix[3] * matrix[14] + matrix[12] * matrix[2] * matrix[11] - matrix[12] * matrix[3] * matrix[10];
		adjugate[6] = -matrix[0] * matrix[6] * matrix[15] + matrix[0] * matrix[7] * matrix[14] + matrix[4] * matrix[2] * matrix[15] - matrix[4] * matrix[3] * matrix[14] - matrix[12] * matrix[2] * matrix[7] + matrix[12] * matrix[3] * matrix[6];
		adjugate[7] = matrix[0] * matrix[6] * matrix[11] - matrix[0] * matrix[7] * matrix[10] - matrix[4] * matrix[2] * matrix[11] + matrix[4] * matrix[3] * matrix[10] + matrix[8] * matrix[2] * matrix[7] - matrix[8] * matrix[3] * matrix[6];
		adjugate[8] = matrix[4] * matrix[9] * matrix[15] - matrix[4] * matrix[11] * matrix[13] - matrix[8] * matrix[5] * matrix[15] + matrix[8] * matrix[7] * matrix[13] + matrix[12] * matrix[5] * matrix[11] - matrix[12] * matrix[7] * matrix[9];
		adjugate[9] = -matrix[0] * matrix[9] * matrix[15] + matrix[0] * matrix[11] * matrix[13] + matrix[8] * matrix[1] * matrix[15] - matrix[8] * matrix[3] * matrix[13] - matrix[12] * matrix[1] * matrix[11] + matrix[12] * matrix[3] * matrix[9];
		adjugate[10] = matrix[0] * matrix[5] * matrix[15] - matrix[0] * matrix[7] * matrix[13] - matrix[4] * matrix[1] * matrix[15] + matrix[4] * matrix[3] * matrix[13] + matrix[12] * matrix[1] * matrix[7] - matrix[12] * matrix[3] * matrix[5];
		adjugate[11] = -matrix[0] * matrix[5] * matrix[11] + matrix[0] * matrix[7] * matrix[9] + matrix[4] * matrix[1] * matrix[11] - matrix[4] * matrix[3] * matrix[9] - matrix[8] * matrix[1] * matrix[7] + matrix[8] * matrix[3] * matrix[5];
		adjugate[12] = -matrix[4] * matrix[9] * matrix[14] + matrix[4] * matrix[10] * matrix[13] + matrix[8] * matrix[5] * matrix[14] - matrix[8] * matrix[6] * matrix[13] - matrix[12] * matrix[5] * matrix[10] + matrix[12] * matrix[6] * matrix[9];
		adjugate[13] = matrix[0] * matrix[9] * matrix[14] - matrix[0] * matrix[10] * matrix[13] - matrix[8] * matrix[1] * matrix[14] + matrix[8] * matrix[2] * matrix[13] + matrix[12] * matrix[1] * matrix[10] - matrix[12] * matrix[2] * matrix[9];
		adjugate[14] = -matrix[0] * matrix[5] * matrix[14] + matrix[0] * matrix[6] * matrix[13] + matrix[4] * matrix[1] * matrix[14] - matrix[4] * matrix[2] * matrix[13] - matrix[12] * matrix[1] * matrix[6] + matrix[12] * matrix[2] * matrix[5];
		adjugate[15] = matrix[0] * matrix[5] * matrix[10] - matrix[0] * matrix[6] * matrix[9] - matrix[4] * matrix[1] * matrix[10] + matrix[4] * matrix[2] * matrix[9] + matrix[8] * matrix[1] * matrix[6] - matrix[8] * matrix[2] * matrix[5];
		return adjugate;
	}
	
	public static final strictfp double[] adjugateMatrix4x4d(double[] matrix) {
		final double[] adjugate = new double[16];
		adjugate[0] = matrix[5] * matrix[10] * matrix[15] - matrix[5] * matrix[11] * matrix[14] - matrix[9] * matrix[6] * matrix[15] + matrix[9] * matrix[7] * matrix[14] + matrix[13] * matrix[6] * matrix[11] - matrix[13] * matrix[7] * matrix[10];
		adjugate[1] = -matrix[1] * matrix[10] * matrix[15] + matrix[1] * matrix[11] * matrix[14] + matrix[9] * matrix[2] * matrix[15] - matrix[9] * matrix[3] * matrix[14] - matrix[13] * matrix[2] * matrix[11] + matrix[13] * matrix[3] * matrix[10];
		adjugate[2] = matrix[1] * matrix[6] * matrix[15] - matrix[1] * matrix[7] * matrix[14] - matrix[5] * matrix[2] * matrix[15] + matrix[5] * matrix[3] * matrix[14] + matrix[13] * matrix[2] * matrix[7] - matrix[13] * matrix[3] * matrix[6];
		adjugate[3] = -matrix[1] * matrix[6] * matrix[11] + matrix[1] * matrix[7] * matrix[10] + matrix[5] * matrix[2] * matrix[11] - matrix[5] * matrix[3] * matrix[10] - matrix[9] * matrix[2] * matrix[7] + matrix[9] * matrix[3] * matrix[6];
		adjugate[4] = -matrix[4] * matrix[10] * matrix[15] + matrix[4] * matrix[11] * matrix[14] + matrix[8] * matrix[6] * matrix[15] - matrix[8] * matrix[7] * matrix[14] - matrix[12] * matrix[6] * matrix[11] + matrix[12] * matrix[7] * matrix[10];
		adjugate[5] = matrix[0] * matrix[10] * matrix[15] - matrix[0] * matrix[11] * matrix[14] - matrix[8] * matrix[2] * matrix[15] + matrix[8] * matrix[3] * matrix[14] + matrix[12] * matrix[2] * matrix[11] - matrix[12] * matrix[3] * matrix[10];
		adjugate[6] = -matrix[0] * matrix[6] * matrix[15] + matrix[0] * matrix[7] * matrix[14] + matrix[4] * matrix[2] * matrix[15] - matrix[4] * matrix[3] * matrix[14] - matrix[12] * matrix[2] * matrix[7] + matrix[12] * matrix[3] * matrix[6];
		adjugate[7] = matrix[0] * matrix[6] * matrix[11] - matrix[0] * matrix[7] * matrix[10] - matrix[4] * matrix[2] * matrix[11] + matrix[4] * matrix[3] * matrix[10] + matrix[8] * matrix[2] * matrix[7] - matrix[8] * matrix[3] * matrix[6];
		adjugate[8] = matrix[4] * matrix[9] * matrix[15] - matrix[4] * matrix[11] * matrix[13] - matrix[8] * matrix[5] * matrix[15] + matrix[8] * matrix[7] * matrix[13] + matrix[12] * matrix[5] * matrix[11] - matrix[12] * matrix[7] * matrix[9];
		adjugate[9] = -matrix[0] * matrix[9] * matrix[15] + matrix[0] * matrix[11] * matrix[13] + matrix[8] * matrix[1] * matrix[15] - matrix[8] * matrix[3] * matrix[13] - matrix[12] * matrix[1] * matrix[11] + matrix[12] * matrix[3] * matrix[9];
		adjugate[10] = matrix[0] * matrix[5] * matrix[15] - matrix[0] * matrix[7] * matrix[13] - matrix[4] * matrix[1] * matrix[15] + matrix[4] * matrix[3] * matrix[13] + matrix[12] * matrix[1] * matrix[7] - matrix[12] * matrix[3] * matrix[5];
		adjugate[11] = -matrix[0] * matrix[5] * matrix[11] + matrix[0] * matrix[7] * matrix[9] + matrix[4] * matrix[1] * matrix[11] - matrix[4] * matrix[3] * matrix[9] - matrix[8] * matrix[1] * matrix[7] + matrix[8] * matrix[3] * matrix[5];
		adjugate[12] = -matrix[4] * matrix[9] * matrix[14] + matrix[4] * matrix[10] * matrix[13] + matrix[8] * matrix[5] * matrix[14] - matrix[8] * matrix[6] * matrix[13] - matrix[12] * matrix[5] * matrix[10] + matrix[12] * matrix[6] * matrix[9];
		adjugate[13] = matrix[0] * matrix[9] * matrix[14] - matrix[0] * matrix[10] * matrix[13] - matrix[8] * matrix[1] * matrix[14] + matrix[8] * matrix[2] * matrix[13] + matrix[12] * matrix[1] * matrix[10] - matrix[12] * matrix[2] * matrix[9];
		adjugate[14] = -matrix[0] * matrix[5] * matrix[14] + matrix[0] * matrix[6] * matrix[13] + matrix[4] * matrix[1] * matrix[14] - matrix[4] * matrix[2] * matrix[13] - matrix[12] * matrix[1] * matrix[6] + matrix[12] * matrix[2] * matrix[5];
		adjugate[15] = matrix[0] * matrix[5] * matrix[10] - matrix[0] * matrix[6] * matrix[9] - matrix[4] * matrix[1] * matrix[10] + matrix[4] * matrix[2] * matrix[9] + matrix[8] * matrix[1] * matrix[6] - matrix[8] * matrix[2] * matrix[5];
		return adjugate;
	}
	
	public static final strictfp float getDeterminantOfMatrix4x4f(float[] matrix, float[] adjugate) {
		return matrix[0] * adjugate[0] + matrix[1] * adjugate[4] + matrix[2] * adjugate[8] + matrix[3] * adjugate[12];
	}
	
	public static final strictfp float getDeterminantOfMatrix4x4f(float[] matrix) {
		return getDeterminantOfMatrix4x4f(matrix, adjugateMatrix4x4f(matrix));
	}
	
	public static final strictfp double getDeterminantOfMatrix4x4d(double[] matrix, double[] adjugate) {
		return matrix[0] * adjugate[0] + matrix[1] * adjugate[4] + matrix[2] * adjugate[8] + matrix[3] * adjugate[12];
	}
	
	public static final strictfp double getDeterminantOfMatrix4x4d(double[] matrix) {
		return getDeterminantOfMatrix4x4d(matrix, adjugateMatrix4x4d(matrix));
	}
	
	public static final strictfp float[] invertMatrix4x4f(float[] matrix) {
		final float[] adjugate = new float[16];
		adjugate[0] = matrix[5] * matrix[10] * matrix[15] - matrix[5] * matrix[11] * matrix[14] - matrix[9] * matrix[6] * matrix[15] + matrix[9] * matrix[7] * matrix[14] + matrix[13] * matrix[6] * matrix[11] - matrix[13] * matrix[7] * matrix[10];
		adjugate[1] = -matrix[1] * matrix[10] * matrix[15] + matrix[1] * matrix[11] * matrix[14] + matrix[9] * matrix[2] * matrix[15] - matrix[9] * matrix[3] * matrix[14] - matrix[13] * matrix[2] * matrix[11] + matrix[13] * matrix[3] * matrix[10];
		adjugate[2] = matrix[1] * matrix[6] * matrix[15] - matrix[1] * matrix[7] * matrix[14] - matrix[5] * matrix[2] * matrix[15] + matrix[5] * matrix[3] * matrix[14] + matrix[13] * matrix[2] * matrix[7] - matrix[13] * matrix[3] * matrix[6];
		adjugate[3] = -matrix[1] * matrix[6] * matrix[11] + matrix[1] * matrix[7] * matrix[10] + matrix[5] * matrix[2] * matrix[11] - matrix[5] * matrix[3] * matrix[10] - matrix[9] * matrix[2] * matrix[7] + matrix[9] * matrix[3] * matrix[6];
		adjugate[4] = -matrix[4] * matrix[10] * matrix[15] + matrix[4] * matrix[11] * matrix[14] + matrix[8] * matrix[6] * matrix[15] - matrix[8] * matrix[7] * matrix[14] - matrix[12] * matrix[6] * matrix[11] + matrix[12] * matrix[7] * matrix[10];
		adjugate[5] = matrix[0] * matrix[10] * matrix[15] - matrix[0] * matrix[11] * matrix[14] - matrix[8] * matrix[2] * matrix[15] + matrix[8] * matrix[3] * matrix[14] + matrix[12] * matrix[2] * matrix[11] - matrix[12] * matrix[3] * matrix[10];
		adjugate[6] = -matrix[0] * matrix[6] * matrix[15] + matrix[0] * matrix[7] * matrix[14] + matrix[4] * matrix[2] * matrix[15] - matrix[4] * matrix[3] * matrix[14] - matrix[12] * matrix[2] * matrix[7] + matrix[12] * matrix[3] * matrix[6];
		adjugate[7] = matrix[0] * matrix[6] * matrix[11] - matrix[0] * matrix[7] * matrix[10] - matrix[4] * matrix[2] * matrix[11] + matrix[4] * matrix[3] * matrix[10] + matrix[8] * matrix[2] * matrix[7] - matrix[8] * matrix[3] * matrix[6];
		adjugate[8] = matrix[4] * matrix[9] * matrix[15] - matrix[4] * matrix[11] * matrix[13] - matrix[8] * matrix[5] * matrix[15] + matrix[8] * matrix[7] * matrix[13] + matrix[12] * matrix[5] * matrix[11] - matrix[12] * matrix[7] * matrix[9];
		adjugate[9] = -matrix[0] * matrix[9] * matrix[15] + matrix[0] * matrix[11] * matrix[13] + matrix[8] * matrix[1] * matrix[15] - matrix[8] * matrix[3] * matrix[13] - matrix[12] * matrix[1] * matrix[11] + matrix[12] * matrix[3] * matrix[9];
		adjugate[10] = matrix[0] * matrix[5] * matrix[15] - matrix[0] * matrix[7] * matrix[13] - matrix[4] * matrix[1] * matrix[15] + matrix[4] * matrix[3] * matrix[13] + matrix[12] * matrix[1] * matrix[7] - matrix[12] * matrix[3] * matrix[5];
		adjugate[11] = -matrix[0] * matrix[5] * matrix[11] + matrix[0] * matrix[7] * matrix[9] + matrix[4] * matrix[1] * matrix[11] - matrix[4] * matrix[3] * matrix[9] - matrix[8] * matrix[1] * matrix[7] + matrix[8] * matrix[3] * matrix[5];
		adjugate[12] = -matrix[4] * matrix[9] * matrix[14] + matrix[4] * matrix[10] * matrix[13] + matrix[8] * matrix[5] * matrix[14] - matrix[8] * matrix[6] * matrix[13] - matrix[12] * matrix[5] * matrix[10] + matrix[12] * matrix[6] * matrix[9];
		adjugate[13] = matrix[0] * matrix[9] * matrix[14] - matrix[0] * matrix[10] * matrix[13] - matrix[8] * matrix[1] * matrix[14] + matrix[8] * matrix[2] * matrix[13] + matrix[12] * matrix[1] * matrix[10] - matrix[12] * matrix[2] * matrix[9];
		adjugate[14] = -matrix[0] * matrix[5] * matrix[14] + matrix[0] * matrix[6] * matrix[13] + matrix[4] * matrix[1] * matrix[14] - matrix[4] * matrix[2] * matrix[13] - matrix[12] * matrix[1] * matrix[6] + matrix[12] * matrix[2] * matrix[5];
		adjugate[15] = matrix[0] * matrix[5] * matrix[10] - matrix[0] * matrix[6] * matrix[9] - matrix[4] * matrix[1] * matrix[10] + matrix[4] * matrix[2] * matrix[9] + matrix[8] * matrix[1] * matrix[6] - matrix[8] * matrix[2] * matrix[5];
		float determinant = getDeterminantOfMatrix4x4f(matrix, adjugate);
		if(determinant == 0) {
			return null;
		}
		determinant = 1.0f / determinant;
		for(int i = 0; i < 16; i++) {
			adjugate[i] *= determinant;
		}
		//adjugate is now the 'inverse' of matrix
		return adjugate;
	}
	
	public static final strictfp double[] invertMatrix4x4d(double[] matrix) {
		final double[] adjugate = new double[16];
		adjugate[0] = matrix[5] * matrix[10] * matrix[15] - matrix[5] * matrix[11] * matrix[14] - matrix[9] * matrix[6] * matrix[15] + matrix[9] * matrix[7] * matrix[14] + matrix[13] * matrix[6] * matrix[11] - matrix[13] * matrix[7] * matrix[10];
		adjugate[1] = -matrix[1] * matrix[10] * matrix[15] + matrix[1] * matrix[11] * matrix[14] + matrix[9] * matrix[2] * matrix[15] - matrix[9] * matrix[3] * matrix[14] - matrix[13] * matrix[2] * matrix[11] + matrix[13] * matrix[3] * matrix[10];
		adjugate[2] = matrix[1] * matrix[6] * matrix[15] - matrix[1] * matrix[7] * matrix[14] - matrix[5] * matrix[2] * matrix[15] + matrix[5] * matrix[3] * matrix[14] + matrix[13] * matrix[2] * matrix[7] - matrix[13] * matrix[3] * matrix[6];
		adjugate[3] = -matrix[1] * matrix[6] * matrix[11] + matrix[1] * matrix[7] * matrix[10] + matrix[5] * matrix[2] * matrix[11] - matrix[5] * matrix[3] * matrix[10] - matrix[9] * matrix[2] * matrix[7] + matrix[9] * matrix[3] * matrix[6];
		adjugate[4] = -matrix[4] * matrix[10] * matrix[15] + matrix[4] * matrix[11] * matrix[14] + matrix[8] * matrix[6] * matrix[15] - matrix[8] * matrix[7] * matrix[14] - matrix[12] * matrix[6] * matrix[11] + matrix[12] * matrix[7] * matrix[10];
		adjugate[5] = matrix[0] * matrix[10] * matrix[15] - matrix[0] * matrix[11] * matrix[14] - matrix[8] * matrix[2] * matrix[15] + matrix[8] * matrix[3] * matrix[14] + matrix[12] * matrix[2] * matrix[11] - matrix[12] * matrix[3] * matrix[10];
		adjugate[6] = -matrix[0] * matrix[6] * matrix[15] + matrix[0] * matrix[7] * matrix[14] + matrix[4] * matrix[2] * matrix[15] - matrix[4] * matrix[3] * matrix[14] - matrix[12] * matrix[2] * matrix[7] + matrix[12] * matrix[3] * matrix[6];
		adjugate[7] = matrix[0] * matrix[6] * matrix[11] - matrix[0] * matrix[7] * matrix[10] - matrix[4] * matrix[2] * matrix[11] + matrix[4] * matrix[3] * matrix[10] + matrix[8] * matrix[2] * matrix[7] - matrix[8] * matrix[3] * matrix[6];
		adjugate[8] = matrix[4] * matrix[9] * matrix[15] - matrix[4] * matrix[11] * matrix[13] - matrix[8] * matrix[5] * matrix[15] + matrix[8] * matrix[7] * matrix[13] + matrix[12] * matrix[5] * matrix[11] - matrix[12] * matrix[7] * matrix[9];
		adjugate[9] = -matrix[0] * matrix[9] * matrix[15] + matrix[0] * matrix[11] * matrix[13] + matrix[8] * matrix[1] * matrix[15] - matrix[8] * matrix[3] * matrix[13] - matrix[12] * matrix[1] * matrix[11] + matrix[12] * matrix[3] * matrix[9];
		adjugate[10] = matrix[0] * matrix[5] * matrix[15] - matrix[0] * matrix[7] * matrix[13] - matrix[4] * matrix[1] * matrix[15] + matrix[4] * matrix[3] * matrix[13] + matrix[12] * matrix[1] * matrix[7] - matrix[12] * matrix[3] * matrix[5];
		adjugate[11] = -matrix[0] * matrix[5] * matrix[11] + matrix[0] * matrix[7] * matrix[9] + matrix[4] * matrix[1] * matrix[11] - matrix[4] * matrix[3] * matrix[9] - matrix[8] * matrix[1] * matrix[7] + matrix[8] * matrix[3] * matrix[5];
		adjugate[12] = -matrix[4] * matrix[9] * matrix[14] + matrix[4] * matrix[10] * matrix[13] + matrix[8] * matrix[5] * matrix[14] - matrix[8] * matrix[6] * matrix[13] - matrix[12] * matrix[5] * matrix[10] + matrix[12] * matrix[6] * matrix[9];
		adjugate[13] = matrix[0] * matrix[9] * matrix[14] - matrix[0] * matrix[10] * matrix[13] - matrix[8] * matrix[1] * matrix[14] + matrix[8] * matrix[2] * matrix[13] + matrix[12] * matrix[1] * matrix[10] - matrix[12] * matrix[2] * matrix[9];
		adjugate[14] = -matrix[0] * matrix[5] * matrix[14] + matrix[0] * matrix[6] * matrix[13] + matrix[4] * matrix[1] * matrix[14] - matrix[4] * matrix[2] * matrix[13] - matrix[12] * matrix[1] * matrix[6] + matrix[12] * matrix[2] * matrix[5];
		adjugate[15] = matrix[0] * matrix[5] * matrix[10] - matrix[0] * matrix[6] * matrix[9] - matrix[4] * matrix[1] * matrix[10] + matrix[4] * matrix[2] * matrix[9] + matrix[8] * matrix[1] * matrix[6] - matrix[8] * matrix[2] * matrix[5];
		double determinant = getDeterminantOfMatrix4x4d(matrix, adjugate);
		if(determinant == 0) {
			return null;
		}
		determinant = 1.0 / determinant;
		for(int i = 0; i < 16; i++) {
			adjugate[i] *= determinant;
		}
		//adjugate is now the 'inverse' of matrix
		return adjugate;
	}
	
	public static final String matrix4x4ToStringf(float[] matrix, int numOfPlaces, boolean pad) {
		if(!pad) {
			StringBuilder sb = new StringBuilder(StringUtil.lineOf('=', ((numOfPlaces + 5) * 4) - 1)).append("\r\n");
			
			for(int i = 0; i < 4; i++) {
				float d0 = matrix[0 + (4 * i)];
				float d1 = matrix[1 + (4 * i)];
				float d2 = matrix[2 + (4 * i)];
				float d3 = matrix[3 + (4 * i)];
				sb.append(d0 < 0.0 || Float.floatToIntBits(d0) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(d0, numOfPlaces, pad)).append(", ");
				sb.append(d1 < 0.0 || Float.floatToIntBits(d1) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(d1, numOfPlaces, pad)).append(", ");
				sb.append(d2 < 0.0 || Float.floatToIntBits(d2) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(d2, numOfPlaces, pad)).append(", ");
				sb.append(d3 < 0.0 || Float.floatToIntBits(d3) == negZerof ? "" : " ").append(MathUtil.limitDecimalNoRounding(d3, numOfPlaces, pad)).append(i == 3 ? ";" : ",\r\n");
			}
			
			return sb.toString();
		}
		
		int largestWholePortionOfDecimal = 0;
		for(int i = 0; i < 4; i++) {
			float d0 = matrix[0 + (4 * i)];
			float d1 = matrix[1 + (4 * i)];
			float d2 = matrix[2 + (4 * i)];
			float d3 = matrix[3 + (4 * i)];
			String s0 = MathUtil.limitDecimalNoRounding(d0, numOfPlaces, pad);
			String s1 = MathUtil.limitDecimalNoRounding(d1, numOfPlaces, pad);
			String s2 = MathUtil.limitDecimalNoRounding(d2, numOfPlaces, pad);
			String s3 = MathUtil.limitDecimalNoRounding(d3, numOfPlaces, pad);
			int i0 = s0.contains(".") ? s0.substring(0, s0.indexOf(".")).length() - (s0.startsWith("-") ? 1 : 0) : 1;
			int i1 = s1.contains(".") ? s1.substring(0, s1.indexOf(".")).length() - (s1.startsWith("-") ? 1 : 0) : 1;
			int i2 = s2.contains(".") ? s2.substring(0, s2.indexOf(".")).length() - (s2.startsWith("-") ? 1 : 0) : 1;
			int i3 = s3.contains(".") ? s3.substring(0, s3.indexOf(".")).length() - (s3.startsWith("-") ? 1 : 0) : 1;
			largestWholePortionOfDecimal = Math.max(i0, Math.max(i1, Math.max(i2, Math.max(i3, largestWholePortionOfDecimal))));
		}
		StringBuilder sb = new StringBuilder(StringUtil.lineOf('=', ((numOfPlaces + 5) * 4) + (largestWholePortionOfDecimal > 1 ? largestWholePortionOfDecimal + 1 : -1))).append("\r\n");
		largestWholePortionOfDecimal += 1;
		for(int i = 0; i < 4; i++) {
			float d0 = matrix[0 + (4 * i)];
			float d1 = matrix[1 + (4 * i)];
			float d2 = matrix[2 + (4 * i)];
			float d3 = matrix[3 + (4 * i)];
			String s0 = MathUtil.limitDecimalNoRounding(d0, numOfPlaces, pad);
			String s1 = MathUtil.limitDecimalNoRounding(d1, numOfPlaces, pad);
			String s2 = MathUtil.limitDecimalNoRounding(d2, numOfPlaces, pad);
			String s3 = MathUtil.limitDecimalNoRounding(d3, numOfPlaces, pad);
			int i0 = s0.contains(".") ? s0.substring(0, s0.indexOf(".")).length() : 1;
			int i1 = s1.contains(".") ? s1.substring(0, s1.indexOf(".")).length() : 1;
			int i2 = s2.contains(".") ? s2.substring(0, s2.indexOf(".")).length() : 1;
			int i3 = s3.contains(".") ? s3.substring(0, s3.indexOf(".")).length() : 1;
			
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i0))).append(s0).append(", ");
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i1))).append(s1).append(", ");
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i2))).append(s2).append(", ");
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i3))).append(s3).append(i == 3 ? ";" : ",\r\n");
		}
		
		return sb.toString();
	}
	
	public static final String matrix4x4ToStringd(double[] matrix, int numOfPlaces, boolean pad) {
		if(!pad) {
			StringBuilder sb = new StringBuilder(StringUtil.lineOf('=', ((numOfPlaces + 5) * 4) - 1)).append("\r\n");
			
			for(int i = 0; i < 4; i++) {
				double d0 = matrix[0 + (4 * i)];
				double d1 = matrix[1 + (4 * i)];
				double d2 = matrix[2 + (4 * i)];
				double d3 = matrix[3 + (4 * i)];
				sb.append(d0 < 0.0 || Double.doubleToLongBits(d0) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(d0, numOfPlaces, pad)).append(", ");
				sb.append(d1 < 0.0 || Double.doubleToLongBits(d1) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(d1, numOfPlaces, pad)).append(", ");
				sb.append(d2 < 0.0 || Double.doubleToLongBits(d2) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(d2, numOfPlaces, pad)).append(", ");
				sb.append(d3 < 0.0 || Double.doubleToLongBits(d3) == negZero ? "" : " ").append(MathUtil.limitDecimalNoRounding(d3, numOfPlaces, pad)).append(i == 3 ? ";" : ",\r\n");
			}
			
			return sb.toString();
		}
		
		int largestWholePortionOfDecimal = 0;
		for(int i = 0; i < 4; i++) {
			double d0 = matrix[0 + (4 * i)];
			double d1 = matrix[1 + (4 * i)];
			double d2 = matrix[2 + (4 * i)];
			double d3 = matrix[3 + (4 * i)];
			String s0 = MathUtil.limitDecimalNoRounding(d0, numOfPlaces, pad);
			String s1 = MathUtil.limitDecimalNoRounding(d1, numOfPlaces, pad);
			String s2 = MathUtil.limitDecimalNoRounding(d2, numOfPlaces, pad);
			String s3 = MathUtil.limitDecimalNoRounding(d3, numOfPlaces, pad);
			int i0 = s0.contains(".") ? s0.substring(0, s0.indexOf(".")).length() - (s0.startsWith("-") ? 1 : 0) : 1;
			int i1 = s1.contains(".") ? s1.substring(0, s1.indexOf(".")).length() - (s1.startsWith("-") ? 1 : 0) : 1;
			int i2 = s2.contains(".") ? s2.substring(0, s2.indexOf(".")).length() - (s2.startsWith("-") ? 1 : 0) : 1;
			int i3 = s3.contains(".") ? s3.substring(0, s3.indexOf(".")).length() - (s3.startsWith("-") ? 1 : 0) : 1;
			largestWholePortionOfDecimal = Math.max(i0, Math.max(i1, Math.max(i2, Math.max(i3, largestWholePortionOfDecimal))));
		}
		StringBuilder sb = new StringBuilder(StringUtil.lineOf('=', ((numOfPlaces + 5) * 4) + (largestWholePortionOfDecimal > 1 ? largestWholePortionOfDecimal + 1 : -1))).append("\r\n");
		largestWholePortionOfDecimal += 1;
		for(int i = 0; i < 4; i++) {
			double d0 = matrix[0 + (4 * i)];
			double d1 = matrix[1 + (4 * i)];
			double d2 = matrix[2 + (4 * i)];
			double d3 = matrix[3 + (4 * i)];
			String s0 = MathUtil.limitDecimalNoRounding(d0, numOfPlaces, pad);
			String s1 = MathUtil.limitDecimalNoRounding(d1, numOfPlaces, pad);
			String s2 = MathUtil.limitDecimalNoRounding(d2, numOfPlaces, pad);
			String s3 = MathUtil.limitDecimalNoRounding(d3, numOfPlaces, pad);
			int i0 = s0.contains(".") ? s0.substring(0, s0.indexOf(".")).length() : 1;
			int i1 = s1.contains(".") ? s1.substring(0, s1.indexOf(".")).length() : 1;
			int i2 = s2.contains(".") ? s2.substring(0, s2.indexOf(".")).length() : 1;
			int i3 = s3.contains(".") ? s3.substring(0, s3.indexOf(".")).length() : 1;
			
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i0))).append(s0).append(", ");
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i1))).append(s1).append(", ");
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i2))).append(s2).append(", ");
			sb.append(StringUtil.lineOf(' ', Math.max(0, largestWholePortionOfDecimal - i3))).append(s3).append(i == 3 ? ";" : ",\r\n");
		}
		
		return sb.toString();
	}
	
	public static final String matrix4x4ToStringf(float[] matrix) {
		return GLUtil.matrix4x4ToStringf(matrix, 8, true);
	}
	
	public static final String matrix4x4ToStringd(double[] matrix) {
		return GLUtil.matrix4x4ToStringd(matrix, 16, true);
	}
	
	//=====================================================================================================================
	
	private static final double[] glColor = new double[] {1, 1, 1, 1};
	private static volatile int colorStackIndex = 0;
	private static final double[][] colorStack = new double[20][4];
	
	public static final void glPushColor() {
		if(colorStackIndex + 1 >= colorStack.length) {
			throw new RuntimeException("Cannot push any more color onto the stack!");
		}
		glColord();
		colorStack[colorStackIndex++] = new double[] {glColor[0], glColor[1], glColor[2], glColor[3]};
	}
	
	public static final void glPopColor() {
		if(colorStackIndex - 1 < 0) {
			throw new RuntimeException("There are no more colors to pop from the stack!");
		}
		double[] color = colorStack[--colorStackIndex];
		glColor[0] = color[0];
		glColor[1] = color[1];
		glColor[2] = color[2];
		glColor[3] = color[3];
		glColord();
	}
	
	public static final double[] getGLColor() {
		return new double[] {glColor[0], glColor[1], glColor[2], glColor[3]};
	}
	
	public static final void glColorf(float[] color) {
		glColor[0] = color[0];
		glColor[1] = color[1];
		glColor[2] = color[2];
		if(color.length >= 4) {
			glColor[3] = color[3];
		}
		glColord();
	}
	
	public static final void glColord(Color color) {
		glColord(color.getRed() + 0.0D / 255.0D, color.getGreen() + 0.0D / 255.0D, color.getBlue() + 0.0D / 255.0D, color.getAlpha() + 0.0D / 255.0D);
	}
	
	public static final void glColord(java.awt.Color color) {
		glColord(color.getRed() + 0.0D / 255.0D, color.getGreen() + 0.0D / 255.0D, color.getBlue() + 0.0D / 255.0D, color.getAlpha() + 0.0D / 255.0D);
	}
	
	public static final void glColord(double r, double g, double b) {
		glColord(r, g, b, glColor[3]);
	}
	
	public static final void glColord(double r, double g, double b, double a) {
		glColor[0] = r;
		glColor[1] = g;
		glColor[2] = b;
		glColor[3] = a;
		glColord();
	}
	
	public static final void glColord(double[] color) {
		glColor[0] = color[0];
		glColor[1] = color[1];
		glColor[2] = color[2];
		if(color.length == 4) {
			glColor[3] = color[3];
		}
		glColord();
	}
	
	public static final void glColorRed(double r) {
		glColor[0] = r;
		glColord();
	}
	
	public static final void glColorGreen(double g) {
		glColor[1] = g;
		glColord();
	}
	
	public static final void glColorBlue(double b) {
		glColor[2] = b;
		glColord();
	}
	
	public static final void glColorAlpha(double a) {
		glColor[3] = a;
		glColord();
	}
	
	public static final void glColord() {
		GL11.glColor4d(glColor[0], glColor[1], glColor[2], glColor[3]);
	}
	
	private static volatile int cullStackIndex = 0;
	private static final int[][] cullStack = new int[20][3];
	private static volatile int cullEnabled = 1;
	private static volatile int frontFace = GL11.GL_CCW;
	private static volatile int cullFace = GL11.GL_BACK;
	
	private static final void glUpdateCullMode() {
		if(cullEnabled == 1) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		} else {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		GL11.glFrontFace(frontFace);
		GL11.glCullFace(cullFace);
	}
	
	public static final void glPushCullMode() {
		if(cullStackIndex + 1 >= cullStack.length) {
			throw new RuntimeException("Cannot push any more cull modes onto the stack!");
		}
		glUpdateCullMode();
		cullStack[cullStackIndex++] = new int[] {cullEnabled, frontFace, cullFace};
	}
	
	public static final void glPopCullMode() {
		if(cullStackIndex - 1 < 0) {
			throw new RuntimeException("There are no more cull modes to pop from the stack!");
		}
		int[] pop = cullStack[--cullStackIndex];
		cullEnabled = pop[0];
		frontFace = pop[1];
		cullFace = pop[2];
		cullStack[cullStackIndex + 1] = null;
		glUpdateCullMode();
	}
	
	public static final int getFrontFace() {
		return frontFace;
	}
	
	public static final int getCullFace() {
		return cullFace;
	}
	
	public static final void glCullNone() {
		cullEnabled = 0;
		frontFace = GL11.GL_CCW;
		cullFace = GL11.GL_BACK;
		glUpdateCullMode();
	}
	
	public static final void glCullBack() {
		cullEnabled = 1;
		frontFace = GL11.GL_CCW;
		cullFace = GL11.GL_BACK;
		glUpdateCullMode();
	}
	
	public static final void glCullFront() {
		cullEnabled = 1;
		frontFace = GL11.GL_CCW;
		cullFace = GL11.GL_FRONT;
		glUpdateCullMode();
	}
	
	public static final void glCullFrontAndBack() {
		cullEnabled = 0;
		frontFace = GL11.GL_CCW;
		cullFace = GL11.GL_FRONT_AND_BACK;
		glUpdateCullMode();
	}
	
	private static volatile int blendStackIndex = 0;
	private static final int[][] blendStack = new int[20][4];
	private static volatile int blendEnabled = 0;
	private static volatile int blendSrcFactor = GL11.GL_ONE;
	private static volatile int blendDestFactor = GL11.GL_ZERO;
	private static volatile int blendEquation = GL14.GL_FUNC_ADD;
	
	public static final void glSetBlendEnabled(boolean enabled) {
		blendEnabled = enabled ? 1 : 0;
		glUpdateBlendMode();
	}
	
	public static final void glResetBlend() {
		glResetBlend(false);
	}
	
	public static final void glResetBlend(boolean enabled) {
		glBlend(enabled, GL11.GL_ONE, GL11.GL_ZERO, GL14.GL_FUNC_ADD);
	}
	
	private static final void glUpdateBlendMode() {
		if(blendEnabled == 1) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(blendSrcFactor, blendDestFactor);
			if(GLUtil.isGL14Available(GL.getCapabilities()/*, false*/)) {
				GL14.glBlendEquation(blendEquation);
			}
		} else {
			GL11.glBlendFunc(blendSrcFactor, blendDestFactor);
			if(GLUtil.isGL14Available(GL.getCapabilities()/*, false*/)) {
				GL14.glBlendEquation(blendEquation);
			}
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
	
	public static final void glPushBlendMode() {
		if(blendStackIndex + 1 >= blendStack.length) {
			throw new RuntimeException("Cannot push any more blend modes onto the stack!");
		}
		glUpdateBlendMode();
		blendStack[blendStackIndex++] = new int[] {blendEnabled, blendSrcFactor, blendDestFactor, blendEquation};
	}
	
	public static final void glPopBlendMode() {
		if(blendStackIndex - 1 < 0) {
			throw new RuntimeException("There are no more blend modes to pop from the stack!");
		}
		int[] pop = blendStack[--blendStackIndex];
		blendEnabled = pop[0];
		blendSrcFactor = pop[1];
		blendDestFactor = pop[2];
		blendEquation = pop[3];
		blendStack[blendStackIndex + 1] = null;
		glUpdateBlendMode();
	}
	
	public static final void glBlend(boolean enable, int sFactor, int dFactor) {
		glBlend(enable, sFactor, dFactor, blendEquation);
	}
	
	public static final void glBlend(boolean enable, int sFactor, int dFactor, int mode) {
		blendEnabled = enable ? 1 : 0;
		blendSrcFactor = sFactor;
		blendDestFactor = dFactor;
		blendEquation = mode;
		glUpdateBlendMode();
	}
	
	public static final void glBlendInvertColorMode() {
		glBlend(true, GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO, GL14.GL_FUNC_ADD);
	}
	
	public static final void glBlendAlphaMode() {
		glBlend(true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL14.GL_FUNC_ADD);
	}
	
	//====================================================================================================================================================
	
	private static volatile int viewportStackIndex = 0;
	private static final int[][] viewportStack = new int[20][4];
	
	public static final void glPushViewport() {
		if(blendStackIndex + 1 >= blendStack.length) {
			throw new RuntimeException("Cannot push any more viewports onto the stack!");
		}
		viewportStack[viewportStackIndex++] = GLUtil.glGetViewport();
	}
	
	public static final void glPopViewport() {
		if(blendStackIndex - 1 < 0) {
			throw new RuntimeException("There are no more viewports to pop from the stack!");
		}
		int[] vp = viewportStack[--viewportStackIndex];
		viewportStack[viewportStackIndex + 1] = null;
		GL11.glViewport(vp[0], vp[1], vp[2], vp[3]);
	}
	
	//====================================================================================================================================================
	
	public static final void glPushStacks(boolean pushMatrix, boolean pushViewport) {
		if(pushMatrix) {
			GL11.glPushMatrix();
		}
		if(pushViewport) {
			glPushViewport();
		}
		glPushColor();
		glPushCullMode();
		glPushBlendMode();
	}
	
	public static final void glPushStacks(boolean pushMatrix) {
		glPushStacks(pushMatrix, false);
	}
	
	public static final void glPushStacks() {
		glPushStacks(false);
	}
	
	public static final void glPopStacks(boolean popMatrix, boolean popViewport) {
		if(popMatrix) {
			GL11.glPopMatrix();
		}
		if(popViewport) {
			glPopViewport();
		}
		glPopColor();
		glPopCullMode();
		glPopBlendMode();
	}
	
	public static final void glPopStacks(boolean popMatrix) {
		glPopStacks(popMatrix, false);
	}
	
	public static final void glPopStacks() {
		glPopStacks(false);
	}
	
	//====================================================================================================================================================
	
	public static final float[] glGetClearColorf() {
		float[] color = new float[4];
		GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, color);
		return color;
	}
	
	public static final double[] glGetClearColord() {
		double[] color = new double[4];
		GL11.glGetDoublev(GL11.GL_COLOR_CLEAR_VALUE, color);
		return color;
	}
	
	public static final float[] glGetColorf() {
		float[] color = new float[4];
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, color);
		return color;
	}
	
	public static final double[] glGetColord() {
		double[] color = new double[4];
		GL11.glGetDoublev(GL11.GL_CURRENT_COLOR, color);
		return color;
	}
	
	@Deprecated
	public static final int[] glGetViewport() {
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		return viewport;
	}
	
	public static final float[] glGetProjectionMatrixf() {
		float[] projection = new float[16];
		GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
		return projection;
	}
	
	public static final double[] glGetProjectionMatrixd() {
		double[] projection = new double[16];
		GL11.glGetDoublev(GL11.GL_PROJECTION_MATRIX, projection);
		return projection;
	}
	
	public static final float[] glGetModelViewMatrixf() {
		float[] modelView = new float[16];
		GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
		return modelView;
	}
	
	public static final double[] glGetModelViewMatrixd() {
		double[] modelView = new double[16];
		GL11.glGetDoublev(GL11.GL_MODELVIEW_MATRIX, modelView);
		return modelView;
	}
	
	public static final strictfp void glDrawRect2d(double x, double y, double width, double height) {
		//GL11.glTranslated(x, y, 0x0.0p0);
		
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
		GL11.glTexCoord2d(1, 1);
		GL11.glVertex2d(x + width, y + height);
		GL11.glTexCoord2d(0, 1);
		GL11.glVertex2d(x, y + height);
		GL11.glTexCoord2d(1, 0);
		GL11.glVertex2d(x + width, y);
		GL11.glTexCoord2d(0, 0);
		GL11.glVertex2d(x, y);
		GL11.glEnd();
	}
	
	public static final strictfp void glDrawRect2d(double[] bounds) {
		GLUtil.glDrawRect2d(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	
	public static final strictfp void glInvertRect2d(double x, double y, double width, double height) {
		boolean tex2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		if(tex2DEnabled) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
		GLUtil.glPushColor();
		GLUtil.glPushBlendMode();
		GLUtil.glBlendInvertColorMode();
		GLUtil.glColord(1, 1, 1, 1);
		GLUtil.glDrawRect2d(x, y, width, height);
		GLUtil.glPopBlendMode();
		GLUtil.glPopColor();
		if(tex2DEnabled) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public static final strictfp void glInvertRect2d(double[] bounds) {
		GLUtil.glInvertRect2d(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	
	public static final strictfp void glStDrawRect2d(double x, double y, double width, double height) {
		glPushStacks(true);
		glDrawRect2d(x, y, width, height);
		glPopStacks(true);
	}
	
	public static final strictfp void glStDrawRect2d(double[] bounds) {
		glPushStacks(true);
		glDrawRect2d(bounds);
		glPopStacks(true);
	}
	
	public static final strictfp void glDrawTriangle2dDistorted(double x1, double y1, double x2, double y2, double x3, double y3, double sOffset, double tOffset) {
		//==[Texture coordinates]===================================================
		
		//Let's find the center point of this triangle first:
		double cx = (x1 + x2 + x3) / 3.0;//30 + 300 + 570 = 900; 900 / 3 = 300
		double cy = (y1 + y2 + y3) / 3.0;//30 + 570 + 30 = 630; 630 / 3 = 210
		
		//Now we take the difference of each of the triangle's point from its center... :
		
		double rx1 = x1 - cx;
		double ry1 = y1 - cy;
		double rx2 = x2 - cx;
		double ry2 = y2 - cy;
		double rx3 = x3 - cx;
		double ry3 = y3 - cy;
		
		//... and use that to create normalized vectors that we can use as the texture coordinates!
		
		double nmag1 = Math.sqrt((rx1 * rx1) + (ry1 * ry1)), s1 = (rx1 / nmag1) + sOffset, t1 = (ry1 / nmag1) + tOffset;
		double nmag2 = Math.sqrt((rx2 * rx2) + (ry2 * ry2)), s2 = (rx2 / nmag2) + sOffset, t2 = (ry2 / nmag2) + tOffset;
		double nmag3 = Math.sqrt((rx3 * rx3) + (ry3 * ry3)), s3 = (rx3 / nmag3) + sOffset, t3 = (ry3 / nmag3) + tOffset;
		
		//==[Now to actually render the triangle :D]==========================================================================
		
		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glTexCoord2d(s3, t3);
		GL11.glVertex2d(x3, y3);
		GL11.glTexCoord2d(s2, t2);
		GL11.glVertex2d(x2, y2);
		GL11.glTexCoord2d(s1, t1);
		GL11.glVertex2d(x1, y1);
		GL11.glEnd();
	}
	
	public static final strictfp void glDrawTriangle2dDistorted(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawTriangle2dDistorted(x1, y1, x2, y2, x3, y3, 0, 0);
	}
	
	public static final strictfp void glStDrawTriangle2dDistorted(double x1, double y1, double x2, double y2, double x3, double y3, double sOffset, double tOffset) {
		glPushStacks(true);
		glDrawTriangle2dDistorted(x1, y1, x2, y2, x3, y3, sOffset, tOffset);
		glPopStacks(true);
	}
	
	public static final strictfp void glStDrawTriangle2dDistorted(double x1, double y1, double x2, double y2, double x3, double y3) {
		glStDrawTriangle2dDistorted(x1, y1, x2, y2, x3, y3, 0, 0);
	}
	
	public static final strictfp void glDrawTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3, double s1, double t1, double s2, double t2, double s3, double t3) {
		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glTexCoord2d(s3, t3);
		GL11.glVertex2d(x3, y3);
		GL11.glTexCoord2d(s2, t2);
		GL11.glVertex2d(x2, y2);
		GL11.glTexCoord2d(s1, t1);
		GL11.glVertex2d(x1, y1);
		GL11.glEnd();
	}
	
	public static final strictfp void glDrawEquilateralTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawTriangle2d(x1, y1, x2, y2, x3, y3, 0, 0, 0.5, 1.0, 1.0, 0);
	}
	
	public static final strictfp void glDrawTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawEquilateralTriangle2d(x1, y1, x2, y2, x3, y3);
	}
	
	public static final strictfp void glDrawBottomLeftRightAngledTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawTriangle2d(x1, y1, x2, y2, x3, y3, 0, 1.0, 1.0, 0, 0, 0);
	}
	
	public static final strictfp void glDrawBottomRightRightAngledTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawTriangle2d(x1, y1, x2, y2, x3, y3, 0, 0, 1.0, 1.0, 1.0, 0);
	}
	
	public static final strictfp void glDrawTopLeftRightAngledTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawTriangle2d(x1, y1, x2, y2, x3, y3, 0, 0, 0, 1.0, 1.0, 1.0);
	}
	
	public static final strictfp void glDrawTopRightRightAngledTriangle2d(double x1, double y1, double x2, double y2, double x3, double y3) {
		glDrawTriangle2d(x1, y1, x2, y2, x3, y3, 0, 1.0, 1.0, 1.0, 1.0, 0);
	}
	
	//================================================================================================================================================================================================
	
	private static final Map<Integer, Integer> preferredPixelFormats = new HashMap<>();
	
	private static final int getPPFFromMap(int target) {
		Integer key = Integer.valueOf(target);
		Integer value = preferredPixelFormats.get(key);
		if(value == null) {
			return -1;
		}
		return value.intValue();
	}
	
	public static final int glGetPreferredPixelFormat(int target) {
		if(!GL.getCapabilities().GL_ARB_internalformat_query || !GL.getCapabilities().GL_ARB_internalformat_query2) {
			if(Platform.get() == Platform.WINDOWS) {
				return GL12.GL_BGRA;
			}
			return GL11.GL_RGBA;
		}
		int value = getPPFFromMap(target);
		if(value == -1) {
			try {
				value = ARBInternalformatQuery.glGetInternalformati(target, GL11.GL_RGBA8, ARBInternalformatQuery2.GL_TEXTURE_IMAGE_FORMAT);
			} catch(IllegalStateException ignored) {
				if(Platform.get() == Platform.WINDOWS) {
					value = GL12.GL_BGRA;
				} else {
					value = GL11.GL_RGBA;
				}
			}
			preferredPixelFormats.put(Integer.valueOf(target), Integer.valueOf(value));
		}
		return value;
	}
	
	protected static volatile boolean fboEnabled = false;
	protected static volatile boolean fboSupported = false;
	
	protected static volatile boolean fboDrawing = false;
	
	protected static volatile int framebufferID;
	protected static volatile int colorTextureID;
	protected static volatile int depthRenderBufferID;
	
	public static final Texture fboTexture = TextureLoader.OPENGL;//new Texture(GL11.GL_TEXTURE_2D, 0, "OpenGL");
	
	public static final void toggleFBOEnabled() {
		fboEnabled = !fboEnabled;
	}
	
	public static final boolean areFrameBufferObjectsEnabled() {
		return fboEnabled && fboSupported;
	}
	
	public static final boolean areFrameBufferObjectsSupported() {
		return fboSupported;
	}
	
	public static final boolean isFrameBufferBeingDrawnTo() {
		return fboDrawing;
	}
	
	public static final void beginDrawingFrameBuffer() {
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);
		fboDrawing = true;
		if(fboTexture.getID() == 0) {
			TextureLoader.openGLTextureID = GL11.glGenTextures();
		}
	}
	
	public static final void endDrawingFrameBuffer() {
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
		Texture.unbindAllTextures();
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		fboDrawing = false;
	}
	
	public static final void drawFrameBufferTexture(final float alpha) {
		int width = Window.getWindow().getWidth();
		int height = Window.getWindow().getHeight();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboTexture.getID());                   // bind our FBO texture
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glColor4f(1, 1, 1, alpha);
		GL11.glBegin(GL11.GL_QUADS);//@formatter:off
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(0, 0);// top left
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(width, 0);// top right
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(width, height);// bottom right
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(0, height);// bottom left
		GL11.glEnd();//@formatter:on
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public static final void updateFrameBuffer() {
		int width = Window.getWindow().getWidth();
		int height = Window.getWindow().getHeight();
		
		// Initialize FrameBufferObjects ====
		
		// check if GL_EXT_framebuffer_object can be use on this system
		if(GL.getCapabilities().GL_EXT_framebuffer_object) {
			fboSupported = true;
			
			framebufferID = EXTFramebufferObject.glGenFramebuffersEXT();                                         // create a new framebuffer
			colorTextureID = GL11.glGenTextures();                                               // and a new texture used as a color buffer
			depthRenderBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();                                  // And finally a new depthbuffer
			
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);                        // switch to the new framebuffer
			
			// initialize color texture
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextureID);                                   // Bind the colorbuffer texture
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);               // make it linear filtered
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, glGetPreferredPixelFormat(GL11.GL_TEXTURE_2D)/*GL11.GL_RGBA*/, GL11.GL_UNSIGNED_BYTE/*GL11.GL_INT*/, (java.nio.ByteBuffer) null);  // Create the texture data
			EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, colorTextureID, 0); // attach it to the framebuffer
			
			// initialize depth renderbuffer
			EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRenderBufferID);                // bind the depth renderbuffer
			EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT32, width, height); // get the data space for it
			EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRenderBufferID); // bind it to the renderbuffer
			
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);                                    // Switch back to normal framebuffer rendering
		} else {
			fboSupported = false;
		}
	}
	
	//================================================================================================================================================================================================
	
	public static final strictfp void glLegacyRenderCrosshairsAt(double x, double y, double z, double width, double height, boolean invertBackground) {
		final int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		final int texTarget = isGL45Available() ? GL45.glGetTextureParameteri(boundTexture, GL45.GL_TEXTURE_TARGET) : 0;
		final boolean tex2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		final boolean texTargetEnabled = texTarget != 0 ? GL11.glIsEnabled(texTarget) : false;
		final boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
		final int srcFactor = GL11.glGetInteger(GL11.GL_BLEND_SRC);
		final int dstFactor = GL11.glGetInteger(GL11.GL_BLEND_DST);
		
		GLUtil.glPushBlendMode();
		try {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			if(invertBackground) {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO);
			}
			
			GL11.glPushMatrix();
			GL11.glTranslated(x - (width / 2.0), y - (height / 2.0), z);
			GL11.glColor3f(1, 1, 1);
			GL11.glBegin(GL11.GL_QUADS);//@formatter:off
				GL11.glVertex2d(0, 0);// top left
				GL11.glVertex2d(width, 0);// top right
				GL11.glVertex2d(width, height);// bottom right
				GL11.glVertex2d(0, height);// bottom left
			GL11.glEnd();//@formatter:on
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glTranslated(x - (height / 2.0), y - (width / 2.0), z);
			GL11.glBegin(GL11.GL_QUADS);//@formatter:off
				GL11.glVertex2d(0, 0);// top left
				GL11.glVertex2d(height, 0);// top right
				GL11.glVertex2d(height, width);// bottom right
				GL11.glVertex2d(0, width);// bottom left
			GL11.glEnd();//@formatter:on
			GL11.glPopMatrix();
			
			//if(invertBackground) {
			//	GL11.glDisable(GL11.GL_BLEND);
			//}
		} finally {
			GLUtil.glPopBlendMode();
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(srcFactor, dstFactor);
			if(!blendEnabled) {
				GL11.glDisable(GL11.GL_BLEND);
			}
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(texTarget == 0 ? GL11.GL_TEXTURE_2D : texTarget, boundTexture);
			if(!tex2DEnabled) {
				GL11.glDisable(GL11.GL_TEXTURE_2D);
			}
			if(texTargetEnabled) {
				GL11.glEnable(texTarget);
			}
		}
	}
	
	public static final strictfp void glLegacyRenderCrosshairsAt(double x, double y, double z, boolean invertBackground) {
		glLegacyRenderCrosshairsAt(x, y, z, 21.0, 1.0, invertBackground);
	}
	
	public static final strictfp void glLegacyRenderCrosshairsOnMouse(boolean invertBackground) {
		java.awt.Point mLoc = Mouse.getLocationRelativeToCanvasOpenGL();
		glLegacyRenderCrosshairsAt(mLoc.x, mLoc.y, 1.0, 21.0, 1.0, invertBackground);
	}
	
	/** Inverts the colors within the specified area of the screen.
	 *
	 * @deprecated Uses deprecated legacy OpenGL calls (immediate mode) */
	@Deprecated
	public static final void glInvertColors(int x, int y, int width, int height) {
		GLUtil.glPushBlendMode();
		GLUtil.glBlend(true, GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO, GL14.GL_FUNC_ADD);
		
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_QUADS);//@formatter:off
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(x, y);// bottom left
			
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(x + width, y);// bottom right
			
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(x + width, y + height);// top right
			
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(x, y + height);// top left
		GL11.glEnd();//@formatter:on
		
		GLUtil.glPopBlendMode();
	}
	
	/** Inverts the entire screen contents' colors.
	 *
	 * @deprecated Uses deprecated legacy OpenGL calls (immediate mode) */
	@Deprecated
	public static final void glInvertColors() {
		int width = Window.getWindow().getWidth();
		int height = Window.getWindow().getHeight();
		
		glInvertColors(0, 0, width, height);
	}
	
	/** @param texture The texture to render
	 * @param size The size of the texture to use(null = display size, default)
	 * @param colorHue The hue to use(null = white, default) */
	public static void glRenderQuad(Texture texture, Vector2f size, Vector3f colorHue) {
		Vector4f hue = colorHue != null ? new Vector4f(colorHue.getX(), colorHue.getY(), colorHue.getZ(), 0.0f) : null;
		glRenderQuad(texture, size, hue);
	}
	
	/** @param texture The texture to render
	 * @param size The size of the texture to use(null = display size, default)
	 * @param colorHue The hue to use(null = white, default) */
	public static void glRenderQuad(Texture texture, Vector2f size, Vector4f colorHue) {
		glRenderQuad(texture, size, colorHue, false, false);
	}
	
	/** @param texture The texture to render
	 * @param size The size of the texture to use(null = display size, default)
	 * @param colorHue The hue to use(null = white, default)
	 * @param flipHorizontally Whether or not the texture should be flipped
	 *            horizontally(left to right)
	 * @param flipVertically Whether or not the texture should be flipped
	 *            vertically(top to bottom) */
	public static void glRenderQuad(Texture texture, Vector2f size, Vector4f colorHue, boolean flipHorizontally, boolean flipVertically) {
		if(texture != null) {
			texture.bind();
		}
		if(size == null) {
			size = new Vector2f(Window.getWindow().getWidth(), Window.getWindow().getHeight());
		}
		if(colorHue == null) {
			colorHue = new Vector4f(1, 1, 1, 0);
		}
		if(texture == TextureLoader.NONE) {
			GL11.glColor4f(1, 1, 1, 0);
		} else {
			GL11.glColor4f(colorHue.getX(), colorHue.getY(), colorHue.getZ(), colorHue.getW());
		}
		GL11.glBegin(GL11.GL_QUADS);
		if(flipHorizontally && flipVertically) {
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex2f(0, 0);// top left
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex2f(size.getX(), 0);// top right
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex2f(size.getX(), size.getY());// bottom right
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex2f(0, size.getY());// bottom left
		} else if(flipHorizontally) {
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex2f(0, 0);// top left
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex2f(size.getX(), 0);// top right
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex2f(size.getX(), size.getY());// bottom right
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex2f(0, size.getY());// bottom left
		} else if(flipVertically) {
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex2f(0, 0);// top left
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex2f(size.getX(), 0);// top right
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex2f(size.getX(), size.getY());// bottom right
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex2f(0, size.getY());// bottom left
		} else {
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex2f(0, 0);// bottom left
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex2f(size.getX(), 0);// bottom right
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex2f(size.getX(), size.getY());// top right
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex2f(0, size.getY());// top left
		}
		GL11.glEnd();
	}
	
	/** @param size The size of the texture to use(null = display size, default)
	 * @param color The color to use(null = white, default) */
	public static void glRenderQuad(Vector2f size, Vector3f color) {
		glRenderQuad(size, color != null ? new Vector4f(color.getX(), color.getY(), color.getZ(), 1.0f) : null);
	}
	
	/** @param size The size of the texture to use(null = display size, default)
	 * @param color The color to use(null = white, default) */
	public static void glRenderQuad(Vector2f size, Vector4f color) {
		if(size == null) {
			size = new Vector2f(Window.getWindow().getWidth(), Window.getWindow().getHeight());
		}
		if(color == null) {
			color = new Vector4f(1, 1, 1, 1);
		}
		GL11.glColor4f(color.getX(), color.getY(), color.getZ(), color.getW());
		GL11.glBegin(GL11.GL_QUADS);//@formatter:off
			GL11.glVertex2f(0, 0);// top left
			GL11.glVertex2f(size.getX(), 0);// top right
			GL11.glVertex2f(size.getX(), size.getY());// bottom right
			GL11.glVertex2f(0, size.getY());// bottom left
		GL11.glEnd();//@formatter:on
	}
	
	/** @param size The size of the texture to use(null = display size, default)
	 * @param color The color to use(null = white, default) */
	public static void glRenderQuad(Vector2d size, Vector4f color) {
		int width = Window.getWindow().getWidth();
		int height = Window.getWindow().getHeight();
		if(size == null) {
			size = new Vector2d(width, height);
		}
		if(color == null) {
			color = new Vector4f(1, 1, 1, 1);
		}
		GL11.glColor4f(color.getX(), color.getY(), color.getZ(), color.getW());
		GL11.glBegin(GL11.GL_QUADS);//@formatter:off
			GL11.glVertex2d(0, 0);// top left
			GL11.glVertex2d(size.getX(), 0);// top right
			GL11.glVertex2d(size.getX(), size.getY());// bottom right
			GL11.glVertex2d(0, size.getY());// bottom left
		GL11.glEnd();//@formatter:on
	}
	
	private static final float[] grayPauseOverlayColor = new float[] {0.25f, 0.25f, 0.25f, 0.75f};
	
	/** Renders a transparent gray quad over the entire game screen */
	public static final void glRenderGrayPauseOverlay() {
		int width = Window.getWindow().getWidth();
		int height = Window.getWindow().getHeight();
		Texture.unbindAllTextures();
		GLUtil.glPushColor();
		GLUtil.glColorf(grayPauseOverlayColor);
		GL11.glBegin(GL11.GL_QUADS);//@formatter:off
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(0, 0);// top left
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(width, 0);// top right
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(width, height);// bottom right
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(0, height);// bottom left
		GL11.glEnd();//@formatter:on
	}
	
	//================================================================================================================================================================================================
	
	public static final String getGLVersion(boolean gfxCard) {
		String glVersion = GL11.glGetString(GL11.GL_VERSION);
		if(!gfxCard) {
			glVersion = glVersion.contains(" ") ? glVersion.trim().split(Pattern.quote(" "))[0] : glVersion;
		}
		return glVersion;
	}
	
	/*public static final boolean isGL14Available(GLCapabilities caps, boolean fc) {
		return (fc || checkFunctions(//
				caps.glFogCoordf, caps.glFogCoordd, caps.glFogCoordfv, caps.glFogCoorddv, caps.glFogCoordPointer, caps.glSecondaryColor3b, caps.glSecondaryColor3s, //
				caps.glSecondaryColor3i, caps.glSecondaryColor3f, caps.glSecondaryColor3d, caps.glSecondaryColor3ub, caps.glSecondaryColor3us, //
				caps.glSecondaryColor3ui, caps.glSecondaryColor3bv, caps.glSecondaryColor3sv, caps.glSecondaryColor3iv, caps.glSecondaryColor3fv, //
				caps.glSecondaryColor3dv, caps.glSecondaryColor3ubv, caps.glSecondaryColor3usv, caps.glSecondaryColor3uiv, caps.glSecondaryColorPointer, //
				caps.glWindowPos2i, caps.glWindowPos2s, caps.glWindowPos2f, caps.glWindowPos2d, caps.glWindowPos2iv, caps.glWindowPos2sv, caps.glWindowPos2fv, //
				caps.glWindowPos2dv, caps.glWindowPos3i, caps.glWindowPos3s, caps.glWindowPos3f, caps.glWindowPos3d, caps.glWindowPos3iv, caps.glWindowPos3sv, //
				caps.glWindowPos3fv, caps.glWindowPos3dv//
		)) && checkFunctions(//
				caps.glBlendColor, caps.glBlendEquation, caps.glMultiDrawArrays, caps.glMultiDrawElements, caps.glPointParameterf, caps.glPointParameteri, //
				caps.glPointParameterfv, caps.glPointParameteriv, caps.glBlendFuncSeparate//
		);
	}*/
	
	/** @return Whether or not GL11 is available */
	public static boolean isGL11Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL11Available(caps, false);
	}
	
	/** @param caps The GLCapabilities
	 * @param GL_NV_vertex_buffer_unified_memory Whether or not the
	 *            <code>GL_NV_vertex_buffer_unified_memory</code> extension will
	 *            be checked
	 * @return Whether or not GL11 is available */
	public static boolean isGL11Available(GLCapabilities caps, boolean GL_NV_vertex_buffer_unified_memory) {
		return checkFunctions(caps.glAccum, caps.glAlphaFunc, caps.glAreTexturesResident, caps.glBegin, caps.glBitmap, caps.glCallList, caps.glCallLists, caps.glClearAccum, //
				caps.glClearIndex, caps.glColor3b, caps.glColor3s, caps.glColor3i, caps.glColor3f, caps.glColor3d, caps.glColor3ub, caps.glColor3us, //
				caps.glColor3ui, caps.glColor3bv, caps.glColor3sv, caps.glColor3iv, caps.glColor3fv, caps.glColor3dv, caps.glColor3ubv, caps.glColor3usv, //
				caps.glColor3uiv, caps.glColor4b, caps.glColor4s, caps.glColor4i, caps.glColor4f, caps.glColor4d, caps.glColor4ub, caps.glColor4us, caps.glColor4ui, //
				caps.glColor4bv, caps.glColor4sv, caps.glColor4iv, caps.glColor4fv, caps.glColor4dv, caps.glColor4ubv, caps.glColor4usv, caps.glColor4uiv, //
				caps.glColorMaterial, caps.glColorPointer, caps.glDeleteLists, caps.glDrawPixels, caps.glEdgeFlag, caps.glEdgeFlagv, caps.glEdgeFlagPointer, //
				caps.glEnd, caps.glEvalCoord1f, caps.glEvalCoord1fv, caps.glEvalCoord1d, caps.glEvalCoord1dv, caps.glEvalCoord2f, caps.glEvalCoord2fv, //
				caps.glEvalCoord2d, caps.glEvalCoord2dv, caps.glEvalMesh1, caps.glEvalMesh2, caps.glEvalPoint1, caps.glEvalPoint2, caps.glFeedbackBuffer, //
				caps.glFogi, caps.glFogiv, caps.glFogf, caps.glFogfv, caps.glGenLists, caps.glGetLightiv, caps.glGetLightfv, caps.glGetMapiv, caps.glGetMapfv, //
				caps.glGetMapdv, caps.glGetMaterialiv, caps.glGetMaterialfv, caps.glGetPixelMapfv, caps.glGetPixelMapusv, caps.glGetPixelMapuiv, //
				caps.glGetPolygonStipple, caps.glGetTexGeniv, caps.glGetTexGenfv, caps.glGetTexGendv, caps.glIndexi, caps.glIndexub, caps.glIndexs, caps.glIndexf, //
				caps.glIndexd, caps.glIndexiv, caps.glIndexubv, caps.glIndexsv, caps.glIndexfv, caps.glIndexdv, caps.glIndexMask, caps.glIndexPointer, //
				caps.glInitNames, caps.glIsList, caps.glLightModeli, caps.glLightModelf, caps.glLightModeliv, caps.glLightModelfv, caps.glLighti, caps.glLightf, //
				caps.glLightiv, caps.glLightfv, caps.glLineStipple, caps.glListBase, caps.glLoadMatrixf, caps.glLoadMatrixd, caps.glLoadIdentity, caps.glLoadName, //
				caps.glMap1f, caps.glMap1d, caps.glMap2f, caps.glMap2d, caps.glMapGrid1f, caps.glMapGrid1d, caps.glMapGrid2f, caps.glMapGrid2d, caps.glMateriali, //
				caps.glMaterialf, caps.glMaterialiv, caps.glMaterialfv, caps.glMatrixMode, caps.glMultMatrixf, caps.glMultMatrixd, caps.glFrustum, caps.glNewList, //
				caps.glEndList, caps.glNormal3f, caps.glNormal3b, caps.glNormal3s, caps.glNormal3i, caps.glNormal3d, caps.glNormal3fv, caps.glNormal3bv, //
				caps.glNormal3sv, caps.glNormal3iv, caps.glNormal3dv, caps.glNormalPointer, caps.glOrtho, caps.glPassThrough, caps.glPixelMapfv, caps.glPixelMapusv, //
				caps.glPixelMapuiv, caps.glPixelTransferi, caps.glPixelTransferf, caps.glPixelZoom, caps.glPolygonStipple, caps.glPushAttrib, //
				caps.glPushClientAttrib, caps.glPopAttrib, caps.glPopClientAttrib, caps.glPopMatrix, caps.glPopName, caps.glPrioritizeTextures, caps.glPushMatrix, //
				caps.glPushName, caps.glRasterPos2i, caps.glRasterPos2s, caps.glRasterPos2f, caps.glRasterPos2d, caps.glRasterPos2iv, caps.glRasterPos2sv, //
				caps.glRasterPos2fv, caps.glRasterPos2dv, caps.glRasterPos3i, caps.glRasterPos3s, caps.glRasterPos3f, caps.glRasterPos3d, caps.glRasterPos3iv, //
				caps.glRasterPos3sv, caps.glRasterPos3fv, caps.glRasterPos3dv, caps.glRasterPos4i, caps.glRasterPos4s, caps.glRasterPos4f, caps.glRasterPos4d, //
				caps.glRasterPos4iv, caps.glRasterPos4sv, caps.glRasterPos4fv, caps.glRasterPos4dv, caps.glRecti, caps.glRects, caps.glRectf, caps.glRectd, //
				caps.glRectiv, caps.glRectsv, caps.glRectfv, caps.glRectdv, caps.glRenderMode, caps.glRotatef, caps.glRotated, caps.glScalef, caps.glScaled, //
				caps.glSelectBuffer, caps.glShadeModel, caps.glTexCoord1f, caps.glTexCoord1s, caps.glTexCoord1i, caps.glTexCoord1d, caps.glTexCoord1fv, //
				caps.glTexCoord1sv, caps.glTexCoord1iv, caps.glTexCoord1dv, caps.glTexCoord2f, caps.glTexCoord2s, caps.glTexCoord2i, caps.glTexCoord2d, //
				caps.glTexCoord2fv, caps.glTexCoord2sv, caps.glTexCoord2iv, caps.glTexCoord2dv, caps.glTexCoord3f, caps.glTexCoord3s, caps.glTexCoord3i, //
				caps.glTexCoord3d, caps.glTexCoord3fv, caps.glTexCoord3sv, caps.glTexCoord3iv, caps.glTexCoord3dv, caps.glTexCoord4f, caps.glTexCoord4s, //
				caps.glTexCoord4i, caps.glTexCoord4d, caps.glTexCoord4fv, caps.glTexCoord4sv, caps.glTexCoord4iv, caps.glTexCoord4dv, caps.glTexCoordPointer, //
				caps.glTexGeni, caps.glTexGeniv, caps.glTexGenf, caps.glTexGenfv, caps.glTexGend, caps.glTexGendv, caps.glTranslatef, caps.glTranslated, //
				caps.glVertex2f, caps.glVertex2s, caps.glVertex2i, caps.glVertex2d, caps.glVertex2fv, caps.glVertex2sv, caps.glVertex2iv, caps.glVertex2dv, //
				caps.glVertex3f, caps.glVertex3s, caps.glVertex3i, caps.glVertex3d, caps.glVertex3fv, caps.glVertex3sv, caps.glVertex3iv, caps.glVertex3dv, //
				caps.glVertex4f, caps.glVertex4s, caps.glVertex4i, caps.glVertex4d, caps.glVertex4fv, caps.glVertex4sv, caps.glVertex4iv, caps.glVertex4dv, //
				caps.glVertexPointer) && checkFunctions(caps.glEnable, caps.glDisable, caps.glArrayElement, caps.glBindTexture, caps.glBlendFunc, caps.glClear, caps.glClearColor, caps.glClearDepth, //
						caps.glClearStencil, caps.glClipPlane, caps.glColorMask, caps.glCopyPixels, caps.glCullFace, caps.glDepthFunc, caps.glDepthMask, caps.glDepthRange, //
						GL_NV_vertex_buffer_unified_memory ? caps.glDisableClientState : -1L, caps.glDrawArrays, caps.glDrawBuffer, caps.glDrawElements, //
						GL_NV_vertex_buffer_unified_memory ? caps.glEnableClientState : -1L, caps.glFinish, caps.glFlush, caps.glFrontFace, //
						caps.glGenTextures, caps.glDeleteTextures, caps.glGetClipPlane, caps.glGetBooleanv, caps.glGetFloatv, caps.glGetIntegerv, caps.glGetDoublev, //
						caps.glGetError, caps.glGetPointerv, caps.glGetString, caps.glGetTexEnviv, caps.glGetTexEnvfv, caps.glGetTexImage, caps.glGetTexLevelParameteriv, //
						caps.glGetTexLevelParameterfv, caps.glGetTexParameteriv, caps.glGetTexParameterfv, caps.glHint, caps.glInterleavedArrays, caps.glIsEnabled, //
						caps.glIsTexture, caps.glLineWidth, caps.glLogicOp, caps.glPixelStorei, caps.glPixelStoref, caps.glPointSize, caps.glPolygonMode, //
						caps.glPolygonOffset, caps.glReadBuffer, caps.glReadPixels, caps.glScissor, caps.glStencilFunc, caps.glStencilMask, caps.glStencilOp, //
						caps.glTexEnvi, caps.glTexEnviv, caps.glTexEnvf, caps.glTexEnvfv, caps.glTexImage2D, caps.glTexImage1D, caps.glCopyTexImage2D, //
						caps.glCopyTexImage1D, caps.glCopyTexSubImage1D, caps.glCopyTexSubImage2D, caps.glTexParameteri, caps.glTexParameteriv, caps.glTexParameterf, //
						caps.glTexParameterfv, caps.glTexSubImage1D, caps.glTexSubImage2D, caps.glViewport);
	}
	
	/** @return Whether or not GL12 is available */
	public static boolean isGL12Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL12Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL12 is available */
	public static boolean isGL12Available(GLCapabilities caps) {
		return checkFunctions(caps.glTexImage3D, caps.glTexSubImage3D, caps.glCopyTexSubImage3D, caps.glDrawRangeElements);
	}
	
	/** @return Whether or not GL13 is available */
	public static boolean isGL13Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL13Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL13 is available */
	public static boolean isGL13Available(GLCapabilities caps) {
		return checkFunctions(caps.glClientActiveTexture, caps.glMultiTexCoord1f, caps.glMultiTexCoord1s, caps.glMultiTexCoord1i, caps.glMultiTexCoord1d, caps.glMultiTexCoord1fv, //
				caps.glMultiTexCoord1sv, caps.glMultiTexCoord1iv, caps.glMultiTexCoord1dv, caps.glMultiTexCoord2f, caps.glMultiTexCoord2s, caps.glMultiTexCoord2i, //
				caps.glMultiTexCoord2d, caps.glMultiTexCoord2fv, caps.glMultiTexCoord2sv, caps.glMultiTexCoord2iv, caps.glMultiTexCoord2dv, caps.glMultiTexCoord3f, //
				caps.glMultiTexCoord3s, caps.glMultiTexCoord3i, caps.glMultiTexCoord3d, caps.glMultiTexCoord3fv, caps.glMultiTexCoord3sv, caps.glMultiTexCoord3iv, //
				caps.glMultiTexCoord3dv, caps.glMultiTexCoord4f, caps.glMultiTexCoord4s, caps.glMultiTexCoord4i, caps.glMultiTexCoord4d, caps.glMultiTexCoord4fv, //
				caps.glMultiTexCoord4sv, caps.glMultiTexCoord4iv, caps.glMultiTexCoord4dv, caps.glLoadTransposeMatrixf, caps.glLoadTransposeMatrixd, //
				caps.glMultTransposeMatrixf, caps.glMultTransposeMatrixd//
		) && checkFunctions(caps.glCompressedTexImage3D, caps.glCompressedTexImage2D, caps.glCompressedTexImage1D, caps.glCompressedTexSubImage3D, //
				caps.glCompressedTexSubImage2D, caps.glCompressedTexSubImage1D, caps.glGetCompressedTexImage, caps.glSampleCoverage, caps.glActiveTexture);
	}
	
	/** @return Whether or not GL14 is available */
	public static boolean isGL14Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL14Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL14 is available */
	public static boolean isGL14Available(GLCapabilities caps) {
		return checkFunctions(caps.glFogCoordf, caps.glFogCoordd, caps.glFogCoordfv, caps.glFogCoorddv, caps.glFogCoordPointer, caps.glSecondaryColor3b, caps.glSecondaryColor3s, //
				caps.glSecondaryColor3i, caps.glSecondaryColor3f, caps.glSecondaryColor3d, caps.glSecondaryColor3ub, caps.glSecondaryColor3us, //
				caps.glSecondaryColor3ui, caps.glSecondaryColor3bv, caps.glSecondaryColor3sv, caps.glSecondaryColor3iv, caps.glSecondaryColor3fv, //
				caps.glSecondaryColor3dv, caps.glSecondaryColor3ubv, caps.glSecondaryColor3usv, caps.glSecondaryColor3uiv, caps.glSecondaryColorPointer, //
				caps.glWindowPos2i, caps.glWindowPos2s, caps.glWindowPos2f, caps.glWindowPos2d, caps.glWindowPos2iv, caps.glWindowPos2sv, caps.glWindowPos2fv, //
				caps.glWindowPos2dv, caps.glWindowPos3i, caps.glWindowPos3s, caps.glWindowPos3f, caps.glWindowPos3d, caps.glWindowPos3iv, caps.glWindowPos3sv, //
				caps.glWindowPos3fv, caps.glWindowPos3dv//
		) && checkFunctions(caps.glBlendColor, caps.glBlendEquation, caps.glMultiDrawArrays, caps.glMultiDrawElements, caps.glPointParameterf, caps.glPointParameteri, //
				caps.glPointParameterfv, caps.glPointParameteriv, caps.glBlendFuncSeparate);
	}
	
	/** @return Whether or not GL15 is available */
	public static boolean isGL15Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL15Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL15 is available */
	public static boolean isGL15Available(GLCapabilities caps) {
		return checkFunctions(caps.glBindBuffer, caps.glDeleteBuffers, caps.glGenBuffers, caps.glIsBuffer, caps.glBufferData, caps.glBufferSubData, caps.glGetBufferSubData, //
				caps.glMapBuffer, caps.glUnmapBuffer, caps.glGetBufferParameteriv, caps.glGetBufferPointerv, caps.glGenQueries, caps.glDeleteQueries, //
				caps.glIsQuery, caps.glBeginQuery, caps.glEndQuery, caps.glGetQueryiv, caps.glGetQueryObjectiv, caps.glGetQueryObjectuiv);
	}
	
	/** @return Whether or not GL20 is available */
	public static boolean isGL20Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL20Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL20 is available */
	public static boolean isGL20Available(GLCapabilities caps) {
		return checkFunctions(caps.glCreateProgram, caps.glDeleteProgram, caps.glIsProgram, caps.glCreateShader, caps.glDeleteShader, caps.glIsShader, caps.glAttachShader, //
				caps.glDetachShader, caps.glShaderSource, caps.glCompileShader, caps.glLinkProgram, caps.glUseProgram, caps.glValidateProgram, caps.glUniform1f, //
				caps.glUniform2f, caps.glUniform3f, caps.glUniform4f, caps.glUniform1i, caps.glUniform2i, caps.glUniform3i, caps.glUniform4i, caps.glUniform1fv, //
				caps.glUniform2fv, caps.glUniform3fv, caps.glUniform4fv, caps.glUniform1iv, caps.glUniform2iv, caps.glUniform3iv, caps.glUniform4iv, //
				caps.glUniformMatrix2fv, caps.glUniformMatrix3fv, caps.glUniformMatrix4fv, caps.glGetShaderiv, caps.glGetProgramiv, caps.glGetShaderInfoLog, //
				caps.glGetProgramInfoLog, caps.glGetAttachedShaders, caps.glGetUniformLocation, caps.glGetActiveUniform, caps.glGetUniformfv, caps.glGetUniformiv, //
				caps.glGetShaderSource, caps.glVertexAttrib1f, caps.glVertexAttrib1s, caps.glVertexAttrib1d, caps.glVertexAttrib2f, caps.glVertexAttrib2s, //
				caps.glVertexAttrib2d, caps.glVertexAttrib3f, caps.glVertexAttrib3s, caps.glVertexAttrib3d, caps.glVertexAttrib4f, caps.glVertexAttrib4s, //
				caps.glVertexAttrib4d, caps.glVertexAttrib4Nub, caps.glVertexAttrib1fv, caps.glVertexAttrib1sv, caps.glVertexAttrib1dv, caps.glVertexAttrib2fv, //
				caps.glVertexAttrib2sv, caps.glVertexAttrib2dv, caps.glVertexAttrib3fv, caps.glVertexAttrib3sv, caps.glVertexAttrib3dv, caps.glVertexAttrib4fv, //
				caps.glVertexAttrib4sv, caps.glVertexAttrib4dv, caps.glVertexAttrib4iv, caps.glVertexAttrib4bv, caps.glVertexAttrib4ubv, caps.glVertexAttrib4usv, //
				caps.glVertexAttrib4uiv, caps.glVertexAttrib4Nbv, caps.glVertexAttrib4Nsv, caps.glVertexAttrib4Niv, caps.glVertexAttrib4Nubv, //
				caps.glVertexAttrib4Nusv, caps.glVertexAttrib4Nuiv, caps.glVertexAttribPointer, caps.glEnableVertexAttribArray, caps.glDisableVertexAttribArray, //
				caps.glBindAttribLocation, caps.glGetActiveAttrib, caps.glGetAttribLocation, caps.glGetVertexAttribiv, caps.glGetVertexAttribfv, //
				caps.glGetVertexAttribdv, caps.glGetVertexAttribPointerv, caps.glDrawBuffers, caps.glBlendEquationSeparate, caps.glStencilOpSeparate, //
				caps.glStencilFuncSeparate, caps.glStencilMaskSeparate);
	}
	
	/** @return Whether or not GL21 is available */
	public static boolean isGL21Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL21Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL21 is available */
	public static boolean isGL21Available(GLCapabilities caps) {
		return checkFunctions(caps.glUniformMatrix2x3fv, caps.glUniformMatrix3x2fv, caps.glUniformMatrix2x4fv, caps.glUniformMatrix4x2fv, caps.glUniformMatrix3x4fv, //
				caps.glUniformMatrix4x3fv);
	}
	
	/** @return Whether or not GL30 is available */
	public static boolean isGL30Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL30Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL30 is available */
	public static boolean isGL30Available(GLCapabilities caps) {
		return checkFunctions(caps.glGetStringi, caps.glClearBufferiv, caps.glClearBufferuiv, caps.glClearBufferfv, caps.glClearBufferfi, caps.glVertexAttribI1i, //
				caps.glVertexAttribI2i, caps.glVertexAttribI3i, caps.glVertexAttribI4i, caps.glVertexAttribI1ui, caps.glVertexAttribI2ui, caps.glVertexAttribI3ui, //
				caps.glVertexAttribI4ui, caps.glVertexAttribI1iv, caps.glVertexAttribI2iv, caps.glVertexAttribI3iv, caps.glVertexAttribI4iv, //
				caps.glVertexAttribI1uiv, caps.glVertexAttribI2uiv, caps.glVertexAttribI3uiv, caps.glVertexAttribI4uiv, caps.glVertexAttribI4bv, //
				caps.glVertexAttribI4sv, caps.glVertexAttribI4ubv, caps.glVertexAttribI4usv, caps.glVertexAttribIPointer, caps.glGetVertexAttribIiv, //
				caps.glGetVertexAttribIuiv, caps.glUniform1ui, caps.glUniform2ui, caps.glUniform3ui, caps.glUniform4ui, caps.glUniform1uiv, caps.glUniform2uiv, //
				caps.glUniform3uiv, caps.glUniform4uiv, caps.glGetUniformuiv, caps.glBindFragDataLocation, caps.glGetFragDataLocation, //
				caps.glBeginConditionalRender, caps.glEndConditionalRender, caps.glMapBufferRange, caps.glFlushMappedBufferRange, caps.glClampColor, //
				caps.glIsRenderbuffer, caps.glBindRenderbuffer, caps.glDeleteRenderbuffers, caps.glGenRenderbuffers, caps.glRenderbufferStorage, //
				caps.glRenderbufferStorageMultisample, caps.glGetRenderbufferParameteriv, caps.glIsFramebuffer, caps.glBindFramebuffer, caps.glDeleteFramebuffers, //
				caps.glGenFramebuffers, caps.glCheckFramebufferStatus, caps.glFramebufferTexture1D, caps.glFramebufferTexture2D, caps.glFramebufferTexture3D, //
				caps.glFramebufferTextureLayer, caps.glFramebufferRenderbuffer, caps.glGetFramebufferAttachmentParameteriv, caps.glBlitFramebuffer, //
				caps.glGenerateMipmap, caps.glTexParameterIiv, caps.glTexParameterIuiv, caps.glGetTexParameterIiv, caps.glGetTexParameterIuiv, caps.glColorMaski, //
				caps.glGetBooleani_v, caps.glGetIntegeri_v, caps.glEnablei, caps.glDisablei, caps.glIsEnabledi, caps.glBindBufferRange, caps.glBindBufferBase, //
				caps.glBeginTransformFeedback, caps.glEndTransformFeedback, caps.glTransformFeedbackVaryings, caps.glGetTransformFeedbackVarying, //
				caps.glBindVertexArray, caps.glDeleteVertexArrays, caps.glGenVertexArrays, caps.glIsVertexArray//
		);
	}
	
	/** @return Whether or not GL31 is available */
	public static boolean isGL31Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL31Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL31 is available */
	public static boolean isGL31Available(GLCapabilities caps) {
		return checkFunctions(caps.glDrawArraysInstanced, caps.glDrawElementsInstanced, caps.glCopyBufferSubData, caps.glPrimitiveRestartIndex, caps.glTexBuffer, //
				caps.glGetUniformIndices, caps.glGetActiveUniformsiv, caps.glGetActiveUniformName, caps.glGetUniformBlockIndex, caps.glGetActiveUniformBlockiv, //
				caps.glGetActiveUniformBlockName, caps.glUniformBlockBinding);
	}
	
	/** @return Whether or not GL32 is available */
	public static boolean isGL32Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL32Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL32 is available */
	public static boolean isGL32Available(GLCapabilities caps) {
		return checkFunctions(caps.glGetBufferParameteri64v, caps.glDrawElementsBaseVertex, caps.glDrawRangeElementsBaseVertex, caps.glDrawElementsInstancedBaseVertex, //
				caps.glMultiDrawElementsBaseVertex, caps.glProvokingVertex, caps.glTexImage2DMultisample, caps.glTexImage3DMultisample, caps.glGetMultisamplefv, //
				caps.glSampleMaski, caps.glFramebufferTexture, caps.glFenceSync, caps.glIsSync, caps.glDeleteSync, caps.glClientWaitSync, caps.glWaitSync, //
				caps.glGetInteger64v, caps.glGetInteger64i_v, caps.glGetSynciv);
	}
	
	/** @return Whether or not GL33 is available */
	public static boolean isGL33Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL33Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL33 is available */
	public static boolean isGL33Available(GLCapabilities caps) {
		return checkFunctions(caps.glVertexP2ui, caps.glVertexP3ui, caps.glVertexP4ui, caps.glVertexP2uiv, caps.glVertexP3uiv, caps.glVertexP4uiv, caps.glTexCoordP1ui, //
				caps.glTexCoordP2ui, caps.glTexCoordP3ui, caps.glTexCoordP4ui, caps.glTexCoordP1uiv, caps.glTexCoordP2uiv, caps.glTexCoordP3uiv, //
				caps.glTexCoordP4uiv, caps.glMultiTexCoordP1ui, caps.glMultiTexCoordP2ui, caps.glMultiTexCoordP3ui, caps.glMultiTexCoordP4ui, //
				caps.glMultiTexCoordP1uiv, caps.glMultiTexCoordP2uiv, caps.glMultiTexCoordP3uiv, caps.glMultiTexCoordP4uiv, caps.glNormalP3ui, caps.glNormalP3uiv, //
				caps.glColorP3ui, caps.glColorP4ui, caps.glColorP3uiv, caps.glColorP4uiv, caps.glSecondaryColorP3ui, caps.glSecondaryColorP3uiv//
		) && checkFunctions(caps.glBindFragDataLocationIndexed, caps.glGetFragDataIndex, caps.glGenSamplers, caps.glDeleteSamplers, caps.glIsSampler, caps.glBindSampler, //
				caps.glSamplerParameteri, caps.glSamplerParameterf, caps.glSamplerParameteriv, caps.glSamplerParameterfv, caps.glSamplerParameterIiv, //
				caps.glSamplerParameterIuiv, caps.glGetSamplerParameteriv, caps.glGetSamplerParameterfv, caps.glGetSamplerParameterIiv, //
				caps.glGetSamplerParameterIuiv, caps.glQueryCounter, caps.glGetQueryObjecti64v, caps.glGetQueryObjectui64v, caps.glVertexAttribDivisor, //
				caps.glVertexAttribP1ui, caps.glVertexAttribP2ui, caps.glVertexAttribP3ui, caps.glVertexAttribP4ui, caps.glVertexAttribP1uiv, //
				caps.glVertexAttribP2uiv, caps.glVertexAttribP3uiv, caps.glVertexAttribP4uiv);
	}
	
	/** @return Whether or not GL40 is available */
	public static boolean isGL40Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL40Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL40 is available */
	public static boolean isGL40Available(GLCapabilities caps) {
		return checkFunctions(caps.glBlendEquationi, caps.glBlendEquationSeparatei, caps.glBlendFunci, caps.glBlendFuncSeparatei, caps.glDrawArraysIndirect, caps.glDrawElementsIndirect, caps.glUniform1d, caps.glUniform2d, caps.glUniform3d, caps.glUniform4d, caps.glUniform1dv, caps.glUniform2dv, caps.glUniform3dv, caps.glUniform4dv, caps.glUniformMatrix2dv, caps.glUniformMatrix3dv, caps.glUniformMatrix4dv, caps.glUniformMatrix2x3dv, caps.glUniformMatrix2x4dv, caps.glUniformMatrix3x2dv, caps.glUniformMatrix3x4dv, caps.glUniformMatrix4x2dv, caps.glUniformMatrix4x3dv, caps.glGetUniformdv, caps.glMinSampleShading, caps.glGetSubroutineUniformLocation, caps.glGetSubroutineIndex, caps.glGetActiveSubroutineUniformiv, caps.glGetActiveSubroutineUniformName, caps.glGetActiveSubroutineName, caps.glUniformSubroutinesuiv, caps.glGetUniformSubroutineuiv, caps.glGetProgramStageiv, caps.glPatchParameteri, caps.glPatchParameterfv, caps.glBindTransformFeedback, caps.glDeleteTransformFeedbacks, caps.glGenTransformFeedbacks, caps.glIsTransformFeedback, caps.glPauseTransformFeedback, caps.glResumeTransformFeedback, caps.glDrawTransformFeedback, caps.glDrawTransformFeedbackStream, caps.glBeginQueryIndexed, caps.glEndQueryIndexed, caps.glGetQueryIndexediv);
	}
	
	/** @return Whether or not GL41 is available */
	public static boolean isGL41Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL41Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL41 is available */
	public static boolean isGL41Available(GLCapabilities caps) {
		return checkFunctions(caps.glReleaseShaderCompiler, caps.glShaderBinary, caps.glGetShaderPrecisionFormat, caps.glDepthRangef, caps.glClearDepthf, caps.glGetProgramBinary, //
				caps.glProgramBinary, caps.glProgramParameteri, caps.glUseProgramStages, caps.glActiveShaderProgram, caps.glCreateShaderProgramv, //
				caps.glBindProgramPipeline, caps.glDeleteProgramPipelines, caps.glGenProgramPipelines, caps.glIsProgramPipeline, caps.glGetProgramPipelineiv, //
				caps.glProgramUniform1i, caps.glProgramUniform2i, caps.glProgramUniform3i, caps.glProgramUniform4i, caps.glProgramUniform1ui, //
				caps.glProgramUniform2ui, caps.glProgramUniform3ui, caps.glProgramUniform4ui, caps.glProgramUniform1f, caps.glProgramUniform2f, //
				caps.glProgramUniform3f, caps.glProgramUniform4f, caps.glProgramUniform1d, caps.glProgramUniform2d, caps.glProgramUniform3d, //
				caps.glProgramUniform4d, caps.glProgramUniform1iv, caps.glProgramUniform2iv, caps.glProgramUniform3iv, caps.glProgramUniform4iv, //
				caps.glProgramUniform1uiv, caps.glProgramUniform2uiv, caps.glProgramUniform3uiv, caps.glProgramUniform4uiv, caps.glProgramUniform1fv, //
				caps.glProgramUniform2fv, caps.glProgramUniform3fv, caps.glProgramUniform4fv, caps.glProgramUniform1dv, caps.glProgramUniform2dv, //
				caps.glProgramUniform3dv, caps.glProgramUniform4dv, caps.glProgramUniformMatrix2fv, caps.glProgramUniformMatrix3fv, caps.glProgramUniformMatrix4fv, //
				caps.glProgramUniformMatrix2dv, caps.glProgramUniformMatrix3dv, caps.glProgramUniformMatrix4dv, caps.glProgramUniformMatrix2x3fv, //
				caps.glProgramUniformMatrix3x2fv, caps.glProgramUniformMatrix2x4fv, caps.glProgramUniformMatrix4x2fv, caps.glProgramUniformMatrix3x4fv, //
				caps.glProgramUniformMatrix4x3fv, caps.glProgramUniformMatrix2x3dv, caps.glProgramUniformMatrix3x2dv, caps.glProgramUniformMatrix2x4dv, //
				caps.glProgramUniformMatrix4x2dv, caps.glProgramUniformMatrix3x4dv, caps.glProgramUniformMatrix4x3dv, caps.glValidateProgramPipeline, //
				caps.glGetProgramPipelineInfoLog, caps.glVertexAttribL1d, caps.glVertexAttribL2d, caps.glVertexAttribL3d, caps.glVertexAttribL4d, //
				caps.glVertexAttribL1dv, caps.glVertexAttribL2dv, caps.glVertexAttribL3dv, caps.glVertexAttribL4dv, caps.glVertexAttribLPointer, //
				caps.glGetVertexAttribLdv, caps.glViewportArrayv, caps.glViewportIndexedf, caps.glViewportIndexedfv, caps.glScissorArrayv, caps.glScissorIndexed, //
				caps.glScissorIndexedv, caps.glDepthRangeArrayv, caps.glDepthRangeIndexed, caps.glGetFloati_v, caps.glGetDoublei_v);
	}
	
	/** @return Whether or not GL42 is available */
	public static boolean isGL42Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL42Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL42 is available */
	public static boolean isGL42Available(GLCapabilities caps) {
		return checkFunctions(caps.glGetActiveAtomicCounterBufferiv, caps.glTexStorage1D, caps.glTexStorage2D, caps.glTexStorage3D, caps.glDrawTransformFeedbackInstanced, //
				caps.glDrawTransformFeedbackStreamInstanced, caps.glDrawArraysInstancedBaseInstance, caps.glDrawElementsInstancedBaseInstance, //
				caps.glDrawElementsInstancedBaseVertexBaseInstance, caps.glBindImageTexture, caps.glMemoryBarrier, caps.glGetInternalformativ);
	}
	
	/** @return Whether or not GL43 is available */
	public static boolean isGL43Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL43Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL43 is available */
	public static boolean isGL43Available(GLCapabilities caps) {
		return checkFunctions(caps.glClearBufferData, caps.glClearBufferSubData, caps.glDispatchCompute, caps.glDispatchComputeIndirect, caps.glCopyImageSubData, //
				caps.glDebugMessageControl, caps.glDebugMessageInsert, caps.glDebugMessageCallback, caps.glGetDebugMessageLog, caps.glPushDebugGroup, //
				caps.glPopDebugGroup, caps.glObjectLabel, caps.glGetObjectLabel, caps.glObjectPtrLabel, caps.glGetObjectPtrLabel, caps.glFramebufferParameteri, //
				caps.glGetFramebufferParameteriv, caps.glGetInternalformati64v, caps.glInvalidateTexSubImage, caps.glInvalidateTexImage, //
				caps.glInvalidateBufferSubData, caps.glInvalidateBufferData, caps.glInvalidateFramebuffer, caps.glInvalidateSubFramebuffer, //
				caps.glMultiDrawArraysIndirect, caps.glMultiDrawElementsIndirect, caps.glGetProgramInterfaceiv, caps.glGetProgramResourceIndex, //
				caps.glGetProgramResourceName, caps.glGetProgramResourceiv, caps.glGetProgramResourceLocation, caps.glGetProgramResourceLocationIndex, //
				caps.glShaderStorageBlockBinding, caps.glTexBufferRange, caps.glTexStorage2DMultisample, caps.glTexStorage3DMultisample, caps.glTextureView, //
				caps.glBindVertexBuffer, caps.glVertexAttribFormat, caps.glVertexAttribIFormat, caps.glVertexAttribLFormat, caps.glVertexAttribBinding, //
				caps.glVertexBindingDivisor);
	}
	
	/** @return Whether or not GL44 is available */
	public static boolean isGL44Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL44Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL44 is available */
	public static boolean isGL44Available(GLCapabilities caps) {
		return checkFunctions(caps.glBufferStorage, caps.glClearTexSubImage, caps.glClearTexImage, caps.glBindBuffersBase, caps.glBindBuffersRange, caps.glBindTextures, //
				caps.glBindSamplers, caps.glBindImageTextures, caps.glBindVertexBuffers);
	}
	
	/** @return Whether or not GL45 is available */
	public static boolean isGL45Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL45Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL45 is available */
	public static boolean isGL45Available(GLCapabilities caps) {
		return checkFunctions(caps.glClipControl, caps.glCreateTransformFeedbacks, caps.glTransformFeedbackBufferBase, caps.glTransformFeedbackBufferRange, //
				caps.glGetTransformFeedbackiv, caps.glGetTransformFeedbacki_v, caps.glGetTransformFeedbacki64_v, caps.glCreateBuffers, caps.glNamedBufferStorage, //
				caps.glNamedBufferData, caps.glNamedBufferSubData, caps.glCopyNamedBufferSubData, caps.glClearNamedBufferData, caps.glClearNamedBufferSubData, //
				caps.glMapNamedBuffer, caps.glMapNamedBufferRange, caps.glUnmapNamedBuffer, caps.glFlushMappedNamedBufferRange, caps.glGetNamedBufferParameteriv, //
				caps.glGetNamedBufferParameteri64v, caps.glGetNamedBufferPointerv, caps.glGetNamedBufferSubData, caps.glCreateFramebuffers, //
				caps.glNamedFramebufferRenderbuffer, caps.glNamedFramebufferParameteri, caps.glNamedFramebufferTexture, caps.glNamedFramebufferTextureLayer, //
				caps.glNamedFramebufferDrawBuffer, caps.glNamedFramebufferDrawBuffers, caps.glNamedFramebufferReadBuffer, caps.glInvalidateNamedFramebufferData, //
				caps.glInvalidateNamedFramebufferSubData, caps.glClearNamedFramebufferiv, caps.glClearNamedFramebufferuiv, caps.glClearNamedFramebufferfv, //
				caps.glClearNamedFramebufferfi, caps.glBlitNamedFramebuffer, caps.glCheckNamedFramebufferStatus, caps.glGetNamedFramebufferParameteriv, //
				caps.glGetNamedFramebufferAttachmentParameteriv, caps.glCreateRenderbuffers, caps.glNamedRenderbufferStorage, //
				caps.glNamedRenderbufferStorageMultisample, caps.glGetNamedRenderbufferParameteriv, caps.glCreateTextures, caps.glTextureBuffer, //
				caps.glTextureBufferRange, caps.glTextureStorage1D, caps.glTextureStorage2D, caps.glTextureStorage3D, caps.glTextureStorage2DMultisample, //
				caps.glTextureStorage3DMultisample, caps.glTextureSubImage1D, caps.glTextureSubImage2D, caps.glTextureSubImage3D, //
				caps.glCompressedTextureSubImage1D, caps.glCompressedTextureSubImage2D, caps.glCompressedTextureSubImage3D, caps.glCopyTextureSubImage1D, //
				caps.glCopyTextureSubImage2D, caps.glCopyTextureSubImage3D, caps.glTextureParameterf, caps.glTextureParameterfv, caps.glTextureParameteri, //
				caps.glTextureParameterIiv, caps.glTextureParameterIuiv, caps.glTextureParameteriv, caps.glGenerateTextureMipmap, caps.glBindTextureUnit, //
				caps.glGetTextureImage, caps.glGetCompressedTextureImage, caps.glGetTextureLevelParameterfv, caps.glGetTextureLevelParameteriv, //
				caps.glGetTextureParameterfv, caps.glGetTextureParameterIiv, caps.glGetTextureParameterIuiv, caps.glGetTextureParameteriv, //
				caps.glCreateVertexArrays, caps.glDisableVertexArrayAttrib, caps.glEnableVertexArrayAttrib, caps.glVertexArrayElementBuffer, //
				caps.glVertexArrayVertexBuffer, caps.glVertexArrayVertexBuffers, caps.glVertexArrayAttribFormat, caps.glVertexArrayAttribIFormat, //
				caps.glVertexArrayAttribLFormat, caps.glVertexArrayAttribBinding, caps.glVertexArrayBindingDivisor, caps.glGetVertexArrayiv, //
				caps.glGetVertexArrayIndexediv, caps.glGetVertexArrayIndexed64iv, caps.glCreateSamplers, caps.glCreateProgramPipelines, caps.glCreateQueries, //
				caps.glGetQueryBufferObjectiv, caps.glGetQueryBufferObjectuiv, caps.glGetQueryBufferObjecti64v, caps.glGetQueryBufferObjectui64v, //
				caps.glMemoryBarrierByRegion, caps.glGetTextureSubImage, caps.glGetCompressedTextureSubImage, caps.glTextureBarrier, caps.glGetGraphicsResetStatus, //
				caps.glReadnPixels, caps.glGetnUniformfv, caps.glGetnUniformiv, caps.glGetnUniformuiv);
	}
	
	/** @return Whether or not GL46 is available */
	public static boolean isGL46Available() {
		GLCapabilities caps = null;
		try {
			caps = GL.getCapabilities();
		} catch(IllegalStateException ex) {
			return false;
		}
		return caps == null ? false : isGL46Available(caps);
	}
	
	/** @param caps The GLCapabilities
	 * @return Whether or not GL46 is available */
	public static boolean isGL46Available(GLCapabilities caps) {
		return checkFunctions(caps.glMultiDrawArraysIndirectCount, caps.glMultiDrawElementsIndirectCount, caps.glPolygonOffsetClamp, caps.glSpecializeShader);
	}
	
}
