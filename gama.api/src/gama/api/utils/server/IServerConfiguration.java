/*******************************************************************************************************
 *
 * IServerConfiguration.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import org.java_websocket.WebSocket;

/**
 *
 */
public interface IServerConfiguration {

	/** The null. */
	IServerConfiguration NULL = new IServerConfiguration() {};

	/**
	 * @return
	 */
	default WebSocket socket() {
		return null;
	}

	/**
	 * @return
	 */
	default String expId() {
		return "";
	}

	/**
	 * @return
	 */
	default boolean hasConsole() {
		return false;
	}

	/**
	 * @return
	 */
	default boolean hasDialog() {
		return false;
	}

	/**
	 * @return
	 */
	default boolean hasRuntime() {
		return false;
	}

	/**
	 * @return
	 */
	default boolean hasStatus() {
		return false;
	}

	/**
	 * @param experimentID
	 * @return
	 */
	default IServerConfiguration withExpId(final String experimentID) {
		return this;
	}

	/**
	 * @param socket
	 * @return
	 */
	default IServerConfiguration withSocket(final WebSocket socket) {
		return this;
	}

}