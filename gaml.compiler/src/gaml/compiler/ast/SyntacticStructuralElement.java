/*******************************************************************************************************
 *
 * SyntacticStructuralElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ast;

import org.eclipse.emf.ecore.EObject;

import gama.api.gaml.symbols.Facets;

/**
 * Represents a named structural element in the GAML Abstract Syntax Tree.
 * 
 * <p>Structural elements are composed elements that have an identifying name, such as
 * species, models, experiments, and actions. This class extends {@link SyntacticComposedElement}
 * to add name caching for performance optimization.</p>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Named Elements:</strong> All structural elements have an identifying name</li>
 *   <li><strong>Name Caching:</strong> Name is cached at construction for fast repeated access</li>
 *   <li><strong>Composed Structure:</strong> Can contain children like attributes, actions, etc.</li>
 *   <li><strong>Base for Hierarchy:</strong> Parent class for species, experiments, and models</li>
 * </ul>
 * 
 * <p><strong>Name Resolution Strategy:</strong></p>
 * <p>The name is extracted from the {@link IKeyword#NAME} facet and cached in a field.
 * This optimization is important because:</p>
 * <ul>
 *   <li>Names are accessed frequently during compilation and validation</li>
 *   <li>Repeated facet lookups would be expensive</li>
 *   <li>Names don't change after construction</li>
 * </ul>
 * 
 * <p><strong>Lazy Initialization:</strong></p>
 * <p>The name field is initialized at construction if available, but can be lazily
 * resolved later if initially null. This handles edge cases where name facets are
 * added after construction.</p>
 * 
 * <p><strong>Subclass Hierarchy:</strong></p>
 * <pre>
 * SyntacticStructuralElement
 *   ├── SyntacticSpeciesElement (species and grids)
 *   ├── SyntacticExperimentElement (experiments)
 *   └── SyntacticTopLevelElement (models)
 * </pre>
 * 
 * <p><strong>Performance Optimization:</strong></p>
 * <p>By caching the name, this class eliminates repeated string operations and facet
 * lookups, which is significant for large models with hundreds of structural elements.</p>
 * 
 * <p><strong>Thread Safety:</strong> NOT thread-safe. The name caching logic uses
 * lazy initialization without synchronization.</p>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see SyntacticComposedElement
 * @see SyntacticSpeciesElement
 * @see SyntacticExperimentElement
 */
public class SyntacticStructuralElement extends SyntacticComposedElement {

	/**
	 * Cached name of this structural element.
	 * 
	 * <p>Initialized from the {@link IKeyword#NAME} facet at construction or
	 * lazily resolved on first access if initially null. Once set, the name
	 * should remain constant.</p>
	 * 
	 * <p><strong>Optimization Note:</strong> This field avoids repeated facet
	 * lookups and string conversions, improving performance for large models.</p>
	 */
	String name;

	/**
	 * Constructs a new syntactic structural element with name caching.
	 * 
	 * <p>The name is immediately extracted from the {@link IKeyword#NAME} facet
	 * and cached for fast repeated access. If the name facet is not present at
	 * construction time, the name field will be null and lazily initialized on
	 * first access.</p>
	 * 
	 * <p><strong>Performance Note:</strong> Eagerly caching the name at construction
	 * avoids repeated facet lookups during the compilation process, which can
	 * significantly improve performance for large models.</p>
	 *
	 * @param keyword   the GAML keyword identifying the element type (e.g., "species", "experiment")
	 * @param facets    the initial facets for this element, typically including the name facet
	 * @param statement the underlying EMF EObject from the parsed model
	 */
	public SyntacticStructuralElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
		name = super.getName();
	}

	/**
	 * Returns the cached name of this structural element.
	 * 
	 * <p>This method uses lazy initialization: if the name wasn't set at construction
	 * (because the facet wasn't present), it will be resolved from the parent's
	 * {@code getName()} method on first access and cached for subsequent calls.</p>
	 * 
	 * <p><strong>Optimization:</strong> After the first call, this method simply
	 * returns the cached field value, avoiding facet lookups and string operations.</p>
	 * 
	 * <p><strong>Thread Safety Note:</strong> The lazy initialization is not
	 * thread-safe. Multiple threads calling this method concurrently on a new
	 * instance with null name could result in redundant initialization, though
	 * the final result would be correct.</p>
	 *
	 * @return the element's name, or null if no name facet is available
	 */
	@Override
	public String getName() {
		if (name == null) { name = super.getName(); }
		return name;
	}

}
