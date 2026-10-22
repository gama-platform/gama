package gaml.compiler.ui.wizards.species;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.gaml.Array;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Definition;

/** Lossless source adapter: only changed header tokens are replaced; body nodes are never serialized. */
public final class SpeciesSource {

	record Patch(int offset, int length, String text) {}

	private final String source;
	private final IParser parser;
	private final Model model;
	private final S_Species species;
	private final SpeciesDraft original;
	private final SpeciesWizard.Kind kind;

	/** A negative offset selects top-level creation; otherwise select the enclosing ordinary species. */
	public SpeciesSource(final String source, final int offset, final IParser parser) {
		this(source, offset, parser, SpeciesWizard.Kind.SPECIES);
	}

	public SpeciesSource(final String source, final int offset, final IParser parser, final SpeciesWizard.Kind creationKind) {
		this.source = source;
		this.parser = parser;
		var parsed = parser.parse(new StringReader(source));
		if (parsed.hasSyntaxErrors()) throw new IllegalArgumentException("Fix the document's syntax errors first.");
		if (!(parsed.getRootASTElement() instanceof Model root))
			throw new IllegalArgumentException("Species declarations require a GAML model, not an experiment file.");
		model = root;
		S_Species selected = null;
		var contents = model.eAllContents();
		while (contents.hasNext()) {
			EObject object = contents.next();
			if (object instanceof S_Species candidate) {
				INode node = NodeModelUtils.findActualNodeFor(candidate);
				if (offset >= node.getOffset() && offset < node.getEndOffset()
						&& (selected == null || node.getLength() < NodeModelUtils.findActualNodeFor(selected).getLength()))
					selected = candidate;
			}
		}
		if (offset >= 0 && (selected == null || !List.of("species", "grid", "class").contains(selected.getKey())))
			throw new IllegalArgumentException("Select a species, grid or class declaration (not global or world).");
		species = selected;
		kind = species == null ? creationKind : switch (species.getKey()) {
			case "grid" -> SpeciesWizard.Kind.GRID;
			case "class" -> SpeciesWizard.Kind.CLASS;
			default -> SpeciesWizard.Kind.SPECIES;
		};
		if (species != null && species.getBlock() == null)
			throw new IllegalArgumentException("This species has no editable block. Edit its declaration in the source.");
		original = species == null ? SpeciesDraft.empty() : readHeader(species);
	}

	public SpeciesDraft draft() { return original; }

	public SpeciesWizard.Kind kind() { return kind; }

	/** A declaration header or the complete declaration selects a species, not its nested statements. */
	public boolean isSpeciesSelection(final int offset, final int length) {
		if (species == null || offset < 0 || length < 0) return false;
		INode node = NodeModelUtils.findActualNodeFor(species);
		INode block = NodeModelUtils.findActualNodeFor(species.getBlock());
		long end = (long) offset + length;
		return (offset >= node.getOffset() && end <= block.getOffset())
				|| (offset <= node.getOffset() && end >= node.getEndOffset());
	}

	/** Read-only context provider for locally declared parent species, composed with platform metadata. */
	public SpeciesWizardContext context() {
		SpeciesWizardContext composed = (kind == SpeciesWizard.Kind.CLASS ? SpeciesWizardContext.classBuiltIns()
				: SpeciesWizardContext.builtIns()).and(selection -> {
			List<SpeciesWizardContext.InheritedAttribute> result = new ArrayList<>();
			collectParentAttributes(selection.parent(defaultParent()), result, new java.util.HashSet<>());
			String mirror = SpeciesWizardContext.facet(selection, "mirrors", "").trim();
			if (!mirror.isEmpty()) result.add(new SpeciesWizardContext.InheritedAttribute(
					new SpeciesDraft.Variable("target", mirror, "", "", "", "", ""), "mirror: " + mirror, false));
			return List.copyOf(result);
		});
		return new SpeciesWizardContext() {
			@Override
			public List<InheritedAttribute> attributes(final SpeciesDraft selection) { return composed.attributes(selection); }

			@Override
			public List<String> parentNames(final List<String> candidates) {
				return filterParentNames(candidates);
			}

			@Override
			public List<String> mirrorNames(final List<String> fallback) { return List.copyOf(visibleSpecies().keySet()); }
		};
	}

	private void collectParentAttributes(final String name,
			final List<SpeciesWizardContext.InheritedAttribute> result, final java.util.Set<String> visited) {
		if (!visited.add(name)) throw new IllegalArgumentException("Cyclic inheritance at " + name);
		S_Species target = visibleSpecies().get(name);
		if (target == null) return;
		SpeciesDraft draft = readHeader(target);
		collectParentAttributes(draft.parent(defaultParent()), result, visited);
		if (kind != SpeciesWizard.Kind.CLASS) result.addAll(SpeciesWizardContext.builtIns().attributes(draft));
		if (target.getBlock() == null) return;
		for (var statement : target.getBlock().getStatements()) {
			if (!(statement instanceof S_Definition definition) || definition instanceof S_Callable) continue;
			boolean constant = definition.getFacets().stream().anyMatch(facet -> "const".equals(key(facet))
					&& facet.getExpr() != null && "true".equals(text(facet.getExpr()).trim()));
			result.add(new SpeciesWizardContext.InheritedAttribute(readAttribute(definition), "parent: " + name,
					!constant));
		}
	}

	static SpeciesDraft.Variable readAttribute(final S_Definition definition) {
		Map<String, String> facets = new LinkedHashMap<>();
		for (Facet facet : definition.getFacets()) {
			String key = attributeFacetKey(facet);
			if (List.of("init", "update", "function", "min", "max").contains(key) && facet.getExpr() != null)
				facets.put(key, text(facet.getExpr()));
		}
		return new SpeciesDraft.Variable(definition.getName(), text(definition.getTkey()).trim(),
				facets.getOrDefault("init", ""), facets.getOrDefault("update", ""), facets.getOrDefault("function", ""),
				facets.getOrDefault("min", ""), facets.getOrDefault("max", ""));
	}

	private static String attributeFacetKey(final Facet facet) {
		return switch (key(facet)) {
			case "<-" -> "init";
			case "->" -> "function";
			default -> key(facet);
		};
	}

	/** Registered and locally visible parents, excluding self and local inheritance descendants. */
	public List<String> parentNames() {
		var names = new java.util.LinkedHashSet<String>();
		names.add(defaultParent());
		if (kind != SpeciesWizard.Kind.CLASS) names.addAll(SpeciesWizardContext.builtInParentNames());
		names.addAll(visibleSpecies().keySet());
		return filterParentNames(List.copyOf(names));
	}

	private List<String> filterParentNames(final List<String> candidates) {
		var visible = visibleSpecies();
		return candidates.stream().distinct()
				.filter(name -> kind != SpeciesWizard.Kind.CLASS || "object".equals(name) || visible.containsKey(name))
				.filter(name -> kind == SpeciesWizard.Kind.CLASS || visible.containsKey(name) || !isLocalClass(name))
				.filter(name -> species == null || !species.getName().equals(name))
				.filter(name -> !visible.containsKey(name) || !wouldCreateCycle(visible.get(name))).toList();
	}

	private Map<String, S_Species> visibleSpecies() {
		Map<String, S_Species> result = new LinkedHashMap<>();
		if (kind == SpeciesWizard.Kind.CLASS) {
			var contents = model.eAllContents();
			while (contents.hasNext()) {
				if (contents.next() instanceof S_Species candidate && candidate != species
						&& "class".equals(candidate.getKey()) && isWorldDeclaration(candidate))
					result.put(candidate.getName(), candidate);
			}
			return result;
		}
		EObject scope = species == null ? model.getBlock() : species.eContainer();
		while (scope != null) {
			for (EObject child : scope.eContents()) {
				if (child instanceof S_Species candidate && candidate != species
						&& "species".equals(candidate.getKey()))
					result.putIfAbsent(candidate.getName(), candidate);
			}
			scope = scope.eContainer();
		}
		return result;
	}

	private boolean isWorldDeclaration(final S_Species declaration) {
		EObject container = declaration.eContainer();
		return container == model.getBlock() || container != null && container.eContainer() instanceof S_Global global
				&& global.eContainer() == model.getBlock();
	}

	private boolean wouldCreateCycle(final S_Species candidate) {
		var visited = new java.util.HashSet<S_Species>();
		var visible = visibleSpecies();
		S_Species current = candidate;
		while (current != null) {
			if (!visited.add(current)) return true;
			String parent = current.getFacets().stream().filter(facet -> "parent".equals(key(facet)))
					.map(facet -> text(facet.getExpr()).trim()).findFirst().orElse(defaultParent());
			if (species != null && species.getName().equals(parent)) return true;
			current = visible.get(parent);
		}
		return false;
	}

	private String defaultParent() { return kind == SpeciesWizard.Kind.CLASS ? "object" : "agent"; }

	static String text(final EObject object) {
		if (object == null) throw new IllegalArgumentException("This header contains a facet without an editable value.");
		INode node = NodeModelUtils.findActualNodeFor(object);
		StringBuilder value = new StringBuilder();
		for (var leaf : node.getLeafNodes()) {
			String token = leaf.getText();
			value.append(!leaf.isHidden() || token.isBlank() ? token : " ");
		}
		return value.toString();
	}

	private SpeciesDraft readHeader(final S_Species target) { return readHeader(target, target.getName()); }

	/** Header facets, skills and attribute declarations of a species-like declaration; bodies are not read. */
	static SpeciesDraft readHeader(final gaml.compiler.gaml.FacetsAndBlock target, final String name) {
		List<String> skills = new ArrayList<>();
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (Facet facet : target.getFacets()) {
			String key = key(facet);
			if ("skills".equals(key)) {
				if (!(facet.getExpr() instanceof Array array))
					throw new IllegalArgumentException("Edit this non-list skills expression directly in the source.");
				if (array.getExprs() != null) {
					for (var expression : array.getExprs().getExprs()) {
						String skill = NodeModelUtils.getTokenText(NodeModelUtils.findActualNodeFor(expression));
						if (!SpeciesDraft.isIdentifier(skill))
							throw new IllegalArgumentException("Edit this complex skills expression directly in the source.");
						skills.add(skill);
					}
				}
			} else {
				facets.add(new SpeciesDraft.Facet(key, text(facet.getExpr())));
			}
		}
		// Attributes are displayed read-only for existing source; their source nodes are never regenerated.
		List<SpeciesDraft.Variable> attributes = new ArrayList<>();
		if (target.getBlock() != null) for (var statement : target.getBlock().getStatements())
			if (statement instanceof S_Definition definition && !(definition instanceof S_Callable))
				attributes.add(readAttribute(definition));
		return new SpeciesDraft(name, skills, facets, attributes, "", List.of());
	}

	static String key(final Facet facet) {
		String key = facet.getKey();
		return key.endsWith(":") ? key.substring(0, key.length() - 1) : key;
	}

	public String validationError(final SpeciesDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		if (kind == SpeciesWizard.Kind.GRID && !SpeciesWizardContext.facet(draft, "mirrors", "").isBlank())
			return "A grid cannot mirror another species.";
		String parentName = draft.parent(defaultParent());
		if (draft.name().equals(parentName)) return "A declaration cannot inherit from itself.";
		if (kind == SpeciesWizard.Kind.CLASS) {
			if (!draft.skills().isEmpty() || draft.facets().stream()
					.anyMatch(facet -> !List.of("parent", "virtual").contains(facet.name())))
				return "Classes support only name, parent and virtual header settings.";
			if (!draft.init().isBlank() || !draft.reflexOrder().isEmpty())
				return "Classes do not support init blocks or reflexes.";
			if (!"object".equals(parentName) && !visibleSpecies().containsKey(parentName)
					&& ("agent".equals(parentName) || SpeciesWizardContext.builtInParent(parentName) != null || isLocalSpecies(parentName)
							|| !(species != null && parentName.equals(original.parent("object")))))
				return "The parent of a class must be another class.";
			if (species != null && !isWorldDeclaration(species))
				return "Classes can only belong to the world.";
		} else if ("object".equals(parentName) || !visibleSpecies().containsKey(parentName) && isLocalClass(parentName)) {
			return "A species or grid cannot inherit from a class.";
		}
		S_Species proposedParent = visibleSpecies().get(parentName);
		if (proposedParent != null && wouldCreateCycle(proposedParent))
			return "This parent would introduce an inheritance cycle.";
		for (var facet : draft.facets()) {
			if (!isExpression(facet.value())) return "Enter a single valid GAML expression for facet " + facet.name() + ".";
		}
		for (var variable : draft.variables()) {
			String[] values = { variable.init(), variable.update(), variable.function(), variable.min(), variable.max() };
			String[] labels = { "init", "update", "function", "min", "max" };
			for (int i = 0; i < values.length; i++) {
				if (!values[i].isBlank() && !isExpression(values[i]))
					return "Enter a single valid GAML expression for " + variable.name() + " " + labels[i] + ".";
			}
		}
		if (species != null && (!draft.variables().equals(original.variables())
				|| !draft.init().equals(original.init()) || !draft.reflexOrder().equals(original.reflexOrder())))
			return "Edit existing body declarations in the source editor.";
		if (species == null && !draft.reflexOrder().isEmpty())
			return "New text species have no existing reflexes to reorder. Add reflex bodies in the source editor.";
		EObject scope = species == null ? model.getBlock() : species.eContainer();
		if (scope != null) {
			for (EObject sibling : scope.eContents()) {
				if (sibling != species && sibling instanceof S_Species s && draft.name().equals(s.getName()))
					return "A species, grid or class named '" + draft.name() + "' already exists in this scope.";
			}
			if (kind == SpeciesWizard.Kind.CLASS) {
				var declarations = model.eAllContents();
				while (declarations.hasNext()) {
					if (declarations.next() instanceof S_Species sibling && sibling != species
							&& isWorldDeclaration(sibling) && draft.name().equals(sibling.getName()))
						return "A species, grid or class named '" + draft.name() + "' already exists in the world.";
				}
			}
		}
		String candidate = render(draft);
		var parsed = parser.parse(new StringReader(candidate));
		for (INode node : parsed.getSyntaxErrors())
			return "GAML syntax error at line " + node.getStartLine() + ": " + node.getSyntaxErrorMessage().getMessage();
		return null;
	}

	private boolean isLocalClass(final String name) {
		return isLocalDeclaration(name, true);
	}

	private boolean isLocalSpecies(final String name) {
		return isLocalDeclaration(name, false);
	}

	private boolean isLocalDeclaration(final String name, final boolean classDeclaration) {
		var contents = model.eAllContents();
		while (contents.hasNext()) {
			if (contents.next() instanceof S_Species candidate && classDeclaration == "class".equals(candidate.getKey())
					&& name.equals(candidate.getName())) return true;
		}
		return false;
	}

	private boolean isExpression(final String value) {
		var parsed = parser.parse(new StringReader("model __species_wizard\nspecies __species_wizard frequency: "
				+ value + " {}"));
		if (parsed.hasSyntaxErrors() || !(parsed.getRootASTElement() instanceof Model root)
				|| root.getBlock().getStatements().size() != 1) return false;
		var statement = root.getBlock().getStatements().getFirst();
		return statement instanceof S_Species target && target.getFacets().size() == 1
				&& target.getBlock() != null && target.getBlock().getStatements().isEmpty();
	}

	/** Caller must validate before apply. Produces a complete snapshot, not an edit against live offsets. */
	public String render(final SpeciesDraft draft) {
		if (species == null) return source + newline() + declaration(draft, newline(), kind) + newline();
		if (draft.equals(original)) return source;
		List<Patch> patches = new ArrayList<>();
		if (!draft.name().equals(original.name())) {
			var nodes = NodeModelUtils.findNodesForFeature(species,
					species.eClass().getEStructuralFeature("name"));
			INode node = nodes.getFirst();
			patches.add(new Patch(node.getOffset(), node.getLength(), draft.name()));
		}
		Map<String, String> before = header(original);
		Map<String, String> after = header(draft);
		var existing = new java.util.HashSet<String>();
		for (Facet facet : species.getFacets()) {
			String key = key(facet);
			existing.add(key);
			if ("skills".equals(key) && original.skills().equals(draft.skills())) continue;
			if (java.util.Objects.equals(before.get(key), after.get(key))) continue;
			if (after.containsKey(key)) {
				replaceTokens(patches, NodeModelUtils.findActualNodeFor(facet.getExpr()), after.get(key));
			} else {
				replaceTokens(patches, NodeModelUtils.findActualNodeFor(facet), "");
			}
		}
		StringBuilder additions = new StringBuilder();
		after.forEach((key, value) -> {
			if (!existing.contains(key)) additions.append(key).append(": ").append(value).append(' ');
		});
		if (!additions.isEmpty()) {
			INode block = NodeModelUtils.findActualNodeFor(species.getBlock());
			patches.add(new Patch(block.getOffset(), 0, additions.toString()));
		}
		patches.sort(Comparator.comparingInt(Patch::offset).reversed());
		StringBuilder result = new StringBuilder(source);
		for (Patch patch : patches) result.replace(patch.offset(), patch.offset() + patch.length(), patch.text());
		return result.toString();
	}

	/** Hidden whitespace/comments are retained even inside a changed expression or removed facet. */
	static void replaceTokens(final List<Patch> patches, final INode node, final String replacement) {
		boolean first = true;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) continue;
			patches.add(new Patch(leaf.getOffset(), leaf.getLength(), first ? replacement : ""));
			first = false;
		}
	}

	static Map<String, String> header(final SpeciesDraft draft) {
		Map<String, String> result = new LinkedHashMap<>();
		if (!draft.skills().isEmpty()) result.put("skills", "[" + String.join(", ", draft.skills()) + "]");
		for (var facet : draft.facets()) result.put(facet.name(), facet.value());
		return result;
	}

	private String newline() { return source.contains("\r\n") ? "\r\n" : "\n"; }

	public static String declaration(final SpeciesDraft draft, final String newline) {
		return declaration(draft, newline, SpeciesWizard.Kind.SPECIES);
	}

	public static String declaration(final SpeciesDraft draft, final String newline, final SpeciesWizard.Kind kind) {
		StringBuilder result = new StringBuilder(switch (kind) {
			case GRID -> "grid " + draft.name() + " ";
			case MODEL -> "global ";
			case CLASS -> "class " + draft.name() + " ";
			default -> "species " + draft.name() + " ";
		});
		header(draft).forEach((key, value) -> result.append(key).append(": ").append(value).append(' '));
		result.append('{').append(newline);
		for (var variable : draft.variables()) {
			result.append('\t').append(variable.type()).append(' ').append(variable.name());
			String[] keys = { "init", "update", "function", "min", "max" };
			String[] values = { variable.init(), variable.update(), variable.function(), variable.min(), variable.max() };
			for (int i = 0; i < keys.length; i++) {
				if (!values[i].isBlank()) result.append(' ').append(keys[i]).append(": ").append(values[i]);
			}
			result.append(';').append(newline);
		}
		if (!draft.init().isBlank())
			result.append("\tinit {").append(newline).append(draft.init()).append(newline).append("\t}").append(newline);
		return result.append('}').toString();
	}
}
