/*******************************************************************************************************
 *
 * Types.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Iterables;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.object.GamaGenericObjectType;
import gama.dev.DEBUG;

/**
 * The Class Types.
 *
 * This utility class provides static access to the built-in types of the GAML language, as well as methods to retrieve
 * types by their name, ID, or underlying Java class. It also manages the type hierarchy and the correspondence between
 * Java classes and GAML types.
 *
 * @author drogoul
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Types {

	static {
		DEBUG.OFF();
	}

	/**
	 * Special keyword for species variable declarations (from SyntacticFactory).
	 */
	public static final String SPECIES_VAR = "species_var";

	/** The manager responsible for storing and retrieving built-in types. */
	private final static ITypesManager BUILT_IN_TYPES = new TypesManager(null);

	/** The constant representing the absence of a type (GamaNoType). */
	public final static IType NO_TYPE = new GamaNoType(BUILT_IN_TYPES);

	/** Static references to common built-in types for fast access. */
	public static IType AGENT, PATH, FONT, SKILL, DATE, ACTION, TYPE;

	/** The singleton instance of the Integer type. */
	public static GamaIntegerType INT;

	/** The singleton instance of the Float type. */
	public static GamaFloatType FLOAT;

	/** The singleton instance of the Color type. */
	public static GamaColorType COLOR;

	/** The singleton instance of the Boolean type. */
	public static GamaBoolType BOOL;

	/** The singleton instance of the String type. */
	public static GamaStringType STRING;

	/** The singleton instance of the Point type. */
	public static GamaPointType POINT;

	/** The singleton instance of the Geometry type. */
	public static GamaGeometryType GEOMETRY;

	/** The singleton instance of the Topology type. */
	public static GamaTopologyType TOPOLOGY;

	/** The object. */
	public static GamaGenericObjectType OBJECT;

	/** The singleton instance of the Field type. */
	public static GamaFieldType FIELD;

	/** Static references to common container types. */
	public static IContainerType LIST, MATRIX, MAP, GRAPH, FILE, PAIR, CONTAINER, SPECIES, DATAFRAME;

	/** A thread-safe cache mapping Java classes to their corresponding GAML type names. */
	private static final Map<Class, String> CLASSES_TYPES_CORRESPONDANCE = new ConcurrentHashMap<>();

	/**
	 * Associates a Java class with a GAML type name in the cache.
	 *
	 * @param clazz
	 *            the Java class
	 * @param type
	 *            the GAML type name
	 */
	public static final void addClassTypeCorrespondance(final Class clazz, final String type) {
		if (clazz == null || type == null) return;
		CLASSES_TYPES_CORRESPONDANCE.put(clazz, type);
	}

	/**
	 * Caches a type instance into the corresponding static field based on its type ID.
	 *
	 * <p>
	 * This method is called during type initialization (by {@link TypesManager#addRegularType}) to populate the static
	 * type references for fast access. Instead of looking up types by name every time, code can use the static
	 * references directly (e.g., {@code Types.INT}, {@code Types.STRING}).
	 * </p>
	 *
	 * <p>
	 * The switch statement maps each built-in type ID to its corresponding static field. Only built-in types with IDs
	 * less than {@link IType#BEGINNING_OF_CUSTOM_TYPES} are cached this way.
	 * </p>
	 *
	 * <p>
	 * Example usage during GAMA initialization:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Type system initialization creates types and caches them
	 * GamaIntegerType intType = new GamaIntegerType();
	 * Types.cache(intType);
	 * // Now Types.INT is available
	 * }
	 * </pre>
	 *
	 * @param instance
	 *            the type instance to cache (must be a built-in type)
	 */
	public static void cache(final IType instance) {
		switch (instance.id()) {
			case IType.INT:
				INT = (GamaIntegerType) instance;
				break;
			case IType.FLOAT:
				FLOAT = (GamaFloatType) instance;
				break;
			case IType.BOOL:
				BOOL = (GamaBoolType) instance;
				break;
			case IType.COLOR:
				COLOR = (GamaColorType) instance;
				break;
			case IType.DATE:
				DATE = instance;
				break;
			case IType.STRING:
				STRING = (GamaStringType) instance;
				break;
			case IType.POINT:
				POINT = (GamaPointType) instance;
				break;
			case IType.GEOMETRY:
				GEOMETRY = (GamaGeometryType) instance;
				break;
			case IType.TOPOLOGY:
				TOPOLOGY = (GamaTopologyType) instance;
				break;
			case IType.LIST:
				LIST = (IContainerType) instance;
				break;
			case IType.MAP:
				MAP = (GamaMapType) instance;
				break;
			case IType.GRAPH:
				GRAPH = (IContainerType) instance;
				break;
			case IType.FILE:
				FILE = (IContainerType) instance;
				break;
			case IType.PAIR:
				PAIR = (IContainerType) instance;
				break;
			case IType.AGENT:
				AGENT = instance;
				break;
			case IType.PATH:
				PATH = instance;
				break;
			case IType.MATRIX:
				MATRIX = (GamaMatrixType) instance;
				break;
			case IType.CONTAINER:
				CONTAINER = (IContainerType) instance;
				break;
			case IType.SPECIES:
				SPECIES = (IContainerType) instance;
				break;
			case IType.FONT:
				FONT = instance;
				break;
			case IType.SKILL:
				SKILL = instance;
				break;
			case IType.TYPE:
				TYPE = instance;
				break;
			case IType.ACTION:
				ACTION = instance;
				break;
			case IType.FIELD:
				FIELD = (GamaFieldType) instance;
				break;
			case IType.OBJECT:
				OBJECT = (GamaGenericObjectType) instance;
				break;
			case IType.DATAFRAME:
				DATAFRAME = (IContainerType) instance;
				break;
			default:
		}
	}

	/**
	 * Retrieves a built-in type by its integer ID.
	 *
	 * @param type
	 *            the type ID (see constants in {@link IType})
	 * @return the IType instance, or a type looked up by string ID if not found in the fast switch.
	 */
	public static IType get(final int type) {
		// use cache first
		switch (type) {
			case IType.INT:
				return INT;
			case IType.FLOAT:
				return FLOAT;
			case IType.BOOL:
				return BOOL;
			case IType.COLOR:
				return COLOR;
			case IType.DATE:
				return DATE;
			case IType.STRING:
				return STRING;
			case IType.POINT:
				return POINT;
			case IType.GEOMETRY:
				return GEOMETRY;
			case IType.TOPOLOGY:
				return TOPOLOGY;
			case IType.LIST:
				return LIST;
			case IType.MAP:
				return MAP;
			case IType.GRAPH:
				return GRAPH;
			case IType.FILE:
				return FILE;
			case IType.PAIR:
				return PAIR;
			case IType.AGENT:
				return AGENT;
			case IType.PATH:
				return PATH;
			case IType.MATRIX:
				return MATRIX;
			case IType.CONTAINER:
				return CONTAINER;
			case IType.SPECIES:
				return SPECIES;
			case IType.SKILL:
				return SKILL;
			case IType.ACTION:
				return ACTION;
			case IType.TYPE:
				return TYPE;
			case IType.OBJECT:
				return OBJECT;
			case IType.DATAFRAME:
				return DATAFRAME;
		}
		return BUILT_IN_TYPES.get(String.valueOf(type));
	}

	/**
	 * Retrieves a type by its name.
	 *
	 * @param type
	 *            the name of the type (e.g., "int", "list", "species_name")
	 * @return the IType instance, or {@link Types#NO_TYPE} if not found.
	 */
	public static IType get(final String type) {
		return BUILT_IN_TYPES.get(type);
	}

	/**
	 * Retrieves the GAML type corresponding to a given Java class. This method uses a cache to speed up lookups and
	 * searches the hierarchy of the class if no direct mapping is found.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the Java class
	 * @return the GAML type, or {@link Types#NO_TYPE} if no correspondence is found.
	 */
	public static <T> IType<T> get(final Class<T> type) {
		// Optimization: direct lookup first
		String name = CLASSES_TYPES_CORRESPONDANCE.get(type);
		if (name != null) return (IType<T>) BUILT_IN_TYPES.get(name);
		final IType<T> t = internalGet(type);
		return t == null ? Types.NO_TYPE : t;
	}

	/**
	 * Internal method to find the GAML type for a Java class by traversing its hierarchy. The result is always cached.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the Java class to look up
	 * @return the found IType, or NO_TYPE
	 */
	private static <T> IType<T> internalGet(final Class<T> type) {
		IType<T> result = Types.NO_TYPE;
		// We iterate over the values to find a superclass
		if (!type.isInterface()) {
			for (Map.Entry<Class, String> entry : Types.CLASSES_TYPES_CORRESPONDANCE.entrySet()) {
				Class<?> support = entry.getKey();
				String id = entry.getValue();
				// We exclude Object.class to avoid matching everything to NO_TYPE if cached
				if (support != Object.class && support.isAssignableFrom(type)) {
					result = (IType<T>) BUILT_IN_TYPES.get(id);
					break;
				}
			}
		}
		// We always cache the result, even if it is NO_TYPE (to avoid recomputing it)
		addClassTypeCorrespondance(type, result.toString());
		return result;
	}

	/**
	 * Returns an iterable of all known type names.
	 *
	 * @return the type names
	 */
	public static Iterable<String> getTypeNames() { return Iterables.transform(getAllTypes(), IType::getName); }

	/**
	 * Initializes the type hierarchy of built-in types by computing parent-child relationships.
	 *
	 * <p>
	 * This method builds the type inheritance tree using a graph-based algorithm:
	 * </p>
	 * <ol>
	 * <li>Creates a directed graph where edges connect parent types to child types (based on Java class hierarchy)</li>
	 * <li>Uses topological traversal starting from NO_TYPE (the root) to establish parent relationships</li>
	 * <li>Sets the parent for each type using {@link IType#setParent}</li>
	 * <li>Registers type names in the artefact proto registry</li>
	 * <li>Initializes field getters for each type using reflection on their Java support classes</li>
	 * </ol>
	 *
	 * <p>
	 * The algorithm ensures that each type is parented to its most specific supertype in the GAMA type system, not
	 * necessarily its direct Java parent class.
	 * </p>
	 *
	 * <p>
	 * Example hierarchy established:
	 * </p>
	 *
	 * <pre>
	 * NO_TYPE (root)
	 *   ├─ int
	 *   ├─ float
	 *   ├─ bool
	 *   ├─ string
	 *   ├─ container
	 *   │   ├─ list
	 *   │   ├─ map
	 *   │   ├─ matrix
	 *   │   └─ graph
	 *   ├─ geometry
	 *   │   └─ point
	 *   └─ agent
	 * </pre>
	 *
	 * <p>
	 * This method should be called once during GAMA platform initialization, after all built-in types have been
	 * registered.
	 * </p>
	 */
	public static void init() {
		// We build a graph-type multimap structure
		Map<IType<?>, Set<IType<?>>> outgoing = new HashMap(), incoming = new HashMap();
		Set<IType<?>> types = BUILT_IN_TYPES.getAllTypes();
		for (IType t : types) {
			outgoing.put(t, new HashSet<>());
			incoming.put(t, new HashSet<>());
		}
		for (IType t1 : types) {
			for (IType t2 : types) {
				if (t1 != t2 && t1.toClass().isAssignableFrom(t2.toClass())) {
					outgoing.get(t1).add(t2);
					incoming.get(t2).add(t1);
				}
			}
		}

		// We traverse the hierarchy beginning with NO_TYPE and browsing through its children to determine which ones
		// are only its children and which ones are sub-subtypes. The only children of a type are parented and
		// processed, the others left for further iterations
		Deque<IType<?>> toProcess = new ArrayDeque<>();
		toProcess.push(NO_TYPE);
		while (!toProcess.isEmpty()) {
			IType parent = toProcess.pop();
			for (IType t : outgoing.get(parent)) {
				incoming.get(t).remove(parent);
				if (incoming.get(t).isEmpty()) {
					toProcess.push(t);
					// DEBUG.OUT("Parenting " + t.getName() + " with " + parent.getName());
					t.setParent(parent);
					t.setFieldGetters(GAML.getAllFields(t.toClass()));
				}
			}
		}
	}

	/**
	 * Checks if two types represent a combination of int and float (in any order).
	 *
	 * <p>
	 * This is a special case in type checking because int and float are both numeric types but have different
	 * representations. Many operations need to detect when one operand is int and the other is float to perform proper
	 * numeric promotion.
	 * </p>
	 *
	 * <p>
	 * Used primarily in:
	 * </p>
	 * <ul>
	 * <li>Operator overload resolution</li>
	 * <li>Type coercion for arithmetic operations</li>
	 * <li>Common supertype computation (result is float)</li>
	 * </ul>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * Types.intFloatCase(Types.INT, Types.FLOAT)   // true
	 * Types.intFloatCase(Types.FLOAT, Types.INT)   // true (order doesn't matter)
	 * Types.intFloatCase(Types.INT, Types.INT)     // false
	 * Types.intFloatCase(Types.INT, Types.STRING)  // false
	 * }
	 * </pre>
	 *
	 * @param t1
	 *            the first type
	 * @param t2
	 *            the second type
	 * @return true if one type is INT and the other is FLOAT (in any order)
	 */
	public static boolean intFloatCase(final IType t1, final IType t2) {
		return t1 == FLOAT && t2 == INT || t2 == FLOAT && t1 == INT;
	}

	/**
	 * Tests whether an expression is compatible with a container receiver when the expression is empty.
	 *
	 * <p>
	 * This is a special case in GAML's type system that handles empty literal containers. An empty list {@code []} or
	 * empty map {@code map([])} can be assigned to any list or map variable regardless of its parametric content type.
	 * </p>
	 *
	 * <p>
	 * The method handles nested empty containers recursively. For example, an empty list of lists {@code [[], []]}
	 * should be assignable to {@code list<list<int>>}.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // In GAML:
	 * list<int> numbers <- [];  // valid (empty list case)
	 * map<string,float> data <- map([]);  // valid (empty map case)
	 * list<list<int>> matrix <- [[], []];  // valid (nested empty list case)
	 *
	 * // Detection:
	 * isEmptyContainerCase(Types.LIST.of(Types.INT), emptyListExpr)  // true
	 * isEmptyContainerCase(Types.INT, emptyListExpr)  // false
	 * }
	 * </pre>
	 *
	 * @param receiverType
	 *            the type of the variable receiving the value (e.g., list<int>)
	 * @param expr2
	 *            the expression being assigned (must be a container expression)
	 * @return true if the expression is an empty container compatible with the receiver type
	 */
	public static boolean isEmptyContainerCase(final IType receiverType, final IExpression expr2) {
		final IType receiver = receiverType.getGamlType();
		final boolean result = (receiver == MAP || receiver == LIST) && expr2.isEmpty();
		if (result) return true;
		// One last chance if receiverType is a list of lists/maps and expr2 is a list expression containing empty
		// lists. This case is treated recursively in case of complex data structures
		if (expr2 instanceof IExpression.List) {
			for (final IExpression subExpr : ((IExpression.List) expr2).getElements()) {
				if (!isEmptyContainerCase(receiverType.getContentType(), subExpr)) return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Retrieves all field prototypes (attributes, pseudo-attributes, and operators) defined across all known types.
	 *
	 * <p>
	 * This method aggregates all field getters from every type registered in the built-in types manager. Fields
	 * include:
	 * </p>
	 * <ul>
	 * <li>Attributes (e.g., {@code myPoint.x}, {@code myAgent.location})</li>
	 * <li>Pseudo-attributes (e.g., {@code myList.length}, {@code myGeometry.area})</li>
	 * <li>Unary operators accessed via dot notation</li>
	 * </ul>
	 *
	 * <p>
	 * Used primarily for:
	 * </p>
	 * <ul>
	 * <li>Documentation generation (listing all available fields)</li>
	 * <li>IDE auto-completion</li>
	 * <li>Validation of field access expressions</li>
	 * </ul>
	 *
	 * <p>
	 * Example usage:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Get all fields for documentation
	 * for (IArtefact field : Types.getAllFields()) {
	 * 	System.out.println(field.getName() + " on " + field.getDefiningType());
	 * }
	 * }
	 * </pre>
	 *
	 * @return an iterable of all field operator prototypes from all types
	 *
	 * @see IType#getFieldGetters()
	 */
	public static Iterable<IArtefact> getAllFields() {
		return concat(transform(BUILT_IN_TYPES.getAllTypes(), each -> each.getFieldGetters().values()));
	}

	/**
	 * Checks if a type with the given name exists in the built-in types registry.
	 *
	 * <p>
	 * This is a convenience method that delegates to {@link ITypesManager#containsType(String)} on the built-in types
	 * manager. It only checks built-in types, not model-specific species types.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * Types.hasType("int")        // true
	 * Types.hasType("list")       // true
	 * Types.hasType("mySpecies")  // false (unless it's a built-in species)
	 * Types.hasType("unknown")    // false
	 * }
	 * </pre>
	 *
	 * @param name
	 *            the type name to check
	 * @return true if the type exists in the built-in types registry
	 *
	 * @see ITypesManager#containsType(String)
	 */
	public static boolean hasType(final String name) {
		return BUILT_IN_TYPES.containsType(name);
	}

	/**
	 * Finds the most specific (non-built-in) types manager among the provided managers.
	 *
	 * <p>
	 * This utility method selects the first types manager that is not the built-in types manager. It's used to
	 * determine which manager should be used for type operations when multiple managers are in scope.
	 * </p>
	 *
	 * <p>
	 * The rationale is that model-specific or experiment-specific types managers are more specific than the built-in
	 * types manager and should take precedence.
	 * </p>
	 *
	 * <p>
	 * Example usage in compilation context:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * // Get the most specific manager from model, experiment, and built-in
	 * ITypesManager manager =
	 * 		Types.findMoreSpecificTypesManagerAmong(experimentTypes, modelTypes, Types.BUILT_IN_TYPES);
	 * // Returns experimentTypes if not null and not BUILT_IN_TYPES,
	 * // otherwise modelTypes if not null and not BUILT_IN_TYPES,
	 * // otherwise BUILT_IN_TYPES
	 * }
	 * </pre>
	 *
	 * @param typesManagers
	 *            variable number of types managers to choose from
	 * @return the first non-built-in manager, or BUILT_IN_TYPES if all are built-in
	 */
	public static ITypesManager findMoreSpecificTypesManagerAmong(final ITypesManager... typesManagers) {
		for (ITypesManager tm : typesManagers) { if (tm != BUILT_IN_TYPES) return tm; }
		return BUILT_IN_TYPES;
	}

	/**
	 * Find types manager.
	 *
	 * @param context
	 *            the context
	 * @return the i types manager
	 */
	public static ITypesManager findTypesManager(final IDescription context) {
		if (context == null) return BUILT_IN_TYPES;
		final IModelDescription md = context.getModelDescription();
		if (md == null) return BUILT_IN_TYPES;
		final ITypesManager tm = md.getTypesManager();
		return tm != null ? tm : BUILT_IN_TYPES;
	}

	/**
	 * Creates the types manager parented by.
	 *
	 * @param context
	 *            the context
	 * @return the i types manager
	 */
	public static ITypesManager createTypesManagerParentedBy(final IDescription context) {
		if (context == null) return BUILT_IN_TYPES;
		final IModelDescription md = context.getModelDescription();
		if (md == null) return BUILT_IN_TYPES;
		final ITypesManager tm = md.getTypesManager();
		return tm != null ? new TypesManager(tm) : BUILT_IN_TYPES;
	}

	/**
	 * @param executionScope
	 * @return
	 */
	public static ITypesManager findTypesManager(final IScope scope) {
		if (scope == null) return BUILT_IN_TYPES;
		IModelSpecies ms = scope.getModel();
		if (ms != null) return findTypesManager(ms.getDescription());
		return BUILT_IN_TYPES;
	}

	/**
	 * @return
	 */
	public static Set<IType<?>> getAllTypes() { return BUILT_IN_TYPES.getAllTypes(); }

	/**
	 * Gets the built in type manager.
	 *
	 * @return the built in type manager
	 */
	public static ITypesManager getBuiltInTypeManager() { return BUILT_IN_TYPES; }

	/**
	 * @param name
	 * @param t
	 * @param plugin
	 */
	public static IType<?> addRegularType(final String name, final IType<?> t, final String plugin) {
		if (IKeyword.SPECIES.equals(name)) { BUILT_IN_TYPES.addRegularType(SPECIES_VAR, t, plugin); }
		return BUILT_IN_TYPES.addRegularType(name, t, plugin);
	}

	/**
	 * Contains type.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public static boolean containsType(final String name) {
		return BUILT_IN_TYPES.containsType(name);
	}

}
