package gama.headless.server;

import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.java_websocket.WebSocket;

import gama.api.GAMA;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.simulation.IExperimentStateListener.State;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.server.GamaServerExperimentConfiguration;
import gama.api.utils.server.IServerConfiguration;
import gama.core.util.json.Json;
import gama.ui.application.server.GamaGuiWebSocketServer;

/** Standalone regression checks for #988, including an in-flight status racing with closure. */
public final class ServerLifecycleStateTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static void await(final CountDownLatch latch) throws InterruptedException {
		check(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for lifecycle notification");
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static final class Experiment {
		volatile boolean disposing;
		volatile boolean paused = true;
		volatile IServerConfiguration configuration;
		final IExperimentController controller = proxy(IExperimentController.class, (p, method, args) ->
				switch (method.getName()) {
					case "isDisposing" -> disposing;
					case "isPaused" -> paused;
					default -> throw new AssertionError("Unexpected controller call: " + method);
				});
		final IScope scope = proxy(IScope.class, (p, method, args) -> {
			if ("getServerConfiguration".equals(method.getName())) return configuration;
			throw new AssertionError("Unexpected scope call: " + method);
		});
		final IExperimentAgent agent = proxy(IExperimentAgent.class, (p, method, args) -> {
			if ("getScope".equals(method.getName())) return scope;
			throw new AssertionError("Unexpected agent call: " + method);
		});
		final IExperimentSpecies plan = proxy(IExperimentSpecies.class, (p, method, args) ->
				switch (method.getName()) {
					case "getAgent" -> agent;
					case "getController" -> controller;
					default -> throw new AssertionError("Unexpected experiment call: " + method);
				});

		Experiment(final WebSocket socket) {
			configuration = new GamaServerExperimentConfiguration(socket, "Basic", true, false, false, true);
		}
	}

	private static GamaGuiWebSocketServer server() throws Exception {
		final var constructor = GamaGuiWebSocketServer.class.getDeclaredConstructor(int.class, int.class, boolean.class);
		constructor.setAccessible(true);
		return constructor.newInstance(0, -1, true);
	}

	private static WebSocket socket(final List<String> messages) {
		return proxy(WebSocket.class, (p, method, args) -> switch (method.getName()) {
			case "isClosed" -> false;
			case "send" -> { messages.add((String) args[0]); yield null; }
			default -> throw new AssertionError("Unexpected socket call: " + method);
		});
	}

	private static String state(final String message) {
		return GAMA.getJsonEncoder().parse(message).asObject().get("content").asString();
	}

	private static void checkClosing() throws Exception {
		final var messages = new CopyOnWriteArrayList<String>();
		final var experiment = new Experiment(socket(messages));
		final var server = server();
		check(GAMA.getExperimentState(experiment.plan) == State.PAUSED, "Initial state must be paused");
		server.updateStateTo(experiment.plan, State.PAUSED);
		experiment.paused = false;
		check(GAMA.getExperimentState(experiment.plan) == State.RUNNING, "An active experiment must report running");
		server.updateStateTo(experiment.plan, State.RUNNING);
		experiment.disposing = true;
		for (final boolean paused : new boolean[] { true, false }) {
			experiment.paused = paused;
			check(GAMA.getExperimentState(experiment.plan) == State.NONE, "A closing experiment must report NONE");
		}
		server.updateStateTo(experiment.plan, State.NONE);
		for (final State stale : List.of(State.PAUSED, State.RUNNING, State.NOTREADY)) {
			server.updateStateTo(experiment.plan, stale);
		}
		check(messages.stream().map(ServerLifecycleStateTest::state).toList()
				.equals(List.of("PAUSED", "RUNNING", "NONE")), "Stale notifications resurrected a closed experiment");

		final var replacement = new Experiment(socket(messages));
		server.updateStateTo(replacement.plan, State.PAUSED);
		server.updateStateTo(experiment.plan, State.NONE);
		check(state(messages.getLast()).equals("PAUSED"), "Old disposal changed the replacement experiment's state");
		final int count = messages.size();
		replacement.configuration = replacement.configuration.withSocket(socket(messages));
		server.updateStateTo(replacement.plan, State.PAUSED);
		check(messages.size() == count + 1, "A new socket must receive its first status even if the state is unchanged");
		final var next = new Experiment(replacement.configuration.socket());
		server.updateStateTo(next.plan, State.PAUSED);
		check(messages.size() == count + 2, "A new experiment must receive its first status");
	}

	private static void checkConcurrentDelivery() throws Exception {
		final var messages = new CopyOnWriteArrayList<String>();
		final var entered = new CountDownLatch(1);
		final var release = new CountDownLatch(1);
		final var closeStarted = new CountDownLatch(1);
		final var failure = new AtomicReference<Throwable>();
		final WebSocket socket = proxy(WebSocket.class, (p, method, args) -> switch (method.getName()) {
			case "isClosed" -> false;
			case "send" -> {
				final String message = (String) args[0];
				if ("PAUSED".equals(state(message))) {
					entered.countDown();
					await(release);
				}
				messages.add(message);
				yield null;
			}
			default -> throw new AssertionError("Unexpected socket call: " + method);
		});
		final var server = server();
		final var experiment = new Experiment(socket);
		final Thread step = new Thread(() -> {
			try {
				server.updateStateTo(experiment.plan, State.PAUSED);
			} catch (Throwable e) { failure.set(e); }
		}, "status from finishing cycle");
		final Thread close = new Thread(() -> {
			try {
				experiment.disposing = true;
				closeStarted.countDown();
				server.updateStateTo(experiment.plan, State.NONE);
			} catch (Throwable e) { failure.set(e); }
		}, "closing experiment status");
		try {
			step.start();
			await(entered);
			close.start();
			await(closeStarted);
		} finally {
			release.countDown();
			step.join(5000);
			close.join(5000);
		}
		check(!step.isAlive() && !close.isAlive(), "State notification threads did not finish");
		if (failure.get() != null) throw new AssertionError(failure.get());
		check(messages.stream().map(ServerLifecycleStateTest::state).toList().equals(List.of("PAUSED", "NONE")),
				"Concurrent status delivery must end in NONE");
	}

	public static void main(final String[] args) throws Exception {
		final PrintStream stderr = System.err;
		final var previousJson = GAMA.getJsonEncoder();
		try {
			GAMA.setJsonEncoder(Json.getNew());
			checkClosing();
			checkConcurrentDelivery();
			System.out.println("Server lifecycle state checks passed");
		} finally {
			System.setErr(stderr);
			GAMA.setJsonEncoder(previousJson);
		}
	}
}
