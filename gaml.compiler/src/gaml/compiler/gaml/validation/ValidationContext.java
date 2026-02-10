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
 * The ValidationContext class manages compilation errors, warnings, and information messages during GAML model
 * validation and compilation. It collects and categorizes errors from both the current resource and imported
 * resources, while also handling documentation generation.
 * 
 * <p>
 * This context distinguishes between:
 * <ul>
 * <li>Internal errors: errors in the current resource being validated</li>
 * <li>Imported errors: errors from resources imported by the current resource</li>
 * <li>Syntax errors: errors detected during parsing</li>
 * <li>Warnings and info messages: non-critical issues that can be filtered based on preferences</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Thread-safety: This class uses concurrent collections for thread-safe access to expressions requiring
 * documentation.
 * </p>
 * 
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
public class ValidationContext extends Collector.AsList<GamlCompilationError> implements IValidationContext {

	static {
		DEBUG.OFF();
	}

	/**
	 * A null object pattern instance for cases where no validation context is needed. This avoids null checks
	 * throughout the codebase.
	 */
	public static IValidationContext NULL = new ValidationContext(null, false, IDocManager.NULL);

	/**
	 * Maximum number of errors to report. This prevents memory issues when a file has an excessive number of errors.
	 */
	final static int MAX_SIZE = 1000;

	/**
	 * Flag indicating whether documentation should be generated during validation. When true, expressions and
	 * descriptions are collected for documentation generation.
	 */
	boolean shouldDocument;

	/**
	 * The URI of the resource being validated. Used to distinguish between errors in the current resource and errors
	 * in imported resources.
	 */
	final URI resourceURI;

	/**
	 * Collection of errors that originated from imported resources rather than the current resource. Only error-level
	 * issues are stored here; warnings and info messages from imports are not collected.
	 */
	Set<GamlCompilationError> importedErrors;

	/**
	 * Flag controlling whether warning messages should be collected. When true, warnings are suppressed.
	 */
	private boolean noWarning;

	/**
	 * Flag controlling whether info messages should be collected. When true, info messages are suppressed.
	 */
	private boolean noInfo;

	/**
	 * Flag indicating whether syntax errors were detected during parsing. When true, the resource has syntax errors
	 * that may prevent further validation.
	 */
	private boolean hasSyntaxErrors;

	/**
	 * Flag indicating whether the model has no experiment defined. Used to track whether an experiment is required but
	 * missing.
	 */
	private boolean noExperiment;

	/**
	 * Delegate responsible for generating and managing documentation for GAML elements. This delegate is called to
	 * document the model and individual expressions when {@link #shouldDocument} is true.
	 */
	private final IDocManager docDelegate;

	/**
	 * Map of EObjects to their corresponding GAML descriptions for documentation purposes. Uses a ConcurrentHashMap to
	 * support thread-safe concurrent access during parallel validation. Entries are accumulated during validation and
	 * processed when {@link #doDocument(IModelDescription)} is called.
	 */
	private final Map<EObject, IGamlDescription> expressionsToDocument = new ConcurrentHashMap<>();

	/**
	 * Instantiates a new validation context for a specific resource.
	 * 
	 * <p>
	 * This constructor initializes a validation context with the specified resource URI, syntax error status, and
	 * documentation delegate. The context will collect and manage compilation errors, warnings, and information
	 * messages for the resource during validation.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the resource being validated. Used to distinguish between errors in the current resource
	 *            and imported resources. Can be null for test or null contexts.
	 * @param syntax
	 *            true if syntax errors have already been detected during parsing, false otherwise. When true, the
	 *            context will report that syntax errors exist via {@link #hasInternalSyntaxErrors()}
	 * @param delegate
	 *            the documentation manager delegate responsible for generating documentation. If null, a null object
	 *            pattern delegate ({@link IDocManager#NULL}) will be used instead.
	 */
	public ValidationContext(final URI uri, final boolean syntax, final IDocManager delegate) {
		this.resourceURI = uri;
		this.hasSyntaxErrors = syntax;
		this.docDelegate = delegate == null ? IDocManager.NULL : delegate;
	}

	/**
	 * Adds a compilation error, warning, or info message to this validation context.
	 * 
	 * <p>
	 * This method applies filtering based on user preferences and the error severity. Warnings are suppressed if
	 * preferences or {@link #noWarning} is set. Info messages are suppressed if preferences or {@link #noInfo} is set.
	 * Errors from imported resources are stored separately in {@link #importedErrors}.
	 * </p>
	 *
	 * @param error
	 *            the compilation error to add
	 * @return true if the error was added to this context, false if it was filtered out or suppressed
	 */
	@Override
	public boolean add(final GamlCompilationError error) {
		// Filter out warnings if disabled
		if (error.isWarning()) {
			if (!GamaPreferences.Modeling.WARNINGS_ENABLED.getValue() || noWarning) return false;
		} else if (error.isInfo() && (!GamaPreferences.Modeling.INFO_ENABLED.getValue() || noInfo)) {
			return false;
		}
		
		final URI uri = error.getURI();
		final boolean sameResource = uri == null || uri.equals(resourceURI);
		
		if (sameResource) return super.add(error);
		
		// Only collect errors from imported resources, not warnings or info
		if (error.isError()) {
			if (importedErrors == null) { 
				importedErrors = new LinkedHashSet<>(); 
			}
			return importedErrors.add(error);
		}
		return false;
	}

	/**
	 * Checks whether syntax errors were detected during parsing of the current resource.
	 * 
	 * <p>
	 * Syntax errors are typically detected by the parser before validation begins. When syntax errors exist, further
	 * validation may be skipped or produce unreliable results.
	 * </p>
	 *
	 * @return true if syntax errors were detected in the current resource, false otherwise
	 */
	@Override
	public boolean hasInternalSyntaxErrors() {
		return hasSyntaxErrors;
	}

	/**
	 * Checks whether any errors exist in this validation context, including syntax errors, internal errors, and
	 * imported errors.
	 * 
	 * <p>
	 * This method returns true if any of the following conditions are met:
	 * <ul>
	 * <li>Syntax errors were detected ({@link #hasInternalSyntaxErrors()})</li>
	 * <li>Internal errors exist in the current resource ({@link #hasInternalErrors()})</li>
	 * <li>Errors exist in imported resources ({@link #hasImportedErrors()})</li>
	 * </ul>
	 * </p>
	 *
	 * @return true if any errors exist, false otherwise
	 */
	@Override
	public boolean hasErrors() {
		return hasSyntaxErrors || hasInternalErrors() || hasImportedErrors();
	}

	/**
	 * Checks whether any error-level issues exist in the current resource being validated.
	 * 
	 * <p>
	 * This method only checks for errors in the current resource, not imported resources. It filters the internal
	 * collection to find any error-level issues.
	 * </p>
	 *
	 * @return true if internal errors exist, false otherwise
	 */
	@Override
	public boolean hasInternalErrors() {
		return Iterables.any(items(), IS_ERROR);
	}

	/**
	 * Checks whether any errors exist in resources imported by the current resource.
	 * 
	 * <p>
	 * Imported errors are collected separately from internal errors and only include error-level issues, not warnings
	 * or info messages.
	 * </p>
	 *
	 * @return true if imported errors exist, false otherwise
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
		return importedErrors == null ? Collections.emptyList() : importedErrors;
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

	/**
	 * Clears all errors, warnings, and info messages from this validation context. Resets the context to its initial
	 * state, including clearing imported errors and resetting the syntax error flag.
	 */
	@Override
	public void clear() {
		super.clear();
		if (importedErrors != null) { importedErrors.clear(); }
		hasSyntaxErrors = false;
	}

	/**
	 * Returns an iterator over all errors in this context, including both internal and imported errors. The iterator
	 * is limited to {@link #MAX_SIZE} errors to prevent memory issues.
	 *
	 * @return an iterator over all errors (internal and imported), limited to MAX_SIZE
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
		if (importedErrors == null) return Collections.emptyMap();
		Map<String, URI> result = new LinkedHashMap<>();
		importedErrors.forEach(e -> result.put(
				e.toString() + " (" + IMPORTED_FROM + " " + URI.decode(e.getURI().lastSegment()) + ")", e.getURI()));
		return result;
	}

	/**
	 * Disables collection of warning messages. After calling this method, any warnings added to the context will be
	 * suppressed and not stored. This setting persists until {@link #resetInfoAndWarning()} is called.
	 */
	@Override
	public void setNoWarning() {
		noWarning = true;
	}

	/**
	 * Disables collection of info messages. After calling this method, any info messages added to the context will be
	 * suppressed and not stored. This setting persists until {@link #resetInfoAndWarning()} is called.
	 */
	@Override
	public void setNoInfo() {
		noInfo = true;
	}

	/**
	 * Re-enables collection of warning and info messages. This resets the flags set by {@link #setNoWarning()} and
	 * {@link #setNoInfo()}, allowing warnings and info messages to be collected again.
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
			expressionsToDocument.forEach((e, d) -> { docDelegate.setGamlDocumentation(resourceURI, e, d); });
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
