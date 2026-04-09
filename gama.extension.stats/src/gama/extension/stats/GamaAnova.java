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
				doc = { @doc ("The F-statistic of the ANOVA test") }) })
public class GamaAnova implements IValue {

	/** The p value. */
	double pValue;

	/** The f stat. */
	double fStat;

	/**
	 * Instantiates a new gama anova.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data (list of lists of numbers)
	 */
	public GamaAnova(final IScope scope, final List<List<?>> data) {
		OneWayAnova anova = new OneWayAnova();
		Collection<double[]> categoryData = new ArrayList<>();
		for (List<?> list : data) {
			double[] d = new double[list.size()];
			for (int i = 0; i < list.size(); i++) { d[i] = Cast.asFloat(scope, list.get(i)); }
			categoryData.add(d);
		}
		this.pValue = anova.anovaPValue(categoryData);
		this.fStat = anova.anovaFValue(categoryData);
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
		GamaAnova copy = new GamaAnova(null, new ArrayList<>());
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
