/*******************************************************************************************************
 *
 * ChartJFreeChartOutputHistogram.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
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
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.renderer.category.LevelRenderer;
import org.jfree.chart.renderer.category.ScatterRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IChartDataSource;

/**
 * JFreeChart implementation for Histogram / Bar charts.
 */
public class ChartJFreeChartOutputHistogram extends ChartJFreeChartOutput {

	private static final String XAXIS = "xaxis";
	private boolean useSubAxis = false;
	private boolean useMainAxisLabel = true;

	public static void enableFlatLook(final boolean flat) {
		if (flat) {
			BarRenderer.setDefaultBarPainter(new StandardBarPainter());
			BarRenderer.setDefaultShadowsVisible(false);
			XYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter());
			XYBarRenderer.setDefaultShadowsVisible(false);
		} else {
			BarRenderer.setDefaultBarPainter(new GradientBarPainter());
			BarRenderer.setDefaultShadowsVisible(true);
			XYBarRenderer.setDefaultBarPainter(new GradientXYBarPainter());
			XYBarRenderer.setDefaultShadowsVisible(true);
		}
	}

	public ChartJFreeChartOutputHistogram(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		jfreedataset.add(0, new DefaultCategoryDataset());
		PlotOrientation orientation = properties.isReverseAxes() ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		String style = properties.getStyle();
		if (IKeyword.THREE_D.equals(style) || !IKeyword.STACK.equals(style)) {
			chart = ChartFactory.createBarChart(getName(), null, null, null, orientation, true, true, false);
		} else {
			chart = ChartFactory.createStackedBarChart(getName(), null, null, null, orientation, true, true, false);
		}
	}

	@Override
	public void initdataset() {
		super.initdataset();
		chartdataset.setCommonXSeries(true);
		chartdataset.setByCategory(true);
	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final IChartDataSource source, final int type_val) {
		source.setCumulative(scope, false);
		source.setUseSize(scope, false);
	}

	static class LabelGenerator extends StandardCategoryItemLabelGenerator {
		@Override
		public String generateLabel(final CategoryDataset dataset, final int series, final int category) {
			return dataset.getRowKey(series).toString();
		}
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		final String style = this.getChartdataset().getDataSeries(scope, serieid).getStyle(scope);
		return switch (style) {
			case IKeyword.STACK -> new StackedBarRenderer();
			case IKeyword.DOT -> new ScatterRenderer();
			case IKeyword.AREA -> new StackedAreaRenderer();
			case IKeyword.LINE -> new StatisticalLineAndShapeRenderer();
			case IKeyword.STEP -> new LevelRenderer();
			default -> new BarRenderer();
		};
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
		final AbstractCategoryItemRenderer newr = (AbstractCategoryItemRenderer) plot.getRenderer();
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (!idPosition.containsKey(serieid) || myserie == null) return;

		final int myrow = idPosition.get(serieid);
		if (myserie.getMycolor() != null) { newr.setSeriesPaint(myrow, IColor.toAWTColor(myserie.getMycolor())); }

		configureLegend(newr, myserie, myrow, scope);

		if ("onchart".equals(properties.getSeriesLabelPosition())) {
			newr.setDefaultItemLabelGenerator(new LabelGenerator());
			final ItemLabelPosition itemlabelposition = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
			newr.setDefaultPositiveItemLabelPosition(itemlabelposition);
			newr.setDefaultNegativeItemLabelPosition(itemlabelposition);
			newr.setDefaultItemLabelsVisible(true);
			if (properties.getTextColor() != null) { newr.setDefaultItemLabelPaint(IColor.toAWTColor(properties.getTextColor())); }
		}

		if (newr instanceof BarRenderer && properties.getGap() >= 0) {
			((BarRenderer) newr).setMaximumBarWidth(1 - properties.getGap());
		}
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
		((DefaultCategoryDataset) jfreedataset.get(0)).clear();
		jfreedataset.clear();
		jfreedataset.add(0, new DefaultCategoryDataset());
		plot.setDataset((DefaultCategoryDataset) jfreedataset.get(0));
		plot.setRenderer(0, null);
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		if (!idPosition.containsKey(serieid) && chart != null) {
			final CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
			final DefaultCategoryDataset firstdataset = (DefaultCategoryDataset) plot.getDataset();
			if (nbseries == 0) {
				plot.setDataset(0, firstdataset);
				plot.setRenderer(nbseries, (CategoryItemRenderer) getOrCreateRenderer(scope, serieid));
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

		final DefaultCategoryDataset serie = (DefaultCategoryDataset) jfreedataset.get(0);
		if (serie.getRowKeys().contains(serieid)) { serie.removeRow(serieid); }
		final ArrayList<String> cValues = dataserie.getCValues(scope);
		final ArrayList<Double> yValues = dataserie.getYValues(scope);

		if (!cValues.isEmpty()) {
			final NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) this.chart.getPlot()).getRangeAxis();
			rangeAxis.setAutoRange(false);
			for (int i = 0; i < cValues.size(); i++) {
				if (properties.isYLogscale()) {
					final double val = yValues.get(i);
					if (val <= 0) throw GamaRuntimeException.warning("Log scale with <=0 value:" + val, scope);
				}
				serie.addValue(yValues.get(i), serieid, cValues.get(i));
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
		Color ac = IColor.toAWTColor(properties.getAxesColor());
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
		Color ac = IColor.toAWTColor(properties.getAxesColor());
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

		if (this.useSubAxis && !this.useMainAxisLabel) {
			pp.getDomainAxis().setTickLabelsVisible(false);
		}
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

	@Override
	public void initChart_post_data_init(final IScope scope) {
		super.initChart_post_data_init(scope);
		if (chart == null) return;
		final CategoryPlot pp = (CategoryPlot) chart.getPlot();

		final String sty = getStyle();
		this.useSubAxis = false;
		if (IKeyword.STACK.equals(sty)) {
			if (XAXIS.equals(properties.getSeriesLabelPosition())) { properties.setSeriesLabelPosition("default"); }
			if ("default".equals(properties.getSeriesLabelPosition())) { properties.setSeriesLabelPosition("legend"); }
		} else {
			if ("default".equals(properties.getSeriesLabelPosition())) {
				if (!chartdataset.getSources().isEmpty()) {
					final ChartDataSource onesource = chartdataset.getSources().get(0);
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
		}

		if ("none".equals(properties.getSeriesLabelPosition())) {
			this.useSubAxis = false;
		} else if (XAXIS.equals(properties.getSeriesLabelPosition())) {
			this.useSubAxis = true;
		}

		if (!"legend".equals(properties.getSeriesLabelPosition()) && chart.getLegend() != null) {
			chart.getLegend().setVisible(false);
		}
		this.resetDomainAxis(scope);
		Color ac = IColor.toAWTColor(properties.getAxesColor());
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

}
