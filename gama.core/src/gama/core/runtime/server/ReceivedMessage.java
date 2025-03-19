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

import java.util.Map;

import gama.core.util.GamaMap;
import gama.gaml.types.Types;

/**
 *
 */
@SuppressWarnings ("unchecked")
public class ReceivedMessage extends GamaMap<String, Object> {

	/** The json contents. */
	String jsonContents;

	/**
	 * Instantiates a new received message.
	 *
	 * @param map
	 *            the map
	 */
	public ReceivedMessage(final String message, final Map<String, Object> map) {
		super(16, Types.STRING, Types.NO_TYPE);
		jsonContents = message;
		this.putAll(map);
	}

	/**
	 * Original contents.
	 *
	 * @return the string
	 */
	public String originalContents() {
		return jsonContents;
	}

}
