/*******************************************************************************************************
 *
 * Morris.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment.batch.exploration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;

/**
 * The Class Morris.
 */
public class Morris {

	/** The default number of levels for Morris screening */
	public static final int DEFAULT_LEVELS = 4;

	/** The default number of trajectories */
	public static final int DEFAULT_TRAJECTORIES = 10;

	/** The simulation samples. */
	private List<Map<String, Object>> simulationSamples;

	/** The parameters names. */
	private List<String> parametersNames;

	/** The outputs. */
	private Map<String, List<Double>> outputs;

	/** The elementary effects. */
	private Map<String, List<Double>> elementaryEffects;

	/** The p. */
	private final int p;

	/** The delta. */
	private final double delta;

	/**
	 * Instantiates a new morris.
	 *
	 * @param p
	 *            the p
	 */
	public Morris(final int p) {
		this.p = p;
		this.delta = p / (2.0 * (p - 1));
	}

	/**
	 * Instantiates a new morris.
	 *
	 * @param samples
	 *            the samples
	 * @param p
	 *            the p
	 */
	public Morris(final List<Map<String, Object>> samples, final int p) {
		this(p);
		this.simulationSamples = samples;
		this.parametersNames = new ArrayList<>(samples.get(0).keySet());
		this.outputs = new LinkedHashMap<>();
	}

	/**
	 * Build a morris problem from a map of data (columns)
	 *
	 * @param data
	 *            : map containing columns (parameters then outputs)
	 * @param nbParams
	 *            : number of input parameter of the simulation
	 * @param nblevels
	 *            : the number of level used for the Morris sample
	 * @param scope
	 */
	public Morris(final Map<String, ? extends List<?>> data, final int nbParams, final int nblevels, final IScope scope) {
		this(nblevels);
		this.simulationSamples = new ArrayList<>();
		this.parametersNames = new ArrayList<>();
		this.outputs = new LinkedHashMap<>();

		int nbCols = data.size();
		int nbRows = data.values().iterator().next().size();
		List<String> listNames = new ArrayList<>(data.keySet());

		for (int idx = 0; idx < nbCols; idx++) {
			String name = listNames.get(idx);
			if (idx < nbParams) {
				parametersNames.add(name);
			} else {
				outputs.put(name, new ArrayList<>());
			}
		}

		for (int row = 0; row < nbRows; row++) {
			Map<String, Object> temp_map = new LinkedHashMap<>();
			for (int idx = 0; idx < nbCols; idx++) {
				String name = listNames.get(idx);
				Double val = Cast.asFloat(scope, data.get(name).get(row));
				if (idx < nbParams) {
					temp_map.put(name, val);
				} else {
					outputs.get(name).add(val);
				}
			}
			simulationSamples.add(temp_map);
		}
	}

	/**
	 * File should be a csv file with ',' separator with given properties:
	 * </p>
	 * 1) each line is a simulation </br>
	 * 2) each colomn is either a input or an output of the simulation </br>
	 * 3) all inputs should be first in a row (i.e. from column 0 to n-1, 'n' being the number of parameters) </br>
	 * 4) all outputs should follow at the end of the row (i.e. from column n to the end) </br>
	 * 5) First row should be the corresponding name of inputs and outputs
	 * </p>
	 *
	 * @param file
	 *            : the file containing I/O of all simulations
	 * @param nbParams
	 *            : the number of input parameter of the simulation
	 * @param nblevels
	 *            : the number of level used for the Morris sample
	 * @param scope
	 */
	public Morris(final File file, final int nbParams, final int nblevels, final IScope scope) {
		this(nblevels);
		this.simulationSamples = new ArrayList<>();
		this.parametersNames = new ArrayList<>();
		this.outputs = new LinkedHashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();
			String[] columnsNames = line.split(",");

			for (int i = 0; i < nbParams; i++) { parametersNames.add(columnsNames[i]); }
			for (int i = nbParams; i < columnsNames.length; i++) { outputs.put(columnsNames[i], new ArrayList<>()); }

			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				Map<String, Object> temp_map = new LinkedHashMap<>();
				for (int i = 0; i < nbParams; i++) { temp_map.put(columnsNames[i], Double.parseDouble(values[i])); }
				for (int i = nbParams; i < columnsNames.length; i++) {
					outputs.get(columnsNames[i]).add(Double.parseDouble(values[i]));
				}
				simulationSamples.add(temp_map);
			}
		} catch (IOException e) {
			throw GamaRuntimeException.error("File " + file.toString() + " not found", scope);
		}
	}

	/**
	 * Sets the outputs.
	 *
	 * @param outputs
	 *            the outputs
	 * @param scope
	 *            the scope
	 */
	public void setOutputs(final Map<String, List<Double>> outputs, final IScope scope) { this.outputs = outputs; }

	/**
	 * Evaluate the elementary effects for each variable.
	 */
	public void evaluate() {
		elementaryEffects = new LinkedHashMap<>();
		outputs.keySet().stream().forEach(o -> elementaryEffects.put(o, computeElementaryEffects(o)));
	}

	/**
	 * Builds the report string.
	 *
	 * @param extension
	 *            the extension
	 * @return the string
	 */
	public String buildReportString(final String extension) {
		StringBuilder sb = new StringBuilder();
		if ("txt".equalsIgnoreCase(extension)) {
			sb.append("MORRIS ANALYSIS:").append(System.lineSeparator());
			for (String outputName : elementaryEffects.keySet()) {
				sb.append("##############################").append(System.lineSeparator());
				sb.append("output variable : " + outputName).append(System.lineSeparator());
				sb.append("-------------------").append(System.lineSeparator());
				List<Double> ee = elementaryEffects.get(outputName);
				for (int i = 0; i < parametersNames.size(); i++) {
					sb.append(parametersNames.get(i) + " : " + ee.get(i)).append(System.lineSeparator());
				}
			}
		} else {
			sb.append("output,parameter,elementary effect").append(System.lineSeparator());
			for (String outputName : elementaryEffects.keySet()) {
				List<Double> ee = elementaryEffects.get(outputName);
				for (int i = 0; i < parametersNames.size(); i++) {
					sb.append(outputName + "," + parametersNames.get(i) + "," + ee.get(i))
							.append(System.lineSeparator());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Compute elementary effects.
	 *
	 * @param outputName
	 *            the output name
	 * @return the list
	 */
	private List<Double> computeElementaryEffects(final String outputName) {
		List<Double> elementary_effects = new ArrayList<>();
		List<Double> outputValues = outputs.get(outputName);

		for (int i = 0; i < parametersNames.size(); i++) {
			double sum = 0;
			int count = 0;
			for (int j = 0; j < simulationSamples.size(); j++) {
				for (int k = j + 1; k < simulationSamples.size(); k++) {
					if (isOneStepApart(simulationSamples.get(j), simulationSamples.get(k), i)) {
						double diffOutput = outputValues.get(k) - outputValues.get(j);
						double diffInput = (Double) simulationSamples.get(k).get(parametersNames.get(i))
								- (Double) simulationSamples.get(j).get(parametersNames.get(i));
						sum += Math.abs(diffOutput / diffInput);
						count++;
					}
				}
			}
			elementary_effects.add(count == 0 ? 0 : sum / count);
		}
		return elementary_effects;
	}

	/**
	 * Checks if is one step apart.
	 *
	 * @param m1
	 *            the m 1
	 * @param m2
	 *            the m 2
	 * @param index
	 *            the index
	 * @return true, if is one step apart
	 */
	private boolean isOneStepApart(final Map<String, Object> m1, final Map<String, Object> m2, final int index) {
		for (int i = 0; i < parametersNames.size(); i++) {
			String name = parametersNames.get(i);
			if (i == index) {
				if (Math.abs((Double) m1.get(name) - (Double) m2.get(name)) < 0.000001) return false;
			} else {
				if (Math.abs((Double) m1.get(name) - (Double) m2.get(name)) > 0.000001) return false;
			}
		}
		return true;
	}

}
