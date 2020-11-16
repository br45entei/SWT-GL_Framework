/*******************************************************************************
 * 
 * Copyright © 2020 Brian_Entei (br45entei@gmail.com)
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
package com.gmail.br45entei.game.math;

import org.lwjgl.util.vector.ReadableVector2f;

/** @author Brian_Entei */
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
	
	public Vector2d(ReadableVector2f vectorToCopy) {
		this.x = Math.round(vectorToCopy.getX());
		this.y = Math.round(vectorToCopy.getY());
	}
	
	/** @see java.lang.Object#hashCode() */
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
	
	/** @see java.lang.Object#equals(java.lang.Object) */
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
