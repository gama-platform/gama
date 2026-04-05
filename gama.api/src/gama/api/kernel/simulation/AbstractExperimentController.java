/*******************************************************************************************************
 *
 * AbstractExperimentController.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.concurrent.ArrayBlockingQueue;

import gama.api.GAMA;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.GeneralSynchronizer;
import gama.api.runtime.scope.IScope;
import gama.api.utils.server.IServerConfiguration;
import gama.dev.DEBUG;

/**
 * Abstract base class for experiment controllers in GAMA.
 * 
 * <p>
 * This class provides the core infrastructure for controlling experiment execution through a command-based
 * architecture. It manages the experiment lifecycle, command processing, synchronization, and state management in a
 * thread-safe manner.
 * </p>
 * 
 * <h3>Architecture Overview</h3>
 * <p>
 * The controller uses a dual-thread architecture:
 * </p>
 * <ul>
 * <li><b>Command Thread:</b> Processes user commands from a blocking queue</li>
 * <li><b>Execution Thread:</b> Runs the simulation steps (subclass responsibility)</li>
 * </ul>
 * 
 * <h3>Command Processing</h3>
 * <p>
 * Commands can be processed in two modes:
 * </p>
 * <ul>
 * <li><b>Asynchronous:</b> Commands are queued and return immediately</li>
 * <li><b>Synchronous:</b> Commands execute directly and block until completion</li>
 * </ul>
 * 
 * <h3>Supported Commands</h3>
 * <table border="1">
 * <tr>
 * <th>Command</th>
 * <th>Description</th>
 * <th>Async/Sync</th>
 * </tr>
 * <tr>
 * <td>_OPEN</td>
 * <td>Initialize and open the experiment</td>
 * <td>Both</td>
 * </tr>
 * <tr>
 * <td>_START</td>
 * <td>Start continuous execution</td>
 * <td>Both</td>
 * </tr>
 * <tr>
 * <td>_PAUSE</td>
 * <td>Pause execution</td>
 * <td>Both</td>
 * </tr>
 * <tr>
 * <td>_STEP</td>
 * <td>Execute a single step</td>
 * <td>Both</td>
 * </tr>
 * <tr>
 * <td>_BACK</td>
 * <td>Step backward (if supported)</td>
 * <td>Both</td>
 * </tr>
 * <tr>
 * <td>_RELOAD</td>
 * <td>Reload the experiment</td>
 * <td>Both</td>
 * </tr>
 * <tr>
 * <td>_CLOSE</td>
 * <td>Close and dispose the experiment</td>
 * <td>Async only</td>
 * </tr>
 * </table>
 * 
 * <h3>State Management</h3>
 * <p>
 * The controller maintains several volatile flags for thread-safe state management:
 * </p>
 * <ul>
 * <li><b>experimentAlive:</b> Whether the experiment is still running</li>
 * <li><b>paused:</b> Whether execution is currently paused</li>
 * <li><b>acceptingCommands:</b> Whether new commands are being accepted</li>
 * <li><b>disposing:</b> Whether the controller is being disposed (volatile; observed promptly by all threads)</li>
 * </ul>
 * 
 * <h3>Synchronization Mechanism</h3>
 * <p>
 * Uses {@link GeneralSynchronizer} objects to coordinate threads:
 * </p>
 * <ul>
 * <li><b>lock:</b> Controls pause/resume of execution</li>
 * <li><b>previouslock:</b> Used for step-by-step execution synchronization</li>
 * </ul>
 * 
 * <h3>Usage in Subclasses</h3>
 * <p>
 * Subclasses must implement {@link #processUserCommand(ExperimentCommand)} to handle commands according to their
 * execution model:
 * </p>
 * <ul>
 * <li>{@link DefaultExperimentController} - For GUI-based experiments with interactive control</li>
 * <li>{@link HeadlessExperimentController} - For batch/headless execution without UI</li>
 * </ul>
 * 
 * <h3>Example Subclass Implementation</h3>
 * 
 * <pre>
 * <code>
 * public class MyController extends AbstractExperimentController {
 *     
 *     protected boolean processUserCommand(ExperimentCommand command) {
 *         switch (command) {
 *             case _START:
 *                 paused = false;
 *                 lock.release();
 *                 return true;
 *             case _PAUSE:
 *                 paused = true;
 *                 return true;
 *             // ... handle other commands
 *         }
 *         return false;
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Thread Safety</h3>
 * <p>
 * This class is designed for concurrent access:
 * </p>
 * <ul>
 * <li>State flags are declared volatile</li>
 * <li>Command queue is thread-safe (ArrayBlockingQueue)</li>
 * <li>Synchronizers provide proper memory barriers</li>
 * <li>Offer pattern prevents blocking on queue full</li>
 * </ul>
 * 
 * <h3>Lifecycle</h3>
 * <ol>
 * <li><b>Construction:</b> Command thread starts, accepting commands</li>
 * <li><b>Operation:</b> Commands queued and processed continuously</li>
 * <li><b>Disposal:</b> Flags set, threads signaled, resources released</li>
 * </ol>
 * 
 * <h3>Server Configuration</h3>
 * <p>
 * Supports server-based experiments through {@link IServerConfiguration}, allowing remote control and headless
 * operation in server environments.
 * </p>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 24 oct. 2023
 * @see IExperimentController
 * @see DefaultExperimentController
 * @see HeadlessExperimentController
 * @see ExperimentCommand
 */
public abstract class AbstractExperimentController implements IExperimentController {

	/** The scope. Volatile so writes from schedule() on the calling thread are immediately visible to the execution thread. */
	protected volatile IScope scope;

	/** The disposing. Flag set when the controller is being shut down. Must be volatile so all threads observe it promptly. */
	protected volatile boolean disposing;

	/** The server configuration. */
	protected IServerConfiguration serverConfiguration;

	/**
	 * Alive. Flag indicating that the scheduler is running (it should be alive unless the application is shutting down)
	 */
	protected volatile boolean experimentAlive = true;

	/**
	 * Paused. Flag indicating that the experiment is set to pause (used in stepping the experiment)
	 **/
	protected volatile boolean paused = true;

	/** AcceptingCommands. A flag indicating that the command thread is accepting commands */
	protected volatile boolean acceptingCommands = true;

	/** The lock. Used to pause the experiment */
	protected final GeneralSynchronizer lock = GeneralSynchronizer.withInitialAndMaxPermits(1, 1);

	/** The previouslock. */
	protected final GeneralSynchronizer previouslock = GeneralSynchronizer.withInitialAndMaxPermits(1, 1);

	/** The experiment. */
	protected IExperimentSpecies experiment;

	/** The commands. Blocking queue of pending experiment commands. {@link ArrayBlockingQueue} is thread-safe; the field itself must not be re-assigned after construction. */
	protected final ArrayBlockingQueue<ExperimentCommand> commands = new ArrayBlockingQueue<>(50);

	/** The command thread. Processes commands from the {@link #commands} queue in a dedicated thread. */
	protected Thread commandThread = new Thread(() -> {
		while (acceptingCommands) {
			try {
				processUserCommand(commands.take());
			} catch (final InterruptedException e) {
				// Restore the interrupted status so the thread can exit cleanly
				Thread.currentThread().interrupt();
				return;
			} catch (final Throwable e) {
				// Catch Throwable (not just Exception) so that JVM Errors such as
				// NoClassDefFoundError or LinkageError never silently kill this thread
				// and leave the controller in a zombie/stalled state
				DEBUG.ERR("Unexpected error in command thread", e);
			}
		}
	}, "Front end controller");

	@Override
	public IExperimentSpecies getExperiment() { return experiment; }

	/**
	 * Sets the experiment.
	 *
	 * @param exp
	 *            the new experiment
	 */
	public void setExperiment(final IExperimentSpecies exp) { this.experiment = exp; }

	/**
	 * Offer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param command
	 *            the command
	 * @date 24 oct. 2023
	 */
	private boolean offer(final ExperimentCommand command) {
		if (experiment == null || isDisposing()) return false;
		return commands.offer(command);
	}

	/**
	 * Process user command.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param command
	 *            the command
	 * @date 24 oct. 2023
	 */
	protected abstract boolean processUserCommand(final ExperimentCommand command);

	/**
	 * Synchronous step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean synchronousStep() {
		return processUserCommand(ExperimentCommand._STEP);

	}

	/**
	 * Synchronous step back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean synchronousStepBack() {
		return processUserCommand(ExperimentCommand._BACK);
	}

	/**
	 * Synchronous start.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean synchronousStart() {
		return processUserCommand(ExperimentCommand._START);
	}

	/**
	 * Synchronous reload.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean synchronousReload() {
		return processUserCommand(ExperimentCommand._RELOAD);
	}

	/**
	 * Asynchronous pause.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean asynchronousPause() {
		return offer(ExperimentCommand._PAUSE);
	}

	/**
	 * Synchronous pause.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean synchronousPause() {
		return processUserCommand(ExperimentCommand._PAUSE);
	}

	/**
	 * Asynchronous step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean asynchronousStep() {
		return offer(ExperimentCommand._STEP);
	}

	/**
	 * Asynchronous step back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean asynchronousStepBack() {
		return offer(ExperimentCommand._BACK);
	}

	/**
	 * Asynchronous reload.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean asynchronousReload() {
		return offer(ExperimentCommand._RELOAD);
	}

	/**
	 * Synchronous open.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean synchronousOpen() {
		return processUserCommand(ExperimentCommand._OPEN);
	}

	/**
	 * Asynchronous start.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean asynchronousStart() {
		return offer(ExperimentCommand._START);
	}

	/**
	 * Asynchronous open.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	protected boolean asynchronousOpen() {
		return offer(ExperimentCommand._OPEN);
	}

	@Override
	public boolean isDisposing() { return disposing; }

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	protected IScope getScope() { return scope == null ? experiment.getExperimentScope() : scope; }

	/**
	 * Process open.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	@Override
	public boolean processOpen(final boolean andWait) {
		return andWait ? synchronousOpen() : asynchronousOpen();
	}

	/**
	 * Process pause.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	@Override
	public boolean processPause(final boolean andWait) {
		// Don't block display threads (see #
		return !GAMA.getGui().isInDisplayThread() && andWait ? synchronousPause() : asynchronousPause();
	}

	/**
	 * Process reload.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	@Override
	public boolean processReload(final boolean andWait) {
		return andWait ? synchronousReload() : asynchronousReload();
	}

	/**
	 * Process step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	@Override
	public boolean processStep(final boolean andWait) {
		return andWait ? synchronousStep() : asynchronousStep();
	}

	/**
	 * Process back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	@Override
	public boolean processBack(final boolean andWait) {
		return andWait ? synchronousStepBack() : asynchronousStepBack();
	}

	/**
	 * Process start.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 24 oct. 2023
	 */
	@Override
	public boolean processStart(final boolean andWait) {
		return andWait ? synchronousStart() : asynchronousStart();
	}

}
