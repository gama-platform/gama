/*******************************************************************************************************
 *
 * IDescriptionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Set;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;

/**
 *
 */
public interface IDescriptionFactory {

	/**
	 * Creates the.
	 *
	 * @param source
	 *            the source
	 * @param superDesc
	 *            the super desc
	 * @param cp
	 *            the cp
	 * @return the i description
	 */
	IDescription create(final ISyntacticElement source, final IDescription superDesc, final Iterable<IDescription> cp);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the model description
	 */
	IModelDescription createRootModelDescription(final String name, final Class clazz, final ISpeciesDescription macro,
			final ISpeciesDescription parent, final IAgentConstructor helper, final Set<String> skills,
			final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	IExperimentDescription createBuiltInExperimentDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param allSkills
	 *            the all skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	ISpeciesDescription.Platform createPlatformSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> allSkills, final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the i skill description
	 */
	ISkillDescription createBuiltInSkillDescription(final String name, final Class clazz,
			final Iterable<IDescription> children, final String plugin);

	/**
	 * Creates a new Description object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	ISpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final String... facets);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDescription
	 *            the super description
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDescription, final String... facets);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDesc, final Iterable<IDescription> children,
			final String... facets);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDesc, final Iterable<IDescription> children);

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final String keyword, final IDescription superDesc, final Iterable<IDescription> children,
			final Facets facets);

	/**
	 * Creates the.
	 *
	 * @param factory
	 *            the factory
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	IDescription create(final ISymbolDescriptionFactory factory, final String keyword, final IDescription superDesc,
			final Iterable<IDescription> children, final Facets facets);

}
