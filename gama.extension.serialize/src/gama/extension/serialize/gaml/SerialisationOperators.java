/*******************************************************************************************************
 *
 * SerialisationOperators.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.gaml;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.api.utils.json.IJsonValue;
import gama.dev.DEBUG;
import gama.extension.serialize.binary.BinarySerialisation;
import gama.gaml.statements.save.GeoJSonSaver;

/**
 * The Class ReverseOperators.
 */
public class SerialisationOperators {

	static {
		DEBUG.OFF();
	}

	/**
	 * To gaml.
	 *
	 * @param val
	 *            the val
	 * @return the string
	 */
	@operator (
			value = "to_gaml",
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@doc (
			value = "Returns the literal description of an expression in gaml, in a format suitable to be reinterpreted and return a similar object",
			examples = { @example (
					value = "to_gaml(0)",
					equals = "'0'"),
					@example (
							value = "to_gaml(3.78)",
							equals = "'3.78'"),
					@example (
							value = "to_gaml({23, 4.0})",
							equals = "'{23.0,4.0,0.0}'"),
					@example (
							value = "to_gaml(rgb(255,0,125))",
							equals = "'rgb (255, 0, 125,255)'"),
					@example (
							value = "to_gaml('hello')",
							equals = "\"'hello'\""),
					@example (
							value = "to_gaml(a_graph)",
							equals = "([((1 as node)::(3 as node))::(5 as edge),((0 as node)::(3 as node))::(3 as edge),((1 as node)::(2 as node))::(1 as edge),((0 as node)::(2 as node))::(2 as edge),((0 as node)::(1 as node))::(0 as edge),((2 as node)::(3 as node))::(4 as edge)] as map ) as graph",
							isExecutable = false),
					@example (
							value = "to_gaml(node1)",
							equals = " 1 as node",
							isExecutable = false) },
			see = {})
	@tests ({
			@test ("to_gaml(true) = 'true'"),
			@test ("to_gaml(5::34) = '5::34'"),
			@test ("to_gaml([1,5,9,3]) = '[1,5,9,3]'"),
			@test ("to_gaml(['a'::345, 'b'::13, 'c'::12]) = \"map([\'a\'::345,\'b\'::13,\'c\'::12])\""),
			@test ("to_gaml([[3,5,7,9],[2,4,6,8]]) = '[[3,5,7,9],[2,4,6,8]]'"),
			@test ("to_gaml(#infinity) = \"#infinity\""),
			@test ("to_gaml(#nan) = \"#nan\""),
			@test ("to_gaml(12) = \"12\""),
			@test ("to_gaml(3.5) = \"3.5\""),
			@test ("to_gaml(true) = \"true\""),
			@test ("to_gaml(nil) = \"nil\""),
			@test ("to_gaml(\"text\") = \"'text'\""),
			@test ("to_gaml([\"a\"::1]) = \"map(['a'::1])\""),
			@test ("to_gaml(1::2) = \"1::2\""),
			@test ("to_gaml({1, 2}) = \"{1.0,2.0,0.0}\""),
			@test ("to_gaml(#red) = \"#red\""),
			@test ("to_gaml(matrix([[1, 2], [3, 4]])) = \"matrix<int>([[1,2],[3,4]])\"")
	})
	public static String toGaml(final Object val) {
		return StringUtils.toGaml(val, false);
	}

	/**
	 * To geojson.
	 *
	 * @param val
	 *            the val
	 * @return the string
	 */
	@operator (
			value = "to_geojson",
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@doc (
			value = "Returns a geojson representation of a population, a list of agents/geometries or an agent/geometry, provided with a CRS and a list of attributes to save",
			examples = { @example (
					value = "to_geojson(boat,\"EPSG:4326\",[\"color\"])",
					equals = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[100.51155642068785,3.514781609095577E-4,0.0]},\"properties\":{},\"id\":\"0\"}]}") },
			see = {})
	@no_test
	public static String toGeoJSon(final IScope scope, final IExpression spec, final String epsgCode,
			final IExpression attributesFacet) {

		final GeoJSonSaver gjsoner = new GeoJSonSaver();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			gjsoner.save(scope, spec, baos, epsgCode, attributesFacet);
			return baos.toString(StandardCharsets.UTF_8);

		} catch (final GamaRuntimeException e) {
			throw e;
		} catch (final Throwable e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/**
	 * To json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the string
	 * @date 31 oct. 2023
	 */
	@operator (
			value = { "to_json" },
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@test ("to_json(1) = '1'")
	@test ("to_json(1.24) = '1.24'")
	@test ("to_json('a string') = '\"a string\"'")
	@test ("to_json(#blue) = '{\"gaml_type\":\"rgb\",\"red\":0,\"green\":0,\"blue\":255,\"alpha\":255}'")
	@test ("to_json(font('Helvetica')) = '{\"gaml_type\":\"font\",\"name\":\"Helvetica\",\"style\":0,\"size\":12}'")
	@test ("to_json(point(20,10)) = '{\"gaml_type\":\"point\",\"x\":20.0,\"y\":10.0,\"z\":0.0}'")
	@doc (
			value = """
					Serializes any object/agent/simulation into a string, using the json format. A flag can be passed to enable/disable pretty printing (`false` by default).
					The format used by GAMA follows simple rules. `int`, `float`, `bool`, `string` values are outputted as they are. `nil` is outputted as `null`. A list is outputted as a json array. Any other object or agent is outputted as a json object. If this object possesses the attribute `gaml_type`, \
					it is an instance of the corresponding type, and the members that follow contain the attributes and the values necessary to reconstruct it. If it has the `agent_reference` attribute, its value represents the reference to an agent. If any reference to an agent is found, the \
					json string returned will be an object with two attributes: `gama_object`, the object containing the references, and `reference_table` a dictionary mapping the references to the json description of the agents (their `species`, `name`, `index`, and list of attributes). \
					This choice allows to manage cross references between agents""",
			see = { "serialize", "to_gaml" })
	public static String toJson(final IScope scope, final Object obj, final boolean pretty) {
		IJsonValue jsonValue = GAMA.getJsonEncoder().valueOf(obj);
		return pretty ? jsonValue.toPrettyPrint() : jsonValue.toCompactPrint();
	}

	/**
	 * To json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the string
	 * @date 31 oct. 2023
	 */
	@operator (
			value = { "to_json" },
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@doc (
			value = """
					Serializes any object/agent/simulation into a string, using the json format and no pretty printing.
					The format used by GAMA follows simple rules. `int`, `float`, `bool`, `string` values are outputted as they are. `nil` is outputted as `null`. A `list` is outputted as a json array. Any other object or agent is outputted as a json object. If this object possesses the "gaml_type" attribute, \
					it is an instance of the corresponding type, and the members that follow contain the attributes and the values necessary to reconstruct it. If it has the "agent_reference" attribute, its value represent the reference to an agent. If any reference to an agent is found, the \
					json string returned will be an object with two attributes: "gama_object", the object containing the references, and "reference_table" a dictionary mapping the references to the json description of the agents (their species, name, index, and list of attributes). \
					This choice allows to manage cross references between agents""",
			see = { "serialize", "to_gaml" })
	@no_test
	@tests ({
			@test ("to_json(1) = \"1\""),
			@test ("to_json(1.2) = \"1.2\""),
			@test ("to_json(\"\") = '\"\"'"),
			@test ("to_json(\"abcd\") = '\"abcd\"'"),
			@test ("to_json([]) = \"[]\""),
			@test ("to_json([1,\"a\",false]) = '[1,\"a\",false]'"),
			@test ("to_json([[1,2,3],[4,5,6]]) = '[[1,2,3],[4,5,6]]'"),
			@test ("to_json(nil) = 'null'"),
			@test ("map my_var <- [ \"x\"::\"abc\", \"y\"::#red, \"z\"::123, \"123\"::10.2, \"a\"::false ]; to_json(my_var) = '{\"x\":\"abc\",\"y\":{\"gaml_type\":\"rgb\",\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"z\":123,\"123\":10.2,\"a\":false}'"),
			@test ("to_json(3.5) = \"3.5\""),
			@test ("to_json(true) = \"true\""),
			@test ("to_json(nil) = \"null\""),
			@test ("to_json(\"text\") = '\"text\"'"),
			@test ("to_json([1, [2, [3]]]) = \"[1,[2,[3]]]\""),
			@test ("to_json([\"a\"::1, \"b\"::[1, 2]]) = '{\"a\":1,\"b\":[1,2]}'"),
			// GAML types with no JSON equivalent are tagged with their type
			@test ("to_json({1, 2, 3}) = '{\"gaml_type\":\"point\",\"x\":1.0,\"y\":2.0,\"z\":3.0}'"),
			@test ("to_json(#red) = '{\"gaml_type\":\"rgb\",\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255}'"),
			@test ("to_json(1::2) = '{\"gaml_type\":\"pair<int, int>\",\"key\":1,\"value\":2}'")
	})
	public static String toJson(final IScope scope, final Object obj) {
		return toJson(scope, obj, false);
	}

	/**
	 * Serialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the string
	 * @date 28 oct. 2023
	 */
	@operator (
			value = { "serialize", "to_binary" },
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@doc (
			value = "Serializes any object/agent/simulation into a string, using the `binary` format\n"
					+ "The result of this operator can be then used in the `from` facet of `restore` or `create` statements in case of agents, or using `deserialize` for other items",
			see = { "to_json", "to_gaml" })
	@no_test ()
	public static String serialize(final IScope scope, final Object obj) {
		return BinarySerialisation.saveToString(scope, obj);
	}

	/**
	 * Unserialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param t
	 *            the t
	 * @return the object
	 * @date 29 sept. 2023
	 */
	@operator (
			value = { "deserialize", "from_binary" },
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@tests ({
			@test ("from_binary(to_binary(25+5)) = 30"),
			@test ("from_binary(to_binary([1,2,4])) = [1,2,4]"),
			@test ("from_binary(to_binary(1)) = 1"),
			@test ("from_binary(to_binary(1.2)) = 1.2"),
			@test ("from_binary(to_binary(\"\")) = \"\""),
			@test ("from_binary(to_binary(\"abcd\")) = \"abcd\""),
			@test ("from_binary(to_binary([])) = []"),
			@test ("from_binary(to_binary([1, \"a\", false])) = [1, \"a\", false]"),
			@test ("from_binary(to_binary([[1, 2, 3], [4, 5, 6]])) = [[1, 2, 3], [4, 5, 6]]"),
			@test ("from_binary(to_binary(nil)) = nil"),
			@test ("map my_var <- [\"x\"::\"abc\", \"y\"::#red, \"z\"::123, \"123\"::10.2, \"a\"::false]; from_binary(to_binary(my_var)) = my_var"),
			@test ("map my_roundtrip <- [\"x\"::\"abc\", \"y\"::#red, \"z\"::123, \"123\"::10.2, \"a\"::false, \"e\"::[1,2,3]]; from_binary(to_binary(my_roundtrip)) = my_roundtrip"),
			@test ("from_binary(to_binary(#infinity)) = #infinity"),
			@test ("from_binary(to_binary(map(['x'::#infinity]))) = map(['x'::#infinity])"),
			@test ("from_binary(to_binary(-#infinity)) = -#infinity"),
			@test ("from_binary(to_binary(map(['x'::-#infinity]))) = map(['x'::-#infinity])"),
			@test ("from_binary(to_binary(#nan)) = #nan"),
			@test ("from_binary(to_binary(map([\"x\"::#nan]))) = map([\"x\"::#nan])"),
			@test ("from_binary(to_binary('')) = ''"),
			@test ("from_binary(to_binary(12)) = 12"),
			@test ("from_binary(to_binary(\"text\")) = \"text\""),
			@test ("from_binary(to_binary([1, \"a\", 2.5, true])) = [1, \"a\", 2.5, true]"),
			@test ("from_binary(to_binary([\"k\"::{1, 2}, \"c\"::#red])) = [\"k\"::{1, 2}, \"c\"::#red]"),
			@test ("from_binary(to_binary(matrix([[1, 2], [3, 4]]))) = matrix([[1, 2], [3, 4]])"),
			@test ("from_binary(to_binary(date(\"2026-01-02T03:04:05\"))) = date(\"2026-01-02T03:04:05\")"),
			@test ("deserialize(serialize(3.5)) = 3.5"),
			@test ("deserialize(serialize([1, 2, 3])) = [1, 2, 3]"),
			@test ("deserialize(serialize([\"k\"::[1, 2]])) = [\"k\"::[1, 2]]")
	})
	@doc (
			value = "Deserializes an object precedently serialized using `serialize` or `to_binary`."
					+ "It is safer to deserialize agents or simulations with the 'restore' or 'create' statements rather than with this operator.",
			see = { "from_gaml", "from_json" })
	public static Object unserialize(final IScope scope, final String s) {
		return BinarySerialisation.createFromString(scope, s);
	}

	/**
	 * Unserialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param t
	 *            the t
	 * @return the object
	 * @date 29 sept. 2023
	 */
	@operator (
			value = { "from_json" },
			can_be_const = true,
			type = ITypeProvider.DENOTED_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.SERIALIZE })
	@doc (
			value = "Deserializes an object precedently serialized using 'to_json' (or an arbitrary json string obtained elsewhere). Agents and populations are not supported yet (i.e. they will return maps)",
			see = { "from_gaml", "from_binary" })
	@no_test
	@tests ({
			@test ("from_json(\"1\") = 1"),
			@test ("from_json(\"1.2\") = 1.2"),
			@test ("from_json('\"\"') = \"\""),
			@test ("from_json('\"abcd\"') = \"abcd\""),
			@test ("from_json('[]') = []"),
			@test ("from_json('[1,\"a\",false]') = [1,\"a\",false]"),
			@test ("from_json('[[1,2,3],[4,5,6]]') = [[1,2,3],[4,5,6]]"),
			@test ("from_json(\"null\") = nil"),
			@test ("map my_var <- [ \"x\"::\"abc\", \"y\"::#red, \"z\"::123, \"123\"::10.2, \"a\"::false ]; from_json('{\"x\":\"abc\",\"y\":{\"gaml_type\":\"rgb\",\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"z\":123,\"123\":10.2,\"a\":false}') = my_var"),
			@test ("map my_var2 <- [\"x\"::\"abc\",\"y\"::#red,\"z\"::123,\"123\"::10.2,\"a\"::false, \"e\"::[1,2,3]]; from_json(to_json(my_var2)) = my_var2"),
			@test ("from_json(\"3\") = 3"),
			@test ("from_json('\"text\"') = \"text\""),
			@test ("from_json(\"[]\") = []"),
			@test ("from_json('[1, \"a\", 2.5, true, null]') = [1, \"a\", 2.5, true, nil]"),
			@test ("map<string, unknown> parsed <-  map<string, unknown>(from_json('{\"a\": 1.5, \"b\": \"s\", \"c\": true, \"d\": null, \"e\": {\"f\": [1, 2]}}')); float(parsed[\"a\"]) = 1.5"),
			@test ("map<string, unknown> parsed2 <- map<string, unknown>(from_json('{\"a\": 1.5, \"b\": \"s\", \"c\": true, \"d\": null, \"e\": {\"f\": [1, 2]}}')); parsed2[\"b\"] = \"s\""),
			@test ("map<string, unknown> parsed3 <- map<string, unknown>(from_json('{\"a\": 1.5, \"b\": \"s\", \"c\": true, \"d\": null, \"e\": {\"f\": [1, 2]}}')); bool(parsed3[\"c\"]) = true"),
			@test ("map<string, unknown> parsed4 <- map<string, unknown>(from_json('{\"a\": 1.5, \"b\": \"s\", \"c\": true, \"d\": null, \"e\": {\"f\": [1, 2]}}')); parsed4[\"d\"] = nil"),
			@test ("map<string, unknown> parsed5 <- map<string, unknown>(from_json('{\"a\": 1.5, \"b\": \"s\", \"c\": true, \"d\": null, \"e\": {\"f\": [1, 2]}}')); map(parsed5[\"e\"])[\"f\"] = [1, 2]"),
			@test ("from_json(to_json([1, [2, [3]]])) = [1, [2, [3]]]"),
			@test ("from_json(to_json([\"a\"::1, \"b\"::[1, 2]])) = [\"a\"::1, \"b\"::[1, 2]]"),
			@test ("from_json(to_json({1, 2, 3})) = {1, 2, 3}"),
			@test ("from_json(to_json(#red)) = #red"),
			@test ("from_json(to_json(1::2)) = (1::2)"),
			@test ("from_json(to_json(matrix([[1, 2], [3, 4]]))) = matrix([[1, 2], [3, 4]])"),
			@test ("from_json(to_json(date(\"2026-01-02T03:04:05\"))) = date(\"2026-01-02T03:04:05\")"),
			@test ("from_json(to_json([\"k\"::{1, 2}])) = [\"k\"::{1, 2}]")
	})
	public static Object fromJson(final IScope scope, final String s) {
		return GAMA.getJsonEncoder().parse(s).toGamlValue(scope);
	}

	/**
	 * Op eval gaml.
	 *
	 * @param scope
	 *            the scope
	 * @param gaml
	 *            the gaml
	 * @return the object
	 */
	@operator (
			value = { "from_gaml", "eval_gaml" },
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.CASTING },
			concept = { IConcept.SYSTEM, IConcept.SERIALIZE })
	@doc (
			value = "Evaluates/deserialises the given GAML string into a value.",
			examples = { @example (
					value = "eval_gaml(\"2+3\")",
					equals = "5") })
	@tests ({
			@test ("string expr <- \"10 + 20\"; int result <- int(eval_gaml(expr)); result = 30"),
			@test ("string expr2 <- \"[1, 2, 3] collect (each * 2)\"; list<int> l_result <- list<int>(eval_gaml(expr2)); length(l_result) = 3"),
			@test ("string expr22 <- \"[1, 2, 3] collect (each * 2)\"; list<int> l_result2 <- list<int>(eval_gaml(expr22)); l_result2[1] = 4"),
			@test ("from_gaml(to_gaml(\"it's\")) = \"it's\""),
			@test ("from_gaml(to_gaml(\"say \\\"hi\\\"\")) = \"say \\\"hi\\\"\""),
			@test ("from_gaml(to_gaml(\"a\\\\b\")) = \"a\\\\b\""),
			@test ("from_gaml(\"12\") = 12"),
			@test ("from_gaml(\"[1, 2] + [3]\") = [1, 2, 3]"),
			@test ("from_gaml(\"3 * 4\") = 12"),
			@test ("from_gaml(\"'a' + 'b'\") = \"ab\""),
			@test ("from_gaml(to_gaml(12)) = 12"),
			@test ("from_gaml(to_gaml(3.5)) = 3.5"),
			@test ("from_gaml(to_gaml(\"text\")) = \"text\""),
			@test ("from_gaml(to_gaml([1, 2, 3])) = [1, 2, 3]"),
			@test ("from_gaml(to_gaml([\"a\"::1, \"b\"::2])) = [\"a\"::1, \"b\"::2]"),
			@test ("from_gaml(to_gaml(1::2)) = (1::2)"),
			@test ("from_gaml(to_gaml({1, 2, 3})) = {1, 2, 3}"),
			@test ("from_gaml(to_gaml(#red)) = #red"),
			@test ("from_gaml(to_gaml(matrix([[1, 2], [3, 4]]))) = matrix([[1, 2], [3, 4]])"),
			@test ("from_gaml(to_gaml(date(\"2026-01-02T03:04:05\"))) = date(\"2026-01-02T03:04:05\")")
	})
	public static Object opEvalGaml(final IScope scope, final String gaml) {
		final IAgent agent = scope.getAgent();
		final IDescription d = agent.getSpecies().getDescription();
		try {
			final IExpression e = GAML.getExpressionFactory().createExpr(gaml, d);
			return scope.evaluate(e, agent).getValue();
		} catch (final GamaRuntimeException e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Error in evaluating Gaml code : '" + gaml
					+ "' in " + scope.getAgent() + StringUtils.LN + "Reason: " + e.getMessage(), scope), false);

			return null;
		}

	}

}