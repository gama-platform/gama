/**
 * 
 */
package gama.api.exceptions;

import gama.api.runtime.scope.IScope;

/**
 * The Class GamaRuntimeFileException.
 */
public class GamaRuntimeFileException extends GamaRuntimeException {

	/**
	 * @param scope
	 * @param ex
	 */
	public GamaRuntimeFileException(final IScope scope, final Throwable ex) {
		super(scope, ex);
	}

	/**
	 * Instantiates a new gama runtime file exception.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 */
	public GamaRuntimeFileException(final IScope scope, final String s) {
		super(scope, s, false);
	}

}