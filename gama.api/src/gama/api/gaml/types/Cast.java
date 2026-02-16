/*******************************************************************************************************
 *
 * Cast.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.util.stream.IntStream;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;

/**
 * Utility class providing GAML casting operators and type coercion helpers. Includes operators for type checking (is,
 * is_skill), type casting (as), specialized casting methods, and container creation (list_with, map_with,
 * parallel_list_with).
 *
 * @author drogoul
 */
@SuppressWarnings ({ "rawtypes" })
public class Cast {

	/**
	 * Checks if an object is an instance of a given GAML type. For agent types, verifies species membership; for other
	 * types, checks type assignability.
	 *
	 * @param scope
	 *            the execution scope
	 * @param object
	 *            the object to check
	 * @param typeExpression
	 *            an expression denoting the type to check against
	 * @return true if the object is an instance of the type
	 * @throws GamaRuntimeException
	 *             if type resolution fails
	 */
	@operator (
			value = { IKeyword.IS },
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.TYPE })
	@doc (
			value = "returns true if the left operand is of the right operand type, false otherwise",
			examples = { @example (
					value = "0 is int",
					equals = "true"),
					@example (
							value = "an_agent is node",
							equals = "true",
							isExecutable = false),
					@example (
							value = "1 is float",
							equals = "false") })
	public static Boolean isA(final IScope scope, final Object object, final IExpression typeExpression)
			throws GamaRuntimeException {
		final IType<?> type = asType(scope, typeExpression);
		if (type.isAgentType()) {
			final ISpecies species = scope.getModel().getSpecies(type.getSpeciesName());
			return object instanceof IAgent a && a.isInstanceOf(species, false);
		}
		return type.isAssignableFrom(GamaType.of(object));
	}

	/**
	 * Checks if an agent has a specific skill.
	 *
	 * @param scope
	 *            the execution scope
	 * @param agent
	 *            the agent to check
	 * @param skill
	 *            the skill name
	 * @return true if the agent's species implements the skill
	 */
	@operator (
			value = IKeyword.IS_SKILL,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.SKILL })
	@doc (
			value = "returns true if the left operand is an agent whose species implements the right-hand skill name",
			examples = { @example (
					value = "agentA is_skill 'moving'",
					equals = "true",
					isExecutable = false) })
	@test ("simulation is_skill 'moving' = false")
	public static Boolean isSkill(final IScope scope, final Object agent, final String skill) {
		return agent instanceof IAgent a && a.getSpecies().implementsSkill(skill);
	}

	/**
	 * Resolves an expression to a GAML type. Handles string type names, species, and type expressions.
	 *
	 * @param scope
	 *            the execution scope
	 * @param expr
	 *            the expression to resolve
	 * @return the resolved GAML type
	 * @throws GamaRuntimeException
	 *             if type resolution fails
	 */
	public static IType<?> asType(final IScope scope, final IExpression expr) throws GamaRuntimeException {
		final Object value = expr.value(scope);
		if (value instanceof String s) return scope.getModel().getDescription().getTypeNamed(s);
		if (value instanceof ISpecies is) return is.getDescription().getGamlType();
		return expr.getGamlType();
	}

	/**
	 * Casts an object to an agent type.
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @return the casted agent
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	public static IAgent asAgent(final IScope scope, final Object val) throws GamaRuntimeException {
		return (IAgent) Types.AGENT.cast(scope, val, null, false);
	}

	/**
	 * Generic type casting operator. Casts a value to the specified type.
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @param type
	 *            the target GAML type
	 * @return the casted value
	 */
	@operator (
			value = IKeyword.AS,
			type = ITypeProvider.SECOND_DENOTED_TYPE,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST })
	@doc (
			value = "Casting of the first argument into a given type",
			comment = "It is equivalent to the application of the type operator on the left operand.",
			examples = @example (
					value = "3.5 as int",
					returnType = "int",
					equals = "int(3.5)"))
	@test ("int(4.2) = (4.2 as int)")
	public static Object as(final IScope scope, final Object val, final IType type) {
		return type.cast(scope, val, null, false);
	}

	/**
	 * Casts a value to boolean.
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @return the boolean result
	 */
	public static Boolean asBool(final IScope scope, final Object val) {
		return GamaBoolType.staticCast(scope, val, null, false);
	}

	/**
	 * Casts a value to double (float).
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @return the double result
	 */
	public static Double asFloat(final IScope scope, final Object val) {
		return GamaFloatType.staticCast(scope, val, null, false);
	}

	/**
	 * Casts a value to integer.
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @return the integer result
	 */
	public static Integer asInt(final IScope scope, final Object val) {
		return GamaIntegerType.staticCast(scope, val, null, false);
	}

	/**
	 * Casts a value to string.
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @return the string result
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	public static String asString(final IScope scope, final Object val) throws GamaRuntimeException {
		return GamaStringType.staticCast(scope, val, false);
	}

	/**
	 * Parses a string as a signed integer in a specified radix.
	 *
	 * @param scope
	 *            the execution scope
	 * @param string
	 *            the string to parse
	 * @param radix
	 *            the radix (base) for parsing
	 * @return the integer result, or 0 if string is null/empty
	 * @throws GamaRuntimeException
	 *             if the string cannot be parsed as an integer
	 */
	@operator (
			value = "as_int",
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST })
	@doc (
			value = "parses the string argument as a signed integer in the radix specified by the second argument.",
			usages = { @usage ("if the left operand is nil or empty, as_int returns 0"),
					@usage ("if the left operand does not represent an integer in the specified radix, as_int throws an exception ") },
			examples = { @example (
					value = "'20' as_int 10",
					equals = "20"),
					@example (
							value = "'20' as_int 8",
							equals = "16"),
					@example (
							value = "'20' as_int 16",
							equals = "32"),
					@example (
							value = "'1F' as_int 16",
							equals = "31"),
					@example (
							value = "'hello' as_int 32",
							equals = "18306744") },
			see = { "int" })
	public static Integer asInt(final IScope scope, final String string, final Integer radix)
			throws GamaRuntimeException {
		if (string == null || string.isEmpty()) return 0;
		return GamaIntegerType.staticCast(scope, string, radix, false);
	}

	/**
	 * Creates a parallel-filled list of a given size. Useful for constant or thread-safe expressions.
	 *
	 * @param scope
	 *            the execution scope
	 * @param size
	 *            the list size
	 * @param init
	 *            the initializer expression (evaluated in parallel for each position)
	 * @return a new list filled with the expression value
	 */
	@operator (
			value = "parallel_list_with",
			content_type = ITypeProvider.TYPE_AT_INDEX + 2,
			can_be_const = false,
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "Creates a list with a size provided by the first operand, and filled with the second operand, the list is filled in parallel.",
			comment = "Note that the first operand  should be positive, and that the second one is evaluated for each position  in the list.\\nSome exception can happen in case the expression uses a random number generator that doesn't support parallel execution like mersenne.",
			see = { "list" },
			examples = { @example (
					value = "parallel_list_with(5,2)",
					equals = "[2,2,2,2,2]") })
	@test ("parallel_list_with(5,2) = [2,2,2,2,2]")
	public static IList parallel_list_with(final IScope scope, final Integer size, final IExpression init) {
		return GamaListFactory.create(scope, init, size, true);
	}

	/**
	 * Creates an iterator-based list of a given size. Each element is computed by evaluating the expression with a loop
	 * variable (each/index).
	 *
	 * @param scope
	 *            the execution scope
	 * @param eachName
	 *            the loop variable name
	 * @param size
	 *            the list size
	 * @param fillExpr
	 *            the expression to evaluate for each position (may reference the loop variable)
	 * @return a new list with the computed elements
	 */
	@operator (
			value = "list_with",
			iterator = true,
			content_type = ITypeProvider.TYPE_AT_INDEX + 3,
			can_be_const = false,
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "creates a list with a size provided by the first operand, and filled with the evaluation of the second operand. As with any iterator, the value of the current index can be retrieved with `each` or explicitly set using the `(x: ...` syntax",
			comment = "Note that the first operand  should be positive, otherwise an empty list is returned, and that the second one is evaluated for each position in the list.",
			see = { "list" },
			examples = { @example (
					value = "list_with(5,2)",
					equals = "[2,2,2,2,2]") })
	@test ("list_with(5,2) = [2,2,2,2,2]")
	@test ("5 list_with(i: i+1) = [1,2,3,4,5]")
	@test ("5 list_with string(each / 2) = ['0.0','0.5','1.0','1.5','2.0']")
	public static IList list_with(final IScope scope, final String eachName, final Integer size,
			final IExpression fillExpr) {
		if (fillExpr == null || size <= 0) return GamaListFactory.create(Types.NO_TYPE);
		final Object[] contents = new Object[size];
		final IType contentType = fillExpr.getGamlType();

		// Use parallel fill for constant expressions, sequential for others
		if (fillExpr.isConst()) {
			final Object value = fillExpr.value(scope);
			GamaExecutorService
					.executeThreaded(() -> IntStream.range(0, size).parallel().forEach(i -> contents[i] = value));
		} else {
			for (int i = 0; i < size; i++) {
				scope.setEach(eachName, i);
				contents[i] = fillExpr.value(scope);
			}
		}
		return GamaListFactory.create(scope, contentType, contents);
	}

	/**
	 * Creates an iterator-based map of a given size. Each entry is computed by evaluating a pair expression with a loop
	 * variable.
	 *
	 * @param scope
	 *            the execution scope
	 * @param eachName
	 *            the loop variable name
	 * @param size
	 *            the map size
	 * @param pairs
	 *            the pair expression to evaluate for each position (returns key::value pairs)
	 * @return a new map with the computed entries
	 */
	@operator (
			value = "map_with",
			iterator = true,
			expected_content_type = IType.PAIR,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 3,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 3,
			can_be_const = false,
			concept = { IConcept.CAST, IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "creates a map with a size provided by the first operand, and filled with the keys and values computed by the second pair operand. As any iterator, the second operand can make use of the index (obtained with each or explicitly set using the `(x: ...`  syntax)",
			comment = "Note that the first operand should be positive, otherwise an empty map is returned, and that the second is reevaluated for each new pair in the map.",
			see = { "map" })
	@test ("5 map_with(i: i::i+1) = [0::1,1::2,2::3,3::4,4::5]")
	@test ("5 map_with(i: i::i+1) = range(4) as_map (each::each+1)")
	public static IMap map_with(final IScope scope, final String eachName, final Integer size,
			final IExpression pairs) {
		if (pairs == null || size <= 0) return GamaMapFactory.create();
		IMap<?, Object> result =
				GamaMapFactory.create(pairs.getGamlType().getKeyType(), pairs.getGamlType().getContentType(), true);
		for (int i = 0; i < size; i++) {
			scope.setEach(eachName, i);
			result.addValue(scope, pairs.value(scope));
		}
		return result;
	}

	/**
	 * Casts a value to a species type.
	 *
	 * @param scope
	 *            the execution scope
	 * @param val
	 *            the value to cast
	 * @return the casted species
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	@operator (
			value = { "species_of" },
			content_type = ITypeProvider.TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.SPECIES })
	@doc (
			value = "casting of the operand to a species.",
			usages = { @usage ("if the operand is nil, returns nil;"),
					@usage ("if the operand is an agent, returns its species;"),
					@usage ("if the operand is a string, returns the species with this name (nil if not found);"),
					@usage ("otherwise, returns nil") },
			examples = { @example (
					value = "species(self)",
					equals = "the species of the current agent",
					isExecutable = false),
					@example (
							value = "species('node')",
							equals = "node",
							isExecutable = false),
					@example (
							value = "species([1,5,9,3])",
							equals = "nil",
							isExecutable = false),
					@example (
							value = "species(node1)",
							equals = "node",
							isExecutable = false) })
	@test ("species([1,5,9,3]) = nil")
	public static ISpecies asSpecies(final IScope scope, final Object val) throws GamaRuntimeException {
		return (ISpecies) Types.SPECIES.cast(scope, val, null, false);
	}

}
