/*******************************************************************************************************
 *
 * RuntimeExceptionHandler.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.IRuntimeExceptionHandler;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.dev.THREADS;

/**
 * The Class RuntimeExceptionHandler.
 */
public class RuntimeExceptionHandler extends Job implements IRuntimeExceptionHandler {

	static {
		DEBUG.OFF();
	}

	/**
	 * Instantiates a new runtime exception handler.
	 */
	public RuntimeExceptionHandler() {
		super("Runtime error collector");
	}

	/** The incoming exceptions. */
	volatile BlockingQueue<GamaRuntimeException> incomingExceptions = new LinkedBlockingQueue<>();

	/** The clean exceptions. */
	volatile List<GamaRuntimeException> cleanExceptions = new ArrayList<>();

	/** The running. */
	volatile boolean running;

	/** The remaining time. */
	volatile int remainingTime = 5000;

	@Override
	public void offer(final GamaRuntimeException ex) {
		if (ex == null) return;
		// DEBUG.OUT("Adding exception to error collector -- " + ex.getAllText());
		remainingTime = 5000;
		incomingExceptions.offer(ex);
	}

	@Override
	public void clearErrors() {
		incomingExceptions.clear();
		cleanExceptions.clear();
		// Refresh the view to show it empty, but do NOT hide it — the user may have
		// opened it intentionally and should be able to keep it open even when empty.
		GAMA.getGui().displayErrors(null, cleanExceptions, true);
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		while (running) {
			while (incomingExceptions.isEmpty() && running && remainingTime > 0) {
				if (!THREADS.WAIT(500)) return Status.OK_STATUS;
				remainingTime -= 500;
			}
			if (!running) return Status.CANCEL_STATUS;
			if (remainingTime <= 0) {
				stop();
				return Status.OK_STATUS;
			}
			final boolean reset[] = { true };
			Map<ITopLevelAgent, List<GamaRuntimeException>> exceptions = new HashMap<>();

			for (GamaRuntimeException ge : incomingExceptions) {
				if (ge == null) { continue; }
				ITopLevelAgent top = ge.getTopLevelAgent();
				if (top == null) { continue; }
				if (!exceptions.containsKey(top)) { exceptions.put(top, new ArrayList<>()); }
				exceptions.get(top).add(ge);
			}

			for (Map.Entry<ITopLevelAgent, List<GamaRuntimeException>> entry : exceptions.entrySet()) {
				List<GamaRuntimeException> list = entry.getValue();

				// DEBUG.LOG("Processing exceptions for " + root);
				if (GamaPreferences.Runtime.CORE_STOP_AT_FIRST_ERROR.getValue()) {
					final GamaRuntimeException firstEx = list.get(0);
					if (GamaPreferences.Runtime.CORE_ERRORS_EDITOR_LINK.getValue()) {
						GAMA.getGui().getModelsManager().editModel(firstEx.getEditorContext());
					}
					firstEx.setReported();
					if (GamaPreferences.Runtime.CORE_SHOW_ERRORS.getValue()) {
						final List<GamaRuntimeException> exceps = new ArrayList<>();
						exceps.add(firstEx);
						updateUI(exceps, reset[0]);
						reset[0] = false;
					}
				} else if (GamaPreferences.Runtime.CORE_SHOW_ERRORS.getValue()) {
					final ArrayList<GamaRuntimeException> oldExcp = new ArrayList<>(cleanExceptions);
					for (final GamaRuntimeException newEx : list) {
						if (oldExcp.size() == 0) {
							oldExcp.add(newEx);
						} else {
							boolean toAdd = true;
							for (final GamaRuntimeException oldEx : oldExcp
									.toArray(new GamaRuntimeException[oldExcp.size()])) {
								if (oldEx.equivalentTo(newEx)) {
									if (oldEx != newEx) { oldEx.addAgents(newEx.getAgentsNames()); }
									toAdd = false;
								}
							}
							if (toAdd) { oldExcp.add(newEx); }

						}
					}
					updateUI(oldExcp, true);
				}

			}

			incomingExceptions.clear();
		}

		return Status.OK_STATUS;
	}

	@Override
	public void stop() {
		running = false;
	}

	/**
	 * Update UI.
	 *
	 * @param newExceptions
	 *            the new exceptions
	 */
	public void updateUI(final List<GamaRuntimeException> newExceptions, final boolean reset) {
		if (newExceptions != null) {
			newExceptions.removeIf(GamaRuntimeException::isInvalid);
			cleanExceptions = new ArrayList<>(newExceptions);
		} else {
			// null means "clear the data" — do NOT pass null to displayErrors, which
			// would hide the view. Pass the now-empty cleanExceptions instead so the
			// view stays open (showing nothing) if it was already visible.
			cleanExceptions = new ArrayList<>();
		}
		// displayErrors() opens the view if needed and refreshes it — all on the UI
		// thread — so the previous separate openErrorView() call is no longer required.
		GAMA.getGui().displayErrors(null, cleanExceptions, reset);
	}

	@Override
	public void start() {
		running = true;
		// Reinits remainingTime (issue found while working in #3641 : two executions in a row would lead to the second
		// one not reporting any error)
		remainingTime = 5000;
		schedule();

	}

	@Override
	public boolean isRunning() { return running; }

	@Override
	public void remove(final GamaRuntimeException obj) {
		cleanExceptions.remove(obj);
	}

	@Override
	public List<GamaRuntimeException> getCleanExceptions() { return cleanExceptions; }

	/**
	 * Immediately drains {@code incomingExceptions} into {@code cleanExceptions} and updates the UI. This bypasses the
	 * normal async Job delivery and is used on init-failure paths where the background Job may not have woken up yet.
	 */
	@Override
	public void displayLatestErrors() {
		final List<GamaRuntimeException> pending = new ArrayList<>(incomingExceptions);
		incomingExceptions.removeAll(pending);
		if (pending.isEmpty()) return;
		final List<GamaRuntimeException> merged = new ArrayList<>(cleanExceptions);
		for (final GamaRuntimeException ex : pending) {
			if (merged.stream().noneMatch(old -> old.equivalentTo(ex))) { merged.add(ex); }
		}
		updateUI(merged, true);
	}

}
