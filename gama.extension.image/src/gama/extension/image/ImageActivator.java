/*******************************************************************************************************
 *
 * ImageActivator.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.awt.GraphicsEnvironment;

import org.osgi.framework.BundleContext;

import gama.api.GAMA;
import gama.dependencies.GamaBundleActivator;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 * The Class ImageActivator.
 */
public class ImageActivator extends GamaBundleActivator {

	/**
	 * Start.
	 *
	 * @param bundleContext
	 *            the bundle context
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void initialize(final BundleContext bundleContext) {
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

}
