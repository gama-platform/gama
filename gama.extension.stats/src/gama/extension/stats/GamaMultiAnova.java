/*******************************************************************************************************
 *
 * GamaMultiAnova.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats;

import java.util.Map;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.misc.IValue;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class GamaMultiAnova.
 */
@vars ({ @variable (
		name = "p_values",
		type = IType.MAP,
		doc = { @doc ("The p-values for each effect (main effects and interactions)") }),
		@variable (
				name = "f_stats",
				type = IType.MAP,
				doc = { @doc ("The F-statistics for each effect") }) })
public class GamaMultiAnova implements IValue {

	/** The p values. */
	IMap<String, Double> pValues = GamaMapFactory.create(Types.STRING, Types.FLOAT);

	/** The f stats. */
	Map<String, Double> fStats = GamaMapFactory.create(Types.STRING, Types.FLOAT);

	/**
	 * Instantiates a new gama multi anova.
	 */
	public GamaMultiAnova() {}

	/**
	 * Adds an effect.
	 *
	 * @param name
	 *            the effect name
	 * @param p
	 *            the p-value
	 * @param f
	 *            the f-stat
	 */
	public void addEffect(final String name, final double p, final double f) {
		pValues.put(name, p);
		fStats.put(name, f);
	}

	/**
	 * Gets the p values.
	 *
	 * @return the p values
	 */
	@getter ("p_values")
	public Map<String, Double> getPValues() { return pValues; }

	/**
	 * Gets the f stats.
	 *
	 * @return the f stats
	 */
	@getter ("f_stats")
	public Map<String, Double> getFStats() { return fStats; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return stringValue(null);
	}

	@Override
	public IType<?> getGamlType() { return Types.get(IType.ANOVA); } // Reuse ANOVA type for now

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "MultiAnova" + pValues.toString();
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		GamaMultiAnova copy = new GamaMultiAnova();
		copy.pValues.putAll(this.pValues);
		copy.fStats.putAll(this.fStats);
		return copy;
	}

	@Override
	public int intValue(final IScope scope) { return 0; }

	@Override
	public double floatValue(final IScope scope) {
		return pValues.isEmpty() ? 0.0 : pValues.values().iterator().next();
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "p_values", json.valueOf(pValues), "f_stats", json.valueOf(fStats));
	}

}
