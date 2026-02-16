/*******************************************************************************************************
 *
 * GamaMapSupplier.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.map;

import java.util.function.Supplier;

import gama.api.gaml.types.IType;

/**
 * The Class GamaMapSupplier.
 */
public class GamaMapSupplier implements Supplier<IMap> {

	/** The k. */
	IType k;

	/** The c. */
	IType c;

	/**
	 * Instantiates a new gama map supplier.
	 *
	 * @param key
	 *            the key
	 * @param contents
	 *            the contents
	 * @param internalGamaMapBuilder
	 *            TODO
	 */
	public GamaMapSupplier(final IType key, final IType contents) {
		k = key;
		c = contents;
	}

	@Override
	public IMap get() {
		return GamaMapFactory.create(k, c);
	}
}