package gaml.compiler.ui.wizards.species;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.kernel.GamaMetaModel;

/** Read-only attribute metadata for a detached species selection. Never mutate compiler descriptions. */
@FunctionalInterface
public interface SpeciesWizardContext {

	record InheritedAttribute(SpeciesDraft.Variable variable, String origin, boolean redefinable) {
		public InheritedAttribute {
			Objects.requireNonNull(variable);
			Objects.requireNonNull(origin);
		}
	}

	List<InheritedAttribute> attributes(SpeciesDraft selection);

	/** Filters combined built-in/local candidates using the editor's local inheritance graph. */
	default List<String> parentNames(final List<String> candidates) { return candidates; }

	/** Mirror candidates need not obey parent-cycle filtering. */
	default List<String> mirrorNames(final List<String> fallback) { return fallback; }

	/** Composes platform metadata with an editor's model-local parent/mirror descriptions. */
	default SpeciesWizardContext and(final SpeciesWizardContext additional) {
		return new SpeciesWizardContext() {
			@Override
			public List<InheritedAttribute> attributes(final SpeciesDraft selection) {
				List<InheritedAttribute> result = new ArrayList<>(SpeciesWizardContext.this.attributes(selection));
				result.addAll(additional.attributes(selection));
				return List.copyOf(result);
			}

			@Override
			public List<String> parentNames(final List<String> candidates) {
				return additional.parentNames(SpeciesWizardContext.this.parentNames(candidates));
			}

			@Override
			public List<String> mirrorNames(final List<String> fallback) {
				return additional.mirrorNames(SpeciesWizardContext.this.mirrorNames(fallback));
			}
		};
	}

	/** Registered parents for species and grids; world/experiment and the internal scheduler are not candidates. */
	static List<String> builtInParentNames() {
		return GamaMetaModel.getAllSpeciesDescriptions().stream().map(ISpeciesDescription::getName)
				.filter(name -> builtInParent(name) != null).distinct().sorted().toList();
	}

	/** Read-only built-in parent description, or null for an unknown/inapplicable name. */
	static ISpeciesDescription builtInParent(final String name) {
		if (name == null || GamaMetaModel.INTERNAL_GLOBAL_SCHEDULER_SPECIES.equals(name)) return null;
		var description = GamaMetaModel.getSpeciesDescription(name);
		return description == null || description.isModel() || description.isExperiment() ? null : description;
	}

	static SpeciesWizardContext builtIns() {
		return selection -> {
			List<InheritedAttribute> result = new ArrayList<>();
			addSpecies(result, "agent", "agent");
			if (!"agent".equals(selection.parent())) addSpecies(result, selection.parent(), "parent: " + selection.parent());
			for (String skill : selection.skills()) addSkill(result, skill, "skill: " + skill);
			String control = facet(selection, "control", "reflex").trim();
			addSkill(result, control, "control: " + control);
			String mirror = facet(selection, "mirrors", "").trim();
			if (!mirror.isEmpty()) result.add(new InheritedAttribute(
					new SpeciesDraft.Variable("target", mirror, "", "", "", "", ""), "mirror: " + mirror, false));
			return List.copyOf(result);
		};
	}

	/** Class metadata starts at object, without agent attributes, skills or control architectures. */
	static SpeciesWizardContext classBuiltIns() {
		return selection -> {
			var object = classParent("object");
			if (object == null) return List.of();
			List<InheritedAttribute> result = new ArrayList<>();
			for (var variable : object.getAttributes()) result.add(attribute(variable, "object"));
			return List.copyOf(result);
		};
	}

	/** The built-in class root. Local class parents are supplied by each editor's context. */
	static List<String> classParentNames() { return List.of("object"); }

	/** Read-only object description, or null for a non-class name or an uninitialized compiler registry. */
	static IClassDescription classParent(final String name) {
		return !"object".equals(name) || gama.api.gaml.GAML.getDescriptionFactory() == null ? null
				: GamaMetaModel.getObjectClassDescription();
	}

	static String facet(final SpeciesDraft draft, final String name, final String defaultValue) {
		return draft.facets().stream().filter(facet -> name.equals(facet.name())).map(SpeciesDraft.Facet::value)
				.findFirst().orElse(defaultValue);
	}

	static InheritedAttribute attribute(final IVariableDescription description, final String origin) {
		var variable = new SpeciesDraft.Variable(description.getName(), description.getGamlType().toString(),
				expression(description, "init"), expression(description, "update"), expression(description, "function"),
				expression(description, "min"), expression(description, "max"));
		return new InheritedAttribute(variable, origin, !description.isUnmodifiable());
	}

	private static String expression(final IVariableDescription description, final String name) {
		var expression = description.getFacet(name);
		return expression == null ? "" : expression.serializeToGaml(false);
	}

	private static void addSpecies(final List<InheritedAttribute> result, final String name, final String origin) {
		var description = builtInParent(name);
		if (description != null) for (var attribute : description.getAttributes()) result.add(attribute(attribute, origin));
	}

	private static void addSkill(final List<InheritedAttribute> result, final String name, final String origin) {
		result.addAll(skillAttributes(name, origin));
	}

	/** Skill attributes, including Java superclass/interface additions used by the species compiler. */
	static List<InheritedAttribute> skillAttributes(final String name, final String origin) {
		return GamaSkillRegistry.INSTANCE.getEffectiveVariablesForSkill(name).stream()
				.map(variable -> attribute(variable, origin)).toList();
	}
}
