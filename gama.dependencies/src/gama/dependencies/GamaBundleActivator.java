/*******************************************************************************************************
 *
 * GamaBundleActivator.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 *
 */
public abstract class GamaBundleActivator implements BundleActivator {

	@Override
	public final void start(final BundleContext context) throws Exception {
		DEBUG.TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAMA, "Activation of " + context.getBundle().getSymbolicName(),
				"done in", () -> {
					initialize(context);
				});

	}

	/**
	 * @param context
	 */
	protected abstract void initialize(BundleContext context);

	@Override
	public final void stop(final BundleContext context) throws Exception {}

}
