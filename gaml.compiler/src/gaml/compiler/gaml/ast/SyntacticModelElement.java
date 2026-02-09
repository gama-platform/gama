/*******************************************************************************************************
 *
 * SyntacticModelElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.ast;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.constants.IKeyword;
import gama.api.gaml.symbols.Facets;

/**
 * Class SyntacticModelElement.
 *
 * @author drogoul
 * @since 12 avr. 2014
 *
 */
public class SyntacticModelElement extends SyntacticTopLevelElement {

	/**
	 * The path.
	 */
	final private String path;

	/**
	 * Instantiates a new syntactic model element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param statement
	 *            the statement
	 * @param path
	 *            the path
	 * @param imports
	 *            the imports
	 */
	public SyntacticModelElement(final String keyword, final Facets facets, final EObject statement,
			final String path) {
		super(keyword, facets, statement);
		if (path != null) {
			final String p = path;
			this.path = p.endsWith(File.pathSeparator) ? p : p + "/";
		} else {
			// Case of ill resources (compilation of blocks)
			this.path = null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.SyntacticSpeciesElement#isSpecies()
	 */
	@Override
	public boolean isSpecies() { return false; }

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gama.gaml.compilation.ast.AbstractSyntacticElement#visitExperiments(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitExperiments(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, EXPERIMENT_FILTER);
	}

	/**
	 * The compacter.
	 */
	static SyntacticVisitor compacter = ISyntacticElement::compact;

	/**
	 * Compact model.
	 */
	public void compactModel() {
		this.visitThisAndAllChildrenRecursively(compacter);
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath() { return path; }

	/**
	 * Gets the pragmas.
	 *
	 * @return the pragmas
	 */
	@Override
	@SuppressWarnings ("unchecked")
	public Map<String, List<String>> getPragmas() {
		if (hasFacet(IKeyword.PRAGMA))
			return (Map<String, List<String>>) this.getExpressionAt(IKeyword.PRAGMA).getExpression().getConstValue();
		return null;
	}

}
