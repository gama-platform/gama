/*******************************************************************************************************
 *
 * ISocketCommand.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import org.java_websocket.WebSocket;

import gama.api.constants.IKeyword;

/**
 * The Interface ISocketCommand.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
@FunctionalInterface
public interface ISocketCommand {

	/** The Constant ESCAPED. */
	String ESCAPED = "escaped";

	/** The Constant EXPR. */
	String EXPR = "expr";

	/** The Constant SYNTAX. */
	String SYNTAX = "syntax";

	/** The Constant PARAMETERS. */
	String PARAMETERS = "parameters";

	/** The Constant UNTIL. */
	String UNTIL = IKeyword.UNTIL;

	/** The Constant SYNC. */
	String SYNC = "sync";

	/** The Constant SOCKET_ID. */
	String SOCKET_ID = "socket_id";

	/** The Constant EXP_ID. */
	String EXP_ID = "exp_id";

	/** The Constant NB_STEP. */
	String NB_STEP = "nb_step";

	/** The play. */
	String PLAY = "play";

	/** The pause. */
	String PAUSE = "pause";

	/** The step. */
	String STEP = IKeyword.STEP;

	/** The back. */
	String BACK = "back"; // synonym to stepBack

	/** The stepback. */
	String STEPBACK = "stepBack";

	/** The load. */
	String LOAD = "load";

	/** The stop. */
	String STOP = "stop";

	/** The reload. */
	String RELOAD = "reload";

	/** The expression. */
	String EXPRESSION = "expression";

	/** The evaluate. */
	String EVALUATE = "evaluate"; // synonym to expression

	/** The exit. */
	String EXIT = "exit";

	/** The download. */
	String DOWNLOAD = "download";

	/** The upload. */
	String UPLOAD = "upload";

	/** The description. */
	String DESCRIBE = "describe";

	/** The ask. This action allows to ask an agent to execute an action */
	String ASK = IKeyword.ASK;

	/** The args. for the arguments of an action to execute */
	String ARGS = "args";

	/** The validate. This action allows to validate a GAML expression passed as a string */
	String VALIDATE = "validate";

	/**
	 * Execute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @param map
	 *            the map
	 * @return the gama server message
	 * @date 15 oct. 2023
	 */
	GamaServerMessage execute(final IGamaServer server, final WebSocket socket, final ReceivedMessage map);

}
