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

import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.kernel.agent.IAgent;
import gama.dev.DEBUG;

/**
 * Model-scoped registry that hosts GAML types (built-in and species) and parses/aliases type names. Can delegate
 * lookups to a parent manager while supporting local additions, aliases, and parametric decoding.
 */

/**
 * The Class TypesManager.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class TypesManager implements ITypesManager {

	static {
		DEBUG.OFF();
	}

	/** The current index. */
	public static int CURRENT_INDEX = IType.BEGINNING_OF_SPECIES_TYPES;

	/** The parent. */
	private TypesManager parent;

	/** The types. */
	private final ConcurrentHashMap<String, IType<?>> types = new ConcurrentHashMap<>();

	/** The unique types. */
	private final Set<IType<?>> uniqueTypes = ConcurrentHashMap.newKeySet();

	/** Cache for isAssignableFrom operations - stores Boolean results */
	private final Cache<TypePair, Boolean> assignabilityCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/** Cache for findCommonSupertypeWith operations - stores IType results */
	private final Cache<TypePair, IType<?>> commonSupertypeCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/** Cache for distanceTo operations - stores Integer results */
	private final Cache<TypePair, Integer> distanceCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/** Cache for isTranslatableInto operations - stores Boolean results */
	private final Cache<TypePair, Boolean> translatabilityCache = newBuilder().expireAfterAccess(5, MINUTES).build();

	/**
	 * Instantiates a new types manager.
	 *
	 * @param types2
	 *            the types 2
	 */
	public TypesManager(final ITypesManager types2) {
		setParent(types2);
	}

	@Override
	public Set<IType<?>> getAllTypes() { return new HashSet(uniqueTypes); }

	@Override
	public void setParent(final ITypesManager parent) { this.parent = (TypesManager) parent; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.types.ITypesManager#alias(java.lang.String, java.lang.String)
	 */
	/**
	 * Registers an alias for an existing type name.
	 *
	 * @param existing
	 *            canonical type name
	 * @param alias
	 *            alternative keyword
	 */
	@Override
	public void alias(final String existingTypeName, final String otherTypeName) {
		final IType t = types.get(existingTypeName);
		if (t != null) { types.put(otherTypeName, t); }
	}

	/**
	 * Adds the species type.
	 *
	 * @param species
	 *            the species
	 * @return the i type<? extends I agent>
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.types.ITypesManager#addSpeciesType(gama.gaml.descriptions. TypeDescription)
	 */

	/**
	 * Registers a type into this manager, initializing its metadata and capturing its plugin origin.
	 *
	 * @param <Support>
	 *            Java support class of the type
	 * @param name
	 *            keyword used in GAML
	 * @param originalType
	 *            type instance to initialize/store
	 * @param id
	 *            numeric id (see {@link IType})
	 * @param varKind
	 *            variable kind (field/attribute/etc.)
	 * @param support
	 *            Java class backing the type
	 * @param plugin
	 *            plugin identifier defining the type
	 * @return initialized type (or NO_TYPE for "unknown")
	 */
	@Override
	public <Support> IType<Support> addRegularType(final String name, final IType<Support> originalType,
			final String plugin) {
		IType<Support> type = originalType;
		if (IKeyword.UNKNOWN.equals(name)) { type = Types.NO_TYPE; }
		type.setDefiningPlugin(plugin);
		ArtefactProtoRegistry.addNewTypeName(name, type.getVarKind());
		addType(type);
		return type;
	}

	/**
	 * Registers a species description as a type and ensures unique naming.
	 *
	 * @param species
	 *            species description to register
	 * @return resulting species type
	 */
	@Override
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
	 * Adds the type.
	 *
	 * @param t
	 *            the t
	 * @param support
	 *            the support
	 * @return the i type
	 */
	private void addType(final IType t) {
		final String name = t.toString();
		types.put(name, t);
		// Hack to allow types to be declared with their id as string
		types.put(String.valueOf(t.id()), t);
		uniqueTypes.add(t);
	}

	/**
	 * Initializes built-in species types from a model, sets their parents, and fills the local registry.
	 *
	 * @param model
	 *            root model description containing species
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.types.ITypesManager#init()
	 */
	@Override
	public void init(final IModelDescription model) {
		// We first add the species as types
		model.visitAllSpecies(entry -> {
			addSpeciesType(entry);
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
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.types.ITypesManager#containsType(java.lang.String)
	 */
	/**
	 * @param name
	 *            type keyword
	 * @return true when this manager (or parent) knows a type with that name
	 */
	@Override
	public boolean containsType(final String s) {
		final IType t = types.get(s);
		if (t != null) return true;
		if (parent == null) return false;
		return parent.containsType(s);
	}

	@Override
	public IType get(final String type) {
		return get(type, Types.NO_TYPE);
	}

	/**
	 * Resolves a type by name, optionally falling back to a default when not found.
	 *
	 * @param name
	 *            keyword or id string
	 * @param defaultValue
	 *            fallback if absent
	 * @return resolved type or {@code defaultValue}
	 */
	@Override
	public IType get(final String type, final IType defaultValue) {
		if (type == null) return defaultValue;
		final IType t = types.get(type);
		if (t != null) return t;
		if (parent == null) return defaultValue;
		return parent.get(type, defaultValue);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.types.ITypesManager#dispose()
	 */
	/**
	 * Clears all locally registered types and aliases, and invalidates all caches.
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
	 * Parses a type expression possibly containing parametric parts (e.g., "list<float>" or "map<string,int>").
	 *
	 * @param s
	 *            textual type expression
	 * @return resolved type or {@link Types#NO_TYPE} on failure
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
		final TypePair key = new TypePair(from, to);
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
		final TypePair key = new TypePair(type1, type2);
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
		final TypePair key = new TypePair(from, to);
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
		final TypePair key = new TypePair(from, to);
		try {
			return translatabilityCache.get(key, () -> from.computeIsTranslatableInto(to));
		} catch (ExecutionException e) {
			return false;
		}
	}

}
