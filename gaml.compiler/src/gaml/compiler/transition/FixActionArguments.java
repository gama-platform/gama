package gaml.compiler.transition;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link IFileTransformer} that converts inline `arg` statements into explicit
 * arguments in the action signature. Arguments with unsupported facets are left for manual review.
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
	private static final Pattern ARG_PATTERN = Pattern.compile("\\barg\\s+([A-Za-z_]\\w*)([^;]*);");
	private static final Pattern ARG_KEYWORD = Pattern.compile("\\barg\\b");

	/**
	 * Only move declarations whose type and optional default can be preserved without interpreting other facets.
	 */
	private static final Pattern SUPPORTED_ATTRIBUTES = Pattern.compile(
			"\\s*type:\\s*([A-Za-z_]\\w*(?:\\s*<[^<>]+>)?)(?:\\s+default:\\s*(\\S[\\s\\S]*?))?\\s*");

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

			if (!existingArgs.isEmpty()) return Matcher.quoteReplacement(mr.group(0));
			final Matcher argMatcher = ARG_PATTERN.matcher(argsBlock);
			final StringBuilder newArgs = new StringBuilder();
			boolean first = true;
			int converted = 0;
			
			while (argMatcher.find()) {
				final Matcher attributes = SUPPORTED_ATTRIBUTES.matcher(argMatcher.group(2));
				if (!attributes.matches() || attributes.group(2) != null
						&& GamlSource.containsPossibleFacet(attributes.group(2))) {
					return Matcher.quoteReplacement(mr.group(0));
				}
				if (!first) {
					newArgs.append(", ");
				}
				first = false;
				converted++;
				
				final String argName = argMatcher.group(1);
				final String argType = attributes.group(1).trim();
				final String argDef = attributes.group(2);

				newArgs.append(argType).append(" ");
				newArgs.append(argName);
				if (argDef != null) {
					newArgs.append(" <- ").append(argDef.trim());
				}
			}
			if (converted != ARG_KEYWORD.matcher(argsBlock).results().count()) {
				return Matcher.quoteReplacement(mr.group(0));
			}
			
			// Extract any comments from the argsBlock by removing the arg lines
			String remainingBody = ARG_PATTERN.matcher(argsBlock).replaceAll("");
			// Clean up extra empty lines left behind by the removal
			remainingBody = remainingBody.replaceAll("(?m)^[ \\t]*\\n+", "");
			
			final String replacement = indent + typeOrAction + generics + ws + name + "(" + newArgs.toString() + ") {" + remainingBody;
			return Matcher.quoteReplacement(replacement);
		});
	}
}
