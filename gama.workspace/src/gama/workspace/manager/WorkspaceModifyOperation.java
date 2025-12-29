/*******************************************************************************************************
 *
 * WorkspaceModifyOperation.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.manager;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * An operation which potentially makes changes to the workspace. All resource modification should be performed using
 * this operation. The primary consequence of using this operation is that events which typically occur as a result of
 * workspace changes (such as the firing of resource deltas, performance of autobuilds, etc.) are generally deferred
 * until the outermost operation has successfully completed. The platform may still decide to broadcast periodic
 * resource change notifications during the scope of the operation if the operation runs for a long time or another
 * thread modifies the workspace concurrently.
 * <p>
 * If a scheduling rule is provided, the operation will obtain that scheduling rule for the duration of its
 * <code>execute</code> method. If no scheduling rule is provided, the operation will obtain a scheduling rule that
 * locks the entire workspace for the duration of the operation.
 * </p>
 * <p>
 * Subclasses must implement <code>execute</code> to do the work of the operation.
 * </p>
 *
 * @see ISchedulingRule
 * @see org.eclipse.core.resources.IWorkspace#run(ICoreRunnable, IProgressMonitor)
 */
public abstract class WorkspaceModifyOperation {

	/** The rule. */
	private final ISchedulingRule rule;

	/**
	 * Creates a new operation.
	 */
	protected WorkspaceModifyOperation() {
		this(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Creates a new operation that will run using the provided scheduling rule.
	 *
	 * @param rule
	 *            The ISchedulingRule to use or <code>null</code>.
	 * @since 3.0
	 */
	protected WorkspaceModifyOperation(final ISchedulingRule rule) {
		this.rule = rule;
	}

	/**
	 * Performs the steps that are to be treated as a single logical workspace change.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param monitor
	 *            the progress monitor to use to display progress and field user requests to cancel
	 * @exception CoreException
	 *                if the operation fails due to a CoreException
	 * @exception InvocationTargetException
	 *                if the operation fails due to an exception other than CoreException
	 * @exception InterruptedException
	 *                if the operation detects a request to cancel, using <code>IProgressMonitor.isCanceled()</code>, it
	 *                should exit by throwing <code>InterruptedException</code>. It is also possible to throw
	 *                <code>OperationCanceledException</code>, which gets mapped to <code>InterruptedException</code> by
	 *                the <code>run</code> method.
	 */
	protected abstract void execute(IProgressMonitor monitor)
			throws CoreException, InvocationTargetException, InterruptedException;

	/**
	 * The <code>WorkspaceModifyOperation</code> implementation of this <code>IRunnableWithProgress</code> method
	 * initiates a batch of changes by invoking the <code>execute</code> method as a workspace runnable
	 * (<code>IWorkspaceRunnable</code>).
	 */
	public synchronized final void run(final IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		AtomicReference<InvocationTargetException> rethrownInvocationTargetException = new AtomicReference<>();
		AtomicReference<InterruptedException> rethrownInterruptedException = new AtomicReference<>();
		try {
			IWorkspaceRunnable workspaceRunnable = pm -> {
				try {
					execute(pm);
				} catch (InvocationTargetException e1) {
					rethrownInvocationTargetException.set(e1);
				} catch (InterruptedException e2) {
					rethrownInterruptedException.set(e2);
				}
				// CoreException and unchecked exceptions (e.g. OperationCanceledException) are
				// propagated to the outer catch
			};

			ResourcesPlugin.getWorkspace().run(workspaceRunnable, rule, IResource.NONE, monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} catch (OperationCanceledException e) {
			InterruptedException interruptedException = new InterruptedException(e.getMessage());
			interruptedException.initCause(e);
			throw interruptedException;
		}

		// Re-throw any exceptions caught while running the IWorkspaceRunnable
		if (rethrownInvocationTargetException.get() != null) throw rethrownInvocationTargetException.get();
		if (rethrownInterruptedException.get() != null) throw rethrownInterruptedException.get();
	}

	/**
	 * The scheduling rule. Should not be modified.
	 *
	 * @return the scheduling rule, or <code>null</code>.
	 * @since 3.4
	 */
	public ISchedulingRule getRule() { return rule; }
}
