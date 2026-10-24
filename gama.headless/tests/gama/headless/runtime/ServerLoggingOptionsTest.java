/*******************************************************************************************************
 *
 * ServerLoggingOptionsTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.runtime;

import java.util.List;

import gama.api.utils.prefs.GamaPreferences;

/** Standalone checks for launch-only server logging overrides. */
public final class ServerLoggingOptionsTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) {
		final String enabled = System.getProperty("gama.server.log");
		final String directory = System.getProperty("gama.server.log.directory");
		final boolean preference = GamaPreferences.Runtime.CORE_SERVER_LOG.getValue();
		final String preferredDirectory = GamaPreferences.Runtime.CORE_SERVER_LOG_DIRECTORY.getValue();
		try {
			System.setProperty("gama.server.log", "false");
			System.setProperty("gama.server.log.directory", "/existing");
			HeadlessApplication.configureServerLogging(List.of("-socket", "1000", "-server-log",
					"-server-log-directory", "/path with spaces/logs"));
			check("true".equals(System.getProperty("gama.server.log")), "Enable flag must override JVM property");
			check("/path with spaces/logs".equals(System.getProperty("gama.server.log.directory")),
					"Directory override lost spaces");
			for (final var flags : List.of(List.of("-server-log", "-no-server-log"),
					List.of("-no-server-log", "-server-log"))) {
				HeadlessApplication.configureServerLogging(flags);
				check("false".equals(System.getProperty("gama.server.log")), "Explicit disable must win");
			}
			HeadlessApplication.configureServerLogging(List.of("-server-log-directory", "relative-logs"));
			check("false".equals(System.getProperty("gama.server.log")), "Directory alone must not enable logging");
			check("relative-logs".equals(System.getProperty("gama.server.log.directory")), "Relative path rejected");
			HeadlessApplication.configureServerLogging(List.of("-ssocket", "1000"));
			check("false".equals(System.getProperty("gama.server.log"))
					&& "relative-logs".equals(System.getProperty("gama.server.log.directory")),
					"No flags must preserve existing settings");
			for (final var invalid : List.of(List.of("-server-log-directory"),
					List.of("-server-log-directory", "-server-log"), List.of("-server-log-directory", ""))) {
				try {
					HeadlessApplication.configureServerLogging(invalid);
					throw new AssertionError("Missing directory accepted");
				} catch (final IllegalArgumentException expected) {
					check(expected.getMessage().contains("-server-log-directory"), "Unclear directory error");
				}
			}
			check(preference == GamaPreferences.Runtime.CORE_SERVER_LOG.getValue()
					&& preferredDirectory.equals(GamaPreferences.Runtime.CORE_SERVER_LOG_DIRECTORY.getValue()),
					"Launch overrides must not change saved preferences");
			System.out.println("ServerLoggingOptionsTest passed");
		} finally {
			if (enabled == null) System.clearProperty("gama.server.log");
			else System.setProperty("gama.server.log", enabled);
			if (directory == null) System.clearProperty("gama.server.log.directory");
			else System.setProperty("gama.server.log.directory", directory);
		}
	}
}
