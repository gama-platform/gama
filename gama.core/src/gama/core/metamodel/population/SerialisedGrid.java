/*******************************************************************************************************
 *
 * SerialisedGrid.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.population;

import java.util.ArrayList;
import java.util.List;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.ISerialisedAgent;
import gama.core.metamodel.agent.SerialisedAgent;
import gama.core.metamodel.topology.grid.GridPopulation;
import gama.core.metamodel.topology.grid.IGrid;
import gama.core.runtime.IScope;

/**
 * The SerialisedGrid.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 27 août 2023
 */
public record SerialisedGrid(String speciesName, List<ISerialisedAgent> agents, IGrid matrix)
		implements ISerialisedPopulation {

	/**
	 * Instantiates a new serialised grid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param pop
	 *            the pop
	 * @date 27 août 2023
	 */
	public SerialisedGrid(final GridPopulation pop) {
		this(pop.getSpecies().getName(), new ArrayList<>(), pop.getTopology().getPlaces());
		for (IAgent a : pop) { agents.add(SerialisedAgent.of(a, true)); }
	}

	/**
	 * Checks if is grid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is grid
	 * @date 27 août 2023
	 */
	@Override
	public boolean isGrid() { return true; }
	//
	// @Override
	// public JsonObject serializeToJson(final Json json) {
	// return ISerialisedPopulation.super.serializeToJson(json).add("cols", matrix.getCols(null)).add("rows",
	// matrix.getRows(null));
	// }

	/**
	 * Restore as.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param pop
	 *            the pop
	 * @date 31 oct. 2023
	 */
	public void restoreAs(final IScope scope, final IPopulation<? extends IAgent> pop) {
		GridPopulation grid = (GridPopulation) pop;
		grid.setGrid(matrix());
		for (ISerialisedAgent a : agents()) {
			IAgent agent = pop.getAgent(a.getIndex());
			a.attributes().forEach((name, v) -> { agent.setDirectVarValue(scope, name, v); });
		}

	}

}
