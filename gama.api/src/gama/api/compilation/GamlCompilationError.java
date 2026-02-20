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
 * The Class GamlCompilationError. Represents the errors produced by the validation/compilation of IDescription's.
 */
public record GamlCompilationError(String message, String code, EObject source, URI uri, Type errorType,
		String... data) {

	/**
	 * The Enum Type.
	 */
	public enum Type {

		/** The Warning. */
		Warning,
		/** The Info. */
		Info,
		/** The Error. */
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
