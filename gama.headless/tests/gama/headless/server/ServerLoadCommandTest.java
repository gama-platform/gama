/*******************************************************************************************************
 *
 * ServerLoadCommandTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.java_websocket.WebSocket;
import org.java_websocket.enums.ReadyState;

import gama.api.GAMA;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.IModelSpecies;
import gama.api.types.list.GamaListFactory;
import gama.api.types.map.GamaMapFactory;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IStatusDisplayer;
import gama.api.ui.NullGuiHandler;
import gama.api.utils.server.DefaultServerCommands;
import gama.api.utils.server.GamaServerConsoleListener;
import gama.api.utils.server.GamaServerExperimentConfiguration;
import gama.api.utils.server.GamaServerMessager;
import gama.api.utils.server.GamaServerStatusDisplayer;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.MessageType;
import gama.api.utils.server.ReceivedMessage;
import gama.api.utils.server.ServerLoadRequest;
import gama.headless.listener.HeadlessLoadCommand;

/** Regression checks for #394. Invoke run() after initializing the runtime, before starting a server. */
public final class ServerLoadCommandTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static Object value(final String name) {
		final var sim = GAMA.getExperiment().getCurrentSimulation();
		return sim.getDirectVarValue(sim.getScope(), name);
	}

	private static long count(final List<String> events, final MessageType type) {
		return events.stream().filter(s -> GAMA.getJsonEncoder().parse(s).asObject().get("type").asString()
				.equals(type.name())).count();
	}

	public static void run() throws Exception {
		final var previousGui = GAMA.getHeadlessGui();
		final var previousServer = GAMA.getServer();
		check(previousServer == null, "Run before starting a server");
		final String previousDirectory = System.getProperty("user.dir");
		final var events = new CopyOnWriteArrayList<String>();
		final var ended = new AtomicReference<>(new CountDownLatch(1));
		final WebSocket socket = (WebSocket) Proxy.newProxyInstance(WebSocket.class.getClassLoader(),
				new Class<?>[] { WebSocket.class }, (p, method, args) -> switch (method.getName()) {
					case "isOpen" -> true;
					case "isClosed", "isClosing" -> false;
					case "getReadyState" -> ReadyState.OPEN;
					case "hashCode" -> System.identityHashCode(p);
					case "equals" -> p == args[0];
					case "toString" -> "load-test-socket";
					case "send" -> {
						final String message = (String) args[0];
						events.add(message);
						if (message.contains("\"SimulationEnded\"")) ended.get().countDown();
						yield null;
					}
					default -> throw new AssertionError("Unexpected socket method: " + method);
				});
		GAMA.setHeadlessGui(new NullGuiHandler() {
			@Override
			public boolean openSimulationPerspective(final IModelSpecies model, final String experiment) { return true; }

			@Override
			public IConsoleListener getConsole() { return new GamaServerConsoleListener(); }

			@Override
			public IStatusDisplayer getStatus() { return new GamaServerStatusDisplayer(); }
		});
		final var file = Files.createTempFile("server-load-", ".gaml");
		try {
			Files.writeString(file, """
					model server_load
					global {
						int var1 <- -100;
						float amount <- -1.0;
						int initial;
						int completed <- 0;
						init { initial <- var1; write var1; }
						reflex tick { completed <- completed + 1; }
					}
					experiment exp type: gui autorun: false until: (completed >= 7) {
						parameter "param" var: var1;
						parameter "Amount" var: amount;
					}
					""");
			final Map<String, Object> command = new HashMap<>(Map.of(
					"type", "load", "model", file.toString(), "experiment", "exp"));
			final var defaults = ServerLoadRequest.parse(socket, command);
			check(defaults.configuration().hasConsole() && defaults.configuration().hasRuntime()
					&& !defaults.configuration().hasStatus() && !defaults.configuration().hasDialog(),
					"Load defaults must match the documented protocol");

			final var invalidElement = GamaListFactory.create();
			invalidElement.add("not a map");
			final var missingValue = GamaListFactory.create(Types.MAP);
			missingValue.add(GamaMapFactory.wrap(Map.of("name", "var1")));
			for (final Object malformed : List.of("not an array", invalidElement, missingValue)) {
				command.put("parameters", malformed);
				check(DefaultServerCommands.LOAD(null, socket, command).type == MessageType.MalformedRequest,
						"GUI load must reject malformed parameters");
				check(new HeadlessLoadCommand().execute(null, socket, new ReceivedMessage("", command))
						.type == MessageType.MalformedRequest, "Headless load must use the same validation");
			}
			final var parameters = GamaListFactory.create(Types.MAP);
			parameters.add(GamaMapFactory.wrap(Map.of("name", "param", "value", "42", "type", "int")));
			parameters.add(GamaMapFactory.wrap(Map.of("name", "amount", "value", "2.5", "type", "float")));
			command.put("parameters", parameters);
			command.put("until", "completed >= 3");
			final var response = DefaultServerCommands.LOAD(null, socket, command);
			if (response.type != MessageType.CommandExecutedSuccessfully)
				throw new AssertionError("GUI loading failed: " + response.content,
						response.content instanceof Throwable error ? error : null);
			check(response.content.equals("exp"), "GUI load must preserve the experiment-name response");
			check(value("var1").equals(42) && value("initial").equals(42),
					"Parameters by title must be set before global init");
			check(value("amount").equals(2.5), "Parameters by variable name must support typed numeric strings");
			final var plan = GAMA.getExperiment();
			check(plan.getAgent().getScope().getServerConfiguration().socket() == socket,
					"The experiment must retain the requesting socket");
			check(count(events, MessageType.SimulationOutput) == 1, "Init output must reach the configured client");

			for (final String enabled : List.of("", "console", "status", "dialog", "runtime")) {
				events.clear();
				for (final String flag : List.of("console", "status", "dialog", "runtime")) {
					command.put(flag, flag.equals(enabled));
				}
				final var config = ServerLoadRequest.parse(socket, command).configuration();
				final var scope = plan.getAgent().getScope();
				scope.setServerConfiguration(config);
				new GamaServerConsoleListener().informConsole("console", plan.getAgent(), null);
				new GamaServerStatusDisplayer().informStatus(scope, "a \"quoted\" status", null);
				GamaServerMessager.forwardMessage(scope, "dialog", MessageType.SimulationDialog);
				GamaServerMessager.forwardMessage(scope, "error dialog", MessageType.SimulationErrorDialog);
				GamaServerMessager.forwardMessage(scope, "runtime", MessageType.SimulationError);
				check(count(events, MessageType.SimulationOutput) == ("console".equals(enabled) ? 1 : 0),
						"Console forwarding must only depend on console");
				check(count(events, MessageType.SimulationStatusInform) == ("status".equals(enabled) ? 1 : 0),
						"Status forwarding must only depend on status");
				check(count(events, MessageType.SimulationDialog) == ("dialog".equals(enabled) ? 1 : 0)
						&& count(events, MessageType.SimulationErrorDialog) == ("dialog".equals(enabled) ? 1 : 0),
						"Dialog forwarding must only depend on dialog");
				check(count(events, MessageType.SimulationError) == ("runtime".equals(enabled) ? 1 : 0),
						"Runtime forwarding must only depend on runtime");
			}
			events.clear();
			check(plan.getController().processStart(true), "Starting the GUI experiment failed");
			check(ended.get().await(5, TimeUnit.SECONDS), "The GUI experiment must report its stop condition");
			check(value("completed").equals(3), "Until must stop at the exact requested step");
			check(plan.getController().isPaused(), "The GUI experiment must remain paused for inspection");
			check(count(events, MessageType.SimulationEnded) == 1, "Completion must be reported exactly once");
			check(!plan.getController().processStep(1, true), "Steps after completion must be refused");
			check(!plan.getController().processStart(true), "Play after completion must be refused");

			final WebSocket otherSocket = (WebSocket) Proxy.newProxyInstance(WebSocket.class.getClassLoader(),
					new Class<?>[] { WebSocket.class }, (p, method, args) -> {
						throw new AssertionError("Experiment output leaked to another client: " + method);
					});
			GAMA.setServer((IGamaServer) Proxy.newProxyInstance(IGamaServer.class.getClassLoader(),
					new Class<?>[] { IGamaServer.class }, (p, method, args) -> {
						if ("stop".equals(method.getName())) return null;
						if ("retrieveExperimentPlan".equals(method.getName())) return plan;
						if ("obtainGuiServerConfiguration".equals(method.getName()))
							return new GamaServerExperimentConfiguration(otherSocket, "", true, true, true, true);
						throw new AssertionError("Unexpected server call: " + method);
					}));
			command.put("type", "reload");
			command.put("parameters", Map.of("param", 43, "amount", 3.5));
			command.put("until", "completed >= 2");
			final var compactReload = DefaultServerCommands.RELOAD(GAMA.getServer(), socket, command);
			check(compactReload.type == MessageType.CommandExecutedSuccessfully,
					"Reload must accept compact parameters: " + compactReload.content);
			check(value("initial").equals(43) && value("amount").equals(3.5),
					"Compact reload parameters must be applied before global init");
			check(plan.getAgent().getScope().getServerConfiguration().socket() == socket,
					"Reload must retain experiment-specific routing");
			ended.set(new CountDownLatch(1));
			check(plan.getController().processReload(true), "Reloading must permit another run");
			check(value("initial").equals(43), "Reload must retain parameter overrides");
			check(plan.getAgent().getScope().getServerConfiguration().socket() == socket,
					"Reload must retain experiment-specific routing");
			check(plan.getController().processStart(true), "Reload must reset completion state");
			check(ended.get().await(5, TimeUnit.SECONDS), "Reloaded experiments must report completion again");
			check(value("completed").equals(2), "Reload must apply the new stop condition");
			GAMA.setServer(previousServer);

			try {
				plan.setStopCondition("42");
				throw new AssertionError("Non-boolean until must be rejected");
			} catch (gama.api.exceptions.GamaRuntimeException expected) {
				check(expected.getMessage().contains("boolean"), "Until rejection must explain the required type");
			}
			plan.setStopCondition("");
			check(Boolean.FALSE.equals(plan.getStopCondition().value(plan.getAgent().getScope())),
					"An empty reload condition must clear the previous limit");

			GAMA.closeAllExperiments(true, false);
			command.remove("parameters");
			command.remove("until");
			check(DefaultServerCommands.LOAD(null, socket, command).type == MessageType.CommandExecutedSuccessfully,
					"A plain GUI load must still work");
			check(value("initial").equals(-100), "Omitted parameters must use model defaults");
			ended.set(new CountDownLatch(1));
			check(GAMA.getExperiment().getController().processStart(true), "Plain GUI load must be runnable");
			check(ended.get().await(5, TimeUnit.SECONDS), "The model's declared condition must be retained");
			check(value("completed").equals(7), "Omitting until must preserve the model's stop condition");
			GAMA.closeAllExperiments(true, false);

			command.put("type", "load");
			command.put("parameters", Map.of("param", 44, "amount", 4.5));
			command.put("socket_id", "load-test");
			final var server = (IGamaServer) Proxy.newProxyInstance(IGamaServer.class.getClassLoader(),
					new Class<?>[] { IGamaServer.class }, (p, method, args) -> {
						if ("addExperiment".equals(method.getName()) || "execute".equals(method.getName())) return null;
						throw new AssertionError("Unexpected headless server call: " + method);
					});
			final var headless = new HeadlessLoadCommand().execute(server, socket, new ReceivedMessage("", command));
			check(headless.type == MessageType.CommandExecutedSuccessfully, "Headless loading must still work");
			check(value("initial").equals(44) && value("amount").equals(4.5),
					"Headless load must apply compact parameters before init");
			final var simulation = GAMA.getExperiment().getCurrentSimulation();
			simulation.setDirectVarValue(simulation.getScope(), "completed", 7);
			check(Boolean.TRUE.equals(GAMA.getExperiment().getAgent().getStopCondition().value(simulation.getScope())),
					"Headless loading must also preserve a declared condition when until is omitted");
		} finally {
			GAMA.closeAllExperiments(true, false);
			GAMA.setHeadlessGui(previousGui);
			GAMA.setServer(previousServer);
			System.setProperty("user.dir", previousDirectory);
			Files.delete(file);
		}
	}
}
