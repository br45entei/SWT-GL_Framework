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
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.util.CodeUtil;

import org.lwjgl.opengl.GL11;

/** MatrixStack is a class which you use to store, manipulate, and retrieve
 * matrices. Its main use is with shaders, as it is built to replace the
 * deprecated {@link GL11#glLoadMatrixf(float[]) GL11.glLoadMatrixf(float[])}
 * functions.
 * 
 * @since 1.0
 * @author Brian_Entei */
public strictfp class MatrixStack {
	
	/** Tests this {@link MatrixStack} class's various methods for accuracy.
	 * 
	 * @param args Program command line arguments (unused) */
	public static final void main(String[] args) {
		MatrixStack stack = new MatrixStack();
		System.out.println(stack.push());
		System.out.println(stack.rotate(0, 180, 0));
		stack.pop();
		System.out.println(stack);
		System.out.println(stack.rotate(90, 0, 27));
		System.out.println(stack.rotate(-90, 0, -27, RotationOrder.ZXY));
		System.out.println(stack.transpose());
		System.out.println(stack.loadMatrix(new double[] {5, 5, 5, 5, 5}));
		System.out.println(stack.lookAt(new double[] {1, 3, 7}, new double[] {-3, 7, 11}, new double[] {0, 1, 0}));
		
		/* Yaw: (looking around horizontally; left and right head movement)
		 *           0°
		 *     315°  Z-  45°
		 *        \  |  /
		 * 270°  X-  y   X+  90°
		 *        /  |  \
		 *     225°  Z+  135°
		 *          180°
		 * 
		 * Pitch: (looking around vertically; up and down head movement)
		 *          270°
		 *     225°  Y+  315°
		 *        \  |  /
		 * 180°  Z+  x   Z-  0°
		 *        /  |  \
		 *     135°  Y-  45°
		 *          90°
		 * 
		 * Roll: (looking around sideways; tilting head movement)
		 *           0°
		 *     315°  Y+  45°
		 *        \  |  /
		 * 270°  X-  z   X+  90°
		 *        /  |  \
		 *     225°  Y-  135°
		 *          180°
		 */
		
		String lookAtTest = stack.lookAt(new double[] {0, 3, 0}, new double[] {4, 3, -4}, new double[] {0, 1, 0}).toString();
		String modelViewTest = stack.modelView(0, 3, 0, 45, 0, 0).toString();
		System.out.println(lookAtTest);
		System.out.println(modelViewTest);
		System.out.println("===================================================");
		if(!lookAtTest.replace("-0.00000000", " 0.00000000").equals(modelViewTest.replace("-0.00000000", " 0.00000000"))) {
			throw new IllegalStateException("The Look-At matrix does not match the equivalent Model-View matrix!");
		}
		System.out.println("The Look-At matrix and the Model-View matrix match!");
		System.out.println(stack.modelView(0, 0, 0, 0, 0, 0));
		try {
			System.out.println(stack.loadMatrix(new double[] {5, 5, 5, 5, 75}, 4, 2));
			System.out.println("This text should not appear!");
		} catch(IndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}
	
	/** Copies and returns the specified matrix as a float array.
	 * 
	 * @param matrix The matrix to convert
	 * @return A copy of the specified matrix, as a float array */
	public static final float[] wrapToFloat(double[] matrix) {
		float[] rtrn = new float[matrix.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = (float) matrix[i];
		}
		return rtrn;
	}
	
	/** Copies and returns the specified matrix as a double array.
	 * 
	 * @param matrix The matrix to convert
	 * @return A copy of the specified matrix, as a double array */
	public static final double[] wrapToDouble(float[] matrix) {
		double[] rtrn = new double[matrix.length];
		for(int i = 0; i < rtrn.length; i++) {
			rtrn[i] = matrix[i];
		}
		return rtrn;
	}
	
	/** The size of each matrix array in a given {@link MatrixStack}'s internal
	 * stack. */
	public static final int MATRIX_ARRAY_LENGTH = 16;
	
	private final double[][] stack;
	private volatile int index = 0;
	
	/** @param stackSize The total number of matrices that can be pushed onto
	 *            this MatrixStack */
	public MatrixStack(int stackSize) {
		this.stack = new double[stackSize][MATRIX_ARRAY_LENGTH];
		for(int i = 0; i < this.stack.length; i++) {
			this.stack[i] = new double[MATRIX_ARRAY_LENGTH];
		}
		this.loadIdentity();
	}
	
	/** Creates a new MatrixStack with a stack size of <tt>20</tt>. */
	public MatrixStack() {
		this(20);
	}
	
	/** Returns the size of this {@link MatrixStack}'s internal matrix stack; in
	 * other words, the total number of matrices that can be pushed onto this
	 * MatrixStack.
	 * 
	 * @return This MatrixStack's internal stack size */
	public int getStackSize() {
		return this.stack.length;
	}
	
	/** Returns the index of the current matrix within this
	 * {@link MatrixStack}'s internal stack.
	 * 
	 * @return The current matrix's stack index */
	public int getStackIndex() {
		return this.index;
	}
	
	/** Returns a String representation of this {@link MatrixStack}'s current
	 * matrix using {@link GLUtil#matrix4x4ToStringd(double[], int, boolean)}.
	 * 
	 * @param padDigits Whether or not the printed decimal places should be
	 *            padded with trailing zeros if their length isn't at least
	 *            <tt>decimalPlaces</tt>
	 * @param decimalPlaces The number of digits to show after the decimal
	 * @return The resulting String */
	public String toString(boolean padDigits, int decimalPlaces) {
		return GLUtil.matrix4x4ToStringd(this.stack[this.index], decimalPlaces, padDigits);
	}
	
	@Override
	public String toString() {
		return this.toString(true, 8);
	}
	
	/** Returns whether or not this {@link MatrixStack}'s internal stack can
	 * have another matrix pushed onto it.<br>
	 * If the {@link #getStackIndex() current index} plus one is greater than or
	 * equal to the {@link #getStackSize() internal stack size}, this method
	 * returns false.
	 * 
	 * @return Whether or not this MatrixStack's internal stack can have another
	 *         matrix pushed onto it */
	public boolean canPush() {
		return this.index + 1 < this.stack.length;
	}
	
	/** Returns whether or not this {@link MatrixStack}'s internal stack has an
	 * available matrix that can be popped off of it.<br>
	 * If the {@link #getStackIndex() current index} minus one is less than
	 * zero, this method returns false.
	 * 
	 * @return Whether or not this MatrixStack's internal stack has an available
	 *         matrix that can be popped off of it */
	public boolean canPop() {
		return this.index - 1 >= 0;
	}
	
	/** Duplicates the current matrix and pushes it back onto the stack.
	 * 
	 * @return This MatrixStack */
	public MatrixStack push() {
		if(!this.canPush()) {
			throw new IllegalStateException("Cannot push any more matrices onto the stack!");
		}
		System.arraycopy(this.stack[this.index++], 0, this.stack[this.index], 0, MATRIX_ARRAY_LENGTH);
		return this;
	}
	
	/** Pops the current matrix off of the stack and returns a copy of it.
	 * 
	 * @return The matrix that was just popped off the stack
	 * @throws IllegalStateException Thrown if there are no more matrices to pop
	 *             off of the stack
	 * @see #canPop() */
	public double[] pop() throws IllegalStateException {
		if(!this.canPop()) {
			throw new IllegalStateException("Cannot pop any more matrices off of the stack!");
		}
		try {
			return this.peek();
		} finally {
			this.index--;
		}
	}
	
	/** Pops the current matrix off of the stack and returns a copy of it.
	 * 
	 * @return The matrix that was just popped off the stack
	 * @throws IllegalStateException Thrown if there are no more matrices to pop
	 *             off of the stack
	 * @see #canPop() */
	public float[] popf() throws IllegalStateException {
		if(!this.canPop()) {
			throw new IllegalStateException("Cannot pop any more matrices off of the stack!");
		}
		try {
			return this.peekf();
		} finally {
			this.index--;
		}
	}
	
	protected static void checkArrayParams(int arrayLength, int offset, int length) throws IndexOutOfBoundsException {
		if(offset < 0 || length < 0 || offset >= arrayLength || offset + length > arrayLength || length > MATRIX_ARRAY_LENGTH) {
			String lineSeparator = CodeUtil.getProperty("line.separator");
			String msg = "";
			if(offset < 0) {
				msg = msg.concat(String.format("Offset '%s' is less than zero!%s", Integer.toString(offset), lineSeparator));
			}
			if(length < 0) {
				msg = msg.concat(String.format("Length '%s' is less than zero!%s", Integer.toString(length), lineSeparator));
			}
			if(offset >= arrayLength) {
				msg = msg.concat(String.format("Offset '%s' is greater than the length of the supplied array! (%s)%s", Integer.toString(offset), Integer.toString(arrayLength), lineSeparator));
			}
			if(offset + length > arrayLength) {
				msg = msg.concat(String.format("Offset '%s' plus Length '%s' is greater than the length of the supplied array! (%s)%s", Integer.toString(offset), Integer.toString(length), Integer.toString(arrayLength), lineSeparator));
			}
			if(length > MATRIX_ARRAY_LENGTH) {
				msg = msg.concat(String.format("Length '%s' is greater than the MATRIX_ARRAY_LENGTH! (%s)%s", Integer.toString(length), Integer.toString(MATRIX_ARRAY_LENGTH), lineSeparator));
			}
			
			msg = msg.indexOf(lineSeparator) != msg.lastIndexOf(lineSeparator) ? "Multiple errors have occurred:".concat(lineSeparator).concat(msg) : msg;
			msg = msg.endsWith(lineSeparator) ? msg.substring(0, msg.length() - lineSeparator.length()) : msg;
			throw new IndexOutOfBoundsException(msg);
		}
	}
	
	/** Writes the data from the current matrix on the stack into the given
	 * array.
	 * 
	 * @param matrix The array to write to
	 * @param offset The offset within the array to start writing at
	 * @param length The number of doubles that will be written
	 * @return This MatrixStack
	 * @throws IndexOutOfBoundsException Thrown if the specified offset or
	 *             length are less than zero, or (combined) are greater than the
	 *             length of either the supplied array or this MatrixStack's
	 *             current matrix array
	 * @see #peek()
	 * @see #peek(double[]) peek(double[])
	 * @see #peekf()
	 * @see #peekf(float[]) peekf(float[])
	 * @see #peekf(float[], int, int) peekf(float[], int, int)
	 * @see #loadMatrix(double[], int, int) loadMatrix(double[], int, int) */
	public MatrixStack peek(double[] matrix, int offset, int length) throws IndexOutOfBoundsException {
		checkArrayParams(matrix.length, offset, length);
		System.arraycopy(this.stack[this.index], 0, matrix, offset, length);
		return this;
	}
	
	/** Writes the data from the current matrix on the stack into the given
	 * array.
	 * 
	 * @param matrix The array to write to
	 * @return This MatrixStack
	 * @see #peek()
	 * @see #peek(double[], int, int) peek(double[], int, int)
	 * @see #peekf()
	 * @see #peekf(float[]) peekf(float[])
	 * @see #peekf(float[], int, int) peekf(float[], int, int)
	 * @see #loadMatrix(double[]) loadMatrix(double[]) */
	public MatrixStack peek(double[] matrix) {
		return this.peek(matrix, 0, Math.min(MATRIX_ARRAY_LENGTH, matrix.length));
	}
	
	/** Returns a copy of the current matrix without {@link #pop() popping} it
	 * off of the stack.
	 * 
	 * @return A copy of the current matrix on the stack
	 * @see #peek(double[]) peek(double[])
	 * @see #peek(double[], int, int) peek(double[], int, int)
	 * @see #peekf()
	 * @see #peekf(float[]) peekf(float[])
	 * @see #peekf(float[], int, int) peekf(float[], int, int) */
	public double[] peek() {
		double[] matrix = new double[MATRIX_ARRAY_LENGTH];
		this.peek(matrix);
		return matrix;
	}
	
	/** Writes the data from the current matrix on the stack into the given
	 * array.
	 * 
	 * @param matrix The array to write to
	 * @param offset The offset within the array to start writing at
	 * @param length The number of floats that will be written
	 * @return This MatrixStack
	 * @throws IndexOutOfBoundsException Thrown if the specified offset or
	 *             length are less than zero, or (combined) are greater than the
	 *             length of either the supplied array or this MatrixStack's
	 *             current matrix array
	 * @see #peek()
	 * @see #peek(double[]) peek(double[])
	 * @see #peek(double[], int, int) peek(double[], int, int)
	 * @see #peekf()
	 * @see #peekf(float[]) peekf(float[]) */
	public MatrixStack peekf(float[] matrix, int offset, int length) throws IndexOutOfBoundsException {
		checkArrayParams(matrix.length, offset, length);
		//System.arraycopy(this.stack[this.index], 0, matrix, offset, length);
		for(int i = 0; i < length; i++) {
			matrix[i + offset] = (float) this.stack[this.index][i];
		}
		return this;
	}
	
	/** Writes the data from the current matrix on the stack into the given
	 * array.
	 * 
	 * @param matrix The array to write to
	 * @return This MatrixStack
	 * @see #peek()
	 * @see #peek(double[]) peek(double[])
	 * @see #peek(double[], int, int) peek(double[], int, int)
	 * @see #peekf()
	 * @see #peekf(float[], int, int) peekf(float[], int, int) */
	public MatrixStack peekf(float[] matrix) {
		return this.peekf(matrix, 0, Math.min(MATRIX_ARRAY_LENGTH, matrix.length));
	}
	
	/** Returns a copy of the current matrix without {@link #pop() popping} it
	 * off of the stack.
	 * 
	 * @return A copy of the current matrix on the stack
	 * @see #peek()
	 * @see #peek(double[]) peek(double[])
	 * @see #peek(double[], int, int) peek(double[], int, int)
	 * @see #peekf(float[]) peekf(float[])
	 * @see #peekf(float[], int, int) peekf(float[], int, int) */
	public float[] peekf() {
		float[] matrix = new float[MATRIX_ARRAY_LENGTH];
		this.peekf(matrix);
		return matrix;
	}
	
	/** Sets the current matrix on the stack to an array filled with zeros.
	 * 
	 * @return This MatrixStack */
	public MatrixStack clear() {
		this.stack[this.index] = new double[MATRIX_ARRAY_LENGTH];
		return this;
	}
	
	/** Sets the current matrix on the stack to an identity matrix.
	 * 
	 * @return This MatrixStack */
	public MatrixStack loadIdentity() {
		this.stack[this.index] = GLUtil.getIdentityd();
		return this;
	}
	
	/** Transposes the current matrix on the stack.<br>
	 * That is, swaps the values within the matrix across a diagonal line from
	 * the top left to the bottom right, effectively converting it from a matrix
	 * with a column-major order to one with a row-major order, or vice versa.
	 * 
	 * @return This MatrixStack */
	public MatrixStack transpose() {
		this.stack[this.index] = GLUtil.transpose(this.stack[this.index]);
		return this;
	}
	
	/** Writes the data from the given matrix into the current matrix on the
	 * stack.
	 * 
	 * @param matrix The array to copy from
	 * @param offset The offset within the array to start reading from
	 * @param length The number of doubles that will be copied
	 * @return This MatrixStack
	 * @throws IndexOutOfBoundsException Thrown if the specified offset or
	 *             length are less than zero, or (combined) are greater than the
	 *             length of either the supplied array or this MatrixStack's
	 *             current matrix array
	 * @see #peek(double[], int, int) peek(double[], int, int) */
	public MatrixStack loadMatrix(double[] matrix, int offset, int length) throws IndexOutOfBoundsException {
		checkArrayParams(matrix.length, offset, length);
		System.arraycopy(matrix, offset, this.stack[this.index], 0, length);
		return this;
	}
	
	/** Writes the data from the given matrix into the current matrix on the
	 * stack.
	 * 
	 * @param matrix The array to copy from
	 * @return This MatrixStack
	 * @see #peek(double[]) peek(double[]) */
	public MatrixStack loadMatrix(double[] matrix) {
		return this.loadMatrix(matrix, 0, Math.min(16, matrix.length));
	}
	
	/** Writes the data from the given matrix into the current matrix on the
	 * stack.
	 * 
	 * @param matrix The array to copy from
	 * @param offset The offset within the array to start reading from
	 * @param length The number of doubles that will be copied
	 * @return This MatrixStack
	 * @throws IndexOutOfBoundsException Thrown if the specified offset or
	 *             length are less than zero, or (combined) are greater than the
	 *             length of either the supplied array or this MatrixStack's
	 *             current matrix array
	 * @see #peek(double[], int, int) peek(double[], int, int) */
	public MatrixStack loadMatrix(float[] matrix, int offset, int length) throws IndexOutOfBoundsException {
		checkArrayParams(matrix.length, offset, length);
		//System.arraycopy(matrix, offset, this.stack[this.index], 0, length);
		for(int i = 0; i < length; i++) {
			this.stack[this.index][i] = matrix[offset + i];
		}
		return this;
	}
	
	/** Writes the data from the given matrix into the current matrix on the
	 * stack.
	 * 
	 * @param matrix The array to copy from
	 * @return This MatrixStack
	 * @see #peek(double[]) peek(double[]) */
	public MatrixStack loadMatrix(float[] matrix) {
		return this.loadMatrix(matrix, 0, Math.min(16, matrix.length));
	}
	
	/** Multiplies the current matrix in the stack by the given matrix using the
	 * specified multiplication order.
	 * 
	 * @param matrix The matrix to multiply
	 * @param order The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack multMatrix4x4(double[] matrix, MultiplicationOrder order) {
		switch(order) {
		case OLDxNEW:
			this.stack[this.index] = GLUtil.multMatrix4x4d(this.stack[this.index], matrix);
			return this;
		case NEWxOLD:
			this.stack[this.index] = GLUtil.multMatrix4x4d(matrix, this.stack[this.index]);
			return this;
		default:
			throw new UnsupportedOperationException(String.format("Unimplemented MultiplicationOrder type: %s", order.name()));
		}
	}
	
	/** Multiplies the current matrix in the stack by the given matrix.
	 * 
	 * @param matrix The matrix to multiply
	 * @return This MatrixStack */
	public MatrixStack multMatrix4x4(double[] matrix) {
		return this.multMatrix4x4(matrix, MultiplicationOrder.OLDxNEW);
	}
	
	/** Multiplies the current matrix in the stack by the given matrix using the
	 * specified multiplication order.
	 * 
	 * @param matrix The matrix to multiply
	 * @param order The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack multMatrix4x4(float[] matrix, MultiplicationOrder order) {
		return this.multMatrix4x4(wrapToDouble(matrix), order);
	}
	
	/** Multiplies the current matrix in the stack by the given matrix.
	 * 
	 * @param matrix The matrix to multiply
	 * @return This MatrixStack */
	public MatrixStack multMatrix4x4(float[] matrix) {
		return this.multMatrix4x4(matrix, MultiplicationOrder.OLDxNEW);
	}
	
	/** Translates the current matrix in the stack by the given vector.
	 * 
	 * @param xyz The vector to translate the stack by
	 * @param order The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack translate(double[] xyz, MultiplicationOrder order) {
		return this.multMatrix4x4(GLUtil.buildTranslate4x4d(xyz[0], xyz[1], xyz[2]), order);
	}
	
	/** Translates the current matrix in the stack by the given vector.
	 * 
	 * @param xyz The vector to translate the stack by
	 * @return This MatrixStack */
	public MatrixStack translate(double[] xyz) {
		return this.translate(xyz, MultiplicationOrder.OLDxNEW);
	}
	
	/** Translates the current matrix in the stack by the given vector.
	 * 
	 * @param x The amount to translate the stack by along the X axis
	 * @param y The amount to translate the stack by along the Y axis
	 * @param z The amount to translate the stack by along the Z axis
	 * @param order The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack translate(double x, double y, double z, MultiplicationOrder order) {
		return this.multMatrix4x4(GLUtil.buildTranslate4x4d(x, y, z), order);
	}
	
	/** Translates the current matrix in the stack by the given vector.
	 * 
	 * @param x The amount to translate the stack by along the X axis
	 * @param y The amount to translate the stack by along the Y axis
	 * @param z The amount to translate the stack by along the Z axis
	 * @return This MatrixStack */
	public MatrixStack translate(double x, double y, double z) {
		return this.translate(x, y, z, MultiplicationOrder.OLDxNEW);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param yaw The amount to rotate the stack by around the Y axis
	 * @param pitch The amount to rotate the stack by around the X axis
	 * @param roll The amount to rotate the stack by around the Z axis
	 * @param rotOrder The rotation order to use
	 * @param multOrder The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack rotate(double yaw, double pitch, double roll, RotationOrder rotOrder, MultiplicationOrder multOrder) {
		switch(rotOrder) {//@formatter:off
		case ZXY:
			return this
					.multMatrix4x4(GLUtil.buildRotateZ4x4Degd(roll, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateX4x4Degd(pitch, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateY4x4Degd(yaw, 1.0), multOrder);
		case ZYX:
			return this
					.multMatrix4x4(GLUtil.buildRotateZ4x4Degd(roll, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateY4x4Degd(yaw, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateX4x4Degd(pitch, 1.0), multOrder);
		case YXZ:
			return this
					.multMatrix4x4(GLUtil.buildRotateY4x4Degd(yaw, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateX4x4Degd(pitch, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateZ4x4Degd(roll, 1.0), multOrder);
		case YZX:
			return this
					.multMatrix4x4(GLUtil.buildRotateY4x4Degd(yaw, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateZ4x4Degd(roll, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateX4x4Degd(pitch, 1.0), multOrder);
		case XYZ:
			return this
					.multMatrix4x4(GLUtil.buildRotateX4x4Degd(pitch, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateY4x4Degd(yaw, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateZ4x4Degd(roll, 1.0), multOrder);
		case XZY:
			return this
					.multMatrix4x4(GLUtil.buildRotateX4x4Degd(pitch, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateZ4x4Degd(roll, 1.0), multOrder)
					.multMatrix4x4(GLUtil.buildRotateY4x4Degd(yaw, 1.0), multOrder);
		default:
			throw new UnsupportedOperationException(String.format("Unimplemented RotationOrder type: %s", rotOrder.name()));
		}//@formatter:on
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param ypr The array containing the yaw, pitch, and roll to rotate the
	 *            stack by
	 * @param rotOrder The rotation order to use
	 * @param multOrder The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack rotate(double[] ypr, RotationOrder rotOrder, MultiplicationOrder multOrder) {
		return this.rotate(ypr[0], ypr[1], ypr[2], rotOrder, multOrder);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param ypr The array containing the yaw, pitch, and roll to rotate the
	 *            stack by
	 * @param rotOrder The rotation order to use
	 * @return This MatrixStack */
	public MatrixStack rotate(double[] ypr, RotationOrder rotOrder) {
		return this.rotate(ypr, rotOrder, MultiplicationOrder.OLDxNEW);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param yaw The amount to rotate the stack by around the Y axis
	 * @param pitch The amount to rotate the stack by around the X axis
	 * @param roll The amount to rotate the stack by around the Z axis
	 * @param rotOrder The rotation order to use
	 * @return This MatrixStack */
	public MatrixStack rotate(double yaw, double pitch, double roll, RotationOrder rotOrder) {
		return this.rotate(yaw, pitch, roll, rotOrder, MultiplicationOrder.OLDxNEW);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param ypr The array containing the yaw, pitch, and roll to rotate the
	 *            stack by
	 * @param multOrder The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack rotate(double[] ypr, MultiplicationOrder multOrder) {
		return this.rotate(ypr, RotationOrder.YXZ, multOrder);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param yaw The amount to rotate the stack by around the Y axis
	 * @param pitch The amount to rotate the stack by around the X axis
	 * @param roll The amount to rotate the stack by around the Z axis
	 * @param multOrder The multiplication order to use
	 * @return This MatrixStack */
	public MatrixStack rotate(double yaw, double pitch, double roll, MultiplicationOrder multOrder) {
		return this.rotate(yaw, pitch, roll, RotationOrder.YXZ, multOrder);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param ypr The array containing the yaw, pitch, and roll to rotate the
	 *            stack by
	 * @return This MatrixStack */
	public MatrixStack rotate(double[] ypr) {
		return this.rotate(ypr, MultiplicationOrder.OLDxNEW);
	}
	
	/** Rotates the current matrix in the stack by the given angles.
	 * 
	 * @param yaw The amount to rotate the stack by around the Y axis
	 * @param pitch The amount to rotate the stack by around the X axis
	 * @param roll The amount to rotate the stack by around the Z axis
	 * @return This MatrixStack */
	public MatrixStack rotate(double yaw, double pitch, double roll) {
		return this.rotate(yaw, pitch, roll, MultiplicationOrder.OLDxNEW);
	}
	
	/** Sets the current matrix in the stack to a new look-at matrix using
	 * {@link GLUtil#lookAtd(double[], double[], double[])
	 * GLUtil.lookAtd(double[], double[], double[])} with the specified
	 * parameters.
	 * 
	 * @param eye The position of the viewer
	 * @param target The point in space that the viewer will be looking at
	 * @param up The viewer's up vector
	 * @return This MatrixStack */
	public MatrixStack lookAt(double[] eye, double[] target, double[] up) {
		this.stack[this.index] = GLUtil.lookAtd(eye, target, up);
		return this;
	}
	
	public MatrixStack setOrthographic(double x, double y, double width, double height, double zNear, double zFar) {
		this.stack[this.index] = GLUtil.getOrthographicMatrixd(x, y, width, height, zNear, zFar);
		return this;
	}
	
	public MatrixStack setProjection(double fovy, double width, double height, double zNear, double zFar) {
		double aspect = width / height;
		this.stack[this.index] = GLUtil.getPerspectiveMatrixd(fovy, aspect, zNear, zFar);
		return this;
	}
	
	public MatrixStack modelView(double[] xyz, double[] ypr) {
		return this.loadIdentity().rotate(GLUtil.multiply(ypr, -1.0), RotationOrder.ZXY, MultiplicationOrder.NEWxOLD).translate(GLUtil.multiply(xyz, -1.0), MultiplicationOrder.NEWxOLD).transpose();
	}
	
	public MatrixStack modelView(double x, double y, double z, double yaw, double pitch, double roll) {
		return this.loadIdentity().rotate(-yaw, -pitch, -roll, RotationOrder.ZXY, MultiplicationOrder.NEWxOLD).translate(-x, -y, -z, MultiplicationOrder.NEWxOLD);
	}
	
	/** Enum class used to depict the two matrix multiplication orders, as
	 * matrix multiplication is not <a href=
	 * "https://en.wikipedia.org/wiki/Matrix_multiplication#Non-commutativity">communitive</a>.
	 * 
	 * @author Brian_Entei
	 * @see MultiplicationOrder#OLDxNEW
	 * @see MultiplicationOrder#NEWxOLD */
	public static enum MultiplicationOrder {
		/** The default multiplication order, this multiplies the existing
		 * matrix by the new matrix.
		 * 
		 * @see MultiplicationOrder#NEWxOLD */
		OLDxNEW,
		/** The order used by most camera implementations, this multiplies the
		 * new matrix by the existing matrix.
		 * 
		 * @see MultiplicationOrder#OLDxNEW */
		NEWxOLD;
	}
	
	/** Enum class used to depict the various rotation orders used by
	 *
	 * @author Brian_Entei */
	public static enum RotationOrder {
		/** This rotates the existing matrix by pitch, then yaw, then roll. */
		XYZ,
		/** This rotates the existing matrix by pitch, then roll, then yaw. */
		XZY,
		/** The default rotation order, this rotates the existing matrix by yaw,
		 * then pitch, then roll. */
		YXZ,
		/** This rotates the existing matrix by yaw, then roll, then pitch. */
		YZX,
		/** The order used by most camera implementations, this rotates the
		 * existing matrix by roll, then pitch, then yaw. */
		ZXY,
		/** This rotates the existing matrix by roll, then yaw, then pitch. */
		ZYX;
	}
	
}
