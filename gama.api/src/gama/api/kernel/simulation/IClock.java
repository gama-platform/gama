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

import gama.api.data.factories.GamaDateFactory;
import gama.api.data.objects.IDate;

/**
 * The Interface IClock. Defines the methods required for a clock used in simulations.
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