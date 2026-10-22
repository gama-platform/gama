package gaml.compiler.ui.wizards.experiment;

import java.util.HashSet;
import java.util.List;

import gaml.compiler.ui.wizards.species.SpeciesDraft;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/**
 * Detached experiment settings. {@code name} is the plain label (adapters quote it when it is not an identifier);
 * {@code type} is gui, batch or test (absent in source means gui); {@code facets} are the other header facets with raw
 * GAML values, in source order; {@code init} is the text of the init block only. Statements not represented here
 * (displays, methods, tests, reflexes, actions...) belong to the adapters, which preserve them.
 */
public record ExperimentDraft(String name, String type, List<Facet> facets, List<SpeciesDraft.Variable> variables,
		String init, List<Parameter> parameters, List<Monitor> monitors) {

	public static final String GUI = "gui";
	public static final String BATCH = "batch";
	public static final String TEST = "test";
	public static final List<String> TYPES = List.of(GUI, BATCH, TEST);

	/**
	 * {@code parameter "title" var: variable facets...;}. {@code title} is plain text (unquoted, unescaped); facet
	 * values are raw GAML.
	 */
	public record Parameter(String title, String variable, List<Facet> facets) {
		public Parameter {
			title = title == null ? "" : title;
			variable = variable == null ? "" : variable;
			facets = facets == null ? List.of() : List.copyOf(facets);
		}

		public String facet(final String name) { return find(facets, name); }
	}

	/** {@code monitor "title" value: expression facets...;}. {@code title} is plain text; values are raw GAML. */
	public record Monitor(String title, String value, List<Facet> facets) {
		public Monitor {
			title = title == null ? "" : title;
			value = value == null ? "" : value;
			facets = facets == null ? List.of() : List.copyOf(facets);
		}

		public String facet(final String name) { return find(facets, name); }
	}

	public ExperimentDraft {
		name = name == null ? "" : name;
		type = type == null || type.isBlank() ? GUI : type.trim();
		facets = facets == null ? List.of() : List.copyOf(facets);
		variables = variables == null ? List.of() : List.copyOf(variables);
		init = init == null ? "" : init;
		parameters = parameters == null ? List.of() : List.copyOf(parameters);
		monitors = monitors == null ? List.of() : List.copyOf(monitors);
	}

	public static ExperimentDraft empty(final String type) {
		return new ExperimentDraft("my_experiment", type, List.of(), List.of(), "", List.of(), List.of());
	}

	private static String find(final List<Facet> facets, final String name) {
		for (var facet : facets) if (facet.name().equals(name)) return facet.value();
		return null;
	}

	/** The value of a header facet, or null when absent. */
	public String facet(final String facetName) { return find(facets, facetName); }

	/** Facets that are set but have no effect for the current type (shown as a warning, never blocking). */
	public List<String> inactiveFacets() {
		List<String> result = new java.util.ArrayList<>();
		for (var facet : facets) {
			String section = ExperimentFacet.section(facet.name());
			if (section != null && !section.equals(ExperimentFacet.COMMON) && !section.equals(type)) result.add(facet.name());
		}
		return result;
	}

	/** Returns an actionable error, or null. Compilation belongs to the adapter's validator. */
	public String validationError() {
		if (name.isBlank()) return "Experiment name is required.";
		if (name.contains("\"") || name.contains("\\") || name.contains("\n") || name.contains("\r"))
			return "Experiment name cannot contain quotes, backslashes or line breaks.";
		if (!TYPES.contains(type)) return "Experiment type must be gui, batch or test.";
		var names = new HashSet<String>();
		for (Facet facet : facets) {
			if (!SpeciesDraft.isIdentifier(facet.name())) return "Invalid facet name: " + facet.name();
			if (List.of("name", "type").contains(facet.name())) return "Use the dedicated field for " + facet.name() + ".";
			if (!names.add(facet.name())) return "Duplicate facet: " + facet.name();
			if (facet.value().isBlank()) return "Enter a value for facet " + facet.name() + ".";
		}
		names.clear();
		for (var variable : variables) {
			if (!SpeciesDraft.isIdentifier(variable.name())) return "Invalid attribute name: " + variable.name();
			if (!names.add(variable.name())) return "Duplicate attribute: " + variable.name();
			if (variable.type().isBlank()) return "Enter a type for attribute " + variable.name() + ".";
		}
		for (int i = 0; i < parameters.size(); i++) {
			var parameter = parameters.get(i);
			String label = "Parameter " + (i + 1);
			if (parameter.title().isBlank()) return label + ": enter a title.";
			if (parameter.title().contains("\n") || parameter.title().contains("\r"))
				return label + ": the title cannot contain line breaks.";
			if (parameter.variable().isBlank()) return label + ": choose the variable (var:).";
			String error = facets(label, parameter.facets(), List.of("name", "var"));
			if (error != null) return error;
		}
		for (int i = 0; i < monitors.size(); i++) {
			var monitor = monitors.get(i);
			String label = "Monitor " + (i + 1);
			if (monitor.title().isBlank()) return label + ": enter a title.";
			if (monitor.title().contains("\n") || monitor.title().contains("\r"))
				return label + ": the title cannot contain line breaks.";
			if (monitor.value().isBlank()) return label + ": enter the monitored value.";
			String error = facets(label, monitor.facets(), List.of("name", "value"));
			if (error != null) return error;
		}
		return null;
	}

	private static String facets(final String label, final List<Facet> facets, final List<String> dedicated) {
		var names = new HashSet<String>();
		for (Facet facet : facets) {
			if (!SpeciesDraft.isIdentifier(facet.name())) return label + ": invalid facet name " + facet.name() + ".";
			if (dedicated.contains(facet.name())) return label + ": use the dedicated field for " + facet.name() + ".";
			if (!names.add(facet.name())) return label + ": duplicate facet " + facet.name() + ".";
			if (facet.value().isBlank()) return label + ": enter a value for " + facet.name() + ".";
		}
		return null;
	}

	/** The label as written in GAML: an identifier as is, otherwise a double-quoted string. */
	public static String quoteName(final String label) {
		return SpeciesDraft.isIdentifier(label) ? label : quote(label);
	}

	/** A GAML string literal for plain text. */
	public static String quote(final String text) {
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	/**
	 * Sets ({@code value} non-null) or removes a facet: in place when present, otherwise at its position in
	 * {@code original} (if it was there) or at the end. A value equal (trimmed) to the original keeps the original text.
	 */
	public static List<Facet> withFacet(final List<Facet> current, final List<Facet> original, final String name,
			final String value) {
		String originalText = find(original, name);
		String effective = value != null && originalText != null && value.trim().equals(originalText.trim())
				? originalText : value;
		List<Facet> result = new java.util.ArrayList<>(current);
		int index = -1;
		for (int i = 0; i < result.size(); i++) if (result.get(i).name().equals(name)) index = i;
		if (effective == null) {
			if (index >= 0) result.remove(index);
		} else if (index >= 0) result.set(index, new Facet(name, effective));
		else {
			int position = result.size();
			if (originalText != null) {
				List<String> order = original.stream().map(Facet::name).toList();
				int rank = order.indexOf(name);
				position = 0;
				for (int i = 0; i < result.size(); i++) {
					int r = order.indexOf(result.get(i).name());
					if (r >= 0 && r < rank) position = i + 1;
				}
			}
			result.add(position, new Facet(name, effective));
		}
		return List.copyOf(result);
	}

	/** The header (gui type implicit), e.g. {@code experiment "My exp" type: batch repeat: 2}. */
	public String header() {
		StringBuilder result = new StringBuilder("experiment ").append(quoteName(name));
		if (!GUI.equals(type)) result.append(" type: ").append(type);
		for (var facet : facets) result.append(' ').append(facet.name()).append(": ").append(facet.value().trim());
		return result.toString();
	}
}
