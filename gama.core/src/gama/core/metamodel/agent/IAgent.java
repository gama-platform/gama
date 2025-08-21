/*******************************************************************************************************
 *
 * IAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.agent;

import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.IScoped;
import gama.core.common.interfaces.IStepable;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IDelegatingShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.interfaces.INamed;
import gama.gaml.species.ISpecies;
import gama.gaml.types.IType;
import gama.gaml.variables.IVariable;

/**
 * Written by drogoul on Apr. 07, Modified on 24 oct. 2010, 05 Apr. 2013
 *
 * @todo Description
 *
 */

/**
 * The Interface IAgent.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 7 oct. 2023
 */
@vars ({ @variable (
		name = IKeyword.NAME,
		type = IType.STRING,
		doc = { @doc ("Returns the name of the agent (not necessarily unique in its population)") }),
		@variable (
				name = IKeyword.INDEX,
				type = IType.INT,
				constant = true,
				doc = { @doc ("Returns the unique index of this agent in its population. Read-only attribute") }),
		@variable (
				name = IKeyword.PEERS,
				type = IType.LIST,
				of = ITypeProvider.OWNER_TYPE,
				doc = { @doc ("Returns the population of agents of the same species, in the same host, minus the receiver agent") }),
		@variable (
				name = IKeyword.HOST,
				type = ITypeProvider.MACRO_TYPE,
				doc = { @doc ("Returns the agent that hosts the population of the receiver agent") }),
		@variable (
				name = IKeyword.LOCATION,
				type = IType.POINT,
				depends_on = IKeyword.SHAPE,
				doc = { @doc ("Returns the location of the agent") }),
		@variable (
				name = IKeyword.SHAPE,
				type = IType.GEOMETRY,
				doc = { @doc ("Returns the shape of the receiver agent") }) })
@doc ("The species hierarchy derives from a single built-in species, which is 'agent'. All its components (attributes, actions) will then be inherited by all direct or indirect children species (including 'model' and 'experiment'")
public interface IAgent extends IObject<ISpecies>, IDelegatingShape, INamed, Comparable<IAgent>, IStepable, IScoped {

	/**
	 * Returns the topology which manages this agent.
	 *
	 * @return
	 */
	ITopology getTopology();

	/**
	 * Sets the peers.
	 *
	 * @param peers
	 *            the new peers
	 */
	@setter (IKeyword.PEERS)
	default void setPeers(final IList<IAgent> peers) {
		// "peers" is read-only attribute
	}

	/**
	 * Returns agents having the same species and sharing the same direct host with this agent.
	 *
	 * @return
	 */
	@getter (IKeyword.PEERS)
	IList<IAgent> getPeers() throws GamaRuntimeException;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	@getter (
			value = IKeyword.NAME,
			initializer = true)
	String getName();

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	@Override
	@setter (IKeyword.NAME)
	void setName(String name);

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */

	@Override
	default GamaPoint getLocation() { return getLocation(getScope()); }

	/**
	 * Gets the location.
	 *
	 * @param scope
	 *            the scope
	 * @return the location
	 */
	@getter (
			value = IKeyword.LOCATION,
			initializer = true)
	GamaPoint getLocation(IScope scope);

	/**
	 * Sets the location.
	 *
	 * @param l
	 *            the l
	 * @return the gama point
	 */
	@setter (IKeyword.LOCATION)
	GamaPoint setLocation(IScope scope, final GamaPoint l);

	/**
	 * Sets the location.
	 *
	 * @param l
	 *            the l
	 * @return the gama point
	 */
	@Override
	default GamaPoint setLocation(final GamaPoint l) {
		return setLocation(getScope(), l);
	}

	/**
	 * Gets the getGeometry().
	 *
	 * @return the geometry
	 */

	@getter (IKeyword.SHAPE)
	IShape getGeometry(IScope scope);

	/**
	 * Gets the geometry.
	 *
	 * @return the geometry
	 */
	@Override
	default IShape getGeometry() { return getGeometry(getScope()); }

	/**
	 * Sets the getGeometry().
	 *
	 * @param newGeometry
	 *            the new geometry
	 */
	@setter (IKeyword.SHAPE)
	void setGeometry(IScope scope, final IShape newGeometry);

	/**
	 * Sets the geometry.
	 *
	 * @param newGeometry
	 *            the new geometry
	 */
	@Override
	default void setGeometry(final IShape newGeometry) {
		setGeometry(getScope(), newGeometry);
	}

	/**
	 * Dead.
	 *
	 * @return true, if successful
	 */
	boolean dead();

	/**
	 * Returns the agent which hosts the population of this agent.
	 *
	 * @return
	 */
	@getter (IKeyword.HOST)
	IMacroAgent getHost();

	/**
	 * Gets the top level host.
	 *
	 * @return the top level host
	 */
	default ITopLevelAgent getTopLevelHost() {
		IMacroAgent host = getHost();
		if (host instanceof ITopLevelAgent top) return top;
		if (host == null) return null;
		return host.getTopLevelHost();
	}

	/**
	 * Sets the host.
	 *
	 * @param macroAgent
	 *            the new host
	 */
	@setter (IKeyword.HOST)
	void setHost(final IMacroAgent macroAgent);

	/**
	 * Schedule.
	 *
	 * @param scope
	 *            the scope
	 */
	void schedule(IScope scope);

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	@getter (IKeyword.INDEX)
	int getIndex();

	/**
	 * Gets the population.
	 *
	 * @return the population
	 */
	IPopulation<? extends IAgent> getPopulation();

	/**
	 * A utility method to notify any variable listener of a value change
	 *
	 * @param varName
	 * @param newValue
	 */
	default void notifyVarValueChange(final String varName, final Object newValue) {
		IVariable var = getSpecies().getVar(varName);
		if (var == null) return;
		var.notifyOfValueChange(getScope(), this, null, newValue);
	}

	/**
	 * Gets the macro agents.
	 *
	 * @return the macro agents
	 */
	List<IAgent> getMacroAgents();

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	IModel getModel();

	/**
	 * Checks if is instance of.
	 *
	 * @param skill
	 *            the skill
	 * @param direct
	 *            the direct
	 * @return true, if is instance of
	 */
	boolean isInstanceOf(String skill, boolean direct);

	/**
	 *
	 * Finds the corresponding population of a species from the "viewpoint" of this agent.
	 *
	 * An agent can "see" the following populations: 1. populations of its species' direct micro-species; 2. population
	 * of its species; populations of its peer species; 3. populations of its direct&in-direct macro-species and of
	 * their peers.
	 *
	 * @param microSpecies
	 * @return the corresponding population
	 * @throws GamaRuntimeException
	 */
	IPopulation<? extends IAgent> getPopulationFor(final ISpecies microSpecies);

	/**
	 *
	 * Finds the corresponding population of a species from the "viewpoint" of this agent.
	 *
	 * An agent can "see" the following populations: 1. populations of its species' direct micro-species; 2. population
	 * of its species; populations of its peer species; 3. populations of its direct&in-direct macro-species and of
	 * their peers.
	 *
	 * @param speciesName
	 *            the name of the species
	 * @return the corresponding population
	 * @throws GamaRuntimeException
	 */
	IPopulation<? extends IAgent> getPopulationFor(final String speciesName);

	/**
	 * Update with.
	 *
	 * @param s
	 *            the s
	 * @param sa
	 *            the sa
	 */
	void updateWith(final IScope s, final ISerialisedAgent sa);

	/**
	 * Prim die.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "die",
			doc = @doc ("Kills the agent and disposes of it. Once dead, the agent cannot behave anymore"))
	Object primDie(final IScope scope);

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	default int intValue(final IScope scope) {
		return getIndex();
	}

	/**
	 * Gets the simulation.
	 *
	 * @return the simulation
	 */
	default SimulationAgent getSimulation() { return getPopulation().getHost().getSimulation(); }

	/**
	 * Serialize to json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	@Override
	default JsonValue serializeToJson(final Json json) {
		AgentReference ar = AgentReference.of(this);
		json.addRef(ar.toString(), () -> SerialisedAgent.of(this, false));
		return json.valueOf(ar);
	}

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default IType<?> computeRuntimeType(final IScope scope) {
		return scope.getType(getSpeciesName());
	}

}