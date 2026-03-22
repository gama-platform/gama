/*******************************************************************************************************
 *
 * IList.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.list;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaIntegerType;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.pair.GamaPairFactory;
import gama.api.types.pair.IPair;
import gama.api.utils.StringUtils;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import gama.dev.FLAGS;

/**
 * The main interface for type-safe, indexed lists in the GAMA modeling platform.
 *
 * <p>
 * {@code IList} extends Java's {@link List} interface while integrating with GAMA's type system and runtime
 * capabilities. It provides:
 * </p>
 * <ul>
 * <li><b>Type Safety</b>: Tracks content type via {@link IContainerType} for GAML type checking</li>
 * <li><b>Modifiable & Addressable</b>: Supports indexed access and modifications with proper type conversions</li>
 * <li><b>GAML Integration</b>: Serialization, conversion to other container types (map, matrix), and runtime type
 * computation</li>
 * <li><b>Scope-Aware Operations</b>: Most operations accept an {@link IScope} for proper context and random number
 * generation</li>
 * </ul>
 *
 * <h2>Core Features</h2>
 *
 * <h3>1. Type Conversions</h3>
 * <p>
 * Provides seamless conversion to other GAMA container types:
 * </p>
 *
 * <pre>
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(Types.INT, 1, 2, 3);
 *
 * // Convert to matrix
 * IMatrix&lt;Integer&gt; matrix = numbers.matrixValue(scope, Types.INT, false);
 *
 * // Convert to map (elements become key-value pairs)
 * IMap&lt;?, ?&gt; map = numbers.mapValue(scope, Types.INT, Types.INT, false);
 * </pre>
 *
 * <h3>2. Indexed Operations</h3>
 * <p>
 * All indexed operations are type-safe and scope-aware:
 * </p>
 *
 * <pre>
 * // Add with automatic type casting
 * list.addValueAtIndex(scope, 0, "42"); // Casts "42" to content type
 *
 * // Set with type casting
 * list.setValueAtIndex(scope, 1, someValue);
 *
 * // Remove by index
 * list.removeIndex(scope, 2);
 * </pre>
 *
 * <h3>3. GAML Serialization</h3>
 * <p>
 * Supports serialization to GAML code and JSON:
 * </p>
 *
 * <pre>
 * String gaml = list.serializeToGaml(false); // "[1, 2, 3]"
 * IJsonValue json = list.serializeToJson(jsonContext);
 * </pre>
 *
 * <h3>4. Random Access</h3>
 * <p>
 * Provides scope-aware random element selection:
 * </p>
 *
 * <pre>
 * E random = list.anyValue(scope); // Uses scope's random generator
 * </pre>
 *
 * <h2>Default Implementations</h2>
 * <p>
 * Most methods provide default implementations that delegate to standard {@link List} operations while ensuring proper
 * type handling via {@link #buildValue(IScope, Object)} and {@link #buildIndex(IScope, Object)}.
 * </p>
 *
 * <h2>Type Building</h2>
 * <p>
 * The interface provides methods to convert values and indices according to the list's content type:
 * </p>
 * <ul>
 * <li>{@link #buildValue(IScope, Object)} - Casts values to the content type</li>
 * <li>{@link #buildIndex(IScope, Object)} - Casts indices to Integer</li>
 * <li>{@link #buildValues(IScope, IContainer)} - Casts entire containers</li>
 * </ul>
 *
 * <h2>Usage in GAML</h2>
 * <p>
 * In GAML language, lists are created and manipulated using GAML syntax:
 * </p>
 *
 * <pre>
 * list&lt;int&gt; myList &lt;- [1, 2, 3];
 * add 4 to: myList;
 * remove index: 0 from: myList;
 * </pre>
 *
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li><b>Content Type Casting</b>: Controlled by {@code FLAGS.CAST_CONTAINER_CONTENTS}. When enabled, all operations
 * cast elements to the declared content type.</li>
 * <li><b>Empty Lists</b>: Operations on empty lists (first, last, any) return {@code null} rather than throwing
 * exceptions.</li>
 * <li><b>Index Validation</b>: Out-of-bounds indices follow standard Java List behavior (IndexOutOfBoundsException).
 * </li>
 * <li><b>Runtime Type</b>: The runtime type is computed dynamically from actual elements via
 * {@link #computeRuntimeType(IScope)}.</li>
 * </ul>
 *
 * @param <E>
 *            the element type
 *
 * @see GamaListFactory for creating IList instances
 * @see GamaList for the primary implementation
 * @see IContainer for parent container interfaces
 * @see List for Java List interface
 *
 * @author drogoul
 * @since 14 déc. 2011
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
		for (final E e : this) { result.addValue(scope, GamaPairFactory.castToPair(scope, e, kt, ct, copy)); }
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
		this.subList(Cast.asInt(scope, range.key()), Cast.asInt(scope, range.value()))
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
	 * @see gama.api.types.misc.IContainer.ToSet#removeIndexes(gama.api.runtime.scope.IScope,
	 *      gama.api.types.misc.IContainer)
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
	 * @see gama.api.types.misc.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	default E buildValue(final IScope scope, final Object object) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return (E) object;
		return (E) getGamlType().getContentType().cast(scope, object, null, false);
	}

	/**
	 * Method buildValues()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildValues(gama.api.runtime.scope.IScope,
	 *      gama.api.types.misc.IContainer, gama.gaml.types.IContainerType)
	 */
	default IList<E> buildValues(final IScope scope, final IContainer objects) {
		if (!FLAGS.CAST_CONTAINER_CONTENTS) return (IList<E>) objects;
		return (IList<E>) getGamlType().cast(scope, objects, null, false);
	}

	/**
	 * Method buildIndex()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
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
