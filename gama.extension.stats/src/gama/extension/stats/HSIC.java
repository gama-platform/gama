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
package gama.extension.stats;

import java.util.Arrays;

/**
 * Hilbert-Schmidt Independence Criterion (HSIC) implementation.
 * HSIC is a kernel-based statistic to test the independence of two variables.
 */
public class HSIC {

	/**
	 * Computes the HSIC value for two sets of observations.
	 * 
	 * @param x observations of the first variable
	 * @param y observations of the second variable
	 * @return the HSIC value
	 */
	public static double computeHSIC(double[] x, double[] y) {
		int n = x.length;
		if (n != y.length) {
			throw new IllegalArgumentException("Input arrays must have the same length");
		}
		if (n < 2) return 0.0;

		// 1. Compute Gram matrices K and L using RBF kernel
		double sigmaX = estimateBandwidth(x);
		double sigmaY = estimateBandwidth(y);

		double[][] K = computeGramMatrix(x, sigmaX);
		double[][] L = computeGramMatrix(y, sigmaY);

		// 2. Centering matrix H = I - (1/n) * 11^T
		// HSIC = (1/(n-1)^2) * trace(K H L H)
		// More efficiently: HSIC = (1/(n-1)^2) * [ trace(KL) + (1/n^2)sum(K)sum(L) - (2/n)sum(K*L_rows) ]
		// But for simplicity and clarity, we use the trace(Kc Lc) where Kc and Lc are centered Gram matrices.
		
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

	/**
	 * Computes the p-value for the HSIC test using a permutation test.
	 * 
	 * @param x observations of the first variable
	 * @param y observations of the second variable
	 * @param permutations number of permutations
	 * @return the p-value
	 */
	public static double computePValue(double[] x, double[] y, int permutations) {
		double observedHSIC = computeHSIC(x, y);
		int count = 0;
		double[] yPerm = y.clone();
		java.util.Random rnd = new java.util.Random();

		for (int p = 0; p < permutations; p++) {
			// Shuffle yPerm
			for (int i = yPerm.length - 1; i > 0; i--) {
				int index = rnd.nextInt(i + 1);
				double temp = yPerm[index];
				yPerm[index] = yPerm[i];
				yPerm[i] = temp;
			}
			if (computeHSIC(x, yPerm) >= observedHSIC) {
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
				dists[count++] = Math.abs(data[i] - data[j]);
			}
		}
		Arrays.sort(dists);
		double median = dists[size / 2];
		return median == 0 ? 1.0 : median;
	}
}
