/*******************************************************************************************************
 *
 * IList.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.factories.GamaPairFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaIntegerType;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.dev.FLAGS;

/**
 * The class IList. Interface for classes representing various lists in GAML (list, population, etc.)
 *
 * @author drogoul
 * @since 14 d�c. 2011
 *
 */
@SuppressWarnings ("unchecked")
public interface IList<E>
		extends IContainer.Modifiable<Integer, E, Integer, E>, IContainer.Addressable<Integer, E, Integer, E>, List<E> {

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	default boolean containsKey(final IScope scope, final Object o) {
		if (o instanceof final Integer i) return i >= 0 && i < this.size();
		return false;
	}

	/**
	 * List value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i list
	 */
	@Override
	default IList<E> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		if (!GamaType.requiresCasting(contentsType, getGamlType().getContentType())) {
			if (copy) return GamaListFactory.createWithoutCasting(contentsType, this);
			return this;
		}
		return GamaListFactory.create(scope, contentsType, this);
	}

	/**
	 * Matrix value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentType
	 *            the content type
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 */
	@Override
	default IMatrix<E> matrixValue(final IScope scope, final IType contentType, final boolean copy) {
		return GamaMatrixFactory.createFrom(scope, this, contentType, null);
	}

	/**
	 * Matrix value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param preferredSize
	 *            the preferred size
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 */
	@Override
	default IMatrix<E> matrixValue(final IScope scope, final IType contentsType, final IPoint preferredSize,
			final boolean copy) {
		return GamaMatrixFactory.createFrom(scope, this, contentsType, preferredSize);
	}

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default String stringValue(final IScope scope) throws GamaRuntimeException {
		return serializeToGaml(false);
	}

	/**
	 * Serialize.
	 *
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	default String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder(size() * 10);
		sb.append('[');
		for (int i = 0; i < size(); i++) {
			if (i != 0) { sb.append(','); }
			sb.append(StringUtils.toGaml(get(i), includingBuiltIn));
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Map value.
	 *
	 * @param scope
	 *            the scope
	 * @param keyType
	 *            the key type
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i map
	 */
	@Override
	default IMap<?, ?> mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy) {
		// 08/01/14: Change of behavior. A list now returns a map containing its
		// contents casted to pairs.
		// Allows to build sets with the idiom: list <- map(list).values;
		final IType myCt = getGamlType().getContentType();
		final IType kt, ct;
		if (myCt.isParametricFormOf(Types.PAIR) || myCt.equals(Types.PAIR)) {
			// Issue #2607: specific treatment of lists of pairs
			kt = GamaType.findSpecificType(keyType, myCt.getKeyType());
			ct = GamaType.findSpecificType(contentsType, myCt.getContentType());
		} else {
			kt = GamaType.findSpecificType(keyType, myCt); // not keyType()
			ct = GamaType.findSpecificType(contentsType, myCt);

		}
		final IMap result = GamaMapFactory.create(kt, ct);
		for (final E e : this) { result.addValue(scope, GamaPairFactory.toPair(scope, e, kt, ct, copy)); }
		return result;
	}

	/**
	 * Adds the value.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 */
	@Override
	default void addValue(final IScope scope, final E object) {
		add(buildValue(scope, object));
	}

	/**
	 * Adds the value at index.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @param object
	 *            the object
	 */
	@Override
	default void addValueAtIndex(final IScope scope, final Object index, final E object) {
		add(buildIndex(scope, index), buildValue(scope, object));
	}

	/**
	 * Sets the value at index.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	@Override
	default void setValueAtIndex(final IScope scope, final Object index, final E value) {
		set(buildIndex(scope, index), buildValue(scope, value));
	}

	/**
	 * Replace range.
	 *
	 * @param scope
	 *            the scope
	 * @param range
	 *            the range
	 * @param value
	 *            the value
	 */
	// See Issue #3099
	default void replaceRange(final IScope scope, final IPair range, final E value) {
		this.subList(Cast.asInt(scope, range.getKey()), Cast.asInt(scope, range.getValue()))
				.replaceAll(v -> buildValue(scope, value));
	}

	/**
	 * Adds the values.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @param values
	 *            the values
	 */
	// AD July 2020: Addition of the index (see #2985)
	@Override
	default void addValues(final IScope scope, final Object index, final IContainer values) {
		if (index == null) {
			addAll(buildValues(scope, values));
		} else {
			final int i = buildIndex(scope, index);
			addAll(i, buildValues(scope, values));
		}
	}

	/**
	 * Sets the all values.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	@Override
	default void setAllValues(final IScope scope, final E value) {
		final E element = buildValue(scope, value);
		for (int i = 0, n = size(); i < n; i++) { set(i, element); }
	}

	/**
	 * Removes the value.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	@Override
	default void removeValue(final IScope scope, final Object value) {
		remove(value);
	}

	/**
	 * Removes the index.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 */
	@Override
	default void removeIndex(final IScope scope, final Object index) {
		// Fixes issue #3294 -- additionnaly make sure that "use unboxing" is unchecked in Save Actions
		int intIndex = Cast.asInt(scope, index).intValue();
		remove(intIndex);
	}

	/**
	 * Removes the values.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 */
	@Override
	default void removeValues(final IScope scope, final IContainer<?, ?> values) {
		if (values instanceof Collection) {
			removeAll((Collection) values);
		} else {
			removeAll(values.listValue(scope, Types.NO_TYPE, false));
		}
	}

	/**
	 * Removes the all occurrences of value.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 */
	@Override
	default void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		removeIf(each -> Objects.equals(each, value));
	}

	/**
	 * First value.
	 *
	 * @param scope
	 *            the scope
	 * @return the e
	 */
	@Override
	default E firstValue(final IScope scope) {
		if (size() == 0) return null;
		return get(0);
	}

	/**
	 * Last value.
	 *
	 * @param scope
	 *            the scope
	 * @return the e
	 */
	@Override
	default E lastValue(final IScope scope) {
		if (size() == 0) return null;
		return get(size() - 1);
	}

	/**
	 * Gets the.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @return the e
	 */
	@Override
	default E get(final IScope scope, final Integer index) {
		return get(index);
	}

	/**
	 * Length.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	default int length(final IScope scope) {
		return size();
	}

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i container
	 */
	@Override
	default IContainer<Integer, E> reverse(final IScope scope) {
		final IList list = copy(scope);
		Collections.reverse(list);
		return list;
	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	@Override
	default IList<E> copy(final IScope scope) {
		return GamaListFactory.createWithoutCasting(getGamlType().getContentType(), this);
	}

	/**
	 * Any value.
	 *
	 * @param scope
	 *            the scope
	 * @return the e
	 */
	@Override
	default E anyValue(final IScope scope) {
		if (isEmpty()) return null;
		final int i = scope.getRandom().between(0, size() - 1);
		return get(i);
	}

	/**
	 * Contains.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		return contains(o);
	}

	/**
	 * Checks if is empty.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is empty
	 */
	@Override
	default boolean isEmpty(final IScope scope) {
		return isEmpty();
	}

	/**
	 * Iterable.
	 *
	 * @param scope
	 *            the scope
	 * @return the iterable<? extends e>
	 */
	@Override
	default Iterable<? extends E> iterable(final IScope scope) {
		return this;
	}

	/**
	 * Gets the from indices list.
	 *
	 * @param scope
	 *            the scope
	 * @param indices
	 *            the indices
	 * @return the from indices list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default E getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, Cast.asInt(scope, indices.get(0)));
		// We do not consider the case where multiple indices are used. Maybe
		// could be used in the
		// future to return a list of values ?
	}

	/**
	 * Method removeIndexes()
	 *
	 * @see gama.api.data.objects.IContainer.ToSet#removeIndexes(gama.api.runtime.scope.IScope,
	 *      gama.api.data.objects.IContainer)
	 */
	@Override
	default void removeIndexes(final IScope scope, final IContainer<?, ?> index) {
		final IList<Integer> l = (IList<Integer>) index.listValue(scope, Types.INT, false);
		Collections.sort(l, Collections.reverseOrder());
		for (final Integer i : l) { removeIndex(scope, i); }
	}

	/**
	 * Method buildValue()
	 *
	 * @see gama.api.data.objects.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	default E buildValue(final IScope scope, final Object object) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return (E) object;
		return (E) getGamlType().getContentType().cast(scope, object, null, false);
	}

	/**
	 * Method buildValues()
	 *
	 * @see gama.api.data.objects.IContainer.ToSet#buildValues(gama.api.runtime.scope.IScope,
	 *      gama.api.data.objects.IContainer, gama.gaml.types.IContainerType)
	 */
	default IList<E> buildValues(final IScope scope, final IContainer objects) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return (IList<E>) objects;
		return (IList<E>) getGamlType().cast(scope, objects, null, false);
	}

	/**
	 * Method buildIndex()
	 *
	 * @see gama.api.data.objects.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	default Integer buildIndex(final IScope scope, final Object object) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return (Integer) object;
		return GamaIntegerType.staticCast(scope, object, null, false);
	}

	/**
	 * Builds the indexes.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @return the i container
	 */
	default IContainer<?, Integer> buildIndexes(final IScope scope, final IContainer value) {
		final IList<Integer> result = GamaListFactory.create(Types.INT);
		for (final Object o : value.iterable(scope)) { result.add(buildIndex(scope, o)); }
		return result;
	}

	/**
	 * To json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 28 oct. 2023
	 */
	@Override
	default IJsonValue serializeToJson(final IJson json) {
		return json.array(this);
	}

	/**
	 * Compute runtime type.
	 *
	 * @param scope
	 *            the scope
	 * @return the i type
	 */
	@Override
	default IType<?> computeRuntimeType(final IScope scope) {
		return Types.LIST.of(
				GamaType.findCommonType(stream(scope).map(e -> GamaType.actualTypeOf(scope, e)).toArray(IType.class)));
	}

	/**
	 * Gets the gaml type. Specialized in implementing classes with the contents type
	 *
	 * @return the gaml type
	 */
	@Override
	default IContainerType<?> getGamlType() { return Types.LIST; }

}
