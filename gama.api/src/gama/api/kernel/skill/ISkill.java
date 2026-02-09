/*******************************************************************************************************
 *
 * ISkill.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.compilation.IVarAndActionSupport;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.ISkillDescription;

/**
 * SkillInterface - convenience interface for any object that might be used as a "skill" for an agent.
 *
 * @author drogoul 4 juil. 07
 */
public interface ISkill extends IGamlDescription, IVarAndActionSupport {

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	ISkillDescription getDescription();

	/**
	 * @param skillDescription
	 */
	void setDescription(ISkillDescription skillDescription);
}
