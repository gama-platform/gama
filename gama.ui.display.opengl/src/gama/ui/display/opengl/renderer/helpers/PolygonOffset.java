package gama.ui.display.opengl.renderer.helpers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;

import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass;

/** Temporarily moves polygon fills away from their coplanar outlines in depth-buffer space. */
public final class PolygonOffset {

	private static final Pass NO_OFFSET = () -> {};

	private PolygonOffset() {}

	public static Pass forOutline(final IOpenGL openGL, final boolean hasOutline) {
		if (!hasOutline) return NO_OFFSET;
		return openGL.offsetPolygonsForOutline();
	}

	public static Pass apply(final IOpenGL openGL) {
		final GL2GL3 gl = openGL.getGL();
		if (openGL.isDrawingStored()) {
			// Attribute operations are recorded in legacy display lists and restore the state at replay time.
			final GL2 gl2 = gl.getGL2();
			gl2.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_POLYGON_BIT);
			gl2.glEnable(GL.GL_POLYGON_OFFSET_FILL);
			gl2.glPolygonOffset(1, 1);
			return gl2::glPopAttrib;
		}
		final boolean enabled = gl.glIsEnabled(GL.GL_POLYGON_OFFSET_FILL);
		final float[] factor = new float[1], units = new float[1];
		gl.glGetFloatv(GL.GL_POLYGON_OFFSET_FACTOR, factor, 0);
		gl.glGetFloatv(GL.GL_POLYGON_OFFSET_UNITS, units, 0);
		gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(1, 1);
		return () -> {
			gl.glPolygonOffset(factor[0], units[0]);
			if (!enabled) { gl.glDisable(GL.GL_POLYGON_OFFSET_FILL); }
		};
	}
}
