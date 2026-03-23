/*******************************************************************************************************
 *
 * SyntacticComposedElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.ast;

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.gaml.symbols.Facets;
import gama.api.utils.StringUtils;

/**
 * Represents a composite syntactic element that can contain child elements in the GAML AST.
 * 
 * <p>This class extends {@link AbstractSyntacticElement} to add support for hierarchical
 * structures. Elements like species, models, actions, and control structures use this class
 * as their base to manage nested statements and declarations.</p>
 * 
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li><strong>Child Management:</strong> Dynamic array-based storage for child elements</li>
 *   <li><strong>Tree Traversal:</strong> Implements visitor pattern for recursive AST navigation</li>
 *   <li><strong>Selective Visitation:</strong> Supports filtering by element type (species, grids, etc.)</li>
 *   <li><strong>Resource Management:</strong> Properly disposes children on cleanup</li>
 * </ul>
 * 
 * <p><strong>Child Storage Implementation:</strong></p>
 * <p>Children are stored in a dynamically resized array. While this requires array copying on
 * each addition, it provides:</p>
 * <ul>
 *   <li>Minimal memory overhead (no ArrayList wrapper)</li>
 *   <li>Fast iteration during traversal (no iterator objects)</li>
 *   <li>Compact representation for small child counts</li>
 * </ul>
 * 
 * <p><strong>Design Trade-offs:</strong></p>
 * <ul>
 *   <li><strong>Pro:</strong> Memory efficient, fast traversal, cache-friendly</li>
 *   <li><strong>Con:</strong> O(n) insertion cost due to array copying</li>
 *   <li><strong>Justification:</strong> AST construction is typically done once, while traversal
 *       happens many times during compilation phases</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong> NOT thread-safe. The children array is modified without
 * synchronization. Instances should only be accessed from the thread performing AST construction.</p>
 * 
 * <p><strong>Example Hierarchy:</strong></p>
 * <pre>{@code
 * model MyModel {           // SyntacticModelElement (extends SyntacticComposedElement)
 *   species MySpecies {     // SyntacticSpeciesElement (extends SyntacticComposedElement)
 *     int age;              // SyntacticAttributeElement
 *     reflex move { ... }   // SyntacticComposedElement
 *   }
 * }
 * }</pre>
 *
 * @author drogoul
 * @since 5 févr. 2012
 * @see AbstractSyntacticElement
 * @see SyntacticStructuralElement
 * @see SyntacticModelElement
 */
public class SyntacticComposedElement extends AbstractSyntacticElement {

	/**
	 * Dynamic array of child syntactic elements.
	 * 
	 * <p>Initialized to null and created on first child addition. Array is expanded
	 * by copying when new children are added. This provides minimal memory overhead
	 * while maintaining fast traversal performance.</p>
	 * 
	 * <p>Set to null after disposal to release memory and allow garbage collection.</p>
	 */
	ISyntacticElement[] children;

	/**
	 * Constructs a new syntactic composed element capable of containing children.
	 * 
	 * <p>The children array is initialized to null and will be created lazily when the
	 * first child is added via {@link #addChild(ISyntacticElement)}.</p>
	 *
	 * @param keyword   the GAML keyword identifying this element type (e.g., "species", "reflex")
	 * @param facets    the initial facets (attributes) for this element, or null if none
	 * @param statement the underlying EMF EObject from the parsed model
	 */
	SyntacticComposedElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/**
	 * Returns a detailed string representation including all children for debugging.
	 * 
	 * <p>Format: {@code <keyword> <name> <facets>\n\t<child1>\n\t<child2>...}</p>
	 * 
	 * <p>Each child is indented with a tab character for readability in debug output.</p>
	 *
	 * @return a multi-line string describing this element and its children
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		visitAllChildren(c -> sb.append(StringUtils.LN).append(StringUtils.TAB).append(c));
		return super.toString() + sb;
	}

	/**
	 * Adds a child element to this composed element.
	 * 
	 * <p>Children are appended to the end of the array in the order they are added.
	 * This maintains the source code ordering which is important for semantic correctness.</p>
	 * 
	 * <p><strong>Performance Note:</strong> This operation is O(n) where n is the current
	 * number of children, as it requires array copying. However, this is acceptable because:</p>
	 * <ul>
	 *   <li>AST construction happens once during parsing</li>
	 *   <li>Most elements have few children (typically 1-10)</li>
	 *   <li>Traversal performance is prioritized over construction performance</li>
	 * </ul>
	 * 
	 * <p><strong>Null Safety:</strong> Null children are silently ignored to avoid
	 * polluting the children array.</p>
	 *
	 * @param e the child element to add, ignored if null
	 */
	@Override
	public void addChild(final ISyntacticElement e) {

		if (e == null) return;
		if (children == null) {
			children = new ISyntacticElement[] { e };
		} else {
			children = Arrays.copyOf(children, children.length + 1);
			children[children.length - 1] = e;
		}
	}

	/**
	 * Recursively visits this element and all its descendants using the visitor pattern.
	 * 
	 * <p>This method implements depth-first traversal: visit this node, then recursively
	 * visit all children. This is the primary method for complete AST traversal.</p>
	 * 
	 * <p><strong>Performance:</strong> Enhanced for-loop is used for optimal iteration
	 * performance without iterator object allocation.</p>
	 *
	 * @param visitor the visitor to apply to each element in the subtree
	 */
	@Override
	public void visitThisAndAllChildrenRecursively(final SyntacticVisitor visitor) {
		visitor.visit(this);
		if (children != null) {
			for (final ISyntacticElement child : children) { child.visitThisAndAllChildrenRecursively(visitor); }
		}
	}

	/**
	 * Visits immediate children of this element, excluding species and grids.
	 * 
	 * <p>This method applies the {@link #OTHER_FILTER} to select only regular children
	 * (statements, actions, etc.) while skipping structural elements like species and grids.</p>
	 *
	 * @param visitor the visitor to apply to each qualifying child
	 */
	@Override
	public void visitChildren(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, OTHER_FILTER);
	}

	/**
	 * Visits children matching the specified filter predicate.
	 * 
	 * <p>This is the core filtering method used by specialized visitation methods.
	 * The filter allows selective traversal based on element properties like keyword
	 * or type.</p>
	 * 
	 * <p><strong>Common Filters:</strong></p>
	 * <ul>
	 *   <li>{@link ISyntacticElement#SPECIES_FILTER} - Only species elements</li>
	 *   <li>{@link ISyntacticElement#GRID_FILTER} - Only grid elements</li>
	 *   <li>{@link ISyntacticElement#EXPERIMENT_FILTER} - Only experiment elements</li>
	 *   <li>{@link ISyntacticElement#OTHER_FILTER} - Everything except species/grids</li>
	 * </ul>
	 *
	 * @param visitor the visitor to apply to matching children
	 * @param filter  the predicate to determine which children to visit
	 */
	protected void visitAllChildren(final SyntacticVisitor visitor, final Predicate<ISyntacticElement> filter) {
		if (children != null) {
			for (final ISyntacticElement e : children) { if (filter.test(e)) { visitor.visit(e); } }
		}
	}

	/**
	 * Checks if this element has any children.
	 * 
	 * <p>Simple null check on the children array. Returns false even if the array
	 * was allocated but is currently empty (which shouldn't happen in normal usage).</p>
	 *
	 * @return true if children array is not null, false otherwise
	 */
	@Override
	public boolean hasChildren() {
		return children != null;
	}

	/**
	 * Disposes of this element and all its children recursively.
	 * 
	 * <p>This method performs a complete cleanup:</p>
	 * <ol>
	 *   <li>Calls super.dispose() to clean up facets</li>
	 *   <li>Recursively disposes all children</li>
	 *   <li>Clears the children array reference</li>
	 * </ol>
	 * 
	 * <p>This ensures proper resource release and allows garbage collection of
	 * the entire subtree.</p>
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (children != null) { for (final ISyntacticElement e : children) { e.dispose(); } }
		clear();
	}

	/**
	 * Clears the children array, releasing the reference to allow garbage collection.
	 * 
	 * <p>This method is called during disposal but can also be used independently
	 * if child references need to be released early.</p>
	 */
	@Override
	public void clear() {
		children = null;
	}

	/**
	 * Visits immediate species children of this element.
	 * 
	 * <p>This method applies the {@link ISyntacticElement#SPECIES_FILTER} to visit
	 * only direct species children, not recursively descending into their contents.</p>
	 * 
	 * <p>Added to fix Issue #2619 - proper species traversal in model hierarchy.</p>
	 *
	 * @param visitor the visitor to apply to each species child
	 */
	@Override
	public void visitSpecies(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, SPECIES_FILTER);
	}

	/**
	 * Visits immediate grid children of this element.
	 * 
	 * <p>Grids are special species with spatial organization. This method applies
	 * the {@link ISyntacticElement#GRID_FILTER} to visit only grid children.</p>
	 * 
	 * <p>Added to fix Issue #2619 - proper grid traversal in model hierarchy.</p>
	 *
	 * @param visitor the visitor to apply to each grid child
	 */
	@Override
	public void visitGrids(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, GRID_FILTER);
	}

}
