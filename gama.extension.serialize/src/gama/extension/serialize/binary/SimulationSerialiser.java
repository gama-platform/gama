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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import gama.api.constants.ISerialisationConstants;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.simulation.IExperimentRecorder;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.dev.DEBUG;
import gama.extension.serialize.binary.SimulationHistory.SimulationHistoryNode;

/**
 * The Class SimulationSerialiser. Used to record, store, and retrieve simulation states.
 *
 * <p>
 * <b>Thread safety:</b> {@link #record(ISimulationAgent)} and {@link #restore(ISimulationAgent)} may be
 * called from different threads (the scheduler thread and the UI step-back thread respectively). A
 * per-simulation {@link ReentrantLock}, stored in {@link #simulationLocks}, ensures that the two
 * operations are mutually exclusive for the same simulation while allowing parallelism across different
 * simulations.
 * </p>
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
	 * Per-simulation locks. Allows {@link #record} and {@link #restore} to be mutually exclusive for the
	 * same simulation without blocking operations on unrelated simulations. Entries are lazily created via
	 * {@link ConcurrentHashMap#computeIfAbsent}.
	 */
	private final ConcurrentHashMap<ISimulationAgent, ReentrantLock> simulationLocks = new ConcurrentHashMap<>();

	/**
	 * Returns (creating if necessary) the lock associated with the given simulation.
	 *
	 * @param sim
	 *            the simulation agent
	 * @return the per-simulation lock
	 */
	private ReentrantLock lockFor(final ISimulationAgent sim) {
		return simulationLocks.computeIfAbsent(sim, s -> new ReentrantLock());
	}

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
		final ReentrantLock l = lockFor(sim);
		l.lock();
		try {
			byte[] state = processor.saveObjectToBytes(sim.getScope(), sim);
			SimulationHistory history = getSimulationHistory(sim);
			history.push(state, sim.getClock().getCycle());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			l.unlock();
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
		final ReentrantLock l = lockFor(sim);
		l.lock();
		try {
			LinkedList<SimulationHistoryNode> history = getSimulationHistory(sim);
			SimulationHistoryNode node = history.pop();
			if (node != null && node.cycle() == sim.getClock().getCycle()) { node = history.pop(); }
			if (node != null) {
				processor.restoreAgentFromBytes(sim, node.bytes());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			l.unlock();
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
