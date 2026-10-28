/*******************************************************************************************************
 *
 * TaskProgress.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import gama.api.runtime.scope.IScope;

/**
 * Reports the progress of a long task (e.g. reading a large file or creating many agents) in the status bar, as a
 * percentage. The status is only updated when the percentage changes, so that loops over millions of elements do not
 * flood the user interface. Use it with try-with-resources so that the task is always ended:
 *
 * <pre>
 * try (TaskProgress progress = TaskProgress.begin(scope, "Creating cells")) {
 * 	for (int i = 0; i &lt; n; i++) {
 * 		progress.report(i, n);
 * 		...
 * 	}
 * }
 * </pre>
 */
public final class TaskProgress implements AutoCloseable {

	/** The status displayer. */
	private final IStatusDisplayer status;

	/** The name of the task. */
	private final String task;

	/** The last percentage reported. */
	private int last = -1;

	/**
	 * Begins a new task and displays its name in the status bar.
	 *
	 * @param scope
	 *            the scope
	 * @param task
	 *            the name of the task
	 * @return the task progress
	 */
	public static TaskProgress begin(final IScope scope, final String task) {
		return new TaskProgress(scope.getGui().getStatus(), task);
	}

	/**
	 * Instantiates a new task progress.
	 *
	 * @param status
	 *            the status
	 * @param task
	 *            the task
	 */
	private TaskProgress(final IStatusDisplayer status, final String task) {
		this.status = status;
		this.task = task;
		status.beginTask(task, IStatusMessage.DOWNLOAD_ICON);
	}

	/**
	 * Reports the completion of the task.
	 *
	 * @param ratio
	 *            the completion, between 0 and 1
	 */
	public void report(final double ratio) {
		final int percent = (int) (ratio * 100);
		if (percent == last) return;
		last = percent;
		status.setTaskCompletion(task, ratio);
	}

	/**
	 * Reports the completion of the task as a number of elements processed.
	 *
	 * @param done
	 *            the number of elements already processed
	 * @param total
	 *            the total number of elements
	 */
	public void report(final long done, final long total) {
		if (total > 0) { report(done / (double) total); }
	}

	/**
	 * Ends the task.
	 */
	@Override
	public void close() {
		status.endTask(task, IStatusMessage.DOWNLOAD_ICON);
	}

}
