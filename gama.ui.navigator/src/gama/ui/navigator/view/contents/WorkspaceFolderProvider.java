/*******************************************************************************************************
 *
 * WorkspaceFolderProvider.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import gama.workspace.manager.ProjectCategory;

/** Factory used by the navigator's own topLevelFolders declarations, not by independent downstream roots. */
public final class WorkspaceFolderProvider implements ITopLevelFolderProvider, IExecutableExtension {

	private ProjectCategory category;
	private String name;

	@Override
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) {
		category = ProjectCategory.valueOf(config.getAttribute("category"));
		name = config.getAttribute("name");
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Workspace folder name is required");
	}

	@Override
	public TopLevelFolder createFolder(final NavigatorRoot root) {
		return switch (category) {
			case DATA -> new DataFolder(root, name);
			case DEMOS -> new DemosFolder(root, name);
			case LIBRARY -> new ModelsLibraryFolder(root, name);
			case RECIPES -> new RecipesFolder(root, name);
			case TUTORIALS -> new TutorialsFolder(root, name);
			case TESTS -> new TestModelsFolder(root, name);
			case USER -> new UserProjectsFolder(root, name);
		};
	}
}
