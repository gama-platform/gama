/*******************************************************************************************************
 *
 * LetStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.validation.Assert;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.gaml.statements.LetStatement.LetSerializer;
import gama.gaml.statements.LetStatement.LetValidator;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.NEW_TEMP_ID,
				optional = false,
				doc = @doc ("The name of the temporary variable")),
				@facet (
						name = IKeyword.VALUE,
						type = { IType.NONE },
						optional = /* AD change false */true,
						doc = @doc ("The value assigned to the temporary variable")),

				@facet (
						name = IKeyword.OF,
						type = { IType.TYPE_ID },
						optional = true,
						doc = @doc ("The type of the contents if this declaration concerns a container")),
				@facet (
						name = IKeyword.INDEX,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("The type of the index if this declaration concerns a container")),
				@facet (
						name = IKeyword.TYPE,
						type = { IType.TYPE_ID },
						optional = true,
						doc = @doc ("The type of the temporary variable")) },
		omissible = IKeyword.NAME)
@symbol (
		name = { IKeyword.LET },
		kind = ISymbolKind.SINGLE_STATEMENT,
		concept = { IConcept.SYSTEM },
		with_sequence = false)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@validator (LetValidator.class)
@serializer (LetSerializer.class)
@doc ("Declaration and initialization of a temporary variable.")
public class LetStatement extends SetStatement {

	/**
	 * The Class LetSerializer.
	 */
	public static class LetSerializer extends AssignmentSerializer {

		@Override
		public void serialize(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(desc.getGamlType().serializeToGaml(includingBuiltIn)).append(" ");
			super.serialize(desc, sb, includingBuiltIn);

		}

	}

	/**
	 * The Class LetValidator.
	 */
	public static class LetValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription cd) {
			if (Assert.nameIsValid(cd)) {
				final IExpressionDescription receiver = cd.getFacet(NAME);
				final IExpression expr = receiver.getExpression();
				if (!(expr instanceof IVarExpression)) {
					cd.error("The expression " + cd.getLitteral(NAME) + " is not a reference to a variable ", NAME);
					return;
				}
				final IExpressionDescription assigned = cd.getFacet(VALUE);
				if (assigned != null) {
					Assert.typesAreCompatibleForAssignment(VALUE, cd, StringUtils.toGaml(expr, false),
							expr.getGamlType(), assigned);
				}

			}
		}
	}

	/**
	 * Instantiates a new let statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public LetStatement(final IDescription desc) {
		super(desc);
		setName(IKeyword.LET + getVarName());
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final Object val = value.value(scope);
		varExpr.setVal(scope, val, true);
		return val;
	}

}
