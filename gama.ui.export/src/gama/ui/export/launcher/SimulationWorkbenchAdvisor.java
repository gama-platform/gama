package gama.ui.export.launcher;

import java.util.Arrays;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

import gama.api.GAMA;
import gama.api.utils.files.BufferingUtils;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ApplicationWorkbenchAdvisor;
import gama.ui.application.workbench.ApplicationWorkbenchWindowAdvisor;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.export.commands.SaveSimulationsArtifacts;
import gama.export.SimulationPackageLauncher;
import gama.ui.shared.utils.LaunchPresentation;

public final class SimulationWorkbenchAdvisor extends ApplicationWorkbenchAdvisor {

	@Override
	protected boolean checkCopyOfBuiltInModels() { return false; }

	@Override
	protected boolean shouldOpenCommandLineModels() { return false; }

	@Override
	protected void showInteractiveConsoleAtStartup() {}

	@Override
	protected void openStartupModel() {
		configureLauncherUI();
		if (System.getProperty(SimulationPackageLauncher.CONTENT_PROPERTY) != null) {
			try {
				java.nio.file.Files.writeString(StartupModelHelper.getInstance().getContentRoot().resolve("player.ready"),
						"Simulation player workbench started\n");
			} catch (final java.io.IOException e) {
				DEBUG.ERR("Unable to record simulation player startup", e);
				GAMA.getGui().getDialogFactory().error("Unable to record player startup: " + e.getMessage());
			}
		}
		if (!StartupModelHelper.getInstance().getConfiguration().showHome()) {
			org.eclipse.swt.widgets.Display.getDefault().asyncExec(
					() -> StartupModelHelper.getInstance().launchDefaultExperiment());
		}
	}

	@Override
	public String getInitialWindowPerspectiveId() { return LauncherHomePerspective.ID; }

	@Override
	public ApplicationWorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(this, configurer) {
			@Override
			public void preWindowOpen() {
				super.preWindowOpen();
				configurer.setTitle(StartupModelHelper.getInstance().getPresentation().title() + " (powered by GAMA)");
			}

			@Override
			public void postWindowOpen() {
				super.postWindowOpen();
				if (configurer.getWindow() instanceof WorkbenchWindow window)
					configureHomeTrim(window, true);
			}
		};
	}

	private static void configureHomeTrim(final WorkbenchWindow window, final boolean home) {
		// Search/status/heap controls live beside the cool bar in the same top trim.
		window.setCoolBarVisible(!home && !Boolean.FALSE.equals(PerspectiveHelper.keepControls()));
		for (final var element : window.getTopTrim().getChildren()) {
			if ("SearchField".equals(element.getElementId())) element.setVisible(!home);
		}
		window.getTopTrim().setVisible(!home);
	}

	private void configureLauncherUI() {
		final var workbench = PlatformUI.getWorkbench();
		final var home = workbench.getPerspectiveRegistry().findPerspectiveWithId(LauncherHomePerspective.ID);
		if (home == null) throw new IllegalStateException("The simulation player Home perspective is not installed");
		final var service = workbench.getService(IBindingService.class);
		if (service instanceof BindingService bindings) {
			try {
				final var newFile = KeySequence.getInstance("M1+N");
				bindings.getBindingManager().setBindings(Arrays.stream(bindings.getBindings())
						.filter(binding -> !newFile.equals(binding.getTriggerSequence()))
						.toArray(org.eclipse.jface.bindings.Binding[]::new));
			} catch (final ParseException e) {
				throw new IllegalStateException("Invalid launcher key binding", e);
			}
		}
		for (final var window : workbench.getWorkbenchWindows()) {
			final var page = window.getActivePage();
			if (page == null) throw new IllegalStateException("The simulation player has no active workbench page");
			if (page.getPerspective() == null || !LauncherHomePerspective.ID.equals(page.getPerspective().getId()))
				page.setPerspective(home);
			try {
				page.showView(LauncherHomeView.ID);
			} catch (final org.eclipse.ui.PartInitException e) {
				throw new IllegalStateException("Unable to open the simulation player Home screen", e);
			}
			page.setEditorAreaVisible(false);
			final var helper = StartupModelHelper.getInstance();
			final String selection = helper.getConfiguration().startupExperiment().isBlank()
					? helper.getSelections().getFirst() : helper.getConfiguration().startupExperiment();
			window.getShell().setData(LaunchPresentation.SHELL_DATA_KEY,
					new LaunchPresentation.State(helper.getPresentation(), selection, null));
			window.getShell().setText(helper.getPresentation().title() + " (powered by GAMA)");
			if (!(window instanceof WorkbenchWindow internal)) continue;
			configureHomeTrim(internal, true);
			window.addPerspectiveListener(new PerspectiveAdapter() {
				@Override
				public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
					final boolean homeVisible = LauncherHomePerspective.ID.equals(perspective.getId());
					configureHomeTrim(internal, homeVisible);
					if (homeVisible) window.getShell().setText(helper.getPresentation().title() + " (powered by GAMA)");
				}
			});
			final var menu = internal.getMenuBarManager();
			for (final IContributionItem item : menu.getItems()) {
				if ("file".equals(item.getId()) || "edit".equals(item.getId())
						|| "org.eclipse.search.menu".equals(item.getId())) item.setVisible(false);
			}
			menu.update(true);
		}
	}

	@Override
	public boolean preShutdown() {
		try {
			final var running = GAMA.getControllers().stream().filter(controller -> !controller.isPaused()).toList();
			final var experiment = GAMA.getExperiment();
			boolean proceed = false;
			try {
				if (experiment != null) {
					if (!GAMA.pauseFrontmostExperiment(true)) return false;
					experiment.pauseAllOutputs();
				}
				BufferingUtils.getInstance().flushAllBuffers();
				if (StartupModelHelper.getInstance().areThereAnyArtifactsToSave()) {
					if (GAMA.getGui().getDialogFactory().question("Save experiment data",
								"The experiment has produced some data files. Do you want to save them before closing?"
										+ "\n\nNew or modified files will be saved in a ZIP archive. "
										+ "This does not save a resumable simulation state."
										+ "\n\nGenerated files will be deleted and original inputs restored when closing, "
										+ "whether or not you save a ZIP archive.")) {
						if (!Boolean.TRUE.equals(new SaveSimulationsArtifacts().execute(null))) return false;
					}
				}
				proceed = true;
			} finally {
				if (!proceed) {
					if (experiment != null) experiment.resumeAllOutputs();
					running.forEach(controller -> controller.processStart(true));
				}
			}
		} catch (final Exception e) {
			DEBUG.ERR("Unable to prepare the playable model for shutdown", e);
			GAMA.getGui().getDialogFactory().error("Unable to prepare shutdown: " + e.getMessage());
			return false;
		}
		return super.preShutdown();
	}

	@Override
	public void postShutdown() {
		super.postShutdown();
		try {
			BufferingUtils.getInstance().flushAllBuffers();
			final var helper = StartupModelHelper.getInstance();
			helper.discardSimulationArtifacts();
			helper.releaseOriginalInputs();
		} catch (final java.io.IOException | RuntimeException e) {
			DEBUG.ERR("Unable to clean up experiment data", e);
			org.eclipse.jface.dialogs.MessageDialog.openError(null, "Experiment data cleanup",
					"Unable to clean up experiment data: " + e.getMessage());
		}
	}

	@Override
	protected void disconnectFromWorkspace() {
		// An extracted workspace is temporary; it must not be persisted on shutdown.
	}
}
