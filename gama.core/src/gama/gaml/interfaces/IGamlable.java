/*******************************************************************************************************
 *
 * IGamlable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.interfaces;


/**
 * The interface IGamlable. Represents objects that can represent themselves in terms of GAML descriptions
 * (serialization).
 *
 * @author A. Drogoul
 * @since 13 dec. 2011
 *
 */
public interface IGamlable {

	/**
	 * Returns the serialization in GAML of this object, taking into account (or not) built-in structures
	 *
	 * @param includingBuiltIn
	 *            whether built-in structures should be part of the serialization in GAML (for instance, built-in
	 *            species within a model)
	 * @return a string that can be reinterpreted in GAML to reproduce the object
	 */
	default String serializeToGaml(final boolean includingBuiltIn) {
		return toString();
	}

//	/**
//	 * Returns the serialization in GAML of this object, using a scope if it is provided. Calls the regular function by
//	 * default
//	 *
//	 * @param scope
//	 * @param includingBuiltIn
//	 * @return
//	 */
//	default String serializeToGaml(final IScope scope, final boolean includingBuiltIn) {
//		return serializeToGaml(includingBuiltIn);
//	}
}
