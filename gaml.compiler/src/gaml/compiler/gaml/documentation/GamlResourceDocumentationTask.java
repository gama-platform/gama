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
package gaml.compiler.gaml.documentation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;

/**
 * The Class GamlResourceDocumentationTask.
 */
public class GamlResourceDocumentationTask {

	/** The Job. */
	ConcurrentLinkedQueue<Runnable> queue;

	/** The job. */
	final Job job;

	/** The name. */
	final String name;

	/** The current generation. */
	int currentGeneration;

	/** The objects. */
	Set<URI> objects = new HashSet<>();

	/**
	 * Instantiates a new gaml resource documentation task.
	 *
	 * @param res
	 *            the res
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
				Runnable task = queue.poll();
				while (task != null) {
					task.run();
					task = queue.poll();
				}
				return Status.OK_STATUS;
			}
		};
	}

	/**
	 * Increment generation.
	 */
	public void incrementGeneration() {
		currentGeneration++;
		queue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Adds the.
	 *
	 * @param run
	 *            the run
	 */
	public void add(final Runnable run) {
		queue.offer(run);
		job.schedule(50);
	}

}