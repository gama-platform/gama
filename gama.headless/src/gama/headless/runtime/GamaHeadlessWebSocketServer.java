/*******************************************************************************************************
 *
 * GamaHeadlessWebSocketServer.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.runtime;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.server.SSLParametersWebSocketServerFactory;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.runtime.server.CommandResponse;
import gama.core.runtime.server.GamaServerExperimentConfiguration;
import gama.core.runtime.server.GamaWebSocketServer;
import gama.core.runtime.server.IGamaServer;
import gama.core.runtime.server.ISocketCommand;
import gama.core.runtime.server.ISocketCommand.CommandException;
import gama.core.runtime.server.MessageType;
import gama.core.util.IMap;

/**
 * The Class GamaWebSocketServer.
 */
public class GamaHeadlessWebSocketServer extends GamaWebSocketServer {

	/** The Constant TLS. */
	static final String TLS = "TLS";

	/** The Constant JKS. */
	static final String JKS = "JKS";

	/** The Constant SUN_X509. */
	static final String SUN_X509 = "SunX509";

	/** The executor. */
	private final ThreadPoolExecutor executor;

	/** The experiments. Only used in the headless version */
	private final Map<String, Map<String, IExperimentPlan>> launchedExperiments = new ConcurrentHashMap<>();

	/**
	 * Start for headless with SSL security on
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to which to listen to
	 * @param runner
	 *            the runner a ThreadPoolExecutor to launch concurrent experiments
	 * @param ssl
	 *            the ssl whether to use ssl or not
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
	public static IGamaServer startForSecureHeadless(final int port, final ThreadPoolExecutor runner, final boolean ssl,
			final String jksPath, final String spwd, final String kpwd, final int pingInterval, final boolean noDelay) {
		GamaHeadlessWebSocketServer server =
				new GamaHeadlessWebSocketServer(port, runner, ssl, jksPath, spwd, kpwd, pingInterval, noDelay);
		try {
			server.setReuseAddr(true);
			server.start();
			return server;
		} finally {
			server.infiniteLoop();
		}
	}

	/**
	 * Start for headless without SSL
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to listen to
	 * @param runner
	 *            the runner
	 * @param pingInterval
	 *            the ping interval
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaHeadlessWebSocketServer startForHeadless(final int port, final ThreadPoolExecutor runner,
			final int pingInterval, final boolean noDelay) {
		GamaHeadlessWebSocketServer server =
				new GamaHeadlessWebSocketServer(port, runner, false, "", "", "", pingInterval, noDelay);

		try {

			server.setReuseAddr(true);
			server.start();
			return server;

		} finally {
			server.infiniteLoop();
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
	private GamaHeadlessWebSocketServer(final int port, final ThreadPoolExecutor runner, final boolean ssl,
			final String jksPath, final String spwd, final String kpwd, final int interval, final boolean noDelay) {
		super(port, interval, noDelay);
		executor = runner;
		if (ssl) { configureWebSocketFactoryWithSSL(jksPath, spwd, kpwd); }
	}

	/**
	 * Configure web socket factory.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param keyStore
	 *            the jks path
	 * @param spwd
	 *            the spwd
	 * @param kpwd
	 *            the kpwd
	 * @date 16 oct. 2023
	 */
	private void configureWebSocketFactoryWithSSL(final String keyStore, final String storePassword,
			final String keyPassword) {
		// load up the key store
		KeyStore ks;
		try (InputStream fis = Files.newInputStream(new File(keyStore).toPath())) {
			ks = KeyStore.getInstance(JKS);
			ks.load(fis, storePassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(SUN_X509);
			kmf.init(ks, keyPassword.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(SUN_X509);
			tmf.init(ks);
			SSLContext sslContext = SSLContext.getInstance(TLS);
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLParameters sslParameters = new SSLParameters();
			sslParameters.setNeedClientAuth(false);
			this.setWebSocketFactory(new SSLParametersWebSocketServerFactory(sslContext, sslParameters));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Infinite loop.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 oct. 2023
	 */
	public void infiniteLoop() {
		try {
			// empty loop to keep alive the server and catch exceptions
			while (true) { Thread.sleep(999999); }

		} catch (Exception ex) {
			ex.printStackTrace(); // will be broadcasted to every client
		}
	}

	/**
	 * On close.
	 *
	 * @param socket
	 *            the socket
	 * @param code
	 *            the code
	 * @param reason
	 *            the reason
	 * @param remote
	 *            the remote
	 */
	@Override
	public void onClose(final WebSocket socket, final int code, final String reason, final boolean remote) {
		super.onClose(socket, code, reason, remote);
		String socketId = getSocketId(socket);
		if (getLaunchedExperiments().get(socketId) != null) {
			for (IExperimentPlan e : getLaunchedExperiments().get(socketId).values()) {
				e.getController().processPause(true);
				e.getController().close();
				e.getController().dispose();
			}
			getLaunchedExperiments().get(socketId).clear();
		}
	}

	/**
	 * Gets the all experiments.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the all experiments
	 * @date 15 oct. 2023
	 */
	public Map<String, Map<String, IExperimentPlan>> getAllExperiments() { return launchedExperiments; }

	/**
	 * Gets the experiments of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @return the experiments of
	 * @date 15 oct. 2023
	 */
	public Map<String, IExperimentPlan> getExperimentsOf(final String socket) {
		return launchedExperiments.get(socket);
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
		if (launchedExperiments.get(socket) == null) return null;
		return launchedExperiments.get(socket).get(expid);
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
		if (executor == null) {
			command.run();
		} else {
			executor.execute(command);
		}
	}

	/**
	 * Gets the launched experiments.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the launched experiments
	 * @date 2 nov. 2023
	 */
	public Map<String, Map<String, IExperimentPlan>> getLaunchedExperiments() { return launchedExperiments; }

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
	public void addExperiment(final String socketId, final String experimentId, final IExperimentPlan plan) {
		launchedExperiments.putIfAbsent(socketId, new ConcurrentHashMap<>());
		launchedExperiments.get(socketId).put(experimentId, plan);
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
		return GamaServerExperimentConfiguration.NULL;
	}

	/**
	 * Gets the experiment plan from the
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @param map
	 *            the map
	 * @param error
	 *            the error
	 * @return the experiment plan
	 * @date 5 déc. 2023
	 */
	@Override
	public IExperimentPlan retrieveExperimentPlan(final WebSocket socket, final IMap<String, Object> map)
			throws CommandException {
		final String exp_id = map.get(ISocketCommand.EXP_ID) != null ? map.get(ISocketCommand.EXP_ID).toString() : "";
		final String socket_id = map.get(ISocketCommand.SOCKET_ID) != null
				? map.get(ISocketCommand.SOCKET_ID).toString() : "" + socket.hashCode();
		if ("".equals(exp_id)) throw new CommandException(new CommandResponse(MessageType.MalformedRequest,
				"For " + map.get("type") + ", mandatory parameter is: " + ISocketCommand.EXP_ID, map, false));
		IExperimentPlan plan = getExperiment(socket_id, exp_id);
		if (plan == null || plan.getAgent() == null || plan.getAgent().dead())
			throw new CommandException(new CommandResponse(MessageType.UnableToExecuteRequest,
					"Unable to find the experiment or simulation", map, false));
		return plan;
	}

}
