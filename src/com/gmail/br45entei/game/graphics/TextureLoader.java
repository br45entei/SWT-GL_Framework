package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.util.BufferUtil;
import com.gmail.br45entei.util.ResourceUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
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
import org.lwjgl.opengl.GL31;

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
 * @since 1.0
 * @author Kevin Glass
 * @author Brian Matzon */
public class TextureLoader {
	/** (Totally not a Pokémon reference) */
	private static final String missingNo = "/assets/textures/missing.png";
	
	protected static volatile int openGLTextureID;
	
	/** The table of textures that have been loaded in this loader */
	private static final ConcurrentHashMap<String, Texture> table = new ConcurrentHashMap<>();
	
	private static final Texture checkTableFor(String resourceName) {
		Texture tex = table.get(resourceName);
		if(tex != null) {
			if(tex.isDisposed()) {
				table.remove(resourceName);
			} else {
				return tex;
			}
		}
		return null;
	}
	
	/** The colour model including alpha for the GL image */
	public static final ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	
	/** The colour model for the GL image */
	public static final ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 0}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	
	public static final Texture NONE = new Texture(GL11.GL_TEXTURE_2D, 0, "null", false);
	public static final Texture OPENGL = new Texture(GL11.GL_TEXTURE_2D, 0, "OpenGL", true);
	
	/** Create a new texture ID
	 *
	 * @return A new texture ID */
	private static int createTextureID() {
		/*IntBuffer tmp = createIntBuffer(1);
		GL11.glGenTextures(tmp);
		return tmp.get(0);*/
		return GL11.glGenTextures();
	}
	
	/** @return The missing texture to be used when another texture is specified
	 *         but unable to be loaded */
	public static Texture getMissingTexture() {
		Texture tex = checkTableFor(missingNo);
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
		final boolean hasAlpha = bufferedImage.getColorModel().hasAlpha();
		if(hasAlpha) {
			srcPixelFormat = GL11.GL_RGBA;
		} else {
			srcPixelFormat = GL11.GL_RGB;
		}
		Texture texture = new Texture(target, textureID, missingNo, hasAlpha);
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		if(target == GL11.GL_TEXTURE_2D) {
			if(texture.getWidth() != texture.getHeight() && GLUtil.isGL31Available()) {
				target = texture.target = GL31.GL_TEXTURE_RECTANGLE;
			}
		}
		
		// convert that image into a byte buffer of texture data 
		
		ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);
		
		if(target == GL11.GL_TEXTURE_2D) {
			GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
			GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
			
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
	
	public static String getMissingTexturePath() {
		return missingNo;
	}
	
	/** Load a texture
	 *
	 * @param resourceName The location of the resource to load
	 * @return The loaded texture */
	public static Texture getTexture(String resourceName) {
		resourceName = fullPath(resourceName);
		Texture tex = checkTableFor(resourceName);
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
		Texture tex = checkTableFor(resourceName);
		if(tex != null) {
			return tex;
		}
		return getTexture(resourceName, target, GL11.GL_RGBA,     // dst pixel format
				
				GL11.GL_NEAREST, // min filter
				
				GL11.GL_NEAREST); // max filter
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
		Texture tex = checkTableFor(resourceName);
		if(tex != null) {
			return tex;
		}
		return getTexture(resourceName, target, GL11.GL_RGBA, minFilter, magFilter);
	}
	
	/** Resolves the specified resource path against the
	 * <tt>/assets/textures/</tt> package.
	 * 
	 * @param resourcePath The resource path to resolve
	 * @return The resulting path */
	public static final String fullPath(String resourcePath) {
		if(!resourcePath.startsWith("/") && !(resourcePath.startsWith("<") && resourcePath.endsWith(">"))) {
			resourcePath = "/" + resourcePath;
			if(!resourcePath.startsWith("/textures")) {
				resourcePath = "/textures" + resourcePath;
			}
			if(!resourcePath.startsWith("/assets")) {
				resourcePath = "/assets" + resourcePath;
			}
		}
		return resourcePath;
	}
	
	/** @param resourcePath The resource path to check
	 * @return Whether or not the resource exists(true if an input stream was
	 *         successfully opened from the resource, false otherwise) */
	public static final boolean doesResourceExist(String resourcePath) {
		return ResourceUtil.doesResourceExist(fullPath(resourcePath));
	}
	
	/** Load a texture into OpenGL from a image reference on
	 * disk.<br>
	 * This method is identical to
	 * {@link #createTexture(String, int, int, int, int)}, except that it caches
	 * textures based on the resource path and returns them when requested
	 * again.
	 *
	 * @param resourceName The location of the resource to load
	 * @param target The GL target to load the texture against
	 * @param dstPixelFormat The pixel format of the screen
	 * @param minFilter The minimizing filter
	 * @param magFilter The magnification filter
	 * @return The loaded texture */
	public static Texture getTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) {
		resourceName = fullPath(resourceName);
		Texture texture = checkTableFor(resourceName);
		if(texture != null) {
			return texture;
		}
		texture = createTexture(resourceName, target, dstPixelFormat, minFilter, magFilter);
		table.put(resourceName, texture);
		return texture;
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
	public static Texture createTexture(String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) {
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
				return null;
			}
		}
		return createTexture(bufferedImage, resourceName, target, dstPixelFormat, minFilter, magFilter);
	}
	
	public static Texture createTexture(BufferedImage bufferedImage, String resourceName, int target, int dstPixelFormat, int minFilter, int magFilter) {
		resourceName = fullPath(resourceName);
		int srcPixelFormat = 0;
		
		// create the texture ID for this texture 
		int textureID = createTextureID();
		
		// bind this texture 
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(target, textureID);
		final boolean hasAlpha = bufferedImage.getColorModel().hasAlpha();
		if(hasAlpha) {
			srcPixelFormat = GL11.GL_RGBA;
		} else {
			srcPixelFormat = GL11.GL_RGB;
		}
		Texture texture = new Texture(target, textureID, resourceName, hasAlpha);
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		if(target == GL11.GL_TEXTURE_2D) {
			if(texture.getWidth() != texture.getHeight() && GLUtil.isGL31Available()) {
				target = texture.target = GL31.GL_TEXTURE_RECTANGLE;
			}
		}
		
		// convert that image into a byte buffer of texture data 
		
		ByteBuffer textureBuffer = convertImageData(bufferedImage, texture);
		
		if(target == GL11.GL_TEXTURE_2D) {
			GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
			GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
			
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
			GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		}
		
		// produce a texture from the byte buffer
		
		int level = 0;
		int width = target == GL31.GL_TEXTURE_RECTANGLE ? bufferedImage.getWidth() : get2Fold(bufferedImage.getWidth());
		int height = target == GL31.GL_TEXTURE_RECTANGLE ? bufferedImage.getHeight() : get2Fold(bufferedImage.getHeight());
		int border = 0;
		int type = GL11.GL_UNSIGNED_BYTE;
		GL11.glTexImage2D(target, level, dstPixelFormat, width, height, border, srcPixelFormat, type, textureBuffer);
		
		// Unbind the newly created texture
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(target, 0);
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
		
		int texWidth = bufferedImage.getWidth();
		int texHeight = bufferedImage.getHeight();
		
		if(texture.getTarget() != GL31.GL_TEXTURE_RECTANGLE) {
			// find the closest power of 2 for the width and height of the produced texture
			texWidth = get2Fold(bufferedImage.getWidth());
			texHeight = get2Fold(bufferedImage.getHeight());
		}
		
		texture.setTextureHeight(texHeight);
		texture.setTextureWidth(texWidth);
		
		// create a raster that can be used by OpenGL as a source for the texture
		
		if(bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, new Point(0, 0));
			texImage = new BufferedImage(glAlphaColorModel, raster, false, new Hashtable<>());
		} else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 3, new Point(0, 0));
			texImage = new BufferedImage(glColorModel, raster, false, new Hashtable<>());
		}
		
		// copy the source image into the produced image
		
		Graphics2D g = texImage.createGraphics();
		g.setColor(new Color(0f, 0f, 0f, 0f));
		g.fillRect(0, 0, texWidth, texHeight);
		g.drawImage(bufferedImage, 0, 0, null);
		g.dispose();
		// build a byte buffer from the temporary image 
		
		// that be used by OpenGL to produce a texture.
		
		byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();
		
		/*imageBuffer = ByteBuffer.allocateDirect(data.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(data, 0, data.length);
		imageBuffer.flip();*/
		imageBuffer = BufferUtil.wrapDirect(data);
		
		return imageBuffer;
	}
	
	/** Load a given resource as a buffered image
	 * 
	 * @param ref The location of the resource to load
	 * @return The loaded buffered image
	 * @throws IOException Indicates a failure to find a resource */
	public static BufferedImage loadImage(String ref) throws IOException {
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
	
	/** Flips the image vertically and returns the resulting image.
	 * 
	 * @param image The image to flip vertically
	 * @return A new BufferedImage containing the same contents as the original,
	 *         only flipped vertically */
	public static BufferedImage createFlipped(BufferedImage image) {
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
