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
 * The kernel package contains the core platform kernel including agents, simulations, species, and skills.
 * 
 * <p>This package defines the fundamental abstractions and implementations of GAMA's agent-based
 * modeling framework. It includes the metamodel, agent lifecycle management, species definitions,
 * simulation control, and the skill system.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Platform Kernel:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.GamaMetaModel} - GAMA metamodel definitions</li>
 *   <li>{@link gama.api.kernel.PlatformAgent} - The platform-level agent</li>
 * </ul>
 * 
 * <h2>Sub-packages</h2>
 * 
 * <h3>Agent Management:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.agent} - Agent interfaces, implementations, and lifecycle</li>
 * </ul>
 * 
 * <h3>Species System:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.species} - Species definitions and management</li>
 * </ul>
 * 
 * <h3>Simulation Control:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.simulation} - Simulation and experiment management</li>
 * </ul>
 * 
 * <h3>Skills:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.skill} - Skill system for reusable agent behaviors</li>
 * </ul>
 * 
 * <h3>Topology:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.topology} - Spatial topology management</li>
 * </ul>
 * 
 * <h3>Serialization:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.serialization} - Agent and simulation state serialization</li>
 * </ul>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>Agents:</h3>
 * <p>Agents are the fundamental entities in GAMA simulations. They have:</p>
 * <ul>
 *   <li>Attributes (variables)</li>
 *   <li>Behaviors (actions)</li>
 *   <li>Skills (reusable capabilities)</li>
 *   <li>Lifecycle (initialization, step, disposal)</li>
 * </ul>
 * 
 * <h3>Species:</h3>
 * <p>Species define agent types with shared characteristics:</p>
 * <ul>
 *   <li>Attribute definitions</li>
 *   <li>Action definitions</li>
 *   <li>Inheritance hierarchy</li>
 *   <li>Skill associations</li>
 * </ul>
 * 
 * <h3>Simulations:</h3>
 * <p>Simulations are the execution environments for models:</p>
 * <ul>
 *   <li>Population management</li>
 *   <li>Scheduler control</li>
 *   <li>Global variables</li>
 *   <li>Output management</li>
 * </ul>
 * 
 * <h3>Skills:</h3>
 * <p>Skills provide reusable behaviors that can be added to species:</p>
 * <ul>
 *   <li>Movement capabilities</li>
 *   <li>Communication protocols</li>
 *   <li>Perception abilities</li>
 *   <li>Custom behaviors</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Accessing Agent Information:</h3>
 * <pre>{@code
 * IAgent agent = scope.getAgent();
 * ISpecies species = agent.getSpecies();
 * Object value = agent.getAttribute("my_attribute");
 * }</pre>
 * 
 * <h3>Creating Agents:</h3>
 * <pre>{@code
 * IPopulation<IAgent> population = simulation.getPopulationFor(speciesName);
 * List<IAgent> newAgents = population.createAgents(scope, 10);
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.kernel.agent
 * @see gama.api.kernel.species
 * @see gama.api.kernel.simulation
 */
package gama.api.kernel;
