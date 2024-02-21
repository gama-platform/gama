/*******************************************************************************************************
 *
 * JOGLRenderer.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import gama.core.common.interfaces.IAsset;
import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IImageProvider;
import gama.core.common.interfaces.ILayer;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.outputs.display.AbstractDisplayGraphics;
import gama.core.outputs.layers.charts.ChartOutput;
import gama.core.runtime.GAMA;
import gama.core.util.GamaColor;
import gama.core.util.file.GamaGeometryFile;
import gama.core.util.matrix.IField;
import gama.dev.DEBUG;
import gama.gaml.statements.draw.AssetDrawingAttributes;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.MeshDrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;
import gama.gaml.statements.draw.TextDrawingAttributes;
import gama.gaml.types.GamaGeometryType;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.renderer.helpers.CameraHelper;
import gama.ui.display.opengl.renderer.helpers.KeystoneHelper;
import gama.ui.display.opengl.renderer.helpers.LightHelper;
import gama.ui.display.opengl.renderer.helpers.PickingHelper;
import gama.ui.display.opengl.renderer.helpers.SceneHelper;
import gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass;
import gama.ui.display.opengl.scene.ModelScene;
import gama.ui.display.opengl.view.GamaGLCanvas;
import gama.ui.display.opengl.view.SWTOpenGLDisplaySurface;
import gama.ui.shared.utils.DPIHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * This class plays the role of Renderer and IGraphics. Class JOGLRenderer.
 *
 * @author drogoul
 * @since 27 avr. 2015
 *
 */

/**
 * The Class JOGLRenderer.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class JOGLRenderer extends AbstractDisplayGraphics implements IOpenGLRenderer {

	static {
		DEBUG.ON();
	}

	/** The keystone helper. */
	// Helpers
	private final KeystoneHelper keystoneHelper;

	/** The picking helper. */
	private final PickingHelper pickingHelper;

	/** The light helper. */
	private final LightHelper lightHelper;

	/** The camera helper. */
	private final CameraHelper cameraHelper;

	/** The scene helper. */
	private final SceneHelper sceneHelper;

	/** The open GL. */
	// OpenGL back-end
	protected OpenGL openGL;

	/** The disposed. */
	// State
	protected volatile boolean inited, disposed;

	/** The canvas. */
	// Canvas
	protected GamaGLCanvas canvas;

	/**
	 * Instantiates a new JOGL renderer.
	 *
	 * @param surface
	 *            the surface
	 */
	public JOGLRenderer(final IDisplaySurface surface) {
		setDisplaySurface(surface);
		keystoneHelper = new KeystoneHelper(this);
		pickingHelper = new PickingHelper(this);
		lightHelper = new LightHelper(this);
		cameraHelper = new CameraHelper(this);
		sceneHelper = new SceneHelper(this);
	}

	@Override
	public void setDisplaySurface(final IDisplaySurface d) {
		super.setDisplaySurface(d);
		d.getScope().setGraphics(this);
		openGL = new OpenGL(this);
	}

	@Override
	public void setCanvas(final GamaGLCanvas canvas) {
		this.canvas = canvas;
		canvas.addGLEventListener(this);
		cameraHelper.hook();
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		openGL.setGL2(drawable.getGL().getGL2());
		cameraHelper.initialize();
		openGL.initializeGLStates(data.getBackgroundColor());
		lightHelper.initialize();
		// We mark the renderer as inited
		inited = true;
	}

	@Override
	public void fillBackground(final Color bgColor) {
		openGL.setCurrentObjectAlpha(1);
	}

	@Override
	public SWTOpenGLDisplaySurface getSurface() { return (SWTOpenGLDisplaySurface) surface; }

	@Override
	public final GamaGLCanvas getCanvas() { return canvas; }

	@Override
	public void initScene() {
		final ModelScene scene = sceneHelper.getSceneToRender();
		if (scene != null) { scene.reload(); }
	}

	@Override
	public boolean beginDrawingLayers() {
		if (isNotReadyToUpdate()) // DEBUG.OUT(">>> " + getSurface().getOutput().getName() + " is not ready to update");
			return false;
		// while (!inited) {
		// try {
		// Thread.sleep(10);
		// } catch (final InterruptedException e) {
		// return false;
		// }
		// }
		return sceneHelper.beginUpdatingScene();

	}

	@Override
	public boolean isNotReadyToUpdate() {
		if (super.isNotReadyToUpdate() || !inited || !getCanvas().getVisibleStatus()) return true;
		if (GAMA.isSynchronized()) return false;
		return sceneHelper.isNotReadyToUpdate();
	}

	@Override
	public void dispose() {
		super.dispose();
		dispose(canvas);
	}

	@Override
	public void beginDrawingLayer(final ILayer layer) {
		super.beginDrawingLayer(layer);
		sceneHelper.beginDrawingLayer(layer, currentLayerAlpha);
	}

	/**
	 * Method endDrawingLayers()
	 *
	 * @see gama.core.common.interfaces.IGraphics#endDrawingLayers()
	 */
	@Override
	public void endDrawingLayers() {
		sceneHelper.endUpdatingScene();
		getSurface().invalidateVisibleRegions();
	}

	@Override
	public void display(final GLAutoDrawable drawable) {
		if (!sceneHelper.isReady()) return;

		try (Pass c = keystoneHelper.render(); Pass d = openGL.beginScene();) {
			cameraHelper.update();
			lightHelper.draw();
			sceneHelper.draw();
		}
		//
		// if (!visible) {
		// // We make the canvas visible only after a first display has occured
		// WorkbenchHelper.asyncRun(() -> getCanvas().setVisible(true));
		// visible = true;
		// }

		if (first) {
			first = false;
			if (getData().fullScreen() > -1) {
				WorkbenchHelper.runInUI("FS", 100, m -> this.getSurface().getOutput().getView().toggleFullScreen());
			}
		}
	}

	/** The first. */
	boolean first = true;

	@Override
	public void reshape(final GLAutoDrawable drawable, final int arg1, final int arg2, final int w, final int h) {
		int width = DPIHelper.autoScaleDown(getCanvas().getMonitor(), w),
				height = DPIHelper.autoScaleDown(getCanvas().getMonitor(), h);
		// int width = w, height = h;
		// See #2628 and https://github.com/sgothel/jogl/commit/ca7f0fb61b0a608b6e684a5bbde71f6ecb6e3fe0
		// width = scaleDownIfMac(width);
		// height = scaleDownIfMac(height);
		if (width <= 0 || height <= 0 || openGL.getViewWidth() == width && openGL.getViewHeight() == height) return;
		final GL2 gl = drawable.getContext().getGL().getGL2();
		keystoneHelper.reshape(width, height);
		openGL.reshape(gl, width, height);
		// sceneHelper.reshape(width, height);
		surface.updateDisplay(true);
		getCanvas().updateVisibleStatus(getCanvas().isVisible());
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		sceneHelper.garbageCollect(openGL);
		sceneHelper.dispose();
		openGL.dispose();
		keystoneHelper.dispose();
		cameraHelper.dispose();
		drawable.removeGLEventListener(this);
		disposed = true;
	}

	/**
	 *
	 * IGraphics DRAWING METHODS
	 *
	 */

	// @Override
	public boolean cannotDraw() {
		return sceneHelper.getSceneToUpdate() == null;
	}

	@Override
	public Rectangle2D drawAsset(final IAsset file, final DrawingAttributes attributes) {
		if (file == null) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		tryToHighlight(attributes);
		if (file instanceof GamaGeometryFile) {
			scene.addGeometryFile((GamaGeometryFile) file, attributes);
			openGL.cacheGeometry((GamaGeometryFile) file);
		} else if (file instanceof IImageProvider iip) {
			if (attributes.useCache()) { openGL.cacheTexture(iip); }
			scene.addImage(file, attributes);
		}

		return rect;
	}

	@Override
	public Rectangle2D drawField(final IField fieldValues, final MeshDrawingAttributes attributes) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		final List<?> textures = attributes.getTextures();
		if (textures != null && !textures.isEmpty()) {
			for (final Object img : textures) { if (img instanceof IImageProvider im) { openGL.cacheTexture(im); } }
		}
		scene.addField(fieldValues, attributes);
		/*
		 * This line has been removed to fix the issue 1174 if ( gridColor != null ) { drawGridLine(img, gridColor); }
		 */
		return rect;
	}

	/**
	 * Method drawShape. Add a given JTS Geometry in the list of all the existing geometry that will be displayed by
	 * openGl.
	 */
	@Override
	public Rectangle2D drawShape(final Geometry shape, final DrawingAttributes attributes) {
		if (shape == null) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		tryToHighlight(attributes);
		scene.addGeometry(shape, attributes);
		return rect;
	}

	@Override
	public Rectangle2D drawImage(final BufferedImage img, final DrawingAttributes attributes) {
		if (img == null) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		// if (attributes.useCache()) { openGL.cacheTexture(img); }
		scene.addImage(img, attributes);
		tryToHighlight(attributes);
		if (attributes.getBorder() != null) {
			drawGridLine(new GamaPoint(img.getWidth(), img.getHeight()), attributes.getBorder());
		}
		return rect;
	}

	@Override
	public Rectangle2D drawChart(final ChartOutput chart) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		int x = getLayerWidth();
		int y = getLayerHeight();
		// y = x = Math.min(x, y);
		y = x = (int) (Math.min(x, y) * GamaPreferences.Displays.CHART_QUALITY.getValue());
		// TODO See if it not possible to generate directly a texture renderer instead
		final BufferedImage im = chart.getImage(x, y, getSurface().getData().isAntialias());
		scene.addImage(im, new AssetDrawingAttributes(null, true));
		return rect;
	}

	/**
	 * Try to highlight.
	 *
	 * @param attributes
	 *            the attributes
	 */
	protected void tryToHighlight(final DrawingAttributes attributes) {
		if (highlight) { attributes.setHighlighted(data.getHighlightColor()); }
	}

	/**
	 * Draw grid line.
	 *
	 * @param dimensions
	 *            the dimensions
	 * @param lineColor
	 *            the line color
	 */
	public void drawGridLine(final GamaPoint dimensions, final Color lineColor) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return;
		double stepX, stepY;
		final double cellWidth = getEnvWidth() / dimensions.x;
		final double cellHeight = getEnvHeight() / dimensions.y;
		final GamaColor color = GamaColor.get(lineColor.getRGB());
		final DrawingAttributes attributes = new ShapeDrawingAttributes(null, color, color, IShape.Type.GRIDLINE);
		attributes.setEmpty(true);
		for (double i = 0; i < dimensions.x; i++) {
			for (double j = 0; j < dimensions.y; j++) {
				stepX = i + 0.5;
				stepY = j + 0.5;
				final Geometry g = GamaGeometryType
						.buildRectangle(cellWidth, cellHeight, new GamaPoint(stepX * cellWidth, stepY * cellHeight))
						.getInnerGeometry();
				scene.addGeometry(g, attributes);
			}
		}
	}

	@Override
	public Rectangle2D drawString(final String string, final TextDrawingAttributes attributes) {
		if (string == null || string.isEmpty()) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		// Multiline: Issue #780
		if (string.contains("\n")) {
			int i = 0;
			final double shift = attributes.getFont().getSize() / this.getAbsoluteRatioBetweenPixelsAndModelsUnits();
			for (final String s : string.split("\n")) {
				// DEBUG.OUT("Attributes Font Size: " + attributes.font.getSize());
				// DEBUG.OUT("Get Y Ratio: " + getyRatioBetweenPixelsAndModelUnits());
				drawString(s, attributes.copyTranslatedBy(new GamaPoint(0, shift * i++)));
			}
			return null;
		}
		// openGL.cacheFont(attributes.getFont());
		attributes.getLocation().setY(-attributes.getLocation().getY());
		scene.addString(string, attributes);
		return null;
	}

	/**
	 *
	 * DIMENSIONS, RATIOS AND LOCATIONS METHODS
	 *
	 */

	@Override
	public final GamaPoint getCameraPos() { return cameraHelper.getPosition(); }

	@Override
	public final GamaPoint getCameraTarget() { return cameraHelper.getTarget(); }

	@Override
	public final GamaPoint getCameraOrientation() { return cameraHelper.getOrientation(); }

	@Override
	public double getxRatioBetweenPixelsAndModelUnits() {
		return DPIHelper.autoScaleDown(getCanvas().getMonitor(), openGL.getRatios().x);
	}

	@Override
	public double getyRatioBetweenPixelsAndModelUnits() {
		return DPIHelper.autoScaleDown(getCanvas().getMonitor(), openGL.getRatios().y);
	}

	@Override
	public double getAbsoluteRatioBetweenPixelsAndModelsUnits() {
		return Math.min(
				DPIHelper.autoScaleDown(getCanvas().getMonitor(), canvas.getSurfaceHeight() / data.getEnvHeight()),
				DPIHelper.autoScaleDown(getCanvas().getMonitor(), canvas.getSurfaceWidth() / data.getEnvWidth()));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getWidth()
	 */
	@SuppressWarnings ("restriction")
	@Override
	public final double getWidth() {
		// DEBUG.OUT(
		// "Result of getWidth: "
		// + PlatformHelper.autoScaleDown(canvas.getSurfaceWidth()) * (float) surface.getZoomLevel(),
		// false);
		// DEBUG.OUT(" -- Canvas surface width " + canvas.getSurfaceWidth(), false);
		// DEBUG.OUT("Canvas size " + canvas.getSize().x, false);
		// DEBUG.OUT(" -- Width of world in pixels computed by OpenGL " + openGL.getPixelWidthAndHeightOfWorld()[0]);
		// return canvas.getSize().x * surface.getZoomLevel();
		return openGL.getPixelWidthAndHeightOfWorld()[0] * (float) surface.getZoomLevel();
		// return PlatformHelper.autoScaleDown(canvas.getSurfaceWidth()) *
		// PlatformHelper.autoScaleDown(canvas.getSurfaceWidth()) * (float) surface.getZoomLevel();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getHeight()
	 */
	@Override
	public final double getHeight() {
		return openGL.getPixelWidthAndHeightOfWorld()[1] * (float) surface.getZoomLevel();
		// return canvas.getSurfaceHeight() * surface.getZoomLevel();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getRealWorldPointFromWindowPoint (java.awt.Point)
	 */
	@Override
	public GamaPoint getRealWorldPointFromWindowPoint(final GamaPoint mouse) {
		return getCameraHelper().getWorldPositionFrom(new GamaPoint(mouse.x, mouse.y), new GamaPoint());
	}

	@Override
	public final int getDisplayWidth() { return (int) Math.round(getWidth()); }

	@Override
	public final int getDisplayHeight() { return (int) Math.round(getHeight()); }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getCameraHelper()
	 */

	@Override
	public CameraHelper getCameraHelper() { return cameraHelper; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getKeystoneHelper()
	 */
	@Override
	public KeystoneHelper getKeystoneHelper() { return keystoneHelper; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getPickingHelper()
	 */
	@Override
	public PickingHelper getPickingHelper() { return pickingHelper; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getOpenGLHelper()
	 */
	@Override
	public OpenGL getOpenGLHelper() { return openGL; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getLightHelper()
	 */
	@Override
	public LightHelper getLightHelper() { return lightHelper; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getSceneHelper()
	 */
	@Override
	public SceneHelper getSceneHelper() { return sceneHelper; }

	@Override
	public boolean isDisposed() { return disposed; }

	@Override
	public boolean hasDrawnOnce() {
		return !first;
	}

}
