package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.game.ui.Window;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

/** A texture to be bound within <strike>JOGL</strike> LWJGL. This
 * object is responsible for keeping track of a given OpenGL
 * texture and for calculating the texturing mapping coordinates
 * of the full image.
 * 
 * Since textures need to be powers of 2 the actual texture may be
 * considerably bigger than the source image and hence the texture
 * mapping coordinates need to be adjusted to match up drawing the
 * sprite against the texture.<br>
 * <br>
 * <b>Edited by:</b> Brian_Entei
 *
 * @since 1.0
 * @author Kevin Glass
 * @author Brian Matzon */
public class Texture {
	
	/** The GL target type */
	protected int target;
	/** The GL texture ID */
	private final int textureID;
	private final boolean hasAlpha;
	
	protected final String name;
	/** The height of the image */
	private int height;
	/** The width of the image */
	private int width;
	/** The width of the texture */
	private int texWidth;
	/** The height of the texture */
	private int texHeight;
	/** The ratio of the width of the image to the texture */
	private float widthRatio;
	/** The ratio of the height of the image to the texture */
	private float heightRatio;
	
	private volatile boolean isDisposed = false;
	
	/** Create a new texture
	 *
	 * @param target The GL target
	 * @param textureID The GL texture ID
	 * @param name The path to the resource used to load this texture
	 * @param hasAlpha Whether or not this Texture has an alpha channel */
	public Texture(int target, int textureID, String name, boolean hasAlpha) {
		this.target = target;
		this.textureID = textureID;
		this.name = name;
		this.hasAlpha = hasAlpha;
	}
	
	/** @return This texture's id */
	public final int getID() {
		if(this == TextureLoader.OPENGL) {
			return TextureLoader.openGLTextureID;
		}
		return this.textureID;
	}
	
	/** @return The GL target type */
	public int getTarget() {
		return this.target;
	}
	
	/** @return The file name or resource path name used to load this texture */
	public final String getTextureName() {
		return this.name;
	}
	
	/** @return True if this texture is translucent, or has an alpha channel */
	public final boolean hasAlpha() {
		return this.hasAlpha;
	}
	
	/** Unbinds any textures of any kind that may be in use */
	public static final void unbindAllTextures() {
		/*GL11.glColor3f(1, 1, 1);
		if(GLUtil.isGL20Available()) {
			GL20.glUseProgram(0);//disables active shader
		}*/
		if(GLUtil.isGL15Available()) {
			GL15.glBindBuffer(0, 0);//sets active buffer to null
		}
		if(GLUtil.isGL13Available()) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);//unbinds sampler slots and sets active texture to default
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
	}
	
	/** Binds the GL context to this texture. */
	public void bind() {
		this.bind(0);
	}
	
	/** Binds the GL context to this texture in the specified sampler slot.
	 * 
	 * @param samplerSlot The sampler slot to use */
	public void bind(int samplerSlot) {
		if(samplerSlot < 0 || samplerSlot > 31) {
			throw new IllegalArgumentException("Sampler slot out of range(must be >= 0 and <= 31)!");
		}
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + samplerSlot);
		GL11.glBindTexture(this.target, this.getID());
	}
	
	/** Set the height of the image
	 *
	 * @param height The height of the image */
	public void setHeight(int height) {
		this.height = height;
		setHeight();
	}
	
	/** Set the width of the image
	 *
	 * @param width The width of the image */
	public void setWidth(int width) {
		this.width = width;
		setWidth();
	}
	
	/** Get the height of the original image
	 *
	 * @return The height of the original image */
	public int getImageHeight() {
		if(this == TextureLoader.OPENGL) {
			return Window.getWindow().getHeight();
		}
		return this.height;
	}
	
	/** Get the width of the original image
	 *
	 * @return The width of the original image */
	public int getImageWidth() {
		if(this == TextureLoader.OPENGL) {
			return Window.getWindow().getWidth();
		}
		return this.width;
	}
	
	/** Get the height of the physical texture
	 *
	 * @return The height of physical texture */
	public float getHeight() {
		return this.heightRatio;
	}
	
	/** Get the width of the physical texture
	 *
	 * @return The width of physical texture */
	public float getWidth() {
		return this.widthRatio;
	}
	
	/** Set the height of this texture
	 *
	 * @param texHeight The height of the texture */
	public void setTextureHeight(int texHeight) {
		this.texHeight = texHeight;
		setHeight();
	}
	
	/** Set the width of this texture
	 *
	 * @param texWidth The width of the texture */
	public void setTextureWidth(int texWidth) {
		this.texWidth = texWidth;
		setWidth();
	}
	
	/** Set the height of the texture. This will update the
	 * ratio also. */
	private void setHeight() {
		if(this.texHeight != 0) {
			this.heightRatio = ((float) this.getImageHeight()) / this.texHeight;
		}
	}
	
	/** Set the width of the texture. This will update the
	 * ratio also. */
	private void setWidth() {
		if(this.texWidth != 0) {
			this.widthRatio = ((float) this.getImageWidth()) / this.texWidth;
		}
	}
	
	/** @param minFilter The min filter to use for this Texture
	 * @param magFilter The mag filter to use for this Texture
	 * @return This Texture */
	public Texture glTexParameteri(int minFilter, int magFilter) {
		this.bind();
		GL11.glTexParameteri(this.target, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
		GL11.glTexParameteri(this.target, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		return this;
	}
	
	/** Disposes of this Texture's resources */
	public final void dispose() {
		this.isDisposed = true;
		GL11.glDeleteTextures(this.textureID);
	}
	
	/** @return Whether or not this Texture has been {@link #dispose()
	 *         disposed}. */
	public final boolean isDisposed() {
		return this.isDisposed;
	}
	
}
