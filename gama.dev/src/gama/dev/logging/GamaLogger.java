/*******************************************************************************************************
 *
 * GamaLogger.java, in gama.dev, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev.logging;

import static org.slf4j.helpers.MessageFormatter.basicArrayFormat;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;

import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.dev.FLAGS;

/**
 * <p>
 * Simple implementation of {@link Logger} that sends all enabled log messages, for all defined loggers, to
 * ({@code DEBUG.OUT} or {@code DEBUG.ERR}).
 */
public class GamaLogger extends LegacyAbstractLogger {

	/**
	 * Protected access allows only {@link GamaLoggerFactory} and also derived classes to instantiate GamaLogger
	 * instances.
	 */
	protected GamaLogger(final String name) {
		this.name = name;
	}

	/** Are {@code trace} messages currently enabled? */
	@Override
	public boolean isTraceEnabled() { return FLAGS.ENABLE_LEGACY_LOGGING; }

	/** Are {@code debug} messages currently enabled? */
	@Override
	public boolean isDebugEnabled() { return FLAGS.ENABLE_LEGACY_LOGGING; }

	/** Are {@code info} messages currently enabled? */
	@Override
	public boolean isInfoEnabled() { return FLAGS.ENABLE_LEGACY_LOGGING; }

	/** Are {@code warn} messages currently enabled? */
	@Override
	public boolean isWarnEnabled() { return FLAGS.ENABLE_LEGACY_LOGGING; }

	/** Are {@code error} messages currently enabled? */
	@Override
	public boolean isErrorEnabled() { return FLAGS.ENABLE_LEGACY_LOGGING; }

	/**
	 * GamaLogger's implementation
	 *
	 */
	@Override
	protected void handleNormalizedLoggingCall(final Level level, final Marker marker, final String messagePattern,
			final Object[] arguments, final Throwable throwable) {
		DEBUG.BANNER(BANNER_CATEGORY.SLF4J, basicArrayFormat(messagePattern, arguments), "issued by", getName());
		if (throwable != null) { throwable.printStackTrace(); }
	}

	@Override
	protected String getFullyQualifiedCallerName() { return null; }

}
