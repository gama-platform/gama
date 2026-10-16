/*******************************************************************************************************
 *
 * TestWorkerApplication.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.runtime;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.emf.common.util.URI;

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gama.api.gaml.statements.IStatement;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IGamaView;
import gama.api.ui.NullGuiHandler;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.tests.IndividualTestSummary;
import gama.api.utils.tests.TestProcessProtocol;
import gama.api.utils.tests.TestExperimentSummary;
import gama.core.experiment.TestAgent;
import gama.core.experiment.parameters.ParametersSet;
import gama.dev.DEBUG;
import gaml.compiler.validation.GamlModelBuilder;

/** Sequential experiments in a warm, killable JVM, with incremental reports sent to the workbench. */
public final class TestWorkerApplication implements IApplication {

	private volatile IExperimentSpecies experiment;
	private volatile boolean stopped;
	private int publishedTimeout;
	private volatile IOException timeoutPublicationFailure;

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (args.length != 1) throw new IllegalArgumentException("Expected report directory");
		final Path directory = Path.of(args[0]);
		try (var events = Files.newOutputStream(directory.resolve("events"));
				var requests = new DataInputStream(System.in)) {
			try {
				HeadlessApplication.getInjector();
				final var gui = new WorkerGui(events);
				GAMA.setHeadlessGui(gui);
				final Thread execution = Thread.currentThread();
				final Thread watcher =
						new Thread(() -> watch(directory.resolve("stop"), execution, events), "Test interruption");
				watcher.setDaemon(true);
				watcher.start();
				while (!stopped) {
					final String path;
					try {
						path = TestProcessProtocol.readString(requests);
					} catch (final EOFException e) {
						break;
					}
					final String name = TestProcessProtocol.readString(requests);
					gui.useUIConsole = requests.readBoolean();
					GamaPreferences.Runtime.TEST_TIMEOUT.set(requests.readInt());
					run(path, name, events);
				}
				return IApplication.EXIT_OK;
			} catch (final Exception e) {
				TestProcessProtocol.send(events, TestProcessProtocol.ERROR,
						out -> TestProcessProtocol.writeString(out, gama.api.utils.tests.AbstractSummary.errorWithTrace(e)));
				throw e;
			}
		} finally {
			stopped = true;
		}
	}

	private void run(final String path, final String name, final OutputStream events) throws IOException {
		// Experiment disposal releases its compiled statements; each request needs a fresh model instance.
		final var errors = new ArrayList<GamlCompilationError>();
		final var model = GamlModelBuilder.getInstance().compile(URI.createFileURI(path), errors);
		if (model == null || errors.stream().anyMatch(GamlCompilationError::isError))
			throw new IllegalArgumentException("Cannot compile test model " + path + ": " + errors);
		final var plan = model.getExperiment(name);
		if (plan == null || !plan.isTest()) throw new IllegalArgumentException("Not a test experiment: " + name);
		final var initialSummary = new TestExperimentSummary(name + " in " + Path.of(path).getFileName(),
				URI.createFileURI(path));
		model.getAllTests().forEach(test -> initialSummary.addSummary(test.getSummary()));
		plan.getBehaviors().forEach(statement -> {
			if (statement instanceof IStatement.Test test) { initialSummary.addSummary(test.getSummary()); }
		});
		initialSummary.reset();
		TestProcessProtocol.send(events, TestProcessProtocol.READY, out -> {});
		TestProcessProtocol.send(events, TestProcessProtocol.SUMMARY,
				out -> TestProcessProtocol.writeSummary(out, initialSummary));
		final TestExperimentSummary summary;
		try {
			synchronized (this) {
				experiment = plan;
				publishedTimeout = 0;
				publishTimeout(events);
			}
			GAMA.addHeadlessExperiment(model, name, new ParametersSet(), null);
			final var agent = (TestAgent) experiment.getAgent();
			if (!stopped) { agent.init(agent.getScope()); }
			if (!stopped) { agent.step(agent.getScope()); }
			summary = agent.getSummary();
		} finally {
			synchronized (this) { experiment = null; }
			GAMA.closeExperiment(plan);
		}
		if (timeoutPublicationFailure != null) throw timeoutPublicationFailure;
		TestProcessProtocol.send(events, TestProcessProtocol.RESULT,
				out -> TestProcessProtocol.writeSummary(out, summary));
	}

	private synchronized void publishTimeout(final OutputStream events) throws IOException {
		if (experiment == null) return;
		final int seconds = GamaPreferences.Runtime.TEST_TIMEOUT.getValue();
		if (seconds == publishedTimeout) return;
		TestProcessProtocol.send(events, TestProcessProtocol.TIMEOUT, out -> out.writeInt(seconds));
		publishedTimeout = seconds;
	}

	private void watch(final Path stop, final Thread execution, final OutputStream events) {
		try {
			while (!stopped && !Files.exists(stop)) {
				publishTimeout(events);
				Thread.sleep(20);
			}
			if (stopped) return;
			stopped = true;
			final var current = experiment;
			if (current != null && current.getAgent() != null) {
				current.getAgent().getScope().setDisposeStatus();
				current.getAgent().closeSimulations(false);
			}
			execution.interrupt();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final IOException e) {
			timeoutPublicationFailure = e;
			DEBUG.ERR("Cannot publish test timeout", e);
			stopped = true;
			execution.interrupt();
		}
	}

	@Override
	public void stop() { stopped = true; }

	private final class WorkerGui extends NullGuiHandler {

		private final OutputStream events;
		private volatile boolean useUIConsole;
		private final IConsoleListener uiConsole = (message, root, color) -> {
			final var resolvedColor = color == null && root != null ? root.getColor() : color;
			send(TestProcessProtocol.CONSOLE, out -> {
				TestProcessProtocol.writeString(out, message);
				out.writeBoolean(resolvedColor != null);
				if (resolvedColor != null) { out.writeInt(resolvedColor.getRGB()); }
			});
		};

		WorkerGui(final OutputStream events) { this.events = events; }

		@Override
		public IConsoleListener getConsole() { return useUIConsole ? uiConsole : super.getConsole(); }

		@Override
		public boolean isTestStopRequested() { return stopped; }

		@Override
		public IGamaView.Test openTestView(final IScope scope, final boolean allTests) {
			if (scope.getExperiment() instanceof IExperimentAgent.Test agent) {
				send(TestProcessProtocol.SUMMARY, out -> TestProcessProtocol.writeSummary(out, agent.getSummary()));
			}
			return null;
		}

		@Override
		public void displayRunningTest(final IScope scope, final String name) {
			final String title = scope.getExperiment() instanceof IExperimentAgent.Test agent
					? agent.getTitleForSummary() : "";
			send(TestProcessProtocol.RUNNING,
					out -> TestProcessProtocol.writeString(out, title.equals(name) ? name : title + " - " + name));
		}

		@Override
		public void displayTestSummary(final IScope scope, final IndividualTestSummary summary, final boolean finished) {
			if (!(scope.getExperiment() instanceof IExperimentAgent.Test agent)) return;
			final String key = agent.getSummary().summaries.entrySet().stream()
					.filter(entry -> entry.getValue() == summary).map(java.util.Map.Entry::getKey).findFirst()
					.orElseThrow(() -> new IllegalStateException("Test missing from experiment summary: " + summary.getTitle()));
			send(TestProcessProtocol.TEST, out -> {
				TestProcessProtocol.writeString(out, key);
				out.writeBoolean(finished);
				TestProcessProtocol.writeSummary(out, summary);
			});
		}

		private void send(final int event, final TestProcessProtocol.Payload payload) {
			try {
				TestProcessProtocol.send(events, event, payload);
			} catch (final IOException e) {
				throw new UncheckedIOException("Cannot publish test results", e);
			}
		}
	}
}
