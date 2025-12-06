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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.IntStream;

import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.operators.Strings;

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

public final class Morris {
	
	public static final int DEFAULT_LEVELS = 4;
	public static final int DEFAULT_TRAJECTORIES = 10;
	
	/**
	 * Attributes 
	 * simulationSamples -> List of samples for simulation 
	 * parametersNames-> List of names of inputs 
	 * mu -> mu indicates the mean of the distribution 
	 * mu_star -> mu_star indicates the mean of the distribution of absolute values 
	 * sigma -> The standard deviation of the distribution
	 *
	 * Example: MySample=[{Var1=1,Var2=1},{Var1=4,Var2=5}] ParametersName=[Var1,Var2]
	 *
	 */
	 protected List<Map<String, Object>> simulationSamples; // Experiment plan defined using Morris sampling
	 protected List<String> parametersNames;
	 
	 final private int nblevels;
	  
	 /** Map of outputs values */
	 private Map<String, List<Double>> outputs; // for each outputs the list of all results
	 
	 /**
	  * RESULTS
	  */
	 protected Map<String, Map<String, Double>> mu;
	 protected Map<String, Map<String, Double>> mu_star;
	 protected Map<String, Map<String, Double>> sigma;
	 
	 // #################################################################################### //
	 
	 /**
	  * Inner utility constructor
	  * @param nblevels
	  */
	 private Morris(int nblevels) {
		 this.nblevels = nblevels;
		 this.mu = new HashMap<>();
		 this.mu_star = new HashMap<>();
		 this.sigma = new HashMap<>();
	 }
	 
	 /**
	  * Usual constructor from inside MorrisExploration class that monitor the
	  * Morris sensitivity analysis exploration
	  * 
	  * @param sample
	  * @param nblevels
	  */
	 public Morris(List<Map<String, Object>> sample, int nblevels) {
		 this(nblevels);
		 this.simulationSamples = new ArrayList<>(sample);
		 this.parametersNames = this.simulationSamples.stream().findAny().get().keySet().stream().toList();
	 }
	 
	 /**
	  * File should be a csv file with ',' separator with given properties: </p>
	  * 1) each line is a simulation </br>
	  * 2) each colomn is either a input or an output of the simulation </br>
	  * 3) all inputs should be first in a row (i.e. from column 0 to n-1, 'n' being the number of parameters) </br>
	  * 4) all outputs should follow at the end of the row (i.e. from column n to the end) </br>
	  * 5) First row should be the corresponding name of inputs and outputs </p>
	  * 
	  * @param file : the file containing I/O of all simulations
	  * @param nbParams : the number of input parameter of the simulation
	  * @param nblevels : the number of level used for the Morris sample
	  * @param scope
	  */
	 public Morris (File file, int nbParams, int nblevels, IScope scope) {
		 this(nblevels);
		 this.simulationSamples = new ArrayList<>();
		 this.parametersNames = new ArrayList<>();
		 this.outputs = new LinkedHashMap<>();

		 try {
			 
			 FileReader fr = null;
			 fr = new FileReader(file, StandardCharsets.UTF_8);
			 BufferedReader br = new BufferedReader(fr);
			 List<String> listNames = readMorrisCsvHeader(br, nbParams); 
			 readMorrisCsvContent(br, nbParams, listNames);
			 
		 } catch (FileNotFoundException e) {
			 GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error(e.getMessage(), scope), true);
		 } catch (IOException e) {
			 GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error(e.getMessage(), scope), true);
		}		

	 }
	 
	 /**
	  * Reads all the content lines (after the header) of the csv file used to create an instance of Morris. 
	  * This must be called after readMorrisCsvHeader
	  * @param br the BufferedReader used to read the csv
	  * @param nbParams the number of parameter columns
	  * @param listNames the list of names of each column
	  * @throws NumberFormatException
	  * @throws IOException
	  */
	 private void readMorrisCsvContent(final BufferedReader br, int nbParams, final List<String> listNames) throws NumberFormatException, IOException {
		 String line;
		 String[] tempArr;
		 while ((line = br.readLine()) != null) {
			 tempArr = line.split(",");
			 Map<String, Object> temp_map = new LinkedHashMap<>();
			 for (int idx = 0; idx < tempArr.length; idx++) {
				 String var = listNames.get(idx);
				 if (idx < nbParams) {
					 temp_map.put(var, tempArr[idx]);
				 } else {
					 outputs.get(var).add(Double.parseDouble(tempArr[idx].toString()));
				 }
			 }
			 simulationSamples.add(temp_map);
		 }
	}

	 /**
	  * Read the first line of the csv used to create the Morris class and use it to fill parameterNames and outputs.
	  * It returns the list of column names.
	  * @param br the BufferedReader used to read the csv file
	  * @param nbParams the number parameters
	  * @return the list of names of every column
	  * @throws IOException
	  */
	 private List<String> readMorrisCsvHeader(final BufferedReader br, int nbParams) throws IOException {
		 String line = br.readLine();
		 String[] tempArr;
		 List<String> listNames = new ArrayList<>();
		 if (line!= null) {
			 tempArr = line.split(",");
			 for (int idx = 0; idx < tempArr.length; idx++) {
				 listNames.add(tempArr[idx]);
				 if (idx < nbParams) {
					 parametersNames.add(tempArr[idx]);
				 } else {
					 outputs.put(tempArr[idx], new ArrayList<>());
				 }
			 }
		 }
		 return listNames;
		 
	}

	/**
	  * When using Morris within an experiment, the class is created, sampling is made and then Gama runs simulations
	  * then results is given back to Morris class for analysis. This setter is meant to do that !
	  * 
	  * @param outputs
	  * @param scope
	  */
	 public void setOutputs(Map<String, List<Double>> outputs, IScope scope) {
		 if (simulationSamples == null || simulationSamples.isEmpty()) { 
			 GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] Cannot setup outputs before the morris sample", scope), true); 
		 }
		 this.outputs = outputs;
	 }

	/** ########################## ANALYSIS METHODS ############################# */

	/**
	 * Main method for Morris Analysis
	 *
	 * @param num_levels:
	 *            Number of levels used for the sampling (Usually 4)
	 * @param Outputs:
	 *            List of the Output to analyze
	 *
	 */

	public List<Map<String, Map<String, Double>>> evaluate() {

		List<Map<String, Double>> MySampleTemp = new ArrayList<>();
		simulationSamples.forEach(m -> {
			Map<String, Double> maptmp = new LinkedHashMap<>();
			IntStream.range(0, parametersNames.size()).forEach(i -> {
				Object o = m.get(parametersNames.get(i));
				if (Objects.equals(o.toString(), "false")) {
					maptmp.put(parametersNames.get(i), 0.0);
				} else if (Objects.equals(o.toString(), "true")) {
					maptmp.put(parametersNames.get(i), 1.0);
				} else {
					maptmp.put(parametersNames.get(i), Double.parseDouble(o.toString()));
				}
			});
			MySampleTemp.add(maptmp);
		});

		double delta = this.nblevels / (2.0 * ((double) this.nblevels - 1));
		int num_vars = MySampleTemp.get(0).size();
		int num_trajectories;
		int trajectory_size;
		
		num_trajectories = (int) Math.round(simulationSamples.size() / (num_vars + 1.0));
		trajectory_size = (int) Math.round(simulationSamples.size() / (double)num_trajectories);
		
		for (var entry : outputs.entrySet()) {
			List<Map<String, Double>> elementary_effects =
					compute_elementary_effects(MySampleTemp, entry.getValue(), trajectory_size, delta, parametersNames, simulationSamples);
			Map<String, List<Double>> elementary = transformListMapToMapList(elementary_effects, parametersNames);
			Map<String, Double> mu = new LinkedHashMap<>();
			IntStream.range(0, parametersNames.size()).forEach(i -> {
				double val = 0;
				List<Double> listtmp = elementary.get(parametersNames.get(i));
				for (Double aDouble : listtmp) { val = val + aDouble; }
				mu.put(parametersNames.get(i), val / listtmp.size());
			});
			this.mu.put(entry.getKey(), mu);
			
			Map<String, Double> mu_star = new LinkedHashMap<>();
			IntStream.range(0, parametersNames.size()).forEach(i -> {
				double val = 0;
				List<Double> listtmp = elementary.get(parametersNames.get(i));
				for (Double aDouble : listtmp) { val = val + abs(aDouble); }
				mu_star.put(parametersNames.get(i), val / listtmp.size());
			});
			this.mu_star.put(entry.getKey(), mu_star);
			
			Map<String, Double> sigma = new LinkedHashMap<>();
			IntStream.range(0, parametersNames.size()).forEach(i -> {
				double val = 0;
				List<Double> listtmp = elementary.get(parametersNames.get(i));
				for (Double aDouble : listtmp) { val = val + pow(aDouble - mu.get(parametersNames.get(i)), 2); }
				val = Math.sqrt(val / (listtmp.size() - 1));
				sigma.put(parametersNames.get(i), val);
			});
			this.sigma.put(entry.getKey(), sigma);
		}
		
		return List.of(mu,mu_star,sigma);
	}
	
	/** ########################## SAVING/LOADING METHODS ############################# */

	public String buildReportString(final String extension) {
		
		if ("txt".equalsIgnoreCase(extension)) {
			return buildTextReportString();			
		} else {
			return buildOtherReportString();
		}
	}
	
	private String buildOtherReportString() {
		StringBuilder sb = new StringBuilder();
		char sep = ',';
		// Build header
		sb.append("output").append(sep);
		sb.append("parameter").append(sep); 
		sb.append("\u00B5").append(sep);
		sb.append("\u00B5").append("*").append(sep);
		sb.append("\u03C3").append(Strings.LN);
		
		for (String output_name : outputs.keySet()) {
			for (String param : parametersNames) {
				// The output & parameter
				sb.append(output_name).append(sep);
				sb.append(param).append(sep);
				sb.append(mu.get(output_name).get(param)).append(sep);
				sb.append(mu_star.get(output_name).get(param)).append(sep);
				sb.append(sigma.get(output_name).get(param)).append(Strings.LN);
			}
		}

		return sb.toString();
	}

	/** ########################## MORRIS GAMA RE-IMPLEMENTATION ############################# */
	
	private String buildTextReportString() {
		StringBuilder sb = new StringBuilder();

		sb.append("MORRIS ANALYSIS :").append(Strings.LN);
		for (Entry<String, List<Double>> o : outputs.entrySet()) {
			
			sb.append(Strings.LN);
			sb.append("Result for output :" + o).append(Strings.LN);
			Map<String, Double> _mu = mu.get(o.getKey());
			sb.append("\u00B5 :").append(Strings.LN); 
			for (String n : _mu.keySet()) {
				sb.append("\t").append(n).append(" : ")
					.append(_mu.get(n)).append(Strings.LN);
			}
			
			Map<String, Double> _mu_star = mu_star.get(o.getKey());
			sb.append("\u00B5 * :").append(Strings.LN);
			for (String n : _mu_star.keySet()) {
				sb.append("\t").append(n).append(" : ")
					.append(_mu_star.get(n)).append(Strings.LN);
			}
			
			Map<String, Double> _sigma = sigma.get(o.getKey());
			sb.append("\u03C3 :").append(Strings.LN);
			for (String n : _sigma.keySet()) {
				sb.append("\t").append(n).append(" : ")
					.append(_sigma.get(n)).append(Strings.LN);
			}
		}
		return sb.toString();
	}

	/**
	 * Convert a List of Map into a Map of List
	 *
	 * @param ListMap
	 *            a list of map
	 * @return A map of list
	 */
	private Map<String, List<Double>> transformListMapToMapList(final List<Map<String, Double>> ListMap,
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
	private List<Map<String, Double>> calc_results_difference(final List<Map<String, Double>> result_up,
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
	private List<Map<String, Double>> reorganize_output_matrix(final List<List<Double>> Outputs,
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
	private List<Map<String, Double>> compute_elementary_effects(final List<Map<String, Double>> MySampleTemp,
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

}
