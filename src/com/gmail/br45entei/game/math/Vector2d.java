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
package com.gmail.br45entei.game.math;

import org.lwjgl.util.vector.ReadableVector2f;

/** @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class Vector2d {
	
	private double x;
	private double y;
	
	/** Default constructor */
	public Vector2d() {
		this(0, 0);
	}
	
	/** @param x The x
	 * @param y The y */
	public Vector2d(double x, double y) {
		this.setX(x);
		this.setY(y);
	}
	
	/** @param vectorToCopy The vector to copy */
	public Vector2d(Vector2d vectorToCopy) {
		this.x = vectorToCopy.x;
		this.y = vectorToCopy.y;
	}
	
	/** @param vectorToCopy The vector to copy */
	public Vector2d(ReadableVector2f vectorToCopy) {
		this.x = Math.round(vectorToCopy.getX());
		this.y = Math.round(vectorToCopy.getY());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Vector2d)) {
			return false;
		}
		Vector2d other = (Vector2d) obj;
		if(Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if(Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}
	
	/** @return The x */
	public double getX() {
		return this.x;
	}
	
	/** @param x The x to set */
	public void setX(double x) {
		this.x = x;
	}
	
	/** @return The y */
	public double getY() {
		return this.y;
	}
	
	/** @param y The y to set */
	public void setY(double y) {
		this.y = y;
	}
	
	@Override
	public final String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
	
	/** Returns a String representation of this Vector2d with extra dividends on each component.
	 *
	 * @param xMax The x dividend
	 * @param yMax The y dividend
	 * @return This Vector2d as a String */
	public final String toString(int xMax, int yMax) {
		return "(" + this.x + "/" + xMax + ", " + this.y + "/" + yMax + ")";
	}
	
	/** @param x The x to set
	 * @param y The y to set */
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/** @param vector The vector to subtract
	 * @return A new vector representing the difference between this vector and
	 *         the given vector */
	public Vector2d sub(Vector2d vector) {
		return new Vector2d(this.getX() - vector.getX(), this.getY() - vector.getY());
	}
	
	public Vector2d mul(int x, int y) {
		return new Vector2d(this.x * x, this.y * y);
	}
	
	public Vector2d round(int radix) {
		return new Vector2d(MathUtil.roundTo(this.x, radix), MathUtil.roundTo(this.y, radix));
	}
	
}
