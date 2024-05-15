/*******************************************************************************************************
 *
 * ChartJFreeChartOutputBoxAndWhiskerCategory.java, in gama.core, is part of the source code of the GAMA modeling
 * and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Point;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;

/**
 * The Class ChartJFreeChartOutputHistogram.
 */
public class ChartJFreeChartOutputBoxAndWhiskerCategory extends ChartJFreeChartOutput {

	/** The Constant XAXIS. */
	private static final String XAXIS = "xaxis";

	/** The use sub axis. */
	boolean useSubAxis = false;

	/** The use main axis label. */
	boolean useMainAxisLabel = true;

	/**
	 * Enable flat look.
	 *
	 * @param flat
	 *            the flat
	 */
	public static void enableFlatLook(final boolean flat) {
		/*
		 * if (flat) { BoxAndWhiskerRenderer.setDefaultBarPainter(new StandardBarPainter());
		 * BoxAndWhiskerRenderer.setDefaultShadowsVisible(false); } else {
		 * BoxAndWhiskerRenderer.setDefaultBarPainter(new GradientBarPainter());
		 * BoxAndWhiskerRenderer.setDefaultShadowsVisible(true); }
		 */
	}

	static {
		enableFlatLook(GamaPreferences.Displays.CHART_FLAT.getValue());
		GamaPreferences.Displays.CHART_FLAT.onChange(ChartJFreeChartOutputBoxAndWhiskerCategory::enableFlatLook);
	}

	/**
	 * Instantiates a new chart J free chart output box and whisker category.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param typeexp
	 *            the typeexp
	 */
	public ChartJFreeChartOutputBoxAndWhiskerCategory(final IScope scope, final String name,
			final IExpression typeexp) {
		super(scope, name, typeexp);
		// TODO Auto-generated constructor stub

	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		jfreedataset.add(0, new DefaultBoxAndWhiskerCategoryDataset());

		chart = ChartFactory.createBoxAndWhiskerChart(getName(), null, null,
				(BoxAndWhiskerCategoryDataset) jfreedataset.get(0), true);

	}

	@Override
	public void initdataset() {
		super.initdataset();
		chartdataset.setCommonXSeries(true);
		chartdataset.setByCategory(true);
	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final ChartDataSource source, final int type_val) {

		source.setUseXErrValues(false);
		source.setUseYErrValues(false);
		source.setisBoxAndWhiskerData(true);
		switch (type_val) {
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_N, ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_N, ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_12, ChartDataSource.DATA_TYPE_LIST_POINT, ChartDataSource.DATA_TYPE_MATRIX_DOUBLE, ChartDataSource.DATA_TYPE_LIST_DOUBLE_3, ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_3: {
				source.setCumulative(scope, false);
				source.setUseSize(scope, false);
				break;

			}
			default: {
				source.setCumulative(scope, false); // never cumulative by default
				source.setUseSize(scope, false);
			}
		}

	}


	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		// final String style = this.getChartdataset().getDataSeries(scope, serieid).getStyle(scope);
		return new BoxAndWhiskerRenderer();
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

		final CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		final BoxAndWhiskerRenderer newr = (BoxAndWhiskerRenderer) plot.getRenderer();

		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (!idPosition.containsKey(serieid)) {
			// DEBUG.LOG("pb!!!");
		} else {
			final int myrow = idPosition.get(serieid);
			if (myserie.getMycolor() != null) { newr.setSeriesPaint(myrow, myserie.getMycolor()); }

		}

	}

	@Override
	protected void clearDataSet(final IScope scope) {

		super.clearDataSet(scope);
		final CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		for (int i = plot.getDatasetCount() - 1; i >= 1; i--) {
			plot.setDataset(i, null);
			plot.setRenderer(i, null);
		}
		// ((BoxAndWhiskerCategoryDataset) jfreedataset.get(0)).clear();
		jfreedataset.clear();
		jfreedataset.add(0, new DefaultBoxAndWhiskerCategoryDataset());
		plot.setDataset((BoxAndWhiskerCategoryDataset) jfreedataset.get(0));
		plot.setRenderer(0, null);
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		if (!idPosition.containsKey(serieid)) {

			final CategoryPlot plot = (CategoryPlot) this.chart.getPlot();

			final BoxAndWhiskerCategoryDataset firstdataset = (BoxAndWhiskerCategoryDataset) plot.getDataset();

			if (nbseries == 0) {
				plot.setDataset(0, firstdataset);
				plot.setRenderer(nbseries, (BoxAndWhiskerRenderer) getOrCreateRenderer(scope, serieid));
			}
			nbseries++;
			idPosition.put(serieid, nbseries - 1);
		}

	}

	@Override
	public void removeSerie(final IScope scope, final String serieid) {

		super.removeSerie(scope, serieid);
		this.clearDataSet(scope);
	}

	@Override
	protected void resetSerie(final IScope scope, final String serieid) {

		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		final DefaultBoxAndWhiskerCategoryDataset serie = (DefaultBoxAndWhiskerCategoryDataset) jfreedataset.get(0);
		if (serie.getRowKeys().contains(serieid)) { serie.removeRow(serieid); }
		// final ArrayList<Double> XValues = dataserie.getXValues(scope);
		final ArrayList<String> cValues = dataserie.getCValues(scope);
		final ArrayList<Double> yValues = dataserie.getYValues(scope);
		final ArrayList<Double> sValues = dataserie.getSValues(scope);
		if (!cValues.isEmpty()) {
			final NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) this.chart.getPlot()).getRangeAxis();
			rangeAxis.setAutoRange(false);
			for (int i = 0; i < cValues.size(); i++) {
				if (getY_LogScale(scope)) {
					final double val = yValues.get(i);
					if (val <= 0) throw GamaRuntimeException.warning("Log scale with <=0 value:" + val, scope);
					serie.add(new BoxAndWhiskerItem(yValues.get(i), sValues.get(i), dataserie.xerrvaluesmin.get(i),
							dataserie.xerrvaluesmax.get(i), dataserie.yerrvaluesmin.get(i),
							dataserie.yerrvaluesmax.get(i), null, null, null), serieid, cValues.get(i));

				} else {
					serie.add(new BoxAndWhiskerItem(yValues.get(i),
							sValues.size() > i ? sValues.get(i) : yValues.get(i),
							dataserie.xerrvaluesmin.size() > i ? dataserie.xerrvaluesmin.get(i) : yValues.get(i),
							dataserie.xerrvaluesmax.size() > i ? dataserie.xerrvaluesmax.get(i) : yValues.get(i),
							dataserie.yerrvaluesmin.size() > i ? dataserie.yerrvaluesmin.get(i) : yValues.get(i),
							dataserie.yerrvaluesmax.size() > i ? dataserie.yerrvaluesmax.get(i) : yValues.get(i), null,
							null, null), serieid, cValues.get(i));

				}
			}
		}

		this.resetRenderer(scope, serieid);

	}

	@Override
	public void resetAxes(final IScope scope) {
		final CategoryPlot pp = (CategoryPlot) this.chart.getPlot();
		NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) this.chart.getPlot()).getRangeAxis();
		if (getY_LogScale(scope)) {
			final LogarithmicAxis logAxis = new LogarithmicAxis(rangeAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			((CategoryPlot) this.chart.getPlot()).setRangeAxis(logAxis);
			rangeAxis = logAxis;
		}

		if (!useyrangeinterval && !useyrangeminmax) { rangeAxis.setAutoRange(true); }

		if (this.useyrangeinterval) {
			rangeAxis.setFixedAutoRange(yrangeinterval);
			rangeAxis.setAutoRangeMinimumSize(yrangeinterval);
			rangeAxis.setAutoRange(true);

		}
		if (this.useyrangeminmax) {
			rangeAxis.setRange(yrangemin, yrangemax);

		}

		resetDomainAxis(scope);

		final CategoryAxis domainAxis = ((CategoryPlot) this.chart.getPlot()).getDomainAxis();

		pp.setDomainGridlinePaint(axesColor);
		pp.setRangeGridlinePaint(axesColor);
		pp.setRangeCrosshairVisible(true);

		pp.getRangeAxis().setAxisLinePaint(axesColor);
		pp.getRangeAxis().setLabelFont(getLabelFont());
		pp.getRangeAxis().setTickLabelFont(getTickFont());
		if (textColor != null) {
			pp.getRangeAxis().setLabelPaint(textColor);
			pp.getRangeAxis().setTickLabelPaint(textColor);
		}
		if (getYTickUnit(scope) > 0) {
			((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(getYTickUnit(scope)));
		}

		if (getYLabel(scope) != null && !getYLabel(scope).isEmpty()) { pp.getRangeAxis().setLabel(getYLabel(scope)); }
		if ("yaxis".equals(this.series_label_position)) {
			pp.getRangeAxis().setLabel(this.getChartdataset().getDataSeriesIds(scope).iterator().next());
			chart.getLegend().setVisible(false);
		}

		if (getXLabel(scope) != null && !getXLabel(scope).isEmpty()) { pp.getDomainAxis().setLabel(getXLabel(scope)); }

		if (this.useSubAxis) {
			for (final String serieid : chartdataset.getDataSeriesIds(scope)) {
				((SubCategoryAxis) domainAxis).addSubCategory(serieid);
			}

		}
		if (!this.getYTickLineVisible(scope)) { pp.setDomainGridlinesVisible(false); }

		if (!this.getYTickLineVisible(scope)) {
			pp.setRangeCrosshairVisible(false);

		}

		if (!this.getYTickValueVisible(scope)) {
			pp.getRangeAxis().setTickMarksVisible(false);
			pp.getRangeAxis().setTickLabelsVisible(false);

		}

	}

	/**
	 * Reset domain axis.
	 *
	 * @param scope
	 *            the scope
	 */
	public void resetDomainAxis(final IScope scope) {

		final CategoryPlot pp = (CategoryPlot) chart.getPlot();
		if (this.useSubAxis) {
			final SubCategoryAxis newAxis = new SubCategoryAxis(pp.getDomainAxis().getLabel());
			pp.setDomainAxis(newAxis);
		}

		pp.getDomainAxis().setAxisLinePaint(axesColor);
		pp.getDomainAxis().setTickLabelFont(getTickFont());
		pp.getDomainAxis().setLabelFont(getLabelFont());
		if (textColor != null) {
			pp.getDomainAxis().setLabelPaint(textColor);
			pp.getDomainAxis().setTickLabelPaint(textColor);
			if (XAXIS.equals(this.series_label_position)) {
				((SubCategoryAxis) pp.getDomainAxis()).setSubLabelPaint(textColor);
			}
		}

		if (gap > 0) {

			pp.getDomainAxis().setCategoryMargin(gap);
			pp.getDomainAxis().setUpperMargin(gap);
			pp.getDomainAxis().setLowerMargin(gap);
		}

		if (this.useSubAxis && !this.useMainAxisLabel) { pp.getDomainAxis().setTickLabelsVisible(false); }
		if (!this.getYTickLineVisible(scope)) { pp.setDomainGridlinesVisible(false); }

		if (!this.getYTickLineVisible(scope)) {
			pp.setRangeCrosshairVisible(false);

		}

		if (!this.getYTickValueVisible(scope)) {
			pp.getRangeAxis().setTickMarksVisible(false);
			pp.getRangeAxis().setTickLabelsVisible(false);

		}
		if (!this.getXTickValueVisible(scope)) {
			pp.getDomainAxis().setTickMarksVisible(false);
			pp.getDomainAxis().setTickLabelsVisible(false);

		}

	}

	@Override
	public void initChart_post_data_init(final IScope scope) {

		super.initChart_post_data_init(scope);
		final CategoryPlot pp = (CategoryPlot) chart.getPlot();

		final String sty = getStyle();
		this.useSubAxis = false;

		if (IKeyword.STACK.equals(sty)) {
			if (XAXIS.equals(this.series_label_position)) { this.series_label_position = IKeyword.DEFAULT; }
			if (IKeyword.DEFAULT.equals(this.series_label_position)) { this.series_label_position = IKeyword.LEGEND; }
		} else if ("default".equals(this.series_label_position)) {
			if (!this.getChartdataset().getSources().isEmpty()) {
				final ChartDataSource onesource = this.getChartdataset().getSources().get(0);
				if (onesource.isCumulative()) {
					this.series_label_position = "legend";
				} else {
					this.series_label_position = XAXIS;
					useMainAxisLabel = false;
				}

			} else {
				this.series_label_position = "legend";

			}
		}

		if (XAXIS.equals(this.series_label_position)) { this.useSubAxis = true; }

		if (!"legend".equals(this.series_label_position)) {
			chart.getLegend().setVisible(false);
			// legend is useless, but I find it nice anyway... Could put back...
		}
		this.resetDomainAxis(scope);

		pp.setDomainGridlinePaint(axesColor);
		pp.setRangeGridlinePaint(axesColor);
		if (!this.getXTickLineVisible(scope)) { pp.setDomainGridlinesVisible(false); }
		if (!this.getYTickLineVisible(scope)) { pp.setRangeGridlinesVisible(false); }
		pp.setRangeCrosshairVisible(true);
		pp.getRangeAxis().setAxisLinePaint(axesColor);
		pp.getRangeAxis().setLabelFont(getLabelFont());
		pp.getRangeAxis().setTickLabelFont(getTickFont());
		if (textColor != null) {
			pp.getRangeAxis().setLabelPaint(textColor);
			pp.getRangeAxis().setTickLabelPaint(textColor);
		}
		if (ytickunit > 0) { ((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(ytickunit)); }

		if (ylabel != null && !ylabel.isEmpty()) { pp.getRangeAxis().setLabel(ylabel); }
		if ("yaxis".equals(this.series_label_position)) {
			pp.getRangeAxis().setLabel(this.getChartdataset().getDataSeriesIds(scope).iterator().next());
			chart.getLegend().setVisible(false);
		}

		if (xlabel != null && !xlabel.isEmpty()) { pp.getDomainAxis().setLabel(xlabel); }
		if (textColor != null) {
			pp.getDomainAxis().setLabelPaint(textColor);
			pp.getDomainAxis().setTickLabelPaint(textColor);
			if (XAXIS.equals(this.series_label_position)) {
				((SubCategoryAxis) pp.getDomainAxis()).setSubLabelPaint(textColor);
			}
		}
	}

	@Override
	protected void initRenderer(final IScope scope) {
		// final CategoryPlot pp = (CategoryPlot) chart.getPlot();
		// final BarRenderer renderer = (BarRenderer) pp.getRenderer();

		// CategoryPlot plot = (CategoryPlot)this.chart.getPlot();
		// defaultrenderer = new BarRenderer();
		// plot.setRenderer((BarRenderer)defaultrenderer);

	}

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {
		final int x = xOnScreen - positionInPixels.x;
		final int y = yOnScreen - positionInPixels.y;
		final ChartEntity entity = info.getEntityCollection().getEntity(x, y);
		// getChart().handleClick(x, y, info);
		if (entity instanceof XYItemEntity xyie) {
			final XYDataset data = xyie.getDataset();
			final int index = xyie.getItem();
			final int series = xyie.getSeriesIndex();
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
		} else if (entity instanceof PieSectionEntity pie) {
			final String title = pie.getSectionKey().toString();
			final PieDataset data = pie.getDataset();
			final int index = pie.getSectionIndex();
			final double xx = data.getValue(index).doubleValue();
			final boolean xInt = xx % 1 == 0;
			sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
		} else if (entity instanceof CategoryItemEntity cie) {
			final Comparable<?> columnKey = cie.getColumnKey();
			final String title = columnKey.toString();
			final CategoryDataset data = cie.getDataset();
			final Comparable<?> rowKey = cie.getRowKey();
			final double xx = data.getValue(rowKey, columnKey).doubleValue();
			final boolean xInt = xx % 1 == 0;
			sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
		}
	}

}
