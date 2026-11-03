/*******************************************************************************************************
 *
 * InstallExtensionsContribution.java, in gama.ui.navigator, is part of the source code of the GAMA modeling
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.ICompositeRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.statushandlers.StatusManager;

import gama.ui.navigator.commands.InstallExtension.Extension;
import gama.ui.shared.resources.GamaIcon;

/**
 * Discovers installable features from the GAMA and Copilot p2 repositories without blocking menu opening.
 */
public class InstallExtensionsContribution extends CompoundContributionItem implements IStartup {

	private static final URI GAMA_REPOSITORY = URI.create("https://updates.gama-platform.org/2026-06/");
	private static final URI PLUGINS_REPOSITORY = URI.create("https://updates.gama-platform.org/plugin/2026-06/");
	private static final List<URI> REPOSITORIES =
			List.of(GAMA_REPOSITORY, PLUGINS_REPOSITORY, InstallExtension.COPILOT_REPOSITORY);
	private static final long REFRESH_INTERVAL = TimeUnit.HOURS.toNanos(1);

	private static final Map<URI, List<Extension>> catalogue = new LinkedHashMap<>();
	private static long lastRefresh;
	private static int pending;
	private static boolean failed;
	private static MenuManager activeMenu;
	private static final List<Job> jobs = new ArrayList<>();

	@Override
	public void earlyStartup() {
		if (Boolean.getBoolean("exported_runner")) return;
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display.isDisposed()) return;
		display.asyncExec(() -> {
			if (lastRefresh == 0) refresh(display);
		});
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		final MenuManager menu = new MenuManager("Install GAMA extensions...", "gama.ui.install.extensions");
		menu.setImageDescriptor(GamaIcon.named("generic/menu.install.gama").descriptor());
		menu.setRemoveAllWhenShown(true);
		menu.addMenuListener(_ -> {
			activeMenu = menu;
			if (pending == 0 && (lastRefresh == 0 || System.nanoTime() - lastRefresh >= REFRESH_INTERVAL)) {
				refresh(menu.getMenu().getDisplay());
			}
			populate(menu);
		});
		return new IContributionItem[] { menu };
	}

	private static void populate(final MenuManager menu) {
		menu.removeAll();
		final Map<String, Version> installed = new LinkedHashMap<>();
		final var profile = InstallExtension.installationProfile(ProvisioningUI.getDefaultUI());
		if (profile != null) {
			for (final IInstallableUnit unit : profile.query(QueryUtil.createIUGroupQuery(), null)) {
				installed.merge(unit.getId(), unit.getVersion(), (first, second) ->
						first.compareTo(second) >= 0 ? first : second);
			}
		}
		final var entries = new LinkedHashMap<String, Extension>();
		catalogue.values().forEach(list -> list.forEach(extension -> entries.merge(extension.featureId(), extension,
				InstallExtensionsContribution::mergeExtensions)));
		final Map<String, List<Extension>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		entries.values().forEach(extension -> extension.categories().forEach(category ->
				groups.computeIfAbsent(category, _ -> new ArrayList<>()).add(extension)));
		groups.forEach((category, extensions) -> {
			final MenuManager categoryMenu = new MenuManager(category);
			menu.add(categoryMenu);
			extensions.stream().sorted(Comparator.comparing(Extension::label, String.CASE_INSENSITIVE_ORDER))
					.forEach(extension -> {
						final Version version = installed.get(extension.featureId());
						final boolean current = version != null && version.compareTo(extension.version()) >= 0;
						final String prefix = version == null ? "Install " : current ? "(Installed) " : "Update ";
						final Action action = new Action(prefix + extension.label() + " extension...") {
							@Override
							public void run() {
								InstallExtension.install(menu.getMenu().getShell(), extension);
							}
						};
						action.setEnabled(!current);
						categoryMenu.add(action);
					});
		});
		if (pending > 0) {
			addMessage(menu, "Loading extensions (" + pending + " update site(s) remaining)...");
		}
		if (failed) {
			addMessage(menu, "Some update sites could not be read; refresh to retry.");
		} else if (pending == 0 && entries.isEmpty()) {
			addMessage(menu, "No extensions published.");
		}
		menu.add(new Separator());
		if (pending > 0) {
			menu.add(new Action("Cancel refresh") {
				@Override
				public void run() {
					jobs.forEach(Job::cancel);
				}
			});
		}
		final Action refresh = new Action("Refresh extension list") {
			@Override
			public void run() {
				refresh(menu.getMenu().getDisplay());
				populate(menu);
				menu.update(true);
			}
		};
		refresh.setEnabled(pending == 0);
		menu.add(refresh);
	}

	private static void addMessage(final IMenuManager menu, final String message) {
		final Action action = new Action(message) {};
		action.setEnabled(false);
		menu.add(new ActionContributionItem(action));
	}

	private static Extension mergeExtensions(final Extension first, final Extension second) {
		final int comparison = first.version().compareTo(second.version());
		if (comparison != 0) return comparison > 0 ? first : second;
		final List<String> categories = new ArrayList<>(first.categories());
		second.categories().forEach(category -> {
			if (!categories.contains(category)) categories.add(category);
		});
		if (categories.size() > 1) categories.remove("Uncategorized");
		return new Extension(first.repository(), first.featureId(), first.label(), first.version(),
				List.copyOf(categories));
	}

	private static void refresh(final Display display) {
		if (pending > 0) return;
		pending = REPOSITORIES.size();
		failed = false;
		jobs.clear();
		for (final URI source : REPOSITORIES) {
			refreshSite(source, display);
		}
	}

	private static void refreshSite(final URI source, final Display display) {
		final List<Extension> extensions = new ArrayList<>();
		final Job job = Job.create("Discovering GAMA extensions: " + source.getHost() + source.getPath(), monitor -> {
			final ProvisioningUI ui = ProvisioningUI.getDefaultUI();
			final IMetadataRepositoryManager manager = ui == null ? null
					: ui.getSession().getProvisioningAgent().getService(IMetadataRepositoryManager.class);
			if (manager == null) {
				return new Status(IStatus.ERROR, InstallExtension.PLUGIN_ID,
						"The p2 metadata repository service is unavailable.");
			}
			final SubMonitor progress = SubMonitor.convert(monitor, "Reading " + source, 100);
			try {
				discover(manager, source, source, extensions, new HashSet<>(), progress, true);
				return Status.OK_STATUS;
			} catch (final OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (final ProvisionException e) {
				return new Status(IStatus.ERROR, InstallExtension.PLUGIN_ID,
						"Unable to read extensions from " + source + ". Check the connection and refresh to retry.", e);
			}
		});
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				final IStatus result = event.getResult();
				if (!result.isOK() && !result.matches(IStatus.CANCEL)) {
					StatusManager.getManager().handle(result, StatusManager.LOG | StatusManager.SHOW);
				}
				if (display.isDisposed()) return;
				display.asyncExec(() -> {
					pending--;
					if (pending == 0) {
						lastRefresh = System.nanoTime();
						jobs.clear();
					}
					failed |= !result.isOK();
					// Keep the last successful list for any temporarily unavailable site.
					if (result.isOK()) catalogue.put(source, List.copyOf(extensions));
					if (activeMenu != null && activeMenu.getMenu() != null && !activeMenu.getMenu().isDisposed()) {
						populate(activeMenu);
						activeMenu.update(true);
					}
				});
			}
		});
		jobs.add(job);
		job.schedule();
	}

	private static void discover(final IMetadataRepositoryManager manager, final URI location,
			final URI source, final List<Extension> extensions, final Set<URI> visited, final SubMonitor progress,
			final boolean refresh)
			throws ProvisionException {
		if (!visited.add(location)) return;
		progress.setWorkRemaining(100);
		progress.subTask("Reading " + location);
		// Composite refresh already refreshes all descendants. Traversal must only read the resulting cache.
		final var repository = refresh && manager.contains(location)
				? manager.refreshRepository(location, progress.split(50))
				: manager.loadRepository(location, progress.split(50));
		if (repository instanceof ICompositeRepository<?> composite) {
			final var children = composite.getChildren();
			progress.setWorkRemaining(children.size());
			for (final URI child : children) {
				final URI resolved = location.resolve(child);
				discover(manager, resolved, source, extensions, visited, progress.split(1), false);
			}
			return;
		}
		final var categories = repository.query(QueryUtil.createIUCategoryQuery(), progress.split(20)).toSet();
		final var units = repository.query(QueryUtil.createLatestQuery(QueryUtil.createIUGroupQuery()), progress.split(30))
				.toSet();
		for (final IInstallableUnit unit : units) {
			final List<String> labels = categories.stream()
					.filter(category -> category.getRequirements().stream().anyMatch(requirement -> requirement.isMatch(unit)))
					.map(InstallExtensionsContribution::unitLabel).distinct().toList();
			extensions.add(new Extension(source, unit.getId(), unitLabel(unit), unit.getVersion(),
					labels.isEmpty() ? List.of("Uncategorized") : labels));
		}
	}

	private static String unitLabel(final IInstallableUnit unit) {
		final String name = unit.getProperty(IInstallableUnit.PROP_NAME, null);
		return name == null || name.isBlank() ? unit.getId() : name;
	}
}
