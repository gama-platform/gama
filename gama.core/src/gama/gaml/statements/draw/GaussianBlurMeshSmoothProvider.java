/*******************************************************************************************************
 *
 * GaussianBlurMeshSmoothProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import gama.api.ui.layers.IMeshSmoothProvider;

/**
 * The Class GaussianBlurMeshSmoothProvider. A "smoothing" algorithm based on an efficient approximation of a Gaussian
 * Blur. See http://blog.ivank.net/fastest-gaussian-blur.html *
 */
public class GaussianBlurMeshSmoothProvider implements IMeshSmoothProvider {

	@Override
	public double[] smooth(final int cols, final int rows, final double[] data, final double noData, final int passes) {
		if (passes <= 0 || data.length == 0) return data.clone();
		double[] current = data.clone();
		double[] scratch = new double[current.length];
		double[] output = new double[current.length];
		int nbBoxes = 3;
		var wIdeal = Math.sqrt(12 * passes * passes / nbBoxes + 1); // Ideal averaging filter width
		double wl = Math.floor(wIdeal);
		if (wl % 2 == 0) { wl--; }
		double wu = wl + 2;
		var mIdeal = (12 * passes * passes - nbBoxes * wl * wl - 4 * nbBoxes * wl - 3 * nbBoxes) / (-4 * wl - 4);
		var m = Math.round(mIdeal);
		double[] sizes = new double[nbBoxes];
		for (var i = 0; i < nbBoxes; i++) { sizes[i] = i < m ? wl : wu; } // number of "boxes"
		var boxes = sizes;
		int r = (int) Math.round((boxes[0] - 1) / 2);
		if (r <= cols / 2 && r <= rows / 2) {
			boxBlurHorizontal(cols, rows, current, scratch, r);
			boxBlurVertical(cols, rows, scratch, output, r);
			final double[] previous = current;
			current = output;
			output = previous;
		}
		r = (int) Math.round((boxes[1] - 1) / 2);
		if (r <= cols / 2 && r <= rows / 2) {
			boxBlurHorizontal(cols, rows, current, scratch, r);
			boxBlurVertical(cols, rows, scratch, output, r);
			final double[] previous = current;
			current = output;
			output = previous;
		}
		r = (int) Math.round((boxes[2] - 1) / 2);
		if (r <= cols / 2 && r <= rows / 2) {
			boxBlurHorizontal(cols, rows, current, scratch, r);
			boxBlurVertical(cols, rows, scratch, output, r);
			final double[] previous = current;
			current = output;
			output = previous;
		}
		return current;
	}

	/**
	 * Box blur operating vertically
	 */
	void boxBlurVertical(final int cols, final int rows, final double[] src, final double[] dst, final int r) {
		double iarr = 1d / (r + r + 1);
		for (var i = 0; i < rows; i++) {
			var ti = i * cols;
			var li = ti;
			var ri = ti + r;
			var fv = src[ti];
			var lv = src[ti + cols - 1];
			var val = (r + 1) * fv;
			for (var j = 0; j < r; j++) { val += src[ti + j]; }
			for (var j = 0; j <= r; j++) {
				val += src[ri++] - fv;
				dst[ti++] = val * iarr;
			}
			for (var j = r + 1; j < cols - r; j++) {
				val += src[ri++] - src[li++];
				dst[ti++] = val * iarr;
			}
			for (var j = cols - r; j < cols; j++) {
				val += lv - src[li++];
				dst[ti++] = val * iarr;
			}
		}
	}

	/**
	 * Box blur operating horizontally
	 *
	 * @return the function
	 */
	void boxBlurHorizontal(final int cols, final int rows, final double[] src, final double[] dst, final int r) {
		double iarr = 1d / (r + r + 1);
		for (var i = 0; i < cols; i++) {
			var ti = i;
			var li = ti;
			var ri = ti + r * cols;
			var fv = src[ti];
			var lv = src[ti + cols * (rows - 1)];
			var val = (r + 1) * fv;
			for (var j = 0; j < r; j++) { val += src[ti + j * cols]; }
			for (var j = 0; j <= r; j++) {
				val += src[ri] - fv;
				dst[ti] = val * iarr;
				ri += cols;
				ti += cols;
			}
			for (var j = r + 1; j < rows - r; j++) {
				val += src[ri] - src[li];
				dst[ti] = val * iarr;
				li += cols;
				ri += cols;
				ti += cols;
			}
			for (var j = rows - r; j < rows; j++) {
				val += lv - src[li];
				dst[ti] = val * iarr;
				li += cols;
				ti += cols;
			}
		}
	}

}
