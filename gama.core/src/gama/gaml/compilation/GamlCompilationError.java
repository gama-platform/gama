/*******************************************************************************************************
 *
 * GamlCompilationError.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

/**
 * The Class GamlCompilationError. Represents the errors produced by the validation/compilation of IDescription's.
 */
public class GamlCompilationError implements IGamlCompilationError {

	/** The message. */
	protected final String message;

	/** The code. */
	protected String code;

	/** The data. */
	protected final String[] data;

	/** The source. */
	protected EObject source;

	/** The uri. */
	protected final URI uri;

	/** The error type. */
	protected final GamlCompilationErrorType errorType;

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
	public GamlCompilationError(final String string, final String code, final EObject object,
			final GamlCompilationErrorType type, final String... data) {

		message = string;
		errorType = type;
		this.code = code;
		this.data = data;
		source = object;
		uri = object.eResource().getURI();
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
	public GamlCompilationError(final String string, final String code, final URI uri,
			final GamlCompilationErrorType type, final String... data) {

		message = string;
		errorType = type;
		this.code = code;
		this.data = data;
		source = null;
		this.uri = uri;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	@Override
	public String[] getData() { return data; }

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	@Override
	public URI getURI() { return uri; }

	/**
	 * Gets the error type.
	 *
	 * @return the error type
	 */
	@Override
	public GamlCompilationErrorType getErrorType() { return errorType; }

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	@Override
	public String getCode() { return code; }

	@Override
	public String toString() {
		return message;
	}

	/**
	 * Checks if is warning.
	 *
	 * @return true, if is warning
	 */
	@Override
	public boolean isWarning() { return errorType == GamlCompilationErrorType.Warning; }

	/**
	 * Checks if is info.
	 *
	 * @return true, if is info
	 */
	@Override
	public boolean isInfo() { return errorType == GamlCompilationErrorType.Info; }

	/**
	 * Gets the statement.
	 *
	 * @return the statement
	 */
	@Override
	public EObject getSource() { return source; }

	/**
	 * Checks if is error.
	 *
	 * @return true, if is error
	 */
	@Override
	public boolean isError() { return errorType == GamlCompilationErrorType.Error; }

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

	@Override
	public String getMessage() { return message; }
}
