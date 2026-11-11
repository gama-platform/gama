/*******************************************************************************************************
 *
 * GamlCompletionContext.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;

import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.Block;

/**
 * Cursor-based context shared by semantic completion and templates, independent of parser follow alternatives.
 */
record GamlCompletionContext(EObject model, Expression receiver, String receiverName, boolean memberAccess,
		String prefix, int replacementOffset, int replacementLength) {

	static GamlCompletionContext at(final ContentAssistContext context) {
		final var root = context.getRootNode();
		final String text = context.getViewer() == null ? root.getText() : context.getViewer().getDocument().get();
		final int offset = context.getOffset();
		int start = offset;
		while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) { start--; }
		int end = offset;
		while (end < text.length() && Character.isJavaIdentifierPart(text.charAt(end))) { end++; }
		ILeafNode previous = null;
		ILeafNode receiverNode = null;
		for (final ILeafNode leaf : root.getLeafNodes()) {
			if (leaf.getEndOffset() > start) break;
			if (leaf.isHidden()) continue;
			receiverNode = previous;
			previous = leaf;
		}
		final boolean member = previous != null && ".".equals(previous.getText());
		final ILeafNode position = NodeModelUtils.findLeafNodeAtOffset(root, Math.max(0, offset - 1));
		EObject model = position == null ? context.getCurrentModel()
				: NodeModelUtils.findActualSemanticObjectFor(position);
		Expression receiver = null;
		String receiverName = null;
		if (member && receiverNode != null) {
			receiverName = receiverNode.getText();
			model = NodeModelUtils.findActualSemanticObjectFor(receiverNode);
			for (EObject current = model; current != null; current = current.eContainer()) {
				final var node = NodeModelUtils.findActualNodeFor(current);
				if (current instanceof Expression expression && node != null
						&& node.getEndOffset() <= previous.getOffset()) {
					receiver = expression;
				}
			}
		}
		model = lexicalContextAt(context, model, offset);
		return new GamlCompletionContext(model, receiver, receiverName, member, text.substring(start, offset),
				start, Math.max(end - start, context.getReplaceRegion().getOffset()
						+ context.getReplaceRegion().getLength() - start));
	}

	private static EObject lexicalContextAt(final ContentAssistContext context, final EObject model, final int offset) {
		if (context.getRootModel() == null) return model;
		Block active = null;
		int opening = -1;
		final var contents = context.getRootModel().eAllContents();
		while (contents.hasNext()) {
			if (!(contents.next() instanceof Block block)) continue;
			final var node = NodeModelUtils.findActualNodeFor(block);
			if (node == null) continue;
			int start = -1;
			int end = Integer.MAX_VALUE;
			for (final var leaf : node.getLeafNodes()) {
				if (leaf.isHidden() || leaf.getParent() != node) continue;
				if ("{".equals(leaf.getText())) start = leaf.getOffset();
				if ("}".equals(leaf.getText())) end = leaf.getOffset();
			}
			if (start > opening && start < offset && offset <= end) {
				active = block;
				opening = start;
			}
		}
		if (active == null) return model;
		for (EObject current = model; current != null; current = current.eContainer()) {
			if (current == active) return model;
		}
		return active;
	}
}
