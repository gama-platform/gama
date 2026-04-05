/*******************************************************************************************************
 *
 * IValidator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.expressions.IExpression;

/**
 * Base tagging interface for expression and description validators.
 * 
 * <p>
 * This interface serves as a marker and contract for all validators in the GAMA compilation system.
 * Validators perform semantic validation of descriptions and expressions during model compilation,
 * checking for correctness beyond basic syntax validation.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * Validators enable:
 * </p>
 * <ul>
 *   <li><strong>Semantic Validation:</strong> Checking logical correctness and consistency</li>
 *   <li><strong>Type Checking:</strong> Verifying type compatibility and constraints</li>
 *   <li><strong>Error Reporting:</strong> Attaching detailed error messages to descriptions</li>
 *   <li><strong>Custom Rules:</strong> Implementing domain-specific validation logic</li>
 * </ul>
 * 
 * <h2>Validator Types</h2>
 * 
 * <ul>
 *   <li><strong>Description Validators ({@link gama.api.compilation.descriptions.IDescriptionValidator}):</strong> Validate symbol descriptions</li>
 *   <li><strong>Operator Validators ({@link IOperatorValidator}):</strong> Validate operator usage</li>
 *   <li><strong>Specialized Validators:</strong> {@link ActionValidator}, {@link BatchValidator}, etc.</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * public class MyValidator implements IValidator {
 *     
 *     @Override
 *     public boolean validate(IDescription desc, EObject emfContext, IExpression... args) {
 *         // Perform validation
 *         if (!isValid(desc)) {
 *             desc.error("Validation failed", IGamlIssue.GENERAL);
 *             return false;
 *         }
 *         return true;
 *     }
 * }
 * }</pre>
 * 
 * @author A. Drogoul
 * @since July 2018
 * @version 2025-03
 * 
 * @see gama.api.compilation.descriptions.IDescriptionValidator
 * @see IOperatorValidator
 */
public interface IValidator extends IKeyword, IGamlIssue {

	/**
	 * Null-object validator that always succeeds.
	 * 
	 * <p>
	 * This singleton provides a no-op implementation for cases where validation is not needed.
	 * </p>
	 */
	IValidator NULL = (d, c, a) -> true;

	/**
	 * Validates a description or expression in the given context.
	 * 
	 * <p>
	 * This method performs semantic validation and reports errors by attaching them to the
	 * description. The return value indicates whether validation succeeded or should be
	 * vetoed (stopped).
	 * </p>
	 * 
	 * <h3>Implementation Guidelines:</h3>
	 * <ul>
	 *   <li>Report errors using {@code description.error(message, issueCode)}</li>
	 *   <li>Report warnings using {@code description.warning(message, issueCode)}</li>
	 *   <li>Return {@code true} to continue validation, {@code false} to veto</li>
	 *   <li>Use appropriate issue codes from {@link IGamlIssue}</li>
	 * </ul>
	 *
	 * @param description the description to validate (never null)
	 * @param emfContext the EMF context object for error location (may be null)
	 * @param arguments optional expression arguments being validated (may be empty)
	 * @return true if validation succeeds and should continue, false to veto further validation
	 */
	boolean validate(IDescription description, EObject emfContext, IExpression... arguments);
}
