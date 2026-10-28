/*******************************************************************************************************
 *
 * ManifestText.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal, formatting-preserving editor for {@code META-INF/MANIFEST.MF} text. Only the edited header is rewritten
 * (using the "one value per continuation line" layout used across the GAMA code base); every other line is kept
 * verbatim.
 */
public final class ManifestText {

	private ManifestText() {}

	/**
	 * Returns the raw value of a header (continuation lines joined), or {@code null} if absent.
	 */
	public static String get(final String manifest, final String header) {
		List<String> lines = lines(manifest);
		int[] range = find(lines, header);
		if (range == null) return null;
		StringBuilder sb = new StringBuilder(lines.get(range[0]).substring(header.length() + 1).stripLeading());
		for (int i = range[0] + 1; i < range[1]; i++) { sb.append(lines.get(i).substring(1)); }
		return sb.toString().strip();
	}

	/**
	 * Returns the comma-separated elements of a header (attributes/directives are kept with their element), or an
	 * empty list if the header is absent.
	 */
	public static List<String> getList(final String manifest, final String header) {
		return splitTopLevel(get(manifest, header));
	}

	/**
	 * Returns the bare names of the elements of a header (i.e. without attributes or directives).
	 */
	public static List<String> getNames(final String manifest, final String header) {
		List<String> result = new ArrayList<>();
		for (String s : getList(manifest, header)) { result.add(s.split(";", 2)[0].strip()); }
		return result;
	}

	/**
	 * Replaces (or appends) a header with the given elements, one per line.
	 */
	public static String setList(final String manifest, final String header, final List<String> values) {
		List<String> lines = lines(manifest);
		while (!lines.isEmpty() && lines.getLast().isBlank()) { lines.removeLast(); }
		List<String> block = new ArrayList<>();
		for (int i = 0; i < values.size(); i++) {
			String v = values.get(i) + (i < values.size() - 1 ? "," : "");
			block.add(i == 0 ? header + ": " + v : " " + v);
		}
		int[] range = find(lines, header);
		if (range == null) {
			lines.addAll(block);
		} else {
			for (int i = range[1] - 1; i >= range[0]; i--) { lines.remove(i); }
			lines.addAll(range[0], block);
		}
		return String.join("\n", lines) + "\n";
	}

	/**
	 * Splits a header value on commas that are not inside double quotes.
	 */
	static List<String> splitTopLevel(final String value) {
		List<String> result = new ArrayList<>();
		if (value == null || value.isBlank()) return result;
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		for (char c : value.toCharArray()) {
			if (c == '"') { quoted = !quoted; }
			if (c == ',' && !quoted) {
				if (!current.toString().isBlank()) { result.add(current.toString().strip()); }
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		if (!current.toString().isBlank()) { result.add(current.toString().strip()); }
		return result;
	}

	private static List<String> lines(final String manifest) {
		return new ArrayList<>(List.of(manifest.replace("\r\n", "\n").split("\n", -1)));
	}

	/** Returns [start, end) of the header block, or null. */
	private static int[] find(final List<String> lines, final String header) {
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).regionMatches(true, 0, header + ":", 0, header.length() + 1)) {
				int end = i + 1;
				while (end < lines.size() && lines.get(end).startsWith(" ")) { end++; }
				return new int[] { i, end };
			}
		}
		return null;
	}

}
