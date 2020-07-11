package com.gmail.br45entei.game.math;

import com.gmail.br45entei.util.MathUtil;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/** @author theBennyBox
 * @author Brian_Entei */
@SuppressWarnings("javadoc")
public final strictfp class Quaternion {
	private float x;
	private float y;
	private float z;
	private float w;
	
	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + "," + this.w + ")";
	}
	
	/** The default constructor
	 * 
	 * @param x The x value to set
	 * @param y The y value to set
	 * @param z The z value to set
	 * @param w The w value to set */
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	/** The default constructor
	 * 
	 * @param axis The axis to use
	 * @param angle The angle to use */
	public Quaternion(Vector3f axis, float angle) {
		float sinHalfAngle = (float) Math.sin(angle / 2);
		float cosHalfAngle = (float) Math.cos(angle / 2);
		
		this.x = axis.getX() * sinHalfAngle;
		this.y = axis.getY() * sinHalfAngle;
		this.z = axis.getZ() * sinHalfAngle;
		this.w = cosHalfAngle;
	}
	
	/** @return The length of this Quaternion */
	public float length() {
		return (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w));
	}
	
	/** @return The normalized version of this Quaternion */
	public Quaternion normalize() {
		float length = this.length();
		
		return new Quaternion(this.x / length, this.y / length, this.z / length, this.w / length);
	}
	
	/** @return The conjugate of this Quaternion */
	public Quaternion conjugate() {
		return new Quaternion(-this.x, -this.y, -this.z, this.w);
	}
	
	/** @param r The value to multiply by
	 * @return The resulting Quaternion */
	public Quaternion mul(float r) {
		return new Quaternion(this.x * r, this.y * r, this.z * r, this.w * r);
	}
	
	/** @param r The Quaternion to multiply by
	 * @return The product of the given Quaternion and this one */
	public Quaternion mul(Quaternion r) {
		float w_ = (this.w * r.getW()) - (this.x * r.getX()) - (this.y * r.getY()) - (this.z * r.getZ());
		float x_ = ((this.x * r.getW()) + (this.w * r.getX()) + (this.y * r.getZ())) - (this.z * r.getY());
		float y_ = ((this.y * r.getW()) + (this.w * r.getY()) + (this.z * r.getX())) - (this.x * r.getZ());
		float z_ = ((this.z * r.getW()) + (this.w * r.getZ()) + (this.x * r.getY())) - (this.y * r.getX());
		return new Quaternion(x_, y_, z_, w_);
	}
	
	/** @param r The Vector4f to multiply by
	 * @return The product of the given Vector4f and this Quaternion */
	public Quaternion mul(Vector4f r) {
		float w_ = (this.w * r.getW()) - (this.x * r.getX()) - (this.y * r.getY()) - (this.z * r.getZ());
		float x_ = ((this.x * r.getW()) + (this.w * r.getX()) + (this.y * r.getZ())) - (this.z * r.getY());
		float y_ = ((this.y * r.getW()) + (this.w * r.getY()) + (this.z * r.getX())) - (this.x * r.getZ());
		float z_ = ((this.z * r.getW()) + (this.w * r.getZ()) + (this.x * r.getY())) - (this.y * r.getX());
		return new Quaternion(x_, y_, z_, w_);
	}
	
	/** @param r
	 * @return */
	public Quaternion mul(Vector3f r) {
		float w_ = (-this.x * r.getX()) - (this.y * r.getY()) - (this.z * r.getZ());
		float x_ = ((this.w * r.getX()) + (this.y * r.getZ())) - (this.z * r.getY());
		float y_ = ((this.w * r.getY()) + (this.z * r.getX())) - (this.x * r.getZ());
		float z_ = ((this.w * r.getZ()) + (this.x * r.getY())) - (this.y * r.getX());
		return new Quaternion(x_, y_, z_, w_);
	}
	
	/** @return a new Quaternion whose value is the inverse of this */
	public Quaternion inverse() {
		float d = (this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w);
		return new Quaternion(-this.x / d, -this.y / d, -this.z / d, this.w / d);
	}
	
	/** @param r
	 * @return */
	public Quaternion div(Quaternion r) {
		return this.mul(r.inverse());
	}
	
	/** @param r
	 * @return */
	public Quaternion sub(Quaternion r) {
		return new Quaternion(this.x - r.getX(), this.y - r.getY(), this.z - r.getZ(), this.w - r.getW());
	}
	
	/** @param r
	 * @return */
	public Quaternion add(Quaternion r) {
		return new Quaternion(this.x + r.getX(), this.y + r.getY(), this.z + r.getZ(), this.w + r.getW());
	}
	
	/** @return */
	public Matrix4f toRotationMatrix() {
		Vector3f forward = new Vector3f(2.0f * ((this.x * this.z) - (this.w * this.y)), 2.0f * ((this.y * this.z) + (this.w * this.x)), 1.0f - (2.0f * ((this.x * this.x) + (this.y * this.y))));
		Vector3f up = new Vector3f(2.0f * ((this.x * this.y) + (this.w * this.z)), 1.0f - (2.0f * ((this.x * this.x) + (this.z * this.z))), 2.0f * ((this.y * this.z) - (this.w * this.x)));
		Vector3f right = new Vector3f(1.0f - (2.0f * ((this.y * this.y) + (this.z * this.z))), 2.0f * ((this.x * this.y) - (this.w * this.z)), 2.0f * ((this.x * this.z) + (this.w * this.y)));
		
		return new Matrix4f().initRotation(forward, up, right);
	}
	
	/** @param r
	 * @return */
	public float dot(Quaternion r) {
		return (this.x * r.getX()) + (this.y * r.getY()) + (this.z * r.getZ()) + (this.w * r.getW());
	}
	
	/** @param dest The quaternion to use as the destination in this lerp
	 *            operation
	 * @param lerpFactor The 0 to 1 decimal value to use in this lerp operation
	 * @param shortest Whether or not to calculate the shortest value
	 * @return A new quaternion with lerped values calculated by the given
	 *         values */
	public Quaternion nlerp(Quaternion dest, float lerpFactor, boolean shortest) {
		Quaternion correctedDest = dest;
		
		if(shortest && (this.dot(dest) < 0)) {
			correctedDest = new Quaternion(-dest.getX(), -dest.getY(), -dest.getZ(), -dest.getW());
		}
		
		return correctedDest.sub(this).mul(lerpFactor).add(this).normalize();
	}
	
	/** Longer version of {@link Quaternion#nlerp(Quaternion, float, boolean)}
	 * that uses trigonometry
	 * 
	 * @param dest The quaternion to use as the destination in this lerp
	 *            operation
	 * @param lerpFactor The 0 to 1 decimal value to use in this lerp operation
	 * @param shortest Whether or not to calculate the shortest value
	 * @return A new quaternion with lerped values calculated by the given
	 *         values */
	public Quaternion slerp(Quaternion dest, float lerpFactor, boolean shortest) {
		final float EPSILON = 1e3f;
		
		float cos = this.dot(dest);
		Quaternion correctedDest = dest;
		
		if(shortest && (cos < 0)) {
			cos = -cos;
			correctedDest = new Quaternion(-dest.getX(), -dest.getY(), -dest.getZ(), -dest.getW());
		}
		
		if(Math.abs(cos) >= (1 - EPSILON)) {
			return this.nlerp(correctedDest, lerpFactor, false);
		}
		
		float sin = (float) Math.sqrt(1.0f - (cos * cos));
		float angle = (float) Math.atan2(sin, cos);
		float invSin = 1.0f / sin;
		
		float srcFactor = (float) Math.sin((1.0f - lerpFactor) * angle) * invSin;
		float destFactor = (float) Math.sin((lerpFactor) * angle) * invSin;
		
		return this.mul(srcFactor).add(correctedDest.mul(destFactor));
	}
	
	/** From Ken Shoemake's "Quaternion Calculus and Fast Animation" article
	 * 
	 * @param rot The matrix to use */
	public Quaternion(Matrix4f rot) {
		if(rot == null) {
			this.w = 0;
			this.x = 0;
			this.y = 0;
			this.z = 0;
			return;
		}
		float trace = rot.get(0, 0) + rot.get(1, 1) + rot.get(2, 2);
		
		if(trace > 0) {
			float s = 0.5f / (float) Math.sqrt(trace + 1.0f);
			this.w = 0.25f / s;
			this.x = (rot.get(1, 2) - rot.get(2, 1)) * s;
			this.y = (rot.get(2, 0) - rot.get(0, 2)) * s;
			this.z = (rot.get(0, 1) - rot.get(1, 0)) * s;
		} else {
			if((rot.get(0, 0) > rot.get(1, 1)) && (rot.get(0, 0) > rot.get(2, 2))) {
				float s = 2.0f * (float) Math.sqrt((1.0f + rot.get(0, 0)) - rot.get(1, 1) - rot.get(2, 2));
				this.w = (rot.get(1, 2) - rot.get(2, 1)) / s;
				this.x = 0.25f * s;
				this.y = (rot.get(1, 0) + rot.get(0, 1)) / s;
				this.z = (rot.get(2, 0) + rot.get(0, 2)) / s;
			} else if(rot.get(1, 1) > rot.get(2, 2)) {
				float s = 2.0f * (float) Math.sqrt((1.0f + rot.get(1, 1)) - rot.get(0, 0) - rot.get(2, 2));
				this.w = (rot.get(2, 0) - rot.get(0, 2)) / s;
				this.x = (rot.get(1, 0) + rot.get(0, 1)) / s;
				this.y = 0.25f * s;
				this.z = (rot.get(2, 1) + rot.get(1, 2)) / s;
			} else {
				float s = 2.0f * (float) Math.sqrt((1.0f + rot.get(2, 2)) - rot.get(0, 0) - rot.get(1, 1));
				this.w = (rot.get(0, 1) - rot.get(1, 0)) / s;
				this.x = (rot.get(2, 0) + rot.get(0, 2)) / s;
				this.y = (rot.get(1, 2) + rot.get(2, 1)) / s;
				this.z = 0.25f * s;
			}
		}
		
		float length = (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w));
		this.x /= length;
		this.y /= length;
		this.z /= length;
		this.w /= length;
	}
	
	/** @return This instantiation's forward vector */
	public Vector3f getForward() {
		return MathUtil.rotate(new Vector3f(0, 0, 1), this);
	}
	
	/** @return This instantiation's backward vector */
	public Vector3f getBack() {
		return MathUtil.rotate(new Vector3f(0, 0, -1), this);
	}
	
	/** @return This instantiation's up vector */
	public Vector3f getUp() {
		return MathUtil.rotate(new Vector3f(0, 1, 0), this);
	}
	
	/** @return This instantiation's down vector */
	public Vector3f getDown() {
		return MathUtil.rotate(new Vector3f(0, -1, 0), this);
	}
	
	/** @return This instantiation's right vector */
	public Vector3f getRight() {
		return MathUtil.rotate(new Vector3f(1, 0, 0), this);
	}
	
	/** @return This instantiation's left vector */
	public Vector3f getLeft() {
		return MathUtil.rotate(new Vector3f(-1, 0, 0), this);
	}
	
	/** @return This instantiation's euler angles */
	public Vector3f toEulerAngles() {
		float yaw = (float) (Math.atan((2 * ((this.getX() * this.getY()) + (this.getW() * this.getZ()))) / (((this.getW() * this.getW()) + (this.getX() * this.getX())) - (this.getY() * this.getY()) - (this.getZ() * this.getZ()))));
		float pitch = (float) (Math.asin(-2 * ((this.getX() * this.getZ()) - (this.getW() * this.getY()))));
		float roll = (float) (Math.atan((2 * ((this.getW() * this.getX()) + (this.getY() * this.getZ()))) / (((this.getW() * this.getW()) - (this.getX() * this.getX()) - (this.getY() * this.getY())) + (this.getZ() * this.getZ()))));
		return new Vector3f(yaw, pitch, roll);
	}
	
	/** @param x The x value to set
	 * @param y The y value to set
	 * @param z The z value to set
	 * @param w The w value to set
	 * @return This instantiation with the new values set */
	public Quaternion set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	/** @param r The quaternion to copy
	 * @return This instantiation with the given quaternion's values */
	public Quaternion set(Quaternion r) {
		this.set(r.getX(), r.getY(), r.getZ(), r.getW());
		return this;
	}
	
	/** @return This instantiation's x value */
	public float getX() {
		return this.x;
	}
	
	/** @param x The x value to set */
	public void setX(float x) {
		this.x = x;
	}
	
	/** @return This instantiation's y value */
	public float getY() {
		return this.y;
	}
	
	/** @param y The y value to set */
	public void setY(float y) {
		this.y = y;
	}
	
	/** @return This instantiation's z value */
	public float getZ() {
		return this.z;
	}
	
	/** @param z The z value to set */
	public void setZ(float z) {
		this.z = z;
	}
	
	/** @return This instantiation's w value */
	public float getW() {
		return this.w;
	}
	
	/** @param w The w value to set */
	public void setW(float w) {
		this.w = w;
	}
	
	/** @param r The quaternion to check
	 * @return True if the given quaternion's values all equal this
	 *         instantiation's values, false otherwise */
	public boolean equals(Quaternion r) {
		return (this.x == r.getX()) && (this.y == r.getY()) && (this.z == r.getZ()) && (this.w == r.getW());
	}
	
}
