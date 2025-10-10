/*******************************************************************************************************
 *
 * Benchmark.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.benchmark;

import java.util.concurrent.ConcurrentHashMap;

import gama.core.common.interfaces.IBenchmarkable;
import gama.core.kernel.experiment.IExperimentSpecies;
import gama.core.runtime.IScope;
import gama.core.runtime.benchmark.Benchmark.ScopeRecord;

/**
 * The Class Benchmark.
 */
public class Benchmark extends ConcurrentHashMap<IScope, ScopeRecord> {

	/**
	 * The Class ScopeRecord.
	 */
	public static class ScopeRecord extends ConcurrentHashMap<IBenchmarkable, BenchmarkRecord> {

		/** The own record. */
		final BenchmarkRecord ownRecord;

		/**
		 * Instantiates a new scope record.
		 *
		 * @param scope
		 *            the scope
		 */
		public ScopeRecord(final IScope scope) {
			ownRecord = new BenchmarkRecord(scope);
		}

		/**
		 * Find.
		 *
		 * @param object
		 *            the object
		 * @return the benchmark record
		 */
		public BenchmarkRecord find(final IBenchmarkable object) {
			return computeIfAbsent(object, BenchmarkRecord::new);
		}

		/**
		 * Gets the stop watch for.
		 *
		 * @param desc
		 *            the desc
		 * @return the stop watch for
		 */
		public StopWatch getStopWatchFor(final IBenchmarkable desc) {
			return new StopWatch(ownRecord, find(desc));
		}

	}

	/** The tree. */
	public final BenchmarkTree tree;

	/**
	 * Instantiates a new benchmark.
	 *
	 * @param experiment
	 *            the experiment
	 */
	public Benchmark(final IExperimentSpecies experiment) {
		tree = new BenchmarkTree(experiment.getModel().getDescription(), experiment.getDescription());
	}

	/**
	 * Record.
	 *
	 * @param scope
	 *            the scope
	 * @param symbol
	 *            the symbol
	 * @return the stop watch
	 */
	public StopWatch record(final IScope scope, final IBenchmarkable symbol) {
		return computeIfAbsent(scope, ScopeRecord::new).getStopWatchFor(symbol).start();
	}

	/**
	 * Save and dispose.
	 *
	 * @param experiment
	 *            the experiment
	 */
	public void saveAndDispose(final IExperimentSpecies experiment) {
		new BenchmarkConsolePrinter().print(this);
		new BenchmarkCSVExporter().save(experiment, this);
		tree.dispose();
		clear();
	}

}
