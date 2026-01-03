/*******************************************************************************************************
 *
 * IClock.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IDate;

/**
 *
 */
public interface IClock {

	/**
	 * @throws GamaRuntimeException
	 *             Sets a new value to the cycle.
	 * @param i
	 *            the new value
	 */

	// FIXME Make setCycle() or incrementCycle() advance the other variables as
	// well, so as to allow writing
	// "cycle <- cycle + 1" in GAML and have the correct information computed.
	void setCycle(int i) throws GamaRuntimeException;

	/**
	 * Sets the cycle without doing no check (except on negative cycles). Used by restoration of simulations
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param i
	 *            the new cycle no check
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 9 août 2023
	 */
	void setCycleNoCheck(int i) throws GamaRuntimeException;

	/**
	 * Increment cycle.
	 */
	void incrementCycle();

	/**
	 * Reset cycles.
	 */
	void resetCycles();

	/**
	 * Returns the current value of cycle
	 */
	int getCycle();

	/**
	 * Gets the current value of time in the simulation
	 *
	 * @return a positive double
	 */
	double getTimeElapsedInSeconds();

	/**
	 * @throws GamaRuntimeException
	 *             Sets the value of the current step duration (in model time) of the simulation. Cannot be negative.
	 *
	 * @throws GamaRuntimeException
	 * @param i
	 *            a positive double
	 */

	void setStep(double exp) throws GamaRuntimeException;

	/**
	 * Return the current value of step
	 *
	 * @return a positive double
	 */
	double getStepInSeconds();

	/**
	 * Gets the step in millis.
	 *
	 * @return the step in millis
	 */
	long getStepInMillis();

	/**
	 * Initializes start at the beginning of a step
	 */
	void resetDuration();

	/**
	 * Reset total duration.
	 */
	void resetTotalDuration();

	/**
	 * Gets the duration (in milliseconds) of the latest cycle elapsed so far
	 *
	 * @return a duration in milliseconds
	 */
	long getDuration();

	/**
	 * Gets the average duration (in milliseconds) over
	 *
	 * @return a duration in milliseconds
	 */
	double getAverageDuration();

	/**
	 * Gets the total duration in milliseconds since the beginning of the current simulation.
	 *
	 * @return a duration in milliseconds
	 */
	long getTotalDuration();

	/**
	 * Step.
	 *
	 * @param scope
	 *            the scope
	 */
	void step();

	/**
	 * Wait delay.
	 */
	void waitDelay();

	/**
	 * Reset.
	 *
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void reset() throws GamaRuntimeException;

	/**
	 * Begin cycle.
	 */
	void beginCycle();

	/**
	 * Gets the info appended to an existing StringBuilder
	 *
	 * @param sb
	 *            the info string builder
	 * @return the info
	 */
	StringBuilder getInfo(StringBuilder sb);

	/**
	 * Gets the delay in milliseconds.
	 *
	 * @return the delay in milliseconds
	 */
	double getDelayInMilliseconds();

	/**
	 * Gets the current date.
	 *
	 * @return the current date
	 */
	IDate getCurrentDate();

	/**
	 * Gets the starting date.
	 *
	 * @return the starting date
	 */
	IDate getStartingDate();

	/**
	 * Sets the starting date.
	 *
	 * @param starting_date
	 *            the new starting date
	 */
	void setStartingDate(IDate starting_date);

	/**
	 * Sets the current date.
	 *
	 * @param date
	 *            the new current date
	 */
	void setCurrentDate(IDate date);

}