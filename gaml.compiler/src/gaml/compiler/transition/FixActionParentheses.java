package gaml.compiler.transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds missing parentheses to explicit {@code action} declarations. Typed headers are left unchanged:
 * without type metadata, they cannot be distinguished safely from other named blocks.
 */
public class FixActionParentheses implements IFileTransformer {

	private static final Pattern PATTERN = Pattern.compile(
			"^(\\s*+)action(\\s++)([A-Za-z_]\\w*+)(?!\\s*+\\()(\\s*+)(?=\\{|;)",
			Pattern.MULTILINE);

	@Override
	public String transform(final String content) {
		return PATTERN.matcher(content).replaceAll(mr -> Matcher.quoteReplacement(
				mr.group(1) + "action" + mr.group(2) + mr.group(3) + "()" + mr.group(4)));
	}
}
