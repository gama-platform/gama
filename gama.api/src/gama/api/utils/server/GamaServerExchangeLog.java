/*******************************************************************************************************
 *
 * GamaServerExchangeLog.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Base64;

import org.java_websocket.WebSocket;
import org.java_websocket.enums.Opcode;
import org.java_websocket.framing.Framedata;

import gama.api.GAMA;
import gama.api.runtime.SystemInfo;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/** One flushed JSON Lines journal per server run, shared by all its connections. */
final class GamaServerExchangeLog implements AutoCloseable {

	static final String ENABLED_PROPERTY = "gama.server.log";
	static final String DIRECTORY_PROPERTY = "gama.server.log.directory";

	// Do not feed logging failures back through the server's stderr-to-WebSocket broadcaster.
	private final PrintStream errors = System.err;
	private BufferedWriter writer;
	private Path file;
	private long sequence;
	private boolean stopped;

	synchronized void start(final int port) {
		try {
			final String enabled = System.getProperty(ENABLED_PROPERTY,
					GamaPreferences.Runtime.CORE_SERVER_LOG.getValue().toString());
			if ("false".equalsIgnoreCase(enabled)) return;
			if (!"true".equalsIgnoreCase(enabled))
				throw new IllegalArgumentException(ENABLED_PROPERTY + " must be true or false");
			enable(port);
		} catch (final IOException | IllegalArgumentException | SecurityException e) {
			fail(e);
		}
	}

	private void enable(final int port) throws IOException {
		if (writer != null) return;
		if (stopped) throw new IOException("The server has stopped");
		if (file == null) {
			final String directoryName = System.getProperty(DIRECTORY_PROPERTY,
					GamaPreferences.Runtime.CORE_SERVER_LOG_DIRECTORY.getValue());
			if (directoryName.isBlank()) throw new IllegalArgumentException("Server exchange log directory is empty");
			final Path directory = Path.of(directoryName).toAbsolutePath();
			Files.createDirectories(directory);
			file = Files.createTempFile(directory, "server-" + port + "-", ".jsonl");
		}
		writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		write(record(sequence == 0 ? "server_started" : "logging_enabled", null)
				.add("port", port).add("gama_version", SystemInfo.VERSION));
		if (writer == null) throw new IOException("Could not write the server exchange log");
		DEBUG.LOG("GAMA server exchange log: " + file);
	}

	synchronized IJsonObject setEnabled(final boolean enabled, final int port) throws IOException {
		if (enabled) {
			try {
				enable(port);
			} catch (final IOException | IllegalArgumentException | SecurityException e) {
				fail(e);
				throw new IOException("Unable to enable server exchange logging: " + e.getMessage(), e);
			}
		} else if (writer != null) {
			write(record("logging_disabled", null));
			if (writer == null) throw new IOException("Could not write the server exchange log");
			try {
				writer.close();
			} finally {
				writer = null;
			}
		}
		return GAMA.getJsonEncoder().object("enabled", writer != null, "log_file", file == null ? null : file.toString());
	}

	synchronized Path file() { return file; }

	synchronized void connectionOpened(final WebSocket socket) {
		if (writer != null) { write(record("connection_opened", socket)); }
	}

	synchronized void connectionClosed(final WebSocket socket, final int code, final String reason,
			final boolean remote) {
		if (writer != null) {
			write(record("connection_closed", socket).add("code", code).add("reason", reason).add("remote", remote));
		}
	}

	synchronized void frame(final WebSocket socket, final String direction, final Framedata frame) {
		if (writer == null) return;
		final ByteBuffer payload = frame.getPayloadData().asReadOnlyBuffer();
		final byte[] bytes = new byte[payload.remaining()];
		payload.get(bytes);
		final boolean text = frame.getOpcode() == Opcode.TEXT && frame.isFin();
		write(record("frame", socket).add("direction", direction).add("opcode", frame.getOpcode().name())
				.add("fin", frame.isFin()).add("encoding", text ? "utf-8" : "base64")
				.add("payload", text ? new String(bytes, StandardCharsets.UTF_8) : Base64.getEncoder().encodeToString(bytes)));
	}

	private IJsonObject record(final String event, final WebSocket socket) {
		final IJsonObject record = GAMA.getJsonEncoder().object("timestamp", Instant.now().toString())
				.add("sequence", ++sequence).add("event", event);
		if (socket != null) {
			record.add("socket_id", GamaWebSocketServer.getSocketId(socket))
					.add("remote_address", String.valueOf(socket.getRemoteSocketAddress()));
		}
		return record;
	}

	private void write(final IJsonObject record) {
		try {
			writer.write(record.toString());
			writer.newLine();
			writer.flush();
		} catch (final IOException e) {
			fail(e);
		}
	}

	private void fail(final Exception error) {
		errors.println("GAMA server exchange logging unavailable"
				+ (file == null ? "" : " (" + file + ")") + ": " + error);
		if (writer != null) {
			try {
				writer.close();
			} catch (final IOException e) {
				errors.println("Could not close GAMA server exchange log: " + e);
			}
			writer = null;
		}
	}

	@Override
	public synchronized void close() {
		stopped = true;
		if (writer == null) return;
		write(record("server_stopped", null));
		if (writer == null) return;
		try {
			writer.close();
		} catch (final IOException e) {
			fail(e);
		} finally {
			writer = null;
		}
	}
}
