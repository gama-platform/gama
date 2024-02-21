/*******************************************************************************************************
 *
 * ResourceDrawer.java, in gama.ui.display.opengl, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.resources;

import java.awt.Color;

import gama.core.common.geometry.AxisAngle;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.Scaling3D;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.util.file.GamaGeometryFile;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.scene.ObjectDrawer;

/**
 * The Class ResourceDrawer.
 */
public class ResourceDrawer extends ObjectDrawer<ResourceObject> {

	/**
	 * Instantiates a new resource drawer.
	 *
	 * @param gl the gl
	 */
	public ResourceDrawer(final OpenGL gl) {
		super(gl);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Takes into account the initial rotation of the file
	 */

	@Override
	protected boolean applyRotation(final ResourceObject object) {
		final AxisAngle rotation = object.getAttributes().getRotation();
		final AxisAngle initRotation = object.getObject().getInitRotation();
		if (rotation == null && initRotation == null) return false;
		final GamaPoint loc = object.getAttributes().getLocation();
		try {
			gl.translateBy(loc.x, -loc.y, loc.z);
			if (rotation != null) {
				final GamaPoint axis = rotation.getAxis();
				// AD Change to a negative rotation to fix Issue #1514
				gl.rotateBy(-rotation.getAngle(), axis.x, axis.y, axis.z);
			}
			if (initRotation != null) {
				final GamaPoint initAxis = initRotation.axis;
				// AD Change to a negative rotation to fix Issue #1514
				gl.rotateBy(-initRotation.angle, initAxis.x, initAxis.y, initAxis.z);
			}
		} finally {
			gl.translateBy(-loc.x, loc.y, -loc.z);
		}
		return true;
	}

	@Override
	protected boolean isDrawing2D(final Scaling3D size, final Envelope3D env, final ResourceObject object) {
		return super.isDrawing2D(size, env, object) || object.getObject().is2D();
	}

	@Override
	protected void _draw(final ResourceObject object) {
		final boolean push = object.getAttributes().getRotation() != null
				|| object.getObject().getInitRotation() != null || object.getAttributes().getSize() != null;
		try {
			if (push) {
				gl.pushMatrix();
				applyRotation(object);
				applyTranslation(object);
				applyScaling(object);
			}
			final boolean solid = object.isFilled() || gl.isTextured();
			final Color border = !solid && object.getAttributes().getBorder() == null
					? object.getAttributes().getColor() : object.getAttributes().getBorder();
			final GamaGeometryFile file = object.getObject();
			if (file != null) { gl.drawCachedGeometry(file, border); }
		} finally {
			if (push) { gl.popMatrix(); }
		}

	}

	@Override
	public void dispose() {}

}
