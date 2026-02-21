/*******************************************************************************************************
 *
 * GamaAssertException.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.exceptions;

import gama.api.runtime.scope.IScope;

/**
 * Exception thrown when an assertion fails in a GAML model.
 * <p>
 * This exception is raised when the {@code assert} statement in GAML evaluates to false,
 * indicating that an expected condition was not met during model execution. It can be
 * configured to generate either an error or a warning, depending on the severity of
 * the assertion failure.
 * </p>
 * <p>
 * Assert exceptions are useful for model validation and debugging, allowing modelers
 * to specify invariants and preconditions that should hold during simulation.
 * </p>
 * 
 * @author GAMA Development Team
 */
public class GamaAssertException extends GamaRuntimeException {

	/**
	 * Instantiates a new gama assert exception.
	 *
	 * @param scope the scope
	 * @param s the s
	 * @param warning the warning
	 */
	public GamaAssertException(final IScope scope, final String s, final boolean warning) {
		super(scope, s, warning);
	}

}
