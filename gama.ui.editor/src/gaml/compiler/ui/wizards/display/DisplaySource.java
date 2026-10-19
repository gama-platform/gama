package gaml.compiler.ui.wizards.display;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.FacetsAndBlock;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.S_Display;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.experiment.ExperimentDraft;
import gaml.compiler.ui.wizards.species.SpeciesDraft.Facet;

/**
 * Detached display adapter. Layers retain their own source fragments and identities, so settings edits do not
 * regenerate their contents; moves carry their leading comments. Unsupported declarations remain editable raw GAML.
 * Creation requires a caret inside an experiment and reuses its output section, creating one if absent.
 * Global and species declarations are never display creation targets.
 * Every public parse helper reports syntax failures, without touching a workspace resource.
 */
public final class DisplaySource {
	public record Body(List<LayerDraft> layers, String trailingSource) {
		public Body { layers = List.copyOf(layers); }
	}

	private record Patch(int offset, int length, String text) {}
	private record Wrapped(String text, S_Display display, int bodyStart) {}
	private static final String PREFIX = "model display_wizard\nexperiment e type: gui { output { display d {";
	private static final String SUFFIX = "\n} } }\n";
	private final String source;
	private final IParser parser;
	private final S_Display display;
	private final S_Experiment owner;
	private final Statement selectedOutput;
	private final DisplayDraft original;

	public DisplaySource(final String source, final int offset, final IParser parser, final boolean create) {
		this.source = source;
		this.parser = parser;
		EObject root = parse(source, parser);
		display = create ? null : containing(root, offset, S_Display.class);
		owner = containing(root, offset, S_Experiment.class);
		Statement output = null;
		if (owner != null && owner.getBlock() != null) for (var statement : owner.getBlock().getStatements())
			if (("output".equals(statement.getKey()) || "permanent".equals(statement.getKey())) && statement.getBlock() != null
					&& offset >= node(statement).getOffset() && offset < node(statement).getEndOffset()) output = statement;
		selectedOutput = output;
		if (create) {
			if (!canCreate(root, offset)) throw new IllegalArgumentException(
					"Place the cursor inside an experiment to add a display. Displays belong in its output section; "
							+ "an output section will be created if needed.");
			original = DisplayDraft.empty();
		} else {
			if (display == null) throw new IllegalArgumentException("Place the cursor in a display declaration.");
			var facets = readFacets(display, "type");
			Body body = readBody(source, display);
			original = new DisplayDraft(display.getName(), value(display, "type"), facets, body.layers(), body.trailingSource());
		}
	}

	public DisplayDraft draft() { return original; }
	public boolean isCreation() { return display == null; }

	/** Whether the caret is in an experiment that can receive an output/display declaration. */
	public static boolean canCreate(final EObject root, final int offset) {
		if (root == null || offset < 0) return false;
		S_Experiment experiment = containing(root, offset, S_Experiment.class);
		return experiment != null && experiment.getBlock() != null;
	}

	/** A selection anywhere in a display opens this form, including its raw layers. */
	public boolean isDisplaySelection(final int offset, final int length) {
		if (display == null || length < 0) return false;
		INode node = node(display);
		return offset >= node.getOffset() && (long) offset + length <= node.getEndOffset();
	}

	public String validationError(final DisplayDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		if (owner != null) for (var it = owner.eAllContents(); it.hasNext();)
			if (it.next() instanceof S_Display other && other != display && draft.name().equals(other.getName()))
				return "A display named " + draft.name() + " already exists in this owner.";
		if (display != null) for (var layer : draft.layers()) for (var before : original.layers())
			if (layer.id().equals(before.id()) && layer.kind() != before.kind())
				return "An existing layer's kind cannot be changed. Add a new layer instead.";
		return null;
	}

	public ActionBodyContext bodyContext(final DisplayDraft draft, final URI uri) {
		return new ActionBodyContext(uri, render(draft), "");
	}

	/** Model-local suggestions; expression fields remain editable. */
	public List<String> speciesNames() { return names(false); }
	public List<String> gridNames() { return names(true); }

	/** Source-visible aspects of a named species, including source-visible parents. Arbitrary expressions stay valid. */
	public List<String> aspectNames(final String speciesName) {
		EObject root = parse(source, parser);
		java.util.Map<String, gaml.compiler.gaml.S_Species> species = new java.util.HashMap<>();
		for (var it = root.eAllContents(); it.hasNext();)
			if (it.next() instanceof gaml.compiler.gaml.S_Species candidate) species.put(candidate.getName(), candidate);
		var found = new java.util.TreeSet<String>();
		var visited = new java.util.HashSet<String>();
		String current = speciesName;
		while (visited.add(current) && species.containsKey(current)) {
			var declaration = species.get(current);
			if (declaration.getBlock() != null) for (var statement : declaration.getBlock().getStatements()) {
				if (statement instanceof gaml.compiler.gaml.S_Definition definition
						&& definition.getTkey() != null && "aspect".equals(tokens(node(definition.getTkey()))))
					found.add(definition.getName());
				else if ("aspect".equals(statement.getKey()) && statement.getExpr() != null)
					found.add(expression(statement));
			}
			current = value(declaration, "parent");
		}
		return List.copyOf(found);
	}

	private List<String> names(final boolean grids) {
		List<String> names = new ArrayList<>();
		EObject root = parse(source, parser);
		for (var it = root.eAllContents(); it.hasNext();) {
			EObject next = it.next();
			if (next instanceof gaml.compiler.gaml.S_Species species && (!grids || "grid".equals(species.getKey())))
				names.add(species.getName());
		}
		return names.stream().distinct().sorted().toList();
	}

	public String render(final DisplayDraft draft) {
		if (draft.equals(original) && display != null) return source;
		if (display == null) return create(draft);
		List<Patch> patches = new ArrayList<>();
		if (!draft.name().equals(original.name())) replace(patches, nameNode(display),
				ExperimentDraft.quoteName(draft.name()));
		List<Facet> before = new ArrayList<>(original.facets()), after = new ArrayList<>(draft.facets());
		if (!original.renderer().isEmpty()) before.add(new Facet("type", original.renderer()));
		if (!draft.renderer().isEmpty()) after.add(new Facet("type", draft.renderer()));
		patchFacets(patches, display, before, after);
		if (!draft.layers().equals(original.layers()) || !draft.trailingSource().equals(original.trailingSource())) {
			int start = open(display.getBlock()) + 1, end = close(display.getBlock());
			patches.add(new Patch(start, end - start, renderBody(draft.layers(), draft.trailingSource(), parser)));
		}
		return apply(source, patches, 0);
	}

	private String create(final DisplayDraft draft) {
		Statement output = selectedOutput;
		if (output == null) for (var statement : owner.getBlock().getStatements())
			if ("output".equals(statement.getKey()) && statement.getBlock() != null) { output = statement; break; }
		String declaration = renderDisplay(draft, parser);
		if (output != null) return insert(source, output.getBlock(), declaration);
		return insert(source, owner.getBlock(), "output {\n" + declaration + "\n}");
	}

	private static String insert(final String text, final Block block, final String declaration) {
		int end = close(block);
		int line = text.lastIndexOf('\n', end - 1) + 1;
		String nl = text.contains("\r\n") ? "\r\n" : "\n";
		String indent = text.substring(line, end);
		if (!indent.isBlank()) { indent = ""; line = end; }
		return text.substring(0, line) + (line == end ? nl : "") + indent + "\t"
				+ declaration.replace("\r\n", "\n").replace("\n", nl + indent + "\t") + nl + text.substring(line);
	}

	public static String renderDisplay(final DisplayDraft draft, final IParser parser) {
		return draft.header() + " {" + renderBody(draft.layers(), draft.trailingSource(), parser) + "}";
	}

	/** Parses an entire body without model linking. Pure trailing whitespace/comments are not made into fake layers. */
	public static Body parseBody(final String body, final IParser parser) {
		Wrapped wrapped = wrap(body, parser);
		return readBody(wrapped.text(), wrapped.display(), PREFIX.length() + body.length());
	}

	/** Parses exactly one declaration (including any leading/trailing comments). */
	public static LayerDraft parseLayer(final String source, final String id, final IParser parser) {
		Wrapped wrapped = wrap(source, parser);
		var statements = wrapped.display().getBlock().getStatements();
		if (statements.size() != 1) throw new IllegalArgumentException("Enter exactly one layer declaration.");
		return readLayer(statements.getFirst(), source, id);
	}

	private static Wrapped wrap(final String body, final IParser parser) {
		String text = PREFIX + body + SUFFIX;
		EObject root = parse(text, parser);
		for (var it = root.eAllContents(); it.hasNext();)
			if (it.next() instanceof S_Display found) {
				if (open(found.getBlock()) + 1 != PREFIX.length()
						|| close(found.getBlock()) != PREFIX.length() + body.length() + 1)
					throw new IllegalArgumentException("Layer source must stay inside the display body.");
				return new Wrapped(text, found, PREFIX.length());
			}
		throw new IllegalArgumentException("Cannot parse display body.");
	}

	private static Body readBody(final String text, final S_Display display) {
		return readBody(text, display, close(display.getBlock()));
	}

	private static Body readBody(final String text, final S_Display display, final int bodyEnd) {
		List<LayerDraft> layers = new ArrayList<>();
		int previous = open(display.getBlock()) + 1;
		var statements = display.getBlock().getStatements();
		for (int i = 0; i < statements.size(); i++) {
			Statement statement = statements.get(i);
			int end = node(statement).getEndOffset();
			int limit = i + 1 < statements.size() ? node(statements.get(i + 1)).getOffset() : bodyEnd;
			// Inline comments belong to this row; comments starting on a new line belong to the following row.
			for (var leaf : node(display).getLeafNodes()) {
				if (leaf.getOffset() < end) continue;
				if (leaf.getOffset() >= limit || !leaf.isHidden()) break;
				boolean newline = leaf.getText().contains("\n") || leaf.getText().contains("\r");
				if (!leaf.getText().isBlank()) end = Math.min(limit, leaf.getEndOffset());
				if (newline) break;
			}
			layers.add(readLayer(statement, text.substring(previous, end), UUID.randomUUID().toString()));
			previous = end;
		}
		return new Body(layers, text.substring(previous, bodyEnd));
	}

	private static LayerDraft readLayer(final Statement statement, final String source, final String id) {
		LayerKind kind = switch (statement.getKey()) {
			case "species" -> LayerKind.SPECIES;
			case "agents" -> LayerKind.AGENTS;
			case "grid" -> LayerKind.GRID;
			case "image" -> LayerKind.IMAGE;
			default -> LayerKind.RAW;
		};
		// Do not turn arbitrary graphics/drawing code into a form. Only one literal text draw is unambiguous.
		if ("graphics".equals(statement.getKey()) && statement.getBlock() != null
				&& statement.getBlock().getStatements().size() == 1) {
			Statement draw = statement.getBlock().getStatements().getFirst();
			if ("draw".equals(draw.getKey()) && draw.getBlock() == null && draw.getExpr() instanceof StringLiteral) {
				try {
					List<Facet> outer = readFacets(statement, null);
					if (statement.getExpr() != null) outer.addFirst(new Facet("name", expression(statement)));
					return new LayerDraft(id, LayerKind.TEXT, expression(draw), outer, readFacets(draw, null), source);
				} catch (IllegalArgumentException unsupported) {
					return raw(id, source);
				}
			}
		}
		if (kind == LayerKind.RAW || statement.getBlock() != null) return raw(id, source);
		try {
			String expression = expression(statement);
			var facets = readFacets(statement, kind.expressionFacet());
			if (expression.isEmpty()) expression = value(statement, kind.expressionFacet());
			else if (facet(statement, kind.expressionFacet()) != null) return raw(id, source);
			return new LayerDraft(id, kind, expression, facets, List.of(), source);
		} catch (IllegalArgumentException unsupported) {
			return raw(id, source);
		}
	}

	private static LayerDraft raw(final String id, final String source) {
		return new LayerDraft(id, LayerKind.RAW, "", List.of(), List.of(), source);
	}

	public static String renderBody(final List<LayerDraft> layers, final String trailingSource, final IParser parser) {
		StringBuilder result = new StringBuilder();
		String nl = trailingSource.contains("\r\n") || layers.stream().anyMatch(l -> l.source().contains("\r\n"))
				? "\r\n" : "\n";
		boolean comment = false;
		for (var layer : layers) {
			String text = renderLayer(layer, parser);
			// A new row has no source whitespace. Always separate it from a preceding // comment.
			if (layer.source().isEmpty()) result.append(nl).append('\t');
			else if (comment && !leadingNewline(text)) result.append(nl);
			result.append(text);
			comment = lineCommentAtEnd(text, parser);
		}
		if (comment && !trailingSource.isEmpty() && !leadingNewline(trailingSource))
			result.append(nl);
		result.append(trailingSource);
		// A raw editor can leave a // comment at EOF. The display's closing brace must not become part of it.
		if (lineCommentAtEnd(result.toString(), parser)) result.append(nl);
		return result.toString();
	}

	private static boolean leadingNewline(final String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\r' || c == '\n') return true;
			if (!Character.isWhitespace(c)) return false;
		}
		return false;
	}

	private static boolean lineCommentAtEnd(final String text, final IParser parser) {
		if (text.isEmpty() || text.endsWith("\n") || text.endsWith("\r")) return false;
		// Use lexer leaves, not a regex: strings and multiline comments may themselves contain "//".
		var parsed = parser.parse(new StringReader(PREFIX + text + SUFFIX));
		int end = PREFIX.length() + text.length();
		for (var leaf : parsed.getRootNode().getLeafNodes())
			if (leaf.getOffset() < end && leaf.getEndOffset() >= end)
				return leaf.isHidden() && leaf.getText().startsWith("//");
		return false;
	}

	public static String renderLayer(final LayerDraft draft, final IParser parser) {
		if (draft.kind() == LayerKind.RAW) return draft.source();
		if (draft.source().isEmpty()) return generated(draft);
		Wrapped wrapped = wrap(draft.source(), parser);
		var statements = wrapped.display().getBlock().getStatements();
		if (statements.size() != 1) throw new IllegalArgumentException("A layer source must contain one declaration.");
		Statement statement = statements.getFirst();
		LayerDraft before = readLayer(statement, draft.source(), draft.id());
		if (before.kind() != draft.kind()) throw new IllegalArgumentException("An existing layer's kind cannot be changed.");
		if (draft.equals(before)) return draft.source();
		List<Patch> patches = new ArrayList<>();
		if (draft.kind() == LayerKind.TEXT) {
			Statement draw = statement.getBlock().getStatements().getFirst();
			if (!draft.expression().equals(before.expression())) replace(patches, node(draw.getExpr()), draft.expression());
			patchFacets(patches, draw, before.textFacets(), draft.textFacets());
			// The positional graphics label is represented as a name facet in the draft.
			patchLayerHeader(patches, statement, before.facets(), draft.facets(), "name");
		} else {
			if (!draft.expression().equals(before.expression())) patchExpression(patches, statement,
					draft.kind().expressionFacet(), draft.expression());
			patchFacets(patches, statement, before.facets(), draft.facets());
		}
		return apply(draft.source(), patches, wrapped.bodyStart());
	}

	private static void patchLayerHeader(final List<Patch> patches, final Statement statement, final List<Facet> before,
			final List<Facet> after, final String positional) {
		String previous = DisplayDraft.find(before, positional), next = DisplayDraft.find(after, positional);
		if (statement.getExpr() != null) {
			if (!java.util.Objects.equals(previous, next)) patchExpression(patches, statement, positional, next == null ? "" : next);
			patchFacets(patches, statement, before.stream().filter(f -> !f.name().equals(positional)).toList(),
					after.stream().filter(f -> !f.name().equals(positional)).toList());
		} else patchFacets(patches, statement, before, after);
	}

	private static void patchExpression(final List<Patch> patches, final Statement statement, final String omissible,
			final String expression) {
		gaml.compiler.gaml.Facet explicit = facet(statement, omissible);
		if (explicit != null) {
			if (expression.isBlank()) remove(patches, node(explicit));
			else replace(patches, node(explicit.getExpr()), expression);
		} else if (statement.getExpr() != null) {
			if (expression.isBlank()) remove(patches, node(statement.getExpr()));
			else replace(patches, node(statement.getExpr()), expression);
		} else if (!expression.isBlank()) {
			INode key = NodeModelUtils.findNodesForFeature(statement, GamlPackage.Literals.FACETS_AND_BLOCK__KEY).getFirst();
			patches.add(new Patch(key.getEndOffset(), 0, " " + expression));
		}
	}

	private static String generated(final LayerDraft draft) {
		StringBuilder text = new StringBuilder(draft.kind().keyword());
		if (draft.kind() != LayerKind.TEXT && !draft.expression().isBlank()) text.append(' ').append(draft.expression());
		DisplayDraft.appendFacets(text, draft.facets());
		if (draft.kind() == LayerKind.TEXT) {
			text.append(" { draw ").append(draft.expression());
			DisplayDraft.appendFacets(text, draft.textFacets());
			text.append("; }");
		} else text.append(';');
		return text.toString();
	}

	private static List<Facet> readFacets(final FacetsAndBlock declaration, final String excluded) {
		List<Facet> result = new ArrayList<>();
		for (var facet : declaration.getFacets()) if (!key(facet).equals(excluded)) {
			if (facet.getBlock() != null || facet.getExpr() == null)
				throw new IllegalArgumentException("This declaration contains a non-expression facet: " + key(facet));
			result.add(new Facet(key(facet), tokens(node(facet.getExpr()))));
		}
		return result;
	}

	private static String expression(final Statement statement) {
		return statement.getExpr() == null ? "" : tokens(node(statement.getExpr()));
	}

	private static String value(final FacetsAndBlock statement, final String name) {
		var facet = facet(statement, name);
		return facet == null ? "" : tokens(node(facet.getExpr()));
	}

	private static gaml.compiler.gaml.Facet facet(final FacetsAndBlock statement, final String name) {
		for (var facet : statement.getFacets()) if (key(facet).equals(name)) return facet;
		return null;
	}

	private static String key(final gaml.compiler.gaml.Facet facet) { return facet.getKey().replace(":", "").trim(); }

	private static INode nameNode(final S_Display display) {
		return NodeModelUtils.findNodesForFeature(display, GamlPackage.Literals.GAML_DEFINITION__NAME).getFirst();
	}

	private static void patchFacets(final List<Patch> patches, final FacetsAndBlock statement,
			final List<Facet> before, final List<Facet> after) {
		StringBuilder added = new StringBuilder();
		for (var facet : after) {
			String previous = DisplayDraft.find(before, facet.name());
			if (previous == null) added.append(' ').append(facet.name()).append(": ").append(facet.value());
			else if (!previous.equals(facet.value())) replace(patches, node(facet(statement, facet.name()).getExpr()), facet.value());
		}
		for (var facet : before) if (DisplayDraft.find(after, facet.name()) == null) remove(patches, node(facet(statement, facet.name())));
		if (!added.isEmpty()) patches.add(new Patch(headerEnd(statement), 0, added.toString()));
	}

	private static int headerEnd(final FacetsAndBlock statement) {
		int end = statement.getBlock() == null ? node(statement).getEndOffset() : open(statement.getBlock());
		int last = node(statement).getOffset();
		for (var leaf : node(statement).getLeafNodes()) {
			if (leaf.getOffset() >= end) break;
			if (!leaf.isHidden() && !";".equals(leaf.getText())) last = leaf.getEndOffset();
		}
		return last;
	}

	/** Token replacement leaves every comment intact, including comments within a changed expression. */
	private static void replace(final List<Patch> patches, final INode node, final String replacement) {
		boolean first = true;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden() && !leaf.getText().isBlank()) continue;
			if (leaf.getOffset() < node.getOffset() || leaf.getEndOffset() > node.getEndOffset()) continue;
			patches.add(new Patch(leaf.getOffset(), leaf.getLength(), first ? replacement : ""));
			first = false;
		}
	}

	private static void remove(final List<Patch> patches, final INode node) { replace(patches, node, ""); }

	private static String apply(final String text, final List<Patch> patches, final int base) {
		java.util.Collections.reverse(patches);
		patches.sort(Comparator.comparingInt(Patch::offset).reversed());
		StringBuilder result = new StringBuilder(text);
		for (var patch : patches) result.replace(patch.offset() - base, patch.offset() - base + patch.length(), patch.text());
		return result.toString();
	}

	private static String tokens(final INode node) {
		StringBuilder text = new StringBuilder();
		for (var leaf : node.getLeafNodes()) text.append(leaf.isHidden() && !leaf.getText().isBlank() ? " " : leaf.getText());
		return text.toString().trim();
	}

	private static int open(final Block block) {
		for (var leaf : node(block).getLeafNodes()) if (!leaf.isHidden() && "{".equals(leaf.getText())) return leaf.getOffset();
		throw new IllegalArgumentException("Missing opening brace.");
	}

	private static int close(final Block block) {
		int end = -1;
		for (var leaf : node(block).getLeafNodes()) if (!leaf.isHidden() && "}".equals(leaf.getText())) end = leaf.getOffset();
		if (end < 0) throw new IllegalArgumentException("Missing closing brace.");
		return end;
	}

	private static INode node(final EObject object) { return NodeModelUtils.findActualNodeFor(object); }

	private static EObject parse(final String source, final IParser parser) {
		var parsed = parser.parse(new StringReader(source));
		if (parsed.hasSyntaxErrors()) {
			var issue = parsed.getSyntaxErrors().iterator().next();
			throw new IllegalArgumentException("GAML syntax error on line " + issue.getStartLine() + ": "
					+ issue.getSyntaxErrorMessage().getMessage());
		}
		return parsed.getRootASTElement();
	}

	private static <T extends EObject> T containing(final EObject root, final int offset, final Class<T> type) {
		T found = null;
		int size = Integer.MAX_VALUE;
		for (var it = root.eAllContents(); it.hasNext();) {
			EObject next = it.next();
			INode node = node(next);
			if (type.isInstance(next) && node != null && offset >= node.getOffset() && offset < node.getEndOffset()
					&& node.getLength() < size) { found = type.cast(next); size = node.getLength(); }
		}
		return found;
	}
}
