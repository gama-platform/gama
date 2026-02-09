/*******************************************************************************************************
 *
 * IArchitecture.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 12 sept. 2010
 *
 * @todo Description
 *
 */
public interface IArchitecture extends ISkill, IStatement {

	/**
	 * Inits the.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	boolean init(IScope scope) throws GamaRuntimeException;

	/**
	 * Abort.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	boolean abort(IScope scope) throws GamaRuntimeException;

	/**
	 * Verify behaviors.
	 *
	 * @param context
	 *            the context
	 */
	void verifyBehaviors(ISpecies context);

	/**
	 * Pre step.
	 *
	 * @param scope
	 *            the scope
	 * @param gamaPopulation
	 *            the gama population
	 */
	void preStep(final IScope scope, IPopulation<? extends IAgent> gamaPopulation);

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	@Override
	default int getOrder() { return 0; }

	/**
	 * Sets the order.
	 *
	 * @param o
	 *            the new order
	 */
	@Override
	default void setOrder(final int o) {}
}