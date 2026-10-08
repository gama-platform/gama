/*******************************************************************************************************
 *
 * IGamlTextValidator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.GamlCompilationError;

/**
 * Interface for validating GAML text fragments at different syntactic levels.
 * 
 * <p>This interface provides a hierarchy of validation methods for different granularities of GAML code,
 * from complete models down to individual expressions. It's primarily used by IDE features and
 * interactive tools to validate code snippets without requiring a complete model file.</p>
 * 
 * <h2>Validation Hierarchy</h2>
 * 
 * <p>The interface supports validation at four levels of granularity:</p>
 * <ol>
 *   <li><strong>Model Level:</strong> Complete GAML models with all declarations</li>
 *   <li><strong>Species Level:</strong> Individual species definitions</li>
 *   <li><strong>Statement Level:</strong> Code blocks and statement sequences</li>
 *   <li><strong>Expression Level:</strong> Individual expressions and assignments</li>
 * </ol>
 * 
 * <h2>Validation Modes</h2>
 * 
 * <p>Two validation modes are supported via the {@code syntaxOnly} parameter:</p>
 * <ul>
 *   <li><strong>Syntax Only:</strong> Fast validation checking only parsing errors</li>
 *   <li><strong>Full Semantic:</strong> Complete validation including type checking and resolution</li>
 * </ul>
 * 
 * <h2>Wrapping Strategy</h2>
 * 
 * <p>Lower-level validation methods wrap code in progressively larger contexts:</p>
 * <ul>
 *   <li><strong>Expression:</strong> Wrapped in assignment statement within species init block</li>
 *   <li><strong>Statements:</strong> Wrapped in species init block</li>
 *   <li><strong>Species:</strong> Wrapped in synthetic model</li>
 * </ul>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li><strong>IDE Content Assist:</strong> Quick validation while typing</li>
 *   <li><strong>Expression Editor:</strong> Validate user input in dialogs</li>
 *   <li><strong>Interactive Console:</strong> Validate commands before execution</li>
 *   <li><strong>Code Snippets:</strong> Validate documentation examples</li>
 *   <li><strong>Model Fragments:</strong> Test partial model components</li>
 * </ul>
 * 
 * <h2>Error Collection</h2>
 * 
 * <p>All methods populate a provided error list with {@link GamlCompilationError} instances.
 * Errors include location information adjusted to account for wrapping.</p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IGamlTextValidator validator = ...;
 * List<GamlCompilationError> errors = new ArrayList<>();
 * 
 * // Validate an expression
 * validator.validateExpression("length(my_list) * 2", errors, false);
 * 
 * // Validate a code block
 * validator.validateStatements(
 *     "int x <- 5; create species: people number: x;",
 *     errors,
 *     false
 * );
 * 
 * // Check for errors
 * if (!errors.isEmpty()) {
 *     for (GamlCompilationError error : errors) {
 *         System.err.println(error.toString());
 *     }
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see GamlCompilationError
 * @see IGamlModelBuilder
 */
public interface IGamlTextValidator {

	/**
	 * Validates a GAML expression by wrapping it in a minimal executable context.
	 * 
	 * <p>This method is the most granular validation level, suitable for validating
	 * individual expressions like operators, variable references, or literals.</p>
	 * 
	 * <h3>Wrapping Strategy:</h3>
	 * <p>The expression is wrapped as follows:</p>
	 * <pre>{@code
	 * species synthetic_species {
	 *     init {
	 *         unknow result <- <YOUR_EXPRESSION>;
	 *     }
	 * }
	 * }</pre>
	 * 
	 * <h3>Automatic Adjustments:</h3>
	 * <ul>
	 *   <li>Semicolon added if missing (unless expression ends with '}')</li>
	 *   <li>Assignment wrapper added if no '<-' operator present</li>
	 * </ul>
	 *
	 * @param expr the GAML expression to validate (e.g., "5 + 3", "length(my_list)")
	 * @param errors list to collect validation errors (will be populated)
	 * @param syntaxOnly true for syntax-only validation, false for full semantic validation
	 * 
	 * @see #validateStatements
	 */
	default void validateExpression(final String expr, final List<GamlCompilationError> errors,
			final boolean syntaxOnly) {
		String fixed_exp = expr;
		if (!expr.endsWith("}") && !expr.endsWith(";")) { fixed_exp += ";"; }
		if (!expr.contains("<-")) { fixed_exp = "unknow result <- " + fixed_exp; }
		validateStatements(fixed_exp, errors, syntaxOnly);
	}

	/**
	 * Validates a sequence of GAML statements by wrapping them in a species init block.
	 * 
	 * <p>This method validates code blocks containing multiple statements, local variables,
	 * loops, conditionals, and other control structures.</p>
	 * 
	 * <h3>Wrapping Strategy:</h3>
	 * <p>The statements are wrapped as follows:</p>
	 * <pre>{@code
	 * species synthetic_species {
	 *     init {
	 *         <YOUR_STATEMENTS>
	 *     }
	 * }
	 * }</pre>
	 * 
	 * <h3>Supported Statements:</h3>
	 * <ul>
	 *   <li>Variable declarations: {@code int x <- 5;}</li>
	 *   <li>Assignments: {@code x <- x + 1;}</li>
	 *   <li>Control flow: {@code if (condition) {...}}, {@code loop i from: 0 to: 10 {...}}</li>
	 *   <li>Actions: {@code do my_action;}, {@code create my_species;}</li>
	 * </ul>
	 *
	 * @param expr the GAML statements to validate
	 * @param errors list to collect validation errors (will be populated)
	 * @param syntaxOnly true for syntax-only validation, false for full semantic validation
	 * 
	 * @see #validateExpression
	 * @see #validateSpecies
	 */
	default void validateStatements(final String expr, final List<GamlCompilationError> errors,
			final boolean syntaxOnly) {
		validateSpecies("species synthetic_species { init {" + expr + " }}", errors, syntaxOnly);
	}

	/**
	 * Validates a GAML species definition by wrapping it in a minimal model.
	 * 
	 * <p>This method validates species declarations including their attributes, actions,
	 * behaviors, and inheritance relationships.</p>
	 * 
	 * <h3>Wrapping Strategy:</h3>
	 * <p>The species is wrapped as follows:</p>
	 * <pre>{@code
	 * model synthetic
	 * <YOUR_SPECIES>
	 * }</pre>
	 * 
	 * <h3>Supported Features:</h3>
	 * <ul>
	 *   <li>Species inheritance: {@code species child parent: parent_species {...}}</li>
	 *   <li>Skills: {@code species agent skills: [moving] {...}}</li>
	 *   <li>Variables: {@code int age <- 0;}</li>
	 *   <li>Actions: {@code action move {...}}</li>
	 *   <li>Reflexes and behaviors</li>
	 * </ul>
	 *
	 * @param expr the GAML species definition to validate
	 * @param errors list to collect validation errors (will be populated)
	 * @param syntaxOnly true for syntax-only validation, false for full semantic validation
	 * 
	 * @see #validateStatements
	 * @see #validateModel
	 */
	default void validateSpecies(final String expr, final List<GamlCompilationError> errors, final boolean syntaxOnly) {
		validateModel("model synthetic \n" + expr, errors, syntaxOnly);
	}

	/**
	 * Validates a complete GAML model.
	 * 
	 * <p>This is the top-level validation method that processes a complete model definition
	 * including global variables, species, experiments, and all other model elements.</p>
	 * 
	 * <h3>Model Components Validated:</h3>
	 * <ul>
	 *   <li><strong>Global Section:</strong> Global variables, functions, and initialization</li>
	 *   <li><strong>Species:</strong> All species definitions and hierarchies</li>
	 *   <li><strong>Experiments:</strong> GUI and batch experiment configurations</li>
	 *   <li><strong>Imports:</strong> Import statements and dependencies</li>
	 *   <li><strong>Grid Species:</strong> Grid and graph topologies</li>
	 * </ul>
	 * 
	 * <h3>Validation Scope:</h3>
	 * <p>When {@code syntaxOnly} is false, performs comprehensive validation:</p>
	 * <ul>
	 *   <li>Type checking for all expressions and variables</li>
	 *   <li>Reference resolution for species, variables, and actions</li>
	 *   <li>Facet requirement validation</li>
	 *   <li>Constraint checking (value ranges, dependencies)</li>
	 * </ul>
	 *
	 * @param expr the complete GAML model text to validate
	 * @param errors list to collect validation errors and warnings (will be populated)
	 * @param syntaxOnly true to validate only syntax (fast), false for full semantic validation
	 */
	void validateModel(final String expr, final List<GamlCompilationError> errors, boolean syntaxOnly);

	/**
	 * Retrieves the starting line number and offset in the source file for an EMF object.
	 * 
	 * <p>This method provides location information for error reporting and navigation.
	 * The returned array contains two elements:</p>
	 * <ul>
	 *   <li><strong>[0]:</strong> Line number (1-based)</li>
	 *   <li><strong>[1]:</strong> Character offset from start of file (0-based)</li>
	 * </ul>
	 * 
	 * <p>This information is essential for:</p>
	 * <ul>
	 *   <li>Displaying errors at correct locations in editors</li>
	 *   <li>Navigating to error positions</li>
	 *   <li>Highlighting problematic code regions</li>
	 * </ul>
	 *
	 * @param source the EMF object to get location information for
	 * @return array of [lineNumber, offset], or appropriate defaults if location unavailable
	 */
	int[] getStartLineAndOffsetInFileInfo(EObject source);

}