/*******************************************************************************************************
 *
 * ChartJFreeChartOutputPie.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.general.DefaultPieDataset;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.displays.IChartDataSource;

/**
 * JFreeChart implementation for Pie charts (2D, 3D, Ring).
 */
public class ChartJFreeChartOutputPie extends ChartJFreeChartOutput {

	public ChartJFreeChartOutputPie(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		String style = properties.getStyle();
		if (IKeyword.THREE_D.equals(style)) {
			chart = ChartFactory.createPieChart3D(getName(), null, false, true, false);
		} else if (IKeyword.RING.equals(style)) {
			chart = ChartFactory.createRingChart(getName(), null, false, true, false);
		} else {
			chart = ChartFactory.createPieChart(getName(), null, false, true, false);
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

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);
		if (chart == null) return;

		final PiePlot<?> pp = (PiePlot<?>) chart.getPlot();
		pp.setShadowXOffset(0);
		pp.setShadowYOffset(0);

		if (!"none".equals(properties.getSeriesLabelPosition())) {
			pp.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} ({2})"));
			if (properties.getAxesColor() != null) { pp.setLabelLinkPaint(IColor.toAWTColor(properties.getAxesColor())); }
			pp.setLabelFont(properties.getTickFont());
			if (properties.getLabelTextColor() != null) { pp.setLabelPaint(IColor.toAWTColor(properties.getLabelTextColor())); }
			if (properties.getLabelBackgroundColor() != null) {
				pp.setLabelBackgroundPaint(IColor.toAWTColor(properties.getLabelBackgroundColor()));
			}
		} else {
			pp.setLabelLinksVisible(false);
			pp.setLabelGenerator(null);
		}
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		return new DefaultPolarItemRenderer();
	}

	protected void resetRenderer(final IScope scope, final String serieid) {
		if (chart == null) return;
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (myserie == null || myserie.getMycolor() == null) return;
		((PiePlot<?>) this.chart.getPlot()).setSectionPaint(serieid, IColor.toAWTColor(myserie.getMycolor()));
	}

	@Override
	protected void clearDataSet(final IScope scope) {
		super.clearDataSet(scope);
		if (chart == null) return;
		final PiePlot<?> plot = (PiePlot<?>) this.chart.getPlot();
		jfreedataset.clear();
		DefaultPieDataset<String> dd = new DefaultPieDataset<>();
		jfreedataset.add(0, dd);
		plot.setDataset(dd);
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		if (!idPosition.containsKey(serieid)) {
			@SuppressWarnings("unchecked")
			final PiePlot<String> plot = (PiePlot<String>) this.chart.getPlot();
			nbseries++;
			idPosition.put(serieid, nbseries - 1);
			if (IKeyword.EXPLODED.equals(getStyle())) {
				plot.setExplodePercent(serieid, 0.20);
			}
		}
	}

	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		if (dataserie == null || jfreedataset.isEmpty()) return;
		@SuppressWarnings("unchecked")
		final DefaultPieDataset<String> serie = (DefaultPieDataset<String>) jfreedataset.get(0);
		final ArrayList<Double> yValues = dataserie.getYValues(scope);

		if (!yValues.isEmpty()) {
			serie.setValue(serieid, yValues.get(yValues.size() - 1));
		}
		this.resetRenderer(scope, serieid);
	}

}
