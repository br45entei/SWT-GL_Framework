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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Utility class used for housing common file-related functions.
 * 
 * @author Brian_Entei */
public class FileUtil {
	
	/** Wraps the given {@link OutputStream} with a new {@link PrintStream} that
	 * uses the given line separator.
	 * 
	 * @param out The output stream to wrap
	 * @param lineSeparator The line separator that the returned PrintStream
	 *            will use. If <tt><b>null</b></tt>, a new {@link PrintStream}
	 *            is simply created and returned.
	 * @return The resulting PrintStream */
	public static final PrintStream wrapOutputStream(final OutputStream out, final String lineSeparator) {
		if(lineSeparator == null) {
			return new PrintStream(out, true);
		}
		final String originalLineSeparator = CodeUtil.getProperty("line.separator");
		try {
			CodeUtil.setProperty("line.separator", lineSeparator);
			return new PrintStream(out, true);
		} finally {
			CodeUtil.setProperty("line.separator", originalLineSeparator);
		}
	}
	
	/** Reads and returns a single line of text from the given input stream,
	 * using the given charset to convert the read data into a string.
	 * 
	 * @param in The {@link InputStream} to read the text from
	 * @param trim Whether or not the end of the line should have any existing
	 *            (single) carriage return character removed
	 * @param charset The {@link Charset} to use when converting the read data
	 *            into a {@link String}
	 * @return The read line, or <tt><b>null</b></tt> if the end of the stream
	 *         was reached
	 * @throws IOException Thrown if a read error occurs */
	public static final String readLine(InputStream in, boolean trim, Charset charset) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while((b = in.read()) != -1) {
			if(b == 10) {//LF character '\n' (line feed)
				break;
			}
			baos.write(b);
		}
		if(b == -1 && baos.size() == 0) {
			return null;
		}
		byte[] data = baos.toByteArray();
		String line = new String(data, 0, data.length, charset);
		return trim && line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
	}
	
	/** Reads and returns a single line of text from the given input stream,
	 * using the {@link StandardCharsets#ISO_8859_1 ISO_8859_1} standard charset
	 * to convert the read data into a string.
	 * 
	 * @param in The {@link InputStream} to read the text from
	 * @param trim Whether or not the end of the line should have any existing
	 *            (single) carriage return character removed
	 * @return The read line, or <tt><b>null</b></tt> if the end of the stream
	 *         was reached
	 * @throws IOException Thrown if a read error occurs */
	public static final String readLine(InputStream in, boolean trim) throws IOException {
		return readLine(in, trim, StandardCharsets.ISO_8859_1);
	}
	
	/** Reads and returns a single line of text from the given input stream,
	 * using the {@link StandardCharsets#ISO_8859_1 ISO_8859_1} standard charset
	 * to convert the read data into a string.
	 * 
	 * @param in The {@link InputStream} to read the text from
	 * @return The read line, or <tt><b>null</b></tt> if the end of the stream
	 *         was reached
	 * @throws IOException Thrown if a read error occurs */
	public static final String readLine(InputStream in) throws IOException {
		return readLine(in, true);
	}
	
}
