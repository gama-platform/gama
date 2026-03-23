/*******************************************************************************************************
 *
 * JsonSourceViewerConfiguration.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import static gama.ui.viewers.json.eclipse.JsonEditorUtil.getPreferences;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.BOOLEAN;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.COMMENT;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.KEY;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.NULL;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.STRING;
import static gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers.allIdsToStringArray;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_BOOLEAN;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_COMMENT;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_KEY;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_NORMAL_TEXT;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_NULL;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_STRING;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifier;
import gama.ui.viewers.json.eclipse.document.JsonDocumentIdentifiers;
import gama.ui.viewers.json.eclipse.presentation.JsonDefaultTextScanner;
import gama.ui.viewers.json.eclipse.presentation.PresentationSupport;

/**
 *
 * @author Albert Tregnaghi
 *
 */
public class JsonSourceViewerConfiguration extends TextSourceViewerConfiguration {

	/** The scanner. */
	private JsonDefaultTextScanner scanner;

	/** The color manager. */
	private final ColorManager colorManager;

	/** The default text attribute. */
	private final TextAttribute defaultTextAttribute;

	/** The annotation hoover. */
	private final JsonEditorAnnotationHoover annotationHoover;

	/** The adaptable. */
	private final IAdaptable adaptable;

	/** The content assistant. */
	private final ContentAssistant contentAssistant;

	/** The content assist processor. */
	private final JsonEditorSimpleWordContentAssistProcessor contentAssistProcessor;

	/**
	 * Creates configuration by given adaptable
	 * 
	 * @param adaptable
	 *            must provide {@link ColorManager} and {@link IFile}
	 */
	public JsonSourceViewerConfiguration(final IAdaptable adaptable) {
		IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
		this.fPreferenceStore = new ChainedPreferenceStore(
				new IPreferenceStore[] { getPreferences().getPreferenceStore(), generalTextStore });

		Assert.isNotNull(adaptable, "adaptable may not be null!");
		this.annotationHoover = new JsonEditorAnnotationHoover();

		this.contentAssistant = new ContentAssistant();
		contentAssistProcessor = new JsonEditorSimpleWordContentAssistProcessor();
		contentAssistant.enableColoredLabels(true);

		contentAssistant.setContentAssistProcessor(contentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE);
		for (JsonDocumentIdentifier identifier : JsonDocumentIdentifiers.values()) {
			contentAssistant.setContentAssistProcessor(contentAssistProcessor, identifier.getId());
		}

		contentAssistant.addCompletionListener(contentAssistProcessor.getCompletionListener());

		this.colorManager = adaptable.getAdapter(ColorManager.class);
		Assert.isNotNull(colorManager, " adaptable must support color manager");
		this.defaultTextAttribute =
				new TextAttribute(colorManager.getColor(getPreferences().getColor(COLOR_NORMAL_TEXT)));
		this.adaptable = adaptable;
	}

	@Override
	public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
		return contentAssistant;
	}

	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(final ISourceViewer sourceViewer) {
		/*
		 * currently we avoid the default quick assistence parts (spell checking etc.)
		 */
		return null;
	}

	@Override
	public IReconciler getReconciler(final ISourceViewer sourceViewer) {
		/*
		 * currently we avoid the default reconciler mechanism parts (spell checking etc.)
		 */
		return null;
	}

	@Override
	public IAnnotationHover getAnnotationHover(final ISourceViewer sourceViewer) {
		return annotationHoover;
	}

	/**
	 * The Class JsonEditorAnnotationHoover.
	 */
	private static class JsonEditorAnnotationHoover extends DefaultAnnotationHover {
		@Override
		protected boolean isIncluded(final Annotation annotation) {
			if (annotation instanceof MarkerAnnotation) return true;
			/* we do not support other annotations */
			return false;
		}
	}

	@Override
	public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
		/* @formatter:off */
		return allIdsToStringArray(IDocument.DEFAULT_CONTENT_TYPE);
		/* @formatter:on */
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		addDefaultPresentation(reconciler);

		addPresentation(reconciler, STRING.getId(), getPreferences().getColor(COLOR_STRING), SWT.NONE);

		addPresentation(reconciler, COMMENT.getId(), getPreferences().getColor(COLOR_COMMENT), SWT.NONE);

		addPresentation(reconciler, NULL.getId(), getPreferences().getColor(COLOR_NULL), SWT.NONE);

		addPresentation(reconciler, KEY.getId(), getPreferences().getColor(COLOR_KEY), SWT.NONE);

		addPresentation(reconciler, BOOLEAN.getId(), getPreferences().getColor(COLOR_BOOLEAN), SWT.NONE);

		return reconciler;
	}

	/**
	 * Adds the default presentation.
	 *
	 * @param reconciler
	 *            the reconciler
	 */
	private void addDefaultPresentation(final PresentationReconciler reconciler) {
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getDefaultTextScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

	/**
	 * Creates the color token.
	 *
	 * @param rgb
	 *            the rgb
	 * @return the i token
	 */
	private IToken createColorToken(final RGB rgb) {
		Token token = new Token(new TextAttribute(colorManager.getColor(rgb)));
		return token;
	}

	/**
	 * Adds the presentation.
	 *
	 * @param reconciler
	 *            the reconciler
	 * @param id
	 *            the id
	 * @param rgb
	 *            the rgb
	 * @param style
	 *            the style
	 */
	private void addPresentation(final PresentationReconciler reconciler, final String id, final RGB rgb, final int style) {
		TextAttribute textAttribute =
				new TextAttribute(colorManager.getColor(rgb), defaultTextAttribute.getBackground(), style);
		PresentationSupport presentation = new PresentationSupport(textAttribute);
		reconciler.setDamager(presentation, id);
		reconciler.setRepairer(presentation, id);
	}

	/**
	 * Gets the default text scanner.
	 *
	 * @return the default text scanner
	 */
	private JsonDefaultTextScanner getDefaultTextScanner() {
		if (scanner == null) {
			scanner = new JsonDefaultTextScanner(colorManager);
			updateTextScannerDefaultColorToken();
		}
		return scanner;
	}

	/**
	 * Update text scanner default color token.
	 */
	public void updateTextScannerDefaultColorToken() {
		if (scanner == null) return;
		RGB color = getPreferences().getColor(COLOR_NORMAL_TEXT);
		scanner.setDefaultReturnToken(createColorToken(color));
	}

}