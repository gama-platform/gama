/*******************************************************************************************************
 *
 * IStepable.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * The class IStepable. Represents objects (incl. agents) that can be 'stepped' by the scheduler or the scope of GAMA
 *
 * @author drogoul
 * @since 13 dec. 2011
 *
 */
public interface IStepable {

	/**
	 * Called to initialize the attributes of the IStepable with a valid scope.
	 *
	 * @param scope
	 *            the scope in which this stepable should init itself
	 * @return true, if the initialization has been performed correctly
	 * @throws GamaRuntimeException
	 */
	boolean init(IScope scope) throws GamaRuntimeException;

	/**
	 * Called to step the stepable object. Informations on the cycle, context, etc. can be retrieved from the scope
	 *
	 * @param scope
	 *            the scope in which this stepable should perform its step
	 * @return true if the step has correctly been performed, false otherwise
	 * @throws GamaRuntimeException
	 */
	boolean step(IScope scope) throws GamaRuntimeException;

}
