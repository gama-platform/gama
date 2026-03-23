/*******************************************************************************************************
 *
 * BlockExpressionDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gaml.compiler.gaml.expression.DenotedActionExpression;

/**
 * The Class BlockExpressionDescription.
 */
public class BlockExpressionDescription extends EcoreBasedExpressionDescription {

	/** The element. */
	final ISyntacticElement element;

	/**
	 * Instantiates a new block expression description.
	 *
	 * @param element
	 *            the element
	 */
	public BlockExpressionDescription(final ISyntacticElement element) {
		super(element.getElement());
		this.element = element;
	}

	@Override
	public IExpression compile(final IDescription context) {
		final ITypeDescription sd = context.getTypeContext();
		// if (sd.isExperiment())
		// sd = sd.getModelDescription();
		final IStatementDescription action =
				(IStatementDescription) GAML.getDescriptionFactory().create(element, sd, null);
		if (action != null) {
			sd.addChild(action);
			action.validate();
			// final String name = action.getName();
			expression = new DenotedActionExpression(action);
		}
		return expression;
	}

	@Override
	public IExpressionDescription cleanCopy() {
		return new BlockExpressionDescription(element);
	}

}
