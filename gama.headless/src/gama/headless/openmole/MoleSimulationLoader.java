/*******************************************************************************************************
 *
 * MoleSimulationLoader.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.openmole;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gama.api.compilation.GamlCompilationError;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.GamlProperties;
import gama.headless.core.GamaHeadlessException;
import gaml.compiler.validation.GamlModelBuilder;

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
	public static IModelSpecies loadModel(final File modelPath, final List<GamlCompilationError> errors)
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
	public static IModelSpecies loadModel(final File modelPath, final List<GamlCompilationError> errors,
			final GamlProperties metadata) throws IOException, GamaCompilationFailedException {
		return GamlModelBuilder.getInstance().compile(modelPath, errors, metadata);
	}

	/**
	 * New experiment.
	 *
	 * @param model
	 *            the model
	 * @return the i mole experiment
	 */
	public static IMoleExperiment newExperiment(final IModelSpecies model) {
		return new MoleExperiment(model);
	}

}
