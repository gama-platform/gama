/*******************************************************************************************************
 *
 * ChartOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.kernel.simulation.IClock;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IChart;
import gama.api.ui.displays.IChartDataSource;
import gama.api.ui.displays.IDisplaySurface;

/**
 * Abstract base class for GAMA chart outputs, managing lifecycle, dataset updates, and properties.
 */
public abstract class ChartOutput implements IChart {

	public static final int SERIES_CHART = 0;
	public static final int HISTOGRAM_CHART = 1;
	public static final int PIE_CHART = 2;
	public static final int XY_CHART = 3;
	public static final int BOX_WHISKER_CHART = 4;
	public static final int SCATTER_CHART = 5;
	public static final int RADAR_CHART = 6;
	public static final int HEATMAP_CHART = 7;

	public int lastUpdateCycle = -1;
	public boolean ismyfirststep = true;
	protected String chname = "";
	protected int type = SERIES_CHART;
	protected ChartDataSet chartdataset;
	protected final ChartProperties properties = new ChartProperties();

	/**
	 * Instantiates a new chart output.
	 */
	public ChartOutput(final IScope scope, final String name, final IExpression typeexp) {
		final String t = typeexp == null ? IKeyword.SERIES : Cast.asString(scope, typeexp.value(scope));
		type = IKeyword.SERIES.equals(t) ? SERIES_CHART
				: IKeyword.HISTOGRAM.equals(t) ? HISTOGRAM_CHART
				: IKeyword.RADAR.equals(t) ? RADAR_CHART
				: IKeyword.PIE.equals(t) ? PIE_CHART
				: IKeyword.BOX_WHISKER.equals(t) ? BOX_WHISKER_CHART
				: IKeyword.SCATTER.equals(t) ? SCATTER_CHART
				: XY_CHART;
	}

	public ChartProperties getProperties() {
		return properties;
	}

	@Override
	public abstract BufferedImage getImage(final int sizeX, final int sizeY, final boolean antiAlias);

	public int getChartCycle(final IScope scope) {
		if (ismyfirststep) {
			ismyfirststep = false;
			return 0;
		}
		if (scope != null) {
			IClock clock = scope.getClock();
			if (clock != null) return clock.getCycle() + 1;
		}
		return 0;
	}

	public void step(final IScope scope) {
		if (chartdataset != null) {
			chartdataset.updatedataset(scope, getChartCycle(scope));
		}
		updateOutput(scope);
	}

	public void initdataset() {}

	public void updateOutput(final IScope scope) {
		if (chartdataset == null) return;
		if (chartdataset.doResetAll(scope, lastUpdateCycle)) {
			clearDataSet(scope);
			for (final String serieid : chartdataset.getDataSeriesIds(scope)) { createNewSerie(scope, serieid); }
			preResetSeries(scope);
			for (final String serieid : chartdataset.getDataSeriesIds(scope)) { this.resetSerie(scope, serieid); }
		} else {
			final LinkedHashMap<String, Integer> toremove = chartdataset.getSerieRemovalDate();
			for (final String serieid : toremove.keySet()) {
				if (toremove.get(serieid) >= lastUpdateCycle) {
					removeSerie(scope, serieid);
					toremove.put(serieid, toremove.get(serieid) - 1);
				}
			}
			final LinkedHashMap<String, Integer> toadd = chartdataset.getSerieCreationDate();
			for (final String serieid : toadd.keySet()) {
				if (toadd.get(serieid) >= lastUpdateCycle) {
					createNewSerie(scope, serieid);
					toadd.put(serieid, toadd.get(serieid) - 1);
				}
			}
			preResetSeries(scope);
			for (final String serieid : chartdataset.getDataSeriesIds(scope)) { this.resetSerie(scope, serieid); }
		}
		resetAxes(scope);
		IClock clock = scope.getClock();
		if (clock != null) {
			lastUpdateCycle = clock.getCycle();
		}
	}

	public void preResetSeries(final IScope scope) {}
	public void resetAxes(final IScope scope) {}
	public void removeSerie(final IScope scope, final String serieid) {}
	protected void resetSerie(final IScope scope, final String serieid) {}
	protected void clearDataSet(final IScope scope) {}
	protected void createNewSerie(final IScope scope, final String serieid) {}
	public void setUseXSource(final IScope scope, final IExpression expval) {}
	public void setUseXLabels(final IScope scope, final IExpression expval) {}
	public void setUseYLabels(final IScope scope, final IExpression expval) {}

	public void initChart(final IScope scope, final String chartname) {
		chname = chartname;
	}

	public void createChart(final IScope scope) {}

	public ChartDataSet getChartdataset() { return chartdataset; }

	public void setChartdataset(final ChartDataSet chartdataset) {
		this.chartdataset = chartdataset;
		if (chartdataset != null) {
			chartdataset.setOutput(this);
		}
	}

	public String getName() { return chname; }
	public String getStyle() { return properties.getStyle(); }

	// Delegated property configuration

	public void setAxesColorValue(final IScope scope, final IColor color) { properties.setAxesColor(color); }
	public void setTickColorValue(final IScope scope, final IColor color) { properties.setTickColor(color); }
	public void setBackgroundColorValue(final IScope scope, final IColor color) { properties.setBackgroundColor(color); }
	public void setLabelTextColorValue(final IScope scope, final IColor color) { properties.setLabelTextColor(color); }
	public void setLabelBackgroundColorValue(final IScope scope, final IColor color) { properties.setLabelBackgroundColor(color); }
	public void setColorValue(final IScope scope, final IColor color) { properties.setTextColor(color); }

	public void setTickFontFace(final IScope scope, final String value) { properties.setTickFontFace(value); }
	public void setLabelFontFace(final IScope scope, final String value) { properties.setLabelFontFace(value); }
	public void setLegendFontFace(final IScope scope, final String value) { properties.setLegendFontFace(value); }
	public void setTitleFontFace(final IScope scope, final String value) { properties.setTitleFontFace(value); }

	public void setTickFontSize(final IScope scope, final int value) { properties.setTickFontSize(value); }
	public void setLabelFontSize(final IScope scope, final int value) { properties.setLabelFontSize(value); }
	public void setLegendFontSize(final IScope scope, final int value) { properties.setLegendFontSize(value); }
	public void setTitleFontSize(final IScope scope, final int value) { properties.setTitleFontSize(value); }

	public void setTickFontStyle(final IScope scope, final int value) { properties.setTickFontStyle(value); }
	public void setLabelFontStyle(final IScope scope, final int value) { properties.setLabelFontStyle(value); }
	public void setLegendFontStyle(final IScope scope, final int value) { properties.setLegendFontStyle(value); }
	public void setTitleFontStyle(final IScope scope, final int value) { properties.setTitleFontStyle(value); }

	public void setXLabel(final IScope scope, final String asString) { properties.setXLabel(asString); }
	public String getXLabel(final IScope scope) { return properties.getXLabel(); }

	public void setYLabel(final IScope scope, final String asString) { properties.setYLabel(asString); }
	public String getYLabel(final IScope scope) { return properties.getYLabel(); }

	public void setY2Label(final IScope scope, final String asString) { properties.setY2Label(asString); }
	public String getY2Label(final IScope scope) { return properties.getY2Label(); }

	public boolean getUseXRangeInterval(final IScope scope) { return properties.isUseXRangeInterval(); }
	public boolean getUseXRangeMinMax(final IScope scope) { return properties.isUseXRangeMinMax(); }
	public boolean getUseYRangeInterval(final IScope scope) { return properties.isUseYRangeInterval(); }
	public boolean getUseYRangeMinMax(final IScope scope) { return properties.isUseYRangeMinMax(); }
	public boolean getUseY2RangeInterval(final IScope scope) { return properties.isUseY2RangeInterval(); }
	public boolean getUseY2RangeMinMax(final IScope scope) { return properties.isUseY2RangeMinMax(); }

	public void setXRangeInterval(final IScope scope, final double doubleValue) { properties.setXRangeInterval(doubleValue); }
	public double getXRangeInterval(final IScope scope) { return properties.getXRangeInterval(); }

	public void setXRangeMinMax(final IScope scope, final double minValue, final double maxValue) { properties.setXRangeMinMax(minValue, maxValue); }
	public double getXRangeMin(final IScope scope) { return properties.getXRangeMin(); }
	public double getXRangeMax(final IScope scope) { return properties.getXRangeMax(); }

	public boolean getUseXMin(final IScope scope) { return properties.isUseXMin(); }
	public boolean getUseXMax(final IScope scope) { return properties.isUseXMax(); }
	public void setXMin(final IScope scope, final double value) { properties.setXMin(value); }
	public void setXMax(final IScope scope, final double value) { properties.setXMax(value); }
	public double getXMin(final IScope scope) { return properties.getXMinVal(); }
	public double getXMax(final IScope scope) { return properties.getXMaxVal(); }

	public double getYRangeInterval(final IScope scope) { return properties.getYRangeInterval(); }
	public void setYRangeInterval(final IScope scope, final double doubleValue) { properties.setYRangeInterval(doubleValue); }
	public void setYRangeMinMax(final IScope scope, final double minValue, final double maxValue) { properties.setYRangeMinMax(minValue, maxValue); }
	public double getYRangeMin(final IScope scope) { return properties.getYRangeMin(); }
	public double getYRangeMax(final IScope scope) { return properties.getYRangeMax(); }

	public boolean getUseYMin(final IScope scope) { return properties.isUseYMin(); }
	public boolean getUseYMax(final IScope scope) { return properties.isUseYMax(); }
	public void setYMin(final IScope scope, final double value) { properties.setYMin(value); }
	public void setYMax(final IScope scope, final double value) { properties.setYMax(value); }
	public double getYMin(final IScope scope) { return properties.getYMinVal(); }
	public double getYMax(final IScope scope) { return properties.getYMaxVal(); }

	public double getY2RangeInterval(final IScope scope) { return properties.getY2RangeInterval(); }
	public void setY2RangeInterval(final IScope scope, final double doubleValue) { properties.setY2RangeInterval(doubleValue); }
	public void setY2RangeMinMax(final IScope scope, final double minValue, final double maxValue) { properties.setY2RangeMinMax(minValue, maxValue); }
	public double getY2RangeMin(final IScope scope) { return properties.getY2RangeMin(); }
	public double getY2RangeMax(final IScope scope) { return properties.getY2RangeMax(); }

	public void setXTickUnit(final IScope scope, final double r) { properties.setXTickUnit(r); }
	public double getXTickUnit(final IScope scope) { return properties.getXTickUnit(); }

	public void setYTickUnit(final IScope scope, final double r) { properties.setYTickUnit(r); }
	public double getYTickUnit(final IScope scope) { return properties.getYTickUnit(); }

	public void setY2TickUnit(final IScope scope, final double r) { properties.setY2TickUnit(r); }
	public double getY2TickUnit(final IScope scope) { return properties.getY2TickUnit(); }

	public void setGap(final IScope scope, final double range) { properties.setGap(range); }

	public void setSerieMarkerShape(final IScope scope, final String serieid, final String markershape) {}
	public void setDefaultPropertiesFromType(final IScope scope, final IChartDataSource source, final int type_val) {}
	public void setUseSize(final IScope scope, final String name, final boolean b) {}

	public void setSeriesLabelPosition(final IScope scope, final String asString) { properties.setSeriesLabelPosition(asString); }
	public void setStyle(final IScope scope, final String asString) { properties.setStyle(asString); }
	public void initChart_post_data_init(final IScope scope) {}

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {}

	public void setReverseAxis(final IScope scope, final Boolean asBool) { properties.setReverseAxes(asBool); }
	public void setX_LogScale(final IScope scope, final Boolean asBool) { properties.setXLogscale(asBool); }
	public void setY_LogScale(final IScope scope, final Boolean asBool) { properties.setYLogscale(asBool); }
	public boolean getX_LogScale(final IScope scope) { return properties.isXLogscale(); }
	public boolean getY_LogScale(final IScope scope) { return properties.isYLogscale(); }

	public void setY2_LogScale(final IScope scope, final Boolean asBool) { properties.setY2Logscale(asBool); }
	public boolean getY2_LogScale(final IScope scope) { return properties.isY2Logscale(); }

	public void setUseSecondYAxis(final IScope scope, final Boolean asBool) { properties.setUseSecondYAxis(asBool); }
	public boolean getUseSecondYAxis(final IScope scope) { return properties.isUseSecondYAxis(); }

	public void setXTickValueVisible(final IScope scope, final Boolean asBool) { properties.setXTickValueVisible(asBool); }
	public boolean getXTickValueVisible(final IScope scope) { return properties.isXTickValueVisible(); }

	public void setYTickValueVisible(final IScope scope, final Boolean asBool) { properties.setYTickValueVisible(asBool); }
	public boolean getYTickValueVisible(final IScope scope) { return properties.isYTickValueVisible(); }

	public void setY2TickValueVisible(final IScope scope, final Boolean asBool) { properties.setY2TickValueVisible(asBool); }
	public boolean getY2TickValueVisible(final IScope scope) { return properties.isY2TickValueVisible(); }

	public void setTitleVisible(final IScope scope, final Boolean asBool) { properties.setTitleVisible(asBool); }
	public boolean getTitleVisible(final IScope scope) { return properties.isTitleVisible(); }

	public void setXTickLineVisible(final IScope scope, final Boolean asBool) { properties.setXTickLineVisible(asBool); }
	public boolean getXTickLineVisible(final IScope scope) { return properties.isXTickLineVisible(); }

	public void setYTickLineVisible(final IScope scope, final Boolean asBool) { properties.setYTickLineVisible(asBool); }
	public boolean getYTickLineVisible(final IScope scope) { return properties.isYTickLineVisible(); }

	public void setGridLinesVisible(final IScope scope, final Boolean visible) { properties.setGridLinesVisible(visible); }
	public boolean getGridLinesVisible() { return properties.isGridLinesVisible(); }

	public void dispose(final IScope scope) {}

	public void setSeriesLabelAnchor(final IScope scope, final IPoint pt) { properties.setSeriesLabelAnchor(pt); }
	public void setLegendOrientation(final IScope scope, final String orient) { properties.setLegendOrientation(orient); }

}
