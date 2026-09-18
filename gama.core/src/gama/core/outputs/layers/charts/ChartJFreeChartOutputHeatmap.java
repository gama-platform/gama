/*******************************************************************************************************
 *
 * ChartJFreeChartOutputHeatmap.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Color;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;

import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IChartDataSource;

/**
 * JFreeChart implementation for Heatmap charts.
 */
public class ChartJFreeChartOutputHeatmap extends ChartJFreeChartOutput {

	public ChartJFreeChartOutputHeatmap(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		jfreedataset.add(0, new MatrixSeriesCollection());
		PlotOrientation orientation = properties.isReverseAxes() ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		chart = ChartFactory.createXYLineChart(getName(), "", "", (MatrixSeriesCollection) jfreedataset.get(0),
				orientation, true, false, false);
	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final IChartDataSource source, final int type_val) {
		switch (type_val) {
			case IChartDataSource.DATA_TYPE_LIST_DOUBLE_N, IChartDataSource.DATA_TYPE_LIST_DOUBLE_3, IChartDataSource.DATA_TYPE_LIST_DOUBLE_12 -> {
				source.setCumulative(scope, false);
				source.setCumulativeY(scope, true);
				source.setUseSize(scope, true);
			}
			default -> {
				source.setCumulative(scope, false);
				source.setUseSize(scope, true);
			}
		}
	}

	@Override
	public void initdataset() {
		super.initdataset();
		chartdataset.setCommonXSeries(true);
		chartdataset.setCommonYSeries(true);
		chartdataset.setByCategory(false);
		chartdataset.forceNoXAccumulate = true;
		chartdataset.forceNoYAccumulate = true;
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		return new XYBlockRenderer();
	}

	protected static LookupPaintScale createLUT(final int ncol, final float vmin, final float vmax,
			final Color start, final Color med, final Color end) {
		if (vmin >= vmax) return new LookupPaintScale();
		final LookupPaintScale lut = new LookupPaintScale(vmin, vmax, med);
		final float vSpan = vmax - vmin;
		final float vScale = 1f / (ncol - 0.99f);
		final int sr = start.getRed(), sg = start.getGreen(), sb = start.getBlue(), sa = start.getAlpha();
		final int mr = med.getRed(), mg = med.getGreen(), mb = med.getBlue(), ma = med.getAlpha();
		final int er = end.getRed(), eg = end.getGreen(), eb = end.getBlue(), ea = end.getAlpha();
		for (int j = 0; j < ncol; j++) {
			final float val = j * vScale;
			final boolean firstHalf = val < 0.5f;
			final float t = firstHalf ? val * 2f : (val - 0.5f) * 2f;
			final int r = firstHalf ? Math.round(sr + t * (mr - sr)) : Math.round(mr + t * (er - mr));
			final int g = firstHalf ? Math.round(sg + t * (mg - sg)) : Math.round(mg + t * (eg - mg));
			final int b = firstHalf ? Math.round(sb + t * (mb - sb)) : Math.round(mb + t * (eb - mb));
			final int a = firstHalf ? Math.round(sa + t * (ma - sa)) : Math.round(ma + t * (ea - ma));
			lut.add(val * vSpan + vmin, new Color(r, g, b, a));
		}
		return lut;
	}

	protected static LookupPaintScale createLUT(final int ncol, final float vmin, final float vmax,
			final Color start, final Color end) {
		if (vmin >= vmax) return new LookupPaintScale();
		final LookupPaintScale lut = new LookupPaintScale(vmin, vmax, start);
		final float vSpan = vmax - vmin;
		final float vScale = 1f / (ncol - 0.99f);
		final int sr = start.getRed(), sg = start.getGreen(), sb = start.getBlue(), sa = start.getAlpha();
		final int er = end.getRed(), eg = end.getGreen(), eb = end.getBlue(), ea = end.getAlpha();
		for (int j = 0; j < ncol; j++) {
			final float val = j * vScale;
			final int r = Math.round(sr + val * (er - sr));
			final int g = Math.round(sg + val * (eg - sg));
			final int b = Math.round(sb + val * (eb - sb));
			final int a = Math.round(sa + val * (ea - sa));
			lut.add(val * vSpan + vmin, new Color(r, g, b, a));
		}
		return lut;
	}

	protected void resetRenderer(final IScope scope, final String serieid) {
		if (chart == null) return;
		final XYBlockRenderer newr = (XYBlockRenderer) this.getOrCreateRenderer(scope, serieid);
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (myserie == null) return;

		if (myserie.getMycolor() != null) { newr.setSeriesPaint(0, IColor.toAWTColor(myserie.getMycolor())); }
		if (!myserie.getSValues(scope).isEmpty()) {
			final double maxval = Collections.max(myserie.getSValues(scope));
			final double minval = Collections.min(myserie.getSValues(scope));
			Color cdeb = myserie.getMyMincolor() != null ? IColor.toAWTColor(myserie.getMyMincolor()) : new Color(0, 0, 0, 0);
			Color cend = myserie.getMycolor() != null ? IColor.toAWTColor(myserie.getMycolor()) : new Color(0.9f, 0.9f, 0.9f, 1.0f);

			LookupPaintScale paintscale = createLUT(100, (float) minval, (float) maxval, cdeb, cend);
			if (myserie.getMyMedcolor() != null) {
				paintscale = createLUT(100, (float) minval, (float) maxval, cdeb,
						IColor.toAWTColor(myserie.getMyMedcolor()), cend);
			}
			newr.setPaintScale(paintscale);

			final NumberAxis scaleAxis = new NumberAxis(myserie.getName());
			Color ac = IColor.toAWTColor(properties.getAxesColor());
			scaleAxis.setAxisLinePaint(ac);
			scaleAxis.setTickMarkPaint(ac);
			scaleAxis.setTickLabelFont(properties.getTickFont());
			scaleAxis.setRange(paintscale.getLowerBound(), paintscale.getUpperBound());
			scaleAxis.setLabelFont(properties.getLabelFont());
			if (properties.getTextColor() != null) {
				Color tc = IColor.toAWTColor(properties.getTextColor());
				scaleAxis.setLabelPaint(tc);
				scaleAxis.setTickLabelPaint(tc);
			}
			if (!properties.isXTickValueVisible()) {
				scaleAxis.setTickMarksVisible(false);
				scaleAxis.setTickLabelsVisible(false);
			}

			final PaintScaleLegend legend = new PaintScaleLegend(paintscale, scaleAxis);
			legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			legend.setAxisOffset(5.0);
			legend.setPosition(RectangleEdge.RIGHT);
			legend.setBackgroundPaint(IColor.toAWTColor(properties.getBackgroundColor()));
			if (!"none".equals(properties.getSeriesLabelPosition())) { chart.addSubtitle(legend); }
		}
	}

	@Override
	protected void clearDataSet(final IScope scope) {
		super.clearDataSet(scope);
		if (chart == null) return;
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		for (int i = plot.getDatasetCount() - 1; i >= 1; i--) {
			plot.setDataset(i, null);
			plot.setRenderer(i, null);
		}
		((MatrixSeriesCollection) jfreedataset.get(0)).removeAllSeries();
		jfreedataset.clear();
		jfreedataset.add(0, new MatrixSeriesCollection());
		plot.setDataset((MatrixSeriesCollection) jfreedataset.get(0));
		plot.setRenderer(0, null);
		idPosition.clear();
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		if (dataserie == null || chart == null) return;
		final MatrixSeries serie = new MatrixSeries((String) dataserie.getSerieLegend(scope),
				Math.max(1, this.getChartdataset().getYSeriesValues().size()),
				Math.max(1, this.getChartdataset().getXSeriesValues().size()));
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final MatrixSeriesCollection firstdataset = (MatrixSeriesCollection) plot.getDataset();

		if (!idPosition.containsKey(serieid)) {
			if (firstdataset.getSeriesCount() == 0) {
				firstdataset.addSeries(serie);
				plot.setDataset(0, firstdataset);
			} else {
				final MatrixSeriesCollection newdataset = new MatrixSeriesCollection();
				newdataset.addSeries(serie);
				jfreedataset.add(newdataset);
				plot.setDataset(jfreedataset.size() - 1, newdataset);
			}
			plot.setRenderer(jfreedataset.size() - 1, (XYItemRenderer) getOrCreateRenderer(scope, serieid));
			idPosition.put(serieid, jfreedataset.size() - 1);
		}
	}

	@Override
	public void preResetSeries(final IScope scope) {
		this.clearDataSet(scope);
		if (chart != null) { chart.setSubtitles(new ArrayList<Title>()); }
	}

	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		this.createNewSerie(scope, serieid);
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		if (dataserie == null || chart == null) return;

		final MatrixSeries serie = ((MatrixSeriesCollection) jfreedataset.get(idPosition.get(dataserie.getSerieId(scope)))).getSeries(0);
		final ArrayList<Double> xValues = dataserie.getXValues(scope);
		final ArrayList<Double> yValues = dataserie.getYValues(scope);
		final ArrayList<Double> sValues = dataserie.getSValues(scope);
		final NumberAxis domainAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getDomainAxis();
		final NumberAxis rangeAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis();

		if (xValues.isEmpty() && !properties.isUseXRangeInterval() && !properties.isUseXRangeMinMax()) {
			domainAxis.setAutoRange(false);
			domainAxis.setRange(-0.5, xValues.size() + 0.5);
		}
		if (yValues.isEmpty() && !properties.isUseYRangeInterval() && !properties.isUseYRangeMinMax()) {
			rangeAxis.setAutoRange(false);
			rangeAxis.setRange(-0.5, yValues.size() + 0.5);
		}

		if (!xValues.isEmpty()) {
			domainAxis.setAutoRange(false);
			rangeAxis.setAutoRange(false);
			domainAxis.setTickLabelsVisible(properties.isXTickValueVisible());
			domainAxis.setTickMarksVisible(properties.isXTickValueVisible());
			rangeAxis.setTickLabelsVisible(properties.isYTickValueVisible());
			rangeAxis.setTickMarksVisible(properties.isYTickValueVisible());
			for (int i = 0; i < xValues.size(); i++) {
				if (xValues.get(i) > domainAxis.getUpperBound() && !properties.isUseXRangeInterval() && !properties.isUseXRangeMinMax()) {
					domainAxis.setRange(-0.5, yValues.get(i) + 0.5);
				}
				if (yValues.get(i) > rangeAxis.getUpperBound() && !properties.isUseYRangeInterval() && !properties.isUseYRangeMinMax()) {
					rangeAxis.setRange(-0.5, yValues.get(i) + 0.5);
				}
				serie.update(yValues.get(i).intValue(), xValues.get(i).intValue(), sValues.get(i).doubleValue());
			}
		}
		this.resetRenderer(scope, serieid);
	}

	@Override
	public void resetAxes(final IScope scope) {
		if (chart == null) return;
		XYPlot plot = (XYPlot) this.chart.getPlot();
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		if (properties.isXLogscale()) {
			final LogarithmicAxis logAxis = new LogarithmicAxis(domainAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			plot.setDomainAxis(logAxis);
			domainAxis = logAxis;
		}
		if (properties.isYLogscale()) {
			final LogarithmicAxis logAxis = new LogarithmicAxis(rangeAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			plot.setRangeAxis(logAxis);
			rangeAxis = logAxis;
		}

		if (properties.isUseXRangeInterval()) {
			domainAxis.setFixedAutoRange(properties.getXRangeInterval());
			domainAxis.setAutoRangeMinimumSize(properties.getXRangeInterval());
			domainAxis.setAutoRange(true);
		}
		if (properties.isUseXRangeMinMax()) { domainAxis.setRange(properties.getXRangeMin(), properties.getXRangeMax()); }
		if ((properties.isUseXMin() || properties.isUseXMax()) && !properties.isUseXRangeMinMax()) { applyXSingleBounds(scope, domainAxis); }

		if (properties.isUseYRangeInterval()) {
			rangeAxis.setFixedAutoRange(properties.getYRangeInterval());
			rangeAxis.setAutoRangeMinimumSize(properties.getYRangeInterval());
			rangeAxis.setAutoRange(true);
		}
		if (properties.isUseYRangeMinMax()) { rangeAxis.setRange(properties.getYRangeMin(), properties.getYRangeMax()); }
		if ((properties.isUseYMin() || properties.isUseYMax()) && !properties.isUseYRangeMinMax()) { applyYSingleBounds(scope, rangeAxis); }

		if ("none".equals(properties.getSeriesLabelPosition()) && this.chart.getLegend() != null) {
			this.chart.getLegend().setVisible(false);
		}
		if (!properties.isXTickLineVisible()) { plot.setDomainGridlinesVisible(false); }
		if (!properties.isYTickLineVisible()) { plot.setRangeGridlinesVisible(false); }
	}

	@Override
	protected void initRenderer(final IScope scope) {
		if (chart == null) return;
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		defaultrenderer = new XYBlockRenderer();
		plot.setRenderer((XYBlockRenderer) defaultrenderer);
	}

	@Override
	public void setUseXLabels(final IScope scope, final IExpression expval) {
		if (chart == null) return;
		final XYPlot pp = (XYPlot) chart.getPlot();
		((NumberAxis) pp.getDomainAxis()).setNumberFormatOverride(new NumberFormat() {
			@Override
			public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
				final int ind = chartdataset.XSeriesValues.indexOf(number);
				if (ind >= 0 && ind < chartdataset.Xcategories.size()) return new StringBuffer(chartdataset.Xcategories.get(ind));
				return new StringBuffer();
			}
			@Override
			public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) { return new StringBuffer("n" + number); }
			@Override
			public Number parse(final String source, final ParsePosition parsePosition) { return null; }
		});
	}

	@Override
	public void setUseYLabels(final IScope scope, final IExpression expval) {
		if (chart == null) return;
		final XYPlot pp = (XYPlot) chart.getPlot();
		((NumberAxis) pp.getRangeAxis()).setNumberFormatOverride(new NumberFormat() {
			@Override
			public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
				final int ind = chartdataset.YSeriesValues.indexOf(number);
				if (ind >= 0 && ind < chartdataset.Ycategories.size()) return new StringBuffer(chartdataset.Ycategories.get(ind));
				return new StringBuffer();
			}
			@Override
			public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) { return new StringBuffer("n" + number); }
			@Override
			public Number parse(final String source, final ParsePosition parsePosition) { return null; }
		});
	}

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);
		if (chart == null) return;
		Color ac = IColor.toAWTColor(properties.getAxesColor());
		Color tc = properties.getTextColor() != null ? IColor.toAWTColor(properties.getTextColor()) : null;
		final XYPlot pp = (XYPlot) chart.getPlot();
		pp.setDomainGridlinePaint(ac);
		pp.setRangeGridlinePaint(ac);
		pp.setDomainCrosshairPaint(ac);
		pp.setRangeCrosshairPaint(ac);
		pp.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		pp.setDomainCrosshairVisible(false);
		pp.setRangeCrosshairVisible(false);
		pp.setRangeGridlinesVisible(false);
		pp.setDomainGridlinesVisible(false);

		pp.getDomainAxis().setAxisLinePaint(ac);
		pp.getDomainAxis().setTickLabelFont(properties.getTickFont());
		pp.getDomainAxis().setLabelFont(properties.getLabelFont());
		if (tc != null) {
			pp.getDomainAxis().setLabelPaint(tc);
			pp.getDomainAxis().setTickLabelPaint(tc);
		}
		if (properties.getXTickUnit() > 0) { ((NumberAxis) pp.getDomainAxis()).setTickUnit(new NumberTickUnit(properties.getXTickUnit())); }

		pp.getRangeAxis().setAxisLinePaint(ac);
		pp.getRangeAxis().setLabelFont(properties.getLabelFont());
		pp.getRangeAxis().setTickLabelFont(properties.getTickFont());
		if (tc != null) {
			pp.getRangeAxis().setLabelPaint(tc);
			pp.getRangeAxis().setTickLabelPaint(tc);
		}
		if (properties.getYTickUnit() > 0) { ((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(properties.getYTickUnit())); }

		if (properties.getXLabel() != null && !properties.getXLabel().isEmpty()) { pp.getDomainAxis().setLabel(properties.getXLabel()); }
		if (properties.getYLabel() != null && !properties.getYLabel().isEmpty()) { pp.getRangeAxis().setLabel(properties.getYLabel()); }
	}

}
