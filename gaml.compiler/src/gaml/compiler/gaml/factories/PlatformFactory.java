/*******************************************************************************************************
 *
 * PlatformFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.factories;

import java.util.Set;

import gama.annotations.support.ISymbolKind;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gaml.compiler.gaml.descriptions.PlatformSpeciesDescription;

//
// @factory (
/**
 * A factory for creating Platform objects.
 */
// handles = { PLATFORM })
public class PlatformFactory extends SpeciesFactory {

	/** The instance. */
	private static PlatformFactory INSTANCE;

	/**
	 * Gets the single instance of PlatformFactory.
	 *
	 * @return single instance of PlatformFactory
	 */
	public static PlatformFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new PlatformFactory(); }
		return INSTANCE;
	}

	@Override
	public ISpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription superDesc, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final Facets userSkills, final String plugin) {
		ArtefactProtoRegistry.addBuiltInSpeciesNameAsType(name);
		return new PlatformSpeciesDescription(name, clazz, superDesc, parent, helper, skills, userSkills, plugin);
	}

	@Override
	public ISymbolKind[] getKinds() { return new ISymbolKind[] { ISymbolKind.PLATFORM }; }

}
