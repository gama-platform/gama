/*******************************************************************************************************
 *
 * GamaLogger.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import gama.dev.DEBUG;

/**
 * <p>
 * Simple implementation of {@link Logger} that sends all enabled log messages, for all defined loggers, to
 * ({@code DEBUG.OUT} or {@code DEBUG.ERR}).
 */
public class GamaLogger extends LegacyAbstractLogger {

	/** The Constant START_TIME. */
	private static final long START_TIME = System.currentTimeMillis();

	/**
	 * Protected access allows only {@link GamaLoggerFactory} and also derived classes to instantiate SimpleLogger
	 * instances.
	 */
	protected GamaLogger(final String name) {
		this.name = name;
	}

	/** Are {@code trace} messages currently enabled? */
	@Override
	public boolean isTraceEnabled() { return true; }

	/** Are {@code debug} messages currently enabled? */
	@Override
	public boolean isDebugEnabled() { return true; }

	/** Are {@code info} messages currently enabled? */
	@Override
	public boolean isInfoEnabled() { return true; }

	/** Are {@code warn} messages currently enabled? */
	@Override
	public boolean isWarnEnabled() { return true; }

	/** Are {@code error} messages currently enabled? */
	@Override
	public boolean isErrorEnabled() { return true; }

	/**
	 * GamaLogger's implementation
	 *
	 */
	@Override
	protected void handleNormalizedLoggingCall(final Level level, final Marker marker, final String messagePattern,
			final Object[] arguments, final Throwable throwable) {
		StringBuilder buf = new StringBuilder(32);
		buf.append('[');
		buf.append(Thread.currentThread().getName());
		buf.append(" - ");
		buf.append(System.currentTimeMillis() - START_TIME);
		buf.append("] ");
		if (marker != null) {
			buf.append(' ');
			buf.append(marker.getName()).append(' ');
		}
		String formattedMessage = MessageFormatter.basicArrayFormat(messagePattern, arguments);
		// Append the message
		buf.append(formattedMessage);
		if (throwable != null) {
			DEBUG.ERR(buf, throwable);
		} else if (level.toInt() == LocationAwareLogger.ERROR_INT) {
			DEBUG.ERR(buf);
		} else {
			DEBUG.OUT(buf, true);
		}
	}

	@Override
	protected String getFullyQualifiedCallerName() { return null; }

}
