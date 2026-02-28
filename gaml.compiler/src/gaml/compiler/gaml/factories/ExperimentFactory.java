/*******************************************************************************************************
 *
 * ExperimentFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
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
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gaml.compiler.gaml.descriptions.ExperimentDescription;

/**
 * The Class EnvironmentFactory.
 *
 * @author drogoul
 */
// @factory (
// handles = { ISymbolKind.EXPERIMENT })
public class ExperimentFactory extends SpeciesFactory {

	/** The instance. */
	static ExperimentFactory INSTANCE;

	/**
	 * Gets the single instance of ExperimentFactory.
	 *
	 * @return single instance of ExperimentFactory
	 */
	public static ExperimentFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new ExperimentFactory(); }
		return INSTANCE;
	}

	/**
	 * Instantiates a new experiment factory.
	 */
	private ExperimentFactory() {}

	/**
	 * Creates a new Experiment object.
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
	 * @return the experiment description
	 */
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IExperimentDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription superDesc, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final Facets userSkills, final String plugin) {
		// ArtefactRegistry.addBuiltInSpeciesNameAsType(name);
		return new ExperimentDescription(name, clazz, superDesc, parent, helper, skills, userSkills, plugin);
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
	 * @param sd
	 *            the sd
	 * @param artefact
	 *            the artefact
	 * @return the experiment description
	 */
	@Override
	public IExperimentDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription sd, final IArtefact.Symbol proto) {
		return new ExperimentDescription(keyword, (ISpeciesDescription) sd, children, element, facets);
	}

	@Override
	public ISymbolKind[] getKinds() { return new ISymbolKind[] { ISymbolKind.EXPERIMENT }; }

}
