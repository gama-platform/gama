/*******************************************************************************************************
 *
 * IEnvelopeProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;


import gama.api.data.objects.IEnvelope;
import gama.api.runtime.scope.IScope;

/**
 * The Interface IEnvelopeProvider. Returns an envelope3D that contains the object represented by this interface
 */

public interface IEnvelopeProvider {

	/**
	 * Compute envelope.
	 *
	 * @param scope
	 *            the scope
	 * @return the envelope 3 D
	 */
	IEnvelope computeEnvelope(final IScope scope);

}
