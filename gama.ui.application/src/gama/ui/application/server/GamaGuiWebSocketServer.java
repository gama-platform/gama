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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import gama.api.GAMA;
import gama.api.exceptions.CommandException;
import gama.api.kernel.simulation.IExperimentStateListener;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceBeforeChangeListener;
import gama.api.utils.server.CommandResponse;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.GamaServerConsole;
import gama.api.utils.server.GamaWebSocketServer;
import gama.api.utils.server.IServerConfiguration;
import gama.api.utils.server.MessageType;

/**
 * The Class GamaWebSocketServer.
 */
public class GamaGuiWebSocketServer extends GamaWebSocketServer implements IExperimentStateListener {

	private static final GamaServerConsole SERVER_CONSOLE = new GamaServerConsole();
	private static final ExecutorService SERVER_CONTROL = Executors.newSingleThreadExecutor(task -> {
		final Thread thread = new Thread(task, "GAMA GUI server controls");
		thread.setDaemon(true);
		return thread;
	});

	private static final IPreferenceBeforeChangeListener<Boolean> LOGGING_PREFERENCE_LISTENER = enabled -> {
		if (!(GAMA.getServer() instanceof GamaGuiWebSocketServer server)
				|| SERVER_CONSOLE.snapshot().state() != GamaServerConsole.State.RUNNING) return true;
		if (server.isExchangeLogging() == enabled) return true;
		try {
			server.setExchangeLogging(enabled);
			return true;
		} catch (final IOException e) {
			GAMA.getGui().getDialogFactory().error("Unable to change server exchange logging: " + e.getMessage());
			return false;
		}
	};

	public static GamaServerConsole getGuiServerConsole() { return SERVER_CONSOLE; }

	/**
	 * Start server.
	 */
	public static void startGuiServer() {
		if (GAMA.isInHeadLessMode()) return;
		GamaPreferences.Runtime.CORE_SERVER_LOG.addChangeListener(LOGGING_PREFERENCE_LISTENER);
		if (GamaPreferences.Runtime.CORE_SERVER_MODE.getValue()) { startServer(); }
		GamaPreferences.Runtime.CORE_SERVER_MODE.onChange(enabled -> {
			if (enabled) {
				startServer();
			} else {
				stopServer();
			}
		});
		GamaPreferences.Runtime.CORE_SERVER_PORT.onChange(port -> control(() -> {
			if (GAMA.getServer() instanceof GamaGuiWebSocketServer server
					&& server.getPort() != GamaPreferences.Runtime.CORE_SERVER_PORT.getValue()) {
				GAMA.setServer(null);
				createGuiServer();
			}
		}));
	}

	public static CompletableFuture<Void> startServer() {
		return control(() -> {
			final GamaServerConsole.State state = SERVER_CONSOLE.snapshot().state();
			if (GAMA.getServer() instanceof GamaGuiWebSocketServer
					&& (state == GamaServerConsole.State.RUNNING || state == GamaServerConsole.State.STARTING)) return;
			GAMA.setServer(null);
			createGuiServer();
		});
	}

	public static CompletableFuture<Void> stopServer() {
		return control(() -> GAMA.setServer(null));
	}

	public static CompletableFuture<Void> restartServer() {
		return control(() -> {
			GAMA.setServer(null);
			createGuiServer();
		});
	}

	/** Applies the toolbar's selected port and starts or restarts the server in one serialized operation. */
	public static CompletableFuture<Void> restartServer(final int port) {
		return control(() -> {
			if (port < 1 || port > 65535) { throw new IllegalArgumentException("Server port must be between 1 and 65535"); }
			GamaPreferences.setNewPreferences(Map.of(GamaPreferences.Runtime.CORE_SERVER_PORT.getKey(), port));
			GAMA.setServer(null);
			createGuiServer();
		});
	}

	public static CompletableFuture<Void> setServerLogging(final boolean enabled) {
		return control(() -> {
			if (GAMA.getServer() instanceof GamaGuiWebSocketServer server
					&& SERVER_CONSOLE.snapshot().state() == GamaServerConsole.State.RUNNING) {
				try {
					server.setExchangeLogging(enabled);
				} catch (final IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		});
	}

	private static CompletableFuture<Void> control(final Runnable action) {
		return CompletableFuture.runAsync(() -> {
			try {
				action.run();
			} catch (final RuntimeException e) {
				SERVER_CONSOLE.error("Unable to control GUI server", e);
				if (!(GAMA.getServer() instanceof GamaGuiWebSocketServer)
						|| SERVER_CONSOLE.snapshot().state() != GamaServerConsole.State.RUNNING) {
					SERVER_CONSOLE.setState(GamaServerConsole.State.FAILED,
							GamaPreferences.Runtime.CORE_SERVER_PORT.getValue());
				}
				throw e;
			}
		}, SERVER_CONTROL);
	}

	/**
	 * Creates the gui server.
	 */
	private static void createGuiServer() {
		final int port = GamaPreferences.Runtime.CORE_SERVER_PORT.getValue();
		final int ping = GamaPreferences.Runtime.CORE_SERVER_PING.getValue();
		final boolean noDelay = GamaPreferences.Runtime.CORE_SERVER_NO_DELAY.getValue();
		if (port < 1 || port > 65535) { throw new IllegalArgumentException("Server port must be between 1 and 65535"); }
		final GamaGuiWebSocketServer server = new GamaGuiWebSocketServer(port, ping, noDelay);
		GAMA.setServer(server);
		server.start();
	}

	/** The current server config. */
	private IServerConfiguration currentServerConfig = GamaWebSocketServer.GUI;

	/** The current state. */
	private volatile State currentState = State.NONE;

	private WeakReference<IExperimentSpecies> currentStateExperiment = new WeakReference<>(null);

	private WebSocket currentStateSocket;

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
		final GamaGuiWebSocketServer server = new GamaGuiWebSocketServer(port, pingInterval, noDelay);
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
	private GamaGuiWebSocketServer(final int port, final int interval, final boolean noDelay) {
		super(port, interval, noDelay, SERVER_CONSOLE);
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
		super.onError(conn, ex);
	}

	/**
	 * On start.
	 */
	@Override
	public void onStart() {
		super.onStart();
		GAMA.getGui().getConsole().addConsoleListener(console);
		GAMA.addExperimentStateListener(this);
	}

	/**
	 * Stop.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Override
	public void stop(final int timeout, final String closeMessage) throws InterruptedException {
		try {
			super.stop(timeout, closeMessage);
		} finally {
			GAMA.getGui().getConsole().removeConsoleListener(console);
			GAMA.removeExperimentStateListener(this);
		}
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
	public synchronized void updateStateTo(final IExperimentSpecies experiment, final State state) {
		final var agent = experiment == null ? null : experiment.getAgent();
		final var scope = agent == null ? null : agent.getScope();
		final IServerConfiguration config = scope == null ? currentServerConfig : scope.getServerConfiguration();
		final WebSocket ws = config == null ? null : config.socket();
		if (ws == null || ws.isClosed()) return;
		// A cycle or controller command may finish after disposal has already announced NONE.
		final boolean closing = experiment != null && experiment.getController().isDisposing();
		final IExperimentSpecies previousExperiment = currentStateExperiment.get();
		if (closing && previousExperiment != null && experiment != previousExperiment) return;
		final State effectiveState = closing ? State.NONE : state;
		if (effectiveState == currentState && experiment == previousExperiment && ws == currentStateSocket) return;
		ws.send(GAMA.getJsonEncoder()
				.valueOf(new GamaServerMessage(MessageType.SimulationStatus, effectiveState.name(),
						config.expId().isBlank() ? "0" : config.expId())).toString());
		currentState = effectiveState;
		currentStateExperiment = new WeakReference<>(experiment);
		currentStateSocket = ws;
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
