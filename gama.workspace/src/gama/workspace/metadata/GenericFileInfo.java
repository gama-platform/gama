/*******************************************************************************************************
 *
 * GenericFileInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import org.eclipse.core.resources.IFile;

import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.files.AbstractFileMetaData;

/**
 * The Class GenericFileInfo.
 */
public class GenericFileInfo extends AbstractFileMetaData {

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
	public GenericFileInfo(final IFile file, final String suffix) {
		super(file);
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
	public IGamlDocumentation getDocumentation() { return new GamlConstantDocumentation(suffix); }
}