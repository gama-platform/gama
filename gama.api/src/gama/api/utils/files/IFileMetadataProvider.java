/*******************************************************************************************************
 *
 * IFileMetadataProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import org.eclipse.core.resources.IResource;

/**
 * Class IFileMetadataProvider.
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public interface IFileMetadataProvider {

	/** The text ct id. */
	String TEXT_CT_ID = "org.eclipse.core.runtime.text";

	/** The Constant CSV_CT_ID. */
	String CSV_CT_ID = "gama.csv.file.type";

	/** The Constant IMAGE_CT_ID. */
	String IMAGE_CT_ID = "gama.images.file.type";

	/** The Constant GAML_CT_ID. */
	String GAML_CT_ID = "gama.gaml.file.type";

	/** The Constant SHAPEFILE_CT_ID. */
	String SHAPEFILE_CT_ID = "gama.shapefile.type";

	/** The Constant OSM_CT_ID. */
	String OSM_CT_ID = "gama.osm.file.type";

	/** The Constant SHAPEFILE_SUPPORT_CT_ID. */
	String SHAPEFILE_SUPPORT_CT_ID = "gama.shapefile.support.type";

	/** The Constant GSIM_CT_ID. */
	String GSIM_CT_ID = "gama.gsim.file.type";

	/** The Constant SVG_CT_ID. */
	String SVG_CT_ID = "gama.svg.file.type";

	/** The Constant JSON_CT_ID. */
	String JSON_CT_ID = "gama.json.file.type";

	/** The Constant GML_CT_ID. */
	String GML_CT_ID = "gama.gml.file.type";

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
	default void storeMetaData(final IResource file, final IGamaFileMetaData data, final boolean immediately) {}

	/**
	 * Refresh (deletes and recreate) the metadata of all files in the workspace
	 */
	default void refreshAllMetaData() {}

	/**
	 * @param attribute
	 * @param class1
	 */
	default void registerMetadataClass(final String attribute, final Class<? extends IGamaFileMetaData> class1) {}

}
