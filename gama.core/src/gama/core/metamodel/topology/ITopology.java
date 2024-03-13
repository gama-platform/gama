/*******************************************************************************************************
 *
 * ITopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology;

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.IValue;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.continuous.RootTopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.core.util.path.GamaSpatialPath;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 4 juil. 2011
 *
 * @todo Description
 *
 */
@vars ({ @variable (
		name = IKeyword.ENVIRONMENT,
		type = IType.GEOMETRY,
		doc = { @doc ("Returns the environment of this topology, either an agent or a geometry, which defines its boundaries") }),
		@variable (
				name = IKeyword.PLACES,
				type = IType.CONTAINER,
				of = IType.GEOMETRY,
				doc = { @doc ("Returns the list of discrete places that compose this topology (e.g. the list of cells for a grid topology). The continuous topologies will return a singleton list with their environment") }),
		// Could be replaced by "geometries"

})
public interface ITopology extends IValue {

	/**
	 * The Enum SpatialRelation.
	 */
	enum SpatialRelation {

		/** The overlap. */
		OVERLAP,

		/** The cover. */
		COVER,

		/** The inside. */
		INSIDE,

		/** The touch. */
		TOUCH,

		/** The cross. */
		CROSS,

		/** The partially overlap. */
		PARTIALLY_OVERLAP
	}

	/**
	 * Gets the spatial index.
	 *
	 * @return the spatial index
	 */
	ISpatialIndex getSpatialIndex();

	/**
	 * Initialize.
	 *
	 * @param scope
	 *            the scope
	 * @param pop
	 *            the pop
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void initialize(IScope scope, IPopulation<? extends IAgent> pop) throws GamaRuntimeException;

	/**
	 * Update agent.
	 *
	 * @param previous
	 *            the previous
	 * @param agent
	 *            the agent
	 */
	void updateAgent(Envelope3D previous, IAgent agent);

	/**
	 * Removes the agent.
	 *
	 * @param agent
	 *            the agent
	 */
	void removeAgent(final IAgent agent);

	/**
	 * List toroidal geometries.
	 *
	 * @param geom
	 *            the geom
	 * @return the list
	 */
	List<Geometry> listToroidalGeometries(final Geometry geom);

	/**
	 * Gets the agent closest to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param filter
	 *            the filter
	 * @param number
	 *            the number
	 * @return the agent closest to
	 */
	Collection<IAgent> getAgentClosestTo(IScope scope, final IShape source, IAgentFilter filter, int number);

	/**
	 * Gets the agent closest to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param filter
	 *            the filter
	 * @return the agent closest to
	 */
	IAgent getAgentClosestTo(IScope scope, final IShape source, IAgentFilter filter);

	/**
	 * Gets the agent farthest to.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param filter
	 *            the filter
	 * @return the agent farthest to
	 */
	IAgent getAgentFarthestTo(IScope scope, final IShape source, IAgentFilter filter);

	/**
	 * Gets the neighbors of.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param distance
	 *            the distance
	 * @param filter
	 *            the filter
	 * @return the neighbors of
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	Collection<IAgent> getNeighborsOf(IScope scope, final IShape source, final Double distance, IAgentFilter filter)
			throws GamaRuntimeException;

	/**
	 * Gets the agents in.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param f
	 *            the f
	 * @param relation
	 *            the relation
	 * @return the agents in
	 */
	Collection<IAgent> getAgentsIn(IScope scope, final IShape source, final IAgentFilter f, SpatialRelation relation);

	/**
	 * Checks if is torus.
	 *
	 * @return true, if is torus
	 */
	boolean isTorus();

	/**
	 * Checks if is continuous.
	 *
	 * @return true, if is continuous
	 */
	boolean isContinuous();

	/**
	 * @throws GamaRuntimeException
	 *             Distance between two geometries in this topology.
	 *
	 * @param source
	 *            the source geometry (cannot be null)
	 * @param target
	 *            the target (cannot be null)
	 * @return a double representing the distance between the two geometries, or Double.MAX_VALUE if either one of them
	 *         is not reachable from this topology
	 */
	Double distanceBetween(IScope scope, final IShape source, final IShape target);

	/**
	 * Distance between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the double
	 */
	Double distanceBetween(IScope scope, final GamaPoint source, final GamaPoint target);

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the gama spatial path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	GamaSpatialPath pathBetween(IScope scope, final IShape source, final IShape target) throws GamaRuntimeException;

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the gama spatial path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	GamaSpatialPath pathBetween(IScope scope, final GamaPoint source, final GamaPoint target)
			throws GamaRuntimeException;

	/**
	 * Return the location corresponding to a displacement from source, with an angle (in degrees) given by direction
	 * and a given distance. The returned point is a valid local location of this topology or null.
	 *
	 * @param source
	 *            the point from which the destination is computed
	 * @param direction
	 *            an angle in degrees (between 0 and 360 -- other values will be normalized
	 * @param distance
	 *            the distance that should separate the actual location and the destination
	 * @param nullIfOutside
	 *            tells wether to return the destination point or null if the destination is outside the topology
	 * @return a point or null if no random locations are available
	 */
	GamaPoint getDestination(IScope scope, final GamaPoint source, final double direction, final double distance,
			boolean nullIfOutside);

	/**
	 * Return the location corresponding to a displacement from source, with an angle (in degrees) given by direction
	 * and a given distance. The returned point is a valid local location of this topology or null.
	 *
	 * @param source
	 *            the point from which the destination is computed
	 * @param direction
	 *            an angle in degrees (between 0 and 360 -- other values will be normalized
	 * @param distance
	 *            the distance that should separate the actual location and the destination
	 * @param nullIfOutside
	 *            tells wether to return the destination point or null if the destination is outside the topology
	 * @return a point or null if no random locations are available
	 */
	GamaPoint getDestination3D(IScope scope, final GamaPoint source, final double heading, final double pitch,
			final double distance, boolean nullIfOutside);

	/**
	 * Return a random location inside the bounds of the environment's shape. The returned point is a valid local
	 * location of this topology.
	 *
	 * @return a point, or null if no random locations are available
	 */
	GamaPoint getRandomLocation(IScope scope);

	/**
	 * Return the collection of places (IGeometry) defined by this topology. For continuous topologies, it is a GamaList
	 * with their environment. For discrete topologies, it can be any of the container supporting the inclusion of
	 * geometries (GamaList, GamaSpatialGraph, GamaMap, GamaSpatialMatrix)
	 *
	 * @return an instance of IGamaContainer, which geometries can be iterated.
	 */
	@getter (IKeyword.PLACES)
	IContainer<?, IShape> getPlaces();

	/**
	 * Return the environment of this topology (i.e. the IGeometry that defines its boundaries).
	 *
	 * @return an instance of IGeometry.
	 */
	@getter (IKeyword.ENVIRONMENT)
	IShape getEnvironment();

	/**
	 * Normalizes a location so that the returned location is inside the bounds of the topology. The returned point is a
	 * valid local location of this topology.
	 *
	 * @param p
	 *            the location to normalize
	 * @param nullIfOutside
	 *            tells whether to return null or to coerce p if p is outside the bounds of the topology
	 * @return a valid point or null if nullIfOutside is true and the point is outside
	 */
	GamaPoint normalizeLocation(IScope scope, final GamaPoint p, boolean nullIfOutside);

	/**
	 * @throws GamaRuntimeException
	 *             Called by a population to tell this topology that the shape of its host has changed. If the
	 *             environment of the topology depends on the shape of the host, the topology can choose to adapt in
	 *             consequence.
	 *
	 * @param pop
	 *            the population to which this topology is attached.
	 */
	// public abstract void shapeChanged(IPopulation pop);

	double getWidth();

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	double getHeight();

	/**
	 * Dispose.
	 */
	void dispose();

	/**
	 * Checks if is valid location.
	 *
	 * @param scope
	 *            the scope
	 * @param p
	 *            the p
	 * @return true, if is valid location
	 */
	boolean isValidLocation(IScope scope, GamaPoint p);

	/**
	 * Checks if is valid geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return true, if is valid geometry
	 */
	boolean isValidGeometry(IScope scope, IShape g);

	/**
	 * @param scope
	 *            TODO
	 * @throws GamaRuntimeException
	 * @param source
	 * @param target
	 * @return the direction or null if one these two geometries are invalid in this topology
	 */
	Double directionInDegreesTo(IScope scope, IShape source, IShape target);

	/**
	 * Kpaths between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param k
	 *            the k
	 * @return the i list
	 */
	IList<GamaSpatialPath> kPathsBetween(IScope scope, IShape source, IShape target, int k);

	/**
	 * Kpaths between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param k
	 *            the k
	 * @return the i list
	 */
	IList<GamaSpatialPath> kPathsBetween(IScope scope, GamaPoint source, GamaPoint target, int k);

	/**
	 * Sets the root.
	 *
	 * @param scope
	 *            the scope
	 * @param rt
	 *            the rt
	 */
	void setRoot(IScope scope, RootTopology rt);

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	default int intValue(final IScope scope) {
		return this.getPlaces().intValue(scope);
	}

	/**
	 * Float value.
	 *
	 * @param scope
	 *            the scope
	 * @return the double
	 */
	@Override
	default double floatValue(final IScope scope) {
		return this.getEnvironment().floatValue(scope);
	}

	/**
	 * Serialize to json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json value
	 * @date 29 oct. 2023
	 */
	@Override
	default JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), IKeyword.ENVIRONMENT, getEnvironment());
	}

}