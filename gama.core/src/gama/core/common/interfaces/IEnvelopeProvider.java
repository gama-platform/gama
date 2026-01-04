/*******************************************************************************************************
 *
 * IEnvelopeProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.annotations.precompiler.OkForAPI;
import gama.core.common.geometry.IEnvelope;
import gama.core.runtime.IScope;

/**
 * The Interface IEnvelopeProvider. Returns an envelope3D that contains the object represented by this interface
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
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
