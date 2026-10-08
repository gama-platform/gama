/*******************************************************************************************************
 *
 * GamaGuiWebSocketServer.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application.server;

import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import gama.api.GAMA;
import gama.api.exceptions.CommandException;
import gama.api.kernel.simulation.IExperimentStateListener;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.server.CommandResponse;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.GamaWebSocketServer;
import gama.api.utils.server.IServerConfiguration;
import gama.api.utils.server.MessageType;

/**
 * The Class GamaWebSocketServer.
 */
public class GamaGuiWebSocketServer extends GamaWebSocketServer implements IExperimentStateListener {

	/**
	 * Start server.
	 */
	public static void startGuiServer() {
		if (!GAMA.isInHeadLessMode() && GamaPreferences.Runtime.CORE_SERVER_MODE.getValue()) { createGuiServer(); }
		GamaPreferences.Runtime.CORE_SERVER_MODE.onChange(newValue -> { if (newValue) { createGuiServer(); } });
	}

	/**
	 * Creates the gui server.
	 */
	private static void createGuiServer() {
		final int port = GamaPreferences.Runtime.CORE_SERVER_PORT.getValue();
		final int ping = GamaPreferences.Runtime.CORE_SERVER_PING.getValue();
		final boolean noDelay = GamaPreferences.Runtime.CORE_SERVER_NO_DELAY.getValue();
		GAMA.setServer(startForGUI(port, ping, noDelay));
	}

	/** The current server config. */
	private IServerConfiguration currentServerConfig = GamaWebSocketServer.GUI;

	/** The current state. */
	private volatile State currentState = State.NONE;

	/**
	 * Start for GUI. No SSL, the default ping interval, and no TCP_NODELAY option
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaGuiWebSocketServer startForGUI(final int port) {
		return startForGUI(port, DEFAULT_PING_INTERVAL, false);
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
	public static GamaGuiWebSocketServer startForGUI(final int port, final int pingInterval, final boolean noDelay) {
		try {
			GamaGuiWebSocketServer server = new GamaGuiWebSocketServer(port, pingInterval, noDelay);
			server.currentServerConfig = GamaWebSocketServer.GUI;
			server.start();
			return server;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
	private GamaGuiWebSocketServer(final int port, final int interval, final boolean noDelay) {
		super(port, interval, noDelay);
	}

	/**
	 * On open.
	 *
	 * @param socket
	 *            the socket
	 * @param handshake
	 *            the handshake
	 */
	@Override
	public void onOpen(final WebSocket socket, final ClientHandshake handshake) {
		currentServerConfig = currentServerConfig.withSocket(socket);
		GAMA.getGui().getConsole().addConsoleListener(console);
		super.onOpen(socket, handshake);
	}

	/**
	 * On close.
	 *
	 * @param conn
	 *            the conn
	 * @param code
	 *            the code
	 * @param reason
	 *            the reason
	 * @param remote
	 *            the remote
	 */
	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		super.onClose(conn, code, reason, remote);
		GAMA.getGui().getConsole().removeConsoleListener(console);
	}

	/**
	 * On error.
	 *
	 * @param conn
	 *            the conn
	 * @param ex
	 *            the ex
	 */
	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	/**
	 * On start.
	 */
	@Override
	public void onStart() {
		super.onStart();
		GAMA.addExperimentStateListener(this);
	}

	/**
	 * Stop.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
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
	public IExperimentSpecies getExperiment(final String socket, final String expid) {
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
	public void updateStateTo(final IExperimentSpecies experiment, final State state) {
		if (state != currentState) {
			currentState = state;
			WebSocket ws = currentServerConfig.socket();
			if (ws == null || ws.isClosed()) return;
			ws.send(GAMA.getJsonEncoder()
					.valueOf(new GamaServerMessage(MessageType.SimulationStatus, state.name(), "0")).toString());
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
	public IServerConfiguration obtainGuiServerConfiguration() {
		return currentServerConfig;
	}

	/**
	 * Adds the experiment.
	 *
	 * @param socketId
	 *            the socket id
	 * @param experimentId
	 *            the experiment id
	 * @param plan
	 *            the plan
	 */
	@Override
	public void addExperiment(final String socketId, final String experimentId, final IExperimentSpecies plan) {}

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
	public IExperimentSpecies retrieveExperimentPlan(final WebSocket socket, final Map<String, Object> map)
			throws CommandException {
		IExperimentSpecies plan = GAMA.getExperiment();
		if (plan == null || plan.getAgent() == null || plan.getAgent().dead() || plan.getCurrentSimulation() == null)
			throw new CommandException(new CommandResponse(MessageType.UnableToExecuteRequest,
					"Unable to find the experiment or simulation", map, false));
		return plan;
	}

}
