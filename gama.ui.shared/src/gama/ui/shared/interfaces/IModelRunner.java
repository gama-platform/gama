/*******************************************************************************************************
 *
 * IModelRunner.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

import java.util.List;

import gama.gaml.statements.test.TestExperimentSummary;

/**
 * The Interface IModelRunner.
 */
public interface IModelRunner {

	/**
	 * Edits the model.
	 *
	 * @param eObject the e object
	 */
	void editModel(Object eObject);

	/**
	 * Run model.
	 *
	 * @param object the object
	 * @param exp the exp
	 */
	void runModel(Object object, String exp);

	/**
	 * Run headless tests.
	 *
	 * @param model the model
	 * @return the list
	 */
	List<TestExperimentSummary> runHeadlessTests(Object model);

}
