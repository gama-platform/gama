package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that adds missing empty parentheses to
 * parameter-less {@code do} statements in GAML source files.
 *
 * <p>In older GAML syntax, parameter-less action calls were written without
 * parentheses:</p>
 * <pre>
 *   do my_action;
 * </pre>
 *
 * <p>The new syntax requires an explicit (possibly empty) argument list:</p>
 * <pre>
 *   do my_action();
 * </pre>
 *
 * <h3>What is matched</h3>
 * <p>Each candidate line must:</p>
 * <ol>
 *   <li>Start with optional whitespace, followed by the {@code do} keyword.</li>
 *   <li>Be followed by at least one whitespace character and then a valid
 *       GAML identifier (letters, digits, underscores; not starting with a
 *       digit).</li>
 *   <li>Not already have a {@code (} immediately after the identifier name
 *       (idempotent guard).</li>
 *   <li>Be followed – after optional whitespace – by {@code ;}, ensuring that
 *       only parameter-less calls are matched and not calls with a {@code with}
 *       clause or existing argument list.</li>
 * </ol>
 *
 * <h3>Idempotency</h3>
 * <p>Because the pattern requires the absence of {@code (} immediately after
 * the identifier, applying this transformer twice produces the same result as
 * applying it once.</p>
 *
 * <h3>Examples</h3>
 * <pre>
 *   do my_action;          →  do my_action();
 *   do my_action   ;       →  do my_action();   (extra spaces kept as-is)
 *   do my_action();        →  (unchanged – already correct)
 *   do my_action with [];  →  (unchanged – has arguments)
 * </pre>
 */
public class FixDoParentheses implements IFileTransformer {

	// -----------------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------------

	/**
	 * Compiled {@link Pattern} that matches a parameter-less GAML {@code do}
	 * statement.
	 *
	 * <p>The pattern breaks down as follows (free-spacing equivalent):</p>
	 * <pre>
	 *   ^               – start of a logical line (with MULTILINE flag)
	 *   (\s*)           – group 1: leading indentation
	 *   do              – literal {@code do} keyword
	 *   (\s+)           – group 2: mandatory whitespace between {@code do} and name
	 *   ([A-Za-z_]\w*)  – group 3: the action name (identifier)
	 *   (?!\s*\()       – negative look-ahead: NOT already followed by '('
	 *   (\s*)           – group 4: optional whitespace before ';'
	 *   (?=;)           – positive look-ahead: must be followed by ';'
	 * </pre>
	 *
	 * <p>The {@link Pattern#MULTILINE} flag makes {@code ^} match at the
	 * beginning of every line rather than only at the beginning of the entire
	 * input string.</p>
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*+)(?!//)do"
			+ "(\\s++)"               // mandatory whitespace
			+ "([A-Za-z_]\\w*+)"     // action name identifier
			+ "(?!\\s*+\\()"          // NOT already followed by '('
			+ "(\\s*+)"              // optional whitespace before ';'
			+ "(?=;)",               // must be followed by ';'
			Pattern.MULTILINE);

	// -----------------------------------------------------------------------
	// IFileTransformer
	// -----------------------------------------------------------------------

	/**
	 * Apply the missing-parentheses transformation to the given source content.
	 *
	 * <p>Each line that begins (after optional indentation) with the {@code do}
	 * keyword, followed by an identifier and then a semicolon {@code ;} (without
	 * an existing argument list), has {@code ()} inserted between the identifier
	 * and the semicolon.</p>
	 *
	 * <p>The replacement preserves:</p>
	 * <ul>
	 *   <li>All leading indentation.</li>
	 *   <li>The exact whitespace between {@code do} and the identifier.</li>
	 *   <li>The exact whitespace between the identifier and the semicolon.</li>
	 * </ul>
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text with {@code ()} inserted where required;
	 *         the original string is returned unchanged when no match is found
	 */
	@Override
	public String transform(final String content) {
		final Matcher m = PATTERN.matcher(content);
		if (!m.find()) { return content; }
		// Reset and do a full replacement pass
		return PATTERN.matcher(content).replaceAll(mr -> {
			final String full = mr.group(0);
			// group(4) = optional whitespace before ';'
			// Insert "()" between the identifier and the trailing whitespace.
			final String wsBeforeSemicolon = mr.group(4);
			return full.substring(0, full.length() - wsBeforeSemicolon.length())
					+ "()" + wsBeforeSemicolon;
		});
	}
}
