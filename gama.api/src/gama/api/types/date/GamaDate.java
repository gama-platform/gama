/*******************************************************************************************************
 *
 * GamaDate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.date;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.DAY_OF_YEAR;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.WeekFields;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.constants.GamlCoreUnits;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaDateType;
import gama.api.gaml.types.IType;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * Immutable implementation of the {@link IDate} interface for GAMA.
 *
 * <p>
 * Record {@code GamaDate} provides a thread-safe, immutable wrapper around Java's JSR-310 temporal classes
 * ({@link java.time.LocalDateTime}, {@link java.time.ZonedDateTime}, {@link java.time.OffsetDateTime}). All temporal
 * operations return new instances rather than modifying the existing date.
 * </p>
 *
 * <h2>Implementation Details</h2>
 *
 * <h3>Immutability</h3>
 * <p>
 * Once created, a GamaDate instance cannot be modified. All operations that appear to change the date (plus, minus,
 * with) create and return new instances. This ensures thread safety and prevents accidental modifications.
 * </p>
 *
 * <h3>Time Zone Handling</h3>
 * <p>
 * GamaDate intelligently handles time zones:
 * </p>
 * <ul>
 * <li>If the source temporal has zone information (ZonedDateTime), it is preserved</li>
 * <li>If only an offset is available (OffsetDateTime), a zone is created from the offset</li>
 * <li>If no zone information exists, the default system zone is used</li>
 * </ul>
 *
 * <h3>Partial Dates</h3>
 * <p>
 * GamaDate can handle partial temporal information:
 * </p>
 * <ul>
 * <li>Dates without time (uses 00:00:00 as the time)</li>
 * <li>Times without dates (uses the simulation starting date or epoch)</li>
 * </ul>
 *
 * <h2>Creation</h2>
 * <p>
 * GamaDate instances should be created using {@link GamaDateFactory} methods:
 * </p>
 *
 * <pre>{@code
 * // From ISO string
 * IDate date = GamaDateFactory.createFromISOString("2024-03-15T14:30:00Z");
 *
 * // From temporal object
 * IDate date = GamaDateFactory.createFromTemporal(LocalDateTime.now());
 *
 * // From string with pattern
 * IDate date = GamaDateFactory.createWith(scope, "15/03/2024", "dd/MM/yyyy");
 *
 * // From double (seconds since starting date)
 * IDate date = GamaDateFactory.createFromDouble(scope, 86400.0); // 1 day
 * }</pre>
 *
 * <h2>String Parsing</h2>
 * <p>
 * GamaDate supports flexible string parsing with automatic format detection and custom patterns:
 * </p>
 *
 * <pre>{@code
 * // ISO format (automatic)
 * new GamaDate(scope, "2024-03-15T14:30:00Z");
 *
 * // Custom pattern
 * new GamaDate(scope, "15/03/2024 14:30", "dd/MM/yyyy HH:mm");
 *
 * // With locale
 * new GamaDate(scope, "March 15, 2024", "MMMM d, yyyy", "en_US");
 * }</pre>
 *
 * <h2>Common Operations</h2>
 *
 * <h3>Arithmetic</h3>
 *
 * <pre>{@code
 * // Add time
 * IDate tomorrow = date.plus(1, ChronoUnit.DAYS);
 * IDate nextWeek = date.plus(Duration.ofDays(7));
 * IDate nextMonth = date.plus(1, ChronoUnit.MONTHS);
 *
 * // Subtract time
 * IDate yesterday = date.minus(1, ChronoUnit.DAYS);
 * IDate lastHour = date.minusMillis(3600000);
 *
 * // Field modification
 * IDate noon = date.with(ChronoField.HOUR_OF_DAY, 12);
 * IDate firstOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1);
 * }</pre>
 *
 * <h3>Comparison</h3>
 *
 * <pre>{@code
 * boolean before = date1.isBefore(date2);
 * boolean after = date1.isAfter(date2);
 * int comparison = date1.compareTo(date2);
 * }</pre>
 *
 * <h3>Formatting</h3>
 *
 * <pre>{@code
 * String iso = date.toISOString();
 * String formatted = date.toString("yyyy-MM-dd HH:mm:ss", "en");
 * String gaml = date.serializeToGaml(false);
 * }</pre>
 *
 * @see IDate
 * @see GamaDateFactory
 * @see java.time.LocalDateTime
 * @see java.time.ZonedDateTime
 *
 * @author Patrick Taillandier
 * @author Alexis Drogoul
 * @since GAMA 1.7
 */

/**
 * The sole record component of {@code GamaDate}. Holds the underlying JSR-310 {@link Temporal} value that represents
 * this date and time. The concrete runtime type may be a {@link java.time.LocalDateTime},
 * {@link java.time.ZonedDateTime}, or {@link java.time.OffsetDateTime}, depending on how the instance was constructed.
 * All temporal field queries and arithmetic operations are delegated to this object.
 */
record GamaDate(Temporal internal) implements IDate {

	/**
	 * Returns the complete number of seconds elapsed between the simulation's starting date and this date. This value
	 * is equivalent to a duration expressed in seconds. When no simulation is available in the scope, the global
	 * {@link GamaDateType#DATES_STARTING_DATE} value is used as the reference point instead.
	 *
	 * <pre>{@code
	 * // In a GAMA simulation context:
	 * double elapsed = date.floatValue(scope); // seconds since simulation start
	 * }</pre>
	 *
	 * @param scope
	 *            the current execution scope, used to retrieve the running {@link ISimulationAgent} and its starting
	 *            date; must not be {@code null}
	 * @return the number of whole and fractional seconds between the simulation's starting date and this date; may be
	 *         negative if this date precedes the starting date
	 */
	@Override
	public double floatValue(final IScope scope) {
		final ISimulationAgent sim = scope.getSimulation();
		if (sim == null) return GamaDateType.DATES_STARTING_DATE.getValue().until(this, ChronoUnit.SECONDS);
		return sim.getStartingDate().until(this, ChronoUnit.SECONDS);
	}

	/**
	 * Returns the number of whole seconds elapsed between the simulation's starting date and this date, truncated to an
	 * {@code int}. This is the integer counterpart of {@link #floatValue(IScope)}: the fractional part of the duration
	 * is discarded via a cast to {@code int}.
	 *
	 * <pre>{@code
	 * int elapsed = date.intValue(scope); // truncated seconds since simulation start
	 * }</pre>
	 *
	 * @param scope
	 *            the current execution scope, used to retrieve the running {@link ISimulationAgent} and its starting
	 *            date; must not be {@code null}
	 * @return the truncated integer number of seconds between the simulation's starting date and this date
	 * @see #floatValue(IScope)
	 */
	@Override
	public int intValue(final IScope scope) {
		return (int) floatValue(scope);
	}

	/**
	 * Returns a {@link IList} containing the individual date/time components of this date in the following fixed order:
	 * {@code [year, month, dayOfMonth, hourOfDay, minute, second]}. All values are extracted from the
	 * {@link LocalDateTime} projection of the underlying temporal. The list elements are cast to the content type
	 * {@code ct} using the supplied scope.
	 *
	 * <pre>{@code
	 * IList<?> parts = date.listValue(scope, Types.INT);
	 * // parts = [2024, 3, 15, 14, 30, 0] for 2024-03-15T14:30:00
	 * }</pre>
	 *
	 * @param scope
	 *            the current execution scope used for list creation and type casting; must not be {@code null}
	 * @param ct
	 *            the target {@link IType} to which each list element will be cast; must not be {@code null}
	 * @return a new {@link IList} of six elements: year, month (1–12), day of month (1–31), hour of day (0–23), minute
	 *         (0–59), and second (0–59)
	 */
	@Override
	public IList<?> listValue(final IScope scope, final IType<?> ct) {
		final LocalDateTime ld = LocalDateTime.from(internal);
		return GamaListFactory.create(scope, ct, ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), ld.getHour(),
				ld.getMinute(), ld.getSecond());
	}

	/**
	 * Returns the default human-readable string representation of this date. Delegates to
	 * {@link #toString(String, String)} with both pattern and locale set to {@code null}, which causes
	 * {@link GamaDateType#getFormatter(String, String)} to apply its built-in default format.
	 *
	 * @return a non-{@code null} string representation of this date using the default GAMA date format
	 * @see #toString(String, String)
	 */
	@Override
	public String toString() {
		return toString(null, null);
	}

	/**
	 * Serializes this date as a valid GAML {@code date} expression that can be parsed back by the GAML interpreter. The
	 * produced string has the form {@code date ('...')} where the inner value is the result of {@link #toString()}.
	 *
	 * <pre>{@code
	 * String gaml = date.serializeToGaml(false);
	 * // e.g. "date ('2024-03-15 14:30:00')"
	 * }</pre>
	 *
	 * @param includingBuiltIn
	 *            {@code true} if built-in definitions should be included in the serialization (currently unused for
	 *            dates); {@code false} otherwise
	 * @return a non-{@code null} GAML expression string representing this date
	 * @see #toString()
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "date ('" + toString() + "')";
	}

	/**
	 * Returns a string representation of this date suitable for display within a GAMA simulation. Delegates to
	 * {@link #toString()}, which uses the default GAMA date format.
	 *
	 * @param scope
	 *            the current execution scope (not used by this implementation, but required by the {@link IDate}
	 *            contract)
	 * @return a non-{@code null} string representation of this date using the default GAMA date format
	 * @see #toString()
	 */
	@Override
	public String stringValue(final IScope scope) {
		return toString();
	}

	/**
	 * Creates and returns a new {@link IDate} instance that represents the same point in time as this date. Because
	 * {@code GamaDate} is immutable, this effectively returns a semantically equivalent but independently allocated
	 * instance by delegating to {@link GamaDateFactory#createFromTemporal(IScope, Temporal)}.
	 *
	 * @param scope
	 *            the current execution scope; must not be {@code null}
	 * @return a new, non-{@code null} {@link IDate} holding the same temporal value as this instance
	 * @throws GamaRuntimeException
	 *             if the factory method fails to construct the new instance
	 */
	@Override
	public IDate copy(final IScope scope) throws GamaRuntimeException {
		return GamaDateFactory.createFromTemporal(scope, internal);
	}

	/**
	 * Returns the year field of this date as a proleptic year number (e.g. 2024). The value is retrieved from the
	 * {@link ChronoField#YEAR} field of the underlying temporal.
	 *
	 * @return the four-digit proleptic year of this date (e.g. {@code 2024})
	 */
	@Override
	public int getYear() { return internal.get(YEAR); }

	/**
	 * Returns a new {@link IDate} that contains only the date portion of this instance (year, month, and day), with the
	 * time component set to midnight (00:00:00). The returned instance is backed by a {@link LocalDate} temporal.
	 *
	 * <pre>{@code
	 * IDate dateOnly = dateTime.getDate();
	 * // dateOnly represents midnight on the same calendar day as dateTime
	 * }</pre>
	 *
	 * @return a new, non-{@code null} {@link IDate} representing midnight on the same calendar day as this instance
	 * @see GamaDateFactory#createFromTemporal(Temporal)
	 */
	@Override
	public IDate getDate() { return GamaDateFactory.createFromTemporal(LocalDate.of(getYear(), getMonth(), getDay())); }

	/**
	 * Returns the day-of-year field of this date, i.e. the ordinal position of this date within its year, starting at 1
	 * for January 1st. The value is retrieved from the {@link ChronoField#DAY_OF_YEAR} field of the underlying
	 * temporal.
	 *
	 * @return the day of the year in the range [1, 365] (or [1, 366] in a leap year)
	 */
	@Override
	public int getDayOfYear() { return internal.get(DAY_OF_YEAR); }

	/**
	 * Returns the second-of-day field of this date, i.e. the total number of seconds elapsed since midnight on the same
	 * calendar day. The value is retrieved from the {@link ChronoField#SECOND_OF_DAY} field of the underlying temporal.
	 *
	 * @return the second of the day in the range [0, 86399]
	 */
	@Override
	public int getSecondOfDay() { return internal.get(ChronoField.SECOND_OF_DAY); }

	/**
	 * Returns the month-of-year field of this date as an integer in the range 1 (January) to 12 (December). The value
	 * is retrieved from the {@link ChronoField#MONTH_OF_YEAR} field of the underlying temporal.
	 *
	 * @return the month of the year in the range [1, 12]
	 */
	@Override
	public int getMonth() { return internal.get(MONTH_OF_YEAR); }

	/**
	 * Returns the day-of-month field of this date, i.e. the calendar day number within its month. The value is
	 * retrieved from the {@link ChronoField#DAY_OF_MONTH} field of the underlying temporal.
	 *
	 * @return the day of the month in the range [1, 28/29/30/31] depending on the month and year
	 */
	@Override
	public int getDay() { return internal.get(DAY_OF_MONTH); }

	/**
	 * Returns the hour-of-day field of this date using a 24-hour clock. The value is retrieved from the
	 * {@link ChronoField#HOUR_OF_DAY} field of the underlying temporal.
	 *
	 * @return the hour of the day in the range [0, 23]
	 */
	@Override
	public int getHour() { return internal.get(ChronoField.HOUR_OF_DAY); }

	/**
	 * Returns the minute-of-hour field of this date. The value is retrieved from the {@link ChronoField#MINUTE_OF_HOUR}
	 * field of the underlying temporal.
	 *
	 * @return the minute within the current hour in the range [0, 59]
	 */
	@Override
	public int getMinute() { return internal.get(MINUTE_OF_HOUR); }

	/**
	 * Returns the minute-of-day field of this date, i.e. the total number of minutes elapsed since midnight on the same
	 * calendar day. The value is retrieved from the {@link ChronoField#MINUTE_OF_DAY} field of the underlying temporal.
	 *
	 * @return the minute of the day in the range [0, 1439]
	 */
	@Override
	public int getMinuteOfDay() { return internal.get(ChronoField.MINUTE_OF_DAY); }

	/**
	 * Returns the second-of-minute field of this date. The value is retrieved from the
	 * {@link ChronoField#SECOND_OF_MINUTE} field of the underlying temporal.
	 *
	 * @return the second within the current minute in the range [0, 59]
	 */
	@Override
	public int getSecond() { return internal.get(SECOND_OF_MINUTE); }

	/**
	 * Returns the day-of-week field of this date as an integer following the ISO-8601 standard, where Monday is 1 and
	 * Sunday is 7. The value is retrieved from the {@link ChronoField#DAY_OF_WEEK} field of the underlying temporal.
	 *
	 * @return the ISO day of the week in the range [1 (Monday), 7 (Sunday)]
	 */
	@Override
	public int getDayWeek() { return internal.get(DAY_OF_WEEK); }

	/**
	 * Returns {@code true} if the year of this date is a leap year according to the proleptic Gregorian calendar. A
	 * leap year has 366 days instead of the usual 365 and occurs when the year is divisible by 4, except for years
	 * divisible by 100, which must also be divisible by 400.
	 *
	 * @return {@code true} if the year of this date is a leap year; {@code false} otherwise
	 * @see LocalDate#isLeapYear()
	 */
	@Override
	public boolean getIsLeap() { return LocalDate.from(internal).isLeapYear(); }

	/**
	 * Returns the ISO week number within the year of this date, as defined by ISO-8601. The ISO week numbering scheme
	 * assigns week 1 to the week that contains the year's first Thursday, with weeks starting on Monday. The result is
	 * in the range [1, 52] (or [1, 53] for years that have 53 ISO weeks).
	 *
	 * @return the ISO week-of-year number in the range [1, 52] or [1, 53]
	 * @see WeekFields#ISO
	 */
	@Override
	public int getWeekYear() { return internal.get(WeekFields.ISO.weekOfYear()); }

	/**
	 * Returns the number of days in the month of this date. This accounts for the actual length of the month, including
	 * whether February has 28 or 29 days in leap years. For example, this returns {@code 31} for January, {@code 28} or
	 * {@code 29} for February, {@code 30} for April, etc.
	 *
	 * @return the number of days in the month containing this date, in the range [28, 31]
	 * @see LocalDate#lengthOfMonth()
	 */
	@Override
	public int getDaysMonth() { return LocalDate.from(internal).lengthOfMonth(); }

	/**
	 * Returns the number of days in the year of this date. This is {@code 366} for leap years and {@code 365} for all
	 * other years.
	 *
	 * @return {@code 366} if the year of this date is a leap year; {@code 365} otherwise
	 * @see LocalDate#lengthOfYear()
	 */
	@Override
	public int getDaysInYear() { return LocalDate.from(internal).lengthOfYear(); }

	/**
	 * Returns the underlying JSR-310 {@link Temporal} object that backs this {@code GamaDate}. The concrete type may be
	 * a {@link java.time.LocalDateTime}, {@link java.time.ZonedDateTime}, or {@link java.time.OffsetDateTime} depending
	 * on how the instance was originally constructed. Direct use of this object allows access to the full JSR-310 API.
	 *
	 * @return the non-{@code null} underlying {@link Temporal} value of this date
	 */
	@Override
	public Temporal getTemporal() { return internal; }

	/**
	 * Returns the date and time represented by this instance as a {@link LocalDateTime}, stripping any time-zone or
	 * offset information. The conversion is performed by {@link LocalDateTime#from(Temporal)} on the underlying
	 * temporal. This is the canonical representation used internally for comparisons and arithmetic.
	 *
	 * @return a non-{@code null} {@link LocalDateTime} representing the local date and time of this instance
	 * @throws java.time.DateTimeException
	 *             if the underlying temporal does not contain enough information to form a {@link LocalDateTime}
	 */
	@Override
	public LocalDateTime getLocalDateTime() { return LocalDateTime.from(internal); }

	/**
	 * Returns the date and time represented by this instance as a {@link ZonedDateTime}, including full time-zone
	 * information. The conversion is performed by {@link ZonedDateTime#from(Temporal)} on the underlying temporal. If
	 * the underlying temporal does not carry zone information, the conversion may fail.
	 *
	 * @return a non-{@code null} {@link ZonedDateTime} representing this date with its associated time zone
	 * @throws java.time.DateTimeException
	 *             if the underlying temporal does not contain sufficient zone information to form a
	 *             {@link ZonedDateTime}
	 */
	@Override
	public ZonedDateTime getZonedDateTime() { return ZonedDateTime.from(internal); }

	/**
	 * Returns the date and time represented by this instance as an {@link OffsetDateTime}, including the UTC offset.
	 * The conversion is performed by {@link OffsetDateTime#from(Temporal)} on the underlying temporal. If the
	 * underlying temporal does not carry offset information, the conversion may fail.
	 *
	 * @return a non-{@code null} {@link OffsetDateTime} representing this date with its UTC offset
	 * @throws java.time.DateTimeException
	 *             if the underlying temporal does not contain sufficient offset information to form an
	 *             {@link OffsetDateTime}
	 */
	@Override
	public OffsetDateTime getOffsetDateTime() { return OffsetDateTime.from(internal); }

	/**
	 * Returns {@code true} if the specified temporal field is supported by this date. In addition to the fields
	 * natively supported by the underlying {@link Temporal}, this implementation also reports support for
	 * {@link ChronoField#OFFSET_SECONDS} and {@link ChronoField#INSTANT_SECONDS}, which are handled with fallback
	 * values in {@link #getLong(TemporalField)} when the underlying temporal does not provide them directly.
	 *
	 * @param field
	 *            the temporal field to test; may be {@code null} (returns {@code false})
	 * @return {@code true} if the field is supported natively or via fallback; {@code false} otherwise
	 * @see #getLong(TemporalField)
	 */
	@Override
	public boolean isSupported(final TemporalField field) {
		return internal.isSupported(field) || ChronoField.OFFSET_SECONDS.equals(field)
				|| ChronoField.INSTANT_SECONDS.equals(field);
	}

	/**
	 * Returns the {@code long} value of the specified temporal field for this date. If the underlying {@link Temporal}
	 * supports the field natively, its value is returned directly. Otherwise, the following fallbacks apply:
	 * <ul>
	 * <li>{@link ChronoField#OFFSET_SECONDS}: returns the total seconds of the default modeler time-zone offset
	 * ({@link GamaDateFactory#DEFAULT_OFFSET_IN_SECONDS}), assuming no explicit offset was provided.</li>
	 * <li>{@link ChronoField#INSTANT_SECONDS}: returns the number of seconds elapsed between the UNIX epoch
	 * ({@link GamaDateFactory#EPOCH}) and this date.</li>
	 * <li>Any other unsupported field: returns {@code 0}.</li>
	 * </ul>
	 *
	 * @param field
	 *            the temporal field whose value is requested; must not be {@code null}
	 * @return the {@code long} value of the requested field, or a fallback value as described above
	 * @throws java.time.temporal.UnsupportedTemporalTypeException
	 *             if the field is not supported and no fallback applies (in practice, returns 0 for unknown fields)
	 */
	@Override
	public long getLong(final TemporalField field) {
		if (internal.isSupported(field)) return internal.getLong(field);
		if (ChronoField.OFFSET_SECONDS.equals(field)) // If no offset or time zone is supplied, we assume it is the zone
														// of the modeler
			return GamaDateFactory.DEFAULT_OFFSET_IN_SECONDS.getTotalSeconds();
		if (ChronoField.INSTANT_SECONDS.equals(field)) return GamaDateFactory.EPOCH.until(internal, ChronoUnit.SECONDS);
		return 0l;

	}

	/**
	 * Returns {@code true} if the specified temporal unit is supported for arithmetic operations on this date. The
	 * check is delegated entirely to the underlying {@link Temporal#isSupported(TemporalUnit)}.
	 *
	 * @param unit
	 *            the temporal unit to test; may be {@code null} (returns {@code false})
	 * @return {@code true} if the unit can be used in {@link #plus(long, TemporalUnit)} and
	 *         {@link #minus(long, TemporalUnit)} operations; {@code false} otherwise
	 */
	@Override
	public boolean isSupported(final TemporalUnit unit) {
		return internal.isSupported(unit);
	}

	/**
	 * Returns a new {@link IDate} with the specified temporal field set to the given value. The adjustment is performed
	 * on the underlying {@link Temporal} via {@link Temporal#with(TemporalField, long)}, and the result is wrapped in a
	 * new {@code GamaDate} instance. This instance is not modified.
	 *
	 * <pre>{@code
	 * IDate noon = date.with(ChronoField.HOUR_OF_DAY, 12);
	 * IDate firstOfMonth = date.with(ChronoField.DAY_OF_MONTH, 1);
	 * }</pre>
	 *
	 * @param field
	 *            the temporal field to set; must not be {@code null} and must be supported by the underlying temporal
	 * @param newValue
	 *            the new value for the field
	 * @return a new, non-{@code null} {@link IDate} with the specified field adjusted
	 * @throws java.time.DateTimeException
	 *             if the value is out of range or the field is not supported
	 */
	@Override
	public IDate with(final TemporalField field, final long newValue) {
		return GamaDateFactory.createFromTemporal(internal.with(field, newValue));
	}

	/**
	 * Returns a new {@link IDate} with the specified amount added according to the given temporal unit. The addition is
	 * performed on the underlying {@link Temporal} via {@link Temporal#plus(long, TemporalUnit)}, and the result is
	 * wrapped in a new {@code GamaDate} instance. This instance is not modified.
	 *
	 * <pre>{@code
	 * IDate tomorrow = date.plus(1, ChronoUnit.DAYS);
	 * IDate nextYear = date.plus(1, ChronoUnit.YEARS);
	 * }</pre>
	 *
	 * @param amountToAdd
	 *            the amount of the unit to add; may be negative to subtract
	 * @param unit
	 *            the unit of the amount to add; must not be {@code null} and must be supported
	 * @return a new, non-{@code null} {@link IDate} with the specified amount added
	 * @throws java.time.DateTimeException
	 *             if the addition is not possible or produces an out-of-range value
	 */
	@Override
	public IDate plus(final long amountToAdd, final TemporalUnit unit) {
		return GamaDateFactory.createFromTemporal(internal.plus(amountToAdd, unit));
	}

	/**
	 * Returns a new {@link IDate} with the specified amount subtracted according to the given temporal unit. The
	 * subtraction is performed on the underlying {@link Temporal} via {@link Temporal#minus(long, TemporalUnit)}, and
	 * the result is wrapped in a new {@code GamaDate} instance. This instance is not modified.
	 *
	 * <pre>{@code
	 * IDate yesterday = date.minus(1, ChronoUnit.DAYS);
	 * IDate lastMonth = date.minus(1, ChronoUnit.MONTHS);
	 * }</pre>
	 *
	 * @param amountToAdd
	 *            the amount of the unit to subtract; may be negative to effectively add
	 * @param unit
	 *            the unit of the amount to subtract; must not be {@code null} and must be supported
	 * @return a new, non-{@code null} {@link IDate} with the specified amount subtracted
	 * @throws java.time.DateTimeException
	 *             if the subtraction is not possible or produces an out-of-range value
	 */
	@Override
	public IDate minus(final long amountToAdd, final TemporalUnit unit) {
		return GamaDateFactory.createFromTemporal(internal.minus(amountToAdd, unit));
	}

	/**
	 * Calculates the amount of time until the specified end temporal, measured in the given unit. The calculation is
	 * delegated to {@link TemporalUnit#between(Temporal, Temporal)} using the underlying temporal as the start point.
	 * The result is negative if {@code endExclusive} is before this date.
	 *
	 * <pre>{@code
	 * long days = startDate.until(endDate, ChronoUnit.DAYS);
	 * long hours = startDate.until(endDate, ChronoUnit.HOURS);
	 * }</pre>
	 *
	 * @param endExclusive
	 *            the end temporal, exclusive; must not be {@code null}
	 * @param unit
	 *            the unit in which the result is expressed; must not be {@code null}
	 * @return the amount of time from this date to {@code endExclusive} in the specified unit; negative if
	 *         {@code endExclusive} is before this date
	 * @throws java.time.DateTimeException
	 *             if the amount cannot be calculated
	 */
	@Override
	public long until(final Temporal endExclusive, final TemporalUnit unit) {
		return unit.between(internal, endExclusive);
	}

	/**
	 * Formats this date using the given pattern string and locale. The formatting is delegated to
	 * {@link GamaDateType#getFormatter(String, String)}, which resolves the appropriate
	 * {@link java.time.format.DateTimeFormatter}. Passing {@code null} for both arguments produces the same output as
	 * {@link #toString()}.
	 *
	 * <pre>{@code
	 * String formatted = date.toString("yyyy-MM-dd HH:mm:ss", "en");
	 * String iso = date.toString("iso_offset_date_time", null);
	 * String defaults = date.toString(null, null);
	 * }</pre>
	 *
	 * @param string
	 *            the format pattern string (e.g. {@code "yyyy-MM-dd HH:mm:ss"}), a named GAML format key (e.g.
	 *            {@code "iso_offset_date_time"}), or {@code null} to use the default format
	 * @param locale
	 *            the BCP-47 language tag or {@code null} to use the system default locale
	 * @return a non-{@code null} formatted string representation of this date
	 * @see GamaDateType#getFormatter(String, String)
	 */
	@Override
	public String toString(final String string, final String locale) {
		return GamaDateType.getFormatter(string, locale).format(this);
	}

	/**
	 * Determines whether this date is chronologically after the given date. The comparison is performed on the
	 * {@link LocalDateTime} projections of both dates, ignoring any time-zone information. When {@code strict} is
	 * {@code true}, equality does not satisfy the condition; when {@code false}, a date equal to {@code date2} is also
	 * considered greater-than-or-equal.
	 *
	 * <pre>{@code
	 * boolean strictlyAfter = date1.isGreaterThan(date2, true); // date1 > date2
	 * boolean afterOrEqual = date1.isGreaterThan(date2, false); // date1 >= date2
	 * }</pre>
	 *
	 * @param date2
	 *            the date to compare to; must not be {@code null}
	 * @param strict
	 *            if {@code true}, returns {@code true} only when this date is strictly after {@code date2}; if
	 *            {@code false}, returns {@code true} when this date is after or equal to {@code date2}
	 * @return {@code true} if this date is greater than (or, when non-strict, equal to) {@code date2}; {@code false}
	 *         otherwise
	 */
	@Override
	public boolean isGreaterThan(final IDate date2, final boolean strict) {
		final boolean greater = getLocalDateTime().isAfter(date2.getLocalDateTime());
		return strict ? greater : greater || equals(date2);
	}

	/**
	 * Determines whether this date is chronologically before the given date. The comparison is performed on the
	 * {@link LocalDateTime} projections of both dates, ignoring any time-zone information. When {@code strict} is
	 * {@code true}, equality does not satisfy the condition; when {@code false}, a date equal to {@code date2} is also
	 * considered smaller-than-or-equal.
	 *
	 * <pre>{@code
	 * boolean strictlyBefore = date1.isSmallerThan(date2, true); // date1 < date2
	 * boolean beforeOrEqual = date1.isSmallerThan(date2, false); // date1 <= date2
	 * }</pre>
	 *
	 * @param date2
	 *            the date to compare to; must not be {@code null}
	 * @param strict
	 *            if {@code true}, returns {@code true} only when this date is strictly before {@code date2}; if
	 *            {@code false}, returns {@code true} when this date is before or equal to {@code date2}
	 * @return {@code true} if this date is smaller than (or, when non-strict, equal to) {@code date2}; {@code false}
	 *         otherwise
	 */
	@Override
	public boolean isSmallerThan(final IDate date2, final boolean strict) {
		final boolean smaller = getLocalDateTime().isBefore(date2.getLocalDateTime());
		return strict ? smaller : smaller || equals(date2);
	}

	/**
	 * Tests whether this {@code GamaDate} is equal to the given object. Two {@code GamaDate} instances are considered
	 * equal if and only if their {@link LocalDateTime} projections (obtained via {@link #getLocalDateTime()}) are
	 * equal, thereby ignoring any time-zone or offset differences. Non-{@code GamaDate} objects always return
	 * {@code false}.
	 *
	 * @param o
	 *            the object to test for equality; may be {@code null}
	 * @return {@code true} if {@code o} is a {@code GamaDate} whose local date-time is equal to this instance's local
	 *         date-time; {@code false} otherwise
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof GamaDate gd) {
			Temporal a = getLocalDateTime();
			Temporal b = gd.getLocalDateTime();
			return a.equals(b);
		}
		return false;
	}

	/**
	 * Returns a hash code for this {@code GamaDate} based on the underlying {@link Temporal} object. The hash code is
	 * derived from {@link Temporal#hashCode()} of the {@link #internal} component, ensuring consistency with
	 * {@link #equals(Object)}.
	 *
	 * @return the hash code of the underlying {@link Temporal} value
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		return internal.hashCode();
	}

	/**
	 * Returns a new {@link IDate} with the specified double-precision duration added according to the given temporal
	 * unit. The {@code duration} value is truncated to a {@code long} before the addition, so any fractional part is
	 * silently discarded. Delegates to {@link #plus(long, TemporalUnit)}.
	 *
	 * <pre>{@code
	 * IDate result = date.plus(1.9, ChronoUnit.DAYS); // adds 1 full day, 0.9 is discarded
	 * }</pre>
	 *
	 * @param duration
	 *            the amount to add, expressed in {@code unit}; the fractional part is truncated
	 * @param unit
	 *            the unit of the amount to add; must not be {@code null} and must be supported
	 * @return a new, non-{@code null} {@link IDate} with the truncated duration added
	 * @throws java.time.DateTimeException
	 *             if the addition is not possible or produces an out-of-range value
	 * @see #plus(long, TemporalUnit)
	 */
	@Override
	public IDate plus(final double duration, final TemporalUnit unit) {
		return plus((long) duration, unit);
	}

	/**
	 * Returns a new {@link IDate} with the specified {@link TemporalAmount} added (e.g. a {@link java.time.Duration} or
	 * {@link java.time.Period}). The addition is delegated to {@link Temporal#plus(TemporalAmount)} on the underlying
	 * temporal, and the result is wrapped in a new {@code GamaDate} instance. This instance is not modified.
	 *
	 * <pre>{@code
	 * IDate nextWeek = date.plus(Duration.ofDays(7));
	 * IDate nextYear = date.plus(Period.ofYears(1));
	 * }</pre>
	 *
	 * @param amount
	 *            the temporal amount to add; must not be {@code null}
	 * @return a new, non-{@code null} {@link IDate} with the amount added
	 * @throws java.time.DateTimeException
	 *             if the addition is not possible or produces an out-of-range value
	 */
	@Override
	public IDate plus(final TemporalAmount amount) {
		return GamaDateFactory.createFromTemporal(internal.plus(amount));
	}

	/**
	 * Returns a new {@link IDate} with the specified {@link TemporalAmount} subtracted (e.g. a
	 * {@link java.time.Duration} or {@link java.time.Period}). The subtraction is delegated to
	 * {@link Temporal#minus(TemporalAmount)} on the underlying temporal, and the result is wrapped in a new
	 * {@code GamaDate} instance. This instance is not modified.
	 *
	 * <pre>{@code
	 * IDate lastWeek = date.minus(Duration.ofDays(7));
	 * IDate lastYear = date.minus(Period.ofYears(1));
	 * }</pre>
	 *
	 * @param amount
	 *            the temporal amount to subtract; must not be {@code null}
	 * @return a new, non-{@code null} {@link IDate} with the amount subtracted
	 * @throws java.time.DateTimeException
	 *             if the subtraction is not possible or produces an out-of-range value
	 */
	@Override
	public IDate minus(final TemporalAmount amount) {
		return GamaDateFactory.createFromTemporal(internal.minus(amount));
	}

	/**
	 * Returns a new {@link IDate} with the specified number of milliseconds added. The {@code duration} value is
	 * truncated to a {@code long} before the addition, discarding any sub-millisecond fractional part. Delegates to
	 * {@link #plus(long, TemporalUnit)} with {@link ChronoUnit#MILLIS}.
	 *
	 * <pre>{@code
	 * IDate later = date.plusMillis(3600000.0); // add 1 hour (3600000 ms)
	 * }</pre>
	 *
	 * @param duration
	 *            the number of milliseconds to add; the fractional part is truncated; may be negative
	 * @return a new, non-{@code null} {@link IDate} with the truncated millisecond duration added
	 * @see #plus(long, TemporalUnit)
	 */
	@Override
	public IDate plusMillis(final double duration) {
		return plus((long) duration, ChronoUnit.MILLIS);
	}

	/**
	 * Returns a new {@link IDate} with the specified number of milliseconds subtracted. The {@code duration} value is
	 * truncated to a {@code long} before the subtraction, discarding any sub-millisecond fractional part. Delegates to
	 * {@link #minus(long, TemporalUnit)} with {@link ChronoUnit#MILLIS}.
	 *
	 * <pre>{@code
	 * IDate earlier = date.minusMillis(3600000.0); // subtract 1 hour (3600000 ms)
	 * }</pre>
	 *
	 * @param duration
	 *            the number of milliseconds to subtract; the fractional part is truncated; may be negative
	 * @return a new, non-{@code null} {@link IDate} with the truncated millisecond duration subtracted
	 * @see #minus(long, TemporalUnit)
	 */
	@Override
	public IDate minusMillis(final double duration) {
		return minus((long) duration, ChronoUnit.MILLIS);
	}

	/**
	 * Determines whether this date represents an interval boundary that has been reached or crossed by the simulation
	 * clock during the current simulation step. This method implements a general-purpose, iterative interval-checking
	 * algorithm:
	 * <ol>
	 * <li>If the current simulation date exactly equals this date, the interval is reached.</li>
	 * <li>If this date is still in the future relative to the current date, the interval has not yet been reached.</li>
	 * <li>Starting from this date, the next occurrence is computed by repeatedly adding {@code period} until the
	 * resulting date is no longer before the current date.</li>
	 * <li>The interval is considered reached if the next occurrence falls within the time window
	 * {@code [current, current + stepInMillis)}.</li>
	 * </ol>
	 *
	 * <p>
	 * Note: this method evaluates the {@code period} expression on every iteration of the loop and for every call. For
	 * performance-sensitive contexts with simple numeric periods, consider using
	 * {@link #isIntervalReachedOptimized(IScope, IExpression)} instead.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope, used to obtain the clock, the current date, and the simulation step; must
	 *            not be {@code null}
	 * @param period
	 *            an expression that evaluates to the period (in seconds as a {@code float}) between two consecutive
	 *            occurrences; must not be {@code null}
	 * @return {@code true} if the current simulation date coincides with or has just passed one of the recurrences of
	 *         this interval date; {@code false} otherwise
	 * @see #isIntervalReachedOptimized(IScope, IExpression)
	 */
	@Override
	public boolean isIntervalReached(final IScope scope, final IExpression period) {
		// We get the current date from the model
		final IDate current = scope.getClock().getCurrentDate();
		// Exact date ?
		if (this.equals(current)) return true;
		// Not yet reached ?
		if (isGreaterThan(current, true)) return false;

		// Check whether the period uses calendar-based units (months or years)
		final ChronoUnit calUnit = period.getCalendarChronoUnit();
		if (calUnit != null) {
			// For calendar-based periods, compute N (e.g. 2 for "every 2 months") and use
			// proper calendar arithmetic so that month/year lengths are respected.
			final long N = calendarN(scope, period, current, calUnit);
			IDate candidateDate = plus(N, calUnit);
			// Null period?
			if (this.equals(candidateDate)) return false;
			// Exactly reached?
			if (candidateDate.equals(current)) return true;
			while (candidateDate.isSmallerThan(current, true)) { candidateDate = candidateDate.plus(N, calUnit); }
			final long stepInMillis = scope.getClock().getStepInMillis();
			final IDate nextByStep = current.plus(stepInMillis, ChronoUnit.MILLIS);
			return nextByStep.isGreaterThan(candidateDate, true);
		}

		IDate nextByPeriod = plus(scope, period);
		// Null period ?
		if (this.equals(nextByPeriod)) return false;
		// Exactly reached ?
		if (nextByPeriod.equals(current)) return true;
		while (nextByPeriod.isSmallerThan(current, true)) { nextByPeriod = nextByPeriod.plus(scope, period); }

		final long stepInMillis = scope.getClock().getStepInMillis();
		final IDate nextByStep = current.plus(stepInMillis, ChronoUnit.MILLIS);

		return nextByStep.isGreaterThan(nextByPeriod, true);

	}

	/**
	 * A performance-optimized variant of {@link #isIntervalReached(IScope, IExpression)} that avoids iterative date
	 * arithmetic by using modular arithmetic on millisecond timestamps. This method is suitable for constant or numeric
	 * period expressions and significantly reduces computational overhead for frequent checks.
	 *
	 * <p>
	 * The algorithm proceeds as follows:
	 * </p>
	 * <ol>
	 * <li>If the current simulation date exactly equals this date, return {@code true}.</li>
	 * <li>If this date is still in the future, return {@code false}.</li>
	 * <li>Convert the simulation time step and the evaluated period to milliseconds.</li>
	 * <li>If the step is greater than or equal to the period, every step satisfies the interval, so return
	 * {@code true}.</li>
	 * <li>Compute {@code r = sinceBeginning % period} (the millisecond offset within the current period).</li>
	 * <li>If {@code r == 0}, the boundary is hit exactly; return {@code true}.</li>
	 * <li>Otherwise return {@code true} if the current offset is close enough to the period boundary such that it falls
	 * within one step: {@code r - tStep > 0 && r + tStep > periodToMilliSecond}.</li>
	 * </ol>
	 *
	 * @param scope
	 *            the current execution scope, used to obtain the simulation agent, its starting date, and current date;
	 *            must not be {@code null}
	 * @param period
	 *            an expression that evaluates to the period (in seconds as a {@code float}) between consecutive
	 *            occurrences; must not be {@code null}
	 * @return {@code true} if the current simulation time coincides with or straddles a period boundary; {@code false}
	 *         otherwise
	 * @see #isIntervalReached(IScope, IExpression)
	 */
	@Override
	public boolean isIntervalReachedOptimized(final IScope scope, final IExpression period) {
		// We get the current date from the model
		final IDate current = scope.getClock().getCurrentDate();
		// Exact date ?
		if (this.equals(current)) return true;

		// Not yet reached ?
		if (isGreaterThan(current, true)) return false;

		// Check whether the period uses calendar-based units (months or years)
		final ChronoUnit calUnit = period.getCalendarChronoUnit();
		if (calUnit != null) {
			// For calendar-based periods avoid modular arithmetic (which drifts because month/year
			// lengths vary). Instead, anchor every period boundary to this starting date using
			// proper calendar arithmetic, then check whether the current date falls inside the
			// trigger window [lastExpectedFiringDate, lastExpectedFiringDate + step).
			final long N = calendarN(scope, period, current, calUnit);
			final long unitsBetween = calUnit.between(getLocalDateTime(), current.getLocalDateTime());
			final long k = unitsBetween / N;
			final IDate lastExpectedFiringDate = this.plus(k * N, calUnit);
			final long gapMillis = ChronoUnit.MILLIS.between(lastExpectedFiringDate.getLocalDateTime(), current.getLocalDateTime());
			final long tStepMillis = scope.getClock().getStepInMillis();
			return gapMillis >= 0 && gapMillis < tStepMillis;
		}

		long tStep = (long) (scope.getSimulation().getTimeStep(scope) * 1000);
		long periodToMilliSecond = (long) (Cast.asFloat(scope, period.value(scope)) * 1000);
		if (tStep >= periodToMilliSecond) return true;
		long sinceBeginning = scope.getSimulation().getStartingDate().until(scope.getSimulation().getCurrentDate(),
				ChronoUnit.MILLIS);
		long r = sinceBeginning % periodToMilliSecond;
		if (r == 0) return true;
		return r - tStep > 0 && r + tStep > periodToMilliSecond;

	}

	/**
	 * Computes the integer multiplier {@code N} for a calendar-based {@code every} period such as {@code 2#months} or
	 * {@code 3#years}. The multiplier is derived by dividing the evaluated period (in seconds) by the length of a
	 * single calendar unit at the current date.
	 *
	 * <p>
	 * Because {@link gama.api.gaml.constants.GamlCoreUnits#month} and
	 * {@link gama.api.gaml.constants.GamlCoreUnits#year} evaluate to the actual length of the current month/year, the
	 * division {@code periodSecs / unitSecs} always yields the original integer multiplier (e.g. {@code 2} for
	 * {@code 2#months}).
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param period
	 *            the period expression (e.g. {@code 2#months})
	 * @param current
	 *            the current simulation date (used to measure one unit's length)
	 * @param calUnit
	 *            {@link ChronoUnit#MONTHS} or {@link ChronoUnit#YEARS}
	 * @return the positive integer multiplier N ≥ 1
	 */
	private static long calendarN(final IScope scope, final IExpression period, final IDate current,
			final ChronoUnit calUnit) {
		final double periodSecs = Cast.asFloat(scope, period.value(scope));
		final double unitSecs = ChronoUnit.SECONDS.between(current.getLocalDateTime(),
				current.plus(1L, calUnit).getLocalDateTime());
		return Math.max(1L, Math.round(periodSecs / unitSecs));
	}

	/**
	 * Returns a new {@link IDate} obtained by advancing this date by the amount of time that the given GAML expression
	 * evaluates to. The expression is expected to produce a numeric value representing a duration in seconds; this
	 * value is converted to milliseconds (multiplied by 1000) and truncated to a {@code long} before the addition. If
	 * the resulting period is zero, this instance is returned unchanged.
	 *
	 * <pre>{@code
	 * // In GAML, equivalent to: date + period
	 * IDate next = startDate.plus(scope, periodExpression);
	 * }</pre>
	 *
	 * @param scope
	 *            the current execution scope used to evaluate the expression; must not be {@code null}
	 * @param period
	 *            a GAML expression evaluating to a duration in seconds as a {@code float}; must not be {@code null}
	 * @return this instance if the evaluated period is zero; otherwise a new, non-{@code null} {@link IDate} advanced
	 *         by the evaluated period
	 * @see #plus(long, TemporalUnit)
	 */
	@Override
	public IDate plus(final IScope scope, final IExpression period) {
		final long p = (long) (Cast.asFloat(scope, period.value(scope)) * 1000);
		if (p == 0) return this;
		return plus(p, ChronoUnit.MILLIS);
	}

	/**
	 * Returns a new {@link IDate} obtained by adding {@code period} in the given {@link ChronoUnit} exactly
	 * {@code repeat} times in succession. Each addition is performed on the result of the previous one, starting from
	 * this date. The {@code period} is truncated to a {@code long} at each step via
	 * {@link #plus(double, TemporalUnit)}. If {@code repeat} is zero, this instance is returned unchanged.
	 *
	 * <pre>{@code
	 * // Add 6 months (3 times × 2 months):
	 * IDate result = date.plus(2.0, 3, ChronoUnit.MONTHS);
	 * }</pre>
	 *
	 * @param period
	 *            the amount to add per iteration, expressed in {@code unit}; the fractional part is truncated at each
	 *            step
	 * @param repeat
	 *            the number of times to apply the addition; if {@code 0} or negative, no addition is performed
	 * @param unit
	 *            the {@link ChronoUnit} of {@code period}; must not be {@code null} and must be supported
	 * @return a new, non-{@code null} {@link IDate} with {@code period × repeat} added, or this instance if
	 *         {@code repeat} is zero or negative
	 * @throws java.time.DateTimeException
	 *             if any individual addition fails
	 * @see #plus(double, TemporalUnit)
	 */
	@Override
	public IDate plus(final double period, final int repeat, final ChronoUnit unit) {
		IDate result = this;
		for (int i = 0; i < repeat; i++) { result = result.plus(period, unit); }
		return result;
	}

	/**
	 * Compares this date with the specified {@link IDate} for chronological ordering. The comparison is based on the
	 * {@link LocalDateTime} projections of both dates (timezone information is ignored). Returns a negative integer,
	 * zero, or a positive integer as this date is earlier than, equal to, or later than the given date.
	 *
	 * @param o
	 *            the date to compare to; must not be {@code null}
	 * @return {@code -1} if this date is earlier than {@code o}, {@code 1} if this date is later than {@code o}, or
	 *         {@code 0} if both dates represent the same local date-time
	 * @see #isSmallerThan(IDate, boolean)
	 * @see #isGreaterThan(IDate, boolean)
	 */
	@Override
	public int compareTo(final IDate o) {
		return isSmallerThan(o, true) ? -1 : isGreaterThan(o, true) ? 1 : 0;
	}

	/**
	 * Returns {@code true} if this date is strictly before the specified date. Delegates to
	 * {@link #isSmallerThan(IDate, boolean)} with {@code strict = true}, meaning that a date equal to
	 * {@code startInclusive} is <em>not</em> considered to be before it.
	 *
	 * <pre>{@code
	 * boolean before = date1.isBefore(date2); // equivalent to date1 < date2
	 * }</pre>
	 *
	 * @param startInclusive
	 *            the date to compare to; must not be {@code null}
	 * @return {@code true} if this date is strictly earlier than {@code startInclusive}; {@code false} otherwise
	 * @see #isSmallerThan(IDate, boolean)
	 */
	@Override
	public boolean isBefore(final IDate startInclusive) {
		return isSmallerThan(startInclusive, true);
	}

	/**
	 * Returns {@code true} if this date is strictly after the specified date. Delegates to
	 * {@link #isGreaterThan(IDate, boolean)} with {@code strict = true}, meaning that a date equal to
	 * {@code startInclusive} is <em>not</em> considered to be after it.
	 *
	 * <pre>{@code
	 * boolean after = date1.isAfter(date2); // equivalent to date1 > date2
	 * }</pre>
	 *
	 * @param startInclusive
	 *            the date to compare to; must not be {@code null}
	 * @return {@code true} if this date is strictly later than {@code startInclusive}; {@code false} otherwise
	 * @see #isGreaterThan(IDate, boolean)
	 */
	@Override
	public boolean isAfter(final IDate startInclusive) {
		return isGreaterThan(startInclusive, true);
	}

	/**
	 * Returns this date formatted as an ISO-8601 string with UTC offset (e.g. {@code "2024-03-15T14:30:00+01:00"}).
	 * Delegates to {@link #toString(String, String)} using {@link GamlCoreUnits#ISO_OFFSET_KEY} as the format key and
	 * {@code null} as the locale. The resulting string can be parsed back unambiguously by standard ISO-8601 parsers.
	 *
	 * <pre>{@code
	 * String iso = date.toISOString();
	 * // e.g. "2024-03-15T14:30:00+01:00"
	 * }</pre>
	 *
	 * @return a non-{@code null} ISO-8601 formatted string representation of this date including the UTC offset
	 * @see GamlCoreUnits#ISO_OFFSET_KEY
	 * @see #toString(String, String)
	 */
	@Override
	public String toISOString() {
		return toString(GamlCoreUnits.ISO_OFFSET_KEY, null);
	}

	/**
	 * Serializes this date to a JSON representation using the supplied {@link IJson} factory. The resulting JSON object
	 * is a typed object whose GAML type is the type of this date and whose {@code "iso"} field contains the ISO-8601
	 * string produced by {@link #toISOString()}.
	 *
	 * <pre>{@code
	 * // Produces: {"gaml_type": "date", "iso": "2024-03-15T14:30:00+01:00"}
	 * IJsonValue json = date.serializeToJson(jsonFactory);
	 * }</pre>
	 *
	 * @param json
	 *            the {@link IJson} factory used to construct the JSON value; must not be {@code null}
	 * @return a non-{@code null} {@link IJsonValue} representing this date as a typed JSON object with an {@code "iso"}
	 *         field
	 * @see #toISOString()
	 * @see IJson#typedObject(IType, Object...)
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "iso", toISOString());
	}

}
