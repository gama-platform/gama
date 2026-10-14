package gama.ui.navigator.view.contents;

import gama.workspace.manager.ProjectCategory;

/**
 * Virtual top-level folder gathering the projects classified as "Data" (models related to data management), i.e.
 * those found in the {@code models/Data} directory of a plugin.
 */
public class DataFolder extends TopLevelFolder {

	/**
	 * Instantiates a new data folder.
	 *
	 * @param root
	 *            the navigator root
	 * @param name
	 *            the display name shown in the navigator
	 */
	public DataFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_DATA, "Models related to data management", BLUE,
				ProjectCategory.DATA);
	}

}
