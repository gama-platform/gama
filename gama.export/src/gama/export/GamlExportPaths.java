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

	/** Adjusts static resource paths when projects in the same archive are renamed. */
	public static String renameProjects(final String content, final String project, final Path model,
			final Map<String, String> renamed) {
		final Matcher matcher = TOKENS.matcher(content);
		final var result = new StringBuilder();
		final Path original = Path.of("/" + project).resolve(model).getParent();
		final Path destination = Path.of("/" + renamed.getOrDefault(project, project)).resolve(model).getParent();
		while (matcher.find()) {
			final String token = matcher.group();
			String replacement = token;
			if (token.charAt(0) == '"' || token.charAt(0) == '\'') {
				final String decoded = fileLiteral(token.substring(1, token.length() - 1));
				if (decoded != null) {
					String value = decoded;
					boolean explicit = false;
					for (final var entry : renamed.entrySet()) {
						for (final String prefix : java.util.List.of("/resource/", "platform:/resource/")) {
							final String from = prefix + entry.getKey() + "/";
							if (value.startsWith(from)) {
								value = prefix + entry.getValue() + "/" + value.substring(from.length());
								explicit = true;
								break;
							}
						}
						if (explicit) break;
					}
					if (!explicit && value.startsWith("../")) {
						try {
							final Path target = original.resolve(value).normalize();
							if (target.getNameCount() > 1) {
								final String source = target.getName(0).toString();
								final String name = renamed.get(source);
								if (name != null && !name.equals(source))
									value = destination.relativize(Path.of("/" + name).resolve(
											target.subpath(1, target.getNameCount()))).toString().replace('\\', '/');
							}
						} catch (final InvalidPathException e) {
							// Non-path strings are deliberately left unchanged.
						}
					}
					if (!value.equals(decoded)) replacement = token.charAt(0) + value.replace("\\", "\\\\")
							.replace(String.valueOf(token.charAt(0)), "\\" + token.charAt(0)) + token.charAt(0);
				}
			}
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

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
