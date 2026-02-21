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
 * The GAMA API package provides the core public API for the GAMA modeling and simulation platform.
 * 
 * <p>This package defines the fundamental interfaces, classes, and services that form the foundation
 * of the GAMA platform. It serves as the primary entry point for:</p>
 * 
 * <ul>
 *   <li><strong>Platform Services:</strong> Core platform initialization and lifecycle management</li>
 *   <li><strong>Experiment Management:</strong> Creation, execution, and control of simulation experiments</li>
 *   <li><strong>Model Compilation:</strong> Parsing, validation, and compilation of GAML models</li>
 *   <li><strong>Runtime Environment:</strong> Execution context, scopes, and agent management</li>
 *   <li><strong>Type System:</strong> GAML's rich type system including spatial, temporal, and collection types</li>
 *   <li><strong>User Interface:</strong> GUI abstractions and display management</li>
 *   <li><strong>Utilities:</strong> Common utilities for files, geometry, collections, and more</li>
 * </ul>
 * 
 * <h2>Package Organization</h2>
 * 
 * <p>The API is organized into the following main sub-packages:</p>
 * 
 * <h3>Core Packages:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation} - Model compilation, AST, descriptions, and validation</li>
 *   <li>{@link gama.api.runtime} - Runtime execution context, scopes, and synchronization</li>
 *   <li>{@link gama.api.kernel} - Core platform kernel, agents, simulations, and skills</li>
 *   <li>{@link gama.api.gaml} - GAML language constructs: expressions, statements, types, variables</li>
 * </ul>
 * 
 * <h3>Support Packages:</h3>
 * <ul>
 *   <li>{@link gama.api.types} - Type system: colors, dates, files, geometry, graphs, collections</li>
 *   <li>{@link gama.api.ui} - User interface abstractions: displays, views, outputs, dialogs</li>
 *   <li>{@link gama.api.utils} - Utilities: collections, files, geometry, benchmarking, preferences</li>
 *   <li>{@link gama.api.additions} - Platform extensions and GAML additions framework</li>
 *   <li>{@link gama.api.constants} - Platform constants: keywords, file extensions, generators</li>
 *   <li>{@link gama.api.exceptions} - Exception hierarchy for runtime and compilation errors</li>
 *   <li>{@link gama.api.annotations} - Annotations for model dependencies and serialization</li>
 * </ul>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.GAMA} - Main facade and entry point for the GAMA platform</li>
 *   <li>{@link gama.api.APIActivator} - OSGi bundle activator for the API bundle</li>
 *   <li>{@link gama.api.gaml.GAML} - Facade for GAML language operations</li>
 * </ul>
 * 
 * <h2>Architecture Principles</h2>
 * 
 * <p>The GAMA API follows several key architectural principles:</p>
 * 
 * <ul>
 *   <li><strong>Separation of Concerns:</strong> Clear separation between compilation, runtime, and UI concerns</li>
 *   <li><strong>Facade Pattern:</strong> Main classes (GAMA, GAML) provide simplified access to complex subsystems</li>
 *   <li><strong>Service Registry:</strong> Centralized registration and lookup of platform services</li>
 *   <li><strong>OSGi Modularity:</strong> Designed for OSGi bundle-based deployment</li>
 *   <li><strong>Interface-based Design:</strong> Extensive use of interfaces for flexibility and testability</li>
 *   <li><strong>Thread Safety:</strong> Critical services designed for concurrent access</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Starting the Platform:</h3>
 * <pre>{@code
 * // Platform is automatically initialized via OSGi
 * // Access platform services through the GAMA class
 * GAMA.getGui().openSimulationPerspective();
 * }</pre>
 * 
 * <h3>Creating and Running an Experiment:</h3>
 * <pre>{@code
 * // Load a model
 * IModel model = GAMA.getModelManager().compile(modelFile);
 * 
 * // Create an experiment
 * IExperimentPlan experiment = model.getExperiment("my_experiment");
 * IExperimentController controller = experiment.getController();
 * 
 * // Run the experiment
 * controller.schedule();
 * }</pre>
 * 
 * <h3>Accessing Runtime Information:</h3>
 * <pre>{@code
 * // Get current scope
 * IScope scope = GAMA.getRuntimeScope();
 * 
 * // Access current simulation
 * ISimulationAgent simulation = scope.getSimulation();
 * 
 * // Access platform agent
 * PlatformAgent platform = GAMA.getPlatformAgent();
 * }</pre>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Many classes in this API are designed to be thread-safe, particularly those dealing with:</p>
 * <ul>
 *   <li>Platform services and registries</li>
 *   <li>Preference stores</li>
 *   <li>Concurrent execution services</li>
 *   <li>Error reporting mechanisms</li>
 * </ul>
 * 
 * <p>However, simulation execution and agent manipulation should generally occur within
 * the appropriate runtime scope to ensure proper synchronization.</p>
 * 
 * <h2>Extension Points</h2>
 * 
 * <p>The API provides several extension mechanisms:</p>
 * <ul>
 *   <li><strong>GAML Additions:</strong> Extend GAML with new operators, types, skills, and statements</li>
 *   <li><strong>File Type Providers:</strong> Register handlers for custom file types</li>
 *   <li><strong>Display Types:</strong> Create custom display implementations</li>
 *   <li><strong>Skills:</strong> Define reusable agent behaviors</li>
 *   <li><strong>Random Generators:</strong> Plug in custom random number generators</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.GAMA
 * @see gama.api.gaml.GAML
 * @see gama.api.compilation
 * @see gama.api.runtime
 * @see gama.api.kernel
 */
package gama.api;
