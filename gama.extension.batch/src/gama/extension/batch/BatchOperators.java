/*******************************************************************************************************
 *
 * BatchOperators.java, in gama.extension.batch, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.utils.files.FileUtils;
import gama.extension.batch.exploration.Morris;
import gama.extension.batch.exploration.Sobol;
import gama.extension.batch.exploration.Stochanalysis;

/**
 * The Class BatchOperators. Provides GAML operators for sensitivity and stochasticity analysis.
 */
public class BatchOperators {

	/**
	 * Performs a Sobol sensitivity analysis.
	 */
	@operator (
			value = "sobolAnalysis",
			type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return a string containing the Report of the sobol analysis for the corresponding data (path, map or matrix) and save this report in a txt/csv file.")
	public static String sobolAnalysis(final IScope scope, final Object data, final String report_path,
			final int nb_parameters) {
		final File f_report = new File(FileUtils.constructAbsoluteFilePath(scope, report_path, false));
		Sobol sob;
		if (data instanceof String path) {
			final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path, false));
			sob = new Sobol(f, nb_parameters, scope);
		} else if (data instanceof IMap map) {
			sob = new Sobol(convertToDoubleMap(scope, map), nb_parameters, scope);
		} else if (data instanceof IMatrix matrix) {
			sob = new Sobol(matrixToMap(scope, matrix), nb_parameters, scope);
		} else
			throw GamaRuntimeException.error("sobolAnalysis expects a path (string), a map or a matrix", scope);

		sob.evaluate();
		sob.saveResult(f_report);
		return sob.buildReportString(FilenameUtils.getExtension(f_report.getPath()));
	}

	/**
	 * Performs a Morris sensitivity analysis.
	 */
	@operator (
			value = "morrisAnalysis",
			type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return a string containing the Report of the morris analysis for the corresponding data (path, map or matrix)")
	public static String morrisAnalysis(final IScope scope, final Object data, final int nb_levels,
			final int nb_parameters) {
		Morris momo;
		String ext = "csv";
		if (data instanceof String path) {
			final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path, false));
			momo = new Morris(f, nb_parameters, nb_levels, scope);
			ext = FilenameUtils.getExtension(path);
		} else if (data instanceof IMap map) {
			momo = new Morris(convertToDoubleMap(scope, map), nb_parameters, nb_levels, scope);
		} else if (data instanceof IMatrix matrix) {
			momo = new Morris(matrixToMap(scope, matrix), nb_parameters, nb_levels, scope);
		} else
			throw GamaRuntimeException.error("morrisAnalysis expects a path (string), a map or a matrix", scope);

		momo.evaluate();
		return momo.buildReportString(ext);
	}

	/**
	 * Performs a stochasticity analysis.
	 */
	@operator (
			value = "stochanalyse",
			type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the result of the stochasticity analysis for the corresponding data (path, map or matrix)")
	public static String stochanalyse(final IScope scope, final int replicat, final double threshold, final Object data,
			final int nb_parameters) {

		if (data instanceof String path) {
			String new_path = scope.getExperiment().getWorkingPath() + "/" + path;
			return Stochanalysis.stochasticityAnalysis_From_CSV(replicat, threshold, new_path, nb_parameters, scope);
		}

		IMap<String, IList<Double>> mapData;
		if (data instanceof IMap m) {
			mapData = m;
		} else if (data instanceof IMatrix matrix) {
			mapData = matrixToMap(scope, matrix);
		} else
			throw GamaRuntimeException.error("stochanalyse expects a path (string), a map or a matrix", scope);

		int nbCols = mapData.size();
		int nbRows = mapData.values().iterator().next().size();
		List<String> listNames = new ArrayList<>(mapData.keySet());

		IList<IMap<String, Object>> MySample = GamaListFactory.create(Types.MAP);
		IMap<String, IList<Double>> Outputs = GamaMapFactory.create();

		for (int idx = 0; idx < nbCols; idx++) {
			String name = listNames.get(idx);
			if (idx >= nb_parameters) { Outputs.put(name, GamaListFactory.create()); }
		}

		for (int row = 0; row < nbRows; row++) {
			IMap<String, Object> temp_map = GamaMapFactory.create();
			for (int idx = 0; idx < nbCols; idx++) {
				String name = listNames.get(idx);
				Double val = Cast.asFloat(scope, mapData.get(name).get(row));
				if (idx < nb_parameters) {
					temp_map.put(name, val);
				} else {
					Outputs.get(name).add(val);
				}
			}
			MySample.add(temp_map);
		}

		return Stochanalysis.stochasticityAnalysis_From_Data(replicat, threshold, MySample, Outputs, scope);
	}

	@operator (
			value = "rolling_vc",
			type = IType.LIST,
			content_type = IType.FLOAT,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the list of rolling coefficient of variance according to the number of observations, </br> i.e. value at index i is the coefficient of variance for the first i observations.")
	public static IList<Double> rollingVC(final IScope scope, final IList<Double> data) {
		return Stochanalysis.coefficientOfVariance(scope, data);
	}

	@operator (
			value = "rolling_se",
			type = IType.LIST,
			content_type = IType.FLOAT,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the list of standard error according to the number of observations, </br> i.e. value at index i is the standard error for the first i observations.")
	public static IList<Double> rollingSE(final IScope scope, final IList<Double> data) {
		return Stochanalysis.standardError(scope, data);
	}

	@operator (
			value = "power_test",
			type = IType.INT,
			can_be_const = true,
			category = { IOperatorCategory.STATISTICAL },
			concept = { IConcept.STATISTIC })
	@doc (
			value = "Return the number of observation to satisfy power test given a critical effect size, tAlpha and tBeta."
					+ "</br>see reference: https://rseri.me/publication/b016/B016.pdf (accessible as of 04/2026).")
	public static Integer powerTestCSE(final IScope scope, final IList<Double> data, final double tAlpha,
			final double tBeta, final double criticalEffectSize) {
		return Stochanalysis.ces(scope, data, tAlpha, tBeta, criticalEffectSize);
	}

	/**
	 * Helper to convert matrix to map of columns.
	 */
	private static IMap<String, IList<Double>> matrixToMap(final IScope scope, final IMatrix matrix) {
		IMap<String, IList<Double>> map = GamaMapFactory.create();
		for (int j = 0; j < matrix.getCols(scope); j++) {
			IList<Double> col = GamaListFactory.create(Types.FLOAT);
			for (int i = 0; i < matrix.getRows(scope); i++) { col.add(Cast.asFloat(scope, matrix.get(scope, j, i))); }
			map.put("col" + j, col);
		}
		return map;
	}

	/**
	 * Helper to convert a GAML map to a rigid Map<String, List<Double>>.
	 */
	private static Map<String, List<Double>> convertToDoubleMap(final IScope scope, final IMap<?, ?> map) {
		if (map == null || map.isEmpty()) throw GamaRuntimeException.error("Data map is empty or null", scope);
		Map<String, List<Double>> result = new LinkedHashMap<>();
		map.forEach((k, v) -> {
			if (v instanceof List<?> list) {
				List<Double> doubles = new ArrayList<>();
				for (Object o : list) { doubles.add(Cast.asFloat(scope, o)); }
				result.put(Cast.asString(scope, k), doubles);
			}
		});
		return result;
	}
}
