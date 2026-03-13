/*******************************************************************************************************
 *
 * ISpeciesDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Map;

import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.species.ISpecies;
import gama.api.utils.prefs.Pref;

/**
 *
 */
public interface ISpeciesDescription extends IClassDescription {

	/**
	 * The Interface Platform.
	 */
	interface Platform extends ISpeciesDescription {

		/**
		 * @param key
		 * @param gp
		 */
		void addPrefAsVariable(Pref<?> gp);
	}

	/**
	 * @param sd
	 * @return
	 */
	boolean hasParent(ISpeciesDescription sd);

	/**
	 * @return
	 */
	boolean isGrid();

	/**
	 * @return
	 */
	boolean isModel();

	/**
	 * @param speciesName
	 * @return
	 */
	ISpeciesDescription getMicroSpecies(String speciesName);

	/**
	 * @return
	 */
	@Override
	Class<? extends IAgent> getJavaBase();

	/**
	 * @param descriptionVisitor
	 * @return
	 */
	boolean visitMicroSpecies(DescriptionVisitor<ISpeciesDescription> descriptionVisitor);

	/**
	 * @return
	 */
	boolean belongsToAMicroModel();

	/**
	 * @return
	 */
	IAgentConstructor getAgentConstructor();

	/**
	 * @param a
	 * @return
	 */
	IDescription getAspect(String a);

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	@Override
	ISpeciesDescription getParent();

	/**
	 * @param found_sd
	 * @return
	 */
	boolean hasMacroSpecies(ISpeciesDescription sd);

	/**
	 * @return
	 */
	IExpression getSpeciesExpr();

	/**
	 * @return
	 */
	boolean isExperiment();

	/**
	 * @param varName
	 * @return
	 */
	boolean hasBehavior(String varName);

	/**
	 * @param varName
	 * @return
	 */
	boolean hasAspect(String varName);

	/**
	 * @return
	 */
	ISkillDescription getControl();

	/**
	 * @return
	 */
	boolean finalizeDescription();

	/**
	 * @return
	 */
	Iterable<ISkillDescription> getSkills();

	/**
	 * @return
	 */
	boolean isMirror();

	/**
	 * @return
	 */
	ISpeciesDescription getMacroSpecies();

	/**
	 *
	 */
	void copyJavaAdditions();

	/**
	 *
	 */
	void inheritFromParent();

	/**
	 * @param varName
	 * @return
	 */
	IStatementDescription getBehavior(String varName);

	/**
	 * Gets the behaviors.
	 *
	 * @return the behaviors
	 */
	Iterable<IStatementDescription> getBehaviors();

	/**
	 * @return
	 */
	Iterable<IStatementDescription> getAspects();

	/**
	 * @return
	 */
	Map<String, ISpeciesDescription> getOwnMicroSpecies();

	/**
	 * @param skill
	 * @return
	 */
	Boolean implementsSkill(String skill);

	/**
	 * @param temporaryActionName
	 */
	void removeAction(String temporaryActionName);

	/**
	 * @return
	 */
	Iterable<String> getSkillsNames();

	/**
	 * Compile as built in.
	 *
	 * @return the i species
	 */
	@Override
	ISpecies compileAsBuiltIn();

}
