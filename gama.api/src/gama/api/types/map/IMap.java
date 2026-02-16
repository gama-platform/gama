/*******************************************************************************************************
 *
 * IMap.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.map;

import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IContainer.Addressable;
import gama.api.types.misc.IContainer.Modifiable;
import gama.api.types.misc.IContainer.ToSet;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;

/**
 * The Interface IMap.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
@vars ({ @variable (
		name = IMap.KEYS,
		type = IType.LIST,
		of = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
		doc = { @doc ("Returns the list of keys of this map (in their order of insertion)") }),
		@variable (
				name = IMap.VALUES,
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of values of this map (in their order of insertion)") }),
		@variable (
				name = IMap.PAIRS,
				type = IType.LIST,
				of = ITypeProvider.KEY_AND_CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of pairs (key, value) that compose this map") }) })
@SuppressWarnings ("unchecked")

public interface IMap<K, V> extends Map<K, V>, IContainer.Modifiable<K, V, K, V>, IContainer.Addressable<K, V, K, V> {

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
	 *
	 *
	 * /** Value at.
	 *
	 * @param index
	 *            the index
	 * @return the v
	 */
	V valueAt(final int index);

	/**
	 * Method buildValue()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	V buildValue(final IScope scope, final Object object);

	/**
	 * Method buildIndex()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	K buildIndex(final IScope scope, final Object object);

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
	IMap reverse(final IScope scope);

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	@getter ("keys")
	IList<K> getKeys();

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	@getter ("values")
	IList<V> getValues();

	/**
	 * Gets the pairs.
	 *
	 * @return the pairs
	 */
	@getter (PAIRS)
	IPairList getPairs();

	/**
	 * For each pair.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean forEachPair(final BiConsumerWithPruning<K, V> visitor);

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
	boolean forEachValue(final ConsumerWithPruning<? super V> visitor);

	/**
	 * For each key.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean forEachKey(final ConsumerWithPruning<K> visitor);

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	IMap<K, V> copy(IScope scope) throws GamaRuntimeException;

}