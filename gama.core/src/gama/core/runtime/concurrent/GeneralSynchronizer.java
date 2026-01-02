/*******************************************************************************************************
 *
 * GeneralSynchronizer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.concurrent;

import java.util.concurrent.Semaphore;

import gama.annotations.precompiler.OkForAPI;

/**
 * Encapsulates a general purpose {@link Semaphore}, with optional control over the maximum amount of permits allowed
 */
@OkForAPI (OkForAPI.Location.UTILS)
public class GeneralSynchronizer {

	/**
	 * Returns a synchronizer with {@code init} initial permits and no maximum amount
	 *
	 * @param init
	 * @return
	 */
	public static GeneralSynchronizer withInitialPermits(final int init) {
		return withInitialAndMaxPermits(init, Integer.MAX_VALUE);
	}

	/**
	 * Returns a synchronizer with {@code init} initial permits and a maximum amount of permits of {@code max}
	 *
	 * @param init
	 * @param max
	 * @return
	 */
	public static GeneralSynchronizer withInitialAndMaxPermits(final int init, final int max) {
		return new GeneralSynchronizer(init, max);
	}

	/**
	 * Instantiates a new general synchronizer.
	 *
	 * @param n
	 *            the n
	 * @param max
	 *            the max
	 */
	private GeneralSynchronizer(final int n, final int max) {
		semaphore = new Semaphore(n);
		this.max = max;
	}

	/** The semaphore. */
	final Semaphore semaphore;

	/** The max. */
	int max;

	/**
	 * Release.
	 */
	public void release() {
		if (semaphore.availablePermits() >= max) return;
		semaphore.release();
	}

	/**
	 * Acquire.
	 */
	public void acquire() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Release.
	 *
	 * @param nb
	 *            the nb
	 */
	public void release(final int nb) {
		int already = semaphore.availablePermits();
		if (already >= max) return;
		semaphore.release(Math.min(max - already, nb));
	}

	/**
	 * Acquire.
	 *
	 * @param n
	 *            the n
	 */
	public void acquire(final int n) {
		try {
			semaphore.acquire(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
