/*******************************************************************************************************
 *
 * GamlConstantDocumentation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.documentation;

/**
 * Constant documentation that cannot change once instantiated
 */
public record GamlConstantDocumentation(String value) implements IGamlDocumentation {

	@Override
	public String getContents() { return value; }

	@Override
	public String toString() {
		return value;
	}

}