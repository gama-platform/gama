package gaml.grammar.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that rewrites {@code display} and {@code experiment}
 * statements whose name is given as a plain double-quoted string into the new
 * syntax that separates the identifier from the human-readable title.
 *
 * <p>Examples:</p>
 * <pre>
 *   display "3 Simulations"
 *       → display _3_Simulations title: "3 Simulations"
 *
 *   display "My Disp" title: "Custom Title"
 *       → display My_Disp title: "Custom Title"
 *
 *   experiment "Hello World!"
 *       → experiment Hello_World_ title: "Hello World!"
 * </pre>
 *
 * <h3>Identifier-building rules</h3>
 * <ol>
 *   <li>Every run of non-alphanumeric characters is replaced by a single
 *       {@code _}.</li>
 *   <li>Leading and trailing underscores are stripped.</li>
 *   <li>If the resulting identifier starts with a digit, a {@code _} is
 *       prepended.</li>
 * </ol>
 *
 * <p>The transformation is applied line by line so that each line is processed
 * independently.</p>
 */
public class FixDisplayExperimentNames implements IFileTransformer {

	/**
	 * Pattern matching {@code display} or {@code experiment} followed by a
	 * double-quoted name.
	 * <ul>
	 *   <li>Group 1 – the keyword ({@code display} or {@code experiment})</li>
	 *   <li>Group 2 – the raw text inside the quotes</li>
	 * </ul>
	 */
	private static final Pattern PATTERN =
			Pattern.compile("\\b(display|experiment)\\s+\"([^\"]+)\"");

	/**
	 * Pattern used to detect whether a line already contains a {@code title:}
	 * facet, in which case no new {@code title:} attribute is added.
	 */
	private static final Pattern HAS_TITLE = Pattern.compile("\\btitle\\s*:");

	/**
	 * Pattern used inside {@link #makeIdentifier(String)} to replace runs of
	 * non-alphanumeric characters with a single underscore.
	 */
	private static final Pattern NON_ALNUM = Pattern.compile("[^A-Za-z0-9]+");

	/**
	 * Build a valid GAML identifier from the raw display/experiment name.
	 *
	 * <p>The rules applied are:</p>
	 * <ol>
	 *   <li>Every run of non-alphanumeric characters is replaced by {@code _}.</li>
	 *   <li>Leading and trailing underscores are stripped.</li>
	 *   <li>If the first character is a digit, a {@code _} is prepended.</li>
	 * </ol>
	 *
	 * @param raw
	 *            the original string as it appears inside the double quotes;
	 *            never {@code null}
	 * @return a string suitable for use as a GAML identifier; never {@code null}
	 */
	static String makeIdentifier(final String raw) {
		String ident = NON_ALNUM.matcher(raw).replaceAll("_");
		// Strip leading and trailing underscores
		ident = ident.replaceAll("^_+|_+$", "");
		// Prepend underscore if the identifier starts with a digit
		if (!ident.isEmpty() && Character.isDigit(ident.charAt(0))) {
			ident = "_" + ident;
		}
		return ident;
	}

	/**
	 * Transform a single line by rewriting any {@code display "…"} or
	 * {@code experiment "…"} occurrence it contains.
	 *
	 * <p>If the line already contains a {@code title:} facet the identifier is
	 * substituted but no new {@code title:} attribute is appended.</p>
	 *
	 * @param line
	 *            a single line of source text; never {@code null}
	 * @return the (possibly transformed) line; never {@code null}
	 */
	String transformLine(final String line) {
		final boolean hasTitle = HAS_TITLE.matcher(line).find();
		final Matcher m = PATTERN.matcher(line);
		if (!m.find()) { return line; }
		final StringBuffer sb = new StringBuffer();
		m.reset();
		while (m.find()) {
			final String keyword = m.group(1);
			final String raw = m.group(2);
			final String ident = makeIdentifier(raw);
			final String replacement = hasTitle
					? keyword + " " + ident
					: keyword + " " + ident + " title: \"" + raw + "\"";
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Apply the display/experiment name transformation to every line in the
	 * given source content.
	 *
	 * @param content
	 *            the full text of a GAML source file; never {@code null}
	 * @return the transformed text; the original string is returned when no
	 *         match is found
	 */
	@Override
	public String transform(final String content) {
		// Preserve the original line endings
		final String[] lines = content.split("(?<=\n)", -1);
		final StringBuilder sb = new StringBuilder(content.length());
		boolean changed = false;
		for (final String line : lines) {
			final String newLine = transformLine(line);
			sb.append(newLine);
			if (!newLine.equals(line)) { changed = true; }
		}
		return changed ? sb.toString() : content;
	}
}
