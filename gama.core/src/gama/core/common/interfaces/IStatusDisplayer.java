/*******************************************************************************************************
 *
 * IStatusDisplayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;

/**
 * The Interface IStatusDisplayer.
 *
 * Changed AD 11/11/22: puts IScope first like everywhere in the code...
 */
public interface IStatusDisplayer extends ITopLevelAgentChangeListener {

	/**
	 * Sets the listening agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the new listening agent
	 * @date 14 août 2023
	 */
	@Override
	default void topLevelAgentChanged(final ITopLevelAgent agent) {}

	/**
	 * Wait status.
	 *
	 * @param scope
	 *            the scope
	 * @param string
	 *            the string
	 * @param run
	 *            the run
	 */
	default void waitStatus(final String string, final String icon, final Runnable run) {
		informStatus(string, icon);
		run.run();
		// resetStatus();
	}

	/**
	 * Inform status.
	 *
	 * @param string
	 *            the string
	 */
	default void informStatus(final String message, final String icon) {}

	/**
	 * Error status.
	 *
	 * @param message
	 *            the message
	 */
	default void errorStatus(final GamaRuntimeException error) {}

	/**
	 * Sets the sub status completion.
	 *
	 * @param status
	 *            the new sub status completion
	 */
	default void setTaskCompletion(final String name, final Double s) {}

	/**
	 * Inform status.
	 *
	 * @param message
	 *            the message
	 * @param icon
	 *            the icon
	 */
	default void updateExperimentStatus() {}

	/**
	 * Sets the status.
	 *
	 * @param msg
	 *            the msg
	 * @param icon
	 *            the icon
	 */
	default void setStatus(final String message, final String icon, final GamaColor color) {}

	/**
	 * Begin sub status.
	 *
	 * @param name
	 *            the name
	 */
	default void beginTask(final String name, final String icon) {}

	/**
	 * End sub status.
	 *
	 * @param name
	 *            the name
	 */
	default void endTask(final String name, final String icon) {}

	/**
	 * Sets the status target.
	 *
	 * @param target
	 *            the new status target
	 */
	default void setStatusTarget(final IStatusControl target) {}

	/**
	 * Sets the experiment target.
	 *
	 * @param target
	 *            the new experiment target
	 */
	default void setExperimentTarget(final IStatusControl target) {}

}
