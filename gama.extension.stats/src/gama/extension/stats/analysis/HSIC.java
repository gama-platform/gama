/*******************************************************************************************************
 *
 * HSIC.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats.analysis;

import java.util.Arrays;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.utils.random.IRandom;
import gama.api.utils.random.RandomUtils;

/**
 * Hilbert-Schmidt Independence Criterion (HSIC) implementation. HSIC is a kernel-based statistic to test the
 * independence of two variables.
 */
public class HSIC {

	/** Relative tolerance for floating-point tie comparisons in permutation tests. */
	private static final double TIE_TOLERANCE = 1e-12;

	/** Threshold beyond which pairwise differences could overflow Double.MAX_VALUE on subtraction. */
	private static final double OVERFLOW_SAFE_BOUND = Double.MAX_VALUE / 2.0;

	/**
	 * Computes the normalized HSIC value (R2-HSIC) for two sets of observations. This value is bounded between 0 and 1,
	 * making it comparable across variables.
	 *
	 * @param x
	 *            observations of the first variable
	 * @param y
	 *            observations of the second variable
	 * @return the normalized HSIC value
	 */
	public static double computeNormalizedHSIC(final double[] x, final double[] y) {
		if (x == null || y == null || x.length != y.length)
			throw new IllegalArgumentException("Input arrays must be non-null and have the same length");
		if (x.length < 2) return 0.0;

		final boolean unbiased = x.length >= 4;
		final double hsicXY = computeHSIC(x, y, unbiased);
		final double hsicXX = computeHSIC(x, x, unbiased);
		final double hsicYY = computeHSIC(y, y, unbiased);

		final double den = Math.sqrt(Math.max(0.0, hsicXX) * Math.max(0.0, hsicYY));
		if (den <= 0.0) return 0.0;
		return Math.min(1.0, Math.max(0.0, hsicXY) / den);
	}

	/**
	 * Computes the HSIC value for two sets of observations.
	 *
	 * @param x
	 *            observations of the first variable
	 * @param y
	 *            observations of the second variable
	 * @param unbiased
	 *            if true, uses the unbiased estimator (can be negative)
	 * @return the HSIC value
	 */
	public static double computeHSIC(final double[] x, final double[] y, final boolean unbiased) {
		if (x == null || y == null || x.length != y.length)
			throw new IllegalArgumentException("Input arrays must be non-null and have the same length");
		final int n = x.length;
		if (n < (unbiased ? 4 : 2)) return 0.0;
		for (int i = 0; i < n; i++) {
			if (!Double.isFinite(x[i]) || !Double.isFinite(y[i]))
				throw new IllegalArgumentException("Input arrays must contain finite values");
		}

		final double scaleX = computeScale(x);
		final double scaleY = computeScale(y);

		final double sigmaX = estimateBandwidth(x, scaleX);
		final double sigmaY = estimateBandwidth(y, scaleY);

		final double[][] K = computeGramMatrix(x, sigmaX, scaleX);
		final double[][] L = computeGramMatrix(y, sigmaY, scaleY);

		if (unbiased) {
			// Unbiased estimator formula according to Song et al. (2007)
			// HSIC = 1/(n(n-3)) * [ trace(KL) + sum(K)sum(L)/( (n-1)(n-2) ) - 2/(n-2) sum(K*L_rows) ]
			// with diagonal elements of K and L set to 0.
			for (int i = 0; i < n; i++) {
				K[i][i] = 0.0;
				L[i][i] = 0.0;
			}

			double traceKL = 0.0;
			double sumK = 0.0;
			double sumL = 0.0;
			double sumKLrows = 0.0;

			final double[] rowSumsK = new double[n];
			final double[] rowSumsL = new double[n];

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					traceKL += K[i][j] * L[j][i];
					rowSumsK[i] += K[i][j];
					rowSumsL[i] += L[i][j];
				}
				sumK += rowSumsK[i];
				sumL += rowSumsL[i];
			}

			for (int i = 0; i < n; i++) { sumKLrows += rowSumsK[i] * rowSumsL[i]; }

			return (traceKL + sumK * sumL / ((n - 1.0) * (n - 2.0)) - 2.0 * sumKLrows / (n - 2.0)) / (n * (n - 3.0));
		}
		// Biased (empirical) estimator
		final double[][] Kc = centerMatrix(K);
		final double[][] Lc = centerMatrix(L);
		double trace = 0.0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) { trace += Kc[i][j] * Lc[j][i]; }
		}
		final double denom = (n - 1.0) * (n - 1.0);
		return Math.max(0.0, trace) / denom;
	}

	/**
	 * Computes the p-value for the HSIC test using a permutation test.
	 *
	 * @param scope
	 *            the execution scope providing interruption status and random stream
	 * @param x
	 *            observations of the first variable
	 * @param y
	 *            observations of the second variable
	 * @param permutations
	 *            number of permutations
	 * @return the p-value
	 */
	public static double computePValue(final IScope scope, final double[] x, final double[] y, final int permutations) {
		if (permutations <= 0)
			throw GamaRuntimeException.error("hsic_p_value requires a positive permutation count", scope);
		if (x == null || y == null || x.length != y.length || x.length < 2)
			throw GamaRuntimeException.error(
					"hsic_p_value requires equally sized arrays of at least two observations", scope);
		for (int i = 0; i < x.length; i++) {
			if (!Double.isFinite(x[i]) || !Double.isFinite(y[i]))
				throw GamaRuntimeException.error("hsic_p_value requires finite observations", scope);
		}
		if (scope != null && scope.interrupted())
			throw GamaRuntimeException.error("hsic_p_value computation was interrupted", scope);

		final int n = x.length;
		final double scaleX = computeScale(x);
		final double scaleY = computeScale(y);
		final double[][] Kc = centerMatrix(computeGramMatrix(x, estimateBandwidth(x, scaleX), scaleX));
		final double[][] Lc = centerMatrix(computeGramMatrix(y, estimateBandwidth(y, scaleY), scaleY));

		double observedTrace = 0.0;
		double normK = 0.0;
		double normL = 0.0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				final double kij = Kc[i][j];
				final double lij = Lc[i][j];
				observedTrace += kij * lij;
				normK += kij * kij;
				normL += lij * lij;
			}
		}
		final double threshold = observedTrace - TIE_TOLERANCE * Math.sqrt(normK * normL);

		final IRandom rnd = scope == null || scope.getRandom() == null ? new RandomUtils() : scope.getRandom();
		final int[] perm = new int[n];
		for (int i = 0; i < n; i++) { perm[i] = i; }

		int count = 0;
		for (int p = 0; p < permutations; p++) {
			if (scope != null && scope.interrupted())
				throw GamaRuntimeException.error("hsic_p_value computation was interrupted", scope);
			rnd.shuffleInPlace(perm);
			double permTrace = 0.0;
			for (int i = 0; i < n; i++) {
				final double[] kcRow = Kc[i];
				final double[] lcRow = Lc[perm[i]];
				for (int j = 0; j < n; j++) { permTrace += kcRow[j] * lcRow[perm[j]]; }
			}
			if (permTrace >= threshold) { count++; }
		}
		return (count + 1.0) / (permutations + 1.0);
	}

	/**
	 * Determines whether the sample contains extreme values that require 0.5 pre-scaling to prevent
	 * overflow on pairwise subtraction, or whether direct subtraction can be used to preserve full precision
	 * for subnormal numbers (down to {@link Double#MIN_VALUE}).
	 *
	 * @param data
	 *            the observations
	 * @return 0.5 if extreme values exceed {@code Double.MAX_VALUE / 2}, 1.0 otherwise
	 */
	private static double computeScale(final double[] data) {
		double max = 0.0;
		for (final double v : data) {
			final double abs = Math.abs(v);
			if (abs > max) { max = abs; }
		}
		return max > OVERFLOW_SAFE_BOUND ? 0.5 : 1.0;
	}

	/**
	 * Computes the RBF Gram matrix. When {@code scale == 0.5}, differences are pre-scaled by 0.5 to avoid
	 * overflow on extreme values; when {@code scale == 1.0}, direct differences preserve exact subnormal precision.
	 *
	 * @param data
	 *            the observations
	 * @param sigma
	 *            the RBF bandwidth in the scaled coordinate space ({@code > 0})
	 * @param scale
	 *            the scale factor (1.0 or 0.5)
	 * @return the symmetric Gram matrix
	 */
	private static double[][] computeGramMatrix(final double[] data, final double sigma, final double scale) {
		final int n = data.length;
		final double[][] gram = new double[n][n];
		for (int i = 0; i < n; i++) {
			gram[i][i] = 1.0;
			final double xi = scale == 1.0 ? data[i] : scale * data[i];
			for (int j = i + 1; j < n; j++) {
				final double xj = scale == 1.0 ? data[j] : scale * data[j];
				final double ratio = (xi - xj) / sigma;
				final double val = Math.exp(-0.5 * ratio * ratio);
				gram[i][j] = val;
				gram[j][i] = val;
			}
		}
		return gram;
	}

	/**
	 * Double-centers a symmetric matrix ({@code H M H} where {@code H = I - (1/n) 1 1^T}).
	 *
	 * @param matrix
	 *            the symmetric matrix
	 * @return the centered matrix
	 */
	private static double[][] centerMatrix(final double[][] matrix) {
		final int n = matrix.length;
		final double[] rowMeans = new double[n];
		double totalMean = 0.0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) { rowMeans[i] += matrix[i][j]; }
			totalMean += rowMeans[i];
			rowMeans[i] /= n;
		}
		totalMean /= (double) n * n;

		final double[][] centered = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) { centered[i][j] = matrix[i][j] - rowMeans[i] - rowMeans[j] + totalMean; }
		}
		return centered;
	}

	/**
	 * Median heuristic for the RBF bandwidth in the given scale. Uses direct pairwise distances when
	 * {@code scale == 1.0} (preventing subnormal precision loss) and half-scaled distances when {@code scale == 0.5}
	 * (preventing overflow). Falls back to the median of strictly positive distances when ties dominate, and to 1.0
	 * when all values are identical.
	 */
	private static double estimateBandwidth(final double[] data, final double scale) {
		final int n = data.length;
		if (n < 2) return 1.0;
		final long pairCount = (long) n * (n - 1) / 2;
		if (pairCount > Integer.MAX_VALUE - 8)
			throw new IllegalArgumentException("Sample size exceeds maximum supported capacity");
		final int size = (int) pairCount;
		final double[] dists = new double[size];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			final double xi = scale == 1.0 ? data[i] : scale * data[i];
			for (int j = i + 1; j < n; j++) {
				final double xj = scale == 1.0 ? data[j] : scale * data[j];
				dists[idx++] = Math.abs(xi - xj);
			}
		}
		Arrays.sort(dists);
		final double median = medianSlice(dists, 0, size);
		if (median > 0.0) return median;
		for (int k = 0; k < size; k++) {
			if (dists[k] > 0.0) return medianSlice(dists, k, size);
		}
		return 1.0;
	}

	/**
	 * Computes the exact median of the sorted slice {@code sorted[lo..hi)}.
	 */
	private static double medianSlice(final double[] sorted, final int lo, final int hi) {
		final int len = hi - lo;
		final int mid = lo + (len >>> 1);
		if ((len & 1) == 1) return sorted[mid];
		return 0.5 * sorted[mid - 1] + 0.5 * sorted[mid];
	}
}
