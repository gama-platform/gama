/*******************************************************************************************************
 *
 * NAryOperator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.operators;

import java.util.Arrays;

import gama.gaml.descriptions.OperatorProto;
import gama.gaml.expressions.IExpression;

/**
 * The Class NAryOperator.
 */
public class NAryOperator extends AbstractNAryOperator {

	/**
	 * Creates the.
	 *
	 * @param proto
	 *            the proto
	 * @param child
	 *            the child
	 * @return the i expression
	 */
	public static IExpression create(final OperatorProto proto, final IExpression... child) {
		return new NAryOperator(proto, child).optimized();
	}

	/**
	 * Instantiates a new n ary operator.
	 *
	 * @param proto
	 *            the proto
	 * @param exprs
	 *            the exprs
	 */
	public NAryOperator(final OperatorProto proto, final IExpression... exprs) {
		super(proto, exprs);
	}

	@Override
	public NAryOperator copy() {
		if (exprs == null) return new NAryOperator(prototype);
		return new NAryOperator(prototype, Arrays.copyOf(exprs, exprs.length));
	}

}
