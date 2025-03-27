/*******************************************************************************************************
 *
 * ThreadedUpdater.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import com.google.common.base.Strings;

import gama.core.common.StatusMessage;
import gama.core.common.StatusMessage.StatusType;
import gama.core.common.interfaces.IUpdaterTarget;
import gama.dev.DEBUG;

/**
 * Class ThreadedUpdater.
 *
 * @author drogoul
 * @since 10 mars 2014
 *
 */
public class ThreadedUpdater extends UIJob implements IUpdaterTarget {

	/** The message. */
	StatusMessage message = null;

	/** The experimentControl. */
	private IUpdaterTarget experimentControl = new IUpdaterTarget() {};

	/** The status control. */
	private IUpdaterTarget statusControl = new IUpdaterTarget() {};

	/**
	 * Instantiates a new threaded updater.
	 *
	 * @param name
	 *            the name
	 */
	public ThreadedUpdater(final String name) {
		super(WorkbenchHelper.getDisplay(), name);
		setPriority(DECORATE);
		setSystem(true);
	}

	@Override
	public boolean isDisposed() { return experimentControl.isDisposed() || statusControl.isDisposed(); }

	@Override
	public boolean isVisible() { return experimentControl.isVisible() && statusControl.isVisible(); }

	/**
	 * Update with.
	 *
	 * @param m
	 *            the m
	 */
	@Override
	public void updateWith(final StatusMessage m) {

		if (isDisposed() || !isVisible() || isBusy() || m == null) return;
		message = m;
		schedule();
	}

	/**
	 * Sets the target.
	 *
	 * @param l
	 *            the l
	 * @param s
	 *            the s
	 */
	public void setExperimentTarget(final IUpdaterTarget l) { experimentControl = l; }

	/**
	 * Sets the status target.
	 *
	 * @param l
	 *            the new status target
	 */
	public void setStatusTarget(final IUpdaterTarget l) { statusControl = l; }

	@Override
	public boolean isBusy() {
		return false;

		// message != null
		// && (message.getType() == StatusType.EXPERIMENT && experimentControl.isBusy() || statusControl.isBusy());
	}

	@Override
	public IStatus runInUIThread(final IProgressMonitor monitor) {
		if (isDisposed()) return Status.CANCEL_STATUS;
		try {
			if (message != null && message.getType() == StatusType.EXPERIMENT) {
				if (experimentControl.isBusy() || !experimentControl.isVisible()) return Status.OK_STATUS;
				experimentControl.updateWith(message);
			} else {
				if (statusControl.isBusy() || !statusControl.isVisible()) return Status.OK_STATUS;
				if (message instanceof StatusMessage sm && Strings.isNullOrEmpty(sm.message())) {

					DEBUG.OUT("");

				}
				statusControl.updateWith(message);
			}
		} finally {
			message = null;
		}
		return Status.OK_STATUS;
	}

	@Override
	public void reset() {
		statusControl.reset();
		experimentControl.reset();
	}

}
