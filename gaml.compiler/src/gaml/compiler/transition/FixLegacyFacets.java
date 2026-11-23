package gaml.compiler.transition;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.impl.BinaryGrammarResourceFactoryImpl;

import gama.annotations.constants.IKeyword;
import gama.api.kernel.simulation.ISimulationAgent;
import gaml.compiler.GamlStandaloneSetupGenerated;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.ActionRef;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.FacetsAndBlock;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.Parameter;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Display;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.VariableRef;

/**
 * Updates legacy selectors, layer referentials and statically identifiable experiment ownership in parseable GAML source.
 */
final class FixLegacyFacets implements IFileTransformer {

	private static final String SYNTAX_REVIEW =
			"/* TODO GAMA 2026 (syntax): Fix the remaining syntax errors and run the converter again; "
					+ "selectors and ownership cannot be migrated safely until the file parses. */";

	// SimulationAgent's built-in attributes are unavailable through the standalone parser's metamodel.
	private static final Set<String> SIMULATION_BUILTINS = Set.of(
			IKeyword.NAME, IKeyword.INDEX, IKeyword.PEERS, IKeyword.HOST, IKeyword.LOCATION, IKeyword.SHAPE,
			IKeyword.COLOR, IKeyword.SEED, IKeyword.RNG, IKeyword.EXPERIMENT, IKeyword.WORLD_AGENT_NAME,
			IKeyword.STEP, ISimulationAgent.TIME, IKeyword.CYCLE, ISimulationAgent.USAGE, ISimulationAgent.PAUSED,
			"is_on_user_hold", ISimulationAgent.DURATION, ISimulationAgent.TOTAL_DURATION,
			ISimulationAgent.AVERAGE_DURATION, ISimulationAgent.CURRENT_DATE, ISimulationAgent.STARTING_DATE);

	private static final Set<String> SHARED_BUILTINS = Set.of(
			IKeyword.NAME, IKeyword.INDEX, IKeyword.PEERS, IKeyword.HOST, IKeyword.LOCATION, IKeyword.SHAPE,
			IKeyword.SEED, IKeyword.RNG, IKeyword.CYCLE, ISimulationAgent.USAGE);

	private static final Set<String> EXPERIMENT_CONTROL_NAMES =
			Set.of("pause", "play", "step_once", "update_outputs", "simulations");

	private static class Parser {
		private static final IParser INSTANCE = create();

		private static IParser create() {
			// Load the binary grammar without replacing the IDE's GAML resource service provider.
			XtextPackage.eINSTANCE.eClass();
			GamlPackage.eINSTANCE.eClass();
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
					.putIfAbsent("xtextbin", new BinaryGrammarResourceFactoryImpl());
			return new GamlStandaloneSetupGenerated().createInjector().getInstance(IParser.class);
		}
	}

	private record Edit(int start, int end, String replacement) {}

	private record Review(String kind, String message) {

		String comment() { return "/* TODO GAMA 2026 (" + kind + "): " + message + " */"; }
	}

	@Override
	public String transform(final String content) {
		if (content.startsWith("\uFEFF")) return "\uFEFF" + transform(content.substring(1));
		final var parsed = Parser.INSTANCE.parse(new StringReader(content));
		final var root = parsed.getRootASTElement();
		// Recovery nodes are not reliable source ranges for in-place migration.
		final int start = 0;
		if (root == null || parsed.hasSyntaxErrors()) {
			if (content.startsWith(SYNTAX_REVIEW, start)) return content;
			return content.substring(0, start) + SYNTAX_REVIEW + lineSeparator(content) + content.substring(start);
		}

		final List<Edit> edits = new ArrayList<>();
		if (content.startsWith(SYNTAX_REVIEW, start)) {
			int end = start + SYNTAX_REVIEW.length();
			if (end < content.length() && content.charAt(end) == '\r') end++;
			if (end < content.length() && content.charAt(end) == '\n') end++;
			edits.add(new Edit(start, end, ""));
		}
		final Map<FacetsAndBlock, List<Review>> reviews = new LinkedHashMap<>();
		final List<FacetsAndBlock> statements = new ArrayList<>();
		if (root instanceof FacetsAndBlock statement) statements.add(statement);
		for (final var children = root.eAllContents(); children.hasNext();) {
			if (children.next() instanceof FacetsAndBlock statement) statements.add(statement);
		}
		final List<FacetsAndBlock> experiments =
				statements.stream().filter(s -> "experiment".equals(s.getKey())).toList();
		final Model model = root instanceof Model m ? m : null;
		final Set<String> globalAttributes = globalAttributes(model);
		for (final FacetsAndBlock statement : statements) {
			collectEdits(content, statement, experiments, model, globalAttributes, edits, reviews);
		}
		final Set<String> simulationNames = simulationNames(model);
		for (final FacetsAndBlock experiment : experiments) {
			reviewExperimentReferences(experiment, experiments, model, simulationNames, reviews);
		}
		for (final var children = root.eAllContents(); children.hasNext();) {
			if (children.next() instanceof UnitName unit) reviewDisplayDimension(unit, reviews);
		}
		reviews.forEach((statement, notes) -> addReviews(content, statement, notes, edits));
		if (edits.isEmpty()) return content;
		edits.sort(Comparator.comparingInt(Edit::start).reversed()
				.thenComparing(Comparator.comparingInt(Edit::end).reversed()));
		final var result = new StringBuilder(content);
		for (final Edit edit : edits) {
			result.replace(edit.start(), edit.end(), edit.replacement());
		}
		return result.toString();
	}

	private static void collectEdits(final String content, final FacetsAndBlock statement,
			final List<FacetsAndBlock> experiments, final Model model, final Set<String> globalAttributes,
			final List<Edit> edits, final Map<FacetsAndBlock, List<Review>> reviews) {
		final String keyword = statement.getKey();
		if (keyword == null) return;
		switch (keyword) {
			case "experiment" -> {
				renameFacet(statement, "type:", "mode:", edits, reviews);
				reviewQuotedName(statement, reviews);
			}
			case "display" -> {
				renameFacet(statement, "type:", "renderer:", edits, reviews);
				reviewQuotedName(statement, reviews);
			}
			case "image" -> {
				if (statement.eContainer() != null
						&& statement.eContainer().eContainer() instanceof S_Display display
						&& display.getBlock() == statement.eContainer()) {
					final INode key = statementKey(statement);
					edits.add(new Edit(key.getOffset(), key.getEndOffset(), "picture"));
				}
			}
			case "chart", "light" -> renameFacet(statement, "type:", "kind:", edits, reviews);
			case "event" -> renameFacet(statement, "type:", "device:", edits, reviews);
			case "inspect", "browse" -> migrateInspection(content, statement, edits, reviews);
			case "graphics" -> renameFacet(statement, "content_coordinates:", "referential:", edits, reviews);
			case "overlay" -> {
				renameFacet(statement, "content_coordinates:", "referential:", edits, reviews);
				review(reviews, statement, "overlay coordinates", "Review position, size and contents: overlay "
						+ "units and scaling changed. The correct coordinates depend on the intended layout.");
			}
			case "permanent" -> {
				migratePermanent(statement, edits, reviews);
				reviewOwnerReferences(statement, experiments, model, reviews);
			}
			case "parameter" -> migrateParameter(statement, experiments, model, globalAttributes, edits, reviews);
			case "output", "user_command" -> reviewOwnerReferences(statement, experiments, model, reviews);
		}
	}

	private static void reviewDisplayDimension(final UnitName unit,
			final Map<FacetsAndBlock, List<Review>> reviews) {
		final String name = NodeModelUtils.getTokenText(NodeModelUtils.getNode(unit));
		if (!"display_width".equals(name) && !"display_height".equals(name)) return;
		EObject parent = unit.eContainer();
		while (parent != null && !(parent instanceof FacetsAndBlock)) { parent = parent.eContainer(); }
		if (parent instanceof FacetsAndBlock statement) {
			review(reviews, statement, "display dimensions", "#display_width and #display_height now measure "
					+ "the viewport in logical pixels, not the zoom-dependent world extent. Review this calculation "
					+ "and its coordinate units.");
		}
	}

	private static void reviewOwnerReferences(final FacetsAndBlock statement, final List<FacetsAndBlock> experiments,
			final Model model, final Map<FacetsAndBlock, List<Review>> reviews) {
		final FacetsAndBlock experiment = declaringExperiment(statement);
		if (experiment == null) return;
		final Set<String> experimentNames = new HashSet<>(EXPERIMENT_CONTROL_NAMES);
		final boolean complete = collectExperimentNames(experiment, experiments, new HashSet<>(), experimentNames);
		if (!complete || model == null || !model.getImports().isEmpty()) {
			review(reviews, statement, "owner declarations", "Imported or inherited declarations are not resolved "
					+ "by this converter. Check references and actions against the selected owner, including "
					+ "callbacks and nested ask contexts.");
		}
		if ("permanent".equals(statement.getKey()) && !hasOwner(statement)
				|| hasOwner(statement) && !hasNamedOwner(statement, "simulation")) return;
		final Set<String> candidates = referencedNames(statement, experimentNames);
		if (!candidates.isEmpty()) {
			review(reviews, statement, "owner references", "Review possible experiment references "
					+ String.join(", ", candidates) + ": output and commands default to the simulation. "
					+ "Check shadowing and nested contexts; qualify controller state/actions or choose an "
					+ "experiment owner or output locals according to the intended lifetime and behavior.");
		}
	}

	private static boolean hasNamedOwner(final FacetsAndBlock statement, final String name) {
		final Facet owner = uniqueFacet(statement, "of:");
		return owner != null && owner.getExpr() instanceof VariableRef
				&& name.equals(NodeModelUtils.getTokenText(NodeModelUtils.getNode(owner.getExpr())));
	}

	private static Set<String> simulationNames(final Model model) {
		final Set<String> names = new HashSet<>();
		if (model == null || model.getBlock() == null) return names;
		for (final var section : model.getBlock().getStatements()) {
			if (section instanceof S_Species species
					&& ("species".equals(species.getKey()) || "grid".equals(species.getKey()))) {
				names.add(species.getName());
			} else if ("global".equals(section.getKey()) && section.getBlock() != null) {
				for (final var member : section.getBlock().getStatements()) {
					if (member instanceof S_Definition definition) names.add(definition.getName());
				}
			}
		}
		return names;
	}

	private static void reviewExperimentReferences(final FacetsAndBlock experiment,
			final List<FacetsAndBlock> experiments, final Model model, final Set<String> simulationNames,
			final Map<FacetsAndBlock, List<Review>> reviews) {
		if (hasUnknownSimulationAttributes(model)
				|| !collectExperimentNames(experiment, experiments, new HashSet<>(), new HashSet<>())) {
			review(reviews, experiment, "owner declarations", "Imported, inherited or skill-provided declarations "
					+ "are not resolved by this converter. Review experiment and simulation contexts and "
					+ "compiler diagnostics.");
		}
		if (simulationNames.isEmpty()) return;
		final Set<String> facetNames = new TreeSet<>();
		for (final Facet facet : experiment.getFacets()) {
			facetNames.addAll(referencedNames(facet, simulationNames));
		}
		reviewSimulationNames(experiment, facetNames, reviews);
		if (experiment.getBlock() == null) return;
		for (final var member : experiment.getBlock().getStatements()) {
			final String key = member.getKey();
			if ("parameter".equals(key)) continue;
			if ("output".equals(key) || "user_command".equals(key)) {
				if (!hasNamedOwner(member, "experiment")) continue;
			} else if ("permanent".equals(key) && hasOwner(member)
					&& !hasNamedOwner(member, "experiment")) {
				continue;
			}
			reviewSimulationNames(member, referencedNames(member, simulationNames), reviews);
		}
	}

	private static void reviewSimulationNames(final FacetsAndBlock statement, final Set<String> names,
			final Map<FacetsAndBlock, List<Review>> reviews) {
		if (!names.isEmpty()) {
			review(reviews, statement, "simulation references", "Review possible simulation references "
					+ String.join(", ", names) + " in this experiment context. Check shadowing and nested "
					+ "contexts; use an explicit simulation reference or ask simulation when appropriate.");
		}
	}

	private static Set<String> referencedNames(final EObject root, final Set<String> names) {
		final Set<String> result = new TreeSet<>();
		for (final var children = root.eAllContents(); children.hasNext();) {
			final EObject child = children.next();
			if (!(child instanceof VariableRef) && !(child instanceof ActionRef)) continue;
			if (isQualifiedOrNamedArgument(child)) continue;
			final String name = NodeModelUtils.getTokenText(NodeModelUtils.getNode(child));
			if (names.contains(name)) result.add(name);
		}
		return result;
	}

	private static boolean collectExperimentNames(final FacetsAndBlock experiment,
			final List<FacetsAndBlock> experiments, final Set<FacetsAndBlock> visited, final Set<String> names) {
		if (!visited.add(experiment) || experiment.getBlock() == null) return false;
		for (final var member : experiment.getBlock().getStatements()) {
			if (member instanceof S_Definition definition) names.add(definition.getName());
		}
		final Facet parent = uniqueFacet(experiment, "parent:");
		if (parent == null) return experiment.getFacets().stream().noneMatch(f -> hasKey(f, "parent:"));
		if (!(parent.getExpr() instanceof VariableRef)) return false;
		final String parentName = NodeModelUtils.getTokenText(NodeModelUtils.getNode(parent.getExpr()));
		final var candidates = experiments.stream()
				.filter(candidate -> candidate.eContainer() == experiment.eContainer()
						&& parentName.equals(experimentName(candidate)))
				.toList();
		return candidates.size() == 1 && collectExperimentNames(candidates.getFirst(), experiments, visited, names);
	}

	private static boolean isQualifiedOrNamedArgument(final EObject reference) {
		EObject expression = reference;
		if (reference instanceof ActionRef && reference.eContainer() instanceof Function function) {
			expression = function;
		}
		final EObject parent = expression.eContainer();
		if (parent instanceof Access access && ".".equals(access.getOp()) && access.getRight() == expression) {
			return true;
		}
		if (parent instanceof Parameter parameter && parameter.getLeft() == reference) return true;
		return parent instanceof S_Do call && call.getTarget() != null && call.getExpr() == expression;
	}

	private static void reviewQuotedName(final FacetsAndBlock statement,
			final Map<FacetsAndBlock, List<Review>> reviews) {
		final var feature = switch (statement) {
			case S_Display ignored -> GamlPackage.Literals.SDISPLAY__NAME;
			case S_Experiment ignored -> GamlPackage.Literals.GAML_DEFINITION__NAME;
			case StandaloneExperiment ignored -> GamlPackage.Literals.STANDALONE_EXPERIMENT__NAME;
			default -> null;
		};
		if (feature == null) return;
		final var nodes = NodeModelUtils.findNodesForFeature(statement, feature);
		if (nodes.isEmpty()) return;
		final String name = NodeModelUtils.getTokenText(nodes.getFirst());
		if (name.startsWith("\"") || name.startsWith("'")) {
			review(reviews, statement, "identifier", "Quoted names are retained. If converting to an identifier and "
					+ "title:, check for collisions and update model and external references.");
		}
	}

	private static String lineSeparator(final String content) {
		final int newline = content.indexOf('\n');
		if (newline >= 0) return newline > 0 && content.charAt(newline - 1) == '\r' ? "\r\n" : "\n";
		return content.indexOf('\r') >= 0 ? "\r" : "\n";
	}

	private static void review(final Map<FacetsAndBlock, List<Review>> reviews, final FacetsAndBlock statement,
			final String kind, final String message) {
		final List<Review> notes = reviews.computeIfAbsent(statement, ignored -> new ArrayList<>());
		if (notes.stream().noneMatch(note -> note.kind().equals(kind))) notes.add(new Review(kind, message));
	}

	private static boolean hasReview(final String content, final int offset, final String kind) {
		int end = offset;
		while (end > 0) {
			while (end > 0 && Character.isWhitespace(content.charAt(end - 1))) end--;
			if (end < 2 || !content.startsWith("*/", end - 2)) return false;
			final int start = content.lastIndexOf("/*", end - 2);
			if (start < 0) return false;
			if (content.startsWith("/* TODO GAMA 2026 (" + kind + "):", start)) return true;
			end = start;
		}
		return false;
	}

	private static void addReviews(final String content, final FacetsAndBlock statement, final List<Review> reviews,
			final List<Edit> edits) {
		final int offset = NodeModelUtils.getNode(statement).getOffset();
		final int lineStart = Math.max(content.lastIndexOf('\n', offset - 1), content.lastIndexOf('\r', offset - 1)) + 1;
		final String indentation = content.substring(lineStart, offset);
		final boolean atLineStart = indentation.chars().allMatch(c -> c == ' ' || c == '\t');
		final String separator = atLineStart ? lineSeparator(content) + indentation : " ";
		final var text = new StringBuilder();
		for (final Review review : reviews) {
			if (!hasReview(content, offset, review.kind())) text.append(review.comment()).append(separator);
		}
		if (!text.isEmpty()) edits.add(new Edit(offset, offset, text.toString()));
	}

	private static INode statementKey(final FacetsAndBlock statement) {
		return NodeModelUtils.findNodesForFeature(statement, GamlPackage.Literals.FACETS_AND_BLOCK__KEY).getFirst();
	}

	private static FacetsAndBlock declaringExperiment(final FacetsAndBlock statement) {
		if (statement.eContainer() != null
				&& statement.eContainer().eContainer() instanceof FacetsAndBlock owner
				&& "experiment".equals(owner.getKey()) && owner.getBlock() == statement.eContainer()) return owner;
		return null;
	}

	private static boolean hasOwner(final FacetsAndBlock statement) {
		return statement.getFacets().stream().anyMatch(f -> hasKey(f, "of:"));
	}

	private static void migratePermanent(final FacetsAndBlock statement, final List<Edit> edits,
			final Map<FacetsAndBlock, List<Review>> reviews) {
		if (declaringExperiment(statement) == null) return;
		if (statement.getBlock() == null) {
			review(reviews, statement, "output", "This permanent output has no block, so it cannot be converted "
					+ "safely. Review its contents and use output of: experiment if appropriate.");
			return;
		}
		if (hasOwner(statement)) {
			review(reviews, statement, "output", "This permanent output has an explicit of: facet; "
					+ "choose the intended owner before replacing it with output.");
			return;
		}
		final INode key = statementKey(statement);
		edits.add(new Edit(key.getOffset(), key.getEndOffset(), "output of: experiment"));
	}

	private static void migrateParameter(final FacetsAndBlock statement, final List<FacetsAndBlock> experiments,
			final Model model, final Set<String> globalAttributes, final List<Edit> edits,
			final Map<FacetsAndBlock, List<Review>> reviews) {
		final FacetsAndBlock experiment = declaringExperiment(statement);
		if (experiment == null) return;
		if (statement.getBlock() != null || statement.getFacets().stream().anyMatch(f -> hasKey(f, "on_change:"))) {
			review(reviews, statement, "parameter callback", "Review this callback: it runs in the selected owner's "
					+ "context. Cross-owner references depend on model behavior and cannot be rewritten safely; "
					+ "qualify them as needed.");
		}
		if (hasOwner(statement)) return;
		final Facet target = uniqueFacet(statement, "var:");
		if (target == null || !(target.getExpr() instanceof VariableRef)) {
			review(reviews, statement, "parameter owner", "Cannot infer the target from its var: facet. Check whether "
					+ "the parameter belongs to the simulation (the default) or needs of: experiment.");
			return;
		}
		final INode expression = NodeModelUtils.getNode(target.getExpr());
		final String name = NodeModelUtils.getTokenText(expression);
		final boolean experimentAttribute = declaresAttribute(experiment, name, experiments, new HashSet<>());
		final boolean simulationAttribute = globalAttributes.contains(name) || SIMULATION_BUILTINS.contains(name);
		final boolean unknownSimulationAttributes = hasUnknownSimulationAttributes(model);
		if (experimentAttribute && !simulationAttribute && !unknownSimulationAttributes) {
			edits.add(new Edit(expression.getEndOffset(), expression.getEndOffset(), " of: experiment"));
		} else if (experimentAttribute && simulationAttribute) {
			review(reviews, statement, "parameter owner", "Both the experiment and simulation define " + name
					+ "; older parameters selected the simulation first. Verify the intended owner and add "
					+ "of: experiment only for the experiment.");
		} else if (experimentAttribute) {
			review(reviews, statement, "parameter owner", "The simulation may also define " + name
					+ " through another model, a skill or an architecture; simulation attributes took precedence "
					+ "over experiment attributes. Check the intended owner before adding of: experiment.");
		} else if (SHARED_BUILTINS.contains(name)) {
			review(reviews, statement, "parameter owner", name + " is available on both the simulation and "
					+ "experiment, so its intended owner cannot be inferred. The default is simulation; "
					+ "add of: experiment if needed.");
		} else if (!simulationAttribute) {
			review(reviews, statement, "parameter owner", "The declaration of " + name
					+ " is not available in this file and may be inherited, imported, provided by a skill or built in. "
					+ "Check whether its owner is simulation (the default) or experiment (add of: experiment).");
		}
	}

	private static boolean hasUnknownSimulationAttributes(final Model model) {
		if (model == null || model.getBlock() == null || !model.getImports().isEmpty()) return true;
		return model.getBlock().getStatements().stream().filter(s -> "global".equals(s.getKey()))
				.anyMatch(s -> s.getFacets().stream()
						.anyMatch(f -> hasKey(f, "skills:") || hasKey(f, "control:") || hasKey(f, "parent:")));
	}

	private static Set<String> globalAttributes(final Model model) {
		final Set<String> names = new HashSet<>();
		if (model == null || model.getBlock() == null) return names;
		for (final var section : model.getBlock().getStatements()) {
			if (!"global".equals(section.getKey()) || section.getBlock() == null) continue;
			for (final var member : section.getBlock().getStatements()) {
				if (member instanceof S_Definition variable && !(variable instanceof S_Callable)) {
					names.add(variable.getName());
				}
			}
		}
		return names;
	}

	private static boolean declaresAttribute(final FacetsAndBlock experiment, final String name,
			final List<FacetsAndBlock> experiments, final Set<FacetsAndBlock> visited) {
		if (!visited.add(experiment) || experiment.getBlock() == null) return false;
		for (final var member : experiment.getBlock().getStatements()) {
			if (member instanceof S_Definition variable && !(variable instanceof S_Callable)
					&& name.equals(variable.getName())) return true;
		}
		final Facet parent = uniqueFacet(experiment, "parent:");
		if (parent == null || !(parent.getExpr() instanceof VariableRef)) return false;
		final String parentName = NodeModelUtils.getTokenText(NodeModelUtils.getNode(parent.getExpr()));
		final var candidates = experiments.stream()
				.filter(candidate -> candidate.eContainer() == experiment.eContainer()
						&& parentName.equals(experimentName(candidate)))
				.toList();
		return candidates.size() == 1 && declaresAttribute(candidates.getFirst(), name, experiments, visited);
	}

	private static String experimentName(final FacetsAndBlock experiment) {
		return switch (experiment) {
			case S_Experiment declaration -> declaration.getName();
			case StandaloneExperiment declaration -> declaration.getName();
			default -> null;
		};
	}

	private static Facet uniqueFacet(final FacetsAndBlock statement, final String key) {
		Facet found = null;
		for (final Facet facet : statement.getFacets()) {
			if (hasKey(facet, key)) {
				if (found != null) return null;
				found = facet;
			}
		}
		return found;
	}

	private static INode keyNode(final Facet facet) {
		return NodeModelUtils.findNodesForFeature(facet, GamlPackage.Literals.FACET__KEY).getFirst();
	}

	private static boolean hasKey(final Facet facet, final String key) {
		if (key.equals(facet.getKey())) return true;
		// Xtext data type rules can include the hidden text between the identifier and colon.
		if (!facet.getKey().endsWith(":")) return false;
		final var spelling = new StringBuilder();
		for (final ILeafNode leaf : keyNode(facet).getLeafNodes()) {
			if (!leaf.isHidden()) spelling.append(leaf.getText());
		}
		return key.contentEquals(spelling);
	}

	private static void renameFacet(final FacetsAndBlock statement, final String oldKey, final String newKey,
			final List<Edit> edits, final Map<FacetsAndBlock, List<Review>> reviews) {
		final long oldCount = statement.getFacets().stream().filter(f -> hasKey(f, oldKey)).count();
		if (oldCount == 0) return;
		final Facet legacy = uniqueFacet(statement, oldKey);
		if (legacy == null || statement.getFacets().stream().anyMatch(f -> hasKey(f, newKey))) {
			review(reviews, statement, "facet", "Cannot replace " + oldKey + " with " + newKey
					+ " automatically because the statement has duplicate or conflicting facets; choose the intended value.");
			return;
		}
		// Edit just the identifier: whitespace or comments before the colon can remain as written.
		for (final ILeafNode leaf : keyNode(legacy).getLeafNodes()) {
			if (!leaf.isHidden()) {
				final boolean includesColon = leaf.getText().endsWith(":");
				edits.add(new Edit(leaf.getOffset(), leaf.getEndOffset(),
						includesColon ? newKey : newKey.substring(0, newKey.length() - 1)));
				break;
			}
		}
	}

	private static void migrateInspection(final String content, final FacetsAndBlock statement,
			final List<Edit> edits, final Map<FacetsAndBlock, List<Review>> reviews) {
		if (statement.getFacets().stream().noneMatch(f -> hasKey(f, "type:"))) return;
		final Facet type = uniqueFacet(statement, "type:");
		if (type == null || type.getExpr() == null) {
			review(reviews, statement, "inspection", "Review the duplicate or missing type: selector; "
					+ "use inspect for agents or browse for tables, without a type: facet.");
			return;
		}
		final String replacement = switch (NodeModelUtils.getTokenText(NodeModelUtils.getNode(type.getExpr()))) {
			case "agent", "'agent'", "\"agent\"" -> "inspect";
			case "table", "'table'", "\"table\"" -> "browse";
			default -> null;
		};
		if (replacement == null) {
			review(reviews, statement, "inspection", "The type: value cannot be converted automatically; "
					+ "choose inspect for agents or browse for tables, without a type: facet.");
			return;
		}
		if (!replacement.equals(statement.getKey())) {
			final INode key = statementKey(statement);
			edits.add(new Edit(key.getOffset(), key.getEndOffset(), replacement));
		}

		final INode facet = NodeModelUtils.getNode(type);
		final List<ILeafNode> tokens = new ArrayList<>();
		boolean hasComment = false;
		for (final ILeafNode leaf : facet.getLeafNodes()) {
			if (!leaf.isHidden()) {
				tokens.add(leaf);
			} else if (leaf.getText().startsWith("/")) {
				hasComment = true;
			}
		}
		int start = tokens.getFirst().getOffset();
		final int end = tokens.getLast().getEndOffset();
		if (hasComment) {
			for (final ILeafNode token : tokens) {
				edits.add(new Edit(token.getOffset(), token.getEndOffset(), ""));
			}
		} else {
			while (start > 0 && (content.charAt(start - 1) == ' ' || content.charAt(start - 1) == '\t')) {
				start--;
			}
			edits.add(new Edit(start, end, ""));
		}
	}
}
