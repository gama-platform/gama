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
import gama.api.ui.IOutput;
import gama.api.ui.displays.IChart;
import gama.api.ui.displays.IChartDataSource;
import gama.api.ui.displays.IDisplaySurface;

/**
 * Abstract base class for GAMA chart outputs, managing lifecycle, dataset updates, and properties.
 */
public abstract class ChartOutput implements IChart {

	/** The Constant SERIES_CHART. */
	public static final int SERIES_CHART = 0;

	/** The Constant HISTOGRAM_CHART. */
	public static final int HISTOGRAM_CHART = 1;

	/** The Constant PIE_CHART. */
	public static final int PIE_CHART = 2;

	/** The Constant XY_CHART. */
	public static final int XY_CHART = 3;

	/** The Constant BOX_WHISKER_CHART. */
	public static final int BOX_WHISKER_CHART = 4;

	/** The Constant SCATTER_CHART. */
	public static final int SCATTER_CHART = 5;

	/** The Constant RADAR_CHART. */
	public static final int RADAR_CHART = 6;

	/** The Constant HEATMAP_CHART. */
	public static final int HEATMAP_CHART = 7;

	/** The last update cycle. */
	public int lastUpdateCycle = -1;

	/** The ismyfirststep. */
	public boolean ismyfirststep = true;

	/** The chname. */
	protected String chname = "";

	/** The type. */
	protected int type = SERIES_CHART;

	/** The chartdataset. */
	protected ChartDataSet chartdataset;

	/** The host display output. */
	protected IOutput hostDisplayOutput;

	/** The properties. */
	protected final ChartProperties properties = new ChartProperties();

	/**
	 * Gets the host display output.
	 *
	 * @return the host display output
	 */
	public IOutput getHostDisplayOutput() { return hostDisplayOutput; }

	/**
	 * Sets the host display output.
	 *
	 * @param output
	 *            the new host display output
	 */
	public void setHostDisplayOutput(final IOutput.Display output) {
		this.hostDisplayOutput = output;
		properties.setHostDisplayOutput(output);
	}

	/**
	 * Instantiates a new chart output.
	 */
	public ChartOutput(final IScope scope, final String name, final IExpression typeexp) {
		final String t = typeexp == null ? IKeyword.SERIES : Cast.asString(scope, typeexp.value(scope));
		type = IKeyword.SERIES.equals(t) ? SERIES_CHART : IKeyword.HISTOGRAM.equals(t) ? HISTOGRAM_CHART
				: IKeyword.RADAR.equals(t) ? RADAR_CHART : IKeyword.PIE.equals(t) ? PIE_CHART
				: IKeyword.BOX_WHISKER.equals(t) ? BOX_WHISKER_CHART : IKeyword.SCATTER.equals(t) ? SCATTER_CHART
				: XY_CHART;
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public ChartProperties getProperties() { return properties; }

	@Override
	public abstract BufferedImage getImage(final int sizeX, final int sizeY, final boolean antiAlias);

	/**
	 * Gets the chart cycle.
	 *
	 * @param scope
	 *            the scope
	 * @return the chart cycle
	 */
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

	/**
	 * Step.
	 *
	 * @param scope
	 *            the scope
	 */
	public void step(final IScope scope) {
		if (chartdataset != null) { chartdataset.updatedataset(scope, getChartCycle(scope)); }
		updateOutput(scope);
	}

	/**
	 * Initdataset.
	 */
	public void initdataset() {}

	/**
	 * Update output.
	 *
	 * @param scope
	 *            the scope
	 */
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
		if (clock != null) { lastUpdateCycle = clock.getCycle(); }
	}

	/**
	 * Pre reset series.
	 *
	 * @param scope
	 *            the scope
	 */
	public void preResetSeries(final IScope scope) {}

	/**
	 * Reset axes.
	 *
	 * @param scope
	 *            the scope
	 */
	public void resetAxes(final IScope scope) {}

	/**
	 * Removes the serie.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 */
	public void removeSerie(final IScope scope, final String serieid) {}

	/**
	 * Reset serie.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 */
	protected void resetSerie(final IScope scope, final String serieid) {}

	/**
	 * Clear data set.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void clearDataSet(final IScope scope) {}

	/**
	 * Creates the new serie.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 */
	protected void createNewSerie(final IScope scope, final String serieid) {}

	/**
	 * Sets the use X source.
	 *
	 * @param scope
	 *            the scope
	 * @param expval
	 *            the expval
	 */
	public void setUseXSource(final IScope scope, final IExpression expval) {}

	/**
	 * Sets the use X labels.
	 *
	 * @param scope
	 *            the scope
	 * @param expval
	 *            the expval
	 */
	public void setUseXLabels(final IScope scope, final IExpression expval) {}

	/**
	 * Sets the use Y labels.
	 *
	 * @param scope
	 *            the scope
	 * @param expval
	 *            the expval
	 */
	public void setUseYLabels(final IScope scope, final IExpression expval) {}

	/**
	 * Inits the chart.
	 *
	 * @param scope
	 *            the scope
	 * @param chartname
	 *            the chartname
	 */
	public void initChart(final IScope scope, final String chartname) {
		chname = chartname;
	}

	/**
	 * Creates the chart.
	 *
	 * @param scope
	 *            the scope
	 */
	public void createChart(final IScope scope) {}

	/**
	 * Gets the chartdataset.
	 *
	 * @return the chartdataset
	 */
	public ChartDataSet getChartdataset() { return chartdataset; }

	/**
	 * Sets the chartdataset.
	 *
	 * @param chartdataset
	 *            the new chartdataset
	 */
	public void setChartdataset(final ChartDataSet chartdataset) {
		this.chartdataset = chartdataset;
		if (chartdataset != null) { chartdataset.setOutput(this); }
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() { return chname; }

	/**
	 * Gets the style.
	 *
	 * @return the style
	 */
	public String getStyle() { return properties.getStyle(); }

	// Delegated property configuration

	/**
	 * Sets the axes color value.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 */
	public void setAxesColorValue(final IScope scope, final IColor color) {
		properties.setAxesColor(color);
	}

	/**
	 * Sets the tick color value.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 */
	public void setTickColorValue(final IScope scope, final IColor color) {
		properties.setTickColor(color);
	}

	/**
	 * Sets the background color value.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 */
	public void setBackgroundColorValue(final IScope scope, final IColor color) {
		properties.setBackgroundColor(color);
	}

	/**
	 * Sets the label text color value.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 */
	public void setLabelTextColorValue(final IScope scope, final IColor color) {
		properties.setLabelTextColor(color);
	}

	/**
	 * Sets the label background color value.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 */
	public void setLabelBackgroundColorValue(final IScope scope, final IColor color) {
		properties.setLabelBackgroundColor(color);
	}

	/**
	 * Sets the color value.
	 *
	 * @param scope
	 *            the scope
	 * @param color
	 *            the color
	 */
	public void setColorValue(final IScope scope, final IColor color) {
		properties.setTextColor(color);
	}

	/**
	 * Sets the tick font face.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setTickFontFace(final IScope scope, final String value) {
		properties.setTickFontFace(value);
	}

	/**
	 * Sets the label font face.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setLabelFontFace(final IScope scope, final String value) {
		properties.setLabelFontFace(value);
	}

	/**
	 * Sets the legend font face.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setLegendFontFace(final IScope scope, final String value) {
		properties.setLegendFontFace(value);
	}

	/**
	 * Sets the title font face.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setTitleFontFace(final IScope scope, final String value) {
		properties.setTitleFontFace(value);
	}

	/**
	 * Sets the tick font size.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setTickFontSize(final IScope scope, final int value) {
		properties.setTickFontSize(value);
	}

	/**
	 * Sets the label font size.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setLabelFontSize(final IScope scope, final int value) {
		properties.setLabelFontSize(value);
	}

	/**
	 * Sets the legend font size.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setLegendFontSize(final IScope scope, final int value) {
		properties.setLegendFontSize(value);
	}

	/**
	 * Sets the title font size.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setTitleFontSize(final IScope scope, final int value) {
		properties.setTitleFontSize(value);
	}

	/**
	 * Sets the tick font style.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setTickFontStyle(final IScope scope, final int value) {
		properties.setTickFontStyle(value);
	}

	/**
	 * Sets the label font style.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setLabelFontStyle(final IScope scope, final int value) {
		properties.setLabelFontStyle(value);
	}

	/**
	 * Sets the legend font style.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setLegendFontStyle(final IScope scope, final int value) {
		properties.setLegendFontStyle(value);
	}

	/**
	 * Sets the title font style.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setTitleFontStyle(final IScope scope, final int value) {
		properties.setTitleFontStyle(value);
	}

	/**
	 * Sets the X label.
	 *
	 * @param scope
	 *            the scope
	 * @param asString
	 *            the as string
	 */
	public void setXLabel(final IScope scope, final String asString) {
		properties.setXLabel(asString);
	}

	/**
	 * Gets the x label.
	 *
	 * @param scope
	 *            the scope
	 * @return the x label
	 */
	public String getXLabel(final IScope scope) {
		return properties.getXLabel();
	}

	/**
	 * Sets the Y label.
	 *
	 * @param scope
	 *            the scope
	 * @param asString
	 *            the as string
	 */
	public void setYLabel(final IScope scope, final String asString) {
		properties.setYLabel(asString);
	}

	/**
	 * Gets the y label.
	 *
	 * @param scope
	 *            the scope
	 * @return the y label
	 */
	public String getYLabel(final IScope scope) {
		return properties.getYLabel();
	}

	/**
	 * Sets the Y 2 label.
	 *
	 * @param scope
	 *            the scope
	 * @param asString
	 *            the as string
	 */
	public void setY2Label(final IScope scope, final String asString) {
		properties.setY2Label(asString);
	}

	/**
	 * Gets the y 2 label.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 label
	 */
	public String getY2Label(final IScope scope) {
		return properties.getY2Label();
	}

	/**
	 * Gets the use X range interval.
	 *
	 * @param scope
	 *            the scope
	 * @return the use X range interval
	 */
	public boolean getUseXRangeInterval(final IScope scope) {
		return properties.isUseXRangeInterval();
	}

	/**
	 * Gets the use X range min max.
	 *
	 * @param scope
	 *            the scope
	 * @return the use X range min max
	 */
	public boolean getUseXRangeMinMax(final IScope scope) {
		return properties.isUseXRangeMinMax();
	}

	/**
	 * Gets the use Y range interval.
	 *
	 * @param scope
	 *            the scope
	 * @return the use Y range interval
	 */
	public boolean getUseYRangeInterval(final IScope scope) {
		return properties.isUseYRangeInterval();
	}

	/**
	 * Gets the use Y range min max.
	 *
	 * @param scope
	 *            the scope
	 * @return the use Y range min max
	 */
	public boolean getUseYRangeMinMax(final IScope scope) {
		return properties.isUseYRangeMinMax();
	}

	/**
	 * Gets the use Y 2 range interval.
	 *
	 * @param scope
	 *            the scope
	 * @return the use Y 2 range interval
	 */
	public boolean getUseY2RangeInterval(final IScope scope) {
		return properties.isUseY2RangeInterval();
	}

	/**
	 * Gets the use Y 2 range min max.
	 *
	 * @param scope
	 *            the scope
	 * @return the use Y 2 range min max
	 */
	public boolean getUseY2RangeMinMax(final IScope scope) {
		return properties.isUseY2RangeMinMax();
	}

	/**
	 * Sets the X range interval.
	 *
	 * @param scope
	 *            the scope
	 * @param doubleValue
	 *            the double value
	 */
	public void setXRangeInterval(final IScope scope, final double doubleValue) {
		properties.setXRangeInterval(doubleValue);
	}

	/**
	 * Gets the x range interval.
	 *
	 * @param scope
	 *            the scope
	 * @return the x range interval
	 */
	public double getXRangeInterval(final IScope scope) {
		return properties.getXRangeInterval();
	}

	/**
	 * Sets the X range min max.
	 *
	 * @param scope
	 *            the scope
	 * @param minValue
	 *            the min value
	 * @param maxValue
	 *            the max value
	 */
	public void setXRangeMinMax(final IScope scope, final double minValue, final double maxValue) {
		properties.setXRangeMinMax(minValue, maxValue);
	}

	/**
	 * Gets the x range min.
	 *
	 * @param scope
	 *            the scope
	 * @return the x range min
	 */
	public double getXRangeMin(final IScope scope) {
		return properties.getXRangeMin();
	}

	/**
	 * Gets the x range max.
	 *
	 * @param scope
	 *            the scope
	 * @return the x range max
	 */
	public double getXRangeMax(final IScope scope) {
		return properties.getXRangeMax();
	}

	/**
	 * Gets the use X min.
	 *
	 * @param scope
	 *            the scope
	 * @return the use X min
	 */
	public boolean getUseXMin(final IScope scope) {
		return properties.isUseXMin();
	}

	/**
	 * Gets the use X max.
	 *
	 * @param scope
	 *            the scope
	 * @return the use X max
	 */
	public boolean getUseXMax(final IScope scope) {
		return properties.isUseXMax();
	}

	/**
	 * Sets the X min.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setXMin(final IScope scope, final double value) {
		properties.setXMin(value);
	}

	/**
	 * Sets the X max.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setXMax(final IScope scope, final double value) {
		properties.setXMax(value);
	}

	/**
	 * Gets the x min.
	 *
	 * @param scope
	 *            the scope
	 * @return the x min
	 */
	public double getXMin(final IScope scope) {
		return properties.getXMinVal();
	}

	/**
	 * Gets the x max.
	 *
	 * @param scope
	 *            the scope
	 * @return the x max
	 */
	public double getXMax(final IScope scope) {
		return properties.getXMaxVal();
	}

	/**
	 * Gets the y range interval.
	 *
	 * @param scope
	 *            the scope
	 * @return the y range interval
	 */
	public double getYRangeInterval(final IScope scope) {
		return properties.getYRangeInterval();
	}

	/**
	 * Sets the Y range interval.
	 *
	 * @param scope
	 *            the scope
	 * @param doubleValue
	 *            the double value
	 */
	public void setYRangeInterval(final IScope scope, final double doubleValue) {
		properties.setYRangeInterval(doubleValue);
	}

	/**
	 * Sets the Y range min max.
	 *
	 * @param scope
	 *            the scope
	 * @param minValue
	 *            the min value
	 * @param maxValue
	 *            the max value
	 */
	public void setYRangeMinMax(final IScope scope, final double minValue, final double maxValue) {
		properties.setYRangeMinMax(minValue, maxValue);
	}

	/**
	 * Gets the y range min.
	 *
	 * @param scope
	 *            the scope
	 * @return the y range min
	 */
	public double getYRangeMin(final IScope scope) {
		return properties.getYRangeMin();
	}

	/**
	 * Gets the y range max.
	 *
	 * @param scope
	 *            the scope
	 * @return the y range max
	 */
	public double getYRangeMax(final IScope scope) {
		return properties.getYRangeMax();
	}

	/**
	 * Gets the use Y min.
	 *
	 * @param scope
	 *            the scope
	 * @return the use Y min
	 */
	public boolean getUseYMin(final IScope scope) {
		return properties.isUseYMin();
	}

	/**
	 * Gets the use Y max.
	 *
	 * @param scope
	 *            the scope
	 * @return the use Y max
	 */
	public boolean getUseYMax(final IScope scope) {
		return properties.isUseYMax();
	}

	/**
	 * Sets the Y min.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setYMin(final IScope scope, final double value) {
		properties.setYMin(value);
	}

	/**
	 * Sets the Y max.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	public void setYMax(final IScope scope, final double value) {
		properties.setYMax(value);
	}

	/**
	 * Gets the y min.
	 *
	 * @param scope
	 *            the scope
	 * @return the y min
	 */
	public double getYMin(final IScope scope) {
		return properties.getYMinVal();
	}

	/**
	 * Gets the y max.
	 *
	 * @param scope
	 *            the scope
	 * @return the y max
	 */
	public double getYMax(final IScope scope) {
		return properties.getYMaxVal();
	}

	/**
	 * Gets the y 2 range interval.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 range interval
	 */
	public double getY2RangeInterval(final IScope scope) {
		return properties.getY2RangeInterval();
	}

	/**
	 * Sets the Y 2 range interval.
	 *
	 * @param scope
	 *            the scope
	 * @param doubleValue
	 *            the double value
	 */
	public void setY2RangeInterval(final IScope scope, final double doubleValue) {
		properties.setY2RangeInterval(doubleValue);
	}

	/**
	 * Sets the Y 2 range min max.
	 *
	 * @param scope
	 *            the scope
	 * @param minValue
	 *            the min value
	 * @param maxValue
	 *            the max value
	 */
	public void setY2RangeMinMax(final IScope scope, final double minValue, final double maxValue) {
		properties.setY2RangeMinMax(minValue, maxValue);
	}

	/**
	 * Gets the y 2 range min.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 range min
	 */
	public double getY2RangeMin(final IScope scope) {
		return properties.getY2RangeMin();
	}

	/**
	 * Gets the y 2 range max.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 range max
	 */
	public double getY2RangeMax(final IScope scope) {
		return properties.getY2RangeMax();
	}

	/**
	 * Sets the X tick unit.
	 *
	 * @param scope
	 *            the scope
	 * @param r
	 *            the r
	 */
	public void setXTickUnit(final IScope scope, final double r) {
		properties.setXTickUnit(r);
	}

	/**
	 * Gets the x tick unit.
	 *
	 * @param scope
	 *            the scope
	 * @return the x tick unit
	 */
	public double getXTickUnit(final IScope scope) {
		return properties.getXTickUnit();
	}

	/**
	 * Sets the Y tick unit.
	 *
	 * @param scope
	 *            the scope
	 * @param r
	 *            the r
	 */
	public void setYTickUnit(final IScope scope, final double r) {
		properties.setYTickUnit(r);
	}

	/**
	 * Gets the y tick unit.
	 *
	 * @param scope
	 *            the scope
	 * @return the y tick unit
	 */
	public double getYTickUnit(final IScope scope) {
		return properties.getYTickUnit();
	}

	/**
	 * Sets the Y 2 tick unit.
	 *
	 * @param scope
	 *            the scope
	 * @param r
	 *            the r
	 */
	public void setY2TickUnit(final IScope scope, final double r) {
		properties.setY2TickUnit(r);
	}

	/**
	 * Gets the y 2 tick unit.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 tick unit
	 */
	public double getY2TickUnit(final IScope scope) {
		return properties.getY2TickUnit();
	}

	/**
	 * Sets the gap.
	 *
	 * @param scope
	 *            the scope
	 * @param range
	 *            the range
	 */
	public void setGap(final IScope scope, final double range) {
		properties.setGap(range);
	}

	/**
	 * Sets the serie marker shape.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 * @param markershape
	 *            the markershape
	 */
	public void setSerieMarkerShape(final IScope scope, final String serieid, final String markershape) {}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final IChartDataSource source, final int type_val) {}

	/**
	 * Sets the use size.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param b
	 *            the b
	 */
	public void setUseSize(final IScope scope, final String name, final boolean b) {}

	/**
	 * Sets the series label position.
	 *
	 * @param scope
	 *            the scope
	 * @param asString
	 *            the as string
	 */
	public void setSeriesLabelPosition(final IScope scope, final String asString) {
		properties.setSeriesLabelPosition(asString);
	}

	/**
	 * Sets the style.
	 *
	 * @param scope
	 *            the scope
	 * @param asString
	 *            the as string
	 */
	public void setStyle(final IScope scope, final String asString) {
		properties.setStyle(asString);
	}

	/**
	 * Inits the chart post data init.
	 *
	 * @param scope
	 *            the scope
	 */
	public void initChart_post_data_init(final IScope scope) {}

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {}

	/**
	 * Sets the reverse axis.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setReverseAxis(final IScope scope, final Boolean asBool) {
		properties.setReverseAxes(asBool);
	}

	/**
	 * Sets the X log scale.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setX_LogScale(final IScope scope, final Boolean asBool) {
		properties.setXLogscale(asBool);
	}

	/**
	 * Sets the Y log scale.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setY_LogScale(final IScope scope, final Boolean asBool) {
		properties.setYLogscale(asBool);
	}

	/**
	 * Gets the x log scale.
	 *
	 * @param scope
	 *            the scope
	 * @return the x log scale
	 */
	public boolean getX_LogScale(final IScope scope) {
		return properties.isXLogscale();
	}

	/**
	 * Gets the y log scale.
	 *
	 * @param scope
	 *            the scope
	 * @return the y log scale
	 */
	public boolean getY_LogScale(final IScope scope) {
		return properties.isYLogscale();
	}

	/**
	 * Sets the Y 2 log scale.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setY2_LogScale(final IScope scope, final Boolean asBool) {
		properties.setY2Logscale(asBool);
	}

	/**
	 * Gets the y 2 log scale.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 log scale
	 */
	public boolean getY2_LogScale(final IScope scope) {
		return properties.isY2Logscale();
	}

	/**
	 * Sets the use second Y axis.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setUseSecondYAxis(final IScope scope, final Boolean asBool) {
		properties.setUseSecondYAxis(asBool);
	}

	/**
	 * Gets the use second Y axis.
	 *
	 * @param scope
	 *            the scope
	 * @return the use second Y axis
	 */
	public boolean getUseSecondYAxis(final IScope scope) {
		return properties.isUseSecondYAxis();
	}

	/**
	 * Sets the X tick value visible.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setXTickValueVisible(final IScope scope, final Boolean asBool) {
		properties.setXTickValueVisible(asBool);
	}

	/**
	 * Gets the x tick value visible.
	 *
	 * @param scope
	 *            the scope
	 * @return the x tick value visible
	 */
	public boolean getXTickValueVisible(final IScope scope) {
		return properties.isXTickValueVisible();
	}

	/**
	 * Sets the Y tick value visible.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setYTickValueVisible(final IScope scope, final Boolean asBool) {
		properties.setYTickValueVisible(asBool);
	}

	/**
	 * Gets the y tick value visible.
	 *
	 * @param scope
	 *            the scope
	 * @return the y tick value visible
	 */
	public boolean getYTickValueVisible(final IScope scope) {
		return properties.isYTickValueVisible();
	}

	/**
	 * Sets the Y 2 tick value visible.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setY2TickValueVisible(final IScope scope, final Boolean asBool) {
		properties.setY2TickValueVisible(asBool);
	}

	/**
	 * Gets the y 2 tick value visible.
	 *
	 * @param scope
	 *            the scope
	 * @return the y 2 tick value visible
	 */
	public boolean getY2TickValueVisible(final IScope scope) {
		return properties.isY2TickValueVisible();
	}

	/**
	 * Sets the title visible.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setTitleVisible(final IScope scope, final Boolean asBool) {
		properties.setTitleVisible(asBool);
	}

	/**
	 * Gets the title visible.
	 *
	 * @param scope
	 *            the scope
	 * @return the title visible
	 */
	public boolean getTitleVisible(final IScope scope) {
		return properties.isTitleVisible();
	}

	/**
	 * Sets the X tick line visible.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setXTickLineVisible(final IScope scope, final Boolean asBool) {
		properties.setXTickLineVisible(asBool);
	}

	/**
	 * Gets the x tick line visible.
	 *
	 * @param scope
	 *            the scope
	 * @return the x tick line visible
	 */
	public boolean getXTickLineVisible(final IScope scope) {
		return properties.isXTickLineVisible();
	}

	/**
	 * Sets the Y tick line visible.
	 *
	 * @param scope
	 *            the scope
	 * @param asBool
	 *            the as bool
	 */
	public void setYTickLineVisible(final IScope scope, final Boolean asBool) {
		properties.setYTickLineVisible(asBool);
	}

	/**
	 * Gets the y tick line visible.
	 *
	 * @param scope
	 *            the scope
	 * @return the y tick line visible
	 */
	public boolean getYTickLineVisible(final IScope scope) {
		return properties.isYTickLineVisible();
	}

	/**
	 * Sets the grid lines visible.
	 *
	 * @param scope
	 *            the scope
	 * @param visible
	 *            the visible
	 */
	public void setGridLinesVisible(final IScope scope, final Boolean visible) {
		properties.setGridLinesVisible(visible);
	}

	/**
	 * Gets the grid lines visible.
	 *
	 * @return the grid lines visible
	 */
	public boolean getGridLinesVisible() { return properties.isGridLinesVisible(); }

	/**
	 * Dispose.
	 *
	 * @param scope
	 *            the scope
	 */
	public void dispose(final IScope scope) {}

	/**
	 * Sets the series label anchor.
	 *
	 * @param scope
	 *            the scope
	 * @param pt
	 *            the pt
	 */
	public void setSeriesLabelAnchor(final IScope scope, final IPoint pt) {
		properties.setSeriesLabelAnchor(pt);
	}

	/**
	 * Sets the legend orientation.
	 *
	 * @param scope
	 *            the scope
	 * @param orient
	 *            the orient
	 */
	public void setLegendOrientation(final IScope scope, final String orient) {
		properties.setLegendOrientation(orient);
	}

}
