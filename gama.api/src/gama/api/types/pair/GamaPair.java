/*******************************************************************************************************
 *
 * GamaPair.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.pair;

import static java.util.Objects.hash;

import java.util.Objects;

import gama.annotations.getter;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.utils.StringUtils;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class IPair.
 */

@SuppressWarnings ({ "unchecked", "rawtypes" })
public record GamaPair<K, V>(K key, V value, IContainerType type) implements IPair<K, V> {

	// TODO Makes it inherit from Map.Entry<K,V> in order to tighten the link
	// between it and GamaMap
	// (have the entrySet() of GamaMap built from IPairs)
	// FIXME: This has still to be implemented

	/**
	 * Instantiates a new gama pair.
	 *
	 * @param k
	 *            the k
	 * @param v
	 *            the v
	 * @param keyType
	 *            the key type
	 * @param contentsType
	 *            the contents type
	 */
	GamaPair(final K k, final V v, final IType keyType, final IType contentsType) {
		this(k, v, Types.PAIR.of(keyType, contentsType));
	}

	/**
	 * Instantiates a new gama pair.
	 *
	 * @param scope
	 *            the scope
	 * @param k
	 *            the k
	 * @param v
	 *            the v
	 * @param keyType
	 *            the key type
	 * @param contentsType
	 *            the contents type
	 */
	GamaPair(final IScope scope, final K k, final V v, final IType keyType, final IType contentsType) {
		this((K) keyType.cast(scope, k, null, false), (V) contentsType.cast(scope, v, null, false),
				Types.PAIR.of(keyType, contentsType));
	}

	@Override
	public int hashCode() {
		return hash(key, value);
	}

	@Override
	public boolean equals(final Object a) {
		if (a == null) return false;
		if (a instanceof IPair p) return Objects.equals(key, p.getKey()) && Objects.equals(value, p.getValue());
		return false;
	}

	@Override
	public IContainerType getGamlType() { return type; }

	@Override
	@getter (KEY)
	public K getKey() { return key; }

	/**
	 * First.
	 *
	 * @return the k
	 */
	// FIXME: To be removed
	@Override
	public K first() {
		return key;
	}

	@Override
	@getter (VALUE)
	public V getValue() { return value; }

	/**
	 * Last.
	 *
	 * @return the v
	 */
	// FIXME: To be removed
	@Override
	public V last() {
		return value;
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return Cast.asString(scope, key) + "::" + Cast.asString(scope, value);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return StringUtils.toGaml(key, includingBuiltIn) + "::" + StringUtils.toGaml(value, includingBuiltIn);
	}

	@Override
	public String toString() {
		return (key == null ? "nil" : key.toString()) + "::" + (value == null ? "nil" : value.toString());
	}

	@Override
	public IPair<K, V> copy(final IScope scope) {
		return GamaPairFactory.createWith(key, value, type.getKeyType(), type.getContentType());
	}

	/**
	 * Method get()
	 *
	 * @see gama.api.types.misc.IContainer#get(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public Object get(final IScope scope, final Integer index) throws GamaRuntimeException {
		return index == 0 ? key : value;
	}

	/**
	 * Method getFromIndicesList()
	 *
	 * @see gama.api.types.misc.IContainer#getFromIndicesList(gama.api.runtime.scope.IScope, gama.api.types.list.IList)
	 */
	@Override
	public Object getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		return null;
	}

	/**
	 * Method contains()
	 *
	 * @see gama.api.types.misc.IContainer#contains(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		return o == null ? key == null || value == null : o.equals(key) || o.equals(value);
	}

	/**
	 * Method firstValue()
	 *
	 * @see gama.api.types.misc.IContainer#firstValue(gama.api.runtime.scope.IScope)
	 */
	@Override
	public Object firstValue(final IScope scope) throws GamaRuntimeException {
		return key;
	}

	/**
	 * Method lastValue()
	 *
	 * @see gama.api.types.misc.IContainer#lastValue(gama.api.runtime.scope.IScope)
	 */
	@Override
	public Object lastValue(final IScope scope) throws GamaRuntimeException {
		return value;
	}

	/**
	 * Method length()
	 *
	 * @see gama.api.types.misc.IContainer#length(gama.api.runtime.scope.IScope)
	 */
	@Override
	public int length(final IScope scope) {
		return 2;
	}

	/**
	 * Method isEmpty()
	 *
	 * @see gama.api.types.misc.IContainer#isEmpty(gama.api.runtime.scope.IScope)
	 */
	@Override
	public boolean isEmpty(final IScope scope) {
		return false;
	}

	/**
	 * Method reverse()
	 *
	 * @see gama.api.types.misc.IContainer#reverse(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IPair reverse(final IScope scope) throws GamaRuntimeException {
		return GamaPairFactory.createWith(value, key, type.getContentType(), type.getKeyType());
	}

	/**
	 * Method anyValue()
	 *
	 * @see gama.api.types.misc.IContainer#anyValue(gama.api.runtime.scope.IScope)
	 */
	@Override
	public Object anyValue(final IScope scope) {
		final int i = scope.getRandom().between(0, 1);
		return i == 0 ? key : value;
	}

	/**
	 * Method listValue()
	 *
	 * @see gama.api.types.misc.IContainer#listValue(gama.api.runtime.scope.IScope, gama.api.gaml.types.IType)
	 */
	@Override
	public IList listValue(final IScope scope, final IType contentType, final boolean copy) {
		return GamaListFactory.wrap(contentType, contentType.cast(scope, key, null, copy),
				contentType.cast(scope, value, null, copy));
	}

	/**
	 * Method matrixValue()
	 *
	 * @see gama.api.types.misc.IContainer#matrixValue(gama.api.runtime.scope.IScope, gama.api.gaml.types.IType)
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentType, final boolean copy) {
		return GamaMatrixFactory.createFrom(scope, listValue(scope, contentType, copy), contentType, null);
	}

	/**
	 * Method matrixValue()
	 *
	 * @see gama.api.types.misc.IContainer#matrixValue(gama.api.runtime.scope.IScope, gama.api.gaml.types.IType,
	 *      gama.core.metamodel.shape.GamaPoint)
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentType, final IPoint size, final boolean copy) {
		return GamaMatrixFactory.createFrom(scope, listValue(scope, contentType, copy), contentType, size);
	}

	/**
	 * Method mapValue()
	 *
	 * @see gama.api.types.misc.IContainer#mapValue(gama.api.runtime.scope.IScope, gama.api.gaml.types.IType,
	 *      gama.api.gaml.types.IType)
	 */
	@Override
	public IMap mapValue(final IScope scope, final IType keyType, final IType contentType, final boolean copy) {
		final IMap result = GamaMapFactory.create(keyType, contentType);
		result.setValueAtIndex(scope, key, value);
		return result;
	}

	/**
	 * Method iterable()
	 *
	 * @see gama.api.types.misc.IContainer#iterable(gama.api.runtime.scope.IScope)
	 */
	@Override
	public java.lang.Iterable iterable(final IScope scope) {
		return listValue(scope, Types.NO_TYPE, false);
	}

	@Override
	public boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
		return Objects.equals(key, o);
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "key", key, "value", value);
	}

}
