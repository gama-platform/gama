package gama.core.experiment.batch.exploration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gama.api.gaml.symbols.IParameter.Batch;
import gama.api.runtime.scope.IScope;
import gama.core.experiment.parameters.ParametersSet;

/**
 * This class creates a Latin Hypercube Sampling. Optimized with a robust ESE-inspired algorithm (Jin et al. 2003)
 * using a Maximin-distance penalty criterion.
 *
 * @author Paul-Saves
 */
public class LatinhypercubeSampling extends SamplingUtils {

	/** The default penalty factor for the Phi_p criterion (p=50) */
	private static final int P = 50;
	/** The default number of outer iterations */
	private static final int DEFAULT_OUTER_ITERS = 50;
	/** The default number of inner iterations */
	private static final int DEFAULT_INNER_ITERS = 100;
	/** Precomputed exponent for the distance penalty calculation (-p/2.0) */
	private static final double EXPONENT = -P / 2.0;
	/** Small epsilon to prevent numerical instability (NaN/Infinity) when points are extremely close */
	private static final double EPS = 1e-12;
	/** The initial temperature ratio relative to the objective value (0.5% acceptance threshold) */
	private static final double INITIAL_TEMP_RATIO = 0.005;

	/**
	 * Building Latin Hypercube samples using ESE optimization.
	 *
	 * @param n
	 *            Number of samples
	 * @param parameters
	 *            List of parameters
	 * @param r
	 *            Random number generator
	 * @param scope
	 *            Execution scope
	 * @return A list of parameter sets
	 */
	public static List<ParametersSet> latinHypercubeSamples(final int n, final List<Batch> parameters, final Random r,
			final IScope scope) {
		return latinHypercubeSamples(n, parameters, r, scope, DEFAULT_OUTER_ITERS, DEFAULT_INNER_ITERS);
	}

	/**
	 * Building Latin Hypercube samples using ESE optimization with specific iterations.
	 *
	 * @param n
	 *            Number of samples
	 * @param parameters
	 *            List of parameters
	 * @param r
	 *            Random number generator
	 * @param scope
	 *            Execution scope
	 * @param outerIter
	 *            Number of outer iterations
	 * @param innerIter
	 *            Number of inner iterations
	 * @return A list of parameter sets
	 */
	public static List<ParametersSet> latinHypercubeSamples(final int n, final List<Batch> parameters, final Random r,
			final IScope scope, final int outerIter, final int innerIter) {
		int d = parameters.size();
		if (d == 0 || n == 0) return new ArrayList<>();

		// Initial design (centered LHS: one point per interval per dimension)
		double[][] matrix = generateCenteredLHS(n, d, r);

		// Precompute squared distance matrix for performance
		double[][] distSq = computeDistanceMatrix(matrix, n, d);

		double currentJ = calculateJ(distSq, n);
		double bestJ = currentJ;
		double[][] bestMatrix = copyMatrix(matrix);

		// Initial temperature calibrated to the actual objective scale
		double t = INITIAL_TEMP_RATIO * currentJ;

		for (int o = 0; o < outerIter; o++) {
			int acceptances = 0;
			for (int i = 0; i < innerIter; i++) {
				// Cyclic column selection ensures all dimensions are explored uniformly
				int col = i % d;
				int row1 = r.nextInt(n);
				int row2 = r.nextInt(n);
				while (row1 == row2) { row2 = r.nextInt(n); }

				double v1 = matrix[row1][col];
				double v2 = matrix[row2][col];

				// Efficient delta calculation for the J penalty (sum of d^-p)
				double oldContr = 0;
				double newContr = 0;
				for (int l = 0; l < n; l++) {
					if (l == row1 || l == row2) continue;
					double d1Old = distSq[row1][l];
					double d2Old = distSq[row2][l];
					oldContr += Math.pow(d1Old + EPS, EXPONENT) + Math.pow(d2Old + EPS, EXPONENT);

					double d1New = d1Old - Math.pow(v1 - matrix[l][col], 2) + Math.pow(v2 - matrix[l][col], 2);
					double d2New = d2Old - Math.pow(v2 - matrix[l][col], 2) + Math.pow(v1 - matrix[l][col], 2);
					newContr += Math.pow(d1New + EPS, EXPONENT) + Math.pow(d2New + EPS, EXPONENT);
				}

				double newJ = currentJ - oldContr + newContr;

				// Metropolis criterion: Align acceptance scale with the objective scale (J)
				if (newJ < currentJ || r.nextDouble() < Math.exp((currentJ - newJ) / t)) {
					matrix[row1][col] = v2;
					matrix[row2][col] = v1;
					updateDistances(distSq, matrix, row1, row2, col, n);
					currentJ = newJ;
					acceptances++;
					if (currentJ < bestJ) {
						bestJ = currentJ;
						bestMatrix = copyMatrix(matrix);
					}
				}
			}

			// Adaptive Temperature Schedule: Balance exploration vs. exploitation
			if (acceptances > 0.8 * innerIter) {
				t *= 0.8;
			} else if (acceptances < 0.1 * innerIter) {
				t *= 1.2;
			} else {
				t *= 0.95;
			}
		}

		return matrixToParameterSets(bestMatrix, parameters, scope);
	}

	/**
	 * Generate a centered Latin Hypercube Sampling matrix.
	 */
	private static double[][] generateCenteredLHS(final int n, final int d, final Random r) {
		double[][] matrix = new double[n][d];
		List<Integer> list = IntStream.range(0, n).boxed().collect(Collectors.toList());
		for (int j = 0; j < d; j++) {
			Collections.shuffle(list, r);
			for (int i = 0; i < n; i++) { matrix[i][j] = (list.get(i) + 0.5) / n; }
		}
		return matrix;
	}

	/**
	 * Calculate the J criterion (sum of d^-p).
	 */
	private static double calculateJ(final double[][] distSq, final int n) {
		double sum = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) { sum += Math.pow(distSq[i][j] + EPS, EXPONENT); }
		}
		return sum;
	}

	/**
	 * Efficiently update the distance matrix after a swap in one column.
	 */
	private static void updateDistances(final double[][] distSq, final double[][] matrix, final int row1, final int row2,
			final int col, final int n) {
		double newVal1 = matrix[row1][col];
		double newVal2 = matrix[row2][col];
		for (int l = 0; l < n; l++) {
			if (l == row1 || l == row2) continue;
			double oldDist1 = distSq[row1][l];
			double oldDist2 = distSq[row2][l];

			double newDist1 = oldDist1 - Math.pow(newVal2 - matrix[l][col], 2) + Math.pow(newVal1 - matrix[l][col], 2);
			double newDist2 = oldDist2 - Math.pow(newVal1 - matrix[l][col], 2) + Math.pow(newVal2 - matrix[l][col], 2);

			distSq[row1][l] = distSq[l][row1] = newDist1;
			distSq[row2][l] = distSq[l][row2] = newDist2;
		}
	}

	/**
	 * Compute the initial squared distance matrix.
	 */
	private static double[][] computeDistanceMatrix(final double[][] matrix, final int n, final int d) {
		double[][] distSq = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				double d2 = 0;
				for (int k = 0; k < d; k++) {
					double diff = matrix[i][k] - matrix[j][k];
					d2 += diff * diff;
				}
				distSq[i][j] = distSq[j][i] = d2;
			}
		}
		return distSq;
	}

	/**
	 * Copy a matrix.
	 */
	private static double[][] copyMatrix(final double[][] src) {
		double[][] dest = new double[src.length][src[0].length];
		for (int i = 0; i < src.length; i++) { System.arraycopy(src[i], 0, dest[i], 0, src[i].length); }
		return dest;
	}

	/**
	 * Convert the matrix to parameter sets.
	 */
	private static List<ParametersSet> matrixToParameterSets(final double[][] matrix, final List<Batch> parameters,
			final IScope scope) {
		int n = matrix.length;
		int d = matrix[0].length;
		List<String> names = parameters.stream().map(Batch::getName).collect(Collectors.toList());

		List<Map<String, Double>> sampling = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Map<String, Double> map = new LinkedHashMap<>();
			for (int j = 0; j < d; j++) { map.put(names.get(j), matrix[i][j]); }
			sampling.add(map);
		}
		return buildParametersSetfromSample(scope, parameters, sampling);
	}
}
