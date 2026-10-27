/*******************************************************************************************************
 *
 * OverlayLayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.layers.ILayer;
import gama.core.outputs.layers.OverlayLayerData;
import gama.dev.DEBUG;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;

/**
 * The Class OverlayLayerObject.
 */
public class OverlayLayerObject extends LayerObject {

	static {
		DEBUG.OFF();
	}

	/**
	 * Instantiates a new overlay layer object.
	 *
	 * @param renderer
	 *            the renderer
	 * @param layer
	 *            the layer
	 */
	public OverlayLayerObject(final IOpenGLRenderer renderer, final ILayer layer) {
		super(renderer, layer);
	}

	/** The radius, in pixels, of the rounded corners (same as the Java2D displays). */
	private static final double CORNER_RADIUS = 5;

	/** The number of segments used to draw each rounded corner. */
	private static final int CORNER_SEGMENTS = 10;

	/**
	 * The geometry of overlays is entirely computed by {@link OverlayLayerData} in pixels: no offset, scale, rotation or
	 * z translation is computed here.
	 */
	@Override
	public void computeScale(final Trace list) {}

	@Override
	public void computeOffset(final Trace list) {}

	@Override
	public void computeRotation(final Trace trace) {}

	/**
	 * Adds the frame (background and border), drawn in pixels from the origin of the overlay.
	 *
	 * @param gl
	 *            the gl
	 */
	@Override
	protected void addFrame(final IOpenGL gl) {
		if (layer == null || !(layer.getData() instanceof OverlayLayerData d)) return;
		final java.awt.Point size = d.getSizeInPixels();
		final double w = size.getX();
		final double h = size.getY();
		if (w <= 0 || h <= 0) return;
		final IScope scope = renderer.getSurface().getScope();
		final double radius = d.isRounded() ? CORNER_RADIUS : 0;
		final boolean previous = gl.setObjectWireframe(false);
		try {
			final IColor background = d.getBackgroundColor(scope);
			if (background != null) {
				gl.setCurrentColor(background, 1 - d.getTransparency(scope));
				drawRoundedRectangle(gl, 0, -h, w, h, radius, CORNER_SEGMENTS);
			}
			final IColor border = d.getBorderColor();
			if (border != null) {
				gl.setCurrentColor(border, 1);
				drawRoundedOutline(gl, 0, -h, w, h, radius, CORNER_SEGMENTS);
			}
		} finally {
			gl.setObjectWireframe(previous);
		}
	}

	/**
	 * Draws the outline of a rectangle with rounded corners.
	 *
	 * @param gl
	 *            The rendering backend.
	 * @param d
	 *            The X coordinate of the lower-left corner of the enclosing rectangle.
	 * @param e
	 *            The Y coordinate of the lower-left corner of the enclosing rectangle.
	 * @param x
	 *            The total width of the rectangle.
	 * @param y
	 *            The total height of the rectangle.
	 * @param cornerRadius
	 *            The radius of the rounded corners.
	 * @param numSegments
	 *            The number of line segments to draw each corner arc.
	 */
	public void drawRoundedOutline(final IOpenGL gl, final double d, final double e, final double x, final double y,
			final double cornerRadius, final int numSegments) {
		final double r = Math.max(0, Math.min(cornerRadius, Math.min(x / 2, y / 2)));
		final double[][] centers = { { d + r, e + r }, { d + x - r, e + r }, { d + x - r, e + y - r }, { d + r, e + y - r } };
		final int segments = r == 0 ? 0 : numSegments;
		gl.beginDrawing(GL.GL_LINE_LOOP);
		for (int corner = 0; corner < 4; corner++) {
			final double startAngle = Math.PI + corner * Math.PI / 2;
			for (int i = 0; i <= segments; i++) {
				final double angle = startAngle + i * Math.PI / 2 / Math.max(1, segments);
				gl.outputVertex(centers[corner][0] + r * Math.cos(angle), centers[corner][1] + r * Math.sin(angle), 0);
			}
		}
		gl.endDrawing();
	}

	/**
	 * Draws a filled rectangle with rounded corners.
	 *
	 * @param gl
	 *            The rendering backend.
	 * @param d
	 *            The X coordinate of the lower-left corner of the enclosing rectangle.
	 * @param e
	 *            The Y coordinate of the lower-left corner of the enclosing rectangle.
	 * @param x
	 *            The total width of the rectangle.
	 * @param y
	 *            The total height of the rectangle.
	 * @param cornerRadius
	 *            The radius of the rounded corners.
	 * @param numSegments
	 *            The number of line segments to draw each corner arc (more => smoother).
	 */
	public void drawRoundedRectangle(final IOpenGL gl, final double d, final double e, final double x, final double y,
			double cornerRadius, final int numSegments) {
		double maxRadius = Math.min(x / 2.0f, y / 2.0f);
		if (cornerRadius < 0) { cornerRadius = 0; }
		if (cornerRadius > maxRadius) { cornerRadius = maxRadius; }

		if (cornerRadius == 0) {
			gl.beginDrawing(GL.GL_TRIANGLES);
			outputRectangle(gl, d, e, d + x, e + y);
			gl.endDrawing();
			return;
		}

		// Coordonnées des centres des arcs pour chaque coin
		// Ces points sont aussi les coins intérieurs du corps principal du rectangle arrondi
		double cx_bl = d + cornerRadius; // Centre X Coin Inférieur Gauche (Bottom-Left)
		double cy_bl = e + cornerRadius; // Centre Y Coin Inférieur Gauche

		double cx_br = d + x - cornerRadius; // Centre X Coin Inférieur Droit (Bottom-Right)
		double cy_br = e + cornerRadius; // Centre Y Coin Inférieur Droit

		double cx_tr = d + x - cornerRadius; // Centre X Coin Supérieur Droit (Top-Right)
		double cy_tr = e + y - cornerRadius;// Centre Y Coin Supérieur Droit

		double cx_tl = d + cornerRadius; // Centre X Coin Supérieur Gauche (Top-Left)
		double cy_tl = e + y - cornerRadius;// Centre Y Coin Supérieur Gauche

		// Triangles work in both the fixed-function and core profiles.
		gl.beginDrawing(GL.GL_TRIANGLES);
		outputRectangle(gl, cx_bl, cy_bl, cx_br, cy_tr);
		outputRectangle(gl, cx_bl, e, cx_br, cy_br);
		outputRectangle(gl, cx_tl, cy_tl, cx_tr, e + y);
		outputRectangle(gl, d, cy_bl, cx_tl, cy_tl);
		outputRectangle(gl, cx_br, cy_br, d + x, cy_tr);
		gl.endDrawing();

		// --- Dessiner les 4 coins arrondis (quarts de cercle) ---
		// Chaque coin est un GL_TRIANGLE_FAN centré sur le point cx_*, cy_*.
		// L'angle pour un quart de cercle est PI/2 radians (90 degrés).
		float angleIncrement = (float) (Math.PI / 2.0 / numSegments);

		// Coin Inférieur Gauche
		// Angles de PI (180°) à 3*PI/2 (270°)
		gl.beginDrawing(GL.GL_TRIANGLE_FAN);
		gl.outputVertex(cx_bl, cy_bl, 0); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = (float) Math.PI + i * angleIncrement;
			double vx = cx_bl + cornerRadius * Math.cos(angle);
			double vy = cy_bl + cornerRadius * Math.sin(angle);
			gl.outputVertex(vx, vy, 0);
		}
		gl.endDrawing();

		// Coin Inférieur Droit
		// Angles de 3*PI/2 (270°) à 2*PI (360°)
		gl.beginDrawing(GL.GL_TRIANGLE_FAN);
		gl.outputVertex(cx_br, cy_br, 0); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = (float) (3 * Math.PI / 2.0) + i * angleIncrement;
			double vx = cx_br + cornerRadius * Math.cos(angle);
			double vy = cy_br + cornerRadius * Math.sin(angle);
			gl.outputVertex(vx, vy, 0);
		}
		gl.endDrawing();

		// Coin Supérieur Droit
		// Angles de 0° à PI/2 (90°)
		gl.beginDrawing(GL.GL_TRIANGLE_FAN);
		gl.outputVertex(cx_tr, cy_tr, 0); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = i * angleIncrement; // angle = 0 pour le premier point, puis augmente
			double vx = cx_tr + cornerRadius * Math.cos(angle);
			double vy = cy_tr + cornerRadius * Math.sin(angle);
			gl.outputVertex(vx, vy, 0);
		}
		gl.endDrawing();

		// Coin Supérieur Gauche
		// Angles de PI/2 (90°) à PI (180°)
		gl.beginDrawing(GL.GL_TRIANGLE_FAN);
		gl.outputVertex(cx_tl, cy_tl, 0); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = (float) (Math.PI / 2.0) + i * angleIncrement;
			double vx = cx_tl + cornerRadius * Math.cos(angle);
			double vy = cy_tl + cornerRadius * Math.sin(angle);
			gl.outputVertex(vx, vy, 0);
		}
		gl.endDrawing();
	}

	private static void outputRectangle(final IOpenGL gl, final double left, final double bottom,
			final double right, final double top) {
		gl.outputVertex(left, bottom, 0);
		gl.outputVertex(right, bottom, 0);
		gl.outputVertex(right, top, 0);
		gl.outputVertex(left, bottom, 0);
		gl.outputVertex(right, top, 0);
		gl.outputVertex(left, top, 0);
	}

	@Override
	public boolean isOverlay() { return true; }

	/**
	 * Increase Z.
	 */
	@Override
	protected void computeZ(final Trace list) {}

	/**
	 * Sets up a projection where one unit is one (logical) pixel of the view, with the origin at the top-left corner,
	 * translates to the position of the overlay, draws its frame and finally scales the contents using the ratio
	 * computed by {@link OverlayLayerData}, so that `#px` inside the overlay represents exactly one pixel.
	 */
	@Override
	protected void prepareDrawing(final IOpenGL gl, final Trace list) {
		gl.pushIdentity(GLMatrixFunc.GL_PROJECTION);
		gl.ortho(0, gl.getViewWidth(), -gl.getViewHeight(), 0, -1, 1);
		final OverlayLayerData data = layer != null && layer.getData() instanceof OverlayLayerData d ? d : null;
		if (data != null) {
			final java.awt.Point position = data.getPositionInPixels();
			gl.translateBy(position.getX(), -position.getY(), 0);
		}
		addFrame(gl);
		if (data != null) {
			final double ratio = data.getContentRatio();
			gl.scaleBy(ratio, ratio, 1);
		}
	}

	@Override
	protected boolean hasDepth() {
		return false;
	}

	@Override
	protected void doDrawing(final IOpenGL gl) {
		drawObjects(gl, currentList, alpha, false);
	}

	@Override
	public boolean isPickable() { return false; }

	@Override
	protected void stopDrawing(final IOpenGL gl) {
		// super.stopDrawing(gl);
		// Addition to fix #2228 and #2222
		gl.resumeZTranslation();
		// gl.getGL().glEnable(GL.GL_DEPTH_TEST);
		gl.pop(GLMatrixFunc.GL_MODELVIEW);
		gl.pop(GLMatrixFunc.GL_PROJECTION);
	}

}
