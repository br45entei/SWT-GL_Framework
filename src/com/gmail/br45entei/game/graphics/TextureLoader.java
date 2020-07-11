package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.util.BufferUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.ARBTextureRectangle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

/** A utility class to load textures for JOGL. This source is based
 * on a texture that can be found in the Java Gaming (www.javagaming.org)
 * Wiki. It has been simplified slightly for explicit 2D graphics use.
 * 
 * OpenGL uses a particular image format. Since the images that are
 * loaded from disk may not match this format this loader introduces
 * a intermediate image which the source image is copied into. In turn,
 * this image is used as source for the OpenGL texture.<br>
 * <br>
 * <b>Edited by:</b> Brian_Entei
 *
 * @author Kevin Glass
 * @author Brian Matzon */
@SuppressWarnings("static-access")
public class TextureLoader {
	
	protected static volatile int openGLTextureID;
	
	/** The table of textures that have been loaded in this loader */
	private static final HashMap<String, Texture> table = new HashMap<>();
	
	/** The colour model including alpha for the GL image */
	private static final ColorModel glAlphaColorModel;
	
	/** The colour model for the GL image */
	private static final ColorModel glColorModel;
	
	public static final Texture NONE = new Texture(GL11.GL_TEXTURE_2D, 0, "null");
	public static final Texture OPENGL = new Texture(GL11.GL_TEXTURE_2D, 0, "OpenGL");
	
	/** Create a new texture loader based on the game panel */
	static {
		glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 0}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
	}
	
	/** Create a new texture ID
	 *
	 * @return A new texture ID */
	private static final int createTextureID() {
		IntBuffer tmp = BufferUtil.createDirectIntBuffer(1);//createIntBuffer(1);
		GL11.glGenTextures(tmp);
		return tmp.rewind().get();
	}
	
	/** Totally not a Pokemon reference. Totally not. */
	private static Texture missingNo;
	
	/** @return The missing texture, or null if the missing texture is
	 *         missing.(Missing texture-ception!) */
	public static final Texture getMissingTexture() {
		if(missingNo != null) {
			return missingNo;
		}
		try {
			missingNo = getTexture("missing.png");
		} catch(Throwable ignored) {
		}
		return missingNo;
	}
	
	public static final Texture getTextureIfExists(String resourceName) {
		try {
			return getTexture(resourceName);
		} catch(IOException ignored) {
			return getMissingTexture();
		}
	}
	
	/** Load a texture
	 *
	 * @param resourceName The location of the resource to load
	 * @return The loaded texture
	 * @throws IOException Indicates a failure to access the resource */
	public static final Texture getTexture(String resourceName) throws IOException {
		Texture tex = table.get(resourceName);
		
		if(tex != null) {
			return tex;
		}
		
		tex = getTexture(resourceName, GL11.GL_TEXTURE_2D, // target
				
				GL11.GL_RGBA,     // dst pixel format
				
				GL11.GL_NEAREST, // min filter (unused) <--? orlly?
				
				GL11.GL_NEAREST);
		
		table.put(resourceName, tex);
		
		return tex;
	}
	
	/** Load a texture into OpenGL from a image reference on
	 * disk.
	 *
	 * @param resourceName The location of the resource to load
	 * @param target The GL target to load the texture against
	 * @param dstPixelFormat The pixel format of the screen
	 * @param minFilter The minimizing filter
	 * @param magFilter The magnification filter
	 * @return The loaded texture
	 * @throws IOException Indicates a failure to access the resource */
	public static final Texture getTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) throws IOException {
		return getTexture(resourceName, new Texture(target, createTextureID(), resourceName), target, dstPixelFormat, minFilter, magFilter);
	}
	
	/** @param resourceName The location of the resource to load
	 * @param texture The GL target to load the texture against
	 * @param target The GL target to load the texture against
	 * @param dstPixelFormat The pixel format of the screen
	 * @param minFilter The minimizing filter
	 * @param magFilter The magnification filter
	 * @return The loaded texture
	 * @throws IOException Indicates a failure to access the resource */
	public static final Texture getTexture(String resourceName, Texture texture, int target, int dstPixelFormat, int minFilter, int magFilter) throws IOException {
		String ref = resourceName == null ? "" : resourceName;
		ref = ref.startsWith("/") ? ref : "/assets/" + (ref.startsWith("textures") ? "" : "textures/") + ref;
		if(resourceName == null || !doesResourceExist(ref)) {
			return getMissingTexture();
		}
		int srcPixelFormat = 0;
		
		// bind this texture 
		
		GL11.glBindTexture(target, texture.getID());//this.bind(0);
		
		BufferedImage bufferedImage = loadImage(resourceName);
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		if(target == GL11.GL_TEXTURE_2D) {
			if(texture.getWidth() != texture.getHeight()) {
				target = GL31.GL_TEXTURE_RECTANGLE;
				texture.target = GL31.GL_TEXTURE_RECTANGLE;
			}
		}
		
		if(bufferedImage.getColorModel().hasAlpha()) {
			srcPixelFormat = GL11.GL_RGBA;
		} else {
			srcPixelFormat = GL11.GL_RGB;
		}
		
		// convert that image into a byte buffer of texture data 
		
		ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);
		
		if(target == GL11.GL_TEXTURE_2D || target == ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB) {
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		}
		
		// produce a texture from the byte buffer
		
		GL11.glTexImage2D(target, 0, dstPixelFormat, get2Fold(bufferedImage.getWidth()), get2Fold(bufferedImage.getHeight()), 0, srcPixelFormat, GL11.GL_UNSIGNED_BYTE, textureBuffer);
		
		return texture;
	}
	
	/** Get the closest greater power of 2 to the fold number
	 * 
	 * @param fold The target number
	 * @return The power of 2 */
	private static final int get2Fold(int fold) {
		int ret = 2;
		while(ret < fold) {
			ret *= 2;
		}
		return ret;
	}
	
	/** Convert the buffered image to a texture
	 *
	 * @param bufferedImage The image to convert to a texture
	 * @param texture The texture to store the data into
	 * @return A buffer containing the data */
	private static final ByteBuffer convertImageData(BufferedImage bufferedImage, Texture texture) {
		ByteBuffer imageBuffer = null;
		WritableRaster raster;
		BufferedImage texImage;
		
		int texWidth = 2;
		int texHeight = 2;
		
		// find the closest power of 2 for the width and height
		
		// of the produced texture
		
		while(texWidth < bufferedImage.getWidth()) {
			texWidth *= 2;
		}
		while(texHeight < bufferedImage.getHeight()) {
			texHeight *= 2;
		}
		
		texture.setTextureHeight(texHeight);
		texture.setTextureWidth(texWidth);
		
		// create a raster that can be used by OpenGL as a source
		
		// for a texture
		
		if(bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, null);
			texImage = new BufferedImage(glAlphaColorModel, raster, false, new Hashtable<>());
		} else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 3, null);
			texImage = new BufferedImage(glColorModel, raster, false, new Hashtable<>());
		}
		
		// copy the source image into the produced image
		
		Graphics g = texImage.getGraphics();
		g.setColor(new Color(0f, 0f, 0f, 0f));
		g.fillRect(0, 0, texWidth, texHeight);
		g.drawImage(bufferedImage, 0, 0, null);
		
		// build a byte buffer from the temporary image 
		
		// that be used by OpenGL to produce a texture.
		
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();
		
		imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);
		imageBuffer.flip();
		
		return imageBuffer;
	}
	
	private static final boolean doesResourceExist(String ref) {
		try(InputStream in = TextureLoader.class.getResourceAsStream(ref)) {
			return in != null;
		} catch(IOException ex) {
			ex.printStackTrace(System.err);
			System.err.flush();
			return false;
		}
	}
	
	/** Load a given resource as a buffered image
	 * 
	 * @param ref The location of the resource to load
	 * @return The loaded buffered image
	 * @throws IOException Indicates a failure to find a resource */
	private static final BufferedImage loadImage(String ref) throws IOException {
		ref = ref.startsWith("/") ? ref : "/assets/" + (ref.startsWith("textures") ? "" : "textures/") + ref;
		try(BufferedInputStream in = new BufferedInputStream(TextureLoader.class.getResourceAsStream(ref))) {
			return ImageIO.read(in);
		}
	}
	
	/** Creates an integer buffer to hold specified ints
	 * - strictly a utility method
	 *
	 * @param size how many int to contain
	 * @return created IntBuffer */
	/*protected static final IntBuffer createIntBuffer(int size) {
		ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
		temp.order(ByteOrder.nativeOrder());
		
		return temp.asIntBuffer();
	}*/
}
