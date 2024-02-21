/*******************************************************************************************************
 *
 * GeometryObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.geometry;

import org.locationtech.jts.geom.Geometry;

import gama.core.common.geometry.GeometryUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.DrawingAttributes.DrawerType;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class GeometryObject.
 */
public class GeometryObject extends AbstractObject<Geometry, DrawingAttributes> {

	/**
	 * Instantiates a new geometry object.
	 *
	 * @param geometry
	 *            the geometry
	 * @param attributes
	 *            the attributes
	 */
	public GeometryObject(final Geometry geometry, final DrawingAttributes attributes) {
		super(geometry, attributes, DrawerType.GEOMETRY);
	}

	@Override
	public void getTranslationInto(final GamaPoint p) {
		final GamaPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			p.setLocation(0, 0, 0);
		} else {
			GeometryUtils.getContourCoordinates(getObject()).getCenter(p);
			p.negate();
			p.add(explicitLocation);
		}
	}

	@Override
	public void getTranslationForRotationInto(final GamaPoint p) {
		final GamaPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			// System.out.println(GeometryUtils.getContourCoordinates(getObject()).getEnvelope());
			GeometryUtils.getContourCoordinates(getObject()).getCenter(p);
			Double depth = getAttributes().getDepth();
			if (depth != null) {
				switch (getAttributes().type) {
					case SPHERE:
						p.z += depth;
						break;
					case CYLINDER:
					case PYRAMID:
					case CONE:
					case BOX:
					case CUBE:
						p.z += depth / 2;
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
	public void getTranslationForScalingInto(final GamaPoint p) {
		GeometryUtils.getContourCoordinates(getObject()).getCenter(p);
	}

}
