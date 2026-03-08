/*******************************************************************************************************
 *
 * IMeshSmoothProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

/**
 * The Interface IMeshSmoothProvider.
 */
public interface IMeshSmoothProvider {

	/**
	 * Smooth. Applies a "smoothing" algorithm to the data so as to soften the visualisation of fields. Usually implies
	 * some diffusion too.
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param data
	 *            the data
	 * @param noData
	 *            the no data
	 * @param passes
	 *            the passes
	 */
	double[] smooth(final int cols, final int rows, final double[] data, double noData, final int passes);

	/**
	 * A safe way to access the value of the data
	 *
	 * @param cols
	 *            the cols
	 * @param rows
	 *            the rows
	 * @param data
	 *            the data
	 * @param x0
	 *            the x 0
	 * @param y0
	 *            the y 0
	 * @return the double
	 */
	default double get(final int cols, final int rows, final double[] data, final int x0, final int y0) {
		var x = x0 < 0 ? 0 : x0 > cols - 1 ? cols - 1 : x0;
		var y = y0 < 0 ? 0 : y0 > rows - 1 ? rows - 1 : y0;
		return data[y * cols + x];
	}

}
