package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that transforms {@code with:} argument lists in
 * GAML source files from the old bracket / double-colon pair syntax to the new
 * parenthesis / single-colon syntax.
 *
 * <p>Examples:</p>
 * <pre>
 *   with: [with_viz::true, step::1#s, name::"Simulation step=1s"]
 *       → with: (with_viz:true, step:1#s, name:"Simulation step=1s")
 *
 *   with: [agents::ag, values::[1,2,3]]
 *       → with: (agents:ag, values:[1,2,3])
 * </pre>
 *
 * <p>Multi-line {@code with: [...]} blocks are also handled correctly because
 * the transformation operates on the entire file text at once.</p>
 *
 * <h3>Rules applied</h3>
 * <ol>
 *   <li>The opening {@code [} immediately following {@code with:} (with optional
 *       whitespace, including newlines) is replaced by {@code (}.</li>
 *   <li>The matching closing {@code ]} (respecting nested brackets and quoted
 *       strings) is replaced by {@code )}.</li>
 *   <li>Every {@code ::} that sits directly inside the outermost list (i.e. not
 *       inside a nested {@code [...]} or a quoted string) is replaced by
 *       {@code :}.</li>
 * </ol>
 *
 * <p>Nested {@code [...]} expressions and their contents are left completely
 * untouched.</p>
 */
public class FixWithPairs implements IFileTransformer {

	/**
	 * Pattern that finds {@code with:} followed by optional whitespace (including
	 * newlines) immediately before an opening {@code [}. The look-ahead ensures
	 * the {@code [} is present but not consumed, so that {@link #findMatchingBracket}
	 * can locate it precisely via {@link Matcher#end()}.
	 */
	private static final Pattern WITH_PREFIX = Pattern.compile("with:\\s*(?=\\[)");

	/**
	 * Find the index of the {@code ]} that closes the {@code [} at position
	 * {@code start} inside {@code text}.
	 *
	 * <p>The search respects:</p>
	 * <ul>
	 *   <li>Nested {@code [...]} brackets (depth tracking).</li>
	 *   <li>Double-quoted strings (characters inside quotes are ignored).</li>
	 *   <li>Newlines (the search continues across line boundaries).</li>
	 * </ul>
	 *
	 * @param text
	 *            the full source text; never {@code null}
	 * @param start
	 *            the index of the opening {@code [} character
	 * @return the index of the matching {@code ]}, or {@code -1} if not found
	 */
	static int findMatchingBracket(final String text, final int start) {
		int depth = 0;
		boolean inString = false;
		for (int i = start; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if (inString) {
				if (ch == '\\') {
					i++; // skip escaped character
				} else if (ch == '"') {
					inString = false;
				}
			} else {
				if (ch == '"') {
					inString = true;
				} else if (ch == '[') {
					depth++;
				} else if (ch == ']') {
					depth--;
					if (depth == 0) { return i; }
				}
			}
		}
		return -1;
	}

	/**
	 * Replace every top-level {@code ::} inside {@code content} with {@code :}.
	 *
	 * <p>Characters inside nested {@code [...]} brackets or double-quoted strings
	 * are left completely untouched.</p>
	 *
	 * @param content
	 *            the raw text between the outermost {@code [} and {@code ]}
	 *            delimiters (not including those delimiters themselves);
	 *            never {@code null}
	 * @return the transformed content; never {@code null}
	 */
	static String transformContent(final String content) {
		final StringBuilder result = new StringBuilder(content.length());
		int depth = 0;
		boolean inString = false;
		for (int i = 0; i < content.length(); i++) {
			final char ch = content.charAt(i);
			if (inString) {
				if (ch == '\\') {
					result.append(ch);
					result.append(content.charAt(i + 1));
					i++;
					continue;
				}
				if (ch == '"') { inString = false; }
				result.append(ch);
			} else {
				if (ch == '"') {
					inString = true;
					result.append(ch);
				} else if (ch == '[') {
					depth++;
					result.append(ch);
				} else if (ch == ']') {
					depth--;
					result.append(ch);
				} else if (ch == ':' && depth == 0
						&& i + 1 < content.length() && content.charAt(i + 1) == ':') {
					// Top-level "::" → replace with single ":"
					result.append(':');
					i++; // skip the second ':'
				} else {
					result.append(ch);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Apply the {@code with: [...]} → {@code with: (...)} transformation to the
	 * given source content.
	 *
	 * <p>Uses a bracket-aware parser so that nested {@code [...]} inside the list
	 * are preserved intact, and multi-line {@code with: [...]} blocks are handled
	 * correctly.</p>
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text; the original string is returned when no
	 *         match is found
	 */
	@Override
	public String transform(final String content) {
		final StringBuilder result = new StringBuilder(content.length());
		int pos = 0;
		final Matcher m = WITH_PREFIX.matcher(content);
		boolean changed = false;

		while (m.find()) {
			// Skip matches that fall inside an already-consumed region
			if (m.start() < pos) { continue; }

			final int bracketStart = m.end(); // index of the '['
			final int bracketEnd = findMatchingBracket(content, bracketStart);
			if (bracketEnd == -1) { continue; } // no matching ']' — skip

			// Append everything up to the '['
			result.append(content, pos, bracketStart);
			// Opening '(' instead of '['
			result.append('(');
			// Transformed inner content
			result.append(transformContent(content.substring(bracketStart + 1, bracketEnd)));
			// Closing ')' instead of ']'
			result.append(')');
			pos = bracketEnd + 1;
			changed = true;
		}

		if (!changed) { return content; }
		result.append(content, pos, content.length());
		return result.toString();
	}
}
