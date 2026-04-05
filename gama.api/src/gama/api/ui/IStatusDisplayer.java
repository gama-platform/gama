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
package gama.api.ui;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.types.color.IColor;
import gama.api.utils.interfaces.ITopLevelAgentChangeListener;

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
	 * Displays a status message, runs the given runnable synchronously, then always resets the status — even if the
	 * runnable throws. This prevents the status bar from remaining stuck on messages like "Initializing simulations"
	 * when an error occurs during experiment initialization.
	 *
	 * @param string
	 *            the status message to display while the runnable executes
	 * @param icon
	 *            the icon to show alongside the message
	 * @param run
	 *            the work to perform under this status message
	 */
	default void waitStatus(final String string, final String icon, final Runnable run) {
		informStatus(string, icon);
		try {
			run.run();
		} finally {
			endTask(string, icon);
		}
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
	default void setStatus(final String message, final String icon, final IColor color) {}

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
	 * Returns the current status control target, or {@code null} if unavailable.
	 *
	 * @return the current status target
	 */
	default IStatusControl getStatusTarget() { return null; }

	/**
	 * Sets the experiment target.
	 *
	 * @param target
	 *            the new experiment target
	 */
	default void setExperimentTarget(final IStatusControl target) {}

}
