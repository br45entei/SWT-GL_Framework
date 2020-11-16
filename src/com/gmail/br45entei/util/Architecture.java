package com.gmail.br45entei.util;

/** The architectures supported by LWJGL.<br>
 * <br>
 * <b>Note:</b>&nbsp;This class contains modified code (as of 06/06/2020 05:47
 * PM CST) copied from {@link org.lwjgl.system.Platform.Architecture} because
 * using any class from LWJGL before the native libraries are loaded in causes
 * an {@link UnsatisfiedLinkError}, resulting in the libraries not being loaded
 * properly.
 *
 * @since 1.0
 * @author LWJGL3-3.2.3 (Unascribed; Ripped from Platform.java) */
public enum Architecture {
	/** Represents 32-bit architectures. */
	X86,
	/** Represents 64-bit architectures. */
	X64,
	/** Represents 32-bit ARM architectures. */
	ARM32,
	/** Represents 64-bit ARM architectures. */
	ARM64;
	
	private static final Architecture current;
	
	static {
		String osArch = System.getProperty("os.arch");
		boolean is64Bit = osArch.contains("64") || osArch.startsWith("armv8");
		
		current = osArch.startsWith("arm") || osArch.startsWith("aarch64")// || osArch.startsWith("armv8")// Shouldn't this be here as well? idk ...
				? (is64Bit ? Architecture.ARM64 : Architecture.ARM32)//
				: (is64Bit ? Architecture.X64 : Architecture.X86);
	}
	
	/** @return The platform's {@link Architecture} */
	public static final Architecture get() {
		return current;
	}
	
}
