/*******************************************************************************************************
 *
 * SerializeActivator.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize;

import org.osgi.framework.BundleContext;

import gama.api.GAMA;
import gama.dependencies.GamaBundleActivator;
import gama.extension.serialize.binary.SimulationSerialiser;

/**
 * The Class SerializeActivator.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
public class SerializeActivator extends GamaBundleActivator {

	/**
	 * Start.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void initialize(final BundleContext context) {
		GAMA.setRecorderClass(SimulationSerialiser.class);
	}

}
