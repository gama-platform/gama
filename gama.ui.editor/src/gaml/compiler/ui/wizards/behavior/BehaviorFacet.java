package gaml.compiler.ui.wizards.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;

/**
 * Metadata of a facet proposed by a declaration form. {@code type} is the GAML type name ({@code bool} facets are
 * shown as check boxes, others as expressions). Read from the compiler's statement declarations when available, with a
 * built-in copy of those declarations otherwise (e.g. outside the platform).
 */
public record BehaviorFacet(String name, String type, boolean required, String documentation) {

	public BehaviorFacet {
		Objects.requireNonNull(name);
		type = type == null ? "" : type;
		documentation = documentation == null ? "" : documentation;
	}

	public boolean bool() {
		return "bool".equals(type);
	}

	private static BehaviorFacet f(final String name, final String type, final boolean required, final String doc) {
		return new BehaviorFacet(name, type, required, doc);
	}

	private static final String PREDICATE = "predicate";

	/** Copies of the @facets declarations (ReflexStatement, FsmStateStatement, ...), used without the registry. */
	private static final Map<BehaviorKind, List<BehaviorFacet>> DECLARED = Map.of(
			BehaviorKind.REFLEX, List.of(f("when", "bool", false, "an expression that evaluates a boolean, the condition to fulfill in order to execute the statements embedded in the reflex.")),
			BehaviorKind.STATE, List.of(f("initial", "bool", false, "specifies whether the state is the initial one (default value = false)"),
					f("final", "bool", false, "specifies whether the state is a final one (i.e. there is no transition from this state to another state) (default value= false)")),
			BehaviorKind.TASK, List.of(f("weight", "float", true, "the priority level of the task")),
			BehaviorKind.PLAN, List.of(f("when", "bool", false, "the condition to execute the plan"),
					f("finished_when", "bool", false, "the condition to end the plan"),
					f("priority", "float", false, "the priority of the plan"),
					f("intention", PREDICATE, false, "the intention the plan answers"),
					f("emotion", "emotion", false, "the emotion triggering the plan"),
					f("threshold", "float", false, "the threshold on the emotion's intensity"),
					f("instantaneous", "bool", false, "whether the plan is executed within the same cycle")),
			BehaviorKind.PERCEIVE, List.of(f("target", "container", true, "the list of the agent you want to perceive"),
					f("as", "species", false, "an expression that evaluates to a species"),
					f("when", "bool", false, "a boolean to tell when the perceive is active"),
					f("parallel", "bool", false, "setting this facet to 'true' will allow 'perceive' to use concurrency with a parallel_bdi architecture; setting it to an integer will set the threshold under which they will be run sequentially"),
					f("in", "float", false, "a float or a geometry. If it is a float, it's a radius of a detection area. If it is a geometry, it is the area of detection of others species."),
					f("emotion", "emotion", false, "The emotion needed to do the perception"),
					f("threshold", "float", false, "Threshold linked to the emotion.")),
			BehaviorKind.RULE, rule());

	private static List<BehaviorFacet> rule() {
		List<BehaviorFacet> result = new ArrayList<>();
		for (String n : List.of("belief", "desire", "uncertainty", "ideal", "obligation", "new_desire", "new_belief",
				"new_uncertainty", "new_ideal", "remove_belief", "remove_ideal", "remove_desire", "remove_intention",
				"remove_uncertainty", "remove_obligation"))
			result.add(f(n, PREDICATE, false, ""));
		for (String n : List.of("emotion", "new_emotion", "remove_emotion")) result.add(f(n, "emotion", false, ""));
		for (String n : List.of("desires", "beliefs", "emotions", "uncertainties", "ideals", "obligations",
				"new_desires", "new_beliefs", "new_emotions", "new_uncertainties", "new_ideals", "remove_beliefs",
				"remove_desires", "remove_emotions", "remove_ideals", "remove_obligations", "remove_uncertainties"))
			result.add(f(n, "list", false, ""));
		result.add(f("when", "bool", false, ""));
		result.add(f("threshold", "float", false, ""));
		result.add(f("parallel", "bool", false, ""));
		result.add(f("strength", "float", false, ""));
		result.add(f("all", "bool", false, ""));
		return List.copyOf(result);
	}

	/** The facets proposed for a kind (excluding {@code name}), in declaration order. */
	public static List<BehaviorFacet> of(final BehaviorKind kind) {
		List<BehaviorFacet> registered = fromRegistry(kind.keyword());
		return registered.isEmpty() ? DECLARED.get(kind) : registered;
	}

	/** Facets (except name and internal ones) declared for a keyword in the compiler, or an empty list. */
	public static List<BehaviorFacet> fromRegistry(final String keyword) {
		try {
			IArtefact.Symbol symbol = ArtefactRegistry.getStatementArtefact(keyword);
			if (symbol == null) return List.of();
			List<BehaviorFacet> result = new ArrayList<>();
			symbol.getPossibleFacets().forEach((name, facet) -> {
				if ("name".equals(name) || facet.isInternal()) return;
				var types = facet.getTypes();
				String type = types == null || types.length == 0 ? "" : types[0].getName();
				String doc = facet.getDocumentation() == null ? "" : facet.getDocumentation().getContents();
				result.add(new BehaviorFacet(name, type, !facet.isOptional(), doc));
			});
			return List.copyOf(result);
		} catch (RuntimeException | LinkageError unavailable) {
			return List.of();
		}
	}
}
