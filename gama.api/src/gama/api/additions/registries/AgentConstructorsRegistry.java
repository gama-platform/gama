/*******************************************************************************************************
 *
 * AgentConstructorsRegistry.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.registries;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IAgentConstructor;

/**
 * Registry for agent constructor implementations in GAMA.
 *
 * <p>
 * This registry maintains the mapping between agent types and their constructor implementations. GAMA uses different
 * agent implementations depending on whether the agent is a grid cell or a normal agent, and whether it uses a minimal
 * or regular implementation (with or without scheduling capabilities).
 * </p>
 *
 * <h2>Agent Implementation Types</h2>
 * <p>
 * GAMA provides four base agent implementation classes:
 * </p>
 * <ul>
 * <li><b>Minimal Normal</b> - Basic agent without scheduling</li>
 * <li><b>Regular Normal</b> - Full-featured agent with scheduling</li>
 * <li><b>Minimal Grid</b> - Grid cell without scheduling</li>
 * <li><b>Regular Grid</b> - Grid cell with scheduling</li>
 * </ul>
 *
 * <p>
 * The appropriate base class is selected at runtime based on the species definition and whether the agent belongs to a
 * grid.
 * </p>
 *
 * <h2>Constructor Registration</h2>
 * <p>
 * Agent constructors are registered during platform initialization by the core plugin. Each constructor is wrapped in
 * an {@link IAgentConstructor} functional interface for efficient instantiation.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // Get the appropriate base class for a grid species with scheduling
 * Class<? extends IAgent> gridClass = AgentConstructorsRegistry.getBaseClass(true, false);
 *
 * // Get the constructor for instantiation
 * IAgentConstructor constructor = AgentConstructorsRegistry.CONSTRUCTORS.get(gridClass);
 * IAgent newAgent = constructor.createAgent(population, index);
 * }</pre>
 *
 * @author drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class AgentConstructorsRegistry {

	/** The minimal normal agent implementation class. */
	static Class<? extends IAgent> minimalNormal;

	/** The regular (full-featured) normal agent implementation class. */
	static Class<? extends IAgent> regularNormal;

	/** Map of agent classes to their constructor functions. */
	public static Map<Class<? extends IAgent>, IAgentConstructor> CONSTRUCTORS = new HashMap<>();

	/**
	 * Registers an agent implementation class and its constructor.
	 *
	 * <p>
	 * This method is called during platform initialization to register the base agent implementations. It stores the
	 * class reference for later retrieval and wraps the class constructor in an {@link IAgentConstructor} for efficient
	 * instantiation.
	 * </p>
	 *
	 * <p>
	 * The method assumes the agent class has exactly one constructor that takes a population manager and an index as
	 * parameters.
	 * </p>
	 *
	 * @param clazz
	 *            the agent implementation class to register
	 * @param isGrid
	 *            true if this is a grid cell implementation, false for normal agents
	 * @param isMinimal
	 *            true if this is a minimal (no scheduling) implementation, false for regular
	 */
	public static void register(final Class<? extends IAgent> clazz, final boolean isMinimal) {

		if (isMinimal) {
			minimalNormal = clazz;
		} else {
			regularNormal = clazz;
		}
		Constructor<? extends IAgent> constructor = (Constructor<? extends IAgent>) clazz.getDeclaredConstructors()[0];
		CONSTRUCTORS.put(clazz, (manager, index) -> {
			try {
				return constructor.newInstance(manager, index);
			} catch (Exception e) {
				return null;
			}
		});

	}

	/**
	 * Returns the appropriate base agent class for the specified characteristics.
	 *
	 * <p>
	 * This method selects one of the four registered base agent implementation classes based on whether the agent is a
	 * grid cell and whether it requires a minimal or regular (scheduled) implementation.
	 * </p>
	 *
	 * <p>
	 * The returned class can be used to:
	 * </p>
	 * <ul>
	 * <li>Retrieve the appropriate constructor from the {@link #CONSTRUCTORS} map</li>
	 * <li>Determine superclass relationships during species compilation</li>
	 * <li>Check agent implementation capabilities</li>
	 * </ul>
	 *
	 * @param isGrid
	 *            true to get a grid cell implementation, false for a normal agent
	 * @param isMinimal
	 *            true to get a minimal (no scheduling) implementation, false for regular
	 * @return the agent base class matching the specified characteristics
	 */
	public static Class<? extends IAgent> getBaseClass(final boolean isMinimal) {
		return isMinimal ? minimalNormal : regularNormal;
	}

}
