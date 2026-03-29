/*******************************************************************************************************
 *
 * AbstractSyntacticElement.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ast;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IDescription.IFacetVisitor;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;

/**
 * Abstract base class for all syntactic elements in the GAML Abstract Syntax Tree (AST).
 *
 * <p>
 * This class provides the core implementation for representing parsed GAML language constructs during the compilation
 * process. Each syntactic element corresponds to a language construct like species, experiments, statements, or
 * expressions.
 * </p>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * </p>
 * <ul>
 * <li><strong>Element Identity:</strong> Maintains keyword, name, and source EObject reference</li>
 * <li><strong>Facet Management:</strong> Stores and manages element attributes (facets) as key-value pairs</li>
 * <li><strong>Tree Traversal:</strong> Provides visitor pattern methods for navigating the AST hierarchy</li>
 * <li><strong>Element Classification:</strong> Identifies element types (species, experiments, etc.)</li>
 * <li><strong>Statistics Computation:</strong> Supports AST analysis and metrics collection</li>
 * </ul>
 *
 * <p>
 * <strong>Design Patterns:</strong>
 * </p>
 * <ul>
 * <li><strong>Visitor Pattern:</strong> Used for tree traversal via {@code visit*()} methods</li>
 * <li><strong>Template Method:</strong> Defines algorithm structure with customization points for subclasses</li>
 * <li><strong>Null Object:</strong> Returns null or empty structures for missing data</li>
 * </ul>
 *
 * <p>
 * <strong>Memory Optimization:</strong>
 * </p>
 * <ul>
 * <li>Facets are lazily initialized and nullified when empty to reduce memory footprint</li>
 * <li>The {@link #compact()} method removes empty facet collections</li>
 * <li>Disposal pattern via {@link #dispose()} for proper cleanup</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong> This class is NOT thread-safe. Instances should be accessed from a single thread or
 * externally synchronized during AST construction and traversal.
 * </p>
 *
 * <p>
 * <strong>Subclass Hierarchy:</strong>
 * </p>
 * <ul>
 * <li>{@link SyntacticSingleElement} - Elements without children</li>
 * <li>{@link SyntacticComposedElement} - Elements with nested children</li>
 * <li>{@link SyntacticStructuralElement} - Named structural elements (species, experiments)</li>
 * </ul>
 *
 * @author drogoul
 * @since 15 sept. 2013
 * @see ISyntacticElement
 * @see SyntacticFactory
 */
public abstract class AbstractSyntacticElement implements ISyntacticElement {

	/**
	 * The facets (attributes) of this element, stored as key-value pairs. Lazily initialized and set to null when empty
	 * to optimize memory usage.
	 */
	private Facets facets;

	/**
	 * The GAML keyword that identifies this element's type (e.g., "species", "experiment", "action").
	 */
	private String keyword;

	/**
	 * The underlying EMF EObject from the parsed model. This provides access to the original source location and
	 * structure. Made final as it should never change after construction.
	 */
	final EObject element;

	/**
	 * Constructs a new abstract syntactic element with the specified properties.
	 *
	 * <p>
	 * This constructor is package-private and should only be called by subclasses through the {@link SyntacticFactory}.
	 * </p>
	 *
	 * @param keyword
	 *            the GAML keyword identifying this element type (e.g., "species", "experiment"). Must not be null for
	 *            proper element identification.
	 * @param facets
	 *            the initial facets (attributes) for this element, or null if none. Facets will be lazily initialized
	 *            on first use if null.
	 * @param element
	 *            the underlying EMF EObject from the parsed model. Should not be null as it provides source location
	 *            information for error reporting.
	 */
	AbstractSyntacticElement(final String keyword, final Facets facets, final EObject element) {
		this.keyword = keyword;
		this.facets = facets;
		this.element = element;
	}

	/**
	 * Returns the underlying EMF EObject from the parsed model.
	 *
	 * <p>
	 * The EObject provides access to the original source structure and location, which is essential for error reporting
	 * and debugging.
	 * </p>
	 *
	 * @return the EMF EObject representing this element in the parsed model, may be null for synthetic elements
	 */
	@Override
	public EObject getElement() { return element; }

	/**
	 * Returns a string representation of this syntactic element for debugging purposes.
	 *
	 * <p>
	 * The format is: {@code <keyword> <name> <facets>}
	 * </p>
	 *
	 * @return a string describing this element's keyword, name, and facets
	 */
	@Override
	public String toString() {
		return getKeyword() + " " + getName() + " " + (facets == null ? "" : facets.toString());
	}

	/**
	 * Adds a child element to this syntactic element.
	 *
	 * <p>
	 * The default implementation throws a RuntimeException as base elements do not support children. Override in
	 * subclasses like {@link SyntacticComposedElement} to enable child management.
	 * </p>
	 *
	 * @param e
	 *            the child element to add
	 * @throws RuntimeException
	 *             always, as this element type doesn't support children
	 */
	@Override
	public void addChild(final ISyntacticElement e) {
		throw new RuntimeException("No children allowed for " + getKeyword());
	}

	/**
	 * Sets the keyword that identifies this element's type.
	 *
	 * <p>
	 * Use with caution as changing the keyword may affect how the element is processed during compilation.
	 * </p>
	 *
	 * @param name
	 *            the new keyword to set
	 */
	@Override
	public void setKeyword(final String name) { keyword = name; }

	/**
	 * Returns the GAML keyword identifying this element's type.
	 *
	 * @return the keyword (e.g., "species", "experiment", "action"), may be null
	 */
	@Override
	public String getKeyword() { return keyword; }

	/**
	 * Checks if this element has any facets (attributes) defined.
	 *
	 * <p>
	 * This is an optimized check that doesn't create the facets collection if it doesn't exist yet.
	 * </p>
	 *
	 * @return true if facets exist and the collection is not null, false otherwise
	 */
	@Override
	public final boolean hasFacets() {
		return facets != null;
	}

	/**
	 * Checks if this element has a facet with the specified name.
	 *
	 * <p>
	 * This method performs a null-safe check without initializing the facets collection if it doesn't exist.
	 * </p>
	 *
	 * @param name
	 *            the facet name to check
	 * @return true if the facet exists, false otherwise
	 */
	@Override
	public final boolean hasFacet(final String name) {
		return facets != null && facets.containsKey(name);
	}

	/**
	 * Returns the expression description for a facet with the specified name.
	 *
	 * <p>
	 * Facets represent attributes like name, type, value, etc. This method provides safe access without throwing
	 * exceptions for missing facets.
	 * </p>
	 *
	 * @param name
	 *            the facet name to retrieve
	 * @return the expression description for the facet, or null if not found
	 */
	@Override
	public final IExpressionDescription getExpressionAt(final String name) {
		return facets == null ? null : facets.get(name);
	}

	/**
	 * Creates a deep copy of this element's facets, optionally processing labels.
	 *
	 * <p>
	 * This method is essential during AST transformation and validation. It creates clean copies of expression
	 * descriptions to avoid shared mutable state between syntactic and semantic models.
	 * </p>
	 *
	 * <p>
	 * Label facets are specially handled: they are compiled as labels which affects how they are validated and resolved
	 * during compilation.
	 * </p>
	 *
	 * @param sp
	 *            the symbol prototype defining which facets are labels, or null to skip label processing
	 * @return a new Facets object with clean copies of all facets, or null if no facets exist
	 */
	@Override
	public final Facets copyFacets(final IArtefact.Symbol sp) {
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

	/**
	 * Sets a facet (attribute) with the specified name and expression.
	 *
	 * <p>
	 * Facets are lazily initialized - the collection is only created when the first facet is set. This reduces memory
	 * usage for simple elements.
	 * </p>
	 *
	 * @param string
	 *            the facet name
	 * @param expr
	 *            the expression description for the facet value, ignored if null
	 */
	@Override
	public void setFacet(final String string, final IExpressionDescription expr) {
		if (expr == null) return;
		if (facets == null) { facets = new Facets(); }
		facets.put(string, expr);
	}

	/**
	 * Returns the name of this element.
	 *
	 * <p>
	 * Default implementation extracts the name from the "name" facet. Subclasses may override to cache the name or
	 * compute it differently.
	 * </p>
	 *
	 * @return the element's name extracted from the name facet, or null if not found
	 */
	@Override
	public String getName() {
		// Default behavior. Redefined in subclasses
		final IExpressionDescription expr = getExpressionAt(IKeyword.NAME);
		return expr == null ? null : expr.toString();
	}

	/**
	 * Removes a facet with the specified name.
	 *
	 * <p>
	 * If removing the facet results in an empty collection, the facets object is nullified to save memory.
	 * </p>
	 *
	 * @param name
	 *            the name of the facet to remove
	 */
	protected void removeFacet(final String name) {
		if (facets == null) return;
		facets.remove(name);
		if (facets.isEmpty()) { facets = null; }
	}

	/**
	 * Computes statistics about the AST structure by counting element types.
	 *
	 * <p>
	 * This method recursively visits all children and increments counters in the provided map. Used for AST analysis,
	 * profiling, and debugging.
	 * </p>
	 *
	 * <p>
	 * <strong>Optimization Note:</strong> Uses {@code computeIfAbsent} for cleaner code and better performance with
	 * modern JVMs.
	 * </p>
	 *
	 * @param stats
	 *            a mutable map from class simple name to occurrence count
	 */
	@Override
	public void computeStats(final Map<String, Integer> stats) {
		final String s = getClass().getSimpleName();
		stats.merge(s, 1, Integer::sum);
		visitAllChildren(element -> element.computeStats(stats));
	}

	/**
	 * Applies a visitor to all facets of this element.
	 *
	 * <p>
	 * The visitor pattern allows processing facets without exposing the internal facets collection. The visitor can
	 * terminate early by returning false.
	 * </p>
	 *
	 * @param visitor
	 *            the facet visitor to apply to each facet
	 */
	@Override
	public void visitFacets(final IFacetVisitor visitor) {
		if (facets == null) return;
		facets.forEachFacet(visitor);
	}

	/**
	 * Compacts this element by removing empty facets collection.
	 *
	 * <p>
	 * This memory optimization should be called after AST construction is complete and no more facets will be added.
	 * The {@link SyntacticModelElement#compactModel()} method applies this recursively to the entire tree.
	 * </p>
	 */
	@Override
	public void compact() {
		if (facets == null) return;
		if (facets.isEmpty()) {
			facets.dispose();
			facets = null;
		}
	}

	/**
	 * Visits this element and all its children recursively using the visitor pattern.
	 *
	 * <p>
	 * Default implementation only visits this element. Composed elements override to recursively visit children.
	 * </p>
	 *
	 * @param visitor
	 *            the visitor to apply to each element in the tree
	 */
	@Override
	public void visitThisAndAllChildrenRecursively(final SyntacticVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Visits immediate children of this element (non-species, non-grid).
	 *
	 * <p>
	 * Default implementation does nothing as base elements have no children. Override in subclasses to implement child
	 * visitation.
	 * </p>
	 *
	 * @param visitor
	 *            the visitor to apply to each child
	 */
	@Override
	public void visitChildren(final SyntacticVisitor visitor) {}

	/**
	 * Visits all species children of this element.
	 *
	 * <p>
	 * Species are structural elements that define agent types. This method allows selective traversal of species
	 * definitions.
	 * </p>
	 *
	 * @param visitor
	 *            the visitor to apply to each species child
	 */
	@Override
	public void visitSpecies(final SyntacticVisitor visitor) {}

	/**
	 * Visits all experiment children of this element.
	 *
	 * <p>
	 * Experiments are typically found at the model level and define simulation execution scenarios.
	 * </p>
	 *
	 * @param visitor
	 *            the visitor to apply to each experiment child
	 */
	@Override
	public void visitExperiments(final SyntacticVisitor visitor) {}

	/**
	 * Visits all grid children of this element.
	 *
	 * <p>
	 * Grids are special species that represent spatially organized agent populations. This method allows separate
	 * processing of grid definitions.
	 * </p>
	 *
	 * @param visitor
	 *            the visitor to apply to each grid child
	 */
	@Override
	public void visitGrids(final SyntacticVisitor visitor) {}

	/**
	 * Visits all children of this element in order: grids, species, then other children.
	 *
	 * <p>
	 * This method establishes the standard traversal order for AST processing. Note that experiments are intentionally
	 * excluded from this traversal as they are processed separately.
	 * </p>
	 *
	 * @param visitor
	 *            the visitor to apply to each child
	 */
	@Override
	public void visitAllChildren(final SyntacticVisitor visitor) {
		visitGrids(visitor);
		visitSpecies(visitor);
		visitChildren(visitor);
		// visitExperiments(visitor);
	}

	/**
	 * Disposes of this element and releases resources.
	 *
	 * <p>
	 * This method should be called when the element is no longer needed to allow garbage collection. It disposes the
	 * facets collection but doesn't modify the EObject reference.
	 * </p>
	 */
	@Override
	public void dispose() {
		if (facets != null) { facets.dispose(); }
	}

	/**
	 * Returns the file path associated with this element.
	 *
	 * <p>
	 * Default implementation returns null. Overridden in {@link SyntacticModelElement} to return the model's file path.
	 * </p>
	 *
	 * @return the file path or null if not applicable
	 */
	@Override
	public String getPath() { return null; }

}