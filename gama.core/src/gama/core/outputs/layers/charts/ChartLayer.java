/*******************************************************************************************************
 *
 * ChartLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.geom.Rectangle2D;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.metamodel.shape.IShape;
import gama.core.outputs.layers.AbstractLayer;
import gama.core.outputs.layers.ILayerStatement;
import gama.core.runtime.IScope.IGraphicsScope;

/**
 * Written by drogoul Modified on 1 avr. 2010
 *
 * @todo Description
 *
 */
public class ChartLayer extends AbstractLayer {

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
	public ChartOutput getChart() { return ((ChartLayerStatement) definition).getOutput(); }

	@Override
	public String getType() { return "Chart layer"; }

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {
		dg.drawChart(getChart());
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
