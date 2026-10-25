package gaml.compiler.ui.wizards.action;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.Statement;

/** Snapshot-only extraction of complete sibling statements; the owning editor applies the final text atomically. */
public final class ActionExtractionSource {
	private final String source;
	private final int offset, length;
	private final IParser parser;
	private final ActionSource creation;
	private final String problem;
	private final ActionDraft original;

	public ActionExtractionSource(final String source, final int offset, final int length, final IParser parser) {
		if (offset < 0 || length <= 0 || (long) offset + length > source.length())
			throw new IllegalArgumentException("Select complete statements to include in the action.");
		this.source = source;
		this.offset = offset;
		this.length = length;
		this.parser = parser;
		creation = new ActionSource(source, offset, parser, true);
		var parsed = parser.parse(new StringReader(source));
		problem = selectionProblem(parsed.getRootASTElement());
		String name = "my_action";
		for (int i = 2; creation.siblingActions().contains(name); i++) name = "my_action_" + i;
		original = new ActionDraft(name, ActionDraft.NO_RETURN, false, List.of(), source.substring(offset, offset + length));
	}

	public ActionDraft draft() { return original; }
	public ActionSource creation() { return creation; }

	/** An invalid extraction can still open the form: unchecking Include selection permits normal creation. */
	public String validationError(final ActionDraft draft, final boolean include) {
		String error = creation.validationError(draft);
		if (error != null || !include) return error;
		if (problem != null) return problem;
		if (draft.virtual()) return "An extracted action cannot be virtual. Uncheck Include selection to create a virtual action.";
		return null;
	}

	private String selectionProblem(final EObject root) {
		Block selected = null;
		int size = Integer.MAX_VALUE;
		for (var it = root.eAllContents(); it.hasNext();) {
			if (!(it.next() instanceof Block block) || owner(block.eContainer())) continue;
			INode node = NodeModelUtils.findActualNodeFor(block);
			if (node != null && node.getOffset() < offset && node.getEndOffset() > offset + length
					&& node.getLength() < size) {
				selected = block;
				size = node.getLength();
			}
		}
		if (selected == null) return "Select complete statements inside an action, reflex or another executable block.";
		List<Statement> statements = new ArrayList<>();
		for (Statement statement : selected.getStatements()) {
			INode node = NodeModelUtils.findActualNodeFor(statement);
			if (node.getOffset() < offset + length && node.getEndOffset() > offset) {
				if (node.getOffset() < offset || node.getEndOffset() > offset + length)
					return "The selection cuts through a statement. Select complete statements only.";
				statements.add(statement);
			}
		}
		if (statements.isEmpty()) return "Select at least one complete executable statement.";
		INode rootNode = NodeModelUtils.getNode(root);
		for (var leaf : rootNode.getLeafNodes()) {
			if (leaf.getOffset() >= offset + length || leaf.getEndOffset() <= offset) continue;
			if (leaf.getOffset() < offset || leaf.getEndOffset() > offset + length) {
				if (!leaf.getText().isBlank()) return "The selection cuts through a token or comment.";
			}
			if (!leaf.isHidden() && List.of("return", "break", "continue").contains(leaf.getText()))
				return "Selections containing return, break or continue cannot be moved safely into a separate action.";
			if (!leaf.isHidden() && statements.stream().noneMatch(s -> {
				INode node = NodeModelUtils.findActualNodeFor(s);
				return leaf.getOffset() >= node.getOffset() && leaf.getEndOffset() <= node.getEndOffset();
			})) return "Select only complete sibling statements and their comments.";
		}
		for (EObject current = selected.eContainer(); current != null; current = current.eContainer()) {
			if (owner(current)) break;
			if (current instanceof Statement statement && List.of("aspect", "output", "display", "graphics", "overlay")
					.contains(statement.getKey()))
				return "Drawing/output code cannot be extracted as an agent action.";
		}
		return null;
	}

	private static boolean owner(final EObject object) {
		return object instanceof S_Global || object instanceof S_Species || object instanceof S_Experiment;
	}

	public String render(final ActionDraft draft, final boolean include) {
		String error = validationError(draft, include);
		if (error != null) throw new IllegalArgumentException(error);
		String rendered = creation.render(draft);
		if (!include) return rendered;
		// ActionSource inserts at the owner's closing brace, strictly after the selected executable statements.
		if (!rendered.startsWith(source.substring(0, offset + length)))
			throw new IllegalStateException("The action insertion overlaps the selected statements.");
		String call = call(draft);
		return rendered.substring(0, offset) + call + rendered.substring(offset + length);
	}

	private String call(final ActionDraft draft) {
		String selected = source.substring(offset, offset + length);
		int leading = 0;
		while (leading < selected.length() && Character.isWhitespace(selected.charAt(leading))) leading++;
		int trailing = selected.length();
		while (trailing > leading && Character.isWhitespace(selected.charAt(trailing - 1))) trailing--;
		List<String> arguments = draft.arguments().stream().filter(a -> a.defaultValue().isBlank())
				.map(a -> a.name() + ": " + a.name()).toList();
		return selected.substring(0, leading) + "do " + draft.name()
				+ (arguments.isEmpty() ? "" : " (" + String.join(", ", arguments) + ")") + ";"
				+ selected.substring(trailing);
	}

	/** Contains both the replacement call and new action, so validation also catches caller-scope errors. */
	public ActionBodyContext bodyContext(final ActionDraft draft, final boolean include, final URI uri) {
		String rendered = render(new ActionDraft(draft.name(), draft.returnType(), false, draft.arguments(), draft.body()), include);
		var parsed = parser.parse(new StringReader(rendered));
		if (parsed.hasSyntaxErrors()) throw new IllegalArgumentException("The action and replacement call must contain valid GAML statements.");
		S_Callable created = null;
		int ownerSize = Integer.MAX_VALUE;
		for (var it = parsed.getRootASTElement().eAllContents(); it.hasNext();) {
			if (!(it.next() instanceof S_Callable candidate) || !draft.name().equals(candidate.getName())
					|| !(candidate.eContainer() instanceof Block block) || !owner(block.eContainer())) continue;
			INode enclosing = NodeModelUtils.findActualNodeFor(block.eContainer());
			if (enclosing.getOffset() <= offset && enclosing.getEndOffset() > offset && enclosing.getLength() < ownerSize) {
				created = candidate;
				ownerSize = enclosing.getLength();
			}
		}
		if (created == null) throw new IllegalStateException("Cannot locate the extracted action body.");
		INode node = NodeModelUtils.findActualNodeFor(created.getBlock());
		int bodyStart = node.getOffset() + node.getText().indexOf('{') + 1;
		int bodyEnd = node.getOffset() + node.getText().lastIndexOf('}');
		// Empty actions normalize their boundary whitespace when rendered.
		if (draft.body().isBlank())
			return new ActionBodyContext(uri, rendered.substring(0, bodyStart), rendered.substring(bodyStart));
		String body = rendered.substring(bodyStart, bodyEnd);
		int start = body.indexOf(draft.body());
		if (start < 0) throw new IllegalStateException("The extracted body changed while preparing validation.");
		// ActionSource may add boundary line breaks; retain those outside the exact editable draft body.
		return new ActionBodyContext(uri, rendered.substring(0, bodyStart + start),
				rendered.substring(bodyStart + start + draft.body().length()));
	}
}
