/*******************************************************************************************************
 *
 * GamaMapWrapper.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util;

import java.util.Map;

import com.google.common.collect.ForwardingMap;

import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;

/**
 * The Class GamaMapWrapper.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings ("unchecked")
public class GamaMapWrapper<K, V> extends ForwardingMap<K, V> implements IMap<K, V> {

	/** The wrapped. */
	final Map<K, V> wrapped;
	
	/** The contents type. */
	final IType keyType, contentsType;
	
	/** The ordered. */
	boolean ordered;

	/**
	 * Instantiates a new gama map wrapper.
	 *
	 * @param wrapped the wrapped
	 * @param key the key
	 * @param contents the contents
	 * @param isOrdered the is ordered
	 */
	GamaMapWrapper(final Map<K, V> wrapped, final IType key, final IType contents, final boolean isOrdered) {
		this.wrapped = wrapped;
		this.contentsType = contents;
		this.keyType = key;
		this.ordered = isOrdered;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) { return true; }
		if (!(o instanceof IMap)) { return false; }
		return GamaMapFactory.equals(this, (IMap) o);
	}

	@Override
	protected Map<K, V> delegate() {
		return wrapped;
	}

	@Override
	public IContainerType<?> getGamlType() {
		return gama.gaml.types.Types.MAP.of(keyType, contentsType);
	}

	@Override
	public boolean isOrdered() {
		return ordered;
	}

}
