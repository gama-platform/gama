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

import static gama.api.constants.IKeyword.EXPERIMENT;
import static gama.api.constants.IKeyword.GRID;
import static gama.api.constants.IKeyword.MODEL;
import static gama.api.constants.IKeyword.SPECIES;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.ast.ISyntacticFactory;
import gama.api.gaml.symbols.Facets;

/**
 * The SyntacticFactory is a singleton factory responsible for creating syntactic elements
 * that represent different GAML language constructs in the compilation pipeline.
 * 
 * <p>This factory serves as the central point for instantiating various types of syntactic elements:</p>
 * <ul>
 *   <li><strong>Model Elements:</strong> Regular models and synthetic models for standalone blocks</li>
 *   <li><strong>Experiment Elements:</strong> Experiment models and individual experiment definitions</li>
 *   <li><strong>Species Elements:</strong> Species and grid definitions with specialized handling</li>
 *   <li><strong>Generic Elements:</strong> Single elements without children and composed elements with nested structures</li>
 *   <li><strong>Attribute Elements:</strong> Variable declarations and attribute definitions</li>
 * </ul>
 * 
 * <p><strong>Design Patterns:</strong></p>
 * <ul>
 *   <li><strong>Singleton:</strong> Ensures single instance across the compilation process</li>
 *   <li><strong>Factory Method:</strong> Provides specialized creation methods for different element types</li>
 *   <li><strong>Strategy:</strong> Different element types have specialized behavior implementations</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong> This singleton implementation is thread-safe using
 * lazy initialization. The factory methods are stateless and safe for concurrent access.</p>
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
	 * Private constructor to prevent direct instantiation.
	 * Use {@link #getInstance()} to obtain the singleton instance.
	 */
	private SyntacticFactory() {
		// Private constructor for singleton
	}

	/**
	 * Returns the singleton instance of SyntacticFactory using double-checked locking
	 * for thread-safe lazy initialization.
	 *
	 * @return the singleton SyntacticFactory instance
	 */
	public static SyntacticFactory getInstance() {
		if (instance == null) {
			synchronized (SyntacticFactory.class) {
				if (instance == null) {
					instance = new SyntacticFactory();
				}
			}
		}
		return instance;
	}

	/**
	 * Creates a synthetic model element from a standalone statement block.
	 * 
	 * <p>Synthetic models are used when processing standalone GAML blocks that are not
	 * part of a complete model file. This allows the compilation pipeline to treat
	 * such blocks as if they were contained within a proper model structure.</p>
	 *
	 * @param statement the EObject representing the standalone block to wrap in a synthetic model
	 * @return a SyntacticModelElement configured as a synthetic model containing the statement
	 * @throws IllegalArgumentException if the statement is null
	 */
	@Override
	public SyntacticModelElement createSyntheticModel(final EObject statement) {
		if (statement == null) {
			throw new IllegalArgumentException("Statement cannot be null");
		}
		return new SyntacticModelElement(SYNTHETIC_MODEL, null, statement, null);
	}

	/**
	 * Creates a new Syntactic object.
	 *
	 * @param root
	 *            the root
	 * @param expObject
	 *            the exp object
	 * @param path
	 *            the path
	 * @return the syntactic experiment model element
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
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param statement
	 *            the statement
	 * @param withChildren
	 *            the with children
	 * @param data
	 *            the data
	 * @return the i syntactic element
	 */
	@Override
	public ISyntacticElement create(final String keyword, final EObject statement, final boolean withChildren,
			final Object... data) {
		return create(keyword, null, statement, withChildren, data);
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param withChildren
	 *            the with children
	 * @param data
	 *            the data
	 * @return the i syntactic element
	 */
	@Override
	public ISyntacticElement create(final String keyword, final Facets facets, final boolean withChildren,
			final Object... data) {
		return create(keyword, facets, null, withChildren, data);
	}

	/**
	 * Creates the.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param statement
	 *            the statement
	 * @param withChildren
	 *            the with children
	 * @param data
	 *            the data
	 * @return the i syntactic element
	 */
	@Override
	public ISyntacticElement create(final String keyword, final Facets facets, final EObject statement,
			final boolean withChildren, final Object... data) {
		switch (keyword) {
			case MODEL:
				if (data.length > 0) return new SyntacticModelElement(keyword, facets, statement, (String) data[0]);
				return new SyntacticModelElement(keyword, facets, statement, null);
			case SPECIES:
			case GRID:
				return new SyntacticSpeciesElement(keyword, facets, statement);
			case EXPERIMENT:
				return new SyntacticExperimentElement(keyword, facets, statement);
			case null:
			default:
				break;
		}
		if (!withChildren) return new SyntacticSingleElement(keyword, facets, statement);
		return new SyntacticComposedElement(keyword, facets, statement);
	}

	/**
	 * Creates a new Syntactic object.
	 *
	 * @param keyword
	 *            the keyword
	 * @param name
	 *            the name
	 * @param stm
	 *            the stm
	 * @return the i syntactic element
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