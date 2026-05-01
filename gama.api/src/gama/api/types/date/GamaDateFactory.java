/*******************************************************************************************************
 *
 * GamaDateFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.date;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import org.apache.commons.lang3.StringUtils;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.constants.GamlCoreUnits;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaDateType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;

/**
 * A static factory for creating and manipulating {@link IDate} instances. This class handles date and time related
 * operations, including creation from various formats, parsing strings, and providing time constants. It delegates the
 * actual creation to an {@link IDateFactory} implementation.
 */
public class GamaDateFactory {

	/** The Constant THE_DATE. */
	private static final String THE_DATE = "The date ";

	/**
	 * The default time zone (ID) of the system.
	 */
	public static final ZoneId DEFAULT_ZONE = Clock.systemDefaultZone().getZone();

	/**
	 * The default time zone offset in seconds from UTC.
	 */
	public static final ZoneOffset DEFAULT_OFFSET_IN_SECONDS =
			Clock.systemDefaultZone().getZone().getRules().getOffset(Instant.now(Clock.systemDefaultZone()));

	/**
	 * The epoch date (1970-01-01T00:00:00Z), adjusted to the default system offset if necessary.
	 */
	public static IDate EPOCH = createFromTemporal(LocalDateTime.ofEpochSecond(0, 0, DEFAULT_OFFSET_IN_SECONDS));

	/**
	 * Creates a new {@link IDate} from an ISO-8601 formatted string.
	 *
	 * @param isoString
	 *            the string containing the date in ISO format.
	 * @return the corresponding {@link IDate} instance.
	 */
	public static IDate createFromISOString(final String isoString) {
		try {
			final TemporalAccessor t = GamaDateType.getFormatter(GamlCoreUnits.ISO_OFFSET_KEY, null).parse(isoString);
			if (t instanceof Temporal tmp) return createFromTemporal(tmp);
		} catch (final DateTimeParseException e) {
			//
		}
		return createFromString(null, isoString);
	}

	/**
	 * Creates a copy of an existing {@link IDate}.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param date
	 *            the source {@link IDate}.
	 * @return a new {@link IDate} instance identical to the source.
	 */
	public static IDate createFromIDate(final IScope scope, final IDate other) {
		return createFromTemporal(scope, LocalDateTime.from(other));
	}

	/**
	 * Creates a new {@link IDate} from a Java {@link Temporal} object.
	 *
	 * @param temporal
	 *            the temporal object (e.g., LocalDateTime, Instant).
	 * @return the corresponding {@link IDate} instance.
	 */
	public static IDate createFromTemporal(final Temporal temporal) {
		return createFromTemporal(null, temporal);
	}

	/**
	 * Creates a new GamaDate object.
	 *
	 * @param scope
	 *            the scope
	 * @param temporal
	 *            the temporal
	 * @return the i date
	 */
	public static IDate createFromTemporal(final IScope scope, final Temporal d) {
		final ZoneId zone;
		Temporal internal;
		if (d instanceof ChronoZonedDateTime) {
			zone = ZonedDateTime.from(d).getZone();
		} else if (d.isSupported(ChronoField.OFFSET_SECONDS)) {
			zone = ZoneId.ofOffset("", ZoneOffset.ofTotalSeconds(d.get(ChronoField.OFFSET_SECONDS)));
		} else {
			zone = GamaDateFactory.DEFAULT_ZONE;
		}
		if (!d.isSupported(MINUTE_OF_HOUR)) {
			internal = ZonedDateTime.of(LocalDate.from(d), LocalTime.of(0, 0), zone);
		} else if (!d.isSupported(DAY_OF_MONTH)) {
			internal = ZonedDateTime.of(
					LocalDate.from(scope == null || scope.getSimulation() == null
							? GamaDateType.DATES_STARTING_DATE.getValue() : scope.getSimulation().getStartingDate()),
					LocalTime.from(d), zone);
		} else {
			internal = d;
		}
		return new GamaDate(internal);
	}

	/**
	 * Creates a new {@link IDate} from a container (e.g., a list or map of date components).
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param c
	 *            the container holding date information.
	 * @return the corresponding {@link IDate} instance.
	 */
	public static IDate createFromContainer(final IScope scope, final IContainer<?, ?> container) {
		// Check if this is a list with [dateString, pattern] or [dateString, pattern, locale]
		final var list = container.listValue(scope, Types.NO_TYPE, false);
		final int size = list.size();
		if (size >= 2 && size <= 3) {
			final Object first = list.get(0);
			final Object second = list.get(1);
			// If the second element is a string (pattern), treat as date parsing with pattern
			if (second instanceof String pattern) {
				final String dateStr = Cast.asString(scope, first);
				if (size == 3) {
					final String locale = Cast.asString(scope, list.get(2));
					return createWith(scope, dateStr, pattern, locale);
				}
				return createWith(scope, dateStr, pattern);
			}
		}
		// Otherwise, treat as [year, month, day, hour, minute, second]
		// return new GamaDate(scope, container.listValue(scope, Types.INT, false));
		return createFromTemporal(scope, computeFromList(scope, container.listValue(scope, Types.INT, false)));
	}

	/**
	 * Creates a new {@link IDate} by parsing a generic string representation.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param s
	 *            the string to parse.
	 * @return the parsed {@link IDate} instance.
	 */
	public static IDate createFromString(final IScope scope, final String s) {
		return createFromTemporal(scope, parse(scope, s, null));
	}

	/**
	 * Creates a new {@link IDate} from a double value representing seconds/duration.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param d
	 *            the time value as a double (typically seconds).
	 * @return the corresponding {@link IDate} instance.
	 */
	public static IDate createFromDouble(final IScope scope, final Double d) {
		return createFromTemporal(scope,
				scope.getSimulation().getStartingDate().plus((long) (d * 1000), ChronoUnit.MILLIS));
	}

	/**
	 * Creates a new {@link IDate} from a string using a specific pattern and locale.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param value
	 *            the date string to parse.
	 * @param pattern
	 *            the format pattern (e.g., "yyyy-MM-dd").
	 * @param locale
	 *            the locale string (e.g., "en_US").
	 * @return the parsed {@link IDate} instance.
	 */
	public static IDate createWith(final IScope scope, final String value, final String pattern, final String locale) {
		return createFromTemporal(scope, parse(scope, value, GamaDateType.getFormatter(pattern, locale)));
	}

	/**
	 * Creates a new {@link IDate} from a string using a specific pattern.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param value
	 *            the date string to parse.
	 * @param pattern
	 *            the format pattern.
	 * @return the parsed {@link IDate} instance.
	 */
	public static IDate createWith(final IScope scope, final String value, final String pattern) {
		return createWith(scope, value, pattern, null);
	}

	/**
	 * Converts an arbitrary object into an {@link IDate}.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param obj
	 *            the object to convert (e.g., String, IDate, Container, Double).
	 * @param param
	 *            optional parameter (not currently used in this implementation).
	 * @param copy
	 *            whether to create a copy if the object is already an IDate.
	 * @return the resulting {@link IDate} instance.
	 * @throws GamaRuntimeException
	 *             if the object cannot be converted to a date.
	 */
	public static IDate castToDate(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return switch (obj) {
			case null -> null;
			case IDate d -> copy ? createFromIDate(scope, d) : d;
			case IContainer c -> createFromContainer(scope, c);
			case String s -> createFromString(scope, s);
			// If everything fails, we assume it is a duration in seconds since the starting date of the model
			default -> createFromDouble(scope, Cast.asFloat(scope, obj));
		};
	}

	/**
	 * Convenience method to convert an object to an {@link IDate}.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param obj
	 *            the object to convert.
	 * @return the resulting {@link IDate} instance.
	 * @throws GamaRuntimeException
	 *             if the object cannot be converted.
	 */
	public static IDate castToDate(final IScope scope, final Object obj) throws GamaRuntimeException {
		return castToDate(scope, obj, null, false);
	}

	/**
	 * @return
	 */
	public static IDate now() {
		return createFromTemporal(LocalDateTime.now());
	}

	/**
	 * Compute from list.
	 *
	 * @param scope
	 *            the scope
	 * @param vals
	 *            the vals
	 * @return the local date time
	 */
	private static LocalDateTime computeFromList(final IScope scope, final IList<?> vals) {
		int year = 0;
		int month = 1;
		int day = 1;
		int hour = 0;
		int minute = 0;
		int second = 0;
		final int size = vals.size();
		if (size > 0) {
			year = Cast.asInt(scope, vals.get(0));
			if (size > 1) {
				month = Cast.asInt(scope, vals.get(1));
				if (size > 2) {
					day = Cast.asInt(scope, vals.get(2));
					if (size > 3) {
						hour = Cast.asInt(scope, vals.get(3));
						if (size > 4) {
							minute = Cast.asInt(scope, vals.get(4));
							if (size > 5) { second = Cast.asInt(scope, vals.get(5)); }
						}
					}
				}
			}
		}
		return LocalDateTime.of(year, month, day, hour, minute, second);
	}

	/**
	 * Parses the.
	 *
	 * @param scope
	 *            the scope
	 * @param original
	 *            the original
	 * @param df
	 *            the df
	 * @return the temporal
	 */
	private static Temporal parse(final IScope scope, final String original, final DateTimeFormatter df) {
		if (original == null || original.isEmpty() || "now".equals(original))
			return LocalDateTime.now(GamaDateFactory.DEFAULT_ZONE);
		Temporal result = null;

		if (df != null) {
			try {
				final TemporalAccessor ta = df.parse(original);
				if (ta instanceof Temporal tmp) return tmp;
				if (!ta.isSupported(ChronoField.YEAR) && !ta.isSupported(ChronoField.MONTH_OF_YEAR)
						&& !ta.isSupported(ChronoField.DAY_OF_MONTH) && ta.isSupported(ChronoField.HOUR_OF_DAY))
					return LocalTime.from(ta);
				if (!ta.isSupported(ChronoField.HOUR_OF_DAY) && !ta.isSupported(ChronoField.MINUTE_OF_HOUR)
						&& !ta.isSupported(ChronoField.SECOND_OF_MINUTE))
					return LocalDate.from(ta);
				return LocalDateTime.from(ta);
			} catch (final DateTimeParseException e) {
				e.printStackTrace();
			}
			GAMA.reportAndThrowIfNeeded(scope,
					GamaRuntimeException.warning(
							THE_DATE + original + " can not correctly be parsed by the pattern provided", scope),
					false);
			return parse(scope, original, null);
		}

		String dateStr;
		try {
			// We first make sure all date fields have the correct length and
			// the string is correctly formatted
			String string = original;
			if (!original.contains("T") && original.contains(" ")) {
				string = StringUtils.replaceOnce(original, " ", "T");
			}
			final String[] base = string.split("T");
			final String[] date = base[0].split("-");
			String other;
			if (base.length == 1) {
				other = "00:00:00";
			} else {
				other = base[1];
			}
			String year, month, day;
			if (date.length == 1) {
				// ISO basic date format
				year = date[0].substring(0, 4);
				month = date[0].substring(4, 6);
				day = date[0].substring(6, 8);
			} else if (date.length >= 4 && date[0].isEmpty()) {
				// Negative year: "-1000-01-01" splits to ["", "1000", "01", "01"]
				year = "-" + date[1];
				month = date[2];
				day = date[3];
			} else {
				year = date[0];
				month = date[1];
				day = date[2];
			}
			if (year.length() == 2 && !year.startsWith("-")) { year = "20" + year; }
			if (month.length() == 1) { month = '0' + month; }
			if (day.length() == 1) { day = '0' + day; }
			dateStr = year + "-" + month + "-" + day + "T" + other;
		} catch (final Exception e1) {
			throw GamaRuntimeException.error(
					THE_DATE + original + " is not correctly formatted. Please refer to the ISO date/time format",
					scope);
		}

		try {
			result = LocalDateTime.parse(dateStr);
		} catch (final DateTimeParseException e) {
			try {
				result = OffsetDateTime.parse(dateStr);
			} catch (final DateTimeParseException e2) {
				try {
					result = ZonedDateTime.parse(dateStr);
				} catch (final DateTimeParseException e3) {
					throw GamaRuntimeException.error(THE_DATE + original
							+ " is not correctly formatted. Please refer to the ISO date/time format", scope);
				}
			}
		}

		return result;
	}

}