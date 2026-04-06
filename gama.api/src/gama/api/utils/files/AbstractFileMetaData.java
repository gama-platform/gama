/*******************************************************************************************************
 *
 * AbstractFileMetaData.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import java.io.File;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IResource;

import gama.api.compilation.descriptions.IGamlDescription;

/**
 * Class GamaFileMetaInformation.
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public abstract class AbstractFileMetaData implements IGamaFileMetaData {

	/**
	 * The IResource modification stamp of the corresponding file at the time the cache entry was loaded.
	 */
	public long fileModificationStamp;

	/** The has failed. */
	boolean hasFailed;

	/**
	 * Instantiates a new gama file meta data.
	 *
	 * @param stamp
	 *            the stamp
	 */
	public AbstractFileMetaData(final IResource r) {
		fileModificationStamp = r == null ? 0 : r.getModificationStamp();
	}

	/**
	 * Instantiates a new abstract file meta data.
	 *
	 * @param f
	 *            the f
	 */
	public AbstractFileMetaData(final File f) {
		fileModificationStamp = f == null ? 0 : f.lastModified();
	}

	/**
	 * Instantiates a new gama file meta data.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public AbstractFileMetaData(final String propertyString) {
		final String s = StringUtils.substringBefore(propertyString, DELIMITER);
		if (FAILED.equals(s)) {
			hasFailed = true;
		} else if (s == null || s.isEmpty()) {
			fileModificationStamp = 0;
		} else {
			fileModificationStamp = Long.parseLong(s);
		}
	}

	@Override
	public boolean hasFailed() {
		return hasFailed;
	}

	/**
	 * Split.
	 *
	 * @param s
	 *            the s
	 * @return the string[]
	 */
	protected String[] split(final String s) {
		return StringUtils.splitByWholeSeparatorPreserveAllTokens(s, DELIMITER);
	}

	/**
	 * Method getModificationStamp()
	 *
	 * @see gama.core.util.file.IGamaFileInfo#getModificationStamp()
	 */
	@Override
	public long getModificationStamp() { return fileModificationStamp; }

	@Override
	public Object getThumbnail() { return null; }

	/**
	 * Subclasses should extend ! Method toPropertyString()
	 *
	 * @see gama.api.utils.files.IGamaFileMetaData#toPropertyString()
	 */

	@Override
	public String toPropertyString() {
		if (hasFailed) return FAILED;
		return String.valueOf(fileModificationStamp);
	}

	@Override
	public void setModificationStamp(final long ms) { fileModificationStamp = ms; }

	/**
	 * Sets the failed.
	 *
	 * @param failed
	 *            the new failed
	 */
	public void setFailed(final boolean failed) { this.hasFailed = failed; }

	/**
	 * @return
	 */
	public Consumer<IGamlDescription> getContextualAction() { return null; }

}