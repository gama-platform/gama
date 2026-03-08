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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.constants.GamlCoreUnits;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaDateType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IContainer;

/**
 * A static factory for creating and manipulating {@link IDate} instances. This class handles date and time related
 * operations, including creation from various formats, parsing strings, and providing time constants. It delegates the
 * actual creation to an {@link IDateFactory} implementation.
 */
public class GamaDateFactory {

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
		return new GamaDate(null, isoString);
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
	public static IDate createFromIDate(final IScope scope, final IDate date) {
		return new GamaDate(scope, date);
	}

	/**
	 * Creates a new {@link IDate} from a Java {@link Temporal} object.
	 *
	 * @param temporal
	 *            the temporal object (e.g., LocalDateTime, Instant).
	 * @return the corresponding {@link IDate} instance.
	 */
	public static IDate createFromTemporal(final Temporal temporal) {
		return new GamaDate(temporal);
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
	public static IDate createFromContainer(final IScope scope, final IContainer<?, ?> c) {
		return new GamaDate(scope, c.listValue(scope, Types.INT, false));
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
		return new GamaDate(scope, s);
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
		return new GamaDate(scope, d);
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
		return new GamaDate(scope, value, pattern, locale);
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
		return new GamaDate(scope, value, pattern);
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

}