/*******************************************************************************************************
 *
 * IChart.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;

import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IChart {

	/**
	 * Renders and returns the chart as a BufferedImage.
	 *
	 * @param x width in pixels
	 * @param y height in pixels
	 * @param antialias whether anti-aliasing should be enabled
	 * @return rendered BufferedImage
	 */
	BufferedImage getImage(int x, int y, boolean antialias);

	/**
	 * Renders a chart laid out in logical pixels into an independently sized raster.
	 * Fonts and strokes retain their logical size when the raster is displayed in that frame.
	 *
	 * @param width logical layout width
	 * @param height logical layout height
	 * @param rasterWidth physical raster width
	 * @param rasterHeight physical raster height
	 * @param antialias whether anti-aliasing should be enabled
	 * @return rendered BufferedImage
	 */
	BufferedImage getImage(int width, int height, int rasterWidth, int rasterHeight, boolean antialias);

	/**
	 * Draws the chart into the supplied logical display area.
	 *
	 * @param graphics drawing target
	 * @param area logical display area
	 * @param antialias whether anti-aliasing should be enabled
	 */
	default void draw(final Graphics2D graphics, final Rectangle2D area, final boolean antialias) {
		final int width = Math.max(1, (int) Math.round(area.getWidth()));
		final int height = Math.max(1, (int) Math.round(area.getHeight()));
		final BufferedImage image = getImage(width, height, antialias);
		if (image != null) {
			graphics.drawImage(image, (int) Math.round(area.getX()), (int) Math.round(area.getY()), width, height,
					null);
		}
	}

	/**
	 * Returns the underlying native chart object (e.g. JFreeChart instance or other chart engine object).
	 *
	 * @return native chart object or null
	 */
	default Object getNativeChart() { return null; }

	/**
	 * Returns the JFreeChart instance if the chart engine is JFreeChart.
	 *
	 * @return JFreeChart instance or null
	 */
	default JFreeChart getJFChart() {
		return getNativeChart() instanceof JFreeChart jfc ? jfc : null;
	}

	/**
	 * Obtains model coordinates info for screen coordinates and user interaction.
	 */
	void getModelCoordinatesInfo(int xOnScreen, int yOnScreen, IDisplaySurface g, Point positionInPixels,
			StringBuilder sb);

	/**
	 * Sets default chart properties based on the data source type.
	 */
	void setDefaultPropertiesFromType(IScope scope, IChartDataSource chartDataSourceUnique, int type_val);

}