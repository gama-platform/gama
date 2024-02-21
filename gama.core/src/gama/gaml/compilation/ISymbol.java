/*******************************************************************************************************
 *
 * ISymbol.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import org.eclipse.emf.common.util.URI;

import gama.core.common.interfaces.IDisposable;
import gama.core.runtime.IScope;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.INamed;

/**
 * Written by drogoul Modified on 19 mars 2010
 *
 * @todo Description
 *
 */
public interface ISymbol extends INamed, IDisposable {

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	int getOrder();

	/**
	 * Sets the order.
	 *
	 * @param o
	 *            the new order
	 */
	void setOrder(int o);

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	IDescription getDescription();

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	URI getURI();

	/**
	 * Returns the expression located at the first facet of 'keys'
	 *
	 * @param keys
	 * @return
	 */
	IExpression getFacet(String... keys);

	/**
	 * Checks for facet.
	 *
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	boolean hasFacet(String key);

	/**
	 * Sets the children.
	 *
	 * @param children
	 *            the new children
	 */
	void setChildren(Iterable<? extends ISymbol> children);

	/**
	 * Gets the trace.
	 *
	 * @param abstractScope
	 *            the abstract scope
	 * @return the trace
	 */
	String getTrace(IScope abstractScope);

	/**
	 * Gets the keyword.
	 *
	 * @return the keyword
	 */
	String getKeyword();

	/**
	 * Sets the enclosing.
	 *
	 * @param enclosing
	 *            the new enclosing
	 */
	void setEnclosing(ISymbol enclosing);

}
