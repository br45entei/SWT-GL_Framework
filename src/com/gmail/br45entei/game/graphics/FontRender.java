/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2007 Erwin Coumans http://continuousphysics.com/Bullet/
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.gmail.br45entei.game.graphics;

import com.gmail.br45entei.util.CodeUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;

/** Rendering class used to draw text using OpenGL.<br>
 * <br>
 * <b>Note from Brian_Entei:</b>&nbsp;Right now, this is using the deprecated GL
 * functions which are unavailable in forward-compatible contexts.<br>
 * I had a version of this class where I went through and updated the code to
 * support shaders, but it is on a dead USB flash drive right now...
 * 
 * @since 1.0
 * @author <a href=
 *         "https://github.com/davidB/jmbullet/blob/master/src/test/java/com/bulletphysics/demos/opengl/FontRender.java">jezek2</a><br>
 *         <b>Modified and expanded upon by Brian_Entei &lt;br45entei&#064;gmail.com&gt; </b> */
@SuppressWarnings("javadoc")
public class FontRender {
	
	private static volatile String fallbackFontFamily = "Consolas";
	
	public static final String setFallbackFontFamily(String family) {
		String oldFallback = fallbackFontFamily;
		if(family != null && !family.isBlank()) {
			fallbackFontFamily = family;
			family.isBlank();
		}
		return oldFallback;
	}
	
	public static void main(String[] args) {
		// With an Eclipse IDE, press F11 to launch this class, then make the nearestPowerOf2() method return 64 and click save and see what happens :)
		while(true) {
			int check = nearestPowerOf2(127);
			if(check == 64) {
				break;
			}
			System.out.println(check);
			CodeUtil.sleep(10L);
		}
		System.out.println(nearestPowerOf2(95));// 64
		System.out.println(nearestPowerOf2(96));// 128
		System.out.println(nearestPowerOf2(127));// 128
		System.out.println(nearestPowerOf2(128));// 128
		System.out.println(nearestPowerOf2(129));// 128
		System.out.println(nearestPowerOf2(191));// 128
		System.out.println(nearestPowerOf2(192));// 256
		// ...
	}
	
	/*public static final int charBegin = 0;//32
	public static final int charEnd = 256;//128
	public static final int charLength = charEnd - charBegin;*/
	
	//private static final File cacheDir = new File("/path/to/font/cache/dir/");
	
	private FontRender() {
	}
	
	protected static class Glyph {
		float x, y, w, h;
		final String c;
		int list = -1;
		
		public Glyph(String c) {
			this.c = c;
		}
		
		public Glyph(int codePoint) {
			this.c = new String(new int[] {codePoint}, 0, 1);
		}
		
		public Glyph(char c) {
			this.c = new String(new char[] {c});
		}
		
	}
	
	public static final void disposeAll() {
		for(GLFont font : GLFont.instances) {
			font.destroy();
			GLFont.instances.remove(font);
		}
	}
	
	public static final Collection<GLFont> getLoadedFonts() {
		return new ArrayList<>(GLFont.instances);
	}
	
	public static final int[] getSupportedCodePointsFor(Font font, int startingCodePoint, int endingCodePoint) {
		ArrayList<Integer> list = new ArrayList<>();
		for(int cp = startingCodePoint; cp < endingCodePoint; cp++) {
			if(font.canDisplay(cp)) {
				list.add(Integer.valueOf(cp));
			}
		}
		return GLUtil.unboxIntegers(list);
	}
	
	/** @author jezek2, Brian_Entei */
	public static class GLFont {
		
		public final Font baseFont;
		public final String unknownCharacterSymbol;
		
		protected static final ConcurrentLinkedDeque<GLFont> instances = new ConcurrentLinkedDeque<>();
		
		public final String name;
		public final boolean bold;
		public final boolean italic;
		public final boolean antialiasing;
		public final boolean usesFractionalMetrics;
		public final FontRenderContext frc;
		
		protected int texture = -1;
		protected int width, height;
		protected final Glyph[] glyphs = new Glyph[65536];
		protected final int unknownGlyphIndex;
		protected final int[] supportedCodePoints;
		public final boolean widerGlyphs;
		public final boolean scaleTextSizeToMatchNativeRenderingSize;
		
		public final int size;
		private volatile boolean disposed = false;
		
		public GLFont(String name, Font baseFont, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs) {
			this(name, baseFont, unknownCharSym, size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, true);
		}
		
		public GLFont(String name, Font baseFont, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs, boolean scaleTextSizeToMatchNativeRenderingSize) {
			this.name = name;
			this.baseFont = baseFont;
			
			this.supportedCodePoints = getSupportedCodePointsFor(baseFont, 0, 65536);
			this.unknownCharacterSymbol = new String(new int[] {unknownCharSym}, 0, 1);
			
			this.size = size;
			this.bold = bold;
			this.italic = italic;
			this.antialiasing = antialiasing;
			this.usesFractionalMetrics = usesFractionalMetrics;
			final double awtToGLScalar = 9.0 / 7.0;// Fonts drawn in OpenGL seem to be smaller than they should be (as compared to fonts of the same size rendered in the OS or by native applications)
			AffineTransform af = scaleTextSizeToMatchNativeRenderingSize ? AffineTransform.getScaleInstance(awtToGLScalar, awtToGLScalar) : null;
			this.frc = new FontRenderContext(af, antialiasing, usesFractionalMetrics);
			final Glyph unsupportedGlyph = new Glyph(this.unknownCharacterSymbol);
			this.glyphs[unknownCharSym] = unsupportedGlyph;
			this.unknownGlyphIndex = unknownCharSym;
			/*int unknownGlyphIndex = -1;
			for(int i = 0; i < this.glyphs.length; i++) {
				if(i < this.supportedCodePoints.length) {
					this.glyphs[i] = new Glyph(this.supportedCodePoints[i]);
					if(this.supportedCodePoints[i] == unknownCharSym) {
						unknownGlyphIndex = i;
					}
				} else {
					this.glyphs[i] = unsupportedGlyph;
					if(unknownGlyphIndex == -1) {
						unknownGlyphIndex = i;
					}
				}
			}*/
			for(int cp : this.supportedCodePoints) {
				if(cp < 0 || cp >= this.glyphs.length || cp == this.unknownGlyphIndex) {
					continue;
				}
				if(this.glyphs[cp] != null) {
					throw new IllegalArgumentException("Duplicate supported code point detected: \\u".concat(Integer.toHexString(cp)));
				}
				this.glyphs[cp] = new Glyph(cp);
				System.out.println("FontRender.GLFont<init>: Code Point #".concat(Integer.toString(cp)).concat(" (\"\\u".concat(Integer.toHexString(cp)).concat("\")")));
			}
			for(int i = 0; i < this.glyphs.length; i++) {
				if(this.glyphs[i] == null) {
					this.glyphs[i] = unsupportedGlyph;
				}
			}
			//this.unknownGlyphIndex = unknownGlyphIndex;
			this.widerGlyphs = widerGlyphs;
			this.scaleTextSizeToMatchNativeRenderingSize = scaleTextSizeToMatchNativeRenderingSize;
			instances.add(this);
		}
		
		public GLFont(String name, Font baseFont, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics) {
			this(name, baseFont, '?', size, bold, italic, antialiasing, usesFractionalMetrics, false);
		}
		
		public GLFont(String name, Font baseFont, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, InputStream in) throws IOException {
			this(name, baseFont, size, bold, italic, antialiasing, usesFractionalMetrics);
			load(in);
		}
		
		public Glyph getUnknownGlyph() {
			return this.glyphs[this.unknownGlyphIndex];
		}
		
		/*public Glyph getGlyphFor(int codePoint) {
			for(Glyph g : this.glyphs) {
				if(g.c.codePointAt(0) == codePoint) {
					return g;
				}
			}
			return this.getUnknownGlyph();
		}*/
		
		public Rectangle2D getStringBounds(String str) {
			return this.baseFont.getStringBounds(str, this.frc);
		}
		
		protected Glyph getGlyphForCodePoint(int codePoint) {
			if(codePoint < 0 || codePoint >= this.glyphs.length) {
				codePoint = this.unknownCharacterSymbol.codePointAt(0);
			}
			return this.glyphs[codePoint];
		}
		
		public float[] getGlyphBounds(int codePoint) {
			Glyph g = this.getGlyphForCodePoint(codePoint);
			return new float[] {0, 0, g.w, g.h};
		}
		
		public int[] getSupportedCodePoints() {
			int[] tmp = new int[this.supportedCodePoints.length];
			System.arraycopy(this.supportedCodePoints, 0, tmp, 0, tmp.length);
			return tmp;
		}
		
		public String getName() {
			return this.name;
		}
		
		public int getSize() {
			return this.size;
		}
		
		public int getWidth() {
			return this.width;
		}
		
		public int getHeight() {
			return this.height;
		}
		
		public double getLineHeight() {
			return this.size + (this.size * 0.36125); //0.355
		}
		
		protected double getLineHeightRender() {
			return this.size;// - (this.size * 0.094875);//0.098
		}
		
		public void destroy() {
			if(this.texture != 0) {
				GL11.glDeleteTextures(this.texture);
				this.texture = 0;
			}
			this.disposed = true;
		}
		
		public boolean isDisposed() {
			return this.disposed;
		}
		
		protected void save(File f) throws IOException {
			try(DataOutputStream out = new DataOutputStream(new FileOutputStream(f))) {
				out.writeInt(this.width);
				out.writeInt(this.height);
				
				GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 0);
				GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
				GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, 0);
				GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 0);
				
				int size = this.width * this.height * 4;
				ByteBuffer buf = ByteBuffer.allocateDirect((Byte.SIZE / 8) * size).order(ByteOrder.nativeOrder());//BufferUtils.createByteBuffer(size);
				byte[] data = new byte[size];
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
				GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
				buf.get(data);
				for(byte b : data) {
					out.writeByte(b & 0xFF);
				}
				
				for(int i = 0; i < this.glyphs.length; i++) {
					out.writeFloat(this.glyphs[i].x);
					out.writeFloat(this.glyphs[i].y);
					out.writeFloat(this.glyphs[i].w);
					out.writeFloat(this.glyphs[i].h);
				}
				
				out.close();
			}
		}
		
		@SuppressWarnings("resource")
		protected void load(File f) throws IOException {
			this.load(new FileInputStream(f));
		}
		
		protected void load(InputStream _in) throws IOException {
			try(DataInputStream in = new DataInputStream(_in)) {
				int w = in.readInt();
				int h = in.readInt();
				int size = w * h * 4;
				
				GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
				GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
				GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
				GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
				
				ByteBuffer buf = BufferUtils.createByteBuffer(size);//BufferUtil.createDirectByteBuffer(size);
				buf.rewind();
				for(int i = 0; i < size; i++) {
					buf.put(in.readByte());
				}
				buf.rewind();
				
				int[] id = new int[1];
				GL11.glGenTextures(id);
				this.texture = id[0];
				this.width = w;
				this.height = h;
				
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
				
				for(int i = 0; i < this.glyphs.length; i++) {
					this.glyphs[i].x = in.readFloat();
					this.glyphs[i].y = in.readFloat();
					this.glyphs[i].w = in.readFloat();
					this.glyphs[i].h = in.readFloat();
				}
				
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	private static String getFontFileName(String family, int size, boolean bold) {
		return family.replace(' ', '_') + "_" + size + (bold ? "_bold" : "") + ".fnt";
	}
	
	public static Font importTrueTypeFont(InputStream ttfIn) throws IOException {
		final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		final Font font;
		try(InputStream in = ttfIn) {
			font = Font.createFont(Font.TRUETYPE_FONT, in);
		} catch(FontFormatException | IOException ex) {
			if(ex instanceof IOException) {
				throw(IOException) ex;
			}
			throw new IOException(ex);
		}
		
		final String family = font.getFamily();
		boolean fontAlreadyRegistered = false;
		for(Font check : env.getAllFonts()) {
			if(check.getFamily().equals(family)) {
				fontAlreadyRegistered = true;
				break;
			}
		}
		if(!fontAlreadyRegistered) {
			env.registerFont(font);
		}
		
		return font;
	}
	
	public static void renderFont(GLFont glFont) {
		if(glFont.texture != -1) {
			return;
		}
		int style = glFont.bold ? Font.BOLD : Font.PLAIN;
		style = glFont.italic ? style | Font.ITALIC : style;
		final BufferedImage img = renderFont(glFont.baseFont.deriveFont(style, glFont.size), glFont, glFont.unknownCharacterSymbol.codePointAt(0), glFont.widerGlyphs);
		glFont.texture = createTexture(img, false);
		glFont.width = img.getWidth();
		glFont.height = img.getHeight();
	}
	
	@Deprecated
	public static void forceRenderFont(GLFont glFont) throws Throwable {
		final int oldTex = glFont.texture;
		try {
			renderFont(glFont);
			if(oldTex > 0) {
				GL11.glDeleteTextures(oldTex);
			}
		} catch(Throwable ex) {
			glFont.texture = oldTex;
			//ex.printStackTrace();
			throw ex;
		}
	}
	
	public static GLFont importTrueTypeFont(Font baseFont, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs, boolean scaleTextSizeToMatchNativeRenderingSize) {
		final GLFont gf = new GLFont(baseFont.getFamily(), baseFont, unknownCharSym, size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, scaleTextSizeToMatchNativeRenderingSize);
		/*int style = bold ? Font.BOLD : Font.PLAIN;
		style = italic ? style | Font.ITALIC : style;
		final BufferedImage img = renderFont(baseFont.deriveFont(style, size), gf, unknownCharSym, antialiasing, usesFractionalMetrics, widerGlyphs);
		gf.texture = createTexture(img, false);
		gf.width = img.getWidth();
		gf.height = img.getHeight();*/
		renderFont(gf);
		return gf;
	}
	
	public static GLFont importTrueTypeFont(InputStream ttfIn, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs, boolean scaleTextSizeToMatchNativeRenderingSize) throws IOException {
		return importTrueTypeFont(importTrueTypeFont(ttfIn), unknownCharSym, size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, scaleTextSizeToMatchNativeRenderingSize);
	}
	
	public static GLFont importTrueTypeFont(InputStream ttfIn, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs) throws IOException {
		return importTrueTypeFont(importTrueTypeFont(ttfIn), unknownCharSym, size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, true);
	}
	
	public static GLFont importTrueTypeFont(InputStream ttfIn, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics) throws IOException {
		return importTrueTypeFont(ttfIn, '?', size, bold, italic, antialiasing, usesFractionalMetrics, false);
	}
	
	public static GLFont createFont(String family, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs, boolean scaleTextSizeToMatchNativeRenderingSize) {
		int style = bold ? Font.BOLD : Font.PLAIN;
		style = italic ? style | Font.ITALIC : style;
		
		Font font = new Font(family, style, size);
		GLFont gf = new GLFont(family, font, unknownCharSym, size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, scaleTextSizeToMatchNativeRenderingSize);
		/*File f = new File(cacheDir, getFontFileName(family, size, bold));
		if (f.exists()) {
			gf.load(f);
			return gf;
		}*/
		
		BufferedImage img = renderFont(font, gf, unknownCharSym, widerGlyphs);
		gf.texture = createTexture(img, false);
		gf.width = img.getWidth();
		gf.height = img.getHeight();
		//gf.save(f);
		return gf;
	}
	
	public static GLFont createFont(String family, int unknownCharSym, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs) {
		return createFont(family, unknownCharSym, size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, true);
	}
	
	public static GLFont createFont(String family, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs, boolean scaleTextSizeToMatchNativeRenderingSize) {
		return createFont(family, '?', size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs, scaleTextSizeToMatchNativeRenderingSize);
	}
	
	public static GLFont createFont(String family, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, boolean widerGlyphs) {
		return createFont(family, '?', size, bold, italic, antialiasing, usesFractionalMetrics, widerGlyphs);
	}
	
	public static GLFont createFont(String family, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics) {
		return createFont(family, '?', size, bold, italic, antialiasing, usesFractionalMetrics, false);
	}
	
	public static final int nextPowerOf2(int i) {
		int j = 2;
		while(j < i) {
			j *= 2;
		}
		return j;
	}
	
	public static final int nearestPowerOf2(int i) {
		int j = 2;
		int lastJ = j;
		while(j < i) {
			lastJ = j;
			j *= 2;
		}
		int diff1 = Math.abs(j - i);
		int diff2 = Math.abs(lastJ - i);
		if(diff1 == 0 || diff1 <= diff2) {
			return j;
		}
		return lastJ;
	}
	
	/** Returns the glyph's actual bounds.
	 *
	 * @param font The font
	 * @param frc The FontRenderContext
	 * @param s The string containing the glyph
	 * @return The glyph's actual bounds
	 * @credit <a href=
	 *         "https://coderanch.com/t/672937/java/Writing-text-AWT-bounds#:~:text=width%20in%20FontMetrics.-,Chris%20Poe,-Greenhorn">Chris
	 *         Poe</a> */
	public static double[] getActualGlyphBounds(Font font, FontRenderContext frc, String s) {
		final GlyphVector gv = font.createGlyphVector(frc, s);// Chris Poe
		final Rectangle2D rect = gv.getOutline().getBounds2D();// Chris Poe
		AffineTransform af = frc.getTransform();// Brian_Entei
		double scaleX = af.getScaleX();// Brian_Entei
		double scaleY = af.getScaleY();// Brian_Entei
		return new double[] {rect.getX() * scaleX, rect.getY() * scaleY, rect.getWidth() * scaleX, rect.getHeight() * scaleY};
	}
	
	/** Returns the glyph's actual bounds.
	 *
	 * @param font The font
	 * @param frc The FontRenderContext
	 * @param s The string containing the glyph
	 * @return The glyph's actual bounds
	 * @credit <a href=
	 *         "https://coderanch.com/t/672937/java/Writing-text-AWT-bounds#:~:text=width%20in%20FontMetrics.-,Chris%20Poe,-Greenhorn">Chris
	 *         Poe</a> */
	public static int[] getActualGlyphBoundsI(Font font, FontRenderContext frc, String s) {
		double[] bounds = getActualGlyphBounds(font, frc, s);
		return new int[] {Long.valueOf(Math.round(Math.floor(bounds[0]))).intValue(), Long.valueOf(Math.round(Math.floor(bounds[1]))).intValue(), Long.valueOf(Math.round(Math.ceil(bounds[2]))).intValue(), Long.valueOf(Math.round(Math.ceil(bounds[3]))).intValue()};
	}
	
	public static BufferedImage renderFont(final Font font, GLFont glFont, int unknownCharSym, boolean widerGlyphs) {
		int[] supportedGlyphs = glFont.getSupportedCodePoints();
		int charLength = supportedGlyphs.length;
		System.out.println("[" + glFont.getName() + "] ================= " + charLength + " =================");
		FontRenderContext frc = glFont.frc;//new FontRenderContext(null, antialiasing, usesFractionalMetrics);
		boolean antialiasing = glFont.antialiasing;
		boolean usesFractionalMetrics = glFont.usesFractionalMetrics;
		
		int imgw, imgh;
		int cl = nextPowerOf2(charLength);
		{
			if(cl > 16384) {
				//throw new IllegalArgumentException("glyphs.length must not be larger than 16384! glyphs.length: ".concat(Integer.toString(glyphs.length)));
				System.err.println("[" + glFont.getName() + "] ================= " + cl + " --> 16384 =================");
				cl = 16384;
			}
			imgw = cl;
			if(font.getSize() >= 36) imgw <<= 1;
			if(font.getSize() >= 72) imgw <<= 1;
			
			imgw = Math.min(imgw, 16384);
			imgh = Math.min(imgw * 2, 16384);
			
			imgw = imgh == imgw ? imgw / 2 : imgw;
		}
		/*
		
		java.lang.IllegalArgumentException: Dimensions (width=65536 height=131072) are too large
			at java.desktop/java.awt.image.SampleModel.<init>(SampleModel.java:130)
			at java.desktop/java.awt.image.ComponentSampleModel.<init>(ComponentSampleModel.java:140)
			at java.desktop/java.awt.image.PixelInterleavedSampleModel.<init>(PixelInterleavedSampleModel.java:87)
			at java.desktop/java.awt.image.Raster.createInterleavedRaster(Raster.java:642)
			at java.desktop/java.awt.image.Raster.createInterleavedRaster(Raster.java:278)
			at java.desktop/java.awt.image.Raster.createInterleavedRaster(Raster.java:212)
			at com.gmail.br45entei.game.graphics.FontRender.createImage(FontRender.java:941)
		
		 */
		
		//BufferedImage img = new BufferedImage(imgw, charLength * 8, BufferedImage.TYPE_INT_ARGB);
		BufferedImage img = createImage(imgw, imgh, true);
		Graphics2D g = (Graphics2D) img.getGraphics();
		AffineTransform af = frc.getTransform();
		
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialiasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, usesFractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g.transform(af);
		g.setColor(Color.WHITE);
		g.setFont(font);
		
		int x = 0, y = 0, largestRowSize = 0, largestColumnSize = 0;
		double gw, gh;
		float glyphXSpacing = (font.getSize2D() / 18.0f) * ((float) af.getScaleX());
		for(int cp : glFont.supportedCodePoints) {
			if(cp < 0 || cp >= glFont.glyphs.length) {
				continue;
			}
			Glyph glyph = glFont.glyphs[cp];
			Rectangle2D rect = font.getStringBounds(glyph.c, frc);
			gw = rect.getWidth() * af.getScaleX();
			gh = rect.getHeight() * af.getScaleY();
			LineMetrics lm = font.getLineMetrics(glyph.c, frc);
			int[] bounds = getActualGlyphBoundsI(font, frc, glyph.c);
			//int xOffset = Math.min(0, Math.round(bounds[0]));
			int xOffset = widerGlyphs ? Math.min(0, Math.round(bounds[0])) : 0;
			float w = (widerGlyphs ? Math.max(bounds[2], (float) gw) : (float) gw) + 2;
			float h = ((float) gh) + glyphXSpacing;
			
			if(x + glyphXSpacing + w + (widerGlyphs ? 4 : 2) > img.getWidth()) {
				x = 0;
				y += largestColumnSize;
				largestColumnSize = 0;
			}
			if(cp != '\r' && cp != '\n' && cp != '\t' && cp != '\b' && cp != ' ') {
				g.drawString(glyph.c, x - xOffset, y + (int) lm.getAscent() + 1);
			} else {// Whitespace characters (except for space) have no actual width or height
				if(cp != ' ') {
					w = 0;
					h = 0;
				}
			}
			
			glyph.x = x * ((float) af.getScaleX());
			glyph.y = y * ((float) af.getScaleY());
			glyph.w = (w + (widerGlyphs ? 4 : 0)) * (glyph.c.equals("\t") ? 4.0f : 1.0f);
			glyph.h = h;
			
			/*if(Window.DEVELOPMENT_ENVIRONMENT && widerGlyphs) {
				if(cp != '\r' && cp != '\n' && cp != '\t' && cp != '\b' && cp != ' ') {
					g.drawRect(Math.round(glyph.x), Math.round(glyph.y), Math.round(glyph.w) - 2, Math.round(glyph.h) - 2);
				}
			}*/
			
			w += widerGlyphs ? (glyphXSpacing * 2.0) : glyphXSpacing;
			h += glyphXSpacing;
			
			x += w + glyphXSpacing;
			largestRowSize = Math.max(largestRowSize, x);
			largestColumnSize = Math.max(largestColumnSize, Math.round((float) Math.ceil(h)));
		}
		
		final Glyph unknownCharSymGlyph = glFont.getUnknownGlyph();//glFont.glyphs[glFont.unknownCharacterSymbol.codePointAt(0)];
		if(!"".isBlank()) {
			Font f;
			for(int i = 0; i < glFont.glyphs.length; i++) {
				f = font;
				Glyph glyph = glFont.glyphs[i];
				
				String s = glyph.c;
				final int cp = s.codePointAt(0);
				/*if(!f.canDisplay(cp) && cp != '\r' && cp != '\n' && cp != '\t' && cp != '\b' && cp != ' ') {
					if(cp >= 0 && cp < 256) {
						f = new Font(fallbackFontFamily, font.getSize(), font.getStyle());
					} else {
						s = new String(new int[] {unknownCharSym}, 0, 1);
					}
					//glFont.glyphs[i] = unknownCharSymGlyph;
					continue;
				}*/
				g.setFont(f);
				Rectangle2D rect = f.getStringBounds(s, frc);
				/*if((rect.getWidth() == 0.0 || rect.getHeight() == 0.0) && cp != unknownCharSym && cp != '\r' && cp != '\n' && cp != '\t' && cp != '\b' && cp != ' ') {
					if(Window.DEVELOPMENT_ENVIRONMENT) {
						System.err.println("FontRender.renderFont(...): \"".concat(new String(new int[] {cp}, 0, 1)).concat("\" (\\u").concat(Integer.toHexString(cp)).concat(") has either a width or height that is zero!"));
					}
					//glFont.glyphs[i] = unknownCharSymGlyph;
					continue;
				}*/
				LineMetrics lm = f.getLineMetrics(s, frc);
				int[] bounds = getActualGlyphBoundsI(f, frc, s);
				int xOffset = Math.min(0, bounds[0]);
				int w = (widerGlyphs ? Math.max(bounds[2], (int) rect.getWidth()) : (int) rect.getWidth()) + 2;
				int h = (int) rect.getHeight() + 2;
				
				if(x + 2 + w + (widerGlyphs ? 4 : 2) > img.getWidth()) {
					x = 0;
					y += largestColumnSize;
					largestColumnSize = 0;
				}
				if(cp != '\r' && cp != '\n' && cp != '\t' && cp != '\b' && cp != ' ') {
					g.drawString(s, x - xOffset, y + (int) lm.getAscent() + 1);
				} else {// Whitespace characters (except for space) have no actual width or height
					if(cp != ' ') {
						w = 0;
						h = 0;
					}
				}
				
				glyph.x = x;
				glyph.y = y;
				glyph.w = (w + (widerGlyphs ? 4 : 0)) * (s.equals("\t") ? 4.0f : 1.0f);
				glyph.h = h;
				
				/*if(Window.DEVELOPMENT_ENVIRONMENT && widerGlyphs) {
					if(cp != '\r' && cp != '\n' && cp != '\t' && cp != '\b' && cp != ' ') {
						g.drawRect(Math.round(glyph.x), Math.round(glyph.y), Math.round(glyph.w) - 2, Math.round(glyph.h) - 2);
					}
				}*/
				
				w += widerGlyphs ? 4 : 2;
				h += 2;
				
				x += w + 2;
				largestColumnSize = Math.max(largestColumnSize, h);
			}
			g.setFont(font);
		}
		
		/*for(int i = 0; i < glFont.glyphs.length; i++) {
			if(glFont.glyphs[i] == null) {
				glFont.glyphs[i] = unknownCharSymGlyph;
			}
		}*/
		
		y += largestColumnSize;
		g.dispose();
		
		/*if(y < cl) {
			img = img.getSubimage(0, 0, img.getWidth(), cl);
		} else if(y < cl * 2) {
			img = img.getSubimage(0, 0, img.getWidth(), cl * 2);
		} else if(y < cl * 4) {
			img = img.getSubimage(0, 0, img.getWidth(), cl * 4);
		} else if(y < cl * 8) {
			img = img.getSubimage(0, 0, img.getWidth(), cl * 8);
		}*/
		
		/*int width = Math.min(img.getWidth(), largestRowSize);
		int height = Math.min(img.getHeight(), y);
		
		if(width < img.getWidth() || height < img.getHeight()) {
			img = img.getSubimage(0, 0, width, height);
		}*/
		if(y < img.getHeight()) {
			img = img.getSubimage(0, 0, img.getWidth(), y);
		}
		
		return img;
	}
	
	private static void renderGlyph(GLFont font, Glyph g) {
		if(g.list != -1) {
			GL11.glCallList(g.list);
			return;
		}
		
		g.list = GL11.glGenLists(1);
		GL11.glNewList(g.list, GL11.GL_COMPILE_AND_EXECUTE);//GL11.glNewList(g.list, GL11.GL_COMPILE);
		
		float tw = font.width;// 512.0
		float th = font.height;//256.0
		
		CodeUtil.printlnDebug("FontRender.renderGlyph(): " + g.c + " (\"\\u" + Integer.toHexString(g.c.codePointAt(0)) + "\"): " + tw + ", " + th + ", " + g.x + ", " + g.y + ", " + g.w + ", " + g.h);
		
		float gX = g.x;//    203.0
		float gY = g.y;//    18.0
		float gW = g.w - 1;//10.0
		float gH = g.h - 1;//15.0
		
		GL11.glBegin(GL11.GL_QUADS);
		/*GL11.glTexCoord2d(203.0 / 512.0, 18.0 / 256.0);// This is H if font size == 12 and font == "Times New Roman"
		GL11.glVertex3d(0, 0, 1);
		
		GL11.glTexCoord2d((203.0 + 10.0) / 512.0, 18.0 / 256.0);
		GL11.glVertex3d(10.0, 0, 1);
		
		GL11.glTexCoord2d((203.0 + 10.0) / 512.0, (18.0 + 15.0) / 256.0);
		GL11.glVertex3d(10.0, 15.0, 1);
		
		GL11.glTexCoord2d(203.0 / 512.0, (18.0 + 15.0) / 256.0);
		GL11.glVertex3d(0, 15.0, 1);*/
		GL11.glTexCoord2f((gX) / tw, (gY) / th);
		GL11.glVertex3f(0, 0, 1);
		
		GL11.glTexCoord2f((gX + gW) / tw, (gY) / th);
		GL11.glVertex3f((0 + gW), 0, 1);
		
		GL11.glTexCoord2f((gX + gW) / tw, (gY + gH) / th);
		GL11.glVertex3f((0 + gW), 0 + gH, 1);
		
		GL11.glTexCoord2f((gX) / tw, (gY + gH) / th);
		GL11.glVertex3f(0, 0 + gH, 1);
		GL11.glEnd();
		
		GL11.glEndList();
		//GL11.glCallList(g.list);
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s) {
		return drawString(font, s, 0, 0);
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s, double x, double y) {
		double[] color = GLUtil.getGLColor();
		return drawString(font, s, x, y, color[0], color[1], color[2], color[3]);
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s, double x, double y, double red, double green, double blue) {
		return drawString(font, s, x, y, red, green, blue, GLUtil.getGLColor()[3]);
	}
	
	public static final class GLFontBounds {
		
		public volatile double x;
		public volatile double y;
		public volatile double negWidth;
		public volatile double negHeight;
		public volatile double width;
		public volatile double height;
		
		public GLFontBounds(double x, double y, double negWidth, double negHeight, double width, double height) {
			this.x = x;
			this.y = y;
			this.negWidth = negWidth < 0 ? -negWidth : negWidth;
			this.negHeight = negHeight < 0 ? -negHeight : negHeight;
			this.width = width;
			this.height = height;
		}
		
		public final double[] getBounds() {
			return new double[] {this.x - this.negWidth, this.y - (this.negHeight + this.height), this.negWidth + this.width, this.negHeight + this.height};
		}
		
		public final GLFontBounds bottomLeftToUpperLeftOrigin(double height) {
			this.y = height - this.y;
			return this;
		}
		
		@Override
		public String toString() {
			return new StringBuilder().append(this.x).append(",").append(this.y).append(",").append(this.negWidth).append(",").append(this.negHeight).append(",").append(this.width).append(",").append(this.height).toString();
		}
		
	}
	
	public static GLFontBounds sizeOf(GLFont font, CharSequence s) {
		return sizeOf(font, s, 0, 0);
	}
	
	public static GLFontBounds sizeOf(GLFont font, CharSequence s, double x, double y) {
		return sizeOf(font, s, x, y, 1.0, 1.0);
	}
	
	public static GLFontBounds sizeOf(GLFont font, CharSequence s, double x, double y, double scaleX, double scaleY) {
		double sizeWidth = 0;
		double sizeHeight = font.getLineHeight();
		
		double standardCharWidth = (font.size + 0.0) * 0.75;
		
		double smallestWidth = 0;
		double smallestHeight = 0;
		double largestWidth = sizeWidth;
		double largestHeight = sizeHeight;
		double lastCharWidth = 0;
		
		for(int cp : s.codePoints().toArray()) {
			String c = new String(new int[] {cp}, 0, 1);
			//String c = font.codePointsOrCharacters ? new String(new int[] {cp}, 0, 1) : new String(new char[] {(char) cp});
			if(cp < 0 || cp >= font.glyphs.length) {
				if(!c.equals("\r") && !c.equals("\n") && !c.equals("\b") && !c.equals("\t") && !c.equals(" ")) {
					c = font.unknownCharacterSymbol;
				}
				cp = font.unknownCharacterSymbol.codePointAt(0);
			}
			if(cp < 0 || cp >= font.glyphs.length) {
				cp = font.unknownCharacterSymbol.codePointAt(0);
			}
			Glyph g = font.glyphs[cp];
			//Glyph g = font.getGlyphFor(cp);
			double w = Math.round(g.w - (font.widerGlyphs ? -0.0 : g.w * 0.15));
			if(c.equals("\b")) {
				sizeWidth -= lastCharWidth;//w * scaleX;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else if(c.equals("\r")) {
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
				sizeWidth = 0;
			} else if(c.equals("\n")) {
				sizeHeight += Math.round(font.getLineHeight()) * scaleY;
				if(sizeHeight < smallestHeight) {
					smallestHeight = sizeHeight;
				} else if(sizeHeight > largestHeight) {
					largestHeight = sizeHeight;
				}
			} else if(c.equals("\t")) {
				if(Math.round(w) == 0.0) {
					w = Math.round(standardCharWidth * 4.0);//Math.round(font.size * 0.5625 * 5.3333333333333333);
				}
				sizeWidth += w * scaleX;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else {
				sizeWidth += w * scaleX;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			}
			if(!c.equals("\b") && !c.equals("\r") && !c.equals("\n")) {
				lastCharWidth = w * scaleX;
			}
		}
		return new GLFontBounds(x, y, smallestWidth, smallestHeight, largestWidth, largestHeight);
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s, double x, double y, double red, double green, double blue, double alpha) {
		return drawString(font, s, x, y, red, green, blue, alpha, 1.0, 1.0, 1.0);
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s, double x, double y, double red, double green, double blue, double alpha, double scaleX, double scaleY, double scaleZ) {
		/*Rectangle vp = Window.getWindow().getViewport();
		double width = (Math.max(1.0, vp.width) + 0.0);
		double height = (Math.max(1.0, vp.height) + 0.0);*/
		
		GLUtil.glPushCullMode();
		GLUtil.glCullFront();
		GLUtil.glPushBlendMode();
		GLUtil.glBlend(true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glPushMatrix();
		GLUtil.glPushColor();
		GL11.glTranslated(x, y, 1.0);
		GL11.glRotated(180, 1, 0, 0);
		GL11.glScaled(scaleX, scaleY, scaleZ);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GLUtil.glColord(red, green, blue, alpha);
		double lineWidth = 0;
		//double lineHeight = font.getLineHeightRender();
		double sizeWidth = 0;
		double sizeHeight = font.getLineHeight();
		
		double standardCharWidth = (font.size + 0.0) * 0.75;
		
		double smallestWidth = 0;
		double smallestHeight = 0;
		double largestWidth = sizeWidth;
		double largestHeight = sizeHeight;
		double lastCharWidth = 0;
		/*for(int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			if(c < charBegin || c > charEnd) c = '?';
			Glyph g = font.glyphs[c - charBegin];*/
		for(int cp : s.codePoints().toArray()) {
			String c = new String(new int[] {cp}, 0, 1);
			//String c = font.codePointsOrCharacters ? new String(new int[] {cp}, 0, 1) : new String(new char[] {(char) cp});
			if(cp < 0 || cp >= font.glyphs.length) {
				if(!c.equals("\r") && !c.equals("\n") && !c.equals("\b") && !c.equals("\t") && !c.equals(" ")) {
					c = font.unknownCharacterSymbol;
				}
				cp = font.unknownCharacterSymbol.codePointAt(0);
			}
			if(cp < 0 || cp >= font.glyphs.length) {
				cp = font.unknownCharacterSymbol.codePointAt(0);
			}
			Glyph g = font.glyphs[cp];
			//Glyph g = font.getGlyphFor(cp);
			
			double w = Math.round(g.w - (font.widerGlyphs ? -0.0 : g.w * 0.15));
			if(c.equals("\b")) {
				GL11.glTranslated(-lastCharWidth, 0, 0);
				lineWidth -= w;
				sizeWidth -= w;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else if(c.equals("\r")) {
				GL11.glTranslated(-lineWidth, 0, 0);
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
				lineWidth = 0;
				sizeWidth = 0;
			} else if(c.equals("\n")) {
				double h = Math.round(font.getLineHeight());
				GL11.glTranslated(0, h, 0);
				//lineHeight += h;
				sizeHeight += h;
				if(sizeHeight < smallestHeight) {
					smallestHeight = sizeHeight;
				} else if(sizeHeight > largestHeight) {
					largestHeight = sizeHeight;
				}
			} else if(c.equals("\t")) {
				if(Math.round(w) == 0.0) {
					w = Math.round(standardCharWidth * 4.0);//Math.round(font.size * 0.5625 * 5.3333333333333333);
				}
				GL11.glTranslated(w, 0, 0);
				lineWidth += w;
				sizeWidth += w;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else {
				renderGlyph(font, g);
				GL11.glTranslated(w, 0, 0);
				lineWidth += w;
				sizeWidth += w;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			}
			if(!c.equals("\b") && !c.equals("\r") && !c.equals("\n")) {
				lastCharWidth = w;
			}
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GLUtil.glPopColor();
		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_BLEND);
		GLUtil.glPopBlendMode();
		GLUtil.glPopCullMode();
		return new GLFontBounds(x, y, smallestWidth, smallestHeight, largestWidth, largestHeight);
	}
	
	protected static final int test = '\uE230';
	
	private static ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 0}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	private static ColorModel glColorModelAlpha = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	
	private static int createTexture(BufferedImage img, boolean mipMap) {
		boolean USE_COMPRESSION = false;
		
		int[] id = new int[1];
		GL11.glGenTextures(id);
		int tex = id[0];
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipMap ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		
		byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder()).put(data, 0, data.length).rewind();
		
		boolean alpha = img.getColorModel().hasAlpha();
		
		//GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, alpha? GL11.GL_RGBA:GL11.GL_RGB, img.getWidth(), img.getHeight(), 0, alpha? GL11.GL_RGBA:GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, USE_COMPRESSION ? (alpha ? GL13.GL_COMPRESSED_RGBA : GL13.GL_COMPRESSED_RGB) : (alpha ? GL11.GL_RGBA : GL11.GL_RGB), img.getWidth(), img.getHeight(), 0, alpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
		if(mipMap) {
			GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, USE_COMPRESSION ? (alpha ? GL13.GL_COMPRESSED_RGBA : GL13.GL_COMPRESSED_RGB) : (alpha ? GL11.GL_RGBA : GL11.GL_RGB), img.getWidth(), img.getHeight(), alpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
			//glu.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL2GL3.GL_COMPRESSED_RGB, img.getWidth(), img.getHeight(), GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
		}
		return tex;
	}
	
	private static BufferedImage createImage(int width, int height, boolean alpha) {
		if(alpha) {
			return new BufferedImage(glColorModelAlpha, Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null), false, new Hashtable<>());
		}
		return new BufferedImage(glColorModel, Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 3, null), false, new Hashtable<>());
	}
	
}
