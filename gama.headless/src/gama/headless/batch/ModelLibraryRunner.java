/*******************************************************************************************************
 *
 * ModelLibraryRunner.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.batch;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import com.google.common.collect.Multimap;
import com.google.inject.Injector;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.model.IModel;
import gama.dev.DEBUG;
import gama.dev.STRINGS;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.kernel.GamaBundleLoader;
import gama.headless.core.Experiment;
import gama.headless.runtime.HeadlessApplication;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The Class ModelLibraryRunner.
 */
public class ModelLibraryRunner extends AbstractModelLibraryRunner {

	/** The instance. */
	private static ModelLibraryRunner instance;

	/**
	 * Instantiates a new model library runner.
	 */
	private ModelLibraryRunner() {
		DEBUG.ON();
	}

	@Override
	public int start() throws IOException {
		final Injector injector = HeadlessApplication.getInjector();
		final GamlModelBuilder builder = createBuilder(injector);
		final int[] count = { 0 };
		final int[] code = { 0, 0 };
		final Multimap<Bundle, String> plugins = GamaBundleLoader.getPluginsWithModels();
		final List<URL> allURLs = new ArrayList<>();
		for (final Bundle bundle : plugins.keySet()) {
			for (final String entry : plugins.get(bundle)) {
				final Enumeration<URL> urls = bundle.findEntries(entry, "*", true);
				if (urls != null) {
					while (urls.hasMoreElements()) {
						final URL url = urls.nextElement();
						if (isModel(url)) {
							final URL resolvedFileURL = FileLocator.toFileURL(url);
							allURLs.add(resolvedFileURL);
						}
					}
				}
			}
		}
		builder.loadURLs(allURLs);
		// allURLs.forEach(u -> validate(builder, count, code, u));
		final Map<String, Exception> errors = new HashMap<>();
		allURLs.forEach(u -> validateAndRun(builder, errors, count, code, u, true, 1));

		DEBUG.OUT("" + count[0] + " GAMA models compiled in built-in library and plugins. " + code[0]
				+ " compilation errors found");

		DEBUG.SECTION("SUMMARY");

		errors.forEach((name, ex) -> DEBUG.OUT(name + " = " + ex.toString()));

		DEBUG.SECTION("SUMMARY");

		return code[0] + code[1];
	}


	/**
	 * Validate and run.
	 *
	 * @param builder
	 *            the builder
	 * @param executionErrors
	 *            the execution errors
	 * @param countOfModelsValidated
	 *            the count of models validated
	 * @param returnCode
	 *            the return code
	 * @param pathToModel
	 *            the path to model
	 * @param expGUIOnly
	 *            the exp GUI only
	 * @param nbCycles
	 *            the nb cycles
	 */
	private void validateAndRun(final GamlModelBuilder builder, final Map<String, Exception> executionErrors,
			final int[] countOfModelsValidated, final int[] returnCode, final URL pathToModel, final boolean expGUIOnly,
			final int nbCycles) {
		if (pathToModel.toString().contains("Database")) {
			return;
		}
		STRINGS.PAD("", 80, '=');

		final List<GamlCompilationError> errors = new ArrayList<>();
		final IModel mdl = builder.compile(pathToModel, errors);

		countOfModelsValidated[0]++;
		errors.stream().filter(GamlCompilationError::isError).forEach(e -> {
			DEBUG.OUT("Error in " + e.getURI() + ":\n " + e.toString() + " \n " + e.getStatement().toString() + "\n");
			returnCode[0]++;
		});

		Experiment experiment = null;
		try {
			experiment = new Experiment(mdl);
		} catch (final Exception ex) {
			executionErrors.put(pathToModel.getPath() + "\n", ex);
			// AD ? if (experiment != null) { experiment.dispose(); }
		}

		for (final String expName : mdl.getDescription().getExperimentNames()) {
			final IExperimentPlan exp = mdl.getExperiment(expName);
			if (!exp.isBatch() || !expGUIOnly) {
				DEBUG.OUT("*********** Run experiment " + exp + " from model: " + mdl.getName());
				if (experiment != null) {
					try {
						experiment.setup(expName, 0.1);
						for (int i = 0; i < nbCycles; i++) {
							experiment.step();
							DEBUG.OUT("****** Ap step()");
						}
					} catch (final gama.dependencies.webb.WebbException ex1) {
						DEBUG.OUT("gama.dependencies.webb.WebbException");
					} catch (final Exception ex) {
						ex.printStackTrace();
						executionErrors.put(pathToModel.getPath() + "\n" + expName, ex);
					}
				}
			}
		}

	}

	/**
	 * Gets the single instance of ModelLibraryRunner.
	 *
	 * @return single instance of ModelLibraryRunner
	 */
	public static ModelLibraryRunner getInstance() {
		if (instance == null) { instance = new ModelLibraryRunner(); }
		return instance;
	}
}