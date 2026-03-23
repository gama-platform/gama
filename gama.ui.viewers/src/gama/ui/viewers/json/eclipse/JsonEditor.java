/*******************************************************************************************************
 *
 * JsonEditor.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ENCLOSING_BRACKETS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_MATCHING_BRACKETS_COLOR;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_MATCHING_BRACKETS_ENABLED;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;

import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.viewers.json.document.JSONFormatSupport;
import gama.ui.viewers.json.document.JSONFormatSupport.FormatterResult;
import gama.ui.viewers.json.eclipse.document.JsonFileDocumentProvider;
import gama.ui.viewers.json.eclipse.document.JsonTextFileDocumentProvider;
import gama.ui.viewers.json.eclipse.outline.JsonEditorContentOutlinePage;
import gama.ui.viewers.json.eclipse.outline.JsonEditorTreeContentProvider;
import gama.ui.viewers.json.eclipse.outline.JsonQuickOutlineDialog;
import gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferences;
import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.script.DefaultJsonModelBuilder;
import gama.ui.viewers.json.script.JsonError;
import gama.ui.viewers.json.script.JsonModel;
import gama.ui.viewers.json.script.JsonModelBuilder;

/**
 * The Class JsonEditor.
 */
public class JsonEditor extends TextEditor implements StatusMessageSupport {

	/** The COMMAND_ID of this editor as defined in plugin.xml */
	public static final String EDITOR_ID = "gama.ui.application.editor.json";
	/** The COMMAND_ID of the editor context menu */
	public static final String EDITOR_CONTEXT_MENU_ID = "gama.ui.application.editor.json.context";
	/** The COMMAND_ID of the editor ruler context menu */
	public static final String EDITOR_RULER_CONTEXT_MENU_ID = EDITOR_CONTEXT_MENU_ID + ".ruler";

	/** The Constant FALLBACK_EMPTY_MODEL. */
	private static final JsonModel FALLBACK_EMPTY_MODEL = new JsonModel();

	/** The bracket matcher. */
	private JsonBracketsSupport bracketMatcher = new JsonBracketsSupport();

	/** The additional source viewer support. */
	private SourceViewerDecorationSupport additionalSourceViewerSupport;

	/** The outline page. */
	private JsonEditorContentOutlinePage outlinePage;

	/** The model builder. */
	private final JsonModelBuilder modelBuilder;

	/** The monitor. */
	private final Object monitor = new Object();

	/** The quick outline opened. */
	private boolean quickOutlineOpened;

	/** The last caret position. */
	private int lastCaretPosition;

	/**
	 * Instantiates a new highspeed JSON editor.
	 */
	public JsonEditor() {
		this.modelBuilder = DefaultJsonModelBuilder.INSTANCE;
	}

	/**
	 * Format JSON.
	 */
	public void formatJSON() {
		FormatterResult result = JSONFormatSupport.DEFAULT.formatJSON(getDocumentText());
		if (!result.state.hasContentChanged()) return;
		getDocument().set(result.getFormatted());
	}

	/**
	 * Validate JSON.
	 */
	public void validateJSON() {
		try {
			JSONFormatSupport.DEFAULT.validateJSON(getDocumentText());
		} catch (JsonProcessingException e) {
			JsonLocation location = e.getLocation();
			JsonEditorUtil.addErrorMarker(location.getLineNr(), e.getMessage(), getEditorInput(),
					(int) location.getCharOffset(), (int) location.getCharOffset());
		}
	}

	/**
	 * Opens quick outline
	 */
	public void openQuickOutline() {
		synchronized (monitor) {
			if (quickOutlineOpened) /*
									 * already opened - this is in future the anker point for ctrl+o+o...
									 */
				return;
			quickOutlineOpened = true;
		}
		Shell shell = getEditorSite().getShell();
		JsonModel model = buildOutlineModelWithoutValidation();
		JsonQuickOutlineDialog dialog = new JsonQuickOutlineDialog(this, shell, "Quick outline");
		dialog.setInput(model);

		dialog.open();
		synchronized (monitor) {
			quickOutlineOpened = false;
		}
	}

	/**
	 * Builds the outline model without validation.
	 *
	 * @return the highspeed JSON model
	 */
	private JsonModel buildOutlineModelWithoutValidation() {
		String text = getDocumentText();
		JsonModel model = modelBuilder.build(text, getGroupdArraysTreshold(), true);
		return model;
	}

	/**
	 * Gets the severity.
	 *
	 * @return the severity
	 */
	private int getSeverity() {
		IEditorInput editorInput = getEditorInput();
		if (editorInput == null) return IMarker.SEVERITY_INFO;
		try {
			final IResource resource = ResourceUtil.getResource(editorInput);
			if (resource == null) return IMarker.SEVERITY_INFO;
			int severity = resource.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			return severity;
		} catch (CoreException e) {
			// Might be a project that is not open
		}
		return IMarker.SEVERITY_INFO;
	}

	/**
	 * Adds the error markers.
	 *
	 * @param model
	 *            the model
	 */
	protected void addErrorMarkers(final JsonModel model) {
		if (model == null) return;
		IDocument document = getDocument();
		if (document == null) return;

		List<JsonError> errors = model.getErrors();
		for (JsonError error : errors) {
			int startPos = (int) error.offset;
			int line;
			try {
				line = document.getLineOfOffset(startPos);
			} catch (BadLocationException e) {
				JsonEditorUtil.logError("Cannot get line offset for " + startPos, e);
				line = 0;
			}
			JsonEditorUtil.addErrorMarker(this, line, error);
		}

	}

	@Override
	public void setErrorMessage(final String message) {
		super.setStatusLineErrorMessage(message);
	}

	/**
	 * Gets the bracket matcher.
	 *
	 * @return the bracket matcher
	 */
	public JsonBracketsSupport getBracketMatcher() { return bracketMatcher; }

	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);

		Control adapter = getAdapter(Control.class);
		if (adapter instanceof StyledText text) { text.addCaretListener(new JsonEditorCaretListener()); }

		activateJsonEditorContext();

		installAdditionalSourceViewerSupport();

		StyledText styledText = getSourceViewer().getTextWidget();
		styledText.addKeyListener(new JsonBracketInsertionCompleter(this));

	}

	/**
	 * Gets the outline page.
	 *
	 * @return the outline page
	 */
	public JsonEditorContentOutlinePage getOutlinePage() {
		if (outlinePage == null) { outlinePage = new JsonEditorContentOutlinePage(this); }
		return outlinePage;
	}

	/**
	 * Installs an additional source viewer support which uses editor preferences instead of standard text preferences.
	 * If standard source viewer support would be set with editor preferences all standard preferences would be lost or
	 * had to be reimplmented. To avoid this another source viewer support is installed...
	 */
	private void installAdditionalSourceViewerSupport() {

		additionalSourceViewerSupport = new SourceViewerDecorationSupport(getSourceViewer(), getOverviewRuler(),
				getAnnotationAccess(), getSharedColors());
		additionalSourceViewerSupport.setCharacterPairMatcher(bracketMatcher);
		additionalSourceViewerSupport.setMatchingCharacterPainterPreferenceKeys(
				P_EDITOR_MATCHING_BRACKETS_ENABLED.getId(), P_EDITOR_MATCHING_BRACKETS_COLOR.getId(),
				P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION.getId(), P_EDITOR_ENCLOSING_BRACKETS.getId());

		IPreferenceStore preferenceStoreForDecorationSupport = JsonEditorUtil.getPreferences().getPreferenceStore();
		additionalSourceViewerSupport.install(preferenceStoreForDecorationSupport);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (additionalSourceViewerSupport != null) { additionalSourceViewerSupport.dispose(); }
		if (bracketMatcher != null) {
			bracketMatcher.dispose();
			bracketMatcher = null;
		}

	}

	/**
	 * Gets the back ground color as web.
	 *
	 * @return the back ground color as web
	 */
	public String getBackGroundColorAsWeb() {
		ensureColorsFetched();
		return bgColor;
	}

	/**
	 * Gets the fore ground color as web.
	 *
	 * @return the fore ground color as web
	 */
	public String getForeGroundColorAsWeb() {
		ensureColorsFetched();
		return fgColor;
	}

	/**
	 * Ensure colors fetched.
	 */
	private void ensureColorsFetched() {
		if (bgColor == null || fgColor == null) {

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null) return;
			StyledText textWidget = sourceViewer.getTextWidget();
			if (textWidget == null) return;

			WorkbenchHelper.run(() -> {
				bgColor = ColorUtil.convertToHexColor(textWidget.getBackground());
				fgColor = ColorUtil.convertToHexColor(textWidget.getForeground());
			});
		}

	}

	/** The bg color. */
	private String bgColor;

	/** The fg color. */
	private String fgColor;

	/** The ignore next caret move. */
	private boolean ignoreNextCaretMove;

	@SuppressWarnings ("unchecked")
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (JsonEditor.class.equals(adapter)) return (T) this;
		if (IContentOutlinePage.class.equals(adapter)) return (T) getOutlinePage();
		if (ColorManager.class.equals(adapter)) return (T) getColorManager();
		if (IFile.class.equals(adapter)) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput feditorInput) return (T) feditorInput.getFile();
			return null;
		}
		if (ISourceViewer.class.equals(adapter)) return (T) getSourceViewer();
		if (StatusMessageSupport.class.equals(adapter)) return (T) this;
		if (ITreeContentProvider.class.equals(adapter) || JsonEditorTreeContentProvider.class.equals(adapter)) {
			if (outlinePage == null) return null;
			return (T) outlinePage.getContentProvider();
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {

		bracketMatcher.gotoMatchingBracket(this);
	}

	/**
	 * Get document text - safe way.
	 *
	 * @return string, never <code>null</code>
	 */
	String getDocumentText() {
		IDocument doc = getDocument();
		if (doc == null) return "";
		return doc.get();
	}

	@Override
	protected void doSetInput(final IEditorInput input) throws CoreException {
		setDocumentProvider(createDocumentProvider(input));
		super.doSetInput(input);

		rebuildOutlineAndOrValidate();
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();
		rebuildOutlineAndOrValidate();
	}

	/**
	 * Gets the groupd arrays treshold.
	 *
	 * @return the groupd arrays treshold
	 */
	private int getGroupdArraysTreshold() { return JsonEditorPreferences.getInstance().getGroupdArraysTreshold(); }

	/**
	 * Does rebuild the outline - this is done asynchronous
	 */
	public void rebuildOutlineAndOrValidate() {

		Runnable r = () -> {
			JsonEditorUtil.removeScriptErrors(JsonEditor.this);

			JsonEditorContentOutlinePage page = getOutlinePage();

			boolean outlineBuildEnabled = page.isOutlineBuildEnabled();
			boolean validateOnSaveEnabled = JsonEditorPreferences.getInstance().isValidateOnSaveEnabled();

			if (validateOnSaveEnabled) { validateJSON(); }

			if (!outlineBuildEnabled) {
				page.rebuild(FALLBACK_EMPTY_MODEL); // reset tree
				return;
			}
			JsonModel outlineJSONModel = buildOutlineModelWithoutValidation();
			if (outlineJSONModel == null) { outlineJSONModel = FALLBACK_EMPTY_MODEL; }

			page.rebuild(outlineJSONModel);
		};

		UIJob job = new UIJob("Build JSON outline") {

			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				r.run();
				return Status.OK_STATUS;
			}
		};
		job.schedule(100);
	}

	/**
	 * Creates the document provider.
	 *
	 * @param input
	 *            the input
	 * @return the i document provider
	 */
	private IDocumentProvider createDocumentProvider(final IEditorInput input) {
		if (input instanceof FileStoreEditorInput) return new JsonTextFileDocumentProvider(this);
		return new JsonFileDocumentProvider(this);
	}

	/**
	 * Gets the document.
	 *
	 * @return the document
	 */
	public IDocument getDocument() { return getDocumentProvider().getDocument(getEditorInput()); }

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		super.init(site, input);
		if (site == null) return;
		IWorkbenchPage page = site.getPage();
		if (page == null) return;

		// workaround to show action set for block mode etc.
		// https://www.eclipse.org/forums/index.php/t/366630/
		page.showActionSet("org.eclipse.ui.edit.text.actionSet.presentation");

	}

	@Override
	protected void initializeEditor() {
		setSourceViewerConfiguration(new JsonSourceViewerConfiguration(this));
		super.initializeEditor();
		setEditorContextMenuId(EDITOR_CONTEXT_MENU_ID);
		setRulerContextMenuId(EDITOR_RULER_CONTEXT_MENU_ID);
	}

	/**
	 * Activate highspeed JSON editor context.
	 */
	private void activateJsonEditorContext() {
		IContextService contextService = getSite().getService(IContextService.class);
		if (contextService != null) { contextService.activateContext(EDITOR_CONTEXT_MENU_ID); }
	}

	/**
	 * Gets the color manager.
	 *
	 * @return the color manager
	 */
	private ColorManager getColorManager() { return JsonEditorActivator.getDefault().getColorManager(); }

	/**
	 * Handle color settings changed.
	 */
	public void handleColorSettingsChanged() {
		// done like in TextEditor for spelling
		ISourceViewer viewer = getSourceViewer();
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (viewer instanceof ISourceViewerExtension2 viewerExtension2) {
			viewerExtension2.unconfigure();
			if (configuration instanceof JsonSourceViewerConfiguration gconf) {
				gconf.updateTextScannerDefaultColorToken();
			}
			viewer.configure(configuration);
		}
	}

	/**
	 * Toggles comment of current selected lines
	 */
	public void toggleComment() {
		ISelection selection = getSelectionProvider().getSelection();
		if (!(selection instanceof TextSelection)) return;
		IDocumentProvider dp = getDocumentProvider();
		IDocument doc = dp.getDocument(getEditorInput());
		TextSelection ts = (TextSelection) selection;
		int startLine = ts.getStartLine();
		int endLine = ts.getEndLine();

		/* do comment /uncomment */
		for (int i = startLine; i <= endLine; i++) {
			IRegion info;
			try {
				info = doc.getLineInformation(i);
				int offset = info.getOffset();
				String line = doc.get(info.getOffset(), info.getLength());
				StringBuilder foundCode = new StringBuilder();
				StringBuilder whitespaces = new StringBuilder();
				for (int j = 0; j < line.length(); j++) {
					char ch = line.charAt(j);
					if (Character.isWhitespace(ch)) {
						if (foundCode.length() == 0) { whitespaces.append(ch); }
					} else {
						foundCode.append(ch);
					}
					if (foundCode.length() > 2) { break; }
				}
				int whitespaceOffsetAdd = whitespaces.length();
				if ("REM".equals(foundCode.toString())) {
					/* comment before */
					doc.replace(offset + whitespaceOffsetAdd, 4, "");
				} else {
					/* not commented */
					doc.replace(offset, 0, "REM ");
				}

			} catch (BadLocationException e) {
				/* ignore and continue */
				continue;
			}

		}
		/* reselect */
		int selectionStartOffset;
		try {
			selectionStartOffset = doc.getLineOffset(startLine);
			int endlineOffset = doc.getLineOffset(endLine);
			int endlineLength = doc.getLineLength(endLine);
			int endlineLastPartOffset = endlineOffset + endlineLength;
			int length = endlineLastPartOffset - selectionStartOffset;

			ISelection newSelection = new TextSelection(selectionStartOffset, length);
			getSelectionProvider().setSelection(newSelection);
		} catch (BadLocationException e) {
			/* ignore */
		}
	}

	/**
	 * Open selected tree item in editor.
	 *
	 * @param selection
	 *            the selection
	 * @param grabFocus
	 *            the grab focus
	 */
	public void openSelectedTreeItemInEditor(final ISelection selection, final boolean grabFocus) {
		if (selection instanceof IStructuredSelection ss) {
			Object firstElement = ss.getFirstElement();
			if (firstElement instanceof Item item) {
				int offset = item.getOffset();
				int length = item.getLength();
				if (length == 0) {
					/* fall back */
					length = 1;
				}
				ignoreNextCaretMove = true;
				selectAndReveal(offset, length);
				if (grabFocus) { setFocus(); }
			}
		}
	}

	/**
	 * Gets the item at carret position.
	 *
	 * @return the item at carret position
	 */
	public Item getItemAtCarretPosition() { return getItemAt(lastCaretPosition); }

	/**
	 * Gets the item at.
	 *
	 * @param offset
	 *            the offset
	 * @return the item at
	 */
	public Item getItemAt(final int offset) {
		if (outlinePage == null) return null;
		JsonEditorTreeContentProvider contentProvider = outlinePage.getContentProvider();
		if (contentProvider == null) return null;
		Item item = contentProvider.tryToFindByOffset(offset);
		return item;
	}

	/**
	 * Select function.
	 *
	 * @param text
	 *            the text
	 */
	public void selectFunction(final String text) {
		System.out.println("should select functin:" + text);

	}

	/**
	 * Gets the preferences.
	 *
	 * @return the preferences
	 */
	public JsonEditorPreferences getPreferences() { return JsonEditorPreferences.getInstance(); }

	/**
	 * The listener interface for receiving highspeedJSONEditorCaret events. The class that is interested in processing
	 * a highspeedJSONEditorCaret event implements this interface, and the object created with that class is registered
	 * with a component using the component's <code>addJsonEditorCaretListener</code> method. When the
	 * highspeedJSONEditorCaret event occurs, that object's appropriate method is invoked.
	 *
	 * @see JsonEditorCaretEvent
	 */
	private class JsonEditorCaretListener implements CaretListener {

		@Override
		public void caretMoved(final CaretEvent event) {
			if (event == null) return;
			lastCaretPosition = event.caretOffset;
			if (ignoreNextCaretMove) {
				ignoreNextCaretMove = false;
				return;
			}
			if (outlinePage == null) return;
			outlinePage.onEditorCaretMoved(event.caretOffset);
		}

	}

	/**
	 * Mark as dirty.
	 */
	private void markAsDirty() {
		IDocumentProvider provider = getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension ext) { ext.setCanSaveDocument(getEditorInput()); }
		// firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	public boolean isDirty() { return super.isDirty(); }

	/**
	 * Mark dirty because format was necessary for one liner handling.
	 */
	public void markDirtyBecauseFormatWasNecessaryForOneLinerHandling() {
		/*
		 * document was a "one liner" and was transformed to multi lines - so rendered now fast by eclipse
		 */
		String message = "Auto format done - was necessary because one liners in eclipse would be extreme slow!";
		setStatusLineMessage(message);
		WorkbenchHelper.asyncRun(() -> {
			JsonEditorUtil.addInfoMarker(this, 0, message);
			markAsDirty();
		});
	}

	/**
	 * Creates the json as one line.
	 *
	 * @return the string
	 */
	public String createJsonAsOneLine() {
		String json = getDocumentText();
		return JSONFormatSupport.DEFAULT.createJSONAsOneLine(json);
	}

}
