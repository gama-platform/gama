/*******************************************************************************************************
 *
 * TutorialsFolder.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import gama.workspace.nature.GamaNatures;

/**
 * The Class TutorialsFolder.
 *
 * <p>
 * Virtual top-level folder that aggregates all tutorial projects in the GAMA Navigator under the label "Tutorials". A
 * tutorial project is one that bears {@link GamaNatures#TUTORIAL_NATURE} in its Eclipse project description.
 * </p>
 *
 * <p>
 * To contribute tutorials, a plugin bundle should:
 * </p>
 * <ol>
 * <li>Create a {@code tutorials/} sub-directory at the root of the bundle.</li>
 * <li>Place one or more GAMA sub-projects inside that directory, each with a {@code .project} file that declares
 * {@code gama.workspace.gamaNature}, {@code org.eclipse.xtext.ui.shared.xtextNature}, and
 * {@code gama.workspace.tutorialNature}.</li>
 * </ol>
 * <p>
 * The platform will automatically detect and import those projects at startup and place them in this folder.
 * </p>
 */
public class TutorialsFolder extends TopLevelFolder {

	/**
	 * Instantiates a new tutorials folder.
	 *
	 * @param root
	 *            the root navigator root
	 * @param name
	 *            the display name shown in the navigator (typically "Tutorials")
	 */
	public TutorialsFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_TUTORIALS, "Step-by-step tutorials shipped with GAMA plugins", BLUE,
				GamaNatures.TUTORIAL_NATURE, Location.Tutorials);
	}

}
