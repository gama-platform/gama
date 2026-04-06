package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms old GAML set statements:
 *
 *   set x value:5;
 *   set x <- 5;
 *
 * into:
 *
 *   x <- 5;
 *
 * <h3>Handled forms</h3>
 * <ul>
 *   <li>{@code set var value:expr;}</li>
 *   <li>{@code set var <- expr;}</li>
 * </ul>
 *
 * <h3>Idempotency</h3>
 * Only lines starting with {@code set} are matched.
 */
public class FixSetStatement implements IFileTransformer {

	/**
	 * Regex breakdown:
	 *
	 * ^(\s*)                  → group 1: indentation
	 * set\s+                  → keyword
	 * ([A-Za-z_][A-Za-z0-9_]*)→ group 2: variable name
	 * \s+                     → spaces
	 * (?:value:\s*|<-\s*)     → either "value:" or "<-"
	 * (.+?)                   → group 3: expression
	 * \s*;                    → semicolon
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*)set\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+(?:value:\\s*|<-\\s*)(.+?)\\s*;",
			Pattern.MULTILINE);

	@Override
	public String transform(final String content) {
		final Matcher m = PATTERN.matcher(content);
		if (!m.find()) { return content; }

		return PATTERN.matcher(content).replaceAll(mr -> {
			final String indent = mr.group(1);
			final String varName = mr.group(2);
			final String value = mr.group(3);

			return indent + varName + " <- " + value + ";";
		});
	}
}