/*******************************************************************************************************
 *
 * ISerialisedAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.agent;

import java.util.Map;

import gama.annotations.precompiler.OkForAPI;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.ISerialisedPopulation;
import gama.core.runtime.IScope;
import gama.gaml.interfaces.IJsonable;

/**
 * The Interface ISerialisedAgent.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface ISerialisedAgent extends IJsonable {

	/**
	 * Gets the index.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the index
	 * @date 8 août 2023
	 */
	int getIndex();

	/**
	 * Gets the attribute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param var
	 *            the var
	 * @return the attribute
	 * @date 8 août 2023
	 */
	Object getAttributeValue(String var);

	/**
	 * Sets the attribute value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param var
	 *            the var
	 * @param val
	 *            the val
	 * @date 8 août 2023
	 */
	void setAttributeValue(String var, Object val);

	/**
	 * Gets the variables.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the variables
	 * @date 29 oct. 2023
	 */
	Map<String, Object> attributes();

	/**
	 * Gets the inner populations.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the inner populations
	 * @date 29 oct. 2023
	 */
	Map<String, ISerialisedPopulation> innerPopulations();

	/**
	 * Restore to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param microPop
	 *            the micro pop
	 * @date 29 oct. 2023
	 */
	IAgent restoreInto(IScope scope, IPopulation<? extends IAgent> microPop);

	/**
	 * Restore as.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @date 31 oct. 2023
	 */
	void restoreAs(IScope scope, IAgent agent);

}
