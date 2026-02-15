/*******************************************************************************************************
 *
 * SimulationPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.simulation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gama.api.GAMA;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IVariable;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.AmorphousTopology;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.GamaExecutorService.Caller;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.ISimulationRunner;
import gama.api.runtime.SimulationRunner;
import gama.api.ui.IStatusMessage;
import gama.api.utils.list.GamaListFactory;
import gama.core.experiment.ExperimentAgent;
import gama.core.experiment.ExperimentSpecies;
import gama.core.experiment.parameters.ParametersSet;
import gama.core.population.GamaPopulation;

/**
 * The Class SimulationPopulation.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class SimulationPopulation extends GamaPopulation<ISimulationAgent> implements IPopulation.Simulation {

	/** The current simulation. */
	private ISimulationAgent currentSimulation;

	/** The runner. */
	private final ISimulationRunner runner;

	/**
	 * Instantiates a new simulation population.
	 *
	 * @param agent
	 *            the agent
	 * @param species
	 *            the species
	 */
	public SimulationPopulation(final ExperimentAgent agent, final ISpecies species) {
		super(agent, species);
		runner = SimulationRunner.of(this);
	}

	/**
	 * Gets the max number of concurrent simulations.
	 *
	 * @return the max number of concurrent simulations
	 */
	public int getMaxNumberOfConcurrentSimulations() {
		return GamaExecutorService.getParallelism(getHost().getScope(), getHost().getSpecies().getConcurrency(),
				Caller.SIMULATION);
	}

	@Override
	public void removeValue(final IScope scope, final Object value) {
		if (value instanceof ISimulationAgent sim) {
			int index = indexOf(sim);
			if (index == -1) return;
			setCurrentSimulation(nextSimulationAfter(index));
			super.removeValue(scope, value);
		}
	}

	/**
	 * Next simulation after.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @return the simulation agent
	 * @date 26 août 2023
	 */
	private ISimulationAgent nextSimulationAfter(final int index) {
		if (size() <= 1 || index == -1) return null;
		return get((index + 1) % size());
	}

	/**
	 * Method fireAgentRemoved()
	 *
	 */
	@Override
	protected void fireAgentRemoved(final IScope scope, final IAgent old) {
		super.fireAgentRemoved(scope, old);
		runner.remove((ISimulationAgent) old);
	}

	@Override
	public void initializeFor(final IScope scope) {
		super.initializeFor(scope);
		this.currentAgentIndex = 0;
	}

	@Override
	public void dispose() {
		runner.dispose();
		super.dispose();
	}

	@Override
	public Iterable<ISimulationAgent> iterable(final IScope scope) {
		return (Iterable<ISimulationAgent>) getAgents(scope);
	}

	/**
	 * Creates the agents.
	 *
	 * @param scope
	 *            the scope
	 * @param number
	 *            the number
	 * @param initialValues
	 *            the initial values
	 * @param isRestored
	 *            the is restored
	 * @param toBeScheduled
	 *            the to be scheduled
	 * @param sequence
	 *            the sequence
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IList<ISimulationAgent> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final IStatement sequence) throws GamaRuntimeException {

		final IList<ISimulationAgent> result = GamaListFactory.create(ISimulationAgent.class);
		final IAgentConstructor constr = species.getDescription().getAgentConstructor();
		scope.getGui().getStatus().waitStatus("Initializing simulations", IStatusMessage.SIMULATION_ICON, () -> {
			for (int i = 0; i < number; i++) {

				// Model do not only rely on SimulationAgent

				ISimulationAgent sim = (ISimulationAgent) constr.createOneAgent(this, currentAgentIndex++);
				sim.setScheduled(toBeScheduled);
				sim.setName("Simulation " + sim.getIndex());
				add(sim);
				boolean isBatch = getHost().getSpecies().isBatch();
				// Batch experiments now dont allow their simulations to have outputs

				if (!isBatch) { sim.setOutputs(((ExperimentSpecies) host.getSpecies()).getOriginalSimulationOutputs()); }
				if (!scope.interrupted()) {
					// Necessary to set it early -- see Issue #3872
					setCurrentSimulation(sim);
					initSimulation(scope, sim, initialValues, i, isRestored, toBeScheduled, sequence);
					if (toBeScheduled) { runner.add(sim); }
					result.add(sim);
				}
			}
		});

		// Linked to Issue #2430. Should not return this, but the newly created simulations
		// return this;
		return result;
	}

	/**
	 * Inits the simulation.
	 *
	 * @param scope
	 *            the scope
	 * @param sim
	 *            the sim
	 * @param initialValues
	 *            the initial values
	 * @param index
	 *            the index
	 * @param isRestored
	 *            the is restored
	 * @param toBeScheduled
	 *            the to be scheduled
	 * @param sequence
	 *            the sequence
	 */
	private void initSimulation(final IScope scope, final ISimulationAgent sim,
			final List<? extends Map<String, Object>> initialValues, final int index, final boolean isRestored,
			final boolean toBeScheduled, final IStatement sequence) {
		scope.getGui().getStatus().waitStatus("Instantiating agents", IStatusMessage.SIMULATION_ICON, () -> {
			final Map<String, Object> firstInitValues =
					initialValues.isEmpty() ? ParametersSet.EMPTY : initialValues.get(index);
			final Object firstValue = !firstInitValues.isEmpty() ? firstInitValues.values().toArray()[0] : null;
			if (firstValue instanceof ISerialisedAgent sa) {
				sim.updateWith(scope, sa);
			} else {
				// See issue #130 -- we add the parameters values to make sure they are passed (but not for batch).
				if (!getHost().getSpecies().isBatch()) { sim.setExternalInits(getHost().getParameterValues()); }
				sim.setExternalInits(firstInitValues);
				createVariablesFor(sim.getScope(), Collections.singletonList(sim),
						Collections.singletonList(sim.getExternalInits()));
			}

			if (toBeScheduled) {
				if (isRestored || firstValue instanceof ISerialisedAgent) {
					sim.initOutputs();
				} else {
					sim.schedule(scope);
					if (sequence != null && !sequence.isEmpty()) { scope.execute(sequence, sim, null); }
				}
			}
		});

	}

	@Override
	protected boolean allowVarInitToBeOverridenByExternalInit(final IVariable theVar) {
		return switch (theVar.getName()) {
			case IKeyword.SEED, IKeyword.RNG -> !theVar.hasFacet(IKeyword.INIT);
			default -> true;
		};
	}

	@Override
	public ExperimentAgent getHost() { return (ExperimentAgent) super.getHost(); }

	@Override
	public ISimulationAgent getAgent(final IScope scope, final IPoint value) {
		return get(null, 0);
	}

	/**
	 * Sets the host.
	 *
	 * @param agent
	 *            the new host
	 */
	public void setHost(final ExperimentAgent agent) { host = agent; }

	@Override
	public void computeTopology(final IScope scope) throws GamaRuntimeException {
		// Temporary topology set before the world gets a shape
		topology = new AmorphousTopology();
	}

	@Override
	protected boolean stepAgents(final IScope scope) {
		runner.step();
		return true;
	}

	/**
	 * This method can be called by the batch experiments to temporarily stop (unschedule) a simulation
	 *
	 * @param sim
	 */
	public void unscheduleSimulation(final ISimulationAgent sim) {
		runner.remove(sim);
	}

	/**
	 * Gets the number of active stepables.
	 *
	 * @return the number of active stepables
	 */
	public Set<ISimulationAgent> getRunningSimulations() { return runner.getStepable(); }

	/**
	 * Gets the number of active threads.
	 *
	 * @return the number of active threads
	 */
	@Override
	public int getNumberOfActiveThreads() { return runner.getActiveThreads(); }

	/**
	 * @return
	 */
	public boolean hasScheduledSimulations() {
		return runner.hasSimulations();
	}

	/**
	 * Last simulation created.
	 *
	 * @return the simulation agent
	 */
	public ISimulationAgent getCurrentSimulation() { return currentSimulation; }

	/**
	 * Sets the current simulation.
	 *
	 * @param current
	 *            the new current simulation
	 */
	public void setCurrentSimulation(final ISimulationAgent current) {
		currentSimulation = current;
		GAMA.changeCurrentTopLevelAgent(current == null ? getHost() : current, false);
	}

}