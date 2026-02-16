/*******************************************************************************************************
 *
 * IEnvelopeComputer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;


import gama.api.runtime.scope.IScope;

/**
 * The Interface IEnvelopeComputer.
 */

public interface IEnvelopeComputer {

	/**
	 * Compute envelope from.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the envelope 3 D
	 */
	IEnvelope computeEnvelopeFrom(final IScope scope, final Object obj);

}
