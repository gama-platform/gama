/*******************************************************************************************************
 *
 * SWTOpenGLDisplaySurface.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and
 * simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.view;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.locationtech.jts.geom.Envelope;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;

import gama.annotations.precompiler.GamlAnnotations.display;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.ILayer;
import gama.core.common.interfaces.ILayerManager;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.filter.Different;
import gama.core.outputs.LayeredDisplayData;
import gama.core.outputs.LayeredDisplayData.Changes;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.display.LayerManager;
import gama.core.outputs.layers.IEventLayerListener;
import gama.core.outputs.layers.OverlayLayer;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.PlatformHelper;
import gama.dev.DEBUG;
import gama.extension.image.GamaImage;
import gama.extension.image.ImageHelper;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.ui.display.opengl.renderer.JOGLRenderer;
import gama.ui.experiment.menus.AgentsMenu;
import gama.ui.experiment.views.displays.DisplaySurfaceMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.DPIHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Class OpenGLSWTDisplaySurface.
 *
 * @author drogoul
 * @since 25 mars 2015
 *
 */
@display (
		value = { "opengl", "3d" })
@doc ("Displays that uses the OpenGL technology to display their layers in 3D")
public class SWTOpenGLDisplaySurface implements IDisplaySurface.OpenGL {

	static {
		DEBUG.ON();

	}

	/** The animator. */
	GLAnimatorControl animator;

	/** The renderer. */
	JOGLRenderer renderer;

	/** The zoom fit. */
	protected boolean zoomFit = true;

	/** The listeners. */
	Set<IEventLayerListener> listeners = new HashSet<>();

	/** The output. */
	final LayeredDisplayOutput output;

	/** The layer manager. */
	final LayerManager layerManager;

	/** The menu manager. */
	protected DisplaySurfaceMenu menuManager;
	//
	// /** The temp focus. */
	// protected IExpression temp_focus;

	/** The scope. */
	IGraphicsScope scope;

	/** The synchronizer. */
	// public IDisplaySynchronizer synchronizer;

	/** The parent. */
	final Composite parent;

	/** The disposed. */
	volatile boolean disposed;

	/** The already updating. */
	private volatile boolean alreadyUpdating;

	/** The current mouse location converted to a world position */
	private GamaPoint world_position;

	/**
	 * Instantiates a new SWT open GL display surface.
	 *
	 * @param objects
	 *            the objects
	 */
	public SWTOpenGLDisplaySurface(final Object... objects) {
		output = (LayeredDisplayOutput) objects[0];
		parent = (Composite) objects[1];
		output.getData().addListener(this);
		output.setSurface(this);
		setDisplayScope(output.getScope().copyForGraphics("in opengl display"));
		layerManager = new LayerManager(this, output);
		if (!layerManager.stayProportional()) { output.getData().setDrawEnv(false); }
		renderer = createRenderer();
		animator = new GamaGLCanvas(parent, renderer, this).getAnimator();
		animator.start();
	}

	/**
	 * Creates the renderer.
	 *
	 * @return the i open GL renderer
	 */
	protected JOGLRenderer createRenderer() {
		return new JOGLRenderer(this);
	}

	@Override
	public void setMenuManager(final Object menuManager) { this.menuManager = (DisplaySurfaceMenu) menuManager; }

	/**
	 * Method getImage()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getImage()
	 */
	@Override
	public GamaImage getImage(final int desiredWidth, final int desiredHeight) {
		if (desiredWidth == 0 || desiredHeight == 0 || !renderer.hasDrawnOnce()) return null;
		// We first render at the right dimensions and then we scale
		Rectangle dimensions = this.getBoundsForRegularSnapshot();
		int w = dimensions.width;
		int h = dimensions.height;
		final GLAutoDrawable glad = renderer.getCanvas();
		if (glad == null) return null;
		GL2 gl = glad.getGL().getGL2();
		if (gl == null) return null;
		GLContext context = gl.getContext();
		if (context == null) return null;
		final boolean current = context.isCurrent();
		if (!current) { context.makeCurrent(); }
		GamaImage[] image = new GamaImage[1];
		glad.invoke(true, drawable -> {
			// See #2628 and https://github.com/sgothel/jogl/commit/ca7f0fb61b0a608b6e684a5bbde71f6ecb6e3fe0
			final ByteBuffer buffer = getBuffer(w, h);
			// be sure we are reading from the right fbo (here is supposed to be the default one)
			// bind the right buffer to read from
			gl.glReadBuffer(GL.GL_BACK); // or GL.GL_FRONT ?
			gl.glReadPixels(0, 0, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			ColorModel cm = new ComponentColorModel(cs, new int[] { 8, 8, 8, 8 }, true, false, Transparency.TRANSLUCENT,
					DataBuffer.TYPE_BYTE);
			SampleModel sm = cm.createCompatibleSampleModel(w, h);
			WritableRaster raster = new WritableRaster(sm, dbuf, new Point()) {};
			GamaImage im = GamaImage.from(cm, raster, false);
			// TODO Seems to take a very long time -- verify

			if (desiredWidth != w || desiredHeight != h) {
				im = ImageHelper.scaleImage(im, desiredWidth, desiredHeight);
			}
			ImageHelper.flipImageVertically(im);
			image[0] = im;
			return true;
		});
		if (!current) { glad.getGL().getContext().release(); }
		return image[0];
	}

	/** The buffer. */
	ByteBuffer buffer;

	/** The dbuf. */
	DataBuffer dbuf;

	/** The at. */
	AffineTransform at;

	/**
	 * Gets the buffer.
	 *
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @return the buffer
	 */
	protected ByteBuffer getBuffer(final int w, final int h) {

		if (buffer == null || buffer.capacity() != w * h * 4) {
			buffer = Buffers.newDirectByteBuffer(w * h * 4);
			// Build a wrapper around the buffer (to use for the raster of the image)
			dbuf = new DataBuffer(DataBuffer.TYPE_BYTE, w * h * 4) {
				@Override
				public void setElem(final int bank, final int i, final int val) {
					buffer.put(i, (byte) val);
				}

				@Override
				public int getElem(final int bank, final int i) {
					return buffer.get(i);
				}
			};
			// Build the affine transform to flip the image
			at = new AffineTransform();
			at.concatenate(AffineTransform.getScaleInstance(1, -1));
			at.concatenate(AffineTransform.getTranslateInstance(0, h));

		} else {
			buffer.rewind();
		}

		return buffer;
	}

	/**
	 * Method updateDisplay()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#updateDisplay(boolean)
	 */
	@Override
	public void updateDisplay(final boolean force, final Semaphore synchronizer) {
		if (alreadyUpdating) return;
		try {
			alreadyUpdating = true;
			renderer.setSynchronizer(synchronizer);
			layerManager.drawLayersOn(renderer);
		} finally {
			alreadyUpdating = false;
		}
	}

	@Override
	public double getDisplayWidth() { return renderer.getWidth(); }

	@Override
	public double getDisplayHeight() { return renderer.getHeight(); }

	/**
	 * Method zoomIn()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#zoomIn()
	 */
	@Override
	public void zoomIn() {
		if (renderer.getData().isCameraLocked()) return;
		renderer.getCameraHelper().zoom(true);
	}

	/**
	 * Method zoomOut()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#zoomOut()
	 */
	@Override
	public void zoomOut() {
		if (renderer.getData().isCameraLocked()) return;
		renderer.getCameraHelper().zoom(false);
	}

	/**
	 * Method zoomFit()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#zoomFit()
	 */
	@Override
	public void zoomFit() {
		// if (renderer.getData().cameraInteractionDisabled())
		// return;
		renderer.getCameraHelper().initialize();
		output.getData().resetRotation();
		output.getData().setZoomLevel(renderer.getCameraHelper().zoomLevel(), true);
		// output.getData().setZoomLevel(LayeredDisplayData.INITIAL_ZOOM, true, true);
		zoomFit = true;
	}

	@Override
	public void toggleLock() {
		renderer.getCameraHelper().toggleCamera();
	}

	/**
	 * Method getManager()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getManager()
	 */
	@Override
	public ILayerManager getManager() { return layerManager; }

	/**
	 * Method focusOn()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#focusOn(gama.core.metamodel.shape.IShape)
	 */
	@Override
	public void focusOn(final IShape geometry) {
		// FIXME: Need to compute the depth of the shape to adjust ZPos value.
		// FIXME: Problem when the geometry is a point how to determine the
		// maxExtent of the shape?
		// FIXME: Problem when an agent is placed on a layer with a z_value how
		// to get this z_layer value to offset it?
		renderer.getCameraHelper().zoomFocus(geometry.getEnvelope().yNegated());
	}

	/**
	 * Method waitForUpdateAndRun()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#waitForUpdateAndRun(java.lang.Runnable)
	 */
	@Override
	public void runAndUpdate(final Runnable r) {
		r.run();
		if (getScope().isPaused()) { updateDisplay(true); }
		if (animator.isPaused()) {
			animator.resume();
			animator.pause();
		}
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getWidth()
	 */
	@Override
	public int getWidth() {
		return renderer.getCanvas().getSurfaceWidth();
		// return size.x;
	}

	/**
	 * Method getHeight()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getHeight()
	 */
	@Override
	public int getHeight() {
		return renderer.getCanvas().getSurfaceHeight();
		// return size.y;
	}

	/**
	 * Method outputReloaded()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#outputReloaded()
	 */
	@Override
	public void outputReloaded() {
		setDisplayScope(output.getScope().copyForGraphics("in opengl display"));
		if (!GamaPreferences.Runtime.ERRORS_IN_DISPLAYS.getValue()) { getScope().disableErrorReporting(); }
		renderer.initScene();
		layerManager.outputChanged();

		// resizeImage(getWidth(), getHeight(), true);
		if (zoomFit) { zoomFit(); }
	}

	/**
	 * Method addMouseListener()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addListener(final IEventLayerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Method removeMouseListener()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#removeMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void removeListener(final IEventLayerListener listener) {
		listeners.remove(listener);

	}

	@Override
	public Collection<IEventLayerListener> getLayerListeners() { return listeners; }

	/**
	 * Method getEnvWidth()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getEnvWidth()
	 */
	@Override
	public double getEnvWidth() { return output.getData().getEnvWidth(); }

	/**
	 * Method getEnvHeight()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getEnvHeight()
	 */
	@Override
	public double getEnvHeight() { return output.getData().getEnvHeight(); }

	/**
	 * Method getModelCoordinates()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getModelCoordinates()
	 */
	@Override
	public GamaPoint getModelCoordinates() {
		return world_position;
		// final GamaPoint mp = renderer.getCameraHelper().getMousePosition();
		// DEBUG.OUT("Coordinates in display " + mp);
		// if (mp == null) return null;
		// final GamaPoint p = renderer.getRealWorldPointFromWindowPoint(mp);
		// DEBUG.OUT("Coordinates in env " + p);
		// if (p == null) return null;
		// return new GamaPoint(p.x, -p.y);
	}

	@Override
	public GamaPoint getWindowCoordinates() {
		return renderer.getCameraHelper().getMousePosition();
		// DEBUG.OUT("Coordinates in display " + mp);
		// if (mp == null) return null;
		// final GamaPoint p = renderer.getRealWorldPointFromWindowPoint(mp);
		// DEBUG.OUT("Coordinates in env " + p);
		// if (p == null) return null;
		// return new GamaPoint(p.x, -p.y);
	}

	@Override
	public void getModelCoordinatesInfo(final StringBuilder sb) {
		boolean canObtainInfo = getManager().isProvidingCoordinates();
		if (!canObtainInfo) {
			sb.append("No world coordinates");
			return;
		}
		canObtainInfo = getManager().isProvidingWorldCoordinates();
		if (!canObtainInfo) {
			sb.append("No world coordinates");
			return;
		}
		// By default, returns the coordinates in the world.
		final GamaPoint point = getModelCoordinates();
		final String x = point == null ? "N/A" : String.format("%5.2f", point.getX());
		final String y = point == null ? "N/A" : String.format("%5.2f", point.getY());
		sb.append(String.format("X%8s | Y%8s", x, y));
	}

	@Override
	public Envelope getVisibleRegionForLayer(final ILayer currentLayer) {
		if (currentLayer instanceof OverlayLayer) return getScope().getSimulation().getEnvelope();
		Envelope e = currentLayer.getData().getVisibleRegion();
		if (e == null) {
			e = new Envelope();
			final Point origin = new Point(0, 0);
			int xc = -origin.x;
			int yc = -origin.y;
			e.expandToInclude(currentLayer.getModelCoordinatesFrom(xc, yc, this));
			xc = xc + renderer.getCanvas().getSurfaceWidth();
			yc = yc + renderer.getCanvas().getSurfaceHeight();
			e.expandToInclude(currentLayer.getModelCoordinatesFrom(xc, yc, this));
			currentLayer.getData().setVisibleRegion(e);
		}
		return e;
	}

	/**
	 * Method getModelCoordinatesFrom()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getModelCoordinatesFrom(int, int, java.awt.Point,
	 *      java.awt.Point)
	 */
	@Override
	public GamaPoint getModelCoordinatesFrom(final int xOnScreen, final int yOnScreen, final Point sizeInPixels,
			final Point positionInPixels) {
		final GamaPoint mp = new GamaPoint(xOnScreen, yOnScreen);
		final GamaPoint p = renderer.getRealWorldPointFromWindowPoint(mp);
		return new GamaPoint(p.x, -p.y);
	}

	/**
	 * Method selectAgent()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#selectAgent(int, int)
	 */
	@Override
	public Collection<IAgent> selectAgent(final int x, final int y) {
		final GamaPoint pp = getModelCoordinatesFrom(x, y, null, null);
		return scope.getRoot().getTopology().getNeighborsOf(scope, new GamaPoint(pp.getX(), pp.getY()),
				renderer.getMaxEnvDim() / 100, Different.with());
	}

	/**
	 * Method getZoomLevel()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getZoomLevel()
	 */
	@Override
	public double getZoomLevel() {
		if (output.getData().getZoomLevel() == null) { output.getData().setZoomLevel(computeInitialZoomLevel(), true); }
		return output.getData().getZoomLevel();
	}

	/**
	 * Compute initial zoom level.
	 *
	 * @return the double
	 */
	protected Double computeInitialZoomLevel() {
		return renderer.getCameraHelper().zoomLevel();
	}

	/**
	 * Method getDisplayScope()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getDisplayScope()
	 */
	@Override
	public IGraphicsScope getScope() { return scope; }

	/**
	 * Method getOutput()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getOutput()
	 */
	@Override
	public LayeredDisplayOutput getOutput() { return output; }

	/**
	 * Method setPaused()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface.OpenGL#setPaused(boolean)
	 */
	@Override
	public void setPaused(final boolean paused) {
		if (paused) {
			animator.pause();
		} else {
			animator.resume();
		}
	}

	/**
	 * Method selectAgents()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface.OpenGL#selectAgents(gama.core.metamodel.agent.IAgent)
	 */
	@Override
	public void selectAgent(final DrawingAttributes attributes) {
		IAgent ag = null;
		boolean withHighlight = true;
		if (attributes != null) {
			if (attributes.getSpeciesName() != null) {
				// The picked image is a grid or an image of a grid
				withHighlight = false;
				final GamaPoint pickedPoint = renderer
						.getRealWorldPointFromWindowPoint(renderer.getCameraHelper().getLastMousePressedPosition());
				ag = scope.getRoot().getPopulationFor(attributes.getSpeciesName()).getAgent(scope,
						new GamaPoint(pickedPoint.x, -pickedPoint.y));
			} else {
				ag = attributes.getAgentIdentifier();
			}
		}
		/** The cleanup. */
		Runnable cleanup = ag != null ? () -> { renderer.getPickingHelper().setPicking(false); } : () -> {
			renderer.getPickingHelper().setPicking(false);
			// Necessary to avoir situations like issue #3232. The result is however a bit of flickering
			getManager().forceRedrawingLayers();
			updateDisplay(true);
		};
		if (withHighlight) {
			menuManager.buildMenu((int) renderer.getCameraHelper().getMousePosition().x,
					(int) renderer.getCameraHelper().getMousePosition().y, ag, cleanup,
					AgentsMenu.getHighlightActionFor(ag));
		} else {
			menuManager.buildMenu((int) renderer.getCameraHelper().getMousePosition().x,
					(int) renderer.getCameraHelper().getMousePosition().y, ag, cleanup);
		}
	}

	/**
	 * Method selectSeveralAgents()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface.OpenGL#selectSeveralAgents(java.util.Collection, int)
	 */
	@Override
	public void selectionIn(final Envelope3D env) {

		final Envelope3D envInWorld = Envelope3D.withYNegated(env);
		final Collection<IAgent> agents = scope.getTopology().getSpatialIndex().allInEnvelope(scope,
				envInWorld.centre(), envInWorld, new Different(), false);
		final Map<String, Runnable> actions = new LinkedHashMap<>();
		final Map<String, Image> images = new HashMap<>();
		images.put(renderer.getCameraHelper().isStickyROI() ? "Hide region" : "Keep region visible",
				GamaIcon.named(IGamaIcons.MENU_INSPECT).image());
		images.put("Focus on region", GamaIcon.named(IGamaIcons.DISPLAY_TOOLBAR_ZOOMFIT).image());
		actions.put(renderer.getCameraHelper().isStickyROI() ? "Hide region" : "Keep region visible",
				() -> renderer.getCameraHelper().toogleROI());
		actions.put("Focus on region", () -> renderer.getCameraHelper().zoomFocus(env));
		WorkbenchHelper.run(() -> {
			final Menu menu = menuManager.buildROIMenu((int) renderer.getCameraHelper().getMousePosition().x,
					(int) renderer.getCameraHelper().getMousePosition().y, agents, actions, images);
			menu.addMenuListener(new MenuListener() {

				@Override
				public void menuHidden(final MenuEvent e) {
					animator.resume();
					// Will be run after the selection
					WorkbenchHelper.asyncRun(() -> renderer.getCameraHelper().cancelROI());

				}

				@Override
				public void menuShown(final MenuEvent e) {
					animator.pause();
				}
			});

			menu.setVisible(true);
		});

	}

	/**
	 * Sets the display scope.
	 *
	 * @param scope
	 *            the new display scope
	 */
	protected void setDisplayScope(final IGraphicsScope scope) {
		if (this.scope != null) { GAMA.releaseScope(this.scope); }
		this.scope = scope;
	}

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		if (layerManager != null) { layerManager.dispose(); }
		if (animator != null && animator.isStarted()) { animator.stop(); }
		this.menuManager = null;
		this.listeners.clear();
		this.renderer = null;
		GAMA.releaseScope(getScope());
		setDisplayScope(null);
		// if (getOutput() != null) { getOutput().setRendered(true); }
	}

	@Override
	public LayeredDisplayData getData() { return output.getData(); }

	/**
	 * Method changed()
	 *
	 * @see gama.core.outputs.LayeredDisplayData.DisplayDataListener#changed(int, boolean)
	 */
	@Override
	public void changed(final Changes property, final Object value) {
		if (renderer == null) return;
		switch (property) {
			case ZOOM:
				renderer.getCameraHelper().zoom((Double) value);
				break;
			default:
				break;

		}

	}

	/**
	 * Method setSize()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#setSize(int, int)
	 */
	@Override
	public void setSize(final int x, final int y) {}

	@Override
	public void layersChanged() {
		renderer.getSceneHelper().layersChanged();
	}

	/**
	 * Invalidate visible regions.
	 */
	public void invalidateVisibleRegions() {
		for (final ILayer layer : layerManager.getItems()) { layer.getData().setVisibleRegion(null); }
	}

	/**
	 * Method getFPS()
	 *
	 * @see gama.core.common.interfaces.IDisplaySurface#getFPS()
	 */
	@Override
	public int getFPS() { return Math.round(renderer.getCanvas().getLastFPS()); }

	// @Override
	// public boolean isRealized() {
	// if (renderer == null) return false;
	// final GLAutoDrawable d = renderer.getCanvas();
	// if (d == null) return false;
	// return d.isRealized();
	// }

	// @Override
	// public boolean isRendered() {
	// if (renderer == null || renderer.getSceneHelper().getSceneToRender() == null) return false;
	// return renderer.getSceneHelper().getSceneToRender().rendered();
	// }

	@Override
	public boolean isDisposed() { return disposed; }

	@Override
	public Envelope3D getROIDimensions() { return renderer.getCameraHelper().getROIEnvelope(); }

	@Override
	public void dispatchKeyEvent(final char e) {
		for (final IEventLayerListener gl : listeners) { gl.keyPressed(String.valueOf(e)); }
	}

	@Override
	public void dispatchSpecialKeyEvent(final int e) {
		DEBUG.OUT("Special key received by the surface " + e);
		for (final IEventLayerListener gl : listeners) { gl.specialKeyPressed(e); }
	}

	@Override
	public void dispatchMouseEvent(final int swtMouseEvent, final int x, final int y) {
		for (final IEventLayerListener gl : listeners) {
			switch (swtMouseEvent) {
				case SWT.MouseDown:
					gl.mouseDown(x, y, 1);
					break;
				case SWT.MouseUp:
					gl.mouseUp(x, y, 1);
					break;
				case SWT.MouseMove:
					gl.mouseMove(x, y);
					break;
				case SWT.DragDetect:
					gl.mouseDrag(x, y, 1);
					break;
				case SWT.MouseEnter:
					gl.mouseEnter(x, y);
					break;
				case SWT.MouseExit:
					gl.mouseExit(x, y);
					break;
				case SWT.MenuDetect:
					gl.mouseMenu(x, y);
					break;
			}
		}
	}

	@Override
	public void setMousePosition(final int x, final int y) {
		// Callable from non OpenGL context
		world_position =
				renderer.getCameraHelper().getWorldPositionFrom(new GamaPoint(x, y), new GamaPoint()).yNegated();
	}

	@Override
	public void selectAgentsAroundMouse() {
		// Nothing to do (taken in charge by the camera)
	}

	@Override
	public void draggedTo(final int x, final int y) {
		// Nothing to do (taken in charge by the camera

	}

	// @Override
	// public void setDisplaySynchronizer(final IDisplaySynchronizer s) { synchronizer = s; }

	@Override
	public boolean isVisible() {
		if (renderer == null) return false;
		return renderer.getCanvas().getVisibleStatus();
	}

	@Override
	public IGraphics getIGraphics() { return renderer; }

	@Override
	public Rectangle getBoundsForRobotSnapshot() {
		var rect = WorkbenchHelper.displaySizeOf(renderer.getCanvas());
		// For some reason, macOS requires the native dimension for the robot to snapshot correctly
		if (PlatformHelper.isMac()) { rect = DPIHelper.autoScaleUp(renderer.getCanvas().getMonitor(), rect); }
		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Gets the bounds for regular snapshot.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the bounds for regular snapshot
	 * @date 17 sept. 2023
	 */
	public Rectangle getBoundsForRegularSnapshot() {
		var rect = WorkbenchHelper.displaySizeOf(renderer.getCanvas());
		// For some reason, macOS and Windows require the native dimension for the internal process to snapshot
		// correctly
		if (PlatformHelper.isMac() || PlatformHelper.isWindows()) {
			rect = DPIHelper.autoScaleUp(renderer.getCanvas().getMonitor(), rect);
		}
		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}

}
