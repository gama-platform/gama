/*******************************************************************************************************
 *
 * IDescriptionValidator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.validation.IValidator;
import gama.api.gaml.expressions.IExpression;

/**
 * Validator interface for custom semantic validation of GAML symbol descriptions.
 * 
 * <p>
 * This interface defines the contract for validators that perform specialized semantic validation
 * of symbol descriptions after the core validation has completed. Validators are typically associated
 * with specific symbol types via the {@code @validator} annotation and are called during compilation
 * to check for domain-specific correctness, consistency, and constraints.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * Description validators enable:
 * </p>
 * <ul>
 *   <li><strong>Custom Validation Rules:</strong> Domain-specific checks beyond standard syntax validation</li>
 *   <li><strong>Cross-facet Validation:</strong> Checking consistency between multiple facets</li>
 *   <li><strong>Constraint Verification:</strong> Validating value ranges, type compatibility, etc.</li>
 *   <li><strong>Warning Generation:</strong> Flagging potentially problematic but syntactically valid code</li>
 *   <li><strong>Facet Transformation:</strong> Modifying or normalizing facet values during validation</li>
 * </ul>
 * 
 * <h2>Validation Lifecycle</h2>
 * 
 * <p>
 * The validator is called at the end of the validation process, after:
 * </p>
 * <ol>
 *   <li>The enclosing description has been validated</li>
 *   <li>All children have been validated</li>
 *   <li>All facets have been validated and their expressions compiled</li>
 * </ol>
 * 
 * <p>
 * This ensures that all components of the description are available and valid when the
 * custom validator runs.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Implementing a Validator:</h3>
 * <pre>{@code
 * public class MyStatementValidator implements IDescriptionValidator<StatementDescription> {
 *     
 *     @Override
 *     public void validate(StatementDescription desc) {
 *         // Check required facets
 *         if (!desc.hasFacet("required_param")) {
 *             desc.error("'required_param' facet is mandatory", IGamlIssue.MISSING_FACET);
 *         }
 *         
 *         // Validate facet values
 *         IExpressionDescription sizeExpr = desc.getFacet("size");
 *         if (sizeExpr != null && sizeExpr.getExpression().isConst()) {
 *             Object value = sizeExpr.getExpression().literalValue();
 *             if ((Integer) value <= 0) {
 *                 desc.warning("Size should be positive", IGamlIssue.WRONG_TYPE);
 *             }
 *         }
 *         
 *         // Validate cross-facet dependencies
 *         if (desc.hasFacet("min") && desc.hasFacet("max")) {
 *             // Ensure min < max
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h3>Registering a Validator:</h3>
 * <pre>{@code
 * @validator(MyStatementValidator.class)
 * @symbol(name = "my_statement", kind = ISymbolKind.SINGLE_STATEMENT, ...)
 * public class MyCustomStatement extends AbstractStatement {
 *     // Statement implementation
 * }
 * }</pre>
 * 
 * <h2>Implementation Guidelines</h2>
 * 
 * <ul>
 *   <li>Use {@code description.error()} for fatal validation errors</li>
 *   <li>Use {@code description.warning()} for non-fatal issues</li>
 *   <li>Use {@code description.info()} for informational messages</li>
 *   <li>Access facets via {@code description.getFacet()} or {@code description.getFacetExpr()}</li>
 *   <li>Provide clear, actionable error messages</li>
 *   <li>Use appropriate {@code IGamlIssue} constants for error categorization</li>
 *   <li>Avoid modifying the description unless absolutely necessary</li>
 *   <li>Consider performance for frequently validated symbols</li>
 * </ul>
 * 
 * <h2>Helper Methods</h2>
 * 
 * <p>
 * The interface provides utility methods for common validation tasks:
 * </p>
 * <ul>
 *   <li>{@link #swap(IDescription, String, String)} - Swap one facet for another</li>
 * </ul>
 * 
 * @param <T> the type of description this validator validates (must extend {@link IDescription})
 * 
 * @author drogoul
 * @since 13 sept. 2013
 * @version 2025-03
 * 
 * @see gama.api.annotations.validator
 * @see IDescription
 * @see gama.api.compilation.validation.IValidator
 */
@SuppressWarnings ({ "rawtypes" })
public interface IDescriptionValidator<T extends IDescription> extends IValidator {

	/**
	 * Performs custom validation on the given description.
	 * 
	 * <p>
	 * This method is called at the end of the validation process after all standard validation
	 * has completed. At this point:
	 * </p>
	 * <ul>
	 *   <li>The enclosing description has been validated</li>
	 *   <li>All children have been validated</li>
	 *   <li>All facets have been validated and their expressions compiled</li>
	 * </ul>
	 * 
	 * <p>
	 * The validator should check the description for semantic correctness and consistency
	 * according to the symbol's specific requirements. It should report errors and warnings
	 * by calling methods on the description object:
	 * </p>
	 * <ul>
	 *   <li>{@code description.error(message, issueCode)} - For fatal errors</li>
	 *   <li>{@code description.warning(message, issueCode)} - For warnings</li>
	 *   <li>{@code description.info(message)} - For informational messages</li>
	 * </ul>
	 * 
	 * <p>
	 * <b>Note:</b> This method should not modify the description unless absolutely necessary.
	 * It is intended for validation and error reporting, not transformation.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * @Override
	 * public void validate(StatementDescription description) {
	 *     // Check for required facets
	 *     if (!description.hasFacet("size")) {
	 *         description.error("Missing required 'size' facet", IGamlIssue.MISSING_FACET);
	 *     }
	 *     
	 *     // Validate facet values
	 *     IExpression expr = description.getFacetExpr("threshold");
	 *     if (expr != null && expr.getGamlType().id() != IType.FLOAT) {
	 *         description.error("'threshold' must be a float", IGamlIssue.WRONG_TYPE);
	 *     }
	 * }
	 * }</pre>
	 *
	 * @param description the description to validate (never null)
	 */
	void validate(T description);

	/**
	 * Alternative validation method with EMF context and expression arguments.
	 * 
	 * <p>
	 * This is a legacy method from the {@link IValidator} interface. For description validators,
	 * the arguments array will always be empty, and this method simply delegates to
	 * {@link #validate(IDescription)}.
	 * </p>
	 * 
	 * <p>
	 * <b>Return Value:</b> Returning {@code false} will veto the validation process, preventing
	 * further validation. Implementations should typically return {@code true} after calling
	 * {@link #validate(IDescription)}.
	 * </p>
	 * 
	 * <p><b>Default Implementation:</b> Calls {@code validate((T) description)} and returns {@code true}.</p>
	 *
	 * @param description the description to validate
	 * @param emfContext the EMF context object (unused for description validators)
	 * @param arguments expression arguments (always empty for description validators)
	 * @return {@code true} to continue validation, {@code false} to veto further validation
	 */
	@SuppressWarnings ("unchecked")
	@Override
	default boolean validate(final IDescription description, final EObject emfContext, final IExpression... arguments) {
		validate((T) description);
		return true;
	}

	/**
	 * Swaps one facet for another in the description.
	 * 
	 * <p>
	 * This utility method is useful for normalizing facet names or migrating from deprecated
	 * facets to new ones during validation. If the old facet exists, its value is copied to
	 * the new facet and the old facet is removed.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Migrate deprecated 'colour' to 'color'
	 * swap(desc, "colour", "color");
	 * 
	 * // After this call:
	 * // - If 'colour' facet existed, it's now under 'color'
	 * // - 'colour' facet is removed
	 * }</pre>
	 * 
	 * <p>
	 * <b>Note:</b> This method modifies the description, which should generally be avoided
	 * in validators. Use this only for facet normalization or migration of deprecated syntax.
	 * </p>
	 *
	 * @param desc the description to modify
	 * @param oldFacet the name of the facet to swap from
	 * @param newFacet the name of the facet to swap to
	 */
	default void swap(final IDescription desc, final String oldFacet, final String newFacet) {
		if (desc.hasFacet(oldFacet)) {
			desc.setFacetExprDescription(newFacet, desc.getFacet(oldFacet));
			desc.removeFacets(oldFacet);
		}
	}

	/**
	 * Null validator implementation that performs no validation.
	 * 
	 * <p>
	 * This is a placeholder validator used when no custom validation is needed.
	 * It simply does nothing when called, allowing the description to pass validation
	 * without any custom checks.
	 * </p>
	 * 
	 * <p>
	 * This class can be used as a default validator or as a base class for validators
	 * that need minimal validation logic.
	 * </p>
	 */
	public static class NullValidator implements IDescriptionValidator {

		/**
		 * Performs no validation - the description always passes.
		 * 
		 * <p>
		 * This implementation does nothing, effectively skipping custom validation.
		 * </p>
		 *
		 * @param cd the description (ignored)
		 */
		@Override
		public void validate(final IDescription cd) {}
	}

}
