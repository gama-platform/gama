/*******************************************************************************************************
 *
 * SimulationClock.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.kernel.simulation;

import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.model.IModelSpecies;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaDate;
import gama.dev.THREADS;
import gama.gaml.operators.Dates;

/**
 * The class GamaRuntimeInformation.
 *
 * @author drogoul
 * @since 13 d�c. 2011
 *
 */
/**
 * @author administrateur
 *
 */
public class SimulationClock {

	/** The number of simulation cycles elapsed so far. */
	private volatile AtomicInteger cycle = new AtomicInteger(0);

	/**
	 * The current value of time in the model timescale. The base unit is the second (see <link>IUnits</link>). This
	 * value is normally always equal to step * cycle. Note that time can take values smaller than 1 (in case of a step
	 * in milliseconds, for instance), but not smaller than 0.
	 */
	// AD: not kept anymore as the whole computation is based on dates
	// private double time = 0d;

	/**
	 * The length (in model time) of the interval between two cycles. Default is 1 (or 1 second if time matters). Step
	 * can be smaller than 1 (to express an interval smaller than one second).
	 */
	// AD: kept as an expression to allow temporal expressions to be evaluated
	// in the context of the starting_date
	// private IExpression step = new ConstantExpression(1);
	private double step = Dates.DATES_TIME_STEP.getValue();

	/** The duration (in milliseconds) of the last cycle elapsed. */
	protected long duration = 0;

	/**
	 * The total duration in milliseconds since the beginning of the simulation. Since it is the addition of the
	 * consecutive durations of cycles, note that it may be different from the actual duration of the simulation if the
	 * user chooses to pause it, for instance.
	 */
	protected long totalDuration = 0;

	/**
	 * A variable used to compute duration (holds the time, in milliseconds, of the beginning of a cycle).
	 */
	private long start = 0;

	// /**
	// * Whether to display the number of cycles or a more readable information (in model time)
	// */
	// private volatile boolean displayCycles = true;

	/** The starting date. */
	protected GamaDate startingDate = null;

	/** The current date. */
	protected GamaDate currentDate = null;

	/** The output current date as duration. */
	private final boolean outputAsDuration;

	/** The clock scope. */
	private final IScope clockScope;

	/**
	 * Instantiates a new simulation clock.
	 *
	 * @param scope
	 *            the scope
	 */
	public SimulationClock(final IScope scope) {
		final IModelSpecies model = scope.getModel();
		outputAsDuration = model == null ? true : !model.getDescription().isStartingDateDefined();
		this.clockScope = scope;
	}

	/**
	 * @throws GamaRuntimeException
	 *             Sets a new value to the cycle.
	 * @param i
	 *            the new value
	 */

	// FIXME Make setCycle() or incrementCycle() advance the other variables as
	// well, so as to allow writing
	// "cycle <- cycle + 1" in GAML and have the correct information computed.
	public void setCycle(final int i) throws GamaRuntimeException {
		if (i < 0) throw GamaRuntimeException.error("The current cycle of a simulation cannot be negative", clockScope);
		final int previous = cycle.get();
		if (i < previous)
			throw GamaRuntimeException.error("The current cycle of a simulation cannot be set backwards", clockScope);
		cycle.set(i);
		setCurrentDate(getCurrentDate().plus(getStepInMillis(), i - previous, ChronoUnit.MILLIS));
	}

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
	public void setCycleNoCheck(final int i) throws GamaRuntimeException {
		if (i < 0) throw GamaRuntimeException.error("The current cycle of a simulation cannot be negative", clockScope);
		final int previous = cycle.get();
		cycle.set(i);
		setCurrentDate(getCurrentDate().plus(getStepInMillis(), i - previous, ChronoUnit.MILLIS));
	}

	/**
	 * Increment cycle.
	 */
	public void incrementCycle() {
		cycle.incrementAndGet();
		setCurrentDate(getCurrentDate().plusMillis(getStepInMillis()));
	}

	/**
	 * Reset cycles.
	 */
	public void resetCycles() {
		cycle.set(0);
		startingDate = null;
		currentDate = null;
	}

	/**
	 * Returns the current value of cycle
	 */
	public int getCycle() { return cycle.get(); }

	/**
	 * Sets the value of the current time of the simulation. Cannot be negative.
	 *
	 * @throws GamaRuntimeException
	 * @param i
	 *            a positive double
	 */
	// AD cannot be set anymore
	// public void setTime(final double i) throws GamaRuntimeException {
	// if (i < 0) {
	// throw GamaRuntimeException
	// .error("The current time of a simulation cannot be set. Please set
	// starting_date instead", scope);
	// }
	// // time = i;
	// }

	/**
	 * Gets the current value of time in the simulation
	 *
	 * @return a positive double
	 */
	public double getTimeElapsedInSeconds() {
		// BG 16/03/2018 : to fix the issue that time != cycle * step, when step is not an integer number.
		// return getStartingDate().until(getCurrentDate(), ChronoUnit.SECONDS);
		return getStartingDate().until(getCurrentDate(), ChronoUnit.MILLIS) / 1000.0;
	}

	/**
	 * @throws GamaRuntimeException
	 *             Sets the value of the current step duration (in model time) of the simulation. Cannot be negative.
	 *
	 * @throws GamaRuntimeException
	 * @param i
	 *            a positive double
	 */

	public void setStep(final double exp) throws GamaRuntimeException {
		if (exp <= 0) throw GamaRuntimeException
				.error("The interval between two cycles of a simulation cannot be negative or null", clockScope);
		step = exp;

		// step = i <= 0 ? 1 : i;
	}

	/**
	 * Return the current value of step
	 *
	 * @return a positive double
	 */
	public double getStepInSeconds() { return step; }

	/**
	 * Gets the step in millis.
	 *
	 * @return the step in millis
	 */
	public long getStepInMillis() { return (long) (step * 1000); }

	/**
	 * Initializes start at the beginning of a step
	 */
	public void resetDuration() {
		start = System.currentTimeMillis();
		// duration = 0;
	}

	/**
	 * Reset total duration.
	 */
	public void resetTotalDuration() {
		resetDuration();
		duration = 0;
		totalDuration = 0;
	}

	/**
	 * Computes the duration by subtracting start to the current time in milliseconds
	 */
	private void computeDuration() {
		duration = System.currentTimeMillis() - start;
		totalDuration += duration;
	}

	/**
	 * Gets the duration (in milliseconds) of the latest cycle elapsed so far
	 *
	 * @return a duration in milliseconds
	 */
	public long getDuration() { return duration; }

	/**
	 * Gets the average duration (in milliseconds) over
	 *
	 * @return a duration in milliseconds
	 */
	public double getAverageDuration() {
		if (cycle.get() == 0) return 0;
		return totalDuration / (double) cycle.get();
	}

	/**
	 * Gets the total duration in milliseconds since the beginning of the current simulation.
	 *
	 * @return a duration in milliseconds
	 */
	public long getTotalDuration() { return totalDuration; }

	/**
	 * Step.
	 *
	 * @param scope
	 *            the scope
	 */
	public void step() {
		incrementCycle();
		computeDuration();
		waitDelay();
	}

	/**
	 * Wait delay.
	 */
	public void waitDelay() {
		final double delay = getDelayInMilliseconds();
		if (delay <= 0d || duration >= delay) return;
		THREADS.WAIT((long) delay - duration);
	}

	/**
	 * Reset.
	 *
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public void reset() throws GamaRuntimeException {
		resetCycles();
		resetTotalDuration();
	}

	/**
	 * Begin cycle.
	 */
	public void beginCycle() {
		resetDuration();
	}

	/**
	 * Gets the info appended to an existing StringBuilder
	 *
	 * @param sb
	 *            the info string builder
	 * @return the info
	 */
	public StringBuilder getInfo(final StringBuilder sb) {
		final int c = getCycle();
		final ITopLevelAgent agent = clockScope.getRoot();
		if (agent != null) {
			sb.append(agent.getName()).append(": ").append(c).append(c <= 1 ? " cycle " : " cycles ")
					.append("elapsed ");

			try {
				GamaDate d = getCurrentDate();
				final String date = outputAsDuration ? Dates.asDuration(getStartingDate(), d)
						: d.toString("yyyy-MM-dd HH:mm:ss", "en");
				sb.append("[").append(date).append("]");
			} catch (final DateTimeException e) {}
		}
		return sb;
	}

	/**
	 * Gets the delay in milliseconds.
	 *
	 * @return the delay in milliseconds
	 */
	public double getDelayInMilliseconds() {
		IExperimentAgent agent = clockScope.getExperiment();
		return agent == null ? 0 : agent.getMinimumDuration() * 1000;
	}

	/**
	 * Gets the current date.
	 *
	 * @return the current date
	 */
	public GamaDate getCurrentDate() {
		if (currentDate == null) { currentDate = getStartingDate(); }
		return currentDate;
	}

	/**
	 * Gets the starting date.
	 *
	 * @return the starting date
	 */
	public GamaDate getStartingDate() {
		if (startingDate == null) { setStartingDate(Dates.DATES_STARTING_DATE.getValue()); }
		return startingDate;
	}

	/**
	 * Sets the starting date.
	 *
	 * @param starting_date
	 *            the new starting date
	 */
	public void setStartingDate(final GamaDate starting_date) {
		this.startingDate = starting_date;
		this.currentDate = starting_date;
		cycle.set(0);
	}

	/**
	 * Sets the current date.
	 *
	 * @param date
	 *            the new current date
	 */
	public void setCurrentDate(final GamaDate date) { currentDate = date; }

}
