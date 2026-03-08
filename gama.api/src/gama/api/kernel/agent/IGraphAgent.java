/*******************************************************************************************************
 *
 * IGraphAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

/**
 * The Interface IGraphAgent.
 * 
 * <p>
 * Represents an agent that can be part of a graph structure within GAMA simulations. This interface is a marker
 * interface for agents that participate in graph-based topologies or networks.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * This interface identifies agents that can be nodes or vertices in graph structures, enabling them to participate in
 * graph-based algorithms, network analysis, and topology-based interactions.
 * </p>
 * 
 * <h3>Usage in GAML</h3>
 * 
 * <pre>
 * <code>
 * species node skills: [graph_agent] {
 *     // Agent can now be used in graph structures
 * }
 * 
 * global {
 *     graph road_network;
 *     
 *     init {
 *         create node number: 10;
 *         road_network <- as_edge_graph(node);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
public interface IGraphAgent {

}
