/*******************************************************************************************************
 *
 * Dates.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import org.eclipse.emf.ecore.EObject;
import org.geotools.filter.ConstantExpression;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.IOperatorValidator;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.simulation.IClock;
import gama.api.runtime.scope.IScope;
import gama.api.types.date.DurationFormatter;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.GamaDateInterval;
import gama.api.types.date.IDate;
import gama.api.types.list.IList;

/**
 * Provides all date/time/duration operators for the GAML language. Dates in GAMA are
 * <em>timezone-free</em> values represented as ISO-8601 local date-times
 * ({@link java.time.LocalDateTime} internally).
 *
 * <h3>Operator families</h3>
 * <ul>
 *   <li><b>Arithmetic:</b>
 *     <ul>
 *       <li>{@code +} &mdash; {@code date + float/int} (seconds) returns a new {@link IDate}</li>
 *       <li>{@code -} &mdash; {@code date - date} returns elapsed seconds as a {@code float};
 *           {@code date - float/int} (seconds) returns a new {@link IDate}</li>
 *     </ul>
 *   </li>
 *   <li><b>Comparison:</b> {@code <}, {@code >}, {@code <=}, {@code >=}, {@code =}, {@code !=},
 *       {@code between}</li>
 *   <li><b>Period queries</b> (signed &mdash; negative when date1 &gt; date2):
 *       {@code years_between}, {@code months_between}, {@code weeks_between},
 *       {@code days_between}, {@code hours_between}, {@code minutes_between},
 *       {@code milliseconds_between}</li>
 *   <li><b>Duration field access:</b> {@code milliseconds_of}, {@code seconds_of},
 *       {@code minutes_of}, {@code hours_of}, {@code days_of}, {@code months_of},
 *       {@code years_of}</li>
 *   <li><b>Date component access:</b> {@code day_of_week}, {@code day_of_year}</li>
 *   <li><b>Construction:</b> {@code date} (from {@code string} or from {@link IList}),
 *       {@code current_date}, {@code starting_date}</li>
 *   <li><b>Temporal predicates / scheduling:</b> {@code every}, {@code since},
 *       {@code after}, {@code before}, {@code until}, {@code between}, {@code to}</li>
 * </ul>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>GAMA dates are <em>timezone-free</em> (ISO-8601 local time); no DST or UTC offset is
 *       applied.</li>
 *   <li>Duration constants: {@code #second = 1.0}, {@code #minute = 60.0},
 *       {@code #hour = 3600.0}, {@code #day = 86400.0}, {@code #week = 604800.0}.
 *       {@code #month} and {@code #year} are pseudo-constants whose exact second-value depends
 *       on the current simulation date.</li>
 *   <li>Date subtraction ({@code date - date}) returns a {@code float} representing the
 *       duration in <em>seconds</em>. A negative result means the left operand is earlier
 *       than the right one.</li>
 *   <li>Leap-year handling (e.g. Feb 29 arithmetic) is fully delegated to
 *       {@link java.time}.</li>
 * </ul>
 *
 * <h3>Usage examples</h3>
 * <pre>{@code
 * // Arithmetic
 * date d1 <- date('2000-01-01') + 86400;         // => date('2000-01-02')
 * float secs <- date('2000-01-02') - date('2000-01-01'); // => 86400.0
 *
 * // Period queries
 * int yrs  <- years_between(date('2000-01-01'), date('2010-01-01'));  // => 10
 * float ms <- milliseconds_between(date('2000-01-01'), date('2000-01-02')); // => 86400000.0
 *
 * // Scheduling
 * reflex every_day when: every(#day) { write "one day passed"; }
 * }</pre>
 *
 * @author GAMA development team
 * @see IDate
 * @see IClock
 * @see GamaDateFactory
 */
public class Dates {

	/**
	 * Initialize.Only here to load the class and its preferences
	 */
	public static void initialize() {}

	/**
	 * Minus date.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Minus date.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.MINUS },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns the duration in seconds between date1 and date2. A positive value means date1 is after date2.",
			returns = "a {@code float} representing the number of seconds elapsed from date2 to date1. Negative if date1 < date2.",
			special_cases = { "If both dates are equal, returns 0.0.",
					"If date1 < date2, returns a negative value.",
					"Leap year boundaries are handled correctly by java.time." },
			see = "milliseconds_between",
			usages = @usage (
					value = "if both operands are dates, returns the duration in seconds between date2 and date1. To obtain a more precise duration, in milliseconds, use milliseconds_between(date1, date2)",
					examples = { @example (
							value = "date('2000-01-02') - date('2000-01-01')",
							equals = "86400") }))
	@test ("date('2000-01-02') - date('2000-01-01') = 86400.0")
	@test ("date('2000-01-01') - date('2000-01-01') = 0.0")
	@test ("date('2000-01-01') - date('2000-01-02') = -86400.0")

	public static double minusDate(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		final Duration duration = Duration.between(date2, date1);
		return duration.getSeconds();
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the boolean
	 */
	@operator (
			value = { "every", "every_cycle" },
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.CYCLE })
	@doc (
			value = "true every operand * cycle, false otherwise",
			comment = "the value of the every operator depends on the cycle. It can be used to do something every x cycle.",
			special_cases = { "Returns true only on simulation cycles that are multiples of the given step.",
					"The first call at step 0 returns true." },
			examples = { @example (
					value = "if every(2#cycle) {write \"the cycle number is even\";}",
					test = false),
					@example (
							value = "	     else {write \"the cycle number is odd\";}",
							test = false) })
	@no_test
	@validator (EveryValidator.class)
	public static Boolean every(final IScope scope, final Integer period) {
		IClock clock = scope.getClock();
		if (clock == null) return false;
		final int time = clock.getCycle();
		return period > 0 && (time == 0 || time >= period) && time % period == 0;
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the boolean
	 */
	@operator (
			value = { "every", "every_cycle" },
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.CYCLE })
	@doc (
			value = "returns the first float operand every 2nd operand * cycle, 0.0 otherwise",
			comment = "the value of the every operator depends on the cycle. It can be used to return a value every x cycle. `1000.0 every(10#cycle)` is strictly equivalent to `every(10#cycle) ? 1000.0 : 0.0`",
			examples = { @example (
					value = "if (1000.0 every(2#cycle) != 0) {write \"this is a value\";}",
					test = false),
					@example (
							value = "	     else {write \"this is 0.0\";}",
							test = false) })
	@no_test
	@validator (EveryValidator.class)
	public static Double every(final IScope scope, final Double object, final Integer period) {
		final int time = scope.getClock().getCycle();
		return period > 0 && (time == 0 || time >= period) && time % period == 0 ? object : 0d;
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the boolean
	 */
	@operator (
			value = { "every", "every_cycle" },

			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.CYCLE })
	@doc (
			value = "returns the first integer operand every 2nd operand * cycle, 0 otherwise",
			comment = "the value of the every operator depends on the cycle. It can be used to return a value every x cycle. `1000 every(10#cycle)` is strictly equivalent to `every(10#cycle) ? 1000 : 0`",
			examples = { @example (
					value = "if (1000 every(2#cycle) != 0) {write \"this is a value\";}",
					test = false),
					@example (
							value = "	     else {write \"this is 0\";}",
							test = false) })
	@no_test
	@validator (EveryValidator.class)
	public static Integer every(final IScope scope, final Integer object, final Integer period) {
		final int time = scope.getClock().getCycle();
		return period > 0 && (time == 0 || time >= period) && time % period == 0 ? object : 0;
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the boolean
	 */
	@operator (
			value = { "every", "every_cycle" },
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.CYCLE })
	@doc (
			value = "returns the first operand every 2nd operand * cycle, nil otherwise",
			comment = "the value of the every operator depends on the cycle. It can be used to return a value every x cycle. `object every(10#cycle)` is strictly equivalent to `every(10#cycle) ? object : nil`",
			examples = { @example (
					value = "if ({2000,2000} every(2#cycle) != nil) {write \"this is a point\";}",
					test = false),
					@example (
							value = "	     else {write \"this is nil\";}",
							test = false) })
	@no_test
	@validator (EveryValidator.class)
	public static Object every(final IScope scope, final Object object, final Integer period) {
		final int time = scope.getClock().getCycle();
		return period > 0 && (time == 0 || time >= period) && time % period == 0 ? object : null;
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @param period
	 *            the period
	 * @return the boolean
	 */
	@operator (
			value = { "every", "every_cycle" },
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.CYCLE })
	@doc (
			value = "returns the first bool operand every 2nd operand * cycle, false otherwise",
			comment = "the value of the every operator depends on the cycle. It can be used to return a value every x cycle. `object every(10#cycle)` is strictly equivalent to `every(10#cycle) ? object : false`",
			examples = { @example (
					value = "if (true every(2#cycle) != false) {write \"this is true\";}",
					test = false),
					@example (
							value = "	     else {write \"this is false\";}",
							test = false) })
	@no_test
	@validator (EveryValidator.class)
	public static Boolean every(final IScope scope, final Boolean object, final Integer period) {
		final int time = scope.getClock().getCycle();
		return period > 0 && (time == 0 || time >= period) && time % period == 0 ? object : false;
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the boolean
	 */

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the boolean
	 */
	@operator (
			value = "every",
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE, IConcept.CYCLE })
	@doc (
			see = { "since", "after" },
			value = "expects a frequency (expressed in seconds of simulated time) as argument. Will return true every time the current_date matches with this frequency",
			comment = "Used to do something at regular intervals of time. Can be used in conjunction with 'since', 'after', 'before', 'until' or 'between', so that this computation only takes place in the temporal segment defined by these operators. In all cases, the starting_date of the model is used as a reference starting point",
			examples = { @example (
					value = "reflex when: every(2#days) since date('2000-01-01') { .. }",
					isExecutable = false),
					@example (
							value = "state a { transition to: b when: every(2#mn);} state b { transition to: a when: every(30#s);} // This oscillatory behavior will use the starting_date of the model as its starting point in time",
							isExecutable = false) })
	@validator (EveryValidator.class)
	@no_test
	public static Boolean every(final IScope scope, final IExpression period) {
		return scope.getClock().getStartingDate().isIntervalReachedOptimized(scope, period);
	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @param optimized
	 *            the optimized
	 * @return the boolean
	 */
	@operator (
			value = "every",
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE, IConcept.CYCLE })
	@doc (
			see = { "since", "after" },
			value = "expects a frequency (expressed in seconds of simulated time) as argument and a bool to use (default) the optimzied version (true) and the old one (false). Will return true every time the current_date matches with this frequency",
			comment = "Used to do something at regular intervals of time. Can be used in conjunction with 'since', 'after', 'before', 'until' or 'between', so that this computation only takes place in the temporal segment defined by these operators. In all cases, the starting_date of the model is used as a reference starting point",
			examples = { @example (
					value = "reflex when: every(2#days, false) since date('2000-01-01') { .. }",
					isExecutable = false) })
	@validator (EveryValidator.class)
	@no_test
	public static Boolean every(final IScope scope, final IExpression period, final boolean optimized) {
		if (optimized) return scope.getClock().getStartingDate().isIntervalReachedOptimized(scope, period);
		return scope.getClock().getStartingDate().isIntervalReached(scope, period);
	}

	/**
	 * The Class EveryValidator.
	 */
	public static class EveryValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			if (arguments == null || arguments.length == 0) return false;
			IExpression expr = arguments[arguments.length - 1];
			if (expr instanceof ConstantExpression && expr.getGamlType() == Types.INT) {
				context.warning(
						"No unit provided. If this frequency concerns cycles, please use the #cycle unit. Otherwise use one of the temporal unit (#ms, #s, #mn, #h, #day, #week, #month, #year)",
						IGamlIssue.DEPRECATED, emfContext);
			}
			return true;
		}

	}

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param interval
	 *            the interval
	 * @param period
	 *            the period
	 * @return the i list
	 */

	/**
	 * Every.
	 *
	 * @param scope
	 *            the scope
	 * @param interval
	 *            the interval
	 * @param period
	 *            the period
	 * @return the i list
	 */
	@operator (
			value = "every",
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE, IConcept.CYCLE })
	@doc (
			see = { "to" },
			value = """
					applies a step to an interval of dates defined by 'date1 to date2'. Beware that using every with #month or #year will produce odd results,\
					as these pseudo-constants are not constant; only the first value will be used to compute the intervals, so, for instance, if current_date is set to February\
					#month will only represent 28 or 29 days.\s""",
			comment = "",
			examples = { @example (
					value = "(date('2000-01-01') to date('2010-01-01')) every (#day) // builds an interval between these two dates which contains all the days starting from the beginning of the interval",
					isExecutable = false) })
	@test ("list((date('2001-01-01') to date('2001-1-02')) every(#day)) collect each = [date ('2001-01-01 00:00:00')]")
	public static IList<IDate> every(final IScope scope, final GamaDateInterval interval, final IExpression period) {
		return interval.step(Cast.asFloat(scope, period.value(scope)));
	}

	/**
	 * To.
	 *
	 * @param scope
	 *            the scope
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @return the i list
	 */
	@operator (
			value = "to",
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE, IConcept.CYCLE })
	@doc (
			see = { "every" },
			value = "builds an interval between two dates (the first inclusive and the second exclusive, which behaves like a read-only list of dates. The default step between two dates is the step of the model",
			comment = "The default step can be overruled by using the every operator applied to this interval",
			examples = { @example (
					value = "date('2000-01-01') to date('2010-01-01') // builds an interval between these two dates",
					isExecutable = false),
					@example (
							value = """
									(date('2000-01-01') to date('2010-01-01')) every (#day) // builds an interval between these two dates which contains all \
									the days starting from the beginning of the interval. Beware that using every with #month or #year will produce odd results, \
									as these pseudo-constants are not constant; only the first value will be used to compute the intervals (if current_date is set to a month of February, \
									#month will only represent 28 or 29 days depending on whether it is a leap year or not !). If such intervals need to be built, it is recommended to use\
									a generative way, for instance a loop using the 'plus_years' or 'plus_months' operators to build a list of dates""",
							isExecutable = false) })
	@test ("to_list((date('2001-01-01') to date('2001-01-06')) every(#day)) =\n"
			+ "		[date ('2001-01-01 00:00:00'),date ('2001-01-02 00:00:00'),date ('2001-01-03 00:00:00'),date ('2001-01-04 00:00:00'),date ('2001-01-05 00:00:00')]")
	public static IList<IDate> to(final IScope scope, final IDate start, final IDate end) {
		return GamaDateInterval.of(start, end);
	}

	/**
	 * Since.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * Since.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "since", "from" },
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the current_date of the model is after (or equal to) the date passed in argument. Synonym of 'current_date >= argument'. Can be used, like 'after', in its composed form with 2 arguments to express the lowest boundary of the computation of a frequency. However, contrary to 'after', there is a subtle difference: the lowest boundary will be tested against the frequency as well ",
			examples = { @example (
					value = "reflex when: since(starting_date) {}  	// this reflex will always be run",
					isExecutable = false),
					@example (
							value = "every(2#days) since (starting_date + 1#day) // the computation will return true 1 day after the starting date and every two days after this reference date",
							isExecutable = false) })
	@test ("starting_date <- date([2019,5,9]);since(date([2019,5,10])) = false")
	@test ("starting_date <- date([2019,5,9]);since(date([2019,5,9])) = true")
	@test ("starting_date <- date([2019,5,9]);since(date([2019,5,8])) = true")
	public static boolean since(final IScope scope, final IDate date) {
		return scope.getSimulation().getCurrentDate().isGreaterThan(date, false);
	}

	/**
	 * After.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * After.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "after" },
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the current_date of the model is strictly after the date passed in argument. Synonym of 'current_date > argument'. Can be used in its composed form with 2 arguments to express the lower boundary for the computation of a frequency. Note that only dates strictly after this one will be tested against the frequency",
			examples = { @example (
					value = "reflex when: after(starting_date) {} 	// this reflex will always be run after the first step",
					isExecutable = false),
					@example (
							value = "reflex when: false after(starting date + #10days) {} 	// This reflex will not be run after this date. Better to use 'until' or 'before' in that case",
							isExecutable = false),
					@example (
							value = "every(2#days) after (starting_date + 1#day) 	// the computation will return true every two days (using the starting_date of the model as the starting point) only for the dates strictly after this starting_date + 1#day",
							isExecutable = false) })
	@test ("starting_date <- date([2019,5,9]);after(date([2019,5,10])) = false")
	@test ("starting_date <- date([2019,5,9]);after(date([2019,5,9])) = false")
	@test ("starting_date <- date([2019,5,9]);after(date([2019,5,8])) = true")
	public static boolean after(final IScope scope, final IDate date) {
		return scope.getSimulation().getCurrentDate().isGreaterThan(date, true);
	}

	/**
	 * Before.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * Before.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "before" },
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the current_date of the model is strictly before the date passed in argument. Synonym of 'current_date < argument'",
			examples = { @example (
					value = "reflex when: before(starting_date) {} 	// this reflex will never be run",
					isExecutable = false) })
	@test ("starting_date <- date([2019,5,9]);before(date([2019,5,10])) = true")
	@test ("starting_date <- date([2019,5,9]);before(date([2019,5,9])) = false")
	@test ("starting_date <- date([2019,5,9]);before(date([2019,5,8])) = false")
	public static boolean before(final IScope scope, final IDate date) {
		return scope.getSimulation().getCurrentDate().isSmallerThan(date, true);
	}

	/**
	 * Until.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * Until.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "until", "to" },
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the current_date of the model is before (or equel to) the date passed in argument. Synonym of 'current_date <= argument'",
			examples = { @example (
					value = "reflex when: until(starting_date) {} 	// This reflex will be run only once at the beginning of the simulation",
					isExecutable = false) })
	@test ("starting_date <- date([2019,5,9]);until(date([2019,5,10])) = true")
	@test ("starting_date <- date([2019,5,9]);until(date([2019,5,9])) = true")
	@test ("starting_date <- date([2019,5,9]);until(date([2019,5,8])) = false")
	public static boolean until(final IScope scope, final IDate date) {
		return scope.getSimulation().getCurrentDate().isSmallerThan(date, false);
	}

	/**
	 * Since.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * Since.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "since", "from" },
			category = { IOperatorCategory.DATE },
			doc = @doc ("Returns true if the first operand is true and the current date is equal to or after the second operand"),
			concept = { IConcept.DATE })
	@no_test
	public static boolean since(final IScope scope, final IExpression expression, final IDate date) {
		return since(scope, date) && Cast.asBool(scope, expression.value(scope));
	}

	/**
	 * After.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * After.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "after" },
			doc = @doc ("Returns true if the first operand is true and the current date is situated strictly after the second operand"),
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@no_test
	public static boolean after(final IScope scope, final IExpression expression, final IDate date) {
		return after(scope, date) && Cast.asBool(scope, expression.value(scope));
	}

	/**
	 * Before.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * Before.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "before" },
			category = { IOperatorCategory.DATE },
			doc = @doc ("Returns true if the first operand is true and the current date is situated strictly before the second operand"),
			concept = { IConcept.DATE })
	@no_test
	public static boolean before(final IScope scope, final IExpression expression, final IDate date) {
		return before(scope, date) && Cast.asBool(scope, expression.value(scope));
	}

	/**
	 * Until.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */

	/**
	 * Until.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param date
	 *            the date
	 * @return true, if successful
	 */
	@operator (
			value = { "until", "to" },
			doc = @doc ("Returns true if the first operand is true and the current date is equal to or situated before the second operand"),
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@no_test
	public static boolean until(final IScope scope, final IExpression expression, final IDate date) {
		return until(scope, date) && Cast.asBool(scope, expression.value(scope));
	}

	/**
	 * Between.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param start
	 *            the start
	 * @param stop
	 *            the stop
	 * @return true, if successful
	 */

	/**
	 * Between.
	 *
	 * @param scope
	 *            the scope
	 * @param expression
	 *            the expression
	 * @param start
	 *            the start
	 * @param stop
	 *            the stop
	 * @return true, if successful
	 */
	@operator (
			value = { "between" },
			category = { IOperatorCategory.DATE },
			doc = @doc ("Returns true if the first operand is true and the current date is situated strictly after the second operand and before the third one"),
			concept = { IConcept.DATE })
	@no_test
	public static boolean between(final IScope scope, final IExpression expression, final IDate start,
			final IDate stop) {
		return between(scope, scope.getClock().getCurrentDate(), start, stop) && (boolean) expression.value(scope);
	}

	/**
	 * Between.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 */

	/**
	 * Between.
	 *
	 * @param scope
	 *            the scope
	 * @param date
	 *            the date
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 */
	@operator (
			value = { "between" },
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			usages = @usage (
					value = "returns true if the first operand is between the two dates passed in arguments (both exclusive). Can be combined with 'every' to express a frequency between two dates",
					examples = { @example (
							value = "(date('2016-01-01') between(date('2000-01-01'), date('2020-02-02')))",
							equals = "true"),
							@example (
									value = "// will return true every new day between these two dates, taking the first one as the starting point",
									isExecutable = false),
							@example (
									value = "every(#day between(date('2000-01-01'), date('2020-02-02'))) ",
									isExecutable = false) }))

	public static boolean between(final IScope scope, final IDate date, final IDate date1, final IDate date2) {
		return date.isGreaterThan(date1, true) && date.isSmallerThan(date2, true);
	}

	/**
	 * Between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 */

	/**
	 * Between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 */
	@operator (
			value = { "between" },
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			usages = @usage (
					value = "With only 2 date operands, it returns true if the current_date is between the 2 date  operands.",
					examples = { @example (
							value = "between(date('2000-01-01'), date('2020-02-02'))",
							equals = "false") }))
	@test ("starting_date <- date([2019,5,9]);between((date([2019,5,8])), (date([2019,5,10]))) = true")
	public static boolean between(final IScope scope, final IDate date1, final IDate date2) {
		return scope.getSimulation().getCurrentDate().isGreaterThan(date1, true)
				&& scope.getSimulation().getCurrentDate().isSmallerThan(date2, true);
	}

	/**
	 * Plus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Plus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.PLUS, "plus_seconds", "add_seconds" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			special_cases = { "Adding 0 seconds returns a date equal to the original.",
					"Day boundaries (midnight) are handled correctly." },
			usages = @usage (
					value = "if one of the operands is a date and the other a number, returns a date corresponding to the date plus the given number as duration (in seconds)",
					examples = { @example (
							value = "date('2000-01-01') + 86400",
							equals = "date('2000-01-02')") }))
	@test ("date('2000-01-01') + 86400 = date('2000-01-02')")
	public static IDate plusDuration(final IScope scope, final IDate date1, final int duration)
			throws GamaRuntimeException {
		return date1.plus(duration, SECONDS);
	}

	/**
	 * Plus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Plus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.PLUS },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.TIME, IConcept.DATE })
	@doc (
			value = "Add a duration to a date. The duration is supposed to be in seconds (so that adding 0.5, "
					+ "for instance, will add 500ms)",
			special_cases = { "Adding 0 seconds returns a date equal to the original.",
					"Day boundaries (midnight) are handled correctly." },
			examples = { @example (
					value = "date('2016-01-01 00:00:01') + 86400",
					equals = "date('2016-01-02 00:00:01')"), })
	@test ("date('2016-01-01 00:00:01') + 86400 = date('2016-01-02 00:00:01')")
	public static IDate plusDuration(final IScope scope, final IDate date1, final double duration)
			throws GamaRuntimeException {
		return date1.plus(duration * 1000, ChronoUnit.MILLIS);
	}

	/**
	 * Minus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Minus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.MINUS, "minus_seconds", "subtract_seconds" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = {})
	@doc (
			special_cases = { "Subtracting 0 returns the original date.",
					"Year boundaries are handled correctly." },
			usages = @usage (
					value = "if one of the operands is a date and the other a number, returns a date corresponding to the "
							+ "date minus the given number as duration (in seconds)",
					examples = { @example (
							value = "date('2000-01-01') - 86400",
							equals = "date('1999-12-31')") }))
	@test ("date('2000-01-01') - 86400 = date('1999-12-31')")
	@test ("date('2000-01-02') - 86400 = date('2000-01-01')")
	public static IDate minusDuration(final IScope scope, final IDate date1, final int duration)
			throws GamaRuntimeException {
		return date1.plus(-duration, SECONDS);
	}

	/**
	 * Minus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Minus duration.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param duration
	 *            the duration
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.MINUS },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.TIME, IConcept.DATE })
	@doc (
			value = "Removes a duration from a date. The duration is expected to be in seconds (so that removing 0.5, "
					+ "for instance, will add 500ms) ",
			special_cases = { "Subtracting 0 returns the original date.",
					"Year boundaries are handled correctly." },
			examples = { @example (
					value = "date('2000-01-01') - 86400",
					equals = "date('1999-12-31')") })
	@test ("date('2000-01-01') - 86400 = date('1999-12-31')")
	@test ("date('2000-01-02') - 86400 = date('2000-01-01')")
	public static IDate minusDuration(final IScope scope, final IDate date1, final double duration)
			throws GamaRuntimeException {
		return date1.plus(-duration * 1000, ChronoUnit.MILLIS);
	}

	/**
	 * Concatenate date.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param text
	 *            the text
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.PLUS },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = {})
	@doc (
			value = "returns the resulting string from the addition of a date and a string",
			examples = { @example (
					value = "date('2000-01-01 00:00:00') + '_Test'",
					equals = "'2000-01-01 00:00:00_Test'") })
	@test ("date('2000-01-01 00:00:00') + '_Test' = '2000-01-01 00:00:00_Test'")
	public static String concatenateDate(final IScope scope, final IDate date1, final String text)
			throws GamaRuntimeException {
		return date1.toString() + text;
	}

	/**
	 * Adds the years.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbYears
	 *            the nb years
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the years.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbYears
	 *            the nb years
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_years", "add_years" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of years to a date",
			examples = { @example (
					value = "date('2000-01-01') plus_years 15",
					equals = "date('2015-01-01')") })
	@test ("date('2000-01-01') plus_years 15 = date('2015-01-01')")
	public static IDate addYears(final IScope scope, final IDate date1, final int nbYears) throws GamaRuntimeException {

		return date1.plus(nbYears, YEARS);

	}

	/**
	 * Adds the months.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMonths
	 *            the nb months
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the months.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMonths
	 *            the nb months
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_months", "add_months" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of months to a date",
			examples = { @example (
					value = "date('2000-01-01') plus_months 5",
					equals = "date('2000-06-01')") })
	@test ("date('2000-01-01') plus_months 5 = date('2000-06-01')")
	public static IDate addMonths(final IScope scope, final IDate date1, final int nbMonths)
			throws GamaRuntimeException {

		return date1.plus(nbMonths, MONTHS);

	}

	/**
	 * Adds the weeks.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbWeeks
	 *            the nb weeks
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the weeks.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbWeeks
	 *            the nb weeks
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_weeks", "add_weeks" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of weeks to a date",
			examples = { @example (
					value = "date('2000-01-01') plus_weeks 15",
					equals = "date('2000-04-15')") })
	@test ("is_error(date('2000-15-01'))")
	public static IDate addWeeks(final IScope scope, final IDate date1, final int nbWeeks) throws GamaRuntimeException {
		return date1.plus(nbWeeks, WEEKS);

	}

	/**
	 * Adds the days.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbDays
	 *            the nb days
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the days.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbDays
	 *            the nb days
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_days", "add_days" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of days to a date",
			examples = { @example (
					value = "date('2000-01-01') plus_days 12",
					equals = "date('2000-01-13')") })
	@test ("date('2000-01-01') plus_days 12 = date('2000-01-13')")
	public static IDate addDays(final IScope scope, final IDate date1, final int nbDays) throws GamaRuntimeException {
		return date1.plus(nbDays, DAYS);

	}

	/**
	 * Adds the hours.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbHours
	 *            the nb hours
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the hours.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbHours
	 *            the nb hours
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_hours", "add_hours" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of hours to a date",
			examples = { @example (
					value = "// equivalent to date1 + 15 #h",
					test = false),
					@example (
							value = "date('2000-01-01') plus_hours 24",
							equals = "date('2000-01-02')") })
	@test ("date('2000-01-01') plus_hours 24  = date('2000-01-02')")
	public static IDate addHours(final IScope scope, final IDate date1, final int nbHours) throws GamaRuntimeException {
		return date1.plus(nbHours, HOURS);

	}

	/**
	 * Adds the minutes.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMinutes
	 *            the nb minutes
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the minutes.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMinutes
	 *            the nb minutes
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_minutes", "add_minutes" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of minutes to a date",
			examples = { @example (
					value = "// equivalent to date1 + 5 #mn",
					test = false),
					@example (
							value = "date('2000-01-01') plus_minutes 5 ",
							equals = "date('2000-01-01 00:05:00')") })
	@test ("date('2000-01-01') plus_minutes 5  = date('2000-01-01 00:05:00')")
	public static IDate addMinutes(final IScope scope, final IDate date1, final int nbMinutes)
			throws GamaRuntimeException {
		return date1.plus(nbMinutes, MINUTES);

	}

	/**
	 * Subtract years.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbYears
	 *            the nb years
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract years.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbYears
	 *            the nb years
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_years", "subtract_years" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Subtract a given number of year from a date",
			examples = { @example (
					value = "date('2000-01-01') minus_years 3",
					equals = "date('1997-01-01')") })
	@test ("date('2000-01-01') minus_years 3 = date('1997-01-01')")
	public static IDate subtractYears(final IScope scope, final IDate date1, final int nbYears)
			throws GamaRuntimeException {
		return date1.plus(-nbYears, YEARS);

	}

	/**
	 * Subtract months.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMonths
	 *            the nb months
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract months.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMonths
	 *            the nb months
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_months", "subtract_months" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Subtract a given number of months from a date",
			examples = { @example (
					value = "date('2000-01-01') minus_months 5",
					equals = "date('1999-08-01')") })
	@test ("date('2000-01-01') minus_months 5 = date('1999-08-01')")
	public static IDate subtractMonths(final IScope scope, final IDate date1, final int nbMonths)
			throws GamaRuntimeException {
		return date1.plus(-nbMonths, MONTHS);

	}

	/**
	 * Subtract weeks.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbWeeks
	 *            the nb weeks
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract weeks.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbWeeks
	 *            the nb weeks
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_weeks", "subtract_weeks" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Subtract a given number of weeks from a date",
			examples = { @example (
					value = "date('2000-01-01') minus_weeks 15",
					equals = "date('1999-09-18')") })
	@test ("date('2000-01-01') minus_weeks 15 = date('1999-09-18')")
	public static IDate subtractWeeks(final IScope scope, final IDate date1, final int nbWeeks)
			throws GamaRuntimeException {
		return date1.plus(-nbWeeks, WEEKS);

	}

	/**
	 * Subtract days.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbDays
	 *            the nb days
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract days.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbDays
	 *            the nb days
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_days", "subtract_days" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Subtract a given number of days from a date",
			examples = { @example (
					value = "date('2000-01-01') minus_days 20",
					equals = "date('1999-12-12')") })
	@test ("date('2000-01-01') minus_days 20 = date('1999-12-12')")
	public static IDate subtractDays(final IScope scope, final IDate date1, final int nbDays)
			throws GamaRuntimeException {
		return date1.plus(-nbDays, DAYS);

	}

	/**
	 * Subtract hours.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbHours
	 *            the nb hours
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract hours.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbHours
	 *            the nb hours
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_hours", "subtract_hours" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Remove a given number of hours from a date",
			examples = { @example (
					value = "// equivalent to date1 - 15 #h",
					isExecutable = false),
					@example (
							value = "date('2000-01-01') minus_hours 15 ",
							equals = "date('1999-12-31 09:00:00')") })
	@test ("(date('2000-01-01') minus_hours 15)  = date('1999-12-31 09:00:00')")
	public static IDate subtractHours(final IScope scope, final IDate date1, final int nbHours)
			throws GamaRuntimeException {
		return date1.plus(-nbHours, HOURS);

	}

	/**
	 * Subtract ms.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMs
	 *            the nb ms
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract ms.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMs
	 *            the nb ms
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_ms", "subtract_ms" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Remove a given number of milliseconds from a date",
			examples = { @example (
					value = "// equivalent to date1 - 15 #ms",
					isExecutable = false),
					@example (
							value = "date('2000-01-01') minus_ms 1000 ",
							equals = "date('1999-12-31 23:59:59')") })
	@test ("date('2000-01-01') minus_ms 1000  = date('1999-12-31 23:59:59')")
	public static IDate subtractMs(final IScope scope, final IDate date1, final int nbMs) throws GamaRuntimeException {
		return date1.plus(-nbMs, ChronoUnit.MILLIS);
	}

	/**
	 * Adds the ms.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMs
	 *            the nb ms
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Adds the ms.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMs
	 *            the nb ms
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "plus_ms", "add_ms" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Add a given number of milliseconds to a date",
			examples = { @example (
					value = "// equivalent to date('2000-01-01') + 15 #ms",
					isExecutable = false),
					@example (
							value = "date('2000-01-01') plus_ms 1000 ",
							equals = "date('2000-01-01 00:00:01')") })
	@test ("date('2000-01-01') plus_ms 1000  = date('2000-01-01 00:00:01')")
	public static IDate addMs(final IScope scope, final IDate date1, final int nbMs) throws GamaRuntimeException {
		return date1.plus(nbMs, ChronoUnit.MILLIS);
	}

	/**
	 * Subtract minutes.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMinutes
	 *            the nb minutes
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Subtract minutes.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param nbMinutes
	 *            the nb minutes
	 * @return the gama date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "minus_minutes", "subtract_minutes" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Subtract a given number of minutes from a date",
			examples = { @example (
					value = "// date('2000-01-01') to date1 - 5#mn",
					isExecutable = false),
					@example (
							value = "date('2000-01-01') minus_minutes 5 ",
							equals = "date('1999-12-31 23:55:00')") })
	@test ("date('2000-01-01') minus_minutes 5  = date('1999-12-31 23:55:00')")
	public static IDate subtractMinutes(final IScope scope, final IDate date1, final int nbMinutes)
			throws GamaRuntimeException {
		return date1.plus(-nbMinutes, MINUTES);

	}

	/**
	 * Years between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Years between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "years_between" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Provide the exact number of years between two dates. This number can be positive or negative (if the second operand is smaller than the first one)",
			special_cases = { "If both dates are equal, returns 0.",
					"If date1 is after date2, returns a negative value." },
			examples = { @example (
					value = "years_between(date('2000-01-01'), date('2010-01-01'))",
					equals = "10") })
	@test ("years_between(date('2000-01-01'), date('2010-01-01')) = 10")
	@test ("years_between(date('2000-01-01'), date('2001-01-01')) = 1")
	public static int years_between(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return (int) ChronoUnit.YEARS.between(date1, date2);
	}

	/**
	 * Milliseconds between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Milliseconds between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "milliseconds_between" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Provide the exact number of milliseconds between two dates. This number can be positive or negative (if the second operand is smaller than the first one)",
			special_cases = { "If both dates are equal, returns 0.",
					"If date1 is after date2, returns a negative value." },
			examples = { @example (
					value = "milliseconds_between(date('2000-01-01'), date('2000-02-01'))",
					equals = "2.6784E9") })
	@test ("milliseconds_between(date('2000-01-01'), date('2000-02-01')) = 2.6784E9")
	@test ("milliseconds_between(date('2000-01-01'), date('2000-01-01')) = 0")
	public static double milliseconds_between(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return ChronoUnit.MILLIS.between(date1, date2);
	}

	/**
	 * Months between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Months between.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return the int
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "months_between" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Provide the exact number of months between two dates. This number can be positive or negative (if the second operand is smaller than the first one)",
			special_cases = { "If both dates are equal, returns 0.",
					"If date1 is after date2, returns a negative value." },
			examples = { @example (
					value = "months_between(date('2000-01-01'), date('2000-02-01'))",
					equals = "1") })
	@test ("months_between(date('2000-01-01'), date('2000-02-01')) = 1")
	public static int months_between(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return (int) ChronoUnit.MONTHS.between(date1, date2);
	}

	/**
	 * Greater than.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Greater than.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { ">" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the first date is strictly greater than the second one",
			examples = { @example (
					value = "(#now > (#now minus_hours 1))",
					equals = "true") })
	@test ("(#now > (#now minus_hours 1)) = true")
	public static boolean greater_than(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return date1.isGreaterThan(date2, true);
	}

	/**
	 * Greater than or equal.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Greater than or equal.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { ">=" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the first date is greater than or equal to the second one",
			examples = { @example (
					value = "#now >= #now minus_hours 1",
					equals = "true") })
	@test ("(#now >= (#now minus_hours 1)) = true")
	public static boolean greater_than_or_equal(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return date1.isGreaterThan(date2, false);
	}

	/**
	 * Smaller than.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Smaller than.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "<" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the first date is strictly smaller than the second one",
			examples = { @example (
					value = "#now < #now minus_hours 1",
					equals = "false") })
	@test ("(#now < (#now minus_hours 1)) = false")
	public static boolean smaller_than(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return date1.isSmallerThan(date2, true);
	}

	/**
	 * Smaller than or equal.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Smaller than or equal.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "<=" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the first date is smaller than or equal to the second one",
			examples = { @example (
					value = "(#now <= (#now minus_hours 1))",
					equals = "false") })
	@test ("(#now <= (#now minus_hours 1)) = false")
	public static boolean smaller_than_or_equal(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return date1.isSmallerThan(date2, false);
	}

	/**
	 * Equal.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Equal.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword.EQUALS },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the two dates are equal (i.e.they represent the same instant in time)",
			examples = { @example (
					value = "#now = #now minus_hours 1",
					equals = "false") })
	@test ("(#now = (#now minus_hours 1)) = false")
	public static boolean equal(final IScope scope, final IDate date1, final IDate date2) throws GamaRuntimeException {
		return date1.equals(date2);
	}

	/**
	 * Different.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Different.
	 *
	 * @param scope
	 *            the scope
	 * @param date1
	 *            the date 1
	 * @param date2
	 *            the date 2
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "!=" },
			content_type = IType.NONE,
			category = { IOperatorCategory.DATE },
			concept = { IConcept.DATE })
	@doc (
			value = "Returns true if the two dates are different  (i.e.they do not represent the same instant in time)",
			examples = { @example (
					value = "#now != #now minus_hours 1",
					equals = "true") })
	@test ("(#now != (#now minus_hours 1)) = true")
	public static boolean different(final IScope scope, final IDate date1, final IDate date2)
			throws GamaRuntimeException {
		return !date1.equals(date2);
	}

	/**
	 * As duration.
	 *
	 * @param d1
	 *            the d 1
	 * @param d2
	 *            the d 2
	 * @return the string
	 */
	public static String asDuration(final Temporal d1, final Temporal d2) {
		final Duration p = Duration.between(d1, d2);
		return DurationFormatter.INSTANCE.toString(p);
	}

	/**
	 * Date.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @param pattern
	 *            the pattern
	 * @return the gama date
	 */

	/**
	 * Date.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @param pattern
	 *            the pattern
	 * @return the gama date
	 */
	@operator (
			value = "date",
			can_be_const = true,
			category = { IOperatorCategory.STRING, IOperatorCategory.TIME },
			concept = { IConcept.STRING, IConcept.CAST, IConcept.TIME })
	@doc (
			value = "converts a string to a date following a custom pattern. The pattern can use \"%Y %M %N %D %E %h %m %s %z\" for outputting years, months, name of month, days, name of days, hours, minutes, seconds and the time-zone. A null or empty pattern will parse the date using one of the ISO date & time formats (similar to date('...') in that case). The pattern can also follow the pattern definition found here, which gives much more control over what will be parsed: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns. Different patterns are available by default as constant: #iso_local, #iso_simple, #iso_offset, #iso_zoned and #custom, which can be changed in the preferences ",
			masterDoc = true,
			usages = @usage (
					value = "",
					examples = @example (
							value = "date den <- date(\"1999-12-30\", 'yyyy-MM-dd');",
							test = false)))
	@no_test
	public static IDate date(final IScope scope, final String value, final String pattern) {
		return GamaDateFactory.createWith(scope, value, pattern);
	}

	/**
	 * Date.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @param pattern
	 *            the pattern
	 * @param locale
	 *            the locale
	 * @return the gama date
	 */

	/**
	 * Date.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @param pattern
	 *            the pattern
	 * @param locale
	 *            the locale
	 * @return the gama date
	 */
	@operator (
			value = "date",
			can_be_const = true,
			category = { IOperatorCategory.STRING, IOperatorCategory.TIME },
			concept = { IConcept.STRING, IConcept.CAST, IConcept.TIME })
	@doc (
			value = "converts a string to a date following a custom pattern and a specific locale (e.g. 'fr', 'en'...). The pattern can use \"%Y %M %N %D %E %h %m %s %z\" for parsing years, months, name of month, days, name of days, hours, minutes, seconds and the time-zone. A null or empty pattern will parse the date using one of the ISO date & time formats (similar to date('...') in that case). The pattern can also follow the pattern definition found here, which gives much more control over what will be parsed: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns. Different patterns are available by default as constant: #iso_local, #iso_simple, #iso_offset, #iso_zoned and #custom, which can be changed in the preferences ",
			usages = @usage (
					value = "In addition to the date and  pattern string operands, a specific locale (e.g. 'fr', 'en'...) can be added.",
					examples = @example (
							value = "date d <- date(\"1999-january-30\", 'yyyy-MMMM-dd', 'en');",
							test = false)))
	@test ("date('1999-01-30', 'yyyy-MM-dd', 'en') = date('1999-01-30 00:00:00')")
	// @test("date('1999-january-30', 'yyyy-MMMM-dd', 'en') = date('1999-01-30 00:00:00')")
	public static IDate date(final IScope scope, final String value, final String pattern, final String locale) {
		return GamaDateFactory.createWith(scope, value, pattern, locale);
	}

	/**
	 * Format.
	 *
	 * @param time
	 *            the time
	 * @param pattern
	 *            the pattern
	 * @return the string
	 */

	/**
	 * Format.
	 *
	 * @param time
	 *            the time
	 * @param pattern
	 *            the pattern
	 * @return the string
	 */
	@operator (
			value = "string",
			can_be_const = true,
			category = { IOperatorCategory.STRING, IOperatorCategory.TIME },
			concept = { IConcept.STRING, IConcept.CAST, IConcept.TIME })
	@doc (
			value = "converts a date to astring following a custom pattern. The pattern can use \"%Y %M %N %D %E %h %m %s %z\" for outputting years, months, name of month, days, name of days, hours, minutes, seconds and the time-zone. A null or empty pattern will return the complete date as defined by the ISO date & time format. The pattern can also follow the pattern definition found here, which gives much more control over the format of the date: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns. Different patterns are available by default as constants: #iso_local, #iso_simple, #iso_offset, #iso_zoned and #custom, which can be changed in the preferences",
			masterDoc = true,
			usages = @usage (
					value = "",
					examples = @example (
							value = "string(#now, 'yyyy-MM-dd')",
							isExecutable = false)))
	@test ("string(date('2000-01-02'),'yyyy-MM-dd') = '2000-01-02'")
	@test ("string(date('2000-01-31'),'yyyy-MM-dd') = '2000-01-31'")
	@test ("string(date('2000-01-02'),'yyyy-MM-dd') = '2000-01-02'")
	public static String format(final IDate time, final String pattern) {
		return format(time, pattern, null);
	}

	/**
	 * Format.
	 *
	 * @param time
	 *            the time
	 * @param pattern
	 *            the pattern
	 * @param locale
	 *            the locale
	 * @return the string
	 */

	/**
	 * Format.
	 *
	 * @param time
	 *            the time
	 * @param pattern
	 *            the pattern
	 * @param locale
	 *            the locale
	 * @return the string
	 */
	@operator (
			value = "string",
			can_be_const = true,
			category = { IOperatorCategory.STRING, IOperatorCategory.TIME },
			concept = { IConcept.STRING, IConcept.CAST, IConcept.TIME })
	@doc (
			value = "converts a date to astring following a custom pattern and using a specific locale (e.g.: 'fr', 'en', etc.). The pattern can use \"%Y %M %N %D %E %h %m %s %z\" for outputting years, months, name of month, days, name of days, hours, minutes, seconds and the time-zone. A null or empty pattern will return the complete date as defined by the ISO date & time format. "
					+ "The pattern can also follow the pattern definition found here, which gives much more control over the format of the date: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns. Different patterns are available by default as constants: #iso_local, #iso_simple, #iso_offset, #iso_zoned and #custom, which can be changed in the preferences",
			usages = @usage (
					value = "",
					examples = @example (
							value = "string(#now, 'yyyy-MM-dd', 'en')",
							isExecutable = false)))
	@test ("string(date('2000-01-02'),'yyyy-MMMM-dd','en') = '2000-January-02'")

	public static String format(final IDate time, final String pattern, final String locale) {
		return time.toString(pattern, locale);
	}

}
