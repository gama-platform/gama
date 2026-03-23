/*******************************************************************************************************
 *
 * TypesManager.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.constants.IGamlIssue;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.object.IObject;
import gama.api.types.object.GamaObjectType;
import gama.dev.DEBUG;

/**
 * Implementation of {@link ITypesManager} that provides model-scoped type registry with hierarchical delegation and
 * performance-optimized caching of type relationships.
 *
 * <p>
 * TypesManager maintains a local registry of types and can delegate lookups to a parent manager, creating a hierarchy:
 * </p>
 *
 * <pre>
 * Built-in TypesManager (Types.BUILT_IN_TYPES)
 *   ↑
 * Model TypesManager
 *   ↑
 * Experiment TypesManager (if needed)
 * </pre>
 *
 * <p>
 * This implementation includes sophisticated caching mechanisms for expensive type relationship operations:
 * </p>
 * <ul>
 * <li><b>Assignability cache:</b> Stores results of {@code isAssignableFrom} checks</li>
 * <li><b>Common supertype cache:</b> Stores computed common supertypes</li>
 * <li><b>Distance cache:</b> Stores hierarchy distance computations</li>
 * <li><b>Translatability cache:</b> Stores type translation compatibility checks</li>
 * </ul>
 *
 * <p>
 * All caches use a 5-minute expiration policy (expire after access) to balance memory usage with performance.
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Thread-safe type storage using ConcurrentHashMap</li>
 * <li>Automatic type ID allocation for species types</li>
 * <li>Support for type aliases</li>
 * <li>Parametric type decoding (list<int>, map<string,float>, etc.)</li>
 * <li>Hierarchical delegation to parent managers</li>
 * <li>Guava cache-based memoization of type relationships</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * // Create a types manager for a model
 * ITypesManager modelTypes = new TypesManager(Types.BUILT_IN_TYPES);
 *
 * // Initialize with model's species
 * modelTypes.init(modelDescription);
 *
 * // Look up types (searches local, then parent)
 * IType mySpecies = modelTypes.get("mySpecies"); // local
 * IType intType = modelTypes.get("int"); // from parent
 *
 * // Use cached type relationships
 * boolean assignable = modelTypes.checkAssignability(Types.FLOAT, Types.INT);
 *
 * // Decode parametric types
 * IType listOfInt = modelTypes.decodeType("list<int>");
 *
 * // Clean up when done
 * modelTypes.dispose();
 * }
 * </pre>
 *
 * @author drogoul
 * @since GAMA 1.0
 *
 * @see ITypesManager
 * @see Types
 * @see IType
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class TypesManager implements ITypesManager {

	static {
		DEBUG.OFF();
	}

	/**
	 * Creates a unique cache key from two types by combining their hash codes into a single long value.
	 *
	 * <p>
	 * The key is constructed by placing the first type's hash code in the upper 32 bits and the second type's hash code
	 * in the lower 32 bits. This creates a unique, order-sensitive key for caching binary type relationships.
	 * </p>
	 *
	 * <p>
	 * Implementation note: The bitwise operations ensure that different type pairs always produce different keys,
	 * avoiding cache collisions.
	 * </p>
	 *
	 * @param type1
	 *            the first type
	 * @param type2
	 *            the second type
	 * @return a unique long key representing the type pair
	 */
	public static Long createPairKey(final IType<?> type1, final IType<?> type2) {
		int t1 = type1.hashCode();
		int t2 = type2.hashCode();
		long key = (long) t1 << 32 | t2 & 0xFFFFFFFFL;
		return key;
	}

	/**
	 * Global counter for assigning unique IDs to dynamically created species types.
	 *
	 * <p>
	 * Species type IDs start at {@link IType#BEGINNING_OF_SPECIES_TYPES} (10000) and increment for each new species
	 * registered across all TypesManager instances.
	 * </p>
	 */
	public static int CURRENT_INDEX = IType.BEGINNING_OF_SPECIES_TYPES;

	/**
	 * The parent types manager to delegate to when a type is not found locally.
	 *
	 * <p>
	 * Typically points to {@link Types#BUILT_IN_TYPES} for model-level managers.
	 * </p>
	 */
	private TypesManager parent;

	/**
	 * Local registry mapping type names to type instances.
	 *
	 * <p>
	 * Thread-safe storage using ConcurrentHashMap. Contains both the canonical type names and their string ID
	 * representations.
	 * </p>
	 */
	private final ConcurrentHashMap<String, IType<?>> types = new ConcurrentHashMap<>();

	/**
	 * Set of unique type instances registered in this manager (prevents duplicates).
	 *
	 * <p>
	 * Uses a thread-safe Set to ensure the same type object is not registered multiple times under different names.
	 * </p>
	 */
	private final Set<IType<?>> uniqueTypes = ConcurrentHashMap.newKeySet();

	/**
	 * Cache for isAssignableFrom operations.
	 *
	 * <p>
	 * Maps type pair keys to Boolean results. Expires entries 5 minutes after last access to balance memory and
	 * performance.
	 * </p>
	 *
	 * @see #checkAssignability(IType, IType)
	 */
	private final Cache<Long, Boolean> assignabilityCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/**
	 * Cache for findCommonSupertypeWith operations.
	 *
	 * <p>
	 * Maps type pair keys to the computed common supertype. Expires entries 5 minutes after last access.
	 * </p>
	 *
	 * @see #computeCommonSupertype(IType, IType)
	 */
	private final Cache<Long, IType<?>> commonSupertypeCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/**
	 * Cache for distanceTo operations.
	 *
	 * <p>
	 * Maps type pair keys to Integer distance values. Expires entries 5 minutes after last access.
	 * </p>
	 *
	 * @see #computeDistance(IType, IType)
	 */
	private final Cache<Long, Integer> distanceCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/**
	 * Cache for isTranslatableInto operations.
	 *
	 * <p>
	 * Maps type pair keys to Boolean translatability results. Expires entries 5 minutes after last access.
	 * </p>
	 *
	 * @see #checkTranslatability(IType, IType)
	 */
	private final Cache<Long, Boolean> translatabilityCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/**
	 * Constructs a new TypesManager with an optional parent for delegation.
	 *
	 * @param types2
	 *            the parent types manager, or null for a root manager
	 */
	public TypesManager(final ITypesManager types2) {
		setParent(types2);
	}

	/**
	 * Returns a copy of all types registered locally in this manager.
	 *
	 * <p>
	 * This method returns only the types directly registered in this manager, not those from the parent. The returned
	 * set is a defensive copy to prevent external modification.
	 * </p>
	 *
	 * @return a new HashSet containing all locally registered types
	 */
	@Override
	public Set<IType<?>> getAllTypes() { return new HashSet(uniqueTypes); }

	/**
	 * Sets the parent types manager for hierarchical type resolution.
	 *
	 * <p>
	 * When a type lookup fails in this manager, it is delegated to the parent. This creates the type hierarchy where
	 * model-specific types can access built-in types.
	 * </p>
	 *
	 * @param parent
	 *            the parent types manager
	 */
	@Override
	public void setParent(final ITypesManager parent) { this.parent = (TypesManager) parent; }

	/**
	 * Registers an alias for an existing type name, allowing the type to be referenced by multiple names.
	 *
	 * <p>
	 * If the existing type is found in this manager's local registry, the alias is added locally. If the type doesn't
	 * exist locally, the alias is not created (it does not search the parent).
	 * </p>
	 *
	 * <p>
	 * Example:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Create an alias "integer" for "int"
	 * typesManager.alias("int", "integer");
	 *
	 * // Both names now resolve to the same type object
	 * assert typesManager.get("int") == typesManager.get("integer");
	 * }
	 * </pre>
	 *
	 * @param existingTypeName
	 *            the canonical name of the type to alias
	 * @param otherTypeName
	 *            the alias name to register
	 */
	@Override
	public void alias(final String existingTypeName, final String otherTypeName) {
		final IType t = types.get(existingTypeName);
		if (t != null) { types.put(otherTypeName, t); }
	}

	/**
	 * Registers a regular (non-species) type with metadata, making it available for GAML code.
	 *
	 * <p>
	 * This method performs the following operations:
	 * </p>
	 * <ul>
	 * <li>Sets the defining plugin for documentation purposes</li>
	 * <li>Registers the type name in the artefact proto registry for variable declarations</li>
	 * <li>Adds the type to the local registry</li>
	 * <li>Handles the special case of "unknown" types by returning NO_TYPE</li>
	 * </ul>
	 *
	 * <p>
	 * Example from plugin initialization:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Register a custom type from a plugin
	 * GamaCustomType customType = new GamaCustomType(manager);
	 * manager.addRegularType("custom", customType, "my.plugin.id");
	 * }
	 * </pre>
	 *
	 * @param <Support>
	 *            the Java support class of the type
	 * @param name
	 *            the GAML keyword for this type
	 * @param originalType
	 *            the type instance to register
	 * @param plugin
	 *            the identifier of the plugin defining this type
	 * @return the registered type, or Types.NO_TYPE if name is "unknown"
	 */
	@Override
	public <Support> IType<Support> addRegularType(final String name, final IType<Support> originalType,
			final String plugin) {
		IType<Support> type = originalType;
		if (IKeyword.UNKNOWN.equals(name)) { type = Types.NO_TYPE; }
		type.setDefiningPlugin(plugin);
		addType(type);
		return type;
	}

	/**
	 * Registers a species description as a type, allowing agents of that species to be referenced by type in GAML.
	 *
	 * <p>
	 * This method:
	 * </p>
	 * <ul>
	 * <li>Checks for duplicate species names and reports errors</li>
	 * <li>Handles the special "agent" species (returns existing agent type)</li>
	 * <li>Allocates a unique type ID for the species</li>
	 * <li>Creates a GamaAgentType instance</li>
	 * <li>Registers the Java class to type name mapping</li>
	 * <li>Adds the type to the local registry</li>
	 * </ul>
	 *
	 * <p>
	 * Example usage during model compilation:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Model defines: species mySpecies { ... }
	 * IType<IAgent> speciesType = modelTypes.addSpeciesType(speciesDescription);
	 *
	 * // Now can use in GAML:
	 * // mySpecies agent1 <- create mySpecies;
	 * }
	 * </pre>
	 *
	 * @param species
	 *            the species description to register as a type
	 * @return the created agent type for this species
	 */
	public IType<? extends IAgent> addSpeciesType(final ISpeciesDescription species) {
		final String name = species.getName();
		if (IKeyword.AGENT.equals(name)) return get(IKeyword.AGENT);
		if (!species.isBuiltIn() && containsType(name)) {
			species.error("Species " + name + " already declared. Species name must be unique",
					IGamlIssue.DUPLICATE_NAME, species.getUnderlyingElement(), name);
			return this.get(name);
		}
		GamaAgentType t = new GamaAgentType(this, species, ++CURRENT_INDEX);
		Types.addClassTypeCorrespondance(species.getJavaBase(), name);
		addType(t);
		return t;
	}

	/**
	 * Adds the class type.
	 *
	 * @param species
	 *            the species
	 * @return the i type<? extends I agent>
	 */
	public IType<? extends IObject> addClassType(final IClassDescription species) {
		final String name = species.getName();
		if (IKeyword.OBJECT.equals(name)) return get(IKeyword.OBJECT);
		if (!species.isBuiltIn() && containsType(name)) {
			species.error("Species " + name + " already declared. Species name must be unique",
					IGamlIssue.DUPLICATE_NAME, species.getUnderlyingElement(), name);
			return this.get(name);
		}
		GamaObjectType t = new GamaObjectType(this, species, name, ++CURRENT_INDEX, IObject.class);
		Types.addClassTypeCorrespondance(species.getJavaBase(), name);
		addType(t);
		return t;
	}

	/**
	 * Internal method to add a type to the local registry.
	 *
	 * <p>
	 * This method registers the type under two keys:
	 * </p>
	 * <ul>
	 * <li>Its string name (e.g., "int", "mySpecies")</li>
	 * <li>Its numeric ID as a string (e.g., "1" for int) - this is a legacy feature for backward compatibility</li>
	 * </ul>
	 *
	 * <p>
	 * The type is also added to the uniqueTypes set to prevent duplicate instances.
	 * </p>
	 *
	 * @param t
	 *            the type to register
	 */
	private void addType(final IType t) {
		final String name = t.toString();
		types.put(name, t);
		// Hack to allow types to be declared with their id as string
		types.put(String.valueOf(t.id()), t);
		uniqueTypes.add(t);
	}

	/**
	 * Initializes this types manager from a model description by registering all species types and setting up the type
	 * hierarchy.
	 *
	 * <p>
	 * This two-phase initialization process:
	 * </p>
	 * <ol>
	 * <li><b>Phase 1:</b> Visits all species in the model and registers them as types</li>
	 * <li><b>Phase 2:</b> Visits all species again to set up parent-child relationships in the type hierarchy based on
	 * species inheritance</li>
	 * </ol>
	 *
	 * <p>
	 * The two-phase approach ensures all species types exist before establishing relationships, avoiding null parent
	 * references.
	 * </p>
	 *
	 * <p>
	 * Example:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Model defines:
	 * // species animal { ... }
	 * // species dog parent: animal { ... }
	 *
	 * TypesManager modelTypes = new TypesManager(Types.BUILT_IN_TYPES);
	 * modelTypes.init(modelDescription);
	 *
	 * // After init:
	 * // - "animal" type exists with parent "agent"
	 * // - "dog" type exists with parent "animal"
	 * }
	 * </pre>
	 *
	 * @param model
	 *            the model description containing species definitions
	 */
	@Override
	public void collectAndInitializeTypesFrom(final IModelDescription model) {
		// We first add the species as types
		model.visitAllSpecies(entry -> {
			addSpeciesType(entry);
			return true;
		});
		model.visitAllClasses(clazz -> {
			addClassType(clazz);
			return true;
		});

		// Then we parent the types
		model.visitAllSpecies(entry -> {
			final IType type = get(entry.getName());
			if (!IKeyword.AGENT.equals(type.getName())) {
				final ITypeDescription parent = entry.getParent();
				// Takes care of invalid species (see Issue 711)
				type.setParent(parent == null || parent == entry ? get(IKeyword.AGENT) : get(parent.getName()));
			}
			return true;
		});
		model.visitAllClasses(clazz -> {
			final IType type = get(clazz.getName());
			if (!IKeyword.OBJECT.equals(type.getName())) {
				final ITypeDescription parent = clazz.getParent();
				// Takes care of invalid classes
				type.setParent(parent == null || parent == clazz ? get(IKeyword.OBJECT) : get(parent.getName()));
			}
			return true;
		});

	}

	/**
	 * Checks if a type with the given name exists in this manager or the parent hierarchy.
	 *
	 * <p>
	 * This method searches locally first, then delegates to the parent if not found.
	 * </p>
	 *
	 * @param s
	 *            the type name to check
	 * @return true if the type exists locally or in the parent hierarchy
	 */
	@Override
	public boolean containsType(final String s) {
		final IType t = types.get(s);
		if (t != null) return true;
		if (parent == null) return false;
		return parent.containsType(s);
	}

	/**
	 * Retrieves a type by name, returning NO_TYPE if not found.
	 *
	 * <p>
	 * Convenience method that delegates to {@link #get(String, IType)} with Types.NO_TYPE as the default.
	 * </p>
	 *
	 * @param type
	 *            the type name
	 * @return the type, or Types.NO_TYPE if not found
	 */
	@Override
	public IType get(final String type) {
		return get(type, Types.NO_TYPE);
	}

	/**
	 * Retrieves a type by name with a custom default value.
	 *
	 * <p>
	 * Search order:
	 * </p>
	 * <ol>
	 * <li>Check if type is null → return defaultValue</li>
	 * <li>Look in local registry → return if found</li>
	 * <li>Delegate to parent (if exists) → return parent result</li>
	 * <li>No parent → return defaultValue</li>
	 * </ol>
	 *
	 * @param type
	 *            the type name to look up
	 * @param defaultValue
	 *            the value to return if type not found
	 * @return the found type or defaultValue
	 */
	@Override
	public IType get(final String type, final IType defaultValue) {
		if (type == null) return defaultValue;
		final IType t = types.get(type);
		if (t != null) return t;
		if (parent == null) return defaultValue;
		return parent.get(type, defaultValue);
	}

	/**
	 * Clears all locally registered types and invalidates all caches.
	 *
	 * <p>
	 * This method performs a complete cleanup:
	 * </p>
	 * <ul>
	 * <li>Clears the types map (removes all type name → type mappings)</li>
	 * <li>Clears the uniqueTypes set</li>
	 * <li>Invalidates all 4 relationship caches (assignability, common supertype, distance, translatability)</li>
	 * </ul>
	 *
	 * <p>
	 * After disposal, this manager is empty but can still delegate to its parent. Call this when a model is unloaded to
	 * free memory.
	 * </p>
	 */
	@Override
	public void dispose() {
		types.clear();
		uniqueTypes.clear();
		assignabilityCache.invalidateAll();
		commonSupertypeCache.invalidateAll();
		distanceCache.invalidateAll();
		translatabilityCache.invalidateAll();
	}

	/**
	 * Parses and decodes a type expression that may contain parametric type parameters.
	 *
	 * <p>
	 * This powerful method handles complex nested type expressions with angle bracket notation:
	 * </p>
	 * <ul>
	 * <li>Simple types: "int", "float", "string"</li>
	 * <li>Single parameter containers: "list<int>", "species<mySpecies>"</li>
	 * <li>Two parameter containers: "map<string,float>", "pair<int,bool>"</li>
	 * <li>Deeply nested types: "list<map<string,list<int>>>"</li>
	 * </ul>
	 *
	 * <p>
	 * The parser correctly handles:
	 * </p>
	 * <ul>
	 * <li>Nested angle brackets by tracking depth</li>
	 * <li>Multiple parameters separated by commas</li>
	 * <li>Whitespace around type names and parameters</li>
	 * </ul>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Simple type
	 * IType t1 = manager.decodeType("int");
	 *
	 * // List with parameter
	 * IType t2 = manager.decodeType("list<int>");
	 * // Equivalent to: Types.LIST.of(Types.INT)
	 *
	 * // Map with two parameters
	 * IType t3 = manager.decodeType("map<string,float>");
	 * // Equivalent to: Types.MAP.of(Types.STRING, Types.FLOAT)
	 *
	 * // Nested types
	 * IType t4 = manager.decodeType("list<map<string,int>>");
	 * // List of maps from string to int
	 *
	 * // Complex nesting
	 * IType t5 = manager.decodeType("map<string,list<pair<int,float>>>");
	 * }
	 * </pre>
	 *
	 * @param s
	 *            the type expression string to parse
	 * @return the decoded type with all parameters resolved, or Types.NO_TYPE if parsing fails
	 */
	@Override
	public IType<?> decodeType(final String s) {
		if (s == null || s.isEmpty()) return Types.NO_TYPE;
		int index = s.indexOf('<');
		if (index == -1) return get(s);
		String baseName = s.substring(0, index);
		IType base = get(baseName);
		if (!(base instanceof IContainerType)) return base;
		String params = s.substring(index + 1, s.lastIndexOf('>'));
		List<String> args = new ArrayList<>();
		int depth = 0;
		int start = 0;
		int length = params.length();
		for (int i = 0; i < length; i++) {
			char c = params.charAt(i);
			if (c == '<') {
				depth++;
			} else if (c == '>') {
				depth--;
			} else if (c == ',' && depth == 0) {
				args.add(params.substring(start, i).trim());
				start = i + 1;
			}
		}
		args.add(params.substring(start).trim());

		IType key = args.isEmpty() ? Types.NO_TYPE : decodeType(args.get(0));
		if (args.size() == 1) return ((IContainerType) base).of(key);
		IType content = decodeType(args.get(args.size() - 1));
		return ((IContainerType) base).of(key, content);
	}

	// ========== Type Relation Caching Methods ==========

	/**
	 * Checks if one type is assignable from another, using cache when possible. This is the cached version of
	 * {@link IType#isAssignableFrom(IType)}.
	 *
	 * @param from
	 *            the type to check (target type)
	 * @param to
	 *            the type being assigned (source type)
	 * @return true if 'from' is assignable from 'to'
	 */
	@Override
	public boolean checkAssignability(final IType<?> from, final IType<?> to) {
		if (from == null || to == null) return false;
		if (from == to) return true;
		final Long key = createPairKey(from, to);
		try {
			return assignabilityCache.get(key, () -> from.computeIsAssignableFrom(to));
		} catch (ExecutionException e) {
			return false;
		}

	}

	/**
	 * Finds the common supertype between two types, using cache when possible. This is the cached version of
	 * {@link IType#findCommonSupertypeWith(IType)}.
	 *
	 * @param type1
	 *            the first type
	 * @param type2
	 *            the second type
	 * @return the common supertype, or Types.NO_TYPE if none found
	 */
	@Override
	public IType<?> computeCommonSupertype(final IType<?> type1, final IType<?> type2) {
		if (type1 == null || type2 == null) return Types.NO_TYPE;
		if (type1 == type2) return type1;
		final Long key = createPairKey(type1, type2);
		try {
			return commonSupertypeCache.get(key, () -> type1.computeFindCommonSupertypeWith(type2));
		} catch (ExecutionException e) {
			return Types.NO_TYPE;
		}
	}

	/**
	 * Computes the distance between two types in the type hierarchy, using cache when possible. This is the cached
	 * version of {@link IType#distanceTo(IType)}.
	 *
	 * @param from
	 *            the starting type
	 * @param to
	 *            the target type
	 * @return the distance (number of steps in hierarchy), or Integer.MAX_VALUE if unreachable
	 */
	@Override
	public int computeDistance(final IType<?> from, final IType<?> to) {
		if (from == null || to == null) return Integer.MAX_VALUE;
		if (from == to) return 0;
		final Long key = createPairKey(from, to);
		try {
			return distanceCache.get(key, () -> from.computeDistanceTo(to));
		} catch (ExecutionException e) {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * Checks if one type is translatable into another, using cache when possible. This is the cached version of
	 * {@link IType#isTranslatableInto(IType)}.
	 *
	 * @param from
	 *            the source type
	 * @param to
	 *            the target type
	 * @return true if 'from' is translatable into 'to'
	 */
	@Override
	public boolean checkTranslatability(final IType<?> from, final IType<?> to) {
		if (from == null || to == null) return false;
		if (from == to) return true;
		final Long key = createPairKey(from, to);
		try {
			return translatabilityCache.get(key, () -> from.computeIsTranslatableInto(to));
		} catch (ExecutionException e) {
			return false;
		}
	}

}
