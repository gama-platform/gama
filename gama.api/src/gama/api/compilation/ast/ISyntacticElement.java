/*******************************************************************************************************
 *
 * ISyntacticElement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.ast;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription.IFacetVisitor;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;
import gama.api.utils.interfaces.IDisposable;
import gama.api.utils.interfaces.INamed;

/**
 * Represents a syntactic element in the GAML Abstract Syntax Tree (AST).
 * 
 * <p>
 * This interface defines the core abstraction for all syntactic elements that form the AST of a GAML model.
 * Syntactic elements represent statements, symbols, and other language constructs as parsed from source code,
 * maintaining the hierarchical structure and preserving source location information.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * ISyntacticElement serves as the foundation for the AST-based representation of GAML models, providing:
 * </p>
 * <ul>
 *   <li><strong>Hierarchical Structure:</strong> Parent-child relationships between model elements</li>
 *   <li><strong>Facet Management:</strong> Storage and access to element properties (facets)</li>
 *   <li><strong>Source Linking:</strong> Connection to original EMF/XText parsed objects</li>
 *   <li><strong>Visitor Support:</strong> Traversal patterns for AST processing</li>
 *   <li><strong>Type Information:</strong> Keyword and category identification</li>
 * </ul>
 * 
 * <h2>Element Structure</h2>
 * 
 * <p>
 * Each syntactic element consists of:
 * </p>
 * <ul>
 *   <li><strong>Keyword:</strong> The GAML keyword (e.g., "species", "action", "if")</li>
 *   <li><strong>Name:</strong> Optional element name (typically from the "name" facet)</li>
 *   <li><strong>Facets:</strong> Key-value pairs representing element properties</li>
 *   <li><strong>Children:</strong> Nested syntactic elements forming the hierarchy</li>
 *   <li><strong>EObject:</strong> Reference to the original parsed EMF object</li>
 * </ul>
 * 
 * <h2>Element Categories</h2>
 * 
 * <p>
 * The interface provides filtering and visiting methods for common element categories:
 * </p>
 * <ul>
 *   <li><strong>Species:</strong> Agent type definitions (regular and grid)</li>
 *   <li><strong>Experiments:</strong> Experiment specifications</li>
 *   <li><strong>Grids:</strong> Grid-based species</li>
 *   <li><strong>Others:</strong> Statements, actions, variables, etc.</li>
 * </ul>
 * 
 * <h2>Visitor Pattern</h2>
 * 
 * <p>
 * The interface supports the Visitor pattern for AST traversal with multiple visitation strategies:
 * </p>
 * <ul>
 *   <li>{@link #visitThisAndAllChildrenRecursively(SyntacticVisitor)} - Full recursive traversal</li>
 *   <li>{@link #visitChildren(SyntacticVisitor)} - Direct children only (non-species/experiments)</li>
 *   <li>{@link #visitSpecies(SyntacticVisitor)} - Species elements only</li>
 *   <li>{@link #visitExperiments(SyntacticVisitor)} - Experiment elements only</li>
 *   <li>{@link #visitGrids(SyntacticVisitor)} - Grid elements only</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Creating and Manipulating Elements:</h3>
 * <pre>{@code
 * ISyntacticFactory factory = ...;
 * 
 * // Create a syntactic element
 * ISyntacticElement species = factory.create("species", facets, eObject, true);
 * species.setFacet("name", ConstantExpressionDescription.create("my_agent"));
 * 
 * // Add children
 * ISyntacticElement variable = factory.createVar("int", "energy", varEObject);
 * species.addChild(variable);
 * 
 * // Check properties
 * if (species.isSpecies()) {
 *     String name = species.getName(); // "my_agent"
 *     String keyword = species.getKeyword(); // "species"
 * }
 * }</pre>
 * 
 * <h3>Visiting the AST:</h3>
 * <pre>{@code
 * ISyntacticElement model = ...;
 * 
 * // Visit all species
 * model.visitSpecies(species -> {
 *     System.out.println("Found species: " + species.getName());
 * });
 * 
 * // Visit all elements recursively
 * model.visitThisAndAllChildrenRecursively(element -> {
 *     System.out.println(element.getKeyword() + " " + element.getName());
 * });
 * 
 * // Collect statistics
 * Map<String, Integer> stats = new HashMap<>();
 * model.computeStats(stats);
 * }</pre>
 * 
 * <h2>Facet Management</h2>
 * 
 * <p>
 * Facets are key-value pairs that represent element properties:
 * </p>
 * <pre>{@code
 * element.setFacet("size", ConstantExpressionDescription.create(100));
 * element.setFacet("color", VarExpressionDescription.create("red"));
 * 
 * if (element.hasFacet("size")) {
 *     IExpressionDescription sizeExpr = element.getExpressionAt("size");
 *     Facets facets = element.copyFacets(symbolPrototype);
 * }
 * }</pre>
 * 
 * <h2>Memory Management</h2>
 * 
 * <p>
 * Syntactic elements support memory optimization through:
 * </p>
 * <ul>
 *   <li>{@link #compact()} - Reduces memory footprint by compacting internal structures</li>
 *   <li>{@link #dispose()} - Releases resources when the element is no longer needed</li>
 *   <li>{@link #clear()} - Clears temporary data while preserving structure</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * Syntactic elements are generally not thread-safe and should only be accessed from a single thread
 * during construction and traversal. Once the AST is built, it is typically immutable during description
 * building and can be safely read by multiple threads.
 * </p>
 * 
 * @author drogoul
 * @since 9 sept. 2013
 * @version 2025-03
 * 
 * @see ISyntacticFactory
 * @see IGamlLabelProvider
 * @see gama.api.compilation.descriptions.IDescription
 * @see gama.api.gaml.expressions.IExpressionDescription
 */
public interface ISyntacticElement extends INamed, IDisposable {

	/**
	 * Visitor interface for traversing syntactic elements in the AST.
	 * 
	 * <p>
	 * Implementations of this interface can be passed to various visit methods to process
	 * syntactic elements during AST traversal. This follows the Visitor design pattern.
	 * </p>
	 * 
	 * <h3>Usage Example:</h3>
	 * <pre>{@code
	 * SyntacticVisitor visitor = element -> {
	 *     System.out.println("Visiting: " + element.getKeyword());
	 * };
	 * model.visitThisAndAllChildrenRecursively(visitor);
	 * }</pre>
	 */
	public interface SyntacticVisitor {

		/**
		 * Visits a syntactic element during AST traversal.
		 * 
		 * <p>
		 * This method is called for each element encountered during traversal.
		 * Implementations can inspect, modify, or collect information about the element.
		 * </p>
		 *
		 * @param element the syntactic element being visited (never null)
		 */
		void visit(ISyntacticElement element);
	}

	/**
	 * Predefined visitor that disposes all visited elements.
	 * 
	 * <p>
	 * This visitor calls {@link #dispose()} on each visited element, releasing resources
	 * and clearing references. Useful for cleaning up the entire AST when it's no longer needed.
	 * </p>
	 * 
	 * <h3>Usage:</h3>
	 * <pre>{@code
	 * model.visitThisAndAllChildrenRecursively(DISPOSE_VISITOR);
	 * }</pre>
	 */
	SyntacticVisitor DISPOSE_VISITOR = ISyntacticElement::dispose;

	/**
	 * Predicate filter for regular species elements (excluding grids).
	 * 
	 * <p>
	 * Matches elements that are species but not grids. Can be used with stream operations
	 * or custom traversal logic to filter only regular species.
	 * </p>
	 */
	Predicate<ISyntacticElement> SPECIES_FILTER = each -> each.isSpecies() && !IKeyword.GRID.equals(each.getKeyword());

	/**
	 * Predicate filter for grid species elements.
	 * 
	 * <p>
	 * Matches elements that are grid-based species (keyword = "grid").
	 * </p>
	 */
	Predicate<ISyntacticElement> GRID_FILTER = each -> IKeyword.GRID.equals(each.getKeyword());

	/**
	 * Predicate filter for experiment elements.
	 * 
	 * <p>
	 * Matches elements that are experiments.
	 * </p>
	 */
	Predicate<ISyntacticElement> EXPERIMENT_FILTER = ISyntacticElement::isExperiment;

	/**
	 * Predicate filter for elements that are neither experiments nor species.
	 * 
	 * <p>
	 * Matches regular statements, actions, variables, and other non-special elements.
	 * </p>
	 */
	Predicate<ISyntacticElement> OTHER_FILTER = each -> !each.isExperiment() && !each.isSpecies();

	/**
	 * Sets the keyword of this syntactic element.
	 * 
	 * <p>
	 * The keyword identifies the type of GAML construct this element represents
	 * (e.g., "species", "action", "if", "create"). This should typically be set
	 * during element construction and not changed afterwards.
	 * </p>
	 *
	 * @param name the GAML keyword for this element (e.g., "species", "action", "reflex")
	 */
	void setKeyword(final String name);

	/**
	 * Gets the keyword of this syntactic element.
	 * 
	 * <p>
	 * The keyword identifies the GAML construct type (e.g., "species", "action", "if").
	 * </p>
	 *
	 * @return the keyword of the element, or null if not set
	 */
	String getKeyword();

	/**
	 * Checks whether this element contains a facet with the given name.
	 * 
	 * <p>
	 * Facets are key-value pairs that represent element properties/parameters.
	 * Common facets include "name", "type", "init", "size", etc.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * if (element.hasFacet("name")) {
	 *     String name = element.getName();
	 * }
	 * }</pre>
	 *
	 * @param name the name of the facet to check for
	 * @return true if the element has a facet with this name, false otherwise
	 */
	boolean hasFacet(final String name);

	/**
	 * Returns the expression description of the facet with the given name.
	 * 
	 * <p>
	 * Facets store their values as {@link IExpressionDescription} objects, which
	 * can represent constants, variables, operators, or complex expressions.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * IExpressionDescription sizeExpr = element.getExpressionAt("size");
	 * if (sizeExpr != null && sizeExpr.isConst()) {
	 *     Object value = sizeExpr.getExpression().literalValue();
	 * }
	 * }</pre>
	 *
	 * @param name the name of the facet
	 * @return the expression description at this facet, or null if the facet doesn't exist
	 */
	IExpressionDescription getExpressionAt(final String name);

	/**
	 * Creates a copy of this element's facets for use in description building.
	 * 
	 * <p>
	 * This method creates a clean copy of the facets map, potentially applying transformations
	 * based on the symbol prototype. The copy is independent and modifications won't affect
	 * the original element.
	 * </p>
	 * 
	 * <p>
	 * The symbol prototype (if provided) may be used to:
	 * </p>
	 * <ul>
	 *   <li>Transform label facets into appropriate forms</li>
	 *   <li>Apply default values for missing facets</li>
	 *   <li>Validate facet types and values</li>
	 * </ul>
	 *
	 * @param sp the symbol prototype, may be null
	 * @return a new Facets instance containing a copy of this element's facets (never null)
	 */
	Facets copyFacets(IArtefact.Symbol sp);

	/**
	 * Sets or replaces a facet with the given name and expression.
	 * 
	 * <p>
	 * If a facet with this name already exists, it will be replaced. Otherwise, a new
	 * facet is added.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * element.setFacet("name", ConstantExpressionDescription.create("my_element"));
	 * element.setFacet("size", ConstantExpressionDescription.create(100));
	 * }</pre>
	 *
	 * @param name the name of the facet to set
	 * @param expr the expression description for the facet value
	 */
	void setFacet(final String name, final IExpressionDescription expr);

	/**
	 * Allows a visitor to iterate over all facets of this element.
	 * 
	 * <p>
	 * The visitor's {@code visit} method will be called once for each facet,
	 * receiving the facet name and expression description.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * element.visitFacets((name, expr) -> {
	 *     System.out.println(name + " = " + expr);
	 *     return true; // continue visiting
	 * });
	 * }</pre>
	 *
	 * @param visitor the facet visitor to apply (must not be null)
	 */
	void visitFacets(IFacetVisitor visitor);

	/**
	 * Returns the name of this syntactic element.
	 * 
	 * <p>
	 * The name is typically extracted from the "name" facet, if present. Not all
	 * elements have names (e.g., anonymous blocks, some statements).
	 * </p>
	 * 
	 * <h3>Examples:</h3>
	 * <ul>
	 *   <li>Species: "my_agent"</li>
	 *   <li>Action: "move"</li>
	 *   <li>Variable: "energy"</li>
	 *   <li>Anonymous statement: null</li>
	 * </ul>
	 *
	 * @return the name of the element, or null if unnamed
	 */
	@Override
	String getName();

	/**
	 * Returns the EMF/XText EObject that this syntactic element represents.
	 * 
	 * <p>
	 * This links the syntactic element back to the original parsed EMF model,
	 * which contains source location information and is used for error reporting.
	 * Synthetic elements (created programmatically rather than parsed) may return null.
	 * </p>
	 *
	 * @return the EObject element, or null if this is a synthetic element
	 */
	EObject getElement();

	/**
	 * Adds a child element to this syntactic element.
	 * 
	 * <p>
	 * This builds the hierarchical AST structure. For example, a species element
	 * would have action, variable, and reflex elements as children.
	 * </p>
	 * 
	 * <p>
	 * Not all elements support children. Adding a child to an element that doesn't
	 * support them may have no effect or throw an exception depending on implementation.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * ISyntacticElement species = factory.create("species", ...);
	 * ISyntacticElement action = factory.create("action", ...);
	 * species.addChild(action); // action is now a child of species
	 * }</pre>
	 *
	 * @param e the child syntactic element to add (must not be null)
	 */
	void addChild(final ISyntacticElement e);

	/**
	 * Checks if this element represents a species.
	 * 
	 * <p>
	 * Returns true for both regular species and grid species. Use the keyword
	 * to distinguish between them if needed.
	 * </p>
	 *
	 * @return true if this element is a species (regular or grid), false otherwise
	 */
	boolean isSpecies();

	/**
	 * Checks if this element represents an experiment.
	 * 
	 * <p>
	 * Experiments are special top-level elements that define simulation execution contexts.
	 * </p>
	 *
	 * @return true if this element is an experiment, false otherwise
	 */
	boolean isExperiment();

	/**
	 * Checks if this element has any facets.
	 * 
	 * <p>
	 * This is more efficient than checking if the facet count is zero, as it may
	 * avoid creating collections.
	 * </p>
	 *
	 * @return true if the element has at least one facet, false otherwise
	 */
	boolean hasFacets();

	/**
	 * Computes simple frequency statistics for element types in this subtree.
	 * 
	 * <p>
	 * This method recursively traverses the element and its children, counting occurrences
	 * of each element type (by keyword or class name). The results are accumulated in the
	 * provided map.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * Map<String, Integer> stats = new HashMap<>();
	 * model.computeStats(stats);
	 * // stats might contain: {"species": 3, "action": 12, "reflex": 5, ...}
	 * }</pre>
	 *
	 * @param stats a map to be filled with element type frequencies (must not be null)
	 */
	void computeStats(Map<String, Integer> stats);

	/**
	 * Allows a visitor to visit this element and all its children recursively.
	 * 
	 * <p>
	 * This performs a depth-first traversal of the entire subtree rooted at this element,
	 * visiting every element exactly once. The visitor is called for this element first,
	 * then recursively for all children.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * model.visitThisAndAllChildrenRecursively(element -> {
	 *     System.out.println("Visiting: " + element.getKeyword() + " " + element.getName());
	 * });
	 * }</pre>
	 *
	 * @param visitor the visitor to apply to each element (must not be null)
	 */
	void visitThisAndAllChildrenRecursively(SyntacticVisitor visitor);

	/**
	 * Allows a visitor to visit the direct children that are neither species, grids, nor experiments.
	 * 
	 * <p>
	 * This visits only the "regular" children like actions, variables, reflexes, and statements,
	 * skipping species and experiments which are typically handled separately.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * species.visitChildren(child -> {
	 *     // Visits actions, variables, reflexes, etc. but not nested species
	 *     System.out.println("Child: " + child.getKeyword());
	 * });
	 * }</pre>
	 *
	 * @param visitor the visitor to apply (must not be null)
	 */
	void visitChildren(final SyntacticVisitor visitor);

	/**
	 * Allows a visitor to visit only the species elements (either this element or its children).
	 * 
	 * <p>
	 * If this element is a species, the visitor is called on it. Otherwise, the method
	 * recursively searches for species in the children and calls the visitor on each found.
	 * This includes both regular species and grid species.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * model.visitSpecies(species -> {
	 *     System.out.println("Species: " + species.getName());
	 * });
	 * }</pre>
	 *
	 * @param visitor the visitor to apply to species elements (must not be null)
	 */
	void visitSpecies(final SyntacticVisitor visitor);

	/**
	 * Allows a visitor to visit only the experiment elements (either this element or its children).
	 * 
	 * <p>
	 * If this element is an experiment, the visitor is called on it. Otherwise, the method
	 * recursively searches for experiments in the children and calls the visitor on each found.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * model.visitExperiments(experiment -> {
	 *     System.out.println("Experiment: " + experiment.getName());
	 * });
	 * }</pre>
	 *
	 * @param visitor the visitor to apply to experiment elements (must not be null)
	 */
	void visitExperiments(final SyntacticVisitor visitor);

	/**
	 * Allows a visitor to visit only the grid species elements (either this element or its children).
	 * 
	 * <p>
	 * Grids are special species with keyword "grid". This method finds and visits only those
	 * elements.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * model.visitGrids(grid -> {
	 *     System.out.println("Grid: " + grid.getName());
	 * });
	 * }</pre>
	 *
	 * @param visitor the visitor to apply to grid elements (must not be null)
	 */
	void visitGrids(final SyntacticVisitor visitor);

	/**
	 * Compacts this element to reduce memory footprint.
	 * 
	 * <p>
	 * This method performs two optimizations:
	 * </p>
	 * <ol>
	 *   <li>Sets facets to null if they are empty</li>
	 *   <li>Compacts internal collections to use minimal memory</li>
	 * </ol>
	 * 
	 * <p>
	 * This should be called after the element is fully constructed and won't be modified further.
	 * Particularly useful for large ASTs to reduce memory consumption.
	 * </p>
	 */
	void compact();

	/**
	 * Checks whether this element has any children.
	 * 
	 * <p>
	 * This is more efficient than getting the children list and checking its size,
	 * as it may avoid creating collections.
	 * </p>
	 *
	 * @return true if this element has at least one child, false otherwise
	 */
	boolean hasChildren();

	/**
	 * Allows a visitor to visit all children of this element.
	 * 
	 * <p>
	 * Unlike {@link #visitChildren(SyntacticVisitor)}, this method visits ALL children
	 * including species, grids, and experiments. It does not visit this element itself.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * element.visitAllChildren(child -> {
	 *     System.out.println("Direct child: " + child.getKeyword());
	 * });
	 * }</pre>
	 *
	 * @param syntacticVisitor the visitor to apply to all children (must not be null)
	 */
	void visitAllChildren(SyntacticVisitor syntacticVisitor);

	/**
	 * Returns the pragmas associated with this element.
	 * 
	 * <p>
	 * Pragmas are compiler directives or metadata annotations that can be attached to
	 * GAML elements. They typically start with '@' in the source code and provide
	 * additional compilation hints or documentation.
	 * </p>
	 * 
	 * <p>
	 * The map keys are pragma names, and the values are lists of pragma arguments/values.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * Map<String, List<String>> pragmas = element.getPragmas();
	 * if (pragmas != null && pragmas.containsKey("no_warning")) {
	 *     // Suppress warnings for this element
	 * }
	 * }</pre>
	 *
	 * @return a map of pragma names to their values, or null if no pragmas are present
	 */
	default Map<String, List<String>> getPragmas() { return null; }

	/**
	 * Clears temporary data while preserving the element structure.
	 * 
	 * <p>
	 * This method releases temporary resources or caches used during compilation
	 * without disposing the entire element. Unlike {@link #dispose()}, the element
	 * remains valid and usable after calling this method.
	 * </p>
	 * 
	 * <p>
	 * Default implementation does nothing. Override to provide cleanup logic.
	 * </p>
	 */
	default void clear() {}

	/**
	 * Returns the source path of this element.
	 * 
	 * <p>
	 * The path typically represents the location of this element in the source file
	 * or the hierarchical path within the model structure. This is used for error
	 * reporting and debugging.
	 * </p>
	 * 
	 * <h3>Example paths:</h3>
	 * <ul>
	 *   <li>"model/species[my_agent]/action[move]"</li>
	 *   <li>"experiment/output/display[main]"</li>
	 * </ul>
	 *
	 * @return the path string, or null if not available
	 */
	String getPath();

}