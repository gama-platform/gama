/*******************************************************************************************************
 *
 * MoleExperiment.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.openmole;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.IModelSpecies;
import gama.headless.core.Experiment;

/**
 * The Class MoleExperiment.
 */
public class MoleExperiment extends Experiment implements IMoleExperiment {

	/**
	 * Instantiates a new mole experiment.
	 *
	 * @param mdl
	 *            the mdl
	 */
	MoleExperiment(final IModelSpecies mdl) {
		super(mdl);
	}

	@Override
	public void play(final int finalStep) {
		while (finalStep < this.step()) {}
	}

	/**
	 * Play.
	 *
	 * @param finalCondition
	 *            the final condition
	 */
	public void play(final String finalCondition) {
		play(finalCondition, -1);
	}

	@Override
	public void play(final String exp, final int finalStep) {
		IExpression endCondition = this.compileExpression(exp);
		if (exp == null || "".equals(exp)) {
			endCondition = GAML.getExpressionFactory().getFalse();
		} else {
			endCondition = this.compileExpression(exp);
		}
		if (endCondition.getGamlType() != Types.BOOL)
			throw GamaRuntimeException.error("The until condition of the experiment should be a boolean", getScope());
		long step = 0;
		while (!Types.BOOL.cast(getScope(), this.evaluateExpression(endCondition), null, false).booleanValue()
				&& (finalStep >= 0 ? step < finalStep : true)) {
			step = this.step();
		}
	}

}
