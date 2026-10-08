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
 * The UI package provides interfaces and abstractions for user interface components in GAMA.
 * 
 * <p>This package defines the contract between the GAMA platform core and its user interface
 * implementations. It allows the platform to work in both GUI and headless modes through
 * a consistent interface.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <h3>Main UI Interface:</h3>
 * <ul>
 *   <li>{@link gama.api.ui.IGui} - Main interface for GUI operations and interactions</li>
 *   <li>{@link gama.api.ui.NullGuiHandler} - Null object implementation for headless mode</li>
 * </ul>
 * 
 * <h3>Views and Displays:</h3>
 * <ul>
 *   <li>{@link gama.api.ui.IGamaView} - Base interface for GAMA views</li>
 *   <li>{@link gama.api.ui.IExperimentDisplayable} - Interface for experiment-related displays</li>
 * </ul>
 * 
 * <h3>Output Management:</h3>
 * <ul>
 *   <li>{@link gama.api.ui.IOutput} - Interface for simulation outputs</li>
 *   <li>{@link gama.api.ui.IOutputManager} - Manager for output lifecycle</li>
 * </ul>
 * 
 * <h3>User Interaction:</h3>
 * <ul>
 *   <li>{@link gama.api.ui.IDialogFactory} - Factory for creating user dialogs</li>
 *   <li>{@link gama.api.ui.IItemList} - Interface for item selection lists</li>
 * </ul>
 * 
 * <h3>Status and Progress:</h3>
 * <ul>
 *   <li>{@link gama.api.ui.IStatusDisplayer} - Interface for displaying status information</li>
 *   <li>{@link gama.api.ui.IStatusControl} - Control interface for status management</li>
 *   <li>{@link gama.api.ui.IStatusMessage} - Status message representation</li>
 *   <li>{@link gama.api.ui.IProgressIndicator} - Progress indication interface</li>
 * </ul>
 * 
 * <h3>Console:</h3>
 * <ul>
 *   <li>{@link gama.api.ui.IConsoleListener} - Listener for console events</li>
 * </ul>
 * 
 * <h2>Sub-packages</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.ui.displays} - Display types and implementations</li>
 *   <li>{@link gama.api.ui.layers} - Layer management for displays</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Accessing the GUI:</h3>
 * <pre>{@code
 * IGui gui = GAMA.getGui();
 * gui.openSimulationPerspective();
 * }</pre>
 * 
 * <h3>Creating User Dialogs:</h3>
 * <pre>{@code
 * IDialogFactory factory = gui.getDialogFactory();
 * String result = factory.showInputDialog("Enter value:");
 * }</pre>
 * 
 * <h3>Updating Status:</h3>
 * <pre>{@code
 * IStatusDisplayer status = gui.getStatus();
 * status.informStatus("Processing...");
 * }</pre>
 * 
 * <h2>Headless Mode</h2>
 * 
 * <p>When running in headless mode, GAMA uses {@link NullGuiHandler} which provides
 * no-op implementations of all GUI methods, allowing models to run without a graphical
 * interface.</p>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ul>
 *   <li><strong>Separation of Concerns:</strong> Core platform is independent of UI implementation</li>
 *   <li><strong>Null Object Pattern:</strong> Headless mode through NullGuiHandler</li>
 *   <li><strong>Interface-based:</strong> All UI interactions through interfaces</li>
 *   <li><strong>Asynchronous:</strong> UI operations can be non-blocking</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.ui.IGui
 * @see gama.api.ui.displays
 * @see gama.api.ui.layers
 */
package gama.api.ui;
