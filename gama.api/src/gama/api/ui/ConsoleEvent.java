/*******************************************************************************************************
 *
 * ConsoleEvent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import java.time.Instant;
import java.util.Objects;

import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.color.IColor;

/** An immutable, typed console message with optional simulation and presentation context. */
public record ConsoleEvent(Level level, String message, ITopLevelAgent agent, IColor color, Integer cycle,
		String source, Instant timestamp) {

	public enum Level {
		INFO, DEBUG, WARNING, ERROR
	}

	public ConsoleEvent {
		Objects.requireNonNull(level);
		Objects.requireNonNull(timestamp);
	}

	public static ConsoleEvent info(final String message, final ITopLevelAgent agent, final IColor color) {
		return new ConsoleEvent(Level.INFO, message, agent, color, null, null, Instant.now());
	}

	public static ConsoleEvent debug(final int cycle, final String message, final ITopLevelAgent agent,
			final IColor color) {
		return new ConsoleEvent(Level.DEBUG, message, agent, color, cycle, null, Instant.now());
	}

	public ConsoleEvent withColor(final IColor replacement) {
		return new ConsoleEvent(level, message, agent, replacement, cycle, source, timestamp);
	}
}
