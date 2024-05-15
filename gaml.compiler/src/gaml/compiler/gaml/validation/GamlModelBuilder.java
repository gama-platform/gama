/*******************************************************************************************************
 *
 * GamlModelBuilder.java, in gaml.compiler.gaml, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

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

import gama.annotations.precompiler.GamlProperties;
import gama.core.kernel.model.IModel;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.GamlCompilationError.GamlCompilationErrorType;
import gama.gaml.compilation.IGamlModelBuilder;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.interfaces.IGamlIssue;
import gaml.compiler.gaml.resource.GamlResource;
import one.util.streamex.StreamEx;

/**
 * Class GamlResourceBuilder.
 *
 * @author drogoul
 * @since 8 avr. 2014
 *
 */
public class GamlModelBuilder implements IGamlModelBuilder {

	/** The default instance. */
	private static GamlModelBuilder defaultInstance = new GamlModelBuilder();

	/**
	 * Gets the default instance.
	 *
	 * @return the default instance
	 */
	public static GamlModelBuilder getDefaultInstance() { return defaultInstance; }

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
	public IModel compile(final URL url, final List<GamlCompilationError> errors) {
		try {
			final java.net.URI uri = new java.net.URI(url.getProtocol(), url.getPath(), null).normalize();
			final URI resolvedURI = URI.createURI(uri.toString());
			return compile(resolvedURI, errors);
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
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
	 *            an instance of GamlProperties that will be filled with the sylmbolic names of bundles required to run
	 *            the model (can be null) and other informations (skills, operators, statements, ...).
	 * @return the compiled model or null if errors occur
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws IllegalArgumentException
	 *             Signals that errors occured
	 * @date 15 oct. 2023
	 */
	@Override
	public synchronized IModel compile(final File myFile, final List<GamlCompilationError> errors,
			final GamlProperties metaProperties) throws IOException, IllegalArgumentException {
		if (myFile == null) throw new IOException("Model file is null");
		final String fileName = myFile.getAbsolutePath();
		if (!myFile.exists()) throw new IOException("Model file does not exist: " + fileName);
		DEBUG.LOG(fileName + " model is being compiled...");

		final IModel model = GamlModelBuilder.getDefaultInstance().compile(URI.createFileURI(fileName), errors);
		if (model == null) {
			DEBUG.LOG("Model not compiled because of the following compilation errors: \n"
					+ (errors == null ? "" : StreamEx.of(errors).joining("\n")));
			throw new IllegalArgumentException("Compilation errors: \n" + StreamEx.of(errors).joining("\n"));
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
	public IModel compile(final URI uri, final List<GamlCompilationError> errors) {
		// We build the description and fill the errors list
		final ModelDescription model = buildModelDescription(uri, errors);
		// And compile it before returning it, unless it is null.
		return model == null ? null : (IModel) model.compile();
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
	private ModelDescription buildModelDescription(final URI uri, final List<GamlCompilationError> errors) {
		try {
			final GamlResource r = (GamlResource) buildResourceSet.getResource(uri, true);
			// Syntactic errors detected, we cannot build the resource
			if (r.hasErrors()) {
				if (errors != null) {
					final String err_ =
							r.getErrors() != null && r.getErrors().size() > 0 ? r.getErrors().get(0).toString() : "";
					errors.add(new GamlCompilationError("Syntax errors: " + err_, IGamlIssue.GENERAL,
							r.getContents().get(0), GamlCompilationErrorType.Error));
				}
				return null;
			}
			// We build the description
			final ModelDescription model = r.buildCompleteDescription();
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
		for (final URL url : URLs) {
			java.net.URI uri;
			try {
				uri = new java.net.URI(url.getProtocol(), url.getPath(), null).normalize();
				final URI resolvedURI = URI.createURI(uri.toString());
				buildResourceSet.getResource(resolvedURI, true);
			} catch (final URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
}
