/*******************************************************************************
 * 
 * Copyright © 2020 Brian_Entei (br45entei@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 *******************************************************************************/
package com.gmail.br45entei.game.math;

import com.gmail.br45entei.util.StringUtil;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/** @since 1.0
 * @author Brian_Entei */
public final strictfp class MathUtil {
	
	/** Allows you to round decimals to the nearest thousandth(0.00) */
	public static final DecimalFormat decimal = new DecimalFormat("#0.00");
	
	static {
		decimal.setRoundingMode(RoundingMode.HALF_EVEN);
	}
	
	// Code copied from Double.valueOf(String s):
	private static final String Digits = "(\\p{Digit}+)";
	private static final String HexDigits = "(\\p{XDigit}+)";
	// an exponent is 'e' or 'E' followed by an optionally
	// signed decimal integer.
	private static final String Exp = "[eE][+-]?" + Digits;
	private static final String fpRegex = ("[\\x00-\\x20]*" +  // Optional leading "whitespace"
			"[+-]?(" + // Optional sign character
			"NaN|" +           // "NaN" string
			"Infinity|" +      // "Infinity" string
			
			// A decimal floating-point string representing a finite positive
			// number without a leading sign has at most five basic pieces:
			// Digits . Digits ExponentPart FloatTypeSuffix
			//
			// Since this method allows integer-only strings as input
			// in addition to strings of floating-point literals, the
			// two sub-patterns below are simplifications of the grammar
			// productions from section 3.10.2 of
			// The Java Language Specification.
			
			// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
			"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +
			
			// . Digits ExponentPart_opt FloatTypeSuffix_opt
			"(\\.(" + Digits + ")(" + Exp + ")?)|" +
			
			// Hexadecimal strings
			"((" +
			// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
			"(0[xX]" + HexDigits + "(\\.)?)|" +
			
			// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
			"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
			
			")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");// Optional trailing "whitespace"
			
	/** @param str The string to check
	 * @return Whether or not the given string can safely be converted into a
	 *         double via {@link Double#valueOf(String)} or
	 *         {@link Double#parseDouble(String)}
	 * @see Double#valueOf(String) */
	public static final boolean isDouble(String str) {
		return Pattern.matches(fpRegex, str);
		/*if(Pattern.matches(fpRegex, str)) {
			Double.valueOf(str); // Will not throw NumberFormatException
		} else {
			// Perform suitable alternative action
		}*/
	}
	
	/** @param A Numerator
	 * @param B Denominator
	 * @return True if A goes into B<br>
	 *         Special case: if A equals zero, return false. */
	public static final boolean doesAGoIntoB(double A, double B) {
		if(A == 0.0D) {
			return false;
		}
		return (B / A) % 1 == 0;
	}
	
	/** @param d The decimal to round
	 * @param radix The amount of decimal places to round to
	 * @return The resulting decimal in String form
	 * @see #roundTo(double, int)
	 * @see #roundTo(float, int) */
	public static final String roundToString(double d, int radix) {
		String format = "#.";
		for(int i = 0; i < radix; i++) {
			format += "#";
		}
		DecimalFormat df = new DecimalFormat(format);
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		String result = df.format(d);
		if(radix == 0 && result.endsWith(".")) {
			return result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	/** @param d The decimal to round
	 * @param radix The amount of decimal places to round to
	 * @return The resulting decimal
	 * @see #roundToString(double, int)
	 * @see #roundTo(float, int) */
	public static final double roundTo(double d, int radix) {
		try {
			return d != d ? d : Double.valueOf(roundToString(d, radix)).doubleValue();
		} catch(NumberFormatException ignored) {
			return d;
		}
	}
	
	/** @param d The decimal to round
	 * @param radix The amount of decimal places to round to
	 * @return The resulting decimal
	 * @see #roundToString(double, int)
	 * @see #roundTo(double, int) */
	public static final float roundTo(float d, int radix) {
		return d != d ? d : Double.valueOf(roundToString(d, radix)).floatValue();
	}
	
	public static final long getWholePartOf(double decimal) {
		return Long.valueOf(roundToString(decimal, 0)).longValue();
	}
	
	public static final double getDecimalPartOf(double decimal) {
		String d = new BigDecimal(decimal).toPlainString();
		int indexOfDecimalPoint = d.indexOf(".");
		if(indexOfDecimalPoint != -1) {
			return Double.valueOf(d.substring(indexOfDecimalPoint)).doubleValue();
		}
		return decimal;
	}
	
	/** @param decimal The decimal to limit
	 * @param numOfPlaces The number of places to limit the decimal to
	 * @return The resulting String */
	public static final String limitDecimalToNumberOfPlaces(double decimal, int numOfPlaces) {
		return String.format("%." + numOfPlaces + "f", Double.valueOf(decimal));
	}
	
	/** @param decimal The decimal to limit
	 * @param numOfPlaces The number of places to limit the decimal to
	 * @return The resulting String */
	public static final String limitDecimalToNumberOfPlaces(float decimal, int numOfPlaces) {
		return String.format("%." + numOfPlaces + "f", Float.valueOf(decimal));
	}
	
	/** @param decimal The decimal to limit
	 * @param numOfPlaces The number of places to limit the decimal to(radix)
	 * @param pad Whether or not the decimal should be padded with trailing
	 *            zeros if the resulting length is less than
	 *            <code>numOfPads</code>
	 * @return The limited decimal */
	public static final String limitDecimalNoRounding(double decimal, int numOfPlaces, boolean pad) {
		if(Double.isNaN(decimal) || Double.isInfinite(decimal)) {
			return Double.toString(decimal);
		}
		String padStr = pad ? StringUtil.lineOf('0', numOfPlaces) : "0";
		if(Double.doubleToLongBits(decimal) == Double.doubleToLongBits(0.0)) {
			return "0" + (numOfPlaces != 0 ? "." + padStr : "");
		}
		if(Double.doubleToLongBits(decimal) == Double.doubleToLongBits(-0.0)) {
			return "-0" + (numOfPlaces != 0 ? "." + padStr : "");
		}
		numOfPlaces += 1;
		String whole = Double.isFinite(decimal) ? StringUtil.getWholePartOf(decimal) : Double.isInfinite(decimal) ? "Infinity" : "NaN";
		if(numOfPlaces == 0) {
			return whole;
		}
		
		if(pad) {
			int checkWholeLength = whole.length();
			checkWholeLength = decimal < 0 ? checkWholeLength - 1 : checkWholeLength;
			checkWholeLength -= 2;
			if(checkWholeLength > 0) {
				if(padStr.length() - checkWholeLength <= 0) {
					padStr = "";
				} else {
					padStr = padStr.substring(0, padStr.length() - checkWholeLength);
				}
			}
			if(padStr.isEmpty()) {
				return whole;
			}
		}
		
		String d = Double.isFinite(decimal) ? StringUtil.getDecimalPartOf(decimal) : "";
		if(d.length() == 1 || d.equals(".0")) {
			return whole + (numOfPlaces != 0 ? "." + padStr : "");
		}
		if(d.length() > numOfPlaces) {
			d = d.substring(d.indexOf('.') + 1, numOfPlaces);
		}
		if(d.startsWith(".")) {
			d = d.substring(1);
		}
		String restore = d;
		if(d.endsWith("9")) {//Combat weird java rounding
			int chopIndex = -1;
			char[] array = d.toCharArray();
			boolean lastChar9 = false;
			for(int i = array.length - 1; i >= 0; i--) {
				boolean _9 = array[i] == '9';
				array[i] = _9 ? '0' : array[i];
				chopIndex = i;
				if(!_9 && lastChar9) {//If the current character isn't a 9 and the one after it(to the right) is, then add one to the current non-nine char and set the chop-off index, "removing" the "rounding issue"
					array[i] = Integer.valueOf(Integer.valueOf(new String(new char[] {array[i]})).intValue() + 1).toString().charAt(0);
					chopIndex = i + 1;
					break;
				}
				lastChar9 = _9;
			}
			d = new String(array, 0, (chopIndex == -1 ? array.length : chopIndex));
		}
		if(d.endsWith("0")) {
			while(d.endsWith("0")) {
				d = d.substring(0, d.length() - 1);
			}
		}
		if(d.isEmpty()) {
			d = restore;
		}
		if(pad && (numOfPlaces - d.length()) > 0) {
			d += StringUtil.lineOf('0', numOfPlaces - d.length());
		}
		if(d.length() > numOfPlaces - 1) {
			d = d.substring(0, numOfPlaces - 1);
		}
		//System.out.println("\"" + whole + "." + d + "\"");
		return whole + "." + d;//(d.isEmpty() ? "" : ("." + d));
	}
	
	/** @param decimal The decimal to limit
	 * @param numOfPlaces The number of places to limit the decimal to(radix)
	 * @return The limited decimal */
	public static final String limitDecimalNoRounding(double decimal, int numOfPlaces) {
		return limitDecimalNoRounding(decimal, numOfPlaces, false);
	}
	
	/** @param a The first number
	 * @param b The second number
	 * @return The given numbers added in order */
	public static final double add(double a, double b) {
		return a + b;
	}
	
	/** @param a The first number
	 * @param b The second number
	 * @return The given numbers subtracted in order */
	public static final double sub(double a, double b) {
		return a - b;
	}
	
	/*public static final double mult(double a, double b) {
		long index = 0;
		double result = 0;
		while((index += 1) <= b) {
			result += a;
		}
		double bDecimal = b % 1.00;
		if(bDecimal != 0) {
			index = 0;
			while((index += 1) <= a) {
				result += bDecimal;
			}
		}
		return Double.valueOf(String.format("%.12f", Double.valueOf(result))).doubleValue();
	}
	
	public static final double div(double a, double b) {
		double remainder = 0;
		double result = 0;
		long index = 0;
		
		double sub = a;
		if(sub - b < 0) {
			remainder = a;
		} else {
			double lastSub = sub;
			while((sub -= b) >= 0) {
				if(sub == lastSub) {
					break;
				}
				if(sub - b < 0) {
					remainder = sub;
				}
				index++;
			}
		}
		result = index;
		if(remainder != 0) {
			result += div(remainder, b);
		}
		return Double.valueOf(String.format("%.12f", Double.valueOf(result))).doubleValue();
		//return new DivisionRemainder(result, remainder);
	}*/
	
	public static final class DivisionRemainder {
		public final double quotient;
		public final double remainder;
		
		public DivisionRemainder(double quotient, double remainder) {
			this.quotient = quotient;
			this.remainder = remainder;
		}
		
		@Override
		public final String toString() {
			return this.quotient + " R " + this.remainder;
		}
		
		public static final DivisionRemainder divide(double a, double b) {
			double quotient = (b != 0.00D ? a / b : Double.NaN);
			double remainder = a % b;
			return new DivisionRemainder(quotient, remainder);
		}
		
	}
	
	/*public static final double div(double a, double b) {
		long remainder = 0;
		double result = 0;
		long index = 0;
		
		double sub = a;
		if(sub - b < 0) {
			remainder = 1;
		} else {
			while((sub -= b) >= 0) {
				if(sub - b < 0) {
					remainder = index;
				}
				index++;
			}
		}
		result = index;
		if(remainder != 0) {
			result += div(remainder, b);
		}
		return Double.valueOf(String.format("%.12f", Double.valueOf(result))).doubleValue();
	}*/
	
	/** @param start The starting(or original) value
	 * @param end The maximum value(or ending value)
	 * @param i The percentage(value between 0 and 1.0)
	 * @return The lerped double value */
	public static final double lerp(double start, double end, double i) {
		if(start != start || Double.isInfinite(start) || end != end || Double.isInfinite(end) || i != i || Double.isInfinite(i)) {
			return 0;
		}
		if(start == end) {
			return start;
		}
		return (start * (1.0D - i)) + (end * i);
	}
	
	/** @param start The starting(or original) value
	 * @param end The maximum value(or ending value)
	 * @param i The percentage(value between 0 and 1.0)
	 * @return The lerped float value */
	public static final float lerp(float start, float end, float i) {
		if(start != start || Float.isInfinite(start) || end != end || Float.isInfinite(end) || i != i || Float.isInfinite(i)) {
			return 0;
		}
		if(start == end) {
			return start;
		}
		return (start * (1.0F - i)) + (end * i);
	}
	
	public static final Vector3f rotate(Vector3f r, Quaternion rotation) {
		Quaternion conjugate = rotation.conjugate();
		Quaternion w = rotation.mul(r).mul(conjugate);
		return new Vector3f(w.getX(), w.getY(), w.getZ());
	}
	
	public static final Vector3f normalize(Vector3f q) {
		float length = q.length();
		return new Vector3f(q.x / length, q.y / length, q.z / length);
	}
	
	public static final Vector3f cross(Vector3f q, Vector3f r) {
		float x_ = (q.getY() * r.getZ()) - (q.getZ() * r.getY());
		float y_ = (q.getZ() * r.getX()) - (q.getX() * r.getZ());
		float z_ = (q.getX() * r.getY()) - (q.getY() * r.getX());
		return new Vector3f(x_, y_, z_);
	}
	
	public static final Vector3f add(Vector3f q, Vector3f r) {
		return new Vector3f(q.getX() + r.getX(), q.getY() + r.getY(), q.getZ() + r.getZ());
	}
	
	public static final Vector3f mul(Vector3f q, Vector3f r) {
		return new Vector3f(q.getX() * r.getX(), q.getY() * r.getY(), q.getZ() * r.getZ());
	}
	
	public static final Vector2f add(Vector2f q, Vector2f r) {
		return new Vector2f(q.getX() + r.getX(), q.getY() + r.getY());
	}
	
	public static final Vector2f div(Vector2f v, float r) {
		return new Vector2f(v.getX() / r, v.getY() / r);
	}
	
	public static final Vector2f sub(Vector2f v, float x, float y) {
		return new Vector2f(v.getX() - x, v.getY() - y);
	}
	
	public static final ArrayList<Long> getFactorsOf(long num, boolean includeNum) {
		long incrementer = 1;
		if(num % 2 != 0) {
			incrementer = 2; //only test the odd ones
		}
		ArrayList<Long> list = new ArrayList<>();
		for(long i = 1; i <= num / 2; i += incrementer) {
			if(num % i == 0) {
				list.add(Long.valueOf(i));
			}
		}
		if(includeNum) {
			list.add(Long.valueOf(num));
		}
		return list;
	}
	
	public static final int div(final int a, final int b, final int posBase, final int negBase) {
		final int base = a < 0 ? negBase : posBase;
		final int div = (a + base) / b;
		return div - base;
	}
	
	/** Runs a simple test to test some experimental methods
	 * 
	 * @param args System command line arguments */
	public static final void main1(String[] args) {
		//Maximum safe value for double and long values together(in terms of movement): 92233720368
		System.out.println("Float max value: " + new BigDecimal(Float.MAX_VALUE).toPlainString() + ";\r\nMin value: " + new BigDecimal(Float.MIN_VALUE).toPlainString());
		System.out.println("Long max value: " + new BigDecimal(Long.MAX_VALUE).toPlainString() + ";\r\nMin value: " + new BigDecimal(Long.MIN_VALUE).toPlainString());
		System.out.println("Double max value: " + new BigDecimal(Double.MAX_VALUE).toPlainString() + ";\r\nMin value: " + new BigDecimal(Double.MIN_VALUE).toPlainString());
		/*System.out.println("0 lerped to 100 at 75 percent: " + lerp(0, 100, (75.00F / 100.00F)));
		System.out.println("73 lerped to 16 at 24 percent: " + lerp(73, 16, (24.00F / 100.00F)));
		System.out.println("0.0002222222222222 limited to 5 decimal places: " + limitDecimalToNumberOfPlaces(0.0002222222222222f, 5));
		System.out.println("0.0002222222222222 formatted to 12 decimal places: " + Double.valueOf(String.format("%.12f", Double.valueOf(0.0002222222222222))));
		System.out.println("0.0 limited to 5 decimal places: " + limitDecimalToNumberOfPlaces(0.0f, 5));*/
		
		/*System.out.println("5 x 6.1: " + mult(5.0, 6.1) + "; real math: " + (5.0 * 6.1));
		System.out.println("6.1 x 5: " + mult(6.1, 5.0) + "; real math: " + (6.1 * 5));
		System.out.println("6 x 6.1: " + mult(6.0, 6.1) + "; real math: " + (6.0 * 6.1));
		System.out.println("6.1 x 6: " + mult(6.1, 6.0) + "; real math: " + (6.1 * 6.0));
		System.out.println("6.1 x 6.1: " + mult(6.1, 6.1) + "; real math: " + (6.1 * 6.1));
		System.out.println("12 x 12: " + mult(12, 12) + "; real math: " + (12.0 * 12.0));
		System.out.println("0.25 x 2: " + mult(0.25, 2.0) + "; real math: " + (0.25 * 2.00));
		System.out.println("2 x 0.25: " + mult(2.0, 0.25) + "; real math: " + (2.0 * 0.25));
		System.out.println("3.751 x 5.7: " + mult(3.751, 5.7) + "; real math: " + (3.751 * 5.7));
		
		System.out.println("1 / 2: " + div(1.00, 2.00) + "; real math: " + (1.00 / 2.00));
		System.out.println("3 / 17: " + div(3.00, 17.00) + "; real math: " + (3.00 / 17.00));
		System.out.println("17 / 3: " + div(17.00, 3.00) + "; real math: " + (17.00 / 3.00));
		System.out.println("8 / 7: " + div(8.00, 7.00) + "; real math: " + (8.00 / 7.00));
		System.out.println("5 / 7: " + div(5.00, 7.00) + "; real math: " + (5.00 / 7.00));
		System.out.println("0 / 7: " + div(0.00, 7.00) + "; real math: " + (0.00 / 7.00));
		System.out.println("5 / 0: " + div(5.00, 0.00) + "; real math: " + (5.00 / 0.00));
		System.out.println("0 / 0: " + div(0.00, 0.00) + "; real math: " + (0.0 / 0.0));*/
		
		/*for(int i = 0; i <= Long.MAX_VALUE; i++) {
			String print = "";
			ArrayList<Long> factors = getFactorsOf(i, false);
			long sum = 0;
			if(factors.size() > 0) {
				for(Long factor : factors) {
					print += factor + ", ";
					sum += factor.longValue();
				}
				print = print.substring(0, print.length() - 2) + "\r\n";
			} else {
				print = i + ": null";
			}
			boolean prime = sum == 1;
			boolean perfect = sum == i;
			print = print.trim();
			if(!print.isEmpty() && (perfect)) {
				System.out.println(i + (perfect ? "(Perfect Number!)" : "") + (prime ? "(Prime Number!)" : "") + ": " + print);
			}
		}*/
		
		final int posBase = 0;
		final int negBase = 1;
		final int divisor = 512;
		for(int i = -515; i < 516; i++) {
			System.out.println(i + " / " + divisor + ": " + div(i, divisor, posBase, negBase));
		}
		List<? extends Number> decimals = Arrays.asList(boxDoubles(12, 123.12345, 0.23, 0.97, 2341234.212431324, 7.945, 2147327.63751));
		for(Number n : decimals) {
			double d = n.doubleValue();
			System.out.println("[" + d + "]: " + roundTo(d, 0));
		}
		System.out.println("=============\r\n\r\n");
		for(Number n : decimals) {
			double d = n.doubleValue();
			System.out.println("[" + d + "]: " + new BigDecimal(getDecimalPartOf(d)).toPlainString());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> Class<T> findClass(T... array) {
		for(T t : array) {
			if(t != null) {
				return (Class<T>) t.getClass();
			}
		}
		return null;
	}
	
	public static final void printObj(Object obj) {
		if(obj instanceof Iterable) {
			for(Object o : (Iterable<?>) obj) {
				printObj(o);
			}
		} else if(obj instanceof Object[]) {
			for(Object o : (Object[]) obj) {
				printObj(o);
			}
		} else {
			System.out.println(obj);
		}
	}
	
	public static final void main(String[] args) {
		@SuppressWarnings("unchecked")
		List<Object>[] foo = (List<Object>[]) new List<?>[16];
		for(int i = 0; i < foo.length; i++) {
			foo[i] = new ArrayList<>();
			for(int j = 1; j <= 5;) {
				foo[i].add(Integer.valueOf(j++));
				foo[i].add(null);
				ArrayList<Object> list = new ArrayList<>();
				list.add(Integer.valueOf(j - 1));
				list.add(null);
				foo[i].add(list);
				Object[] lel = new Object[2];
				lel[0] = Integer.valueOf(j - 1);
				lel[1] = null;//How to clean this????!?!?!?!?!?!?!?!
				foo[i].add(lel);
			}
		}
		Object[] bar = clean(foo);
		printObj(bar);
	}
	
	public static final <T> Iterable<T> clean(Iterable<T> array) {
		Iterator<T> it = array.iterator();
		while(it.hasNext()) {
			T t = it.next();
			if(t == null) {
				it.remove();
			} else {
				if(t instanceof Iterable) {
					clean((Iterable<?>) t);
				}
			}
		}
		return array;
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static final <T> T[] clean(T... array) {
		Class<T> clazz = findClass(array);
		if(clazz == null) {
			return array;
		}
		int length = 0;
		for(T t : array) {
			if(t != null) {
				length++;
			}
		}
		T[] rtrn = (T[]) Array.newInstance(clazz, length);
		int i = 0;
		for(T t : array) {
			if(t != null) {
				if(t instanceof Iterable) {
					t = (T) clean((Iterable<?>) t);
				} else if(t instanceof Object[]) {
					t = (T) clean((Object[]) t);
				}
				rtrn[i++] = t;
			}
		}
		return rtrn;
	}
	
	public static final double[] unboxDoubles(Double... doubles) {
		double[] boxed = new double[doubles.length];
		int i = 0;
		for(Double d : doubles) {
			boxed[i++] = d.doubleValue();
		}
		return boxed;
	}
	
	public static final Double[] boxDoubles(double... doubles) {
		Double[] boxed = new Double[doubles.length];
		int i = 0;
		for(double d : doubles) {
			boxed[i++] = Double.valueOf(d);
		}
		return boxed;
	}
	
	public static final long[] unboxLongs(Long... longs) {
		long[] boxed = new long[longs.length];
		int i = 0;
		for(Long l : longs) {
			boxed[i++] = l.longValue();
		}
		return boxed;
	}
	
	public static final Long[] boxLongs(long... longs) {
		Long[] boxed = new Long[longs.length];
		int i = 0;
		for(long l : longs) {
			boxed[i++] = Long.valueOf(l);
		}
		return boxed;
	}
	
	public static final float[] unboxFloat(Float... floats) {
		float[] boxed = new float[floats.length];
		int i = 0;
		for(Float f : floats) {
			boxed[i++] = f.floatValue();
		}
		return boxed;
	}
	
	public static final Float[] boxFloats(float... floats) {
		Float[] boxed = new Float[floats.length];
		int i = 0;
		for(float f : floats) {
			boxed[i++] = Float.valueOf(f);
		}
		return boxed;
	}
	
	public static final int[] unboxIntegers(Integer... ints) {
		int[] boxed = new int[ints.length];
		int i = 0;
		for(Integer n : ints) {
			boxed[i++] = n.intValue();
		}
		return boxed;
	}
	
	public static final Integer[] boxIntegers(int... ints) {
		Integer[] boxed = new Integer[ints.length];
		int i = 0;
		for(int n : ints) {
			boxed[i++] = Integer.valueOf(n);
		}
		return boxed;
	}
	
	public static final char[] unboxCharacters(Character... chars) {
		char[] boxed = new char[chars.length];
		int i = 0;
		for(Character d : chars) {
			boxed[i++] = d.charValue();
		}
		return boxed;
	}
	
	public static final Character[] boxCharacters(char... chars) {
		Character[] boxed = new Character[chars.length];
		int i = 0;
		for(char d : chars) {
			boxed[i++] = Character.valueOf(d);
		}
		return boxed;
	}
	
	public static final short[] unboxShorts(Short... shorts) {
		short[] boxed = new short[shorts.length];
		int i = 0;
		for(Short s : shorts) {
			boxed[i++] = s.shortValue();
		}
		return boxed;
	}
	
	public static final Short[] boxShorts(short... shorts) {
		Short[] boxed = new Short[shorts.length];
		int i = 0;
		for(short s : shorts) {
			boxed[i++] = Short.valueOf(s);
		}
		return boxed;
	}
	
	public static final byte[] unboxBytes(Byte... bytes) {
		byte[] boxed = new byte[bytes.length];
		int i = 0;
		for(Byte b : bytes) {
			boxed[i++] = b.byteValue();
		}
		return boxed;
	}
	
	public static final Byte[] boxBytes(byte... bytes) {
		Byte[] boxed = new Byte[bytes.length];
		int i = 0;
		for(byte b : bytes) {
			boxed[i++] = Byte.valueOf(b);
		}
		return boxed;
	}
	
	public static final boolean[] unboxBooleans(Boolean... booleans) {
		boolean[] boxed = new boolean[booleans.length];
		int i = 0;
		for(Boolean b : booleans) {
			boxed[i++] = b.booleanValue();
		}
		return boxed;
	}
	
	public static final Boolean[] boxBooleans(boolean... booleans) {
		Boolean[] boxed = new Boolean[booleans.length];
		int i = 0;
		for(boolean b : booleans) {
			boxed[i++] = Boolean.valueOf(b);
		}
		return boxed;
	}
	
	/*public static final void[] unboxVoids(Void... voids) {
		void[] boxed = new void[voids.length];
		int i = 0;
		for(Void b : voids) {
			boxed[i++] = b.voidValue();
		}
		return boxed;
	}
	
	public static final Void[] boxVoids(void... voids) {
		Void[] boxed = new Void[voids.length];
		int i = 0;
		for(void b : voids) {
			boxed[i++] = Void.valueOf(b);
		}
		return boxed;
	}*/
	
	public static final strictfp double nthRoot(final double x, final int nthRoot) {
		return nthRoot(x, nthRoot, 393211);
	}
	
	public static final strictfp double nthRoot(final double x, final int nthRoot, int significantFigures) {
		if(x == 0.0D) {
			return x;
		}
		final double x0 = 600000;
		double result = x0;
		double lastResult = result;
		double sign = x >= 0 ? 1.0D : -1.0D;
		boolean lastFlag = false;
		significantFigures = significantFigures < 1 ? 393211 : significantFigures;
		
		final double root = (1.0 / nthRoot);
		
		for(int i = 1; i <= significantFigures; i++) {
			//System.out.println("[" + i + "]=" + result);
			double n = root/*0.5D*/ * (result + ((x / result) * sign));
			result = n == n ? n : result;
			if(result == lastResult) {
				if(lastFlag) {
					//System.err.println(x + ": sigFigs: " + i + ": " + result);
					return result * sign;
				}
				lastFlag = true;
				//System.err.println(x + ": sigFigs: " + i + ": " + result);=
				//break;
			}
			lastResult = result;
		}
		return result * sign;
	}
	
}
