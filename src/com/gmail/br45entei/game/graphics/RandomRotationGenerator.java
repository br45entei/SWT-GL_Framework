/*******************************************************************************
 * 
 * Copyright © 2022 Brian_Entei (br45entei@gmail.com)
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

import com.gmail.br45entei.thread.SecureRandomProvider;

import java.security.SecureRandom;

/** Class used to provide a random color that changes a little bit each time it
 * is requested.
 *
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class RandomRotationGenerator {
	
	private final SecureRandom random;
	private volatile float maxIncrement = 1.2f;
	private volatile float yaw = 0.0f, pitch = 0.0f, roll = 0.0f;
	private volatile boolean yawWait = false;
	private volatile boolean pitchWait = false;
	private volatile boolean rollWait = false;
	
	/** Constructs a new RandomRotationGenerator using the default settings. */
	public RandomRotationGenerator() {
		this.random = SecureRandomProvider.getSecureRandom();
	}
	
	/** @param maxIncrement The maximum amount of change per frame for each
	 *            rotation<br>
	 *            &nbsp;&nbsp;&nbsp;&nbsp;<b>Default:</b>&nbsp;<tt>1.2f</tt> */
	public RandomRotationGenerator(float maxIncrement) {
		this();
		this.maxIncrement = maxIncrement;
	}
	
	/** @param update Whether or not the rotation should be updated for the next
	 *            frame
	 * @return The next randomly generated rotation, slightly different than the
	 *         last */
	public float[] getRotation(boolean update) {
		try {
			return new float[] {this.yaw, this.pitch, this.roll};
		} finally {
			if(update) {
				if(!this.yawWait) {
					this.yaw += (this.random.nextFloat() * this.maxIncrement);
				} else {
					if(this.yawWait && this.random.nextInt(256) == 42) {
						this.yawWait = false;
					}
				}
				if(!this.pitchWait) {
					this.pitch += (this.random.nextFloat() * this.maxIncrement);
				} else {
					if(this.pitchWait && this.random.nextInt(256) == 42) {
						this.pitchWait = false;
					}
				}
				if(!this.rollWait) {
					this.roll += (this.random.nextFloat() * this.maxIncrement);
				} else {
					if(this.rollWait && this.random.nextInt(256) == 42) {
						this.rollWait = false;
					}
				}
				
				if(this.yaw >= 360.0f || this.yaw <= 0.0f) {
					this.yaw = (360.0f + this.yaw) % 360.0f;
					if(!this.yawWait && this.random.nextInt(100) == 42) {
						this.yawWait = true;
					}
				}
				if(this.pitch >= 360.0f || this.pitch <= 0.0f) {
					this.pitch = (360.0f + this.pitch) % 360.0f;
					if(!this.pitchWait && this.random.nextInt(100) == 42) {
						this.pitchWait = true;
					}
				}
				if(this.roll >= 360.0f || this.roll <= 0.0f) {
					this.roll = (360.0f + this.roll) % 360.0f;
					if(!this.rollWait && this.random.nextInt(100) == 42) {
						this.rollWait = true;
					}
				}
			}
		}
	}
	
	/** @return The next randomly generated rotation, slightly different than
	 *         the
	 *         last */
	public float[] getRotation() {
		return this.getRotation(true);
	}
	
}
