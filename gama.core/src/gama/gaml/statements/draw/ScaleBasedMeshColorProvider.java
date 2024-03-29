/*******************************************************************************************************
 *
 * ScaleBasedMeshColorProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.util.Map;

import gama.core.common.preferences.GamaPreferences;
import gama.core.util.GamaColor;
import gama.gaml.operators.Colors.GamaScale;

/**
 * Colors are chosen in a discrete map where the "weights" of colors correspond to elevations. If z is smaller or larger
 * than the values in the scale, the default color is returned
 *
 * @author drogoul
 *
 */
public class ScaleBasedMeshColorProvider implements IMeshColorProvider {

	/** The scale. */
	GamaScale scale; // should be already sorted

	/**
	 * Instantiates a new scale based mesh color provider.
	 *
	 * @param scale
	 *            the scale
	 */
	public ScaleBasedMeshColorProvider(final GamaScale scale) {
		this.scale = scale;
	}

	@Override
	public double[] getColor(final int index, final double z, final double min, final double max, final double[] rgb) {
		double[] result = rgb;
		if (result == null) { result = new double[4]; }
		GamaColor chosen = GamaPreferences.Displays.CORE_COLOR.getValue();
		for (Map.Entry<Double, GamaColor> entry : scale.entrySet()) {
			if (z < entry.getKey()) { break; }
			chosen = entry.getValue();
		}
		final GamaColor c = chosen;
		result[0] = c.getRed() / 255d;
		result[1] = c.getGreen() / 255d;
		result[2] = c.getBlue() / 255d;
		result[3] = 1d; // c.getAlpha() / 255d;
		return result;
	}

}
