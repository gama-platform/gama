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

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/**
 * The Class Morris. This class performs a robust Morris sensitivity analysis (Elementary Effects method).
 *
 * mu: mean of the distribution (overall influence) mu_star: mean of the absolute values (ranking of importance) sigma:
 * standard deviation (interactions and non-linearities)
 *
 * @author tomroy
 */
public final class Morris {

	/**
	 * Data class to hold Morris analysis results.
	 */
	public static class MorrisResult {

		/** The mu. */
		public final Map<String, Map<String, Double>> mu = new HashMap<>();

		/** The mu star. */
		public final Map<String, Map<String, Double>> muStar = new HashMap<>();

		/** The sigma. */
		public final Map<String, Map<String, Double>> sigma = new HashMap<>();
	}

	/** The default number of levels for Morris screening */
	public static final int DEFAULT_LEVELS = 4;

	/** The default number of trajectories */
	public static final int DEFAULT_TRAJECTORIES = 10;

	/** The simulation samples. */
	protected final List<Map<String, Object>> simulationSamples = new ArrayList<>();

	/** The parameters names. */
	protected final List<String> parametersNames = new ArrayList<>();

	/** The nblevels. */
	protected final int nblevels;

	/** The execution scope. */
	private IScope scope;

	/** The outputs. */
	private Map<String, List<Double>> outputs = new LinkedHashMap<>();

	/** The results: mu (mean) */
	protected Map<String, Map<String, Double>> mu;

	/** The results: mu_star (ranking) */
	protected Map<String, Map<String, Double>> mu_star;

	/** The results: sigma (interaction) */
	protected Map<String, Map<String, Double>> sigma;

	/**
	 * Instantiates a new morris with a number of levels.
	 */
	public Morris(final int nblevels) {
		this.nblevels = nblevels;
	}

	/**
	 * Instantiates a new morris with samples and levels.
	 */
	public Morris(final List<Map<String, Object>> samples, final int nblevels) {
		this.nblevels = nblevels;
		if (samples != null && !samples.isEmpty()) {
			this.simulationSamples.addAll(samples);
			this.parametersNames.addAll(samples.get(0).keySet());
		}
	}

	/**
	 * Build a morris problem from a map of data (columns). Data MUST be an ordered map (e.g. LinkedHashMap) to ensure
	 * correct parameter/output separation.
	 */
	public Morris(final Map<String, List<Double>> data, final int nbParams, final int nblevels, final IScope scope) {
		this(nblevels);
		this.scope = scope;
		if (data == null || data.isEmpty()) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] Input data map cannot be empty", scope), true);
			return;
		}

		int nbRows = data.values().iterator().next().size();
		List<String> allKeys = new ArrayList<>(data.keySet());

		for (int i = 0; i < allKeys.size(); i++) {
			String name = allKeys.get(i);
			if (i < nbParams) {
				parametersNames.add(name);
			} else {
				outputs.put(name, new ArrayList<>());
			}
		}

		for (int r = 0; r < nbRows; r++) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < allKeys.size(); i++) {
				String name = allKeys.get(i);
				Double val = data.get(name).get(r);
				row.put(name, val);
				if (i >= nbParams) { outputs.get(name).add(val); }
			}
			simulationSamples.add(row);
		}
	}

	/**
	 * Build a morris problem from a CSV file.
	 */
	public Morris(final File file, final int nbParams, final int nblevels, final IScope scope) {
		this(nblevels);
		this.scope = scope;
		try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String header = br.readLine();
			if (header == null) return;
			String[] cols = parseCsvLine(header);
			
			for (int i = 0; i < cols.length; i++) {
				String name = cols[i].trim();
				if (i < nbParams) {
					parametersNames.add(name);
				} else {
					outputs.put(name, new ArrayList<>());
				}
			}

			String line;
			int rowIdx = 1;
			while ((line = br.readLine()) != null) {
				rowIdx++;
				if (line.trim().isEmpty()) continue;
				String[] vals = parseCsvLine(line);
				if (vals.length != cols.length) {
					throw new IOException("Row " + rowIdx + " has " + vals.length + " columns, expected " + cols.length);
				}
				Map<String, Object> row = new LinkedHashMap<>();
				for (int i = 0; i < cols.length; i++) {
					String name = cols[i].trim();
					double val;
					try {
						val = Double.parseDouble(vals[i].trim());
					} catch (NumberFormatException e) {
						throw new IOException("Invalid number '" + vals[i] + "' in column '" + name + "' at row " + rowIdx);
					}
					row.put(name, val);
					if (i >= nbParams) { outputs.get(name).add(val); }
				}
				simulationSamples.add(row);
			}
		} catch (IOException | NumberFormatException e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] Failed to load Morris CSV: " + e.getMessage(), scope), true);
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
	 * Sets the outputs.
	 */
	public void setOutputs(final Map<String, List<Double>> outputs, final IScope scope) {
		this.scope = scope;
		this.outputs = outputs;
	}

	/**
	 * Main evaluation method.
	 */
	public MorrisResult evaluate() {
		if (simulationSamples.isEmpty()) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] No simulation samples provided.", scope), true);
		}
		if (outputs == null || outputs.isEmpty()) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] No outputs provided for analysis.", scope), true);
		}

		int k = parametersNames.size();
		int trajectorySize = k + 1;
		if (simulationSamples.size() % trajectorySize != 0) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] Sample size (" + simulationSamples.size() + ") is not a multiple of (k+1)=" + trajectorySize, scope), true);
		}
		int numTrajectories = simulationSamples.size() / trajectorySize;

		// Theoretical Morris step size delta = p / (2*(p-1))
		double expectedDelta = (double) nblevels / (2.0 * (nblevels - 1.0));

		// Compute parameter ranges for normalization
		Map<String, Double> mins = new HashMap<>();
		Map<String, Double> maxs = new HashMap<>();
		for (String name : parametersNames) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (Map<String, Object> sample : simulationSamples) {
				double v = toDouble(sample.get(name));
				if (v < min) min = v;
				if (v > max) max = v;
			}
			mins.put(name, min);
			maxs.put(name, max);
		}

		MorrisResult results = new MorrisResult();

		for (var entry : outputs.entrySet()) {
			String outName = entry.getKey();
			List<Double> y = entry.getValue();
			if (y.size() != simulationSamples.size()) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] Output list '" + outName + "' size mismatch.", scope), true);
			}

			Map<String, List<Double>> eeMap = new HashMap<>();
			for (String name : parametersNames) { eeMap.put(name, new ArrayList<>()); }

			// Walk trajectories and compute Elementary Effects (EE)
			for (int t = 0; t < numTrajectories; t++) {
				for (int step = 0; step < k; step++) {
					int i1 = t * trajectorySize + step;
					int i2 = i1 + 1;

					Map<String, Object> p1 = simulationSamples.get(i1);
					Map<String, Object> p2 = simulationSamples.get(i2);

					String changedParam = null;
					double deltaX = 0;
					for (String name : parametersNames) {
						double v1 = toDouble(p1.get(name));
						double v2 = toDouble(p2.get(name));
						if (abs(v1 - v2) > 1e-10) {
							if (changedParam != null) {
								GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] Multiple parameters changed at index " + i1, scope), true);
							}
							changedParam = name;
							double range = maxs.get(name) - mins.get(name);
							deltaX = range == 0 ? (v2 - v1) : (v2 - v1) / range;
						}
					}
					if (changedParam == null) {
						GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("[MORRIS] No parameters changed at index " + i1, scope), true);
					}

					// Strict Morris delta check
					if (abs(abs(deltaX) - expectedDelta) > 1e-5) {
						// Custom warns could go here
					}

					double ee = (y.get(i2) - y.get(i1)) / deltaX;
					eeMap.get(changedParam).add(ee);
				}
			}

			// Compute Statistics
			Map<String, Double> muMap = new LinkedHashMap<>();
			Map<String, Double> muStarMap = new LinkedHashMap<>();
			Map<String, Double> sigmaMap = new LinkedHashMap<>();

			for (String name : parametersNames) {
				List<Double> ees = eeMap.get(name);
				if (ees.isEmpty()) {
					muMap.put(name, 0.0); muStarMap.put(name, 0.0); sigmaMap.put(name, 0.0);
					continue;
				}
				double sum = 0, sumAbs = 0;
				for (double e : ees) { sum += e; sumAbs += abs(e); }
				
				int n_ee = ees.size();
				double mean = sum / n_ee;
				muMap.put(name, mean);
				muStarMap.put(name, sumAbs / n_ee);

				double varSum = 0;
				for (double e : ees) varSum += pow(e - mean, 2);
				sigmaMap.put(name, n_ee > 1 ? sqrt(varSum / (n_ee - 1)) : 0.0);
			}
			results.mu.put(outName, muMap);
			results.muStar.put(outName, muStarMap);
			results.sigma.put(outName, sigmaMap);
		}
		
		this.mu = results.mu;
		this.mu_star = results.muStar;
		this.sigma = results.sigma;

		return results;
	}

	private double toDouble(final Object o) {
		if (o instanceof Number n) return n.doubleValue();
		if (o instanceof Boolean b) return b ? 1.0 : 0.0;
		return Double.parseDouble(o.toString());
	}

	/**
	 * Builds the report string.
	 */
	public String buildReportString(final String extension) {
		StringBuilder sb = new StringBuilder();
		if ("txt".equalsIgnoreCase(extension)) {
			sb.append("MORRIS ANALYSIS:").append(StringUtils.LN);
			if (mu != null) {
				for (String o : mu.keySet()) {
					sb.append(StringUtils.LN).append("Result for output: ").append(o).append(StringUtils.LN);
					sb.append("\u00B5:").append(StringUtils.LN);
					mu.get(o).forEach((n, v) -> sb.append("\t").append(n).append(" : ").append(v).append(StringUtils.LN));
					sb.append("\u00B5*:").append(StringUtils.LN);
					mu_star.get(o).forEach((n, v) -> sb.append("\t").append(n).append(" : ").append(v).append(StringUtils.LN));
					sb.append("\u03C3:").append(StringUtils.LN);
					sigma.get(o).forEach((n, v) -> sb.append("\t").append(n).append(" : ").append(v).append(StringUtils.LN));
				}
			}
		} else {
			sb.append("output,parameter,\u00B5,\u00B5*,\u03C3").append(StringUtils.LN);
			if (mu != null) {
				for (String o : mu.keySet()) {
					for (String p : parametersNames) {
						sb.append(o).append(",").append(p).append(",").append(mu.get(o).get(p)).append(",")
								.append(mu_star.get(o).get(p)).append(",").append(sigma.get(o).get(p))
								.append(StringUtils.LN);
					}
				}
			}
		}
		return sb.toString();
	}
}
