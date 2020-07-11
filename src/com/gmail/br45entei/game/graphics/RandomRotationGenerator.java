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

import java.security.SecureRandom;

/** Class used to provide a random color that changes a little bit each time it
 * is requested.
 *
 * @since 1.0
 * @author Brian_Entei */
public class RandomRotationGenerator {
	
	private final SecureRandom random = new SecureRandom();
	private volatile float maxIncrement = 1.2f;
	private volatile float yaw = 0.0f, pitch = 0.0f, roll = 0.0f;
	private volatile boolean yawWait = false;
	private volatile boolean pitchWait = false;
	private volatile boolean rollWait = false;
	
	/** @param maxIncrement The maximum amount of change per frame for each
	 *            rotation<br>
	 *            &nbsp;&nbsp;&nbsp;&nbsp;<b>Default:</b>&nbsp;<tt>1.2f</tt> */
	public RandomRotationGenerator(float maxIncrement) {
		this.maxIncrement = maxIncrement;
	}
	
	/** Constructs a new RandomRotationGenerator using the default settings. */
	public RandomRotationGenerator() {
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
