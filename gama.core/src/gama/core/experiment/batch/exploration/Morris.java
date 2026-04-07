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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/**
 * The Class Morris. This class performs a robust Morris sensitivity analysis (Elementary Effects method).
 *
 * mu: mean of the distribution (overall influence) mu_star: mean of the absolute values (ranking of importance) sigma:
 * standard deviation (interactions and non-linearities)
 */
public class Morris {

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
	private final List<Map<String, Object>> simulationSamples;

	/** The parameters names. */
	private final List<String> parametersNames;

	/** The nblevels. */
	private final int nblevels;

	/** The outputs. */
	private Map<String, List<Double>> outputs;

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
		this.simulationSamples = new ArrayList<>();
		this.parametersNames = new ArrayList<>();
	}

	/**
	 * Instantiates a new morris with samples and levels.
	 */
	public Morris(final List<Map<String, Object>> samples, final int nblevels) {
		this.nblevels = nblevels;
		if (samples == null || samples.isEmpty())
			throw new IllegalArgumentException("Simulation samples cannot be empty");
		this.simulationSamples = new ArrayList<>(samples);
		this.parametersNames = new ArrayList<>(samples.get(0).keySet());
	}

	/**
	 * Build a morris problem from a map of data (columns)
	 */
	public Morris(final Map<String, ? extends List<?>> data, final int nbParams, final int nblevels,
			final IScope scope) {
		this(nblevels);
		if (data == null || data.isEmpty()) throw new IllegalArgumentException("Input data map cannot be empty");

		int nbRows = data.values().iterator().next().size();
		List<String> allKeys = new ArrayList<>(data.keySet());

		for (int i = 0; i < allKeys.size(); i++) {
			String name = allKeys.get(i);
			if (i < nbParams) { parametersNames.add(name); }
		}

		for (int r = 0; r < nbRows; r++) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (String name : allKeys) { row.put(name, Cast.asFloat(scope, data.get(name).get(r))); }
			simulationSamples.add(row);
		}
	}

	/**
	 * Build a morris problem from a CSV file.
	 */
	public Morris(final File file, final int nbParams, final int nblevels, final IScope scope) {
		this(nblevels);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String header = br.readLine();
			if (header == null) return;
			String[] cols = header.split(",");
			for (int i = 0; i < nbParams; i++) { parametersNames.add(cols[i].trim()); }

			String line;
			while ((line = br.readLine()) != null) {
				String[] vals = line.split(",");
				Map<String, Object> row = new LinkedHashMap<>();
				for (int i = 0; i < cols.length; i++) { row.put(cols[i].trim(), Double.parseDouble(vals[i].trim())); }
				simulationSamples.add(row);
			}
		} catch (IOException | NumberFormatException e) {
			throw GamaRuntimeException.error("Failed to load Morris CSV: " + e.getMessage(), scope);
		}
	}

	/**
	 * Sets the outputs.
	 */
	public void setOutputs(final Map<String, List<Double>> outputs, final IScope scope) { this.outputs = outputs; }

	/**
	 * Main evaluation method.
	 */
	public MorrisResult evaluate() {
		if (simulationSamples.isEmpty()) throw new IllegalStateException("No simulation samples provided.");
		if (outputs == null || outputs.isEmpty()) throw new IllegalStateException("No outputs provided for analysis.");

		int k = parametersNames.size();
		int trajectorySize = k + 1;
		if (simulationSamples.size() % trajectorySize != 0) throw new IllegalStateException(
				"Sample size (" + simulationSamples.size() + ") is not a multiple of (k+1)=" + trajectorySize);
		int r = simulationSamples.size() / trajectorySize;

		MorrisResult results = new MorrisResult();

		for (var entry : outputs.entrySet()) {
			String outName = entry.getKey();
			List<Double> y = entry.getValue();
			if (y.size() != simulationSamples.size()) throw new IllegalStateException(
					"Output list '" + outName + "' size mismatch. Expected " + simulationSamples.size() + " but got " + y.size());

			Map<String, List<Double>> eeMap = new HashMap<>();
			for (String name : parametersNames) { eeMap.put(name, new ArrayList<>()); }

			// Walk trajectories
			for (int t = 0; t < r; t++) {
				for (int step = 0; step < k; step++) {
					int i1 = t * trajectorySize + step;
					int i2 = i1 + 1;

					Map<String, Object> p1 = simulationSamples.get(i1);
					Map<String, Object> p2 = simulationSamples.get(i2);

					// Validate exactly one change
					String changedParam = null;
					double deltaX = 0;
					for (String name : parametersNames) {
						double v1 = toDouble(p1.get(name));
						double v2 = toDouble(p2.get(name));
						if (abs(v1 - v2) > 1e-10) {
							if (changedParam != null) throw new IllegalStateException(
									"Invalid Morris trajectory at index " + i1 + ": Multiple parameters changed.");
							changedParam = name;
							deltaX = v2 - v1;
						}
					}
					if (changedParam == null) throw new IllegalStateException(
							"Invalid Morris trajectory at index " + i1 + ": No parameters changed.");

					double ee = (y.get(i2) - y.get(i1)) / deltaX;
					eeMap.get(changedParam).add(ee);
				}
			}

			// Compute Stats
			Map<String, Double> muMap = new LinkedHashMap<>();
			Map<String, Double> muStarMap = new LinkedHashMap<>();
			Map<String, Double> sigmaMap = new LinkedHashMap<>();

			for (String name : parametersNames) {
				List<Double> ees = eeMap.get(name);
				double sum = 0;
				double sumAbs = 0;
				for (double e : ees) {
					sum += e;
					sumAbs += abs(e);
				}
				double mean = sum / r;
				muMap.put(name, mean);
				muStarMap.put(name, sumAbs / r);

				double varSum = 0;
				for (double e : ees) { varSum += pow(e - mean, 2); }
				sigmaMap.put(name, r > 1 ? sqrt(varSum / (r - 1)) : 0.0);
			}
			results.mu.put(outName, muMap);
			results.muStar.put(outName, muStarMap);
			results.sigma.put(outName, sigmaMap);
		}
		// For backward compatibility with internal GAMA calls if needed
		this.mu = results.mu;
		this.mu_star = results.muStar;
		this.sigma = results.sigma;

		return results;
	}

	/**
	 * Convert object to double robustly.
	 */
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
			for (String o : mu.keySet()) {
				sb.append(StringUtils.LN).append("Result for output: ").append(o).append(StringUtils.LN);
				sb.append("\u00B5:").append(StringUtils.LN);
				mu.get(o).forEach((n, v) -> sb.append("\t").append(n).append(" : ").append(v).append(StringUtils.LN));
				sb.append("\u00B5*:").append(StringUtils.LN);
				mu_star.get(o).forEach((n, v) -> sb.append("\t").append(n).append(" : ").append(v).append(StringUtils.LN));
				sb.append("\u03C3:").append(StringUtils.LN);
				sigma.get(o).forEach((n, v) -> sb.append("\t").append(n).append(" : ").append(v).append(StringUtils.LN));
			}
		} else {
			sb.append("output,parameter,\u00B5,\u00B5*,\u03C3").append(StringUtils.LN);
			for (String o : mu.keySet()) {
				for (String p : parametersNames) {
					sb.append(o).append(",").append(p).append(",").append(mu.get(o).get(p)).append(",")
							.append(mu_star.get(o).get(p)).append(",").append(sigma.get(o).get(p))
							.append(StringUtils.LN);
				}
			}
		}
		return sb.toString();
	}
}
