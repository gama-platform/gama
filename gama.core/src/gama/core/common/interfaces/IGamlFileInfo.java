/*******************************************************************************************************
 *
 * IGamlFileInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.util.Collection;

import gama.core.util.file.IGamaFileMetaData;
import gama.gaml.interfaces.IGamlDescription;

/**
 *
 */
public interface IGamlFileInfo extends IGamlDescription, IGamaFileMetaData {

	/**
	 * Gets the documentation.
	 *
	 * @return the documentation
	 */
	@Override
	default Doc getDocumentation() { return IGamaFileMetaData.super.getDocumentation(); }

	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	boolean isValid();

	/**
	 * Gets the imports.
	 *
	 * @return the imports
	 */
	Collection<String> getImports();

	/**
	 * Gets the uses.
	 *
	 * @return the uses
	 */
	Collection<String> getUses();

	/**
	 * Gets the tags.
	 *
	 * @return the tags
	 */
	Collection<String> getTags();

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	Collection<String> getExperiments();

}
