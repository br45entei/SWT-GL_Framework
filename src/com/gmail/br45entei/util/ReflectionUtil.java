/*******************************************************************************
 * 
 * Copyright © 2021 Brian_Entei (br45entei@gmail.com)
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
package com.gmail.br45entei.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** ReflectionUtil is a class mostly used to locate platform-specific classes,
 * methods, and fields.<br>
 * <br>
 * <b>Note:</b>&nbsp;While this class isn't API, you are free to use it as such;
 * however one shouldn't rely on reflection unless it is the last option
 * available, as things can change under the hood, causing your code to break.
 *
 * @since 1.0
 * @author Brian_Entei */
public class ReflectionUtil {
	
	/** Finds and returns the requested class given its fully qualified
	 * name.<br>
	 * This function is equivalent to calling {@link Class#forName(String)},
	 * except that it is wrapped in a <tt>try-catch</tt> statement.
	 *
	 * @param name The fully qualified name of the desired class
	 * @return The desired class, if found, or <tt><b>null</b</tt> otherwise */
	public static final Class<?> getClass(String name) {
		try {
			return Class.forName(name);
		} catch(LinkageError | ClassNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/** Compares two classes to see if they describe the same {@link Class}.<br>
	 * This method first checks whether or not either field is
	 * <tt><b>null</b></tt>, then checks if the two classes are equivalent by
	 * calling {@link Class#isAssignableFrom(Class)
	 * class1.isAssignableFrom(class2)} and then
	 * {@link Class#isAssignableFrom(Class)
	 * class2.isAssignableFrom(class1)}.<br>
	 * If either of the two classes are <tt><b>null</b></tt>, then <tt>true</tt>
	 * is returned if and only if both classes are <tt><b>null</b></tt>,
	 * otherwise <tt>false</tt> is returned.
	 *
	 * @param class1 The first class to compare
	 * @param class2 The second class to compare
	 * @return Whether or not the two specified classes are equal to each
	 *         other */
	public static final boolean equals(Class<?> class1, Class<?> class2) {
		if(class1 == null) {
			if(class2 == null) {
				return true;
			}
			return false;
		}
		if(class2 == null) {
			return false;
		}
		return class1.isAssignableFrom(class2) && class2.isAssignableFrom(class1);
	}
	
	/** Searches for and returns the requested field given its name and the
	 * class it is defined in.<br>
	 * This function iterates through all of the
	 * {@link Class#getDeclaredFields() declared fields} in the specified class
	 * and returns the first one whose name matches the specified name.
	 *
	 * @param clazz The class whose field is desired
	 * @param name The name of the declared field within the <tt>clazz</tt>
	 * @return The desired {@link Field}, if found, or <tt><b>null</b</tt>
	 *         otherwise */
	public static final Field getField(Class<?> clazz, String name) {
		for(Field field : clazz.getDeclaredFields()) {
			if(field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}
	
	/** Attempts to obtain and return the requested field's value from the
	 * specified object (or class for static fields when <tt>obj</tt> is
	 * <tt><b>null</b></tt>).<br>
	 * This function is equivalent to calling {@link Field#get(Object)}, except
	 * that wraps the call in a <tt>try-catch</tt> statement, and it first
	 * attempts to set the field as {@link Field#setAccessible(boolean)
	 * accessible} before the call, and sets the field's original accessible
	 * value after the call.
	 *
	 * @param field The field whose value is desired
	 * @param obj The object whose field's value is desired (use
	 *            <tt><b>null</b></tt> when accessing a static field)
	 * @return The specified field's value, or <tt><b>null</b></tt> if an
	 *         exception occurred while attempting to retrieve the value */
	public static final Object getValue(Field field, Object obj) {
		boolean accessible = field.canAccess(obj);
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch(SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				field.setAccessible(accessible);
			} catch(SecurityException ignored) {
			}
		}
	}
	
	/** Searches for a field with the specified name within the specified class,
	 * and then attempts to obtain and return the field's value from the
	 * specified object (or class for static fields when <tt>obj</tt> is
	 * <tt><b>null</b></tt>).<br>
	 * This method is equivalent to performing the following:<br>
	 * <br>
	 * <tt>Field field = {@link ReflectionUtil#getField(Class, String)};<br>
	 * Object value;<br>
	 * if(field != null) {<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;value = {@link #getValue(Field, Object) ReflectionUtil.getValue(field, Object)};<br>
	 * } else {<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;value = null<br>
	 * }</tt>
	 *
	 * @param clazz The class which declares the field whose value is desired
	 * @param fieldName The name of the field whose value is desired
	 * @param obj The object whose field's value is desired (use
	 *            <tt><b>null</b></tt> when accessing a static field)
	 * @return The targeted field's value, or <tt><b>null</b></tt> if either the
	 *         specified field could not be found, or an exception occurred
	 *         while attempting to retrieve the value */
	public static final Object getValue(Class<?> clazz, String fieldName, Object obj) {
		Field field = getField(clazz, fieldName);
		return field == null ? null : getValue(field, obj);
	}
	
	/** Searches for the desired method within the specified class and returns
	 * it.<br>
	 * This function iterates over the class' {@link Class#getDeclaredMethods()
	 * declared methods} and returns the first method it finds one whose name
	 * and parameter types (if any) match.
	 *
	 * @param clazz The class which declares the desired method
	 * @param name The name of the desired method
	 * @param parameterTypes The method's parameters' {@link Class} types (for
	 *            primitive values, use &lt;their object counterpart&gt;.TYPE;
	 *            e.g. for a parameter of type <tt>short</tt>, use
	 *            {@link Short#TYPE})
	 * @return The desired {@link Method}, if found, or <tt><b>null</b></tt>
	 *         otherwise */
	public static final Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		for(Method method : clazz.getDeclaredMethods()) {
			Class<?>[] checkParamTypes = method.getParameterTypes();
			if(method.getName().equals(name) && checkParamTypes.length == parameterTypes.length) {
				boolean matches = true;
				for(int i = 0; i < parameterTypes.length; i++) {
					Class<?> paramType = parameterTypes[i];
					Class<?> checkParamType = checkParamTypes[i];
					
					if(!equals(paramType, checkParamType)) {
						matches = false;
						break;
					}
				}
				if(!matches) {
					continue;
				}
				return method;
			}
		}
		return null;
	}
	
}
