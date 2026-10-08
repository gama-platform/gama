/*******************************************************************************************************
 *
 * GamlResourceDocumentationTask.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.documentation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;

import gama.dev.DEBUG;

/**
 * Represents a background Eclipse {@link Job} responsible for asynchronously generating documentation for a single
 * GAML resource.
 *
 * <p>
 * Each task has a <em>generation counter</em> that is incremented every time a new documentation pass is triggered
 * (i.e. every time {@link #incrementGeneration()} is called). Tasks submitted for an older generation are silently
 * discarded by {@code GamlResourceDocumenter} when the generation numbers no longer match, ensuring that only the
 * most recent compilation pass contributes to the documentation store.
 * </p>
 *
 * <p>
 * <strong>Thread safety:</strong>
 * </p>
 * <ul>
 * <li>{@link #currentGeneration} is an {@link AtomicInteger} so that increment and read operations are
 * individually atomic and visible across threads without additional synchronisation.</li>
 * <li>{@link #queue} is {@code volatile}: when {@link #incrementGeneration()} replaces the queue with a fresh
 * instance, the new reference is immediately visible to the background job thread.</li>
 * <li>{@link #objects} is backed by a {@link ConcurrentHashMap} key-set and is therefore safe for concurrent
 * insertion from the job thread and removal from the invalidation thread.</li>
 * </ul>
 */
public class GamlResourceDocumentationTask {

	static {
		DEBUG.OFF();
	}

	/**
	 * The pending-task queue for the current generation. Replaced atomically (via the {@code volatile} modifier)
	 * every time a new generation starts so that tasks from a superseded pass are simply abandoned.
	 */
	volatile ConcurrentLinkedQueue<Runnable> queue;

	/**
	 * The Eclipse background job that drains {@link #queue} and executes each {@link Runnable} in order.
	 * Created once at construction time and re-scheduled as needed.
	 */
	final Job job;

	/**
	 * The last segment of the resource URI, used only as a human-readable label in the job name.
	 */
	final String name;

	/**
	 * Monotonically increasing generation counter. Incremented each time {@link #incrementGeneration()} is called.
	 * Uses {@link AtomicInteger} to avoid torn reads/writes when multiple threads access the field concurrently.
	 */
	final AtomicInteger currentGeneration = new AtomicInteger(0);

	/**
	 * The set of EObject fragment URIs that were documented during the <em>current</em> generation. Used by
	 * {@code GamlResourceDocumenter#invalidate} to efficiently remove stale entries from the global documentation
	 * store. Backed by a {@link ConcurrentHashMap} key-set to support concurrent access from the job thread (writes)
	 * and the invalidation path (reads/iteration).
	 */
	Set<URI> objects = ConcurrentHashMap.newKeySet();

	/**
	 * Instantiates a new {@code GamlResourceDocumentationTask} for the given resource URI.
	 *
	 * <p>
	 * The constructor immediately calls {@link #incrementGeneration()} to initialise the queue and set the generation
	 * counter to {@code 1}, then creates the background {@link Job} that will drain that queue.
	 * </p>
	 *
	 * @param res
	 *            the URI of the resource being documented; used only to derive the job name. May be {@code null}, in
	 *            which case the job is named {@code "unknown"}.
	 */
	public GamlResourceDocumentationTask(final URI res) {
		incrementGeneration();
		name = res == null ? "unknown" : res.lastSegment();
		job = new Job("Documentation of " + name) {
			{
				setUser(false);
				setPriority(Job.SHORT);
			}

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				// Snapshot the queue reference once; a concurrent incrementGeneration() may replace it,
				// but we want to drain exactly the queue that was current when this job run started.
				final ConcurrentLinkedQueue<Runnable> current = queue;
				Runnable task = current.poll();
				while (task != null) {
					try {
						task.run();
					} catch (final Throwable t) {
						// Isolate task failures: log and continue draining. This ensures that a
						// RuntimeException (or Error) thrown by one documentation task never terminates
						// the loop, never leaves the job in a failed state, and never blocks future
						// job.schedule() calls.
						DEBUG.ERR("Documentation task failed for " + name + " — skipping", t);
					}
					task = current.poll();
				}
				return Status.OK_STATUS;
			}
		};
	}

	/**
	 * Starts a new documentation generation.
	 *
	 * <p>
	 * Atomically increments the generation counter, resets the tracked-objects set so that fragment URIs from the
	 * previous generation do not survive into the next {@link gaml.compiler.documentation.GamlResourceDocumenter#invalidate}
	 * call, and then replaces the pending-task queue with a fresh empty instance.
	 * </p>
	 *
	 * <p>
	 * The ordering — increment generation <em>before</em> replacing the queue — is intentional: it ensures that any
	 * thread which concurrently reads {@link #currentGeneration} via
	 * {@code GamlResourceDocumenter#getCurrentDocGenerationFor} and then enqueues work will always see the fresh
	 * queue, not the old one.
	 * </p>
	 *
	 * @return the new generation number, which can be captured by the caller to stamp asynchronous tasks.
	 */
	public int incrementGeneration() {
		// 1. Increment generation FIRST so readers always see the new generation with the new queue.
		final int gen = currentGeneration.incrementAndGet();
		// 2. Reset the objects set so stale fragment URIs from the prior generation are not iterated
		//    by a concurrent invalidate() call after this generation's job has been superseded.
		objects = ConcurrentHashMap.newKeySet();
		// 3. Replace the queue last.
		queue = new ConcurrentLinkedQueue<>();
		return gen;
	}

	/**
	 * Enqueues a documentation task and schedules the background job to run it.
	 *
	 * <p>
	 * The job is scheduled with a short delay (50 ms) to allow multiple tasks submitted in rapid succession to be
	 * batched into a single job run.
	 * </p>
	 *
	 * @param run
	 *            the task to enqueue; must not be {@code null}.
	 */
	public void add(final Runnable run) {
		queue.offer(run);
		job.schedule(50);
	}

}