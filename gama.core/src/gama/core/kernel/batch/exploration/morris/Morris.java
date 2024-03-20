/*******************************************************************************************************
 *
 * Morris.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.morris;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 *
 * @author tomroy
 *
 */

/**
 *
 * This class make a Morris analysis from the GAMA simulation Input and output or from a CSV data file with all Inputs
 * and Outputs of the GAMA model.
 */

/**
 *
 * A bit of context pour the results of Morris analysis. mu describe for each parameters the influence on the outputs, a
 * positive high value indicate that the parameter has a strong positive influence on the output conversely, a negative
 * high value indicate that the parameter has a strong negative influence on the output. If mu is near to zero, the
 * parameter don't have strong influence on the output.
 *
 * mu_star rank the parameters in order of importance.
 *
 *
 * sigma indicate if a factor has interactions with others.
 */

public class Morris {
	/**
	 * Attributes MySample -> List of samples for simulation ParametersNames-> List of names of inputs mu -> mu
	 * indicates the mean of the distribution mu_star -> mu_star indicates the mean of the distribution of absolute
	 * values sigma -> The standard deviation of the distribution
	 *
	 * Example: MySample=[{Var1=1,Var2=1},{Var1=4,Var2=5}] ParametersName=[Var1,Var2]
	 *
	 */

	// protected List<Map<String, Object>> MySample;
	// protected List<String> ParametersNames;
	// protected Map<String, Double> mu;
	// protected Map<String, Double> mu_star;
	// protected Map<String, Double> sigma;

	/** ########################## SAVING/LOADING METHODS ############################# */

	public static String buildResultTxt(final String name, final Map<String, Double> mu,
			final Map<String, Double> mu_star, final Map<String, Double> sigma) {
		StringBuilder sb = new StringBuilder();
		sb.append("MORRIS ANALYSIS : \n");
		sb.append("\n");
		sb.append("Result for output :" + name + "\n");
		sb.append("mu : \n");
		for (String n : mu.keySet()) {
			sb.append("		");
			sb.append(n);
			sb.append(" : ");
			sb.append(mu.get(n));
			sb.append("\n");
		}
		sb.append("mu_star : \n");
		for (String n : mu_star.keySet()) {
			sb.append("		");
			sb.append(n);
			sb.append(" : ");
			sb.append(mu_star.get(n));
			sb.append("\n");
		}
		sb.append("sigma : \n");
		for (String n : sigma.keySet()) {
			sb.append("		");
			sb.append(n);
			sb.append(" : ");
			sb.append(sigma.get(n));
			sb.append("\n");
		}
		return sb.toString();

	}

	/**
	 * Write and tell result.
	 *
	 * @param name
	 *            the name
	 * @param path
	 *            the path
	 * @param first
	 *            the first
	 * @param scope
	 *            the scope
	 * @param morris_coefficient
	 *            the morris coefficient
	 */
	public static void writeAndTellResult(final String name, final String path, final IScope scope,
			final List<Map<String, Double>> morris_coefficient) throws GamaRuntimeException {
		Map<String, Double> mu = morris_coefficient.get(0);
		Map<String, Double> mu_star = morris_coefficient.get(1);
		Map<String, Double> sigma = morris_coefficient.get(2);
		try {
			File file = new File(path);
			try (FileWriter fw = new FileWriter(file, false)) {
				fw.write(buildResultTxt(name, mu, mu_star, sigma));
			}
		} catch (IOException e) {
			throw GamaRuntimeException.error("File " + path + " not found", scope);
		}
	}

	/**
	 *
	 * @param path
	 *            Path to a CSV file with Inputs and Outputs, Outputs should be at the end of List of Variable
	 * @param idOutput
	 *            id of the column of the first output
	 * @return A List of Outputs' values
	 */
	public static List<Object> readSimulation(final String path, final int idOutput, final IScope scope) throws GamaRuntimeException {
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

	/** ########################## ANALYSIS METHODS ############################# */

	/**
	 * Convert a List of Map into a Map of List
	 *
	 * @param ListMap
	 *            a list of map
	 * @return A map of list
	 */
	private static Map<String, List<Double>> transformListMapToMapList(final List<Map<String, Double>> ListMap,
			final List<String> ParametersNames) {
		Map<String, List<Double>> MapList = new HashMap<>();
		for (int i = 0; i < ParametersNames.size(); i++) {
			List<Double> tmpList = new ArrayList<>();
			int finalI = i;
			ListMap.forEach(map -> { tmpList.add(map.get(ParametersNames.get(finalI))); });
			MapList.put(ParametersNames.get(i), tmpList);
		}
		return MapList;
	}

	/**
	 * Calculation of the difference between two matrix
	 *
	 * @param result_up
	 *            first matrix
	 * @param result_lo
	 *            second matrix
	 * @return Return the difference
	 */
	private static List<Map<String, Double>> calc_results_difference(final List<Map<String, Double>> result_up,
			final List<Map<String, Double>> result_lo, final List<String> ParametersNames) {
		List<Map<String, Double>> resutat_calc = new ArrayList<>();
		for (int i = 0; i < result_lo.size(); i++) {
			Map<String, Double> tmp2 = result_lo.get(i);
			Map<String, Double> tmp1 = result_up.get(i);
			Map<String, Double> tmpFin = new HashMap<>();
			IntStream.range(0, ParametersNames.size()).forEach(z -> {
				double val = tmp1.get(ParametersNames.get(z)) - tmp2.get(ParametersNames.get(z));
				tmpFin.put(ParametersNames.get(z), val);
			});
			resutat_calc.add(tmpFin);
		}
		return resutat_calc;
	}

	/**
	 * Reorganize the output matrix to shape : List<Map<String,Double>
	 *
	 * @param Outputs
	 *            List of Outputs
	 * @param value_increased
	 *            List of increased values
	 * @param value_decreased
	 *            List of decreased values
	 * @param increase
	 *            a boolean depending if we create the matrix for increased of decreased value
	 * @return
	 */
	private static List<Map<String, Double>> reorganize_output_matrix(final List<List<Double>> Outputs,
			final List<List<Map<String, Boolean>>> value_increased,
			final List<List<Map<String, Boolean>>> value_decreased, final boolean increase,
			final List<String> ParametersNames) {
		List<List<Map<String, Boolean>>> new_value_increased = new ArrayList<>(value_increased);
		List<List<Map<String, Boolean>>> new_value_decreased = new ArrayList<>(value_decreased);
		if (increase) {
			IntStream.range(0, value_decreased.size()).forEach(l -> {
				Map<String, Boolean> tmpMap = new LinkedHashMap<>();
				IntStream.range(0, ParametersNames.size()).forEach(i -> tmpMap.put(ParametersNames.get(i), false));
				new_value_increased.get(l).add(0, tmpMap);
				new_value_decreased.get(l).add(new_value_decreased.get(l).size(), tmpMap);
			});

		} else {
			IntStream.range(0, value_decreased.size()).forEach(l -> {
				Map<String, Boolean> tmpMap = new LinkedHashMap<>();
				IntStream.range(0, ParametersNames.size()).forEach(i -> tmpMap.put(ParametersNames.get(i), false));
				new_value_increased.get(l).add(new_value_decreased.get(l).size(), tmpMap);
				new_value_decreased.get(l).add(0, tmpMap);
			});
		}
		List<List<Map<String, Boolean>>> global = new ArrayList<>();
		IntStream.range(0, new_value_decreased.size()).forEach(i -> {
			List<Map<String, Boolean>> res = new ArrayList<>();
			List<Map<String, Boolean>> tmp1 = new_value_increased.get(i);
			List<Map<String, Boolean>> tmp2 = new_value_decreased.get(i);
			IntStream.range(0, tmp1.size()).forEach(z -> {
				Map<String, Boolean> tmpres = new LinkedHashMap<>();
				Map<String, Boolean> map1 = tmp1.get(z);
				Map<String, Boolean> map2 = tmp2.get(z);
				IntStream.range(0, ParametersNames.size()).forEach(y -> {
					if (map1.get(ParametersNames.get(y)) || map2.get(ParametersNames.get(y))) {
						tmpres.put(ParametersNames.get(y), true);
					} else {
						tmpres.put(ParametersNames.get(y), false);
					}
				});
				res.add(tmpres);
			});
			global.add(res);
		});
		List<List<Map<String, Double>>> resultat = new ArrayList<>();
		IntStream.range(0, Outputs.size()).forEach(i -> {
			List<Map<String, Double>> Ltmp = new ArrayList<>();
			List<Double> l = Outputs.get(i);
			for (int z = 0; z < l.size(); z++) {
				Map<String, Boolean> maptmp = global.get(i).get(z);
				Map<String, Double> maptmp2 = new LinkedHashMap<>();
				double val = l.get(z);
				IntStream.range(0, ParametersNames.size()).forEach(y -> {
					boolean booltmp = maptmp.get(ParametersNames.get(y));
					if (booltmp) {
						maptmp2.put(ParametersNames.get(y), val);
					} else {
						maptmp2.put(ParametersNames.get(y), 0.0);
					}
				});
				Ltmp.add(maptmp2);
			}
			resultat.add(Ltmp);
		});
		List<Map<String, Double>> finalResult = new ArrayList<>();
		resultat.forEach(traj -> {
			Map<String, Double> maptmp = new LinkedHashMap<>();
			IntStream.range(0, traj.get(0).size()).forEach(val -> {
				double tmpSum = 0;
				for (Map<String, Double> stringDoubleMap : traj) {
					tmpSum = tmpSum + stringDoubleMap.get(ParametersNames.get(val));
				}
				maptmp.put(ParametersNames.get(val), tmpSum);
			});
			finalResult.add(maptmp);
		});
		return finalResult;
	}

	/**
	 * Find the elementary effects of the sample on outputs
	 *
	 * @param MySampleTemp
	 *            Sample used for finding outputs
	 * @param Outputs
	 *            Outputs founds with the sample
	 * @param trajectory_size
	 *            length of a trajectory
	 * @param delta
	 *            delta : 1/nb_levels
	 * @return
	 */
	private static List<Map<String, Double>> compute_elementary_effects(final List<Map<String, Double>> MySampleTemp,
			final List<Double> Outputs, final int trajectory_size, final double delta,
			final List<String> ParametersNames, final List<Map<String, Object>> MySample) {
		int num_trajectories;
		num_trajectories = MySample.size() / trajectory_size;
		List<List<Double>> new_Outputs = new ArrayList<>();
		List<Double> cpOutput = new ArrayList<>(Outputs);
		IntStream.range(0, num_trajectories).forEach(i -> {
			List<Double> tmp = new ArrayList<>();
			IntStream.range(0, trajectory_size).forEach(y -> { tmp.add(cpOutput.get(y + i * trajectory_size)); });
			new_Outputs.add(tmp);
		});
		List<Map<String, Double>> new_Input = new ArrayList<>();
		int i;
		for (i = 1; i < MySampleTemp.size(); i++) {
			Map<String, Double> maptmp = new HashMap<>();
			if (i == 0) {
				IntStream.range(0, ParametersNames.size()).forEach(y -> { maptmp.put(ParametersNames.get(y), 0.0); });
			} else {
				int valtmp = i;
				IntStream.range(0, ParametersNames.size()).forEach(y -> {
					double val = MySampleTemp.get(valtmp).get(ParametersNames.get(y))
							- MySampleTemp.get(valtmp - 1).get(ParametersNames.get(y));
					maptmp.put(ParametersNames.get(y), val);
				});
			}
			new_Input.add(maptmp);
		}
		List<Map<String, Boolean>> value_increased = new ArrayList<>();
		List<Map<String, Boolean>> value_decreased = new ArrayList<>();
		new_Input.forEach(m -> {
			Map<String, Boolean> maptmp1 = new LinkedHashMap<>();
			Map<String, Boolean> maptmp2 = new LinkedHashMap<>();
			IntStream.range(0, ParametersNames.size()).forEach(y -> {
				maptmp1.put(ParametersNames.get(y), m.get(ParametersNames.get(y)) > 0);
				maptmp2.put(ParametersNames.get(y), m.get(ParametersNames.get(y)) < 0);
			});
			value_increased.add(maptmp1);
			value_decreased.add(maptmp2);
		});
		List<List<Map<String, Boolean>>> inputs_splits_increased = new ArrayList<>();
		List<List<Map<String, Boolean>>> inputs_splits_decreased = new ArrayList<>();
		List<List<Map<String, Boolean>>> inputs_splits_increased2 = new ArrayList<>();
		List<List<Map<String, Boolean>>> inputs_splits_decreased2 = new ArrayList<>();
		List<Map<String, Boolean>> cpIntput_increased = new ArrayList<>(value_increased);
		List<Map<String, Boolean>> cpIntput_decreased = new ArrayList<>(value_decreased);
		IntStream.range(0, num_trajectories).forEach(z -> {
			List<Map<String, Boolean>> tmp1 = new ArrayList<>();
			List<Map<String, Boolean>> tmp2 = new ArrayList<>();
			List<Map<String, Boolean>> tmp3 = new ArrayList<>();
			List<Map<String, Boolean>> tmp4 = new ArrayList<>();
			IntStream.range(0, trajectory_size - 1).forEach(y -> {
				tmp1.add(cpIntput_increased.get(y + z * trajectory_size));
				tmp2.add(cpIntput_decreased.get(y + z * trajectory_size));
				tmp3.add(cpIntput_increased.get(y + z * trajectory_size));
				tmp4.add(cpIntput_decreased.get(y + z * trajectory_size));
			});
			inputs_splits_increased.add(tmp1);
			inputs_splits_decreased.add(tmp2);
			inputs_splits_increased2.add(tmp3);
			inputs_splits_decreased2.add(tmp4);
		});
		List<Map<String, Double>> resultat_increased = reorganize_output_matrix(new_Outputs, inputs_splits_increased,
				inputs_splits_decreased, true, ParametersNames);
		List<Map<String, Double>> resultat_decreased = reorganize_output_matrix(new_Outputs, inputs_splits_increased2,
				inputs_splits_decreased2, false, ParametersNames);
		List<Map<String, Double>> elementary_effects =
				calc_results_difference(resultat_increased, resultat_decreased, ParametersNames);
		elementary_effects.forEach(map -> {
			IntStream.range(0, ParametersNames.size()).forEach(w -> {
				double val = map.get(ParametersNames.get(w)) / delta;
				map.replace(ParametersNames.get(w), val);
			});
		});
		return elementary_effects;
	}

	/**
	 * Main method for Morris Analysis
	 *
	 * @param num_levels:
	 *            Number of levels used for the sampling (Usually 4)
	 * @param Outputs:
	 *            List of the Output to analyze
	 *
	 */

	public static List<Map<String, Double>> morrisAggregation(final int num_levels, final List<Double> Outputs,
			final List<Map<String, Object>> sample) {
		List<Map<String, Object>> MySample = sample;
		List<String> ParametersNames = sample.get(0).keySet().stream().toList();

		List<Map<String, Double>> MySampleTemp = new ArrayList<>();
		MySample.forEach(m -> {
			Map<String, Double> maptmp = new LinkedHashMap<>();
			IntStream.range(0, ParametersNames.size()).forEach(i -> {
				Object o = m.get(ParametersNames.get(i));
				if (Objects.equals(o.toString(), "false")) {
					maptmp.put(ParametersNames.get(i), 0.0);
				} else if (Objects.equals(o.toString(), "true")) {
					maptmp.put(ParametersNames.get(i), 1.0);
				} else {
					maptmp.put(ParametersNames.get(i), Double.parseDouble(o.toString()));
				}
			});
			MySampleTemp.add(maptmp);
		});

		double delta = num_levels / (2.0 * ((double) num_levels - 1));
		int num_vars = MySampleTemp.get(0).size();
		int number_of_groups = num_vars;
		int num_trajectories;
		int trajectory_size;
		num_trajectories = (int) Math.round(MySample.size() / (number_of_groups + 1.0));
		trajectory_size = (int) Math.round(MySample.size() / (double)num_trajectories);
		List<Map<String, Double>> elementary_effects =
				compute_elementary_effects(MySampleTemp, Outputs, trajectory_size, delta, ParametersNames, MySample);
		Map<String, List<Double>> elementary = transformListMapToMapList(elementary_effects, ParametersNames);
		Map<String, Double> mu = new LinkedHashMap<>();
		IntStream.range(0, ParametersNames.size()).forEach(i -> {
			double val = 0;
			List<Double> listtmp = elementary.get(ParametersNames.get(i));
			for (Double aDouble : listtmp) { val = val + aDouble; }
			mu.put(ParametersNames.get(i), val / listtmp.size());
		});
		Map<String, Double> mu_star = new LinkedHashMap<>();
		IntStream.range(0, ParametersNames.size()).forEach(i -> {
			double val = 0;
			List<Double> listtmp = elementary.get(ParametersNames.get(i));
			for (Double aDouble : listtmp) { val = val + abs(aDouble); }
			mu_star.put(ParametersNames.get(i), val / listtmp.size());
		});
		Map<String, Double> sigma = new LinkedHashMap<>();
		IntStream.range(0, ParametersNames.size()).forEach(i -> {
			double val = 0;
			List<Double> listtmp = elementary.get(ParametersNames.get(i));
			for (Double aDouble : listtmp) { val = val + pow(aDouble - mu.get(ParametersNames.get(i)), 2); }
			val = Math.sqrt(val / (listtmp.size() - 1));
			sigma.put(ParametersNames.get(i), val);
		});
		List<Map<String, Double>> morris_coefficient = new ArrayList<>();
		morris_coefficient.add(mu);
		morris_coefficient.add(mu_star);
		morris_coefficient.add(sigma);
		return morris_coefficient;
	}

	/**
	 * Morris aggregation CSV.
	 *
	 * @param num_levels
	 *            the num levels
	 * @param Outputs
	 *            the outputs
	 * @param sample
	 *            the sample
	 * @return the list
	 */
	public static List<Map<String, Double>> morrisAggregation_CSV(final int num_levels, final List<Double> Outputs,
			final List<Map<String, Object>> sample) {
		List<Map<String, Object>> MySample = sample;
		List<String> ParametersNames = sample.get(0).keySet().stream().toList();

		List<Map<String, Double>> MySampleTemp = new ArrayList<>();
		MySample.forEach(m -> {
			Map<String, Double> maptmp = new LinkedHashMap<>();
			IntStream.range(0, ParametersNames.size()).forEach(i -> {
				Object o = m.get(ParametersNames.get(i));
				if (Objects.equals(o.toString(), "false")) {
					maptmp.put(ParametersNames.get(i), 0.0);
				} else if (Objects.equals(o.toString(), "true")) {
					maptmp.put(ParametersNames.get(i), 1.0);
				} else {
					maptmp.put(ParametersNames.get(i), Double.parseDouble(o.toString()));
				}
			});
			MySampleTemp.add(maptmp);
		});

		double delta = num_levels / (2.0 * ((double) num_levels - 1));
		int num_vars = MySampleTemp.get(0).size();
		int number_of_groups = num_vars;
		int num_trajectories;
		int trajectory_size;
		num_trajectories = (int) Math.round(MySample.size() / (number_of_groups + 1.0));
		trajectory_size = (int) Math.round(MySample.size() / (double)num_trajectories);
		List<Map<String, Double>> elementary_effects =
				compute_elementary_effects(MySampleTemp, Outputs, trajectory_size, delta, ParametersNames, MySample);
		Map<String, List<Double>> elementary = transformListMapToMapList(elementary_effects, ParametersNames);
		Map<String, Double> mu = new LinkedHashMap<>();
		IntStream.range(0, ParametersNames.size()).forEach(i -> {
			double val = 0;
			List<Double> listtmp = elementary.get(ParametersNames.get(i));
			for (Double aDouble : listtmp) { val = val + aDouble; }
			mu.put(ParametersNames.get(i), val / listtmp.size());
		});
		Map<String, Double> mu_star = new LinkedHashMap<>();
		IntStream.range(0, ParametersNames.size()).forEach(i -> {
			double val = 0;
			List<Double> listtmp = elementary.get(ParametersNames.get(i));
			for (Double aDouble : listtmp) { val = val + abs(aDouble); }
			mu_star.put(ParametersNames.get(i), val / listtmp.size());
		});
		Map<String, Double> sigma = new LinkedHashMap<>();
		IntStream.range(0, ParametersNames.size()).forEach(i -> {
			double val = 0;
			List<Double> listtmp = elementary.get(ParametersNames.get(i));
			for (Double aDouble : listtmp) { val = val + pow(aDouble - mu.get(ParametersNames.get(i)), 2); }
			val = Math.sqrt(val / (listtmp.size() - 1));
			sigma.put(ParametersNames.get(i), val);
		});
		List<Map<String, Double>> morris_coefficient = new ArrayList<>();
		morris_coefficient.add(mu);
		morris_coefficient.add(mu_star);
		morris_coefficient.add(sigma);
		return morris_coefficient;
	}

}
