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

import gama.api.types.geometry.IPoint;

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

	/** The computed. */
	boolean computed;

	/** The last display width used to compute overlay dimensions. */
	private int lastDisplayWidth = -1;

	/** The last display height used to compute overlay dimensions. */
	private int lastDisplayHeight = -1;

	/** The last horizontal pixel-to-model ratio used to compute overlay dimensions. */
	private double lastXRatio = Double.NaN;

	/** The last vertical pixel-to-model ratio used to compute overlay dimensions. */
	private double lastYRatio = Double.NaN;

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
		final int displayWidth = g.getViewWidth();
		final int displayHeight = g.getViewHeight();
		final double xRatio = displayWidth / g.getEnvWidth();
		final double yRatio = displayHeight / g.getEnvHeight();
		if (computed && lastDisplayWidth == displayWidth && lastDisplayHeight == displayHeight
				&& Double.compare(lastXRatio, xRatio) == 0 && Double.compare(lastYRatio, yRatio) == 0) {
			return;
		}
		final IPoint position = getPosition();
		final double x = position.getX();
		final double relativeX = !isRelativePosition() ? xRatio * x : Math.abs(x) <= 1 ? displayWidth * x : xRatio * x;
		final double absoluteX = Math.signum(x) < 0 ? displayWidth + relativeX : relativeX;
		final double y = position.getY();
		final double relativeY = !isRelativePosition() ? yRatio * y : Math.abs(y) <= 1 ? displayHeight * y : yRatio * y;
		final double absoluteY = Math.signum(y) < 0 ? displayHeight + relativeY : relativeY;
		final IPoint size = getSize();
		final double w = size.getX();
		final double absoluteWidth = !isRelativeSize() ? xRatio * w : Math.abs(w) <= 1 ? displayWidth * w : xRatio * w;
		final double h = size.getY();
		final double absoluteHeight = !isRelativeSize() ? yRatio * h : Math.abs(h) <= 1 ? displayHeight * h : yRatio * h;
		getSizeInPixels().setLocation(absoluteWidth, absoluteHeight);
		getPositionInPixels().setLocation(absoluteX, absoluteY);
		computed = true;
		lastDisplayWidth = displayWidth;
		lastDisplayHeight = displayHeight;
		lastXRatio = xRatio;
		lastYRatio = yRatio;
	}

	/**
	 * Checks if is rounded.
	 *
	 * @return true, if is rounded
	 */
	public boolean isRounded() { return rounded.get(); }

}
