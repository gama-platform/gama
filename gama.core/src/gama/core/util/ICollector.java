/*******************************************************************************************************
 *
 * ICollector.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util;

import java.io.Closeable;
import java.util.Collection;

import gama.core.common.util.PoolUtils;
import gama.core.common.util.RandomUtils;

/**
 * The Interface ICollector.
 *
 * @param <E> the element type
 */
public interface ICollector<E> extends Collection<E>, Closeable {

	/**
	 * Items.
	 *
	 * @return the collection
	 */
	Collection<E> items();

	/**
	 * Close.
	 */
	@Override
	default void close() {
		if (PoolUtils.POOL) { Collector.release(this); }
	}

	/**
	 * Shuffle in place with.
	 *
	 * @param random the random
	 */
	default void shuffleInPlaceWith(final RandomUtils random) {
		random.shuffleInPlace(items());
	}

	/**
	 * Sets the.
	 *
	 * @param c the c
	 */
	void set(final ICollector<?> c);

}