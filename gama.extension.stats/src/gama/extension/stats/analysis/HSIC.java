/*******************************************************************************************************
 *
 * HSIC.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats.analysis;

import java.util.Arrays;

/**
 * Hilbert-Schmidt Independence Criterion (HSIC) implementation.
 * HSIC is a kernel-based statistic to test the independence of two variables.
 */
public class HSIC {

	/**
	 * Computes the normalized HSIC value (R2-HSIC) for two sets of observations.
	 * This value is bounded between 0 and 1, making it comparable across variables.
	 * 
	 * @param x observations of the first variable
	 * @param y observations of the second variable
	 * @return the normalized HSIC value
	 */
	public static double computeNormalizedHSIC(double[] x, double[] y) {
		double hsicXY = computeHSIC(x, y, true); // Use unbiased estimator
		double hsicXX = computeHSIC(x, x, true);
		double hsicYY = computeHSIC(y, y, true);
		
		double den = Math.sqrt(Math.max(0, hsicXX) * Math.max(0, hsicYY));
		if (den == 0) return 0.0;
		return Math.max(0, hsicXY) / den;
	}

	/**
	 * Computes the HSIC value for two sets of observations.
	 * 
	 * @param x observations of the first variable
	 * @param y observations of the second variable
	 * @param unbiased if true, uses the unbiased estimator (can be negative)
	 * @return the HSIC value
	 */
	public static double computeHSIC(double[] x, double[] y, boolean unbiased) {
		int n = x.length;
		if (n != y.length) {
			throw new IllegalArgumentException("Input arrays must have the same length");
		}
		if (n < (unbiased ? 4 : 2)) return 0.0;

		double sigmaX = estimateBandwidth(x);
		double sigmaY = estimateBandwidth(y);

		double[][] K = computeGramMatrix(x, sigmaX);
		double[][] L = computeGramMatrix(y, sigmaY);

		if (unbiased) {
			// Unbiased estimator formula according to Song et al. (2007)
			// HSIC = 1/(n(n-3)) * [ trace(KL) + sum(K)sum(L)/( (n-1)(n-2) ) - 2/(n-2) sum(K*L_rows) ]
			// with diagonal elements of K and L set to 0.
			for (int i = 0; i < n; i++) {
				K[i][i] = 0;
				L[i][i] = 0;
			}
			
			double traceKL = 0;
			double sumK = 0;
			double sumL = 0;
			double sumKLrows = 0;
			
			double[] rowSumsK = new double[n];
			double[] rowSumsL = new double[n];
			
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					traceKL += K[i][j] * L[j][i];
					rowSumsK[i] += K[i][j];
					rowSumsL[i] += L[i][j];
				}
				sumK += rowSumsK[i];
				sumL += rowSumsL[i];
			}
			
			for (int i = 0; i < n; i++) {
				sumKLrows += rowSumsK[i] * rowSumsL[i];
			}
			
			return (traceKL + (sumK * sumL) / ((n - 1.0) * (n - 2.0)) - 2.0 * sumKLrows / (n - 2.0)) / (n * (n - 3.0));
		} else {
			// Biased (empirical) estimator
			double[][] Kc = centerMatrix(K);
			double[][] Lc = centerMatrix(L);
			double trace = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					trace += Kc[i][j] * Lc[j][i];
				}
			}
			return trace / Math.pow(n - 1, 2);
		}
	}

	/**
	 * Computes the p-value for the HSIC test using a permutation test.
	 * 
	 * @param x observations of the first variable
	 * @param y observations of the second variable
	 * @param permutations number of permutations
	 * @return the p-value
	 */
	public static double computePValue(double[] x, double[] y, int permutations) {
		double observedHSIC = computeHSIC(x, y, false); // Use biased for p-value stability
		int count = 0;
		double[] yPerm = y.clone();
		java.util.Random rnd = new java.util.Random();

		for (int p = 0; p < permutations; p++) {
			// Shuffle yPerm (Fisher-Yates)
			for (int i = yPerm.length - 1; i > 0; i--) {
				int index = rnd.nextInt(i + 1);
				double temp = yPerm[index];
				yPerm[index] = yPerm[i];
				yPerm[i] = temp;
			}
			if (computeHSIC(x, yPerm, false) >= observedHSIC) {
				count++;
			}
		}
		return (double) (count + 1) / (permutations + 1);
	}

	private static double[][] computeGramMatrix(double[] data, double sigma) {
		int n = data.length;
		double[][] gram = new double[n][n];
		double gamma = 1.0 / (2.0 * sigma * sigma);
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				double distSq = Math.pow(data[i] - data[j], 2);
				double val = Math.exp(-gamma * distSq);
				gram[i][j] = val;
				gram[j][i] = val;
			}
		}
		return gram;
	}

	private static double[][] centerMatrix(double[][] matrix) {
		int n = matrix.length;
		double[] rowMeans = new double[n];
		double totalMean = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				rowMeans[i] += matrix[i][j];
			}
			totalMean += rowMeans[i];
			rowMeans[i] /= n;
		}
		totalMean /= (n * n);

		double[][] centered = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				centered[i][j] = matrix[i][j] - rowMeans[i] - rowMeans[j] + totalMean;
			}
		}
		return centered;
	}

	/**
	 * Median heuristic for bandwidth estimation.
	 */
	private static double estimateBandwidth(double[] data) {
		int n = data.length;
		if (n < 2) return 1.0;
		int size = n * (n - 1) / 2;
		double[] dists = new double[size];
		int count = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				double d = Math.abs(data[i] - data[j]);
				dists[count++] = d * d; // Use squared distance for median heuristic in RBF
			}
		}
		Arrays.sort(dists);
		double medianSq = dists[size / 2];
		return medianSq == 0 ? 1.0 : Math.sqrt(medianSq);
	}
}
