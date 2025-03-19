/*******************************************************************************************************
 *
 * GamaWebSocketServer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import static gama.core.runtime.server.ISocketCommand.EXP_ID;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import gama.core.common.interfaces.IConsoleListener;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.runtime.GAMA;
import gama.core.runtime.server.ISocketCommand.CommandException;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.dev.DEBUG;

/**
 * The Class GamaWebSocketServer.
 */
public abstract class GamaWebSocketServer extends WebSocketServer {

	/** The Constant SOCKET_ID. */
	static final String SOCKET_ID = "socket_id";

	/** The Constant DEFAULT_PING_INTERVAL. */
	public static final int DEFAULT_PING_INTERVAL = 10000;

	/** The cmd helper. */
	protected final CommandExecutor cmdHelper = new CommandExecutor(this);

	/** The can ping. false if pingInterval is negative */
	public final boolean canPing;

	/** The ping interval. the time interval between two ping requests in ms */
	public final int pingInterval;

	/** The ping timers. map of all connected clients and their associated timers running ping requests */
	protected final Map<WebSocket, Timer> pingTimers = new HashMap<>();

	/** The json err. */
	protected Json jsonErr = Json.getNew();

	/** The console. */
	protected final IConsoleListener console = new GamaServerConsoleListener();

	/**
	 * The listener interface for processing messages received by the server.
	 *
	 * @see IServerEvent
	 */
	public interface IServerListener {

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

	/** The listeners. */
	protected final List<IServerListener> listeners = new ArrayList<>();

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
	protected GamaWebSocketServer(final int port, final int interval) {
		super(new InetSocketAddress(port));
		// Should solve the problem with the address being still used after relaunching
		this.setReuseAddr(true);
		canPing = interval >= 0;
		pingInterval = interval;
		configureErrorStream();
	}

	/**
	 * Configure error stream so as to broadcast errors
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 oct. 2023
	 */
	private void configureErrorStream() {
		PrintStream errorStream = new PrintStream(System.err) {

			@Override
			public void println(final String x) {
				super.println(x);
				broadcast(jsonErr.valueOf(new GamaServerMessage(MessageType.GamaServerError, x)).toString());
			}
		};
		System.setErr(errorStream);
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
		DEBUG.BANNER("GAMA", "Server started", "at port", "" + this.getPort());
	}

	@Override
	public void onOpen(final WebSocket socket, final ClientHandshake handshake) {
		socket.send(Json.getNew()
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
		var timer = pingTimers.remove(conn);
		if (timer != null) { timer.cancel(); }
		DEBUG.OUT(conn + " has left the room!");
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
		ReceivedMessage received = null;
		try {
			final Object o = Json.getNew().parse(message).toGamlValue(GAMA.getRuntimeScope());
			if (o instanceof IMap map) {
				received = new ReceivedMessage(map);
			} else {
				received = new ReceivedMessage();
				received.put(IKeyword.CONTENTS, o);
			}
			received.put("server", this);
		} catch (Exception e1) {
			DEBUG.OUT(e1.toString());
			socket.send(jsonErr.valueOf(new GamaServerMessage(MessageType.MalformedRequest, e1)).toString());
		}
		return received;
	}

	@Override
	public void onMessage(final WebSocket socket, final String message) {
		try {
			IMap<String, Object> map = extractParam(socket, message);
			final String expId = map.getOrDefault(EXP_ID, "").toString();
			final String socketId = map.getOrDefault(SOCKET_ID, getSocketId(socket)).toString();
			IExperimentPlan exp = getExperiment(socketId, expId);
			if (exp != null) {
				// In order to sync the command with the experiment cycles
				ExperimentAgent agent = exp.getAgent();
				if (agent != null && !exp.getController().isPaused()) {
					agent.postOneShotAction(scope1 -> {
						cmdHelper.process(socket, map);
						return null;
					});
					return;
				}
			}
			cmdHelper.process(socket, map);

		} catch (Exception e1) {
			DEBUG.OUT(e1);
			socket.send(jsonErr.valueOf(new GamaServerMessage(MessageType.GamaServerError, e1)).toString());

		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
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
	public abstract IExperimentPlan getExperiment(final String socket, final String expid);

	/**
	 * Execute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executionThread
	 *            the t
	 * @date 15 oct. 2023
	 */
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
	public abstract void addExperiment(final String socketId, final String experimentId, final IExperimentPlan plan);

	/**
	 * Obtain gui server configuration.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	public abstract GamaServerExperimentConfiguration obtainGuiServerConfiguration();

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
	public abstract IExperimentPlan retrieveExperimentPlan(final WebSocket socket, final IMap<String, Object> map)
			throws CommandException;

}
