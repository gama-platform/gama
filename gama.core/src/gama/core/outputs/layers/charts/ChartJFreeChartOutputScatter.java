/*******************************************************************************************************
 *
 * ChartJFreeChartOutputScatter.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalDataItem;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IChartDataSource;
import gama.api.ui.displays.IDisplaySurface;

/**
 * JFreeChart output implementation for Scatter, Series, and XY charts.
 */
public class ChartJFreeChartOutputScatter extends ChartJFreeChartOutput {

	public class CustomXYErrorRenderer extends XYErrorRenderer {
		private static final long serialVersionUID = 1L;

		ChartJFreeChartOutputScatter myoutput;
		String myid;
		boolean useSize;
		AffineTransform transform = new AffineTransform();

		public boolean isUseSize() { return useSize; }
		public void setUseSize(final IScope scope, final boolean useSize) { this.useSize = useSize; }
		public void setMyid(final String myid) { this.myid = myid; }
		public void setOutput(final ChartJFreeChartOutput output) { myoutput = (ChartJFreeChartOutputScatter) output; }

		@Override
		public Shape getItemShape(final int row, final int col) {
			if (isUseSize() && myoutput != null) {
				transform.setToScale(myoutput.getScale(myid, col), myoutput.getScale(myid, col));
				return transform.createTransformedShape(super.getItemShape(row, col));
			}
			return super.getItemShape(row, col);
		}
	}

	double getScale(final String serie, final int col) {
		DoubleList scales = markerScale.get(serie);
		if (scales == null) return 1;
		if (col < 0 || col >= scales.size()) return 1;
		return scales.get(col);
	}

	HashMap<String, DoubleList> markerScale = new HashMap<>();

	public ChartJFreeChartOutputScatter(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);

		jfreedataset.add(0, new XYIntervalSeriesCollection());
		PlotOrientation orientation = properties.isReverseAxes() ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;

		switch (type) {
			case SERIES_CHART, XY_CHART, SCATTER_CHART -> chart = ChartFactory.createXYLineChart(getName(), "", "",
					(XYIntervalSeriesCollection) jfreedataset.get(0), orientation, true, false, false);
			case BOX_WHISKER_CHART -> {
				chart = ChartFactory.createBoxAndWhiskerChart(getName(), "Time", "Value",
						(BoxAndWhiskerCategoryDataset) jfreedataset.get(0), true);
				chart.setBackgroundPaint(new Color(249, 231, 236));
			}
			default -> {}
		}
	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final IChartDataSource source, final int type_val) {
		switch (type_val) {
			case IChartDataSource.DATA_TYPE_LIST_DOUBLE_N, IChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_N, IChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_12, IChartDataSource.DATA_TYPE_LIST_POINT, IChartDataSource.DATA_TYPE_MATRIX_DOUBLE -> {
				source.setCumulative(scope, false);
				source.setUseSize(scope, false);
			}
			case IChartDataSource.DATA_TYPE_LIST_DOUBLE_3 -> {
				source.setCumulative(scope, true);
				source.setUseSize(scope, true);
			}
			case IChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_3 -> {
				source.setCumulative(scope, false);
				source.setUseSize(scope, true);
			}
			default -> {
				source.setCumulative(scope, true);
				source.setUseSize(scope, false);
			}
		}
	}

	@Override
	public void initdataset() {
		super.initdataset();
		switch (type) {
			case ChartOutput.SERIES_CHART -> {
				chartdataset.setCommonXSeries(true);
				chartdataset.setByCategory(false);
			}
			case ChartOutput.XY_CHART, ChartOutput.SCATTER_CHART -> {
				chartdataset.setCommonXSeries(false);
				chartdataset.setByCategory(false);
			}
			default -> {}
		}
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		final String theStyle = this.getChartdataset().getDataSeries(scope, serieid).getStyle(scope);
		return switch (theStyle) {
			case IKeyword.SPLINE -> new XYSplineRenderer();
			case IKeyword.STEP -> new XYStepRenderer();
			case IKeyword.DOT -> new XYShapeRenderer();
			case IKeyword.WHISKER -> new XYBoxAndWhiskerRenderer();
			case IKeyword.AREA -> new XYAreaRenderer();
			case IKeyword.BAR -> new XYBarRenderer();
			case IKeyword.THREE_D -> new XYLineAndShapeRenderer();
			default -> {
				CustomXYErrorRenderer errRenderer = new CustomXYErrorRenderer();
				errRenderer.setMyid(serieid);
				errRenderer.setOutput(this);
				yield errRenderer;
			}
		};
	}

	private void configureLegend(final AbstractXYItemRenderer newr, final ChartDataSeries myserie, final IScope scope) {
		final String legStr = myserie.getSerieLegend(scope) == null ? "" : myserie.getSerieLegend(scope).toString();
		if (StringUtils.isBlank(legStr)) {
			newr.setSeriesVisibleInLegend(0, false);
			return;
		}
		newr.setSeriesVisibleInLegend(0, true);
		newr.setLegendItemLabelGenerator((dataset, series) -> {
			String id = (String) dataset.getSeriesKey(series);
			ChartDataSeries ds = getChartdataset().getDataSeries(scope, id);
			return ds != null && ds.getSerieLegend(scope) != null ? ds.getSerieLegend(scope).toString() : id;
		});
	}

	private void configureErrorRenderer(final CustomXYErrorRenderer xy, final ChartDataSeries myserie, final IScope scope) {
		xy.setDrawYError(myserie.isUseYErrValues());
		xy.setDrawXError(myserie.isUseXErrValues());
		if (myserie.getMysource().isUseSize()) { xy.setUseSize(scope, true); }
	}

	protected void resetRenderer(final IScope scope, final String serieid) {
		if (chart == null) return;
		final AbstractXYItemRenderer newr = (AbstractXYItemRenderer) this.getOrCreateRenderer(scope, serieid);
		newr.setDefaultCreateEntities(true);

		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (myserie == null) return;

		configureLegend(newr, myserie, scope);

		if (newr instanceof XYLineAndShapeRenderer xy) {
			xy.setSeriesLinesVisible(0, myserie.getMysource().showLine);
			xy.setSeriesShapesFilled(0, myserie.getMysource().fillMarker);
			xy.setSeriesShapesVisible(0, myserie.getMysource().useMarker);
		}

		if (newr instanceof XYShapeRenderer xy && !myserie.getMysource().fillMarker) {
			xy.setUseFillPaint(false);
		}

		if (myserie.getMycolor() != null) { newr.setSeriesPaint(0, IColor.toAWTColor(myserie.getMycolor())); }

		float thickness = Cast.asFloat(scope, myserie.getLineThickness().value(scope)).floatValue();
		newr.setSeriesStroke(0, getStroke(thickness));

		if (newr instanceof CustomXYErrorRenderer xy) {
			configureErrorRenderer(xy, myserie, scope);
		}

		String markerShape = myserie.getMysource().getUniqueMarkerName();
		if (markerShape == null && myserie.getMysource().useMarker) {
			markerShape = "default";
		}
		if (markerShape != null) {
			setSerieMarkerShape(scope, myserie.getName(), markerShape);
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
		((XYIntervalSeriesCollection) jfreedataset.get(0)).removeAllSeries();
		jfreedataset.clear();
		jfreedataset.add(0, new XYIntervalSeriesCollection());
		plot.setDataset((XYIntervalSeriesCollection) jfreedataset.get(0));
		plot.setRenderer(0, null);
		idPosition.clear();
		rendererSet.clear();
		markerScale.clear();
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		if (chart == null) return;
		final XYIntervalSeries serie = new XYIntervalSeries(serieid, false, true);
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final XYIntervalSeriesCollection firstdataset = (XYIntervalSeriesCollection) plot.getDataset();

		if (!idPosition.containsKey(serieid)) {
			if (firstdataset.getSeriesCount() == 0) {
				firstdataset.addSeries(serie);
				plot.setDataset(0, firstdataset);
			} else {
				final XYIntervalSeriesCollection newdataset = new XYIntervalSeriesCollection();
				newdataset.addSeries(serie);
				jfreedataset.add(newdataset);
				plot.setDataset(jfreedataset.size() - 1, newdataset);
			}
			plot.setRenderer(jfreedataset.size() - 1, (XYItemRenderer) getOrCreateRenderer(scope, serieid));
			idPosition.put(serieid, jfreedataset.size() - 1);
		}
	}

	private XYIntervalDataItem buildIntervalDataItem(final ChartDataSeries dataserie, final double xVal, final double yVal, final int i) {
		if (dataserie.isUseYErrValues()) {
			if (dataserie.isUseXErrValues()) {
				return new XYIntervalDataItem(xVal, dataserie.xerrvaluesmin.get(i), dataserie.xerrvaluesmax.get(i),
						yVal, dataserie.yerrvaluesmin.get(i), dataserie.yerrvaluesmax.get(i));
			}
			return new XYIntervalDataItem(xVal, xVal, xVal, yVal, dataserie.yerrvaluesmin.get(i), dataserie.yerrvaluesmax.get(i));
		}
		if (dataserie.isUseXErrValues()) {
			return new XYIntervalDataItem(xVal, dataserie.xerrvaluesmin.get(i), dataserie.xerrvaluesmax.get(i), yVal, yVal, yVal);
		}
		return new XYIntervalDataItem(xVal, xVal, xVal, yVal, yVal, yVal);
	}

	@SuppressWarnings ("unchecked")
	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		if (chart == null) return;
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		if (dataserie == null || !idPosition.containsKey(dataserie.getSerieId(scope))) return;

		final XYIntervalSeries serie = ((XYIntervalSeriesCollection) jfreedataset.get(idPosition.get(dataserie.getSerieId(scope)))).getSeries(0);
		serie.clear();
		final DoubleList xValues = dataserie.getXValues(scope);
		final DoubleList yValues = dataserie.getYValues(scope);
		final DoubleList sValues = dataserie.getSValues(scope);
		boolean secondaxis = dataserie.getMysource().getUseSecondYAxis(scope);
		if (secondaxis) { this.setUseSecondYAxis(scope, true); }

		if (!xValues.isEmpty()) {
			final NumberAxis domainAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getDomainAxis();
			final NumberAxis rangeAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis(0);
			final int ids = idPosition.get(dataserie.getSerieId(scope));
			((XYPlot) this.chart.getPlot()).mapDatasetToRangeAxis(ids, secondaxis ? 1 : 0);
			domainAxis.setAutoRange(false);
			rangeAxis.setAutoRange(false);
			boolean oldNotify = serie.getNotify();
			serie.setNotify(false);
			try {
				int total = xValues.size();
				int stride = total > 3000 ? total / 2000 : 1;
				for (int i = 0; i < total; i += stride) {
					serie.add(buildIntervalDataItem(dataserie, xValues.get(i), yValues.get(i), i), false);
				}
				if (stride > 1 && (total - 1) % stride != 0) {
					int last = total - 1;
					serie.add(buildIntervalDataItem(dataserie, xValues.get(last), yValues.get(last), last), false);
				}
			} finally {
				serie.setNotify(oldNotify);
			}
		}
		if (!sValues.isEmpty()) {
			markerScale.remove(serieid);
			markerScale.put(serieid, sValues.clone());
		}
		this.resetRenderer(scope, serieid);
	}

	public NumberAxis formatYAxis(final IScope scope, final NumberAxis axis) {
		return formatYAxis(scope, axis, false);
	}

	public NumberAxis formatYAxis(final IScope scope, final NumberAxis axis, final boolean isSecondAxis) {
		if (axis == null) return axis;
		Color ac = IColor.toAWTColor(properties.getAxesColor());
		axis.setAxisLinePaint(ac);
		axis.setTickLabelFont(properties.getTickFont());
		axis.setLabelFont(properties.getLabelFont());
		if (properties.getTextColor() != null) {
			Color tc = IColor.toAWTColor(properties.getTextColor());
			axis.setLabelPaint(tc);
			axis.setTickLabelPaint(tc);
		}
		boolean visible = isSecondAxis ? this.getY2TickValueVisible(scope) : this.getYTickValueVisible(scope);
		axis.setTickMarksVisible(visible);
		axis.setTickLabelsVisible(visible);
		return axis;
	}

	private void configureXAxis(final IScope scope, final NumberAxis domainAxis, final Color tc) {
		if (!properties.isUseXRangeInterval() && !properties.isUseXRangeMinMax() && !properties.isUseXMin() && !properties.isUseXMax()) {
			domainAxis.setAutoRange(true);
		}
		if (properties.isUseXRangeInterval()) {
			domainAxis.setFixedAutoRange(properties.getXRangeInterval());
			domainAxis.setAutoRangeMinimumSize(properties.getXRangeInterval());
			domainAxis.setAutoRange(true);
		}
		if (properties.isUseXRangeMinMax()) {
			double max = properties.getXRangeMax();
			double min = properties.getXRangeMin();
			if (max - min > 0) { domainAxis.setRange(min, max); } else { domainAxis.setAutoRange(true); }
		}
		if ((properties.isUseXMin() || properties.isUseXMax()) && !properties.isUseXRangeMinMax()) {
			applyXSingleBounds(scope, domainAxis);
		}
		if (properties.isXTickLineVisible()) {
			if (tc != null) ((XYPlot) this.chart.getPlot()).setDomainGridlinePaint(tc);
			if (properties.getXTickUnit() > 0) {
				domainAxis.setTickUnit(new NumberTickUnit(properties.getXTickUnit()));
				((XYPlot) this.chart.getPlot()).setDomainGridlinesVisible(true);
			} else {
				((XYPlot) this.chart.getPlot()).setDomainGridlinesVisible(properties.isGridLinesVisible());
			}
		} else {
			((XYPlot) this.chart.getPlot()).setDomainGridlinesVisible(false);
		}
		if (properties.getXLabel() != null && !properties.getXLabel().isEmpty()) { domainAxis.setLabel(properties.getXLabel()); }
		if (!properties.isXTickValueVisible()) {
			domainAxis.setTickMarksVisible(false);
			domainAxis.setTickLabelsVisible(false);
		}
	}

	private void configureYAxis(final IScope scope, final NumberAxis rangeAxis, final Color tc) {
		if (!properties.isUseYRangeInterval() && !properties.isUseYRangeMinMax() && !properties.isUseYMin() && !properties.isUseYMax()) {
			rangeAxis.setAutoRange(true);
		}
		if (properties.isUseYRangeInterval()) {
			rangeAxis.setFixedAutoRange(properties.getYRangeInterval());
			rangeAxis.setAutoRangeMinimumSize(properties.getYRangeInterval());
			rangeAxis.setAutoRange(true);
		}
		if (properties.isUseYRangeMinMax()) {
			double max = properties.getYRangeMax();
			double min = properties.getYRangeMin();
			if (max - min > 0) { rangeAxis.setRange(min, max); } else { rangeAxis.setAutoRange(true); }
		}
		if ((properties.isUseYMin() || properties.isUseYMax()) && !properties.isUseYRangeMinMax()) {
			applyYSingleBounds(scope, rangeAxis);
		}
		if (properties.isYTickLineVisible()) {
			if (tc != null) ((XYPlot) this.chart.getPlot()).setRangeGridlinePaint(tc);
			if (properties.getYTickUnit() > 0) {
				rangeAxis.setTickUnit(new NumberTickUnit(properties.getYTickUnit()));
				((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(true);
			} else {
				((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(properties.isGridLinesVisible());
			}
		} else {
			((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(false);
		}
		if (properties.getYLabel() != null && !properties.getYLabel().isEmpty()) { rangeAxis.setLabel(properties.getYLabel()); }
	}

	private void configureY2Axis(final IScope scope, final NumberAxis range2Axis, final Color tc) {
		if (!properties.isUseY2RangeInterval() && !properties.isUseY2RangeMinMax()) { range2Axis.setAutoRange(true); }
		if (properties.isUseY2RangeInterval()) {
			range2Axis.setFixedAutoRange(properties.getY2RangeInterval());
			range2Axis.setAutoRangeMinimumSize(properties.getY2RangeInterval());
			range2Axis.setAutoRange(true);
		}
		if (properties.isUseY2RangeMinMax()) {
			double max = properties.getY2RangeMax();
			double min = properties.getY2RangeMin();
			if (max - min > 0) { range2Axis.setRange(min, max); } else { range2Axis.setAutoRange(true); }
		}
		if (properties.isYTickLineVisible()) {
			if (tc != null) ((XYPlot) this.chart.getPlot()).setRangeGridlinePaint(tc);
			if (properties.getY2TickUnit() > 0) {
				range2Axis.setTickUnit(new NumberTickUnit(properties.getY2TickUnit()));
				((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(true);
			} else {
				((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(properties.isGridLinesVisible());
			}
		} else {
			((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(false);
		}
		if (properties.getY2Label() != null && !properties.getY2Label().isEmpty()) {
			range2Axis.setLabel(properties.getY2Label());
		}
	}

	@Override
	public void resetAxes(final IScope scope) {
		if (chart == null) return;
		XYPlot plot = (XYPlot) this.chart.getPlot();
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		NumberAxis range2Axis = rangeAxis;
		boolean secondaxis = properties.isUseSecondYAxis();

		if (secondaxis) {
			range2Axis = (NumberAxis) plot.getRangeAxis(1);
			if (range2Axis == null) {
				range2Axis = new NumberAxis("");
				plot.setRangeAxis(1, range2Axis);
			}
			formatYAxis(scope, range2Axis, true);
		}

		if (properties.isXLogscale()) {
			LogarithmicAxis logAxis = new LogarithmicAxis(domainAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			plot.setDomainAxis(logAxis);
			domainAxis = logAxis;
		}
		if (properties.isYLogscale()) {
			LogarithmicAxis logAxis = new LogarithmicAxis(rangeAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			formatYAxis(scope, logAxis, false);
			plot.setRangeAxis(logAxis);
			rangeAxis = logAxis;
		} else {
			formatYAxis(scope, rangeAxis, false);
		}
		if (secondaxis && properties.isY2Logscale()) {
			LogarithmicAxis logAxis = new LogarithmicAxis(range2Axis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			formatYAxis(scope, logAxis, true);
			plot.setRangeAxis(1, logAxis);
			range2Axis = logAxis;
		}

		Color tc = properties.getTickColor() != null ? IColor.toAWTColor(properties.getTickColor()) : null;
		configureXAxis(scope, domainAxis, tc);
		configureYAxis(scope, rangeAxis, tc);
		if (secondaxis) { configureY2Axis(scope, range2Axis, tc); }

		if ("none".equals(properties.getSeriesLabelPosition()) && this.chart.getLegend() != null) {
			this.chart.getLegend().setVisible(false);
		}
	}

	@Override
	public void setSerieMarkerShape(final IScope scope, final String serieid, final String markershape) {
		if (markershape == null) return;
		final AbstractXYItemRenderer newr = (AbstractXYItemRenderer) this.getOrCreateRenderer(scope, serieid);
		int seriesIndex = idPosition.getOrDefault(serieid, 0);
		Shape baseShape = ChartDataStatement.MARKER_EMPTY.equals(markershape) ? null : switch (markershape) {
			case ChartDataStatement.MARKER_CIRCLE -> defaultmarkers[1];
			case ChartDataStatement.MARKER_UP_TRIANGLE -> defaultmarkers[2];
			case ChartDataStatement.MARKER_DIAMOND -> defaultmarkers[3];
			case ChartDataStatement.MARKER_HOR_RECTANGLE -> defaultmarkers[4];
			case ChartDataStatement.MARKER_DOWN_TRIANGLE -> defaultmarkers[5];
			case ChartDataStatement.MARKER_HOR_ELLIPSE -> defaultmarkers[6];
			case ChartDataStatement.MARKER_RIGHT_TRIANGLE -> defaultmarkers[7];
			case ChartDataStatement.MARKER_VERT_RECTANGLE -> defaultmarkers[8];
			case ChartDataStatement.MARKER_LEFT_TRIANGLE -> defaultmarkers[9];
			case "default" -> defaultmarkers[seriesIndex % defaultmarkers.length];
			default -> defaultmarkers[0];
		};

		Shape myshape = baseShape;
		if (myshape != null && getChartdataset() != null) {
			ChartDataSeries myserie = getChartdataset().getDataSeries(scope, serieid);
			if (myserie != null && !myserie.getMysource().isUseSize()) {
				float thickness = Cast.asFloat(scope, myserie.getLineThickness().value(scope)).floatValue();
				if (thickness > 1.0f) {
					double scaleFactor = thickness * 1.25;
					AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
					myshape = at.createTransformedShape(baseShape);
				}
			}
		}

		if (newr instanceof XYLineAndShapeRenderer serierenderer) {
			if (myshape == null) {
				serierenderer.setSeriesShapesVisible(0, false);
			} else {
				serierenderer.setSeriesShape(0, myshape);
			}
		} else if (newr instanceof XYShapeRenderer serierenderer) {
			if (myshape != null) {
				serierenderer.setSeriesShape(0, myshape);
			}
		}
	}

	@Override
	public void setUseSize(final IScope scope, final String name, final boolean b) {
		final AbstractXYItemRenderer newr = (AbstractXYItemRenderer) this.getOrCreateRenderer(scope, name);
		if (newr instanceof CustomXYErrorRenderer xy) { xy.setUseSize(scope, b); }
	}

	@Override
	protected void initRenderer(final IScope scope) {
		if (chart == null) return;
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		defaultrenderer = new CustomXYErrorRenderer();
		plot.setRenderer((CustomXYErrorRenderer) defaultrenderer);
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
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);
		if (chart == null) return;
		Color ac = IColor.toAWTColor(properties.getAxesColor());
		final XYPlot pp = (XYPlot) chart.getPlot();
		pp.setDomainGridlinePaint(ac);
		pp.setRangeGridlinePaint(ac);
		pp.setDomainCrosshairPaint(ac);
		pp.setRangeCrosshairPaint(ac);
		pp.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		pp.setDomainCrosshairVisible(false);
		pp.setRangeCrosshairVisible(false);

		pp.getDomainAxis().setAxisLinePaint(ac);
		pp.getDomainAxis().setTickLabelFont(properties.getTickFont());
		pp.getDomainAxis().setLabelFont(properties.getLabelFont());
		if (properties.getTextColor() != null) {
			Color tc = IColor.toAWTColor(properties.getTextColor());
			pp.getDomainAxis().setLabelPaint(tc);
			pp.getDomainAxis().setTickLabelPaint(tc);
		}

		NumberAxis axis = (NumberAxis) pp.getRangeAxis();
		axis = formatYAxis(scope, axis);
		pp.setRangeAxis(axis);
		if (properties.getYTickUnit() > 0) {
			((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(properties.getYTickUnit()));
			pp.setRangeGridlinesVisible(true);
		} else {
			pp.setRangeGridlinesVisible(properties.isGridLinesVisible());
		}

		if (type == ChartOutput.SERIES_CHART && properties.getXLabel() == null) { properties.setXLabel("time"); }
		if (!properties.isXTickValueVisible()) {
			pp.getDomainAxis().setTickMarksVisible(false);
			pp.getDomainAxis().setTickLabelsVisible(false);
		}
	}


}
