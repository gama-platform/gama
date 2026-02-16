/*******************************************************************************************************
 *
 * GamaListFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.date.IDate;
import gama.api.types.geometry.IPoint;
import gama.api.types.misc.IContainer;
import gama.dev.FLAGS;

/**
 * A static factory for creating and managing {@link IList} instances. This class serves as a frontend for list
 * creation, delegating to concrete implementations ({@link GamaList}, {@link GamaListWrapper}, etc.). It supports
 * creation from various sources (arrays, streams, collections) and handles GAML-specific type casting and wrapping
 * logic.
 *
 * <h2>Factory Method Categories</h2>
 * <ul>
 * <li><b>Core creation methods</b> - {@link #create(IType)}, {@link #create(IType, int)} - Create empty lists</li>
 * <li><b>Scope-aware methods</b> - {@link #create(IScope, IType, Object...)} - Cast elements according to GAML
 * contracts</li>
 * <li><b>Wrapping methods</b> - {@link #wrap(IType, List)}, {@link #wrap(IType, Object[])} - Zero-copy wrappers for
 * existing data</li>
 * <li><b>Without-casting methods</b> - {@link #createWithoutCasting(IType, Object[])} - Skip type casting for
 * performance</li>
 * <li><b>Stream/Collector support</b> - {@link #toGamaList()}, {@link #create(IType, Stream)} - Integration with Java
 * Streams API</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>
 * // Creating an empty list with a specific content type
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(Types.INT);
 *
 * // Creating a list from an array with type casting (requires scope)
 * IList&lt;Double&gt; values = GamaListFactory.create(scope, Types.FLOAT, 1, 2.5, "3.14");
 *
 * // Creating a list without casting (for performance when types are guaranteed)
 * IList&lt;String&gt; strings = GamaListFactory.createWithoutCasting(Types.STRING, "a", "b", "c");
 *
 * // Wrapping an existing Java list (zero-copy, changes are bidirectional)
 * List&lt;IAgent&gt; javaList = new ArrayList&lt;&gt;();
 * IList&lt;IAgent&gt; gamaList = GamaListFactory.wrap(Types.AGENT, javaList);
 *
 * // Using with Java Streams
 * IList&lt;Integer&gt; streamResult = IntStream.range(0, 10).boxed().collect(GamaListFactory.toGamaList());
 *
 * // Converting objects to lists
 * IList&lt;?&gt; converted = GamaListFactory.toList(scope, someObject);
 * </pre>
 *
 * <h2>Performance Considerations</h2>
 * <ul>
 * <li><b>Wrapping vs. Copying</b>: {@code wrap()} methods provide zero-copy views but changes affect the original data.
 * {@code create()} methods always create new independent lists.</li>
 * <li><b>Casting Overhead</b>: Methods with {@link IScope} parameter perform type casting based on
 * {@code FLAGS.CAST_CONTAINER_CONTENTS}. Use {@code createWithoutCasting()} when type safety is guaranteed.</li>
 * <li><b>Parallel Filling</b>: {@link #create(IScope, IExpression, Integer, boolean)} supports parallel execution for
 * expression evaluation when creating large lists.</li>
 * </ul>
 *
 * <h2>Type Casting Behavior</h2>
 * <p>
 * All methods accepting an {@link IScope} strictly adhere to GAML container contracts, ensuring elements are cast to
 * the list's content type when {@code FLAGS.CAST_CONTAINER_CONTENTS} is enabled (default: true). Methods without a
 * scope generally wrap existing data to avoid unnecessary casting overhead.
 * </p>
 *
 * @author drogoul
 * @since GAMA 1.0
 * @see IList
 * @see GamaList
 * @see GamaListWrapper
 * @see GamaListArrayWrapper
 * @see GamaListCollectionWrapper
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaListFactory {

	/** The ch. */
	static final Set<Collector.Characteristics> CH =
			ImmutableSet.<Collector.Characteristics> of(Collector.Characteristics.IDENTITY_FINISH);

	/** The empty list. */
	public static IList<Object> EMPTY_LIST = wrap(Types.NO_TYPE, Collections.emptyList());

	// ================================================================================================
	// STREAM AND COLLECTOR SUPPORT
	// Methods for integrating with Java Streams API
	// ================================================================================================

	/**
	 * To gama list.
	 *
	 * @param <T>
	 *            the generic type
	 * @return the collector
	 */
	public static <T> Collector<T, IList<T>, IList<T>> toGamaList() {
		return new Collector<>() {

			@Override
			public Supplier<IList<T>> supplier() {
				return GamaListFactory::create;
			}

			@Override
			public BiConsumer<IList<T>, T> accumulator() {
				return IList::add;
			}

			@Override
			public BinaryOperator<IList<T>> combiner() {
				return (left, right) -> {
					left.addAll(right);
					return left;
				};
			}

			@Override
			public java.util.function.Function<IList<T>, IList<T>> finisher() {
				return left -> left;
			}

			@Override
			public Set<java.util.stream.Collector.Characteristics> characteristics() {
				return CH;
			}
		};
	}

	/** The to gama list. */
	public static final Collector<Object, IList<Object>, IList<Object>> TO_GAMA_LIST = toGamaList();

	/**
	 * Creates a list from a stream of elements.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param t
	 *            the content type of the list.
	 * @param stream
	 *            the source stream.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IType t, final Stream<T> stream) {
		return (IList<T>) stream.collect(TO_GAMA_LIST);
	}

	// ================================================================================================
	// CREATION WITHOUT CASTING
	// Methods for creating lists without type casting (performance-optimized)
	// ================================================================================================

	/**
	 * Creates a list from an array of objects without casting. Warning: The caller must ensure the objects are
	 * compatible with the content type.
	 *
	 * @param contentType
	 *            the expected content type.
	 * @param objects
	 *            the array of objects.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> createWithoutCasting(final IType contentType, final T... objects) {
		final IList<T> list = create(contentType, objects.length);
		Collections.addAll(list, objects);
		return list;
	}

	/**
	 * Creates a list from an array of integers without casting.
	 *
	 * @param contentType
	 *            the expected content type.
	 * @param objects
	 *            the array of integers.
	 * @return the created {@link IList}.
	 */
	public static IList<Integer> createWithoutCasting(final IType contentType, final int[] objects) {
		final IList<Integer> list = create(contentType, objects.length);
		for (int i : objects) { list.add(i); }
		return list;
	}

	/**
	 * Creates a list from an array of doubles without casting.
	 *
	 * @param contentType
	 *            the expected content type.
	 * @param objects
	 *            the array of doubles.
	 * @return the created {@link IList}.
	 */
	public static IList<Double> createWithoutCasting(final IType contentType, final double[] objects) {
		final IList<Double> list = create(contentType, objects.length);
		for (double i : objects) { list.add(i); }
		return list;
	}

	/**
	 * Creates a list from an iterable without casting. Warning: The caller must ensure the elements are compatible with
	 * the content type.
	 *
	 * @param contentType
	 *            the expected content type.
	 * @param objects
	 *            the iterable source.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> createWithoutCasting(final IType contentType, final Iterable<T> objects) {
		final IList<T> list = create(contentType);
		for (T o : objects) { list.add(o); }
		return list;
	}

	// ================================================================================================
	// SCOPE-AWARE CREATION WITH CASTING
	// Methods that cast elements according to GAML type contracts (require scope)
	// ================================================================================================

	/**
	 * Cast and add.
	 *
	 * @param scope
	 *            the scope
	 * @param list
	 *            the list
	 * @param o
	 *            the o
	 */
	private static void castAndAdd(final IScope scope, final IList list, final Object o) {
		list.addValue(scope, o);
	}

	/**
	 * Creates a list from a container, casting elements within the given scope.
	 *
	 * @param scope
	 *            the execution scope for casting.
	 * @param contentType
	 *            the target content type.
	 * @param container
	 *            the source container.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IType contentType, final IContainer container) {
		if (container == null) return create(contentType);
		if (FLAGS.CAST_CONTAINER_CONTENTS
				&& GamaType.requiresCasting(contentType, container.getGamlType().getContentType())) {
			final IList list = create(contentType);
			for (final Object o : container.iterable(scope)) { castAndAdd(scope, list, o); }
			return list;
		}
		return createWithoutCasting(contentType, container.iterable(scope));
	}

	/**
	 * Creates a list from another list, ensuring correct content type.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param list
	 *            the source list.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final IList<T> list) {
		return create(scope, contentType, (Iterable<T>) list);
	}

	/**
	 * Creates a list from an iterable, casting elements.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param iterable
	 *            the source iterable.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final Iterable<T> iterable) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return createWithoutCasting(contentType, iterable);
		final IList<T> list = create(contentType);
		for (final Object o : iterable) { castAndAdd(scope, list, o); }
		return list;
	}

	/**
	 * Creates a list from an iterator, casting elements.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param iterator
	 *            the source iterator.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final Iterator<T> iterator) {
		if (iterator == null) return create(contentType);
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			final IList<T> list = create(contentType);
			iterator.forEachRemaining(list::add);
			return list;
		}
		final IList<T> list = create(contentType);
		while (iterator.hasNext()) { castAndAdd(scope, list, iterator.next()); }
		return list;
	}

	/**
	 * Creates a list from an enumeration, casting elements.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param iterator
	 *            the source enumeration.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final Enumeration<T> iterator) {
		if (iterator == null) return create(contentType);
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			final IList<T> list = create(contentType);
			while (iterator.hasMoreElements()) { list.add(iterator.nextElement()); }
			return list;
		}
		final IList<T> list = create(contentType);
		while (iterator.hasMoreElements()) { castAndAdd(scope, list, iterator.nextElement()); }
		return list;
	}

	/**
	 * Creates a list from variable arguments, casting elements.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param objects
	 *            the elements to include in the list.
	 * @return the created {@link IList}.
	 */
	@SafeVarargs
	public static <T> IList<T> create(final IScope scope, final IType contentType, final T... objects) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return createWithoutCasting(contentType, objects);
		final IList<T> list = create(contentType, objects == null ? 0 : objects.length);
		if (objects != null) { for (final Object o : objects) { castAndAdd(scope, list, o); } }
		return list;
	}

	/**
	 * Creates a list from an array of characters.
	 *
	 * @param <T>
	 *            the type of elements (Character).
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param objects
	 *            the array of characters.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final char[] objects) {
		final IList list = create(contentType, objects == null ? 0 : objects.length);
		if (objects == null) return list;
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			for (final char o : objects) { list.add(String.valueOf(o)); }
			return list;
		}
		for (final char o : objects) { castAndAdd(scope, list, String.valueOf(o)); }
		return list;
	}

	/**
	 * Creates a list from an array of bytes.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param ints
	 *            the array of bytes.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IType contentType, final byte[] ints) {
		final IList list = create(contentType, ints == null ? 0 : ints.length);
		if (ints == null) return list;
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			for (final int o : ints) { list.add(Integer.valueOf(o)); }
			return list;
		}
		for (final int o : ints) { castAndAdd(scope, list, Integer.valueOf(o)); }
		return list;
	}

	/**
	 * Creates a list from an array of integers.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param ints
	 *            the array of integers.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IType contentType, final int[] ints) {
		final IList list = create(contentType, ints == null ? 0 : ints.length);
		if (ints == null) return list;
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			list.addAll(Arrays.asList(ints));
			return list;
		}
		for (final int o : ints) { castAndAdd(scope, list, o); }
		return list;
	}

	/**
	 * Creates a list from an array of longs.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param ints
	 *            the array of longs.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IType contentType, final long[] ints) {
		final IList list = create(contentType, ints == null ? 0 : ints.length);
		if (ints == null) return list;
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			for (final long o : ints) { list.add(Long.valueOf(o).intValue()); }
			return list;
		}
		for (final long o : ints) { castAndAdd(scope, list, Long.valueOf(o).intValue()); }
		return list;
	}

	/**
	 * Creates a list from an array of floats.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param doubles
	 *            the array of floats.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IType contentType, final float[] doubles) {
		final IList list = create(contentType, doubles == null ? 0 : doubles.length);
		if (doubles == null) return list;
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			for (final float o : doubles) { list.add((double) o); }
			return list;
		}
		for (final float o : doubles) { castAndAdd(scope, list, (double) o); }
		return list;
	}

	/**
	 * Creates a list from an array of doubles.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param contentType
	 *            the target content type.
	 * @param doubles
	 *            the array of doubles.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IType contentType, final double[] doubles) {
		final IList list = create(contentType, doubles == null ? 0 : doubles.length);
		if (doubles == null) return list;
		if (!FLAGS.CAST_CONTAINER_CONTENTS) {
			list.addAll(Arrays.asList(doubles));
			return list;
		}
		for (final double o : doubles) { castAndAdd(scope, list, o); }
		return list;
	}

	/**
	 * Creates and populates a list by evaluating an expression.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param fillExpr
	 *            the expression to evaluate for filling the list.
	 * @param size
	 *            the number of elements to generate.
	 * @param parallel
	 *            whether filling should be done in parallel.
	 * @return the created {@link IList}.
	 */
	public static IList create(final IScope scope, final IExpression fillExpr, final Integer size,
			final boolean parallel) {
		if (fillExpr == null) return create(Types.NO_TYPE, size);
		final Object[] contents = new Object[size];
		final IType contentType = fillExpr.getGamlType();
		// 10/01/14. Cannot use Arrays.fill() everywhere: see Issue 778.
		if (fillExpr.isConst()) {
			final Object o = fillExpr.value(scope);
			GamaExecutorService.executeThreaded(() -> IntStream.range(0, contents.length).parallel().forEach(i -> {
				contents[i] = o;
			}));
		} else if (parallel) {
			GamaExecutorService.executeThreaded(
					() -> IntStream.range(0, contents.length)./* see #2974. parallel(). */forEach(i -> {
						contents[i] = fillExpr.value(scope);
					}));
		} else {
			for (int i = 0; i < contents.length; i++) { contents[i] = fillExpr.value(scope); }
		}
		return create(scope, contentType, contents);
	}

	// ================================================================================================
	// CORE CREATION METHODS
	// Methods for creating empty or sized lists
	// ================================================================================================

	/**
	 * Creates an empty list of a specific size and type.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param contentType
	 *            the content type of list elements.
	 * @param size
	 *            the initial size (or capacity).
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IType contentType, final int size) {
		return new GamaList<>(size, contentType);
	}

	/**
	 * Creates an empty list with a specific type.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final IType contentType) {
		return create(contentType, 4);
	}

	/**
	 * Creates an empty list specialized for a specific class.
	 *
	 * @param <T>
	 *            the type of elements.
	 * @param clazz,
	 *            the class of elements.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create(final Class<T> clazz) {
		return create(Types.get(clazz));
	}

	/**
	 * Creates a generic empty list (no specific type).
	 *
	 * @param <T>
	 *            the type of elements.
	 * @return the created {@link IList}.
	 */
	public static <T> IList<T> create() {
		return create(Types.NO_TYPE);
	}

	// ================================================================================================
	// WRAPPING METHODS
	// Zero-copy wrappers that expose existing data as IList (changes are bidirectional)
	// ================================================================================================

	/**
	 * Wraps a Java list into an {@link IList}. Changes are reflected in both the wrapper and the original list.
	 * <p>
	 * <b>Performance Note:</b> This is a zero-copy operation. The returned list is a view over the original list, so
	 * any modifications affect both. For an independent copy, use {@link #create(IScope, IType, Iterable)} instead.
	 * </p>
	 *
	 * @param <E>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @param wrapped
	 *            the list to wrap.
	 * @return the {@link IList} wrapper.
	 * @since GAMA 1.0
	 */
	public static <E> IList<E> wrap(final IType contentType, final List<E> wrapped) {
		// return createWithoutCasting(contentType, wrapped);
		return new GamaListWrapper(wrapped, contentType);
	}

	/**
	 * Wraps an array into an {@link IList}. The returned list is backed by the array. Resizing operations will fail.
	 * <p>
	 * <b>Performance Note:</b> This is a zero-copy operation providing a read-write view of the array. Changes to the
	 * list modify the underlying array and vice-versa. The list cannot grow beyond the array size.
	 * </p>
	 *
	 * @param <E>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @param wrapped
	 *            the array to wrap.
	 * @return the {@link IList} wrapper.
	 * @since GAMA 1.0
	 */
	public static <E> IList<E> wrap(final IType contentType, final E... wrapped) {
		// return createWithoutCasting(contentType, wrapped);
		return new GamaListArrayWrapper(wrapped, contentType);
	}

	/**
	 * Wraps a collection into an {@link IList}. Changes to the collection are visible in the list and vice-versa.
	 * Indexed access is emulated for non-list collections.
	 * <p>
	 * <b>Performance Note:</b> For {@link List} arguments, delegates to {@link #wrap(IType, List)} for optimal
	 * performance. For other collections, indexed operations have O(n) complexity.
	 * </p>
	 *
	 * @param <E>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @param wrapped
	 *            the collection to wrap.
	 * @return the {@link IList} wrapper.
	 * @since GAMA 1.0
	 */
	public static <E> IList<E> wrap(final IType contentType, final Collection<E> wrapped) {
		if (wrapped instanceof List) return wrap(contentType, (List<E>) wrapped);
		return new GamaListCollectionWrapper(wrapped, contentType);
	}

	// ================================================================================================
	// UTILITY AND CONVERSION METHODS
	// Helper methods for list comparison, conversion, and GAML operators
	// ================================================================================================

	/**
	 * Checks if two lists are equal (contain the same elements in the same order).
	 *
	 * @param one
	 *            the first list.
	 * @param two
	 *            the second list.
	 * @return true if lists are equal, false otherwise.
	 */
	public static boolean equals(final IList one, final IList two) {
		final Iterator<Object> it1 = one.iterator();
		final Iterator<Object> it2 = two.iterator();
		while (it1.hasNext() && it2.hasNext()) { if (!Objects.equals(it1.next(), it2.next())) return false; }
		return !it1.hasNext() && !it2.hasNext();
	}

	/**
	 * Creates a list of containers matching the given container type and size.
	 *
	 * @param ct
	 *            the container type (e.g. list<int>).
	 * @param size
	 *            the number of elements.
	 * @return the created list.
	 */
	public static List create(final IContainerType ct, final int size) {
		return create((IType) ct.getGamlType(), size);
	}

	/**
	 * Converts an object to a list, handling various types (arrays, collections, single objects).
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert.
	 * @param ct
	 *            the expected content type.
	 * @param copy
	 *            whether to create a copy if the object is already a list.
	 * @return the resulting {@link IList}.
	 * @throws GamaRuntimeException
	 *             if conversion fails.
	 */
	public static IList castToList(final IScope scope, final Object obj, final IType ct, final boolean copy)
			throws GamaRuntimeException {
		final IType contentsType = ct == null ? Types.NO_TYPE : ct;
		return switch (obj) {
			case null -> create(contentsType, 0);
			case IDate gd -> gd.listValue(scope, contentsType);
			// Explicitly set copy to true if we deal with a population
			case IPopulation ip -> ip.listValue(scope, contentsType, true);
			case IContainer ic -> ic.listValue(scope, contentsType, copy);
			case Collection coll -> create(scope, contentsType, coll);
			case IColor c -> create(scope, contentsType, new int[] { c.red(), c.green(), c.blue(), c.alpha() });
			case IPoint point -> create(scope, contentsType, new double[] { point.getX(), point.getY(), point.getZ() });
			case String s -> create(scope, contentsType, s.toCharArray());
			default -> create(scope, contentsType, obj);
		};

	}

	/**
	 * GAML operator to cast an object to a list.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to cast.
	 * @return the resulting {@link IList}.
	 */
	@operator (
			value = "to_list",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			can_be_const = true,
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "casts the operand to a list, making an explicit copy if it is already a list or a subtype of list (interval, population, etc.)",
			see = { "list" })
	@no_test
	public static IList copyToList(final IScope scope, final Object obj) {
		return castToList(scope, obj, null, true);
	}

	/**
	 * Converts an object to a list without copying if possible.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert.
	 * @return the resulting {@link IList}.
	 */
	public static IList castToList(final IScope scope, final Object obj) {
		return castToList(scope, obj, null, false);
	}

	/**
	 * Gets the supplier.
	 *
	 * @param contentType
	 *            the content type
	 * @return the supplier
	 */
	public static Supplier<IList> getSupplier(final IType contentType) {
		return () -> create(contentType, 0);
	}

	/**
	 * Gets the empty list.
	 *
	 * @return the empty list
	 */
	public static IList getEmptyList() { return EMPTY_LIST; }

}