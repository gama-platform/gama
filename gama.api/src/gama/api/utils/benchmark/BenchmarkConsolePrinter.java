/*******************************************************************************************************
 *
 * BenchmarkConsolePrinter.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.benchmark;

import gama.api.utils.StringUtils;
import gama.api.utils.collections.GamaTree.Order;
import gama.dev.DEBUG;

/**
 * The Class BenchmarkConsolePrinter.
 */
public class BenchmarkConsolePrinter {

	/** The Constant S. */
	static final String S = "\n------------------------------------------------\n";

	/**
	 * Prints the.
	 *
	 * @param scopes
	 *            the scopes
	 */
	public void print(final Benchmark scopes) {
		final StringBuilder sb = new StringBuilder();
		scopes.forEach((s, r) -> {
			final BenchmarkRecord sr = r.ownRecord;
			sb.append(S).append(sr.object.getNameForBenchmarks()).append(" (").append(sr.milliseconds).append(" ms)")
					.append(S);
			scopes.tree.visit(Order.PRE_ORDER, n -> {
				final BenchmarkRecord br = r.find(n.getData());
				if (br != BenchmarkRecord.NULL && !br.isUnrecorded()) {
					sb.append(StringUtils.LN)
							.append(String.format("%30s", "[" + br.milliseconds + " ms, " + br.times + " calls] "));
					for (int i = 0; i < n.getWeight(); i++) { sb.append("-" + StringUtils.TAB); }
					sb.append(' ').append(br.object.getNameForBenchmarks());

				}
			});
			sb.append(StringUtils.LN);
		});
		DEBUG.LOG(sb.toString());
	}

}
