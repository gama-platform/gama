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
 * The Class FlushBufferException.
 */
public class FlushBufferException extends GamaRuntimeException {

	/**
	 * Instantiates a new flush buffer exception.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param warning
	 *            the warning
	 */
	public FlushBufferException(final IScope scope, final String s, final boolean warning) {
		super(scope, s, warning);
	}

}