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