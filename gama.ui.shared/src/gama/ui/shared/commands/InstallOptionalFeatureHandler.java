/*******************************************************************************************************
 *
 * InstallOptionalFeatureHandler.java, in gama.ui.shared, is part of the source code of the GAMA modeling
 * and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.commands;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Resolves an optional feature before opening p2's preselected installation wizard.
 */
public class InstallOptionalFeatureHandler extends AbstractHandler {

	private static final String PLUGIN_ID = "gama.ui.shared";
	private static final URI ECLIPSE_REPOSITORY = URI.create("https://download.eclipse.org/releases/2026-03/");
	private static final URI COPILOT_REPOSITORY = URI.create(
			"https://azuredownloads-g3ahgwb5b8bkbxhd.b01.azurefd.net/github-copilot/");
	private static final URI GRAPHICAL_REPOSITORY =
			URI.create("https://updates.gama-platform.org/plugin/2026-06/gama.graphical.modeling/");
	private static final AtomicBoolean INSTALLING = new AtomicBoolean();

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final boolean graphical = switch (event.getCommand().getId()) {
			case "gama.ui.install.graphical" -> true;
			case "gama.ui.install.copilot" -> false;
			default -> throw new ExecutionException("Unknown optional feature installation command");
		};
		final String label = graphical ? "Diagramming" : "GitHub Copilot";
		final String featureId = graphical ? "gama.ui.feature.diagram.feature.group"
				: "gama.feature.copilot.feature.group";
		final Shell shell = HandlerUtil.getActiveShellChecked(event);
		final var display = shell.getDisplay();
		final ProvisioningUI ui = ProvisioningUI.getDefaultUI();
		if (ui == null || ui.getProfileId() == null) {
			throw new ExecutionException("Optional features can only be installed in a provisioned GAMA installation.");
		}
		if (ui.hasScheduledOperations() || !INSTALLING.compareAndSet(false, true)) {
			MessageDialog.openInformation(shell, "Installation in progress",
					"Please finish the current software installation or update before installing another feature.");
			return null;
		}

		final Job job = Job.create("Preparing installation: " + label, monitor -> {
			try {
				final SubMonitor progress = SubMonitor.convert(monitor, 100);
				final IProfileRegistry registry =
						ui.getSession().getProvisioningAgent().getService(IProfileRegistry.class);
				final IProfile profile = registry == null ? null : registry.getProfile(ui.getProfileId());
				if (profile == null) return error("The GAMA installation profile is unavailable.");
				final String installedId = graphical ? featureId
						: "com.microsoft.copilot.eclipse.feature.feature.group";
				if (!profile.query(QueryUtil.createIUQuery(installedId), progress.split(5)).isEmpty()) {
					display.asyncExec(() -> {
						try {
							if (!shell.isDisposed()) {
								MessageDialog.openInformation(shell, label + " is installed",
										label + " is already installed. Use Support > Check for Updates to update it.");
							}
						} finally {
							INSTALLING.set(false);
						}
					});
					return Status.OK_STATUS;
				}

				final URI source;
				final URI[] repositories;
				if (graphical) {
					source = GRAPHICAL_REPOSITORY;
					repositories = new URI[] { source, ECLIPSE_REPOSITORY };
				} else {
					// Release packaging sets gama.version; development builds use the snapshot channel.
					final String version = System.getProperty("gama.version", "SNAPSHOT");
					final String channel = version.endsWith("-SNAPSHOT") ? "SNAPSHOT" : version;
					source = URI.create("https://updates.gama-platform.org/" + channel + "/");
					repositories = new URI[] { source, COPILOT_REPOSITORY, ECLIPSE_REPOSITORY };
				}
				for (final URI repository : repositories) {
					ui.getRepositoryTracker().addRepository(repository, null, ui.getSession());
				}
				final var repository = ui.loadMetadataRepository(source, false, progress.split(40));
				final var matches = repository.query(
						QueryUtil.createLatestQuery(QueryUtil.createIUQuery(featureId)), progress.split(5));
				if (matches.isEmpty()) return error("The update site " + source + " does not yet publish " + label
						+ " (" + featureId + "). Please contact the GAMA maintainers.");
				final List<IInstallableUnit> units = List.copyOf(matches.toSet());
				final InstallOperation operation = ui.getInstallOperation(units, repositories);
				final IStatus resolution = operation.resolveModal(progress.split(50));
				if (resolution.matches(IStatus.ERROR | IStatus.CANCEL)) return resolution;
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				display.asyncExec(() -> {
					try {
						if (!shell.isDisposed()) {
							if (ui.hasScheduledOperations()) {
								MessageDialog.openInformation(shell, "Installation in progress",
										"Another software operation has started. Please finish it and try again.");
							} else {
								// p2 retains license acceptance, signature verification and restart handling.
								ui.openInstallWizard(units, operation, null);
							}
						}
					} finally {
						INSTALLING.set(false);
					}
				});
				return Status.OK_STATUS;
			} catch (final OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (final ProvisionException e) {
				return new Status(IStatus.ERROR, PLUGIN_ID, "Unable to prepare " + label
						+ ". Check your network connection and the update sites.", e);
			}
		});
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent change) {
				if (change.getResult().isOK()) return;
				INSTALLING.set(false);
				if (!change.getResult().matches(IStatus.CANCEL)) {
					StatusManager.getManager().handle(change.getResult(), StatusManager.LOG | StatusManager.SHOW);
				}
			}
		});
		job.setUser(true);
		job.schedule();
		return null;
	}

	private static IStatus error(final String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message);
	}
}
