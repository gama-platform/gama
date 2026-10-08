/*******************************************************************************************************
 *
 * Activator.java, in gama.dev, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.dev.logging.GamaLoggingServiceProvider;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		GamaLoggingServiceProvider.install();
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {}

}
