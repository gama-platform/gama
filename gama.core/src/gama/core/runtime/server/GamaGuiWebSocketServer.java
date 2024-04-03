/*******************************************************************************************************
 *
 * GamaGuiWebSocketServer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.runtime.GAMA;
import gama.core.runtime.IExperimentStateListener;
import gama.core.runtime.server.ISocketCommand.CommandException;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;

/**
 * The Class GamaWebSocketServer.
 */
public class GamaGuiWebSocketServer extends GamaWebSocketServer implements IExperimentStateListener {

	/** The current server config. */
	private GamaServerExperimentConfiguration currentServerConfig = GamaServerExperimentConfiguration.GUI;

	/** The current state. */
	private volatile State currentState = State.NONE;

	/**
	 * Start for GUI. No SSL and a default ping interval
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaGuiWebSocketServer StartForGUI(final int port) {
		return StartForGUI(port, DEFAULT_PING_INTERVAL);
	}

	/**
	 * Start for GUI.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to which to listen to
	 * @param ssl
	 *            the ssl wether to use ssl or no
	 * @param jksPath
	 *            the jks path the store path
	 * @param spwd
	 *            the spwd the store password
	 * @param kpwd
	 *            the kpwd the key password
	 * @param pingInterval
	 *            the ping interval
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaGuiWebSocketServer StartForGUI(final int port, final int pingInterval) {
		GamaGuiWebSocketServer server = new GamaGuiWebSocketServer(port, pingInterval);
		server.currentServerConfig = GamaServerExperimentConfiguration.GUI;
		server.start();
		return server;
	}

	/**
	 * Instantiates a new gama web socket server.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to listen to
	 * @param runner
	 *            the runner
	 * @param ssl
	 *            the ssl
	 * @param jksPath
	 *            the jks path
	 * @param spwd
	 *            the spwd
	 * @param kpwd
	 *            the kpwd
	 * @param interval
	 *            the interval
	 * @date 16 oct. 2023
	 */
	private GamaGuiWebSocketServer(final int port, final int interval) {
		super(port, interval);
	}

	@Override
	public void onOpen(final WebSocket socket, final ClientHandshake handshake) {
		currentServerConfig = currentServerConfig.withSocket(socket);
		GAMA.getGui().getConsole().addConsoleListener(console);
		super.onOpen(socket, handshake);
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		super.onClose(conn, code, reason, remote);
		GAMA.getGui().getConsole().removeConsoleListener(console);
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		GAMA.addExperimentStateListener(this);
	}

	@Override
	public void stop() throws InterruptedException {
		super.stop();
		GAMA.removeExperimentStateListener(this);
	}

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
	@Override
	public IExperimentPlan getExperiment(final String socket, final String expid) {
		return GAMA.getExperiment();
	}

	/**
	 * Execute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executionThread
	 *            the t
	 * @date 15 oct. 2023
	 */
	@Override
	public void execute(final Runnable command) {
		command.run();
	}

	@Override
	public void updateStateTo(final IExperimentPlan experiment, final State state) {
		if (state != currentState) {
			currentState = state;
			WebSocket ws = currentServerConfig.socket();
			if (ws == null || ws.isClosed()) return;
			ws.send(Json.getNew()
					.valueOf(new GamaServerMessage(GamaServerMessage.Type.SimulationStatus, state.name(), "0"))
					.toString());
		}
	}

	/**
	 * Obtain gui server configuration.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	@Override
	public GamaServerExperimentConfiguration obtainGuiServerConfiguration() {
		return currentServerConfig;
	}

	@Override
	public void addExperiment(final String socketId, final String experimentId, final IExperimentPlan plan) {}

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
	@Override
	public IExperimentPlan retrieveExperimentPlan(final WebSocket socket, final IMap<String, Object> map)
			throws CommandException {
		IExperimentPlan plan = GAMA.getExperiment();
		if (plan == null || plan.getAgent() == null || plan.getAgent().dead() || plan.getCurrentSimulation() == null)
			throw new CommandException(new CommandResponse(GamaServerMessage.Type.UnableToExecuteRequest,
					"Unable to find the experiment or simulation", map, false));
		return plan;
	}

}
