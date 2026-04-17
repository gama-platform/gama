/*******************************************************************************************************
 *
 * GamaRegression.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats.analysis;

import java.util.Arrays;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.RegressionResults;

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
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IValue;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class GamaRegression.
 */
@vars ({ @variable (
		name = "parameters",
		type = IType.LIST,
		of = IType.FLOAT,
		doc = { @doc ("List of regression coefficients (float) - same order as the variable in the input matrix ") }),
		@variable (
				name = "std_errors",
				type = IType.LIST,
				of = IType.FLOAT,
				doc = { @doc ("Standard errors of the coefficients") }),
		@variable (
				name = "p_values",
				type = IType.LIST,
				of = IType.FLOAT,
				doc = { @doc ("P-values (t-test) for the coefficients") }),
		@variable (
				name = "nb_features",
				type = IType.INT,
				doc = { @doc ("number of variables") }),
		@variable (
				name = "RSquare",
				type = IType.FLOAT,
				doc = { @doc ("Estimated pearson's R-squared statistic") }),
		@variable (
				name = "residuals",
				type = IType.LIST,
				of = IType.FLOAT,
				doc = { @doc ("error terms associated to each observation of the sample") }) })
public class GamaRegression implements IValue {

	/** The regression results. */
	RegressionResults regressionResults;

	/** The nb features. */
	int nbFeatures;

	/** The param. */
	double param[];

	/** The standard errors. */
	double stdErrors[];

	/** The p-values. */
	double pValues[];

	/** The error. */
	double error[];

	/** The rsquare. */
	double rsquare;

	/**
	 * Instantiates a new gama regression.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @throws Exception
	 *             the exception
	 */
	public GamaRegression(final IScope scope, final IMatrix<?> data) throws Exception {
		final OLSMultipleLinearRegression regressionMethod = new OLSMultipleLinearRegression();
		final int nbFeatures = data.getCols(scope) - 1;
		final int nbInstances = data.getRows(scope);

		final double[] instances = new double[data.getCols(scope) * data.getRows(scope)];

		for (int i = 0; i < data.length(scope); i++) { instances[i] = Cast.asFloat(scope, data.getNthElement(i)); }
		regressionMethod.newSampleData(instances, nbInstances, nbFeatures);
		param = regressionMethod.estimateRegressionParameters();
		rsquare = regressionMethod.calculateAdjustedRSquared();
		error = regressionMethod.estimateResiduals();

		try {
			stdErrors = regressionMethod.estimateRegressionParametersStandardErrors();
			pValues = new double[param.length];
			int df = nbInstances - param.length;
			if (df > 0) {
				TDistribution tDist = new TDistribution(df);
				for (int i = 0; i < param.length; i++) {
					double t = Math.abs(param[i] / stdErrors[i]);
					pValues[i] = 2.0 * (1.0 - tDist.cumulativeProbability(t));
				}
			} else {
				Arrays.fill(pValues, Double.NaN);
			}
		} catch (Exception e) {
			stdErrors = new double[param.length];
			pValues = new double[param.length];
			Arrays.fill(stdErrors, Double.NaN);
			Arrays.fill(pValues, Double.NaN);
		}
	}

	/**
	 * Instantiates a new gama regression.
	 *
	 * @param param
	 *            the param
	 * @param nbFeatures
	 *            the nb features
	 * @param regressionResults
	 *            the regression results
	 * @param stdErrors
	 *            the std errors
	 * @param pValues
	 *            the p values
	 * @param rsquare
	 *            the rsquare
	 * @param error
	 *            the residuals
	 */
	public GamaRegression(final double[] param, final int nbFeatures, final RegressionResults regressionResults,
			final double[] stdErrors, final double[] pValues, final double rsquare, final double[] error) {
		this.regressionResults = regressionResults;
		this.nbFeatures = nbFeatures;
		this.param = param;
		this.stdErrors = stdErrors;
		this.pValues = pValues;
		this.rsquare = rsquare;
		this.error = error;
	}

	/**
	 * Predict.
	 *
	 * @param scope
	 *            the scope
	 * @param instance
	 *            the instance
	 * @return the double
	 */
	public Double predict(final IScope scope, final IList<?> instance) {
		if (param == null) return null;
		double val = param[0];
		for (int i = 1; i < param.length; i++) { val += param[i] * Cast.asFloat(scope, instance.get(i - 1)); }
		return val;
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	@getter ("parameters")
	public IList<Double> getParameters() {
		if (param == null) return GamaListFactory.create(Types.FLOAT);
		final IList<Double> vals = GamaListFactory.create(Types.FLOAT);
		for (final double element : param) { vals.add(element); }
		return vals;
	}

	/**
	 * Gets the standard errors.
	 *
	 * @return the standard errors
	 */
	@getter ("std_errors")
	public IList<Double> getStdErrors() {
		final IList<Double> vals = GamaListFactory.create(Types.FLOAT);
		if (stdErrors != null) { for (final double element : stdErrors) { vals.add(element); } }
		return vals;
	}

	/**
	 * Gets the p-values.
	 *
	 * @return the p-values
	 */
	@getter ("p_values")
	public IList<Double> getPValues() {
		final IList<Double> vals = GamaListFactory.create(Types.FLOAT);
		if (pValues != null) { for (final double element : pValues) { vals.add(element); } }
		return vals;
	}

	/**
	 * Gets the residuals.
	 *
	 * @return the residuals
	 */
	@getter ("residuals")
	public IList<Double> getResiduals() {
		IList<Double> res = GamaListFactory.create(Types.FLOAT);
		if (error != null) { for (double e : error) { res.add(e); } }
		return res;
	}

	/**
	 * Gets the r square.
	 *
	 * @return the r square
	 */
	@getter ("RSquare")
	public double getRSquare() { return rsquare; }

	/**
	 * Gets the nb features.
	 *
	 * @return the nb features
	 */
	@getter ("nb_features")
	public Integer getNbFeatures() { return nbFeatures; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return stringValue(null);
	}

	@Override
	public IType<?> getGamlType() { return Types.get(IType.REGRESSION); }

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		if (param == null) return "no function";
		StringBuilder st = new StringBuilder("y = ").append(param[0]);
		for (int i = 1; i < param.length; i++) { st.append(" + ").append(param[i]).append(" x").append(i); }
		return st.toString();
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new GamaRegression(param == null ? null : param.clone(), nbFeatures, regressionResults,
				stdErrors == null ? null : stdErrors.clone(), pValues == null ? null : pValues.clone(), rsquare,
				error == null ? null : error.clone());
	}

	@Override
	public int intValue(final IScope scope) {
		return this.nbFeatures;
	}

	@Override
	public double floatValue(final IScope scope) {
		return getRSquare();
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType()).add("nb_features", nbFeatures).add("parameters", json.array(param))
				.add("RSquare", rsquare).add("residuals", json.array(error)).add("std_errors", json.array(stdErrors))
				.add("p_values", json.array(pValues));
	}

}
