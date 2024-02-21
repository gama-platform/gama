/*******************************************************************************************************
 *
 * GamaListWrapper.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util;

import java.util.List;

import com.google.common.collect.ForwardingList;

import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class GamaListWrapper.
 *
 * @param <E> the element type
 */
public class GamaListWrapper<E> extends ForwardingList<E> implements IList<E> {

	/** The wrapped. */
	final List<E> wrapped;
	
	/** The type. */
	final IContainerType type;

	/**
	 * Instantiates a new gama list wrapper.
	 *
	 * @param wrapped the wrapped
	 * @param contents the contents
	 */
	GamaListWrapper(final List<E> wrapped, final IType contents) {
		this.type = Types.LIST.of(contents);
		this.wrapped = wrapped;
	}

	@Override
	public IContainerType<?> getGamlType() {
		return type;
	}

	@Override
	protected List<E> delegate() {
		return wrapped;
	}

}
