/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * Provides date and time handling capabilities for the GAMA modeling platform.
 * 
 * <p>
 * This package implements a comprehensive date/time system built on the Java Time API (JSR-310),
 * offering immutable, thread-safe date representations with full temporal arithmetic support.
 * All date operations in GAMA models use these classes to ensure consistency and precision.
 * </p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Date Representation</h3>
 * <ul>
 * <li>{@link gama.api.types.date.IDate} - The main interface for all date operations in GAMA,
 * extending {@link java.time.temporal.Temporal} to provide full integration with Java Time API.</li>
 * <li>{@link gama.api.types.date.GamaDate} - The immutable implementation of {@code IDate} that
 * wraps JSR-310 temporal objects (LocalDateTime, ZonedDateTime, OffsetDateTime).</li>
 * </ul>
 * 
 * <h3>Date Creation</h3>
 * <ul>
 * <li>{@link gama.api.types.date.GamaDateFactory} - A static factory providing multiple ways to
 * create {@code IDate} instances from various sources including strings, numbers, containers, and
 * temporal objects.</li>
 * </ul>
 * 
 * <h3>Date Intervals</h3>
 * <ul>
 * <li>{@link gama.api.types.date.GamaDateInterval} - Represents an immutable time interval between
 * two dates, supporting iteration with custom step durations.</li>
 * </ul>
 * 
 * <h3>Formatting</h3>
 * <ul>
 * <li>{@link gama.api.types.date.DurationFormatter} - Formats durations as human-readable strings
 * with intelligent formatting (e.g., "2 months 15 days 12:30:45").</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * 
 * <h3>Immutability</h3>
 * <p>
 * All date objects are immutable. Operations that modify dates (plus, minus, with) return new
 * instances rather than modifying the original. This ensures thread safety and prevents
 * accidental modifications.
 * </p>
 * 
 * <h3>Time Zone Support</h3>
 * <p>
 * Full support for time zones and offsets. Dates can be created with specific zones or use
 * the system default zone. Zone information is preserved through all operations.
 * </p>
 * 
 * <h3>Temporal Arithmetic</h3>
 * <p>
 * Complete support for date/time calculations using {@link java.time.temporal.TemporalAmount}
 * and {@link java.time.temporal.TemporalUnit}. Supports addition, subtraction, and comparison
 * operations with various units (years, months, days, hours, minutes, seconds).
 * </p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating Dates</h3>
 * <pre>{@code
 * // From current time
 * IDate now = GamaDateFactory.now();
 * 
 * // From ISO string
 * IDate date = GamaDateFactory.createFromISOString("2024-03-15T14:30:00Z");
 * 
 * // From string with pattern
 * IDate custom = GamaDateFactory.createWith(scope, "15/03/2024", "dd/MM/yyyy");
 * 
 * // From epoch
 * IDate epoch = GamaDateFactory.EPOCH;
 * }</pre>
 * 
 * <h3>Date Operations</h3>
 * <pre>{@code
 * // Add time
 * IDate tomorrow = today.plus(1, ChronoUnit.DAYS);
 * IDate nextWeek = today.plus(Duration.ofDays(7));
 * 
 * // Subtract time
 * IDate yesterday = today.minus(1, ChronoUnit.DAYS);
 * 
 * // Modify fields
 * IDate noon = date.with(ChronoField.HOUR_OF_DAY, 12);
 * 
 * // Compare dates
 * boolean isBefore = date1.isBefore(date2);
 * boolean isAfter = date1.isAfter(date2);
 * int comparison = date1.compareTo(date2);
 * }</pre>
 * 
 * <h3>Working with Intervals</h3>
 * <pre>{@code
 * // Create an interval
 * IDate start = GamaDateFactory.createWith(scope, "2024-01-01", "yyyy-MM-dd");
 * IDate end = GamaDateFactory.createWith(scope, "2024-12-31", "yyyy-MM-dd");
 * GamaDateInterval interval = GamaDateInterval.of(start, end);
 * 
 * // Check containment
 * boolean contains = interval.contains(someDate);
 * 
 * // Get duration
 * Duration length = interval.toDuration();
 * 
 * // Iterate with custom step
 * Duration step = Duration.ofDays(7);
 * GamaDateInterval weekly = new GamaDateInterval(start, end, step);
 * for (IDate date : weekly) {
 *     // Process each week
 * }
 * }</pre>
 * 
 * <h3>Accessing Date Components</h3>
 * <pre>{@code
 * int year = date.getYear();
 * int month = date.getMonth();
 * int day = date.getDay();
 * int hour = date.getHour();
 * int minute = date.getMinute();
 * int second = date.getSecond();
 * 
 * int dayOfWeek = date.getDayWeek();  // Monday = 1
 * int dayOfYear = date.getDayOfYear(); // 1-365/366
 * boolean isLeap = date.getIsLeap();
 * }</pre>
 * 
 * <h3>Formatting Dates</h3>
 * <pre>{@code
 * // ISO format
 * String iso = date.toISOString();
 * 
 * // Custom format
 * String formatted = date.toString("yyyy-MM-dd HH:mm:ss", "en");
 * 
 * // Format duration
 * Duration d = Duration.ofDays(45).plusHours(12).plusMinutes(30);
 * String readable = DurationFormatter.INSTANCE.toString(d);
 * // Result: "1 month 15 days 12:30:00"
 * }</pre>
 * 
 * <h2>Integration with GAMA Models</h2>
 * 
 * <p>
 * These date classes are fully integrated into the GAML language through the {@code date} type.
 * In GAMA models, dates are used for simulation time management, scheduling, and temporal data
 * analysis. The starting date and time step of simulations are controlled through these classes.
 * </p>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * All classes in this package are thread-safe. The immutability of date objects ensures that
 * they can be safely shared across multiple threads without synchronization.
 * </p>
 * 
 * @see java.time
 * @see java.time.temporal.Temporal
 * @see java.time.temporal.TemporalAmount
 * @see gama.api.gaml.types.GamaDateType
 * 
 * @author Patrick Taillandier
 * @author Alexis Drogoul
 * @since GAMA 1.7
 */
package gama.api.types.date;
