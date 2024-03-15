/*******************************************************************************************************
 *
 * CacheLocationProvider.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util.file;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;

import gama.core.common.util.FileUtils;

/**
 * The Class CacheLocationProvider.
 */
public class CacheLocationProvider extends PathVariableResolver {

	/** The name. */
	public static final String NAME = "CACHE_LOC";

	@Override
	public String[] getVariableNames(final String variable, final IResource resource) {
		return new String[] { NAME };
	}

	@Override
	public String getValue(final String variable, final IResource resource) {
		return FileUtils.CACHE.toURI().toASCIIString();
	}

}
