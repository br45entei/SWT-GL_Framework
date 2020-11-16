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
package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.thread.SecureRandomProvider;

import java.security.SecureRandom;

/** Class used to provide a random color that changes a little bit each time it
 * is requested.
 *
 * @since 1.0
 * @author Brian_Entei */
public class RandomColorGenerator {
	
	private final SecureRandom random;// A random source of data to use for our changing canvas color
	private volatile float maxIncrement = 0.05f;// Each color channel will be changed by a random float value between 0 and this number
	private volatile float r = 0.0f, g, b = 1.0f;// The three color channels that we'll use to make our GLCanvas change color
	private volatile boolean rUp = true, gUp, bUp = false;// The three booleans that will tell us what each color channel's direction of change is (up/down)
	private volatile boolean ruWait = false, guWait = false, buWait = false;
	private volatile boolean rdWait = false, gdWait = false, bdWait = false;
	
	/** Constructs a new RandomColorGenerator using the default settings. */
	public RandomColorGenerator() {
		this.random = SecureRandomProvider.getSecureRandom();
		this.g = this.random.nextFloat();
		this.gUp = this.random.nextBoolean();
	}
	
	/** Constructs a new RandomColorGenerator using the specified maximum
	 * increment.
	 *
	 * @param maxIncrement The maximum increment that each color channel may be
	 *            increased by each time {@link #getColor(boolean)
	 *            getColor(true)} is called. */
	public RandomColorGenerator(float maxIncrement) {
		this();
		this.maxIncrement = maxIncrement;
	}
	
	/** @param update Whether or not the color should be updated for the next
	 *            frame
	 * @return The next randomly generated color, slightly different than the
	 *         last */
	public float[] getColor(boolean update) {
		try {
			return new float[] {this.r, this.g, this.b};
		} finally {
			if(update) {
				if(!this.rdWait && !this.ruWait) {// If the color channel isn't staying on the same color for a while:
					this.r += (this.random.nextFloat() * this.maxIncrement) * (this.rUp ? 1.0f : -1.0f);
				} else {// The color channel is currently 'waiting', so let's have a slightly rarer random chance to let it continue
					if(this.rdWait && this.random.nextInt(256) == 42) {
						this.rdWait = false;
					}
					if(this.ruWait && this.random.nextInt(256) == 42) {
						this.ruWait = false;
					}
				}
				if(!this.gdWait && !this.guWait) {// ...above steps repeated for the green and blue color channels:
					this.g += (this.random.nextFloat() * this.maxIncrement) * (this.gUp ? 1.0f : -1.0f);
				} else {
					if(this.gdWait && this.random.nextInt(256) == 42) {
						this.gdWait = false;
					}
					if(this.guWait && this.random.nextInt(256) == 42) {
						this.guWait = false;
					}
				}
				if(!this.bdWait && !this.buWait) {
					this.b += (this.random.nextFloat() * this.maxIncrement) * (this.bUp ? 1.0f : -1.0f);
				} else {
					if(this.bdWait && this.random.nextInt(256) == 42) {
						this.bdWait = false;
					}
					if(this.buWait && this.random.nextInt(256) == 42) {
						this.buWait = false;
					}
				}
				
				if(this.r >= 1.0f && this.rUp) {// Check if the color channel has overshot the maximum value (which is 1.0f)
					this.rUp = false;// Set the direction to decreasing
					this.r = 1.0f;// Cap the color channel to the maximum (1.0f) just in case it overshot
					if(!this.rdWait && !this.ruWait && this.random.nextInt(100) == 42) {// Have a random chance to make the color channel stay on the same color for a while (while going up)
						this.rdWait = true;
					}
				}
				if(this.r <= 0.0f && !this.rUp) {// Check if the color channel has undershot the minimum value (which is 0.0f)
					this.rUp = true;// Set the direction to increasing
					this.r = 0.0f;// Cap the color channel to the minimum (0.0f) just in case it undershot
					if(!this.rdWait && !this.ruWait && this.random.nextInt(100) == 42) {// Have a random chance to make the color channel stay on the same color for a while (while going down)
						this.ruWait = true;
					}
				}
				if(this.g >= 1.0f && this.gUp) {// ...above steps repeated for the green and blue color channels:
					this.gUp = false;
					this.g = 1.0f;
					if(!this.gdWait && !this.guWait && this.random.nextInt(100) == 42) {
						this.gdWait = true;
					}
				}
				if(this.g <= 0.0f && !this.gUp) {
					this.gUp = true;
					this.g = 0.0f;
					if(!this.gdWait && !this.guWait && this.random.nextInt(100) == 42) {
						this.guWait = true;
					}
				}
				if(this.b >= 1.0f && this.bUp) {
					this.bUp = false;
					this.b = 1.0f;
					if(!this.bdWait && !this.buWait && this.random.nextInt(100) == 42) {
						this.bdWait = true;
					}
				}
				if(this.b <= 0.0f && !this.bUp) {
					this.bUp = true;
					this.b = 0.0f;
					if(!this.bdWait && !this.buWait && this.random.nextInt(100) == 42) {
						this.buWait = true;
					}
				}
			}
		}
	}
	
	/** @return The next randomly generated color, slightly different than the
	 *         last */
	public float[] getColor() {
		return this.getColor(true);
	}
	
}
