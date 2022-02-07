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

import com.gmail.br45entei.game.math.Matrix4f;
import com.gmail.br45entei.util.BufferUtil;
import com.gmail.br45entei.util.CodeUtil;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.glu.Util;

/** Shader is a class which can load and compile GLSL code which can then be
 * used to render scenes on the GPU.
 *
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class Shader {
	
	private static volatile Shader activeShader = null;
	
	private volatile String name;
	private volatile String[] vertexShaderSource;
	private volatile String[] fragmentShaderSource;
	
	protected volatile int program;
	protected volatile int vertexShader;
	protected volatile int fragmentShader;
	
	private volatile String vertexShaderErr;
	private volatile String fragmentShaderErr;
	private volatile String programErr;
	private volatile String vertexShaderLog;
	private volatile String fragmentShaderLog;
	private volatile String programLog;
	
	private volatile boolean vertexCompiled, fragmentCompiled, programLinked;
	
	private final ConcurrentHashMap<String, Uniform<?>> uniforms = new ConcurrentHashMap<>();
	
	/** @param name The name of this shader program
	 * @param vertexShaderSource The vertex shader's source code
	 * @param fragmentShaderSource The fragment shader's source code */
	public Shader(String name, String[] vertexShaderSource, String[] fragmentShaderSource) {
		this.name = name;
		this.vertexShaderSource = vertexShaderSource;
		this.fragmentShaderSource = fragmentShaderSource;
	}
	
	private static final String[] shaderSourceToArray(String source) {
		List<String> lines = new ArrayList<>();
		for(String line : source.split(Pattern.quote("\n"))) {
			lines.add(line.trim().concat("\n"));
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	/** @param name The name of this shader program
	 * @param vertexShaderSource The vertex shader's source code
	 * @param fragmentShaderSource The fragment shader's source code */
	public Shader(String name, String vertexShaderSource, String fragmentShaderSource) {
		this(name, shaderSourceToArray(vertexShaderSource), shaderSourceToArray(fragmentShaderSource));
	}
	
	public final Shader glUpdateUniforms() throws IllegalArgumentException, ClassCastException {
		for(Uniform<?> uniform : this.uniforms.values()) {
			uniform.glSetValue();
		}
		return this;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public Shader setName(String name) {
		this.name = name;
		return this;
	}
	
	private final boolean glCompileShader(boolean vertexOrFragment) {
		getOpenGLError();
		
		int shader = GL20.glCreateShader(vertexOrFragment ? GL20.GL_VERTEX_SHADER : GL20.GL_FRAGMENT_SHADER);
		GL20.glShaderSource(shader, vertexOrFragment ? this.vertexShaderSource : this.fragmentShaderSource);
		GL20.glCompileShader(shader);
		
		String err = getOpenGLError();
		String log = getShaderLog(shader);
		final int[] shaderCompiled = new int[1];
		GL20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, shaderCompiled);
		
		if(vertexOrFragment) {
			this.vertexShader = shader;
			this.vertexShaderErr = err;
			this.vertexShaderLog = log;
		} else {
			this.fragmentShader = shader;
			this.fragmentShaderErr = err;
			this.fragmentShaderLog = log;
		}
		return shaderCompiled[0] == GL11.GL_TRUE;
	}
	
	public boolean isCompiled() {
		return this.vertexCompiled && this.fragmentCompiled && this.programLinked && this.program != 0;
	}
	
	public Shader glDisposeProgram() {
		if(GL20.glIsShader(this.vertexShader)) {
			GL20.glDeleteShader(this.vertexShader);
		}
		this.vertexShader = 0;
		this.vertexCompiled = false;
		
		if(GL20.glIsShader(this.fragmentShader)) {
			GL20.glDeleteShader(this.fragmentShader);
		}
		this.fragmentShader = 0;
		this.fragmentCompiled = false;
		
		if(GL20.glIsProgram(this.program)) {
			GL20.glDeleteProgram(this.program);
		}
		this.program = 0;
		this.programLinked = false;
		return this;
	}
	
	public boolean glCompileShader() {
		this.vertexCompiled = this.glCompileShader(true);
		this.fragmentCompiled = this.glCompileShader(false);
		
		if(this.vertexCompiled && this.fragmentCompiled) {
			try {
				this.program = GL20.glCreateProgram();
				GL20.glAttachShader(this.program, this.vertexShader);
				GL20.glAttachShader(this.program, this.fragmentShader);
				GL20.glLinkProgram(this.program);
				this.programErr = getOpenGLError();
				this.programLog = getProgramLog(this.program);
				final int[] linked = new int[1];
				GL20.glGetProgramiv(this.program, GL20.GL_LINK_STATUS, linked);
				this.programLinked = linked[0] == GL11.GL_TRUE;
				
				return this.programLinked;
			} finally {
				GL20.glDeleteShader(this.vertexShader);
				GL20.glDeleteShader(this.fragmentShader);
				this.vertexShader = 0;
				this.fragmentShader = 0;
				
				if(!this.programLinked) {
					GL20.glDeleteProgram(this.program);
					this.program = 0;
				}
			}
		}
		this.programLinked = false;
		this.programErr = this.programLog = null;
		return false;
	}
	
	public final String getCompileLog() {
		final String lineSeparator = CodeUtil.getProperty("line.separator");
		if(this.vertexCompiled && this.fragmentCompiled) {
			if(this.programLinked) {
				return null;
			}
			return String.format("Failed to link program \"%s\"%sVertex Shader Log:%s%s%sFragment Shader Log:%s%s%sProgram Linkage Log: %s", this.name, lineSeparator, lineSeparator, this.vertexShaderLog, lineSeparator, lineSeparator, this.fragmentShaderLog, lineSeparator, this.programLog);
		}
		String log = this.vertexCompiled ? "" : "Vertex Shader Log:".concat(lineSeparator).concat(this.vertexShaderLog).concat(lineSeparator);
		log = log.concat(this.fragmentCompiled ? "" : "Fragment Shader Log:".concat(lineSeparator).concat(this.fragmentShaderLog).concat(lineSeparator));
		return String.format("Failed to compile %s:%s%s", !this.vertexCompiled && !this.fragmentCompiled ? "both the vertex and fragment shaders" : String.format("the %s shader", !this.vertexCompiled ? "vertex" : "fragment"), lineSeparator, log);
	}
	
	public final Shader printLog(PrintStream pr) {
		pr.println(this.getCompileLog());
		pr.flush();
		return this;
	}
	
	public final Shader printLog() {
		return this.printLog(System.out);
	}
	
	protected static final ByteBuffer nullTerminatedCharSequence(CharSequence seq) {
		byte[] stringBytes = seq.toString().getBytes(StandardCharsets.ISO_8859_1);
		byte[] ntBytes = new byte[stringBytes.length + 1];
		System.arraycopy(stringBytes, 0, ntBytes, 0, stringBytes.length);
		ByteBuffer buf = BufferUtils.createByteBuffer(ntBytes.length);
		return buf.rewind().put(ntBytes).rewind();
	}
	
	public int glGetUniformLocation(String name) {
		if(!GL20.glIsProgram(this.program)) {
			return -1;
		}
		return GL20.glGetUniformLocation(this.program, nullTerminatedCharSequence(name));
	}
	
	public boolean bind() {
		if(this.program != 0) {//if(GL20.glIsProgram(this.program)) {
			GL20.glUseProgram(this.program);
			activeShader = this;
			return true;
		}
		if(activeShader == this) {
			unbind();
		}
		return false;
	}
	
	public boolean isBound() {
		boolean isBound = activeShader == this;
		/*if(!isBound && GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) == this.program) {
			activeShader = this;
			return true;
		}*/
		return isBound;
	}
	
	public static final void unbind() {
		GL20.glUseProgram(0);
		activeShader = null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Uniform<T> getOrCreateUniform(String name, Class<T> dataType, T def, boolean transpose, boolean matrix2x2fOrVec4f) throws IllegalStateException, NullPointerException, ClassCastException, IllegalArgumentException {
		if(!GL20.glIsProgram(this.program)) {
			throw new IllegalStateException(String.format("Cannot get or create a uniform for the shader \"%s\" if it has not been successfully compiled!"));
		}
		if(name == null || name.isEmpty()) {
			throw new NullPointerException("Uniform name must be non-null and non-empty!");
		}
		Uniform<?> check = this.uniforms.get(name);
		if(check != null) {
			if(check.dataType.isAssignableFrom(dataType)) {
				return (Uniform<T>) check;
			}
			throw new IllegalArgumentException(String.format("A uniform named \"%s\" already exists in the shader \"%s\", however its dataType (\"%s\") does not match the one specified (\"%s\")!", name, this.name, check.dataType.getName(), dataType.getName()));
		}
		if(dataType == null) {
			throw new NullPointerException("The uniform's dataType must not be null!");
		}
		if(def == null) {
			throw new NullPointerException("The uniform's default value 'def' must not be null!");
		}
		Uniform<T> uniform = new Uniform<>(this, name, dataType, def, transpose, matrix2x2fOrVec4f);
		this.uniforms.put(name, uniform);
		return uniform;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Uniform<T> getUniform(String name, Class<T> dataType) {
		Uniform<?> check = this.uniforms.get(name);
		if(check != null) {
			if(check.dataType.isAssignableFrom(dataType)) {
				return (Uniform<T>) check;
			}
		}
		return null;
	}
	
	public Uniform<?> getUniform(String name) {
		return this.uniforms.get(name);
	}
	
	public static final String getShaderLog(int shader) {
		int[] len = new int[1];
		GL20.glGetShaderiv(shader, GL20.GL_INFO_LOG_LENGTH, len);
		if(len[0] == 0) {
			return "";
		}
		return GL20.glGetShaderInfoLog(shader);
	}
	
	public static final String getProgramLog(int program) {
		int[] len = new int[1];
		GL20.glGetProgramiv(program, GL20.GL_INFO_LOG_LENGTH, len);
		if(len[0] == 0) {
			return "";
		}
		return GL20.glGetProgramInfoLog(program);
	}
	
	public static final String getOpenGLError() {
		final String lineSeparator = CodeUtil.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		int error;
		while((error = GL11.glGetError()) != GL11.GL_NO_ERROR) {
			sb.append(Util.translateGLErrorString(error)).append(lineSeparator);
		}
		return sb.toString();
	}
	
	public static final String getProgramLog(Shader shader) {
		return getProgramLog(shader.program);
	}
	
	/** Uniform is a class which handles the passage of data to and from a
	 * compiled shader program.
	 *
	 * @since 1.0
	 * @author Brian_Entei
	 * @param <T> The Java data type representing the GLSL data type for this
	 *            Uniform */
	public static final class Uniform<T> {
		
		public final Shader shader;
		public final String name;
		public final int location;
		public final Class<T> dataType;
		private final T def;
		private volatile T value;
		private final boolean matrix2x2f;
		private volatile boolean transpose = false;
		
		public Uniform(Shader shader, String name, Class<T> dataType, T def, boolean transpose, boolean matrix2x2f) throws IllegalArgumentException, ClassCastException, IllegalStateException {
			this.shader = shader;
			if(this.shader == null) {
				throw new NullPointerException("Shader cannot be null!");
			}
			if(!GL20.glIsProgram(this.shader.program)) {
				throw new IllegalArgumentException(String.format("The shader \"%s\" is either not compiled or invalid!", this.shader.getName()));
			}
			this.name = name;
			this.location = this.shader.glGetUniformLocation(name);
			if(this.location == -1) {
				throw new IllegalArgumentException(String.format("The uniform \"%s\" is not defined in the shader program \"%s\"!", name, this.shader.getName()));
			}
			this.dataType = dataType;
			if(this.dataType == null) {
				throw new NullPointerException("The uniform's dataType must not be null!");
			}
			this.value = this.def = def;
			if(this.def == null) {
				throw new NullPointerException("The uniform's default value 'def' must not be null!");
			}
			this.matrix2x2f = matrix2x2f;
			this.transpose = transpose;
			this.glSetValue(null);
		}
		
		public boolean getTranspose() {
			return this.transpose;
		}
		
		public Uniform<T> setTranspose(boolean transpose) {
			this.transpose = transpose;
			return this;
		}
		
		public T getDefaultValue() {
			return this.def;
		}
		
		public T getCachedValue() {
			return this.value;
		}
		
		public Uniform<T> setCachedValue(T value) {
			if(value == null) {
				value = this.def;
			}
			if(this.def instanceof float[]) {
				float[] def = (float[]) this.def;
				float[] val = (float[]) value;
				if(val.length != def.length) {
					throw new IllegalArgumentException(String.format("Invalid [float] array length specified for uniform \"%s\"! Expected %s; got %s", this.name, Integer.toString(def.length), Integer.toString(val.length)));
				}
			} else if(this.def instanceof int[]) {
				int[] def = (int[]) this.def;
				int[] val = (int[]) value;
				if(val.length != def.length) {
					throw new IllegalArgumentException(String.format("Invalid [int] array length specified for uniform \"%s\"! Expected %s; got %s", this.name, Integer.toString(def.length), Integer.toString(val.length)));
				}
			} else if(this.def instanceof double[]) {
				double[] def = (double[]) this.def;
				double[] val = (double[]) value;
				if(val.length != def.length) {
					throw new IllegalArgumentException(String.format("Invalid [double] array length specified for uniform \"%s\"! Expected %s; got %s", this.name, Integer.toString(def.length), Integer.toString(val.length)));
				}
			}
			this.value = value;
			return this;
		}
		
		/** @return This uniform's value, retrieved from the GPU
		 * @throws ClassCastException Thrown if an unknown data type was used to
		 *             move data */
		@SuppressWarnings("unchecked")
		public T glGetValue() throws ClassCastException {
			final boolean wasBound = this.shader.isBound();
			if(!wasBound) {
				this.shader.bind();
			}
			
			try {
				if(Integer.TYPE.isAssignableFrom(this.dataType)) {
					return this.value = (T) Integer.valueOf(GL20.glGetUniformi(this.shader.program, this.location));
				} else if(int[].class.isAssignableFrom(this.dataType)) {
					int[] def = (int[]) this.def;
					int[] val = new int[def.length];
					switch(def.length) {
					case 1:
					case 2:
					case 3:
					case 4:
						GL20.glGetUniformiv(this.shader.program, this.location, val);
						this.value = (T) val;
						break;
					default:
						throw new IllegalArgumentException(String.format("Invalid default [int] array length specified for uniform \"%s\"! Expected 1, 2, 3, or 4; got %s", this.name, Integer.toString(val.length)));
					}
					return this.value;
				} else if(Float.TYPE.isAssignableFrom(this.dataType)) {
					return this.value = (T) Float.valueOf(GL20.glGetUniformf(this.shader.program, this.location));
				} else if(float[].class.isAssignableFrom(this.dataType)) {
					float[] def = (float[]) this.def;
					float[] val = new float[def.length];
					switch(def.length) {
					case 1:
					case 2:
					case 3:
					case 4:
					case 9:
					case 16:
						GL20.glGetUniformfv(this.shader.program, this.location, val);
						this.value = (T) val;
						break;
					default:
						throw new IllegalArgumentException(String.format("Invalid default [float] array length specified for uniform \"%s\"! Expected 1, 2, 3, 4, 9, or 16; got %s", this.name, Integer.toString(val.length)));
					}
					return this.value;
				} else if(Matrix4f.class.isAssignableFrom(this.dataType)) {
					float[] val = new float[16];
					GL20.glGetUniformfv(this.shader.program, this.location, val);
					return this.value = (T) new Matrix4f(val);
				} else if(org.lwjgl.util.vector.Matrix4f.class.isAssignableFrom(this.dataType)) {
					float[] val = new float[16];
					GL20.glGetUniformfv(this.shader.program, this.location, val);
					return this.value = (T) new org.lwjgl.util.vector.Matrix4f().load(BufferUtil.wrap(val));
				} else if(GLUtil.isGL40Available() && (Double.TYPE.isAssignableFrom(this.dataType) || double[].class.isAssignableFrom(this.dataType))) {
					if(double[].class.isAssignableFrom(this.dataType)) {
						double[] def = (double[]) this.def;
						double[] val = new double[def.length];
						switch(def.length) {
						case 1:
						case 2:
						case 3:
						case 4:
						case 9:
						case 16:
							GL40.glGetUniformdv(this.shader.program, this.location, val);
							this.value = (T) val;
							break;
						default:
							throw new IllegalArgumentException(String.format("Invalid default [double] array length specified for uniform \"%s\"! Expected 1, 2, 3, 4, 9, or 16; got %s", this.name, Integer.toString(val.length)));
						}
						return this.value;
					}
					return this.value = (T) Double.valueOf(GL40.glGetUniformd(this.shader.program, this.location));
				}
				throw new IllegalArgumentException(String.format("Invalid data type specified for uniform \"%s\"! Expected Integer, Float, int[], or float[]; got %s", this.name, this.dataType.getName()));
			} finally {
				if(!wasBound) {
					Shader.unbind();
				}
			}
		}
		
		/** @param value This uniform's new value that will be sent to the GPU
		 * @return This uniform's previously cached value
		 * @throws IllegalArgumentException Thrown if this uniform represents a
		 *             vector or matrix and an array of the wrong length was
		 *             provided
		 * @throws ClassCastException Thrown if an unknown data type was used to
		 *             move data
		 * @throws IllegalStateException Thrown if an unknown/invalid data type
		 *             was used to create this uniform object */
		public T glSetValue(T value) throws IllegalArgumentException, ClassCastException, IllegalStateException {
			final boolean wasBound = this.shader.isBound();
			try {
				if(!wasBound) {
					this.shader.bind();
				}
				
				final T oldVal = this.value;
				if(value == null) {
					value = this.def;
				}
				if(Integer.TYPE.isAssignableFrom(this.dataType)) {
					Integer val = (Integer) value;
					GL20.glUniform1i(this.location, val.intValue());
				} else if(int[].class.isAssignableFrom(this.dataType)) {
					int[] def = (int[]) this.def;
					int[] val = (int[]) value;
					if(val.length != def.length) {
						throw new IllegalArgumentException(String.format("Invalid [int] array length specified for uniform \"%s\"! Expected %s; got %s", this.name, Integer.toString(def.length), Integer.toString(val.length)));
					}
					switch(val.length) {
					case 1:
						GL20.glUniform1iv(this.location, val);
						break;
					case 2:
						GL20.glUniform2iv(this.location, val);
						break;
					case 3:
						GL20.glUniform3iv(this.location, val);
						break;
					case 4:
						GL20.glUniform4iv(this.location, val);
						break;
					default:
						throw new IllegalArgumentException(String.format("Invalid default [int] array length specified for uniform \"%s\"! Expected 1, 2, 3, or 4; got %s", this.name, Integer.toString(val.length)));
					}
				} else if(Float.TYPE.isAssignableFrom(this.dataType)) {
					Float val = (Float) value;
					GL20.glUniform1f(this.location, val.floatValue());
				} else if(float[].class.isAssignableFrom(this.dataType)) {
					float[] def = (float[]) this.def;
					float[] val = (float[]) value;
					if(val.length != def.length) {
						throw new IllegalArgumentException(String.format("Invalid [float] array length specified for uniform \"%s\"! Expected %s; got %s", this.name, Integer.toString(def.length), Integer.toString(val.length)));
					}
					switch(val.length) {
					case 1:
						GL20.glUniform1fv(this.location, val);
						break;
					case 2:
						GL20.glUniform2fv(this.location, val);
						break;
					case 3:
						GL20.glUniform3fv(this.location, val);
						break;
					case 4:
						if(this.matrix2x2f) {
							GL20.glUniformMatrix2fv(this.location, this.transpose, val);
						} else {
							GL20.glUniform4fv(this.location, val);
						}
						break;
					case 9:
						GL20.glUniformMatrix3fv(this.location, this.transpose, val);
						break;
					case 16:
						GL20.glUniformMatrix4fv(this.location, this.transpose, val);
						break;
					default:
						throw new IllegalArgumentException(String.format("Invalid default [float] array length specified for uniform \"%s\"! Expected 1, 2, 3, 4, 9, or 16; got %s", this.name, Integer.toString(val.length)));
					}
				} else if(Matrix4f.class.isAssignableFrom(this.dataType)) {
					Matrix4f val = (Matrix4f) value;
					FloatBuffer buf = BufferUtils.createFloatBuffer(16);//BufferUtil.createDirectFloatBuffer(16);
					val.getMBuffer(buf);
					GL20.glUniformMatrix4fv(this.location, this.transpose, buf.rewind());
				} else if(org.lwjgl.util.vector.Matrix4f.class.isAssignableFrom(this.dataType)) {
					org.lwjgl.util.vector.Matrix4f val = (org.lwjgl.util.vector.Matrix4f) value;
					float[] m = new float[] {//@formatter:off
						val.m00, val.m01, val.m02, val.m03,
						val.m10, val.m11, val.m12, val.m13,
						val.m20, val.m21, val.m22, val.m23,
						val.m30, val.m31, val.m32, val.m33
					};//@formatter:on
					GL20.glUniformMatrix4fv(this.location, this.transpose, m);
				} else if(GLUtil.isGL40Available() && (Double.TYPE.isAssignableFrom(this.dataType) || double[].class.isAssignableFrom(this.dataType))) {
					if(double[].class.isAssignableFrom(this.dataType)) {
						double[] def = (double[]) this.def;
						double[] val = (double[]) value;
						if(val.length != def.length) {
							throw new IllegalArgumentException(String.format("Invalid [double] array length specified for uniform \"%s\"! Expected %s; got %s", this.name, Integer.toString(def.length), Integer.toString(val.length)));
						}
						switch(val.length) {
						case 1:
							GL40.glUniform1dv(this.location, val);
							break;
						case 2:
							GL40.glUniform2dv(this.location, val);
							break;
						case 3:
							GL40.glUniform3dv(this.location, val);
							break;
						case 4:
							if(this.matrix2x2f) {
								GL40.glUniformMatrix2dv(this.location, this.transpose, val);
							} else {
								GL40.glUniform4dv(this.location, val);
							}
							break;
						case 9:
							GL40.glUniformMatrix3dv(this.location, this.transpose, val);
							break;
						case 16:
							GL40.glUniformMatrix4dv(this.location, this.transpose, val);
							break;
						default:
							throw new IllegalArgumentException(String.format("Invalid default [double] array length specified for uniform \"%s\"! Expected 1, 2, 3, 4, 9, or 16; got %s", this.name, Integer.toString(val.length)));
						}
					} else {
						Double val = (Double) value;
						GL40.glUniform1d(this.location, val.doubleValue());
					}
				} else {
					throw new IllegalStateException(String.format("Invalid/Unimplemented data type specified for uniform \"%s\"! Expected Integer, int[], Float, float[], Double, or double[]; got %s", this.name, this.dataType.getName()));
				}
				
				this.value = value;
				return oldVal;
			} finally {
				if(!wasBound) {
					Shader.unbind();
				}
			}
		}
		
		public Uniform<T> glSetValue() throws IllegalArgumentException, ClassCastException {
			this.glSetValue(this.value);
			return this;
		}
		
	}
	
}
