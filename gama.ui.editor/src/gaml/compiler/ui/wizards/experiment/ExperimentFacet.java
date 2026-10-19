package gaml.compiler.ui.wizards.experiment;

import java.util.List;

import gama.api.utils.prefs.GamaPreferences;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/**
 * Header facets of experiments, as declared by the compiler ({@code ExperimentSpecies}), grouped by the experiment
 * type they apply to. {@code flag} facets have literal-only checkbox controls: existing expressions remain in the
 * detached draft until explicitly replaced by a checkbox edit. Conditions such as {@code until} and {@code record}
 * remain GAML expressions. Record initially displays false without rewriting an omitted source facet.
 *
 * @param section
 *            {@link #COMMON}, or the experiment type (gui, batch) for which the facet is meaningful
 */
public record ExperimentFacet(String name, String type, boolean flag, String section, String documentation) {

	/** Defaults used by ExperimentSpecies; inherited experiment facets require unresolved owner context. */
	public Boolean defaultValue(final List<Facet> facets) {
		if (facets.stream().anyMatch(f -> "parent".equals(f.name()))) return null;
		return switch (name) {
			case "virtual", "benchmark", "keep_seed" -> false;
			case "keep_simulations" -> true;
			case "autorun" -> GamaPreferences.Runtime.CORE_AUTO_RUN.getValue();
			default -> null;
		};
	}

	public static final String COMMON = "common";

	public static final List<ExperimentFacet> DECLARED = List.of(
			new ExperimentFacet("title", "string", false, COMMON, "Title shown for the experiment (a string)"),
			new ExperimentFacet("parent", "id", false, COMMON, "The experiment this one inherits from"),
			new ExperimentFacet("skills", "list", false, COMMON, "Skills attached to the experiment, e.g. [moving]"),
			new ExperimentFacet("control", "id", false, COMMON, "The control architecture of the experiment"),
			new ExperimentFacet("schedules", "container", false, COMMON,
					"The agents scheduled by the experiment (default: the simulations)"),
			new ExperimentFacet("parallel", "bool or int", false, COMMON,
					"Runs simulations in parallel: true/false, or the number of simulations run at once"),
			new ExperimentFacet("virtual", "bool", true, COMMON, "A virtual experiment cannot be run, only inherited"),
			new ExperimentFacet("autorun", "bool", true, COMMON, "Starts the experiment as soon as it is opened"),
			new ExperimentFacet("benchmark", "bool", true, COMMON,
					"Records the time spent in each statement and writes it to a CSV file"),
			new ExperimentFacet("record", "bool", false, ExperimentDraft.GUI,
					"Condition (evaluated each cycle) to record the simulation so that it can be replayed backwards"),
			new ExperimentFacet("keep_seed", "bool", true, ExperimentDraft.BATCH,
					"Keeps the same random seed between the simulations of a batch exploration"),
			new ExperimentFacet("keep_simulations", "bool", true, ExperimentDraft.BATCH,
					"Keeps the simulations of a batch exploration in memory (outputs can be inspected)"),
			new ExperimentFacet("repeat", "int", false, ExperimentDraft.BATCH,
					"Number of simulations run for each parameter set (replications)"),
			new ExperimentFacet("until", "bool", false, ExperimentDraft.BATCH,
					"Condition ending each simulation of the exploration"));

	/** The section of a declared facet, or null for facets not declared for experiments. */
	public static String section(final String facet) {
		for (var declared : DECLARED) if (declared.name().equals(facet)) return declared.section();
		return null;
	}

	/** Declared facets of a section, in display order. */
	public static List<ExperimentFacet> of(final String section) {
		return DECLARED.stream().filter(facet -> facet.section().equals(section)).toList();
	}

	/** Dedicated facets of parameters (others are kept verbatim). {@code var} and the title are separate fields. */
	public static final List<String> PARAMETER_FIELDS = List.of("category", "init", "min", "max", "step", "among", "unit");

	/** Boolean parameter facets shown as check boxes. */
	public static final List<String> PARAMETER_FLAGS = List.of("slider", "read_only");

	/** Dedicated facets of monitors (others are kept verbatim). {@code value} and the title are separate fields. */
	public static final List<String> MONITOR_FIELDS = List.of("color", "refresh");
}
