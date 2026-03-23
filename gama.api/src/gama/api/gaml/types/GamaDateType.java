/*******************************************************************************************************
 *
 * GamaDateType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.constants.GamlCoreUnits;
import gama.api.runtime.scope.IScope;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;
import gama.api.types.map.IMap;
import gama.api.utils.StringUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.Pref;
import gama.dev.DEBUG;

/**
 * Type representing dates and date-times in GAML - temporal values for time-based modeling.
 * <p>
 * The date type provides comprehensive support for temporal modeling in GAMA, including calendar dates, times,
 * durations, and scheduling. It integrates with Java's modern time API (java.time) and supports multiple date formats,
 * time zones, and temporal arithmetic.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Calendar dates with year, month, day, hour, minute, second precision</li>
 * <li>Time zone support (UTC, local, custom)</li>
 * <li>Multiple date format parsers and formatters</li>
 * <li>Temporal arithmetic (add/subtract durations)</li>
 * <li>Integration with simulation time</li>
 * <li>ISO 8601 support for serialization</li>
 * <li>Customizable date formatting patterns</li>
 * </ul>
 * 
 * <h2>Date Formats:</h2>
 * <p>
 * GAMA supports multiple date format patterns for parsing and formatting:
 * <ul>
 * <li>ISO formats (ISO_LOCAL, ISO_OFFSET, ISO_ZONED)</li>
 * <li>Custom patterns using Java DateTimeFormatter syntax</li>
 * <li>Pattern symbols: %Y (year), %M (month), %D (day), %h (hour), %m (minute), %s (second), etc.</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Create from simulation time
 * date current_date <- current_date();
 * 
 * // Create from components
 * date specific_date <- date([2024, 3, 15, 14, 30, 0]);  // March 15, 2024, 14:30:00
 * 
 * // Parse from string
 * date parsed <- date("2024-03-15 14:30:00");
 * 
 * // With custom format
 * date custom <- date("15/03/2024", "%D/%M/%Y");
 * 
 * // Temporal arithmetic
 * date tomorrow <- current_date + 1#day;
 * date next_week <- current_date + 7#days;
 * 
 * // Extract components
 * int year <- my_date.year;
 * int month <- my_date.month;
 * int day <- my_date.day;
 * 
 * // Compare dates
 * bool is_after <- date1 > date2;
 * float time_diff <- date2 - date1;  // Duration in seconds
 * 
 * // Format to string
 * string formatted <- string(my_date, "%Y-%M-%D");
 * }
 * </pre>
 * 
 * <h2>Preferences:</h2>
 * <p>
 * The date type behavior can be customized through preferences:
 * <ul>
 * <li>Starting date - default simulation start date</li>
 * <li>Time step - default simulation time increment</li>
 * <li>Date formatter - pattern for date-to-string conversion</li>
 * <li>Custom formatter - user-defined format pattern</li>
 * </ul>
 * </p>
 * 
 * @author Patrick Tallandier
 * @see GamaType
 * @see gama.api.types.date.IDate
 * @see gama.api.types.date.GamaDateFactory
 * @see java.time.format.DateTimeFormatter
 * @since GAMA 1.6
 */
@SuppressWarnings ("unchecked")
@type (
		name = "date",
		id = IType.DATE,
		wraps = { IDate.class },
		kind = ISymbolKind.NUMBER,
		concept = { IConcept.TYPE, IConcept.DATE, IConcept.TIME },
		doc = { @doc ("GAML objects that represent a date") })
public class GamaDateType extends GamaType<IDate> {

	/** Pattern for model-specific date format with placeholders like %Y, %M, %D, etc. */
	static Pattern model_pattern = Pattern.compile("%[YMNDEhmsz]");

	/**
	 * Preference for the default starting date of models.
	 * <p>
	 * This sets the initial simulation date when a model begins execution. Defaults to Unix epoch (1970-01-01).
	 * </p>
	 */
	public final static Pref<IDate> DATES_STARTING_DATE =
			GamaPreferences.create("pref_date_starting_date", "Default starting date of models", GamaDateFactory.EPOCH,
					IType.DATE, true).in(GamaPreferences.External.NAME, GamaPreferences.External.DATES);

	/**
	 * Preference for the default time step of models.
	 * <p>
	 * This defines the default duration (in seconds) by which simulation time advances each step. Defaults to 1
	 * second.
	 * </p>
	 */
	public final static Pref<Double> DATES_TIME_STEP =
			GamaPreferences.create("pref_date_time_step", "Default time step of models", 1d, IType.FLOAT, true)
					.in(GamaPreferences.External.NAME, GamaPreferences.External.DATES).between(1d, null);

	/**
	 * Registry of date formatters indexed by pattern and locale.
	 * <p>
	 * Caches DateTimeFormatter instances for performance.
	 * </p>
	 */
	public static final HashMap<String, DateTimeFormatter> FORMATTERS = new HashMap<>();

	/** The current default formatter value. */
	public static String DEFAULT_VALUE = "CUSTOM";

	/** Key for accessing the default formatter in the FORMATTERS map. */
	public static final String DEFAULT_KEY = "DEFAULT";

	/** Default date format pattern: "yyyy-MM-dd HH:mm:ss". */
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** ISO simple date format pattern: "yy-MM-dd HH:mm:ss". */
	public static final String ISO_SIMPLE_FORMAT = "yy-MM-dd HH:mm:ss";

	/**
	 * Preference for custom date formatter pattern.
	 * <p>
	 * Allows users to define their own date format pattern using Java DateTimeFormatter syntax.
	 * </p>
	 *
	 * @see <a href=
	 *      "https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns">DateTimeFormatter
	 *      Patterns</a>
	 */
	public final static Pref<String> DATES_CUSTOM_FORMATTER = GamaPreferences.create("pref_date_custom_formatter",
			"Custom date pattern (https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns)",
			DEFAULT_FORMAT, IType.STRING, true).in(GamaPreferences.External.NAME, GamaPreferences.External.DATES)
			.onChange(e -> {
				try {
					FORMATTERS.put(GamlCoreUnits.CUSTOM_KEY, getFormatter(StringUtils.toJavaString(e), null));
					if (GamlCoreUnits.CUSTOM_KEY.equals(DEFAULT_VALUE)) {
						FORMATTERS.put(DEFAULT_KEY, GamaDateType.FORMATTERS.get(GamlCoreUnits.CUSTOM_KEY));
					}
				} catch (
				/** The ex. */
				final Exception ex) {
					DEBUG.ERR("Formatter not valid: " + e);
				}
			});

	/** Static initialization block for standard formatters. */
	static {
		FORMATTERS.put(DEFAULT_KEY, DateTimeFormatter.ofPattern(DEFAULT_FORMAT));
		FORMATTERS.put(GamlCoreUnits.ISO_SIMPLE_KEY, DateTimeFormatter.ofPattern(ISO_SIMPLE_FORMAT));
		FORMATTERS.put(GamlCoreUnits.ISO_LOCAL_KEY, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		FORMATTERS.put(GamlCoreUnits.ISO_OFFSET_KEY, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		FORMATTERS.put(GamlCoreUnits.ISO_ZONED_KEY, DateTimeFormatter.ISO_ZONED_DATE_TIME);
		FORMATTERS.put(GamlCoreUnits.CUSTOM_KEY, DateTimeFormatter.ofPattern(DATES_CUSTOM_FORMATTER.getValue()));
		FORMATTERS.put(DEFAULT_KEY, FORMATTERS.get(GamlCoreUnits.CUSTOM_KEY));
	}

	/**
	 * Preference for the default date formatter used when converting dates to strings.
	 * <p>
	 * This determines which format pattern is used by the string() operator on dates.
	 * </p>
	 */
	public final static Pref<String> DATES_DEFAULT_FORMATTER = GamaPreferences
			.create("pref_date_default_formatter", "Default date pattern for writing dates (i.e. string(date1))",
					GamlCoreUnits.CUSTOM_KEY, IType.STRING, true)
			.in(GamaPreferences.External.NAME, GamaPreferences.External.DATES)
			.among(GamlCoreUnits.ISO_LOCAL_KEY, GamlCoreUnits.ISO_OFFSET_KEY, GamlCoreUnits.ISO_ZONED_KEY,
					GamlCoreUnits.ISO_SIMPLE_KEY, GamlCoreUnits.CUSTOM_KEY)
			.onChange(e -> {
				DEFAULT_VALUE = e;
				FORMATTERS.put(DEFAULT_KEY, FORMATTERS.get(e));
			});

	/**
	 * Constructs a new date type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaDateType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a date.
	 * <p>
	 * This method supports flexible conversion to dates from various source types:
	 * <ul>
	 * <li>Date - returns the date itself</li>
	 * <li>Container (list) - interprets contents as [year, month, day, hour, minute, second] integers</li>
	 * <li>String - parses using the format specified in preferences or the provided format parameter</li>
	 * <li>Number - interprets as milliseconds since simulation start</li>
	 * </ul>
	 * The param argument can specify a custom date format pattern for string parsing.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a date
	 * @param param
	 *            optional format pattern for string parsing
	 * @param copy
	 *            whether to create a copy if obj is already a date
	 * @return the date representation of the object
	 * @throws GamaRuntimeException
	 *             if the casting operation fails
	 */
	@doc ("Cast the argument into a date. If the argument is a date already, returns it, otherwise: if it is a container, casts its contents to integer numbers and tries to build a date from it (following the order 'year, month, day, hour, minute, second'); if it is a string, tries to decode it into a date using the format described in the preferences; otherwise cast the argument into a float number and interprets it as the number of milliseconds since the start of the simulation")
	@Override
	public IDate cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaDateFactory.castToDate(scope, obj, param, copy);
	}

	/**
	 * Returns the default value for date type.
	 * <p>
	 * The default date is null, as there is no meaningful default date value.
	 * </p>
	 * 
	 * @return null
	 */
	@Override
	public IDate getDefault() { return null; }

	/**
	 * Returns the content type of dates.
	 * <p>
	 * Dates are numeric values (represented internally as float milliseconds), so their content type is float.
	 * </p>
	 * 
	 * @return the float type
	 */
	@Override
	public IType<?> getContentType() { return Types.get(FLOAT); }

	/**
	 * Indicates whether dates can be cast to constant values.
	 * <p>
	 * Dates cannot be constant as they may depend on simulation time or dynamic values.
	 * </p>
	 * 
	 * @return false, dates are not constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Indicates whether dates are compound types.
	 * <p>
	 * Dates are compound as they contain multiple components (year, month, day, hour, minute, second).
	 * </p>
	 * 
	 * @return true, dates are compound types
	 */
	@Override
	public boolean isCompoundType() { return true; }

	/**
	 * Deserializes a date from a JSON representation.
	 * <p>
	 * The JSON map should contain an "iso" field with an ISO 8601 formatted date string.
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing date data
	 * @return the deserialized date
	 */
	@Override
	public IDate deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaDateFactory.createFromISOString(Cast.asString(scope, map2.get("iso")));
	}

	/**
	 * Resolves a locale from a string identifier.
	 * <p>
	 * Supports common locale codes (us, fr, en, de, it, jp, uk) and arbitrary locale strings.
	 * </p>
	 * 
	 * @param l
	 *            the locale string (e.g., "us", "fr", "en")
	 * @return the corresponding Locale, or default locale if l is null
	 */
	static Locale getLocale(final String l) {
		if (l == null) return Locale.getDefault();
		final String locale = l.toLowerCase();
		return switch (locale) {
			case "us" -> Locale.US;
			case "fr" -> Locale.FRANCE;
			case "en" -> Locale.ENGLISH;
			case "de" -> Locale.GERMAN;
			case "it" -> Locale.ITALIAN;
			case "jp" -> Locale.JAPANESE;
			case "uk" -> Locale.UK;
			default -> new Locale(locale);
		};
	}

	/**
	 * Creates a unique key for formatter caching based on pattern and locale.
	 * 
	 * @param p
	 *            the pattern string
	 * @param locale
	 *            the locale identifier
	 * @return the composite key for the formatter map
	 */
	static String getFormatterKey(final String p, final String locale) {
		if (locale == null) return p;
		return p + locale;
	}

	/**
	 * Gets or creates a DateTimeFormatter for the specified pattern and locale.
	 * <p>
	 * This method supports multiple pattern formats:
	 * <ul>
	 * <li>Null pattern - returns the default formatter</li>
	 * <li>Cached patterns - returns pre-built formatters (ISO, custom, etc.)</li>
	 * <li>Java DateTimeFormatter patterns - creates and caches new formatter</li>
	 * <li>GAMA model patterns - patterns using %Y, %M, %D, etc. placeholders</li>
	 * </ul>
	 * </p>
	 * 
	 * <h3>GAMA Pattern Symbols:</h3>
	 * <ul>
	 * <li>%Y - 4-digit year (e.g., 2024)</li>
	 * <li>%M - 2-digit month (01-12)</li>
	 * <li>%N - month name (January, February, etc.)</li>
	 * <li>%D - 2-digit day of month (01-31)</li>
	 * <li>%E - day of week name (Monday, Tuesday, etc.)</li>
	 * <li>%h - 2-digit hour (00-23)</li>
	 * <li>%m - 2-digit minute (00-59)</li>
	 * <li>%s - 2-digit second (00-59)</li>
	 * <li>%z - time zone ID</li>
	 * </ul>
	 * 
	 * @param p
	 *            the pattern string (Java or GAMA format)
	 * @param locale
	 *            the locale identifier for localized formatting (can be null)
	 * @return the DateTimeFormatter for the pattern and locale
	 */
	public static DateTimeFormatter getFormatter(final String p, final String locale) {

		final String pattern = p;
		// Can happen during initialization
		if (FORMATTERS == null || FORMATTERS.isEmpty()) return DateTimeFormatter.ofPattern(GamaDateType.DEFAULT_FORMAT);
		if (pattern == null) return FORMATTERS.get(GamaDateType.DEFAULT_KEY);
		final DateTimeFormatter formatter = FORMATTERS.get(getFormatterKey(pattern, locale));
		if (formatter != null) return formatter;
		if (!pattern.contains("%")) {
			try {
				final DateTimeFormatterBuilder df = new DateTimeFormatterBuilder();
				final DateTimeFormatter result =
						df.parseCaseInsensitive().appendPattern(pattern).toFormatter(getLocale(locale));
				FORMATTERS.put(getFormatterKey(pattern, locale), result);
				return result;
			} catch (final IllegalArgumentException e) {
				GAMA.reportAndThrowIfNeeded(GAMA.getRuntimeScope(),
						GamaRuntimeException.create(e, GAMA.getRuntimeScope()), false);
				return FORMATTERS.get(GamaDateType.DEFAULT_KEY);
			}
		}
		final DateTimeFormatterBuilder df = new DateTimeFormatterBuilder();
		df.parseCaseInsensitive();
		final List<String> dateList = new ArrayList<>();
		final Matcher m = model_pattern.matcher(pattern);
		int i = 0;
		while (m.find()) {
			final String tmp = m.group();
			if (i != m.start()) { dateList.add(pattern.substring(i, m.start())); }
			dateList.add(tmp);
			i = m.end();
		}
		if (i != pattern.length()) { dateList.add(pattern.substring(i)); }
		for (i = 0; i < dateList.size(); i++) {
			final String s = dateList.get(i);
			if (s.charAt(0) == '%' && s.length() == 2) {
				final Character c = s.charAt(1);
				switch (c) {
					case 'Y' -> df.appendValue(YEAR, 4);
					case 'M' -> df.appendValue(MONTH_OF_YEAR, 2);
					case 'N' -> df.appendText(MONTH_OF_YEAR);
					case 'D' -> df.appendValue(DAY_OF_MONTH, 2);
					case 'E' -> df.appendText(DAY_OF_WEEK);
					case 'h' -> df.appendValue(HOUR_OF_DAY, 2);
					case 'm' -> df.appendValue(MINUTE_OF_HOUR, 2);
					case 's' -> df.appendValue(SECOND_OF_MINUTE, 2);
					case 'z' -> df.appendZoneOrOffsetId();
					default -> df.appendLiteral(s);
				}

			} else {
				df.appendLiteral(s);
			}
		}
		final DateTimeFormatter result = df.toFormatter(getLocale(locale));
		FORMATTERS.put(getFormatterKey(pattern, locale), result);
		return result;
	}

}
