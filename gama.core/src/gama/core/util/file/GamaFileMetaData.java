/*******************************************************************************************************
 *
 * GamaFileMetaData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import gama.dev.DEBUG;
import gama.gaml.interfaces.IGamlDescription;

/**
 * Class GamaFileMetaInformation.
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public abstract class GamaFileMetaData implements IGamaFileMetaData {

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
	public GamaFileMetaData(final long stamp) {
		this.fileModificationStamp = stamp;
	}

	/**
	 * From.
	 *
	 * @param <T>
	 *            the generic type
	 * @param s
	 *            the s
	 * @param stamp
	 *            the stamp
	 * @param clazz
	 *            the clazz
	 * @param includeOutdated
	 *            the include outdated
	 * @return the t
	 */
	public static <T extends IGamaFileMetaData> T from(final String s, final long stamp, final Class<T> clazz,
			final boolean includeOutdated) {
		T result = null;
		try {
			final Constructor<T> c = clazz.getDeclaredConstructor(String.class);
			result = c.newInstance(s);
			final boolean hasFailed = result.hasFailed();
			if (!hasFailed && !includeOutdated && result.getModificationStamp() != stamp) return null;
		} catch (final Exception ignore) {
			DEBUG.ERR("Error loading metadata " + s + " : " + ignore.getClass().getSimpleName() + ":"
					+ ignore.getMessage());
			if (ignore instanceof InvocationTargetException && ignore.getCause() != null) {
				ignore.getCause().printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Instantiates a new gama file meta data.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public GamaFileMetaData(final String propertyString) {
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
	 * @see gama.core.util.file.IGamaFileMetaData#toPropertyString()
	 */

	@Override
	public String toPropertyString() {
		if (hasFailed) return FAILED;
		return String.valueOf(fileModificationStamp);
	}

	@Override
	public void setModificationStamp(final long ms) { fileModificationStamp = ms; }

	/**
	 * @return
	 */
	public Consumer<IGamlDescription> getContextualAction() { // TODO Auto-generated method stub
		return null;
	}

}
