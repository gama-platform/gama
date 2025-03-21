package gama.core.common.interfaces;

import java.util.concurrent.Semaphore;

/**
 * Encapsulates a general purpose {@link Semaphore}, with optional control over the maximum amount of permits allowed
 */
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

	private GeneralSynchronizer(final int n, final int max) {
		semaphore = new Semaphore(n);
		this.max = max;
	}

	final Semaphore semaphore;
	int max;

	public void release() {
		if (semaphore.availablePermits() >= max) return;
		semaphore.release();
	}

	public void acquire() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void release(final int nb) {
		int already = semaphore.availablePermits();
		if (already >= max) return;
		semaphore.release(Math.min(max - already, nb));
	}

	public void acquire(final int n) {
		try {
			semaphore.acquire(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
