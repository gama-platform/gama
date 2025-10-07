/*******************************************************************************************************
 *
 * ISkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import gama.core.common.interfaces.IVarAndActionSupport;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.interfaces.IGamlDescription;

/**
 * SkillInterface - convenience interface for any object that might be used as a "skill" for an agent.
 *
 * @author drogoul 4 juil. 07
 */
public interface ISkill extends IGamlDescription, IVarAndActionSupport, ISymbol {

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	SkillDescription getDescription();
}
