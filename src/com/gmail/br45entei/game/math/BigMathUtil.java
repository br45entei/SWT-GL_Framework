package com.gmail.br45entei.game.math;

import com.gmail.br45entei.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/** Static utility class housing common mathematical functions using the {@link BigInteger} and {@link BigDecimal} types.
 *
 * @author Brian_Entei &lt;br45entei&#064gmail.com&gt; */
public final strictfp class BigMathUtil {
	
	public static final MathContext MC_CEIL = new MathContext(128, RoundingMode.CEILING);
	public static final MathContext MC = new MathContext(128, RoundingMode.HALF_EVEN);
	public static final MathContext MC_FLOOR = new MathContext(128, RoundingMode.FLOOR);
	
	public static BigDecimal magnitude(BigDecimal[] vector, MathContext mc) {
		BigDecimal result = new BigDecimal(0.0);
		for(BigDecimal v : vector) {
			result = result.add(v.pow(2, mc), mc);
		}
		return result.sqrt(mc);
	}
	
	public static BigInteger magnitude(BigInteger[] vector) {
		BigInteger result = BigInteger.valueOf(0);
		for(BigInteger v : vector) {
			result = result.add(v.pow(2));
		}
		return result.sqrt();
	}
	
	public static BigDecimal magnitude(BigDecimal[] vector) {
		return magnitude(vector, MC);
	}
	
	public static BigDecimal[] add(BigDecimal[] vector1, BigDecimal[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigDecimal[] result = new BigDecimal[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].add(vector2[i], MC);
		}
		return result;
	}
	
	public static BigInteger[] add(BigInteger[] vector1, BigInteger[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigInteger[] result = new BigInteger[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].add(vector2[i]);
		}
		return result;
	}
	
	public static BigDecimal[] subtract(BigDecimal[] vector1, BigDecimal[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigDecimal[] result = new BigDecimal[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].subtract(vector2[i], MC);
		}
		return result;
	}
	
	public static BigInteger[] subtract(BigInteger[] vector1, BigInteger[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigInteger[] result = new BigInteger[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].subtract(vector2[i]);
		}
		return result;
	}
	
	public static BigDecimal[] multiply(BigDecimal[] vector1, BigDecimal[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigDecimal[] result = new BigDecimal[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].multiply(vector2[i], MC);
		}
		return result;
	}
	
	public static BigInteger[] multiply(BigInteger[] vector1, BigInteger[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigInteger[] result = new BigInteger[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].multiply(vector2[i]);
		}
		return result;
	}
	
	public static BigDecimal[] divide(BigDecimal[] vector1, BigDecimal[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigDecimal[] result = new BigDecimal[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].divide(vector2[i], MC);
		}
		return result;
	}
	
	public static BigInteger[] divide(BigInteger[] vector1, BigInteger[] vector2) {
		int length = Math.min(vector1.length, vector2.length);
		BigInteger[] result = new BigInteger[length];
		for(int i = 0; i < length; i++) {
			result[i] = vector1[i].divide(vector2[i]);
		}
		return result;
	}
	
	public static BigDecimal[] normalize(BigDecimal[] vector, MathContext mc) {
		BigDecimal length = magnitude(vector, mc);
		BigDecimal[] result = new BigDecimal[vector.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = vector[i].divide(length, mc);
		}
		return result;
	}
	
	public static BigDecimal[] normalize(BigDecimal[] vector) {
		return normalize(vector, MC);
	}
	
	public static BigInteger[] normalize(BigInteger[] vector) {
		BigInteger length = magnitude(vector);
		BigInteger[] result = new BigInteger[vector.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = vector[i].divide(length);
		}
		return result;
	}
	
	public static BigDecimal distance(BigDecimal[] start, BigDecimal[] end) {
		return magnitude(subtract(start, end));
	}
	
	public static String getWholePartOf(BigDecimal d) {
		String str = d.toPlainString();
		if(str.equals("Infinity") || str.equals("-Infinity") || str.equals("NaN")) {
			return str;
		}
		int indexOfDecimalPoint = str.indexOf(".");
		if(indexOfDecimalPoint != -1) {
			return str.substring(0, indexOfDecimalPoint);
		}
		return str;
	}
	
	public static String getDecimalPartOf(BigDecimal d) {
		String str = d.toPlainString();
		if(str.equals("Infinity") || str.equals("-Infinity") || str.equals("NaN")) {
			return "";
		}
		int indexOfDecimalPoint = str.indexOf(".");
		if(indexOfDecimalPoint == -1) {
			return "";
		}
		return str.substring(indexOfDecimalPoint);
	}
	
	public static String limitDecimalToNumberOfPlaces(BigDecimal d, int n, boolean pad) {
		String str = d.toPlainString();
		if(str.equals("Infinity") || str.equals("-Infinity") || str.equals("NaN")) {
			return str;
		}
		String whole = getWholePartOf(d);
		String decimal = getDecimalPartOf(d);
		if(decimal.isEmpty()) {
			if(pad) {
				decimal = ".".concat(StringUtil.lineOf('0', n));
			} else {
				return str;
			}
		} else {
			if(!decimal.startsWith(".")) {
				return str;//whole;
			}
			decimal = decimal.substring(1, Math.min(decimal.length(), n + 1));
			
			int length = decimal.length();
			if(length > n) {
				decimal = decimal.substring(0, n);
			} else if(length < n && pad) {
				int diff = n - length;
				decimal = decimal.concat(StringUtil.lineOf('0', diff));
			}
			decimal = ".".concat(decimal);
		}
		return whole.concat(decimal);
	}
	
	public static String vectorToString(BigDecimal[] vector) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			sb.append(vector[1].toPlainString());
		}
		return sb.append(")").toString();
	}
	
	public static String vectorToString(BigInteger[] vector) {
		StringBuilder sb = new StringBuilder("(");
		for(int i = 0; i < vector.length; i++) {
			sb.append(vector[1].toString());
		}
		return sb.append(")").toString();
	}
	
	public static final void main(String[] args) {
		
	}
	
}
