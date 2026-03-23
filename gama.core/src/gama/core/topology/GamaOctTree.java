/*******************************************************************************************************
 *
 * GamaOctTree.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.core.topology;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Ordering;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.topology.ISpatialIndex;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.map.GamaMapFactory;
import gama.api.utils.collections.Collector;
import gama.api.utils.collections.ICollector;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.geometry.IIntersectable;
import gama.api.utils.interfaces.IAgentFilter;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.gaml.operators.Maths;

/**
 * An OctTree allows to quickly find an object on a three-dimensional space.
 *
 * <p>
 * An OctTree recursively subdivides a 3D space (volume) into eight octants. Each node of an OctTree subdivides
 * the volume covered by the bounding box of its parent node into eight smaller boxes covering the eight octants
 * (upper/lower × left/right × front/back) of the parent box.
 * </p>
 *
 * <p>
 * Unlike {@link GamaQuadTree}, which only partitions the XY plane and ignores the Z axis, this implementation
 * partitions all three spatial dimensions (X, Y and Z). As a consequence, proximity queries against 3D agents
 * with meaningful Z coordinates (e.g. flying agents, underground agents, stacked layers) are more precise:
 * the candidate set returned by the tree is already filtered in 3D, reducing the number of false positives
 * that the caller has to discard.
 * </p>
 *
 * <p>
 * The class exposes the same {@link ISpatialIndex} interface as {@link GamaQuadTree} and is therefore a
 * drop-in replacement. Use {@link #create(IEnvelope, boolean)} to obtain an instance (possibly wrapped in a
 * thread-safe {@link OctTreeSynchronizer}).
 * </p>
 *
 * <h3>Design decisions and trade-offs</h3>
 * <ul>
 *   <li><b>Subdivision criterion:</b> an {@link OctNode} is split when its {@code objects} map reaches
 *       {@link #maxCapacity} entries <em>and</em> all three dimensions of the node are larger than
 *       {@link #minSize}. This prevents infinite splits in degenerate flat or linear worlds.</li>
 *   <li><b>Duplicate storage:</b> agents whose envelope spans multiple octants are stored in every leaf
 *       that their envelope overlaps (same strategy as {@link GamaQuadTree} for XY-spanning agents).</li>
 *   <li><b>Thread safety:</b> when {@link GamaPreferences.Experimental#QUADTREE_SYNCHRONIZATION} is
 *       {@code true}, the factory method returns an {@link OctTreeSynchronizer} that delegates to a
 *       single underlying {@code GamaOctTree} under a coarse-grained lock — identical to the quad-tree
 *       strategy.</li>
 *   <li><b>Parallel maps:</b> when {@code parallel} is {@code true} the per-node object map is a
 *       synchronized ordered map; otherwise it is a plain ordered map.</li>
 * </ul>
 *
 * @author Alexis Drogoul, adapted from GamaQuadTree for 3D worlds
 * <h3>Distance computations in 3D</h3>
 * <p>
 * The query methods ({@link #allAtDistance}, {@link #firstAtDistance}) use {@link #distance3D(IShape, IShape)} instead
 * of the standard {@link IShape#euclidianDistanceTo(IShape)}, which is documented with a "WARNING Only 2D now"
 * comment in both {@code GamaShape} and {@code GamaProxyGeometry}.
 * </p>
 * <p>
 * The helper applies a two-tier strategy:
 * </p>
 * <ul>
 *   <li><b>Point ↔ point:</b> calls {@link IPoint#euclidianDistanceTo(IPoint)}, which delegates to the JTS
 *       {@code Coordinate.distance3D()} method — a true Euclidean distance in all three dimensions.</li>
 *   <li><b>Any other pair:</b> returns the 3D Euclidean distance between the two {@link IEnvelope}s (minimum
 *       bounding boxes) as a <em>lower-bound approximation</em>. The {@code Envelope3D.distance()} implementation
 *       already takes the Z axis into account, so this estimate is strictly ≤ the real geometry distance.
 *       This is a <em>naive / first-intention</em> approximation; for more accurate results a full 3D
 *       geometry distance algorithm (e.g. GJK or JTS 3D extensions) would be required.</li>
 * </ul>
 * <p>
 * Because the envelope distance is a lower bound, no valid neighbour is ever incorrectly excluded: agents
 * at the boundary of the search radius may be over-counted (false positives from the candidate set), but they
 * are then removed by the post-filtering step in each query method.
 * </p>
 *
 * @author Alexis Drogoul, adapted from GamaQuadTree for 3D worlds
 * @see GamaQuadTree
 * @see ISpatialIndex
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaOctTree implements ISpatialIndex {

	static {
		DEBUG.OFF();
	}

	// -------------------------------------------------------------------------
	// Fields
	// -------------------------------------------------------------------------

	/**
	 * The root node of the octree. It covers the entire simulation envelope.
	 */
	final OctNode root;

	/**
	 * The maximum number of agents that a single {@link OctNode} may hold before it is split into eight children.
	 * This is deliberately the same value as in {@link GamaQuadTree} to keep comparable memory usage.
	 */
	final static int maxCapacity = 100;

	/**
	 * The minimum spatial size (in each of the three dimensions) below which a node will no longer be split,
	 * regardless of the number of agents it contains. Prevents degenerate splits in very flat or very thin worlds.
	 * Initialised to {@code bounds.getWidth() / 100d} at construction time.
	 */
	double minSize = 10;

	/**
	 * Whether this tree was constructed in parallel mode, in which case per-node maps use
	 * synchronized collections.
	 */
	final boolean parallel;

	// -------------------------------------------------------------------------
	// 3D distance helper
	// -------------------------------------------------------------------------

	/**
	 * Computes a 3D-aware distance between {@code source} and {@code target}, used by all proximity query methods in
	 * this class instead of the standard {@link IShape#euclidianDistanceTo(IShape)} (which is documented as
	 * "WARNING Only 2D now" in the current codebase).
	 *
	 * <h4>Algorithm — two-tier, first-intention approach</h4>
	 * <ol>
	 *   <li><b>Both shapes are points</b> ({@link IShape#isPoint()} returns {@code true} for both):
	 *       delegates to {@link IPoint#euclidianDistanceTo(IPoint)}, which in turn calls the JTS
	 *       {@code Coordinate.distance3D()} method:
	 *       <pre>  sqrt((x2-x1)² + (y2-y1)² + (z2-z1)²)</pre>
	 *       This is an <em>exact</em> 3D Euclidean distance.</li>
	 *   <li><b>All other cases</b> (at least one shape is not a point — line, polygon, multi-geometry …):
	 *       returns the 3D Euclidean distance between the two minimum bounding boxes ({@link IEnvelope}s)
	 *       via {@link IEnvelope#distance(IEnvelope)}.  The underlying {@code Envelope3D.distance()}
	 *       implementation already accounts for the Z axis:
	 *       <pre>  sqrt(dx² + dy² + dz²)</pre>
	 *       where each component is the gap between the two intervals on that axis (0 if they overlap).</li>
	 * </ol>
	 *
	 * <h4>Approximation caveats for complex geometries</h4>
	 * <p>
	 * For non-point shapes the envelope distance is a <em>lower-bound approximation</em> of the true geometry
	 * distance: it is always ≤ the real boundary-to-boundary distance because bounding boxes are the tightest
	 * axis-aligned containers.  This means:
	 * </p>
	 * <ul>
	 *   <li>The approximation can return 0 even when the shapes do not actually touch (e.g. two L-shaped
	 *       polygons whose bounding boxes overlap but whose boundaries do not).</li>
	 *   <li>Agents at the outer edge of a search radius may be included as false positives in the candidate
	 *       set.  They are subsequently removed by the exact distance check in the callers
	 *       ({@link #allAtDistance}, {@link #firstAtDistance}).</li>
	 *   <li>No valid neighbour is ever <em>excluded</em> (no false negatives), so query correctness is
	 *       preserved; only efficiency is slightly reduced compared to a full 3D geometry distance.</li>
	 * </ul>
	 * <p>
	 * A more accurate implementation would require a full 3D geometry distance algorithm (e.g. the GJK
	 * algorithm, or JTS 3D distance extensions).  The envelope approach is a pragmatic first intention
	 * that is fast, dependency-free and always safe.
	 * </p>
	 *
	 * @param source
	 *            the reference shape (the one the user is measuring distance <em>from</em>)
	 * @param target
	 *            the candidate agent shape
	 * @return a 3D-aware distance value that is:
	 *         <ul>
	 *           <li><b>exact</b> when both shapes are points;</li>
	 *           <li>a <b>lower-bound approximation</b> (bounding-box distance) otherwise.</li>
	 *         </ul>
	 */
	private static double distance3D(final IShape source, final IShape target) {
		if (source.isPoint() && target.isPoint()) {
			// Exact 3D point-to-point distance: IPoint.euclidianDistanceTo delegates to
			// Coordinate.distance3D() which computes sqrt(dx²+dy²+dz²) in all three dimensions.
			return source.getLocation().euclidianDistanceTo(target.getLocation());
		}
		// Approximation for non-point geometries: 3D bounding-box (envelope) distance.
		// This is a lower bound: envelope distance ≤ true geometry distance.
		// See method Javadoc for full discussion of the approximation.
		final IEnvelope se = source.getEnvelope();
		final IEnvelope te = target.getEnvelope();
		if (se == null || te == null) return Double.MAX_VALUE;
		return se.distance(te);
	}

	// -------------------------------------------------------------------------
	// Factory
	// -------------------------------------------------------------------------

	/**
	 * Creates a spatial index for a 3D world. Returns a thread-safe wrapper when the
	 * {@link GamaPreferences.Experimental#QUADTREE_SYNCHRONIZATION} preference is enabled (cf. issue #3576).
	 *
	 * @param envelope
	 *            the bounding envelope of the simulation world (must include valid min/max Z values for the
	 *            3D partitioning to be effective; a flat/2D envelope works correctly but gives no Z benefit)
	 * @param parallel
	 *            {@code true} to use synchronized per-node maps (required when agents may be inserted or
	 *            removed concurrently from multiple threads)
	 * @return a new {@link ISpatialIndex} backed by an OctTree, possibly wrapped in an
	 *         {@link OctTreeSynchronizer}
	 */
	public static ISpatialIndex create(final IEnvelope envelope, final boolean parallel) {
		ISpatialIndex ot = new GamaOctTree(envelope, parallel);
		if (GamaPreferences.Experimental.QUADTREE_SYNCHRONIZATION.getValue()) return new OctTreeSynchronizer(ot);
		return ot;
	}

	// -------------------------------------------------------------------------
	// OctTreeSynchronizer inner class
	// -------------------------------------------------------------------------

	/**
	 * A thread-safe decorator for any {@link ISpatialIndex}. Every public method is {@code synchronized} on
	 * the decorator instance, providing coarse-grained mutual exclusion. This mirrors the
	 * {@code QuadTreeSynchronizer} pattern from {@link GamaQuadTree}.
	 *
	 * <p>
	 * This class is package-private and should not be used directly; obtain instances via
	 * {@link GamaOctTree#create(IEnvelope, boolean)}.
	 * </p>
	 */
	static class OctTreeSynchronizer implements ISpatialIndex {

		/**
		 * The underlying spatial index that is being synchronized. All delegation calls are made to this object
		 * while holding the monitor lock of the enclosing {@link OctTreeSynchronizer} instance.
		 */
		private final ISpatialIndex octree;

		/**
		 * Instantiates a new {@link OctTreeSynchronizer} wrapping the given spatial index.
		 *
		 * @param ot
		 *            the underlying spatial index to synchronize; must not be {@code null}
		 */
		public OctTreeSynchronizer(final ISpatialIndex ot) {
			octree = ot;
		}

		/**
		 * Inserts an agent into the underlying spatial index under a lock.
		 *
		 * @param agent
		 *            the agent to insert; {@code null} is silently ignored by the delegate
		 */
		@Override
		public synchronized void insert(final IAgent agent) {
			octree.insert(agent);
		}

		/**
		 * Removes an agent from the underlying spatial index under a lock.
		 *
		 * @param previous
		 *            the agent's previous bounding envelope before the move or death
		 * @param agent
		 *            the agent to remove
		 */
		@Override
		public synchronized void remove(final IEnvelope previous, final IAgent agent) {
			octree.remove(previous, agent);
		}

		/**
		 * Finds the first (closest) agent within the given distance under a lock.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param source
		 *            the source shape
		 * @param dist
		 *            the maximum distance
		 * @param f
		 *            the agent filter
		 * @return the closest matching agent, or {@code null}
		 */
		@Override
		public synchronized IAgent firstAtDistance(final IScope scope, final IShape source, final double dist,
				final IAgentFilter f) {
			return octree.firstAtDistance(scope, source, dist, f);
		}

		/**
		 * Finds the N closest agents within the given distance under a lock.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param source
		 *            the source shape
		 * @param dist
		 *            the maximum distance
		 * @param f
		 *            the agent filter
		 * @param number
		 *            the maximum number of results
		 * @param alreadyChosen
		 *            agents to exclude from the results
		 * @return a collection of up to {@code number} matching agents
		 */
		@Override
		public synchronized Collection<IAgent> firstAtDistance(final IScope scope, final IShape source,
				final double dist, final IAgentFilter f, final int number, final Collection<IAgent> alreadyChosen) {
			return octree.firstAtDistance(scope, source, dist, f, number, alreadyChosen);
		}

		/**
		 * Returns all agents whose envelope intersects (or is contained in) the given envelope, under a lock.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param source
		 *            the source shape (used for additional filtering)
		 * @param envelope
		 *            the search envelope
		 * @param f
		 *            the agent filter
		 * @param contained
		 *            if {@code true}, only fully contained agents are returned
		 * @return a collection of matching agents
		 */
		@Override
		public synchronized Collection<IAgent> allInEnvelope(final IScope scope, final IShape source,
				final IEnvelope envelope, final IAgentFilter f, final boolean contained) {
			return octree.allInEnvelope(scope, source, envelope, f, contained);
		}

		/**
		 * Returns all agents within the given distance from the source, under a lock.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param source
		 *            the source shape
		 * @param dist
		 *            the maximum distance
		 * @param f
		 *            the agent filter
		 * @return a collection of matching agents
		 */
		@Override
		public synchronized Collection<IAgent> allAtDistance(final IScope scope, final IShape source, final double dist,
				final IAgentFilter f) {
			return octree.allAtDistance(scope, source, dist, f);
		}

		/**
		 * Disposes the underlying spatial index. This call is not synchronized because it is expected to be called
		 * only when the index is no longer in use by any thread.
		 */
		@Override
		public void dispose() {
			octree.dispose();
		}

	}

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new {@link GamaOctTree} covering the given 3D bounding envelope.
	 *
	 * <p>
	 * The root {@link OctNode} is created with the full simulation envelope. The minimum node size
	 * ({@link #minSize}) is set to {@code bounds.getWidth() / 100d}; when the envelope has zero width,
	 * it defaults to the value set in the field initialiser (10).
	 * </p>
	 *
	 * @param bounds
	 *            the 3D bounding envelope of the simulation world; the Z range should be set to meaningful
	 *            values ({@link IEnvelope#getMinZ()} and {@link IEnvelope#getMaxZ()}) so that octree
	 *            partitioning is effective in the Z direction
	 * @param parallel
	 *            {@code true} to use synchronized per-node maps
	 */
	private GamaOctTree(final IEnvelope bounds, final boolean parallel) {
		this.parallel = parallel;
		root = new OctNode(GamaEnvelopeFactory.of(bounds));
		minSize = bounds.getWidth() / 100d;
	}

	// -------------------------------------------------------------------------
	// ISpatialIndex implementation
	// -------------------------------------------------------------------------

	/**
	 * Disposes the entire octree by recursively disposing all nodes and clearing all stored agents.
	 * After this call the tree must not be used.
	 */
	@Override
	public void dispose() {
		root.dispose();
	}

	/**
	 * Inserts an agent into the octree.
	 *
	 * <p>
	 * Point agents (whose envelope has zero area) are inserted using their location {@link IPoint},
	 * so that they always fall into exactly one leaf node. Non-point agents are inserted using their
	 * full 3D {@link IEnvelope}, which may cause them to appear in multiple leaf nodes when they span
	 * octant boundaries.
	 * </p>
	 *
	 * @param agent
	 *            the agent to insert; {@code null} is silently ignored
	 */
	@Override
	public void insert(final IAgent agent) {
		if (agent == null) return;
		if (agent.isPoint()) {
			root.add(agent.getLocation(), agent);
		} else {
			root.add(agent.getEnvelope(), agent);
		}
	}

	/**
	 * Removes an agent from the octree.
	 *
	 * <p>
	 * The previous envelope is needed because the agent's current envelope may already have changed
	 * (e.g. after a move). If {@code previous} is {@code null}, the agent's current envelope is used
	 * as a fallback. The envelope is disposed after the removal.
	 * </p>
	 *
	 * @param previous
	 *            the envelope the agent occupied before it moved or died; may be {@code null}
	 * @param agent
	 *            the agent to remove
	 */
	@Override
	public void remove(final IEnvelope previous, final IAgent agent) {
		final IEnvelope current = previous == null ? agent.getEnvelope() : previous;
		if (current == null) return;
		if (current.getArea() == 0.0) {
			root.remove(current.center(), agent);
		} else {
			root.remove(current, agent);
		}
		current.dispose();
	}

	// -------------------------------------------------------------------------
	// Query helpers
	// -------------------------------------------------------------------------

	/**
	 * Finds all agents whose 3D envelope intersects the given search region {@code r}, then applies
	 * the provided {@link IAgentFilter} and shuffles the results in-place using the scope's random
	 * number generator (to avoid systematic bias in the order of returned neighbours).
	 *
	 * <p>
	 * Addresses Issue 722 by explicitly shuffling results and removing duplicates.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope (used for the random shuffle)
	 * @param source
	 *            the source shape (passed to {@link IAgentFilter#filter})
	 * @param r
	 *            the 3D search envelope
	 * @param filter
	 *            the filter to apply to the candidate set
	 * @return the filtered, shuffled list of intersecting agents (never {@code null})
	 */
	protected Collection<IAgent> findIntersects(final IScope scope, final IShape source, final IEnvelope r,
			final IAgentFilter filter) {
		try (final ICollector<IAgent> list = Collector.getOrderedSet()) {
			root.findIntersects(r, list);
			if (list.isEmpty()) return GamaListFactory.create();
			filter.filter(scope, source, list);
			list.shuffleInPlaceWith(scope.getRandom());
			return list.items();
		}
	}

	// -------------------------------------------------------------------------
	// ISpatialIndex queries
	// -------------------------------------------------------------------------

	/**
	 * Returns all agents within 3D Euclidean distance {@code dist} from the source shape.
	 *
	 * <p>
	 * The search envelope is expanded by {@code dist × √2} in all three dimensions before querying the
	 * tree. The candidate set is then post-filtered using {@link #distance3D(IShape, IShape)}:
	 * </p>
	 * <ul>
	 *   <li>For point sources and point targets the distance is exact in 3D.</li>
	 *   <li>For non-point shapes a 3D bounding-box distance is used as a lower-bound approximation (see
	 *       {@link #distance3D} Javadoc for details).</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param source
	 *            the source shape; distance is measured from its boundary
	 * @param dist
	 *            the maximum 3D Euclidean distance (inclusive)
	 * @param f
	 *            the agent filter
	 * @return all agents whose {@link #distance3D 3D distance} to {@code source} is ≤ {@code dist}
	 */
	@Override
	public Collection<IAgent> allAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f) {
		final double exp = dist * Maths.SQRT2;
		final IEnvelope env = GamaEnvelopeFactory.of(source.getEnvelope());
		env.expandBy(exp);
		try {
			final Collection<IAgent> result = findIntersects(scope, source, env, f);
			if (result.isEmpty()) return GamaListFactory.create();
			result.removeIf(each -> distance3D(source, each) > dist);
			return result;
		} finally {
			env.dispose();
		}
	}

	/**
	 * Returns up to {@code number} agents that are closest to the source shape and within {@code dist},
	 * excluding any agent already present in {@code alreadyChosen}.
	 *
	 * <p>
	 * The search envelope is expanded by {@code dist × √2} in all three dimensions. If the candidate set
	 * has more than {@code number} elements, the {@code number} closest (by {@link #distance3D 3D distance})
	 * are kept. For point agents the distance is exact; for complex geometries a bounding-box lower bound
	 * is used (see {@link #distance3D} for the full discussion).
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param source
	 *            the source shape
	 * @param dist
	 *            the maximum distance
	 * @param f
	 *            the agent filter
	 * @param number
	 *            the maximum number of agents to return
	 * @param alreadyChosen
	 *            agents to exclude from the results
	 * @return up to {@code number} closest agents by {@link #distance3D 3D distance}
	 */
	@Override
	public Collection<IAgent> firstAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f, final int number, final Collection<IAgent> alreadyChosen) {
		final double exp = dist * Maths.SQRT2;
		final IEnvelope env = GamaEnvelopeFactory.of(source.getEnvelope());
		env.expandBy(exp);
		try {
			final Collection<IAgent> in_cube = findIntersects(scope, source, env, f);
			in_cube.removeAll(alreadyChosen);
			if (in_cube.isEmpty()) return GamaListFactory.create();
			if (in_cube.size() <= number) return in_cube;
			final Ordering<IShape> ordering = Ordering.natural().onResultOf(input -> distance3D(source, input));
			return ordering.leastOf(in_cube, number);
		} finally {
			env.dispose();
		}
	}

	/**
	 * Returns the single agent closest to the source shape within the given distance.
	 *
	 * <p>
	 * The search envelope is expanded by {@code dist × √2} in all three dimensions. Among all candidates,
	 * the one with the smallest {@link #distance3D 3D distance} to {@code source} (strictly less than
	 * {@code dist}) is returned. Returns {@code null} when no such agent exists.
	 * </p>
	 * <p>
	 * Distance is exact for point agents and approximate (bounding-box lower bound) for complex geometries;
	 * see {@link #distance3D} for the full discussion of the approximation.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param source
	 *            the source shape
	 * @param dist
	 *            the maximum distance
	 * @param f
	 *            the agent filter
	 * @return the closest matching agent by {@link #distance3D 3D distance}, or {@code null}
	 */
	@Override
	public IAgent firstAtDistance(final IScope scope, final IShape source, final double dist, final IAgentFilter f) {
		final IEnvelope env = GamaEnvelopeFactory.of(source.getEnvelope());
		env.expandBy(dist * Maths.SQRT2);
		try {
			final Collection<IAgent> in_cube = findIntersects(scope, source, env, f);
			if (in_cube.isEmpty()) return null;
			double min_distance = dist;
			IAgent min_agent = null;
			for (final IAgent a : in_cube) {
				final double dd = distance3D(source, a);
				if (dd < min_distance) {
					min_distance = dd;
					min_agent = a;
				}
			}
			return min_agent;
		} finally {
			env.dispose();
		}
	}

	/**
	 * Returns all agents whose envelope intersects (or is contained in) the given search envelope.
	 *
	 * <p>
	 * Delegates to {@link #findIntersects(IScope, IShape, IEnvelope, IAgentFilter)}, which performs a
	 * full 3D intersection test. The {@code contained} flag is accepted for interface compatibility but
	 * is not enforced here — callers that strictly require containment semantics should post-filter the
	 * result themselves.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param source
	 *            the source shape
	 * @param envelope
	 *            the search envelope
	 * @param f
	 *            the agent filter
	 * @param contained
	 *            if {@code true}, only agents fully contained in the envelope should be returned
	 *            (currently not enforced — see note above)
	 * @return the collection of matching agents
	 */
	@Override
	public Collection<IAgent> allInEnvelope(final IScope scope, final IShape source, final IEnvelope envelope,
			final IAgentFilter f, final boolean contained) {
		return findIntersects(scope, source, envelope, f);
	}

	// =========================================================================
	// OctNode inner class
	// =========================================================================

	/**
	 * An internal node of the {@link GamaOctTree}. An {@code OctNode} covers a 3D box ({@link #bounds})
	 * and either stores agents directly in {@link #objects} (when it is a leaf node) or delegates to eight
	 * children covering the eight octants of its bounding box ({@link #nwf}, {@link #nef}, {@link #swf},
	 * {@link #sef}, {@link #nwb}, {@link #neb}, {@link #swb}, {@link #seb}).
	 *
	 * <p>
	 * Octant naming convention (matches the 2D quad-tree convention extended with a front/back axis):
	 * </p>
	 * <ul>
	 *   <li><b>n/s</b> — north / south (Y axis: low Y = north, high Y = south)</li>
	 *   <li><b>w/e</b> — west / east   (X axis: low X = west, high X = east)</li>
	 *   <li><b>f/b</b> — front / back  (Z axis: low Z = front, high Z = back)</li>
	 * </ul>
	 *
	 * <p>
	 * A node is split when both conditions hold:
	 * </p>
	 * <ol>
	 *   <li>It has accumulated {@link GamaOctTree#maxCapacity} or more agents.</li>
	 *   <li>All three extents of its bounding box are larger than {@link GamaOctTree#minSize}.</li>
	 * </ol>
	 */
	private class OctNode {

		// ------------------------------------------------------------------
		// Fields
		// ------------------------------------------------------------------

		/**
		 * The 3D bounding box covered by this node. Must not be modified after construction.
		 */
		final IEnvelope bounds;

		/**
		 * The mid-point in the X direction, i.e. {@code (bounds.getMinX() + bounds.getMaxX()) / 2}.
		 * Used to determine which east/west octant a point falls into.
		 */
		protected final double halfx;

		/**
		 * The mid-point in the Y direction, i.e. {@code (bounds.getMinY() + bounds.getMaxY()) / 2}.
		 * Used to determine which north/south octant a point falls into.
		 */
		protected final double halfy;

		/**
		 * The mid-point in the Z direction, i.e. {@code (bounds.getMinZ() + bounds.getMaxZ()) / 2}.
		 * Used to determine which front/back octant a point falls into.
		 */
		protected final double halfz;

		// Eight children — front face (low Z half)
		/** North-West-Front child node (low X, low Y, low Z). {@code null} when this is a leaf. */
		protected volatile OctNode nwf;
		/** North-East-Front child node (high X, low Y, low Z). {@code null} when this is a leaf. */
		protected volatile OctNode nef;
		/** South-West-Front child node (low X, high Y, low Z). {@code null} when this is a leaf. */
		protected volatile OctNode swf;
		/** South-East-Front child node (high X, high Y, low Z). {@code null} when this is a leaf. */
		protected volatile OctNode sef;

		// Eight children — back face (high Z half)
		/** North-West-Back child node (low X, low Y, high Z). {@code null} when this is a leaf. */
		protected volatile OctNode nwb;
		/** North-East-Back child node (high X, low Y, high Z). {@code null} when this is a leaf. */
		protected volatile OctNode neb;
		/** South-West-Back child node (low X, high Y, high Z). {@code null} when this is a leaf. */
		protected volatile OctNode swb;
		/** South-East-Back child node (high X, high Y, high Z). {@code null} when this is a leaf. */
		protected volatile OctNode seb;

		/**
		 * The agents stored in this leaf node, mapped to their spatial key ({@link IPoint} for point agents,
		 * {@link IEnvelope} for non-point agents). Insertion order is preserved.
		 *
		 * <p>
		 * When {@link GamaOctTree#parallel} is {@code true} a synchronized ordered map is used; otherwise a
		 * plain ordered map.
		 * </p>
		 *
		 * <p>
		 * Addresses part of Issue 722: agents must be kept in insertion order.
		 * </p>
		 */
		protected final Map<IAgent, IIntersectable> objects =
				parallel ? GamaMapFactory.synchronizedOrderedMap() : GamaMapFactory.create();

		/**
		 * Whether this node is allowed to split further. {@code false} when any of the three extents of
		 * {@link #bounds} is ≤ {@link GamaOctTree#minSize}, preventing degenerate infinite splits.
		 */
		protected final boolean canSplit;

		// ------------------------------------------------------------------
		// Constructor
		// ------------------------------------------------------------------

		/**
		 * Instantiates a new {@link OctNode} covering the given 3D bounding envelope.
		 *
		 * @param bounds
		 *            the 3D bounding envelope; must not be {@code null}
		 */
		public OctNode(final IEnvelope bounds) {
			this.bounds = bounds;
			final double hw = bounds.getWidth();
			final double hh = bounds.getHeight();
			final double hd = bounds.getDepth();
			halfx = bounds.getMinX() + hw / 2;
			halfy = bounds.getMinY() + hh / 2;
			halfz = bounds.getMinZ() + hd / 2;
			canSplit = hw > minSize && hh > minSize && hd > minSize;
		}

		// ------------------------------------------------------------------
		// Lifecycle
		// ------------------------------------------------------------------

		/**
		 * Recursively disposes this node and all its children, releasing the envelopes stored as spatial keys
		 * in {@link #objects} and setting all child references to {@code null}.
		 */
		public void dispose() {
			objects.forEach((a, e) -> { if (e != null) { e.dispose(); } });
			objects.clear();
			if (nwf != null) {
				nwf.dispose(); nwf = null;
				nef.dispose(); nef = null;
				swf.dispose(); swf = null;
				sef.dispose(); sef = null;
				nwb.dispose(); nwb = null;
				neb.dispose(); neb = null;
				swb.dispose(); swb = null;
				seb.dispose(); seb = null;
			}
		}

		// ------------------------------------------------------------------
		// Insertion
		// ------------------------------------------------------------------

		/**
		 * Inserts a point agent into this node (or one of its descendants).
		 *
		 * <p>
		 * If this node is a leaf, the agent is stored directly in {@link #objects}. Otherwise, the point is
		 * routed to the single child octant that contains it.
		 * </p>
		 *
		 * @param p
		 *            the 3D location of the agent
		 * @param a
		 *            the agent to insert
		 */
		private void add(final IPoint p, final IAgent a) {
			trySplit();
			if (nwf == null) {
				objects.put(a, p);
			} else {
				getNode(p).add(p, a);
			}
		}

		/**
		 * Inserts a non-point agent (with a 3D envelope) into this node (or its descendants).
		 *
		 * <p>
		 * If this node is a leaf, the agent is stored directly in {@link #objects}. Otherwise, the agent is
		 * inserted into every child whose bounds intersect the agent's envelope, which may result in the
		 * same agent being stored in up to eight leaf nodes.
		 * </p>
		 *
		 * @param e
		 *            the 3D bounding envelope of the agent
		 * @param a
		 *            the agent to insert
		 */
		private void add(final IEnvelope e, final IAgent a) {
			trySplit();
			if (nwf == null) {
				objects.put(a, e);
			} else {
				if (nwf.bounds.intersects(e)) { nwf.add(e, a); }
				if (nef.bounds.intersects(e)) { nef.add(e, a); }
				if (swf.bounds.intersects(e)) { swf.add(e, a); }
				if (sef.bounds.intersects(e)) { sef.add(e, a); }
				if (nwb.bounds.intersects(e)) { nwb.add(e, a); }
				if (neb.bounds.intersects(e)) { neb.add(e, a); }
				if (swb.bounds.intersects(e)) { swb.add(e, a); }
				if (seb.bounds.intersects(e)) { seb.add(e, a); }
			}
		}

		/**
		 * Triggers a split of this leaf node into eight children if the capacity threshold has been reached
		 * and splitting is allowed (i.e. {@link #canSplit} is {@code true}).
		 */
		private void trySplit() {
			if (nwf == null && canSplit && objects.size() >= maxCapacity) { split(); }
		}

		// ------------------------------------------------------------------
		// Removal
		// ------------------------------------------------------------------

		/**
		 * Removes a point agent from this node (or one of its descendants).
		 *
		 * <p>
		 * If this is a leaf, the agent is removed from {@link #objects} and its spatial key (if non-{@code null})
		 * is disposed. Otherwise, the point is routed to the correct child.
		 * </p>
		 *
		 * @param p
		 *            the 3D location of the agent at removal time
		 * @param a
		 *            the agent to remove
		 */
		private void remove(final IPoint p, final IShape a) {
			if (nwf == null) {
				final IIntersectable env = objects.remove(a);
				if (env != null) { env.dispose(); }
			} else {
				getNode(p).remove(p, a);
			}
		}

		/**
		 * Removes a non-point agent from this node (or its descendants).
		 *
		 * <p>
		 * If this is a leaf, the agent is removed from {@link #objects} and its spatial key is disposed.
		 * Otherwise, the removal request is forwarded to every child whose bounds intersect the agent's
		 * envelope (since a spanning agent may have been stored in multiple octants).
		 * </p>
		 *
		 * @param e
		 *            the 3D bounding envelope of the agent at removal time
		 * @param a
		 *            the agent to remove
		 */
		private void remove(final IEnvelope e, final IShape a) {
			if (nwf == null) {
				final IIntersectable env = objects.remove(a);
				if (env != null) { env.dispose(); }
			} else {
				if (nwf.bounds.intersects(e)) { nwf.remove(e, a); }
				if (nef.bounds.intersects(e)) { nef.remove(e, a); }
				if (swf.bounds.intersects(e)) { swf.remove(e, a); }
				if (sef.bounds.intersects(e)) { sef.remove(e, a); }
				if (nwb.bounds.intersects(e)) { nwb.remove(e, a); }
				if (neb.bounds.intersects(e)) { neb.remove(e, a); }
				if (swb.bounds.intersects(e)) { swb.remove(e, a); }
				if (seb.bounds.intersects(e)) { seb.remove(e, a); }
			}
		}

		// ------------------------------------------------------------------
		// Navigation helpers
		// ------------------------------------------------------------------

		/**
		 * Returns the single child {@link OctNode} that contains the point {@code p}.
		 *
		 * <p>
		 * This method is only valid when the node has already been split (i.e. {@code nwf != null}).
		 * </p>
		 *
		 * @param p
		 *            the 3D location to locate
		 * @return the child node whose bounding box contains {@code p}
		 */
		private OctNode getNode(final IPoint p) {
			final double px = p.getX();
			final double py = p.getY();
			final double pz = p.getZ();
			final boolean north = py >= bounds.getMinY() && py < halfy;
			final boolean west  = px >= bounds.getMinX() && px < halfx;
			final boolean front = pz >= bounds.getMinZ() && pz < halfz;
			if (front) return north ? west ? nwf : nef : west ? swf : sef;
			return north ? west ? nwb : neb : west ? swb : seb;
		}

		// ------------------------------------------------------------------
		// Split
		// ------------------------------------------------------------------

		/**
		 * Splits this leaf node into eight children, redistributing the currently stored agents into the
		 * appropriate children. The {@link #objects} map is cleared after the redistribution.
		 *
		 * <p>
		 * The eight octants are named according to the convention documented on {@link OctNode}:
		 * front (low-Z) children are created first, then back (high-Z) children.
		 * </p>
		 *
		 * <p>
		 * Any dead agents ({@link IAgent#dead()}) encountered during redistribution are silently discarded.
		 * </p>
		 */
		private void split() {
			try {
				final double maxx = bounds.getMaxX();
				final double minx = bounds.getMinX();
				final double miny = bounds.getMinY();
				final double maxy = bounds.getMaxY();
				final double minz = bounds.getMinZ();
				final double maxz = bounds.getMaxZ();

				// Front children (low-Z half: [minz, halfz])
				nwf = new OctNode(GamaEnvelopeFactory.of(minx, halfx, miny, halfy, minz, halfz));
				nef = new OctNode(GamaEnvelopeFactory.of(halfx, maxx, miny, halfy, minz, halfz));
				swf = new OctNode(GamaEnvelopeFactory.of(minx, halfx, halfy, maxy, minz, halfz));
				sef = new OctNode(GamaEnvelopeFactory.of(halfx, maxx, halfy, maxy, minz, halfz));

				// Back children (high-Z half: [halfz, maxz])
				nwb = new OctNode(GamaEnvelopeFactory.of(minx, halfx, miny, halfy, halfz, maxz));
				neb = new OctNode(GamaEnvelopeFactory.of(halfx, maxx, miny, halfy, halfz, maxz));
				swb = new OctNode(GamaEnvelopeFactory.of(minx, halfx, halfy, maxy, halfz, maxz));
				seb = new OctNode(GamaEnvelopeFactory.of(halfx, maxx, halfy, maxy, halfz, maxz));

				objects.forEach((a, e) -> {
					if (a != null && !a.dead()) {
						final IShape g = a.getGeometry();
						if (g.isPoint()) {
							add(g.getLocation(), a);
						} else {
							add(g.getEnvelope(), a);
						}
					}
				});
			} finally {
				objects.clear();
			}
		}

		// ------------------------------------------------------------------
		// Query
		// ------------------------------------------------------------------

		/**
		 * Recursively collects all agents stored in this node (or its descendants) whose spatial key
		 * intersects the 3D search envelope {@code r}.
		 *
		 * <p>
		 * The intersection test between the node's bounding box and {@code r} uses the full 3D
		 * {@link IEnvelope#intersects(IEnvelope)} method, which checks all three axes (X, Y and Z).
		 * This is the primary behavioral difference from {@link GamaQuadTree.QuadNode#findIntersects},
		 * which uses only the 2D XY projection.
		 * </p>
		 *
		 * <p>
		 * Agents stored in leaf nodes are further tested using {@link IIntersectable#intersects(IEnvelope)},
		 * which also performs a full 3D intersection check. Agents whose Z range does not overlap {@code r}'s
		 * Z range are therefore excluded here, reducing the false-positive rate compared to the quad-tree.
		 * </p>
		 *
		 * @param r
		 *            the 3D search envelope (all three axes are used)
		 * @param result
		 *            the collector to which matching agents are added
		 */
		public void findIntersects(final IEnvelope r, final Collection<IAgent> result) {
			// Full 3D intersection check for the node bounds
			if (!bounds.intersects(r)) return;
			if (nwf == null) {
				objects.forEach((a, e) -> {
					// Full 3D intersection: agents are tested in all three dimensions
					if (e != null && e.intersects(r)) { result.add(a); }
				});
			} else {
				nwf.findIntersects(r, result);
				nef.findIntersects(r, result);
				swf.findIntersects(r, result);
				sef.findIntersects(r, result);
				nwb.findIntersects(r, result);
				neb.findIntersects(r, result);
				swb.findIntersects(r, result);
				seb.findIntersects(r, result);
			}
		}

	}

}
