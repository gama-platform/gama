/*******************************************************************************************************
 *
 * AbstractSyntacticElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation.ast;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.IDescription.IFacetVisitor;
import gama.gaml.statements.Facets;

/**
 * Class AbstractSyntacticElement.
 *
 * @author drogoul
 * @since 15 sept. 2013
 *
 */
public abstract class AbstractSyntacticElement implements ISyntacticElement {

	/**
	 * The facets.
	 */
	private Facets facets;

	/**
	 * The keyword.
	 */
	private String keyword;

	/**
	 * The element.
	 */
	final EObject element;

	/**
	 * Instantiates a new abstract syntactic element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param element
	 *            the element
	 */
	AbstractSyntacticElement(final String keyword, final Facets facets, final EObject element) {
		this.keyword = keyword;
		this.facets = facets;
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#getElement()
	 */
	@Override
	public EObject getElement() { return element; }

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getKeyword() + " " + getName() + " " + (facets == null ? "" : facets.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#addChild(gama.gaml.compilation.ast.ISyntacticElement)
	 */
	@Override
	public void addChild(final ISyntacticElement e) {
		throw new RuntimeException("No children allowed for " + getKeyword());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#setKeyword(java.lang.String)
	 */
	@Override
	public void setKeyword(final String name) { keyword = name; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#getKeyword()
	 */
	@Override
	public String getKeyword() { return keyword; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#hasFacets()
	 */
	@Override
	public final boolean hasFacets() {
		return facets != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#hasFacet(java.lang.String)
	 */
	@Override
	public final boolean hasFacet(final String name) {
		return facets != null && facets.containsKey(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#getExpressionAt(java.lang.String)
	 */
	@Override
	public final IExpressionDescription getExpressionAt(final String name) {
		return facets == null ? null : facets.get(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#copyFacets(gama.gaml.descriptions.SymbolProto)
	 */
	@Override
	public final Facets copyFacets(final SymbolProto sp) {
		if (facets != null) {
			final Facets ff = new Facets();
			visitFacets((a, b) -> {
				if (b != null) {
					ff.put(a, sp != null && sp.isLabel(a) ? b.cleanCopy().compileAsLabel() : b.cleanCopy());
				}
				return true;
			});
			return ff;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#setFacet(java.lang.String,
	 * gama.gaml.descriptions.IExpressionDescription)
	 */
	@Override
	public void setFacet(final String string, final IExpressionDescription expr) {
		if (expr == null) return;
		if (facets == null) { facets = new Facets(); }
		facets.put(string, expr);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#getName()
	 */
	@Override
	public String getName() {
		// Default behavior. Redefined in subclasses
		final IExpressionDescription expr = getExpressionAt(IKeyword.NAME);
		return expr == null ? null : expr.toString();
	}

	/**
	 * Removes the facet.
	 *
	 * @param name
	 *            the name
	 */
	protected void removeFacet(final String name) {
		if (facets == null) return;
		facets.remove(name);
		if (facets.isEmpty()) { facets = null; }
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#isSpecies()
	 */
	@Override
	public boolean isSpecies() { return false; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#isExperiment()
	 */
	@Override
	public boolean isExperiment() { return false; }

	
	@Override
	public boolean isData() { return false; }
	
	@Override
	public boolean isSkill() { return false; }
	
	
	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#computeStats(java.util.Map)
	 */
	@Override
	public void computeStats(final Map<String, Integer> stats) {
		final String s = getClass().getSimpleName();
		if (!stats.containsKey(s)) {
			stats.put(s, 1);
		} else {
			stats.put(s, stats.get(s) + 1);
		}
		visitAllChildren(element -> element.computeStats(stats));

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitFacets(gama.gaml.descriptions.IDescription.IFacetVisitor)
	 */
	@Override
	public void visitFacets(final IFacetVisitor visitor) {
		if (facets == null) return;
		facets.forEachFacet(visitor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#compact()
	 */
	@Override
	public void compact() {
		if (facets == null) return;
		if (facets.isEmpty()) {
			facets.dispose();
			facets = null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitThisAndAllChildrenRecursively(gama.gaml.compilation.ast.
	 * ISyntacticElement.SyntacticVisitor)
	 */
	@Override
	public void visitThisAndAllChildrenRecursively(final SyntacticVisitor visitor) {
		visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitChildren(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitChildren(final SyntacticVisitor visitor) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitSpecies(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitSpecies(final SyntacticVisitor visitor) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitExperiments(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitExperiments(final SyntacticVisitor visitor) {}
	
	
	@Override
	public void visitData(final SyntacticVisitor visitor) {}
	
	@Override
	public void visitSkills(final SyntacticVisitor visitor) {}
	

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitGrids(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitGrids(final SyntacticVisitor visitor) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.ISyntacticElement#visitAllChildren(gama.gaml.compilation.ast.ISyntacticElement.
	 * SyntacticVisitor)
	 */
	@Override
	public void visitAllChildren(final SyntacticVisitor visitor) {
		visitGrids(visitor);
		visitSpecies(visitor);
		visitChildren(visitor);
		// visitExperiments(visitor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.common.interfaces.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		if (facets != null) { facets.dispose(); }
	}

}