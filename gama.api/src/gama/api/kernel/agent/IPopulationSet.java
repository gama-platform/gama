/*******************************************************************************************************
 *
 * IPopulationSet.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import java.util.Collection;

import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IContainer;
import gama.api.utils.interfaces.IAgentFilter;
import one.util.streamex.StreamEx;

/**
 * The Interface IPopulationSet.
 * 
 * <p>
 * A common interface for collections of agents in GAMA, implemented by ISpecies, IPopulation, and MetaPopulation. This
 * interface provides a unified way to work with different kinds of agent collections, whether they represent a single
 * population, a species (which may have multiple populations), or a meta-population (union of multiple populations).
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * IPopulationSet abstracts the concept of "a set of agents" allowing code to work uniformly with:
 * </p>
 * <ul>
 * <li><b>IPopulation:</b> A single concrete collection of agents of one species</li>
 * <li><b>ISpecies:</b> All agents belonging to a species (across all populations)</li>
 * <li><b>MetaPopulation:</b> A union of multiple populations or species</li>
 * </ul>
 * 
 * <h3>Key Capabilities</h3>
 * <ul>
 * <li><b>Container Interface:</b> Provides collection-like operations (iteration, counting, etc.)</li>
 * <li><b>Agent Filtering:</b> Implements IAgentFilter for filtering operations</li>
 * <li><b>Population Access:</b> Access to underlying population(s)</li>
 * <li><b>Streaming:</b> Efficient stream-based processing of agents</li>
 * </ul>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <h4>1. Working with Species (IPopulationSet)</h4>
 * 
 * <pre>
 * <code>
 * species person {
 *     int age;
 * }
 * 
 * global {
 *     reflex analyze {
 *         // 'person' is an IPopulationSet (specifically an ISpecies)
 *         int count <- length(person);
 *         
 *         // Filter agents
 *         list&lt;person&gt; adults <- person where (each.age >= 18);
 *         
 *         // Aggregate operations
 *         float avg_age <- mean(person collect each.age);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>2. Working with Multiple Populations</h4>
 * 
 * <pre>
 * <code>
 * species parent {
 *     species child {
 *         int value;
 *     }
 * }
 * 
 * global {
 *     init {
 *         create parent number: 5 {
 *             create child number: 10;
 *         }
 *     }
 *     
 *     reflex analyze {
 *         // 'child' as a species represents all child populations
 *         int total_children <- length(child);
 *         // Access all children across all parent hosts
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h4>3. Filtering and Querying</h4>
 * 
 * <pre>
 * <code>
 * species animal {
 *     string type;
 *     float energy;
 * }
 * 
 * global {
 *     reflex monitor {
 *         // IPopulationSet enables filtering
 *         list&lt;animal&gt; predators <- animal where (each.type = "predator");
 *         list&lt;animal&gt; low_energy <- animal where (each.energy < 20);
 *         
 *         // Count matching agents
 *         int hungry_count <- animal count (each.energy < 50);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * // IPopulationSet can be ISpecies, IPopulation, or MetaPopulation
 * IPopulationSet&lt;IAgent&gt; agentSet = ...;
 * 
 * // Get all populations in this set
 * Collection&lt;? extends IPopulation&lt;? extends IAgent&gt;&gt; populations = 
 *     agentSet.getPopulations(scope);
 * 
 * // Stream processing
 * StreamEx&lt;IAgent&gt; agentStream = agentSet.stream(scope);
 * long count = agentStream.filter(a -> a.getAttribute("age") > 18).count();
 * 
 * // Container operations (inherited from IContainer)
 * int size = agentSet.length(scope);
 * IAgent firstAgent = agentSet.firstValue(scope);
 * boolean contains = agentSet.contains(scope, someAgent);
 * </code>
 * </pre>
 * 
 * <h3>Design Pattern</h3>
 * <p>
 * This interface implements the Composite pattern, allowing clients to treat individual populations and compositions
 * of populations uniformly. This is particularly useful in multi-level models where a species may have multiple
 * populations hosted by different macro-agents.
 * </p>
 * 
 * @param <T>
 *            the type of agents in this population set
 * @see IPopulation
 * @see ISpecies
 * @see IContainer
 * @see IAgentFilter
 * @author drogoul
 * @since GAMA 1.6
 */
public interface IPopulationSet<T extends IAgent> extends IContainer<Integer, T>, IAgentFilter {

	/**
	 * Gets the populations.
	 *
	 * @param scope
	 *            the scope
	 * @return the populations
	 */
	Collection<? extends IPopulation<? extends IAgent>> getPopulations(IScope scope);

	/**
	 * Stream.
	 *
	 * @param scope
	 *            the scope
	 * @return the stream ex
	 */
	@Override
	StreamEx<T> stream(final IScope scope);

}
