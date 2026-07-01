/*******************************************************************************************************
 *
 * ConvolutionBasedMeshSmoothProvider.java, in gama.core, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import gama.api.ui.layers.IMeshSmoothProvider;

/**
 * The Class ConvolutionBasedMeshSmoothProvider. A "smoothing" algorithm based on convolution. Slow, but can take into
 * account the noData value
 */
public class ConvolutionBasedMeshSmoothProvider implements IMeshSmoothProvider {

	@Override
	public double[] smooth(final int cols, final int rows, final double[] data, final double noData, final int passes) {
		var input = data;
		var output = new double[input.length];
		for (var i = 0; i < passes; i++) {
			if (cols < 3 || rows < 3) {
				for (var y = 0; y < rows; ++y) {
					for (var x = 0; x < cols; ++x) { blurAt(cols, rows, input, output, noData, x, y); }
				}
			} else {
				for (var x = 0; x < cols; ++x) { blurAt(cols, rows, input, output, noData, x, 0); }
				for (var y = 1; y < rows - 1; ++y) {
					blurAt(cols, rows, input, output, noData, 0, y);
					for (var x = 1; x < cols - 1; ++x) {
						final int index = x + y * cols;
						final double z00 = input[index - cols - 1];
						final double z01 = input[index - cols];
						final double z02 = input[index - cols + 1];
						final double z03 = input[index - 1];
						final double z = input[index];
						final double z05 = input[index + 1];
						final double z06 = input[index + cols - 1];
						final double z07 = input[index + cols];
						final double z08 = input[index + cols + 1];
						if (z00 == noData || z01 == noData || z02 == noData || z03 == noData || z == noData
								|| z05 == noData || z06 == noData || z07 == noData || z08 == noData) {
							continue;
						}
						output[index] = (z00 + z01 + z02 + z03 + z + z05 + z06 + z07 + z08) / 9d;
					}
					blurAt(cols, rows, input, output, noData, cols - 1, y);
				}
				for (var x = 0; x < cols; ++x) { blurAt(cols, rows, input, output, noData, x, rows - 1); }
			}
			input = output;
		}
		return output;

	}

	private void blurAt(final int cols, final int rows, final double[] input, final double[] output, final double noData,
			final int x, final int y) {
		final double z00 = get(cols, rows, input, x - 1, y - 1);
		final double z01 = get(cols, rows, input, x, y - 1);
		final double z02 = get(cols, rows, input, x + 1, y - 1);
		final double z03 = get(cols, rows, input, x - 1, y);
		final double z = get(cols, rows, input, x, y);
		final double z05 = get(cols, rows, input, x + 1, y);
		final double z06 = get(cols, rows, input, x - 1, y + 1);
		final double z07 = get(cols, rows, input, x, y + 1);
		final double z08 = get(cols, rows, input, x + 1, y + 1);
		if (z00 == noData || z01 == noData || z02 == noData || z03 == noData || z == noData || z05 == noData
				|| z06 == noData || z07 == noData || z08 == noData) {
			return;
		}
		output[x + y * cols] = (z00 + z01 + z02 + z03 + z + z05 + z06 + z07 + z08) / 9d;
	}

}
