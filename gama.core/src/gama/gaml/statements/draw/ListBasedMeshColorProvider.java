/*******************************************************************************************************
 *
 * ListBasedMeshColorProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.util.List;

import gama.api.types.color.IColor;
import gama.api.ui.layers.IMeshColorProvider;

/**
 * A simple implementation of the color provider that picks a color using the index of the cell being drawn (in a cyclic
 * manner so as to allow lists of colors with a smaller size than the field).
 *
 * @author drogoul
 *
 */
public class ListBasedMeshColorProvider implements IMeshColorProvider {

	/** The components. */
	private final double[] components;

	/** The size. */
	private final int size;

	/**
	 * Instantiates a new list based mesh color provider.
	 *
	 * @param colors
	 *            the colors
	 */
	public ListBasedMeshColorProvider(final List<? extends IColor> colors) {
		this.size = colors.size();
		components = new double[size * 4];
		for (int i = 0; i < size; ++i) {
			IColor color = colors.get(i);
			if (color != null) {
				components[i * 4] = color.red() / 255d;
				components[i * 4 + 1] = color.green() / 255d;
				components[i * 4 + 2] = color.blue() / 255d;
				components[i * 4 + 3] = color.alpha() / 255d;
			}

		}
	}

	@Override
	public double[] getColor(final int index, final double elevation, final double min, final double max,
			final double[] rgb) {
		double[] result = rgb;
		if (result == null) { result = new double[4]; }
		int i = index % size;
		result[0] = components[i * 4];
		result[1] = components[i * 4 + 1];
		result[2] = components[i * 4 + 2];
		result[3] = components[i * 4 + 3];
		return result;
	}

}
