/*******************************************************************************************************
 *
 * ArtefactRegistry.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.registries;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.IInternalFacets;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * Central registry for GAML language artefact prototypes (statements, variables, and facets).
 *
 * <p>
 * This registry maintains the metadata and prototypes for all GAML language constructs, including statements (e.g.
 * {@code create}, {@code ask}, {@code loop}), variable declarations (e.g. {@code int}, {@code float}, {@code species}),
 * and their facets (e.g. {@code name:}, {@code type:}, {@code from:}). It serves as the primary reference for the GAML
 * compiler and parser when resolving keywords and their legal facets.
 * </p>
 *
 * <h2>Artefact Categories</h2>
 * <p>
 * The registry manages three main categories of artefacts:
 * </p>
 * <ul>
 * <li><b>Statement Artefacts</b> – Prototypes for GAML statements (commands, control structures). Keyed by keyword in
 * {@link #STATEMENT_ARTEFACTS}.</li>
 * <li><b>Variable Artefacts</b> – Prototypes for variable declaration keywords, keyed by {@link ISymbolKind} in
 * {@link #VAR_DECLARATION_ARTEFACTS}.</li>
 * <li><b>Facet Artefacts</b> – Prototypes for facets (named parameters) associated with statements or variables,
 * derived lazily from the statement artefacts.</li>
 * </ul>
 *
 * <h2>Special Keyword Sets</h2>
 * <p>
 * Several constant sets expose keywords with particular semantic roles:
 * </p>
 * <ul>
 * <li>{@link #BREAKABLE_STATEMENTS} – Statements that may legally contain a {@code break} instruction.</li>
 * <li>{@link #CONTINUABLE_STATEMENTS} – Statements that may legally contain a {@code continue} instruction.</li>
 * <li>{@link #BINARY_ARTEFACTS_NAMES} – All binary operator keywords.</li>
 * <li>{@link #ARTEFACTS_WITHOUT_PARENTHESES} – Unary operators that do not require surrounding parentheses.</li>
 * <li>{@link #NON_SERIALIZABLE_FACETS} – Facets that must be excluded from serialization.</li>
 * <li>{@link #ID_FACETS} – Facet type IDs that represent identifiers or labels.</li>
 * </ul>
 *
 * <h2>Registration</h2>
 * <p>
 * Artefacts are registered at startup via {@link #addArtefact(IArtefact.Symbol, Iterable)}, which records the prototype
 * both under each of its keyword names and under its {@link ISymbolKind}. The lazy caches
 * ({@link #cachedStatementArtefacts} and {@link #cachedFacetsArtefacts}) are populated on first access and must be
 * invalidated whenever new artefacts are added.
 * </p>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Retrieve a statement artefact by keyword
 * IArtefact.Symbol createArtefact = ArtefactRegistry.getStatementArtefact("create");
 *
 * // Check whether a keyword corresponds to a statement
 * boolean isStatement = ArtefactRegistry.isStatementArtefact("loop");
 *
 * // Retrieve the omissible facet name for a given keyword
 * String omissible = ArtefactRegistry.getOmissibleFacetForSymbol("create");
 *
 * // Retrieve the set of allowed facets for a keyword
 * Set<String> facets = ArtefactRegistry.getAllowedFacetsFor("create");
 * }</pre>
 *
 * @author drogoul
 * @since GAMA 1.0
 * @see IArtefact
 * @see IArtefact.Symbol
 * @see IArtefact.Facet
 * @see gama.api.compilation.descriptions.IDescription
 */
public class ArtefactRegistry {

	/**
	 * Set of statement keywords that may legally contain a {@code break} instruction (e.g. {@code loop}, {@code ask}).
	 */
	public static final Set<String> BREAKABLE_STATEMENTS = new HashSet<>();

	/**
	 * Set of statement keywords that may legally contain a {@code continue} instruction (e.g. {@code loop}).
	 */
	public static final Set<String> CONTINUABLE_STATEMENTS = new HashSet<>();

	/**
	 * List of {@link IType} kind constants whose facets represent identifiers or labels rather than arbitrary
	 * expressions. Includes {@link IType#LABEL}, {@link IType#ID}, {@link IType#NEW_TEMP_ID}, and
	 * {@link IType#NEW_VAR_ID}.
	 */
	public static final List<Integer> ID_FACETS =
			Arrays.asList(IType.LABEL, IType.ID, IType.NEW_TEMP_ID, IType.NEW_VAR_ID);

	/**
	 * Set of facet names that must be excluded from GAML model serialization (e.g. {@code internal_function},
	 * {@code with}).
	 */
	public static final Set<String> NON_SERIALIZABLE_FACETS = new HashSet<>(
			Arrays.asList(IInternalFacets.INTERNAL_TARGET, IInternalFacets.INTERNAL_FUNCTION, IKeyword.WITH));

	/**
	 * Primary map from GAML statement keyword strings to their corresponding {@link IArtefact.Symbol} prototype
	 * definitions.
	 */
	private static final Map<String, IArtefact.Symbol> STATEMENT_ARTEFACTS = new HashMap<>();

	/**
	 * Map from {@link ISymbolKind} constants to the {@link IArtefact.Symbol} prototype that represents variable
	 * declarations of that kind.
	 */
	private static final Map<ISymbolKind, IArtefact.Symbol> VAR_DECLARATION_ARTEFACTS = new HashMap<>();

	/**
	 * Lazy cache of all facet artefact prototypes, computed on first call to {@link #getFacetsArtefacts()} and
	 * invalidated when new artefacts are registered.
	 */
	private static volatile Iterable<? extends IArtefact.Facet> cachedFacetsArtefacts = null;

	// /**
	// * Returns the set of facet names that are allowed on the {@code do} statement. The set is computed lazily on
	// first
	// * access and cached in {@link #DO_FACETS}.
	// *
	// * @return the (possibly cached) set of facet names for the {@code do} statement; never {@code null}
	// */
	// public static Set<String> getDoFacets() {
	// if (DO_FACETS == null) { DO_FACETS = getAllowedFacetsFor(IKeyword.DO); }
	// return DO_FACETS;
	// }

	/** Operators that can be used without parentheses in GAML expressions. */
	public static final Set<String> ARTEFACTS_WITHOUT_PARENTHESES = ImmutableSet.of("-", "!");

	/** Binary operator keywords in GAML. */
	public static final Set<String> BINARY_ARTEFACTS_NAMES = ImmutableSet.of(IKeyword.EQUALS, IKeyword.PLUS,
			IKeyword.MINUS, IKeyword.DIVIDE, IKeyword.TIMES, "^", "<", ">", "<=", ">=", "?", "!=", ":", ".", "where",
			"select", "collect", "first_with", "last_with", "overlapping", "at_distance", "in", "inside", "among",
			"contains", "contains_any", "contains_all", "min_of", "max_of", "with_max_of", "with_min_of", "of_species",
			"of_generic_species", "sort_by", "accumulate", "or", "and", "at", "is", "group_by", "index_of",
			"last_index_of", "index_by", "count", "sort", "::", "as_map");

	/**
	 * Returns the {@link IArtefact.Symbol} prototype registered for the given GAML statement keyword, or {@code null}
	 * if no statement artefact is registered under that keyword.
	 *
	 * @param keyword
	 *            the GAML keyword to look up (e.g. {@code "create"}, {@code "loop"})
	 * @return the corresponding statement artefact, or {@code null} if not found
	 */
	public final static IArtefact.Symbol getStatementArtefact(final String keyword) {
		return STATEMENT_ARTEFACTS.get(keyword);
	}

	/**
	 * Returns all GAML statement keyword strings that have a registered artefact prototype.
	 *
	 * @return an iterable over the registered statement keyword names; never {@code null}
	 */
	public final static Iterable<String> getStatementArtefactNames() { return STATEMENT_ARTEFACTS.keySet(); }

	/**
	 * Returns the best-matching {@link IArtefact.Symbol} prototype for the given keyword, consulting statement
	 * artefacts first and falling back to variable declaration artefacts.
	 *
	 * @param keyword
	 *            the GAML keyword to look up
	 * @param superDesc
	 *            the enclosing {@link IDescription} context, used when resolving variable artefacts; may be
	 *            {@code null}
	 * @return the matching artefact prototype, or {@code null} if none is found
	 */
	public final static IArtefact.Symbol getArtefact(final String keyword, final IDescription superDesc) {
		// Check statement artefacts first
		IArtefact.Symbol artefact = STATEMENT_ARTEFACTS.get(keyword);
		// If not a statement, try var declaration artefact
		return artefact != null ? artefact : getVarArtefact(keyword, superDesc);
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
	public final static IArtefact.Symbol getVarArtefact(final String keyword, final IDescription superDesc) {
		// Not a type and not a statement. So probably a species
		if (!Types.containsType(keyword) && !STATEMENT_ARTEFACTS.containsKey(keyword))
			return VAR_DECLARATION_ARTEFACTS.get(Types.AGENT.getVarKind());
		return VAR_DECLARATION_ARTEFACTS.get(Types.get(keyword).getVarKind());
		// superDesc will be useful later when objets / skills will be defined in GAML
	}

	/**
	 * Returns {@code true} if the given keyword is registered as a statement artefact, or is the special
	 * {@link IKeyword#METHOD} keyword. Assignments are added because they are translated early on
	 *
	 * @param s
	 *            the GAML keyword to test
	 * @return {@code true} if {@code s} maps to a statement artefact or equals {@code "method"}
	 */
	public final static boolean isStatementArtefact(final String s) {
		return STATEMENT_ARTEFACTS.containsKey(s) || IKeyword.METHOD.equals(s) || IKeyword.ASSIGNMENTS.contains(s);
	}

	/**
	 * Returns the name of the omissible facet for the given GAML keyword. The omissible facet is the one that may be
	 * written without its name in a GAML statement. Falls back to {@link IKeyword#NAME} when no artefact is found.
	 *
	 * @param keyword
	 *            the GAML keyword to query (e.g. {@code "create"}, {@code "loop"})
	 * @return the omissible facet name, or {@link IKeyword#NAME} if the artefact is unknown
	 */
	public static String getOmissibleFacetForSymbol(final String keyword) {
		final IArtefact.Symbol md = getArtefact(keyword, null);
		if (md == null) return IKeyword.NAME;
		return md.getOmissible();
	}

	/**
	 * Returns the set of facet names that are allowed for the given GAML keyword. Returns an empty set when the keyword
	 * is {@code null} or has no registered artefact.
	 *
	 * @param key
	 *            the GAML keyword to query; may be {@code null}
	 * @return the set of allowed facet names, or an empty set if the keyword is unknown
	 */
	public static Set<String> getAllowedFacetsFor(final String key) {
		if (key == null) return Collections.emptySet();
		final IArtefact.Symbol md = getArtefact(key, null);
		if (md == null) return Collections.emptySet();
		return md.getPossibleFacets().keySet();
	}

	/**
	 * Returns all registered {@link IArtefact.Symbol} prototypes for GAML statements. The result is computed lazily on
	 * first access and cached in {@link #cachedStatementArtefacts}.
	 *
	 * @return an iterable over all registered statement artefact prototypes; never {@code null}
	 */
	public static Iterable<IArtefact.Symbol> getStatementArtefacts() { return STATEMENT_ARTEFACTS.values(); }

	/**
	 * Returns all registered {@link IArtefact.Facet} prototypes, derived lazily from the facets declared on each
	 * statement artefact. The result is cached in {@link #cachedFacetsArtefacts} on first access.
	 *
	 * @return an iterable over all registered facet artefact prototypes; never {@code null}
	 */
	public static Iterable<? extends IArtefact.Facet> getFacetsArtefacts() {
		if (cachedFacetsArtefacts == null) {
			cachedFacetsArtefacts = Iterables
					.concat(Iterables.transform(getStatementArtefacts(), each -> each.getPossibleFacets().values()));
		}
		return cachedFacetsArtefacts;
	}

	/**
	 * Registers a new {@link IArtefact.Symbol} prototype under each of the given keyword names and under its
	 * {@link ISymbolKind}. Both the {@link #STATEMENT_ARTEFACTS} map and the {@link #VAR_DECLARATION_ARTEFACTS} map are
	 * updated. Any previously cached iterables derived from these maps (see {@link #cachedStatementArtefacts} and
	 * {@link #cachedFacetsArtefacts}) should be considered stale after this call.
	 *
	 * @param md
	 *            the artefact prototype to register; must not be {@code null}
	 * @param names
	 *            the GAML keyword strings under which the prototype should be reachable; must not be {@code null}
	 */
	public static void addArtefact(final IArtefact.Symbol md, final Iterable<String> names) {
		final ISymbolKind kind = md.getKind();
		if (ISymbolKind.isVariable(kind)) {
			VAR_DECLARATION_ARTEFACTS.put(kind, md);
		} else {
			for (final String s : names) { STATEMENT_ARTEFACTS.put(s, md); }
		}

	}

	/**
	 * Prints a summary of the current registry state to the debug output, listing the registered statement artefact
	 * keywords, the registered type names, and the registered {@link ISymbolKind} keys for variable artefacts.
	 */
	public static void writeStats() {
		DEBUG.LINE();
		DEBUG.TITLE("Artefact Registry Stats");
		DEBUG.LINE();
		DEBUG.LOG("Statement artefacts registered: " + STATEMENT_ARTEFACTS.keySet());
		DEBUG.LOG("Compared to registered types: " + StreamEx.of(Iterables.toArray(Types.getTypeNames(), String.class))
				.sorted().collect(Collectors.toList()));
		DEBUG.LOG("Kinds artefacts registered: " + VAR_DECLARATION_ARTEFACTS);
		DEBUG.LINE();
	}

}
