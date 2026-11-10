/*******************************************************************************************************
 *
 * IndexedExpressionDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.descriptions;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gaml.compiler.EGaml;
import gaml.compiler.expressions.ExpressionCompilationContext;
import gaml.compiler.expressions.ExpressionCompilationSwitch;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.VariableRef;

/** Resolves an assignment index against the receiver before looking up caller variables. */
public class IndexedExpressionDescription extends EcoreBasedExpressionDescription {

	private final Access access;

	public IndexedExpressionDescription(final Access access) {
		super(EGaml.getInstance().getExprsOf(access.getRight()).get(0));
		this.access = access;
	}

	@Override
	public IExpression compile(final IDescription description) {
		if (expression == null && target instanceof VariableRef reference) {
			try (ExpressionCompilationContext context = new ExpressionCompilationContext(description)) {
				final var compiler = new ExpressionCompilationSwitch(context);
				final IExpression container = compiler.compile(access.getLeft());
				if (container == null) return null;
				expression = compiler.speciesAttribute(container, reference, true);
			}
		}
		return expression == null ? super.compile(description) : expression;
	}

	@Override
	public IExpressionDescription cleanCopy() {
		return new IndexedExpressionDescription(access);
	}
}
