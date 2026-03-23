package gaml.grammar.transition;

/**
 * {@link IFileTransformer} that removes the outermost curly braces surrounding
 * the body of {@code ->} (arrow) expressions in GAML source files.
 *
 * <p>Examples:</p>
 * <pre>
 *   action my_action -> { do something; };
 *       → action my_action -> do something;
 *
 *   -> { if (true) { do A; } };
 *       → -> if (true) { do A; };
 * </pre>
 *
 * <p>The parser correctly handles:</p>
 * <ul>
 *   <li>Nested curly braces (only the outermost pair is removed).</li>
 *   <li>String literals (single and double quotes, with escape sequences).</li>
 *   <li>Single-line ({@code //}) and multi-line ({@code /* … *}{@code /})
 *       comments.</li>
 *   <li>Nested arrow expressions (the inner content is processed
 *       recursively).</li>
 *   <li>Trailing semicolons inside the braces to avoid producing
 *       {@code do A;;} double-semicolons.</li>
 * </ul>
 */
public class FixArrowBraces implements IFileTransformer {

	/**
	 * Parse {@code text} and remove outermost curly braces after every arrow
	 * ({@code ->}) operator.
	 *
	 * <p>String literals and comments are tracked to avoid counting false braces.
	 * Nested curly braces are handled robustly via a depth counter. The inner
	 * content is processed recursively so that nested arrow expressions are also
	 * transformed.</p>
	 *
	 * @param text
	 *            the full source text to process; never {@code null}
	 * @return the transformed text; never {@code null}
	 */
	// Package-private so it can be called recursively and tested directly.
	String removeArrowBraces(final String text) {
		final StringBuilder result = new StringBuilder(text.length());
		int i = 0;
		final int n = text.length();

		while (i < n) {
			// 1. Skip double-quoted string literals
			if (text.charAt(i) == '"') {
				final int start = i++;
				while (i < n && text.charAt(i) != '"') {
					if (text.charAt(i) == '\\') { i++; } // skip escaped char
					i++;
				}
				if (i < n) { i++; } // consume closing quote
				result.append(text, start, i);
				continue;
			}

			// 2. Skip single-quoted string literals
			if (text.charAt(i) == '\'') {
				final int start = i++;
				while (i < n && text.charAt(i) != '\'') {
					if (text.charAt(i) == '\\') { i++; }
					i++;
				}
				if (i < n) { i++; }
				result.append(text, start, i);
				continue;
			}

			// 3. Skip single-line comments (//)
			if (i + 1 < n && text.charAt(i) == '/' && text.charAt(i + 1) == '/') {
				final int start = i;
				while (i < n && text.charAt(i) != '\n') { i++; }
				result.append(text, start, i);
				continue;
			}

			// 4. Skip multi-line comments (/* … */)
			if (i + 1 < n && text.charAt(i) == '/' && text.charAt(i + 1) == '*') {
				final int start = i;
				i += 2;
				while (i < n - 1 && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) {
					i++;
				}
				if (i < n - 1) { i += 2; } else { i = n; }
				result.append(text, start, i);
				continue;
			}

			// 5. Arrow operator "->"
			if (i + 1 < n && text.charAt(i) == '-' && text.charAt(i + 1) == '>') {
				result.append("->");
				i += 2;

				// Consume whitespace after the arrow
				final StringBuilder ws = new StringBuilder();
				while (i < n && Character.isWhitespace(text.charAt(i))) {
					ws.append(text.charAt(i++));
				}

				// If the next non-whitespace character is '{', remove the braces
				if (i < n && text.charAt(i) == '{') {
					result.append(' '); // standardise to a single space

					int braceDepth = 1;
					int j = i + 1;
					final int contentStart = j;

					// Scan forward to find the matching closing brace,
					// skipping strings and comments inside the block
					while (j < n && braceDepth > 0) {
						if (text.charAt(j) == '"' || text.charAt(j) == '\'') {
							final char q = text.charAt(j++);
							while (j < n && text.charAt(j) != q) {
								if (text.charAt(j) == '\\') { j++; }
								j++;
							}
							if (j < n) { j++; }
							continue;
						}
						if (j + 1 < n && text.charAt(j) == '/' && text.charAt(j + 1) == '/') {
							while (j < n && text.charAt(j) != '\n') { j++; }
							continue;
						}
						if (j + 1 < n && text.charAt(j) == '/' && text.charAt(j + 1) == '*') {
							j += 2;
							while (j < n - 1
									&& !(text.charAt(j) == '*' && text.charAt(j + 1) == '/')) {
								j++;
							}
							if (j < n - 1) { j += 2; } else { j = n; }
							continue;
						}
						if (text.charAt(j) == '{') {
							braceDepth++;
						} else if (text.charAt(j) == '}') {
							braceDepth--;
							if (braceDepth == 0) { break; }
						}
						j++;
					}

					if (braceDepth == 0) {
						// Extract and clean inner content
						String inner = text.substring(contentStart, j).strip();
						// Prevent double semicolons: -> { do A; }; → -> do A;
						if (inner.endsWith(";")) {
							inner = inner.substring(0, inner.length() - 1).stripTrailing();
						}
						// Recursively handle nested arrows
						result.append(removeArrowBraces(inner));
						i = j + 1; // skip past the closing '}'
					} else {
						// Unmatched brace — revert safely
						result.append(ws);
						result.append('{');
						i++; // skip just the '{'
					}
				} else {
					// Arrow without a following '{' — keep whitespace as-is
					result.append(ws);
				}
				continue;
			}

			result.append(text.charAt(i++));
		}

		return result.toString();
	}

	/**
	 * Apply the arrow-braces removal transformation to the given source content.
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text; the original string is returned when no
	 *         change was made
	 */
	@Override
	public String transform(final String content) {
		final String result = removeArrowBraces(content);
		return result.equals(content) ? content : result;
	}
}
