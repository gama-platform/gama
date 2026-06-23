/*******************************************************************************************************
 *
 * GamaFoldingRegionProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.folding;

import java.util.Collection;
import java.util.regex.Matcher;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.folding.DefaultFoldedPosition;
import org.eclipse.xtext.ui.editor.folding.DefaultFoldingRegionAcceptor;
import org.eclipse.xtext.ui.editor.folding.DefaultFoldingRegionProvider;
import org.eclipse.xtext.ui.editor.folding.FoldedPosition;
import org.eclipse.xtext.ui.editor.folding.IFoldingRegionAcceptor;
import org.eclipse.xtext.ui.editor.folding.IFoldingRegionAcceptorExtension;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.TextRegion;

import gaml.compiler.EGaml;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.impl.S_IfImpl;

/**
 * The class GamaFoldingRegionProvider.
 *
 * @author drogoul
 * @since 3 déc. 2015
 *
 */
public class GamaFoldingRegionProvider extends DefaultFoldingRegionProvider {

	@Override
	protected void computeObjectFolding(final XtextResource xtextResource,
			final IFoldingRegionAcceptor<ITextRegion> foldingRegionAcceptor) {
		super.computeObjectFolding(xtextResource, foldingRegionAcceptor);
	}

	@Override
	protected void computeObjectFolding(final EObject eObject,
			final IFoldingRegionAcceptor<ITextRegion> foldingRegionAcceptor) {
		super.computeObjectFolding(eObject, foldingRegionAcceptor);
	}

	@Override
	protected void computeObjectFolding(final EObject eObject,
			final IFoldingRegionAcceptor<ITextRegion> foldingRegionAcceptor, final boolean initiallyFolded) {
		if (eObject instanceof S_IfImpl sIf && sIf.eIsSet(GamlPackage.SIF__ELSE)) {
			// When an if has an else clause, the default fold would cover the entire S_If node
			// (including the else), hiding the else when the if is folded. Instead, compute a
			// fold region that ends at the last statement of the if-block so that the
			// "} else {" line (and the rest of the else clause) remains visible when folded.
			final Block block = sIf.getBlock();
			if (block != null) {
				final var statements = block.getStatements();
				if (!statements.isEmpty()) {
					final ICompositeNode sIfNode = NodeModelUtils.getNode(sIf);
					final ICompositeNode lastStmtNode = NodeModelUtils.getNode(statements.get(statements.size() - 1));
					if (sIfNode != null && lastStmtNode != null) {
						final int offset = sIfNode.getOffset();
						final int length = lastStmtNode.getOffset() + lastStmtNode.getLength() - offset;
						if (length > 0 && foldingRegionAcceptor instanceof IFoldingRegionAcceptorExtension<?> ext) {
							@SuppressWarnings ("unchecked") final var typedExt =
									(IFoldingRegionAcceptorExtension<ITextRegion>) ext;
							typedExt.accept(offset, length, initiallyFolded, null);
						}
					}
				}
			}
			// Empty block or node not found: skip folding for this S_If
			return;
		}
		super.computeObjectFolding(eObject, foldingRegionAcceptor, initiallyFolded);
	}

	@Override
	protected boolean isHandled(final EObject eObject) {
		// Don't create a separate fold for the if-block when the S_If has an else clause.
		// The custom fold in computeObjectFolding handles the if-block region directly,
		// so a Block-level fold would create a redundant and confusing second toggle on the same line.
		if (eObject instanceof Block block && block.eContainer() instanceof S_IfImpl sIf && block == sIf.getBlock()
				&& sIf.eIsSet(GamlPackage.SIF__ELSE))
			return false;
		return EGaml.getInstance().hasChildren(eObject) && super.isHandled(eObject);
	}

	@Override
	protected boolean shouldProcessContent(final EObject object) {
		return EGaml.getInstance().hasChildren(object);
	}

	/**
	 * The Class TypedFoldedPosition.
	 */
	public class TypedFoldedPosition extends DefaultFoldedPosition {

		/** The type. */
		String type;

		/**
		 * Instantiates a new typed folded position.
		 *
		 * @param offset
		 *            the offset
		 * @param length
		 *            the length
		 * @param contentStart
		 *            the content start
		 * @param contentLength
		 *            the content length
		 * @param type
		 *            the type
		 */
		public TypedFoldedPosition(final int offset, final int length, final int contentStart, final int contentLength,
				final String type) {
			super(offset, length, contentStart, contentLength);
			this.type = type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() { return type; }

	}

	/**
	 * The Class GamaFoldingRegionAcceptor.
	 */
	private class GamaFoldingRegionAcceptor extends DefaultFoldingRegionAcceptor {

		/** The type. */
		String type;

		/**
		 * Instantiates a new gama folding region acceptor.
		 *
		 * @param document
		 *            the document
		 * @param result
		 *            the result
		 */
		public GamaFoldingRegionAcceptor(final IXtextDocument document, final Collection<FoldedPosition> result) {
			super(document, result);
		}

		@Override
		protected FoldedPosition newFoldedPosition(final IRegion region, final ITextRegion significantRegion) {
			FoldedPosition result = null;
			if (region != null) {
				if (type != null && significantRegion != null) {
					result = new TypedFoldedPosition(region.getOffset(), region.getLength(),
							significantRegion.getOffset() - region.getOffset() - 1, significantRegion.getLength(),
							type);
				} else {
					result = super.newFoldedPosition(region, significantRegion);
				}
			}
			return result;
		}

	}

	@Override
	protected IFoldingRegionAcceptor<ITextRegion> createAcceptor(final IXtextDocument xtextDocument,
			final Collection<FoldedPosition> foldedPositions) {
		return new GamaFoldingRegionAcceptor(xtextDocument, foldedPositions);
	}

	@Override
	protected void computeCommentFolding(final IXtextDocument xtextDocument,
			final IFoldingRegionAcceptor<ITextRegion> foldingRegionAcceptor, final ITypedRegion typedRegion,
			final boolean initiallyFolded) throws BadLocationException {
		final int offset = typedRegion.getOffset();
		final int length = typedRegion.getLength();
		final Matcher matcher = getTextPatternInComment().matcher(xtextDocument.get(offset, length));
		((GamaFoldingRegionAcceptor) foldingRegionAcceptor).type = typedRegion.getType();
		if (matcher.find()) {
			final TextRegion significant = new TextRegion(offset + matcher.start(), 0);
			((IFoldingRegionAcceptorExtension<ITextRegion>) foldingRegionAcceptor).accept(offset, length,
					initiallyFolded, significant);
		} else {
			((IFoldingRegionAcceptorExtension<ITextRegion>) foldingRegionAcceptor).accept(offset, length,
					initiallyFolded);
		}
	}

}
