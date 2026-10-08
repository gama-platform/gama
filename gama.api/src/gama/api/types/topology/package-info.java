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
 * Provides interfaces and implementations for spatial topologies in GAMA simulations.
 * 
 * <p>
 * This package contains the core abstractions for topologies, which define the spatial structure of simulation
 * environments. Topologies provide methods for spatial operations such as distance calculations, path finding,
 * neighbor queries, and location validation.
 * </p>
 * 
 * <h2>Main Components</h2>
 * <ul>
 * <li>{@link gama.api.types.topology.ITopology} - The core interface representing a spatial topology</li>
 * <li>{@link gama.api.types.topology.ITopologyFactory} - Factory interface for creating topology instances</li>
 * <li>{@link gama.api.types.topology.GamaTopologyFactory} - Static factory providing convenient methods for topology
 * creation</li>
 * <li>{@link gama.api.types.topology.AmorphousTopology} - An expandable, boundless topology implementation</li>
 * </ul>
 * 
 * <h2>Topology Types</h2>
 * <p>
 * GAMA supports several types of topologies:
 * </p>
 * <ul>
 * <li><b>Continuous Topologies:</b> Allow free movement within boundaries defined by a shape</li>
 * <li><b>Grid Topologies:</b> Discrete space divided into cells (typically represented by agents)</li>
 * <li><b>Graph Topologies:</b> Connectivity defined by a spatial graph structure</li>
 * <li><b>Toroidal Topologies:</b> Wrap-around boundaries for continuous movement</li>
 * <li><b>Amorphous Topologies:</b> Expandable, boundless space that grows to contain agents</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>
 * Topologies can be created using the {@link gama.api.types.topology.GamaTopologyFactory}:
 * </p>
 * 
 * <pre>
 * // Create a continuous topology from a shape
 * ITopology topology = GamaTopologyFactory.createContinuous(scope, environmentShape);
 * 
 * // Create a grid topology
 * ITopology gridTopology = GamaTopologyFactory.createGrid(scope, species, host);
 * 
 * // Create a graph topology
 * ITopology graphTopology = GamaTopologyFactory.createGraph(scope, shape, spatialGraph);
 * 
 * // Cast an object to a topology
 * ITopology topology = GamaTopologyFactory.castToTopology(scope, object);
 * </pre>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li>Spatial operations: distance, direction, path finding</li>
 * <li>Agent queries: nearest neighbors, agents within distance, spatial filtering</li>
 * <li>Location management: validation, normalization, random locations</li>
 * <li>Movement calculations: destination computation in 2D and 3D</li>
 * <li>Spatial indexing for efficient queries</li>
 * <li>Support for toroidal (wrap-around) spaces</li>
 * <li>Integration with GAMA's agent and geometry systems</li>
 * </ul>
 * 
 * <h2>Spatial Relations</h2>
 * <p>
 * The package defines several spatial relationships through {@link gama.api.types.topology.ITopology.SpatialRelation}:
 * </p>
 * <ul>
 * <li>OVERLAP - geometries share some but not all area</li>
 * <li>COVER - one geometry completely covers another</li>
 * <li>INSIDE - one geometry is completely inside another</li>
 * <li>TOUCH - geometries touch at boundaries but don't overlap</li>
 * <li>CROSS - geometries cross each other</li>
 * <li>PARTIALLY_OVERLAP - geometries share some area</li>
 * </ul>
 * 
 * @since GAMA 1.0
 * @author drogoul
 */
package gama.api.types.topology;
