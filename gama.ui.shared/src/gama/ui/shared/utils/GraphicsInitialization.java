package gama.ui.shared.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;

import gama.api.ui.displays.GraphicsInitializerRegistry;

/** Initializes native graphics off the UI thread, before either Java2D or OpenGL creates a display. */
public final class GraphicsInitialization {

	private GraphicsInitialization() {}

	public static void initialize() {
		for (final FutureTask<Void> task : GraphicsInitializerRegistry.initializers()) {
			run("Initializing graphics", task);
		}
	}

	public static void run(final String title, final FutureTask<Void> task) {
		if (Display.getCurrent() == null || task.isDone()) {
			await(task);
			return;
		}
		try {
			new ProgressMonitorDialog(Display.getCurrent().getActiveShell()).run(true, false, monitor -> {
				monitor.beginTask(title, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
				try {
					await(task);
				} finally {
					monitor.done();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new IllegalStateException(title + " failed", e.getCause());
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(title + " interrupted", e);
		}
	}

	public static void await(final FutureTask<Void> task) {
		task.run();
		try {
			task.get();
		} catch (final ExecutionException e) {
			throw new IllegalStateException("Native graphics initialization failed", e.getCause());
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Native graphics initialization interrupted", e);
		}
	}
}
