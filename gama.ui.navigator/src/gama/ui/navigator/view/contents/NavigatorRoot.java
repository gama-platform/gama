/*******************************************************************************************************
 *
 * NavigatorRoot.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

import gama.api.GAMA;
import gama.api.constants.GamlFileExtension;

/**
 * The Class NavigatorRoot.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class NavigatorRoot extends VirtualContent implements IAdaptable {

	/** The Constant instance. */
	private final static NavigatorRoot instance = new NavigatorRoot();

	/** The mapper. */
	private ResourceManager mapper;

	private TopLevelFolderRegistry folders;

	private boolean loadingContributions;

	/**
	 * Instantiates a new navigator root.
	 */
	public NavigatorRoot() {
		super(null, "Workspace");
	}

	/**
	 * Gets the user folder.
	 *
	 * @return the user folder
	 */
	public TopLevelFolder getUserFolder() {
		return getFolderRegistry().getWorkspaceFolder("user");
	}

	/**
	 * Gets the test folder.
	 *
	 * @return the test folder
	 */
	public TopLevelFolder getTestFolder() {
		return getFolderRegistry().getWorkspaceFolder("tests");
	}

	/**
	 * Gets the library folder. This folder aggregates both built-in core models and plugin models.
	 *
	 * @return the library folder
	 */
	public TopLevelFolder getLibraryFolder() {
		return getFolderRegistry().getWorkspaceFolder("library");
	}

	/**
	 * Gets the data folder.
	 *
	 * @return the data folder
	 */
	public TopLevelFolder getDataFolder() {
		return getFolderRegistry().getWorkspaceFolder("data");
	}

	/**
	 * Gets the demos folder.
	 *
	 * @return the demos folder
	 */
	public TopLevelFolder getDemosFolder() {
		return getFolderRegistry().getWorkspaceFolder("demos");
	}

	/**
	 * Gets the tutorials folder.
	 *
	 * @return the tutorials folder
	 */
	public TopLevelFolder getTutorialsFolder() {
		return getFolderRegistry().getWorkspaceFolder("tutorials");
	}

	/**
	 * Gets the recipes folder.
	 *
	 * @return the recipes folder
	 */
	public TopLevelFolder getRecipesFolder() {
		return getFolderRegistry().getWorkspaceFolder("recipes");
	}

	/**
	 * Rebuilds initialized virtual folders without replacing the elements whose expansion the viewer remembers.
	 *
	 * @param manager
	 *            the manager
	 */
	public void resetVirtualFolders(final ResourceManager manager) {
		if (manager != null) { this.setManager(manager); }
		if (folders != null) { folders.resetWorkspaceFolders(); }
	}

	/**
	 * Recreate virtual folders.
	 */
	public void recreateVirtualFolders() {
		getFolders();
	}

	@Override
	public Object getAdapter(final Class adapter) {
		if (adapter == IResource.class || adapter == IContainer.class) return GAMA.getWorkspaceManager().getRoot();
		return AdapterManager.getDefault().getAdapter(this, adapter);
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public Object[] getNavigatorChildren() {
		final TopLevelFolder user = getUserFolder();
		return java.util.Arrays.stream(getFolders())
				.filter(f -> f == user || f.isVisibleWhenEmpty() || f.hasChildren()).toArray();
	}

	/**
	 * Gets the folders.
	 *
	 * @return the folders
	 */
	public TopLevelFolder[] getFolders() {
		final TopLevelFolder[] workspaceFolders = getWorkspaceFolders();
		if (mapper == null || mapper.isDisposed()) return workspaceFolders;
		if (loadingContributions)
			throw new IllegalStateException("A navigator folder factory must not enumerate the root's folders");
		loadingContributions = true;
		try {
			return getFolderRegistry().getFolders();
		} finally {
			loadingContributions = false;
		}
	}

	private TopLevelFolderRegistry getFolderRegistry() {
		if (folders == null) { folders = new TopLevelFolderRegistry(this); }
		return folders;
	}

	/** Built-in project categories only; workspace changes must not initialize or reparent remote hierarchies. */
	public TopLevelFolder[] getWorkspaceFolders() {
		return getFolderRegistry().getWorkspaceFolders();
	}

	/** Disposes optional roots on navigator shutdown; reopening creates fresh contributions. */
	public void disposeContributions() {
		if (folders != null) { folders.disposeContributions(); }
	}

	/** Finds a top-level folder by its stable identifier rather than its label. */
	public TopLevelFolder getFolderById(final String id) {
		for (final TopLevelFolder folder : getFolders()) { if (folder.getId().equals(id)) return folder; }
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() { return null; }

	@Override
	public void getSuffix(final StringBuilder sb) {}

	@Override
	public int findMaxProblemSeverity() {
		return 0;
	}

	@Override
	public ImageDescriptor getOverlay() { return null; }

	@Override
	public TopLevelFolder getTopLevelFolder() { return getUserFolder(); }

	@Override
	public VirtualContentType getType() { return VirtualContentType.ROOT; }

	@Override
	public String getStatusMessage() {
		final IWorkspaceRoot root = GAMA.getWorkspaceManager().getRoot();
		try {
			final int projectsCount = root.members().length;
			int modelsCount = 0;
			for (final IResource r : root.members()) {
				final int modelCount[] = new int[1];
				try {
					if (r.isAccessible()) {
						r.accept(proxy -> {
							if (proxy.getType() == IResource.FILE && GamlFileExtension.isAny(proxy.getName())) {
								modelCount[0]++;
							}
							return true;
						}, IResource.NONE);
					}
				} catch (final CoreException e) {
					e.printStackTrace();
				}
				modelsCount += modelCount[0];
			}
			final String loc = root.getLocation().lastSegment();
			return getName() + " " + loc + " (" + projectsCount + " projects, " + modelsCount + " models)";
		} catch (final CoreException e) {}
		return getName();

	}

	/**
	 * Sets the library folder.
	 *
	 * @param libraryFolder
	 *            the new library folder
	 */
	public void setLibraryFolder(final TopLevelFolder libraryFolder) {
		getFolderRegistry().replaceWorkspaceFolder("library", libraryFolder);
	}

	/**
	 * Sets the user folder.
	 *
	 * @param userFolder
	 *            the new user folder
	 */
	public void setUserFolder(final TopLevelFolder userFolder) {
		getFolderRegistry().replaceWorkspaceFolder("user", userFolder);
	}

	/**
	 * Sets the test folder.
	 *
	 * @param testFolder
	 *            the new test folder
	 */
	public void setTestFolder(final TopLevelFolder testFolder) {
		getFolderRegistry().replaceWorkspaceFolder("tests", testFolder);
	}

	/**
	 * Sets the tutorials folder.
	 *
	 * @param tutorialsFolder
	 *            the new tutorials folder
	 */
	public void setTutorialsFolder(final TopLevelFolder tutorialsFolder) {
		getFolderRegistry().replaceWorkspaceFolder("tutorials", tutorialsFolder);
	}

	/**
	 * Sets the recipes folder.
	 *
	 * @param recipesFolder
	 *            the new recipes folder
	 */
	public void setRecipesFolder(final TopLevelFolder recipesFolder) {
		getFolderRegistry().replaceWorkspaceFolder("recipes", recipesFolder);
	}

	@Override
	public ResourceManager getManager() { return mapper; }

	/**
	 * Sets the manager.
	 *
	 * @param mapper
	 *            the new manager
	 */
	public void setManager(final ResourceManager mapper) { this.mapper = mapper; }

	/**
	 * Gets the folder.
	 *
	 * @param s
	 *            the s
	 * @return the folder
	 */
	public TopLevelFolder getFolder(final String s) {
		for (final TopLevelFolder folder : getWorkspaceFolders()) { if (folder.getName().equals(s)) return folder; }
		for (final TopLevelFolder folder : getFolders()) { if (folder.getName().equals(s)) return folder; }
		return null;
	}

	/**
	 * Gets the single instance of NavigatorRoot.
	 *
	 * @return single instance of NavigatorRoot
	 */
	public static NavigatorRoot getInstance() { return instance; }

}
