/*******************************************************************************************************
 *
 * Activator.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.awt.GraphicsEnvironment;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.core.runtime.GAMA;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 * The Class Activator.
 */
public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		// We set the snapshot maker early so that it becomes available early from the IGui access
		// In reference to #3689, the GraphicsEnvironment is queried earlier for headless mode.
		String message, mode;

		if (GraphicsEnvironment.isHeadless()) {
			message = "inactive";
			mode = "(headless mode)";
		} else {
			message = "active";
			mode = "(gui mode)";
			GAMA.setSnapshotMaker(SnapshotMaker.getInstance());
		}
		DEBUG.BANNER(BANNER_CATEGORY.GUI, "Snapshot services", message, mode);
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {

	}

}
