/*******************************************************************************************************
 *
 * IPair.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.pair;

import java.util.Map;

import gama.annotations.doc;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IContainer.ToGet;

/**
 * The Interface IPair.
 * 
 * Represents a pair (or tuple) of two values: a key and a value. This interface extends both {@link IContainer} and
 * {@link Map.Entry}, allowing it to be used as a container and as a map entry. Pairs are used throughout GAMA to
 * represent two-element associations, coordinates, or simple key-value relationships.
 * 
 * <p>
 * The interface provides access to both elements through different naming conventions:
 * <ul>
 * <li>As a pair: {@link #first()} and {@link #last()}</li>
 * <li>As a map entry: {@link #getKey()} and {@link #getValue()}</li>
 * <li>As named attributes: "key" and "value" variables</li>
 * </ul>
 * </p>
 * 
 * @param <K>
 *            the type of the key (first element)
 * @param <V>
 *            the type of the value (second element)
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = IPair.KEY,
		type = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
		doc = { @doc ("Returns the key of this pair (can be nil)") }),
		@variable (
				name = IPair.VALUE,
				type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the value of this pair (can be nil)") }) })
public interface IPair<K, V>
		extends IContainer<Integer, Object>, IContainer.ToGet<Integer, Object>, Map.Entry<K, V> {

	/** The constant name for the key attribute. */
	String KEY = "key";

	/** The constant name for the value attribute. */
	String VALUE = "value";

	/**
	 * Returns the first element of the pair (the key).
	 * 
	 * @return the key (first element) of this pair, which can be null
	 */
	K first();

	/**
	 * Returns the second element of the pair (the value).
	 *
	 * @return the value (second element) of this pair, which can be null
	 */
	V last();

	/**
	 * Sets the key (first element) of this pair.
	 * 
	 * @param key
	 *            the new key to set (can be null)
	 */
	void setKey(Object key);

}
