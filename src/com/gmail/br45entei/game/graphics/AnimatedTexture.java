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

import com.gmail.br45entei.game.ui.Window;

import java.util.ArrayList;
import java.util.Collection;

/** Class used to store multiple textures and activate each one for a set amount
 * of time before moving on to the next one, creating a single 'animated'
 * texture.
 * 
 * @since 1.0
 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
public class AnimatedTexture {
	
	private volatile double millisBetweenChanges;
	private final ArrayList<Texture> textures = new ArrayList<>();
	private volatile int texIndex = 0;
	private volatile double frameRenderCount = 0;
	
	/** Creates a new AnimatedTexture with the specified settings and textures.
	 * 
	 * @param millisBetweenChanges The time, in milliseconds, that each texture
	 *            will display on screen before changing to the next texture
	 * @param textures The textures that will make up this animated texture */
	public AnimatedTexture(double millisBetweenChanges, Texture... textures) {
		this.millisBetweenChanges = millisBetweenChanges;
		final Texture missingNo = TextureLoader.getMissingTexture();
		textures = textures == null ? new Texture[] {missingNo} : textures;
		
		for(int i = 0; i < textures.length; i++) {
			Texture tex = textures[i];
			if(tex == null) {
				this.textures.add(missingNo);
			} else {
				this.textures.add(tex);
			}
		}
	}
	
	/** Creates a new AnimatedTexture with the specified settings and textures.
	 * 
	 * @param textures The textures that will make up this animated texture */
	public AnimatedTexture(Texture... textures) {
		this(1000.0 / Window.getDefaultRefreshRate(), textures);
	}
	
	/** Selects the next Texture for rendering in this AnimatedTexture's list of
	 * textures.<br>
	 * If the end of the list is reached, the first Texture is selected.
	 * 
	 * @return This AnimatedTexture */
	public AnimatedTexture advanceToNextTexture() {
		if(this.texIndex + 1 < this.textures.size()) {
			this.texIndex++;
		} else {
			this.texIndex = 0;
		}
		return this;
	}
	
	/** Uses the given deltaTime to see if this AnimatedTexture will
	 * {@link #advanceToNextTexture()} if the <tt>deltaTime</tt> is passed to
	 * {@link #update(double)}.
	 * 
	 * @param deltaTime The amount of time that has passed from the current
	 *            frame to the last (usually in microseconds, or milliseconds /
	 *            1000)
	 * @return Whether or not this AnimatedTexture will
	 *         {@link #advanceToNextTexture()} if the <tt>deltaTime</tt> is
	 *         passed to {@link #update(double)} */
	public boolean willAdvanceToNextTexture(double deltaTime) {
		return (this.frameRenderCount + (deltaTime * 1000.0)) >= this.millisBetweenChanges;
	}
	
	/** Adds the deltaTime to this AnimatedTexture's internal counter, and then
	 * checks to see if it needs to {@link #advanceToNextTexture()}.
	 * 
	 * @param deltaTime The amount of time that has passed from the current
	 *            frame to the last (usually in microseconds, or milliseconds /
	 *            1000) */
	public void update(double deltaTime) {
		this.frameRenderCount += deltaTime * 1000.0;
		if(this.frameRenderCount >= this.millisBetweenChanges) {
			do {
				this.frameRenderCount -= this.millisBetweenChanges;
				this.advanceToNextTexture();
			} while(this.frameRenderCount > this.millisBetweenChanges);
			//this.frameRenderCount = 0;
		}
	}
	
	/** Binds this AnimatedTexture's currently selected Texture
	 * 
	 * @return This AnimatedTexture */
	public AnimatedTexture bind() {
		if(this.texIndex >= 0 && this.texIndex < this.textures.size()) {
			this.textures.get(this.texIndex).bind();
		}
		return this;
	}
	
	/** Calls {@link #update(double)} and then {@link #bind()}.
	 * 
	 * @param deltaTime The amount of time that has passed from the current
	 *            frame to the last (usually in microseconds, or milliseconds /
	 *            1000)
	 * @return This AnimatedTexture */
	public AnimatedTexture bind(double deltaTime) {
		this.update(deltaTime);
		
		this.bind();
		return this;
	}
	
	/** @return The Texture that this AnimatedTexture currently has selected for
	 *         rendering */
	public Texture getCurrentTexture() {
		if(this.texIndex >= 0 && this.texIndex < this.textures.size()) {
			return this.textures.get(this.texIndex);
		}
		return null;
	}
	
	/** @return The total number of textures that this AnimatedTexture has
	 *         stored */
	public int getNumTextures() {
		return this.textures.size();
	}
	
	/** Returns a new array containing this AnimatedTexture's textures, in
	 * order.<br>
	 * Modifications to the returned array will not affect the contents of this
	 * AnimatedTexture's internal list.
	 * 
	 * @return A new array containing this AnimatedTexture's textures, in
	 *         order */
	public Texture[] getTextures() {
		return this.textures.toArray(new Texture[this.textures.size()]);
	}
	
	/** Returns the Texture at the specified index.<br>
	 * The index must be greater than or equal to zero, and less than the
	 * {@link #getNumTextures() number of existing textures}.
	 * 
	 * @param index The zero-based index of the desired Texture
	 * @return The Texture at the specified index, or <tt><b>null</b></tt> if
	 *         the specified index was out of range */
	public Texture getTexture(int index) {
		return index >= 0 && index < this.textures.size() ? this.textures.get(index) : null;
	}
	
	/** Puts the given Texture in this AnimatedTexture's internal list at the
	 * specified index.<br>
	 * The index must be greater than or equal to zero, and less than the
	 * {@link #getNumTextures() number of existing textures}.
	 * 
	 * @param index The zero-based index where the Texture will be set
	 * @param texture The Texture to set at the specified index
	 * @return The Texture that was previously at the specified index, or
	 *         <tt><b>null</b></tt> if the specified index was out of range */
	public Texture setTexture(int index, Texture texture) {
		if(index >= 0 && index < this.textures.size()) {
			Texture oldTex = this.getTexture(index);
			this.textures.set(index, texture == null ? TextureLoader.getMissingTexture() : texture);
			return oldTex;
		}
		return null;
	}
	
	/** Inserts the given Texture at the specified index.<br>
	 * The index must be greater than or equal to zero, and less than <em>or
	 * equal to</em> the {@link #getNumTextures() number of existing textures}.
	 * 
	 * @param index The index to insert the Texture at (the existing Texture at
	 *            this index and subsequent textures are shifted up to the
	 *            next index and so forth)
	 * @param texture The Texture to insert
	 * @return True if the specified index was valid and the Texture was
	 *         added */
	public boolean addTexture(int index, Texture texture) {
		if(index >= 0 && index <= this.textures.size()) {
			this.textures.add(index, texture == null ? TextureLoader.getMissingTexture() : texture);
			return true;
		}
		return false;
	}
	
	/** Removes the Texture at the specified index from this AnimatedTexture's
	 * internal list of textures.
	 * 
	 * @param index The index of the Texture to remove (any textures at indices
	 *            greater than this are shifted down to the previous index and
	 *            so forth)
	 * @return The Texture that was previously at the specified index, or
	 *         <tt><b>null</b></tt> if the specified index was out of range */
	public Texture removeTexture(int index) {
		if(index >= 0 && index < this.textures.size()) {
			return this.textures.remove(index);
		}
		return null;
	}
	
	/** Adds the given Texture to the end of the existing list of textures.
	 * 
	 * @param texture The Texture to insert
	 * @return True, as specified by {@link Collection#add(Object)} */
	public boolean addTexture(Texture texture) {
		return this.addTexture(this.textures.size(), texture);
	}
	
}
