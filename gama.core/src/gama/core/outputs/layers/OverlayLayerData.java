/*******************************************************************************************************
 *
 * OverlayLayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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
import gama.api.ui.layers.LayerCoordinates;

/**
 * Overlay frames share the standard layer rectangle. World contents fit uniformly into a screen frame.
 */
public class OverlayLayerData extends FramedLayerData {

	final Attribute<Boolean> rounded;

	private double contentRatio = 1d;

	public OverlayLayerData(final ILayerStatement def) throws GamaRuntimeException {
		super(def);
		rounded = create(IKeyword.ROUNDED, Types.BOOL, true);
	}

	@Override
	public void computePixelsDimensions(final IGraphics g) {
		super.computePixelsDimensions(g);
		contentRatio = getContentCoordinates() == LayerCoordinates.SCREEN ? 1d
				: Math.min(getSizeInPixels().getX() / g.getEnvWidth(),
						getSizeInPixels().getY() / g.getEnvHeight());
		// A zero-size frame can still intentionally draw overflowing world contents.
		if (contentRatio == 0) { contentRatio = 1d; }
	}

	/** The uniform pixels-per-model-unit ratio for world contents in a screen overlay. */
	public double getContentRatio() { return contentRatio; }

	public boolean isRounded() { return rounded.get(); }
}
