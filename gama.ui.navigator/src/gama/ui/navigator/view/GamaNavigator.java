/*******************************************************************************************************
 *
 * GamaNavigator.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import static gama.ui.navigator.view.contents.NavigatorRoot.getInstance;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.navigator.CommonNavigatorActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonNavigatorManager;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;

import gama.api.GAMA;
import gama.dev.DEBUG;
import gama.ui.navigator.view.contents.NavigatorRoot;
import gama.ui.navigator.view.contents.Tag;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.view.contents.VirtualContent;
import gama.ui.navigator.view.contents.WrappedContainer;
import gama.ui.navigator.view.contents.WrappedFile;
import gama.ui.navigator.view.contents.WrappedResource;
import gama.ui.navigator.view.contents.WrappedSyntacticContent;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.PreferencesHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;

/**
 * The Class GamaNavigator.
 */
public class GamaNavigator extends CommonNavigator
		implements ISelectionChangedListener, IToolbarDecoratedView.Expandable {

	/** The link item. */
	ToolItem linkItem;

	/** The parent. */
	protected Composite parent;

	/** The toolbar. */
	protected GamaToolbar2 toolbar;

	/** The find control. */
	private NavigatorSearchControl findControl;

	/** Key under which the expanded elements are stored (in the memento and in the workspace preferences). */
	private static final String EXPANDED_STATE = "EXPANDED_STATE";

	/** Separator of the expanded elements in their serialized form. */
	private static final String SEPARATOR = "@@";

	/** Prefix of the serialized user groups, followed by the name of their top level folder, '#', and their path. */
	private static final String GROUP_PREFIX = "#";

	/** Saves the expanded state shortly after the user has expanded or collapsed elements. */
	private final UIJob saveExpandedStateJob = new UIJob("Saving navigator state") {

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			saveExpandedState();
			return Status.OK_STATUS;
		}
	};

	/** Reveals the file of the active editor once the editors and the navigator have settled. */
	private final UIJob syncWithEditorJob = new UIJob("Synchronizing navigator with editor") {

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			syncWithActiveEditor();
			return Status.OK_STATUS;
		}
	};

	/** Triggers the synchronization whenever an editor is activated, brought to top or changes its input. */
	private final IPartListener2 editorListener = new IPartListener2() {

		@Override
		public void partActivated(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) { scheduleSyncWithEditor(); }
		}

		@Override
		public void partBroughtToTop(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) { scheduleSyncWithEditor(); }
		}

		@Override
		public void partInputChanged(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) { scheduleSyncWithEditor(); }
		}
	};

	{
		saveExpandedStateJob.setSystem(true);
		syncWithEditorJob.setSystem(true);
	}

	/**
	 * Schedules the synchronization of the navigator with the active editor, if linking is enabled.
	 */
	private void scheduleSyncWithEditor() {
		syncAttempts = 0;
		if (isLinkingEnabled()) { syncWithEditorJob.schedule(200); }
	}

	/** Number of attempts made to reveal a file that the navigator does not know yet (e.g. at startup). */
	private int syncAttempts;

	/**
	 * Retries the synchronization later, as the projects may not be all available in the navigator yet.
	 */
	private void retrySyncWithEditor() {
		if (syncAttempts++ < 10) { syncWithEditorJob.schedule(1000); }
	}

	/**
	 * Selects and reveals, in the navigator, the file edited in the active editor. Does not rely on the editor
	 * activation events used by Eclipse, which are usually sent before the navigator exists when the workbench
	 * restores its editors.
	 */
	private void syncWithActiveEditor() {
		if (!isLinkingEnabled()) return;
		final NavigatorCommonViewer viewer = getCommonViewer();
		if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) return;
		final IWorkbenchPage page = getSite().getPage();
		final IEditorPart editor = page == null ? null : page.getActiveEditor();
		if (editor == null) {
			retrySyncWithEditor();
			return;
		}
		final IFile file = ResourceUtil.getFile(editor.getEditorInput());
		if (file == null) return;
		// Makes sure the virtual folders exist, so that the wrappers of projects are attached to them
		if (NavigatorRoot.getInstance() != null) { NavigatorRoot.getInstance().getFolders(); }
		final WrappedResource<?, ?> wrapped = NavigatorRoot.getInstance() == null ? null
				: NavigatorRoot.getInstance().getManager().resolveWrappedInstanceOf(file);
		if (wrapped == null) {
			retrySyncWithEditor();
			return;
		}
		final Object current = viewer.getStructuredSelection().getFirstElement();
		// Already selected, or a model element of the file is selected
		if (current == wrapped || current instanceof WrappedSyntacticContent s && s.isContainedIn(wrapped)) return;
		final List<Object> ancestors = new ArrayList<>();
		for (Object p = wrapped.getParent(); p != null && p != NavigatorRoot.getInstance(); p =
				((VirtualContent<?>) p).getParent()) {
			ancestors.add(0, p);
		}
		// Use the top level folder actually displayed, in case the virtual folders have been recreated since
		if (!ancestors.isEmpty() && ancestors.get(0) instanceof TopLevelFolder top) {
			for (final TreeItem item : viewer.getTree().getItems()) {
				if (item.getData() instanceof TopLevelFolder t && t.getName().equals(top.getName())) {
					ancestors.set(0, t);
					break;
				}
			}
		}
		for (final Object ancestor : ancestors) { viewer.setExpandedState(ancestor, true); }
		viewer.setSelection(new StructuredSelection(wrapped), true);
		viewer.reveal(wrapped);
	}

	/**
	 * The memento is only written when the workbench shuts down cleanly and Eclipse disables linking when it is absent:
	 * the linking state is therefore kept in the GAMA preferences instead.
	 */
	@Override
	public void init(final IViewSite site, final IMemento aMemento) throws PartInitException {
		super.init(site, aMemento);
		setLinkingEnabled(PreferencesHelper.NAVIGATOR_LINK_WITH_EDITOR.getValue());
	}

	/**
	 * Overriden just to provide an opportunity to hook the navigator to the selection changed events
	 */
	@Override
	protected CommonNavigatorManager createCommonManager() {
		final CommonNavigatorManager manager = new CommonNavigatorManager(this, memento);
		getCommonViewer().addPostSelectionChangedListener(this);
		return manager;
	}

	@Override
	protected ActionGroup createCommonActionGroup() {

		return new CommonNavigatorActionGroup(this, getCommonViewer(), getLinkHelperService()) {

			/**
			 * To empty the toolbar
			 */
			@Override
			protected void fillToolBar(final IToolBarManager toolBar) {
				toolBar.removeAll();
			}

			/**
			 * To empty the little menu on top of the view
			 */
			@Override
			protected void fillViewMenu(final IMenuManager menu) {
				menu.removeAll();
			}

		};
	}

	@Override
	public void createPartControl(final Composite compo) {
		this.parent = GamaToolbarFactory.createToolbars(this, compo);

		super.createPartControl(parent);
		restoreState();
		final GamaCommand linkCommand = new GamaCommand(IGamaIcons.EDITOR_LINK, "", "Stay in sync with the editor",
				e -> setLinkingEnabled(!isLinkingEnabled()));
		linkItem = toolbar.check(linkCommand, SWT.RIGHT);
		linkItem.setSelection(isLinkingEnabled());
		addPropertyListener((source, id) -> {
			if (id != IS_LINKING_ENABLED_PROPERTY) return;
			final boolean linked = isLinkingEnabled();
			if (linkItem != null && !linkItem.isDisposed()) { linkItem.setSelection(linked); }
			if (PreferencesHelper.NAVIGATOR_LINK_WITH_EDITOR.getValue() != linked) {
				PreferencesHelper.NAVIGATOR_LINK_WITH_EDITOR.set(linked).save();
			}
			if (linked) { scheduleSyncWithEditor(); }
		});
		getSite().getPage().addPartListener(editorListener);
		getCommonViewer().addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				saveExpandedStateJob.schedule(500);
				getCommonViewer().revealExpandedChildren(event.getElement());
			}

			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				saveExpandedStateJob.schedule(500);
			}
		});

		GamaToolbarSimple tbs = toolbar.getToolbar(SWT.RIGHT);

		tbs.button("editor/local.menu", "More...", "More options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					separate(mainMenu, "Show:");
					check("Metadata", PreferencesHelper.NAVIGATOR_METADATA,
							event -> PreferencesHelper.NAVIGATOR_METADATA.save());
					check("Hidden files", PreferencesHelper.NAVIGATOR_HIDDEN,
							event -> PreferencesHelper.NAVIGATOR_HIDDEN.save());
					check("Model elements", PreferencesHelper.NAVIGATOR_OUTLINE,
							event -> PreferencesHelper.NAVIGATOR_OUTLINE.save());
					check("Plugin", PreferencesHelper.NAVIGATOR_PLUGIN,
							event -> PreferencesHelper.NAVIGATOR_PLUGIN.save());
					check("Modification date", PreferencesHelper.NAVIGATOR_DATE,
							event -> PreferencesHelper.NAVIGATOR_DATE.save());
					if (PreferencesHelper.NAVIGATOR_DATE.getValue()) {
						separate(mainMenu);
						check("Most recent first", PreferencesHelper.NAVIGATOR_RECENT_FIRST,
								event -> PreferencesHelper.NAVIGATOR_RECENT_FIRST.save());
					}
					separate(mainMenu);
					separate(mainMenu, "Search in:");
					check("Only titles", PreferencesHelper.NAVIGATOR_SEARCH_TITLES, event -> {
						PreferencesHelper.NAVIGATOR_SEARCH_TITLES.save();
						findControl.searchOptionsChanged();
					}).setToolTipText("Search file, folder, group and project names, and model tags. "
							+ "When unchecked, search saved model source, including comments.");
					check("Only models", PreferencesHelper.NAVIGATOR_SEARCH_MODELS_ONLY, event -> {
						PreferencesHelper.NAVIGATOR_SEARCH_MODELS_ONLY.save();
						findControl.searchOptionsChanged();
					}).setEnabled(PreferencesHelper.NAVIGATOR_SEARCH_TITLES.getValue());
				}

			};
			menu.open(tbs, e, tbs.getSize().y, 100);
		});

		try {
			final IDecoratorManager mgr = PlatformUI.getWorkbench().getDecoratorManager();
			mgr.setEnabled("gama.ui.application.date.decorator", PreferencesHelper.NAVIGATOR_DATE.getValue());
			mgr.setEnabled("gama.ui.application.decorator", PreferencesHelper.NAVIGATOR_METADATA.getValue());
		} catch (final CoreException e) {
			e.printStackTrace();
		}
		findControl.initialize();
		scheduleSyncWithEditor();

	}

	@Override
	public void saveState(final IMemento newMemento) {
		if (PreferencesHelper.KEEP_NAVIGATOR_STATE.getValue()) {
			final String state = computeExpandedState();
			if (state != null) {
				newMemento.putString(EXPANDED_STATE, state);
				storeExpandedState(state);
			}
		}
		super.saveState(newMemento);
	}

	@Override
	public void dispose() {
		saveExpandedStateJob.cancel();
		syncWithEditorJob.cancel();
		getSite().getPage().removePartListener(editorListener);
		saveExpandedState();
		super.dispose();
	}

	/**
	 * Saves the expanded elements in the workspace preferences, which are not dependent on a clean shutdown of the
	 * workbench (contrary to the memento).
	 */
	private void saveExpandedState() {
		if (!PreferencesHelper.KEEP_NAVIGATOR_STATE.getValue()) return;
		final String state = computeExpandedState();
		if (state != null) { storeExpandedState(state); }
	}

	/**
	 * Serializes the expanded elements of the viewer.
	 *
	 * @return the serialized state, or null if the viewer is not available
	 */
	private String computeExpandedState() {
		final CommonViewer viewer = getCommonViewer();
		if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) return null;
		final StringBuilder sb = new StringBuilder();
		for (final Object o : viewer.getExpandedElements()) {
			String name = null;
			if (o instanceof WrappedContainer<?> c) {
				name = c.getResource().getFullPath().toString();
			} else if (o instanceof TopLevelFolder t) {
				name = t.getName();
			} else if (o instanceof UserGroupFolder g && g.getTopLevelFolder() != null) {
				name = GROUP_PREFIX + g.getTopLevelFolder().getName() + GROUP_PREFIX
						+ g.getRelativePath().toPortableString();
			}
			if (name != null) { sb.append(name).append(SEPARATOR); }
		}
		if (sb.length() > SEPARATOR.length()) { sb.setLength(sb.length() - SEPARATOR.length()); }
		return sb.toString();
	}

	/**
	 * Stores the serialized expanded state in the preferences of the current workspace.
	 *
	 * @param state
	 *            the state
	 */
	private void storeExpandedState(final String state) {
		final IEclipsePreferences node = getWorkspacePreferences();
		if (state.equals(node.get(EXPANDED_STATE, null))) return;
		node.put(EXPANDED_STATE, state);
		try {
			node.flush();
		} catch (final BackingStoreException e) {
			DEBUG.ERR("Unable to save the state of the navigator", e);
		}
	}

	/**
	 * Gets the preferences of the navigator in the current workspace.
	 *
	 * @return the workspace preferences
	 */
	private static IEclipsePreferences getWorkspacePreferences() {
		return InstanceScope.INSTANCE.getNode("gama.ui.navigator");
	}

	/**
	 * Restore state.
	 */
	private void restoreState() {
		if (!PreferencesHelper.KEEP_NAVIGATOR_STATE.getValue()) return;
		String saved = getWorkspacePreferences().get(EXPANDED_STATE, null);
		if (saved == null && memento != null) { saved = memento.getString(EXPANDED_STATE); }
		if (saved == null || saved.isEmpty()) return;
		final List<VirtualContent<?>> contents = new ArrayList<>();
		final String[] names = saved.split(SEPARATOR);
		for (final String s : names) {
			if (s.startsWith("/")) {
				final WrappedResource<?, ?> resource = getInstance().getManager().resolveWrappedInstanceOf(
						GAMA.getWorkspaceManager().getWorkspace().getRoot().findMember(new Path(s)));
				if (resource != null) { contents.add(resource); }
			} else if (s.startsWith(GROUP_PREFIX)) {
				final int index = s.indexOf(GROUP_PREFIX, GROUP_PREFIX.length());
				if (index < 0) { continue; }
				final TopLevelFolder folder = getInstance().getFolder(s.substring(GROUP_PREFIX.length(), index));
				if (folder == null) { continue; }
				final UserGroupFolder group =
						folder.getGroup(Path.fromPortableString(s.substring(index + GROUP_PREFIX.length())));
				if (group != null) { contents.add(group); }
			} else {
				final TopLevelFolder folder = getInstance().getFolder(s);
				if (folder != null) { contents.add(folder); }
			}
		}
		final VirtualContent<?>[] sel = contents.toArray(new VirtualContent[0]);
		if (sel.length > 0) {
			getCommonViewer().setExpandedElements((Object[]) sel);
			// When linking is enabled, the selection is that of the active editor
			if (!isLinkingEnabled()) { getCommonViewer().setSelection(new StructuredSelection(sel[sel.length - 1])); }
		}

	}

	@Override
	public void selectReveal(final ISelection selection) {
		VirtualContent<?> current;
		final Object o1 = getCommonViewer().getStructuredSelection().getFirstElement();
		if (o1 instanceof IResource) {
			current = NavigatorRoot.getInstance().getManager().resolveWrappedInstanceOf(o1);
		} else {
			current = (VirtualContent<?>) getCommonViewer().getStructuredSelection().getFirstElement();
		}
		StructuredSelection newSelection = new StructuredSelection();
		if (selection instanceof StructuredSelection) {
			newSelection = (StructuredSelection) selection;
			Object o = ((StructuredSelection) selection).getFirstElement();
			if (o instanceof IResource) {
				o = NavigatorRoot.getInstance().getManager().resolveWrappedInstanceOf(o);
				if (o != null) { newSelection = new StructuredSelection(o); }
			}
		}
		if (current instanceof WrappedSyntacticContent) {
			final Object o = newSelection.getFirstElement();
			if (o instanceof WrappedFile) {
				if (((VirtualContent<?>) current).isContainedIn((VirtualContent<?>) o)) {
					getCommonViewer().setSelection(new StructuredSelection(current));
				}
				return;
			}
		}
		if (!newSelection.isEmpty()) { super.selectReveal(newSelection); }
	}

	@Override
	protected CommonViewer createCommonViewerObject(final Composite aParent) {
		return new NavigatorCommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	@Override
	protected Object getInitialInput() { return NavigatorRoot.getInstance(); }

	@Override
	protected void handleDoubleClick(final DoubleClickEvent anEvent) {
		final IStructuredSelection selection = (IStructuredSelection) anEvent.getSelection();
		final Object element = selection.getFirstElement();
		if (element instanceof VirtualContent vc && vc.handleDoubleClick()) {
			if (element instanceof Tag t) {
				findControl.searchFor(t.getName());
				return;
			}
		} else {
			super.handleDoubleClick(anEvent);
		}
		if (element instanceof WrappedContainer || element instanceof TopLevelFolder
				|| element instanceof UserGroupFolder) {
			getCommonViewer().revealExpandedChildren(element);
		}
	}

	/**
	 * Method createToolItem()
	 *
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView#createToolItem(int,
	 *      gama.ui.shared.views.toolbar.GamaToolbar2)
	 */
	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		this.toolbar = tb;
		findControl = new NavigatorSearchControl(this).fill(toolbar.getToolbar(SWT.LEFT));
	}

	@Override
	public NavigatorCommonViewer getCommonViewer() { return (NavigatorCommonViewer) super.getCommonViewer(); }

	/**
	 * Method selectionChanged()
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		final IStructuredSelection currentSelection = (IStructuredSelection) event.getSelection();
		VirtualContent<?> element;
		if (currentSelection == null || currentSelection.isEmpty()) {
			element = NavigatorRoot.getInstance();
		} else {
			element = (VirtualContent<?>) currentSelection.getFirstElement();
		}
		element.handleSingleClick();
	}

	@Override
	public void expandAll() {
		WorkbenchHelper.asyncRun(() -> getCommonViewer().expandAll());
	}

	@Override
	public void collapseAll() {
		getCommonViewer().collapseAll();
	}

}
