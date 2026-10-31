/*******************************************************************************************************
 *
 * GamaWebSocketServer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import static gama.api.utils.server.ISocketCommand.EXP_ID;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.ListenerList;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.exceptions.CommandException;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.types.map.IMap;
import gama.api.ui.IConsoleListener;
import gama.api.utils.json.IJsonObject;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;

/**
 * The Class GamaWebSocketServer.
 */
public abstract class GamaWebSocketServer extends WebSocketServer implements IGamaServer {

	/** The gui. */
	public static final IServerConfiguration GUI =
			new GamaServerExperimentConfiguration(null, "", true, true, true, true);

	/** The Constant SOCKET_ID. */
	static final String SOCKET_ID = "socket_id";

	/** The cmd helper. */
	protected final CommandExecutor cmdHelper = new CommandExecutor();

	/** The can ping. false if pingInterval is negative */
	public final boolean canPing;

	/** The ping interval. the time interval between two ping requests in ms */
	public final int pingInterval;

	/** The ping timers. map of all connected clients and their associated timers running ping requests */
	protected final Map<WebSocket, Timer> pingTimers = new HashMap<>();

	/** The console. */
	protected final IConsoleListener console = new GamaServerConsoleListener();

	/** The listeners. */
	protected final ListenerList<Listener> listeners = new ListenerList<>(ListenerList.IDENTITY);

	private final GamaServerExchangeLog exchangeLog;
	private final GamaServerConsole serverConsole;
	private final PrintStream previousErrorStream = System.err;
	private PrintStream serverErrorStream;

	// Is thread local because the decoder is stateful
	// TODO: It may be better to have one decoder per open connection (needs to be benchmarked) but good enough for now
	protected static final ThreadLocal<CharsetDecoder> DECODER = ThreadLocal.withInitial(() ->
	    StandardCharsets.UTF_8.newDecoder()
	        .onMalformedInput(CodingErrorAction.REPLACE)
	        .onUnmappableCharacter(CodingErrorAction.REPLACE)
	);
	
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
	protected GamaWebSocketServer(final int port, final int interval, final boolean noDelay) {
		this(port, interval, noDelay, new GamaServerConsole());
	}

	protected GamaWebSocketServer(final int port, final int interval, final boolean noDelay,
			final GamaServerConsole serverConsole) {
		this(port, interval, noDelay, new GamaServerExchangeLog(serverConsole), serverConsole);
	}

	private GamaWebSocketServer(final int port, final int interval, final boolean noDelay,
			final GamaServerExchangeLog log, final GamaServerConsole serverConsole) {
		super(new InetSocketAddress(port), List.of(new GamaServerLoggingDraft(log)));
		exchangeLog = log;
		this.serverConsole = serverConsole;
		// Should solve the problem with the address being still used after relaunching
		this.setReuseAddr(true);
		this.setTcpNoDelay(noDelay);
		canPing = interval >= 0;
		pingInterval = interval;
		if (interval == 0 || interval < -1) {
			throw new IllegalArgumentException("Server ping interval must be positive or -1");
		}
		configureErrorStream();
	}

	@Override
	public void start() {
		serverConsole.setState(GamaServerConsole.State.STARTING, getPort());
		try {
			super.start();
		} catch (final RuntimeException e) {
			serverConsole.error("Unable to start server", e);
			serverConsole.setState(GamaServerConsole.State.FAILED, getPort());
			restoreErrorStream();
			throw e;
		}
	}

	/**
	 * Adds the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	@Override
	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener.
	 *
	 * @param listener
	 *            the listener
	 */
	@Override
	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Configure error stream so as to broadcast errors
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 oct. 2023
	 */
	private void configureErrorStream() {
		// error handling should not rely on this as we don't know when the error starts and ends,
		// making it hard to handle on the client side. This is a last resort option
		serverErrorStream = new PrintStream(previousErrorStream) {

			// This is actually probably never called, because printErrorStack uses print(String)
			@Override
			public void println(final String x) {
				super.println(x);
				broadcast(GAMA.getJsonEncoder().valueOf(new GamaServerMessage(MessageType.GamaServerError, x))
						.toString());
			}
		};
		System.setErr(serverErrorStream);
	}

	private void restoreErrorStream() {
		if (System.err == serverErrorStream) { System.setErr(previousErrorStream); }
	}

	/**
	 * Gets the socket id.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @return the socket id
	 * @date 3 nov. 2023
	 */
	public static String getSocketId(final WebSocket socket) {
		return String.valueOf(socket.hashCode());
	}

	@Override
	public void onStart() {
		exchangeLog.start(getPort());
		serverConsole.setState(GamaServerConsole.State.RUNNING, getPort());
		serverConsole.setExchangeLogging(exchangeLog.isEnabled(), exchangeLog.file());
		DEBUG.BANNER(BANNER_CATEGORY.GAMA, "Server started", "at port", "" + this.getPort());
	}

	public GamaServerConsole getServerConsole() { return serverConsole; }

	/** @return this run's exchange log file, or null if no file was created. */
	public Path getExchangeLogFile() { return exchangeLog.file(); }

	public boolean isExchangeLogging() { return exchangeLog.isEnabled(); }

	@Override
	public IJsonObject setExchangeLogging(final boolean enabled) throws IOException {
		try {
			final IJsonObject result = exchangeLog.setEnabled(enabled, getPort());
			serverConsole.setExchangeLogging(exchangeLog.isEnabled(), exchangeLog.file());
			return result;
		} catch (final IOException e) {
			serverConsole.error("Unable to change exchange logging", e);
			throw e;
		}
	}

	@Override
	public void stop(final int timeout, final String closeMessage) throws InterruptedException {
		serverConsole.setState(GamaServerConsole.State.STOPPING, getPort());
		try {
			super.stop(timeout, closeMessage);
			serverConsole.setState(GamaServerConsole.State.STOPPED, getPort());
		} catch (final InterruptedException e) {
			serverConsole.error("Interrupted while stopping server", e);
			serverConsole.setState(GamaServerConsole.State.FAILED, getPort());
			throw e;
		} finally {
			exchangeLog.close();
			restoreErrorStream();
		}
	}

	@Override
	public void onOpen(final WebSocket socket, final ClientHandshake handshake) {
		if (socket.getDraft() instanceof GamaServerLoggingDraft draft) { draft.bind(socket); }
		exchangeLog.connectionOpened(socket);
		serverConsole.connectionOpened(socket);
		socket.send(GAMA.getJsonEncoder()
				.valueOf(new GamaServerMessage(MessageType.ConnectionSuccessful, String.valueOf(socket.hashCode())))
				.toString());
		if (canPing) {
			var timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (socket.isOpen()) { socket.sendPing(); }
				}
			}, 0, pingInterval);
			pingTimers.put(socket, timer);
		}
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		exchangeLog.connectionClosed(conn, code, reason, remote);
		serverConsole.connectionClosed(conn, code, reason);
		var timer = pingTimers.remove(conn);
		if (timer != null) { timer.cancel(); }
		listeners.clear();
	}

	/**
	 * Extract param.
	 *
	 * @param socket
	 *            the socket
	 * @param message
	 *            the message
	 * @return the i map
	 */
	@SuppressWarnings ("unchecked")
	public ReceivedMessage extractParam(final WebSocket socket, final String message) {
		try {
			final Object o = GAMA.getJsonEncoder().parse(message).toGamlValue(GAMA.getRuntimeScope());
			ReceivedMessage m = o instanceof IMap map ? new ReceivedMessage(message, map)
					: new ReceivedMessage(message, Map.of(IKeyword.CONTENTS, o));
			m.put("server", this);
			return m;
		} catch (Exception e1) {
			serverConsole.error("Malformed client request", e1);
			DEBUG.OUT(e1.toString());
			socket.send(
					GAMA.getJsonEncoder().valueOf(new GamaServerMessage(MessageType.MalformedRequest, e1)).toString());
			return null;
		}
	}

	@Override
	public void onMessage(final WebSocket socket, final String message) {
		serverConsole.request(socket, message);
		try {
			ReceivedMessage received = extractParam(socket, message);
			if (received == null) return;
			// Gives listeners a chance to process the message. If one returns false, we abort the processing. See #438
			for (Listener listener : listeners) { if (!listener.process(received)) return; }
			if (ISocketCommand.SERVER_LOG.equals(received.get(IKeyword.TYPE))) {
				cmdHelper.process(this, socket, received);
				return;
			}
			if (ISocketCommand.GET_PREFERENCES.equals(received.get(IKeyword.TYPE))
					|| ISocketCommand.SET_PREFERENCE.equals(received.get(IKeyword.TYPE))) {
				cmdHelper.processAsync(this, socket, received);
				return;
			}
			final String expId = received.getOrDefault(EXP_ID, "").toString();
			final String socketId = received.getOrDefault(SOCKET_ID, getSocketId(socket)).toString();
			IExperimentSpecies exp = getExperiment(socketId, expId);
			if (exp != null) {
				processInSyncWithExperiment(socket, received, exp);
			} else {
				cmdHelper.processAsync(this, socket, received);
			}
		} catch (Exception e1) {
			serverConsole.error("Unable to process client request", e1);
			DEBUG.OUT(e1);
			socket.send(GAMA.getJsonEncoder().valueOf(new GamaServerMessage(MessageType.GamaServerError, e1)).toString());
		}
	}
	
	@Override
	public void onMessage(WebSocket socket, ByteBuffer message) {
		try {
			onMessage(socket, DECODER.get().decode(message).toString());
		} catch (CharacterCodingException e) {
			serverConsole.error("Unable to decode client request", e);
			socket.send(GAMA.getJsonEncoder().valueOf(new GamaServerMessage(MessageType.GamaServerError, e)).toString());
		}
	}

	/**
	 * Processes model access between experiment cycles. Controller commands run separately: a synchronous step or
	 * reload must not wait for the execution thread from inside that same thread.
	 *
	 * @param socket
	 *            the socket
	 * @param received
	 *            the received
	 * @param exp
	 *            the exp
	 */
	private void processInSyncWithExperiment(final WebSocket socket, final ReceivedMessage received,
			final IExperimentSpecies exp) {
		IExperimentAgent agent = exp.getAgent();
		final boolean controlsExperiment = switch (received.get(IKeyword.TYPE).toString()) {
			case ISocketCommand.LOAD, ISocketCommand.PLAY, ISocketCommand.PAUSE, ISocketCommand.STEP,
					ISocketCommand.BACK, ISocketCommand.STEPBACK, ISocketCommand.STOP, ISocketCommand.RELOAD,
					ISocketCommand.EXIT -> true;
			default -> false;
		};
		if (agent == null) {
			cmdHelper.processAsync(this, socket, received);
		} else if (exp.getController().isPaused()) {
			if (controlsExperiment) {
				cmdHelper.processAsync(this, socket, received);
			} else {
				cmdHelper.processAsync(this, socket, received, agent);
			}
		} else {
			// In order to sync the command with the experiment cycles
			agent.postOneShotAction(scope1 -> {
				if (controlsExperiment) {
					cmdHelper.processAsync(this, socket, received);
				} else {
					cmdHelper.process(this, socket, received);
				}
				return null;
			});
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		serverConsole.error(conn == null ? "Server error" : "Client " + getSocketId(conn) + " error", ex);
		if (conn == null && (serverConsole.snapshot().state() == GamaServerConsole.State.STARTING
				|| serverConsole.snapshot().state() == GamaServerConsole.State.RUNNING)) {
			serverConsole.setState(GamaServerConsole.State.FAILED, getPort());
			restoreErrorStream();
		}
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public IServerConfiguration obtainNullServerConfiguration() {
		return NULL;
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
	public abstract IExperimentSpecies getExperiment(final String socket, final String expid);

	/**
	 * Execute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executionThread
	 *            the t
	 * @date 15 oct. 2023
	 */
	@Override
	public abstract void execute(final Runnable command);

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
	@Override
	public abstract void addExperiment(final String socketId, final String experimentId, final IExperimentSpecies plan);

	/**
	 * Obtain gui server configuration.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	@Override
	public abstract IServerConfiguration obtainGuiServerConfiguration();

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
	public abstract IExperimentSpecies retrieveExperimentPlan(final WebSocket socket, final Map<String, Object> map)
			throws CommandException;

}
