/*******************************************************************************************************
 *
 * IGamlCompilationError.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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

import gama.annotations.precompiler.OkForAPI;

/**
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface IGamlCompilationError {

	/**
	 * The Enum GamlCompilationErrorType.
	 */
	public enum GamlCompilationErrorType {

		/** The Info. */
		Info,
		/** The Warning. */
		Warning,
		/** The Error. */
		Error
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	String[] getData();

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	URI getURI();

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	String getCode();

	/**
	 * Checks if is warning.
	 *
	 * @return true, if is warning
	 */
	boolean isWarning();

	/**
	 * Checks if is info.
	 *
	 * @return true, if is info
	 */
	boolean isInfo();

	/**
	 * Gets the statement.
	 *
	 * @return the statement
	 */
	EObject getSource();

	/**
	 * Checks if is error.
	 *
	 * @return true, if is error
	 */
	boolean isError();

	/**
	 * Gets the error type.
	 *
	 * @return the error type
	 */
	GamlCompilationErrorType getErrorType();

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	String getMessage();

}