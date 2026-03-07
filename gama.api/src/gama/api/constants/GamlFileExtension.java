/*******************************************************************************************************
 *
 * GamlFileExtension.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.constants;

import gama.annotations.constants.IKeyword;

/**
 * Utility class providing constants and helper methods for GAML file extensions.
 * <p>
 * This class defines the standard file extensions used in the GAMA platform for different types of GAML files, and
 * provides utility methods to check if a file name matches any of these extensions.
 * </p>
 *
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
public class GamlFileExtension {

	/** The Constant GAML_FILE. */
	public final static String GAML_FILE = ".gaml";

	/** The Constant EXPERIMENT_FILE. */
	public final static String EXPERIMENT_FILE = "." + IKeyword.EXPERIMENT;

	/** The Constant MODEL_FILE. */
	public final static String MODEL_FILE = "." + IKeyword.MODEL;

	/** The Constant SPECIES_FILE. */
	public final static String SPECIES_FILE = "." + IKeyword.SPECIES;

	/**
	 * Checks if is gaml.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if is gaml
	 */
	public static final boolean isGaml(final String fileName) {
		return fileName != null && fileName.endsWith(GAML_FILE);
	}

	/**
	 * Checks if is experiment.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if is experiment
	 */
	public static final boolean isExperiment(final String fileName) {
		return fileName != null && fileName.endsWith(EXPERIMENT_FILE);
	}

	/**
	 * Checks if is model.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if is model
	 */
	public static final boolean isModel(final String fileName) {
		return fileName != null && fileName.endsWith(MODEL_FILE);
	}

	/**
	 * Checks if is species.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if is species
	 */
	public static final boolean isSpecies(final String fileName) {
		return fileName != null && fileName.endsWith(SPECIES_FILE);
	}

	/**
	 * Checks if is any.
	 *
	 * @param fileName
	 *            the file name
	 * @return true, if is any
	 */
	public static final boolean isAny(final String fileName) {
		return isGaml(fileName) || isExperiment(fileName) || isModel(fileName) || isSpecies(fileName);
	}
}
