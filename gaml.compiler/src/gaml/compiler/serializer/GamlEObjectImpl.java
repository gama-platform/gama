/*******************************************************************************************************
 *
 * GamlEObjectImpl.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.serializer;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * The Class GamlEObjectImpl.
 */
// 1. Extend the standard Xtext/EMF base class
public class GamlEObjectImpl extends MinimalEObjectImpl.Container {

	@Override
	public String toString() {
		// 2. Delegate the toString() call to GamlSerializerToString
		return GamlSerializerToString.asString(this);
	}

	/**
	 * As string.
	 *
	 * @return the string
	 */
	public String asString() {
		// 2. Delegate the toString() call to GamlSerializerToString
		return GamlSerializerToString.asString(this);
	}

	/**
	 * Super to string.
	 *
	 * @return the string
	 */
	// Helper method just in case you ever need the original EMF string
	public String superToString() {
		return super.toString();
	}
}