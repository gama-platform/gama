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
package gaml.compiler.factories;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.gaml.symbols.Facets;
import gaml.compiler.descriptions.ClassDescription;

/**
 * SpeciesFactory.
 *
 * @author drogoul 25 oct. 07
 */

// @factory (
// handles = { SPECIES })
@SuppressWarnings ({ "rawtypes" })
public class ClassFactory implements ISymbolDescriptionFactory.Clazz {

	/** The instance. */
	static ClassFactory INSTANCE;

	/**
	 * Gets the single instance of SpeciesFactory.
	 *
	 * @return single instance of SpeciesFactory
	 */
	public static ClassFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new ClassFactory(); }
		return INSTANCE;
	}

	/**
	 * Instantiates a new species factory.
	 */
	protected ClassFactory() {}

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
	public ITypeDescription buildDescription(final String keyword, final Facets facets, final EObject source,
			final Iterable<IDescription> children, final IDescription macroDesc, final IArtefact.Symbol artefact) {
		return new ClassDescription(keyword, null, macroDesc, null, null, source, facets, null);
	}

	@Override
	public ISymbolKind[] getKinds() { return new ISymbolKind[] { ISymbolKind.CLASS }; }

	/**
	 * Creates a new Class object.
	 *
	 * @param plugin
	 *            the plugin
	 * @return the i class description
	 */
	@Override
	public IClassDescription createBuiltInClassDescription(final String name, final String plugin) {
		return new ClassDescription(name, plugin);
	}

}
