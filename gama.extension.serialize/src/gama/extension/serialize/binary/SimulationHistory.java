/*******************************************************************************************************
 *
 * SimulationHistory.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gama.dev.DEBUG;
import gama.extension.serialize.binary.SimulationHistory.SimulationHistoryNode;

/**
 * The Class SimulationHistory.
 */
public class SimulationHistory extends LinkedList<SimulationHistoryNode> {

	/**
	 * The Record SimulationHistoryNode.
	 */
	static record SimulationHistoryNode(byte[] bytes, long cycle) {}

	static {
		DEBUG.OFF();
	}

	/** The executor. */
	final ExecutorService executor = Executors.newCachedThreadPool();

	public void push(final byte[] state, final int cycle) {
		executor.execute(() -> { push(new SimulationHistoryNode(state, cycle)); });
	}

}
