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
 * The topology package provides spatial topology management for GAMA simulations.
 * 
 * <p>This package contains interfaces and implementations for managing spatial relationships,
 * neighbor finding, distance calculations, and spatial movement in simulations.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.kernel.topology.ITopology} - Base interface for spatial topologies</li>
 * </ul>
 * 
 * <h2>Topology Types</h2>
 * 
 * <p>GAMA supports various topology types:</p>
 * <ul>
 *   <li><strong>Continuous:</strong> Standard Euclidean space</li>
 *   <li><strong>Grid:</strong> Discrete grid-based topology</li>
 *   <li><strong>Graph:</strong> Network/graph-based topology</li>
 *   <li><strong>Torus:</strong> Wrapped continuous space</li>
 * </ul>
 * 
 * <h2>Topology Operations</h2>
 * 
 * <p>Topologies provide:</p>
 * <ul>
 *   <li>Distance calculations</li>
 *   <li>Neighbor finding</li>
 *   <li>Path computation</li>
 *   <li>Point normalization</li>
 *   <li>Spatial queries</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.kernel.topology.ITopology
 */
package gama.api.kernel.topology;
