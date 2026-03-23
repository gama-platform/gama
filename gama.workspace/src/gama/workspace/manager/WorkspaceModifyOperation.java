/*******************************************************************************************************
 *
 * WorkspaceModifyOperation.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.manager;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import gama.api.GAMA;

/**
 * An operation which potentially makes changes to the workspace. All resource modification should be performed using
 * this operation. The primary consequence of using this operation is that events which typically occur as a result of
 * workspace changes (such as the firing of resource deltas, performance of autobuilds, etc.) are generally deferred
 * until the outermost operation has successfully completed. 
 * 
 * The platform may still decide to broadcast periodic resource change notifications during the scope of the operation 
 * if the operation runs for a long time or another thread modifies the workspace concurrently.
 * 
 * <h3>Scheduling Rules</h3>
 * If a scheduling rule is provided, the operation will obtain that scheduling rule for the duration of its
 * <code>execute</code> method. If no scheduling rule is provided, the operation will obtain a scheduling rule that
 * locks the entire workspace for the duration of the operation.
 * 
 * <h3>Usage</h3>
 * Subclasses must implement <code>execute</code> to do the work of the operation.
 * 
 * <h3>Performance Considerations</h3>
 * - Operations are executed atomically with respect to workspace modifications
 * - Use specific scheduling rules when possible to avoid locking the entire workspace
 * - Consider breaking long operations into smaller chunks to avoid blocking other operations
 * 
 * @see ISchedulingRule
 * @see org.eclipse.core.resources.IWorkspace#run(ICoreRunnable, IProgressMonitor)
 * 
 * @author GAMA Development Team
 * @version 2025-03
 */
public abstract class WorkspaceModifyOperation {

	/** The scheduling rule for this operation (can be null for workspace-wide lock) */
	private final ISchedulingRule rule;

	/**
	 * Creates a new operation that will lock the entire workspace during execution.
	 * This is equivalent to calling {@link #WorkspaceModifyOperation(ISchedulingRule)} with 
	 * the workspace root as the scheduling rule.
	 */
	protected WorkspaceModifyOperation() {
		this(GAMA.getWorkspaceManager().getRoot());
	}

	/**
	 * Creates a new operation that will run using the provided scheduling rule.
	 * 
	 * @param rule The ISchedulingRule to use for this operation. Pass null to lock
	 *             the entire workspace, or a more specific rule to allow concurrent
	 *             operations on unrelated resources.
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
	 * Executes this workspace modify operation as a single logical workspace change.
	 * This implementation initiates a batch of changes by invoking the <code>execute</code> 
	 * method as a workspace runnable (<code>IWorkspaceRunnable</code>).
	 * 
	 * <h3>Exception Handling</h3>
	 * <ul>
	 * <li>CoreException: Wrapped in InvocationTargetException</li>
	 * <li>OperationCanceledException: Converted to InterruptedException</li>
	 * <li>InvocationTargetException: Re-thrown as-is</li>
	 * <li>InterruptedException: Re-thrown as-is</li>
	 * </ul>
	 * 
	 * @param monitor the progress monitor for tracking operation progress and cancellation
	 * @throws InvocationTargetException if the operation fails due to an exception other than InterruptedException
	 * @throws InterruptedException if the operation is cancelled or interrupted
	 */
	public final void run(final IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		
		// Use atomic references to capture exceptions from the workspace runnable
		final AtomicReference<InvocationTargetException> capturedInvocationException = new AtomicReference<>();
		final AtomicReference<InterruptedException> capturedInterruptedException = new AtomicReference<>();
		
		try {
			// Create workspace runnable that delegates to the execute method
			final IWorkspaceRunnable workspaceRunnable = progressMonitor -> {
				try {
					execute(progressMonitor);
				} catch (InvocationTargetException e) {
					capturedInvocationException.set(e);
				} catch (InterruptedException e) {
					capturedInterruptedException.set(e);
				}
				// Note: CoreException and unchecked exceptions (e.g. OperationCanceledException) 
				// are propagated to the outer catch block
			};

			// Execute the operation within the workspace lock
			GAMA.getWorkspaceManager().getWorkspace().run(workspaceRunnable, rule, IResource.NONE, monitor);
			
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} catch (OperationCanceledException e) {
			final InterruptedException interruptedException = new InterruptedException(e.getMessage());
			interruptedException.initCause(e);
			throw interruptedException;
		}

		// Re-throw any exceptions that were captured during execution
		final InvocationTargetException invocationException = capturedInvocationException.get();
		if (invocationException != null) {
			throw invocationException;
		}
		
		final InterruptedException interruptedException = capturedInterruptedException.get();
		if (interruptedException != null) {
			throw interruptedException;
		}
	}

	/**
	 * Returns the scheduling rule for this operation. The rule determines which resources
	 * will be locked during the operation's execution.
	 * 
	 * @return the scheduling rule, or <code>null</code> if the entire workspace will be locked
	 * @since 3.4
	 */
	public ISchedulingRule getRule() { 
		return rule; 
	}
}
