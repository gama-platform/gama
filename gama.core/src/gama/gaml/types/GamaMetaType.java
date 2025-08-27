/*******************************************************************************************************
 *
 * GamaMetaType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;

/**
 * The Class GamaMetaType.
 */
@type (
		name = "gaml_type",
		id = IType.TYPE,
		wraps = { IType.class },
		doc = @doc ("Metatype of all types in GAML"))
public class GamaMetaType extends GamaType<IType<?>> {

	@Override
	public boolean canCastToConst() {
		return true;
	}

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
	 * Static cast.
	 *
	 * @param obj
	 *            the obj
	 * @return the i type
	 */
	public static IType<?> staticCast(final Object obj) {
		return GamaType.of(obj);
	}

	@Override
	public IType<?> getDefault() { return Types.NO_TYPE; }

	/**
	 * Type of.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i type
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
	 * Type of.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i type
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
	public static IType<?> actualTypeOf(final IScope scope, final IExpression obj) {
		if (obj == null) return Types.NO_TYPE;
		return obj.computeRuntimeType(scope);
	}

	@Override
	public IType<?> deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		if (!map2.containsKey("name")) return getDefault();
		IType base = scope.getType(Cast.asString(scope, map2.get("name")));
		if (base instanceof IContainerType<?> ic && map2.size() > 1)
			return ic.of(scope.getType(Cast.asString(scope, map2.get("key"))),
					scope.getType(Cast.asString(scope, map2.get("content"))));
		return base;
	}
}
