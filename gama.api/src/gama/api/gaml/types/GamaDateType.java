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
 * Written by Patrick Tallandier
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = "date",
		id = IType.DATE,
		wraps = { IDate.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE, IConcept.DATE, IConcept.TIME },
		doc = { @doc ("GAML objects that represent a date") })
public class GamaDateType extends GamaType<IDate> {

	/** The model pattern. */
	static Pattern model_pattern = Pattern.compile("%[YMNDEhmsz]");

	/** The Constant DATES_STARTING_DATE. */
	public final static Pref<IDate> DATES_STARTING_DATE =
			GamaPreferences.create("pref_date_starting_date", "Default starting date of models", GamaDateFactory.EPOCH,
					IType.DATE, true).in(GamaPreferences.External.NAME, GamaPreferences.External.DATES);

	/** The Constant DATES_TIME_STEP. */
	public final static Pref<Double> DATES_TIME_STEP =
			GamaPreferences.create("pref_date_time_step", "Default time step of models", 1d, IType.FLOAT, true)
					.in(GamaPreferences.External.NAME, GamaPreferences.External.DATES).between(1d, null);

	/** The Constant FORMATTERS. */
	public static final HashMap<String, DateTimeFormatter> FORMATTERS = new HashMap<>();

	/** The default value. */
	public static String DEFAULT_VALUE = "CUSTOM";

	/** The Constant DEFAULT_KEY. */
	public static final String DEFAULT_KEY = "DEFAULT";

	/** The Constant DEFAULT_FORMAT. */
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** The Constant ISO_SIMPLE_FORMAT. */
	public static final String ISO_SIMPLE_FORMAT = "yy-MM-dd HH:mm:ss";

	/** The Constant DATES_CUSTOM_FORMATTER. */
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

	/** The Constant FORMATTERS. */
	static {
		FORMATTERS.put(DEFAULT_KEY, DateTimeFormatter.ofPattern(DEFAULT_FORMAT));
		FORMATTERS.put(GamlCoreUnits.ISO_SIMPLE_KEY, DateTimeFormatter.ofPattern(ISO_SIMPLE_FORMAT));
		FORMATTERS.put(GamlCoreUnits.ISO_LOCAL_KEY, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		FORMATTERS.put(GamlCoreUnits.ISO_OFFSET_KEY, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		FORMATTERS.put(GamlCoreUnits.ISO_ZONED_KEY, DateTimeFormatter.ISO_ZONED_DATE_TIME);
		FORMATTERS.put(GamlCoreUnits.CUSTOM_KEY, DateTimeFormatter.ofPattern(DATES_CUSTOM_FORMATTER.getValue()));
		FORMATTERS.put(DEFAULT_KEY, FORMATTERS.get(GamlCoreUnits.CUSTOM_KEY));
	}

	/** The Constant DATES_DEFAULT_FORMATTER. */
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
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaDateType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@doc ("Cast the argument into a date. If the argument is a date already, returns it, otherwise: if it is a container, casts its contents to integer numbers and tries to build a date from it (following the order 'year, month, day, hour, minute, second'); if it is a string, tries to decode it into a date using the format described in the preferences; otherwise cast the argument into a float number and interprets it as the number of milliseconds since the start of the simulation")
	@Override
	public IDate cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaDateFactory.castToDate(scope, obj, param, copy);
	}

	@Override
	public IDate getDefault() { return null; }

	@Override
	public IType<?> getContentType() { return Types.get(FLOAT); }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public IDate deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaDateFactory.createFromISOString(Cast.asString(scope, map2.get("iso")));
	}

	/**
	 * Gets the locale.
	 *
	 * @param l
	 *            the l
	 * @return the locale
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
	 * Gets the formatter key.
	 *
	 * @param p
	 *            the p
	 * @param locale
	 *            the locale
	 * @return the formatter key
	 */
	static String getFormatterKey(final String p, final String locale) {
		if (locale == null) return p;
		return p + locale;
	}

	/**
	 * Gets the formatter.
	 *
	 * @param p
	 *            the p
	 * @param locale
	 *            the locale
	 * @return the formatter
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
