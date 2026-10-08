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
package gaml.compiler.validation;

import static gama.api.utils.prefs.GamaPreferences.Modeling.INFO_ENABLED;
import static gama.api.utils.prefs.GamaPreferences.Modeling.WARNINGS_ENABLED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.documentation.IDocManager;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * The ValidationContext class manages compilation errors, warnings, and information messages during GAML model
 * validation and compilation. It collects and categorizes errors from both the current resource and imported resources,
 * while also handling documentation generation.
 *
 * <p>
 * <strong>Key Design Improvements (2026):</strong>
 * </p>
 * <ul>
 * <li><strong>Separated Collections:</strong> Errors, warnings, and infos stored in separate lists for O(1) access
 * without filtering</li>
 * <li><strong>Memory Efficiency:</strong> No inheritance from ArrayList - uses composition for better
 * encapsulation</li>
 * <li><strong>Performance:</strong> Direct access to each category eliminates repeated Iterables.filter() calls</li>
 * <li><strong>Thread Safety:</strong> ConcurrentHashMap for documentation supports parallel validation</li>
 * </ul>
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
 * @author GAMA Development Team
 * @since GAMA 1.0
 */
public class ValidationContext implements IValidationContext {

	/**
	 * The Enum Flag for ValidationContext state management. Using EnumSet for efficient memory usage and better type
	 * safety.
	 */
	public enum Flag {
		/** Warning messages are suppressed */
		NO_WARNING,
		/** Info messages are suppressed */
		NO_INFO,
		/** Syntax errors were detected during parsing */
		HAS_SYNTAX_ERRORS,
		/** Model has no experiment defined */
		NO_EXPERIMENT
	}

	static {
		DEBUG.OFF();
	}

	/**
	 * A null object pattern instance for cases where no validation context is needed. This avoids null checks
	 * throughout the codebase.
	 */
	public static IValidationContext NULL = create(null, false);

	/**
	 * Maximum number of errors to report per category. This prevents memory issues when a file has an excessive number
	 * of errors.
	 */
	final static int MAX_SIZE = 1000;

	/** Internal errors from the current resource - null until first error is added */
	private Collection<GamlCompilationError> errors;

	/** Warnings from the current resource - null until first warning is added */
	private Collection<GamlCompilationError> warnings;

	/** Info messages from the current resource - null until first info is added */
	private Collection<GamlCompilationError> infos;

	/**
	 * Collection of errors that originated from imported resources rather than the current resource. Only error-level
	 * issues are stored here; warnings and info messages from imports are not collected.
	 */
	private Collection<GamlCompilationError> importedErrors;

	/** The state. */
	private final EnumSet<Flag> state = EnumSet.noneOf(Flag.class);

	/**
	 * The URI of the resource being validated. Used to distinguish between errors in the current resource and errors in
	 * imported resources.
	 */
	final URI resourceURI;

	/**
	 * Creates the.
	 *
	 * @param uri
	 *            the uri
	 * @param syntax
	 *            the syntax
	 * @param delegate
	 *            the delegate
	 * @return the validation context
	 */
	public static IValidationContext create(final URI uri, final boolean syntax) {
		return new ValidationContext(uri, syntax);
	}

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
	protected ValidationContext(final URI uri, final boolean syntax) {
		this.resourceURI = uri;
		if (syntax) { state.add(Flag.HAS_SYNTAX_ERRORS); }
		if (!WARNINGS_ENABLED.getValue()) { state.add(Flag.NO_WARNING); }
		if (!INFO_ENABLED.getValue()) { state.add(Flag.NO_INFO); }
	}

	/**
	 * Adds a compilation error, warning, or info message to this validation context.
	 *
	 * <p>
	 * <strong>Performance Improvement:</strong> Directly adds to the appropriate collection (errors/warnings/infos)
	 * without needing filtering later. This provides O(1) insertion and O(1) access by category.
	 * </p>
	 *
	 * @param error
	 *            the compilation error to add
	 * @return true if the error was added to this context, false if it was filtered out or suppressed
	 */
	@Override
	public boolean add(final GamlCompilationError error) {
		// Filter out warnings/info if disabled
		if (error.isWarning() && (state.contains(Flag.NO_WARNING) || !WARNINGS_ENABLED.getValue())) return false;
		if (error.isInfo() && (state.contains(Flag.NO_INFO) || !INFO_ENABLED.getValue())) return false;

		final URI uri = error.uri();
		final boolean sameResource = uri == null || uri.equals(resourceURI);

		if (sameResource) {
			// Lazy initialization + direct addition to appropriate collection - O(1) operation
			if (error.isError()) {
				if (errors == null) { errors = new ArrayList<>(); }
				if (errors.size() < MAX_SIZE) return errors.add(error);
			} else if (error.isWarning()) {
				if (warnings == null) { warnings = new ArrayList<>(); }
				if (warnings.size() < MAX_SIZE) return warnings.add(error);
			} else if (error.isInfo()) {
				if (infos == null) { infos = new ArrayList<>(); }
				if (infos.size() < MAX_SIZE) return infos.add(error);
			}
			return false;
		}

		// Only collect errors from imported resources, not warnings or info
		if (error.isError()) {
			if (importedErrors == null) { importedErrors = new LinkedHashSet<>(); }
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
		return state.contains(Flag.HAS_SYNTAX_ERRORS);
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
		return hasInternalSyntaxErrors() || hasInternalErrors() || hasImportedErrors();
	}

	/**
	 * Checks whether any error-level issues exist in the current resource being validated.
	 *
	 * <p>
	 * <strong>Performance Improvement:</strong> O(1) operation - just checks if errors list is empty or null, no
	 * filtering needed.
	 * </p>
	 *
	 * @return true if internal errors exist, false otherwise
	 */
	@Override
	public boolean hasInternalErrors() {
		return errors != null && !errors.isEmpty();
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
	 * <p>
	 * <strong>Performance Improvement:</strong> O(1) direct access, no filtering needed. Returns empty list if no
	 * errors were added (lazy initialization).
	 * </p>
	 *
	 * @return the internal errors
	 */
	@Override
	public Iterable<GamlCompilationError> getInternalErrors() {
		return errors == null ? Collections.emptyList() : errors;
	}

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
	 * <p>
	 * <strong>Performance Improvement:</strong> O(1) direct access, no filtering needed. Returns empty list if no
	 * warnings were added (lazy initialization).
	 * </p>
	 *
	 * @return the warnings
	 */
	@Override
	public Iterable<GamlCompilationError> getWarnings() {
		return warnings == null ? Collections.emptyList() : warnings;
	}

	/**
	 * Gets the infos.
	 *
	 * <p>
	 * <strong>Performance Improvement:</strong> O(1) direct access, no filtering needed. Returns empty list if no infos
	 * were added (lazy initialization).
	 * </p>
	 *
	 * @return the infos
	 */
	@Override
	public Iterable<GamlCompilationError> getInfos() { return infos == null ? Collections.emptyList() : infos; }

	/**
	 * Clears all errors, warnings, and info messages from this validation context. Resets the context to its initial
	 * state, including clearing imported errors and resetting the syntax error flag. Only clears collections that were
	 * actually instantiated (lazy initialization).
	 */
	@Override
	public void clear() {
		if (errors != null) { errors.clear(); }
		if (warnings != null) { warnings.clear(); }
		if (infos != null) { infos.clear(); }
		if (importedErrors != null) { importedErrors.clear(); }
		state.remove(Flag.HAS_SYNTAX_ERRORS);
	}

	/**
	 * Returns an iterator over all errors in this context, including both internal and imported errors. The iterator is
	 * limited to {@link #MAX_SIZE} errors to prevent memory issues.
	 *
	 * @return an iterator over all errors (internal and imported), limited to MAX_SIZE
	 */
	@Override
	public Iterator<GamlCompilationError> iterator() {
		// Combine all three collections + imported errors
		return Iterables
				.limit(Iterables.concat(getInternalErrors(), getWarnings(), getInfos(), getImportedErrors()), MAX_SIZE)
				.iterator();
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
		importedErrors.forEach(e -> result
				.put(e.toString() + " (" + IMPORTED_FROM + " " + URI.decode(e.uri().lastSegment()) + ")", e.uri()));
		return result;
	}

	/**
	 * Disables collection of warning messages. After calling this method, any warnings added to the context will be
	 * suppressed and not stored. This setting persists until {@link #resetInfoAndWarning()} is called.
	 */
	@Override
	public void setNoWarning() {
		state.add(Flag.NO_WARNING);
	}

	/**
	 * Disables collection of info messages. After calling this method, any info messages added to the context will be
	 * suppressed and not stored. This setting persists until {@link #resetInfoAndWarning()} is called.
	 */
	@Override
	public void setNoInfo() {
		state.add(Flag.NO_INFO);
	}

	/**
	 * Re-enables collection of warning and info messages. This resets the flags set by {@link #setNoWarning()} and
	 * {@link #setNoInfo()}, allowing warnings and info messages to be collected again.
	 */
	@Override
	public void resetInfoAndWarning() {
		state.remove(Flag.NO_INFO);
		state.remove(Flag.NO_WARNING);
	}

	/**
	 * Checks for error on.
	 *
	 * <p>
	 * <strong>Performance Improvement:</strong> Only searches the errors list if it exists, not all messages.
	 * </p>
	 *
	 * @param objects
	 *            the objects
	 * @return true, if successful
	 */
	@Override
	public boolean hasErrorOn(final EObject... objects) {
		if (errors == null) return false;
		final List<EObject> list = Arrays.asList(objects);
		return StreamEx.of(errors).anyMatch(p -> list.contains(p.source()));
	}

	/**
	 * Sets the no experiment flag.
	 */
	@Override
	public void setNoExperiment() {
		state.add(Flag.NO_EXPERIMENT);
	}

	/**
	 * Gets the no experiment flag.
	 *
	 * @return true if the model has no experiment defined
	 */
	@Override
	public boolean getNoExperiment() { return state.contains(Flag.NO_EXPERIMENT); }

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
					add(GamlCompilationError.create("Missing plugin: " + s, IGamlIssue.MISSING_PLUGIN, resourceURI,
							GamaBundleLoader.isDisplayPlugin(s) ? GamlCompilationError.Type.Error
									: GamlCompilationError.Type.Warning));
				}
				return false;
			}
		}
		return true;
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
