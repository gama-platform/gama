package gama.extension.network.tcp;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import gama.api.kernel.agent.IAgent;
import gama.extension.network.common.IConnector;

/** Standalone TCP transport checks. Run with the network bundle's runtime classpath. */
public final class ClientServiceTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static IConnector connector(final boolean raw) {
		return (IConnector) Proxy.newProxyInstance(IConnector.class.getClassLoader(),
				new Class<?>[] { IConnector.class }, (proxy, method, args) -> {
					if ("isRaw".equals(method.getName())) return raw;
					throw new AssertionError("Unexpected connector call: " + method);
				});
	}

	private static final class RecordingClient extends ClientService {

		final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
		final AtomicReference<Throwable> failure = new AtomicReference<>();

		RecordingClient(final Socket socket, final boolean raw) {
			super(socket, connector(raw));
			setUncaughtExceptionHandler((thread, error) -> failure.set(error));
		}

		@Override
		public void receivedMessage(final String sender, final String message) {
			messages.add(message);
		}

		String receive() throws InterruptedException {
			final String message = messages.poll(5, TimeUnit.SECONDS);
			if (failure.get() != null) throw new AssertionError("TCP receiver failed", failure.get());
			check(message != null, "Timed out waiting for TCP message");
			return message;
		}

		void checkStopped() throws InterruptedException {
			join(5000);
			check(!isAlive(), "TCP receiver did not stop after disconnect");
			if (failure.get() != null) throw new AssertionError("TCP receiver failed", failure.get());
		}
	}

	private static final class Connection implements AutoCloseable {

		final Socket peer;
		final RecordingClient service;

		Connection(final boolean raw) throws IOException {
			try (ServerSocket listener = new ServerSocket(0)) {
				peer = new Socket("localhost", listener.getLocalPort());
				peer.setSoTimeout(5000);
				service = new RecordingClient(listener.accept(), raw);
				service.startService();
			}
		}

		@Override
		public void close() throws Exception {
			peer.close();
			service.stopService();
			service.checkStopped();
		}
	}

	private static String binaryContents() {
		final byte[] bytes = new byte[256];
		for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
		return new String(bytes, StandardCharsets.ISO_8859_1);
	}

	private static void checkEscaping() {
		for (final String payload : List.of(binaryContents(), "\r", "\n", "\r\n", "\b\r",
				"@a@@n@@r@@b@@r@", "@b@\r", "@@\n@\r", "unicode: \u20ac\u6f22\ud83d\ude80")) {
			final String encoded = ClientService.encodeMessage(payload);
			check(!encoded.contains("\r") && !encoded.contains("\n"), "Unescaped TCP line delimiter");
			check(payload.equals(ClientService.decodeMessage(encoded)), "TCP escaping changed the payload");
		}
		check("\b\r\n".equals(ClientService.decodeMessage("@b@@r@@n@")), "Legacy escapes not decoded");
	}

	private static void checkReadAheadAndEof() throws Exception {
		try (Connection connection = new Connection(false)) {
			final StringBuilder batch = new StringBuilder();
			for (int i = 0; i < 512; i++) {
				batch.append(ClientService.encodeMessage(i + binaryContents())).append('\n');
			}
			// Include the extra blank line emitted by older clients.
			connection.peer.getOutputStream().write((batch + "\n").getBytes(StandardCharsets.UTF_8));
			connection.peer.getOutputStream().flush();
			for (int i = 0; i < 512; i++) {
				check((i + binaryContents()).equals(connection.service.receive()),
						"Read-ahead discarded or truncated message " + i);
			}
			check(connection.service.messages.poll(100, TimeUnit.MILLISECONDS) == null, "Spurious empty message");
			connection.peer.shutdownOutput();
			connection.service.join(5000);
			check(!connection.service.isAlive() && !connection.service.isOnline(), "EOF did not stop TCP service");
		}
	}

	private static void checkConcurrentSending() throws Exception {
		try (Connection first = new Connection(false); Connection second = new Connection(false)) {
			final List<Connection> connections = List.of(first, second);
			final IAgent agent = (IAgent) Proxy.newProxyInstance(IAgent.class.getClassLoader(),
					new Class<?>[] { IAgent.class }, (proxy, method, args) -> {
						if ("getAttribute".equals(method.getName())) {
							return connections.get(Integer.parseInt(
									((String) args[0]).substring(TCPConnector._TCP_CLIENT.length()))).service;
						}
						throw new AssertionError("Unexpected agent call: " + method);
					});
			final ServerService server = new ServerService(agent, 0, connector(false));
			server.isAlive = server.isOnline = true;
			final AtomicReference<Throwable> failure = new AtomicReference<>();
			final Thread[] writers = new Thread[4];
			for (int w = 0; w < writers.length; w++) {
				final int writer = w;
				writers[w] = new Thread(() -> {
					try {
						for (int i = 0; i < 128; i++) {
							final String message = writer + ":" + i + binaryContents();
							// Exercise both send paths, including concurrent writes to one connection.
							if (writer < 2) server.sendMessage(message, "" + (writer % 2));
							else connections.get(writer % 2).service.sendMessage(message);
						}
					} catch (Throwable e) {
						failure.compareAndSet(null, e);
					}
				});
				writers[w].start();
			}
			for (int c = 0; c < connections.size(); c++) {
				final Connection connection = connections.get(c);
				final RecordingClient receiver = new RecordingClient(connection.peer, false);
				receiver.startService();
				try {
					final Set<String> expected = new HashSet<>();
					for (int w = c; w < writers.length; w += 2) {
						for (int i = 0; i < 128; i++) expected.add(w + ":" + i + binaryContents());
					}
					for (int i = 0; i < 256; i++) {
						check(expected.remove(receiver.receive()), "Corrupted or duplicated concurrent message");
					}
					check(expected.isEmpty(), "Missing concurrent messages");
				} finally {
					receiver.stopService();
					receiver.checkStopped();
				}
			}
			for (final Thread writer : writers) {
				writer.join(5000);
				check(!writer.isAlive(), "TCP sender did not finish");
			}
			if (failure.get() != null) throw new AssertionError("TCP send failed", failure.get());
		}
	}

	private static void checkRawMode() throws Exception {
		try (Connection connection = new Connection(true)) {
			final String message = "@n@@b@@r@ raw \u20ac";
			connection.service.sendMessage(message);
			final byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
			check(message.equals(new String(connection.peer.getInputStream().readNBytes(bytes.length),
					StandardCharsets.UTF_8)), "Raw output was escaped");
			connection.peer.getOutputStream().write((message + "\n").getBytes(StandardCharsets.UTF_8));
			connection.peer.getOutputStream().flush();
			check(message.equals(connection.service.receive()), "Raw input was unescaped");
			connection.peer.shutdownOutput();
			check(connection.peer.getInputStream().read() == -1, "Raw output appended a line delimiter");
		}
	}

	public static void main(final String[] args) throws Exception {
		checkEscaping();
		checkReadAheadAndEof();
		checkConcurrentSending();
		checkRawMode();
		System.out.println("ClientServiceTest passed");
	}
}
