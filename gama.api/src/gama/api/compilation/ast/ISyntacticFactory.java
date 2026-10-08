/*******************************************************************************************************
 *
 * ISyntacticFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.ast;

import org.eclipse.emf.ecore.EObject;

import gama.api.gaml.symbols.Facets;

/**
 * Factory interface for creating syntactic elements in the GAML Abstract Syntax Tree (AST).
 * 
 * <p>
 * This interface provides factory methods for constructing {@link ISyntacticElement} instances
 * that form the AST representation of GAML models. It serves as the central creation point for
 * all syntactic elements during parsing and model transformation.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * The factory encapsulates the creation logic for syntactic elements, providing:
 * </p>
 * <ul>
 *   <li><strong>Uniform Creation:</strong> Consistent API for creating different element types</li>
 *   <li><strong>Flexibility:</strong> Multiple overloaded methods for various creation scenarios</li>
 *   <li><strong>EMF Integration:</strong> Link syntactic elements to EMF/XText parsed objects</li>
 *   <li><strong>Synthetic Elements:</strong> Create elements programmatically without source code</li>
 * </ul>
 * 
 * <h2>Element Types</h2>
 * 
 * <p>
 * The factory can create various types of syntactic elements:
 * </p>
 * <ul>
 *   <li><strong>Regular Elements:</strong> Statements, actions, species, experiments</li>
 *   <li><strong>Variable Elements:</strong> Attribute declarations with type and initialization</li>
 *   <li><strong>Model Elements:</strong> Top-level model containers</li>
 *   <li><strong>Synthetic Elements:</strong> Programmatically generated elements</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Creating Elements with Facets:</h3>
 * <pre>{@code
 * ISyntacticFactory factory = ...;
 * Facets facets = new Facets("name", "my_agent", "parent", "base_agent");
 * 
 * // Create with EObject and facets
 * ISyntacticElement species = factory.create("species", facets, eObject, true);
 * 
 * // Create with facets only (synthetic)
 * ISyntacticElement action = factory.create("action", facets, true);
 * }</pre>
 * 
 * <h3>Creating Variable Elements:</h3>
 * <pre>{@code
 * // Create a variable declaration
 * ISyntacticElement var = factory.createVar("int", "energy", varEObject);
 * // Represents: int energy;
 * }</pre>
 * 
 * <h3>Creating Model Containers:</h3>
 * <pre>{@code
 * // Create a synthetic model
 * ISyntacticElement model = factory.createSyntheticModel(statement);
 * 
 * // Create an experiment model
 * ISyntacticElement expModel = factory.createExperimentModel(root, expObject, "path/to/model.gaml");
 * }</pre>
 * 
 * <h2>Method Variants</h2>
 * 
 * <p>
 * The factory provides multiple overloaded {@code create} methods with different parameter combinations:
 * </p>
 * <ul>
 *   <li><strong>With EObject:</strong> Links to parsed source for error reporting</li>
 *   <li><strong>Without EObject:</strong> Creates synthetic elements</li>
 *   <li><strong>With Facets:</strong> Provides element properties/parameters</li>
 *   <li><strong>With Children Flag:</strong> Controls whether children should be processed</li>
 *   <li><strong>With Data:</strong> Passes additional creation context</li>
 * </ul>
 * 
 * <h2>Special Constants</h2>
 * 
 * <p>
 * The interface defines constants for special element types:
 * </p>
 * <ul>
 *   <li>{@link #SPECIES_VAR} - Marker for species variable elements</li>
 *   <li>{@link #SYNTHETIC_MODEL} - Marker for programmatically created models</li>
 *   <li>{@link #EXPERIMENT_MODEL} - Marker for experiment model containers</li>
 * </ul>
 * 
 * <h2>Implementation Notes</h2>
 * 
 * <p>
 * Implementations should:
 * </p>
 * <ul>
 *   <li>Create elements with proper keyword and name initialization</li>
 *   <li>Handle null parameters gracefully (especially for synthetic elements)</li>
 *   <li>Process children according to the {@code withChildren} flag</li>
 *   <li>Maintain references to EObjects for source location tracking</li>
 *   <li>Support variable-length {@code data} parameters for extensibility</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * Factory implementations are typically not thread-safe. Element creation should be performed
 * sequentially during parsing and model building phases.
 * </p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see ISyntacticElement
 * @see gama.api.gaml.symbols.Facets
 * @see org.eclipse.emf.ecore.EObject
 */
public interface ISyntacticFactory {

	/**
	 * Constant marker for species variable elements.
	 * 
	 * <p>
	 * Used to identify syntactic elements that represent variable declarations within species.
	 * This can be used as a keyword or marker in element creation and processing.
	 * </p>
	 */
	String SPECIES_VAR = "species_var";
	
	/**
	 * Constant marker for synthetic model elements.
	 * 
	 * <p>
	 * Identifies model elements that are created programmatically rather than parsed from
	 * source code. These synthetic models are used for internal processing and transformations.
	 * </p>
	 */
	String SYNTHETIC_MODEL = "synthetic_model";
	
	/**
	 * Constant marker for experiment model containers.
	 * 
	 * <p>
	 * Identifies special model containers that wrap experiment definitions. These are created
	 * to provide a model context for experiment-level elements.
	 * </p>
	 */
	String EXPERIMENT_MODEL = "experiment_model";

	/**
	 * Creates a syntactic element representing a variable declaration.
	 * 
	 * <p>
	 * This factory method creates elements for variable/attribute declarations with a type,
	 * name, and optional source location. These are typically used for species attributes,
	 * global variables, and action parameters.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Create: int energy <- 100;
	 * ISyntacticElement var = factory.createVar("int", "energy", eObject);
	 * }</pre>
	 *
	 * @param keyword the variable type keyword (e.g., "int", "float", "string", "species_name")
	 * @param name the variable name
	 * @param stm the EMF statement object for source location, may be null for synthetic variables
	 * @return a new syntactic element representing the variable declaration
	 */
	ISyntacticElement createVar(final String keyword, final String name, final EObject stm);

	/**
	 * Creates a syntactic element with full specification.
	 * 
	 * <p>
	 * This is the most complete factory method, providing all options for element creation
	 * including keyword, facets, source location, children processing, and additional data.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * Facets facets = new Facets("name", "my_agent", "skills", "[moving]");
	 * ISyntacticElement species = factory.create("species", facets, eObject, true);
	 * }</pre>
	 *
	 * @param keyword the GAML keyword (e.g., "species", "action", "reflex")
	 * @param facets the facets (properties/parameters) for this element, may be null
	 * @param statement the EMF statement object for source location tracking
	 * @param withChildren if true, process and add children from the statement; if false, create childless element
	 * @param data optional additional creation context or parameters (variable length)
	 * @return a new syntactic element
	 */
	ISyntacticElement create(final String keyword, final Facets facets, final EObject statement,
			final boolean withChildren, final Object... data);

	/**
	 * Creates a synthetic syntactic element without source location.
	 * 
	 * <p>
	 * This method creates elements that are not directly parsed from source code but are
	 * generated programmatically during model transformation or augmentation. The resulting
	 * element will have no associated EObject.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * Facets facets = new Facets("name", "generated_action");
	 * ISyntacticElement action = factory.create("action", facets, false);
	 * }</pre>
	 *
	 * @param keyword the GAML keyword (e.g., "species", "action", "reflex")
	 * @param facets the facets (properties/parameters) for this element, may be null
	 * @param withChildren if true, process children (though typically false for synthetic elements)
	 * @param data optional additional creation context or parameters (variable length)
	 * @return a new synthetic syntactic element without source location
	 */
	ISyntacticElement create(final String keyword, final Facets facets, final boolean withChildren,
			final Object... data);

	/**
	 * Creates a syntactic element with source location but without explicit facets.
	 * 
	 * <p>
	 * This method creates elements linked to parsed EMF objects but without pre-constructed facets.
	 * Facets may be extracted from the statement object during element construction depending on
	 * the implementation.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * // Create an element from a parsed statement, extracting facets automatically
	 * ISyntacticElement element = factory.create("if", eObject, true);
	 * }</pre>
	 *
	 * @param keyword the GAML keyword (e.g., "species", "action", "reflex")
	 * @param statement the EMF statement object for source location tracking
	 * @param withChildren if true, process and add children from the statement
	 * @param data optional additional creation context or parameters (variable length)
	 * @return a new syntactic element
	 */
	ISyntacticElement create(final String keyword, final EObject statement, final boolean withChildren,
			final Object... data);

	/**
	 * Creates an experiment model container element.
	 * 
	 * <p>
	 * This method creates a special model-level container for an experiment definition.
	 * Experiment models provide a complete model context that wraps the experiment,
	 * allowing it to reference and import elements from the main model.
	 * </p>
	 * 
	 * <p>
	 * The experiment model links the experiment to its base model and provides access
	 * to species, globals, and other model-level definitions that the experiment needs.
	 * </p>
	 * 
	 * <h3>Usage Context:</h3>
	 * <p>
	 * This is typically used during compilation when processing experiment definitions that
	 * need to access their parent model's structure and definitions.
	 * </p>
	 *
	 * @param root the root EMF object of the base model
	 * @param expObject the EMF object representing the experiment definition
	 * @param path the file system path to the model file
	 * @return a new experiment model syntactic element
	 */
	ISyntacticElement createExperimentModel(final EObject root, final EObject expObject, final String path);

	/**
	 * Creates a synthetic model container element.
	 * 
	 * <p>
	 * This method creates a programmatically generated model container that is not directly
	 * parsed from source code. Synthetic models are used for:
	 * </p>
	 * <ul>
	 *   <li>Internal model transformations and augmentations</li>
	 *   <li>Temporary model containers during compilation</li>
	 *   <li>Testing and validation scenarios</li>
	 *   <li>Generated code integration</li>
	 * </ul>
	 * 
	 * <p>
	 * Unlike regular models parsed from .gaml files, synthetic models are created entirely
	 * in memory and may not have corresponding source files.
	 * </p>
	 * 
	 * <h3>Example Use Cases:</h3>
	 * <ul>
	 *   <li>Creating wrapper models for code generation</li>
	 *   <li>Building temporary models for partial compilation</li>
	 *   <li>Constructing test models programmatically</li>
	 * </ul>
	 *
	 * @param statement the EMF statement that serves as the basis for the model, may be null
	 * @return a new synthetic model syntactic element
	 */
	ISyntacticElement createSyntheticModel(final EObject statement);

}
