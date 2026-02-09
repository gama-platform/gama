/*******************************************************************************************************
 *
 * IOperator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.expressions;

import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.utils.benchmark.IBenchmarkable;

/**
 * Written by drogoul Modified on 22 ao�t 2010
 *
 * @todo Description
 *
 */
public interface IOperator extends IExpression, IBenchmarkable {

	/**
	 * The Interface IOperatorVisitor.
	 */
	@FunctionalInterface
	public interface IOperatorVisitor {

		/**
		 * Visit.
		 *
		 * @param operator
		 *            the operator
		 */
		void visit(IOperator operator);
	}

	/**
	 * Visit suboperators.
	 *
	 * @param visitor
	 *            the visitor
	 */
	void visitSuboperators(IOperatorVisitor visitor);

	/**
	 * Arg.
	 *
	 * @param i
	 *            the i
	 * @return the i expression
	 */
	IExpression arg(int i);

	/**
	 * Gets the prototype.
	 *
	 * @return the prototype
	 */
	IArtefactProto getPrototype();

	/**
	 * Gets the name for benchmarks.
	 *
	 * @return the name for benchmarks
	 */
	@Override
	default String getNameForBenchmarks() { return serializeToGaml(true); }

}