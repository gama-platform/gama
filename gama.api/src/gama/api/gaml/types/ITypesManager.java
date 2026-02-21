/*******************************************************************************************************
 *
 * ITypesManager.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.util.Set;

import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.kernel.agent.IAgent;
import gama.api.utils.interfaces.IDisposable;

/**
 * Interface for managing GAMA types in a hierarchical registry system. A types manager stores, retrieves, and manages
 * relationships between types within a specific scope (built-in types, model types, or experiment types).
 * 
 * <p>
 * Types managers form a hierarchy where child managers can delegate to parent managers for type lookups. This allows:
 * </p>
 * <ul>
 * <li>Built-in types to be accessible from all models</li>
 * <li>Model-specific species types to be scoped to that model</li>
 * <li>Type aliases to be defined locally</li>
 * <li>Efficient caching of expensive type relationship computations</li>
 * </ul>
 * 
 * <p>
 * The manager also provides cached implementations of type relationship methods (assignability, distance, common
 * supertypes, etc.) to avoid recomputing expensive operations.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * // Get the built-in types manager
 * ITypesManager builtInTypes = Types.builtInTypes;
 * 
 * // Look up a type
 * IType<Integer> intType = builtInTypes.get("int");
 * 
 * // Create a model-specific types manager
 * ITypesManager modelTypes = new TypesManager(builtInTypes);
 * 
 * // Register a species type
 * modelTypes.addSpeciesType(speciesDescription);
 * 
 * // Use cached type relationship checks
 * boolean assignable = modelTypes.checkAssignability(Types.FLOAT, Types.INT);
 * }
 * </pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see TypesManager
 * @see Types
 * @see IType
 */
public interface ITypesManager extends IDisposable {

	// ==================== Type Registration and Aliasing ====================

	/**
	 * Creates an alias for an existing type, allowing it to be referenced by an alternative name.
	 * 
	 * <p>
	 * Aliases are useful for providing alternative keywords or short names for types.
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Make "integer" an alias for "int"
	 * typesManager.alias("int", "integer");
	 * 
	 * // Both names now resolve to the same type
	 * IType t1 = typesManager.get("int");
	 * IType t2 = typesManager.get("integer");
	 * // t1 == t2
	 * }
	 * </pre>
	 *
	 * @param existingTypeName
	 *            the canonical name of the existing type
	 * @param otherTypeName
	 *            the alias name to register
	 */
	void alias(String existingTypeName, String otherTypeName);

	/**
	 * Checks if a type with the given name exists in this manager or its parent hierarchy.
	 *
	 * @param s
	 *            the type name to check
	 * @return true if the type exists, false otherwise
	 * 
	 * @see #get(String)
	 */
	boolean containsType(String s);

	/**
	 * Retrieves a type by its name. This is the primary method for type lookup in GAMA.
	 * 
	 * <p>
	 * The lookup searches this manager first, then delegates to the parent manager if not found. If no type is found
	 * in the entire hierarchy, returns {@link Types#NO_TYPE} instead of null.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * IType intType = typesManager.get("int");
	 * IType mySpeciesType = typesManager.get("mySpecies");
	 * IType unknownType = typesManager.get("nonexistent"); // returns NO_TYPE
	 * }
	 * </pre>
	 *
	 * @param type
	 *            the name of the type to retrieve
	 * @return the type instance, or {@link Types#NO_TYPE} if not found (never returns null)
	 * 
	 * @see #get(String, IType)
	 * @see Types#NO_TYPE
	 */
	IType<?> get(final String type);

	/**
	 * Retrieves a type by its name, with a custom default value if not found.
	 * 
	 * <p>
	 * This variant allows callers to specify what to return when a type is not found, instead of always returning
	 * NO_TYPE.
	 * </p>
	 *
	 * @param type
	 *            the name of the type to retrieve
	 * @param defaultValue
	 *            the value to return if the type is not found
	 * @return the type instance, or defaultValue if not found
	 * 
	 * @see #get(String)
	 */
	IType<?> get(String type, IType<?> defaultValue);

	/**
	 * Registers a species description as a type in this manager.
	 * 
	 * <p>
	 * Species types allow agents of that species to be referenced by type in GAML code. The species name becomes a
	 * type name that can be used in variable declarations.
	 * </p>
	 * 
	 * <p>
	 * Example GAML usage after registration:
	 * </p>
	 * 
	 * <pre>
	 * species mySpecies {
	 *     // ...
	 * }
	 * 
	 * // After registration, can use as a type:
	 * mySpecies agent1 <- create mySpecies;
	 * list<mySpecies> agents <- mySpecies as list;
	 * </pre>
	 *
	 * @param species
	 *            the species description to register
	 * @return the created species type
	 * 
	 * @see ISpeciesDescription
	 */
	IType<? extends IAgent> addSpeciesType(ISpeciesDescription species);

	/**
	 * Initializes this types manager with a model description, registering all species defined in the model.
	 * 
	 * <p>
	 * This method:
	 * </p>
	 * <ul>
	 * <li>Visits all species in the model</li>
	 * <li>Registers each species as a type</li>
	 * <li>Sets up the type hierarchy based on species inheritance</li>
	 * </ul>
	 *
	 * @param model
	 *            the model description containing species to register
	 * 
	 * @see #addSpeciesType(ISpeciesDescription)
	 */
	void init(IModelDescription model);

	/**
	 * Sets the parent types manager for delegation.
	 * 
	 * <p>
	 * When a type is not found in this manager, the lookup is delegated to the parent. This creates a hierarchy:
	 * model types -> built-in types.
	 * </p>
	 *
	 * @param typesManager
	 *            the parent types manager
	 */
	void setParent(ITypesManager typesManager);

	/**
	 * Registers a regular (non-species) type in this manager with metadata.
	 * 
	 * <p>
	 * This method is used to register built-in types and custom types defined by plugins. It:
	 * </p>
	 * <ul>
	 * <li>Associates the type with its GAML keyword name</li>
	 * <li>Records which plugin defines the type</li>
	 * <li>Registers the type in the artefact proto registry</li>
	 * </ul>
	 * 
	 * <p>
	 * Example from plugin initialization:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * GamaMyCustomType customType = new GamaMyCustomType();
	 * typesManager.addRegularType("mycustom", customType, "my.plugin.id");
	 * }
	 * </pre>
	 *
	 * @param <Support>
	 *            the Java support class of the type
	 * @param name
	 *            the keyword used in GAML to reference this type
	 * @param typeInstance
	 *            the type instance to register
	 * @param pluginName
	 *            the identifier of the plugin defining this type
	 * @return the registered type instance (or Types.NO_TYPE if name is "unknown")
	 */
	<Support> IType<Support> addRegularType(String name, IType<Support> typeInstance, String pluginName);

	/**
	 * Returns the set of all types registered in this manager (excluding parent types).
	 * 
	 * <p>
	 * This returns only the types directly registered in this manager, not those inherited from the parent.
	 * </p>
	 *
	 * @return a set of all types in this manager
	 */
	Set<IType<?>> getAllTypes();

	/**
	 * Parses and decodes a type expression that may include parametric type parameters.
	 * 
	 * <p>
	 * This method handles complex type expressions with angle bracket notation:
	 * </p>
	 * <ul>
	 * <li>Simple types: "int", "string", "agent"</li>
	 * <li>Single parameter: "list<int>", "species<mySpecies>"</li>
	 * <li>Two parameters: "map<string,float>", "pair<int,string>"</li>
	 * <li>Nested parameters: "list<map<string,list<int>>>"</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * IType t1 = typesManager.decodeType("list<int>");
	 * // Returns a list type with int content type
	 * 
	 * IType t2 = typesManager.decodeType("map<string,float>");
	 * // Returns a map type with string keys and float values
	 * 
	 * IType t3 = typesManager.decodeType("list<map<string,int>>");
	 * // Returns a list of maps
	 * }
	 * </pre>
	 *
	 * @param type
	 *            the type expression to parse (can be simple or parametric)
	 * @return the decoded type, or {@link Types#NO_TYPE} if parsing fails
	 */
	IType decodeType(String type);

	// ==================== Cached Type Relationship Methods ====================

	/**
	 * Checks if one type is assignable from another, using cache when possible.
	 * 
	 * <p>
	 * This is the cached version of {@link IType#isAssignableFrom(IType)}. It delegates to
	 * {@link IType#computeIsAssignableFrom(IType)} for the actual computation but caches the result for performance.
	 * </p>
	 * 
	 * <p>
	 * The cache key is based on the pair of types, and results are stored for 5 minutes of inactivity.
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Check if float can accept int values
	 * boolean canAssign = typesManager.checkAssignability(Types.FLOAT, Types.INT); // true
	 * 
	 * // Subsequent calls use cached result
	 * boolean cached = typesManager.checkAssignability(Types.FLOAT, Types.INT); // from cache
	 * }
	 * </pre>
	 *
	 * @param from
	 *            the target type (left side of assignment)
	 * @param to
	 *            the source type (right side of assignment)
	 * @return true if 'from' is assignable from 'to'
	 * 
	 * @see IType#isAssignableFrom(IType)
	 * @see IType#computeIsAssignableFrom(IType)
	 */
	boolean checkAssignability(IType<?> from, IType<?> to);

	/**
	 * Finds the common supertype between two types, using cache when possible.
	 * 
	 * <p>
	 * This is the cached version of {@link IType#findCommonSupertypeWith(IType)}. It delegates to
	 * {@link IType#computeFindCommonSupertypeWith(IType)} for the actual computation but caches the result.
	 * </p>
	 * 
	 * <p>
	 * The common supertype is the most specific type that both input types can be assigned to. This is used for type
	 * inference in expressions with mixed types.
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Find common type for int and float
	 * IType common = typesManager.computeCommonSupertype(Types.INT, Types.FLOAT);
	 * // Returns Types.FLOAT
	 * 
	 * // Find common type for point and geometry
	 * IType common2 = typesManager.computeCommonSupertype(Types.POINT, Types.GEOMETRY);
	 * // Returns Types.GEOMETRY
	 * }
	 * </pre>
	 *
	 * @param type1
	 *            the first type
	 * @param type2
	 *            the second type
	 * @return the common supertype, or Types.NO_TYPE if none found
	 * 
	 * @see IType#findCommonSupertypeWith(IType)
	 * @see IType#computeFindCommonSupertypeWith(IType)
	 */
	IType<?> computeCommonSupertype(IType<?> type1, IType<?> type2);

	/**
	 * Computes the distance between two types in the type hierarchy, using cache when possible.
	 * 
	 * <p>
	 * This is the cached version of {@link IType#distanceTo(IType)}. It delegates to
	 * {@link IType#computeDistanceTo(IType)} for the actual computation but caches the result.
	 * </p>
	 * 
	 * <p>
	 * The distance represents the number of parent-child steps in the type hierarchy. It's used for:
	 * </p>
	 * <ul>
	 * <li>Operator overload resolution (prefer closer matches)</li>
	 * <li>Type inference (choose most specific type)</li>
	 * <li>Compatibility checking</li>
	 * </ul>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Distance from int to itself
	 * int d1 = typesManager.computeDistance(Types.INT, Types.INT); // 0
	 * 
	 * // Distance up the hierarchy
	 * int d2 = typesManager.computeDistance(Types.CONTAINER, Types.LIST); // 1
	 * 
	 * // No relationship
	 * int d3 = typesManager.computeDistance(Types.STRING, Types.INT); // Integer.MAX_VALUE
	 * }
	 * </pre>
	 *
	 * @param from
	 *            the starting type
	 * @param to
	 *            the target type
	 * @return the distance (0 = same type, positive = hierarchy steps, MAX_VALUE = unreachable)
	 * 
	 * @see IType#distanceTo(IType)
	 * @see IType#computeDistanceTo(IType)
	 */
	int computeDistance(IType<?> from, IType<?> to);

	/**
	 * Checks if one type is translatable into another, using cache when possible.
	 * 
	 * <p>
	 * This is the cached version of {@link IType#isTranslatableInto(IType)}. It delegates to
	 * {@link IType#computeIsTranslatableInto(IType)} for the actual computation but caches the result.
	 * </p>
	 * 
	 * <p>
	 * Translation is more permissive than assignment and allows conversions with potential information loss (e.g.,
	 * float to int via truncation, string to int via parsing).
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Check if float can be translated to int
	 * boolean canTranslate = typesManager.checkTranslatability(Types.FLOAT, Types.INT); // true
	 * 
	 * // Check if string can be translated to int
	 * boolean canParse = typesManager.checkTranslatability(Types.STRING, Types.INT); // true
	 * }
	 * </pre>
	 *
	 * @param from
	 *            the source type
	 * @param to
	 *            the target type
	 * @return true if 'from' is translatable into 'to'
	 * 
	 * @see IType#isTranslatableInto(IType)
	 * @see IType#computeIsTranslatableInto(IType)
	 */
	boolean checkTranslatability(IType<?> from, IType<?> to);

}