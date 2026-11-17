/*******************************************************************************************************
 *
 * ObjectModelOwners.java, in gama.api, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.object;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;

/** Weak identity bindings keep class objects associated with their model without retaining disposed simulations. */
final class ObjectModelOwners {

	private static final ConcurrentMap<IObject, ISimulationAgent> OWNERS =
			new MapMaker().weakKeys().weakValues().makeMap();

	static ISimulationAgent get(final IObject object) { return OWNERS.get(object); }

	static void bind(final IObject object, final IScope scope) {
		final IObject creator = scope.getCurrentObjectOrAgent();
		final ISimulationAgent owner;
		if (creator instanceof ISimulationAgent simulation) {
			owner = simulation;
		} else if (creator instanceof ITopLevelAgent) {
			owner = null;
		} else if (creator instanceof IAgent agent) {
			owner = agent.getSimulation();
		} else if (creator != null) {
			owner = creator.getModelOwner();
		} else {
			owner = scope.getRoot() instanceof ISimulationAgent simulation ? simulation : null;
		}
		if (owner == null) {
			OWNERS.remove(object);
		} else {
			OWNERS.put(object, owner);
		}
	}

	private ObjectModelOwners() {}
}
