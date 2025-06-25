/*******************************************************************************************************
 *
 * ModelLibraryValidator.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
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
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import com.google.common.collect.Multimap;
import com.google.inject.Injector;

import gama.dev.DEBUG;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.kernel.GamaBundleLoader;
import gama.headless.runtime.HeadlessApplication;
import gaml.compiler.gaml.validation.GamlModelBuilder; 

/**
 * The Class ModelLibraryValidator.
 */
public class ModelLibraryValidator extends AbstractModelLibraryRunner {

	/** The instance. */
	private static ModelLibraryValidator instance;

	/**
	 * Instantiates a new model library validator.
	 */
	private ModelLibraryValidator() {
		DEBUG.ON();
	}

	@Override
	public int start() throws IOException {

		final Injector injector = HeadlessApplication.getInjector();
		final GamlModelBuilder builder = createBuilder(injector);
		final int[] count = { 0 };
		final int[] code = { 0, 0 };

		this.validatePluginsFromURLs(GamaBundleLoader.getPluginsWithModels(), builder, count, code);

		DEBUG.OUT("" + count[0] + " GAMA models compiled in built-in library and plugins. " + code[0]
				+ " compilation errors found");

		code[1] = code[0];
		code[0] = 0;
		count[0] = 0;

		this.validatePluginsFromURLs(GamaBundleLoader.getPluginsWithTests(), builder, count, code);

		DEBUG.OUT("" + count[0] + " GAMA tests compiled in built-in library and plugins. " + code[0]
				+ " compilation errors found");
		DEBUG.OUT(code[0] + code[1]);
		return code[0] + code[1];
	}

	/**
	 * Validate plugins from UR ls.
	 *
	 * @param pluginsURLs
	 *            the plugins UR ls
	 * @param builder
	 *            the builder
	 * @param count
	 *            the count
	 * @param code
	 *            the code
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void validatePluginsFromURLs(final Multimap<Bundle, String> pluginsURLs, final GamlModelBuilder builder,
			final int[] count, final int[] code) throws IOException {
		List<URL> allURLs = new ArrayList<>();
		for (final Bundle bundle : pluginsURLs.keySet()) {
			for (final String entry : pluginsURLs.get(bundle)) {
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
		allURLs.forEach(u -> validate(builder, count, code, u));
	}

	/**
	 * Validate.
	 *
	 * @param builder
	 *            the builder
	 * @param countOfModelsValidated
	 *            the count of models validated
	 * @param returnCode
	 *            the return code
	 * @param pathToModel
	 *            the path to model
	 */
	private void validate(final GamlModelBuilder builder, final int[] countOfModelsValidated, final int[] returnCode,
			final URL pathToModel) {
		final List<GamlCompilationError> errors = new ArrayList<>();
		// log("Compiling " + pathToModel.getFile());
		builder.compile(pathToModel, errors);
		countOfModelsValidated[0]++;
		errors.stream().filter(GamlCompilationError::isError).forEach(e -> {
			// log("Error in " + e.getURI().lastSegment() + ": " + e);
			DEBUG.ERR("Error in " + e.getURI() + ":\n " + e.toString() + " \n " + e.getStatement().toString() + "\n");
			returnCode[0]++;
		});
	}

	/**
	 * Gets the single instance of ModelLibraryValidator.
	 *
	 * @return single instance of ModelLibraryValidator
	 */
	public static ModelLibraryValidator getInstance() {
		if (instance == null) { instance = new ModelLibraryValidator(); }
		return instance;
	}
}
