/*******************************************************************************************************
 *
 * GamlTextRegionAccessBuilder.java, in gaml.compiler, is part of the source code of the GAMA modeling and
 * simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.formatting2;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.TextRegionAccessBuilder;
import org.eclipse.xtext.formatting2.regionaccess.internal.NodeModelBasedRegionAccessBuilder;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.acceptor.ISequenceAcceptor;

/**
 * Handles semantic children whose fragment call hides their parent from Xtext's region builder.
 */
public class GamlTextRegionAccessBuilder extends TextRegionAccessBuilder {

	private NodeModelBasedRegionAccessBuilder nodeModel;

	@Override
	public TextRegionAccessBuilder forNodeModel(final XtextResource resource) {
		nodeModel = new SemanticRegionBuilder().withResource(resource);
		return this;
	}

	@Override
	public ISequenceAcceptor forSequence(final ISerializationContext context, final EObject root) {
		nodeModel = null;
		return super.forSequence(context, root);
	}

	@Override
	public ITextRegionAccess create() {
		return nodeModel == null ? super.create() : nodeModel.create();
	}

	private static class SemanticRegionBuilder extends NodeModelBasedRegionAccessBuilder {

		@Override
		protected boolean isEObjectRoot(final INode node) {
			if (super.isEObjectRoot(node)) return true;
			// Fragments can leave a child's direct node below a rule call without a direct semantic element.
			if (!(node instanceof ICompositeNode) || !node.hasDirectSemanticElement() || getStack().isEmpty())
				return false;
			final EObject semantic = node.getSemanticElement();
			final EObject parent = getStack().getFirst().getSemanticElement();
			return semantic != null && parent != null && semantic.eContainer() == parent;
		}
	}
}
