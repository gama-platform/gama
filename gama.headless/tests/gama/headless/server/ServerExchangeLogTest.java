/*******************************************************************************************************
 *
 * ServerExchangeLogTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.Opcode;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.SSLParametersWebSocketServerFactory;

import gama.api.GAMA;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.server.GamaWebSocketServer;
import gama.api.utils.server.IServerConfiguration;
import gama.api.utils.server.ReceivedMessage;
import gama.core.util.json.Json;
import gama.ui.application.server.GamaGuiWebSocketServer;

/** Standalone checks for #1269. Optional arguments: a localhost TLS keystore path and its password. */
public final class ServerExchangeLogTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static final class Server extends GamaWebSocketServer {
		final CountDownLatch started = new CountDownLatch(1);
		final BlockingQueue<WebSocket> sockets = new LinkedBlockingQueue<>();

		Server() { super(0, -1, true); }

		@Override
		public void onStart() {
			super.onStart();
			started.countDown();
		}

		@Override
		public void onOpen(final WebSocket socket, final ClientHandshake handshake) {
			super.onOpen(socket, handshake);
			sockets.add(socket);
		}

		@Override
		public void onMessage(final WebSocket socket, final String message) {
			if (message.startsWith("{\"type\":\"server_log\"")) {
				super.onMessage(socket, message);
			} else {
				socket.send("reply:" + message);
			}
		}

		@Override
		public ReceivedMessage extractParam(final WebSocket socket, final String message) {
			final var json = GAMA.getJsonEncoder().parse(message).asObject();
			final Map<String, Object> values = new HashMap<>();
			values.put("type", "server_log");
			final var enabled = json.get("enabled");
			if (enabled != null) values.put("enabled", enabled.isBoolean() ? enabled.asBoolean() : enabled.toString());
			return new ReceivedMessage(message, values);
		}

		@Override
		public void onMessage(final WebSocket socket, final ByteBuffer message) { socket.send(message); }

		@Override
		public IExperimentSpecies getExperiment(final String socket, final String id) { return null; }

		@Override
		public void execute(final Runnable command) { command.run(); }

		@Override
		public void addExperiment(final String socket, final String id, final IExperimentSpecies plan) {}

		@Override
		public IServerConfiguration obtainGuiServerConfiguration() { return GUI; }

		@Override
		public IExperimentSpecies retrieveExperimentPlan(final WebSocket socket, final Map<String, Object> map) {
			return null;
		}
	}

	private static final class Client extends WebSocketClient {
		final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
		final BlockingQueue<byte[]> binaries = new LinkedBlockingQueue<>();

		Client(final int port) { this(port, false); }

		Client(final int port, final boolean secure) {
			super(URI.create((secure ? "wss" : "ws") + "://localhost:" + port));
		}

		@Override
		public void onOpen(final ServerHandshake handshake) {}

		@Override
		public void onMessage(final String message) { messages.add(message); }

		@Override
		public void onMessage(final ByteBuffer message) {
			final byte[] bytes = new byte[message.remaining()];
			message.get(bytes);
			binaries.add(bytes);
		}

		@Override
		public void onClose(final int code, final String reason, final boolean remote) {}

		@Override
		public void onError(final Exception ex) { messages.add("ERROR: " + ex); }

		String message() throws InterruptedException {
			final String result = messages.poll(5, TimeUnit.SECONDS);
			check(result != null && !result.startsWith("ERROR:"), "Missing response: " + result);
			return result;
		}
	}

	private static void start(final Server server) throws InterruptedException {
		server.start();
		check(server.started.await(5, TimeUnit.SECONDS), "Server did not start");
	}

	private static void connect(final Client client) throws InterruptedException {
		check(client.connectBlocking(5, TimeUnit.SECONDS), "Client did not connect");
		check(client.message().contains("ConnectionSuccessful"), "Missing greeting");
	}

	private static List<IJsonObject> records(final Path file) throws Exception {
		return Files.readAllLines(file).stream().map(line -> GAMA.getJsonEncoder().parse(line).asObject()).toList();
	}

	private static long frames(final List<IJsonObject> records, final String direction, final String opcode,
			final String payload) {
		return records.stream().filter(r -> "frame".equals(r.get("event").asString())
				&& direction.equals(r.get("direction").asString()) && opcode.equals(r.get("opcode").asString())
				&& payload.equals(r.get("payload").asString())).count();
	}

	private static void checkExchanges() throws Exception {
		final Server server = new Server();
		Client first = null;
		Client second = null;
		try {
			start(server);
			final Path log = server.getExchangeLogFile();
			check(log != null && Files.isRegularFile(log), "Explicitly enabled log not created");
			first = new Client(server.getPort());
			second = new Client(server.getPort());
			connect(first);
			connect(second);
			final WebSocket socket = server.sockets.poll(5, TimeUnit.SECONDS);
			check(socket != null, "Missing server socket");
			final String original = "{\"value\":\"quoted \\\"text\\\"\"}\ninvalid JSON \u00e9";
			first.send(original);
			check(first.message().equals("reply:" + original), "Transport changed the original payload");
			check(frames(records(log), "received", "TEXT", original) == 1,
					"Raw malformed input must be flushed exactly once before parsing");
			check(frames(records(log), "sent", "TEXT", "reply:" + original) == 1, "Direct reply missing");
			final String large = "x".repeat(20000) + "\nend";
			first.send(large);
			check(first.message().equals("reply:" + large), "Large payload changed");
			check(frames(records(log), "received", "TEXT", large) == 1
					&& frames(records(log), "sent", "TEXT", "reply:" + large) == 1, "Log truncated a large payload");

			server.broadcast("broadcast");
			check(first.message().equals("broadcast") && second.message().equals("broadcast"), "Broadcast failed");
			socket.send("asynchronous event");
			check(first.message().equals("asynchronous event"), "Asynchronous socket send failed");
			final byte[] bytes = { 0, -1, 1, 10 };
			first.send(bytes);
			check(java.util.Arrays.equals(bytes, first.binaries.poll(5, TimeUnit.SECONDS)), "Binary echo changed");
			server.broadcast(ByteBuffer.wrap(bytes));
			check(java.util.Arrays.equals(bytes, first.binaries.poll(5, TimeUnit.SECONDS))
					&& java.util.Arrays.equals(bytes, second.binaries.poll(5, TimeUnit.SECONDS)), "Binary broadcast failed");
			first.sendFragmentedFrame(Opcode.TEXT, ByteBuffer.wrap("frag".getBytes(StandardCharsets.UTF_8)), false);
			first.sendFragmentedFrame(Opcode.TEXT, ByteBuffer.wrap("mented".getBytes(StandardCharsets.UTF_8)), true);
			check(first.message().equals("reply:fragmented"), "Fragmented input changed");
			first.sendPing();
			first.send("after ping");
			check(first.message().equals("reply:after ping"), "Ping changed application traffic");

			for (int i = 0; i < 30; i++) {
				first.send("first-" + i);
				second.send("second-" + i);
			}
			for (int i = 0; i < 30; i++) {
				check(first.message().equals("reply:first-" + i), "First client response reordered");
				check(second.message().equals("reply:second-" + i), "Second client response reordered");
			}
			first.closeBlocking();
			second.closeBlocking();
			server.stop();
			final List<IJsonObject> records = records(log);
			long sequence = 0;
			for (final IJsonObject record : records) {
				check(record.get("sequence").asLong() == ++sequence, "Interleaved or missing record");
				Instant.parse(record.get("timestamp").asString());
			}
			check(records.getFirst().get("event").asString().equals("server_started"), "Missing server metadata");
			check(records.getLast().get("event").asString().equals("server_stopped"), "Log not closed on shutdown");
			check(records.getFirst().get("gama_version") != null, "Missing GAMA version");
			check(records.stream().filter(r -> r.get("socket_id") != null)
					.map(r -> r.get("socket_id").asString()).distinct().count() == 2, "Connections not distinguished");
			check(records.stream().filter(r -> "connection_closed".equals(r.get("event").asString())).count() == 2,
					"Missing disconnects");
			check(frames(records, "sent", "TEXT", "broadcast") == 2, "Broadcast must record each recipient once");
			check(frames(records, "sent", "TEXT", "asynchronous event") == 1, "Missing asynchronous event");
			final String base64 = Base64.getEncoder().encodeToString(bytes);
			check(frames(records, "received", "BINARY", base64) == 1, "Binary input missing or not lossless");
			check(frames(records, "sent", "BINARY", base64) == 3, "Binary output missing or duplicated");
			check(frames(records, "received", "TEXT",
					Base64.getEncoder().encodeToString("frag".getBytes(StandardCharsets.UTF_8))) == 1,
					"First fragment missing");
			check(frames(records, "received", "CONTINUOUS",
					Base64.getEncoder().encodeToString("mented".getBytes(StandardCharsets.UTF_8))) == 1,
					"Continuation missing");
			check(frames(records, "received", "PING", "") == 1 && frames(records, "sent", "PONG", "") == 1,
					"Ping/pong not captured");
			for (int i = 0; i < 30; i++) {
				for (final String prefix : List.of("first-", "second-")) {
					check(frames(records, "received", "TEXT", prefix + i) == 1, "Concurrent input missing");
					check(frames(records, "sent", "TEXT", "reply:" + prefix + i) == 1, "Concurrent output missing");
				}
			}
		} finally {
			if (first != null) first.closeBlocking();
			if (second != null) second.closeBlocking();
			server.stop();
		}
	}

	private static void checkDisabled() throws Exception {
		final Server server = new Server();
		Client client = null;
		try {
			start(server);
			check(server.getExchangeLogFile() == null, "Opt-out must not create a log");
			client = new Client(server.getPort());
			connect(client);
			client.send("unlogged");
			check(client.message().equals("reply:unlogged"), "Opt-out changed the protocol");
		} finally {
			if (client != null) client.closeBlocking();
			server.stop();
		}
	}

	private static void checkWriteFailure() throws Exception {
		final Server server = new Server();
		Client client = null;
		try {
			start(server);
			client = new Client(server.getPort());
			connect(client);
			final var logField = GamaWebSocketServer.class.getDeclaredField("exchangeLog");
			logField.setAccessible(true);
			final Object log = logField.get(server);
			final var writerField = log.getClass().getDeclaredField("writer");
			writerField.setAccessible(true);
			((java.io.BufferedWriter) writerField.get(log)).close();
			client.send("after write failure");
			check(client.message().equals("reply:after write failure"), "Logging failure interrupted exchanges");
			client.send("still connected");
			check(client.message().equals("reply:still connected"), "Logging failure broke the connection");
		} finally {
			if (client != null) client.closeBlocking();
			server.stop();
		}
	}

	private static void checkRemoteToggle() throws Exception {
		final Server server = new Server();
		Client client = null;
		Client other = null;
		final boolean preference = GamaPreferences.Runtime.CORE_SERVER_LOG.getValue();
		final String property = System.getProperty("gama.server.log");
		try {
			start(server);
			client = new Client(server.getPort());
			other = new Client(server.getPort());
			connect(client);
			connect(other);
			check(server.getExchangeLogFile() == null, "Remote test must start with logging off");
			for (final String invalid : List.of("{\"type\":\"server_log\"}",
					"{\"type\":\"server_log\",\"enabled\":\"true\"}")) {
				client.send(invalid);
				check(GAMA.getJsonEncoder().parse(client.message()).asObject().get("type").asString()
						.equals("MalformedRequest"), "Invalid remote toggle accepted");
			}
			client.send("{\"type\":\"server_log\",\"enabled\":true}");
			final var response = GAMA.getJsonEncoder().parse(client.message()).asObject();
			check(response.get("type").asString().equals("CommandExecutedSuccessfully"), "Remote enable failed");
			final var content = response.get("content").asObject();
			check(content.get("enabled").asBoolean(), "Incorrect enabled state");
			final Path file = Path.of(content.get("log_file").asString());
			check(file.equals(server.getExchangeLogFile()), "Incorrect log file");
			other.send("logged other client");
			check(other.message().equals("reply:logged other client"), "Other client failed");
			client.send("{\"type\":\"server_log\",\"enabled\":true}");
			client.message();
			other.send("{\"type\":\"server_log\",\"enabled\":false}");
			check(!GAMA.getJsonEncoder().parse(other.message()).asObject().get("content").asObject()
					.get("enabled").asBoolean(), "Remote disable failed");
			final long size = Files.size(file);
			client.send("not logged");
			client.message();
			check(Files.size(file) == size, "Disabled log still receives traffic");
			client.send("{\"type\":\"server_log\",\"enabled\":true}");
			client.message();
			check(file.equals(server.getExchangeLogFile()), "Re-enable must append to the same run log");
			client.send("resumed");
			client.message();
			client.closeBlocking();
			other.closeBlocking();
			server.stop();
			final var records = records(file);
			check(frames(records, "received", "TEXT", "logged other client") == 1, "Toggle not server-wide");
			check(frames(records, "received", "TEXT", "not logged") == 0, "Disabled traffic leaked into log");
			check(frames(records, "received", "TEXT", "resumed") == 1, "Resumed traffic missing");
			check(records.stream().anyMatch(r -> r.get("event").asString().equals("logging_disabled"))
					&& records.stream().anyMatch(r -> r.get("event").asString().equals("logging_enabled")),
					"Toggle boundaries missing");
			long sequence = 0;
			for (final var record : records) check(record.get("sequence").asLong() == ++sequence, "Sequence reset");
			check(preference == GamaPreferences.Runtime.CORE_SERVER_LOG.getValue()
					&& property.equals(System.getProperty("gama.server.log")), "Remote command persisted settings");
		} finally {
			if (client != null) client.closeBlocking();
			if (other != null) other.closeBlocking();
			server.stop();
		}
	}

	private static void checkRemoteFailure() throws Exception {
		final Server server = new Server();
		Client client = null;
		try {
			start(server);
			client = new Client(server.getPort());
			connect(client);
			client.send("{\"type\":\"server_log\",\"enabled\":true}");
			final var response = GAMA.getJsonEncoder().parse(client.message()).asObject();
			check(response.get("type").asString().equals("UnableToExecuteRequest"),
					"Remote logging failure must not claim success");
			client.send("still connected");
			check(client.message().equals("reply:still connected"), "Remote failure broke exchanges");
		} finally {
			if (client != null) client.closeBlocking();
			server.stop();
		}
	}

	private static void checkGuiPreference() throws Exception {
		final GamaGuiWebSocketServer server = GamaGuiWebSocketServer.startForGUI(0, -1, true);
		check(server != null, "GUI server could not be created");
		Client client = null;
		try {
			final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
			while (server.getPort() <= 0 && System.nanoTime() < deadline) Thread.sleep(10);
			check(server.getPort() > 0, "GUI server did not bind");
			client = new Client(server.getPort());
			connect(client);
			check(server.getExchangeLogFile() == null, "GUI logging must start off");
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(true);
			final Path file = server.getExchangeLogFile();
			check(file != null && Files.isRegularFile(file), "Preference did not enable logging immediately");
			server.broadcast("preference enabled");
			check(client.message().equals("preference enabled"), "Client disconnected after preference change");
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(false);
			final long length = Files.size(file);
			server.broadcast("preference disabled");
			check(client.message().equals("preference disabled"), "Client disconnected after disabling logging");
			check(Files.size(file) == length, "Preference did not disable logging immediately");
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(true);
			check(file.equals(server.getExchangeLogFile()), "Preference must resume the same run log");
			client.closeBlocking();
			server.stop();
			final long stoppedLength = Files.size(file);
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(false);
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(true);
			check(Files.size(file) == stoppedLength, "Stopped server retained its preference listener");
		} finally {
			if (client != null) client.closeBlocking();
			server.stop();
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(false);
		}
	}

	private static void checkTls(final Path keyStoreFile, final char[] password) throws Exception {
		final KeyStore keyStore = KeyStore.getInstance("PKCS12");
		try (var input = Files.newInputStream(keyStoreFile)) {
			keyStore.load(input, password);
		}
		final KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keys.init(keyStore, password);
		final TrustManagerFactory trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trust.init(keyStore);
		final SSLContext context = SSLContext.getInstance("TLS");
		context.init(keys.getKeyManagers(), trust.getTrustManagers(), null);
		final Server server = new Server();
		server.setWebSocketFactory(new SSLParametersWebSocketServerFactory(context, new SSLParameters()));
		Client client = null;
		try {
			start(server);
			client = new Client(server.getPort(), true);
			client.setSocketFactory(context.getSocketFactory());
			connect(client);
			client.send("secure exchange");
			check(client.message().equals("reply:secure exchange"), "TLS exchange changed");
			server.broadcast("secure broadcast");
			check(client.message().equals("secure broadcast"), "TLS broadcast failed");
			client.closeBlocking();
			server.stop();
			final var records = records(server.getExchangeLogFile());
			check(frames(records, "received", "TEXT", "secure exchange") == 1, "TLS input missing");
			check(frames(records, "sent", "TEXT", "reply:secure exchange") == 1, "TLS reply missing");
			check(frames(records, "sent", "TEXT", "secure broadcast") == 1, "TLS broadcast missing");
		} finally {
			if (client != null) client.closeBlocking();
			server.stop();
		}
	}

	public static void main(final String[] args) throws Exception {
		final var previousJson = GAMA.getJsonEncoder();
		final PrintStream stderr = System.err;
		final String previousEnabled = System.getProperty("gama.server.log");
		final String previousDirectory = System.getProperty("gama.server.log.directory");
		final boolean previousPreference = GamaPreferences.Runtime.CORE_SERVER_LOG.getValue();
		final Path directory = Files.createTempDirectory("server-exchange-test-");
		try {
			GAMA.setJsonEncoder(Json.getNew());
			System.clearProperty("gama.server.log");
			System.setProperty("gama.server.log.directory", directory.toString());
			check(!GamaPreferences.Runtime.CORE_SERVER_LOG.getDefaultValue(),
					"Exchange logging must default to disabled");
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(false);
			checkDisabled();
			System.setErr(stderr);
			try (var logs = Files.list(directory)) {
				check(logs.count() == 0, "Default-off logging must not create files");
			}
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(true);
			checkExchanges();
			System.setErr(stderr);
			try (var logs = Files.list(directory)) {
				check(logs.count() == 1, "A run must create one log file");
			}
			System.setProperty("gama.server.log", "false");
			checkDisabled();
			System.setErr(stderr);
			System.clearProperty("gama.server.log");
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(false);
			checkDisabled();
			System.setErr(stderr);
			System.setProperty("gama.server.log", "true");
			try (var logs = Files.list(directory)) {
				check(logs.count() == 1, "Disabled logging must not create files");
			}
			final Path notDirectory = Files.createFile(directory.resolve("not-a-directory"));
			System.setProperty("gama.server.log.directory", notDirectory.toString());
			final var errors = new ByteArrayOutputStream();
			System.setErr(new PrintStream(errors, true, StandardCharsets.UTF_8));
			checkDisabled();
			check(errors.toString(StandardCharsets.UTF_8).contains("GAMA server exchange logging unavailable"),
					"Log creation failures must be reported, without breaking exchanges");
			System.setProperty("gama.server.log.directory", directory.toString());
			errors.reset();
			System.setErr(new PrintStream(errors, true, StandardCharsets.UTF_8));
			checkWriteFailure();
			check(errors.toString(StandardCharsets.UTF_8).lines()
					.filter(line -> line.contains("GAMA server exchange logging unavailable")).count() == 1,
					"Mid-run write failures must be reported exactly once");
			System.setErr(stderr);
			if (args.length == 2) { checkTls(Path.of(args[0]), args[1].toCharArray()); }
			System.setErr(stderr);
			System.setProperty("gama.server.log", "false");
			checkRemoteToggle();
			System.setErr(stderr);
			checkGuiPreference();
			System.setErr(stderr);
			System.setProperty("gama.server.log.directory", notDirectory.toString());
			errors.reset();
			System.setErr(new PrintStream(errors, true, StandardCharsets.UTF_8));
			checkRemoteFailure();
			System.out.println("ServerExchangeLogTest passed");
		} finally {
			System.setErr(stderr);
			GAMA.setJsonEncoder(previousJson);
			GamaPreferences.Runtime.CORE_SERVER_LOG.set(previousPreference);
			if (previousEnabled == null) System.clearProperty("gama.server.log");
			else System.setProperty("gama.server.log", previousEnabled);
			if (previousDirectory == null) System.clearProperty("gama.server.log.directory");
			else System.setProperty("gama.server.log.directory", previousDirectory);
			try (var paths = Files.walk(directory)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
			}
		}
	}
}
