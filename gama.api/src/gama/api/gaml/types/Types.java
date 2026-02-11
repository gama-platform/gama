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
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Iterables;

import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

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

	/** The manager responsible for storing and retrieving built-in types. */
	public final static ITypesManager builtInTypes = new TypesManager(null);

	/** A simplified cache of built-in species (as types) */
	private static volatile Map<String, ISpeciesDescription> builtInSpeciesMap;

	/** The constant representing the absence of a type (GamaNoType). */
	public final static IType NO_TYPE = new GamaNoType(builtInTypes);

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

	/** The singleton instance of the Field type. */
	public static GamaFieldType FIELD;

	/** Static references to common container types. */
	public static IContainerType LIST, MATRIX, MAP, GRAPH, FILE, PAIR, CONTAINER, SPECIES;

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
	 * Caches a type instance into the corresponding static field based on its ID. This method is called during type
	 * initialization.
	 *
	 * @param id
	 *            the type ID (see {@link IType})
	 * @param instance
	 *            the type instance
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
		}
		return builtInTypes.get(String.valueOf(type));
	}

	/**
	 * Retrieves a type by its name.
	 *
	 * @param type
	 *            the name of the type (e.g., "int", "list", "species_name")
	 * @return the IType instance, or {@link Types#NO_TYPE} if not found.
	 */
	public static IType get(final String type) {
		return builtInTypes.get(type);
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
		if (name != null) return (IType<T>) builtInTypes.get(name);
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
					result = (IType<T>) builtInTypes.get(id);
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
	public static Iterable<String> getTypeNames() {
		return Iterables.transform(builtInTypes.getAllTypes(), IType::getName);
	}

	/**
	 * Initializes the type hierarchy of built-in types. This computes the parent-child relationships between types
	 * based on their underlying Java classes and sets up the fields for each type.
	 */
	public static void init() {
		// We build a graph-type multimap structure
		Map<IType<?>, Set<IType<?>>> outgoing = new HashMap(), incoming = new HashMap();
		Set<IType<?>> types = builtInTypes.getAllTypes();
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
					ArtefactProtoRegistry.addNewTypeName(t.toString(), t.getVarKind());
					t.setFieldGetters(GAML.getAllFields(t.toClass()));
				}
			}
		}
	}

	/**
	 * Retrieves a map of all built-in species descriptions.
	 *
	 * @return a map where keys are species names and values are their descriptions.
	 */
	public static Map<String, ? extends ISpeciesDescription> getBuiltInSpecies() {
		if (builtInSpeciesMap != null) return builtInSpeciesMap;
		synchronized (Types.class) {
			if (builtInSpeciesMap != null) return builtInSpeciesMap;
			final IModelDescription root = IModelDescription.ROOT[0];
			List<ISpeciesDescription> result = new ArrayList<>();
			root.getAllSpecies(result);
			builtInSpeciesMap = StreamEx.of(result).toMap(ISpeciesDescription::getName, sd -> sd);
		}
		return builtInSpeciesMap;
	}

	/**
	 * Checks if two types represent a combination of int and float (in any order).
	 *
	 * @param t1
	 *            the first type
	 * @param t2
	 *            the second type
	 * @return true if one is INT and the other is FLOAT
	 */
	public static boolean intFloatCase(final IType t1, final IType t2) {
		return t1 == FLOAT && t2 == INT || t2 == FLOAT && t1 == INT;
	}

	/**
	 * Tests whether an expression is compatible with a container receiver (list or map) when the expression is empty,
	 * handling nested empty lists recursively.
	 *
	 * @param receiverType
	 *            the type of the variable receiving the value
	 * @param expr2
	 *            the expression being assigned
	 * @return true if the expression represents an empty container compatible with the receiver
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
	 * Retrieves all fields (attributes/actions) defined across all known types.
	 *
	 * @return an iterable of all IArtefactProto instances representing fields.
	 */
	public static Iterable<IArtefactProto> getAllFields() {
		return concat(transform(builtInTypes.getAllTypes(), each -> each.getFieldGetters().values()));
	}

	/**
	 * Checks if a type with the given name exists.
	 *
	 * @param name
	 *            the name of the type
	 * @return true if the type exists, false otherwise
	 */
	public static boolean hasType(final String name) {
		return builtInTypes.containsType(name);
	}

	/**
	 * @param types
	 * @param typesManager
	 * @param typesManager2
	 * @param typesManager3
	 * @return
	 */
	public static ITypesManager findMoreSpecificTypesManagerAmong(final ITypesManager... typesManagers) {
		for (ITypesManager tm : typesManagers) { if (tm != builtInTypes) return tm; }
		return builtInTypes;
	}

}
