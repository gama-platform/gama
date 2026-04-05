/*******************************************************************************************************
 *
 * DefaultAttributeIO.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

/**
 * The Class DefaultAttributeIO.
 */
public class DefaultAttributeIO implements AttributeIO {

	@Override
	public Object parse(final String att, final String value) {
		return value;
	}

	@Override
	public String encode(final String att, final Object value) {
		return value != null ? value.toString() : null;
	}
}
