/*******************************************************************************************************
 *
 * ChartJFreeChartOutputRadar.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IChartDataSource;

/**
 * JFreeChart implementation for Radar / SpiderWeb charts.
 */
public class ChartJFreeChartOutputRadar extends ChartJFreeChartOutput {

	public ChartJFreeChartOutputRadar(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		jfreedataset.add(0, dataset);
		final SpiderWebPlot plot = new SpiderWebPlot(dataset);
		chart = new JFreeChart(getName(), null, plot, true);
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

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		return null;
	}

	protected void resetRenderer(final IScope scope, final String serieid) {
		if (chart == null) return;
		final SpiderWebPlot plot = (SpiderWebPlot) this.chart.getPlot();
		if (plot.getMaxValue() <= 0.0) { plot.setMaxValue(1.0); }
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (myserie != null && idPosition.containsKey(serieid)) {
			final int myrow = idPosition.get(serieid);
			if (myserie.getMycolor() != null) {
				plot.setSeriesPaint(myrow, IColor.toAWTColor(myserie.getMycolor()));
			}
		}
	}

	@Override
	protected void clearDataSet(final IScope scope) {
		super.clearDataSet(scope);
		if (chart == null) return;
		final SpiderWebPlot plot = (SpiderWebPlot) this.chart.getPlot();
		if (!jfreedataset.isEmpty()) { ((DefaultCategoryDataset) jfreedataset.get(0)).clear(); }
		jfreedataset.clear();
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		jfreedataset.add(0, dataset);
		plot.setDataset(dataset);
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		if (!idPosition.containsKey(serieid) && chart != null) {
			final SpiderWebPlot plot = (SpiderWebPlot) this.chart.getPlot();
			final DefaultCategoryDataset firstdataset = (DefaultCategoryDataset) plot.getDataset();
			if (nbseries == 0) {
				plot.setDataset(firstdataset);
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
			int deb = 0;
			if (properties.isUseXRangeInterval() && cValues.size() > properties.getXRangeInterval()) {
				deb = cValues.size() - (int) properties.getXRangeInterval();
			}
			boolean oldNotify = serie.getNotify();
			serie.setNotify(false);
			try {
				for (int i = deb; i < cValues.size(); i++) {
					serie.addValue(yValues.get(i), serieid, cValues.get(i - deb));
				}
			} finally {
				serie.setNotify(oldNotify);
			}
		}
		this.resetRenderer(scope, serieid);
	}

	@Override
	public void resetAxes(final IScope scope) {
		if (chart == null || chart.getLegend() == null) return;
		if ("none".equals(properties.getSeriesLabelPosition())) {
			this.chart.getLegend().setVisible(false);
		}
	}

	@Override
	public void initChart_post_data_init(final IScope scope) {
		super.initChart_post_data_init(scope);
		if (chart == null) return;
		final SpiderWebPlot pp = (SpiderWebPlot) chart.getPlot();

		Color ac = properties.getAxesColor() == null ? null : IColor.toAWTColor(properties.getAxesColor());
		pp.setAxisLinePaint(ac);
		pp.setLabelFont(properties.getLabelFont());
		if (properties.getTextColor() != null) { pp.setLabelPaint(IColor.toAWTColor(properties.getTextColor())); }

		if (chart.getLegend() != null) {
			chart.getLegend().setVisible(!"none".equals(properties.getSeriesLabelPosition()));
		}

		if (properties.isUseYRangeInterval()) {
			pp.setMaxValue(properties.getYRangeInterval());
		} else if (properties.isUseYRangeMinMax()) {
			pp.setMaxValue(properties.getYRangeMax());
		}
	}

}
