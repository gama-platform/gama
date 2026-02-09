/*******************************************************************************************************
 *
 * IGamaGetter.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.additions;

import gama.api.runtime.scope.IScope;

/**
 * The Interface IGamaGetter.
 *
 * @param <T> the generic type
 */
@FunctionalInterface
public interface IGamaGetter<T> {
	
	/**
	 * Gets the.
	 *
	 * @param scope the scope
	 * @param arguments the arguments
	 * @return the t
	 */
	T get(IScope scope, Object... arguments);

}
