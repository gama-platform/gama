/*******************************************************************************************************
 *
 * JavaHeaderText.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Text-only header edits; everything outside the GAMA notice is preserved verbatim. */
public final class JavaHeaderText {

	private static final Pattern IDENTITY = Pattern.compile(
			"(?m)(\\*\\h+)[^\\r\\n]*?\\.java,\\h*(?:in\\h+[^,\\r\\n]+,\\h*)?");
	private static final Pattern VERSION = Pattern.compile("\\(v\\.[^)\\r\\n]+\\)");
	private static final Pattern PLATFORM = Pattern.compile("simulation[\\s*]+platform");
	private static final Pattern COPYRIGHT = Pattern.compile(
			"(\\(c\\)\\h+)(\\d{4})(?:-\\d{4})?(?=\\h+[^\\r\\n]*UMMIS[CS]O)");

	private JavaHeaderText() {}

	public static String validate(final String version, final String year) {
		if (!version.matches("[A-Za-z0-9][A-Za-z0-9._+-]*")) {
			return "Enter a GAMA version using letters, digits, dots, hyphens, underscores or '+'.";
		}
		if (!year.matches("\\d{4}") || Integer.parseInt(year) < 2007) {
			return "Enter a four-digit copyright end year, at least 2007.";
		}
		return null;
	}

	public static String header(final String filename, final String plugin, final String version, final String year,
			final String newline) {
		return """
				/*******************************************************************************************************
				 *
				 * %s, %sis part of the source code of the GAMA modeling and simulation
				 * platform (v.%s).
				 *
				 * (c) 2007-%s UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
				 *
				 * Visit https://github.com/gama-platform/gama for license information and contacts.
				 *
				 ********************************************************************************************************/
				""".formatted(filename, plugin == null ? "" : "in " + plugin + ", ", version, year).replace("\n", newline);
	}

	public static String update(final String source, final String filename, final String plugin,
			final String version, final String year) {
		String error = validate(version, year);
		if (error != null) { throw new IllegalArgumentException(error); }
		String newline = source.contains("\r\n") ? "\r\n" : source.contains("\n") ? "\n"
				: source.contains("\r") ? "\r" : "\n";
		int bom = source.startsWith("\uFEFF") ? 1 : 0;
		int start = bom;
		while (start < source.length()) {
			while (start < source.length() && Character.isWhitespace(source.charAt(start))) { start++; }
			if (source.startsWith("//", start)) {
				while (start < source.length() && source.charAt(start) != '\n' && source.charAt(start) != '\r') {
					start++;
				}
				continue;
			}
			if (!source.startsWith("/*", start)) { break; }
			int end = source.indexOf("*/", start + 2);
			if (end < 0) { break; }
			end += 2;
			String notice = source.substring(start, end);
			if (notice.contains("GAMA") && notice.contains(newline) && PLATFORM.matcher(notice).find()) {
				Matcher identity = IDENTITY.matcher(notice);
				if (identity.find()) {
					notice = identity.replaceFirst(Matcher.quoteReplacement("* " + filename + ", in " + plugin + ", "));
				} else {
					int firstLine = notice.indexOf(newline);
					if (firstLine >= 0) {
						notice = notice.substring(0, firstLine) + newline + " * " + filename + ", in " + plugin
								+ ", GAMA source file." + notice.substring(firstLine);
					}
				}
				Matcher v = VERSION.matcher(notice);
				if (v.find()) {
					notice = v.replaceFirst(Matcher.quoteReplacement("(v." + version + ")"));
				} else {
					Matcher platform = PLATFORM.matcher(notice);
					if (platform.find()) {
						notice = platform.replaceFirst(Matcher.quoteReplacement(platform.group() + " (v." + version + ")"));
					}
				}
				Matcher copyright = COPYRIGHT.matcher(notice);
				StringBuilder updated = new StringBuilder();
				while (copyright.find()) {
					int first = Integer.parseInt(copyright.group(2));
					if (first > Integer.parseInt(year)) {
						throw new IllegalArgumentException(filename + ": copyright start year is later than " + year);
					}
					copyright.appendReplacement(updated, Matcher.quoteReplacement(copyright.group(1)
							+ copyright.group(2) + (copyright.group(2).equals(year) ? "" : "-" + year)));
				}
				copyright.appendTail(updated);
				notice = updated.toString();
				if (!COPYRIGHT.matcher(notice).find()) {
					String credit = " * (c) 2007-" + year
							+ " UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)" + newline;
					int closingLine = notice.lastIndexOf(newline);
					if (closingLine >= 0) {
						notice = notice.substring(0, closingLine) + newline + " *" + newline + credit
								+ notice.substring(closingLine);
					} else {
						notice = notice.substring(0, 2) + newline + credit + notice.substring(2);
					}
				}
				return source.substring(0, start) + notice + source.substring(end);
			}
			start = end;
		}
		return source.substring(0, bom) + header(filename, plugin, version, year, newline) + newline
				+ source.substring(bom);
	}
}
