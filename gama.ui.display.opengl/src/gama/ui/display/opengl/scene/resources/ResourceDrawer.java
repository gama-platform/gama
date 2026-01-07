/*******************************************************************************************************
 *
 * ResourceDrawer.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.resources;

import gama.core.common.geometry.AxisAngle;
import gama.core.common.geometry.IEnvelope;
import gama.core.common.geometry.Scaling3D;
import gama.core.metamodel.shape.IPoint;
import gama.core.util.IColor;
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
	 * @param gl
	 *            the gl
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
		final IPoint loc = object.getAttributes().getLocation();
		double lx = loc.getX();
		double ly = loc.getY();
		double lz = loc.getZ();
		try {
			gl.translateBy(lx, -ly, lz);
			if (rotation != null) {
				final IPoint axis = rotation.getAxis();
				// AD Change to a negative rotation to fix Issue #1514
				gl.rotateBy(-rotation.getAngle(), axis.getX(), axis.getY(), axis.getZ());
			}
			if (initRotation != null) {
				final IPoint initAxis = initRotation.axis;
				// AD Change to a negative rotation to fix Issue #1514
				gl.rotateBy(-initRotation.angle, initAxis.getX(), initAxis.getY(), initAxis.getZ());
			}
		} finally {
			gl.translateBy(-lx, ly, -lz);
		}
		return true;
	}

	@Override
	protected boolean isDrawing2D(final Scaling3D size, final IEnvelope env, final ResourceObject object) {
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
			final IColor border = !solid && object.getAttributes().getBorder() == null
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
