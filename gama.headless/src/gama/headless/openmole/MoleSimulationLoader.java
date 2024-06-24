/*******************************************************************************************************
 *
 * MoleSimulationLoader.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.openmole;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gama.annotations.precompiler.GamlProperties;
import gama.core.kernel.model.IModel;
import gama.gaml.compilation.GamaCompilationFailedException;
import gama.gaml.compilation.GamlCompilationError;
import gama.headless.core.GamaHeadlessException;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The Class MoleSimulationLoader.
 */
public abstract class MoleSimulationLoader {


	/**
	 * Load model.
	 *
	 * @param modelPath
	 *            the model path
	 * @param errors
	 *            the errors
	 * @return the i model
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 */
	public static IModel loadModel(final File modelPath, final List<GamlCompilationError> errors)
			throws IOException, GamaCompilationFailedException {
		return loadModel(modelPath, errors, null);
	}

	/**
	 * Load model.
	 *
	 * @param modelPath
	 *            the model path
	 * @param errors
	 *            the errors
	 * @param metadata
	 *            the metadata
	 * @return the i model
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 */
	public static IModel loadModel(final File modelPath, final List<GamlCompilationError> errors,
			final GamlProperties metadata) throws IOException, GamaCompilationFailedException {
		return GamlModelBuilder.getDefaultInstance().compile(modelPath, errors, metadata);
	}

	/**
	 * New experiment.
	 *
	 * @param model
	 *            the model
	 * @return the i mole experiment
	 */
	public static IMoleExperiment newExperiment(final IModel model) {
		return new MoleExperiment(model);
	}

}
