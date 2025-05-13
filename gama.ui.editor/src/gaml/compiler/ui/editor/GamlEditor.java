/*******************************************************************************************************
 *
 * GamlEditor.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.SurroundWithBracketsStrategy;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningPreferenceConstants;
import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningProvider;
import org.eclipse.ui.internal.texteditor.LineNumberColumn;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.xtext.ui.XtextUIMessages;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;
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

import gama.annotations.precompiler.GamlProperties;
import gama.core.common.GamlFileExtension;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.preferences.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.DEBUG;
import gama.dev.FLAGS;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.ValidationContext;
import gama.gaml.operators.Strings;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.interfaces.IModelRunner;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaFonts;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.CleanupHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.validation.IGamlBuilderListener;
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
import gaml.compiler.ui.templates.GamlEditTemplateDialogFactory;
import gaml.compiler.ui.templates.GamlTemplateStore;

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

	/** The images. */
	static Map<String, Image> images = new HashMap();

	/** The menu images. */
	static Map<String, Image> menu_images = new HashMap();

	/** The button padding. How much space between each experiment button */
	static int buttonPadding = 4;
	static {
		final var store = EditorsUI.getPreferenceStore();
		store.setDefault(AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR, false);
		store.setDefault("spellingEnabled", false);
		store.setValue("spellingEnabled", false);
		images.put(IKeyword.BATCH, GamaIcon.named(IGamaIcons.BUTTON_BATCH).image());
		images.put(IKeyword.RECORD, GamaIcon.named(IGamaIcons.BUTTON_BACK).image());
		images.put("regular", GamaIcon.named(IGamaIcons.BUTTON_GUI).image());
		menu_images.put(IKeyword.BATCH,
				GamaIcon.named(ThemeHelper.isDark() ? IGamaIcons.BUTTON_BATCH : IGamaIcons.MENU_BATCH).image());
		menu_images.put(IKeyword.RECORD,
				GamaIcon.named(ThemeHelper.isDark() ? IGamaIcons.BUTTON_BACK : IGamaIcons.MENU_BACK).image());
		menu_images.put("regular",
				GamaIcon.named(ThemeHelper.isDark() ? IGamaIcons.BUTTON_GUI : IGamaIcons.MENU_GUI).image());

		images.put("new", GamaIcon.named(IGamaIcons.ADD_EXPERIMENT).image());
		// for (Image im : images.values()) { maxImageHeight = Math.max(maxImageHeight, im.getBounds().height); }
	}

	/**
	 * Instantiates a new gaml editor.
	 */
	public GamlEditor() {
		dndHandler = new GamlEditorDragAndDropHandler(this);
	}

	/** The state. */
	GamlEditorState state = new GamlEditorState(null, Collections.EMPTY_LIST);

	/** The toolbar. */
	GamaToolbar2 toolbar;

	/** The toolbar parent. */
	Composite toolbarParent;

	/** The find control. */
	private EditorSearchControls findControl;

	/** The decoration enabled. */
	// boolean decorationEnabled = GamaPreferences.Modeling.EDITBOX_ENABLED.getValue();
	// boolean editToolbarEnabled = AutoStartup.EDITOR_SHOW_TOOLBAR.getValue();

	/** The resource set provider. */
	@Inject public IResourceSetProvider resourceSetProvider;

	/** The injector. */
	@Inject Injector injector;

	/** The runner. */
	@Inject IModelRunner runner;

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
		toolbar.wipe(SWT.LEFT, false);

		final var t = toolbar.button(IGamaColors.NEUTRAL, "Waiting...", GamaIcon.named(IGamaIcons.STATUS_CLOCK).image(),
				null, SWT.LEFT);
		toolbar.sep(4, SWT.LEFT);
		findControl = new EditorToolbar(this).fill(toolbar.getToolbar(SWT.RIGHT));
		fakeButton = FlatButton.button(toolbar.getToolbar(SWT.LEFT), IGamaColors.OK, "", images.get(IKeyword.BATCH));
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
			// folder.setTabHeight(16);
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
					// actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), getAction(CUT));
					// actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), getAction(COPY));
					// actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), getAction(PASTE));
					// actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), getAction(SELECT_ALL));
					actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), getAction(FIND));
					actionBars.updateActionBars();
				}
			}
		});
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
	 * Schedule validation job.
	 */
	private void scheduleValidationJob() {
		// if (!isEditable()) return;
		final IValidationIssueProcessor processor = new MarkerIssueProcessor(getResource(),
				getInternalSourceViewer().getAnnotationModel(), markerCreator, markerTypeProvider);
		final ValidationJob validate = new ValidationJob(validator, getDocument(), processor, CheckMode.FAST_ONLY) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final var issues = getDocument().readOnly(resource -> {
					if (resource.isValidationDisabled()) return Collections.emptyList();
					return validator.validate(resource, getCheckMode(), null);
				});
				processor.processIssues((List<Issue>) issues, monitor);
				return Status.OK_STATUS;
			}

		};
		validate.schedule();

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

	@Override
	protected void handleCursorPositionChanged() {
		if (getSelectionProvider() == null || getInternalSourceViewer() == null
				|| getInternalSourceViewer().getControl() == null
				|| getInternalSourceViewer().getControl().isDisposed())
			return;
		super.handleCursorPositionChanged();
		this.markInNavigationHistory();
	}

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
				boolean showExperiments =
						!GamlFileExtension.isExperiment(getDocument().getAdapter(IFile.class).getName())
								&& newState.showExperiments;
				toolbar.wipe(SWT.LEFT, true);
				if (showExperiments) {
					toolbar.getToolbar(SWT.LEFT).button("editor/add.experiment", null, "Add an experiment to the model",
							new CreateExperimentSelectionListener(GamlEditor.this, toolbar.getToolbar(SWT.LEFT)));
					toolbar.getToolbar(SWT.LEFT).space(8);
				}

				final var c = state.getColor();
				var msg = state.getStatus();

				Selector listener = null;
				String imageName = null;

				if (GamlEditorState.NO_EXP_DEFINED.equals(msg)) {
					msg = null;
				} else if (newState.hasImportedErrors) {
					listener = new OpenImportedErrorSelectionListener(GamlEditor.this, newState,
							toolbar.getToolbar(SWT.LEFT));
					imageName = IGamaIcons.SMALL_DROPDOWN;
				} else if (msg != null) {
					listener = new RevalidateModelSelectionListener(GamlEditor.this);
					imageName = IGamaIcons.MARKER_ERROR;
				} else {
					listener = new OpenExperimentSelectionListener(GamlEditor.this, newState, runner);
				}
				if (msg != null) {
					toolbar.button(c, msg, GamaIcon.named(imageName).image(), listener, SWT.LEFT);
				} else if (newState.showExperiments) {
					if (GamaPreferences.Modeling.EDITOR_EXPERIMENT_MENU.getValue()) {
						displayExperimentMenu(newState, listener);
					} else if (newState.abbreviations.size() <= 1
							|| !GamaPreferences.Modeling.EDITOR_COLLAPSE_BUTTONS.getValue()) {
						displayExperimentButtons(newState, listener);
					} else {
						int width = computeWidth(newState);
						if (width > toolbar.getSize().x - toolbar.getToolbar(SWT.RIGHT).getSize().x) {
							displayExperimentMenu(newState, listener);
						} else {
							displayExperimentButtons(newState, listener);
						}
					}
				}
				toolbar.requestLayout();

			});
		}

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
		FlatButton t;
		for (final String text : newState.abbreviations) {
			if (text == null) { continue; }
			fakeButton.setText(text);
			width += fakeButton.computeSize(SWT.DEFAULT, 12).x + 2 * buttonPadding;
		}
		fakeButton.setText("Add Experiment");
		width += fakeButton.computeSize(SWT.DEFAULT, 12).x + 2 * buttonPadding;
		return width;
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
			final var image = images.get(type);
			final var t = toolbar.button(IGamaColors.OK, text, image, SWT.LEFT);
			// t.setWidth(t.getWidth() + buttonPadding);
			final FlatButton b = (FlatButton) t.getControl();
			b.setRightPadding(buttonPadding);
			// b.setImageHeight(maxImageHeight);
			b.setToolTipText("Executes the " + type + " experiment " + text);
			b.addSelectionListener(listener);
			t.setData("index", index);
			b.setData("exp", text);
		}
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

		final var menu = toolbar.menu(IGamaColors.OK, "Run Experiment...", SWT.LEFT);
		final FlatButton b = (FlatButton) menu.getControl();
		b.setRightPadding(buttonPadding);

		((FlatButton) menu.getControl()).addSelectionListener(new SelectionAdapter() {

			Menu menu;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (menu == null) {
					menu = new Menu(toolbar.getShell(), SWT.POP_UP);
					fillMenu();
				}
				final Point point = toolbar.toDisplay(new Point(e.x, e.y + toolbar.getSize().y));
				menu.setLocation(point.x, point.y);
				menu.setVisible(true);
			}

			private void fillMenu() {
				var index = 0;
				for (final String text : state.abbreviations) {
					if (text == null) return;
					final var expType = state.types.get(index++);
					final String type = IKeyword.BATCH.equals(expType) ? IKeyword.BATCH
							: IKeyword.RECORD.equals(expType) ? IKeyword.RECORD : "regular";
					final Image image = menu_images.get(type);
					GamaMenu.action(menu, text, listener, image).setData("exp", text);
				}
			}
		});
	}

	@Override
	public void validationEnded(final ModelDescription model, final Iterable<? extends IDescription> newExperiments,
			final ValidationContext status) {

		getInternalSourceViewer().updateCodeMinings();

		if (GamaPreferences.Experimental.REQUIRED_PLUGINS.getValue() && model != null && !status.hasErrors()) {
			String requires = "@" + IKeyword.PRAGMA_REQUIRES;
			GamlProperties meta = new GamlProperties();
			model.collectMetaInformation(meta);
			String newLine = requires + " " + meta.get(GamlProperties.PLUGINS);
			GamaSourceViewer viewer = getInternalSourceViewer();
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
						new InsertEdit(0, Strings.LN + newLine + Strings.LN + Strings.LN).apply(getDocument());
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
	 * The Class GamaSourceViewerConfiguration.
	 */
	public static class GamaSourceViewerConfiguration extends XtextSourceViewerConfiguration {

		@Override
		public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
			return super.getTextHover(sourceViewer, contentType);
		}

		// See issue #391 : automatically surrounds the selected words with a pair of "brackets"
		@Override
		public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
			IAutoEditStrategy[] strategies = super.getAutoEditStrategies(sourceViewer, contentType);
			if (!GamaPreferences.Modeling.CORE_SURROUND_SELECTED.getValue()) return strategies;
			for (IAutoEditStrategy strategy : strategies)
				if (strategy instanceof SurroundWithBracketsStrategy) return strategies;
			return ArrayUtils.insert(0, strategies, new SurroundWithBracketsStrategy(sourceViewer));
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
	 * Do nothing (already installed normally)
	 *
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#installCodeMiningProviders()
	 */
	@Override
	protected void installCodeMiningProviders() {
		ICodeMiningProvider[] providers = { new GamlCodeMiningProvider() };
		((GamaSourceViewer) getSourceViewer()).setCodeMiningProviders(providers);
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
			var offset = doc.getLineOffset(doc.getNumberOfLines() - 1);
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
		final LineNumberColumn c;
		super.handlePreferenceStoreChanged(event);
		if (PREFERENCE_COLOR_BACKGROUND.equals(event.getProperty())) {
			// this.fSourceViewerDecorationSupport.updateOverviewDecorations();

			this.getVerticalRuler().getControl()
					.setBackground(GamaColors.get(GamaPreferences.Modeling.EDITOR_BACKGROUND_COLOR.getValue()).color());

			final Iterator e = ((CompositeRuler) getVerticalRuler()).getDecoratorIterator();
			while (e.hasNext()) {
				final var column = (IVerticalRulerColumn) e.next();
				column.getControl().setBackground(
						GamaColors.get(GamaPreferences.Modeling.EDITOR_BACKGROUND_COLOR.getValue()).color());
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

	/** The to remove. */
	Set<String> toRemove = Set.of("revert", "save", "Preferences.ContextAction", "QuickAssist", "Open W&ith",
			"Sho&w In	⌥⌘W", "group.open");

	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		for (IContributionItem item : menu.getItems()) {
			if (item == null) { continue; }
			if (item instanceof MenuManager mm) { DEBUG.OUT(" --> \"" + mm.getMenuText() + "\""); }
			if (item instanceof MenuManager mmmm && toRemove.contains(mmmm.getMenuText())
					|| item.getId() != null && toRemove.contains(item.getId())) {
				menu.remove(item);
				continue;
			}
			DEBUG.OUT("Item " + item);
			CleanupHelper.RearrangeMenus.changeIcon(menu, item, item.getId());
		}
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

}
