/*******************************************************************************************************
 *
 * Application.java, in gama.ui.application, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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

import gama.core.kernel.root.SystemInfo;
import gama.core.runtime.GAMA;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.dev.FLAGS;
import gama.ui.application.workbench.ApplicationWorkbenchAdvisor;
import gama.workspace.manager.WorkspaceHelper;
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
			for (final String path : filePaths) { WorkspaceModelsManager.instance.openModelPassedAsArgument(path); }
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
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		FLAGS.load();
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
		final Display display = configureDisplay();
		Object check = Display.getCurrent().syncCall(WorkspaceHelper::checkWorkspace);
		if (!EXIT_OK.equals(check)) {
			try {
				createProcessor(display);
				if (getInternalPreferenceStore().getBoolean(WorkspaceHelper.CLEAR_WORKSPACE)) {
					setProperty(CLEAR_PERSISTED_STATE, "true");
					WorkspaceHelper.clearWorkspace(false);
				}
				try {
					GAMA.startGuiServer();
					final int returnCode = Workbench.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
					if (returnCode == RETURN_RESTART) return EXIT_RESTART;
				} catch (Exception t) {
					DEBUG.ERR("Error in application", t);
				}
			} finally {
				if (display != null) { display.dispose(); }
				final Location instanceLoc = Platform.getInstanceLocation();
				if (instanceLoc != null) { instanceLoc.release(); }
				GAMA.getBufferingController().flushAllBuffers();
			}
		}
		return EXIT_OK;

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
		GAMA.getBufferingController().flushAllBuffers();
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
