/*******************************************************************************************************
 *
 * IDate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

import gama.annotations.precompiler.OkForAPI;
import gama.core.runtime.IScope;
import gama.core.util.list.IList;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IValue;
import gama.gaml.types.IType;

/**
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
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
	int getYear();

	/**
	 * @return
	 */
	int getMonth();

	/**
	 * @return
	 */
	int getDay();

	/**
	 * @return
	 */
	int getHour();

	/**
	 * @return
	 */
	int getMinute();

	/**
	 * @return
	 */
	int getSecond();

	/**
	 * @param scope
	 * @param contentsType
	 * @return
	 */
	IList<?> listValue(IScope scope, IType<?> contentsType);

}