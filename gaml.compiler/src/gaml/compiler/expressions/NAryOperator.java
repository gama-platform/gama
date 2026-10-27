/*******************************************************************************************************
 *
 * NAryOperator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import java.util.Arrays;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;

/**
 * The Class NAryOperator.
 */
public class NAryOperator extends AbstractNAryOperator {

	/**
	 * Creates the.
	 *
	 * @param artefact
	 *            the artefact
	 * @param child
	 *            the child
	 * @return the i expression
	 */
	public static IExpression create(final IArtefact.Operator proto, final IDescription context,
			final IExpression... child) {
		return new NAryOperator(proto, context, child).optimized();
	}

	/**
	 * Instantiates a new n ary operator.
	 *
	 * @param artefact
	 *            the artefact
	 * @param exprs
	 *            the exprs
	 */
	public NAryOperator(final IArtefact.Operator proto, final IDescription context, final IExpression... exprs) {
		super(proto, exprs);
		if (context != null && proto.isIterator()) {

			proto.verifyExpectedTypes(context, exprs[2].getGamlType());

		}
	}

	@Override
	public NAryOperator copy() {
		if (exprs == null) return new NAryOperator(prototype, null);
		return new NAryOperator(prototype, null, Arrays.copyOf(exprs, exprs.length));
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		if (!prototype.isIterator() || exprs == null
				|| exprs.length != 3 && !(prototype.isAccumulator() && exprs.length == 4))
			return super.serializeToGaml(includingBuiltIn);
		final StringBuilder sb = new StringBuilder();
		parenthesize(sb, exprs[1]);
		sb.append(' ').append(getName()).append(" (");
		if (exprs.length == 4) { sb.append(exprs[2].serializeToGaml(includingBuiltIn)).append(", "); }
		final String eachName = (String) exprs[0].value(null);
		if (!IKeyword.EACH.equals(eachName)) { sb.append(eachName).append(": "); }
		return sb.append(exprs[exprs.length - 1].serializeToGaml(includingBuiltIn)).append(')').toString();
	}

}
