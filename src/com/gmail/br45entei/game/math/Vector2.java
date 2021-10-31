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

import org.eclipse.swt.graphics.Point;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

/** @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
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
