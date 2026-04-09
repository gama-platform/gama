/*******************************************************************************************************
 *
 * GamaAnova.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.stat.inference.OneWayAnova;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IValue;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class GamaAnova.
 */
@vars ({ @variable (
		name = "p_value",
		type = IType.FLOAT,
		doc = { @doc ("The p-value of the ANOVA test") }),
		@variable (
				name = "f_stat",
				type = IType.FLOAT,
				doc = { @doc ("The F-statistic of the ANOVA test") }),
		@variable (
				name = "df_num",
				type = IType.INT,
				doc = { @doc ("Numerator degrees of freedom") }),
		@variable (
				name = "df_den",
				type = IType.INT,
				doc = { @doc ("Denominator degrees of freedom") }) })
public class GamaAnova implements IValue {

	/** The p value. */
	double pValue;

	/** The f stat. */
	double fStat;

	/** The degrees of freedom. */
	int dfNum, dfDen;

	/**
	 * Instantiates a new gama anova.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data (list of lists of numbers)
	 */
	public GamaAnova(final IScope scope, final IList<IList<?>> data) {
		OneWayAnova anova = new OneWayAnova();
		Collection<double[]> categoryData = new ArrayList<>();
		int totalN = 0;
		if (data != null) {
			for (final IList<?> list : data) {
				double[] d = new double[list.size()];
				for (int i = 0; i < list.size(); i++) { d[i] = Cast.asFloat(scope, list.get(i)); }
				categoryData.add(d);
				totalN += d.length;
			}
		}
		this.pValue = categoryData.isEmpty() ? 1.0 : anova.anovaPValue(categoryData);
		this.fStat = categoryData.isEmpty() ? 0.0 : anova.anovaFValue(categoryData);
		this.dfNum = categoryData.isEmpty() ? 0 : categoryData.size() - 1;
		this.dfDen = categoryData.isEmpty() ? 0 : totalN - categoryData.size();
	}

	/**
	 * Gets the p value.
	 *
	 * @return the p value
	 */
	@getter ("p_value")
	public double getPValue() { return pValue; }

	/**
	 * Gets the f stat.
	 *
	 * @return the f stat
	 */
	@getter ("f_stat")
	public double getFStat() { return fStat; }

	/**
	 * Gets the numerator degrees of freedom.
	 *
	 * @return the df num
	 */
	@getter ("df_num")
	public int getDfNum() { return dfNum; }

	/**
	 * Gets the denominator degrees of freedom.
	 *
	 * @return the df den
	 */
	@getter ("df_den")
	public int getDfDen() { return dfDen; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return stringValue(null);
	}

	@Override
	public IType<?> getGamlType() { return Types.get(IType.ANOVA); }

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "ANOVA(p=" + pValue + ", F=" + fStat + ")";
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		GamaAnova copy = new GamaAnova(null, GamaListFactory.create());
		copy.pValue = this.pValue;
		copy.fStat = this.fStat;
		return copy;
	}

	@Override
	public int intValue(final IScope scope) {
		return (int) fStat;
	}

	@Override
	public double floatValue(final IScope scope) {
		return pValue;
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "p_value", pValue, "f_stat", fStat);
	}

}
