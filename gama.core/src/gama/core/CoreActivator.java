/*******************************************************************************************************
 *
 * CoreActivator.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core;

import org.osgi.framework.BundleContext;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.additions.registries.AgentConstructorsRegistry;
import gama.api.data.factories.GamaEnvelopeFactory;
import gama.api.data.factories.GamaFontFactory;
import gama.api.data.factories.GamaGraphFactory;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.factories.GamaMessageFactory;
import gama.api.data.factories.GamaPairFactory;
import gama.api.data.factories.GamaPathFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.factories.GamaTopologyFactory;
import gama.core.agent.GamlAgent;
import gama.core.agent.MinimalAgent;
import gama.core.geometry.InternalGamaEnvelopeFactory;
import gama.core.geometry.InternalGamaShapeFactory;
import gama.core.topology.InternalTopologyFactory;
import gama.core.topology.grid.GamlGridAgent;
import gama.core.topology.grid.MinimalGridAgent;
import gama.core.util.InternalGamaFontFactory;
import gama.core.util.InternalGamaPairFactory;
import gama.core.util.graph.InternalGamaGraphFactory;
import gama.core.util.json.Json;
import gama.core.util.list.InternalGamaListFactory;
import gama.core.util.map.InternalGamaMapFactory;
import gama.core.util.matrix.InternalGamaMatrixFactory;
import gama.core.util.messaging.GamaMessage;
import gama.core.util.path.InternalGamaPathFactory;
import gama.dependencies.GamaBundleActivator;
import gama.dev.DEBUG;
import gama.gaml.operators.Dates;

/**
 * The CoreActivator is the main entry point for initializing the GAMA core platform bundle.
 *
 * <p>
 * This OSGi bundle activator is responsible for bootstrapping the core GAMA platform services and factories when the
 * gama.core bundle starts. It serves as the foundation initialization layer that sets up all the essential data type
 * factories, JSON services, and triggers the loading of platform contributions.
 * </p>
 *
 * <p>
 * The activator performs the following critical initialization tasks:
 * </p>
 * <ul>
 * <li><strong>Factory Registration:</strong> Registers concrete implementations for all GAMA data type factories
 * (lists, maps, matrices, geometries, etc.)</li>
 * <li><strong>JSON Service Setup:</strong> Configures the JSON encoder/decoder service for the platform</li>
 * <li><strong>Bundle Contribution Loading:</strong> Triggers the discovery and loading of extensions and contributions
 * from all GAMA bundles</li>
 * <li><strong>Platform Bootstrapping:</strong> Ensures the core platform is ready for experiment execution</li>
 * </ul>
 *
 * <p>
 * This class follows the OSGi Bundle Activator pattern and is automatically invoked by the OSGi framework when the
 * gama.core bundle transitions to the ACTIVE state. The initialization order is critical as many other GAMA components
 * depend on the factories and services configured here.
 * </p>
 *
 * <p>
 * <strong>Initialization Order:</strong>
 * </p>
 * <ol>
 * <li>Factory initialization (all data type factories)</li>
 * <li>JSON encoder setup</li>
 * <li>Bundle contribution discovery and loading</li>
 * </ol>
 *
 * @author Alexis Drogoul
 * @author The GAMA Team
 * @since GAMA 1.0
 * @version 2025-03
 */
public class CoreActivator extends GamaBundleActivator {

	static {
		DEBUG.OFF();
	}

	/**
	 * Starts the GAMA core bundle and performs essential platform initialization.
	 *
	 * <p>
	 * This method is automatically called by the OSGi framework when the gama.core bundle is started. It performs the
	 * critical initialization sequence required for the GAMA platform to function properly.
	 * </p>
	 *
	 * <p>
	 * The initialization sequence is carefully ordered:
	 * </p>
	 * <ol>
	 * <li><strong>Factory Initialization:</strong> Sets up all concrete factory implementations for GAMA data types
	 * (collections, geometries, colors, etc.)</li>
	 * <li><strong>JSON Service Setup:</strong> Registers the JSON encoder/decoder service with the GAMA platform</li>
	 * <li><strong>Contribution Loading:</strong> Discovers and loads all extensions and contributions from GAMA
	 * bundles</li>
	 * </ol>
	 *
	 * @param context
	 *            the OSGi bundle context for this bundle
	 * @throws Exception
	 *             if any initialization step fails
	 *
	 * @see #initializeFactories()
	 * @see GamaBundleLoader#buildContributions()
	 */
	@Override
	public void initialize(final BundleContext context) {
		DEBUG.OUT("Starting GAMA Core Bundle");
		initializeFactories();
		initializeAgentClasses();
		GAMA.setJsonEncoder(Json.getNew());
		GamaBundleLoader.buildContributions();
	}

	/**
	 *
	 */
	void initializeAgentClasses() {
		AgentConstructorsRegistry.register(GamlAgent.class, false, false);
		AgentConstructorsRegistry.register(MinimalAgent.class, false, true);
		AgentConstructorsRegistry.register(GamlGridAgent.class, true, false);
		AgentConstructorsRegistry.register(MinimalGridAgent.class, true, true);
	}

	/**
	 * Initializes all GAMA data type factories with their concrete implementations.
	 *
	 * <p>
	 * This method is responsible for registering concrete factory implementations for all GAMA data types using the
	 * Abstract Factory pattern. Each factory is responsible for creating instances of specific GAMA data types
	 * throughout the platform.
	 * </p>
	 *
	 * <p>
	 * The factories are organized by data type category:
	 * </p>
	 * <ul>
	 * <li><strong>Collections:</strong> Lists, Maps, Matrices for data storage</li>
	 * <li><strong>Geometry:</strong> Points, Shapes, Envelopes, Coordinate sequences, Topologies</li>
	 * <li><strong>Utilities:</strong> Colors, Fonts, Dates, Pairs for common data types</li>
	 * <li><strong>Graph Structures:</strong> Graphs and Paths for network operations</li>
	 * <li><strong>Communication:</strong> Messages for agent communication</li>
	 * </ul>
	 *
	 * <p>
	 * Each factory registration follows the pattern: {@code AbstractFactory.setBuilder(new ConcreteFactory())}
	 * </p>
	 *
	 * <p>
	 * <strong>Critical Note:</strong> This method must be called before any GAMA data types are instantiated, as the
	 * platform relies on these factories for object creation. The initialization order within this method is not
	 * critical as factories are independent.
	 * </p>
	 *
	 * @see gama.api.data.factories for the abstract factory interfaces
	 */
	void initializeFactories() {
		// Collection factories for data structures
		GamaListFactory.setBuilder(new InternalGamaListFactory());
		GamaMapFactory.setBuilder(new InternalGamaMapFactory());
		GamaMatrixFactory.setBuilder(new InternalGamaMatrixFactory());
		GamaPairFactory.setBuilder(new InternalGamaPairFactory());

		// Geometry and spatial factories
		// GamaPointFactory.setBuilder(new InternalGamaPointFactory());
		// GamaCoordinateSequenceFactory.setBuilder(new InternalGamaCoordinateSequenceFactory());
		GamaEnvelopeFactory.setBuilder(new InternalGamaEnvelopeFactory());
		GamaShapeFactory.setBuilder(new InternalGamaShapeFactory());
		GamaTopologyFactory.setBuilder(new InternalTopologyFactory());
		GamaPathFactory.setBuilder(new InternalGamaPathFactory());

		// Utility type factories
		// GamaColorFactory.setBuilder(new InternalGamaColorFactory());
		// Only here to load the class and its preferences
		Dates.initialize();
		// GamaDateFactory.setBuilder(new InternalGamaDateFactory());
		GamaFontFactory.setBuilder(new InternalGamaFontFactory());

		// Graph and network factories
		GamaGraphFactory.setBuilder(new InternalGamaGraphFactory());

		// Communication factories
		GamaMessageFactory.setBuilder(new GamaMessage.Factory());
	}

	/**
	 * Static method to trigger core bundle loading during platform initialization.
	 *
	 * <p>
	 * This method provides a static entry point for triggering the core bundle loading process. It can be called from
	 * other initialization routines that need to ensure the GAMA core bundle is properly loaded and initialized.
	 * </p>
	 *
	 * <p>
	 * <strong>Note:</strong> This method currently only logs the loading process. The actual initialization is handled
	 * by the {@link #start(BundleContext)} method when the OSGi framework starts the bundle.
	 * </p>
	 */
	public static void load() {
		DEBUG.OUT("Loading GAMA Core Bundle");
	}

}
