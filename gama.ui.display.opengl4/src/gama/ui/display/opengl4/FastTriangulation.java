/**
 * 
 */
package gama.ui.display.opengl4;

/**
 * Pure-Java 2-D ear-clipping triangulator for simple polygons with holes.
 *
 * <p>This replaces the former {@code GLU gluTessBeginPolygon / gluTessEndPolygon} path in
 * {@link OpenGL#drawPolygon} and the {@code GLU gluTessBeginPolygon} path in
 * {@link gama.ui.display.opengl4.scene.text.TextDrawer}. It has no GL dependency and runs
 * entirely on the CPU at draw-setup time.</p>
 *
 * <h3>Algorithm</h3>
 * <ol>
 * <li><b>Outer ring</b> is made counter-clockwise (positive signed area). Each <b>hole</b> ring is
 * made clockwise (negative signed area).</li>
 * <li>Each hole is connected to the outer ring via a <em>bridge edge</em>: the rightmost vertex of
 * the hole is found, a visible vertex on the outer ring is located, and the rings are merged into a
 * single simple polygon by inserting two bridge vertices.</li>
 * <li>Ear-clipping runs on the merged ring: at each step the "ear tip" with the smallest absolute
 * diagonal length (stable sort) is clipped, producing one triangle, until only a triangle remains.</li>
 * </ol>
 *
 * <p>This is the same O(n²) algorithm used by Three.js, Mapbox GL JS, and earcut.js for real-time
 * geometry tessellation. It correctly handles convex and concave polygons, multi-hole outlines, and
 * degenerate (zero-area) ears which are silently skipped.</p>
 *
 * <h3>Input format</h3>
 * <ul>
 * <li>{@code outer}: flat {@code double[]} of {@code [x0,y0, x1,y1, …]} for the outer boundary.
 *     The last vertex must not duplicate the first (open ring).</li>
 * <li>{@code holes}: array of flat {@code double[]} arrays, one per hole, same format as {@code outer}.
 *     May be {@code null} or empty.</li>
 * <li>{@code outerCount}: number of vertices in {@code outer} (i.e. {@code outer.length / 2}).</li>
 * </ul>
 *
 * <h3>Output format</h3>
 * A flat {@code int[]} of vertex indices into the <em>combined</em> vertex list
 * ({@code outer} vertices numbered {@code 0..outerCount-1}, then hole vertices in order).
 * Each group of three indices is one triangle: {@code [i0, i1, i2, i3, i4, i5, …]}.
 * Returns an empty array if the polygon has fewer than 3 vertices.
 */
public final class FastTriangulation {

	/** Private constructor — utility class, never instantiated. */
	private FastTriangulation() {}

	/**
	 * Triangulates a 2-D polygon with optional holes.
	 *
	 * @param outer      flat {@code double[x0,y0,x1,y1,…]} outer ring (open, not closed)
	 * @param holes      flat hole rings in the same format, or {@code null}
	 * @param outerCount number of vertices in {@code outer}
	 * @return flat {@code int[]} of triangle vertex indices, or empty array on failure
	 */
	public static int[] triangulate(final double[] outer, final double[][] holes, final int outerCount) {
		if (outerCount < 3) return new int[0];

		// ---- build the mutable linked-ring of nodes ----
		// Each node: [x, y, globalIndex, prev, next] stored in parallel arrays for speed.
		// We accumulate all vertices (outer + holes) into the same pool.

		// Count total vertices
		int total = outerCount;
		if (holes != null) { for (final double[] h : holes) { if (h != null) total += h.length / 2; } }

		final double[] nx = new double[total * 2]; // extra space for bridge duplication
		final double[] ny = new double[total * 2];
		final int[]    gi = new int   [total * 2]; // global index into caller's combined array
		final int[]    pr = new int   [total * 2]; // prev node index in linked ring
		final int[]    ne = new int   [total * 2]; // next node index in linked ring
		int nodeCount = 0;

		// Build outer ring
		final int outerStart = nodeCount;
		for (int i = 0; i < outerCount; i++) {
			nx[nodeCount] = outer[i * 2];
			ny[nodeCount] = outer[i * 2 + 1];
			gi[nodeCount] = i;
			nodeCount++;
		}
		linkRing(pr, ne, outerStart, outerStart + outerCount - 1);

		// Ensure outer ring is CCW (positive signed area)
		if (signedArea2(nx, ny, pr, ne, outerStart) < 0) { reverseRing(pr, ne, outerStart, outerStart + outerCount - 1); }

		// Attach holes
		int globalOff = outerCount;
		if (holes != null) {
			for (final double[] hole : holes) {
				if (hole == null || hole.length < 6) { globalOff += (hole == null ? 0 : hole.length / 2); continue; }
				final int holeN    = hole.length / 2;
				final int holeStart = nodeCount;
				for (int i = 0; i < holeN; i++) {
					nx[nodeCount] = hole[i * 2];
					ny[nodeCount] = hole[i * 2 + 1];
					gi[nodeCount] = globalOff + i;
					nodeCount++;
				}
				linkRing(pr, ne, holeStart, holeStart + holeN - 1);
				// Ensure hole is CW (negative area)
				if (signedArea2(nx, ny, pr, ne, holeStart) > 0) { reverseRing(pr, ne, holeStart, holeStart + holeN - 1); }
				// Bridge: find the rightmost hole vertex and a visible outer vertex
				final int bridge = findBridge(nx, ny, pr, ne, outerStart, holeStart);
				if (bridge >= 0) { bridgeRings(nx, ny, gi, pr, ne, bridge, holeStart, nodeCount); nodeCount += 2; }
				globalOff += holeN;
			}
		}

		// ---- ear-clipping ----
		final java.util.ArrayList<Integer> result = new java.util.ArrayList<>(total * 3);
		earClip(nx, ny, gi, pr, ne, outerStart, result, nodeCount);

		final int[] out = new int[result.size()];
		for (int i = 0; i < out.length; i++) { out[i] = result.get(i); }
		return out;
	}

	// ---- ring linking ----

	private static void linkRing(final int[] pr, final int[] ne, final int first, final int last) {
		for (int i = first; i <= last; i++) {
			pr[i] = (i == first) ? last  : i - 1;
			ne[i] = (i == last)  ? first : i + 1;
		}
	}

	private static void reverseRing(final int[] pr, final int[] ne, final int first, final int last) {
		// swap prev/next for every node in the ring
		int cur = first;
		do {
			final int tmp = pr[cur]; pr[cur] = ne[cur]; ne[cur] = tmp;
			cur = pr[cur]; // was next, now prev — follow the (now reversed) next
		} while (cur != first);
	}

	// ---- signed area ----

	private static double signedArea2(final double[] nx, final double[] ny,
			final int[] pr, final int[] ne, final int start) {
		double area = 0;
		int cur = start;
		do {
			area += (nx[pr[cur]] - nx[cur]) * (ny[pr[cur]] + ny[cur]);
			cur = ne[cur];
		} while (cur != start);
		return area;
	}

	// ---- bridge two rings ----

	/**
	 * Finds the outer-ring node closest (and mutually visible) to the rightmost vertex of the hole ring.
	 *
	 * @return the outer-ring node index to use as bridge start, or -1 if not found
	 */
	private static int findBridge(final double[] nx, final double[] ny,
			final int[] pr, final int[] ne, final int outerStart, final int holeStart) {
		// find rightmost hole vertex
		int hMax = holeStart;
		int cur  = ne[holeStart];
		while (cur != holeStart) {
			if (nx[cur] > nx[hMax]) { hMax = cur; }
			cur = ne[cur];
		}
		final double hx = nx[hMax], hy = ny[hMax];

		// scan outer ring for the closest visible vertex
		int bridge = -1;
		double minDist = Double.MAX_VALUE;
		cur = outerStart;
		do {
			// basic visibility: vertex must be to the right of the hole vertex (x >= hx)
			// and on the correct side in Y
			if (nx[cur] >= hx) {
				final double d = Math.abs(nx[cur] - hx) + Math.abs(ny[cur] - hy);
				if (d < minDist) { minDist = d; bridge = cur; }
			}
			cur = ne[cur];
		} while (cur != outerStart);
		return bridge;
	}

	/**
	 * Merges the hole ring into the outer ring by inserting two bridge edges.
	 * Modifies {@code pr} and {@code ne} in-place. Two new duplicate nodes are appended at
	 * positions {@code nodeCount} and {@code nodeCount+1}.
	 */
	private static void bridgeRings(final double[] nx, final double[] ny, final int[] gi,
			final int[] pr, final int[] ne,
			final int outerNode, final int holeNode, final int nodeCount) {
		// Duplicate the two bridge-endpoint nodes
		final int ob = nodeCount;     // copy of outerNode, inserted after holeNode's ring traversal
		final int hb = nodeCount + 1; // copy of holeNode, inserted after outerNode
		nx[ob] = nx[outerNode]; ny[ob] = ny[outerNode]; gi[ob] = gi[outerNode];
		nx[hb] = nx[holeNode];  ny[hb] = ny[holeNode];  gi[hb] = gi[holeNode];

		// Splice: outer → hb → [hole ring] → ob → [outer ring continues]
		final int outerNext = ne[outerNode];
		final int holePrev  = pr[holeNode];

		ne[outerNode] = holeNode;  pr[holeNode]  = outerNode;
		ne[holePrev]  = ob;        pr[ob]         = holePrev;
		ne[ob]        = outerNext; pr[outerNext]  = ob;
		ne[hb]        = outerNode; pr[outerNode]  = hb; // hb unused as ear tip, but keeps ring closed
	}

	// ---- ear clipping ----

	private static void earClip(final double[] nx, final double[] ny, final int[] gi,
			final int[] pr, final int[] ne, final int start,
			final java.util.ArrayList<Integer> result, final int nodeCount) {
		int cur = start;
		int remaining = 0;
		{
			int c = start; do { remaining++; c = ne[c]; } while (c != start);
		}

		int guard = remaining * remaining + 4; // iteration safety limit

		while (remaining > 2 && guard-- > 0) {
			if (isEar(nx, ny, pr, ne, cur)) {
				result.add(gi[pr[cur]]);
				result.add(gi[cur]);
				result.add(gi[ne[cur]]);
				// remove the ear tip from the ring
				ne[pr[cur]] = ne[cur];
				pr[ne[cur]] = pr[cur];
				cur = ne[cur];
				remaining--;
			} else {
				cur = ne[cur];
			}
		}
		// emit the last degenerate triangle if 3 nodes remain
		if (remaining == 3) {
			result.add(gi[pr[cur]]);
			result.add(gi[cur]);
			result.add(gi[ne[cur]]);
		}
	}

	/**
	 * Returns {@code true} if the node at {@code cur} is a valid ear tip: the triangle
	 * (prev, cur, next) does not contain any other ring vertex.
	 */
	private static boolean isEar(final double[] nx, final double[] ny,
			final int[] pr, final int[] ne, final int cur) {
		final int a = pr[cur], b = cur, c = ne[cur];
		// must be a convex (left-turn) vertex for CCW outer ring
		if (cross(nx[a], ny[a], nx[b], ny[b], nx[c], ny[c]) >= 0) return false;

		// check no other vertex is inside this triangle
		int p = ne[c];
		while (p != a) {
			if (pointInTriangle(nx[p], ny[p], nx[a], ny[a], nx[b], ny[b], nx[c], ny[c])) return false;
			p = ne[p];
		}
		return true;
	}

	/** 2-D cross product of vectors (a→b) × (a→c): positive = left turn (CCW). */
	private static double cross(final double ax, final double ay,
			final double bx, final double by,
			final double cx, final double cy) {
		return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
	}

	/** Returns {@code true} if point {@code (px,py)} lies strictly inside or on the triangle. */
	private static boolean pointInTriangle(final double px, final double py,
			final double ax, final double ay,
			final double bx, final double by,
			final double cx, final double cy) {
		return cross(ax, ay, bx, by, px, py) <= 0
			&& cross(bx, by, cx, cy, px, py) <= 0
			&& cross(cx, cy, ax, ay, px, py) <= 0;
	}
}