/*******************************************************************************************************
 *
 * IServerConfiguration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import org.java_websocket.WebSocket;

/**
 *
 */
public interface IServerConfiguration {

	/**
	 * @return
	 */
	WebSocket socket();

	/**
	 * @return
	 */
	String expId();

	/**
	 * @return
	 */
	boolean hasConsole();

	/**
	 * @return
	 */
	boolean hasDialog();

	/**
	 * @return
	 */
	boolean hasRuntime();

	/**
	 * @return
	 */
	boolean hasStatus();

	/**
	 * @param experimentID
	 * @return
	 */
	IServerConfiguration withExpId(String experimentID);

	/**
	 * @param socket
	 * @return
	 */
	IServerConfiguration withSocket(WebSocket socket);

}