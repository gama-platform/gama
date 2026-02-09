/*******************************************************************************************************
 *
 * IValue.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;


import gama.api.data.json.IJsonable;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITyped;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IGamlable;

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
	 */
	String stringValue(IScope scope);

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
	 */
	IValue copy(IScope scope);

	/**
	 * @param scope
	 * @return
	 */
	default IType<?> computeRuntimeType(final IScope scope) {
		return getGamlType();
	}

}
