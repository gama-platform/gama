/*******************************************************************************************************
 *
 * TestModelsFolder.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import gama.api.utils.tests.CompoundSummary;
import gama.workspace.manager.ProjectCategory;

/**
 * The Class TestModelsFolder: the test projects contributed by GAMA and its plugins (in {@code models/Tests}) and the
 * test projects of the user, located in the {@code Tests} directory of the workspace. This directory only exists on
 * disk: the "Tests" folder of the navigator is virtual and displays both kinds of projects.
 */
public class TestModelsFolder extends TopLevelFolder {

	/**
	 * Instantiates a new test models folder.
	 *
	 * @param root
	 *            the root
	 * @param name
	 *            the name
	 */
	public TestModelsFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_TEST, "Tests of GAMA and its plugins, and tests of the user", NEUTRAL,
				ProjectCategory.TESTS);
	}

	@Override
	public void getSuffix(final StringBuilder sb) {
		final CompoundSummary<?, ?> summary = getManager().getTestsSummary();
		if (summary != null) {
			sb.append(summary.getStringSummary());
		} else {
			super.getSuffix(sb);
			sb.append(", not yet run");
		}

	}

}
