/*******************************************************************************************************
 *
 * Stochanalysis.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.stochanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.operators.Cast;

/**
 *
 * @author Tom ROY
 *
 */
/**
 *
 * This class perform a Stochastic Analysis to determinate the minimum size of repeat. This class use 3 different
 * methods: - Coefficient Variation method use a threshold - Standard Error method use a threshold (with a percent) -
 * Student law method
 *
 * This class write the result in a batch report.
 *
 */

public class Stochanalysis {

	// Statistical arbitrary indicators
	final static public String CV = "Coefficient of variation";
	final static public String SE = "Standard error";
	final static double[] STOCHThresholds = {0.05,0.01,0.001};
	
	final static public String ES = "Critical effect size";
	final static double[] FISHEREffectSize = {0.01,0.05,0.1,0.2,0.4,0.8};
	final static String[] FISHERES = {"ultra-micro","micro","small","medium","large","huge"};
	final static double[] TALPHA = {.99,.95};
	final static double[] TBETA = {.95,.80};
	
	final static public String PT = "Power test";
	
	// List of methods
	final static protected List<String> SA = List.of(CV,SE,ES,PT); 
	
	// UTILS 
	final static String SEP = ",";
	final static String RL = "\n";

	/**
	 * Build the report with result for each method and each output
	 *
	 * @param Out
	 *            : Value to print.
	 * @param scope
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	private static String buildResultMap(final Map<String, Map<ParametersSet, Map<String, List<Double>>>> Out, 
			final int nbsample, final int nbreplicates, final IScope scope) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("== STOCHASTICITY ANALYSIS ==");
		sb.append(RL);
		sb.append(Out.size()+" outputs | "+nbsample+" samples | "+nbreplicates+" max replications")
			.append(" | Thresholds: ").append(Arrays.toString(STOCHThresholds))
			.append(" | T test alpha: 0.01").append(" | T test beta: 0.05")
			.append(" | Critical effect size: ").append(Arrays.toString(FISHEREffectSize))
			.append(RL);
		sb.append("Threshold maning: threshold represent the marginal decrease of concerned statistic to decide on the number of replicates").append(RL);
		sb.append("Exemple: increase one replicates decreases standard error, if marginal decrease is under a given threshold, then select this number of replicates").append(RL);
		sb.append("Critical effect size: look at False negative (alpha) & False positive (beta) hypothesis - according to https://www.jasss.org/18/4/4.html").append(RL);
		sb.append(RL).append(RL);

		for (String outputs : Out.keySet()) {
			Map<ParametersSet, Map<String, List<Double>>> pso = Out.get(outputs);
			sb.append("## Output : ");
			sb.append(outputs);
			sb.append(RL);
			
			for (String method : SA) {
				if (pso.values().stream().noneMatch(m -> m.containsKey(method))) { continue; }
				
				switch (method) {
				case CV, SE -> {
					// Compute all given nb of replicate required to accept a threshold hypothesis
					IMap<Double, List<Integer>> res = GamaMapFactory.create();
					for (Double thresh : STOCHThresholds) {
						List<Integer> lres = new ArrayList<>();
						for(ParametersSet ps : pso.keySet()) { lres.add(FindWithRelativeThreshold(pso.get(ps).get(method), thresh)); }
						res.put(thresh, lres);
					}
					sb.append(method).append(RL);
					for (Double threshold : STOCHThresholds) {
						sb.append(threshold).append(" : ");
						sb.append("min = ").append(Collections.min(res.get(threshold))).append(" | ");
						sb.append("max = ").append(Collections.max(res.get(threshold))).append(" | ");
						sb.append("avr = ").append(Math.round(res.get(threshold).stream().mapToInt(i -> i).average().getAsDouble())).append(RL);
					}
				}
				case ES -> {
					IMap<Double, List<List<Integer>>> res = GamaMapFactory.create();
					for (Double es : FISHEREffectSize) {
						int idx = DoubleStream.of(FISHEREffectSize).boxed().toList().indexOf(es) * 2;
						List<List<Integer>> ab = new ArrayList<>();
						// High alpha/beta
						ab.add(pso.values().stream().mapToInt(r -> r.get(ES).get(idx).intValue()).boxed().toList());
						// Low alpha/beta
						ab.add(pso.values().stream().mapToInt(r -> r.get(ES).get(idx+1).intValue()).boxed().toList());
						res.put(es, ab);
					}
					
					List<Double> pt = pso.values().stream().filter(m -> m.containsKey(PT)).findFirst().get().get(PT);
					if (pt.isEmpty() || pt.get(0).isNaN() || pt.size() != 2) {throw GamaRuntimeException.error("Trying to retriev Power Test n estimates but failed to find results: ["+pt.stream().map(d->d.toString()).collect(Collectors.joining(","))+"]", scope);}
					
					sb.append(method).append(RL);
					sb.append(PT).append(" with  alpha=0.01 (0.05), beta=0.05 (0.2) and effect sized based on (ANOVA) f="+pt.get(1)+", theoretical number of replicate is ").append(pt.get(0)).append(RL);
					for (int i = 0; i < FISHEREffectSize.length; i++) {
						
						if (res.get(FISHEREffectSize[i]).stream().flatMap(List::stream).distinct().count() == 1) {
							sb.append(FISHERES[i]).append(" (").append(FISHEREffectSize[i])
								.append(") : "+res.get(FISHEREffectSize[i]).get(0).get(0)+" replicates is not enough")
								.append(RL);
						} else {
							sb.append(FISHERES[i]).append(" (").append(FISHEREffectSize[i]).append(") : ");
							sb.append("min = ").append(Collections.min(res.get(FISHEREffectSize[i]).get(0)))
								.append(" (").append(Collections.min(res.get(FISHEREffectSize[i]).get(1))).append(") | ");
							sb.append("max = ").append(Collections.max(res.get(FISHEREffectSize[i]).get(0)))
								.append(" (").append(Collections.max(res.get(FISHEREffectSize[i]).get(1))).append(") | ");
							sb.append("avr = ").append(Math.round(res.get(FISHEREffectSize[i]).get(0).stream().mapToInt(e->e).average().getAsDouble()))
							.append(" (").append(Math.round(res.get(FISHEREffectSize[i]).get(1).stream().mapToInt(e->e).average().getAsDouble())).append(")"+RL);
						}
					}
				}
				case PT -> { /* Have been put in the Critical Effect Size analysis */ }
				default -> throw new IllegalArgumentException("Unexpected stochastic analysis: " + method);
				}
				
			}
			sb.append(RL).append(RL);
		}
		return sb.toString();

	}

	/**
	 * 
	 * @param Out
	 * @param nbsample
	 * @param nbreplicates
	 * @param scope
	 * @return
	 */
	private static String buildStochMap(final Map<String, Map<ParametersSet, Map<String, List<Double>>>> Out, 
			final int nbsample, final int nbreplicates, final IScope scope) {
		StringBuilder sb = new StringBuilder();
		// Header of the csv
		sb.append("Outputs").append(SEP);
		
		// Parameters
		IList<String> ph = Out.get(Out.keySet().stream().findAny().get()).keySet().stream().findAny().get().getKeys();
		sb.append(ph.stream().collect(Collectors.joining(","))).append(SEP);
		
		sb.append("Indicator").append(SEP);
		sb.append(IntStream.range(1,nbreplicates).boxed().map(i -> String.valueOf(i)).collect(Collectors.joining(SEP)));
		sb.append(RL);
		
		for (String o : Out.keySet()) {
			Map<ParametersSet, Map<String, List<Double>>> om = Out.get(o);
			for (ParametersSet p : om.keySet()) {
				String lineP = ph.stream().map(head -> p.get(head).toString()).collect(Collectors.joining(SEP));
				Map<String,List<Double>> cr = om.get(p);
				for (String m : cr.keySet()) {
					sb.append(o).append(SEP);
					sb.append(lineP).append(SEP);
					sb.append(m).append(SEP);
					sb.append(cr.get(m).stream().skip(1).map(d -> String.valueOf(d)).collect(Collectors.joining(SEP)));
					sb.append(RL);
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Write and tell report.
	 *
	 * @param path
	 *            the path
	 * @param Outputs
	 *            the outputs
	 * @param scope
	 *            the scope
	 */
	public static void WriteAndTellReport(final File f, final Map<String, Map<ParametersSet, Map<String, List<Double>>>> outputs,
			final int nbsample, final int nbreplicates, final IScope scope) {

		try {
			try (FileWriter fw = new FileWriter(f, false)) {
				fw.write( FileNameUtils.getExtension(f.getPath()).equalsIgnoreCase("txt") ? 
						buildResultMap(outputs, nbsample, nbreplicates, scope) : 
							buildStochMap(outputs, nbsample, nbreplicates, scope));
				
			}
		} catch (IOException e) {
			throw GamaRuntimeException.error("File " + f.toString() + " not found", scope);
		}
	}
	
	/**
	 * Rebuild simulations ouptuts to be written in a file
	 * 
	 * @param Outputs
	 * @param scope
	 * @return
	 */
	public static String buildSimulationCsv(final IMap<ParametersSet, Map<String, List<Object>>> outputs, IScope scope) {
		StringBuilder sb = new StringBuilder();
		String sep = ";";
		String linesep = "\n";
		
		// Write the header
		for (String param : outputs.keySet().stream().findFirst().get().keySet()) { sb.append(param).append(sep); }
		for (String output : outputs.anyValue(scope).keySet()) { sb.append(output).append(sep); }
		
		// Find results and append to global string
		for (ParametersSet ps : outputs.keySet()) {
			Map<String, List<Object>> res = outputs.get(ps);
			int nbr = res.values().stream().findAny().get().size();
			if (!res.values().stream().allMatch(r -> r.size()==nbr)) { 
				GamaRuntimeException.warning("Not all sample of stochastic analysis have the same number of replicates", scope); 
			}
			for (int r = 0; r < nbr; r++) {
				sb.append(linesep);
				for (Object pvalue : ps.values()) { sb.append(pvalue).append(sep); }
				for (String output : res.keySet()) { sb.append(res.get(output).get(r)).append(sep); }
			}
		}

		return sb.toString();
	}
	
	/**
	 * Write and tell row results from simulations
	 * 
	 * @param path
	 * @param Outputs
	 * @param scope
	 */
	public static void WriteAndTellResult(final File f, final IMap<ParametersSet, Map<String, List<Object>>> outputs,
			final IScope scope) {
		try (FileWriter fw = new FileWriter(f, false)) {
			fw.write(buildSimulationCsv(outputs, scope));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + f.toString() + " not found", scope);
		}
	}
	
	// ----------------------------- Inner methods

	/**
	 * Compute the mean of a List of object
	 *
	 * @param val
	 *            : List of value (data of each replicates)
	 * @param scope
	 * @return return the mean for each number of replicates
	 */
	private static List<Double> computeMean(final List<Object> val, final IScope scope) {
		List<Double> mean = new ArrayList<>();
		double tmp_mean = 0;
		for (int i = 0; i < val.size(); i++) {
			double tmp_val = Cast.asFloat(scope, val.get(i));
			tmp_mean = tmp_mean + tmp_val;
			mean.add(tmp_mean / (i + 1));
		}
		return mean;
	}


	/**
	 * Compute the Standard Deviation of a list
	 *
	 * @param mean
	 *            : the mean for each number of replicates
	 * @param val
	 *            : List of value (data of each replicates)
	 * @param scope
	 * @return return the standard deviation for each number of replicates (Always 0 for 1).
	 */
	private static List<Double> computeSTD(final List<Double> mean, final List<Object> val, final IScope scope) {
		List<Double> STD = new ArrayList<>();
		for (int i = 0; i < mean.size(); i++) {
			double sum = 0;
			for (int y = 0; y < i; y++) {
				double tmp_val = Cast.asFloat(scope, val.get(y));
				sum = sum + Math.pow(tmp_val - mean.get(i), 2);
			}
			STD.add(Math.sqrt(sum / (i + 1)));
		}
		return STD;
	}

	/**
	 * Compute the Coefficient of Variation of a list
	 *
	 * @param STD
	 *            : the Standard deviation for each number of replicates
	 * @param mean
	 *            : the mean for each number of replicates
	 * @return the coefficient of variation for each number of replicates to 2 at replicate max size
	 */
	private static List<Double> computeCV(final List<Double> STD, final List<Double> mean) {
		List<Double> CV = new ArrayList<>();
		for (int i = 1; i < mean.size(); i++) { CV.add(STD.get(i) / mean.get(i)); }
		return CV;
	}

	/**
	 * Find the minimum replicates size depending of a threshold, when CV[i]-CV[i+1] < threshold, we keep the id "i".
	 *
	 * @param CV
	 *            : the coefficient of variation for each number of replicates
	 * @return the minimum replicates size (or -1 if the threshold is not reached)
	 */
	private static int FindWithThreshold(final List<Double> CV, final double threshold) {
		boolean thresh_ok = false;
		int id_sample = 0;
		for (int i = 0; i < CV.size() - 2; i++) {
			for (int y = i + 1; y < CV.size(); y++) {
				double tmp_val = Math.abs(CV.get(i) - CV.get(y));
				if (tmp_val <= threshold && !thresh_ok) {
					thresh_ok = true;
					id_sample = (1 + i + y) / 2;
				}
			}
		}
		if (!thresh_ok) return -1;
		return id_sample;
	}
	
	/**
	 * Find the minimum replicates size depending on a threshold, 
	 * when CV[i-1] - CV[i] >= 0 and CV[i-1] - CV[i] <= min_arg(CV) * threshold, 
	 * we keep the number of replicates "i".
	 *
	 * @param Stat
	 *            : the statistic given to assess replicates effectiveness
	 * @return the minimum replicates size to reach a given threshold of marginal benefit adding a new replicates
	 */
	private static int FindWithRelativeThreshold(final List<Double> Stat, final double threshold) {
		Double th = Collections.min(Stat) * threshold;
		for (int i = 2; i < Stat.size(); i++) {
			double delta = Stat.get(i-1) - Stat.get(i);
			if (delta >= 0 &&  delta <= th) { return i; }
		}
		return Stat.size();
	}

	// -------------------------------------------------- //
	// ################# ACTUAL METHODS ################# //
	
	/**
	 * Main method for the Stochastic Analysis
	 *
	 * @param sample
	 *            : The sample with all replicates for each points with results
	 * @param threshold
	 *            : Threshold for all method, the value will allow to choose the method
	 * @param scope
	 * @return return a List with 0: The n minimum found // 1: The number of failed (if n_minimum > repeat size) //2:
	 *         the result for each point of the space
	 *         
	 * TODO : also export the raw result of stochasticity measures
	 *         
	 */
	public static IMap<ParametersSet, List<Double>> StochasticityAnalysis(final IMap<ParametersSet, List<Object>> sample,
			final String method, final IScope scope) {
		
		IMap<ParametersSet,List<Double>> res = GamaMapFactory.create();
		switch (method) {
		case CV -> {
			for (ParametersSet ps : sample.keySet()) {
				List<Object> currentXp = new ArrayList<>(sample.get(ps));
				Collections.shuffle(currentXp);
				res.put(ps, coefficientOfVariance(currentXp, scope));
			}
		}
		case SE -> {
			for (ParametersSet ps : sample.keySet()) {
				List<Object> currentXp = new ArrayList<>(sample.get(ps));
				Collections.shuffle(currentXp);
				res.put(ps, standardError(currentXp, scope));
			}
		}
		case ES -> {
			for (ParametersSet ps : sample.keySet()) {
				List<Object> currentXp = new ArrayList<>(sample.get(ps));
				Collections.shuffle(currentXp);
				res.put(ps, criticalEffectSize(currentXp, scope) );
			}
		}
		case PT -> {
			double effectSize = FTestEffectSize(sample.values(), scope);
			sample.getKeys().forEach(ps -> res.put(ps, List.of(
					powerTestEffectSize(sample.values().size(), effectSize), effectSize
					)));
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + method);
		}
		return res;
		
	}
	
	/**
	 * Return the coefficient of variance over a set of replicates
	 * 
	 * @param aSample
	 * @return
	 */
	private static List<Double> coefficientOfVariance(List<Object> aSample, final IScope scope) {
		List<Double> mean = computeMean(aSample, scope);
		List<Double> std = computeSTD(mean, aSample, scope);
		List<Double> cv = computeCV(std, mean);
		return cv;
	}
	
	/**
	 * 
	 * @param aSample
	 * @param scope
	 * @return
	 */
	private static List<Double> standardError(List<Object> aSample, final IScope scope) {
		List<Double> mean = computeMean(aSample, scope);
		List<Double> std = computeSTD(mean, aSample, scope);
		List<Double> SE = new ArrayList<>();
		for (int i = 1; i < std.size(); i++) { SE.add(std.get(i) / Math.sqrt(i + 1)); }
		return SE;
	}
	
	/**
	 * Return a list of desired number of replicates based on more permissive conditions: <\br>
	 * from ES=0.01, alpha=.99 and beta=.95 to ES=0.8, alpha=.95 and beta=.80
	 * 
	 * @param aSample
	 * @param scope
	 * @return
	 */
	private static List<Double> criticalEffectSize(List<Object> aSample, final IScope scope) {
		List<Double> ce = new ArrayList<>();
		for (double es : FISHEREffectSize) {
			for(int i = 0; i < TALPHA.length; i++) {
				ce.add( (double) ces(aSample, TALPHA[i], TBETA[i], es, scope) );
			}
		}
		return ce;
	}

	/**
	 * @see inspiration: https://www.jasss.org/18/4/4.html
	 * @see inspiration: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7745163/
	 * @see actual implementation: https://rseri.me/publication/b016/B016.pdf
	 * 
	 * @param aSample
	 * @param delta : test F value (from ANOVA),
	 * @param scope
	 * @return
	 */
	private static int ces(List<Object> aSample, double alpha, double beta, double effectSize, final IScope scope) {
		List<Double> dSample = aSample.stream().mapToDouble(v -> Cast.asFloat(scope, v)).boxed().collect(Collectors.toList());
		double mean = dSample.stream().mapToDouble(v->v).average().getAsDouble();
		effectSize *= mean;
		
		List<Double> currentES = new ArrayList<>();
		// Starting from worst case deviation
		currentES.add(dSample.stream().mapToDouble(d->d).min().getAsDouble());
		currentES.add(dSample.stream().mapToDouble(d->d).max().getAsDouble());
		dSample.removeAll(currentES);
		// Sort according to deviation from the mean
		dSample.stream().sorted((v1,v2) -> (v1==v2?0:(Math.abs(v1-mean)>Math.abs(v2-mean)?-1:1)));
		for(Double n_incr : dSample) {
			currentES.add(n_incr);
			TDistribution td = new TDistribution(currentES.size()-1);
			double thresh = 2 * Math.pow( new StandardDeviation().evaluate(currentES.stream().mapToDouble(v->v).toArray()), 2 ) 
					/ effectSize * Math.pow( td.inverseCumulativeProbability(alpha) + td.inverseCumulativeProbability(beta), 2 ); 
			if (currentES.size() >= thresh) { return currentES.size();}
		}
		return aSample.size();
	}
	
	// ########### POWER TEST
	
	/**
	 * @see https://doi.org/10.1007/s10588-016-9218-0
	 * 
	 * @param j
	 * @param es
	 * @return
	 */
	private static double powerTestEffectSize(int j, double es) {
		return 14.091 * Math.pow(j, -0.640) * Math.pow(es, -1.986);
	}
	
	// see : https://en.wikipedia.org/wiki/F-test
	private static double FTestEffectSize(Collection<List<Object>> groups, final IScope scope) {
		List<Double> groupMean = groups.stream()
				.mapToDouble(group -> group.stream()
						.mapToDouble(e -> Cast.asFloat(scope, e)).average().getAsDouble())
				.boxed().toList();
		double overallMean = groupMean.stream().mapToDouble(d->d).average().getAsDouble();
		double betweenGroupVariability =  groupMean.stream().mapToDouble(mean -> Math.pow(overallMean-mean,2)).sum() / (groups.size()-1);
		double withinGroupVariability = 0.0;
		for(Double m : groupMean) {
			withinGroupVariability += groups.stream().mapToDouble(d -> Math.pow(Cast.asFloat(scope, d)-m,2)).sum();
		}
		withinGroupVariability /= groups.stream().mapToInt(group -> group.size()).sum();
		return betweenGroupVariability / withinGroupVariability;
	}
	
	
	/*
	 * "#################################################################################################"
	 * "#################################################################################################"
	 * ############################# Method for the statistical function ################################"
	 * "#################################################################################################"
	 * "#################################################################################################"
	 */
	// Need to be tested

	/**
	 * Read simulation.
	 *
	 * @param path
	 *            the path
	 * @param idOutput
	 *            the id output
	 * @param scope
	 *            the scope
	 * @return the list
	 */
	public static List<Object> readSimulation(final String path, final int idOutput, final IScope scope) {
		List<Map<String, Object>> parameters = new ArrayList<>();
		try {
			File file = new File(path);
			try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
				String line = " ";
				String[] tempArr;
				List<String> list_name = new ArrayList<>();
				int i = 0;
				while ((line = br.readLine()) != null) {
					tempArr = line.split(",");
					for (String tempStr : tempArr) { if (i == 0) { list_name.add(tempStr); } }
					if (i > 0) {
						Map<String, Object> temp_map = new LinkedHashMap<>();
						for (int y = 0; y < tempArr.length; y++) { temp_map.put(list_name.get(y), tempArr[y]); }
						parameters.add(temp_map);
					}
					i++;
				}
			}
		} catch (IOException ioe) {
			throw GamaRuntimeException.error("File " + path + " not found", scope);
		}
		Map<String, List<Double>> new_Outputs = new LinkedHashMap<>();
		List<String> tmpNames = parameters.get(0).keySet().stream().toList();
		IntStream.range(0, parameters.size()).forEach(i -> {
			for (int y = idOutput; y < tmpNames.size(); y++) {
				List<Double> tmpList;
				try {
					tmpList = new ArrayList<>(new_Outputs.get(tmpNames.get(y)));
					double val = Double.parseDouble((String) parameters.get(i).get(tmpNames.get(y)));
					tmpList.add(val);
					new_Outputs.replace(tmpNames.get(y), tmpList);
				} catch (Exception ignored) {
					tmpList = new ArrayList<>();
					double val = Double.parseDouble((String) parameters.get(i).get(tmpNames.get(y)));
					tmpList.add(val);
					new_Outputs.put(tmpNames.get(y), tmpList);
				}
				parameters.get(i).remove(tmpNames.get(y));
			}
		});
		List<Object> simulation_morris = new ArrayList<>();
		simulation_morris.add(parameters);
		simulation_morris.add(new_Outputs);
		return simulation_morris;
	}

	/**
	 * Builds the string.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	private static String BuildString(final Map<String, Object> s) {
		StringBuilder txt = new StringBuilder();
		for (String name : s.keySet()) { txt.append(s.get(name).toString()).append("_"); }
		return txt.toString();
	}

	/**
	 * Stochasticity analysis from CSV.
	 *
	 * @param replicat
	 *            the replicat
	 * @param threshold
	 *            the threshold
	 * @param path_to_data
	 *            the path to data
	 * @param id_output
	 *            the id output
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	// Need to be tested and change like the main method if it works
	@SuppressWarnings ("unchecked")
	public static String StochasticityAnalysis_From_CSV(final int replicat, final double threshold,
			final String path_to_data, final int id_output, final IScope scope) {
		List<Object> STO_simu = readSimulation(path_to_data, id_output, scope);
		List<Map<String, Object>> MySample = Cast.asList(scope, STO_simu.get(0));
		Map<String, List<Double>> Outputs = Cast.asMap(scope, STO_simu.get(1), false);
		int min_replicat = 1;
		for (String name : Outputs.keySet()) {
			Map<String, List<Object>> sample = new HashedMap<>();
			for (Map<String, Object> m : MySample) {
				String s = BuildString(m);
				if (sample.containsKey(s)) {
					List<Object> tmp_l = sample.get(s);
					tmp_l.add(Outputs.get(name));
					m.replace(s, tmp_l);
				} else {
					List<Object> tmp_l = new ArrayList<>();
					tmp_l.add(Outputs.get(name));
					m.put(s, tmp_l);
				}
			}
			int tmp_replicat = 0;
			for (String ps : sample.keySet()) {
				List<Double> mean = computeMean(sample.get(ps), scope);
				List<Double> std = computeSTD(mean, sample.get(ps), scope);
				List<Double> cv = computeCV(std, mean);
				tmp_replicat = tmp_replicat + FindWithThreshold(cv, threshold);
			}
			min_replicat = tmp_replicat / sample.size();
		}
		min_replicat = min_replicat / Outputs.size();
		return Cast.asString(scope, min_replicat);
	}
}
