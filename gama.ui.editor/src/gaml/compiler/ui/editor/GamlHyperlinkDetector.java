/*******************************************************************************************************
 *
 * GamlHyperlinkDetector.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hyperlinking.DefaultHyperlinkDetector;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;

import com.google.inject.Inject;

import gama.core.common.util.FileUtils;
import gama.ui.shared.utils.FileOpener;
import gaml.compiler.gaml.HeadlessExperiment;
import gaml.compiler.gaml.Import;
import gaml.compiler.gaml.StringLiteral;

/**
 * Represents an implementation of interface <code>{@link IHyperlinkDetector}</code> to find and convert
 * {@link CrossReference elements}, at a given location, to {@code IHyperlink}.
 *
 * @author Alexis Drogoul
 */
public class GamlHyperlinkDetector extends DefaultHyperlinkDetector {

	/**
	 * The Class ImportHyperlink.
	 */
	class ImportHyperlink implements IHyperlink {

		/** The import uri. */
		private final URI importUri;

		/** The region. */
		private final IRegion region;

		/**
		 * Instantiates a new import hyperlink.
		 *
		 * @param importUri
		 *            the import uri
		 * @param region
		 *            the region
		 */
		ImportHyperlink(final URI importUri, final IRegion region) {
			this.importUri = importUri;
			this.region = region;
		}

		@Override
		public void open() {
			FileOpener.openFile(importUri);
		}

		@Override
		public String getTypeLabel() { return null; }

		@Override
		public IRegion getHyperlinkRegion() { return region; }

		@Override
		public String getHyperlinkText() { return null; }
	}

	/** The Constant NO_HYPERLINKS. */
	private static final IHyperlink[] NO_HYPERLINKS = null;

	/** The e object at offset helper. */
	@Inject private EObjectAtOffsetHelper eObjectAtOffsetHelper;

	@Override
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region,
			final boolean canShowMultipleHyperlinks) {
		final IXtextDocument document = (IXtextDocument) textViewer.getDocument();
		final IHyperlink[] importHyperlinks = importHyperlinks(document, region);
		if (importHyperlinks != NO_HYPERLINKS) return importHyperlinks;
		return document.readOnly(new CancelableUnitOfWork<IHyperlink[], XtextResource>() {

			@Override
			public IHyperlink[] exec(final XtextResource resource, final CancelIndicator c) {
				return getHelper().createHyperlinksByOffset(resource, region.getOffset(), canShowMultipleHyperlinks);
			}
		});
	}

	/**
	 * Gets the uri.
	 *
	 * @param resolved
	 *            the resolved
	 * @return the uri
	 */
	public URI getURI(final StringLiteral resolved) {
		String path = resolved.getOp();
		if (path == null || path.isBlank()) return null;
		return FileUtils.getURI(path, resolved.eResource().getURI());
	}

	/**
	 * Import hyperlinks.
	 *
	 * @param document
	 *            the document
	 * @param region
	 *            the region
	 * @return the i hyperlink[]
	 */
	private IHyperlink[] importHyperlinks(final IXtextDocument document, final IRegion region) {
		return document.readOnly(resource -> {
			final EObject resolved = eObjectAtOffsetHelper.resolveElementAt(resource, region.getOffset());

			if (resolved instanceof StringLiteral) {
				final URI iu1 = getURI((StringLiteral) resolved);
				if (iu1 != null) {
					IRegion hRegion;
					try {
						hRegion = importUriRegion(document, region.getOffset(), ((StringLiteral) resolved).getOp());
					} catch (final BadLocationException e1) {
						return NO_HYPERLINKS;
					}
					if (hRegion == null) return NO_HYPERLINKS;
					final IHyperlink hyperlink1 = new ImportHyperlink(iu1, hRegion);
					return new IHyperlink[] { hyperlink1 };
				}
			}
			String importUri = null;
			if (resolved instanceof Import) {
				importUri = ((Import) resolved).getImportURI();
			} else if (resolved instanceof HeadlessExperiment) {
				importUri = ((HeadlessExperiment) resolved).getImportURI();
			}
			if (importUri == null) return NO_HYPERLINKS;
			final URI iu2 = URI.createURI(importUri, false).resolve(resource.getURI());
			IRegion importUriRegion;
			try {
				importUriRegion = importUriRegion(document, region.getOffset(), importUri);
			} catch (final BadLocationException e2) {
				return NO_HYPERLINKS;
			}
			if (importUriRegion == null) return NO_HYPERLINKS;
			final IHyperlink hyperlink2 = new ImportHyperlink(iu2, importUriRegion);
			return new IHyperlink[] { hyperlink2 };
		});
	}

	/**
	 * Import uri region.
	 *
	 * @param document
	 *            the document
	 * @param offset
	 *            the offset
	 * @param importUri
	 *            the import uri
	 * @return the i region
	 * @throws BadLocationException
	 *             the bad location exception
	 */
	private IRegion importUriRegion(final IXtextDocument document, final int offset, final String importUri)
			throws BadLocationException {
		final int lineNumber = document.getLineOfOffset(offset);
		final int lineLength = document.getLineLength(lineNumber);
		final int lineOffset = document.getLineOffset(lineNumber);
		final String line = document.get(lineOffset, lineLength);
		final int uriIndex = line.indexOf(importUri);
		return new Region(lineOffset + uriIndex, importUri.length());
	}
}
