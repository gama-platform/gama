/*******************************************************************************************************
 *
 * HeadlessExperimentController.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.scope.IScope;

/**
 * Simplified controller for headless experiment execution in GAMA.
 * 
 * <p>
 * This controller is designed for batch experiments, command-line execution, and automated testing where no user
 * interface is present. Unlike {@link DefaultExperimentController}, it provides a minimal implementation with direct
 * execution and no thread management overhead.
 * </p>
 * 
 * <h3>Key Characteristics</h3>
 * <ul>
 * <li><b>Single-threaded:</b> No separate command or execution threads</li>
 * <li><b>Direct execution:</b> Commands execute immediately in calling thread</li>
 * <li><b>Minimal overhead:</b> No locks, queues, or state synchronization</li>
 * <li><b>Automatic completion:</b> Runs until simulation naturally ends or errors</li>
 * <li><b>No pause support:</b> Cannot interactively pause/resume</li>
 * </ul>
 * 
 * <h3>Architecture Comparison</h3>
 * <table border="1">
 * <tr>
 * <th>Feature</th>
 * <th>DefaultExperimentController</th>
 * <th>HeadlessExperimentController</th>
 * </tr>
 * <tr>
 * <td>Threads</td>
 * <td>Command + Execution (2)</td>
 * <td>None (0)</td>
 * </tr>
 * <tr>
 * <td>Pause/Resume</td>
 * <td>Yes</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>Step-by-step</td>
 * <td>Yes</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>Interactive Control</td>
 * <td>Yes</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>Use Case</td>
 * <td>GUI experiments</td>
 * <td>Batch, CLI, tests</td>
 * </tr>
 * </table>
 * 
 * <h3>Execution Model</h3>
 * <p>
 * The {@link #processStart(boolean)} method implements a simple loop:
 * </p>
 * 
 * <pre>
 * <code>
 * // Initialize agent
 * if (!scope.init(agent).passed()) {
 *     return false;
 * }
 * 
 * // Run until completion or error
 * while (scope.step(agent).passed()) {
 *     // Continue stepping
 * }
 * 
 * return true;
 * </code>
 * </pre>
 * 
 * <h3>Command Handling</h3>
 * <p>
 * Most control methods return {@code true} but perform no action:
 * </p>
 * <ul>
 * <li><b>processOpen:</b> Returns true (no-op)</li>
 * <li><b>processPause:</b> Returns true (no-op)</li>
 * <li><b>processStep:</b> Returns true (no-op)</li>
 * <li><b>processBack:</b> Returns true (no-op)</li>
 * <li><b>processReload:</b> Returns true (no-op)</li>
 * <li><b>processStartPause:</b> Returns true (no-op)</li>
 * <li><b>processStart:</b> Actually executes the simulation</li>
 * <li><b>close:</b> Disposes the experiment</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * <code>
 * // Headless experiment setup
 * IExperimentSpecies experiment = ...;
 * HeadlessExperimentController controller = new HeadlessExperimentController(experiment);
 * 
 * // Schedule the agent
 * IExperimentAgent agent = ...;
 * controller.schedule(agent);
 * 
 * // Start execution (blocks until completion)
 * boolean success = controller.processStart(true);
 * 
 * if (success) {
 *     System.out.println("Simulation completed successfully");
 * } else {
 *     System.out.println("Simulation failed or stopped early");
 * }
 * 
 * // Cleanup
 * controller.close();
 * </code>
 * </pre>
 * 
 * <h3>Batch Experiments</h3>
 * <p>
 * Ideal for batch experiments that run multiple simulations:
 * </p>
 * 
 * <pre>
 * <code>
 * experiment batch_exp type: batch {
 *     parameter "population" among: [100, 500, 1000];
 *     parameter "threshold" among: [0.1, 0.5, 0.9];
 *     
 *     method exploration;
 *     
 *     // Runs all combinations headlessly
 * }
 * </code>
 * </pre>
 * 
 * <h3>Error Handling</h3>
 * <p>
 * Exceptions during initialization or stepping:
 * </p>
 * <ul>
 * <li>Caught and reported via {@link GAMA#reportError}</li>
 * <li>Wrapped in {@link GamaRuntimeException} if needed</li>
 * <li>Execution stops and returns {@code false}</li>
 * <li>Scope set to dispose status on init failure</li>
 * </ul>
 * 
 * <h3>Lifecycle</h3>
 * <ol>
 * <li><b>Construction:</b> Stores experiment reference</li>
 * <li><b>Scheduling:</b> Stores agent and initializes it</li>
 * <li><b>Start:</b> Runs simulation loop to completion</li>
 * <li><b>Close:</b> Disposes experiment resources</li>
 * <li><b>Dispose:</b> Nullifies agent reference</li>
 * </ol>
 * 
 * <h3>Scope Handling</h3>
 * <p>
 * Uses the agent's scope for all operations:
 * </p>
 * <ul>
 * <li>No separate controller scope needed</li>
 * <li>Initialization status checked via scope.init()</li>
 * <li>Step execution controlled via scope.step()</li>
 * <li>Errors reported through scope</li>
 * </ul>
 * 
 * <h3>When to Use</h3>
 * <p>
 * Use {@code HeadlessExperimentController} when:
 * </p>
 * <ul>
 * <li>Running batch experiments with parameter exploration</li>
 * <li>Executing simulations from command line</li>
 * <li>Running automated tests</li>
 * <li>No user interaction required</li>
 * <li>Simulation runs to completion without intervention</li>
 * </ul>
 * 
 * <p>
 * Use {@link DefaultExperimentController} when:
 * </p>
 * <ul>
 * <li>Interactive GUI control needed</li>
 * <li>User wants to pause/resume/step</li>
 * <li>Real-time visualization required</li>
 * <li>Exploratory model development</li>
 * </ul>
 * 
 * @see IExperimentController
 * @see DefaultExperimentController
 * @see AbstractExperimentController
 * @see IExperimentAgent
 * @author GAMA Team
 */
public class HeadlessExperimentController implements IExperimentController {

	/** The experiment. */
	private final IExperimentSpecies experiment;

	/** The agent. */
	private IExperimentAgent agent;

	/**
	 * Instantiates a new headless experiment controller.
	 *
	 * @param experiment
	 *            the experiment.
	 */
	public HeadlessExperimentController(final IExperimentSpecies experiment) {
		this.experiment = experiment;
	}

	@Override
	public IExperimentSpecies getExperiment() { return experiment; }

	@Override
	public void close() {
		experiment.dispose(); // will call own dispose() later
	}

	@Override
	public void schedule(final IExperimentAgent agent) {
		this.agent = agent;
		final IScope scope = agent.getScope();
		if (scope == null) return;
		try {
			if (!scope.init(agent).passed()) { scope.setDisposeStatus(); }
		} catch (final Throwable e) {
			if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
		}
	}

	@Override
	public void dispose() {
		agent = null;
	}

	@Override
	public boolean processOpen(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processPause(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processReload(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processStep(final int nbSteps, final boolean andWait) {
		return true;
	}

	@Override
	public boolean processBack(final int nbSteps, final boolean andWait) {
		return true;
	}

	@Override
	public boolean processStartPause(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processStart(final boolean andWait) {
		if (agent == null) return false;
		final IScope scope = agent.getScope();
		if (scope == null) return false;
		try {
			while (scope.step(agent).passed()) {}
		} catch (final Throwable e) {
			if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
			return false;
		}
		return true;
	}
}
