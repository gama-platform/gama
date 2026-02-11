/*******************************************************************************************************
 *
 * ITypesManager.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.util.Set;

import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.kernel.agent.IAgent;
import gama.api.utils.IDisposable;

/**
 * The Interface ITypesManager.
 */
public interface ITypesManager extends IDisposable {

	/**
	 * Alias.
	 *
	 * @param existingTypeName
	 *            the existing type name
	 * @param otherTypeName
	 *            the other type name
	 */
	void alias(String existingTypeName, String otherTypeName);

	/**
	 * Contains type.
	 *
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	boolean containsType(String s);

	/**
	 * Gets the type with this name. Never returns null (return NO_TYPE in case the Type doesnt exist
	 *
	 * @param type
	 *            the type
	 * @return the i type
	 */
	IType<?> get(final String type);

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param type
	 *            the type
	 * @param defaultValue
	 *            the default value
	 * @return the i type
	 * @date 12 janv. 2024
	 */
	IType<?> get(String type, IType<?> defaultValue);

	/**
	 * Adds the species type.
	 *
	 * @param species
	 *            the species
	 * @return the i type<? extends I agent>
	 */
	IType<? extends IAgent> addSpeciesType(ISpeciesDescription species);

	/**
	 * Inits the.
	 *
	 * @param model
	 *            the model
	 */
	void init(IModelDescription model);

	/**
	 * Sets the parent.
	 *
	 * @param typesManager
	 *            the new parent
	 */
	void setParent(ITypesManager typesManager);

	/**
	 * Inits the type.
	 *
	 * @param <Support>
	 *            the generic type
	 * @param keyword
	 *            the keyword
	 * @param typeInstance
	 *            the type instance
	 * @param id
	 *            the id
	 * @param varKind
	 *            the var kind
	 * @param support
	 *            the support
	 * @param pluginName
	 *            the plugin name
	 * @return the i type
	 */
	<Support> IType<Support> addRegularType(String name, IType<Support> typeInstance, String pluginName);

	/**
	 * Gets the all types.
	 *
	 * @return the all types
	 */
	Set<IType<?>> getAllTypes();

	/**
	 * Decode type. Expects either a simple type name, or a parametric one with 1 or 2 type parameters
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param type
	 *            the type
	 * @return the i type
	 * @date 4 nov. 2023
	 */
	IType decodeType(String type);

	/**
	 * Checks if one type is assignable from another, using cache when possible. This is the cached version of
	 * {@link IType#isAssignableFrom(IType)}.
	 *
	 * @param from
	 *            the type to check (target type)
	 * @param to
	 *            the type being assigned (source type)
	 * @return true if 'from' is assignable from 'to'
	 */
	boolean checkAssignability(IType<?> from, IType<?> to);

	/**
	 * Finds the common supertype between two types, using cache when possible. This is the cached version of
	 * {@link IType#findCommonSupertypeWith(IType)}.
	 *
	 * @param type1
	 *            the first type
	 * @param type2
	 *            the second type
	 * @return the common supertype, or Types.NO_TYPE if none found
	 */
	IType<?> computeCommonSupertype(IType<?> type1, IType<?> type2);

	/**
	 * Computes the distance between two types in the type hierarchy, using cache when possible. This is the cached
	 * version of {@link IType#distanceTo(IType)}.
	 *
	 * @param from
	 *            the starting type
	 * @param to
	 *            the target type
	 * @return the distance (number of steps in hierarchy), or Integer.MAX_VALUE if unreachable
	 */
	int computeDistance(IType<?> from, IType<?> to);

	/**
	 * Checks if one type is translatable into another, using cache when possible. This is the cached version of
	 * {@link IType#isTranslatableInto(IType)}.
	 *
	 * @param from
	 *            the source type
	 * @param to
	 *            the target type
	 * @return true if 'from' is translatable into 'to'
	 */
	boolean checkTranslatability(IType<?> from, IType<?> to);

}