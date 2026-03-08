/*******************************************************************************************************
 *
 * AgentSpliterator.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import gama.api.kernel.agent.IAgent;
import gama.api.types.geometry.IShape;

/**
 * A custom {@link Spliterator} implementation for efficiently partitioning collections of agents for parallel
 * processing in GAMA simulations.
 * 
 * <p>
 * AgentSpliterator is designed to support parallel execution of agent-based operations by splitting agent collections
 * into smaller chunks that can be processed concurrently. It implements a threshold-based splitting strategy where
 * collections are only split if they exceed a configurable threshold size, preventing unnecessary overhead for small
 * populations.
 * </p>
 * 
 * <p>
 * Key characteristics:
 * </p>
 * <ul>
 * <li>CONCURRENT: Supports concurrent access during iteration</li>
 * <li>ORDERED: Maintains the original ordering of agents</li>
 * <li>IMMUTABLE: The underlying agent array cannot be modified</li>
 * <li>SIZED: The size is known and reported accurately</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * // Create a spliterator for an agent array with threshold of 20
 * Spliterator&lt;IAgent&gt; spliterator = AgentSpliterator.of(agentArray, 20);
 * 
 * // Use with parallel streams
 * StreamSupport.stream(spliterator, true).forEach(agent -&gt; {
 * 	// Process agent in parallel
 * });
 * </pre>
 * 
 * @see Spliterator
 * @see ParallelAgentRunner
 * @see GamaExecutorService
 */
public class AgentSpliterator implements Spliterator<IAgent> {

	/**
	 * Creates a spliterator from an array of agents with a specified splitting threshold.
	 * 
	 * <p>
	 * This factory method creates an AgentSpliterator that will partition the agent array for parallel processing. The
	 * threshold determines the minimum size at which splits will occur - if the remaining elements are fewer than the
	 * threshold, no further splitting will happen.
	 * </p>
	 * 
	 * @param agents
	 *            the array of IShape objects (typically agents) to be processed. If null or empty, returns an empty
	 *            spliterator
	 * @param threshold
	 *            the minimum number of agents required for the spliterator to split further. Smaller values enable
	 *            more parallelism but may incur overhead; typical values range from 10-100
	 * @return a Spliterator that can partition the agents for parallel processing, or an empty spliterator if the
	 *         array is null or empty
	 */
	public static Spliterator<IAgent> of(final IShape[] agents, final int threshold) {
		if (agents == null || agents.length == 0) { return Spliterators.<IAgent> emptySpliterator(); }
		return new AgentSpliterator(agents, 0, agents.length, threshold);
	}

	/**
	 * Creates a spliterator from a list of agents with a specified splitting threshold.
	 * 
	 * <p>
	 * This convenience factory method converts a list of agents to an array and creates an AgentSpliterator. It
	 * provides the same splitting behavior as {@link #of(IShape[], int)} but accepts a List as input.
	 * </p>
	 * 
	 * @param agents
	 *            the list of IShape objects (typically agents) to be processed
	 * @param threshold
	 *            the minimum number of agents required for the spliterator to split further
	 * @return a Spliterator that can partition the agents for parallel processing
	 */
	public static Spliterator<IAgent> of(final List<? extends IShape> agents, final int threshold) {
		final int size = agents.size();
		return new AgentSpliterator(agents.toArray(new IAgent[size]), 0, size, threshold);
	}

	/** The begin index for this partition of agents. */
	int begin;
	
	/** The end index (exclusive) for this partition and the splitting threshold. */
	final int end, threshold;
	
	/** The array of agents to be processed. */
	final IShape[] agents;

	/**
	 * Constructs a new AgentSpliterator for a specific range of agents.
	 * 
	 * <p>
	 * This private constructor is used internally to create sub-spliterators when splitting the agent collection. Each
	 * spliterator instance manages a specific range [begin, end) of the agent array.
	 * </p>
	 * 
	 * @param array
	 *            the complete array of agents
	 * @param begin
	 *            the starting index (inclusive) of the range managed by this spliterator
	 * @param end
	 *            the ending index (exclusive) of the range managed by this spliterator
	 * @param threshold
	 *            the minimum size for splitting; ranges smaller than this won't be split further
	 */
	private AgentSpliterator(final IShape[] array, final int begin, final int end, final int threshold) {
		this.begin = begin;
		this.end = end;
		this.threshold = threshold;
		agents = array;
	}

	/**
	 * Iterates through all remaining agents in this spliterator's range and applies the specified action.
	 * 
	 * <p>
	 * This method processes all agents from the current {@code begin} index to the {@code end} index sequentially,
	 * applying the given action to each agent.
	 * </p>
	 * 
	 * @param action
	 *            the action to perform on each agent in the remaining range
	 */
	@Override
	public void forEachRemaining(final Consumer<? super IAgent> action) {
		for (int i = begin; i < end; ++i) {
			action.accept((IAgent) agents[i]);
		}
	}

	/**
	 * Attempts to process the next agent in this spliterator's range.
	 * 
	 * <p>
	 * Note: This implementation always returns true but doesn't actually advance. The primary iteration mechanism is
	 * through {@link #forEachRemaining(Consumer)}.
	 * </p>
	 * 
	 * @param action
	 *            the action to perform on the next agent
	 * @return always returns true
	 */
	@Override
	public boolean tryAdvance(final Consumer<? super IAgent> action) {
		return true;
	}

	/**
	 * Attempts to split this spliterator into two parts for parallel processing.
	 * 
	 * <p>
	 * If the remaining range size exceeds the threshold, this method splits the range in half. The current spliterator
	 * keeps the upper half [mid, end), and returns a new spliterator managing the lower half [begin, mid). If the
	 * range is too small (size ≤ threshold), no split occurs and null is returned.
	 * </p>
	 * 
	 * @return a new AgentSpliterator covering the first half of the remaining range, or null if the range is too small
	 *         to split
	 */
	@Override
	public AgentSpliterator trySplit() {
		final int size = end - begin;
		if (size <= threshold) { return null; }
		final int mid = begin + size / 2;
		final AgentSpliterator split = new AgentSpliterator(agents, begin, mid, threshold);
		begin = mid;
		return split;
	}

	/**
	 * Returns the estimated number of agents remaining to be processed in this spliterator.
	 * 
	 * @return the exact count of agents in the range [begin, end)
	 */
	@Override
	public long estimateSize() {
		return (long) end - begin;
	}

	/**
	 * Returns the characteristics of this spliterator.
	 * 
	 * <p>
	 * This spliterator reports the following characteristics:
	 * </p>
	 * <ul>
	 * <li>CONCURRENT: Can be safely used in concurrent contexts</li>
	 * <li>ORDERED: Maintains the original order of agents</li>
	 * <li>IMMUTABLE: The underlying array cannot be modified during iteration</li>
	 * <li>SIZED: The exact size is known via {@link #estimateSize()}</li>
	 * </ul>
	 * 
	 * @return a bitmask of characteristics
	 */
	@Override
	public int characteristics() {
		return Spliterator.CONCURRENT | Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.SIZED;
	}

}
