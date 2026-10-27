/*******************************************************************************************************
 *
 * IConstructor.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.object;

/**
 * Creates an object from the two construction arguments appropriate to its kind.
 *
 * @param <T> the created object type
 * @param <A> the first construction argument
 * @param <B> the second construction argument
 */
@FunctionalInterface
public interface IConstructor<T extends IObject, A, B> {

	T create(A first, B second);
}
