/*******************************************************************************************************
 *
 * SyntacticSkillElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation.ast;

import org.eclipse.emf.ecore.EObject;

import gama.gaml.statements.Facets;

/**
 * Class GlobalSyntacticElement.
 *
 * @author drogoul
 * @since 9 sept. 2013
 *
 */
public class SyntacticSkillElement extends SyntacticStructuralElement {

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
	SyntacticSkillElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	@Override
	public void visitSpecies(final SyntacticVisitor visitor) {}

	@Override
	public void visitGrids(final SyntacticVisitor visitor) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.AbstractSyntacticElement#isSpecies()
	 */
	@Override
	public boolean isSpecies() { return true; }

}
