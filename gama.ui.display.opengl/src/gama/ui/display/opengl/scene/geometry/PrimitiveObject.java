/*******************************************************************************************************
 *
 * PrimitiveObject.java, in gama.ui.display.opengl, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.geometry;

import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;

import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape.Type;
import gama.api.ui.layers.IDrawingAttributes;

/** A per-scene placement of renderer-owned, immutable unit geometry. */
public final class PrimitiveObject extends GeometryObject {

	private final double scale, x, y, z;
	private final Function<Type, Geometry> geometry;

	public PrimitiveObject(final Geometry unitGeometry, final double scale, final IPoint center,
			final IDrawingAttributes attributes) {
		this(type -> unitGeometry, scale, center, attributes);
	}

	public PrimitiveObject(final Function<Type, Geometry> geometry,
			final double scale, final IPoint center, final IDrawingAttributes attributes) {
		super(attributes);
		this.geometry = geometry;
		this.scale = scale;
		x = center.getX();
		y = center.getY();
		z = center.getZ();
	}

	@Override
	public Geometry getObject() {
		return geometry.apply(getAttributes().getType());
	}

	public double getUnitExtent() {
		return switch (getAttributes().getType()) {
			case CIRCLE, SPHERE -> 2;
			default -> 1;
		};
	}

	@Override
	public double getGeometryScale() { return scale; }

	@Override
	public double getDrawingDepth() { return super.getDrawingDepth() / scale; }

	@Override
	public void getTranslationInto(final IPoint point) {
		final IPoint location = getAttributes().getLocation();
		if (location == null) {
			point.setLocation(x, y, z);
		} else {
			point.setLocation(location);
		}
	}

	@Override
	public void getTranslationForRotationInto(final IPoint point) {
		getTranslationInto(point);
		if (getAttributes().getLocation() == null) {
			final Double depth = getAttributes().getDepth();
			if (depth != null) {
				switch (getAttributes().getType()) {
					case SPHERE -> point.setZ(z + depth);
					case CUBE -> point.setZ(z + depth / 2);
					default -> {}
				}
			}
		}
	}

	@Override
	public void getTranslationForScalingInto(final IPoint point) { point.setLocation(0, 0, 0); }
}
