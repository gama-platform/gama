/*******************************************************************************************************
 *
 * DEBUG.java, in gama.dev, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev;

import static gama.dev.FLAGS.ENABLE_DEBUG;
import static gama.dev.FLAGS.ENABLE_LOGGING;
import static java.lang.System.currentTimeMillis;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A comprehensive debugging and logging utility class that provides flexible debugging capabilities that can be
 * enabled/disabled on a per-class basis. This class offers various debugging methods including simple output, timing
 * utilities, banners, and stack traces.
 *
 * <h2>Main Features:</h2>
 * <ul>
 * <li><strong>Class-based debugging:</strong> Can be turned on/off for specific classes</li>
 * <li><strong>Timing utilities:</strong> Measure execution time of code blocks</li>
 * <li><strong>Formatted output:</strong> Banners, titles, sections with consistent formatting</li>
 * <li><strong>Stack trace debugging:</strong> Print filtered stack traces</li>
 * <li><strong>Counter utilities:</strong> Track method call counts</li>
 * <li><strong>Thread-safe:</strong> Uses concurrent collections for multi-threaded environments</li>
 * <li><strong>Custom output streams:</strong> Redirect output to custom streams per thread</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>Basic debugging:</h3>
 *
 * <pre>
 * // Enable debugging for current class
 * DEBUG.ON();
 *
 * // Output debug messages (only shown if debugging is enabled for this class)
 * DEBUG.OUT("This is a debug message");
 * DEBUG.OUT("Value: ", 20, someObject);
 *
 * // Always output (regardless of class registration)
 * DEBUG.LOG("Always shown");
 * DEBUG.ERR("Error message");
 * </pre>
 *
 * <h3>Timing operations:</h3>
 *
 * <pre>
 * // Time a runnable operation
 * DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Loading data", "completed in", () -> {
 * 	// Your code here
 * });
 *
 * // Time a supplier operation and get result
 * String result = DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Processing", "finished in", () -> { return processData(); });
 * </pre>
 *
 * <h3>Formatting utilities:</h3>
 *
 * <pre>
 * DEBUG.SECTION("Important Section"); // Creates a bordered section
 * DEBUG.TITLE("My Title"); // Creates a padded title
 * DEBUG.BANNER(BANNER_CATEGORY.GAMA, "Operation", "status", "result");
 * DEBUG.LINE(); // Outputs a line of dashes
 * </pre>
 *
 * <h3>Stack traces and debugging:</h3>
 *
 * <pre>
 * DEBUG.STACK(); // Print filtered stack trace
 * DEBUG.RESET(); // Reset counters for current class
 * </pre>
 *
 * <h2>Global Control:</h2> The debugging system can be globally controlled through the FLAGS class constants:
 * <ul>
 * <li>{@code FLAGS.ENABLE_DEBUG} - Master switch for debug functionality</li>
 * <li>{@code FLAGS.ENABLE_LOGGING} - Master switch for logging functionality</li>
 * <li>{@code DEBUG.FORCE_ON} - Forces debugging on for all classes regardless of registration</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2> This class is thread-safe and uses:
 * <ul>
 * <li>ConcurrentHashMap for storing registered classes and counters</li>
 * <li>ThreadLocal for per-thread log writers</li>
 * <li>StackWalker for efficient stack inspection</li>
 * </ul>
 *
 * @author A. Drogoul
 * @since August 2018
 */
public class DEBUG {

	/** The force on. */
	public static boolean FORCE_ON;

	/**
	 * A custom security manager that exposes the getClassContext() information for determining the calling class name
	 * when StackWalker is not sufficient.
	 */
	static private class MySecurityManager extends SecurityManager {

		/**
		 * Gets the caller class name.
		 *
		 * @param callStackDepth
		 *            the call stack depth
		 * @return the caller class name
		 */
		public String getCallerClassName(final int callStackDepth) {
			return getClassContext()[callStackDepth].getName();
		}

	}

	/** The Constant SECURITY_MANAGER. */
	private final static MySecurityManager SECURITY_MANAGER = new MySecurityManager();

	/** The Constant REGISTERED. */
	// AD 08/18: Changes to ConcurrentHashMap for multi-threaded DEBUG operations
	private static final ConcurrentHashMap<String, String> REGISTERED = new ConcurrentHashMap<>();

	/** The Constant COUNTERS. */
	private static final ConcurrentHashMap<String, Integer> COUNTERS = new ConcurrentHashMap<>();

	/** The Constant LOG_WRITERS. */
	private static final ThreadLocal<PrintStream> LOG_WRITERS = ThreadLocal.withInitial(() -> System.out);

	/** The Constant stackWalker. */
	static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	/**
	 * Uses a custom security manager to get the caller class name. Use of reflection would be faster, but more prone to
	 * Oracle evolutions. StackWalker in Java 9 will be interesting to use for that
	 *
	 * @return the name of the class that has called the method that has called this method
	 */
	static String findCallingClassName() {
		Optional<String> caller = STACK_WALKER.walk(frames -> frames.map(StackFrame::getClassName)
				.filter(s -> !s.contains("gama.dev") && !s.contains("org.slf4j")).findFirst());
		if (caller.isEmpty()) return SECURITY_MANAGER.getCallerClassName(3);
		return caller.get();
	}

	/**
	 * Resets the number previously used by COUNT() so that the next call to COUNT() returns 0;
	 *
	 */
	public static void RESET() {
		final String s = findCallingClassName();
		if (REGISTERED.containsKey(s) && COUNTERS.containsKey(s)) { COUNTERS.put(s, -1); }
	}

	/**
	 * A functional interface that represents a runnable operation that may throw an exception. This is used by timing
	 * methods that need to handle checked exceptions from the code being timed.
	 *
	 * @param <T>
	 *            the type of exception that may be thrown
	 */
	public interface RunnableWithException<T extends Throwable> {

		/**
		 * Run.
		 *
		 * @throws T
		 *             the t
		 */
		void run() throws T;
	}

	/**
	 * Simple timing utility to measure and output the number of milliseconds taken by a runnable operation. If logging
	 * is enabled, outputs the banner with timing information once the runnable is finished.
	 *
	 * <p>
	 * The method executes the runnable and measures its execution time, then displays the result in a formatted banner.
	 * If global logging is disabled, simply runs the runnable with minimal overhead.
	 * </p>
	 *
	 * <h3>Usage example:</h3>
	 *
	 * <pre>
	 * DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Loading data", "completed in", () -> { loadData(); });
	 * // Output: > GAMA : Loading data _____________ completed in 150ms
	 * </pre>
	 *
	 * @param category
	 *            the banner category to display
	 * @param begin
	 *            the initial description of the operation
	 * @param end
	 *            the completion description (e.g., "completed in", "finished in")
	 * @param runnable
	 *            the operation to execute and measure
	 * @param followUpWithResult
	 *            optional consumers to process the timing result
	 */

	@SafeVarargs
	public static void TIMER(final BANNER_CATEGORY category, final String begin, final String end,
			final Runnable runnable, final Consumer<Long>... followUpWithResult) {
		if (!ENABLE_LOGGING) {
			runnable.run();
			return;
		}
		final long start = currentTimeMillis();
		runnable.run();
		long duration = currentTimeMillis() - start;
		BANNER(category, begin, end, duration + "ms");
		if (followUpWithResult != null && followUpWithResult.length > 0) { followUpWithResult[0].accept(duration); }
	}

	/**
	 * Timer with exceptions.
	 *
	 * @param <T>
	 *            the generic type
	 * @param title
	 *            the title
	 * @param runnable
	 *            the runnable
	 * @throws T
	 *             the t
	 */
	public static <T extends Throwable> void TIMER_WITH_EXCEPTIONS(final BANNER_CATEGORY category, final String begin,
			final String end, final RunnableWithException<T> runnable) throws T {
		if (!ENABLE_LOGGING) {
			runnable.run();
			return;
		}
		final long start = currentTimeMillis();
		runnable.run();
		BANNER(category, begin, end, currentTimeMillis() - start + "ms");
	}

	/**
	 * Simple timing utility to measure and output the number of ms taken by the execution of a Supplier. Contrary to
	 * the timer accepting a runnable, this one returns a result. If the class is registered, outputs the title provided
	 * and the time taken once the supplier is finished and returns its result, otherwise simply returns the result of
	 * the supplier (the overhead is minimal compared to simply executing the contents of the provider)
	 *
	 * Usage: Integer i = DEBUG.TIMER("My important integer computation", ()->myIntegerComputation()); // provided
	 * myIntegerComputation() returns an Integer.
	 *
	 * Output: My important integer computation: 100ms
	 *
	 * @param title
	 *            a string that will prefix the number of ms
	 * @param supplier
	 *            an object that encapsulates the computation to measure
	 *
	 * @return The result of the supplier passed in argument
	 */

	public static <T> T TIMER(final BANNER_CATEGORY category, final String title, final String end,
			final Supplier<T> supplier) {
		if (!ENABLE_LOGGING) return supplier.get();
		final long start = System.currentTimeMillis();
		final T result = supplier.get();
		BANNER(category, title, end, currentTimeMillis() - start + "ms");
		return result;
	}

	/**
	 * Enables debugging for the calling class. Once enabled, all subsequent calls to DEBUG.OUT() from this class will
	 * produce output. This method is thread-safe and can be called from multiple threads concurrently.
	 *
	 * <p>
	 * The enabling is based on the fully qualified class name of the calling class. If debugging is globally disabled
	 * via FLAGS.ENABLE_DEBUG or FLAGS.ENABLE_LOGGING, this method has no effect.
	 * </p>
	 *
	 * <h3>Example usage:</h3>
	 *
	 * <pre>
	 * public class MyClass {
	 * 	static {
	 * 		DEBUG.ON(); // Enable debugging for MyClass
	 * 	}
	 *
	 * 	public void myMethod() {
	 * 		DEBUG.OUT("This will be shown");
	 * 	}
	 * }
	 * </pre>
	 */
	public static final void ON() {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		final String calling = findCallingClassName();
		REGISTERED.put(calling, calling);
	}

	/**
	 * Enables debugging for a specific class by name. This allows enabling debugging for classes other than the current
	 * calling class.
	 *
	 * <p>
	 * This method is useful when you want to enable debugging from a different class context or when programmatically
	 * controlling debug output.
	 * </p>
	 *
	 * @param calling
	 *            the fully qualified class name to enable debugging for
	 */
	public static final void ON(final String calling) {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		REGISTERED.put(calling, calling);
	}

	/**
	 * Disables debugging for the calling class. Once disabled, all subsequent calls to DEBUG.OUT() from this class will
	 * not produce any output.
	 *
	 * <p>
	 * This call can be avoided in a static context (not calling ON() will prevent the calling class from debugging
	 * anyway), but it can be used to disable logging based on some user actions, for instance.
	 * </p>
	 */
	public static final void OFF() {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		final String name = findCallingClassName();
		REGISTERED.remove(name);
	}

	/**
	 * Checks whether DEBUG is active for the calling class. This method inspects the calling class and returns true if
	 * debugging has been enabled for it.
	 *
	 * <p>
	 * The method returns false if global debugging is disabled through FLAGS.ENABLE_DEBUG or FLAGS.ENABLE_LOGGING, and
	 * true if FORCE_ON is set to true.
	 * </p>
	 *
	 * @return true if debug output will be shown for the calling class, false otherwise
	 */
	public static boolean IS_ON() {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return false;
		return IS_ON(findCallingClassName());
	}

	/**
	 * Outputs an error message unconditionally to System.err. This method will always produce output unless global
	 * logging is disabled through FLAGS.ENABLE_DEBUG or FLAGS.ENABLE_LOGGING.
	 *
	 * <p>
	 * This method is designed for error reporting that should be visible regardless of whether debugging is enabled for
	 * the calling class.
	 * </p>
	 *
	 * @param s
	 *            the object to output as an error message
	 */
	public static final void ERR(final Object s) {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		System.err.println(STRINGS.TO_STRING(s));
	}

	/**
	 * Outputs an error message unconditionally to System.err along with a stack trace. This method will always produce
	 * output unless global logging is disabled.
	 *
	 * @param s
	 *            the error message object to output
	 * @param t
	 *            the throwable whose stack trace should be printed
	 */
	public static final void ERR(final Object s, final Throwable t) {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		System.err.println(STRINGS.TO_STRING(s));
		t.printStackTrace();
	}

	/**
	 * Outputs a message unconditionally to System.out or the registered log writer. This method will always produce
	 * output unless global logging is disabled through FLAGS.ENABLE_LOGGING.
	 *
	 * <p>
	 * Unlike DEBUG.OUT(), this method does not check if debugging is enabled for the calling class - it always outputs
	 * the message.
	 * </p>
	 *
	 * @param string
	 *            the object to output
	 */
	public static void LOG(final Object string) {
		if (ENABLE_LOGGING) { LOG(string, true); }
	}

	/**
	 * Outputs a formatted banner line with default category (GAMA). Banners provide a consistent format for displaying
	 * operation status and results.
	 *
	 * <p>
	 * The output format is: "&gt; CATEGORY: title ________ state result"
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>
	 * DEBUG.BANNER("File loading", "completed in", "120ms");
	 * // Outputs: > GAMA : File loading _________________ completed in 120ms
	 * </pre>
	 *
	 * @param title
	 *            the main description of the operation
	 * @param state
	 *            the status description (e.g., "completed in", "failed with")
	 * @param result
	 *            the result or outcome (e.g., "120ms", "success")
	 */
	public static void BANNER(final String title, final String state, final String result) {
		BANNER(BANNER_CATEGORY.GAMA, title, state, result);
	}

	/**
	 * Outputs a formatted banner line with a specific category. Banners provide a consistent format for displaying
	 * operation status and results with category labels.
	 *
	 * <p>
	 * The output format is: "&gt; CATEGORY: title ________ state result"
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>
	 * DEBUG.BANNER(BANNER_CATEGORY.GAMA, "Model initialization", "completed in", "500ms");
	 * // Outputs: > SIMULATION: Model initialization _______ completed in 500ms
	 * </pre>
	 *
	 * @param category
	 *            the category to display (e.g., GAMA, GAML, etc.)
	 * @param title
	 *            the main description of the operation
	 * @param state
	 *            the status description (e.g., "completed in", "failed with")
	 * @param result
	 *            the result or outcome (e.g., "120ms", "success")
	 */
	public static void BANNER(final BANNER_CATEGORY category, final String title, final String state,
			final String result) {
		String cat = STRINGS.PAD("> " + category, 8, ' ') + ": ";
		LOG(STRINGS.PAD(cat + title + " ", 55, ' ') + STRINGS.PAD(" " + state, 15, '_') + " " + result);
	}

	/**
	 * Outputs a message to System.out or the registered logger for this thread, with control over whether to include a
	 * new line. This method always outputs unless ENABLE_LOGGING is false.
	 *
	 * <p>
	 * This method properly handles arrays by outputting their contents instead of their identity/reference.
	 * </p>
	 *
	 * @param object
	 *            the message to output
	 * @param newLine
	 *            whether to add a new line after the output
	 */
	public static void LOG(final Object object, final boolean newLine) {
		if (ENABLE_LOGGING) {
			if (newLine) {
				LOG_WRITERS.get().println(STRINGS.TO_STRING(object));
			} else {
				LOG_WRITERS.get().print(STRINGS.TO_STRING(object));
			}
		}
	}

	/**
	 * Registers a custom output stream for the current thread. All subsequent LOG() calls from this thread will
	 * redirect output to the specified writer instead of System.out.
	 *
	 * <p>
	 * This is useful for capturing debug output to files or other custom streams on a per-thread basis. The
	 * registration is thread-local and does not affect other threads.
	 * </p>
	 *
	 * @param writer
	 *            the OutputStream to redirect debug output to
	 */
	public static void REGISTER_LOG_WRITER(final OutputStream writer) {
		LOG_WRITERS.set(new PrintStream(writer, true));
	}

	/**
	 * Unregisters any custom log writer for the current thread, reverting back to System.out. This removes the
	 * thread-local log writer registration.
	 */
	public static void UNREGISTER_LOG_WRITER() {
		LOG_WRITERS.remove();
	}

	/**
	 * Checks if debugging is enabled for a specific class name. This method supports partial matching for inner classes
	 * and anonymous classes.
	 *
	 * <p>
	 * Returns true if FORCE_ON is set, or if the className starts with any registered class name.
	 * </p>
	 *
	 * @param className
	 *            the fully qualified class name to check
	 * @return true if debugging is enabled for the class, false otherwise
	 */
	static boolean IS_ON(final String className) {
		// Necessary to loop on the names as the call can emanate from an inner class or
		// an anonymous class of the
		// "allowed" class
		if (FORCE_ON) return true;
		for (final String name : REGISTERED.keySet()) { if (className.startsWith(name)) return true; }
		return false;
	}

	/**
	 * Instantiates a new debug.
	 */
	private DEBUG() {}

	/**
	 * Outputs a debug message to System.out only if debugging is enabled for the calling class. This is the primary
	 * method for class-specific debug output.
	 *
	 * <p>
	 * The message will only appear if:
	 * </p>
	 * <ul>
	 * <li>Global debugging is enabled (FLAGS.ENABLE_DEBUG and FLAGS.ENABLE_LOGGING)</li>
	 * <li>The calling class has been registered via DEBUG.ON() or FORCE_ON is true</li>
	 * </ul>
	 *
	 * @param s
	 *            the message to output
	 */
	public static final void OUT(final Object s) {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		if (IS_ON(findCallingClassName())) { LOG(s, true); }
	}

	/**
	 * Outputs a debug message to System.out if DEBUG is turned on for this class, followed or not by a new line
	 *
	 * @param s
	 *            the message to output
	 * @param newLine
	 *            whether or not to output a new line after the message
	 */
	public static final void OUT(final Object s, final boolean newLine) {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING) return;
		if (IS_ON(findCallingClassName())) { LOG(s, newLine); }
	}

	/**
	 * Outputs a debug message to System.out if DEBUG is turned on for this class
	 *
	 * @param title
	 *            the first string to output
	 * @param pad
	 *            the minimum length of the first string (padded with spaces if shorter)
	 * @param other
	 *            another object on which TO_STRING() is applied
	 */
	public static final void OUT(final String title, final int pad, final Object other) {
		if (!ENABLE_DEBUG || !ENABLE_LOGGING || title == null) return;
		if (IS_ON(findCallingClassName())) { LOG(STRINGS.PAD(title, pad) + STRINGS.TO_STRING(other)); }
	}

	/**
	 * A utility method to output a line of 80 dashes.
	 */
	public static final void LINE() {
		LOG(STRINGS.PAD("", 80, '-'));
	}

	/**
	 * A utility method to output a "section" (i.e. a title padded with dashes between two lines of 80 chars).
	 * Equivalent to LINE();TITLE(s);LINE()
	 *
	 */
	public static final void SECTION(final String s) {
		if (s == null) return;
		LINE();
		TITLE(s);
		LINE();
	}

	/**
	 * A utility method to output a "title" (i.e. a title centered and padded with dashes to form a line of 80 chars)
	 *
	 */
	public static final void TITLE(final String s) {
		if (s == null) return;
		LOG(STRINGS.PAD("---------- " + s.toUpperCase() + " ", 80, '-'));
	}

	/**
	 * Outputs a filtered stack trace if debugging is enabled for the calling class. This method shows the call stack
	 * starting from the caller, skipping internal DEBUG class frames.
	 */
	public static void STACK() {
		if (!ENABLE_LOGGING || !IS_ON(findCallingClassName())) return;
		LOG(STRINGS.PAD("--- Stack trace ", 80, '-'));
		STACK_WALKER.walk(stream1 -> {
			stream1.skip(2).forEach(s -> DEBUG.LOG("> " + s));
			return null;
		});
		LINE();
	}

	/**
	 * Forces debugging on for all classes, regardless of registration status. When called, all DEBUG.OUT() calls will
	 * produce output regardless of whether a class has been registered with ON().
	 *
	 * <p>
	 * This is useful for global debugging sessions or when you want to temporarily enable all debug output.
	 * </p>
	 */
	public static void FORCE_ON() {
		FORCE_ON = true;
	}

}
