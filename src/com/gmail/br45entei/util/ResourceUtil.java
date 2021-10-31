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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** @since 1.0
 * @author Brian_Entei &lt;br45entei&#064;gmail.com&gt; */
public class ResourceUtil {
	
	// Apparently using the concurrent linked deque instead of the hashmap for caching is waaaayy faster
	private static final ConcurrentLinkedDeque<String> existingResources = new ConcurrentLinkedDeque<>();
	private static final ConcurrentLinkedDeque<String> nonExistingResources = new ConcurrentLinkedDeque<>();
	
	/** @param path The path to the internal resource to load
	 * @return The resulting InputStream if the resource exists,
	 *         <b><code>null</code></b> otherwise */
	public static final InputStream loadResource(String path) {
		if(!path.startsWith("/")) {
			path = "/" + path;
			if(!path.startsWith("/assets")) {
				path = "/assets" + path;
			}
		}
		try {
			System.out.println("Loading asset: " + path);
			return getResourceAsStream(path);
		} catch(Throwable e) {
			System.err.print("Failed to load asset: " + path);
			System.err.println(e);
			return null;
		}
	}
	
	public static final String loadResourceAsString(String path, Charset charset, String lineSeparator) {
		InputStream _in = loadResource(path);
		if(_in != null) {
			try(InputStream in = _in) {
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = FileUtil.readLine(in, lineSeparator != null, charset)) != null) {
					sb.append(line).append(lineSeparator == null ? "" : lineSeparator);
				}
				return sb.toString();
			} catch(Throwable ex) {
				System.err.print("Failed to load asset: " + path);
				System.err.println(ex);
			}
		}
		return null;
	}
	
	public static final String loadResourceAsString(String path, Charset charset) {
		return loadResourceAsString(path, charset, "\r\n");
	}
	
	public static final String loadResourceAsString(String path) {
		return loadResourceAsString(path, StandardCharsets.UTF_8);
	}
	
	/** @param path The path to the internal resource to load
	 * @param fileName The name of the file to load
	 * @return The loaded file, or <b><code>null</code></b> if an I/O error
	 *         occurred */
	public static final File loadResourceAsFile(String path, String fileName) {
		return loadResourceAsFile(path, new File(System.getProperty("user.dir")), fileName);
	}
	
	/** @param path The path to the internal resource to load
	 * @param file The file to write to
	 * @return The loaded file, or <b><code>null</code></b> if an I/O error
	 *         occurred */
	public static final File loadResourceAsFile(String path, File file) {
		return loadResourceAsFile(path, file.getParentFile(), file.getName());
	}
	
	/** @param path The path to the internal resource to load
	 * @param folder The parent folder of the file to be loaded
	 * @param fileName The name of the file to load
	 * @return The loaded file, or <b><code>null</code></b> if an I/O error
	 *         occurred */
	public static final File loadResourceAsFile(String path, File folder, String fileName) {
		File file = new File(folder, fileName);
		folder.mkdirs();
		try(InputStream in = loadResource(path); FileOutputStream out = new FileOutputStream(file)) {
			byte[] buf = new byte[4096];
			int read;
			while((read = in.read(buf)) != -1) {
				out.write(buf, 0, read);
			}
			out.flush();
		} catch(NullPointerException | IOException e) {
			System.out.print("Failed to load asset \"" + path + "\" as file: " + folder.getAbsolutePath() + File.separator + fileName);
			e.printStackTrace();
			return null;
		}
		return file;
	}
	
	/** @param file The file to read
	 * @return The file's contents as a string using the file's detected
	 *         charset, or <b><code>null</code></b> if an I/O error occurred */
	public static final String readFileToString(File file) {
		return readFileToString(file, StandardCharsets.ISO_8859_1);
	}
	
	/** @param file The file to read
	 * @param charset The charset to use when converting the file's contents to
	 *            string
	 * @return The file's contents as a string, or <b><code>null</code></b> if
	 *         an I/O error occurred */
	public static final String readFileToString(File file, Charset charset) {
		if(file.isFile()) {
			StringBuilder sb = new StringBuilder();
			try(FileInputStream in = new FileInputStream(file)) {
				byte[] buf = new byte[4096];
				int read;
				while((read = in.read(buf)) != -1) {
					sb.append(new String(buf, 0, read, charset));
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}
		return null;
	}
	
	/** @param file The file to write to
	 * @param string The string to write
	 * @return Whether or not writing the data was successful */
	public static final boolean writeStringToFile(File file, String string) {
		return writeStringToFile(file, string, StandardCharsets.UTF_8);
	}
	
	/** @param file The file to write to
	 * @param string The string to write
	 * @param charset The charset to use when writing string data to file
	 * @return Whether or not writing the data was successful */
	public static final boolean writeStringToFile(File file, String string, Charset charset) {
		try(FileOutputStream out = new FileOutputStream(file)) {
			out.write(string.getBytes(charset));
			out.flush();
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/** @param path The package path
	 * @return A list of all resources in the given path */
	public static final List<String> getResourceFiles(String path) {
		path = path.startsWith("/") ? path : (path.startsWith("assets") ? path : "/assets/" + path);
		path = path.startsWith("/") ? path : "/" + path;
		List<String> filenames = new ArrayList<>();
		try(InputStream in = getResourceAsStream(path);//
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String resource;
			while((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
		} catch(IOException | NullPointerException e) {
			System.err.print("Failed to access package \"" + path + "\": ");
			e.printStackTrace();
		}
		return filenames;
	}
	
	/** @return The root folder or jar file that the class loader loaded from */
	public static final File getClasspathFile() {
		String path = ResourceUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		path = URLDecoder.decode(path, StandardCharsets.UTF_8);
		return new File(path);//new File(getProperty("user.dir") + File.separator + getJarFileName());
	}
	
	/** @param clazz The class whose class loader will attempt to load the
	 *            specified resource (may be <tt><b>null</b></tt>
	 * @param resource The path to the resource
	 * @return An InputStream containing the resource's contents, or
	 *         <b><code>null</code></b> if the resource does not exist */
	public static final InputStream getResourceAsStream(Class<?> clazz, String resource) {
		resource = resource.startsWith("/") ? resource : (resource.startsWith("assets") ? "/" + resource : "/assets" + resource);
		if(clazz != null || getClasspathFile().isDirectory()) {//Development environment.(Much simpler... >_>)
			return (clazz == null ? ResourceUtil.class : clazz).getResourceAsStream(resource);
		}
		final String res = resource;
		return AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
			@SuppressWarnings("resource")
			@Override
			public InputStream run() {
				try {
					final JarFile jar = new JarFile(getClasspathFile());
					String resource = res.startsWith("/") ? res.substring(1) : res;
					if(resource.endsWith("/")) {//Directory; list direct contents:(Mimics normal getResourceAsStream("someFolder/") behaviour)
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						Enumeration<JarEntry> entries = jar.entries();
						while(entries.hasMoreElements()) {
							JarEntry entry = entries.nextElement();
							if(entry.getName().startsWith(resource) && entry.getName().length() > resource.length()) {
								String name = entry.getName().substring(resource.length());
								if(name.contains("/") ? (name.endsWith("/") && (name.indexOf("/") == name.lastIndexOf("/"))) : true) {//If it's a folder, we don't want the children's folders, only the parent folder's children!
									name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
									baos.write(name.getBytes(StandardCharsets.UTF_8));
									baos.write('\r');
									baos.write('\n');
								}
							}
						}
						jar.close();
						return new ByteArrayInputStream(baos.toByteArray());
					}
					JarEntry entry = jar.getJarEntry(resource);
					InputStream in = entry != null ? jar.getInputStream(entry) : null;
					if(in == null) {
						jar.close();
						return in;
					}
					final InputStream stream = in;//Don't manage 'jar' with try-with-resources or close jar until the
					return new InputStream() {//returned stream is closed(closing jar closes all associated InputStreams):
						@Override
						public int read() throws IOException {
							return stream.read();
						}
						
						@Override
						public int read(byte b[]) throws IOException {
							return stream.read(b);
						}
						
						@Override
						public int read(byte b[], int off, int len) throws IOException {
							return stream.read(b, off, len);
						}
						
						@Override
						public long skip(long n) throws IOException {
							return stream.skip(n);
						}
						
						@Override
						public int available() throws IOException {
							return stream.available();
						}
						
						@Override
						public void close() throws IOException {
							try {
								stream.close();
							} catch(IOException ignored) {
							}
							jar.close();
						}
						
						@Override
						public synchronized void mark(int readlimit) {
							stream.mark(readlimit);
						}
						
						@Override
						public synchronized void reset() throws IOException {
							stream.reset();
						}
						
						@Override
						public boolean markSupported() {
							return stream.markSupported();
						}
					};
				} catch(Throwable e) {
					e.printStackTrace();
					return null;
				}
			}
		});
	}
	
	/** @param resource The path to the resource
	 * @return An InputStream containing the resource's contents, or
	 *         <b><code>null</code></b> if the resource does not exist */
	public static final InputStream getResourceAsStream(String resource) {
		return getResourceAsStream(null, resource);
	}
	
	/** @param path The path
	 * @return The full path */
	public static final String getResourcePathFromShorthand(String path) {
		return path == null ? null : (path.startsWith("/") ? path : (path.toLowerCase().startsWith("assets/") ? "/" + path : "/assets/" + path));//(path.startsWith("/") ? path : "/assets/" + path);
	}
	
	/** @param path The resource path to check
	 * @param useCache Whether or not the existence (or lack thereof) the
	 *            resource path should be cached, and the cached result returned
	 *            in future calls to this method.
	 * @return Whether or not the resource exists(true if an input stream was
	 *         successfully opened from the resource, false otherwise) */
	public static final boolean doesResourceExist(String path, boolean useCache) {
		path = getResourcePathFromShorthand(path);
		if(useCache) {
			if(existingResources.contains(path)) {
				return true;
			}
			if(nonExistingResources.contains(path)) {
				return false;
			}
		} else {
			while(existingResources.remove(path)) {
			}
			while(nonExistingResources.remove(path)) {
			}
		}
		
		try(InputStream closeMe = ResourceUtil.getResourceAsStream(null, path)) {
			if(closeMe != null) {
				if(useCache) {
					existingResources.add(path);
				}
				return true;
			}
		} catch(IOException ignored) {
		}
		if(useCache) {
			nonExistingResources.add(path);
		}
		return false;
	}
	
	/** @param path The resource path to check
	 * @return Whether or not the resource exists(true if an input stream was
	 *         successfully opened from the resource, false otherwise) */
	public static final boolean doesResourceExist(String path) {
		return doesResourceExist(path, true);
	}
	
	/** @return The path to the current Java Code source(may be null if
	 *         unavailable) */
	public static final String getPathToCodeSource() {
		String path = ResourceUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
			if(decodedPath.startsWith("/")) {
				decodedPath = decodedPath.substring(1);
			}
			//if(Main.isDebugModeOn()) {
			//	System.out.println("Path to currently running jar file: \"" + decodedPath + "\"");
			//}
		} catch(UnsupportedEncodingException e) {
		}
		return decodedPath;
	}
	
	/** @return The Java code source in file form. */
	public static final File getCodeSource() {
		String pathToCodeSource = ResourceUtil.getPathToCodeSource();
		return pathToCodeSource != null ? new File(pathToCodeSource) : null;
	}
	
	/** @return {@link Boolean#TRUE} if the Java code source is a file,
	 *         {@link Boolean#FALSE} if it is a directory, or null if it could
	 *         not be determined. */
	public static final Boolean isCodeSourceAFile() {
		File codeSource = ResourceUtil.getCodeSource();
		if(codeSource == null || !codeSource.exists()) {
			return null;
		}
		if(codeSource.isDirectory()) {
			return Boolean.FALSE;
		} else if(codeSource.isFile()) {
			return Boolean.TRUE;
		}
		return null;
	}
	
	/** @param list The String list of file paths
	 * @param directory The Folder to scan
	 * @return The given list with any files contained within the given folder,
	 *         if any */
	public static final ArrayList<String> scanForAndAddFilePathsFromDirectories(ArrayList<String> list, File directory) {
		return scanForAndAddFilePathsFromDirectories(list, directory, directory);
	}
	
	private static final ArrayList<String> scanForAndAddFilePathsFromDirectories(ArrayList<String> list, File directory, final File rootDir) {
		for(String filePath : directory.list()) {
			File file = new File(directory, filePath);
			if(file.exists()) {
				if(file.isFile()) {
					String pathToAdd = filePath;
					if(!directory.equals(rootDir)) {
						pathToAdd = file.getAbsolutePath().substring(rootDir.getAbsolutePath().length()).replace('\\', '/');
						pathToAdd = pathToAdd.startsWith("/") ? pathToAdd.substring(1) : pathToAdd;
					}
					if(!list.contains(pathToAdd)) {
						list.add(pathToAdd);
					}
				}
				if(file.isDirectory()) {
					scanForAndAddFilePathsFromDirectories(list, file, rootDir);
				}
			}
			//if(Main.isDebugModeOn()) {
			//	System.out.println("Current file path: \"" + file.getAbsolutePath() + "\";");
			//}
		}
		return list;
	}
	
	/** @param list The String list of file paths
	 * @param directory The Folder to scan
	 * @return The given list with any files contained within the given folder,
	 *         if any */
	public static final ArrayList<String> scanForAndAddFolderPathsFromDirectories(ArrayList<String> list, File directory) {
		return scanForAndAddFolderPathsFromDirectories(list, directory, directory);
	}
	
	private static final ArrayList<String> scanForAndAddFolderPathsFromDirectories(ArrayList<String> list, File directory, final File rootDir) {
		for(String filePath : directory.list()) {
			File file = new File(directory, filePath);
			if(file.exists()) {
				if(file.isDirectory()) {
					String pathToAdd = filePath;
					if(!directory.equals(rootDir)) {
						pathToAdd = file.getAbsolutePath().substring(rootDir.getAbsolutePath().length()).replace('\\', '/');
						pathToAdd = pathToAdd.startsWith("/") ? pathToAdd.substring(1) : pathToAdd;
					}
					if(!list.contains(pathToAdd)) {
						list.add(pathToAdd);
					}
					scanForAndAddFolderPathsFromDirectories(list, file, rootDir);
				}
			}
			//if(Main.isDebugModeOn()) {
			//	System.out.println("Current file path: \"" + file.getAbsolutePath() + "\";");
			//}
		}
		return list;
	}
	
	/** @param pkg The package to browse
	 * @return An ArrayList containing the names of any files/folders in the
	 *         given package. */
	public static final ArrayList<String> getAllFilesInPackage(Package pkg) {
		return ResourceUtil.getAllFilesInPackage(pkg.getName());
	}
	
	/** @param pkgName The package to use
	 * @return All packages in the given package
	 * @throws NullPointerException Thrown if the pkgName is not valid */
	public static final ArrayList<String> getAllPackagesInPackage(String pkgName) throws NullPointerException {
		final ArrayList<String> list = new ArrayList<>();
		File directory = null;
		String fullPath;
		String relPath = pkgName;//.replace('.', '/');
		URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
		if(resource == null) {
			throw new NullPointerException("No resource for " + relPath);
		}
		fullPath = resource.getFile();
		try {
			directory = new File(resource.toURI());
		} catch(URISyntaxException e) {
			throw new RuntimeException(pkgName + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
		} catch(IllegalArgumentException e) {
			directory = null;
		}
		if((directory != null) && directory.exists()) {
			scanForAndAddFolderPathsFromDirectories(list, directory);
			//String[] files = directory.list();
			//for(String file : files) {
			//	list.add(file);
			
			//}
		} else {
			//String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
			URL url = ResourceUtil.class.getProtectionDomain().getCodeSource().getLocation();
			String jarPath;
			try {
				jarPath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());
			} catch(UnsupportedEncodingException e1) {
				throw new RuntimeException("\"" + StandardCharsets.UTF_8.name() + "\" is an invalid charset?!", e1);
			}
			try(JarFile jarFile = new JarFile(jarPath)) {
				Enumeration<JarEntry> entries = jarFile.entries();
				while(entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String entryName = entry.getName();
					if(entryName.startsWith(relPath) && (entryName.length() > (relPath + "/").length())) {
						if(entry.isDirectory()) {
							//if(Main.isDebugModeOn()) {
							//	System.out.println("ClassDiscovery: JarEntry: " + entryName);
							//}
							list.add(entryName.replace(relPath, ""));
						}
					}
				}
			} catch(IOException e) {
				System.err.println("Unable to browse over package \"" + jarPath + "\"(fullPath: \"" + fullPath + "\"): " + StringUtil.throwableToStr(e));
			}
		}
		ArrayList<String> rtrn = new ArrayList<>();
		for(String fileName : list) {
			rtrn.add(fileName.startsWith("/") ? fileName.substring(1) : fileName);
		}
		return rtrn;
	}
	
	/** @param pkgName The name of the package to browse
	 * @return An ArrayList containing the names of any files/folders in the
	 *         given package.
	 * @throws NullPointerException Thrown if the given package does not exist
	 *             or could not be loaded */
	public static final ArrayList<String> getAllFilesInPackage(String pkgName) throws NullPointerException {
		final ArrayList<String> list = new ArrayList<>();
		File directory = null;
		String fullPath;
		String relPath = pkgName;//.replace('.', '/');
		URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
		if(resource == null) {
			throw new NullPointerException("No resource for " + relPath);
		}
		fullPath = resource.getFile();
		try {
			directory = new File(resource.toURI());
		} catch(URISyntaxException e) {
			throw new RuntimeException(pkgName + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
		} catch(IllegalArgumentException e) {
			directory = null;
		}
		if((directory != null) && directory.exists()) {
			scanForAndAddFilePathsFromDirectories(list, directory);
			//String[] files = directory.list();
			//for(String file : files) {
			//	list.add(file);
			
			//}
		} else {
			//String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
			URL url = ResourceUtil.class.getProtectionDomain().getCodeSource().getLocation();
			String jarPath;
			try {
				jarPath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());
			} catch(UnsupportedEncodingException e1) {
				throw new RuntimeException("\"" + StandardCharsets.UTF_8.name() + "\" is an invalid charset?!", e1);
			}
			try(JarFile jarFile = new JarFile(jarPath)) {
				Enumeration<JarEntry> entries = jarFile.entries();
				while(entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String entryName = entry.getName();
					if(entryName.startsWith(relPath) && (entryName.length() > (relPath + "/").length())) {
						//if(Main.isDebugModeOn()) {
						//	System.out.println("ClassDiscovery: JarEntry: " + entryName);
						//}
						list.add(entryName.replace(relPath, ""));
					}
				}
			} catch(IOException e) {
				System.err.println("Unable to browse over package \"" + jarPath + "\"(fullPath: \"" + fullPath + "\"): " + StringUtil.throwableToStr(e));
			}
		}
		ArrayList<String> rtrn = new ArrayList<>();
		for(String fileName : list) {
			rtrn.add(fileName.startsWith("/") ? fileName.substring(1) : fileName);
		}
		return rtrn;
	}
	
}
