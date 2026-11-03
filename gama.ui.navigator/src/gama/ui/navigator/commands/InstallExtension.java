/*******************************************************************************************************
 *
 * InstallExtension.java, in gama.ui.navigator, is part of the source code of the GAMA modeling
 * and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Resolves an optional feature before opening p2's preselected installation wizard.
 */
final class InstallExtension {

	static final String PLUGIN_ID = "gama.ui.navigator";
	private static final URI ECLIPSE_REPOSITORY = URI.create("https://download.eclipse.org/releases/2026-03/");
	static final URI COPILOT_REPOSITORY = URI.create("https://gama-platform.org/gama.copilot/");
	private static final URI COPILOT_UPSTREAM_REPOSITORY =
			URI.create("https://azuredownloads-g3ahgwb5b8bkbxhd.b01.azurefd.net/github-copilot/");
	private static final AtomicBoolean INSTALLING = new AtomicBoolean();

	record Extension(URI repository, String featureId, String label, Version version) {}

	private InstallExtension() {}

	static IProfile installationProfile(final ProvisioningUI ui) {
		if (ui == null || ui.getProfileId() == null) return null;
		final IProfileRegistry registry = ui.getSession().getProvisioningAgent().getService(IProfileRegistry.class);
		return registry == null ? null : registry.getProfile(ui.getProfileId());
	}

	static void install(final Shell shell, final Extension extension) {
		final String label = extension.label();
		final String featureId = extension.featureId();
		final var display = shell.getDisplay();
		final ProvisioningUI ui = ProvisioningUI.getDefaultUI();
		if (ui == null || ui.getProfileId() == null) {
			StatusManager.getManager().handle(
					error("Extensions can only be installed in a provisioned GAMA installation."),
					StatusManager.LOG | StatusManager.SHOW);
			return;
		}
		if (ui.hasScheduledOperations() || !INSTALLING.compareAndSet(false, true)) {
			MessageDialog.openInformation(shell, "Installation in progress",
					"Please finish the current software installation or update before installing another feature.");
			return;
		}

		final Job job = Job.create("Preparing installation: " + label, monitor -> {
			try {
				final SubMonitor progress = SubMonitor.convert(monitor, 100);
				final IProfile profile = installationProfile(ui);
				if (profile == null) return error("The GAMA installation profile is unavailable.");
				final URI source = extension.repository();
				final URI[] repositories = source.equals(COPILOT_REPOSITORY)
						? new URI[] { source, COPILOT_UPSTREAM_REPOSITORY, ECLIPSE_REPOSITORY }
						: new URI[] { source, ECLIPSE_REPOSITORY };
				for (final URI repository : repositories) {
					ui.getRepositoryTracker().addRepository(repository, null, ui.getSession());
				}
				final IInstallableUnit installed = profile.query(QueryUtil.createIUQuery(featureId), progress.split(5))
						.toSet().stream().max(Comparator.comparing(IInstallableUnit::getVersion)).orElse(null);

				final var repository = ui.loadMetadataRepository(source, false, progress.split(40));
				final var matches = repository.query(
						QueryUtil.createLatestQuery(QueryUtil.createIUQuery(featureId)), progress.split(5));
				if (matches.isEmpty()) return error("The update site " + source + " does not yet publish " + label
						+ " (" + featureId + "). Please contact the GAMA maintainers.");
				final List<IInstallableUnit> units = List.copyOf(matches.toSet());
				final IInstallableUnit available = units.getFirst();
				if (installed != null && installed.getVersion().compareTo(available.getVersion()) >= 0) {
					display.asyncExec(() -> {
						try {
							if (!shell.isDisposed()) {
								MessageDialog.openInformation(shell, label + " is installed",
										label + " is already installed at the latest available version or newer.");
							}
						} finally {
							INSTALLING.set(false);
						}
					});
					return Status.OK_STATUS;
				}

				final ProfileChangeOperation operation;
				if (installed == null) {
					operation = ui.getInstallOperation(units, repositories);
				} else {
					final UpdateOperation update = ui.getUpdateOperation(List.of(installed), repositories);
					update.setSelectedUpdates(new Update[] { new Update(installed, available) });
					operation = update;
				}
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
								if (operation instanceof UpdateOperation update) {
									ui.openUpdateWizard(false, update, null);
								} else if (operation instanceof InstallOperation install) {
									ui.openInstallWizard(units, install, null);
								}
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
	}

	private static IStatus error(final String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message);
	}
}
