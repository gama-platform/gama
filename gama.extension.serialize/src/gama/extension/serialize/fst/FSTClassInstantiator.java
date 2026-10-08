/*******************************************************************************************************
 *
 * FSTClassInstantiator.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst;

import java.lang.reflect.Constructor;

/**
 * Created by ruedi on 12.12.14.
 */
public interface FSTClassInstantiator {

	/**
	 * New instance.
	 *
	 * @param clazz
	 *            the clazz
	 * @param cons
	 *            the cons
	 * @param doesRequireInit
	 *            the does require init
	 * @return the object
	 */
	Object newInstance(Class clazz, Constructor cons, boolean doesRequireInit);

	/**
	 * Find constructor for externalize.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the constructor
	 */
	Constructor findConstructorForExternalize(Class clazz);

	/**
	 * Find constructor for serializable.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the constructor
	 */
	Constructor findConstructorForSerializable(Class clazz);

}
