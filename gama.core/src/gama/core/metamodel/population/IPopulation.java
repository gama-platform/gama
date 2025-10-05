/*******************************************************************************************************
 *
 * IPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.population;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;

import gama.core.common.interfaces.IDisposable;
import gama.core.common.interfaces.IStepable;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonObject;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.variables.IVariable;

/**
 * A population is a collection of agents of a species.
 *
 * Written by drogoul Modified on 24 juin 2010
 *
 * @todo Description
 *
 */
public interface IPopulation<T extends IAgent>
		extends Comparable<IPopulation<T>>, IList<T>, IStepable, IDisposable, IPopulationSet<T> {

	/**
	 * The Interface Listener.
	 */
	public interface Listener {

		/**
		 * Notify agent removed.
		 *
		 * @param scope
		 *            the scope
		 * @param pop
		 *            the pop
		 * @param agent
		 *            the agent
		 */
		void notifyAgentRemoved(IScope scope, IPopulation<? extends IAgent> pop, IAgent agent);

		/**
		 * Notify agent added.
		 *
		 * @param scope
		 *            the scope
		 * @param pop
		 *            the pop
		 * @param agent
		 *            the agent
		 */
		void notifyAgentAdded(IScope scope, IPopulation<? extends IAgent> pop, IAgent agent);

		/**
		 * Notify agents added.
		 *
		 * @param scope
		 *            the scope
		 * @param pop
		 *            the pop
		 * @param agents
		 *            the agents
		 */
		void notifyAgentsAdded(IScope scope, IPopulation<? extends IAgent> pop, Collection<? extends IAgent> agents);

		/**
		 * Notify agents removed.
		 *
		 * @param scope
		 *            the scope
		 * @param pop
		 *            the pop
		 * @param agents
		 *            the agents
		 */
		void notifyAgentsRemoved(IScope scope, IPopulation<? extends IAgent> pop, Collection<? extends IAgent> agents);

		/**
		 * Notify population cleared.
		 *
		 * @param scope
		 *            the scope
		 * @param pop
		 *            the pop
		 */
		void notifyPopulationCleared(IScope scope, IPopulation<? extends IAgent> pop);

	}

	/**
	 * The Class IsLiving.
	 */
	public static class IsLiving implements Predicate<IAgent> {

		/**
		 * Method apply()
		 *
		 * @see com.google.common.base.Predicate#apply(java.lang.Object)
		 */
		@Override
		public boolean apply(final IAgent input) {
			return input != null && !input.dead();
		}

	}

	/**
	 * Creates the empty.
	 *
	 * @param species
	 *            the species
	 * @return the i population<? extends I agent>
	 */
	static IPopulation<? extends IAgent> createEmpty(final ISpecies species) {
		return new GamaPopulation<>(null, species);
	}

	/**
	 * Creates the variables for.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void createVariablesFor(IScope scope, T agent) throws GamaRuntimeException;

	/**
	 * Checks for var.
	 *
	 * @param n
	 *            the n
	 * @return true, if successful
	 */
	boolean hasVar(final String n);

	/**
	 * Gets the population.
	 *
	 * @param scope
	 *            the scope
	 * @return the population
	 */
	@Override
	default IPopulation<? extends IAgent> getPopulation(final IScope scope) {
		return this;
	}

	/**
	 * Creates the one agent.
	 *
	 * @param scope
	 *            the scope
	 * @param initialValues
	 *            the initial values
	 * @return the t
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	default T createOneAgent(final IScope scope, final Map<String, Object> initialValues) throws GamaRuntimeException {
		return createAgentAtIndex(scope, size(), initialValues, false, true);
	}

	/**
	 * Create agents as members of this population.
	 *
	 * @param scope
	 * @param number
	 *            The number of agent to create.
	 * @param initialValues
	 *            The initial values of agents' variables.
	 * @param isRestored
	 *            Indicates that the agents are newly created or they are restored (on a capture or release). If agents
	 *            are restored on a capture or release then don't run their "init" reflex again
	 * @param toBeScheduled
	 *            Whether the agent should be immediately scheduled or not
	 * @param sequence
	 *            an optional sequence of code to be run after the agent has been scheduled (and then inited). Can be
	 *            null.
	 *
	 * @return
	 * @throws GamaRuntimeException
	 */
	IList<T> createAgents(IScope scope, int number, List<? extends Map<String, Object>> initialValues,
			boolean isRestored, boolean toBeScheduled, RemoteSequence sequence) throws GamaRuntimeException;

	/**
	 * Creates the agents.
	 *
	 * @param scope
	 *            the scope
	 * @param geometries
	 *            the geometries
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	IList<T> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries)
			throws GamaRuntimeException;

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
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	default IList<T> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled) throws GamaRuntimeException {
		return this.createAgents(scope, number, initialValues, isRestored, toBeScheduled, null);
	}

	/**
	 * Creates the agent at.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @param initialValues
	 *            the initial values
	 * @param isRestored
	 *            the is restored
	 * @param toBeScheduled
	 *            the to be scheduled
	 * @return the t
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	T createAgentAtIndex(final IScope s, int index, Map<String, Object> initialValues, boolean isRestored,
			boolean toBeScheduled) throws GamaRuntimeException;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Checks if is grid.
	 *
	 * @return true, if is grid
	 */
	boolean isGrid();

	/**
	 * Checks for aspect.
	 *
	 * @param default1
	 *            the default 1
	 * @return true, if successful
	 */
	boolean hasAspect(String default1);

	/**
	 * Gets the aspect.
	 *
	 * @param default1
	 *            the default 1
	 * @return the aspect
	 */
	IExecutable getAspect(String default1);

	/**
	 * Gets the aspect names.
	 *
	 * @return the aspect names
	 */
	Collection<String> getAspectNames();

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	@Override
	ISpecies getSpecies();

	/**
	 * Gets the var.
	 *
	 * @param s
	 *            the s
	 * @return the var
	 */
	IVariable getVar(final String s);

	/**
	 * Checks for updatable variables.
	 *
	 * @return true, if successful
	 */
	boolean hasUpdatableVariables();

	/**
	 * Returns the topology associated to this population.
	 *
	 * @return
	 */
	ITopology getTopology();

	/**
	 * Initialize for.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void initializeFor(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the macro-agent hosting this population.
	 *
	 * @return
	 */
	IMacroAgent getHost();

	/**
	 * Set the macro-agent hosting this population.
	 *
	 * @return
	 */
	void setHost(IMacroAgent agt);

	/**
	 * @throws GamaRuntimeException
	 *             When the "shape" of host changes, this method is invoked to update the topology.
	 */
	// public abstract void hostChangesShape();

	/**
	 * Kills all the agents managed by this population.
	 *
	 * @throws GamaRuntimeException
	 */
	void killMembers() throws GamaRuntimeException;

	/**
	 * @param obj
	 * @return
	 */
	T getAgent(Integer obj);

	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addListener(IPopulation.Listener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void removeListener(IPopulation.Listener listener);

	/**
	 * Update variables.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 */
	void updateVariables(IScope scope, IAgent a);

	/**
	 * @param scope
	 * @param coord
	 * @return
	 */
	T getAgent(IScope scope, GamaPoint coord);

	/**
	 * To array.
	 *
	 * @return the t[]
	 */
	@Override
	T[] toArray();

	/**
	 * Checks if is inits the overriden.
	 *
	 * @return true, if is inits the overriden
	 */
	boolean isInitOverriden();

	/**
	 * Checks if is step overriden.
	 *
	 * @return true, if is step overriden
	 */
	boolean isStepOverriden();

	/**
	 * Gets the or create agent. If the agent cannot be found at this index, creates a "blank" agent (no initialisation
	 * of any kind except the index). Used for serialisation
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @return the or create agent
	 * @date 6 août 2023
	 */
	T getOrCreateAgent(final IScope scope, final Integer index);

	/**
	 * Serialize to json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @return the json value
	 * @date 31 oct. 2023
	 */
	@Override
	default JsonObject serializeToJson(final Json json) {
		return json.object("population", getSpecies().getName(), "agents", this.subList(0, size()));
		// return json.valueOf(new SerialisedPopulation(this));
	}

	/**
	 * Fire agents added.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 */
	<T extends IAgent> void fireAgentsAdded(final IScope scope, final IList<T> agents);

	/**
	 * @return
	 */
	boolean isDisposing();

}