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
 * A record representing an immutable key-value pair in the GAMA modeling and simulation platform.
 * <p>
 * {@code GamaPair} implements {@link IPair} and serves as the primary concrete pair type used throughout GAMA. It holds
 * a typed key and value, along with an {@link IContainerType} that encodes the GAML types of both components. Pairs are
 * expressed in GAML using the {@code ::} operator (e.g., {@code key::value}).
 * </p>
 * <p>
 * Being a Java {@code record}, instances of {@code GamaPair} are immutable. Equality is based on the key and value
 * only, not on the container type.
 * </p>
 *
 * @param <K>
 *            the type of the key
 * @param <V>
 *            the type of the value
 * @param key
 *            the key component of the pair
 * @param value
 *            the value component of the pair
 * @param type
 *            the GAML container type encoding the key and value types
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public record GamaPair<K, V>(K key, V value, IContainerType type) implements IPair<K, V> {

	// TODO Makes it inherit from Map.Entry<K,V> in order to tighten the link
	// between it and GamaMap
	// (have the entrySet() of GamaMap built from IPairs)
	// FIXME: This has still to be implemented

	/**
	 * Constructs a new {@code GamaPair} with the given key and value, deriving the container type from the provided key
	 * and content GAML types.
	 *
	 * @param k
	 *            the key
	 * @param v
	 *            the value
	 * @param keyType
	 *            the GAML type of the key
	 * @param contentsType
	 *            the GAML type of the value
	 */
	GamaPair(final K k, final V v, final IType keyType, final IType contentsType) {
		this(k, v, Types.PAIR.of(keyType, contentsType));
	}

	/**
	 * Constructs a new {@code GamaPair} by casting the given key and value to the specified GAML types using the
	 * provided scope, then building the corresponding container type.
	 *
	 * @param scope
	 *            the execution scope used for casting
	 * @param k
	 *            the key to cast
	 * @param v
	 *            the value to cast
	 * @param keyType
	 *            the GAML type to cast the key to
	 * @param contentsType
	 *            the GAML type to cast the value to
	 */
	GamaPair(final IScope scope, final K k, final V v, final IType keyType, final IType contentsType) {
		this((K) keyType.cast(scope, k, null, false), (V) contentsType.cast(scope, v, null, false),
				Types.PAIR.of(keyType, contentsType));
	}

	/**
	 * Returns a hash code based on the key and value components.
	 *
	 * @return a hash code computed from the key and value
	 */
	@Override
	public int hashCode() {
		return hash(key, value);
	}

	/**
	 * Checks equality with another object. Two pairs are considered equal if they are both instances of {@link IPair}
	 * and have equal keys and equal values.
	 *
	 * @param a
	 *            the object to compare with
	 * @return {@code true} if {@code a} is an {@link IPair} with the same key and value; {@code false} otherwise
	 */
	@Override
	public boolean equals(final Object a) {
		if (a == null) return false;
		if (a instanceof IPair p) return Objects.equals(key, p.key()) && Objects.equals(value, p.value());
		return false;
	}

	/**
	 * Returns the GAML container type of this pair, encoding both the key and value types.
	 *
	 * @return the {@link IContainerType} of this pair
	 */
	@Override
	public IContainerType getGamlType() { return type; }

	/**
	 * Returns the string representation of this pair in GAML syntax, casting both the key and value to strings using
	 * the provided scope.
	 *
	 * @param scope
	 *            the execution scope used for string casting
	 * @return a string of the form {@code "key::value"}
	 * @throws GamaRuntimeException
	 *             if an error occurs during string casting
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return Cast.asString(scope, key) + "::" + Cast.asString(scope, value);
	}

	/**
	 * Serializes this pair to a GAML expression string using the {@code ::} operator.
	 *
	 * @param includingBuiltIn
	 *            whether to include built-in elements in the serialization
	 * @return a GAML string of the form {@code "key::value"}
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return StringUtils.toGaml(key, includingBuiltIn) + "::" + StringUtils.toGaml(value, includingBuiltIn);
	}

	/**
	 * Returns a human-readable string representation of this pair using the {@code ::} separator. If either the key or
	 * the value is {@code null}, the string {@code "nil"} is used in its place.
	 *
	 * @return a string of the form {@code "key::value"}
	 */
	@Override
	public String toString() {
		return (key == null ? "nil" : key.toString()) + "::" + (value == null ? "nil" : value.toString());
	}

	/**
	 * Creates and returns a shallow copy of this pair with the same key, value, and component types.
	 *
	 * @param scope
	 *            the execution scope
	 * @return a new {@link IPair} with the same key, value, and types
	 */
	@Override
	public IPair<K, V> copy(final IScope scope) {
		return GamaPairFactory.createWith(key, value, type.getKeyType(), type.getContentType());
	}

	/**
	 * Returns the element at the given integer index. Index {@code 0} returns the key; any other index returns the
	 * value.
	 *
	 * @param scope
	 *            the execution scope
	 * @param index
	 *            the index to retrieve ({@code 0} for key, {@code 1} for value)
	 * @return the key if {@code index == 0}, the value otherwise
	 * @throws GamaRuntimeException
	 *             if an error occurs during access
	 */
	@Override
	public Object get(final IScope scope, final Integer index) throws GamaRuntimeException {
		return index == 0 ? key : value;
	}

	/**
	 * Returns the element corresponding to a list of indices. Not supported for pairs; always returns {@code null}.
	 *
	 * @param scope
	 *            the execution scope
	 * @param indices
	 *            the list of indices
	 * @return {@code null}
	 * @throws GamaRuntimeException
	 *             if an error occurs during access
	 */
	@Override
	public Object getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		return null;
	}

	/**
	 * Returns whether the given object is equal to the key or the value of this pair. If {@code o} is {@code null},
	 * returns {@code true} if either the key or the value is {@code null}.
	 *
	 * @param scope
	 *            the execution scope
	 * @param o
	 *            the object to search for
	 * @return {@code true} if the pair contains {@code o} as either its key or its value
	 * @throws GamaRuntimeException
	 *             if an error occurs during the check
	 */
	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		return o == null ? key == null || value == null : o.equals(key) || o.equals(value);
	}

	/**
	 * Returns the first value (i.e., the key) of this pair.
	 *
	 * @param scope
	 *            the execution scope
	 * @return the key
	 * @throws GamaRuntimeException
	 *             if an error occurs during access
	 */
	@Override
	public Object firstValue(final IScope scope) throws GamaRuntimeException {
		return key;
	}

	/**
	 * Returns the last value (i.e., the value) of this pair.
	 *
	 * @param scope
	 *            the execution scope
	 * @return the value
	 * @throws GamaRuntimeException
	 *             if an error occurs during access
	 */
	@Override
	public Object lastValue(final IScope scope) throws GamaRuntimeException {
		return value;
	}

	/**
	 * Returns the number of elements in this pair, which is always {@code 2} (one key and one value).
	 *
	 * @param scope
	 *            the execution scope
	 * @return {@code 2}
	 */
	@Override
	public int length(final IScope scope) {
		return 2;
	}

	/**
	 * Returns whether this pair is empty. A pair always contains exactly two elements and is therefore never empty.
	 *
	 * @param scope
	 *            the execution scope
	 * @return {@code false} always
	 */
	@Override
	public boolean isEmpty(final IScope scope) {
		return false;
	}

	/**
	 * Returns a new pair with the key and value swapped. The key type and content type are also swapped accordingly.
	 *
	 * @param scope
	 *            the execution scope
	 * @return a new {@link IPair} with {@code value::key}
	 * @throws GamaRuntimeException
	 *             if an error occurs during the operation
	 */
	@Override
	public IPair reverse(final IScope scope) throws GamaRuntimeException {
		return GamaPairFactory.createWith(value, key, type.getContentType(), type.getKeyType());
	}

	/**
	 * Returns either the key or the value of this pair, chosen at random using the scope's random number generator.
	 *
	 * @param scope
	 *            the execution scope providing the random number generator
	 * @return the key or the value, chosen randomly
	 */
	@Override
	public Object anyValue(final IScope scope) {
		final int i = scope.getRandom().between(0, 1);
		return i == 0 ? key : value;
	}

	/**
	 * Returns a two-element {@link IList} containing the key and the value, both cast to the specified content type.
	 *
	 * @param scope
	 *            the execution scope
	 * @param contentType
	 *            the GAML type to cast both elements to
	 * @param copy
	 *            whether to copy elements during casting
	 * @return a list of the form {@code [key, value]} with elements cast to {@code contentType}
	 */
	@Override
	public IList listValue(final IScope scope, final IType contentType, final boolean copy) {
		return GamaListFactory.wrap(contentType, contentType.cast(scope, key, null, copy),
				contentType.cast(scope, value, null, copy));
	}

	/**
	 * Returns an {@link IMatrix} built from this pair's list representation, with no size constraint.
	 *
	 * @param scope
	 *            the execution scope
	 * @param contentType
	 *            the GAML type to cast the elements to
	 * @param copy
	 *            whether to copy elements during casting
	 * @return a matrix containing the key and value
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentType, final boolean copy) {
		return GamaMatrixFactory.createFrom(scope, listValue(scope, contentType, copy), contentType, null);
	}

	/**
	 * Returns an {@link IMatrix} built from this pair's list representation, constrained to the given size.
	 *
	 * @param scope
	 *            the execution scope
	 * @param contentType
	 *            the GAML type to cast the elements to
	 * @param size
	 *            the desired size of the matrix
	 * @param copy
	 *            whether to copy elements during casting
	 * @return a matrix of the given size containing the key and value
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentType, final IPoint size, final boolean copy) {
		return GamaMatrixFactory.createFrom(scope, listValue(scope, contentType, copy), contentType, size);
	}

	/**
	 * Returns a single-entry {@link IMap} whose only entry maps the key to the value. Both are cast to the specified
	 * key and content types.
	 *
	 * @param scope
	 *            the execution scope
	 * @param keyType
	 *            the GAML type for the map's key
	 * @param contentType
	 *            the GAML type for the map's value
	 * @param copy
	 *            whether to copy elements during casting
	 * @return a map containing this pair as its sole entry
	 */
	@Override
	public IMap mapValue(final IScope scope, final IType keyType, final IType contentType, final boolean copy) {
		final IMap result = GamaMapFactory.create(keyType, contentType);
		result.setValueAtIndex(scope, key, value);
		return result;
	}

	/**
	 * Returns an {@link Iterable} over the elements of this pair (key, then value), using {@link Types#NO_TYPE} as the
	 * content type and without copying.
	 *
	 * @param scope
	 *            the execution scope
	 * @return an iterable over the key and value
	 */
	@Override
	public java.lang.Iterable iterable(final IScope scope) {
		return listValue(scope, Types.NO_TYPE, false);
	}

	/**
	 * Returns whether the given object is equal to the key of this pair.
	 *
	 * @param scope
	 *            the execution scope
	 * @param o
	 *            the object to compare with the key
	 * @return {@code true} if the key equals {@code o}
	 * @throws GamaRuntimeException
	 *             if an error occurs during the check
	 */
	@Override
	public boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
		return Objects.equals(key, o);
	}

	/**
	 * Serializes this pair to a JSON object with {@code "key"} and {@code "value"} fields, tagged with the GAML type
	 * information.
	 *
	 * @param json
	 *            the JSON serialization context
	 * @return an {@link IJsonValue} representing this pair as a typed JSON object
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "key", key, "value", value);
	}

}
