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
 * The species package provides species (agent type) definitions and management.
 * 
 * <p>This package contains interfaces and classes for defining and managing species,
 * which are the type definitions for agents in GAMA. Species define the structure,
 * behavior, and characteristics shared by groups of agents.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.kernel.species.ISpecies} - Base interface for species</li>
 *   <li>{@link gama.api.kernel.species.IModelSpecies} - Interface for the model/world species</li>
 *   <li>{@link gama.api.kernel.species.IExperimentSpecies} - Interface for experiment species</li>
 * </ul>
 * 
 * <h2>Species Characteristics</h2>
 * 
 * <p>Species define:</p>
 * <ul>
 *   <li><strong>Attributes:</strong> Variables that agents of this species possess</li>
 *   <li><strong>Actions:</strong> Behaviors that agents can perform</li>
 *   <li><strong>Skills:</strong> Capabilities inherited from skill definitions</li>
 *   <li><strong>Inheritance:</strong> Parent species for behavior reuse</li>
 *   <li><strong>Population:</strong> Factory for creating agent instances</li>
 * </ul>
 * 
 * <h2>Species Hierarchy</h2>
 * 
 * <p>Species can be organized in an inheritance hierarchy:</p>
 * <ul>
 *   <li>All species ultimately inherit from built-in agent species</li>
 *   <li>Child species inherit attributes and actions from parents</li>
 *   <li>Species can be abstract or concrete</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * ISpecies species = agent.getSpecies();
 * 
 * // Access definition
 * String name = species.getName();
 * Collection<String> varNames = species.getVarNames();
 * Collection<String> actionNames = species.getActionNames();
 * 
 * // Create agents
 * IPopulation<IAgent> pop = species.getPopulation(simulation);
 * List<IAgent> newAgents = pop.createAgents(scope, 10);
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.kernel.species.ISpecies
 * @see gama.api.kernel.agent.IAgent
 */
package gama.api.kernel.species;
