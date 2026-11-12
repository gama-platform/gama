/*******************************************************************************************************
 *
 * JOGLRenderer.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumMap;
import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.GeneralSynchronizer;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.matrix.IField;
import gama.api.types.file.IGamaFile;
import gama.api.ui.displays.GeometryDrawing;
import gama.api.ui.displays.PixelDrawing;
import gama.api.ui.displays.IAsset;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.ILayer;
import gama.api.ui.layers.LayerCoordinates;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.interfaces.IImageProvider;
import gama.core.outputs.display.AbstractDisplayGraphics;
import gama.core.outputs.layers.OverlayLayer;
import gama.core.util.file.GamaGeometryFile;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.GamaFloatMatrix;
import gama.dev.DEBUG;
import gama.gaml.statements.draw.AssetDrawingAttributes;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;
import gama.ui.display.opengl.OpenGL;
import gama.ui.display.opengl.IOpenGL;
import gama.ui.display.opengl.renderer.helpers.IKeystoneHelper;
import gama.ui.display.opengl.renderer.helpers.ILightHelper;
import gama.ui.display.opengl.renderer.helpers.IPickingHelper;
import gama.ui.display.opengl.renderer.helpers.AbstractRendererHelper.Pass;
import gama.ui.display.opengl.renderer.helpers.CameraHelper;
import gama.ui.display.opengl.renderer.helpers.KeystoneHelper;
import gama.ui.display.opengl.renderer.helpers.LightHelper;
import gama.ui.display.opengl.renderer.helpers.PickingHelper;
import gama.ui.display.opengl.renderer.helpers.SceneHelper;
import gama.ui.display.opengl.renderer.helpers.AdaptiveTextHelper;
import gama.ui.display.opengl.scene.ModelScene;
import gama.ui.display.opengl.scene.layers.LayerObject.LayerState;
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
public class JOGLRenderer extends AbstractDisplayGraphics implements IOpenGLRenderer {

	private final EnumMap<IShape.Type, Geometry> primitiveGeometry = new EnumMap<>(IShape.Type.class);
	private final Function<IShape.Type, Geometry> primitiveSource = this::getPrimitiveGeometry;

	@Override
	public boolean supportsPrimitiveDrawing() { return true; }

	@Override
	public Rectangle2D drawPrimitive(final IShape.Type type, final double scale, final IPoint center,
			final IDrawingAttributes attributes) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		tryToHighlight(attributes);
		scene.addPrimitive(primitiveSource, scale, center, attributes);
		getPreparationDiagnostics().primitive();
		return rect;
	}

	/** Renderer-thread-only realization of the few primitives that still need polygon vertices. */
	private Geometry getPrimitiveGeometry(final IShape.Type type) {
		return primitiveGeometry.computeIfAbsent(type, t -> {
			final IShape shape = switch (t) {
				case CIRCLE, SPHERE -> GamaShapeFactory.buildCircle(1, origin);
				case SQUARE, CUBE -> GamaShapeFactory.buildSquare(1, origin);
				default -> throw new IllegalArgumentException("Unsupported drawing primitive: " + t);
			};
			final Geometry unitGeometry = shape.getInnerGeometry();
			GamaCoordinateSequenceFactory.pointsOf(unitGeometry).ensureClockwiseness();
			return unitGeometry;
		});
	}

	@Override
	public double[] getFieldBuffer(final Object owner, final int length) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		return scene == null ? super.getFieldBuffer(owner, length) : scene.getFieldBuffer(owner, length);
	}

	private final ThreadLocal<ILayer> renderingLayer = new ThreadLocal<>();
	private final ThreadLocal<LayerState> renderingState = new ThreadLocal<>();

	@Override
	public LayerState setRenderingLayerState(final LayerState state) {
		final var previous = renderingState.get();
		if (state == null) {
			renderingState.remove();
		} else {
			renderingState.set(state);
		}
		return previous;
	}

	@Override
	public ILayer setRenderingLayer(final ILayer layer) {
		final ILayer previous = renderingLayer.get();
		if (layer == null) {
			renderingLayer.remove();
		} else {
			renderingLayer.set(layer);
		}
		return previous;
	}

	private ILayer getScreenWorldLayer() {
		if (renderingState.get() != null) return null;
		final ILayer rendered = renderingLayer.get();
		final ILayer layer = rendered == null ? currentLayer : rendered;
		return layer != null && layer.getData().getCoordinates() == LayerCoordinates.SCREEN
				&& layer.getData().getContentCoordinates() == LayerCoordinates.WORLD ? layer : null;
	}

	static {
		DEBUG.OFF();
	}

	/** The keystone helper. */
	// Helpers
	private final IKeystoneHelper keystoneHelper;

	/** The picking helper. */
	private final IPickingHelper pickingHelper;

	/** The light helper. */
	private final ILightHelper lightHelper;

	/** The camera helper. */
	private final CameraHelper cameraHelper;

	/** The scene helper. */
	private final SceneHelper sceneHelper;

	private final AdaptiveTextHelper adaptiveTextHelper;

	private final RenderDiagnostics diagnostics = new RenderDiagnostics();

	/** The open GL. */
	// OpenGL back-end
	protected IOpenGL openGL;

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
		keystoneHelper = createKeystoneHelper();
		pickingHelper = createPickingHelper();
		lightHelper = createLightHelper();
		cameraHelper = new CameraHelper(this);
		sceneHelper = new SceneHelper(this);
		adaptiveTextHelper = new AdaptiveTextHelper(this);
	}

	@Override
	public AdaptiveTextHelper getAdaptiveTextHelper() { return adaptiveTextHelper; }

	@Override
	public RenderDiagnostics getDiagnostics() { return diagnostics; }

	@Override
	public void setDisplaySurface(final IDisplaySurface d) {
		super.setDisplaySurface(d);
		d.getScope().setGraphics(this);
		openGL = createOpenGL();
	}

	/** Backend factories run during construction; overrides must not depend on subclass field initialization. */
	protected IOpenGL createOpenGL() { return new OpenGL(this); }

	protected IKeystoneHelper createKeystoneHelper() { return new KeystoneHelper(this); }

	protected IPickingHelper createPickingHelper() { return new PickingHelper(this); }

	protected ILightHelper createLightHelper() { return new LightHelper(this); }

	@Override
	public void setCanvas(final GamaGLCanvas canvas) {
		this.canvas = canvas;
		canvas.addGLEventListener(this);
		cameraHelper.hook();
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		openGL.setGL(drawable.getGL());
		cameraHelper.initialize();
		openGL.initializeGLStates(data.getBackgroundColor());
		// Present the cleared background once while the first scene is being prepared.
		drawable.swapBuffers();
		lightHelper.initialize();
		// We mark the renderer as inited
		inited = true;
	}

	@Override
	public void fillBackground(final IColor bgColor) {
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
		if (isNotReadyToUpdate()) {
			sceneHelper.requestUpdate();
			if (synchronizer != null) { synchronizer.release(); }
			return false;
		}
		// while (!inited) {
		// try {
		// Thread.sleep(10);
		// } catch (final InterruptedException e) {
		// return false;
		// }
		// }
		if (sceneHelper.beginUpdatingScene()) return true;
		if (synchronizer != null) { synchronizer.release(); }
		return false;

	}

	@Override
	public boolean isNotReadyToUpdate() {
		if (super.isNotReadyToUpdate() || !inited || initialScenePending
				|| getCanvas() != null && !getCanvas().getVisibleStatus())
			return true;
		if (GAMA.isSynchronized()) return false;
		return sceneHelper.isNotReadyToUpdate();
	}

	@Override
	public void dispose() {
		super.dispose();
		primitiveGeometry.clear();
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
	 * @see gama.api.ui.displays.IGraphics#endDrawingLayers()
	 */
	@Override
	public void endDrawingLayers() {
		sceneHelper.endUpdatingScene();
		getSurface().invalidateVisibleRegions();
	}

	@Override
	public void display(final GLAutoDrawable drawable) {
		// JOGL otherwise swaps even when this callback returns without drawing a scene.
		drawable.setAutoSwapBufferMode(false);
		// Fullscreen reparenting can resize the native surface without delivering a reshape callback.
		reshape(drawable, 0, 0, drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
		if (!sceneHelper.isReady()) {
			scheduleInitialScene();
			schedulePendingSceneUpdate();
			return;
		}

		diagnostics.beginFrame();
		try (Pass c = keystoneHelper.render(); Pass d = openGL.beginScene();) {
			cameraHelper.update();
			lightHelper.draw();
			sceneHelper.draw();
			if (synchronizer != null) { synchronizer.release(); }
		} finally {
			diagnostics.endFrame(getSurface().getOutput().getTitle(), openGL.getClass().getName());
		}
		drawable.setAutoSwapBufferMode(true);
		schedulePendingSceneUpdate();
		//
		// if (!visible) {
		// // We make the canvas visible only after a first display has occured
		// WorkbenchHelper.asyncRun(() -> getCanvas().setVisible(true));
		// visible = true;
		// }

		if (first) {
			first = false;
			// Fullscreen entry is handled by ArrangeDisplayViews.decorateDisplays() during layout,
			// before the first render. The old UIJob here (100 ms delay) would fire after
			// decorateDisplays() had already entered fullscreen and call toggleFullScreen() a
			// second time — immediately exiting fullscreen again.
		}
	}

	/** The first. */
	boolean first = true;

	/** Defer the first scene until the visible viewport has been initialized. */
	private volatile boolean initialScenePending = true;

	/** Guards asynchronous initialization of the first scene. */
	private volatile boolean initialSceneScheduled;

	private volatile boolean sceneUpdateScheduled;

	private void schedulePendingSceneUpdate() {
		if (initialScenePending || !sceneHelper.hasPendingUpdate() || sceneUpdateScheduled) return;
		sceneUpdateScheduled = true;
		WorkbenchHelper.asyncRun(() -> {
			sceneUpdateScheduled = false;
			if (disposed || surface.isDisposed()) return;
			sceneHelper.retryPendingUpdate(() -> surface.updateDisplay(true, synchronizer));
		});
	}

	/** The synchronizer. */
	private GeneralSynchronizer synchronizer;

	/** Native viewport dimensions, which can change with DPI even when the logical size stays the same. */
	private int surfaceWidth, surfaceHeight;

	/**
	 * Requests the initial scene after the canvas becomes visible.
	 */
	public void onCanvasShown() {
		scheduleInitialScene();
	}

	/**
	 * Schedules the first scene on the UI thread so late show/resize events can settle first.
	 */
	private void scheduleInitialScene() {
		final GamaGLCanvas currentCanvas = getCanvas();
		if (!initialScenePending || initialSceneScheduled || !inited || currentCanvas == null
				|| currentCanvas.isDisposed() || !currentCanvas.getVisibleStatus()
				|| openGL.getViewWidth() <= 0 || openGL.getViewHeight() <= 0) return;
		initialSceneScheduled = true;
		WorkbenchHelper.asyncRun(() -> {
			initialSceneScheduled = false;
			startInitialScene();
		});
	}

	/**
	 * Starts the first scene once the visible OpenGL viewport has usable dimensions.
	 */
	private void startInitialScene() {
		if (!initialScenePending) return;
		final GamaGLCanvas currentCanvas = getCanvas();
		if (disposed || surface.isDisposed() || !inited || currentCanvas == null || currentCanvas.isDisposed()
				|| !currentCanvas.getVisibleStatus() || openGL.getViewWidth() <= 0 || openGL.getViewHeight() <= 0)
			return;
		initialScenePending = false;
		surface.updateDisplay(true, synchronizer);
	}

	@Override
	public void reshape(final GLAutoDrawable drawable, final int arg1, final int arg2, final int w, final int h) {
		// JOGL's backing scale can differ from SWT's monitor zoom after a cross-monitor reparent.
		final int[] dimensions = drawable.getNativeSurface().convertToWindowUnits(new int[] { w, h });
		final int width = dimensions[0], height = dimensions[1];
		if (width <= 0 || height <= 0
				|| openGL.getViewWidth() == width && openGL.getViewHeight() == height
						&& surfaceWidth == w && surfaceHeight == h)
			return;
		final GL gl = drawable.getContext().getGL();
		keystoneHelper.reshape(width, height);
		openGL.reshape(gl, width, height);
		surfaceWidth = w;
		surfaceHeight = h;
		if (initialScenePending && getCanvas().getVisibleStatus()) {
			scheduleInitialScene();
		}
		surface.updateDisplay(true, synchronizer);
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		sceneHelper.garbageCollect(openGL);
		sceneHelper.dispose();
		adaptiveTextHelper.dispose();
		openGL.dispose();
		keystoneHelper.dispose();
		cameraHelper.dispose();
		drawable.removeGLEventListener(this);
		disposed = true;
		// surface.getOutput().setRendered(true);
	}

	/**
	 *
	 * IGraphics DRAWING SAMPLING
	 *
	 */

	@Override
	public Rectangle2D drawAsset(final IAsset file, final IDrawingAttributes attributes) {
		if (file == null) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		tryToHighlight(attributes);
		if (file instanceof GamaGeometryFile geometry) {
			final String key = geometry.getDrawingCacheKey(getSurface().getScope());
			scene.addGeometryFile(geometry, attributes, key);
			openGL.cacheGeometry(geometry, key);
		} else if (file instanceof IImageProvider iip) {
			final IImageProvider captured = captureImage(iip, attributes.useCache());
			if (attributes.useCache() && !(captured instanceof PixelDrawing)) {
				openGL.cacheTexture(captured);
			}
			scene.addImage(captured, attributes);
		}

		return rect;
	}

	private IImageProvider captureImage(final IImageProvider source, final boolean useCache) {
		if (source instanceof PixelDrawing || source instanceof IGamaFile) return source;
		if (source.getClass() == GamaFloatMatrix.class || source.getClass() == GamaField.class) {
			final GamaFloatMatrix matrix = (GamaFloatMatrix) source;
			return new PixelDrawing(matrix.getCols(getSurface().getScope()), matrix.getRows(getSurface().getScope()),
					matrix.getMatrix());
		}
		final BufferedImage image = source.getImage(getSurface().getScope(), useCache);
		if (image == null) throw GamaRuntimeException.error(
				"Unable to obtain the display image " + source.getId(), getSurface().getScope());
		return PixelDrawing.fromImage(image);
	}

	private void captureTextures(final IDrawingAttributes attributes) {
		final List<?> textures = attributes.getTextures();
		if (textures == null) return;
		ArrayList<Object> captured = null;
		for (int i = 0; i < textures.size(); i++) {
			final Object source = textures.get(i);
			final Object snapshot = source instanceof IImageProvider image
					? captureImage(image, attributes.useCache())
					: source instanceof BufferedImage image ? PixelDrawing.fromImage(image) : source;
			if (snapshot != source) {
				if (captured == null) { captured = new ArrayList<>(textures); }
				captured.set(i, snapshot);
			}
		}
		if (captured != null) { attributes.setTextures(captured); }
	}

	/**
	 * Draw field.
	 *
	 * @param fieldValues
	 *            the field values
	 * @param attributes
	 *            the attributes
	 * @return the rectangle 2 D
	 */
	@Override
	public Rectangle2D drawField(final IField fieldValues, final IDrawingAttributes attributes) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		captureTextures(attributes);
		final List<?> textures = attributes.getTextures();
		if (textures != null && !textures.isEmpty()) {
			for (final Object img : textures) {
				if (img instanceof IImageProvider im && !(im instanceof PixelDrawing)) {
					openGL.cacheTexture(im);
				}
			}
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
	public Rectangle2D drawShape(final Geometry shape, final IDrawingAttributes attributes) {
		if (shape == null) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		tryToHighlight(attributes);
		captureTextures(attributes);
		scene.addGeometry(shape, attributes);
		return rect;
	}

	@Override
	public boolean supportsGeometryCommands() { return true; }

	@Override
	public Rectangle2D drawGeometry(final GeometryDrawing command,
			final IDrawingAttributes attributes) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		tryToHighlight(attributes);
		captureTextures(attributes);
		scene.addGeometry(command, attributes);
		return rect;
	}

	/**
	 * Draw image.
	 *
	 * @param img
	 *            the img
	 * @param attributes
	 *            the attributes
	 * @return the rectangle 2 D
	 */
	@Override
	public Rectangle2D drawImage(final BufferedImage img, final IDrawingAttributes attributes) {
		if (img == null) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		// if (attributes.useCache()) { openGL.cacheTexture(img); }
		scene.addImage(PixelDrawing.fromImage(img), attributes);
		tryToHighlight(attributes);
		if (attributes.getBorder() != null) {
			drawGridLine(GamaPointFactory.create(img.getWidth(), img.getHeight()), attributes.getBorder());
		}
		return rect;
	}

	@Override
	public Rectangle2D drawPixels(final PixelDrawing command,
			final IDrawingAttributes attributes) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		scene.addImage(command, attributes);
		tryToHighlight(attributes);
		if (attributes.getBorder() != null) {
			drawGridLine(GamaPointFactory.create(command.getCols(null), command.getRows(null)), attributes.getBorder());
		}
		return rect;
	}

	@Override
	public double getDevicePixelRatio() {
		final int[] pixels = getCanvas().getNativeSurface().convertToPixelUnits(new int[] { 1024, 1024 });
		return Math.max(pixels[0], pixels[1]) / 1024d;
	}

	@Override
	public Rectangle2D drawChart(final BufferedImage chart) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		final AssetDrawingAttributes attributes = new AssetDrawingAttributes(null, true);
		attributes.setSmoothTexture(true);
		scene.addImage(PixelDrawing.fromImage(chart), attributes);
		return rect;
	}

	/**
	 * Try to highlight.
	 *
	 * @param attributes
	 *            the attributes
	 */
	protected void tryToHighlight(final IDrawingAttributes attributes) {
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
	public void drawGridLine(final IPoint dimensions, final IColor lineColor) {
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return;
		final int columns = (int) dimensions.getX();
		final int rows = (int) dimensions.getY();
		final double width = getEnvWidth();
		final double height = getEnvHeight();
		final IColor color = GamaColorFactory.get(lineColor.getRGB());
		final DrawingAttributes attributes = new ShapeDrawingAttributes(null, color, color, IShape.Type.MULTILINESTRING);
		attributes.setEmpty(true);
		// One object keeps grid size from diluting the scene's anti-z-fighting increment.
		scene.addGridLines(columns, rows, width, height, attributes);
	}

	/**
	 * Draw string.
	 *
	 * @param string
	 *            the string
	 * @param attributes
	 *            the attributes
	 * @return the rectangle 2 D
	 */
	@Override
	public Rectangle2D drawString(final String string, final IDrawingAttributes attributes) {
		if (string == null || string.isEmpty()) return null;
		final ModelScene scene = sceneHelper.getSceneToUpdate();
		if (scene == null) return null;
		scene.addString(string, attributes);
		return null;
	}

	/**
	 *
	 * DIMENSIONS, RATIOS AND LOCATIONS SAMPLING
	 *
	 */

	@Override
	public final IPoint getCameraPos() { return cameraHelper.getPosition(); }

	@Override
	public final IPoint getCameraTarget() { return cameraHelper.getTarget(); }

	@Override
	public final IPoint getCameraOrientation() { return cameraHelper.getOrientation(); }

	@Override
	public final IPoint getCameraUp() { return cameraHelper.getUp(); }

	@Override
	public final IPoint getCameraRight() { return cameraHelper.getRight(); }

	@Override
	public double getxRatioBetweenPixelsAndModelUnits() {
		if (getCoordinateSystem() == LayerCoordinates.SCREEN) return 1d;
		final var state = renderingState.get();
		if (state != null && state.coordinates() == LayerCoordinates.SCREEN) {
			return state.contentRatio() > 0 ? state.contentRatio() : state.width() / getEnvWidth();
		}
		final ILayer layer = getScreenWorldLayer();
		if (layer instanceof OverlayLayer overlay) return overlay.getData().getContentRatio();
		if (layer != null) return layer.getData().getSizeInPixels().getX() / getEnvWidth();
		return DPIHelper.autoScaleDown(getCanvas().getMonitor(), openGL.getRatios().getX());
	}

	@Override
	public double getyRatioBetweenPixelsAndModelUnits() {
		if (getCoordinateSystem() == LayerCoordinates.SCREEN) return 1d;
		final var state = renderingState.get();
		if (state != null && state.coordinates() == LayerCoordinates.SCREEN) {
			return state.contentRatio() > 0 ? state.contentRatio() : state.height() / getEnvHeight();
		}
		final ILayer layer = getScreenWorldLayer();
		if (layer instanceof OverlayLayer overlay) return overlay.getData().getContentRatio();
		if (layer != null) return layer.getData().getSizeInPixels().getY() / getEnvHeight();
		return DPIHelper.autoScaleDown(getCanvas().getMonitor(), openGL.getRatios().getY());
	}

	@Override
	public double getAbsoluteRatioBetweenPixelsAndModelsUnits() {
		if (getCoordinateSystem() == LayerCoordinates.SCREEN) return 1d;
		final var state = renderingState.get();
		if (state != null && state.coordinates() == LayerCoordinates.SCREEN)
			return Math.min(getxRatioBetweenPixelsAndModelUnits(), getyRatioBetweenPixelsAndModelUnits());
		if (getScreenWorldLayer() != null)
			return Math.min(getxRatioBetweenPixelsAndModelUnits(), getyRatioBetweenPixelsAndModelUnits());
		return Math.min(getViewHeight() / data.getEnvHeight(), getViewWidth() / data.getEnvWidth());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getWidth()
	 */
	@Override
	public final double getWidth() {
		return getEnvWidth() * getWorldLayoutScale();
	}

	private double getWorldLayoutScale() {
		// Layout uses the fitted world rectangle, not a camera-projected point that can cross behind the eye.
		return Math.min(getViewWidth() / getEnvWidth(), getViewHeight() / getEnvHeight()) * surface.getZoomLevel();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getHeight()
	 */
	@Override
	public final double getHeight() {
		return getEnvHeight() * getWorldLayoutScale();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getRealWorldPointFromWindowPoint (java.awt.Point)
	 */
	@Override
	public IPoint getRealWorldPointFromWindowPoint(final IPoint mouse) {
		return getCameraHelper().getWorldPositionFrom(GamaPointFactory.create(mouse.getX(), mouse.getY()),
				GamaPointFactory.create());
	}

	@Override
	public final int getDisplayWidth() { return (int) Math.round(getWidth()); }

	@Override
	public final int getDisplayHeight() { return (int) Math.round(getHeight()); }

	@Override
	public int getViewWidth() {
		// Use the same logical viewport as the projection, including after fullscreen reparenting.
		if (openGL != null && openGL.getViewWidth() > 0) return openGL.getViewWidth();
		final GamaGLCanvas currentCanvas = getCanvas();
		return currentCanvas == null || currentCanvas.isDisposed() ? super.getViewWidth()
				: currentCanvas.getClientArea().width;
	}

	@Override
	public int getViewHeight() {
		if (openGL != null && openGL.getViewHeight() > 0) return openGL.getViewHeight();
		final GamaGLCanvas currentCanvas = getCanvas();
		return currentCanvas == null || currentCanvas.isDisposed() ? super.getViewHeight()
				: currentCanvas.getClientArea().height;
	}

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
	public IKeystoneHelper getKeystoneHelper() { return keystoneHelper; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getPickingHelper()
	 */
	@Override
	public IPickingHelper getPickingHelper() { return pickingHelper; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getOpenGLHelper()
	 */
	@Override
	public IOpenGL getOpenGLHelper() { return openGL; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.ui.display.opengl.renderer.IOpenGLRenderer#getLightHelper()
	 */
	@Override
	public ILightHelper getLightHelper() { return lightHelper; }

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

	/**
	 * Sets the synchronizer.
	 *
	 * @param synchronizer
	 *            the new synchronizer
	 */
	public void setSynchronizer(final GeneralSynchronizer synchronizer) {
		if (synchronizer == null) return;
		this.synchronizer = synchronizer;

	}

}
