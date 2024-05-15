/*******************************************************************************************************
 *
 * RefreshHandler.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import static gama.ui.navigator.view.contents.ResourceManager.getInstance;
import static gama.ui.shared.utils.WorkbenchHelper.runInUI;
import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;
import static org.eclipse.core.resources.IResource.PROJECT;
import static org.eclipse.core.resources.IResource.ROOT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

import gama.core.common.interfaces.IGui;
import gama.core.runtime.GAMA;
import gama.core.util.file.IFileMetaDataProvider;
import gama.dev.DEBUG;
import gama.ui.application.workspace.WorkspaceModelsManager;
import gama.ui.navigator.metadata.FileMetaDataProvider;
import gama.ui.navigator.view.GamaNavigator;
import gama.ui.navigator.view.contents.NavigatorRoot;
import gama.ui.navigator.view.contents.ResourceManager;
import gama.ui.shared.dialogs.Messages;
import gama.ui.shared.interfaces.IRefreshHandler;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class RefreshHandler.
 */
public class RefreshHandler implements IRefreshHandler {

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
			if (page != null) { navigator = (GamaNavigator) page.findView(IGui.NAVIGATOR_VIEW_ID); }
		}
		return navigator;
	}

	@Override
	public void refreshResource(final IResource resource) {
		if (resource.getType() == PROJECT) {
			try {
				checkLocationDeleted((IProject) resource);
			} catch (final CoreException e) {
				e.printStackTrace();
				return;
			}
		} else if (resource.getType() == ROOT) {
			final IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
			for (final IProject project : projects) {
				try {
					checkLocationDeleted(project);
				} catch (final CoreException e) {
					e.printStackTrace();
					return;
				}
			}
		}

		runInUI("Refreshing " + resource.getName(), 0, m -> {
			FileMetaDataProvider.getInstance().storeMetaData(resource, null, true);
			FileMetaDataProvider.getInstance().getMetaData(resource, false, true);
			getNavigator().getCommonViewer().refresh(getInstance().findWrappedInstanceOf(resource), true);
			final WorkspaceJob job = new WorkspaceJob("Refreshing " + resource.getName()) {

				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
					resource.refreshLocal(DEPTH_INFINITE, monitor);
					resource.getParent().refreshLocal(DEPTH_INFINITE, monitor);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		});
	}

	@Override
	public void completeRefresh(final List<? extends IResource> list) {
		final IStatus[] errorStatus = new IStatus[1];
		errorStatus[0] = Status.OK_STATUS;
		final List<? extends IResource> resources =
				list == null || list.isEmpty() ? Collections.singletonList(ResourcesPlugin.getWorkspace().getRoot()) : list;
		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			public void execute(final IProgressMonitor monitor) {
				try {
					for (IResource resource : resources) {
						try {
							if (resource.getType() == IResource.PROJECT) {
								checkLocationDeleted((IProject) resource);
							} else if (resource.getType() == IResource.ROOT) {
								final IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
								for (final IProject project : projects) { checkLocationDeleted(project); }
							}
							resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
							if (monitor != null) { monitor.worked(1); }
						} catch (final CoreException e) {}
						if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
					}
				} finally {
					if (monitor != null) { monitor.done(); }
				}
			}
		};
		final WorkspaceJob job = new WorkspaceJob("Refreshing the GAMA Workspace") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

				try {
					ResourceManager.block();
					monitor.beginTask("Refreshing GAMA Workspace: updating the library of models", 100);
					DEBUG.LOG("Refreshing GAMA Workspace: updating the library of models");
					WorkspaceModelsManager.instance.loadModelsLibrary();
					monitor.beginTask("Refreshing GAMA Workspace: recreating files metadata", 1000);
					DEBUG.LOG("Refreshing GAMA Workspace: recreating files metadata");
					for (final IResource r : resources) {
						r.accept(proxy -> {
							final IFileMetaDataProvider provider = GAMA.getGui().getMetaDataProvider();
							final IResource file = proxy.requestResource();
							provider.storeMetaData(file, null, true);
							provider.getMetaData(file, false, true);
							monitor.worked(1);
							return true;
						}, IResource.NONE);

					}
					monitor.beginTask("Refreshing GAMA Workspace: refreshing resources", resources.size());
					DEBUG.LOG("Refreshing GAMA Workspace: refreshing resources");
					op.run(monitor);
					monitor.beginTask("Refreshing GAMA Workspace: deleting virtual folders caches", 1);
					DEBUG.LOG("Refreshing GAMA Workspace: deleting virtual folders caches");
					NavigatorRoot.getInstance().resetVirtualFolders(NavigatorRoot.getInstance().getManager());
					monitor.beginTask("Refreshing GAMA Workspace: refreshing the navigator", 1);
					DEBUG.LOG("Refreshing GAMA Workspace: deleting virtual folders caches");
					final IWorkspace workspace = ResourcesPlugin.getWorkspace();
					monitor.beginTask("Refreshing GAMA Workspace: rebuilding models", 100);
					try {

						workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, new ProgressMonitorWrapper(monitor) {

						});

					} catch (final CoreException ex) {
						ex.printStackTrace();
					}
				} catch (final Exception e) {
					return Status.CANCEL_STATUS;
				} finally {
					ResourceManager.unblock(monitor);
					WorkbenchHelper.refreshNavigator();
					monitor.done();
				}
				return errorStatus[0];
			}

		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Check location deleted.
	 *
	 * @param project
	 *            the project
	 * @throws CoreException
	 *             the core exception
	 */
	void checkLocationDeleted(final IProject project) throws CoreException {
		if (!project.exists()) return;
		final IFileInfo location = IDEResourceInfoUtils.getFileInfo(project.getLocationURI());
		if (!location.exists() && Messages.confirm("Project location has been deleted",
				"The location for project " + project.getName() + " (" + location.toString()
						+ ") has been deleted. Do you want to remove " + project.getName() + " from the workspace ?")) {
			project.delete(true, true, null);
		}
	}

}
