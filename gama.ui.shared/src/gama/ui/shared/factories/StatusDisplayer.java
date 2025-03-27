/*******************************************************************************************************
 *
 * StatusDisplayer.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.factories;

import gama.core.common.StatusMessage;
import gama.core.common.StatusMessage.StatusType;
import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.ui.shared.utils.ThreadedUpdater;

/**
 * The Class StatusDisplayer.
 */
public class StatusDisplayer implements IStatusDisplayer {

	/** The status. */
	private final ThreadedUpdater status = new ThreadedUpdater("Status refresh");

	/**
	 * Instantiates a new status displayer.
	 */
	StatusDisplayer() {
		GAMA.registerTopLevelAgentChangeListener(this);
	}

	/**
	 * Sets the listening agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the new listening agent
	 * @date 14 août 2023
	 */
	@Override
	public void topLevelAgentChanged(final ITopLevelAgent agent) {
		updateExperimentStatus();
	}

	/**
	 * Inform status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param string
	 *            the string
	 * @date 14 août 2023
	 */
	@Override
	public void informStatus(final String string, final String icon) {
		setStatus(string, StatusType.REGULAR, icon);
	}

	/**
	 * Error status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param error
	 *            the error
	 * @date 14 août 2023
	 */
	@Override
	public void errorStatus(final GamaRuntimeException error) {
		status.updateWith(StatusMessage.ERROR(error));
	}

	/**
	 * Sets the status.
	 *
	 * @param msg
	 *            the msg
	 * @param code
	 *            the code
	 * @param icon
	 *            the icon
	 */
	private void setStatus(final String msg, final StatusType code, final String icon) {
		status.updateWith(StatusMessage.CREATE(msg, code, icon));
	}

	/**
	 * Resume status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 14 août 2023
	 */
	@Override
	public void resetStatus() {
		status.reset();
	}

	/**
	 * Sets the sub status completion.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param s
	 *            the new sub status completion
	 * @date 14 août 2023
	 */
	@Override
	public void setTaskCompletion(final String name, final double s) {
		status.updateWith(StatusMessage.COMPLETION(name, s));
	}

	/**
	 * Inform status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param string
	 *            the string
	 * @param icon
	 *            the icon
	 * @date 14 août 2023
	 */
	@Override
	public void updateExperimentStatus() {
		status.updateWith(StatusMessage.EXPERIMENT());
	}

	/**
	 * Begin sub status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name
	 * @date 14 août 2023
	 */
	@Override
	public void beginTask(final String name, final String icon) {
		setStatus(name, StatusType.REGULAR, icon);
	}

	/**
	 * End sub status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name
	 * @date 14 août 2023
	 */
	@Override
	public void endTask(final String name, final String icon) {
		setStatus(name, StatusType.REGULAR, icon);
	}

	/**
	 * Sets the status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param message
	 *            the message
	 * @param color
	 *            the color
	 * @date 14 août 2023
	 */
	@Override
	public void setStatus(final String message, final String icon, final GamaColor color) {
		if (message == null) {
			resetStatus();
		} else {
			status.updateWith(StatusMessage.CUSTOM(message, StatusType.REGULAR, icon, color));
		}

	}

	/**
	 * Gets the threaded updater.
	 *
	 * @return the threaded updater
	 */
	public ThreadedUpdater getThreadedUpdater() { return status; }

}