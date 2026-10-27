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

import gama.workspace.manager.ProjectCategory;

/**
 * The Class UserProjectsFolder: the projects created or imported by the user, i.e. all the projects that are not
 * located in a folder contributed by a plugin or in the {@code Tests} directory of the workspace.
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
		super(root, name, FOLDER_USER, "User-defined models", OK, ProjectCategory.USER);
	}

}
