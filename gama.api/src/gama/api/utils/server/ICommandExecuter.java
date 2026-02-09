/*******************************************************************************************************
 *
 * ICommandExecuter.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.util.Map;

import org.java_websocket.WebSocket;

/**
 *
 */
public interface ICommandExecuter {

	/**
	 * Push command.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @param map
	 *            the map
	 * @date 15 oct. 2023
	 */
	void pushCommand(WebSocket socket, Map<String, Object> map);

}