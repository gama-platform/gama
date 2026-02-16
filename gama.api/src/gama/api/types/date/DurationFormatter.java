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
 * The Class DurationFormatter.
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
	 * To string.
	 *
	 * @param duration
	 *            the duration
	 * @return the string
	 */
	public String toString(final Duration duration) {
		this.temporal = duration.addTo(GamaDateType.DATES_STARTING_DATE.getValue())
				.minus(GamaDateFactory.DEFAULT_OFFSET_IN_SECONDS.getTotalSeconds(), SECONDS);
		// if (duration.toDays() == 0l)
		// temporal = LocalDateTime.(temporal);
		return toString();
	}

	/**
	 * Gets the formatter.
	 *
	 * @return the formatter
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