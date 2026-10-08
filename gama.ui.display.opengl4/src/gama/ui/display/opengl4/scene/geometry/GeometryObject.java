/*******************************************************************************************************
 *
 * GeometryObject.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.scene.geometry;

import org.locationtech.jts.geom.Geometry;

import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.ui.display.opengl4.scene.AbstractObject;

/**
 * The Class GeometryObject.
 */
public class GeometryObject extends AbstractObject<Geometry, IDrawingAttributes> {

	/**
	 * Instantiates a new geometry object.
	 *
	 * @param geometry
	 *            the geometry
	 * @param attributes
	 *            the attributes
	 */
	public GeometryObject(final Geometry geometry, final IDrawingAttributes attributes) {
		super(geometry, attributes, DrawerType.GEOMETRY);
	}

	@Override
	public void getTranslationInto(final IPoint p) {
		final IPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			p.setLocation(0, 0, 0);
		} else {
			GamaCoordinateSequenceFactory.pointsOf(getObject()).getCenter(p);
			p.negate();
			p.add(explicitLocation);
		}
	}

	@Override
	public void getTranslationForRotationInto(final IPoint p) {
		final IPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			GamaCoordinateSequenceFactory.pointsOf(getObject()).getCenter(p);
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
		GamaCoordinateSequenceFactory.pointsOf(getObject()).getCenter(p);
	}

}
