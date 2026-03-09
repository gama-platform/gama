/*******************************************************************************************************
 *
 * SyntacticFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.ast;

import static gama.annotations.constants.IKeyword.CLASS;
import static gama.annotations.constants.IKeyword.EXPERIMENT;
import static gama.annotations.constants.IKeyword.GRID;
import static gama.annotations.constants.IKeyword.MODEL;
import static gama.annotations.constants.IKeyword.SPECIES;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.ast.ISyntacticFactory;
import gama.api.gaml.symbols.Facets;

/**
 * The SyntacticFactory is a singleton factory responsible for creating syntactic elements that represent different GAML
 * language constructs in the compilation pipeline.
 *
 * <p>
 * This factory serves as the central point for instantiating various types of syntactic elements:
 * </p>
 * <ul>
 * <li><strong>Model Elements:</strong> Regular models and synthetic models for standalone blocks</li>
 * <li><strong>Experiment Elements:</strong> Experiment models and individual experiment definitions</li>
 * <li><strong>Species Elements:</strong> Species and grid definitions with specialized handling</li>
 * <li><strong>Generic Elements:</strong> Single elements without children and composed elements with nested
 * structures</li>
 * <li><strong>Attribute Elements:</strong> Variable declarations and attribute definitions</li>
 * </ul>
 *
 * <p>
 * <strong>Design Patterns:</strong>
 * </p>
 * <ul>
 * <li><strong>Singleton:</strong> Ensures single instance across the compilation process</li>
 * <li><strong>Factory Method:</strong> Provides specialized creation methods for different element types</li>
 * <li><strong>Strategy:</strong> Different element types have specialized behavior implementations</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong> This singleton implementation is thread-safe using lazy initialization. The factory
 * methods are stateless and safe for concurrent access.
 * </p>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see ISyntacticElement
 * @see ISyntacticFactory
 */
public class SyntacticFactory implements ISyntacticFactory {

	/** The singleton instance, initialized lazily in a thread-safe manner. */
	private static volatile SyntacticFactory instance;

	/**
	 * Private constructor to prevent direct instantiation. Use {@link #getInstance()} to obtain the singleton instance.
	 */
	private SyntacticFactory() {
		// Private constructor for singleton
	}

	/**
	 * Returns the singleton instance of SyntacticFactory using double-checked locking for thread-safe lazy
	 * initialization.
	 *
	 * @return the singleton SyntacticFactory instance
	 */
	public static SyntacticFactory getInstance() {
		if (instance == null) {
			synchronized (SyntacticFactory.class) {
				if (instance == null) { instance = new SyntacticFactory(); }
			}
		}
		return instance;
	}

	/**
	 * Creates a synthetic model element from a standalone statement block.
	 *
	 * <p>
	 * Synthetic models are used when processing standalone GAML blocks that are not part of a complete model file. This
	 * allows the compilation pipeline to treat such blocks as if they were contained within a proper model structure.
	 * </p>
	 *
	 * @param statement
	 *            the EObject representing the standalone block to wrap in a synthetic model
	 * @return a SyntacticModelElement configured as a synthetic model containing the statement
	 * @throws IllegalArgumentException
	 *             if the statement is null
	 */
	@Override
	public SyntacticModelElement createSyntheticModel(final EObject statement) {
		if (statement == null) throw new IllegalArgumentException("Statement cannot be null");
		return new SyntacticModelElement(SYNTHETIC_MODEL, null, statement, null);
	}

	/**
	 * Creates a synthetic model element wrapping a standalone experiment.
	 *
	 * <p>
	 * This factory method creates a {@link SyntacticExperimentModelElement} which is a specialized model containing
	 * only an experiment. This is used for:
	 * </p>
	 * <ul>
	 * <li>Headless execution of experiments</li>
	 * <li>Remote server-side experiment execution</li>
	 * <li>Unit testing individual experiments</li>
	 * <li>Batch processing scenarios</li>
	 * </ul>
	 *
	 * <p>
	 * The created structure is:
	 * </p>
	 *
	 * <pre>
	 * SyntacticExperimentModelElement (EXPERIMENT_MODEL)
	 *   └── SyntacticExperimentElement (experiment)
	 * </pre>
	 *
	 * @param root
	 *            the root EObject representing the experiment model structure
	 * @param expObject
	 *            the EObject specifically representing the experiment definition
	 * @param path
	 *            the file path for error reporting and resource location, may be null
	 * @return a new SyntacticExperimentModelElement containing the experiment
	 * @see SyntacticExperimentModelElement
	 */
	@Override
	public SyntacticExperimentModelElement createExperimentModel(final EObject root, final EObject expObject,
			final String path) {
		final SyntacticExperimentModelElement model = new SyntacticExperimentModelElement(EXPERIMENT_MODEL, root, path);
		final SyntacticExperimentElement exp = new SyntacticExperimentElement("experiment", null, expObject);
		model.addChild(exp);
		return model;
	}

	/**
	 * Creates a syntactic element with the specified keyword and statement.
	 *
	 * <p>
	 * This convenience method delegates to the full {@link #create} method with null facets.
	 * </p>
	 *
	 * @param keyword
	 *            the GAML keyword identifying the element type
	 * @param statement
	 *            the underlying EMF EObject from the parsed model
	 * @param withChildren
	 *            whether the element should support children (composed vs single)
	 * @param data
	 *            optional additional data, interpretation depends on keyword
	 * @return the created syntactic element of the appropriate type
	 */
	@Override
	public ISyntacticElement create(final String keyword, final EObject statement, final boolean withChildren,
			final Object... data) {
		return create(keyword, null, statement, withChildren, data);
	}

	/**
	 * Creates a syntactic element with the specified keyword and facets.
	 *
	 * <p>
	 * This convenience method delegates to the full {@link #create} method with null statement.
	 * </p>
	 *
	 * @param keyword
	 *            the GAML keyword identifying the element type
	 * @param facets
	 *            the initial facets (attributes) for this element
	 * @param withChildren
	 *            whether the element should support children (composed vs single)
	 * @param data
	 *            optional additional data, interpretation depends on keyword
	 * @return the created syntactic element of the appropriate type
	 */
	@Override
	public ISyntacticElement create(final String keyword, final Facets facets, final boolean withChildren,
			final Object... data) {
		return create(keyword, facets, null, withChildren, data);
	}

	/**
	 * Creates a syntactic element with full control over all construction parameters.
	 *
	 * <p>
	 * This is the primary factory method that dispatches to specialized constructors based on the keyword. It follows
	 * the Factory Method pattern to encapsulate instantiation logic.
	 * </p>
	 *
	 * <p>
	 * <strong>Keyword-Specific Behavior:</strong>
	 * </p>
	 * <ul>
	 * <li><strong>MODEL:</strong> Creates {@link SyntacticModelElement}, data[0] is the file path</li>
	 * <li><strong>SPECIES/GRID:</strong> Creates {@link SyntacticSpeciesElement}</li>
	 * <li><strong>EXPERIMENT:</strong> Creates {@link SyntacticExperimentElement}</li>
	 * <li><strong>Other keywords:</strong> Creates {@link SyntacticSingleElement} or {@link SyntacticComposedElement}
	 * based on withChildren flag</li>
	 * </ul>
	 *
	 * <p>
	 * <strong>Thread Safety:</strong> This method is stateless and safe for concurrent access.
	 * </p>
	 *
	 * @param keyword
	 *            the GAML keyword identifying the element type (e.g., "species", "action")
	 * @param facets
	 *            the initial facets (attributes) for this element, or null
	 * @param statement
	 *            the underlying EMF EObject from the parsed model, or null for synthetic elements
	 * @param withChildren
	 *            whether the element should support children (true for composed, false for single)
	 * @param data
	 *            optional keyword-specific data (e.g., file path for MODEL)
	 * @return the created syntactic element of the appropriate specialized type
	 */
	@Override
	public ISyntacticElement create(final String keyword, final Facets facets, final EObject statement,
			final boolean withChildren, final Object... data) {
		return switch (keyword) {
			case MODEL -> data.length > 0 ? new SyntacticModelElement(keyword, facets, statement, (String) data[0])
					: new SyntacticModelElement(keyword, facets, statement, null);
			case SPECIES, GRID -> new SyntacticSpeciesElement(keyword, facets, statement);
			case EXPERIMENT -> new SyntacticExperimentElement(keyword, facets, statement);
			case CLASS -> new SyntacticClassElement(keyword, facets, statement);
			default -> !withChildren ? new SyntacticSingleElement(keyword, facets, statement)
					: new SyntacticComposedElement(keyword, facets, statement);

		};
	}

	/**
	 * Creates a syntactic attribute element representing a variable or field declaration.
	 *
	 * <p>
	 * Attributes are specialized single elements that cache their name for performance. They represent variable
	 * declarations, parameter definitions, and other named attributes within structural elements.
	 * </p>
	 *
	 * <p>
	 * <strong>Examples:</strong>
	 * </p>
	 * <ul>
	 * <li>{@code int age <- 0;} - keyword="int", name="age"</li>
	 * <li>{@code parameter "Speed" var: speed;} - keyword="parameter", name="speed"</li>
	 * </ul>
	 *
	 * @param keyword
	 *            the GAML keyword identifying the attribute type (e.g., "int", "float", "var")
	 * @param name
	 *            the name of the attribute, should not be null
	 * @param stm
	 *            the underlying EMF EObject from the parsed model
	 * @return a new {@link SyntacticAttributeElement} with the specified properties
	 */
	@Override
	public ISyntacticElement createVar(final String keyword, final String name, final EObject stm) {
		return new SyntacticAttributeElement(keyword, name, stm);
	}
}

// TODO for content assist
// Build a scope accessible by EObjects that contain variables and actions names
// in the syntactic structure
// A global scope can also be built for built-in elements (and attached to the
// local scopes if we can detect things like
// skills, etc.)
// The scope could be attached to resources (like the syntactic elements) and
// become accessible from content assist to
// return possible candidates