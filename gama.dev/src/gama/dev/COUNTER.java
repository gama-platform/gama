/*******************************************************************************************************
 *
 * COUNTER.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Class COUNTER. A simple way to get unique indexes for various objects
 */
public class COUNTER {

	/** The Constant COUNTERS. */
	private static final ConcurrentHashMap<String, Long> COUNTERS = new ConcurrentHashMap<>();

	/** The count. */
	private static AtomicLong COUNT = new AtomicLong();

	/**
	 * Returns a unique integer
	 *
	 * @return the long
	 */
	public static long GET_UNIQUE() {
		return COUNT.incrementAndGet();
	}

	/**
	 * Returns an automatically incremented integer count proper to the class of the calling object. Useful for counting
	 * a number of invocations, etc. without having to define a static number on the class
	 *
	 * @return 0 if it is the first call, otherwise an incremented integer
	 */
	public static Long COUNT() {
		final String s = DEBUG.findCallingClassName();
		Long result = -1l;
		if (COUNTERS.containsKey(s)) {
			result = COUNTERS.get(s) + 1;
		} else {
			result = 0l;
		}
		COUNTERS.put(s, result);
		return result;
	}

}
