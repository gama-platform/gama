/*******************************************************************************************************
 *
 * IRuntimeExceptionHandler.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.List;

import gama.api.exceptions.GamaRuntimeException;

/**
 * Manages runtime exceptions that occur during GAMA simulation execution.
 * 
 * <p>
 * IRuntimeExceptionHandler provides a centralized mechanism for collecting, storing, and managing exceptions that
 * occur during simulation runtime. It acts as a queue that can be started and stopped, allowing the platform to
 * control when exceptions should be collected and processed.
 * </p>
 * 
 * <p>
 * Key responsibilities:
 * </p>
 * <ul>
 * <li>Collect runtime exceptions from multiple sources (threads, agents, statements)</li>
 * <li>Maintain a queue of exceptions for batch processing</li>
 * <li>Provide lifecycle control (start/stop) for exception collection</li>
 * <li>Allow clearing of accumulated errors</li>
 * <li>Support removal of specific exceptions from the queue</li>
 * </ul>
 * 
 * <p>
 * Typical lifecycle:
 * </p>
 * 
 * <pre>
 * IRuntimeExceptionHandler handler = ...;
 * 
 * handler.start(); // Begin collecting exceptions
 * 
 * // During simulation
 * try {
 * 	// Simulation code
 * } catch (GamaRuntimeException ex) {
 * 	handler.offer(ex); // Add exception to queue
 * }
 * 
 * // Retrieve and display exceptions
 * List&lt;GamaRuntimeException&gt; errors = handler.getCleanExceptions();
 * 
 * handler.clearErrors(); // Clear the queue
 * handler.stop(); // Stop collecting
 * </pre>
 * 
 * @see GamaRuntimeException
 */
public interface IRuntimeExceptionHandler {

	/**
	 * Starts the exception handler, enabling it to collect exceptions.
	 * 
	 * <p>
	 * After calling this method, the handler will accept and queue exceptions via {@link #offer(GamaRuntimeException)}.
	 * This is typically called when a simulation begins execution.
	 * </p>
	 */
	void start();

	/**
	 * Stops the exception handler, disabling exception collection.
	 * 
	 * <p>
	 * After calling this method, the handler stops accepting new exceptions. This is typically called when a simulation
	 * completes or is terminated.
	 * </p>
	 */
	void stop();

	/**
	 * Clears all accumulated exceptions from the handler's queue.
	 * 
	 * <p>
	 * This method empties the internal exception queue, effectively resetting the handler's state. It's typically
	 * called after exceptions have been displayed to the user or when starting a new simulation.
	 * </p>
	 */
	void clearErrors();

	/**
	 * Adds a runtime exception to the handler's queue.
	 * 
	 * <p>
	 * This method is called when a {@link GamaRuntimeException} occurs during simulation execution. The exception is
	 * queued for later retrieval and display. If the handler is not running ({@link #isRunning()} returns false), the
	 * exception may be ignored.
	 * </p>
	 * 
	 * @param ex
	 *            the exception to add to the queue
	 */
	void offer(final GamaRuntimeException ex);

	/**
	 * Removes a specific exception from the handler's queue.
	 * 
	 * <p>
	 * This method allows selective removal of exceptions, which can be useful when an exception has been handled or
	 * should no longer be displayed.
	 * </p>
	 * 
	 * @param obj
	 *            the exception to remove from the queue
	 */
	void remove(GamaRuntimeException obj);

	/**
	 * Retrieves and removes all exceptions from the queue.
	 * 
	 * <p>
	 * This method returns a list of all accumulated exceptions and clears them from the internal queue. It's typically
	 * used by the UI to retrieve exceptions for display to the user.
	 * </p>
	 * 
	 * @return a list of all queued exceptions (may be empty but never null)
	 */
	List<GamaRuntimeException> getCleanExceptions();

	/**
	 * Checks whether the exception handler is currently running and collecting exceptions.
	 * 
	 * @return true if the handler is active and will accept exceptions via {@link #offer(GamaRuntimeException)}, false
	 *         otherwise
	 */
	boolean isRunning();

}
