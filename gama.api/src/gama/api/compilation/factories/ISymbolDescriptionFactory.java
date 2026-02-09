/*******************************************************************************************************
 *
 * ISymbolDescriptionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;

/**
 * Written by Alexis Drogoul Modified on 11 mai 2010
 *
 * @todo Description
 *
 */
public interface ISymbolDescriptionFactory extends ISymbolKind {

	/**
	 * The Interface Species.
	 */
	interface Species extends ISymbolDescriptionFactory {

		/**
		 * @param name
		 * @param clazz
		 * @param macro
		 * @param parent
		 * @param helper
		 * @param skills
		 * @param object
		 * @param plugin
		 * @return
		 */
		ISpeciesDescription createBuiltInSpeciesDescription(String name, Class clazz, ISpeciesDescription macro,
				ISpeciesDescription parent, IAgentConstructor helper, Set<String> skills, String plugin);

	}

	/**
	 * The Interface Skill.
	 */
	interface Skill extends ISymbolDescriptionFactory {

		/**
		 * @param name
		 * @param clazz
		 * @param children
		 * @param plugin
		 * @return
		 */
		ISkillDescription createBuiltInSkillDescription(String name, Class clazz, Iterable<IDescription> children,
				String plugin);
	}

	/**
	 * Builds the description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param element
	 *            the element
	 * @param children
	 *            the children
	 * @param enclosing
	 *            the enclosing
	 * @param proto
	 *            the proto
	 * @return the i description
	 */
	IDescription buildDescription(String keyword, Facets facets, EObject element, Iterable<IDescription> children,
			IDescription enclosing, IArtefactProto.Symbol proto);

	/**
	 * Gets the kinds.
	 *
	 * @return the kinds
	 */
	int[] getKinds();

}
