package gaml.grammar.transition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that replaces the {@code image} layer keyword by
 * {@code picture} inside {@code display} blocks in GAML source files.
 *
 * <p>The replacement is context-sensitive: {@code image} is only replaced when
 * it appears as a statement keyword (i.e. at the start of a non-blank,
 * non-comment line, possibly preceded by whitespace) <em>inside</em> a
 * {@code display { ... }} block. Occurrences of {@code image} outside display
 * blocks — as a variable name, inside a string literal, in a comment, or as
 * part of a larger identifier such as {@code image_file} — are left
 * untouched.</p>
 *
 * <p>Examples:</p>
 * <pre>
 *   display my_display {
 *       image "background.jpg";
 *       image my_matrix refresh: true;
 *   }
 *
 *   becomes:
 *
 *   display my_display {
 *       picture "background.jpg";
 *       picture my_matrix refresh: true;
 *   }
 * </pre>
 */
public class FixImageToPicture implements IFileTransformer {

	/**
	 * Pattern that matches a line containing the {@code display} keyword
	 * (outside of comments or blank lines).
	 */
	private static final Pattern DISPLAY_OPEN = Pattern.compile("\\bdisplay\\b");

	/**
	 * Pattern that matches the {@code image} keyword at the very start of a
	 * statement (after optional indentation), ensuring it is not part of a
	 * larger identifier (e.g. {@code image_file}).
	 * <ul>
	 *   <li>Group 1 – leading whitespace</li>
	 * </ul>
	 */
	private static final Pattern IMAGE_KEYWORD = Pattern.compile("^(\\s*)image\\b");

	/**
	 * Compute the net brace delta of a single source line, ignoring characters
	 * inside string literals and single-line comments.
	 *
	 * @param line
	 *            a single line of source text; never {@code null}
	 * @return the number of {@code '{'} characters minus the number of
	 *         {@code '}'} characters found outside strings and comments
	 */
	static int countBraces(final String line) {
		int delta = 0;
		boolean inString = false;
		for (int i = 0; i < line.length(); i++) {
			final char ch = line.charAt(i);
			if (inString) {
				if (ch == '\\') {
					i++; // skip escaped character
				} else if (ch == '"') {
					inString = false;
				}
			} else {
				if (ch == '"') {
					inString = true;
				} else if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					break; // single-line comment: ignore the rest
				} else if (ch == '{') {
					delta++;
				} else if (ch == '}') {
					delta--;
				}
			}
		}
		return delta;
	}

	/**
	 * Return {@code true} if the given line is blank or a pure comment line
	 * (i.e. it carries no executable GAML statement).
	 *
	 * @param line
	 *            a single line of source text; never {@code null}
	 * @return {@code true} when the line is blank or starts with {@code //}
	 *         (after stripping leading whitespace)
	 */
	static boolean isCommentOrBlank(final String line) {
		final String stripped = line.stripLeading();
		return stripped.isEmpty() || stripped.startsWith("//");
	}

	/**
	 * Transform a list of lines by replacing {@code image} with
	 * {@code picture} where appropriate (inside {@code display} blocks only).
	 *
	 * <p>A stack ({@code displayStack}) tracks the brace depth at which each
	 * {@code display} block was opened so that nested display blocks are handled
	 * correctly.</p>
	 *
	 * @param lines
	 *            the original lines of the source file; never {@code null}
	 * @return a new list with the substitutions applied; if no substitution was
	 *         made the original list elements are preserved
	 */
	List<String> transformLines(final List<String> lines) {
		final List<String> result = new ArrayList<>(lines.size());
		// Each entry records the brace depth *before* the matching '{' was seen.
		final Deque<Integer> displayStack = new ArrayDeque<>();
		int braceDepth = 0;

		for (final String line : lines) {
			String newLine = line;
			final boolean insideDisplay = !displayStack.isEmpty();

			if (!isCommentOrBlank(line) && DISPLAY_OPEN.matcher(line).find()) {
				final int delta = countBraces(line);
				if (delta > 0) {
					// The opening '{' is on this same line
					displayStack.push(braceDepth);
				}
				braceDepth += delta;
			} else {
				braceDepth += countBraces(line);
			}

			// Pop display entries whose block has been closed
			while (!displayStack.isEmpty() && braceDepth <= displayStack.peek()) {
				displayStack.pop();
			}

			// Apply image → picture replacement inside display blocks
			if (insideDisplay && !isCommentOrBlank(line)) {
				newLine = IMAGE_KEYWORD.matcher(line).replaceFirst("$1picture");
			}

			result.add(newLine);
		}
		return result;
	}

	/**
	 * Apply the {@code image} → {@code picture} transformation to the given
	 * source content.
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text; the original string is returned when no
	 *         change was made
	 */
	@Override
	public String transform(final String content) {
		// Split preserving line endings
		final String[] arr = content.split("(?<=\n)", -1);
		final List<String> lines = new ArrayList<>(arr.length);
		for (final String s : arr) { lines.add(s); }

		final List<String> newLines = transformLines(lines);

		// Check whether any line changed
		boolean changed = false;
		for (int i = 0; i < lines.size(); i++) {
			if (!lines.get(i).equals(newLines.get(i))) {
				changed = true;
				break;
			}
		}
		if (!changed) { return content; }

		final StringBuilder sb = new StringBuilder(content.length());
		for (final String l : newLines) { sb.append(l); }
		return sb.toString();
	}
}
