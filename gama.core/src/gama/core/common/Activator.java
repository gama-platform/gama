/*******************************************************************************************************
 *
 * Activator.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.common;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.core.runtime.concurrent.GamaExecutorService;
import gama.gaml.compilation.kernel.GamaBundleLoader;
import gama.gaml.operators.Dates;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		/* Early build of the contributions made by plugins to GAMA */
		GamaBundleLoader.preBuildContributions();
		GamaExecutorService.reset();
		Dates.initialize();

	}

	@Override
	public void stop(final BundleContext context) throws Exception {}

}
