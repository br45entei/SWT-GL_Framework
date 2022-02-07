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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/** @since 1.0
 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
public strictfp class CubeTexture {
	
	private final Texture[] textures = new Texture[6];
	
	private volatile float scale = 0.5f;					//1.0f;
	private final Vector3f offset = new Vector3f();
	
	private volatile boolean visible = true;
	
	private volatile boolean isLocked = false;
	
	private volatile boolean renderTop = true;
	private volatile boolean renderBottom = true;
	private volatile boolean renderFront = true;
	private volatile boolean renderBack = true;
	private volatile boolean renderRight = true;
	private volatile boolean renderLeft = true;
	
	private volatile boolean flipTopV = false;
	private volatile boolean flipTopH = false;
	private volatile boolean flipBottomV = false;
	private volatile boolean flipBottomH = false;
	private volatile boolean flipFrontV = false;
	private volatile boolean flipFrontH = false;
	private volatile boolean flipBackV = false;
	private volatile boolean flipBackH = false;
	private volatile boolean flipRightV = false;
	private volatile boolean flipRightH = false;
	private volatile boolean flipLeftV = false;
	private volatile boolean flipLeftH = false;
	
	private volatile boolean isShadingEnabled = true;
	private volatile boolean isRenderingHitBox = false;
	
	protected volatile byte topLightLevel = 15;
	protected volatile byte bottomLightLevel = 15;
	protected volatile byte frontLightLevel = 15;
	protected volatile byte backLightLevel = 15;
	protected volatile byte rightLightLevel = 15;
	protected volatile byte leftLightLevel = 15;
	
	public final Vector3f topLightHue = new Vector3f(1, 1, 1);
	public final Vector3f bottomLightHue = new Vector3f(1, 1, 1);
	public final Vector3f frontLightHue = new Vector3f(1, 1, 1);
	public final Vector3f backLightHue = new Vector3f(1, 1, 1);
	public final Vector3f rightLightHue = new Vector3f(1, 1, 1);
	public final Vector3f leftLightHue = new Vector3f(1, 1, 1);
	
	protected volatile float topTransparency = 1.0f;
	protected volatile float bottomTransparency = 1.0f;
	protected volatile float frontTransparency = 1.0f;
	protected volatile float backTransparency = 1.0f;
	protected volatile float rightTransparency = 1.0f;
	protected volatile float leftTransparency = 1.0f;
	
	private volatile boolean renderTopOutside = true;
	private volatile boolean renderTopInside = false;
	private volatile boolean renderBottomOutside = true;
	private volatile boolean renderBottomInside = false;
	private volatile boolean renderFrontOutside = true;
	private volatile boolean renderFrontInside = false;
	private volatile boolean renderBackOutside = true;
	private volatile boolean renderBackInside = false;
	private volatile boolean renderRightOutside = true;
	private volatile boolean renderRightInside = false;
	private volatile boolean renderLeftOutside = true;
	private volatile boolean renderLeftInside = false;
	
	/** @return All of this block's textures in a list, in no particular
	 *         order */
	public final List<Texture> getAllTextures() {
		return Arrays.asList(this.textures);
	}
	
	public final boolean hasTexture(Texture texture) {//Tried a for loop with "int i = 0; i < ... etc" in here, but it reduces fps... weird.
		for(Texture t : this.textures) {
			if(t == texture) {
				return true;
			}
		}
		return false;
	}
	
	/** @param render Whether or not the outside sides of this cube will be
	 *            rendered */
	public final void setRenderOutsides(boolean render) {
		if(this.isLocked) {
			return;
		}
		this.renderBackOutside = render;
		this.renderBottomOutside = render;
		this.renderFrontOutside = render;
		this.renderLeftOutside = render;
		this.renderRightOutside = render;
		this.renderTopOutside = render;
	}
	
	/** @param render Whether or not the inside sides of this cube will be
	 *            rendered */
	public final void setRenderInsides(boolean render) {
		if(this.isLocked) {
			return;
		}
		this.renderBackInside = render;
		this.renderBottomInside = render;
		this.renderFrontInside = render;
		this.renderLeftInside = render;
		this.renderRightInside = render;
		this.renderTopInside = render;
	}
	
	/** @param texture The texture that will show on all six sides */
	public CubeTexture(Texture texture) {
		for(int i = 0; i < this.textures.length; i++) {
			this.textures[i] = texture;
		}
	}
	
	/** @param textures Front Back Top Bottom Right Left
	 * @throws IllegalArgumentException Thrown if there are not exactly six
	 *             textures provided */
	public CubeTexture(String... textures) {
		if(textures.length != this.textures.length) {
			throw new IllegalArgumentException("Cube textures must have 6 textures!");
		}
		this.textures[0] = TextureLoader.getTexture(textures[1]);
		this.textures[1] = TextureLoader.getTexture(textures[0]);
		this.textures[2] = TextureLoader.getTexture(textures[2]);
		this.textures[3] = TextureLoader.getTexture(textures[3]);
		this.textures[4] = TextureLoader.getTexture(textures[4]);
		this.textures[5] = TextureLoader.getTexture(textures[5]);
	}
	
	/** @param cubeTexture The cube texture to copy */
	public CubeTexture(CubeTexture cubeTexture) {
		this.textures[0] = cubeTexture.textures[0];
		this.textures[1] = cubeTexture.textures[1];
		this.textures[2] = cubeTexture.textures[2];
		this.textures[3] = cubeTexture.textures[3];
		this.textures[4] = cubeTexture.textures[4];
		this.textures[5] = cubeTexture.textures[5];
		
		this.renderTop = cubeTexture.renderTop;
		this.renderBottom = cubeTexture.renderBottom;
		this.renderFront = cubeTexture.renderFront;
		this.renderBack = cubeTexture.renderBack;
		this.renderRight = cubeTexture.renderRight;
		this.renderLeft = cubeTexture.renderLeft;
		
		this.flipTopV = cubeTexture.flipTopV;
		this.flipTopH = cubeTexture.flipTopH;
		this.flipBottomV = cubeTexture.flipBottomV;
		this.flipBottomH = cubeTexture.flipBottomH;
		this.flipFrontV = cubeTexture.flipFrontV;
		this.flipFrontH = cubeTexture.flipFrontH;
		this.flipBackV = cubeTexture.flipBackV;
		this.flipBackH = cubeTexture.flipBackH;
		this.flipRightV = cubeTexture.flipRightV;
		this.flipRightH = cubeTexture.flipRightH;
		this.flipLeftV = cubeTexture.flipLeftV;
		this.flipLeftH = cubeTexture.flipLeftH;
		
		this.isShadingEnabled = cubeTexture.isShadingEnabled;
		
		this.topLightLevel = cubeTexture.topLightLevel;
		this.bottomLightLevel = cubeTexture.bottomLightLevel;
		this.frontLightLevel = cubeTexture.frontLightLevel;
		this.backLightLevel = cubeTexture.backLightLevel;
		this.rightLightLevel = cubeTexture.rightLightLevel;
		this.leftLightLevel = cubeTexture.leftLightLevel;
		
		this.topLightHue.set(cubeTexture.topLightHue);
		this.bottomLightHue.set(cubeTexture.bottomLightHue);
		this.frontLightHue.set(cubeTexture.frontLightHue);
		this.backLightHue.set(cubeTexture.backLightHue);
		this.rightLightHue.set(cubeTexture.rightLightHue);
		this.leftLightHue.set(cubeTexture.leftLightHue);
		
		this.topTransparency = cubeTexture.topTransparency;
		this.bottomTransparency = cubeTexture.bottomTransparency;
		this.frontTransparency = cubeTexture.frontTransparency;
		this.backTransparency = cubeTexture.backTransparency;
		this.rightTransparency = cubeTexture.rightTransparency;
		this.leftTransparency = cubeTexture.leftTransparency;
		
		this.renderTopOutside = cubeTexture.renderTopOutside;
		this.renderTopInside = cubeTexture.renderTopInside;
		this.renderBottomOutside = cubeTexture.renderBottomOutside;
		this.renderBottomInside = cubeTexture.renderBottomInside;
		this.renderFrontOutside = cubeTexture.renderFrontOutside;
		this.renderFrontInside = cubeTexture.renderFrontInside;
		this.renderBackOutside = cubeTexture.renderBackOutside;
		this.renderBackInside = cubeTexture.renderBackInside;
		this.renderRightOutside = cubeTexture.renderRightOutside;
		this.renderRightInside = cubeTexture.renderRightInside;
		this.renderLeftOutside = cubeTexture.renderLeftOutside;
		this.renderLeftInside = cubeTexture.renderLeftInside;
		
	}
	
	protected final void lockProperties() {
		this.isLocked = true;
	}
	
	/** @return Whether or not this cube texture is locked. Specifically,
	 *         whether
	 *         or not this is an original cube texture. */
	public final boolean isLocked() {
		return this.isLocked;
	}
	
	/** @return Whether or not this cube texture is visible */
	public boolean isVisible() {
		if(!this.renderBack && !this.renderBottom && !this.renderFront && !this.renderLeft && !this.renderRight && !this.renderTop) {
			return false;
		}
		return this.visible;
	}
	
	/** @param visible Whether or not this cube texture should be visible */
	public final void setVisible(boolean visible) {
		if(this.isLocked) {
			return;
		}
		this.visible = visible;
	}
	
	/** @return This cube texture's scale(default: 0.5f) */
	public final float getScale() {
		return this.scale;
	}
	
	/** @param scale The scale that this cube texture should have(default:
	 *            0.5f) */
	public final void setScale(float scale) {
		if(this.isLocked) {
			return;
		}
		this.scale = scale;
	}
	
	/** @return This cube texture's offset(default: {0, 0, 0}) */
	public final Vector3f getOffset() {
		return this.offset;
	}
	
	/** @return Whether or not the top of this cube texture is being rendered */
	public final boolean getRenderTop() {
		return this.renderTop;
	}
	
	/** @param renderTop Whether or not the top of this cube texture should be
	 *            rendered */
	public final void setRenderTop(boolean renderTop) {
		if(this.isLocked) {
			return;
		}
		this.renderTop = renderTop;
	}
	
	/** @return Whether or not the bottom of this cube texture is being
	 *         rendered */
	public final boolean getRenderBottom() {
		return this.renderBottom;
	}
	
	/** @param renderBottom Whether or not the bottom of this cube texture
	 *            should
	 *            be rendered */
	public final void setRenderBottom(boolean renderBottom) {
		if(this.isLocked) {
			return;
		}
		this.renderBottom = renderBottom;
	}
	
	/** @return Whether or not the front of this cube texture is being
	 *         rendered */
	public final boolean getRenderFront() {
		return this.renderFront;
	}
	
	/** @param renderFront Whether or not the front of this cube texture should
	 *            be rendered */
	public final void setRenderFront(boolean renderFront) {
		if(this.isLocked) {
			return;
		}
		this.renderFront = renderFront;
	}
	
	/** @return Whether or not the back of this cube texture is being
	 *         rendered */
	public final boolean getRenderBack() {
		return this.renderBack;
	}
	
	/** @param renderBack Whether or not the back of this cube texture should be
	 *            rendered */
	public final void setRenderBack(boolean renderBack) {
		if(this.isLocked) {
			return;
		}
		this.renderBack = renderBack;
	}
	
	/** @return Whether or not the right of this cube texture is being
	 *         rendered */
	public final boolean getRenderRight() {
		return this.renderRight;
	}
	
	/** @param renderRight Whether or not the right of this cube texture should
	 *            be rendered */
	public final void setRenderRight(boolean renderRight) {
		if(this.isLocked) {
			return;
		}
		this.renderRight = renderRight;
	}
	
	/** @return Whether or not the left of this cube texture is being
	 *         rendered */
	public final boolean getRenderLeft() {
		return this.renderLeft;
	}
	
	/** @param renderLeft Whether or not the left of this cube texture should be
	 *            rendered */
	public final void setRenderLeft(boolean renderLeft) {
		if(this.isLocked) {
			return;
		}
		this.renderLeft = renderLeft;
	}
	
	public final boolean getFlipFrontV() {
		return this.flipFrontV;
	}
	
	public final void setFlipFrontV(boolean flipFrontV) {
		this.flipFrontV = flipFrontV;
	}
	
	public final boolean getFlipFrontH() {
		return this.flipFrontH;
	}
	
	public final void setFlipFrontH(boolean flipFrontH) {
		this.flipFrontH = flipFrontH;
	}
	
	public final boolean getFlipTopV() {
		return this.flipTopV;
	}
	
	public final void setFlipTopV(boolean flipTopV) {
		this.flipTopV = flipTopV;
	}
	
	public final boolean getFlipTopH() {
		return this.flipTopH;
	}
	
	public final void setFlipTopH(boolean flipTopH) {
		this.flipTopH = flipTopH;
	}
	
	public final boolean getFlipBottomV() {
		return this.flipBottomV;
	}
	
	public final void setFlipBottomV(boolean flipBottomV) {
		this.flipBottomV = flipBottomV;
	}
	
	public final boolean getFlipBottomH() {
		return this.flipBottomH;
	}
	
	public final void setFlipBottomH(boolean flipBottomH) {
		this.flipBottomH = flipBottomH;
	}
	
	public final boolean getFlipBackV() {
		return this.flipBackV;
	}
	
	public final void setFlipBackV(boolean flipBackV) {
		this.flipBackV = flipBackV;
	}
	
	public final boolean getFlipBackH() {
		return this.flipBackH;
	}
	
	public final void setFlipBackH(boolean flipBackH) {
		this.flipBackH = flipBackH;
	}
	
	public final boolean getFlipRightV() {
		return this.flipRightV;
	}
	
	public final void setFlipRightV(boolean flipRightV) {
		this.flipRightV = flipRightV;
	}
	
	public final boolean getFlipRightH() {
		return this.flipRightH;
	}
	
	public final void setFlipRightH(boolean flipRightH) {
		this.flipRightH = flipRightH;
	}
	
	public final boolean getFlipLeftV() {
		return this.flipLeftV;
	}
	
	public final void setFlipLeftV(boolean flipLeftV) {
		this.flipLeftV = flipLeftV;
	}
	
	public final boolean getFlipLeftH() {
		return this.flipLeftH;
	}
	
	public final void setFlipLeftH(boolean flipLeftH) {
		this.flipLeftH = flipLeftH;
	}
	
	/** @return Whether or not block shading(light levels) is enabled for this
	 *         cube texture */
	public final boolean isShadingEnabled() {
		return this.isShadingEnabled;
	}
	
	/** @param enable Whether or not block shading(light levels) should be
	 *            enabled for this cube texture */
	public final void setShadingEnabled(boolean enable) {
		this.isShadingEnabled = enable;
	}
	
	/** @return This cube texture's top light level */
	public final byte getTopLightLevel() {
		return this.topLightLevel;
	}
	
	/** @param lightLevel This cube texture's new top light level(must be &lt;=
	 *            15 and &gt;= 0) */
	public final void setTopLightLevel(byte lightLevel) {
		if(this.isLocked) {
			return;
		}
		if(lightLevel <= 15 || lightLevel >= 0) {
			this.topLightLevel = lightLevel;
		}
	}
	
	/** @return This cube texture's bottom light level */
	public final byte getBottomLightLevel() {
		return this.bottomLightLevel;
	}
	
	/** @param lightLevel This cube texture's new bottom light level(must be
	 *            &lt;= 15 and &gt;= 0) */
	public final void setBottomLightLevel(byte lightLevel) {
		if(this.isLocked) {
			return;
		}
		if(lightLevel <= 15 || lightLevel >= 0) {
			this.bottomLightLevel = lightLevel;
		}
	}
	
	/** @return This cube texture's front light level */
	public final byte getFrontLightLevel() {
		return this.frontLightLevel;
	}
	
	/** @param lightLevel This cube texture's new front light level(must be
	 *            &lt;=
	 *            15 and &gt;= 0) */
	public final void setFrontLightLevel(byte lightLevel) {
		if(this.isLocked) {
			return;
		}
		if(lightLevel <= 15 || lightLevel >= 0) {
			this.frontLightLevel = lightLevel;
		}
	}
	
	/** @return This cube texture's back light level */
	public final byte getBackLightLevel() {
		return this.backLightLevel;
	}
	
	/** @param lightLevel This cube texture's new back light level(must be &lt;=
	 *            15 and &gt;= 0) */
	public final void setBackLightLevel(byte lightLevel) {
		if(this.isLocked) {
			return;
		}
		if(lightLevel <= 15 || lightLevel >= 0) {
			this.backLightLevel = lightLevel;
		}
	}
	
	/** @return This cube texture's right light level */
	public final byte getRightLightLevel() {
		return this.rightLightLevel;
	}
	
	/** @param lightLevel This cube texture's new right light level(must be
	 *            &lt;=
	 *            15 and &gt;= 0) */
	public final void setRightLightLevel(byte lightLevel) {
		if(this.isLocked) {
			return;
		}
		if(lightLevel <= 15 || lightLevel >= 0) {
			this.rightLightLevel = lightLevel;
		}
	}
	
	/** @return This cube texture's left light level */
	public final byte getLeftLightLevel() {
		return this.leftLightLevel;
	}
	
	/** @param lightLevel This cube texture's new left light level(must be &lt;=
	 *            15 and &gt;= 0) */
	public final void setLeftLightLevel(byte lightLevel) {
		if(this.isLocked) {
			return;
		}
		if(lightLevel <= 15 || lightLevel >= 0) {
			this.leftLightLevel = lightLevel;
		}
	}
	
	/** @return Whether or not the top outside side of this cube is being
	 *         rendered */
	public final boolean getRenderTopOutside() {
		return this.renderTopOutside;
	}
	
	/** @param renderTopOutside Whether or not the top outside side of this cube
	 *            should be rendered */
	public final void setRenderTopOutside(boolean renderTopOutside) {
		if(this.isLocked) {
			return;
		}
		this.renderTopOutside = renderTopOutside;
	}
	
	/** @return Whether or not the top inside side of this cube is being
	 *         rendered */
	public final boolean getRenderTopInside() {
		return this.renderTopInside;
	}
	
	/** @param renderTopInside Whether or not the top inside side of this cube
	 *            should be rendered */
	public final void setRenderTopInside(boolean renderTopInside) {
		if(this.isLocked) {
			return;
		}
		this.renderTopInside = renderTopInside;
	}
	
	/** @return Whether or not the bottom outside side of this cube is being
	 *         rendered */
	public final boolean getRenderBottomOutside() {
		return this.renderBottomOutside;
	}
	
	/** @param renderBottomOutside Whether or not the bottom outside side of
	 *            this
	 *            cube should be rendered */
	public final void setRenderBottomOutside(boolean renderBottomOutside) {
		if(this.isLocked) {
			return;
		}
		this.renderBottomOutside = renderBottomOutside;
	}
	
	/** @return Whether or not the bottom inside side of this cube is being
	 *         rendered */
	public final boolean getRenderBottomInside() {
		return this.renderBottomInside;
	}
	
	/** @param renderBottomInside Whether or not the bottom inside side of this
	 *            cube should be rendered */
	public final void setRenderBottomInside(boolean renderBottomInside) {
		if(this.isLocked) {
			return;
		}
		this.renderBottomInside = renderBottomInside;
	}
	
	/** @return Whether or not the front outside side of this cube is being
	 *         rendered */
	public final boolean getRenderFrontOutside() {
		return this.renderFrontOutside;
	}
	
	/** @param renderFrontOutside Whether or not the front outside side of this
	 *            cube should be rendered */
	public final void setRenderFrontOutside(boolean renderFrontOutside) {
		if(this.isLocked) {
			return;
		}
		this.renderFrontOutside = renderFrontOutside;
	}
	
	/** @return Whether or not the front inside side of this cube is being
	 *         rendered */
	public final boolean getRenderFrontInside() {
		return this.renderFrontInside;
	}
	
	/** @param renderFrontInside Whether or not the front inside side of this
	 *            cube should be rendered */
	public final void setRenderFrontInside(boolean renderFrontInside) {
		if(this.isLocked) {
			return;
		}
		this.renderFrontInside = renderFrontInside;
	}
	
	/** @return Whether or not the back outside side of this cube is being
	 *         rendered */
	public final boolean getRenderBackOutside() {
		return this.renderBackOutside;
	}
	
	/** @param renderBackOutside Whether or not the back outside side of this
	 *            cube should be rendered */
	public final void setRenderBackOutside(boolean renderBackOutside) {
		if(this.isLocked) {
			return;
		}
		this.renderBackOutside = renderBackOutside;
	}
	
	/** @return Whether or not the back inside side of this cube is being
	 *         rendered */
	public final boolean getRenderBackInside() {
		return this.renderBackInside;
	}
	
	/** @param renderBackInside Whether or not the back inside side of this cube
	 *            should be rendered */
	public final void setRenderBackInside(boolean renderBackInside) {
		if(this.isLocked) {
			return;
		}
		this.renderBackInside = renderBackInside;
	}
	
	/** @return Whether or not the right outside side of this cube is being
	 *         rendered */
	public final boolean getRenderRightOutside() {
		return this.renderRightOutside;
	}
	
	/** @param renderRightOutside Whether or not the right outside side of this
	 *            cube should be rendered */
	public final void setRenderRightOutside(boolean renderRightOutside) {
		if(this.isLocked) {
			return;
		}
		this.renderRightOutside = renderRightOutside;
	}
	
	/** @return Whether or not the right inside side of this cube is being
	 *         rendered */
	public final boolean getRenderRightInside() {
		return this.renderRightInside;
	}
	
	/** @param renderRightInside Whether or not the right inside side of this
	 *            cube should be rendered */
	public final void setRenderRightInside(boolean renderRightInside) {
		if(this.isLocked) {
			return;
		}
		this.renderRightInside = renderRightInside;
	}
	
	/** @return Whether or not the left outside side of this cube is being
	 *         rendered */
	public final boolean getRenderLeftOutside() {
		return this.renderLeftOutside;
	}
	
	/** @param renderLeftOutside Whether or not the left outside side of this
	 *            cube should be rendered */
	public final void setRenderLeftOutside(boolean renderLeftOutside) {
		if(this.isLocked) {
			return;
		}
		this.renderLeftOutside = renderLeftOutside;
	}
	
	/** @return Whether or not the left inside side of this cube is being
	 *         rendered */
	public final boolean getRenderLeftInside() {
		return this.renderLeftInside;
	}
	
	/** @param renderLeftInside Whether or not the left inside side of this cube
	 *            should be rendered */
	public final void setRenderLeftInside(boolean renderLeftInside) {
		if(this.isLocked) {
			return;
		}
		this.renderLeftInside = renderLeftInside;
	}
	
	/** @param side The side to get(<b>0 = Front</b>, <b>1 = Back</b>, <b>2 =
	 *            Top</b>, <b>3 = Bottom</b>, <b>4 = Right</b>, <b>5 = Left</b>,
	 *            anything <b>&lt; 0</b> or <b>&gt; 5 equals null</b>)
	 * @return The texture for the given side, or null if there wasn't one or an
	 *         invalid side value was given */
	public final Texture getTexture(int side) {
		if(side < 0 || side >= this.textures.length) {
			return null;
		}
		return this.textures[side];
	}
	
	/** @param side The side to set(<b>0 = Front</b>, <b>1 = Back</b>, <b>2 =
	 *            Top</b>, <b>3 = Bottom</b>, <b>4 = Right</b>, <b>5 = Left</b>,
	 *            anything <b>&lt; 0</b> or <b>&gt; 5 equals null</b>)
	 * @param texture The texture to set the given side to
	 * @return Whether or not a valid side value was given */
	public final boolean setTexture(int side, Texture texture) {
		if(side < 0 || side >= this.textures.length) {
			return false;
		}
		this.textures[side] = texture;
		return true;
	}
	
	private static final boolean setCull(boolean renderInside, boolean renderOutside) {
		final int cull = !renderInside ? (!renderOutside ? GL11.GL_FRONT_AND_BACK : GL11.GL_BACK) : (!renderOutside ? GL11.GL_FRONT : GL11.GL_NONE);
		GL11.glFrontFace(GL11.GL_CCW);
		GL11.glCullFace(cull);
		if(cull == GL11.GL_NONE) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		} else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		return cull == GL11.GL_FRONT_AND_BACK;
	}
	
	@SuppressWarnings("unused")
	private static final void resetCull() {//set culling back to default and disable it
		GL11.glFrontFace(GL11.GL_CCW);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	/** Renders this cube without using any rendering checks */
	public final void renderCube() {
		if(!this.isVisible()) {
			return;
		}
		this.renderFront();
		this.renderBack();
		this.renderTop();
		this.renderBottom();
		this.renderRight();
		this.renderLeft();
	}
	
	public static volatile boolean test = false;
	
	/** Renders any sides of this cube that have the given texture
	 * 
	 * @param texture The texture whose sides will be rendered */
	public void renderSidesWithTexture(Texture texture) {
		if(!this.isVisible()) {
			return;
		}
		if(test) {
			this.translateOffset();
			GL11.glBegin(GL11.GL_QUADS);
		}
		if(this.getTexture(0) == texture) {
			this.renderFront(false);
		}
		if(this.getTexture(1) == texture) {
			this.renderBack(false);
		}
		if(this.getTexture(2) == texture) {
			this.renderTop(false);
		}
		if(this.getTexture(3) == texture) {
			this.renderBottom(false);
		}
		if(this.getTexture(4) == texture) {
			this.renderRight(false);
		}
		if(this.getTexture(5) == texture) {
			this.renderLeft(false);
		}
		if(test) {
			GL11.glEnd();
			this.unTranslateOffset();
		}
	}
	
	private float lastOffsetX = 0;
	private float lastOffsetY = 0;
	private float lastOffsetZ = 0;
	private boolean translated = false;
	
	private final void translateOffset() {
		if(this.offset.getX() != 0 || this.offset.getY() != 0 || this.offset.getZ() != 0) {
			this.lastOffsetX = this.offset.x;
			this.lastOffsetY = this.offset.y;
			this.lastOffsetZ = this.offset.z;
			GL11.glTranslatef(this.lastOffsetX, this.lastOffsetY, this.lastOffsetZ);
			this.translated = true;
		}
	}
	
	private final void unTranslateOffset() {
		if(this.translated) {
			this.translated = false;
			GL11.glTranslatef(-this.lastOffsetX, -this.lastOffsetY, -this.lastOffsetZ);
			this.lastOffsetX = 0;
			this.lastOffsetY = 0;
			this.lastOffsetZ = 0;
		}
	}
	
	private final strictfp void shadeSide(byte lightLevel, Vector3f lightHue, float transparency) {
		if(!this.isShadingEnabled || this.isRenderingHitBox) {
			return;
		}
		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		float shadeAmount = lightLevel / 15.0F;
		GL11.glColor4f(shadeAmount * lightHue.getX(), shadeAmount * lightHue.getY(), shadeAmount * lightHue.getZ(), transparency);
	}
	
	private final void unShade() {
		if(this.isRenderingHitBox) {
			return;
		}
		//GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	/** Binds the texture of and renders the front side of this cube
	 * 
	 * @return Whether or not the side was rendered */
	public final boolean renderFront() {
		return this.renderFront(true);
	}
	
	/** Renders the front side of this cube, binding the texture for that side
	 * if
	 * desired
	 * 
	 * @param bindTexture Whether or not the texture for this side should be
	 *            bound first(useful if you want to bind a texture once and then
	 *            render all of the cubes with that texture; see
	 *            {@link #renderSidesWithTexture(Texture)})
	 * @return Whether or not the side was rendered */
	public final strictfp boolean renderFront(boolean bindTexture) {
		if(this.renderFront && !setCull(this.renderFrontInside, this.renderFrontOutside)) {// Front Face
			if(bindTexture) {
				this.textures[0].bind();
			}
			this.shadeSide(this.frontLightLevel, this.frontLightHue, this.frontTransparency);
			if(!test) {
				this.translateOffset();
				GL11.glBegin(GL11.GL_QUADS);
			}
			GL11.glNormal3f(0f, 0f, 1f);
			if(this.flipFrontH && this.flipFrontV) {
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
			} else if(this.flipFrontH) {
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
			} else if(this.flipFrontV) {
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
			} else {
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
			}
			if(!test) {
				GL11.glEnd();
				this.unTranslateOffset();
			}
			this.unShade();
			return true;
		}
		return false;
	}
	
	/** Binds the texture of and renders the back side of this cube
	 * 
	 * @return Whether or not the side was rendered */
	public final boolean renderBack() {
		return this.renderBack(true);
	}
	
	/** Renders the back side of this cube, binding the texture for that side if
	 * desired
	 * 
	 * @param bindTexture Whether or not the texture for this side should be
	 *            bound first(useful if you want to bind a texture once and then
	 *            render all of the cubes with that texture; see
	 *            {@link #renderSidesWithTexture(Texture)})
	 * @return Whether or not the side was rendered */
	public final strictfp boolean renderBack(boolean bindTexture) {
		if(this.renderBack && !setCull(this.renderBackInside, this.renderBackOutside)) {// Back Face
			if(bindTexture) {
				this.textures[1].bind();
			}
			this.shadeSide(this.backLightLevel, this.backLightHue, this.backTransparency);
			if(!test) {
				this.translateOffset();
				GL11.glBegin(GL11.GL_QUADS);
			}
			GL11.glNormal3f(0f, 0f, -1f);
			if(this.flipFrontH && this.flipFrontV) {
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
			} else if(this.flipFrontH) {
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
			} else if(this.flipFrontV) {
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
			} else {
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
			}
			if(!test) {
				GL11.glEnd();
				this.unTranslateOffset();
			}
			this.unShade();
			return true;
		}
		return false;
	}
	
	/** Binds the texture of and renders the top side of this cube
	 * 
	 * @return Whether or not the side was rendered */
	public final boolean renderTop() {
		return this.renderTop(true);
	}
	
	/** Renders the top side of this cube, binding the texture for that side if
	 * desired
	 * 
	 * @param bindTexture Whether or not the texture for this side should be
	 *            bound first(useful if you want to bind a texture once and then
	 *            render all of the cubes with that texture; see
	 *            {@link #renderSidesWithTexture(Texture)})
	 * @return Whether or not the side was rendered */
	public final strictfp boolean renderTop(boolean bindTexture) {
		if(this.renderTop && !setCull(this.renderTopInside, this.renderTopOutside)) {// Top Face
			if(bindTexture) {
				this.textures[2].bind();
			}
			this.shadeSide(this.topLightLevel, this.topLightHue, this.topTransparency);
			if(!test) {
				this.translateOffset();
				GL11.glBegin(GL11.GL_QUADS);
			}
			GL11.glNormal3f(0f, 1f, 0f);
			if(this.flipFrontH && this.flipFrontV) {
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
			} else if(this.flipFrontH) {
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
			} else if(this.flipFrontV) {
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
			} else {
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
			}
			if(!test) {
				GL11.glEnd();
				this.unTranslateOffset();
			}
			this.unShade();
			return true;
		}
		return false;
	}
	
	/** Binds the texture of and renders the bottom side of this cube
	 * 
	 * @return Whether or not the side was rendered */
	public final boolean renderBottom() {
		return this.renderBottom(true);
	}
	
	/** Renders the bottom side of this cube, binding the texture for that side
	 * if desired
	 * 
	 * @param bindTexture Whether or not the texture for this side should be
	 *            bound first(useful if you want to bind a texture once and then
	 *            render all of the cubes with that texture; see
	 *            {@link #renderSidesWithTexture(Texture)})
	 * @return Whether or not the side was rendered */
	public final strictfp boolean renderBottom(boolean bindTexture) {
		if(this.renderBottom && !setCull(this.renderBottomInside, this.renderBottomOutside)) {// Bottom Face
			if(bindTexture) {
				this.textures[3].bind();
			}
			this.shadeSide(this.bottomLightLevel, this.bottomLightHue, this.bottomTransparency);
			if(!test) {
				this.translateOffset();
				GL11.glBegin(GL11.GL_QUADS);
			}
			GL11.glNormal3f(0f, -1f, 0f);
			if(this.flipFrontH && this.flipFrontV) {
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
			} else if(this.flipFrontH) {
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
			} else if(this.flipFrontV) {
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
			} else {
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
			}
			if(!test) {
				GL11.glEnd();
				this.unTranslateOffset();
			}
			this.unShade();
			return true;
		}
		return false;
	}
	
	/** Binds the texture of and renders the right side of this cube
	 * 
	 * @return Whether or not the side was rendered */
	public final boolean renderRight() {
		return this.renderRight(true);
	}
	
	/** Renders the right side of this cube, binding the texture for that side
	 * if
	 * desired
	 * 
	 * @param bindTexture Whether or not the texture for this side should be
	 *            bound first(useful if you want to bind a texture once and then
	 *            render all of the cubes with that texture; see
	 *            {@link #renderSidesWithTexture(Texture)})
	 * @return Whether or not the side was rendered */
	public final strictfp boolean renderRight(boolean bindTexture) {
		if(this.renderRight && !setCull(this.renderRightInside, this.renderRightOutside)) {// Right face
			if(bindTexture) {
				this.textures[4].bind();
			}
			this.shadeSide(this.rightLightLevel, this.rightLightHue, this.rightTransparency);
			if(!test) {
				this.translateOffset();
				GL11.glBegin(GL11.GL_QUADS);
			}
			GL11.glNormal3f(1f, 0f, 0f);
			if(this.flipFrontH && this.flipFrontV) {
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
			} else if(this.flipFrontH) {
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
			} else if(this.flipFrontV) {
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
			} else {
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, -this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, -this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(this.scale, this.scale, this.scale);	// Top Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(this.scale, -this.scale, this.scale);	// Bottom Left Of The Texture and Quad
			}
			if(!test) {
				GL11.glEnd();
				this.unTranslateOffset();
			}
			this.unShade();
			return true;
		}
		return false;
	}
	
	/** Binds the texture of and renders the left side of this cube
	 * 
	 * @return Whether or not the side was rendered */
	public final boolean renderLeft() {
		return this.renderLeft(true);
	}
	
	/** Renders the left side of this cube, binding the texture for that side if
	 * desired
	 * 
	 * @param bindTexture Whether or not the texture for this side should be
	 *            bound first(useful if you want to bind a texture once and then
	 *            render all of the cubes with that texture; see
	 *            {@link #renderSidesWithTexture(Texture)})
	 * @return Whether or not the side was rendered */
	public final strictfp boolean renderLeft(boolean bindTexture) {
		if(this.renderLeft && !setCull(this.renderLeftInside, this.renderLeftOutside)) {// Left Face
			if(bindTexture) {
				this.textures[5].bind();
			}
			this.shadeSide(this.leftLightLevel, this.rightLightHue, this.rightTransparency);
			if(!test) {
				this.translateOffset();
				GL11.glBegin(GL11.GL_QUADS);
			}
			GL11.glNormal3f(-1f, 0f, 0f);
			if(this.flipFrontH && this.flipFrontV) {
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
			} else if(this.flipFrontH) {
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
			} else if(this.flipFrontV) {
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
			} else {
				GL11.glTexCoord2f(0.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, -this.scale);	// Bottom Left Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 1.0f);
				GL11.glVertex3f(-this.scale, -this.scale, this.scale);	// Bottom Right Of The Texture and Quad
				GL11.glTexCoord2f(1.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, this.scale);	// Top Right Of The Texture and Quad
				GL11.glTexCoord2f(0.0f, 0.0f);
				GL11.glVertex3f(-this.scale, this.scale, -this.scale);	// Top Left Of The Texture and Quad
			}
			if(!test) {
				GL11.glEnd();
				this.unTranslateOffset();
			}
			this.unShade();
			return true;
		}
		return false;
	}
	
	protected static final Vector3f hitBoxColor = new Vector3f(0.0f, 0.5f, 1.0f);
	private static final Random randomColor = new Random();
	private static volatile boolean rUp = true;
	private static volatile boolean gUp = true;
	private static volatile boolean bUp = false;
	
	protected static final strictfp float limitColorValue(float color) {
		if(color > 1.0F) {
			color = 1.0F;
		} else if(color < 0.0f) {
			color = 0.0f;
		}
		return color;
	}
	
	private static final strictfp void incrementHitBoxColor() {
		float r = limitColorValue(hitBoxColor.getX());
		float g = limitColorValue(hitBoxColor.getY());
		float b = limitColorValue(hitBoxColor.getZ());
		
		float rIncrement = randomColor.nextFloat() / 10.0F;
		float gIncrement = randomColor.nextFloat() / 10.0F;
		float bIncrement = randomColor.nextFloat() / 10.0F;
		
		if(r == 1.0F) {
			rUp = false;
		} else if(r == 0.0F) {
			rUp = true;
		}
		if(g == 1.0F) {
			gUp = false;
		} else if(g == 0.0F) {
			gUp = true;
		}
		if(b == 1.0F) {
			bUp = false;
		} else if(b == 0.0F) {
			bUp = true;
		}
		rIncrement = rUp ? rIncrement : -rIncrement;
		gIncrement = gUp ? gIncrement : -gIncrement;
		bIncrement = bUp ? bIncrement : -bIncrement;
		
		r += rIncrement;
		g += gIncrement;
		b += bIncrement;
		hitBoxColor.set(r, g, b);
		//Main.clearColor.set(r, g, b);//XXX Remove/comment this or else som1 vill get da seizureses lel
	}
	
	/** Renders the hit box for this cube texture
	 * 
	 * @param changeColor Whether or not the hit box should change color */
	public strictfp void renderHitBox(boolean changeColor) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
		GL11.glLineWidth(2.0F);
		if(changeColor) {
			incrementHitBoxColor();
			GL11.glColor3f(hitBoxColor.getX(), hitBoxColor.getY(), hitBoxColor.getZ());
		} else {
			GL11.glColor3f(0.0f, 0.0f, 0.0f);
		}
		this.isRenderingHitBox = true;
		this.renderTop(false);
		this.renderBottom(false);
		this.renderFront(false);
		this.renderBack(false);
		this.renderRight(false);
		this.renderLeft(false);
		this.isRenderingHitBox = false;
		GL11.glLineWidth(1.0F);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glColor3f(1, 1, 1);
	}
	
	/** Renders a sample cube with a different color on each side */
	public static final void renderColorCube() {
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glColor3f(1.0f, 1.0f, 0.0f);//bindOpenGLTexture(cubeTexture.getTexture(0));
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		
		//
		GL11.glColor3f(1.0f, 0.5f, 0.0f);//bindOpenGLTexture(cubeTexture.getTexture(1));
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		
		//Front:
		GL11.glColor3f(0.5f, 0.0f, 0.0f);//bindOpenGLTexture(cubeTexture.getTexture(2));
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		
		//
		GL11.glColor3f(0.0f, 1.0f, 0.0f);//bindOpenGLTexture(cubeTexture.getTexture(3));
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		
		//
		GL11.glColor3f(0.0f, 0.0f, 1.0f);//bindOpenGLTexture(cubeTexture.getTexture(4));
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		
		//
		GL11.glColor3f(1.0f, 0.0f, 1.0f);//bindOpenGLTexture(cubeTexture.getTexture(5));
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		
		GL11.glEnd();
	}
	
}
