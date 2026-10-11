/*******************************************************************************************************
 *
 * GamaNatures.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.nature;

/**
 * Defines the nature identifiers used in GAMA workspace projects.
 *
 * <p>
 * Only two natures are used: {@link #GAMA_NATURE}, shared by all GAMA projects, and {@link #XTEXT_NATURE}, which
 * enables the GAML editor and builder. The classification of the projects in the top-level folders of the navigator
 * (Data, Demos, Library, Recipes, Tutorials, Tests, User) does not rely on natures but on their location (see
 * {@link gama.workspace.manager.ProjectCategory}).
 * </p>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @version 2026-04
 */
public final class GamaNatures {

	/** The nature identifier for standard GAMA modeling projects. */
	public static final String GAMA_NATURE = "gama.workspace.nature";

	/**
	 * The former identifiers of the GAMA nature. They are still recognised, and replaced by {@link #GAMA_NATURE} when
	 * a project is loaded in a workspace.
	 */
	public static final String[] LEGACY_GAMA_NATURES =
			{ "gama.workspace.gamaNature", "msi.gama.application.gamaNature" };

	/** The nature identifier for XText language support. */
	public static final String XTEXT_NATURE = "org.eclipse.xtext.ui.shared.xtextNature";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	/**
	 * Tells whether a project description declares the GAMA nature, under its current or one of its former identifiers.
	 */
	public static boolean isGamaProject(final org.eclipse.core.resources.IProjectDescription description) {
		for (final String id : description.getNatureIds()) {
			if (GAMA_NATURE.equals(id) || isLegacy(id)) return true;
		}
		return false;
	}

	/** Tells whether the nature identifier is a former identifier of the GAMA nature. */
	public static boolean isLegacy(final String id) {
		for (final String legacy : LEGACY_GAMA_NATURES) {
			if (legacy.equals(id)) return true;
		}
		return false;
	}

	/**
	 * Returns the nature identifiers with the former GAMA nature identifiers replaced by the current one (without
	 * duplicates). The array is returned unchanged if it contains no former identifier.
	 */
	public static String[] migrate(final String[] ids) {
		final java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
		boolean changed = false;
		for (final String id : ids) {
			if (isLegacy(id)) {
				result.add(GAMA_NATURE);
				changed = true;
			} else {
				result.add(id);
			}
		}
		return changed ? result.toArray(new String[0]) : ids;
	}

	private GamaNatures() {
		// Utility class - no instances allowed
	}

}
