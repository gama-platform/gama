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
import java.util.Map;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.ui.displays.IChartDataSource;

/**
 * The Class ChartDataSourceList.
 */
public class ChartDataSourceList extends ChartDataSource {

	/** The currentseries. */
	ArrayList<String> currentSeriesNames;

	/** The cached series ids. */
	private final ArrayList<String> cachedSeriesIds = new ArrayList<>();

	/**
	 * Gets the series id.
	 *
	 * @param index
	 *            the index
	 * @return the series id
	 */
	private String getSeriesId(final int index) {
		while (cachedSeriesIds.size() <= index) {
			cachedSeriesIds.add("dl_" + System.identityHashCode(this) + "_" + cachedSeriesIds.size());
		}
		return cachedSeriesIds.get(index);
	}

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
		if (getValue() == null) {
			updateserielist(scope, chartCycle, GamaListFactory.create());
			return;
		}
		final Object value = getValue().value(scope);
		final IList<?> values =
				value instanceof IList ? GamaListFactory.castToList(scope, value) : GamaListFactory.create();
		updateserielist(scope, chartCycle, values);
		if (!values.isEmpty()) {
			final Map<String, Object> barvalues = computeBarValues(scope);
			for (int i = 0; i < values.size(); i++) {
				final Object no = values.get(i);
				if (no != null) {
					updateseriewithvalue(scope, mySeries.get(currentSeriesNames.get(i)), no, chartCycle, barvalues, i);
				}
			}
		}
	}

	/**
	 * Extract legends.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	private IList<?> extractLegends(final IScope scope) {
		if (legendExp == null) return null;
		final Object legObj = legendExp.value(scope);
		switch (legObj) {
			case Boolean b -> {
				if (!b) return null;
				return GamaListFactory.create(scope, Types.STRING);
			}
			case String s -> {
				return GamaListFactory.create(scope, Types.STRING, s);
			}
			case IList l -> {
				return GamaListFactory.castToList(scope, l);
			}
			case null, default -> {
			}
		}
		return null;
	}

	/**
	 * Gets the legend label.
	 *
	 * @param scope
	 *            the scope
	 * @param legends
	 *            the legends
	 * @param index
	 *            the index
	 * @return the legend label
	 */
	private String getLegendLabel(final IScope scope, final IList<?> legends, final int index) {
		if (legends == null) return "";
		if (legends.isEmpty()) return "Series " + (index + 1);
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
	private void updateserielist(final IScope scope, final int chartCycle, final IList<?> values) {
		final int targetSize = values.size();
		final IList<?> legends = extractLegends(scope);

		final ArrayList<String> previousSeries = currentSeriesNames != null ? currentSeriesNames : new ArrayList<>();
		currentSeriesNames = new ArrayList<>();

		for (int i = 0; i < targetSize; i++) {
			String serieId = getSeriesId(i);
			currentSeriesNames.add(serieId);

			String legendStr = getLegendLabel(scope, legends, i);

			ChartDataSeries myserie = previousSeries.contains(serieId) ? mySeries.get(serieId)
					: myDataset.createOrGetSerie(scope, serieId, this);
			if (!previousSeries.contains(serieId)) { mySeries.put(serieId, myserie); }
			if (myserie != null) { myserie.setSeriesLegend(legendStr); }
		}

		if (previousSeries.size() > targetSize) {
			for (int i = targetSize; i < previousSeries.size(); i++) {
				String s = previousSeries.get(i);
				mySeries.remove(s);
				getDataset().removeserie(scope, s);
			}
		}

		for (String element : currentSeriesNames) { getDataset().addSerieAtTheEnd(scope, element); }
	}

	@Override
	public void createInitialSeries(final IScope scope) {
		final Object value = getValue() == null ? null : getValue().value(scope);
		final IList<?> values =
				value instanceof IList ? GamaListFactory.castToList(scope, value) : GamaListFactory.create();
		updateserielist(scope, 0, values);
		inferDatasetProperties(scope, values);
	}

	/**
	 * Infer dataset properties.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the current list of values
	 */
	public void inferDatasetProperties(final IScope scope) {
		final Object value = getValue() == null ? null : getValue().value(scope);
		final IList<?> values =
				value instanceof IList ? GamaListFactory.castToList(scope, value) : GamaListFactory.create();
		inferDatasetProperties(scope, values);
	}

	/**
	 * Infer dataset properties.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 */
	private void inferDatasetProperties(final IScope scope, final IList<?> values) {
		int type_val = IChartDataSource.DATA_TYPE_NULL;
		if (!values.isEmpty()) { type_val = get_data_type(scope, values.get(0)); }

		getDataset().getOutput().setDefaultPropertiesFromType(scope, this, type_val);

	}
}
