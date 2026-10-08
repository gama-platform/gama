/*******************************************************************************************************
 *
 * IExpressionDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.expressions;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.types.IType;
import gama.api.utils.interfaces.IDisposable;
import gama.api.utils.interfaces.IGamlable;

/**
 * Interface representing the description of an expression before it is compiled into an executable
 * {@link IExpression}. IExpressionDescription serves as an intermediate representation between parsed GAML syntax
 * (AST nodes) and compiled executable expressions.
 * 
 * <p>
 * Expression descriptions are created during GAML parsing and hold the structural and syntactic information needed to
 * compile expressions. They act as a bridge between the parsing phase and the compilation phase of GAML processing.
 * </p>
 * 
 * <h3>Expression Lifecycle:</h3>
 * <ol>
 * <li>GAML source code is parsed into AST nodes ({@link EObject})</li>
 * <li>AST nodes are transformed into IExpressionDescription objects</li>
 * <li>IExpressionDescription objects are compiled into {@link IExpression} instances via
 * {@link #compile(IDescription)}</li>
 * <li>IExpression instances can be evaluated at runtime</li>
 * </ol>
 * 
 * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li><b>Compilation:</b> Converting the description into an executable expression via {@link #compile(IDescription)}
 * </li>
 * <li><b>Caching:</b> Storing and retrieving compiled expressions to avoid recompilation</li>
 * <li><b>Type Information:</b> Providing type information before compilation via {@link #getDenotedType(IDescription)}
 * </li>
 * <li><b>Serialization:</b> Converting back to GAML source code via {@link #serializeToGaml(boolean)}</li>
 * <li><b>Analysis:</b> Extracting string literals and identifiers for validation</li>
 * </ul>
 * 
 * <h3>Compilation Context:</h3>
 * <p>
 * Expression descriptions require a compilation context ({@link IDescription}) that provides:
 * </p>
 * <ul>
 * <li>Variable and attribute scope information</li>
 * <li>Type resolution capabilities</li>
 * <li>Species and model context</li>
 * <li>Error reporting mechanisms</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>
 * // Creating and compiling an expression description
 * IExpressionDescription desc = ...; // From parser
 * IDescription context = ...; // Compilation context
 * IExpression expr = desc.compile(context);
 * 
 * // Checking if already compiled
 * IExpression cached = desc.getExpression();
 * if (cached == null) {
 * 	cached = desc.compile(context);
 * }
 * 
 * // Getting type before compilation
 * IType<?> denotedType = desc.getDenotedType(context);
 * 
 * // Serializing back to GAML
 * String gamlCode = desc.serializeToGaml(false);
 * 
 * // Creating a label expression
 * IExpressionDescription labelDesc = desc.compileAsLabel();
 * </pre>
 * 
 * <h3>Special Expression Types:</h3>
 * <p>
 * Some expression descriptions can be compiled as labels via {@link #compileAsLabel()}, which creates a special form
 * suitable for display labels that don't require full evaluation.
 * </p>
 * 
 * <h3>Constant Expressions:</h3>
 * <p>
 * Expression descriptions that represent constants can be detected via {@link #isConst()}, allowing for optimization
 * and early validation.
 * </p>
 * 
 * <h3>Resource Management:</h3>
 * <p>
 * As {@link IDisposable}, expression descriptions should be disposed when no longer needed. This typically involves
 * disposing any cached compiled expressions and releasing references to AST nodes.
 * </p>
 * 
 * <h3>Thread Safety:</h3>
 * <p>
 * Expression descriptions are generally not thread-safe. Compilation should occur in a single thread, though the
 * resulting compiled expressions may be evaluated concurrently in different scopes.
 * </p>
 * 
 * @author drogoul
 * @since 31 mars 2012
 * @see IExpression
 * @see IExpressionCompiler
 * @see IDescription
 */
public interface IExpressionDescription extends IGamlable, IDisposable {

	/**
	 * Stores a compiled expression in this description for later retrieval. This caches the expression to avoid
	 * recompilation when {@link #compile(IDescription)} is called multiple times.
	 * 
	 * @param expr
	 *            the compiled expression to cache, may be null to clear the cache
	 */
	void setExpression(final IExpression expr);

	/**
	 * Compiles this expression description into an executable expression within the given context. The context
	 * provides necessary scope information for resolving variables, types, and species references.
	 * 
	 * <p>
	 * If an expression has already been compiled and cached via {@link #setExpression(IExpression)}, this method may
	 * return the cached instance. Otherwise, it performs full compilation using the context's expression compiler.
	 * </p>
	 * 
	 * <p>
	 * Compilation may involve:
	 * </p>
	 * <ul>
	 * <li>Type checking and inference</li>
	 * <li>Variable and operator resolution</li>
	 * <li>Constant folding and optimization</li>
	 * <li>Error validation and reporting</li>
	 * </ul>
	 * 
	 * @param context
	 *            the compilation context providing scope and type information, must not be null
	 * @return the compiled executable expression, or null if compilation fails
	 * @see IExpressionCompiler#compile(IExpressionDescription, IDescription)
	 */
	IExpression compile(final IDescription context);

	/**
	 * Retrieves the cached compiled expression, if one has been set via {@link #setExpression(IExpression)} or a
	 * previous {@link #compile(IDescription)} call.
	 * 
	 * @return the cached expression, or null if no expression has been compiled or cached
	 */
	IExpression getExpression();

	/**
	 * Creates a label-specific version of this expression description. Label expressions are simplified forms used
	 * for display purposes that may not require full evaluation capabilities.
	 * 
	 * <p>
	 * Label compilation is used in contexts where expressions are converted to strings for display, such as in agent
	 * labels or chart titles.
	 * </p>
	 * 
	 * @return a new expression description configured for label compilation
	 */
	IExpressionDescription compileAsLabel();

	/**
	 * Checks if this expression description represents a string literal that equals the given string.
	 * 
	 * <p>
	 * This is a convenience method for quick string literal comparisons without requiring full compilation.
	 * </p>
	 * 
	 * @param o
	 *            the string to compare against
	 * @return true if this description represents a string literal equal to the given string, false otherwise
	 */
	boolean equalsString(String o);

	/**
	 * Returns the AST node (EObject) that this expression description was created from during parsing.
	 * 
	 * @return the source AST node, may be null for synthetic expression descriptions
	 * @see #setTarget(EObject)
	 */
	EObject getTarget();

	/**
	 * Sets the AST node that this expression description represents. This links the description back to the parsed
	 * source code for error reporting and source mapping.
	 * 
	 * @param target
	 *            the source AST node to associate with this description
	 */
	void setTarget(EObject target);

	/**
	 * Indicates whether this expression description represents a constant value that doesn't depend on runtime scope.
	 * 
	 * <p>
	 * Constant expression descriptions can be detected before compilation, enabling early optimization and
	 * validation.
	 * </p>
	 * 
	 * @return true if this description represents a constant expression, false otherwise
	 * @see IExpression#isConst()
	 */
	boolean isConst();

	/**
	 * Extracts string literals and identifiers from this expression description. This is used for validation,
	 * auto-completion, and dependency analysis.
	 * 
	 * <p>
	 * The strings returned may include:
	 * </p>
	 * <ul>
	 * <li>Variable names referenced in the expression</li>
	 * <li>String literals contained in the expression</li>
	 * <li>Skill names if {@code skills} parameter is true</li>
	 * <li>Type names and species identifiers</li>
	 * </ul>
	 * 
	 * @param context
	 *            the context for resolving references
	 * @param skills
	 *            whether to include skill-related strings
	 * @return a collection of string values extracted from the expression, never null but may be empty
	 */
	Collection<String> getStrings(IDescription context, boolean skills);

	/**
	 * Creates a clean copy of this expression description without any cached compiled expression or mutable state.
	 * The copy represents the same expression structure but can be compiled independently.
	 * 
	 * @return a new expression description with the same structure but no cached state
	 */
	IExpressionDescription cleanCopy();

	/**
	 * Returns the GAML type denoted by this expression before compilation. For most expressions, this is their
	 * evaluation type, but for type literals, it's the type they represent.
	 * 
	 * <p>
	 * This method allows type checking before full compilation, which is useful for validation and early error
	 * detection.
	 * </p>
	 * 
	 * @param context
	 *            the context for type resolution
	 * @return the type denoted by this expression description
	 * @see IExpression#getDenotedType()
	 */
	IType<?> getDenotedType(IDescription context);

	/**
	 * Indicates whether this expression description should be compiled as a label rather than a normal expression.
	 * Label expressions are treated specially in display contexts.
	 * 
	 * @return true if this is a label expression description, false otherwise (default)
	 */
	default boolean isLabel() { return false; }

}