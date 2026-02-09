/*******************************************************************************************************
 *
 * DescriptionOptimizationUtils.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import gama.api.gaml.types.IType;

/**
 * Utility class providing optimization helpers for symbol descriptions.
 * Contains caching mechanisms and performance optimizations for commonly used operations.
 * 
 * @author Optimized by GitHub Copilot
 * @since 2026
 */
public final class DescriptionOptimizationUtils {

	/** Cache for commonly computed types to avoid repeated type inference */
	private static final Map<String, IType<?>> typeCache = new ConcurrentHashMap<>();
	
	/** Maximum cache size to prevent memory leaks */
	private static final int MAX_CACHE_SIZE = 1000;
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private DescriptionOptimizationUtils() {}
	
	/**
	 * Gets a cached type or computes and caches it if not present.
	 * Thread-safe implementation with size limit.
	 * 
	 * @param key the cache key
	 * @param typeComputer function to compute the type if not cached
	 * @return the cached or computed type
	 */
	public static IType<?> getCachedType(final String key, final java.util.function.Supplier<IType<?>> typeComputer) {
		IType<?> cachedType = typeCache.get(key);
		if (cachedType != null) {
			return cachedType;
		}
		
		// Compute the type
		IType<?> computedType = typeComputer.get();
		
		// Cache with size limit
		if (typeCache.size() < MAX_CACHE_SIZE) {
			typeCache.put(key, computedType);
		}
		
		return computedType;
	}
	
	/**
	 * Clears the type cache. Useful for memory management in long-running applications.
	 */
	public static void clearTypeCache() {
		typeCache.clear();
	}
	
	/**
	 * Gets the current cache size for monitoring purposes.
	 * 
	 * @return the number of cached types
	 */
	public static int getCacheSize() {
		return typeCache.size();
	}
	
	/**
	 * Fast hash computation for symbol description caching keys.
	 * Combines multiple string values into a single hash key.
	 * 
	 * @param components the string components to hash
	 * @return a hash key suitable for caching
	 */
	public static String computeHashKey(final String... components) {
		if (components.length == 1) {
			return components[0];
		}
		
		StringBuilder sb = new StringBuilder();
		for (String component : components) {
			if (component != null) {
				sb.append(component).append("|");
			}
		}
		return sb.toString();
	}
}