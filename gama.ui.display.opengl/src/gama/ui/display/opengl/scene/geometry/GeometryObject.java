/*******************************************************************************************************
 *
 * GeometryObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.geometry;

import org.locationtech.jts.geom.Geometry;

import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.displays.GeometryDrawing;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class GeometryObject.
 */
public class GeometryObject extends AbstractObject<Geometry, IDrawingAttributes> {

	private final GeometryDrawing command;
	private IPoint center;

	/**
	 * Instantiates a new geometry object.
	 *
	 * @param geometry
	 *            the geometry
	 * @param attributes
	 *            the attributes
	 */
	public GeometryObject(final Geometry geometry, final IDrawingAttributes attributes) {
		this(new GeometryDrawing(geometry, 0, 0, !attributes.isEmpty()), attributes);
	}

	public GeometryObject(final GeometryDrawing command, final IDrawingAttributes attributes) {
		super(null, attributes, DrawerType.GEOMETRY);
		this.command = command;
	}

	protected GeometryObject(final IDrawingAttributes attributes) {
		super(null, attributes, DrawerType.GEOMETRY);
		command = null;
	}

	@Override
	public Geometry getObject() { return command == null ? super.getObject() : command.realize(); }

	protected void getCenterInto(final IPoint point) {
		if (center == null) { center = GamaCoordinateSequenceFactory.pointsOf(getObject()).getCenter(); }
		point.setLocation(center);
	}

	public double getDrawingDepth() {
		final Double depth = getAttributes().getDepth();
		return depth == null ? 0d : depth;
	}

	@Override
	public void getTranslationInto(final IPoint p) {
		final IPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			p.setLocation(0, 0, 0);
		} else {
			getCenterInto(p);
			p.negate();
			p.add(explicitLocation);
		}
	}

	@Override
	public void getTranslationForRotationInto(final IPoint p) {
		final IPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			getCenterInto(p);
			Double depth = getAttributes().getDepth();
			if (depth != null) {
				switch (getAttributes().getType()) {
					case SPHERE:
						p.setZ(p.getZ() + depth);
						break;
					case CYLINDER:
					case PYRAMID:
					case CONE:
					case BOX:
					case CUBE:
						p.setZ(p.getZ() + depth / 2);
						break;
					default:
						break;
				}
			}
		} else {
			p.setLocation(explicitLocation);
		}
	}

	@Override
	public void getTranslationForScalingInto(final IPoint p) {
		getCenterInto(p);
	}

}
