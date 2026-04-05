/**
 * 
 */
package gama.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @example} annotation provides executable code examples for GAML elements.
 * Examples serve multiple purposes: user documentation, automated testing, and IDE assistance.
 * 
 * <p><strong>Architecture:</strong></p>
 * The example system supports both documentation and validation:
 * <ul>
 *   <li><strong>Documentation:</strong> Examples appear in generated help and online docs</li>
 *   <li><strong>Testing:</strong> Examples can be automatically executed and validated</li>
 *   <li><strong>IDE Support:</strong> Examples provide code completion templates</li>
 *   <li><strong>Pattern Matching:</strong> Examples can use template variables for reusability</li>
 * </ul>
 * 
 * <p><strong>Example Types:</strong></p>
 * <pre>{@code
 * // Simple usage example
 * @example("do move speed: 2.0 heading: 90;")
 * 
 * // Example with expected result
 * @example(
 *     value = "int result <- 5 + 3;", 
 *     equals = "8",
 *     returnType = "int"
 * )
 * 
 * // Example that tests for inequality  
 * @example(
 *     value = "float random_val <- rnd(1.0);",
 *     isNot = "0.5", 
 *     returnType = "float"
 * )
 * 
 * // Example that should raise an exception
 * @example(
 *     value = "int invalid <- 10 / 0;",
 *     raises = "Division by zero error"
 * )
 * 
 * // Documentation-only example (not executable)
 * @example(
 *     value = "// This shows the conceptual usage",
 *     isExecutable = false
 * )
 * 
 * // Test-only example (not in documentation)
 * @example(
 *     value = "int test_result <- internal_function();",
 *     isTestOnly = true,
 *     equals = "42"
 * )
 * 
 * // Template example with pattern variables
 * @example(
 *     value = "do ${action_name} target: ${target_agent};",
 *     isPattern = true
 * )
 * }</pre>
 * 
 * <p><strong>Testing Integration:</strong></p>
 * Examples with {@code test = true} are automatically executed during the build process
 * to validate that the documented behavior matches the actual implementation. This ensures
 * that documentation stays current with code changes.
 * 
 * <p><strong>Quality Guidelines:</strong></p>
 * <ul>
 *   <li><strong>Executable:</strong> Examples should be valid GAML code when possible</li>
 *   <li><strong>Realistic:</strong> Use meaningful variable names and realistic values</li>
 *   <li><strong>Progressive:</strong> Start with simple examples, then show advanced usage</li>
 *   <li><strong>Complete:</strong> Include necessary context and variable declarations</li>
 *   <li><strong>Diverse:</strong> Cover different parameter combinations and use cases</li>
 * </ul>
 * 
 * @see doc For comprehensive documentation metadata
 * @see usage For structured usage patterns
 * @see test For test-specific annotations
 * 
 * @author GAMA Development Team  
 * @since GAMA 1.0
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({})

// @Inherited
public @interface example {

	/**
	 * The GAML code expression or statement to demonstrate.
	 * 
	 * <p>This should be valid, executable GAML code that illustrates how to use
	 * the annotated element. The code should be as self-contained as possible,
	 * though it may reference common GAML concepts and built-in functions.</p>
	 * 
	 * <p><strong>Code Quality Guidelines:</strong></p>
	 * <ul>
	 *   <li>Use realistic variable names that convey meaning</li>
	 *   <li>Include necessary context for understanding</li>
	 *   <li>Follow GAML syntax and style conventions</li>
	 *   <li>Keep examples concise but complete</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "do move speed: 2.0 heading: 90;"}</li>
	 *   <li>{@code "list<agent> neighbors <- agents_overlapping(circle(5));"}</li>
	 *   <li>{@code "create species: prey number: 100;"}</li>
	 * </ul>
	 *
	 * @return the GAML code expression to demonstrate
	 */
	String value() default "";

	/**
	 * The variable name to use for storing the result when testing.
	 * 
	 * <p>When the example is executed for testing purposes, the result will be
	 * assigned to this variable name. If omitted, a default variable name will
	 * be generated automatically.</p>
	 * 
	 * <p>This is particularly useful when you want to:</p>
	 * <ul>
	 *   <li>Use a specific variable name that's meaningful in context</li>
	 *   <li>Reference the result in subsequent test comparisons</li>
	 *   <li>Maintain consistency across related examples</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "result"} - Generic result variable</li>
	 *   <li>{@code "distance"} - For distance calculations</li>
	 *   <li>{@code "agents_found"} - For agent collections</li>
	 * </ul>
	 *
	 * @return the variable name for test result storage
	 */
	String var() default "";

	/**
	 * The expected value that the example should evaluate to when executed.
	 * 
	 * <p>This enables automated validation of examples by comparing the actual
	 * execution result with the expected value. The comparison is done using
	 * GAML's equality semantics, which may include type coercion.</p>
	 * 
	 * <p><strong>Value Types:</strong></p>
	 * <ul>
	 *   <li><strong>Literals:</strong> Numbers, strings, booleans ({@code "5", "true", "hello"})</li>
	 *   <li><strong>Collections:</strong> Lists and maps ({@code "[1,2,3]", "{a:1, b:2}"})</li>
	 *   <li><strong>Complex Values:</strong> Points, colors, geometries ({@code "{10,20}", "#red"})</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "8"} - Numeric result</li>
	 *   <li>{@code "true"} - Boolean result</li>
	 *   <li>{@code "hello world"} - String result</li>
	 *   <li>{@code "[1,2,3,4,5]"} - List result</li>
	 * </ul>
	 *
	 * @return the expected result value for comparison
	 */
	String equals() default "";

	/**
	 * The expected return type of the example expression.
	 * 
	 * <p>This provides type information for documentation and enables type-specific
	 * validation during testing. The type should match GAML's type system.</p>
	 * 
	 * <p><strong>Common Types:</strong></p>
	 * <ul>
	 *   <li><strong>Primitives:</strong> {@code "int", "float", "bool", "string"}</li>
	 *   <li><strong>Collections:</strong> {@code "list", "map", "matrix"}</li>
	 *   <li><strong>Spatial:</strong> {@code "point", "geometry", "path"}</li>
	 *   <li><strong>Agents:</strong> {@code "agent", "species_name"}</li>
	 *   <li><strong>Generic:</strong> {@code "any", "object"}</li>
	 * </ul>
	 * 
	 * <p><strong>Parameterized Types:</strong></p>
	 * For generic collections, you can specify element types:
	 * <ul>
	 *   <li>{@code "list<agent>"} - List of agents</li>
	 *   <li>{@code "map<string,int>"} - Map from strings to integers</li>
	 * </ul>
	 *
	 * @return the expected return type
	 */
	String returnType() default "";

	/**
	 * A value that the example should NOT evaluate to (negative assertion).
	 * 
	 * <p>This is useful for testing non-deterministic operations or when you want
	 * to verify that a result is not a specific problematic value. The test
	 * passes if the actual result is different from this value.</p>
	 * 
	 * <p><strong>Use Cases:</strong></p>
	 * <ul>
	 *   <li><strong>Random Functions:</strong> Ensuring random values are not always the same</li>
	 *   <li><strong>Error Conditions:</strong> Verifying that invalid states don't occur</li>
	 *   <li><strong>Boundary Testing:</strong> Checking that results are within valid ranges</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "0.5"} - For random functions that shouldn't always return 0.5</li>
	 *   <li>{@code "null"} - Ensuring functions don't return null unexpectedly</li>
	 *   <li>{@code "[]"} - Verifying collections are not empty when they shouldn't be</li>
	 * </ul>
	 *
	 * @return value that the result should not equal
	 */
	String isNot() default "";

	/**
	 * The exception or warning message that the example is expected to raise.
	 * 
	 * <p>This enables testing of error conditions and validation logic by
	 * specifying the expected error message or warning. The test passes if
	 * the specified error is raised during execution.</p>
	 * 
	 * <p><strong>Error Testing Types:</strong></p>
	 * <ul>
	 *   <li><strong>Parameter Validation:</strong> Invalid argument errors</li>
	 *   <li><strong>Runtime Errors:</strong> Division by zero, null pointer</li>
	 *   <li><strong>Type Errors:</strong> Incompatible type usage</li>
	 *   <li><strong>Business Logic:</strong> Domain-specific validation failures</li>
	 * </ul>
	 * 
	 * <p><strong>Examples:</strong></p>
	 * <ul>
	 *   <li>{@code "Division by zero"} - Mathematical errors</li>
	 *   <li>{@code "Invalid argument: speed must be positive"} - Parameter validation</li>
	 *   <li>{@code "Cannot access attribute of dead agent"} - State validation</li>
	 * </ul>
	 *
	 * @return expected exception or warning message
	 */
	String raises() default "";

	/**
	 * Indicates that this example is for testing purposes only and should not appear in documentation.
	 * 
	 * <p>Test-only examples are used to verify implementation behavior without
	 * cluttering user-facing documentation. They're particularly useful for:</p>
	 * 
	 * <ul>
	 *   <li><strong>Edge Cases:</strong> Testing boundary conditions</li>
	 *   <li><strong>Internal Functions:</strong> Testing framework internals</li>
	 *   <li><strong>Regression Tests:</strong> Preventing specific bugs from recurring</li>
	 *   <li><strong>Performance Tests:</strong> Validating performance characteristics</li>
	 * </ul>
	 * 
	 * <p>These examples will be executed during automated testing but won't
	 * appear in generated documentation or IDE help.</p>
	 *
	 * @return {@code true} if this example is for testing only, {@code false} for documentation
	 */
	boolean isTestOnly() default false;

	/**
	 * Indicates whether this example contains valid, executable GAML code.
	 * 
	 * <p>Not all examples need to be executable. Some might be:</p>
	 * <ul>
	 *   <li><strong>Conceptual:</strong> Showing patterns rather than specific code</li>
	 *   <li><strong>Incomplete:</strong> Requiring additional context to run</li>
	 *   <li><strong>Pseudocode:</strong> Illustrating algorithms or approaches</li>
	 *   <li><strong>Comments:</strong> Explanatory text rather than code</li>
	 * </ul>
	 * 
	 * <p>Non-executable examples ({@code isExecutable = false}) are included in
	 * documentation but skipped during automated testing.</p>
	 *
	 * @return {@code true} if the example is executable GAML code, {@code false} otherwise
	 */
	boolean isExecutable() default true;

	/**
	 * Indicates whether this example should be executed and validated during testing.
	 * 
	 * <p>Even executable examples might not need testing if they:</p>
	 * <ul>
	 *   <li>Depend on external resources or specific simulation states</li>
	 *   <li>Are purely illustrative without testable outcomes</li>
	 *   <li>Have non-deterministic results that can't be reliably validated</li>
	 * </ul>
	 * 
	 * <p>Setting {@code test = false} includes the example in documentation
	 * but excludes it from automated validation.</p>
	 *
	 * @return {@code true} if the example should be tested, {@code false} otherwise
	 */
	boolean test() default true;

	/**
	 * Indicates that this example uses template variables and should be treated as a pattern.
	 * 
	 * <p>Pattern examples use placeholder variables (like {@code ${variable_name}}) that
	 * can be substituted with actual values in different contexts. This enables
	 * creating reusable example templates.</p>
	 * 
	 * <p><strong>Template Variables:</strong></p>
	 * <ul>
	 *   <li>{@code ${agent_name}} - Placeholder for agent references</li>
	 *   <li>{@code ${skill_name}} - Placeholder for skill names</li>
	 *   <li>{@code ${action_name}} - Placeholder for action names</li>
	 *   <li>{@code ${parameter_value}} - Placeholder for parameter values</li>
	 * </ul>
	 * 
	 * <p><strong>Pattern Example:</strong></p>
	 * {@code "do ${action_name} target: ${target_agent} speed: ${movement_speed};"}
	 * 
	 * <p>Pattern examples are primarily used in documentation generation where
	 * the placeholders can be replaced with context-appropriate values.</p>
	 *
	 * @return {@code true} if this example is a pattern with template variables, {@code false} otherwise
	 */
	boolean isPattern() default false;
}