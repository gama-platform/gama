/*******************************************************************************************************
 *
 * ISkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.gaml.compilation.ISymbol;
import gama.gaml.interfaces.IGamlDescription;

/**
 * SkillInterface - convenience interface for any object that might be used as a "skill" for an agent.
 *
 * @author drogoul 4 juil. 07
 */
public interface ISkill extends IGamlDescription, ISymbol, IVarAndActionSupport {

}
