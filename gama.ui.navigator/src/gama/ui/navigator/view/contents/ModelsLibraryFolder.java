/*******************************************************************************************************
 *
 * ModelsLibraryFolder.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import gama.ui.shared.resources.IGamaColors;
import gama.workspace.manager.ProjectCategory;

/**
 * The Class ModelsLibraryFolder.
 *
 * <p>
 * Virtual top-level folder that aggregates the projects contributed by GAMA and its plugins that are not classified in
 * Data, Demos, Recipes, Tutorials or Tests (i.e. those found in {@code models/Library}, directly in {@code models} or
 * in a sub-folder of {@code models} that is not the name of a category).
 * </p>
 */
public class ModelsLibraryFolder extends TopLevelFolder {

	/**
	 * Instantiates a new models library folder.
	 *
	 * @param root
	 *            the root navigator root
	 * @param name
	 *            the display name shown in the navigator
	 */
	public ModelsLibraryFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_BUILTIN, "Models shipped with GAMA and its plugins", IGamaColors.BLUE,
				ProjectCategory.LIBRARY);
	}

}
