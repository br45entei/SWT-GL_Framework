package com.gmail.br45entei.util;

/** The platforms supported by LWJGL.<br>
 * <br>
 * <b>Note:</b>&nbsp;This class contains modified code (as of 06/06/2020 05:47
 * PM CST) copied from {@link org.lwjgl.system.Platform} because using any class
 * from LWJGL before the native libraries are loaded in causes an
 * {@link UnsatisfiedLinkError}, resulting in the libraries not being loaded
 * properly.
 *
 * @since 1.0
 * @author LWJGL3-3.2.3 (Unascribed; Ripped from Platform.java) */
public enum Platform {
	/** Represents Windows operating systems */
	WINDOWS,
	/** Represents Linux/Unix operating systems */
	LINUX,
	/** Represents MacOS operating systems */
	MACOSX,
	/** Represents an unknown operating system */
	UNKNOWN;
	
	private static final Platform current;
	
	static {
		String osName = System.getProperty("os.name");
		if(osName.startsWith("Windows")) {
			current = WINDOWS;
		} else if(osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
			current = LINUX;
		} else if(osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
			current = MACOSX;
		} else {
			current = UNKNOWN;
		}
	}
	
	/** @return The {@link Platform} (or <em>operating system</em>) that the
	 *         computer executing this code is running */
	public static final Platform get() {
		return current;
	}
	
}
