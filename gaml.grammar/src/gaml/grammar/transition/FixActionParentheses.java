package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that adds missing empty parentheses to action and
 * typed-return declarations in GAML source files.
 *
 * <p>In older GAML syntax, parameter-less actions and typed definitions were
 * written without parentheses:</p>
 * <pre>
 *   action my_action { ... }
 *   int    compute_sum { ... }
 *   bool   is_valid { ... }
 * </pre>
 *
 * <p>The new syntax requires an explicit (possibly empty) argument list:</p>
 * <pre>
 *   action my_action() { ... }
 *   int    compute_sum() { ... }
 *   bool   is_valid() { ... }
 * </pre>
 *
 * <p>The transformer handles all of the following GAML keyword / type
 * prefixes:</p>
 * <ul>
 *   <li>{@code action}</li>
 *   <li>Primitive scalar types: {@code int}, {@code float}, {@code bool},
 *       {@code string}</li>
 *   <li>Collection types (bare or parameterised): {@code list},
 *       {@code map}, {@code matrix}, {@code container}, {@code pair},
 *       {@code graph}</li>
 *   <li>Geometry / topology types: {@code point}, {@code geometry},
 *       {@code topology}, {@code path}</li>
 *   <li>Agent and meta types: {@code agent}, {@code unknown}, {@code any}</li>
 *   <li>Miscellaneous: {@code rgb}, {@code file}, {@code date}, {@code font},
 *       {@code image}</li>
 * </ul>
 *
 * <h3>What is matched</h3>
 * <p>Each candidate line must:</p>
 * <ol>
 *   <li>Start with optional whitespace, followed by one of the recognised
 *       type / keyword tokens (word-boundary checked).</li>
 *   <li>Optionally include a generic type parameter (e.g. {@code list<int>},
 *       {@code map<string,int>} – arbitrarily nested angle brackets are
 *       supported).</li>
 *   <li>Be followed by at least one whitespace character and then a valid
 *       GAML identifier (letters, digits, underscores; not starting with a
 *       digit).</li>
 *   <li>Not already have a {@code (} immediately after the identifier name
 *       (idempotent guard).</li>
 *   <li>Be followed – after optional whitespace – by {@code \{} (the opening
 *       body brace), ensuring that only block-style definitions are matched
 *       and not simple variable declarations.</li>
 * </ol>
 *
 * <h3>Idempotency</h3>
 * <p>Because the pattern requires the absence of {@code (} immediately after
 * the identifier, applying this transformer twice produces the same result as
 * applying it once.</p>
 *
 * <h3>Examples</h3>
 * <pre>
 *   action my_action {        →  action my_action() {
 *   action my_action   {      →  action my_action() {  (extra spaces kept as-is)
 *   int    compute {          →  int    compute() {
 *   list&lt;int&gt; build_list {   →  list&lt;int&gt; build_list() {
 *   map&lt;string,int&gt; counts { →  map&lt;string,int&gt; counts() {
 *   action my_action() {      →  (unchanged – already correct)
 *   int x &lt;- 0;               →  (unchanged – no opening brace)
 * </pre>
 */
public class FixActionParentheses implements IFileTransformer {

	// -----------------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------------

	/**
	 * Pipe-separated alternation of all GAML keywords and built-in type names
	 * that may introduce a block-style definition (action or typed action).
	 *
	 * <p>The alternation is ordered so that longer tokens that share a common
	 * prefix (e.g. {@code geometry} before {@code graph}) are tried first, which
	 * is not strictly necessary here because the surrounding word-boundary
	 * anchors in {@link #PATTERN} disambiguate them, but it aids readability.</p>
	 */
	private static final String TYPE_ALTERNATION =
			"action|int|float|bool|string"
			+ "|list|map|matrix|container|pair|graph"
			+ "|point|geometry|topology|path"
			+ "|agent|unknown|any"
			+ "|rgb|file|date|font|image";

	/**
	 * Compiled {@link Pattern} that matches a parameter-less GAML action or
	 * typed-action declaration.
	 *
	 * <p>The pattern breaks down as follows (free-spacing equivalent):</p>
	 * <pre>
	 *   (?&lt;=^|\n)           – start of a logical line (zero-width look-behind)
	 *   (\s*)               – group 1: leading indentation
	 *   (?:TYPE_ALTERNATION) – one of the recognised type keywords (non-capturing)
	 *   (?:&lt;[^&gt;]*(?:&lt;[^&gt;]*&gt;[^&gt;]*)*&gt;)?  – group 2: optional generic type parameter
	 *   (\s+)               – group 3: mandatory whitespace between type and name
	 *   ([A-Za-z_]\w*)      – group 4: the definition name (identifier)
	 *   (?!\s*\()           – negative look-ahead: NOT already followed by '('
	 *   (\s*)               – group 5: whitespace before the opening brace
	 *   (?=\{)              – positive look-ahead: must be followed by '{'
	 * </pre>
	 *
	 * <p>The {@link Pattern#MULTILINE} flag makes {@code ^} match at the
	 * beginning of every line rather than only at the beginning of the entire
	 * input string.</p>
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*)(?:" + TYPE_ALTERNATION + ")"
			+ "(?:<[^>]*(?:<[^>]*>[^>]*)*>)?"   // optional generic type param (up to 2 levels deep)
			+ "(\\s+)"                            // mandatory whitespace
			+ "([A-Za-z_]\\w*)"                  // identifier name
			+ "(?!\\s*\\()"                       // NOT already followed by '('
			+ "(\\s*)"                            // optional whitespace before '{'
			+ "(?=\\{)",                          // must be followed by '{'
			Pattern.MULTILINE);

	// -----------------------------------------------------------------------
	// IFileTransformer
	// -----------------------------------------------------------------------

	/**
	 * Apply the missing-parentheses transformation to the given source content.
	 *
	 * <p>Each line that begins (after optional indentation) with a recognised
	 * GAML type keyword, followed by an identifier and then an opening brace
	 * {@code \{}, has {@code ()} inserted between the identifier and the brace.</p>
	 *
	 * <p>The replacement preserves:</p>
	 * <ul>
	 *   <li>All leading indentation.</li>
	 *   <li>The exact whitespace between the type keyword and the identifier.</li>
	 *   <li>The exact whitespace between the identifier and the opening brace.</li>
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
			// Reconstruct the full match with () appended after the identifier
			// mr.group(0) is the entire match up to (but not including) the '{'.
			// We insert "()" between the identifier and the whitespace-before-brace.
			final String full = mr.group(0);
			// The identifier ends at the position just before group 4 (ws before brace).
			// group(3) = identifier, group(4) = ws before '{'
			// We append "()" after the identifier, before the trailing whitespace.
			final String wsBeforeBrace = mr.group(4);
			return full.substring(0, full.length() - wsBeforeBrace.length())
					+ "()" + wsBeforeBrace;
		});
	}
}
