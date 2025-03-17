/*******************************************************************************************************
 *
 * GamaListFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

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

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaType;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Class GamaListFactory. The factory for creating lists from various other objects. All the methods that accept the
 * scope as a parameter will observe the contract of GAML containers, which is that the objects contained in the list
 * will be casted to the content type of the list. To avoid unecessary castings, some methods (without the scope
 * parameter) will simply copy the objects.
 *
 * @author drogoul
 * @since 30 janv. 2015
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaListFactory {

	/** The Constant DEFAULT_SIZE. */
	private static final int DEFAULT_SIZE = 4;

	/** The Constant EMPTY_LIST. */
	public static final IList EMPTY_LIST = wrap(Types.NO_TYPE, Collections.EMPTY_LIST);

	/** The ch. */
	static final Set<Collector.Characteristics> CH =
			ImmutableSet.<Collector.Characteristics> of(Collector.Characteristics.IDENTITY_FINISH);

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
	 * The Class GamaListSupplier.
	 */
	public static class GamaListSupplier implements Supplier<IList> {

		/** The t. */
		final IType t;

		/**
		 * Instantiates a new gama list supplier.
		 *
		 * @param t
		 *            the t
		 */
		public GamaListSupplier(final IType t) {
			this.t = t;
		}

		@Override
		public IList get() {
			return create(t);
		}

	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param t
	 *            the t
	 * @param stream
	 *            the stream
	 * @return the i list
	 */
	public static <T> IList<T> create(final IType t, final Stream<T> stream) {
		return (IList<T>) stream.collect(TO_GAMA_LIST);
	}

	/**
	 * Create a GamaList from an array of objects, but does not attempt casting its values.
	 *
	 * @param contentType
	 * @param collection
	 * @warning ***WARNING*** This operation can end up putting values of the wrong type into the list
	 * @return
	 */
	public static <T> IList<T> createWithoutCasting(final IType contentType, final T... objects) {
		final IList<T> list = create(contentType, objects.length);
		list.addAll(Arrays.asList(objects));
		return list;
	}

	/**
	 * Create a GamaList from an array of ints, but does not attempt casting its values.
	 *
	 * @param contentType
	 * @param collection
	 * @warning ***WARNING*** This operation can end up putting values of the wrong type into the list
	 * @return
	 */
	public static IList<Integer> createWithoutCasting(final IType contentType, final int[] objects) {
		final IList<Integer> list = create(contentType, objects.length);
		list.addAll(Arrays.asList(ArrayUtils.toObject(objects)));
		return list;
	}

	/**
	 * Create a GamaList from an array of floats, but does not attempt casting its values.
	 *
	 * @param contentType
	 * @param collection
	 * @warning ***WARNING*** This operation can end up putting values of the wrong type into the list
	 * @return
	 */
	public static IList<Double> createWithoutCasting(final IType contentType, final double[] objects) {
		final IList<Double> list = create(contentType, objects.length);
		list.addAll(Arrays.asList(ArrayUtils.toObject(objects)));
		return list;
	}

	/**
	 * Create a GamaList from an iterable, but does not attempt casting its values.
	 *
	 * @param contentType
	 * @param collection
	 * @warning ***WARNING*** This operation can end up putting values of the wrong type into the list
	 * @return
	 */

	public static <T> IList<T> createWithoutCasting(final IType contentType, final Iterable<T> objects) {
		final IList<T> list = create(contentType);
		Iterables.addAll(list, objects);
		return list;
	}

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
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param container
	 *            the container
	 * @return the i list
	 */
	public static IList create(final IScope scope, final IType contentType, final IContainer container) {
		if (container == null) return create(contentType);
		if (GamaType.requiresCasting(contentType, container.getGamlType().getContentType()))
			return create(scope, contentType, container.iterable(scope));
		return createWithoutCasting(contentType, container.iterable(scope));
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param container
	 *            the container
	 * @return the i list
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final IList<T> container) {
		if (container == null) return create(contentType);
		if (GamaType.requiresCasting(contentType, container.getGamlType().getContentType()))
			return create(scope, contentType, (Collection) container);
		return createWithoutCasting(contentType, container);
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param iterable
	 *            the iterable
	 * @return the i list
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final Iterable<T> iterable) {
		final IList<T> list = create(contentType);
		for (final Object o : iterable) { castAndAdd(scope, list, o); }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param iterator
	 *            the iterator
	 * @return the i list
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final Iterator<T> iterator) {
		final IList<T> list = create(contentType);
		if (iterator != null) { while (iterator.hasNext()) { castAndAdd(scope, list, iterator.next()); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param iterator
	 *            the iterator
	 * @return the i list
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final Enumeration<T> iterator) {
		final IList<T> list = create(contentType);
		if (iterator != null) {
			while (iterator.hasMoreElements()) { castAndAdd(scope, list, iterator.nextElement()); }
		}
		return list;
	}

	/**
	 * Creates a list with arbitraty objects inside
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param objects
	 *            the objects
	 * @return the i list
	 */
	@SafeVarargs
	public static <T> IList<T> create(final IScope scope, final IType contentType, final T... objects) {
		final IList<T> list = create(contentType, objects == null ? 0 : objects.length);
		if (objects != null) { for (final Object o : objects) { castAndAdd(scope, list, o); } }
		return list;
	}

	/**
	 * Creates a list with characters inside
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param objects
	 *            the objects
	 * @return the i list
	 * @date 30 mai 2023
	 */
	public static <T> IList<T> create(final IScope scope, final IType contentType, final char[] objects) {
		final IList<T> list = create(contentType, objects == null ? 0 : objects.length);
		if (objects != null) { for (final Object o : objects) { castAndAdd(scope, list, o); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param ints
	 *            the ints
	 * @return the i list
	 */
	public static IList create(final IScope scope, final IType contentType, final byte[] ints) {
		final IList list = create(contentType, ints == null ? 0 : ints.length);
		if (ints != null) { for (final int o : ints) { castAndAdd(scope, list, Integer.valueOf(o)); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param ints
	 *            the ints
	 * @return the i list
	 */
	public static IList create(final IScope scope, final IType contentType, final int[] ints) {
		final IList list = create(contentType, ints == null ? 0 : ints.length);
		if (ints != null) { for (final int o : ints) { castAndAdd(scope, list, Integer.valueOf(o)); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param ints
	 *            the ints
	 * @return the i list
	 */
	public static IList create(final IScope scope, final IType contentType, final long[] ints) {
		final IList list = create(contentType, ints == null ? 0 : ints.length);
		if (ints != null) { for (final long o : ints) { castAndAdd(scope, list, Long.valueOf(o).intValue()); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param doubles
	 *            the doubles
	 * @return the i list
	 */
	public static IList create(final IScope scope, final IType contentType, final float[] doubles) {
		final IList list = create(contentType, doubles == null ? 0 : doubles.length);
		if (doubles != null) { for (final float o : doubles) { castAndAdd(scope, list, Double.valueOf(o)); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param fillExpr
	 *            the fill expr
	 * @param size
	 *            the size
	 * @return the i list
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

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param doubles
	 *            the doubles
	 * @return the i list
	 */
	public static IList create(final IScope scope, final IType contentType, final double[] doubles) {
		final IList list = create(contentType, doubles == null ? 0 : doubles.length);
		if (doubles != null) { for (final double o : doubles) { castAndAdd(scope, list, Double.valueOf(o)); } }
		return list;
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param contentType
	 *            the content type
	 * @param size
	 *            the size
	 * @return the i list
	 */
	public static <T> IList<T> create(final IType contentType, final int size) {
		return new GamaList<>(size, contentType);
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param contentType
	 *            the content type
	 * @return the i list
	 */
	public static <T> IList<T> create(final IType contentType) {
		return create(contentType, DEFAULT_SIZE);
	}

	/**
	 * Create a IList with no type and no elements
	 *
	 * @param clazz,
	 *            the class from which the contents type
	 * @return a new IList
	 */
	public static <T> IList<T> create(final Class<T> clazz) {
		return create(Types.get(clazz));
	}

	/**
	 * Create a IList with no type and no elements
	 *
	 * @return a new IList
	 */
	public static <T> IList<T> create() {
		return create(Types.NO_TYPE);
	}

	/**
	 * Wraps the parameter into an IList. Every change in the wrapped list is reflected immediately and every change to
	 * the IList is reflected in the wrapped list. No copy is made, , only a thin layer is created to wrap the parameter
	 *
	 * @param contentType
	 * @param wrapped
	 * @return
	 */
	public static <E> IList<E> wrap(final IType contentType, final List<E> wrapped) {
		// return createWithoutCasting(contentType, wrapped);
		return new GamaListWrapper(wrapped, contentType);
	}

	/**
	 * Wraps the array into an IList. Every change in the wrapped array is reflected immediately and every change to the
	 * IList is reflected in the array, exluding add and remove (as well as addAll and removeAll) operations, which
	 * yield a runtime exception. No copy of the array is made, only a thin layer is created to wrap the array
	 *
	 * @param contentType
	 * @param wrapped
	 * @return
	 */
	public static <E> IList<E> wrap(final IType contentType, final E... wrapped) {
		// return createWithoutCasting(contentType, wrapped);
		return new GamaListArrayWrapper(wrapped, contentType);
	}

	/**
	 * Wraps the parameter Collection into an IList. Every change in the wrapped Collection is reflected immediately and
	 * every change to the IList is reflected in the wrapped Collection. No copy is made, only a thin layer is created
	 * to wrap the parameter. Some operations (esp. those using indices) are not really meaningful for collections; they
	 * are emulated in the best possible way by this wrapper
	 *
	 * @param contentType
	 *            s
	 * @param wrapped
	 * @return
	 */
	public static <E> IList<E> wrap(final IType contentType, final Collection<E> wrapped) {
		if (wrapped instanceof List) return wrap(contentType, (List<E>) wrapped);
		return new GamaListCollectionWrapper(wrapped, contentType);
	}

	/**
	 * Equals.
	 *
	 * @param one
	 *            the one
	 * @param two
	 *            the two
	 * @return true, if successful
	 */
	public static boolean equals(final IList one, final IList two) {
		final Iterator<Object> it1 = one.iterator();
		final Iterator<Object> it2 = two.iterator();
		while (it1.hasNext() && it2.hasNext()) { if (!Objects.equals(it1.next(), it2.next())) return false; }
		return !it1.hasNext() && !it2.hasNext();
	}

	/**
	 * Creates a list with the same content as the parameter (a container type), for instance a list of maps, etc.
	 * Simply calls create(IType, int)
	 *
	 * @param ct
	 *            the type of the container
	 * @param size
	 *            the size of the list to create
	 * @return
	 */
	public static List create(final IContainerType ct, final int size) {
		return create((IType) ct.getGamlType(), size);
	}

}
