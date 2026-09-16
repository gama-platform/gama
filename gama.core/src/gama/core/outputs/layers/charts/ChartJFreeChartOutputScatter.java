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
		if (markerScale.containsKey(serie) && col >= 0 && col < markerScale.get(serie).size()) {
			return markerScale.get(serie).get(col);
		}
		return 1;
	}

	HashMap<String, ArrayList<Double>> markerScale = new HashMap<>();

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

		newr.setSeriesStroke(0, new BasicStroke(Cast.asFloat(scope, myserie.getLineThickness().value(scope)).floatValue()));

		if (newr instanceof CustomXYErrorRenderer xy) {
			configureErrorRenderer(xy, myserie, scope);
		}

		if (myserie.getMysource().getUniqueMarkerName() != null) {
			setSerieMarkerShape(scope, myserie.getName(), myserie.getMysource().getUniqueMarkerName());
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

	@SuppressWarnings ("unchecked")
	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		if (chart == null) return;
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		if (dataserie == null || !idPosition.containsKey(dataserie.getSerieId(scope))) return;

		final XYIntervalSeries serie = ((XYIntervalSeriesCollection) jfreedataset.get(idPosition.get(dataserie.getSerieId(scope)))).getSeries(0);
		serie.clear();
		final ArrayList<Double> xValues = dataserie.getXValues(scope);
		final ArrayList<Double> yValues = dataserie.getYValues(scope);
		final ArrayList<Double> sValues = dataserie.getSValues(scope);
		boolean secondaxis = false;
		if (dataserie.getMysource().getUseSecondYAxis(scope)) {
			secondaxis = true;
			this.setUseSecondYAxis(scope, true);
		}

		if (!xValues.isEmpty()) {
			final NumberAxis domainAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getDomainAxis();
			final NumberAxis rangeAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis(0);
			final int ids = idPosition.get(dataserie.getSerieId(scope));
			if (secondaxis) {
				((XYPlot) this.chart.getPlot()).mapDatasetToRangeAxis(ids, 1);
			} else {
				((XYPlot) this.chart.getPlot()).mapDatasetToRangeAxis(ids, 0);
			}
			domainAxis.setAutoRange(false);
			rangeAxis.setAutoRange(false);
			XYIntervalDataItem newval;
			for (int i = 0; i < xValues.size(); i++) {
				if (dataserie.isUseYErrValues()) {
					if (dataserie.isUseXErrValues()) {
						newval = new XYIntervalDataItem(xValues.get(i), dataserie.xerrvaluesmin.get(i),
								dataserie.xerrvaluesmax.get(i), yValues.get(i), dataserie.yerrvaluesmin.get(i),
								dataserie.yerrvaluesmax.get(i));
					} else {
						newval = new XYIntervalDataItem(xValues.get(i), xValues.get(i), xValues.get(i), yValues.get(i),
								dataserie.yerrvaluesmin.get(i), dataserie.yerrvaluesmax.get(i));
					}
				} else if (dataserie.isUseXErrValues()) {
					newval = new XYIntervalDataItem(xValues.get(i), dataserie.xerrvaluesmin.get(i),
							dataserie.xerrvaluesmax.get(i), yValues.get(i), yValues.get(i), yValues.get(i));
				} else {
					newval = new XYIntervalDataItem(xValues.get(i), xValues.get(i), xValues.get(i), yValues.get(i),
							yValues.get(i), yValues.get(i));
				}
				serie.add(newval, false);
			}
		}
		if (!sValues.isEmpty()) {
			markerScale.remove(serieid);
			final ArrayList<Double> nscale = (ArrayList<Double>) sValues.clone();
			markerScale.put(serieid, nscale);
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
			if (max - min > 0) {
				domainAxis.setRange(min, max);
			} else {
				domainAxis.setAutoRange(true);
			}
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
			if (max - min > 0) {
				rangeAxis.setRange(min, max);
			} else {
				rangeAxis.setAutoRange(true);
			}
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
			if (max - min > 0) {
				range2Axis.setRange(min, max);
			} else {
				range2Axis.setAutoRange(true);
			}
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
		boolean secondaxis = false;
		if (properties.isUseSecondYAxis()) {
			secondaxis = true;
			range2Axis = (NumberAxis) plot.getRangeAxis(1);
			if (range2Axis == null) {
				final NumberAxis secondAxis = new NumberAxis("");
				plot.setRangeAxis(1, secondAxis);
				range2Axis = (NumberAxis) plot.getRangeAxis(1);
				range2Axis = formatYAxis(scope, range2Axis, true);
				plot.setRangeAxis(1, range2Axis);
			} else {
				formatYAxis(scope, range2Axis, true);
			}
		}

		if (properties.isXLogscale()) {
			final LogarithmicAxis logAxis = new LogarithmicAxis(domainAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			plot.setDomainAxis(logAxis);
			domainAxis = logAxis;
		}
		if (properties.isYLogscale()) {
			LogarithmicAxis logAxis = new LogarithmicAxis(rangeAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			logAxis = (LogarithmicAxis) formatYAxis(scope, logAxis, false);
			plot.setRangeAxis(logAxis);
			rangeAxis = logAxis;
		} else {
			formatYAxis(scope, rangeAxis, false);
		}
		if (secondaxis && properties.isY2Logscale()) {
			LogarithmicAxis logAxis = new LogarithmicAxis(range2Axis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			logAxis = (LogarithmicAxis) formatYAxis(scope, logAxis, true);
			plot.setRangeAxis(1, logAxis);
			range2Axis = logAxis;
		}

		Color tc = properties.getTickColor() != null ? IColor.toAWTColor(properties.getTickColor()) : null;
		configureXAxis(scope, domainAxis, tc);
		configureYAxis(scope, rangeAxis, tc);
		if (secondaxis) {
			configureY2Axis(scope, range2Axis, tc);
		}

		if ("none".equals(properties.getSeriesLabelPosition()) && this.chart.getLegend() != null) {
			this.chart.getLegend().setVisible(false);
		}
	}

	@Override
	public void setSerieMarkerShape(final IScope scope, final String serieid, final String markershape) {
		final AbstractXYItemRenderer newr = (AbstractXYItemRenderer) this.getOrCreateRenderer(scope, serieid);
		if (newr instanceof XYLineAndShapeRenderer serierenderer) {
			if (markershape != null) {
				if (ChartDataStatement.MARKER_EMPTY.equals(markershape)) {
					serierenderer.setSeriesShapesVisible(0, false);
				} else {
					Shape myshape = switch (markershape) {
						case ChartDataStatement.MARKER_CIRCLE -> defaultmarkers[1];
						case ChartDataStatement.MARKER_UP_TRIANGLE -> defaultmarkers[2];
						case ChartDataStatement.MARKER_DIAMOND -> defaultmarkers[3];
						case ChartDataStatement.MARKER_HOR_RECTANGLE -> defaultmarkers[4];
						case ChartDataStatement.MARKER_DOWN_TRIANGLE -> defaultmarkers[5];
						case ChartDataStatement.MARKER_HOR_ELLIPSE -> defaultmarkers[6];
						case ChartDataStatement.MARKER_RIGHT_TRIANGLE -> defaultmarkers[7];
						case ChartDataStatement.MARKER_VERT_RECTANGLE -> defaultmarkers[8];
						case ChartDataStatement.MARKER_LEFT_TRIANGLE -> defaultmarkers[9];
						default -> defaultmarkers[0];
					};
					serierenderer.setSeriesShape(0, myshape);
				}
			}
		} else if (newr instanceof XYShapeRenderer serierenderer) {
			if (markershape != null) {
				if (ChartDataStatement.MARKER_EMPTY.equals(markershape)) {
					serierenderer.setSeriesShape(0, null);
				} else {
					Shape myshape = switch (markershape) {
						case ChartDataStatement.MARKER_CIRCLE -> defaultmarkers[1];
						case ChartDataStatement.MARKER_UP_TRIANGLE -> defaultmarkers[2];
						case ChartDataStatement.MARKER_DIAMOND -> defaultmarkers[3];
						case ChartDataStatement.MARKER_HOR_RECTANGLE -> defaultmarkers[4];
						case ChartDataStatement.MARKER_DOWN_TRIANGLE -> defaultmarkers[5];
						case ChartDataStatement.MARKER_HOR_ELLIPSE -> defaultmarkers[6];
						case ChartDataStatement.MARKER_RIGHT_TRIANGLE -> defaultmarkers[7];
						case ChartDataStatement.MARKER_VERT_RECTANGLE -> defaultmarkers[8];
						case ChartDataStatement.MARKER_LEFT_TRIANGLE -> defaultmarkers[9];
						default -> defaultmarkers[0];
					};
					serierenderer.setSeriesShape(0, myshape);
				}
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

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {
		final int x = xOnScreen - positionInPixels.x;
		final int y = yOnScreen - positionInPixels.y;
		final ChartEntity entity = info.getEntityCollection().getEntity(x, y);
		switch (entity) {
			case XYItemEntity xy -> {
				final XYDataset data = xy.getDataset();
				final int index = xy.getItem();
				final int series = xy.getSeriesIndex();
				final double xx = data.getXValue(series, index);
				final double yy = data.getYValue(series, index);
				final XYPlot plot = (XYPlot) getJFChart().getPlot();
				final ValueAxis xAxis = plot.getDomainAxis(series);
				final ValueAxis yAxis = plot.getRangeAxis(series);
				final boolean xInt = xx % 1 == 0;
				final boolean yInt = yy % 1 == 0;
				String xTitle = xAxis.getLabel();
				if (StringUtils.isBlank(xTitle)) { xTitle = "X"; }
				String yTitle = yAxis.getLabel();
				if (StringUtils.isBlank(yTitle)) { yTitle = "Y"; }
				sb.append(xTitle).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
				sb.append(" | ").append(yTitle).append(" ").append(yInt ? (int) yy : String.format("%.2f", yy));
			}
			case PieSectionEntity ps -> {
				final String title = ps.getSectionKey().toString();
				final PieDataset<?> data = ps.getDataset();
				final int index = ps.getSectionIndex();
				final double xx = data.getValue(index).doubleValue();
				final boolean xInt = xx % 1 == 0;
				sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
			}
			case CategoryItemEntity ci -> {
				final Comparable<?> columnKey = ci.getColumnKey();
				final String title = columnKey.toString();
				final CategoryDataset data = ci.getDataset();
				final Comparable<?> rowKey = ci.getRowKey();
				final double xx = data.getValue(rowKey, columnKey).doubleValue();
				final boolean xInt = xx % 1 == 0;
				sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
			}
			case null, default -> {}
		}
	}

}
