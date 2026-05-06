package gaml.grammar.transition;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that converts inline `arg` statements into explicit
 * arguments in the action signature.
 *
 * <p>In older GAML syntax, arguments were often described inside the action body:</p>
 * <pre>
 *   int calculNbJourEcouleEntreDeuxDates() { // optional comment
 *       arg jourDebutEntree type: int default: 1;
 *       arg moisDebutEntree type: int default: 1;
 *   }
 * </pre>
 *
 * <p>The new syntax requires them to be declared in the action signature:</p>
 * <pre>
 *   int calculNbJourEcouleEntreDeuxDates(int jourDebutEntree &lt;- 1, int moisDebutEntree &lt;- 1) { // optional comment
 *   }
 * </pre>
 */
public class FixActionArguments implements IFileTransformer {

	// -----------------------------------------------------------------------
	// Constants
	// -----------------------------------------------------------------------

	/**
	 * GAML statement keywords that are <em>not</em> type names. When the first
	 * token on a line is one of these, the line is skipped.
	 */
	private static final Set<String> EXCLUDED_KEYWORDS = Set.of(
			// control flow
			"if", "else", "loop", "switch", "match", "break", "return",
			// agent interactions & data
			"ask", "create", "do", "let", "add", "remove", "put", "set",
			"save", "write", "draw",
			// agent behavior blocks
			"reflex", "aspect", "init", "state", "plan", "rule", "perceive",
			"norm", "sanction", "obligation",
			// output / display blocks
			"equation", "chart", "display", "monitor", "event", "layout",
			"overlay", "inspect",
			// species / model structure
			"species", "experiment", "global", "model", "import", "output",
			"permanent",
			// misc
			"capture", "release", "migrate", "using", "try", "catch",
			"error", "warn", "trace", "debug", "assert");

	/**
	 * A regex segment that matches any amount of whitespace or comments.
	 */
	private static final String WS_OR_COMMENT = "(?:\\s*+(?://.*+|/\\*[\\s\\S]*?\\*/))*+";

	/**
	 * Matches an action block with its header and subsequent arg declarations.
	 * It also captures intermixed comments inside the args block to preserve them.
	 */
	private static final Pattern ACTION_BLOCK = Pattern.compile(
			"^(\\s*+)(?!//)"                        // group 1: indent
			+ "([A-Za-z_]\\w*+)"                    // group 2: return type or 'action'
			+ "(<[^>]*+(?:<[^>]*+>[^>]*+)*+>)?"     // group 3: optional generic type param
			+ "(\\s++)"                             // group 4: whitespace
			+ "([A-Za-z_]\\w*+)"                    // group 5: identifier name
			+ "\\s*\\(([^)]*)\\)\\s*\\{"            // group 6: existing arguments
			+ "(" + WS_OR_COMMENT + "\\s*+arg\\b[^;]+;" + "(?:\\s*+(?:arg\\b[^;]+;|//.*+|/\\*[\\s\\S]*?\\*/))*+" + ")", // group 7: args block
			Pattern.MULTILINE);

	/**
	 * Matches an individual arg statement.
	 */
	private static final Pattern ARG_PATTERN = Pattern.compile("arg\\s+([A-Za-z_]\\w*)([^;]*);");

	/**
	 * Matches the type attribute within an arg statement.
	 */
	private static final Pattern TYPE_PATTERN = Pattern.compile("\\btype:\\s*(.*?)(?=\\s+\\b[a-z]+:|$)");

	/**
	 * Matches the default attribute within an arg statement.
	 */
	private static final Pattern DEFAULT_PATTERN = Pattern.compile("\\bdefault:\\s*(.*?)(?=\\s+\\b[a-z]+:|$)");

	// -----------------------------------------------------------------------
	// IFileTransformer
	// -----------------------------------------------------------------------

	@Override
	public String transform(final String content) {
		final Matcher m = ACTION_BLOCK.matcher(content);
		if (!m.find()) { return content; }
		
		return ACTION_BLOCK.matcher(content).replaceAll(mr -> {
			if (EXCLUDED_KEYWORDS.contains(mr.group(2))) {
				return Matcher.quoteReplacement(mr.group(0));
			}
			
			final String indent = mr.group(1);
			final String typeOrAction = mr.group(2);
			final String generics = mr.group(3) != null ? mr.group(3) : "";
			final String ws = mr.group(4);
			final String name = mr.group(5);
			final String existingArgs = mr.group(6) != null ? mr.group(6).trim() : "";
			final String argsBlock = mr.group(7);
			
			final Matcher argMatcher = ARG_PATTERN.matcher(argsBlock);
			final StringBuilder newArgs = new StringBuilder();
			boolean first = existingArgs.isEmpty();
			
			if (!first) {
				newArgs.append(existingArgs);
			}
			
			while (argMatcher.find()) {
				if (!first) {
					newArgs.append(", ");
				}
				first = false;
				
				final String argName = argMatcher.group(1);
				final String attrs = argMatcher.group(2);
				
				final Matcher typeMatcher = TYPE_PATTERN.matcher(attrs);
				final String argType = typeMatcher.find() ? typeMatcher.group(1).trim() : null;
				
				final Matcher defMatcher = DEFAULT_PATTERN.matcher(attrs);
				final String argDef = defMatcher.find() ? defMatcher.group(1).trim() : null;
				
				if (argType != null) {
					newArgs.append(argType).append(" ");
				}
				newArgs.append(argName);
				if (argDef != null) {
					newArgs.append(" <- ").append(argDef);
				}
			}
			
			// Extract any comments from the argsBlock by removing the arg lines
			String remainingBody = argsBlock.replaceAll("(?m)^([ \\t]*)arg\\b[^;]+;[ \\t]*", "$1");
			// Clean up extra empty lines left behind by the removal
			remainingBody = remainingBody.replaceAll("(?m)^[ \\t]*\\n+", "");
			
			final String replacement = indent + typeOrAction + generics + ws + name + "(" + newArgs.toString() + ") {" + remainingBody;
			return Matcher.quoteReplacement(replacement);
		});
	}
}
