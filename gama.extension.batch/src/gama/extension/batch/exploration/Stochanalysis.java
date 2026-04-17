/*******************************************************************************************************
 *
 * Stochanalysis.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.batch.exploration;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.StringUtils;
import gama.core.experiment.parameters.ParametersSet;
import gama.extension.stats.Stats;

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

	/** The Constant CV. */
	// Statistical arbitrary indicators
	final static public String CV = "Coefficient of variation";

	/** The Constant SE. */
	final static public String SE = "Standard error";

	/** The Constant STOCHThresholds. */
	final static double[] STOCHThresholds = { 0.05, 0.01, 0.001 };

	/** The Constant ES. */
	final static public String ES = "Critical effect size";

	/** The Constant FISHEREffectSize. */
	final static double[] FISHEREffectSize = { 0.01, 0.05, 0.1, 0.2, 0.4, 0.8 };

	/** The Constant FISHERES. */
	final static String[] FISHERES = { "ultra-micro", "micro", "small", "medium", "large", "huge" };

	/** The Constant TALPHA. */
	final static double[] TALPHA = { .99, .95 };

	/** The Constant TBETA. */
	final static double[] TBETA = { .95, .80 };

	/** The Constant PT. */
	final static public String PT = "Power test";
	
	final static public int ROOLING_BOOTSTRAPS = 100;

	/** The Constant SA. */
	// List of methods
	final static protected List<String> SA = List.of(CV, SE);//List.of(CV, SE, ES, PT);

	/** The Constant SEP. */
	// UTILS
	final static String SEP = ",";

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
		sb.append(StringUtils.LN);
		sb.append(Out.size() + " outputs | " + nbsample + " samples | " + nbreplicates + " max replications")
				.append(" | Thresholds: ").append(Arrays.toString(STOCHThresholds)).append(" | T test alpha: 0.01")
				.append(" | T test beta: 0.05").append(" | Critical effect size: ")
				.append(Arrays.toString(FISHEREffectSize)).append(StringUtils.LN);
		sb.append(
				"Threshold meaning: threshold represent the marginal decrease of concerned statistic to decide on the number of replicates")
				.append(StringUtils.LN);
		sb.append(
				"Exemple: increase one replicates decreases standard error, if marginal decrease is under a given threshold, then select this number of replicates")
				.append(StringUtils.LN);
		sb.append(
				"Critical effect size: look at False negative (alpha) & False positive (beta) hypothesis - according to https://www.jasss.org/18/4/4.html")
				.append(StringUtils.LN);
		sb.append(StringUtils.LN).append(StringUtils.LN);

		for (String outputs : Out.keySet()) {
			Map<ParametersSet, Map<String, List<Double>>> pso = Out.get(outputs);
			sb.append("## Output : ");
			sb.append(outputs);
			sb.append(StringUtils.LN);

			for (String method : SA) {
				if (pso.values().stream().noneMatch(m -> m.containsKey(method))) { continue; }

				switch (method) {
					case CV, SE -> {
						// Compute all given nb of replicate required to accept a threshold hypothesis
						IMap<Double, List<Integer>> res = GamaMapFactory.create();
						for (Double thresh : STOCHThresholds) {
							List<Integer> lres = new ArrayList<>();
							for (ParametersSet ps : pso.keySet()) {
								lres.add(findWithRelativeThreshold(pso.get(ps).get(method), thresh));
							}
							res.put(thresh, lres);
						}
						sb.append(method).append(StringUtils.LN);
						for (Double threshold : STOCHThresholds) {
							sb.append(threshold).append(" : ");
							sb.append("min = ").append(Collections.min(res.get(threshold))).append(" | ");
							sb.append("max = ").append(Collections.max(res.get(threshold))).append(" | ");
							sb.append("avr = ")
									.append(Math.round(
											res.get(threshold).stream().mapToInt(i -> i).average().getAsDouble()))
									.append(StringUtils.LN);
						}
					}
					case ES, PT -> {
						IMap<Double, List<List<Integer>>> res = GamaMapFactory.create();
						for (Double es : FISHEREffectSize) {
							int idx = DoubleStream.of(FISHEREffectSize).boxed().toList().indexOf(es) * 2;
							List<List<Integer>> ab = new ArrayList<>();
							// High alpha/beta
							ab.add(pso.values().stream().mapToInt(r -> r.get(ES).get(idx).intValue()).boxed().toList());
							// Low alpha/beta
							ab.add(pso.values().stream().mapToInt(r -> r.get(ES).get(idx + 1).intValue()).boxed()
									.toList());
							res.put(es, ab);
						}

						List<Double> pt =
								pso.values().stream().filter(m -> m.containsKey(PT)).findFirst().get().get(PT);
						if (pt.isEmpty() || pt.get(0).isNaN() || pt.size() != 2) throw GamaRuntimeException.error(
								"Trying to retriev Power Test n estimates but failed to find results: ["
										+ pt.stream().map(d -> d.toString()).collect(Collectors.joining(",")) + "]",
								scope);

						sb.append(method).append(StringUtils.LN);
						sb.append(PT)
								.append(" with  alpha=0.01 (0.05), beta=0.05 (0.2) and effect sized based on (ANOVA) f="
										+ pt.get(1) + ", theoretical number of replicate is ")
								.append(pt.get(0)).append(StringUtils.LN);
						for (int i = 0; i < FISHEREffectSize.length; i++) {

							if (res.get(FISHEREffectSize[i]).stream().flatMap(List::stream).distinct().count() == 1) {
								sb.append(FISHERES[i]).append(" (").append(FISHEREffectSize[i]).append(") : "
										+ res.get(FISHEREffectSize[i]).get(0).get(0) + " replicates is not enough")
										.append(StringUtils.LN);
							} else {
								sb.append(FISHERES[i]).append(" (").append(FISHEREffectSize[i]).append(") : ");
								sb.append("min = ").append(Collections.min(res.get(FISHEREffectSize[i]).get(0)))
										.append(" (").append(Collections.min(res.get(FISHEREffectSize[i]).get(1)))
										.append(") | ");
								sb.append("max = ").append(Collections.max(res.get(FISHEREffectSize[i]).get(0)))
										.append(" (").append(Collections.max(res.get(FISHEREffectSize[i]).get(1)))
										.append(") | ");
								sb.append("avr = ")
										.append(Math.round(res.get(FISHEREffectSize[i]).get(0).stream().mapToInt(e -> e)
												.average().getAsDouble()))
										.append(" (").append(Math.round(res.get(FISHEREffectSize[i]).get(1).stream()
												.mapToInt(e -> e).average().getAsDouble()))
										.append(")" + StringUtils.LN);
							}
						}
					}
					default -> throw new IllegalArgumentException("Unexpected stochastic analysis: " + method);
				}

			}
			sb.append(StringUtils.LN).append(StringUtils.LN);
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
		sb.append(IntStream.range(2, nbreplicates).boxed().map(String::valueOf).collect(Collectors.joining(SEP)));
		sb.append(StringUtils.LN);

		for (String o : Out.keySet()) {
			Map<ParametersSet, Map<String, List<Double>>> om = Out.get(o);
			for (ParametersSet p : om.keySet()) {
				String lineP = ph.stream().map(head -> p.get(head).toString()).collect(Collectors.joining(SEP));
				Map<String, List<Double>> cr = om.get(p);
				for (String m : cr.keySet()) {
					sb.append(o).append(SEP);
					sb.append(lineP).append(SEP);
					sb.append(m).append(SEP);
					sb.append(cr.get(m).stream().skip(1).map(String::valueOf).collect(Collectors.joining(SEP)));
					sb.append(StringUtils.LN);
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
	public static void writeAndTellReport(final File f,
			final Map<String, Map<ParametersSet, Map<String, List<Double>>>> outputs, final int nbsample,
			final int nbreplicates, final IScope scope) throws GamaRuntimeException {

		try {
			try (FileWriter fw = new FileWriter(f, false)) {
				fw.write("txt".equalsIgnoreCase(FilenameUtils.getExtension(f.getPath()))
						? buildResultMap(outputs, nbsample, nbreplicates, scope)
						: buildStochMap(outputs, nbsample, nbreplicates, scope));

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
	public static String buildSimulationCsv(final IMap<ParametersSet, Map<String, List<Object>>> outputs,
			final IScope scope) {
		StringBuilder sb = new StringBuilder();
		String sep = ";";

		// Write the header
		for (String param : outputs.keySet().stream().findFirst().get().keySet()) { sb.append(param).append(sep); }
		for (String output : outputs.anyValue(scope).keySet()) { sb.append(output).append(sep); }

		// Find results and append to global string
		for (ParametersSet ps : outputs.keySet()) {
			Map<String, List<Object>> res = outputs.get(ps);
			int nbr = res.values().stream().findAny().get().size();
			if (!res.values().stream().allMatch(r -> r.size() == nbr)) {
				GAMA.reportAndThrowIfNeeded(scope,
						GamaRuntimeException.warning(
								"Not all sample of stochastic analysis have the same number of replicates", scope),
						false);
			} else {
				for (int r = 0; r < nbr; r++) {
					sb.append(StringUtils.LN);
					for (Object pvalue : ps.values()) { sb.append(pvalue).append(sep); }
					for (String output : res.keySet()) { sb.append(res.get(output).get(r)).append(sep); }
				}
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
	public static void writeAndTellResult(final File f, final IMap<ParametersSet, Map<String, List<Object>>> outputs,
			final IScope scope) throws GamaRuntimeException {
		try (FileWriter fw = new FileWriter(f, false)) {
			fw.write(buildSimulationCsv(outputs, scope));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + f.toString() + " not found", scope);
		}
	}

	// ----------------------------- Inner methods

	
	/**
	 * Find the minimum replicates size depending of a threshold, when CV[i]-CV[i+1] < threshold, we keep the id "i".
	 *
	 * @param CV
	 *            : the coefficient of variation for each number of replicates
	 * @return the minimum replicates size (or -1 if the threshold is not reached)
	 */
	private static int findWithThreshold(final List<Double> CV, final double threshold) {
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
	 * Find the minimum replicates size depending on a threshold, when CV[i-1] - CV[i] >= 0 and CV[i-1] - CV[i] <=
	 * min_arg(CV) * threshold, we keep the number of replicates "i".
	 *
	 * @param Stat
	 *            : the statistic given to assess replicates effectiveness
	 * @return the minimum replicates size to reach a given threshold of marginal benefit adding a new replicates
	 */
	private static int findWithRelativeThreshold(final List<Double> Stat, final double threshold) {
		double th = Collections.min(Stat) * threshold;
		for (int i = 2; i < Stat.size(); i++) {
			double delta = Stat.get(i - 1) - Stat.get(i);
			if (delta >= 0 && delta <= th) return i;
		}
		return Stat.size();
	}

	// -------------------------------------------------- //
	// ################# ACTUAL SAMPLING ################# //

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
	 *         TODO : also export the raw result of stochasticity measures
	 *
	 */
	public static IMap<ParametersSet, IList<Double>> stochasticityAnalysis(
			final IMap<ParametersSet, IList<Object>> sample, final String method, final IScope scope) {

		IMap<ParametersSet, IList<Double>> res =
				GamaMapFactory.create(Types.get(ParametersSet.class), Types.LIST.of(Types.FLOAT));

		switch (method) {
			case CV -> {
				for (var es : sample.entrySet()) {
					IList<Double> data = es.getValue().stream(scope).mapToDouble(e -> Cast.asFloat(scope, e)).boxed()
							.collect(GamaListFactory.toGamaList());
					List<IList<Double>> bootstrapRuns = new ArrayList<>();
					for (int i = 0; i < ROOLING_BOOTSTRAPS; i++) {
						IList<Double> bootstrapped = bootstrapSample(scope, data);
						bootstrapRuns.add(Stats.rollingVC(scope, bootstrapped));
					}
					res.put(es.getKey(), averageLists(bootstrapRuns));
				}
			}
			case SE -> {
				for (var es : sample.entrySet()) {
					IList<Double> data = es.getValue().stream(scope).mapToDouble(e -> Cast.asFloat(scope, e)).boxed()
							.collect(GamaListFactory.toGamaList());
					List<IList<Double>> bootstrapRuns = new ArrayList<>();
					for (int i = 0; i < ROOLING_BOOTSTRAPS; i++) {
						IList<Double> bootstrapped = bootstrapSample(scope, data);
						bootstrapRuns.add(Stats.rollingSE(scope, bootstrapped));
					}
					res.put(es.getKey(), averageLists(bootstrapRuns));
				}
			}
			case ES -> {
				for (var es : sample.entrySet()) {
					IList<Double> data = es.getValue().stream(scope).mapToDouble(e -> Cast.asFloat(scope, e)).boxed()
							.collect(GamaListFactory.toGamaList());
					List<IList<Double>> bootstrapRuns = new ArrayList<>();
					for (int i = 0; i < ROOLING_BOOTSTRAPS; i++) {
						IList<Double> bootstrapped = bootstrapSample(scope, data);
						bootstrapRuns.add(criticalEffectSize(scope, bootstrapped));
					}
					res.put(es.getKey(), averageLists(bootstrapRuns));
				}
			}
			case PT -> {
				double effectSize = fTestEffectSize(sample.values(), scope);
				sample.getKeys().forEach(ps -> res.put(ps, List.of(powerTestEffectSize(sample.size(), effectSize), effectSize)
						.stream().collect(GamaListFactory.toGamaList())));
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + method);
		}
		return res;

	}

	/**
	 * Helper to create a bootstrap sample (sampling with replacement).
	 */
	private static IList<Double> bootstrapSample(final IScope scope, final IList<Double> data) {
		int n = data.size();
		IList<Double> result = GamaListFactory.create(Types.FLOAT);
		if (n == 0) return result;
		for (int i = 0; i < n; i++) { result.add(data.get(scope.getRandom().getGenerator().nextInt(n))); }
		return result;
	}

	/**
	 * Helper to average multiple lists of doubles element-wise.
	 */
	private static IList<Double> averageLists(final List<IList<Double>> lists) {
		if (lists == null || lists.isEmpty()) return GamaListFactory.create(Types.FLOAT);
		int size = lists.get(0).size();
		IList<Double> result = GamaListFactory.create(Types.FLOAT);
		for (int i = 0; i < size; i++) {
			double sum = 0;
			for (IList<Double> list : lists) { sum += list.get(i); }
			result.add(sum / lists.size());
		}
		return result;
	}

	/**
	 * Return a list of desired number of replicates based on more permissive conditions: <\br> 
	 * from strongest hypothesis ES=0.01, alpha=.99 and beta=.95 to lowest ES=0.8, alpha=.95 and beta=.80
	 *
	 * @param aSample
	 * @param scope
	 * @return
	 */
	public static IList<Double> criticalEffectSize(final IScope scope, final IList<Double> aSample) {
		IList<Double> ce = GamaListFactory.create(Types.FLOAT);
		for (double es : FISHEREffectSize) {
			for (int i = 0; i < TALPHA.length; i++) { 
				ce.add(Cast.asFloat(scope, Stats.powerTestCSE(scope, aSample, TALPHA[i], TBETA[i], es))); }
		}
		return ce;
	}

	// ########### POWER TEST

	/**
	 * @see https://doi.org/10.1007/s10588-016-9218-0
	 *
	 * @param j
	 * @param es
	 * @return
	 */
	private static double powerTestEffectSize(final int j, final double es) {
		return 14.091 * Math.pow(j, -0.640) * Math.pow(es, -1.986);
	}

	/**
	 * F test effect size.
	 *
	 * @param groups
	 *            the groups
	 * @param scope
	 *            the scope
	 * @return the double
	 */
	// see : https://en.wikipedia.org/wiki/F-test
	private static double fTestEffectSize(final Collection<IList<Object>> groups, final IScope scope) {
		List<Double> groupMean = groups.stream()
				.mapToDouble(group -> group.stream().mapToDouble(e -> Cast.asFloat(scope, e)).average().getAsDouble())
				.boxed().toList();
		double overallMean = groupMean.stream().mapToDouble(d -> d).average().getAsDouble();
		double betweenGroupVariability =
				groupMean.stream().mapToDouble(mean -> Math.pow(overallMean - mean, 2)).sum() / (groups.size() - 1);
		double withinGroupVariability = 0.0;
		int i = 0;
		for (IList<Object> group : groups) {
			Double m = groupMean.get(i++);
			withinGroupVariability += group.stream().mapToDouble(d -> Math.pow(Cast.asFloat(scope, d) - m, 2)).sum();
		}
		withinGroupVariability /= groups.stream().mapToInt(List::size).sum();
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
	public static IList<Object> readSimulation(final String path, final int idOutput, final IScope scope)
			throws GamaRuntimeException {
		IList<Map<String, Object>> parameters = GamaListFactory.create();
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
		IList<Object> simulation_morris = GamaListFactory.create();
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
	private static String buildString(final Map<String, Object> s) {
		StringBuilder txt = new StringBuilder();
		for (var v : s.values()) { txt.append(v).append("_"); }
		return txt.toString();
	}

	/**
	 * Stochasticity analysis from direct data.
	 *
	 * @param replicat
	 *            the replicat
	 * @param threshold
	 *            the threshold
	 * @param MySample
	 *            the sample (list of maps of parameters)
	 * @param Outputs
	 *            the outputs (map of lists of doubles)
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@SuppressWarnings ("unchecked")
	public static String stochasticityAnalysis_From_Data(final int replicat, final double threshold,
			final IList<IMap<String, Object>> MySample, final IMap<String, IList<Double>> Outputs, final IScope scope) {
		double min_replicat = 1;
		for (IList<Double> val : Outputs.values()) {
			IMap<String, IList<Double>> groupedSample = GamaMapFactory.create();
			for (int i = 0; i < MySample.size(); i++) {
				String s = buildString(MySample.get(i));
				groupedSample.computeIfAbsent(s, k -> GamaListFactory.create()).add(val.get(i));
			}
			double tmp_replicat = 0;
			for (String ps : groupedSample.keySet()) {
				IList<Double> outputForParams = groupedSample.get(ps);
				int nbBootstrap = 100;
				List<IList<Double>> bootstrapRuns = new ArrayList<>();
				for (int i = 0; i < nbBootstrap; i++) {
					IList<Double> bootstrapped = bootstrapSample(scope, outputForParams);
					bootstrapRuns.add(Stats.rollingVC(scope, bootstrapped));
				}
				IList<Double> cv = averageLists(bootstrapRuns);
				tmp_replicat = tmp_replicat + findWithThreshold(cv, threshold);
			}
			min_replicat = tmp_replicat / groupedSample.size();
		}
		min_replicat = min_replicat / Outputs.size();
		return Cast.asString(scope, min_replicat);
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
	// TODO: Needs to be tested and change like the main method if it works
	@SuppressWarnings ("unchecked")
	public static String stochasticityAnalysis_From_CSV(final int replicat, final double threshold,
			final String path_to_data, final int id_output, final IScope scope) {
		IList<Object> STO_simu = readSimulation(path_to_data, id_output, scope);
		IList<IMap<String, Object>> MySample = GamaListFactory.castToList(scope, STO_simu.get(0));
		IMap<String, IList<Double>> Outputs = GamaMapFactory.castToMap(scope, STO_simu.get(1));
		return stochasticityAnalysis_From_Data(replicat, threshold, MySample, Outputs, scope);
	}
}
