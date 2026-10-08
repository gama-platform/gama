/*******************************************************************************************************
 *
 * SkillFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.factories;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.gaml.symbols.Facets;
import gaml.compiler.descriptions.SkillDescription;

/**
 * SpeciesFactory.
 *
 * @author drogoul 25 oct. 07
 */

// @factory (
// handles = { SKILL })
@SuppressWarnings ({ "rawtypes" })
public class SkillFactory implements ISymbolDescriptionFactory.Skill {

	/** The instance. */
	private static SkillFactory INSTANCE = new SkillFactory();

	/**
	 * Instantiates a new skill factory.
	 */
	private SkillFactory() {}

	@Override
	public IDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final IArtefact.Symbol artefact) {
		// Should not be invoked for the moment. Later probably when skills can be built in GAML
		return null;
	}

	@Override
	public ISkillDescription createBuiltInSkillDescription(final String name, final Class clazz,
			final Iterable<IDescription> children, final String plugin) {
		return new SkillDescription(name, clazz, children, plugin);
	}

	/**
	 * @return
	 */
	public static SkillFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new SkillFactory(); }
		return INSTANCE;
	}

	@Override
	public ISymbolKind[] getKinds() { return new ISymbolKind[] { ISymbolKind.SKILL }; }

}
