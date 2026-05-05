/*******************************************************************************************************
 *
 * OverlayLayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.ILayerStatement;

/**
 * The Class OverlayLayerData.
 */
public class OverlayLayerData extends FramedLayerData {

	/** The rounded. */
	final Attribute<Boolean> rounded;

	/**
	 * Instantiates a new overlay layer data.
	 *
	 * @param def
	 *            the def
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public OverlayLayerData(final ILayerStatement def) throws GamaRuntimeException {
		super(def);
		rounded = create(IKeyword.ROUNDED, Types.BOOL, true);
	}

	@Override
	public void computePixelsDimensions(final IGraphics g) {
		super.computePixelsDimensions(g);
		// Translate the computed position so that {0,0} aligns with the top-left corner of
		// the simulation viewport rather than the top-left corner of the enclosing panel.
		// For OpenGL displays the viewport equals the full canvas, so these offsets are 0.
		// Fixes #589 and #354 (2D/3D consistency): size:{1,1} now covers the rendered area.
		final int vpOriginX = Math.max(0, (g.getViewWidth() - g.getDisplayWidth()) / 2);
		final int vpOriginY = Math.max(0, (g.getViewHeight() - g.getDisplayHeight()) / 2);
		getPositionInPixels().translate(vpOriginX, vpOriginY);
	}

	/**
	 * Checks if is rounded.
	 *
	 * @return true, if is rounded
	 */
	public boolean isRounded() { return rounded.get(); }

}
