package gaml.compiler.ui.wizards.behavior;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import gaml.compiler.ui.wizards.species.SpeciesDraft;

/**
 * Detached behaviour settings. {@code name} is empty for an anonymous behaviour; {@code facets} are the explicit
 * facets in source order, excluding {@code name}, with raw GAML values; {@code body} is the raw text strictly between
 * the braces (never trimmed). Drafts of kinds without code ({@code rule}) keep their body so that nothing is lost.
 */
public record BehaviorDraft(BehaviorKind kind, String name, List<SpeciesDraft.Facet> facets, String body) {

	public BehaviorDraft {
		Objects.requireNonNull(kind);
		name = name == null ? "" : name;
		facets = facets == null ? List.of() : List.copyOf(facets);
		body = body == null ? "" : body;
	}

	public static BehaviorDraft empty(final BehaviorKind kind) {
		return new BehaviorDraft(kind, kind.nameRequired() || kind == BehaviorKind.REFLEX ? "my_" + kind.keyword() : "",
				List.of(), "");
	}

	/** The value of a facet, or null when absent. */
	public String facet(final String facetName) {
		for (var facet : facets) if (facet.name().equals(facetName)) return facet.value();
		return null;
	}

	/** A copy with the facet replaced in place (appended when absent), or removed when {@code value} is null. */
	public BehaviorDraft withFacet(final String facetName, final String value) {
		List<SpeciesDraft.Facet> result = new ArrayList<>();
		boolean found = false;
		for (var facet : facets) {
			if (!facet.name().equals(facetName)) result.add(facet);
			else if (value != null && !found) result.add(new SpeciesDraft.Facet(facetName, value));
			found |= facet.name().equals(facetName);
		}
		if (!found && value != null) result.add(new SpeciesDraft.Facet(facetName, value));
		return new BehaviorDraft(kind, name, result, body);
	}

	/** Returns an actionable error, or null. Compilation belongs to the dialog/adapter. */
	public String validationError() {
		String noun = Character.toUpperCase(kind.keyword().charAt(0)) + kind.keyword().substring(1);
		if (name.isEmpty()) {
			if (kind.nameRequired()) return noun + " name is required.";
		} else if (!SpeciesDraft.isIdentifier(name)) return noun + " name must be a GAML identifier.";
		var names = new HashSet<String>();
		for (var facet : facets) {
			if (!SpeciesDraft.isIdentifier(facet.name())) return "Invalid facet name: " + facet.name();
			if ("name".equals(facet.name())) return "Use the Name field.";
			if (!names.add(facet.name())) return "Duplicate facet: " + facet.name();
			if (facet.value().isBlank()) return "Enter a value for facet " + facet.name() + ".";
			if (facet.value().contains(";")) return "The value of " + facet.name() + " must be a single expression.";
		}
		for (var facet : BehaviorFacet.of(kind))
			if (facet.required() && facet(facet.name()) == null) return "Enter a value for facet " + facet.name() + ".";
		return null;
	}

	/** Declaration header without block, e.g. {@code reflex move when: alive}. */
	public String header() {
		StringBuilder result = new StringBuilder(kind.keyword());
		if (!name.isEmpty()) result.append(' ').append(name);
		for (var facet : facets) result.append(' ').append(facet.name()).append(": ").append(facet.value().trim());
		return result.toString();
	}
}
