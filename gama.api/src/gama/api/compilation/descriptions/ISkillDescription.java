/*******************************************************************************************************
 *
 * ISkillDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import gama.api.kernel.skill.ISkill;

/**
 *
 */
public interface ISkillDescription extends ITypeDescription {

	/**
	 * @return
	 */
	String getDeprecated();

	/**
	 * @return
	 */
	ISkill getInstance();

	/**
	 * @return
	 */
	boolean isControl();

	/**
	 * Gets the own attributes.
	 *
	 * @return the own attributes
	 */
	Iterable<IVariableDescription> getOwnAttributes();

	/**
	 * Gets the own actions.
	 *
	 * @return the own actions
	 */
	Iterable<IActionDescription> getOwnActions();

}
