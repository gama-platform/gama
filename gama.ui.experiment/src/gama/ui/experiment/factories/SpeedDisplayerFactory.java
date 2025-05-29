/*******************************************************************************************************
 *
 * SpeedDisplayerFactory.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.factories;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.ui.shared.controls.SimulationSpeedControl;

/**
 * A factory for creating SpeedDisplayer objects.
 */
public class SpeedDisplayerFactory extends AbstractServiceFactory {

	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return SimulationSpeedControl.getInstance();
	}

}
