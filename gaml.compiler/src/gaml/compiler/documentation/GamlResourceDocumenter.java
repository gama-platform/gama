/*******************************************************************************************************
 *
 * GamlResourceDocumenter.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.documentation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.documentation.IDocManager;
import gama.dev.DEBUG;
import gaml.compiler.resource.GamlResourceServices;

/**
 * Central documentation manager for GAML resources.
 *
 * <p>
 * {@code GamlResourceDocumenter} maintains two concurrent maps:
 * </p>
 * <ul>
 * <li>{@link #documentationTasks} — one {@link GamlResourceDocumentationTask} per open resource URI. Each task owns a
 * background Eclipse {@link org.eclipse.core.runtime.jobs.Job} that executes documentation work asynchronously.</li>
 * <li>{@link #documentedObjects} — the global store mapping every documented EObject fragment URI to its
 * {@link DocumentedObject} (documentation node + set of resources that reference it).</li>
 * </ul>
 *
 * <h2>Generation numbers</h2>
 * <p>
 * Every call to {@link #doDocument} atomically increments the task's <em>generation counter</em> and captures the new
 * value. The asynchronous work closure checks the generation before writing to the store; if the generation has since
 * been superseded (because the user saved the file again), the stale work is silently dropped.
 * </p>
 *
 * <h2>Thread safety</h2>
 * <p>
 * Both maps use {@link ConcurrentHashMap}. The generation counter inside each task is an {@link java.util.concurrent.atomic.AtomicInteger}.
 * The {@code resources} set inside {@link DocumentedObject} uses {@link ConcurrentHashMap#newKeySet()}.
 * </p>
 *
 * @author drogoul
 * @since 13 avr. 2014
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlResourceDocumenter implements IDocManager {

	static {
		DEBUG.OFF();
	}

	/**
	 * Per-resource documentation tasks. One entry per open/edited resource URI.
	 * Uses {@link ConcurrentHashMap} to support concurrent access from validation and UI threads.
	 */
	final Map<URI, GamlResourceDocumentationTask> documentationTasks = new ConcurrentHashMap();

	/**
	 * Global store mapping each documented EObject fragment URI to its {@link DocumentedObject}.
	 * Uses {@link ConcurrentHashMap} to support concurrent reads from the hover/tooltip thread
	 * and concurrent writes from the background documentation jobs.
	 */
	final Map<URI, DocumentedObject> documentedObjects = new ConcurrentHashMap();

	/**
	 * Associates a documentation node with the set of resource URIs that contributed it.
	 *
	 * <p>
	 * When a resource is invalidated, all fragment URIs whose {@code resources} set becomes empty after removing the
	 * invalidated resource are also removed from {@link #documentedObjects}.
	 * </p>
	 *
	 * @param node
	 *            the compressed documentation node for this EObject
	 * @param resources
	 *            the set of resource URIs that have documented this EObject; must be thread-safe (created via
	 *            {@link ConcurrentHashMap#newKeySet()})
	 */
	record DocumentedObject(DocumentationNode node, Set<URI> resources) {}

	/**
	 * Schedules an arbitrary documentation {@link Runnable} for the given resource.
	 *
	 * <p>
	 * The task is silently dropped if the resource is no longer being edited (i.e. has no registered listener) or if
	 * {@code run} is {@code null}.
	 * </p>
	 *
	 * @param res
	 *            the URI of the resource that owns the task
	 * @param run
	 *            the runnable to enqueue; may be {@code null} (in which case this method is a no-op)
	 */
	public void addDocumentationTask(final URI res, final Runnable run) {
		if (run == null || !isTaskValid(res)) return;
		getTaskFor(res).add(run);
	}

	/**
	 * Schedules documentation for a single EObject / description pair.
	 *
	 * <p>
	 * The actual work is deferred to the background job so that the calling (validation) thread is not blocked.
	 * The generation is read <em>inside</em> the lambda so that it reflects the generation that is current when the
	 * task actually runs, rather than when it was submitted.
	 * </p>
	 *
	 * @param res
	 *            the URI of the resource that contains {@code object}
	 * @param object
	 *            the EObject to document
	 * @param desc
	 *            the description providing the documentation content
	 */
	@Override
	public void setGamlDocumentation(final URI res, final EObject object, final IGamlDescription desc) {
		addDocumentationTask(res,
				() -> internalSetGamlDocumentation(res, getCurrentDocGenerationFor(res), object, desc));
	}

	/**
	 * Stores documentation for a single EObject in the global {@link #documentedObjects} store.
	 *
	 * <p>
	 * The method first checks that the resource is still being edited and that the supplied {@code generation}
	 * matches the task's current generation. If either check fails the call is a no-op and returns {@code false},
	 * ensuring that work from a superseded compilation pass is silently discarded.
	 * </p>
	 *
	 * <p>
	 * Uses {@link ConcurrentHashMap#computeIfAbsent} to atomically create the {@link DocumentedObject} entry,
	 * preventing a put-if-absent race where two concurrent workers could both observe a missing entry and both
	 * insert conflicting objects.
	 * </p>
	 *
	 * @param res
	 *            the resource URI that is documenting this object
	 * @param generation
	 *            the generation at which this documentation was produced; checked against the task's current generation
	 * @param object
	 *            the EObject to document
	 * @param desc
	 *            the description from which documentation is extracted
	 * @return {@code true} if the object was successfully documented; {@code false} if the task was stale or an error
	 *         occurred
	 */
	boolean internalSetGamlDocumentation(final URI res, final int generation, final EObject object,
			final IGamlDescription desc) {
		try {
			if (!isTaskValid(res) || !isValidGeneration(res, generation)) return false;
			URI fragment = EcoreUtil.getURI(object);
			// computeIfAbsent is atomic: prevents two concurrent callers from both observing a missing
			// entry and both inserting a DocumentedObject, which would leave one of them orphaned.
			DocumentedObject documented = documentedObjects.computeIfAbsent(fragment,
					k -> new DocumentedObject(new DocumentationNode(desc), ConcurrentHashMap.newKeySet()));
			documented.resources.add(res);
			getTaskFor(res).objects.add(fragment);
			return true;
		} catch (final RuntimeException e) {
			DEBUG.ERR("Error in documenting " + res.lastSegment(), e);
			return false;
		}
	}

	/**
	 * Schedules a full documentation pass for a model description.
	 *
	 * <p>
	 * This is the primary entry point, called once per successful validation. It:
	 * </p>
	 * <ol>
	 * <li>Atomically starts a new generation by calling {@link GamlResourceDocumentationTask#incrementGeneration()},
	 * capturing the returned generation number in a local variable so that the closure and any subsequent
	 * {@link #internalSetGamlDocumentation} calls all use <em>exactly the same</em> generation value.</li>
	 * <li>Takes an immutable snapshot of {@code additionalExpressions} so that the async closure operates on a
	 * stable copy even if the caller clears or modifies the map after returning.</li>
	 * <li>Enqueues a single {@link Runnable} that documents the full model tree and the additional
	 * expression map.</li>
	 * </ol>
	 *
	 * @param res
	 *            the URI of the resource being documented
	 * @param desc
	 *            the root model description; may be {@code null} (documentation is then skipped)
	 * @param additionalExpressions
	 *            expressions collected during validation that are not reachable through the model-description tree
	 */
	// To be called once the validation has been done
	@Override
	public void doDocument(final URI res, final IModelDescription desc,
			final Map<EObject, IGamlDescription> additionalExpressions) {
		GamlResourceDocumentationTask task = getTaskFor(res);
		// Capture the new generation atomically; the return value of incrementGeneration() is the
		// generation we must use for all work in this pass — avoids a TOCTOU between
		// incrementGeneration() and a separate getCurrentDocGenerationFor() read.
		final int generation = task.incrementGeneration();
		// Defensive snapshot: the caller (DocumentationContext) may clear additionalExpressions after
		// doDocument() returns but before the async job runs.
		final Map<EObject, IGamlDescription> snapshot = Map.copyOf(additionalExpressions);
		task.add(() -> {
			// Wrap the entire traversal so that an exception thrown by visitOwnChildren() or any
			// model-description callback does not escape to the job loop (issue C). Escaping would
			// terminate the loop, mark the job as failed, and prevent future job.schedule() calls
			// from executing. The try/catch in the job loop (GamlResourceDocumentationTask) also
			// provides a safety net, but it is better to keep the failure as local as possible.
			try {
				internalDoDocument(res, generation, desc);
			} catch (final Throwable t) {
				DEBUG.ERR("Exception while documenting model tree for " + res.lastSegment(), t);
			}
			snapshot.forEach((e, d) -> internalSetGamlDocumentation(res, generation, e, d));
		});
	}

	/**
	 * Recursively documents a description and all of its own children.
	 *
	 * <p>
	 * The recursion is cut short as soon as {@link #internalSetGamlDocumentation} returns {@code false} (stale
	 * generation or resource no longer edited), preventing unnecessary work when a newer compilation pass has already
	 * superseded this one.
	 * </p>
	 *
	 * @param resource
	 *            the resource URI
	 * @param generation
	 *            the generation stamp for this pass
	 * @param desc
	 *            the description to document; {@code null} is handled gracefully
	 * @return {@code true} if documentation should continue; {@code false} to abort the current subtree
	 */
	private boolean internalDoDocument(final URI resource, final int generation, final IDescription desc) {
		if (desc == null) return false;
		final EObject e = desc.getUnderlyingElement();
		if (e == null) return true; // We return true to continue exploring if other descriptions should be documented
		if (!internalSetGamlDocumentation(resource, generation, e, desc)) return false;
		return desc.visitOwnChildren(d -> internalDoDocument(resource, generation, d));
	}

	/**
	 * Returns the documentation associated with the given EObject.
	 *
	 * <p>
	 * Called by UI components (hover provider, content assist, etc.) to display documentation for the model element
	 * at the current editor cursor position.
	 * </p>
	 *
	 * @param object
	 *            the EObject to look up; {@code null} returns {@code null}
	 * @return the associated {@link IGamlDescription}, or {@code null} if the object has not been documented yet
	 */
	@Override
	public IGamlDescription getGamlDocumentation(final EObject object) {
		if (object == null) return null;
		DocumentedObject documented = documentedObjects.get(EcoreUtil.getURI(object));
		return documented == null ? null : documented.node;
	}

	/**
	 * Removes all documentation associated with the given resource URI.
	 *
	 * <p>
	 * For each EObject fragment URI that was documented by this resource, the resource is removed from the
	 * fragment's {@code resources} set. If the set becomes empty, the fragment entry is removed from
	 * {@link #documentedObjects} entirely to avoid memory leaks.
	 * </p>
	 *
	 * <p>
	 * <strong>Race-condition note:</strong> A background documentation job may still hold a reference to the task
	 * and write new fragment URIs into {@code task.objects} even after this method has removed the task from
	 * {@link #documentationTasks}. To handle this, we snapshot {@code task.objects} <em>before</em> removing the
	 * task. Entries added after the snapshot will pass the {@link #isTaskValid} check in
	 * {@link #internalSetGamlDocumentation} and be silently rejected because the resource is no longer in
	 * {@code resourceListeners} at that point — so they never enter {@link #documentedObjects}.
	 * </p>
	 *
	 * @param uri
	 *            the resource URI to invalidate; {@code null} is a no-op
	 */
	@Override
	public void invalidate(final URI uri) {
		if (uri == null) return;
		GamlResourceDocumentationTask task = documentationTasks.remove(uri);
		if (task != null) {
			// Snapshot the objects set at this moment. The background job may still add to task.objects
			// after the remove above, but isTaskValid() will reject those writes (the resource listener
			// is already gone), so they will never enter documentedObjects and need no cleanup.
			final Set<URI> objectsSnapshot = Set.copyOf(task.objects);
			objectsSnapshot.forEach(object -> {
				// computeIfPresent is atomic: it only executes the lambda if the key is present,
				// and returns null to trigger removal once the resources set is empty.
				documentedObjects.computeIfPresent(object, (k, documented) -> {
					documented.resources.remove(uri);
					return documented.resources.isEmpty() ? null : documented;
				});
			});
		}
	}

	/**
	 * Clears all documentation from this documenter.
	 *
	 * <p>
	 * Both the {@link #documentedObjects} and {@link #documentationTasks} maps are cleared directly and
	 * synchronously on the calling thread. The previous implementation used a sentinel URI to enqueue the clear
	 * as an async job, which could leave a leaked task entry in {@link #documentationTasks} and risk re-adding
	 * entries after the clear.
	 * </p>
	 */
	public void invalidateAll() {
		documentedObjects.clear();
		documentationTasks.clear();
	}

	/**
	 * Returns {@code true} if the given resource URI represents a resource that is currently open in an editor.
	 *
	 * <p>
	 * Documentation is only generated for edited resources; this guard prevents unnecessary work for resources that
	 * are only imported/referenced.
	 * </p>
	 *
	 * @param resource
	 *            the resource URI to test; {@code null} always returns {@code false}
	 * @return {@code true} if the resource has a registered editor listener
	 */
	boolean isTaskValid(final URI resource) {
		return resource != null && GamlResourceServices.isEdited(resource);
	}

	/**
	 * Returns {@code true} if the given generation number matches the task's current generation for {@code resource}.
	 *
	 * <p>
	 * Used to discard work that was scheduled for a previous compilation pass.
	 * </p>
	 *
	 * @param resource
	 *            the resource URI
	 * @param generation
	 *            the generation to validate
	 * @return {@code true} if {@code generation} equals the task's current generation
	 */
	boolean isValidGeneration(final URI resource, final int generation) {
		return getCurrentDocGenerationFor(resource) == generation;
	}

	/**
	 * Returns the {@link GamlResourceDocumentationTask} for the given resource, creating a new one if necessary.
	 *
	 * @param resource
	 *            the resource URI; must not be {@code null}
	 * @return the task for this resource (never {@code null})
	 */
	GamlResourceDocumentationTask getTaskFor(final URI resource) {
		return documentationTasks.computeIfAbsent(resource, GamlResourceDocumentationTask::new);
	}

	/**
	 * Returns the current documentation generation counter for the given resource.
	 *
	 * <p>
	 * If no task exists yet for the resource, a new one is created (and its generation starts at {@code 1}).
	 * </p>
	 *
	 * @param resource
	 *            the resource URI
	 * @return the current generation number (always &ge; 1)
	 */
	int getCurrentDocGenerationFor(final URI resource) {
		return getTaskFor(resource).currentGeneration.get();
	}

}
