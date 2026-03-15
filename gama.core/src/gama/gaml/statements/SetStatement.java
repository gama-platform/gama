/*******************************************************************************************************
 *
 * SetStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.compilation.validation.Assert;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.gaml.statements.SetStatement.AssignmentSerializer;
import gama.gaml.statements.SetStatement.AssignmentValidator;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@facets (
		value = { /*
					 * @facet(name = IKeyword.VAR, type = IType.NONE, optional = true),
					 */
				@facet (
						name = IKeyword.NAME,
						type = IType.NONE,
						optional = false,
						doc = @doc ("the name of an existing variable or attribute to be modified")),
				@facet (
						name = IKeyword.VALUE,
						type = { IType.NONE },
						optional = false,
						doc = @doc ("the value to affect to the variable or attribute")) },
		omissible = IKeyword.NAME)
@symbol (
		name = { IKeyword.SET },
		kind = ISymbolKind.SINGLE_STATEMENT,
		concept = { IConcept.ATTRIBUTE },
		with_sequence = false)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER },
		symbols = IKeyword.CHART)
@validator (AssignmentValidator.class)
@doc (
		value = "Allows to assign a value to the variable or attribute specified")
@serializer (AssignmentSerializer.class)
public class SetStatement extends AbstractStatement {

	/**
	 * The Class AssignmentSerializer.
	 */
	public static class AssignmentSerializer implements ISymbolSerializer {

		/**
		 * Serialize.
		 *
		 * @param desc
		 *            the desc
		 * @param sb
		 *            the sb
		 * @param includingBuiltIn
		 *            the including built in
		 */
		@Override
		public void serialize(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			if (desc == null) return;
			final IExpressionDescription ed = desc.getFacet(VALUE);
			if (ed == null) return;
			final String exp = ed.serializeToGaml(includingBuiltIn);
			if (exp == null) return;
			sb.append(desc.getName());
			sb.append(" <- ");
			sb.append(exp);
			sb.append(";");
		}

	}

	/**
	 * The Class AssignmentValidator.
	 */
	public static class AssignmentValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription cd) {
			final IExpressionDescription receiver = cd.getFacet(NAME);
			// String name = cd.getName();
			final IExpression expr = receiver.getExpression();
			if (!(expr instanceof IVarExpression var)) {
				cd.error("The expression " + cd.getLitteral(NAME) + " is not a reference to a variable ", NAME);
				return;
			}
			final IExpressionDescription assigned = cd.getFacet(VALUE);
			if (assigned != null) {
				Assert.typesAreCompatibleForAssignment(VALUE, cd, StringUtils.toGaml(expr, false), expr.getGamlType(),
						assigned);
			}

			// AD 19/1/13: test of the constants
			if (var.getVar().isNotModifiable()) {
				cd.error("The variable " + expr.serializeToGaml(false)
						+ " is a constant or a function and cannot be assigned a value.", IKeyword.NAME);
			}

			if (IKeyword.SHAPE.equals(var.getName()) && cd.getTypeContext() instanceof IModelDescription) {
				cd.warning(
						"Dynamically changing the shape of the world can lead to unexpected results. It is advised to redefine the attribute instead (e.g. 'geometry shape <- "
								+ (assigned == null ? "..." : assigned.serializeToGaml(false)) + "')",
						IKeyword.NAME);
			}

		}
	}

	/** The var expr. */
	protected final IVarExpression varExpr;

	/** The value. */
	protected final IExpression value;

	/**
	 * Instantiates a new sets the statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public SetStatement(final IDescription desc) {
		super(desc);
		varExpr = (IVarExpression) getFacet(IKeyword.NAME);
		setName(IKeyword.SET + getVarName());
		final IExpression expr = getFacet(IKeyword.VALUE);
		if (expr == null) {
			value = GAML.getExpressionFactory().createConst(varExpr.getGamlType().getDefault(), varExpr.getGamlType());
		} else {
			value = expr;
		}

	}

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final Object val = value.value(scope);
		varExpr.setVal(scope, val, false);
		return val;
	}

	/**
	 * Gets the var name.
	 *
	 * @return the var name
	 */
	public String getVarName() {
		if (varExpr != null) return varExpr.literalValue();
		return null;
	}

}
