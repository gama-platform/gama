/*******************************************************************************************************
 *
 * IEnvelopeProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.IScope;

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
	Envelope3D computeEnvelope(final IScope scope);

}
