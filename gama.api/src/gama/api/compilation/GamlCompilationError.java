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
	 * Instantiates a new gaml compilation error.
	 *
	 * @param string
	 *            the string
	 * @param code
	 *            the code
	 * @param object
	 *            the object
	 * @param warning
	 *            the warning
	 * @param info
	 *            the info
	 * @param data
	 *            the data
	 */
	public static GamlCompilationError create(final String message, final String code, final EObject source,
			final Type type, final String... data) {
		return new GamlCompilationError(message, code, source, source.eResource().getURI(), type, data);
	}

	/**
	 * Instantiates a new gaml compilation error.
	 *
	 * @param string
	 *            the string
	 * @param code
	 *            the code
	 * @param uri
	 *            the uri
	 * @param warning
	 *            the warning
	 * @param info
	 *            the info
	 * @param data
	 *            the data
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
	 * Checks if is warning.
	 *
	 * @return true, if is warning
	 */
	public boolean isWarning() { return errorType == Type.Warning; }

	/**
	 * Checks if is info.
	 *
	 * @return true, if is info
	 */
	public boolean isInfo() { return errorType == Type.Info; }

	/**
	 * Checks if is error.
	 *
	 * @return true, if is error
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
