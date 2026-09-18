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

import java.awt.Point;
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