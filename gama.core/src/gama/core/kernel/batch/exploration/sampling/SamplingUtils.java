/*******************************************************************************************************
 *
 * SamplingUtils.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.sampling;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import gama.core.kernel.experiment.parameters.IParameter.Batch;
import gama.core.kernel.experiment.parameters.ParametersSet;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IDate;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaDateType;
import gama.gaml.types.IType;

/**
 *
 * @author tomroy
 *
 */
/**
 *
 * This abstract class gives methods for all sampling methods.
 *
 */
public abstract class SamplingUtils {

	/**
	 * Instantiates a new sampling utils.
	 */
	public SamplingUtils() {}

	/**
	 * This scale the sampling value for discrete values
	 *
	 * @param scope
	 * @param set
	 * @param var
	 * @param valFromSampling
	 * @return
	 */
	private static ParametersSet scaleSamplingAmongValue(final IScope scope, final ParametersSet set, final Batch var,
			final double valFromSampling) {
		int size = var.getAmongValue(scope).size();
		int idx = IntStream.range(1, size).filter(i -> valFromSampling <= 1d * i / size).findFirst().orElse(size);
		set.put(var.getName(), var.getAmongValue(scope).get(idx - 1));
		return set;
	}

	/**
	 * This function scale the sampling value
	 *
	 * @param scope
	 * @param set
	 * @param var
	 * @param ValFromSampling
	 * @return
	 */
	private static ParametersSet scaleSampling(final IScope scope, final ParametersSet set, final Batch var, final double ValFromSampling) {
		switch (var.getType().id()) {
			case IType.INT:
				int intValue = Cast.asInt(scope, var.getMinValue(scope));
				int maxIntValue = Cast.asInt(scope, var.getMaxValue(scope));
				int sampleIValue = (int) Math.round(intValue + ValFromSampling * (maxIntValue - intValue));
				set.put(var.getName(), sampleIValue);
				return set;
			case IType.FLOAT:
				double floatValue = Cast.asFloat(scope, var.getMinValue(scope));
				double maxFloatValue = Cast.asFloat(scope, var.getMaxValue(scope));
				double sampleFValue = floatValue + ValFromSampling * (maxFloatValue - floatValue);
				set.put(var.getName(), sampleFValue);
				return set;
			case IType.DATE:
				IDate dateValue = GamaDateType.staticCast(scope, var.getMinValue(scope), null, false);
				IDate maxDateValue = GamaDateType.staticCast(scope, var.getMaxValue(scope), null, false);
				IDate sampleDValue = dateValue.plus(
						dateValue.getTemporal().until(maxDateValue, ChronoUnit.SECONDS) * ValFromSampling,
						ChronoUnit.SECONDS);
				set.put(var.getName(), sampleDValue);
				return set;
			case IType.POINT:
				GamaPoint pointValue = Cast.asPoint(scope, var.getMinValue(scope));
				GamaPoint maxPointValue = Cast.asPoint(scope, var.getMaxValue(scope));
				double samplePXValue = pointValue.getX() + ValFromSampling * (maxPointValue.getX() - pointValue.getX());
				double samplePYValue = pointValue.getY() + ValFromSampling * (maxPointValue.getY() - pointValue.getY());
				double samplePZValue = pointValue.getZ() + ValFromSampling * (maxPointValue.getZ() - pointValue.getZ());
				set.put(var.getName(), new GamaPoint(samplePXValue, samplePYValue, samplePZValue));
				return set;
			case IType.BOOL:
				set.put(var.getName(), (ValFromSampling > 0.5) == true);
				return set;
			case IType.STRING:
				if (var.getAmongValue(scope).isEmpty()) throw GamaRuntimeException
						.error("Trying to force a string variable in sampling without among facets", scope);
				int ms = var.getAmongValue(scope).size();
				int sv = (int) Math.round(ValFromSampling * ms);
				set.put(var.getName(), var.getAmongValue(scope).get(sv));
				return set;
			default:
				throw GamaRuntimeException.error(
						"Trying to add a variable of unknown type " + var.getType().asPattern() + " to a parameter set",
						scope);
		}
	}

	/**
	 * Build the parameters set from samples between 0 and 1, and scale it between min and max value of the parameters
	 *
	 * @param scope
	 * @param parameters
	 * @param SamplingData
	 * @return
	 */
	public static List<ParametersSet> buildParametersSetfromSample(final IScope scope, final List<Batch> parameters,
			final List<Map<String, Double>> SamplingData) {
		List<ParametersSet> ParameterSet = new ArrayList<>();
		for (int i = 0; i < SamplingData.size(); i++) {
			ParametersSet origi = new ParametersSet();
			for (Batch parameter : parameters) {
				if (parameter.getAmongValue(scope) != null) {
					origi = scaleSamplingAmongValue(scope, origi, parameter,
							SamplingData.get(i).get(parameter.getName()));
				} else {
					origi = scaleSampling(scope, origi, parameter, SamplingData.get(i).get(parameter.getName()));
				}
			}
			ParameterSet.add(origi);
		}
		return ParameterSet;
	}
}
