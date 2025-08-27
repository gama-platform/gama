/*******************************************************************************************************
 *
 * IValue.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.interfaces.IGamlable;
import gama.gaml.interfaces.IJsonable;
import gama.gaml.types.IType;

/**
 * Represents a 'value' in GAML (a Java object that can provide a GAML type, be serializable into a GAML expression, and
 * be copied
 *
 * @author drogoul
 * @since 19 nov. 2008
 *
 */
public interface IValue extends IGamlable, ITyped, IJsonable {

	/**
	 * Returns the string 'value' of this value.
	 *
	 * @param scope
	 *            the current GAMA scope
	 * @return a string representing this value (not necessarily its serialization in GAML)
	 * @throws GamaRuntimeException
	 */
	String stringValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	default int intValue(final IScope scope) {
		return 0;
	}

	/**
	 * Float value.
	 *
	 * @param scope
	 *            the scope
	 * @return the double
	 */
	default double floatValue(final IScope scope) {
		return intValue(scope);
	}

	/**
	 * Returns a copy of this value
	 *
	 * @param scope
	 *            the current GAMA scope
	 * @return a copy of this value. The definition of copy (whether shallow or deep, etc.) depends on the subclasses
	 * @throws GamaRuntimeException
	 */
	IValue copy(IScope scope) throws GamaRuntimeException;

	/**
	 * @param scope
	 * @return
	 */
	default IType<?> computeRuntimeType(final IScope scope) {
		return getGamlType();
	}

}
