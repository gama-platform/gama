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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.navigator.CommonNavigatorActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonNavigatorManager;
import org.eclipse.ui.navigator.CommonViewer;

import gama.api.GAMA;
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

	}

	@Override
	public void saveState(final IMemento newMemento) {
		if (PreferencesHelper.KEEP_NAVIGATOR_STATE.getValue()) {
			final StringBuilder sb = new StringBuilder();
			for (final Object o : getCommonViewer().getExpandedElements()) {
				final String name =
						o instanceof WrappedContainer ? ((WrappedContainer<?>) o).getResource().getFullPath().toString()
								: o instanceof TopLevelFolder t ? t.getName() : null;
				if (name != null) {
					sb.append(name);
					sb.append("@@");
				}
			}
			if (sb.length() > 2) { sb.setLength(sb.length() - 2); }
			newMemento.putString("EXPANDED_STATE", sb.toString());
		}
		super.saveState(newMemento);
	}

	/**
	 * Restore state.
	 */
	private void restoreState() {
		if (memento == null) return;
		final String saved = memento.getString("EXPANDED_STATE");
		if (saved == null) return;
		final List<VirtualContent<?>> contents = new ArrayList<>();
		final String[] names = saved.split("@@");
		for (final String s : names) {
			if (s.startsWith("/")) {
				final WrappedResource<?, ?> resource = getInstance().getManager().findWrappedInstanceOf(
						GAMA.getWorkspaceManager().getWorkspace().getRoot().findMember(new Path(s)));
				if (resource != null) { contents.add(resource); }
			} else {
				final TopLevelFolder folder = getInstance().getFolder(s);
				if (folder != null) { contents.add(folder); }
			}
		}
		final VirtualContent<?>[] sel = contents.toArray(new VirtualContent[0]);
		if (sel.length > 0) {
			getCommonViewer().setExpandedElements((Object) sel);
			getCommonViewer().setSelection(new StructuredSelection(sel[sel.length - 1]));
		}

	}

	@Override
	public void selectReveal(final ISelection selection) {
		VirtualContent<?> current;
		final Object o1 = getCommonViewer().getStructuredSelection().getFirstElement();
		if (o1 instanceof IResource) {
			current = NavigatorRoot.getInstance().getManager().findWrappedInstanceOf(o1);
		} else {
			current = (VirtualContent<?>) getCommonViewer().getStructuredSelection().getFirstElement();
		}
		StructuredSelection newSelection = new StructuredSelection();
		if (selection instanceof StructuredSelection) {
			newSelection = (StructuredSelection) selection;
			Object o = ((StructuredSelection) selection).getFirstElement();
			if (o instanceof IResource) {
				o = NavigatorRoot.getInstance().getManager().findWrappedInstanceOf(o);
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
			final CommonViewer tree = getCommonViewer();
			if (tree.getExpandedState(element)) {
				final Object[] contents = ((VirtualContent<?>) element).getNavigatorChildren();
				if (contents.length > 0) { tree.reveal(contents[contents.length - 1]); }
			}
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
