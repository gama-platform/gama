package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that transforms the old GAML {@code list of: T}
 * type syntax into the modern generic {@code list<T>} form.
 *
 * <h3>Handled forms</h3>
 * <pre>
 *   list of: typeName
 *   list of: paramType&lt;innerType&gt;
 * </pre>
 *
 * <h3>Result</h3>
 * <pre>
 *   list&lt;typeName&gt;
 *   list&lt;paramType&lt;innerType&gt;&gt;
 * </pre>
 *
 * <h3>Examples</h3>
 * <pre>
 *   list of: int             →  list&lt;int&gt;
 *   list of: float           →  list&lt;float&gt;
 *   list of: my_species      →  list&lt;my_species&gt;
 *   list of: list&lt;int&gt;       →  list&lt;list&lt;int&gt;&gt;
 * </pre>
 *
 * <h3>Idempotency</h3>
 * <p>The pattern requires the {@code of:} facet after the {@code list} keyword.
 * The result uses angle-bracket syntax and never contains {@code of:}, so
 * applying the transformer twice produces the same result as applying it
 * once.</p>
 *
 * <p>This transformer should be applied <em>before</em> {@link FixLetStatement}
 * so that type expressions such as {@code list of: int} inside a
 * {@code let … type: … value: …} statement are already normalised before
 * {@link FixLetStatement} restructures the whole declaration.</p>
 */
public class FixListOf implements IFileTransformer {

	/**
	 * Regex breakdown:
	 * <pre>
	 *   \blist\b                    → list keyword (word boundaries prevent
	 *                                  matching inside longer identifiers)
	 *   \s++of:\s*+                 → of: facet with surrounding whitespace
	 *   (                           → group 1: type expression
	 *     [A-Za-z_]\w*+             →   simple identifier (possessive)
	 *     (?:&lt;[^&gt;]++&gt;)?  →   optional single-level generic parameter
	 *   )
	 * </pre>
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"\\blist\\b"
			+ "\\s++of:\\s*+"
			+ "([A-Za-z_]\\w*+(?:<[^>]++>)?)");  // group 1: type expression

	@Override
	public String transform(final String content) {
		final Matcher m = PATTERN.matcher(content);
		if (!m.find()) { return content; }
		return PATTERN.matcher(content).replaceAll(mr -> "list<" + mr.group(1) + ">");
	}
}
