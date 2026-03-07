package gaml.grammar.transition;

import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that replaces occurrences of {@code transition to:}
 * (with any surrounding whitespace) by {@code transition } in GAML source files.
 *
 * <p>Examples:</p>
 * <pre>
 *   transition to: idle           →  transition idle
 *   transition  to:   next_state  →  transition next_state
 * </pre>
 *
 * <p>The transformation is applied to the whole file content at once using
 * a single regex substitution.</p>
 */
public class FixTransitionTo implements IFileTransformer {

	/**
	 * Pattern matching {@code transition} followed by one or more whitespace
	 * characters, then {@code to:} followed by one or more whitespace characters.
	 */
	private static final Pattern PATTERN = Pattern.compile("\\btransition\\s+to:\\s+");

	/** The string that replaces every match of {@link #PATTERN}. */
	private static final String REPLACEMENT = "transition ";

	/**
	 * Apply the {@code transition to:} → {@code transition} transformation to the
	 * given source content.
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text with all {@code transition to:} occurrences
	 *         replaced; the original string is returned when no match is found
	 */
	@Override
	public String transform(final String content) {
		return PATTERN.matcher(content).replaceAll(REPLACEMENT);
	}
}
