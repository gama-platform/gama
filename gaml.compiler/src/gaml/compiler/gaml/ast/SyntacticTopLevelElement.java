/*******************************************************************************************************
 *
 * SyntacticTopLevelElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
 * The Class SyntacticTopLevelElement.
 */
public class SyntacticTopLevelElement extends SyntacticSpeciesElement {

	/**
	 * Instantiates a new syntactic top level element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param statement
	 *            the statement
	 */
	SyntacticTopLevelElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.AbstractSyntacticElement#visitGrids(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitGrids(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, GRID_FILTER);
	}

}