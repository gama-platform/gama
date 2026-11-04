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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.PhaseSetFactory;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProfileModificationJob;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Resolves a batch of features and schedules it through p2's normal progress and restart handling.
 */
final class InstallExtension {

	static final String PLUGIN_ID = "gama.ui.navigator";
	private static final URI ECLIPSE_REPOSITORY = URI.create("https://download.eclipse.org/releases/2026-03/");
	static final URI COPILOT_REPOSITORY = URI.create("https://gama-platform.org/gama.copilot/");
	private static final URI COPILOT_UPSTREAM_REPOSITORY =
			URI.create("https://azuredownloads-g3ahgwb5b8bkbxhd.b01.azurefd.net/github-copilot/");
	private static final AtomicBoolean INSTALLING = new AtomicBoolean();

	record Extension(URI repository, String featureId, String label, Version version, List<String> categories) {}

	private InstallExtension() {}

	static IProfile installationProfile(final ProvisioningUI ui) {
		if (ui == null || ui.getProfileId() == null) return null;
		final IProfileRegistry registry = ui.getSession().getProvisioningAgent().getService(IProfileRegistry.class);
		return registry == null ? null : registry.getProfile(ui.getProfileId());
	}

	static void install(final Shell shell, final Extension extension) {
		final WizardDialog dialog = new WizardDialog(shell, new InstallExtensionsWizard(extension));
		dialog.setPageSize(720, 480);
		dialog.open();
	}

	static InstallOperation resolve(final List<Extension> extensions, final IProgressMonitor monitor)
			throws ProvisionException {
		final ProvisioningUI ui = provisioningUI();
		if (ui.hasScheduledOperations() || !INSTALLING.compareAndSet(false, true)) {
			throw failure("Please finish the current software installation or update before installing plugins.");
		}
		try {
			final SubMonitor progress = SubMonitor.convert(monitor, "Preparing plugin installation", 100);
			final SubMonitor loading = progress.split(40).setWorkRemaining(extensions.size());
			final IProfile profile = installationProfile(ui);
			if (profile == null) throw failure("The GAMA installation profile is unavailable.");
			final var repositories = new LinkedHashSet<URI>();
			final List<IInstallableUnit> units = new ArrayList<>();
			for (final Extension extension : extensions) {
				loading.checkCanceled();
				final var repository = ui.loadMetadataRepository(extension.repository(), false, loading.split(1));
				final IInstallableUnit available = repository
						.query(QueryUtil.createIUQuery(extension.featureId(), extension.version()), null)
						.toSet().stream().findFirst().orElse(null);
				if (available == null) throw failure("The update site no longer publishes " + extension.label()
						+ " " + extension.version() + ". Refresh the plugin list and try again.");
				final IInstallableUnit installed = profile
						.query(QueryUtil.createIUQuery(extension.featureId()), null).toSet().stream()
						.max(Comparator.comparing(IInstallableUnit::getVersion)).orElse(null);
				if (installed != null && installed.getVersion().compareTo(available.getVersion()) >= 0) continue;
				units.add(available);
				repositories.add(extension.repository());
				if (extension.repository().equals(COPILOT_REPOSITORY)) repositories.add(COPILOT_UPSTREAM_REPOSITORY);
			}
			if (units.isEmpty()) throw failure("The selected plugins are already installed. Refresh the list.");
			for (final URI repository : repositories) {
				ui.getRepositoryTracker().addRepository(repository, null, ui.getSession());
			}
			// InstallOperation also replaces older installed versions, allowing mixed installs and updates.
			InstallOperation operation = installOperation(ui, profile, units, repositories);
			IStatus status = operation.resolveModal(progress.split(30));
			if (status.matches(IStatus.ERROR)) {
				progress.checkCanceled();
				repositories.add(ECLIPSE_REPOSITORY);
				ui.getRepositoryTracker().addRepository(ECLIPSE_REPOSITORY, null, ui.getSession());
				operation = installOperation(ui, profile, units, repositories);
				status = operation.resolveModal(progress.split(30));
			}
			progress.checkCanceled();
			if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
				throw new ProvisionException(new Status(status.getSeverity(), PLUGIN_ID,
						operation.getResolutionDetails(), status.getException()));
			}
			if (operation.getProvisioningPlan() == null || operation.getProvisioningPlan().isEmpty()) {
				throw failure("No plugin changes could be prepared. Refresh the list and try again.");
			}
			for (final IInstallableUnit removal : operation.getProvisioningPlan().getRemovals()
					.query(QueryUtil.ALL_UNITS, null)) {
				if (isExecutionEnvironment(removal)) {
					throw failure("These plugins require changing GAMA's Java execution environment. "
							+ "Install a compatible plugin version instead.");
				}
			}
			return operation;
		} finally {
			INSTALLING.set(false);
		}
	}

	private static InstallOperation installOperation(final ProvisioningUI ui, final IProfile profile,
			final List<IInstallableUnit> units, final LinkedHashSet<URI> repositories) {
		final URI[] locations = repositories.toArray(URI[]::new);
		final InstallOperation operation = ui.getInstallOperation(units, locations);
		operation.setProfileId(profile.getProfileId());
		final ProvisioningContext context = new ProvisioningContext(ui.getSession().getProvisioningAgent()) {
			@Override
			public String getProperty(final String key) {
				// InstallOperation's retry must not silently widen the explicitly selected update sites.
				return FOLLOW_REPOSITORY_REFERENCES.equals(key) ? Boolean.FALSE.toString() : super.getProperty(key);
			}

			@Override
			public IQueryable<IInstallableUnit> getMetadata(final IProgressMonitor monitor) {
				final var metadata = super.getMetadata(monitor);
				// Repository-generated EE units describe build targets, not the Java running GAMA.
				return (query, progress) -> query.perform(metadata.query(QueryUtil.ALL_UNITS, progress).stream()
						.filter(unit -> !isExecutionEnvironment(unit)).iterator());
			}
		};
		context.setMetadataRepositories(locations);
		context.setArtifactRepositories(locations);
		operation.setProvisioningContext(context);
		return operation;
	}

	private static boolean isExecutionEnvironment(final IInstallableUnit unit) {
		return unit.getProvidedCapabilities().stream().anyMatch(capability -> "osgi.ee".equals(capability.getNamespace()));
	}

	static void schedule(final InstallOperation operation, final boolean trustAll) throws ProvisionException {
		final ProvisioningUI ui = provisioningUI();
		if (ui.hasScheduledOperations() || !INSTALLING.compareAndSet(false, true)) {
			throw failure("Another software operation has started. Please finish it and try again.");
		}
		try {
			final IProfile profile = installationProfile(ui);
			final IProvisioningPlan plan = operation.getProvisioningPlan();
			if (profile == null || plan == null || !profile.getProfileId().equals(plan.getProfile().getProfileId())
					|| profile.getTimestamp() != plan.getProfile().getTimestamp()) {
				throw failure("The installation has changed. Go back and prepare your selection again.");
			}
			final ProvisioningJob job = operation.getProvisioningJob(null);
			if (!(job instanceof ProfileModificationJob modification)) {
				throw failure("The plugin installation job could not be created. Go back and try again.");
			}
			acceptLicenses(ui, plan);
			if (trustAll) {
				// Skip publisher/unsigned-code prompts only for this job; never change global trust settings.
				modification.setPhaseSet(PhaseSetFactory
						.createDefaultPhaseSetExcluding(new String[] { PhaseSetFactory.PHASE_CHECK_TRUST }));
			}
			modification.setRestartPolicy(ProvisioningJob.RESTART_ONLY);
			modification.setUser(true);
			ui.schedule(modification, StatusManager.LOG | StatusManager.SHOW);
		} finally {
			INSTALLING.set(false);
		}
	}

	private static void acceptLicenses(final ProvisioningUI ui, final IProvisioningPlan plan) throws ProvisionException {
		final var manager = ui.getLicenseManager();
		if (manager == null) throw failure("The p2 license acceptance service is unavailable.");
		for (final IInstallableUnit unit : plan.getAdditions().query(QueryUtil.ALL_UNITS, null)) {
			for (final var license : unit.getLicenses(null)) {
				if (!manager.accept(license)) throw failure("Unable to accept the license for " + unit.getId() + ".");
			}
		}
		if (plan.getInstallerPlan() != null) acceptLicenses(ui, plan.getInstallerPlan());
	}

	private static ProvisioningUI provisioningUI() throws ProvisionException {
		final ProvisioningUI ui = ProvisioningUI.getDefaultUI();
		if (ui == null || ui.getProfileId() == null) {
			throw failure("Plugins can only be installed in a provisioned GAMA installation.");
		}
		return ui;
	}

	private static ProvisionException failure(final String message) {
		return new ProvisionException(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}
}
