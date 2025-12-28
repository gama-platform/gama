/*******************************************************************************************************
 *
 * GamaLoggingServiceProvider.java, in gama.dev, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev.logging;

import java.lang.reflect.Field;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import gama.dev.DEBUG;

/**
 * The Class GamaLoggingServiceProvider. Provides a gateway for SLF4J to access Gama's logging implementation.
 */
public class GamaLoggingServiceProvider implements SLF4JServiceProvider {

	/**
	 * Installs the bridge between the SFL4J logging sub system and DEBUG. Directly sets the static fields of
	 * LoggerFactory to use Gama's logging service provider.
	 *
	 * @throws NoSuchFieldException
	 *             the no such field exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	public static void install() throws NoSuchFieldException, IllegalAccessException {
		Field is = LoggerFactory.class.getDeclaredField("INITIALIZATION_STATE");
		is.setAccessible(true);
		is.set(null, 3); // Mark as INITIALIZED to avoid further attempts to initialize SLF4J
		Field pr = LoggerFactory.class.getDeclaredField("PROVIDER");
		pr.setAccessible(true);
		pr.set(null, new GamaLoggingServiceProvider());
	}

	/**
	 * Instantiates a new gama logging service provider.
	 */
	private GamaLoggingServiceProvider() {
		DEBUG.BANNER("Logging facilities", "provided by", "Gama");
	}

	/**
	 * Declare the version of the SLF4J API this implementation is compiled against. The value of this field is modified
	 * with each major release.
	 */
	public static String REQUESTED_API_VERSION = "2.0.17";

	/** The logger factory. */
	private final ILoggerFactory loggerFactory = new GamaLoggerFactory();

	/** The marker factory. */
	private final IMarkerFactory markerFactory = new BasicMarkerFactory();
	/** The mdc adapter. */
	private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

	/**
	 * Instantiates a new simple service provider.
	 */

	@Override
	public ILoggerFactory getLoggerFactory() { return loggerFactory; }

	@Override
	public IMarkerFactory getMarkerFactory() { return markerFactory; }

	@Override
	public MDCAdapter getMDCAdapter() { return mdcAdapter; }

	@Override
	public String getRequestedApiVersion() { return REQUESTED_API_VERSION; }

	@Override
	public void initialize() {}

}
