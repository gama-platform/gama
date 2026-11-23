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
			rule("\\bexperiment\\b", "scheduling",
					"Review experiment scheduling defaults: set parallel: to control concurrency and "
					+ "group_parameter_sets: false to preserve sequential parameter-set grouping in GUI batches."),
			rule("\\b(output|permanent|display|event|overlay)\\b", "outputs",
					"Review output owners, experiment references, display events and controls. "
					+ "Use experiment.x for controller state or output locals for presentation-only state. "
					+ "Check overlay units, viewport dimensions, mouse coordinates and OpenGL rendering."),
			rule("\\b(graph|list|map|nil)\\b", "containers",
					"Review built-in action container types and explicit nil values. Spatial networks may need "
					+ "graph<geometry, geometry>; use [] or map([]) when an empty container is intended."),
			rule("\\blist\\s+(?:[A-Za-z_]\\w*\\s+)?of\\s*:", "list type",
					"Review this legacy list type manually; use list<element_type> name. "
					+ "Nested or otherwise ambiguous type expressions are not converted automatically."),
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
			rule("\\bwith\\s*:", "with",
					"Review with: facets: only unambiguous named argument lists are converted. "
					+ "Keep ordinary lists, maps and exploration parameter sets in their original form."),
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
