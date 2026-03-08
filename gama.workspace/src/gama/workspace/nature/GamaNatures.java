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
 * These natures help Eclipse identify and configure different types of GAMA projects
 * with appropriate tooling and build configurations.
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @version 2025-03
 */
public final class GamaNatures {

	/** The nature identifier for standard GAMA modeling projects */
	public static final String GAMA_NATURE = "gama.workspace.gamaNature";

	/** The nature identifier for XText language support */
	public static final String XTEXT_NATURE = "org.eclipse.xtext.ui.shared.xtextNature";

	/** The nature identifier for GAMA plugin development projects */
	public static final String PLUGIN_NATURE = "gama.workspace.pluginNature";

	/** The nature identifier for GAMA test projects */
	public static final String TEST_NATURE = "gama.workspace.testNature";

	/** The nature identifier for built-in GAMA library projects */
	public static final String BUILTIN_NATURE = "gama.workspace.builtinNature";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private GamaNatures() {
		// Utility class - no instances allowed
	}

}
