/*******************************************************************************************************
 *
 * ChartJFreeChartOutputBoxAndWhiskerCategory.java, in gama.core, is part of the source code of the GAMA modeling and
 * simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Color;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IChartDataSource;

/**
 * JFreeChart implementation for Box and Whisker category charts.
 */
public class ChartJFreeChartOutputBoxAndWhiskerCategory extends ChartJFreeChartOutput {

	private static final String XAXIS = "xaxis";
	private boolean useSubAxis = false;
	private boolean useMainAxisLabel = true;

	public ChartJFreeChartOutputBoxAndWhiskerCategory(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
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
	public void setDefaultPropertiesFromType(final IScope scope, final IChartDataSource source, final int type_val) {
		source.setUseXErrValues(false);
		source.setisBoxAndWhiskerData(true);
		source.setCumulative(scope, false);
		source.setUseSize(scope, false);
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		return new BoxAndWhiskerRenderer();
	}

	private void configureLegend(final AbstractCategoryItemRenderer newr, final ChartDataSeries myserie, final int myrow, final IScope scope) {
		final String legStr = myserie.getSerieLegend(scope) == null ? "" : myserie.getSerieLegend(scope).toString();
		if (StringUtils.isBlank(legStr)) {
			newr.setSeriesVisibleInLegend(myrow, false);
			return;
		}
		newr.setSeriesVisibleInLegend(myrow, true);
		newr.setLegendItemLabelGenerator((dataset, series) -> {
			String id = (String) dataset.getRowKey(series);
			ChartDataSeries ds = getChartdataset().getDataSeries(scope, id);
			return ds != null && ds.getSerieLegend(scope) != null ? ds.getSerieLegend(scope).toString() : id;
		});
	}

	protected void resetRenderer(final IScope scope, final String serieid) {
		if (chart == null) return;
		final CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		final BoxAndWhiskerRenderer newr = (BoxAndWhiskerRenderer) plot.getRenderer();
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (!idPosition.containsKey(serieid) || myserie == null) return;

		final int myrow = idPosition.get(serieid);
		if (myserie.getMycolor() != null) { newr.setSeriesPaint(myrow, IColor.toAWTColor(myserie.getMycolor())); }
		configureLegend(newr, myserie, myrow, scope);
	}

	@Override
	protected void clearDataSet(final IScope scope) {
		super.clearDataSet(scope);
		if (chart == null) return;
		final CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		for (int i = plot.getDatasetCount() - 1; i >= 1; i--) {
			plot.setDataset(i, null);
			plot.setRenderer(i, null);
		}
		jfreedataset.clear();
		jfreedataset.add(0, new DefaultBoxAndWhiskerCategoryDataset());
		plot.setDataset((BoxAndWhiskerCategoryDataset) jfreedataset.get(0));
		plot.setRenderer(0, null);
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		if (!idPosition.containsKey(serieid) && chart != null) {
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
		if (chart == null || jfreedataset.isEmpty()) return;
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		if (dataserie == null) return;

		final DefaultBoxAndWhiskerCategoryDataset serie = (DefaultBoxAndWhiskerCategoryDataset) jfreedataset.get(0);
		if (serie.getRowKeys().contains(serieid)) { serie.removeRow(serieid); }

		final ArrayList<String> cValues = dataserie.getCValues(scope);
		final ArrayList<Double> yValues = dataserie.getYValues(scope);
		final ArrayList<Double> sValues = dataserie.getSValues(scope);

		if (!cValues.isEmpty()) {
			final NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) this.chart.getPlot()).getRangeAxis();
			rangeAxis.setAutoRange(false);
			for (int i = 0; i < cValues.size(); i++) {
				if (properties.isYLogscale()) {
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
		if (chart == null) return;
		final CategoryPlot pp = (CategoryPlot) this.chart.getPlot();
		NumberAxis rangeAxis = (NumberAxis) pp.getRangeAxis();
		if (properties.isYLogscale()) {
			final LogarithmicAxis logAxis = new LogarithmicAxis(rangeAxis.getLabel());
			logAxis.setAllowNegativesFlag(true);
			pp.setRangeAxis(logAxis);
			rangeAxis = logAxis;
		}

		if (!properties.isUseYRangeInterval() && !properties.isUseYRangeMinMax() && !properties.isUseYMin() && !properties.isUseYMax()) {
			rangeAxis.setAutoRange(true);
		}
		if (properties.isUseYRangeInterval()) {
			rangeAxis.setFixedAutoRange(properties.getYRangeInterval());
			rangeAxis.setAutoRangeMinimumSize(properties.getYRangeInterval());
			rangeAxis.setAutoRange(true);
		}
		if (properties.isUseYRangeMinMax()) { rangeAxis.setRange(properties.getYRangeMin(), properties.getYRangeMax()); }
		if ((properties.isUseYMin() || properties.isUseYMax()) && !properties.isUseYRangeMinMax()) { applyYSingleBounds(scope, rangeAxis); }

		resetDomainAxis(scope);

		final CategoryAxis domainAxis = pp.getDomainAxis();
		Color ac = properties.getAxesColor() == null ? null : IColor.toAWTColor(properties.getAxesColor());
		pp.setDomainGridlinePaint(ac);
		pp.setRangeGridlinePaint(ac);
		pp.setRangeCrosshairVisible(true);

		pp.getRangeAxis().setAxisLinePaint(ac);
		pp.getRangeAxis().setLabelFont(properties.getLabelFont());
		pp.getRangeAxis().setTickLabelFont(properties.getTickFont());
		if (properties.getTextColor() != null) {
			Color tc = IColor.toAWTColor(properties.getTextColor());
			pp.getRangeAxis().setLabelPaint(tc);
			pp.getRangeAxis().setTickLabelPaint(tc);
		}
		if (properties.getYTickUnit() > 0) { ((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(properties.getYTickUnit())); }

		if (properties.getYLabel() != null && !properties.getYLabel().isEmpty()) { pp.getRangeAxis().setLabel(properties.getYLabel()); }
		if ("yaxis".equals(properties.getSeriesLabelPosition()) && !chartdataset.getDataSeriesIds(scope).isEmpty()) {
			pp.getRangeAxis().setLabel(this.getChartdataset().getDataSeriesIds(scope).iterator().next());
			if (chart.getLegend() != null) chart.getLegend().setVisible(false);
		}

		if (properties.getXLabel() != null && !properties.getXLabel().isEmpty()) { pp.getDomainAxis().setLabel(properties.getXLabel()); }

		if (this.useSubAxis && domainAxis instanceof SubCategoryAxis subAxis) {
			boolean hasSubCategories = false;
			for (final String serieid : chartdataset.getDataSeriesIds(scope)) {
				ChartDataSeries ds = chartdataset.getDataSeries(scope, serieid);
				String leg = ds != null && ds.getSerieLegend(scope) != null ? ds.getSerieLegend(scope).toString() : "";
				if (StringUtils.isNotBlank(leg)) {
					subAxis.addSubCategory(leg);
					hasSubCategories = true;
				}
			}
			if (!hasSubCategories) {
				pp.setDomainAxis(new CategoryAxis(pp.getDomainAxis().getLabel()));
				this.useSubAxis = false;
			}
		}
		if (!properties.isYTickLineVisible()) {
			pp.setDomainGridlinesVisible(false);
			pp.setRangeCrosshairVisible(false);
		}
		if (!properties.isYTickValueVisible()) {
			pp.getRangeAxis().setTickMarksVisible(false);
			pp.getRangeAxis().setTickLabelsVisible(false);
		}
	}

	public void resetDomainAxis(final IScope scope) {
		if (chart == null) return;
		final CategoryPlot pp = (CategoryPlot) chart.getPlot();
		if ("none".equals(properties.getSeriesLabelPosition())) { this.useSubAxis = false; }
		if (this.useSubAxis) {
			final SubCategoryAxis newAxis = new SubCategoryAxis(pp.getDomainAxis().getLabel());
			pp.setDomainAxis(newAxis);
		}
		Color ac = properties.getAxesColor() == null ? null : IColor.toAWTColor(properties.getAxesColor());
		pp.getDomainAxis().setAxisLinePaint(ac);
		pp.getDomainAxis().setTickLabelFont(properties.getTickFont());
		pp.getDomainAxis().setLabelFont(properties.getLabelFont());
		if (properties.getTextColor() != null) {
			Color tc = IColor.toAWTColor(properties.getTextColor());
			pp.getDomainAxis().setLabelPaint(tc);
			pp.getDomainAxis().setTickLabelPaint(tc);
			if (XAXIS.equals(properties.getSeriesLabelPosition()) && pp.getDomainAxis() instanceof SubCategoryAxis sca) {
				sca.setSubLabelPaint(tc);
			}
		}

		if (properties.getGap() > 0) {
			pp.getDomainAxis().setCategoryMargin(properties.getGap());
			pp.getDomainAxis().setUpperMargin(properties.getGap());
			pp.getDomainAxis().setLowerMargin(properties.getGap());
		}

		if (this.useSubAxis && !this.useMainAxisLabel) { pp.getDomainAxis().setTickLabelsVisible(false); }
		if (!properties.isYTickLineVisible()) {
			pp.setDomainGridlinesVisible(false);
			pp.setRangeCrosshairVisible(false);
		}
		if (!properties.isYTickValueVisible()) {
			pp.getRangeAxis().setTickMarksVisible(false);
			pp.getRangeAxis().setTickLabelsVisible(false);
		}
		if (!properties.isXTickValueVisible()) {
			pp.getDomainAxis().setTickMarksVisible(false);
			pp.getDomainAxis().setTickLabelsVisible(false);
		}
	}

	private void resolveSeriesLabelPosition() {
		final String sty = getStyle();
		this.useSubAxis = false;

		if (IKeyword.STACK.equals(sty)) {
			if (XAXIS.equals(properties.getSeriesLabelPosition())) { properties.setSeriesLabelPosition(IKeyword.DEFAULT); }
			if (IKeyword.DEFAULT.equals(properties.getSeriesLabelPosition())) { properties.setSeriesLabelPosition(IKeyword.LEGEND); }
		} else if ("default".equals(properties.getSeriesLabelPosition())) {
			if (!this.getChartdataset().getSources().isEmpty()) {
				final ChartDataSource onesource = this.getChartdataset().getSources().get(0);
				if (onesource.isCumulative()) {
					properties.setSeriesLabelPosition("legend");
				} else {
					properties.setSeriesLabelPosition(XAXIS);
					useMainAxisLabel = false;
				}
			} else {
				properties.setSeriesLabelPosition("legend");
			}
		}

		if ("none".equals(properties.getSeriesLabelPosition())) {
			this.useSubAxis = false;
		} else if (XAXIS.equals(properties.getSeriesLabelPosition())) {
			this.useSubAxis = true;
		}
	}

	private void formatPlotAxes(final CategoryPlot pp, final IScope scope) {
		Color ac = properties.getAxesColor() == null ? null : IColor.toAWTColor(properties.getAxesColor());
		pp.setDomainGridlinePaint(ac);
		pp.setRangeGridlinePaint(ac);
		if (!properties.isXTickLineVisible()) { pp.setDomainGridlinesVisible(false); }
		if (!properties.isYTickLineVisible()) { pp.setRangeGridlinesVisible(false); }
		pp.setRangeCrosshairVisible(true);
		pp.getRangeAxis().setAxisLinePaint(ac);
		pp.getRangeAxis().setLabelFont(properties.getLabelFont());
		pp.getRangeAxis().setTickLabelFont(properties.getTickFont());
		if (properties.getTextColor() != null) {
			Color tc = IColor.toAWTColor(properties.getTextColor());
			pp.getRangeAxis().setLabelPaint(tc);
			pp.getRangeAxis().setTickLabelPaint(tc);
		}
		if (properties.getYTickUnit() > 0) { ((NumberAxis) pp.getRangeAxis()).setTickUnit(new NumberTickUnit(properties.getYTickUnit())); }

		if (properties.getYLabel() != null && !properties.getYLabel().isEmpty()) { pp.getRangeAxis().setLabel(properties.getYLabel()); }
		if ("yaxis".equals(properties.getSeriesLabelPosition()) && !chartdataset.getDataSeriesIds(scope).isEmpty()) {
			pp.getRangeAxis().setLabel(this.getChartdataset().getDataSeriesIds(scope).iterator().next());
			if (chart.getLegend() != null) chart.getLegend().setVisible(false);
		}

		if (properties.getXLabel() != null && !properties.getXLabel().isEmpty()) { pp.getDomainAxis().setLabel(properties.getXLabel()); }
		if (properties.getTextColor() != null && pp.getDomainAxis() instanceof SubCategoryAxis sca) {
			Color tc = IColor.toAWTColor(properties.getTextColor());
			pp.getDomainAxis().setLabelPaint(tc);
			pp.getDomainAxis().setTickLabelPaint(tc);
			if (XAXIS.equals(properties.getSeriesLabelPosition())) {
				sca.setSubLabelPaint(tc);
			}
		}
	}

	@Override
	public void initChart_post_data_init(final IScope scope) {
		super.initChart_post_data_init(scope);
		if (chart == null) return;
		final CategoryPlot pp = (CategoryPlot) chart.getPlot();

		resolveSeriesLabelPosition();

		if (!"legend".equals(properties.getSeriesLabelPosition()) && chart.getLegend() != null) {
			chart.getLegend().setVisible(false);
		}
		this.resetDomainAxis(scope);
		formatPlotAxes(pp, scope);
	}

}
