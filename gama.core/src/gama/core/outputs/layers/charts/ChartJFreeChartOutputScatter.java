/*******************************************************************************************************
 *
 * ChartJFreeChartOutputScatter.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.IScope;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;

/**
 * The Class ChartJFreeChartOutputScatter.
 */
public class ChartJFreeChartOutputScatter extends ChartJFreeChartOutput {

	static {
		// DEBUG.ON();
	}

	/**
	 * The Class myXYErrorRenderer.
	 */
	public class CustomXYErrorRenderer extends XYErrorRenderer {

		/** The myoutput. */
		ChartJFreeChartOutputScatter myoutput;

		/** The myid. */
		String myid;

		/** The use size. */
		boolean useSize;

		/** The transform. */
		AffineTransform transform = new AffineTransform();

		/**
		 * Checks if is use size.
		 *
		 * @return true, if is use size
		 */
		public boolean isUseSize() { return useSize; }

		/**
		 * Sets the use size.
		 *
		 * @param scope
		 *            the scope
		 * @param useSize
		 *            the use size
		 */
		public void setUseSize(final IScope scope, final boolean useSize) {
			this.useSize = useSize;

		}

		/**
		 * Sets the myid.
		 *
		 * @param myid
		 *            the new myid
		 */
		public void setMyid(final String myid) { this.myid = myid; }

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/**
		 * Sets the output.
		 *
		 * @param output
		 *            the new output
		 */
		public void setOutput(final ChartJFreeChartOutput output) {
			myoutput = (ChartJFreeChartOutputScatter) output;
		}

		@Override
		public Shape getItemShape(final int row, final int col) {
			if (isUseSize()) {
				transform.setToScale(myoutput.getScale(myid, col), myoutput.getScale(myid, col));
				return transform.createTransformedShape(super.getItemShape(row, col));
			}
			return super.getItemShape(row, col);
		}
	}

	/**
	 * Gets the scale.
	 *
	 * @param serie
	 *            the serie
	 * @param col
	 *            the col
	 * @return the scale
	 */
	double getScale(final String serie, final int col) {
		if (markerScale.containsKey(serie)) return markerScale.get(serie).get(col);
		return 1;
	}

	/** The Marker scale. */
	HashMap<String, ArrayList<Double>> markerScale = new HashMap<>();

	/**
	 * Instantiates a new chart J free chart output scatter.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param typeexp
	 *            the typeexp
	 */
	public ChartJFreeChartOutputScatter(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);

		jfreedataset.add(0, new XYIntervalSeriesCollection());
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		if (reverse_axes) { orientation = PlotOrientation.HORIZONTAL; }

		switch (type) {
			case SERIES_CHART, XY_CHART, SCATTER_CHART:
				chart = ChartFactory.createXYLineChart(getName(), "", "",
						(XYIntervalSeriesCollection) jfreedataset.get(0), orientation, true, false, false);
				break;
			case BOX_WHISKER_CHART: {
				chart = ChartFactory.createBoxAndWhiskerChart(getName(), "Time", "Value",
						(BoxAndWhiskerCategoryDataset) jfreedataset.get(0), true);
				chart.setBackgroundPaint(new Color(249, 231, 236));

				break;
			}
			default:

		}

	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final ChartDataSource source, final int type_val) {

		switch (type_val) {
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_N, ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_N, ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_12, ChartDataSource.DATA_TYPE_LIST_POINT, ChartDataSource.DATA_TYPE_MATRIX_DOUBLE: {
				source.setCumulative(scope, false);
				source.setUseSize(scope, false);
				break;
			}
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_3: {
				source.setCumulative(scope, true);
				source.setUseSize(scope, true);
				break;

			}
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_3: {
				source.setCumulative(scope, false);
				source.setUseSize(scope, true);
				break;

			}
			default: {
				source.setCumulative(scope, true);
				source.setUseSize(scope, false);
			}
		}

	}

	@Override
	public void initdataset() {
		super.initdataset();
		switch (type) {
			case ChartOutput.SERIES_CHART:
				chartdataset.setCommonXSeries(true);
				chartdataset.setByCategory(false);
				break;
			case ChartOutput.XY_CHART, ChartOutput.SCATTER_CHART:
				chartdataset.setCommonXSeries(false);
				chartdataset.setByCategory(false);
				break;
			default:
		}
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		final String theStyle = this.getChartdataset().getDataSeries(scope, serieid).getStyle(scope);
		AbstractRenderer newr;
		switch (theStyle) {
			case IKeyword.SPLINE: {
				newr = new XYSplineRenderer();
				break;
			}
			case IKeyword.STEP: {
				newr = new XYStepRenderer();
				break;
			}
			case IKeyword.DOT: {
				newr = new XYShapeRenderer();
				break;
			}
			case IKeyword.WHISKER: {
				newr = new XYBoxAndWhiskerRenderer();
				break;
			}
			case IKeyword.AREA: {
				newr = new XYAreaRenderer();
				break;
			}
			case IKeyword.BAR: {
				newr = new XYBarRenderer();
				break;
			}
			case IKeyword.THREE_D: {
				newr = new XYLineAndShapeRenderer();
				break;
			}
			case IKeyword.STACK, IKeyword.RING, IKeyword.EXPLODED:
			default: {
				// newr = new FastXYItemRenderer();
				newr = new CustomXYErrorRenderer();
				((CustomXYErrorRenderer) newr).setMyid(serieid);
				((CustomXYErrorRenderer) newr).setOutput(this);
				break;

			}
		}
		return newr;
	}

	/**
	 * Reset renderer.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 */
	protected void resetRenderer(final IScope scope, final String serieid) {
		final AbstractXYItemRenderer newr = (AbstractXYItemRenderer) this.getOrCreateRenderer(scope, serieid);
		// newr.setSeriesStroke(0, new BasicStroke(0));
		newr.setDefaultCreateEntities(true);
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);

		if (newr instanceof XYLineAndShapeRenderer xy) {
			xy.setSeriesLinesVisible(0, myserie.getMysource().showLine);
			xy.setSeriesShapesFilled(0, myserie.getMysource().fillMarker);
			xy.setSeriesShapesVisible(0, myserie.getMysource().useMarker);

		}

		if (newr instanceof XYShapeRenderer xy && !myserie.getMysource().fillMarker) {
			xy.setUseFillPaint(false);
			// ((XYShapeRenderer) newr).setDrawOutlines(true);
		}
		if (myserie.getMycolor() != null) { newr.setSeriesPaint(0, myserie.getMycolor()); }
		// DEBUG.OUT("Changing series stroke to " + myserie.getLineThickness().value(scope));
		newr.setSeriesStroke(0,
				new BasicStroke(Cast.asFloat(scope, myserie.getLineThickness().value(scope)).floatValue()));

		if (newr instanceof CustomXYErrorRenderer xy) {
			xy.setDrawYError(false);
			xy.setDrawXError(false);
			if (myserie.isUseYErrValues()) { xy.setDrawYError(true); }
			if (myserie.isUseXErrValues()) { xy.setDrawXError(true); }
			if (myserie.getMysource().isUseSize()) { xy.setUseSize(scope, true); }
		}

		if (myserie.getMysource().getUniqueMarkerName() != null) {
			setSerieMarkerShape(scope, myserie.getName(), myserie.getMysource().getUniqueMarkerName());
		}

	}

	@Override
	protected void clearDataSet(final IScope scope) {

		super.clearDataSet(scope);
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

		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		final XYIntervalSeries serie = new XYIntervalSeries(dataserie.getSerieLegend(scope), false, true);
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
			// DEBUG.LOG("new serie"+serieid+" at
			// "+IdPosition.get(serieid)+" fdsize "+plot.getSeriesCount()+" jfds
			// "+jfreedataset.size()+" datasc "+plot.getDatasetCount());

		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	protected void resetSerie(final IScope scope, final String serieid) {

		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		final XYIntervalSeries serie =
				((XYIntervalSeriesCollection) jfreedataset.get(idPosition.get(dataserie.getSerieId(scope))))
						.getSeries(0);
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
				// rangeAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis(1);
				// ((XYPlot) this.chart.getPlot()).setRangeAxis(IdPosition.get(dataserie.getSerieId(scope)),rangeAxis);
				// ((XYPlot) this.chart.getPlot()).setRangeAxis(IdPosition.get(dataserie.getSerieId(scope)),rangeAxis);
				((XYPlot) this.chart.getPlot()).mapDatasetToRangeAxis(ids, 1);
			} else {
				// ((XYPlot) this.chart.getPlot()).setRangeAxis(IdPosition.get(dataserie.getSerieId(scope)),rangeAxis);
				((XYPlot) this.chart.getPlot()).mapDatasetToRangeAxis(ids, 0);

			}
			domainAxis.setAutoRange(false);
			rangeAxis.setAutoRange(false);
			// domainAxis.setRange(Math.min((double)(Collections.min(XValues)),0),
			// Math.max(Collections.max(XValues),Collections.min(XValues)+1));
			// rangeAxis.setRange(Math.min((double)(Collections.min(YValues)),0),
			// Math.max(Collections.max(YValues),Collections.min(YValues)+1));
			XYIntervalDataItem newval;
			for (int i = 0; i < xValues.size(); i++) {
				if (dataserie.isUseYErrValues()) {
					if (dataserie.isUseXErrValues()) {
						newval = new XYIntervalDataItem(xValues.get(i), dataserie.xerrvaluesmin.get(i),
								dataserie.xerrvaluesmax.get(i), yValues.get(i), dataserie.yerrvaluesmin.get(i),
								dataserie.yerrvaluesmax.get(i));
						// serie.add(XValues.get(i),dataserie.xerrvaluesmin.get(i),dataserie.xerrvaluesmax.get(i),YValues.get(i),dataserie.yerrvaluesmin.get(i),dataserie.yerrvaluesmax.get(i));
					} else {
						newval = new XYIntervalDataItem(xValues.get(i), xValues.get(i), xValues.get(i), yValues.get(i),
								dataserie.yerrvaluesmin.get(i), dataserie.yerrvaluesmax.get(i));
						// serie.add(XValues.get(i),XValues.get(i),XValues.get(i),YValues.get(i),dataserie.yerrvaluesmin.get(i),dataserie.yerrvaluesmax.get(i));
					}

				} else if (dataserie.isUseXErrValues()) {
					newval = new XYIntervalDataItem(xValues.get(i), dataserie.xerrvaluesmin.get(i),
							dataserie.xerrvaluesmax.get(i), yValues.get(i), yValues.get(i), yValues.get(i));
					// serie.add(XValues.get(i),dataserie.xerrvaluesmin.get(i),dataserie.xerrvaluesmax.get(i),YValues.get(i),YValues.get(i),YValues.get(i));
				} else {
					newval = new XYIntervalDataItem(xValues.get(i), xValues.get(i), xValues.get(i), yValues.get(i),
							yValues.get(i), yValues.get(i));
					// serie.add(XValues.get(i),XValues.get(i),XValues.get(i),YValues.get(i),YValues.get(i),YValues.get(i));
				}
				serie.add(newval, false);
			}
			// domainAxis.setAutoRange(true);
			// rangeAxis.setAutoRange(true);
		}
		// resetAutorange(scope);
		if (!sValues.isEmpty()) {
			markerScale.remove(serieid);
			final ArrayList<Double> nscale = (ArrayList<Double>) sValues.clone();
			markerScale.put(serieid, nscale);

		}

		this.resetRenderer(scope, serieid);

	}

	/**
	 * Format Y axis.
	 *
	 * @param scope
	 *            the scope
	 * @param axis
	 *            the axis
	 * @return the number axis
	 */
	public NumberAxis formatYAxis(final IScope scope, final NumberAxis axis) {
		axis.setAxisLinePaint(axesColor);
		axis.setTickLabelFont(getTickFont());
		axis.setLabelFont(getLabelFont());
		if (textColor != null) {
			axis.setLabelPaint(textColor);
			axis.setTickLabelPaint(textColor);
		}
		axis.setAxisLinePaint(axesColor);
		axis.setLabelFont(getLabelFont());
		axis.setTickLabelFont(getTickFont());
		if (textColor != null) {
			axis.setLabelPaint(textColor);
			axis.setTickLabelPaint(textColor);
		}
		if (!this.getYTickValueVisible(scope)) {
			axis.setTickMarksVisible(false);
			axis.setTickLabelsVisible(false);

		}
		return axis;

	}

	@Override
	public void resetAxes(final IScope scope) {
		NumberAxis domainAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getDomainAxis();
		NumberAxis rangeAxis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis();
		NumberAxis range2Axis = rangeAxis;
		boolean secondaxis = false;
		if (getUseSecondYAxis(scope)) {
			secondaxis = true;
			range2Axis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis(1);
			if (range2Axis == null) {
				final NumberAxis secondAxis = new NumberAxis("");
				((XYPlot) this.chart.getPlot()).setRangeAxis(1, secondAxis);
				range2Axis = (NumberAxis) ((XYPlot) this.chart.getPlot()).getRangeAxis(1);
				range2Axis = formatYAxis(scope, range2Axis);

				((XYPlot) this.chart.getPlot()).setRangeAxis(1, range2Axis);
			}
		}

		if (getX_LogScale(scope)) {
			final LogarithmicAxis logAxis = new LogarithmicAxis(domainAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			((XYPlot) this.chart.getPlot()).setDomainAxis(logAxis);
			domainAxis = logAxis;
		}
		if (getY_LogScale(scope)) {
			LogarithmicAxis logAxis = new LogarithmicAxis(rangeAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			logAxis = (LogarithmicAxis) formatYAxis(scope, logAxis);
			((XYPlot) this.chart.getPlot()).setRangeAxis(logAxis);
			rangeAxis = logAxis;
		}
		if (secondaxis && getY2_LogScale(scope)) {
			LogarithmicAxis logAxis = new LogarithmicAxis(range2Axis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			logAxis = (LogarithmicAxis) formatYAxis(scope, logAxis);
			((XYPlot) this.chart.getPlot()).setRangeAxis(1, logAxis);
			range2Axis = logAxis;
		}

		if (!getUseXRangeInterval(scope) && !getUseXRangeMinMax(scope)) { domainAxis.setAutoRange(true); }

		if (this.getUseXRangeInterval(scope)) {
			domainAxis.setFixedAutoRange(getXRangeInterval(scope));
			domainAxis.setAutoRangeMinimumSize(getXRangeInterval(scope));
			domainAxis.setAutoRange(true);

		}
		if (this.getUseXRangeMinMax(scope)) {
			domainAxis.setRange(getXRangeMin(scope), getXRangeMax(scope));

		}
		if (this.getXTickLineVisible(scope)) {
			((XYPlot) this.chart.getPlot()).setDomainGridlinePaint(this.tickColor);
			if (getXTickUnit(scope) > 0) {
				domainAxis.setTickUnit(new NumberTickUnit(getXTickUnit(scope)));
				((XYPlot) this.chart.getPlot()).setDomainGridlinesVisible(true);
			} else {
				((XYPlot) this.chart.getPlot())
						.setDomainGridlinesVisible(GamaPreferences.Displays.CHART_GRIDLINES.getValue());
			}

		} else {
			((XYPlot) this.chart.getPlot()).setDomainGridlinesVisible(false);

		}

		if (!getUseYRangeInterval(scope) && !getUseYRangeMinMax(scope)) { rangeAxis.setAutoRange(true); }

		if (this.getUseYRangeInterval(scope)) {
			rangeAxis.setFixedAutoRange(getYRangeInterval(scope));
			rangeAxis.setAutoRangeMinimumSize(getYRangeInterval(scope));
			rangeAxis.setAutoRange(true);
		}
		if (this.getUseYRangeMinMax(scope)) {
			rangeAxis.setRange(getYRangeMin(scope), getYRangeMax(scope));

		}
		if (this.getYTickLineVisible(scope)) {
			((XYPlot) this.chart.getPlot()).setRangeGridlinePaint(this.tickColor);
			if (getYTickUnit(scope) > 0) {
				rangeAxis.setTickUnit(new NumberTickUnit(getYTickUnit(scope)));
				((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(true);
			} else {
				((XYPlot) this.chart.getPlot())
						.setRangeGridlinesVisible(GamaPreferences.Displays.CHART_GRIDLINES.getValue());
			}

		} else {
			((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(false);

		}

		if (secondaxis) {
			if (!getUseY2RangeInterval(scope) && !getUseY2RangeMinMax(scope)) { range2Axis.setAutoRange(true); }

			if (this.getUseY2RangeInterval(scope)) {
				range2Axis.setFixedAutoRange(getY2RangeInterval(scope));
				range2Axis.setAutoRangeMinimumSize(getY2RangeInterval(scope));
				range2Axis.setAutoRange(true);
			}
			if (this.getUseY2RangeMinMax(scope)) {
				range2Axis.setRange(getY2RangeMin(scope), getY2RangeMax(scope));

			}
			if (this.getYTickLineVisible(scope)) {
				((XYPlot) this.chart.getPlot()).setRangeGridlinePaint(this.tickColor);
				if (getY2TickUnit(scope) > 0) {
					range2Axis.setTickUnit(new NumberTickUnit(getY2TickUnit(scope)));
					((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(true);
				} else {
					((XYPlot) this.chart.getPlot())
							.setRangeGridlinesVisible(GamaPreferences.Displays.CHART_GRIDLINES.getValue());
				}

			} else {
				((XYPlot) this.chart.getPlot()).setRangeGridlinesVisible(false);

			}

		}

		if (getXLabel(scope) != null && !getXLabel(scope).isEmpty()) { domainAxis.setLabel(getXLabel(scope)); }
		if (getYLabel(scope) != null && !getYLabel(scope).isEmpty()) { rangeAxis.setLabel(getYLabel(scope)); }
		if (secondaxis && getY2Label(scope) != null && !getY2Label(scope).isEmpty()) {
			range2Axis.setLabel(getY2Label(scope));
		}
		if ("none".equals(this.series_label_position)) { this.chart.getLegend().setVisible(false); }
		if (!this.getXTickValueVisible(scope)) {
			domainAxis.setTickMarksVisible(false);
			domainAxis.setTickLabelsVisible(false);

		}

	}

	@Override
	public void setSerieMarkerShape(final IScope scope, final String serieid, final String markershape) {
		final AbstractXYItemRenderer newr = (AbstractXYItemRenderer) this.getOrCreateRenderer(scope, serieid);
		if (newr instanceof XYLineAndShapeRenderer) {
			final XYLineAndShapeRenderer serierenderer = (XYLineAndShapeRenderer) getOrCreateRenderer(scope, serieid);
			if (markershape != null) {
				if (ChartDataStatement.MARKER_EMPTY.equals(markershape)) {
					serierenderer.setSeriesShapesVisible(0, false);
				} else {
					Shape myshape = defaultmarkers[0];
					if (ChartDataStatement.MARKER_CIRCLE.equals(markershape)) {
						myshape = defaultmarkers[1];
					} else if (ChartDataStatement.MARKER_UP_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[2];
					} else if (ChartDataStatement.MARKER_DIAMOND.equals(markershape)) {
						myshape = defaultmarkers[3];
					} else if (ChartDataStatement.MARKER_HOR_RECTANGLE.equals(markershape)) {
						myshape = defaultmarkers[4];
					} else if (ChartDataStatement.MARKER_DOWN_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[5];
					} else if (ChartDataStatement.MARKER_HOR_ELLIPSE.equals(markershape)) {
						myshape = defaultmarkers[6];
					} else if (ChartDataStatement.MARKER_RIGHT_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[7];
					} else if (ChartDataStatement.MARKER_VERT_RECTANGLE.equals(markershape)) {
						myshape = defaultmarkers[8];
					} else if (ChartDataStatement.MARKER_LEFT_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[9];
					}
					serierenderer.setSeriesShape(0, myshape);

				}
			}

		} else if (newr instanceof XYShapeRenderer) {
			final XYShapeRenderer serierenderer = (XYShapeRenderer) getOrCreateRenderer(scope, serieid);
			if (markershape != null) {
				if (ChartDataStatement.MARKER_EMPTY.equals(markershape)) {
					serierenderer.setSeriesShape(0, null);
				} else {
					Shape myshape = defaultmarkers[0];
					if (ChartDataStatement.MARKER_CIRCLE.equals(markershape)) {
						myshape = defaultmarkers[1];
					} else if (ChartDataStatement.MARKER_UP_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[2];
					} else if (ChartDataStatement.MARKER_DIAMOND.equals(markershape)) {
						myshape = defaultmarkers[3];
					} else if (ChartDataStatement.MARKER_HOR_RECTANGLE.equals(markershape)) {
						myshape = defaultmarkers[4];
					} else if (ChartDataStatement.MARKER_DOWN_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[5];
					} else if (ChartDataStatement.MARKER_HOR_ELLIPSE.equals(markershape)) {
						myshape = defaultmarkers[6];
					} else if (ChartDataStatement.MARKER_RIGHT_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[7];
					} else if (ChartDataStatement.MARKER_VERT_RECTANGLE.equals(markershape)) {
						myshape = defaultmarkers[8];
					} else if (ChartDataStatement.MARKER_LEFT_TRIANGLE.equals(markershape)) {
						myshape = defaultmarkers[9];
					}
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
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		defaultrenderer = new CustomXYErrorRenderer();
		plot.setRenderer((CustomXYErrorRenderer) defaultrenderer);

	}

	@Override
	public void setUseXSource(final IScope scope, final IExpression expval) {
		// if there is something to do to use custom X axis

	}

	@Override
	public void setUseXLabels(final IScope scope, final IExpression expval) {
		// if there is something to do to use custom X axis
		final XYPlot pp = (XYPlot) chart.getPlot();

		((NumberAxis) pp.getDomainAxis()).setNumberFormatOverride(new NumberFormat() {

			@Override
			public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
				final int ind = chartdataset.XSeriesValues.indexOf(number);
				if (ind >= 0) return new StringBuffer("" + chartdataset.Xcategories.get(ind));
				return new StringBuffer("");

			}

			@Override
			public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
				return new StringBuffer("n" + number);
				// return new StringBuffer(String.format("%s", number));
			}

			@Override
			public Number parse(final String source, final ParsePosition parsePosition) {
				return null;
			}
		});

	}

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);

		final XYPlot pp = (XYPlot) chart.getPlot();
		pp.setDomainGridlinePaint(axesColor);
		pp.setRangeGridlinePaint(axesColor);
		pp.setDomainCrosshairPaint(axesColor);
		pp.setRangeCrosshairPaint(axesColor);
		pp.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		pp.setDomainCrosshairVisible(false);
		pp.setRangeCrosshairVisible(false);

		pp.getDomainAxis().setAxisLinePaint(axesColor);
		pp.getDomainAxis().setTickLabelFont(getTickFont());
		pp.getDomainAxis().setLabelFont(getLabelFont());
		if (textColor != null) {
			pp.getDomainAxis().setLabelPaint(textColor);
			pp.getDomainAxis().setTickLabelPaint(textColor);
		}

		NumberAxis axis = (NumberAxis) pp.getRangeAxis();
		axis = formatYAxis(scope, axis);
		pp.setRangeAxis(axis);
		if (ytickunit > 0) {
			((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(ytickunit));
			pp.setRangeGridlinesVisible(true);
		} else {
			pp.setRangeGridlinesVisible(GamaPreferences.Displays.CHART_GRIDLINES.getValue());
		}

		// resetAutorange(scope);

		if (type == ChartOutput.SERIES_CHART && xlabel == null) { xlabel = "time"; }
		if (!this.getXTickValueVisible(scope)) {
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
		// getChart().handleClick(x, y, info);
		if (entity instanceof XYItemEntity xy) {
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
			return;
		}
		if (entity instanceof PieSectionEntity ps) {
			final String title = ps.getSectionKey().toString();
			final PieDataset<?> data = ps.getDataset();
			final int index = ps.getSectionIndex();
			final double xx = data.getValue(index).doubleValue();
			final boolean xInt = xx % 1 == 0;
			sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
		} else if (entity instanceof CategoryItemEntity ci) {
			final Comparable<?> columnKey = ci.getColumnKey();
			final String title = columnKey.toString();
			final CategoryDataset data = ci.getDataset();
			final Comparable<?> rowKey = ci.getRowKey();
			final double xx = data.getValue(rowKey, columnKey).doubleValue();
			final boolean xInt = xx % 1 == 0;
			sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
		}
	}

}
