/*******************************************************************************************************
 *
 * SyntacticSpeciesElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.ast;

import org.eclipse.emf.ecore.EObject;

import gama.api.gaml.symbols.Facets;

/**
 * Class GlobalSyntacticElement.
 *
 * @author drogoul
 * @since 9 sept. 2013
 *
 */
public class SyntacticSpeciesElement extends SyntacticStructuralElement {

	/**
	 * Instantiates a new syntactic species element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param statement
	 *            the statement
	 */
	SyntacticSpeciesElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.AbstractSyntacticElement#visitSpecies(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitSpecies(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, SPECIES_FILTER);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.AbstractSyntacticElement#isSpecies()
	 */
	@Override
	public boolean isSpecies() { return true; }

}
