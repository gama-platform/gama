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

		final double halfSigmaX = estimateHalfBandwidth(x);
		final double halfSigmaY = estimateHalfBandwidth(y);

		final double[][] K = computeGramMatrix(x, halfSigmaX);
		final double[][] L = computeGramMatrix(y, halfSigmaY);

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
		final double[][] Kc = centerMatrix(computeGramMatrix(x, estimateHalfBandwidth(x)));
		final double[][] Lc = centerMatrix(computeGramMatrix(y, estimateHalfBandwidth(y)));

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
	 * Computes the RBF Gram matrix using half-scaled differences {@code 0.5 * x_i - 0.5 * x_j} and
	 * {@code halfSigma = 0.5 * sigma} so extreme finite values (up to {@link Double#MAX_VALUE}) never overflow.
	 *
	 * @param data
	 *            the observations
	 * @param halfSigma
	 *            half of the RBF bandwidth ({@code > 0})
	 * @return the symmetric Gram matrix
	 */
	private static double[][] computeGramMatrix(final double[] data, final double halfSigma) {
		final int n = data.length;
		final double[][] gram = new double[n][n];
		for (int i = 0; i < n; i++) {
			gram[i][i] = 1.0;
			final double halfXi = 0.5 * data[i];
			for (int j = i + 1; j < n; j++) {
				final double ratio = (halfXi - 0.5 * data[j]) / halfSigma;
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
	 * Median heuristic for half the RBF bandwidth ({@code 0.5 * sigma}), computed on half-scaled pairwise distances so
	 * finite extremes never overflow or underflow due to intermediate squaring. Falls back to the median of strictly
	 * positive pairwise distances when ties dominate, and to {@code 0.5} (i.e. {@code sigma = 1.0}) when constant.
	 */
	private static double estimateHalfBandwidth(final double[] data) {
		final int n = data.length;
		if (n < 2) return 0.5;
		final long pairCount = (long) n * (n - 1) / 2;
		if (pairCount > Integer.MAX_VALUE - 8)
			throw new IllegalArgumentException("Sample size exceeds maximum supported capacity");
		final int size = (int) pairCount;
		final double[] halfDists = new double[size];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			final double halfXi = 0.5 * data[i];
			for (int j = i + 1; j < n; j++) { halfDists[idx++] = Math.abs(halfXi - 0.5 * data[j]); }
		}
		Arrays.sort(halfDists);
		final double median = medianSlice(halfDists, 0, size);
		if (median > 0.0) return median;
		for (int k = 0; k < size; k++) {
			if (halfDists[k] > 0.0) return medianSlice(halfDists, k, size);
		}
		return 0.5;
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
