/*******************************************************************************************************
 *
 * StreamingService.java, in gama.extension.streaming, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.streaming;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.ui.IOutput;
import gama.core.outputs.LayeredDisplayOutput;
import gama.dev.DEBUG;

/**
 * Holds the active display-streaming subscriptions, one set per running experiment, and runs a background pump that
 * captures and sends frames as the simulation advances. This service is shared by every gama-server runtime (headless
 * and GUI) because the capture goes through the public {@link IExperimentSpecies} / {@link LayeredDisplayOutput} API.
 *
 * <p>
 * The pump reads the <em>live</em> experiment each tick: it resolves the current simulation, reads its cycle and, for
 * every subscription due on that cycle, renders and sends a frame. It stops automatically when the last subscription is
 * removed, when the client socket closes, or when the simulation dies.
 */
public class StreamingService {

	static {
		DEBUG.OFF();
	}

	/** How often the pump checks the simulation cycle, in milliseconds. */
	private static final long POLL_INTERVAL_MS = 25;

	/** The singleton instance. */
	private static final StreamingService INSTANCE = new StreamingService();

	/**
	 * Gets the single instance of StreamingService.
	 *
	 * @return the streaming service
	 */
	public static StreamingService getInstance() { return INSTANCE; }

	/** The active streams, keyed by experiment id. */
	private final Map<String, ExperimentStream> streams = new ConcurrentHashMap<>();

	/**
	 * Registers (or updates) a subscription to a display of an experiment and makes sure the pump is running.
	 *
	 * @param socket
	 *            the client WebSocket to which frames are sent
	 * @param experiment
	 *            the running experiment
	 * @param expId
	 *            the experiment id, used both as a key and as the {@code exp_id} of the messages
	 * @param subscription
	 *            the subscription describing the display, resolution and frame rate
	 */
	public synchronized void subscribe(final WebSocket socket, final IExperimentSpecies experiment, final String expId,
			final StreamSubscription subscription) {
		final ExperimentStream stream =
				streams.computeIfAbsent(expId, k -> new ExperimentStream(socket, experiment, expId));
		stream.subscriptions.put(subscription.display, subscription);
		stream.start();
	}

	/**
	 * Removes the subscription to a display. If it was the last subscription of the experiment, the pump is stopped.
	 *
	 * @param expId
	 *            the experiment id
	 * @param display
	 *            the display name to stop streaming
	 */
	public synchronized void unsubscribe(final String expId, final String display) {
		final ExperimentStream stream = streams.get(expId);
		if (stream == null) return;
		stream.subscriptions.remove(display);
		if (stream.subscriptions.isEmpty()) { stop(expId); }
	}

	/**
	 * Stops all streaming for an experiment and releases its pump.
	 *
	 * @param expId
	 *            the experiment id
	 */
	public synchronized void stop(final String expId) {
		final ExperimentStream stream = streams.remove(expId);
		if (stream != null) { stream.stop(); }
	}

	/**
	 * The streaming state of a single experiment: its socket, its subscriptions and the pump thread.
	 */
	private static class ExperimentStream {

		/** The client socket. */
		final WebSocket socket;

		/** The running experiment. */
		final IExperimentSpecies experiment;

		/** The experiment id. */
		final String expId;

		/** The active subscriptions, keyed by display name. */
		final Map<String, StreamSubscription> subscriptions = new ConcurrentHashMap<>();

		/** The pump thread. */
		private volatile Thread pump;

		/** Whether the pump should keep running. */
		private volatile boolean running;

		/**
		 * Instantiates a new experiment stream.
		 *
		 * @param socket
		 *            the socket
		 * @param experiment
		 *            the experiment
		 * @param expId
		 *            the exp id
		 */
		ExperimentStream(final WebSocket socket, final IExperimentSpecies experiment, final String expId) {
			this.socket = socket;
			this.experiment = experiment;
			this.expId = expId;
		}

		/**
		 * Starts the pump thread if it is not already running.
		 */
		synchronized void start() {
			if (running) return;
			running = true;
			pump = new Thread(this::run, "gama-display-stream-" + expId);
			pump.setDaemon(true);
			pump.start();
		}

		/**
		 * Signals the pump to stop.
		 */
		synchronized void stop() {
			running = false;
			if (pump != null) { pump.interrupt(); }
		}

		/**
		 * The pump loop: each tick, resolve the live simulation, read its cycle, and stream every display that is due.
		 */
		private void run() {
			try {
				while (running) {
					if (socket == null || socket.isClosed()) { break; }
					final ISimulationAgent sim = experiment.getCurrentSimulation();
					if (sim != null && !sim.dead()) {
						final int cycle = sim.getCycle(sim.getScope());
						for (final StreamSubscription sub : subscriptions.values()) {
							if (!sub.shouldSend(cycle)) { continue; }
							final IOutput output = sim.getOutputManager().getOutputWithOriginalName(sub.display);
							if (output instanceof LayeredDisplayOutput ldo) {
								DisplayStreamer.streamFrame(socket, expId, ldo, sub, cycle);
								sub.markSent(cycle);
							}
						}
					}
					Thread.sleep(POLL_INTERVAL_MS);
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (final Exception e) {
				DEBUG.ERR("Display streaming pump for experiment '" + expId + "' stopped on error", e);
			} finally {
				running = false;
			}
		}

	}

}
