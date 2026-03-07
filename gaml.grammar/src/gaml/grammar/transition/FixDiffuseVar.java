package gaml.grammar.transition;

import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that replaces occurrences of {@code diffuse var:}
 * (with any surrounding whitespace) by {@code diffuse } in GAML source files.
 *
 * <p>Examples:</p>
 * <pre>
 *   diffuse var: heat          →  diffuse heat
 *   diffuse  var:   my_grid    →  diffuse my_grid
 * </pre>
 *
 * <p>The transformation is applied to the whole file content at once using
 * a single regex substitution.</p>
 */
public class FixDiffuseVar implements IFileTransformer {

	/**
	 * Pattern matching {@code diffuse} followed by one or more whitespace
	 * characters, then {@code var:} followed by one or more whitespace characters.
	 */
	private static final Pattern PATTERN = Pattern.compile("\\bdiffuse\\s+var:\\s+");

	/** The string that replaces every match of {@link #PATTERN}. */
	private static final String REPLACEMENT = "diffuse ";

	/**
	 * Apply the {@code diffuse var:} → {@code diffuse} transformation to the
	 * given source content.
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text with all {@code diffuse var:} occurrences
	 *         replaced; the original string is returned when no match is found
	 */
	@Override
	public String transform(final String content) {
		return PATTERN.matcher(content).replaceAll(REPLACEMENT);
	}
}
