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

import com.gmail.br45entei.game.ui.Window;
import com.gmail.br45entei.util.BufferUtil;
import com.gmail.br45entei.util.CodeUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
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

import org.eclipse.swt.graphics.Rectangle;
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
 *         <b>Modified and expanded upon by Brian_Entei</b> */
@SuppressWarnings("javadoc")
public class FontRender {
	
	public static final int charBegin = 0;//32
	public static final int charEnd = 256;//128
	public static final int charLength = charEnd - charBegin;
	
	//private static final File cacheDir = new File("/path/to/font/cache/dir/");
	
	private FontRender() {
	}
	
	protected static class Glyph {
		float x, y, w, h;
		final char c;
		int list = -1;
		
		public Glyph(char c) {
			this.c = c;
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
	
	/** @author jezek2, Brian_Entei */
	public static class GLFont {
		
		protected static final ConcurrentLinkedDeque<GLFont> instances = new ConcurrentLinkedDeque<>();
		
		public final String name;
		public final boolean bold;
		public final boolean italic;
		public final boolean antialiasing;
		public final boolean usesFractionalMetrics;
		
		protected int texture;
		protected int width, height;
		protected Glyph[] glyphs = new Glyph[charLength];
		
		public final int size;
		
		public GLFont(String name, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics) {
			this.name = name;
			this.size = size;
			this.bold = bold;
			this.italic = italic;
			this.antialiasing = antialiasing;
			this.usesFractionalMetrics = usesFractionalMetrics;
			for(int i = 0; i < this.glyphs.length; i++) {
				this.glyphs[i] = new Glyph((char) i);
			}
			instances.add(this);
		}
		
		public GLFont(String name, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics, InputStream in) throws IOException {
			this(name, size, bold, italic, antialiasing, usesFractionalMetrics);
			load(in);
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
			return this.size - (this.size * 0.094875);//0.098
		}
		
		public void destroy() {
			GL11.glDeleteTextures(this.texture);
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
				out.write(data);
				
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
			load(new FileInputStream(f));
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
				
				ByteBuffer buf = BufferUtil.createDirectByteBuffer(size);
				byte[] data = new byte[size];
				in.read(data);
				buf.put(data);
				
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
	
	public static GLFont createFont(String family, int size, boolean bold, boolean italic, boolean antialiasing, boolean usesFractionalMetrics) {
		GLFont gf = new GLFont(family, size, bold, italic, antialiasing, usesFractionalMetrics);
		/*File f = new File(cacheDir, getFontFileName(family, size, bold));
		if (f.exists()) {
			gf.load(f);
			return gf;
		}*/
		int style = bold ? Font.BOLD : Font.PLAIN;
		style = italic ? style | Font.ITALIC : style;
		BufferedImage img = renderFont(new Font(family, style, size), antialiasing, gf.glyphs, usesFractionalMetrics);
		gf.texture = createTexture(img, false);
		gf.width = img.getWidth();
		gf.height = img.getHeight();
		//gf.save(f);
		return gf;
	}
	
	public static BufferedImage renderFont(Font font, boolean antialiasing, Glyph[] glyphs, boolean usesFractionalMetrics) {
		FontRenderContext frc = new FontRenderContext(null, antialiasing, usesFractionalMetrics);
		
		int imgw = charLength * 2;
		if(font.getSize() >= 36) imgw <<= 1;
		if(font.getSize() >= 72) imgw <<= 1;
		
		//BufferedImage img = new BufferedImage(imgw, charLength * 8, BufferedImage.TYPE_INT_ARGB);
		BufferedImage img = createImage(imgw, charLength * 8, true);
		Graphics2D g = (Graphics2D) img.getGraphics();
		
		if(antialiasing) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		
		g.setColor(Color.WHITE);
		g.setFont(font);
		
		int x = 0, y = 0, rowsize = 0;
		for(int c = charBegin; c < charEnd; c++) {
			String s = Character.toString((char) c);
			Rectangle2D rect = font.getStringBounds(s, frc);
			LineMetrics lm = font.getLineMetrics(s, frc);
			int w = (int) rect.getWidth() + 2;
			int h = (int) rect.getHeight() + 2;
			
			if(x + w + 2 > img.getWidth()) {
				x = 0;
				y += rowsize;
				rowsize = 0;
			}
			g.drawString(s, x, y + (int) lm.getAscent() + 1);
			
			if(glyphs != null) {
				glyphs[c - charBegin].x = x;
				glyphs[c - charBegin].y = y;
				glyphs[c - charBegin].w = w * (c == '\t' ? 4.0f : 1.0f);
				glyphs[c - charBegin].h = h;
			}
			
			w += 2;
			h += 2;
			
			x += w;
			rowsize = Math.max(rowsize, h);
		}
		
		y += rowsize;
		g.dispose();
		
		if(y < charEnd)
			img = img.getSubimage(0, 0, img.getWidth(), charEnd);
		else if(y < charLength * 2)
			img = img.getSubimage(0, 0, img.getWidth(), charLength * 2);
		else if(y < charLength * 4) img = img.getSubimage(0, 0, img.getWidth(), charLength * 4);
		return img;
	}
	
	private static void renderGlyph(GLFont font, Glyph g) {
		if(g.list != -1) {
			GL11.glCallList(g.list);
			return;
		}
		
		g.list = GL11.glGenLists(1);
		GL11.glNewList(g.list, GL11.GL_COMPILE);
		
		float tw = font.width;// 512.0
		float th = font.height;//256.0
		
		CodeUtil.printlnDebug(((int) g.c) + "(\"" + g.c + "\"): " + tw + ", " + th + ", " + g.x + ", " + g.y + ", " + g.w + ", " + g.h);
		
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
		GL11.glCallList(g.list);
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
		
		double smallestWidth = 0;
		double smallestHeight = 0;
		double largestWidth = sizeWidth;
		double largestHeight = sizeHeight;
		for(int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			if(c < charBegin || c > charEnd) c = '?';
			Glyph g = font.glyphs[c - charBegin];
			double sW = g.w + (g.w * 0.3);
			if(c == '\b') {
				sizeWidth -= sW * scaleX;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else if(c == '\r') {
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
				sizeWidth = 0;
			} else if(c == '\n') {
				sizeHeight += font.getLineHeight() * scaleY;
				if(sizeHeight < smallestHeight) {
					smallestHeight = sizeHeight;
				} else if(sizeHeight > largestHeight) {
					largestHeight = sizeHeight;
				}
			} else if(c == '\t') {
				sizeWidth += sW * scaleX;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else {
				sizeWidth += sW * scaleX;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			}
		}
		return new GLFontBounds(x, y, smallestWidth * 0.7, smallestHeight * 0.7, largestWidth * 0.7, largestHeight * 0.7);//Wonder why it needs to be multiplied by 0.7 ...
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s, double x, double y, double red, double green, double blue, double alpha) {
		return drawString(font, s, x, y, red, green, blue, alpha, 1.0, 1.0, 1.0);
	}
	
	public static GLFontBounds drawString(GLFont font, CharSequence s, double x, double y, double red, double green, double blue, double alpha, double scaleX, double scaleY, double scaleZ) {
		Rectangle vp = Window.getWindow().getViewport();
		double width = (Math.max(1.0, vp.width) + 0.0);
		double height = (Math.max(1.0, vp.height) + 0.0);
		
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
		double lineHeight = font.getLineHeightRender();
		double sizeWidth = 0;
		double sizeHeight = font.getLineHeight();
		
		double smallestWidth = 0;
		double smallestHeight = 0;
		double largestWidth = sizeWidth;
		double largestHeight = sizeHeight;
		for(int i = 0, n = s.length(); i < n; i++) {
			char c = s.charAt(i);
			if(c < charBegin || c > charEnd) c = '?';
			Glyph g = font.glyphs[c - charBegin];
			double w = g.w - (g.w * 0.15);
			double sW = g.w + (g.w * 0.3);
			if(c == '\b') {
				GL11.glTranslated(-Math.round(w), 0, 0);
				lineWidth -= Math.round(w);
				sizeWidth -= sW;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else if(c == '\r') {
				GL11.glTranslated(-Math.round(lineWidth), 0, 0);
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
				lineWidth = 0;
				sizeWidth = 0;
			} else if(c == '\n') {
				GL11.glTranslated(0, Math.round(font.getLineHeightRender()), 0);
				lineHeight += Math.round(font.getLineHeightRender());
				sizeHeight += font.getLineHeight();
				if(sizeHeight < smallestHeight) {
					smallestHeight = sizeHeight;
				} else if(sizeHeight > largestHeight) {
					largestHeight = sizeHeight;
				}
			} else if(c == '\t') {
				GL11.glTranslated(Math.round(w), 0, 0);
				lineWidth += Math.round(w);
				sizeWidth += sW;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			} else {
				renderGlyph(font, g);
				GL11.glTranslated(Math.round(w), 0, 0);
				lineWidth += Math.round(w);
				sizeWidth += sW;
				if(sizeWidth < smallestWidth) {
					smallestWidth = sizeWidth;
				} else if(sizeWidth > largestWidth) {
					largestWidth = sizeWidth;
				}
			}
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GLUtil.glPopColor();
		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_BLEND);
		GLUtil.glPopBlendMode();
		GLUtil.glPopCullMode();
		return new GLFontBounds(x, y, smallestWidth * 0.7, smallestHeight, largestWidth * 0.7, largestHeight);//Wonder why it needs to be multiplied by 0.7 ...
	}
	
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
		
		ByteBuffer buf = (ByteBuffer) ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder()).put(data, 0, data.length).flip();
		
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
		return new BufferedImage(alpha ? glColorModelAlpha : glColorModel, Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, alpha ? 4 : 3, null), false, new Hashtable<>());
	}
	
}
