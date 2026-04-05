/*******************************************************************************************************
 *
 * CacheLocationProvider.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

import gama.api.utils.files.FileUtils;

/**
 * Eclipse {@link PathVariableResolver} that provides the {@code CACHE_LOC} workspace path variable, pointing to the
 * GAMA download cache directory ({@code <workspace>/.cache}).
 *
 * <p>
 * This resolver is called by Eclipse's {@code AliasManager} during workspace startup — before GAMA itself is
 * initialized. It therefore computes the cache path directly from {@link ResourcesPlugin#getWorkspace()} rather than
 * going through {@code GAMA.getWorkspaceManager()}, which would be {@code null} at that point and cause a
 * {@link NullPointerException}.
 * </p>
 */
public class CacheLocationProvider extends PathVariableResolver {

	@Override
	public String[] getVariableNames(final String variable, final IResource resource) {
		return new String[] { FileUtils.CACHE_FOLDER_VARIABLE_NAME };
	}

	/**
	 * Returns the absolute URI string of the GAMA cache directory ({@code <workspace>/.cache}).
	 *
	 * <p>
	 * The path is derived directly from {@link ResourcesPlugin#getWorkspace()} so that this method is safe to call
	 * during workspace startup, before GAMA is initialized.
	 * </p>
	 *
	 * @param variable
	 *            the variable name being resolved (should be {@code CACHE_LOC})
	 * @param resource
	 *            the resource context (unused)
	 * @return the cache directory URI as an ASCII string, or {@code null} if the workspace root location is unavailable
	 */
	@Override
	public String getValue(final String variable, final IResource resource) {
		// Use ResourcesPlugin directly — GAMA may not be initialized yet when Eclipse
		// calls this during Workspace.startup() / AliasManager.buildLocationsMap().
		try {
			final org.eclipse.core.runtime.IPath wsLoc =
					ResourcesPlugin.getWorkspace().getRoot().getLocation();
			if (wsLoc == null) return null;
			return wsLoc.append(FileUtils.CACHE_FOLDER_PATH).toFile().toURI().toASCIIString();
		} catch (final Exception e) {
			return null;
		}
	}

}
