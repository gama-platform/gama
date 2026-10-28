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
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.layers.ILayerStatement;

/**
 * Data of an overlay layer. This class is the single place where the screen geometry of an overlay is computed, so
 * that all the renderers (Java2D, OpenGL, image exports) place and size overlays identically. The rules, applied
 * independently to each coordinate of the `position` and `size` facets, are:
 * <ul>
 * <li>a coordinate expressed with `#px` / `#pixels` is a number of (logical) pixels of the view;</li>
 * <li>otherwise, a coordinate in [-1,1] is a fraction of the view;</li>
 * <li>otherwise, it is expressed in model units, considering that the model occupies the entire view;</li>
 * <li>a negative position is counted from the right (resp. bottom) border of the view.</li>
 * </ul>
 * The contents of the overlay are drawn with a uniform ratio between pixels and model units ({@link #getContentRatio()})
 * that only depends on the view, so that `#px` always represents one pixel inside overlays and that the contents do not
 * depend on the size of the overlay itself.
 */
public class OverlayLayerData extends FramedLayerData {

	/** The rounded. */
	final Attribute<Boolean> rounded;

	/** Whether each coordinate of the position and size facets is expressed in pixels. */
	private boolean xPositionInPixels, yPositionInPixels, xSizeInPixels, ySizeInPixels;

	/** The ratio (pixels per model unit) used by `#px` when the facets have been evaluated. */
	private double facetPixelRatio = 1d;

	/** The ratio (pixels per model unit) used to draw the contents of the overlay. */
	private double contentRatio = 1d;

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
		boolean[] px = pixelComponents(def.getFacet(IKeyword.POSITION));
		xPositionInPixels = px[0];
		yPositionInPixels = px[1];
		px = pixelComponents(def.getFacet(IKeyword.SIZE));
		xSizeInPixels = px[0];
		ySizeInPixels = px[1];
	}

	/**
	 * Determines, for each of the x and y coordinates of a point expression, whether it is expressed in pixels. When
	 * the expression is a point literal (e.g. `{2000, 32#px}`), each coordinate is examined separately.
	 *
	 * @param exp
	 *            the expression (can be null)
	 * @return an array of two booleans (x, y)
	 */
	private static boolean[] pixelComponents(final IExpression exp) {
		if (exp == null) return new boolean[] { false, false };
		if (exp instanceof IOperator op && IKeyword.POINT.equals(op.getName())) {
			final IExpression x = op.arg(0);
			final IExpression y = op.arg(1);
			if (x != null && y != null) return new boolean[] { x.containsPixels(), y.containsPixels() };
		}
		final boolean px = exp.containsPixels();
		return new boolean[] { px, px };
	}

	@Override
	public void setSize(final double width, final double height, final double depth) {
		super.setSize(width, height, depth);
		xSizeInPixels = ySizeInPixels = false;
	}

	@Override
	public void setPosition(final double x, final double y, final double z) {
		super.setPosition(x, y, z);
		xPositionInPixels = yPositionInPixels = false;
	}

	@Override
	public boolean compute(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		// The facets are evaluated outside of the overlay, where `#px` is the inverse of this ratio. Remembering it
		// allows to convert the values expressed with `#px` back into exact pixels.
		facetPixelRatio = validRatio(g.getAbsoluteRatioBetweenPixelsAndModelsUnits());
		return super.compute(scope, g);
	}

	@Override
	public void computePixelsDimensions(final IGraphics g) {
		final double viewWidth = g.getViewWidth();
		final double viewHeight = g.getViewHeight();
		if (viewWidth <= 0 || viewHeight <= 0) return;
		final double xRatio = viewWidth / g.getEnvWidth();
		final double yRatio = viewHeight / g.getEnvHeight();
		contentRatio = validRatio(Math.min(xRatio, yRatio));
		final IPoint position = getPosition();
		double x = toPixels(position.getX(), xPositionInPixels, viewWidth, xRatio);
		if (position.getX() < 0) { x += viewWidth; }
		double y = toPixels(position.getY(), yPositionInPixels, viewHeight, yRatio);
		if (position.getY() < 0) { y += viewHeight; }
		final IPoint size = getSize();
		final double w = Math.max(0, toPixels(size.getX(), xSizeInPixels, viewWidth, xRatio));
		final double h = Math.max(0, toPixels(size.getY(), ySizeInPixels, viewHeight, yRatio));
		getSizeInPixels().setLocation(w, h);
		getPositionInPixels().setLocation(x, y);
	}

	/**
	 * Converts a coordinate of the position or size facets into pixels.
	 *
	 * @param value
	 *            the value of the coordinate
	 * @param inPixels
	 *            whether the coordinate has been expressed with `#px`
	 * @param viewDimension
	 *            the dimension of the view in pixels along this axis
	 * @param ratio
	 *            the ratio between pixels and model units along this axis
	 * @return the value in pixels
	 */
	private double toPixels(final double value, final boolean inPixels, final double viewDimension,
			final double ratio) {
		if (inPixels) return value * facetPixelRatio;
		if (Math.abs(value) <= 1) return value * viewDimension;
		return value * ratio;
	}

	/**
	 * Returns the ratio if it is strictly positive and finite, 1 otherwise.
	 */
	private static double validRatio(final double ratio) {
		return ratio > 0 && Double.isFinite(ratio) ? ratio : 1d;
	}

	/**
	 * Gets the ratio (number of pixels per model unit) used to draw the contents of the overlay. It only depends on
	 * the size of the view and of the environment, so that `#px` represents one pixel inside the overlay.
	 *
	 * @return the content ratio
	 */
	public double getContentRatio() { return contentRatio; }

	/**
	 * Checks if is rounded.
	 *
	 * @return true, if is rounded
	 */
	public boolean isRounded() { return rounded.get(); }

}
