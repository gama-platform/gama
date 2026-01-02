/*******************************************************************************************************
 *
 * IArguments.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.Map;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.BiConsumerWithPruning;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IDisposable;

/**
 *
 */
public interface IArguments extends IDisposable, Map<String, IExpressionDescription> {

	/**
	 * Resolve against.
	 *
	 * @param scope
	 *            the scope
	 * @return the arguments
	 */
	Arguments resolveAgainst(IScope scope);

	/**
	 * Put.
	 *
	 * @param s
	 *            the s
	 * @param e
	 *            the e
	 * @return the i expression description
	 */
	@Override
	IExpressionDescription put(String s, IExpressionDescription e);

	/**
	 * Removes the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param s
	 *            the s
	 * @return the i expression description
	 * @date 27 déc. 2023
	 */
	IExpressionDescription remove(String s);

	/**
	 * Sets the caller.
	 *
	 * @param caller
	 *            the new caller
	 */
	void setCaller(IAgent caller);

	/**
	 * Gets the caller.
	 *
	 * @return the caller
	 */
	IAgent getCaller();

	/**
	 * Gets the expr.
	 *
	 * @param index
	 *            the index
	 * @return the expr
	 */
	IExpression getExpr(int index);

	/**
	 * For each argument.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean forEachArgument(final BiConsumerWithPruning<String, IExpressionDescription> visitor);

	/**
	 * @param formalArgs
	 */
	void complementWith(IArguments formalArgs);

	/**
	 * @param args
	 */

}