/*******************************************************************************************************
 *
 * GamaQuadTree.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
 * A QuadTree allows to quickly find an object on a two-dimensional space.
 *
 * <p>
 * QuadTree recursively subdivides a space into four rectangles. Each node of a QuadTree subdivides the space covered by
 * the rectangle of its parent node into four smaller rectangles covering the upper left, upper right, lower left and
 * lower right quadrant of the parent rectangle.
 * </p>
 *
 * <p>
 * Since this implementation is only ever used for <em>flat (2D worlds</em> (3D worlds use {@link GamaOctTree}
 * instead), several 2D-specific optimisations are applied:
 * </p>
 * <ul>
 *   <li>All intersection tests inside {@link QuadNode} use {@link IIntersectable#intersects2D} / the JTS
 *       {@code Envelope.intersects(Envelope)} overload rather than the 3D-aware {@code GamaEnvelope.intersects}
 *       variant, saving the Z-range check on every node visit and every stored-object test.</li>
 *   <li>The four child references ({@code nw/ne/sw/se}) are replaced by a single
 *       {@code volatile QuadNode[] children} array indexed by the 2-bit quadrant code
 *       {@code (py >= halfy ? 2 : 0) | (px >= halfx ? 1 : 0)}.  This cuts the per-node memory footprint
 *       (four separate reference fields → one reference field + one array) and keeps the four pointers
 *       contiguous in the heap, improving spatial locality for the CPU cache.</li>
 *   <li>The distance post-filter in {@link #allAtDistance} and related methods inlines the 2D Euclidean
 *       distance computation directly (skipping interface dispatch and Z-coordinate arithmetic) when the
 *       source shape is a point.</li>
 * </ul>
 *
 * @author Werner Randelshofer, adapted by Alexis Drogoul for GAMA
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaQuadTree implements ISpatialIndex {

	static {
		DEBUG.OFF();
	}

	// Child-array indices: NW=0, NE=1, SW=2, SE=3
	// quadrant code = (py >= halfy ? 2 : 0) | (px >= halfx ? 1 : 0)
	private static final int NW = 0, NE = 1, SW = 2, SE = 3;

	/** The root. */
	final QuadNode root;

	/** The Constant maxCapacity. */
	final static int maxCapacity = 100;

	/** The min size. */
	double minSize = 10;

	/** The parallel. */
	final boolean parallel;

	/**
	 * Creates the spatial index. Returns a synchronized quadtree if necessary (cf. #3576)
	 *
	 * @param envelope
	 *            the envelope
	 * @param parallel
	 *            the parallel
	 * @return the gama quad tree
	 */
	public static ISpatialIndex create(final IEnvelope envelope, final boolean parallel) {
		ISpatialIndex qt = new GamaQuadTree(envelope, parallel);
		if (GamaPreferences.Experimental.QUADTREE_SYNCHRONIZATION.getValue()) return new QuadTreeSynchronizer(qt);
		return qt;
	}

	/**
	 * Computes an inline 2D Euclidean distance between two points.
	 * Avoids interface dispatch and Z-arithmetic present in the general
	 * {@code IShape.euclidianDistanceTo} path, which is important in the
	 * tight distance-filter loops of {@link #allAtDistance} and friends.
	 *
	 * @param sx source X
	 * @param sy source Y
	 * @param tx target X
	 * @param ty target Y
	 * @return the 2D Euclidean distance
	 */
	private static double dist2D(final double sx, final double sy, final double tx, final double ty) {
		final double dx = sx - tx, dy = sy - ty;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// -------------------------------------------------------------------------
	// QuadTreeSynchronizer
	// -------------------------------------------------------------------------

	/**
	 * The Class QuadTreeSynchronizer.
	 */
	static class QuadTreeSynchronizer implements ISpatialIndex {

		/** The quadtree. */
		private final ISpatialIndex quadtree;

		/**
		 * Instantiates a new quad tree synchronizer.
		 *
		 * @param qt
		 *            the qt
		 */
		public QuadTreeSynchronizer(final ISpatialIndex qt) {
			quadtree = qt;
		}

		@Override
		public synchronized void insert(final IAgent agent) {
			quadtree.insert(agent);
		}

		@Override
		public synchronized void remove(final IEnvelope previous, final IAgent agent) {
			quadtree.remove(previous, agent);
		}

		@Override
		public synchronized IAgent firstAtDistance(final IScope scope, final IShape source, final double dist,
				final IAgentFilter f) {
			return quadtree.firstAtDistance(scope, source, dist, f);
		}

		@Override
		public synchronized Collection<IAgent> firstAtDistance(final IScope scope, final IShape source,
				final double dist, final IAgentFilter f, final int number, final Collection<IAgent> alreadyChosen) {
			return quadtree.firstAtDistance(scope, source, dist, f, number, alreadyChosen);
		}

		@Override
		public synchronized Collection<IAgent> allInEnvelope(final IScope scope, final IShape source,
				final IEnvelope envelope, final IAgentFilter f, final boolean contained) {
			return quadtree.allInEnvelope(scope, source, envelope, f, contained);
		}

		@Override
		public synchronized Collection<IAgent> allAtDistance(final IScope scope, final IShape source, final double dist,
				final IAgentFilter f) {
			return quadtree.allAtDistance(scope, source, dist, f);
		}

		@Override
		public void dispose() {
			quadtree.dispose();
		}

	}

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new gama quad tree.
	 *
	 * @param bounds
	 *            the bounds
	 * @param sync
	 *            the sync
	 */
	private GamaQuadTree(final IEnvelope bounds, final boolean sync) {
		// AD To address Issue 804, explicitly converts the bounds to an
		// Envelope 2D, so that all computations are made in 2D in the QuadTree
		this.parallel = sync;
		root = new QuadNode(GamaEnvelopeFactory.of(bounds));
		minSize = bounds.getWidth() / 100d;
	}

	// -------------------------------------------------------------------------
	// ISpatialIndex — top-level operations
	// -------------------------------------------------------------------------

	@Override
	public void dispose() {
		root.dispose();
	}

	@Override
	public void insert(final IAgent agent) {
		if (agent == null) return;
		if (agent.isPoint()) {
			root.add(agent.getLocation(), agent);
		} else {
			root.add(agent.getEnvelope(), agent);
		}
	}

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
	// findIntersects
	// -------------------------------------------------------------------------

	/**
	 * Finds all agents whose stored spatial key (point or envelope) intersects the 2D search region {@code r},
	 * applies the given filter, and shuffles the results for fairness.
	 *
	 * <p>All intersection tests are 2D-only — see class Javadoc for rationale.</p>
	 *
	 * @param scope  the current execution scope
	 * @param source the source shape (passed to the filter)
	 * @param r      the 2D search envelope
	 * @param filter the agent filter
	 * @return the filtered, shuffled collection of intersecting agents
	 */
	protected Collection<IAgent> findIntersects(final IScope scope, final IShape source, final IEnvelope r,
			final IAgentFilter filter) {
		// Addresses Issue 722: explicit shuffle + duplicate removal via ordered-set collector
		try (final ICollector<IAgent> list = Collector.getOrderedSet()) {
			root.findIntersects(r, list);
			if (list.isEmpty()) return GamaListFactory.create();
			filter.filter(scope, source, list);
			list.shuffleInPlaceWith(scope.getRandom());
			return list.items();
		}
	}

	// -------------------------------------------------------------------------
	// Distance queries
	// -------------------------------------------------------------------------

	/**
	 * Builds a 2D search envelope centred on {@code source} and expanded by {@code dist * √2}, using
	 * a direct 4-arg factory call when the source is a point to avoid a pool round-trip and a Z copy.
	 */
	private IEnvelope buildSearchEnvelope(final IShape source, final double dist) {
		final double exp = dist * Maths.SQRT2;
		if (source.isPoint()) {
			final IPoint loc = source.getLocation();
			final double x = loc.getX(), y = loc.getY();
			return GamaEnvelopeFactory.of(x - exp, x + exp, y - exp, y + exp);
		}
		final IEnvelope env = GamaEnvelopeFactory.of(source.getEnvelope());
		env.expandBy(exp);
		return env;
	}

	@Override
	public Collection<IAgent> allAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f) {
		final IEnvelope env = buildSearchEnvelope(source, dist);
		try {
			final Collection<IAgent> result = findIntersects(scope, source, env, f);
			if (result.isEmpty()) return GamaListFactory.create();
			// 2D inline distance filter: avoids IShape dispatch + Z arithmetic for point sources
			if (source.isPoint()) {
				final double sx = source.getLocation().getX(), sy = source.getLocation().getY();
				result.removeIf(each -> dist2D(sx, sy, each.getLocation().getX(), each.getLocation().getY()) > dist);
			} else {
				result.removeIf(each -> source.euclidianDistanceTo(each) > dist);
			}
			return result;
		} finally {
			env.dispose();
		}
	}

	@Override
	public Collection<IAgent> firstAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f, final int number, final Collection<IAgent> alreadyChosen) {
		final IEnvelope env = buildSearchEnvelope(source, dist);
		try {
			final Collection<IAgent> in_square = findIntersects(scope, source, env, f);
			in_square.removeAll(alreadyChosen);
			if (in_square.isEmpty()) return GamaListFactory.create();
			if (in_square.size() <= number) return in_square;
			// Ordering by 2D distance for point sources
			if (source.isPoint()) {
				final double sx = source.getLocation().getX(), sy = source.getLocation().getY();
				final Ordering<IShape> ord = Ordering.natural()
						.onResultOf(a -> dist2D(sx, sy, a.getLocation().getX(), a.getLocation().getY()));
				return ord.leastOf(in_square, number);
			}
			final Ordering<IShape> ordering =
					Ordering.natural().onResultOf(input -> source.euclidianDistanceTo(input));
			return ordering.leastOf(in_square, number);
		} finally {
			env.dispose();
		}
	}

	@Override
	public IAgent firstAtDistance(final IScope scope, final IShape source, final double dist, final IAgentFilter f) {
		final IEnvelope env = buildSearchEnvelope(source, dist);
		try {
			final Collection<IAgent> in_square = findIntersects(scope, source, env, f);
			if (in_square.isEmpty()) return null;
			double min_distance = dist;
			IAgent min_agent = null;
			if (source.isPoint()) {
				final double sx = source.getLocation().getX(), sy = source.getLocation().getY();
				for (final IAgent a : in_square) {
					final double dd = dist2D(sx, sy, a.getLocation().getX(), a.getLocation().getY());
					if (dd < min_distance) { min_distance = dd; min_agent = a; }
				}
			} else {
				for (final IAgent a : in_square) {
					final double dd = source.euclidianDistanceTo(a);
					if (dd < min_distance) { min_distance = dd; min_agent = a; }
				}
			}
			return min_agent;
		} finally {
			env.dispose();
		}
	}

	@Override
	public Collection<IAgent> allInEnvelope(final IScope scope, final IShape source, final IEnvelope envelope,
			final IAgentFilter f, final boolean contained) {
		return findIntersects(scope, source, envelope, f);
	}

	// =========================================================================
	// QuadNode inner class
	// =========================================================================

	/**
	 * An internal node of the {@link GamaQuadTree}.
	 *
	 * <h3>2D-specific optimisations applied here</h3>
	 * <ol>
	 *   <li><b>Single {@code children} array:</b> the four child references that were previously four separate
	 *       {@code volatile QuadNode} fields ({@code nw, ne, sw, se}) are now stored in a single
	 *       {@code volatile QuadNode[] children} array of length 4.  A node is a leaf when
	 *       {@code children == null}.  This reduces the per-node memory footprint and keeps the four pointers
	 *       contiguous in the heap, which is better for CPU-cache prefetch when all four children are
	 *       visited during {@link #findIntersects}.</li>
	 *   <li><b>{@code intersects2D} everywhere:</b> every intersection test — both for node-bounds pruning
	 *       and for the per-object test in leaf nodes — calls {@link IIntersectable#intersects2D} instead
	 *       of the full 3D {@code intersects}, saving the two Z-range comparisons on every call.</li>
	 *   <li><b>Quadrant index arithmetic:</b> {@link #quadrant(IPoint)} computes the child index as
	 *       {@code (py >= halfy ? 2 : 0) | (px >= halfx ? 1 : 0)}, which is two comparisons + one OR
	 *       rather than the previous four comparisons + two boolean temporaries + a ternary chain.</li>
	 * </ol>
	 */
	private class QuadNode {

		/** The 2D bounding box of this node (always flat — Z = [0, 0]). */
		final IEnvelope bounds;

		/** The mid-point in the X direction. */
		protected final double halfx;
		/** The mid-point in the Y direction. */
		protected final double halfy;

		/**
		 * Child nodes: [NW=0, NE=1, SW=2, SE=3].
		 * {@code null} while this node is a leaf; a non-null 4-element array after the first split.
		 * Declared {@code volatile} so that a reader thread that sees a non-null array also sees all
		 * writes to the individual elements performed by the splitting thread (safe publication).
		 */
		protected volatile QuadNode[] children;

		/**
		 * Agents stored in this leaf node, mapped to their 2D spatial key
		 * ({@link IPoint} for point agents, {@link IEnvelope} for non-point agents).
		 * Insertion order is preserved (addresses Issue 722).
		 */
		protected final Map<IAgent, IIntersectable> objects =
				parallel ? GamaMapFactory.synchronizedOrderedMap() : GamaMapFactory.create();

		/** Whether this node is allowed to split further. */
		protected final boolean canSplit;

		/**
		 * Instantiates a new quad node.
		 *
		 * @param bounds the 2D bounding box of this node
		 */
		public QuadNode(final IEnvelope bounds) {
			this.bounds = bounds;
			final double hw = bounds.getWidth();
			final double hh = bounds.getHeight();
			halfx = bounds.getMinX() + hw / 2;
			halfy = bounds.getMinY() + hh / 2;
			canSplit = hw > minSize && hh > minSize;
		}

		// ------------------------------------------------------------------
		// Lifecycle
		// ------------------------------------------------------------------

		/**
		 * Recursively disposes this node and all children.
		 */
		public void dispose() {
			objects.forEach((a, e) -> { if (e != null) { e.dispose(); } });
			objects.clear();
			final QuadNode[] ch = children;
			if (ch != null) {
				for (QuadNode c : ch) { if (c != null) { c.dispose(); } }
				children = null;
			}
		}

		// ------------------------------------------------------------------
		// Navigation
		// ------------------------------------------------------------------

		/**
		 * Returns the child index (0–3) for a 2D point using direct arithmetic.
		 *
		 * <p>Index layout: NW=0, NE=1, SW=2, SE=3, i.e.
		 * {@code (py >= halfy ? 2 : 0) | (px >= halfx ? 1 : 0)}.</p>
		 *
		 * @param p the point to locate
		 * @return the child-array index (0, 1, 2, or 3)
		 */
		private int quadrant(final IPoint p) {
			return (p.getY() >= halfy ? 2 : 0) | (p.getX() >= halfx ? 1 : 0);
		}

		// ------------------------------------------------------------------
		// Insertion
		// ------------------------------------------------------------------

		private void add(final IPoint p, final IAgent a) {
			trySplit();
			final QuadNode[] ch = children;
			if (ch == null) {
				objects.put(a, p);
			} else {
				ch[quadrant(p)].add(p, a);
			}
		}

		private void add(final IEnvelope e, final IAgent a) {
			trySplit();
			final QuadNode[] ch = children;
			if (ch == null) {
				objects.put(a, e);
			} else {
				// 2D intersection: env agents may span multiple quadrants
				if (ch[NW].bounds.intersects2D(e)) { ch[NW].add(e, a); }
				if (ch[NE].bounds.intersects2D(e)) { ch[NE].add(e, a); }
				if (ch[SW].bounds.intersects2D(e)) { ch[SW].add(e, a); }
				if (ch[SE].bounds.intersects2D(e)) { ch[SE].add(e, a); }
			}
		}

		private void trySplit() {
			if (children == null && canSplit && objects.size() >= maxCapacity) { split(); }
		}

		// ------------------------------------------------------------------
		// Removal
		// ------------------------------------------------------------------

		private void remove(final IPoint p, final IShape a) {
			final QuadNode[] ch = children;
			if (ch == null) {
				final IIntersectable env = objects.remove(a);
				if (env != null) { env.dispose(); }
			} else {
				ch[quadrant(p)].remove(p, a);
			}
		}

		private void remove(final IEnvelope e, final IShape a) {
			final QuadNode[] ch = children;
			if (ch == null) {
				final IIntersectable env = objects.remove(a);
				if (env != null) { env.dispose(); }
			} else {
				if (ch[NW].bounds.intersects2D(e)) { ch[NW].remove(e, a); }
				if (ch[NE].bounds.intersects2D(e)) { ch[NE].remove(e, a); }
				if (ch[SW].bounds.intersects2D(e)) { ch[SW].remove(e, a); }
				if (ch[SE].bounds.intersects2D(e)) { ch[SE].remove(e, a); }
			}
		}

		// ------------------------------------------------------------------
		// Split
		// ------------------------------------------------------------------

		private void split() {
			try {
				final double maxx = bounds.getMaxX();
				final double minx = bounds.getMinX();
				final double miny = bounds.getMinY();
				final double maxy = bounds.getMaxY();
				final QuadNode[] ch = new QuadNode[4];
				ch[NW] = new QuadNode(GamaEnvelopeFactory.of(minx, halfx, miny, halfy));
				ch[NE] = new QuadNode(GamaEnvelopeFactory.of(halfx, maxx, miny, halfy));
				ch[SW] = new QuadNode(GamaEnvelopeFactory.of(minx, halfx, halfy, maxy));
				ch[SE] = new QuadNode(GamaEnvelopeFactory.of(halfx, maxx, halfy, maxy));
				// Redistribute existing agents directly into the child nodes.
				// MUST NOT call this.add() here: children is still null at this point,
				// so this.add() → this.trySplit() → this.split() again → StackOverflowError.
				// Calling ch[i].add() directly bypasses trySplit() on this node entirely.
				objects.forEach((a, e) -> {
					if (a != null && !a.dead()) {
						final IShape g = a.getGeometry();
						if (g.isPoint()) {
							final IPoint p = g.getLocation();
							ch[quadrant(p)].add(p, a);
						} else {
							final IEnvelope ge = g.getEnvelope();
							if (ch[NW].bounds.intersects2D(ge)) { ch[NW].add(ge, a); }
							if (ch[NE].bounds.intersects2D(ge)) { ch[NE].add(ge, a); }
							if (ch[SW].bounds.intersects2D(ge)) { ch[SW].add(ge, a); }
							if (ch[SE].bounds.intersects2D(ge)) { ch[SE].add(ge, a); }
						}
					}
				});
				// Publish the new children array. Volatile write: any thread that
				// reads a non-null children also sees all writes made above.
				children = ch;
			} finally {
				objects.clear();
			}
		}

		// ------------------------------------------------------------------
		// Query
		// ------------------------------------------------------------------

		/**
		 * Recursively collects all agents whose 2D spatial key intersects the search envelope {@code r}.
		 *
		 * <p>
		 * Both the node-bounds pruning check and the per-object leaf check use
		 * {@link IIntersectable#intersects2D} / the JTS XY-only {@code Envelope.intersects} overload.
		 * Since the quadtree is always 2D, this avoids the Z-range comparisons in
		 * {@link gama.api.utils.geometry.GamaEnvelope#intersects(org.locationtech.jts.geom.Envelope)}.
		 * </p>
		 *
		 * @param r      the 2D search envelope
		 * @param result the collector to which matching agents are added
		 */
		public void findIntersects(final IEnvelope r, final Collection<IAgent> result) {
			// 2D-only node bounds check: skips Z comparison in GamaEnvelope.intersects
			if (!bounds.intersects2D(r)) return;
			final QuadNode[] ch = children;
			if (ch == null) {
				objects.forEach((a, e) -> {
					// 2D-only per-object intersection: point keys use super.intersects(Envelope)
					// (XY only), envelope keys use GamaEnvelope.intersects2D (XY only)
					if (e != null && e.intersects2D(r)) { result.add(a); }
				});
			} else {
				ch[NW].findIntersects(r, result);
				ch[NE].findIntersects(r, result);
				ch[SW].findIntersects(r, result);
				ch[SE].findIntersects(r, result);
			}
		}

	}

}
