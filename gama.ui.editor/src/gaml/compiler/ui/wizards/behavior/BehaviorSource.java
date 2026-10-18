package gaml.compiler.ui.wizards.behavior;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.FacetsAndBlock;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.species.SpeciesDraft;

/**
 * Lossless GAML source adapter for one behaviour (reflex, init, abort, state, task, plan, perceive, rule). Editing only replaces the
 * tokens that changed (keyword, name, individual facet values, the exact text between the braces), removes deleted
 * facets and appends new ones at the end of the header; comments, other code and the facets' order are kept. Rendering
 * returns a complete snapshot, never an edit on live offsets.
 */
public final class BehaviorSource {

	private record Patch(int offset, int length, String text) {}

	private static final String MARKER = "/*__behavior_wizard_body__*/";

	private final String source;
	private final Statement behavior;
	private final FacetsAndBlock owner;
	private final BehaviorDraft original;

	/**
	 * @param create
	 *            true to add a behaviour to the species/class/grid/global/experiment enclosing {@code offset} (global
	 *            by default); false to edit the behaviour enclosing {@code offset}
	 */
	public BehaviorSource(final String source, final int offset, final IParser parser, final boolean create) {
		this.source = source;
		var parsed = parser.parse(new StringReader(source));
		if (parsed.hasSyntaxErrors()) throw new IllegalArgumentException("Fix the document's syntax errors first.");
		EObject root = parsed.getRootASTElement();
		if (create) {
			behavior = null;
			FacetsAndBlock selected = smallest(root, offset, FacetsAndBlock.class, BehaviorSource::isOwner);
			if (selected == null && root instanceof Model)
				for (var it = root.eAllContents(); it.hasNext() && selected == null;)
					if (it.next() instanceof S_Global global) selected = global;
			if (selected == null)
				throw new IllegalArgumentException("Place the cursor in a species, class, grid, global or experiment.");
			if (selected.getBlock() == null) throw new IllegalArgumentException("This declaration has no block.");
			owner = selected;
			original = BehaviorDraft.empty(BehaviorKind.REFLEX);
		} else {
			behavior = smallest(root, offset, Statement.class, BehaviorSource::isBehavior);
			if (behavior == null) throw new IllegalArgumentException(
					"Place the cursor in a reflex, init, abort, state, task, plan, perceive or rule declaration.");
			owner = behavior.eContainer() instanceof Block block && block.eContainer() instanceof FacetsAndBlock f ? f
					: null;
			original = read(behavior);
		}
	}

	/** Whether the statement is a supported behaviour (reflex, init, abort, state, task, plan, perceive, rule). */
	public static boolean isBehavior(final Statement statement) {
		if (statement instanceof S_Reflex) return BehaviorKind.of(statement.getKey()) != null;
		if (statement instanceof S_Definition definition)
			return BehaviorKind.of(keyword(statement)) != null;
		return statement.eClass() == GamlPackage.Literals.STATEMENT && BehaviorKind.of(statement.getKey()) != null;
	}

	/** The keyword; {@code state busy;} parses as a declaration typed {@code state}. */
	private static String keyword(final Statement statement) {
		if (statement instanceof S_Definition definition)
			return definition.getTkey() instanceof TypeRef type ? text(type).trim() : null;
		return statement.getKey();
	}

	private INode keyNode() {
		if (behavior instanceof S_Definition definition) return NodeModelUtils.findActualNodeFor(definition.getTkey());
		return NodeModelUtils.findNodesForFeature(behavior, GamlPackage.Literals.FACETS_AND_BLOCK__KEY).getFirst();
	}

	private static boolean isOwner(final FacetsAndBlock candidate) {
		return candidate instanceof S_Species || candidate instanceof S_Global || candidate instanceof S_Experiment;
	}

	private static <T extends EObject> T smallest(final EObject root, final int offset, final Class<T> type,
			final java.util.function.Predicate<T> filter) {
		if (root == null || offset < 0) return null;
		T selected = null;
		int length = Integer.MAX_VALUE;
		var contents = root.eAllContents();
		while (contents.hasNext()) {
			EObject object = contents.next();
			if (type.isInstance(object) && filter.test(type.cast(object))) {
				INode node = NodeModelUtils.findActualNodeFor(object);
				if (node != null && offset >= node.getOffset() && offset < node.getEndOffset()
						&& node.getLength() < length) {
					selected = type.cast(object);
					length = node.getLength();
				}
			}
		}
		return selected;
	}

	public BehaviorDraft draft() { return original; }

	public boolean isCreation() { return behavior == null; }

	/** True when the selection covers the behaviour header (or the whole declaration), not only its code. */
	public boolean isBehaviorSelection(final int offset, final int length) {
		if (behavior == null || offset < 0 || length < 0) return false;
		INode node = NodeModelUtils.findActualNodeFor(behavior);
		int headerEnd = behavior.getBlock() == null ? node.getEndOffset()
				: NodeModelUtils.findActualNodeFor(behavior.getBlock()).getOffset();
		long end = (long) offset + length;
		return offset >= node.getOffset() && end <= headerEnd
				|| offset <= node.getOffset() && end >= node.getEndOffset();
	}

	// ----------------------------------------------------------------------------------------------- reading

	private static BehaviorDraft read(final Statement statement) {
		BehaviorKind kind = BehaviorKind.of(keyword(statement));
		String name = "";
		if (statement instanceof GamlDefinition definition) {
			if (definition.getName() != null) name = definition.getName();
		} else if (statement.getExpr() != null) {
			if (!(statement.getExpr() instanceof VariableRef))
				throw new IllegalArgumentException("Edit this " + kind.keyword() + "'s name directly in the source.");
			name = text(statement.getExpr()).trim();
		}
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (Facet facet : statement.getFacets()) {
			String key = key(facet);
			if (!SpeciesDraft.isIdentifier(key) || facet.getBlock() != null || facet.getExpr() == null)
				throw new IllegalArgumentException("Edit this " + kind.keyword() + "'s " + key + " facet directly in the source.");
			String value = text(facet.getExpr()).trim();
			if ("name".equals(key)) {
				if (!name.isEmpty() || !(facet.getExpr() instanceof VariableRef))
					throw new IllegalArgumentException("Edit this " + kind.keyword() + "'s name directly in the source.");
				name = value;
			} else facets.add(new SpeciesDraft.Facet(key, value));
		}
		return new BehaviorDraft(kind, name, facets, body(statement));
	}

	private static String key(final Facet facet) {
		String key = facet.getKey() == null ? "" : facet.getKey().trim();
		return key.endsWith(":") ? key.substring(0, key.length() - 1).trim() : key;
	}

	private static String body(final Statement statement) {
		if (statement.getBlock() == null) return "";
		String text = NodeModelUtils.findActualNodeFor(statement.getBlock()).getText();
		return text.substring(text.indexOf('{') + 1, text.lastIndexOf('}'));
	}

	private static Facet facet(final FacetsAndBlock target, final String key) {
		for (Facet facet : target.getFacets()) if (key(facet).equals(key)) return facet;
		return null;
	}

	private static String text(final EObject object) {
		INode node = NodeModelUtils.findActualNodeFor(object);
		StringBuilder value = new StringBuilder();
		for (var leaf : node.getLeafNodes())
			value.append(!leaf.isHidden() || leaf.getText().isBlank() ? leaf.getText() : " ");
		return value.toString();
	}

	// ----------------------------------------------------------------------------------------------- validation

	/** Names of the behaviours of the same kind declared directly in the owner, excluding the edited one. */
	public Set<String> siblingNames(final BehaviorKind kind) {
		return siblingNames(kind, false);
	}

	/**
	 * Names of all the behaviours declared directly in the owner, whatever their kind, excluding the edited one. The
	 * compiler keeps behaviours in one map by name: a second behaviour with the same name replaces the first one
	 * (silently when the keywords differ, e.g. {@code init startup} and {@code abort startup}).
	 */
	public Set<String> siblingNames() {
		return siblingNames(null, true);
	}

	private Set<String> siblingNames(final BehaviorKind kind, final boolean allKinds) {
		Set<String> result = new TreeSet<>();
		if (owner != null && owner.getBlock() != null) for (var statement : owner.getBlock().getStatements())
			if (statement != behavior && isBehavior(statement)
					&& (allKinds || BehaviorKind.of(keyword(statement)) == kind)) {
				try {
					String name = read(statement).name();
					if (!name.isEmpty()) result.add(name);
				} catch (IllegalArgumentException ignored) {
					// a name written as an expression: the compiler reports duplicates
				}
			}
		return result;
	}

	/** Structural checks against the document (compilation is separate). */
	public String validationError(final BehaviorDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		if (!draft.name().isEmpty() && siblingNames().contains(draft.name()))
			return "A behavior named " + draft.name() + " already exists here; it would replace or be replaced by this "
					+ draft.kind().keyword() + ".";
		return null;
	}

	/**
	 * Contextual source of the rendered document around the draft's code. For kinds without code, the prefix is the
	 * whole rendered document and the suffix is empty.
	 */
	public ActionBodyContext bodyContext(final BehaviorDraft draft, final URI uri) {
		if (!draft.kind().hasBody()) return new ActionBodyContext(uri, render(draft), "");
		String rendered = render(new BehaviorDraft(draft.kind(), draft.name(), draft.facets(), MARKER));
		int index = rendered.indexOf(MARKER);
		if (index < 0 || rendered.indexOf(MARKER, index + 1) >= 0)
			throw new IllegalStateException("The document already contains the behavior wizard marker.");
		return new ActionBodyContext(uri, rendered.substring(0, index), rendered.substring(index + MARKER.length()));
	}

	// ----------------------------------------------------------------------------------------------- rendering

	/** The complete document with the draft applied; the source itself when nothing changed. */
	public String render(final BehaviorDraft draft) {
		if (behavior == null) return create(draft);
		if (draft.equals(original)) return source;
		List<Patch> patches = new ArrayList<>();
		if (draft.kind() != original.kind()) {
			replaceTokens(patches, keyNode(), draft.kind().keyword());
		}
		patchName(draft, patches);
		patchFacets(draft, patches);
		patchBody(draft, patches);
		// insertions at the same offset keep their order (e.g. a new name before new facets of an anonymous header)
		java.util.Collections.reverse(patches);
		patches.sort(Comparator.comparingInt(Patch::offset).reversed());
		StringBuilder result = new StringBuilder(source);
		for (Patch patch : patches) result.replace(patch.offset(), patch.offset() + patch.length(), patch.text());
		return result.toString();
	}

	private void patchName(final BehaviorDraft draft, final List<Patch> patches) {
		if (draft.name().equals(original.name())) return;
		Facet nameFacet = facet(behavior, "name");
		INode node = null;
		if (nameFacet != null) {
			if (draft.name().isEmpty()) removeFacet(patches, nameFacet);
			else replaceTokens(patches, NodeModelUtils.findActualNodeFor(nameFacet.getExpr()), draft.name());
			return;
		}
		if (behavior instanceof GamlDefinition) {
			var nodes = NodeModelUtils.findNodesForFeature(behavior, GamlPackage.Literals.GAML_DEFINITION__NAME);
			if (!nodes.isEmpty()) node = nodes.getFirst();
		} else if (behavior.getExpr() != null) node = NodeModelUtils.findActualNodeFor(behavior.getExpr());
		if (node == null) {
			patches.add(new Patch(keyNode().getEndOffset(), 0, " " + draft.name()));
		} else if (draft.name().isEmpty()) removeNode(patches, node);
		else replaceTokens(patches, node, draft.name());
	}

	private void patchFacets(final BehaviorDraft draft, final List<Patch> patches) {
		StringBuilder added = new StringBuilder();
		for (var facet : draft.facets()) {
			String before = original.facet(facet.name());
			if (before == null) added.append(' ').append(facet.name()).append(": ").append(facet.value().trim());
			else if (!before.equals(facet.value().trim())) replaceTokens(patches,
					NodeModelUtils.findActualNodeFor(facet(behavior, facet.name()).getExpr()), facet.value().trim());
		}
		for (var facet : original.facets())
			if (draft.facet(facet.name()) == null) removeFacet(patches, facet(behavior, facet.name()));
		if (!added.isEmpty()) patches.add(new Patch(headerEnd(), 0, added.toString()));
	}

	/** End offset of the last header token (before the block or the ';'). */
	private int headerEnd() {
		INode node = NodeModelUtils.findActualNodeFor(behavior);
		int limit = behavior.getBlock() == null ? node.getEndOffset()
				: NodeModelUtils.findActualNodeFor(behavior.getBlock()).getOffset();
		ILeafNode last = null;
		for (ILeafNode leaf : node.getLeafNodes()) {
			if (leaf.getOffset() >= limit) break;
			if (!leaf.isHidden() && !(behavior.getBlock() == null && ";".equals(leaf.getText()) && leaf.getEndOffset() == limit))
				last = leaf;
		}
		return last.getEndOffset();
	}

	private void patchBody(final BehaviorDraft draft, final List<Patch> patches) {
		if (!draft.kind().hasBody() || draft.body().equals(original.body())) return;
		if (behavior.getBlock() != null) {
			INode block = NodeModelUtils.findActualNodeFor(behavior.getBlock());
			int start = block.getTotalOffset() + block.getText().indexOf('{') + 1;
			int end = block.getTotalOffset() + block.getText().lastIndexOf('}');
			patches.add(new Patch(start, end - start, draft.body()));
			return;
		}
		ILeafNode semicolon = null;
		for (ILeafNode leaf : NodeModelUtils.findActualNodeFor(behavior).getLeafNodes())
			if (!leaf.isHidden()) semicolon = leaf;
		patches.add(new Patch(semicolon.getOffset(), semicolon.getLength(),
				" " + block(draft.body(), lineIndent(NodeModelUtils.findActualNodeFor(behavior).getOffset()))));
	}

	private String create(final BehaviorDraft draft) {
		INode block = NodeModelUtils.findActualNodeFor(owner.getBlock());
		int close = block.getTotalOffset() + block.getText().lastIndexOf('}');
		String indent = lineIndent(block.getTotalOffset() + block.getText().indexOf('{')) + "\t";
		String declaration = draft.header() + (draft.kind().hasBody() ? " " + block(draft.body(), indent) : ";");
		int lineStart = source.lastIndexOf('\n', close - 1) + 1;
		boolean closeAlone = source.substring(lineStart, close).isBlank();
		String nl = newline();
		if (closeAlone) return source.substring(0, lineStart) + indent + declaration + nl + source.substring(lineStart);
		return source.substring(0, close) + nl + indent + declaration + nl + lineIndent(close) + source.substring(close);
	}

	/** {@code { body }}: the body is kept verbatim; line breaks are only added around it when absent. */
	private String block(final String body, final String indent) {
		String nl = newline();
		if (body.isBlank()) return "{" + nl + indent + "}";
		int lastLine = body.lastIndexOf('\n');
		boolean closed = lastLine >= 0 && body.substring(lastLine + 1).isBlank();
		return "{" + (body.startsWith("\n") || body.startsWith("\r") ? "" : nl) + body + (closed ? "" : nl + indent)
				+ "}";
	}

	private String lineIndent(final int offset) {
		int start = source.lastIndexOf('\n', offset - 1) + 1;
		int end = start;
		while (end < source.length() && (source.charAt(end) == ' ' || source.charAt(end) == '\t')) end++;
		return source.substring(start, end);
	}

	private String newline() { return source.contains("\r\n") ? "\r\n" : "\n"; }

	/** Removes a facet's tokens and whitespace (including just before it); comments are kept. */
	private void removeFacet(final List<Patch> patches, final Facet facet) {
		removeNode(patches, NodeModelUtils.findActualNodeFor(facet));
	}

	private void removeNode(final List<Patch> patches, final INode node) {
		ILeafNode previous = null;
		for (ILeafNode leaf : NodeModelUtils.findActualNodeFor(behavior).getLeafNodes()) {
			if (leaf.getOffset() >= node.getOffset()) break;
			previous = leaf;
		}
		boolean comments = false;
		for (ILeafNode leaf : node.getLeafNodes()) comments |= leaf.isHidden() && !leaf.getText().isBlank();
		// With a kept comment, the preceding space keeps separating it from the previous token.
		if (!comments && previous != null && previous.isHidden() && previous.getText().isBlank()
				&& previous.getEndOffset() == node.getOffset())
			patches.add(new Patch(previous.getOffset(), previous.getLength(), ""));
		for (ILeafNode leaf : node.getLeafNodes())
			if (leaf.getOffset() >= node.getOffset() && (!leaf.isHidden() || leaf.getText().isBlank()))
				patches.add(new Patch(leaf.getOffset(), leaf.getLength(), ""));
	}

	/** Hidden whitespace/comments are retained even inside a replaced expression. */
	private static void replaceTokens(final List<Patch> patches, final INode node, final String replacement) {
		ILeafNode start = null;
		ILeafNode end = null;
		boolean comments = false;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) {
				if (start != null && !leaf.getText().isBlank()) comments = true;
				continue;
			}
			if (start == null) start = leaf;
			end = leaf;
		}
		if (start == null) return;
		if (!comments || start == end) {
			patches.add(new Patch(start.getOffset(), end.getEndOffset() - start.getOffset(), replacement));
			return;
		}
		boolean first = true;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) continue;
			patches.add(new Patch(leaf.getOffset(), leaf.getLength(), first ? replacement : ""));
			first = false;
		}
	}
}
