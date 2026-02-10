/*******************************************************************************************************
 *
 * SyntacticSingleElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
 * Represents a syntactic element that cannot contain children in the GAML Abstract Syntax Tree.
 * 
 * <p>Single elements are leaf nodes in the AST that represent atomic statements or declarations
 * without nested content. They are lightweight and memory-efficient, as they don't maintain
 * child collections or support tree traversal operations.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>No Children:</strong> Cannot add or contain child elements</li>
 *   <li><strong>Leaf Nodes:</strong> Represent terminal constructs in the AST</li>
 *   <li><strong>Memory Efficient:</strong> No child array overhead</li>
 *   <li><strong>Simple Statements:</strong> Typically represent simple statements or expressions</li>
 * </ul>
 * 
 * <p><strong>Common Use Cases:</strong></p>
 * <ul>
 *   <li><strong>Simple Statements:</strong> create, ask, do, return, write</li>
 *   <li><strong>Declarations:</strong> Without bodies or nested content</li>
 *   <li><strong>Assignments:</strong> Variable assignments and updates</li>
 *   <li><strong>Expressions:</strong> Standalone expression statements</li>
 * </ul>
 * 
 * <p><strong>Example GAML Constructs:</strong></p>
 * <pre>{@code
 * write "Hello";           // SyntacticSingleElement - simple statement
 * create species: Bird;    // SyntacticSingleElement - create statement
 * return result;           // SyntacticSingleElement - return statement
 * x <- x + 1;              // SyntacticSingleElement - assignment
 * }</pre>
 * 
 * <p><strong>Comparison with SyntacticComposedElement:</strong></p>
 * <ul>
 *   <li><strong>SyntacticSingleElement:</strong> No children, lighter memory footprint</li>
 *   <li><strong>SyntacticComposedElement:</strong> Has children array, supports nesting</li>
 * </ul>
 * 
 * <p><strong>Design Pattern:</strong> This class implements the Composite pattern's "Leaf" role,
 * providing default implementations for tree operations that are no-ops for leaf nodes.</p>
 * 
 * <p><strong>Thread Safety:</strong> Instances are thread-safe after construction, as they
 * don't modify their state (beyond inherited facet operations from the parent class).</p>
 *
 * @author drogoul
 * @since 5 févr. 2012
 * @see AbstractSyntacticElement
 * @see SyntacticComposedElement
 * @see SyntacticAttributeElement
 */
public class SyntacticSingleElement extends AbstractSyntacticElement {

	/**
	 * Constructs a new syntactic single element without child support.
	 * 
	 * <p>Single elements are created for statements and declarations that don't
	 * contain nested structures. They provide a lightweight alternative to
	 * composed elements when no children are needed.</p>
	 * 
	 * <p><strong>Memory Note:</strong> No child array is allocated, saving memory
	 * compared to {@link SyntacticComposedElement}.</p>
	 *
	 * @param keyword   the GAML keyword identifying the statement type (e.g., "write", "create", "return")
	 * @param facets    the initial facets (attributes) for this element, or null if none
	 * @param statement the underlying EMF EObject from the parsed model
	 */
	SyntacticSingleElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/**
	 * Checks if this element has any children.
	 * 
	 * <p>Single elements never have children by design. This method always returns false
	 * and is part of the {@link ISyntacticElement} contract for tree navigation.</p>
	 * 
	 * <p><strong>Composite Pattern:</strong> This implements the "Leaf" behavior
	 * in the Composite pattern, where leaves report having no children.</p>
	 *
	 * @return false, as single elements cannot have children
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

}
