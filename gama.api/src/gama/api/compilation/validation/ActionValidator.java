/*******************************************************************************************************
 *
 * ActionValidator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import static gama.api.gaml.GAML.getExpressionFactory;
import static gama.api.gaml.types.Types.intFloatCase;
import static gama.api.utils.collections.Collector.getOrderedSet;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescription.DescriptionVisitor;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.utils.collections.ICollector;

/**
 * The Class ActionValidator.
 */
public class ActionValidator implements IDescriptionValidator<IDescription> {

	/**
	 * Method validate()
	 *
	 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
	 */
	@Override
	public void validate(final IDescription description) {
		if (Assert.nameIsValid(description)) { assertReturnedValueIsOk(description); }

	}

	/**
	 * Assert returned value is ok.
	 *
	 * @param cd
	 *            the cd
	 */
	private void assertReturnedValueIsOk(final IDescription cd) {
		final IType at = cd.getGamlType();
		if (at == Types.NO_TYPE) return;
		try (final ICollector<IDescription> returns = getOrderedSet()) {
			final DescriptionVisitor<IDescription> finder = desc -> {
				if (RETURN.equals(desc.getKeyword())) { returns.add(desc); }
				return true;
			};
			cd.visitOwnChildrenRecursively(finder);
			if (returns.isEmpty() && !cd.isAbstract()) {
				cd.error("Action " + cd.getName() + " must return a result of type " + at, IGamlIssue.MISSING_RETURN);
				return;
			}
			for (final IDescription ret : returns) {
				final IExpression ie = ret.getFacetExpr(VALUE);
				if (ie == null) { continue; }
				if (ie.equals(GAML.getExpressionFactory().getNil())) {
					if (at.getDefault() == null) { continue; }
					ret.error("'nil' is not an acceptable return value. A valid " + at + " is expected instead.",
							IGamlIssue.WRONG_TYPE, VALUE);
				} else {
					final IType<?> rt = ie.getGamlType();
					if (!rt.isTranslatableInto(at)) {
						ret.error("Action " + cd.getName() + " must return a result of type " + at + " (and not " + rt
								+ ")", IGamlIssue.SHOULD_CAST, VALUE, at.toString());
					} else if (intFloatCase(rt, at) || intFloatCase(rt.getContentType(), at.getContentType())) {
						// See Issue #3059
						ret.warning("The returned value (of type " + rt + ") will be casted to " + at,
								IGamlIssue.WRONG_TYPE, VALUE);
						ret.setFacet(VALUE, getExpressionFactory().createAs(cd.getSpeciesContext(), ie,
								getExpressionFactory().createTypeExpression(at)));
					}
				}
			}
		}
		// FIXME This assertion is still simple (i.e. the tree is not
		// verified to ensure that every
		// branch returns something)
	}

}