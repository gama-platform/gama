/*******************************************************************************************************
 *
 * IContainerType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IRuntimeContainer;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * Interface for GAML container types.
 * <p>
 * Container types represent collections of values with keys/indices. This interface extends {@link IType} with
 * additional methods specific to containers, including parameterization by key and content types. Container types are
 * used for lists, maps, matrices, graphs, and other collection-based data structures.
 * </p>
 * <p>
 * Container types support:
 * <ul>
 * <li>Parameterization by key type and content type (e.g., {@code list<int>}, {@code map<string,float>})</li>
 * <li>Type-safe casting with type parameters</li>
 * <li>JSON serialization including type parameters</li>
 * </ul>
 * </p>
 *
 * @param <T>
 *            the specific container class this type represents
 *
 * @author drogoul
 * @since GAMA 1.0
 * @see IType
 * @see IContainer
 * @see IRuntimeContainer
 * @see GamaContainerType
 */
public interface IContainerType<T extends IRuntimeContainer<?, ?>> extends IType<T> {

	/**
	 * Gets the GAML type representation of this container type.
	 *
	 * @return this container type
	 */
	@Override
	IContainerType<T> getGamlType();

	/**
	 * Determines the container type resulting from casting an expression to this type.
	 * <p>
	 * This method analyzes the expression's type to determine the appropriate parameterized container type after
	 * casting.
	 * </p>
	 *
	 * @param exp
	 *            the expression to analyze
	 * @return the resulting container type after casting
	 */
	@Override
	IContainerType<?> typeIfCasting(final IExpression exp);

	/**
	 * Casts an object to this container type.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to cast
	 * @param param
	 *            optional casting parameter
	 * @param copy
	 *            whether to copy the result
	 * @return the casted container
	 */
	@Override
	T cast(IScope scope, Object obj, Object param, boolean copy);

	/**
	 * Casts an object to this container type with specified key and content types.
	 * <p>
	 * This method allows for type-safe casting where the key and content types of the resulting container can be
	 * specified explicitly.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to cast
	 * @param param
	 *            optional casting parameter
	 * @param keyType
	 *            the desired key type for the container
	 * @param contentType
	 *            the desired content type for the container
	 * @param copy
	 *            whether to copy the result
	 * @return the casted container with the specified types
	 */
	@Override
	T cast(IScope scope, Object obj, Object param, IType<?> keyType, IType<?> contentType, boolean copy);

	/**
	 * Creates a parameterized version of this container type with a specified content type.
	 * <p>
	 * This method is used to build type expressions like {@code list<int>} from a base {@code list} type. The key type
	 * is inherited from this type.
	 * </p>
	 *
	 * @param sub1
	 *            the content type parameter
	 * @return a new parameterized container type
	 */
	IContainerType<?> of(IType<?> sub1);

	/**
	 * Creates a parameterized version of this container type with specified key and content types.
	 * <p>
	 * This method is used to build type expressions like {@code map<string,float>} from a base {@code map} type,
	 * specifying both key and content types.
	 * </p>
	 *
	 * @param sub1
	 *            the key type parameter
	 * @param sub2
	 *            the content type parameter
	 * @return a new parameterized container type
	 */
	IContainerType<?> of(IType<?> sub1, IType<?> sub2);

	/**
	 * Serializes this container type to JSON format.
	 * <p>
	 * The JSON representation includes the type name and its key and content type parameters.
	 * </p>
	 *
	 * @param json
	 *            the JSON context
	 * @return a JSON representation of this container type
	 */
	@Override
	default IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(Types.TYPE, IKeyword.NAME, json.valueOf(getGamlType()), "key",
				json.valueOf(getKeyType()), "content", json.valueOf(getContentType()));
	}

}