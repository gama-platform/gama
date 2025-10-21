/*******************************************************************************************************
 *
 * SyntacticFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation.ast;

import static gama.core.common.interfaces.IKeyword.CLASS;
import static gama.core.common.interfaces.IKeyword.EXPERIMENT;
import static gama.core.common.interfaces.IKeyword.GRID;
import static gama.core.common.interfaces.IKeyword.MODEL;
import static gama.core.common.interfaces.IKeyword.SKILL;
import static gama.core.common.interfaces.IKeyword.SPECIES;

import org.eclipse.emf.ecore.EObject;

import gama.gaml.compilation.ast.SyntacticModelElement.SyntacticExperimentModelElement;
import gama.gaml.statements.Facets;

/**
 * Class SyntacticFactory.
 *
 * @author drogoul
 * @since 9 sept. 2013
 *
 */
public class SyntacticFactory {

	/**
	 * The Constant SPECIES_VAR.
	 */
	public static final String SPECIES_VAR = "species_var";

	/**
	 * The Constant SYNTHETIC_MODEL.
	 */
	public static final String SYNTHETIC_MODEL = "synthetic_model";

	/**
	 * The Constant EXPERIMENT_MODEL.
	 */
	public static final String EXPERIMENT_MODEL = "experiment_model";

	/**
	 * Creates a new Syntactic object.
	 *
	 * @param statement
	 *            the statement
	 * @return the syntactic model element
	 */
	public static SyntacticModelElement createSyntheticModel(final EObject statement) {
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
	public static SyntacticExperimentModelElement createExperimentModel(final EObject root, final EObject expObject,
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
	public static ISyntacticElement create(final String keyword, final EObject statement, final boolean withChildren,
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
	public static ISyntacticElement create(final String keyword, final Facets facets, final boolean withChildren,
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
	public static ISyntacticElement create(final String keyword, final Facets facets, final EObject statement,
			final boolean withChildren, final Object... data) {
		return switch (keyword) {
			case MODEL -> data.length > 0 ? new SyntacticModelElement(keyword, facets, statement, (String) data[0])
					: new SyntacticModelElement(keyword, facets, statement, null);
			case EXPERIMENT -> new SyntacticExperimentElement(keyword, facets, statement);
			case SPECIES, GRID, SKILL -> new SyntacticSpeciesElement(keyword, facets, statement);
			case CLASS -> new SyntacticClassElement(keyword, facets, statement);
			default -> !withChildren ? new SyntacticSingleElement(keyword, facets, statement)
					: new SyntacticComposedElement(keyword, facets, statement);
		};
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
	public static ISyntacticElement createVar(final String keyword, final String name, final EObject stm) {
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