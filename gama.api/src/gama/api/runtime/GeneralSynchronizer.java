/*******************************************************************************************************
 *
 * GeneralSynchronizer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.concurrent.Semaphore;

/**
 * A general-purpose synchronization utility that encapsulates a {@link Semaphore} with optional maximum permit
 * control.
 * 
 * <p>
 * GeneralSynchronizer provides a simplified interface to Java's semaphore mechanism while adding the ability to cap
 * the maximum number of permits. This is useful for controlling concurrent access to resources in GAMA simulations,
 * particularly for coordinating parallel simulation execution.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Wraps {@link Semaphore} with a cleaner API</li>
 * <li>Supports optional maximum permit limits to prevent permit accumulation</li>
 * <li>Handles InterruptedException internally for simpler client code</li>
 * <li>Provides both single and bulk acquire/release operations</li>
 * </ul>
 * 
 * <p>
 * Typical use case in GAMA: Coordinating the execution of multiple simulations where an experiment thread waits for
 * all simulation threads to complete their step before proceeding to the next cycle.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * // Create a synchronizer for 3 simulations with max 10 permits
 * GeneralSynchronizer sync = GeneralSynchronizer.withInitialAndMaxPermits(0, 10);
 * 
 * // In simulation threads: signal completion
 * sync.release(); // Each simulation releases when done
 * 
 * // In experiment thread: wait for all simulations
 * sync.acquire(3); // Wait for 3 permits (one from each simulation)
 * </pre>
 * 
 * @see Semaphore
 * @see SimulationRunner
 */
public class GeneralSynchronizer {

	/**
	 * Creates a synchronizer with the specified initial permits and no maximum limit.
	 * 
	 * <p>
	 * The returned synchronizer can accumulate an unlimited number of permits through successive
	 * {@link #release()} calls.
	 * </p>
	 * 
	 * @param init
	 *            the initial number of permits available (can be 0 or positive)
	 * @return a new GeneralSynchronizer with the specified initial permits and no maximum
	 */
	public static GeneralSynchronizer withInitialPermits(final int init) {
		return withInitialAndMaxPermits(init, Integer.MAX_VALUE);
	}

	/**
	 * Creates a synchronizer with the specified initial permits and a maximum permit limit.
	 * 
	 * <p>
	 * The maximum limit prevents the synchronizer from accumulating more than {@code max} permits. When the limit is
	 * reached, additional {@link #release()} calls have no effect. This is useful for preventing unbounded permit
	 * growth in scenarios where releases might outpace acquisitions.
	 * </p>
	 * 
	 * @param init
	 *            the initial number of permits available (can be 0 or positive)
	 * @param max
	 *            the maximum number of permits that can be accumulated
	 * @return a new GeneralSynchronizer with the specified initial and maximum permits
	 */
	public static GeneralSynchronizer withInitialAndMaxPermits(final int init, final int max) {
		return new GeneralSynchronizer(init, max);
	}

	/**
	 * Constructs a new GeneralSynchronizer with the specified initial permits and maximum limit.
	 * 
	 * @param n
	 *            the initial number of permits
	 * @param max
	 *            the maximum number of permits allowed
	 */
	private GeneralSynchronizer(final int n, final int max) {
		semaphore = new Semaphore(n);
		this.max = max;
	}

	/** The underlying semaphore controlling permit access. */
	final Semaphore semaphore;

	/** The maximum number of permits that can be accumulated. */
	int max;

	/**
	 * Releases one permit, returning it to the synchronizer.
	 * 
	 * <p>
	 * If the current number of available permits has already reached the maximum, this call has no effect. Otherwise,
	 * one permit is added, potentially unblocking a thread waiting in {@link #acquire()}.
	 * </p>
	 */
	public void release() {
		if (semaphore.availablePermits() >= max) return;
		semaphore.release();
	}

	/**
	 * Acquires one permit from the synchronizer, blocking if necessary until one is available.
	 * 
	 * <p>
	 * This method blocks the calling thread until a permit becomes available. If interrupted while waiting, the
	 * interruption is caught and the stack trace is printed, but the method continues to wait.
	 * </p>
	 */
	public void acquire() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Releases the specified number of permits, up to the maximum allowed.
	 * 
	 * <p>
	 * This method adds up to {@code nb} permits to the synchronizer. If adding all permits would exceed the maximum,
	 * only enough permits are added to reach the maximum. If the maximum is already reached, no permits are added.
	 * </p>
	 * 
	 * @param nb
	 *            the number of permits to release
	 */
	public void release(final int nb) {
		int already = semaphore.availablePermits();
		if (already >= max) return;
		semaphore.release(Math.min(max - already, nb));
	}

	/**
	 * Acquires the specified number of permits from the synchronizer, blocking if necessary.
	 * 
	 * <p>
	 * This method blocks the calling thread until the requested number of permits become available. If interrupted
	 * while waiting, the interruption is caught and the stack trace is printed, but the method continues to wait.
	 * </p>
	 * 
	 * @param n
	 *            the number of permits to acquire
	 */
	public void acquire(final int n) {
		try {
			semaphore.acquire(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
