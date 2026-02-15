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
package gama.api.utils.list;

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
import gama.api.data.factories.IFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IDate;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.dev.FLAGS;

/**
 * A static factory for creating and managing {@link IList} instances. This class serves as a frontend for list
 * creation, delegating to an {@link IListFactory}. It supports creation from various sources (arrays, streams,
 * collections) and handles GAML-specific type casting and wrapping logic.
 *
 * All methods accepting an {@link IScope} strictly adhere to GAML container contracts, ensuring elements are cast to
 * the list's content type. Methods without a scope generally wrap existing data to avoid unnecessary casting overhead.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaListFactory implements IFactory<IList> {

	/** The ch. */
	static final Set<Collector.Characteristics> CH =
			ImmutableSet.<Collector.Characteristics> of(Collector.Characteristics.IDENTITY_FINISH);

	/** The empty list. */
	public static IList<Object> EMPTY_LIST = wrap(Types.NO_TYPE, Collections.emptyList());

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
		final IList<T> list = create(contentType);
		if (iterator != null) {
			while (iterator.hasNext()) {
				T object = iterator.next();
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		final IList<T> list = create(contentType);
		if (iterator != null) {
			while (iterator.hasMoreElements()) {
				T object = iterator.nextElement();
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		if (objects != null) {
			for (final char o : objects) {
				String object = String.valueOf(o);
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		if (ints != null) {
			for (final int o : ints) {
				Integer object = o;
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		if (ints != null) {
			for (final int o : ints) {
				Integer object = o;
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		if (ints != null) {
			for (final long o : ints) {
				Integer object = Long.valueOf(o).intValue();
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		if (doubles != null) {
			for (final float o : doubles) {
				Double object = (double) o;
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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
		if (doubles != null) {
			for (final double o : doubles) {
				Double object = o;
				if (FLAGS.CAST_CONTAINER_CONTENTS) {
					castAndAdd(scope, list, object);
				} else {
					list.add(object);
				}
			}
		}
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

	/**
	 * Wraps a Java list into an {@link IList}. Changes are reflected in both the wrapper and the original list.
	 *
	 * @param <E>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @param wrapped
	 *            the list to wrap.
	 * @return the {@link IList} wrapper.
	 */
	public static <E> IList<E> wrap(final IType contentType, final List<E> wrapped) {
		// return createWithoutCasting(contentType, wrapped);
		return new GamaListWrapper(wrapped, contentType);
	}

	/**
	 * Wraps an array into an {@link IList}. The returned list is backed by the array. Resizing operations will fail.
	 *
	 * @param <E>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @param wrapped
	 *            the array to wrap.
	 * @return the {@link IList} wrapper.
	 */
	public static <E> IList<E> wrap(final IType contentType, final E... wrapped) {
		// return createWithoutCasting(contentType, wrapped);
		return new GamaListArrayWrapper(wrapped, contentType);
	}

	/**
	 * Wraps a collection into an {@link IList}. Changes to the collection are visible in the list and vice-versa.
	 * Indexed access is emulated for non-list collections.
	 *
	 * @param <E>
	 *            the type of elements.
	 * @param contentType
	 *            the content type.
	 * @param wrapped
	 *            the collection to wrap.
	 * @return the {@link IList} wrapper.
	 */
	public static <E> IList<E> wrap(final IType contentType, final Collection<E> wrapped) {
		if (wrapped instanceof List) return wrap(contentType, (List<E>) wrapped);
		return new GamaListCollectionWrapper(wrapped, contentType);
	}

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
	public static IList toList(final IScope scope, final Object obj, final IType ct, final boolean copy)
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
		return toList(scope, obj, null, true);
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
	public static IList toList(final IScope scope, final Object obj) {
		return toList(scope, obj, null, false);
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