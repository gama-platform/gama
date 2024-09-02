/*******************************************************************************************************
 *
 * ThreadedUpdater.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IUpdaterMessage;
import gama.core.common.interfaces.IUpdaterTarget;
import gama.dev.DEBUG;

/**
 * Class ThreadedUpdater.
 *
 * @author drogoul
 * @since 10 mars 2014
 *
 */
public class ThreadedUpdater<Message extends IUpdaterMessage> extends UIJob implements IUpdaterTarget<Message> {

	static {
		DEBUG.ON();
	}

	/** The message. */
	Message message = null;

	/** The control. */
	private IUpdaterTarget<Message> control;

	/**
	 * Instantiates a new threaded updater.
	 *
	 * @param name
	 *            the name
	 */
	public ThreadedUpdater(final String name) {
		super(WorkbenchHelper.getDisplay(), name);
		setPriority(DECORATE);
	}

	@Override
	public boolean isDisposed() { return control.isDisposed(); }

	@Override
	public boolean isVisible() { return control.isVisible(); }

	@Override
	public void updateWith(final Message m) {
		if (isDisposed() || !isVisible() || isBusy() || m == null) return;
		message = m;
		schedule();
	}

	@Override
	public int getCurrentState() { return control.getCurrentState(); }

	/**
	 * Sets the target.
	 *
	 * @param l
	 *            the l
	 * @param s
	 *            the s
	 */
	public void setTarget(final IUpdaterTarget<Message> l, final IDisplaySurface s) {
		control = l;
	}

	@Override
	public boolean isBusy() { return control.isBusy(); }

	@Override
	public IStatus runInUIThread(final IProgressMonitor monitor) {
		if (control.isDisposed()) return Status.CANCEL_STATUS;
		if (control.isBusy() || !control.isVisible()) return Status.OK_STATUS;
		control.updateWith(message);
		return Status.OK_STATUS;
	}

	@Override
	public void resume() {
		control.resume();
	}
}
