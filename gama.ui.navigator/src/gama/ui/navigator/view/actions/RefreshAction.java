/*******************************************************************************************************
 *
 * RefreshAction.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;

import static gama.api.ui.IGui.NAVIGATOR_VIEW_ID;
import static gama.ui.navigator.view.contents.ResourceManager.getInstance;
import static gama.ui.shared.utils.WorkbenchHelper.runInUI;
import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;
import static org.eclipse.core.resources.IResource.PROJECT;
import static org.eclipse.core.resources.IResource.ROOT;
import static org.eclipse.jface.viewers.StructuredSelection.EMPTY;
import static org.eclipse.ui.PlatformUI.PLUGIN_ID;
import static org.eclipse.ui.internal.ide.IIDEHelpContextIds.REFRESH_ACTION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

import gama.api.GAMA;
import gama.ui.navigator.view.GamaNavigator;
import gama.ui.navigator.view.contents.NavigatorRoot;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.view.contents.UserGroups;
import gama.ui.navigator.view.contents.VirtualContent;
import gama.ui.navigator.view.contents.WrappedProject;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.workspace.manager.WorkspaceModelsManager;
import gama.workspace.metadata.FileMetaDataProvider;

/**
 * Standard action for refreshing the workspace from the local file system for the selected resources and all of their
 * descendants.
 * <p>
 * This class may be instantiated; it may also subclass to extend:
 * <ul>
 * <li>getSelectedResources - A list containing 0 or more resources to be refreshed</li>
 * <li>updateSelection - controls when this action is enabled</li>
 * <li>refreshResource - can be extended to refresh model objects related to the resource</li>
 * <ul>
 * </p>
 */
public class RefreshAction extends WorkspaceAction {

	/** The navigator. */
	GamaNavigator navigator;

	/**
	 * Gets the navigator.
	 *
	 * @return the navigator
	 */
	private GamaNavigator getNavigator() {
		if (navigator == null) {
			final IWorkbenchPage page = WorkbenchHelper.getPage();
			if (page != null) { navigator = (GamaNavigator) page.findView(NAVIGATOR_VIEW_ID); }
		}
		return navigator;
	}

	/**
	 * The id of this action.
	 */
	public static final String ID = PLUGIN_ID + ".RefreshAction";//$NON-NLS-1$

	/** The resources. */
	public List<? extends IResource> resources;

	private List<TopLevelFolder> contributedFolders = List.of();

	/**
	 * Creates a new action.
	 *
	 * @param provider
	 *            the IShellProvider for any dialogs.
	 * @since 3.4
	 */
	public RefreshAction(final IShellProvider provider) {
		super(provider, IDEWorkbenchMessages.RefreshAction_text);
		initAction();
	}

	/**
	 * Initializes for the constructor.
	 */
	private void initAction() {
		setToolTipText(IDEWorkbenchMessages.RefreshAction_toolTip);
		setId(ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, REFRESH_ACTION);
	}

	/**
	 * Removes missing library references silently; asks before removing missing user projects.
	 */
	void checkLocationDeleted(final IProject project) throws CoreException {
		if (!project.exists() || WorkspaceModelsManager.removeMissingLibraryProject(project)) return;
		final IFileInfo location = IDEResourceInfoUtils.getFileInfo(project.getLocationURI());
		if (!location.exists() && GAMA.getGui().getDialogFactory().confirm("Project location has been deleted",
				"The location for project " + project.getName() + " (" + location.toString()
						+ ") has been deleted. Do you want to remove " + project.getName() + " from the workspace ?")) {
			project.delete(true, true, null);
		}
	}

	@Override
	protected String getOperationMessage() { return IDEWorkbenchMessages.RefreshAction_progressMessage; }

	@Override
	protected String getProblemsMessage() { return IDEWorkbenchMessages.RefreshAction_problemMessage; }

	@Override
	protected String getProblemsTitle() { return IDEWorkbenchMessages.RefreshAction_problemTitle; }

	/**
	 * Returns a list containing the workspace root if the selection would otherwise be empty.
	 */
	@Override
	protected List<? extends IResource> getSelectedResources() {
		final List<IResource> resources1 = new ArrayList<>();
		final List<TopLevelFolder> contributions = new ArrayList<>();
		for (final IResource r : super.getSelectedResources()) { if (r.isAccessible()) { resources1.add(r); } }
		// A virtual top-level folder stands for the projects it contains
		for (final Object o : getStructuredSelection()) {
			if (o instanceof VirtualContent<?> content && content.getTopLevelFolder() != null
					&& !content.getTopLevelFolder().isWorkspaceFolder()) {
				final TopLevelFolder folder = content.getTopLevelFolder();
				if (!contributions.contains(folder)) { contributions.add(folder); }
			} else if (o instanceof TopLevelFolder folder) {
				for (final WrappedProject wp : folder.getProjects()) {
					resources1.add(wp.getResource());
				}
			} else if (o instanceof UserGroupFolder group) {
				for (final WrappedProject wp : group.getAllProjects()) {
					resources1.add(wp.getResource());
				}
			}
		}
		if (resources1.isEmpty() && contributions.isEmpty()) {
			resources1.add(GAMA.getWorkspaceManager().getRoot());
		}
		if (getStructuredSelection().isEmpty()) {
			for (final TopLevelFolder folder : NavigatorRoot.getInstance().getFolders()) {
				if (!folder.isWorkspaceFolder()) { contributions.add(folder); }
			}
		}
		contributedFolders = List.copyOf(contributions);
		return resources1;
	}

	/**
	 * The <code>RefreshAction</code> implementation of this <code>SelectionListenerAction</code> method ensures that
	 * this action is enabled if the selection is empty, but is disabled if any of the selected elements are not
	 * resources.
	 */
	@Override
	protected boolean updateSelection(final IStructuredSelection s) {
		resources = getSelectedResources();
		return true;
	}

	/**
	 * Refreshes the entire workspace.
	 */
	final public void refreshAll() {
		final IStructuredSelection currentSelection = getStructuredSelection();
		selectionChanged(EMPTY);
		run();
		selectionChanged(currentSelection);
	}

	@Override
	final protected IRunnableWithProgress createOperation(final IStatus[] errorStatus) {
		final List<? extends IResource> selectedResources = List.copyOf(resources);
		final List<TopLevelFolder> selectedContributions = contributedFolders;
		return monitor -> {
			final MultiStatus errors = new MultiStatus("gama.ui.navigator", 0,
					"Some navigator content could not be refreshed", null);
			try {
				for (final TopLevelFolder folder : selectedContributions) {
					if (monitor.isCanceled()) throw new OperationCanceledException();
					try {
						folder.refresh(monitor);
					} catch (final OperationCanceledException e) {
						throw e;
					} catch (final CoreException e) {
						errors.add(e.getStatus());
					} catch (final RuntimeException e) {
						errors.add(new Status(IStatus.ERROR, "gama.ui.navigator",
								"Unable to refresh " + folder.getName(), e));
					}
				}
				if (!selectedResources.isEmpty()) {
					new WorkspaceModifyOperation() {
						@Override
						protected void execute(final IProgressMonitor workspaceMonitor) {
							for (final IResource resource : selectedResources) {
								if (workspaceMonitor.isCanceled()) throw new OperationCanceledException();
								try {
									refreshResource(resource, workspaceMonitor);
								} catch (final CoreException e) {
									errors.add(e.getStatus());
								}
							}
						}
					}.run(monitor);
				}
			} finally {
				errorStatus[0] = errors.isOK() ? Status.OK_STATUS : errors;
				UserGroups.refreshNavigator();
				monitor.done();
			}
		};
	}

	/**
	 * Refresh the resource (with a check for deleted projects).
	 * <p>
	 * This method may be extended to refresh model objects related to the resource.
	 * </p>
	 *
	 * @param resource
	 *            the resource to refresh. Must not be <code>null</code>.
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             if things go wrong
	 * @since 3.4
	 */
	protected void refreshResource(final IResource resource, final IProgressMonitor monitor) throws CoreException {
		if (resource.getType() == PROJECT) {
			checkLocationDeleted((IProject) resource);
		} else if (resource.getType() == ROOT) {
			final IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
			for (final IProject project : projects) { checkLocationDeleted(project); }
		}
		resource.refreshLocal(DEPTH_INFINITE, monitor);
		if (resource.getParent() != null) { resource.getParent().refreshLocal(DEPTH_INFINITE, monitor); }

		runInUI("Refreshing " + resource.getName(), 0, m -> {
			FileMetaDataProvider.getInstance().storeMetaData(resource, null, true);
			FileMetaDataProvider.getInstance().getMetaData(resource, false, true);
			getNavigator().getCommonViewer().refresh(getInstance().findWrappedInstanceOf(resource), true);
		});

	}

}
