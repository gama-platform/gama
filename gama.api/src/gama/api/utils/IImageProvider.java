/*******************************************************************************************************
 *
 * IImageProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils;

import java.awt.image.BufferedImage;

import gama.api.data.objects.IEnvelope;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IAsset;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelopeProvider;

/**
 * The Interface IImageProvider. An abstraction to represent objects that can provide an image (a BufferedImage) and a
 * name associated to it (hence the inheritance from INamed).
 */
public interface IImageProvider extends IEnvelopeProvider, IAsset {

	/**
	 * Returns the number of rows (height) of the receiver
	 *
	 * @param scope
	 * @return
	 */
	int getRows(IScope scope);

	/**
	 * Returns the number of columns (width) of the receiver
	 *
	 * @param scope
	 * @return
	 */
	int getCols(IScope scope);

	/**
	 * Compute envelope.
	 *
	 * @param scope
	 *            the scope
	 * @return the envelope 3 D
	 */
	@Override
	default IEnvelope computeEnvelope(final IScope scope) {
		return GamaEnvelopeFactory.of(0, getCols(scope), 0, getRows(scope), 0, 0);
	}

	/**
	 * Returns the image provided by this provider, specifying whether to compute or load it again or to use a cache. It
	 * is assumed (by default) that the image produced is not for OpenGL
	 *
	 * @param scope
	 *            the scope
	 * @return the image
	 */
	BufferedImage getImage(final IScope scope, final boolean useCache);

	/**
	 * Gets the image. Default is to use whatever cache is provided
	 *
	 * @param scope
	 *            the scope
	 * @return the image
	 */
	default BufferedImage getImage(final IScope scope) {
		return getImage(scope, true);
	}

	/**
	 * Checks if is animated.
	 *
	 * @return true, if is animated
	 */
	default boolean isAnimated() { return false; }

}
