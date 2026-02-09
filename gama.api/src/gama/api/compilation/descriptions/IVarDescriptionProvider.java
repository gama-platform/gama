/*******************************************************************************************************
 *
 * IVarDescriptionProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import gama.api.gaml.expressions.IExpression;

/**
 * The Interface IVarDescriptionProvider.
 */
public interface IVarDescriptionProvider {

	/**
	 * If asField is true, then should not return a GlobalVarExpression, but a normal var expression
	 *
	 * @param name
	 * @param asField
	 * @return
	 */
	IExpression getVarExpr(final String name, boolean asField);

	/**
	 * Checks for attribute.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	boolean hasAttribute(String name);

}
