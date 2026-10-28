/*******************************************************************************************************
 *
 * BandsBasedMeshColorProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.util.List;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.types.matrix.IField;
import gama.api.ui.layers.IMeshColorProvider;

/**
 * An implementation of the color provider that picks a color using the index of the cell being drawn in the R, G, B
 * (bands 0, 1, 2) and, if present, alpha (band 3) bands defined in the field. Invoked by using: 'color: my_field.bands'
 *
 * @author drogoul
 *
 */
public class BandsBasedMeshColorProvider implements IMeshColorProvider {

	/** The components. */
	private final double[] components;

	/** The size. */
	private final int size;

	/**
	 * Instantiates a new bands based mesh color provider.
	 *
	 * @param bands
	 *            the bands
	 */
	public BandsBasedMeshColorProvider(final List<IField> bands) {
		if (bands.size() < 3) throw GamaRuntimeException.error(
				"At least 3 bands (red, green, blue and optionally alpha) are required to color a mesh, but only "
						+ bands.size() + " are available",
				GAMA.getRuntimeScope());
		boolean hasAlpha = bands.size() > 3;
		IField primary = bands.get(0);
		this.size = primary.getMatrix().length;
		components = new double[size * 4];
		double[] mR = bands.get(0).getMatrix();
		double[] mG = bands.get(1).getMatrix();
		double[] mB = bands.get(2).getMatrix();
		double[] mA = hasAlpha ? bands.get(3).getMatrix() : null;
		// 4 components (RGBA) per cell
		for (int i = 0; i < size; ++i) {
			components[i * 4] = mR[i] / 255d;
			components[i * 4 + 1] = mG[i] / 255d;
			components[i * 4 + 2] = mB[i] / 255d;
			components[i * 4 + 3] = mA != null ? mA[i] / 255d : 1d;
		}
	}

	@Override
	public double[] getColor(final int index, final double elevation, final double min, final double max,
			final double[] rgb) {
		double[] result = rgb;
		if (result == null) { result = newArray(); }
		final int i = index * 4;
		result[0] = components[i];
		result[1] = components[i + 1];
		result[2] = components[i + 2];
		result[3] = components[i + 3];
		return result;
	}

}
