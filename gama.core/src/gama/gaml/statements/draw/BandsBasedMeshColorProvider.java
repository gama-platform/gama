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

import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.IField;

/**
 * An implementation of the color provider that picks a color using the index of the cell being drawn in the R, G, and B
 * bands defined in the field. Invoked by using: 'color: my_field.bands'
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
	public BandsBasedMeshColorProvider(final List<GamaField> bands) {
		if (bands.size() < 4)
			throw GamaRuntimeException.error("Number of bands should be at least 3 ", GAMA.getRuntimeScope());
		boolean hasAlpha = bands.size() > 4;
		IField primary = bands.get(0);
		this.size = primary.getMatrix().length;
		components = new double[size * 4];
		double[] mR = bands.get(0).getMatrix();
		double[] mG = bands.get(1).getMatrix();
		double[] mB = bands.get(2).getMatrix();
		for (int i = 0; i < size; ++i) {
			components[i * 3] = mR[i] / 255d;
			components[i * 3 + 1] = mG[i] / 255d;
			components[i * 3 + 2] = mB[i] / 255d;
			components[i * 3 + 3] = hasAlpha ? bands.get(4).getMatrix()[i] / 255d : 1d;
		}
	}

	@Override
	public double[] getColor(final int index, final double elevation, final double min, final double max,
			final double[] rgb) {
		double[] result = rgb;
		if (result == null) { result = newArray(); }
		result[0] = components[index * 3];
		result[1] = components[index * 3 + 1];
		result[2] = components[index * 3 + 2];
		result[3] = components[index * 3 + 3];
		return result;
	}

}
