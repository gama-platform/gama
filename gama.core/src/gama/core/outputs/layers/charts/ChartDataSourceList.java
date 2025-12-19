/*******************************************************************************************************
 *
 * ChartDataSourceList.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.util.ArrayList;
import java.util.HashMap;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;

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
			final IList<?> lval = Cast.asList(scope, o);

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

	/**
	 * Updateserielist.
	 *
	 * @param scope
	 *            the scope
	 * @param chartCycle
	 *            the chart cycle
	 */
	private void updateserielist(final IScope scope, final int chartCycle) {
		final IList<String> legends = legendExp == null ? null : Cast.asList(scope, legendExp.value(scope));
		if (legends == null) return;
		final IList<?> values = Cast.asList(scope, getValue().value(scope));
		final ArrayList<String> previousSeries = currentSeriesNames;
		currentSeriesNames = new ArrayList<>();
		boolean somethingChanged = false;
		if (legends.size() > 0) {
			// value list case
			for (int i = 0; i < Math.min(values.size(), legends.size()); i++) {
				final String name = legends.get(i);
				if (name != null) {
					currentSeriesNames.add(name);
					if (i >= previousSeries.size() || !previousSeries.get(i).equals(name)) {
						somethingChanged = true;
						if (previousSeries.contains(name)) {
							// serie i was serie k before
						} else {
							// new serie
							newSerie(scope, name);
						}
					}
				}
			}
		}
		if (currentSeriesNames.size() != previousSeries.size()) { somethingChanged = true; }
		if (somethingChanged) {
			for (String s : previousSeries) {
				if (!currentSeriesNames.contains(s)) { getDataset().removeserie(scope, s); }
			}
			for (String element : currentSeriesNames) { getDataset().addSerieAtTheEnd(scope, element); }

		}

	}

	/**
	 * Newserie.
	 *
	 * @param scope
	 *            the scope
	 * @param myname
	 *            the myname
	 */
	private void newSerie(final IScope scope, final String myname) {
		if (this.getDataset().getDataSeriesIds(scope).contains(myname)) {
			DEBUG.LOG("Serie " + myname + "s already exists... Will replace old one!!");
		}
		final ChartDataSeries myserie = myDataset.createOrGetSerie(scope, myname, this);
		mySeries.put(myname, myserie);

	}

	@Override
	public void createInitialSeries(final IScope scope) {

		final Object on = legendExp == null ? null : legendExp.value(scope);

		if (on instanceof IList lval) {
			currentSeriesNames = new ArrayList<>();
			for (int i = 0; i < lval.size(); i++) {
				final Object no = lval.get(i);
				if (no != null) {
					final String myname = Cast.asString(scope, no);
					newSerie(scope, myname);
					currentSeriesNames.add(i, myname);
				}
			}
		}
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
		int type_val = ChartDataSource.DATA_TYPE_NULL;
		if (this.getValue() != null) {
			o = this.getValue().value(scope);
			if (o instanceof IList && Cast.asList(scope, o).size() > 0) {
				final Object o2 = Cast.asList(scope, o).get(0);
				type_val = get_data_type(scope, o2);
			}

		}

		getDataset().getOutput().setDefaultPropertiesFromType(scope, this, type_val);

	}
}
