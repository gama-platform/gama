/*******************************************************************************************************
 *
 * GamlHoverProvider.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.hover;

import java.net.URL;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.IURIEditorOpener;
import org.eclipse.xtext.ui.editor.hover.DispatchingEObjectTextHover;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.hover.html.IXtextBrowserInformationControl;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControl;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;
import org.eclipse.xtext.ui.editor.hover.html.XtextElementLinks;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.ui.shared.utils.WebHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.gaml.ActionRef;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.ui.hover.GamlHoverDocumentationProvider.Result;

/**
 * The Class GamlHoverProvider.
 */
public class GamlHoverProvider extends DefaultEObjectHoverProvider {

	/**
	 * The Class GamlDispatchingEObjectTextHover.
	 */
	public static class GamlDispatchingEObjectTextHover extends DispatchingEObjectTextHover {

		/** The e object at offset helper. */
		@Inject private EObjectAtOffsetHelper eObjectAtOffsetHelper;

		/** The location in file provider. */
		@Inject private ILocationInFileProvider locationInFileProvider;

		/** The correct. */
		EObject correct = null;

		@Override
		protected Pair<EObject, IRegion> getXtextElementAt(final XtextResource resource, final int offset) {
			if (resource == null || resource.getParseResult() == null) return null;
			final ILeafNode leaf = NodeModelUtils.findLeafNodeAtOffset(resource.getParseResult().getRootNode(), offset);
			if (leaf == null || leaf.isHidden()) return null;

			int tokenOffset = leaf.getOffset();
			int tokenLength = leaf.getLength();
			String tokenText = leaf.getText();

			// Handle facet key ending in ':' where ':' is a separate leaf node in the grammar
			if (":".equals(tokenText) && tokenOffset > 0) {
				INode prev = leaf.getPreviousSibling();
				while (prev instanceof ILeafNode lp && lp.isHidden()) { prev = prev.getPreviousSibling(); }
				if (prev != null && prev.getOffset() + prev.getLength() == tokenOffset) {
					tokenOffset = prev.getOffset();
					tokenLength = prev.getLength() + leaf.getLength();
					tokenText = prev.getText() + ":";
				}
			} else {
				INode next = leaf.getNextSibling();
				while (next instanceof ILeafNode ln && ln.isHidden()) { next = next.getNextSibling(); }
				if (next != null && ":".equals(next.getText()) && leaf.getOffset() + leaf.getLength() == next.getOffset()) {
					tokenLength = leaf.getLength() + next.getLength();
					tokenText = leaf.getText() + ":";
				}
			}
			final IRegion tokenRegion = new Region(tokenOffset, tokenLength);

			EObject o;
			if (correct == null) {
				o = computeElementAt(resource, offset, leaf, tokenText);
				correct = o;
			} else {
				o = correct;
				correct = null;
			}

			ITextRegion sigRegion = null;
			if (o != null && !(o instanceof Statement) && !(o instanceof Facet) && !(o instanceof GamlDefinition)) {
				sigRegion = locationInFileProvider.getSignificantTextRegion(o);
			}

			final IRegion finalRegion;
			if (sigRegion == null || sigRegion.getLength() > tokenRegion.getLength()) {
				finalRegion = tokenRegion;
			} else {
				finalRegion = new Region(sigRegion.getOffset(), sigRegion.getLength());
			}

			return Tuples.create(o != null ? o : leaf.getSemanticElement(), finalRegion);
		}

		private EObject computeElementAt(final XtextResource resource, final int offset, final ILeafNode leaf,
				final String tokenText) {
			EObject o = eObjectAtOffsetHelper.resolveContainedElementAt(resource, offset);
			if (o instanceof ActionRef) {
				final EObject container = o.eContainer();
				if (container instanceof Function) { o = container; }
			}
			if (o instanceof Statement stmt) {
				if (tokenText.endsWith(":")) {
					String fKey = tokenText.substring(0, tokenText.length() - 1);
					for (Facet f : stmt.getFacets()) {
						String k = f.getKey();
						if (k != null) {
							if (k.endsWith(":")) { k = k.substring(0, k.length() - 1); }
							if (k.equals(fKey)) {
								o = f;
								break;
							}
						}
					}
				}
			} else if (o instanceof GamlDefinition || o == null) {
				EObject target = leaf.getGrammarElement() instanceof Keyword ? leaf.getGrammarElement()
						: leaf.getSemanticElement();
				if (target != null) { o = target; }
			}
			return o;
		}

		@Override
		public Object getHoverInfo(final EObject first, final ITextViewer textViewer, final IRegion hoverRegion) {
			return super.getHoverInfo(first, textViewer, hoverRegion);
		}

	}

	/**
	 * The Class GamlHoverControlCreator.
	 */
	public class GamlHoverControlCreator extends HoverControlCreator {

		/**
		 * @param informationPresenterControlCreator
		 */
		public GamlHoverControlCreator(final IInformationControlCreator informationPresenterControlCreator) {
			super(informationPresenterControlCreator);
		}

		/**
		 * The Class GamlInformationControl.
		 */
		public class GamlInformationControl extends XtextBrowserInformationControl {

			@Override
			public void setSize(final int width, final int height) {
				super.setSize(width, height + 30);
				final org.eclipse.swt.graphics.Point p = WorkbenchHelper.getDisplay().getCursorLocation();
				p.x -= 5;
				p.y += 15;
				setLocation(p);
			}

			/**
			 * @param parent
			 * @param symbolicFontName
			 * @param statusFieldText
			 */
			public GamlInformationControl(final Shell parent, final String symbolicFontName,
					final String statusFieldText) {
				super(parent, symbolicFontName, statusFieldText);
			}

			/*
			 * @see org.eclipse.jface.text.IInformationControlExtension5# getInformationPresenterControlCreator()
			 */
			@Override
			public IInformationControlCreator getInformationPresenterControlCreator() {
				return GamlHoverProvider.this.getInformationPresenterControlCreator();
			}
		}

		@Override
		public IInformationControl doCreateInformationControl(final Shell parent) {

			final String tooltipAffordanceString = EditorsUI.getTooltipAffordanceString();
			if (BrowserInformationControl.isAvailable(parent)) {
				final String font = "org.eclipse.jdt.ui.javadocfont";
				final IXtextBrowserInformationControl iControl =
						new GamlInformationControl(parent, font, tooltipAffordanceString);
				addLinkListener(iControl);
				return iControl;
			}
			return new DefaultInformationControl(parent, tooltipAffordanceString);
		}
	}

	/** The creator. */
	private IInformationControlCreator creator;

	/** The decorated provider. */
	@Inject private IEObjectDocumentationProvider decoratedProvider;

	/** The provider. */
	@Inject private GamlHoverDocumentationProvider provider;

	/** The guesser for decontextualized documentation when models do not compile. */
	@Inject private GamlGlobalDocGuesser guesser = new GamlGlobalDocGuesser();

	/** The current hover context (region and document). */
	private final ThreadLocal<Pair<IRegion, IDocument>> currentHoverContext = new ThreadLocal<>();

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (creator == null) { creator = new GamlHoverControlCreator(getInformationPresenterControlCreator()); }
		return creator;
	}

	@Override
	public IInformationControlCreatorProvider getHoverInfo(final EObject first, final ITextViewer textViewer,
			final IRegion hoverRegion) {
		return new IInformationControlCreatorProvider() {

			@Override
			public IInformationControlCreator getHoverControlCreator() {
				return GamlHoverProvider.this.getHoverControlCreator();
			}

			@Override
			public Object getInfo() {
				try {
					currentHoverContext
							.set(Tuples.create(hoverRegion, textViewer != null ? textViewer.getDocument() : null));
					return GamlHoverProvider.this.getHoverInfo(first, hoverRegion, null);
				} finally {
					currentHoverContext.remove();
				}
			}
		};
	}

	@Override
	public XtextBrowserInformationControlInput getHoverInfo(final EObject object, final IRegion region,
			final XtextBrowserInformationControlInput previous) {
		return super.getHoverInfo(object, region, previous);
	}

	/**
	 * @since 2.3
	 */
	@Override
	protected void addLinkListener(final IXtextBrowserInformationControl control) {
		control.addLocationListener(getElementLinks().createLocationListener(new XtextElementLinks.ILinkHandler() {

			/** The uri editor opener. */
			@Inject private IURIEditorOpener uriEditorOpener;

			@Override
			public void handleXtextdocViewLink(final URI linkTarget) {
				// TODO: enable when XtextDoc view available
				// control.notifyDelayedInputChange(null);
				// control.setVisible(false);
				// control.dispose(); //FIXME: should have protocol to hide, rather than dispose
				// try {
				// JavadocView view= (JavadocView) JavaPlugin.getActivePage().showView(JavaUI.ID_JAVADOC_VIEW);
				// view.setInput(linkTarget);
				// } catch (PartInitException e) {
				// JavaPlugin.log(e);
				// }
			}

			@Override
			public void handleInlineXtextdocLink(final URI linkTarget) {
				XtextBrowserInformationControlInput hoverInfo = getHoverInfo(getTarget(linkTarget), null,
						(XtextBrowserInformationControlInput) control.getInput());
				if (control.hasDelayedInputChangeListener()) {
					control.notifyDelayedInputChange(hoverInfo);
				} else {
					control.setInput(hoverInfo);
				}
			}

			@Override
			public void handleDeclarationLink(final URI linkTarget) {
				control.notifyDelayedInputChange(null);
				control.dispose(); // FIXME: should have protocol to hide, rather than dispose
				if (uriEditorOpener != null) { uriEditorOpener.open(linkTarget, true); }
			}

			@Override
			public boolean handleExternalLink(final URL url, final Display display) {
				control.notifyDelayedInputChange(null);
				control.dispose(); // FIXME: should have protocol to hide, rather than dispose

				// open external links in real browser:
				WebHelper.openPage(url.toString());
				return true;
			}

			@Override
			public void handleTextSet() {}

			EObject getTarget(final URI uri) {
				ResourceSet rs = ((XtextBrowserInformationControlInput) control.getInput()).getElement().eResource()
						.getResourceSet();
				return rs.getEObject(uri, true);
			}
		}));
	}

	@Override
	protected String getHoverInfoAsHtml(final EObject o) {
		Pair<IRegion, IDocument> ctx = currentHoverContext.get();
		IRegion region = ctx != null ? ctx.getFirst() : null;
		IDocument document = ctx != null ? ctx.getSecond() : null;
		return getHoverInfoAsHtml(o, region, document);
	}

	/**
	 * Gets the hover info as HTML with region and document context for decontextualized guessing.
	 */
	protected String getHoverInfoAsHtml(final EObject o, final IRegion region, final IDocument document) {
		StringBuilder buffer = new StringBuilder();
		IGamlDescription doc = provider.getDoc(o);
		if (doc == null && guesser != null) {
			doc = guesser.guessDocumentation(o, region, document);
		}
		if (doc == null) {
			doc = new Result("Unknown object of type " + (o != null ? o.getClass().getSimpleName() : "null"),
					"File an issue at https://github.com/gama-platform/gama/issues to document it");
		}
		String title = doc.getTitle();
		String documentation = doc.getDocumentation() != null ? doc.getDocumentation().toString() : "";
		String comment = o != null ? decoratedProvider.getDocumentation(o) : null;
		if (Strings.isNullOrEmpty(comment)) {
			if (o instanceof VariableRef vr) {
				comment = super.getDocumentation(vr.getRef());
			} else if (o instanceof ActionRef ar) {
				comment = super.getDocumentation(ar.getRef());
			}
		}
		if (!Strings.isNullOrEmpty(title)) { buffer.append("<b>").append(title).append("</b><hr>"); }
		if (!Strings.isNullOrEmpty(comment)) { buffer.append("<p><i>").append(comment).append("</i></p><hr>"); }
		if (!Strings.isNullOrEmpty(documentation)) { buffer.append("<p>").append(documentation).append("</p>"); }
		return buffer.toString();
	}

}