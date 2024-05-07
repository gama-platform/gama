/*******************************************************************************************************
 *
 * GamlCompilationError.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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
public class GamlCompilationError {

	public enum GamlCompilationErrorType {
		Info, Warning, Error
	}

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
	public GamlCompilationError(final String string, final String code, final EObject object, final GamlCompilationErrorType type, 
			final String... data) {

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
	public GamlCompilationError(final String string, final String code, final URI uri, final GamlCompilationErrorType type,
			final String... data) {

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
	public String[] getData() { return data; }

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getURI() { return uri; }

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
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
	public boolean isWarning() { return errorType == GamlCompilationErrorType.Warning; }

	/**
	 * Checks if is info.
	 *
	 * @return true, if is info
	 */
	public boolean isInfo() { return errorType == GamlCompilationErrorType.Info; }

	/**
	 * Gets the statement.
	 *
	 * @return the statement
	 */
	public EObject getStatement() { return source; }

	/**
	 * Checks if is error.
	 *
	 * @return true, if is error
	 */
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
}
