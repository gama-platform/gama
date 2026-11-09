/*******************************************************************************************************
 *
 * ImageObject.java, in gama.ui.display.opengl, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.geometry;

import org.locationtech.jts.geom.Geometry;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.ui.layers.IDrawingAttributes;

/** Image support geometry is realized once, on the rendering thread. */
public final class ImageObject extends GeometryObject {

	private Geometry support;

	public ImageObject(final IDrawingAttributes attributes) {
		super(attributes);
	}

	@Override
	public Geometry getObject() {
		if (support == null) {
			final var size = getAttributes().getSize();
			support = GamaShapeFactory.buildRectangle(size.getX(), size.getY(),
					GamaPointFactory.create()).getInnerGeometry();
		}
		return support;
	}
}
