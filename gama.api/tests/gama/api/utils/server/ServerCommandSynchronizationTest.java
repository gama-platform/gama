package gama.api.utils.server;

import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.java_websocket.WebSocket;
import org.java_websocket.enums.ReadyState;

import gama.api.GAMA;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.IExecutable;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/** Standalone dispatch regression checks. Run with the API bundle's runtime classpath. */
public final class ServerCommandSynchronizationTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static void await(final CountDownLatch latch) {
		try {
			check(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for command execution");
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError(e);
		}
	}

	private static final class Server extends GamaWebSocketServer {

		final BlockingQueue<IExecutable> actions = new LinkedBlockingQueue<>();
		boolean paused;
		boolean loaded = true;

		final IExperimentController controller = proxy(IExperimentController.class, (p, method, args) -> {
			if ("isPaused".equals(method.getName())) return paused;
			throw new AssertionError("Unexpected controller call: " + method);
		});
		final IExperimentAgent agent = proxy(IExperimentAgent.class, (p, method, args) -> {
			if ("postOneShotAction".equals(method.getName())) {
				actions.add((IExecutable) args[0]);
				return args[0];
			}
			throw new AssertionError("Unexpected agent call: " + method);
		});
		final IExperimentSpecies experiment = proxy(IExperimentSpecies.class, (p, method, args) ->
				switch (method.getName()) {
					case "getAgent" -> agent;
					case "getController" -> controller;
					default -> throw new AssertionError("Unexpected experiment call: " + method);
				});

		Server() {
			super(0, -1, true);
		}

		@Override
		public ReceivedMessage extractParam(final WebSocket socket, final String message) {
			return new ReceivedMessage(message, Map.of("type", message));
		}

		@Override
		public IExperimentSpecies getExperiment(final String socket, final String id) {
			return loaded ? experiment : null;
		}

		@Override
		public void execute(final Runnable command) {
			command.run();
		}

		@Override
		public void addExperiment(final String socket, final String id, final IExperimentSpecies plan) {
			throw new AssertionError("Unexpected experiment registration");
		}

		@Override
		public IServerConfiguration obtainGuiServerConfiguration() {
			return GUI;
		}

		@Override
		public IExperimentSpecies retrieveExperimentPlan(final WebSocket socket, final Map<String, Object> map) {
			return experiment;
		}
	}

	private static void checkBetweenCycles(final Server server, final WebSocket socket, final String type)
			throws Exception {
		final CountDownLatch entered = new CountDownLatch(1);
		final CountDownLatch release = new CountDownLatch(1);
		final AtomicReference<Thread> commandThread = new AtomicReference<>();
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		server.cmdHelper.commands.put(type, (s, ws, message) -> {
			commandThread.set(Thread.currentThread());
			entered.countDown();
			await(release);
			return new CommandResponse(MessageType.CommandExecutedSuccessfully, type, message, false);
		});
		server.onMessage(socket, type);
		check(entered.getCount() == 1, "A running experiment must queue model access: " + type);
		final IExecutable action = server.actions.poll(5, TimeUnit.SECONDS);
		check(action != null, "Missing between-cycle action: " + type);
		final Thread simulation = new Thread(() -> {
			try {
				action.executeOn(null);
				check(release.getCount() == 0, "The next cycle must wait for the command: " + type);
			} catch (final Throwable t) {
				failure.set(t);
			}
		}, "test simulation");
		try {
			simulation.start();
			await(entered);
			check(commandThread.get() == simulation, "Model access escaped the simulation thread: " + type);
		} finally {
			release.countDown();
			simulation.join(5000);
		}
		check(!simulation.isAlive(), "Simulation thread did not finish");
		if (failure.get() != null) throw new AssertionError(failure.get());
	}

	private static void checkOffWebSocketThread(final Server server, final WebSocket socket, final String type)
			throws Exception {
		final CountDownLatch entered = new CountDownLatch(1);
		final CountDownLatch release = new CountDownLatch(1);
		final CountDownLatch finished = new CountDownLatch(1);
		final AtomicReference<Thread> commandThread = new AtomicReference<>();
		server.cmdHelper.commands.put(type, (s, ws, message) -> {
			try {
				commandThread.set(Thread.currentThread());
				entered.countDown();
				await(release);
				return null;
			} finally {
				finished.countDown();
			}
		});
		try {
			server.onMessage(socket, type);
			if (server.loaded && !server.paused) {
				final IExecutable action = server.actions.poll(5, TimeUnit.SECONDS);
				check(action != null, "Running experiments must dispatch at the cycle boundary: " + type);
				action.executeOn(null);
			}
			await(entered);
			check(commandThread.get() != Thread.currentThread(), "The WebSocket thread must remain free: " + type);
			check(server.actions.isEmpty(), "The controller command must be dispatched only once: " + type);
		} finally {
			release.countDown();
			await(finished);
		}
	}

	public static void main(final String[] args) throws Exception {
		final PrintStream stderr = System.err;
		final IJson previousJson = GAMA.getJsonEncoder();
		final BlockingQueue<GamaServerMessage> responses = new LinkedBlockingQueue<>();
		final BlockingQueue<String> sent = new LinkedBlockingQueue<>();
		GAMA.setJsonEncoder(proxy(IJson.class, (p, method, arguments) -> {
			if ("valueOf".equals(method.getName())) {
				final GamaServerMessage response = (GamaServerMessage) arguments[0];
				responses.add(response);
				return proxy(IJsonValue.class, (v, m, a) -> {
					if ("toString".equals(m.getName())) return response.type.name();
					throw new AssertionError("Unexpected JSON value call: " + m);
				});
			}
			throw new AssertionError("Unexpected JSON call: " + method);
		}));
		final WebSocket socket = proxy(WebSocket.class, (p, method, arguments) -> switch (method.getName()) {
			case "hashCode" -> 1;
			case "getReadyState" -> ReadyState.OPEN;
			case "send" -> {
				sent.add((String) arguments[0]);
				yield null;
			}
			default -> throw new AssertionError("Unexpected WebSocket call: " + method);
		});
		try {
			final Server server = new Server();
			for (final String type : new String[] { ISocketCommand.EXPRESSION, ISocketCommand.EVALUATE,
					ISocketCommand.ASK, "contributed_command" }) {
				checkBetweenCycles(server, socket, type);
				final GamaServerMessage response = responses.poll(5, TimeUnit.SECONDS);
				check(response != null && response.type == MessageType.CommandExecutedSuccessfully
						&& type.equals(response.content), "Model access must send its command response: " + type);
				check(MessageType.CommandExecutedSuccessfully.name().equals(sent.poll(5, TimeUnit.SECONDS)),
						"The response must reach the client before the next cycle");
			}
			for (final String type : new String[] { ISocketCommand.LOAD, ISocketCommand.PLAY, ISocketCommand.PAUSE,
					ISocketCommand.STEP, ISocketCommand.BACK, ISocketCommand.STEPBACK, ISocketCommand.STOP,
					ISocketCommand.RELOAD, ISocketCommand.EXIT }) {
				checkOffWebSocketThread(server, socket, type);
			}
			server.paused = true;
			checkOffWebSocketThread(server, socket, ISocketCommand.EXPRESSION);
			server.loaded = false;
			checkOffWebSocketThread(server, socket, ISocketCommand.DESCRIBE);

			server.cmdHelper.commands.put("failing_command", (s, ws, message) -> {
				throw new IllegalStateException("Expected failure");
			});
			for (final String type : new String[] { "failing_command", "unknown_command" }) {
				server.onMessage(socket, type);
				final GamaServerMessage response = responses.poll(5, TimeUnit.SECONDS);
				check(response != null && response.type == MessageType.GamaServerError,
						"Command failures must produce an error response");
				check(MessageType.GamaServerError.name().equals(sent.poll(5, TimeUnit.SECONDS)),
						"Error responses must reach the client");
			}
			server.cmdHelper.processAsync(server, socket, new ReceivedMessage("{}", Map.of()));
			final GamaServerMessage malformed = responses.poll(5, TimeUnit.SECONDS);
			check(malformed != null && malformed.type == MessageType.GamaServerError,
					"A missing command type must also receive an error response");
			check(MessageType.GamaServerError.name().equals(sent.poll(5, TimeUnit.SECONDS)),
					"Malformed command responses must reach the client");
			System.out.println("ServerCommandSynchronizationTest passed");
		} finally {
			System.setErr(stderr);
			GAMA.setJsonEncoder(previousJson);
		}
	}
}
