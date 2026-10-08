/*******************************************************************************************************
 *
 * ModelsLibraryFolder.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;

import gama.ui.shared.resources.IGamaColors;
import gama.workspace.nature.GamaNatures;

/**
 * The Class ModelsLibraryFolder.
 *
 * <p>
 * Virtual top-level folder that aggregates <em>all</em> built-in and plugin model projects under a single "Library"
 * entry in the GAMA Navigator. It replaces the former separation between "Library models" (projects bearing
 * {@link GamaNatures#BUILTIN_NATURE}) and "Plugin models" (projects bearing {@link GamaNatures#PLUGIN_NATURE}): both
 * categories are now shown together here.
 * </p>
 *
 * <p>
 * For <em>open</em> projects the nature check is used: a project is accepted if it declares either
 * {@code builtinNature} or {@code pluginNature}. For <em>closed</em> projects the physical location is used via
 * {@link #estimateLocation(IPath)}: paths that resolve to {@link Location#CoreModels} or {@link Location#Plugins} are
 * both mapped to {@link Location#CoreModels} so that the parent-class check
 * ({@code estimateLocation(...) == location}) still succeeds.
 * </p>
 */
public class ModelsLibraryFolder extends TopLevelFolder {

	/**
	 * Instantiates a new models library folder.
	 *
	 * @param root
	 *            the root navigator root
	 * @param name
	 *            the display name shown in the navigator
	 */
	public ModelsLibraryFolder(final NavigatorRoot root, final String name) {
		super(root, name, FOLDER_BUILTIN, "Models shipped with GAMA and its plugins", IGamaColors.BLUE,
				GamaNatures.BUILTIN_NATURE, Location.CoreModels);
	}

	/**
	 * Accepts a project if it has either {@code builtinNature} or {@code pluginNature}.
	 *
	 * @param desc
	 *            the project description to test
	 * @return {@code true} if the project bears a library or plugin nature
	 */
	@Override
	public boolean accepts(final IProjectDescription desc) {
		return desc.hasNature(GamaNatures.BUILTIN_NATURE) || desc.hasNature(GamaNatures.PLUGIN_NATURE);
	}

	/**
	 * Overrides location estimation to map {@link Location#Plugins} back to {@link Location#CoreModels} so that closed
	 * plugin-model projects are also accepted by this folder.
	 *
	 * @param location
	 *            the OS path of the closed project
	 * @return the estimated location, with {@code Plugins} remapped to {@code CoreModels}
	 */
	@Override
	protected Location estimateLocation(final IPath location) {
		final Location result = super.estimateLocation(location);
		return result == Location.Plugins ? Location.CoreModels : result;
	}

}
