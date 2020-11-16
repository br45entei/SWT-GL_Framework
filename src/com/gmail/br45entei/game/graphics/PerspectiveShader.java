package com.gmail.br45entei.game.graphics;

/** A simple Shader program that allows for displaying 3D graphics
 *
 * @since 1.0
 * @author Brian_Entei */
public class PerspectiveShader extends Shader {
	
	/** @param name The name of this shader program */
	public PerspectiveShader(String name) {
		super(name, ""//@formatter:off
				+ "#version 420\n"//+ "#version 130\n"
				//+ "#extension GL_ARB_explicit_attrib_location : require\n"
				+ "\n"
				+ "layout (location=0) in vec3 position;\n"
				+ "layout (location=1) in vec2 texCoord;\n"
				+ "out vec2 tc;\n"// texture Coordinate output to rasterizer for interpolation
				+ "out vec3 interpolatedTextureColor;\n"
				+ "uniform mat4 modelView;\n"
				+ "uniform mat4 projection;\n"
				+ "layout (binding=0) uniform sampler2D samp;\n"// not used in vertex shader
				+ "uniform vec4 textureColor;\n"// not used in vertex shader
				+ "uniform int colorMode;\n"// not used in vertex shader
				+ "\n"
				+ "void main(void) {\n"
				+ "	gl_Position = projection * modelView * vec4(position, 1.0);\n"
				+ "	tc = texCoord;\n"
				+ "	interpolatedTextureColor = textureColor.rgb;"
				+ "}\n"
				+ "\n",
		
				"#version 420\n"//"#version 330\n"
				//+ "#extension GL_ARB_explicit_attrib_location : require\n"
				+ "\n"
				+ "in vec2 tc;\n"// interpolated incoming texture coordinate
				+ "in vec3 interpolatedTextureColor;\n"
				+ "out vec4 color;\n"
				+ "uniform mat4 modelView;\n"
				+ "uniform mat4 projection;\n"
				+ "layout (binding=0) uniform sampler2D samp;\n"
				+ "uniform vec4 textureColor;\n"
				+ "uniform int colorMode;\n"
				+ "\n"
				+ "void main(void) {\n"
				+ "	if(colorMode == 0) {\n"
				+ "		color = texture(samp, tc) * textureColor;\n"
				+ "	} else if(colorMode == 1) {\n"
				+ "		color = texture(samp, tc) * vec4(interpolatedTextureColor, 1.0);\n"
				+ "	} else if(colorMode == 2) {\n"
				+ "		color = texture(samp, tc) * vec4(interpolatedTextureColor, 1.0) * textureColor;\n"
				+ "	} else if(colorMode == 3) {\n"
				+ "		color = textureColor;\n"
				+ "	} else if(colorMode == 4) {\n"
				+ "		color = vec4(interpolatedTextureColor, 1.0);\n"
				+ "	} else {\n"
				+ "		color = vec4(interpolatedTextureColor, 1.0) * textureColor;\n"
				+ "	}\n"
				+ "	\n"
				+ "}\n"
				+ "\n"
		);//@formatter:on
	}
	
	public PerspectiveShader() {
		this("Perspective Shader");
	}
	
	@Override
	public boolean glCompileShader() {
		if(super.glCompileShader()) {
			this.getOrCreateUniform("projection", float[].class, GLUtil.getIdentityf(), false, false);
			this.getOrCreateUniform("modelView", float[].class, GLUtil.getIdentityf(), false, false);
			this.getOrCreateUniform("textureColor", float[].class, new float[] {1.0f, 1.0f, 1.0f, 1.0f}, false, false);
			this.getOrCreateUniform("colorMode", int[].class, new int[] {0}, false, false);
			return true;
		}
		return false;
	}
	
	public float[] getProjection() {
		return this.getUniform("projection", float[].class).getCachedValue();
	}
	
	public float[] setProjection(float[] projection) {
		Uniform<float[]> proj = this.getUniform("projection", float[].class);
		final float[] oldProj = proj.getCachedValue();
		proj.setCachedValue(projection);
		return oldProj;
	}
	
	public float[] glGetProjection() {
		return this.getUniform("projection", float[].class).glGetValue();
	}
	
	public PerspectiveShader glSetProjection(float[] projection) throws IllegalArgumentException {
		if(projection == null) {
			projection = GLUtil.getIdentityf();
		}
		if(projection.length != 16) {
			throw new IllegalArgumentException(String.format("Cannot set the projection matrix uniform to an array of length %s! Projection matrices are 4x4, so their array length should be 16.", Integer.toString(projection.length)));
		}
		this.getUniform("projection", float[].class).glSetValue(projection);
		return this;
	}
	
	public float[] getModelView() {
		return this.getUniform("modelView", float[].class).getCachedValue();
	}
	
	public float[] setModelView(float[] modelView) {
		Uniform<float[]> mod = this.getUniform("modelView", float[].class);
		final float[] oldMod = mod.getCachedValue();
		mod.setCachedValue(modelView);
		return oldMod;
	}
	
	public float[] glGetModelView() {
		return this.getUniform("modelView", float[].class).glGetValue();
	}
	
	public PerspectiveShader glSetModelView(float[] modelView) throws IllegalArgumentException {
		if(modelView == null) {
			modelView = GLUtil.getIdentityf();
		}
		if(modelView.length != 16) {
			throw new IllegalArgumentException(String.format("Cannot set the modelView matrix uniform to an array of length %s! Model view matrices are 4x4, so their array length should be 16.", Integer.toString(modelView.length)));
		}
		this.getUniform("modelView", float[].class).glSetValue(modelView);
		return this;
	}
	
	public PerspectiveShader glSetModelView() {
		return this.glSetModelView(this.getUniform("modelView", float[].class).getCachedValue());
	}
	
	public float[] getColor() {
		return this.getUniform("textureColor", float[].class).getCachedValue();
	}
	
	public float[] setColor(float[] rgba) {
		Uniform<float[]> textureColor = this.getUniform("textureColor", float[].class);
		final float[] oldColor = textureColor.getCachedValue();
		textureColor.setCachedValue(rgba);
		return oldColor;
	}
	
	public float[] setColor(float r, float g, float b, float a) {
		return this.setColor(new float[] {r, g, b, a});
	}
	
	public float[] glGetColor() {
		return this.getUniform("textureColor", float[].class).glGetValue();
	}
	
	public PerspectiveShader glSetColor(float[] rgba) throws IllegalArgumentException {
		if(rgba == null) {
			rgba = GLUtil.getIdentityf();
		}
		if(rgba.length != 4) {
			throw new IllegalArgumentException(String.format("Cannot set the color vector uniform to an array of length %s! The color uniform is a vec4, so its array length should be 4.", Integer.toString(rgba.length)));
		}
		this.getUniform("textureColor", float[].class).glSetValue(rgba);
		return this;
	}
	
	public PerspectiveShader glSetColor(float r, float g, float b, float a) {
		return this.glSetColor(new float[] {r, g, b, a});
	}
	
	public PerspectiveShader glSetColor() {
		return this.glSetColor(this.getUniform("textureColor", float[].class).getCachedValue());
	}
	
	public ColorMode getColorMode() {
		int[] mode = this.getUniform("colorMode", int[].class).getCachedValue();
		return ColorMode.valueOf(mode[0]);
	}
	
	public PerspectiveShader setColorMode(ColorMode mode) {
		mode = mode == null ? ColorMode.COLORED_TEXTURE : mode;
		this.getUniform("colorMode", int[].class).setCachedValue(new int[] {mode.ordinal()});
		return this;
	}
	
	public PerspectiveShader glSetColorMode(ColorMode mode) {
		mode = mode == null ? ColorMode.COLORED_TEXTURE : mode;
		this.getUniform("colorMode", int[].class).glSetValue(new int[] {mode.ordinal()});
		return this;
	}
	
	/** Enum class used to depict the various color modes that a
	 * {@link PerspectiveShader}'s fragment shader can use.
	 *
	 * @since 1.0
	 * @author Brian_Entei */
	public static enum ColorMode {
		COLORED_TEXTURE,
		INTERPOLATED_TEXTURE,
		INTERPOLATED_COLORED_TEXTURE,
		SOLID_COLOR,
		INTERPOLATED,
		INTERPOLATED_COLOR;
		
		public static ColorMode valueOf(int ordinal) {
			for(ColorMode mode : values()) {
				if(mode.ordinal() == ordinal) {
					return mode;
				}
			}
			return null;
		}
		
	}
	
}
