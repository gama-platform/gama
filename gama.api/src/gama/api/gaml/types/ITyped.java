/*******************************************************************************************************
 *
 * ITyped.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;



/**
 * Represents objects that have an associated GAML type.
 * <p>
 * This interface is implemented by all GAML objects (including agents, values, and expressions)
 * that can be queried for their runtime type. The type system uses this interface to perform
 * type checking, casting, and coercion operations.
 * </p>
 * <p>
 * Implementors must ensure that {@link #getGamlType()} never returns null, returning
 * {@link Types#NO_TYPE} instead when no specific type can be determined.
 * </p>
 *
 * @author drogoul
 * @since GAMA 1.0
 * @see IType
 * @see GamaType
 * @see Types
 */
public interface ITyped {

	/**
	 * Returns the GAML type of this object.
	 * <p>
	 * This method provides runtime type information used for type checking and casting.
	 * The returned type corresponds to one of the types defined in the GAML type system.
	 * </p>
	 *
	 * @return the GAML type of this object, or {@link Types#NO_TYPE} if no type applies (never null)
	 * @see GamaType
	 * @see IType
	 */
	IType<?> getGamlType();

}
