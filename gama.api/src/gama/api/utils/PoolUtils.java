/*******************************************************************************************************
 *
 * PoolUtils.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils;

import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import gama.api.utils.interfaces.IDisposable;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 * The Class PoolUtils.
 */
public class PoolUtils {

	/** The pools. */
	static Set<ObjectPool> POOLS = new LinkedHashSet<>();

	/** The pool. */
	static public boolean POOL = true; // GamaPreferences.Experimental.USE_POOLING.getValue();
	static {
		DEBUG.ON();
		GamaPreferences.Experimental.USE_POOLING.onChange(v -> {
			POOLS.forEach(ObjectPool::dispose);
			POOL = v;
		});
	}

	/**
	 * Write stats.
	 */
	public static void writeStats() {
		DEBUG.SECTION("Pool statistics");
		POOLS.forEach(p -> {
			long accessedCount = p.accessed.get();
			long createdCount = p.created.get();
			long percentage = accessedCount == 0 ? 100 : 100 - (long) (createdCount * 100d / accessedCount);
			DEBUG.OUT(p.name, 30, "current size " + p.objects.size() + " / instances created " + createdCount
					+ " / instances asked " + accessedCount + " = " + percentage + "% of coverage");
		});
	}

	/**
	 * A factory for creating Object objects.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public interface ObjectFactory<T> {
		/**
		 * Creates a new Object object.
		 *
		 * @return the t
		 */
		T createNew();
	}

	/**
	 * The Interface ObjectCopy.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public interface ObjectCopy<T> {

		/**
		 * Creates the new.
		 *
		 * @param copyFrom
		 *            the copy from
		 * @param copyTo
		 *            the copy to
		 */
		void createNew(T copyFrom, T copyTo);
	}

	/**
	 * The Interface ObjectCleaner.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public interface ObjectCleaner<T> {
		/**
		 * Clean.
		 *
		 * @param object
		 *            the object
		 */
		void clean(T object);
	}

	/**
	 * The Class ObjectPool.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public static class ObjectPool<T> implements IDisposable {

		/** The name. */
		private String name;

		/** The created. */
		private final AtomicLong accessed = new AtomicLong(0);

		/** The released. */
		private final AtomicLong released = new AtomicLong(0);

		/** The created. */
		private final AtomicLong created = new AtomicLong(0);

		/** The factory. */
		private final ObjectFactory<T> factory;

		/** The copy. */
		private final ObjectCopy<T> copy;

		/** The cleaner. */
		private final ObjectCleaner<T> cleaner;

		/** The objects. */
		private final Queue<T> objects;

		/** The active. */
		public volatile boolean active;

		/**
		 * Instantiates a new object pool.
		 *
		 * @param factory
		 *            the factory
		 * @param copy
		 *            the copy
		 * @param cleaner
		 *            the cleaner
		 */
		private ObjectPool(final ObjectFactory<T> factory, final ObjectCopy<T> copy, final ObjectCleaner<T> cleaner) {
			this.factory = factory;
			this.copy = copy;
			this.cleaner = cleaner;
			objects = new ConcurrentLinkedQueue<>();
		}

		/**
		 * Gets the.
		 *
		 * @return the t
		 */
		public T get() {
			if (!active) return factory.createNew();
			accessed.incrementAndGet();

			T result = objects.poll();
			if (result == null) {
				created.incrementAndGet();
				result = factory.createNew();
			}
			return result;
		}

		/**
		 * Gets the.
		 *
		 * @param from
		 *            the from
		 * @return the t
		 */
		public T get(final T from) {
			T result = get();
			if (copy != null) { copy.createNew(from, result); }
			return result;
		}

		/**
		 * Release a single object back to the pool (optimized fast path).
		 *
		 * @param t
		 *            the object to release
		 */
		public void release(final T t) {
			if (t == null) return;
			if (cleaner != null) { cleaner.clean(t); }
			if (active) {
				released.incrementAndGet();
				objects.offer(t);
			}
		}

		/**
		 * Release multiple objects back to the pool.
		 *
		 * @param tt
		 *            the objects to release
		 */
		public void release(@SuppressWarnings ("unchecked") final T... tt) {
			if (tt == null) return;
			// Fast path for single object
			if (tt.length == 1) {
				release(tt[0]);
				return;
			}
			// Batch release for multiple objects
			for (T t : tt) { release(t); }
		}

		@Override
		public void dispose() {
			objects.clear();
		}
	}

	/**
	 * Creates a new object pool
	 *
	 * @param <T>
	 *            the type of objects created and maintained in the poool
	 * @param name
	 *            the name of the pool
	 * @param active
	 *            whether or not it is active
	 * @param factory
	 *            the factory to create new objects
	 * @param copy
	 *            the factory to create new objects from existing ones
	 * @param cleaner
	 *            the code to execute to return the object to its pristine state
	 * @return
	 */

	public static <T> ObjectPool<T> create(final String name, final boolean active, final ObjectFactory<T> factory,
			final ObjectCopy<T> copy, final ObjectCleaner<T> cleaner) {
		DEBUG.BANNER(BANNER_CATEGORY.POOL, "Object pool added", "for", name);
		final ObjectPool<T> result = new ObjectPool<>(factory, copy, cleaner);
		result.active = POOL && active;
		result.name = name;
		POOLS.add(result);
		return result;
	}

}
