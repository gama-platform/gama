/*******************************************************************************************************
 *
 * GillSolver.java, in gaml.extensions.maths, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.maths.ode.utils.solver;

import org.apache.commons.math3.ode.nonstiff.GillIntegrator;

import gama.core.util.IList;
import gama.core.util.IMap;

/**
 * The Class GillSolver.
 */
public class GillSolver extends Solver {

	/**
	 * Instantiates a new gill solver.
	 *
	 * @param step the step
	 * @param integrated_val the integrated val
	 */
	public GillSolver(final double step, final IMap<String, IList<Double>> integrated_val) {
		super(step, new GillIntegrator(step), integrated_val);
	}

}
