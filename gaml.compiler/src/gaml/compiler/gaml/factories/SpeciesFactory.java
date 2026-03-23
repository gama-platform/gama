/*******************************************************************************************************
 *
 * SpeciesFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.factories;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gaml.compiler.gaml.descriptions.SpeciesDescription;

/**
 * SpeciesFactory.
 *
 * @author drogoul 25 oct. 07
 */

// @factory (
// handles = { SPECIES })
@SuppressWarnings ({ "rawtypes" })
public class SpeciesFactory implements ISymbolDescriptionFactory.Species {

	/** The instance. */
	static SpeciesFactory INSTANCE;

	/**
	 * Gets the single instance of SpeciesFactory.
	 *
	 * @return single instance of SpeciesFactory
	 */
	public static SpeciesFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new SpeciesFactory(); }
		return INSTANCE;
	}

	/**
	 * Instantiates a new species factory.
	 */
	protected SpeciesFactory() {}

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
	 * @param sd
	 *            the sd
	 * @param artefact
	 *            the artefact
	 * @return the type description
	 */
	@Override
	public ITypeDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription sd, final IArtefact.Symbol artefact) {
		return new SpeciesDescription(keyword, null, (ISpeciesDescription) sd, null, children, element, facets);
	}

	/**
	 * Creates a new Species object.
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
	 * @param userSkills
	 *            the user skills
	 * @param plugin
	 *            the plugin
	 * @return the species description
	 */
	public ISpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription superDesc, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final Facets userSkills, final String plugin) {
		// ArtefactRegistry.addBuiltInSpeciesNameAsType(name);
		return new SpeciesDescription(name, clazz, superDesc, parent, helper, skills, userSkills, plugin);
	}

	@Override
	public ISpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin) {
		return createBuiltInSpeciesDescription(name, clazz, macro, parent, helper, skills, null, plugin);
	}

	@Override
	public ISymbolKind[] getKinds() { return new ISymbolKind[] { ISymbolKind.SPECIES }; }

}
