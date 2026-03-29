/*******************************************************************************************************
 *
 * SyntacticAttributeElement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ast;

import org.eclipse.emf.ecore.EObject;

/**
 * Represents an attribute (variable or field) declaration in the GAML Abstract Syntax Tree.
 * 
 * <p>Attribute elements are lightweight syntactic elements that represent variable declarations
 * within species, models, or other structural elements. Unlike composed elements, attributes
 * cannot have children and represent leaf nodes in the AST.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Immutable Name:</strong> The attribute name is stored directly and never changes</li>
 *   <li><strong>No Children:</strong> Extends {@link SyntacticSingleElement} which prohibits children</li>
 *   <li><strong>Optional Facets:</strong> Attributes may have facets for type, initial value, constraints, etc.</li>
 *   <li><strong>Lightweight:</strong> Minimal memory footprint with cached name</li>
 * </ul>
 * 
 * <p><strong>Common Attribute Facets:</strong></p>
 * <ul>
 *   <li>{@code type} - The data type of the attribute</li>
 *   <li>{@code init} - Initial value expression</li>
 *   <li>{@code value} - Computed value expression</li>
 *   <li>{@code const} - Whether the attribute is constant</li>
 *   <li>{@code function} - Function expression for computed attributes</li>
 * </ul>
 * 
 * <p><strong>Memory Optimization:</strong> The name is cached at construction time to avoid
 * repeated facet lookups, improving both memory and performance.</p>
 * 
 * <p><strong>Example GAML:</strong></p>
 * <pre>{@code
 * species MySpecies {
 *     int age <- 0;        // SyntacticAttributeElement with keyword="int", name="age"
 *     float speed <- 1.0;  // SyntacticAttributeElement with keyword="float", name="speed"
 * }
 * }</pre>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see SyntacticSingleElement
 * @see SyntacticStructuralElement
 */
public class SyntacticAttributeElement extends SyntacticSingleElement {

	/**
	 * The cached name of this attribute.
	 * Stored directly for fast access and to avoid repeated facet lookups.
	 * This field is immutable after construction.
	 */
	final String name;

	/**
	 * Constructs a new syntactic attribute element representing a variable or field declaration.
	 * 
	 * <p>Note that facets are intentionally set to null in the super constructor call, as
	 * attributes typically don't have facets in the traditional sense. Any attribute properties
	 * (type, initial value, etc.) would be added later through {@link #setFacet(String, IExpressionDescription)}.</p>
	 *
	 * @param keyword   the GAML keyword identifying the attribute type (e.g., "int", "float", "string", "var")
	 * @param name      the name of the attribute, must not be null
	 * @param statement the underlying EMF EObject from the parsed model providing source location
	 */
	public SyntacticAttributeElement(final String keyword, final String name, final EObject statement) {
		super(keyword, null, statement);
		this.name = name;
	}

	/**
	 * Returns a human-readable string representation for debugging.
	 * 
	 * <p>Format: {@code "Attribute <name>"}</p>
	 *
	 * @return a string describing this attribute
	 */
	@Override
	public String toString() {
		return "Attribute " + getName();
	}

	/**
	 * Returns the cached name of this attribute.
	 * 
	 * <p>This method overrides the default implementation to return the cached name
	 * directly, avoiding facet lookups and providing better performance.</p>
	 *
	 * @return the attribute name, never null
	 */
	@Override
	public String getName() { return name; }

}
