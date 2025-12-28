/*******************************************************************************************************
 *
 * GamaLoggingServiceProvider.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
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
	 * Declare the version of the SLF4J API this implementation is compiled against. The value of this field is modified
	 * with each major release.
	 */
	public static String REQUESTED_API_VERSION = "2.0.17";

	/** The logger factory. */
	private ILoggerFactory loggerFactory;

	/** The marker factory. */
	private final IMarkerFactory markerFactory;
	/** The mdc adapter. */
	private final MDCAdapter mdcAdapter;

	/**
	 * Instantiates a new simple service provider.
	 */
	public GamaLoggingServiceProvider() {
		markerFactory = new BasicMarkerFactory();
		mdcAdapter = new NOPMDCAdapter();
		DEBUG.BANNER("Logging facilities", "provided by", "Gama");
	}

	@Override
	public ILoggerFactory getLoggerFactory() { return loggerFactory; }

	@Override
	public IMarkerFactory getMarkerFactory() { return markerFactory; }

	@Override
	public MDCAdapter getMDCAdapter() { return mdcAdapter; }

	@Override
	public String getRequestedApiVersion() { return REQUESTED_API_VERSION; }

	@Override
	public void initialize() {
		loggerFactory = new GamaLoggerFactory();

	}

}
