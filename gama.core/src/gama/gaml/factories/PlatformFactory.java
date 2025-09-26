/*******************************************************************************************************
 *
 * PlatformFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.factories;

import java.util.Set;

import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.descriptions.PlatformSpeciesDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.statements.Facets;

//
// @factory (
/**
 * A factory for creating Platform objects.
 */
// handles = { PLATFORM })
public class PlatformFactory extends SpeciesFactory {

	@Override
	public PlatformSpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final SpeciesDescription superDesc, final SpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final Facets userSkills, final String plugin) {
		DescriptionFactory.addSpeciesNameAsType(name);
		return new PlatformSpeciesDescription(name, clazz, superDesc, parent, helper, skills, userSkills, plugin);
	}

}
