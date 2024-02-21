/*******************************************************************************************************
 *
 * IEnvelopeComputer.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
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
 * The Interface IEnvelopeComputer.
 */
public interface IEnvelopeComputer {

	/**
	 * Compute envelope from.
	 *
	 * @param scope the scope
	 * @param obj the obj
	 * @return the envelope 3 D
	 */
	Envelope3D computeEnvelopeFrom(final IScope scope, final Object obj);

}
