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

import com.gmail.br45entei.util.CodeUtil.EnumOS;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

/** Utility class used for housing common file-related functions.<br>
 * You can also read file contents up to 2gb, rename files and folders, and log
 * strings of
 * text into files that<br>
 * automatically gzip when log file size reaches 8kb with a configurable root
 * directory for log files.
 * 
 * @author Brian_Entei,
 *         <a href="http://www.joapple.ca/fr">Jonathan</a>
 * @see #readFile(File)
 * @see #renameFile(File, String)
 * @see #getRootLogFolder()
 * @see #setRootLogFolder(File)
 * @see #logStr(String, String) */
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
		final String originalLineSeparator = AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				return System.getProperty("line.separator");
			}
			
		});
		try {
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					System.setProperty("line.separator", lineSeparator);
					return null;
				}
			});
			return new PrintStream(out, true);
		} finally {
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					System.setProperty("line.separator", originalLineSeparator);
					return null;
				}
			});
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
	
	/** Attempts to show the specified file or folder to the user using the
	 * platform's native filesystem.
	 * 
	 * @param file The file to show to the user
	 * @return Whether or not the operation was successful */
	public static final boolean showFileToUser(File file) {
		switch(Platform.get()) {
		case WINDOWS:
			if(file.isFile()) {
				try {
					Runtime.getRuntime().exec(String.format("explorer.exe /select,\"%s\"", file.getAbsolutePath()));
					return true;
				} catch(IOException ex) {
					ex.printStackTrace(System.err);
					System.err.flush();
					//return false;
				}
			} else {
				try {
					Desktop.getDesktop().browse(file.toURI());
					return true;
				} catch(IOException | UnsupportedOperationException | SecurityException | IllegalArgumentException ignored) {
				}
			}
			//$FALL-THROUGH$
		case LINUX:
		case MACOSX:
		case UNKNOWN:
		default:
			file = file.getParentFile() == null ? file : file.getParentFile();
			try {
				Desktop.getDesktop().browse(file.toURI());
				return true;
			} catch(IOException | UnsupportedOperationException | SecurityException | IllegalArgumentException ignored) {
				Display check = Display.getCurrent();
				boolean dispose = false;
				if(check == null || Thread.currentThread() != check.getThread()) {
					check = new Display();
					dispose = true;
				}
				try {
					return Program.launch(file.getAbsolutePath());
				} finally {
					if(dispose) {
						check.dispose();
					}
				}
			}
		}
	}
	
	/** Test to see ratio of file size between gzipped and plain text files;
	 * this is not API
	 * 
	 * @param args Program command line arguments */
	public static final void main(String[] args) {
		String logName = "test";
		for(int index = 0; index < 5; index++) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try(GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
				while(baos.toByteArray().length < 16103) {//14570) {//17636) {//19169) {//12288) {//8192) {
					String random = StringUtil.nextSessionID();
					byte[] r = random.getBytes(StandardCharsets.ISO_8859_1);
					gzip.write(r);
					logStr(logName, random);
				}
				gzip.flush();
			} catch(IOException e) {
				throw new Error("This should not have happened!", e);
			}
		}
	}
	
	private static volatile File rootLogDir = new File(System.getProperty("user.dir"));
	
	/** @param file The file whose contents will be read
	 * @return The file's contents, in a byte array
	 * @throws IOException Thrown if an I/O exception occurs */
	public static final byte[] readFile(File file) throws IOException {
		if(file == null || !file.isFile()) {
			return null;
		}
		try(FileInputStream in = new FileInputStream(file)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read;
			while((read = in.read()) >= 0) {
				baos.write(read);
			}
			return baos.toByteArray();
		}
	}
	
	/** @param file The file whose contents will be read
	 * @return The file's contents, in a byte array */
	public static final byte[] readFileSafe(File file) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(FileInputStream fis = new FileInputStream(file)) {
			byte[] buf = new byte[2048];
			int read;
			while((read = fis.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, read);
			}
		} catch(IOException ex) {
			System.err.println(String.format("Failed to read from file \"%s\":", file.getAbsolutePath()));
			ex.printStackTrace(System.err);
			System.err.flush();
		}
		byte[] bytes = baos.toByteArray();
		return bytes;
	}
	
	/** @param file The file whose contents will be read
	 * @return The file's contents as a string, where lines are delimited via
	 *         '{@code \r\n}'. */
	public static final String readFileBR(File file) {
		StringBuilder sb = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\r\n");
			}
		} catch(IOException ignored) {
		}
		return sb.toString();
	}
	
	/** @param file The file whose contents will be read
	 * @param charset The charset to use when reading the file data
	 * @return The file's contents as a string using the specified charset,
	 *         where lines are delimited via '{@code \r\n}'. */
	public static final String readFile(File file, Charset charset) {
		if(charset == null) {
			return readFileBR(file);
		}
		byte[] bytes = readFileSafe(file);
		String rtrn = new String(bytes, charset);
		bytes = null;
		System.gc();
		return rtrn;
	}
	
	/** @param file The file whose accessibly will be checked
	 * @return True if the file can be written to, false otherwise
	 * @throws InvalidPathException if a {@code Path} object cannot be
	 *             constructed from the abstract path (see
	 *             {@link java.nio.file.FileSystem#getPath
	 *             FileSystem.getPath(String first, String... more)}) */
	public static final boolean isFileAccessible(File file) throws InvalidPathException {
		/*if(file.exists()) {
			try {
				@SuppressWarnings("unused")
				FileInfo unused = new FileInfo(file, null); //(Moved from JavaWebServer FileUtil of same name and package as this)
				return true;
			} catch(IOException ignored) {
			}
		}
		return false;*/
		
		if(file != null) {
			if(file.getAbsolutePath().startsWith("." + File.separator)) {
				file = new File(File.separator + file.getAbsolutePath().substring(("." + File.separator).length()));//remove the freakin dot...
			}
			if(file.getAbsolutePath().contains("." + File.separator)) {
				file = new File(FilenameUtils.normalize(file.getAbsolutePath()));
			}
		}
		return file != null ? Files.isWritable(file.toPath()) : false;
	}
	
	/** @param file The file whose last modified time will be returned
	 * @return The file's last modified attribute
	 * @throws IOException Thrown if an I/O exception occurs */
	public static final long getLastModified(File file) throws IOException {
		return Files.getLastModifiedTime(file.toPath()).toMillis();
	}
	
	/** @param file The file or folder whose size(content length, number of
	 *            bytes, etc....) will be returned
	 * @return The file or folder's size(folders will usually just return
	 *         {@code 0}; to get a folder's size, see
	 *         {@link #getSizeDeep(Path)}.)
	 * @throws IOException if an I/O error occurs */
	public static final long getSize(File file) throws IOException {
		return Files.size(file.toPath());
	}
	
	/** @param startPath The file or folder whose size(content length, number of
	 *            bytes, etc....) will be returned
	 * @return The file or folder's size
	 * @throws IOException if an I/O error is thrown by a visitor method */
	public static final long getSizeDeep(Path startPath) throws IOException {
		final AtomicLong size = new AtomicLong(0);
		Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				size.addAndGet(attrs.size());
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				// Skip folders that can't be traversed
				//System.out.println("skipped: " + file + "e=" + exc);
				return FileVisitResult.CONTINUE;
			}
		});
		return size.get();
	}
	
	/** @param file The file to rename
	 * @param renameTo The new name for the file
	 * @return Whether or not the renaming was successful
	 * @throws IOException Thrown if an I/O exception occurs */
	public static final boolean renameFile(File file, String renameTo) throws IOException {
		Path source = Paths.get(file.toURI());
		boolean success = true;
		if(file.isDirectory() && file.getName().equalsIgnoreCase(renameTo)) {
			File folder = new File(file.getParentFile(), renameTo);
			success = file.renameTo(folder);
		} else {
			Files.move(source, source.resolveSibling(renameTo), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
		return success;
	}
	
	private static final ConcurrentHashMap<String, File> logFiles = new ConcurrentHashMap<>();
	
	/** @param logName The base name of the log file. Example: if you want a log
	 *            file named &quot;Commands.log&quot;, then you would only
	 *            supply the string &quot;Commands&quot; here
	 * @param createIfNotExist Whether or not the log file should be created
	 * @return The log file if it already existed or the createIfNotExist
	 *         boolean argument was set to {@code true}. If the file did not
	 *         exist and the argument was {@code false}, then {@code null} is
	 *         returned.
	 * @throws IOException Thrown if an I/O exception occurs */
	private static final File getLogFile(String logName, boolean createIfNotExist) throws IOException {
		if(logName == null) {
			return null;
		}
		File file = logFiles.get(logName);
		boolean fileDidExist = false;
		if(file != null) {
			fileDidExist = file.isFile();//true;
			//If the file's name doesn't match it's original key(file was renamed into an archive?), or the file's parental directory
			//structure has changed(either the root folder was changed, or the file was moved into a different folder somehow), then:
			if(!file.getName().equals(logName) || !FilenameUtils.normalize(file.getParentFile().getAbsolutePath()).equals(FilenameUtils.normalize(getRootLogFolder().getAbsolutePath()))) {
				logFiles.remove(logName);//unregister the now invalid file object
				file = null;//set the variable to null so that it is re-created below if either createIfNotExist or fileDidExist is true
			}
		}
		if(createIfNotExist || fileDidExist) {
			if(file == null) {
				file = new File(getRootLogFolder(), logName + ".log");
			}
			if(!file.exists()) {
				file.createNewFile();
			}
			logFiles.put(logName, file);
			return file;
		}
		return null;
	}
	
	/** @return The parent directory for any log files created */
	public static final File getRootLogFolder() {
		return rootLogDir;
	}
	
	/** @param folder The new parent directory that will contain all future log
	 *            files */
	public static final void setRootLogFolder(File folder) {
		if(folder != null) {
			if(!folder.exists()) {
				folder.mkdirs();
			}
			rootLogDir = folder;
		}
	}
	
	private static final File getArchiveFolder() {
		File logs = new File(getRootLogFolder(), "Logs");
		if(!logs.exists()) {
			logs.mkdirs();
		}
		return logs;
	}
	
	/** @param logName The base name of the log file. Example: if you want a log
	 *            file<br>
	 *            named &quot;Commands.log&quot;, then you would only supply<br>
	 *            the string &quot;Commands&quot; here
	 * @param str The line or lines of text to append to the end of the log
	 *            file */
	public static final void logStr(String logName, String str) {
		try {
			File file = getLogFile(logName, true);
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			final long lastModifiedTime = attr.lastModifiedTime().toMillis();
			final long fileSize = attr.size();
			boolean fileSizeLimitReached = fileSize >= 8192;
			if(fileSizeLimitReached) {//8kb
				/*if(renameFile(file, "Logs".concat(File.separator).concat(fileName))) {
					file = getLogFile(fileName, true);
				} else {
					System.err.println("File not renamed!");
				}*/
				String baseName = FilenameUtils.getBaseName(file.getName()).concat("_").concat(StringUtil.getTime(lastModifiedTime, false, true));//.concat(StringUtil.getTime(lastModifiedTime, false, true, true));
				String ext = ".log.gz";
				String fileName = baseName.concat(ext);
				File archived = new File(getArchiveFolder(), fileName);
				int duplicates = 0;//juuust in case
				while(archived.exists()) {
					archived = new File(getArchiveFolder(), baseName.concat("_").concat(Integer.toString(duplicates++)).concat(ext));
				}
				byte[] r = FileUtil.readFile(file);
				try(GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(archived, false))) {
					out.write(r);
					out.flush();
				}
				FileDeleteStrategy.FORCE.deleteQuietly(file);
				file = getLogFile(logName, true);
			}
			try(PrintWriter pr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, !fileSizeLimitReached), StandardCharsets.ISO_8859_1), true)) {
				pr.println(str);
				pr.flush();
			}
		} catch(Error e) {
		} catch(RuntimeException e) {
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static final String makeStringFilesystemSafe(String s) {
		char escape = '%'; // ... or some other legal char.
		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for(int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			if(ch < ' ' || ch >= 0x7F || ch == '/' || ch == '\\' || ch == '?' || ch == ':' || ch == '"' || ch == '*' || ch == '|' || ch == '<' || ch == '>' || (ch == '.' && i == 0) || ch == escape) {
				sb.append(escape);
				if(ch < 0x10) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(ch));
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	/** @return The platform-specific executable extension, if there is one */
	public static final String getExecutableExtension() {
		return getExecutableExtensionFor(CodeUtil.getOSType());
	}
	
	/** @param os The operating system
	 * @return The executable extension for the given operating system, if it
	 *         has one */
	public static final String getExecutableExtensionFor(EnumOS os) {
		switch(os) {
		case ANDROID:
			return ".apk";
		case LINUX:
			return "";//"run";//or "out";
		case OSX:
			return ".ipa";
		case SOLARIS:
			return "";//"run";//or "out";
		case UNIX:
			return "";//"???";
		case UNKNOWN:
		default:
			return ".unknown";
		case WINDOWS:
			return ".exe";
		}
	}
	
}
