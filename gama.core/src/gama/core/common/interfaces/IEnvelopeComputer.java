/*******************************************************************************************************
 *
 * IEnvelopeComputer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
 * The Interface IEnvelopeComputer.
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
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
