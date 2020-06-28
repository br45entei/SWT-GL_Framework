/*******************************************************************************
 * 
 * Copyright (C) 2020 Brian_Entei (br45entei@gmail.com)
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
package com.gmail.br45entei.util;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.text.similarity.LevenshteinDistance;

/** @author Brian_Entei */
public class CodeUtil {
	
	/** Retrieves the specified system property with the proper authority.
	 * 
	 * @param property The name of the system property to be retrieved
	 * @return The string value of the system property, or <tt><b>null</b></tt>
	 *         if there is no property with that key. */
	public static final String getProperty(String property) {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				return System.getProperty(property);
			}
		});
	}
	
	/** Sets the specified system property with the proper authority.
	 * 
	 * @param property The name of the system property to be set
	 * @param value The value that the system property will be set to
	 * @return The previous value of the system property, or
	 *         <tt><b>null</b></tt> if it did not have one. */
	public static final String setProperty(String property, String value) {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				return System.setProperty(property, value);
			}
		});
	}
	
	/** @return Whether or not a 64 bit system was detected */
	public static boolean isJvm64bit() {
		for(String s : new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"}) {
			String s1 = System.getProperty(s);
			if((s1 != null) && s1.contains("64")) {
				return true;
			}
		}
		return false;
	}
	
	/** Enum class differentiating types of operating systems
	 * 
	 * @author Brian_Entei */
	public static enum EnumOS {
		/** Unix operating systems */
		UNIX,
		/** Linux operating systems */
		LINUX,
		/** Salaries operating systems */
		SOLARIS,
		/** Android operating systems */
		ANDROID,
		/** Windows operating systems */
		WINDOWS,
		/** Mac/OSX */
		OSX,
		/** An unknown operating system */
		UNKNOWN;
	}
	
	/** @return The type of operating system that java is currently running
	 *         on */
	public static EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.OSX : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.UNIX : (s.contains("android") ? EnumOS.ANDROID : EnumOS.UNKNOWN))))));
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid boolean value */
	public static final boolean isBoolean(String str) {
		return str == null ? false : str.equals("true") || str.equals("false");
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid byte value */
	public static final boolean isByte(String str) {
		try {
			Byte.parseByte(str);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid short value */
	public static final boolean isShort(String str) {
		try {
			Short.parseShort(str);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid integer value */
	public static final boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid float value */
	public static final boolean isFloat(String str) {
		try {
			Float.parseFloat(str);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid long value */
	public static final boolean isLong(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid double value */
	public static final boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}
	
	/** @param str The string to check
	 * @return Whether or not the given string is a valid UUID */
	public static final boolean isUUID(String str) {
		try {
			return UUID.fromString(str) != null;
		} catch(IllegalArgumentException ex) {
			return false;
		}
	}
	
	private static volatile boolean debugLoggingEnabled = false;
	
	/** @return Whether or not debug logging is enabled for the various debug
	 *         print functions in this class */
	public static final boolean isDebugLoggingEnabled() {
		return debugLoggingEnabled;
	}
	
	/** Sets whether or not the various debug print functions in this class are
	 * enabled.
	 * 
	 * @param flag Whether or not debug logging should be enabled for the
	 *            various debug print functions in this class */
	public static final void setDebugLoggingEnabled(boolean flag) {
		debugLoggingEnabled = flag;
	}
	
	/** @param obj The object to be printed to the standard output stream */
	public static final void print(Object obj) {
		System.out.print(Objects.toString(obj));
	}
	
	/** @param obj The object to be printed to the standard output stream */
	public static final void println(Object obj) {
		System.out.println(Objects.toString(obj));
	}
	
	/** @param obj The object to be printed to the standard error stream */
	public static final void printErr(Object obj) {
		System.err.print(Objects.toString(obj));
	}
	
	/** @param obj The object to be printed to the standard error stream */
	public static final void printErrln(Object obj) {
		System.err.println(Objects.toString(obj));
	}
	
	/** @param obj The object to be printed to the standard output stream (if
	 *            debug logging is enabled) */
	public static final void printDebug(Object obj) {
		if(debugLoggingEnabled) {
			System.out.print(Objects.toString(obj));
		}
	}
	
	/** @param obj The object to be printed to the standard output stream (if
	 *            debug logging is enabled) */
	public static final void printlnDebug(Object obj) {
		if(debugLoggingEnabled) {
			System.out.println(Objects.toString(obj));
		}
	}
	
	/** @param obj The object to be printed to the standard error stream (if
	 *            debug logging is enabled) */
	public static final void printErrDebug(Object obj) {
		if(debugLoggingEnabled) {
			System.err.print(Objects.toString(obj));
		}
	}
	
	/** @param obj The object to be printed to the standard error stream (if
	 *            debug logging is enabled) */
	public static final void printErrlnDebug(Object obj) {
		if(debugLoggingEnabled) {
			System.err.println(Objects.toString(obj));
		}
	}
	
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
		String padStr = pad ? lineOf('0', numOfPlaces) : "0";
		if(Double.doubleToLongBits(decimal) == Double.doubleToLongBits(0.0)) {
			return "0" + (numOfPlaces != 0 ? "." + padStr : "");
		}
		if(Double.doubleToLongBits(decimal) == Double.doubleToLongBits(-0.0)) {
			return "-0" + (numOfPlaces != 0 ? "." + padStr : "");
		}
		numOfPlaces += 1;
		String whole = Double.isFinite(decimal) ? getWholePartOf(decimal) : Double.isInfinite(decimal) ? "Infinity" : "NaN";
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
		
		String d = Double.isFinite(decimal) ? getDecimalPartOf(decimal) : "";
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
			d += lineOf('0', numOfPlaces - d.length());
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
	
	/** Creates a new writable {@link Entry} that maps the given key and value.
	 * 
	 * @param <K> The type of the entry's key
	 * @param <V> The type of the entry's value
	 * @param key The entry's key
	 * @param value The entry's value
	 * @param allowNullValues Whether or not the entry will allow
	 *            <tt><b>null</b></tt> values to be set
	 * @return The newly created writable {@link Entry} which maps the given key
	 *         and value */
	public static final <K, V> Entry<K, V> createWritableEntry(final K key, final V value, boolean allowNullValues) {
		Entry<K, V> entry = new Entry<>() {
			volatile V value = null;
			
			@Override
			public K getKey() {
				return key;
			}
			
			@Override
			public V getValue() {
				return this.value;
			}
			
			@Override
			public V setValue(V value) {
				if(value == null && !allowNullValues) {
					throw new NullPointerException();
				}
				V oldValue = this.value;
				this.value = value;
				return oldValue;
			}
		};
		entry.setValue(value);
		return entry;
	}
	
	/** Creates a new writable {@link Entry} that maps the given key and value.
	 * 
	 * @param <K> The type of the entry's key
	 * @param <V> The type of the entry's value
	 * @param key The entry's key
	 * @param value The entry's value
	 * @return The newly created writable {@link Entry} which maps the given key
	 *         and value */
	public static final <K, V> Entry<K, V> createWritableEntry(final K key, final V value) {
		return createWritableEntry(key, value, true);
	}
	
	/** Creates a new read-only {@link Entry} that maps the given key and value.
	 * 
	 * @param <K> The type of the entry's key
	 * @param <V> The type of the entry's value
	 * @param key The entry's key
	 * @param value The entry's value
	 * @param throwUnsupportedOperationException Whether or not an
	 *            {@link UnsupportedOperationException} should be thrown if the
	 *            entry's {@link Entry#setValue(Object) setValue(...)} method is
	 *            called
	 * @return The newly created read-only {@link Entry} which maps the given
	 *         key and value */
	public static final <K, V> Entry<K, V> createReadOnlyEntry(final K key, final V value, final boolean throwUnsupportedOperationException) {
		return new Entry<>() {
			@Override
			public K getKey() {
				return key;
			}
			
			@Override
			public V getValue() {
				return value;
			}
			
			@Override
			public V setValue(V value) {
				if(throwUnsupportedOperationException) {
					throw new UnsupportedOperationException();
				}
				return this.getValue();
			}
		};
	}
	
	/** Creates a new read-only {@link Entry} that maps the given key and value.
	 * 
	 * @param <K> The type of the entry's key
	 * @param <V> The type of the entry's value
	 * @param key The entry's key
	 * @param value The entry's value
	 * @return The newly created {@link Entry} which maps the given key and
	 *         value */
	public static final <K, V> Entry<K, V> createReadOnlyEntry(final K key, final V value) {
		return createReadOnlyEntry(key, value, false);
	}
	
	/** @param buf The ByteBuffer whose data will be returned
	 * @return the given ByteBuffer's data. */
	public static final byte[] getDataFrom(ByteBuffer buf) {
		byte[] data = new byte[buf.capacity()];
		if(buf.hasArray()) {
			//return buf.array();
			System.arraycopy(buf.array(), 0, data, 0, data.length);
			return data;
		}
		buf.rewind();
		for(int i = 0; i < data.length; i++) {
			data[i] = buf.get();
		}
		return data;
	}
	
	/** Causes the currently executing thread to sleep (temporarily cease
	 * execution) for the specified number of milliseconds, subject to
	 * the precision and accuracy of system timers and schedulers. The thread
	 * does not lose ownership of any monitors.
	 * 
	 * @param millis The length of time to sleep in milliseconds
	 * @return An InterruptedException if the thread was interrupted while
	 *         sleeping
	 * @throws IllegalArgumentException Thrown if the value of <tt>millis</tt>
	 *             is negative */
	public static final InterruptedException sleep(long millis) throws IllegalArgumentException {
		try {
			Thread.sleep(millis);
			return null;
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
			return ex;
		}
	}
	
	/** Compares each of the given objects within the collection with the given
	 * target object, and then returns the one that matches the given target
	 * object the closest.
	 * 
	 * @param <T> The type of the collection's objects to compare
	 * @param collection The collection of objects to compare
	 * @param target The target object whose closest match is desired
	 * @return The object that matches the given target object the closest, or
	 *         <tt><b>null</b></tt> if no match was found (usually meaning the
	 *         given collection was empty) */
	public static final <T> T findClosestMatch(Collection<T> collection, T target) {
		int distance = Integer.MAX_VALUE;
		T closest = null;
		for(T compareObject : collection) {
			int currentDistance = LevenshteinDistance.getDefaultInstance().apply(compareObject.toString(), target.toString()).intValue();
			if(currentDistance < distance) {
				distance = currentDistance;
				closest = compareObject;
			}
		}
		return closest;
	}
	
	/** Compares each of the given objects within the array with the given
	 * target object by converting them into a {@link String}, and then returns
	 * the one that matches the given target object the closest.
	 * 
	 * @param <T> The type of the array of objects to compare
	 * @param array The array of objects to compare
	 * @param target The target object whose closest match is desired
	 * @param ignoreCase Whether or not the objects' case should be ignored when
	 *            converting to {@link String} for comparison
	 * @param startsWith Whether or not the objects should at least <em>start
	 *            with</em> the given target object's {@link String}
	 *            representation
	 * @return The object that matches the given target object the closest, or
	 *         <tt><b>null</b></tt> if no match was found (usually meaning the
	 *         given collection was empty) */
	public static final <T> T findClosestMatch(T[] array, T target, boolean ignoreCase, boolean startsWith) {
		int distance = Integer.MAX_VALUE;
		T closest = null;
		for(T compareObject : array) {
			String str1 = compareObject.toString();
			String str2 = target.toString();
			if(ignoreCase) {
				str1 = str1.toUpperCase();
				str2 = str2.toUpperCase();
			}
			int currentDistance = LevenshteinDistance.getDefaultInstance().apply(str1, str2).intValue();
			if(currentDistance < distance && (startsWith ? str1.startsWith(str2) : true)) {
				distance = currentDistance;
				closest = compareObject;
			}
		}
		return closest;
	}
	
	/** Compares the given key with all of the keys in the given map and returns
	 * the one that matches, or returns the given default key if no match was
	 * made.
	 * 
	 * @param <V> The type of the values in the given map
	 * @param map The map to search through
	 * @param key The {@link String} key to search for
	 * @param def The default key that will be returned if no matching key is
	 *            found in the map
	 * @param ignoreCase Whether or not the string comparison between the keys
	 *            should be case-insensitive
	 * @return The matching key in the given map if found, <tt><b>null</b></tt>
	 *         otherwise */
	public static final <V> String getMatchingKeyInMap(Map<String, V> map, String key, String def, boolean ignoreCase) {
		if(key == null) {
			return null;
		}
		if(map.containsKey(key)) {
			return key;
		}
		for(String k : map.keySet()) {
			if(ignoreCase ? key.equalsIgnoreCase(k) : key.equals(k)) {
				return k;
			}
		}
		return def;
	}
	
	/** Compares the given key with all of the keys in the given map and returns
	 * the one that matches, or returns the given default key if no match was
	 * made.
	 * 
	 * @param <K> The type of the keys in the given map
	 * @param <V> The type of the values in the given map
	 * @param map The map to search through
	 * @param key The {@link String} key to search for
	 * @param def The default key that will be returned if no matching key is
	 *            found in the map
	 * @return The matching key in the given map if found, <tt><b>null</b></tt>
	 *         otherwise */
	public static final <K, V> K getMatchingKeyInMap(Map<K, V> map, K key, K def) {
		if(key == null) {
			return null;
		}
		if(map.containsKey(key)) {
			return key;
		}
		for(K k : map.keySet()) {
			if(key.equals(k)) {
				return k;
			}
		}
		return def;
	}
	
	/** Compares the given key with all of the keys in the given map and returns
	 * the one that matches, if one was found, or <tt><b>null</b></tt>
	 * otherwise.
	 * 
	 * @param <V> The type of the values in the given map
	 * @param map The map to search through
	 * @param uuid The {@link String}-representation of the {@link UUID} key
	 *            whose value will be returned
	 * @return The value that the key has stored in the given map, or
	 *         <tt><b>null</b></tt> if the map didn't have the specified key
	 *         mapped to a value */
	public static final <V> V getUUIDMapValue(Map<? extends UUID, V> map, String uuid) {
		for(Entry<? extends UUID, V> entry : map.entrySet()) {
			UUID check = entry.getKey();
			if(check != null && check.toString().equalsIgnoreCase(uuid)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/** Compares the given key with all of the keys in the given map and returns
	 * the one that matches, if one was found, or <tt><b>null</b></tt>
	 * otherwise.
	 * 
	 * @param <V> The type of the values in the given map
	 * @param map The map to search through
	 * @param uuid The {@link UUID} key whose value will be returned
	 * @return The value that the key has stored in the given map, or
	 *         <tt><b>null</b></tt> if the map didn't have the specified key
	 *         mapped to a value */
	public static final <V> V getUUIDMapValue(Map<? extends UUID, V> map, UUID uuid) {
		return getUUIDMapValue(map, uuid == null ? null : uuid.toString());
	}
	
	/** Checks the given map for the presence of the given {@link UUID} key.
	 * 
	 * @param <V> The type of the values in the given map
	 * @param map The map to search through
	 * @param uuid The {@link String}-representation of the {@link UUID} key
	 *            that will be searched for
	 * @return True if the map contains the given {@link UUID} key */
	public static final <V> boolean containsUUIDKey(Map<? extends UUID, V> map, String uuid) {
		for(UUID check : map.keySet()) {
			/*if(check == null && uuid == null) {
				return true;
			}*/
			if(check != null && check.toString().equalsIgnoreCase(uuid)) {
				return true;
			}
		}
		return false;
	}
	
	/** Checks the given map for the presence of the given {@link UUID} key.
	 * 
	 * @param <V> The type of the values in the given map
	 * @param map The map to search through
	 * @param uuid The {@link UUID} key that will be searched for
	 * @return True if the map contains the given {@link UUID} key */
	public static final <V> boolean containsUUIDKey(Map<? extends UUID, V> map, UUID uuid) {
		return containsUUIDKey(map, uuid == null ? null : uuid.toString());
	}
	
	/** @return The path of the file or folder containing this application code.
	 * @author <a href=
	 *         "https://stackoverflow.com/a/32766003/2398263">BullyWiiPlaza</a> */
	public static String getCodeSourcePath() {
		return new File(CodeUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
	}
	
	/** @return Whether or not this code is being run from within a .jar file
	 * @author <a href=
	 *         "https://stackoverflow.com/a/32766003/2398263">BullyWiiPlaza</a> */
	public static boolean runningFromJar() {
		return CodeUtil.getCodeSourcePath().contains(".jar");
	}
	
}
