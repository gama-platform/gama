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
 * The species package provides the core implementation of agent types in GAMA.
 * 
 * <p>
 * This package contains classes and interfaces that define species - the templates for creating agents in GAML
 * models. Species specify the attributes, behaviors, and capabilities of agents, and can be organized hierarchically
 * through inheritance and composition.
 * </p>
 * 
 * <h2>Core Classes</h2>
 * 
 * <ul>
 * <li>{@link gama.api.gaml.species.ISpecies} - Interface defining the species contract</li>
 * <li>{@link gama.api.kernel.species.GamlSpecies} - Standard GAML species implementation</li>
 * <li>{@link gama.api.kernel.species.GamlModelSpecies} - Special species representing the global/world agent</li>
 * </ul>
 * 
 * <h2>Species Hierarchies</h2>
 * 
 * <h3>Inheritance Hierarchy (Parent/Child)</h3>
 * <p>
 * Species can extend other species to inherit their attributes and behaviors:
 * </p>
 * <pre>
 * {@code
 * species animal {
 *     float energy <- 100.0;
 *     reflex consume_energy {
 *         energy <- energy - 1.0;
 *     }
 * }
 * 
 * species predator parent: animal {
 *     // Inherits energy and consume_energy
 *     reflex hunt {
 *         // Additional behavior
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>Composition Hierarchy (Macro/Micro)</h3>
 * <p>
 * Species can contain other species (micro-species) within their agents:
 * </p>
 * <pre>
 * {@code
 * species city {
 *     int population;
 *     
 *     species district {
 *         // Districts live inside city agents
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>Species Types</h2>
 * 
 * <h3>Regular Species</h3>
 * <p>
 * Standard agent populations with custom behaviors and attributes.
 * </p>
 * 
 * <h3>Grid Species</h3>
 * <p>
 * Spatially organized agents in a regular lattice:
 * </p>
 * <pre>
 * {@code
 * grid cell width: 50 height: 50 neighbors: 8 {
 *     rgb color <- #white;
 * }
 * }
 * </pre>
 * 
 * <h3>Graph Species</h3>
 * <p>
 * Species whose agents form nodes in a graph structure with explicit edge connections.
 * </p>
 * 
 * <h3>Mirror Species</h3>
 * <p>
 * Species that automatically track another species' population:
 * </p>
 * <pre>
 * {@code
 * species node_agent mirrors: list(agent) {
 *     // Each instance automatically has a 'target' attribute
 * }
 * }
 * </pre>
 * 
 * <h2>Species Components</h2>
 * 
 * <h3>Variables (Attributes)</h3>
 * <p>
 * Define the state of agents:
 * </p>
 * <pre>
 * {@code
 * species person {
 *     int age <- rnd(80);
 *     string name <- one_of(["Alice", "Bob", "Charlie"]);
 *     point home_location;
 * }
 * }
 * </pre>
 * 
 * <h3>Actions</h3>
 * <p>
 * Reusable procedures that agents can execute:
 * </p>
 * <pre>
 * {@code
 * species person {
 *     action go_home {
 *         do goto target: home_location;
 *     }
 *     
 *     float distance_to(person other) {
 *         return self distance_to other;
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>Behaviors (Reflexes)</h3>
 * <p>
 * Scheduled activities executed automatically:
 * </p>
 * <pre>
 * {@code
 * species person {
 *     reflex move when: flip(0.3) {
 *         do wander;
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>Aspects</h3>
 * <p>
 * Visual representations for displays:
 * </p>
 * <pre>
 * {@code
 * species person {
 *     aspect default {
 *         draw circle(2) color: #blue;
 *     }
 *     
 *     aspect detailed {
 *         draw sphere(2) at: location color: #blue;
 *         draw name size: 3 color: #black;
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>Skills</h3>
 * <p>
 * Reusable capability modules:
 * </p>
 * <pre>
 * {@code
 * species person skills: [moving, communication] {
 *     // Inherits actions like 'wander', 'goto', 'send_message', etc.
 * }
 * }
 * </pre>
 * 
 * <h3>Control Architecture</h3>
 * <p>
 * Defines how behaviors are executed:
 * </p>
 * <pre>
 * {@code
 * species robot control: fsm {
 *     state waiting {
 *         transition to: moving when: has_task;
 *     }
 *     
 *     state moving {
 *         transition to: waiting when: task_completed;
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>Scheduling</h2>
 * 
 * <p>
 * Species can control when and how their agents are scheduled:
 * </p>
 * <pre>
 * {@code
 * // Execute every 2 cycles
 * species slow_agent frequency: 2 { }
 * 
 * // Execute only specific agents
 * species selective schedules: (10 among self) { }
 * 
 * // Enable concurrent execution
 * species fast_agent parallel: true { }
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.species.ISpecies
 * @see gama.api.kernel.agent.IAgent
 * @see gama.api.kernel.agent.IPopulation
 */
package gama.api.kernel.species;
