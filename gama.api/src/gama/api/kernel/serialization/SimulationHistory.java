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
package gama.api.kernel.serialization;

import java.util.LinkedList;

import gama.api.kernel.serialization.SimulationHistory.SimulationHistoryNode;
import gama.dev.DEBUG;

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

	/**
	 * Pushes a recorded state on top of the history. Synchronous, so that it stays covered by the per-simulation lock
	 * held by {@link SimulationRecorder#record} and {@link SimulationRecorder#restore}.
	 *
	 * @param state
	 *            the serialised simulation state
	 * @param cycle
	 *            the cycle this state corresponds to
	 */
	public void push(final byte[] state, final int cycle) {
		push(new SimulationHistoryNode(state, cycle));
	}

}
