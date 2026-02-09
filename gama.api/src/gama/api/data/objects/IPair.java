/*******************************************************************************************************
 *
 * IPair.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;

import java.util.Map;

import gama.annotations.doc;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;

/**
 *
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

	/** The Constant KEY. */
	String KEY = "key";

	/** The Constant VALUE. */
	String VALUE = "value";

	/**
	 * @return
	 */
	K first();

	/**
	 * Second.
	 *
	 * @return the v
	 */
	V last();

	/**
	 * @param key
	 */
	void setKey(Object key);

}
