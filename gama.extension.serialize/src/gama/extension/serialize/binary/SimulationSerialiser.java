/*******************************************************************************************************
 *
 * SimulationSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.util.LinkedList;

import gama.api.constants.ISerialisationConstants;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.simulation.IExperimentRecorder;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.dev.DEBUG;
import gama.extension.serialize.binary.SimulationHistory.SimulationHistoryNode;

/**
 * The Class SimulationSerialiser. Used to record, store, and retrieve simulation states
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
public class SimulationSerialiser implements IExperimentRecorder, ISerialisationConstants {

	static {
		DEBUG.OFF();
	}

	/** The processor. */
	final BinarySerialiser processor = new BinarySerialiser();

	/**
	 * Record.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @date 8 août 2023
	 */
	@Override
	public void record(final ISimulationAgent sim) {
		try {
			byte[] state = processor.saveObjectToBytes(sim.getScope(), sim);
			SimulationHistory history = getSimulationHistory(sim);
			history.push(state, sim.getClock().getCycle());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the simulation history.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @return the simulation history
	 * @date 22 oct. 2023
	 */
	private SimulationHistory getSimulationHistory(final ISimulationAgent sim) {
		SimulationHistory history = (SimulationHistory) sim.getAttribute(SerialisedAgent.HISTORY_KEY);
		if (history == null) {
			history = new SimulationHistory();
			sim.setAttribute(SerialisedAgent.HISTORY_KEY, history);
		}
		return history;
	}

	/**
	 * Restore.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @date 8 août 2023
	 */
	@Override
	public void restore(final ISimulationAgent sim) {
		try {
			synchronized (sim) {
				LinkedList<SimulationHistoryNode> history = getSimulationHistory(sim);
				SimulationHistoryNode node = history.pop();
				if (node != null && node.cycle() == sim.getClock().getCycle()) { node = history.pop(); }
				if (node != null) {
					// long startTime = System.nanoTime();
					processor.restoreAgentFromBytes(sim, node.bytes());
					// DEBUG.OUT("Deserialised in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) +
					// "ms");
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Can step back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 * @return true, if successful
	 * @date 9 août 2023
	 */
	@Override
	public boolean canStepBack(final ISimulationAgent sim) {
		return getSimulationHistory(sim).size() > 0;
	}

}
