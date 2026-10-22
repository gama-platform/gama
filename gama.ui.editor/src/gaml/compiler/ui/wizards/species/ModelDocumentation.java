package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Model header documentation. Text adapters persist it in the conventional leading model comment. */
public record ModelDocumentation(String author, String tags, String documentation) {

	public static final ModelDocumentation EMPTY = new ModelDocumentation("", "", "");

	public ModelDocumentation {
		Objects.requireNonNull(author);
		Objects.requireNonNull(tags);
		Objects.requireNonNull(documentation);
	}

	public boolean isEmpty() { return author.isBlank() && tags.isBlank() && documentation.isBlank(); }

	/**
	 * Conventional GAMA model header: Name, documentation lines, Author and Tags. A comment terminator inside a value
	 * is written as "* /" so the comment cannot end early. Line separators are normalized to {@code newline}.
	 */
	public String comment(final String modelName, final String newline) {
		StringBuilder result = new StringBuilder("/**").append(newline);
		result.append("* Name: ").append(escape(modelName)).append(newline);
		for (String line : documentation.strip().split("\\R", -1))
			if (!documentation.isBlank()) result.append("* ").append(escape(line.stripTrailing())).append(newline);
		result.append("* Author: ").append(escape(author.strip())).append(newline);
		result.append("* Tags: ").append(escape(tags.strip())).append(newline);
		return result.append("*/").toString();
	}

	/** Reads a conventional model header comment; returns null when it has no Name, Author or Tags line. */
	public static ModelDocumentation parse(final String comment) {
		if (comment == null || !comment.startsWith("/*") || !comment.endsWith("*/") || comment.length() < 4) return null;
		String body = comment.substring(comment.startsWith("/**") ? 3 : 2, comment.length() - 2);
		String author = "";
		String tags = "";
		boolean recognized = false;
		List<String> lines = new ArrayList<>();
		for (String raw : body.split("\\R", -1)) {
			String line = raw.strip();
			if (line.startsWith("*")) line = line.substring(1);
			if (line.startsWith(" ")) line = line.substring(1);
			line = line.stripTrailing().replace("* /", "*/");
			String lower = line.toLowerCase(java.util.Locale.ROOT);
			if (lower.startsWith("name:")) recognized = true;
			else if (lower.startsWith("author:")) {
				recognized = true;
				author = line.substring(7).strip();
			} else if (lower.startsWith("tags:")) {
				recognized = true;
				tags = line.substring(5).strip();
			} else lines.add(line);
		}
		if (!recognized) return null;
		while (!lines.isEmpty() && lines.getFirst().isBlank()) lines.removeFirst();
		while (!lines.isEmpty() && lines.getLast().isBlank()) lines.removeLast();
		return new ModelDocumentation(author, tags, String.join("\n", lines));
	}

	/** Documentation of the conventional header comment leading a GAML source, or EMPTY when absent. */
	public static ModelDocumentation read(final String source) {
		int[] range = headerRange(source);
		return range == null ? EMPTY : parse(source.substring(range[0], range[1]));
	}

	/** Offsets [start, end) of the last recognized documentation comment before the first GAML token, or null. */
	static int[] headerRange(final String source) {
		int[] found = null;
		int i = 0;
		int length = source.length();
		while (i < length) {
			char c = source.charAt(i);
			if (Character.isWhitespace(c)) i++;
			else if (source.startsWith("//", i)) {
				while (i < length && source.charAt(i) != '\n' && source.charAt(i) != '\r') i++;
			} else if (source.startsWith("/*", i)) {
				int end = source.indexOf("*/", i + 2);
				if (end < 0) return found;
				if (parse(source.substring(i, end + 2)) != null) found = new int[] { i, end + 2 };
				i = end + 2;
			} else break;
		}
		return found;
	}

	private static String escape(final String value) {
		return value.replace("*/", "* /");
	}
}
