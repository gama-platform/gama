/*******************************************************************************************************
 *
 * GamlEditor.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import static gama.api.utils.prefs.GamaPreferences.Modeling.EDITOR_COLLAPSE_BUTTONS;
import static gama.api.utils.prefs.GamaPreferences.Modeling.EDITOR_EXPERIMENT_MENU;
import static gama.ui.shared.resources.IGamaIcons.MARKER_ERROR;
import static gama.ui.shared.resources.IGamaIcons.SMALL_DROPDOWN;
import static gaml.compiler.ui.editor.GamlEditorState.NO_EXP_DEFINED;
import static org.eclipse.ui.texteditor.ITextEditorActionConstants.DELETE;
import static org.eclipse.ui.texteditor.ITextEditorActionConstants.FIND;
import static org.eclipse.ui.texteditor.ITextEditorActionConstants.REDO;
import static org.eclipse.ui.texteditor.ITextEditorActionConstants.UNDO;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerExtension;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningPreferenceConstants;
import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningProvider;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.xtext.ui.XtextUIMessages;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionProvider;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.templates.XtextTemplateContextType;
import org.eclipse.xtext.ui.editor.validation.IValidationIssueProcessor;
import org.eclipse.xtext.ui.editor.validation.MarkerCreator;
import org.eclipse.xtext.ui.editor.validation.MarkerIssueProcessor;
import org.eclipse.xtext.ui.editor.validation.ValidationJob;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.MarkerTypeProvider;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Inject;
import com.google.inject.Injector;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.IModelsManager;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.GamlFileExtension;
import gama.api.utils.GamlProperties;
import gama.api.utils.StringUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.DEBUG;
import gama.dev.FLAGS;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaFonts;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.UICleanupTasks;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.resource.GamlResourceServices;
import gaml.compiler.ui.decorators.GamlAnnotationImageProvider;
import gaml.compiler.ui.editor.toolbar.CreateExperimentSelectionListener;
import gaml.compiler.ui.editor.toolbar.EditorSearchControls;
import gaml.compiler.ui.editor.toolbar.EditorToolbar;
import gaml.compiler.ui.editor.toolbar.OpenExperimentSelectionListener;
import gaml.compiler.ui.editor.toolbar.OpenImportedErrorSelectionListener;
import gaml.compiler.ui.editor.toolbar.RevalidateModelSelectionListener;
import gaml.compiler.ui.reference.BuiltinReferenceMenu;
import gaml.compiler.ui.reference.ColorReferenceMenu;
import gaml.compiler.ui.reference.OperatorsReferenceMenu;
import gaml.compiler.ui.reference.TemplateReferenceMenu;
import gaml.compiler.ui.editor.refactoring.ExtractActionHandler;
import gaml.compiler.ui.templates.GamlEditTemplateDialogFactory;
import gaml.compiler.ui.templates.GamlTemplateStore;
import gaml.compiler.validation.IGamlBuilderListener;

/**
 * The Class GamlEditor.
 */
/*
 * The class GamlEditor.
 *
 * @author drogoul
 *
 * @since 4 mars 2012
 */

@SuppressWarnings ("all")
public class GamlEditor extends XtextEditor implements IGamlBuilderListener, IToolbarDecoratedView {

	static {
		DEBUG.OFF();
	}

	/** The preference store. */
	private static IPreferenceStore miningPreferencesStore;

	/** The to remove. */
	private final static Set<String> TO_REMOVE_FROM_MENUS = Set.of("revert", "save", "__PREFS__.ContextAction",
			"QuickAssist", "Open W&ith", "Sho&w In	⌥⌘W", "group.open");

	/** The Constant ANNOTATION_INFO_TYPES. */
	// Standard ID for Info annotations
	private final static Set<String> ANNOTATION_INFO_TYPES =
			Set.of("org.eclipse.ui.workbench.texteditor.info", "org.eclipse.xtext.ui.editor.info");

	/**
	 * Gets the preferences.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the preferences
	 * @date 16 sept. 2023
	 */
	protected static IPreferenceStore getPreferences() {
		if (miningPreferencesStore == null) {
			miningPreferencesStore = AnnotationCodeMiningPreferenceConstants.getPreferenceStore();
		}
		return miningPreferencesStore;
	}

	static {
		IPreferenceStore node = getPreferences();
		if (node != null) {
			node.setDefault(AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL,
					AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__WARNING
							| AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__INFO);
			node.setValue(AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL,
					AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__WARNING
							| AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__INFO);
		}

	}

	/** The diagram opener. */
	static IDiagramOpener diagramOpener;

	/** The BUTTON_IMAGES. */
	static final Map<String, Image> BUTTON_IMAGES = new HashMap();

	/** The menu BUTTON_IMAGES. */
	static final Map<String, String> MENU_IMAGES = new HashMap();

	/** The Constant BUTTON_HEIGHT. */
	static final int BUTTON_HEIGHT = 20;

	/** The Constant SCHEDULE_DELAY. */
	public static final int SCHEDULE_DELAY = 0;

	/** The button padding. How much space between each experiment button */
	static {
		final var store = EditorsUI.getPreferenceStore();
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR, false);
		store.setDefault("spellingEnabled", false);
		store.setValue("spellingEnabled", false);
		BUTTON_IMAGES.put(IKeyword.BATCH, GamaIcon.named(IGamaIcons.BUTTON_BATCH).image());
		BUTTON_IMAGES.put(IKeyword.RECORD, GamaIcon.named(IGamaIcons.BUTTON_BACK).image());
		BUTTON_IMAGES.put("regular", GamaIcon.named(IGamaIcons.BUTTON_GUI).image());
		MENU_IMAGES.put(IKeyword.BATCH, ThemeHelper.isDark() ? IGamaIcons.BUTTON_BATCH : IGamaIcons.MENU_BATCH);
		MENU_IMAGES.put(IKeyword.RECORD, ThemeHelper.isDark() ? IGamaIcons.BUTTON_BACK : IGamaIcons.MENU_BACK);
		MENU_IMAGES.put("regular", ThemeHelper.isDark() ? IGamaIcons.BUTTON_GUI : IGamaIcons.MENU_GUI);
		BUTTON_IMAGES.put("new", GamaIcon.named(IGamaIcons.ADD_EXPERIMENT).image());
	}

	/**
	 * Instantiates a new gaml editor.
	 */
	public GamlEditor() {
		dndHandler = new GamlEditorDragAndDropHandler(this);
	}

	/** The state. */
	// 'volatile' is required: this field is written by the reconciler background thread
	// (in validationEnded()) and read by the UI thread (e.g. in the controlResized handler).
	// Without volatile the JMM gives no visibility guarantee, leading to stale reads.
	volatile GamlEditorState state = new GamlEditorState(null, Collections.EMPTY_LIST);

	/** The toolbar. */
	GamaToolbar2 toolbar;

	/** The toolbar parent. */
	Composite toolbarParent;

	/** The find control. */
	private EditorSearchControls findControl;

	/** The resource set provider. */
	@Inject public IResourceSetProvider resourceSetProvider;

	/** The injector. */
	@Inject Injector injector;

	/** The modelsManager. */
	@Inject IModelsManager modelsManager;

	/** The template dialog factory. */
	@Inject private GamlEditTemplateDialogFactory templateDialogFactory;

	/** The template store. */
	@Inject private TemplateStore templateStore;

	/** The validator. */
	@Inject private IResourceValidator validator;

	/** The marker creator. */
	@Inject private MarkerCreator markerCreator;

	/** The marker type provider. */
	@Inject private MarkerTypeProvider markerTypeProvider;

	/** The issue resolver. */
	@Inject private IssueResolutionProvider issueResolver;

	/** The highlighting configuration. */
	@Inject private IHighlightingConfiguration highlightingConfiguration;

	/** The dnd handler. */
	private final GamlEditorDragAndDropHandler dndHandler;

	/** The dnd changed listener. */
	private final IPreferenceAfterChangeListener dndChangedListener = newValue -> {
		uninstallTextDragAndDrop(getInternalSourceViewer());
		installTextDragAndDrop(getInternalSourceViewer());
	};

	/** The is text drag and drop installed. */
	private boolean fIsTextDragAndDropInstalled;

	/** The file URI. */
	private URI fileURI;

	/** The image provider. */
	static GamlAnnotationImageProvider imageProvider = new GamlAnnotationImageProvider();

	@Override
	protected IAnnotationAccess createAnnotationAccess() {

		return new DefaultMarkerAnnotationAccess() {

			@Override
			public int getLayer(final Annotation annotation) {
				if (annotation.isMarkedDeleted()) return IAnnotationAccessExtension.DEFAULT_LAYER;
				return super.getLayer(annotation);
			}

			@Override
			public void paint(final Annotation annotation, final GC gc, final Canvas canvas, final Rectangle bounds) {
				final var image = imageProvider.getManagedImage(annotation);
				if (image != null) {
					ImageUtilities.drawImage(image, gc, canvas, bounds, SWT.CENTER, SWT.TOP);
				} else {
					super.paint(annotation, gc, canvas, bounds);
				}

			}

			@Override
			public boolean isPaintable(final Annotation annotation) {
				if (imageProvider.getManagedImage(annotation) != null) return true;
				return super.isPaintable(annotation);
			}

		};
	}

	@Override
	protected void rulerContextMenuAboutToShow(final IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		menu.remove("projection");

		final IMenuManager foldingMenu = new MenuManager(XtextUIMessages.Editor_FoldingMenu_name, "projection"); //$NON-NLS-1$
		menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);
		var action = getAction("FoldingToggle"); //$NON-NLS-1$
		foldingMenu.add(action);
		action = getAction("FoldingExpandAll"); //$NON-NLS-1$
		foldingMenu.add(action);
		action = getAction("FoldingCollapseAll"); //$NON-NLS-1$
		foldingMenu.add(action);
		action = getAction("FoldingCollapseStrings"); //$NON-NLS-1$
		foldingMenu.add(action);
		action = getAction("FoldingRestore"); //$NON-NLS-1$
		foldingMenu.add(action);
	}

	@Override
	public void dispose() {
		GamaPreferences.Modeling.EDITOR_DRAG_RESOURCES.removeChangeListener(dndChangedListener);
		GamlResourceServices.removeResourceListener(this);
		super.dispose();
	}

	/**
	 * Gets the template store.
	 *
	 * @return the template store
	 */
	public GamlTemplateStore getTemplateStore() { return (GamlTemplateStore) templateStore; }

	/**
	 * Gets the template factory.
	 *
	 * @return the template factory
	 */
	public GamlEditTemplateDialogFactory getTemplateFactory() { return templateDialogFactory; }

	/**
	 * Builds the right toolbar.
	 */
	private void buildRightToolbar() {
		findControl = new EditorToolbar(this).fill(toolbar.getToolbar(SWT.RIGHT));
		fakeButton =
				FlatButton.button(toolbar.getToolbar(SWT.RIGHT), IGamaColors.OK, "", BUTTON_IMAGES.get(IKeyword.BATCH));
		fakeButton.setVisible(false);
		toolbar.requestLayout();
	}

	/** The fake button. To compute text size */
	FlatButton fakeButton;

	@Override
	public boolean isLineNumberRulerVisible() {
		final var store = getAdvancedPreferenceStore();
		return store != null ? store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER)
				: false;
	}

	/**
	 * Checks if is range indicator enabled.
	 *
	 * @return true, if is range indicator enabled
	 */
	public boolean isRangeIndicatorEnabled() { return getInternalSourceViewer().isProjectionMode(); }

	/**
	 * Gets the advanced preference store.
	 *
	 * @return the advanced preference store
	 */
	public final IPreferenceStore getAdvancedPreferenceStore() { return super.getPreferenceStore(); }

	/**
	 * Configure tab folder.
	 *
	 * @param compo
	 *            the compo
	 */
	private void configureTabFolder(final Composite compo) {
		var c = compo;
		while (c != null) {
			if (c instanceof CTabFolder) { break; }
			c = c.getParent();
		}
		if (c != null) {
			final var folder = (CTabFolder) c;
			folder.setMaximizeVisible(true);
			folder.setMinimizeVisible(true);
			folder.setMinimumCharacters(10);
			folder.setMRUVisible(true);
			folder.setUnselectedCloseVisible(true);
			folder.setHighlightEnabled(true);
			folder.setSimple(true);
		}

	}

	@Override
	public void createPartControl(final Composite compo) {
		gama.dev.DEBUG.OUT("Creating part control of " + this.getPartName());
		configureTabFolder(compo);
		toolbarParent = GamaToolbarFactory.createToolbars(this, compo);
		GridLayoutFactory.fillDefaults().spacing(0, 0).extendedMargins(0, 5, 0, 0).applyTo(toolbarParent);
		// Asking the editor to fill the rest
		final var editor = new Composite(toolbarParent, SWT.NONE);
		final var data = new GridData(SWT.FILL, SWT.FILL, true, true);
		editor.setLayoutData(data);
		editor.setLayout(new FillLayout());
		super.createPartControl(editor);
		editor.addControlListener(new ControlListener() {

			long lastEvent;

			@Override
			public void controlMoved(final ControlEvent e) {}

			@Override
			public void controlResized(final ControlEvent e) {
				WorkbenchHelper.asyncRun(() -> {
					long time = System.currentTimeMillis();
					if (time - lastEvent > 500) {
						lastEvent = time;
						updateToolbar(state, true);
					}
				});
			}
		});
		toolbarParent.requestLayout();
		// See issue #502 https://github.com/gama-platform/gama/issues/502
		this.getStyledText().addFocusListener(new FocusListener() {

			@Override
			public void focusLost(final FocusEvent e) {}

			@Override
			public void focusGained(final FocusEvent e) {
				IActionBars actionBars = GamlEditor.this.getEditorSite().getActionBars();
				if (actionBars != null) {
					actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), getAction(DELETE));
					actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), getAction(UNDO));
					actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), getAction(REDO));
					actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), getAction(FIND));
					actionBars.updateActionBars();
				}
			}
		});
		AnnotationPreference ap =
				getAnnotationPreferenceLookup().getAnnotationPreference("org.eclipse.ui.workbench.texteditor.info");
		ap.setTextStyleValue(AnnotationPreference.STYLE_NONE);
		ap.setTextPreferenceValue(false);
		ap = getAnnotationPreferenceLookup().getAnnotationPreference("org.eclipse.xtext.ui.editor.info");
		ap.setTextStyleValue(AnnotationPreference.STYLE_NONE);
		ap.setTextPreferenceValue(false);
	}

	@Override
	public boolean isEditable() { return FLAGS.IS_READ_ONLY ? false : super.isEditable(); }

	@Override
	protected void initializeDirtyStateSupport() {
		if (getDocument() != null) {
			fileURI = ((XtextDocument) getDocument()).getResourceURI();
			GamlResourceServices.addResourceListener(fileURI, GamlEditor.this);
			super.initializeDirtyStateSupport();
			scheduleValidationJob();
		}
	}

	/**
	 * Schedules a one-shot validation job when the editor is first opened. The job validates the document once so that
	 * error markers and the toolbar state are populated before the user makes any edit.
	 *
	 * <p>
	 * <strong>Locking note:</strong> the original implementation wrapped the entire
	 * {@code validator.validate()} call inside {@code getDocument().readOnly(…)}, holding the
	 * XtextDocument read-lock for the full duration of GAML's semantic validation (which can take
	 * hundreds of milliseconds for large models). While a read-lock is held, any pending
	 * {@code modify()} call — including the reconciler's own re-parse triggered by the first
	 * keystroke — must wait, and the REQUIRED_PLUGINS pragma auto-insert (dispatched via
	 * {@code asyncRun} from {@link #validationEnded}) can also block when it tries to apply a
	 * {@code ReplaceEdit}. Since {@link #SCHEDULE_DELAY} is 0, both this job and the
	 * {@link GamlReconciler} fire immediately, racing over the same {@code ValidationContext}.
	 * </p>
	 *
	 * <p>
	 * The fix is to obtain the resource reference <em>outside</em> any document token and call
	 * {@code validator.validate()} directly, without holding a read-lock. The validator already
	 * takes its own snapshot of the resource when needed and is safe to call without an outer lock.
	 * </p>
	 */
	private void scheduleValidationJob() {
		final IValidationIssueProcessor processor = new MarkerIssueProcessor(getResource(),
				getInternalSourceViewer().getAnnotationModel(), markerCreator, markerTypeProvider);
		final ValidationJob validate = new ValidationJob(validator, getDocument(), processor, CheckMode.FAST_ONLY) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				// Retrieve the resource reference without holding the document read-lock.
				// Calling validator.validate() under readOnly() would pin the lock for the entire
				// GAML semantic validation, blocking the reconciler write-lock and causing races
				// with the concurrent GamlReconciler that also fires at SCHEDULE_DELAY = 0.
				final var resource = getDocument().readOnly(r -> r);
				if (resource == null || resource.isValidationDisabled()) return Status.OK_STATUS;
				final var issues = validator.validate(resource, getCheckMode(), null);
				processor.processIssues((List<Issue>) issues, monitor);
				return Status.OK_STATUS;
			}
		};
		validate.schedule(GamlEditor.SCHEDULE_DELAY);
	}

	@Override
	public boolean isOverviewRulerVisible() {
		final var viewer = getInternalSourceViewer();
		if (viewer == null) return super.isOverviewRulerVisible();
		return viewer.isOverviewVisible();
	}

	@Override
	public void showOverviewRuler() {
		getInternalSourceViewer().showAnnotationsOverview(true);
	}

	@Override
	public void hideOverviewRuler() {
		getInternalSourceViewer().showAnnotationsOverview(false);
	}

	@Override
	public GamaSourceViewer getInternalSourceViewer() { return (GamaSourceViewer) super.getInternalSourceViewer(); }

	@Override
	protected void installFoldingSupport(final ProjectionViewer projectionViewer) {
		super.installFoldingSupport(projectionViewer);
		if (!isRangeIndicatorEnabled()) { projectionViewer.doOperation(ProjectionViewer.TOGGLE); }
	}

	/** Timestamp of the last navigation-history mark, used to throttle {@link #markInNavigationHistory()} calls. */
	private long lastNavigationHistoryMark = 0L;

	/**
	 * Timestamp of the last {@link #super#handleCursorPositionChanged()} dispatch. Used to throttle the super call
	 * and avoid overwhelming the Xtext mark-occurrences and status-bar machinery during rapid cursor movement
	 * (fast typing, or navigating through Find/Replace results). On Windows, the Eclipse Job scheduler and the
	 * {@link org.eclipse.xtext.ui.editor.model.XtextDocument} read-lock acquisition are noticeably slower than on
	 * other platforms; without throttling, a burst of mark-occurrences job cancel/reschedule cycles can freeze the
	 * UI for hundreds of milliseconds.
	 *
	 * <p>
	 * The super call is capped at once per 50 ms. This is imperceptible for status-bar updates and does not affect
	 * correctness: the status bar is refreshed as soon as the cursor is idle for 50 ms.
	 * </p>
	 */
	private long lastSuperCursorUpdate = 0L;

	@Override
	protected void handleCursorPositionChanged() {
		GamaSourceViewer v = getInternalSourceViewer();
		if (getSelectionProvider() == null || v == null || v.getControl() == null || v.getControl().isDisposed())
			return;
		final long now = System.currentTimeMillis();
		// Throttle the super call (mark-occurrences rescheduling + status-bar update) to at most
		// once every 50 ms. This prevents job-scheduler overload during fast typing or rapid
		// find-result navigation, which was the main source of Windows editor freezes.
		if (now - lastSuperCursorUpdate > 50) {
			lastSuperCursorUpdate = now;
			super.handleCursorPositionChanged();
		}
		if (now - lastNavigationHistoryMark > 500) {
			lastNavigationHistoryMark = now;
			this.markInNavigationHistory();
		}
	}

	/**
	 * Update toolbar.
	 *
	 * @param newState
	 *            the new state
	 * @param forceState
	 *            the force state
	 */

	ToolItem addExperiments = null;

	/** The previous show experiments. */
	boolean previousShowExperiments = false;

	/**
	 * Update toolbar.
	 *
	 * @param newState
	 *            the new state
	 * @param forceState
	 *            the force state
	 */
	private void updateToolbar(final GamlEditorState newState, final boolean forceState) {
		DEBUG.OUT("Updating toolbar for " + this.getTitle());
		if (forceState || !state.equals(newState)) {
			WorkbenchHelper.runInUI("Editor refresh", 50, m -> {
				if (toolbar == null || toolbar.isDisposed()) return;
				toolbar.wipe(SWT.LEFT, 1);
				// getDocument().getAdapter(IFile.class) can return null if the editor is being
				// initialised or disposed; skip the update in that case to avoid a NullPointerException
				// whose exception-handling overhead can cause a visible stutter on Windows.
				final IFile editorFile = getDocument().getAdapter(IFile.class);
				boolean showExperiments = editorFile != null
						&& !GamlFileExtension.isExperiment(editorFile.getName()) && newState.showExperiments;
				if (addExperiments == null || showExperiments != previousShowExperiments) {
					updateAddExperimentButton(showExperiments);
				}
				final GamaUIColor c = newState.getColor();
				String msg = newState.getStatus();

				Selector listener = null;
				String imageName = null;

				if (NO_EXP_DEFINED.equals(msg)) {
					msg = null;
				} else if (newState.hasImportedErrors) {
					listener = new OpenImportedErrorSelectionListener(GamlEditor.this, newState,
							toolbar.getToolbar(SWT.LEFT));
					imageName = SMALL_DROPDOWN;
				} else if (msg != null) {
					listener = new RevalidateModelSelectionListener(GamlEditor.this);
					imageName = MARKER_ERROR;
				} else {
					listener = new OpenExperimentSelectionListener(GamlEditor.this, newState, modelsManager);
				}
				if (msg != null) {
					toolbar.button(c, msg, GamaIcon.named(imageName).image(), listener, BUTTON_HEIGHT, SWT.LEFT);
				} else if (newState.showExperiments) {
					if (EDITOR_EXPERIMENT_MENU.getValue()
							|| EDITOR_COLLAPSE_BUTTONS.getValue() && buttonsOverflow(newState)) {
						displayExperimentMenu(newState, listener);
					} else {
						displayExperimentButtons(newState, listener);
					}
				}
				toolbar.requestLayout();

			});
		}

	}

	/** The listener. */

	/**
	 * @param showExperiments
	 */
	private void updateAddExperimentButton(final boolean showExperiments) {
		if (addExperiments == null) {
			addExperiments = GamaCommand
					.build("editor/add.experiment", null, "",
							new CreateExperimentSelectionListener(GamlEditor.this, toolbar.getToolbar(SWT.LEFT)))
					.toItem(toolbar.getToolbar(SWT.LEFT));
			toolbar.getToolbar(SWT.LEFT).space(8);
		}
		if (showExperiments) {
			addExperiments.setToolTipText("Add an experiment to the model");
		} else {
			addExperiments.setToolTipText("No experiment can be added");
		}
		addExperiments.setEnabled(showExperiments);
	}

	/**
	 * Compute width.
	 *
	 * @param newState
	 *            the new state
	 * @return the int
	 */
	private int computeWidth(final GamlEditorState newState) {
		int width = 0;
		for (final String text : newState.abbreviations) {
			if (text == null) { continue; }
			fakeButton.setText(text);
			width += fakeButton.computeSize(SWT.DEFAULT, 12).x + 2;
		}
		return width;
	}

	/**
	 * Returns true when the total pixel width of all experiment buttons exceeds the space currently available in the
	 * left toolbar. If the toolbar has not been sized yet (available width unknown), returns false so that buttons are
	 * shown optimistically and the next resize event will re-evaluate.
	 *
	 * @param newState
	 *            the new state
	 * @return true if the buttons would overflow the left toolbar
	 */
	private boolean buttonsOverflow(final GamlEditorState newState) {
		final int available = toolbar.getAvailableLeftWidth();
		if (available < 0) return false; // toolbar not yet laid out — show buttons
		return computeWidth(newState) > available;
	}

	/**
	 * Display experiment buttons.
	 *
	 * @param newState
	 *            the new state
	 * @param listener
	 *            the listener
	 */
	private void displayExperimentButtons(final GamlEditorState state, final Selector listener) {
		var index = 0;
		for (final String text : state.abbreviations) {
			if (text == null) return;
			final var expType = state.types.get(index++);
			final var type = IKeyword.BATCH.equals(expType) ? IKeyword.BATCH
					: IKeyword.RECORD.equals(expType) ? IKeyword.RECORD : "regular";
			final var t =
					toolbar.button(IGamaColors.OK, text, BUTTON_IMAGES.get(type), listener, BUTTON_HEIGHT, SWT.LEFT);
			t.setToolTipText("Executes the " + type + " experiment named '" + text + "'");
			t.setData("index", index);
			t.getControl().setData("exp", text);
		}
		// Necessary to recompute the width correctly
		toolbar.normalizeToolbars();
	}

	/**
	 * Display experiment menu.
	 *
	 * @param state
	 *            the state
	 * @param listener
	 *            the listener
	 */
	private void displayExperimentMenu(final GamlEditorState state, final Selector listener) {

		final ToolItem menu = toolbar.menuButton(IGamaColors.OK, "Run Experiment...", SWT.LEFT);
		final FlatButton b = ((FlatButton) menu.getControl()).withHeight(BUTTON_HEIGHT);

		b.setSelectionListener(new Selector() {

			Menu popupMenu;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (popupMenu == null || popupMenu.isDisposed()) {
					popupMenu = new Menu(toolbar.getShell(), SWT.POP_UP);
					// Dispose the popup menu automatically when the button is disposed (toolbar wipe)
					b.addDisposeListener(de -> { if (popupMenu != null && !popupMenu.isDisposed()) { popupMenu.dispose(); } });
					fillMenu();
				}
				final Point point = toolbar.toDisplay(new Point(e.x, e.y + toolbar.getSize().y));
				popupMenu.setLocation(point.x, point.y);
				popupMenu.setVisible(true);
			}

			private void fillMenu() {
				var index = 0;
				for (final String text : state.abbreviations) {
					if (text == null) return;
					final var expType = state.types.get(index++);
					final String type = IKeyword.BATCH.equals(expType) ? IKeyword.BATCH
							: IKeyword.RECORD.equals(expType) ? IKeyword.RECORD : "regular";
					final String image = MENU_IMAGES.get(type);
					GamaMenu.action(popupMenu, text, listener, image).setData("exp", text);
				}
			}
		});
		toolbar.normalizeToolbars();
	}

	@Override
	public void validationEnded(final IModelDescription model, final Iterable<? extends IDescription> newExperiments,
			final IValidationContext status) {

		// updateCodeMinings() accesses SWT-backed objects and MUST be called on the UI thread.
		// validationEnded() is invoked by the GamlReconciler background thread (via
		// GamlResource.validate() → updateWith() → GamlResourceServices.updateState()), so we
		// dispatch here. On Windows, cross-thread widget access falls back to Win32 SendMessage()
		// which is synchronous – calling it directly from the background thread stalls that thread
		// until the UI thread processes the message, producing the reported editor freezes.
		WorkbenchHelper.asyncRun(() -> {
			final GamaSourceViewer v = getInternalSourceViewer();
			if (v != null && !v.getControl().isDisposed()) { v.updateCodeMinings(); }
		});

		if (GamaPreferences.Experimental.REQUIRED_PLUGINS.getValue() && model != null && !status.hasErrors()) {
			String requires = "@" + IKeyword.PRAGMA_REQUIRES;
			GamlProperties meta = new GamlProperties();
			model.collectMetaInformation(meta);
			String newLine = requires + " " + meta.get(GamlProperties.PLUGINS);
			IXtextDocument document = getDocument();
			WorkbenchHelper.asyncRun(() -> {
				int offset;
				try {
					offset = document.search(0, requires, true, true, false);
					if (offset > -1) {
						int length = document.getLineInformationOfOffset(offset).getLength();
						if (!newLine.equals(document.get(offset, length))) {
							new ReplaceEdit(offset, length, newLine).apply(document);
						}
					} else {
						new InsertEdit(0, StringUtils.LN + newLine + StringUtils.LN + StringUtils.LN)
								.apply(getDocument());
					}
				} catch (BadLocationException e) {}
			});
		}

		// }
		// });

		if (newExperiments == null && state != null) {
			updateToolbar(state, true);
		} else {
			final var newState = new GamlEditorState(status, newExperiments);
			updateToolbar(newState, false);
			state = newState;
		}
	}

	/**
	 * The Class GamlCodeMiningProvider.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 sept. 2023
	 */
	public class GamlCodeMiningProvider extends AnnotationCodeMiningProvider implements ICodeMiningProvider {

		/**
		 * Instantiates a new gaml code mining provider.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @date 16 sept. 2023
		 */
		GamlCodeMiningProvider() {
			this.setContext(GamlEditor.this);
		}

		/** The empty. */
		static CompletableFuture<List<? extends ICodeMining>> EMPTY =
				CompletableFuture.completedFuture(Collections.EMPTY_LIST);

		@Override
		public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(final ITextViewer viewer,
				final IProgressMonitor monitor) {
			if (!GamaPreferences.Modeling.EDITOR_MINING.getValue()) return EMPTY;
			return super.provideCodeMinings(viewer, monitor);
		}

	}

	/**
	 * Installs code mining providers, preserving any providers already registered via the Eclipse extension point
	 * {@code org.eclipse.ui.workbench.texteditor.codeMiningProviders} (which includes third-party providers such as
	 * GitHub Copilot). The GAML-specific {@link GamlCodeMiningProvider} <em>replaces</em> the default Eclipse
	 * {@link AnnotationCodeMiningProvider} that {@code super.installCodeMiningProviders()} installs automatically for
	 * all text editors, so that annotation code minings are not rendered twice.
	 *
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#installCodeMiningProviders()
	 */
	@Override
	protected void installCodeMiningProviders() {
		// Let the framework install any providers registered via the extension point
		// (e.g. GitHub Copilot's ICodeMiningProvider) before adding the GAML one.
		super.installCodeMiningProviders();
		// Replace the default AnnotationCodeMiningProvider installed by super (which handles
		// annotation-based code minings for all text editors) with the GAML-specific subclass.
		// Using replaceAnnotationCodeMiningProvider() instead of addCodeMiningProvider() avoids
		// having two AnnotationCodeMiningProvider instances active simultaneously, which would
		// cause every annotation code mining to be rendered twice in the editor.
		getInternalSourceViewer().replaceAnnotationCodeMiningProvider(new GamlCodeMiningProvider());
	}

	/**
	 * Returns the adapter for the given class. Explicitly exposes {@link ISourceViewer} and {@link ITextViewer} so that
	 * external tools such as GitHub Copilot can always obtain a reference to the underlying viewer, even when the editor
	 * wraps its content in additional composites.
	 *
	 * @param <T>
	 *            the target adapter type
	 * @param adapter
	 *            the class to adapt to; must not be {@code null}
	 * @return the adapter, or {@code null} if no adapter is available
	 */
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (org.eclipse.jface.text.source.ISourceViewer.class == adapter
				|| org.eclipse.jface.text.ITextViewer.class == adapter) {
			GamaSourceViewer viewer = getInternalSourceViewer();
			if (viewer != null) return adapter.cast(viewer);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void doSave(final IProgressMonitor progressMonitor) {
		this.beforeSave();
		super.doSave(progressMonitor);
	}

	@Override
	public void doSaveAs() {
		this.beforeSave();
		super.doSaveAs();
	}

	/**
	 * Before save.
	 */
	private void beforeSave() {
		if (!GamaPreferences.Modeling.EDITOR_CLEAN_UP.getValue()) return;
		final SourceViewer sv = getInternalSourceViewer();
		final var p = sv.getSelectedRange();
		sv.setSelectedRange(0, sv.getDocument().getLength());
		if (sv.canDoOperation(ISourceViewer.FORMAT)) { sv.doOperation(ISourceViewer.FORMAT); }
		sv.setSelectedRange(p.x, p.y);
	}

	/**
	 * @return
	 */
	private StyledText getStyledText() { return (StyledText) super.getAdapter(Control.class); }

	/**
	 * Insert text.
	 *
	 * @param s
	 *            the s
	 */
	public void insertText(final String s) {
		final var selection = (ITextSelection) getSelectionProvider().getSelection();
		final var offset = selection.getOffset();
		final var length = selection.getLength();
		try {
			new ReplaceEdit(offset, length, s).apply(getDocument());
		} catch (final MalformedTreeException | BadLocationException e) {
			e.printStackTrace();
			return;
		}
		getSelectionProvider().setSelection(new TextSelection(getDocument(), offset + s.length(), 0));
	}

	/**
	 * Gets the selected text.
	 *
	 * @return the selected text
	 */
	public String getSelectedText() {
		final var sel = (ITextSelection) getSelectionProvider().getSelection();
		final var length = sel.getLength();
		if (length == 0) return "";
		final IDocument doc = getDocument();
		try {
			return doc.get(sel.getOffset(), length);
		} catch (final BadLocationException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * @see gaml.compiler.gaml.ui.editor.IGamlEditor#openEditTemplateDialog()
	 */
	public boolean openEditTemplateDialog(final TemplatePersistenceData data, final boolean edit) {
		final var d = getTemplateFactory().createDialog(data, edit, getEditorSite().getShell());
		if (d.open() == Window.OK) {
			getTemplateStore().directAdd(d.getData(), edit);
			return true;
		}
		return false;
	}

	/**
	 * @see gaml.compiler.gaml.ui.editor.IGamlEditor#getNewTemplateId(java.lang.String)
	 */
	public String getNewTemplateId(final String path) {
		return getTemplateStore().getNewIdFromId(path);
	}

	/**
	 * @see gaml.compiler.gaml.ui.editor.IGamlEditor#applyTemplate(org.eclipse.jface.text.templates.Template)
	 */

	public void applyTemplateAtTheEnd(final Template t) {

		try {
			final IDocument doc = getDocument();
			var offset = doc.getLength();
			doc.replace(offset, 0, "\n\n");
			offset += 2;
			final var length = 0;
			final var pos = new Position(offset, length);
			final var ct = new XtextTemplateContextType();
			final var dtc = new DocumentTemplateContext(ct, doc, pos);
			final IRegion r = new Region(offset, length);
			final var tp = new TemplateProposal(t, dtc, r, null);
			tp.apply(getInternalSourceViewer(), (char) 0, 0, offset);
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Apply template.
	 *
	 * @param t
	 *            the t
	 */
	public void applyTemplate(final Template t) {
		// TODO Create a specific context type (with GAML specific variables ??)
		final var ct = new XtextTemplateContextType();
		final IDocument doc = getDocument();
		final var selection = (ITextSelection) getSelectionProvider().getSelection();
		final var offset = selection.getOffset();
		final var length = selection.getLength();
		final var pos = new Position(offset, length);
		final var dtc = new DocumentTemplateContext(ct, doc, pos);
		final IRegion r = new Region(offset, length);
		final var tp = new TemplateProposal(t, dtc, r, null);
		tp.apply(getInternalSourceViewer(), (char) 0, 0, offset);
	}

	/**
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView#createToolItem(int,
	 *      gama.ui.shared.views.toolbar.GamaToolbar2)
	 */
	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		this.toolbar = tb;
		buildRightToolbar();
	}

	@Override
	protected void handlePreferenceStoreChanged(final PropertyChangeEvent event) {
		super.handlePreferenceStoreChanged(event);
		if (PREFERENCE_COLOR_BACKGROUND.equals(event.getProperty())) {
			final var bg = GamaColors.get(GamaPreferences.Modeling.EDITOR_BACKGROUND_COLOR.getValue()).color();
			this.getVerticalRuler().getControl().setBackground(bg);
			final Iterator e = ((CompositeRuler) getVerticalRuler()).getDecoratorIterator();
			while (e.hasNext()) {
				final var column = (IVerticalRulerColumn) e.next();
				column.getControl().setBackground(bg);
				column.redraw();
			}
		}
	}

	/**
	 * Do search.
	 */
	public void doSearch() {
		if (findControl.getFindControl().isFocusControl()) {
			findControl.findNext();
		} else {
			findControl.getFindControl().setFocus();
		}
	}

	@Override
	protected void initializeDragAndDrop(final ISourceViewer viewer) {
		GamaPreferences.Modeling.EDITOR_DRAG_RESOURCES.addChangeListener(dndChangedListener);
		super.initializeDragAndDrop(viewer);
	}

	@Override
	protected void installTextDragAndDrop(final ISourceViewer viewer) {
		dndHandler.install(!GamaPreferences.Modeling.EDITOR_DRAG_RESOURCES.getValue());
	}

	@Override
	protected void uninstallTextDragAndDrop(final ISourceViewer viewer) {
		dndHandler.uninstall();
	}

	/**
	 * Creates a composite ruler to be used as the vertical ruler by this editor. Subclasses may re-implement this
	 * method.
	 *
	 * @return the vertical ruler
	 */
	@Override
	protected CompositeRuler createCompositeRuler() {
		return new CompositeRuler(6);
	}

	/**
	 * @return
	 */
	public URI getURI() { return fileURI; }

	/**
	 *
	 */
	public void switchToDiagram() {
		if (diagramOpener != null) { diagramOpener.open(this); }
	}

	/**
	 * Switch to text.
	 */
	public void switchToText() {
		if (diagramOpener != null) { diagramOpener.close(this); }
	}

	/**
	 * @param generateDiagramHandler
	 */
	public static void setDiagramOpener(final IDiagramOpener opener) { diagramOpener = opener; }

	/**
	 * Zoom.
	 *
	 * @param magnification
	 *            the magnification
	 */
	public void zoom(final int magnification) {
		Font font = getStyledText().getFont();
		Font newFont;
		if (magnification != 0) {
			newFont = GamaFonts.withMagnification(font, magnification);
		} else {
			newFont = GamaFonts.getFont(GamaPreferences.Modeling.EDITOR_BASE_FONT.getValue());
		}
		setFont(getSourceViewer(), newFont);
	}

	/**
	 * Zoom out.
	 */
	public void zoomOut() {
		zoom(-1);
	}

	/**
	 * Zoom fit.
	 */
	public void zoomFit() {
		zoom(0);
	}

	/**
	 * Zoom in.
	 */
	public void zoomIn() {
		zoom(1);
	}

	/**
	 * Sets the font.
	 *
	 * @param sourceViewer
	 *            the source viewer
	 * @param font
	 *            the font
	 */
	private void setFont(final ISourceViewer sourceViewer, final Font font) {
		if (sourceViewer.getDocument() != null) {

			ISelectionProvider provider = sourceViewer.getSelectionProvider();
			ISelection selection = provider.getSelection();
			int topIndex = sourceViewer.getTopIndex();
			StyledText styledText = sourceViewer.getTextWidget();
			Control parent = styledText;
			if (sourceViewer instanceof ITextViewerExtension extension) { parent = extension.getControl(); }
			parent.setRedraw(false);
			styledText.setFont(font);
			if (getVerticalRuler() instanceof IVerticalRulerExtension) {
				IVerticalRulerExtension e = (IVerticalRulerExtension) getVerticalRuler();
				e.setFont(font);
			}
			provider.setSelection(selection);
			sourceViewer.setTopIndex(topIndex);
			if (parent instanceof Composite composite) { composite.layout(true); }
			parent.setRedraw(true);
		} else {
			StyledText styledText = sourceViewer.getTextWidget();
			styledText.setFont(font);
			if (getVerticalRuler() instanceof IVerticalRulerExtension e) { e.setFont(font); }
		}
	}

	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		for (IContributionItem item : menu.getItems()) {
			if (item == null) { continue; }
			if (item instanceof MenuManager mm) { DEBUG.OUT(" --> \"" + mm.getMenuText() + "\""); }
			if (item instanceof MenuManager mmmm && TO_REMOVE_FROM_MENUS.contains(mmmm.getMenuText())
					|| item.getId() != null && TO_REMOVE_FROM_MENUS.contains(item.getId())) {
				menu.remove(item);
				continue;
			}
			// DEBUG.OUT("Item " + item);
			UICleanupTasks.RearrangeMenus.changeIcon(menu, item, item.getId());
		}
		menu.add(new Separator());
		menu.add(new Action("Extract Action\u2026") {
			@Override
			public void run() {
				ExtractActionHandler.extractAction(GamlEditor.this);
			}

			@Override
			public boolean isEnabled() {
				final ITextSelection sel = (ITextSelection) getSelectionProvider().getSelection();
				return sel != null && sel.getLength() > 0;
			}
		});
		menu.add(new Separator());
		menu.add(new InternalMenuManager(parent -> new TemplateReferenceMenu().installSubMenuIn(parent)));
		menu.add(new InternalMenuManager(parent -> new BuiltinReferenceMenu().installSubMenuIn(parent)));
		menu.add(new InternalMenuManager(parent -> new OperatorsReferenceMenu().installSubMenuIn(parent)));
		menu.add(new InternalMenuManager(parent -> new ColorReferenceMenu().installSubMenuIn(parent)));

	}

	/**
	 * The Class InternalMenuManager.
	 */
	class InternalMenuManager extends MenuManager {

		/** The fill. */
		final Consumer<Menu> fill;

		/**
		 * Instantiates a new internal menu manager.
		 */
		InternalMenuManager(final Consumer<Menu> fill) {
			this.fill = fill;
			setRemoveAllWhenShown(true);
			setVisible(true);
		}

		@Override
		public void fill(final Menu parent, final int index) {
			fill.accept(parent);
		}

	}

	/**
	 * @return
	 */
	public void updateToolbar() {
		updateToolbar(state, true);
	}

	/**
	 * Configures the annotation decoration support for the source viewer in order to simplify the "info" markers:
	 * remove the line below the text by default and their presence in the "overview" vertical ruler on the right
	 */
	@Override
	protected void configureSourceViewerDecorationSupport(final SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);

		for (String infoType : ANNOTATION_INFO_TYPES) {
			// Create a preference object to define all properties at once
			AnnotationPreference infoPref = new AnnotationPreference();
			infoPref.setAnnotationType(infoType);

			// 1. Color Preference (Value type: String RGB "r,g,b")
			infoPref.setColorPreferenceKey("gama.info.color");
			infoPref.setColorPreferenceValue(new RGB(0, 120, 215)); // Default value
			// 2. Text Decoration (Value type: boolean)
			// Controls if the annotation is painted in the text editor
			infoPref.setTextPreferenceKey("gaml.info.text");
			infoPref.setTextPreferenceValue(false); // We do not paint it by default

			// 3. Visual Style (Value type: String)
			// Valid values: "SQUIGGLES", "BOX", "DASHED_BOX", "UNDERLINE", "HIGHLIGHTER"
			infoPref.setTextStylePreferenceKey("gaml.info.style");
			infoPref.setTextStyleValue(AnnotationPreference.STYLE_UNDERLINE);

			// 4. Overview Ruler (Value type: boolean)
			// Right side bar visibility
			infoPref.setOverviewRulerPreferenceKey("gaml.info.overview");
			infoPref.setOverviewRulerPreferenceValue(false);

			// 5. Vertical Ruler (Value type: boolean)
			// Left side bar visibility (icons)
			infoPref.setVerticalRulerPreferenceKey("gaml.info.vertical");
			infoPref.setVerticalRulerPreferenceValue(true);

			// Register this preference object into the support
			support.setAnnotationPreference(infoPref);
		}
	}

}
