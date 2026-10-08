/*******************************************************************************************************
 *
 * IProgressIndicator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import org.geotools.api.util.InternationalString;
import org.geotools.api.util.ProgressListener;

/**
 *
 */
public interface IProgressIndicator extends ProgressListener {

	/**
	 * Complete.
	 */
	default void complete() {}

	/**
	 * Dispose.
	 */
	default void dispose() {}

	/**
	 * Exception occurred.
	 *
	 * @param arg0
	 *            the arg 0
	 */
	default void exceptionOccurred(final Throwable arg0) {}

	/**
	 * Gets the progress.
	 *
	 * @return the progress
	 */
	default float getProgress() { return 0; }

	/**
	 * Gets the task.
	 *
	 * @return the task
	 */
	default InternationalString getTask() { return null; }

	/**
	 * Checks if is canceled.
	 *
	 * @return true, if is canceled
	 */
	default boolean isCanceled() { return false; }

	/**
	 * Progress.
	 *
	 * @param p
	 *            the p
	 */
	default void progress(final float p) {}

	/**
	 * Sets the canceled.
	 *
	 * @param cancel
	 *            the new canceled
	 */
	default void setCanceled(final boolean cancel) {}

	/**
	 * Sets the task.
	 *
	 * @param n
	 *            the new task
	 */
	default void setTask(final InternationalString n) {}

	/**
	 * Started.
	 */
	default void started() {}

	/**
	 * Warning occurred.
	 *
	 * @param source
	 *            the source
	 * @param location
	 *            the location
	 * @param warning
	 *            the warning
	 */
	default void warningOccurred(final String source, final String location, final String warning) {}

}