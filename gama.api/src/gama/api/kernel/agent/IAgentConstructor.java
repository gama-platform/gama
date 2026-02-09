/*******************************************************************************************************
 *
 * IAgentConstructor.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

/**
 * Written by drogoul Modified on 20 ao�t 2010
 *
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
@FunctionalInterface
public interface IAgentConstructor {

	/**
	 * Creates one agent.
	 *
	 * @param <T>
	 *            the generic type
	 * @param manager
	 *            the manager
	 * @param index
	 *            the index
	 * @return the t
	 */
	IAgent createOneAgent(IPopulation manager, int index);

}
