/*******************************************************************************************************
 *
 * IVariableDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import gama.api.additions.IGamaHelper;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;

/**
 *
 */
public interface IVariableDescription extends IDescription {

	/** The dependencies. */
	Map<String, Collection<String>> dependencies = new HashMap<>();

	/** The Constant INIT_DEPENDENCIES_FACETS. */
	Set<String> INIT_DEPENDENCIES_FACETS = ImmutableSet.<String> builder()
			.add(IKeyword.INIT, IKeyword.MIN, IKeyword.MAX, IKeyword.STEP, IKeyword.SIZE, IKeyword.AMONG).build();

	/** The Constant UPDATE_DEPENDENCIES_FACETS. */
	Set<String> UPDATE_DEPENDENCIES_FACETS =
			ImmutableSet.<String> builder().add(IKeyword.UPDATE, IKeyword.VALUE, IKeyword.MIN, IKeyword.MAX).build();

	/** The Constant FUNCTION_DEPENDENCIES_FACETS. */
	Set<String> FUNCTION_DEPENDENCIES_FACETS = ImmutableSet.<String> builder().add(IKeyword.FUNCTION).build();

	/** The Constant PREF_DEFINITIONS. */
	Map<String, String> PREF_DEFINITIONS = new HashMap<>();

	/**
	 * Checks if is updatable.
	 *
	 * @return true, if is updatable
	 */
	boolean isUpdatable();

	/**
	 * @return
	 */
	boolean isParameter();

	/**
	 * @return
	 */
	String getBuiltInDoc();

	/**
	 * @return
	 */
	boolean isFunction();

	/**
	 * Checks if is synthetic species container.
	 *
	 * @return true, if is synthetic species container
	 */
	boolean isSyntheticSpeciesContainer();

	/**
	 * @param initDependenciesFacets
	 * @param b
	 * @param c
	 * @return
	 */
	Collection<IVariableDescription> getDependencies(Set<String> initDependenciesFacets, boolean b, boolean c);

	/**
	 * @return
	 */
	boolean isNotModifiable();

	/**
	 * @return
	 */
	boolean isDefinedInExperiment();

	/**
	 * @return
	 */
	IGamlDocumentation getShortDocumentation();

	/**
	 * @param vd
	 */
	void copyFrom(IVariableDescription vd);

	/**
	 * @param get
	 * @param init
	 * @param set
	 */
	void addHelpers(IGamaHelper<?> get, IGamaHelper<?> init, IGamaHelper<?> set);

	/**
	 * Adds the helpers.
	 *
	 * @param clazz
	 *            the clazz
	 * @param get
	 *            the get
	 * @param init
	 *            the init
	 * @param set
	 *            the set
	 */
	void addHelpers(Class<?> clazz, IGamaHelper<?> get, IGamaHelper<?> init, IGamaHelper<?> set);

	/**
	 * @param definitionClass
	 */
	void setDefinitionClass(Class<?> definitionClass);

	/**
	 * @return
	 */
	IVariableDescription getBuiltInAncestor();

	/**
	 * @param asField
	 * @return
	 */
	IExpression getVarExpr(boolean asField);

	/**
	 * Copy.
	 *
	 * @param into
	 *            the into
	 * @return the i variable description
	 */
	@Override
	IVariableDescription copy(IDescription into);

	/**
	 * @return
	 */
	String getParameterName();

	/**
	 * @return
	 */
	IGamaHelper getGetter();

	/**
	 * Gets the setter.
	 *
	 * @return the setter
	 */
	IGamaHelper getSetter();

	/**
	 * Gets the initer.
	 *
	 * @return the initer
	 */
	IGamaHelper getIniter();

	/**
	 * @return
	 */
	boolean isExperimentParameter();

}
