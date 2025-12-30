/**
 * 
 */
package gama.workspace.metadata;

import gama.gaml.interfaces.IGamlDescription.ConstantDoc;
import gama.gaml.interfaces.IGamlDescription.Doc;

/**
 * The Class GenericFileInfo.
 */
public class GenericFileInfo extends GamaFileMetaData {

	/** The suffix. */
	final String suffix;

	/**
	 * Instantiates a new generic file info.
	 *
	 * @param stamp
	 *            the stamp
	 * @param suffix
	 *            the suffix
	 */
	public GenericFileInfo(final long stamp, final String suffix) {
		super(stamp);
		this.suffix = suffix;
	}

	/**
	 * Instantiates a new generic file info.
	 *
	 * @param propertiesString
	 *            the properties string
	 */
	public GenericFileInfo(final String propertiesString) { // NO_UCD (unused code)
		super(propertiesString);
		final String[] segments = split(propertiesString);
		suffix = segments[1];
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		if (suffix != null) { sb.append(suffix); }
	}

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + suffix;
	}

	@Override
	public Doc getDocumentation() { return new ConstantDoc(suffix); }
}