/*******************************************************************************************************
 *
 * ReceivedMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import gama.core.util.GamaMap;
import gama.core.util.IMap;
import gama.gaml.types.Types;

/**
 *
 */
@SuppressWarnings ("unchecked")
public class ReceivedMessage extends GamaMap<String, Object> {

	/**
	 * @param capacity
	 * @param key
	 * @param content
	 */
	public ReceivedMessage() {
		super(16, Types.STRING, Types.NO_TYPE);
	}

	/**
	 * Instantiates a new received message.
	 *
	 * @param map
	 *            the map
	 */
	public ReceivedMessage(final IMap<String, Object> map) {
		this();
		this.putAll(map);
	}

}
