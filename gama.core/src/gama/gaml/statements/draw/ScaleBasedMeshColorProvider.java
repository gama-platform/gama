/*******************************************************************************************************
 *
 * ScaleBasedMeshColorProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.util.Map;

import gama.api.data.objects.IColor;
import gama.api.ui.layers.IMeshColorProvider;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.util.color.GamaScale;

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
		IColor chosen = GamaPreferences.Displays.CORE_COLOR.getValue();
		for (Map.Entry<Double, IColor> entry : scale.entrySet()) {
			if (z < entry.getKey()) { break; }
			chosen = entry.getValue();
		}
		final IColor c = chosen;
		result[0] = c.red() / 255d;
		result[1] = c.green() / 255d;
		result[2] = c.blue() / 255d;
		result[3] = 1d; // c.getAlpha() / 255d;
		return result;
	}

}
