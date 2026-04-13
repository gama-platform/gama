package gaml.grammar.transition;

import java.util.Set;
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
 *   my_species get_agent { ... }
 * </pre>
 *
 * <p>The new syntax requires an explicit (possibly empty) argument list:</p>
 * <pre>
 *   action my_action() { ... }
 *   int    compute_sum() { ... }
 *   my_species get_agent() { ... }
 * </pre>
 *
 * <h3>What is matched</h3>
 * <p>Each candidate line must:</p>
 * <ol>
 *   <li>Start with optional whitespace (not a {@code //} comment).</li>
 *   <li>Have a first token that is a valid GAML identifier and is
 *       <em>not</em> a known GAML statement keyword (see
 *       {@link #EXCLUDED_KEYWORDS}).</li>
 *   <li>Optionally include a generic type parameter (e.g. {@code list<int>},
 *       {@code map<string,int>}).</li>
 *   <li>Be followed by at least one whitespace character and then a valid
 *       GAML identifier.</li>
 *   <li>Not already have a {@code (} immediately after the identifier name
 *       (idempotent guard).</li>
 *   <li>Be followed – after optional whitespace – by {@code \{} (the opening
 *       body brace).</li>
 * </ol>
 *
 * <h3>Idempotency</h3>
 * <p>Applying this transformer twice produces the same result as applying it
 * once.</p>
 *
 * <h3>Examples</h3>
 * <pre>
 *   action my_action {        →  action my_action() {
 *   int    compute {          →  int    compute() {
 *   list&lt;int&gt; build_list {   →  list&lt;int&gt; build_list() {
 *   my_species get_agent {    →  my_species get_agent() {
 *   action my_action() {      →  (unchanged – already correct)
 *   int x &lt;- 0;               →  (unchanged – no opening brace)
 *   ask prey {                →  (unchanged – 'ask' is a statement keyword)
 *   if condition {            →  (unchanged – 'if' is a statement keyword)
 *   reflex my_reflex {        →  (unchanged – 'reflex' is a statement keyword)
 *   // action foo {           →  (unchanged – comment line)
 * </pre>
 */
public class FixActionParentheses implements IFileTransformer {

	// -----------------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------------

	/**
	 * GAML statement keywords that are <em>not</em> type names. When the first
	 * token on a line is one of these, the line is skipped even if it
	 * structurally matches {@code keyword identifier \{}.
	 *
	 * <p>The check is performed in Java code ({@link Set#contains}) rather than
	 * in the regex, which keeps the pattern simple and avoids catastrophic
	 * backtracking caused by long alternations inside look-aheads.</p>
	 */
	private static final Set<String> EXCLUDED_KEYWORDS = Set.of(
			// control flow
			"if", "else", "loop", "switch", "match", "break", "return",
			// agent interactions & data
			"ask", "create", "do", "let", "add", "remove", "put", "set",
			"save", "write", "draw",
			// agent behavior blocks
			"reflex", "aspect", "init", "state", "plan", "rule", "perceive",
			"norm", "sanction", "obligation",
			// output / display blocks
			"equation", "chart", "display", "monitor", "event", "layout",
			"overlay", "inspect",
			// species / model structure
			"species", "experiment", "global", "model", "import", "output",
			"permanent",
			// misc
			"capture", "release", "migrate", "using", "try", "catch",
			"error", "warn", "trace", "debug", "assert");

	/**
	 * Compiled {@link Pattern} that matches a candidate GAML action or
	 * typed-action declaration.
	 *
	 * <p>All repeating quantifiers are <em>possessive</em> ({@code *+} /
	 * {@code ++}) to guarantee linear-time matching with zero backtracking.</p>
	 *
	 * <p>Groups:</p>
	 * <ol>
	 *   <li>{@code (\s*+)}            – leading indentation</li>
	 *   <li>{@code ([A-Za-z_]\w*+)}   – first token (type <em>or</em> keyword –
	 *       keywords are filtered in Java code)</li>
	 *   <li>{@code (\s++)}            – whitespace between type and name</li>
	 *   <li>{@code ([A-Za-z_]\w*+)}   – action / definition name</li>
	 *   <li>{@code (\s*+)}            – whitespace before {@code \{}</li>
	 * </ol>
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*+)(?!//)"                        // possessive indent, skip comment lines
			+ "([A-Za-z_]\\w*+)"                    // group 2: first token (type or keyword)
			+ "(?:<[^>]*+(?:<[^>]*+>[^>]*+)*+>)?"   // optional generic type param
			+ "(\\s++)"                              // group 3: mandatory whitespace
			+ "([A-Za-z_]\\w*+)"                    // group 4: identifier name
			+ "(?!\\s*+\\()"                         // NOT already followed by '('
			+ "(\\s*+)"                              // group 5: whitespace before '{'
			+ "(?=\\{)",                             // must be followed by '{'
			Pattern.MULTILINE);

	// -----------------------------------------------------------------------
	// IFileTransformer
	// -----------------------------------------------------------------------

	@Override
	public String transform(final String content) {
		final Matcher m = PATTERN.matcher(content);
		if (!m.find()) { return content; }
		// Reset and do a full replacement pass
		return PATTERN.matcher(content).replaceAll(mr -> {
			// Skip GAML statement keywords (if, reflex, aspect, ask, …)
			if (EXCLUDED_KEYWORDS.contains(mr.group(2))) {
				return mr.group(0);
			}
			// Insert "()" between the identifier and the whitespace-before-brace
			final String full = mr.group(0);
			final String wsBeforeBrace = mr.group(5);
			return full.substring(0, full.length() - wsBeforeBrace.length())
					+ "()" + wsBeforeBrace;
		});
	}
}
