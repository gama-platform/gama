/*******************************************************************************************************
 *
 * IMap.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import static gama.core.util.GamaMapFactory.createWithoutCasting;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonObject;
import gama.core.util.file.json.JsonValue;
import gama.core.util.matrix.GamaObjectMatrix;
import gama.core.util.matrix.IMatrix;
import gama.dev.FLAGS;
import gama.gaml.interfaces.IJsonable;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Interface IMap.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
@vars ({ @variable (
		name = GamaMap.KEYS,
		type = IType.LIST,
		of = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
		doc = { @doc ("Returns the list of keys of this map (in their order of insertion)") }),
		@variable (
				name = GamaMap.VALUES,
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of values of this map (in their order of insertion)") }),
		@variable (
				name = GamaMap.PAIRS,
				type = IType.LIST,
				of = ITypeProvider.KEY_AND_CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of pairs (key, value) that compose this map") }) })
@SuppressWarnings ("unchecked")

public interface IMap<K, V>
		extends Map<K, V>, IModifiableContainer<K, V, K, V>, IAddressableContainer<K, V, K, V>, IJsonable {

	/**
	 * The Interface IPairList.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	interface IPairList<K, V> extends Set<Map.Entry<K, V>>, IList<Map.Entry<K, V>> {

		/**
		 * Spliterator.
		 *
		 * @return the spliterator
		 */
		@Override
		default Spliterator<Entry<K, V>> spliterator() {
			return IList.super.spliterator();
		}

	}

	/** The keys. */
	String KEYS = "keys";

	/** The values. */
	String VALUES = "values";

	/** The pairs. */
	String PAIRS = "pairs";

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@Override
	default String stringValue(final IScope scope) {
		return serializeToGaml(false);
	}

	/**
	 * Method add()
	 *
	 * @see gama.core.util.IContainer#add(gama.core.runtime.IScope, java.lang.Object)
	 */
	@Override
	default void addValue(final IScope scope, final V v) {
		if (v instanceof GamaPair) {
			setValueAtIndex(scope, (K) ((GamaPair) v).key, (V) ((GamaPair) v).value);
		} else {
			setValueAtIndex(scope, v, v);
		}
	}

	/**
	 * Method add()
	 *
	 * @see gama.core.util.IContainer#add(gama.core.runtime.IScope, java.lang.Object, java.lang.Object)
	 */
	@Override
	default void addValueAtIndex(final IScope scope, final Object index, final V value) {
		// Cf. discussion on mailing-list about making "add" a synonym of "put"
		// for maps
		// if ( !containsKey(index) ) {
		setValueAtIndex(scope, index, value);
		// }
	}

	/**
	 * Method addAll()
	 *
	 * @see gama.core.util.IContainer#addAll(gama.core.runtime.IScope, gama.core.util.IContainer)
	 */
	// AD July 2020: Addition of the index (see #2985)
	@Override
	default void addValues(final IScope scope, final Object index, final IContainer/* <?, GamaPair<K, V>> */ values) {
		// If an index is specified, we add only the last object
		if (index != null) {
			final Iterable list = values.iterable(scope);
			setValueAtIndex(scope, index, (V) Iterables.getLast(list));
		} else {
			for (final Object o : values.iterable(scope)) { addValue(scope, (V) o); }
		}
	}

	/**
	 * Method removeAt()
	 *
	 * @see gama.core.util.IContainer#removeAt(gama.core.runtime.IScope, java.lang.Object)
	 */
	@Override
	default void removeIndex(final IScope scope, final Object index) {
		remove(index);
	}

	/**
	 * Contains key.
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
	default boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
		return containsKey(o);
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
	 * Checks if is empty.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is empty
	 */
	@Override
	default boolean isEmpty(final IScope scope) {
		return length(scope) == 0;
	}

	/**
	 * Any value.
	 *
	 * @param scope
	 *            the scope
	 * @return the v
	 */
	@Override
	default V anyValue(final IScope scope) {
		final int size = length(scope);
		if (size == 0) return null;
		final K key = scope.getRandom().oneOf(keySet());
		return get(key);
	}

	/**
	 * First value.
	 *
	 * @param scope
	 *            the scope
	 * @return the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default V firstValue(final IScope scope) throws GamaRuntimeException {
		if (length(scope) == 0) return null;
		return Iterators.get(values().iterator(), 0);
	}

	/**
	 * Last value.
	 *
	 * @param scope
	 *            the scope
	 * @return the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default V lastValue(final IScope scope) throws GamaRuntimeException {
		if (length(scope) == 0) return null;
		return Iterators.getLast(values().iterator());
	}

	/**
	 * Value at.
	 *
	 * @param index
	 *            the index
	 * @return the v
	 */
	default V valueAt(final int index) {
		if (size() == 0) return null;
		return Iterators.get(values().iterator(), index);
	}

	/**
	 * Method removeAll()
	 *
	 * @see gama.core.util.IContainer#removeAll(gama.core.runtime.IScope, gama.core.util.IContainer)
	 */
	@Override
	default void removeValues(final IScope scope, final IContainer<?, ?> values) {
		// we suppose we have pairs
		for (final Object o : values.iterable(scope)) { removeValue(scope, o); }
	}

	/**
	 * Method checkBounds()
	 *
	 * @see gama.core.util.IContainer#checkBounds(java.lang.Object, boolean)
	 */
	// @Override
	// default boolean checkBounds(final IScope scope, final Object index, final boolean forAdding) {
	// return true;
	// }

	/**
	 * Method removeIndexes()
	 *
	 * @see gama.core.util.IContainer.Modifiable#removeIndexes(gama.core.runtime.IScope, gama.core.util.IContainer)
	 */
	@Override
	default void removeIndexes(final IScope scope, final IContainer<?, ?> index) {
		for (final Object key : index.iterable(scope)) { remove(key); }
	}

	/**
	 * Method buildValue()
	 *
	 * @see gama.core.util.IContainer.Modifiable#buildValue(gama.core.runtime.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	default V buildValue(final IScope scope, final Object object) {
		// If we pass a pair to this method, but the content type is not a pair,
		// then it is is interpreted as a key + a value by addValue()
		if (!FLAGS.CAST_CONTAINER_CONTENTS
				|| object instanceof GamaPair && !getGamlType().getContentType().isTranslatableInto(Types.PAIR))
			return (V) object;
		return (V) getGamlType().getContentType().cast(scope, object, null, false);
	}

	/**
	 * Method buildIndex()
	 *
	 * @see gama.core.util.IContainer.Modifiable#buildIndex(gama.core.runtime.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	default K buildIndex(final IScope scope, final Object object) {
		return FLAGS.CAST_CONTAINER_CONTENTS ? (K) getGamlType().getKeyType().cast(scope, object, null, false)
				: (K) object;
	}

	/**
	 * Iterable.
	 *
	 * @param scope
	 *            the scope
	 * @return the java.lang. iterable
	 */
	@Override
	default java.lang.Iterable<V> iterable(final IScope scope) {
		return values();
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
	default V getFromIndicesList(final IScope scope, final IList<K> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.get(0));
		// We do not consider the case where multiple indices are used. Maybe
		// could be used in the
		// future to return a list of values ?
	}

	/**
	 * Contains.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	default boolean contains(final IScope scope, final Object o) {
		// AD: see Issue 918 and #2772
		return /* containsKey(o) || */containsValue(o);
	}

	/**
	 * Returns the list of values by default (NOT the list of pairs) Method listValue()
	 *
	 * @see gama.core.util.IContainer#listValue(gama.core.runtime.IScope)
	 */
	@Override
	default IList<V> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		IType myContentsType = getGamlType().getContentType();
		if (GamaType.requiresCasting(contentsType, myContentsType))
			return GamaListFactory.create(scope, contentsType, values());
		if (!copy) return GamaListFactory.wrap(contentsType, values());
		return GamaListFactory.createWithoutCasting(contentsType, values());
	}

	/**
	 * Matrix value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default IMatrix matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		// No attempt to coerce the contentsType, as both keys and values should
		// be in the same matrix
		final GamaObjectMatrix matrix = new GamaObjectMatrix(2, size(), contentsType);
		int i = 0;
		for (final Map.Entry entry : entrySet()) {
			matrix.set(scope, 0, i, GamaType.toType(scope, entry.getKey(), contentsType, false));
			matrix.set(scope, 1, i, GamaType.toType(scope, entry.getValue(), contentsType, false));
			i++;
		}
		return matrix;
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
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default IMatrix matrixValue(final IScope scope, final IType contentsType, final GamaPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		return matrixValue(scope, contentsType, copy);
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
		return "map(" + getPairs().serializeToGaml(includingBuiltIn) + ")";
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
	default IMap mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy) {
		IType myKeyType = getGamlType().getKeyType();
		IType myContentsType = getGamlType().getContentType();
		final boolean coerceKey = GamaType.requiresCasting(keyType, myKeyType);
		final boolean coerceValue = GamaType.requiresCasting(contentsType, myContentsType);
		if (coerceKey || coerceValue) {
			final IMap result = GamaMapFactory.create(keyType, contentsType, size());
			for (final Map.Entry<K, V> entry : entrySet()) {
				result.put(coerceKey ? result.buildIndex(scope, entry.getKey()) : entry.getKey(),
						coerceValue ? result.buildValue(scope, entry.getValue()) : entry.getValue());
			}
			return result;
		}
		if (copy) return copy(scope);
		if (!keyType.equals(myKeyType) || !contentsType.equals(myContentsType))
			return GamaMapFactory.wrap(keyType, contentsType, this);
		return this;

	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 */
	@Override
	default IMap<K, V> copy(final IScope scope) {
		return createWithoutCasting((IType<K>) getGamlType().getKeyType(), (IType<V>) getGamlType().getContentType(),
				this, isOrdered());
	}

	/**
	 * Method put()
	 *
	 * @see gama.core.util.IContainer#put(gama.core.runtime.IScope, java.lang.Object, java.lang.Object)
	 */
	@Override
	default void setValueAtIndex(final IScope scope, final Object index, final V value) {
		final K key = buildIndex(scope, index);
		final V val = buildValue(scope, value);
		put(key, val);
	}

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 */
	@operator (
			value = "reverse",
			can_be_const = true,
			type = IType.MAP,
			content_type = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
			index_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
	@doc (
			value = "Specialization of the reverse operator for maps. Reverses keys and values",
			comment = "",
			examples = { @example ("map<int,int> m <- [1::111,2::222, 3::333, 4::444];"), @example (
					value = "reverse(m)",
					equals = "map([111::1,222::2,333::3,444::4])") })

	@test ("map<int,int> m2 <- [1::111,2::222, 3::333, 4::444]; reverse(m2) = map([111::1,222::2,333::3,444::4])")

	@Override
	default IMap reverse(final IScope scope) {
		final IMap map = GamaMapFactory.create(getGamlType().getContentType(), getGamlType().getKeyType());
		for (final Map.Entry<K, V> entry : entrySet()) { map.put(entry.getValue(), entry.getKey()); }
		return map;
	}

	/**
	 * Method removeAll()
	 *
	 * @see gama.core.util.IContainer#removeAll(gama.core.runtime.IScope, java.lang.Object)
	 */
	@Override
	default void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		values().removeIf(v -> Objects.equal(value, v));
	}

	/**
	 * Method remove()
	 *
	 * @see gama.core.util.IContainer#remove(gama.core.runtime.IScope, java.lang.Object)
	 */
	@Override
	default void removeValue(final IScope scope, final Object value) {
		// Dont know what to do... Removing the first pair with value = value ?
		final Collection<V> values = values();
		for (V v : values) {
			if (Objects.equal(v, value)) {
				values.remove(v);
				return;
			}
		}
	}

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	@getter ("keys")
	default IList<K> getKeys() {
		// See issue #2792. key can be used to modify the map...
		return GamaListFactory.<K> createWithoutCasting(getGamlType().getKeyType(), keySet());
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	@getter ("values")
	default IList<V> getValues() {
		return GamaListFactory.<V> wrap((IType<V>) getGamlType().getContentType(), values());
	}

	/**
	 * Gets the.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @return the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	default V get(final IScope scope, final K index) throws GamaRuntimeException {
		return get(index);
	}

	/**
	 * Method setAll()
	 *
	 * @see gama.core.util.IContainer#setAll(gama.core.runtime.IScope, java.lang.Object)
	 */
	@Override
	default void setAllValues(final IScope scope, final V value) {
		replaceAll((k, v) -> value);
	}

	/**
	 * Gets the pairs.
	 *
	 * @return the pairs
	 */
	@getter (PAIRS)
	default IPairList getPairs() {
		// FIXME: in the future, this method will be directly operating upon the
		// entry set (so as to
		// avoir duplications). See GamaPair
		final GamaPairList<K, V> pairs = new GamaPairList<>(this);
		forEach((key, value) -> pairs
				.add(new GamaPair<>(key, value, getGamlType().getKeyType(), getGamlType().getContentType())));
		return pairs;
	}

	/**
	 * For each pair.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	default boolean forEachPair(final BiConsumerWithPruning<K, V> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getKey(), entry.getValue())) return false; }
		return true;
	}

	/**
	 * Checks if is ordered.
	 *
	 * @return true, if is ordered
	 */
	boolean isOrdered();

	/**
	 * For each value.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	default boolean forEachValue(final ConsumerWithPruning<? super V> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getValue())) return false; }
		return true;
	}

	/**
	 * For each key.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	default boolean forEachKey(final ConsumerWithPruning<K> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getKey())) return false; }
		return true;
	}

	/**
	 * To json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 28 oct. 2023
	 */
	@Override
	default JsonValue serializeToJson(final Json json) {
		JsonObject result = json.object();
		for (java.util.Map.Entry<K, V> entry : this.entrySet()) {
			result.add(entry.getKey().toString(), json.valueOf(entry.getValue()));
		}
		return result;
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
		return Types.MAP.of(getKeys().computeRuntimeType(scope).getContentType(),
				getValues().computeRuntimeType(scope).getContentType());
	}

}