package gaml.compiler.transition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Lexical protection for the legacy, text-based conversion passes. */
final class GamlSource {

	private record Fragment(String placeholder, String original) {}

	private GamlSource() {}

	static String codeOnly(final String source) {
		final StringBuilder result = new StringBuilder(source);
		for (int i = 0; i < source.length();) {
			final int end = protectedEnd(source, i);
			if (end == i) {
				i++;
				continue;
			}
			for (int j = i; j < end; j++) {
				if (source.charAt(j) != '\n' && source.charAt(j) != '\r') result.setCharAt(j, ' ');
			}
			i = end;
		}
		return result.toString();
	}

	static Set<String> migrationReviews(final String source) {
		final Set<String> result = new HashSet<>();
		final String prefix = "/* TODO GAMA 2026 (";
		for (int i = 0; i < source.length();) {
			final int end = protectedEnd(source, i);
			if (end == i) {
				i++;
			} else {
				if (source.startsWith(prefix, i)) {
					final int close = source.indexOf("):", i + prefix.length());
					if (close >= 0 && close < end) result.add(source.substring(i + prefix.length(), close));
				}
				i = end;
			}
		}
		return result;
	}

	static String transformCode(final IFileTransformer transformer, final String source) {
		String prefix = "__GAMA_MIGRATION_PROTECTED_";
		while (source.contains(prefix)) { prefix += "_"; }
		final List<Fragment> fragments = new ArrayList<>();
		final StringBuilder protectedSource = new StringBuilder(source.length());
		for (int i = 0; i < source.length();) {
			final int end = protectedEnd(source, i);
			if (end == i) {
				protectedSource.append(source.charAt(i++));
				continue;
			}
			final String original = source.substring(i, end);
			final String marker = prefix + fragments.size() + "__";
			final StringBuilder placeholder = new StringBuilder();
			if (original.startsWith("//")) {
				placeholder.append("//").append(marker);
			} else if (original.startsWith("/*")) {
				placeholder.append("/*").append(marker);
				appendNewlines(placeholder, original);
				placeholder.append("*/");
			} else {
				final char quote = original.charAt(0);
				placeholder.append(quote).append(marker);
				appendNewlines(placeholder, original);
				placeholder.append(quote);
			}
			fragments.add(new Fragment(placeholder.toString(), original));
			protectedSource.append(placeholder);
			i = end;
		}
		String result = transformer.transform(protectedSource.toString());
		for (final Fragment fragment : fragments) {
			final int first = result.indexOf(fragment.placeholder());
			if (first < 0 || result.indexOf(fragment.placeholder(), first + 1) >= 0) return source;
			result = result.substring(0, first) + fragment.original()
					+ result.substring(first + fragment.placeholder().length());
		}
		return result.equals(source) ? source : result;
	}

	private static void appendNewlines(final StringBuilder target, final String text) {
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == '\r' || c == '\n') target.append(c);
		}
	}

	/** Returns the end of a string or comment starting at i, or i for ordinary code. */
	private static int protectedEnd(final String source, final int i) {
		final char c = source.charAt(i);
		if (c == '"' || c == '\'') {
			int j = i + 1;
			while (j < source.length()) {
				final char next = source.charAt(j++);
				if (next == '\\' && j < source.length()) {
					j++;
				} else if (next == c) {
					break;
				}
			}
			return j;
		}
		if (c != '/' || i + 1 >= source.length()) return i;
		final char next = source.charAt(i + 1);
		if (next == '/') {
			int j = i + 2;
			while (j < source.length() && source.charAt(j) != '\n' && source.charAt(j) != '\r') { j++; }
			return j;
		}
		if (next == '*') {
			final int close = source.indexOf("*/", i + 2);
			return close < 0 ? source.length() : close + 2;
		}
		return i;
	}
}
