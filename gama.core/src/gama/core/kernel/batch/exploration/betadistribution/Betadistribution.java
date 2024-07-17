package gama.core.kernel.batch.exploration.betadistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;

public class Betadistribution {
	
	double objMin = Double.MAX_VALUE; double objMax = Double.MIN_VALUE;
	double[] empiricalCDFGranularity;
	
	/** The parameters */
	List<Batch> parameters;
	
	/** The res outputs. */
	/* All the outputs for each simulation */
	Map<ParametersSet,List<Double>> sample;
	final EmpiricalDistribution Y;
	
	/**
	 * Build the empirical distribution of results based on an experimental plan 
	 * @param sample: the experimental plan and results
	 * @param inputs: the input parameters
	 */
	public Betadistribution(IMap<ParametersSet,List<Object>> sample, List<Batch> inputs) { 
		this(sample,inputs,100); 
	}
	
	/**
	 * Build the empirical distribution (with user defined granularity - discretisation of distribution) of results based on an experimental plan
	 * @param sample
	 * @param inputs
	 * @param granularity
	 * @param obj
	 */
	public Betadistribution(IMap<ParametersSet,List<Object>> sample, List<Batch> inputs, int granularity) {
		this.sample = new HashMap<>();
		for (var entry : sample.entrySet()) { 
			this.sample.put(entry.getKey(), entry.getValue().stream().mapToDouble(v -> Double.parseDouble(v.toString())).boxed().toList());
			double min = Collections.min(this.sample.get(entry.getKey()));
			double max = Collections.max(this.sample.get(entry.getKey()));
			if (min < this.objMin) {this.objMin=min;}
			if (max > this.objMax) {this.objMax=max;}
		}
		this.parameters = inputs;
		this.empiricalCDFGranularity = granularity(granularity,this.objMin,this.objMax);
		this.Y = get_empirical_distribution(this.sample.values().stream().flatMap(List::stream).toList());
		
	}
	
	/**
	 * Main method that evaluate a set of simulations and propose a beta d for a single output
	 * @return : a mapping of each parameter @Batch with beta d statistic @Double
	 */
	public Map<Batch,Double> evaluate() {
		Map<Batch,Double> betadKu = new HashMap<>();
		
		// Over each input parameter compute beta
		for (Batch p : parameters) {
			// For each value of 'theta' find the conditional 'y'
			Map<Object,List<Double>> conditional_in_out = new HashMap<>();
			for (ParametersSet ps : this.sample.keySet()) {
				Object o = ps.get(p.getName());
				if (!conditional_in_out.containsKey(o)) { conditional_in_out.put(o, new ArrayList<>()); }
				conditional_in_out.get(o).addAll(sample.get(ps));
			}
			List<Double> betas = new ArrayList<>();
			
			// find the Kuiper distance DeltaP_t and DeltaP_s, to make the sum, for each 'theta_i' and store them
			for (List<Double> e : conditional_in_out.values()) {
				EmpiricalDistribution ed = get_empirical_distribution(e);
				List<Double> deltas = IntStream.range(0, Y.length()).mapToDouble(i -> ed.cdf(i) - Y.cdf(i)).boxed().toList();
				betas.add(Collections.max(deltas)+Math.abs(Collections.min(deltas)));
			}

			// compute expectancy - no differences in weight, then just the average - of all the betaKu to obtain betadKu
			betadKu.put(p, betas.stream().mapToDouble(Double::doubleValue).average().getAsDouble());
		}
		
		return betadKu;
	}
	 
	/*
	 * Get the distribution (cdf) from a sample
	 */
	EmpiricalDistribution get_empirical_distribution(List<Double> as) {
		 
		double[] prob = new double[this.empiricalCDFGranularity.length+1];
		
		for (int i = 0; i < prob.length; i++) {
			final int idx = i;
			if (i==0) {
				prob[i] = as.stream().filter(v -> v.doubleValue() < empiricalCDFGranularity[idx]).count() * 1d / as.size(); 
			} else if (i==prob.length-1) {
				prob[i] = as.stream().filter(v -> v.doubleValue() >= empiricalCDFGranularity[idx-1]).count() * 1d / as.size();
			} else {
				prob[i] = as.stream().filter(v -> v.doubleValue() >= empiricalCDFGranularity[idx-1] && 
						v.doubleValue() < empiricalCDFGranularity[idx]).count() * 1d / as.size();
			}
		}

		return new EmpiricalDistribution(prob);
	}
	
	// ----- UTILS ----- //
	
	private double[] granularity(int bins, double min, double max) {
		double[] res = new double[bins-1];
		double incr = (max-min)/bins;
		res[0] = min + incr;
		for (int i = 1; i < bins-1; i++) { res[i] = res[i-1] + incr; }
		if (Math.ulp(res[bins-2]+incr) > Math.ulp(max)) { 
			throw GamaRuntimeException.error("The bins does not fit max val: "+(res[bins-2]+incr)
				+" is not the maximum expected value "+max+" (diff = "+Math.abs(res[bins-2]+incr - max)+")", null);
		}
		return res;
	}
	
	/*
	 * Empirical distribution based on the implementation of smile API:
	 * https://github.com/haifengl/smile/blob/master/base/src/main/java/smile/stat/distribution/EmpiricalDistribution.java
	 * 
	 * For the problematic of assessing acceptable error for probability to sum to 1:
	 * https://stackoverflow.com/questions/54003108/what-is-a-suitable-tolerance-for-expecting-n-floats-to-sum-to-1
	 */
	public class EmpiricalDistribution {
		double[] p;
		private final double[] cdf;
		
		public EmpiricalDistribution(double[] prob) {
			p = new double[prob.length];
	        cdf = new double[prob.length];
			cdf[0] = prob[0];
			for (int i = 0; i < prob.length; i++) {
	            if (prob[i] < 0 || prob[i] > 1) {
	                throw new IllegalArgumentException("Invalid probability " + p[i]);
	            }

	            p[i] = prob[i];

	            if (i > 0) {
	                cdf[i] = cdf[i - 1] + p[i];
	            }

	        }

	        if (Math.abs(cdf[cdf.length - 1] - 1.0f) > (prob.length-1) * 0.5 * Math.ulp(0.5f)) {
	            throw new IllegalArgumentException("The sum of probabilities is not exactly 1: "+cdf[cdf.length - 1]);
	        }
		}
		
	    public int length() { return p.length; }
	    public double cdf(int k) { return cdf[k]; } 
	}
	
}
