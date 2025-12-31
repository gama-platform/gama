/*******************************************************************************************************
 *
 * Activator.java, in gama.serialize, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.core.kernel.experiment.tools.ExperimentRecorderFactory;
import gama.extension.serialize.binary.SimulationSerialiser;

/**
 * The Class Activator.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
public class Activator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		ExperimentRecorderFactory.setRecorderClass(SimulationSerialiser.class);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {

	}

}
