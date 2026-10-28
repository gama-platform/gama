/*******************************************************************************************************
 *
 * BatchServerTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;

import gama.api.GAMA;
import gama.api.runtime.GamaExecutorService;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.MessageType;
import gama.api.utils.server.ReceivedMessage;
import gama.headless.listener.HeadlessPlayCommand;

/** Regression checks for #297. Invoke run() after initializing the headless GAMA runtime. */
public final class BatchServerTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static long count(final List<String> events, final MessageType type) {
		return events.stream().filter(s -> GAMA.getJsonEncoder().parse(s).asObject().get("type").asString()
				.equals(type.name())).count();
	}

	private static void runBatch(final boolean keep, final boolean allParallel, final boolean step,
			final boolean immediatelyOver, final String override) throws Exception {
		final var file = Files.createTempFile("batch-server-", ".gaml");
		final var events = new CopyOnWriteArrayList<String>();
		final WebSocket socket = (WebSocket) Proxy.newProxyInstance(WebSocket.class.getClassLoader(),
				new Class<?>[] { WebSocket.class }, (proxy, method, args) -> switch (method.getName()) {
					case "isOpen" -> true;
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					case "send" -> { events.add((String) args[0]); yield null; }
					default -> throw new AssertionError("Unexpected socket call: " + method);
				});
		GamaServerExperimentJob job = null;
		Thread execution = null;
		try {
			Files.writeString(file, """
					model batchtest
					global {
						bool end <- false;
						reflex e when: cycle = 100 { end <- true; }
					}
					experiment simulationBase type: batch until: %s keep_simulations: %s repeat: 100 parallel: 1 {}
					""".formatted(immediatelyOver ? "true" : "end", keep));
			GamaExecutorService.CONCURRENCY_SIMULATIONS_ALL.set(allParallel);
			job = new GamaServerExperimentJob(file.toString(), "simulationBase", socket, null, override,
					false, false, false, true);
			check(job.controller.processOpen(true), "Loading the batch failed");
			final var controller = job.controller;
			final var plan = controller.getExperiment();
			final String experimentId = job.getExperimentID();
			check(!job.simulator.isInterrupted(), "A batch without a simulation must be runnable");
			execution = new Thread(controller.executionThread, "Batch regression execution");
			execution.start();
			if (step) {
				final var request = new FutureTask<>(() -> controller.processStep(3, true));
				final var caller = new Thread(request, "Batch regression step");
				caller.start();
				try {
					check(request.get(20, TimeUnit.SECONDS), "The completing batch step must succeed");
				} finally {
					caller.interrupt();
					caller.join(1000);
				}
			} else {
				final var server = (IGamaServer) Proxy.newProxyInstance(IGamaServer.class.getClassLoader(),
						new Class<?>[] { IGamaServer.class }, (proxy, method, args) -> {
							if ("retrieveExperimentPlan".equals(method.getName())) return plan;
							throw new AssertionError("Unexpected server call: " + method);
						});
				check(new HeadlessPlayCommand().execute(server, socket,
						new ReceivedMessage("", Map.of("type", "play", "sync", true,
								"exp_id", experimentId))) == null,
						"Synchronous batch play must wait for SimulationEnded, even without a client until");
			}
			execution.join(20_000);
			final int runs = ((Number) plan.getAgent().getClass().getMethod("getRunNumber")
					.invoke(plan.getAgent())).intValue();
			check(!execution.isAlive(), "The server must stop driving a completed batch; runs: "
					+ runs + "; execution: "
					+ java.util.Arrays.toString(execution.getStackTrace()) + "; events: " + events);
			check(runs == 100, "All repetitions must run exactly once");
			check(job.simulator.isInterrupted(), "Completion must use the experiment's disposed scope");
			check(count(events, MessageType.SimulationEnded) == 1, "Batch completion must be reported exactly once");
			check(events.stream().filter(s -> s.contains("\"SimulationEnded\""))
					.allMatch(s -> GAMA.getJsonEncoder().parse(s).asObject().get("command").asObject()
							.get("exp_id").asString().equals(experimentId)),
					"Completion must identify the experiment, including when started with step");
			check(count(events, MessageType.RuntimeError) == 0 && count(events, MessageType.SimulationError) == 0,
					"The batch must not report a runtime error: " + events);
			for (final boolean synchronous : List.of(false, true)) {
				check(!controller.processStep(1, synchronous), "A completed batch must refuse further steps");
				check(!controller.processStart(synchronous), "A completed batch must refuse further play");
			}
		} finally {
			try {
				if (job != null && job.controller.getExperiment() != null) { job.controller.close(); }
				if (execution != null) {
					execution.join(2000);
					check(!execution.isAlive(), "The batch execution thread must terminate");
				}
			} finally {
				Files.delete(file);
			}
		}
	}

	public static void run() throws Exception {
		final boolean previous = GamaExecutorService.CONCURRENCY_SIMULATIONS_ALL.getValue();
		final String directory = System.getProperty("user.dir");
		try {
			for (final boolean allParallel : List.of(false, true)) {
				runBatch(true, allParallel, false, false, "");
				runBatch(false, allParallel, false, false, "");
			}
			runBatch(true, false, false, true, "");
			runBatch(true, false, false, false, "cycle >= 2");
			runBatch(true, false, true, false, "");
		} finally {
			GamaExecutorService.CONCURRENCY_SIMULATIONS_ALL.set(previous);
			System.setProperty("user.dir", directory);
		}
	}
}
