/*******************************************************************************************************
 *
 * GamlModelBuilder.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.validation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.SynchronizedXtextResourceSet;

import com.google.common.collect.Iterables;
import com.google.inject.Injector;

import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IGamlModelBuilder;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.GamlProperties;
import gama.dev.DEBUG;
import gaml.compiler.resource.GamlResource;
import one.util.streamex.StreamEx;

/**
 * Class GamlModelBuilder - Responsible for compiling GAML models from various sources.
 *
 * <p>
 * This class provides the main entry point for compiling GAML source files into executable models. It handles resource
 * loading, syntactic and semantic validation, and model description building.
 * </p>
 *
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe. All public compile methods are synchronized to ensure safe access to
 * the shared {@code buildResourceSet}. The singleton instance uses double-checked locking with a volatile field to
 * guarantee safe initialization in multi-threaded environments.
 * </p>
 *
 * <p>
 * <b>Singleton Pattern:</b> Use {@link #getInstance()} to obtain the default instance. A custom instance can be created
 * via {@link #GamlModelBuilder(Injector)} for dependency injection scenarios.
 * </p>
 *
 * @author drogoul
 * @since 8 avr. 2014
 *
 */
public class GamlModelBuilder implements IGamlModelBuilder {

	/** The default INSTANCE. */
	private static volatile GamlModelBuilder INSTANCE;

	/**
	 * Gets the default INSTANCE using double-checked locking for thread safety.
	 *
	 * @return the default INSTANCE
	 */
	public static GamlModelBuilder getInstance() {
		if (INSTANCE == null) {
			synchronized (GamlModelBuilder.class) {
				if (INSTANCE == null) { INSTANCE = new GamlModelBuilder(); }
			}
		}
		return INSTANCE;
	}

	/** The build resource set. */
	private final ResourceSet buildResourceSet;

	/**
	 * A constructor that builds the resource set based on an existing injecto
	 *
	 * @param injector
	 */
	public GamlModelBuilder(final Injector injector) {
		buildResourceSet = injector.getInstance(ResourceSet.class);
	}

	/**
	 * Instantiates a new gaml model builder.
	 */
	private GamlModelBuilder() {
		buildResourceSet = new SynchronizedXtextResourceSet();
	}

	/**
	 * Compile.
	 *
	 * @param url
	 *            the url
	 * @param errors
	 *            the errors
	 * @return the i model
	 */
	@Override
	public synchronized IModelSpecies compile(final URL url, final List<GamlCompilationError> errors) {
		if (url == null) {
			addError(errors, "URL is null", null);
			return null;
		}
		try {
			final URI resolvedURI = convertURLToURI(url);
			return compile(resolvedURI, errors);
		} catch (final URISyntaxException e) {
			final String errorMsg = "Invalid URL syntax: " + url + " - " + e.getMessage();
			DEBUG.ERR(errorMsg);
			addError(errors, errorMsg, null);
			return null;
		}
	}

	/**
	 * Compiles a file to a GAMA model ready to be experimented
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param myFile
	 *            the my file
	 * @param errors
	 *            a list that will be filled with compilation errors / warnings (can be null)
	 * @param metaProperties
	 *            an INSTANCE of GamlProperties that will be filled with the sylmbolic names of bundles required to run
	 *            the model (can be null) and other informations (skills, operators, statements, ...).
	 * @return the compiled model or null if errors occur
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IllegalArgumentException
	 *             Signals that errors occured
	 * @date 15 oct. 2023
	 */
	@Override
	public synchronized IModelSpecies compile(final File myFile, final List<GamlCompilationError> errors,
			final GamlProperties metaProperties) throws IOException, GamaCompilationFailedException {
		if (myFile == null) throw new IOException("Model file is null");
		final String fileName = myFile.getAbsolutePath();
		if (!myFile.exists()) throw new IOException("Model file does not exist: " + fileName);
		DEBUG.LOG(fileName + " model is being compiled...");

		final IModelSpecies model = compile(URI.createFileURI(fileName), errors);
		if (model == null) {
			DEBUG.LOG("Model didn't compile because of the following compilation errors: \n"
					+ (errors == null ? "" : StreamEx.of(errors).joining("\n")));
			throw new GamaCompilationFailedException(errors);
		}
		if (metaProperties != null) { model.getDescription().collectMetaInformation(metaProperties); }
		return model;
	}

	/**
	 * Compile.
	 *
	 * @param uri
	 *            the uri
	 * @param errors
	 *            the errors
	 * @return the i model
	 */
	@Override
	public synchronized IModelSpecies compile(final URI uri, final List<GamlCompilationError> errors) {
		// We build the description and fill the errors list
		final IModelDescription model = buildModelDescription(uri, errors);
		// And compile it before returning it, unless it is null.
		return model == null ? null : (IModelSpecies) model.compile();
	}

	/**
	 * Builds the model description.
	 *
	 * @param uri
	 *            the uri
	 * @param errors
	 *            the errors
	 * @return the model description
	 */
	private IModelDescription buildModelDescription(final URI uri, final List<GamlCompilationError> errors) {
		try {
			final GamlResource r = (GamlResource) buildResourceSet.getResource(uri, true);
			// Syntactic errors detected, we cannot build the resource
			if (r.hasErrors()) {
				if (errors != null) {
					final String err_ =
							r.getErrors() != null && !r.getErrors().isEmpty() ? r.getErrors().get(0).toString() : "";
					errors.add(GamlCompilationError.create("Syntax errors: " + err_, IGamlIssue.GENERAL,
							r.getContents().get(0), GamlCompilationError.Type.Error));
				}
				return null;
			}
			// We build the description
			final IModelDescription model = r.buildCompleteDescription();
			if (model != null) { model.validate(); }
			if (errors != null) { Iterables.addAll(errors, r.getValidationContext()); }
			if (r.getValidationContext().hasErrors()) return null;
			return model;
		} finally {
			final boolean wasDeliver = buildResourceSet.eDeliver();
			try {
				buildResourceSet.eSetDeliver(false);
				buildResourceSet.getResources().clear();
			} finally {
				buildResourceSet.eSetDeliver(wasDeliver);
			}
		}
	}

	/**
	 * Load UR ls.
	 *
	 * @param URLs
	 *            the UR ls
	 */
	@Override
	public void loadURLs(final List<URL> URLs) {
		if (URLs == null || URLs.isEmpty()) {
			DEBUG.LOG("No URLs to load");
			return;
		}
		for (final URL url : URLs) {
			if (url == null) {
				DEBUG.ERR("Skipping null URL in loadURLs");
				continue;
			}
			try {
				final URI resolvedURI = convertURLToURI(url);
				buildResourceSet.getResource(resolvedURI, true);
			} catch (final URISyntaxException e) {
				DEBUG.ERR("Invalid URL syntax: " + url + " - " + e.getMessage());
			}
		}
	}

	/**
	 * Helper method to safely add an error to the errors list.
	 *
	 * @param errors
	 *            the errors list (can be null)
	 * @param message
	 *            the error message
	 * @param uri
	 *            the URI (can be null)
	 */
	private void addError(final List<GamlCompilationError> errors, final String message, final URI uri) {
		if (errors != null) {
			errors.add(GamlCompilationError.create(message, IGamlIssue.GENERAL, uri, GamlCompilationError.Type.Error));
		}
	}

	/**
	 * Helper method to convert a URL to an EMF URI.
	 *
	 * @param url
	 *            the URL to convert
	 * @return the converted URI, or null if conversion fails
	 * @throws URISyntaxException
	 *             if the URL has invalid syntax
	 */
	private URI convertURLToURI(final URL url) throws URISyntaxException {
		final java.net.URI uri = new java.net.URI(url.getProtocol(), url.getPath(), null).normalize();
		return URI.createURI(uri.toString());
	}
}
