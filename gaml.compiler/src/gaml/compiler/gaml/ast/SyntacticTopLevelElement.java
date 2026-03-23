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
 * Represents a top-level structural element in the GAML Abstract Syntax Tree.
 * 
 * <p>Top-level elements are the highest-level constructs in a GAML model hierarchy,
 * typically representing models themselves. They extend {@link SyntacticSpeciesElement}
 * to inherit species-like structural capabilities while adding top-level specific
 * behaviors like grid traversal.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Model Container:</strong> Represents the root model structure</li>
 *   <li><strong>Top-Level Grid Support:</strong> Can directly contain grid definitions</li>
 *   <li><strong>Species Hierarchy Root:</strong> Top of the species containment hierarchy</li>
 *   <li><strong>Experiment Container:</strong> Can contain experiment definitions (in subclasses)</li>
 * </ul>
 * 
 * <p><strong>Hierarchy Position:</strong></p>
 * <p>This class sits between {@link SyntacticSpeciesElement} and {@link SyntacticModelElement}
 * in the inheritance hierarchy, providing the bridge between species-like structures and
 * model-specific functionality.</p>
 * 
 * <p><strong>Inheritance Chain:</strong></p>
 * <pre>
 * AbstractSyntacticElement
 *   └── SyntacticComposedElement
 *       └── SyntacticStructuralElement
 *           └── SyntacticSpeciesElement
 *               └── SyntacticTopLevelElement (this class)
 *                   └── SyntacticModelElement
 * </pre>
 * 
 * <p><strong>Grid Handling:</strong></p>
 * <p>One of the key responsibilities of top-level elements is to support direct grid
 * definitions. Grids are specialized species with fixed spatial organization, and they
 * can only be defined at the model level (not nested within other species).</p>
 * 
 * <p><strong>Example GAML Structure:</strong></p>
 * <pre>{@code
 * model MyModel {              // SyntacticModelElement (extends this class)
 *   grid cell width: 10 height: 10 {
 *     int value <- 0;
 *   }
 *   species MySpecies { ... }
 *   experiment MyExp { ... }
 * }
 * }</pre>
 * 
 * <p><strong>Design Rationale:</strong></p>
 * <p>The separation of this class allows for potential future top-level constructs
 * beyond just models, while keeping the specialized grid visitation logic isolated
 * from regular species elements.</p>
 * 
 * <p><strong>Thread Safety:</strong> NOT thread-safe. Should only be accessed during
 * compilation phases from a single thread.</p>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see SyntacticSpeciesElement
 * @see SyntacticModelElement
 * @see SyntacticComposedElement
 */
public class SyntacticTopLevelElement extends SyntacticSpeciesElement {

	/**
	 * Constructs a new syntactic top-level element.
	 * 
	 * <p>Top-level elements inherit all capabilities of structural elements and species,
	 * including name caching, child management, and species/grid traversal.</p>
	 * 
	 * <p>This constructor is typically called when creating model elements, which are
	 * the primary concrete implementations of top-level elements.</p>
	 *
	 * @param keyword   the GAML keyword, typically "model" or related top-level constructs
	 * @param facets    the initial facets for this element, including name and model attributes
	 * @param statement the underlying EMF EObject from the parsed model
	 */
	SyntacticTopLevelElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/**
	 * Visits all grid children of this top-level element.
	 * 
	 * <p>This method enables selective traversal of grid definitions at the model level.
	 * Grids are specialized species with regular spatial organization and can only be
	 * defined at the top level, not nested within other species.</p>
	 * 
	 * <p><strong>Grid Characteristics:</strong></p>
	 * <ul>
	 *   <li>Fixed spatial structure (rectangular grid)</li>
	 *   <li>Cells are created automatically at initialization</li>
	 *   <li>Efficient neighbor access and spatial queries</li>
	 *   <li>Cannot be nested within other species</li>
	 * </ul>
	 * 
	 * <p>The {@link ISyntacticElement#GRID_FILTER} is used to identify grid children
	 * based on their keyword being {@link IKeyword#GRID}.</p>
	 * 
	 * <p><strong>Example:</strong></p>
	 * <pre>{@code
	 * grid cell width: 50 height: 50 {
	 *   int pollution <- 0;
	 *   rgb color <- #white;
	 * }
	 * }</pre>
	 *
	 * @param visitor the visitor to apply to each grid child
	 */
	@Override
	public void visitGrids(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, GRID_FILTER);
	}

}