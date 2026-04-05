/*******************************************************************************************************
 *
 * ChartLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gama.api.types.geometry.IShape;
import gama.api.ui.displays.IChart;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.ILayer;
import gama.api.ui.layers.ILayerStatement;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.outputs.layers.AbstractLayer;

/**
 * Written by drogoul Modified on 1 avr. 2010
 *
 * @todo Description
 *
 */
public class ChartLayer extends AbstractLayer implements ILayer.Chart {

	/**
	 * Instantiates a new chart layer.
	 *
	 * @param model
	 *            the model
	 */
	public ChartLayer(final ILayerStatement model) {
		super(model);
	}

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		return null;
	}

	/**
	 * Gets the chart.
	 *
	 * @return the chart
	 */
	@Override
	public IChart getChart() { return ((ChartLayerStatement) definition).getOutput(); }

	@Override
	public String getType() { return "Chart layer"; }

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {
		IChart chart = getChart();
		int x = dg.getLayerWidth();
		int y = dg.getLayerHeight();
		if (!dg.is2D()) { y = x = (int) (Math.min(x, y) * GamaPreferences.Displays.CHART_QUALITY.getValue()); }
		final BufferedImage im = chart.getImage(x, y, dg.getSurface().getData().isAntialias());
		dg.drawChart(im);
	}

	@Override
	public boolean stayProportional() {
		return false;
	}

	@Override
	public boolean isProvidingWorldCoordinates() { return false; }

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final StringBuilder sb) {
		getChart().getModelCoordinatesInfo(xOnScreen, yOnScreen, g, getData().getPositionInPixels(), sb);
	}

}
