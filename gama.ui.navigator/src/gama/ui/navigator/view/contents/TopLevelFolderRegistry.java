/*******************************************************************************************************
 *
 * TopLevelFolderRegistry.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import gama.workspace.manager.ProjectCategory;

/** The same declaration and factory lifecycle serves built-in categories and optional independent roots. */
final class TopLevelFolderRegistry {

	private static final String PLUGIN_ID = "gama.ui.navigator";

	private record Contribution(String id, String before, int order, ProjectCategory category,
			IConfigurationElement element) {}

	private final NavigatorRoot root;
	private final List<Contribution> contributions = new ArrayList<>();
	private final Map<String, TopLevelFolder> folders = new HashMap<>();
	private final Set<String> loading = new HashSet<>();
	private final Set<String> failed = new HashSet<>();

	TopLevelFolderRegistry(final NavigatorRoot root) {
		this.root = root;
		final Set<String> ids = new HashSet<>();
		final var elements = Platform.getExtensionRegistry().getConfigurationElementsFor(PLUGIN_ID + ".topLevelFolders");
		java.util.Arrays.sort(elements, Comparator
				.comparing((IConfigurationElement e) -> !PLUGIN_ID.equals(e.getContributor().getName()))
				.thenComparing(e -> e.getContributor().getName() + ":" + e.getAttribute("id")));
		for (final IConfigurationElement element : elements) {
			try {
				if (!"folder".equals(element.getName())) throw new IllegalArgumentException("Expected a folder element");
				final String id = element.getAttribute("id");
				if (id == null || id.isBlank() || !ids.add(id))
					throw new IllegalArgumentException("Missing or duplicate folder id: " + id);
				final String value = element.getAttribute("category");
				final ProjectCategory category = value == null ? null : ProjectCategory.valueOf(value);
				if (category != null && (!PLUGIN_ID.equals(element.getContributor().getName())
						|| !id.equals(category.name().toLowerCase(java.util.Locale.ROOT))))
					throw new IllegalArgumentException("Workspace categories are reserved for the navigator");
				final String before = element.getAttribute("before") == null ? "user" : element.getAttribute("before");
				final String order = element.getAttribute("order");
				contributions.add(new Contribution(id, before, order == null ? 0 : Integer.parseInt(order),
						category, element));
			} catch (final RuntimeException e) {
				log("Invalid top-level folder declaration from " + element.getContributor().getName(), e);
			}
		}
		contributions.sort(Comparator.comparingInt(Contribution::order).thenComparing(Contribution::id));
		final Set<String> anchors = new HashSet<>();
		for (final Contribution contribution : contributions) {
			if (contribution.category() != null) { anchors.add(contribution.id()); }
		}
		contributions.removeIf(contribution -> {
			if (contribution.category() != null || anchors.contains(contribution.before())) return false;
			log("Unknown placement anchor for " + contribution.id() + ": " + contribution.before(),
					new IllegalArgumentException("Placement must refer to a declared workspace category"));
			return true;
		});
	}

	TopLevelFolder getWorkspaceFolder(final String id) {
		for (final Contribution contribution : contributions) {
			if (contribution.category() != null && contribution.id().equals(id)) {
				final TopLevelFolder folder = instantiate(contribution);
				if (folder != null) return folder;
				break;
			}
		}
		throw new IllegalStateException("Required navigator workspace folder is unavailable: " + id);
	}

	TopLevelFolder[] getWorkspaceFolders() {
		return contributions.stream().filter(c -> c.category() != null)
				.map(c -> getWorkspaceFolder(c.id())).toArray(TopLevelFolder[]::new);
	}

	void resetWorkspaceFolders() {
		for (final TopLevelFolder folder : folders.values()) {
			if (folder.isWorkspaceFolder()) { folder.initializeChildren(); }
		}
	}

	void replaceWorkspaceFolder(final String id, final TopLevelFolder folder) {
		final TopLevelFolder current = getWorkspaceFolder(id);
		if (folder == null || folder.getParent() != root || folder.getCategory() != current.getCategory())
			throw new IllegalArgumentException("Replacement must retain the workspace folder's root and category");
		folder.setContributionId(id);
		folders.put(id, folder);
	}

	TopLevelFolder[] getFolders() {
		final List<TopLevelFolder> result = new ArrayList<>();
		for (final Contribution anchor : contributions) {
			if (anchor.category() == null) continue;
			for (final Contribution contribution : contributions) {
				if (contribution.category() == null && contribution.before().equals(anchor.id())) {
					final TopLevelFolder folder = instantiate(contribution);
					if (folder != null) { result.add(folder); }
				}
			}
			result.add(getWorkspaceFolder(anchor.id()));
		}
		return result.toArray(TopLevelFolder[]::new);
	}

	private TopLevelFolder instantiate(final Contribution contribution) {
		final String id = contribution.id();
		if (folders.containsKey(id)) return folders.get(id);
		if (failed.contains(id)) return null;
		if (!loading.add(id)) throw new IllegalStateException("Recursive navigator folder factory: " + id);
		TopLevelFolder folder = null;
		try {
			final Object factory = contribution.element().createExecutableExtension("class");
			if (!(factory instanceof ITopLevelFolderProvider provider))
				throw new IllegalArgumentException("Factory must implement ITopLevelFolderProvider");
			folder = provider.createFolder(root);
			if (folder == null || folder.getParent() != root || folder.getCategory() != contribution.category())
				throw new IllegalArgumentException("Factory must return a folder attached to its root and declared category");
			folder.setContributionId(id);
			folders.put(id, folder);
			return folder;
		} catch (final CoreException | RuntimeException | LinkageError e) {
			failed.add(id);
			log("Unable to load top-level folder " + id + " from "
					+ contribution.element().getContributor().getName(), e);
			if (folder != null && !folder.isWorkspaceFolder()) { dispose(folder); }
			return null;
		} finally {
			loading.remove(id);
		}
	}

	void disposeContributions() {
		folders.entrySet().removeIf(entry -> {
			if (entry.getValue().isWorkspaceFolder()) return false;
			dispose(entry.getValue());
			return true;
		});
		failed.clear();
	}

	private static void dispose(final TopLevelFolder folder) {
		try {
			folder.dispose();
		} catch (final RuntimeException | LinkageError e) {
			log("Unable to dispose top-level folder " + folder.getName(), e);
		}
	}

	private static void log(final String message, final Throwable cause) {
		Platform.getLog(TopLevelFolderRegistry.class).log(new Status(org.eclipse.core.runtime.IStatus.ERROR,
				PLUGIN_ID, message, cause));
	}
}
