/*******************************************************************************************************
 *
 * PolygonTessellationCache.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and
 * simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer.caches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Polygon;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;

import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.ICoordinates;
import gama.dev.DEBUG;
import gama.ui.display.opengl.ITesselator;

/**
 * Renderer-local CPU tessellations. Keys snapshot every contour, including Z, rather than retaining mutable JTS
 * geometries. Drawing state and texture coordinates are deliberately applied only when replaying the primitives.
 */
public final class PolygonTessellationCache implements ITesselator {

	private static final class Contours {

		final double[][][] vertices;
		final int hash;

		Contours(final Polygon polygon, final ICoordinates exterior) {
			final List<double[][]> rings = new ArrayList<>();
			final List<double[]> points = new ArrayList<>();
			exterior.visitClockwise(points::add);
			rings.add(points.toArray(double[][]::new));
			GeometryUtils.applyToInnerGeometries(polygon, ring -> {
				points.clear();
				GamaCoordinateSequenceFactory.pointsOf(ring).visitYNegatedCounterClockwise(points::add);
				rings.add(points.toArray(double[][]::new));
			});
			vertices = rings.toArray(double[][][]::new);
			hash = Arrays.deepHashCode(vertices);
		}

		@Override
		public int hashCode() { return hash; }

		@Override
		public boolean equals(final Object other) {
			return other instanceof Contours c && Arrays.deepEquals(vertices, c.vertices);
		}
	}

	private record Primitive(int type, double[] vertices) {}

	private record Mesh(List<Primitive> primitives, int weight) {

		void draw(final ITesselator target) {
			for (final Primitive primitive : primitives) {
				target.beginDrawing(primitive.type());
				final double[] v = primitive.vertices();
				for (int i = 0; i < v.length; i += 3) {
					target.drawVertex(0, v[i], v[i + 1], v[i + 2]);
				}
				target.endDrawing();
			}
		}
	}

	private final Cache<Contours, Mesh> meshes = CacheBuilder.newBuilder().maximumWeight(16 * 1024 * 1024)
			.weigher((Contours key, Mesh mesh) -> mesh.weight()).build();

	private final GLUtessellator tessellator = GLU.gluNewTess();
	private final List<Primitive> primitives = new ArrayList<>();
	private double[] vertices = new double[96];
	private int size, type;
	private boolean failed;

	public PolygonTessellationCache() {
		GLU.gluTessCallback(tessellator, GLU.GLU_TESS_BEGIN, this);
		GLU.gluTessCallback(tessellator, GLU.GLU_TESS_VERTEX, this);
		GLU.gluTessCallback(tessellator, GLU.GLU_TESS_END, this);
		GLU.gluTessCallback(tessellator, GLU.GLU_TESS_ERROR, this);
		GLU.gluTessProperty(tessellator, GLU.GLU_TESS_TOLERANCE, 0.1);
	}

	public void draw(final Polygon polygon, final ICoordinates exterior, final ITesselator target) {
		final Contours key = new Contours(polygon, exterior);
		Mesh mesh = meshes.getIfPresent(key);
		if (mesh == null) {
			primitives.clear();
			failed = false;
			try {
				GLU.gluTessBeginPolygon(tessellator, null);
				for (final double[][] ring : key.vertices) {
					GLU.gluTessBeginContour(tessellator);
					for (final double[] point : ring) { GLU.gluTessVertex(tessellator, point, 0, point); }
					GLU.gluTessEndContour(tessellator);
				}
				GLU.gluTessEndPolygon(tessellator);
				long weight = 256;
				for (final double[][] ring : key.vertices) { weight += 32L + ring.length * 48L; }
				for (final Primitive primitive : primitives) {
					weight += 48L + primitive.vertices().length * (long) Double.BYTES;
				}
				mesh = new Mesh(List.copyOf(primitives), (int) Math.min(Integer.MAX_VALUE, weight));
				if (!failed) { meshes.put(key, mesh); }
			} finally {
				primitives.clear();
			}
		}
		mesh.draw(target);
	}

	@Override
	public void beginDrawing(final int primitiveType) {
		type = primitiveType;
		size = 0;
	}

	@Override
	public void drawVertex(final int index, final double x, final double y, final double z) {
		if (size + 3 > vertices.length) { vertices = Arrays.copyOf(vertices, vertices.length * 2); }
		vertices[size++] = x;
		vertices[size++] = y;
		vertices[size++] = z;
	}

	@Override
	public void endDrawing() {
		primitives.add(new Primitive(type, Arrays.copyOf(vertices, size)));
	}

	@Override
	public void error(final int code) {
		failed = true;
		DEBUG.ERR("OpenGL polygon tessellation: " + new GLU().gluErrorString(code));
	}

	public void dispose() {
		meshes.invalidateAll();
		primitives.clear();
		vertices = new double[0];
		GLU.gluDeleteTess(tessellator);
	}
}
