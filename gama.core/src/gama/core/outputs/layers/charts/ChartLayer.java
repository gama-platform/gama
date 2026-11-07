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
import gama.core.outputs.layers.AbstractLayer;

/**
 * Written by drogoul Modified on 1 avr. 2010
 *
 * @todo Description
 *
 */
public class ChartLayer extends AbstractLayer implements ILayer.Chart {

	private static final int MAX_RASTER_DIMENSION = 4096;
	private static final int MAX_RASTER_PIXELS = 8 * 1024 * 1024;

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
		final int x = (int) Math.abs((double) dg.getLayerWidth());
		final int y = (int) Math.abs((double) dg.getLayerHeight());
		if (x == 0 || y == 0) return;
		double scale = dg.getDevicePixelRatio() * ((ChartLayerStatement) definition).getQuality();
		// Bound both raster size and the memory used by the two buffers without changing layout.
		scale = Math.min(scale, (double) MAX_RASTER_DIMENSION / Math.max(x, y));
		scale = Math.min(scale, Math.sqrt(MAX_RASTER_PIXELS / ((double) x * y)));
		final int rasterWidth = Math.max(1, (int) Math.floor(x * scale));
		final int rasterHeight = Math.max(1, (int) Math.floor(y * scale));
		final BufferedImage im = chart.getImage(x, y, rasterWidth, rasterHeight, true);
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
