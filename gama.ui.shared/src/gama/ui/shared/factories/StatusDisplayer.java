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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import gama.core.common.IStatusMessage;
import gama.core.common.IStatusMessage.StatusType;
import gama.core.common.StatusMessage;
import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.common.interfaces.IUpdaterTarget;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class StatusDisplayer.
 */
public class StatusDisplayer implements IStatusDisplayer {

	/** The experimentControl. */
	private IUpdaterTarget experimentControl = new IUpdaterTarget() {};

	/** The statusRefresher control. */
	private IUpdaterTarget statusControl = new IUpdaterTarget() {};

	/** The statusRefresher. */
	private final StatusRefresher statusRefresher = new StatusRefresher("Status refresh");

	/** The statusRefresher. */
	private final ExperimentRefresher experimentRefresher = new ExperimentRefresher("Experiment refresh");

	/**
	 * The Class StatusRefresher.
	 */
	private class ExperimentRefresher extends UIJob {

		/** The message. */
		StatusMessage message = null;

		/**
		 * Instantiates a new threaded updater.
		 *
		 * @param name
		 *            the name
		 */
		public ExperimentRefresher(final String name) {
			super(WorkbenchHelper.getDisplay(), name);
			setProperty(IStatusMessage.JOB_KEY, IStatusMessage.INTERNAL_JOB);
			setPriority(INTERACTIVE);
			setSystem(true);
		}

		/**
		 * Update with.
		 *
		 * @param m
		 *            the m
		 */
		public void updateWith(final StatusMessage m) {
			message = m;
			if (m != null) { schedule(); }
		}

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			try {
				if (message != null) {
					if (experimentControl.isDisposed()) return Status.CANCEL_STATUS;
					experimentControl.updateWith(message);
				}
			} finally {
				message = null;
			}
			return Status.OK_STATUS;
		}

	}

	/**
	 * The Class StatusRefresher.
	 */
	private class StatusRefresher extends UIJob {

		/** The message. */
		StatusMessage message = null;

		/**
		 * Instantiates a new threaded updater.
		 *
		 * @param name
		 *            the name
		 */
		public StatusRefresher(final String name) {
			super(WorkbenchHelper.getDisplay(), name);
			setProperty(IStatusMessage.JOB_KEY, IStatusMessage.INTERNAL_JOB);
			setPriority(DECORATE);
			setSystem(true);
		}

		/**
		 * Update with.
		 *
		 * @param m
		 *            the m
		 */
		public void updateWith(final StatusMessage m) {
			message = m;
			if (m != null) { schedule(); }
		}

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			try {
				if (message != null) {
					if (statusControl.isDisposed()) return Status.CANCEL_STATUS;
					statusControl.updateWith(message);
				}
			} finally {
				message = null;
			}
			return Status.OK_STATUS;
		}

	}

	/**
	 * Instantiates a new statusRefresher displayer.
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
	 * Inform statusRefresher.
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
	 * Error statusRefresher.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param error
	 *            the error
	 * @date 14 août 2023
	 */
	@Override
	public void errorStatus(final GamaRuntimeException error) {
		statusRefresher.updateWith(StatusMessage.ERROR(error));
	}

	/**
	 * Sets the statusRefresher.
	 *
	 * @param msg
	 *            the msg
	 * @param code
	 *            the code
	 * @param icon
	 *            the icon
	 */
	private void setStatus(final String msg, final StatusType code, final String icon) {
		statusRefresher.updateWith(StatusMessage.CREATE(msg, code, icon));
	}

	/**
	 * Resume statusRefresher.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 14 août 2023
	 */
	@Override
	public void resetExperimentStatus() {
		experimentControl.reset();
	}

	/**
	 * Sets the sub statusRefresher completion.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param s
	 *            the new sub statusRefresher completion
	 * @date 14 août 2023
	 */
	@Override
	public void setTaskCompletion(final String name, final double s, final String icon) {
		statusRefresher.updateWith(StatusMessage.COMPLETION(name, s, icon));
	}

	/**
	 * Inform statusRefresher.
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
		experimentRefresher.updateWith(StatusMessage.EXPERIMENT());
	}

	/**
	 * Begin sub statusRefresher.
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
	 * End sub statusRefresher.
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
	 * Sets the statusRefresher.
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
			// resetStatus();
		} else {
			statusRefresher.updateWith(StatusMessage.CUSTOM(message, StatusType.REGULAR, icon, color));
		}

	}

	/**
	 * Sets the target.
	 *
	 * @param l
	 *            the l
	 * @param s
	 *            the s
	 */
	@Override
	public void setExperimentTarget(final IUpdaterTarget l) { experimentControl = l; }

	/**
	 * Sets the statusRefresher target.
	 *
	 * @param l
	 *            the new statusRefresher target
	 */
	@Override
	public void setStatusTarget(final IUpdaterTarget l) { statusControl = l; }

}