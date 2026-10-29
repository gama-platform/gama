package gama.export;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Rewrites static file literals without touching comments or other model text. */
public final class GamlExportPaths {

	private static final Pattern TOKENS = Pattern.compile("//[^\\r\\n]*|/\\*.*?\\*/|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'",
			Pattern.DOTALL);

	private GamlExportPaths() {}

	public static String rewrite(final String content, final Path sourceModel, final Path packagedModel,
			final Map<Path, Path> relocated) {
		final Matcher matcher = TOKENS.matcher(content);
		final var result = new StringBuilder();
		while (matcher.find()) {
			final String token = matcher.group();
			String replacement = token;
			if (token.charAt(0) == '"' || token.charAt(0) == '\'') {
				final String literal = token.substring(1, token.length() - 1);
				final String decoded = fileLiteral(literal);
				if (decoded != null) {
					try {
						final Path path = Path.of(decoded.replace('\\', '/'));
						final Path resolved = (path.isAbsolute() ? path : sourceModel.getParent().resolve(path))
								.toAbsolutePath().normalize();
						final Path target = relocated.get(resolved);
						if (target != null) {
							final String relative = packagedModel.getParent().relativize(target).toString().replace('\\', '/');
							replacement = token.charAt(0) + relative.replace("\\", "\\\\")
									.replace(String.valueOf(token.charAt(0)), "\\" + token.charAt(0)) + token.charAt(0);
						}
					} catch (final InvalidPathException e) {
						// String literals that are not file-system paths are unchanged.
					}
				}
			}
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private static String fileLiteral(final String literal) {
		final var result = new StringBuilder();
		for (int i = 0; i < literal.length(); i++) {
			char character = literal.charAt(i);
			if (character == '\\') {
				if (++i == literal.length()) return null;
				character = literal.charAt(i);
				if (character != '\\' && character != '"' && character != '\'' && character != '/') return null;
			}
			result.append(character);
		}
		return result.toString();
	}
}
