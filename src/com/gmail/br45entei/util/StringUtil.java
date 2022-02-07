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
package com.gmail.br45entei.util;

import com.gmail.br45entei.game.math.MathUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

/** Utility class containing basic string-related utility functions.
 * 
 * @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class StringUtil {
	
	/** @return A string containing random alpha-numeric characters; commonly
	 *         used as a session ID */
	public static final String nextSessionID() {
		return new BigInteger(130, new SecureRandom()).toString(32);
	}
	
	/** @param array The String[] array to convert
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array) {
		return stringArrayToString(array, ' ', 0);
	}
	
	/** @param array The list to read from
	 * @param separator The character to use as a separator
	 * @return The resulting string */
	public static final String stringArrayToString(char separator, Collection<String> array) {
		return stringArrayToString(String.valueOf(separator), array);
	}
	
	/** @param array The list to read from
	 * @param separator The string to use as a separator
	 * @return The resulting string */
	public static final String stringArrayToString(String separator, Collection<String> array) {
		if(array == null) {
			return "null";
		}
		String rtrn = "";
		int index = 0;
		for(String element : array) {
			rtrn += element + ((++index) == array.size() ? "" : separator);
		}
		return rtrn.trim();
	}
	
	/** @param array The array/list/strings to read from
	 * @param separator The character to use as a separator
	 * @return The resulting string */
	public static final String stringArrayToString(char separator, String... array) {
		if(array == null) {
			return "null";
		}
		String rtrn = "";
		for(String element : array) {
			rtrn += element + separator;
		}
		return rtrn.length() >= 2 ? rtrn.substring(0, rtrn.length() - 1) : rtrn;
	}
	
	/** @param array The String[] array to convert
	 * @param separator The separator character to use
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, char separator) {
		return stringArrayToString(array, separator, 0);
	}
	
	/** @param array The String[] array to convert
	 * @param separator The separator character to use
	 * @param startIndex The index to start at
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, char separator, int startIndex) {
		return stringArrayToString(array, String.valueOf(separator), startIndex);
	}
	
	/** @param array The array/list/strings to read from
	 * @param separator The character to use as a separator
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, String separator) {
		return stringArrayToString(array, separator, 0);
	}
	
	/** @param array The array/list/strings to read from
	 * @param separator The character to use as a separator
	 * @param startIndex The index to start at
	 * @param endIndex The index to stop short at
	 * @return The resulting string. If startIndex is greater than or equal to
	 *         the array's size, endIndex is greater than the array's size,
	 *         startIndex is greater than or equal to endIndex, and/or either
	 *         startIndex or endIndex are negative, "null" is returned. */
	public static final String stringArrayToString(String[] array, String separator, int startIndex, int endIndex) {
		if(array == null) {
			return "null";
		}
		if(startIndex >= array.length || endIndex > array.length || startIndex >= endIndex || startIndex < 0 || endIndex < 0) {
			final String lineSeparator = CodeUtil.getProperty("line.separator");
			String errMsg = "";
			if(startIndex >= array.length) {
				errMsg = errMsg.concat(String.format("The specified start index (%s) is greater than or equal to the array's length! (%s)", Integer.toString(startIndex), Integer.toString(array.length))).concat(lineSeparator);
			}
			if(endIndex > array.length) {
				errMsg = errMsg.concat(String.format("The specified end index (%s) is greater than the array's length! (%s)", Integer.toString(endIndex), Integer.toString(array.length))).concat(lineSeparator);
			}
			if(startIndex >= endIndex) {
				errMsg = errMsg.concat(String.format("The specified start index (%s) is greater than or equal to the specified end index! (%s)", Integer.toString(startIndex), Integer.toString(endIndex))).concat(lineSeparator);
			}
			if(startIndex < 0) {
				errMsg = errMsg.concat(String.format("The specified start index (%s) is less than zero! (%s)", Integer.toString(startIndex))).concat(lineSeparator);
			}
			if(endIndex < 0) {
				errMsg = errMsg.concat(String.format("The specified end index (%s) is less than zero! (%s)", Integer.toString(endIndex))).concat(lineSeparator);
			}
			throw new ArrayIndexOutOfBoundsException(errMsg.endsWith(lineSeparator) ? errMsg.substring(0, errMsg.length() - lineSeparator.length()) : errMsg);
		}
		String rtrn = "";
		int i = 0;
		for(String element : array) {
			if(i >= startIndex && i < endIndex) {
				rtrn = rtrn.concat(element).concat(i + 1 == endIndex ? "" : separator);
			}
			if(i >= endIndex) {
				break;
			}
			i++;
		}
		return rtrn;
	}
	
	/** @param array The array/list/strings to read from
	 * @param separator The character to use as a separator
	 * @param startIndex The index to start at
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, String separator, int startIndex) {
		return stringArrayToString(array, separator, startIndex, array.length);
	}
	
	/** @param array The String[] array to convert
	 * @param separator The separator character to use
	 * @param startIndex The index to start at
	 * @param endIndex The index to stop short at
	 * @return The resulting string */
	public static final String stringArrayToString(String[] array, char separator, int startIndex, int endIndex) {
		return stringArrayToString(array, Character.toString(separator), startIndex, endIndex);
	}
	
	/** @param element The StackTraceElement
	 * @param identifier The identifier to use
	 * @return The resulting customized toString */
	public static final String stackTraceElementToStringCustom(StackTraceElement element, String identifier) {
		if(element != null) {
			return "(".concat(element.getFileName()).concat(":").concat(Integer.toString(element.getLineNumber())).concat(")-> ").concat(element.getMethodName()).concat("(").concat(identifier).concat(")");
		}
		return "null-> (".concat(identifier).concat(")");
	}
	
	/** @param stackTraceElements The elements to convert
	 * @return The resulting string */
	public static final String stackTraceElementsToStr(StackTraceElement[] stackTraceElements) {
		return stackTraceElementsToStr(stackTraceElements, "\r\n");
	}
	
	/** @param stackTraceElements The elements to convert
	 * @param lineSeparator The line separator to use
	 * @return The resulting string */
	public static final String stackTraceElementsToStr(StackTraceElement[] stackTraceElements, String lineSeparator) {
		String str = "";
		if(stackTraceElements != null) {
			for(StackTraceElement stackTrace : stackTraceElements) {
				str = str.concat(!stackTrace.toString().startsWith("Caused By") ? "     at " : "").concat(stackTrace.toString()).concat(lineSeparator);
			}
		}
		return str;
	}
	
	/** @param stackTraceElements The elements to convert
	 * @return The resulting string */
	public static final String stackTraceCausedByElementsOnlyToStr(StackTraceElement[] stackTraceElements) {
		return stackTraceCausedByElementsOnlyToStr(stackTraceElements, "\r\n");
	}
	
	/** @param stackTraceElements The elements to convert
	 * @param lineSeparator The line separator to use
	 * @return The resulting string */
	public static final String stackTraceCausedByElementsOnlyToStr(StackTraceElement[] stackTraceElements, String lineSeparator) {
		String str = "";
		if(stackTraceElements != null) {
			for(StackTraceElement stackTrace : stackTraceElements) {
				str += (!stackTrace.toString().startsWith("Caused By") ? "" : stackTrace.toString() + lineSeparator);
			}
		}
		return str;
	}
	
	/** @param e The {@link Throwable} to convert
	 * @return The resulting String */
	public static final String throwableToStrNoStackTraces(Throwable e) {
		return throwableToStrNoStackTraces(e, "\r\n");
	}
	
	/** @param e The {@link Throwable} to convert
	 * @param lineSeparator The line separator to use
	 * @return The resulting String */
	public static final String throwableToStrNoStackTraces(Throwable e, String lineSeparator) {
		if(e == null) {
			return "null";
		}
		String str = e.getClass().getName() + ": ";
		if((e.getMessage() != null) && !e.getMessage().isEmpty()) {
			str += e.getMessage() + lineSeparator;
		} else {
			str += lineSeparator;
		}
		str += stackTraceCausedByElementsOnlyToStr(e.getStackTrace(), lineSeparator);
		if(e.getCause() != null) {
			str += "Caused by:" + lineSeparator + throwableToStrNoStackTraces(e.getCause(), lineSeparator);
		}
		return str;
	}
	
	/** @param e The {@link Throwable} to convert
	 * @return The resulting String */
	public static final String throwableToStr(Throwable e) {
		return throwableToStr(e, "\r\n");
	}
	
	/** @param e The {@link Throwable} to convert
	 * @param lineSeparator The line separator to use
	 * @return The resulting String */
	public static final String throwableToStr(Throwable e, String lineSeparator) {
		if(e == null) {
			return "null";
		}
		String str = e.getClass().getName() + ": ";
		if((e.getMessage() != null) && !e.getMessage().isEmpty()) {
			str += e.getMessage() + lineSeparator;
		} else {
			str += lineSeparator;
		}
		str += stackTraceElementsToStr(e.getStackTrace(), lineSeparator);
		if(e.getCause() != null) {
			str += "Caused by:" + lineSeparator + throwableToStr(e.getCause(), lineSeparator);
		}
		return str;
	}
	
	/** @return The stack trace element of the code that ran this method
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static final StackTraceElement getCurrentStackTraceElement() {
		return ___gjdgerjkgnmFf_Xaf();
	}
	
	/** This method's name is ridiculous on purpose to prevent any other method
	 * names in the stack trace from potentially matching this one.
	 * 
	 * @return The stack trace element of the code that called the method that
	 *         called the method that called this method(Should only be called
	 *         by getCallingStackTraceElement()).
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	private static final StackTraceElement ___gjdgerjkgnmFf_Xaf() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		int index = -1;
		int target = -1;
		for(StackTraceElement element : elements) {
			index++;
			String methodName = element.getMethodName();
			if(methodName.equals("___gjdgerjkgnmFf_Xaf")) {
				target = index + 2;
				break;
			}
		}
		return(target > 0 && target < elements.length ? elements[target] : null);
	}
	
	/** @return The stack trace element of the code that ran this method
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static final StackTraceElement getCallingStackTraceElement() {
		return ___ghdTdjsgd7t5c_Xaf();
	}
	
	/** This method's name is ridiculous on purpose to prevent any other method
	 * names in the stack trace from potentially matching this one.
	 * 
	 * @return The stack trace element of the code that called the method that
	 *         called the method that called this method(Should only be called
	 *         by getCallingStackTraceElement()).
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	private static final StackTraceElement ___ghdTdjsgd7t5c_Xaf() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		int index = -1;
		int target = -1;
		for(StackTraceElement element : elements) {
			index++;
			String methodName = element.getMethodName();
			if(methodName.equals("___ghdTdjsgd7t5c_Xaf")) {
				target = index + 3;
				break;
			}
		}
		return(target > 0 && target < elements.length ? elements[target] : null);
	}
	
	/** @return The line number of the code that ran this method
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static final int getLineNumber() {
		return ___8drrd3148796d_Xaf();
	}
	
	/** This method's name is ridiculous on purpose to prevent any other method
	 * names in the stack trace from potentially matching this one.
	 * 
	 * @return The line number of the code that called the method that called
	 *         this method(Should only be called by getLineNumber()).
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	private static final int ___8drrd3148796d_Xaf() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		int index = -1;
		int target = -1;
		for(StackTraceElement element : elements) {
			index++;
			String methodName = element.getMethodName();
			if(methodName.equals("___8drrd3148796d_Xaf")) {
				target = index + 3;
				break;
			}
		}
		return(target > 0 && target < elements.length ? elements[target].getLineNumber() : -1);
	}
	
	/** @return The method name of the code that ran the code that ran this
	 *         method
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	public static final String getMethodName() {
		return ___fbhfRghjprgGF_Xaf();
	}
	
	/** This method's name is ridiculous on purpose to prevent any other method
	 * names in the stack trace from potentially matching this one.
	 * 
	 * @return The method name of the code that called the method that called
	 *         the method that called this method(Should only be called by
	 *         getMethodName()).
	 * @author Brian_Entei &ltbr45entei&#064;gmail.com&gt; */
	private static final String ___fbhfRghjprgGF_Xaf() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		int index = -1;
		int target = -1;
		for(StackTraceElement element : elements) {
			index++;
			String methodName = element.getMethodName();
			if(methodName.equals("___fbhfRghjprgGF_Xaf")) {
				target = index + 3;
				break;
			}
		}
		return(target > 0 && target < elements.length ? elements[target].getMethodName() : null);
	}
	
	/** @param args Program command line arguments */
	public static final void main(String[] args) {
		System.out.println(MathUtil.getDecimalPartOf(Double.NaN));
		double piiiiii = 3.1415926535897932384626433832795028841971693993751058209749D;
		int size = (Double.SIZE / 4) - 1;
		System.out.println(MathUtil.limitDecimalToNumberOfPlaces(piiiiii, size - 2));
		System.out.println(MathUtil.limitDecimalNoRounding(0.0D, 4));
		System.out.println(MathUtil.limitDecimalNoRounding(-0.0D, 4));
		System.out.println(MathUtil.limitDecimalNoRounding(5, 4));
		System.out.println(MathUtil.limitDecimalNoRounding(-5, 4));
		System.out.println(MathUtil.limitDecimalNoRounding(piiiiii, size - 2));
		System.out.println(MathUtil.limitDecimalNoRounding(1.9969016, size));
		System.out.println(MathUtil.limitDecimalNoRounding(1.99690169, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.899999998, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.9999999996, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.99999999997, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.999999999998, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.9999999999996, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.99999999999999, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.9999969999999996, size));//Unfortunate, but I'll take it.
		System.out.println(MathUtil.limitDecimalNoRounding(1.9999969999999996, size));//Unfortunate, but I'll take it.
		System.out.println(MathUtil.limitDecimalToNumberOfPlaces(0.9999969999999996, size));//herpa derpa derpity derp
		System.out.println(MathUtil.limitDecimalNoRounding(0.999999999999996, size));
		System.out.println(MathUtil.limitDecimalNoRounding(0.9999999999999997, size));
		System.out.println(MathUtil.limitDecimalToNumberOfPlaces(0.9999999999999997, size));
		System.out.println(stackTraceElementToStringCustom(getCurrentStackTraceElement(), "args=\"" + stringArrayToString(args) + "\""));
		checkMethodName();
	}
	
	private static final void checkMethodName() {
		System.out.println(getCurrentStackTraceElement());
	}
	
	/** Returns a string of characters.<br>
	 * Example: <code>lineOf('a', 5);</code> --&gt; <code>aaaaa</code>
	 * 
	 * @param c The character to use
	 * @param length The number of characters
	 * @return A string full of the given characters at the given length */
	public static final String lineOf(char c, int length) {
		char[] str = new char[length];
		for(int i = 0; i < length; i++) {
			str[i] = c;
		}
		return new String(str);
	}
	
	/** @param str The string to search for
	 * @param list The list of strings to search through
	 * @return True if the list contained any string starting with the given
	 *         string, ignoring case */
	public static final boolean startsWithIgnoreCase(String str, String... list) {
		if(str != null && list != null && list.length != 0) {
			str = str.toLowerCase();
			for(String s : list) {
				if(s != null && s.toLowerCase().startsWith(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** @param list The list of strings to search through
	 * @param str The string to search for
	 * @return True if the list contained any string starting with the given
	 *         string, ignoring case */
	public static final boolean startsWithIgnoreCase(Collection<String> list, String str) {
		if(str != null && list != null && !list.isEmpty()) {
			str = str.toLowerCase();
			for(String s : new ArrayList<>(list)) {
				if(s.toLowerCase().startsWith(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** @param str The string to search for
	 * @param list The list of strings to search through
	 * @return True if the list contained any instance of the given string,
	 *         ignoring case */
	public static final boolean containsIgnoreCase(String str, String... list) {
		if(str != null && list != null && list.length != 0) {
			for(String s : list) {
				if(s != null && s.equalsIgnoreCase(str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** @param list The list of strings to search through
	 * @param str The string to search for
	 * @return True if the list contained any instance of the given string,
	 *         ignoring case */
	public static final boolean containsIgnoreCase(Collection<String> list, String str) {
		if(str != null && list != null && !list.isEmpty()) {
			for(String s : new ArrayList<>(list)) {
				if(str.equalsIgnoreCase(s)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** Case insensitive check if a String ends with a specified suffix.
	 * 
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered to be equal. The comparison is case
	 * insensitive.
	 * 
	 * <pre>
	 * StringUtils.endsWithIgnoreCase(null, null)      = true
	 * StringUtils.endsWithIgnoreCase(null, "abcdef")  = false
	 * StringUtils.endsWithIgnoreCase("def", null)     = false
	 * StringUtils.endsWithIgnoreCase("def", "abcdef") = true
	 * StringUtils.endsWithIgnoreCase("def", "ABCDEF") = true
	 * </pre>
	 * 
	 * @see java.lang.String#endsWith(String)
	 * @param str the String to check, may be null
	 * @param suffix the suffix to find, may be null
	 * @return <code>true</code> if the String ends with the suffix, case
	 *         insensitive, or
	 *         both <code>null</code>
	 * @since 2.4 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		return endsWith(str, suffix, true);
	}
	
	/** Check if a String ends with a specified suffix (optionally case
	 * insensitive).
	 * 
	 * <pre>
	 * StringUtils.endsWithIgnoreCase(null, null)      = true
	 * StringUtils.endsWithIgnoreCase(null, "abcdef")  = false
	 * StringUtils.endsWithIgnoreCase("def", null)     = false
	 * StringUtils.endsWithIgnoreCase("def", "abcdef") = true
	 * StringUtils.endsWithIgnoreCase("def", "ABCDEF") = false
	 * </pre>
	 * 
	 * @see java.lang.String#endsWith(String)
	 * @param str the String to check, may be null
	 * @param suffix the suffix to find, may be null
	 * @param ignoreCase indicates whether the compare should ignore case
	 *            (case insensitive) or not.
	 * @return <code>true</code> if the String starts with the prefix or
	 *         both <code>null</code> */
	public static boolean endsWith(String str, String suffix, boolean ignoreCase) {
		if((str == null) || (suffix == null)) {
			return((str == null) && (suffix == null));
		}
		if(suffix.length() > str.length()) {
			return false;
		}
		int strOffset = str.length() - suffix.length();
		return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
	}
	
	/** @param string The string whose contents will be replaced
	 * @param searchString The string to search for that will be replaced
	 * @param replacement The string that will replace the search string
	 * @return The resulting string */
	public static final String replaceOnce(String string, String searchString, String replacement) {
		return replace(string, searchString, replacement, 1);
	}
	
	/** This method is copied from
	 * {@link org.apache.commons.lang3.StringUtils#replace(String, String, String, int)}
	 * and is licensed to the Apache Software Foundation (ASF).<br>
	 * Please see the Apache License (Version 2.0), which is available at:
	 * <a href=
	 * "http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>.<br>
	 * <p>
	 * Replaces a String with another String inside a larger String,
	 * for the first {@code max} values of the search String.
	 * </p>
	 *
	 * <p>
	 * A {@code null} reference passed to this method is a no-op.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *)         = null
	 * StringUtils.replace("", *, *, *)           = ""
	 * StringUtils.replace("any", null, *, *)     = "any"
	 * StringUtils.replace("any", *, null, *)     = "any"
	 * StringUtils.replace("any", "", *, *)       = "any"
	 * StringUtils.replace("any", *, *, 0)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
	 * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
	 * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
	 * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
	 * </pre>
	 *
	 * @param text text to search and replace in, may be null
	 * @param searchString the String to search for, may be null
	 * @param replacement the String to replace it with, may be null
	 * @param max maximum number of values to replace, or {@code -1} if no
	 *            maximum
	 * @return the text with any replacements processed,
	 *         {@code null} if null String input */
	public static String replace(final String text, final String searchString, final String replacement, int max) {
		if(text == null || text.isEmpty() || searchString == null || searchString.isEmpty() || replacement == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if(end == -1) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while(end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if(--max == 0) {
				break;
			}
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}
	
	/** @param str The text to edit
	 * @return The given text with its first letter capitalized */
	public static final String capitalizeFirstLetter(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	public static final String capitalizeFirstLetterOfEachWord(final String str, final char separator, final char separatorReplacement) {
		StringBuilder sb = new StringBuilder();
		String sep = Character.toString(separatorReplacement);
		String[] words = str.split(Pattern.quote(Character.toString(separator)));
		for(int i = 0; i < words.length; i++) {
			String word = StringUtil.capitalizeFirstLetter(words[i].toLowerCase());
			sb.append(word).append(i + 1 == words.length ? "" : sep);
		}
		return sb.toString();
	}
	
	public static final String capitalizeFirstLetterOfEachWord(String str, char separator) {
		return capitalizeFirstLetterOfEachWord(str, separator, separator);
	}
	
	//======================================================================================
	
	/** @param decimal The decimal
	 * @return The whole number portion of the given decimal */
	public static final String getWholePartOf(double decimal) {
		if(decimal != decimal) {
			return Long.toString(Double.doubleToLongBits(decimal));
		}
		String d = new BigDecimal(decimal).toPlainString();
		int indexOfDecimalPoint = d.indexOf(".");
		if(indexOfDecimalPoint != -1) {
			return d.substring(0, indexOfDecimalPoint);
		}
		return Long.toString((long) decimal);
	}
	
	/** @param decimal The decimal
	 * @return The given decimal without */
	public static final String getDecimalPartOf(double decimal) {
		if(decimal != decimal) {
			return Double.toString(decimal);
		}
		String d = new BigDecimal(decimal).toPlainString();
		int indexOfDecimalPoint = d.indexOf(".");
		if(indexOfDecimalPoint == -1) {
			d = Double.toString(decimal);
			indexOfDecimalPoint = d.indexOf(".");
		}
		if(indexOfDecimalPoint != -1) {
			return d.substring(indexOfDecimalPoint);
		}
		return d;
	}
	
	//======================================================================================
	
	/** @param millis The time since midnight, January 1st, 1970 UTC
	 * @param getTimeOnly Whether or not the date should be excluded from the
	 *            returned time string
	 * @param fileSystemSafe Whether or not the returned string will be used in
	 *            the making of a folder or file
	 * @return The resulting string */
	public static String getTime(long millis, boolean getTimeOnly, boolean fileSystemSafe) {
		return (getTimeOnly ? new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss") : new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy_HH.mm.ss" : "MM/dd/yyyy_HH:mm:ss")).format(new Date(millis));
	}
	
	/** @param getTimeOnly Whether or not the date should be excluded from the
	 *            returned time string
	 * @param fileSystemSafe Whether or not the returned string will be used in
	 *            the making of a folder or file
	 * @return The resulting string */
	public static String getSystemTime(boolean getTimeOnly, boolean fileSystemSafe) {
		String timeAndDate = "";
		DateFormat dateFormat;
		if(getTimeOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss");
		} else {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy_HH.mm.ss" : "MM/dd/yyyy_HH:mm:ss");
		}
		timeAndDate = dateFormat.format(new Date(System.currentTimeMillis()));
		return timeAndDate;
	}
	
	//======================================================================================
	
	protected static final int SECOND = 1000;
	protected static final int MINUTE = 60 * SECOND;
	protected static final int HOUR = 60 * MINUTE;
	protected static final double DAY = 24.0000006 * HOUR;
	protected static final double WEEK = 7.0 * DAY;
	protected static final double MONTH = 4.0 * WEEK;
	protected static final double YEAR = 365.2421891 * DAY;
	protected static final double DECADE = 10.0 * YEAR;
	protected static final double CENTURY = 100.0 * YEAR;
	protected static final double MILLENIUM = 1000.0 * YEAR;
	protected static final double MEGA_ANNUM = 1000000.0 * YEAR;
	protected static final double AEON = 1000000000.0 * YEAR;
	
	/** @param milliseconds The amount of time, in milliseconds
	 * @return The given length of time, in human-readable format */
	public static final String getLengthOfTime(long milliseconds) {
		StringBuffer text = new StringBuffer("");
		boolean yearsDaysOrHours = false;
		if(milliseconds >= AEON) {
			long megannum = Math.round(Math.floor(milliseconds / AEON));
			text.append(megannum).append(megannum == 1L ? " aeon " : " aeons ");
			milliseconds %= AEON;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= MEGA_ANNUM) {
			long megannum = Math.round(Math.floor(milliseconds / MEGA_ANNUM));
			text.append(megannum).append(megannum == 1L ? " mega-annum " : " mega-anna ");
			milliseconds %= MEGA_ANNUM;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= MILLENIUM) {
			long millenium = Math.round(Math.floor(milliseconds / MILLENIUM));
			text.append(millenium).append(millenium == 1L ? " millenium " : " millenia ");
			milliseconds %= MILLENIUM;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= CENTURY) {
			long centuries = Math.round(Math.floor(milliseconds / CENTURY));
			text.append(centuries).append(centuries == 1L ? " century " : " centuries ");
			milliseconds %= CENTURY;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= DECADE) {
			long decades = Math.round(Math.floor(milliseconds / DECADE));
			text.append(decades).append(decades == 1L ? " decade " : " decades ");
			milliseconds %= DECADE;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= YEAR) {
			long years = Math.round(Math.floor(milliseconds / YEAR));
			text.append(years).append(years == 1L ? " year " : " years ");
			milliseconds %= YEAR;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= DAY) {
			long days = Math.round(Math.floor(milliseconds / DAY));
			text.append(days).append(days == 1L ? " day " : " days ");
			milliseconds %= DAY;
			yearsDaysOrHours = true;
		}
		if(milliseconds >= HOUR) {
			long hours = milliseconds / HOUR;
			text.append(hours).append(hours == 1L ? " hour " : " hours ");
			milliseconds %= HOUR;
			yearsDaysOrHours = true;
		}
		boolean minutes = false;
		boolean plural = true;
		if(milliseconds >= MINUTE) {
			if(yearsDaysOrHours) {
				text.append("and ");
			} else {
				text.append("00:");
			}
			long mins = milliseconds / MINUTE;
			text.append((mins >= 10 ? "" : "0") + mins).append(":");
			milliseconds %= MINUTE;
			minutes = true;
			plural = mins != 1;
		} else {
			if(yearsDaysOrHours) {
				text.append("and 00:");
			} else {
				text.append("00:00:");
			}
		}
		if(milliseconds >= SECOND) {
			long sec = milliseconds / SECOND;
			text.append((sec >= 10 ? "" : "0") + sec);
			milliseconds %= SECOND;
			plural = !minutes ? sec != 1 : plural;
		} else {
			text.append("00");
		}
		if(milliseconds > 0) {
			text.append(".");
			String millis = Long.toString(milliseconds);
			text.append(millis.length() == 2 ? "0".concat(millis) : (millis.length() == 1 ? "00".concat(millis) : millis));
		}
		text.append(plural ? (minutes ? " minutes" : " seconds") : (minutes ? " minute" : " second"));
		return text.toString();
	}
	
	/** @param input The time to parse(e.g.
	 *            <tt>1w, 7d, 1m, 3y, 5h, 5h 7m 3s, ...</tt>)
	 * @param delimiter The separator to use, usually a single space
	 * @return The parsed time
	 * @throws NumberFormatException Thrown if any of the arguments in the input
	 *             could not be formatted as a long */
	public static final long parseTime(String input, String delimiter) throws NumberFormatException {
		long millis = 0L;
		for(final String amount : input.split(Pattern.quote(delimiter))) {
			final String amt = amount.toLowerCase();
			double value;
			if(CodeUtil.isDouble(amt)) {
				value = Double.parseDouble(amt);
			} else {
				try {
					if(amt.endsWith("s") || amt.endsWith("sec") || amt.endsWith("secs") || amt.endsWith("second") || amt.endsWith("seconds")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("s") ? 1 : (amt.endsWith("sec") ? 3 : (amt.endsWith("secs") ? 4 : (amt.endsWith("second") ? 6 : 7)))))) * SECOND;
					} else if(amt.endsWith("m") || amt.endsWith("min") || amt.endsWith("mins") || amt.endsWith("minute") || amt.endsWith("minutes")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("m") ? 1 : (amt.endsWith("min") ? 3 : (amt.endsWith("mins") ? 4 : (amt.endsWith("minute") ? 6 : 7)))))) * MINUTE;
					} else if(amt.endsWith("h") || amt.endsWith("hour") || amt.endsWith("hours")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("h") ? 1 : (amt.endsWith("hour") ? 4 : 5)))) * HOUR;
					} else if(amt.endsWith("d") || amt.endsWith("day")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("d") ? 1 : 3))) * DAY;
					} else if(amt.endsWith("w") || amt.endsWith("week") || amt.endsWith("weeks")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("w") ? 1 : (amt.endsWith("week") ? 4 : 5)))) * WEEK;
					} else if(amt.endsWith("y") || amt.endsWith("year") || amt.endsWith("years")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("y") ? 1 : (amt.endsWith("year") ? 4 : 5)))) * YEAR;
						
					} else if(amt.endsWith("dec") || amt.endsWith("decade") || amt.endsWith("decades")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("dec") ? 3 : (amt.endsWith("decade") ? 6 : 7)))) * DECADE;
					} else if(amt.endsWith("c") || amt.endsWith("century") || amt.endsWith("centuries")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("c") ? 1 : (amt.endsWith("century") ? 7 : 9)))) * CENTURY;
					} else if(amt.endsWith("mill") || amt.endsWith("millenium") || amt.endsWith("millenia")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("mill") ? 4 : (amt.endsWith("millenium") ? 9 : 8)))) * MILLENIUM;
						
					} else if(amt.endsWith("mega") || amt.endsWith("mega-annum") || amt.endsWith("mega-anna")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("mega") ? 4 : (amt.endsWith("mega-annum") ? 10 : 9)))) * MEGA_ANNUM;
					} else if(amt.endsWith("ae") || amt.endsWith("aeon") || amt.endsWith("aeons")) {
						value = Double.parseDouble(amt.substring(0, amt.length() - (amt.endsWith("ae") ? 2 : (amt.endsWith("aeon") ? 4 : 5)))) * AEON;
						
					} else {
						throw new NumberFormatException(String.format("For input string: \"%s\"", amt));
					}
				} catch(NumberFormatException e) {
					throw new NumberFormatException(String.format("For input string: \"%s\"", amount));
				}
			}
			millis += value;
		}
		return millis;
	}
	
	/** @param str The string to test
	 * @param delimiter The delimiter that the string was constructed with
	 * @return Whether or not the given string is a parsable time with the given
	 *         delimiter */
	public static final boolean isParsableTime(String str, String delimiter) {
		try {
			parseTime(str, delimiter);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	//======================================================================================
	
}
