/*******************************************************************************************************
 *
 * ModelScene.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene;

import org.locationtech.jts.geom.Geometry;

import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IField;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.ILayer;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.util.file.GamaGeometryFile;
import gama.dev.DEBUG;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.IOpenGLRenderer;
import gama.ui.display.opengl.scene.layers.AxesLayerObject;
import gama.ui.display.opengl.scene.layers.FrameLayerObject;
import gama.ui.display.opengl.scene.layers.LayerObject;
import gama.ui.display.opengl.scene.layers.LayerObjectWithTrace;
import gama.ui.display.opengl.scene.layers.OverlayLayerObject;

/**
 *
 * The class ModelScene. A repository for all the objects that constitute the scene of a model : strings, images,
 * shapes... 04/03/14: Now organized by layers to address the issue of z depth
 *
 * @author drogoul
 * @since 3 mai 2013
 *
 */
public class ModelScene {

	static {
		DEBUG.OFF();
	}

	/** The Constant AXES_KEY. */
	public static final String AXES_KEY = "__axes__0";

	/** The Constant FRAME_KEY. */
	public static final String FRAME_KEY = "__frame__0";

	/** The layers. */
	protected final IMap<String, LayerObject> layers = GamaMapFactory.create();

	/** The current layer. */
	protected LayerObject currentLayer;

	/** The renderer. */
	protected final IOpenGLRenderer renderer;

	/** The rendered. */
	private volatile boolean rendered = false;

	/**
	 * Instantiates a new model scene.
	 *
	 * @param renderer
	 *            the renderer
	 * @param withWorld
	 *            the with world
	 */
	public ModelScene(final IOpenGLRenderer renderer, final boolean withWorld) {
		this.renderer = renderer;
		// maxZ = renderer.getMaxEnvDim() * GamaPreferences.Displays.OPENGL_Z_FACTOR.getValue() / 10d;
		if (withWorld) { initWorld(); }
	}

	/**
	 * Inits the world.
	 */
	protected void initWorld() {
		if (renderer.getData().isDrawEnv()) {
			layers.put(FRAME_KEY, new FrameLayerObject(renderer));
			layers.put(AXES_KEY, new AxesLayerObject(renderer));
		}
	}

	/**
	 * @param context
	 *            Called every new iteration when updateDisplay() is called on the surface
	 */
	public void wipe(final IOpenGL gl) {
		layers.forEach((name, layer) -> { if (layer != null && !layer.isStatic()) { layer.clear(gl); } });
		// Wipe the textures.
		gl.deleteVolatileTextures();
	}

	/**
	 * Draw.
	 *
	 * @param gl
	 *            the gl
	 */
	public void draw(final IOpenGL gl) {
		// if (index++ == 0) {
		// DEBUG.OUT("Drawing the first scene");
		// } else {
		// DEBUG.OUT("Drawing scene " + index);
		// }
		double maxZ = renderer.getMaxEnvDim() * 0.005;
		long objectNumber = 0, worldLayerNumber = 0;
		for (final LayerObject layer : layers.values()) {
			if (layer != null && layer.isVisible()) {
				objectNumber += layer.numberOfZTranslatedObjects();
				if (!layer.isOverlay()) { worldLayerNumber++; }
			}
		}
		final double zIncrement = renderer.getData().isMapOrder() || objectNumber < 1
				|| !GamaPreferences.Displays.OPENGL_Z_FIGHTING.getValue() ? 0d
				: maxZ / Math.max(objectNumber, worldLayerNumber) * GamaPreferences.Displays.OPENGL_Z_FACTOR.getValue();

		final double previousIncrement = gl.getCurrentZIncrement();
		final double previousTranslation = gl.getCurrentZTranslation();
		gl.push(GLMatrixFunc.GL_MODELVIEW);
		try {
			gl.setZIncrement(renderer.getData().isOrtho() ? 0D : zIncrement);
			// AD called here so that it is inside the keystone drawing. See #3285
			gl.rotateModel();
			long objectIndex = 0;
			long layerIndex = 0;
			for (LayerObject layer : layers.values()) {
				if (layer != null && layer.isVisible()) {
					final boolean worldLayer = !layer.isOverlay();
					if (worldLayer) { layerIndex++; }
					gl.setCurrentZTranslation(objectIndex * gl.getCurrentZIncrement());
					objectIndex += layer.numberOfZTranslatedObjects();
					gl.push(GLMatrixFunc.GL_MODELVIEW);
					try {
						if (worldLayer) { gl.translateBy(0, 0, layerIndex * zIncrement); }
						if (renderer.getPickingHelper().isBeginningPicking() && !layer.isPickable()) { continue; }
						layer.draw(gl);
					} catch (final RuntimeException r) {
						DEBUG.ERR("Runtime error " + r.getMessage() + " in OpenGL loop");
						r.printStackTrace();
					} finally {
						gl.pop(GLMatrixFunc.GL_MODELVIEW);
					}
				}
			}
			rendered = true;
		} finally {
			gl.setZIncrement(previousIncrement);
			gl.setCurrentZTranslation(previousTranslation);
			gl.pop(GLMatrixFunc.GL_MODELVIEW);
		}
	}

	/**
	 * Adds the string.
	 *
	 * @param string
	 *            the string
	 * @param attributes
	 *            the attributes
	 */
	public void addString(final String string, final IDrawingAttributes attributes) {
		if (currentLayer != null) { currentLayer.addString(string, attributes); }
	}

	/**
	 * Adds the geometry file.
	 *
	 * @param file
	 *            the file
	 * @param attributes
	 *            the attributes
	 */
	public void addGeometryFile(final GamaGeometryFile file, final IDrawingAttributes attributes) {
		if (currentLayer != null) { currentLayer.addFile(file, attributes); }
	}

	/**
	 * Adds the image.
	 *
	 * @param img
	 *            the img
	 * @param attributes
	 *            the attributes
	 */
	public void addImage(final Object img, final IDrawingAttributes attributes) {
		if (currentLayer != null) { currentLayer.addImage(img, attributes); }
	}

	/**
	 * Adds the geometry.
	 *
	 * @param geometry
	 *            the geometry
	 * @param attributes
	 *            the attributes
	 */
	public void addGeometry(final Geometry geometry, final IDrawingAttributes attributes) {
		if (currentLayer != null) { currentLayer.addGeometry(geometry, attributes); }
	}

	/**
	 * Adds the field.
	 *
	 * @param fieldValues
	 *            the field values
	 * @param attributes
	 *            the attributes
	 */
	public void addField(final IField fieldValues, final IDrawingAttributes attributes) {
		if (currentLayer != null) { currentLayer.addField(fieldValues, attributes); }
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		// DEBUG.OUT("ModelScene #" + myIndex + ": Layers keys before disposing = " + layers.keySet());
		layers.clear();
		currentLayer = null;
	}

	/**
	 * Begin drawing layers.
	 */
	public void beginDrawingLayers() {}

	/**
	 * End drawing layers.
	 */
	public void endDrawingLayers() {
		// zIncrement = computeVisualZIncrement();
	}

	/**
	 * Rendered.
	 *
	 * @return true, if successful
	 */
	public boolean rendered() {
		return rendered;
	}

	/**
	 * Reload.
	 */
	public void reload() {
		dispose();
		initWorld();
	}

	/**
	 * Begin drawing layer.
	 *
	 * @param layer
	 *            the layer
	 * @param alpha
	 *            the alpha
	 */
	public void beginDrawingLayer(final ILayer layer, final Double alpha) {
		final String key = layer.getName() + layer.getDefinition().getOrder();
		currentLayer = layers.get(key);
		if (currentLayer == null) {
			// DEBUG.OUT("ModelScene #" + myIndex + ": Layer " + layer.getName() + " is not present in the scene");
			currentLayer = layer.isOverlay() ? new OverlayLayerObject(renderer, layer)
					: layer.getData().getTrace() > 0 ? new LayerObjectWithTrace(renderer, layer)
					: new LayerObject(renderer, layer);
			// DEBUG.OUT("ModelScene #" + myIndex + ": Creating layer " + " : static " + currentLayer.isStatic()
			// + "; trace " + currentLayer.hasTrace());
			layers.put(key, currentLayer);
		}
		// DEBUG.OUT("ModelScene #" + myIndex + ": Layers keys after creation = " + layers.keySet());
		currentLayer.setAlpha(alpha);
	}

	/**
	 * @return
	 */
	public ModelScene copyStatic() {
		// DEBUG.OUT("Creating static scene");
		final ModelScene newScene = new ModelScene(renderer, false);
		// DEBUG.OUT("ModelScene #" + myIndex + ": layers keys before copying to static = " + layers.keySet());
		layers.forEach((name, layer) -> {

			// DEBUG.OUT("ModelScene #" + myIndex + ": Examining layer " + name + " : static " + layer.isStatic()
			// + "; trace " + layer.hasTrace());
			if (layer.isStatic() || layer.hasTrace()) {
				// DEBUG.OUT("===>> Adding " + name + " as static ");
				newScene.layers.put(name, layer);
			}
		});

		return newScene;
	}

}
