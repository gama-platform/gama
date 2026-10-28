/*******************************************************************************************************
 *
 * MeshSampling.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

/**
 * Utilities used by the mesh drawers to bound the size of the geometry built for large fields (level of detail).
 */
public final class MeshSampling {

	/** The maximum size, in bytes, of a Java direct buffer */
	private static final long MAX_DIRECT_BUFFER_BYTES = Integer.MAX_VALUE - 8L;

	/**
	 * Instantiates a new mesh sampling.
	 */
	private MeshSampling() {}

	/**
	 * Computes the stride (in cells) to use so that a field of {@code cols} x {@code rows} cells, sampled every
	 * {@code step} cells in both directions, contains at most {@code maxCells} cells.
	 *
	 * @param cols
	 *            the number of columns of the field
	 * @param rows
	 *            the number of rows of the field
	 * @param maxCells
	 *            the maximum number of cells (0 or less means no limit)
	 * @return a step >= 1
	 */
	public static int stepFor(final int cols, final int rows, final int maxCells) {
		if (maxCells <= 0) return 1;
		long cells = (long) cols * rows;
		if (cells <= maxCells) return 1;
		int step = (int) Math.ceil(Math.sqrt((double) cells / maxCells));
		while ((long) sampledSize(cols, step) * sampledSize(rows, step) > maxCells) { step++; }
		return step;
	}

	/**
	 * Number of samples along a dimension of {@code size} cells when sampling every {@code step} cells.
	 *
	 * @param size
	 *            the size
	 * @param step
	 *            the step
	 * @return the number of samples
	 */
	public static int sampledSize(final int size, final int step) {
		return (size + step - 1) / step;
	}

	/**
	 * Downsamples a row-major field by averaging blocks of {@code step} x {@code step} cells. Cells equal to
	 * {@code noData} (or NaN) are ignored; a block containing only such cells receives {@code noData}.
	 *
	 * @param data
	 *            the row-major data (length cols * rows)
	 * @param cols
	 *            the number of columns
	 * @param rows
	 *            the number of rows
	 * @param step
	 *            the step (> 1)
	 * @param noData
	 *            the value representing the absence of data
	 * @return a new array of length sampledSize(cols, step) * sampledSize(rows, step)
	 */
	public static double[] downsample(final double[] data, final int cols, final int rows, final int step,
			final double noData) {
		final int nc = sampledSize(cols, step);
		final int nr = sampledSize(rows, step);
		final double[] result = new double[nc * nr];
		for (int j = 0; j < nr; j++) {
			final int y0 = j * step;
			final int y1 = Math.min(y0 + step, rows);
			for (int i = 0; i < nc; i++) {
				final int x0 = i * step;
				final int x1 = Math.min(x0 + step, cols);
				double sum = 0;
				int count = 0;
				for (int y = y0; y < y1; y++) {
					final int offset = y * cols;
					for (int x = x0; x < x1; x++) {
						final double v = data[offset + x];
						if (v == noData || Double.isNaN(v)) { continue; }
						sum += v;
						count++;
					}
				}
				result[j * nc + i] = count == 0 ? noData : sum / count;
			}
		}
		return result;
	}

	/**
	 * Checks that a direct buffer of {@code elements} elements of {@code bytesPerElement} bytes can be allocated (Java
	 * direct buffers are limited to 2GB).
	 *
	 * @param elements
	 *            the number of elements
	 * @param bytesPerElement
	 *            the size of each element in bytes
	 * @return true if the buffer can be allocated
	 */
	public static boolean fitsInDirectBuffer(final long elements, final int bytesPerElement) {
		return elements * bytesPerElement <= MAX_DIRECT_BUFFER_BYTES;
	}

}
