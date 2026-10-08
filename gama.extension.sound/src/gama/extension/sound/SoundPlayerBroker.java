/*******************************************************************************************************
 *
 * SoundPlayerBroker.java, in gama.extension.sound, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;

/**
 * The Class SoundPlayerBroker.
 *
 * <p>
 * Manages a shared pool of {@link GamaSoundPlayer} instances and maps them to agents within simulations.
 * </p>
 *
 * <h3>Thread-safety design</h3>
 * <ul>
 * <li>The singleton is created via the <em>initialization-on-demand holder</em> idiom — no locking on
 * {@link #getInstance()} and no volatile field.</li>
 * <li>The per-simulation player map uses a {@link ConcurrentHashMap}, so reads from multiple threads
 * never contend.</li>
 * <li>A single {@link ReentrantLock} ({@code lock}) serialises the <em>two-collection
 * compound operations</em> that simultaneously inspect the player map and modify the pool queue.
 * Using one lock instead of two prevents the lock-ordering deadlock that was present in the previous
 * design (outer=soundPlayerOfAgents, inner=soundPlayerPools vs. the reverse).</li>
 * <li>The player pool itself is an {@link ArrayBlockingQueue}, which is inherently thread-safe for
 * individual offer/poll operations; the lock is only taken when the operation spans both the map
 * and the queue atomically.</li>
 * </ul>
 */
public class SoundPlayerBroker {

	// the maximum number of BasicPlayer instances can only be 2. Increasing this
	/** The Constant MAX_NB_OF_MUSIC_PLAYERS. */
	// number will raise errors.
	private static final int MAX_NB_OF_MUSIC_PLAYERS = 2;

	/**
	 * The pool of available (reusable) sound players. {@link ArrayBlockingQueue} is thread-safe for
	 * individual offer/poll calls.
	 */
	private final ArrayBlockingQueue<GamaSoundPlayer> soundPlayerPool =
			new ArrayBlockingQueue<>(MAX_NB_OF_MUSIC_PLAYERS);

	/**
	 * Maps each simulation to the map of its agents' current players.
	 * {@link ConcurrentHashMap} is used so that read access from different simulations never blocks.
	 */
	private static final Map<ISimulationAgent, Map<IAgent, GamaSoundPlayer>> soundPlayerOfAgents =
			new ConcurrentHashMap<>();

	/**
	 * Single lock that guards all compound operations touching both {@link #soundPlayerPool} and
	 * {@link #soundPlayerOfAgents} at the same time. Using a dedicated, fine-grained lock (rather than
	 * {@code synchronized(this)}) keeps the critical sections explicit and avoids accidental reentrancy.
	 */
	private final ReentrantLock lock = new ReentrantLock();

	// -------------------------------------------------------------------------
	// Singleton — initialization-on-demand holder idiom
	// -------------------------------------------------------------------------

	/** Private holder class; loaded (and the instance created) only when first referenced. */
	private static final class InstanceHolder {
		/** The single instance of the broker. */
		static final SoundPlayerBroker INSTANCE = new SoundPlayerBroker();
	}

	/**
	 * Gets the single instance of SoundPlayerBroker. Lock-free after the first call.
	 *
	 * @return single instance of SoundPlayerBroker
	 */
	public static SoundPlayerBroker getInstance() {
		return InstanceHolder.INSTANCE;
	}

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Instantiates a new sound player broker and fills the player pool.
	 */
	private SoundPlayerBroker() {
		initializePool();
	}

	/**
	 * Fills the player pool with fresh {@link GamaSoundPlayer} instances up to {@link #MAX_NB_OF_MUSIC_PLAYERS}.
	 * Must be called with {@link #lock} held or from the constructor (before the instance is published).
	 */
	private void initializePool() {
		for (int i = 0; i < MAX_NB_OF_MUSIC_PLAYERS; i++) { soundPlayerPool.offer(new GamaSoundPlayer()); }
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Returns the {@link GamaSoundPlayer} currently associated with the given agent, or assigns one from
	 * the pool if none is registered yet.
	 *
	 * @param agent
	 *            the agent requesting a sound player
	 * @return the player assigned to {@code agent}, or {@code null} if the pool is exhausted
	 */
	public GamaSoundPlayer getSoundPlayer(final IAgent agent) {
		final IScope scope = agent.getScope();
		final ISimulationAgent simulation = scope.getSimulation();

		// ConcurrentHashMap.computeIfAbsent is atomic at the simulation level — no lock needed here.
		final Map<IAgent, GamaSoundPlayer> playersOfSimulation =
				soundPlayerOfAgents.computeIfAbsent(simulation, sim -> {
					final Map<IAgent, GamaSoundPlayer> map = new ConcurrentHashMap<>();
					// Register life-cycle callbacks once, inside computeIfAbsent (executed at most once per sim).
					simulation.postEndAction(scope1 -> {
						getInstance().manageMusicPlayers(simulation);
						return null;
					});
					simulation.postDisposeAction(scope1 -> {
						getInstance().schedulerDisposed(simulation);
						return null;
					});
					return map;
				});

		// Fast path: agent already has a player.
		GamaSoundPlayer player = playersOfSimulation.get(agent);
		if (player != null) return player;

		// Slow path: poll a player from the pool and register it atomically.
		// A single lock guards the compound check-then-act on both collections.
		lock.lock();
		try {
			// Re-check inside the lock to avoid a TOCTOU race.
			player = playersOfSimulation.get(agent);
			if (player != null) return player;

			player = soundPlayerPool.poll(); // non-blocking
			if (player != null) { playersOfSimulation.put(agent, player); }
		} finally {
			lock.unlock();
		}
		return player;
	}

	/**
	 * Called at the end of each simulation cycle to reclaim players from dead agents and agents whose
	 * player has finished playing.
	 *
	 * @param simulation
	 *            the simulation whose players should be checked
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public void manageMusicPlayers(final ISimulationAgent simulation) throws GamaRuntimeException {
		final Map<IAgent, GamaSoundPlayer> playersOfSimulation = soundPlayerOfAgents.get(simulation);
		if (playersOfSimulation == null) return;

		final List<IAgent> toReclaim = new ArrayList<>();
		for (final Map.Entry<IAgent, GamaSoundPlayer> entry : playersOfSimulation.entrySet()) {
			if (entry.getKey().dead() || entry.getValue().canBeReused()) { toReclaim.add(entry.getKey()); }
		}

		if (toReclaim.isEmpty()) return;

		lock.lock();
		try {
			for (final IAgent a : toReclaim) {
				final GamaSoundPlayer player = playersOfSimulation.remove(a);
				if (player != null) {
					if (a.dead()) { player.stop(a.getScope(), true); }
					soundPlayerPool.offer(new GamaSoundPlayer());
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Called when a simulation is disposed. Stops all its players, returns them to the pool, and removes
	 * the simulation entry from the map.
	 *
	 * @param simulation
	 *            the simulation being disposed
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public void schedulerDisposed(final ISimulationAgent simulation) throws GamaRuntimeException {
		final Map<IAgent, GamaSoundPlayer> playersOfSimulation = soundPlayerOfAgents.remove(simulation);
		if (playersOfSimulation == null) return;

		lock.lock();
		try {
			for (final GamaSoundPlayer player : playersOfSimulation.values()) {
				player.stop(simulation.getScope(), true);
			}
			playersOfSimulation.clear();
			// Reset pool to a clean state.
			soundPlayerPool.clear();
			initializePool();
		} finally {
			lock.unlock();
		}
	}
}
