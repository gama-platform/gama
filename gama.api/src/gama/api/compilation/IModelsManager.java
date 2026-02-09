/*******************************************************************************************************
 *
 * IModelsManager.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation;

import java.util.Collections;
import java.util.List;

import gama.api.utils.files.IGamlFileInfo;
import gama.api.utils.tests.TestExperimentSummary;

/**
 * The Interface IModelsManager.
 */
public interface IModelsManager {

	/**
	 * Edits the model.
	 *
	 * @param eObject
	 *            the e object
	 */
	default void editModel(final Object eObject) {}

	/**
	 * Run model.
	 *
	 * @param object
	 *            the object
	 * @param exp
	 *            the exp
	 */
	default void runModel(final Object object, final String exp) {}

	/**
	 * Run headless tests.
	 *
	 * @param model
	 *            the model
	 * @return the list
	 */
	default List<TestExperimentSummary> runHeadlessTests(final Object model) {
		return Collections.emptyList();
	}

	/**
	 * Gets the all models.
	 *
	 * @return the all models
	 */
	default List<IGamlFileInfo> getAllModels() { return Collections.emptyList(); }

}
