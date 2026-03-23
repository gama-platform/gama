/*******************************************************************************************************
 *
 * ReceivedMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@SuppressWarnings ("unchecked")
public class ReceivedMessage extends HashMap<String, Object> {

	/** The json contents. */
	String jsonContents;

	/**
	 * Instantiates a new received message.
	 *
	 * @param map
	 *            the map
	 */
	public ReceivedMessage(final String message, final Map<String, Object> map) {
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
