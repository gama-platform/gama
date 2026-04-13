package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that transforms old GAML {@code let} statements with
 * an explicit {@code type:} facet into modern typed variable declarations.
 *
 * <h3>Handled forms</h3>
 * <ul>
 *   <li>{@code let varName type: aType value: expr;}</li>
 *   <li>{@code let varName type: aType <- expr;}</li>
 * </ul>
 *
 * <h3>Result</h3>
 * <pre>
 *   aType varName &lt;- expr;
 * </pre>
 *
 * <h3>Examples</h3>
 * <pre>
 *   let x type: int value: 5;
 *       → int x &lt;- 5;
 *
 *   let myList type: list&lt;int&gt; value: [1,2,3];
 *       → list&lt;int&gt; myList &lt;- [1,2,3];
 *
 *   let m type: map&lt;string, int&gt; &lt;- create_map();
 *       → map&lt;string, int&gt; m &lt;- create_map();
 *
 *   let agent1 type: my_species value: one_of(my_species);
 *       → my_species agent1 &lt;- one_of(my_species);
 * </pre>
 *
 * <h3>Idempotency</h3>
 * <p>Only lines starting with {@code let} followed by a {@code type:} facet are
 * matched. The result never starts with {@code let}, so applying the
 * transformer twice produces the same result as applying it once.</p>
 */
public class FixLetStatement implements IFileTransformer {

	/**
	 * Regex breakdown:
	 * <pre>
	 *   ^(\s*+)                       → group 1: indentation (possessive)
	 *   (?!//)                        → skip comment lines
	 *   let\s++                       → keyword
	 *   ([A-Za-z_]\w*+)              → group 2: variable name
	 *   \s++type:\s*+                 → type facet
	 *   (.+?)                         → group 3: type expression (lazy)
	 *   \s++(?:value:\s*+|&lt;-\s*+) → value facet or arrow
	 *   (.+?)                         → group 4: value expression (lazy)
	 *   \s*;                          → trailing semicolon
	 * </pre>
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*+)(?!//)"
			+ "let\\s++"
			+ "([A-Za-z_]\\w*+)"           // group 2: variable name
			+ "\\s++type:\\s*+"
			+ "(.+?)"                       // group 3: type (lazy – stops at value:/arrow)
			+ "\\s++(?:value:\\s*+|<-\\s*+)"
			+ "(.+?)"                       // group 4: value expression
			+ "\\s*;",
			Pattern.MULTILINE);

	@Override
	public String transform(final String content) {
		final Matcher m = PATTERN.matcher(content);
		if (!m.find()) { return content; }

		return PATTERN.matcher(content).replaceAll(mr -> {
			final String indent  = mr.group(1);
			final String varName = mr.group(2);
			final String type    = mr.group(3);
			final String value   = mr.group(4);

			return indent + type + " " + varName + " <- " + value + ";";
		});
	}
}
