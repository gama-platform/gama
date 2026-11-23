package gaml.compiler.transition;

import java.util.List;
import java.util.regex.Pattern;

/** Adds conservative review reminders for migration changes that cannot be inferred safely. */
final class AddMigrationReviews implements IFileTransformer {

	private record Rule(Pattern pattern, String kind, String message) {}

	private static final List<Rule> RULES = List.of(
			rule("\\b(experiment|user_command|parameter)\\b", "ownership",
					"Review experiment/simulation ownership, including tests, batch facets and commands. "
					+ "Use simulation.x or ask simulation for simulation state; use of: experiment for "
					+ "experiment parameters and commands. Check callbacks and actions_context."),
			rule("\\bparameter\\b", "parameter defaults",
					"Parameter initializers cannot call actions; use a simulation-independent default, including "
					+ "when the default comes from the targeted variable."),
			rule("\\bexperiment\\b", "scheduling",
					"Review experiment scheduling defaults: set parallel: to control concurrency and "
					+ "group_parameter_sets: false to preserve sequential parameter-set grouping in GUI batches."),
			rule("\\b(output|permanent|display|event|overlay)\\b", "outputs",
					"Review output owners, experiment references, display events and controls. "
					+ "Use experiment.x for controller state or output locals for presentation-only state. "
					+ "Check overlay units, viewport dimensions, mouse coordinates and OpenGL rendering."),
			rule("\\bmesh\\b", "mesh source",
					"Bare mesh source names now use normal name resolution; ensure each refers to a declared "
					+ "field, grid or file."),
			rule("\\b(graph|list|map|nil)\\b", "containers",
					"Review built-in action container types and explicit nil values. Spatial networks may need "
					+ "graph<geometry, geometry>; use [] or map([]) when an empty container is intended."),
			rule("\\blist\\s+(?:[A-Za-z_]\\w*\\s+)?of\\s*:", "list type",
					"Legacy list of: declarations remain valid. If modernizing them, use list<element_type> name; "
					+ "nested or ambiguous types are not converted automatically."),
			rule("\\blet\\s+(?:name\\s*:\\s*)?[A-Za-z_]\\w*\\s+[^;\\r\\n]*\\b(?:of|index)\\s*:",
					"local type", "Legacy let declarations remain valid. Preserve container of:/index: facets "
					+ "or translate them to the equivalent generic type when modernizing."),
			rule("\\b(grid|matrix_with|set_values|collect)\\b", "grids",
					"Review grid collect results and matrix_with/set_values: grid collect now returns a matrix; "
					+ "use grid[attribute] for matrix reads and writes or list(grid) for a flat collect."),
			rule("\\b(species|grid)\\b", "species brackets",
					"Review species[attribute] reads and writes and ambiguous indices that share an attribute "
					+ "name; qualify the index as self.name or parenthesize it when needed."),
			rule("\\b(chart|datalist|data)\\b", "charts",
					"Review chart bodies and legends: bodies run every step and changing or duplicate legends "
					+ "can create distinct series."),
			rule("\\b(pause|resume|paused|is_on_user_hold)\\b", "pause",
					"Review pause/resume and paused: use experiment pause/play actions and read "
					+ "experiment.is_paused or simulation.is_on_user_hold as appropriate."),
			rule("\\bpref_(synchronize_quadtree|parallel_simulations|parallel_simulations_all|binary_serializer)\\b",
					"preferences", "Remove obsolete synchronization, parallel-scheduling and binary-serializer "
					+ "preferences. Set experiment parallel: and group_parameter_sets: as needed."),
			rule("\\b(select|insert|AgentDB)\\b", "database",
					"Review SQL select results and insert calls: select returns a dataframe and insert uses data: "
					+ "instead of columns:/values:."),
			rule("\\bSQLSKILL\\b", "database skill",
					"Replace the former SQLSKILL with sql in species skills lists."),
			rule("\\bxml_file\\b", "xml",
					"Review xml_file contents: it is now a root-element map with name, attributes, text and children, "
					+ "not a map of raw lines."),
			rule("\\b(string|length|index_of|last_index_of|copy_between|at|first|last|capitalize)\\b",
					"string indices", "Review string lengths and indices involving supplementary Unicode characters; "
					+ "they now count code points rather than UTF-16 code units."),
			rule("\\bappend_horizontally\\b", "matrix append",
					"append_horizontally now appends columns, not rows. Use append_vertically for row concatenation."),
			rule("\\bcorrelation\\b", "correlation",
					"Pearson correlation has been corrected; review thresholds and expected results."),
			rule("\\b(rng_usage|exploration)\\b", "reproducibility",
					"Assigning rng_usage now restores an absolute raw-draw position. Latin-hypercube designs "
					+ "can also differ from 2025-06 for the same seed."),
			rule("\\b(closest_to|agent_closest_to)\\b", "spatial reproducibility",
					"Nearest-neighbor tie breaking can consume random draws in a different order; "
					+ "recheck seeded results."),
			rule("\\baction\\s+(?:name\\s*:\\s*)?[A-Za-z_]\\w*\\s+[^;{]*\\btype\\s*:",
					"action return type", "Move the return type before the action name, for example "
					+ "action f type: int { ... } becomes int f() { ... }; move arg declarations into the signature."),
			rule("\\baction\\s+(?:name\\s*:\\s*)?[A-Za-z_]\\w*+(?!\\s*\\()", "action parentheses",
					"Explicit action declarations require parentheses after the name; preserve any remaining "
					+ "facets and review return types separately."),
			rule("\\barg\\s+[A-Za-z_]\\w*", "action signature",
					"Move action-body arg declarations into the signature; move arg value: in do/invoke blocks "
					+ "into the call. Preserve of:/index: as generic types and review optional: and defaults."),
			rule("\\b(?:do|invoke)\\s+(?:action\\s*:\\s*)?[A-Za-z_]\\w*\\s*\\{", "action block call",
					"Move arg name value: expression; statements into the call, for example "
					+ "do move { arg distance value: 2; } becomes do move(distance: 2);."),
			rule("\\b(?:do|invoke)\\s+[^;{}\\r\\n]*\\breturns\\s*:", "action result",
					"The returns: facet no longer captures an action result. Use a typed assignment such as "
					+ "int result <- self.count(); and check the action's return type and receiver."),
			rule("\\b(?:self|myself)\\s+[A-Za-z_]\\w*\\s*\\["
					+ "|\\b[A-Za-z_]\\w*\\s*\\(\\s*(?:self|myself)\\s*,\\s*\\[", "action operator",
					"Review possible legacy action-operator calls such as self calculate [x::1] or "
					+ "calculate(self, [x::1]); use self.calculate(x: 1) if this is an action."),
			rule("\\b(action|do|invoke)\\b", "action evaluation",
					"Action arguments evaluate in the caller's scope; do not rely on the evaluation order "
					+ "of expressions with side effects."),
			rule("\\bdo\\s+action\\s*:", "action call",
					"Replace do action: name with do name(...), moving any arguments into the parentheses."),
			rule("\\bset\\s+[^;\\r\\n]+?\\s+(?:value\\s*:|<-)", "assignment",
					"Replace set target value: expression; with target <- expression;."),
			rule("\\b(?:experiment|species|grid|display|action|int|float|string|bool|point|list|map)"
					+ "\\s+name\\s*:", "name facet",
					"Review explicit name: before declaration identifiers; experiment, species, grid, display "
					+ "and action headers require the identifier directly. Keep ordinary name: facets elsewhere."),
			rule("\\bif\\s+condition\\s*:", "condition facet",
					"Replace if condition: expression { ... } with if expression { ... }."),
			rule("\\breturn\\s+value\\s*:", "return facet",
					"Replace return value: expression; with return expression;."),
			rule("\\bsolve\\s+equation\\s*:", "solve facet",
					"Replace solve equation: name; with solve name;."),
			rule("\\bwith\\s*:", "with",
					"Review action with: arguments and the action signature before moving them into do name(args). "
					+ "Keep user_command and exploration with: values; create with: remains accepted but deprecated."),
			rule("\\b(ceil|ceiling|envelope)\\b", "operators",
					"Review ceil return types (now int) and geometry envelope attribute access (use envelope(...)).")
	);

	private static final String GENERAL = "/* TODO GAMA 2026 (migration): Review runtime defaults and external "
			+ "resources, including OBJ data, serialized saves, packages, Java plugins and clients. "
			+ "This converter processes only GAML files; see MIGRATION_GUIDE_2026.md. */";

	private static Rule rule(final String regex, final String kind, final String message) {
		return new Rule(Pattern.compile(regex), kind, message);
	}

	@Override
	public String transform(final String content) {
		final String code = GamlSource.codeOnly(content);
		final var existing = GamlSource.migrationReviews(content);
		final String newline = content.contains("\r\n") ? "\r\n" : content.contains("\r") ? "\r" : "\n";
		final StringBuilder notes = new StringBuilder();
		if (!existing.contains("migration")) notes.append(GENERAL).append(newline);
		for (final Rule rule : RULES) {
			final String prefix = "/* TODO GAMA 2026 (" + rule.kind() + "):";
			if (rule.pattern().matcher(code).find() && !existing.contains(rule.kind())) {
				notes.append(prefix).append(' ').append(rule.message()).append(" */").append(newline);
			}
		}
		if (notes.isEmpty()) return content;
		int offset = content.startsWith("\uFEFF") ? 1 : 0;
		if (content.startsWith("/* TODO GAMA 2026 (syntax):", offset)) {
			final int end = content.indexOf("*/", offset);
			if (end >= 0) {
				offset = end + 2;
				if (offset < content.length() && content.charAt(offset) == '\r') offset++;
				if (offset < content.length() && content.charAt(offset) == '\n') offset++;
			}
		}
		return content.substring(0, offset) + notes + content.substring(offset);
	}
}
