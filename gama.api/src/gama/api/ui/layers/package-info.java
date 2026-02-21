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
 * The layers package provides layer management for GAMA displays.
 * 
 * <p>This package contains interfaces and classes for managing display layers, which control
 * what and how elements are rendered in a display. Layers can display agents, grids, images,
 * charts, and other visual elements.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.ui.layers.IDisplayLayer} - Base interface for display layers</li>
 *   <li>{@link gama.api.ui.layers.ILayerStatement} - Statement definition for layers</li>
 *   <li>{@link gama.api.ui.layers.ISnapshotMaker} - Interface for creating layer snapshots</li>
 * </ul>
 * 
 * <h2>Layer Types</h2>
 * 
 * <p>Common layer types include:</p>
 * <ul>
 *   <li><strong>Species Layer:</strong> Renders agents of specific species</li>
 *   <li><strong>Grid Layer:</strong> Renders grid environments</li>
 *   <li><strong>Image Layer:</strong> Displays background images</li>
 *   <li><strong>Chart Layer:</strong> Displays charts and graphs</li>
 *   <li><strong>Event Layer:</strong> Handles user interaction events</li>
 *   <li><strong>Overlay Layer:</strong> Text and UI overlays</li>
 * </ul>
 * 
 * <h2>Layer Management</h2>
 * 
 * <p>Layers are managed hierarchically within displays:</p>
 * <ul>
 *   <li>Z-order controls rendering order</li>
 *   <li>Visibility can be toggled</li>
 *   <li>Refresh rates can be customized</li>
 *   <li>Transparency and blending supported</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.ui.layers.IDisplayLayer
 * @see gama.api.ui.displays
 */
package gama.api.ui.layers;
