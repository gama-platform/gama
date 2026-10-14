/*******************************************************************************************************
 *
 * TestExperimentRunner.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import gama.api.GAMA;
import gama.api.gaml.types.Types;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.api.ui.IGui;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/** Same-JVM execution waits for cooperative cancellation before disposing resources or running another test. */
public final class TestExperimentRunner {

	private static final AtomicReference<TestExperimentRunner> ACTIVE = new AtomicReference<>();
	private final Thread execution = Thread.currentThread();
	private final IExperimentSpecies experiment;
	private final int seconds = GamaPreferences.Runtime.TEST_TIMEOUT.getValue();
	private volatile TestState termination;
	private IndividualTestSummary currentTest;
	private IScope currentScope;
	private boolean finished;

	private TestExperimentRunner(final IExperimentSpecies experiment) { this.experiment = experiment; }

	public static int countTests(final IModelSpecies model, final String name) {
		return model.getAllTests().size() + (int) model.getExperiment(name).getBehaviors().stream()
				.filter(statement -> statement instanceof WithTestSummary<?>).count();
	}

	public static void interrupt() {
		IsolatedTestRunner.interrupt();
		final var runner = ACTIVE.get();
		if (runner != null) { runner.cancel(TestState.INTERRUPTED); }
	}

	public static TestExperimentSummary run(final IModelSpecies model, final String name, final IGui gui) {
		if (GamaPreferences.Experimental.ISOLATED_TESTS.getValue())
			return IsolatedTestRunner.run(IsolatedTestRunner.modelPath(model), name, gui);
		final var runner = new TestExperimentRunner(model.getExperiment(name));
		if (!ACTIVE.compareAndSet(null, runner)) throw new IllegalStateException("A test experiment is already running");
		try {
			return runner.execute(model, name, gui);
		} finally {
			ACTIVE.compareAndSet(runner, null);
		}
	}

	static void entering(final IScope scope, final IndividualTestSummary summary) {
		final var runner = ACTIVE.get();
		if (runner == null || runner.execution != Thread.currentThread()) return;
		synchronized (runner) {
			runner.currentScope = scope;
			runner.currentTest = summary;
			if (runner.termination != null) { scope.setDisposeStatus(); }
		}
	}

	static void leaving(final IndividualTestSummary summary) {
		final var runner = ACTIVE.get();
		if (runner == null || runner.execution != Thread.currentThread()) return;
		synchronized (runner) {
			if (runner.termination != null) {
				summary.setState(runner.termination);
				summary.setError(runner.message());
			} else {
				runner.currentTest = null;
				runner.currentScope = null;
			}
		}
	}

	private synchronized void cancel(final TestState state) {
		if (finished) return;
		if (termination == null || state == TestState.INTERRUPTED) { termination = state; }
		if (currentScope != null) { currentScope.setDisposeStatus(); }
		final var agent = experiment.getAgent();
		if (agent != null) {
			agent.getScope().setDisposeStatus();
			agent.closeSimulations(false);
		}
		execution.interrupt();
	}

	private String message() {
		return termination == TestState.TIMED_OUT ? "Timeout after " + seconds + " seconds" : "Interrupted by user";
	}

	private TestExperimentSummary execute(final IModelSpecies model, final String name, final IGui gui) {
		final var watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
			final var thread = new Thread(r, "Test experiment timeout");
			thread.setDaemon(true);
			return thread;
		});
		TestExperimentSummary result = null;
		try {
			watchdog.schedule(() -> cancel(TestState.TIMED_OUT), seconds, TimeUnit.SECONDS);
			if (gui.isTestStopRequested()) { cancel(TestState.INTERRUPTED); }
			if (termination == null) {
				GAMA.addHeadlessExperiment(model, name, GamaMapFactory.create(Types.STRING, Types.NO_TYPE), null);
				final var agent = (IExperimentAgent.Test) experiment.getAgent();
				result = agent.getSummary();
				if (termination == null) { agent.init(agent.getScope()); }
				if (termination == null && !gui.isTestStopRequested()) { agent.step(agent.getScope()); }
			}
		} catch (final RuntimeException e) {
			if (termination == null) {
				DEBUG.ERR("Test experiment failed: " + name, e);
				if (result == null) { result = new TestExperimentSummary(name, model.getURI()); }
				result.setState(TestState.ABORTED);
				result.setErrorWithTrace(e);
			}
		} finally {
			synchronized (this) {
				finished = true;
				if (result == null && experiment.getAgent() instanceof IExperimentAgent.Test agent) {
					result = agent.getSummary();
				}
				if (result == null) { result = new TestExperimentSummary(name, model.getURI()); }
				if (termination != null) {
					result.setState(termination);
					result.setError(message());
					if (currentTest == null) {
						currentTest = new IndividualTestSummary("Experiment initialization/execution", model.getURI());
						result.addSummary(currentTest);
					}
					currentTest.setState(termination);
					currentTest.setError(message());
				}
			}
			watchdog.shutdownNow();
			// Consume only the interruption issued by this runner; later experiments must start normally.
			if (termination != null) { Thread.interrupted(); }
			GAMA.closeExperiment(experiment);
		}
		return result;
	}
}
