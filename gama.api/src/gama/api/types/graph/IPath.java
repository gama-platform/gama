/*******************************************************************************************************
 *
 * IPath.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.misc.IValue;
import gama.api.types.topology.ITopology;

/**
 * Interface representing a path through a graph.
 * 
 * <p>
 * An IPath represents a sequence of vertices connected by edges in a graph. It encapsulates
 * both the structural information (vertices and edges that make up the path) and metadata
 * (weight, distance, geometry) about the path.
 * </p>
 * 
 * <h2>Path Components</h2>
 * <p>
 * A path consists of:
 * <ul>
 * <li><b>Vertices</b>: An ordered list of vertices from source to target</li>
 * <li><b>Edges</b>: The edges connecting consecutive vertices</li>
 * <li><b>Source</b>: The starting vertex of the path</li>
 * <li><b>Target</b>: The ending vertex of the path</li>
 * <li><b>Graph</b>: Reference to the graph containing the path</li>
 * </ul>
 * </p>
 * 
 * <h2>Path Metrics</h2>
 * <ul>
 * <li><b>Weight</b>: Sum of edge/vertex weights along the path (used by pathfinding algorithms)</li>
 * <li><b>Distance</b>: Geometric/spatial length of the path (for spatial graphs)</li>
 * <li><b>Length</b>: Number of edges in the path</li>
 * </ul>
 * 
 * <h2>Spatial Properties</h2>
 * <p>
 * For spatial graphs, paths have geometric representations:
 * <ul>
 * <li><b>Shape</b>: The geometric line formed by the path</li>
 * <li><b>Segments</b>: Individual geometric segments (edge geometries)</li>
 * <li><b>Topology</b>: The spatial topology for distance computations</li>
 * </ul>
 * </p>
 * 
 * <h2>Agent Tracking</h2>
 * <p>
 * Paths can track agents moving along them, storing their current position (vertex index
 * and segment index). This is useful for simulating movement along network paths.
 * </p>
 * 
 * <h2>GAML Variables</h2>
 * <p>
 * This interface exposes several attributes accessible from GAML:
 * <ul>
 * <li><code>source</code>: Starting vertex</li>
 * <li><code>target</code>: Ending vertex</li>
 * <li><code>graph</code>: The underlying graph</li>
 * <li><code>vertices</code>: List of vertices in order</li>
 * <li><code>edges</code>: List of edges in order</li>
 * <li><code>shape</code>: Geometric representation</li>
 * <li><code>segments</code>: List of segment geometries</li>
 * <li><code>weight</code>: Total path weight</li>
 * <li><code>distance</code>: Total path distance</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * IGraph graph = ...;
 * IPath path = graph.computeShortestPathBetween(scope, source, target);
 * 
 * // Access path properties
 * IList vertices = path.getVertexList();
 * IList edges = path.getEdgeList();
 * double weight = path.getWeight();
 * double distance = path.getDistance(scope);
 * 
 * // For spatial paths
 * IShape geometry = path.getGeometry();
 * IList segments = path.getEdgeGeometry();
 * </pre>
 * 
 * @param <V> the type of vertices in the path
 * @param <E> the type of edges in the path
 * @param <G> the type of the graph containing the path
 * 
 * @see IGraph
 * @see GamaPathFactory
 * @see IPathComputer
 * @author drogoul
 * @since 14 déc. 2011
 */
@vars ({ @variable (
		name = IKeyword.TARGET,
		type = IType.NONE,
		doc = @doc ("The target (i.e. last element) of this path")),
		@variable (
				name = IKeyword.SOURCE,
				type = IType.NONE,
				doc = @doc ("The source (i.e. first element) of this path")),
		@variable (
				name = IKeyword.GRAPH,
				type = IType.GRAPH,
				doc = @doc ("The graph this path refers to")),
		@variable (
				name = IKeyword.SHAPE,
				type = IType.GEOMETRY,
				doc = @doc ("The shape obtained by all the points of this path")),
		@variable (
				name = IKeyword.SEGMENTS,
				type = IType.LIST,
				of = IType.GEOMETRY,
				doc = { @doc ("Returns the list of segments that compose this path") }),
		@variable (
				name = "distance",
				type = IType.FLOAT,
				doc = { @doc ("Returns the total lenght of all the segments that compose this path") }),
		@variable (
				name = "weight",
				type = IType.FLOAT,
				doc = @doc ("The addition of all the weights of the vertices that compose this path, with respect to the graph they belong to")),
		@variable (
				name = "edges",
				type = IType.LIST,
				of = IType.GEOMETRY,
				doc = @doc ("The list of edges of the underlying graph that compose this path")),
		@variable (
				name = "vertices",
				type = IType.LIST,
				doc = @doc ("The list of vertices of the underlying graph that compose this path"))
		// @var(name = IKeyword.AGENTS, type = IType.LIST, of = IType.AGENT),
		// Could be replaced by "geometries"
		/*
		 * Normally not necessary as it is inherited from GamaGeometry @var(name = GamaPath.POINTS, type = IType.LIST,
		 * of = IType.POINT)
		 */
})
public interface IPath<V, E, G extends IGraph<V, E>> extends IValue {// extends IShape {

	/**
	 * Gets the start vertex.
	 *
	 * @return the start vertex
	 */
	@getter (IKeyword.SOURCE)
	V getStartVertex();

	/**
	 * Gets the end vertex.
	 *
	 * @return the end vertex
	 */
	@getter (IKeyword.TARGET)
	V getEndVertex();

	/**
	 * Gets the graph.
	 *
	 * @return the graph
	 */
	@getter (IKeyword.GRAPH)
	G getGraph();

	/**
	 * Gets the edge geometry.
	 *
	 * @return the edge geometry
	 */
	@getter (IKeyword.SEGMENTS)
	IList<IShape> getEdgeGeometry();

	/**
	 * Gets the vertex list.
	 *
	 * @return the vertex list
	 */
	@getter ("vertices")
	IList<V> getVertexList();

	/**
	 * Gets the edge list.
	 *
	 * @return the edge list
	 */
	@getter ("edges")
	IList<E> getEdgeList();

	/**
	 * Gets the geometry.
	 *
	 * @return the geometry
	 */
	@getter ("shape")
	IShape getGeometry();

	// @getter(IKeyword.AGENTS)
	// public abstract List<IShape> getAgentList();

	/**
	 * Gets the weight.
	 *
	 * @return the weight
	 */
	@getter ("weight")
	double getWeight();

	/**
	 * Gets the weight.
	 *
	 * @param line
	 *            the line
	 * @return the weight
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	double getWeight(final IShape line) throws GamaRuntimeException;

	/**
	 * Accept visitor.
	 *
	 * @param agent
	 *            the agent
	 */
	void acceptVisitor(final IAgent agent);

	/**
	 * Forget visitor.
	 *
	 * @param agent
	 *            the agent
	 */
	void forgetVisitor(final IAgent agent);

	/**
	 * Index of.
	 *
	 * @param a
	 *            the a
	 * @return the int
	 */
	int indexOf(final IAgent a);

	/**
	 * Index segment of.
	 *
	 * @param a
	 *            the a
	 * @return the int
	 */
	int indexSegmentOf(final IAgent a);

	/**
	 * Checks if is visitor.
	 *
	 * @param a
	 *            the a
	 * @return true, if is visitor
	 */
	boolean isVisitor(final IAgent a);

	/**
	 * Sets the index of.
	 *
	 * @param a
	 *            the a
	 * @param index
	 *            the index
	 */
	void setIndexOf(final IAgent a, final int index);

	/**
	 * Sets the index segement of.
	 *
	 * @param a
	 *            the a
	 * @param indexSegement
	 *            the index segement
	 */
	void setIndexSegementOf(final IAgent a, final int indexSegement);

	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	int getLength();

	/**
	 * Gets the distance.
	 *
	 * @param scope
	 *            the scope
	 * @return the distance
	 */
	@getter ("distance")
	double getDistance(IScope scope);

	/**
	 * Gets the topology.
	 *
	 * @param scope
	 *            the scope
	 * @return the topology
	 */
	ITopology getTopology(IScope scope);

	/**
	 * Sets the real objects.
	 *
	 * @param realObjects
	 *            the real objects
	 */
	void setRealObjects(final IMap<IShape, IShape> realObjects);

	/**
	 * Gets the real object.
	 *
	 * @param obj
	 *            the obj
	 * @return the real object
	 */
	IShape getRealObject(final Object obj);

	/**
	 * Sets the source.
	 *
	 * @param source
	 *            the new source
	 */
	void setSource(V source);

	/**
	 * Sets the target.
	 *
	 * @param target
	 *            the new target
	 */
	void setTarget(V target);

	/**
	 * Gets the graph version.
	 *
	 * @return the graph version
	 */
	int getGraphVersion();

	/**
	 * Sets the graph.
	 *
	 * @param graph
	 *            the new graph
	 */
	void setGraph(G graph);

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	default int intValue(final IScope scope) {
		return this.getLength();
	}

	/**
	 * @param size
	 */
	void setWeight(double weight);

}