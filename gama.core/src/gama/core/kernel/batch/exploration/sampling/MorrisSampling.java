package gama.core.kernel.batch.exploration.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 *
 * @author tomroy
 *
 */

/**
 *
 * This class make a Morris Sampling for a Morris analysis
 *
 */
public class MorrisSampling extends SamplingUtils {
	
	public static class Trajectory {
		List<List<Double>> points;

		/**
		 * Build a trajectory.
		 * @param points
		 *            : Points that the trajectory visit.
		 */
		public Trajectory(final List<List<Double>> points) {
			this.points = points;
		}
	}
	
	/* Retro-engine SAlib Morris sampling */
	

	/**
	 * Main method for Morris samples, give the samples with List<ParametersSet> and List<Map<String,Object>>
	 *
	 * @param nb_levels
	 *            the number of levels (Should be even, frequently 4)
	 * @param nb_sample
	 *            the number of sample for each parameter. Add the end, the number of simulation is nb_sample *
	 *            nb_parameters
	 * @param parameters
	 *            Parameters of the model. 
	 * @param scope
	 * @return samples for simulations. Size: nb_sample * inputs.size()
	 */
	public static List<Object> makeMorrisSampling(final int nb_levels, final int nb_sample,
			final List<Batch> parameters, final IScope scope) {
		if (nb_levels % 2 != 0) throw GamaRuntimeException.error("The number of value should be even", scope);
		int nb_attributes = parameters.size();
		
		List<Trajectory> trajectories = new ArrayList<>();
		trajectories = morrisTrajectories(nb_attributes, nb_levels, nb_sample, scope.getRandom().getGenerator(), trajectories);
		
		List<String> nameInputs = new ArrayList<>();
		for (int i = 0; i < parameters.size(); i++) { nameInputs.add(parameters.get(i).getName()); }
		List<Map<String, Double>> MorrisSamples = new ArrayList<>();
		trajectories.forEach(t -> {
			t.points.forEach(p -> {
				Map<String, Double> tmpMap = new LinkedHashMap<>();
				IntStream.range(0, nb_attributes).forEach(i -> { tmpMap.put(nameInputs.get(i), p.get(i)); });
				MorrisSamples.add(tmpMap);
			});

		});
		List<Object> result=new ArrayList<>();
		result.add(MorrisSamples);
		result.add(buildParametersSetfromSample(scope, parameters, MorrisSamples));
		return result;
	}
	
	/**
	 * Same as above but only give the sampling with a list of parameter set
	 * @param nb_levels
	 * 				the number of levels (Should be even, frequently 4)
	 * @param nb_sample
	 *            the number of sample for each parameter. Add the end, the number of simulation is nb_sample *
	 *            nb_parameters
	 * @param parameters
	 *            Parameters of the model. 
	 * @param scope
	 * @return
	 */
	public static List<ParametersSet> makeMorrisSamplingOnly(final int nb_levels, final int nb_sample,
			final List<Batch> parameters, final IScope scope) {
		if (nb_levels % 2 != 0) throw GamaRuntimeException.error("The number of value should be even", scope);
		int nb_attributes = parameters.size();
		List<Trajectory> trajectories = new ArrayList<>();
		trajectories = morrisTrajectories(nb_attributes, nb_levels, nb_sample, scope.getRandom().getGenerator(), trajectories);
		List<String> nameInputs = new ArrayList<>();
		for (int i = 0; i < parameters.size(); i++) { nameInputs.add(parameters.get(i).getName()); }
		List<Map<String, Double>> MorrisSamples = new ArrayList<>();
		trajectories.forEach(t -> {
			t.points.forEach(p -> {
				Map<String, Double> tmpMap = new LinkedHashMap<>();
				IntStream.range(0, nb_attributes).forEach(i -> { tmpMap.put(nameInputs.get(i), p.get(i)); });
				MorrisSamples.add(tmpMap);
			});
		});
		return buildParametersSetfromSample(scope, parameters, MorrisSamples);
	}
	
	/**
	 * Recursively generates r independent trajectories for k variables sampled with p levels.
	 * 
	 * No specific strategy is used to sample trajectories - uniformely random
	 */
	private static List<Trajectory> morrisTrajectories(final int k, final int p, final int r, final Random rng, final List<Trajectory> acc) {
		if (r == 0) return acc;
		acc.add(generateTraj(k, p, rng));
		return morrisTrajectories(k, p, r - 1, rng, acc);
	}

	
	/*
	 * Create one random trajectory following classical random strategy:
	 * 
	 * https://gsa-module.readthedocs.io/en/stable/implementation/morris_screening_method.html
	 * 
	 * b∗ = (x∗ + Δ2 × ((2×b−j_k) × d∗ + j_k)) × p∗
	 * 
	 */
	private static Trajectory generateTraj(final int k, final int l, final Random rng) {
		double delta = l / (2.0 * (l - 1));
		
		RealMatrix g = MatrixUtils.createRealIdentityMatrix(k);
        int numParams = g.getColumnDimension();
        int numGroups = g.getRowDimension();

        RealMatrix B = MatrixUtils.createRealMatrix(numGroups+1, numGroups);
        tril(B);
        
        RealMatrix P_star = MatrixUtils.createRealMatrix(numGroups, numGroups);
        shuffleRow(P_star);
        
        RealMatrix J = ones(numGroups+1,numGroups);
        
        double[][] _Ds = new double[numParams][numParams];
        for (int i = 0; i < numParams; i++) { _Ds[i][i] = rng.nextBoolean() ? -1 : 1; }
        RealMatrix D_star = MatrixUtils.createRealMatrix(_Ds);
        
        double[] x_star = seed(numParams, delta, l, rng);
        
        // b
        RealMatrix b = g.multiply(P_star).transpose();
        // c
        RealMatrix c = B.scalarMultiply(2d).multiply(b);
        // d
        RealMatrix d = c.subtract(J).multiply(D_star);
        // EE
        RealMatrix EE = d.add(J).scalarMultiply(delta/2);
        
        // Trajectory
        RealMatrix B_star = rowise_add(x_star, EE);
        
        // Change to Gama Morris contract
        List<List<Double>> trajectories = new ArrayList<>();
        for (int i = 0; i < B_star.getRowDimension(); i++) {
        	trajectories.add(Arrays.stream(B_star.getRow(i)).boxed().collect(Collectors.toList()));
        }
        
        return new Trajectory(trajectories);
	}
	
	// ----------------- INNER UTILITIES ----------------- //
	
	// Row wise addition to a 2D matrix : must be of same column dimension
	private static RealMatrix rowise_add(double[] row, RealMatrix m) {
        for (int i = 0; i < m.getRowDimension(); i++) {
        	final int fi = i;
        	m.setRow(i, 
        			IntStream.range(0, m.getColumnDimension())
        			.mapToDouble(j -> m.getEntry(fi, j)+row[j]).toArray());
        }
        return m;
	}
	
	// Original random point in the trajectory
	private static double[] seed(int size, double delta, int levels, final Random rng) {
		double[] seed = new double[size];
        double bound = 1 - delta;
        double[] grid = new double[levels / 2];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = i * (bound / (grid.length - 1));
        }

        for (int i = 0; i < size; i++) {
            seed[i] = grid[rng.nextInt(grid.length)];
        }
        return seed;
	}
	
	// 2D Matrix of ones
	private static RealMatrix ones(int rows, int columns) {
		double[][] ones = new double[rows][columns];
        for (int i = 0; i < rows; i++) { Arrays.fill(ones[i], 1.0); }
        return MatrixUtils.createRealMatrix(ones);
	}
	
	// Lower triangulation
	private static RealMatrix tril(RealMatrix m) {
		for (int i = 1; i < m.getRowDimension(); i++) {
            for (int j = 0; j < i; j++) {
            	m.setEntry(i, j, 1);
            }
        }
		return m;
	}
	
	// Shuffles rows of the matrix
	private static RealMatrix shuffleRow(RealMatrix m) {
		List<Integer> clm = IntStream.range(0, m.getColumnDimension()).boxed().collect(Collectors.toList());
        Collections.shuffle(clm);
        for (int j = 0; j < m.getColumnDimension(); j++) {
        	m.setEntry(clm.remove(0), j, 1.0);
        }
        return m;
	}

}
