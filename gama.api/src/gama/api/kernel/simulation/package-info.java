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
 * The simulation package provides simulation and experiment management for GAMA.
 * 
 * <p>This package contains interfaces and classes for managing simulations, experiments,
 * their lifecycle, and execution control. It implements the core simulation loop and
 * experiment orchestration.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <h3>Simulation:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.simulation.ISimulationAgent} - Interface for simulation agents</li>
 *   <li>{@link gama.api.kernel.simulation.ITopLevelAgent} - Interface for top-level agents</li>
 * </ul>
 * 
 * <h3>Experiment:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.simulation.IExperimentAgent} - Interface for experiment agents</li>
 *   <li>{@link gama.api.kernel.simulation.IExperimentController} - Controller for experiment execution</li>
 *   <li>{@link gama.api.kernel.simulation.IExperimentStateListener} - Listener for experiment state changes</li>
 * </ul>
 * 
 * <h3>Recording:</h3>
 * <ul>
 *   <li>{@link gama.api.kernel.simulation.IExperimentRecorder} - Interface for recording experiment execution</li>
 * </ul>
 * 
 * <h2>Simulation Lifecycle</h2>
 * 
 * <p>Simulations go through these phases:</p>
 * <ol>
 *   <li><strong>Initialization:</strong> Setup of initial state</li>
 *   <li><strong>Running:</strong> Executing simulation steps</li>
 *   <li><strong>Paused:</strong> Temporarily stopped</li>
 *   <li><strong>Stopped:</strong> Ended permanently</li>
 *   <li><strong>Disposed:</strong> Resources released</li>
 * </ol>
 * 
 * <h2>Experiment Control</h2>
 * 
 * <p>Experiments provide:</p>
 * <ul>
 *   <li>Parameter exploration</li>
 *   <li>Batch execution</li>
 *   <li>GUI or headless operation</li>
 *   <li>Output management</li>
 *   <li>Simulation scheduling</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IExperimentAgent experiment = ...;
 * IExperimentController controller = experiment.getController();
 * 
 * // Start simulation
 * controller.schedule();
 * 
 * // Step through
 * controller.stepBack();
 * controller.step();
 * 
 * // Pause and resume
 * controller.pause();
 * controller.start();
 * 
 * // Stop
 * controller.close();
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.kernel.simulation.ISimulationAgent
 * @see gama.api.kernel.simulation.IExperimentController
 */
package gama.api.kernel.simulation;
