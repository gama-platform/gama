/*******************************************************************************************************
 *
 * Cast.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.model.IModel;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.graph.IGraph;
import gama.core.util.matrix.IMatrix;
import gama.gaml.expressions.IExpression;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaBoolType;
import gama.gaml.types.GamaColorType;
import gama.gaml.types.GamaFloatType;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.GamaGraphType;
import gama.gaml.types.GamaIntegerType;
import gama.gaml.types.GamaListType;
import gama.gaml.types.GamaMatrixType;
import gama.gaml.types.GamaPairType;
import gama.gaml.types.GamaPointType;
import gama.gaml.types.GamaStringType;
import gama.gaml.types.GamaTopologyType;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 15 d�c. 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class Cast {

	/**
	 * Checks if is A.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the boolean
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
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
	public static Boolean isA(final IScope scope, final Object a, final IExpression b) throws GamaRuntimeException {
		final IType<?> type = asType(scope, b);
		if (type.isAgentType()) {
			final ISpecies s = scope.getModel().getSpecies(type.getSpeciesName());
			if (a instanceof IAgent) return ((IAgent) a).isInstanceOf(s, false);
			return false;
		}
		return type.isAssignableFrom(GamaType.of(a));
	}

	/**
	 * Checks if is skill.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param skill
	 *            the skill
	 * @return the boolean
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
	public static Boolean isSkill(final IScope scope, final Object a, final String skill) {
		if (!(a instanceof IAgent)) return false;
		final ISpecies s = ((IAgent) a).getSpecies();
		return s.implementsSkill(skill);
	}

	/**
	 * As type.
	 *
	 * @param scope
	 *            the scope
	 * @param expr
	 *            the expr
	 * @return the i type
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IType asType(final IScope scope, final IExpression expr) throws GamaRuntimeException {
		final Object value = expr.value(scope);
		if (value instanceof String) {
			final IModel m = scope.getModel();
			return m.getDescription().getTypeNamed((String) value);
		}
		if (value instanceof ISpecies) return ((ISpecies) value).getDescription().getGamlType();
		return expr.getGamlType();
	}

	/**
	 * As graph.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i graph
	 */
	public static IGraph asGraph(final IScope scope, final Object val) {
		return GamaGraphType.staticCast(scope, val, null, false);
	}

	/**
	 * As topology.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// @operator (
	// value = IKeyword.TOPOLOGY,
	// content_type = IType.GEOMETRY,
	// category = { IOperatorCategory.CASTING },
	// concept = { IConcept.CAST, IConcept.TOPOLOGY })
	// @doc (
	// value = "casting of the operand to a topology.",
	// usages = { @usage ("if the operand is a topology, returns the topology itself;"),
	// @usage ("if the operand is a spatial graph, returns the graph topology associated;"),
	// @usage ("if the operand is a population, returns the topology of the population;"),
	// @usage ("if the operand is a shape or a geometry, returns the continuous topology bounded by the geometry;"),
	// @usage ("if the operand is a matrix, returns the grid topology associated"),
	// @usage ("if the operand is another kind of container, returns the multiple topology associated to the
	// container"),
	// @usage ("otherwise, casts the operand to a geometry and build a topology from it.") },
	// examples = { @example (
	// value = "topology(0)",
	// equals = "nil",
	// isExecutable = true),
	// @example (
	// value = "topology(a_graph) --: Multiple topology in POLYGON ((24.712119771887785 7.867357373616512,
	// 24.712119771887785 61.283226839310565, 82.4013676510046 7.867357373616512)) "
	// + "at location[53.556743711446195;34.57529210646354]",
	// isExecutable = false) },
	// see = { "geometry" })
	public static ITopology asTopology(final IScope scope, final Object val) throws GamaRuntimeException {
		return GamaTopologyType.staticCast(scope, val, false);
	}

	/**
	 * As agent.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IAgent asAgent(final IScope scope, final Object val) throws GamaRuntimeException {
		return (IAgent) Types.AGENT.cast(scope, val, null, false);
	}

	/**
	 * As.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param type
	 *            the type
	 * @return the object
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
		// WARNING copy is set explicity to false
		return type.cast(scope, val, null, false);
	}

	/**
	 * As bool.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param copy
	 *            the copy
	 * @return the boolean
	 */
	public static Boolean asBool(final IScope scope, final Object val, final boolean copy) {
		// copy not passed
		return GamaBoolType.staticCast(scope, val, null, false);
	}

	/**
	 * As bool.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the boolean
	 */
	public static Boolean asBool(final IScope scope, final Object val) {
		// copy not passed
		return asBool(scope, val, false);
	}

	/**
	 * As color.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param copy
	 *            the copy
	 * @return the gama color
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static GamaColor asColor(final IScope scope, final Object val, final boolean copy)
			throws GamaRuntimeException {
		// copy not passed
		return GamaColorType.staticCast(scope, val, null, false);
	}

	/**
	 * As color.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the gama color
	 */
	public static GamaColor asColor(final IScope scope, final Object val) {
		return asColor(scope, val, false);
	}

	/**
	 * As float.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the double
	 */
	public static Double asFloat(final IScope scope, final Object val) {
		return GamaFloatType.staticCast(scope, val, null, false);
	}

	/**
	 * As geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param copy
	 *            the copy
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IShape asGeometry(final IScope scope, final Object s, final boolean copy)
			throws GamaRuntimeException {
		return GamaGeometryType.staticCast(scope, s, null, copy);
	}

	/**
	 * As geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IShape asGeometry(final IScope scope, final Object s) throws GamaRuntimeException {
		return asGeometry(scope, s, false);
	}

	/**
	 * As int.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the integer
	 */
	public static Integer asInt(final IScope scope, final Object val) {
		return GamaIntegerType.staticCast(scope, val, null, false);
	}

	/**
	 * As pair.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param copy
	 *            the copy
	 * @return the gama pair
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static GamaPair asPair(final IScope scope, final Object val, final boolean copy)
			throws GamaRuntimeException {
		return GamaPairType.staticCast(scope, val, Types.NO_TYPE, Types.NO_TYPE, copy);
	}

	/**
	 * As string.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static String asString(final IScope scope, final Object val) throws GamaRuntimeException {
		return GamaStringType.staticCast(scope, val, false);
	}

	/**
	 * As point.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param copy
	 *            the copy
	 * @return the gama point
	 */
	public static GamaPoint asPoint(final IScope scope, final Object val, final boolean copy) {
		GamaPoint result = GamaPointType.staticCast(scope, val, copy);
		return result == null ? null : result;
	}

	/**
	 * As point.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the gama point
	 */
	public static GamaPoint asPoint(final IScope scope, final Object val) {
		return asPoint(scope, val, false);
	}

	/**
	 * As map.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param copy
	 *            the copy
	 * @return the i map
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IMap asMap(final IScope scope, final Object val, final boolean copy) throws GamaRuntimeException {
		return (IMap) Types.MAP.cast(scope, val, null, copy);
	}

	/**
	 * As int.
	 *
	 * @param scope
	 *            the scope
	 * @param string
	 *            the string
	 * @param radix
	 *            the radix
	 * @return the integer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
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
	 * As list.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IList asList(final IScope scope, final Object val) throws GamaRuntimeException {
		return GamaListType.staticCast(scope, val, null, false);
	}

	/**
	 * To list.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * List with.
	 *
	 * @param scope
	 *            the scope
	 * @param size
	 *            the size
	 * @param init
	 *            the init
	 * @return the i list
	 */
	@operator (
			value = "to_list",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			can_be_const = true,
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "casts the operand to a list, making an explicit copy if it is already a list or a subtype of list (interval, population, etc.)",
			see = { "list" })
	@no_test
	public static IList toList(final IScope scope, final Object val) throws GamaRuntimeException {
		// copy is set to true in order to force the creation of a new list
		return GamaListType.staticCast(scope, val, null, true);
	}


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
	 * As matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IMatrix asMatrix(final IScope scope, final Object val) throws GamaRuntimeException {
		return asMatrix(scope, val, null);
	}
	
	@operator (
			value = "parallel_matrix_with",
			content_type = ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "Creates a matrix with a size provided by the first operand, and filled with the second operand. The given expression, unless constant, is evaluated for each cell and is done in parallel.",
			comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.\nIf run in parallel, some exception can happen in case the expression uses a random number generator that doesn't support parallel execution like mersenne.",
			see = { IKeyword.MATRIX, "as_matrix" })
	@test ("{2,2} matrix_with (1) = matrix([1,1],[1,1])")
	public static IMatrix parallel_matrix_with(final IScope scope, final GamaPoint size, final IExpression init) {
		if (size == null) throw GamaRuntimeException.error("A nil size is not allowed for matrices", scope);
		return GamaMatrixType.with(scope, init, size, true);
	}
	


	
	/**
	 * As matrix.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @param size
	 *            the size
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "as_matrix",
			content_type = ITypeProvider.FIRST_CONTENT_TYPE_OR_TYPE,
			can_be_const = true,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.CAST, IConcept.CONTAINER })
	@doc (
			value = "casts the left operand into a matrix with right operand as preferred size",
			comment = "This operator is very useful to cast a file containing raster data into a matrix."
					+ "Note that both components of the right operand point should be positive, otherwise an exception is raised."
					+ "The operator as_matrix creates a matrix of preferred size. It fills in it with elements of the left operand until the matrix is full "
					+ "If the size is to short, some elements will be omitted. Matrix remaining elements will be filled in by nil.",
			usages = { @usage ("if the right operand is nil, as_matrix is equivalent to the matrix operator") },
			see = { IKeyword.MATRIX })
	@test ("as_matrix('a', {2,3}) = matrix(['a','a','a'],['a','a','a'])")
	@test ("as_matrix(1.0, {2,2}) = matrix([1.0,1.0],[1.0,1.0])")
	public static IMatrix asMatrix(final IScope scope, final Object val, final GamaPoint size)
			throws GamaRuntimeException {
		return GamaMatrixType.staticCast(scope, val, size, Types.NO_TYPE, false);
	}

	/**
	 * As species.
	 *
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the i species
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
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
