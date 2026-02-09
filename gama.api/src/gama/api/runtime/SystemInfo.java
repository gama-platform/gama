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
 * The Class SystemInfo.
 */
public class SystemInfo {

	/**
	 * The JAVA version
	 */
	public static final int JAVA_VERSION; // NO_UCD (unused code)

	/** The platform string. */
	private static String platformString = Platform.getOS();

	static {
		JAVA_VERSION = parseVersion(System.getProperty("java.version")); //$NON-NLS-1$
	}

	/** The Constant VERSION_NUMBER. */
	public static final String VERSION_NUMBER = "0.0.0-SNAPSHOT";

	/** The Constant VERSION. */
	public static final String VERSION = "GAMA " + VERSION_NUMBER;

	/** The java vm name. */
	public static final String JAVA_VM_NAME = System.getProperty("java.vm.name");

	/** The java vm vendor. */
	public static final String JAVA_VM_VENDOR = System.getProperty("java.vm.vendor");

	/** The java vm version. */
	public static final String JAVA_VM_VERSION = System.getProperty("java.vm.version");

	/** The os name. */
	public static final String OS_NAME = System.getProperty("os.name");

	/** The os version. */
	public static final String OS_VERSION = System.getProperty("os.version");

	/** The os arch. */
	public static final String OS_ARCH = System.getProperty("os.arch");

	/**
	 * Binary prefixes, used in IEC Standard for naming bytes.
	 * (https://en.wikipedia.org/wiki/International_Electrotechnical_Commission)
	 *
	 * Should be used for most representations of bytes
	 */
	private static final long KIBI = 1L << 10;

	/** The Constant MEBI. */
	private static final long MEBI = 1L << 20;

	/** The Constant GIBI. */
	private static final long GIBI = 1L << 30;

	/** The Constant TEBI. */
	private static final long TEBI = 1L << 40;

	/** The Constant PEBI. */
	private static final long PEBI = 1L << 50;

	/** The Constant EXBI. */
	private static final long EXBI = 1L << 60;

	/** The displays. */
	static List<String> displays;

	/** The graphics card. */
	public static String graphicsCard = "Not available";

	/** The physical memory. */
	public static String physicalMemory;

	/**
	 * Format bytes into a rounded string representation using IEC standard (matches Mac/Linux).
	 *
	 * @param bytes
	 *            Bytes.
	 * @return Rounded string representation of the byte size.
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
	 * Format units as exact integer or fractional decimal based on the prefix, appending the appropriate units
	 *
	 * @param value
	 *            The value to format
	 * @param prefix
	 *            The divisor of the unit multiplier
	 * @param unit
	 *            A string representing the units
	 * @return A string with the value
	 */
	private static String formatUnits(final long value, final long prefix, final String unit) {
		if (value % prefix == 0) return String.format(Locale.ROOT, "%d %s", value / prefix, unit);
		return String.format(Locale.ROOT, "%.1f %s", (double) value / prefix, unit);
	}

	/**
	 * Sets the graphics card.
	 *
	 * @param string
	 *            the new graphics card
	 */
	public static void setGraphicsCard(final String string) { graphicsCard = string; }

	/** The INFO. */
	static List<String> INFO;

	/** The get system info. */
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
	 * The main method, demonstrating use of classes.
	 *
	 * @param args
	 *            the arguments (unused)
	 */
	public static void main(final String[] args) {
		DEBUG.LOG(getSystemInfo());
	}

	/** The is ARM. */
	private static boolean isARM = Platform.ARCH_AARCH64.equals(Platform.getOSArch());

	/** The is developer. */
	private static volatile Boolean isDeveloper;

	/** The is linux. */
	private static boolean isLinux = "linux".equals(platformString);

	/** The is mac. */
	private static boolean isMac = "macosx".equals(platformString);

	/** The is windows. */
	private static boolean isWindows = "win32".equals(platformString);

	/**
	 * Checks if is arm.
	 *
	 * @return true, if is arm
	 */
	public static boolean isARM() { return isARM; }

	/**
	 * Checks if is developer.
	 *
	 * @return true, if is developer
	 */
	public static boolean isDeveloper() { // NO_UCD (unused code)
		if (isDeveloper == null) {
			isDeveloper = Platform.getInstallLocation() == null
					|| Platform.getInstallLocation().getURL().getPath().contains("org.eclipse.pde.core");
		}
		return isDeveloper;
	}

	/**
	 * Checks if is linux.
	 *
	 * @return true, if is linux
	 */
	public static boolean isLinux() { return isLinux; }

	/**
	 * Checks if is mac.
	 *
	 * @return true, if is mac
	 */
	public static boolean isMac() { return isMac; }

	/**
	 * Checks if is windows.
	 *
	 * @return true, if is windows
	 */
	public static boolean isWindows() { return isWindows; }

	/**
	 * Returns the Java version number as an integer.
	 *
	 * @param major
	 * @param minor
	 * @param micro
	 * @return the version
	 */
	public static int javaVersion(final int major, final int minor, final int micro) {
		return (major << 16) + (minor << 8) + micro;
	}

	/**
	 * Parses the version.
	 *
	 * @param version
	 *            the version
	 * @return the int
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
