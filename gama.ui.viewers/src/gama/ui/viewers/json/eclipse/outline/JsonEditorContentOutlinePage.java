/*******************************************************************************************************
 *
 * JsonEditorContentOutlinePage.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.outline;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.viewers.json.eclipse.JsonEditor;
import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.script.JsonModel;

/**
 * The Class JsonEditorContentOutlinePage.
 */
public class JsonEditorContentOutlinePage extends ContentOutlinePage implements IDoubleClickListener {

	/** The img desc linked. */
	/* @formatter:on */
	private static ImageDescriptor IMG_DESC_LINKED = GamaIcon.named("navigator/editor.link").descriptor();

	/** The img desc not linked. */
	private static ImageDescriptor IMG_DESC_NOT_LINKED = GamaIcon.named("navigator/editor.link").disabledDescriptor();

	/** The img desc outline enabled. */
	private static ImageDescriptor IMG_DESC_OUTLINE_ENABLED = GamaIcon.named("editor/command.outline").descriptor();

	/** The img desc outline disabled. */
	private static ImageDescriptor IMG_DESC_OUTLINE_DISABLED =
			GamaIcon.named("editor/command.outline").disabledDescriptor();

	/** The img desc copy fullpath to clipboard. */
	private static ImageDescriptor IMG_DESC_COPY_FULLPATH_TO_CLIPBOARD =
			GamaIcon.named("generic/menu.copy").descriptor();

	/** The img desc expand all. */
	private static ImageDescriptor IMG_DESC_EXPAND_ALL = GamaIcon.named("toolbar/bar.expand").descriptor();

	/** The img desc collapse all. */
	private static ImageDescriptor IMG_DESC_COLLAPSE_ALL = GamaIcon.named("toolbar/bar.collapse").descriptor();

	/* @formatter:off */

	/** The content provider. */
	private final JsonEditorTreeContentProvider contentProvider;

	/** The input. */
	private Object input;

	/** The editor. */
	private final JsonEditor editor;

	/** The label provider. */
	private JsonEditorOutlineLabelProvider labelProvider;

	/** The selection. */
	private ISelection selection;

	/** The linking with editor enabled. */
	private boolean linkingWithEditorEnabled;

	/** The ignore next selection events. */
	private boolean ignoreNextSelectionEvents;

	/** The toggle linking action. */
	private ToggleLinkingAction toggleLinkingAction;

	/** The toggle enable outline action. */
	private ToggleEnableOutlineAction toggleEnableOutlineAction;

	/**
	 * Instantiates a new highspeed JSON editor content outline page.
	 *
	 * @param editor
	 *            the editor
	 */
	public JsonEditorContentOutlinePage(final JsonEditor editor) {
		this.editor = editor;
		this.contentProvider = new JsonEditorTreeContentProvider();
	}

	/**
	 * Gets the content provider.
	 *
	 * @return the content provider
	 */
	public JsonEditorTreeContentProvider getContentProvider() { return contentProvider; }

	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);

		labelProvider = new JsonEditorOutlineLabelProvider();

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(contentProvider);
		viewer.addDoubleClickListener(this);
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(labelProvider));
		viewer.addSelectionChangedListener(this);

		/* it can happen that input is already updated before control created */
		if (input != null) { viewer.setInput(input); }
		toggleLinkingAction = new ToggleLinkingAction();
		toggleLinkingAction.setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);

		toggleEnableOutlineAction = new ToggleEnableOutlineAction();

		CopyFullKeyPathToClipboardAction copyFullKeyPathToClipboardAction = new CopyFullKeyPathToClipboardAction();

		ExpandAllAction expandAllAction = new ExpandAllAction();
		CollapseAllAction collapseAllAction = new CollapseAllAction();

		ExpandSelectionAction expandSelectionAction = new ExpandSelectionAction();
		CollapseSelectionAction collapseSelectionAction = new CollapseSelectionAction();

		MenuManager menuMgr = new MenuManager();
		menuMgr.add(expandSelectionAction);
		menuMgr.add(collapseSelectionAction);
		menuMgr.add(new Separator("clipboardGroup"));
		menuMgr.add(copyFullKeyPathToClipboardAction);

		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getControl().setMenu(menu);

		IActionBars actionBars = getSite().getActionBars();

		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(toggleEnableOutlineAction);
		toolBarManager.add(toggleLinkingAction);
		toolBarManager.add(copyFullKeyPathToClipboardAction);

		IMenuManager viewMenuManager = actionBars.getMenuManager();
		viewMenuManager.add(new Separator("EndFilterGroup")); //$NON-NLS-1$

		viewMenuManager.add(new Separator("treeGroup")); //$NON-NLS-1$
		viewMenuManager.add(toggleEnableOutlineAction);
		viewMenuManager.add(toggleLinkingAction);
		viewMenuManager.add(expandAllAction);
		viewMenuManager.add(collapseAllAction);
		viewMenuManager.add(new Separator("treeSelectionGroup")); //$NON-NLS-1$
		viewMenuManager.add(expandSelectionAction);
		viewMenuManager.add(collapseSelectionAction);
		viewMenuManager.add(new Separator("clipboardGroup")); //$NON-NLS-1$
		viewMenuManager.add(copyFullKeyPathToClipboardAction);

		/*
		 * when no input is set on init state - let the editor rebuild outline (async)
		 */
		if (input == null && editor != null) { editor.rebuildOutlineAndOrValidate(); }

	}

	/**
	 * Checks if is outline build enabled.
	 *
	 * @return true, if is outline build enabled
	 */
	public boolean isOutlineBuildEnabled() { return contentProvider.outlineEnabled; }

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		if (editor == null) return;
		if (linkingWithEditorEnabled) { editor.setFocus(); }
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		super.selectionChanged(event);

		selection = event.getSelection();

		if (!linkingWithEditorEnabled || ignoreNextSelectionEvents) return;
		editor.openSelectedTreeItemInEditor(selection, false);
	}

	/**
	 * On editor caret moved.
	 *
	 * @param caretOffset
	 *            the caret offset
	 */
	public void onEditorCaretMoved(final int caretOffset) {
		if (!linkingWithEditorEnabled) return;
		ignoreNextSelectionEvents = true;

		JsonEditorTreeContentProvider gcp = contentProvider;
		Item item = gcp.tryToFindByOffset(caretOffset);
		if (item != null) {
			StructuredSelection selection = new StructuredSelection(item);
			getTreeViewer().setSelection(selection, true);
		}
		ignoreNextSelectionEvents = false;
	}

	/**
	 * Rebuild.
	 *
	 * @param model
	 *            the model
	 */
	public void rebuild(final JsonModel model) {
		contentProvider.rebuildTree(model);

		TreeViewer treeViewer = getTreeViewer();
		if (treeViewer != null) {
			Control control = treeViewer.getControl();
			if (control == null || control.isDisposed()) return;
			treeViewer.setInput(model);
		}
	}

	/**
	 * Gets the first selected element.
	 *
	 * @return the first selected element
	 */
	private Object getFirstSelectedElement() {
		if (!(selection instanceof IStructuredSelection ss)) return null;
		Object element = ss.getFirstElement();
		return element;
	}

	/**
	 * The Class CollapseAllAction.
	 */
	class CollapseAllAction extends Action {

		/**
		 * Instantiates a new collapse all action.
		 */
		private CollapseAllAction() {
			setImageDescriptor(IMG_DESC_COLLAPSE_ALL);
			setText("Collapse all");
		}

		@Override
		public void run() {
			getTreeViewer().collapseAll();
		}
	}

	/**
	 * The Class ExpandAllAction.
	 */
	class ExpandAllAction extends Action {

		/**
		 * Instantiates a new expand all action.
		 */
		private ExpandAllAction() {
			setImageDescriptor(IMG_DESC_EXPAND_ALL);
			setText("Expand all");
		}

		@Override
		public void run() {
			getTreeViewer().expandAll();
		}
	}

	/**
	 * The Class ExpandSelectionAction.
	 */
	class ExpandSelectionAction extends Action {

		/**
		 * Instantiates a new expand selection action.
		 */
		private ExpandSelectionAction() {
			setImageDescriptor(IMG_DESC_EXPAND_ALL);
			setText("Expand children");
		}

		@Override
		public void run() {
			Object element = getFirstSelectedElement();
			if (element == null) return;
			getTreeViewer().expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
		}
	}

	/**
	 * The Class CollapseSelectionAction.
	 */
	class CollapseSelectionAction extends Action {

		/**
		 * Instantiates a new collapse selection action.
		 */
		private CollapseSelectionAction() {
			setImageDescriptor(IMG_DESC_COLLAPSE_ALL);
			setText("Collapse children");
		}

		@Override
		public void run() {
			Object element = getFirstSelectedElement();
			if (element == null) return;
			getTreeViewer().collapseToLevel(element, AbstractTreeViewer.ALL_LEVELS);
		}

	}

	/**
	 * The Class CopyFullKeyPathToClipboardAction.
	 */
	class CopyFullKeyPathToClipboardAction extends Action {

		/**
		 * Instantiates a new copy full key path to clipboard action.
		 */
		private CopyFullKeyPathToClipboardAction() {
			setImageDescriptor(IMG_DESC_COPY_FULLPATH_TO_CLIPBOARD);
			setText("Copy qualified key to clipboard");
			setToolTipText("Copy qualified key to clipboard.\n"
					+ "Only the selected node and its parents are checked. Select the deepest key node to copy the full path.");
		}

		@Override
		public void run() {
			Object element = getFirstSelectedElement();
			if (element instanceof Item item) {
				String keyFullPath = getContentProvider().createFullPath(item);

				StringSelection selection = new StringSelection(keyFullPath);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);

			}
		}

	}

	/**
	 * The Class ToggleEnableOutlineAction.
	 */
	class ToggleEnableOutlineAction extends Action {

		/**
		 * Instantiates a new toggle enable outline action.
		 */
		private ToggleEnableOutlineAction() {
			if (editor != null) { contentProvider.outlineEnabled = editor.getPreferences().isOutlineBuildEnabled(); }
			setDescription("enable/disable outline model build");
			initImage();
			initText();
		}

		@Override
		public void run() {
			contentProvider.outlineEnabled = !contentProvider.outlineEnabled;

			initText();
			initImage();

			editor.rebuildOutlineAndOrValidate();
		}

		/**
		 * Inits the image.
		 */
		private void initImage() {
			setImageDescriptor(contentProvider.outlineEnabled ? getImageDescriptionForOutlineEnabled()
					: getImageDescriptionForOutlineDisabled());
		}

		/**
		 * Inits the text.
		 */
		private void initText() {
			setText(contentProvider.outlineEnabled ? "Click to disable outline creation"
					: "Click to enable outline creation");
		}

	}

	/**
	 * The Class ToggleLinkingAction.
	 */
	class ToggleLinkingAction extends Action {

		/**
		 * Instantiates a new toggle linking action.
		 */
		private ToggleLinkingAction() {
			if (editor != null) { linkingWithEditorEnabled = editor.getPreferences().isLinkOutlineWithEditorEnabled(); }
			setDescription("link with editor");
			initImage();
			initText();
		}

		@Override
		public void run() {
			linkingWithEditorEnabled = !linkingWithEditorEnabled;

			initText();
			initImage();
		}

		/**
		 * Inits the image.
		 */
		private void initImage() {
			setImageDescriptor(
					linkingWithEditorEnabled ? getImageDescriptionForLinked() : getImageDescriptionNotLinked());
		}

		/**
		 * Inits the text.
		 */
		private void initText() {
			setText(linkingWithEditorEnabled ? "Click to unlink from editor" : "Click to link with editor");
		}

	}

	/**
	 * Gets the image description for linked.
	 *
	 * @return the image description for linked
	 */
	protected ImageDescriptor getImageDescriptionForLinked() { return IMG_DESC_LINKED; }

	/**
	 * Gets the image description for outline disabled.
	 *
	 * @return the image description for outline disabled
	 */
	public ImageDescriptor getImageDescriptionForOutlineDisabled() { return IMG_DESC_OUTLINE_DISABLED; }

	/**
	 * Gets the image description for outline enabled.
	 *
	 * @return the image description for outline enabled
	 */
	public ImageDescriptor getImageDescriptionForOutlineEnabled() { return IMG_DESC_OUTLINE_ENABLED; }

	/**
	 * Gets the image description not linked.
	 *
	 * @return the image description not linked
	 */
	protected ImageDescriptor getImageDescriptionNotLinked() { return IMG_DESC_NOT_LINKED; }

}
