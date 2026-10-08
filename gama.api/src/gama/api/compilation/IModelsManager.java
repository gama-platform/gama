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
 * The IModelsManager interface defines the contract for managing GAML model lifecycle and operations.
 * 
 * <p>This interface provides high-level model management operations including editing, running, testing,
 * and discovery of GAML models. It serves as an abstraction layer between the GAMA platform core and
 * UI or headless execution environments.</p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>Implementations of this interface handle:</p>
 * <ul>
 *   <li><strong>Model Editing:</strong> Opening models in appropriate editors</li>
 *   <li><strong>Model Execution:</strong> Running models with specified experiments</li>
 *   <li><strong>Model Testing:</strong> Running test suites in headless mode</li>
 *   <li><strong>Model Discovery:</strong> Finding and listing available models</li>
 * </ul>
 * 
 * <h2>Default Implementation</h2>
 * 
 * <p>All methods provide default no-op or empty implementations, allowing implementations to
 * override only the methods relevant to their execution context (UI, headless, server, etc.).</p>
 * 
 * <h2>Usage Contexts</h2>
 * 
 * <ul>
 *   <li><strong>GUI Mode:</strong> Full implementation with editor integration and UI feedback</li>
 *   <li><strong>Headless Mode:</strong> Minimal implementation focused on model execution</li>
 *   <li><strong>Server Mode:</strong> Remote model management and execution</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IModelsManager manager = GAMA.getModelManager();
 * 
 * // Open a model for editing
 * manager.editModel(modelEObject);
 * 
 * // Run a specific experiment
 * manager.runModel(modelObject, "my_experiment");
 * 
 * // Run all tests in headless mode
 * List<TestExperimentSummary> results = manager.runHeadlessTests(model);
 * 
 * // Get all available models
 * List<IGamlFileInfo> models = manager.getAllModels();
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.GAMA
 * @see gama.api.utils.files.IGamlFileInfo
 * @see gama.api.utils.tests.TestExperimentSummary
 */
public interface IModelsManager {

	/**
	 * Opens the specified model for editing in the appropriate editor.
	 * 
	 * <p>In GUI mode, this typically opens the model file in the GAML editor.
	 * In headless mode, this is a no-op.</p>
	 * 
	 * <p><b>Default Implementation:</b> Does nothing. Override to provide editing functionality.</p>
	 *
	 * @param eObject the EMF EObject representing the model or model element to edit.
	 *                Typically a GamlResource or ModelDescription
	 */
	default void editModel(final Object eObject) {}

	/**
	 * Runs the specified model with the given experiment.
	 * 
	 * <p>This method initiates model execution, typically creating an experiment controller
	 * and starting the simulation. In GUI mode, this may open experiment views. In headless
	 * mode, this runs the experiment to completion.</p>
	 * 
	 * <p><b>Default Implementation:</b> Does nothing. Override to provide execution functionality.</p>
	 *
	 * @param object the model object to run. Can be a file path, URI, or model object
	 * @param exp the name of the experiment to run. Must match an experiment defined in the model
	 */
	default void runModel(final Object object, final String exp) {}

	/**
	 * Runs all test experiments defined in the specified model in headless mode.
	 * 
	 * <p>This method executes all experiments marked as tests in the model and collects
	 * their results. It's primarily used for automated testing and continuous integration.</p>
	 * 
	 * <p><b>Default Implementation:</b> Returns an empty list. Override to provide testing functionality.</p>
	 *
	 * @param model the model containing test experiments. Can be a file path, URI, or model object
	 * @return a list of {@link TestExperimentSummary} objects, one for each test experiment,
	 *         containing test results (passed/failed assertions, execution time, etc.).
	 *         Returns an empty list if no tests are found or in the default implementation
	 */
	default List<TestExperimentSummary> runHeadlessTests(final Object model) {
		return Collections.emptyList();
	}

	/**
	 * Retrieves information about all GAML models available in the workspace.
	 * 
	 * <p>This method scans the workspace and returns metadata about all .gaml model files,
	 * including their location, experiments, and other properties.</p>
	 * 
	 * <p><b>Default Implementation:</b> Returns an empty list. Override to provide model discovery.</p>
	 *
	 * @return a list of {@link IGamlFileInfo} objects describing each model file,
	 *         or an empty list if no models are found or in the default implementation
	 */
	default List<IGamlFileInfo> getAllModels() { return Collections.emptyList(); }

}
