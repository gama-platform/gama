/*******************************************************************************************************
 *
 * IsolatedTestRunner.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.FrameworkUtil;

import gama.api.GAMA;
import gama.api.kernel.species.IModelSpecies;
import gama.api.types.color.GamaColorFactory;
import gama.api.ui.IGui;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/** Reuses an isolated JVM across a sequence; unresponsive experiments cause the worker to be replaced. */
public final class IsolatedTestRunner {

	private static final AtomicReference<IsolatedTestRunner> ACTIVE = new AtomicReference<>();
	private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();
	private volatile boolean interrupted;
	private TestExperimentSummary summary;
	private String currentTest;
	private final java.util.Set<String> finishedTests = new java.util.HashSet<>();
	private boolean completed;
	private long started;
	private int seconds = GamaPreferences.Runtime.TEST_TIMEOUT.getValue();
	private final boolean useUIConsole = TestExperimentRunner.usesUIConsole();

	private IsolatedTestRunner(final String model, final String experiment) {
		summary = new TestExperimentSummary(experiment + " in " + Path.of(model).getFileName(),
				URI.createFileURI(model));
	}

	public static boolean isRunning() { return ACTIVE.get() != null; }

	/** Owns a warm worker for a sequence. Nested callers borrow it without closing their caller's worker. */
	public static Session openSession() {
		final Session existing = SESSION.get();
		final Session session = new Session(existing);
		if (existing == null) { SESSION.set(session); }
		return session;
	}

	public static final class Session implements AutoCloseable {

		private final Session borrowed;
		private Path directory;
		private Process process;
		private RandomAccessFile input;
		private DataOutputStream requests;

		private Session(final Session borrowed) { this.borrowed = borrowed; }

		private Session owner() { return borrowed == null ? this : borrowed.owner(); }

		private void request(final String model, final String experiment, final boolean useUIConsole) throws IOException {
			if (process == null || !process.isAlive()) {
				discard();
				directory = Files.createTempDirectory("gama-test-");
				final Path events = Files.createFile(directory.resolve("events"));
				process = new ProcessBuilder(command(directory)).redirectErrorStream(true)
						.redirectOutput(directory.resolve("worker.log").toFile()).start();
				input = new RandomAccessFile(events.toFile(), "r");
				requests = new DataOutputStream(process.getOutputStream());
			}
			TestProcessProtocol.writeString(requests, model);
			TestProcessProtocol.writeString(requests, experiment);
			requests.writeBoolean(useUIConsole);
			requests.writeInt(GamaPreferences.Runtime.TEST_TIMEOUT.getValue());
			requests.flush();
		}

		private void discard() {
			if (process != null) { terminate(process); }
			try {
				if (input != null) { input.close(); }
				if (requests != null) { requests.close(); }
			} catch (final IOException e) {
				DEBUG.ERR("Unable to close test worker streams", e);
			} finally {
				if (directory != null) { clean(directory); }
				directory = null;
				process = null;
				input = null;
				requests = null;
			}
		}

		@Override
		public void close() {
			if (borrowed != null) return;
			try {
				discard();
			} finally {
				SESSION.remove();
			}
		}
	}

	public static String modelPath(final IModelSpecies model) {
		final URI uri = model.getURI();
		final String path;
		if (uri.isPlatformResource()) {
			final var file = GAMA.getWorkspaceManager().getRoot()
					.getFile(new org.eclipse.core.runtime.Path(uri.toPlatformString(true)));
			if (file.getLocation() == null)
				throw new IllegalArgumentException("Test model has no local file: " + uri);
			path = file.getLocation().toOSString();
		} else {
			path = uri.isFile() ? uri.toFileString() : model.getFilePath();
		}
		if (!Files.isRegularFile(Path.of(path)))
			throw new IllegalArgumentException("Isolated tests require a saved local model: " + path);
		return Path.of(path).toAbsolutePath().toString();
	}

	/** Nonblocking: the supervisor asks for cooperative closure, then kills an unresponsive worker. */
	public static void interrupt() {
		final var runner = ACTIVE.get();
		if (runner != null) { runner.interrupted = true; }
	}

	public static TestExperimentSummary run(final String model, final String experiment, final IGui gui) {
		final var runner = new IsolatedTestRunner(model, experiment);
		if (!ACTIVE.compareAndSet(null, runner)) throw new IllegalStateException("A test experiment is already running");
		try (final Session session = openSession()) {
			return runner.execute(model, experiment, gui, session.owner());
		} finally {
			ACTIVE.compareAndSet(runner, null);
		}
	}

	private TestExperimentSummary execute(final String model, final String experiment, final IGui gui,
			final Session session) {
		if (interrupted || gui.isTestStopRequested()) {
			markTermination(TestState.INTERRUPTED, "Interrupted by user", null);
			return summary;
		}
		TestState termination = null;
		String terminationMessage = null, terminatedTest = null;
		try {
			session.request(model, experiment, useUIConsole);
			final Path stop = session.directory.resolve("stop");
			final Path log = session.directory.resolve("worker.log");
			final Process process = session.process;
			final long startup = System.nanoTime();
			final RandomAccessFile input = session.input;
			while (true) {
				readEvents(input, gui);
				if (completed) break;
				final boolean stopped = interrupted || gui.isTestStopRequested()
						|| Thread.currentThread().isInterrupted();
				final boolean timedOut =
						started != 0 && System.nanoTime() - started >= TimeUnit.SECONDS.toNanos(seconds);
				if (stopped || timedOut) {
					terminatedTest = currentTest;
					termination = stopped ? TestState.INTERRUPTED : TestState.TIMED_OUT;
					terminationMessage = stopped ? "Interrupted by user" : "Timeout after " + seconds + " seconds";
					Files.writeString(stop, "interrupt");
					// Give scopes and interruptible blocking operations a brief chance to unwind.
					process.waitFor(250, TimeUnit.MILLISECONDS);
					terminate(process);
					readEvents(input, gui);
					break;
				}
				if (!process.isAlive()) {
					readEvents(input, gui);
					if (!completed) throw new IOException("Test worker exited with code " + process.exitValue()
							+ " without a result.\n" + logTail(log));
					break;
				}
				if (started == 0 && System.nanoTime() - startup >= TimeUnit.SECONDS.toNanos(120))
					throw new IOException("Test worker startup or compilation exceeded 120 seconds.\n" + logTail(log));
				Thread.sleep(20);
			}
		} catch (final InterruptedException e) {
			if (termination == null) {
				termination = TestState.INTERRUPTED;
				terminationMessage = "Test runner interrupted";
				terminatedTest = currentTest;
			}
			Thread.currentThread().interrupt();
		} catch (final IOException | RuntimeException e) {
			DEBUG.ERR("Unable to run isolated test experiment " + experiment, e);
			if (termination == null) { markTermination(TestState.ABORTED, AbstractSummary.errorWithTrace(e), currentTest); }
			if (currentTest != null && finishedTests.add(currentTest)) {
				gui.displayTestSummary(GAMA.getRuntimeScope(), summary,
						summary.summaries.get(currentTest), true);
			}
		} finally {
			if (!completed || termination != null) { session.discard(); }
			if (termination != null) { markTermination(termination, terminationMessage, terminatedTest); }
			if (terminatedTest != null && termination != null && finishedTests.add(terminatedTest)) {
				gui.displayTestSummary(GAMA.getRuntimeScope(), summary,
						summary.summaries.get(terminatedTest), true);
			}
		}
		return summary;
	}

	private void readEvents(final RandomAccessFile file, final IGui gui) throws IOException {
		final long available = file.length();
		while (available - file.getFilePointer() >= Integer.BYTES) {
			final long position = file.getFilePointer();
			final int length = file.readInt();
			if (length < Integer.BYTES || length > 64 * 1024 * 1024)
				throw new IOException("Invalid test worker event size: " + length);
			if (available - file.getFilePointer() < length) {
				file.seek(position);
				return;
			}
			final byte[] bytes = new byte[length];
			file.readFully(bytes);
			try (var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
				switch (in.readInt()) {
					case TestProcessProtocol.READY -> started = System.nanoTime();
					case TestProcessProtocol.TIMEOUT -> {
						seconds = in.readInt();
						if (seconds < 1) throw new IOException("Invalid test timeout: " + seconds);
					}
					case TestProcessProtocol.SUMMARY -> summary = TestProcessProtocol.readExperiment(in);
					case TestProcessProtocol.RUNNING ->
						gui.displayRunningTest(GAMA.getRuntimeScope(), TestProcessProtocol.readString(in));
					case TestProcessProtocol.TEST -> {
						final String key = TestProcessProtocol.readString(in);
						final boolean finished = in.readBoolean();
						summary.summaries.put(key, TestProcessProtocol.readTest(in));
						currentTest = finished ? null : key;
						if (!finished || finishedTests.add(key)) {
							gui.displayTestSummary(GAMA.getRuntimeScope(), summary, summary.summaries.get(key),
									finished);
						}
					}
					case TestProcessProtocol.RESULT -> {
						summary = TestProcessProtocol.readExperiment(in);
						completed = true;
						return;
					}
					case TestProcessProtocol.ERROR -> throw new IOException(TestProcessProtocol.readString(in));
					case TestProcessProtocol.CONSOLE -> {
						final String message = TestProcessProtocol.readString(in);
						final var color = in.readBoolean() ? GamaColorFactory.get(in.readInt()) : null;
						if (useUIConsole) {
							gui.getConsole().informConsole(message, GAMA.getPlatformAgent(), color);
						}
					}
					default -> throw new IOException("Unknown test worker event");
				}
			}
		}
	}

	private void markTermination(final TestState state, final String message, final String activeTest) {
		summary.setState(state);
		summary.setError(message);
		IndividualTestSummary test = activeTest == null ? null : summary.summaries.get(activeTest);
		if (test == null) {
			test = new IndividualTestSummary(started == 0 || summary.isEmpty()
					? "Experiment startup/initialization" : "Experiment initialization/execution", summary.getURI());
			summary.addSummary(test);
		}
		if (test != null) {
			test.setState(state);
			test.setError(message);
		}
	}

	private static List<String> command(final Path directory)
			throws IOException {
		final var launcher = Platform.getBundle("org.eclipse.equinox.launcher");
		if (Platform.getBundle("gama.headless") == null)
			throw new IOException("Isolated tests require the gama.headless bundle");
		final String launcherPath = launcher == null ? launcherOnClasspath()
				: FileLocator.getBundleFile(launcher).getAbsolutePath();
		final Path configuration = Files.createDirectory(directory.resolve("configuration"));
		final Properties config = new Properties();
		final List<String> bundles = new ArrayList<>();
		for (final var bundle : FrameworkUtil.getBundle(IsolatedTestRunner.class).getBundleContext().getBundles()) {
			if (bundle.getBundleId() == 0) continue;
			String location = bundle.getLocation();
			if (location.startsWith("initial@")) { location = location.substring("initial@".length()); }
			final String name = bundle.getSymbolicName();
			if ("org.eclipse.core.runtime".equals(name) || "org.eclipse.equinox.common".equals(name)
					|| "org.apache.felix.scr".equals(name) || "org.eclipse.equinox.event".equals(name)) {
				location += "@2:start";
			}
			bundles.add(location);
		}
		config.setProperty("osgi.bundles", String.join(",", bundles));
		config.setProperty("osgi.bundles.defaultStartLevel", "4");
		config.setProperty("osgi.framework", FileLocator.getBundleFile(Platform.getBundle("org.eclipse.osgi"))
				.toURI().toString());
		config.setProperty("osgi.framework.beginningstartlevel", "6");
		config.setProperty("osgi.instance.area", directory.resolve("workspace").toUri().toString());
		final var parentConfig = Platform.getConfigurationLocation().getURL();
		final String dev = System.getProperty("osgi.dev");
		if (dev != null) {
			config.setProperty("osgi.dev", dev);
		} else {
			final var devFile = new java.net.URL(parentConfig, "dev.properties");
			if ("file".equals(devFile.getProtocol()) && Files.exists(Path.of(java.net.URI.create(devFile.toString())))) {
				config.setProperty("osgi.dev", devFile.toString());
			}
		}
		try (var out = Files.newOutputStream(configuration.resolve("config.ini"))) { config.store(out, null); }
		final String executable = Platform.OS_WIN32.equals(Platform.getOS()) ? "java.exe" : "java";
		final var command = new ArrayList<String>();
		command.add(Path.of(System.getProperty("java.home"), "bin", executable).toString());
		// GAML extensions use vector APIs and reflective access configured by the product launcher.
		for (final String argument : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			if (argument.startsWith("--add-opens=") || argument.startsWith("--add-exports=")
					|| argument.startsWith("--add-modules=") || argument.startsWith("--enable-native-access=")) {
				command.add(argument);
			}
		}
		command.addAll(List.of("--add-modules=jdk.incubator.vector",
				"--enable-native-access=ALL-UNNAMED", "-Djava.awt.headless=true",
				"-Djava.util.prefs.userRoot=" + directory.resolve("preferences"),
				"-Xmx" + Runtime.getRuntime().maxMemory(), "-cp", launcherPath,
				"org.eclipse.equinox.launcher.Main", "-install", Platform.getInstallLocation().getURL().toString(),
				"-configuration", configuration.toString(), "-data", directory.resolve("workspace").toString(),
				"-application", "gama.headless.testWorker", directory.toString()));
		return command;
	}

	private static String launcherOnClasspath() throws IOException {
		for (final String entry : System.getProperty("java.class.path", "")
				.split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
			if (entry.isBlank()) continue;
			final Path path = Path.of(entry);
			if (path.getFileName().toString().startsWith("org.eclipse.equinox.launcher")
					&& path.toString().endsWith(".jar") && Files.isRegularFile(path)) {
				return path.toAbsolutePath().toString();
			}
		}
		final Path plugins = Path.of(java.net.URI.create(Platform.getInstallLocation().getURL().toString()))
				.resolve("plugins");
		if (Files.isDirectory(plugins)) {
			try (var paths = Files.list(plugins)) {
				final var jar = paths.filter(path -> path.getFileName().toString().startsWith("org.eclipse.equinox.launcher_")
						&& path.toString().endsWith(".jar")).sorted(Comparator.reverseOrder()).findFirst();
				if (jar.isPresent()) return jar.get().toAbsolutePath().toString();
			}
		}
		throw new IOException("Cannot locate the Equinox launcher for isolated tests");
	}

	private static String logTail(final Path log) throws IOException {
		try (var file = new RandomAccessFile(log.toFile(), "r")) {
			file.seek(Math.max(0, file.length() - 8192));
			final byte[] bytes = new byte[(int) (file.length() - file.getFilePointer())];
			file.readFully(bytes);
			return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
		}
	}

	private static void terminate(final Process process) {
		process.descendants().forEach(ProcessHandle::destroyForcibly);
		if (process.isAlive()) { process.destroyForcibly(); }
		// Do not dispose shared resources or start the next experiment until the worker is actually dead.
		boolean interrupted = false;
		while (process.isAlive()) {
			try {
				process.waitFor();
			} catch (final InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) { Thread.currentThread().interrupt(); }
	}

	private static void clean(final Path directory) {
		try (var paths = Files.walk(directory)) {
			for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
		} catch (final IOException e) {
			DEBUG.ERR("Unable to remove test worker files in " + directory, e);
		}
	}
}
