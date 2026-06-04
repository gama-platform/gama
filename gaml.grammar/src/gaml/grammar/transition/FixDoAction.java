package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that replaces the {@code action:} facet form of
 * {@code do} statements with the direct action-name form.
 *
 * <h3>Handled forms</h3>
 * <pre>
 *   do action: actionName;
 *   do action: actionName with: (...);
 * </pre>
 *
 * <h3>Result</h3>
 * <pre>
 *   do actionName;
 *   do actionName with: (...);
 * </pre>
 *
 * <h3>Examples</h3>
 * <pre>
 *   do action: move;                   →  do move;
 *   do action: my_action;              →  do my_action;
 *   do action: foo with: (bar: 1);     →  do foo with: (bar: 1);
 * </pre>
 *
 * <h3>Idempotency</h3>
 * <p>Only lines where {@code do} is immediately followed by the {@code action:}
 * facet are matched. The result never contains {@code action:} after {@code do},
 * so applying the transformer twice produces the same result as applying it
 * once.</p>
 *
 * <p>This transformer must be applied <em>before</em> {@link FixDoParentheses}
 * so that the resulting bare {@code do actionName;} statements are then given
 * the required empty parentheses.</p>
 */
public class FixDoAction implements IFileTransformer {

	/**
	 * Regex breakdown:
	 * <pre>
	 *   ^(\s*+)           → group 1: leading indentation (possessive)
	 *   (?!//)             → skip comment lines
	 *   do                → literal keyword
	 *   (\s++)             → group 2: mandatory whitespace between do and action:
	 *   action:\s*+        → action: facet (consumed, whitespace after it dropped)
	 *   ([A-Za-z_]\w*+)   → group 3: action name identifier
	 * </pre>
	 */
	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*+)(?!//)"
			+ "do"
			+ "(\\s++)"               // group 2: whitespace between do and action:
			+ "action:\\s*+"          // action: facet
			+ "([A-Za-z_]\\w*+)",     // group 3: action name
			Pattern.MULTILINE);

	@Override
	public String transform(final String content) {
		final Matcher m = PATTERN.matcher(content);
		if (!m.find()) { return content; }
		return PATTERN.matcher(content).replaceAll(mr ->
				mr.group(1) + "do" + mr.group(2) + mr.group(3));
	}
}
