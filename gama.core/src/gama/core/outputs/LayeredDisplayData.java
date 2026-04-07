/*******************************************************************************************************
 *
 * LayeredDisplayData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.base.Objects;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.ui.displays.IDisplayData;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.ui.layers.ILightDefinition;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.ICoordinates;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.geometry.IRotationDefinition;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.core.experiment.ExperimentAgent;
import gama.core.outputs.layers.properties.GenericCameraDefinition;
import gama.core.outputs.layers.properties.GenericLightDefinition;
import gama.dev.DEBUG;

/**
 */
public class LayeredDisplayData implements IDisplayData {

	static {
		DEBUG.OFF();
	}

	/** The listeners. */
	public final Set<DisplayDataListener> listeners = new CopyOnWriteArraySet<>();

	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	@Override
	public void addListener(final DisplayDataListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	@Override
	public void removeListener(final DisplayDataListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify listeners.
	 *
	 * @param property
	 *            the property
	 * @param value
	 *            the value
	 */
	@Override
	public void notifyListeners(final Changes property, final Object value) {
		for (final DisplayDataListener listener : listeners) { listener.changed(property, value); }
	}

	/**
	 * Colors
	 */
	private IColor backgroundColor = GamaPreferences.Displays.CORE_BACKGROUND.getValue();
	//
	// /** The ambient color. */
	// private GamaColor ambientColor = new GamaColor(64, 64, 64, 255);

	/** The highlight color. */
	private IColor highlightColor = GamaPreferences.Displays.CORE_HIGHLIGHT.getValue();

	/** The toolbar color. */
	private IColor toolbarColor = null;

	/**
	 * Properties
	 */
	private boolean isAutosaving = false;

	/** The autosaving path. */
	private String autosavingPath = "";

	/** The is toolbar visible. */
	private boolean isToolbarVisible = GamaPreferences.Displays.CORE_DISPLAY_TOOLBAR.getValue();

	/** The is synchronized. */
	// private boolean isSynchronized = GamaPreferences.Runtime.CORE_SYNC.getValue();

	/** The display type. */
	private String displayType = GamaPreferences.Displays.CORE_DISPLAY.getValue() ? IKeyword._2D : IKeyword._3D;

	/** The env width. */
	private double envWidth = 0d;

	/** The env height. */
	private double envHeight = 0d;

	/** The is antialiasing. */
	private boolean isAntialiasing = GamaPreferences.Displays.CORE_ANTIALIAS.getValue();

	/** The image dimension. */
	private volatile IPoint imageDimension;

	/** The zoom level. */
	private Double zoomLevel = INITIAL_ZOOM;

	/** The keystone. */
	private final ICoordinates keystone = (ICoordinates) GamaCoordinateSequenceFactory.getKeystoneIdentity().copy();

	/** The is open GL. */
	private boolean is3D;

	/**
	 * OpenGL
	 */

	private boolean isWireframe = false;

	/** The ortho. */
	private boolean ortho = false;

	/** The is showing FPS. */
	private boolean isShowingFPS = false; // GamaPreferences.CORE_SHOW_FPS.getValue();

	/** The is drawing environment. */
	private boolean isDrawingEnvironment = GamaPreferences.Displays.CORE_DRAW_ENV.getValue();

	/** The constant background. */
	private boolean constantBackground = true;

	/** The z near. */
	private double zNear = -1.0;

	/** The z far. */
	private double zFar = -1.0;

	/** The full screen. */
	private int fullScreen = -1;

	/** The highlight listener. */
	IPreferenceAfterChangeListener<IColor> highlightListener = this::setHighlightColor;

	/**
	 * Instantiates a new layered display data.
	 */
	public LayeredDisplayData() {
		GamaPreferences.Displays.CORE_HIGHLIGHT.addChangeListener(highlightListener);
	}

	/**
	 * Dispose.
	 */
	@Override
	public void dispose() {
		GamaPreferences.Displays.CORE_HIGHLIGHT.removeChangeListener(highlightListener);
		listeners.clear();
	}

	/**
	 * @return the backgroundColor
	 */
	@Override
	public IColor getBackgroundColor() { return backgroundColor; }

	/**
	 * @param backgroundColor
	 *            the backgroundColor to set
	 */
	@Override
	public void setBackgroundColor(final IColor backgroundColor) {
		this.backgroundColor = backgroundColor;
		notifyListeners(Changes.BACKGROUND, backgroundColor);
	}

	/**
	 * @return the autosave
	 */
	@Override
	public boolean isAutosave() { return isAutosaving; }

	/**
	 * @param autosave
	 *            the autosave to set
	 */
	@Override
	public void setAutosave(final boolean autosave) { this.isAutosaving = autosave; }

	/**
	 * Sets the autosave path.
	 *
	 * @param p
	 *            the new autosave path
	 */
	@Override
	public void setAutosavePath(final String p) { this.autosavingPath = p; }

	/**
	 * Gets the autosave path.
	 *
	 * @return the autosave path
	 */
	@Override
	public String getAutosavePath() { return autosavingPath; }

	/**
	 * Checks if is wireframe.
	 *
	 * @return true, if is wireframe
	 */
	@Override
	public boolean isWireframe() { return isWireframe; }

	/**
	 * Sets the wireframe.
	 *
	 * @param t
	 *            the new wireframe
	 */
	@Override
	public void setWireframe(final boolean t) { isWireframe = t; }

	/**
	 * @return the ortho
	 */
	@Override
	public boolean isOrtho() { return ortho; }

	/**
	 * @param ortho
	 *            the ortho to set
	 */
	@Override
	public void setOrtho(final boolean ortho) { this.ortho = ortho; }

	/**
	 * @return the showfps
	 */
	@Override
	public boolean isShowfps() { return isShowingFPS; }

	/**
	 * @param showfps
	 *            the showfps to set
	 */
	@Override
	public void setShowfps(final boolean showfps) { this.isShowingFPS = showfps; }

	/**
	 * Gets the z near.
	 *
	 * @return the z near
	 */
	@Override
	public double getzNear() {
		return zNear;
	}

	/**
	 * Gets the z far.
	 *
	 * @return the z far
	 */
	@Override
	public double getzFar() {
		return zFar;
	}

	/**
	 * @return the drawEnv
	 */
	@Override
	public boolean isDrawEnv() { return isDrawingEnvironment; }

	/**
	 * @param drawEnv
	 *            the drawEnv to set
	 */
	@Override
	public void setDrawEnv(final boolean drawEnv) { this.isDrawingEnvironment = drawEnv; }

	/**
	 * @return the displayType
	 */
	@Override
	public String getDisplayType() { return displayType; }

	/**
	 * @param displayType
	 *            the displayType to set
	 */
	@Override
	public void setDisplayType(final String displayType) {
		this.displayType = displayType;
		is3D = IKeyword.OPENGL.equals(displayType) || IKeyword._3D.equals(displayType) || OPENGL2.equals(displayType);

	}

	/**
	 * @return the imageDimension
	 */
	@Override
	public IPoint getImageDimension() { return imageDimension; }

	/**
	 * @param imageDimension
	 *            the imageDimension to set
	 */
	@Override
	public void setImageDimension(final IPoint imageDimension) {
		// DEBUG.OUT("Setting image dimension to : " + imageDimension);
		this.imageDimension = imageDimension;
	}

	/**
	 * @return the envWidth
	 */
	@Override
	public double getEnvWidth() { return envWidth; }

	/**
	 * @param envWidth
	 *            the envWidth to set
	 */
	@Override
	public void setEnvWidth(final double envWidth) { this.envWidth = envWidth; }

	/**
	 * @return the envHeight
	 */
	@Override
	public double getEnvHeight() { return envHeight; }

	/**
	 * @param envHeight
	 *            the envHeight to set
	 */
	@Override
	public void setEnvHeight(final double envHeight) { this.envHeight = envHeight; }

	/**
	 * Gets the max env dim.
	 *
	 * @return the max env dim
	 */
	@Override
	public double getMaxEnvDim() { return envWidth > envHeight ? envWidth : envHeight; }

	/**
	 * @return
	 */
	@Override
	public IColor getHighlightColor() { return highlightColor; }

	/**
	 * Sets the highlight color.
	 *
	 * @param hc
	 *            the new highlight color
	 */
	@Override
	public void setHighlightColor(final IColor hc) { highlightColor = hc; }

	/**
	 * Checks if is antialias.
	 *
	 * @return true, if is antialias
	 */
	@Override
	public boolean isAntialias() { return isAntialiasing; }

	/**
	 * Sets the antialias.
	 *
	 * @param a
	 *            the new antialias
	 */
	@Override
	public void setAntialias(final boolean a) { isAntialiasing = a; }

	/**
	 * @return the zoomLevel
	 */
	@Override
	public Double getZoomLevel() { return zoomLevel; }

	/**
	 * @param zoomLevel
	 *            the zoomLevel to set
	 */
	@Override
	public void setZoomLevel(final Double zoomLevel, final boolean notify) {
		if (this.zoomLevel != null && this.zoomLevel.equals(zoomLevel)) return;
		this.zoomLevel = zoomLevel;
		if (notify) { notifyListeners(Changes.ZOOM, this.zoomLevel); }
	}

	/**
	 * Full screen.
	 *
	 * @return the int
	 */
	@Override
	public int fullScreen() {
		return fullScreen;
	}

	/**
	 * Sets the overlay.
	 *
	 * @param fs
	 *            the new overlay
	 */
	@Override
	public void setFullScreen(final int fs) { fullScreen = fs; }

	/**
	 * Sets the keystone.
	 *
	 * @param value
	 *            the new keystone
	 */
	@Override
	public void setKeystone(final List<IPoint> value) {
		if (value == null) return;
		keystone.setTo(value.toArray(new IPoint[4]));
	}

	/**
	 * Sets the keystone.
	 *
	 * @param value
	 *            the new keystone
	 */
	@Override
	public void setKeystone(final ICoordinates value) {
		if (value == null) return;
		this.keystone.setTo(value.toPointsArray());
	}

	/**
	 * Gets the keystone.
	 *
	 * @return the keystone
	 */
	@Override
	public ICoordinates getKeystone() { return this.keystone; }

	/**
	 * Checks if is keystone defined.
	 *
	 * @return true, if is keystone defined
	 */
	@Override
	public boolean isKeystoneDefined() { return !keystone.equals(GamaCoordinateSequenceFactory.getKeystoneIdentity()); }

	/**
	 * Checks if is toolbar visible.
	 *
	 * @return true, if is toolbar visible
	 */
	@Override
	public boolean isToolbarVisible() { return this.isToolbarVisible; }

	/**
	 * Gets the toolbar color.
	 *
	 * @return the toolbar color
	 */
	@Override
	public IColor getToolbarColor() { return toolbarColor == null ? getBackgroundColor() : toolbarColor; }

	/**
	 * Sets the toolbar visible.
	 *
	 * @param b
	 *            the new toolbar visible
	 */
	@Override
	public void setToolbarVisible(final boolean b) { isToolbarVisible = b; }

	/**
	 * Inits the with.
	 *
	 * @param scope
	 *            the scope
	 * @param desc
	 *            the desc
	 */
	@Override
	public void initWith(final IScope scope, final IDescription desc) {
		final Facets facets = desc.getFacets();
		// Initializing the size of the environment
		ISimulationAgent sim = scope.getSimulation();
		// hqnghi if layer come from micro-model
		final IModelDescription micro = desc.getModelDescription();
		final IModelDescription main = scope.getModel().getDescription();
		final boolean fromMicroModel = main.getMicroModel(micro.getMicroAlias()) != null;
		if (fromMicroModel) {
			final ExperimentAgent exp = (ExperimentAgent) scope.getRoot()
					.getExternMicroPopulationFor(micro.getMicroAlias() + "." + desc.getOriginName()).getAgent(0);
			sim = exp.getSimulation();
		}
		// end-hqnghi
		IEnvelope env = null;
		if (sim != null) {
			env = sim.getEnvelope();
		} else {
			env = GamaEnvelopeFactory.of(0, 100, 0, 100, 0, 0);
		}
		setEnvWidth(env.getWidth());
		setEnvHeight(env.getHeight());
		env.dispose();

		updateAutoSave(scope, facets.getExpr(IKeyword.AUTOSAVE));
		final IExpression toolbar = facets.getExpr(IKeyword.TOOLBAR);
		if (toolbar != null) {
			if (toolbar.getGamlType() == Types.BOOL) {
				setToolbarVisible(Cast.asBool(scope, toolbar.value(scope)));
			} else {
				setToolbarVisible(true);
				toolbarColor = GamaColorFactory.castToColor(scope, toolbar.value(scope));
			}
		}
		final IExpression fps = facets.getExpr("show_fps");
		if (fps != null) { setShowfps(Cast.asBool(scope, fps.value(scope))); }

		final IExpression nZ = facets.getExpr("z_near");
		if (nZ != null) { setZNear(Cast.asFloat(scope, nZ.value(scope))); }

		final IExpression fZ = facets.getExpr("z_far");
		if (fZ != null) { setZFar(Cast.asFloat(scope, fZ.value(scope))); }
		final IExpression denv = facets.getExpr("draw_env", "axes");
		if (denv != null) { setDrawEnv(Cast.asBool(scope, denv.value(scope))); }

		final IExpression ortho = facets.getExpr(IKeyword.ORTHOGRAPHIC_PROJECTION);
		if (ortho != null) { setOrtho(Cast.asBool(scope, ortho.value(scope))); }

		final IExpression keystone_exp = facets.getExpr(IKeyword.KEYSTONE);
		if (keystone_exp != null) {
			@SuppressWarnings ("unchecked") final List<IPoint> val = GamaListFactory.create(scope, Types.POINT,
					GamaListFactory.castToList(scope, keystone_exp.value(scope)));
			if (val.size() >= 4) { setKeystone(val); }
		}

		initRotationFacets(scope, facets);

		final IExpression lightOn = facets.getExpr(IKeyword.IS_LIGHT_ON);
		if (lightOn != null) { setLightOn(Cast.asBool(scope, lightOn.value(scope))); }

		initializePresetCameraDefinitions();
		cameraNameExpression = facets.getExpr(IKeyword.CAMERA);
		setCameraNameFromGaml(cameraNameExpression != null ? Cast.asString(scope, cameraNameExpression.value(scope))
				: IKeyword.DEFAULT);

		final IExpression fs = facets.getExpr(IKeyword.FULLSCREEN);
		if (fs != null) {
			int monitor;
			if (fs.getGamlType() == Types.BOOL) {
				monitor = Cast.asBool(scope, fs.value(scope)) ? 0 : -1;
			} else {
				monitor = Cast.asInt(scope, fs.value(scope));
			}
			setFullScreen(monitor);
		}

		// final IExpression use_shader = facets.getExpr("use_shader");
		// if (use_shader != null) {
		// this.useShader = Cast.asBool(scope, use_shader.value(scope));
		// }

		final IExpression color = facets.getExpr(IKeyword.BACKGROUND);
		if (color != null) {
			setBackgroundColor(GamaColorFactory.castToColor(scope, color.value(scope)));
			constantBackground = color.isConst();
		}

		final IExpression light = facets.getExpr("ambient_light");
		if (light != null) {
			IColor intensity;
			if (light.getGamlType().equals(Types.COLOR)) {
				intensity = GamaColorFactory.castToColor(scope, light.value(scope));
			} else {
				final int meanValue = Cast.asInt(scope, light.value(scope));
				intensity = GamaColorFactory.createWithRGBA(meanValue, meanValue, meanValue, 255);
			}
			lights.put(ILightDefinition.ambient, new GenericLightDefinition(ILightDefinition.ambient, -1, intensity));
		}

		final IExpression antialias = facets.getExpr("antialias");
		if (antialias != null) { setAntialias(Cast.asBool(scope, antialias.value(scope))); }

		final IExpression locked = facets.getExpr("locked");
		if (locked != null) { setCameraLocked(Cast.asBool(scope, locked.value(scope))); }

		if (camera != null) { camera.refresh(scope); }
		if (rotation != null) { rotation.refresh(scope); }
		lights.forEach((s, l) -> l.refresh(scope));
	}

	/**
	 * Update auto save.
	 *
	 * @param scope
	 *            the scope
	 * @param auto
	 *            the auto
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private void updateAutoSave(final IScope scope, final IExpression auto) throws GamaRuntimeException {
		if (auto == null) return;
		if (auto.getGamlType().equals(Types.POINT)) {
			IPoint result = GamaPointFactory.castToPoint(scope, auto.value(scope));
			setAutosave(result != null);
			setImageDimension(result);
		} else if (auto.getGamlType().equals(Types.STRING)) {
			String result = Cast.asString(scope, auto.value(scope));
			setAutosave(result != null && !result.isBlank());
			setAutosavePath(result);
		} else {
			setAutosave(Cast.asBool(scope, auto.value(scope)));
		}
	}

	/**
	 * Sets the z far.
	 *
	 * @param zF
	 *            the new z far
	 */
	@Override
	public void setZFar(final Double zF) {
		zFar = zF;

	}

	/**
	 * Sets the z near.
	 *
	 * @param zN
	 *            the new z near
	 */
	@Override
	public void setZNear(final Double zN) { zNear = zN; }

	/**
	 * Update.
	 *
	 * @param scope
	 *            the scope
	 * @param facets
	 *            the facets
	 */
	@Override
	public void update(final IScope scope, final Facets facets) {

		if (cameraNameExpression != null) {
			boolean isLocked = camera == null ? false : camera.isLocked();
			setCameraNameFromGaml(Cast.asString(scope, cameraNameExpression.value(scope)));
			if (camera != null) {
				// camera.reset();
				camera.setLocked(isLocked);
			}
		}

		if (camera != null) { camera.refresh(scope); }
		if (rotation != null) { rotation.refresh(scope); }
		lights.forEach((s, l) -> l.refresh(scope));

		updateAutoSave(scope, facets.getExpr(IKeyword.AUTOSAVE));
		// /////////////// dynamic Lighting ///////////////////

		if (!constantBackground) {

			final IExpression color = facets.getExpr(IKeyword.BACKGROUND);
			if (color != null) { setBackgroundColor(GamaColorFactory.castToColor(scope, color.value(scope))); }

		}

	}

	/**
	 * Checks if is open GL 2.
	 *
	 * @return true, if is open GL 2
	 */
	@Override
	public boolean isOpenGL2() { return OPENGL2.equals(displayType); }

	/**
	 * Checks if is web.
	 *
	 * @return true, if is web
	 */
	@Override
	public boolean isWeb() { return WEB.equals(displayType); }

	/**
	 * Checks if is open GL.
	 *
	 * @return true, if is open GL
	 */
	@Override
	public boolean is3D() {
		return is3D;
	}

	// ************************************************************************************************
	// ************************************************************************************************
	// * CAMERA
	// ************************************************************************************************
	// ************************************************************************************************

	/** The camera definitions. */
	private final Map<String, ICameraDefinition> cameraDefinitions = new LinkedHashMap<>();

	/** The current camera. */
	private ICameraDefinition camera;

	/** The camera name expression. */
	private IExpression cameraNameExpression;

	/**
	 * Adds the camera definition.
	 *
	 * @param legend
	 *            the name
	 * @param definition
	 *            the definition
	 */
	@Override
	public void addCameraDefinition(final ICameraDefinition definition) {
		cameraDefinitions.putIfAbsent(definition.getName(), definition);
	}

	/**
	 * Gets the distance coefficient.
	 *
	 * @return the distance coefficient
	 */
	@Override
	public double getCameraDistanceCoefficient() { return isDrawEnv() ? 1.4 : 1.2; }

	/**
	 * Reset camera.
	 */
	@Override
	public void resetCamera() {
		if (camera != null) { camera.reset(); }
	}

	/**
	 * Gets the distance.
	 *
	 * @return the distance
	 */
	@Override
	public double getCameraDistance() { return camera.getDistance(); }

	/**
	 * Sets the distance.
	 *
	 * @param distance
	 *            the new distance
	 */
	@Override
	public void setCameraDistance(final double distance) {
		camera.setDistance(distance);
	}

	/**
	 * Initialize preset camera definitions.
	 */
	private void initializePresetCameraDefinitions() {
		double w = getEnvWidth();
		double h = getEnvHeight();
		double max = Math.max(w, h) * getCameraDistanceCoefficient();
		IPoint target = GamaPointFactory.create(w / 2, -h / 2, 0); // Y negated
		for (String preset : ICameraDefinition.PRESETS) {
			addCameraDefinition(new GenericCameraDefinition(preset, target, getEnvWidth(), getEnvHeight(), max));
		}
		cameraDefinitions.putIfAbsent(IKeyword.DEFAULT,
				cameraDefinitions.get(GamaPreferences.Displays.OPENGL_DEFAULT_CAM.getValue()));
	}

	/**
	 * Sets the preset camera.
	 *
	 * @param newValue
	 *            the new preset camera
	 */
	@Override
	public void setCameraNameFromGaml(final String newValue) {
		if (camera != null && Objects.equal(newValue, camera.getName())) return;
		resetCamera();
		camera = cameraDefinitions.get(newValue);
		if (camera == null) { camera = cameraDefinitions.get(IKeyword.DEFAULT); }
		// notifyListeners(Changes.CAMERA_PRESET, newValue);
	}

	/**
	 * Sets the preset camera.
	 *
	 * @param newValue
	 *            the new preset camera
	 */
	@Override
	public void setCameraNameFromUser(final String newValue) {
		if (camera != null && Objects.equal(newValue, camera.getName())) return;
		// We force the camera name to remain the same by modifying the expression
		cameraNameExpression = GAML.getExpressionFactory().createConst(newValue, Types.STRING);
		// resetCamera();
		camera = cameraDefinitions.get(newValue);
		if (camera == null) { camera = cameraDefinitions.get(IKeyword.DEFAULT); }
		// notifyListeners(Changes.CAMERA_PRESET, newValue);
	}

	/**
	 * @return the cameraPos
	 */
	@Override
	public IPoint getCameraPos() { return camera.getLocation(); }

	/**
	 * @param cameraPos
	 *            the cameraPos to set
	 */
	@Override
	public void setCameraPos(final IPoint point) {
		camera.setLocation(point);
	}

	/**
	 * @return the cameraLookPos
	 */
	@Override
	public IPoint getCameraTarget() { return camera.getTarget(); }
	
	@Override
	public IPoint getCameraOrientation() { return camera.getTarget().minus(camera.getLocation());}

	/**
	 * @param cameraLookPos
	 *            the cameraLookPos to set
	 */
	@Override
	public void setCameraTarget(final IPoint point) {
		camera.setTarget(point);
	}

	/**
	 * @return the cameraLens
	 */
	@Override
	public double getCameraLens() { return camera.getLens(); }

	/**
	 * Gets the preset camera.
	 *
	 * @return the preset camera
	 */
	@Override
	public String getCameraName() { return camera.getName(); }

	/**
	 * Gets the camera names.
	 *
	 * @return the camera names
	 */
	@Override
	public Collection<String> getCameraNames() { return cameraDefinitions.keySet(); }

	/**
	 * Disable camera interactions.
	 *
	 * @param disableCamInteract
	 *            the disable cam interact
	 */
	@Override
	public void setCameraLocked(final boolean disableCamInteract) {
		camera.setLocked(disableCamInteract);
	}

	/**
	 * Camera interaction disabled.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean isCameraLocked() { return camera.isLocked(); }

	/**
	 * Checks if is camera dynamic.
	 *
	 * @return true, if is camera dynamic
	 */
	@Override
	public boolean isCameraDynamic() { return camera.isDynamic(); }

	// ************************************************************************************************
	// ************************************************************************************************
	// * ROTATION
	// ************************************************************************************************
	// ************************************************************************************************

	/** The rotation. */
	IRotationDefinition rotation;

	/**
	 * Inits the rotation facets.
	 *
	 * @param scope
	 *            the scope
	 * @param facets
	 *            the facets
	 */
	private void initRotationFacets(final IScope scope, final Facets facets) {
		final IExpression rotate_exp = facets.getExpr(IKeyword.ROTATE);
		if (rotate_exp != null) {
			final double val = Cast.asFloat(scope, rotate_exp.value(scope));
			setRotationAngle(val);
		}
	}

	/**
	 * Sets the rotation.
	 *
	 * @param rotation
	 *            the new rotation
	 */
	@Override
	public void setRotation(final IRotationDefinition rotation) { this.rotation = rotation; }

	/**
	 * @return
	 */
	@Override
	public boolean isContinuousRotationOn() { return rotation != null && rotation.isDynamic(); }

	/**
	 * Sets the continuous rotation.
	 *
	 * @param r
	 *            the new continuous rotation
	 */
	@Override
	public void setContinuousRotation(final boolean r) {
		if (rotation != null) { rotation.setDynamic(r); }
	}

	/**
	 * Gets the current rotation about Z.
	 *
	 * @return the current rotation about Z
	 */
	@Override
	public double getRotationAngle() { return rotation == null ? 0d : rotation.getCurrentAngle(); }

	/**
	 * Checks for rotation.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasRotation() {
		return rotation != null && rotation.getAngleDelta() != 0d && !rotation.getAxis().isNull();
	}

	/**
	 * Sets the z rotation angle.
	 *
	 * @param val
	 *            the new z rotation angle
	 */
	@Override
	public void setRotationAngle(final double val) {
		if (rotation != null) { rotation.setAngle(val); }
	}

	/**
	 * Gets the rotation center.
	 *
	 * @return the rotation center
	 */
	@Override
	public IPoint getRotationCenter() { return rotation != null ? rotation.getCenter().yNegated() : null; }

	/**
	 * Gets the rotation axis.
	 *
	 * @return the rotation axis
	 */
	@Override
	public IPoint getRotationAxis() { return rotation != null ? rotation.getAxis().yNegated() : null; }

	/**
	 * Reset Z rotation.
	 */
	@Override
	public void resetRotation() {
		if (rotation != null) { rotation.reset(); }
	}

	// ************************************************************************************************
	// ************************************************************************************************
	// * LIGHT
	// ************************************************************************************************
	// ************************************************************************************************

	/** The is light on. */
	private boolean isLightOn = true;

	/** The light index. */
	private int lightIndex = 1;

	/** The lights. */
	private final Map<String, ILightDefinition> lights = new LinkedHashMap<>() {
		{
			put(ILightDefinition.ambient, new GenericLightDefinition(ILightDefinition.ambient, -1,
					GamaPreferences.Displays.OPENGL_DEFAULT_LIGHT_INTENSITY.getValue()));
			put(IKeyword.DEFAULT, new GenericLightDefinition(IKeyword.DEFAULT, 0,
					GamaPreferences.Displays.OPENGL_DEFAULT_LIGHT_INTENSITY.getValue()));
		}
	};

	/**
	 * @return the isLightOn
	 */
	@Override
	public boolean isLightOn() { return isLightOn; }

	/**
	 * @param isLightOn
	 *            the isLightOn to set
	 */
	@Override
	public void setLightOn(final boolean isLightOn) { this.isLightOn = isLightOn; }

	/**
	 * Gets the diffuse lights.
	 *
	 * @return the diffuse lights
	 */
	@Override
	public Map<String, ILightDefinition> getLights() { return lights; }

	/**
	 * Adds the light definition.
	 *
	 * @param definition
	 *            the definition
	 */
	@Override
	public void addLightDefinition(final ILightDefinition definition) {
		String name = definition.getName();
		int index = lights.containsKey(name) ? lights.get(name).getId() : lightIndex++;
		definition.setId(index);
		lights.put(name, definition);
	}

	@Override
	public Set<DisplayDataListener> getListeners() { return listeners; }

}