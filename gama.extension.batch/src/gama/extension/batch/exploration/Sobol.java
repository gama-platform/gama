/*******************************************************************************************************
 *
 * Sobol.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.moeaframework.util.sequence.Saltelli;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/**
 * The Class Sobol.
 */
public final class Sobol {

	/** The list of Saltelli indices [0; 1] */
	private double[][] saltelli;

	/** The sample. */
	private int sample;

	/** The sample. */
	private int _sample;

	/** The resample. */
	protected int _resample = 1000; // Bootstraping for confidence interval

	/** Map describing the problem : pair of min and max value for each parameters */
	private Map<String, List<Object>> problem;

	/** Map giving the values of each parameters */
	private final Map<String, List<Object>> parameters;

	/** Output name */
	private final List<String> output_names;

	/** Map of outputs values */
	private Map<String, List<Object>> outputs;

	/**
	 * sobol indexes for each variable <br>
	 * - K1 the output name <br>
	 * - K2 the variable name <br>
	 * - V list of first order index, first order confidence, second order index, second order confidence
	 */
	private final Map<String, Map<String, List<Double>>> sobol_analysis = new HashMap<>();

	/**
	 * Scope for GamaRunTimeException
	 */
	private final IScope scope;

	/**
	 * Build a Sobol element corresponding to the problem
	 */
	public Sobol(final LinkedHashMap<String, List<Object>> problem, final List<String> output_names, final int sample,
			final IScope scope) {
		this.scope = scope;
		this.problem = problem;
		this.parameters = new LinkedHashMap<>();
		problem.keySet().stream().forEach(p -> this.parameters.put(p, new ArrayList<>()));
		this.output_names = output_names;
		this.outputs = new HashMap<>();

		this.sample = sample;
		this._sample = sample * (2 * parameters.size() + 2);
	}

	/**
	 * Build a sobol problem from a map of data (columns)
	 */
	public Sobol(final Map<String, List<Double>> data, final int nb_parameters, final IScope scope) {
		this.scope = scope;
		this.parameters = new LinkedHashMap<>();
		this.output_names = new ArrayList<>();
		this.outputs = new HashMap<>();

		int i = 0;
		for (Entry<String, List<Double>> entry : data.entrySet()) {
			String name = entry.getKey();
			List<Object> values = new ArrayList<>(entry.getValue());

			if (i < nb_parameters) {
				this.parameters.put(name, values);
			} else {
				this.output_names.add(name);
				this.outputs.put(name, values);
			}
			i++;
		}

		_sample = this.parameters.values().iterator().next().size();
		if (_sample % (2 * nb_parameters + 2) != 0) throw new IllegalArgumentException(
				"Number of sample in the data doesn't match the number of parameters");
		sample = _sample / (2 * nb_parameters + 2);
	}

	/**
	 * Build a sobol problem from a .csv file.
	 */
	public Sobol(final File f, final int nb_parameters, final IScope scope) throws GamaRuntimeException {
		this.scope = scope;
		this.parameters = new LinkedHashMap<>();
		this.output_names = new ArrayList<>();
		this.outputs = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
			String line = br.readLine();
			if (line == null) return;

			String columns_names[] = parseCsvLine(line);
			for (int i = 0; i < nb_parameters; i++) { parameters.put(columns_names[i].trim(), new ArrayList<>()); }
			for (int i = nb_parameters; i < columns_names.length; i++) {
				output_names.add(columns_names[i].trim());
				this.outputs.put(columns_names[i].trim(), new ArrayList<>());
			}

			this._sample = 0;
			int rowIdx = 1;
			while ((line = br.readLine()) != null) {
				rowIdx++;
				if (line.trim().isEmpty()) continue;
				this._sample++;
				String values[] = parseCsvLine(line);
				if (values.length != columns_names.length) {
					throw new IOException("Row " + rowIdx + " has " + values.length + " columns, expected " + columns_names.length);
				}
				for (int i = 0; i < nb_parameters; i++) {
					String val = values[i].trim();
					try {
						this.parameters.get(columns_names[i].trim()).add(Double.parseDouble(val));
					} catch (NumberFormatException e) {
						throw new IOException("Invalid number '" + val + "' at row " + rowIdx + ", column " + columns_names[i]);
					}
				}
				for (int i = nb_parameters; i < columns_names.length; i++) {
					String val = values[i].trim();
					try {
						this.outputs.get(columns_names[i].trim()).add(Double.parseDouble(val));
					} catch (NumberFormatException e) {
						throw new IOException("Invalid number '" + val + "' at row " + rowIdx + ", column " + columns_names[i]);
					}
				}
			}
			if (_sample % (2 * nb_parameters + 2) != 0) throw new IllegalArgumentException(
					"Number of sample in the file doesn't match the number of parameters");
			sample = _sample / (2 * nb_parameters + 2);

		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[SOBOL] Load Error: " + e.getMessage(), scope), true);
		}
	}

	/**
	 * Simple robust CSV line parser handling quotes.
	 */
	private String[] parseCsvLine(String line) {
		List<String> result = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '\"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				result.add(cur.toString());
				cur.setLength(0);
			} else {
				cur.append(c);
			}
		}
		result.add(cur.toString());
		return result.toArray(new String[0]);
	}

	/**
	 * Generate a sample using a random Saltelli SAMPLING
	 */
	public void setRandomSaltelliSampling() {
		saltelli = new Saltelli().generate(_sample, parameters.size());
		sample();
	}

	/**
	 * Generate a sample using the provided .csv file.
	 */
	public void setSaltelliSamplingFromCsv(final File file) {
		parseSaltelli(file);
		sample();
	}

	/**
	 * Save the Saltelli sample.
	 */
	public void saveSaltelliSample(final File file) throws GamaRuntimeException {
		try (FileWriter fw = new FileWriter(file, false)) {
			fw.write(buildSaltelliReport());
		} catch (IOException e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Failed to save Saltelli sample: " + e.getMessage(), scope), true);
		}
	}

	public Map<String, List<Object>> getParametersValues() { return this.parameters; }

	public void setOutputs(final Map<String, List<Object>> outputs) {
		for (String output : outputs.keySet()) {
			if (outputs.get(output).size() != _sample) throw GamaRuntimeException.error(
					"This size of the output " + output + " doesn't match the number of samples in the parameters",
					scope);
		}
		this.outputs = outputs;
	}

	/**
	 * Evaluate the sobol indices
	 */
	public Map<String, Map<String, List<Double>>> evaluate() {
		if (outputs.isEmpty()) { System.err.println("no output porivded call setOutputs before calling evaluate"); }

		double[] A = new double[sample];
		double[] B = new double[sample];
		double[][] C_A = new double[sample][this.parameters.size()];

		for (String output : outputs.keySet()) {
			Iterator<Object> it = outputs.get(output).iterator();
			Map<String, List<Double>> sobolIndexes_output = new HashMap<>();

			for (int i = 0; i < sample; i++) {
				A[i] = Double.parseDouble(it.next().toString());
				for (int j = 0; j < this.parameters.size(); j++) {
					C_A[i][j] = Double.parseDouble(it.next().toString());
				}
				for (int j = 0; j < this.parameters.size(); j++) {
					it.next();
				}
				B[i] = Double.parseDouble(it.next().toString());
			}

			int j = 0;
			for (String param : parameters.keySet()) {
				List<Double> sobolIndexes = new ArrayList<>();
				double[] a0 = new double[sample];
				double[] a1 = new double[sample];
				double[] a2 = new double[sample];

				for (int i = 0; i < sample; i++) {
					a0[i] = A[i];
					a1[i] = C_A[i][j];
					a2[i] = B[i];
				}

				sobolIndexes.add(computeFirstOrder(a0, a1, a2, sample));
				sobolIndexes.add(computeFirstOrderConfidence(a0, a1, a2, sample, _resample));
				sobolIndexes.add(computeTotalOrder(a0, a1, a2, sample));
				sobolIndexes.add(computeTotalOrderConfidence(a0, a1, a2, sample, _resample));

				sobolIndexes_output.put(param, sobolIndexes);
				j++;
			}
			sobol_analysis.put(output, sobolIndexes_output);
		}

		return sobol_analysis;
	}

	public void saveResult(final File file) throws GamaRuntimeException {
		try (FileWriter fw = new FileWriter(file, false)) {
			fw.write(this.buildReportString(FilenameUtils.getExtension(file.getPath())));
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Failed to save Sobol results: " + e.getMessage(), scope), true);
		}
	}

	private void sample() {
		for (int i = 0; i < _sample; i++) {
			int j = 0;
			for (String param : parameters.keySet()) {
				roll(param, saltelli[i][j], problem.get(param));
				j++;
			}
		}
	}

	public String buildReportString(final String extension) {
		StringBuilder sb = new StringBuilder();
		char sep = ',';

		if ("txt".equalsIgnoreCase(extension)) {
			sb.append("SOBOL ANALYSIS:\n");
			for (String output_name : sobol_analysis.keySet()) {
				sb.append("##############################\n");
				sb.append("output variable : " + output_name).append(StringUtils.LN);
				sb.append("-------------------").append(StringUtils.LN);
				for (String param : sobol_analysis.get(output_name).keySet()) {
					sb.append(param + " : \n");
					sb.append("first order : ");
					sb.append(sobol_analysis.get(output_name).get(param).get(0)).append(StringUtils.LN);
					sb.append("first order confidence : ");
					sb.append(sobol_analysis.get(output_name).get(param).get(1)).append(StringUtils.LN);
					sb.append("Total order : ");
					sb.append(sobol_analysis.get(output_name).get(param).get(2)).append(StringUtils.LN);
					sb.append("Total order confidence : ");
					sb.append(sobol_analysis.get(output_name).get(param).get(3)).append(StringUtils.LN);
					sb.append("-------------------").append(StringUtils.LN);
				}
			}
		} else {
			sb.append("output,parameter,first order,first order confidence,Total order,Total order confidence").append(StringUtils.LN);
			for (String output_name : sobol_analysis.keySet()) {
				for (String param : sobol_analysis.get(output_name).keySet()) {
					sb.append(output_name).append(sep).append(param);
					for (Double indices : sobol_analysis.get(output_name).get(param)) {
						sb.append(sep).append(indices);
					}
					sb.append(StringUtils.LN);
				}
			}
		}
		return sb.toString();
	}

	private String buildSaltelliReport() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this._sample; i++) {
			for (int j = 0; j < parameters.size(); j++) {
				sb.append(saltelli[i][j]);
				if (j != parameters.size() - 1) { sb.append(", "); }
			}
			if (i != this._sample - 1) { sb.append(System.lineSeparator()); }
		}
		return sb.toString();
	}

	private void parseSaltelli(final File file) throws GamaRuntimeException {
		saltelli = new double[this._sample][parameters.size()];
		try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			int sIdx = 0;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				for (int i = 0; i < values.length; i++) { saltelli[sIdx][i] = Double.parseDouble(values[i].trim()); }
				sIdx++;
			}
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Failed to parse Saltelli file: " + e.getMessage(), scope), true);
		}
	}

	private void roll(final String param, final Double saltelli, final List<Object> info) {
		Object val = null;
		if (info.stream().allMatch(Double.class::isInstance)) {
			Double min = (Double) info.get(0);
			Double max = (Double) info.get(1);
			val = min + saltelli * (max - min);
		} else if (info.stream().allMatch(Integer.class::isInstance)) {
			int min = (int) info.get(0);
			int max = (int) info.get(1);
			val = (int) Math.floor(min + saltelli * (max - min));
		} else if (info.stream().allMatch(Boolean.class::isInstance)) {
			val = saltelli > 0.5;
		} else if (info.size() > 2) {
			int n = (int) Math.floor(saltelli * info.size());
			val = info.get(n);
		} else
			throw GamaRuntimeException.error("Uknown type for " + param + " : " + info.toString(), scope);
		parameters.get(param).add(val);
	}

	private double computeFirstOrderConfidence(final double[] a0, final double[] a1, final double[] a2,
			final int nsample, final int nresample) {
		double[] b0 = new double[nsample];
		double[] b1 = new double[nsample];
		double[] b2 = new double[nsample];
		double[] s = new double[nresample];
		for (int i = 0; i < nresample; i++) {
			for (int j = 0; j < nsample; j++) {
				int index = scope.getRandom().getGenerator().nextInt(nsample);
				b0[j] = a0[index]; b1[j] = a1[index]; b2[j] = a2[index];
			}
			s[i] = computeFirstOrder(b0, b1, b2, nsample);
		}
		double ss = Arrays.stream(s).sum() / nresample;
		double sss = 0.0;
		for (int i = 0; i < nresample; i++) { sss += Math.pow(s[i] - ss, 2.0); }
		return 1.96 * Math.sqrt(sss / (nresample - 1));
	}

	private double computeFirstOrder(final double[] a0, final double[] a1, final double[] a2, final int nsample) {
		double c = 0.0;
		for (int i = 0; i < nsample; i++) { c += a0[i]; }
		c /= nsample;
		double tmp1 = 0.0, tmp2 = 0.0, tmp3 = 0.0, EY2 = 0.0;
		for (int i = 0; i < nsample; i++) {
			EY2 += (a0[i] - c) * (a2[i] - c);
			tmp1 += (a2[i] - c) * (a2[i] - c);
			tmp2 += a2[i] - c;
			tmp3 += (a1[i] - c) * (a2[i] - c);
		}
		EY2 /= nsample;
		double V = tmp1 / (nsample - 1) - Math.pow(tmp2 / nsample, 2.0);
		double U = tmp3 / (nsample - 1);
		return (U - EY2) / V;
	}

	private double computeTotalOrder(final double[] a0, final double[] a1, final double[] a2, final int nsample) {
		double c = 0.0;
		for (int i = 0; i < nsample; i++) { c += a0[i]; }
		c /= nsample;
		double tmp1 = 0.0, tmp2 = 0.0, tmp3 = 0.0;
		for (int i = 0; i < nsample; i++) {
			tmp1 += (a0[i] - c) * (a0[i] - c);
			tmp2 += (a0[i] - c) * (a1[i] - c);
			tmp3 += a0[i] - c;
		}
		double EY2 = Math.pow(tmp3 / nsample, 2.0);
		double V = tmp1 / (nsample - 1) - EY2;
		double U = tmp2 / (nsample - 1);
		return 1.0 - (U - EY2) / V;
	}

	private double computeTotalOrderConfidence(final double[] a0, final double[] a1, final double[] a2,
			final int nsample, final int nresample) {
		double[] b0 = new double[nsample];
		double[] b1 = new double[nsample];
		double[] b2 = new double[nsample];
		double[] s = new double[nresample];
		for (int i = 0; i < nresample; i++) {
			for (int j = 0; j < nsample; j++) {
				int index = scope.getRandom().getGenerator().nextInt(nsample);
				b0[j] = a0[index]; b1[j] = a1[index]; b2[j] = a2[index];
			}
			s[i] = computeTotalOrder(b0, b1, b2, nsample);
		}
		double ss = Arrays.stream(s).sum() / nresample;
		double sss = 0.0;
		for (int i = 0; i < nresample; i++) { sss += Math.pow(s[i] - ss, 2.0); }
		return 1.96 * Math.sqrt(sss / (nresample - 1));
	}
}
