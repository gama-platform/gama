/*******************************************************************************************************
 *
 * ICollector.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.collections;

import java.io.Closeable;
import java.util.Collection;

import gama.api.utils.random.IRandom;

/**
 * The Interface ICollector.
 *
 * @param <E>
 *            the element type
 */
public interface ICollector<E> extends Collection<E>, Closeable {

	/**
	 * Items.
	 *
	 * @return the collection
	 */
	Collection<E> items();

	/**
	 * Shuffle in place with.
	 *
	 * @param random
	 *            the random
	 */
	default void shuffleInPlaceWith(final IRandom random) {
		random.shuffleInPlace(items());
	}

	/**
	 * Sets the.
	 *
	 * @param c
	 *            the c
	 */
	void set(final ICollector<?> c);

	/**
	 * To avoid the IOException
	 */
	@Override
	void close();

}