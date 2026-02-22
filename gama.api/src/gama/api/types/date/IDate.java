/*******************************************************************************************************
 *
 * IDate.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.date;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IValue;

/**
 * The main interface for date and time operations in the GAMA platform.
 * 
 * <p>
 * {@code IDate} represents an immutable point in time with full support for temporal operations.
 * It extends {@link Temporal} to integrate seamlessly with the Java Time API (JSR-310), allowing
 * for precise date/time calculations and manipulations.
 * </p>
 * 
 * <p>
 * This interface provides access to all date/time components (year, month, day, hour, minute, second)
 * and supports comprehensive temporal arithmetic operations (addition, subtraction, comparison).
 * All implementations are immutable - operations that appear to modify a date actually return a new
 * instance with the modified values.
 * </p>
 * 
 * <h2>Key Capabilities</h2>
 * <ul>
 * <li><b>Component Access:</b> Direct access to all date/time fields through annotated getter methods</li>
 * <li><b>Temporal Arithmetic:</b> Add/subtract time using {@link java.time.temporal.TemporalAmount} or units</li>
 * <li><b>Comparisons:</b> Compare dates using standard comparison operations</li>
 * <li><b>Formatting:</b> Convert to ISO strings or custom formats with locale support</li>
 * <li><b>Integration:</b> Full compatibility with {@link java.time.LocalDateTime}, {@link java.time.ZonedDateTime}</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>{@code
 * // Creating dates (via GamaDateFactory)
 * IDate now = GamaDateFactory.now();
 * IDate specific = GamaDateFactory.createFromISOString("2024-03-15T10:30:00Z");
 * 
 * // Accessing components
 * int year = date.getYear();
 * int month = date.getMonth();       // 1-12
 * int dayOfWeek = date.getDayWeek(); // Monday = 1
 * boolean isLeap = date.getIsLeap();
 * 
 * // Temporal arithmetic
 * IDate tomorrow = today.plus(1, ChronoUnit.DAYS);
 * IDate nextMonth = today.plus(Duration.ofDays(30));
 * IDate lastYear = today.minus(1, ChronoUnit.YEARS);
 * 
 * // Modifying fields
 * IDate noon = date.with(ChronoField.HOUR_OF_DAY, 12);
 * IDate newYear = date.with(ChronoField.DAY_OF_YEAR, 1);
 * 
 * // Comparisons
 * boolean before = date1.isBefore(date2);
 * boolean after = date1.isAfter(date2);
 * int comparison = date1.compareTo(date2); // -1, 0, or 1
 * 
 * // Formatting
 * String iso = date.toISOString();
 * String custom = date.toString("yyyy-MM-dd HH:mm", "en_US");
 * }</pre>
 * 
 * <h2>Temporal Field Support</h2>
 * <p>
 * The following temporal fields are supported through the {@link #getLong(TemporalField)} method:
 * </p>
 * <ul>
 * <li>YEAR, MONTH_OF_YEAR, DAY_OF_MONTH</li>
 * <li>DAY_OF_WEEK, DAY_OF_YEAR, WEEK_OF_YEAR</li>
 * <li>HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE</li>
 * <li>MINUTE_OF_DAY, SECOND_OF_DAY</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * All implementations of this interface must be immutable and thread-safe.
 * </p>
 * 
 * @see GamaDate
 * @see GamaDateFactory
 * @see GamaDateInterval
 * @see java.time.temporal.Temporal
 * @see gama.api.gaml.types.GamaDateType
 * 
 * @author Patrick Taillandier
 * @author Alexis Drogoul
 * @since GAMA 1.7
 */

@vars ({ @variable (
		name = "day_of_week",
		type = IType.INT,
		doc = { @doc ("Returns the index of the day of the week (with Monday being 1)") }),
		@variable (
				name = "date",
				type = IType.DATE,
				doc = { @doc ("Returns a new date object with only the year-month-day components of this date") }),
		@variable (
				name = "leap",
				type = IType.BOOL,
				doc = { @doc ("Returns true if the year is a leap year") }),
		@variable (
				name = "days_in_month",
				type = IType.INT,
				doc = { @doc ("Returns the number of days of the month (28-31) of this date") }),
		@variable (
				name = "day_of_year",
				type = IType.INT,
				doc = { @doc ("Returns the current day number of the year of this date") }),
		@variable (
				name = "days_in_year",
				type = IType.INT,
				doc = { @doc ("Returns the number of days of the year (365-366) of this date") }),
		@variable (
				name = "week_of_year",
				type = IType.INT,
				doc = { @doc ("Returns the week (1-52) of the year") }),
		@variable (
				name = "second",
				type = IType.INT,
				doc = { @doc ("Returns the second of minute (0-59) of this date") }),
		@variable (
				name = "second_of_day",
				type = IType.INT,
				doc = { @doc ("Returns the second of day (0-86399) of this date") }),
		@variable (
				name = "minute",
				type = IType.INT,
				doc = { @doc ("Returns the minute of hour (0-59) of this date") }),
		@variable (
				name = "minute_of_day",
				type = IType.INT,
				doc = { @doc ("Returns the minute of day (0-1439) of this date") }),
		@variable (
				name = "hour",
				type = IType.INT,
				doc = { @doc ("Returns the hour of the day (0-23) of this date") }),
		@variable (
				name = "day",
				type = IType.INT,
				doc = { @doc ("Returns the day of month (1-31) of this date") }),
		@variable (
				name = "month",
				type = IType.INT,
				doc = { @doc ("Returns the month of year (1-12) of this date") }),
		@variable (
				name = "year",
				type = IType.INT,
				doc = { @doc ("Returns the year") }) })
public interface IDate extends IValue, Temporal, Comparable<IDate> {

	/**
	 * Gets the value of the specified temporal field as a long.
	 * 
	 * <p>
	 * This method provides access to all temporal fields supported by the underlying temporal object.
	 * Common fields include YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, HOUR_OF_DAY, MINUTE_OF_HOUR, etc.
	 * </p>
	 *
	 * @param field
	 *            the temporal field to query (e.g., ChronoField.YEAR)
	 * @return the value of the field as a long
	 * @throws java.time.temporal.UnsupportedTemporalTypeException
	 *             if the field is not supported
	 * @see java.time.temporal.ChronoField
	 */
	@Override
	long getLong(TemporalField field);

	/**
	 * Checks if this date is smaller than another date.
	 * 
	 * @param date
	 *            the date to compare with
	 * @param orEqual
	 *            if true, also returns true when dates are equal
	 * @return true if this date is smaller than (or equal to, if orEqual is true) the specified date
	 */
	boolean isSmallerThan(IDate date, boolean orEqual);

	/**
	 * Checks if this date is greater than another date.
	 * 
	 * @param date
	 *            the date to compare with
	 * @param orEqual
	 *            if true, also returns true when dates are equal
	 * @return true if this date is greater than (or equal to, if orEqual is true) the specified date
	 */
	boolean isGreaterThan(IDate date, boolean orEqual);

	/**
	 * Converts this date to a LocalDateTime.
	 * 
	 * <p>
	 * Returns a LocalDateTime representation of this date, discarding any time zone or offset information.
	 * </p>
	 * 
	 * @return the LocalDateTime representation
	 */
	LocalDateTime getLocalDateTime();

	/**
	 * Checks if a periodic interval has been reached, optimized version.
	 * 
	 * <p>
	 * This optimized method checks whether the current date represents a point where a periodic
	 * interval (specified by the period expression) has been reached. Used internally for
	 * efficient periodic event scheduling in simulations.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param period
	 *            the expression defining the period
	 * @return true if the interval has been reached
	 */
	boolean isIntervalReachedOptimized(IScope scope, IExpression period);

	/**
	 * Checks if this date is after the specified date.
	 * 
	 * @param end
	 *            the date to compare with
	 * @return true if this date is strictly after the specified date
	 */
	boolean isAfter(IDate end);

	/**
	 * Converts this date to an ISO-8601 formatted string.
	 * 
	 * <p>
	 * Returns a string representation in ISO-8601 format, for example:
	 * "2024-03-15T14:30:00+01:00"
	 * </p>
	 * 
	 * @return the ISO-8601 string representation of this date
	 */
	String toISOString();

	/**
	 * Checks if this date is before the specified date.
	 *
	 * @param startInclusive
	 *            the date to compare with
	 * @return true if this date is strictly before the specified date
	 */
	boolean isBefore(final IDate startInclusive);

	/**
	 * Adds a period to this date with repetition.
	 * 
	 * <p>
	 * Adds the specified period multiplied by repeat count to this date.
	 * For example, plus(2.5, 3, ChronoUnit.DAYS) adds 7.5 days.
	 * </p>
	 *
	 * @param period
	 *            the amount to add (can be fractional)
	 * @param repeat
	 *            the number of times to apply the period
	 * @param unit
	 *            the temporal unit (e.g., ChronoUnit.DAYS, ChronoUnit.HOURS)
	 * @return a new IDate with the period added
	 */
	IDate plus(final double period, final int repeat, final ChronoUnit unit);

	/**
	 * Adds a period defined by an expression to this date.
	 * 
	 * <p>
	 * Evaluates the period expression in the given scope and adds the resulting
	 * duration to this date.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope for evaluating the expression
	 * @param period
	 *            the expression defining the period to add
	 * @return a new IDate with the period added
	 */
	IDate plus(final IScope scope, final IExpression period);

	/**
	 * Checks if a periodic interval has been reached.
	 * 
	 * <p>
	 * Determines whether the current date represents a point where a periodic
	 * interval (defined by the period expression) has been completed.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope for evaluating the expression
	 * @param period
	 *            the expression defining the period
	 * @return true if the interval has been reached
	 */
	boolean isIntervalReached(final IScope scope, final IExpression period);

	/**
	 * Subtracts milliseconds from this date.
	 * 
	 * @param duration
	 *            the number of milliseconds to subtract (can be fractional)
	 * @return a new IDate with the duration subtracted
	 */
	IDate minusMillis(final double duration);

	/**
	 * Adds milliseconds to this date.
	 * 
	 * @param duration
	 *            the number of milliseconds to add (can be fractional)
	 * @return a new IDate with the duration added
	 */
	IDate plusMillis(final double duration);

	/**
	 * Subtracts a temporal amount from this date.
	 * 
	 * <p>
	 * Subtracts the specified amount (e.g., Duration or Period) from this date and returns
	 * a new IDate instance. The original date is not modified.
	 * </p>
	 * 
	 * <p>Example: {@code date.minus(Duration.ofHours(2))}</p>
	 *
	 * @param amount
	 *            the amount to subtract (e.g., Duration.ofDays(5))
	 * @return a new IDate with the amount subtracted
	 */
	@Override
	IDate minus(final TemporalAmount amount);

	/**
	 * Adds a temporal amount to this date.
	 * 
	 * <p>
	 * Adds the specified amount (e.g., Duration or Period) to this date and returns
	 * a new IDate instance. The original date is not modified.
	 * </p>
	 * 
	 * <p>Example: {@code date.plus(Duration.ofDays(7))}</p>
	 *
	 * @param amount
	 *            the amount to add (e.g., Period.ofMonths(3))
	 * @return a new IDate with the amount added
	 */
	@Override
	IDate plus(final TemporalAmount amount);

	/**
	 * Adds a duration in the specified unit to this date.
	 * 
	 * <p>
	 * Adds the specified duration (which can be fractional) in the given unit.
	 * For example, {@code date.plus(2.5, ChronoUnit.HOURS)} adds 2 hours and 30 minutes.
	 * </p>
	 *
	 * @param duration
	 *            the amount to add (can be fractional, e.g., 2.5)
	 * @param unit
	 *            the temporal unit (e.g., ChronoUnit.DAYS, ChronoUnit.HOURS)
	 * @return a new IDate with the duration added
	 */
	IDate plus(final double duration, final TemporalUnit unit);

	/**
	 * Subtracts a specified amount in the given unit from this date.
	 * 
	 * <p>
	 * Returns a copy of this date with the specified amount subtracted.
	 * </p>
	 * 
	 * <p>Example: {@code date.minus(5, ChronoUnit.DAYS)}</p>
	 *
	 * @param amountToAdd
	 *            the amount of the unit to subtract
	 * @param unit
	 *            the temporal unit (e.g., ChronoUnit.WEEKS)
	 * @return a new IDate with the amount subtracted
	 */
	@Override
	IDate minus(final long amountToAdd, final TemporalUnit unit);

	/**
	 * Adds a specified amount in the given unit to this date.
	 * 
	 * <p>
	 * Returns a copy of this date with the specified amount added.
	 * </p>
	 * 
	 * <p>Example: {@code date.plus(10, ChronoUnit.MINUTES)}</p>
	 *
	 * @param amountToAdd
	 *            the amount of the unit to add
	 * @param unit
	 *            the temporal unit (e.g., ChronoUnit.SECONDS)
	 * @return a new IDate with the amount added
	 */
	@Override
	IDate plus(final long amountToAdd, final TemporalUnit unit);

	/**
	 * Returns a copy of this date with the specified field set to a new value.
	 * 
	 * <p>
	 * This allows modifying individual fields of a date. For example, to set the hour to 14:
	 * {@code date.with(ChronoField.HOUR_OF_DAY, 14)}
	 * </p>
	 *
	 * @param field
	 *            the field to set (e.g., ChronoField.MONTH_OF_YEAR)
	 * @param newValue
	 *            the new value for the field
	 * @return a new IDate with the specified field updated
	 * @throws java.time.DateTimeException
	 *             if the value is invalid for the field
	 */
	@Override
	IDate with(final TemporalField field, final long newValue);

	/**
	 * Checks if the specified temporal unit is supported.
	 * 
	 * <p>
	 * Determines whether arithmetic operations can be performed using the specified unit.
	 * </p>
	 *
	 * @param unit
	 *            the unit to check (e.g., ChronoUnit.DAYS)
	 * @return true if the unit is supported
	 */
	@Override
	boolean isSupported(final TemporalUnit unit);

	/**
	 * Checks if the specified temporal field is supported.
	 * 
	 * <p>
	 * Determines whether the specified field can be queried or modified.
	 * </p>
	 *
	 * @param field
	 *            the field to check (e.g., ChronoField.YEAR)
	 * @return true if the field is supported
	 */
	@Override
	boolean isSupported(final TemporalField field);

	/**
	 * Converts this date to an OffsetDateTime.
	 * 
	 * <p>
	 * Returns an OffsetDateTime representation with the date's zone offset.
	 * </p>
	 *
	 * @return the OffsetDateTime representation
	 */
	OffsetDateTime getOffsetDateTime();

	/**
	 * Converts this date to a ZonedDateTime.
	 * 
	 * <p>
	 * Returns a ZonedDateTime representation with the date's complete zone information.
	 * </p>
	 *
	 * @return the ZonedDateTime representation
	 */
	ZonedDateTime getZonedDateTime();

	/**
	 * Formats this date as a string using the specified pattern and locale.
	 * 
	 * <p>
	 * Uses DateTimeFormatter with the given pattern and locale to produce a formatted string.
	 * </p>
	 * 
	 * <p>Example: {@code date.toString("yyyy-MM-dd HH:mm:ss", "fr_FR")}</p>
	 *
	 * @param string
	 *            the format pattern (e.g., "dd/MM/yyyy HH:mm")
	 * @param locale
	 *            the locale string (e.g., "en_US", "fr_FR")
	 * @return the formatted date string
	 */
	String toString(final String string, final String locale);

	/**
	 * Gets the underlying temporal object.
	 * 
	 * <p>
	 * Returns the internal Temporal representation (typically a ZonedDateTime).
	 * </p>
	 *
	 * @return the underlying Temporal object
	 */
	Temporal getTemporal();

	/**
	 * Gets the year component of this date.
	 * 
	 * @return the year (e.g., 2024)
	 */
	@getter ("year")
	int getYear();

	/**
	 * Gets a new date with only the year-month-day components.
	 * 
	 * <p>
	 * Returns a new IDate representing the same calendar date but with the time
	 * component set to midnight (00:00:00).
	 * </p>
	 *
	 * @return a date with time set to midnight
	 */
	@getter ("date")
	IDate getDate();

	/**
	 * Gets the day of the year.
	 * 
	 * @return the day of year (1-365 or 1-366 for leap years)
	 */
	@getter ("day_of_year")
	int getDayOfYear();

	/**
	 * Gets the second of the day.
	 * 
	 * @return the second of day (0-86399)
	 */
	@getter ("second_of_day")
	int getSecondOfDay();

	/**
	 * Gets the month component of this date.
	 * 
	 * @return the month (1-12, where 1=January, 12=December)
	 */
	@getter ("month")
	int getMonth();

	/**
	 * Gets the day of month component.
	 * 
	 * @return the day of month (1-31)
	 */
	@getter ("day")
	int getDay();

	/**
	 * Gets the hour component.
	 * 
	 * @return the hour of day (0-23)
	 */
	@getter ("hour")
	int getHour();

	/**
	 * Gets the minute of the day.
	 * 
	 * @return the minute of day (0-1439)
	 */
	@getter ("minute_of_day")
	int getMinuteOfDay();

	/**
	 * Gets the minute component.
	 * 
	 * @return the minute of hour (0-59)
	 */
	@getter ("minute")
	int getMinute();

	/**
	 * Gets the second component.
	 * 
	 * @return the second of minute (0-59)
	 */
	@getter ("second")
	int getSecond();

	/**
	 * Checks if this date's year is a leap year.
	 * 
	 * <p>
	 * A leap year has 366 days instead of the usual 365 days.
	 * </p>
	 *
	 * @return true if the year is a leap year
	 */
	@getter ("leap")
	boolean getIsLeap();

	/**
	 * Gets the day of the week.
	 * 
	 * @return the day of week (1-7, where 1=Monday, 7=Sunday)
	 */
	@getter ("day_of_week")
	int getDayWeek();

	/**
	 * Gets the week number of the year.
	 * 
	 * @return the week of year (1-52 or 1-53)
	 */
	@getter ("week_of_year")
	int getWeekYear();

	/**
	 * Gets the number of days in this date's month.
	 * 
	 * @return the number of days in month (28-31)
	 */
	@getter ("days_in_month")
	int getDaysMonth();

	/**
	 * Gets the number of days in this date's year.
	 * 
	 * @return the number of days in year (365 or 366 for leap years)
	 */
	@getter ("days_in_year")
	int getDaysInYear();

	/**
	 * Converts this date to a list representation.
	 * 
	 * <p>
	 * Returns a list containing the date components, useful for certain GAMA operations.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param contentsType
	 *            the type of list contents
	 * @return a list representation of this date
	 */
	IList<?> listValue(IScope scope, IType<?> contentsType);

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default IType<?> getGamlType() { return Types.DATE; }

}