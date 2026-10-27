/*******************************************************************************************************
 *
 * RecipesFolder.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import gama.workspace.manager.ProjectCategory;

/**
 * The Class RecipesFolder.
 *
 * <p>
 * Virtual top-level folder that aggregates the recipe projects: focused, self-contained demonstrations of a single GAML
 * feature or modeling pattern, found in the {@code models/Recipes} directory of GAMA or of one of its plugins.
 * </p>
 */
public class RecipesFolder extends TopLevelFolder {

	/**
	 * Instantiates a new recipes folder.
	 *
	 * @param root
	 *            the root navigator root
	 * @param name
	 *            the display name shown in the navigator (typically "Recipes")
	 */
	public RecipesFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_RECIPES, "Focused GAMA features demonstrations", WARNING, ProjectCategory.RECIPES);
	}

}
