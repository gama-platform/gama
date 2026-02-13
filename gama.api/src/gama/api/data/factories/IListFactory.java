/*******************************************************************************************************
 *
 * IListFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IListFactory extends IFactory<IList> {

	/**
	 * Returns a collector that accumulates elements into a GamaList.
	 *
	 * @param <T>
	 *            the generic type of elements
	 * @return the collector
	 */
	<T> Collector<T, IList<T>, IList<T>> toGamaList();

	/**
	 * Creates a GamaList from a stream of elements.
	 *
	 * @param <T>
	 *            the generic type
	 * @param t
	 *            the type of contents
	 * @param stream
	 *            the source stream
	 * @return the created list
	 */
	<T> IList<T> create(IType t, Stream<T> stream);

	/**
	 * Creates a GamaList from an array of objects without type casting checks.
	 *
	 * @param contentType
	 *            the type of content
	 * @param objects
	 *            the array of objects
	 * @warning This operation can end up putting values of the wrong type into the list if not used carefully.
	 * @return the created list
	 */

	@SuppressWarnings ("unchecked")
	<T> IList<T> createWithoutCasting(IType contentType, T... objects);

	/**
	 * Creates a GamaList from an array of ints without type casting checks.
	 *
	 * @param contentType
	 *            the type of content (usually Types.INT)
	 * @param objects
	 *            the array of ints
	 * @warning This operation can end up putting values of the wrong type into the list.
	 * @return the created list
	 */
	IList<Integer> createWithoutCasting(IType contentType, int[] objects);

	/**
	 * Creates a GamaList from an array of doubles without type casting checks.
	 *
	 * @param contentType
	 *            the type of content (usually Types.FLOAT)
	 * @param objects
	 *            the array of doubles
	 * @warning This operation can end up putting values of the wrong type into the list.
	 * @return the created list
	 */
	IList<Double> createWithoutCasting(IType contentType, double[] objects);

	/**
	 * Creates a GamaList from an iterable without type casting checks.
	 *
	 * @param contentType
	 *            the type of content
	 * @param objects
	 *            the iterable
	 * @warning This operation can end up putting values of the wrong type into the list.
	 * @return the created list
	 */
	<T> IList<T> createWithoutCasting(IType contentType, Iterable<T> objects);

	/**
	 * Creates a GamaList from a generic container.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param container
	 *            the source container
	 * @return the created list
	 */
	IList create(IScope scope, IType contentType, IContainer container);

	/**
	 * Creates a copy of a GamaList.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param list
	 *            the source list
	 * @return the new list
	 */
	<T> IList<T> create(IScope scope, IType contentType, IList<T> list);

	/**
	 * Creates a GamaList from an Iterable.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param iterable
	 *            the source iterable
	 * @return the created list
	 */
	<T> IList<T> create(IScope scope, IType contentType, Iterable<T> iterable);

	/**
	 * Creates a GamaList from an Iterator.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param iterator
	 *            the source iterator
	 * @return the created list
	 */
	<T> IList<T> create(IScope scope, IType contentType, Iterator<T> iterator);

	/**
	 * Creates a GamaList from an Enumeration.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param iterator
	 *            the source enumeration
	 * @return the created list
	 */
	<T> IList<T> create(IScope scope, IType contentType, Enumeration<T> iterator);

	/**
	 * Creates a GamaList from an array of arbitrary objects.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param objects
	 *            the array of objects
	 * @return the created list
	 */
	@SuppressWarnings ("unchecked")
	<T> IList<T> create(IScope scope, IType contentType, T... objects);

	/**
	 * Creates a list of characters from a char array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param <T>
	 *            the generic type (Character)
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the expected content type
	 * @param objects
	 *            the array of chars
	 * @return the created list
	 * @date 30 mai 2023
	 */
	<T> IList<T> create(IScope scope, IType contentType, char[] objects);

	/**
	 * Creates a list from a byte array.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param ints
	 *            the byte array
	 * @return the created list
	 */
	IList create(IScope scope, IType contentType, byte[] ints);

	/**
	 * Creates a list from an int array.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param ints
	 *            the int array
	 * @return the created list
	 */
	IList create(IScope scope, IType contentType, int[] ints);

	/**
	 * Creates a list from a long array.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param ints
	 *            the long array
	 * @return the created list
	 */
	IList create(IScope scope, IType contentType, long[] ints);

	/**
	 * Creates a list from a float array.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param doubles
	 *            the float array
	 * @return the created list
	 */
	IList create(IScope scope, IType contentType, float[] doubles);

	/**
	 * Creates a list from a double array.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param doubles
	 *            the double array
	 * @return the created list
	 */
	IList create(IScope scope, IType contentType, double[] doubles);

	/**
	 * Creates a list filled using an expression, optionally in parallel.
	 *
	 * @param scope
	 *            the scope
	 * @param fillExpr
	 *            the expression used to fill the list
	 * @param size
	 *            the size of the list to create
	 * @param parallel
	 *            whether to use parallel evaluation
	 * @return the created list
	 */
	IList create(IScope scope, IExpression fillExpr, Integer size, boolean parallel);

	/**
	 * Creates an empty list of a given size.
	 *
	 * @param <T>
	 *            the generic type
	 * @param contentType
	 *            the content type
	 * @param size
	 *            the initial capacity or size
	 * @return the created list
	 */
	<T> IList<T> create(IType contentType, int size);

	/**
	 * Creates an empty list of a given type.
	 *
	 * @param <T>
	 *            the generic type
	 * @param contentType
	 *            the content type
	 * @return the created list
	 */
	<T> IList<T> create(IType contentType);

	/**
	 * Creates an empty list based on a Java class for type inference.
	 *
	 * @param clazz
	 *            the class representing the content type
	 * @return a new empty IList
	 */
	<T> IList<T> create(Class<T> clazz);

	/**
	 * Creates a generic empty list (no specific content type).
	 *
	 * @return a new empty IList
	 */
	<T> IList<T> create();

	/**
	 * Wraps a Java List into an IList. Changes are reflected in both directions.
	 *
	 * @param contentType
	 *            the content type
	 * @param wrapped
	 *            the list to wrap
	 * @return a view of the list as an IList
	 */
	<E> IList<E> wrap(IType contentType, List<E> wrapped);

	/**
	 * Wraps an array into an IList. Changes to elements are reflected, but structural changes (add/remove) are not
	 * supported.
	 *
	 * @param contentType
	 *            the content type
	 * @param wrapped
	 *            the array to wrap
	 * @return a view of the array as an IList
	 */
	@SuppressWarnings ("unchecked")
	<E> IList<E> wrap(IType contentType, E... wrapped);

	/**
	 * Wraps a Collection into an IList. Since Collections are not indexed, some list operations are emulated.
	 *
	 * @param contentType
	 *            the content type
	 * @param wrapped
	 *            the collection to wrap
	 * @return a view of the collection as an IList
	 */
	<E> IList<E> wrap(IType contentType, Collection<E> wrapped);

	/**
	 * Creates a list of the specified size, intended to hold elements of a container type.
	 *
	 * @param ct
	 *            the type of the container elements
	 * @param size
	 *            the size of the list to create
	 * @return the created list
	 */
	List create(IContainerType ct, int size);

	/**
	 * Returns a shared immutable empty list.
	 *
	 * @return the empty list
	 */
	IList getEmptyList();

}