/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The expressions package provides the expression system for GAML.
 * 
 * <p>This package contains interfaces and implementations for GAML expressions, which are
 * evaluable constructs that produce values during simulation execution.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.gaml.expressions.IExpression} - Base interface for all expressions</li>
 *   <li>{@link gama.api.gaml.expressions.IExpressionFactory} - Factory for creating expressions</li>
 * </ul>
 * 
 * <h2>Expression Types</h2>
 * 
 * <p>GAML supports various expression types:</p>
 * <ul>
 *   <li><strong>Literals:</strong> Constant values (numbers, strings, booleans)</li>
 *   <li><strong>Variable References:</strong> Access to agent attributes</li>
 *   <li><strong>Operators:</strong> Binary and unary operations</li>
 *   <li><strong>Function Calls:</strong> Invocation of operators and actions</li>
 *   <li><strong>Casts:</strong> Type conversion expressions</li>
 *   <li><strong>Closures:</strong> Anonymous functions and code blocks</li>
 * </ul>
 * 
 * <h2>Expression Evaluation</h2>
 * 
 * <p>Expressions are evaluated in a runtime scope:</p>
 * <pre>{@code
 * IExpression expr = GAML.compileExpression("2 + 2", scope);
 * Object result = expr.value(scope);  // Returns 4
 * }</pre>
 * 
 * <h2>Type Information</h2>
 * 
 * <p>Expressions provide type information:</p>
 * <ul>
 *   <li>Return type (getGamlType())</li>
 *   <li>Content type for containers</li>
 *   <li>Constant evaluation capability</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.expressions.IExpression
 * @see gama.api.gaml.GAML
 */
package gama.api.gaml.expressions;
