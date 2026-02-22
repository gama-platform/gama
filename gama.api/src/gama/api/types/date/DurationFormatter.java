/**
 *
 */
package gama.api.types.date;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;

import gama.api.gaml.types.GamaDateType;

/**
 * Formats {@link Duration} objects as human-readable strings with intelligent unit selection.
 * 
 * <p>
 * This formatter automatically selects the most appropriate time units to display a duration,
 * ranging from years and months down to seconds. The output format adapts based on the magnitude
 * of the duration, ensuring readability while maintaining precision.
 * </p>
 * 
 * <h2>Formatting Rules</h2>
 * <ul>
 * <li>If duration includes years: displays as "Xy Mm Dd HH:mm:ss"</li>
 * <li>If duration includes 2+ months: displays as "M months d days HH:mm:ss"</li>
 * <li>If duration includes 1 month: displays as "M month d day(s) HH:mm:ss"</li>
 * <li>If duration includes 2+ days: displays as "d days HH:mm:ss"</li>
 * <li>If duration includes 1 day: displays as "d day HH:mm:ss"</li>
 * <li>Otherwise: displays as "HH:mm:ss"</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Format a duration
 * Duration d1 = Duration.ofHours(25);
 * String s1 = DurationFormatter.INSTANCE.toString(d1);
 * // Result: "1 day 01:00:00"
 * 
 * Duration d2 = Duration.ofDays(45).plusHours(12).plusMinutes(30);
 * String s2 = DurationFormatter.INSTANCE.toString(d2);
 * // Result: "1 month 15 days 12:30:00"
 * 
 * Duration d3 = Duration.ofMinutes(90);
 * String s3 = DurationFormatter.INSTANCE.toString(d3);
 * // Result: "01:30:00"
 * }</pre>
 * 
 * <h2>Implementation Note</h2>
 * <p>
 * This class implements {@link TemporalAccessor} to integrate with Java's date/time formatting
 * system. It converts durations to a temporal representation relative to the GAMA starting date
 * to enable proper formatting with standard DateTimeFormatter patterns.
 * </p>
 * 
 * @see java.time.Duration
 * @see java.time.format.DateTimeFormatter
 * @see gama.api.gaml.types.GamaDateType
 * 
 * @author Alexis Drogoul
 */
public class DurationFormatter implements TemporalAccessor {

	/** The Constant DURATION_FORMATTER. */
	public static final DurationFormatter INSTANCE = new DurationFormatter();

	/** The Constant YMDHMS. */
	private static final DateTimeFormatter YMDHMS = DateTimeFormatter.ofPattern("u'y' M'm' d'd' HH:mm:ss");

	/** The Constant MDHMS. */
	private static final DateTimeFormatter MDHMS = DateTimeFormatter.ofPattern("M' months' d 'days' HH:mm:ss");

	/** The Constant M1DHMS. */
	private static final DateTimeFormatter M1DHMS = DateTimeFormatter.ofPattern("M' month' d 'days' HH:mm:ss");

	/** The Constant M1D1HMS. */
	private static final DateTimeFormatter M1D1HMS = DateTimeFormatter.ofPattern("M' month' d 'day' HH:mm:ss");

	/** The Constant DHMS. */
	private static final DateTimeFormatter DHMS = DateTimeFormatter.ofPattern("d 'days' HH:mm:ss");

	/** The Constant D1HMS. */
	private static final DateTimeFormatter D1HMS = DateTimeFormatter.ofPattern("d 'day' HH:mm:ss");

	/** The Constant HMS. */
	private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");

	/** The temporal. */
	private Temporal temporal;

	/**
	 * Converts a duration to a human-readable string representation.
	 * 
	 * <p>
	 * The method intelligently selects the most appropriate format based on the duration's magnitude.
	 * Longer durations will include years and months, while shorter ones display only days, hours,
	 * minutes, and seconds. Singular vs. plural forms are used appropriately (e.g., "1 day" vs "2 days").
	 * </p>
	 *
	 * @param duration
	 *            the duration to format (must not be null)
	 * @return a human-readable string representation of the duration (e.g., "2 days 12:30:45")
	 * 
	 * @example
	 * <pre>{@code
	 * Duration d = Duration.ofHours(50);
	 * String formatted = DurationFormatter.INSTANCE.toString(d);
	 * // Returns: "2 days 02:00:00"
	 * }</pre>
	 */
	public String toString(final Duration duration) {
		this.temporal = duration.addTo(GamaDateType.DATES_STARTING_DATE.getValue())
				.minus(GamaDateFactory.DEFAULT_OFFSET_IN_SECONDS.getTotalSeconds(), SECONDS);
		// if (duration.toDays() == 0l)
		// temporal = LocalDateTime.(temporal);
		return toString();
	}

	/**
	 * Selects the appropriate DateTimeFormatter based on the duration's magnitude.
	 * 
	 * <p>
	 * The selection logic prioritizes larger units (years, then months, then days) and adjusts
	 * for singular vs. plural forms to produce grammatically correct output.
	 * </p>
	 *
	 * @return the most appropriate DateTimeFormatter for this duration
	 */
	private DateTimeFormatter getFormatter() {
		if (getLong(YEAR) > 0) return YMDHMS;
		final long month = getLong(MONTH_OF_YEAR);
		final long day = getLong(DAY_OF_MONTH);
		if (month > 0) {
			if (month >= 2) return MDHMS;
			if (day < 2) return M1D1HMS;
			return M1DHMS;
		}
		if (day > 0) {
			if (day < 2) return D1HMS;
			return DHMS;
		}
		return HMS;
	}

	@Override
	public boolean isSupported(final TemporalField field) {
		return temporal.isSupported(field);
	}

	@Override
	public long getLong(final TemporalField field) {
		if (field == SECOND_OF_MINUTE) return temporal.getLong(SECOND_OF_MINUTE);
		if (field == MINUTE_OF_HOUR) return temporal.getLong(MINUTE_OF_HOUR);
		if (field == HOUR_OF_DAY) return temporal.getLong(HOUR_OF_DAY);
		if (field == DAY_OF_MONTH) return temporal.getLong(DAY_OF_MONTH) - 1l;
		if (field == MONTH_OF_YEAR) return temporal.getLong(MONTH_OF_YEAR) - 1;
		if (field == YEAR) return temporal.getLong(YEAR) - GamaDateType.DATES_STARTING_DATE.getValue().getLong(YEAR);
		return 0;
	}

	@Override
	public String toString() {
		return getFormatter().format(this);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <R> R query(final TemporalQuery<R> query) {
		if (query == TemporalQueries.precision()) return (R) SECONDS;
		if (query == TemporalQueries.chronology()) return (R) IsoChronology.INSTANCE;
		if (query == TemporalQueries.zone() || query == TemporalQueries.zoneId()) return null;
		return query.queryFrom(this);
	}

}