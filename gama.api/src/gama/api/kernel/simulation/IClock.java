/*******************************************************************************************************
 *
 * IClock.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;

/**
 * The Interface IClock.
 * 
 * <p>
 * Defines the time-keeping mechanism for GAMA simulations. The clock manages both discrete time (cycles) and
 * continuous time (elapsed seconds), as well as performance metrics (step duration, average duration).
 * </p>
 * 
 * <h3>Core Concepts</h3>
 * <ul>
 * <li><b>Cycle:</b> Discrete simulation step counter (0, 1, 2, ...)</li>
 * <li><b>Step:</b> Duration of one cycle in model time (seconds)</li>
 * <li><b>Time Elapsed:</b> Cumulative model time since simulation start</li>
 * <li><b>Duration:</b> Real-world time taken to execute a cycle (milliseconds)</li>
 * </ul>
 * 
 * <h3>Time Model</h3>
 * <p>
 * GAMA simulations use a hybrid time model:
 * </p>
 * <ul>
 * <li><b>Discrete:</b> Cycles increment by 1 each step (0, 1, 2, 3, ...)</li>
 * <li><b>Continuous:</b> Time advances by step duration each cycle</li>
 * <li><b>Configurable:</b> Step duration can be changed during simulation</li>
 * </ul>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Accessing Cycle and Time</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     reflex monitor {
 *         write "Cycle: " + cycle;              // Discrete time
 *         write "Time: " + time + " seconds";   // Continuous time
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Configuring Step Duration</h4>
 * 
 * <pre>
 * <code>
 * experiment myExperiment {
 *     float step <- 0.1 #s;  // Each cycle = 0.1 seconds of model time
 * }
 * 
 * // Or variable step:
 * global {
 *     reflex change_step when: cycle = 100 {
 *         step <- 1 #s;  // Change to 1 second per cycle
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Monitoring Performance</h4>
 * 
 * <pre>
 * <code>
 * global {
 *     reflex monitor_performance {
 *         write "Last cycle took: " + duration + " ms";
 *         write "Average: " + average_duration + " ms";
 *         write "Total: " + total_duration + " ms";
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>4. Time-Based Conditions</h4>
 * 
 * <pre>
 * <code>
 * species animal {
 *     reflex eat when: every(10 #cycle) {
 *         // Execute every 10 cycles
 *     }
 *     
 *     reflex sleep when: mod(cycle, 24) = 0 {
 *         // Execute when cycle is multiple of 24
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * IClock clock = simulation.getClock();
 * 
 * // Access time information
 * int currentCycle = clock.getCycle();
 * double timeElapsed = clock.getTimeElapsedInSeconds();
 * double stepDuration = clock.getStepInSeconds();
 * 
 * // Advance simulation
 * clock.incrementCycle();
 * clock.step();
 * 
 * // Performance metrics
 * long lastDuration = clock.getDuration();          // Last cycle in ms
 * double avgDuration = clock.getAverageDuration();  // Average in ms
 * long totalDuration = clock.getTotalDuration();    // Total in ms
 * 
 * // Control
 * clock.setStep(1.0);  // 1 second per cycle
 * clock.reset();       // Reset all counters
 * </code>
 * </pre>
 * 
 * <h3>Clock Lifecycle</h3>
 * <ol>
 * <li><b>Initialization:</b> Clock starts at cycle 0, time 0</li>
 * <li><b>Begin Cycle:</b> beginCycle() marks the start of a simulation step</li>
 * <li><b>Execution:</b> Simulation step executes (agents, reflexes, etc.)</li>
 * <li><b>End Cycle:</b> Duration is recorded, cycle increments, time advances</li>
 * <li><b>Repeat:</b> Process repeats for each cycle</li>
 * </ol>
 * 
 * <h3>Performance Tracking</h3>
 * <ul>
 * <li><b>Duration:</b> Time taken for the most recent cycle</li>
 * <li><b>Average Duration:</b> Moving average of cycle durations</li>
 * <li><b>Total Duration:</b> Cumulative time since simulation start</li>
 * <li><b>Delay:</b> Optional artificial delay to slow down simulation</li>
 * </ul>
 * 
 * <h3>Special Features</h3>
 * <ul>
 * <li><b>NULL_CLOCK:</b> Placeholder clock that does nothing (for platform agent)</li>
 * <li><b>setCycleNoCheck:</b> Used for simulation restoration without validation</li>
 * <li><b>waitDelay:</b> Implements minimum/maximum cycle duration constraints</li>
 * </ul>
 * 
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>All methods have default no-op implementations for NULL_CLOCK</li>
 * <li>Step duration cannot be negative</li>
 * <li>Cycle cannot be set to negative values</li>
 * <li>Duration tracking uses System.nanoTime() for precision</li>
 * </ul>
 * 
 * @see ISimulationAgent
 * @see ITopLevelAgent
 * @author drogoul
 * @since GAMA 1.0
 */

public interface IClock {

	/** The null clock. */
	IClock NULL_CLOCK = new IClock() {};

	/**
	 * Sets a new value to the cycle.
	 *
	 * @param i
	 *            the new value
	 */

	// FIXME Make setCycle() or incrementCycle() advance the other variables as
	// well, so as to allow writing
	// "cycle <- cycle + 1" in GAML and have the correct information computed.
	default void setCycle(final int i) {}

	/**
	 * Sets the cycle without doing no check (except on negative cycles). Used by restoration of simulations
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param i
	 *            the new cycle no check
	 * @date 9 août 2023
	 */
	default void setCycleNoCheck(final int i) {}

	/**
	 * Increment cycle.
	 */
	default void incrementCycle() {}

	/**
	 * Reset cycles.
	 */
	default void resetCycles() {}

	/**
	 * Returns the current value of cycle
	 */
	default int getCycle() { return 0; }

	/**
	 * Gets the current value of time in the simulation
	 *
	 * @return a positive double
	 */
	default double getTimeElapsedInSeconds() { return 0; }

	/**
	 * Sets the value of the current step duration (in model time) of the simulation. Cannot be negative.
	 *
	 * @param i
	 *            a positive double
	 */

	default void setStep(final double exp) {}

	/**
	 * Return the current value of step
	 *
	 * @return a positive double
	 */
	default double getStepInSeconds() { return 0; }

	/**
	 * Gets the step in millis.
	 *
	 * @return the step in millis
	 */
	default long getStepInMillis() { return 0; }

	/**
	 * Initializes start at the beginning of a step
	 */
	default void resetDuration() {}

	/**
	 * Reset total duration.
	 */
	default void resetTotalDuration() {}

	/**
	 * Gets the duration (in milliseconds) of the latest cycle elapsed so far
	 *
	 * @return a duration in milliseconds
	 */
	default long getDuration() { return 0; }

	/**
	 * Gets the average duration (in milliseconds) over
	 *
	 * @return a duration in milliseconds
	 */
	default double getAverageDuration() { return 0; }

	/**
	 * Gets the total duration in milliseconds since the beginning of the current simulation.
	 *
	 * @return a duration in milliseconds
	 */
	default long getTotalDuration() { return 0; }

	/**
	 * Step.
	 *
	 * @param scope
	 *            the scope
	 */
	default void step() {}

	/**
	 * Wait delay.
	 */
	default void waitDelay() {}

	/**
	 * Reset.
	 *
	 */
	default void reset() {}

	/**
	 * Begin cycle.
	 */
	default void beginCycle() {}

	/**
	 * Gets the info appended to an existing StringBuilder
	 *
	 * @param sb
	 *            the info string builder
	 * @return the info
	 */
	default StringBuilder getInfo(final StringBuilder sb) {
		return sb;
	}

	/**
	 * Gets the delay in milliseconds.
	 *
	 * @return the delay in milliseconds
	 */
	default double getDelayInMilliseconds() { return 0; }

	/**
	 * Gets the current date.
	 *
	 * @return the current date
	 */
	default IDate getCurrentDate() { return GamaDateFactory.now(); }

	/**
	 * Gets the starting date.
	 *
	 * @return the starting date
	 */
	default IDate getStartingDate() { return GamaDateFactory.now(); }

	/**
	 * Sets the starting date.
	 *
	 * @param starting_date
	 *            the new starting date
	 */
	default void setStartingDate(final IDate starting_date) {}

	/**
	 * Sets the current date.
	 *
	 * @param date
	 *            the new current date
	 */
	default void setCurrentDate(final IDate date) {}

}