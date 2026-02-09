/*******************************************************************************************************
 *
 * ISymbolFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.symbols.ISymbol;

/**
 * Written by drogoul Modified on 29 ao�t 2010
 *
 * @todo Description
 *
 */
@FunctionalInterface
public interface ISymbolFactory {

	/**
	 * Creates the.
	 *
	 * @param description
	 *            the description
	 * @return the i symbol
	 */
	ISymbol create(IDescription description);

}
