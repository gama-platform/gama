/*******************************************************************************************************
 *
 * IExpressionDescriptionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;

/**
 * Factory interface for creating {@link IExpressionDescription} objects from various data sources.
 * <p>
 * This factory provides a centralized way to create expression descriptions, which are intermediate representations
 * used in GAMA's compilation process. Expression descriptions can later be compiled into executable {@link IExpression}
 * objects.
 * </p>
 * <p>
 * The factory supports creating expression descriptions from:
 * </p>
 * <ul>
 * <li>Constant values (Object, Integer, Double, Boolean)</li>
 * <li>String-based expressions</li>
 * <li>Labels</li>
 * <li>Existing compiled expressions</li>
 * </ul>
 * <p>
 * Some methods provide caching capabilities for performance optimization, while others explicitly bypass caching when
 * needed.
 * </p>
 *
 * @author drogoul
 * @since 2025-03
 * @see IExpressionDescription
 * @see IExpression
 */
public interface IExpressionDescriptionFactory {

	/**
	 * Gets the true.
	 *
	 * @return the true
	 */
	IExpressionDescription getTrue();

	/**
	 * Gets the false.
	 *
	 * @return the false
	 */
	IExpressionDescription getFalse();

	/**
	 * Gets the null.
	 *
	 * @return the null
	 */
	IExpressionDescription getNull();

	/**
	 * Creates an expression description for a constant value of any type.
	 * <p>
	 * This method is cached for performance optimization, meaning that subsequent calls with the same value may return
	 * the same instance. Use {@link #createConstantNoCache(Object)} if caching is not desired.
	 * </p>
	 *
	 * @param val
	 *            the constant value to create an expression description for. Can be any Object type including
	 *            primitives boxed to their wrapper types (Integer, Double, Boolean, String, etc.)
	 * @return an IExpressionDescription representing the constant value
	 */
	IExpressionDescription createConstant(Object val);

	/**
	 * Creates an expression description for an integer constant value.
	 * <p>
	 * This is a specialized version of {@link #createConstant(Object)} for Integer values, providing type-safe creation
	 * with caching capabilities.
	 * </p>
	 *
	 * @param val
	 *            the Integer constant value to create an expression description for
	 * @return an IExpressionDescription representing the integer constant
	 */
	IExpressionDescription createConstant(Integer val);

	/**
	 * Creates an expression description for a double constant value.
	 * <p>
	 * This is a specialized version of {@link #createConstant(Object)} for Double values, providing type-safe creation
	 * with caching capabilities.
	 * </p>
	 *
	 * @param val
	 *            the Double constant value to create an expression description for
	 * @return an IExpressionDescription representing the double constant
	 */
	IExpressionDescription createConstant(Double val);

	/**
	 * Creates an expression description for a boolean constant value.
	 * <p>
	 * This is a specialized version of {@link #createConstant(Object)} for Boolean values, providing type-safe creation
	 * with caching capabilities.
	 * </p>
	 *
	 * @param val
	 *            the Boolean constant value to create an expression description for
	 * @return an IExpressionDescription representing the boolean constant
	 */
	IExpressionDescription createConstant(Boolean val);

	/**
	 * Creates an expression description for a constant value without using any caching mechanism.
	 * <p>
	 * This method explicitly bypasses caching and always creates a new instance. Use this method when you need to
	 * ensure that each call returns a distinct object, or when caching might interfere with the intended behavior.
	 * </p>
	 *
	 * @param val
	 *            the constant value to create an expression description for. Can be any Object type
	 * @return a new IExpressionDescription representing the constant value, never cached
	 * @see #createConstant(Object) for the cached version
	 */
	IExpressionDescription createConstantNoCache(Object val);

	/**
	 * Creates an expression description for a label.
	 * <p>
	 * Labels are identifiers used in GAML that can reference variables, attributes, or other named elements within the
	 * model context. This method creates an expression description that represents such a label reference.
	 * </p>
	 *
	 * @param val
	 *            the string value representing the label identifier
	 * @return an IExpressionDescription representing the label
	 */
	IExpressionDescription createLabel(String val);

	/**
	 * Creates an expression description from an existing compiled expression.
	 * <p>
	 * This method wraps an already compiled {@link IExpression} into an {@link IExpressionDescription}. This is useful
	 * when you have a compiled expression that needs to be used in contexts where expression descriptions are expected,
	 * or when creating composite expressions.
	 * </p>
	 *
	 * @param expr
	 *            the compiled IExpression to wrap in an expression description
	 * @return an IExpressionDescription that wraps the provided expression
	 */
	IExpressionDescription createBasic(IExpression expr);

	/**
	 * Creates an expression description from a string-based expression.
	 * <p>
	 * This method parses a string containing a GAML expression and creates an expression description from it. The
	 * string can contain any valid GAML expression syntax including operations, function calls, variable references,
	 * and complex expressions.
	 * </p>
	 *
	 * @param string
	 *            the string containing the GAML expression to parse and create a description for
	 * @return an IExpressionDescription representing the parsed string expression
	 */
	IExpressionDescription createStringBased(String string);

	/**
	 * Creates a new IExpressionDescription object.
	 *
	 * @param object
	 *            the object
	 * @return the i expression description
	 */
	IExpressionDescription createFromEObject(EObject object);

	/**
	 * @param expr
	 * @return
	 */
	IExpressionDescription createBlock(ISyntacticElement expr);

}
