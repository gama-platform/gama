/*******************************************************************************************************
 *
 * ValuedDisplayOutputFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.Collection;

import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.model.GamlModelSpecies;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.species.ISpecies;

/**
 * A factory for creating ValuedDisplayOutput objects.
 */
public class ValuedDisplayOutputFactory {

	/**
	 * Browse.
	 *
	 * @param agents
	 *            the agents
	 */
	public static void browse(final Collection<? extends IAgent> agents) {
		IPopulation<? extends IAgent> pop = null;
		IMacroAgent root = null;
		if (agents instanceof IPopulation) {
			pop = (IPopulation<? extends IAgent>) agents;
			browse(pop.getHost(), pop.getSpecies());
		} else {
			for (final IAgent agent : agents) {
				if (agent == null) { continue; }
				final IPopulation<?> agentPop = agent.getPopulation();
				root = agentPop.getHost();
				if (root != null) { break; }
			}
			if (root == null) return;
			final IMacroAgent realRoot = findRootOf(root, agents);
			if (realRoot == null) throw GamaRuntimeException
					.error("Impossible to find a common host agent for " + agents, root.getScope());
			InspectDisplayOutput.browse(realRoot, agents, null).launch(realRoot.getScope());
		}
	}

	/**
	 * Find root of.
	 *
	 * @param root
	 *            the root
	 * @param agents
	 *            the agents
	 * @return the i macro agent
	 */
	private static IMacroAgent findRootOf(final IMacroAgent root, final Collection<? extends IAgent> agents) {
		if (agents instanceof IPopulation) return ((IPopulation<? extends IAgent>) agents).getHost();
		IMacroAgent result = null;
		for (final IAgent a : agents) {
			if (a == null) { continue; }
			if (result == null) {
				result = a.getHost();
			} else if (a.getHost() != result) return null;
		}
		return result;

	}

	/**
	 * Browse.
	 *
	 * @param root
	 *            the root
	 * @param species
	 *            the species
	 */
	public static void browse(final IMacroAgent root, final ISpecies species) {
		if (root instanceof IExperimentAgent && species instanceof GamlModelSpecies) {
			// special case to be able to browse simulations, as their species is not contained in the experiment
			// species
			InspectDisplayOutput.browse(root, species, null).launch(root.getScope());
			return;
		}
		if (!root.getSpecies().getMicroSpecies().contains(species)) {
			if (!(root instanceof ExperimentAgent)) throw GamaRuntimeException
					.error("Agent " + root + " has no access to populations of " + species.getName(), root.getScope());
			final IMacroAgent realRoot = ((ExperimentAgent) root).getSimulation();
			browse(realRoot, species);
			return;
		}
		InspectDisplayOutput.browse(root, species, null).launch(root.getScope());
	}

	/**
	 * Browse.
	 *
	 * @param root
	 *            the root
	 * @param expr
	 *            the expr
	 */
	public static void browse(final IMacroAgent root, final IExpression expr, final IExpression attributes) {
		final SpeciesDescription species = expr.getGamlType().isContainer()
				? expr.getGamlType().getContentType().getSpecies() : expr.getGamlType().getSpecies();
		if (species == null) throw GamaRuntimeException
				.error("Expression '" + expr.serializeToGaml(true) + "' does not reference agents", root.getScope());
		final ISpecies rootSpecies = root.getSpecies();
		if (rootSpecies.getMicroSpecies(species.getName()) == null) {
			if (!(root instanceof ExperimentAgent)) throw GamaRuntimeException
					.error("Agent " + root + " has no access to populations of " + species.getName(), root.getScope());
			final IMacroAgent realRoot = ((ExperimentAgent) root).getSimulation();
			browse(realRoot, expr, attributes);
			return;
		}
		InspectDisplayOutput.browse(root, expr, attributes).launch(root.getScope());
	}

	/**
	 * Browse simulations.
	 *
	 * @param host
	 *            the host
	 */
	public static void browseSimulations(final ExperimentAgent host) {
		InspectDisplayOutput.inspect(host, null).launch(host.getScope());
	}

}
