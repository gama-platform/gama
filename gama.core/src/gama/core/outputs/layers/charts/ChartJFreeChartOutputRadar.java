/*******************************************************************************************************
 *
 * ChartJFreeChartOutputRadar.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.awt.Point;
import java.util.ArrayList;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.runtime.IScope;
import gama.gaml.expressions.IExpression;

/**
 * The Class ChartJFreeChartOutputRadar.
 */
public class ChartJFreeChartOutputRadar extends ChartJFreeChartOutput {

	/**
	 * Instantiates a new chart J free chart output radar.
	 *
	 * @param scope the scope
	 * @param name the name
	 * @param typeexp the typeexp
	 */
	public ChartJFreeChartOutputRadar(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
		// TODO Auto-generated constructor stub

	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		final SpiderWebPlot plot = new SpiderWebPlot((CategoryDataset) createDataset(scope));
		chart = new JFreeChart(getName(), null, plot, true);

	}

	@Override
	public void initdataset() {
		super.initdataset();
		chartdataset.setCommonXSeries(true);
		chartdataset.setByCategory(true);
	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final ChartDataSource source, final int type_val) {
		

		switch (type_val) {
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_N:
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_N:
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_12:
			case ChartDataSource.DATA_TYPE_LIST_POINT:
			case ChartDataSource.DATA_TYPE_MATRIX_DOUBLE:
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_3:
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_3:
			default: {
				source.setCumulative(scope, false); // never cumulative by default
				source.setUseSize(scope, false);
			}
		}

	}

	/**
	 * Creates the dataset.
	 *
	 * @param scope the scope
	 * @return the dataset
	 */
	Dataset createDataset(final IScope scope) {
		return new DefaultCategoryDataset();
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {
		return null;
	}

	/**
	 * Reset renderer.
	 *
	 * @param scope the scope
	 * @param serieid the serieid
	 */
	protected void resetRenderer(final IScope scope, final String serieid) {
		final SpiderWebPlot plot = (SpiderWebPlot) this.chart.getPlot();
		if (plot.getMaxValue() <= 0.0) plot.setMaxValue(1.0);
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		if (!idPosition.containsKey(serieid)) {
			// DEBUG.LOG("pb!!!");
		} else {
			final int myrow = idPosition.get(serieid);
			if (myserie.getMycolor() != null) {
				plot.setSeriesPaint(myrow, myserie.getMycolor());
			}

			if ("onchart".equals(series_label_position)) {
				//// newr.setBaseItemLabelGenerator(new LabelGenerator());
				// ItemLabelPosition itemlabelposition = new
				//// ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
				//// TextAnchor.BOTTOM_CENTER);
				// newr.setBasePositiveItemLabelPosition(itemlabelposition);
				// newr.setBaseNegativeItemLabelPosition(itemlabelposition);
				// newr.setBaseItemLabelsVisible(true);
			}

		}

	}

	@Override
	protected void clearDataSet(final IScope scope) {
		
		super.clearDataSet(scope);
		final SpiderWebPlot plot = (SpiderWebPlot) this.chart.getPlot();
		for (int i = plot.getDataset().getRowCount() - 1; i >= 1; i--) {
			// plot.setDataset(i, null);
			// plot.setRenderer(i, null);
		}
		if (jfreedataset.size() > 0) {
			((DefaultCategoryDataset) jfreedataset.get(0)).clear();
		}
		jfreedataset.clear();
		jfreedataset.add(0, new DefaultCategoryDataset());
		plot.setDataset((DefaultCategoryDataset) jfreedataset.get(0));
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		// final ChartDataSeries dataserie = chartdataset.getDataSeries(scope,
		// serieid);
		// final XYIntervalSeries serie = new
		// XYIntervalSeries(dataserie.getSerieLegend(scope), false, true);
		if(!idPosition.containsKey(serieid)) {

			final SpiderWebPlot plot = (SpiderWebPlot) this.chart.getPlot();
	
			final DefaultCategoryDataset firstdataset = (DefaultCategoryDataset) plot.getDataset();
	
			if (nbseries == 0) {
				plot.setDataset(firstdataset);
	
			} else {
	
				// DefaultCategoryDataset newdataset=new DefaultCategoryDataset();
				// jfreedataset.add(newdataset);
				// plot.setDataset(jfreedataset.size()-1, newdataset);
				// plot.setDataset(nbseries, firstdataset);
	
			}
			nbseries++;
			// plot.setRenderer(nbseries-1,
			// (CategoryItemRenderer)getOrCreateRenderer(scope,serieid));
			idPosition.put(serieid, nbseries - 1);
		}
		// DEBUG.LOG("new serie"+serieid+" at
		// "+IdPosition.get(serieid)+" fdsize "+plot.getCategories().size()+"
		// jfds "+jfreedataset.size()+" datasc "+plot.getDatasetCount()+" nbse
		// "+nbseries);
		
	}

	@Override
	public void removeSerie(final IScope scope, final String serieid) {
		
		super.removeSerie(scope, serieid);
		this.clearDataSet(scope);
	}

	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		

		chart.getPlot();
		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		// DefaultCategoryDataset serie=((DefaultCategoryDataset)
		// jfreedataset.get(IdPosition.get(dataserie.getSerieId(scope))));
		final DefaultCategoryDataset serie = (DefaultCategoryDataset) jfreedataset.get(0);
		if (serie.getRowKeys().contains(serieid)) {
			serie.removeRow(serieid);
		}
		final ArrayList<String> CValues = dataserie.getCValues(scope);
		final ArrayList<Double> YValues = dataserie.getYValues(scope);
		final ArrayList<Double> SValues = dataserie.getSValues(scope);
		if (CValues.size() > 0) {
			int deb = 0;
			if (this.usexrangeinterval && CValues.size() > this.xrangeinterval) {
				deb = CValues.size() - (int) this.xrangeinterval;
			}
			for (int i = deb; i < CValues.size(); i++) {
				serie.addValue(YValues.get(i), serieid, CValues.get(i - deb));
				// ((ExtendedCategoryAxis)domainAxis).addSubLabel(CValues.get(i),
				// serieid);;
			}
		}
		if (SValues.size() > 0) {
			// what to do with Z values??

		}

		this.resetRenderer(scope, serieid);

	}

	@Override
	public void resetAxes(final IScope scope) {
		if ("none".equals(series_label_position)) {
			this.chart.getLegend().setVisible(false);
		}

	}

	/**
	 * Reset domain axis.
	 *
	 * @param scope the scope
	 */
	private void resetDomainAxis(final IScope scope) {
		
		chart.getPlot();

	}

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);
		chart.getPlot();

	}

	@Override
	public void initChart_post_data_init(final IScope scope) {
		
		super.initChart_post_data_init(scope);
		final SpiderWebPlot pp = (SpiderWebPlot) chart.getPlot();
		
		// final String sty = getStyle();
		// this.useSubAxis=false;
		// switch (sty) {
		// default: {
		if ("default".equals(series_label_position)) {
			this.series_label_position = "legend";
		}
		// break;
		// }
		// }
		if ("xaxis".equals(series_label_position)) {
			// this.useSubAxis=true;
		}

		if (!"legend".equals(series_label_position)) {
			chart.getLegend().setVisible(false);
			// legend is useless, but I find it nice anyway... Could put back...
		}
		this.resetDomainAxis(scope);

		pp.setAxisLinePaint(axesColor);

		pp.setLabelFont(getLabelFont());
		if (textColor != null) {
			pp.setLabelPaint(textColor);
		}

		// if (ylabel != null && ylabel != "") {}
		if ("yaxis".equals(series_label_position)) {
			// pp.getRangeAxis().setLabel(this.getChartdataset().getDataSeriesIds(scope).iterator().next());
			chart.getLegend().setVisible(false);
		}
		chart.getLegend().setVisible(true);

		if (xlabel != null && ! "".equals(xlabel) ) {
			// pp.getDomainAxis().setLabel(xlabel);
		}

		if ("none".equals(series_label_position)) {
			pp.setLabelPaint(this.backgroundColor);
		}
		if (this.useyrangeinterval) 
			((SpiderWebPlot) chart.getPlot()).setMaxValue(this.yrangeinterval);
		else if (this.useyrangeminmax)
			((SpiderWebPlot) chart.getPlot()).setMaxValue(this.yrangemax);
	}

	@Override
	protected void initRenderer(final IScope scope) {

	}

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {
		final int x = xOnScreen - positionInPixels.x;
		final int y = yOnScreen - positionInPixels.y;
		final ChartEntity entity = info.getEntityCollection().getEntity(x, y);
		// getChart().handleClick(x, y, info);

		final Comparable<?> columnKey = ((CategoryItemEntity) entity).getColumnKey();
		final String title = columnKey.toString();
		final CategoryDataset data = ((CategoryItemEntity) entity).getDataset();
		final Comparable<?> rowKey = ((CategoryItemEntity) entity).getRowKey();
		final double xx = data.getValue(rowKey, columnKey).doubleValue();
		final boolean xInt = xx % 1 == 0;
		sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));

	}

}
