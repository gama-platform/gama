/*******************************************************************************************************
 *
 * IDate.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;

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

/**
 *
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
	 * Gets the long.
	 *
	 * @param field
	 *            the field
	 * @return the long
	 */
	@Override
	long getLong(TemporalField field);

	/**
	 * @param date
	 * @param b
	 * @return
	 */
	boolean isSmallerThan(IDate date, boolean b);

	/**
	 * @param date
	 * @param b
	 * @return
	 */
	boolean isGreaterThan(IDate date, boolean b);

	/**
	 * @return
	 */
	LocalDateTime getLocalDateTime();

	/**
	 * @param scope
	 * @param period
	 * @return
	 */
	boolean isIntervalReachedOptimized(IScope scope, IExpression period);

	/**
	 * @param end
	 * @return
	 */
	boolean isAfter(IDate end);

	/**
	 * @return
	 */
	String toISOString();

	/**
	 * Checks if is before.
	 *
	 * @param startInclusive
	 *            the start inclusive
	 * @return true, if is before
	 */
	boolean isBefore(final IDate startInclusive);

	/**
	 * Plus.
	 *
	 * @param period
	 *            the period
	 * @param repeat
	 *            the repeat
	 * @param unit
	 *            the unit
	 * @return the i date
	 */
	IDate plus(final double period, final int repeat, final ChronoUnit unit);

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return the i date
	 */
	IDate plus(final IScope scope, final IExpression period);

	/**
	 * Checks if is interval reached.
	 *
	 * @param scope
	 *            the scope
	 * @param period
	 *            the period
	 * @return true, if is interval reached
	 */
	boolean isIntervalReached(final IScope scope, final IExpression period);

	/**
	 * Minus millis.
	 *
	 * @param duration
	 *            the duration
	 * @return the i date
	 */
	IDate minusMillis(final double duration);

	/**
	 * Plus millis.
	 *
	 * @param duration
	 *            the duration
	 * @return the i date
	 */
	IDate plusMillis(final double duration);

	/**
	 * Minus.
	 *
	 * @param amount
	 *            the amount
	 * @return the i date
	 */
	@Override
	IDate minus(final TemporalAmount amount);

	/**
	 * Plus.
	 *
	 * @param amount
	 *            the amount
	 * @return the i date
	 */
	@Override
	IDate plus(final TemporalAmount amount);

	/**
	 * Plus.
	 *
	 * @param duration
	 *            the duration
	 * @param unit
	 *            the unit
	 * @return the i date
	 */
	IDate plus(final double duration, final TemporalUnit unit);

	/**
	 * Minus.
	 *
	 * @param amountToAdd
	 *            the amount to add
	 * @param unit
	 *            the unit
	 * @return the i date
	 */
	@Override
	IDate minus(final long amountToAdd, final TemporalUnit unit);

	/**
	 * Plus.
	 *
	 * @param amountToAdd
	 *            the amount to add
	 * @param unit
	 *            the unit
	 * @return the i date
	 */
	@Override
	IDate plus(final long amountToAdd, final TemporalUnit unit);

	/**
	 * With.
	 *
	 * @param field
	 *            the field
	 * @param newValue
	 *            the new value
	 * @return the i date
	 */
	@Override
	IDate with(final TemporalField field, final long newValue);

	/**
	 * Checks if is supported.
	 *
	 * @param unit
	 *            the unit
	 * @return true, if is supported
	 */
	@Override
	boolean isSupported(final TemporalUnit unit);

	/**
	 * Checks if is supported.
	 *
	 * @param field
	 *            the field
	 * @return true, if is supported
	 */
	@Override
	boolean isSupported(final TemporalField field);

	/**
	 * Gets the offset date time.
	 *
	 * @return the offset date time
	 */
	OffsetDateTime getOffsetDateTime();

	/**
	 * Gets the zoned date time.
	 *
	 * @return the zoned date time
	 */
	ZonedDateTime getZonedDateTime();

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	String toString(final String string, final String locale);

	/**
	 * Gets the temporal.
	 *
	 * @return the temporal
	 */
	Temporal getTemporal();

	/**
	 * @return
	 */
	@getter ("year")
	int getYear();

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	@getter ("date")
	IDate getDate();

	/**
	 * Gets the day of year.
	 *
	 * @return the day of year
	 */
	@getter ("day_of_year")
	int getDayOfYear();

	/**
	 * Gets the second of day.
	 *
	 * @return the second of day
	 */
	@getter ("second_of_day")
	int getSecondOfDay();

	/**
	 * @return
	 */
	@getter ("month")
	int getMonth();

	/**
	 * @return
	 */
	@getter ("day")
	int getDay();

	/**
	 * @return
	 */
	@getter ("hour")
	int getHour();

	/**
	 * Gets the minute of day.
	 *
	 * @return the minute of day
	 */
	@getter ("minute_of_day")
	int getMinuteOfDay();

	/**
	 * @return
	 */
	@getter ("minute")
	int getMinute();

	/**
	 * @return
	 */
	@getter ("second")
	int getSecond();

	/**
	 * Gets the checks if is leap.
	 *
	 * @return the checks if is leap
	 */
	@getter ("leap")
	boolean getIsLeap();

	/**
	 * Gets the day week.
	 *
	 * @return the day week
	 */
	@getter ("day_of_week")
	int getDayWeek();

	/**
	 * Gets the week year.
	 *
	 * @return the week year
	 */
	@getter ("week_of_year")
	int getWeekYear();

	/**
	 * Gets the days month.
	 *
	 * @return the days month
	 */
	@getter ("days_in_month")
	int getDaysMonth();

	/**
	 * Gets the days in year.
	 *
	 * @return the days in year
	 */
	@getter ("days_in_year")
	int getDaysInYear();

	/**
	 * @param scope
	 * @param contentsType
	 * @return
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