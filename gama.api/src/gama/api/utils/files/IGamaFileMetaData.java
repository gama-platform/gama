/*******************************************************************************************************
 *
 * IGamaFileMetaData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import gama.api.compilation.documentation.IGamlDocumentation;

/**
 * Class IGamaFileInfo. Provides meta-information on files (like crs and envelope for shapefiles, number of rows/columns
 * for csv files, etc. Used in the UI for decorating files in the navigator. Will be used also to accelerate the loading
 * of files in GAMA, when it can be retrieved ( i.e. when we are in a workspace context).
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public interface IGamaFileMetaData {

	/**
	 * A delimiter character for separating attributes in the property string
	 */
	String DELIMITER = "_!_";

	/** The Constant SUB_DELIMITER. */
	String SUB_DELIMITER = "@%@";

	/** The Constant FAILED. */
	String FAILED = "failed";

	/** The Constant SUFFIX_DEL. */
	String SUFFIX_DEL = " | ";

	/**
	 * Gets the modification stamp.
	 *
	 * @return the modification stamp
	 */
	default long getModificationStamp() { return 0; }

	/**
	 * Indicates a failure in the computation of metadata
	 *
	 * @return
	 */
	default boolean hasFailed() {
		return false;
	}

	/**
	 * Never returns null
	 *
	 * @return the suffix to use for decorating files in the navigator
	 */
	default String getSuffix() {
		final StringBuilder sb = new StringBuilder();
		appendSuffix(sb);
		return sb.toString();
	}

	/**
	 * Append suffix.
	 *
	 * @param sb
	 *            the sb
	 */
	default void appendSuffix(final StringBuilder sb) {}

	/**
	 * Returns a thumbnail (imageDescriptor or anything else) or null if no image are provided
	 *
	 * @return an image (ImageDescriptor, BufferedImage, ...) or null
	 */
	default Object getThumbnail() { return null; }

	/**
	 * Returns a string that can be stored in the metadata part of the workspace. The implementing classes should also
	 * allow instantiating with this string as an input
	 *
	 * @return a string describing completely the attributes of this metadata or null
	 */
	default String toPropertyString() {
		return "";
	}

	/**
	 * Returns a string that can be displayed in hover info
	 *
	 * @return
	 */
	default IGamlDocumentation getDocumentation() { return IGamlDocumentation.EMPTY_DOC; }

	/**
	 * @param modificationStamp
	 */
	default void setModificationStamp(final long modificationStamp) {}

}
