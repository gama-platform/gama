/*******************************************************************************************************
 *
 * BuildPropertiesText.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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
 * Minimal, formatting-preserving editor for PDE {@code build.properties} files. Only the edited key is rewritten,
 * using the aligned "{@code key = a,\}" layout found in the GAMA plugins.
 */
public final class BuildPropertiesText {

	private BuildPropertiesText() {}

	/**
	 * Returns the comma-separated values of a key (empty if absent).
	 */
	public static List<String> getList(final String text, final String key) {
		List<String> lines = lines(text);
		int[] range = find(lines, key);
		List<String> result = new ArrayList<>();
		if (range == null) return result;
		StringBuilder sb = new StringBuilder();
		for (int i = range[0]; i < range[1]; i++) {
			String l = lines.get(i);
			if (i == range[0]) { l = l.substring(l.indexOf('=') + 1); }
			l = l.strip();
			if (l.endsWith("\\")) { l = l.substring(0, l.length() - 1); }
			sb.append(l);
		}
		for (String s : sb.toString().split(",")) { if (!s.isBlank()) { result.add(s.strip()); } }
		return result;
	}

	/**
	 * Replaces (or appends) a key with the given values.
	 */
	public static String setList(final String text, final String key, final List<String> values) {
		List<String> lines = lines(text);
		while (!lines.isEmpty() && lines.getLast().isBlank()) { lines.removeLast(); }
		String prefix = key + " = ";
		String indent = " ".repeat(prefix.length());
		List<String> block = new ArrayList<>();
		for (int i = 0; i < values.size(); i++) {
			boolean last = i == values.size() - 1;
			block.add((i == 0 ? prefix : indent) + values.get(i) + (last ? "" : ",\\"));
		}
		int[] range = find(lines, key);
		if (range == null) {
			lines.addAll(block);
		} else {
			for (int i = range[1] - 1; i >= range[0]; i--) { lines.remove(i); }
			lines.addAll(range[0], block);
		}
		return String.join("\n", lines) + "\n";
	}

	/**
	 * Adds the given values to a key if they are missing. Values are compared without trailing slashes.
	 */
	public static String addAll(final String text, final String key, final List<String> toAdd) {
		List<String> values = getList(text, key);
		boolean changed = false;
		for (String v : toAdd) {
			if (!containsPath(values, v)) {
				values.add(v);
				changed = true;
			}
		}
		return changed ? setList(text, key, values) : text;
	}

	/**
	 * Returns true if the list contains the given path, ignoring trailing slashes.
	 */
	public static boolean containsPath(final List<String> values, final String path) {
		String p = strip(path);
		for (String v : values) { if (strip(v).equals(p)) return true; }
		return false;
	}

	private static String strip(final String s) {
		return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
	}

	private static List<String> lines(final String text) {
		return new ArrayList<>(List.of(text.replace("\r\n", "\n").split("\n", -1)));
	}

	/** Returns [start, end) of the logical line holding the key, or null. */
	private static int[] find(final List<String> lines, final String key) {
		for (int i = 0; i < lines.size(); i++) {
			String l = lines.get(i);
			int eq = l.indexOf('=');
			if (eq > 0 && !l.startsWith(" ") && l.substring(0, eq).strip().equals(key)) {
				int end = i + 1;
				while (end < lines.size() && lines.get(end - 1).stripTrailing().endsWith("\\")) { end++; }
				return new int[] { i, end };
			}
		}
		return null;
	}

}
