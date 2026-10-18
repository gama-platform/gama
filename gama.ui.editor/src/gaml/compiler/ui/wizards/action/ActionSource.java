package gaml.compiler.ui.wizards.action;

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

import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.FacetsAndBlock;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Action;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.Statement;

/**
 * Lossless GAML source adapter for one action. Editing only replaces the tokens that changed (name, return type,
 * individual argument parts, the virtual facet, the exact text between the braces); comments, other facets and
 * surrounding code are never serialized again. Rendering returns a complete snapshot, never an edit on live offsets.
 */
public final class ActionSource {

	private record Patch(int offset, int length, String text) {}

	private static final String MARKER = "/*__action_wizard_body__*/";

	private final String source;
	private final S_Callable action;
	private final FacetsAndBlock owner;
	private final ActionDraft original;

	/**
	 * @param create
	 *            true to add an action to the species/class/grid/global/experiment enclosing {@code offset} (global
	 *            by default); false to edit the action enclosing {@code offset}
	 */
	public ActionSource(final String source, final int offset, final IParser parser, final boolean create) {
		this.source = source;
		var parsed = parser.parse(new StringReader(source));
		if (parsed.hasSyntaxErrors()) throw new IllegalArgumentException("Fix the document's syntax errors first.");
		EObject root = parsed.getRootASTElement();
		if (create) {
			action = null;
			FacetsAndBlock selected = smallest(root, offset, FacetsAndBlock.class, ActionSource::isOwner);
			if (selected == null && root instanceof Model) for (var it = root.eAllContents(); it.hasNext() && selected == null;)
				if (it.next() instanceof S_Global global) selected = global;
			if (selected == null)
				throw new IllegalArgumentException("Place the cursor in a species, class, grid, global or experiment.");
			if (selected.getBlock() == null) throw new IllegalArgumentException("This declaration has no block.");
			owner = selected;
			original = ActionDraft.empty();
		} else {
			action = smallest(root, offset, S_Callable.class, c -> true);
			if (action == null) throw new IllegalArgumentException("Place the cursor in an action declaration.");
			owner = action.eContainer() instanceof Block block && block.eContainer() instanceof FacetsAndBlock f ? f
					: null;
			original = read(action);
		}
	}

	private static boolean isOwner(final FacetsAndBlock candidate) {
		return candidate instanceof S_Species || candidate instanceof S_Global || candidate instanceof S_Experiment;
	}

	private static List<Statement> statements(final Block block) {
		return block == null ? List.of() : block.getStatements();
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

	public ActionDraft draft() { return original; }

	public boolean isCreation() { return action == null; }

	/** True when the selection covers the action header (or the whole declaration), not only its code. */
	public boolean isActionSelection(final int offset, final int length) {
		if (action == null || offset < 0 || length < 0) return false;
		INode node = NodeModelUtils.findActualNodeFor(action);
		int headerEnd = action.getBlock() == null ? node.getEndOffset()
				: NodeModelUtils.findActualNodeFor(action.getBlock()).getOffset();
		long end = (long) offset + length;
		return offset >= node.getOffset() && end <= headerEnd
				|| offset <= node.getOffset() && end >= node.getEndOffset();
	}

	// ----------------------------------------------------------------------------------------------- reading

	private static ActionDraft read(final S_Callable callable) {
		String returnType;
		Facet typeFacet = typeFacet(callable);
		if (callable instanceof S_Action) {
			returnType = typeFacet == null ? ActionDraft.NO_RETURN : text(typeFacet.getExpr()).trim();
		} else {
			if (typeFacet != null)
				throw new IllegalArgumentException("Edit this action's type facet directly in the source.");
			returnType = text(callable.getTkey()).trim();
		}
		Facet virtualFacet = facet(callable, "virtual");
		boolean virtual = callable.getBlock() == null;
		if (virtualFacet != null) {
			String value = text(virtualFacet.getExpr()).trim();
			if (!"true".equals(value) && !"false".equals(value))
				throw new IllegalArgumentException("Edit this non-literal virtual facet directly in the source.");
			virtual |= "true".equals(value);
		}
		List<ActionDraft.Argument> arguments = new ArrayList<>();
		for (ArgumentDefinition argument : callable.getArgs()) arguments.add(new ActionDraft.Argument(
				argument.getName(), text(argument.getType()).trim(),
				argument.getDefault() == null ? "" : text(argument.getDefault()).trim()));
		return new ActionDraft(callable.getName(), returnType, virtual, arguments, body(callable));
	}

	private static String body(final S_Callable callable) {
		if (callable.getBlock() == null) return "";
		INode block = NodeModelUtils.findActualNodeFor(callable.getBlock());
		String text = block.getText();
		int start = text.indexOf('{');
		int end = text.lastIndexOf('}');
		return text.substring(start + 1, end);
	}

	private int bodyStart() {
		INode block = NodeModelUtils.findActualNodeFor(action.getBlock());
		return block.getTotalOffset() + block.getText().indexOf('{') + 1;
	}

	private int bodyEnd() {
		INode block = NodeModelUtils.findActualNodeFor(action.getBlock());
		return block.getTotalOffset() + block.getText().lastIndexOf('}');
	}

	private static Facet typeFacet(final S_Callable callable) {
		Facet result = facet(callable, "type");
		return result == null ? facet(callable, "returns") : result;
	}

	private static Facet facet(final FacetsAndBlock target, final String key) {
		for (Facet facet : target.getFacets()) {
			String k = facet.getKey();
			if (k != null && (k.endsWith(":") ? k.substring(0, k.length() - 1) : k).equals(key)) return facet;
		}
		return null;
	}

	private static String text(final EObject object) {
		if (object == null) throw new IllegalArgumentException("This declaration contains an incomplete element.");
		INode node = NodeModelUtils.findActualNodeFor(object);
		StringBuilder value = new StringBuilder();
		for (var leaf : node.getLeafNodes()) value.append(!leaf.isHidden() || leaf.getText().isBlank() ? leaf.getText() : " ");
		return value.toString();
	}

	// ----------------------------------------------------------------------------------------------- validation

	/** Names of the actions declared directly in the owner, excluding the edited one. */
	public Set<String> siblingActions() {
		Set<String> result = new TreeSet<>();
		if (owner != null) for (var statement : statements(owner.getBlock()))
			if (statement instanceof S_Callable callable && callable != action) result.add(callable.getName());
		return result;
	}

	/** Species, grid and class names declared in this document (usable as types). */
	public List<String> localTypeNames() {
		Set<String> result = new TreeSet<>();
		EObject root = action != null ? org.eclipse.emf.ecore.util.EcoreUtil.getRootContainer(action) : org.eclipse.emf.ecore.util.EcoreUtil.getRootContainer(owner);
		for (var it = root.eAllContents(); it.hasNext();)
			if (it.next() instanceof S_Species species && species.getName() != null) result.add(species.getName());
		return List.copyOf(result);
	}

	/** Structural checks against the document (compilation is separate). */
	public String validationError(final ActionDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		if (siblingActions().contains(draft.name())) return "An action named " + draft.name() + " already exists here.";
		return null;
	}

	/** Contextual source of the rendered document around the draft's code (for {@link ActionVerifier}). */
	public ActionBodyContext bodyContext(final ActionDraft draft, final URI uri) {
		String rendered = render(new ActionDraft(draft.name(), draft.returnType(), false, draft.arguments(), MARKER));
		int index = rendered.indexOf(MARKER);
		if (index < 0 || rendered.indexOf(MARKER, index + 1) >= 0)
			throw new IllegalStateException("The document already contains the action wizard marker.");
		return new ActionBodyContext(uri, rendered.substring(0, index), rendered.substring(index + MARKER.length()));
	}

	// ----------------------------------------------------------------------------------------------- rendering

	/** The complete document with the draft applied; the source itself when nothing changed. */
	public String render(final ActionDraft draft) {
		if (action == null) return create(draft);
		if (draft.equals(original)) return source;
		List<Patch> patches = new ArrayList<>();
		if (!draft.name().equals(original.name())) {
			INode node = NodeModelUtils.findNodesForFeature(action, action.eClass().getEStructuralFeature("name"))
					.getFirst();
			patches.add(new Patch(node.getOffset(), node.getLength(), draft.name()));
		}
		patchReturnType(draft, patches);
		patchArguments(draft, patches);
		patchVirtualAndBody(draft, patches);
		patches.sort(Comparator.comparingInt(Patch::offset).reversed());
		StringBuilder result = new StringBuilder(source);
		for (Patch patch : patches) result.replace(patch.offset(), patch.offset() + patch.length(), patch.text());
		return result.toString();
	}

	private void patchReturnType(final ActionDraft draft, final List<Patch> patches) {
		String before = original.returnType().trim();
		String after = draft.returnType().trim();
		if (before.equals(after)) return;
		if (action instanceof S_Action) {
			Facet typeFacet = typeFacet(action);
			if (typeFacet != null) {
				if (draft.returnsValue()) replaceTokens(patches, NodeModelUtils.findActualNodeFor(typeFacet.getExpr()), after);
				else removeFacet(patches, typeFacet);
				return;
			}
			INode key = NodeModelUtils.findNodesForFeature(action, action.eClass().getEStructuralFeature("key"))
					.getFirst();
			patches.add(new Patch(key.getOffset(), key.getLength(), after));
		} else {
			replaceTokens(patches, NodeModelUtils.findActualNodeFor(action.getTkey()), after);
		}
	}

	private void patchArguments(final ActionDraft draft, final List<Patch> patches) {
		List<ActionDraft.Argument> before = original.arguments();
		List<ActionDraft.Argument> after = draft.arguments();
		if (before.equals(after)) return;
		boolean inPlace = before.size() == after.size();
		// A name moved to another position is a reorder: per-position patches would misattach comments.
		for (int i = 0; inPlace && i < after.size(); i++) {
			String name = after.get(i).name();
			if (!name.equals(before.get(i).name()))
				for (var b : before) if (b.name().equals(name)) inPlace = false;
		}
		if (inPlace) {
			for (int i = 0; i < after.size(); i++) {
				ArgumentDefinition definition = action.getArgs().get(i);
				var b = before.get(i);
				var a = after.get(i);
				if (!b.type().equals(a.type().trim()))
					replaceTokens(patches, NodeModelUtils.findActualNodeFor(definition.getType()), a.type().trim());
				if (!b.name().equals(a.name())) {
					INode node = NodeModelUtils
							.findNodesForFeature(definition, definition.eClass().getEStructuralFeature("name"))
							.getFirst();
					patches.add(new Patch(node.getOffset(), node.getLength(), a.name()));
				}
				String value = a.defaultValue().trim();
				if (b.defaultValue().equals(value)) continue;
				if (definition.getDefault() == null) {
					INode node = NodeModelUtils.findActualNodeFor(definition);
					patches.add(new Patch(node.getEndOffset(), 0, " <- " + value));
				} else if (value.isEmpty()) {
					// removes '<-' and the expression, keeping comments
					INode name = NodeModelUtils
							.findNodesForFeature(definition, definition.eClass().getEStructuralFeature("name"))
							.getFirst();
					for (ILeafNode leaf : NodeModelUtils.findActualNodeFor(definition).getLeafNodes())
						if (leaf.getOffset() >= name.getEndOffset() && !leaf.isHidden())
							patches.add(new Patch(leaf.getOffset(), leaf.getLength(), ""));
				} else {
					replaceTokens(patches, NodeModelUtils.findActualNodeFor(definition.getDefault()), value);
				}
			}
			return;
		}
		// Arguments were added, removed or reordered: the text between the parentheses is regenerated.
		int[] parentheses = parentheses();
		patches.add(new Patch(parentheses[0], parentheses[1] - parentheses[0], draft.argumentsText()));
	}

	/** Offsets just after '(' and at ')'. */
	private int[] parentheses() {
		INode name = NodeModelUtils.findNodesForFeature(action, action.eClass().getEStructuralFeature("name"))
				.getFirst();
		int open = -1;
		int depth = 0;
		for (ILeafNode leaf : NodeModelUtils.findActualNodeFor(action).getLeafNodes()) {
			if (leaf.isHidden() || leaf.getOffset() < name.getEndOffset()) continue;
			String text = leaf.getText();
			if ("(".equals(text)) {
				if (open < 0) open = leaf.getEndOffset();
				depth++;
			} else if (")".equals(text) && --depth == 0) return new int[] { open, leaf.getOffset() };
		}
		throw new IllegalStateException("Cannot locate the argument list of " + action.getName() + ".");
	}

	/** Whether rendering keeps a block when the action is virtual (an existing action written with a block). */
	public boolean keepsBlockWhenVirtual() {
		return action != null && action.getBlock() != null;
	}

		private void patchVirtualAndBody(final ActionDraft draft, final List<Patch> patches) {
		Facet virtualFacet = facet(action, "virtual");
		boolean hasBlock = action.getBlock() != null;
		if (draft.virtual() != original.virtual()) {
			if (draft.virtual()) {
				if (virtualFacet != null) replaceTokens(patches, NodeModelUtils.findActualNodeFor(virtualFacet.getExpr()), "true");
				else patches.add(new Patch(parentheses()[1] + 1, 0, " virtual: true"));
			} else {
				if (virtualFacet != null) removeFacet(patches, virtualFacet);
				if (!hasBlock) {
					ILeafNode semicolon = null;
					for (ILeafNode leaf : NodeModelUtils.findActualNodeFor(action).getLeafNodes())
						if (!leaf.isHidden()) semicolon = leaf;
					patches.add(new Patch(semicolon.getOffset(), semicolon.getLength(), " " + block(draft.body(), indent())));
					return;
				}
			}
		}
		if (hasBlock && !draft.body().equals(original.body()))
			patches.add(new Patch(bodyStart(), bodyEnd() - bodyStart(), draft.body()));
	}

	private String create(final ActionDraft draft) {
		INode block = NodeModelUtils.findActualNodeFor(owner.getBlock());
		int close = block.getTotalOffset() + block.getText().lastIndexOf('}');
		String indent = lineIndent(block.getTotalOffset() + block.getText().indexOf('{')) + "\t";
		String declaration = draft.header() + (draft.virtual() ? " virtual: true;" : " " + block(draft.body(), indent));
		int lineStart = source.lastIndexOf('\n', close - 1) + 1;
		boolean closeAlone = source.substring(lineStart, close).isBlank();
		String nl = newline();
		if (closeAlone)
			return source.substring(0, lineStart) + indent + declaration + nl + source.substring(lineStart);
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

	private String indent() {
		return lineIndent(NodeModelUtils.findActualNodeFor(action).getOffset());
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
		INode node = NodeModelUtils.findActualNodeFor(facet);
		ILeafNode previous = null;
		for (ILeafNode leaf : NodeModelUtils.findActualNodeFor(action).getLeafNodes()) {
			if (leaf.getOffset() >= node.getOffset()) break;
			previous = leaf;
		}
		if (previous != null && previous.isHidden() && previous.getText().isBlank()
				&& previous.getEndOffset() == node.getOffset())
			patches.add(new Patch(previous.getOffset(), previous.getLength(), ""));
		for (ILeafNode leaf : node.getLeafNodes())
			if (leaf.getOffset() >= node.getOffset() && (!leaf.isHidden() || leaf.getText().isBlank()))
				patches.add(new Patch(leaf.getOffset(), leaf.getLength(), ""));
	}

	/** Hidden whitespace/comments are retained even inside a replaced expression. */
	private static void replaceTokens(final List<Patch> patches, final INode node, final String replacement) {
		boolean first = true;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) continue;
			patches.add(new Patch(leaf.getOffset(), leaf.getLength(), first ? replacement : ""));
			first = false;
		}
	}
}
