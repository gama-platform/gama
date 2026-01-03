/*******************************************************************************************************
 *
 * ColorBasedMeshColorProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import gama.core.util.IColor;

/**
 * The Class ColorBasedMeshColorProvider.
 */
public class ColorBasedMeshColorProvider implements IMeshColorProvider {

	/** The b. */
	final double r, g, b;

	/**
	 * Instantiates a new color based mesh color provider.
	 *
	 * @param c
	 *            the c
	 */
	public ColorBasedMeshColorProvider(final IColor c) {
		r = c.red() / 255d;
		g = c.green() / 255d;
		b = c.blue() / 255d;
	}

	@Override
	public double[] getColor(final int index, final double z, final double min, final double max, final double[] rgb) {
		double[] result = rgb;
		if (result == null) { result = newArray(); }
		double ratio = (z - min) * 1.5 / (max - min); // we lighten it a bit
		result[0] = r * ratio;
		result[1] = g * ratio;
		result[2] = b * ratio;
		result[3] = 1d;
		return result;
	}

}
