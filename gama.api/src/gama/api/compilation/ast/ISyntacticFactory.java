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
 *
 */
public interface ISyntacticFactory {

	/**
	 * The Constant SPECIES_VAR.
	 */
	String SPECIES_VAR = "species_var";
	/**
	 * The Constant SYNTHETIC_MODEL.
	 */
	String SYNTHETIC_MODEL = "synthetic_model";
	/**
	 * The Constant EXPERIMENT_MODEL.
	 */
	String EXPERIMENT_MODEL = "experiment_model";

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
	ISyntacticElement createVar(final String keyword, final String name, final EObject stm);

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
	ISyntacticElement create(final String keyword, final Facets facets, final EObject statement,
			final boolean withChildren, final Object... data);

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
	ISyntacticElement create(final String keyword, final Facets facets, final boolean withChildren,
			final Object... data);

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
	ISyntacticElement create(final String keyword, final EObject statement, final boolean withChildren,
			final Object... data);

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
	ISyntacticElement createExperimentModel(final EObject root, final EObject expObject, final String path);

	/**
	 * Creates a new Syntactic object.
	 *
	 * @param statement
	 *            the statement
	 * @return the syntactic model element
	 */
	ISyntacticElement createSyntheticModel(final EObject statement);

}
