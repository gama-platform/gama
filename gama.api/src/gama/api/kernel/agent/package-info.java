/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The agent package provides the core agent abstraction and lifecycle management for GAMA simulations.
 * 
 * <p>This package contains interfaces and implementations for agents, which are the fundamental
 * entities in GAMA's agent-based modeling framework. It defines agent behavior, attributes,
 * lifecycle, and interaction protocols.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.kernel.agent.IAgent} - Base interface for all agents</li>
 *   <li>{@link gama.api.kernel.agent.IAgentConstructor} - Interface for constructing agents</li>
 *   <li>{@link gama.api.kernel.agent.IMacroAgent} - Interface for agents containing populations</li>
 * </ul>
 * 
 * <h2>Agent Capabilities</h2>
 * 
 * <p>Agents provide:</p>
 * <ul>
 *   <li><strong>Attributes:</strong> Named values and variables</li>
 *   <li><strong>Actions:</strong> Behaviors that can be executed</li>
 *   <li><strong>Skills:</strong> Reusable capability sets</li>
 *   <li><strong>Lifecycle:</strong> Initialization, stepping, and disposal</li>
 *   <li><strong>Population Management:</strong> Creating and managing sub-agents</li>
 *   <li><strong>Scheduling:</strong> Temporal behavior control</li>
 * </ul>
 * 
 * <h2>Agent Hierarchy</h2>
 * 
 * <p>Agents can be organized hierarchically:</p>
 * <ul>
 *   <li>World/Simulation agent at top level</li>
 *   <li>Macro agents can contain populations of micro agents</li>
 *   <li>Agents inherit from species definitions</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IAgent agent = scope.getAgent();
 * 
 * // Access attributes
 * Object value = agent.getAttribute("my_attribute");
 * agent.setAttribute("my_attribute", newValue);
 * 
 * // Execute actions
 * Object result = agent.executeAction(scope, "my_action", args);
 * 
 * // Access species
 * ISpecies species = agent.getSpecies();
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.kernel.agent.IAgent
 * @see gama.api.kernel.species
 */
package gama.api.kernel.agent;
