/*******************************************************************************************************
 *
 * IValidationContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.GamlCompilationError;

/**
 * Context interface for collecting and managing compilation errors during model validation.
 * 
 * <p>This interface provides a centralized repository for compilation errors, warnings, and
 * informational messages generated during the GAML model compilation process. It categorizes
 * errors by source (internal vs. imported) and severity (error vs. warning vs. info).</p>
 * 
 * <h2>Error Categories</h2>
 * 
 * <h3>By Source:</h3>
 * <ul>
 *   <li><strong>Internal Errors:</strong> Errors from the model being compiled</li>
 *   <li><strong>Imported Errors:</strong> Errors from imported/referenced models</li>
 * </ul>
 * 
 * <h3>By Type:</h3>
 * <ul>
 *   <li><strong>Syntax Errors:</strong> Parsing failures, malformed code</li>
 *   <li><strong>Semantic Errors:</strong> Type mismatches, undefined references</li>
 *   <li><strong>Warnings:</strong> Non-critical issues, deprecated usage</li>
 *   <li><strong>Info:</strong> Informational messages, suggestions</li>
 * </ul>
 * 
 * <h2>Error Collection Strategy</h2>
 * 
 * <p>The context maintains separate collections for different error categories, allowing:</p>
 * <ul>
 *   <li>Quick filtering by severity or source</li>
 *   <li>Selective display in IDE error views</li>
 *   <li>Configurable error suppression (warnings, info)</li>
 *   <li>Import error tracking across model dependencies</li>
 * </ul>
 * 
 * <h2>Lifecycle</h2>
 * 
 * <p>Typical usage pattern:</p>
 * <ol>
 *   <li>Create context at start of compilation</li>
 *   <li>Pass context to validators and builders</li>
 *   <li>Validators add errors via {@link #add}</li>
 *   <li>Check error status via {@link #hasErrors()}</li>
 *   <li>Retrieve errors for display/logging</li>
 *   <li>Clear context for next compilation</li>
 * </ol>
 * 
 * <h2>Configuration Flags</h2>
 * 
 * <p>The context supports several configuration options:</p>
 * <ul>
 *   <li><strong>No Warnings:</strong> Suppress warning messages ({@link #setNoWarning})</li>
 *   <li><strong>No Info:</strong> Suppress informational messages ({@link #setNoInfo})</li>
 *   <li><strong>No Experiment:</strong> Flag indicating no experiment definitions ({@link #setNoExperiment})</li>
 * </ul>
 * 
 * <h2>Plugin Verification</h2>
 * 
 * <p>The context can verify that required plugins are available in the platform,
 * ensuring models using extension features can actually run.</p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IValidationContext context = ...; // Get or create context
 * 
 * // Add error during validation
 * context.add(new GamlCompilationError(
 *     "Type mismatch",
 *     IGamlIssue.WRONG_TYPE,
 *     eObject,
 *     false,  // is warning
 *     false   // is info
 * ));
 * 
 * // Check compilation status
 * if (context.hasErrors()) {
 *     System.err.println("Compilation failed:");
 *     for (GamlCompilationError error : context.getInternalErrors()) {
 *         System.err.println("  " + error);
 *     }
 * }
 * 
 * // Clear for next compilation
 * context.clear();
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see GamlCompilationError
 * @see IGamlModelBuilder
 * @see IValidator
 */
public interface IValidationContext extends Iterable<GamlCompilationError> {

	/** Constant prefix for imported error messages. */
	String IMPORTED_FROM = "imported from";

	/**
	 * Adds a compilation error to the context.
	 * 
	 * <p>The error is automatically categorized based on its properties (source file,
	 * severity, etc.) and stored in the appropriate internal collection.</p>
	 *
	 * @param error the compilation error to add (must not be null)
	 * @return true if the error was added successfully, false otherwise
	 */
	boolean add(GamlCompilationError error);

	/**
	 * Checks if there are any internal syntax errors.
	 * 
	 * <p>Syntax errors are parsing failures that prevent the model from being
	 * properly loaded into the AST. These are typically the first errors shown
	 * to users.</p>
	 *
	 * @return true if internal syntax errors exist, false otherwise
	 */
	boolean hasInternalSyntaxErrors();

	/**
	 * Checks if there are any errors (syntax or semantic, internal or imported).
	 * 
	 * <p>This is the primary method for determining if compilation succeeded.
	 * Returns true if any errors (but not warnings or info) are present.</p>
	 *
	 * @return true if any errors exist, false otherwise
	 */
	boolean hasErrors();

	/**
	 * Checks if there are any internal errors (from the main model being compiled).
	 * 
	 * <p>Internal errors are errors in the model file itself, not in imported
	 * dependencies. This helps distinguish between local issues and dependency
	 * problems.</p>
	 *
	 * @return true if internal errors exist, false otherwise
	 */
	boolean hasInternalErrors();

	/**
	 * Checks if there are any imported errors (from referenced models).
	 * 
	 * <p>Imported errors originate from models referenced via import statements.
	 * These errors may indicate that a dependency needs to be fixed before the
	 * current model can compile successfully.</p>
	 *
	 * @return true if imported errors exist, false otherwise
	 */
	boolean hasImportedErrors();

	/**
	 * Retrieves all internal errors (syntax and semantic).
	 * 
	 * <p>Returns errors from the model being compiled, excluding imported errors.
	 * This is the primary error collection shown to users.</p>
	 *
	 * @return iterable of internal errors (never null, may be empty)
	 */
	Iterable<GamlCompilationError> getInternalErrors();

	/**
	 * Retrieves all errors from imported/referenced models.
	 * 
	 * <p>These errors indicate problems in dependencies. They are typically
	 * displayed separately or with special marking to indicate their origin.</p>
	 *
	 * @return collection of imported errors (never null, may be empty)
	 */
	Collection<GamlCompilationError> getImportedErrors();

	/**
	 * Retrieves all warning messages.
	 * 
	 * <p>Warnings indicate potential issues that don't prevent compilation but
	 * may cause runtime problems or indicate non-optimal code.</p>
	 *
	 * @return iterable of warnings (never null, may be empty)
	 */
	Iterable<GamlCompilationError> getWarnings();

	/**
	 * Retrieves all informational messages.
	 * 
	 * <p>Info messages provide helpful suggestions, deprecation notices, or
	 * other non-critical information to improve code quality.</p>
	 *
	 * @return iterable of info messages (never null, may be empty)
	 */
	Iterable<GamlCompilationError> getInfos();

	/**
	 * Clears all errors, warnings, and info messages from the context.
	 * 
	 * <p>This should be called before starting a new compilation to ensure
	 * errors from previous compilations don't interfere.</p>
	 */
	void clear();

	/**
	 * Returns an iterator over all compilation errors (all types and sources).
	 *
	 * @return iterator for all errors (never null)
	 */
	@Override
	Iterator<GamlCompilationError> iterator();

	/**
	 * Retrieves imported errors as a map of error strings to source URIs.
	 * 
	 * <p>This representation is useful for displaying imported errors grouped
	 * by their source file, allowing users to navigate to the problematic
	 * dependencies.</p>
	 *
	 * @return map from error description to source file URI (never null, may be empty)
	 */
	Map<String, URI> getImportedErrorsAsStrings();

	/**
	 * Suppresses all warning messages in subsequent compilations.
	 * 
	 * <p>When set, warnings will not be added to the context. Useful for
	 * batch processing where only errors matter.</p>
	 */
	void setNoWarning();

	/**
	 * Suppresses all informational messages in subsequent compilations.
	 * 
	 * <p>When set, info messages will not be added to the context.</p>
	 */
	void setNoInfo();

	/**
	 * Re-enables warning and info message collection.
	 * 
	 * <p>Reverses the effects of {@link #setNoWarning} and {@link #setNoInfo}.</p>
	 */
	void resetInfoAndWarning();

	/**
	 * Checks if any of the specified EMF objects have associated errors.
	 * 
	 * <p>Useful for determining if specific model elements are valid before
	 * attempting to use them.</p>
	 *
	 * @param objects the EMF objects to check for errors
	 * @return true if any object has an associated error, false otherwise
	 */
	boolean hasErrorOn(EObject... objects);

	/**
	 * Flags that the model contains no experiment definitions.
	 * 
	 * <p>This affects validation behavior, as some features require experiments.</p>
	 */
	void setNoExperiment();

	/**
	 * Checks if the no-experiment flag is set.
	 *
	 * @return true if the model has no experiments, false otherwise
	 */
	boolean getNoExperiment();

	/**
	 * Verifies that all required plugins are available in the current platform.
	 * 
	 * <p>This method checks that plugin bundles referenced by the model (through
	 * skills, statements, operators, etc.) are actually installed and available.
	 * Missing plugins will prevent the model from running.</p>
	 *
	 * @param list the list of plugin symbolic names to verify
	 * @return true if all plugins are present, false if any are missing
	 */
	boolean verifyPlugins(List<String> list);

	/**
	 * Gets the URI of the model being validated.
	 * 
	 * <p>This URI identifies the source file or resource being compiled,
	 * useful for error reporting and resource resolution.</p>
	 *
	 * @return the model URI (may be null if not set)
	 */
	URI getURI();

}