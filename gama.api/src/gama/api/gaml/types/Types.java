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
 * Written by drogoul Modified on 9 juin 2010
 *
 * @todo Description
 *
 */

/**
 * The Class Types.
 */

/**
 * The Class Types.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class Types {

	static {
		DEBUG.OFF();
	}

	/** The Constant builtInTypes. */
	public final static ITypesManager builtInTypes = new TypesManager(null);

	/** The built in species map. */
	private static volatile Map<String, ISpeciesDescription> builtInSpeciesMap;

	/** The Constant NO_TYPE. */
	public final static IType NO_TYPE = new GamaNoType();

	/** The type. */
	public static IType AGENT, PATH, FONT, SKILL, DATE, ACTION, TYPE;

	/** The int. */
	public static GamaIntegerType INT;

	/** The float. */
	public static GamaFloatType FLOAT;

	/** The color. */
	public static GamaColorType COLOR;

	/** The bool. */
	public static GamaBoolType BOOL;

	/** The string. */
	public static GamaStringType STRING;

	/** The point. */
	public static GamaPointType POINT;

	/** The geometry. */
	public static GamaGeometryType GEOMETRY;

	/** The topology. */
	public static GamaTopologyType TOPOLOGY;

	/** The field. */
	public static GamaFieldType FIELD;

	/** The species. */
	public static IContainerType LIST, MATRIX, MAP, GRAPH, FILE, PAIR, CONTAINER, SPECIES;

	/** The Constant CLASSES_TYPES_CORRESPONDANCE. */
	private static final Map<Class, String> CLASSES_TYPES_CORRESPONDANCE = new HashMap<>();

	/**
	 * Adds the class type correspondance.
	 *
	 * @param clazz
	 *            the clazz
	 * @param type
	 *            the type
	 */
	public static final void addClassTypeCorrespondance(final Class clazz, final String type) {
		if (clazz == null || type == null) return;
		CLASSES_TYPES_CORRESPONDANCE.put(clazz, type);
	}

	/**
	 * Cache.
	 *
	 * @param id
	 *            the id
	 * @param instance
	 *            the instance
	 */
	public static void cache(final int id, final IType instance) {
		switch (id) {
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
	 * Gets the.
	 *
	 * @param type
	 *            the type
	 * @return the i type
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
	 * Gets the.
	 *
	 * @param type
	 *            the type
	 * @return the i type
	 */
	public static IType get(final String type) {
		return builtInTypes.get(type);
	}

	/**
	 * Gets the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the i type
	 */
	public static <T> IType<T> get(final Class<T> type) {
		final IType<T> t = internalGet(type);
		return t == null ? Types.NO_TYPE : t;
	}

	/**
	 * Internal get.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the i type
	 */
	private static <T> IType<T> internalGet(final Class<T> type) {
		final IType<T>[] t = new IType[] { builtInTypes.get(Types.CLASSES_TYPES_CORRESPONDANCE.get(type)) };
		boolean newEntry = false;
		if (t[0] == Types.NO_TYPE && !type.isInterface()) {
			newEntry = true;
			for (Map.Entry<Class, String> entry : Types.CLASSES_TYPES_CORRESPONDANCE.entrySet()) {
				Class<?> support = entry.getKey();
				String id = entry.getValue();
				if (support != Object.class && support.isAssignableFrom(type)) {
					t[0] = (IType<T>) builtInTypes.get(id);
					newEntry = false;
					break;
				}
			}
		}
		if (newEntry) { addClassTypeCorrespondance(type, t[0].toString()); }
		return t[0];
	}

	/**
	 * Gets the type names.
	 *
	 * @return the type names
	 */
	public static Iterable<String> getTypeNames() {
		return Iterables.transform(builtInTypes.getAllTypes(), IType::getName);
	}

	/**
	 * Inits the types hierarchy of built-in types
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
	 * Gets the built in species.
	 *
	 * @return the built in species
	 */
	public static Map<String, ? extends ISpeciesDescription> getBuiltInSpecies() {
		if (builtInSpeciesMap != null) return builtInSpeciesMap;
		final IModelDescription root = IModelDescription.ROOT[0];
		List<ISpeciesDescription> result = new ArrayList();
		root.getAllSpecies(result);
		builtInSpeciesMap = StreamEx.of(result).toMap(ISpeciesDescription::getName, sd -> sd);
		return builtInSpeciesMap;
	}

	/**
	 * @param matchType
	 * @param switchType
	 * @return
	 */
	public static boolean intFloatCase(final IType t1, final IType t2) {
		return t1 == FLOAT && t2 == INT || t2 == FLOAT && t1 == INT;
	}

	/**
	 * Tests whether constant list expressions can still be compatible with a receiver even if their actual types differ
	 *
	 * @param receiverType
	 * @param assignedType
	 * @param expr2
	 * @return
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
	 * Gets the all fields.
	 *
	 * @return the all fields
	 */
	public static Iterable<IArtefactProto> getAllFields() {
		return concat(transform(builtInTypes.getAllTypes(), each -> each.getFieldGetters().values()));
	}

	/**
	 * Checks for type.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name
	 * @return true, if successful
	 * @date 7 janv. 2024
	 */
	public static boolean hasType(final String name) {
		return builtInTypes.containsType(name);
	}

}