/*******************************************************************************************************
 *
 * ShapeDrawer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.BufferParameters;

import gama.api.additions.delegates.IDrawDelegate;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.exceptions.GamaRuntimeFileException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.IGamaFile;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.ui.displays.DrawingData;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.ICoordinates;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.geometry.Scaling3D;
import gama.api.utils.interfaces.IImageProvider;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class ShapeExecuter.
 */
public class ShapeDrawer implements IDrawDelegate {

	/**
	 * Execute on.
	 *
	 * @param scope
	 *            the scope
	 * @param gr
	 *            the gr
	 * @param data
	 *            the data
	 * @return the rectangle 2 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public Rectangle2D executeOn(final IGraphicsScope scope, final DrawingData data, final IExpression... items)
			throws GamaRuntimeException {
		final IShape shape = GamaShapeFactory.castToShape(scope, items[0].value(scope), false);
		if (shape == null) return null;
		final IDrawingAttributes attributes = computeAttributes(scope, data, shape);
		Geometry gg = shape.getInnerGeometry();
		if (gg == null) return null;

		// Early visibility culling: skip all transform work for shapes outside the visible region
		if (GamaPreferences.Displays.DISPLAY_ONLY_VISIBLE.getValue() && !scope.getExperiment().isHeadless()) {
			final IEnvelope e = shape.getEnvelope();
			try {
				final IEnvelope visible = scope.getGraphics().getVisibleRegion();
				if (visible != null && !visible.intersects(e)) return null;
			} finally {
				e.dispose();
			}
		}

		final ICoordinates ic = GamaCoordinateSequenceFactory.pointsOf(gg);
		ic.ensureClockwiseness();

		// If the graphics is 2D, we pre-translate and pre-rotate the geometry
		if (scope.getGraphics().is2D()) {
			final IPoint center = ic.getCenter();
			final AxisAngle rot = attributes.getRotation();
			final IPoint location = attributes.getLocation();
			if (rot != null) {
				// Do this instead of copy() or clone() to avoid the exception quoted in #3602
				// Seems to work...
				gg = gg.buffer(0.0, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT);
				// Negate the angle to match the OpenGL renderer behavior, which also negates it (see ObjectDrawer#applyRotation)
				GeometryUtils.rotate(gg, center, new AxisAngle(rot.getAxis(), -rot.getAngle()));
			}
			if (location != null) {
				if (gg.getNumPoints() == 1) {
					gg = GeometryUtils.getGeometryFactory().createPoint(location.toCoordinate());
				} else {
					// Copy only if not already copied by the rotation branch above
					if (rot == null) {
						gg = gg.buffer(0.0, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT);
					}
					GeometryUtils.translate(gg, center, location);
				}
			}
		}
		// Items is of length 3 , but let's verify anyway
		if (items.length > 1) {
			final Geometry withArrows = addArrows(scope, gg, items[1], items[2], !attributes.isEmpty());
			if (withArrows != gg) {
				gg = withArrows;
				attributes.setType(IShape.Type.NULL);
			}
		}
		final Geometry withTorus = addToroidalParts(scope, gg);
		if (withTorus != gg) {
			gg = withTorus;
			attributes.setType(IShape.Type.NULL);
		}

		// The textures are computed as well in advance
		addTextures(scope, attributes);
		// And we ask the IGraphics object to draw the shape
		return scope.getGraphics().drawShape(gg, attributes);
	}

	/**
	 * Compute attributes.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @param shape
	 *            the shape
	 * @return the drawing attributes
	 */
	IDrawingAttributes computeAttributes(final IScope scope, final DrawingData data, final IShape shape) {
		Double depth = data.depth.get();
		if (depth == null) { depth = shape.getDepth(); }
		return new ShapeDrawingAttributes(Scaling3D.of(data.size.get()), depth, data.rotation.get(), data.getLocation(),
				data.empty.get(), data.color.get(), /* data.getColors(), */
				data.border.get(), data.texture.get(), /* data.material.get(), */ scope.getAgent(),
				shape.getGeometricalType(), data.lineWidth.get(), data.lighting.get());
	}

	/**
	 * @param scope
	 * @param attributes
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	private void addTextures(final IScope scope, final IDrawingAttributes attributes) {
		if (attributes.getTextures() == null) return;
		attributes.getTextures().replaceAll(s -> {
			IImageProvider image = null;
			if (s instanceof IImageProvider) {
				image = (IImageProvider) s;
			} else if (s instanceof String) {
				IGamaFile file = GamaFileType.createFile(scope, (String) s, false, null);
				if (!(file instanceof IImageProvider iip) || !file.exists(scope))
					throw new GamaRuntimeFileException(scope, "Texture file not found: " + s);
				image = iip;
			}

			return image;

		});
	}

	/**
	 * @param scope
	 * @param shape
	 * @return
	 */
	private Geometry addToroidalParts(final IScope scope, final Geometry shape) {

		// final ITopology t = scope.getTopology();
		// if (t != null && t.isTorus()) {
		// final List<Geometry> geoms = t.listToroidalGeometries(shape);
		// final Geometry all = GeometryUtils.GEOMETRY_FACTORY.buildGeometry(geoms);
		// final Geometry world = scope.getSimulation().getInnerGeometry();
		// result = all.intersection(world);
		// // WARNING Does not correctly handle rotations or translations
		// }
		return shape;
	}

	/** The temp arrow list. */
	// private final List<Geometry> tempArrowList = new ArrayList<>();

	/**
	 * Adds the arrows.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param fill
	 *            the fill
	 * @return the geometry
	 */
	private Geometry addArrows(final IScope scope, final Geometry g1, final IExpression beginArrow,
			final IExpression endArrow, final Boolean fill) {
		if (g1 == null) return g1;
		// if (!(g1 instanceof org.locationtech.jts.geom.Lineal)) return g1;
		final IPoint[] points = GeometryUtils.getPointsOf(g1);
		final int size = points.length;
		if (size < 2) return g1;
		Geometry end = null, begin = null;
		if (endArrow != null) {
			final double width = Cast.asFloat(scope, endArrow.value(scope));
			if (width > 0) {
				end = GamaShapeFactory.buildArrow(points[size - 2], points[size - 1], width, width + width / 3, fill)
						.getInnerGeometry();
			}
		}
		if (beginArrow != null) {
			final double width = Cast.asFloat(scope, beginArrow.value(scope));
			if (width > 0) {
				begin = GamaShapeFactory.buildArrow(points[1], points[0], width, width + width / 3, fill)
						.getInnerGeometry();
			}
		}
		if (end == null) {
			if (begin == null) return g1;
			return GeometryUtils.getGeometryFactory().createCollection(g1, begin);
		}
		if (begin == null) return GeometryUtils.getGeometryFactory().createCollection(g1, end);
		return GeometryUtils.getGeometryFactory().createCollection(g1, end, begin);
	}

	/**
	 * Type drawn.
	 *
	 * @return the i type
	 */
	@Override
	public IType<?> typeDrawn() {
		return Types.GEOMETRY;
	}
}