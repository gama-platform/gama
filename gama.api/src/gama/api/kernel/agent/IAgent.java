/*******************************************************************************************************
 *
 * IAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import org.locationtech.jts.geom.Geometry;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
import gama.api.compilation.IVarAndActionSupport;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.IType;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.IStepable;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.IScoped;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.ITopology;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.INamed;

/**
 * Core interface for all agents in GAMA simulations.
 * 
 * <p>IAgent is the fundamental abstraction in GAMA's agent-based modeling framework. It represents
 * an individual entity with attributes, behaviors, location, and lifecycle. All agents in GAMA,
 * including simulations, experiments, and the platform agent, implement this interface.</p>
 * 
 * <h2>Agent Characteristics</h2>
 * 
 * <p>Agents have the following key characteristics:</p>
 * <ul>
 *   <li><b>Identity:</b> Unique index within their population</li>
 *   <li><b>Name:</b> String identifier (not necessarily unique)</li>
 *   <li><b>Species:</b> Type definition with shared behaviors</li>
 *   <li><b>Attributes:</b> Named variables with values</li>
 *   <li><b>Location:</b> Position in space</li>
 *   <li><b>Shape:</b> Geometric representation</li>
 *   <li><b>Host:</b> Container agent (for hierarchical models)</li>
 *   <li><b>Population:</b> Collection of agents of the same species</li>
 * </ul>
 * 
 * <h2>Agent Lifecycle</h2>
 * 
 * <ol>
 *   <li><b>Creation:</b> Agent constructed via {@link IAgentConstructor}</li>
 *   <li><b>Initialization:</b> {@link #init(IScope)} called to set initial state</li>
 *   <li><b>Scheduling:</b> {@link #schedule(IScope)} adds agent to scheduler</li>
 *   <li><b>Stepping:</b> {@link #step(IScope)} called each cycle</li>
 *   <li><b>Death:</b> {@link #primDie(IScope)} removes agent from simulation</li>
 *   <li><b>Disposal:</b> Resources released</li>
 * </ol>
 * 
 * <h2>Built-in Attributes</h2>
 * 
 * <p>All agents have these attributes by default:</p>
 * <ul>
 *   <li><b>name:</b> Agent's name (string)</li>
 *   <li><b>index:</b> Unique identifier within population (int, read-only)</li>
 *   <li><b>location:</b> Position in space (point)</li>
 *   <li><b>shape:</b> Geometric representation (geometry)</li>
 *   <li><b>host:</b> Parent agent containing this agent's population</li>
 *   <li><b>peers:</b> Other agents in the same population</li>
 * </ul>
 * 
 * <h2>Hierarchical Structure</h2>
 * 
 * <p>Agents can be organized hierarchically:</p>
 * <pre>
 * World/Simulation (top-level)
 *   ├── City agents
 *   │   └── Building agents (micro-species)
 *   └── Person agents
 * </pre>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Accessing Agent Attributes in GAML</h3>
 * <pre>{@code
 * species Person {
 *     int age <- 20;
 *     point target;
 *     
 *     reflex info {
 *         write "Agent " + name + " (index: " + index + ")";
 *         write "Location: " + location;
 *         write "Age: " + age;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Accessing Agent Attributes in Java</h3>
 * <pre>{@code
 * IAgent agent = ...;
 * 
 * // Get attribute values
 * Object age = agent.getAttribute("age");
 * IPoint location = agent.getLocation(scope);
 * IShape shape = agent.getGeometry(scope);
 * String name = agent.getName();
 * 
 * // Set attribute values
 * agent.setAttribute("age", 25);
 * agent.setLocation(scope, new GamaPoint(10, 20));
 * 
 * // Get species and population
 * ISpecies species = agent.getSpecies();
 * IPopulation<? extends IAgent> pop = agent.getPopulation();
 * }</pre>
 * 
 * <h3>Agent Hierarchy</h3>
 * <pre>{@code
 * // In GAML
 * species City {
 *     species District {
 *         // District agents live inside City agents
 *     }
 * }
 * 
 * // In Java
 * IAgent city = ...;
 * IMacroAgent macroCity = (IMacroAgent) city;
 * IPopulation<? extends IAgent> districts = 
 *     macroCity.getMicroPopulation("District");
 * }</pre>
 * 
 * <h3>Agent Lifecycle</h3>
 * <pre>{@code
 * // Create agents
 * IPopulation<IAgent> population = ...;
 * List<IAgent> newAgents = population.createAgents(scope, 10);
 * 
 * // Initialize agent
 * IAgent agent = newAgents.get(0);
 * agent.init(scope);
 * 
 * // Schedule for execution
 * agent.schedule(scope);
 * 
 * // Step agent
 * agent.step(scope);
 * 
 * // Check if dead
 * if (!agent.dead()) {
 *     // Agent is still alive
 * }
 * 
 * // Kill agent
 * agent.primDie(scope);
 * }</pre>
 * 
 * <h3>Geometric Operations</h3>
 * <pre>{@code
 * IAgent agent1 = ...;
 * IAgent agent2 = ...;
 * 
 * // Distance calculation
 * double dist = agent1.euclidianDistanceTo(agent2);
 * 
 * // Spatial relationships
 * boolean intersects = agent1.intersects(agent2);
 * boolean covers = agent1.covers(agent2);
 * 
 * // Geometric properties
 * double area = agent1.getArea();
 * double perimeter = agent1.getPerimeter();
 * IPoint centroid = agent1.getCentroid();
 * }</pre>
 * 
 * @see IMacroAgent for agents containing populations
 * @see ISpecies for agent type definitions
 * @see IPopulation for agent collections
 * @see IShape for geometric operations
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
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
@doc ("""
		The species hierarchy derives from a single built-in species, which is 'agent'. All its components (attributes, actions) will then be inherited by all direct \
		or indirect children species (including 'model' and 'experiment' except species that explicitly set 'use_minimal_agents' facet to 'true', which inherit from
		 a stripped-down version of 'agent'.\s""")
public interface IAgent extends IShape, INamed, Comparable<IAgent>, IStepable, IContainer.ToGet<String, Object>,
		IVarAndActionSupport, IScoped {

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
	void setPeers(IList<IAgent> peers);

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
	default IPoint getLocation() { return getLocation(getScope()); }

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
	IPoint getLocation(IScope scope);

	/**
	 * Sets the location.
	 *
	 * @param l
	 *            the l
	 * @return the gama point
	 */
	@setter (IKeyword.LOCATION)
	IPoint setLocation(IScope scope, final IPoint l);

	/**
	 * Sets the location.
	 *
	 * @param l
	 *            the l
	 * @return the gama point
	 */
	@Override
	default IPoint setLocation(final IPoint l) {
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
	 * Gets the species name.
	 *
	 * @return the species name
	 */
	String getSpeciesName();

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	ISpecies getSpecies();

	/**
	 * Gets the population.
	 *
	 * @return the population
	 */
	IPopulation<? extends IAgent> getPopulation();

	/**
	 * Checks if is instance of.
	 *
	 * @param s
	 *            the s
	 * @param direct
	 *            the direct
	 * @return true, if is instance of
	 */
	boolean isInstanceOf(final ISpecies s, boolean direct);

	/**
	 * Gets the direct var value.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the direct var value
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException;

	/**
	 * Sets the direct var value.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param v
	 *            the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException;

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
	 * Gets the model.
	 *
	 * @return the model
	 */
	IModelSpecies getModel();

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

	/***
	 * All the methods of IShape are delegated by default to getGeometry()
	 */

	/**
	 * Method getArea(). Simply delegates to the geometry
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getArea()
	 */
	@Override
	default Double getArea() { return getGeometry().getArea(); }

	/**
	 * Method getVolume(). Simply delegates to the geometry
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getVolume()
	 */
	@Override
	default Double getVolume() { return getGeometry().getVolume(); }

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getPerimeter()
	 */
	@Override
	default double getPerimeter() { return getGeometry().getPerimeter(); }

	/**
	 * Method getHoles()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getHoles()
	 */
	@Override
	default IList<IShape> getHoles() { return getGeometry().getHoles(); }

	/**
	 * Method getCentroid()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getCentroid()
	 */
	@Override
	default IPoint getCentroid() { return getGeometry().getCentroid(); }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getExteriorRing()
	 */
	@Override
	default IShape getExteriorRing(final IScope scope) {
		return getGeometry().getExteriorRing(scope);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getWidth()
	 */
	@Override
	default Double getWidth() { return getGeometry().getWidth(); }

	/**
	 * Method getHeight()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getDepth()
	 */
	@Override
	default Double getHeight() { return getGeometry().getHeight(); }

	/**
	 * Method getDepth()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getDepth()
	 */
	@Override
	default Double getDepth() { return getGeometry().getDepth(); }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getGeometricEnvelope()
	 */
	@Override
	default IShape getGeometricEnvelope() { return getGeometry().getGeometricEnvelope(); }

	/**
	 * Gets the geometries.
	 *
	 * @return the geometries
	 */
	@Override
	default IList<? extends IShape> getGeometries() { return getGeometry().getGeometries(); }

	/**
	 * Method isMultiple()
	 *
	 * @see gama.api.types.geometry.core.metamodel.shape.IShape#isMultiple()
	 */
	@Override
	default boolean isMultiple() { return getGeometry().isMultiple(); }

	/**
	 * Checks if is point.
	 *
	 * @return true, if is point
	 */
	@Override
	default boolean isPoint() { return getGeometry().isPoint(); }

	/**
	 * Checks if is line.
	 *
	 * @return true, if is line
	 */
	@Override
	default boolean isLine() { return getGeometry().isLine(); }

	/**
	 * Gets the inner geometry.
	 *
	 * @return the inner geometry
	 */
	@Override
	default Geometry getInnerGeometry() { return getGeometry().getInnerGeometry(); }

	/**
	 * Returns the envelope of the geometry of the agent, or null if the geometry has not yet been defined
	 *
	 */
	@Override
	default IEnvelope getEnvelope() {
		final IShape g = getGeometry();
		return g == null ? null : g.getEnvelope();
	}

	/**
	 * Covers.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean covers(final IShape g) {
		return getGeometry().covers(g);
	}

	/**
	 * Euclidian distance to.
	 *
	 * @param g
	 *            the g
	 * @return the double
	 */
	@Override
	default double euclidianDistanceTo(final IShape g) {
		return getGeometry().euclidianDistanceTo(g);
	}

	/**
	 * Euclidian distance to.
	 *
	 * @param g
	 *            the g
	 * @return the double
	 */
	@Override
	default double euclidianDistanceTo(final IPoint g) {
		return getGeometry().euclidianDistanceTo(g);
	}

	/**
	 * Intersects.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean intersects(final IShape g) {
		return getGeometry().intersects(g);
	}

	/**
	 * Partially overlaps.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean partiallyOverlaps(final IShape g) {
		return getGeometry().partiallyOverlaps(g);
	}

	/**
	 * Touches.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean touches(final IShape g) {
		return getGeometry().touches(g);
	}

	/**
	 * Crosses.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean crosses(final IShape g) {
		return getGeometry().crosses(g);
	}

	/**
	 * @see gama.core.common.interfaces.IGeometry#setInnerGeometry(org.locationtech.jts.geom.Geometry)
	 */
	@Override
	default void setInnerGeometry(final Geometry geom) {
		getGeometry().setInnerGeometry(geom);
	}

	/**
	 * Sets the depth.
	 *
	 * @param depth
	 *            the new depth
	 */
	@Override
	default void setDepth(final double depth) {
		if (getGeometry() == null) return;
		getGeometry().setDepth(depth);
	}

	/**
	 * Sets the geometrical type.
	 *
	 * @param t
	 *            the new geometrical type
	 */
	@Override
	default void setGeometricalType(final Type t) {
		getGeometry().setGeometricalType(t);
	}

	/**
	 * Prim die.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

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
	default ISimulationAgent getSimulation() { return getPopulation().getHost().getSimulation(); }

}