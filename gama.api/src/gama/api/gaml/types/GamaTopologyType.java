/*******************************************************************************************************
 *
 * GamaTopologyType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.map.IMap;
import gama.api.types.topology.GamaTopologyFactory;
import gama.api.types.topology.ITopology;

/**
 * Type representing topologies in GAML - structures defining spatial relationships and distance calculations.
 * <p>
 * Topologies are essential for spatial modeling in GAMA, providing the framework for computing distances, finding
 * neighbors, and navigating space. Different topology types support different spatial structures: continuous space,
 * grid-based environments, graph networks, and more.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Distance computation between locations</li>
 * <li>Neighbor detection within radius or k-nearest</li>
 * <li>Path finding and navigation</li>
 * <li>Bounded spatial environments</li>
 * <li>Multiple topology types (continuous, grid, graph, multiple)</li>
 * <li>Wraparound/toroidal space support</li>
 * </ul>
 * 
 * <h2>Topology Types:</h2>
 * <ul>
 * <li><b>Continuous</b> - euclidean space bounded by a geometry</li>
 * <li><b>Grid</b> - discrete cell-based space from a matrix</li>
 * <li><b>Graph</b> - network topology following graph edges</li>
 * <li><b>Multiple</b> - topology over a collection of disconnected spaces</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Continuous topology from environment
 * topology world_topo <- topology(world);
 * 
 * // Grid topology from matrix
 * topology grid_topo <- topology(my_grid);
 * 
 * // Graph topology for road network
 * graph road_graph <- as_edge_graph(road_shapefile);
 * topology road_topo <- topology(road_graph);
 * 
 * // Use topology for distance
 * float dist <- world_topo distance_between(agent1, agent2);
 * 
 * // Find neighbors
 * list<agent> neighbors <- world_topo neighbors_of(self, 50.0);
 * 
 * // Compute path
 * path route <- world_topo path_between(source, target);
 * 
 * // Population topology (from species)
 * topology pop_topo <- topology(people);
 * }
 * </pre>
 * 
 * @author Alexis Drogoul
 * @see GamaType
 * @see ITopology
 * @see gama.api.types.topology.GamaTopologyFactory
 * @since 26 nov. 2011
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.TOPOLOGY,
		id = IType.TOPOLOGY,
		wraps = { ITopology.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.TOPOLOGY },
		doc = @doc ("Represents a topology, obtained from agents or geometries, that can be used to compute distances, neighbours, etc."))
public class GamaTopologyType extends GamaType<ITopology> {

	/**
	 * Constructs a new topology type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaTopologyType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a topology.
	 * <p>
	 * This method supports casting from various source types to create appropriate topology:
	 * <ul>
	 * <li>Topology - returns the topology itself</li>
	 * <li>Spatial graph - returns the graph topology for network navigation</li>
	 * <li>Population - returns the topology of the population</li>
	 * <li>Geometry/Shape - returns continuous topology bounded by the geometry</li>
	 * <li>Matrix - returns grid topology for the matrix cells</li>
	 * <li>Container - returns multiple topology for the container elements</li>
	 * <li>Other - casts to geometry first, then creates topology from it</li>
	 * </ul>
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a topology
	 * @param param
	 *            optional parameter for topology configuration
	 * @param copy
	 *            whether to create a copy if obj is already a topology
	 * @return the topology representation of the object
	 * @throws GamaRuntimeException
	 *             if the casting operation fails
	 */
	@Override
	@doc (
			value = "casting of the operand to a topology.",
			usages = { @usage ("if the operand is a topology, returns the topology itself;"),
					@usage ("if the operand is a spatial graph, returns the graph topology associated;"),
					@usage ("if the operand is a population, returns the topology of the population;"),
					@usage ("if the operand is a shape or a geometry, returns the continuous topology bounded by the geometry;"),
					@usage ("if the operand is a matrix, returns the grid topology associated"),
					@usage ("if the operand is another kind of container, returns the multiple topology associated to the container"),
					@usage ("otherwise, casts the operand to a geometry and build a topology from it.") },
			examples = { @example (
					value = "topology(0)",
					equals = "nil",
					isExecutable = true),
					@example (
							value = "topology(a_graph)	--: Multiple topology in POLYGON ((24.712119771887785 7.867357373616512, 24.712119771887785 61.283226839310565, 82.4013676510046  7.867357373616512)) "
									+ "at location[53.556743711446195;34.57529210646354]",
							isExecutable = false) },
			see = { "geometry" })
	public ITopology cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaTopologyFactory.castToTopology(scope, obj, copy);
	}

	/**
	 * Returns the default value for topology type.
	 * <p>
	 * The default topology is null, as there is no meaningful default topology.
	 * </p>
	 * 
	 * @return null
	 */
	@Override
	public ITopology getDefault() { return null; }

	/**
	 * Returns the content type of topologies.
	 * <p>
	 * Topologies contain geometries as their fundamental elements (locations, shapes, etc.).
	 * </p>
	 * 
	 * @return the geometry type
	 */
	@Override
	public IType<?> getContentType() { return Types.GEOMETRY; }

	/**
	 * Indicates whether topologies can be cast to constant values.
	 * <p>
	 * Topologies cannot be constant as they may reference dynamic spatial structures.
	 * </p>
	 * 
	 * @return false, topologies are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Deserializes a topology from a JSON representation.
	 * <p>
	 * The JSON map should contain an "environment" field with the geometry defining the topology bounds.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing topology data
	 * @return the deserialized topology
	 */
	@Override
	public ITopology deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaTopologyFactory.createFrom(scope,
				GamaShapeFactory.castToShape(scope, map2.get("environment"), false));
	}

}
