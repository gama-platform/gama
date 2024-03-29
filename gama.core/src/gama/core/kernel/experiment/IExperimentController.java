/*******************************************************************************************************
 *
 * IExperimentController.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import java.io.Closeable;

import gama.core.common.interfaces.IDisposable;

/**
 * Class IExperimentController.
 *
 * @author drogoul
 * @since 6 déc. 2015
 *
 */
public interface IExperimentController extends IDisposable, Closeable {

	/**
	 * The Enum ExperimentCommand.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 24 oct. 2023
	 */
	enum ExperimentCommand {

		/** The open. */
		_OPEN(),
		/** The start. */
		_START(),
		/** The step. */
		_STEP(),
		/** The pause. */
		_PAUSE(),
		/** The reload. */
		_RELOAD(),
		/** The back. */
		_BACK(),
		/** The close. */
		_CLOSE();

	}

	/**
	 * Gets the experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the experiment
	 * @date 24 oct. 2023
	 */
	IExperimentPlan getExperiment();

	/**
	 * Checks if is disposing.
	 *
	 * @return true, if is disposing
	 */
	default boolean isDisposing() { return false; }

	/**
	 * Checks if is paused.
	 *
	 * @return true, if is paused
	 */
	default boolean isPaused() { return false; }

	/**
	 * Schedule.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	default void schedule(final ExperimentAgent agent) {}

	/**
	 * Process open.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	boolean processOpen(final boolean andWait);

	/**
	 * Process pause.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	boolean processPause(final boolean andWait);

	/**
	 * Process reload.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	boolean processReload(final boolean andWait);

	/**
	 * Process step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	boolean processStep(final boolean andWait);

	/**
	 * Process back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 23 oct. 2023
	 */
	boolean processBack(final boolean andWait);

	/**
	 * Process start pause.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 24 oct. 2023
	 */
	default boolean processStartPause(final boolean andWait) {
		return isPaused() ? processStart(andWait) : processPause(andWait);
	}

	/**
	 * Process start.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param andWait
	 *            the and wait
	 * @date 24 oct. 2023
	 */
	boolean processStart(final boolean andWait);

	/**
	 * Close.Getting rid of the IO execption inherited from Closeable
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 oct. 2023
	 */
	@Override
	void close();

}