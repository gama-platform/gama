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
 * Written by drogoul Modified on 20 ao�t 2010
 *
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class AgentConstructorsRegistry {

	/** The minimal grid. */
	static Class<? extends IAgent> minimalGrid;

	/** The regular grid. */
	static Class<? extends IAgent> regularGrid;

	/** The minimal normal. */
	static Class<? extends IAgent> minimalNormal;

	/** The regular normal. */
	static Class<? extends IAgent> regularNormal;

	/** The constructors. */
	public static Map<Class<? extends IAgent>, IAgentConstructor> CONSTRUCTORS = new HashMap<>();

	/**
	 * @param class1
	 * @param object
	 */
	public static void register(final Class<? extends IAgent> clazz, final boolean isGrid, final boolean isMinimal) {

		if (isGrid) {
			if (isMinimal) {
				minimalGrid = clazz;
			} else {
				regularGrid = clazz;
			}
		} else if (isMinimal) {
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
	 * Gets the base class.
	 *
	 * @param isGrid
	 *            the is grid
	 * @param isMinimal
	 *            the is minimal
	 * @return the base class
	 */
	public static Class<? extends IAgent> getBaseClass(final boolean isGrid, final boolean isMinimal) {
		if (isGrid) {
			if (isMinimal) return minimalGrid;
			return regularGrid;
		}
		if (isMinimal) return minimalNormal;
		return regularNormal;
	}

}
