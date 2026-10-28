/*******************************************************************************************************
 *
 * BenchmarkCSVExporter.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.benchmark;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.collections.GamaNode;
import gama.api.utils.collections.GamaTree.Order;
import gama.api.utils.csv.CsvWriter;
import gama.api.utils.files.FileUtils;

/**
 * The Class BenchmarkCSVExporter.
 */
public class BenchmarkCSVExporter {

	/** The Constant exportFolder. */
	private static final String exportFolder = "benchmarks";

	/**
	 * Save.
	 *
	 * @param experiment
	 *            the experiment
	 * @param records
	 *            the records
	 */
	public void save(final IExperimentSpecies experiment, final Benchmark records) throws GamaRuntimeException {
		final IScope scope = experiment.getExperimentScope();
		try {
			FileUtils.createFolder(scope, exportFolder);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("Impossible to create folder " + exportFolder);
			GAMA.reportError(scope, e1, false);
			e1.printStackTrace();
			return;
		}
		final String exportFilePrefix = exportFolder + "/" + experiment.getModel().getName() + "_benchmark_"
				+ Instant.now().toString().replace(':', '_');

		final List<Map.Entry<IScope, Benchmark.ScopeRecord>> scopeRecords = new ArrayList<>(records.entrySet());
		scopeRecords.sort((a, b) -> a.getKey().getNameForBenchmarks().compareTo(b.getKey().getNameForBenchmarks()));
		for (int i = 0; i < scopeRecords.size(); i++) {
			final Map.Entry<IScope, Benchmark.ScopeRecord> scopeRecord = scopeRecords.get(i);
			final String contextName = safeFileName(scopeRecord.getKey().getNameForBenchmarks());
			final String contextFileName = FileUtils.constructAbsoluteFilePath(scope,
					exportFilePrefix + "_" + (i + 1) + "_" + contextName + ".csv", false);
			saveContext(scope, records, scopeRecord.getValue(), contextFileName);
		}
	}

	private void saveContext(final IScope scope, final Benchmark records, final Benchmark.ScopeRecord scopeRecord,
			final String exportFileName) throws GamaRuntimeException {
		final List<GamaNode<IBenchmarkable>> rows = records.tree.list(Order.PRE_ORDER).stream()
				.filter(node -> isModelStatement(node.getData()))
				.filter(node -> {
					final BenchmarkRecord record = scopeRecord.get(node.getData());
					return record != null && !record.isUnrecorded();
				})
				.sorted((a, b) -> Long.compare(scopeRecord.get(b.getData()).milliseconds.longValue(),
						scopeRecord.get(a.getData()).milliseconds.longValue()))
				.toList();

		try (final CsvWriter writer = new CsvWriter(exportFileName)) {
			writer.setDelimiter(';');
			writer.writeRecord(new String[] { "Model statement", "Time in ms", "Invocations" });
			for (final GamaNode<IBenchmarkable> row : rows) {
				final IBenchmarkable element = row.getData();
				final BenchmarkRecord record = scopeRecord.get(element);
				writer.writeRecord(new String[] { getElementName(element), String.valueOf(record.milliseconds),
						String.valueOf(record.times) });
			}
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	private boolean isModelStatement(final IBenchmarkable element) {
		if (!(element instanceof final IDescription description) || description.isBuiltIn()) return false;
		return switch (description.getKind()) {
			case BEHAVIOR, SINGLE_STATEMENT, SEQUENCE_STATEMENT -> true;
			default -> false;
		};
	}

	private String getElementName(final IBenchmarkable element) {
		final IDescription description = (IDescription) element;
		final String name = description.getNameForBenchmarks();
		final String descriptionName = description.getKeyword().matches("-\\d+") ? description.getName() : name;
		return "statement: " + descriptionName;
	}

	private String safeFileName(final String name) {
		final String safeName = name.replaceAll("[^A-Za-z0-9._-]", "_");
		if (safeName.isBlank()) return "context";
		return safeName.length() > 80 ? safeName.substring(0, 80) : safeName;
	}

}
