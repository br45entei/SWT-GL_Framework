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

import org.eclipse.swt.graphics.Point;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

/** @author Brian_Entei */
public class Vector2 {
	
	private volatile int x;
	private volatile int y;
	
	/** Default constructor */
	public Vector2() {
		this(0, 0);
	}
	
	/** @param x The x
	 * @param y The y */
	public Vector2(int x, int y) {
		this.setX(x);
		this.setY(y);
	}
	
	/** @param vectorToCopy The vector to copy */
	public Vector2(Point vectorToCopy) {
		this.set(vectorToCopy.x, vectorToCopy.y);
	}
	
	/** @param vectorToCopy The vector to copy */
	public Vector2(java.awt.Point vectorToCopy) {
		this.set(vectorToCopy.x, vectorToCopy.y);
	}
	
	/** @param vectorToCopy The vector to copy */
	public Vector2(Vector2 vectorToCopy) {
		this.x = vectorToCopy.x;
		this.y = vectorToCopy.y;
	}
	
	public Vector2(ReadableVector2f vectorToCopy) {
		this.x = Math.round(vectorToCopy.getX());
		this.y = Math.round(vectorToCopy.getY());
	}
	
	public final Vector2f asVector2f() {
		return new Vector2f(this.x, this.y);
	}
	
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
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
		if(!(obj instanceof Vector2)) {
			return false;
		}
		Vector2 other = (Vector2) obj;
		if(this.x != other.x) {
			return false;
		}
		if(this.y != other.y) {
			return false;
		}
		return true;
	}
	
	/** @return The x */
	public int getX() {
		return this.x;
	}
	
	/** @param x The x to set */
	public void setX(int x) {
		this.x = x;
	}
	
	/** @return The y */
	public int getY() {
		return this.y;
	}
	
	/** @param y The y to set */
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public final String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
	
	/** @param x The x to set
	 * @param y The y to set */
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(Vector2 vectorToCopy) {
		this.x = vectorToCopy.x;
		this.y = vectorToCopy.y;
	}
	
	public void set(Point vectorToCopy) {
		this.x = vectorToCopy.x;
		this.y = vectorToCopy.y;
	}
	
	public void set(java.awt.Point vectorToCopy) {
		this.x = vectorToCopy.x;
		this.y = vectorToCopy.y;
	}
	
	/** @param vector The vector to subtract
	 * @return A new vector representing the difference between this vector and
	 *         the given vector */
	public Vector2 sub(Vector2 vector) {
		return new Vector2(this.getX() - vector.getX(), this.getY() - vector.getY());
	}
	
}
