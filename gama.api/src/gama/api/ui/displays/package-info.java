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
 * The displays package provides display type definitions and management for GAMA visualizations.
 * 
 * <p>This package contains interfaces and classes for defining and managing different types of
 * displays in GAMA, including Java2D displays, OpenGL displays, and custom display implementations.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.ui.displays.IDisplaySurface} - Interface for display surfaces</li>
 *   <li>{@link gama.api.ui.displays.IDisplayCreator} - Factory for creating displays</li>
 *   <li>{@link gama.api.ui.displays.DisplayDescription} - Display type metadata and configuration</li>
 * </ul>
 * 
 * <h2>Display Types</h2>
 * 
 * <p>GAMA supports various display types:</p>
 * <ul>
 *   <li><strong>Java2D:</strong> 2D rendering using Java graphics</li>
 *   <li><strong>OpenGL:</strong> 3D rendering using OpenGL</li>
 *   <li><strong>Web:</strong> Web-based visualizations</li>
 *   <li><strong>Custom:</strong> Plugin-provided display implementations</li>
 * </ul>
 * 
 * <h2>Display Features</h2>
 * 
 * <p>Displays provide:</p>
 * <ul>
 *   <li>Rendering of agents and environments</li>
 *   <li>Layer management</li>
 *   <li>Zooming and panning</li>
 *   <li>Screenshot and video capture</li>
 *   <li>Overlay management</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.ui.displays.IDisplaySurface
 * @see gama.api.ui.layers
 */
package gama.api.ui.displays;
