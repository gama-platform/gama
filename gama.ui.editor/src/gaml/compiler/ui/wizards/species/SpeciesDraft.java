package gaml.compiler.ui.wizards.species;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Detached values only: init is the init body, not the complete species body. */
public record SpeciesDraft(String name, List<String> skills, List<Facet> facets, List<Variable> variables,
		String init, List<String> reflexOrder) {

	public record Facet(String name, String value) {
		public Facet {
			Objects.requireNonNull(name);
			Objects.requireNonNull(value);
		}
	}

	public record Variable(String name, String type, String init, String update, String function, String min, String max) {
		public Variable {
			Objects.requireNonNull(name);
			Objects.requireNonNull(type);
			Objects.requireNonNull(init);
			Objects.requireNonNull(update);
			Objects.requireNonNull(function);
			Objects.requireNonNull(min);
			Objects.requireNonNull(max);
		}
	}

	public SpeciesDraft {
		Objects.requireNonNull(name);
		skills = List.copyOf(skills);
		facets = List.copyOf(facets);
		variables = List.copyOf(variables);
		Objects.requireNonNull(init);
		reflexOrder = List.copyOf(reflexOrder);
	}

	public static SpeciesDraft empty() {
		return new SpeciesDraft("", List.of(), List.of(), List.of(), "", List.of());
	}

	/** Effective parent; an absent facet is the implicit agent parent. */
	public String parent() {
		return facets.stream().filter(facet -> "parent".equals(facet.name())).map(facet -> facet.value().trim())
				.findFirst().orElse("agent");
	}

	public static boolean isIdentifier(final String value) {
		return value.matches("[A-Za-z_][A-Za-z_0-9]*");
	}

	/** Returns an actionable error, or null. Syntax and context validation belong to the adapter. */
	public String validationError() {
		if (!isIdentifier(name)) return "Species name must be a GAML identifier.";
		var names = new HashSet<String>();
		for (String skill : skills) {
			if (!isIdentifier(skill)) return "Invalid skill name: " + skill;
			if (!names.add(skill)) return "Duplicate skill: " + skill;
		}
		names.clear();
		for (Facet facet : facets) {
			if (!isIdentifier(facet.name())) return "Invalid facet name: " + facet.name();
			if (List.of("name", "skills").contains(facet.name()))
				return "Use the dedicated field for " + facet.name() + ".";
			if (!names.add(facet.name())) return "Duplicate facet: " + facet.name();
			if (facet.value().isBlank()) return "Enter a value for facet " + facet.name() + ".";
		}
		names.clear();
		for (Variable variable : variables) {
			if (!isIdentifier(variable.name())) return "Invalid variable name: " + variable.name();
			if (!names.add(variable.name())) return "Duplicate variable: " + variable.name();
			if (variable.type().isBlank()) return "Enter a type for variable " + variable.name() + ".";
		}
		names.clear();
		for (String reflex : reflexOrder) {
			if (!isIdentifier(reflex)) return "Invalid reflex name: " + reflex;
			if (!names.add(reflex)) return "Duplicate reflex: " + reflex;
		}
		return null;
	}
}
