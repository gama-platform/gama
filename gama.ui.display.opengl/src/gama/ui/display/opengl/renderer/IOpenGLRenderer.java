/*******************************************************************************************************
 *
 * IOpenGLRenderer.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer;

import com.jogamp.opengl.GLEventListener;

import gama.core.common.interfaces.IGraphics;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.outputs.LayeredDisplayData;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.renderer.helpers.CameraHelper;
import gama.ui.display.opengl.renderer.helpers.KeystoneHelper;
import gama.ui.display.opengl.renderer.helpers.LightHelper;
import gama.ui.display.opengl.renderer.helpers.PickingHelper;
import gama.ui.display.opengl.renderer.helpers.SceneHelper;
import gama.ui.display.opengl.view.GamaGLCanvas;
import gama.ui.display.opengl.view.SWTOpenGLDisplaySurface;

/**
 * The Interface IOpenGLRenderer.
 */
public interface IOpenGLRenderer extends GLEventListener, IGraphics.ThreeD {

	/**
	 * Sets the canvas.
	 *
	 * @param canvas
	 *            the new canvas
	 */
	void setCanvas(GamaGLCanvas canvas);

	/**
	 * Gets the canvas.
	 *
	 * @return the canvas
	 */
	GamaGLCanvas getCanvas();

	/**
	 * Inits the scene.
	 */
	void initScene();

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	double getWidth();

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	double getHeight();

	/**
	 * Gets the real world point from window point.
	 *
	 * @param mouse
	 *            the mouse
	 * @return the real world point from window point
	 */
	GamaPoint getRealWorldPointFromWindowPoint(final GamaPoint mouse);

	/**
	 * Gets the surface.
	 *
	 * @return the surface
	 */
	@Override
	SWTOpenGLDisplaySurface getSurface();

	/**
	 * Gets the camera helper.
	 *
	 * @return the camera helper
	 */
	CameraHelper getCameraHelper();

	/**
	 * Gets the keystone helper.
	 *
	 * @return the keystone helper
	 */
	KeystoneHelper getKeystoneHelper();

	/**
	 * Gets the picking helper.
	 *
	 * @return the picking helper
	 */
	PickingHelper getPickingHelper();

	/**
	 * Gets the open GL helper.
	 *
	 * @return the open GL helper
	 */
	OpenGL getOpenGLHelper();

	/**
	 * Gets the light helper.
	 *
	 * @return the light helper
	 */
	LightHelper getLightHelper();

	/**
	 * Gets the scene helper.
	 *
	 * @return the scene helper
	 */
	SceneHelper getSceneHelper();

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	default LayeredDisplayData getData() { return getSurface().getData(); }

	/**
	 * Gets the layer width.
	 *
	 * @return the layer width
	 */
	int getLayerWidth();

	/**
	 * Gets the layer height.
	 *
	 * @return the layer height
	 */
	int getLayerHeight();

	/**
	 * Use shader.
	 *
	 * @return true, if successful
	 */
	default boolean useShader() {
		return false;
	}

	/**
	 * Checks if is disposed.
	 *
	 * @return true, if is disposed
	 */
	boolean isDisposed();

	/**
	 * Checks for drawn once.
	 *
	 * @return true, if successful
	 */
	boolean hasDrawnOnce();

}