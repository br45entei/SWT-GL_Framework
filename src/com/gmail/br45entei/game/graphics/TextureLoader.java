package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.util.ResourceUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/** A utility class to load textures for <strike>JOGL</strike>. This source is
 * based on a texture that can be found in the Java Gaming (www.javagaming.org)
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
public class TextureLoader {
	/** (Totally not a Pokémon reference) */
	private static final String missingNo = "/assets/textures/missing.png";
	
	protected static volatile int openGLTextureID;
	
	/** The table of textures that have been loaded in this loader */
	private static final ConcurrentHashMap<String, Texture> table = new ConcurrentHashMap<>();
	
	/** The colour model including alpha for the GL image */
	private static final ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	
	/** The colour model for the GL image */
	private static final ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 0}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	
	public static final Texture NONE = new Texture(GL11.GL_TEXTURE_2D, 0, "null", false);
	public static final Texture OPENGL = new Texture(GL11.GL_TEXTURE_2D, 0, "OpenGL", true);
	
	/** Create a new texture ID
	 *
	 * @return A new texture ID */
	private static int createTextureID() {
		IntBuffer tmp = createIntBuffer(1);
		GL11.glGenTextures(tmp);
		return tmp.get(0);
	}
	
	/** @return The missing texture to be used when another texture is specified
	 *         but unable to be loaded */
	public static Texture getMissingTexture() {
		Texture tex = table.get(missingNo);
		if(tex != null) {
			return tex;
		}
		int target = GL11.GL_TEXTURE_2D;
		int dstPixelFormat = GL11.GL_RGBA;
		int minFilter = GL11.GL_NEAREST;
		int magFilter = GL11.GL_NEAREST;
		int srcPixelFormat = 0;
		int textureID = createTextureID();
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = loadImage(missingNo);
		} catch(IOException e) {
			//LogUtil.printErr("Failed to load texture \"" + resourceName + "\": ");
			//LogUtil.printErrln(e);
			GL11.glDeleteTextures(textureID);
			return null;
		}
		if(bufferedImage.getColorModel().hasAlpha()) {
			srcPixelFormat = GL11.GL_RGBA;
		} else {
			srcPixelFormat = GL11.GL_RGB;
		}
		Texture texture = new Texture(target, textureID, missingNo, bufferedImage.getColorModel().hasAlpha());
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		
		// convert that image into a byte buffer of texture data 
		
		ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);
		
		if(target == GL11.GL_TEXTURE_2D) {
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		}
		
		// produce a texture from the byte buffer
		
		GL11.glTexImage2D(target, 0, dstPixelFormat, get2Fold(bufferedImage.getWidth()), get2Fold(bufferedImage.getHeight()), 0, srcPixelFormat, GL11.GL_UNSIGNED_BYTE, textureBuffer);
		
		// Unbind the newly created texture
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(target, 0);
		table.put(missingNo, texture);
		return texture;
	}
	
	/** Load a texture
	 *
	 * @param resourceName The location of the resource to load
	 * @return The loaded texture */
	public static Texture getTexture(String resourceName) {
		resourceName = fullPath(resourceName);
		Texture tex = table.get(resourceName);
		if(tex != null) {
			return tex;
		}
		return getTexture(resourceName, GL11.GL_TEXTURE_2D, // target
				
				GL11.GL_RGBA,     // dst pixel format
				
				GL11.GL_NEAREST, // min filter
				
				GL11.GL_NEAREST); // max filter
	}
	
	/** Load a texture
	 *
	 * @param resourceName The location of the resource to load
	 * @param target The GL target to load the texture against
	 * @return The loaded texture */
	public static Texture getTexture(String resourceName, int target) {
		resourceName = fullPath(resourceName);
		Texture tex = table.get(resourceName);
		if(tex != null) {
			return tex;
		}
		return getTexture(resourceName, target, GL11.GL_RGBA,     // dst pixel format
				
				GL11.GL_LINEAR, // min filter
				
				GL11.GL_LINEAR); // max filter
	}
	
	/** Load a texture into OpenGL from a image reference on
	 * disk.
	 *
	 * @param resourceName The location of the resource to load
	 * @param target The GL target to load the texture against
	 * @param minFilter The minimizing filter
	 * @param magFilter The magnification filter
	 * @return The loaded texture */
	public static Texture getTexture(String resourceName, int target, int minFilter, int magFilter) {
		resourceName = fullPath(resourceName);
		Texture tex = table.get(resourceName);
		if(tex != null) {
			return tex;
		}
		return getTexture(resourceName, target, GL11.GL_RGBA, minFilter, magFilter);
	}
	
	private static final String fullPath(String resourceName) {
		if(!resourceName.startsWith("/")) {
			resourceName = "/" + resourceName;
			if(!resourceName.startsWith("/textures")) {
				resourceName = "/textures" + resourceName;
			}
			if(!resourceName.startsWith("/assets")) {
				resourceName = "/assets" + resourceName;
			}
		}
		return resourceName;
	}
	
	/** Load a texture into OpenGL from a image reference on
	 * disk.
	 *
	 * @param resourceName The location of the resource to load
	 * @param target The GL target to load the texture against
	 * @param dstPixelFormat The pixel format of the screen
	 * @param minFilter The minimizing filter
	 * @param magFilter The magnification filter
	 * @return The loaded texture */
	public static Texture getTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) {
		resourceName = fullPath(resourceName);
		Texture tex = table.get(resourceName);
		if(tex != null) {
			return tex;
		}
		Texture check = table.get(resourceName);
		if(check != null) {
			return check;
		}
		int srcPixelFormat = 0;
		
		// create the texture ID for this texture 
		int textureID = createTextureID();
		
		// bind this texture 
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(target, textureID);
		resourceName = fullPath(resourceName);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = loadImage(resourceName);
		} catch(IOException e) {
			//LogUtil.printErr("Failed to load texture \"" + resourceName + "\": ");
			//LogUtil.printErrln(e);
			try {
				bufferedImage = loadImage(missingNo);
			} catch(IOException e1) {
				//LogUtil.printErr(" /!\\  Failed to missing texture(\"" + missingNo + "\")!\r\n/___\\ Cause: ");
				//LogUtil.printErrln(e);
				GL11.glDeleteTextures(textureID);
				return null;
			}
		}
		if(bufferedImage.getColorModel().hasAlpha()) {
			srcPixelFormat = GL11.GL_RGBA;
		} else {
			srcPixelFormat = GL11.GL_RGB;
		}
		Texture texture = new Texture(target, textureID, resourceName, bufferedImage.getColorModel().hasAlpha());
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		
		// convert that image into a byte buffer of texture data 
		
		ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);
		
		if(target == GL11.GL_TEXTURE_2D) {
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		}
		
		// produce a texture from the byte buffer
		
		GL11.glTexImage2D(target, 0, dstPixelFormat, get2Fold(bufferedImage.getWidth()), get2Fold(bufferedImage.getHeight()), 0, srcPixelFormat, GL11.GL_UNSIGNED_BYTE, textureBuffer);
		
		// Unbind the newly created texture
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(target, 0);
		table.put(resourceName, texture);
		return texture;
	}
	
	/** Disposes of all of this TextureLoader's loaded Textures */
	public static final void disposeAll() {
		for(Texture tex : table.values()) {
			disposeTexture(tex);
		}
	}
	
	protected static final void disposeTexture(Texture texture) {
		texture.dispose();
		table.remove(texture.name);
	}
	
	/** Get the closest greater power of 2 to the fold number
	 * 
	 * @param fold The target number
	 * @return The power of 2 */
	public static final int get2Fold(int fold) {
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
	private static ByteBuffer convertImageData(BufferedImage bufferedImage, Texture texture) {
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
	
	/** Load a given resource as a buffered image
	 * 
	 * @param ref The location of the resource to load
	 * @return The loaded buffered image
	 * @throws IOException Indicates a failure to find a resource */
	private static BufferedImage loadImage(String ref) throws IOException {
		BufferedImage bufferedImage = null;
		try(InputStream in = ResourceUtil.loadResource(ref)) {//;//getClass().getClassLoader().getResourceAsStream(ref);//URL url = TextureLoader.class.getClassLoader().getResource(ref);
			if(in == null) {
				throw new IOException("Cannot find: " + ref);
			}
			bufferedImage = createFlipped(ImageIO.read(new BufferedInputStream(in)));
		}//in.close();
			//LogUtil.println/*Debug*/("Loaded texture: " + ref);
		return bufferedImage;
	}
	
	/** Creates an integer buffer to hold specified ints
	 * - strictly a utility method
	 *
	 * @param size how many int to contain
	 * @return created IntBuffer */
	protected static IntBuffer createIntBuffer(int size) {
		ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
		temp.order(ByteOrder.nativeOrder());
		
		return temp.asIntBuffer();
	}
	
	private static BufferedImage convertToARGB(BufferedImage image) {
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
	}
	
	private static BufferedImage createFlipped(BufferedImage image) {
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
		return createTransformed(image, at);
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage createRotated(BufferedImage image) {
		AffineTransform at = AffineTransform.getRotateInstance(Math.PI, image.getWidth() / 2, image.getHeight() / 2.0);
		return createTransformed(image, at);
	}
	
	private static BufferedImage createTransformed(BufferedImage image, AffineTransform at) {
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.transform(at);
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage createInverted(BufferedImage image) {
		if(image.getType() != BufferedImage.TYPE_INT_ARGB) {
			image = convertToARGB(image);
		}
		LookupTable lookup = new LookupTable(0, 4) {
			@Override
			public int[] lookupPixel(int[] src, int[] dest) {
				dest[0] = 255 - src[0];
				dest[1] = 255 - src[1];
				dest[2] = 255 - src[2];
				return dest;
			}
		};
		LookupOp op = new LookupOp(lookup, new RenderingHints(null));
		return op.filter(image, null);
	}
	
}
