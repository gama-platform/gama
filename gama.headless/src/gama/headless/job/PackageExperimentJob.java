/*******************************************************************************************************
 *
 * PackageExperimentJob.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.job;

import java.io.IOException;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;

/**
 * An experiment job that reports how it ended, so that running an archive can fail when the simulation does. It runs
 * until the simulation reaches a given cycle or, without one, until the {@code until:} condition of the experiment is
 * true. It stops at the first runtime error reported with {@link #fail(Throwable)}.
 */
public final class PackageExperimentJob extends ExperimentJob {

	/** The cycle at which the job stops, or a negative number to run until the experiment stops. */
	private final long untilCycle;

	/** The exception that ended the job, if any. */
	private volatile Throwable failure;

	/** The number of steps executed. */
	private volatile long executed;

	/** The cycle of the simulation when the job stopped. */
	private volatile long cycle;

	/** Why the job stopped. */
	private volatile String stopReason = "";

	/**
	 * Creates a job running the same experiment as {@code job}.
	 *
	 * @param job
	 *            a job built by {@link JobListFactory}
	 * @param untilCycle
	 *            the cycle at which to stop, or a negative number to run until the experiment stops
	 */
	public PackageExperimentJob(final ExperimentJob job, final long untilCycle) {
		super(job);
		this.untilCycle = untilCycle;
		setFinalStep(-1);
	}

	@Override
	public void loadAndBuild() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			IOException, GamaCompilationFailedException {
		super.loadAndBuild();
		// The legacy job replaces the stop condition of the experiment by its own (empty) one: keep the model's
		final IExpression until = simulator.getExperimentPlan().getFacet(IKeyword.UNTIL);
		if (until != null) { simulator.getExperimentPlan().setStopCondition(until); }
	}

	/** Records the first error of the simulation; the job stops before its next step. */
	public void fail(final Throwable error) {
		if (failure == null) { failure = error; }
	}

	@Override
	public void run() {
		try (final DebugStream file = new DebugStream(this)) {
			loadAndBuild();
			playAndDispose();
		} catch (final Exception | LinkageError e) {
			fail(e);
		}
	}

	@Override
	public void play() {
		if (this.outputFile != null) { this.outputFile.writeSimulationHeader(this); }
		try {
			while (true) {
				if (failure != null) break;
				if (simulator.isInterrupted()) {
					stopReason = "the experiment was interrupted";
					break;
				}
				final ISimulationAgent sim = simulator.getSimulation();
				final IExperimentAgent exp = simulator.getExperimentPlan().getAgent();
				final IScope scope = sim == null ? exp.getScope() : sim.getScope();
				// The cycle of the simulation itself, not a count of the steps run by this job
				cycle = sim == null ? executed : sim.getCycle(scope);
				if (untilCycle >= 0 && cycle >= untilCycle) {
					stopReason = "it reached cycle " + untilCycle;
					break;
				}
				if (Cast.asBool(scope, exp.getStopCondition().value(scope))) {
					stopReason = "the until: condition of the experiment is true";
					break;
				}
				doStep();
				executed++;
			}
		} catch (final RuntimeException e) {
			fail(e);
		}
	}

	/** The exception that ended the job, or null if it ended normally. */
	public Throwable getFailure() { return failure; }

	/** The number of steps executed. */
	public long getExecutedSteps() { return executed; }

	/** The cycle of the simulation when the job stopped. */
	public long getCycle() { return cycle; }

	/** Why the job stopped. */
	public String getStopReason() { return stopReason; }
}
