/*******************************************************************************************************
 *
 * ChartDataSourceList.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.util.ArrayList;
import java.util.HashMap;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.ui.displays.IChartDataSource;
import gama.dev.DEBUG;

/**
 * The Class ChartDataSourceList.
 */
public class ChartDataSourceList extends ChartDataSource {

	/** The currentseries. */
	ArrayList<String> currentSeriesNames;

	/** The legend exp. */
	IExpression legendExp;

	@Override
	public boolean cloneMe(final IScope scope, final int chartCycle, final ChartDataSource source) {
		currentSeriesNames = ((ChartDataSourceList) source).currentSeriesNames;
		legendExp = ((ChartDataSourceList) source).legendExp;
		return super.cloneMe(scope, chartCycle, source);
	}

	@Override
	public ChartDataSourceList getClone(final IScope scope, final int chartCycle) {
		final ChartDataSourceList res = new ChartDataSourceList();
		res.cloneMe(scope, chartCycle, this);
		return res;
	}

	/**
	 * Sets the name exp.
	 *
	 * @param scope
	 *            the scope
	 * @param expval
	 *            the expval
	 */
	public void setNameExp(final IScope scope, final IExpression expval) {
		legendExp = expval;
	}

	@Override
	public void updatevalues(final IScope scope, final int chartCycle) {
		super.updatevalues(scope, chartCycle);
		Object o = null;
		// final Object oname = this.getNameExp();
		final HashMap<String, Object> barvalues = new HashMap<>();
		if (this.isUseYErrValues()) { barvalues.put(ChartDataStatement.YERR_VALUES, this.getValueyerr().value(scope)); }
		if (this.isUseXErrValues()) { barvalues.put(ChartDataStatement.XERR_VALUES, this.getValuexerr().value(scope)); }
		if (this.isUseYMinMaxValues()) {
			barvalues.put(ChartDataStatement.XERR_VALUES, this.getValuexerr().value(scope));
		}
		if (this.isUseSizeExp()) { barvalues.put(ChartDataStatement.MARKERSIZE, this.getSizeexp().value(scope)); }
		if (this.isUseColorExp()) { barvalues.put(IKeyword.COLOR, this.getColorexp().value(scope)); }

		// TODO check same length and list

		updateserielist(scope, chartCycle);

		// int type_val = this.DATA_TYPE_NULL;
		if (getValue() != null) { o = getValue().value(scope); }
		// type_val = get_data_type(scope, o);

		if (o instanceof IList) {
			final IList<?> lval = GamaListFactory.castToList(scope, o);

			if (lval.size() > 0) {
				for (int i = 0; i < lval.size(); i++) {
					final Object no = lval.get(i);
					if (no != null) {
						updateseriewithvalue(scope, mySeries.get(currentSeriesNames.get(i)), no, chartCycle, barvalues,
								i);
					}
				}
			}
		}

	}

	private IList<?> extractLegends(final IScope scope) {
		if (legendExp == null) return null;
		final Object legObj = legendExp.value(scope);
		if (legObj instanceof Boolean b && !b) return null;
		if (legObj instanceof String s) return GamaListFactory.create(scope, Types.STRING, s);
		if (legObj instanceof IList l) return GamaListFactory.castToList(scope, l);
		return null;
	}

	private String getLegendLabel(final IScope scope, final IList<?> legends, final int index) {
		if (legends == null) return "";
		if (index >= legends.size()) return "";
		final Object val = legends.get(index);
		if (val == null) return "";
		return Cast.asString(scope, val);
	}

	/**
	 * Updateserielist.
	 *
	 * @param scope
	 *            the scope
	 * @param chartCycle
	 *            the chart cycle
	 */
	private void updateserielist(final IScope scope, final int chartCycle) {
		final Object valObj = getValue() == null ? null : getValue().value(scope);
		final IList<?> values = valObj instanceof IList ? GamaListFactory.castToList(scope, valObj) : GamaListFactory.create();
		final int targetSize = values.size();
		final IList<?> legends = extractLegends(scope);

		final ArrayList<String> previousSeries = currentSeriesNames != null ? currentSeriesNames : new ArrayList<>();
		currentSeriesNames = new ArrayList<>();

		for (int i = 0; i < targetSize; i++) {
			String serieId = "dl_" + this.hashCode() + "_" + i;
			currentSeriesNames.add(serieId);

			String legendStr = getLegendLabel(scope, legends, i);

			ChartDataSeries myserie = previousSeries.contains(serieId)
					? mySeries.get(serieId)
					: myDataset.createOrGetSerie(scope, serieId, this);
			if (!previousSeries.contains(serieId)) {
				mySeries.put(serieId, myserie);
			}
			if (myserie != null) {
				myserie.setSeriesLegend(legendStr);
			}
		}

		if (previousSeries.size() > targetSize) {
			for (int i = targetSize; i < previousSeries.size(); i++) {
				String s = previousSeries.get(i);
				mySeries.remove(s);
				getDataset().removeserie(scope, s);
			}
		}

		for (String element : currentSeriesNames) {
			getDataset().addSerieAtTheEnd(scope, element);
		}
	}

	@Override
	public void createInitialSeries(final IScope scope) {
		updateserielist(scope, 0);
		inferDatasetProperties(scope);
	}

	/**
	 * Infer dataset properties.
	 *
	 * @param scope
	 *            the scope
	 */
	public void inferDatasetProperties(final IScope scope) {
		Object o = null;
		int type_val = IChartDataSource.DATA_TYPE_NULL;
		if (this.getValue() != null) {
			o = this.getValue().value(scope);
			if (o instanceof IList && GamaListFactory.castToList(scope, o).size() > 0) {
				final Object o2 = GamaListFactory.castToList(scope, o).get(0);
				type_val = get_data_type(scope, o2);
			}

		}

		getDataset().getOutput().setDefaultPropertiesFromType(scope, this, type_val);

	}
}
