/*******************************************************************************************************
 *
 * UserProjectsFolder.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;

import gama.workspace.nature.GamaNatures;

/**
 * The Class UserProjectsFolder.
 */
public class UserProjectsFolder extends TopLevelFolder {

	/**
	 * Instantiates a new user projects folder.
	 *
	 * @param root
	 *            the root
	 * @param name
	 *            the name
	 */
	public UserProjectsFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_USER, "User-defined models", OK, null, Location.Other);
	}

	@Override
	public boolean accepts(final IProjectDescription desc) {
		if (desc == null) return false;
		return !desc.hasNature(GamaNatures.BUILTIN_NATURE)
				&& !desc.hasNature(GamaNatures.PLUGIN_NATURE)
				&& !desc.hasNature(GamaNatures.TEST_NATURE)
				&& !desc.hasNature(GamaNatures.TUTORIAL_NATURE)
				&& !desc.hasNature(GamaNatures.RECIPE_NATURE);
	}

	@Override
	protected Location estimateLocation(final IPath location) {
		final Location result = super.estimateLocation(location);
		return result == Location.Other ? Location.Other : Location.Unknown;
	}

}
