/*******************************************************************************************************
 *
 * GamaMapType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;

/**
 * Type representing associative arrays (maps) in GAML. Maps store key-value pairs where each key is unique and maps to
 * exactly one value.
 * 
 * <p>
 * Maps in GAML:
 * </p>
 * <ul>
 * <li>Store key-value associations (like dictionaries or hash tables)</li>
 * <li>Maintain insertion order (unlike traditional hash maps)</li>
 * <li>Require unique keys (duplicate keys overwrite values)</li>
 * <li>Can be parameterized with both key and value types (e.g., {@code map<string,int>})</li>
 * <li>Support efficient key-based lookup, iteration, and transformation</li>
 * </ul>
 * 
 * <p>
 * Map literals and operations in GAML:
 * </p>
 * 
 * <pre>
 * // Empty map
 * map<string,int> scores <- map([]);
 * 
 * // Map with initial pairs
 * map<string,float> prices <- ["apple"::2.5, "banana"::1.8, "orange"::3.2];
 * 
 * // Map access and modification
 * scores["Alice"] <- 100;
 * int score <- scores["Alice"];
 * 
 * // Convert agent to map of its attributes
 * map<string,unknown> attrs <- map(myAgent);
 * </pre>
 * 
 * <p>
 * Type conversion to map handles various cases:
 * </p>
 * <ul>
 * <li><b>Agent → Map:</b> Returns map of attribute names to values</li>
 * <li><b>String → Map:</b> Parses JSON and converts to map</li>
 * <li><b>List of pairs → Map:</b> Converts pairs to key-value entries</li>
 * <li><b>Matrix → Map:</b> Maps indices to values</li>
 * <li><b>Graph → Map:</b> Maps edges (as pairs) to their weights</li>
 * </ul>
 * 
 * <p>
 * Maps preserve insertion order, making them suitable for ordered dictionaries. They are implemented using
 * {@link IMap} interface and created via {@link GamaMapFactory}.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see IMap
 * @see GamaMapFactory
 * @see GamaContainerType
 * @see GamaPairType
 */
@type (
		name = IKeyword.MAP,
		id = IType.MAP,
		wraps = { IMap.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.MAP },
		doc = @doc ("Represents lists of pairs key::value, where each key is unique in the map. Maps are ordered by the insertion order of elements"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMapType extends GamaContainerType<IMap> {

	/**
	 * Constructs a new map type with the specified types manager.
	 * 
	 * <p>
	 * The map type is a built-in parametric type that requires two type parameters: key type and value type (e.g.,
	 * {@code map<string,int>}).
	 * </p>
	 *
	 * @param typesManager
	 *            the types manager that owns this type
	 */
	public GamaMapType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a GAMA map with specified key and content types.
	 * 
	 * <p>
	 * Conversion strategies:
	 * </p>
	 * <ul>
	 * <li><b>Agent → Map:</b> Converts agent attributes to map entries (attribute name → value)</li>
	 * <li><b>String → Map:</b> Parses JSON string and converts to map structure</li>
	 * <li><b>List of pairs → Map:</b> Each pair becomes a key-value entry</li>
	 * <li><b>Matrix → Map:</b> Maps each cell's coordinates to its value</li>
	 * <li><b>Graph → Map:</b> Maps edges (represented as pairs of vertices) to their weights</li>
	 * <li><b>Existing map → Map:</b> Creates a copy or type-converted map</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Cast agent to map of attributes
	 * IMap attrs = mapType.cast(scope, agent, null, Types.STRING, Types.NO_TYPE, false);
	 * // Returns {"name": "agent1", "location": point(0,0), ...}
	 * 
	 * // Cast list of pairs to map
	 * IList pairs = GamaListFactory.create(scope, Types.PAIR, pair("a", 1), pair("b", 2));
	 * IMap map = mapType.cast(scope, pairs, null, Types.STRING, Types.INT, false);
	 * // Returns {"a": 1, "b": 2}
	 * 
	 * // Parse JSON string to map
	 * String json = "{\"x\": 10, \"y\": 20}";
	 * IMap parsed = mapType.cast(scope, json, null, Types.STRING, Types.INT, false);
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a map
	 * @param param
	 *            optional parameter (not used for maps)
	 * @param keyType
	 *            the type for map keys
	 * @param contentType
	 *            the type for map values
	 * @param copy
	 *            if true, creates a defensive copy of the map
	 * @return the object cast to an IMap
	 * @throws GamaRuntimeException
	 *             if the cast fails
	 * 
	 * @see GamaMapFactory#castToMap(IScope, Object, IType, IType, boolean)
	 */
	@Override
	@doc ("Casts the operand into a map. In case of an agent, returns its attributes. In case of a string, tries to parse JSON contents and returns a corresponding map.")
	public IMap cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) throws GamaRuntimeException {
		return GamaMapFactory.castToMap(scope, obj, keyType, contentType, copy);
	}

	/**
	 * Returns the number of type parameters for maps, which is always 2 (key type and value type).
	 * 
	 * <p>
	 * Unlike lists (1 parameter) or simple types (0 parameters), maps require both a key type and a value type to be
	 * fully specified.
	 * </p>
	 *
	 * @return 2, the number of type parameters for maps
	 */
	@Override
	public int getNumberOfParameters() { return 2; }

	/**
	 * Determines the key type when casting a specific expression to a map.
	 * 
	 * <p>
	 * The inferred key type depends on the source expression type:
	 * </p>
	 * <ul>
	 * <li><b>Agent:</b> Keys are attribute names → Types.STRING</li>
	 * <li><b>String (JSON):</b> Keys are JSON property names → Types.STRING</li>
	 * <li><b>Map:</b> Preserves the original key type</li>
	 * <li><b>Pair:</b> Preserves the pair's key type</li>
	 * <li><b>Matrix:</b> Keys are cell values → matrix's content type</li>
	 * <li><b>Graph:</b> Keys are edges (vertex pairs) → Types.PAIR</li>
	 * <li><b>List of pairs:</b> Keys are pair keys → pair's key type</li>
	 * <li><b>List of other:</b> Keys are list elements → list's content type</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Agent expression
	 * keyTypeIfCasting(agentExpr) → Types.STRING
	 * 
	 * // List<pair<int,string>> expression
	 * keyTypeIfCasting(listOfPairsExpr) → Types.INT
	 * 
	 * // Matrix<float> expression
	 * keyTypeIfCasting(matrixExpr) → Types.FLOAT
	 * }
	 * </pre>
	 *
	 * @param exp
	 *            the expression being cast to a map
	 * @return the inferred key type for the resulting map
	 */
	@Override
	public IType keyTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		if (itemType.isAgentType()) return Types.STRING;
		switch (itemType.id()) {
			case STRING:
				return Types.STRING;
			case PAIR:
			case MAP:
				return itemType.getKeyType();
			case MATRIX:
				return itemType.getContentType();
			case GRAPH:
				return Types.PAIR;
			case LIST:
				if (itemType.getContentType().id() == IType.PAIR) return itemType.getContentType().getKeyType();
				return itemType.getContentType();
		}
		return itemType;
	}

	/**
	 * Determines the value/content type when casting a specific expression to a map.
	 * 
	 * <p>
	 * The inferred content type depends on the source expression type:
	 * </p>
	 * <ul>
	 * <li><b>Agent:</b> Values are attribute values → Types.NO_TYPE (mixed types)</li>
	 * <li><b>String (JSON):</b> Values are JSON values → Types.NO_TYPE (mixed types)</li>
	 * <li><b>Map:</b> Preserves the original content type</li>
	 * <li><b>Pair:</b> Values are pair values → pair's content type</li>
	 * <li><b>Matrix:</b> Values are cell values → matrix's content type</li>
	 * <li><b>Graph:</b> Values are edge weights → graph's content type</li>
	 * <li><b>List of pairs:</b> Values are pair values → pair's content type</li>
	 * <li><b>List of other:</b> Values are list elements → list's content type</li>
	 * </ul>
	 *
	 * @param exp
	 *            the expression being cast to a map
	 * @return the inferred value type for the resulting map
	 */
	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		if (itemType.isAgentType()) return Types.NO_TYPE;
		switch (itemType.id()) {
			case STRING:
				return Types.NO_TYPE;
			case LIST:
				if (itemType.getContentType().id() == IType.PAIR) return itemType.getContentType().getContentType();
				return itemType.getContentType();
			case PAIR:
			case GRAPH:
			case MAP:
			case MATRIX:
				return itemType.getContentType();

		}
		return itemType;
	}

	/**
	 * Indicates that maps can be cast to constant expressions.
	 * 
	 * <p>
	 * Map literals (e.g., {@code ["a"::1, "b"::2]}) can be evaluated at compile time and used in constant contexts.
	 * </p>
	 *
	 * @return true, maps support constant casting
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}
}
