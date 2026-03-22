/*******************************************************************************************************
 *
 * StopWatch.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.utils.benchmark;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A lightweight stopwatch used during benchmarking to measure execution time of GAML symbols and agents.
 *
 * <p>
 * The {@link #NULL} singleton is returned by {@code GAMA.benchmark()} whenever benchmarking is disabled (i.e.
 * no experiment is being benchmarked). Its {@link #start()} and {@link #close()} methods are no-ops, so
 * {@code try (StopWatch w = GAMA.benchmark(scope, symbol)) { ... }} incurs zero allocation and zero timing
 * overhead when benchmarking is off.
 * </p>
 *
 * <p>
 * When benchmarking is active a fresh {@code StopWatch} instance is allocated per call to
 * {@link gama.api.utils.benchmark.Benchmark#record(gama.api.runtime.scope.IScope, IBenchmarkable)}.
 * </p>
 */
public class StopWatch implements Closeable {
	
	/**
	 * Singleton no-op stopwatch. Returned by {@code GAMA.benchmark()} when benchmarking is inactive.
	 * Its {@link #start()} and {@link #close()} methods are cheaply no-op: {@code start()} records a timestamp but
	 * that is never read because {@code close()} checks {@code lastStart != notRunning} (which is always
	 * {@code false} for the NULL instance since its initial {@code lastStart} is {@code notRunning}).
	 */
	public final static StopWatch NULL = new StopWatch(BenchmarkRecord.NULL, BenchmarkRecord.NULL) {
		/** Overridden to be a true no-op — avoids the volatile write to {@code lastStart} on every call. */
		@Override
		public StopWatch start() { return this; }

		/** Overridden to be a true no-op — avoids the {@code reentrant} CAS and timestamp subtraction. */
		@Override
		public void close() {}
	};
	
	/** The Constant notRunning. */
	final static long notRunning = -1;
	
	/** The scope. */
	private final BenchmarkRecord numbers, scope;
	
	/** The last start. */
	private long lastStart = notRunning;
	
	/** The reentrant. */
	private final AtomicInteger reentrant = new AtomicInteger();

	/**
	 * Instantiates a new stop watch.
	 *
	 * @param scope the scope
	 * @param numbers the numbers
	 */
	StopWatch(final BenchmarkRecord scope, final BenchmarkRecord numbers) {
		this.numbers = numbers;
		this.scope = scope;
	}

	/**
	 * Start.
	 *
	 * @return the stop watch
	 */
	public StopWatch start() {
		if (lastStart == notRunning) {
			lastStart = System.currentTimeMillis();
		}
		reentrant.incrementAndGet();
		return this;
	}

	@Override
	public void close() {
		if (lastStart != notRunning) {
			final int value = reentrant.decrementAndGet();
			if (value == 0) {
				final long milli = System.currentTimeMillis() - lastStart;
				numbers.milliseconds.add(milli);
				scope.milliseconds.add(milli);
				numbers.times.increment();
				lastStart = notRunning;
			}
		}
	}
}