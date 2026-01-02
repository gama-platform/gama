/*******************************************************************************************************
 *
 * ITyped.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.interfaces;

import gama.annotations.precompiler.OkForAPI;
import gama.gaml.types.IType;

/**
 * Interface ITyped. Represents all the objects (incl. agents) that are provided and can return a GAML type
 *
 * @author drogoul
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface ITyped {

	/**
	 * Returns the type of this object with respect to the GAML available types
	 *
	 * @return a GAML type or Types.NO_TYPE if none (never null)
	 * @see GamaType, IType
	 */
	IType<?> getGamlType();

}
