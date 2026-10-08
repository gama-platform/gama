/*******************************************************************************************************
 *
 * ISnapshotMaker.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import java.awt.image.BufferedImage;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IDisplaySurface;

/**
 * The Interface ISnapshotMaker.
 */
public interface ISnapshotMaker {

	/**
	 * Do snapshot.
	 *
	 * @param surface
	 *            the surface
	 * @param composite
	 *            the composite
	 */
	default void takeAndSaveSnapshot(final IDisplaySurface surface, final IPoint desiredDimensions) {}

	/**
	 * Capture image.
	 *
	 * @param surface
	 *            the surface
	 * @return the buffered image
	 */
	default BufferedImage captureImage(final IDisplaySurface surface, final IPoint desiredDimensions) {
		return null;
	}

	/**
	 * Do snapshot.
	 *
	 * @param scope
	 *            the scope
	 * @param autosavePath
	 *            the autosave path
	 */
	default void takeAndSaveScreenshot(final IScope scope, final String autosavePath) {}

}