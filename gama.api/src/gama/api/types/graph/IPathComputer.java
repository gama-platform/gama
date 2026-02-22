/*******************************************************************************************************
 *
 * IPathComputer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import java.util.Map;

import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.util.Pair;

import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;

/**
 * Interface for computing paths in a graph using various algorithms.
 * 
 * <p>
 * IPathComputer provides methods for finding shortest paths and k-shortest paths in graphs.
 * It supports multiple pathfinding algorithms and can cache computed paths for efficiency.
 * Each graph has an associated path computer that handles all pathfinding operations.
 * </p>
 * 
 * <h2>Shortest Path Algorithms</h2>
 * <p>
 * The following algorithms are available via {@link ShortestPathAlgorithmEnum}:
 * <ul>
 * <li><b>Dijkstra</b>: Classic algorithm for non-negative weights, good general-purpose choice</li>
 * <li><b>A*</b>: Heuristic-guided search, faster when target is known and heuristic is good</li>
 * <li><b>BellmanFord</b>: Handles negative weights, slower than Dijkstra</li>
 * <li><b>FloydWarshall</b>: All-pairs shortest paths, good for dense graphs</li>
 * <li><b>BidirectionalDijkstra</b>: Searches from both source and target simultaneously</li>
 * <li><b>NBAStar</b>: New Bidirectional A* algorithm</li>
 * <li><b>NBAStarApprox</b>: Approximate version of NBAStar</li>
 * <li><b>DeltaStepping</b>: Parallel shortest path algorithm</li>
 * <li><b>CHBidirectionalDijkstra</b>: Contraction Hierarchies bidirectional Dijkstra</li>
 * <li><b>TransitNodeRouting</b>: Fast routing for large road networks</li>
 * </ul>
 * </p>
 * 
 * <h2>K-Shortest Paths Algorithms</h2>
 * <p>
 * For finding multiple alternative paths via {@link KShortestPathAlgorithmEnum}:
 * <ul>
 * <li><b>Yen</b>: Finds k simple shortest paths (no loops)</li>
 * <li><b>Bhandari</b>: Finds k edge-disjoint shortest paths</li>
 * </ul>
 * </p>
 * 
 * <h2>Path Caching</h2>
 * <p>
 * The path computer can cache computed shortest paths for repeated queries:
 * <ul>
 * <li>Use {@link #setSaveComputedShortestPaths(boolean)} to enable/disable caching</li>
 * <li>Cached paths are returned instantly without recomputation</li>
 * <li>Cache is invalidated when the graph structure changes</li>
 * <li>Paths can be saved to/loaded from a matrix for persistence</li>
 * </ul>
 * </p>
 * 
 * <h2>Version Management</h2>
 * <p>
 * The path computer maintains a version number that is incremented when the graph
 * changes. This allows paths to detect if they were computed on an outdated graph
 * structure.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * IGraph graph = ...;
 * IPathComputer computer = graph.getPathComputer();
 * 
 * // Configure algorithm
 * computer.setShortestPathAlgorithm("Dijkstra");
 * computer.setSaveComputedShortestPaths(true);
 * 
 * // Compute paths
 * IPath path = computer.computeShortestPathBetween(scope, source, target);
 * IList kPaths = computer.computeKShortestPathsBetween(scope, source, target, 5);
 * 
 * // Save/load cached paths
 * IMatrix pathMatrix = computer.saveShortestPaths(scope);
 * computer.loadShortestPaths(scope, pathMatrix);
 * </pre>
 * 
 * @param <V> the type of vertices in the graph
 * @param <E> the type of edges in the graph
 * 
 * @see IGraph
 * @see IPath
 * @see ShortestPathAlgorithmEnum
 * @see KShortestPathAlgorithmEnum
 * @author drogoul
 */
public interface IPathComputer<V, E> {

	/**
	 * The Enum shortestPathAlgorithm.
	 */
	public enum ShortestPathAlgorithmEnum {

		/** The Floyd warshall. */
		FloydWarshall,

		/** The Bellmann ford. */
		BellmannFord,

		/** The Dijkstra. */
		Dijkstra,

		/** The A star. */
		AStar,

		/** The NBA star. */
		NBAStar,

		/** The NBA star approx. */
		NBAStarApprox,

		/** The Delta stepping. */
		DeltaStepping,

		/** The CH bidirectional dijkstra. */
		CHBidirectionalDijkstra,

		/** The Bidirectional dijkstra. */
		BidirectionalDijkstra,

		/** The Transit node routing. */
		TransitNodeRouting;
	}

	/**
	 * The Enum kShortestPathAlgorithm.
	 */
	public enum KShortestPathAlgorithmEnum {

		/** The Yen. */
		Yen,
		/** The Bhandari. */
		Bhandari;
	}

	/**
	 * Gets the shortest path computed.
	 *
	 * @return the shortest path computed
	 */
	Map<Pair<V, V>, IList<IList<E>>> getShortestPathComputed();

	/**
	 * Gets the shortest path.
	 *
	 * @param s
	 *            the s
	 * @param t
	 *            the t
	 * @return the shortest path
	 */
	IList<E> getShortestPath(V s, V t);

	/**
	 * Save shortest paths.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama int matrix
	 */
	IMatrix<Integer> saveShortestPaths(IScope scope);

	/**
	 * Save paths.
	 *
	 * @param M
	 *            the m
	 * @param vertices
	 *            the vertices
	 * @param nbvertices
	 *            the nbvertices
	 * @param v1
	 *            the v 1
	 * @param i
	 *            the i
	 * @param t
	 *            the t
	 * @return the i list
	 */
	IList savePaths(int M[], IList vertices, int nbvertices, Object v1, int i, int t);

	/**
	 * Gets the shortest path from matrix.
	 *
	 * @param s
	 *            the s
	 * @param t
	 *            the t
	 * @return the shortest path from matrix
	 */
	IList<E> getShortestPathFromMatrix(V s, V t);

	/**
	 * Checks if is save computed shortest paths.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is save computed shortest paths
	 * @date 30 oct. 2023
	 */
	boolean isSaveComputedShortestPaths();

	/**
	 * Sets the save computed shortest paths.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param saveComputedShortestPaths
	 *            the new save computed shortest paths
	 * @date 30 oct. 2023
	 */
	void setSaveComputedShortestPaths(boolean saveComputedShortestPaths);

	/**
	 * Load shortest paths.
	 *
	 * @param scope
	 *            the scope
	 * @param matrix
	 *            the matrix
	 */
	void loadShortestPaths(IScope scope, IMatrix matrix);

	/**
	 * Compute shortest path between.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the i path
	 * @date 30 oct. 2023
	 */
	IPath<V, E, IGraph<V, E>> computeShortestPathBetween(IScope scope, V source, V target);

	/**
	 * Gets the shortest path.
	 *
	 * @param scope
	 *            the scope
	 * @param algo
	 *            the algo
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the shortest path
	 */
	IList<E> getShortestPath(IScope scope, ShortestPathAlgorithm<V, E> algo, V source, V target);

	/**
	 * Compute best route between.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the i list
	 * @date 30 oct. 2023
	 */
	IList<E> computeBestRouteBetween(IScope scope, V source, V target);

	/**
	 * Compute K shortest paths between.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param k
	 *            the k
	 * @return the i list
	 * @date 30 oct. 2023
	 */
	IList<IPath<V, E, IGraph<V, E>>> computeKShortestPathsBetween(IScope scope, V source, V target, int k);

	/**
	 * Ge kt shortest path.
	 *
	 * @param scope
	 *            the scope
	 * @param algo
	 *            the algo
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param k
	 *            the k
	 * @param useLinkedGraph
	 *            the use linked graph
	 * @return the i list
	 */
	IList<IList<E>> geKtShortestPath(IScope scope, KShortestPathAlgorithm algo, V source, V target, int k,
			boolean useLinkedGraph);

	/**
	 * Compute K best routes between.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param k
	 *            the k
	 * @return the i list
	 * @date 30 oct. 2023
	 */
	IList<IList<E>> computeKBestRoutesBetween(IScope scope, V source, V target, int k);

	/**
	 * Sets the shortest path algorithm.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param s
	 *            the new shortest path algorithm
	 * @date 30 oct. 2023
	 */
	void setShortestPathAlgorithm(String s);

	/**
	 * Sets the k shortest path algorithm.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param s
	 *            the new k shortest path algorithm
	 * @date 30 oct. 2023
	 */
	void setKShortestPathAlgorithm(String s);

	/**
	 * Gets the version.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the version
	 * @date 30 oct. 2023
	 */
	int getVersion();

	/**
	 * Sets the version.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param version
	 *            the new version
	 * @date 30 oct. 2023
	 */
	void setVersion(int version);

	/**
	 * Inc version.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 oct. 2023
	 */
	void incVersion();

	/**
	 * Re init path finder.
	 */
	void reInitPathFinder();

}