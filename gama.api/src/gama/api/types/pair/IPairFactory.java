/*******************************************************************************************************
 *
 * IPairFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.pair;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Interface IPairFactory.
 * 
 * Factory interface for creating {@link IPair} instances. This interface defines the contract for implementations that
 * create pairs from various sources, including direct values, objects to be converted, and default/empty pairs.
 * 
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 * <li>Creating pairs from two separate values</li>
 * <li>Converting objects (lists, maps, points, etc.) to pairs</li>
 * <li>Handling type casting and conversion</li>
 * <li>Providing default empty pairs</li>
 * </ul>
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see IPair
 * @see GamaPairFactory
 */
public interface IPairFactory {

	/**
	 * Creates a pair from an object (e.g. attempting to cast or convert).
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the source object
	 * @param keyType
	 *            the expected key type
	 * @param contentsType
	 *            the expected value type
	 * @param copy
	 *            whether to copy contents
	 * @return the created pair
	 * @throws GamaRuntimeException
	 *             if creation fails
	 */
	IPair createFrom(IScope scope, Object obj, IType keyType, IType contentsType, boolean copy)
			throws GamaRuntimeException;

	/**
	 * Creates a pair from two objects, specifying their types.
	 *
	 * @param v1
	 *            the key (first element)
	 * @param v2
	 *            the value (second element)
	 * @param keyType
	 *            the type of the key
	 * @param keyType2
	 *            the type of the value
	 * @return the created pair
	 */
	IPair createFrom(Object v1, Object v2, IType keyType, IType keyType2);

	/**
	 * Creates a default empty pair.
	 *
	 * @return the default pair
	 */
	IPair createDefault();

	/**
	 * Creates a new pair from two objects (key and value).
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the created pair
	 */
	IPair create(Object key, Object value);

}