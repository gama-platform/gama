/*******************************************************************************************************
 *
 * Application.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application;

import static java.lang.System.setProperty;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import static org.eclipse.e4.ui.workbench.IWorkbench.CLEAR_PERSISTED_STATE;
import static org.eclipse.ui.PlatformUI.RETURN_RESTART;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.internal.util.PrefUtil.getInternalPreferenceStore;

import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.ide.application.DelayedEventsProcessor;
import org.osgi.service.prefs.BackingStoreException;

import gama.api.GAMA;
import gama.api.runtime.IWorkspaceManager;
import gama.api.runtime.SystemInfo;
import gama.api.utils.files.BufferingUtils;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ApplicationWorkbenchAdvisor;
import gama.workspace.manager.WorkspaceModelsManager;

/** This class controls all aspects of the application's execution */
public class Application implements IApplication {

	static {
		DEBUG.ON();
	}

	/** The processor. */
	private static OpenDocumentEventProcessor OPEN_DOCUMENT_PROCESSOR;

	/**
	 * The Class OpenDocumentEventProcessor.
	 */
	public static class OpenDocumentEventProcessor extends DelayedEventsProcessor {

		/**
		 * Instantiates a new open document event processor.
		 *
		 * @param display
		 *            the display
		 */
		OpenDocumentEventProcessor(final Display display) {
			super(display);
		}

		/** The files to open. */
		private final ArrayList<String> filesToOpen = new ArrayList<>(1);

		@Override
		public void handleEvent(final Event event) {
			if (event.text != null) {
				filesToOpen.add(event.text);
				DEBUG.OUT("RECEIVED FILE TO OPEN: " + event.text);
			}
		}

		@Override
		public void catchUp(final Display display) {
			if (filesToOpen.isEmpty()) return;
			final String[] filePaths = filesToOpen.toArray(new String[filesToOpen.size()]);
			filesToOpen.clear();
			for (final String path : filePaths) { openDocument(path); }
		}
	}

	/** Routes package documents through their handler without introducing a dependency on the export UI bundle. */
	public static void openDocument(final String path) {
		final String extension = path.toLowerCase(java.util.Locale.ROOT);
		if (extension.endsWith(".gamr")) {
			for (final var delegate : gama.api.additions.registries.GamaAdditionRegistry.getEventLayerDelegates()) {
				if (delegate.acceptSource(null, "launcher")) delegate.createFrom(null, path, null);
			}
			return;
		}
		final boolean project = extension.endsWith(".gamp") || extension.endsWith(".zip");
		if (!project && !extension.endsWith(".gamx")) {
			WorkspaceModelsManager.instance.openModelPassedAsArgument(path);
			return;
		}
		final var workbench = PlatformUI.getWorkbench();
		final var commands = workbench.getService(org.eclipse.ui.commands.ICommandService.class);
		final var handlers = workbench.getService(org.eclipse.ui.handlers.IHandlerService.class);
		try {
			final var command = org.eclipse.core.commands.ParameterizedCommand.generateCommand(
					commands.getCommand(project ? "gama.ui.export.importProject" : "gama.ui.export.openPackage"),
					java.util.Map.of(project ? "gama.ui.export.projectPath" : "gama.ui.export.packagePath", path));
			if (command == null) throw new IllegalStateException("The exported model command is not available");
			handlers.executeCommand(command, null);
		} catch (final org.eclipse.core.commands.common.CommandException | RuntimeException e) {
			DEBUG.ERR("Unable to open GAMA document " + path, e);
			org.eclipse.jface.dialogs.MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(),
					"Unable to open GAMA document", e.getMessage());
		}
	}

	/**
	 * Creates the processor.
	 *
	 * @param display2
	 */
	public static void createProcessor(final Display display) {
		if (display == null) return;
		OPEN_DOCUMENT_PROCESSOR = new OpenDocumentEventProcessor(display);
		if (display.getData("gama.pendingStartupDocuments") instanceof java.util.List<?> pending) {
			for (final Object path : pending) {
				if (!(path instanceof String document)) continue;
				final Event event = new Event();
				event.text = document;
				OPEN_DOCUMENT_PROCESSOR.handleEvent(event);
			}
			display.setData("gama.pendingStartupDocuments", null);
		}
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		final Display display = configureDisplay();
		// Keep native document events queued while workspace selection pumps the event loop.
		createProcessor(display);
		configureCopilotStartup();
		GAMA.setRegularGui(new TempSWTGui());
		setDefaultUncaughtExceptionHandler((t, e) -> {
			if (e instanceof OutOfMemoryError) {
				final boolean close = GAMA.getGui().getDialogFactory().confirm("Out of memory",
						"GAMA is out of memory and will likely crash. Do you want to close now ?");
				if (close) { this.stop(); }
				e.printStackTrace();
			} else {
				DEBUG.ERR("Exception in Application", e);
			}

		});
		try {
			Object check = Display.getCurrent().syncCall(() -> checkWorkspace());


			if (!EXIT_OK.equals(check)) {
				if (getInternalPreferenceStore().getBoolean(IWorkspaceManager.CLEAR_WORKSPACE)) {
					setProperty(CLEAR_PERSISTED_STATE, "true");
					GAMA.getWorkspaceManager().clearWorkspace(false);
				}
				if (EXIT_OK.equals(initializeWorkspace())) return EXIT_OK;
				try {					
					final int returnCode = Workbench.createAndRunWorkbench(display, createWorkbenchAdvisor());
					if (returnCode == RETURN_RESTART) return EXIT_RESTART;
				} catch (Exception t) {
					DEBUG.ERR("Error in application", t);
				}
			}
		} finally {
			if (display != null) { display.dispose(); }
			final Location instanceLoc = Platform.getInstanceLocation();
			if (instanceLoc != null) { instanceLoc.release(); }
			BufferingUtils.getInstance().flushAllBuffers();
		}
		return EXIT_OK;

	}

	/** Called after workspace selection and before the workbench is created; EXIT_OK cancels startup. */
	protected Object initializeWorkspace() throws Exception { return null; }

	protected Object checkWorkspace() throws Exception { return GAMA.getWorkspaceManager().checkWorkspace(); }

	protected ApplicationWorkbenchAdvisor createWorkbenchAdvisor() { return new ApplicationWorkbenchAdvisor(); }

	private static void configureCopilotStartup() {
		final IEclipsePreferences preferences =
				ConfigurationScope.INSTANCE.getNode("com.microsoft.copilot.eclipse.ui");
		boolean changed = false;
		// Copilot 0.21 checks this marker before showing its Quick Start.
		if (preferences.get("copilotQuickStartVersion", null) == null) {
			preferences.putInt("copilotQuickStartVersion", 1);
			changed = true;
		}
		if (preferences.get("autoShowWhatsNew", null) == null) {
			preferences.putBoolean("autoShowWhatsNew", false);
			changed = true;
		}
		if (changed) {
			try {
				preferences.flush();
			} catch (final BackingStoreException e) {
				DEBUG.ERR("Unable to persist Copilot startup preferences", e);
			}
		}
	}

	/**
	 * Configure display. Issues #3596 & #3308
	 *
	 * @return the display
	 */
	private Display configureDisplay() {
		final Display display = PlatformUI.createDisplay();
		Display.setAppName("Gama Platform");
		Display.setAppVersion(SystemInfo.VERSION_NUMBER);

		Monitor primary = display.getPrimaryMonitor();

		DEBUG.BANNER(BANNER_CATEGORY.GUI, "Primary monitor resolution", "defined as",
				"" + primary.getBounds().width + "x" + primary.getBounds().height);
		DEBUG.BANNER(BANNER_CATEGORY.GUI, "Primary monitor zoom ", "defined as", "" + primary.getZoom() + "%");
		Monitor[] monitors = display.getMonitors();
		if (monitors.length > 1) {
			int i = 0;
			for (Monitor m : monitors) {
				if (m.equals(primary)) { continue; }
				i++;
				DEBUG.BANNER(BANNER_CATEGORY.GUI, "Monitor #" + i + " resolution ", "defined as",
						"" + m.getBounds().width + "x" + m.getBounds().height);
				DEBUG.BANNER(BANNER_CATEGORY.GUI, "Monitor #" + i + " zoom", "defined as", "" + m.getZoom() + "%");
			}
		}
		return display;
	}

	@Override
	public void stop() {
		BufferingUtils.getInstance().flushAllBuffers();
		final IWorkbench workbench = getWorkbench();
		if (workbench == null) return;
		final Display display = workbench.getDisplay();
		display.syncExec(() -> { if (!display.isDisposed()) { workbench.close(); } });
	}

	/**
	 * Gets the open document processor.
	 *
	 * @return the open document processor
	 */
	public static OpenDocumentEventProcessor getOpenDocumentProcessor() { return OPEN_DOCUMENT_PROCESSOR; }

}
