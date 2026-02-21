/*******************************************************************************************************
 *
 * validator.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.api.compilation.validation.IValidator;

/**
 * Declares a custom validator for GAML symbols (statements, species, experiments) and operators.
 * 
 * <p>
 * This annotation allows developers to specify custom validation logic that extends or supplements the standard
 * validation performed by the GAMA platform. The validator is invoked during model compilation to check for semantic
 * errors, inconsistencies, or invalid configurations that cannot be detected by syntax checking alone.
 * </p>
 * 
 * <h2>Purpose</h2>
 * <p>
 * Custom validators enable:
 * </p>
 * <ul>
 *   <li>Domain-specific validation rules beyond standard syntax checking</li>
 *   <li>Cross-facet validation (checking consistency between multiple parameters)</li>
 *   <li>Runtime constraint verification (e.g., value ranges, type compatibility)</li>
 *   <li>Warning generation for potentially problematic but syntactically valid constructs</li>
 * </ul>
 * 
 * <h2>Usage Contexts</h2>
 * <p>
 * This annotation can be applied to two different contexts:
 * </p>
 * 
 * <h3>1. Symbol Validation (TYPE target)</h3>
 * <p>
 * When applied to a class (symbol type such as statements, species, experiments), the validator is called after the
 * standard validation is complete. The validator class must implement {@code IDescriptionValidator}.
 * </p>
 * 
 * <h3>2. Operator Validation (METHOD target)</h3>
 * <p>
 * When applied to a method (operator), the validator is called when the operator is used in the model. The validator
 * class must implement {@code IOperatorValidator}.
 * </p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Example 1: Validator for a custom statement</h3>
 * <pre>{@code
 * @validator(MyStatementValidator.class)
 * @symbol(name = "my_statement", kind = ISymbolKind.SINGLE_STATEMENT, ...)
 * public class MyCustomStatement extends AbstractStatement {
 *     // Statement implementation
 * }
 * 
 * // The validator implementation
 * public class MyStatementValidator implements IDescriptionValidator {
 *     public void validate(IDescription description) {
 *         // Check that required facets are present
 *         if (!description.hasFacet("required_param")) {
 *             description.error("'required_param' is mandatory", IGamlIssue.MISSING_FACET);
 *         }
 *         
 *         // Validate facet values
 *         IExpression expr = description.getFacetExpr("size");
 *         if (expr != null && expr.isConst() && ((Integer) expr.getConstValue()) <= 0) {
 *             description.warning("Size should be positive", IGamlIssue.WRONG_TYPE);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h3>Example 2: Validator for an operator</h3>
 * <pre>{@code
 * @operator(value = "custom_operation", ...)
 * @validator(MyOperatorValidator.class)
 * public static Object customOperation(IScope scope, Object arg1, Object arg2) {
 *     // Operator implementation
 *     return ...;
 * }
 * 
 * // The validator implementation
 * public class MyOperatorValidator implements IOperatorValidator {
 *     public void validate(IExpression expression, IDescription context) {
 *         // Validate operator arguments
 *         IExpression[] args = expression.getArgs();
 *         if (args.length != 2) {
 *             context.error("This operator requires exactly 2 arguments", IGamlIssue.WRONG_NUMBER_OF_ARGUMENTS);
 *         }
 *         
 *         // Check argument types
 *         if (args[0].getGamlType() != Types.INT) {
 *             context.error("First argument must be an integer", IGamlIssue.WRONG_TYPE);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h3>Example 3: Validator for a species</h3>
 * <pre>{@code
 * @validator(SpeciesValidator.class)
 * @species("custom_agent")
 * public class CustomAgentSpecies extends AbstractSpecies {
 *     // Species implementation
 * }
 * 
 * public class SpeciesValidator implements IDescriptionValidator {
 *     public void validate(IDescription description) {
 *         // Ensure certain attributes are defined
 *         if (!description.hasAttribute("energy")) {
 *             description.warning("Custom agents should have an 'energy' attribute", 
 *                 IGamlIssue.MISSING_DEFINITION);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h2>Implementation Guidelines</h2>
 * 
 * <h3>For Symbol Validators (IDescriptionValidator)</h3>
 * <ul>
 *   <li>Implement {@code void validate(IDescription description)}</li>
 *   <li>Use {@code description.error()} for fatal validation errors</li>
 *   <li>Use {@code description.warning()} for non-fatal issues</li>
 *   <li>Use {@code description.info()} for informational messages</li>
 *   <li>Access facets via {@code description.getFacet()} or {@code description.getFacetExpr()}</li>
 *   <li>Check for facet presence using {@code description.hasFacet()}</li>
 * </ul>
 * 
 * <h3>For Operator Validators (IOperatorValidator)</h3>
 * <ul>
 *   <li>Implement {@code void validate(IExpression expression, IDescription context)}</li>
 *   <li>Access operator arguments via {@code expression.getArgs()}</li>
 *   <li>Check argument types using {@code arg.getGamlType()}</li>
 *   <li>Report errors through the {@code context} description</li>
 *   <li>Consider constant folding: check {@code expr.isConst()} and {@code expr.getConstValue()}</li>
 * </ul>
 * 
 * <h3>General Requirements</h3>
 * <ul>
 *   <li>Validator classes must have a public no-argument constructor</li>
 *   <li>Validators should be stateless or thread-safe</li>
 *   <li>Avoid performing expensive operations during validation</li>
 *   <li>Provide clear, actionable error messages</li>
 *   <li>Use appropriate {@code IGamlIssue} constants for error categorization</li>
 * </ul>
 * 
 * <h3>Best Practices</h3>
 * <ul>
 *   <li>Validate semantic correctness, not just syntax (syntax is already validated)</li>
 *   <li>Check for common user mistakes and provide helpful error messages</li>
 *   <li>Use warnings for potentially problematic but valid code</li>
 *   <li>Validate cross-facet dependencies and constraints</li>
 *   <li>Consider performance: validation runs on every compilation</li>
 *   <li>Test validators with both valid and invalid inputs</li>
 *   <li>Document what the validator checks in the class Javadoc</li>
 * </ul>
 * 
 * <h2>Validation Timing</h2>
 * <ul>
 *   <li>For symbols: Called after the standard validation, before children are validated</li>
 *   <li>For operators: Called when the operator expression is validated during compilation</li>
 *   <li>Validation happens during model compilation, before execution</li>
 * </ul>
 * 
 * <h2>Inheritance</h2>
 * <p>
 * This annotation is inherited, meaning that subclasses of an annotated class will use the same validator unless they
 * specify their own {@code @validator} annotation. This allows creating hierarchies of symbols with shared validation
 * logic.
 * </p>
 * 
 * <h2>Common Validation Scenarios</h2>
 * <ul>
 *   <li>Checking mandatory facets/parameters are provided</li>
 *   <li>Validating value ranges (e.g., size > 0, probability between 0 and 1)</li>
 *   <li>Ensuring type compatibility between related facets</li>
 *   <li>Detecting mutually exclusive facets</li>
 *   <li>Verifying file paths or resource references exist</li>
 *   <li>Warning about deprecated usage patterns</li>
 * </ul>
 * 
 * @author drogoul
 * @since 11 nov. 2014
 * @see gama.api.compilation.validation.IValidator
 * @see gama.gaml.descriptions.IDescription
 * @see gama.gaml.expressions.IExpression
 */

@Retention (RetentionPolicy.RUNTIME)
@Target ({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@SuppressWarnings ({ "rawtypes" })
public @interface validator {

	/**
	 * The custom validator class to use for this symbol or operator.
	 * 
	 * <p>
	 * Specifies the class that implements the appropriate validator interface:
	 * </p>
	 * <ul>
	 *   <li>For symbol validation (TYPE target): implement {@code IDescriptionValidator}</li>
	 *   <li>For operator validation (METHOD target): implement {@code IOperatorValidator}</li>
	 * </ul>
	 * <p>
	 * The class must have a public no-argument constructor and will be instantiated during platform initialization.
	 * </p>
	 * 
	 * @return the validator class that implements {@link gama.api.compilation.validation.IValidator}
	 */
	Class<? extends IValidator> value();
}