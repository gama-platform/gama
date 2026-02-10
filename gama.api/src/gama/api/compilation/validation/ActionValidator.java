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
 * Validator for action descriptions that ensures proper return type compliance.
 * 
 * <p>This validator performs two main checks on action descriptions:</p>
 * <ol>
 *   <li>Validates that the action name is valid according to GAML naming conventions</li>
 *   <li>Ensures that all return statements within the action return values compatible with the declared return type</li>
 * </ol>
 * 
 * <p>The validation process includes:</p>
 * <ul>
 *   <li>Verifying that non-abstract actions with a declared return type contain at least one return statement</li>
 *   <li>Checking that returned values are type-compatible with the declared return type</li>
 *   <li>Handling special cases for nil values and int/float type casting (Issue #3059)</li>
 *   <li>Automatically inserting type cast expressions when implicit casting is needed</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> The current implementation performs a simple validation and does not verify
 * that every execution branch returns a value (see FIXME in {@link #assertReturnedValueIsOk(IDescription)}).</p>
 * 
 * @see IDescriptionValidator
 * @see IDescription
 */
public class ActionValidator implements IDescriptionValidator<IDescription> {

	/**
	 * Validates an action description by checking name validity and return type compliance.
	 * 
	 * <p>This method performs a two-step validation process:</p>
	 * <ol>
	 *   <li>Validates the action name using {@link Assert#nameIsValid(IDescription)}</li>
	 *   <li>If the name is valid, validates return type consistency via {@link #assertReturnedValueIsOk(IDescription)}</li>
	 * </ol>
	 * 
	 * <p>If the name is invalid, validation stops early and return type validation is skipped.</p>
	 *
	 * @param description the action description to validate (must not be null)
	 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
	 */
	@Override
	public void validate(final IDescription description) {
		if (!Assert.nameIsValid(description)) return;
		assertReturnedValueIsOk(description);
	}

	/**
	 * Validates that return statements in the action provide values compatible with the declared return type.
	 * 
	 * <p>This method performs comprehensive return type validation:</p>
	 * 
	 * <h3>Validation Steps:</h3>
	 * <ol>
	 *   <li><strong>Type Check:</strong> If the action has no declared return type ({@link Types#NO_TYPE}), validation is skipped</li>
	 *   <li><strong>Return Statement Collection:</strong> Recursively visits all child descriptions to find return statements</li>
	 *   <li><strong>Missing Return Check:</strong> Non-abstract actions must have at least one return statement</li>
	 *   <li><strong>Value Validation:</strong> Each return value is checked for type compatibility</li>
	 * </ol>
	 * 
	 * <h3>Error Conditions:</h3>
	 * <ul>
	 *   <li><strong>Missing Return ({@link IGamlIssue#MISSING_RETURN}):</strong> Non-abstract action with return type has no return statements</li>
	 *   <li><strong>Wrong Type ({@link IGamlIssue#WRONG_TYPE}):</strong> Returning 'nil' when the type has no default value</li>
	 *   <li><strong>Should Cast ({@link IGamlIssue#SHOULD_CAST}):</strong> Return value type cannot be translated to declared type</li>
	 * </ul>
	 * 
	 * <h3>Special Cases:</h3>
	 * <ul>
	 *   <li><strong>Nil Values:</strong> Accepted only if the return type has a default value</li>
	 *   <li><strong>Int/Float Casting:</strong> Automatic cast insertion with warning for int↔float conversions (Issue #3059)</li>
	 *   <li><strong>Content Type Casting:</strong> Also checks and casts container content types (e.g., list&lt;int&gt; → list&lt;float&gt;)</li>
	 * </ul>
	 * 
	 * <h3>Optimizations:</h3>
	 * <p>This method caches frequently accessed values to minimize redundant method calls:</p>
	 * <ul>
	 *   <li>Expression factory reference</li>
	 *   <li>Nil expression instance</li>
	 *   <li>Action name</li>
	 *   <li>Content types when checking int/float cases</li>
	 * </ul>
	 * 
	 * <p><strong>Known Limitation:</strong> This validation does not verify that every execution path
	 * (branches in if/else, switch, etc.) returns a value. Only the presence of return statements
	 * is validated, not their reachability or completeness across all code paths.</p>
	 *
	 * @param cd the action description to validate (must not be null)
	 */
	private void assertReturnedValueIsOk(final IDescription cd) {
		final IType at = cd.getGamlType();
		if (at == Types.NO_TYPE) return;
		
		final var expressionFactory = getExpressionFactory();
		final IExpression nilExpression = expressionFactory.getNil();
		final String actionName = cd.getName();
		
		try (final ICollector<IDescription> returns = getOrderedSet()) {
			final DescriptionVisitor<IDescription> finder = desc -> {
				if (RETURN.equals(desc.getKeyword())) { returns.add(desc); }
				return true;
			};
			cd.visitOwnChildrenRecursively(finder);
			if (returns.isEmpty() && !cd.isAbstract()) {
				cd.error("Action " + actionName + " must return a result of type " + at, IGamlIssue.MISSING_RETURN);
				return;
			}
			for (final IDescription ret : returns) {
				final IExpression ie = ret.getFacetExpr(VALUE);
				if (ie == null) continue;
				
				if (ie.equals(nilExpression)) {
					if (at.getDefault() == null) continue;
					ret.error("'nil' is not an acceptable return value. A valid " + at + " is expected instead.",
							IGamlIssue.WRONG_TYPE, VALUE);
				} else {
					final IType<?> rt = ie.getGamlType();
					if (!rt.isTranslatableInto(at)) {
						ret.error("Action " + actionName + " must return a result of type " + at + " (and not " + rt
								+ ")", IGamlIssue.SHOULD_CAST, VALUE, at.toString());
					} else {
						final IType<?> rtContent = rt.getContentType();
						final IType<?> atContent = at.getContentType();
						if (intFloatCase(rt, at) || intFloatCase(rtContent, atContent)) {
							// See Issue #3059
							ret.warning("The returned value (of type " + rt + ") will be casted to " + at,
									IGamlIssue.WRONG_TYPE, VALUE);
							ret.setFacet(VALUE, expressionFactory.createAs(cd.getSpeciesContext(), ie,
									expressionFactory.createTypeExpression(at)));
						}
					}
				}
			}
		}
		// FIXME This assertion is still simple (i.e. the tree is not
		// verified to ensure that every
		// branch returns something)
	}

}