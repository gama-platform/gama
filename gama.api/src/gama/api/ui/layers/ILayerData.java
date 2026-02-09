/*******************************************************************************************************
 *
 * ILayerData.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import java.awt.Point;

import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IGraphics;

/**
 * The class IDisplayLayerBox.
 *
 * @author drogoul
 * @since 14 d�c. 2011
 *
 */
public interface ILayerData {

	/**
	 * Compute.
	 *
	 * @param sim
	 *            the sim
	 * @param g
	 *            the g
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @return whether the layer has changed in some aspects (visibility, size, ...)
	 */
	boolean compute(final IScope sim, IGraphics g) throws GamaRuntimeException;

	/**
	 * Sets the transparency.
	 *
	 * @param f
	 *            the new transparency
	 */
	void setTransparency(final double f);

	/**
	 * Gets the transparency.
	 *
	 * @param scope
	 *            the scope
	 * @return the transparency
	 */
	Double getTransparency(final IScope scope);

	/**
	 * Sets the size.
	 *
	 * @param p
	 *            the new size
	 */
	void setSize(final IPoint p);

	/**
	 * Sets the size.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param depth
	 *            the depth
	 */
	void setSize(final double width, final double height, final double depth);

	/**
	 * Checks if is relative position.
	 *
	 * @return true, if is relative position
	 */
	boolean isRelativePosition();

	/**
	 * Checks if is relative size.
	 *
	 * @return true, if is relative size
	 */
	boolean isRelativeSize();

	/**
	 * Sets the position.
	 *
	 * @param p
	 *            the new position
	 */
	void setPosition(final IPoint p);

	/**
	 * Sets the position.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	void setPosition(final double x, final double y, final double z);

	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	IPoint getPosition();

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	IPoint getSize();

	/**
	 * Gets the refresh.
	 *
	 * @return the refresh
	 */
	Boolean getRefresh();

	/**
	 * Gets the trace.
	 *
	 * @return the trace
	 */
	Integer getTrace();

	/**
	 * Gets the fading.
	 *
	 * @return the fading
	 */
	Boolean getFading();

	/**
	 * Checks if is selectable.
	 *
	 * @return the boolean
	 */
	Boolean isSelectable();

	/**
	 * Sets the selectable.
	 *
	 * @param b
	 *            the new selectable
	 */
	void setSelectable(Boolean b);

	/**
	 * Gets the position in pixels.
	 *
	 * @return the position in pixels
	 */
	Point getPositionInPixels();

	/**
	 * Gets the size in pixels.
	 *
	 * @return the size in pixels
	 */
	Point getSizeInPixels();

	/**
	 * Compute pixels dimensions.
	 *
	 * @param g
	 *            the g
	 */
	void computePixelsDimensions(IGraphics g);

	// /**
	// * Adds the elevation.
	// *
	// * @param currentElevation
	// * the current elevation
	// */
	// void addElevation(double currentElevation);

	/**
	 * Sets the visible region.
	 *
	 * @param e
	 *            the new visible region
	 */
	void setVisibleRegion(IEnvelope e);

	/**
	 * Gets the visible region.
	 *
	 * @return the visible region
	 */
	IEnvelope getVisibleRegion();

	/**
	 * Gets the added elevation.
	 *
	 * @return the added elevation
	 */
	// double getAddedElevation();

	/**
	 * Whether the layer is to be refreshed dynamically everytime the surface displays itself
	 *
	 * @return true if the layer is dynamic, false otherwise
	 */
	default boolean isDynamic() { return getRefresh() == null || getRefresh(); }

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	Boolean isVisible();

	/**
	 * Sets the visible.
	 *
	 * @param b
	 *            the new visible
	 */
	void setVisible(Boolean b);

	/**
	 * The rotation around the z-axis
	 *
	 * @return the rotation of the layer, in degrees, around the z-axis
	 */
	Double getRotation();

	/**
	 * Checks for structurally changed.
	 *
	 * @return true, if successful
	 */
	boolean hasStructurallyChanged();

	/**
	 * @return
	 */
	default boolean drawLines() {
		return false;
	}

	/**
	 * @param b
	 */
	default void setDrawLines(final boolean b) {}

}