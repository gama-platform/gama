/*******************************************************************************************************
 *
 * SpeciesFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.factories;

import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.compilation.kernel.GamaBundleLoader;
import gama.gaml.descriptions.ClassDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.statements.Facets;

/**
 * SpeciesFactory.
 *
 * @author drogoul 25 oct. 07
 */

// @factory (
// handles = { SPECIES })
@SuppressWarnings ({ "rawtypes" })
public class SpeciesFactory extends SymbolFactory {

	@Override
	protected TypeDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription sd, final SymbolProto proto) {
		TypeDescription result = null;
		if (IKeyword.CLASS.equals(keyword)) {
			result = new ClassDescription(keyword, null, (ModelDescription) sd, null, children, element, null, facets,
					GamaBundleLoader.CURRENT_PLUGIN_NAME);
		} else {
			result = new SpeciesDescription(keyword, null, (SpeciesDescription) sd, null, children, element, facets);
		}
		return result;
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
	public SpeciesDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final SpeciesDescription superDesc, final SpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final Facets userSkills, final String plugin) {
		DescriptionFactory.addSpeciesNameAsType(name);
		return new SpeciesDescription(name, clazz, superDesc, parent, helper, skills, userSkills, plugin);
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
	 * @return the class description
	 */
	public ClassDescription createBuiltInClassDescription(final String name, final Class clazz,
			final ModelDescription macro, final ClassDescription parent) {
		DescriptionFactory.addSpeciesNameAsType(name);
		return new ClassDescription(name, clazz, macro, parent, Collections.EMPTY_LIST, null, null, Facets.NULL,
				GamaBundleLoader.CURRENT_PLUGIN_NAME);
	}

}
