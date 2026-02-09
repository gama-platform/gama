/*******************************************************************************************************
 *
 * IExpressionCompiler.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.expressions;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.runtime.IExecutionContext;
import gama.api.utils.IDisposable;

/**
 * Interface for GAML expression compilers that transform GAML language constructs into executable expressions.
 * This interface defines the contract for compiling various forms of GAML expressions including strings,
 * expression descriptions, and statement blocks into executable IExpression objects.
 * 
 * <p>Expression compilers are responsible for:</p>
 * <ul>
 * <li>Parsing and validating GAML syntax</li>
 * <li>Resolving variable, type, and action references</li>
 * <li>Type checking and inference</li>
 * <li>Creating optimized expression trees</li>
 * <li>Argument parsing and validation for action calls</li>
 * <li>Error reporting and validation</li>
 * </ul>
 * 
 * <p>The compiler operates within compilation contexts that provide scope information for
 * variable resolution, type management, and species context. It supports both single expression
 * compilation and block compilation for statement sequences.</p>
 * 
 * <h3>Thread Safety:</h3>
 * <p>Implementations should document their thread safety guarantees. The interface itself does not
 * mandate thread safety, but implementations may choose to provide it for concurrent compilation scenarios.</p>
 * 
 * <h3>Resource Management:</h3>
 * <p>As this interface extends IDisposable, implementations should properly clean up resources
 * when dispose() is called, including any cached expressions, temporary resources, or context references.</p>
 * 
 * @param <T> the specific type of expression AST nodes that this compiler can handle
 * 
 * @author GAMA Development Team
 * @since GAMA 1.0
 * @see IExpression
 * @see IExpressionDescription  
 * @see Arguments
 * @see IDescription
 */
public interface IExpressionCompiler<T> extends IDisposable {

	/**
	 * Compiles an expression description into an executable expression within the specified parsing context.
	 * This method takes a pre-parsed expression description and transforms it into an executable IExpression.
	 * The parsing context provides the necessary scope information for variable resolution, type checking,
	 * and validation.
	 * 
	 * <p>This method handles:</p>
	 * <ul>
	 * <li>Expression description validation and compilation</li>
	 * <li>Context-sensitive variable and type resolution</li>
	 * <li>Optimization of constant expressions</li>
	 * <li>Error reporting through the parsing context</li>
	 * </ul>
	 * 
	 * @param s the expression description to compile (should not be null)
	 * @param parsingContext the description context providing scope and validation information
	 * @return the compiled executable expression, or null if compilation fails
	 */
	IExpression compile(final IExpressionDescription s, final IDescription parsingContext);

	/**
	 * Compiles a string expression into an executable expression within the specified contexts.
	 * This method parses the string expression using GAML syntax and creates an executable IExpression.
	 * It provides more direct string-to-expression compilation with support for temporary execution contexts.
	 * 
	 * <p>This method is useful for:</p>
	 * <ul>
	 * <li>Dynamic expression compilation from user input</li>
	 * <li>Runtime expression creation and evaluation</li>
	 * <li>String-based expression caching and optimization</li>
	 * <li>Temporary expression compilation for validation</li>
	 * </ul>
	 * 
	 * @param expression the string expression to parse and compile (GAML syntax)
	 * @param parsingContext the description context providing scope and validation information
	 * @param tempContext the temporary execution context for runtime information and error reporting
	 * @return the compiled executable expression, or null if compilation fails
	 */
	IExpression compile(final String expression, final IDescription parsingContext, IExecutionContext tempContext);

	/**
	 * Parses and validates arguments for action calls from various AST node formats.
	 * This method extracts argument information from expression AST nodes and creates
	 * an Arguments object that maps parameter names to their expression descriptions.
	 * It supports multiple argument syntaxes including named arguments and positional arguments.
	 * 
	 * <p>Supported argument formats:</p>
	 * <ul>
	 * <li>Named arguments: [a1::v1, a2::v2] or (a1:v1, a2:v2)</li>
	 * <li>Positional arguments: (v1, v2)</li>
	 * <li>Mixed formats with automatic parameter binding</li>
	 * </ul>
	 * 
	 * @param action the action description containing expected parameter information for validation
	 * @param eObject the AST node containing argument expressions (typically Array or ExpressionList)
	 * @param context the description context for error reporting and validation
	 * @param compileArgValues whether to compile argument expressions immediately or defer compilation
	 * @return the parsed arguments map with parameter names as keys, or null if parsing fails
	 */
	Arguments parseArguments(IActionDescription action, EObject eObject, IDescription context,
			boolean compileArgValues);

	/**
	 * Compiles a block of GAML statements from a string into a list of description objects.
	 * This method parses a string containing multiple GAML statements or a single complex statement
	 * and creates a list of IDescription objects that can be executed sequentially. It is primarily
	 * used for compiling action bodies, conditional blocks, and loop bodies.
	 * 
	 * <p>This method handles:</p>
	 * <ul>
	 * <li>Multi-statement block parsing and validation</li>
	 * <li>Individual statement compilation and description creation</li>
	 * <li>Context-sensitive compilation within action or block scopes</li>
	 * <li>Error reporting for syntax and semantic issues</li>
	 * </ul>
	 * 
	 * @param string the string containing GAML statements to compile (can contain multiple statements)
	 * @param actionContext the description context providing scope for the compiled statements
	 * @param tempContext the temporary execution context for runtime information and error reporting
	 * @return the list of compiled statement descriptions, or null if compilation fails
	 * @throws gama.api.exceptions.GamaRuntimeException if parsing or compilation errors occur
	 */
	List<IDescription> compileBlock(final String string, final IDescription actionContext,
			IExecutionContext tempContext);

}