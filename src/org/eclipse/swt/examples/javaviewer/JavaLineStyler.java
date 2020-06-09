/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.examples.javaviewer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public final class JavaLineStyler implements LineStyleListener {
	
	/** A simple fuzzy scanner for Java */
	public class JavaScanner {
		
		private String[]							fgKeywords	= { "abstract", "boolean", "break", "byte", "case", "catch", "char",
																	"class", "continue", "default", "do", "double", "else", "extends",
																	"false", "final", "finally", "float", "for", "goto", "if",
																	"implements", "import", "instanceof", "int", "interface", "long",
																	"native", "new", "null", "package", "private", "protected",
																	"public", "return", "short", "static", "super", "strictfp",
																	"switch", "synchronized", "this", "throw", "throws", "transient",
																	"true", "try", "void", "volatile", "while",
																	
																	"#include", "#define", "#version", "#extension", "#line", "#ifdef",
																	"#ifndef", "#endif", "const", "typedef",
																	"uniform", "vec2", "vec3", "vec4", "mat2", "mat3", "mat4", "in",
																	"out", "inout", //"gl_PrimitiveID", "gl_InstanceID", "gl_Position", //https://www.khronos.org/opengl/wiki/Built-in_Variable_(GLSL)
																	"uint", "uvec2", "uvec3", "uvec4", "dvec2", "dvec3", "dvec4",
																	"struct", "discard"
													};
		protected Hashtable<String, Integer>		fgKeywordKeys		= null;
		
		private String[]							fgClasswords	= { "AbstractMethodError", "AbstractStringBuilder", "Appendable", "ApplicationShutdownHooks",
																		"ArithmeticException", "ArrayIndexOutOfBoundsException", "ArrayStoreException", "AssertionError",
																		"AssertionStatusDirectives", "AutoCloseable", "Boolean", "BootstrapMethodError", "Byte", "Character",
																		"CharacterData", "CharacterData00", "CharacterData01", "CharacterData02", "CharacterData0E",
																		"CharacterDataLatin1", "CharacterDataPrivateUse", "CharacterDataUndefined", "CharacterName",
																		"CharSequence", "Class", "ClassCastException", "ClassCircularityError", "ClassFormatError",
																		"ClassLoader", "ClassLoaderHelper", "ClassNotFoundException", "ClassValue", "Cloneable",
																		"CloneNotSupportedException", "Comparable", "Compiler", "ConditionalSpecialCasing", "Double", "Enum", 
																		"EnumConstantNotPresentException", "Error", "Exception", "ExceptionInInitializerError", "Float",
																		"IllegalAccessError", "IllegalAccessException", "IllegalArgumentException", "IllegalMonitorStateException",
																		"IllegalStateException", "IllegalThreadStateException", "IncompatibleClassChangeError", "IndexOutOfBoundsException",
																		"InheritableThreadLocal", "InstantiationError", "InstantiationException", "Integer", "InternalError",
																		"InterruptedException", "Iterable", "LinkageError", "Long", "Math", "NegativeArraySizeException",
																		"NoClassDefFoundError", "NoSuchFieldError", "NoSuchFieldException", "NoSuchMethodError", "NoSuchMethodException",
																		"NullPointerException", "Number", "NumberFormatException", "Object", "OutOfMemoryError", "Package", "Process",
																		"ProcessBuilder", "ProcessEnvironment", "ProcessImpl", "Readable", "ReflectiveOperationException", "Runnable",
																		"Runtime", "RuntimeException", "RuntimePermission", "SecurityException", "SecurityManager", "Short",
																		"Shutdown", "StackOverflowError", "StackTraceElement", "StrictMath", "String", "StringBuffer", "StringBuilder",
																		"StringCoding", "StringIndexOutOfBoundsException", "System", "SystemClassLoaderAction", "Terminator", "Thread",
																		"ThreadDeath", "ThreadGroup", "ThreadLocal", "Throwable", "TypeNotPresentException", "UnknownError",
																		"UnsatisfiedLinkError", "UnsupportedClassVersionError", "UnsupportedOperationException", "VerifyError",
																		"VirtualMachineError", "Void",
																		
																		"Bits", "BufferedInputStream", "BufferedOutputStream", "BufferedReader", "BufferedWriter", "ByteArrayInputStream",
																		"ByteArrayOutputStream", "CharArrayReader", "CharArrayWriter", "CharConversionException", "Closeable", "Console",
																		"DataInput", "DataInputStream", "DataOutput", "DataOutputStream", "DefaultFileSystem", "DeleteOnExitHook",
																		"EOFException", "ExpiringCache", "Externalizable", "File", "FileDescriptor", "FileFilter", "FileInputStream",
																		"FilenameFilter", "FileNotFoundException", "FileOutputStream", "FilePermission", "FilePermissionCollection",
																		"FileReader", "FileSystem", "FileWriter", "FilterInputStream", "FilterOutputStream", "FilterReader", "FilterWriter",
																		"Flushable", "InputStream", "InputStreamReader", "InterruptedIOException", "InvalidClassException",
																		"InvalidObjectException", "IOError", "IOException", "LineNumberInputStream", "LineNumberReader", "NotActiveException",
																		"NotSerializableException", "ObjectInput", "ObjectInputStream", "ObjectInputValidation", "ObjectOutput",
																		"ObjectOutputStream", "ObjectStreamClass", "ObjectStreamConstants", "ObjectStreamException", "ObjectStreamField",
																		"OptionalDataException", "OutputStream", "OutputStreamWriter", "PipedInputStream", "PipedOutputStream", "PipedReader",
																		"PipedWriter", "PrintStream", "PrintWriter", "PushbackInputStream", "PushbackReader", "RandomAccessFile", "Reader",
																		"SequenceInputStream", "SerialCallbackContext", "Serializable", "SerializablePermission", "StreamCorruptedException",
																		"StreamTokenizer", "StringBufferInputStream", "StringReader", "StringWriter", "SyncFailedException", "UncheckedIOException",
																		"UnsupportedEncodingException", "UTFDataFormatException", "WinNTFileSystem", "WriteAbortedException", "Writer",
																		"Annotation", "AnnotationFormatError", "AnnotationTypeMismatchException", "IncompleteAnnotationException",
																		"AbstractCollection", "AbstractList", "AbstractMap", "AbstractQueue", "AbstractSequentialList", "AbstractSet", "ArrayDeque",
																		"ArrayList", "ArrayPrefixHelpers", "Arrays", "ArraysParallelSortHelpers", "Base64", "BitSet", "Calendar", "Collection",
																		"Collections", "ComparableTimSort", "Comparator", "Comparators", "ConcurrentModificationException", "Currency", "Date",
																		"Deque", "Dictionary", "DoubleSummaryStatistics", "DualPivotQuicksort", "DuplicateFormatFlagsException",
																		"EmptyStackException", "Enumeration", "EnumMap", "EnumSet", "EventListener", "EventListenerProxy", "EventObject",
																		"FormatFlagsConversionMismatchException", "Formattable", "FormattableFlags", "Formatter", "FormatterClosedException",
																		"GregorianCalendar", "HashMap", "HashSet", "Hashtable", "IdentityHashMap", "IllegalFormatCodePointException",
																		"IllegalFormatConversionException", "IllegalFormatException", "IllegalFormatFlagsException",
																		"IllegalFormatPrecisionException", "IllegalFormatWidthException", "IllformedLocaleException", "InputMismatchException",
																		"IntSummaryStatistics", "InvalidPropertiesFormatException", "Iterator", "JapaneseImperialCalendar", "JumboEnumSet",
																		"LinkedHashMap", "LinkedHashSet", "LinkedList", "List", "ListIterator", "ListResourceBundle", "Locale", "LocaleISOData",
																		"LongSummaryStatistics", "Map", "MissingFormatArgumentException", "MissingFormatWidthException", "MissingResourceException",
																		"NavigableMap", "NavigableSet", "NoSuchElementException", "Objects", "Observable", "Observer", "Optional", "OptionalDouble",
																		"OptionalInt", "OptionalLong", "PrimitiveIterator", "PriorityQueue", "Properties", "PropertyPermission",
																		"PropertyPermissionCollection", "PropertyResourceBundle", "Queue", "Random", "RandomAccess", "RandomAccessSubList",
																		"RegularEnumSet", "ResourceBundle", "Scanner", "ServiceConfigurationError", "ServiceLoader", "Set", "SimpleTimeZone",
																		"SortedMap", "SortedSet", "Spliterator", "Spliterators", "SplittableRandom", "Stack", "StringJoiner", "StringTokenizer",
																		"SubList", "TaskQueue", "Timer", "TimerTask", "TimerThread", "TimeZone", "TimSort", "TooManyListenersException", "TreeMap",
																		"TreeSet", "Tripwire", "UnknownFormatConversionException", "UnknownFormatFlagsException", "UUID", "Vector", "WeakHashMap",
		};
		protected Hashtable<String, Integer>		fgClasswordKeys		= null;
		
		private String[]							fgAnnotationWords	= { "Deprecated", "@Deprecated", "FunctionalInterface",
																			"@FunctionalInterface", "Override", "@Override",
																			"SafeVarargs", "@SafeVarargs", "SuppressWarnings",
																			"@SuppressWarnings",
		};
		protected Hashtable<String, Integer>		fgAnnotationKeys	= null;
		
		protected StringBuffer						fBuffer		= new StringBuffer();
		protected String							fDoc;
		protected int								fEnd;
		protected boolean							fEofSeen	= false;
		protected int								fPos;
		
		protected int								fStartToken;
		
		public JavaScanner() {
			this.initialize();
		}
		
		/**
		 * Returns the ending location of the current token in the document.
		 */
		public final int getLength() {
			return this.fPos - this.fStartToken;
		}
		
		/**
		 * Returns the starting location of the current token in the document.
		 */
		public final int getStartOffset() {
			return this.fStartToken;
		}
		
		/**
		 * Returns the next lexical token in the document.
		 */
		public int nextToken() {
			int c;
			this.fStartToken = this.fPos;
			while (true) {
				switch (c = read()) {
				case EOF:
					return JavaLineStyler.EOF;
				case '/': // comment
					c = read();
					if (c == '/')
						while (true) {
							c = read();
							if (c == JavaLineStyler.EOF || c == JavaLineStyler.EOL) {
								unread(c);
								return JavaLineStyler.COMMENT;
							}
						}
					unread(c);
					return JavaLineStyler.OTHER;
				case '\'': // char const
					while (true) {
						c = read();
						switch (c) {
						case '\'':
							return JavaLineStyler.STRING;
						case EOF:
							unread(c);
							return JavaLineStyler.STRING;
						case '\\':
							c = read();
							break;
						}
					}
					
				case '"': // string
					while (true) {
						c = read();
						switch (c) {
						case '"':
							return JavaLineStyler.STRING;
						case EOF:
							unread(c);
							return JavaLineStyler.STRING;
						case '\\':
							c = read();
							break;
						}
					}
					
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					do
						c = read();
					while (Character.isDigit((char) c));
					unread(c);
					return JavaLineStyler.NUMBER;
				default:
					if (Character.isWhitespace((char) c)) {
						do {
							c = read();
						} while(Character.isWhitespace((char) c));
						unread(c);
						return JavaLineStyler.WHITE;
					}
					if(Character.isJavaIdentifierStart((char) c) || ((char) c) == '#') {
						this.fBuffer.setLength(0);
						do {
							this.fBuffer.append((char) c);
							c = read();
						} while(Character.isJavaIdentifierPart((char) c));
						unread(c);
						Integer i = this.fgKeywordKeys.get(this.fBuffer.toString());
						if (i != null)
							return i.intValue();
						i = this.fgClasswordKeys.get(this.fBuffer.toString());
						if(i != null) {
							return i.intValue();
						}
						i = this.fgAnnotationKeys.get(this.fBuffer.toString());
						if(i != null) {
							return i.intValue();
						}
						return JavaLineStyler.WORD;
					}
					if(((char) c) == '@') {
						this.fBuffer.setLength(0);
						do {
							this.fBuffer.append((char) c);
							c = read();
						} while(Character.isJavaIdentifierPart((char) c));
						unread(c);
						Integer i = this.fgAnnotationKeys.get(this.fBuffer.toString());
						if(i != null) {
							return i.intValue();
						}
						//return JavaLineStyler.WORD;
					}
					return JavaLineStyler.OTHER;
				}
			}
		}
		
		public void setRange(String text) {
			this.fDoc = text;
			this.fPos = 0;
			this.fEnd = this.fDoc.length() - 1;
		}
		
		/**
		 * Returns next character.
		 */
		protected int read() {
			if(this.fPos <= this.fEnd) {
				return this.fDoc.charAt(this.fPos++);
			}
			return JavaLineStyler.EOF;
		}
		
		protected void unread(int c) {
			if(c != JavaLineStyler.EOF){ 
				this.fPos--;
			}
		}
		
		/**
		 * Initialize the lookup table.
		 */
		void initialize() {
			this.fgKeywordKeys = new Hashtable<>();
			Integer k = Integer.valueOf(JavaLineStyler.KEY);
			for (int i = 0; i < this.fgKeywords.length; i++) {
				this.fgKeywordKeys.put(this.fgKeywords[i], k);
			}
			
			this.fgClasswordKeys = new Hashtable<>();
			k = Integer.valueOf(JavaLineStyler.CLASS);
			for(int i = 0; i < this.fgClasswords.length; i++) {
				this.fgClasswordKeys.put(this.fgClasswords[i], k);
			}
			
			this.fgAnnotationKeys = new Hashtable<>();
			k = Integer.valueOf(JavaLineStyler.ANNOTATION);
			for(int i = 0; i < this.fgAnnotationWords.length; i++) {
				this.fgAnnotationKeys.put(this.fgAnnotationWords[i], k);
			}
		}
		
	}
	
	public static final int	MAXIMUM_TOKEN	= 10;
	public static final int	ANNOTATION		= 9;
	public static final int	NUMBER			= 8;
	public static final int	OTHER			= 7;
	public static final int	STRING			= 6;
	public static final int	CLASS			= 5;
	public static final int	BLOCK_COMMENT	= 4;
	public static final int	COMMENT			= 3;
	public static final int	KEY				= 2;
	public static final int	WHITE			= 1;
	public static final int	WORD			= 0;
	public static final int	EOF				= -1;
	public static final int	EOL				= 10;
	
	Vector<int[]>					blockComments	= new Vector<>();
	Color[]					colors;
	
	JavaScanner				scanner			= new JavaScanner();
	
	int[]					tokenColors;
	
	public JavaLineStyler() {
		initializeColors();
		this.scanner = new JavaScanner();
	}
	
	/**
	 * Event.detail line start offset (input) Event.text line text (input) LineStyleEvent.styles Enumeration of
	 * StyleRanges, need to be in order. (output) LineStyleEvent.background line background color (output)
	 *///Test comment - this should be green
	@Override
	public void lineGetStyle(LineStyleEvent event) {
		Vector<StyleRange> styles = new Vector<>();
		int token;
		StyleRange lastStyle;
		// If the line is part of a block comment, create one style for the entire line.
		if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length())) {
			styles.addElement(new StyleRange(event.lineOffset, event.lineText.length(),
					getColor(JavaLineStyler.BLOCK_COMMENT), null));
			event.styles = new StyleRange[styles.size()];
			styles.copyInto(event.styles);
			return;
		}
		Color defaultFgColor = ((Control) event.widget).getForeground();
		this.scanner.setRange(event.lineText);
		token = this.scanner.nextToken();
		while (token != JavaLineStyler.EOF) {
			if (token == JavaLineStyler.OTHER) {
				// do nothing for non-colored tokens
			} else if (token != JavaLineStyler.WHITE) {
				Color color = getColor(token);
				// Only create a style if the token color is different than the
				// widget's default foreground color and the token's style is not
				// bold. Keywords are bolded.
				if (!color.equals(defaultFgColor) || token == JavaLineStyler.KEY) {
					StyleRange style = new StyleRange(this.scanner.getStartOffset() + event.lineOffset, this.scanner.getLength(),
							color, null);
					if (token == JavaLineStyler.KEY)
						style.fontStyle = SWT.BOLD;
					if (styles.isEmpty())
						styles.addElement(style);
					else {
						// Merge similar styles. Doing so will improve performance.
						lastStyle = styles.lastElement();
						if (lastStyle.similarTo(style) && lastStyle.start + lastStyle.length == style.start)
							lastStyle.length += style.length;
						else
							styles.addElement(style);
					}
				}
			} else if (!styles.isEmpty() && (lastStyle = styles.lastElement()).fontStyle == SWT.BOLD) {
				int start = this.scanner.getStartOffset() + event.lineOffset;
				lastStyle = styles.lastElement();
				// A font style of SWT.BOLD implies that the last style
				// represents a java keyword.
				if (lastStyle.start + lastStyle.length == start)
					// Have the white space take on the style before it to
					// minimize the number of style ranges created and the
					// number of font style changes during rendering.
					lastStyle.length += this.scanner.getLength();
			}
			token = this.scanner.nextToken();
		}
		event.styles = new StyleRange[styles.size()];
		styles.copyInto(event.styles);
	}
	
	public void parseBlockComments(String text) {
		this.blockComments = new Vector<>();
		StringReader buffer = new StringReader(text);
		int ch;
		boolean blkComment = false;
		int cnt = 0;
		int[] offsets = new int[2];
		boolean done = false;
		
		try {
			while (!done)
				switch (ch = buffer.read()) {
				case -1: {
					if (blkComment) {
						offsets[1] = cnt;
						this.blockComments.addElement(offsets);
					}
					done = true;
					break;
				}
				case '/': {
					ch = buffer.read();
					if (ch == '*' && !blkComment) {
						offsets = new int[2];
						offsets[0] = cnt;
						blkComment = true;
						cnt++;
					} else
						cnt++;
					cnt++;
					break;
				}
				case '*': {
					if (blkComment) {
						ch = buffer.read();
						cnt++;
						if (ch == '/') {
							blkComment = false;
							offsets[1] = cnt;
							this.blockComments.addElement(offsets);
						}
					}
					cnt++;
					break;
				}
				default: {
					cnt++;
					break;
				}
				}
		} catch (IOException e) {
			// ignore errors
		}
	}
	
	void disposeColors() {
		for (int i = 0; i < this.colors.length; i++)
			this.colors[i].dispose();
	}
	
	Color getColor(int type) {
		if (type < 0 || type >= this.tokenColors.length)
			return null;
		return this.colors[this.tokenColors[type]];
	}
	
	boolean inBlockComment(int start, int end) {
		for (int i = 0; i < this.blockComments.size(); i++) {
			int[] offsets = this.blockComments.elementAt(i);
			// start of comment in the line
			if (offsets[0] >= start && offsets[0] <= end)
				return true;
			// end of comment in the line
			if (offsets[1] >= start && offsets[1] <= end)
				return true;
			if (offsets[0] <= start && offsets[1] >= end)
				return true;
		}
		return false;
	}
	
	void initializeColors() {
		Display display = Display.getDefault();
		this.colors = new Color[] { 
				new Color(display, new RGB(255, 255, 255)), // white
				new Color(display, new RGB(255, 0, 0)),     // red
				new Color(display, new RGB(0, 255, 0)),     // green
				new Color(display, new RGB(0, 0, 255)),     // blue
				new Color(display, new RGB(255, 83, 0)),   // deep orange //204, 102, 0
				new Color(display, new RGB(0, 55, 183)),    // dark indigo(-ish)
				new Color(display, new RGB(223, 196, 0)),   // dandelion yellow //213, 165, 0
				new Color(display, new RGB(100, 100, 100)), // gray //127, 127, 127
				new Color(display, new RGB(0, 74, 102)),    // darker indigo
				new Color(display, new RGB(0, 234, 0)),     // class green
				new Color(display, new RGB(128, 0, 255)),   // purple
				new Color(display, new RGB(0, 0, 0))        // black
		};
		this.tokenColors = new int[JavaLineStyler.MAXIMUM_TOKEN];
		this.tokenColors[JavaLineStyler.WORD] = 6;// dandelion yellow
		this.tokenColors[JavaLineStyler.WHITE] = 0;// white
		this.tokenColors[JavaLineStyler.KEY] = 4;// deep orange
		this.tokenColors[JavaLineStyler.COMMENT] = 2;// green
		this.tokenColors[JavaLineStyler.BLOCK_COMMENT] = 10;// purple
		this.tokenColors[JavaLineStyler.CLASS] = 9;// class green
		this.tokenColors[JavaLineStyler.STRING] = 8;// darker indigo
		this.tokenColors[JavaLineStyler.OTHER] = 0;// white
		this.tokenColors[JavaLineStyler.NUMBER] = 8;// darker indigo
		this.tokenColors[JavaLineStyler.ANNOTATION] = 7;// gray
	}
	
}
