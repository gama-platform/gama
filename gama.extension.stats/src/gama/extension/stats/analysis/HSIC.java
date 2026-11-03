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
	 * Encapsulates a double-centered Gram kernel matrix along with its squared Frobenius norm.
	 *
	 * @param matrix
	 *            the centered Gram matrix
	 * @param frobeniusNormSq
	 *            the sum of squared elements (Frobenius norm squared)
	 */
	private record CenteredKernel(double[][] matrix, double frobeniusNormSq) {}

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
		validateInputs(x, y);
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
		validateFiniteInputs(x, y);
		final int n = x.length;
		if (n < (unbiased ? 4 : 2)) return 0.0;

		final double scaleX = computeScale(x);
		final double scaleY = computeScale(y);
		final double[][] K = computeGramMatrix(x, estimateBandwidth(x, scaleX), scaleX);
		final double[][] L = computeGramMatrix(y, estimateBandwidth(y, scaleY), scaleY);

		return unbiased ? computeUnbiased(K, L, n) : computeBiased(K, L, n);
	}

	/**
	 * Computes the unbiased HSIC estimator according to Song et al. (2007).
	 */
	private static double computeUnbiased(final double[][] K, final double[][] L, final int n) {
		zeroDiagonal(K, n);
		zeroDiagonal(L, n);

		double traceKL = 0.0;
		double sumK = 0.0;
		double sumL = 0.0;
		double sumKLrows = 0.0;

		for (int i = 0; i < n; i++) {
			double rowK = 0.0;
			double rowL = 0.0;
			for (int j = 0; j < n; j++) {
				traceKL += K[i][j] * L[j][i];
				rowK += K[i][j];
				rowL += L[i][j];
			}
			sumK += rowK;
			sumL += rowL;
			sumKLrows += rowK * rowL;
		}

		final double term1 = traceKL;
		final double term2 = (sumK * sumL) / ((n - 1.0) * (n - 2.0));
		final double term3 = (2.0 * sumKLrows) / (n - 2.0);
		return (term1 + term2 - term3) / (n * (n - 3.0));
	}

	/**
	 * Computes the empirical (biased) HSIC estimator.
	 */
	private static double computeBiased(final double[][] K, final double[][] L, final int n) {
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
		validatePValuePreconditions(scope, x, y, permutations);

		final int n = x.length;
		final CenteredKernel kx = buildCenteredKernel(x);
		final CenteredKernel ky = buildCenteredKernel(y);

		final int[] perm = initialPermutation(n);
		final double observedTrace = computeTrace(kx.matrix(), ky.matrix(), perm, n);
		final double threshold = observedTrace - TIE_TOLERANCE * Math.sqrt(kx.frobeniusNormSq() * ky.frobeniusNormSq());

		final IRandom rnd = resolveRandom(scope);
		final int count = countExceedingPermutations(scope, kx.matrix(), ky.matrix(), perm, threshold, permutations, rnd, n);
		return (count + 1.0) / (permutations + 1.0);
	}

	/**
	 * Constructs the centered Gram kernel matrix and calculates its squared Frobenius norm.
	 */
	private static CenteredKernel buildCenteredKernel(final double[] data) {
		final double scale = computeScale(data);
		final double sigma = estimateBandwidth(data, scale);
		final double[][] centered = centerMatrix(computeGramMatrix(data, sigma, scale));
		double normSq = 0.0;
		for (final double[] row : centered) {
			for (final double val : row) { normSq += val * val; }
		}
		return new CenteredKernel(centered, normSq);
	}

	/**
	 * Initializes an identity permutation index array.
	 */
	private static int[] initialPermutation(final int n) {
		final int[] perm = new int[n];
		for (int i = 0; i < n; i++) { perm[i] = i; }
		return perm;
	}

	/**
	 * Computes the trace of the product of two centered Gram matrices given a permutation of the second matrix.
	 */
	private static double computeTrace(final double[][] Kc, final double[][] Lc, final int[] perm, final int n) {
		double trace = 0.0;
		for (int i = 0; i < n; i++) {
			final double[] kcRow = Kc[i];
			final double[] lcRow = Lc[perm[i]];
			for (int j = 0; j < n; j++) { trace += kcRow[j] * lcRow[perm[j]]; }
		}
		return trace;
	}

	/**
	 * Executes the permutation Monte Carlo loop and counts the number of permutations where HSIC exceeds the threshold.
	 */
	private static int countExceedingPermutations(final IScope scope, final double[][] Kc, final double[][] Lc,
			final int[] perm, final double threshold, final int permutations, final IRandom rnd, final int n) {
		int count = 0;
		for (int p = 0; p < permutations; p++) {
			checkInterrupted(scope);
			rnd.shuffleInPlace(perm);
			if (computeTrace(Kc, Lc, perm, n) >= threshold) { count++; }
		}
		return count;
	}

	/**
	 * Determines whether the sample contains extreme values that require 0.5 pre-scaling to prevent
	 * overflow on pairwise subtraction, or whether direct subtraction can be used to preserve full precision
	 * for subnormal numbers (down to {@link Double#MIN_VALUE}).
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
		final double[] dists = computePairwiseDistances(data, scale, n);
		Arrays.sort(dists);
		final double median = medianSlice(dists, 0, dists.length);
		if (median > 0.0) return median;
		return findFirstPositiveMedian(dists);
	}

	/**
	 * Computes pairwise distances in the chosen scale.
	 */
	private static double[] computePairwiseDistances(final double[] data, final double scale, final int n) {
		final long pairCount = (long) n * (n - 1) / 2;
		if (pairCount > Integer.MAX_VALUE - 8)
			throw new IllegalArgumentException("Sample size exceeds maximum supported capacity");
		final double[] dists = new double[(int) pairCount];
		int idx = 0;
		for (int i = 0; i < n; i++) {
			final double xi = scale == 1.0 ? data[i] : scale * data[i];
			for (int j = i + 1; j < n; j++) {
				final double xj = scale == 1.0 ? data[j] : scale * data[j];
				dists[idx++] = Math.abs(xi - xj);
			}
		}
		return dists;
	}

	/**
	 * Finds the median among strictly positive distances for zero-inflated / tied samples.
	 */
	private static double findFirstPositiveMedian(final double[] sortedDists) {
		for (int k = 0; k < sortedDists.length; k++) {
			if (sortedDists[k] > 0.0) return medianSlice(sortedDists, k, sortedDists.length);
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

	/**
	 * Sets diagonal elements of a square matrix to 0.
	 */
	private static void zeroDiagonal(final double[][] matrix, final int n) {
		for (int i = 0; i < n; i++) { matrix[i][i] = 0.0; }
	}

	/**
	 * Validates that input arrays are non-null and have identical lengths.
	 */
	private static void validateInputs(final double[] x, final double[] y) {
		if (x == null || y == null)
			throw new IllegalArgumentException("Input arrays must be non-null");
		if (x.length != y.length)
			throw new IllegalArgumentException("Input arrays must have the same length");
	}

	/**
	 * Validates that input arrays are non-null, equal-length, and contain only finite numbers.
	 */
	private static void validateFiniteInputs(final double[] x, final double[] y) {
		validateInputs(x, y);
		for (int i = 0; i < x.length; i++) {
			if (!Double.isFinite(x[i]) || !Double.isFinite(y[i]))
				throw new IllegalArgumentException("Input arrays must contain finite values");
		}
	}

	/**
	 * Validates preconditions for the permutation p-value test.
	 */
	private static void validatePValuePreconditions(final IScope scope, final double[] x, final double[] y,
			final int permutations) {
		if (permutations <= 0)
			throw GamaRuntimeException.error("hsic_p_value requires a positive permutation count", scope);
		if (x == null || y == null || x.length != y.length || x.length < 2)
			throw GamaRuntimeException.error(
					"hsic_p_value requires equally sized arrays of at least two observations", scope);
		for (int i = 0; i < x.length; i++) {
			if (!Double.isFinite(x[i]) || !Double.isFinite(y[i]))
				throw GamaRuntimeException.error("hsic_p_value requires finite observations", scope);
		}
		checkInterrupted(scope);
	}

	/**
	 * Checks if the current simulation scope was cancelled or interrupted.
	 */
	private static void checkInterrupted(final IScope scope) {
		if (scope != null && scope.interrupted())
			throw GamaRuntimeException.error("hsic_p_value computation was interrupted", scope);
	}

	/**
	 * Obtains the random generator from scope or defaults to a new RandomUtils instance.
	 */
	private static IRandom resolveRandom(final IScope scope) {
		return scope == null || scope.getRandom() == null ? new RandomUtils() : scope.getRandom();
	}
}
