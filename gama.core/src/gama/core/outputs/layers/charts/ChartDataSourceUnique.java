/*******************************************************************************************************
 *
 * ChartDataSourceUnique.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.charts;

import java.util.HashMap;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Random;
import gama.gaml.types.Types;

/**
 * The Class ChartDataSourceUnique.
 */
public class ChartDataSourceUnique extends ChartDataSource {

	/** The myname. */
	private String legend;

	@Override
	public boolean cloneMe(final IScope scope, final int chartCycle, final ChartDataSource source) {
		final boolean res = super.cloneMe(scope, chartCycle, source);
		final GamaColor col =
				GamaColor.get(Random.opRnd(scope, 255), Random.opRnd(scope, 255), Random.opRnd(scope, 255), 255);
		final IExpression ncol = GAML.getExpressionFactory().createConst(col, Types.COLOR);
		this.colorexp = ncol;
		final String previousname = ((ChartDataSourceUnique) source).legend;
		legend = previousname + "_1*";
		if (previousname.endsWith("*")) {
			final int index = previousname.lastIndexOf('_');
			final String nosim = previousname.substring(index + 1, previousname.lastIndexOf('*'));
			int nosimv = Cast.asInt(scope, nosim);
			final String basename = previousname.substring(0, index);
			nosimv = nosimv + 1;
			legend = basename + "_" + nosimv + "*";
		}

		return res;
	}

	@Override
	public ChartDataSource getClone(final IScope scope, final int chartCycle) {
		final ChartDataSourceUnique res = new ChartDataSourceUnique();
		res.cloneMe(scope, chartCycle, this);
		return res;
	}

	/**
	 * Gets the myserie.
	 *
	 * @return the myserie
	 */
	public ChartDataSeries getMyserie() { return mySeries.get(legend); }

	/**
	 * Sets the legend.
	 *
	 * @param scope
	 *            the scope
	 * @param stval
	 *            the stval
	 */
	public void setLegend(final IScope scope, final String stval) {
		legend = stval;
	}

	@Override
	public void updatevalues(final IScope scope, final int chartCycle) {
		super.updatevalues(scope, chartCycle);
		Object o = null;
		final HashMap<String, Object> barvalues = new HashMap<>();
		if (this.isUseYErrValues()) { barvalues.put(ChartDataStatement.YERR_VALUES, getValueyerr().value(scope)); }
		if (this.isUseXErrValues()) { barvalues.put(ChartDataStatement.XERR_VALUES, getValueyerr().value(scope)); }
		if (this.isUseYMinMaxValues()) { barvalues.put(ChartDataStatement.XERR_VALUES, getValuexerr().value(scope)); }
		if (this.isUseSizeExp()) { barvalues.put(ChartDataStatement.MARKERSIZE, getSizeexp().value(scope)); }
		if (this.isUseColorExp()) { barvalues.put(IKeyword.COLOR, getColorexp().value(scope)); }
		if (getValue() != null) { o = getValue().value(scope); }
		if (o != null) { updateseriewithvalue(scope, getMyserie(), o, chartCycle, barvalues, -1); }
	}

	/**
	 * Infer dataset properties.
	 *
	 * @param scope
	 *            the scope
	 * @param myserie
	 *            the myserie
	 */
	public void inferDatasetProperties(final IScope scope, final ChartDataSeries myserie) {
		Object o = null;
		if (this.getValue() != null) { o = this.getValue().value(scope); }
		final int type_val = get_data_type(scope, o);
		getDataset().getOutput().setDefaultPropertiesFromType(scope, this, type_val);

	}

	@Override
	public void createInitialSeries(final IScope scope) {
		final ChartDataSeries myserie = new ChartDataSeries();
		myserie.setMysource(this);
		myserie.setDataset(getDataset());
		inferDatasetProperties(scope, myserie);
		myserie.setName(legend);
		mySeries.put(legend, myserie);
	}

}
