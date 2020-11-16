package com.gmail.br45entei.game.math;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector3f;

/** Matrix class used to compute position, scale, rotations, translations, etc.
 *
 * @since 1.0
 * @author theBennyBox
 * @author Brian_Entei */
public final strictfp class Matrix4f {
	private float[][] m;
	
	/** The default constructor */
	public Matrix4f() {
		this.m = new float[4][4];
	}
	
	public Matrix4f(float[] m) {
		if(m.length != 16) {
			throw new IllegalArgumentException("float array length must be 16!");
		}
		this.m = new float[4][4];
		this.m[0][0] = m[0];
		this.m[0][1] = m[1];
		this.m[0][2] = m[2];
		this.m[0][3] = m[3];
		this.m[1][0] = m[4];
		this.m[1][1] = m[5];
		this.m[1][2] = m[6];
		this.m[1][3] = m[7];
		this.m[2][0] = m[8];
		this.m[2][1] = m[9];
		this.m[2][2] = m[10];
		this.m[2][3] = m[11];
		this.m[3][0] = m[12];
		this.m[3][1] = m[13];
		this.m[3][2] = m[14];
		this.m[3][3] = m[15];
	}
	
	/** @return This instantiation */
	public Matrix4f initIdentity() {
		this.m[0][0] = 1;
		this.m[0][1] = 0;
		this.m[0][2] = 0;
		this.m[0][3] = 0;
		
		this.m[1][0] = 0;
		this.m[1][1] = 1;
		this.m[1][2] = 0;
		this.m[1][3] = 0;
		
		this.m[2][0] = 0;
		this.m[2][1] = 0;
		this.m[2][2] = 1;
		this.m[2][3] = 0;
		
		this.m[3][0] = 0;
		this.m[3][1] = 0;
		this.m[3][2] = 0;
		this.m[3][3] = 1;
		return this;
	}
	
	/** @param pos The vector to translate by
	 * @return This instantiation, translated by the given vectors values */
	public Matrix4f initTranslation(Vector3f pos) {
		return this.initTranslation(pos.getX(), pos.getY(), pos.getZ());
	}
	
	/** @param x The x value
	 * @param y The y value
	 * @param z The z value
	 * @return This instantiation, translated by the given values */
	public Matrix4f initTranslation(float x, float y, float z) {
		this.m[0][0] = 1;
		this.m[0][1] = 0;
		this.m[0][2] = 0;
		this.m[0][3] = x;
		this.m[1][0] = 0;
		this.m[1][1] = 1;
		this.m[1][2] = 0;
		this.m[1][3] = y;
		this.m[2][0] = 0;
		this.m[2][1] = 0;
		this.m[2][2] = 1;
		this.m[2][3] = z;
		this.m[3][0] = 0;
		this.m[3][1] = 0;
		this.m[3][2] = 0;
		this.m[3][3] = 1;
		return this;
	}
	
	/** @param x The x value
	 * @param y The y value
	 * @param z The z value
	 * @return This instantiation, rotated by the given values */
	public Matrix4f initRotation(float x, float y, float z) {
		Matrix4f rx = new Matrix4f();
		Matrix4f ry = new Matrix4f();
		Matrix4f rz = new Matrix4f();
		
		x = (float) Math.toRadians(x);
		y = (float) Math.toRadians(y);
		z = (float) Math.toRadians(z);
		
		rz.m[0][0] = (float) Math.cos(z);
		rz.m[0][1] = -(float) Math.sin(z);
		rz.m[0][2] = 0;
		rz.m[0][3] = 0;
		rz.m[1][0] = (float) Math.sin(z);
		rz.m[1][1] = (float) Math.cos(z);
		rz.m[1][2] = 0;
		rz.m[1][3] = 0;
		rz.m[2][0] = 0;
		rz.m[2][1] = 0;
		rz.m[2][2] = 1;
		rz.m[2][3] = 0;
		rz.m[3][0] = 0;
		rz.m[3][1] = 0;
		rz.m[3][2] = 0;
		rz.m[3][3] = 1;
		
		rx.m[0][0] = 1;
		rx.m[0][1] = 0;
		rx.m[0][2] = 0;
		rx.m[0][3] = 0;
		rx.m[1][0] = 0;
		rx.m[1][1] = (float) Math.cos(x);
		rx.m[1][2] = -(float) Math.sin(x);
		rx.m[1][3] = 0;
		rx.m[2][0] = 0;
		rx.m[2][1] = (float) Math.sin(x);
		rx.m[2][2] = (float) Math.cos(x);
		rx.m[2][3] = 0;
		rx.m[3][0] = 0;
		rx.m[3][1] = 0;
		rx.m[3][2] = 0;
		rx.m[3][3] = 1;
		
		ry.m[0][0] = (float) Math.cos(y);
		ry.m[0][1] = 0;
		ry.m[0][2] = -(float) Math.sin(y);
		ry.m[0][3] = 0;
		ry.m[1][0] = 0;
		ry.m[1][1] = 1;
		ry.m[1][2] = 0;
		ry.m[1][3] = 0;
		ry.m[2][0] = (float) Math.sin(y);
		ry.m[2][1] = 0;
		ry.m[2][2] = (float) Math.cos(y);
		ry.m[2][3] = 0;
		ry.m[3][0] = 0;
		ry.m[3][1] = 0;
		ry.m[3][2] = 0;
		ry.m[3][3] = 1;
		
		this.m = rz.mul(ry.mul(rx)).getM();
		return this;
	}
	
	/** @param x The x value
	 * @param y The y value
	 * @param z The z value
	 * @return This instantiation, scaled by the given values */
	public Matrix4f initScale(float x, float y, float z) {
		this.m[0][0] = x;
		this.m[0][1] = 0;
		this.m[0][2] = 0;
		this.m[0][3] = 0;
		this.m[1][0] = 0;
		this.m[1][1] = y;
		this.m[1][2] = 0;
		this.m[1][3] = 0;
		this.m[2][0] = 0;
		this.m[2][1] = 0;
		this.m[2][2] = z;
		this.m[2][3] = 0;
		this.m[3][0] = 0;
		this.m[3][1] = 0;
		this.m[3][2] = 0;
		this.m[3][3] = 1;
		
		return this;
	}
	
	/** @param fov The field of view value
	 * @param aspectRatio The aspect ratio value
	 * @param zNear The nearest z value
	 * @param zFar The farthest z value
	 * @return This instantiation with a new perspective generated from the
	 *         given values */
	public Matrix4f initPerspective(float fov, float aspectRatio, float zNear, float zFar) {
		float tanHalfFOV = (float) Math.tan(fov / 2);
		float zRange = zNear - zFar;
		
		this.m[0][0] = 1.0f / (tanHalfFOV * aspectRatio);
		this.m[0][1] = 0;
		this.m[0][2] = 0;
		this.m[0][3] = 0;
		this.m[1][0] = 0;
		this.m[1][1] = 1.0f / tanHalfFOV;
		this.m[1][2] = 0;
		this.m[1][3] = 0;
		this.m[2][0] = 0;
		this.m[2][1] = 0;
		this.m[2][2] = (-zNear - zFar) / zRange;
		this.m[2][3] = 2 * zFar * zNear / zRange;
		this.m[3][0] = 0;
		this.m[3][1] = 0;
		this.m[3][2] = 1;
		this.m[3][3] = 0;
		
		return this;
	}
	
	/** @param left The left value
	 * @param right The right value
	 * @param bottom The bottom value
	 * @param top The top value
	 * @param near The nearest (z) value
	 * @param far The farthest (z) value
	 * @return This instantiation with a new orthographic projection generated
	 *         from the given values */
	public Matrix4f initOrthographic(float left, float right, float bottom, float top, float near, float far) {
		float width = right - left;
		float height = top - bottom;
		float depth = far - near;
		
		this.m[0][0] = 2 / width;
		this.m[0][1] = 0;
		this.m[0][2] = 0;
		this.m[0][3] = -(right + left) / width;
		this.m[1][0] = 0;
		this.m[1][1] = 2 / height;
		this.m[1][2] = 0;
		this.m[1][3] = -(top + bottom) / height;
		this.m[2][0] = 0;
		this.m[2][1] = 0;
		this.m[2][2] = -2 / depth;
		this.m[2][3] = -(far + near) / depth;
		this.m[3][0] = 0;
		this.m[3][1] = 0;
		this.m[3][2] = 0;
		this.m[3][3] = 1;
		return this;
	}
	
	/** @param forward The forward vector
	 * @param up The up vector
	 * @return This instantiation, rotated by the given values */
	public Matrix4f initRotation(Vector3f forward, Vector3f up) {
		Vector3f f = MathUtil.normalize(forward);
		Vector3f r = MathUtil.normalize(up);
		r = MathUtil.cross(r, f);//r = r.cross(f);
		Vector3f u = MathUtil.cross(f, r);//Vector3f u = f.cross(r);
		return initRotation(f, u, r);
	}
	
	/** @param forward The forward vector
	 * @param up The up vector
	 * @param right The right vector
	 * @return This instantiation, rotated by the given values */
	public Matrix4f initRotation(Vector3f forward, Vector3f up, Vector3f right) {
		Vector3f f = forward;
		Vector3f r = right;
		Vector3f u = up;
		
		this.m[0][0] = r.getX();
		this.m[0][1] = r.getY();
		this.m[0][2] = r.getZ();
		this.m[0][3] = 0;
		this.m[1][0] = u.getX();
		this.m[1][1] = u.getY();
		this.m[1][2] = u.getZ();
		this.m[1][3] = 0;
		this.m[2][0] = f.getX();
		this.m[2][1] = f.getY();
		this.m[2][2] = f.getZ();
		this.m[2][3] = 0;
		this.m[3][0] = 0;
		this.m[3][1] = 0;
		this.m[3][2] = 0;
		this.m[3][3] = 1;
		
		return this;
	}
	
	public Matrix4f getInverse() {
		final float[] inv = new float[16];
		float det;
		int i;
		final float m0 = this.m[0][0];
		final float m1 = this.m[0][1];
		final float m2 = this.m[0][2];
		final float m3 = this.m[0][3];
		final float m4 = this.m[1][0];
		final float m5 = this.m[1][1];
		final float m6 = this.m[1][2];
		final float m7 = this.m[1][3];
		final float m8 = this.m[2][0];
		final float m9 = this.m[2][1];
		final float m10 = this.m[2][2];
		final float m11 = this.m[2][3];
		final float m12 = this.m[3][0];
		final float m13 = this.m[3][1];
		final float m14 = this.m[3][2];
		final float m15 = this.m[3][3];
		
		inv[0] = m5 * m10 * m15 - m5 * m11 * m14 - m9 * m6 * m15 + m9 * m7 * m14 + m13 * m6 * m11 - m13 * m7 * m10;
		
		inv[4] = -m4 * m10 * m15 + m4 * m11 * m14 + m8 * m6 * m15 - m8 * m7 * m14 - m12 * m6 * m11 + m12 * m7 * m10;
		
		inv[8] = m4 * m9 * m15 - m4 * m11 * m13 - m8 * m5 * m15 + m8 * m7 * m13 + m12 * m5 * m11 - m12 * m7 * m9;
		
		inv[12] = -m4 * m9 * m14 + m4 * m10 * m13 + m8 * m5 * m14 - m8 * m6 * m13 - m12 * m5 * m10 + m12 * m6 * m9;
		
		inv[1] = -m1 * m10 * m15 + m1 * m11 * m14 + m9 * m2 * m15 - m9 * m3 * m14 - m13 * m2 * m11 + m13 * m3 * m10;
		
		inv[5] = m0 * m10 * m15 - m0 * m11 * m14 - m8 * m2 * m15 + m8 * m3 * m14 + m12 * m2 * m11 - m12 * m3 * m10;
		
		inv[9] = -m0 * m9 * m15 + m0 * m11 * m13 + m8 * m1 * m15 - m8 * m3 * m13 - m12 * m1 * m11 + m12 * m3 * m9;
		
		inv[13] = m0 * m9 * m14 - m0 * m10 * m13 - m8 * m1 * m14 + m8 * m2 * m13 + m12 * m1 * m10 - m12 * m2 * m9;
		
		inv[2] = m1 * m6 * m15 - m1 * m7 * m14 - m5 * m2 * m15 + m5 * m3 * m14 + m13 * m2 * m7 - m13 * m3 * m6;
		
		inv[6] = -m0 * m6 * m15 + m0 * m7 * m14 + m4 * m2 * m15 - m4 * m3 * m14 - m12 * m2 * m7 + m12 * m3 * m6;
		
		inv[10] = m0 * m5 * m15 - m0 * m7 * m13 - m4 * m1 * m15 + m4 * m3 * m13 + m12 * m1 * m7 - m12 * m3 * m5;
		
		inv[14] = -m0 * m5 * m14 + m0 * m6 * m13 + m4 * m1 * m14 - m4 * m2 * m13 - m12 * m1 * m6 + m12 * m2 * m5;
		
		inv[3] = -m1 * m6 * m11 + m1 * m7 * m10 + m5 * m2 * m11 - m5 * m3 * m10 - m9 * m2 * m7 + m9 * m3 * m6;
		
		inv[7] = m0 * m6 * m11 - m0 * m7 * m10 - m4 * m2 * m11 + m4 * m3 * m10 + m8 * m2 * m7 - m8 * m3 * m6;
		
		inv[11] = -m0 * m5 * m11 + m0 * m7 * m9 + m4 * m1 * m11 - m4 * m3 * m9 - m8 * m1 * m7 + m8 * m3 * m5;
		
		inv[15] = m0 * m5 * m10 - m0 * m6 * m9 - m4 * m1 * m10 + m4 * m2 * m9 + m8 * m1 * m6 - m8 * m2 * m5;
		
		det = m0 * inv[0] + m1 * inv[4] + m2 * inv[8] + m3 * inv[12];
		
		if(det == 0) {
			return null;
		}
		
		det = 1.0F / det;
		
		float[] invOut = new float[16];
		for(i = 0; i < 16; i++) {
			invOut[i] = inv[i] * det;
		}
		return new Matrix4f(invOut);
	}
	
	/** @param r The vector to multiply by(use a vector containing three 1.0F
	 *            values to return a 'default' value)
	 * @return The requested transform vector */
	public Vector3f transform(Vector3f r) {
		return new Vector3f(this.m[0][0] * r.getX() + this.m[0][1] * r.getY() + this.m[0][2] * r.getZ() + this.m[0][3], this.m[1][0] * r.getX() + this.m[1][1] * r.getY() + this.m[1][2] * r.getZ() + this.m[1][3], this.m[2][0] * r.getX() + this.m[2][1] * r.getY() + this.m[2][2] * r.getZ() + this.m[2][3]);
	}
	
	/** @param r The matrix to multiply by
	 * @return A new matrix whose values are a product of this instantiation and
	 *         the given matrix */
	public Matrix4f mul(Matrix4f r) {
		Matrix4f res = new Matrix4f();
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				res.set(i, j, this.m[i][0] * r.get(0, j) + this.m[i][1] * r.get(1, j) + this.m[i][2] * r.get(2, j) + this.m[i][3] * r.get(3, j));
			}
		}
		
		return res;
	}
	
	public static final double[] mul(double[] m1, double[] m2) {
		double[] r = new double[16];
		r[0] = m1[0] * m2[0] + m1[1] * m2[4] + m1[2] * m2[8] + m1[3] * m2[12];
		r[1] = m1[0] * m2[1] + m1[1] * m2[5] + m1[2] * m2[9] + m1[3] * m2[13];
		r[2] = m1[0] * m2[2] + m1[1] * m2[6] + m1[2] * m2[10] + m1[3] * m2[14];
		r[3] = m1[0] * m2[3] + m1[1] * m2[7] + m1[2] * m2[11] + m1[3] * m2[15];
		
		r[4] = m1[4] * m2[0] + m1[5] * m2[4] + m1[6] * m2[8] + m1[7] * m2[12];
		r[5] = m1[4] * m2[1] + m1[5] * m2[5] + m1[6] * m2[9] + m1[7] * m2[13];
		r[6] = m1[4] * m2[2] + m1[5] * m2[6] + m1[6] * m2[10] + m1[7] * m2[14];
		r[7] = m1[4] * m2[3] + m1[5] * m2[7] + m1[6] * m2[11] + m1[7] * m2[15];
		
		r[8] = m1[8] * m2[0] + m1[9] * m2[4] + m1[10] * m2[8] + m1[11] * m2[12];
		r[9] = m1[8] * m2[1] + m1[9] * m2[5] + m1[10] * m2[9] + m1[11] * m2[13];
		r[10] = m1[8] * m2[2] + m1[9] * m2[6] + m1[10] * m2[10] + m1[11] * m2[14];
		r[11] = m1[8] * m2[3] + m1[9] * m2[7] + m1[10] * m2[11] + m1[11] * m2[15];
		
		r[12] = m1[12] * m2[0] + m1[13] * m2[4] + m1[14] * m2[8] + m1[15] * m2[12];
		r[13] = m1[12] * m2[1] + m1[13] * m2[5] + m1[14] * m2[9] + m1[15] * m2[13];
		r[14] = m1[12] * m2[2] + m1[13] * m2[6] + m1[14] * m2[10] + m1[15] * m2[14];
		r[15] = m1[12] * m2[3] + m1[13] * m2[7] + m1[14] * m2[11] + m1[15] * m2[15];
		return r;
	}
	
	/** @param m The float buffer whose values will be set to this matrix's
	 *            values */
	public final void getMBuffer(FloatBuffer m) {
		if(m == null) {
			return;
		}
		m.limit(16);
		m.clear();
		int index = 0;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				m.put(index, this.m[i][j]);
				index++;
			}
		}
		m.flip();
	}
	
	/** @return This instantiations matrix values */
	public float[][] getM() {
		float[][] res = new float[4][4];
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				res[i][j] = this.m[i][j];
		return res;
	}
	
	/** @param x The x index to get by
	 * @param y The y index to get by
	 * @return The requested value at the index specified by the given
	 *         values(may return null or throw a {@link NullPointerException} if
	 *         the given index values are out of range) */
	public float get(int x, int y) {
		return this.m[x][y];
	}
	
	/** @param m The new matrix values to set */
	public void setM(float[][] m) {
		this.m = m;
	}
	
	/** @param x The x index to set by
	 * @param y The y index to set by
	 * @param value The value to set(may return null or throw a
	 *            {@link NullPointerException} if
	 *            the given index values are out of range) */
	public void set(int x, int y, float value) {
		this.m[x][y] = value;
	}
}
