package gama.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/** Default GUI entry point; package-only invocations never instantiate the modeling application. */
public final class DocumentApplication implements IApplication {

	public static final String APPLICATION = "gama.export.DocumentApplication";
	private volatile IApplication modeler;

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		if (System.getenv("WAYLAND_DISPLAY") != null)
			System.setProperty("org.eclipse.swt.internal.gtk.disableWayland", "true");
		Display.setAppName("Gama");
		final Display display = Display.getCurrent() == null ? new Display() : Display.getCurrent();
		final var documents = new ArrayList<String>();
		final Listener listener = event -> {
			if (event.text != null) documents.add(event.text);
		};
		display.addListener(SWT.OpenDocument, listener);
		try {
			final Object supplied = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
			final var arguments = PackageDocuments.fromArguments(supplied instanceof String[] values ? values : new String[0]);
			context.applicationRunning();
			// Cocoa delivers the initial document event after the native launcher has completed its startup handshake.
			if (PackageDocuments.shouldWaitForNativeDocuments(Platform.OS_MACOSX.equals(Platform.getOS()), arguments)) {
				final long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(250);
				do {
					if (!display.readAndDispatch()) Thread.sleep(5);
				} while (System.nanoTime() < deadline);
			}
			final var initial = new ArrayList<>(arguments);
			for (final String path : documents) if (!initial.contains(path)) initial.add(path);
			if (!PackageDocuments.packagesOnly(initial)) {
				display.removeListener(SWT.OpenDocument, listener);
				display.setData(PackageDocuments.PENDING_DOCUMENTS, List.copyOf(initial));
				display.setData(PackageDocuments.ARGUMENTS_CONSUMED, !initial.isEmpty());
				return startModeler(context);
			}
			final Shell owner = new Shell(display);
			try {
				final var pending = new ArrayList<>(initial);
				documents.clear();
				for (int i = 0; i < pending.size(); i++) {
					try {
						final var launch = SimulationPackageOpening.open(owner, Path.of(pending.get(i)));
						waitForPlayer(owner, launch);
					} catch (final IOException | RuntimeException e) {
						Platform.getLog(DocumentApplication.class).log(new Status(org.eclipse.core.runtime.IStatus.ERROR,
								"gama.export", "Unable to open playable model " + pending.get(i), e));
						MessageDialog.openError(owner, "Unable to open playable model", e.getMessage());
					}
					pending.addAll(documents);
					documents.clear();
					if (pending.subList(i + 1, pending.size()).stream().anyMatch(path -> !PackageDocuments.isPackage(path))) {
						display.removeListener(SWT.OpenDocument, listener);
						display.setData(PackageDocuments.PENDING_DOCUMENTS, List.copyOf(pending.subList(i + 1, pending.size())));
						display.setData(PackageDocuments.ARGUMENTS_CONSUMED, Boolean.TRUE);
						return startModeler(context);
					}
				}
			} finally {
				owner.dispose();
			}
			return EXIT_OK;
		} finally {
			if (!display.isDisposed()) {
				display.removeListener(SWT.OpenDocument, listener);
				display.dispose();
			}
		}
	}

	private static void waitForPlayer(final Shell owner, final SimulationPackageOpening.Launch launch)
			throws IOException, InterruptedException {
		try {
			new ProgressMonitorDialog(owner).run(true, false, monitor -> {
				monitor.beginTask("Opening simulation player", IProgressMonitor.UNKNOWN);
				try {
					SimulationPackageOpening.awaitStartup(launch);
				} catch (final IOException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			});
		} catch (final InvocationTargetException e) {
			throw new IOException(e.getCause().getMessage(), e.getCause());
		}
	}

	private Object startModeler(final IApplicationContext context) throws Exception {
		final var extension = Platform.getExtensionRegistry()
				.getExtension("org.eclipse.core.runtime.applications", "gama.ui.application.GamaApplication");
		if (extension == null) throw new IOException("The GAMA modeling application is not installed");
		for (final var application : extension.getConfigurationElements()) {
			for (final var run : application.getChildren("run")) {
				modeler = (IApplication) run.createExecutableExtension("class");
				return modeler.start(context);
			}
		}
		throw new IOException("The GAMA modeling application has no entry point");
	}

	@Override
	public void stop() {
		final IApplication application = modeler;
		if (application != null) application.stop();
	}
}
