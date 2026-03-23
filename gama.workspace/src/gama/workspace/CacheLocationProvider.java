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
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

import gama.api.utils.files.FileUtils;

/**
 * The Class CacheLocationProvider.
 */
public class CacheLocationProvider extends PathVariableResolver {

	@Override
	public String[] getVariableNames(final String variable, final IResource resource) {
		return new String[] { FileUtils.CACHE_FOLDER_VARIABLE_NAME };
	}

	@Override
	public String getValue(final String variable, final IResource resource) {
		return FileUtils.getCache().toURI().toASCIIString();
	}

}
