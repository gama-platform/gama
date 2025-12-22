/*******************************************************************************************************
 *
 * ProgressCounter.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;

import org.geotools.api.util.InternationalString;
import org.geotools.api.util.ProgressListener;
import org.geotools.util.SimpleInternationalString;

import gama.core.common.IStatusMessage;
import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * The Class ProgressCounter.
 */
public class ProgressCounter implements ProgressListener, IIOReadProgressListener {

	/** The scope. */
	final IScope scope;

	/** The name. */
	final String name;

	/** The progress. */
	float progress;

	/**
	 * Instantiates a new progress counter.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 */
	public ProgressCounter(final IScope scope, final String name) {
		this.scope = scope;
		this.name = name;
	}

	/**
	 * Gets the displayer.
	 *
	 * @return the displayer
	 */
	IStatusDisplayer getDisplayer() { return GAMA.getGui().getStatus(); }

	/**
	 * Complete.
	 */
	@Override
	public void complete() {
		getDisplayer().setTaskCompletion(name, 1d);
	}

	/**
	 * Dispose.
	 */
	@Override
	public void dispose() {
		getDisplayer().endTask(name, IStatusMessage.DOWNLOAD_ICON);
	}

	/**
	 * Exception occurred.
	 *
	 * @param arg0
	 *            the arg 0
	 */
	@Override
	public void exceptionOccurred(final Throwable arg0) {
		GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(arg0, scope), true);
	}

	/**
	 * Gets the progress.
	 *
	 * @return the progress
	 */
	@Override
	public float getProgress() { return progress; }

	/**
	 * Gets the task.
	 *
	 * @return the task
	 */
	@Override
	public InternationalString getTask() { return new SimpleInternationalString(name); }

	/**
	 * Checks if is canceled.
	 *
	 * @return true, if is canceled
	 */
	@Override
	public boolean isCanceled() { return scope.interrupted(); }

	/**
	 * Progress.
	 *
	 * @param p
	 *            the p
	 */
	@Override
	public void progress(final float p) {
		progress = p;
		getDisplayer().setTaskCompletion(name, (double) progress);
	}

	/**
	 * Sets the canceled.
	 *
	 * @param cancel
	 *            the new canceled
	 */
	@Override
	public void setCanceled(final boolean cancel) {
		getDisplayer().endTask(name, IStatusMessage.DOWNLOAD_ICON);
	}

	/**
	 * Sets the task.
	 *
	 * @param n
	 *            the new task
	 */
	@Override
	public void setTask(final InternationalString n) {}

	/**
	 * Started.
	 */
	@Override
	public void started() {
		getDisplayer().beginTask(name, IStatusMessage.DOWNLOAD_ICON);
	}

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
	@Override
	public void warningOccurred(final String source, final String location, final String warning) {
		GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning(warning, scope), false);
	}

	@Override
	public void sequenceStarted(final ImageReader source, final int minIndex) {}

	@Override
	public void sequenceComplete(final ImageReader source) {}

	@Override
	public void imageStarted(final ImageReader source, final int imageIndex) {
		getDisplayer().beginTask(name, IStatusMessage.DOWNLOAD_ICON);
	}

	@Override
	public void imageProgress(final ImageReader source, final float percentageDone) {
		progress(percentageDone);
	}

	@Override
	public void imageComplete(final ImageReader source) {
		getDisplayer().setTaskCompletion(name, 1d);
		getDisplayer().endTask(name, IStatusMessage.DOWNLOAD_ICON);
	}

	@Override
	public void thumbnailStarted(final ImageReader source, final int imageIndex, final int thumbnailIndex) {}

	@Override
	public void thumbnailProgress(final ImageReader source, final float percentageDone) {}

	@Override
	public void thumbnailComplete(final ImageReader source) {}

	@Override
	public void readAborted(final ImageReader source) {
		getDisplayer().endTask(name, IStatusMessage.DOWNLOAD_ICON);
	}

}