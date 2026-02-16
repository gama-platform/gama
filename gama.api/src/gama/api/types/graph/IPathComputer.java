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
 * @param <V>
 * @param <E>
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