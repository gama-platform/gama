/*******************************************************************************************************
 *
 * OverlayLayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ILayer;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.outputs.layers.OverlayLayerData;
import gama.core.runtime.IScope;
import gama.dev.DEBUG;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.ui.display.opengl.OpenGL;
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

	@Override
	public void computeScale(final Trace list) {
		list.scale.setLocation(0.9, 0.9, 1);
	}

	/**
	 * Compute rotation.
	 *
	 * @param trace
	 *            the trace
	 */
	@Override
	public void computeRotation(final Trace trace) {
		// trace.rotation = NULL_ROTATION;
	}

	/**
	 * Adds the frame.
	 *
	 * @param gl
	 *            the gl
	 */
	@Override
	protected void addFrame(final OpenGL gl) {
		GamaPoint size = new GamaPoint(renderer.getEnvWidth(), renderer.getEnvHeight());
		final IScope scope = renderer.getSurface().getScope();
		final IExpression expr = layer.getDefinition().getFacet(IKeyword.SIZE);
		OverlayLayerData d = (OverlayLayerData) layer.getData();
		if (expr != null) {
			size = Cast.asPoint(scope, expr.value(scope));
			if (size.x <= 1) { size.x *= renderer.getEnvWidth(); }
			if (size.y <= 1) { size.y *= renderer.getEnvHeight(); }
		}
		gl.pushMatrix();
		boolean previous = gl.setObjectWireframe(false);
		try {
			gl.setCurrentColor(d.getBackgroundColor(scope), 1 - layer.getData().getTransparency(scope));
			if (d.isRounded()) {
				gl.translateBy(-size.x / 2, -size.y / 2, 0);
				gl.scaleBy(1, 1, 1);
				drawRoundedRectangle(gl.getGL(), size.x / 2, -size.y / 2, size.x, size.y, size.x / 20, 40);
			} else {
				gl.translateBy(size.x / 2, -size.y / 2, 0);
				gl.scaleBy(size.x, size.y, 1);
				gl.drawCachedGeometry(IShape.Type.SQUARE, null);
			}
		} finally {
			gl.setObjectWireframe(previous);
			gl.popMatrix();
		}
	}

	/**
	 * Draws a filled rectangle with rounded corners.
	 *
	 * @param gl
	 *            The OpenGL GL2 context.
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
	public void drawRoundedRectangle(final GL2 gl, final double d, final double e, final double x, final double y,
			double cornerRadius, final int numSegments) {
		double maxRadius = Math.min(x / 2.0f, y / 2.0f);
		if (cornerRadius < 0) { cornerRadius = 0; }
		if (cornerRadius > maxRadius) { cornerRadius = maxRadius; }

		if (cornerRadius == 0) {
			gl.glBegin(GL2ES3.GL_QUADS);
			gl.glVertex2d(d, e);
			gl.glVertex2d(d + x, e);
			gl.glVertex2d(d + x, e + y);
			gl.glVertex2d(d, e + y);
			gl.glEnd();
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

		// --- Dessiner les 5 parties rectangulaires ---
		// Utiliser GL_QUADS pour dessiner les rectangles. Chaque quad est défini par 4 sommets.
		gl.glBegin(GL2ES3.GL_QUADS);

		// 1. Rectangle Central
		gl.glVertex2d(cx_bl, cy_bl); // Coin inférieur gauche du rectangle central
		gl.glVertex2d(cx_br, cy_br); // Coin inférieur droit du rectangle central
		gl.glVertex2d(cx_tr, cy_tr); // Coin supérieur droit du rectangle central
		gl.glVertex2d(cx_tl, cy_tl); // Coin supérieur gauche du rectangle central

		// 2. Rectangle du Bas (sous le rectangle central)
		gl.glVertex2d(cx_bl, e); // Coin inférieur gauche (bord du rectangle englobant)
		gl.glVertex2d(cx_br, e); // Coin inférieur droit (bord du rectangle englobant)
		gl.glVertex2d(cx_br, cy_br); // Coin supérieur droit (jonction avec central)
		gl.glVertex2d(cx_bl, cy_bl); // Coin supérieur gauche (jonction avec central)

		// 3. Rectangle du Haut (au-dessus du rectangle central)
		gl.glVertex2d(cx_tl, cy_tl); // Coin inférieur gauche (jonction avec central)
		gl.glVertex2d(cx_tr, cy_tr); // Coin inférieur droit (jonction avec central)
		gl.glVertex2d(cx_tr, e + y); // Coin supérieur droit (bord du rectangle englobant)
		gl.glVertex2d(cx_tl, e + y); // Coin supérieur gauche (bord du rectangle englobant)

		// 4. Rectangle de Gauche (à gauche du rectangle central)
		gl.glVertex2d(d, cy_bl); // Coin inférieur gauche (bord du rectangle englobant)
		gl.glVertex2d(cx_bl, cy_bl); // Coin inférieur droit (jonction avec central)
		gl.glVertex2d(cx_tl, cy_tl); // Coin supérieur droit (jonction avec central)
		gl.glVertex2d(d, cy_tl); // Coin supérieur gauche (bord du rectangle englobant)

		// 5. Rectangle de Droite (à droite du rectangle central)
		gl.glVertex2d(cx_br, cy_br); // Coin inférieur gauche (jonction avec central)
		gl.glVertex2d(d + x, cy_br); // Coin inférieur droit (bord du rectangle englobant)
		gl.glVertex2d(d + x, cy_tr); // Coin supérieur droit (bord du rectangle englobant)
		gl.glVertex2d(cx_tr, cy_tr); // Coin supérieur gauche (jonction avec central)

		gl.glEnd(); // Fin du dessin des parties rectangulaires

		// --- Dessiner les 4 coins arrondis (quarts de cercle) ---
		// Chaque coin est un GL_TRIANGLE_FAN centré sur le point cx_*, cy_*.
		// L'angle pour un quart de cercle est PI/2 radians (90 degrés).
		float angleIncrement = (float) (Math.PI / 2.0 / numSegments);

		// Coin Inférieur Gauche
		// Angles de PI (180°) à 3*PI/2 (270°)
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx_bl, cy_bl); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = (float) Math.PI + i * angleIncrement;
			double vx = cx_bl + cornerRadius * Math.cos(angle);
			double vy = cy_bl + cornerRadius * Math.sin(angle);
			gl.glVertex2d(vx, vy);
		}
		gl.glEnd();

		// Coin Inférieur Droit
		// Angles de 3*PI/2 (270°) à 2*PI (360°)
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx_br, cy_br); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = (float) (3 * Math.PI / 2.0) + i * angleIncrement;
			double vx = cx_br + cornerRadius * Math.cos(angle);
			double vy = cy_br + cornerRadius * Math.sin(angle);
			gl.glVertex2d(vx, vy);
		}
		gl.glEnd();

		// Coin Supérieur Droit
		// Angles de 0° à PI/2 (90°)
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx_tr, cy_tr); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = i * angleIncrement; // angle = 0 pour le premier point, puis augmente
			double vx = cx_tr + cornerRadius * Math.cos(angle);
			double vy = cy_tr + cornerRadius * Math.sin(angle);
			gl.glVertex2d(vx, vy);
		}
		gl.glEnd();

		// Coin Supérieur Gauche
		// Angles de PI/2 (90°) à PI (180°)
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(cx_tl, cy_tl); // Centre de l'éventail
		for (int i = 0; i <= numSegments; i++) {
			double angle = (float) (Math.PI / 2.0) + i * angleIncrement;
			double vx = cx_tl + cornerRadius * Math.cos(angle);
			double vy = cy_tl + cornerRadius * Math.sin(angle);
			gl.glVertex2d(vx, vy);
		}
		gl.glEnd();
	}

	@Override
	public boolean isOverlay() { return true; }

	/**
	 * Increase Z.
	 */
	@Override
	protected void computeZ(final Trace list) {}

	@Override
	protected void prepareDrawing(final OpenGL gl, final Trace list) {
		final double viewHeight = gl.getViewHeight();
		final double viewWidth = gl.getViewWidth();
		final double viewRatio = viewWidth / (viewHeight == 0 ? 1 : viewHeight);
		final double worldHeight = gl.getWorldHeight();
		final double worldWidth = gl.getWorldWidth();
		final double maxDim = worldHeight > worldWidth ? worldHeight : worldWidth;
		gl.pushIdentity(GLMatrixFunc.GL_PROJECTION);
		if (viewRatio >= 1.0) {
			gl.getGL().glOrtho(0, maxDim * viewRatio, -maxDim, 0, -1, 1);
		} else {
			gl.getGL().glOrtho(0, maxDim, -maxDim / viewRatio, 0, -1, 1);
		}
		super.prepareDrawing(gl, list);

	}

	@Override
	protected boolean hasDepth() {
		return false;
	}

	@Override
	protected void doDrawing(final OpenGL gl) {
		drawObjects(gl, currentList, alpha, false);
	}

	@Override
	public boolean isPickable() { return false; }

	@Override
	protected void stopDrawing(final OpenGL gl) {
		// super.stopDrawing(gl);
		// Addition to fix #2228 and #2222
		gl.resumeZTranslation();
		// gl.getGL().glEnable(GL.GL_DEPTH_TEST);
		gl.pop(GLMatrixFunc.GL_MODELVIEW);
		gl.pop(GLMatrixFunc.GL_PROJECTION);
	}

}
