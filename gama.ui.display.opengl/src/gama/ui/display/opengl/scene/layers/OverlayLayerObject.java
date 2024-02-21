/*******************************************************************************************************
 *
 * OverlayLayerObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.layers;

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
			gl.translateBy(size.x / 2, -size.y / 2, 0);
			gl.scaleBy(size.x, size.y, 1);
			gl.setCurrentColor(d.getBackgroundColor(scope), 1 - layer.getData().getTransparency(scope));
			gl.drawCachedGeometry(d.isRounded() ? IShape.Type.ROUNDED : IShape.Type.SQUARE, null);
		} finally {
			gl.setObjectWireframe(previous);
			gl.popMatrix();
		}
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
