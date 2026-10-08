/*******************************************************************************************************
 *
 * IPathFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.topology.ITopology;

/**
 * Interface for path factory implementations.
 * 
 * <p>
 * This interface defines the contract for creating {@link IPath} instances from various
 * inputs. Implementations provide the actual path construction logic and are registered
 * with {@link GamaPathFactory} to make them available throughout GAMA.
 * </p>
 * 
 * <h2>Path Creation Strategies</h2>
 * 
 * <h3>Graph-Based Paths</h3>
 * <ul>
 * <li><b>From Vertices</b>: Create paths by specifying an ordered list of vertices.
 *     The edges are inferred from consecutive vertices in the graph.</li>
 * <li><b>From Edges</b>: Create paths by specifying start, target, and an ordered list
 *     of edges. The edges must form a valid path from start to target.</li>
 * <li><b>Edge Modification</b>: Optionally modify edges to ensure path validity
 *     (e.g., reversing edges in undirected graphs).</li>
 * </ul>
 * 
 * <h3>Topology-Based Paths (Spatial)</h3>
 * <ul>
 * <li><b>From Shapes</b>: Create spatial paths where vertices and edges are geometric
 *     shapes with locations and geometries.</li>
 * <li><b>From Shape List</b>: Create paths from lists of shape objects, interpreting
 *     them as either edges or nodes based on a flag.</li>
 * <li><b>With Weight</b>: Create paths with explicit weight values for optimization.</li>
 * </ul>
 * 
 * <h3>Generic Creation</h3>
 * <ul>
 * <li><b>From Objects</b>: Flexible creation from various source types with optional
 *     parameters and copy semantics.</li>
 * <li>Used by GAML's type casting system to convert objects to paths.</li>
 * </ul>
 * 
 * <h2>Implementation Requirements</h2>
 * <p>
 * Implementations must:
 * <ul>
 * <li>Create valid paths that reference existing graph elements</li>
 * <li>Compute path metrics (weight, distance, length) correctly</li>
 * <li>Handle both graph-based and topology-based paths</li>
 * <li>Support spatial paths with geometric properties when applicable</li>
 * <li>Validate path connectivity (edges connect consecutive vertices)</li>
 * </ul>
 * </p>
 * 
 * <h2>Path Validity</h2>
 * <p>
 * Created paths should satisfy:
 * <ul>
 * <li>All vertices exist in the graph or topology</li>
 * <li>All edges exist in the graph or topology</li>
 * <li>Edges connect consecutive vertices in the vertex list</li>
 * <li>Start and target vertices match the first and last vertices</li>
 * </ul>
 * </p>
 * 
 * @see GamaPathFactory
 * @see IPath
 * @see IGraph
 * @see ITopology
 * @author drogoul
 */
public interface IPathFactory {

	/**
	 * New instance.
	 *
	 * @param <V>
	 *            the value type
	 * @param <E>
	 *            the element type
	 * @param g
	 *            the g
	 * @param nodes
	 *            the nodes
	 * @return the gama path
	 */
	<V, E> IPath<V, E, IGraph<V, E>> createFrom(IGraph<V, E> g, IList<? extends V> nodes);

	/**
	 * New instance.
	 *
	 * @param <V>
	 *            the value type
	 * @param <E>
	 *            the element type
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @return the gama path
	 */
	<V, E> IPath<V, E, IGraph<V, E>> createFrom(IGraph<V, E> g, V start, V target, IList<E> edges);

	/**
	 * New instance.
	 *
	 * @param <V>
	 *            the value type
	 * @param <E>
	 *            the element type
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @param modify_edges
	 *            the modify edges
	 * @return the gama path
	 */
	<V, E> IPath<V, E, IGraph<V, E>> createFrom(IGraph<V, E> g, V start, V target, IList<E> edges,
			boolean modify_edges);

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param nodes
	 *            the nodes
	 * @param weight
	 *            the weight
	 * @return the gama spatial path
	 */
	// With Topology
	IPath createFrom(IScope scope, ITopology g, IList<? extends IShape> nodes, double weight);

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @return the gama spatial path
	 */
	IPath createFrom(IScope scope, ITopology g, IShape start, IShape target, IList<IShape> edges);

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param edges
	 *            the edges
	 * @param modify_edges
	 *            the modify edges
	 * @return the gama spatial path
	 */
	IPath createFrom(IScope scope, ITopology g, IShape start, IShape target, IList<IShape> edges, boolean modify_edges);

	/**
	 * New instance.
	 *
	 * @param scope
	 *            the scope
	 * @param edgesNodes
	 *            the edges nodes
	 * @param isEdges
	 *            the is edges
	 * @return the i path
	 */
	IPath createFrom(IScope scope, IList<? extends IShape> edgesNodes, boolean isEdges);

	/**
	 * Creates a new IPath object.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the i path
	 */
	IPath createFrom(final IScope scope, final Object obj, final Object param, final boolean copy);

}