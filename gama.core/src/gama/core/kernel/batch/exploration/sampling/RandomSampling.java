package gama.core.kernel.batch.exploration.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.runtime.IScope;

/**
 * A sampling method from a given parameter space with uniformally chosen points
 * 
 * @author kevinchapuis
 *
 */
public class RandomSampling extends SamplingUtils {
	
	/**
	 * Construct a list of uniformally drawn points from a n dimensional space (parameter space)
	 * 
	 * @param scope
	 * @param sample
	 * @param parameters
	 * @return
	 */
	public static List<ParametersSet> uniformSampling(IScope scope, int sample, List<Batch> parameters){
		
		List<Map<String,Double>> rSample = new ArrayList<>();
		int i = 0;
		while (i++ < sample) {
			Map<String,Double> point = new HashMap<>();
			for (Batch p : parameters) { point.put(p.getName(), scope.getRandom().next()); }
			rSample.add(point);
		}
		
        return buildParametersSetfromSample(scope,parameters,rSample);
	}
	
	/**
	 * Construct a list of factorial point uniformally drawn in a n dimensional space
	 * 
	 * @param scope
	 * @param samples : number of values to draw for each dimensions
	 * @param parameters : the list of dimensions
	 * @return
	 */
	public static List<ParametersSet> factorialUniformSampling(IScope scope, int[] samples, List<Batch> parameters){
		
		Map<Batch,List<Double>> facorial = new HashMap<>();
		for(Batch p : parameters) {
			facorial.put(p, 
				IntStream.range(0, samples[parameters.indexOf(p)]).mapToDouble(o -> scope.getRandom().next()).boxed().toList() 
			); 
		}
		
		List<Map<String,Double>> rSample = buildFactorialDesign(parameters,facorial,new ArrayList<>(),0);
		
        return buildParametersSetfromSample(scope,parameters,rSample);
	}
	
	/**
	 * Construct a list (size approximating sample size) of factorial point uniformally drawn in a n dimensional space
	 * 
	 * @param scope
	 * @param samples
	 * @param parameters
	 * @return
	 */
	public static List<ParametersSet> factorialUniformSampling(IScope scope, int samples, List<Batch> parameters){
		int f = (int) Math.round(Math.pow(samples, 1d / parameters.size()));
		f = f < 1 ? 1 : f;
		int[] factor = new int[parameters.size()];
		Arrays.fill(factor, f);
		return factorialUniformSampling(scope, factor, parameters);
	}
	
	/**
	 * Factorial design with cartesian product of linked sets
	 * 
	 * @param parameters
	 * @param vals
	 * @param sample
	 * @param index
	 * @return
	 */
	private static List<Map<String,Double>> buildFactorialDesign(
			final List<Batch> parameters, Map<Batch,List<Double>> vals, 
			final List<Map<String,Double>> sample, final int index) {
		List<Map<String,Double>> sample2 = new ArrayList<>();

		if (sample.isEmpty()) { sample.add(new HashMap<>()); }

		final IParameter.Batch var = parameters.get(index);
		for (Map<String,Double> solution : sample) {
			for (final Double val : vals.get(var)) {
				Map<String,Double> ps = new HashMap<>(solution);
				ps.put(var.getName(), val);
				sample2.add(ps);
			}
		}
		if (index == parameters.size() - 1) return sample2;
		return buildFactorialDesign(parameters, vals, sample2, index + 1);
	}
	
}
