/*******************************************************************************************************
 *
 * ArtefactProtoRegistry.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.registries;

import static gama.api.constants.IKeyword.AGENT;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;

/**
 * Central registry for GAML language artefact prototypes (statements, variables, and facets).
 *
 * <p>
 * This registry maintains the metadata and prototypes for all GAML language constructs, including statements (like
 * 'create', 'ask', 'loop'), variable declarations (like 'int', 'float', 'species'), and their facets (like 'name:',
 * 'type:', 'from:'). It serves as the primary reference for the GAML compiler and parser.
 * </p>
 *
 * <h2>Artefact Types</h2>
 * <p>
 * The registry manages three main categories of artefacts:
 * </p>
 * <ul>
 * <li><b>Statement Prototypes</b> - Definitions of GAML statements (commands, control structures)</li>
 * <li><b>Variable Prototypes</b> - Definitions of variable declaration keywords and types</li>
 * <li><b>Facet Prototypes</b> - Definitions of statement and variable facets (named parameters)</li>
 * </ul>
 *
 * <h2>Prototype Organization</h2>
 * <p>
 * Prototypes are organized by:
 * </p>
 * <ul>
 * <li><b>Keyword</b> - The GAML keyword that triggers the artefact (stored in keyword maps)</li>
 * <li><b>Kind</b> - The semantic category of the artefact (stored in kind map)</li>
 * <li><b>Variable Type</b> - For variables, organized by type ID (stored in type-to-keyword multimap)</li>
 * </ul>
 *
 * <h2>Special Keyword Sets</h2>
 * <p>
 * The registry maintains several sets of keywords with special semantic meaning:
 * </p>
 * <ul>
 * <li>{@link #BREAKABLE_STATEMENTS} - Statements that can contain 'break'</li>
 * <li>{@link #CONTINUABLE_STATEMENTS} - Statements that can contain 'continue'</li>
 * <li>{@link #BINARY_PROTO_NAMES} - Binary operator keywords</li>
 * <li>{@link #PROTOS_WITHOUT_PARENTHESES} - Operators that don't require parentheses</li>
 * <li>{@link #NON_SERIALIZABLE_FACETS} - Facets excluded from serialization</li>
 * </ul>
 *
 * <h2>Dynamic Type Registration</h2>
 * <p>
 * As species are defined in GAML models, they are dynamically registered as valid type keywords using
 * {@link #addSpeciesNameAsType(String)}, allowing them to be used in variable declarations.
 * </p>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Retrieve a statement prototype
 * IArtefactProto.Symbol createProto = ArtefactProtoRegistry.getStatementProto("create");
 *
 * // Check if a keyword is a statement
 * boolean isStatement = ArtefactProtoRegistry.isStatementProto("loop");
 *
 * // Get the omissible facet for a statement
 * String omissible = ArtefactProtoRegistry.getOmissibleFacetForSymbol("create");
 *
 * // Register a species as a type
 * ArtefactProtoRegistry.addSpeciesNameAsType("my_species");
 * }</pre>
 *
 * @author drogoul
 * @since GAMA 1.0
 *
 * @see IArtefactProto
 * @see gama.api.compilation.descriptions.IDescription
 */
public class ArtefactProtoRegistry {

	/** The Constant DO_FACETS. */
	public static Set<String> DO_FACETS;

	/** The Constant BREAKABLE_STATEMENTS. */
	public static final Set<String> BREAKABLE_STATEMENTS = new HashSet<>();

	/** The Constant CONTINUABLE_STATEMENTS. */
	public static final Set<String> CONTINUABLE_STATEMENTS = new HashSet<>();

	/** Facet types that represent identifiers or labels. */
	public static final List<Integer> ID_FACETS =
			Arrays.asList(IType.LABEL, IType.ID, IType.NEW_TEMP_ID, IType.NEW_VAR_ID);

	/** Facets that should not be included in serialization. */
	public static final Set<String> NON_SERIALIZABLE_FACETS =
			new HashSet<>(Arrays.asList(IKeyword.INTERNAL_FUNCTION, IKeyword.WITH));

	/**
	 * Special keyword for species variable declarations (from SyntacticFactory).
	 */
	public static final String SPECIES_VAR = "species_var";

	/** Map of statement keywords to their prototype definitions. */
	public static final Map<String, IArtefactProto.Symbol> STATEMENT_KEYWORDS_PROTOS = new HashMap<>();

	/** Map of variable declaration keywords to their prototype definitions. */
	public static final Map<String, IArtefactProto.Symbol> VAR_KEYWORDS_PROTOS = new HashMap<>();

	/** Multimap from variable kind (type ID) to the keywords that declare that kind. */
	public final static SetMultimap<Integer, String> VARKIND2KEYWORDS =
			Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

	/** Map of artefact kinds to their prototype definitions. */
	public static final Map<Integer, IArtefactProto.Symbol> KINDS_PROTOS = new HashMap<>();

	/** Cache for statement protos. */
	private static volatile Iterable<IArtefactProto.Symbol> cachedStatementProtos = null;

	/** Cache for facets protos. */
	private static volatile Iterable<? extends IArtefactProto.Facet> cachedFacetsProtos = null;

	/**
	 * Registers a new type name as a valid variable declaration keyword.
	 *
	 * <p>
	 * This method is called when new types are defined (e.g., through species declarations) to make them available as
	 * variable declaration keywords in GAML. For example, after declaring a species "my_agent", this allows writing
	 * "my_agent x;".
	 * </p>
	 *
	 * @param s
	 *            the type name to register
	 * @param kind
	 *            the kind ID for this type (from {@link IType})
	 */
	public static void addNewTypeName(final String s, final int kind) {
		addNewVarKeyword(s, kind);
		if (VAR_KEYWORDS_PROTOS.containsKey(s)) return;
		final IArtefactProto.Symbol p = KINDS_PROTOS.get(kind);
		if (p != null) {
			if ("species".equals(s)) {
				VAR_KEYWORDS_PROTOS.put(SPECIES_VAR, p);
			} else {
				VAR_KEYWORDS_PROTOS.put(s, p);
			}
		}
	}

	/**
	 * Gets the do facets.
	 *
	 * @return the do facets
	 */
	public static Set<String> getDoFacets() {
		if (DO_FACETS == null) { DO_FACETS = getAllowedFacetsFor(IKeyword.DO); }
		return DO_FACETS;
	}

	/** Operators that can be used without parentheses in GAML expressions. */
	public static final Set<String> PROTOS_WITHOUT_PARENTHESES = ImmutableSet.of("-", "!");

	/** Binary operator keywords in GAML. */
	public static final Set<String> BINARY_PROTO_NAMES = ImmutableSet.of(IKeyword.EQUALS, IKeyword.PLUS, IKeyword.MINUS,
			IKeyword.DIVIDE, IKeyword.TIMES, "^", "<", ">", "<=", ">=", "?", "!=", ":", ".", "where", "select",
			"collect", "first_with", "last_with", "overlapping", "at_distance", "in", "inside", "among", "contains",
			"contains_any", "contains_all", "min_of", "max_of", "with_max_of", "with_min_of", "of_species",
			"of_generic_species", "sort_by", "accumulate", "or", "and", "at", "is", "group_by", "index_of",
			"last_index_of", "index_by", "count", "sort", "::", "as_map");

	/**
	 * Adds the new var keyword.
	 *
	 * @param s
	 *            the s
	 * @param kind
	 *            the kind
	 */
	public static void addNewVarKeyword(final String s, final int kind) {
		VARKIND2KEYWORDS.put(kind, s);
	}

	/**
	 * Gets the statement proto.
	 *
	 * @param keyword
	 *            the keyword
	 * @param control
	 *            the control
	 * @return the statement proto
	 */
	public final static IArtefactProto.Symbol getStatementProto(final String keyword) {
		return STATEMENT_KEYWORDS_PROTOS.get(keyword);
	}

	/**
	 * Gets the proto names.
	 *
	 * @return the proto names
	 */
	public final static Iterable<String> getProtoNames() {
		return Iterables.concat(getStatementProtoNames(), getVarProtoNames());
	}

	/**
	 * Gets the statement proto names.
	 *
	 * @return the statement proto names
	 */
	public final static Iterable<String> getStatementProtoNames() { return STATEMENT_KEYWORDS_PROTOS.keySet(); }

	/**
	 * Gets the var proto names.
	 *
	 * @return the var proto names
	 */
	public final static Iterable<String> getVarProtoNames() { return VAR_KEYWORDS_PROTOS.keySet(); }

	/**
	 * Gets the proto.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @return the proto
	 */
	public final static IArtefactProto.Symbol getProto(final String keyword, final IDescription superDesc) {
		// Check statement proto first
		IArtefactProto.Symbol p = STATEMENT_KEYWORDS_PROTOS.get(keyword);
		// If not a statement, try var declaration prototype
		return p != null ? p : getVarProto(keyword, superDesc);
	}

	/**
	 * Gets the var proto.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @return the var proto
	 */
	public final static IArtefactProto.Symbol getVarProto(final String keyword, final IDescription superDesc) {
		final IArtefactProto.Symbol p = VAR_KEYWORDS_PROTOS.get(keyword);
		if (p == null) {
			// If not a var declaration, we try to find if it is not a species
			// name (in which case, it is an "agent"
			// declaration prototype)
			if (superDesc == null) return null;
			final IModelDescription md = superDesc.getModelDescription();
			if (md == null) return null;
			final IType t = md.getTypesManager().get(keyword);
			if (t.isAgentType()) return getVarProto(AGENT, null);
		}
		return p;
	}

	/**
	 * Checks if is statement proto.
	 *
	 * @param s
	 *            the s
	 * @return true, if is statement proto
	 */
	public final static boolean isStatementProto(final String s) {
		// WARNING METHOD is treated here as a special keyword, but it should be
		// leveraged in the future
		return STATEMENT_KEYWORDS_PROTOS.containsKey(s) || IKeyword.METHOD.equals(s);
	}

	/**
	 * Gets the omissible facet for symbol.
	 *
	 * @param keyword
	 *            the keyword
	 * @return the omissible facet for symbol
	 */
	public static String getOmissibleFacetForSymbol(final String keyword) {
		final IArtefactProto.Symbol md = getProto(keyword, null);
		if (md == null) return IKeyword.NAME;
		return md.getOmissible();
	}

	/**
	 * Gets the allowed facets for.
	 *
	 * @param keys
	 *            the keys
	 * @return the allowed facets for
	 */
	public static Set<String> getAllowedFacetsFor(final String key) {
		if (key == null) return Collections.emptySet();
		final IArtefactProto.Symbol md = getProto(key, null);
		if (md == null) return Collections.emptySet();
		return md.getPossibleFacets().keySet();
	}

	/**
	 * Gets the statement protos.
	 *
	 * @return the statement protos
	 */
	public static Iterable<IArtefactProto.Symbol> getStatementProtos() {
		if (cachedStatementProtos == null) {
			cachedStatementProtos =
					Iterables.filter(Iterables.concat(STATEMENT_KEYWORDS_PROTOS.values(), VAR_KEYWORDS_PROTOS.values()),
							IArtefactProto.Symbol.class);
		}
		return cachedStatementProtos;
	}

	/**
	 * Gets the facets protos.
	 *
	 * @return the facets protos
	 */
	public static Iterable<? extends IArtefactProto.Facet> getFacetsProtos() {
		if (cachedFacetsProtos == null) {
			cachedFacetsProtos = Iterables
					.concat(Iterables.transform(getStatementProtos(), each -> each.getPossibleFacets().values()));
		}
		return cachedFacetsProtos;
	}

	/**
	 * Adds the species name as type.
	 *
	 * @param name
	 *            the name
	 */
	public static void addSpeciesNameAsType(final String name) {
		if (!AGENT.equals(name) && !IKeyword.EXPERIMENT.equals(name)) {
			VAR_KEYWORDS_PROTOS.putIfAbsent(name, VAR_KEYWORDS_PROTOS.get(AGENT));
		}
	}

	/**
	 * Adds the proto.
	 *
	 * @param md
	 *            the md
	 * @param names
	 *            the names
	 */
	public static void addProto(final IArtefactProto.Symbol md, final Iterable<String> names) {
		final int kind = md.getKind();
		if (ISymbolKind.Variable.KINDS.contains(kind)) {
			for (final String s : names) { VAR_KEYWORDS_PROTOS.putIfAbsent(s, md); }
		} else {
			for (final String s : names) { STATEMENT_KEYWORDS_PROTOS.put(s, md); }
		}
		KINDS_PROTOS.put(kind, md);
	}

}
