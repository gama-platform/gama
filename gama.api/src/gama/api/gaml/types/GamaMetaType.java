/*******************************************************************************************************
 *
 * GamaMetaType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;

/**
 * Meta-type representing types themselves in GAML - the type of all types.
 * <p>
 * The meta-type (or type-type) allows types themselves to be treated as first-class values in GAML. This enables
 * powerful meta-programming capabilities including runtime type inspection, dynamic type manipulation, and generic
 * algorithms that operate on types. It's the foundation of GAMA's reflective type system.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Types as first-class values</li>
 * <li>Runtime type inspection and manipulation</li>
 * <li>Generic programming support</li>
 * <li>Type comparison and checking</li>
 * <li>Dynamic type resolution</li>
 * <li>Meta-programming capabilities</li>
 * </ul>
 *
 * <h2>Type Inspection:</h2>
 * <p>
 * The meta-type provides operators for inspecting and working with types:
 * <ul>
 * <li>type_of - returns the declared type of an expression</li>
 * <li>actual_type_of - returns the runtime type of a value</li>
 * <li>Type comparison and compatibility checking</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>
 * {@code
 * // Get type of variables/expressions
 * gaml_type str_type <- type_of("hello");  // Returns string type
 * gaml_type list_type <- type_of([1,2,3]); // Returns list<int>
 * gaml_type agent_type <- type_of(self);   // Returns agent type
 *
 * // Actual runtime type (computed from value)
 * list my_list <- [1,2,3];
 * gaml_type actual <- actual_type_of(my_list);  // list<int>
 *
 * // Declared type (from variable declaration)
 * gaml_type declared <- type_of(my_list);  // just 'list'
 *
 * // Type as string
 * string type_name <- string(type_of([1,2,3]));  // "list<int>"
 *
 * // Store types in collections
 * list<gaml_type> numeric_types <- [int, float];
 * map<string, gaml_type> type_registry <- [
 *     "integer" :: int,
 *     "decimal" :: float,
 *     "text" :: string
 * ];
 *
 * // Use in generic algorithms
 * action create_from_type(gaml_type t, int count) {
 *     // Create entities based on type parameter
 *     if (t = species) {
 *         create t number: count;
 *     }
 * }
 *
 * // Type checking
 * bool is_list <- type_of(my_var) = list;
 * bool is_container <- container in ancestors_of(type_of(my_var));
 *
 * // Parametric types
 * gaml_type list_of_agents <- list<agent>;
 * gaml_type map_str_int <- map<string, int>;
 * }
 * </pre>
 *
 * <h2>Type Hierarchy:</h2>
 * <p>
 * The meta-type allows inspection of GAMA's type hierarchy:
 * <ul>
 * <li>Check type compatibility and assignment</li>
 * <li>Navigate parent/child type relationships</li>
 * <li>Determine common supertypes</li>
 * </ul>
 * </p>
 *
 * @author GAMA Development Team
 * @see GamaType
 * @see IType
 * @since GAMA 1.0
 */
@type (
		name = "gaml_type",
		id = IType.TYPE,
		wraps = { IType.class },
		doc = @doc ("Metatype of all types in GAML"))
public class GamaMetaType extends GamaType<IType<?>> {

	/**
	 * Constructs a new meta-type.
	 *
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaMetaType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Indicates whether types can be cast to constant values.
	 * <p>
	 * Types can be constant as they are immutable descriptors.
	 * </p>
	 *
	 * @return true, types can be constant
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Casts an object to a type.
	 * <p>
	 * This method supports casting from:
	 * <ul>
	 * <li>IType - returns the type itself</li>
	 * <li>String - decodes the type name and returns the corresponding type</li>
	 * <li>Other objects - determines the type from the object's class</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to get the type of
	 * @param param
	 *            optional parameter (not used for type casting)
	 * @param copy
	 *            whether to create a copy (not applicable for types)
	 * @return the type representation
	 * @throws GamaRuntimeException
	 *             if the casting operation encounters an error
	 */
	@doc ("The type of all types")
	@no_test
	@Override
	public IType<?> cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof IType t) return t;
		if (obj instanceof String s) return scope.getTypes().decodeType(s);
		return staticCast(obj);
	}

	/**
	 * Statically determines the type of an object.
	 * <p>
	 * This is a static version of type resolution that doesn't require a scope.
	 * </p>
	 *
	 * @param obj
	 *            the object to get the type of
	 * @return the type of the object
	 */
	public static IType<?> staticCast(final Object obj) {
		return GamaType.of(obj);
	}

	/**
	 * Returns the default value for meta-type.
	 * <p>
	 * The default type is NO_TYPE, representing absence of type information.
	 * </p>
	 *
	 * @return the NO_TYPE constant
	 */
	@Override
	public IType<?> getDefault() { return Types.NO_TYPE; }

	/**
	 * Returns the declared GAML type of an expression.
	 * <p>
	 * This operator returns the type as declared in the model, based on compile-time type inference. For variables, it
	 * returns the declared type, not the actual runtime type of the contents. Use actual_type_of for runtime type
	 * inspection.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>
	 * {@code
	 * list my_list <- [1,2,3];
	 * string declared <- string(type_of(my_list));  // Returns "list" (declared type)
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the expression whose type to retrieve
	 * @return the declared GAML type of the expression
	 */
	@operator (
			value = { "type_of" },
			can_be_const = true,
			doc = @doc ("Returns the GAML type of the operand"))
	@doc (
			value = "Returns the GAML type of the operand. Note that the type of a variable, e.g. `type_of(v)` will correspond to its declared type, but not to its actual contents. "
					+ "So, if v is declared as `list`, then `type_of` will return `list`, without consideration for what is stored inside. ",
			examples = { @example (
					value = "string(type_of(\"a string\"))",
					equals = "\"string\"",
					returnType = "string"),
					@example (
							value = "string(type_of([1,2,3,4,5]))",
							equals = "\"list<int>\"",
							returnType = "string"),
					@example (
							value = "string(type_of(g0))",
							equals = "\"point\"",
							returnType = "string")

			})
	@no_test
	public static IType<?> typeOf(final IScope scope, final IExpression obj) {
		if (obj == null) return Types.NO_TYPE;
		return obj.getGamlType();
	}

	/**
	 * Returns the actual runtime GAML type of an expression's value.
	 * <p>
	 * This operator evaluates the expression and determines the type of its actual value at runtime, including complete
	 * parametric type information for containers. Unlike type_of, this recomputes the type based on the actual
	 * contents.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>
	 * {@code
	 * list my_list <- [1,2,3];
	 * string actual <- string(actual_type_of(my_list));  // Returns "list<int>" (computed from contents)
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param exp
	 *            the expression to evaluate and get the type of
	 * @return the actual runtime GAML type of the expression's value
	 */
	@operator (
			value = { "actual_type_of" },
			can_be_const = true,
			doc = @doc ("Returns the GAML type of the operand, computing the type of its actual value if a variable is passed"))
	@doc (
			value = "Returns the GAML type of the operand. Note that the type of a variable, e.g. `type_of(v)` may not correspond to its declared type, as `actual_type_of` will recompute the type of its contents. "
					+ "So, for instance, if v is declared as `list`, then `type_of` will return `list<int>` if it is equal to `[1,2]`. ",
			examples = { @example (
					value = "string(type_of(\"a string\"))",
					equals = "\"string\"",
					returnType = "string"),
					@example (
							value = "string(type_of([1,2,3,4,5]))",
							equals = "\"list<int>\"",
							returnType = "string"),
					@example (
							value = "string(type_of(g0))",
							equals = "\"point\"",
							returnType = "string")

			})
	@no_test
	public static IType<?> actualTypeOf(final IScope scope, final IExpression exp) {
		if (exp == null) return Types.NO_TYPE;
		Object obj = exp.value(scope);
		return GamaType.actualTypeOf(scope, obj);
	}

	/**
	 * Deserializes a type from a JSON representation.
	 * <p>
	 * The JSON map should contain:
	 * <ul>
	 * <li>"name" - the base type name</li>
	 * <li>"key" - (optional) the key type for parametric types</li>
	 * <li>"content" - (optional) the content type for parametric types</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param map2
	 *            the JSON map containing type data
	 * @return the deserialized type
	 */
	@Override
	public IType<?> deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		if (!map2.containsKey(IKeyword.NAME)) return getDefault();
		IType base = scope.getType(Cast.asString(scope, map2.get(IKeyword.NAME)));
		if (base instanceof IContainerType<?> ic && map2.size() > 1)
			return ic.of(scope.getType(Cast.asString(scope, map2.get(IKeyword.KEY))),
					scope.getType(Cast.asString(scope, map2.get("content"))));
		return base;
	}
}
