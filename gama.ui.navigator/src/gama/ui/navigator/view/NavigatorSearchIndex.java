/*******************************************************************************************************
 *
 * NavigatorSearchIndex.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import gama.api.GAMA;
import gama.api.constants.GamlFileExtension;
import gama.dev.DEBUG;
import gaml.compiler.resource.GamlFileInfo;

/** Background-maintained index of workspace resource names and GAML tags for navigator searches. */
final class NavigatorSearchIndex implements IResourceChangeListener {

	/**
	 * The Record Results.
	 *
	 * @param visiblePaths
	 *            the visible paths
	 * @param matchingContainers
	 *            the matching containers
	 */
	record Results(Set<IPath> visiblePaths, Set<IPath> matchingContainers) {}

	/**
	 * The Record Entry.
	 *
	 * @param path
	 *            the path
	 * @param name
	 *            the name
	 * @param tags
	 *            the tags
	 * @param model
	 *            the model
	 * @param container
	 *            the container
	 */
	private record Entry(IPath path, String name, List<String> tags, boolean model, boolean container) {}

	/**
	 * The Record Change.
	 *
	 * @param path
	 *            the path
	 * @param resource
	 *            the resource
	 * @param removed
	 *            the removed
	 */
	private record Change(IPath path, IResource resource, boolean removed) {}

	/** The Constant NGRAM_SIZE. */
	private static final int NGRAM_SIZE = 3;

	/** The Constant INSTANCE. */
	private static final NavigatorSearchIndex INSTANCE = new NavigatorSearchIndex();

	/** The lock. */
	private final Object lock = new Object();

	/** The initialized. */
	private final CountDownLatch initialized = new CountDownLatch(1);

	/** The entries. */
	private final Map<IPath, Entry> entries = new HashMap<>();

	/** The postings. */
	private final Map<String, Set<IPath>> postings = new HashMap<>();

	/** The pending changes. */
	private final List<Change> pendingChanges = new ArrayList<>();

	/** The initialization finished. */
	private boolean initializationFinished;

	/** The change job scheduled. */
	private boolean changeJobScheduled;

	/** The initialization job. */
	private final Job initializationJob = new Job("Index navigator search resources") {
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final Map<IPath, Entry> initial = new HashMap<>();
			try {
				GAMA.getWorkspaceManager().getRoot().accept((IResourceProxyVisitor) proxy -> {
					if (monitor.isCanceled()) throw new OperationCanceledException();
					final int type = proxy.getType();
					if (type == IResource.PROJECT || type == IResource.FOLDER || type == IResource.FILE) {
						final Entry entry = createEntry(proxy.requestResource());
						if (entry != null) { initial.put(entry.path(), entry); }
					}
					return proxy.isAccessible() && type != IResource.FILE;
				}, IResource.NONE);
			} catch (final OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (final CoreException e) {
				DEBUG.ERR("Unable to initialize navigator search index", e);
			} finally {
				synchronized (lock) {
					entries.clear();
					postings.clear();
					initial.values().forEach(NavigatorSearchIndex.this::addEntry);
					initializationFinished = true;
					if (!pendingChanges.isEmpty()) { scheduleChangeJob(); }
				}
				initialized.countDown();
			}
			return Status.OK_STATUS;
		}
	};

	/** The change job. */
	private final Job changeJob = new Job("Update navigator search index") {
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			while (!monitor.isCanceled()) {
				final List<Change> changes;
				synchronized (lock) {
					if (pendingChanges.isEmpty()) {
						changeJobScheduled = false;
						return Status.OK_STATUS;
					}
					changes = new ArrayList<>(pendingChanges);
					pendingChanges.clear();
				}
				for (final Change change : changes) {
					if (monitor.isCanceled()) return Status.CANCEL_STATUS;
					if (change.removed() || !change.resource().exists() || !change.resource().isAccessible()) {
						removePath(change.path());
					} else {
						final Entry entry = createEntry(change.resource());
						if (entry == null) {
							removePath(change.path());
						} else {
							putEntry(entry);
						}
					}
				}
			}
			synchronized (lock) {
				changeJobScheduled = false;
			}
			return Status.CANCEL_STATUS;
		}
	};

	/**
	 * Instantiates a new navigator search index.
	 */
	private NavigatorSearchIndex() {
		GAMA.getWorkspaceManager().getWorkspace().addResourceChangeListener(this, POST_CHANGE);
		initializationJob.setSystem(true);
		changeJob.setSystem(true);
		initializationJob.schedule();
	}

	/**
	 * Gets the single instance of NavigatorSearchIndex.
	 *
	 * @return single instance of NavigatorSearchIndex
	 */
	static NavigatorSearchIndex getInstance() { return INSTANCE; }

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		final IResourceDelta delta = event.getDelta();
		if (delta == null) return;
		try {
			delta.accept(node -> {
				final IResource resource = node.getResource();
				// The workspace root groups deltas; it is not an indexed resource.
				if (resource.getType() == IResource.ROOT) return true;
				final int kind = node.getKind();
				if (kind == IResourceDelta.REMOVED) {
					enqueue(new Change(resource.getFullPath(), resource, true));
					return false;
				}
				if (kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED) {
					enqueue(new Change(resource.getFullPath(), resource, false));
				}
				return true;
			});
		} catch (final CoreException e) {
			DEBUG.ERR("Unable to collect navigator search index changes", e);
		}
	}

	/**
	 * Enqueue.
	 *
	 * @param change
	 *            the change
	 */
	private void enqueue(final Change change) {
		synchronized (lock) {
			pendingChanges.add(change);
			if (initializationFinished) { scheduleChangeJob(); }
		}
	}

	/**
	 * Schedule change job.
	 */
	private void scheduleChangeJob() {
		if (changeJobScheduled) return;
		changeJobScheduled = true;
		changeJob.schedule();
	}

	/**
	 * Search.
	 *
	 * @param query
	 *            the query
	 * @param modelsOnly
	 *            the models only
	 * @param monitor
	 *            the monitor
	 * @return the results
	 */
	Results search(final String query, final boolean modelsOnly, final IProgressMonitor monitor) {
		try {
			while (!initialized.await(100, TimeUnit.MILLISECONDS)) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		if (monitor.isCanceled()) return null;
		synchronized (lock) {
			final Set<IPath> candidates = candidates(query);
			final Set<IPath> visiblePaths = new HashSet<>();
			final Set<IPath> matchingContainers = new HashSet<>();
			for (final IPath path : candidates) {
				final Entry entry = entries.get(path);
				if (entry == null || modelsOnly && !entry.model()) { continue; }
				final boolean nameMatches = entry.name().contains(query);
				final boolean tagMatches = entry.model() && entry.tags().stream().anyMatch(tag -> tag.contains(query));
				if (!nameMatches && !tagMatches) { continue; }
				visiblePaths.add(path);
				if (entry.container()) { matchingContainers.add(path); }
				for (IPath parent = path.removeLastSegments(1); parent.segmentCount() > 0; parent =
						parent.removeLastSegments(1)) {
					visiblePaths.add(parent);
				}
			}
			return new Results(Set.copyOf(visiblePaths), Set.copyOf(matchingContainers));
		}
	}

	/**
	 * Candidates.
	 *
	 * @param query
	 *            the query
	 * @return the sets the
	 */
	private Set<IPath> candidates(final String query) {
		if (query.length() < NGRAM_SIZE) return new HashSet<>(entries.keySet());
		Set<IPath> result = null;
		for (int i = 0; i <= query.length() - NGRAM_SIZE; i++) {
			final Set<IPath> paths = postings.get(query.substring(i, i + NGRAM_SIZE));
			if (paths == null) return Set.of();
			if (result == null) {
				result = new HashSet<>(paths);
			} else {
				result.retainAll(paths);
				if (result.isEmpty()) return Set.of();
			}
		}
		return result == null ? Set.of() : result;
	}

	/**
	 * Creates the entry.
	 *
	 * @param resource
	 *            the resource
	 * @return the entry
	 */
	private static Entry createEntry(final IResource resource) {
		if (!resource.isAccessible()) return null;
		final int type = resource.getType();
		if (type != IResource.PROJECT && type != IResource.FOLDER && type != IResource.FILE) return null;
		final boolean model = type == IResource.FILE && GamlFileExtension.isAny(resource.getName());
		final List<String> tags;
		if (model) {
			final var metadata = GAMA.getMetadataProvider().getMetaData(resource, false, false);
			tags = metadata instanceof GamlFileInfo info
					? info.getTags().stream().map(tag -> tag.toLowerCase(Locale.ROOT)).toList() : List.of();
		} else {
			tags = List.of();
		}
		return new Entry(resource.getFullPath(), resource.getName().toLowerCase(Locale.ROOT), tags, model,
				type == IResource.PROJECT || type == IResource.FOLDER);
	}

	/**
	 * Put entry.
	 *
	 * @param entry
	 *            the entry
	 */
	private void putEntry(final Entry entry) {
		synchronized (lock) {
			removeEntry(entries.remove(entry.path()));
			addEntry(entry);
		}
	}

	/**
	 * Removes the path.
	 *
	 * @param path
	 *            the path
	 */
	private void removePath(final IPath path) {
		synchronized (lock) {
			final List<IPath> removed = entries.keySet().stream().filter(path::isPrefixOf).toList();
			for (final IPath removedPath : removed) { removeEntry(entries.remove(removedPath)); }
		}
	}

	/**
	 * Adds the entry.
	 *
	 * @param entry
	 *            the entry
	 */
	private void addEntry(final Entry entry) {
		entries.put(entry.path(), entry);
		for (final String value : indexedValues(entry)) {
			for (final String gram : grams(value)) {
				Set<IPath> paths = postings.get(gram);
				if (paths == null) {
					paths = new HashSet<>();
					postings.put(gram, paths);
				}
				paths.add(entry.path());
			}
		}
	}

	/**
	 * Removes the entry.
	 *
	 * @param entry
	 *            the entry
	 */
	private void removeEntry(final Entry entry) {
		if (entry == null) return;
		for (final String value : indexedValues(entry)) {
			for (final String gram : grams(value)) {
				final Set<IPath> paths = postings.get(gram);
				if (paths != null) {
					paths.remove(entry.path());
					if (paths.isEmpty()) { postings.remove(gram); }
				}
			}
		}
	}

	/**
	 * Indexed values.
	 *
	 * @param entry
	 *            the entry
	 * @return the list
	 */
	private static List<String> indexedValues(final Entry entry) {
		final List<String> values = new ArrayList<>(entry.tags().size() + 1);
		values.add(entry.name());
		values.addAll(entry.tags());
		return values;
	}

	/**
	 * Grams.
	 *
	 * @param value
	 *            the value
	 * @return the sets the
	 */
	private static Set<String> grams(final String value) {
		final Set<String> result = new HashSet<>();
		for (int i = 0; i <= value.length() - NGRAM_SIZE; i++) { result.add(value.substring(i, i + NGRAM_SIZE)); }
		return result;
	}
}
