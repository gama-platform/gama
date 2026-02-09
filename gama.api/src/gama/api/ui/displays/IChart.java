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
	 * @param x
	 * @param y
	 * @param antialias
	 * @return
	 */
	BufferedImage getImage(int x, int y, boolean antialias);

	/**
	 * @return
	 */
	JFreeChart getJFChart();

	/**
	 * @param xOnScreen
	 * @param yOnScreen
	 * @param g
	 * @param positionInPixels
	 * @param sb
	 */
	void getModelCoordinatesInfo(int xOnScreen, int yOnScreen, IDisplaySurface g, Point positionInPixels,
			StringBuilder sb);

	/**
	 * @param scope
	 * @param chartDataSourceUnique
	 * @param type_val
	 */
	void setDefaultPropertiesFromType(IScope scope, IChartDataSource chartDataSourceUnique, int type_val);

}