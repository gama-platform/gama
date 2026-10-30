/*******************************************************************************************************
 *
 * ApplicationWorkbenchAdvisor.java, in gama.ui.application, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.application.workbench;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.PluginActionBuilder;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;
import org.osgi.service.prefs.BackingStoreException;

import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.SystemInfo;
import gama.api.types.file.IGamaFile;
import gama.api.ui.IGui;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.application.Application;
import gama.ui.application.server.GamaGuiWebSocketServer;
import gama.workspace.manager.WorkspaceModelsManager;

/**
 * The Class ApplicationWorkbenchAdvisor.
 */
public class ApplicationWorkbenchAdvisor extends IDEWorkbenchAdvisor {

	{
		DEBUG.OFF();
	}

	/**
	 * Instantiates a new application workbench advisor.
	 */
	public ApplicationWorkbenchAdvisor() {
		super(Application.getOpenDocumentProcessor());
		// DEBUG.OUT(DEBUG.CALLER() + " is created");
	}

	@Override
	public ApplicationWorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(this, configurer);
	}

	@Override
	public void initialize(final IWorkbenchConfigurer configurer) {

		ResourcesPlugin.getPlugin().getStateLocation();
		try {
			super.initialize(configurer);

			IDE.registerAdapters();
			configurer.setSaveAndRestore(true);

			final IDecoratorManager dm = configurer.getWorkbench().getDecoratorManager();
			dm.setEnabled("org.eclipse.pde.ui.binaryProjectDecorator", false);
			dm.setEnabled("org.eclipse.team.svn.ui.decorator.SVNLightweightDecorator", false);
			dm.setEnabled("gama.ui.application.decorator", true);
			dm.setEnabled("org.eclipse.ui.LinkedResourceDecorator", false);
			dm.setEnabled("org.eclipse.ui.VirtualResourceDecorator", false);
			dm.setEnabled("org.eclipse.xtext.builder.nature.overlay", false);
			if (Display.getCurrent() != null) {
				Display.getCurrent().getThread().setUncaughtExceptionHandler(GamaExecutorService.EXCEPTION_HANDLER);
			}
		} catch (final CoreException e) {
			// e.printStackTrace();
		}
		PluginActionBuilder.setAllowIdeLogging(false);
		ThemeHelper.install();
	}

	@Override
	public void postStartup() {
		super.postStartup();
		showInteractiveConsoleAtStartup();
		// Defer linking until the workbench startup sequence has initialized the UI.
		if (checkCopyOfBuiltInModels()) {
			WorkspaceModelsManager.instance.linkSampleModelsToWorkspace();
		}
		FileUtils.cleanCache();
		final String[] args = Boolean.TRUE.equals(org.eclipse.swt.widgets.Display.getCurrent()
				.getData("gama.startupArgumentsConsumed")) ? new String[0] : Platform.getApplicationArgs();
		// DEBUG.LOG("Arguments received by GAMA : " + DEBUG.TO_STRING(args));

		// Start Server after the GUI is loaded
		GamaGuiWebSocketServer.startGuiServer();

		warnIfWaylandUnstable();

		if (Boolean.TRUE.equals(Display.getCurrent().getData("gama.startupArgumentsConsumed"))) return;

		if (shouldOpenCommandLineModels() && args.length > 0) {
			int i = 0;
			if (args[0].contains("--launcher.defaultAction")) { i += 2; }
			if (i < args.length) {
				String exp = args[i];
				if (!exp.endsWith(".gamr")) {
					Application.openDocument(args[args.length - 1]);
					return;
				}
				for (final IEventLayerDelegate delegate : GamaAdditionRegistry.getEventLayerDelegates()) {
					if (delegate.acceptSource(null, "launcher")) {
						delegate.createFrom(null, args[args.length - 1], null);
					}
				}
			}

		}

		if (GamaPreferences.Interface.CORE_STARTUP_MODEL.getValue()) { openStartupModel(); }

	}

	protected void openStartupModel() {
		IGamaFile<?, ?> file = GamaPreferences.Interface.CORE_DEFAULT_MODEL.getValue();
		if (file != null && file.exists(null)) {
			StringBuilder name = new StringBuilder().append(file.getPath(null));
			String exp = GamaPreferences.Interface.CORE_DEFAULT_EXPERIMENT.getValue();
			if (exp != null && !exp.isBlank()) { name.append("#").append(exp); }
			WorkspaceModelsManager.instance.openModelPassedAsArgument(name.toString());
		}
	}

	protected boolean shouldOpenCommandLineModels() { return true; }

	protected void showInteractiveConsoleAtStartup() {
		final var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null || window.getActivePage() == null) return;
		try {
			window.getActivePage().showView(IGui.INTERACTIVE_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (final PartInitException e) {
			DEBUG.ERR("Unable to show the interactive console at startup", e);
		}
	}

	/** Qualifier of the workspace-scoped preference node holding the Wayland-warning dismissal flag. */
	private static final String WAYLAND_PREF_QUALIFIER = "gama.ui.application";

	/** Key of the boolean flag recording that the user dismissed the Wayland warning for the current workspace. */
	private static final String WAYLAND_WARNING_DISMISSED = "wayland.warning.dismissed";

	/**
	 * On Linux/Wayland, warns the user that GAMA (whose 3D/OpenGL rendering relies on JOGL) is not stable under the
	 * Wayland display server, and recommends relaunching with {@code GDK_BACKEND=x11} or using the installed
	 * application launcher (which is already configured for this). Does nothing when GAMA is not running under native
	 * Wayland, when the x11 backend has already been forced, or when the user dismissed the warning for this workspace.
	 * <p>
	 * The dismissal flag is stored in the workspace-scoped {@link InstanceScope} (i.e. in the current workspace's
	 * {@code .metadata}), so ticking "do not show again" only silences the warning for that workspace; a fresh
	 * workspace warns again. This deliberately bypasses {@code GamaPreferences}, whose store is global to the GAMA
	 * installation/user account. This only runs in GUI mode, since headless uses a separate application that never
	 * loads this advisor.
	 */
	private void warnIfWaylandUnstable() {
		if (!SystemInfo.isLinux() || (System.getenv("WAYLAND_DISPLAY") == null) || "x11".equals(System.getenv("GDK_BACKEND"))) return; // already forced onto XWayland

		final IEclipsePreferences node = InstanceScope.INSTANCE.getNode(WAYLAND_PREF_QUALIFIER);
		if (node.getBoolean(WAYLAND_WARNING_DISMISSED, false)) return; // dismissed for this workspace

		final Shell shell = Display.getDefault().getActiveShell(); // postStartup runs on the UI thread
		final String title = "Wayland compatibility warning";
		final String message = """
				GAMA is not stable under the Wayland display server \
				(its 3D/OpenGL rendering relies on JOGL, which does not support Wayland).

				For a stable experience, relaunch GAMA with the environment variable GDK_BACKEND=x11, \
				or start it from the installed application launcher, which is already configured for this.""";
		final MessageDialogWithToggle dialog = MessageDialogWithToggle.openWarning(shell, title, message,
				"Do not show this warning again for this workspace", false, null, null);
		if (dialog.getToggleState()) {
			node.putBoolean(WAYLAND_WARNING_DISMISSED, true);
			try {
				node.flush();
			} catch (final BackingStoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reconcile on every normal startup, including workspaces containing only part of the current library.
	 * Exported simulations override this hook to leave their dedicated workspace alone.
	 *
	 * @return true, if successful
	 */
	protected boolean checkCopyOfBuiltInModels() { return true; }

	@Override
	public String getInitialWindowPerspectiveId() { return IGui.PERSPECTIVE_MODELING_ID; }

	/**
	 * A workbench pre-shutdown method calls to prompt a confirmation of the shutdown and perform a saving of the
	 * workspace
	 */
	@Override
	public boolean preShutdown() {
		try {
			GAMA.closeAllExperiments(true, true);
			PerspectiveHelper.deleteCurrentSimulationPerspective();
			// So that they are not saved to the workbench.xmi file
			PerspectiveHelper.cleanPerspectives();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return super.preShutdown();

	}

	@Override
	public void postShutdown() {
		try {
			super.postShutdown();
		} catch (final Exception e) {
			// Remove the trace of exceptions
			// e.printStackTrace();
		}
	}

	@Override
	public void preStartup() {
		// Suspend background jobs while we startup
		Job.getJobManager().suspend();
		// super.preStartup();
	}

	/**
	 * Method getWorkbenchErrorHandler()
	 *
	 * @see org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor#getWorkbenchErrorHandler()
	 */
	// @Override
	// public synchronized AbstractStatusHandler getWorkbenchErrorHandler() {
	// return new AbstractStatusHandler() {
	//
	// @Override
	// public void handle(final StatusAdapter statusAdapter, final int style) {
	// final int severity = statusAdapter.getStatus().getSeverity();
	// if (severity == IStatus.INFO || severity == IStatus.CANCEL) return;
	// final Throwable e = statusAdapter.getStatus().getException();
	// if (e instanceof OutOfMemoryError) {
	// GamaExecutorService.EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), e);
	// }
	// final String message = statusAdapter.getStatus().getMessage();
	// // Stupid Eclipse
	// if (!message.contains("File toolbar contribution item") && !message.contains("Duplicate template id")) {
	// DEBUG.OUT("GAMA caught a workbench message : " + message);
	// }
	// if (e != null) { DEBUG.OUT("GAMA caught an error in the main application loop: " + e.getMessage()); }
	// }
	// };
	// }

	@Override
	public void eventLoopException(final Throwable t) {
		DEBUG.OUT("GAMA caught an error in the main application loop: " + t.getMessage());
	}

}
