/*******************************************************************************************************
 *
 * IDisplayData.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.ui.layers.ILightDefinition;
import gama.api.utils.geometry.ICoordinates;
import gama.api.utils.geometry.IRotationDefinition;

/**
 *
 */
public interface IDisplayData {

	/**
	 * The listener interface for receiving displayData events. The class that is interested in processing a displayData
	 * event implements this interface, and the object created with that class is registered with a component using the
	 * component's <code>addDisplayDataListener<code> method. When the displayData event occurs, that object's
	 * appropriate method is invoked.
	 *
	 * @see DisplayDataEvent
	 */
	public interface DisplayDataListener {

		/**
		 * Changed.
		 *
		 * @param property
		 *            the property
		 * @param value
		 *            the value
		 */
		void changed(Changes property, Object value);
	}

	/**
	 * The Enum Changes.
	 */
	public enum Changes {

		/** The background. */
		BACKGROUND,

		/** The zoom. */
		ZOOM
	}

	/** The Constant OPENGL2. */
	String OPENGL2 = "opengl4";
	/** The Constant WEB. */
	String WEB = "web";
	/** The Constant INITIAL_ZOOM. */
	Double INITIAL_ZOOM = 1.0;

	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addListener(DisplayDataListener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void removeListener(DisplayDataListener listener);

	/**
	 * Notify listeners.
	 *
	 * @param property
	 *            the property
	 * @param value
	 *            the value
	 */
	void notifyListeners(Changes property, Object value);

	/**
	 * Gets the listeners.
	 *
	 * @return the listeners
	 */
	Set<DisplayDataListener> getListeners();

	/**
	 * Dispose.
	 */
	void dispose();

	/**
	 * @return the backgroundColor
	 */
	IColor getBackgroundColor();

	/**
	 * @param backgroundColor
	 *            the backgroundColor to set
	 */
	void setBackgroundColor(IColor backgroundColor);

	/**
	 * @return the autosave
	 */
	boolean isAutosave();

	/**
	 * @param autosave
	 *            the autosave to set
	 */
	void setAutosave(boolean autosave);

	/**
	 * Sets the autosave path.
	 *
	 * @param p
	 *            the new autosave path
	 */
	void setAutosavePath(String p);

	/**
	 * Gets the autosave path.
	 *
	 * @return the autosave path
	 */
	String getAutosavePath();

	/**
	 * Checks if is wireframe.
	 *
	 * @return true, if is wireframe
	 */
	boolean isWireframe();

	/**
	 * Sets the wireframe.
	 *
	 * @param t
	 *            the new wireframe
	 */
	void setWireframe(boolean t);

	/**
	 * @return the ortho
	 */
	boolean isOrtho();

	/**
	 * @param ortho
	 *            the ortho to set
	 */
	void setOrtho(boolean ortho);

	/**
	 * @return the showfps
	 */
	boolean isShowfps();

	/**
	 * @param showfps
	 *            the showfps to set
	 */
	void setShowfps(boolean showfps);

	/**
	 * Gets the z near.
	 *
	 * @return the z near
	 */
	double getzNear();

	/**
	 * Gets the z far.
	 *
	 * @return the z far
	 */
	double getzFar();

	/**
	 * @return the drawEnv
	 */
	boolean isDrawEnv();

	/**
	 * @param drawEnv
	 *            the drawEnv to set
	 */
	void setDrawEnv(boolean drawEnv);

	/**
	 * @return the displayType
	 */
	String getDisplayType();

	/**
	 * @param displayType
	 *            the displayType to set
	 */
	void setDisplayType(String displayType);

	/**
	 * @return the imageDimension
	 */
	IPoint getImageDimension();

	/**
	 * @param imageDimension
	 *            the imageDimension to set
	 */
	void setImageDimension(IPoint imageDimension);

	/**
	 * @return the envWidth
	 */
	double getEnvWidth();

	/**
	 * @param envWidth
	 *            the envWidth to set
	 */
	void setEnvWidth(double envWidth);

	/**
	 * @return the envHeight
	 */
	double getEnvHeight();

	/**
	 * @param envHeight
	 *            the envHeight to set
	 */
	void setEnvHeight(double envHeight);

	/**
	 * Gets the max env dim.
	 *
	 * @return the max env dim
	 */
	double getMaxEnvDim();

	/**
	 * @return
	 */
	IColor getHighlightColor();

	/**
	 * Sets the highlight color.
	 *
	 * @param hc
	 *            the new highlight color
	 */
	void setHighlightColor(IColor hc);

	/**
	 * Checks if is antialias.
	 *
	 * @return true, if is antialias
	 */
	boolean isAntialias();

	/**
	 * Sets the antialias.
	 *
	 * @param a
	 *            the new antialias
	 */
	void setAntialias(boolean a);

	/**
	 * @return the zoomLevel
	 */
	Double getZoomLevel();

	/**
	 * @param zoomLevel
	 *            the zoomLevel to set
	 */
	void setZoomLevel(Double zoomLevel, boolean notify);

	/**
	 * Full screen.
	 *
	 * @return the int
	 */
	int fullScreen();

	/**
	 * Sets the overlay.
	 *
	 * @param fs
	 *            the new overlay
	 */
	void setFullScreen(int fs);

	/**
	 * Sets the keystone.
	 *
	 * @param value
	 *            the new keystone
	 */
	void setKeystone(List<IPoint> value);

	/**
	 * Sets the keystone.
	 *
	 * @param value
	 *            the new keystone
	 */
	void setKeystone(ICoordinates value);

	/**
	 * Gets the keystone.
	 *
	 * @return the keystone
	 */
	ICoordinates getKeystone();

	/**
	 * Checks if is keystone defined.
	 *
	 * @return true, if is keystone defined
	 */
	boolean isKeystoneDefined();

	/**
	 * Checks if is toolbar visible.
	 *
	 * @return true, if is toolbar visible
	 */
	boolean isToolbarVisible();

	/**
	 * Gets the toolbar color.
	 *
	 * @return the toolbar color
	 */
	IColor getToolbarColor();

	/**
	 * Sets the toolbar visible.
	 *
	 * @param b
	 *            the new toolbar visible
	 */
	void setToolbarVisible(boolean b);

	/**
	 * Inits the with.
	 *
	 * @param scope
	 *            the scope
	 * @param desc
	 *            the desc
	 */
	void initWith(IScope scope, IDescription desc);

	/**
	 * Sets the z far.
	 *
	 * @param zF
	 *            the new z far
	 */
	void setZFar(Double zF);

	/**
	 * Sets the z near.
	 *
	 * @param zN
	 *            the new z near
	 */
	void setZNear(Double zN);

	/**
	 * Update.
	 *
	 * @param scope
	 *            the scope
	 * @param facets
	 *            the facets
	 */
	void update(IScope scope, Facets facets);

	/**
	 * Checks if is open GL 2.
	 *
	 * @return true, if is open GL 2
	 */
	boolean isOpenGL2();

	/**
	 * Checks if is web.
	 *
	 * @return true, if is web
	 */
	boolean isWeb();

	/**
	 * Checks if is open GL.
	 *
	 * @return true, if is open GL
	 */
	boolean is3D();

	/**
	 * Adds the camera definition.
	 *
	 * @param legend
	 *            the name
	 * @param definition
	 *            the definition
	 */
	void addCameraDefinition(ICameraDefinition definition);

	/**
	 * Gets the distance coefficient.
	 *
	 * @return the distance coefficient
	 */
	double getCameraDistanceCoefficient();

	/**
	 * Reset camera.
	 */
	void resetCamera();

	/**
	 * Gets the distance.
	 *
	 * @return the distance
	 */
	double getCameraDistance();

	/**
	 * Sets the distance.
	 *
	 * @param distance
	 *            the new distance
	 */
	void setCameraDistance(double distance);

	/**
	 * Sets the preset camera.
	 *
	 * @param newValue
	 *            the new preset camera
	 */
	void setCameraNameFromGaml(String newValue);

	/**
	 * Sets the preset camera.
	 *
	 * @param newValue
	 *            the new preset camera
	 */
	void setCameraNameFromUser(String newValue);

	/**
	 * @return the cameraPos
	 */
	IPoint getCameraPos();

	/**
	 * @param cameraPos
	 *            the cameraPos to set
	 */
	void setCameraPos(IPoint point);

	/**
	 * @return the cameraLookPos
	 */
	IPoint getCameraTarget();

	/**
	 * @param cameraLookPos
	 *            the cameraLookPos to set
	 */
	void setCameraTarget(IPoint point);

	/**
	 * @return the cameraLens
	 */
	double getCameraLens();

	/**
	 * Gets the preset camera.
	 *
	 * @return the preset camera
	 */
	String getCameraName();

	/**
	 * Gets the camera names.
	 *
	 * @return the camera names
	 */
	Collection<String> getCameraNames();

	/**
	 * Disable camera interactions.
	 *
	 * @param disableCamInteract
	 *            the disable cam interact
	 */
	void setCameraLocked(boolean disableCamInteract);

	/**
	 * Camera interaction disabled.
	 *
	 * @return true, if successful
	 */
	boolean isCameraLocked();

	/**
	 * Checks if is camera dynamic.
	 *
	 * @return true, if is camera dynamic
	 */
	boolean isCameraDynamic();

	/**
	 * Sets the rotation.
	 *
	 * @param rotation
	 *            the new rotation
	 */
	void setRotation(IRotationDefinition rotation);

	/**
	 * @return
	 */
	boolean isContinuousRotationOn();

	/**
	 * Sets the continuous rotation.
	 *
	 * @param r
	 *            the new continuous rotation
	 */
	void setContinuousRotation(boolean r);

	/**
	 * Gets the current rotation about Z.
	 *
	 * @return the current rotation about Z
	 */
	double getRotationAngle();

	/**
	 * Checks for rotation.
	 *
	 * @return true, if successful
	 */
	boolean hasRotation();

	/**
	 * Sets the z rotation angle.
	 *
	 * @param val
	 *            the new z rotation angle
	 */
	void setRotationAngle(double val);

	/**
	 * Gets the rotation center.
	 *
	 * @return the rotation center
	 */
	IPoint getRotationCenter();

	/**
	 * Gets the rotation axis.
	 *
	 * @return the rotation axis
	 */
	IPoint getRotationAxis();

	/**
	 * Reset Z rotation.
	 */
	void resetRotation();

	/**
	 * @return the isLightOn
	 */
	boolean isLightOn();

	/**
	 * @param isLightOn
	 *            the isLightOn to set
	 */
	void setLightOn(boolean isLightOn);

	/**
	 * Gets the diffuse lights.
	 *
	 * @return the diffuse lights
	 */
	Map<String, ILightDefinition> getLights();

	/**
	 * Adds the light definition.
	 *
	 * @param definition
	 *            the definition
	 */
	void addLightDefinition(ILightDefinition definition);

}