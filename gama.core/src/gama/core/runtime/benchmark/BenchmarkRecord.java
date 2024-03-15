/*******************************************************************************************************
 *
 * BenchmarkRecord.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.runtime.benchmark;

import java.util.concurrent.atomic.LongAdder;

import gama.core.common.interfaces.IBenchmarkable;

/**
 * The Class BenchmarkRecord.
 */
public class BenchmarkRecord {

	/** The null. */
	public static final BenchmarkRecord NULL = new BenchmarkRecord(() -> "unknown");
	
	/** The times. */
	public final LongAdder milliseconds = new LongAdder(), times = new LongAdder();
	
	/** The object. */
	public final IBenchmarkable object;

	/**
	 * Instantiates a new benchmark record.
	 *
	 * @param object the object
	 */
	public BenchmarkRecord(final IBenchmarkable object) {
		this.object = object;
	}

	/**
	 * Checks if is unrecorded.
	 *
	 * @return true, if is unrecorded
	 */
	public boolean isUnrecorded() {
		return times.longValue() == 0l;
	}

}
