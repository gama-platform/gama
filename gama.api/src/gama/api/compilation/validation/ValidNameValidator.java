/*******************************************************************************************************
 *
 * ValidNameValidator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;

/**
 * Simple validator that ensures description names are valid GAML identifiers.
 * 
 * <p>This validator performs name validation for any GAML description, ensuring that:</p>
 * <ul>
 *   <li>The name is not a reserved keyword ({@code false}, {@code true}, {@code null}, {@code myself})</li>
 *   <li>The name doesn't conflict with existing type or species names</li>
 *   <li>The name attribute is present and non-null</li>
 * </ul>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>This validator is typically attached to descriptions that require simple name validation
 * without additional semantic checks. It delegates the actual validation logic to
 * {@link Assert#nameIsValid}, which performs comprehensive identifier validation.</p>
 * 
 * <h2>Applicable Contexts</h2>
 * 
 * <p>This validator is appropriate for:</p>
 * <ul>
 *   <li>Variable declarations</li>
 *   <li>Parameter definitions</li>
 *   <li>Attribute declarations</li>
 *   <li>Simple identifiers requiring only name validation</li>
 * </ul>
 * 
 * <p>For more complex validation (e.g., actions with return types), use specialized
 * validators like {@link ActionValidator}.</p>
 * 
 * <h2>Error Reporting</h2>
 * 
 * <p>Validation errors are reported directly on the description through {@link Assert#nameIsValid},
 * which emits errors with appropriate issue codes:</p>
 * <ul>
 *   <li>{@link IGamlIssue#MISSING_NAME} - Name attribute is missing</li>
 *   <li>{@link IGamlIssue#IS_RESERVED} - Name is a reserved keyword</li>
 *   <li>{@link IGamlIssue#IS_A_TYPE} - Name conflicts with a type or species</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // In an annotation processor or description factory
 * @validator(ValidNameValidator.class)
 * public class MySymbolDescription extends SymbolDescription {
 *     // Description implementation
 * }
 * 
 * // The validator ensures:
 * int false <- 5;     // ERROR: 'false' is reserved
 * int agent <- 5;     // ERROR: 'agent' is a species/type
 * int my_var <- 5;    // OK: valid identifier
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see Assert#nameIsValid
 * @see IDescriptionValidator
 * @see ActionValidator
 */
public class ValidNameValidator implements IDescriptionValidator {

	/**
	 * Validates that the description's name is a valid GAML identifier.
	 * 
	 * <p>This method delegates to {@link Assert#nameIsValid} which performs the actual
	 * validation. The validation checks that the name:</p>
	 * <ol>
	 *   <li>Exists and is non-null</li>
	 *   <li>Is not a reserved keyword</li>
	 *   <li>Doesn't conflict with type or species names</li>
	 * </ol>
	 * 
	 * <p>Validation errors are attached directly to the description, so this method
	 * has no return value. Callers should check the description's error state if needed.</p>
	 *
	 * @param cd the description to validate (must not be null)
	 * 
	 * @see Assert#nameIsValid
	 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
	 */
	@Override
	public void validate(final IDescription cd) {
		Assert.nameIsValid(cd);
	}
}