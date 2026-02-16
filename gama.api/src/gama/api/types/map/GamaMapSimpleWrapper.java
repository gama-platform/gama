/*******************************************************************************************************
 *
 * GamaMapSimpleWrapper.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.map;

import static gama.api.types.map.GamaMapFactory.createWithoutCasting;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.GamaPairList;
import gama.api.types.list.IList;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.pair.GamaPairFactory;
import gama.api.types.pair.IPair;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;

/**
 * The Class GamaMapSimpleWrapper.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
@SuppressWarnings ("unchecked")
public abstract class GamaMapSimpleWrapper<K, V> extends ForwardingMap<K, V> implements IMap<K, V> {

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof IMap)) return false;
		return GamaMapFactory.equals(this, (IMap) o);
	}

	@Override
	public IContainerType<?> getGamlType() { return Types.MAP; }

	/**
	 * Method buildValue()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.api.gaml.types.IContainerType)
	 */
	@Override
	public V buildValue(final IScope scope, final Object object) {
		return (V) object;
	}

	/**
	 * Method buildIndex()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.api.gaml.types.IContainerType)
	 */
	@Override
	public K buildIndex(final IScope scope, final Object object) {
		return (K) object;
	}

	@Override
	public IList<K> getKeys() { return GamaListFactory.<K> wrap(Types.NO_TYPE, keySet()); }

	@Override
	public IList<V> getValues() { return GamaListFactory.<V> wrap(Types.NO_TYPE, values()); }

	@Override
	public IPairList getPairs() {
		// FIXME: in the future, this method will be directly operating upon the
		// entry set (so as to
		// avoir duplications). See IPair
		final GamaPairList<K, V> pairs = new GamaPairList<>(this);
		forEach((key, value) -> pairs.add(GamaPairFactory.createWith(key, value, Types.NO_TYPE, Types.NO_TYPE)));
		return pairs;
	}

	@Override
	public IList<V> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		if (!copy) return GamaListFactory.wrap(contentsType, values());
		return GamaListFactory.create(scope, contentsType, values());
	}

	@Override
	public IMap mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy) {

		final IMap result = GamaMapFactory.create(keyType, contentsType, size());
		for (final Map.Entry<K, V> entry : entrySet()) {
			result.put(result.buildIndex(scope, entry.getKey()), result.buildValue(scope, entry.getValue()));
		}
		return result;

	}

	@Override
	public IMap reverse(final IScope scope) {
		final IMap map = isOrdered() ? GamaMapFactory.createOrdered() : GamaMapFactory.createUnordered();
		for (final Map.Entry<K, V> entry : entrySet()) { map.put(entry.getValue(), entry.getKey()); }
		return map;
	}

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@Override
	public String stringValue(final IScope scope) {
		return serializeToGaml(false);
	}

	/**
	 * Method add()
	 *
	 * @see gama.api.types.misc.IContainer#add(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void addValue(final IScope scope, final V v) {
		if (v instanceof IPair) {
			setValueAtIndex(scope, (K) ((IPair) v).first(), (V) ((IPair) v).last());
		} else {
			setValueAtIndex(scope, v, v);
		}
	}

	/**
	 * Method add()
	 *
	 * @see gama.api.types.misc.IContainer#add(gama.api.runtime.scope.IScope, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final V value) {
		// Cf. discussion on mailing-list about making "add" a synonym of "put"
		// for maps
		// if ( !containsKey(index) ) {
		setValueAtIndex(scope, index, value);
		// }
	}

	/**
	 * Method addAll()
	 *
	 * @see gama.api.types.misc.IContainer#addAll(gama.api.runtime.scope.IScope, gama.api.types.misc.IContainer)
	 */
	// AD July 2020: Addition of the index (see #2985)
	@Override
	public void addValues(final IScope scope, final Object index, final IContainer/* <?, IPair<K, V>> */ values) {
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
	 * @see gama.api.types.misc.IContainer#removeAt(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void removeIndex(final IScope scope, final Object index) {
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
	public boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
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
	public int length(final IScope scope) {
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
	public boolean isEmpty(final IScope scope) {
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
	public V anyValue(final IScope scope) {
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
	public V firstValue(final IScope scope) throws GamaRuntimeException {
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
	public V lastValue(final IScope scope) throws GamaRuntimeException {
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
	@Override
	public V valueAt(final int index) {
		if (size() == 0) return null;
		return Iterators.get(values().iterator(), index);
	}

	/**
	 * Method removeAll()
	 *
	 * @see gama.api.types.misc.IContainer#removeAll(gama.api.runtime.scope.IScope, gama.api.types.misc.IContainer)
	 */
	@Override
	public void removeValues(final IScope scope, final IContainer<?, ?> values) {
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
	 * @see gama.api.types.misc.IContainer.ToSet#removeIndexes(gama.api.runtime.scope.IScope,
	 *      gama.api.types.misc.IContainer)
	 */
	@Override
	public void removeIndexes(final IScope scope, final IContainer<?, ?> index) {
		for (final Object key : index.iterable(scope)) { remove(key); }
	}

	/**
	 * Iterable.
	 *
	 * @param scope
	 *            the scope
	 * @return the java.lang. iterable
	 */
	@Override
	public java.lang.Iterable<V> iterable(final IScope scope) {
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
	public V getFromIndicesList(final IScope scope, final IList<K> indices) throws GamaRuntimeException {
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
	public boolean contains(final IScope scope, final Object o) {
		// AD: see Issue 918 and #2772
		return /* containsKey(o) || */containsValue(o);
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
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		// No attempt to coerce the contentsType, as both keys and values should
		// be in the same matrix
		final IMatrix<?> matrix = GamaMatrixFactory.create(2, size(), contentsType);
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
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final IPoint preferredSize,
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
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "map(" + getPairs().serializeToGaml(includingBuiltIn) + ")";
	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 */
	@Override
	public IMap<K, V> copy(final IScope scope) {
		return createWithoutCasting((IType<K>) getGamlType().getKeyType(), (IType<V>) getGamlType().getContentType(),
				this, isOrdered());
	}

	/**
	 * Method put()
	 *
	 * @see gama.api.types.misc.IContainer#put(gama.api.runtime.scope.IScope, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setValueAtIndex(final IScope scope, final Object index, final V value) {
		final K key = buildIndex(scope, index);
		final V val = buildValue(scope, value);
		put(key, val);
	}

	/**
	 * Method removeAll()
	 *
	 * @see gama.api.types.misc.IContainer#removeAll(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		values().removeIf(v -> Objects.equal(value, v));
	}

	/**
	 * Method remove()
	 *
	 * @see gama.api.types.misc.IContainer#remove(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void removeValue(final IScope scope, final Object value) {
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
	public V get(final IScope scope, final K index) throws GamaRuntimeException {
		return get(index);
	}

	/**
	 * Method setAll()
	 *
	 * @see gama.api.types.misc.IContainer#setAll(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void setAllValues(final IScope scope, final V value) {
		replaceAll((k, v) -> value);
	}

	/**
	 * For each pair.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean forEachPair(final BiConsumerWithPruning<K, V> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getKey(), entry.getValue())) return false; }
		return true;
	}

	/**
	 * For each value.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean forEachValue(final ConsumerWithPruning<? super V> visitor) {
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
	@Override
	public boolean forEachKey(final ConsumerWithPruning<K> visitor) {
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
	public IJsonValue serializeToJson(final IJson json) {
		IJsonObject result = json.object();
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
	public IType<?> computeRuntimeType(final IScope scope) {
		return Types.MAP.of(getKeys().computeRuntimeType(scope).getContentType(),
				getValues().computeRuntimeType(scope).getContentType());
	}

}
