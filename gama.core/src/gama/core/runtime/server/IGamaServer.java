/*******************************************************************************************************
 *
 * IGamaServer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import org.java_websocket.WebSocket;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.runtime.server.ISocketCommand.CommandException;
import gama.core.util.IMap;

/**
 *
 */
public interface IGamaServer {

	/**
	 * The listener interface for processing messages received by the server. See #438
	 *
	 * @see IServerEvent
	 */
	public interface Listener {

		/**
		 * Process a message received by the server before the server has a chance to process it. Returns true to allow
		 * the message to be processed by the server, false to skip it. Listeners can also alterate the message in any
		 * way they want (adding parameters, changing values, etc.)
		 *
		 * @param message
		 *            the message
		 * @return true, if successful
		 */
		boolean process(ReceivedMessage message);
	}

	/** The Constant DEFAULT_PING_INTERVAL. */
	int DEFAULT_PING_INTERVAL = 10000;

	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addListener(Listener listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void removeListener(Listener listener);

	/**
	 * Gets the experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @param expid
	 *            the expid
	 * @return the experiment
	 * @date 15 oct. 2023
	 */
	IExperimentPlan getExperiment(String socket, String expid);

	/**
	 * Execute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executionThread
	 *            the t
	 * @date 15 oct. 2023
	 */
	void execute(Runnable command);

	/**
	 * Adds the experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socketId
	 *            the socket id
	 * @param experimentId
	 *            the experiment id
	 * @date 3 nov. 2023
	 */
	void addExperiment(String socketId, String experimentId, IExperimentPlan plan);

	/**
	 * Obtain gui server configuration.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	GamaServerExperimentConfiguration obtainGuiServerConfiguration();

	/**
	 * Retrieve experiment plan.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name
	 * @param socket
	 *            the socket
	 * @param map
	 *            the map
	 * @return the i experiment plan
	 * @throws CommandException
	 *             the command exception
	 * @date 5 déc. 2023
	 */
	IExperimentPlan retrieveExperimentPlan(WebSocket socket, IMap<String, Object> map) throws CommandException;

	/**
	 * Stop
	 */
	void stop() throws InterruptedException;

}