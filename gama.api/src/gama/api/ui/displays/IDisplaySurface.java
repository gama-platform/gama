/*******************************************************************************************************
 *
 * IDisplaySurface.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;

import gama.api.kernel.agent.IAgent;
import gama.api.runtime.GeneralSynchronizer;
import gama.api.runtime.scope.IScoped;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.ui.IOutput;
import gama.api.ui.displays.IDisplayData.Changes;
import gama.api.ui.displays.IDisplayData.DisplayDataListener;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IEventLayerListener;
import gama.api.ui.layers.ILayer;
import gama.api.ui.layers.ILayerManager;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IDisposable;

/**
 * Class IDisplaySurface. Represents a concrete object on which layers can be drawn on screen. Instances of subclasses
 * are the 'display's of GAMA (java2D, openGL, image)
 *
 * Written by A. Drogoul
 *
 * @since26 nov. 2009
 *
 */
public interface IDisplaySurface extends DisplayDataListener, IScoped, IDisposable {

	/** The null. */
	IDisplaySurface NULL = new IDisplaySurface() {};

	/** The snapshot folder name. */
	String SNAPSHOT_FOLDER_NAME = "snapshots";

	/** The min zoom factor. */
	double MIN_ZOOM_FACTOR = 0.1;

	/** The max zoom factor. */
	int MAX_ZOOM_FACTOR = 10;

	/** The selection size. */
	double SELECTION_SIZE = 5; // pixels

	/**
	 * This sub-interface represents display surfaces relying on OpenGL
	 *
	 * @author drogoul
	 *
	 */
	public interface OpenGL extends IDisplaySurface {

		/**
		 * Gets the ROI dimensions.
		 *
		 * @return the ROI dimensions
		 */
		IEnvelope getROIDimensions();

		/**
		 * Sets the paused.
		 *
		 * @param flag
		 *            the new paused
		 */
		void setPaused(boolean flag);

		/**
		 * Select agent.
		 *
		 * @param attributes
		 *            the attributes
		 */
		void selectAgent(final IDrawingAttributes attributes);

		/**
		 * Selection in.
		 *
		 * @param env
		 *            the env
		 */
		void selectionIn(IEnvelope env);

	}

	/**
	 * Returns a BufferedImage that captures the current state of the surface on screen.
	 *
	 * @param width
	 *            the desired width of the image
	 * @param height
	 *            the desired height of the image
	 * @return a BufferedImage of size {width, height} with all layers drawn on it
	 */
	default BufferedImage getImage(final int width, final int height) {
		return null;
	}

	/**
	 * Changed.
	 *
	 * @param property
	 *            the property
	 * @param value
	 *            the value
	 */
	@Override
	default void changed(final Changes property, final Object value) {}

	/**
	 * Asks the surface to update its display, optionnaly forcing it to do so (if it is paused, for instance). A
	 * synchronizer (possibly null) is passed, that needs to be released when the physical display is done
	 **/
	default void updateDisplay(final boolean force, final GeneralSynchronizer synchronizer) {}

	/**
	 * Update display.
	 *
	 * @param force
	 *            the force
	 */
	default void updateDisplay(final boolean force) {
		updateDisplay(force, null);
	}

	/**
	 * Sets a concrete menu manager to be used for displaying menus on this surface
	 *
	 * @param displaySurfaceMenu
	 *            an object, normally instance of DisplaySurfaceMenu
	 */
	default void setMenuManager(final Object displaySurfaceMenu) {}

	/**
	 * Zoom in.
	 */
	default void zoomIn() {}

	/**
	 * Zoom out.
	 */
	default void zoomOut() {}

	/**
	 * Zoom fit.
	 */
	default void zoomFit() {}

	/**
	 * Toggles surface view lock.
	 */
	default void toggleLock() {}

	/**
	 * Gets the manager.
	 *
	 * @return the manager
	 */
	default ILayerManager getManager() { return ILayerManager.NULL; }

	/**
	 * Focus on.
	 *
	 * @param geometry
	 *            the geometry
	 */
	default void focusOn(final IShape geometry) {}

	/**
	 * Run the runnable in argument and refresh the output
	 *
	 * @param r
	 */
	default void runAndUpdate(final Runnable r) {
		r.run();
		updateDisplay(true);
	}

	/**
	 * @return the width of the panel
	 */
	default int getWidth() { return 0; }

	/**
	 * @return the height of the panel
	 */
	default int getHeight() { return 0; }

	/**
	 * Whatever is needed to do when the simulation has been reloaded.
	 *
	 * @param layerDisplayOutput
	 */
	default void outputReloaded() {}

	/**
	 * Gets the env width.
	 *
	 * @return the env width
	 */
	default double getEnvWidth() { return 0; }

	/**
	 * Gets the env height.
	 *
	 * @return the env height
	 */
	default double getEnvHeight() { return 0; }

	/**
	 * Gets the display width.
	 *
	 * @return the display width
	 */
	default double getDisplayWidth() { return 0; }

	/**
	 * Gets the display height.
	 *
	 * @return the display height
	 */
	default double getDisplayHeight() { return 0; }

	/**
	 * Gets the model coordinates.
	 *
	 * @return the model coordinates
	 */
	default IPoint getModelCoordinates() { return GamaPointFactory.getNullPoint(); }

	/**
	 * Gets the window coordinates.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the window coordinates
	 * @date 17 sept. 2023
	 */
	default IPoint getWindowCoordinates() { return GamaPointFactory.getNullPoint(); }

	/**
	 * Gets the model coordinates from.
	 *
	 * @param xOnScreen
	 *            the x on screen
	 * @param yOnScreen
	 *            the y on screen
	 * @param sizeInPixels
	 *            the size in pixels
	 * @param positionInPixels
	 *            the position in pixels
	 * @return the model coordinates from
	 */
	default IPoint getModelCoordinatesFrom(final int xOnScreen, final int yOnScreen, final Point sizeInPixels,
			final Point positionInPixels) {
		return GamaPointFactory.getNullPoint();
	}

	/**
	 * Select agent.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the collection
	 */
	default Collection<IAgent> selectAgent(final int x, final int y) {
		return Collections.emptyList();
	}

	/**
	 * @return the current zoom level (between 0 and 1).
	 */
	default double getZoomLevel() { return 0; }

	/**
	 * Sets the size.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	default void setSize(final int x, final int y) {}

	/**
	 * Gets the output.
	 *
	 * @return the output
	 */
	default IOutput.Display getOutput() { return null; }

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	default IDisplayData getData() { return null; }

	/**
	 * Layers changed.
	 */
	default void layersChanged() {}

	/**
	 * Adds the listener.
	 *
	 * @param e
	 *            the e
	 */
	default void addListener(final IEventLayerListener e) {}

	/**
	 * Removes the listener.
	 *
	 * @param e
	 *            the e
	 */
	default void removeListener(final IEventLayerListener e) {}

	/**
	 * Gets the layer listeners.
	 *
	 * @return the layer listeners
	 */
	default Collection<IEventLayerListener> getLayerListeners() { return Collections.emptyList(); }

	/**
	 * Gets the visible region for layer.
	 *
	 * @param currentLayer
	 *            the current layer
	 * @return the visible region for layer
	 */
	default IEnvelope getVisibleRegionForLayer(final ILayer currentLayer) {
		return null;
	}

	/**
	 * Gets the fps.
	 *
	 * @return the fps
	 */
	default int getFPS() { return 0; }

	/**
	 * @return true if the surface has been 'disposed' already
	 */
	default boolean isDisposed() { return false; }

	/**
	 * @return
	 */
	default void getModelCoordinatesInfo(final StringBuilder receiver) {}

	/**
	 * Dispatch key event.
	 *
	 * @param character
	 *            the character
	 */
	default void dispatchKeyEvent(final char character) {}

	/**
	 * Dispatch special key event.
	 *
	 * @param keyCode
	 *            the key code
	 */
	default void dispatchSpecialKeyEvent(final int keyCode) {}

	/**
	 * Dispatch mouse event.
	 *
	 * @param swtEventType
	 *            the swt event type
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	default void dispatchMouseEvent(final int swtEventType, final int x, final int y) {}

	/**
	 * Sets the mouse position.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	default void setMousePosition(final int x, final int y) {}

	/**
	 * Dragged to.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	default void draggedTo(final int x, final int y) {}

	/**
	 * Select agents around mouse.
	 */
	default void selectAgentsAroundMouse() {}

	/**
	 * Compute font.
	 *
	 * @param f
	 *            the f
	 * @return the font
	 */
	default Font computeFont(final Font f) {
		return f;
	}

	/**
	 * Can trigger contextual menu.
	 *
	 * @return true, if successful
	 */
	default boolean canTriggerContextualMenu() {
		return !getManager().hasMouseMenuEventLayer();
	}

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	@Override
	default IGraphicsScope getScope() { return null; }

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	default boolean isVisible() { return false; }

	/**
	 * Gets the i graphics.
	 *
	 * @return the i graphics
	 */
	default IGraphics getIGraphics() { return null; }

	/**
	 * Gets the bounds for snapshot.
	 *
	 * @return the bounds for snapshot
	 */
	default Rectangle getBoundsForRobotSnapshot() { return new Rectangle(); }

	/**
	 * Checks if the esc key has been redefined as an event in an event layer of this surface
	 *
	 * @return true, if is esc redefined
	 */
	default boolean isEscRedefined() { return getManager().hasEscEventLayer(); }

	/**
	 * Checks if is arrow redefined.
	 *
	 * @return true, if is arrow redefined
	 */
	default boolean isArrowRedefined() { return getManager().hasArrowEventLayer(); }

	/**
	 * Checks if this displaySuface needs to wait to be rendered by an external process (like a view, for instance)
	 *
	 * @return true, if is renderable
	 */
	default boolean shouldWaitToBecomeRendered() {
		return true;
	}

}
