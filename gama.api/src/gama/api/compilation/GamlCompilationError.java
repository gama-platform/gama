/*******************************************************************************************************
 *
 * GamlCompilationError.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

/**
 * Represents compilation and validation errors produced during GAML model processing.
 * 
 * <p>This record encapsulates all information about errors, warnings, and informational messages
 * generated during the compilation and validation phases of GAML model processing. It provides
 * complete error context including the error message, location, severity, and additional metadata.</p>
 * 
 * <h2>Error Components</h2>
 * 
 * <ul>
 *   <li><strong>message:</strong> Human-readable error description</li>
 *   <li><strong>code:</strong> Error code identifier (from {@link gama.api.constants.IGamlIssue})</li>
 *   <li><strong>source:</strong> EMF EObject where the error occurred (can be null)</li>
 *   <li><strong>uri:</strong> URI of the resource containing the error</li>
 *   <li><strong>errorType:</strong> Severity level (Error, Warning, or Info)</li>
 *   <li><strong>data:</strong> Optional additional data about the error</li>
 * </ul>
 * 
 * <h2>Error Severity</h2>
 * 
 * <p>Errors are classified by severity:</p>
 * <ul>
 *   <li><strong>Error:</strong> Prevents model compilation or execution</li>
 *   <li><strong>Warning:</strong> Potential issues but compilation continues</li>
 *   <li><strong>Info:</strong> Informational messages</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Create error with EObject
 * GamlCompilationError error = GamlCompilationError.create(
 *     "Type mismatch",
 *     IGamlIssue.WRONG_TYPE,
 *     statement,
 *     GamlCompilationError.Type.Error
 * );
 * 
 * // Create error with URI only
 * GamlCompilationError error = GamlCompilationError.create(
 *     "File not found",
 *     IGamlIssue.GENERAL,
 *     resourceURI,
 *     GamlCompilationError.Type.Error
 * );
 * 
 * // Check error type
 * if (error.isError()) {
 *     // Handle compilation error
 * }
 * }</pre>
 * 
 * <h2>Integration</h2>
 * 
 * <p>Compilation errors are typically collected in a list during model building and can be
 * queried from the {@link gama.api.compilation.validation.IValidationContext} after compilation.</p>
 * 
 * @param message Human-readable error description
 * @param code Error code identifier
 * @param source EMF object where error occurred (may be null)
 * @param uri URI of the resource containing the error
 * @param errorType Severity level (Error, Warning, Info)
 * @param data Optional additional context data
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.compilation.validation.IValidationContext
 * @see gama.api.constants.IGamlIssue
 */
public record GamlCompilationError(String message, String code, EObject source, URI uri, Type errorType,
		String... data) {

	/**
	 * Enumeration of error severity levels.
	 * 
	 * <p>Defines the three levels of diagnostic severity used in GAML compilation:</p>
	 * <ul>
	 *   <li><strong>Error:</strong> Critical issues that prevent compilation/execution</li>
	 *   <li><strong>Warning:</strong> Potential problems that don't prevent compilation</li>
	 *   <li><strong>Info:</strong> Informational messages for the user</li>
	 * </ul>
	 */
	public enum Type {

		/** Warning level - potential issues but compilation continues. */
		Warning,
		/** Info level - informational messages only. */
		Info,
		/** Error level - prevents successful compilation. */
		Error
	}

	/**
	 * Creates a new compilation error from an EObject source.
	 * 
	 * <p>
	 * This factory method creates a compilation error associated with a specific EMF EObject
	 * in the parsed model. The URI is automatically extracted from the EObject's resource.
	 * </p>
	 *
	 * @param message the human-readable error message describing the issue
	 * @param code the error code identifier (typically from {@code IGamlIssue})
	 * @param source the EMF EObject where the error occurred (must not be null and must have a resource)
	 * @param type the severity level (Error, Warning, or Info)
	 * @param data optional additional context data (variable length array)
	 * @return a new {@link GamlCompilationError} instance
	 * @throws NullPointerException if source or its resource is null
	 */
	public static GamlCompilationError create(final String message, final String code, final EObject source,
			final Type type, final String... data) {
		return new GamlCompilationError(message, code, source, source.eResource().getURI(), type, data);
	}

	/**
	 * Creates a new compilation error from a URI only.
	 * 
	 * <p>
	 * This factory method creates a compilation error associated with a resource URI
	 * but without a specific EObject source. This is useful for file-level errors
	 * (e.g., file not found, parse errors) where no specific AST element exists.
	 * </p>
	 *
	 * @param message the human-readable error message describing the issue
	 * @param code the error code identifier (typically from {@code IGamlIssue})
	 * @param uri the URI of the resource containing the error
	 * @param type the severity level (Error, Warning, or Info)
	 * @param data optional additional context data (variable length array)
	 * @return a new {@link GamlCompilationError} instance with null source
	 */
	public static GamlCompilationError create(final String message, final String code, final URI uri, final Type type,
			final String... data) {
		return new GamlCompilationError(message, code, null, uri, type, data);
	}

	@Override
	public String toString() {
		return message;
	}

	/**
	 * Checks if this error is a warning.
	 * 
	 * <p>
	 * Warnings indicate potential issues that don't prevent compilation or execution
	 * but may lead to unexpected behavior or performance problems.
	 * </p>
	 *
	 * @return true if this is a warning, false otherwise
	 */
	public boolean isWarning() { return errorType == Type.Warning; }

	/**
	 * Checks if this error is informational.
	 * 
	 * <p>
	 * Info messages provide helpful information to the user but don't indicate
	 * any problems with the model.
	 * </p>
	 *
	 * @return true if this is an informational message, false otherwise
	 */
	public boolean isInfo() { return errorType == Type.Info; }

	/**
	 * Checks if this is a compilation error.
	 * 
	 * <p>
	 * Errors indicate critical issues that prevent model compilation or execution.
	 * Models with errors cannot be run until the issues are resolved.
	 * </p>
	 *
	 * @return true if this is an error, false otherwise
	 */
	public boolean isError() { return errorType == Type.Error; }

	@Override
	public boolean equals(final Object other) {
		if (this == other) return true;
		if (!(other instanceof final GamlCompilationError error)) return false;
		return message.equals(error.message) && source == error.source;
	}

	@Override
	public int hashCode() {
		return message.hashCode() + (source == null ? 0 : source.hashCode());
	}

}
