/*******************************************************************************************************
 *
 * IScoped.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.interfaces;

import gama.annotations.precompiler.OkForAPI;
import gama.core.runtime.IScope;

/**
 * The Interface IScoped.
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface IScoped {

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	IScope getScope();

}
