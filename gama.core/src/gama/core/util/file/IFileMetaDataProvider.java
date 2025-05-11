/*******************************************************************************************************
 *
 * IFileMetaDataProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import org.eclipse.core.resources.IResource;

/**
 * Class IFileMetaDataProvider.
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public interface IFileMetaDataProvider {

	/**
	 * Gets the meta data.
	 *
	 * @param element
	 *            the element
	 * @param includeOutdated
	 *            the include outdated
	 * @param immediately
	 *            the immediately
	 * @return the meta data
	 */
	IGamaFileMetaData getMetaData(Object element, boolean includeOutdated, boolean immediately);

	/**
	 * Store meta data.
	 *
	 * @param file
	 *            the file
	 * @param data
	 *            the data
	 * @param immediately
	 *            the immediately
	 */
	void storeMetaData(final IResource file, final IGamaFileMetaData data, final boolean immediately);

	/**
	 * Refresh (deletes and recreate) the metadata of all files in the workspace
	 */
	void refreshAllMetaData();

}
