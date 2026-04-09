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
 * Defines the nature identifiers used in GAMA workspace projects. These natures help Eclipse identify and configure
 * different types of GAMA projects with appropriate tooling and build configurations.
 *
 * <p>
 * The following natures are defined and correspond to the virtual folders displayed in the GAMA Navigator:
 * </p>
 * <ul>
 * <li>{@link #GAMA_NATURE} – base nature shared by all GAMA projects</li>
 * <li>{@link #XTEXT_NATURE} – XText builder nature added to all GAMA projects</li>
 * <li>{@link #BUILTIN_NATURE} – projects shipped with the core GAMA library ("Library" folder)</li>
 * <li>{@link #PLUGIN_NATURE} – projects contributed by additional Eclipse plugins ("Library" folder)</li>
 * <li>{@link #TEST_NATURE} – built-in test suites ("Tests" folder)</li>
 * <li>{@link #TUTORIAL_NATURE} – step-by-step tutorial projects ("Tutorials" folder)</li>
 * <li>{@link #RECIPE_NATURE} – focused single-feature demonstrations ("Recipes" folder)</li>
 * </ul>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @version 2026-04
 */
public final class GamaNatures {

	/** The nature identifier for standard GAMA modeling projects. */
	public static final String GAMA_NATURE = "gama.workspace.gamaNature";

	/** The nature identifier for XText language support. */
	public static final String XTEXT_NATURE = "org.eclipse.xtext.ui.shared.xtextNature";

	/** The nature identifier for GAMA plugin development projects (appears in the "Library" virtual folder). */
	public static final String PLUGIN_NATURE = "gama.workspace.pluginNature";

	/** The nature identifier for GAMA test projects (appears in the "Tests" virtual folder). */
	public static final String TEST_NATURE = "gama.workspace.testNature";

	/** The nature identifier for built-in GAMA library projects (appears in the "Library" virtual folder). */
	public static final String BUILTIN_NATURE = "gama.workspace.builtinNature";

	/**
	 * The nature identifier for GAMA tutorial projects (appears in the "Tutorials" virtual folder). Plugins wishing to
	 * contribute tutorials should place their tutorial sub-projects inside a {@code tutorials/} subdirectory at the root
	 * of the plugin bundle and declare this nature in the sub-project's {@code .project} file.
	 */
	public static final String TUTORIAL_NATURE = "gama.workspace.tutorialNature";

	/**
	 * The nature identifier for GAMA recipe projects (appears in the "Recipes" virtual folder). A recipe is a focused,
	 * self-contained demonstration of a specific GAML feature or modeling pattern. Plugins wishing to contribute
	 * recipes should place their recipe sub-projects inside a {@code recipes/} subdirectory at the root of the plugin
	 * bundle and declare this nature in the sub-project's {@code .project} file.
	 */
	public static final String RECIPE_NATURE = "gama.workspace.recipeNature";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private GamaNatures() {
		// Utility class - no instances allowed
	}

}
