/*******************************************************************************************************
 *
 * ILayerManager.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import gama.api.types.geometry.IShape;
import gama.api.ui.IItemList;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.utils.interfaces.IDisposable;

/**
 * The class ILayerManager. Manages a fixed set of layers on behalf of a IDisplaySurface
 *
 * @author drogoul
 * @since 15 dec. 2011
 *
 */
public interface ILayerManager extends IItemList<ILayer>, IDisposable {

	/** The null layer manager. */
	ILayerManager NULL = new ILayerManager() {};

	/**
	 * Forces all layers to reload on the surface
	 */
	default void outputChanged() {}

	/**
	 * @param xc
	 *            x-ordinate on screen
	 * @param yc
	 *            y-ordinate on screen
	 * @return a list of ILayers that contain the screen point {x,y} or an empty list if none contain it
	 */
	default List<ILayer> getLayersIntersecting(final int xc, final int yc) {
		return Collections.emptyList();
	}

	/**
	 * Asks this manager to draw all of its enabled layers on the graphics passed in parameter
	 *
	 * @param displayGraphics
	 *            an instance of IGraphics on which to draw the layers
	 */
	default void drawLayersOn(final IGraphics displayGraphics) {}

	/**
	 * Whether the layers in this manager are to be drawn by respecting the world's proportions or not
	 *
	 * @return true if at least one layer needs to be drawn proportionnaly, false otherwise
	 */
	default boolean stayProportional() {
		return false;
	}

	/**
	 * Returns a rectangle that represent the area to focus on in order to focus on the geometry passed in parameter
	 *
	 * @param geometry
	 *            the geometry or agent on which to focus on
	 * @param s
	 *            the surface of this manager
	 * @return a rectangle in screen coordinates
	 */
	default Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		return new Rectangle2D.Double();
	}

	/**
	 * Whether any of the layer managed by this manager can return coordinates for the position of the mouse
	 *
	 * @return true if at least one layer can provide coordinates
	 */
	default boolean isProvidingCoordinates() { return false; }

	/**
	 * Whether any of the layers managed by this manager can return world coordinates for the position of the mouse
	 *
	 * @return true if at least one layer can provide world coordinates
	 */
	default boolean isProvidingWorldCoordinates() { return false; }

	/**
	 * Checks for mouse menu event layer.
	 *
	 * @return true, if successful
	 */
	default boolean hasMouseMenuEventLayer() {
		return false;
	}

	/**
	 * Force redrawing layers.
	 */
	default void forceRedrawingLayers() {}

	/**
	 * Checks for structurally changed.
	 *
	 * @return true, if successful
	 */
	default boolean hasStructurallyChanged() {
		return false;
	}

	/**
	 * Checks for esc event layer.
	 *
	 * @return true, if successful
	 */
	default boolean hasEscEventLayer() {
		return false;
	}

	/**
	 * Checks for arrow event layer.
	 *
	 * @return true, if successful
	 */
	default boolean hasArrowEventLayer() {
		return false;
	}

	/**
	 * Returns a ChartLayer if it is the only "physical" layer present in this manager (event layers do not count) or
	 * null if there are none and/or other layers are displayed and visible
	 *
	 * @return the only chart
	 */
	default ILayer.Chart getOnlyChart() { return null; }

}
