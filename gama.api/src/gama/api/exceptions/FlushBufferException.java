/*******************************************************************************************************
 *
 * FlushBufferException.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.exceptions;

import gama.api.runtime.scope.IScope;

/**
 * Exception thrown when an operation requires flushing the output buffer.
 * <p>
 * This exception is used internally to signal that buffered output (such as console messages
 * or status updates) should be immediately flushed to the output stream. It can represent
 * either an error condition or a warning that requires immediate user attention.
 * </p>
 * <p>
 * The exception carries the scope context in which the flush was triggered, allowing
 * proper contextualization of the output.
 * </p>
 * 
 * @author GAMA Development Team
 */
public class FlushBufferException extends GamaRuntimeException {

	/**
	 * Constructs a new flush buffer exception.
	 * <p>
	 * This exception signals that buffered output should be immediately flushed to the output
	 * stream. It is used to ensure critical messages (errors or warnings) are displayed to
	 * the user without delay, rather than being held in a buffer.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope in which the flush was triggered, may be null
	 * @param s
	 *            the message to be flushed and displayed
	 * @param warning
	 *            true if this represents a warning, false if it's an error
	 */
	public FlushBufferException(final IScope scope, final String s, final boolean warning) {
		super(scope, s, warning);
	}

}