/*******************************************************************************************************
 *
 * IRuntimeContainer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.misc;

import java.util.Collection;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import one.util.streamex.StreamEx;

/**
 * Shared runtime contract for collection-like values in GAMA.
 *
 * <p>
 * This interface captures the common Java-level API currently shared by true {@link IContainer} implementations and
 * by {@link gama.api.types.map.IMap maps}. It exists to progressively separate the runtime contract from the GAML
 * {@code container} inheritance branch.
 * </p>
 *
 * <p>
 * At this stage, {@link IContainer} still refines this interface and remains the main home of operator metadata. This
 * extracted contract is intended to host the methods that maps and indexed/sequential containers genuinely share at
 * runtime.
 * </p>
 *
 * @param <KeyType>
 *            the type used to address elements
 * @param <ValueType>
 *            the type of stored values
 *
 * @author drogoul
 * @since GAMA 2026-05
 *
 * @see IContainer
 * @see gama.api.types.map.IMap
 */
public interface IRuntimeContainer<KeyType, ValueType> extends IValue {

	/**
	 * Returns a copy of this runtime container.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a copy preserving the runtime semantics of the receiver
	 * @throws GamaRuntimeException
	 *             if the copy cannot be produced
	 */
	@Override
	IRuntimeContainer<KeyType, ValueType> copy(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the GAML type describing this runtime container.
	 *
	 * @return the associated parametric container type
	 */
	@Override
	IContainerType<?> getGamlType();

	/**
	 * Converts this runtime container to a list view or copy.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param contentType
	 *            the desired content type of the produced list
	 * @param copy
	 *            whether a copy should be forced
	 * @return a list representation of the receiver
	 */
	IList<ValueType> listValue(IScope scope, IType<?> contentType, boolean copy);

	/**
	 * Converts this runtime container to a matrix.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param contentType
	 *            the desired matrix content type
	 * @param copy
	 *            whether a copy should be forced
	 * @return a matrix representation of the receiver
	 */
	IMatrix<?> matrixValue(IScope scope, IType<?> contentType, boolean copy);

	/**
	 * Converts this runtime container to a matrix with the specified size.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param contentType
	 *            the desired matrix content type
	 * @param size
	 *            the preferred dimensions of the resulting matrix
	 * @param copy
	 *            whether a copy should be forced
	 * @return a matrix representation of the receiver
	 */
	IMatrix<?> matrixValue(IScope scope, IType<?> contentType, IPoint size, boolean copy);

	/**
	 * Converts this runtime container to a map.
	 *
	 * @param <D>
	 *            the desired value type
	 * @param <C>
	 *            the desired key type
	 * @param scope
	 *            the current execution scope
	 * @param keyType
	 *            the desired key type
	 * @param contentType
	 *            the desired value type
	 * @param copy
	 *            whether a copy should be forced
	 * @return a map representation of the receiver
	 */
	<D, C> IMap<C, D> mapValue(IScope scope, IType<C> keyType, IType<D> contentType, boolean copy);

	/**
	 * Returns an iterable over the values stored in this runtime container.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return an iterable over the receiver values
	 */
	java.lang.Iterable<? extends ValueType> iterable(IScope scope);

	/**
	 * Returns a sequential stream over the stored values.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a sequential stream over the values
	 */
	@SuppressWarnings ("unchecked")
	default StreamEx<ValueType> stream(final IScope scope) {
		if (this instanceof Collection) return StreamEx.of(((Collection<ValueType>) this).stream());
		return StreamEx.of(listValue(scope, Types.NO_TYPE, false));
	}

	/**
	 * Returns a parallel stream over the stored values.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a parallel stream over the values
	 */
	default StreamEx<ValueType> parallelStream(final IScope scope) {
		return stream(scope).parallel(GamaExecutorService.AGENT_PARALLEL_EXECUTOR);
	}

	/**
	 * Returns whether the receiver contains the provided value.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param o
	 *            the searched value
	 * @return {@code true} if the value is present
	 * @throws GamaRuntimeException
	 *             if containment cannot be evaluated
	 */
	boolean contains(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * Returns whether the receiver contains the provided key or index.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param o
	 *            the searched key or index
	 * @return {@code true} if the key or index is valid
	 * @throws GamaRuntimeException
	 *             if the test cannot be evaluated
	 */
	boolean containsKey(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * Returns the first accessible value of the receiver.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the first value, or {@code null} when appropriate
	 * @throws GamaRuntimeException
	 *             if the value cannot be retrieved
	 */
	ValueType firstValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the last accessible value of the receiver.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the last value, or {@code null} when appropriate
	 * @throws GamaRuntimeException
	 *             if the value cannot be retrieved
	 */
	ValueType lastValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the number of accessible values in the receiver.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the runtime length of the receiver
	 */
	int length(IScope scope);

	/**
	 * Returns the integer representation of this runtime container.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the length of the receiver
	 */
	@Override
	default int intValue(final IScope scope) {
		return length(scope);
	}

	/**
	 * Returns whether the receiver is empty.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return {@code true} if the receiver has no accessible value
	 */
	boolean isEmpty(IScope scope);

	/**
	 * Returns a reversed or structurally inverted copy of the receiver.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a reversed copy matching the runtime kind of the receiver
	 * @throws GamaRuntimeException
	 *             if the reversed representation cannot be produced
	 */
	IRuntimeContainer<?, ?> reverse(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns one accessible value of the receiver, typically chosen at random.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return one accessible value, or {@code null} when appropriate
	 */
	ValueType anyValue(IScope scope);

	/**
	 * Read capability for runtime containers that can retrieve values through keys or indices.
	 *
	 * @param <KeyType>
	 *            the addressing type
	 * @param <ValueType>
	 *            the retrieved value type
	 */
	interface ToGet<KeyType, ValueType> {

		/**
		 * Returns the value stored at the specified key or index.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param index
		 *            the key or index to resolve
		 * @return the resolved value
		 * @throws GamaRuntimeException
		 *             if access fails
		 */
		ValueType get(IScope scope, KeyType index) throws GamaRuntimeException;

		/**
		 * Returns the value resolved from a list of keys or indices.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param indices
		 *            the indices to resolve
		 * @return the resolved value or values, depending on the implementation
		 * @throws GamaRuntimeException
		 *             if access fails
		 */
		ValueType getFromIndicesList(IScope scope, IList<KeyType> indices) throws GamaRuntimeException;
	}

	/**
	 * Write capability for runtime containers that support insertion, replacement, and removal.
	 *
	 * @param <KeyType>
	 *            the key or index type used by write operations
	 * @param <ValueType>
	 *            the value type used by write operations
	 */
	interface ToSet<KeyType, ValueType> {

		/**
		 * Adds a value to the receiver.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param value
		 *            the value to add
		 */
		void addValue(IScope scope, ValueType value);

		/**
		 * Adds a value at the specified key or index.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param index
		 *            the key or index at which to add the value
		 * @param value
		 *            the value to add
		 */
		void addValueAtIndex(IScope scope, Object index, ValueType value);

		/**
		 * Replaces the value stored at the specified key or index.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param index
		 *            the key or index to update
		 * @param value
		 *            the new value
		 */
		void setValueAtIndex(IScope scope, Object index, ValueType value);

		/**
		 * Adds all values from another runtime container.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param index
		 *            an optional key or index hint
		 * @param values
		 *            the runtime container supplying the values
		 */
		void addValues(IScope scope, Object index, IRuntimeContainer<?, ?> values);

		/**
		 * Adds all values from another runtime container without an explicit index.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param values
		 *            the runtime container supplying the values
		 */
		default void addValues(final IScope scope, final IRuntimeContainer<?, ?> values) {
			addValues(scope, null, values);
		}

		/**
		 * Replaces all values of the receiver with the provided value when supported.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param value
		 *            the replacement value
		 */
		void setAllValues(IScope scope, ValueType value);

		/**
		 * Removes one occurrence of the provided value.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param value
		 *            the value to remove
		 */
		void removeValue(IScope scope, Object value);

		/**
		 * Removes the value stored at the specified key or index.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param index
		 *            the key or index to remove
		 */
		void removeIndex(IScope scope, Object index);

		/**
		 * Removes all values referenced by the specified keys or indices.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param index
		 *            the runtime container supplying the keys or indices to remove
		 */
		void removeIndexes(IScope scope, IRuntimeContainer<?, ?> index);

		/**
		 * Removes all values matching those supplied by another runtime container.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param values
		 *            the runtime container supplying the values to remove
		 */
		void removeValues(IScope scope, IRuntimeContainer<?, ?> values);

		/**
		 * Removes all occurrences of the provided value.
		 *
		 * @param scope
		 *            the current execution scope
		 * @param value
		 *            the value to remove everywhere it occurs
		 */
		void removeAllOccurrencesOfValue(IScope scope, Object value);
	}

	/**
	 * Shared readable runtime container capability.
	 *
	 * @param <Key>
	 *            the container key type
	 * @param <Value>
	 *            the container value type
	 * @param <AddressableKey>
	 *            the read key type
	 * @param <AddressableValue>
	 *            the read value type
	 */
	interface Addressable<Key, Value, AddressableKey, AddressableValue>
			extends IRuntimeContainer<Key, Value>, ToGet<AddressableKey, AddressableValue> {}

	/**
	 * Shared writable runtime container capability.
	 *
	 * @param <K>
	 *            the container key type
	 * @param <V>
	 *            the container value type
	 * @param <KeyToAdd>
	 *            the write key type
	 * @param <ValueToAdd>
	 *            the write value type
	 */
	interface Modifiable<K, V, KeyToAdd, ValueToAdd>
			extends IRuntimeContainer<K, V>, ToSet<KeyToAdd, ValueToAdd> {}
}
