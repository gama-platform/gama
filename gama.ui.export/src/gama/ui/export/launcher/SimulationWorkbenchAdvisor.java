package gama.ui.export.launcher;

import java.util.Arrays;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;

import gama.api.GAMA;
import gama.api.utils.files.BufferingUtils;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ApplicationWorkbenchAdvisor;
import gama.ui.export.commands.SaveSimulationsArtifacts;

public final class SimulationWorkbenchAdvisor extends ApplicationWorkbenchAdvisor {

	@Override
	protected boolean checkCopyOfBuiltInModels() { return false; }

	@Override
	protected void openStartupModel() {
		configureLauncherUI();
		StartupModelHelper.getInstance().startSimulation();
	}

	private void configureLauncherUI() {
		final var workbench = PlatformUI.getWorkbench();
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
			if (!(window instanceof WorkbenchWindow internal)) continue;
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
				if (StartupModelHelper.getInstance().areThereAnyArtifactsToSave() && GAMA.getGui().getDialogFactory()
						.question("Save simulation data", "New simulation artifacts have been found. Save them before exiting?")) {
					if (!Boolean.TRUE.equals(new SaveSimulationsArtifacts().execute(null))) return false;
				}
				proceed = true;
			} finally {
				if (!proceed) {
					if (experiment != null) experiment.resumeAllOutputs();
					running.forEach(controller -> controller.processStart(true));
				}
			}
		} catch (final Exception e) {
			DEBUG.ERR("Unable to prepare the simulation launcher for shutdown", e);
			GAMA.getGui().getDialogFactory().error("Unable to prepare shutdown: " + e.getMessage());
			return false;
		}
		return super.preShutdown();
	}

	@Override
	protected void disconnectFromWorkspace() {
		// An extracted workspace is temporary; it must not be persisted on shutdown.
	}
}
