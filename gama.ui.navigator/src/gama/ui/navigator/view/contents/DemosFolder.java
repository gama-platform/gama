package gama.ui.navigator.view.contents;

import gama.workspace.manager.ProjectCategory;

/**
 * Virtual top-level folder gathering the projects classified as "Demos" (complete toy and serious models), i.e. those
 * found in the {@code models/Demos} directory of a plugin.
 */
public class DemosFolder extends TopLevelFolder {

	/**
	 * Instantiates a new demos folder.
	 *
	 * @param root
	 *            the navigator root
	 * @param name
	 *            the display name shown in the navigator
	 */
	public DemosFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_DEMOS, "Complete models, from toy to serious ones", BLUE,
				ProjectCategory.DEMOS);
	}

}
