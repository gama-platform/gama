/*******************************************************************************************************
 *
 * ValidationContext.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.documentation.IDocManager;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.utils.collections.Collector;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * The Class ValidationContext.
 */
public class ValidationContext extends Collector.AsList<GamlCompilationError> implements IValidationContext {

	static {
		DEBUG.OFF();
	}

	/** The Constant NULL. */
	public static IValidationContext NULL = new ValidationContext(null, false, IDocManager.NULL);

	/** The Constant MAX_SIZE. */
	final static int MAX_SIZE = 1000;

	/** The should document. */
	boolean shouldDocument;

	/** The resource URI. */
	final URI resourceURI;

	/** The imported errors. */
	Set<GamlCompilationError> importedErrors;

	/** The no experiment. */
	private boolean noWarning, noInfo, hasSyntaxErrors, noExperiment;

	/** The doc delegate. */
	private final IDocManager docDelegate;

	/** The expressions to document. */
	private final Map<EObject, IGamlDescription> expressionsToDocument = new ConcurrentHashMap<>();

	/**
	 * Instantiates a new validation context.
	 *
	 * @param uri
	 *            the uri
	 * @param syntax
	 *            the syntax
	 * @param delegate
	 *            the delegate
	 */
	public ValidationContext(final URI uri, final boolean syntax, final IDocManager delegate) {
		this.resourceURI = uri;
		hasSyntaxErrors = syntax;
		docDelegate = delegate == null ? IDocManager.NULL : delegate;
	}

	@Override
	public boolean add(final GamlCompilationError error) {
		if (error.isWarning()) {
			if (!GamaPreferences.Modeling.WARNINGS_ENABLED.getValue() || noWarning) return false;
		} else if (error.isInfo() && (!GamaPreferences.Modeling.INFO_ENABLED.getValue() || noInfo)) return false;
		final URI uri = error.getURI();
		final boolean sameResource =
				uri == null || uri.equals(resourceURI);/* || uri.toString().startsWith(resourceURI.toString()) */
		if (sameResource) return super.add(error);
		if (error.isError()) {
			if (importedErrors == null) { importedErrors = new LinkedHashSet<>(); }
			importedErrors.add(error);
			return true;
		}
		return false;
	}

	/**
	 * Checks for internal syntax errors.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasInternalSyntaxErrors() {
		return hasSyntaxErrors;
	}

	/**
	 * Checks for errors.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasErrors() {
		return hasSyntaxErrors || hasInternalErrors() || hasImportedErrors();
	}

	/**
	 * Checks for internal errors.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasInternalErrors() {
		return Iterables.any(items(), IS_ERROR);
	}

	/**
	 * Checks for imported errors.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasImportedErrors() {
		return importedErrors != null && !importedErrors.isEmpty();
	}

	/**
	 * Gets the internal errors.
	 *
	 * @return the internal errors
	 */
	@Override
	public Iterable<GamlCompilationError> getInternalErrors() { return Iterables.filter(items(), IS_ERROR); }

	/**
	 * Gets the imported errors.
	 *
	 * @return the imported errors
	 */
	@Override
	public Collection<GamlCompilationError> getImportedErrors() {
		return importedErrors == null ? Collections.EMPTY_LIST : importedErrors;
	}

	/**
	 * Gets the warnings.
	 *
	 * @return the warnings
	 */
	@Override
	public Iterable<GamlCompilationError> getWarnings() { return Iterables.filter(items(), IS_WARNING); }

	/**
	 * Gets the infos.
	 *
	 * @return the infos
	 */
	@Override
	public Iterable<GamlCompilationError> getInfos() { return Iterables.filter(items(), IS_INFO); }

	@Override
	public void clear() {
		super.clear();
		if (importedErrors != null) { importedErrors.clear(); }
		hasSyntaxErrors = false;
	}

	/**
	 * Method iterator()
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<GamlCompilationError> iterator() {
		return Iterables.limit(Iterables.concat(items(), getImportedErrors()), MAX_SIZE).iterator();
	}

	/**
	 * Gets the imported errors as strings.
	 *
	 * @return the imported errors as strings
	 */
	@Override
	public Map<String, URI> getImportedErrorsAsStrings() {
		if (importedErrors == null) return Collections.EMPTY_MAP;
		Map<String, URI> result = new LinkedHashMap<>();
		importedErrors.forEach(e -> result.put(
				e.toString() + " (" + IMPORTED_FROM + " " + URI.decode(e.getURI().lastSegment()) + ")", e.getURI()));
		return result;
	}

	/**
	 * Sets the no warning.
	 */
	@Override
	public void setNoWarning() {
		noWarning = true;

	}

	/**
	 * Sets the no info.
	 */
	@Override
	public void setNoInfo() {
		noInfo = true;
	}

	/**
	 * Reset info and warning.
	 */
	@Override
	public void resetInfoAndWarning() {
		noInfo = false;
		noWarning = false;

	}

	/**
	 * Do document.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param description
	 *            the description
	 * @date 31 déc. 2023
	 */
	@Override
	public void doDocument(final IModelDescription description) {
		if (shouldDocument) {
			docDelegate.doDocument(resourceURI, description, expressionsToDocument);
			synchronized (expressionsToDocument) {
				expressionsToDocument.forEach((e, d) -> { docDelegate.setGamlDocumentation(resourceURI, e, d); });
			}
		}
		expressionsToDocument.clear();
	}

	/**
	 * Sets the gaml documentation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param e
	 *            the e
	 * @param d
	 *            the d
	 * @date 31 déc. 2023
	 */
	@Override
	public void setGamlDocumentation(final EObject e, final IGamlDescription d) {
		// Called by SymbolDescription to document individual expressions -- they are kept in a Map<EObject,
		// IGamlDescription> and done when the whole model is documented
		if (shouldDocument && e != null && d != null) { expressionsToDocument.put(e, d); }
	}

	/**
	 * Checks for error on.
	 *
	 * @param objects
	 *            the objects
	 * @return true, if successful
	 */
	@Override
	public boolean hasErrorOn(final EObject... objects) {
		final List<EObject> list = Arrays.asList(objects);
		return StreamEx.of(items()).filter(IS_ERROR).findAny(p -> list.contains(p.getSource())).isPresent();
	}

	/**
	 * Sets the no experiment.
	 */
	@Override
	public void setNoExperiment() {
		noExperiment = true;
	}

	/**
	 * Gets the no experiment.
	 *
	 * @return the no experiment
	 */
	@Override
	public boolean getNoExperiment() { return noExperiment; }

	/**
	 * Verify plugins. Returns true if all the plugins are present in the current platform
	 *
	 * @param list
	 *            the list
	 * @return true, if successful
	 */
	@Override
	public boolean verifyPlugins(final List<String> list) {
		for (String s : list) {
			if (!GamaBundleLoader.gamlPluginExists(s)) {
				if (!GAMA.isInHeadLessMode() || !GamaBundleLoader.isDisplayPlugin(s)) {
					add(new GamlCompilationError("Missing plugin: " + s, IGamlIssue.MISSING_PLUGIN, resourceURI,
							GamaBundleLoader.isDisplayPlugin(s) ? GamlCompilationError.Type.Error
									: GamlCompilationError.Type.Warning));
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Should document. True by default
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param document
	 * @return true, if successful
	 * @date 30 déc. 2023
	 */
	@Override
	public boolean shouldDocument() {
		return shouldDocument;
	}

	/**
	 * Should document. Do nothing by default.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param document
	 *            the document
	 * @return true, if successful
	 * @date 30 déc. 2023
	 */
	@Override
	public void shouldDocument(final boolean document) {
		shouldDocument = document;
	}

	/**
	 * Gets the uri.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the uri
	 * @date 10 janv. 2024
	 */
	@Override
	public URI getURI() { return resourceURI; }
}
