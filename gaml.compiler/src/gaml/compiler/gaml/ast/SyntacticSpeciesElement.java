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
 * Represents a species or grid definition in the GAML Abstract Syntax Tree.
 * 
 * <p>Species are the fundamental agent types in GAMA simulations. This class represents
 * their syntactic structure during compilation, including attributes, actions, behaviors,
 * and nested species definitions.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Agent Definition:</strong> Defines a type of agent with properties and behaviors</li>
 *   <li><strong>Hierarchical:</strong> Can contain nested species (micro-species)</li>
 *   <li><strong>Structural Element:</strong> Has a name and can contain children</li>
 *   <li><strong>Inheritance Support:</strong> Species can inherit from other species or built-in types</li>
 * </ul>
 * 
 * <p><strong>Species vs Grid:</strong></p>
 * <ul>
 *   <li><strong>Species:</strong> General purpose agent definition with flexible spatial behavior</li>
 *   <li><strong>Grid:</strong> Specialized species with fixed spatial organization in a regular grid</li>
 * </ul>
 * 
 * <p><strong>Common Species Children:</strong></p>
 * <ul>
 *   <li><strong>Attributes:</strong> Variables defining agent state (int, float, string, etc.)</li>
 *   <li><strong>Actions:</strong> Behaviors that can be explicitly invoked</li>
 *   <li><strong>Reflexes:</strong> Automatic behaviors executed at each step</li>
 *   <li><strong>Aspects:</strong> Visual representations for display</li>
 *   <li><strong>Init:</strong> Initialization block executed at agent creation</li>
 *   <li><strong>Nested Species:</strong> Micro-species defined within the parent species</li>
 * </ul>
 * 
 * <p><strong>Example GAML:</strong></p>
 * <pre>{@code
 * species Bird skills: [moving] {
 *   float speed <- 1.0;
 *   int energy <- 100;
 *   
 *   reflex move {
 *     do wander;
 *   }
 *   
 *   aspect default {
 *     draw circle(2) color: #blue;
 *   }
 * }
 * 
 * grid cell width: 10 height: 10 {
 *   int pollution <- 0;
 * }
 * }</pre>
 * 
 * <p><strong>Recursive Structure:</strong> Species can contain nested species (micro-species),
 * which allows modeling of multi-level agent hierarchies. The {@link #visitSpecies(SyntacticVisitor)}
 * method enables recursive traversal of these nested definitions.</p>
 * 
 * <p><strong>Thread Safety:</strong> NOT thread-safe. Should only be accessed during
 * compilation phases from a single thread.</p>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see SyntacticStructuralElement
 * @see SyntacticModelElement
 * @see SyntacticTopLevelElement
 */
public class SyntacticSpeciesElement extends SyntacticStructuralElement {

	/**
	 * Constructs a new syntactic species element representing an agent type definition.
	 * 
	 * <p>Species elements are structural elements with a name and can contain various
	 * children including attributes, actions, reflexes, aspects, and nested species.</p>
	 * 
	 * <p>The keyword is typically "species" or "grid", which determines how the
	 * element is processed during compilation and how agents are spatially organized.</p>
	 *
	 * @param keyword   the GAML keyword, either "species" or "grid"
	 * @param facets    the facets including name, parent, skills, and other species attributes
	 * @param statement the underlying EMF EObject from the parsed model
	 */
	SyntacticSpeciesElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/**
	 * Visits all nested species children of this species.
	 * 
	 * <p>This method enables recursive traversal of species hierarchies. Micro-species
	 * (species defined inside other species) are visited through this method, allowing
	 * the compiler to process multi-level agent structures.</p>
	 * 
	 * <p><strong>Use Case:</strong> Collecting all species definitions in a model,
	 * including nested ones, for validation and compilation.</p>
	 * 
	 * <p><strong>Example Hierarchy:</strong></p>
	 * <pre>{@code
	 * species Hive {
	 *   species Bee {      // Micro-species
	 *     // ...
	 *   }
	 * }
	 * }</pre>
	 *
	 * @param visitor the visitor to apply to each nested species child
	 */
	@Override
	public void visitSpecies(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, SPECIES_FILTER);
	}

	/**
	 * Checks if this element represents a species definition.
	 * 
	 * <p>This is the identifying characteristic of species elements and is used
	 * throughout the compilation pipeline to distinguish species from other
	 * structural elements like experiments or models.</p>
	 * 
	 * <p><strong>Note:</strong> Both regular species and grids return true from
	 * this method, as grids are specialized species.</p>
	 *
	 * @return true, as this is a species element
	 */
	@Override
	public boolean isSpecies() { return true; }

}
