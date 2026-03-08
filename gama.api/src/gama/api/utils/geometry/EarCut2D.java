/*******************************************************************************************************
 *
 * EarCut2D.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;

/**
 * Pure-Java 2-D ear-clipping polygon triangulator.
 *
 * <p>
 * This class is the canonical, dependency-free triangulator used throughout GAMA wherever a polygon needs to be
 * decomposed into triangles at <em>CPU</em> time. It has no OpenGL, GLU, or JOGL dependency and is therefore usable
 * from any layer of the architecture.
 * </p>
 *
 * <h3>Design decisions</h3>
 * <ul>
 * <li>The core algorithm works on <em>flat {@code double[]} coordinate arrays</em> (alternating x, y values, open ring
 * — last point does not repeat the first). This is the fastest possible in-memory representation for the inner
 * loop.</li>
 * <li>High-level overloads accept {@link Polygon}, {@link Coordinate[]}, {@link ICoordinates} and {@link IShape} so
 * that callers never need to manually flatten coordinates.</li>
 * <li>Z values are preserved in the high-level overloads: the triangles returned carry the original elevation of the
 * polygon's vertices.</li>
 * </ul>
 *
 * <h3>Algorithm</h3>
 * <ol>
 * <li>The outer ring is normalised to counter-clockwise (positive signed area) and each hole ring is normalised to
 * clockwise (negative area).</li>
 * <li>Each hole is merged into the outer ring via a <em>bridge edge</em>: the rightmost hole vertex is connected to the
 * nearest visible outer-ring vertex, producing a single simple polygon.</li>
 * <li>Ear-clipping removes one ear (a convex vertex whose triangle contains no other ring vertex) per step until only
 * three vertices remain.</li>
 * </ol>
 *
 * <p>
 * Complexity is O(n²) in the number of vertices; for typical GAMA agent geometries (≤ 200 vertices) this is negligible
 * compared to the overhead of a full Delaunay/Conforming triangulation.
 * </p>
 *
 * <h3>Relationship to other triangulation methods in GAMA</h3>
 * <table>
 * <tr>
 * <th>Method</th>
 * <th>Output</th>
 * <th>Use case</th>
 * </tr>
 * <tr>
 * <td>{@link GeometryUtils#iterateOverTriangles}</td>
 * <td>Delaunay triangles (JTS)</td>
 * <td>Skeletonisation, spatial analysis — quality triangles</td>
 * </tr>
 * <tr>
 * <td>{@link EarCut2D#iterateOverTriangles(Polygon, Consumer)}</td>
 * <td>Ear-clip triangles (no JTS overhead)</td>
 * <td>Real-time rendering, GPU upload — speed matters</td>
 * </tr>
 * <tr>
 * <td>{@link EarCut2D#triangulate(double[], double[][], int)}</td>
 * <td>Flat {@code int[]} index array</td>
 * <td>Direct VBO construction in {@link gama.ui.display.opengl4.OpenGL}</td>
 * </tr>
 * </table>
 *
 * <h3>Thread safety</h3> All methods are stateless and may be called from any thread.
 *
 * @author A. Drogoul — extracted from {@code gama.ui.display.opengl4.OpenGL} 2026-03
 */
public final class EarCut2D {

	/** Private constructor — static utility class, never instantiated. */
	private EarCut2D() {}

	// =========================================================================
	// Public API — flat double[] core
	// =========================================================================

	/**
	 * Triangulates a 2-D polygon with optional holes.
	 *
	 * <p>
	 * All coordinates are expressed as flat interleaved arrays: {@code [x₀,y₀, x₁,y₁, …]}. The rings must be
	 * <em>open</em> (last vertex ≠ first vertex). Z values are not part of the triangulation; if you need to recover Z,
	 * keep the original coordinate arrays alongside the returned index list.
	 * </p>
	 *
	 * <p>
	 * Vertex numbering in the returned index array:
	 * <ul>
	 * <li>Indices {@code 0 .. outerCount-1} refer to {@code outer}.</li>
	 * <li>Indices {@code outerCount .. outerCount+holeN₀-1} refer to the first hole, and so on.</li>
	 * </ul>
	 * </p>
	 *
	 * @param outer
	 *            flat {@code double[x₀,y₀,x₁,y₁,…]} outer ring (open, not closed)
	 * @param holes
	 *            flat hole rings in the same format, or {@code null} if there are no holes
	 * @param outerCount
	 *            number of vertices in {@code outer} (i.e. {@code outer.length / 2})
	 * @return flat {@code int[]} of triangle vertex indices (groups of 3), or empty array on failure
	 */
	public static int[] triangulate(final double[] outer, final double[][] holes, final int outerCount) {
		if (outerCount < 3) return new int[0];

		// Count total vertices (outer + all holes); double the pool for bridge duplicates
		int total = outerCount;
		if (holes != null) { for (final double[] h : holes) { if (h != null) { total += h.length / 2; } } }

		// Node pool: parallel arrays hold x, y, global-index, prev-link, next-link
		final double[] nx = new double[total * 2];
		final double[] ny = new double[total * 2];
		final int[] gi = new int[total * 2];
		final int[] pr = new int[total * 2];
		final int[] ne = new int[total * 2];
		int nodeCount = 0;

		// --- outer ring ---
		final int outerStart = nodeCount;
		for (int i = 0; i < outerCount; i++) {
			nx[nodeCount] = outer[i * 2];
			ny[nodeCount] = outer[i * 2 + 1];
			gi[nodeCount] = i;
			nodeCount++;
		}
		linkRing(pr, ne, outerStart, outerStart + outerCount - 1);
		if (signedArea2(nx, ny, pr, ne, outerStart) < 0) {
			reverseRing(pr, ne, outerStart, outerStart + outerCount - 1);
		}

		// --- attach holes ---
		int globalOff = outerCount;
		if (holes != null) {
			for (final double[] hole : holes) {
				if (hole == null || hole.length < 6) {
					globalOff += hole == null ? 0 : hole.length / 2;
					continue;
				}
				final int holeN = hole.length / 2;
				final int holeStart = nodeCount;
				for (int i = 0; i < holeN; i++) {
					nx[nodeCount] = hole[i * 2];
					ny[nodeCount] = hole[i * 2 + 1];
					gi[nodeCount] = globalOff + i;
					nodeCount++;
				}
				linkRing(pr, ne, holeStart, holeStart + holeN - 1);
				if (signedArea2(nx, ny, pr, ne, holeStart) > 0) {
					reverseRing(pr, ne, holeStart, holeStart + holeN - 1);
				}
				final int bridge = findBridge(nx, ny, pr, ne, outerStart, holeStart);
				if (bridge >= 0) {
					bridgeRings(nx, ny, gi, pr, ne, bridge, holeStart, nodeCount);
					nodeCount += 2;
				}
				globalOff += holeN;
			}
		}

		// --- ear-clip ---
		final ArrayList<Integer> result = new ArrayList<>(total * 3);
		earClip(nx, ny, gi, pr, ne, outerStart, result);

		final int[] out = new int[result.size()];
		for (int i = 0; i < out.length; i++) { out[i] = result.get(i); }
		return out;
	}

	// =========================================================================
	// Public API — JTS Polygon overload
	// =========================================================================

	/**
	 * Triangulates a JTS {@link Polygon} (with any number of holes) and delivers each resulting triangle to
	 * {@code action} as a freshly created {@link Polygon}.
	 *
	 * <p>
	 * The triangles preserve the Z values of the original polygon's vertices (linearly interpolated for
	 * bridge-duplicate nodes).
	 * </p>
	 *
	 * <p>
	 * This is a <strong>fast alternative to {@link GeometryUtils#iterateOverTriangles}</strong> for render-time use: it
	 * runs in O(n²) without any Delaunay-specific overhead and requires no JTS triangulation infrastructure. The
	 * resulting triangles are not Delaunay-optimal (they may be sliver triangles) but are correct for shading and GPU
	 * upload purposes.
	 * </p>
	 *
	 * @param polygon
	 *            the polygon to triangulate (must not be {@code null})
	 * @param factory
	 *            the {@link GeometryFactory} used to create the triangle polygons
	 * @param action
	 *            called once per output triangle
	 */
	public static void iterateOverTriangles(final Polygon polygon, final GeometryFactory factory,
			final Consumer<Geometry> action) {
		// --- outer ring ---
		final Coordinate[] outerCoords = polygon.getExteriorRing().getCoordinates();
		final int outerN = outerCoords.length - 1; // last == first: drop it
		final double[] outer = new double[outerN * 2];
		final double[] outerZ = new double[outerN];
		for (int i = 0; i < outerN; i++) {
			outer[i * 2] = outerCoords[i].getX();
			outer[i * 2 + 1] = outerCoords[i].getY();
			outerZ[i] = outerCoords[i].getZ();
		}

		// --- hole rings ---
		final int holeCount = polygon.getNumInteriorRing();
		final double[][] holes = holeCount == 0 ? null : new double[holeCount][];
		final double[][] holesZ = holeCount == 0 ? null : new double[holeCount][];
		for (int h = 0; h < holeCount; h++) {
			final Coordinate[] hc = polygon.getInteriorRingN(h).getCoordinates();
			final int hn = hc.length - 1;
			holes[h] = new double[hn * 2];
			holesZ[h] = new double[hn];
			for (int i = 0; i < hn; i++) {
				holes[h][i * 2] = hc[i].getX();
				holes[h][i * 2 + 1] = hc[i].getY();
				holesZ[h][i] = hc[i].getZ();
			}
		}

		// --- triangulate ---
		final int[] indices = triangulate(outer, holes, outerN);

		// --- build combined XYZ lookup ---
		int totalV = outerN;
		if (holes != null) { for (final double[] h : holes) { totalV += h.length / 2; } }
		final double[] allX = new double[totalV];
		final double[] allY = new double[totalV];
		final double[] allZ = new double[totalV];
		for (int i = 0; i < outerN; i++) {
			allX[i] = outer[i * 2];
			allY[i] = outer[i * 2 + 1];
			allZ[i] = outerZ[i];
		}
		int off = outerN;
		if (holes != null) {
			for (int h = 0; h < holeCount; h++) {
				final int hn = holes[h].length / 2;
				for (int i = 0; i < hn; i++) {
					allX[off + i] = holes[h][i * 2];
					allY[off + i] = holes[h][i * 2 + 1];
					allZ[off + i] = holesZ[h][i];
				}
				off += hn;
			}
		}

		// --- emit triangles ---
		for (int t = 0; t < indices.length; t += 3) {
			final int i0 = indices[t], i1 = indices[t + 1], i2 = indices[t + 2];
			final Coordinate[] tri =
					{ new Coordinate(allX[i0], allY[i0], allZ[i0]), new Coordinate(allX[i1], allY[i1], allZ[i1]),
							new Coordinate(allX[i2], allY[i2], allZ[i2]), new Coordinate(allX[i0], allY[i0], allZ[i0]) // closed
					};
			action.accept(factory.createPolygon(factory.createLinearRing(tri)));
		}
	}

	// =========================================================================
	// Public API — Coordinate[] overload
	// =========================================================================

	/**
	 * Triangulates a simple polygon given as a JTS {@link Coordinate} array with no holes.
	 *
	 * <p>
	 * The array may be either open ({@code coords[0] != coords[n-1]}) or closed ({@code coords[0] == coords[n-1]}); the
	 * closing duplicate is detected and stripped automatically.
	 * </p>
	 *
	 * @param coords
	 *            the polygon boundary as a {@link Coordinate} array
	 * @param factory
	 *            the {@link GeometryFactory} used to create the triangle polygons
	 * @param action
	 *            called once per output triangle
	 */
	public static void iterateOverTriangles(final Coordinate[] coords, final GeometryFactory factory,
			final Consumer<Geometry> action) {
		if (coords == null || coords.length < 3) return;
		// detect and strip closing duplicate
		final int raw = coords.length;
		final int n = coords[0].equals2D(coords[raw - 1]) ? raw - 1 : raw;
		final double[] flat = new double[n * 2];
		final double[] flatZ = new double[n];
		for (int i = 0; i < n; i++) {
			flat[i * 2] = coords[i].getX();
			flat[i * 2 + 1] = coords[i].getY();
			flatZ[i] = coords[i].getZ();
		}
		final int[] indices = triangulate(flat, null, n);
		for (int t = 0; t < indices.length; t += 3) {
			final int i0 = indices[t], i1 = indices[t + 1], i2 = indices[t + 2];
			final Coordinate[] tri = { new Coordinate(flat[i0 * 2], flat[i0 * 2 + 1], flatZ[i0]),
					new Coordinate(flat[i1 * 2], flat[i1 * 2 + 1], flatZ[i1]),
					new Coordinate(flat[i2 * 2], flat[i2 * 2 + 1], flatZ[i2]),
					new Coordinate(flat[i0 * 2], flat[i0 * 2 + 1], flatZ[i0]) };
			action.accept(factory.createPolygon(factory.createLinearRing(tri)));
		}
	}

	// =========================================================================
	// Public API — ICoordinates / IShape overloads
	// =========================================================================

	/**
	 * Triangulates the boundary of an {@link IShape} (outer ring only, no holes) and delivers each resulting triangle
	 * to {@code action} as an {@link IShape}.
	 *
	 * <p>
	 * This overload is suitable for GAMA-layer code that works with {@link IShape} directly (e.g. agent geometries in
	 * the GIS layer) and does not need to construct a JTS {@link Polygon} first.
	 * </p>
	 *
	 * @param shape
	 *            the shape whose boundary is triangulated
	 * @param factory
	 *            the {@link GeometryFactory} used to create the output triangles
	 * @param action
	 *            called once per output triangle
	 */
	public static void iterateOverTriangles(final IShape shape, final GeometryFactory factory,
			final Consumer<IShape> action) {
		if (shape == null) return;
		final Geometry g = shape.getInnerGeometry();
		if (g instanceof Polygon p) {
			iterateOverTriangles(p, factory,
					tri -> action.accept(gama.api.types.geometry.GamaShapeFactory.createFrom(tri)));
		} else {
			// Delegate to the Coordinate[] path using the outer boundary
			iterateOverTriangles(g.getCoordinates(), factory,
					tri -> action.accept(gama.api.types.geometry.GamaShapeFactory.createFrom(tri)));
		}
	}

	/**
	 * Triangulates an {@link ICoordinates} ring (no holes) and delivers each resulting triangle to {@code action} as
	 * three {@link IPoint} objects in CCW order.
	 *
	 * <p>
	 * This is the lowest-overhead overload for rendering code that only needs raw vertex triples and does not need to
	 * materialise full JTS/GAMA geometry objects.
	 * </p>
	 *
	 * @param ring
	 *            the ring to triangulate (open or closed, detected automatically)
	 * @param action
	 *            called once per output triangle with (p0, p1, p2) in CCW order
	 */
	public static void iterateOverTriangles(final ICoordinates ring, final TriangleConsumer action) {
		if (ring == null) return;
		final int raw = ring.size();
		if (raw < 3) return;
		// detect closing duplicate
		final IPoint first = ring.at(0);
		final IPoint last = ring.at(raw - 1);
		final int n = Math.abs(first.getX() - last.getX()) < 1e-10 && Math.abs(first.getY() - last.getY()) < 1e-10
				? raw - 1 : raw;
		final double[] flat = new double[n * 2];
		final double[] flatZ = new double[n];
		for (int i = 0; i < n; i++) {
			final IPoint p = ring.at(i);
			flat[i * 2] = p.getX();
			flat[i * 2 + 1] = p.getY();
			flatZ[i] = p.getZ();
		}
		final int[] indices = triangulate(flat, null, n);
		for (int t = 0; t < indices.length; t += 3) {
			final int i0 = indices[t], i1 = indices[t + 1], i2 = indices[t + 2];
			action.accept(GamaPointFactory.create(flat[i0 * 2], flat[i0 * 2 + 1], flatZ[i0]),
					GamaPointFactory.create(flat[i1 * 2], flat[i1 * 2 + 1], flatZ[i1]),
					GamaPointFactory.create(flat[i2 * 2], flat[i2 * 2 + 1], flatZ[i2]));
		}
	}

	// =========================================================================
	// Functional interface for triangle consumers
	// =========================================================================

	/**
	 * Callback interface for receiving triangulated output as three {@link IPoint} vertices.
	 *
	 * <p>
	 * Used by {@link EarCut2D#iterateOverTriangles(ICoordinates, TriangleConsumer)} to avoid creating intermediate
	 * geometry objects when only raw vertex coordinates are needed.
	 * </p>
	 */
	@FunctionalInterface
	public interface TriangleConsumer {
		/**
		 * Called once for each output triangle.
		 *
		 * @param p0
		 *            first vertex (CCW order)
		 * @param p1
		 *            second vertex
		 * @param p2
		 *            third vertex
		 */
		void accept(IPoint p0, IPoint p1, IPoint p2);
	}

	// =========================================================================
	// Public API — polygon geometry (area, centroid, triangle list, random point)
	// =========================================================================

	/**
	 * Returns the <em>signed area</em> of a JTS {@link Polygon} computed via the shoelace formula.
	 *
	 * <p>
	 * The sign follows the 2-D right-hand convention: positive for a counter-clockwise exterior ring (as JTS produces
	 * by default), negative for clockwise. Holes are accounted for by subtracting their areas.
	 * </p>
	 *
	 * <p>
	 * Delegates to {@link GeometryUtils#signedArea(IPoint[])} — the canonical shoelace implementation already used
	 * throughout the geometry package.
	 * </p>
	 *
	 * @param polygon
	 *            the polygon (must not be {@code null})
	 * @return signed area (positive = CCW exterior)
	 */
	public static double signedArea(final Polygon polygon) {
		double area = GeometryUtils
				.signedArea(GamaCoordinateSequenceFactory.pointsOf(polygon.getExteriorRing()).toPointsArray());
		for (int h = 0; h < polygon.getNumInteriorRing(); h++) {
			area -= GeometryUtils
					.signedArea(GamaCoordinateSequenceFactory.pointsOf(polygon.getInteriorRingN(h)).toPointsArray());
		}
		return area;
	}

	/**
	 * Computes the <em>centroid</em> of a JTS {@link Polygon} (with optional holes) using ear-clip triangulation
	 * combined with triangle-area weighting.
	 *
	 * <p>
	 * For concave polygons and polygons with holes this is more accurate than the vertex centroid and avoids creating
	 * JTS geometry objects.
	 * </p>
	 *
	 * <p>
	 * If the polygon is degenerate (zero area), returns the midpoint of its envelope.
	 * </p>
	 *
	 * @param polygon
	 *            the polygon (must not be {@code null})
	 * @return an {@link IPoint} at the polygon's centroid, z = area-weighted average elevation
	 */
	public static IPoint centroid(final Polygon polygon) {
		final List<double[]> tris = toTriangleList(polygon);
		if (tris.isEmpty()) {
			final org.locationtech.jts.geom.Envelope env = polygon.getEnvelopeInternal();
			return GamaPointFactory.create(env.centre().x, env.centre().y, 0);
		}
		double cx = 0, cy = 0, cz = 0, totalArea = 0;
		for (final double[] t : tris) {
			final double a = Math.abs(triangleSignedArea(t[0], t[1], t[3], t[4], t[6], t[7]));
			cx += (t[0] + t[3] + t[6]) * a;
			cy += (t[1] + t[4] + t[7]) * a;
			cz += (t[2] + t[5] + t[8]) * a;
			totalArea += a;
		}
		if (totalArea == 0) return GamaPointFactory.create(0, 0, 0);
		return GamaPointFactory.create(cx / (3 * totalArea), cy / (3 * totalArea), cz / (3 * totalArea));
	}

	/**
	 * Triangulates the polygon and returns all triangles as a list of flat {@code double[9]} arrays, each of the form
	 * {@code [x0,y0,z0, x1,y1,z1, x2,y2,z2]}.
	 *
	 * <p>
	 * This is the lowest-overhead representation for physics engines (convex decomposition seeding) and any code that
	 * needs raw triangle vertex data without materialising JTS or GAMA geometry objects.
	 * </p>
	 *
	 * @param polygon
	 *            the polygon to triangulate (must not be {@code null})
	 * @return a {@link List} of {@code double[9]} triangle arrays, CCW vertex order
	 */
	public static List<double[]> toTriangleList(final Polygon polygon) {
		final List<double[]> result = new ArrayList<>();

		// outer ring
		final Coordinate[] outerCoords = polygon.getExteriorRing().getCoordinates();
		final int outerN = outerCoords.length - 1; // drop closing duplicate
		final double[] outer = new double[outerN * 2];
		final double[] outerZ = new double[outerN];
		for (int i = 0; i < outerN; i++) {
			outer[i * 2] = outerCoords[i].getX();
			outer[i * 2 + 1] = outerCoords[i].getY();
			outerZ[i] = outerCoords[i].getZ();
		}

		// hole rings
		final int holeCount = polygon.getNumInteriorRing();
		final double[][] holes = holeCount == 0 ? null : new double[holeCount][];
		final double[][] holesZ = holeCount == 0 ? null : new double[holeCount][];
		for (int h = 0; h < holeCount; h++) {
			final Coordinate[] hc = polygon.getInteriorRingN(h).getCoordinates();
			final int hn = hc.length - 1;
			holes[h] = new double[hn * 2];
			holesZ[h] = new double[hn];
			for (int i = 0; i < hn; i++) {
				holes[h][i * 2] = hc[i].getX();
				holes[h][i * 2 + 1] = hc[i].getY();
				holesZ[h][i] = hc[i].getZ();
			}
		}

		// triangulate
		final int[] indices = triangulate(outer, holes, outerN);

		// combined vertex lookup
		int totalV = outerN;
		if (holes != null) { for (final double[] h : holes) { totalV += h.length / 2; } }
		final double[] allX = new double[totalV];
		final double[] allY = new double[totalV];
		final double[] allZ = new double[totalV];
		for (int i = 0; i < outerN; i++) {
			allX[i] = outer[i * 2];
			allY[i] = outer[i * 2 + 1];
			allZ[i] = outerZ[i];
		}
		int off = outerN;
		if (holes != null) {
			for (int h = 0; h < holeCount; h++) {
				final int hn = holes[h].length / 2;
				for (int i = 0; i < hn; i++) {
					allX[off + i] = holes[h][i * 2];
					allY[off + i] = holes[h][i * 2 + 1];
					allZ[off + i] = holesZ[h][i];
				}
				off += hn;
			}
		}

		// collect triangles as flat [x0,y0,z0, x1,y1,z1, x2,y2,z2]
		for (int t = 0; t < indices.length; t += 3) {
			final int i0 = indices[t], i1 = indices[t + 1], i2 = indices[t + 2];
			result.add(new double[] { allX[i0], allY[i0], allZ[i0], allX[i1], allY[i1], allZ[i1], allX[i2], allY[i2],
					allZ[i2] });
		}
		return result;
	}

	/**
	 * Returns a uniformly random point that is guaranteed to lie strictly inside the polygon, including polygons with
	 * holes and highly concave polygons.
	 *
	 * <h3>Algorithm</h3>
	 * <ol>
	 * <li>Triangulate the polygon using ear-clipping (O(n²), once per call).</li>
	 * <li>Pick one triangle with probability proportional to its area (O(n) scan).</li>
	 * <li>Generate a uniform random point inside that triangle using the barycentric formula:
	 * {@code P = (1-√r₁)·A + √r₁·(1-r₂)·B + √r₁·r₂·C}.</li>
	 * </ol>
	 *
	 * <p>
	 * This is O(n) per call and always succeeds — unlike rejection sampling which may iterate indefinitely for thin,
	 * concave, or holed polygons.
	 * </p>
	 *
	 * <p>
	 * The two uniform random inputs must each be in {@code [0, 1)}, typically obtained via
	 * {@code scope.getRandom().between(0.0, 1.0)}.
	 * </p>
	 *
	 * @param polygon
	 *            the polygon (must not be {@code null}, must have positive area)
	 * @param r1
	 *            first uniform random value in {@code [0, 1)}
	 * @param r2
	 *            second uniform random value in {@code [0, 1)}
	 * @return a uniformly distributed point inside the polygon; z = barycentric blend of vertex elevations
	 */
	public static IPoint randomPointIn(final Polygon polygon, final double r1, final double r2) {
		final List<double[]> tris = toTriangleList(polygon);
		if (tris.isEmpty()) {
			final org.locationtech.jts.geom.Point c = polygon.getCentroid();
			return GamaPointFactory.create(c.getX(), c.getY(), 0);
		}

		// build cumulative area array for weighted random selection
		final double[] cumArea = new double[tris.size()];
		double total = 0;
		for (int i = 0; i < tris.size(); i++) {
			final double[] t = tris.get(i);
			total += Math.abs(triangleSignedArea(t[0], t[1], t[3], t[4], t[6], t[7]));
			cumArea[i] = total;
		}

		// pick triangle proportional to area using r1
		final double target = r1 * total;
		int picked = tris.size() - 1;
		for (int i = 0; i < cumArea.length; i++) {
			if (cumArea[i] >= target) {
				picked = i;
				break;
			}
		}

		// uniform point inside the picked triangle via barycentric coordinates
		// Standard formula: P = (1-sqrt(r1))*A + sqrt(r1)*(1-r2)*B + sqrt(r1)*r2*C
		// We reuse r1/r2 — no extra RNG call needed.
		final double[] t = tris.get(picked);
		final double sqR1 = Math.sqrt(r1);
		final double u = 1.0 - sqR1;
		final double v = sqR1 * (1.0 - r2);
		final double w = sqR1 * r2;
		return GamaPointFactory.create(u * t[0] + v * t[3] + w * t[6], u * t[1] + v * t[4] + w * t[7],
				u * t[2] + v * t[5] + w * t[8]);
	}

	// =========================================================================
	// Private geometry helpers
	// =========================================================================

	/**
	 * Returns the signed area of the triangle {@code (ax,ay)–(bx,by)–(cx,cy)}. Positive = CCW, negative = CW. Delegates
	 * to {@link #cross} which computes the same value × 2.
	 *
	 * @param ax
	 *            x of vertex a @param ay y of vertex a
	 * @param bx
	 *            x of vertex b @param by y of vertex b
	 * @param cx
	 *            x of vertex c @param cy y of vertex c
	 * @return signed triangle area
	 */
	private static double triangleSignedArea(final double ax, final double ay, final double bx, final double by,
			final double cx, final double cy) {
		return cross(ax, ay, bx, by, cx, cy) / 2.0;
	}

	// =========================================================================
	// Core algorithm — private implementation
	// =========================================================================

	/**
	 * Links nodes {@code first..last} (inclusive, 0-based indices into the node pool) into a doubly-linked circular
	 * ring.
	 *
	 * @param pr
	 *            prev-link array (modified in place)
	 * @param ne
	 *            next-link array (modified in place)
	 * @param first
	 *            index of the first node
	 * @param last
	 *            index of the last node
	 */
	private static void linkRing(final int[] pr, final int[] ne, final int first, final int last) {
		for (int i = first; i <= last; i++) {
			pr[i] = i == first ? last : i - 1;
			ne[i] = i == last ? first : i + 1;
		}
	}

	/**
	 * Reverses the traversal direction of a linked ring by swapping every node's prev/next pointers.
	 *
	 * @param pr
	 *            prev-link array (modified in place)
	 * @param ne
	 *            next-link array (modified in place)
	 * @param first
	 *            index of the first node in the ring
	 * @param last
	 *            index of the last node in the ring (only used to set the start; the whole ring is walked via the
	 *            links)
	 */
	private static void reverseRing(final int[] pr, final int[] ne, final int first, final int last) {
		int cur = first;
		do {
			final int tmp = pr[cur];
			pr[cur] = ne[cur];
			ne[cur] = tmp;
			cur = pr[cur]; // follow the (now-reversed) next link
		} while (cur != first);
	}

	/**
	 * Computes twice the signed area of the ring starting at {@code start} using the shoelace formula on the
	 * linked-ring traversal order. Positive = CCW, negative = CW.
	 *
	 * @param nx
	 *            x-coordinate array of the node pool
	 * @param ny
	 *            y-coordinate array of the node pool
	 * @param pr
	 *            prev-link array
	 * @param ne
	 *            next-link array
	 * @param start
	 *            index of any node in the ring
	 * @return twice the signed area (sign indicates winding direction)
	 */
	private static double signedArea2(final double[] nx, final double[] ny, final int[] pr, final int[] ne,
			final int start) {
		double area = 0;
		int cur = start;
		do {
			area += (nx[pr[cur]] - nx[cur]) * (ny[pr[cur]] + ny[cur]);
			cur = ne[cur];
		} while (cur != start);
		return area;
	}

	/**
	 * Finds the outer-ring node that is the best candidate for bridging to the given hole ring.
	 *
	 * <p>
	 * The strategy is: find the rightmost vertex of the hole, then scan the outer ring for the nearest vertex with
	 * {@code x ≥ hx} (i.e. to the right of or at the hole vertex). This is a heuristic O(n) visibility test — it is
	 * correct for convex outer rings and a good approximation for concave ones.
	 * </p>
	 *
	 * @param nx
	 *            x-coordinate array of the node pool
	 * @param ny
	 *            y-coordinate array of the node pool
	 * @param pr
	 *            prev-link array
	 * @param ne
	 *            next-link array
	 * @param outerStart
	 *            index of the first outer-ring node
	 * @param holeStart
	 *            index of the first hole-ring node
	 * @return the outer-ring node index to use as bridge start, or {@code -1} if none found
	 */
	private static int findBridge(final double[] nx, final double[] ny, final int[] pr, final int[] ne,
			final int outerStart, final int holeStart) {
		// locate rightmost hole vertex
		int hMax = holeStart;
		int cur = ne[holeStart];
		while (cur != holeStart) {
			if (nx[cur] > nx[hMax]) { hMax = cur; }
			cur = ne[cur];
		}
		final double hx = nx[hMax];
		final double hy = ny[hMax];

		// find closest outer vertex with x >= hx
		int bridge = -1;
		double minD = Double.MAX_VALUE;
		cur = outerStart;
		do {
			if (nx[cur] >= hx) {
				final double d = Math.abs(nx[cur] - hx) + Math.abs(ny[cur] - hy);
				if (d < minD) {
					minD = d;
					bridge = cur;
				}
			}
			cur = ne[cur];
		} while (cur != outerStart);
		return bridge;
	}

	/**
	 * Merges the hole ring starting at {@code holeStart} into the outer ring at {@code outerNode} by inserting two
	 * bridge (duplicate) nodes at positions {@code nodeCount} and {@code nodeCount+1}.
	 *
	 * <p>
	 * After this operation the two originally separate rings are fused into a single simple polygon ring that can be
	 * ear-clipped as usual.
	 * </p>
	 *
	 * @param nx
	 *            x-coordinate array (extended at nodeCount and nodeCount+1)
	 * @param ny
	 *            y-coordinate array (extended)
	 * @param gi
	 *            global-index array (extended)
	 * @param pr
	 *            prev-link array (modified in place)
	 * @param ne
	 *            next-link array (modified in place)
	 * @param outerNode
	 *            the outer-ring node that will be the bridge start
	 * @param holeNode
	 *            the hole-ring node that will be the bridge end
	 * @param nodeCount
	 *            current number of used nodes (two new nodes are written here)
	 */
	private static void bridgeRings(final double[] nx, final double[] ny, final int[] gi, final int[] pr,
			final int[] ne, final int outerNode, final int holeNode, final int nodeCount) {
		final int ob = nodeCount; // duplicate of outerNode, appended after hole traversal
		final int hb = nodeCount + 1; // duplicate of holeNode, appended before outerNode

		nx[ob] = nx[outerNode];
		ny[ob] = ny[outerNode];
		gi[ob] = gi[outerNode];
		nx[hb] = nx[holeNode];
		ny[hb] = ny[holeNode];
		gi[hb] = gi[holeNode];

		// Re-splice: outerNode → holeNode → … hole ring … → ob → outerNext
		// hb ← outerNode (closing)
		final int outerNext = ne[outerNode];
		final int holePrev = pr[holeNode];

		ne[outerNode] = holeNode;
		pr[holeNode] = outerNode;
		ne[holePrev] = ob;
		pr[ob] = holePrev;
		ne[ob] = outerNext;
		pr[outerNext] = ob;
		ne[hb] = outerNode;
		pr[outerNode] = hb;
	}

	/**
	 * Core ear-clipping loop. Walks the linked ring starting at {@code start}, clips one ear per iteration, and appends
	 * the three vertex global-indices to {@code result}.
	 *
	 * <p>
	 * A safety counter ({@code guard = n² + 4}) prevents infinite loops on degenerate input.
	 * </p>
	 *
	 * @param nx
	 *            x-coordinate array
	 * @param ny
	 *            y-coordinate array
	 * @param gi
	 *            global-index array
	 * @param pr
	 *            prev-link array (modified in place)
	 * @param ne
	 *            next-link array (modified in place)
	 * @param start
	 *            index of any node in the ring to start from
	 * @param result
	 *            accumulator for triangle vertex indices
	 */
	private static void earClip(final double[] nx, final double[] ny, final int[] gi, final int[] pr, final int[] ne,
			final int start, final ArrayList<Integer> result) {
		int cur = start;
		// count ring size
		int remaining = 0;
		{
			int c = start;
			do {
				remaining++;
				c = ne[c];
			} while (c != start);
		}

		int guard = remaining * remaining + 4;
		while (remaining > 2 && guard-- > 0) {
			if (isEar(nx, ny, pr, ne, cur)) {
				result.add(gi[pr[cur]]);
				result.add(gi[cur]);
				result.add(gi[ne[cur]]);
				ne[pr[cur]] = ne[cur];
				pr[ne[cur]] = pr[cur];
				cur = ne[cur];
				remaining--;
			} else {
				cur = ne[cur];
			}
		}
		// emit the final triangle when exactly 3 nodes remain
		if (remaining == 3) {
			result.add(gi[pr[cur]]);
			result.add(gi[cur]);
			result.add(gi[ne[cur]]);
		}
	}

	/**
	 * Returns {@code true} if the ring node at {@code cur} is a valid ear tip.
	 *
	 * <p>
	 * A node is an ear tip if:
	 * <ol>
	 * <li>The triangle formed by its previous, current, and next neighbours is convex (left turn = negative cross
	 * product for CCW ring).</li>
	 * <li>No other ring vertex lies strictly inside that triangle.</li>
	 * </ol>
	 * </p>
	 *
	 * @param nx
	 *            x-coordinate array
	 * @param ny
	 *            y-coordinate array
	 * @param pr
	 *            prev-link array
	 * @param ne
	 *            next-link array
	 * @param cur
	 *            the node to test
	 * @return {@code true} if {@code cur} is a valid ear tip
	 */
	private static boolean isEar(final double[] nx, final double[] ny, final int[] pr, final int[] ne, final int cur) {
		final int a = pr[cur], b = cur, c = ne[cur];
		// convexity check: cross product of (a→b) × (a→c) must be negative (CCW ring = left-turns)
		if (cross(nx[a], ny[a], nx[b], ny[b], nx[c], ny[c]) >= 0) return false;
		// containment check: no other vertex may be inside the ear triangle
		int p = ne[c];
		while (p != a) {
			if (pointInTriangle(nx[p], ny[p], nx[a], ny[a], nx[b], ny[b], nx[c], ny[c])) return false;
			p = ne[p];
		}
		return true;
	}

	/**
	 * Returns the 2-D signed cross product {@code (b-a) × (c-a)}. Positive = left turn (CCW), negative = right turn
	 * (CW), zero = collinear.
	 *
	 * @param ax
	 *            x of point a
	 * @param ay
	 *            y of point a
	 * @param bx
	 *            x of point b
	 * @param by
	 *            y of point b
	 * @param cx
	 *            x of point c
	 * @param cy
	 *            y of point c
	 * @return signed cross product
	 */
	public static double cross(final double ax, final double ay, final double bx, final double by, final double cx,
			final double cy) {
		return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
	}

	/**
	 * Returns {@code true} if point {@code (px, py)} lies inside or on the boundary of the triangle {@code (a, b, c)},
	 * using the cross-product half-plane test.
	 *
	 * @param px
	 *            x of the point to test
	 * @param py
	 *            y of the point to test
	 * @param ax
	 *            x of triangle vertex a
	 * @param ay
	 *            y of triangle vertex a
	 * @param bx
	 *            x of triangle vertex b
	 * @param by
	 *            y of triangle vertex b
	 * @param cx
	 *            x of triangle vertex c
	 * @param cy
	 *            y of triangle vertex c
	 * @return {@code true} if the point is inside or on the triangle
	 */
	public static boolean pointInTriangle(final double px, final double py, final double ax, final double ay,
			final double bx, final double by, final double cx, final double cy) {
		return cross(ax, ay, bx, by, px, py) <= 0 && cross(bx, by, cx, cy, px, py) <= 0
				&& cross(cx, cy, ax, ay, px, py) <= 0;
	}
}
