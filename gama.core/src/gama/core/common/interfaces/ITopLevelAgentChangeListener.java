/*******************************************************************************************************
 *
 * ITopLevelAgentChangeListener.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.kernel.experiment.ITopLevelAgent;

/**
 * The Interface ITopLevelAgentInterface.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 14 août 2023
 */
public interface ITopLevelAgentChangeListener {

	/**
	 * Sets the listening agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the new listening agent
	 * @date 14 août 2023
	 */
	void topLevelAgentChanged(ITopLevelAgent agent);

}
