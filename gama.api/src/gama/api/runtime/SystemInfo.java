/*******************************************************************************************************
 *
 * SystemInfo.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.eclipse.core.runtime.Platform;

import gama.dev.DEBUG;
import gama.dev.STRINGS;

/**
 * Utility class for gathering and reporting system information about the runtime environment.
 * 
 * <p>
 * SystemInfo provides detailed information about the hardware and software environment in which GAMA is running. This
 * includes Java version, operating system details, memory configuration, processor count, graphics capabilities, and
 * display information.
 * </p>
 * 
 * <p>
 * Key capabilities:
 * </p>
 * <ul>
 * <li>Detect OS type (Windows, macOS, Linux) and architecture (x86, ARM)</li>
 * <li>Report Java VM details and version</li>
 * <li>Query physical and available memory</li>
 * <li>Enumerate displays and graphics cards</li>
 * <li>Format byte sizes using IEC binary prefixes (KiB, MiB, GiB, etc.)</li>
 * <li>Determine if running in development mode</li>
 * <li>Generate formatted system information reports</li>
 * </ul>
 * 
 * <p>
 * This information is used for:
 * </p>
 * <ul>
 * <li>Displaying system info in GAMA's "About" dialog and console</li>
 * <li>Bug reports and troubleshooting</li>
 * <li>Platform-specific behavior (e.g., different UI on macOS vs Windows)</li>
 * <li>Detecting development vs. production environments</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * // Get formatted system info
 * String info = SystemInfo.getSystemInfo();
 * System.out.println(info);
 * 
 * // Check platform
 * if (SystemInfo.isMac()) {
 * 	// macOS-specific code
 * } else if (SystemInfo.isWindows()) {
 * 	// Windows-specific code
 * }
 * 
 * // Format memory sizes
 * String memStr = SystemInfo.formatBytes(Runtime.getRuntime().maxMemory());
 * </pre>
 * 
 * @see Platform
 */
public class SystemInfo {

	/**
	 * The parsed Java version as an integer for easy comparison.
	 */
	public static final int JAVA_VERSION; // NO_UCD (unused code)

	/** The detected platform string from Eclipse Platform API. */
	private static String platformString = Platform.getOS();

	static {
		JAVA_VERSION = parseVersion(System.getProperty("java.version")); //$NON-NLS-1$
	}

	/** The GAMA version number (overridden at build time). */
	public static final String VERSION_NUMBER = "0.0.0-SNAPSHOT";

	/** The full GAMA version string. */
	public static final String VERSION = "GAMA " + VERSION_NUMBER;

	/** The Java Virtual Machine name. */
	public static final String JAVA_VM_NAME = System.getProperty("java.vm.name");

	/** The Java Virtual Machine vendor. */
	public static final String JAVA_VM_VENDOR = System.getProperty("java.vm.vendor");

	/** The Java Virtual Machine version. */
	public static final String JAVA_VM_VERSION = System.getProperty("java.vm.version");

	/** The operating system name. */
	public static final String OS_NAME = System.getProperty("os.name");

	/** The operating system version. */
	public static final String OS_VERSION = System.getProperty("os.version");

	/** The operating system architecture. */
	public static final String OS_ARCH = System.getProperty("os.arch");

	/**
	 * Binary prefixes, used in IEC Standard for naming bytes.
	 * (https://en.wikipedia.org/wiki/International_Electrotechnical_Commission)
	 *
	 * Should be used for most representations of bytes
	 */
	private static final long KIBI = 1L << 10; // 1024 bytes

	/** 1 Mebibyte = 1024 * 1024 bytes. */
	private static final long MEBI = 1L << 20;

	/** 1 Gibibyte = 1024^3 bytes. */
	private static final long GIBI = 1L << 30;

	/** 1 Tebibyte = 1024^4 bytes. */
	private static final long TEBI = 1L << 40;

	/** 1 Pebibyte = 1024^5 bytes. */
	private static final long PEBI = 1L << 50;

	/** 1 Exbibyte = 1024^6 bytes. */
	private static final long EXBI = 1L << 60;

	/** List of detected display configurations. */
	static List<String> displays;

	/** Graphics card description (set externally by platform-specific code). */
	public static String graphicsCard = "Not available";

	/** Total physical memory available to the system. */
	public static String physicalMemory;

	/**
	 * Formats bytes into a rounded string representation using IEC standard (matches Mac/Linux).
	 * 
	 * <p>
	 * This method converts raw byte counts into human-readable strings using binary prefixes (KiB, MiB, GiB, etc.) as
	 * defined by the IEC standard. This matches how macOS and Linux display file sizes.
	 * </p>
	 * 
	 * @param bytes
	 *            the number of bytes to format
	 * @return a formatted string like "1.5 GB" or "512 MB"
	 */
	public static String formatBytes(final long bytes) {
		if (bytes == 1L) return String.format(Locale.ROOT, "%d byte", bytes);
		if (bytes < KIBI) return String.format(Locale.ROOT, "%d bytes", bytes);
		if (bytes < MEBI) return formatUnits(bytes, KIBI, "KB");
		if (bytes < GIBI) return formatUnits(bytes, MEBI, "MB");
		if (bytes < TEBI) return formatUnits(bytes, GIBI, "GB");
		if (bytes < PEBI) return formatUnits(bytes, TEBI, "TB");
		if (bytes < EXBI) return formatUnits(bytes, PEBI, "PB");
		return formatUnits(bytes, EXBI, "EiB");
	}

	/**
	 * Formats units as exact integer or fractional decimal based on the prefix, appending the appropriate units.
	 * 
	 * @param value
	 *            the value to format
	 * @param prefix
	 *            the divisor of the unit multiplier
	 * @param unit
	 *            a string representing the units (KB, MB, GB, etc.)
	 * @return a formatted string with the value and unit
	 */
	private static String formatUnits(final long value, final long prefix, final String unit) {
		if (value % prefix == 0) return String.format(Locale.ROOT, "%d %s", value / prefix, unit);
		return String.format(Locale.ROOT, "%.1f %s", (double) value / prefix, unit);
	}

	/**
	 * Sets the graphics card description (called by platform-specific initialization code).
	 * 
	 * @param string
	 *            the graphics card description
	 */
	public static void setGraphicsCard(final String string) { graphicsCard = string; }

	/** Cached system information list. */
	static List<String> INFO;

	/**
	 * Gathers and returns comprehensive system information as a formatted string.
	 * 
	 * <p>
	 * This method collects information about:
	 * </p>
	 * <ul>
	 * <li>GAMA version and build commit</li>
	 * <li>Computer hardware (OS, architecture, CPU cores, physical memory)</li>
	 * <li>Java Virtual Machine details</li>
	 * <li>Available memory for the JVM</li>
	 * <li>Graphics card</li>
	 * <li>Connected monitors (resolution, scaling, primary display)</li>
	 * </ul>
	 * 
	 * <p>
	 * The information is formatted as a multi-line string suitable for display in consoles or dialog boxes.
	 * </p>
	 * 
	 * @return a formatted string containing complete system information
	 */
	public static String getSystemInfo() {
		displays = new ArrayList<>();
		if (!GraphicsEnvironment.isHeadless()) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice sd : ge.getScreenDevices()) {
				displays.add(sd.getDisplayMode().getWidth() + "x" + sd.getDisplayMode().getHeight() + " zoom "
						+ String.valueOf((int) sd.getDefaultConfiguration().getDefaultTransform().getScaleX() * 100)
						+ "% " + (sd.equals(ge.getDefaultScreenDevice()) ? "(primary)" : ""));
			}

		}

		if (physicalMemory == null) {
			physicalMemory = "Memory not available";
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			Object size;
			try {
				size = mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"),
						"TotalMemorySize");
				physicalMemory = formatBytes(Long.parseLong(size.toString()));
			} catch (InstanceNotFoundException | AttributeNotFoundException | MalformedObjectNameException
					| ReflectionException | MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		INFO = new ArrayList<>();
		INFO.add("\n");
		INFO.add(STRINGS.PAD("GAMA VERSION:", 20)
				+ System.getProperty("gama.version", VERSION_NUMBER).replace("-SNAPSHOT", "") + " commit "
				+ System.getProperty("gama.commit", "Not available"));
		INFO.add(STRINGS.PAD("COMPUTER:", 20) + OS_NAME + " " + OS_VERSION + " on " + OS_ARCH + " ("
				+ Runtime.getRuntime().availableProcessors() + " cores, " + physicalMemory + ")");
		INFO.add(STRINGS.PAD("VIRTUAL MACHINE:", 20) + JAVA_VM_NAME + " " + JAVA_VM_VENDOR + " version "
				+ JAVA_VM_VERSION);
		INFO.add(STRINGS.PAD("AVAILABLE MEMORY:", 20) + formatBytes(Runtime.getRuntime().freeMemory()) + " / "
				+ formatBytes(Runtime.getRuntime().totalMemory()));
		INFO.add(STRINGS.PAD("GRAPHICS CARD:", 20) + graphicsCard);
		if (displays.isEmpty()) {
			INFO.add(STRINGS.PAD("MONITORS:", 20) + "None detected");
		} else {
			for (int i = 0; i < displays.size(); i++) {
				INFO.add(STRINGS.PAD("MONITOR #" + i + ":", 20) + displays.get(i));
			}
		}

		StringBuilder output = new StringBuilder();
		for (String line : INFO) {
			output.append(line);
			if (line != null && !line.endsWith("\n")) { output.append('\n'); }
		}
		return output.toString();

	}

	/**
	 * Main method for testing, demonstrating the use of SystemInfo.
	 * 
	 * @param args
	 *            command line arguments (unused)
	 */
	public static void main(final String[] args) {
		DEBUG.LOG(getSystemInfo());
	}

	/** Flag indicating if running on ARM architecture (Apple Silicon, ARM Windows, etc.). */
	private static boolean isARM = Platform.ARCH_AARCH64.equals(Platform.getOSArch());

	/** Cached result of isDeveloper() check. */
	private static volatile Boolean isDeveloper;

	/** Flag indicating if running on Linux. */
	private static boolean isLinux = "linux".equals(platformString);

	/** Flag indicating if running on macOS. */
	private static boolean isMac = "macosx".equals(platformString);

	/** Flag indicating if running on Windows. */
	private static boolean isWindows = "win32".equals(platformString);

	/**
	 * Checks if GAMA is running on ARM architecture.
	 * 
	 * <p>
	 * This includes Apple Silicon Macs (M1, M2, etc.) and ARM-based Windows devices.
	 * </p>
	 * 
	 * @return true if running on ARM/AArch64, false otherwise
	 */
	public static boolean isARM() { return isARM; }

	/**
	 * Checks if GAMA is running in development mode.
	 * 
	 * <p>
	 * Development mode is detected when running from Eclipse/PDE (not from a packaged installation). This is
	 * determined by checking if the install location contains "org.eclipse.pde.core".
	 * </p>
	 * 
	 * @return true if running in development/debug mode, false if running from a packaged installation
	 */
	public static boolean isDeveloper() { // NO_UCD (unused code)
		if (isDeveloper == null) {
			isDeveloper = Platform.getInstallLocation() == null
					|| Platform.getInstallLocation().getURL().getPath().contains("org.eclipse.pde.core");
		}
		return isDeveloper;
	}

	/**
	 * Checks if GAMA is running on Linux.
	 * 
	 * @return true if the platform is Linux, false otherwise
	 */
	public static boolean isLinux() { return isLinux; }

	/**
	 * Checks if GAMA is running on macOS.
	 * 
	 * @return true if the platform is macOS, false otherwise
	 */
	public static boolean isMac() { return isMac; }

	/**
	 * Checks if GAMA is running on Windows.
	 * 
	 * @return true if the platform is Windows, false otherwise
	 */
	public static boolean isWindows() { return isWindows; }

	/**
	 * Constructs a Java version number as an integer for easy comparison.
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param micro
	 *            the micro/patch version number
	 * @return an integer encoding the version (major << 16 | minor << 8 | micro)
	 */
	public static int javaVersion(final int major, final int minor, final int micro) {
		return (major << 16) + (minor << 8) + micro;
	}

	/**
	 * Parses a Java version string into an integer for comparison.
	 * 
	 * <p>
	 * Extracts major, minor, and micro version numbers from strings like "17.0.1", "11.0.12", etc.
	 * </p>
	 * 
	 * @param version
	 *            the version string to parse
	 * @return an integer encoding of the version, or 0 if the string cannot be parsed
	 */
	static int parseVersion(final String version) {
		if (version == null) return 0;
		int major = 0, minor = 0, micro = 0;
		final var length = version.length();
		int index = 0, start = 0;
		while (index < length && Character.isDigit(version.charAt(index))) { index++; }
		try {
			if (start < length) { major = Integer.parseInt(version.substring(start, index)); }
		} catch (final NumberFormatException e) {}
		start = ++index;
		while (index < length && Character.isDigit(version.charAt(index))) { index++; }
		try {
			if (start < length) { minor = Integer.parseInt(version.substring(start, index)); }
		} catch (final NumberFormatException e) {}
		start = ++index;
		while (index < length && Character.isDigit(version.charAt(index))) { index++; }
		try {
			if (start < length) { micro = Integer.parseInt(version.substring(start, index)); }
		} catch (final NumberFormatException e) {}
		return javaVersion(major, minor, micro);
	}

}
