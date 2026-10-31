/*******************************************************************************************************
 *
 * GamaServerConsole.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.java_websocket.WebSocket;

import gama.api.ui.ConsoleEvent;
import gama.api.ui.ConsoleEvent.Level;
import gama.api.ui.IConsoleSink;
import gama.dev.DEBUG;

/** UI-independent server diagnostics, retained across view closure and GUI server restarts. */
public final class GamaServerConsole implements IConsoleSink {

	public enum State {
		STOPPED, STARTING, RUNNING, STOPPING, FAILED
	}

	public record Entry(long sequence, ConsoleEvent event) {}

	public record Snapshot(State state, int port, String lastError, Path logFile, boolean exchangeLogging,
			Map<String, String> clients,
			List<Entry> entries, long sequence) {}

	private static final int MAX_CHARACTERS = 200_000;
	private static final int MAX_EVENTS = 2_000;
	private static final int MAX_MESSAGE = 16_384;
	private static final String TRUNCATION_MARKER = "\n(... truncated)";

	private final Deque<Entry> entries = new ArrayDeque<>();
	private final Map<String, String> clients = new LinkedHashMap<>();
	private final List<IConsoleSink> sinks = new java.util.ArrayList<>();
	private State state = State.STOPPED;
	private int port;
	private String lastError = "";
	private Path logFile;
	private boolean exchangeLogging;
	private int characters;
	private long sequence;

	public synchronized Snapshot snapshot() {
		return new Snapshot(state, port, lastError, logFile, exchangeLogging,
				Map.copyOf(clients), List.copyOf(entries), sequence);
	}

	/** Sinks must return promptly; GUI sinks schedule their own batched updates. */
	public synchronized void addConsoleSink(final IConsoleSink sink) {
		Objects.requireNonNull(sink);
		if (sink != this && !sinks.contains(sink)) { sinks.add(sink); }
	}

	public synchronized void removeConsoleSink(final IConsoleSink sink) {
		sinks.remove(sink);
	}

	public synchronized void setState(final State value, final int serverPort) {
		state = value;
		port = serverPort;
		if (value == State.STARTING) {
			lastError = "";
			logFile = null;
			exchangeLogging = false;
			clients.clear();
		} else if (value == State.STOPPED || value == State.FAILED) {
			exchangeLogging = false;
			clients.clear();
		}
		message(Level.INFO, "server", "Server " + value.name().toLowerCase() + " on port " + serverPort);
	}

	public synchronized void setExchangeLogging(final boolean enabled, final Path file) {
		exchangeLogging = enabled;
		logFile = file;
		message(Level.INFO, "server", "Exchange logging " + (enabled ? "enabled: " + file : "disabled"));
	}

	public synchronized void connectionOpened(final WebSocket socket) {
		final String id = GamaWebSocketServer.getSocketId(socket);
		final String address = String.valueOf(socket.getRemoteSocketAddress());
		clients.put(id, address);
		message(Level.INFO, id, "Client connected: " + address);
	}

	public synchronized void connectionClosed(final WebSocket socket, final int code, final String reason) {
		final String id = GamaWebSocketServer.getSocketId(socket);
		clients.remove(id);
		message(Level.INFO, id, "Client disconnected (" + code + "): " + reason);
	}

	public void request(final WebSocket socket, final String message) {
		message(Level.DEBUG, GamaWebSocketServer.getSocketId(socket), "Request: " + truncate(message));
	}

	public synchronized void error(final String context, final Throwable error) {
		lastError = truncate(context + ": " + error);
		final StringWriter trace = new StringWriter();
		error.printStackTrace(new PrintWriter(trace));
		message(Level.ERROR, "server", context + "\n" + trace);
	}

	public void message(final Level level, final String source, final String message) {
		accept(new ConsoleEvent(level, message, null, null, null, source, Instant.now()));
	}

	@Override
	public synchronized void accept(final ConsoleEvent event) {
		if (event.message() == null || event.message().isEmpty()) return;
		final String text = truncate(event.message());
		final ConsoleEvent retained = new ConsoleEvent(event.level(), text, null, event.color(), event.cycle(),
				event.source(), event.timestamp());
		entries.addLast(new Entry(++sequence, retained));
		characters += text.length();
		while (characters > MAX_CHARACTERS || entries.size() > MAX_EVENTS) {
			characters -= entries.removeFirst().event().message().length();
		}
		for (final IConsoleSink sink : List.copyOf(sinks)) {
			try {
				sink.accept(retained);
			} catch (final RuntimeException e) {
				DEBUG.ERR("Unable to deliver server diagnostics to a console sink", e);
			}
		}

	}

	private static String truncate(final String text) {
		return text.length() > MAX_MESSAGE
				? text.substring(0, MAX_MESSAGE - TRUNCATION_MARKER.length()) + TRUNCATION_MARKER : text;
	}
}
