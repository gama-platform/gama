/*******************************************************************************************************
 *
 * AbstractExperimentController.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import java.util.concurrent.ArrayBlockingQueue;

import gama.core.common.interfaces.GeneralSynchronizer;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.server.GamaServerExperimentConfiguration;

/**
 * The Class AbstractExperimentController.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 24 oct. 2023
 */
public abstract class AbstractExperimentController implements IExperimentController {

	/** The scope. */
	protected IScope scope;

	/** The disposing. */
	protected boolean disposing;

	/** The server configuration. */
	protected GamaServerExperimentConfiguration serverConfiguration;

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
	protected final GeneralSynchronizer previouslock = GeneralSynchronizer.withInitialAndMaxPermits(1, 1);

	/** The experiment. */
	protected IExperimentPlan experiment;

	/** The commands. */
	protected volatile ArrayBlockingQueue<ExperimentCommand> commands = new ArrayBlockingQueue<>(50);

	/** The command thread. */
	protected Thread commandThread = new Thread(() -> {
		while (acceptingCommands) {
			try {
				processUserCommand(commands.take());
			} catch (final Exception e) {}
		}
	}, "Front end controller");

	@Override
	public IExperimentPlan getExperiment() { return experiment; }

	/**
	 * Sets the experiment.
	 *
	 * @param exp
	 *            the new experiment
	 */
	public void setExperiment(final IExperimentPlan exp) { this.experiment = exp; }

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
