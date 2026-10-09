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

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
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
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.PluginActionBuilder;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;
import org.osgi.service.prefs.BackingStoreException;
import org.eclipse.ui.keys.IBindingService;	
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.jface.dialogs.IDialogConstants;

import gama.export.ui.SaveSimulationsArtifactsDialog;
import gama.api.GAMA;
import gama.api.additions.delegates.IEventLayerDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.SystemInfo;
import gama.api.types.file.IGamaFile;
import gama.api.ui.IGui;
import gama.gaml.operators.Files;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.dev.FLAGS;
import gama.ui.application.Application;
import gama.ui.application.server.GamaGuiWebSocketServer;
import gama.workspace.manager.WorkspaceModelsManager;
import gama.workspace.nature.GamaNatures;
import gama.export.ExportHelper;
import gama.ui.application.workbench.StartupModelHelper;


/**
 * The Class ApplicationWorkbenchAdvisor.
 */
public class ApplicationWorkbenchAdvisor extends IDEWorkbenchAdvisor {
	static
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
		// Defer linking until the workbench startup sequence has initialized the UI.
		if (checkCopyOfBuiltInModels()) { WorkspaceModelsManager.instance.linkSampleModelsToWorkspace(); }
		FileUtils.cleanCache();
		final String[] args = Platform.getApplicationArgs();
		// DEBUG.LOG("Arguments received by GAMA : " + DEBUG.TO_STRING(args));

		// Start Server after the GUI is loaded
		GamaGuiWebSocketServer.startGuiServer();

		warnIfWaylandUnstable();

		if (args.length > 0) {
			int i = 0;
			if (args[0].contains("--launcher.defaultAction")) { i += 2; }
			if (i < args.length) {
				String exp = args[i];
				String modelPath = args[args.length - 1];
				if (!exp.endsWith(".gamr") && modelPath.endsWith(".gaml")) {
					WorkspaceModelsManager.instance.openModelPassedAsArgument(modelPath);
					return;
				}
				for (final IEventLayerDelegate delegate : GamaAdditionRegistry.getEventLayerDelegates()) {
					if (delegate.acceptSource(null, "launcher")) {
						delegate.createFrom(null, args[args.length - 1], null);
					}
				}
			}
		}

		// Disable Ctrl+N (opens new wizard menu) if simulation mode
		if (FLAGS.SIMULATION_ONLY)
		{
			IBindingService service = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
			
			if (service instanceof BindingService) {
				BindingService bindingService = (BindingService) service;
				
				try {
					KeySequence ctrlN = KeySequence.getInstance("M1+N");
					Binding[] currentBindings = bindingService.getBindings();
					
					// Create a list or array excluding the Ctrl+N binding
					List<Binding> newBindingsList = new ArrayList<>();
					
					for (Binding binding : currentBindings) {
						if (!ctrlN.equals(binding.getTriggerSequence())) {
							newBindingsList.add(binding);
						}
					}
					
					// Update the binding manager with the filtered list
					Binding[] newBindingsArray = newBindingsList.toArray(new Binding[0]);
					bindingService.getBindingManager().setBindings(newBindingsArray);
					
					// Force Eclipse to refresh the system shortcuts
					bindingService.savePreferences(bindingService.getActiveScheme(), newBindingsArray);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
		}

        if (FLAGS.SIMULATION_ONLY) {
			
            // IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            for(IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {

            if (window instanceof WorkbenchWindow) {
                IMenuManager menuBarManager = ((WorkbenchWindow) window).getMenuBarManager();
                IContributionItem[] items = menuBarManager.getItems();
                
                for (IContributionItem item : items) {
                    String id = item.getId();
                    // Match the default Eclipse legacy menu IDs
					// System.out.println(id);
                    if ("file".equals(id) || "edit".equals(id) || "org.eclipse.search.menu".equals(id)) {
                        item.setVisible(false);
                    }
                }
                // Force the top navigation bar to redraw its layout
                menuBarManager.update(true);
            }
        }}

		// handle startup model mode
		if (GamaPreferences.Interface.CORE_STARTUP_MODEL.getValue())
			StartupModelHelper.getInstance().startSimulation();
			
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
	 * Check copy of built in models.
	 *
	 * @return true, if successful
	 */
	protected boolean checkCopyOfBuiltInModels() {

		final IWorkspace workspace = GAMA.getWorkspaceManager().getWorkspace();
		final IProject[] projects = workspace.getRoot().getProjects();
		// User projects do not imply that the bundled library is present in this workspace.
		for (final IProject project : projects) {
			try {
				if (project.hasNature(GamaNatures.BUILTIN_NATURE)) return false;
			} catch (final CoreException e) {
				DEBUG.ERR("Could not check whether project " + project.getName() + " belongs to the built-in library",
						e);
			}
		}
		return true;
		// Following is not ready for prime time !
		// // If there are projects, we must be careful to distinguish user projects from built-in projects
		// List<IProject> builtInProjects = new ArrayList<>();
		// for ( IProject p : projects ) {
		// try {
		// // Assumption here : a non-accessible / linked project means a built-in model that is not accessible
		// // anymore. Maybe false sometimes... But how to check ?
		// DEBUG.OUT("Project = " + p.getName());
		// DEBUG.OUT(" ==== > Accessible : " + p.isAccessible());
		// DEBUG.OUT(" ==== > Open : " + p.isOpen());
		// DEBUG.OUT(" ==== > Linked : " + p.isLinked());
		// if ( !p.isAccessible() && p.isLinked() ) {
		// builtInProjects.add(p);
		// } else if ( p.isOpen() && p.getPersistentProperty(BUILTIN_PROPERTY) != null ) {
		// builtInProjects.add(p);
		// }
		// } catch (CoreException e) {
		// e.printStackTrace();
		// }
		// }
		// if ( builtInProjects.isEmpty() ) {
		// // only user projects there
		// return true;
		// }
		// String workspaceStamp = null;
		// try {
		// workspaceStamp = workspace.getRoot().getPersistentProperty(BUILTIN_PROPERTY);
		// DEBUG.OUT("Version of the models in workspace = " + workspaceStamp);
		// } catch (CoreException e) {
		// e.printStackTrace();
		// }
		// String gamaStamp = getCurrentGamaStampString();
		// // We dont know when the builtin models have been created -- there is probably a problem, but we do not try
		// to
		// // solve it
		// if ( gamaStamp == null ) {
		// DEBUG.ERR("Problem when trying to gather the date of creation of built-in models");
		// return false;
		// }
		// if ( gamaStamp.equals(workspaceStamp) ) {
		// // It's ok. The models in the workspace and in GAMA have the same time stamp
		// return false;
		// }
		// // We now have to (1) ask the user if he/she wants to update the models
		// boolean create =
		// MessageDialog
		// .openConfirm(
		// Display.getDefault().getActiveShell(),
		// "Update the models library",
		// "A new version of the built-in library of models is available. Would you like to update the ones present in
		// the workspace?");
		// // (2) erase the built-in projects from the workspace
		// if ( !create ) { return false; }
		// for ( IProject p : builtInProjects ) {
		// try {
		// p.delete(true, null);
		// } catch (CoreException e) {
		// e.printStackTrace();
		// }
		// }
		// return true;
	}

	@Override
	public String getInitialWindowPerspectiveId() { return IGui.PERSPECTIVE_MODELING_ID; }

	/**
	 * A workbench pre-shutdown method calls to prompt a confirmation of the shutdown and perform a saving of the
	 * workspace
	 */
	@Override
	public boolean preShutdown() {
		try {
			if(FLAGS.SIMULATION_ONLY
			   && StartupModelHelper.getInstance().areThereAnyArtifactsToSave()
			   && GAMA.getGui()
				.getDialogFactory()
					.question("Warning","New simulation artifacts have been found. Do you want to save them ?")
			)
			{
				IHandlerService handlerService =
					(IHandlerService) PlatformUI.getWorkbench()
						.getService(IHandlerService.class);

				if (handlerService != null) {
					try {
						handlerService.executeCommand(
							"gama.application.commands.SaveSimulationsArtifacts",
							null
						);

					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			// 	final SaveSimulationsArtifactsDialog dialog = new SaveSimulationsArtifactsDialog();
			// 	final int result = dialog.open();
				
			// 	if (result == IDialogConstants.OK_ID)
			// 	{
			// 		Path outputParentDirectory = Path.of(dialog.getOutputPath());
			// 		String outputDirectoryName = dialog.getOutputDirectoryName();

			// 		Path targetSavePath = outputParentDirectory.resolve(outputDirectoryName);
			// 		try {
			// 			StartupModelHelper.getInstance().saveSimulationArtifacts(targetSavePath);
			// 		} catch (final Exception exception) {
			// 			System.out.println("An error occured while saving simulation artifacts : ");
			// 			exception.printStackTrace();
			// 			GAMA.getGui().getDialogFactory().error("An error occured while saving simulation artifacts.");
			// 		}
			// 	}
			}			

			GAMA.closeAllExperiments(true, true);
			PerspectiveHelper.deleteCurrentSimulationPerspective();
			// So that they are not saved to the workbench.xmi file
			PerspectiveHelper.cleanPerspectives();

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return super.preShutdown();

	}

	/**
	 * disables the workspace saving by rerouting the responsible
	 * function from IDEWorkbenchAdvisor to nothing, 
	 * if the SIMULATION_ONLY flag is true.
	 * 
	 * It prevents a crash after closing the exported app, which
	 * is caused by the partial metadatas of the embedded workspace.
	 * 
	 * This workaround is much simpler than cleaning the metadatas
	 */
	@Override
	protected void disconnectFromWorkspace() {
		if(! FLAGS.SIMULATION_ONLY)
			super.disconnectFromWorkspace();
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
