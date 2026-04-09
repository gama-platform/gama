/*******************************************************************************************************
 *
 * IType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.util.Map;

import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.artefacts.IArtefact.Operator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import gama.api.utils.json.IJsonable;

/**
 * The central interface defining the type system in GAMA. All GAML types (built-in, species, parametric containers,
 * etc.) implement this interface.
 *
 * <p>
 * IType provides a comprehensive API for:
 * </p>
 * <ul>
 * <li>Type casting and conversion operations</li>
 * <li>Type hierarchy and relationships (assignability, distance, common supertypes)</li>
 * <li>Container and parametric type support</li>
 * <li>Integration with GAML expressions and runtime scopes</li>
 * <li>Serialization/deserialization to/from JSON</li>
 * <li>Documentation and metadata</li>
 * </ul>
 *
 * <p>
 * Each type has a unique integer ID, a name (used in GAML code), and a Java support class that represents values of
 * this type at runtime.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * // Get a built-in type
 * IType<Integer> intType = Types.INT;
 *
 * // Cast a value to this type
 * Integer value = intType.cast(scope, "42", null, false);
 *
 * // Check type relationships
 * if (Types.FLOAT.isAssignableFrom(Types.INT)) {
 * 	// INT values can be assigned to FLOAT variables
 * }
 *
 * // Create parametric types
 * IContainerType<IList<Integer>> listOfInt = Types.LIST.of(Types.INT);
 * }
 * </pre>
 *
 * @param <Support>
 *            the Java class representing values of this type at runtime (e.g., Integer for int, GamaPoint for point)
 *
 * @author drogoul
 * @since GAMA 1.0
 *
 * @see GamaType
 * @see IContainerType
 * @see Types
 * @see ITypesManager
 */
public interface IType<Support> extends IGamlDescription, ITyped, IJsonable {

	/** Array of vowels used for article determination ("a" vs "an") in error messages. */
	String[] vowels = { "a", "e", "i", "o", "u", "y" };

	// ==================== Special Type IDs (for facets and internal use) ====================

	/** Type ID for label facets. */
	int LABEL = -200;

	/** Type ID for identifier facets. */
	int ID = -201;

	/** Type ID for type facets. */
	int TYPE_ID = -202;

	/** Type ID for new variable declarations. */
	int NEW_VAR_ID = -203;

	/** Type ID for temporary variables. */
	int NEW_TEMP_ID = -204;

	// ==================== Built-in Type IDs ====================

	/** Type ID for the "none" type (GamaNoType), representing the absence of a type. */
	int NONE = 0;

	/** Type ID for the int type (integer numbers). */
	int INT = 1;

	/** Type ID for the float type (floating-point numbers). */
	int FLOAT = 2;

	/** Type ID for the bool type (boolean values: true/false). */
	int BOOL = 3;

	/** Type ID for the string type (text values). */
	int STRING = 4;

	/** Type ID for the list type (ordered collections). */
	int LIST = 5;

	/** Type ID for the color type (RGB/RGBA colors). */
	int COLOR = 6;

	/** Type ID for the point type (2D/3D coordinates). */
	int POINT = 7;

	/** Type ID for the matrix type (2D arrays of values). */
	int MATRIX = 8;

	/** Type ID for the pair type (key-value pairs). */
	int PAIR = 9;

	/** Type ID for the map type (associative arrays). */
	int MAP = 10;

	/** Type ID for the agent type (simulation agents). */
	int AGENT = 11;

	/** Type ID for the file type (external data files). */
	int FILE = 12;

	/** Type ID for the geometry type (spatial geometries). */
	int GEOMETRY = 13;

	/** Type ID for the species type (agent species containers). */
	int SPECIES = 14;

	/** Type ID for the graph type (network structures). */
	int GRAPH = 15;

	/** Type ID for the container type (abstract supertype of all containers). */
	int CONTAINER = 16;

	/** Type ID for the path type (spatial paths). */
	int PATH = 17;

	/** Type ID for the topology type (spatial topologies). */
	int TOPOLOGY = 18;

	/** Type ID for the font type (text fonts). */
	int FONT = 19;

	/** Type ID for the image type (raster images). */
	int IMAGE = 20;

	/** Type ID for the regression type (statistical regression models). */
	int REGRESSION = 21;

	/** Type ID for the skill type (agent skills/capabilities). */
	int SKILL = 22;

	/** Type ID for the date type (temporal values). */
	int DATE = 23;

	/** Type ID for the message type (FIPA messages for agent communication). */
	int MESSAGE = 24;

	/** Type ID for the action type (executable actions). */
	int ACTION = 26;

	/** Type ID for the attributes type (attribute maps). */
	int ATTRIBUTES = 27;

	/** Type ID for the type type (meta-type representing types themselves). */
	int TYPE = 28;

	/** Type ID for the KML type (KML geographic files). */
	int KML = 29;

	/** Type ID for the directory type (file system directories). */
	int DIRECTORY = 30;

	/** Type ID for the field type (continuous spatial fields). */
	int FIELD = 31;

	/** The class. */
	int CLASS = 32;

	/** The object. */
	int OBJECT = 33;

	/** Type ID for the anova type (statistical analysis of variance). */
	int ANOVA = 34;

	// ==================== Type ID Ranges ====================

	/** Starting ID for custom user-defined types. IDs >= this value are for custom types. */
	int BEGINNING_OF_CUSTOM_TYPES = 5000;

	/** Starting ID for species types. IDs >= this value represent agent species. */
	int BEGINNING_OF_SPECIES_TYPES = 10000;

	/** Starting ID for file types. IDs >= this value represent specific file formats. */
	int BEGINNING_OF_FILE_TYPES = 20000;

	// ==================== Core Type Casting Methods ====================

	/**
	 * Casts an object to this type's support class. This is the fundamental conversion operation in GAMA's type system.
	 *
	 * <p>
	 * Examples:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Cast string to int
	 * Integer value = Types.INT.cast(scope, "42", null, false); // returns 42
	 *
	 * // Cast with copy flag
	 * IList<Integer> copiedList = Types.LIST.cast(scope, originalList, null, true);
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope, provides access to agents, simulation state, etc.
	 * @param obj
	 *            the object to cast (can be any Java object)
	 * @param param
	 *            optional parameter for the casting operation (type-specific, can be null)
	 * @param copy
	 *            if true, creates a copy of the object rather than reusing it
	 * @return the object cast to this type's support class, or the default value if casting fails
	 *
	 * @see #getDefault()
	 */
	Support cast(IScope scope, Object obj, Object param, boolean copy);

	/**
	 * Casts an object to this type with explicit key and content types (for parametric container types).
	 *
	 * <p>
	 * This method is primarily used for container types (list, map, matrix, etc.) that have parametric content.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Cast to list<int>
	 * IList<Integer> intList = Types.LIST.cast(scope, rawList, null, Types.INT, Types.NO_TYPE, false);
	 *
	 * // Cast to map<string, float>
	 * IMap<String, Double> stringFloatMap = Types.MAP.cast(scope, rawMap, null, Types.STRING, Types.FLOAT, false);
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast
	 * @param param
	 *            optional parameter for the casting operation
	 * @param keyType
	 *            the type of keys (for maps) or indices (for other containers)
	 * @param contentType
	 *            the type of content/values in the container
	 * @param copy
	 *            if true, creates a copy of the object
	 * @return the object cast to this type's support class with specified parametric types
	 */
	Support cast(IScope scope, Object obj, Object param, IType<?> keyType, IType<?> contentType, boolean copy);

	// ==================== Type Identity and Metadata ====================

	/**
	 * Returns the unique integer identifier of this type.
	 *
	 * <p>
	 * Type IDs are used for efficient type comparison and switching. Built-in types have fixed IDs (see constants
	 * above), while dynamic types (species, custom types) have IDs allocated at runtime.
	 * </p>
	 *
	 * @return the type ID
	 *
	 * @see #NONE
	 * @see #INT
	 * @see #FLOAT
	 */
	int id();

	/**
	 * Returns the Java class that represents values of this type at runtime.
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>For Types.INT, returns Integer.class</li>
	 * <li>For Types.POINT, returns GamaPoint.class</li>
	 * <li>For Types.LIST, returns IList.class</li>
	 * </ul>
	 *
	 * @return the Java support class for this type
	 */
	Class<? extends Support> toClass();

	/**
	 * Returns the default value for this type. Used when initializing variables without explicit values.
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.INT.getDefault() returns 0</li>
	 * <li>Types.BOOL.getDefault() returns false</li>
	 * <li>Types.STRING.getDefault() returns ""</li>
	 * <li>Types.LIST.getDefault() returns an empty list</li>
	 * </ul>
	 *
	 * @return the default value for this type (never null)
	 */
	Support getDefault();

	/**
	 * Returns the variable kind constant for this type, used in GAML grammar and variable declarations.
	 *
	 * <p>
	 * Variable kinds distinguish between different syntactic categories (regular variables, temporary variables,
	 * parameters, etc.).
	 * </p>
	 *
	 * @return the variable kind constant
	 */
	ISymbolKind getVarKind();

	// ==================== Field and Attribute Access ====================

	/**
	 * Retrieves the getter operation for a named field/attribute of this type.
	 *
	 * <p>
	 * Fields represent attributes and pseudo-attributes that can be accessed using the dot notation in GAML (e.g.,
	 * {@code myPoint.x}, {@code myAgent.location}).
	 * </p>
	 *
	 * <p>
	 * Example:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Get the "x" field getter for point type
	 * IArtefact xGetter = Types.POINT.getGetter("x");
	 * }
	 * </pre>
	 *
	 * @param name
	 *            the field name
	 * @return the field getter operation, or null if no such field exists
	 *
	 * @see #getFieldGetters()
	 */
	IArtefact getGetter(String name);

	/**
	 * Returns a map of all field getters available for this type.
	 *
	 * <p>
	 * The map keys are field names, and values are the corresponding operator prototypes.
	 * </p>
	 *
	 * @return an immutable map of field name to operator
	 *
	 * @see #setFieldGetters(Map)
	 */
	Map<String, Operator> getFieldGetters();

	/**
	 * Sets the field getters for this type. Called during type initialization.
	 *
	 * @param map
	 *            a map from field names to operator prototypes
	 */
	void setFieldGetters(Map<String, IArtefact.Operator> map);

	// ==================== Type Classification ====================

	/**
	 * Checks if this type represents an agent or agent species.
	 *
	 * <p>
	 * Returns true for the agent type and all species types.
	 * </p>
	 *
	 * @return true if this is an agent type
	 *
	 * @see #isSkillType()
	 * @see #getSpecies()
	 */
	boolean isAgentType();

	/**
	 * Checks if this type represents a skill.
	 *
	 * <p>
	 * Skills are reusable behaviors that can be added to agent species.
	 * </p>
	 *
	 * @return true if this is a skill type
	 */
	boolean isSkillType();

	/**
	 * Checks if this type is parametric (can accept type parameters).
	 *
	 * <p>
	 * Container types (list, map, matrix, etc.) and file types are parametric.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.LIST.isParametricType() returns true</li>
	 * <li>Types.INT.isParametricType() returns false</li>
	 * </ul>
	 *
	 * @return true if this type can be parameterized
	 *
	 * @see #isParametricFormOf(IType)
	 * @see #getNumberOfParameters()
	 */
	boolean isParametricType();

	/**
	 * Checks if this type is a parameterized form of another type.
	 *
	 * <p>
	 * For example, {@code list<int>} is a parametric form of {@code list}.
	 * </p>
	 *
	 * @param l
	 *            the base type to check against
	 * @return true if this type is a parameterized version of l
	 */
	boolean isParametricFormOf(final IType<?> l);

	// ==================== Species-related Methods ====================

	/**
	 * Returns the species name if this type represents an agent species.
	 *
	 * <p>
	 * For example, if this type represents a species defined as {@code species mySpecies {...}}, this returns
	 * "mySpecies".
	 * </p>
	 *
	 * @return the species name, or null if this is not a species type
	 *
	 * @see #getSpecies()
	 * @see #isAgentType()
	 */
	String getSpeciesName();

	/**
	 * Returns the species description if this type represents an agent species.
	 *
	 * <p>
	 * The species description contains all metadata about the species: its attributes, actions, parent species, etc.
	 * </p>
	 *
	 * @return the species description, or null if this is not a species type
	 *
	 * @see #getDenotedSpecies()
	 */
	ITypeDescription getSpecies();

	/**
	 * Returns the species description denoted by this type (for species container types).
	 *
	 * <p>
	 * This is used for types that represent containers of agents of a specific species, such as when using species as a
	 * type (e.g., {@code mySpecies allAgents <- mySpecies;}).
	 * </p>
	 *
	 * @return the denoted species, or null if not applicable
	 */
	ITypeDescription getDenotedSpecies();

	// ==================== Type Hierarchy and Relationships ====================

	/**
	 * Checks if a value of type {@code l} can be assigned to a variable of this type without explicit casting.
	 *
	 * <p>
	 * This is the fundamental assignability check in GAMA's type system. The relation is transitive: if A is assignable
	 * from B and B is assignable from C, then A is assignable from C.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.FLOAT.isAssignableFrom(Types.INT) returns true (ints can be assigned to floats)</li>
	 * <li>Types.INT.isAssignableFrom(Types.FLOAT) returns false (floats cannot be assigned to ints without
	 * casting)</li>
	 * <li>Types.GEOMETRY.isAssignableFrom(Types.POINT) returns true (points are geometries)</li>
	 * </ul>
	 *
	 * @param l
	 *            the type to check
	 * @return true if values of type l can be assigned to variables of this type
	 *
	 * @see #computeIsAssignableFrom(IType)
	 * @see #isTranslatableInto(IType)
	 */
	boolean isAssignableFrom(IType<?> l);

	/**
	 * Internal computation of isAssignableFrom without cache lookup. Used by TypesManager to avoid infinite recursion.
	 *
	 * <p>
	 * Implementations should provide the actual assignability logic without delegating to the cache. This method is
	 * called by {@link ITypesManager#checkAssignability(IType, IType)} which manages caching.
	 * </p>
	 *
	 * @param l
	 *            the type to check
	 * @return true if is assignable from
	 *
	 * @see ITypesManager#checkAssignability(IType, IType)
	 */
	boolean computeIsAssignableFrom(IType<?> l);

	/**
	 * Checks if values of this type can be translated (converted with information loss) into the target type.
	 *
	 * <p>
	 * Translation is more permissive than assignment. For example, float can be translated into int (with truncation),
	 * even though float is not assignable to int.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.FLOAT.isTranslatableInto(Types.INT) returns true (truncation)</li>
	 * <li>Types.STRING.isTranslatableInto(Types.INT) returns true (parsing)</li>
	 * <li>Types.POINT.isTranslatableInto(Types.LIST) returns true (extraction of coordinates)</li>
	 * </ul>
	 *
	 * @param t
	 *            the target type
	 * @return true if this type can be translated into the target type
	 *
	 * @see #computeIsTranslatableInto(IType)
	 * @see #isAssignableFrom(IType)
	 */
	boolean isTranslatableInto(IType<?> t);

	/**
	 * Internal computation of isTranslatableInto without cache lookup. Used by TypesManager to avoid infinite
	 * recursion.
	 *
	 * <p>
	 * Implementations should provide the actual translatability logic without delegating to the cache. This method is
	 * called by {@link ITypesManager#checkTranslatability(IType, IType)} which manages caching.
	 * </p>
	 *
	 * @param t
	 *            the target type
	 * @return true if translatable
	 *
	 * @see ITypesManager#checkTranslatability(IType, IType)
	 */
	boolean computeIsTranslatableInto(IType<?> t);

	/**
	 * Sets the parent type in the type hierarchy.
	 *
	 * <p>
	 * The type hierarchy forms a tree rooted at Types.NO_TYPE. Parent-child relationships are based on Java class
	 * inheritance and semantic relationships.
	 * </p>
	 *
	 * <p>
	 * Example hierarchy:
	 * </p>
	 *
	 * <pre>
	 * NO_TYPE
	 *   ├─ int
	 *   ├─ float
	 *   ├─ container
	 *   │   ├─ list
	 *   │   ├─ map
	 *   │   └─ matrix
	 *   └─ geometry
	 *       └─ point
	 * </pre>
	 *
	 * @param p
	 *            the parent type
	 *
	 * @see #getParent()
	 */
	void setParent(IType<? super Support> p);

	/**
	 * Returns the parent type in the type hierarchy.
	 *
	 * <p>
	 * All types except NO_TYPE have a parent. This is used for inheritance of fields and type compatibility checks.
	 * </p>
	 *
	 * @return the parent type, or null for NO_TYPE
	 *
	 * @see #setParent(IType)
	 */
	IType<?> getParent();

	/**
	 * Coerces an expression's type to make it compatible with this type in a given context.
	 *
	 * <p>
	 * This method is used during compilation to determine if an expression needs type conversion. It may return a
	 * different type than this or the expression type, representing the most specific common type.
	 * </p>
	 *
	 * @param expr
	 *            the type of the expression to coerce
	 * @param context
	 *            the compilation context (statement, expression, etc.)
	 * @return the coerced type, or NO_TYPE if incompatible
	 */
	IType<?> coerce(IType<?> expr, IDescription context);

	/**
	 * Computes the distance between this type and another type in the type hierarchy.
	 *
	 * <p>
	 * The distance represents the number of steps in the type hierarchy between two types. This is used for:
	 * </p>
	 * <ul>
	 * <li>Operator overload resolution (prefer closer types)</li>
	 * <li>Type inference (choose most specific type)</li>
	 * <li>Compatibility checking</li>
	 * </ul>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.INT.distanceTo(Types.INT) returns 0</li>
	 * <li>Types.FLOAT.distanceTo(Types.INT) returns 1 (float is parent of int)</li>
	 * <li>Types.STRING.distanceTo(Types.INT) returns Integer.MAX_VALUE (no relationship)</li>
	 * </ul>
	 *
	 * @param originalChildType
	 *            the target type to compute distance to
	 * @return the distance (0 = same type, positive = steps in hierarchy, MAX_VALUE = unreachable)
	 *
	 * @see #computeDistanceTo(IType)
	 */
	int distanceTo(IType<?> originalChildType);

	/**
	 * Internal computation of distanceTo without cache lookup. Used by TypesManager to avoid infinite recursion.
	 *
	 * <p>
	 * Implementations should provide the actual distance computation logic without delegating to the cache. This method
	 * is called by {@link ITypesManager#computeDistance(IType, IType)} which manages caching.
	 * </p>
	 *
	 * @param originalChildType
	 *            the target type
	 * @return the distance
	 *
	 * @see ITypesManager#computeDistance(IType, IType)
	 */
	int computeDistanceTo(IType<?> originalChildType);

	/**
	 * Tries to find a common supertype shared between this type and the argument.
	 *
	 * <p>
	 * The common supertype is the most specific type that both types can be assigned to. This is used for type
	 * inference in expressions with mixed types.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.INT.findCommonSupertypeWith(Types.FLOAT) returns Types.FLOAT</li>
	 * <li>Types.POINT.findCommonSupertypeWith(Types.GEOMETRY) returns Types.GEOMETRY</li>
	 * <li>Types.STRING.findCommonSupertypeWith(Types.INT) returns Types.NO_TYPE</li>
	 * </ul>
	 *
	 * @param iType
	 *            the other type
	 * @return the common supertype, or Types.NO_TYPE if no common supertype exists
	 *
	 * @see #computeFindCommonSupertypeWith(IType)
	 */
	IType<? super Support> findCommonSupertypeWith(IType<?> iType);

	/**
	 * Internal computation of findCommonSupertypeWith without cache lookup. Used by TypesManager to avoid infinite
	 * recursion.
	 *
	 * <p>
	 * Implementations should provide the actual common supertype logic without delegating to the cache. This method is
	 * called by {@link ITypesManager#computeCommonSupertype(IType, IType)} which manages caching.
	 * </p>
	 *
	 * @param iType
	 *            the other type
	 * @return the common supertype
	 *
	 * @see ITypesManager#computeCommonSupertype(IType, IType)
	 */
	IType<? super Support> computeFindCommonSupertypeWith(IType<?> iType);

	/**
	 * Determines the actual type that results from casting an expression to this type.
	 *
	 * <p>
	 * Usually returns this type, but some types (like species types or agent types) compute a more specific type based
	 * on the expression being cast.
	 * </p>
	 *
	 * <p>
	 * For example, casting an agent to a species type might return the specific species type of that agent.
	 * </p>
	 *
	 * @param exp
	 *            the expression being cast
	 * @return the resulting type after casting, or this type by default
	 */
	IType<?> typeIfCasting(final IExpression exp);

	// ==================== Container Type Methods ====================

	/**
	 * Checks if this type represents a container (list, map, matrix, etc.).
	 *
	 * <p>
	 * Container types hold collections of values and support iteration, indexing, and other container operations.
	 * </p>
	 *
	 * @return true if this is a container type
	 *
	 * @see IContainerType
	 * @see #isCompoundType()
	 */
	boolean isContainer();

	/**
	 * Checks if this type has a fixed length (cannot use add/remove operations).
	 *
	 * <p>
	 * Types like matrices, strings, and points have fixed length. Lists and maps do not.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.LIST.isFixedLength() returns false</li>
	 * <li>Types.MATRIX.isFixedLength() returns true</li>
	 * <li>Types.STRING.isFixedLength() returns true</li>
	 * </ul>
	 *
	 * @return true if the type has fixed length and cannot be modified via add/remove
	 */
	boolean isFixedLength();

	/**
	 * Returns the type of elements contained in this container type.
	 *
	 * <p>
	 * For non-container types, this may return the type of components (e.g., float for point).
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>For {@code list<int>}, returns Types.INT</li>
	 * <li>For {@code map<string,float>}, returns Types.FLOAT</li>
	 * <li>For point, returns Types.FLOAT (the coordinate type)</li>
	 * </ul>
	 *
	 * @return the content type, or Types.NO_TYPE if not applicable
	 *
	 * @see #getKeyType()
	 */
	IType<?> getContentType();

	/**
	 * Returns the type of keys/indices for this container type.
	 *
	 * <p>
	 * For maps, this is the actual key type. For lists and matrices, this is the index type (int).
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>For {@code map<string,float>}, returns Types.STRING</li>
	 * <li>For {@code list<int>}, returns Types.INT (index type)</li>
	 * </ul>
	 *
	 * @return the key type, or Types.NO_TYPE if not applicable
	 *
	 * @see #getContentType()
	 */
	IType<?> getKeyType();

	/**
	 * Checks if this type represents a compound value which components can be extracted when casting to a container.
	 *
	 * <p>
	 * For instance, points have float components that can be extracted as a list. Containers are compound types by
	 * default.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>Types.POINT.isCompoundType() returns true (has x, y, z components)</li>
	 * <li>Types.LIST.isCompoundType() returns true (has elements)</li>
	 * <li>Types.INT.isCompoundType() returns false (atomic value)</li>
	 * </ul>
	 *
	 * @return true if the type represents a compound value
	 *
	 * @see #getContentType()
	 */
	boolean isCompoundType();

	/**
	 * Returns the number of type parameters this type can accept.
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>list: 1 parameter (content type)</li>
	 * <li>map: 2 parameters (key type, content type)</li>
	 * <li>int: 0 parameters (not parametric)</li>
	 * <li>file: depends on the wrapped buffer type</li>
	 * </ul>
	 *
	 * @return the number of type parameters (0 for non-parametric types)
	 *
	 * @see #isParametricType()
	 */
	int getNumberOfParameters();

	/**
	 * Returns the wrapped type for wrapper types (like file types wrapping specific formats).
	 *
	 * <p>
	 * For most types, returns Types.NO_TYPE. File types return their buffer type (csv, shapefile, etc.).
	 * </p>
	 *
	 * @return the wrapped type, or Types.NO_TYPE if not a wrapper type
	 */
	IType<?> getWrappedType();

	// ==================== Type Capabilities and Validation ====================

	/**
	 * Checks if an object can be considered an instance of this type in the given scope.
	 *
	 * <p>
	 * This is a runtime check that may involve scope-dependent logic (e.g., checking if an agent belongs to a specific
	 * species).
	 * </p>
	 *
	 * @param s
	 *            the execution scope
	 * @param c
	 *            the object to check
	 * @return true if the object can be considered an instance of this type
	 */
	boolean canBeTypeOf(IScope s, Object c);

	/**
	 * Checks if values of this type can be cast to constant expressions.
	 *
	 * <p>
	 * Most primitive types (int, float, bool, string) can be cast to const. Complex types (agents, files) typically
	 * cannot.
	 * </p>
	 *
	 * @return true if this type supports constant casting
	 */
	boolean canCastToConst();

	/**
	 * Checks if this type represents a numeric type (int or float).
	 *
	 * @return true if this is int or float type
	 */
	boolean isNumber();

	/**
	 * Checks if values of this type can be drawn/rendered in displays.
	 *
	 * <p>
	 * Drawable types include geometries, agents, images, etc.
	 * </p>
	 *
	 * @return true if this type can be drawn
	 */
	boolean isDrawable();

	/**
	 * Checks if values of this type are comparable (implement Comparable interface).
	 *
	 * <p>
	 * Comparable types can be used in comparisons (<, >, <=, >=) and sorting operations.
	 * </p>
	 *
	 * @return true if the support class implements Comparable
	 */
	default boolean isComparable() { return Comparable.class.isAssignableFrom(toClass()); }

	// ==================== String and Pattern Methods ====================

	/**
	 * Returns a string pattern representation of this type for use in regular expressions or pattern matching.
	 *
	 * <p>
	 * Used internally for type name parsing and validation.
	 * </p>
	 *
	 * @return the type pattern string
	 */
	String asPattern();

	// ==================== Plugin and Metadata Methods ====================

	/**
	 * Sets the name of the plugin that defines this type.
	 *
	 * <p>
	 * Used for documentation and error messages to indicate which GAMA extension provides this type.
	 * </p>
	 *
	 * @param plugin
	 *            the plugin name
	 */
	void setDefiningPlugin(String plugin);

	/**
	 * Documents the fields of this type for use in the GAMA documentation.
	 *
	 * <p>
	 * This method is called during documentation generation to include type-specific field information.
	 * </p>
	 *
	 * @param result
	 *            the documentation builder to populate
	 */
	default void documentFields(final IGamlDocumentation result) {}

	// ==================== Clipboard and Serialization Methods ====================

	/**
	 * Creates an instance of this type from clipboard text content.
	 *
	 * <p>
	 * The default implementation retrieves text from the clipboard and casts it to this type.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a value of this type parsed from the clipboard, or the default value if parsing fails
	 *
	 * @see #cast(IScope, Object, Object, boolean)
	 */
	default Support copyFromClipboard(final IScope scope) {
		return cast(scope, scope.getGui().copyTextFromClipboard(), null, false);
	}

	/**
	 * Deserializes a value of this type from a JSON map representation.
	 *
	 * <p>
	 * Used when loading GAMA state from JSON files or network messages.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing serialized data
	 * @return the deserialized value
	 *
	 * @see #serializeToJson(IJson)
	 */
	Support deserializeFromJson(IScope scope, IMap<String, Object> map2);

	/**
	 * Serializes this type metadata to JSON.
	 *
	 * <p>
	 * The default implementation creates a JSON object with the type name.
	 * </p>
	 *
	 * @param json
	 *            the JSON builder
	 * @return the JSON representation of this type
	 *
	 * @see #deserializeFromJson(IScope, IMap)
	 */
	@Override
	default IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(Types.TYPE, IKeyword.NAME, getName());
	}

	// ==================== Expression Methods ====================

	/**
	 * Returns the expression associated with this type (for types defined by expressions).
	 *
	 * <p>
	 * Some types are defined dynamically based on expressions (e.g., species types from species expressions).
	 * </p>
	 *
	 * @return the associated expression, or null if not applicable
	 *
	 * @see #setExpression(IExpression)
	 */
	IExpression getExpression();

	/**
	 * Sets the expression associated with this type.
	 *
	 * @param exp
	 *            the expression to associate
	 *
	 * @see #getExpression()
	 */
	void setExpression(IExpression exp);

	/**
	 * Returns the types manager that owns this type and provides cached type relation operations.
	 *
	 * <p>
	 * The types manager handles caching of expensive type relationship computations (assignability, distance, etc.).
	 * </p>
	 *
	 * @return the types manager, or null if not set
	 *
	 * @see ITypesManager
	 */
	ITypesManager getTypesManager();

	/**
	 * @return
	 */
	default boolean isObjectType() { return false; }

}