/*******************************************************************************************************
 *
 * GamaPairType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;
import gama.api.types.pair.GamaPairFactory;
import gama.api.types.pair.IPair;

/**
 * Type representing pairs in GAML - simple containers holding exactly two arbitrary elements.
 * <p>
 * Pairs are lightweight data structures for grouping two related values together. They are commonly used for key-value
 * associations, coordinate pairs, range bounds, and any situation where two values need to be treated as a single
 * unit. Pairs are parametric types with separate types for their two components.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Fixed size container (always two elements)</li>
 * <li>Parametric types for key and value components</li>
 * <li>Efficient for simple associations</li>
 * <li>Can be constant if both elements are constant</li>
 * <li>Accessible via .key and .value attributes</li>
 * <li>Created with :: operator</li>
 * </ul>
 * 
 * <h2>Type Parameters:</h2>
 * <p>
 * Pairs have two type parameters:
 * <ul>
 * <li>Key type - type of the first element</li>
 * <li>Value type - type of the second element</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Create pairs using :: operator
 * pair<string, int> name_age <- "Alice" :: 30;
 * pair<float, float> coordinates <- 45.5 :: 23.7;
 * pair<agent, float> agent_distance <- target_agent :: 15.0;
 * 
 * // Access components
 * string name <- name_age.key;
 * int age <- name_age.value;
 * 
 * // Type inference
 * pair my_pair <- "key" :: "value";  // pair<string, string>
 * 
 * // In map operations (maps are containers of pairs)
 * map<string, int> scores <- ["Alice"::100, "Bob"::85];
 * loop p over: scores.pairs {
 *     write p.key + " scored " + p.value;
 * }
 * 
 * // Range specification
 * pair<int, int> range <- 1 :: 10;
 * 
 * // Geographic bounds
 * pair<point, point> bbox <- {0,0} :: {100,100};
 * 
 * // Can be used in lists
 * list<pair<string, float>> data <- [
 *     "temperature" :: 25.5,
 *     "humidity" :: 60.0
 * ];
 * }
 * </pre>
 * 
 * <h2>Relationship with Maps:</h2>
 * <p>
 * Maps are essentially containers of pairs. The .pairs attribute of a map returns a list of pairs, and pairs can be
 * used to construct maps.
 * </p>
 * 
 * @author GAMA Development Team
 * @see GamaContainerType
 * @see IPair
 * @see gama.api.types.pair.GamaPairFactory
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.PAIR,
		id = IType.PAIR,
		wraps = { IPair.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.CONTAINER },
		doc = @doc ("Represents a pair of 2 arbitrary elements"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPairType extends GamaContainerType<IPair> {

	/**
	 * Constructs a new pair type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaPairType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a pair.
	 * <p>
	 * This method supports casting from various source types:
	 * <ul>
	 * <li>Pair - returns the pair itself</li>
	 * <li>Map entry - creates a pair from the key and value</li>
	 * <li>Other types - attempts to create a pair based on the object structure</li>
	 * </ul>
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a pair
	 * @param param
	 *            optional parameter (not typically used for pair casting)
	 * @param keyType
	 *            the desired type for the pair's key component
	 * @param contentsType
	 *            the desired type for the pair's value component
	 * @param copy
	 *            whether to create a copy if obj is already a pair
	 * @return the pair representation of the object
	 */
	@Override
	public IPair cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) {
		return GamaPairFactory.castToPair(scope, obj, keyType, contentsType, copy);
	}

	/**
	 * Returns the number of type parameters for pairs.
	 * <p>
	 * Pairs are parametric types with two parameters: key type and value type.
	 * </p>
	 * 
	 * @return 2, as pairs have two type parameters
	 */
	@Override
	public int getNumberOfParameters() { return 2; }

	/**
	 * Determines the key type when casting an expression to a pair.
	 * <p>
	 * Analyzes the expression type to determine what type the pair's key will have:
	 * <ul>
	 * <li>For pairs: returns the key type</li>
	 * <li>For maps: returns a list of the map's key type (as maps cast to pairs of lists)</li>
	 * <li>Otherwise: returns the expression's type itself</li>
	 * </ul>
	 * </p>
	 * 
	 * @param exp
	 *            the expression being cast to a pair
	 * @return the type that the pair's key will have
	 */
	@Override
	public IType keyTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		switch (itemType.id()) {
			case PAIR:
				return itemType.getKeyType();
			case MAP:
				return Types.LIST.of(itemType.getKeyType());
		}
		return itemType;
	}

	/**
	 * Determines the value type when casting an expression to a pair.
	 * <p>
	 * Analyzes the expression type to determine what type the pair's value will have:
	 * <ul>
	 * <li>For pairs: returns the value type</li>
	 * <li>For maps: returns a list of the map's value type (as maps cast to pairs of lists)</li>
	 * <li>Otherwise: returns the expression's type itself</li>
	 * </ul>
	 * </p>
	 * 
	 * @param exp
	 *            the expression being cast to a pair
	 * @return the type that the pair's value will have
	 */
	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		switch (itemType.id()) {
			case MAP:
				return Types.LIST.of(itemType.getContentType());
			case PAIR:
				return itemType.getContentType();

		}
		return itemType;
	}

	/**
	 * Returns the default value for pair type.
	 * <p>
	 * Creates a default pair with null key and value.
	 * </p>
	 * 
	 * @return a default pair instance
	 */
	@Override
	public IPair getDefault() { return GamaPairFactory.createDefault(); }

	/**
	 * Returns the content type of pairs.
	 * <p>
	 * Since pairs can contain any types, the content type is none by default.
	 * </p>
	 * 
	 * @return the none type
	 */
	@Override
	public IType getContentType() { return Types.get(NONE); }

	/**
	 * Indicates whether pairs can be cast to constant values.
	 * <p>
	 * Pairs can be constant if both their key and value are constant.
	 * </p>
	 * 
	 * @return true, pairs can be constant
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Deserializes a pair from a JSON representation.
	 * <p>
	 * The JSON map should contain the pair's key and value, along with type information in "requested_type".
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing pair data
	 * @return the deserialized pair
	 */
	@Override
	public IPair deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		IType requested = (IType) map2.remove("requested_type");
		return GamaPairFactory.castToPair(scope, map2, requested.getKeyType(), requested.getContentType(), false);
	}

}
