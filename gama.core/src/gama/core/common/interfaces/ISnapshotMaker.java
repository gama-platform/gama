/*******************************************************************************************************
 *
 * ISnapshotMaker.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.awt.image.BufferedImage;

import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;

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
	default void takeAndSaveSnapshot(final IDisplaySurface surface, GamaPoint desiredDimensions) {}

	/**
	 * Capture image.
	 *
	 * @param surface
	 *            the surface
	 * @return the buffered image
	 */
	default BufferedImage captureImage(final IDisplaySurface surface, GamaPoint desiredDimensions) {
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